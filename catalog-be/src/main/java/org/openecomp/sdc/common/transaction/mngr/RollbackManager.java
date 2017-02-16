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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.common.transaction.api.RollbackHandler;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.DBActionCodeEnum;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.DBTypeEnum;
import org.openecomp.sdc.common.util.MethodActivationStatusEnum;

import fj.data.Either;

public class RollbackManager {
	private Map<DBTypeEnum, RollbackHandler> rollBackHandlersMap;
	private Integer transactionId;
	private String userId, actionType;

	RollbackManager(Integer transactionId, String userId, String actionType, List<RollbackHandler> roleBackHandlers) {
		this.transactionId = transactionId;
		this.userId = userId;
		this.actionType = actionType;
		rollBackHandlersMap = new HashMap<>();
		for (RollbackHandler handler : roleBackHandlers) {
			rollBackHandlersMap.put(handler.getDBType(), handler);
		}

	}

	public DBActionCodeEnum transactionRollback() {
		DBActionCodeEnum rollbackResult = DBActionCodeEnum.SUCCESS;
		Iterator<RollbackHandler> handlersItr = rollBackHandlersMap.values().iterator();
		while (handlersItr.hasNext()) {
			RollbackHandler handler = handlersItr.next();
			DBActionCodeEnum rollbackCode = handler.doRollback();
			if (rollbackCode == DBActionCodeEnum.FAIL_GENERAL) {
				rollbackResult = DBActionCodeEnum.FAIL_GENERAL;
			}
		}

		return rollbackResult;
	}

	protected Either<RollbackHandler, MethodActivationStatusEnum> addRollbackHandler(RollbackHandler rollbackHandler) {
		Either<RollbackHandler, MethodActivationStatusEnum> result;
		if (rollBackHandlersMap.containsKey(rollbackHandler.getDBType())) {
			result = Either.right(MethodActivationStatusEnum.NOT_ALLOWED);
		} else {
			rollBackHandlersMap.put(rollbackHandler.getDBType(), rollbackHandler);
			result = Either.left(rollbackHandler);
		}
		return result;

	}

	protected Either<RollbackHandler, MethodActivationStatusEnum> createRollbackHandler(DBTypeEnum dbType) {

		final DBTypeEnum dbTypeFinal = dbType;
		RollbackHandler rollbackHandler = new RollbackHandler(transactionId, userId, actionType) {

			@Override
			public DBTypeEnum getDBType() {
				return dbTypeFinal;
			}

			@Override
			protected boolean isRollbackForPersistenceData() {
				return true;
			}
		};
		Either<RollbackHandler, MethodActivationStatusEnum> result = addRollbackHandler(rollbackHandler);

		return result;
	}

	protected Either<RollbackHandler, MethodActivationStatusEnum> getRollbackHandler(DBTypeEnum dbType) {
		Either<RollbackHandler, MethodActivationStatusEnum> result;
		if (rollBackHandlersMap.containsKey(dbType)) {
			result = Either.left(rollBackHandlersMap.get(dbType));
		} else {
			result = Either.right(MethodActivationStatusEnum.NOT_FOUND);
		}
		return result;
	}

}
