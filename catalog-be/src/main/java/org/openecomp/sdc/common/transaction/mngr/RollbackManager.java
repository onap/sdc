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

import org.openecomp.sdc.common.transaction.api.RollbackHandler;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.DBActionCodeEnum;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.DBTypeEnum;
import org.openecomp.sdc.common.util.MethodActivationStatusEnum;

import fj.P;
import fj.data.Either;
import fj.data.HashMap;
import fj.data.List;

public class RollbackManager {
	private final HashMap<DBTypeEnum, RollbackHandler> rollbackHandlersMap;
	private final Integer transactionId;
	private final String userId; 
	private final String actionType;

	RollbackManager(Integer transactionId, String userId, String actionType, Iterable<RollbackHandler> rollbackHandlers) {
		this.transactionId = transactionId;
		this.userId = userId;
		this.actionType = actionType;
		this.rollbackHandlersMap = HashMap.from(List.iterableList(rollbackHandlers).map(i -> P.p(i.getDBType(), i)));
	}

	public DBActionCodeEnum transactionRollback() {
		List<DBActionCodeEnum> results = rollbackHandlersMap.values().map(RollbackHandler::doRollback);
		boolean failure = results.exists(r -> r == DBActionCodeEnum.FAIL_GENERAL);
		return failure ? DBActionCodeEnum.FAIL_GENERAL : DBActionCodeEnum.SUCCESS;
	}

	protected Either<RollbackHandler, MethodActivationStatusEnum> addRollbackHandler(RollbackHandler rollbackHandler) {
		Either<RollbackHandler, MethodActivationStatusEnum> result;
		if (rollbackHandlersMap.contains(rollbackHandler.getDBType())) {
			result = Either.right(MethodActivationStatusEnum.NOT_ALLOWED);
		} else {
			rollbackHandlersMap.set(rollbackHandler.getDBType(), rollbackHandler);
			result = Either.left(rollbackHandler);
		}
		return result;

	}

	protected Either<RollbackHandler, MethodActivationStatusEnum> createRollbackHandler(final DBTypeEnum dbType) {

		RollbackHandler rollbackHandler = new RollbackHandler(transactionId, userId, actionType) {

			@Override
			public DBTypeEnum getDBType() {
				return dbType;
			}

			@Override
			protected boolean isRollbackForPersistenceData() {
				return true;
			}
		};
		return addRollbackHandler(rollbackHandler);
	}

	protected Either<RollbackHandler, MethodActivationStatusEnum> getRollbackHandler(DBTypeEnum dbType) {
		// need to swap here because the uses of Either in SDC appears to be opposite of convention
		// by convention left is failure; in SDC right is failure
		return rollbackHandlersMap.get(dbType).toEither(MethodActivationStatusEnum.NOT_FOUND).swap();
	}
}
