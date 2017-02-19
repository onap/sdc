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

package org.openecomp.sdc.common.transaction.api;

import java.util.Stack;

import org.openecomp.sdc.common.transaction.api.TransactionUtils.DBActionCodeEnum;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.LogMessages;
import org.openecomp.sdc.common.util.MethodActivationStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RollbackHandler implements IDBType {
	private static Logger log = LoggerFactory.getLogger(RollbackHandler.class.getName());
	private Stack<IDBAction> dbActionRollbacks;

	private Integer transactionId;
	private String userId, actionType;

	protected RollbackHandler(Integer transactionId, String userId, String actionType) {
		if (isRollbackForPersistenceData()) {
			dbActionRollbacks = new Stack<>();
		}
		this.transactionId = transactionId;
		this.userId = userId;
		this.actionType = actionType;
	}

	public MethodActivationStatusEnum addRollbackAction(IDBAction rollbackAction) {
		MethodActivationStatusEnum result = MethodActivationStatusEnum.SUCCESS;
		if (isRollbackForPersistenceData()) {
			dbActionRollbacks.push(rollbackAction);
		} else {
			result = MethodActivationStatusEnum.NOT_ALLOWED;
		}
		return result;
	}

	public DBActionCodeEnum doRollback() {
		DBActionCodeEnum result;

		try {
			if (isRollbackForPersistenceData()) {
				result = doPersistenceDataRollback();
			} else {
				log.debug(LogMessages.ROLLBACK_NON_PERSISTENT_ACTION, getDBType().name(), transactionId, userId, actionType);
				log.debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_NON_PERSISTENT_ACTION, getDBType().name(), transactionId, userId, actionType);
				result = doNonPersistenceDataRollback();
			}
			if (result != DBActionCodeEnum.SUCCESS) {
				log.error(LogMessages.ROLLBACK_FAILED_ON_DB, transactionId, getDBType().name(), userId, actionType);
				log.error(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_FAILED_ON_DB, transactionId, getDBType().name(), userId, actionType);
			}

		} catch (Exception e) {
			result = DBActionCodeEnum.FAIL_GENERAL;
			log.error(LogMessages.ROLLBACK_FAILED_ON_DB_WITH_EXCEPTION, transactionId, getDBType().name(), e.getMessage(), userId, actionType);
			log.debug(LogMessages.ROLLBACK_FAILED_ON_DB_WITH_EXCEPTION, transactionId, getDBType().name(), e.getMessage(), userId, actionType, e);
			log.error(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_FAILED_ON_DB_WITH_EXCEPTION, transactionId, getDBType().name(), e.getMessage(), userId, actionType);
		}

		return result;
	}

	private <T> DBActionCodeEnum doPersistenceDataRollback() {
		DBActionCodeEnum result = DBActionCodeEnum.SUCCESS;
		while (!dbActionRollbacks.empty()) {
			log.debug(LogMessages.ROLLBACK_PERSISTENT_ACTION, getDBType().name(), transactionId, userId, actionType);
			log.debug(TransactionUtils.TRANSACTION_MARKER, LogMessages.ROLLBACK_PERSISTENT_ACTION, getDBType().name(), transactionId, userId, actionType);
			IDBAction rollbackAction = dbActionRollbacks.pop();
			T rollbackResult = rollbackAction.doAction();
			if (!isRollbackResultValid(rollbackResult)) {
				result = DBActionCodeEnum.FAIL_GENERAL;
			}
		}
		return result;
	}

	/**
	 * Override for specific logic
	 * 
	 * @param <T>
	 */
	public <T> boolean isRollbackResultValid(T rollbackResult) {
		return true;
	}

	/**
	 * Override for specific logic
	 */
	public DBActionCodeEnum doNonPersistenceDataRollback() {
		return DBActionCodeEnum.SUCCESS;
	}

	protected abstract boolean isRollbackForPersistenceData();

	/**
	 * Only Used for Unit Tests !
	 */
	public static void setLog(Logger log) {
		RollbackHandler.log = log;
	}
}
