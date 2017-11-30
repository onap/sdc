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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ResourceUploadStatus;
import org.openecomp.sdc.be.dao.impl.ESCatalogDAO;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.transaction.api.IDBAction;
import org.openecomp.sdc.common.transaction.api.RollbackHandler;
import org.openecomp.sdc.common.transaction.api.TransactionUtils;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.ActionTypeEnum;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.DBActionCodeEnum;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.DBTypeEnum;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.ESActionTypeEnum;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.LogMessages;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.TransactionCodeEnum;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.TransactionStatusEnum;
import org.slf4j.Logger;

import fj.data.Either;

public class SdncTransactionTest {
	private static ESCatalogDAO esCatalogDao = Mockito.mock(ESCatalogDAO.class);
	private static TitanGenericDao titanGenericDao = Mockito.mock(TitanGenericDao.class);
	private static Logger log = Mockito.spy(Logger.class);
	private static int transactionId = 0;
	private static ConfigurationManager configurationManager;

	public enum TestAction {
		TitanAction, Rollback, GeneralAction
	}

	public enum TestResponse {
		TitanResponseSuccess, GeneralSuccess
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
		reset(titanGenericDao);
	}

	@Test
	public void testInvokeTitanAction() {
		int transactionId = getNextTransactionId();
		TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao, titanGenericDao);

