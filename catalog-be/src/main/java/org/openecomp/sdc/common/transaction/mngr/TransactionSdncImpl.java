/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.common.transaction.mngr;

import fj.data.Either;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.impl.ESCatalogDAO;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.transaction.api.*;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.*;
import org.openecomp.sdc.common.transaction.impl.ESAction;
import org.openecomp.sdc.common.transaction.impl.ESRollbackHandler;
import org.openecomp.sdc.common.transaction.impl.JanusGraphCommitHandler;
import org.openecomp.sdc.common.transaction.impl.JanusGraphRollbackHandler;
import org.openecomp.sdc.common.util.MethodActivationStatusEnum;

import java.util.ArrayList;
import java.util.List;

public class TransactionSdncImpl implements ITransactionSdnc {

    // TODO test using slf4j-test and make this final
    private static Logger log = Logger.getLogger(TransactionSdncImpl.class);
    private boolean lastActionAlreadyCalled;
    private RollbackManager rollbackManager;
    private CommitManager commitManager;
    private ESCatalogDAO esCatalogDao;
    private JanusGraphGenericDao janusGraphGenericDao;
    private Integer transactionId;
    private TransactionStatusEnum status;
    private String userId, actionType;

    TransactionSdncImpl(Integer transactionId, String userId, ActionTypeEnum actionTypeEnum, ESCatalogDAO esCatalogDao, JanusGraphGenericDao janusGraphGenericDao) {
        this.esCatalogDao = esCatalogDao;
        this.janusGraphGenericDao = janusGraphGenericDao;
        this.transactionId = transactionId;
        this.userId = userId;
        actionType = actionTypeEnum.name();
        rollbackManager = new RollbackManager(transactionId, userId, actionType, initRollbackHandlers());
        commitManager = new CommitManager(transactionId, userId, actionType, initCommitHandlers());
        status = TransactionStatusEnum.OPEN;

    }

    private List<ICommitHandler> initCommitHandlers() {
        List<ICommitHandler> commitHandlers = new ArrayList<>();
        commitHandlers.add(new JanusGraphCommitHandler(janusGraphGenericDao));
        return commitHandlers;
    }

    private List<RollbackHandler> initRollbackHandlers() {
        List<RollbackHandler> rolebackHandlers = new ArrayList<>();
        rolebackHandlers.add(new JanusGraphRollbackHandler(transactionId, userId, actionType,
            janusGraphGenericDao));
        rolebackHandlers.add(new ESRollbackHandler(transactionId, userId, actionType));
        return rolebackHandlers;
    }

    private <T> Either<T, TransactionCodeEnum> invokeAction(boolean isLastAction, IDBAction dbAction, DBTypeEnum dbType) {

        Either<T, DBActionCodeEnum> actionResult;
        log.debug(LogMessages.INVOKE_ACTION, transactionId, dbType.name(), userId, actionType);
        if (isLastAction) {
            actionResult = getLastActionResult(dbAction, dbType);
        } else {
            actionResult = getActionResult(dbAction, dbType);
        }

        Either<T, TransactionCodeEnum> result;
        boolean isRollbackNedded = actionResult.isRight();
        if (isRollbackNedded) {
            TransactionCodeEnum transactionCode = transactionRollback();
            result = Either.right(transactionCode);
        } else {
            result = Either.left(actionResult.left().value());
        }
        return result;
    }

