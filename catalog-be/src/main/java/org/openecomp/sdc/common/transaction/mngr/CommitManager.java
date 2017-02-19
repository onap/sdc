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

import java.util.List;

import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.transaction.api.ICommitHandler;
import org.openecomp.sdc.common.transaction.api.TransactionUtils;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.DBActionCodeEnum;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.LogMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommitManager {
	private List<ICommitHandler> commitHandlers;
	private Integer transactionId;
	private String userId, actionType;
	private static Logger log = LoggerFactory.getLogger(CommitManager.class.getName());

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
					BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, "transactionCommit on DB " + handler.getDBType().name());
					BeEcompErrorManager.getInstance().logBeSystemError("transactionCommit on DB " + handler.getDBType().name());
					log.debug("Commit failed for SdncTransactionID:{} on DB:{}", transactionId, handler.getDBType().name());
					commitResult = DBActionCodeEnum.FAIL_GENERAL;
					break;
				}
				log.debug("Commit succeeded for SdncTransactionID:{} on DB:{}", transactionId, handler.getDBType().name());
			}
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, "transactionCommit - on DB " + getDBName(lastHandler));
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

	static void setLog(Logger log) {
		CommitManager.log = log;
	}
}
