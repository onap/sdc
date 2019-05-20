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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ResourceUploadStatus;
import org.openecomp.sdc.be.dao.impl.ESCatalogDAO;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.transaction.api.IDBAction;
import org.openecomp.sdc.common.transaction.api.RollbackHandler;
import org.openecomp.sdc.common.transaction.api.TransactionUtils;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.*;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class SdncTransactionTest {
    private static ESCatalogDAO esCatalogDao = Mockito.mock(ESCatalogDAO.class);
    private static JanusGraphGenericDao janusGraphGenericDao = Mockito.mock(JanusGraphGenericDao.class);
    private static final Logger log = Mockito.spy(Logger.getLogger(SdncTransactionTest.class));
    private static int transactionId = 0;
    private static ConfigurationManager configurationManager;

    public enum TestAction {
        JanusGraphAction, Rollback, GeneralAction
    }

    public enum TestResponse {
        JanusGraphResponseSuccess, GeneralSuccess
    }

    @BeforeClass
    public static void beforeClass() {
        TransactionSdncImpl.setLog(log);
        CommitManager.setLog(log);
        RollbackHandler.setLog(log);
        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        configurationManager = new ConfigurationManager(configurationSource);
    }

    @Before
    public void beforeTest() {
        reset(log);
        reset(esCatalogDao);
        reset(janusGraphGenericDao);
    }

    @Test
    public void testInvokeJanusGraphAction() {
        int transactionId = getNextTransactionId();
        TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao,
            janusGraphGenericDao);

        doBasicJanusGraphAction(transactionId, tx, false, true);
        assertSame(tx.getStatus(), TransactionStatusEnum.OPEN);
    }

    @Test
    public void testInvokeESAction() {
        int transactionId = getNextTransactionId();
        TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao,
            janusGraphGenericDao);

        doESAddArtifactAction(transactionId, tx, true, true);
        assertSame(tx.getStatus(), TransactionStatusEnum.OPEN);
    }

    @Test
    public void testfinishTransaction() {
        int transactionId = getNextTransactionId();
        TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao,
            janusGraphGenericDao);
        doFinishTransaction(transactionId, tx, true);
        assertSame(tx.getStatus(), TransactionStatusEnum.CLOSED);
    }

    @Test
    public void testFinishOnClosedTransaction() {
        int transactionId = getNextTransactionId();
        TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao,
            janusGraphGenericDao);
        doFinishTransaction(transactionId, tx, true);

        TransactionCodeEnum finishTransaction = tx.finishTransaction();
        assertSame(finishTransaction, TransactionCodeEnum.TRANSACTION_CLOSED);
        verify(log).info(TransactionUtils.TRANSACTION_MARKER, LogMessages.COMMIT_ON_CLOSED_TRANSACTION, transactionId, TransactionStatusEnum.CLOSED.name(), TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
        assertSame(tx.getStatus(), TransactionStatusEnum.CLOSED);

    }

    @Test
    public void testCallingLastActionTwice() {
        int transactionId = getNextTransactionId();
        TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao,
            janusGraphGenericDao);
        doBasicJanusGraphAction(transactionId, tx, true, true);
        Either<TestResponse, TransactionCodeEnum> doBasicJanusGraphAction = doBasicJanusGraphAction(transactionId, tx, true, false);
        assertTrue(doBasicJanusGraphAction.isRight());
        assertNotSame(tx.getStatus(), TransactionStatusEnum.OPEN);
        verify(log).info(TransactionUtils.TRANSACTION_MARKER, LogMessages.DOUBLE_FINISH_FLAG_ACTION, transactionId, DBTypeEnum.JANUSGRAPH
            .name(), TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
    }

    @Test
    public void testActionOnClosedTransaction() {
        int transactionId = getNextTransactionId();
        TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao,
            janusGraphGenericDao);
        doFinishTransaction(transactionId, tx, true);

        Either<DBActionCodeEnum, TransactionCodeEnum> eitherESResult = tx.invokeESAction(false, ESActionTypeEnum.ADD_ARTIFACT, createDummyArtifactData());
        assertTrue(eitherESResult.isRight());
        assertSame(eitherESResult.right().value(), TransactionCodeEnum.TRANSACTION_CLOSED);

        Either<Object, TransactionCodeEnum> eitherJanusGraphResult = tx.invokeJanusGraphAction(false, createBasicAction(TestAction.JanusGraphAction, TestResponse.JanusGraphResponseSuccess));
        assertTrue(eitherJanusGraphResult.isRight());
        assertSame(eitherJanusGraphResult.right().value(), TransactionCodeEnum.TRANSACTION_CLOSED);

        Either<Object, TransactionCodeEnum> eitherGeneralDBAction = tx.invokeGeneralDBAction(true, DBTypeEnum.JANUSGRAPH, createBasicAction(TestAction.JanusGraphAction, TestResponse.JanusGraphResponseSuccess),
                createBasicAction(TestAction.Rollback, TestResponse.JanusGraphResponseSuccess));
        assertTrue(eitherGeneralDBAction.isRight());
        assertSame(eitherGeneralDBAction.right().value(), TransactionCodeEnum.TRANSACTION_CLOSED);

        assertSame(tx.getStatus(), TransactionStatusEnum.CLOSED);
        verify(log, times(3)).info(TransactionUtils.TRANSACTION_MARKER, LogMessages.ACTION_ON_CLOSED_TRANSACTION, transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

    }

    @Test
    public void testBasicHappyScenario() {
        int transactionId = getNextTransactionId();
        TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao,
            janusGraphGenericDao);

        doBasicJanusGraphAction(transactionId, tx, false, true);
        assertSame(tx.getStatus(), TransactionStatusEnum.OPEN);

        doESAddArtifactAction(transactionId, tx, true, true);
        assertSame(tx.getStatus(), TransactionStatusEnum.OPEN);

        doFinishTransaction(transactionId, tx, true);

        assertSame(tx.getStatus(), TransactionStatusEnum.CLOSED);

    }

    @Test
    public void testRollbackSucceededOnAction() {
        int transactionId = getNextTransactionId();
        TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao,
            janusGraphGenericDao);
        doESAddArtifactAction(transactionId, tx, false, true);

        when(janusGraphGenericDao.rollback()).thenReturn(JanusGraphOperationStatus.OK);
        String crushMessage = "DB Crush Simulation";
        Either<TestResponse, TransactionCodeEnum> eitherTransactionResult = tx.invokeJanusGraphAction(false, createCrushingAction(TestAction.JanusGraphAction, crushMessage));

        assertTrue(eitherTransactionResult.isRight());
        assertSame(eitherTransactionResult.right().value(), TransactionCodeEnum.ROLLBACK_SUCCESS);
        assertSame(tx.getStatus(), TransactionStatusEnum.CLOSED);
        verify(log).info(TransactionUtils.TRANSACTION_MARKER, LogMessages.DB_ACTION_FAILED_WITH_EXCEPTION, DBTypeEnum.JANUSGRAPH
            .name(), transactionId, crushMessage, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

        verify(log, times(1)).debug(LogMessages.ROLLBACK_PERSISTENT_ACTION, DBTypeEnum.ELASTIC_SEARCH.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
        verify(log, times(1)).debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_PERSISTENT_ACTION, DBTypeEnum.ELASTIC_SEARCH.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

        verify(log, times(1)).debug(LogMessages.ROLLBACK_NON_PERSISTENT_ACTION, DBTypeEnum.JANUSGRAPH
            .name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
        verify(log, times(1)).debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_NON_PERSISTENT_ACTION, DBTypeEnum.JANUSGRAPH
            .name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

        verify(log).info(LogMessages.ROLLBACK_SUCCEEDED_GENERAL, transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
        verify(log).info(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_SUCCEEDED_GENERAL, transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
    }

    @Test
    public void testRollbackFailedOnAction() {
        int transactionId = getNextTransactionId();
        TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao,
            janusGraphGenericDao);

        doESAddArtifactAction(transactionId, tx, false, true);

        when(janusGraphGenericDao.rollback()).thenReturn(JanusGraphOperationStatus.NOT_CONNECTED);
        String crushMessage = "DB Crush Simulation";
        Either<TestResponse, TransactionCodeEnum> eitherTransactionResult = tx.invokeJanusGraphAction(false, createCrushingAction(TestAction.JanusGraphAction, crushMessage));

        assertTrue(eitherTransactionResult.isRight());
        assertSame(tx.getStatus(), TransactionStatusEnum.FAILED_ROLLBACK);
        assertSame(eitherTransactionResult.right().value(), TransactionCodeEnum.ROLLBACK_FAILED);
        verify(log).info(TransactionUtils.TRANSACTION_MARKER, LogMessages.DB_ACTION_FAILED_WITH_EXCEPTION, DBTypeEnum.JANUSGRAPH
            .name(), transactionId, crushMessage, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

        verify(log, times(1)).debug(LogMessages.ROLLBACK_PERSISTENT_ACTION, DBTypeEnum.ELASTIC_SEARCH.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
        verify(log, times(1)).debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_PERSISTENT_ACTION, DBTypeEnum.ELASTIC_SEARCH.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

        verify(log, times(1)).debug(LogMessages.ROLLBACK_NON_PERSISTENT_ACTION, DBTypeEnum.JANUSGRAPH
            .name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
        verify(log, times(1)).debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_NON_PERSISTENT_ACTION, DBTypeEnum.JANUSGRAPH
            .name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
    }

    @Test
    public void testRollbackSucceededOnCommit() {
        int transactionId = getNextTransactionId();
        TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao,
            janusGraphGenericDao);
        doESAddArtifactAction(transactionId, tx, false, true);
        doBasicJanusGraphAction(transactionId, tx, true, true);

        when(janusGraphGenericDao.commit()).thenReturn(JanusGraphOperationStatus.GENERAL_ERROR);
        when(janusGraphGenericDao.rollback()).thenReturn(JanusGraphOperationStatus.OK);
        // finishTransaction
        TransactionCodeEnum transactionCode = tx.finishTransaction();
        assertSame(transactionCode, TransactionCodeEnum.ROLLBACK_SUCCESS);
        assertSame(tx.getStatus(), TransactionStatusEnum.CLOSED);

        verify(log, times(1)).debug(LogMessages.ROLLBACK_PERSISTENT_ACTION, DBTypeEnum.ELASTIC_SEARCH.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
        verify(log, times(1)).debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_PERSISTENT_ACTION, DBTypeEnum.ELASTIC_SEARCH.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

        verify(log, times(1)).debug(LogMessages.ROLLBACK_NON_PERSISTENT_ACTION, DBTypeEnum.JANUSGRAPH
            .name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
        verify(log, times(1)).debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_NON_PERSISTENT_ACTION, DBTypeEnum.JANUSGRAPH
            .name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

        verify(log).info(LogMessages.ROLLBACK_SUCCEEDED_GENERAL, transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
        verify(log).info(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_SUCCEEDED_GENERAL, transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
    }

    @Test
    public void testRollbackFailedOnCommit() {
        int transactionId = getNextTransactionId();
        TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao,
            janusGraphGenericDao);
        doESAddArtifactAction(transactionId, tx, false, true);
        doBasicJanusGraphAction(transactionId, tx, true, true);

        when(janusGraphGenericDao.commit()).thenReturn(JanusGraphOperationStatus.GENERAL_ERROR);
        when(janusGraphGenericDao.rollback()).thenReturn(JanusGraphOperationStatus.OK);
        String esError = "No Connection to Es";
        Mockito.doThrow(new RuntimeException(esError)).when(esCatalogDao).deleteArtifact(Mockito.anyString());
        // finishTransaction
        TransactionCodeEnum transactionCode = tx.finishTransaction();
        assertSame(transactionCode, TransactionCodeEnum.ROLLBACK_FAILED);
        assertSame(tx.getStatus(), TransactionStatusEnum.FAILED_ROLLBACK);

        verify(log, times(1)).debug(LogMessages.ROLLBACK_PERSISTENT_ACTION, DBTypeEnum.ELASTIC_SEARCH.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
        verify(log, times(1)).debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_PERSISTENT_ACTION, DBTypeEnum.ELASTIC_SEARCH.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

        verify(log, times(1)).debug(LogMessages.ROLLBACK_NON_PERSISTENT_ACTION, DBTypeEnum.JANUSGRAPH
            .name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
        verify(log, times(1)).debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_NON_PERSISTENT_ACTION, DBTypeEnum.JANUSGRAPH
            .name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
    }

    @Test
    public void testInvokeGeneralAction() {
        when(janusGraphGenericDao.rollback()).thenReturn(JanusGraphOperationStatus.OK);
        int transactionId = getNextTransactionId();
        TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao,
            janusGraphGenericDao);
        IDBAction generalAction = createBasicAction(TestAction.GeneralAction, TestResponse.GeneralSuccess);
        IDBAction rollbackAction = createBasicAction(TestAction.Rollback, TestResponse.GeneralSuccess);
        String crushMessage = "No DB Connection";
        IDBAction crushingAction = createCrushingAction(TestAction.GeneralAction, crushMessage);

        Either<TestResponse, TransactionCodeEnum> eitherResult = tx.invokeGeneralDBAction(false, DBTypeEnum.MYSTERY, generalAction, rollbackAction);
        assertTrue(eitherResult.isLeft());
        assertSame(eitherResult.left().value(), TestResponse.GeneralSuccess);
        assertSame(tx.getStatus(), TransactionStatusEnum.OPEN);
        eitherResult = tx.invokeGeneralDBAction(false, DBTypeEnum.MYSTERY, crushingAction, rollbackAction);

        assertTrue(eitherResult.isRight());
        assertSame(eitherResult.right().value(), TransactionCodeEnum.ROLLBACK_SUCCESS);
        assertSame(tx.getStatus(), TransactionStatusEnum.CLOSED);

        verify(log).info(TransactionUtils.TRANSACTION_MARKER, LogMessages.DB_ACTION_FAILED_WITH_EXCEPTION, DBTypeEnum.MYSTERY.name(), transactionId, crushMessage, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

        verify(log, times(2)).debug(LogMessages.ROLLBACK_PERSISTENT_ACTION, DBTypeEnum.MYSTERY.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
        verify(log, times(2)).debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_PERSISTENT_ACTION, DBTypeEnum.MYSTERY.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

        verify(log).info(LogMessages.ROLLBACK_SUCCEEDED_GENERAL, transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
        verify(log).info(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_SUCCEEDED_GENERAL, transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

    }

    private Either<TestResponse, TransactionCodeEnum> doBasicJanusGraphAction(int transactionId, TransactionSdncImpl tx, boolean isLastAction, boolean isVerifyAction) {
        // Add JanusGraph Action
        Either<TestResponse, TransactionCodeEnum> eitherJanusGraphResult = tx.invokeJanusGraphAction(isLastAction, createBasicAction(TestAction.JanusGraphAction, TestResponse.JanusGraphResponseSuccess));
        if (isVerifyAction) {
            // Check JanusGraph Action
            assertTrue(eitherJanusGraphResult.isLeft());
            assertSame(eitherJanusGraphResult.left().value(), TestResponse.JanusGraphResponseSuccess);
            verify(log).debug(TestAction.JanusGraphAction.name());
            verify(log).debug(LogMessages.INVOKE_ACTION, transactionId, DBTypeEnum.JANUSGRAPH.name(), TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
            verifyNoErrorsInLog();
            verifyNoInfoInLog();
        }
        return eitherJanusGraphResult;
    }

    private TransactionCodeEnum doFinishTransaction(int transactionId, TransactionSdncImpl tx, boolean isVerifyAction) {
        // Prerequisite finishTransaction
        when(janusGraphGenericDao.commit()).thenReturn(JanusGraphOperationStatus.OK);
        // finishTransaction
        TransactionCodeEnum transactionCode = tx.finishTransaction();
        if (isVerifyAction) {
            // Check finishTransaction
            verify(log).debug(LogMessages.COMMIT_ACTION_ALL_DB, transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
            verify(log).debug(LogMessages.COMMIT_ACTION_SPECIFIC_DB, transactionId, DBTypeEnum.JANUSGRAPH
                .name(), TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
            assertSame(transactionCode, TransactionCodeEnum.SUCCESS);
        }
        return transactionCode;
    }

    private void doESAddArtifactAction(int transactionId, TransactionSdncImpl tx, boolean isLastAction, boolean isVerifyAction) {
        // Prerequisite ES Action
        Either<ESArtifactData, ResourceUploadStatus> eitherBeforeAddArtifact = Either.right(ResourceUploadStatus.NOT_EXIST);
        when(esCatalogDao.getArtifact(Mockito.anyString())).thenReturn(eitherBeforeAddArtifact);

        // Add ES Action
        Either<DBActionCodeEnum, TransactionCodeEnum> eitherEsAction = tx.invokeESAction(isLastAction, ESActionTypeEnum.ADD_ARTIFACT, createDummyArtifactData());

        if (isVerifyAction) {
            // Check JanusGraph Action
            assertTrue(eitherEsAction.isLeft());
            assertSame(eitherEsAction.left().value(), DBActionCodeEnum.SUCCESS);
            verify(log).debug(LogMessages.INVOKE_ACTION, transactionId, DBTypeEnum.ELASTIC_SEARCH.name(), TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
            verifyNoErrorsInLog();
            verifyNoInfoInLog();
        }
    }

    private ESArtifactData createDummyArtifactData() {
        String strData = "qweqwqweqw34e4wrwer";
        return new ESArtifactData("artifactNewMarina11", strData.getBytes());
    }

    private void verifyNoErrorsInLog() {
        verify(log, Mockito.times(0)).error(Mockito.anyString(), Mockito.any(Object[].class));
        verify(log, Mockito.times(0)).error(Mockito.anyString());
    }

    private void verifyNoInfoInLog() {
        verify(log, Mockito.times(0)).info(Mockito.anyString(), Mockito.any(Object[].class));
        verify(log, Mockito.times(0)).info(Mockito.anyString());
    }

    private IDBAction createBasicAction(TestAction action, TestResponse resp) {
        final TestAction finalAction = action;
        final TestResponse finalResp = resp;
        return new IDBAction() {
            @Override
            public TestResponse doAction() {
                log.debug(finalAction.name());
                return finalResp;
            }
        };
    }

    private IDBAction createCrushingAction(TestAction action, final String crushMessage) {
        final TestAction finalAction = action;
        return new IDBAction() {
            @Override
            public TestResponse doAction() {
                log.debug(finalAction.name());
                throw new RuntimeException(crushMessage);
            }
        };
    }

    public int getNextTransactionId() {
        transactionId++;
        return transactionId;
    }
}
