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

import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.transaction.api.ICommitHandler;
import org.openecomp.sdc.common.transaction.api.TransactionUtils;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.DBActionCodeEnum;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.LogMessages;

import java.util.List;

public class CommitManager {

    // TODO test using slf4j-test and make this final
    private static Logger log = Logger.getLogger(CommitManager.class);
    private List<ICommitHandler> commitHandlers;
    private Integer transactionId;
    private String userId, actionType;

    CommitManager(Integer transactionId, String userId, String actionType, List<ICommitHandler> commitHandlers) {
        this.commitHandlers = commitHandlers;
        this.transactionId = transactionId;
        this.userId = userId;
        this.actionType = actionType;

    }

    public DBActionCodeEnum transactionCommit() {
        log.debug(LogMessages.COMMIT_ACTION_ALL_DB, transactionId, userId, actionType);
        DBActionCodeEnum commitResult = DBActionCodeEnum.SUCCESS;
        ICommitHandler lastHandler = null;
        try {
            for (ICommitHandler handler : commitHandlers) {
                lastHandler = handler;
                log.debug(LogMessages.COMMIT_ACTION_SPECIFIC_DB, transactionId, handler.getDBType().name(), userId, actionType);
                DBActionCodeEnum commitCode = handler.doCommit();
                if (commitCode == DBActionCodeEnum.FAIL_GENERAL) {
                    BeEcompErrorManager.getInstance().logBeSystemError("transactionCommit on DB " + handler.getDBType().name());
                    log.debug("Commit failed for SdncTransactionID:{} on DB:{}", transactionId, handler.getDBType().name());
                    commitResult = DBActionCodeEnum.FAIL_GENERAL;
                    break;
                }
                log.debug("Commit succeeded for SdncTransactionID:{} on DB:{}", transactionId, handler.getDBType().name());
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeSystemError("transactionCommit - on DB " + getDBName(lastHandler));
            log.debug("Commit failed for SdncTransactionID:{} on DB:{}, Exception message:{}", transactionId, getDBName(lastHandler), e.getMessage(), e);
            log.info(TransactionUtils.TRANSACTION_MARKER, "Commit failed for SdncTransactionID:{} on DB:{}", transactionId, getDBName(lastHandler));
            commitResult = DBActionCodeEnum.FAIL_GENERAL;
        }
        return commitResult;
    }

    private String getDBName(ICommitHandler lastHandler) {
        String dbName = "Unknown";
        if (lastHandler != null) {
            dbName = lastHandler.getDBType().name();
        }
        return dbName;
    }

    // TODO test using slf4j-test and remove this
    static void setLog(Logger log) {
        CommitManager.log = log;
    }
}
