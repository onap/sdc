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

package org.openecomp.sdc.be.model.jsontitan.operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CompositionDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapCapabiltyProperty;
import org.openecomp.sdc.be.datatypes.elements.MapDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapGroupsDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListCapabiltyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RelationshipInstDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsontitan.datamodel.NodeType;
import org.openecomp.sdc.be.model.jsontitan.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.jsontitan.enums.JsonConstantKeysEnum;
import org.openecomp.sdc.be.model.jsontitan.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility.LogLevelEnum;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

@org.springframework.stereotype.Component("node-template-operation")
public class NodeTemplateOperation extends BaseOperation {
	private static final String ARTIFACT_PLACEHOLDER_TYPE = "type";
	private static final String ARTIFACT_PLACEHOLDER_DISPLAY_NAME = "displayName";
	private static final Object ARTIFACT_PLACEHOLDER_DESCRIPTION = "description";
	public static final String HEAT_ENV_NAME = "heatEnv";
	public static final String HEAT_VF_ENV_NAME = "VfHeatEnv";
	public static final String HEAT_ENV_SUFFIX = "env";
	private static Integer defaultHeatTimeout;
	public static final Integer NON_HEAT_TIMEOUT = 0;

	private static Logger logger = LoggerFactory.getLogger(NodeTemplateOperation.class.getName());

	public NodeTemplateOperation() {
		defaultHeatTimeout = ConfigurationManager.getConfigurationManager().getConfiguration().getDefaultHeatArtifactTimeoutMinutes();
		if ((defaultHeatTimeout == null) || (defaultHeatTimeout < 1)) {
			defaultHeatTimeout = 60;
		}
	}

	public static Integer getDefaultHeatTimeout() {
		return defaultHeatTimeout;
	}

	public Either<ImmutablePair<TopologyTemplate, String>, StorageOperationStatus> addComponentInstanceToTopologyTemplate(TopologyTemplate container, ToscaElement originToscaElement, String instanceNumberSuffix, ComponentInstance componentInstance,
			boolean allowDeleted, User user) {

		Either<ImmutablePair<TopologyTemplate, String>, StorageOperationStatus> result = null;
		Either<TopologyTemplate, StorageOperationStatus> addComponentInstanceRes = null;
		CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Going to create component instance {} in component {}", componentInstance, container.getUniqueId());
		ComponentInstanceDataDefinition componentInstanceData = null;
		Either<String, StorageOperationStatus> newInstanceNameRes = null;

		Either<GraphVertex, TitanOperationStatus> metadataVertex = titanDao.getVertexById(container.getUniqueId(), JsonParseFlagEnum.ParseJson);
		if (metadataVertex.isRight()) {
			TitanOperationStatus status = metadataVertex.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}

		if (result == null) {

			newInstanceNameRes = buildValidateInstanceName(container, originToscaElement, componentInstance, instanceNumberSuffix);
			if (newInstanceNameRes.isRight()) {
				result = Either.right(newInstanceNameRes.right().value());
			}
		}
		if (result == null) {
			componentInstanceData = buildComponentInstanceDataDefinition(componentInstance, container.getUniqueId(), newInstanceNameRes.left().value(), true, originToscaElement);

			addComponentInstanceRes = addComponentInstanceToTopologyTemplate(container, originToscaElement, componentInstanceData, metadataVertex.left().value(), allowDeleted, user);

			if (addComponentInstanceRes.isRight()) {
				StorageOperationStatus status = addComponentInstanceRes.right().value();
				if (status == StorageOperationStatus.NOT_FOUND) {
					status = StorageOperationStatus.INVALID_ID;
				}
				result = Either.right(status);
			}
			if(componentInstance.getOriginType()  == OriginTypeEnum.ServiceProxy){
				TopologyTemplate updatedContainer = addComponentInstanceRes.left().value();
				result = addServerCapAndReqToProxyServerInstance(
						updatedContainer, componentInstance, componentInstanceData);
				

			
			}
		}
		if (result == null) {
			result = Either.left(new ImmutablePair<>(addComponentInstanceRes.left().value(), componentInstanceData.getUniqueId()));
		}
		return result;
	}

	private Either<ImmutablePair<TopologyTemplate, String>, StorageOperationStatus> addServerCapAndReqToProxyServerInstance(TopologyTemplate updatedContainer, ComponentInstance componentInstance,
			
			ComponentInstanceDataDefinition componentInstanceData) {
		
		Either<ImmutablePair<TopologyTemplate, String>, StorageOperationStatus> result;
		
	
		Map<String, MapListCapabiltyDataDefinition> calcCap = updatedContainer.getCalculatedCapabilities();
		Map<String, MapListRequirementDataDefinition>  calcReg = updatedContainer.getCalculatedRequirements();
		Map<String, MapCapabiltyProperty> calcCapProp = updatedContainer.getCalculatedCapabilitiesProperties();
		
		
		Map<String, List<CapabilityDefinition>> additionalCap = componentInstance.getCapabilities();
		Map<String, List<RequirementDefinition>> additionalReq = componentInstance.getRequirements();
		
		MapListCapabiltyDataDefinition	allCalculatedCap = calcCap==null ||!calcCap.containsKey(componentInstanceData.getUniqueId())?new MapListCapabiltyDataDefinition() :calcCap.get(componentInstanceData.getUniqueId());
		/********capability****************************/
		StorageOperationStatus status = deleteToscaDataDeepElementsBlockToToscaElement(updatedContainer.getUniqueId(), EdgeLabelEnum.CALCULATED_CAPABILITIES, VertexTypeEnum.CALCULATED_CAPABILITIES, componentInstanceData.getUniqueId());
		if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to remove calculated capabilty  for instance {} in container {}. error {] ", componentInstanceData.getUniqueId(), updatedContainer.getUniqueId(), status);
			return Either.right(status);
		}
		
