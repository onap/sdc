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

import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.graph.datatype.RelationEndPoint;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.FilterKeyEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.IArtifactOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IServiceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.ServiceMetadataData;
import org.openecomp.sdc.be.resources.data.UserData;
import org.openecomp.sdc.be.resources.data.category.CategoryData;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component("service-operation")
@Deprecated
public class ServiceOperation extends ComponentOperation implements IServiceOperation {

	private static Logger log = LoggerFactory.getLogger(ServiceOperation.class.getName());

	@Resource
	private IArtifactOperation artifactOperation;

	@Resource
	private IElementOperation elementOperation;

	public ServiceOperation() {
		log.debug("ServiceOperation created");
	}

	@Override
	public Either<Service, StorageOperationStatus> createService(Service service) {
		return createService(service, false);
	}

	@Override
	public Either<Service, StorageOperationStatus> createService(Service service, boolean inTransaction) {
		Either<Service, StorageOperationStatus> result = null;

		try {

			ServiceMetadataData serviceData = getServiceMetaDataFromService(service);
			addComponentInternalFields(serviceData);
			String uniqueId = (String) serviceData.getUniqueId();
			generateUUID(service);

			String userId = service.getCreatorUserId();

			Either<UserData, TitanOperationStatus> findUser = findUser(userId);

			if (findUser.isRight()) {
				TitanOperationStatus status = findUser.right().value();
				log.error("Cannot find user {} in the graph. status is {}",userId,status);
				return sendError(status, StorageOperationStatus.USER_NOT_FOUND);
			}

			UserData creatorUserData = findUser.left().value();
			UserData updaterUserData = creatorUserData;
			String updaterUserId = service.getLastUpdaterUserId();
			if (updaterUserId != null && !updaterUserId.equals(userId)) {
				findUser = findUser(updaterUserId);
				if (findUser.isRight()) {
					TitanOperationStatus status = findUser.right().value();
					log.error("Cannot find user {} in the graph. status is {}",userId, status);
					return sendError(status, StorageOperationStatus.USER_NOT_FOUND);
				} else {
					updaterUserData = findUser.left().value();
				}
			}

			// get category
			List<CategoryDefinition> categories = service.getCategories();
			CategoryData categoryData = null;

			String categoryName = categories.get(0).getName();
			if (categoryName != null) {
				Either<CategoryData, StorageOperationStatus> categoryResult = elementOperation.getNewCategoryData(categoryName, NodeTypeEnum.ServiceNewCategory, CategoryData.class);
				if (categoryResult.isRight()) {
					StorageOperationStatus status = categoryResult.right().value();
					/*
					 * TitanOperationStatus titanStatus = null; if(ActionStatus.CATEGORY_NOT_FOUND.equals(status)){ titanStatus = TitanOperationStatus.NOT_FOUND; }else{ titanStatus = TitanOperationStatus.GENERAL_ERROR; }
					 */
					log.error("Cannot find category {} in the graph. status is {}",categoryName,status);
					return Either.right(status);
				}

				categoryData = categoryResult.left().value();
			}

			StorageOperationStatus storageOperationStatus = createTagsForComponent(service);
			if (storageOperationStatus != StorageOperationStatus.OK) {
				return Either.right(storageOperationStatus);
			}

			log.debug("try to create service node on graph for id {}",serviceData.getUniqueId());
			Either<ServiceMetadataData, TitanOperationStatus> createNode = titanGenericDao.createNode(serviceData, ServiceMetadataData.class);
			if (createNode.isRight()) {
				TitanOperationStatus status = createNode.right().value();
				log.error("Error returned after creating service data node {}. status returned is {}",serviceData,status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}
			log.debug("create service node created on graph for id {}",serviceData.getUniqueId());

			TitanOperationStatus associateMetadata = associateMetadataToComponent(serviceData, creatorUserData, updaterUserData, null, null);
			if (associateMetadata != TitanOperationStatus.OK) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(associateMetadata));
				return result;
			}
			TitanOperationStatus associateCategory = associateMetadataCategoryToComponent(serviceData, categoryData);
			if (associateCategory != TitanOperationStatus.OK) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(associateCategory));
				return result;
			}

			Map<String, ArtifactDefinition> allArtifacts = new HashMap<String, ArtifactDefinition>();
			if (service.getArtifacts() != null) {
				allArtifacts.putAll(service.getArtifacts());
			}
			if (service.getDeploymentArtifacts() != null) {
				allArtifacts.putAll(service.getDeploymentArtifacts());
			}
			if (service.getServiceApiArtifacts() != null) {
				allArtifacts.putAll(service.getServiceApiArtifacts());
			}
			if (service.getToscaArtifacts() != null) {
				allArtifacts.putAll(service.getToscaArtifacts());
			}

			StorageOperationStatus associateArtifacts = associateArtifactsToComponent(NodeTypeEnum.Service, serviceData, allArtifacts);
			if (associateArtifacts != StorageOperationStatus.OK) {
				result = Either.right(associateArtifacts);
				return result;
			}

			TitanOperationStatus associateInputs = associateInputsToComponent(NodeTypeEnum.Service, serviceData, service.getInputs());
			if (associateInputs != TitanOperationStatus.OK) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(associateInputs));
				return result;
			}

			List<AdditionalInformationDefinition> additionalInformation = service.getAdditionalInformation();
			StorageOperationStatus addAdditionalInformation = addAdditionalInformationToService(uniqueId, additionalInformation);
			if (addAdditionalInformation != StorageOperationStatus.OK) {
				result = Either.right(addAdditionalInformation);
				return result;
			}

			result = this.getService(uniqueId, true);
			if (result.isRight()) {
				log.error("Cannot get full service from the graph. status is {}", result.right().value());
				return Either.right(result.right().value());
			}

			if (log.isDebugEnabled()) {
				String json = prettyJson.toJson(result.left().value());
				log.debug("Service retrieved is {}",json);
			}

			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}
	}

	private TitanOperationStatus associateMetadataCategoryToComponent(ServiceMetadataData serviceData, CategoryData categoryData) {
		Either<GraphRelation, TitanOperationStatus> result;
		if (categoryData != null) {
			result = titanGenericDao.createRelation(serviceData, categoryData, GraphEdgeLabels.CATEGORY, null);
			log.debug("After associating component {} to category {}. Edge type is {}",serviceData.getUniqueId(),categoryData,GraphEdgeLabels.CATEGORY);
			if (result.isRight()) {
				log.error("Faield to associate component {} to category {}. Edge type is {}",serviceData.getUniqueId(),categoryData,GraphEdgeLabels.CATEGORY);
				return result.right().value();
			}
		}
		return TitanOperationStatus.OK;
	}

	private StorageOperationStatus addAdditionalInformationToService(String resourceUniqueId, List<AdditionalInformationDefinition> additionalInformation) {

		StorageOperationStatus result = null;

		if (additionalInformation == null || true == additionalInformation.isEmpty()) {
			result = super.addAdditionalInformation(NodeTypeEnum.Service, resourceUniqueId, null);
		} else {
			if (additionalInformation.size() == 1) {
				result = super.addAdditionalInformation(NodeTypeEnum.Service, resourceUniqueId, additionalInformation.get(0));
			} else {
				result = StorageOperationStatus.BAD_REQUEST;
				log.info("Cannot create resource with more than one additional information object. The number of received object is {}", additionalInformation.size());
			}
		}
		return result;
	}

	public Either<Service, StorageOperationStatus> cloneService(Service other, String version, boolean inTransaction) {
		return cloneService(other, version, null, inTransaction);
	}

	public Either<Service, StorageOperationStatus> cloneService(Service other, String version, LifecycleStateEnum targetLifecycle, boolean inTransaction) {
		Either<Service, StorageOperationStatus> result = null;

		try {
			String origServiceId = other.getUniqueId();
			other.setVersion(version);
			other.setUniqueId(null);

			Either<Integer, StorageOperationStatus> counterStatus = getComponentInstanceCoutner(origServiceId, NodeTypeEnum.Service);
			if (counterStatus.isRight()) {
				StorageOperationStatus status = counterStatus.right().value();
				log.error("failed to get resource instance counter on service {}. status={}", origServiceId, counterStatus);
				result = Either.right(status);
				return result;
			}
			Map<String, List<ComponentInstanceInput>> inputsValuesMap = new HashMap<String, List<ComponentInstanceInput>>();
			List<InputDefinition> inputs = other.getInputs();
			if (inputs != null) {
				for (InputDefinition input : inputs) {

					Either<List<ComponentInstanceInput>, TitanOperationStatus> inputStatus = inputOperation
							.getComponentInstanceInputsByInputId(input.getUniqueId());

					if (inputStatus.isLeft() && inputStatus.left().value() != null) {
						inputsValuesMap.put(input.getName(), inputStatus.left().value());
					}
				}
			}	

			Either<Service, StorageOperationStatus> createServiceMD = createService(other, true);
			
			if (createServiceMD.isRight()) {
				StorageOperationStatus status = createServiceMD.right().value();
				log.error("failed to clone service. status= {}", status);
				result = Either.right(status);
				return result;
			}
		
			Service service = createServiceMD.left().value();			

			Either<ImmutablePair<List<ComponentInstance>, Map<String, String>>, StorageOperationStatus> cloneInstances = componentInstanceOperation.cloneAllComponentInstancesFromContainerComponent(origServiceId, service,
					NodeTypeEnum.Service, NodeTypeEnum.Resource, targetLifecycle, inputsValuesMap);
			if (cloneInstances.isRight()) {
				result = Either.right(cloneInstances.right().value());
				return result;
			}

			Either<Integer, StorageOperationStatus> setResourceInstanceCounter = setComponentInstanceCounter(service.getUniqueId(), NodeTypeEnum.Service, counterStatus.left().value(), true);
			if (setResourceInstanceCounter.isRight()) {
				StorageOperationStatus status = setResourceInstanceCounter.right().value();
				log.error("failed to set resource instance counter on service {}. status={}", service.getUniqueId(), setResourceInstanceCounter);
				result = Either.right(status);
				return result;
			}

			result = this.getService(service.getUniqueId(), true);
			if (result.isRight()) {
				log.error("Cannot get full service from the graph. status is {}", result.right().value());
				return Either.right(result.right().value());
			}

			if (log.isTraceEnabled()) {
				String json = prettyJson.toJson(result.left().value());
				log.trace("Resource retrieved is {}", json);
			}

			return result;
		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}
	}

	private ServiceMetadataData getServiceMetaDataFromService(Service service) {
		ServiceMetadataData serviceData = new ServiceMetadataData((ServiceMetadataDataDefinition) service.getComponentMetadataDefinition().getMetadataDataDefinition());
		if (service.getNormalizedName() == null || service.getNormalizedName().isEmpty()) {
			serviceData.getMetadataDataDefinition().setNormalizedName(ValidationUtils.normaliseComponentName(service.getName()));
		}
		if (service.getSystemName() == null || service.getSystemName().isEmpty()) {
			serviceData.getMetadataDataDefinition().setSystemName(ValidationUtils.convertToSystemName(service.getName()));
		}

		return serviceData;
	}

	private Either<Service, StorageOperationStatus> sendError(TitanOperationStatus status, StorageOperationStatus statusIfNotFound) {
		Either<Service, StorageOperationStatus> result;
		if (status == TitanOperationStatus.NOT_FOUND) {
			result = Either.right(statusIfNotFound);
			return result;
		} else {
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			return result;
		}
	}

	/** 
	 * 
	 */
	public Either<Service, StorageOperationStatus> getService(String uniqueId) {
		return getService(uniqueId, false);
	}

	public Either<Service, StorageOperationStatus> getService(String uniqueId, boolean inTransaction) {
		ComponentParametersView componentParametersView = new ComponentParametersView();
		return getService(uniqueId, componentParametersView, inTransaction);
	}

	public Either<Service, StorageOperationStatus> getService(String uniqueId, ComponentParametersView componentParametersView, boolean inTransaction) {

		Service service = null;
		Either<Service, StorageOperationStatus> result = null;
		try {

			NodeTypeEnum serviceNodeType = NodeTypeEnum.Service;
			NodeTypeEnum compInstNodeType = NodeTypeEnum.Resource;

			Either<ServiceMetadataData, StorageOperationStatus> getComponentByLabel = getComponentByLabelAndId(uniqueId, serviceNodeType, ServiceMetadataData.class);
			if (getComponentByLabel.isRight()) {
				result = Either.right(getComponentByLabel.right().value());
				return result;
			}
			ServiceMetadataData serviceData = getComponentByLabel.left().value();
			// Try to fetch resource from the cache. The resource will be
			// fetched only if the time on the cache equals to
			// the time on the graph.
			Either<Service, ActionStatus> componentFromCacheIfUpToDate = this.getComponentFromCacheIfUpToDate(uniqueId, serviceData, componentParametersView, Service.class, ComponentTypeEnum.SERVICE);
			if (componentFromCacheIfUpToDate.isLeft()) {
				Service cachedService = componentFromCacheIfUpToDate.left().value();
				log.debug("Service {} with uid {} was fetched from cache.", cachedService.getName(), cachedService.getUniqueId());
				return Either.left(cachedService);
			}

			service = convertServiceDataToService(serviceData);
			TitanOperationStatus status = null;
			if (false == componentParametersView.isIgnoreUsers()) {
				status = setComponentCreatorFromGraph(service, uniqueId, serviceNodeType);
				if (status != TitanOperationStatus.OK) {
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
					return result;
				}

				status = setComponentLastModifierFromGraph(service, uniqueId, serviceNodeType);
				if (status != TitanOperationStatus.OK) {
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
					return result;

				}
			}
			if (false == componentParametersView.isIgnoreCategories()) {
				status = setComponentCategoriesFromGraph(service);
				if (status != TitanOperationStatus.OK) {
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
					return result;

				}
			}

			if (false == componentParametersView.isIgnoreArtifacts()) {
				StorageOperationStatus storageStatus = setArtifactFromGraph(uniqueId, service, serviceNodeType, artifactOperation);
				if (storageStatus != StorageOperationStatus.OK) {
					result = Either.right(storageStatus);
					return result;
				}
			}

			if (false == componentParametersView.isIgnoreComponentInstances() || false == componentParametersView.isIgnoreComponentInstancesProperties() || false == componentParametersView.isIgnoreCapabilities()
					|| false == componentParametersView.isIgnoreRequirements()) {
				status = setComponentInstancesFromGraph(uniqueId, service, serviceNodeType, compInstNodeType);
				if (status != TitanOperationStatus.OK) {
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
					return result;

				}
			}
			if (false == componentParametersView.isIgnoreComponentInstancesProperties()) {
				status = setComponentInstancesPropertiesFromGraph(service);
				if (status != TitanOperationStatus.OK) {
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
					return result;
				}
			}
			if (false == componentParametersView.isIgnoreCapabilities()) {
				status = setCapabilitiesFromGraph(uniqueId, service, NodeTypeEnum.Service);
				if (status != TitanOperationStatus.OK) {
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
					return result;
				}
			}
			if (false == componentParametersView.isIgnoreRequirements()) {
				status = setRequirementsFromGraph(uniqueId, service, NodeTypeEnum.Service);
				if (status != TitanOperationStatus.OK) {
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
					return result;
				}
			}
			if (false == componentParametersView.isIgnoreAllVersions()) {
				status = setAllVersions(service);
				if (status != TitanOperationStatus.OK) {
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				}
			}
			if (false == componentParametersView.isIgnoreAdditionalInformation()) {
				status = setServiceAdditionalInformationFromGraph(uniqueId, service);
				if (status != TitanOperationStatus.OK) {
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				}
			}

			if (false == componentParametersView.isIgnoreGroups()) {
				status = setGroupsFromGraph(uniqueId, service, NodeTypeEnum.Resource);
				if (status != TitanOperationStatus.OK) {
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				}
			}
			if (false == componentParametersView.isIgnoreInputs()) {
				status = setComponentInputsFromGraph(uniqueId, service, true);
				if (status != TitanOperationStatus.OK) {
					log.error("Failed to set inputs of resource {}. status is {}",uniqueId,status);
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				}

			}

			if (false == componentParametersView.isIgnoreComponentInstancesInputs()) {
				status = setComponentInstancesInputsFromGraph(uniqueId, service);
				if (status != TitanOperationStatus.OK) {
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));

				}
			}

			result = Either.left(service);
			return result;
		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					titanGenericDao.rollback();
				} else {
					titanGenericDao.commit();
				}
			}
		}
	}

	// public Either<Service, StorageOperationStatus> getService_tx(String
	// uniqueId, boolean inTransaction) {
	//
	// Service service = null;
	// Either<Service, StorageOperationStatus> result = null;
	// try {
	//
	// NodeTypeEnum serviceNodeType = NodeTypeEnum.Service;
	// NodeTypeEnum compInstNodeType = NodeTypeEnum.Resource;
	//
	// Either<ServiceMetadataData, StorageOperationStatus> getComponentByLabel =
	// getComponentByLabelAndId_tx(uniqueId, serviceNodeType,
	// ServiceMetadataData.class);
	// if (getComponentByLabel.isRight()) {
	// result = Either.right(getComponentByLabel.right().value());
	// return result;
	// }
	// ServiceMetadataData serviceData = getComponentByLabel.left().value();
	// service = convertServiceDataToService(serviceData);
	//
	// TitanOperationStatus status = setComponentCreatorFromGraph(service,
	// uniqueId, serviceNodeType);
	// if (status != TitanOperationStatus.OK) {
	// result =
	// Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
	// return result;
	// }
	//
	// status = setComponentLastModifierFromGraph(service, uniqueId,
	// serviceNodeType);
	// if (status != TitanOperationStatus.OK) {
	// result =
	// Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
	// return result;
	//
	// }
	// status = setComponentCategoriesFromGraph(service);
	// if (status != TitanOperationStatus.OK) {
	// result =
	// Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
	// return result;
	//
	// }
	//
	// // status = setServicePropertiesFromGraph(uniqueId, resource, vertex);
	// // if (status != TitanOperationStatus.OK) {
	// // return
	// Either.right(TitanStatusConverter.convertTitanStatusToStorageStatus(status));
	// // }
	//
	// StorageOperationStatus storageStatus = setArtifactFromGraph(uniqueId,
	// service, serviceNodeType, artifactOperation);
	// if (storageStatus != StorageOperationStatus.OK) {
	// result = Either.right(storageStatus);
	// return result;
	// }
	//
	// status = setComponentInstancesFromGraph(uniqueId, service,
	// serviceNodeType, compInstNodeType);
	// if (status != TitanOperationStatus.OK) {
	// result =
	// Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
	// return result;
	//
	// }
	//
	// status = setComponentInstancesPropertiesFromGraph(uniqueId, service);
	// if (status != TitanOperationStatus.OK) {
	// result =
	// Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
	// return result;
	// }
	//
	// status = setCapabilitiesFromGraph(uniqueId, service,
	// NodeTypeEnum.Service);
	// if (status != TitanOperationStatus.OK) {
	// result =
	// Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
	// return result;
	// }
	//
	// status = setRequirementsFromGraph( uniqueId, service,
	// NodeTypeEnum.Service);
	// if (status != TitanOperationStatus.OK) {
	// result =
	// Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
	// return result;
	// }
	//
	// status = setAllVersions(service);
	// if (status != TitanOperationStatus.OK) {
	// return
	// Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
	// }
	//
	// status = setServiceAdditionalInformationFromGraph(uniqueId, service);
	// if (status != TitanOperationStatus.OK) {
	// return
	// Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
	// }
	//
	// result = Either.left(service);
	// return result;
	// } finally {
	// if (false == inTransaction) {
	// if (result == null || result.isRight()) {
	// titanGenericDao.rollback();
	// } else {
	// titanGenericDao.commit();
	// }
	// }
	// }
	// }

	@Override
	TitanOperationStatus setComponentCategoriesFromGraph(Component service) {

		String uniqueId = service.getUniqueId();
		Either<List<ImmutablePair<CategoryData, GraphEdge>>, TitanOperationStatus> parentNode = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Service), uniqueId, GraphEdgeLabels.CATEGORY,
				NodeTypeEnum.ServiceNewCategory, CategoryData.class);
		if (parentNode.isRight()) {
			return parentNode.right().value();
		}

		List<ImmutablePair<CategoryData, GraphEdge>> listValue = parentNode.left().value();
		if (log.isDebugEnabled())
			log.debug("Result after looking for category nodes pointed by service {}. status is {}", uniqueId, listValue);
		if (listValue.size() > 1) {
			log.error("Multiple edges foud between resource {} to category nodes.",uniqueId);
		}
		ImmutablePair<CategoryData, GraphEdge> value = listValue.get(0);
		if (log.isDebugEnabled())
			log.debug("Found parent node {}", value);

		CategoryData categoryData = value.getKey();
		CategoryDefinition categoryDefinition = new CategoryDefinition(categoryData.getCategoryDataDefinition());

		List<CategoryDefinition> categories = new ArrayList<>();
		categories.add(categoryDefinition);
		service.setCategories(categories);
		return TitanOperationStatus.OK;

	}

	@Override
	public Either<Service, StorageOperationStatus> deleteService(String serviceId) {
		return deleteService(serviceId, false);
	}

	@Override
	public Either<Service, StorageOperationStatus> deleteService(String serviceId, boolean inTransaction) {

		Either<Service, StorageOperationStatus> result = Either.right(StorageOperationStatus.GENERAL_ERROR);
		try {

			Either<TitanGraph, TitanOperationStatus> graphResult = titanGenericDao.getGraph();
			if (graphResult.isRight()) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graphResult.right().value()));
				return result;
			}

			Either<ServiceMetadataData, TitanOperationStatus> serviceNode = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Service), serviceId, ServiceMetadataData.class);
			if (serviceNode.isRight()) {
				TitanOperationStatus status = serviceNode.right().value();
				log.error("Failed to find service {}. status is {}",serviceId,status);
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}

			Either<Service, StorageOperationStatus> serviceRes = getService(serviceId, true);
			if (serviceRes.isRight()) {
				StorageOperationStatus status = serviceRes.right().value();
				log.error("Failed to find sevice {}.status is {}",serviceId,status);
				result = Either.right(status);
				return result;
			}
			Service service = serviceRes.left().value();

			Either<List<ComponentInstance>, StorageOperationStatus> deleteAllResourceInstancesRes = componentInstanceOperation.deleteAllComponentInstances(serviceId, NodeTypeEnum.Service, true);
			log.debug("After deleting resource instances under service {}.Result is {}",serviceId,deleteAllResourceInstancesRes);
			if (deleteAllResourceInstancesRes.isRight()) {
				StorageOperationStatus status = deleteAllResourceInstancesRes.right().value();
				if (status != StorageOperationStatus.NOT_FOUND) {
					log.error("Failed to delete resource instances under service {} .status is ",serviceId,status);
					result = Either.right(status);
					return result;
				}
			}
			StorageOperationStatus removeArtifactsFromResource = removeArtifactsFromComponent(service, NodeTypeEnum.Service);
			log.debug("After deleting artifacts nodes in the graph. status is {}",removeArtifactsFromResource);
			if (!removeArtifactsFromResource.equals(StorageOperationStatus.OK)) {
				result = Either.right(removeArtifactsFromResource);
				return result;
			}

			StorageOperationStatus removeInputsFromResource = removeInputsFromComponent(NodeTypeEnum.Service, service);
			log.debug("After deleting requirements nodes in the graph. status is {}",removeInputsFromResource);
			if (removeInputsFromResource != StorageOperationStatus.OK) {
				result = Either.right(removeInputsFromResource);
				return result;
			}

			Either<List<ImmutablePair<PropertyData, GraphEdge>>, TitanOperationStatus> deleteChildrenNodesRes = titanGenericDao.deleteChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Service), serviceId, GraphEdgeLabels.PROPERTY,
					NodeTypeEnum.Property, PropertyData.class);

			if (deleteChildrenNodesRes.isRight()) {
				TitanOperationStatus status = deleteChildrenNodesRes.right().value();
				if (status != TitanOperationStatus.NOT_FOUND) {
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
					return result;
				}
			}

			StorageOperationStatus removeAdditionalInformationFromService = super.deleteAdditionalInformation(NodeTypeEnum.Service, serviceId);
			log.debug("After deleting additional information node in the graph. status is {}",removeAdditionalInformationFromService);
			if (!removeAdditionalInformationFromService.equals(StorageOperationStatus.OK)) {
				result = Either.right(removeAdditionalInformationFromService);
				return result;
			}

			StorageOperationStatus removeGroupsFromService = super.deleteGroups(NodeTypeEnum.Service, serviceId);
			log.debug("After deleting group nodes in the graph. status is {}",removeGroupsFromService);
			if (!removeGroupsFromService.equals(StorageOperationStatus.OK)) {
				result = Either.right(removeGroupsFromService);
				return result;
			}

			Either<ServiceMetadataData, TitanOperationStatus> deleteServiceNodeRes = titanGenericDao.deleteNode(serviceNode.left().value(), ServiceMetadataData.class);
			if (deleteServiceNodeRes.isRight()) {
				TitanOperationStatus status = deleteServiceNodeRes.right().value();
				log.error("Failed to delete service node {}. status is {}",serviceId, status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}

			result = Either.left(service);

			return result;
		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.debug("deleteService operation : Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("deleteService operation : Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}

	}

	@Override
	public Either<Boolean, StorageOperationStatus> validateServiceNameExists(String serviceName) {
		return validateServiceNameUniqueness(serviceName, titanGenericDao);
	}

	private Service convertServiceDataToService(ServiceMetadataData serviceData) {
		ServiceMetadataDefinition serviceMetadataDefinition = new ServiceMetadataDefinition((ServiceMetadataDataDefinition) serviceData.getMetadataDataDefinition());

		Service service = new Service(serviceMetadataDefinition);

		return service;
	}

	@Override
	public <T extends Component> Either<T, StorageOperationStatus> getComponent(String id, Class<T> clazz) {

		Either<Service, StorageOperationStatus> component = getService(id);
		if (component.isRight()) {
			return Either.right(component.right().value());
		}
		return Either.left(clazz.cast(component.left().value()));
	}

	@Override
	@SuppressWarnings("unchecked")
	public Either<List<Service>, StorageOperationStatus> getFollowed(String userId, Set<LifecycleStateEnum> lifecycleStates, Set<LifecycleStateEnum> lastStateStates, boolean inTransaction) {

		return (Either<List<Service>, StorageOperationStatus>) (Either<?, StorageOperationStatus>) getFollowedComponent(userId, lifecycleStates, lastStateStates, inTransaction, titanGenericDao, NodeTypeEnum.Service);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Either<T, StorageOperationStatus> getComponent(String id, boolean inTransaction) {
		return (Either<T, StorageOperationStatus>) getService(id, inTransaction);
	}

	@Override
	public Either<Set<Service>, StorageOperationStatus> getCatalogData(Map<String, Object> propertiesToMatch, boolean inTransaction) {
		return getComponentCatalogData(NodeTypeEnum.Service, propertiesToMatch, Service.class, ServiceMetadataData.class, inTransaction);
	}

	@Override
	public Either<Service, StorageOperationStatus> updateService(Service service, boolean inTransaction) {
		Either<Service, StorageOperationStatus> result = updateComponent(service, inTransaction, titanGenericDao, Service.class, NodeTypeEnum.Service);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Either<T, StorageOperationStatus> updateComponent(T component, boolean inTransaction) {
		return (Either<T, StorageOperationStatus>) updateService((Service) component, inTransaction);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Either<Component, StorageOperationStatus> deleteComponent(String id, boolean inTransaction) {
		return (Either<Component, StorageOperationStatus>) (Either<?, StorageOperationStatus>) deleteService(id, inTransaction);
	}

	@Override
	protected ComponentMetadataData getMetaDataFromComponent(Component component) {
		return getServiceMetaDataFromService((Service) component);
	}

	@Override
	public <T> Either<T, StorageOperationStatus> getLightComponent(String id, boolean inTransaction) {
		return getLightComponent(id, NodeTypeEnum.Service, inTransaction);
	}

	@Override
	public <T> Either<List<T>, StorageOperationStatus> getFilteredComponents(Map<FilterKeyEnum, String> filters, boolean inTransaction) {
		Either<List<T>, StorageOperationStatus> components = null;

		String categoryName = filters.get(FilterKeyEnum.CATEGORY);
		String distributionStatus = filters.get(FilterKeyEnum.DISTRIBUTION_STATUS);
		DistributionStatusEnum distEnum = DistributionStatusEnum.findState(distributionStatus);
		if (distributionStatus != null && distEnum == null) {
			filters.remove(FilterKeyEnum.CATEGORY);
			return Either.right(StorageOperationStatus.CATEGORY_NOT_FOUND);
		}

		if (categoryName != null) { // primary filter
			components = fetchByCategoryOrSubCategoryName(categoryName, NodeTypeEnum.ServiceNewCategory, GraphEdgeLabels.CATEGORY.getProperty(), NodeTypeEnum.Service, inTransaction, ServiceMetadataData.class, null);
			if (components.isLeft() && distEnum != null) {// secondary filter
				Predicate<T> statusFilter = p -> ((Service) p).getDistributionStatus().equals(distEnum);
				return Either.left(components.left().value().stream().filter(statusFilter).collect(Collectors.toList()));
			}
			filters.remove(FilterKeyEnum.DISTRIBUTION_STATUS);
			return components;
		}
		components = fetchByDistributionStatus(distEnum.name(), inTransaction);
		if (components.isRight()) { // not found == empty list
			return Either.left(new ArrayList<>());
		}
		return components;
	}

	private <T> Either<List<T>, StorageOperationStatus> fetchByDistributionStatus(String status, boolean inTransaction) {
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.DISTRIBUTION_STATUS.getProperty(), status);
		props.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);
		return (Either<List<T>, StorageOperationStatus>) (Either<?, StorageOperationStatus>) getServiceListByCriteria(props, inTransaction);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Either<List<Service>, StorageOperationStatus> getTesterFollowed(String userId, Set<LifecycleStateEnum> lifecycleStates, boolean inTransaction) {
		return (Either<List<Service>, StorageOperationStatus>) (Either<?, StorageOperationStatus>) getTesterFollowedComponent(userId, lifecycleStates, inTransaction, NodeTypeEnum.Service);
	}

	@Override
	public Either<Service, StorageOperationStatus> updateDestributionStatus(Service service, User user, DistributionStatusEnum distributionStatus) {
		String userId = user.getUserId();
		Either<UserData, TitanOperationStatus> findUser = findUser(userId);
		if (findUser.isRight()) {
			TitanOperationStatus status = findUser.right().value();
			log.error("Cannot find user {} in the graph. status is {}", userId, status);
			return sendError(status, StorageOperationStatus.USER_NOT_FOUND);
		}
		UserData userData = findUser.left().value();

		Either<ServiceMetadataData, TitanOperationStatus> serviceMetadataDataRequeset = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Service), service.getUniqueId(), ServiceMetadataData.class);
		if (serviceMetadataDataRequeset.isRight()) {
			TitanOperationStatus status = serviceMetadataDataRequeset.right().value();
			log.error("Cannot find service {} in the graph. status is {}",service.getUniqueId(),status);
			return sendError(status, StorageOperationStatus.NOT_FOUND);
		}
		ServiceMetadataData serviceMetadataData = serviceMetadataDataRequeset.left().value();

		StorageOperationStatus result = StorageOperationStatus.OK;

		Either<GraphRelation, TitanOperationStatus> deleteIncomingRelation = deleteLastDistributionModifierRelation(service);
		if (deleteIncomingRelation.isRight() && deleteIncomingRelation.right().value() != TitanOperationStatus.NOT_FOUND) {
			log.error("Failed to delete user from component {}. Edge type is {}",service.getUniqueId(),GraphEdgeLabels.LAST_DISTRIBUTION_STATE_MODIFAIER);
			result = DaoStatusConverter.convertTitanStatusToStorageStatus(deleteIncomingRelation.right().value());
			return Either.right(result);
		}

		Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(userData, serviceMetadataData, GraphEdgeLabels.LAST_DISTRIBUTION_STATE_MODIFAIER, null);
		log.debug("After associating user {} to component {}. Edge type is {}",userData,serviceMetadataData.getUniqueId(),GraphEdgeLabels.LAST_DISTRIBUTION_STATE_MODIFAIER);
		if (createRelation.isRight()) {
			log.error("Failed to associate user {} to component {}. Edge type is {}",userData,serviceMetadataData.getUniqueId(),GraphEdgeLabels.LAST_DISTRIBUTION_STATE_MODIFAIER);
			result = DaoStatusConverter.convertTitanStatusToStorageStatus(createRelation.right().value());
			return Either.right(result);
		}
		service.setDistributionStatus(distributionStatus);
		Either<Service, StorageOperationStatus> updateResponse = updateComponent(service, true, titanGenericDao, Service.class, NodeTypeEnum.Service);

		return updateResponse;

	}

	private Either<GraphRelation, TitanOperationStatus> deleteLastDistributionModifierRelation(Service service) {
		GraphRelation lastDistributionStateModifaierRelation = new GraphRelation();
		lastDistributionStateModifaierRelation.setType(GraphEdgeLabels.LAST_DISTRIBUTION_STATE_MODIFAIER.getProperty());
		RelationEndPoint relationEndPoint = new RelationEndPoint(NodeTypeEnum.Service, UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Service), service.getUniqueId());
		lastDistributionStateModifaierRelation.setTo(relationEndPoint);
		Either<GraphRelation, TitanOperationStatus> deleteIncomingRelation = titanGenericDao.deleteIncomingRelation(lastDistributionStateModifaierRelation);
		return deleteIncomingRelation;
	}

	@Override
	public Either<Set<Service>, StorageOperationStatus> getCertifiedServicesWithDistStatus(Map<String, Object> propertiesToMatch, Set<DistributionStatusEnum> distStatus, boolean inTransaction) {
		log.debug("Start getCertifiedServicesWithDistStatus.");
		Set<Service> servicesSet = new HashSet<Service>();
		if (distStatus != null && !distStatus.isEmpty()) {
			for (DistributionStatusEnum status : distStatus) {
				Map<String, Object> props = new HashMap<>();
				props.putAll(propertiesToMatch);
				props.put(GraphPropertiesDictionary.DISTRIBUTION_STATUS.getProperty(), status.name());
				Either<Set<Service>, StorageOperationStatus> services = retrieveCertifiedServicesWithStatus(inTransaction, props);
				if (services.isRight()) {
					return services;
				} else {
					servicesSet.addAll(services.left().value());
				}
			}
			return Either.left(servicesSet);
		} else {
			return retrieveCertifiedServicesWithStatus(inTransaction, propertiesToMatch);
		}
	}

	private Either<Set<Service>, StorageOperationStatus> retrieveCertifiedServicesWithStatus(boolean inTransaction, Map<String, Object> props) {
		Either<List<ServiceMetadataData>, TitanOperationStatus> criteriaRes = titanGenericDao.getByCriteria(NodeTypeEnum.Service, props, ServiceMetadataData.class);
		return retrieveComponentsFromNodes(criteriaRes, inTransaction);
	}

	public Either<List<Service>, StorageOperationStatus> getServiceCatalogData(boolean inTransaction) {

		long start = System.currentTimeMillis();

		try {
			/*
			 * Map<String, Object> propertiesToMatch = new HashMap<>(); propertiesToMatch.put(GraphPropertiesDictionary.STATE.getProperty (), LifecycleStateEnum.CERTIFIED.name()); Either<List<ServiceMetadataData>, TitanOperationStatus>
			 * lastVersionNodes = getLastVersion(NodeTypeEnum.Service, propertiesToMatch, ServiceMetadataData.class); if (lastVersionNodes.isRight() && lastVersionNodes.right().value() != TitanOperationStatus.NOT_FOUND) { return
			 * Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus (lastVersionNodes.right().value())); } List<ServiceMetadataData> notCertifiedHighest = (lastVersionNodes.isLeft() ? lastVersionNodes.left().value() : new
			 * ArrayList<ServiceMetadataData>());
			 * 
			 * propertiesToMatch.put(GraphPropertiesDictionary. IS_HIGHEST_VERSION.getProperty(), true); Either<List<ServiceMetadataData>, TitanOperationStatus> componentsNodes = titanGenericDao.getByCriteria(NodeTypeEnum.Service, propertiesToMatch,
			 * ServiceMetadataData.class); if (componentsNodes.isRight() && componentsNodes.right().value() != TitanOperationStatus.NOT_FOUND) { return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus
			 * (componentsNodes.right().value())); } List<ServiceMetadataData> certifiedHighest = (componentsNodes.isLeft() ? componentsNodes.left().value() : new ArrayList<ServiceMetadataData>());
			 */

			Either<List<ServiceMetadataData>, TitanOperationStatus> listOfHighestComponents = this.getListOfHighestComponents(NodeTypeEnum.Service, ServiceMetadataData.class);
			if (listOfHighestComponents.isRight() && listOfHighestComponents.right().value() != TitanOperationStatus.NOT_FOUND) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(listOfHighestComponents.right().value()));
			}

			List<ServiceMetadataData> notCertifiedHighest = listOfHighestComponents.left().value();

			List<Service> result = new ArrayList<>();

			if (notCertifiedHighest != null && false == notCertifiedHighest.isEmpty()) {

				// fetch from cache
				long startFetchAllFromCache = System.currentTimeMillis();

				Map<String, Long> components = notCertifiedHighest.stream().collect(Collectors.toMap(p -> p.getMetadataDataDefinition().getUniqueId(), p -> p.getMetadataDataDefinition().getLastUpdateDate()));

				Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> componentsFromCacheForCatalog = this.getComponentsFromCacheForCatalog(components, ComponentTypeEnum.SERVICE);
				if (componentsFromCacheForCatalog.isLeft()) {
					ImmutablePair<List<Component>, Set<String>> immutablePair = componentsFromCacheForCatalog.left().value();
					List<Component> list = immutablePair.getLeft();
					if (list != null) {
						for (Component component : list) {
							result.add((Service) component);
						}
						List<String> addedUids = list.stream().map(p -> p.getComponentMetadataDefinition().getMetadataDataDefinition().getUniqueId()).collect(Collectors.toList());
						notCertifiedHighest = notCertifiedHighest.stream().filter(p -> false == addedUids.contains(p.getMetadataDataDefinition().getUniqueId())).collect(Collectors.toList());
					}
				}
				long endFetchAllFromCache = System.currentTimeMillis();
				log.debug("Fetch all catalog services metadata from cache took {} ms", (endFetchAllFromCache - startFetchAllFromCache));
				log.debug("The number of services added to catalog from cache is {}", result.size());

				log.debug("The number of services needed to be fetch as light component is {}", notCertifiedHighest.size());
				for (ServiceMetadataData data : notCertifiedHighest) {
					Either<Service, StorageOperationStatus> component = getLightComponent(data.getMetadataDataDefinition().getUniqueId(), inTransaction);
					if (component.isRight()) {
						log.debug("Failed to get Service for id = {}, error : {}. Skip service", data.getUniqueId(), component.right().value());
					} else {
						result.add(component.left().value());
					}
				}
			}
			return Either.left(result);
		} finally {
			if (false == inTransaction) {
				titanGenericDao.commit();
			}
			log.debug("Fetch all catalog services took {} ms",(System.currentTimeMillis() - start));
		}

	}

	public Either<List<Service>, StorageOperationStatus> getServiceCatalogDataLatestCertifiedAndNotCertified(boolean inTransaction) {
		Map<String, Object> properties = new HashMap<>();

		properties.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);
		List<Service> result = new ArrayList<>();
		Either<List<ServiceMetadataData>, TitanOperationStatus> lastVersionNodes = titanGenericDao.getByCriteria(NodeTypeEnum.Service, properties, ServiceMetadataData.class);

		if (lastVersionNodes.isRight() && lastVersionNodes.right().value() != TitanOperationStatus.NOT_FOUND) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(lastVersionNodes.right().value()));
		}

		List<ServiceMetadataData> latestServices;

		if (lastVersionNodes.isLeft()) {
			latestServices = lastVersionNodes.left().value();
		} else {
			return Either.left(result);
		}

		for (ServiceMetadataData data : latestServices) {
			Either<Service, StorageOperationStatus> component = getLightComponent(data.getMetadataDataDefinition().getUniqueId(), inTransaction);
			if (component.isRight()) {
				log.debug("Failed to get Service for id =  {} error : {} skip resource",data.getUniqueId(),component.right().value());
			} else {
				result.add(component.left().value());
			}
		}

		return Either.left(result);

	}

	private Either<List<Service>, StorageOperationStatus> getServiceListByCriteria(Map<String, Object> props, boolean inTransaction) {
		props.put(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.Service.getName());
		Either<List<ServiceMetadataData>, TitanOperationStatus> byCriteria = titanGenericDao.getByCriteria(NodeTypeEnum.Service, props, ServiceMetadataData.class);

		if (byCriteria.isRight()) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(byCriteria.right().value()));
		}
		List<Service> services = new ArrayList<>();
		List<ServiceMetadataData> servicesDataList = byCriteria.left().value();
		for (ServiceMetadataData data : servicesDataList) {
			Either<Service, StorageOperationStatus> service = getService(data.getMetadataDataDefinition().getUniqueId(), inTransaction);
			if (service.isLeft()) {
				services.add(service.left().value());
			} else {
				log.debug("Failed to fetch resource for name = {}  and id = {}",data.getMetadataDataDefinition().getName(),data.getUniqueId());
			}
		}
		return Either.left(services);
	}

	public Either<List<Service>, StorageOperationStatus> getServiceListByUuid(String uuid, boolean inTransaction) {
		return getLatestServiceByUuid(uuid, false, inTransaction);
	}

	public Either<List<Service>, StorageOperationStatus> getLatestServiceByUuid(String uuid, boolean inTransaction) {
		return getLatestServiceByUuid(uuid, true, inTransaction);
	}

	private Either<List<Service>, StorageOperationStatus> getLatestServiceByUuid(String uuid, boolean isLatest, boolean inTransaction) {
		Map<String, Object> props = new HashMap<String, Object>();

		if (isLatest) {
			props.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), isLatest);
		}

		props.put(GraphPropertiesDictionary.UUID.getProperty(), uuid);
		return getServiceListByCriteria(props, inTransaction);
	}

	@Override
	public Either<List<Service>, StorageOperationStatus> getAll() {
		Either<List<Service>, StorageOperationStatus> serviceListByCriteria = getServiceListByCriteria(new HashMap<>(), false);
		if (serviceListByCriteria.isRight() && serviceListByCriteria.right().value() == StorageOperationStatus.NOT_FOUND) {
			return Either.left(Collections.emptyList());
		}
		return serviceListByCriteria;
	}

	public Either<List<Service>, StorageOperationStatus> getServiceListBySystemName(String systemName, boolean inTransaction) {
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.SYSTEM_NAME.getProperty(), systemName);
		return getServiceListByCriteria(props, inTransaction);
	}

	public Either<Service, StorageOperationStatus> getServiceByNameAndVersion(String name, String version, Map<String, Object> additionalParams, boolean inTransaction) {
		return getByNamesAndVersion(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty(), ValidationUtils.normaliseComponentName(name), version, additionalParams, inTransaction);
	}

	@Override
	public Either<Service, StorageOperationStatus> getServiceByNameAndVersion(String name, String version) {
		return getServiceByNameAndVersion(name, version, null, false);
	}

	protected Either<Service, StorageOperationStatus> getByNamesAndVersion(String nameKey, String nameValue, String version, Map<String, Object> additionalParams, boolean inTransaction) {
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(nameKey, nameValue);
		props.put(GraphPropertiesDictionary.VERSION.getProperty(), version);
		props.put(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.Service.getName());
		if (additionalParams != null && !additionalParams.isEmpty()) {
			props.putAll(additionalParams);
		}

		Either<List<ServiceMetadataData>, TitanOperationStatus> byCriteria = titanGenericDao.getByCriteria(NodeTypeEnum.Service, props, ServiceMetadataData.class);

		if (byCriteria.isRight()) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(byCriteria.right().value()));
		}
		List<ServiceMetadataData> dataList = byCriteria.left().value();
		if (dataList != null && !dataList.isEmpty()) {
			if (dataList.size() > 1) {
				log.debug("More that one instance of resource for name ={} and version = {}",nameValue,version);
				return Either.right(StorageOperationStatus.GENERAL_ERROR);
			}
			ServiceMetadataData serviceData = dataList.get(0);
			Either<Service, StorageOperationStatus> service = getService(serviceData.getMetadataDataDefinition().getUniqueId(), inTransaction);
			if (service.isRight()) {
				log.debug("Failed to fetch resource for name = {}  and id = {}",serviceData.getMetadataDataDefinition().getName(),serviceData.getMetadataDataDefinition().getUniqueId());
			}
			return service;
		}
		return Either.right(StorageOperationStatus.NOT_FOUND);
	}

	protected <T> Either<T, StorageOperationStatus> getComponentByNameAndVersion(String name, String version, Map<String, Object> additionalParams, boolean inTransaction) {
		return (Either<T, StorageOperationStatus>) getServiceByNameAndVersion(name, version, additionalParams, inTransaction);
	}

	@Override
	public Either<Service, StorageOperationStatus> getServiceBySystemNameAndVersion(String name, String version, boolean inTransaction) {
		return getByNamesAndVersion(GraphPropertiesDictionary.SYSTEM_NAME.getProperty(), name, version, null, inTransaction);
	}

	private TitanOperationStatus setServiceAdditionalInformationFromGraph(String uniqueId, Service service) {

		List<AdditionalInformationDefinition> additionalInformation = new ArrayList<>();

		Either<AdditionalInformationDefinition, TitanOperationStatus> either = additionalInformationOperation.getAllAdditionalInformationParameters(NodeTypeEnum.Service, uniqueId, true);

		if (either.isRight()) {
			TitanOperationStatus status = either.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				return TitanOperationStatus.OK;
			}
			return status;
		}

		AdditionalInformationDefinition additionalInformationDefinition = either.left().value();
		additionalInformation.add(additionalInformationDefinition);

		service.setAdditionalInformation(additionalInformation);

		return TitanOperationStatus.OK;

	}

	private TitanOperationStatus setAllVersions(Service service) {
		Either<Map<String, String>, TitanOperationStatus> res = getVersionList(NodeTypeEnum.Service, service.getVersion(), service, ServiceMetadataData.class);
		if (res.isRight()) {
			return res.right().value();
		}
		service.setAllVersions(res.left().value());
		return TitanOperationStatus.OK;
	}

	public Either<List<ArtifactDefinition>, StorageOperationStatus> getAdditionalArtifacts(String resourceId, boolean recursively, boolean inTransaction) {
		List<ArtifactDefinition> artifacts = new ArrayList<>();
		return Either.left(artifacts);
	}

	@Override
	public boolean isComponentExist(String serviceId) {
		return isComponentExist(serviceId, NodeTypeEnum.Service);
	}

	// @SuppressWarnings("unchecked")
	// @Override
	// public <T> Either<T, StorageOperationStatus> cloneComponent(T other,
	// String version, boolean inTransaction) {
	// return (Either<T, StorageOperationStatus>) cloneService((Service)other,
	// version, inTransaction);
	// }

	@SuppressWarnings("unchecked")
	@Override
	public <T> Either<T, StorageOperationStatus> cloneComponent(T other, String version, LifecycleStateEnum targetLifecycle, boolean inTransaction) {
		return (Either<T, StorageOperationStatus>) cloneService((Service) other, version, targetLifecycle, inTransaction);
	}

	@Override
	public Either<Integer, StorageOperationStatus> increaseAndGetComponentInstanceCounter(String componentId, boolean inTransaction) {
		return increaseAndGetComponentInstanceCounter(componentId, NodeTypeEnum.Service, inTransaction);
	}

	@Override
	protected StorageOperationStatus validateCategories(Component currentComponent, Component component, ComponentMetadataData componentData, NodeTypeEnum type) {
		List<CategoryDefinition> newcategories = component.getCategories();
		CategoryDefinition newCat = newcategories.get(0);
		CategoryDefinition currentCategory = currentComponent.getCategories().get(0);

		StorageOperationStatus status = StorageOperationStatus.OK;
		if (newCat != null && newCat.getName() != null && false == newCat.getName().equals(currentCategory.getName())) {
			log.debug("Going to update the category of the resource from {} to {}",currentCategory,newCat.getName());

			status = moveCategoryEdge(component, componentData, newCat, type);
			log.debug("Going to update the category of the resource from {} to {}. status is {}",currentCategory,newCat.getName(),status);
		}
		return status;
	}

	@Override
	protected <T extends Component> StorageOperationStatus updateDerived(Component component, Component currentComponent, ComponentMetadataData componentData, Class<T> clazz) {
		log.debug("Derived class isn't supported for resource");
		return null;
	}

	@Override
	public Service getDefaultComponent() {
		return new Service();
	}

	@Override
	public Either<Component, StorageOperationStatus> getMetadataComponent(String id, boolean inTransaction) {
		return getMetadataComponent(id, NodeTypeEnum.Service, inTransaction);
	}

	@Override
	Component convertComponentMetadataDataToComponent(ComponentMetadataData componentMetadataData) {
		return convertServiceDataToService((ServiceMetadataData) componentMetadataData);
	}

	@Override
	public Either<Boolean, StorageOperationStatus> validateComponentNameExists(String componentName) {
		return validateComponentNameUniqueness(componentName, titanGenericDao, NodeTypeEnum.Service);
	}

	@Override
	public Either<Component, StorageOperationStatus> markComponentToDelete(Component componentToDelete, boolean inTransaction) {
		return internalMarkComponentToDelete(componentToDelete, inTransaction);
	}

	@Override
	public Either<Boolean, StorageOperationStatus> isComponentInUse(String componentId) {
		return isComponentInUse(componentId, NodeTypeEnum.Service);
	}

	@Override
	public Either<List<String>, StorageOperationStatus> getAllComponentsMarkedForDeletion() {
		return getAllComponentsMarkedForDeletion(NodeTypeEnum.Service);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Either<T, StorageOperationStatus> getComponent(String id, ComponentParametersView componentParametersView, boolean inTransaction) {

		Either<Service, StorageOperationStatus> component = getService(id, componentParametersView, inTransaction);
		if (component.isRight()) {
			return Either.right(component.right().value());
		}
		return (Either<T, StorageOperationStatus>) component;
	}

	public Either<Service, StorageOperationStatus> updateService(Service service, boolean inTransaction, ComponentParametersView filterResultView) {
		return (Either<Service, StorageOperationStatus>) updateComponentFilterResult(service, inTransaction, titanGenericDao, service.getClass(), NodeTypeEnum.Service, filterResultView);
	}

	@Override
	protected <T> Either<T, StorageOperationStatus> updateComponentFilterResult(T component, boolean inTransaction, ComponentParametersView filterResultView) {
		return (Either<T, StorageOperationStatus>) updateService((Service) component, inTransaction, filterResultView);
	}

}