    private TransactionCodeEnum transactionRollback() {

        TransactionCodeEnum result;
        DBActionCodeEnum transactionRollback = rollbackManager.transactionRollback();
        if (transactionRollback == DBActionCodeEnum.SUCCESS) {
            result = TransactionCodeEnum.ROLLBACK_SUCCESS;
            log.info(LogMessages.ROLLBACK_SUCCEEDED_GENERAL, transactionId, userId, actionType);
            log.info(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_SUCCEEDED_GENERAL, transactionId, userId, actionType);

        } else {
            result = TransactionCodeEnum.ROLLBACK_FAILED;
            BeEcompErrorManager.getInstance().logBeSystemError("transactionCommit for transaction " + transactionId);

            log.info(LogMessages.ROLLBACK_FAILED_GENERAL, transactionId, userId, actionType);
            log.debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_FAILED_GENERAL, transactionId, userId, actionType);
        }
        return result;
    }

    public <T> Either<T, TransactionCodeEnum> invokeJanusGraphAction(boolean isLastAction, IDBAction dbAction) {
        Either<T, TransactionCodeEnum> result;
        if (status == TransactionStatusEnum.OPEN) {
            result = invokeAction(isLastAction, dbAction, DBTypeEnum.JANUSGRAPH);
        } else {
            result = handleActionOnClosedTransaction();
        }
        updateTransactionStatus(result);
        return result;
    }

    public <T> Either<T, TransactionCodeEnum> invokeGeneralDBAction(boolean isLastAction, DBTypeEnum dbType, IDBAction dbAction, IDBAction dbRollbackAction) {

        Either<T, TransactionCodeEnum> result;
        MethodActivationStatusEnum addingHandlerResult;
        if (status == TransactionStatusEnum.OPEN) {
            log.debug(LogMessages.PRE_INVOKE_ACTION, transactionId, dbType.name(), userId, actionType);
            Either<RollbackHandler, MethodActivationStatusEnum> eitherRollbackHandler = rollbackManager.getRollbackHandler(dbType);

            if (eitherRollbackHandler.isLeft()) {
                RollbackHandler rollbackHandler = eitherRollbackHandler.left().value();
                addingHandlerResult = rollbackHandler.addRollbackAction(dbRollbackAction);
            } else {
                addingHandlerResult = addToNewRollbackHandler(dbType, dbRollbackAction);
            }

            if (addingHandlerResult == MethodActivationStatusEnum.SUCCESS) {
                result = invokeAction(isLastAction, dbAction, dbType);
            } else {
                result = Either.right(TransactionCodeEnum.PREPARE_ROLLBACK_FAILED);
            }
        } else {
            result = handleActionOnClosedTransaction();
        }
        updateTransactionStatus(result);
        return result;
    }

    private MethodActivationStatusEnum addToNewRollbackHandler(DBTypeEnum dbType, IDBAction dbRollbackAction) {
        log.debug(LogMessages.CREATE_ROLLBACK_HANDLER, dbType.name(), transactionId, userId, actionType);
        MethodActivationStatusEnum result;

        Either<RollbackHandler, MethodActivationStatusEnum> eitherRollbackHandler = rollbackManager.createRollbackHandler(dbType);
        if (eitherRollbackHandler.isRight()) {
            result = eitherRollbackHandler.right().value();
            BeEcompErrorManager.getInstance().logBeSystemError("TransactionManager -  addToNewRollbackHandler");
            log.info(LogMessages.FAILED_CREATE_ROLLBACK, dbType.name(), transactionId, userId, actionType);
            log.debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.FAILED_CREATE_ROLLBACK, dbType.name(), transactionId, userId, actionType);
        } else {
            RollbackHandler rollbackHandler = eitherRollbackHandler.left().value();
            rollbackHandler.addRollbackAction(dbRollbackAction);
            result = MethodActivationStatusEnum.SUCCESS;
        }

        return result;
    }

    public Either<DBActionCodeEnum, TransactionCodeEnum> invokeESAction(boolean isLastAction, ESActionTypeEnum esActiontype, ESArtifactData artifactData) {

        Either<DBActionCodeEnum, TransactionCodeEnum> result;
        if (status == TransactionStatusEnum.OPEN) {
            Either<RollbackHandler, MethodActivationStatusEnum> eitherEsHandler = rollbackManager.getRollbackHandler(DBTypeEnum.ELASTIC_SEARCH);
            if (eitherEsHandler.isRight()) {
                result = Either.right(TransactionCodeEnum.INTERNAL_ERROR);
            } else {
                ESRollbackHandler esHandler = (ESRollbackHandler) eitherEsHandler.left().value();

                Either<ESAction, MethodActivationStatusEnum> eitherEsRollbackAction = esHandler.buildEsRollbackAction(esCatalogDao, artifactData, esActiontype);
                if (eitherEsRollbackAction.isLeft()) {
                    esHandler.addRollbackAction(eitherEsRollbackAction.left().value());
                    result = invokeAction(isLastAction, new ESAction(esCatalogDao, artifactData, esActiontype), DBTypeEnum.ELASTIC_SEARCH);
                } else {
                    result = Either.right(TransactionCodeEnum.PREPARE_ROLLBACK_FAILED);
                }

            }
        } else {
            result = handleActionOnClosedTransaction();
        }
        updateTransactionStatus(result);
        return result;
    }

    private <T> void updateTransactionStatus(Either<T, TransactionCodeEnum> result) {
        if (result.isRight()) {
            updateTransactionStatus(result.right().value());
        }

    }

    private <T> Either<T, TransactionCodeEnum> handleActionOnClosedTransaction() {
        Either<T, TransactionCodeEnum> result = Either.right(TransactionCodeEnum.TRANSACTION_CLOSED);
        log.debug(LogMessages.ACTION_ON_CLOSED_TRANSACTION, transactionId, userId, actionType);
        log.info(TransactionUtils.TRANSACTION_MARKER, LogMessages.ACTION_ON_CLOSED_TRANSACTION, transactionId, userId, actionType);
        return result;
    }

    private <T> Either<T, DBActionCodeEnum> getLastActionResult(IDBAction dataBaseAction, DBTypeEnum dbType) {
        Either<T, DBActionCodeEnum> result;
        if (isLastActionAlreadyCalled()) {
            result = Either.right(DBActionCodeEnum.FAIL_MULTIPLE_LAST_ACTION);
            BeEcompErrorManager.getInstance().logBeSystemError("TransactionManager - getLastActionResult");
            log.debug(LogMessages.DOUBLE_FINISH_FLAG_ACTION, transactionId, dbType.name(), userId, actionType);
            log.info(TransactionUtils.TRANSACTION_MARKER, LogMessages.DOUBLE_FINISH_FLAG_ACTION, transactionId, dbType.name(), userId, actionType);
        } else {
            setLastActionAlreadyCalled(true);
            result = getActionResult(dataBaseAction, dbType);
        }
        return result;
    }

    private <T> Either<T, DBActionCodeEnum> getActionResult(IDBAction dataBaseAction, DBTypeEnum dbType) {
        Either<T, DBActionCodeEnum> result;
        try {
            T doAction = dataBaseAction.doAction();
            result = Either.left(doAction);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeSystemError("TransactionManager - getActionResult");
            log.debug(LogMessages.DB_ACTION_FAILED_WITH_EXCEPTION, dbType.name(), transactionId, e.getMessage(), userId, actionType, e);
            log.info(TransactionUtils.TRANSACTION_MARKER, LogMessages.DB_ACTION_FAILED_WITH_EXCEPTION, dbType.name(), transactionId, e.getMessage(), userId, actionType);

            result = Either.right(DBActionCodeEnum.FAIL_GENERAL);
        }
        return result;
    }

    public TransactionCodeEnum finishTransaction() {
        TransactionCodeEnum result;
        if (status == TransactionStatusEnum.OPEN) {
            DBActionCodeEnum transactionCommit = commitManager.transactionCommit();
            if (transactionCommit == DBActionCodeEnum.SUCCESS) {
                result = TransactionCodeEnum.SUCCESS;
                status = TransactionStatusEnum.CLOSED;
            } else {
                result = transactionRollback();
            }
        } else {
            BeEcompErrorManager.getInstance().logBeSystemError("TransactionManager - finishTransaction");
            log.debug(LogMessages.COMMIT_ON_CLOSED_TRANSACTION, transactionId, status.name(), userId, actionType);
            log.info(TransactionUtils.TRANSACTION_MARKER, LogMessages.COMMIT_ON_CLOSED_TRANSACTION, transactionId, status.name(), userId, actionType);
            result = TransactionCodeEnum.TRANSACTION_CLOSED;
        }
        updateTransactionStatus(result);
        return result;
    }

    private void updateTransactionStatus(TransactionCodeEnum result) {
        switch (result) {
        case SUCCESS:
            status = TransactionStatusEnum.CLOSED;
            break;
        case ROLLBACK_SUCCESS:
            status = TransactionStatusEnum.CLOSED;
            break;
        case ROLLBACK_FAILED:
            status = TransactionStatusEnum.FAILED_ROLLBACK;
            break;
        default:
            break;
        }

    }

    private boolean isLastActionAlreadyCalled() {
        return lastActionAlreadyCalled;
    }

    private void setLastActionAlreadyCalled(boolean lastAction) {
        this.lastActionAlreadyCalled = lastAction;
    }

    // TODO test using slf4j-test and remove this
    static void setLog(Logger log) {
        TransactionSdncImpl.log = log;
    }

    TransactionStatusEnum getStatus() {
        return status;
    }
}
