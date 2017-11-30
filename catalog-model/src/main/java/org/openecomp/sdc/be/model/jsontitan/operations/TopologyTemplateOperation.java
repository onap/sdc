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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CompositionDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapCapabiltyProperty;
import org.openecomp.sdc.be.datatypes.elements.MapGroupsDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListCapabiltyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsontitan.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.jsontitan.enums.JsonConstantKeysEnum;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility.LogLevelEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

import fj.data.Either;

@org.springframework.stereotype.Component("topology-template-operation")
public class TopologyTemplateOperation extends ToscaElementOperation {
	private static Logger log = LoggerFactory.getLogger(TopologyTemplateOperation.class.getName());

	public Either<TopologyTemplate, StorageOperationStatus> createTopologyTemplate(TopologyTemplate topologyTemplate) {
		Either<TopologyTemplate, StorageOperationStatus> result = null;

		topologyTemplate.generateUUID();

		topologyTemplate = (TopologyTemplate) getResourceMetaDataFromResource(topologyTemplate);
		String resourceUniqueId = topologyTemplate.getUniqueId();
		if (resourceUniqueId == null) {
			resourceUniqueId = UniqueIdBuilder.buildResourceUniqueId();
			topologyTemplate.setUniqueId(resourceUniqueId);
		}

		GraphVertex topologyTemplateVertex = new GraphVertex();
		topologyTemplateVertex = fillMetadata(topologyTemplateVertex, topologyTemplate, JsonParseFlagEnum.ParseAll);

		Either<GraphVertex, TitanOperationStatus> createdVertex = titanDao.createVertex(topologyTemplateVertex);
		if (createdVertex.isRight()) {
			TitanOperationStatus status = createdVertex.right().value();
			log.error("Error returned after creating topology template data node {}. status returned is ", topologyTemplateVertex, status);
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			return result;
		}

		topologyTemplateVertex = createdVertex.left().value();

		StorageOperationStatus assosiateCommon = assosiateCommonForToscaElement(topologyTemplateVertex, topologyTemplate, null);
		if (assosiateCommon != StorageOperationStatus.OK) {
			result = Either.right(assosiateCommon);
			return result;
		}

		StorageOperationStatus associateCategory = assosiateMetadataToCategory(topologyTemplateVertex, topologyTemplate);
		if (associateCategory != StorageOperationStatus.OK) {
			result = Either.right(associateCategory);
			return result;
		}

		StorageOperationStatus associateInputs = associateInputsToComponent(topologyTemplateVertex, topologyTemplate);
		if (associateInputs != StorageOperationStatus.OK) {
			result = Either.right(associateInputs);
			return result;
		}
		StorageOperationStatus associateGroups = associateGroupsToComponent(topologyTemplateVertex, topologyTemplate);
		if (associateGroups != StorageOperationStatus.OK) {
			result = Either.right(associateGroups);
			return result;
		}
		StorageOperationStatus associateInstAttr = associateInstAttributesToComponent(topologyTemplateVertex, topologyTemplate);
		if (associateInstAttr != StorageOperationStatus.OK) {
			result = Either.right(associateInstAttr);
			return result;
		}
		StorageOperationStatus associateInstProperties = associateInstPropertiesToComponent(topologyTemplateVertex, topologyTemplate);
		if (associateInstProperties != StorageOperationStatus.OK) {
			result = Either.right(associateInstProperties);
			return result;
		}
		StorageOperationStatus associateInstInputs = associateInstInputsToComponent(topologyTemplateVertex, topologyTemplate);
		if (associateInstProperties != StorageOperationStatus.OK) {
			result = Either.right(associateInstInputs);
			return result;
		}
		StorageOperationStatus associateInstGroups = associateInstGroupsToComponent(topologyTemplateVertex, topologyTemplate);
		if (associateInstGroups != StorageOperationStatus.OK) {
			result = Either.right(associateInstInputs);
			return result;
		}
		
		StorageOperationStatus associateRequirements = associateRequirementsToResource(topologyTemplateVertex, topologyTemplate);
		if (associateRequirements != StorageOperationStatus.OK) {
			result = Either.right(associateRequirements);
			return result;
		}

		StorageOperationStatus associateCapabilities = associateCapabilitiesToResource(topologyTemplateVertex, topologyTemplate);
		if (associateCapabilities != StorageOperationStatus.OK) {
			result = Either.right(associateCapabilities);
			return result;
		}

		StorageOperationStatus associateArtifacts = associateTopologyTemplateArtifactsToComponent(topologyTemplateVertex, topologyTemplate);
		if (associateArtifacts != StorageOperationStatus.OK) {
			result = Either.right(associateArtifacts);
			return result;
		}

		StorageOperationStatus addAdditionalInformation = addAdditionalInformationToResource(topologyTemplateVertex, topologyTemplate);
		if (addAdditionalInformation != StorageOperationStatus.OK) {
			result = Either.right(addAdditionalInformation);
			return result;
		}
		StorageOperationStatus associateCapProperties = associateCapPropertiesToResource(topologyTemplateVertex, topologyTemplate);
		if (associateCapProperties != StorageOperationStatus.OK) {
			result = Either.right(associateCapProperties);
			return result;
		}
		return Either.left(topologyTemplate);

	}

