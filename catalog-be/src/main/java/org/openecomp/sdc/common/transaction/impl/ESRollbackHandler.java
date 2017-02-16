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

package org.openecomp.sdc.common.transaction.impl;

import org.openecomp.sdc.be.dao.api.ResourceUploadStatus;
import org.openecomp.sdc.be.dao.impl.ESCatalogDAO;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.common.transaction.api.RollbackHandler;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.DBActionCodeEnum;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.DBTypeEnum;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.ESActionTypeEnum;
import org.openecomp.sdc.common.util.MethodActivationStatusEnum;

import fj.data.Either;

public class ESRollbackHandler extends RollbackHandler {

	public ESRollbackHandler(Integer transactionId, String userId, String actionType) {
		super(transactionId, userId, actionType);
	}

	public DBTypeEnum getDBType() {
		return DBTypeEnum.ELASTIC_SEARCH;
	}

	protected boolean isRollbackForPersistenceData() {
		return true;
	}

	public boolean isRollbackResultValid(DBActionCodeEnum rollbackResult) {
		return rollbackResult == DBActionCodeEnum.SUCCESS;
	}

	public Either<ESAction, MethodActivationStatusEnum> buildEsRollbackAction(ESCatalogDAO esCatalogDao, ESArtifactData artifactData, ESActionTypeEnum esActiontype) {
		Either<ESAction, MethodActivationStatusEnum> result;

		try {
			ESAction esRollbackAction = null;
			Either<ESArtifactData, ResourceUploadStatus> either = esCatalogDao.getArtifact(artifactData.getId());

			switch (esActiontype) {
			case ADD_ARTIFACT:

				if (either.isRight() && either.right().value() == ResourceUploadStatus.NOT_EXIST) {
					esRollbackAction = new ESAction(esCatalogDao, artifactData, ESActionTypeEnum.REMOVE_ARTIFACT);
				}
				break;
			case REMOVE_ARTIFACT:
				if (either.isLeft()) {
					esRollbackAction = new ESAction(esCatalogDao, artifactData, ESActionTypeEnum.ADD_ARTIFACT);
				}
				break;
			case UPDATE_ARTIFACT:

				if (either.isLeft()) {
					ESArtifactData originalArtifactData = either.left().value();
					esRollbackAction = new ESAction(esCatalogDao, originalArtifactData, ESActionTypeEnum.UPDATE_ARTIFACT);
				}
				break;

			}
			if (esRollbackAction != null) {
				result = Either.left(esRollbackAction);
			} else {
				result = Either.right(MethodActivationStatusEnum.FAILED);
			}
		} catch (Exception e) {
			result = Either.right(MethodActivationStatusEnum.FAILED);
		}

		return result;
	}

}