		if(additionalCap != null && !additionalCap.isEmpty()){
			
			Map<String, ListCapabilityDataDefinition> serverCap = additionalCap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, en -> new ListCapabilityDataDefinition(en.getValue().stream().map(iCap -> new CapabilityDataDefinition(iCap)).collect(Collectors.toList()))));
			
			serverCap.entrySet().forEach(entryPerType -> {
					entryPerType.getValue().getListToscaDataDefinition().forEach(cap -> {
						cap.addToPath(componentInstance.getUniqueId());							
						allCalculatedCap.add(entryPerType.getKey(), cap);
					});
				});
			
			status = addToscaDataDeepElementsBlockToToscaElement(updatedContainer.getUniqueId(), EdgeLabelEnum.CALCULATED_CAPABILITIES, VertexTypeEnum.CALCULATED_CAPABILITIES, allCalculatedCap,
					componentInstance.getUniqueId());
			
			/********capability property****************************/
			status = deleteToscaDataDeepElementsBlockToToscaElement(updatedContainer.getUniqueId(), EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, VertexTypeEnum.CALCULATED_CAP_PROPERTIES, componentInstanceData.getUniqueId());
			if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to remove calculated capabilty properties for instance {} in container {}. error {] ", componentInstanceData.getUniqueId(), updatedContainer.getUniqueId(), status);
				return Either.right(status);
			}
			
			
			MapCapabiltyProperty	allCalculatedCapProp = calcCapProp==null ||!calcCapProp.containsKey(componentInstanceData.getUniqueId())?new MapCapabiltyProperty() :calcCapProp.get(componentInstanceData.getUniqueId());
			

			additionalCap.forEach(new BiConsumer<String, List<CapabilityDefinition>>() {
				@Override
				public void accept(String s, List<CapabilityDefinition> caps) {

					if (caps != null && !caps.isEmpty()) {

						MapPropertiesDataDefinition dataToCreate = new MapPropertiesDataDefinition();

						for (CapabilityDefinition cap : caps) {
							List<ComponentInstanceProperty> capPrps = cap.getProperties();
							if (capPrps != null) {

								for (ComponentInstanceProperty cip : capPrps) {
									dataToCreate.put(cip.getName(), new PropertyDataDefinition(cip));
								}
							

								StringBuffer sb = new StringBuffer(componentInstance.getUniqueId());
								sb.append(ModelConverter.CAP_PROP_DELIM);
								
								sb.append(cap.getOwnerId());
								
								sb.append(ModelConverter.CAP_PROP_DELIM).append(s).append(ModelConverter.CAP_PROP_DELIM).append(cap.getName());
								allCalculatedCapProp.put(sb.toString(), dataToCreate);
							}
						}

					}

				}
			});
			
			status = addToscaDataDeepElementsBlockToToscaElement(updatedContainer.getUniqueId(), EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, VertexTypeEnum.CALCULATED_CAP_PROPERTIES, allCalculatedCapProp,
					componentInstance.getUniqueId());
		}
		
		/********Requirements property****************************/
		if(additionalReq != null && !additionalReq.isEmpty()){
		
			MapListRequirementDataDefinition	allCalculatedReq = calcReg==null ||!calcReg.containsKey(componentInstanceData.getUniqueId())?new MapListRequirementDataDefinition() :calcReg.get(componentInstanceData.getUniqueId());
			status = deleteToscaDataDeepElementsBlockToToscaElement(updatedContainer.getUniqueId(), EdgeLabelEnum.CALCULATED_REQUIREMENTS, VertexTypeEnum.CALCULATED_REQUIREMENTS, componentInstanceData.getUniqueId());
			if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to remove calculated Requirements for instance {} in container {}. error {] ", componentInstanceData.getUniqueId(), updatedContainer.getUniqueId(), status);
				return Either.right(status);
			}
			
			Map<String, ListRequirementDataDefinition> serverReq = additionalReq.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, en -> new ListRequirementDataDefinition(en.getValue().stream().map(iCap -> new RequirementDataDefinition(iCap)).collect(Collectors.toList()))));
				
			serverReq.entrySet().forEach(entryPerType -> {
					entryPerType.getValue().getListToscaDataDefinition().forEach(cap -> {
						cap.addToPath(componentInstance.getUniqueId());							
						allCalculatedReq.add(entryPerType.getKey(), cap);
					});
				});
			
			status = addToscaDataDeepElementsBlockToToscaElement(updatedContainer.getUniqueId(), EdgeLabelEnum.CALCULATED_REQUIREMENTS, VertexTypeEnum.CALCULATED_REQUIREMENTS, allCalculatedReq,
					componentInstance.getUniqueId());
		
		}


		Either<ToscaElement, StorageOperationStatus> updatedComponentInstanceRes = topologyTemplateOperation.getToscaElement(updatedContainer.getUniqueId());
		if (updatedComponentInstanceRes.isRight()) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to fetch updated topology template {} with new component instance {}. ", updatedContainer.getName(), componentInstance.getName());
			result = Either.right(updatedComponentInstanceRes.right().value());
		}
		result = Either.left(new ImmutablePair<>((TopologyTemplate)updatedComponentInstanceRes.left().value(), componentInstanceData.getUniqueId()));
		return result;
	}
	

	private Either<String, StorageOperationStatus> buildValidateInstanceName(TopologyTemplate container, ToscaElement originToscaElement, ComponentInstance componentInstance, String instanceNumberSuffix) {

		Either<String, StorageOperationStatus> result = null;
		String instanceName = componentInstance.getName();
		if (StringUtils.isEmpty(instanceName) || instanceName.equalsIgnoreCase(originToscaElement.getName()) || componentInstance.getOriginType() == OriginTypeEnum.ServiceProxy) {
			instanceName = buildComponentInstanceName(instanceNumberSuffix, instanceName);
		} else if (!isUniqueInstanceName(container, componentInstance.getName())) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to create component instance with name {} on component container {}. The instance with the same name already exists. ", componentInstance.getName(), container.getName());
			result = Either.right(StorageOperationStatus.ENTITY_ALREADY_EXISTS);
		}
		if (result == null) {
			result = Either.left(instanceName);
		}
		return result;
	}

	public Either<TopologyTemplate, StorageOperationStatus> addComponentInstanceToTopologyTemplate(TopologyTemplate container, ToscaElement originToscaElement, ComponentInstanceDataDefinition componentInstance, GraphVertex metadataVertex,
			boolean allowDeleted, User user) {

		Either<TopologyTemplate, StorageOperationStatus> result = null;
		Either<ToscaElement, StorageOperationStatus> updateContainerComponentRes = null;
		String containerComponentId = container.getUniqueId();
		CommonUtility.addRecordToLog(logger, LogLevelEnum.TRACE, "Going to create component instance {} in component {}", componentInstance, containerComponentId);
		String instOriginComponentId = componentInstance.getComponentUid();
		Either<GraphVertex, TitanOperationStatus> updateElement = null;

		Boolean isDeleted = (Boolean) originToscaElement.getMetadataValue(JsonPresentationFields.IS_DELETED);

		if (!allowDeleted && (isDeleted != null) && isDeleted) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Component {} is already deleted. Cannot add component instance", instOriginComponentId);
			result = Either.right(StorageOperationStatus.INVALID_ID);
		}
		if (result == null) {
			container.addComponentInstance(componentInstance);
			metadataVertex.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, System.currentTimeMillis());
			topologyTemplateOperation.fillToscaElementVertexData(metadataVertex, container, JsonParseFlagEnum.ParseAll);
			updateElement = titanDao.updateVertex(metadataVertex);
			if (updateElement.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update topology template {} with new component instance {}. ", container.getName(), componentInstance.getName());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(updateElement.right().value()));
			}
		}
		if (result == null) {
			Either<GraphVertex, StorageOperationStatus> addToscaDataRes = addComponentInstanceToscaDataToContainerComponent(originToscaElement, componentInstance, updateElement.left().value(), user);
			if (addToscaDataRes.isRight()) {
				result = Either.right(addToscaDataRes.right().value());
			}
		}

		if (result == null) {
			updateContainerComponentRes = topologyTemplateOperation.getToscaElement(containerComponentId);
			if (updateContainerComponentRes.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to fetch updated topology template {} with new component instance {}. ", container.getName(), componentInstance.getName());
				result = Either.right(updateContainerComponentRes.right().value());
			}
		}
		if (result == null) {
			result = Either.left((TopologyTemplate) updateContainerComponentRes.left().value());
		}
		return result;
	}

	public Either<ImmutablePair<TopologyTemplate, String>, StorageOperationStatus> updateComponentInstanceMetadataOfTopologyTemplate(TopologyTemplate container, ToscaElement originToscaElement, ComponentInstance componentInstance) {

		Either<ImmutablePair<TopologyTemplate, String>, StorageOperationStatus> result = null;
		Either<ToscaElement, StorageOperationStatus> updateContainerComponentRes = null;

		String containerComponentId = container.getUniqueId();
		CommonUtility.addRecordToLog(logger, LogLevelEnum.TRACE, "Going to update component instance metadata {} of container component {}", componentInstance, containerComponentId);
		ComponentInstanceDataDefinition componentInstanceData = null;

		Either<GraphVertex, TitanOperationStatus> metadataVertex = titanDao.getVertexById(container.getUniqueId(), JsonParseFlagEnum.ParseMetadata);
		if (metadataVertex.isRight()) {
			TitanOperationStatus status = metadataVertex.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		if (result == null) {
			componentInstanceData = buildComponentInstanceDataDefinition(componentInstance, container.getUniqueId(), componentInstance.getName(), false, originToscaElement);
			container.addComponentInstance(componentInstanceData);
			metadataVertex.left().value().setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, System.currentTimeMillis());
			topologyTemplateOperation.fillToscaElementVertexData(metadataVertex.left().value(), container, JsonParseFlagEnum.ParseAll);
			Either<GraphVertex, TitanOperationStatus> updateElement = titanDao.updateVertex(metadataVertex.left().value());
			if (updateElement.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update topology template {} with new component instance {}. ", container.getName(), componentInstance.getName());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(updateElement.right().value()));
			}
		}
		if (result == null) {
			updateContainerComponentRes = topologyTemplateOperation.getToscaElement(containerComponentId);
			if (updateContainerComponentRes.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to fetch updated topology template {} with updated component instance {}. ", container.getName(), componentInstance.getName());
				result = Either.right(updateContainerComponentRes.right().value());
			}
		}
		if (result == null) {
			result = Either.left(new ImmutablePair<>((TopologyTemplate) updateContainerComponentRes.left().value(), componentInstanceData.getUniqueId()));
		}
		return result;
	}

	public Either<TopologyTemplate, StorageOperationStatus> updateComponentInstanceMetadataOfTopologyTemplate(TopologyTemplate container, ComponentParametersView filter) {

		Either<TopologyTemplate, StorageOperationStatus> result = null;
		Either<ToscaElement, StorageOperationStatus> updateContainerComponentRes = null;

		String containerComponentId = container.getUniqueId();
		CommonUtility.addRecordToLog(logger, LogLevelEnum.TRACE, "Going to update component instance metadata  of container component {}", containerComponentId);

		Either<GraphVertex, TitanOperationStatus> metadataVertex = titanDao.getVertexById(container.getUniqueId(), JsonParseFlagEnum.ParseMetadata);
		if (metadataVertex.isRight()) {
			TitanOperationStatus status = metadataVertex.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		if (result == null) {
			metadataVertex.left().value().setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, System.currentTimeMillis());
			topologyTemplateOperation.fillToscaElementVertexData(metadataVertex.left().value(), container, JsonParseFlagEnum.ParseAll);
			Either<GraphVertex, TitanOperationStatus> updateElement = titanDao.updateVertex(metadataVertex.left().value());
			if (updateElement.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update topology template {}. ", container.getName());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(updateElement.right().value()));
			}
		}
		if (result == null) {
			updateContainerComponentRes = topologyTemplateOperation.getToscaElement(containerComponentId, filter);
			if (updateContainerComponentRes.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to fetch updated topology template {}. ", container.getName());
				result = Either.right(updateContainerComponentRes.right().value());
			}
		}
		if (result == null) {
			result = Either.left((TopologyTemplate) updateContainerComponentRes.left().value());
		}
		return result;
	}

	public Either<ImmutablePair<TopologyTemplate, String>, StorageOperationStatus> deleteComponentInstanceFromTopologyTemplate(TopologyTemplate container, String componentInstanceId) {

		Either<ImmutablePair<TopologyTemplate, String>, StorageOperationStatus> result = null;
		Either<ToscaElement, StorageOperationStatus> updateContainerComponentRes = null;

		String containerComponentId = container.getUniqueId();
		CommonUtility.addRecordToLog(logger, LogLevelEnum.TRACE, "Going to update component instance metadata {} of container component {}", componentInstanceId, containerComponentId);

		Either<GraphVertex, TitanOperationStatus> metadataVertex = titanDao.getVertexById(container.getUniqueId(), JsonParseFlagEnum.ParseMetadata);
		if (metadataVertex.isRight()) {
			TitanOperationStatus status = metadataVertex.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		GraphVertex containerV = null;
		if (result == null) {
			container.getComponentInstances().remove(componentInstanceId);
			containerV = metadataVertex.left().value();
			StorageOperationStatus status = removeRelationsOfInstance(container, componentInstanceId, containerV);
			if (status != StorageOperationStatus.OK) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to delete relation for component instance {} in container. error {}", componentInstanceId, container.getUniqueId(), status);
				result = Either.right(status);
			}

			containerV.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, System.currentTimeMillis());
			topologyTemplateOperation.fillToscaElementVertexData(containerV, container, JsonParseFlagEnum.ParseAll);
			Either<GraphVertex, TitanOperationStatus> updateElement = titanDao.updateVertex(containerV);
			if (updateElement.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update topology template {} with new component instance {}. ", container.getName(), componentInstanceId);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(updateElement.right().value()));
			}
		}
		if (result == null) {
			StorageOperationStatus status = deleteComponentInstanceToscaDataFromContainerComponent(containerV, componentInstanceId);
			if (status != StorageOperationStatus.OK) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to delete data  for instance {} in container {}. error {] ", componentInstanceId, container.getUniqueId(), status);
				return Either.right(status);
			}
			updateContainerComponentRes = topologyTemplateOperation.getToscaElement(containerComponentId);
			if (updateContainerComponentRes.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to fetch updated topology template {} after deleting the component instance {}. ", container.getName(), componentInstanceId);
				result = Either.right(updateContainerComponentRes.right().value());
			}
		}
		if (result == null) {
			result = Either.left(new ImmutablePair<>((TopologyTemplate) updateContainerComponentRes.left().value(), componentInstanceId));
		}
		return result;
	}

	private StorageOperationStatus removeRelationsOfInstance(TopologyTemplate container, String ciToRemove, GraphVertex containerV) {
		CompositionDataDefinition composition = container.getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue());
		if (composition != null) {
			Map<String, RelationshipInstDataDefinition> relations = composition.getRelations();
			if (MapUtils.isNotEmpty(relations)) {
				Either<Pair<GraphVertex, Map<String, MapListCapabiltyDataDefinition>>, StorageOperationStatus> capResult = fetchContainerCalculatedCapability(containerV, EdgeLabelEnum.CALCULATED_CAPABILITIES);
				if (capResult.isRight()) {
					return capResult.right().value();

				}
				Map<String, MapListCapabiltyDataDefinition> calculatedCapabilty = capResult.left().value().getRight();

				Either<Pair<GraphVertex, Map<String, MapListCapabiltyDataDefinition>>, StorageOperationStatus> capFullResult = fetchContainerCalculatedCapability(containerV, EdgeLabelEnum.FULLFILLED_CAPABILITIES);
				if (capFullResult.isRight()) {
					return capFullResult.right().value();

				}
				Map<String, MapListCapabiltyDataDefinition> fullFilledCapabilty = capFullResult.left().value().getRight();

				Either<Pair<GraphVertex, Map<String, MapListRequirementDataDefinition>>, StorageOperationStatus> reqResult = fetchContainerCalculatedRequirement(containerV, EdgeLabelEnum.CALCULATED_REQUIREMENTS);
				if (reqResult.isRight()) {
					return reqResult.right().value();
				}
				Map<String, MapListRequirementDataDefinition> calculatedRequirement = reqResult.left().value().getRight();

				Either<Pair<GraphVertex, Map<String, MapListRequirementDataDefinition>>, StorageOperationStatus> reqFullResult = fetchContainerCalculatedRequirement(containerV, EdgeLabelEnum.FULLFILLED_REQUIREMENTS);
				if (reqResult.isRight()) {
					return reqResult.right().value();
				}
				Map<String, MapListRequirementDataDefinition> fullfilledRequirement = reqFullResult.left().value().getRight();

				Iterator<Entry<String, RelationshipInstDataDefinition>> iterator = relations.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, RelationshipInstDataDefinition> relation = iterator.next();
					RelationshipInstDataDefinition relationToDelete = relation.getValue();
					if (relationToDelete.getFromId().equals(ciToRemove) || relationToDelete.getToId().equals(ciToRemove)) {
						iterator.remove();
						if (relationToDelete.getFromId().equals(ciToRemove)) {
							updateCalculatedRequirementsAfterDeleteRelation(calculatedRequirement, fullfilledRequirement, ciToRemove, relationToDelete);
							updateCalculatedCapabiltyAfterDeleteRelation(calculatedCapabilty, fullFilledCapabilty, relationToDelete.getToId(), relationToDelete);
						}
						if (relationToDelete.getToId().equals(ciToRemove)) {
							updateCalculatedRequirementsAfterDeleteRelation(calculatedRequirement, fullfilledRequirement, relationToDelete.getFromId(), relationToDelete);
							updateCalculatedCapabiltyAfterDeleteRelation(calculatedCapabilty, fullFilledCapabilty, ciToRemove, relationToDelete);
						}
					}
				}
			}
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus deleteComponentInstanceToscaDataFromContainerComponent(GraphVertex containerV, String componentInstanceId) {
		StorageOperationStatus status = deleteToscaDataDeepElementsBlockToToscaElement(containerV, EdgeLabelEnum.CALCULATED_CAPABILITIES, VertexTypeEnum.CALCULATED_CAPABILITIES, componentInstanceId);
		if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to remove calculated capabilty  for instance {} in container {}. error {] ", componentInstanceId, containerV.getUniqueId(), status);
			return status;
		}
		status = deleteToscaDataDeepElementsBlockToToscaElement(containerV, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, VertexTypeEnum.CALCULATED_CAP_PROPERTIES, componentInstanceId);
		if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to remove calculated capabilty properties for instance {} in container {}. error {] ", componentInstanceId, containerV.getUniqueId(), status);
			return status;
		}
		status = deleteToscaDataDeepElementsBlockToToscaElement(containerV, EdgeLabelEnum.CALCULATED_REQUIREMENTS, VertexTypeEnum.CALCULATED_REQUIREMENTS, componentInstanceId);
		if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to remove calculated requirement  for instance {} in container {}. error {] ", componentInstanceId, containerV.getUniqueId(), status);
			return status;
		}
		status = deleteToscaDataDeepElementsBlockToToscaElement(containerV, EdgeLabelEnum.FULLFILLED_CAPABILITIES, VertexTypeEnum.FULLFILLED_CAPABILITIES, componentInstanceId);
		if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to remove fullfilled capabilities  for instance {} in container {}. error {] ", componentInstanceId, containerV.getUniqueId(), status);
			return status;
		}
		status = deleteToscaDataDeepElementsBlockToToscaElement(containerV, EdgeLabelEnum.FULLFILLED_REQUIREMENTS, VertexTypeEnum.FULLFILLED_REQUIREMENTS, componentInstanceId);
		if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to remove fullfilled requirement  for instance {} in container {}. error {] ", componentInstanceId, containerV.getUniqueId(), status);
			return status;
		}
		status = deleteToscaDataDeepElementsBlockToToscaElement(containerV, EdgeLabelEnum.INST_ATTRIBUTES, VertexTypeEnum.INST_ATTRIBUTES, componentInstanceId);
		if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to remove attributes for instance {} in container {}. error {] ", componentInstanceId, containerV.getUniqueId(), status);
			return status;
		}
		status = deleteToscaDataDeepElementsBlockToToscaElement(containerV, EdgeLabelEnum.INST_PROPERTIES, VertexTypeEnum.INST_PROPERTIES, componentInstanceId);
		if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to remove properties for instance {} in container {}. error {] ", componentInstanceId, containerV.getUniqueId(), status);
			return status;
		}
		status = deleteToscaDataDeepElementsBlockToToscaElement(containerV, EdgeLabelEnum.INST_INPUTS, VertexTypeEnum.INST_INPUTS, componentInstanceId);
		if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to remove instance inputs  for instance {} in container {}. error {] ", componentInstanceId, containerV.getUniqueId(), status);
			return status;
		}
		status = deleteToscaDataDeepElementsBlockToToscaElement(containerV, EdgeLabelEnum.INST_GROUPS, VertexTypeEnum.INST_GROUPS, componentInstanceId);
		if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to remove fullfilled requirement  for instance {} in container {}. error {] ", componentInstanceId, containerV.getUniqueId(), status);
			return status;
		}
		status = deleteToscaDataDeepElementsBlockToToscaElement(containerV, EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS, VertexTypeEnum.INST_DEPLOYMENT_ARTIFACTS, componentInstanceId);
		if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to remove instance deployment artifacts  for instance {} in container {}. error {] ", componentInstanceId, containerV.getUniqueId(), status);
			return status;
		}
		status = deleteToscaDataDeepElementsBlockToToscaElement(containerV, EdgeLabelEnum.INSTANCE_ARTIFACTS, VertexTypeEnum.INSTANCE_ARTIFACTS, componentInstanceId);
		if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to remove instance artifacts  for instance {} in container {}. error {] ", componentInstanceId, containerV.getUniqueId(), status);
			return status;
		}
		return StorageOperationStatus.OK;
	}

	protected Either<GraphVertex, StorageOperationStatus> addComponentInstanceToscaDataToContainerComponent(ToscaElement originToscaElement, ComponentInstanceDataDefinition componentInstance, GraphVertex updatedContainerVertex, User user) {

		Either<GraphVertex, StorageOperationStatus> result;
		StorageOperationStatus status;
		if (originToscaElement.getToscaType() == ToscaElementTypeEnum.NodeType) {
			status = addComponentInstanceToscaDataToNodeTypeContainer((NodeType) originToscaElement, componentInstance, updatedContainerVertex, user, HEAT_VF_ENV_NAME);
		} else {
			status = addComponentInstanceToscaDataToTopologyTemplateContainer((TopologyTemplate) originToscaElement, componentInstance, updatedContainerVertex);
		}
		if (status == StorageOperationStatus.OK) {
			result = Either.left(updatedContainerVertex);
		} else {
			result = Either.right(status);
		}
		return result;
	}

	private StorageOperationStatus addComponentInstanceToscaDataToTopologyTemplateContainer(TopologyTemplate originTopologyTemplate, ComponentInstanceDataDefinition componentInstance, GraphVertex updatedContainerVertex) {

		StorageOperationStatus status;

		status = addCalculatedCapReqFromTopologyTemplate(originTopologyTemplate, componentInstance, updatedContainerVertex);

		if (status != StorageOperationStatus.OK) {

			return status;
		}

		MapPropertiesDataDefinition instProperties = new MapPropertiesDataDefinition(originTopologyTemplate.getInputs());

		status = addToscaDataDeepElementsBlockToToscaElement(updatedContainerVertex, EdgeLabelEnum.INST_INPUTS, VertexTypeEnum.INST_INPUTS, instProperties, componentInstance.getUniqueId());
		if (status != StorageOperationStatus.OK) {
			return status;
		}

		return status;
	}

	private StorageOperationStatus addCalculatedCapReqFromTopologyTemplate(TopologyTemplate originTopologyTemplate, ComponentInstanceDataDefinition componentInstance, GraphVertex updatedContainerVertex) {
		Map<String, MapListCapabiltyDataDefinition> calculatedCapabilities = originTopologyTemplate.getCalculatedCapabilities();

		if (calculatedCapabilities != null) {
			MapListCapabiltyDataDefinition allCalculatedCap = new MapListCapabiltyDataDefinition();
			calculatedCapabilities.entrySet().forEach(enntryPerInstance -> {
				Map<String, ListCapabilityDataDefinition> mapByType = enntryPerInstance.getValue().getMapToscaDataDefinition();
				mapByType.entrySet().forEach(entryPerType -> {
					entryPerType.getValue().getListToscaDataDefinition().forEach(cap -> {
						cap.addToPath(componentInstance.getUniqueId());
						allCalculatedCap.add(entryPerType.getKey(), cap);
					});
				});
			});

			StorageOperationStatus calculatedResult = addToscaDataDeepElementsBlockToToscaElement(updatedContainerVertex, EdgeLabelEnum.CALCULATED_CAPABILITIES, VertexTypeEnum.CALCULATED_CAPABILITIES, allCalculatedCap,
					componentInstance.getUniqueId());

			if (calculatedResult != StorageOperationStatus.OK) {
				return calculatedResult;
			}
			MapListCapabiltyDataDefinition fullCalculatedCap = new MapListCapabiltyDataDefinition();
			calculatedResult = addToscaDataDeepElementsBlockToToscaElement(updatedContainerVertex, EdgeLabelEnum.FULLFILLED_CAPABILITIES, VertexTypeEnum.FULLFILLED_CAPABILITIES, fullCalculatedCap, componentInstance.getUniqueId());

			if (calculatedResult != StorageOperationStatus.OK) {
				return calculatedResult;
			}
		}
		Map<String, MapListRequirementDataDefinition> calculatedRequirements = originTopologyTemplate.getCalculatedRequirements();
		if (calculatedRequirements != null) {

			MapListRequirementDataDefinition allCalculatedReq = new MapListRequirementDataDefinition();
			calculatedRequirements.entrySet().forEach(enntryPerInstance -> {
				Map<String, ListRequirementDataDefinition> mapByType = enntryPerInstance.getValue().getMapToscaDataDefinition();
				mapByType.entrySet().forEach(entryPerType -> {
					entryPerType.getValue().getListToscaDataDefinition().forEach(req -> {
						req.addToPath(componentInstance.getUniqueId());
						allCalculatedReq.add(entryPerType.getKey(), req);
					});
				});
			});

			StorageOperationStatus calculatedResult = addToscaDataDeepElementsBlockToToscaElement(updatedContainerVertex, EdgeLabelEnum.CALCULATED_REQUIREMENTS, VertexTypeEnum.CALCULATED_REQUIREMENTS, allCalculatedReq,
					componentInstance.getUniqueId());
			if (calculatedResult != StorageOperationStatus.OK) {
				return calculatedResult;
			}
			MapListRequirementDataDefinition fullCalculatedReq = new MapListRequirementDataDefinition();
			calculatedResult = addToscaDataDeepElementsBlockToToscaElement(updatedContainerVertex, EdgeLabelEnum.FULLFILLED_REQUIREMENTS, VertexTypeEnum.FULLFILLED_REQUIREMENTS, fullCalculatedReq, componentInstance.getUniqueId());
			if (calculatedResult != StorageOperationStatus.OK) {
				return calculatedResult;
			}
		}

		Map<String, MapCapabiltyProperty> calculatedCapabilitiesProperties = originTopologyTemplate.getCalculatedCapabilitiesProperties();
		Map<String, MapPropertiesDataDefinition> updateKeyMap = new HashMap<>();

		if (calculatedCapabilitiesProperties != null && !calculatedCapabilitiesProperties.isEmpty()) {
			for (MapCapabiltyProperty map : calculatedCapabilitiesProperties.values()) {
				for (Entry<String, MapPropertiesDataDefinition> entry : map.getMapToscaDataDefinition().entrySet()) {
					String newKey = new String(componentInstance.getUniqueId() + ModelConverter.CAP_PROP_DELIM + entry.getKey());
					updateKeyMap.put(newKey, entry.getValue());
				}
			}
			MapCapabiltyProperty mapCapabiltyProperty = new MapCapabiltyProperty(updateKeyMap);
			StorageOperationStatus calculatedResult = addToscaDataDeepElementsBlockToToscaElement(updatedContainerVertex, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, VertexTypeEnum.CALCULATED_CAP_PROPERTIES, mapCapabiltyProperty,
					componentInstance.getUniqueId());
			if (calculatedResult != StorageOperationStatus.OK) {
				return calculatedResult;
			}
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus addComponentInstanceToscaDataToNodeTypeContainer(NodeType originNodeType, ComponentInstanceDataDefinition componentInstance, GraphVertex updatedContainerVertex, User user, String envType) {

		MapPropertiesDataDefinition instProperties = new MapPropertiesDataDefinition(originNodeType.getProperties());

		StorageOperationStatus status = addToscaDataDeepElementsBlockToToscaElement(updatedContainerVertex, EdgeLabelEnum.INST_PROPERTIES, VertexTypeEnum.INST_PROPERTIES, instProperties, componentInstance.getUniqueId());
		if (status != StorageOperationStatus.OK) {
			return status;
		}

		MapPropertiesDataDefinition instAttributes = new MapPropertiesDataDefinition(originNodeType.getAttributes());

		status = addToscaDataDeepElementsBlockToToscaElement(updatedContainerVertex, EdgeLabelEnum.INST_ATTRIBUTES, VertexTypeEnum.INST_ATTRIBUTES, instAttributes, componentInstance.getUniqueId());

		if (status != StorageOperationStatus.OK) {
			return status;
		}

		return addCalculatedCapReqFromNodeType(originNodeType, componentInstance, updatedContainerVertex);
	}

	public MapArtifactDataDefinition prepareInstDeploymentArtifactPerInstance(Map<String, ArtifactDataDefinition> deploymentArtifacts, String componentInstanceId, User user, String envType) {
		if (deploymentArtifacts != null && envType.equals(HEAT_VF_ENV_NAME)) {
			Map<String, ArtifactDataDefinition> instDeploymentArtifacts = new HashMap<>();

			deploymentArtifacts.entrySet().forEach(e -> {
				ArtifactDataDefinition artifact = e.getValue();
				String type = artifact.getArtifactType();
				if (type.equalsIgnoreCase(ArtifactTypeEnum.HEAT.getType()) || type.equalsIgnoreCase(ArtifactTypeEnum.HEAT_NET.getType()) || type.equalsIgnoreCase(ArtifactTypeEnum.HEAT_VOL.getType())) {
					ArtifactDataDefinition artifactEnv = createArtifactPlaceHolderInfo(artifact, componentInstanceId, user, envType);
					instDeploymentArtifacts.put(artifactEnv.getArtifactLabel(), artifactEnv);
				}
			});

			deploymentArtifacts.putAll(instDeploymentArtifacts);
			MapArtifactDataDefinition instArtifacts = new MapArtifactDataDefinition(deploymentArtifacts);

			return instArtifacts;
		}
		return null;
	}

	@SuppressWarnings({ "unchecked" })
	private ArtifactDataDefinition createArtifactPlaceHolderInfo(ArtifactDataDefinition artifactHeat, String componentId, User user, String heatEnvType) {
		Map<String, Object> deploymentResourceArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration().getDeploymentResourceInstanceArtifacts();
		if (deploymentResourceArtifacts == null) {
			logger.debug("no deployment artifacts are configured for generated artifacts");
			return null;
		}
		Map<String, Object> placeHolderData = (Map<String, Object>) deploymentResourceArtifacts.get(heatEnvType);
		if (placeHolderData == null) {
			logger.debug("no env type {} are configured for generated artifacts", heatEnvType);
			return null;
		}

		String envLabel = (artifactHeat.getArtifactLabel() + HEAT_ENV_SUFFIX).toLowerCase();

		ArtifactDataDefinition artifactInfo = new ArtifactDataDefinition();

		String artifactName = (String) placeHolderData.get(ARTIFACT_PLACEHOLDER_DISPLAY_NAME);
		String artifactType = (String) placeHolderData.get(ARTIFACT_PLACEHOLDER_TYPE);
		String artifactDescription = (String) placeHolderData.get(ARTIFACT_PLACEHOLDER_DESCRIPTION);

		artifactInfo.setArtifactDisplayName(artifactName);
		artifactInfo.setArtifactLabel(envLabel);
		artifactInfo.setArtifactType(artifactType);
		artifactInfo.setDescription(artifactDescription);
		artifactInfo.setArtifactGroupType(artifactHeat.getArtifactGroupType());
		setDefaultArtifactTimeout(artifactHeat.getArtifactGroupType(), artifactInfo);
		artifactInfo.setGeneratedFromId(artifactHeat.getUniqueId());
		// clone heat parameters in case of heat env only not VF heat env
		if (heatEnvType.equals(HEAT_ENV_NAME)) {
			artifactInfo.setHeatParameters(artifactHeat.getHeatParameters());
		}
		setArtifactPlaceholderCommonFields(componentId, user, artifactInfo);

		return artifactInfo;
	}

	public void setDefaultArtifactTimeout(ArtifactGroupTypeEnum groupType, ArtifactDataDefinition artifactInfo) {
		if (groupType.equals(ArtifactGroupTypeEnum.DEPLOYMENT)) {
			artifactInfo.setTimeout(defaultHeatTimeout);
		} else {
			artifactInfo.setTimeout(NON_HEAT_TIMEOUT);
		}
	}

	private void setArtifactPlaceholderCommonFields(String resourceId, User user, ArtifactDataDefinition artifactInfo) {
		String uniqueId = null;

		if (resourceId != null) {
			uniqueId = UniqueIdBuilder.buildPropertyUniqueId(resourceId.toLowerCase(), artifactInfo.getArtifactLabel().toLowerCase());
			artifactInfo.setUniqueId(uniqueId);
		}
		artifactInfo.setUserIdCreator(user.getUserId());
		String fullName = user.getFullName();
		artifactInfo.setUpdaterFullName(fullName);

		long time = System.currentTimeMillis();

		artifactInfo.setCreatorFullName(fullName);
		artifactInfo.setCreationDate(time);

		artifactInfo.setLastUpdateDate(time);
		artifactInfo.setUserIdLastUpdater(user.getUserId());

		artifactInfo.setMandatory(true);
	}

	/**
	 * 
	 * @param originNodeType
	 * @param componentInstance
	 * @param updatedContainerVertex
	 * @return
	 */
	private StorageOperationStatus addCalculatedCapReqFromNodeType(NodeType originNodeType, ComponentInstanceDataDefinition componentInstance, GraphVertex updatedContainerVertex) {

		Map<String, ListCapabilityDataDefinition> capabilities = originNodeType.getCapabilties();
		MapListCapabiltyDataDefinition allCalculatedCap = prepareCalculatedCapabiltyForNodeType(capabilities, componentInstance);
		StorageOperationStatus calculatedResult;
		if (allCalculatedCap != null) {
			calculatedResult = addToscaDataDeepElementsBlockToToscaElement(updatedContainerVertex, EdgeLabelEnum.CALCULATED_CAPABILITIES, VertexTypeEnum.CALCULATED_CAPABILITIES, allCalculatedCap, componentInstance.getUniqueId());

			if (calculatedResult != StorageOperationStatus.OK) {
				return calculatedResult;
			}
		}
		Map<String, MapPropertiesDataDefinition> capabiltiesProperties = originNodeType.getCapabiltiesProperties();
		if (capabiltiesProperties != null) {
			Map<String, MapPropertiesDataDefinition> updateKeyMap = capabiltiesProperties.entrySet().stream().collect(Collectors.toMap(e -> createCapPropertyKey(e.getKey(), componentInstance.getUniqueId()), e -> e.getValue()));
			MapCapabiltyProperty mapCapabiltyProperty = new MapCapabiltyProperty(updateKeyMap);
			calculatedResult = addToscaDataDeepElementsBlockToToscaElement(updatedContainerVertex, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, VertexTypeEnum.CALCULATED_CAP_PROPERTIES, mapCapabiltyProperty, componentInstance.getUniqueId());
			if (calculatedResult != StorageOperationStatus.OK) {
				return calculatedResult;
			}
		}

		MapListCapabiltyDataDefinition fullCalculatedCap = new MapListCapabiltyDataDefinition();
		calculatedResult = addToscaDataDeepElementsBlockToToscaElement(updatedContainerVertex, EdgeLabelEnum.FULLFILLED_CAPABILITIES, VertexTypeEnum.FULLFILLED_CAPABILITIES, fullCalculatedCap, componentInstance.getUniqueId());

		if (calculatedResult != StorageOperationStatus.OK) {
			return calculatedResult;
		}

		Map<String, ListRequirementDataDefinition> requirements = originNodeType.getRequirements();

		MapListRequirementDataDefinition allCalculatedReq = prepareCalculatedRequirementForNodeType(requirements, componentInstance);

		StorageOperationStatus status;
		if (allCalculatedReq != null) {
			status = addToscaDataDeepElementsBlockToToscaElement(updatedContainerVertex, EdgeLabelEnum.CALCULATED_REQUIREMENTS, VertexTypeEnum.CALCULATED_REQUIREMENTS, allCalculatedReq, componentInstance.getUniqueId());
			if (status != StorageOperationStatus.OK) {
				return status;
			}
		}
		MapListRequirementDataDefinition fullCalculatedReq = new MapListRequirementDataDefinition();
		status = addToscaDataDeepElementsBlockToToscaElement(updatedContainerVertex, EdgeLabelEnum.FULLFILLED_REQUIREMENTS, VertexTypeEnum.FULLFILLED_REQUIREMENTS, fullCalculatedReq, componentInstance.getUniqueId());
		return StorageOperationStatus.OK;

	}

	public static String createCapPropertyKey(String key, String instanceId) {
		StringBuffer sb = new StringBuffer(instanceId);
		sb.append(ModelConverter.CAP_PROP_DELIM).append(instanceId).append(ModelConverter.CAP_PROP_DELIM).append(key);
		return sb.toString();
	}
	
	/**
	 * Prepares a map of capabilities lists
	 * Produces a deep copy of the received map of capabilities
	 * Sets values to the specific fields according to received component instance 
	 * @param capabilities
	 * @param componentInstance
	 * @return
	 */
	public MapListCapabiltyDataDefinition prepareCalculatedCapabiltyForNodeType(Map<String, ListCapabilityDataDefinition> capabilities, ComponentInstanceDataDefinition componentInstance) {
		if (capabilities != null) {
			MapListCapabiltyDataDefinition allCalculatedCap = new MapListCapabiltyDataDefinition();

			capabilities.entrySet().forEach(e -> {
				List<CapabilityDataDefinition> listCapabilities = e.getValue().getListToscaDataDefinition().stream().map(c -> new CapabilityDataDefinition(c)).collect(Collectors.toList());
				listCapabilities.forEach(cap -> {
					cap.setSource(componentInstance.getComponentUid());
					cap.addToPath(componentInstance.getUniqueId());
					cap.setOwnerId(componentInstance.getUniqueId());
					cap.setOwnerName(componentInstance.getName());
					cap.setLeftOccurrences(cap.getMaxOccurrences());
					allCalculatedCap.add(e.getKey(), cap);
				});
			});
			return allCalculatedCap;
		}
		return null;
	}

	/**
	 * Prepares a map of requirements lists
	 * Produces a deep copy of the received map of requirements
	 * Sets values to the specific fields according to received component instance 
	 * @param requirements
	 * @param componentInstance
	 * @return
	 */
	public MapListRequirementDataDefinition prepareCalculatedRequirementForNodeType(Map<String, ListRequirementDataDefinition> requirements, ComponentInstanceDataDefinition componentInstance) {
		if (requirements != null) {
			MapListRequirementDataDefinition allCalculatedReq = new MapListRequirementDataDefinition();

			requirements.entrySet().forEach(e -> {
				List<RequirementDataDefinition> listRequirements = e.getValue().getListToscaDataDefinition().stream().map(r -> new RequirementDataDefinition(r)).collect(Collectors.toList());
				listRequirements.forEach(req -> {
					req.setSource(componentInstance.getComponentUid());
					req.addToPath(componentInstance.getUniqueId());
					req.setOwnerId(componentInstance.getUniqueId());
					req.setOwnerName(componentInstance.getName());
					req.setLeftOccurrences(req.getMaxOccurrences());
					allCalculatedReq.add(e.getKey(), req);
				});
			});
			return allCalculatedReq;
		}
		return null;
	}

	public StorageOperationStatus addGroupInstancesToComponentInstance(Component containerComponent, ComponentInstanceDataDefinition componentInstance, List<GroupDefinition> groups, Map<String, List<ArtifactDefinition>> groupInstancesArtifacts) {

		StorageOperationStatus result = null;
		Map<String, GroupInstanceDataDefinition> groupInstanceToCreate = new HashMap<>();
		if (groupInstancesArtifacts != null && CollectionUtils.isNotEmpty(groups)) {
			for (Map.Entry<String, List<ArtifactDefinition>> groupArtifacts : groupInstancesArtifacts.entrySet()) {
				Optional<GroupDefinition> groupOptional = groups.stream().filter(g -> g.getUniqueId().equals(groupArtifacts.getKey())).findFirst();
				if (groupOptional.isPresent()) {
					GroupInstanceDataDefinition groupInstance = buildGroupInstanceDataDefinition((GroupDataDefinition) groupOptional.get(), (ComponentInstanceDataDefinition) componentInstance, null);
					groupInstance.setGroupInstanceArtifacts(groupArtifacts.getValue().stream().map(a -> a.getUniqueId()).collect(Collectors.toList()));
					groupInstance.setGroupInstanceArtifactsUuid(groupArtifacts.getValue().stream().map(a -> a.getArtifactUUID()).collect(Collectors.toList()));
					groupInstanceToCreate.put(groupInstance.getName(), groupInstance);
				}
			}
		}
		if (MapUtils.isNotEmpty(groupInstanceToCreate)) {
			result = addToscaDataDeepElementsBlockToToscaElement(containerComponent.getUniqueId(), EdgeLabelEnum.INST_GROUPS, VertexTypeEnum.INST_GROUPS, new MapDataDefinition<>(groupInstanceToCreate), componentInstance.getUniqueId());
		}
		if (result == null) {
			result = StorageOperationStatus.OK;
		}
		return result;
	}

	private ComponentInstanceDataDefinition buildComponentInstanceDataDefinition(ComponentInstance resourceInstance, String containerComponentId, String instanceNewName, boolean generateUid, ToscaElement originToscaElement) {
		String ciOriginComponentUid = resourceInstance.getComponentUid();

		if (!ValidationUtils.validateStringNotEmpty(resourceInstance.getCustomizationUUID())) {
			resourceInstance.setCustomizationUUID(generateCustomizationUUID());
		}
		ComponentInstanceDataDefinition dataDefinition = new ComponentInstanceDataDefinition(resourceInstance);

		Long creationDate = resourceInstance.getCreationTime();
		Long modificationTime;
		if (creationDate == null) {
			creationDate = System.currentTimeMillis();
			modificationTime = creationDate;
		} else {
			modificationTime = System.currentTimeMillis();
		}
		dataDefinition.setComponentUid(ciOriginComponentUid);
		dataDefinition.setCreationTime(creationDate);
		dataDefinition.setModificationTime(modificationTime);
		if (StringUtils.isNotEmpty(instanceNewName)) {
			dataDefinition.setName(instanceNewName);
			resourceInstance.setName(instanceNewName);
		}
		if (StringUtils.isNotEmpty(dataDefinition.getName()))
			dataDefinition.setNormalizedName(ValidationUtils.normalizeComponentInstanceName(dataDefinition.getName()));
		dataDefinition.setIcon(resourceInstance.getIcon());
		if (generateUid) {
			dataDefinition.setUniqueId(UniqueIdBuilder.buildResourceInstanceUniuqeId(containerComponentId, ciOriginComponentUid, dataDefinition.getNormalizedName()));
			resourceInstance.setUniqueId(dataDefinition.getUniqueId());
		}
		if (StringUtils.isEmpty(dataDefinition.getComponentVersion()) && originToscaElement != null)
			dataDefinition.setComponentVersion((String) originToscaElement.getMetadataValue(JsonPresentationFields.VERSION));
		if (StringUtils.isEmpty(dataDefinition.getComponentName()) && originToscaElement != null)
			dataDefinition.setComponentName((String) originToscaElement.getMetadataValue(JsonPresentationFields.NAME));
		if (originToscaElement != null && dataDefinition.getToscaComponentName() == null)
			dataDefinition.setToscaComponentName((String) originToscaElement.getMetadataValue(JsonPresentationFields.TOSCA_RESOURCE_NAME));
		if (dataDefinition.getOriginType() == null && originToscaElement != null) {
			ResourceTypeEnum resourceType = originToscaElement.getResourceType();
			OriginTypeEnum originType = null;
			switch (resourceType) {
			case VF:
				originType = OriginTypeEnum.VF;
				break;
			case VFC:
				originType = OriginTypeEnum.VFC;
				break;
			case CVFC:
				originType = OriginTypeEnum.CVFC;
				break;
			case VL:
				originType = OriginTypeEnum.VL;
				break;
			case CP:
				originType = OriginTypeEnum.CP;
				break;
			case PNF:
				originType = OriginTypeEnum.PNF;
				break;
			case ServiceProxy:
				originType = OriginTypeEnum.ServiceProxy;
				break;
			case Configuration:
				originType = OriginTypeEnum.Configuration;
				break;
			default:
				break;
			}
			dataDefinition.setOriginType(originType);
		}
		if(dataDefinition.getOriginType()  == OriginTypeEnum.ServiceProxy)
			dataDefinition.setIsProxy(true);
	
		return dataDefinition;
	}

	private Boolean isUniqueInstanceName(TopologyTemplate container, String instanceName) {
		Boolean isUniqueName = true;
		try {
			isUniqueName = !container.getComponentInstances().values().stream().filter(ci -> ci.getName() != null && ci.getName().equals(instanceName)).findAny().isPresent();

		} catch (Exception e) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during fetching component instance with name {} from component container {}. {} ", instanceName, container.getName(), e.getMessage());
		}
		return isUniqueName;
	}

	private String buildComponentInstanceName(String instanceSuffixNumber, String instanceName) {
		return instanceName + " " + (instanceSuffixNumber == null ? 0 : instanceSuffixNumber);
	}

	public Either<RequirementCapabilityRelDef, StorageOperationStatus> associateResourceInstances(String componentId, RequirementCapabilityRelDef relation) {
		List<RequirementCapabilityRelDef> relations = new ArrayList<>();
		relations.add(relation);
		Either<List<RequirementCapabilityRelDef>, StorageOperationStatus> associateResourceInstances = associateResourceInstances(componentId, relations);
		if (associateResourceInstances.isRight()) {
			return Either.right(associateResourceInstances.right().value());
		}
		return Either.left(associateResourceInstances.left().value().get(0));
	}

	@SuppressWarnings({ "unchecked" })
	public Either<List<RequirementCapabilityRelDef>, StorageOperationStatus> associateResourceInstances(String componentId, List<RequirementCapabilityRelDef> relations) {

		Either<GraphVertex, TitanOperationStatus> containerVEither = titanDao.getVertexById(componentId, JsonParseFlagEnum.ParseAll);
		if (containerVEither.isRight()) {
			TitanOperationStatus error = containerVEither.right().value();
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to fetch container vertex {} error {}", componentId, error);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(error));
		}
		GraphVertex containerV = containerVEither.left().value();
		List<RequirementAndRelationshipPair> relationshipsResult = new ArrayList<RequirementAndRelationshipPair>();
		Either<Pair<GraphVertex, Map<String, MapListCapabiltyDataDefinition>>, StorageOperationStatus> capResult = fetchContainerCalculatedCapability(containerV, EdgeLabelEnum.CALCULATED_CAPABILITIES);
		if (capResult.isRight()) {
			return Either.right(capResult.right().value());

		}
		Map<String, MapListCapabiltyDataDefinition> calculatedCapabilty = capResult.left().value().getRight();

		Either<Pair<GraphVertex, Map<String, MapListCapabiltyDataDefinition>>, StorageOperationStatus> capFullResult = fetchContainerCalculatedCapability(containerV, EdgeLabelEnum.FULLFILLED_CAPABILITIES);
		if (capResult.isRight()) {
			return Either.right(capResult.right().value());

		}
		Map<String, MapListCapabiltyDataDefinition> fullFilledCapabilty = capFullResult.left().value().getRight();

		Either<Pair<GraphVertex, Map<String, MapListRequirementDataDefinition>>, StorageOperationStatus> reqResult = fetchContainerCalculatedRequirement(containerV, EdgeLabelEnum.CALCULATED_REQUIREMENTS);
		if (reqResult.isRight()) {
			return Either.right(reqResult.right().value());
		}
		Map<String, MapListRequirementDataDefinition> calculatedRequirement = reqResult.left().value().getRight();

		Either<Pair<GraphVertex, Map<String, MapListRequirementDataDefinition>>, StorageOperationStatus> reqFullResult = fetchContainerCalculatedRequirement(containerV, EdgeLabelEnum.FULLFILLED_REQUIREMENTS);
		if (reqResult.isRight()) {
			return Either.right(reqResult.right().value());
		}
		Map<String, MapListRequirementDataDefinition> fullfilledRequirement = reqFullResult.left().value().getRight();

		Map<String, CompositionDataDefinition> jsonComposition = (Map<String, CompositionDataDefinition>) containerV.getJson();
		CompositionDataDefinition compositionDataDefinition = jsonComposition.get(JsonConstantKeysEnum.COMPOSITION.getValue());

		StorageOperationStatus status;
		List<RequirementCapabilityRelDef> relationsList = new ArrayList<>();
		for (RequirementCapabilityRelDef relation : relations) {

			String fromNode = relation.getFromNode();
			String toNode = relation.getToNode();
			List<RequirementAndRelationshipPair> relationships = relation.getRelationships();
			if (relationships == null || relationships.isEmpty()) {
				BeEcompErrorManager.getInstance().logBeFailedAddingResourceInstanceError("AssociateResourceInstances - missing relationship", fromNode, componentId);
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "No requirement definition sent in order to set the relation between {} to {}", fromNode, toNode);
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.ILLEGAL_ARGUMENT));
			}

			for (RequirementAndRelationshipPair immutablePair : relationships) {
				String requirement = immutablePair.getRequirement();

				Either<RelationshipInstDataDefinition, StorageOperationStatus> associateRes = connectInstancesInContainer(fromNode, toNode, immutablePair, calculatedCapabilty, calculatedRequirement, fullFilledCapabilty, fullfilledRequirement,
						compositionDataDefinition, containerV.getUniqueId());

				if (associateRes.isRight()) {
					status = associateRes.right().value();
					BeEcompErrorManager.getInstance().logBeFailedAddingResourceInstanceError("AssociateResourceInstances - missing relationship", fromNode, componentId);
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to associate resource instance {} to resource instance {}. status is {}", fromNode, toNode, status);
					return Either.right(status);
				}

				RelationshipInstDataDefinition relationshipInstData = associateRes.left().value();
				RelationshipImpl relationshipImplResult = new RelationshipImpl();
				relationshipImplResult.setType(relationshipInstData.getType());
				RequirementAndRelationshipPair requirementAndRelationshipPair = new RequirementAndRelationshipPair(requirement, relationshipImplResult);
				requirementAndRelationshipPair.setCapability(immutablePair.getCapability());
				requirementAndRelationshipPair.setCapabilityOwnerId(relationshipInstData.getCapabilityOwnerId());
				requirementAndRelationshipPair.setRequirementOwnerId(relationshipInstData.getRequirementOwnerId());
				requirementAndRelationshipPair.setCapabilityUid(immutablePair.getCapabilityUid());
				requirementAndRelationshipPair.setRequirementUid(immutablePair.getRequirementUid());
				relationshipsResult.add(requirementAndRelationshipPair);
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "update customization UUID for from CI {} and to CI {}", relation.getFromNode(), relation.getToNode());
				status = updateCustomizationUUID(relation.getFromNode(), compositionDataDefinition);
				if (status != StorageOperationStatus.OK) {
					return Either.right(status);
				}
				status = updateCustomizationUUID(relation.getToNode(), compositionDataDefinition);
				if (status != StorageOperationStatus.OK) {
					return Either.right(status);
				}
			}
			RequirementCapabilityRelDef capabilityRelDef = new RequirementCapabilityRelDef();
			capabilityRelDef.setFromNode(fromNode);
			capabilityRelDef.setToNode(toNode);
			capabilityRelDef.setRelationships(relationshipsResult);
			relationsList.add(capabilityRelDef);
		}
		// update metadata of container and composition json
		status = updateAllAndCalculatedCapReqOnGraph(componentId, containerV, capResult, capFullResult, reqResult, reqFullResult);
		if (status != StorageOperationStatus.OK) {
			return Either.right(status);
		}

		return Either.left(relationsList);
	}

	private StorageOperationStatus updateAllAndCalculatedCapReqOnGraph(String componentId, GraphVertex containerV, Either<Pair<GraphVertex, Map<String, MapListCapabiltyDataDefinition>>, StorageOperationStatus> capResult,
			Either<Pair<GraphVertex, Map<String, MapListCapabiltyDataDefinition>>, StorageOperationStatus> capFullResult, Either<Pair<GraphVertex, Map<String, MapListRequirementDataDefinition>>, StorageOperationStatus> reqResult,
			Either<Pair<GraphVertex, Map<String, MapListRequirementDataDefinition>>, StorageOperationStatus> reqFullResult) {
		containerV.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, System.currentTimeMillis());
		Either<GraphVertex, TitanOperationStatus> updateElement = titanDao.updateVertex(containerV);
		if (updateElement.isRight()) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update topology template {} with new relations error {}. ", componentId, updateElement.right().value());
			return DaoStatusConverter.convertTitanStatusToStorageStatus(updateElement.right().value());
		}
		// update cap/req jsons, fulfilled cap/req jsons!!!!!
		Either<GraphVertex, TitanOperationStatus> status;
		CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Update calculated capabilty for container {}", containerV.getUniqueId());
		status = updateOrCopyOnUpdate(capResult.left().value().getLeft(), containerV, EdgeLabelEnum.CALCULATED_CAPABILITIES);
		if (status.isRight()) {
			TitanOperationStatus error = status.right().value();
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update calculated capabilty for container {} error {}", containerV.getUniqueId(), error);
			return DaoStatusConverter.convertTitanStatusToStorageStatus(error);
		}

		CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Update calculated requirement for container {}", containerV.getUniqueId());
		status = updateOrCopyOnUpdate(reqResult.left().value().getLeft(), containerV, EdgeLabelEnum.CALCULATED_REQUIREMENTS);
		if (status.isRight()) {
			TitanOperationStatus error = status.right().value();
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update calculated requiremnt for container {} error {}", containerV.getUniqueId(), error);
			return DaoStatusConverter.convertTitanStatusToStorageStatus(error);
		}

		CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Update fullfilled capabilty for container {}", containerV.getUniqueId());
		status = updateOrCopyOnUpdate(capFullResult.left().value().getLeft(), containerV, EdgeLabelEnum.FULLFILLED_CAPABILITIES);
		if (status.isRight()) {
			TitanOperationStatus error = status.right().value();
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update fullfilled capabilty for container {} error {}", containerV.getUniqueId(), error);
			return DaoStatusConverter.convertTitanStatusToStorageStatus(error);
		}

		CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Update fullfilled requirement for container {}", containerV.getUniqueId());
		status = updateOrCopyOnUpdate(reqFullResult.left().value().getLeft(), containerV, EdgeLabelEnum.FULLFILLED_REQUIREMENTS);
		if (status.isRight()) {
			TitanOperationStatus error = status.right().value();
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update fullfilled requirement for container {} error {}", containerV.getUniqueId(), error);
			return DaoStatusConverter.convertTitanStatusToStorageStatus(error);
		}
		return StorageOperationStatus.OK;
	}

	@SuppressWarnings({ "unchecked" })
	public Either<RequirementCapabilityRelDef, StorageOperationStatus> dissociateResourceInstances(String componentId, RequirementCapabilityRelDef requirementDef) {
		if (requirementDef.getRelationships() == null) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "No relation pair in request [ {} ]", requirementDef);
			return Either.right(StorageOperationStatus.BAD_REQUEST);
		}

		String fromResInstanceUid = requirementDef.getFromNode();
		String toResInstanceUid = requirementDef.getToNode();

		Either<GraphVertex, TitanOperationStatus> containerVEither = titanDao.getVertexById(componentId, JsonParseFlagEnum.ParseAll);
		if (containerVEither.isRight()) {
			TitanOperationStatus error = containerVEither.right().value();
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to fetch container vertex {} error {}", componentId, error);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(error));
		}
		GraphVertex containerV = containerVEither.left().value();

		// DE191707 - validations
		Map<String, CompositionDataDefinition> jsonComposition = (Map<String, CompositionDataDefinition>) containerV.getJson();
		CompositionDataDefinition compositionDataDefinition = jsonComposition.get(JsonConstantKeysEnum.COMPOSITION.getValue());
		Map<String, ComponentInstanceDataDefinition> componentInstances = compositionDataDefinition.getComponentInstances();
		ComponentInstanceDataDefinition ciFrom = componentInstances.get(fromResInstanceUid);
		if (ciFrom == null) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "FROM instance {} isn't under container {}", fromResInstanceUid, componentId);
			return Either.right(StorageOperationStatus.NOT_FOUND);

		}
		ComponentInstanceDataDefinition ciTo = componentInstances.get(toResInstanceUid);
		if (ciFrom == ciTo) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "TO instance {} isn't under container {}", toResInstanceUid, componentId);
			return Either.right(StorageOperationStatus.NOT_FOUND);

		}
		Map<String, RelationshipInstDataDefinition> relations = compositionDataDefinition.getRelations();

		List<RequirementAndRelationshipPair> relationPairList = requirementDef.getRelationships();
		Either<Pair<GraphVertex, Map<String, MapListCapabiltyDataDefinition>>, StorageOperationStatus> capResult = fetchContainerCalculatedCapability(containerV, EdgeLabelEnum.CALCULATED_CAPABILITIES);
		if (capResult.isRight()) {
			return Either.right(capResult.right().value());
		}
		Map<String, MapListCapabiltyDataDefinition> calculatedCapability = capResult.left().value().getRight();

		Either<Pair<GraphVertex, Map<String, MapListCapabiltyDataDefinition>>, StorageOperationStatus> capFullResult = fetchContainerCalculatedCapability(containerV, EdgeLabelEnum.FULLFILLED_CAPABILITIES);
		if (capResult.isRight()) {
			return Either.right(capResult.right().value());

		}
		Map<String, MapListCapabiltyDataDefinition> fulfilledCapability = capFullResult.left().value().getRight();

		Either<Pair<GraphVertex, Map<String, MapListRequirementDataDefinition>>, StorageOperationStatus> reqResult = fetchContainerCalculatedRequirement(containerV, EdgeLabelEnum.CALCULATED_REQUIREMENTS);
		if (reqResult.isRight()) {
			return Either.right(reqResult.right().value());
		}
		Map<String, MapListRequirementDataDefinition> calculatedRequirement = reqResult.left().value().getRight();

		Either<Pair<GraphVertex, Map<String, MapListRequirementDataDefinition>>, StorageOperationStatus> reqFullResult = fetchContainerCalculatedRequirement(containerV, EdgeLabelEnum.FULLFILLED_REQUIREMENTS);
		if (reqResult.isRight()) {
			return Either.right(reqResult.right().value());
		}
		Map<String, MapListRequirementDataDefinition> fulfilledRequirement = reqFullResult.left().value().getRight();

		for (RequirementAndRelationshipPair relationPair : relationPairList) {
			Iterator<Entry<String, RelationshipInstDataDefinition>> iterator = relations.entrySet().iterator();
			boolean isDeleted = false;
			while (iterator.hasNext()) {
				Entry<String, RelationshipInstDataDefinition> entryInJson = iterator.next();
				RelationshipInstDataDefinition relationInJson = entryInJson.getValue();
				if (relationInJson.getFromId().equals(fromResInstanceUid) && relationInJson.getToId().equals(toResInstanceUid)) {
					if (relationPair.equalsTo(relationInJson)) {
						CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Remove relation from {} to {} capability {} capOwnerId {} reqOwnerId {} ", toResInstanceUid, componentId, relationInJson.getType(),
								relationInJson.getCapabilityOwnerId(), relationInJson.getRequirementOwnerId());
						iterator.remove();

						// update calculated cap/req
						StorageOperationStatus status = updateCalculatedCapabiltyAfterDeleteRelation(calculatedCapability, fulfilledCapability, toResInstanceUid, relationInJson);
						if (status != StorageOperationStatus.OK) {
							return Either.right(status);
						}
						status = updateCalculatedRequirementsAfterDeleteRelation(calculatedRequirement, fulfilledRequirement, fromResInstanceUid, relationInJson);
						if (status != StorageOperationStatus.OK) {
							return Either.right(status);
						}
						isDeleted = true;
					}
				}
			}
			if (isDeleted == false) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "No relation to delete from {} to {} capabilty {} capOwnerId {} reqOwnerId {} ", toResInstanceUid, componentId, relationPair.getCapability(),
						relationPair.getCapabilityOwnerId(), relationPair.getRequirementOwnerId());
				return Either.right(StorageOperationStatus.NOT_FOUND);
			}
		}
		StorageOperationStatus status = updateCustomizationUUID(fromResInstanceUid, compositionDataDefinition);
		if (status != StorageOperationStatus.OK) {
			return Either.right(status);
		}
		status = updateCustomizationUUID(toResInstanceUid, compositionDataDefinition);
		if (status != StorageOperationStatus.OK) {
			return Either.right(status);
		}

		// update jsons
		// update metadata of container and composition json
		status = updateAllAndCalculatedCapReqOnGraph(componentId, containerV, capResult, capFullResult, reqResult, reqFullResult);
		if (status != StorageOperationStatus.OK) {
			return Either.right(status);
		}

		return Either.left(requirementDef);
	}

	private StorageOperationStatus updateCalculatedRequirementsAfterDeleteRelation(Map<String, MapListRequirementDataDefinition> calculatedRequirement, Map<String, MapListRequirementDataDefinition> fullFilledRequirement, String fromResInstanceUid,
			RelationshipInstDataDefinition relation) {
		StorageOperationStatus status;
		String hereIsTheKey = null;
		MapListRequirementDataDefinition reqByInstance = calculatedRequirement.get(fromResInstanceUid);
		if (reqByInstance == null || reqByInstance.findKeyByItemUidMatch(relation.getRequirementId()) == null) {
			// move from fulfilled
			status = moveFromFullFilledRequirement(calculatedRequirement, fullFilledRequirement, fromResInstanceUid, relation, hereIsTheKey);
		} else {
			hereIsTheKey = reqByInstance.findKeyByItemUidMatch(relation.getRequirementId());
			ListRequirementDataDefinition reqByType = reqByInstance.findByKey(hereIsTheKey);
			Optional<RequirementDataDefinition> requirementOptional = reqByType.getListToscaDataDefinition().stream()
					.filter(req -> req.getOwnerId().equals(relation.getRequirementOwnerId()) && req.getName().equals(relation.getRequirement()) && req.getUniqueId().equals(relation.getRequirementId())).findFirst();

			if (requirementOptional.isPresent()) {

				RequirementDataDefinition requirement = requirementOptional.get();
				String leftOccurrences = requirement.getLeftOccurrences();
				if (leftOccurrences != null && !leftOccurrences.equals(RequirementDataDefinition.MAX_OCCURRENCES)) {
					Integer leftIntValue = Integer.parseInt(leftOccurrences);
					++leftIntValue;
					requirement.setLeftOccurrences(String.valueOf(leftIntValue));
				}
				status = StorageOperationStatus.OK;
			} else {
				// move from fulfilled
				status = moveFromFullFilledRequirement(calculatedRequirement, fullFilledRequirement, fromResInstanceUid, relation, hereIsTheKey);
			}
		}
		return status;
	}

	private StorageOperationStatus updateCalculatedCapabiltyAfterDeleteRelation(Map<String, MapListCapabiltyDataDefinition> calculatedCapability, Map<String, MapListCapabiltyDataDefinition> fullFilledCapability, String toResInstanceUid,
			RelationshipInstDataDefinition relation) {
		StorageOperationStatus status;
		String hereIsTheKey = null;
		MapListCapabiltyDataDefinition capByInstance = calculatedCapability.get(toResInstanceUid);
		if (capByInstance == null || capByInstance.findKeyByItemUidMatch(relation.getCapabilityId()) == null) {
			// move from fulfilled
			status = moveFromFullFilledCapabilty(calculatedCapability, fullFilledCapability, toResInstanceUid, relation, hereIsTheKey);
		} else {
			hereIsTheKey = capByInstance.findKeyByItemUidMatch(relation.getCapabilityId());
			ListCapabilityDataDefinition capByType = capByInstance.findByKey(hereIsTheKey);
			Optional<CapabilityDataDefinition> capabilityOptional = capByType.getListToscaDataDefinition().stream().filter(cap -> cap.getOwnerId().equals(relation.getCapabilityOwnerId()) && cap.getUniqueId().equals(relation.getCapabilityId()))
					.findFirst();

			if (capabilityOptional.isPresent()) {

				CapabilityDataDefinition capability = capabilityOptional.get();
				String leftOccurrences = capability.getLeftOccurrences();
				if (leftOccurrences != null && !leftOccurrences.equals(CapabilityDataDefinition.MAX_OCCURRENCES)) {
					Integer leftIntValue = Integer.parseInt(leftOccurrences);
					++leftIntValue;
					capability.setLeftOccurrences(String.valueOf(leftIntValue));
				}
				status = StorageOperationStatus.OK;
			} else {
				// move from fulfilled
				status = moveFromFullFilledCapabilty(calculatedCapability, fullFilledCapability, toResInstanceUid, relation, hereIsTheKey);
			}
		}
		return status;
	}

	private StorageOperationStatus moveFromFullFilledCapabilty(Map<String, MapListCapabiltyDataDefinition> calculatedCapability, Map<String, MapListCapabiltyDataDefinition> fullFilledCapability, String toResInstanceUid,
			RelationshipInstDataDefinition relation, String hereIsTheKey) {
		MapListCapabiltyDataDefinition capByInstance = fullFilledCapability.get(toResInstanceUid);
		if (capByInstance == null) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "No capability in fulfilled list for instance {} ", toResInstanceUid);
			return StorageOperationStatus.GENERAL_ERROR;
		}
		if (null == hereIsTheKey)
			hereIsTheKey = capByInstance.findKeyByItemUidMatch(relation.getCapabilityId());
		if (null == hereIsTheKey) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "No capability with id {} in fulfilled list for instance {} ", relation.getCapabilityId(), toResInstanceUid);
			return StorageOperationStatus.GENERAL_ERROR;
		}
		ListCapabilityDataDefinition capByType = capByInstance.findByKey(hereIsTheKey);
		Iterator<CapabilityDataDefinition> iterator = capByType.getListToscaDataDefinition().iterator();
		boolean found = false;
		while (iterator.hasNext()) {
			CapabilityDataDefinition cap = iterator.next();
			if (cap.getOwnerId().equals(relation.getCapabilityOwnerId()) && cap.getUniqueId().equals(relation.getCapabilityId())) {
				found = true;
				iterator.remove();
				// return to calculated list
				String leftOccurrences = cap.getLeftOccurrences();
				Integer leftIntValue = Integer.parseInt(leftOccurrences);
				++leftIntValue;
				cap.setLeftOccurrences(String.valueOf(leftIntValue));

				MapListCapabiltyDataDefinition mapListCapaDataDef = calculatedCapability.get(toResInstanceUid);
				if (mapListCapaDataDef == null) {
					mapListCapaDataDef = new MapListCapabiltyDataDefinition();
				}
				ListCapabilityDataDefinition findByKey = mapListCapaDataDef.findByKey(hereIsTheKey);
				if (findByKey == null) {
					findByKey = new ListCapabilityDataDefinition();
					mapListCapaDataDef.put(hereIsTheKey, findByKey);
				}
				findByKey.add(cap);
				break;
			}
		}
		if (found == false) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "No capability type {} with ownerId {} in fulfilled list for instance {} ", hereIsTheKey, relation.getCapabilityOwnerId(), toResInstanceUid);
			return StorageOperationStatus.GENERAL_ERROR;
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus moveFromFullFilledRequirement(Map<String, MapListRequirementDataDefinition> calculatedRequirement, Map<String, MapListRequirementDataDefinition> fullFilledRequirement, String fromResInstanceUid,
			RelationshipInstDataDefinition relation, String hereIsTheKey) {
		MapListRequirementDataDefinition reqByInstance = fullFilledRequirement.get(fromResInstanceUid);
		if (reqByInstance == null) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "No requirement in fullfilled list for instance {} ", fromResInstanceUid);
			return StorageOperationStatus.GENERAL_ERROR;
		}
		if (null == hereIsTheKey)
			hereIsTheKey = reqByInstance.findKeyByItemUidMatch(relation.getRequirementId());
		if (null == hereIsTheKey) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "No requirement with id {} in fulfilled list for instance {} ", relation.getRequirementId(), fromResInstanceUid);
			return StorageOperationStatus.GENERAL_ERROR;
		}
		ListRequirementDataDefinition reqByType = reqByInstance.findByKey(hereIsTheKey);
		Iterator<RequirementDataDefinition> iterator = reqByType.getListToscaDataDefinition().iterator();
		boolean found = false;
		while (iterator.hasNext()) {
			RequirementDataDefinition req = iterator.next();
			if (req.getOwnerId().equals(relation.getRequirementOwnerId()) && req.getName().equals(relation.getRequirement()) && req.getUniqueId().equals(relation.getRequirementId())) {
				found = true;
				iterator.remove();
				// return to calculated list
				String leftOccurrences = req.getLeftOccurrences();
				Integer leftIntValue = Integer.parseInt(leftOccurrences);
				++leftIntValue;
				req.setLeftOccurrences(String.valueOf(leftIntValue));

				MapListRequirementDataDefinition mapListReqDataDef = calculatedRequirement.get(fromResInstanceUid);
				if (mapListReqDataDef == null) {
					mapListReqDataDef = new MapListRequirementDataDefinition();
				}
				ListRequirementDataDefinition findByKey = mapListReqDataDef.findByKey(hereIsTheKey);
				if (findByKey == null) {
					findByKey = new ListRequirementDataDefinition();
					mapListReqDataDef.put(hereIsTheKey, findByKey);
				}
				findByKey.add(req);
				break;
			}
		}
		if (found == false) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "No requirement type {} with ownerId {} in fulfilled list for instance {} ", hereIsTheKey, relation.getRequirementOwnerId(), fromResInstanceUid);
			return StorageOperationStatus.GENERAL_ERROR;
		}
		return StorageOperationStatus.OK;

	}

	public StorageOperationStatus updateCustomizationUUID(String componentInstanceId, CompositionDataDefinition compositionDataDefinition) {
		ComponentInstanceDataDefinition componentInstance = compositionDataDefinition.getComponentInstances().get(componentInstanceId);

		if (componentInstance == null) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to fetch component instance by id {} from map of instances ", componentInstanceId);
			return StorageOperationStatus.NOT_FOUND;
		}
		UUID uuid = UUID.randomUUID();
		componentInstance.setCustomizationUUID(uuid.toString());

		return StorageOperationStatus.OK;
	}

	public Either<RelationshipInstDataDefinition, StorageOperationStatus> connectInstancesInContainer(String fromResInstanceUid, String toResInstanceUid, RequirementAndRelationshipPair relationPair,
			Map<String, MapListCapabiltyDataDefinition> calculatedCapabilty, Map<String, MapListRequirementDataDefinition> calculatedRequirement, Map<String, MapListCapabiltyDataDefinition> fullfilledCapabilty,
			Map<String, MapListRequirementDataDefinition> fullfilledRequirement, CompositionDataDefinition compositionDataDefinition, String containerId) {
		String requirement = relationPair.getRequirement();
		Map<String, ComponentInstanceDataDefinition> componentInstances = compositionDataDefinition.getComponentInstances();

		CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Going to associate resource instance {} to resource instance {} under component {}. Requirement is {}.", fromResInstanceUid, toResInstanceUid, containerId, requirement);

		ComponentInstanceDataDefinition fromResourceInstData = componentInstances.get(fromResInstanceUid);
		if (fromResourceInstData == null) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to find from resource instance {}.", fromResInstanceUid);
			return Either.right(StorageOperationStatus.NOT_FOUND);
		}
		ComponentInstanceDataDefinition toResourceInstData = componentInstances.get(toResInstanceUid);
		if (toResourceInstData == null) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to find to resource instance {}.", toResInstanceUid);
			return Either.right(StorageOperationStatus.NOT_FOUND);
		}

		Either<RelationshipInstDataDefinition, StorageOperationStatus> reqVsCap = connectRequirementVsCapability(fromResourceInstData, toResourceInstData, relationPair, calculatedCapabilty, calculatedRequirement, fullfilledCapabilty,
				fullfilledRequirement, containerId);
		if (reqVsCap.isRight()) {
			StorageOperationStatus status = reqVsCap.right().value();
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to connect requirement {} between resource instance {} to resource instance {}. status is {}", requirement, fromResInstanceUid, toResInstanceUid, status);
			return Either.right(status);
		}
		RelationshipInstDataDefinition relation = reqVsCap.left().value();

		// add to json new relations
		compositionDataDefinition.addRelation(relation.getUniqueId(), relation);

		return Either.left(relation);
	}

	private Either<Pair<GraphVertex, Map<String, MapListCapabiltyDataDefinition>>, StorageOperationStatus> fetchContainerCalculatedCapability(GraphVertex containerV, EdgeLabelEnum capLabel) {

		Either<Pair<GraphVertex, Map<String, MapListCapabiltyDataDefinition>>, TitanOperationStatus> calculatedCapabiltyEither = getDataAndVertexFromGraph(containerV, capLabel);
		if (calculatedCapabiltyEither.isRight()) {
			TitanOperationStatus error = calculatedCapabiltyEither.right().value();
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to fetch calculated capabilties for container {}.", containerV.getUniqueId(), error);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(error));
		}
		Pair<GraphVertex, Map<String, MapListCapabiltyDataDefinition>> calculatedCapabilty = calculatedCapabiltyEither.left().value();
		return Either.left(calculatedCapabilty);
	}

	private Either<Pair<GraphVertex, Map<String, MapListRequirementDataDefinition>>, StorageOperationStatus> fetchContainerCalculatedRequirement(GraphVertex containerV, EdgeLabelEnum reqLabel) {
		Either<Pair<GraphVertex, Map<String, MapListRequirementDataDefinition>>, TitanOperationStatus> calculatedRequirementEither = getDataAndVertexFromGraph(containerV, reqLabel);
		if (calculatedRequirementEither.isRight()) {
			TitanOperationStatus error = calculatedRequirementEither.right().value();
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to fetch calculated requirements for container {}.", containerV.getUniqueId(), error);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(error));
		}
		Pair<GraphVertex, Map<String, MapListRequirementDataDefinition>> calculatedRequirement = calculatedRequirementEither.left().value();
		return Either.left(calculatedRequirement);
	}

	private Either<RelationshipInstDataDefinition, StorageOperationStatus> connectRequirementVsCapability(ComponentInstanceDataDefinition fromResInstance, ComponentInstanceDataDefinition toResInstance, RequirementAndRelationshipPair relationPair,
			Map<String, MapListCapabiltyDataDefinition> calculatedCapabilty, Map<String, MapListRequirementDataDefinition> calculatedRequirement, Map<String, MapListCapabiltyDataDefinition> fullfilledCapabilty,
			Map<String, MapListRequirementDataDefinition> fullfilledRequirement, String containerId) {
		String type = relationPair.getRelationship().getType();
		// capability

		String toInstId = toResInstance.getUniqueId();
		MapListCapabiltyDataDefinition mapListCapabiltyDataDefinition = calculatedCapabilty.get(toInstId);

		if (mapListCapabiltyDataDefinition == null) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to fetch calculated capabilities for instance {} in container {}.", toInstId, containerId);
			return Either.right(StorageOperationStatus.MATCH_NOT_FOUND);
		}
		ListCapabilityDataDefinition listCapabilityDataDefinition = mapListCapabiltyDataDefinition.getMapToscaDataDefinition().get(type);
		if (listCapabilityDataDefinition == null) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to fetch calculated capabilities for type {} for instance {} in container {}.", type, toInstId, containerId);
			return Either.right(StorageOperationStatus.MATCH_NOT_FOUND);
		}
		CapabilityDataDefinition capabilityForRelation = null;
		Iterator<CapabilityDataDefinition> iteratorCap = listCapabilityDataDefinition.getListToscaDataDefinition().iterator();
		while (iteratorCap.hasNext()) {
			CapabilityDataDefinition cap = iteratorCap.next();
			if (cap.getUniqueId().equals(relationPair.getCapabilityUid()) && cap.getOwnerId().equals(relationPair.getCapabilityOwnerId())) {
				capabilityForRelation = cap;
				String leftOccurrences = cap.getLeftOccurrences();
				if (leftOccurrences != null && !leftOccurrences.equals(CapabilityDataDefinition.MAX_OCCURRENCES)) {
					Integer leftIntValue = Integer.parseInt(leftOccurrences);
					if (leftIntValue > 0) {
						--leftIntValue;
						capabilityForRelation.setLeftOccurrences(String.valueOf(leftIntValue));
						if (leftIntValue == 0) {
							// remove from calculated
							iteratorCap.remove();
							// move to fulfilled
							MapListCapabiltyDataDefinition mapListCapabiltyFullFilledInst = fullfilledCapabilty.get(toInstId);
							if (mapListCapabiltyFullFilledInst == null) {
								mapListCapabiltyFullFilledInst = new MapListCapabiltyDataDefinition();
								fullfilledCapabilty.put(toInstId, mapListCapabiltyFullFilledInst);
							}

							ListCapabilityDataDefinition listCapabilityFull = mapListCapabiltyFullFilledInst.findByKey(type);
							if (listCapabilityFull == null) {
								listCapabilityFull = new ListCapabilityDataDefinition();
								mapListCapabiltyFullFilledInst.put(type, listCapabilityFull);
							}
							listCapabilityFull.add(capabilityForRelation);
						}
						break;
					} else {
						CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "No left occurrences capabilty {} to {} in container {}.", capabilityForRelation.getType(), toInstId, containerId);
						return Either.right(StorageOperationStatus.MATCH_NOT_FOUND);
					}
				}
			}
		}
		if (capabilityForRelation == null) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to fetch capabilty for type {} for instance {} in container {}.", type, toInstId, containerId);
			return Either.right(StorageOperationStatus.MATCH_NOT_FOUND);
		}

		// requirements
		String fromInstId = fromResInstance.getUniqueId();
		MapListRequirementDataDefinition mapListRequirementDataDefinition = calculatedRequirement.get(fromInstId);
		if (mapListRequirementDataDefinition == null) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to fetch calculated requirements for instance {} in container {}.", fromInstId, containerId);
			return Either.right(StorageOperationStatus.MATCH_NOT_FOUND);
		}
		ListRequirementDataDefinition listRequirementDataDefinition = mapListRequirementDataDefinition.getMapToscaDataDefinition().get(type);
		if (listRequirementDataDefinition == null) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to fetch calculated requirements for type {} for instance {} in container {}.", type, fromInstId, containerId);
			return Either.right(StorageOperationStatus.MATCH_NOT_FOUND);
		}

		RequirementDataDefinition requirementForRelation = null;
		Iterator<RequirementDataDefinition> iteratorReq = listRequirementDataDefinition.getListToscaDataDefinition().iterator();
		while (iteratorReq.hasNext()) {
			RequirementDataDefinition req = iteratorReq.next();
			if (req.getUniqueId().equals(relationPair.getRequirementUid()) && req.getOwnerId().equals(relationPair.getRequirementOwnerId())) {
				requirementForRelation = req;

				String leftOccurrences = req.getLeftOccurrences();
				if (leftOccurrences != null && !leftOccurrences.equals(RequirementDataDefinition.MAX_OCCURRENCES)) {
					Integer leftIntValue = Integer.parseInt(leftOccurrences);
					if (leftIntValue > 0) {
						--leftIntValue;
						req.setLeftOccurrences(String.valueOf(leftIntValue));
						if (leftIntValue == 0) {
							// remove from calculated
							iteratorReq.remove();
							// move to fulfilled
							MapListRequirementDataDefinition mapListRequirementFullFilledInst = fullfilledRequirement.get(fromInstId);
							if (mapListRequirementFullFilledInst == null) {
								mapListRequirementFullFilledInst = new MapListRequirementDataDefinition();
								fullfilledRequirement.put(fromInstId, mapListRequirementFullFilledInst);
							}

							ListRequirementDataDefinition listRequirementFull = mapListRequirementFullFilledInst.findByKey(type);
							if (listRequirementFull == null) {
								listRequirementFull = new ListRequirementDataDefinition();
								mapListRequirementFullFilledInst.put(type, listRequirementFull);
							}
							listRequirementFull.add(requirementForRelation);
						}
						break;
					} else {
						CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "No left occurrences requirement {} from {} to {} in container {}.", requirementForRelation.getCapability(), fromInstId, toInstId, containerId);
						return Either.right(StorageOperationStatus.MATCH_NOT_FOUND);
					}
				}
			}
		}
		if (!capabilityForRelation.getType().equals(requirementForRelation.getCapability())) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "No math for capability from type {} and requirement {} from {} to {} in container {}.", capabilityForRelation.getType(), requirementForRelation.getCapability(), fromInstId,
					toInstId, containerId);
			return Either.right(StorageOperationStatus.MATCH_NOT_FOUND);
		}

		RelationshipInstDataDefinition relationshipTypeData = buildRelationshipInstData(fromInstId, toInstId, relationPair);
		if(requirementForRelation.getRelationship() != null)
			relationshipTypeData.setType(requirementForRelation.getRelationship());
		return Either.left(relationshipTypeData);
	}

	private RelationshipInstDataDefinition buildRelationshipInstData(String fromResInstanceUid, String toInstId, RequirementAndRelationshipPair relationPair) {

		RelationshipInstDataDefinition relationshipInstData = new RelationshipInstDataDefinition();
		relationshipInstData.setUniqueId(UniqueIdBuilder.buildRelationsipInstInstanceUid(fromResInstanceUid, toInstId));

		relationshipInstData.setType(relationPair.getRelationship().getType());
		Long creationDate = System.currentTimeMillis();
		relationshipInstData.setCreationTime(creationDate);
		relationshipInstData.setModificationTime(creationDate);
		relationshipInstData.setCapabilityOwnerId(relationPair.getCapabilityOwnerId());
		relationshipInstData.setRequirementOwnerId(relationPair.getRequirementOwnerId());
		relationshipInstData.setCapabilityId(relationPair.getCapabilityUid());
		relationshipInstData.setRequirementId(relationPair.getRequirementUid());
		relationshipInstData.setFromId(fromResInstanceUid);
		relationshipInstData.setToId(toInstId);
		relationshipInstData.setRequirement(relationPair.getRequirement());
		relationshipInstData.setCapability(relationPair.getCapability());

		return relationshipInstData;
	}

	public StorageOperationStatus associateComponentInstancesToComponent(Component containerComponent, Map<ComponentInstance, Resource> resourcesInstancesMap, GraphVertex containerVertex, boolean allowDeleted) {

		StorageOperationStatus result = null;
		String containerId = containerComponent.getUniqueId();
		Map<String, ComponentInstanceDataDefinition> instancesJsonData = null;
		Either<GraphVertex, TitanOperationStatus> updateElement = null;
		if (!validateInstanceNames(resourcesInstancesMap)) {
			result = StorageOperationStatus.INCONSISTENCY;
		}
		if (result == null) {
			if (!validateInstanceNames(resourcesInstancesMap)) {
				result = StorageOperationStatus.INCONSISTENCY;
			}
		}
		if (result == null && !allowDeleted) {
			if (!validateDeletedResources(resourcesInstancesMap)) {
				result = StorageOperationStatus.INCONSISTENCY;
			}
		}
		if (result == null) {
			instancesJsonData = convertToComponentInstanceDataDefinition(resourcesInstancesMap, containerId);
		}
		if (result == null && MapUtils.isNotEmpty(instancesJsonData)) {
			containerVertex.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, System.currentTimeMillis());
			Map<String, CompositionDataDefinition> compositions = new HashMap<>();
			CompositionDataDefinition composition = new CompositionDataDefinition();
			composition.setComponentInstances(instancesJsonData);
			compositions.put(JsonConstantKeysEnum.COMPOSITION.getValue(), composition);
			containerVertex.setJson(compositions);
			updateElement = titanDao.updateVertex(containerVertex);
			if (updateElement.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update topology template {} with new component instances. ", containerComponent.getName());
				result = DaoStatusConverter.convertTitanStatusToStorageStatus(updateElement.right().value());
			}
		}
		if (result == null) {
			result = StorageOperationStatus.OK;
		}
		return result;
	}

	private Map<String, ComponentInstanceDataDefinition> convertToComponentInstanceDataDefinition(Map<ComponentInstance, Resource> resourcesInstancesMap, String containerId) {

		Map<String, ComponentInstanceDataDefinition> instances = new HashMap<>();
		for (Entry<ComponentInstance, Resource> entry : resourcesInstancesMap.entrySet()) {
			ComponentInstanceDataDefinition instance = buildComponentInstanceDataDefinition(entry.getKey(), containerId, null, true, ModelConverter.convertToToscaElement(entry.getValue()));
			instances.put(instance.getUniqueId(), instance);
		}
		return instances;
	}

	private boolean validateDeletedResources(Map<ComponentInstance, Resource> resourcesInstancesMap) {
		boolean result = true;
		for (Resource resource : resourcesInstancesMap.values()) {
			if (resource.getIsDeleted() != null && resource.getIsDeleted()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Component {} is already deleted. Cannot add component instance. ", resource.getName());
				result = false;
				break;
			}
		}
		return result;
	}

	private boolean validateInstanceNames(Map<ComponentInstance, Resource> resourcesInstancesMap) {
		boolean result = true;
		Set<String> names = new HashSet<>();
		for (ComponentInstance instance : resourcesInstancesMap.keySet()) {
			if (StringUtils.isEmpty(instance.getName())) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Component instance {} name is empty. Cannot add component instance. ", instance.getUniqueId());
				result = false;
				break;
			} else if (names.contains(instance.getName())) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Component instance with the name {} already exsists. Cannot add component instance. ", instance.getName());
				result = false;
				break;
			} else {
				names.add(instance.getName());
			}
		}
		return result;
	}

	public StorageOperationStatus addDeploymentArtifactsToInstance(String toscaElementId, String instanceId, Map<String, ArtifactDataDefinition> instDeplArtifacts) {
		return addArtifactsToInstance(toscaElementId, instanceId, instDeplArtifacts, EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS, VertexTypeEnum.INST_DEPLOYMENT_ARTIFACTS);
	}

	public StorageOperationStatus addInformationalArtifactsToInstance(String toscaElementId, String instanceId, Map<String, ArtifactDataDefinition> instDeplArtifacts) {
		return addArtifactsToInstance(toscaElementId, instanceId, instDeplArtifacts, EdgeLabelEnum.INSTANCE_ARTIFACTS, VertexTypeEnum.INSTANCE_ARTIFACTS);
	}

	public StorageOperationStatus addArtifactsToInstance(String toscaElementId, String instanceId, Map<String, ArtifactDataDefinition> instDeplArtifacts, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexType) {
		Either<GraphVertex, TitanOperationStatus> metadataVertex = titanDao.getVertexById(toscaElementId, JsonParseFlagEnum.NoParse);
		if (metadataVertex.isRight()) {
			TitanOperationStatus status = metadataVertex.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return DaoStatusConverter.convertTitanStatusToStorageStatus(status);
		}
		MapArtifactDataDefinition instArtifacts = new MapArtifactDataDefinition(instDeplArtifacts);
		return addToscaDataDeepElementsBlockToToscaElement(metadataVertex.left().value(), edgeLabel, vertexType, instArtifacts, instanceId);

	}

	@SuppressWarnings({ "unchecked" })
	public StorageOperationStatus generateCustomizationUUIDOnInstance(String componentId, String instanceId) {
		Either<GraphVertex, TitanOperationStatus> metadataVertex = titanDao.getVertexById(componentId, JsonParseFlagEnum.ParseAll);
		if (metadataVertex.isRight()) {
			TitanOperationStatus status = metadataVertex.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return DaoStatusConverter.convertTitanStatusToStorageStatus(status);
		}
		GraphVertex metaVertex = metadataVertex.left().value();
		Map<String, CompositionDataDefinition> json = (Map<String, CompositionDataDefinition>) metaVertex.getJson();
		CompositionDataDefinition compositionDataDefinition = json.get(JsonConstantKeysEnum.COMPOSITION.getValue());
		StorageOperationStatus status = updateCustomizationUUID(instanceId, compositionDataDefinition);
		if (status != StorageOperationStatus.OK) {
			logger.debug("Failed to update customization UUID for instance {} in component {} error {}", instanceId, componentId, status);
			return status;
		}
		Either<GraphVertex, TitanOperationStatus> updateVertex = titanDao.updateVertex(metaVertex);
		if (updateVertex.isRight()) {
			logger.debug("Failed to update vertex of component {} error {}", componentId, updateVertex.right().value());
			return DaoStatusConverter.convertTitanStatusToStorageStatus(updateVertex.right().value());
		}
		return StorageOperationStatus.OK;
	}

	public StorageOperationStatus generateCustomizationUUIDOnInstanceGroup(String componentId, String instanceId, List<String> groupInstances) {
		if (groupInstances != null) {
			Either<Map<String, MapGroupsDataDefinition>, TitanOperationStatus> dataFromGraph = getDataFromGraph(componentId, EdgeLabelEnum.INST_GROUPS);
			if (dataFromGraph.isRight()) {
				return DaoStatusConverter.convertTitanStatusToStorageStatus(dataFromGraph.right().value());
			}
			MapGroupsDataDefinition grInstPerInstance = dataFromGraph.left().value().get(instanceId);
			if (grInstPerInstance == null) {
				logger.debug("No  instance groups for instance {} in component {}", instanceId, componentId);
				return StorageOperationStatus.NOT_FOUND;
			}
			for (String instGroupForUpdate : groupInstances) {
				GroupInstanceDataDefinition groupInst = grInstPerInstance.findByKey(instGroupForUpdate);
				if (groupInst == null) {
					logger.debug("No group instance {} in group list  for instance {} in component {}", instGroupForUpdate, instanceId, componentId);
					continue;
				}
				UUID uuid = UUID.randomUUID();
				groupInst.setCustomizationUUID(uuid.toString());
			}

		}
		return StorageOperationStatus.OK;
	}

	public StorageOperationStatus addGroupInstancesToComponentInstance(Component containerComponent, ComponentInstance componentInstance, List<GroupInstance> groupInstances) {

		return addToscaDataDeepElementsBlockToToscaElement(containerComponent.getUniqueId(), EdgeLabelEnum.INST_GROUPS, VertexTypeEnum.INST_GROUPS,
				new MapDataDefinition<>(groupInstances.stream().collect(Collectors.toMap(gi -> gi.getName(), gi -> gi))), componentInstance.getUniqueId());
	}

	public StorageOperationStatus addDeploymentArtifactsToComponentInstance(Component containerComponent, ComponentInstance componentInstance, Map<String, ArtifactDefinition> deploymentArtifacts) {

		return addToscaDataDeepElementsBlockToToscaElement(containerComponent.getUniqueId(), EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS, VertexTypeEnum.INST_DEPLOYMENT_ARTIFACTS, new MapDataDefinition<>(deploymentArtifacts),
				componentInstance.getUniqueId());
	}

	public StorageOperationStatus updateComponentInstanceProperty(Component containerComponent, String componentInstanceId, ComponentInstanceProperty property) {

		List<String> pathKeys = new ArrayList<>();
		pathKeys.add(componentInstanceId);
		return updateToscaDataDeepElementOfToscaElement(containerComponent.getUniqueId(), EdgeLabelEnum.INST_PROPERTIES, VertexTypeEnum.INST_PROPERTIES, property, pathKeys, JsonPresentationFields.NAME);
	}

	public StorageOperationStatus updateComponentInstanceCapabilityProperty(Component containerComponent, String componentInstanceId, String capabilityUniqueId, ComponentInstanceProperty property) {
		List<String> pathKeys = new ArrayList<>();
		pathKeys.add(componentInstanceId);
		pathKeys.add(capabilityUniqueId);
		return updateToscaDataDeepElementOfToscaElement(containerComponent.getUniqueId(), EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, VertexTypeEnum.CALCULATED_CAP_PROPERTIES, property, pathKeys, JsonPresentationFields.NAME);
	}

	public StorageOperationStatus addComponentInstanceProperty(Component containerComponent, String componentInstanceId, ComponentInstanceProperty property) {
		List<String> pathKeys = new ArrayList<>();
		pathKeys.add(componentInstanceId);
		return addToscaDataDeepElementToToscaElement(containerComponent.getUniqueId(), EdgeLabelEnum.INST_PROPERTIES, VertexTypeEnum.INST_PROPERTIES, property, pathKeys, JsonPresentationFields.NAME);
	}

	public StorageOperationStatus updateComponentInstanceProperties(Component containerComponent, String componentInstanceId, List<ComponentInstanceProperty> properties) {
		List<String> pathKeys = new ArrayList<>();
		pathKeys.add(componentInstanceId);
		return updateToscaDataDeepElementsOfToscaElement(containerComponent.getUniqueId(), EdgeLabelEnum.INST_PROPERTIES, VertexTypeEnum.INST_PROPERTIES, properties, pathKeys, JsonPresentationFields.NAME);
	}

	public StorageOperationStatus updateComponentInstanceInput(Component containerComponent, String componentInstanceId, ComponentInstanceInput property) {

		List<String> pathKeys = new ArrayList<>();
		pathKeys.add(componentInstanceId);
		return updateToscaDataDeepElementOfToscaElement(containerComponent.getUniqueId(), EdgeLabelEnum.INST_INPUTS, VertexTypeEnum.INST_INPUTS, property, pathKeys, JsonPresentationFields.NAME);
	}

	public StorageOperationStatus updateComponentInstanceInputs(Component containerComponent, String componentInstanceId, List<ComponentInstanceInput> properties) {
		List<String> pathKeys = new ArrayList<>();
		pathKeys.add(componentInstanceId);
		return updateToscaDataDeepElementsOfToscaElement(containerComponent.getUniqueId(), EdgeLabelEnum.INST_INPUTS, VertexTypeEnum.INST_INPUTS, properties, pathKeys, JsonPresentationFields.NAME);
	}

	public StorageOperationStatus addComponentInstanceInput(Component containerComponent, String componentInstanceId, ComponentInstanceInput property) {
		List<String> pathKeys = new ArrayList<>();
		pathKeys.add(componentInstanceId);
		return addToscaDataDeepElementToToscaElement(containerComponent.getUniqueId(), EdgeLabelEnum.INST_INPUTS, VertexTypeEnum.INST_INPUTS, property, pathKeys, JsonPresentationFields.NAME);
	}

}