	private StorageOperationStatus associateCapPropertiesToResource(GraphVertex topologyTemplateVertex, TopologyTemplate topologyTemplate) {
		Map<String, MapCapabiltyProperty> calculatedCapProperties = topologyTemplate.getCalculatedCapabilitiesProperties();
		if (calculatedCapProperties != null && !calculatedCapProperties.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(topologyTemplateVertex, VertexTypeEnum.CALCULATED_CAP_PROPERTIES, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, calculatedCapProperties);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus associateCapabilitiesToResource(GraphVertex nodeTypeVertex, TopologyTemplate topologyTemplate) {
		Map<String, MapListCapabiltyDataDefinition> calculatedCapabilities = topologyTemplate.getCalculatedCapabilities();
		if (calculatedCapabilities != null && !calculatedCapabilities.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.CALCULATED_CAPABILITIES, EdgeLabelEnum.CALCULATED_CAPABILITIES, calculatedCapabilities);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		Map<String, MapListCapabiltyDataDefinition> fullfilledCapabilities = topologyTemplate.getFullfilledCapabilities();
		if (fullfilledCapabilities != null && !fullfilledCapabilities.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.FULLFILLED_CAPABILITIES, EdgeLabelEnum.FULLFILLED_CAPABILITIES, fullfilledCapabilities);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		return StorageOperationStatus.OK;

	}

	private StorageOperationStatus associateRequirementsToResource(GraphVertex nodeTypeVertex, TopologyTemplate topologyTemplate) {
		Map<String, MapListRequirementDataDefinition> calculatedRequirements = topologyTemplate.getCalculatedRequirements();
		if (calculatedRequirements != null && !calculatedRequirements.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.CALCULATED_REQUIREMENTS, EdgeLabelEnum.CALCULATED_REQUIREMENTS, calculatedRequirements);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		Map<String, MapListRequirementDataDefinition> fullfilledRequirements = topologyTemplate.getFullfilledRequirements();
		if (fullfilledRequirements != null && !fullfilledRequirements.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.FULLFILLED_REQUIREMENTS, EdgeLabelEnum.FULLFILLED_REQUIREMENTS, fullfilledRequirements);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus associateTopologyTemplateArtifactsToComponent(GraphVertex nodeTypeVertex, TopologyTemplate topologyTemplate) {
		Map<String, ArtifactDataDefinition> addInformation = topologyTemplate.getServiceApiArtifacts();

		if (addInformation != null && !addInformation.isEmpty()) {
			addInformation.values().stream().filter(a -> a.getUniqueId() == null).forEach(a -> {
				String uniqueId = UniqueIdBuilder.buildPropertyUniqueId(nodeTypeVertex.getUniqueId().toLowerCase(), a.getArtifactLabel().toLowerCase());
				a.setUniqueId(uniqueId);
			});
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.SERVICE_API_ARTIFACTS, EdgeLabelEnum.SERVICE_API_ARTIFACTS, addInformation);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		Map<String, MapArtifactDataDefinition> instArtifacts = topologyTemplate.getInstDeploymentArtifacts();

		if (instArtifacts != null && !instArtifacts.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.INST_DEPLOYMENT_ARTIFACTS, EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS, instArtifacts);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		Map<String, MapArtifactDataDefinition> instInfoArtifacts = topologyTemplate.getInstanceArtifacts();

		if (instInfoArtifacts != null && !instInfoArtifacts.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.INSTANCE_ARTIFACTS, EdgeLabelEnum.INSTANCE_ARTIFACTS, instInfoArtifacts);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus addAdditionalInformationToResource(GraphVertex nodeTypeVertex, TopologyTemplate topologyTemplate) {

		Map<String, AdditionalInfoParameterDataDefinition> addInformation = topologyTemplate.getAdditionalInformation();

		if (addInformation != null && !addInformation.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.ADDITIONAL_INFORMATION, EdgeLabelEnum.ADDITIONAL_INFORMATION, addInformation);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		return StorageOperationStatus.OK;
	}

	public StorageOperationStatus associateInstPropertiesToComponent(GraphVertex nodeTypeVertex, TopologyTemplate topologyTemplate) {
		Map<String, MapPropertiesDataDefinition> instProps = topologyTemplate.getInstProperties();
		return associateInstPropertiesToComponent(nodeTypeVertex, instProps);
	}

	public StorageOperationStatus associateInstInputsToComponent(GraphVertex nodeTypeVertex, TopologyTemplate topologyTemplate) {
		Map<String, MapPropertiesDataDefinition> instProps = topologyTemplate.getInstInputs();
		return associateInstInputsToComponent(nodeTypeVertex, instProps);
	}
	
	public StorageOperationStatus associateInstGroupsToComponent(GraphVertex nodeTypeVertex, TopologyTemplate topologyTemplate) {
		Map<String, MapGroupsDataDefinition> instGroups = topologyTemplate.getInstGroups();
		return associateInstGroupsToComponent(nodeTypeVertex, instGroups);
	}
	

	public StorageOperationStatus associateInstPropertiesToComponent(GraphVertex nodeTypeVertex, Map<String, MapPropertiesDataDefinition> instProps) {
		if (instProps != null && !instProps.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.INST_PROPERTIES, EdgeLabelEnum.INST_PROPERTIES, instProps);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		return StorageOperationStatus.OK;
	}

	public StorageOperationStatus associateInstInputsToComponent(GraphVertex nodeTypeVertex, Map<String, MapPropertiesDataDefinition> instInputs) {
		if (instInputs != null && !instInputs.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.INST_INPUTS, EdgeLabelEnum.INST_INPUTS, instInputs);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		return StorageOperationStatus.OK;
	}
	
	public StorageOperationStatus associateInstGroupsToComponent(GraphVertex nodeTypeVertex, Map<String, MapGroupsDataDefinition> instGroups) {
		if (instGroups != null && !instGroups.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.INST_GROUPS, EdgeLabelEnum.INST_GROUPS, instGroups);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		return StorageOperationStatus.OK;
	}


	public StorageOperationStatus deleteInstInputsToComponent(GraphVertex nodeTypeVertex, Map<String, MapPropertiesDataDefinition> instInputs) {

		if (instInputs != null && !instInputs.isEmpty()) {
			instInputs.entrySet().forEach(i -> {
				List<String> uniqueKeys = new ArrayList<String>(i.getValue().getMapToscaDataDefinition().keySet());
				List<String> pathKeys = new ArrayList<String>();
				pathKeys.add(i.getKey());

				StorageOperationStatus status = deleteToscaDataDeepElements(nodeTypeVertex, EdgeLabelEnum.INST_INPUTS, VertexTypeEnum.INST_INPUTS, uniqueKeys, pathKeys, JsonPresentationFields.NAME);
				if (status != StorageOperationStatus.OK) {
					return;
				}
			});
		}

		return StorageOperationStatus.OK;
	}

	public StorageOperationStatus addInstPropertiesToComponent(GraphVertex nodeTypeVertex, Map<String, MapPropertiesDataDefinition> instInputs) {

		if (instInputs != null && !instInputs.isEmpty()) {
			instInputs.entrySet().forEach(i -> {
				StorageOperationStatus status = addToscaDataDeepElementsBlockToToscaElement(nodeTypeVertex, EdgeLabelEnum.INST_PROPERTIES, VertexTypeEnum.INST_PROPERTIES, i.getValue(), i.getKey());
				if (status != StorageOperationStatus.OK) {
					return;
				}
			});
		}

		return StorageOperationStatus.OK;
	}

	public StorageOperationStatus associateInstDeploymentArtifactsToComponent(GraphVertex nodeTypeVertex, Map<String, MapArtifactDataDefinition> instArtifacts) {
		return associateInstanceArtifactsToComponent(nodeTypeVertex, instArtifacts, VertexTypeEnum.INST_DEPLOYMENT_ARTIFACTS, EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
	}
	
	public StorageOperationStatus associateInstArtifactsToComponent(GraphVertex nodeTypeVertex, Map<String, MapArtifactDataDefinition> instArtifacts) {
		return associateInstanceArtifactsToComponent(nodeTypeVertex, instArtifacts, VertexTypeEnum.INSTANCE_ARTIFACTS, EdgeLabelEnum.INSTANCE_ARTIFACTS);
	}

	private StorageOperationStatus associateInstanceArtifactsToComponent(GraphVertex nodeTypeVertex, Map<String, MapArtifactDataDefinition> instProps, VertexTypeEnum vertexType, EdgeLabelEnum edgeLabel) {
		if (instProps != null && !instProps.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, vertexType, edgeLabel, instProps);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		return StorageOperationStatus.OK;
	}

	public StorageOperationStatus associateCalcCapReqToComponent(GraphVertex nodeTypeVertex, Map<String, MapListRequirementDataDefinition> calcRequirements, Map<String, MapListCapabiltyDataDefinition> calcCapabilty, Map<String, MapCapabiltyProperty> calculatedCapabilitiesProperties) {
		if (calcRequirements != null && !calcRequirements.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.CALCULATED_REQUIREMENTS, EdgeLabelEnum.CALCULATED_REQUIREMENTS, calcRequirements);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
			Map<String, MapListRequirementDataDefinition> fullFilled = new HashMap<>();
			assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.FULLFILLED_REQUIREMENTS, EdgeLabelEnum.FULLFILLED_REQUIREMENTS, fullFilled);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		if (calcCapabilty != null && !calcCapabilty.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.CALCULATED_CAPABILITIES, EdgeLabelEnum.CALCULATED_CAPABILITIES, calcCapabilty);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
			Map<String, MapListCapabiltyDataDefinition> fullFilled = new HashMap<>();
			assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.FULLFILLED_CAPABILITIES, EdgeLabelEnum.FULLFILLED_CAPABILITIES, fullFilled);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		if ( calculatedCapabilitiesProperties != null && !calculatedCapabilitiesProperties.isEmpty() ){
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.CALCULATED_CAP_PROPERTIES, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, calculatedCapabilitiesProperties);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus associateInstAttributesToComponent(GraphVertex nodeTypeVertex, TopologyTemplate topologyTemplate) {
		Map<String, MapPropertiesDataDefinition> instAttr = topologyTemplate.getInstAttributes();
		return associateInstAttributeToComponent(nodeTypeVertex, instAttr);
	}

	public StorageOperationStatus associateInstAttributeToComponent(GraphVertex nodeTypeVertex, Map<String, MapPropertiesDataDefinition> instAttr) {
		if (instAttr != null && !instAttr.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.INST_ATTRIBUTES, EdgeLabelEnum.INST_ATTRIBUTES, instAttr);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		return StorageOperationStatus.OK;
	}

	public StorageOperationStatus associateGroupsToComponent(GraphVertex nodeTypeVertex, Map<String, GroupDataDefinition> groups) {

		if (groups != null && !groups.isEmpty()) {
			groups.values().stream().filter(p -> p.getUniqueId() == null).forEach(p -> {
				String uid = UniqueIdBuilder.buildGroupingUid(nodeTypeVertex.getUniqueId(), p.getName());
				p.setUniqueId(uid);
			});
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.GROUPS, EdgeLabelEnum.GROUPS, groups);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus associateGroupsToComponent(GraphVertex nodeTypeVertex, TopologyTemplate topologyTemplate) {
		return associateGroupsToComponent(nodeTypeVertex, topologyTemplate.getGroups());
	}

	public StorageOperationStatus associateInputsToComponent(GraphVertex nodeTypeVertex, TopologyTemplate topologyTemplate) {
		Map<String, PropertyDataDefinition> inputs = topologyTemplate.getInputs();
		return associateInputsToComponent(nodeTypeVertex, inputs, topologyTemplate.getUniqueId());
	}

	public StorageOperationStatus associateInputsToComponent(GraphVertex nodeTypeVertex, Map<String, PropertyDataDefinition> inputs, String id) {
		if (inputs != null && !inputs.isEmpty()) {
			inputs.values().stream().filter(e -> e.getUniqueId() == null).forEach(e -> e.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(id, e.getName())));

			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.INPUTS, EdgeLabelEnum.INPUTS, inputs);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		return StorageOperationStatus.OK;
	}

	private GraphVertex fillMetadata(GraphVertex nodeTypeVertex, TopologyTemplate topologyTemplate, JsonParseFlagEnum flag) {
		nodeTypeVertex.setLabel(VertexTypeEnum.TOPOLOGY_TEMPLATE);
		fillCommonMetadata(nodeTypeVertex, topologyTemplate);
		if (flag == JsonParseFlagEnum.ParseAll || flag == JsonParseFlagEnum.ParseJson) {
			nodeTypeVertex.setJson(topologyTemplate.getCompositions());
		}
		nodeTypeVertex.addMetadataProperty(GraphPropertyEnum.CSAR_UUID, topologyTemplate.getMetadataValue(JsonPresentationFields.CSAR_UUID));
		nodeTypeVertex.addMetadataProperty(GraphPropertyEnum.DISTRIBUTION_STATUS, topologyTemplate.getMetadataValue(JsonPresentationFields.DISTRIBUTION_STATUS));
		
		return nodeTypeVertex;

	}

	private StorageOperationStatus assosiateMetadataToCategory(GraphVertex nodeTypeVertex, TopologyTemplate topologyTemplate) {
		if (topologyTemplate.getResourceType() == null) {
			// service
			return associateServiceMetadataToCategory(nodeTypeVertex, topologyTemplate);
		} else {
			// VF
			return assosiateResourceMetadataToCategory(nodeTypeVertex, topologyTemplate);
		}
	}

	private StorageOperationStatus associateServiceMetadataToCategory(GraphVertex nodeTypeVertex, TopologyTemplate topologyTemplate) {
		String categoryName = topologyTemplate.getCategories().get(0).getName();
		Either<GraphVertex, StorageOperationStatus> category = categoryOperation.getCategory(categoryName, VertexTypeEnum.SERVICE_CATEGORY);
		if (category.isRight()) {
			log.trace("NO category {} for service {}", categoryName, topologyTemplate.getUniqueId());
			return StorageOperationStatus.CATEGORY_NOT_FOUND;
		}
		GraphVertex categoryV = category.left().value();
		TitanOperationStatus createEdge = titanDao.createEdge(nodeTypeVertex, categoryV, EdgeLabelEnum.CATEGORY, new HashMap<>());
		if (createEdge != TitanOperationStatus.OK) {
			log.trace("Failed to associate resource {} to category {} with id {}", topologyTemplate.getUniqueId(), categoryName, categoryV.getUniqueId());
			return DaoStatusConverter.convertTitanStatusToStorageStatus(createEdge);
		}
		return StorageOperationStatus.OK;
	}

	@Override
	public Either<ToscaElement, StorageOperationStatus> getToscaElement(String uniqueId, ComponentParametersView componentParametersView) {
		JsonParseFlagEnum parseFlag = componentParametersView.detectParseFlag();

		Either<GraphVertex, StorageOperationStatus> componentByLabelAndId = getComponentByLabelAndId(uniqueId, ToscaElementTypeEnum.TopologyTemplate, parseFlag);
		if (componentByLabelAndId.isRight()) {
			return Either.right(componentByLabelAndId.right().value());
		}
		GraphVertex componentV = componentByLabelAndId.left().value();

		return getToscaElement(componentV, componentParametersView);

	}
	// -------------------------------------------------------------

	public Either<ToscaElement, StorageOperationStatus> getToscaElement(GraphVertex componentV, ComponentParametersView componentParametersView) {
		TopologyTemplate toscaElement;

		toscaElement = convertToTopologyTemplate(componentV);
		TitanOperationStatus status = null;
		if (false == componentParametersView.isIgnoreUsers()) {
			status = setCreatorFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}

			status = setLastModifierFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}
		if (false == componentParametersView.isIgnoreCategories()) {
			status = setTopologyTempalteCategoriesFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));

			}
		}
		if (false == componentParametersView.isIgnoreArtifacts()) {
			TitanOperationStatus storageStatus = setAllArtifactsFromGraph(componentV, toscaElement);
			if (storageStatus != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(storageStatus));
			}
		}
		if (false == componentParametersView.isIgnoreComponentInstancesProperties()) {
			status = setComponentInstancesPropertiesFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}
		if (false == componentParametersView.isIgnoreCapabilities()) {
			status = setCapabilitiesFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}
		if (false == componentParametersView.isIgnoreRequirements()) {
			status = setRequirementsFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}
		if (false == componentParametersView.isIgnoreAllVersions()) {
			status = setAllVersions(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}
		if (false == componentParametersView.isIgnoreAdditionalInformation()) {
			status = setAdditionalInformationFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}

		if (false == componentParametersView.isIgnoreGroups()) {
			status = setGroupsFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}

		}
		if (false == componentParametersView.isIgnoreComponentInstances()) {
			status = setInstGroupsFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}

		}
		if (false == componentParametersView.isIgnoreInputs()) {
			status = setInputsFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}

		}
		if (false == componentParametersView.isIgnoreProperties()) {
			status = setPropertiesFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}

		}

		if (false == componentParametersView.isIgnoreComponentInstancesInputs()) {
			status = setComponentInstancesInputsFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));

			}
		}

		if (false == componentParametersView.isIgnoreCapabiltyProperties()) {
			status = setComponentInstancesCapPropertiesFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));

			}
		}
		return Either.left(toscaElement);
	}

	private TitanOperationStatus setComponentInstancesCapPropertiesFromGraph(GraphVertex componentV, TopologyTemplate topologyTemplate) {
		Either<Map<String, MapCapabiltyProperty>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES);
		if (result.isLeft()) {
			topologyTemplate.setCalculatedCapabilitiesProperties(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus setPropertiesFromGraph(GraphVertex componentV, TopologyTemplate toscaElement) {
		Either<Map<String, PropertyDataDefinition>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.PROPERTIES);
		if (result.isLeft()) {
			toscaElement.setProperties(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus setInstGroupsFromGraph(GraphVertex componentV, TopologyTemplate topologyTemplate) {
		Either<Map<String, MapGroupsDataDefinition>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.INST_GROUPS);
		if (result.isLeft()) {
			topologyTemplate.setInstGroups(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus setComponentInstancesPropertiesFromGraph(GraphVertex componentV, TopologyTemplate topologyTemplate) {
		Either<Map<String, MapPropertiesDataDefinition>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.INST_PROPERTIES);
		if (result.isLeft()) {
			topologyTemplate.setInstProperties(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus setComponentInstancesInputsFromGraph(GraphVertex componentV, TopologyTemplate topologyTemplate) {
		Either<Map<String, MapPropertiesDataDefinition>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.INST_INPUTS);
		if (result.isLeft()) {
			topologyTemplate.setInstInputs(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		return TitanOperationStatus.OK;
	}

	@Override
	protected <T extends ToscaElement> TitanOperationStatus setRequirementsFromGraph(GraphVertex componentV, T toscaElement) {
		Either<Map<String, MapListRequirementDataDefinition>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.CALCULATED_REQUIREMENTS);
		if (result.isLeft()) {
			((TopologyTemplate) toscaElement).setCalculatedRequirements(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		result = getDataFromGraph(componentV, EdgeLabelEnum.FULLFILLED_REQUIREMENTS);
		if (result.isLeft()) {
			((TopologyTemplate) toscaElement).setFullfilledRequirements(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		return TitanOperationStatus.OK;

	}

	protected <T extends ToscaElement> TitanOperationStatus setCapabilitiesFromGraph(GraphVertex componentV, T toscaElement) {
		Either<Map<String, MapListCapabiltyDataDefinition>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.CALCULATED_CAPABILITIES);
		if (result.isLeft()) {
			((TopologyTemplate) toscaElement).setCalculatedCapabilities(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		result = getDataFromGraph(componentV, EdgeLabelEnum.FULLFILLED_CAPABILITIES);
		if (result.isLeft()) {
			((TopologyTemplate) toscaElement).setFullfilledCapabilities(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus setAllArtifactsFromGraph(GraphVertex componentV, TopologyTemplate toscaElement) {
		TitanOperationStatus storageStatus = setArtifactsFromGraph(componentV, toscaElement);
		if (storageStatus != TitanOperationStatus.OK) {
			return storageStatus;
		}
		Either<Map<String, ArtifactDataDefinition>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.SERVICE_API_ARTIFACTS);
		if (result.isLeft()) {
			toscaElement.setServiceApiArtifacts(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		Either<Map<String, MapArtifactDataDefinition>, TitanOperationStatus> resultInstArt = getDataFromGraph(componentV, EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
		if (resultInstArt.isLeft()) {
			toscaElement.setInstDeploymentArtifacts(resultInstArt.left().value());
		} else {
			if (resultInstArt.right().value() != TitanOperationStatus.NOT_FOUND) {
				return resultInstArt.right().value();
			}
		}
		Either<Map<String, MapArtifactDataDefinition>, TitanOperationStatus> instanceArt = getDataFromGraph(componentV, EdgeLabelEnum.INSTANCE_ARTIFACTS);
		if (instanceArt.isLeft()) {
			toscaElement.setInstanceArtifacts(instanceArt.left().value());
		} else {
			if (instanceArt.right().value() != TitanOperationStatus.NOT_FOUND) {
				return instanceArt.right().value();
			}
		}
		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus setInputsFromGraph(GraphVertex componentV, TopologyTemplate toscaElement) {
		Either<Map<String, PropertyDataDefinition>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.INPUTS);
		if (result.isLeft()) {
			toscaElement.setInputs(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus setGroupsFromGraph(GraphVertex componentV, TopologyTemplate toscaElement) {
		Either<Map<String, GroupDataDefinition>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.GROUPS);
		if (result.isLeft()) {
			toscaElement.setGroups(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus setTopologyTempalteCategoriesFromGraph(GraphVertex componentV, ToscaElement toscaElement) {
		List<CategoryDefinition> categories = new ArrayList<>();

		switch (componentV.getType()) {
		case RESOURCE:
			return setResourceCategoryFromGraph(componentV, toscaElement);
		case SERVICE:
			return setServiceCategoryFromGraph(componentV, toscaElement, categories);

		default:
			log.debug("Not supported component type {} ", componentV.getType());
			break;
		}
		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus setServiceCategoryFromGraph(GraphVertex componentV, ToscaElement toscaElement, List<CategoryDefinition> categories) {
		Either<GraphVertex, TitanOperationStatus> childVertex = titanDao.getChildVertex(componentV, EdgeLabelEnum.CATEGORY, JsonParseFlagEnum.NoParse);
		if (childVertex.isRight()) {
			log.debug("failed to fetch {} for tosca element with id {}, error {}", EdgeLabelEnum.CATEGORY, componentV.getUniqueId(), childVertex.right().value());
			return childVertex.right().value();
		}
		GraphVertex categoryV = childVertex.left().value();
		Map<GraphPropertyEnum, Object> metadataProperties = categoryV.getMetadataProperties();
		CategoryDefinition category = new CategoryDefinition();
		category.setUniqueId(categoryV.getUniqueId());
		category.setNormalizedName((String) metadataProperties.get(GraphPropertyEnum.NORMALIZED_NAME));
		category.setName((String) metadataProperties.get(GraphPropertyEnum.NAME));

		Type listTypeCat = new TypeToken<List<String>>() {}.getType();
		List<String> iconsfromJsonCat = getGson().fromJson((String) metadataProperties.get(GraphPropertyEnum.ICONS.getProperty()), listTypeCat);
		category.setIcons(iconsfromJsonCat);
		categories.add(category);
		toscaElement.setCategories(categories);

		return TitanOperationStatus.OK;
	}

	private TopologyTemplate convertToTopologyTemplate(GraphVertex componentV) {

		TopologyTemplate topologyTemplate = super.convertToComponent(componentV);

		Map<String, CompositionDataDefinition> json = (Map<String, CompositionDataDefinition>) componentV.getJson();
		topologyTemplate.setCompositions(json);

		return topologyTemplate;
	}

	@Override
	public Either<ToscaElement, StorageOperationStatus> deleteToscaElement(GraphVertex toscaElementVertex) {
		Either<ToscaElement, StorageOperationStatus> nodeType = getToscaElement(toscaElementVertex, new ComponentParametersView());
		if (nodeType.isRight()) {
			log.debug("Failed to fetch tosca element {} error {}", toscaElementVertex.getUniqueId(), nodeType.right().value());
			return nodeType;
		}
		TitanOperationStatus status = disassociateAndDeleteCommonElements(toscaElementVertex);
		if (status != TitanOperationStatus.OK) {
			Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.INST_ATTRIBUTES);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disassociate instances attributes for {} error {}", toscaElementVertex.getUniqueId(), status);
			Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.INST_PROPERTIES);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disassociate instances properties for {} error {}", toscaElementVertex.getUniqueId(), status);
			Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}

		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.INST_INPUTS);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disassociate instances inputs for {} error {}", toscaElementVertex.getUniqueId(), status);
			Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}

		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.GROUPS);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disassociate groups for {} error {}", toscaElementVertex.getUniqueId(), status);
			Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.INST_GROUPS);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disassociate instance groups for {} error {}", toscaElementVertex.getUniqueId(), status);
			Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.INPUTS);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disassociate inputs for {} error {}", toscaElementVertex.getUniqueId(), status);
			Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.INST_INPUTS);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disassociate instance inputs for {} error {}", toscaElementVertex.getUniqueId(), status);
			Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.CALCULATED_CAPABILITIES);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disassociate calculated capabiliites for {} error {}", toscaElementVertex.getUniqueId(), status);
			Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.FULLFILLED_CAPABILITIES);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disassociate fullfilled capabilities for {} error {}", toscaElementVertex.getUniqueId(), status);
			Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disassociate calculated capabiliites properties for {} error {}", toscaElementVertex.getUniqueId(), status);
			Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.CALCULATED_REQUIREMENTS);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disassociate calculated requirements for {} error {}", toscaElementVertex.getUniqueId(), status);
			Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.FULLFILLED_REQUIREMENTS);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disassociate full filled requirements for {} error {}", toscaElementVertex.getUniqueId(), status);
			Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disassociate instance artifacts for {} error {}", toscaElementVertex.getUniqueId(), status);
			Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.SERVICE_API_ARTIFACTS);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disassociate service api artifacts for {} error {}", toscaElementVertex.getUniqueId(), status);
			Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.INSTANCE_ARTIFACTS);
		toscaElementVertex.getVertex().remove();
		log.trace("Tosca element vertex for {} was removed", toscaElementVertex.getUniqueId());

		return nodeType;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Either<TopologyTemplate, StorageOperationStatus> createToscaElement(ToscaElement toscaElement) {
		return createTopologyTemplate((TopologyTemplate) toscaElement);
	}

	@Override
	protected <T extends ToscaElement> TitanOperationStatus setCategoriesFromGraph(GraphVertex vertexComponent, T toscaElement) {
		return setTopologyTempalteCategoriesFromGraph(vertexComponent, toscaElement);
	}

	@Override
	protected <T extends ToscaElement> StorageOperationStatus validateCategories(T toscaElementToUpdate, GraphVertex elementV) {
		// Product isn't supported now!!
		// TODO add for Product
		if (toscaElementToUpdate.getComponentType() == ComponentTypeEnum.SERVICE) {
			return validateServiceCategory(toscaElementToUpdate, elementV);
		} else {
			// Resource
			return validateResourceCategory(toscaElementToUpdate, elementV);
		}
	}

	@Override
	protected <T extends ToscaElement> StorageOperationStatus updateDerived(T toscaElementToUpdate, GraphVertex updateElementV) {
		// not relevant now for topology template
		return StorageOperationStatus.OK;
	}

	@Override
	public <T extends ToscaElement> void fillToscaElementVertexData(GraphVertex elementV, T toscaElementToUpdate, JsonParseFlagEnum flag) {
		fillMetadata(elementV, (TopologyTemplate) toscaElementToUpdate, flag);
	}

	private <T extends ToscaElement> StorageOperationStatus validateServiceCategory(T toscaElementToUpdate, GraphVertex elementV) {
		StorageOperationStatus status = StorageOperationStatus.OK;
		List<CategoryDefinition> newCategoryList = toscaElementToUpdate.getCategories();
		CategoryDefinition newCategory = newCategoryList.get(0);

		Either<GraphVertex, TitanOperationStatus> childVertex = titanDao.getChildVertex(elementV, EdgeLabelEnum.CATEGORY, JsonParseFlagEnum.NoParse);
		if (childVertex.isRight()) {
			log.debug("failed to fetch {} for tosca element with id {}, error {}", EdgeLabelEnum.CATEGORY, elementV.getUniqueId(), childVertex.right().value());
			return DaoStatusConverter.convertTitanStatusToStorageStatus(childVertex.right().value());
		}

		GraphVertex categoryV = childVertex.left().value();
		Map<GraphPropertyEnum, Object> metadataProperties = categoryV.getMetadataProperties();
		String categoryNameCurrent = (String) metadataProperties.get(GraphPropertyEnum.NAME);

		String newCategoryName = newCategory.getName();
		if (newCategoryName != null && false == newCategoryName.equals(categoryNameCurrent)) {
			// the category was changed
			Either<GraphVertex, StorageOperationStatus> getCategoryVertex = categoryOperation.getCategory(newCategoryName, VertexTypeEnum.SERVICE_CATEGORY);

			if (getCategoryVertex.isRight()) {
				return getCategoryVertex.right().value();
			}
			GraphVertex newCategoryV = getCategoryVertex.left().value();
			status = moveCategoryEdge(elementV, newCategoryV);
			log.debug("Going to update the category of the resource from {} to {}. status is {}", categoryNameCurrent, newCategory, status);
		}
		return status;
	}

	public Either<List<GraphVertex>, TitanOperationStatus> getAllNotDeletedElements() {
		Map<GraphPropertyEnum, Object> propsHasNot = new HashMap<>();
		propsHasNot.put(GraphPropertyEnum.IS_DELETED, true);

		Either<List<GraphVertex>, TitanOperationStatus> byCriteria = titanDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, null, propsHasNot, JsonParseFlagEnum.ParseJson);
		if (byCriteria.isRight()) {
			log.debug("Failed to fetch all non marked topology templates , propsHasNot {}, error {}", propsHasNot, byCriteria.right().value());
			return Either.right(byCriteria.right().value());
		}
		return Either.left(byCriteria.left().value());
	}

	public boolean isInUse(GraphVertex elementV, List<GraphVertex> allNonDeleted) {
		for (GraphVertex containerV : allNonDeleted) {
			Map<String, CompositionDataDefinition> composition = (Map<String, CompositionDataDefinition>) containerV.getJson();
			if (composition != null) {
				CompositionDataDefinition instances = composition.get(JsonConstantKeysEnum.COMPOSITION.getValue());
				if (instances != null && instances.getComponentInstances() != null && !instances.getComponentInstances().isEmpty()) {
					for (ComponentInstanceDataDefinition ci : instances.getComponentInstances().values()) {
						if (ci.getComponentUid().equals(elementV.getUniqueId())) {
							log.debug("The resource {} failed to delete cause in use as component instance UniqueID = {} in {} with UniqueID {}", elementV.getUniqueId(), ci.getUniqueId(), containerV.getType(), containerV.getUniqueId());
							return true;
						}
					}

				}
			}
		}

		return false;
	}

	public boolean isInUse(String componentId, List<GraphVertex> allNonDeleted) {
		for (GraphVertex containerV : allNonDeleted) {
			Map<String, CompositionDataDefinition> composition = (Map<String, CompositionDataDefinition>) containerV.getJson();
			if (composition != null) {
				CompositionDataDefinition instances = composition.get(JsonConstantKeysEnum.COMPOSITION.getValue());
				if (instances != null && instances.getComponentInstances() != null && !instances.getComponentInstances().isEmpty()) {
					for (ComponentInstanceDataDefinition ci : instances.getComponentInstances().values()) {
						if (ci.getComponentUid().equals(componentId)) {
							return true;
						}
					}

				}
			}
		}

		return false;
	}

	public Either<GraphVertex, StorageOperationStatus> updateDistributionStatus(String uniqueId, User user, DistributionStatusEnum distributionStatus) {

		Either<GraphVertex, StorageOperationStatus> result = null;
		String userId = user.getUserId();
		Either<GraphVertex, TitanOperationStatus> getRes = findUserVertex(userId);
		GraphVertex userVertex = null;
		GraphVertex serviceVertex = null;
		if (getRes.isRight()) {
			TitanOperationStatus status = getRes.right().value();
			CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Cannot find user {} in the graph. status is {}", userId, status);
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		if (result == null) {
			userVertex = getRes.left().value();
			getRes = titanDao.getVertexById(uniqueId, JsonParseFlagEnum.ParseMetadata);
			if (getRes.isRight()) {
				TitanOperationStatus status = getRes.right().value();
				log.error("Cannot find service {} in the graph. status is {}", uniqueId, status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}
		if (result == null) {
			serviceVertex = getRes.left().value();
			Iterator<Edge> edgeIterator = serviceVertex.getVertex().edges(Direction.IN, EdgeLabelEnum.LAST_DISTRIBUTION_STATE_MODIFIER.name());
			if (edgeIterator.hasNext()) {
				log.debug("Remove existing edge from user to component {}. Edge type is {}", userId, uniqueId, EdgeLabelEnum.LAST_DISTRIBUTION_STATE_MODIFIER);
				edgeIterator.next().remove();
			}
		}
		if (result == null) {
			TitanOperationStatus status = titanDao.createEdge(userVertex, serviceVertex, EdgeLabelEnum.LAST_DISTRIBUTION_STATE_MODIFIER, null);
			if (status != TitanOperationStatus.OK) {
				log.error("Failed to associate user {} to component {}. Edge type is {}", userId, uniqueId, EdgeLabelEnum.LAST_DISTRIBUTION_STATE_MODIFIER);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}
		if (result == null) {
			serviceVertex.addMetadataProperty(GraphPropertyEnum.DISTRIBUTION_STATUS, distributionStatus.name());
			long lastUpdateDate = System.currentTimeMillis();
			serviceVertex.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, lastUpdateDate);
			Either<GraphVertex, TitanOperationStatus> updateRes = titanDao.updateVertex(serviceVertex);
			if (updateRes.isRight()) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(updateRes.right().value()));
			}
		}
		if (result == null) {
			result = Either.left(serviceVertex);
		}
		return result;
	}

}
