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

package org.openecomp.sdc.be.model.cache.jobs;

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.cache.DaoInfo;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

public abstract class Job<E> {
	private static Logger log = LoggerFactory.getLogger(Job.class.getName());
	protected DaoInfo daoInfo;
	protected String componentId;
	protected long timestamp;
	protected NodeTypeEnum nodeTypeEnum;

	protected Job(DaoInfo daoInfo, String componentId, NodeTypeEnum nodeTypeEnum, long timestamp) {
		this.daoInfo = daoInfo;
		this.componentId = componentId;
		this.timestamp = timestamp;
		this.nodeTypeEnum = nodeTypeEnum;
	}

	protected Job(DaoInfo daoInfo, Component component, NodeTypeEnum nodeTypeEnum) {
		this.daoInfo = daoInfo;
		this.componentId = component.getUniqueId();
		this.timestamp = component.getLastUpdateDate();
		this.nodeTypeEnum = nodeTypeEnum;
	}

	public abstract E doWork();

	protected Either<ComponentMetadataData, StorageOperationStatus> getComponentMetaData(String componentId,
			NodeTypeEnum nodeTypeEnum) {
		Either<ComponentMetadataData, StorageOperationStatus> metaDataRes = daoInfo.getToscaOperationFacade().getComponentMetadata(componentId);
		if (metaDataRes.isRight()) {
			// in case we cant find the component on graph exit
			if (StorageOperationStatus.NOT_FOUND.equals(metaDataRes.right().value())) {
				log.debug("failed to locate component:{} on graph status:{}", componentId, metaDataRes.right().value());
			} else {
				log.debug("failed to get component:{} from graph status:{}", componentId, metaDataRes.right().value());
			}
		}
		return metaDataRes;
	}

	protected NodeTypeEnum getNodeTypeFromComponentType(ComponentTypeEnum type) {
		NodeTypeEnum result = null;
		switch (type) {
		case PRODUCT:
			result = NodeTypeEnum.Product;
			break;
		case RESOURCE:
			result = NodeTypeEnum.Resource;
			break;
		case SERVICE:
			result = NodeTypeEnum.Service;
			break;
		default:

		}
		return result;

	}
}
