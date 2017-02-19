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

import org.openecomp.sdc.be.dao.impl.ESCatalogDAO;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.be.resources.exception.ResourceDAOException;
import org.openecomp.sdc.common.transaction.api.IDBAction;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.DBActionCodeEnum;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.ESActionTypeEnum;

public class ESAction implements IDBAction {

	private ESCatalogDAO esCatalogDao;
	private ESArtifactData artifactData;
	private ESActionTypeEnum esActionType;

	public ESAction(ESCatalogDAO esCatalogDao, ESArtifactData artifactData, ESActionTypeEnum esActiontype) {
		this.esCatalogDao = esCatalogDao;
		this.artifactData = artifactData;
		this.esActionType = esActiontype;
	}

	@Override
	public DBActionCodeEnum doAction() {
		DBActionCodeEnum result = DBActionCodeEnum.SUCCESS;
		try {
			if (esActionType == ESActionTypeEnum.ADD_ARTIFACT || esActionType == ESActionTypeEnum.UPDATE_ARTIFACT) {
				esCatalogDao.writeArtifact(artifactData);
			} else if (esActionType == ESActionTypeEnum.REMOVE_ARTIFACT) {
				esCatalogDao.deleteArtifact(artifactData.getId());
			}

		} catch (ResourceDAOException daoException) {
			result = DBActionCodeEnum.FAIL_GENERAL;
		}
		return result;
	}

}