		doBasicTitanAction(transactionId, tx, false, true);
		assertTrue(tx.getStatus() == TransactionStatusEnum.OPEN);
	}

	@Test
	public void testInvokeESAction() {
		int transactionId = getNextTransactionId();
		TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao, titanGenericDao);

		doESAddArtifactAction(transactionId, tx, true, true);
		assertTrue(tx.getStatus() == TransactionStatusEnum.OPEN);
	}

	@Test
	public void testfinishTransaction() {
		int transactionId = getNextTransactionId();
		TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao, titanGenericDao);
		doFinishTransaction(transactionId, tx, true);
		assertTrue(tx.getStatus() == TransactionStatusEnum.CLOSED);
	}

	@Test
	public void testFinishOnClosedTransaction() {
		int transactionId = getNextTransactionId();
		TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao, titanGenericDao);
		doFinishTransaction(transactionId, tx, true);

		TransactionCodeEnum finishTransaction = tx.finishTransaction();
		assertTrue(finishTransaction == TransactionCodeEnum.TRANSACTION_CLOSED);
		// verify(log).error(LogMessages.COMMIT_ON_CLOSED_TRANSACTION,
		// transactionId, TransactionStatusEnum.CLOSED.name(),
		// TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
		verify(log).info(TransactionUtils.TRANSACTION_MARKER, LogMessages.COMMIT_ON_CLOSED_TRANSACTION, transactionId, TransactionStatusEnum.CLOSED.name(), TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
		assertTrue(tx.getStatus() == TransactionStatusEnum.CLOSED);

	}

	@Test
	public void testCallingLastActionTwice() {
		int transactionId = getNextTransactionId();
		TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao, titanGenericDao);
		doBasicTitanAction(transactionId, tx, true, true);
		Either<TestResponse, TransactionCodeEnum> doBasicTitanAction = doBasicTitanAction(transactionId, tx, true, false);
		assertTrue(doBasicTitanAction.isRight());
		assertTrue(tx.getStatus() != TransactionStatusEnum.OPEN);
		verify(log).info(TransactionUtils.TRANSACTION_MARKER, LogMessages.DOUBLE_FINISH_FLAG_ACTION, transactionId, DBTypeEnum.TITAN.name(), TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
		// verify(log).error( LogMessages.DOUBLE_FINISH_FLAG_ACTION,
		// transactionId, DBTypeEnum.TITAN.name(), TransactionUtils.DUMMY_USER,
		// ActionTypeEnum.ADD_ARTIFACT.name());
	}

	@Test
	public void testActionOnClosedTransaction() {
		int transactionId = getNextTransactionId();
		TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao, titanGenericDao);
		doFinishTransaction(transactionId, tx, true);

		Either<DBActionCodeEnum, TransactionCodeEnum> eitherESResult = tx.invokeESAction(false, ESActionTypeEnum.ADD_ARTIFACT, createDummyArtifactData());
		assertTrue(eitherESResult.isRight());
		assertTrue(eitherESResult.right().value() == TransactionCodeEnum.TRANSACTION_CLOSED);

		Either<Object, TransactionCodeEnum> eitherTitanResult = tx.invokeTitanAction(false, createBasicAction(TestAction.TitanAction, TestResponse.TitanResponseSuccess));
		assertTrue(eitherTitanResult.isRight());
		assertTrue(eitherTitanResult.right().value() == TransactionCodeEnum.TRANSACTION_CLOSED);

		Either<Object, TransactionCodeEnum> eitherGeneralDBAction = tx.invokeGeneralDBAction(true, DBTypeEnum.TITAN, createBasicAction(TestAction.TitanAction, TestResponse.TitanResponseSuccess),
				createBasicAction(TestAction.Rollback, TestResponse.TitanResponseSuccess));
		assertTrue(eitherGeneralDBAction.isRight());
		assertTrue(eitherGeneralDBAction.right().value() == TransactionCodeEnum.TRANSACTION_CLOSED);

		assertTrue(tx.getStatus() == TransactionStatusEnum.CLOSED);
		// verify(log, times(3)).error(LogMessages.ACTION_ON_CLOSED_TRANSACTION,
		// transactionId, TransactionUtils.DUMMY_USER,
		// ActionTypeEnum.ADD_ARTIFACT.name());
		verify(log, times(3)).info(TransactionUtils.TRANSACTION_MARKER, LogMessages.ACTION_ON_CLOSED_TRANSACTION, transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

	}

	@Test
	public void testBasicHappyScenario() {
		int transactionId = getNextTransactionId();
		TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao, titanGenericDao);

		doBasicTitanAction(transactionId, tx, false, true);
		assertTrue(tx.getStatus() == TransactionStatusEnum.OPEN);

		doESAddArtifactAction(transactionId, tx, true, true);
		assertTrue(tx.getStatus() == TransactionStatusEnum.OPEN);

		doFinishTransaction(transactionId, tx, true);

		assertTrue(tx.getStatus() == TransactionStatusEnum.CLOSED);

	}

	@Test
	public void testRollbackSucceededOnAction() {
		int transactionId = getNextTransactionId();
		TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao, titanGenericDao);
		doESAddArtifactAction(transactionId, tx, false, true);

		when(titanGenericDao.rollback()).thenReturn(TitanOperationStatus.OK);
		String crushMessage = "DB Crush Simulation";
		Either<TestResponse, TransactionCodeEnum> eitherTransactionResult = tx.invokeTitanAction(false, createCrushingAction(TestAction.TitanAction, crushMessage));

		assertTrue(eitherTransactionResult.isRight());
		assertTrue(eitherTransactionResult.right().value() == TransactionCodeEnum.ROLLBACK_SUCCESS);
		assertTrue(tx.getStatus() == TransactionStatusEnum.CLOSED);
		// verify(log).error(LogMessages.DB_ACTION_FAILED_WITH_EXCEPTION,
		// DBTypeEnum.TITAN.name(), transactionId, crushMessage,
		// TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
		verify(log).info(TransactionUtils.TRANSACTION_MARKER, LogMessages.DB_ACTION_FAILED_WITH_EXCEPTION, DBTypeEnum.TITAN.name(), transactionId, crushMessage, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

		verify(log, times(1)).debug(LogMessages.ROLLBACK_PERSISTENT_ACTION, DBTypeEnum.ELASTIC_SEARCH.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
		verify(log, times(1)).debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_PERSISTENT_ACTION, DBTypeEnum.ELASTIC_SEARCH.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

		verify(log, times(1)).debug(LogMessages.ROLLBACK_NON_PERSISTENT_ACTION, DBTypeEnum.TITAN.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
		verify(log, times(1)).debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_NON_PERSISTENT_ACTION, DBTypeEnum.TITAN.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

		verify(log).info(LogMessages.ROLLBACK_SUCCEEDED_GENERAL, transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
		verify(log).info(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_SUCCEEDED_GENERAL, transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
	}

	@Test
	public void testRollbackFailedOnAction() {
		int transactionId = getNextTransactionId();
		TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao, titanGenericDao);

		doESAddArtifactAction(transactionId, tx, false, true);

		when(titanGenericDao.rollback()).thenReturn(TitanOperationStatus.NOT_CONNECTED);
		String crushMessage = "DB Crush Simulation";
		Either<TestResponse, TransactionCodeEnum> eitherTransactionResult = tx.invokeTitanAction(false, createCrushingAction(TestAction.TitanAction, crushMessage));

		assertTrue(eitherTransactionResult.isRight());
		assertTrue(tx.getStatus() == TransactionStatusEnum.FAILED_ROLLBACK);
		assertTrue(eitherTransactionResult.right().value() == TransactionCodeEnum.ROLLBACK_FAILED);
		// verify(log).error(LogMessages.DB_ACTION_FAILED_WITH_EXCEPTION,
		// DBTypeEnum.TITAN.name(), transactionId, crushMessage,
		// TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
		verify(log).info(TransactionUtils.TRANSACTION_MARKER, LogMessages.DB_ACTION_FAILED_WITH_EXCEPTION, DBTypeEnum.TITAN.name(), transactionId, crushMessage, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

		verify(log, times(1)).debug(LogMessages.ROLLBACK_PERSISTENT_ACTION, DBTypeEnum.ELASTIC_SEARCH.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
		verify(log, times(1)).debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_PERSISTENT_ACTION, DBTypeEnum.ELASTIC_SEARCH.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

		verify(log, times(1)).debug(LogMessages.ROLLBACK_NON_PERSISTENT_ACTION, DBTypeEnum.TITAN.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
		verify(log, times(1)).debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_NON_PERSISTENT_ACTION, DBTypeEnum.TITAN.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

		// verify(log, times(1)).error(LogMessages.ROLLBACK_FAILED_GENERAL,
		// transactionId, TransactionUtils.DUMMY_USER,
		// ActionTypeEnum.ADD_ARTIFACT.name());
		// verify(log, times(1)).error(TransactionUtils.TRANSACTION_MARKER,
		// LogMessages.ROLLBACK_FAILED_GENERAL, transactionId,
		// TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
	}

	@Test
	public void testRollbackSucceededOnCommit() {
		int transactionId = getNextTransactionId();
		TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao, titanGenericDao);
		doESAddArtifactAction(transactionId, tx, false, true);
		doBasicTitanAction(transactionId, tx, true, true);

		when(titanGenericDao.commit()).thenReturn(TitanOperationStatus.GENERAL_ERROR);
		when(titanGenericDao.rollback()).thenReturn(TitanOperationStatus.OK);
		// finishTransaction
		TransactionCodeEnum transactionCode = tx.finishTransaction();
		assertTrue(transactionCode == TransactionCodeEnum.ROLLBACK_SUCCESS);
		assertTrue(tx.getStatus() == TransactionStatusEnum.CLOSED);

		verify(log, times(1)).debug(LogMessages.ROLLBACK_PERSISTENT_ACTION, DBTypeEnum.ELASTIC_SEARCH.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
		verify(log, times(1)).debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_PERSISTENT_ACTION, DBTypeEnum.ELASTIC_SEARCH.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

		verify(log, times(1)).debug(LogMessages.ROLLBACK_NON_PERSISTENT_ACTION, DBTypeEnum.TITAN.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
		verify(log, times(1)).debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_NON_PERSISTENT_ACTION, DBTypeEnum.TITAN.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

		verify(log).info(LogMessages.ROLLBACK_SUCCEEDED_GENERAL, transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
		verify(log).info(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_SUCCEEDED_GENERAL, transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
	}

	@Test
	public void testRollbackFailedOnCommit() {
		int transactionId = getNextTransactionId();
		TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao, titanGenericDao);
		doESAddArtifactAction(transactionId, tx, false, true);
		doBasicTitanAction(transactionId, tx, true, true);

		when(titanGenericDao.commit()).thenReturn(TitanOperationStatus.GENERAL_ERROR);
		when(titanGenericDao.rollback()).thenReturn(TitanOperationStatus.OK);
		String esError = "No Connection to Es";
		Mockito.doThrow(new RuntimeException(esError)).when(esCatalogDao).deleteArtifact(Mockito.anyString());
		// finishTransaction
		TransactionCodeEnum transactionCode = tx.finishTransaction();
		assertTrue(transactionCode == TransactionCodeEnum.ROLLBACK_FAILED);
		assertTrue(tx.getStatus() == TransactionStatusEnum.FAILED_ROLLBACK);

		verify(log, times(1)).debug(LogMessages.ROLLBACK_PERSISTENT_ACTION, DBTypeEnum.ELASTIC_SEARCH.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
		verify(log, times(1)).debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_PERSISTENT_ACTION, DBTypeEnum.ELASTIC_SEARCH.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

		verify(log, times(1)).debug(LogMessages.ROLLBACK_NON_PERSISTENT_ACTION, DBTypeEnum.TITAN.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
		verify(log, times(1)).debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_NON_PERSISTENT_ACTION, DBTypeEnum.TITAN.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

		// verify(log).error(LogMessages.ROLLBACK_FAILED_ON_DB_WITH_EXCEPTION,
		// transactionId, DBTypeEnum.ELASTIC_SEARCH.name(), esError,
		// TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
		// verify(log).error(TransactionUtils.TRANSACTION_MARKER,
		// LogMessages.ROLLBACK_FAILED_ON_DB_WITH_EXCEPTION, transactionId,
		// DBTypeEnum.ELASTIC_SEARCH.name(), esError,
		// TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

		// verify(log, times(1)).error(LogMessages.ROLLBACK_FAILED_GENERAL,
		// transactionId, TransactionUtils.DUMMY_USER,
		// ActionTypeEnum.ADD_ARTIFACT.name());
		// verify(log, times(1)).error(TransactionUtils.TRANSACTION_MARKER,
		// LogMessages.ROLLBACK_FAILED_GENERAL, transactionId,
		// TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
	}

	@Test
	public void testInvokeGeneralAction() {
		when(titanGenericDao.rollback()).thenReturn(TitanOperationStatus.OK);
		int transactionId = getNextTransactionId();
		TransactionSdncImpl tx = new TransactionSdncImpl(transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT, esCatalogDao, titanGenericDao);
		IDBAction generalAction = createBasicAction(TestAction.GeneralAction, TestResponse.GeneralSuccess);
		IDBAction rollbackAction = createBasicAction(TestAction.Rollback, TestResponse.GeneralSuccess);
		String crushMessage = "No DB Connection";
		IDBAction crushingAction = createCrushingAction(TestAction.GeneralAction, crushMessage);

		Either<TestResponse, TransactionCodeEnum> eitherResult = tx.invokeGeneralDBAction(false, DBTypeEnum.MYSTERY, generalAction, rollbackAction);
		assertTrue(eitherResult.isLeft());
		assertTrue(eitherResult.left().value() == TestResponse.GeneralSuccess);
		assertTrue(tx.getStatus() == TransactionStatusEnum.OPEN);
		eitherResult = tx.invokeGeneralDBAction(false, DBTypeEnum.MYSTERY, crushingAction, rollbackAction);

		assertTrue(eitherResult.isRight());
		assertTrue(eitherResult.right().value() == TransactionCodeEnum.ROLLBACK_SUCCESS);
		assertTrue(tx.getStatus() == TransactionStatusEnum.CLOSED);

		// verify(log).error(LogMessages.DB_ACTION_FAILED_WITH_EXCEPTION,
		// DBTypeEnum.MYSTERY.name(), transactionId, crushMessage,
		// TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
		verify(log).info(TransactionUtils.TRANSACTION_MARKER, LogMessages.DB_ACTION_FAILED_WITH_EXCEPTION, DBTypeEnum.MYSTERY.name(), transactionId, crushMessage, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

		verify(log, times(2)).debug(LogMessages.ROLLBACK_PERSISTENT_ACTION, DBTypeEnum.MYSTERY.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
		verify(log, times(2)).debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_PERSISTENT_ACTION, DBTypeEnum.MYSTERY.name(), transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

		verify(log).info(LogMessages.ROLLBACK_SUCCEEDED_GENERAL, transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
		verify(log).info(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_SUCCEEDED_GENERAL, transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());

	}

	private Either<TestResponse, TransactionCodeEnum> doBasicTitanAction(int transactionId, TransactionSdncImpl tx, boolean isLastAction, boolean isVerifyAction) {
		// Add Titan Action
		Either<TestResponse, TransactionCodeEnum> eitherTitanResult = tx.invokeTitanAction(isLastAction, createBasicAction(TestAction.TitanAction, TestResponse.TitanResponseSuccess));
		if (isVerifyAction) {
			// Check Titan Action
			assertTrue(eitherTitanResult.isLeft());
			assertTrue(eitherTitanResult.left().value() == TestResponse.TitanResponseSuccess);
			verify(log).debug(TestAction.TitanAction.name());
			verify(log).debug(LogMessages.INVOKE_ACTION, transactionId, DBTypeEnum.TITAN.name(), TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
			verifyNoErrorsInLog();
			verifyNoInfoInLog();
		}
		return eitherTitanResult;
	}

	private TransactionCodeEnum doFinishTransaction(int transactionId, TransactionSdncImpl tx, boolean isVerifyAction) {
		// Prerequisite finishTransaction
		when(titanGenericDao.commit()).thenReturn(TitanOperationStatus.OK);
		// finishTransaction
		TransactionCodeEnum transactionCode = tx.finishTransaction();
		if (isVerifyAction) {
			// Check finishTransaction
			verify(log).debug(LogMessages.COMMIT_ACTION_ALL_DB, transactionId, TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
			verify(log).debug(LogMessages.COMMIT_ACTION_SPECIFIC_DB, transactionId, DBTypeEnum.TITAN.name(), TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
			assertTrue(transactionCode == TransactionCodeEnum.SUCCESS);
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
			// Check Titan Action
			assertTrue(eitherEsAction.isLeft());
			assertTrue(eitherEsAction.left().value() == DBActionCodeEnum.SUCCESS);
			verify(log).debug(LogMessages.INVOKE_ACTION, transactionId, DBTypeEnum.ELASTIC_SEARCH.name(), TransactionUtils.DUMMY_USER, ActionTypeEnum.ADD_ARTIFACT.name());
			verifyNoErrorsInLog();
			verifyNoInfoInLog();
		}
	}

	private ESArtifactData createDummyArtifactData() {
		String strData = "qweqwqweqw34e4wrwer";
		String myNodeType = "MyNewNodeType";
		ESArtifactData arData = new ESArtifactData("artifactNewMarina11", strData.getBytes());
		return arData;
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
