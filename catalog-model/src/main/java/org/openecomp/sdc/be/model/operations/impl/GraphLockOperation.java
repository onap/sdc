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

package org.openecomp.sdc.be.model.operations.impl;

import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.ICacheMangerOperation;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("graph-lock-operation")
public class GraphLockOperation implements IGraphLockOperation {
	private static Logger log = LoggerFactory.getLogger(ResourceOperation.class.getName());

	@javax.annotation.Resource
	private TitanGenericDao titanGenericDao;
	
	@Autowired
	ToscaOperationFacade toscaOperationFacade;

	@javax.annotation.Resource
	private ICacheMangerOperation cacheManagerOperation;

	public GraphLockOperation() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openecomp.sdc.be.model.operations.impl.IGraphLockOperation# lockResource(java.lang.String, org.openecomp.sdc.be.model.operations.api.IResourceOperation)
	 */
	@Override
	public StorageOperationStatus lockComponent(String componentId, NodeTypeEnum nodeType) {
		log.info("lock resource with id {}", componentId);
		TitanOperationStatus lockElementStatus = null;
		try {

			// SET LAST UPDATE DATE OF THE COMPONENT.
			// In this way we mark the component as updated one (and component
			// won't be fetched from cache since the component in cache has
			// different timestamp)
//			Either<ComponentMetadataData, TitanOperationStatus> updateTime = updateModificationTimeOfComponent(componentId, nodeType);
//			if (updateTime.isRight()) {
//				TitanOperationStatus operationStatus = updateTime.right().value();
//				if (operationStatus != TitanOperationStatus.OK) {
//					return DaoStatusConverter.convertTitanStatusToStorageStatus(operationStatus);
//				}
//			}

			lockElementStatus = titanGenericDao.lockElement(componentId, nodeType);

		} catch (Exception e) {
			lockElementStatus = TitanOperationStatus.ALREADY_LOCKED;

		}

		return DaoStatusConverter.convertTitanStatusToStorageStatus(lockElementStatus);

	}

	/**
	 * update the last update date of the component
	 * 
	 * @param componentId
	 * @param nodeType
	 * @return
	 */
	private Either<ComponentMetadataData, TitanOperationStatus> updateModificationTimeOfComponent(String componentId, NodeTypeEnum nodeType) {

		if (nodeType == NodeTypeEnum.Resource || nodeType == NodeTypeEnum.Service || nodeType == NodeTypeEnum.Product) {
			// We fetch all node since update only timestamp make problems since
			// there is default resource type (VFC) which changes component
			// resource type when we update only timestamp(ResourceMetadataData
			// contains default value VFC on resourceType field).
			Either<ComponentMetadataData, TitanOperationStatus> findComp = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), componentId, ComponentMetadataData.class);

			if (findComp.isRight()) {
				return Either.right(findComp.right().value());
			}
			ComponentMetadataData componentMetadataData = findComp.left().value();
			componentMetadataData.getMetadataDataDefinition().setLastUpdateDate(System.currentTimeMillis());
			Either<ComponentMetadataData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(componentMetadataData, ComponentMetadataData.class);
			return updateNode;
		}
		return Either.right(TitanOperationStatus.OK);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openecomp.sdc.be.model.operations.impl.IGraphLockOperation# unlockResource(java.lang.String, org.openecomp.sdc.be.model.operations.api.IResourceOperation)
	 */
	@Override
	public StorageOperationStatus unlockComponent(String componentId, NodeTypeEnum nodeType) {

//		Either<Long, StorageOperationStatus> addComponentToCachePart1 = addComponentToCachePart1WithoutCommit(componentId, nodeType);

		TitanOperationStatus lockElementStatus = titanGenericDao.releaseElement(componentId, nodeType);

//		if (addComponentToCachePart1.isLeft()) {
//			Long lastUpdateDate = addComponentToCachePart1.left().value();
//			addComponentToCachePart2(componentId, lastUpdateDate, nodeType);
//		}
//
		return DaoStatusConverter.convertTitanStatusToStorageStatus(lockElementStatus);
	}

	private ResourceMetadataData getResourceMetaDataFromResource(Resource resource) {
		ResourceMetadataData resourceData = new ResourceMetadataData((ResourceMetadataDataDefinition) resource.getComponentMetadataDefinition().getMetadataDataDefinition());
		return resourceData;
	}

	@Override
	public StorageOperationStatus unlockComponentByName(String name, String componentId, NodeTypeEnum nodeType) {

//		Either<Long, StorageOperationStatus> addComponentToCachePart1 = addComponentToCachePart1WithoutCommit(componentId, nodeType);

		TitanOperationStatus lockElementStatus = titanGenericDao.releaseElement(name, nodeType);
//
//		if (addComponentToCachePart1.isLeft()) {
//			Long lastUpdateDate = addComponentToCachePart1.left().value();
//			addComponentToCachePart2(componentId, lastUpdateDate, nodeType);
//		}

		return DaoStatusConverter.convertTitanStatusToStorageStatus(lockElementStatus);
	}

	/**
	 * We fetch the last update date of the component
	 * 
	 * @param componentId
	 * @param nodeType
	 * @return
	 */
	private Either<Long, StorageOperationStatus> addComponentToCachePart1WithoutCommit(String componentId, NodeTypeEnum nodeType) {
		if (componentId != null) { // In case of error, the componentId might be
									// empty.
			if (nodeType == NodeTypeEnum.Resource || nodeType == NodeTypeEnum.Service || nodeType == NodeTypeEnum.Product) {
				Long lastUpdateDate = null;
				Either<ComponentMetadataData, StorageOperationStatus> resResult = toscaOperationFacade.getComponentMetadata(componentId);
				if (resResult.isLeft()) {
					ComponentMetadataData resourceMetadataData = resResult.left().value();
					lastUpdateDate = resourceMetadataData.getMetadataDataDefinition().getLastUpdateDate();

					return Either.left(lastUpdateDate);
				} else {
					return Either.right(resResult.right().value());
				}
			}
		}
		return Either.right(StorageOperationStatus.OPERATION_NOT_SUPPORTED);
	}

	/**
	 * add the component to the cache
	 * 
	 * @param componentId
	 * @param lastUpdateDate
	 * @param nodeType
	 * @return
	 */
	private Either<Long, StorageOperationStatus> addComponentToCachePart2(String componentId, Long lastUpdateDate, NodeTypeEnum nodeType) {
		if (componentId != null) { // In case of error, the componentId might be
									// empty.
			if (nodeType == NodeTypeEnum.Resource || nodeType == NodeTypeEnum.Service || nodeType == NodeTypeEnum.Product) {
				// add task to Q
				log.debug("Going to add component {} of type {} to cache", componentId, nodeType.name().toLowerCase());
				cacheManagerOperation.updateComponentInCache(componentId, lastUpdateDate, nodeType);
			}
		}
		return Either.right(StorageOperationStatus.OPERATION_NOT_SUPPORTED);
	}

	@Override
	public StorageOperationStatus lockComponentByName(String name, NodeTypeEnum nodeType) {
		log.info("lock resource with name {}", name);
		TitanOperationStatus lockElementStatus = null;
		try {

			lockElementStatus = titanGenericDao.lockElement(name, nodeType);

		} catch (Exception e) {
			lockElementStatus = TitanOperationStatus.ALREADY_LOCKED;

		}

		return DaoStatusConverter.convertTitanStatusToStorageStatus(lockElementStatus);

	}
}
