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

import com.google.gson.reflect.TypeToken;
import fj.data.Either;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.*;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapCapabilityProperty;
import org.openecomp.sdc.be.datatypes.elements.MapListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsontitan.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.jsontitan.enums.JsonConstantKeysEnum;
import org.openecomp.sdc.be.model.jsontitan.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.utils.ComponentUtilities;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility.LogLevelEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component("topology-template-operation")
public class TopologyTemplateOperation extends ToscaElementOperation {

    private static final Logger log = Logger.getLogger(TopologyTemplateOperation.class);

    @Autowired
    private ArchiveOperation archiveOperation;

    public Either<TopologyTemplate, StorageOperationStatus> createTopologyTemplate(TopologyTemplate topologyTemplate) {
        Either<TopologyTemplate, StorageOperationStatus> result = null;

        topologyTemplate.generateUUID();

        topologyTemplate = getResourceMetaDataFromResource(topologyTemplate);
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
            log.debug( "Error returned after creating topology template data node {}. status returned is ", topologyTemplateVertex, status);
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
        StorageOperationStatus associatePolicies = associatePoliciesToComponent(topologyTemplateVertex, topologyTemplate);
        if (associatePolicies != StorageOperationStatus.OK) {
            result = Either.right(associatePolicies);
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

        StorageOperationStatus associateInterfaces = associateInterfacesToComponent(topologyTemplateVertex, topologyTemplate);
        if (associateInterfaces != StorageOperationStatus.OK) {
            result = Either.right(associateInterfaces);
        return result;
        }

        StorageOperationStatus associatePathProperties = associateForwardingPathToResource(topologyTemplateVertex, topologyTemplate);
        if (associateCapProperties != StorageOperationStatus.OK) {
            result = Either.right(associatePathProperties);
            return result;
        }


        return Either.left(topologyTemplate);

    }

    private StorageOperationStatus associatePoliciesToComponent(GraphVertex nodeTypeVertex, TopologyTemplate topologyTemplate) {
        return associatePoliciesToComponent(nodeTypeVertex, topologyTemplate.getPolicies());
    }

    private StorageOperationStatus associatePoliciesToComponent(GraphVertex nodeTypeVertex,    Map<String, PolicyDataDefinition> policies) {
        if (policies != null && !policies.isEmpty()) {
            policies.values().stream().filter(p -> p.getUniqueId() == null).forEach(p -> {
                String uid = UniqueIdBuilder.buildGroupingUid(nodeTypeVertex.getUniqueId(), p.getName());
                p.setUniqueId(uid);
            });
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.POLICIES, EdgeLabelEnum.POLICIES, policies);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
        }
        return StorageOperationStatus.OK;
    }

    private StorageOperationStatus associateForwardingPathToResource(GraphVertex topologyTemplateVertex, TopologyTemplate topologyTemplate) {
        Map<String, ForwardingPathDataDefinition> forwardingPaths = topologyTemplate.getForwardingPaths();
        return associateForwardingPathToComponent(topologyTemplateVertex,forwardingPaths);
    }

    private StorageOperationStatus associateCapPropertiesToResource(GraphVertex topologyTemplateVertex, TopologyTemplate topologyTemplate) {
        Map<String, MapCapabilityProperty> calculatedCapProperties = topologyTemplate.getCalculatedCapabilitiesProperties();
        if (calculatedCapProperties != null && !calculatedCapProperties.isEmpty()) {
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(topologyTemplateVertex, VertexTypeEnum.CALCULATED_CAP_PROPERTIES, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, calculatedCapProperties);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
        }
        return StorageOperationStatus.OK;
    }

    private StorageOperationStatus associateCapabilitiesToResource(GraphVertex nodeTypeVertex, TopologyTemplate topologyTemplate) {
        Map<String, MapListCapabilityDataDefinition> calculatedCapabilities = topologyTemplate.getCalculatedCapabilities();
        if (calculatedCapabilities != null && !calculatedCapabilities.isEmpty()) {
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.CALCULATED_CAPABILITIES, EdgeLabelEnum.CALCULATED_CAPABILITIES, calculatedCapabilities);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
        }
        Map<String, MapListCapabilityDataDefinition> fullfilledCapabilities = topologyTemplate.getFullfilledCapabilities();
        if (fullfilledCapabilities != null && !fullfilledCapabilities.isEmpty()) {
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.FULLFILLED_CAPABILITIES, EdgeLabelEnum.FULLFILLED_CAPABILITIES, fullfilledCapabilities);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
        }
        return StorageOperationStatus.OK;

    }

    private StorageOperationStatus associateRequirementsToResource(GraphVertex nodeTypeVertex, TopologyTemplate topologyTemplate) {
        Map<String, MapListRequirementDataDefinition> calculatedRequirements = topologyTemplate.getCalculatedRequirements();
        if (calculatedRequirements != null && !calculatedRequirements.isEmpty()) {
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.CALCULATED_REQUIREMENTS, EdgeLabelEnum.CALCULATED_REQUIREMENTS, calculatedRequirements);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
        }
        Map<String, MapListRequirementDataDefinition> fullfilledRequirements = topologyTemplate.getFullfilledRequirements();
        if (fullfilledRequirements != null && !fullfilledRequirements.isEmpty()) {
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.FULLFILLED_REQUIREMENTS, EdgeLabelEnum.FULLFILLED_REQUIREMENTS, fullfilledRequirements);
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
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.SERVICE_API_ARTIFACTS, EdgeLabelEnum.SERVICE_API_ARTIFACTS, addInformation);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
        }
        Map<String, MapArtifactDataDefinition> instArtifacts = topologyTemplate.getInstDeploymentArtifacts();

        if (instArtifacts != null && !instArtifacts.isEmpty()) {
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.INST_DEPLOYMENT_ARTIFACTS, EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS, instArtifacts);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
        }
        Map<String, MapArtifactDataDefinition> instInfoArtifacts = topologyTemplate.getInstanceArtifacts();

        if (instInfoArtifacts != null && !instInfoArtifacts.isEmpty()) {
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.INSTANCE_ARTIFACTS, EdgeLabelEnum.INSTANCE_ARTIFACTS, instInfoArtifacts);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
        }
        return StorageOperationStatus.OK;
    }

    private StorageOperationStatus addAdditionalInformationToResource(GraphVertex nodeTypeVertex, TopologyTemplate topologyTemplate) {

        Map<String, AdditionalInfoParameterDataDefinition> addInformation = topologyTemplate.getAdditionalInformation();

        if (addInformation != null && !addInformation.isEmpty()) {
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.ADDITIONAL_INFORMATION, EdgeLabelEnum.ADDITIONAL_INFORMATION, addInformation);
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
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.INST_PROPERTIES, EdgeLabelEnum.INST_PROPERTIES, instProps);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
        }
        return StorageOperationStatus.OK;
    }

    public StorageOperationStatus associateInstInputsToComponent(GraphVertex nodeTypeVertex, Map<String, MapPropertiesDataDefinition> instInputs) {
        if (instInputs != null && !instInputs.isEmpty()) {
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.INST_INPUTS, EdgeLabelEnum.INST_INPUTS, instInputs);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
        }
        return StorageOperationStatus.OK;
    }

    public StorageOperationStatus associateInstGroupsToComponent(GraphVertex nodeTypeVertex, Map<String, MapGroupsDataDefinition> instGroups) {
        if (instGroups != null && !instGroups.isEmpty()) {
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.INST_GROUPS, EdgeLabelEnum.INST_GROUPS, instGroups);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
        }
        return StorageOperationStatus.OK;
    }


    public StorageOperationStatus deleteInstInputsToComponent(GraphVertex nodeTypeVertex, Map<String, MapPropertiesDataDefinition> instInputs) {

        if (instInputs != null && !instInputs.isEmpty()) {
            instInputs.entrySet().forEach(i -> {
                List<String> uniqueKeys = new ArrayList<>(i.getValue().getMapToscaDataDefinition().keySet());
                List<String> pathKeys = new ArrayList<>();
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
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, vertexType, edgeLabel, instProps);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
        }
        return StorageOperationStatus.OK;
    }

    public StorageOperationStatus associateOrAddCalcCapReqToComponent(GraphVertex nodeTypeVertex, Map<String, MapListRequirementDataDefinition> calcRequirements, Map<String, MapListCapabilityDataDefinition> calcCapabilty, Map<String, MapCapabilityProperty> calculatedCapabilitiesProperties) {
        if (calcRequirements != null && !calcRequirements.isEmpty()) {
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateOrAddElementToData(nodeTypeVertex, VertexTypeEnum.CALCULATED_REQUIREMENTS, EdgeLabelEnum.CALCULATED_REQUIREMENTS, calcRequirements);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
            Map<String, MapListRequirementDataDefinition> fullFilled = new HashMap<>();
            assosiateElementToData = associateOrAddElementToData(nodeTypeVertex, VertexTypeEnum.FULLFILLED_REQUIREMENTS, EdgeLabelEnum.FULLFILLED_REQUIREMENTS, fullFilled);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
        }
        if (calcCapabilty != null && !calcCapabilty.isEmpty()) {
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateOrAddElementToData(nodeTypeVertex, VertexTypeEnum.CALCULATED_CAPABILITIES ,EdgeLabelEnum.CALCULATED_CAPABILITIES, calcCapabilty);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
            Map<String, MapListCapabilityDataDefinition> fullFilled = new HashMap<>();
            assosiateElementToData = associateOrAddElementToData(nodeTypeVertex, VertexTypeEnum.FULLFILLED_CAPABILITIES, EdgeLabelEnum.FULLFILLED_CAPABILITIES, fullFilled);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
        }
        if ( calculatedCapabilitiesProperties != null && !calculatedCapabilitiesProperties.isEmpty() ){
            return associateOrAddElementToData(nodeTypeVertex, VertexTypeEnum.CALCULATED_CAP_PROPERTIES,
                    EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, calculatedCapabilitiesProperties)
                    .right()
                    .on(v -> StorageOperationStatus.OK);
        }
        return StorageOperationStatus.OK;
    }

    private <T extends MapDataDefinition> Either<GraphVertex, StorageOperationStatus> associateOrAddElementToData(GraphVertex nodeTypeVertex, VertexTypeEnum vertexTypeEnum, EdgeLabelEnum edgeLabelEnum, Map<String, T> dataMap){
        return titanDao.getChildVertex(nodeTypeVertex, edgeLabelEnum, JsonParseFlagEnum.ParseJson)
                .either(dataVertex -> addElementsToComponent(nodeTypeVertex, dataVertex, vertexTypeEnum, edgeLabelEnum, dataMap),
                        status -> associateElementToDataIfNotFound(status, nodeTypeVertex, vertexTypeEnum, edgeLabelEnum, dataMap));
    }
    
    private Either<GraphVertex, StorageOperationStatus> associateElementToDataIfNotFound(TitanOperationStatus status, GraphVertex nodeTypeVertex, VertexTypeEnum vertexTypeEnum, EdgeLabelEnum edgeLabelEnum, Map<String, ? extends ToscaDataDefinition> dataMap) {
        if(status == TitanOperationStatus.NOT_FOUND){
            return associateElementToData(nodeTypeVertex, vertexTypeEnum, edgeLabelEnum, dataMap);
        }
        return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
    }

    private <T extends MapDataDefinition> Either<GraphVertex, StorageOperationStatus> addElementsToComponent(GraphVertex nodeTypeVertex, GraphVertex dataVertex, VertexTypeEnum vertexTypeEnum, EdgeLabelEnum edgeLabelEnum, Map<String, T> dataMap) {
        Optional<StorageOperationStatus> error = dataMap.entrySet()
                .stream()
                .map(e -> addElementToComponent(nodeTypeVertex.getUniqueId(), vertexTypeEnum, edgeLabelEnum, e))
                .filter(s -> s != StorageOperationStatus.OK)
                .findFirst();
        if(error.isPresent()){
            return Either.right(error.get());
        }
        return Either.left(dataVertex);
    }

    private StorageOperationStatus associateInstAttributesToComponent(GraphVertex nodeTypeVertex, TopologyTemplate topologyTemplate) {
        Map<String, MapPropertiesDataDefinition> instAttr = topologyTemplate.getInstAttributes();
        return associateInstAttributeToComponent(nodeTypeVertex, instAttr);
    }

    public StorageOperationStatus associateForwardingPathToComponent(GraphVertex nodeTypeVertex, Map<String, ForwardingPathDataDefinition> forwardingPathMap) {
        if (forwardingPathMap != null && !forwardingPathMap.isEmpty()) {
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.FORWARDING_PATH, EdgeLabelEnum.FORWARDING_PATH, forwardingPathMap);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
        }
        return StorageOperationStatus.OK;
    }

    public StorageOperationStatus associateInstAttributeToComponent(GraphVertex nodeTypeVertex, Map<String, MapPropertiesDataDefinition> instAttr) {
        if (instAttr != null && !instAttr.isEmpty()) {
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.INST_ATTRIBUTES, EdgeLabelEnum.INST_ATTRIBUTES, instAttr);
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
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.GROUPS, EdgeLabelEnum.GROUPS, groups);
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

            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.INPUTS, EdgeLabelEnum.INPUTS, inputs);
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

        Either<GraphVertex, StorageOperationStatus> componentByLabelAndId = getComponentByLabelAndId(uniqueId, ToscaElementTypeEnum.TOPOLOGY_TEMPLATE, parseFlag);
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
        TitanOperationStatus status;
        if (!componentParametersView.isIgnoreUsers()) {
            status = setCreatorFromGraph(componentV, toscaElement);
            if (status != TitanOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
            }

            status = setLastModifierFromGraph(componentV, toscaElement);
            if (status != TitanOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
            }
        }
        if (!componentParametersView.isIgnoreCategories()) {
            status = setTopologyTempalteCategoriesFromGraph(componentV, toscaElement);
            if (status != TitanOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));

            }
        }
        if (!componentParametersView.isIgnoreArtifacts()) {
            TitanOperationStatus storageStatus = setAllArtifactsFromGraph(componentV, toscaElement);
            if (storageStatus != TitanOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(storageStatus));
            }
        }
        if (!componentParametersView.isIgnoreComponentInstancesProperties()) {
            status = setComponentInstancesPropertiesFromGraph(componentV, toscaElement);
            if (status != TitanOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
            }
        }
        if (!componentParametersView.isIgnoreCapabilities()) {
            status = setCapabilitiesFromGraph(componentV, toscaElement);
            if (status != TitanOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
            }
        }
        if (!componentParametersView.isIgnoreRequirements()) {
            status = setRequirementsFromGraph(componentV, toscaElement);
            if (status != TitanOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
            }
        }
        if (!componentParametersView.isIgnoreAllVersions()) {
            status = setAllVersions(componentV, toscaElement);
            if (status != TitanOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
            }
        }
        if (!componentParametersView.isIgnoreAdditionalInformation()) {
            status = setAdditionalInformationFromGraph(componentV, toscaElement);
            if (status != TitanOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
            }
        }

        if (!componentParametersView.isIgnoreGroups()) {
            status = setGroupsFromGraph(componentV, toscaElement);
            if (status != TitanOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
            }

        }
        if (!componentParametersView.isIgnorePolicies()) {
            status = setPoliciesFromGraph(componentV, toscaElement);
            if (status != TitanOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
            }

        }
        if (!componentParametersView.isIgnoreComponentInstances()) {
            status = setInstGroupsFromGraph(componentV, toscaElement);

            //Mark all CIs that has archived origins
            archiveOperation.setArchivedOriginsFlagInComponentInstances(componentV);

            if (status != TitanOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
            }

        }
        if (!componentParametersView.isIgnoreInputs()) {
            status = setInputsFromGraph(componentV, toscaElement);
            if (status != TitanOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
            }

        }
        if (!componentParametersView.isIgnoreProperties()) {
            status = setPropertiesFromGraph(componentV, toscaElement);
            if (status != TitanOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
            }

        }

        if (!componentParametersView.isIgnoreComponentInstancesInputs()) {
            status = setComponentInstancesInputsFromGraph(componentV, toscaElement);
            if (status != TitanOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));

            }
        }

        if (!componentParametersView.isIgnoreCapabiltyProperties()) {
            status = setComponentInstancesCapPropertiesFromGraph(componentV, toscaElement);
            if (status != TitanOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));

            }
        }

        if (!componentParametersView.isIgnoreForwardingPath()) {
            status = setForwardingGraphPropertiesFromGraph(componentV, toscaElement);
            if (status != TitanOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));

            }
        }
        
        if (!componentParametersView.isIgnoreInterfaces()) {
            TitanOperationStatus storageStatus = setInterfcesFromGraph(componentV, toscaElement);
            if (storageStatus != TitanOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(storageStatus));
                
            }
        }

        return Either.left(toscaElement);
    }

    private TitanOperationStatus setInterfcesFromGraph(GraphVertex componentV, TopologyTemplate topologyTemplate) {
      Either<Map<String, InterfaceDataDefinition>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.INTERFACE);
      if (result.isLeft()) {
        topologyTemplate.setInterfaces(result.left().value());
      } else {
        if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
          return result.right().value();
        }
      }
      return TitanOperationStatus.OK;
    }

    private StorageOperationStatus associateInterfacesToComponent(GraphVertex topologyTemplateVertex, TopologyTemplate topologyTemplate) {
      Map<String, InterfaceDataDefinition> interfaceMap = topologyTemplate.getInterfaces();
      if (interfaceMap != null && !interfaceMap.isEmpty()) {
        Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(topologyTemplateVertex, VertexTypeEnum.INTERFACE, EdgeLabelEnum.INTERFACE, interfaceMap);
        if (assosiateElementToData.isRight()) {
          return assosiateElementToData.right().value();
        }
        else {
          Map<String, OperationDataDefinition> operationMap = interfaceMap.values().stream().filter(op -> MapUtils.isNotEmpty(op.getOperations())).flatMap(a -> a.getOperations().entrySet().stream()).collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
          if(MapUtils.isNotEmpty(operationMap)) {
            Either<GraphVertex, StorageOperationStatus> assosiateOpToInterface = associateElementToData(assosiateElementToData.left().value(), VertexTypeEnum.INTERFACE_OPERATION, EdgeLabelEnum.INTERFACE_OPERATION, operationMap);
            if (assosiateOpToInterface.isRight()) {
              return assosiateOpToInterface.right().value();
            }
          }
        }
      }
      return StorageOperationStatus.OK;
    }

    private TitanOperationStatus setPoliciesFromGraph(GraphVertex componentV, TopologyTemplate toscaElement) {
        Either<Map<String, PolicyDataDefinition>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.POLICIES);
        if (result.isLeft()) {
            toscaElement.setPolicies(result.left().value());
        } else {
            if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
                return result.right().value();
            }
        }
        return TitanOperationStatus.OK;
    }

    private TitanOperationStatus setForwardingGraphPropertiesFromGraph(GraphVertex componentV, TopologyTemplate topologyTemplate) {
        Either<Map<String, ForwardingPathDataDefinition>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.FORWARDING_PATH);
        if (result.isLeft()) {
            topologyTemplate.setForwardingPaths(result.left().value());
        } else {
            if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
                return result.right().value();
            }
        }
        return TitanOperationStatus.OK;
    }


    private TitanOperationStatus setComponentInstancesCapPropertiesFromGraph(GraphVertex componentV, TopologyTemplate topologyTemplate) {
        Either<Map<String, MapCapabilityProperty>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES);
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
        Either<Map<String, MapListCapabilityDataDefinition>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.CALCULATED_CAPABILITIES);
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

    @SuppressWarnings("unchecked")
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
        status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.FORWARDING_PATH);
        if (status != TitanOperationStatus.OK) {
            log.debug("Failed to disassociate service api artifacts for {} error {}", toscaElementVertex.getUniqueId(), status);
            Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
        }

        Either<GraphVertex, TitanOperationStatus> getInterfaceVertex = titanDao.getChildVertex(toscaElementVertex, EdgeLabelEnum.INTERFACE, JsonParseFlagEnum.NoParse);
        if (getInterfaceVertex.isLeft()) {
            status = titanDao.disassociateAndDeleteLast(getInterfaceVertex.left().value(), Direction.OUT, EdgeLabelEnum.INTERFACE_OPERATION);
            if (status != TitanOperationStatus.OK) {
                log.debug("Failed to disassociate interface operations for {} error {}", getInterfaceVertex.left().value().getUniqueId(), status);
                Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
            }
            else {
                status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.INTERFACE);
                if (status != TitanOperationStatus.OK) {
                    log.debug("Failed to disassociate interfaces for {} error {}", toscaElementVertex.getUniqueId(), status);
                    Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
                }
            }

        }

        titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.INSTANCE_ARTIFACTS);
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
        if (newCategoryName != null && !newCategoryName.equals(categoryNameCurrent)) {
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
                log.debug( "Cannot find service {} in the graph. status is {}", uniqueId, status);
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
                log.debug( "Failed to associate user {} to component {}. Edge type is {}", userId, uniqueId, EdgeLabelEnum.LAST_DISTRIBUTION_STATE_MODIFIER);
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
    /**
     * Returns list of ComponentInstanceProperty belonging to component instance capability specified by name, type and ownerId
     * @param componentId
     * @param instanceId
     * @param capabilityName
     * @param capabilityType
     * @param ownerId
     * @return
     */
    public Either<List<ComponentInstanceProperty>, StorageOperationStatus> getComponentInstanceCapabilityProperties(String componentId, String instanceId, String capabilityName, String capabilityType, String ownerId) {

        Either<List<ComponentInstanceProperty>, StorageOperationStatus> result = null;
        Map<String, MapCapabilityProperty> mapPropertiesDataDefinition = null;
        Either<GraphVertex, StorageOperationStatus> componentByLabelAndId = getComponentByLabelAndId(componentId, ToscaElementTypeEnum.TOPOLOGY_TEMPLATE, JsonParseFlagEnum.NoParse);
        if (componentByLabelAndId.isRight()) {
            result = Either.right(componentByLabelAndId.right().value());
        }
        if(componentByLabelAndId.isLeft()){
            Either<Map<String, MapCapabilityProperty>, TitanOperationStatus> getDataRes = getDataFromGraph(componentByLabelAndId.left().value(), EdgeLabelEnum.CALCULATED_CAP_PROPERTIES);
            if (getDataRes.isRight()) {
                result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getDataRes.right().value()));
            } else {
                mapPropertiesDataDefinition = getDataRes.left().value();
            }
        }
        if(isNotEmptyMapOfProperties(instanceId, mapPropertiesDataDefinition)){
            result = Either.left(findComponentInstanceCapabilityProperties(instanceId, capabilityName, capabilityType, ownerId, mapPropertiesDataDefinition.get(instanceId).getMapToscaDataDefinition()));
        }
        return result;
    }

    public StorageOperationStatus updateComponentInstanceCapabilityProperties(Component containerComponent, String componentInstanceId, MapCapabilityProperty instanceProperties) {
        return updateToscaDataDeepElementsBlockToToscaElement(containerComponent.getUniqueId(), EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, instanceProperties, componentInstanceId);
    }


    private boolean isNotEmptyMapOfProperties(String instanceId, Map<String, MapCapabilityProperty> mapPropertiesDataDefinition) {
        return  MapUtils.isNotEmpty(mapPropertiesDataDefinition) &&
                mapPropertiesDataDefinition.get(instanceId) != null &&
                MapUtils.isNotEmpty(mapPropertiesDataDefinition.get(instanceId).getMapToscaDataDefinition());
    }

    private List<ComponentInstanceProperty> findComponentInstanceCapabilityProperties(String instanceId, String capabilityName, String capabilityType, String ownerId, Map<String, MapPropertiesDataDefinition> propertiesMap) {
        List<ComponentInstanceProperty> capPropsList = null;
        for(Entry<String, MapPropertiesDataDefinition> capProp : propertiesMap.entrySet()){
            if (isBelongingPropertyMap(instanceId, capabilityName, capabilityType, ownerId, capProp)) {
                Map<String, PropertyDataDefinition> capMap = capProp.getValue().getMapToscaDataDefinition();
                if (capMap != null && !capMap.isEmpty()) {
                    capPropsList = capMap.values().stream().map(ComponentInstanceProperty::new).collect(Collectors.toList());
                    break;
                }
            }
        }
        if(capPropsList == null){
            capPropsList = new ArrayList<>();
        }
        return capPropsList;
    }

    private boolean isBelongingPropertyMap(String instanceId, String capabilityName, String capabilityType, String ownerId, Entry<String, MapPropertiesDataDefinition> capProp) {
        if (capProp != null) {
            String[] path = capProp.getKey().split(ModelConverter.CAP_PROP_DELIM );
            if (path.length < 4) {
                log.debug("wrong key format for capabilty, key {}", capProp);
                return false;
            }
            return path[path.length - 2].equals(capabilityType) && path[path.length - 1].equals(capabilityName) && path[1].equals(ownerId) && path[0].equals(instanceId);
        }
        return false;
    }

    public StorageOperationStatus addPolicyToToscaElement(GraphVertex componentV, PolicyDefinition policyDefinition, int counter) {
        fillPolicyDefinition(componentV, policyDefinition, counter);
        return addToscaDataToToscaElement(componentV, EdgeLabelEnum.POLICIES, VertexTypeEnum.POLICIES, policyDefinition, JsonPresentationFields.UNIQUE_ID);
    }

    public StorageOperationStatus addPoliciesToToscaElement(GraphVertex componentV, List<PolicyDefinition> policies) {
        return addToscaDataToToscaElement(componentV, EdgeLabelEnum.POLICIES, VertexTypeEnum.POLICIES, policies, JsonPresentationFields.UNIQUE_ID);
    }

    public StorageOperationStatus updatePolicyOfToscaElement(GraphVertex componentV, PolicyDefinition policyDefinition) {
        return updateToscaDataOfToscaElement(componentV, EdgeLabelEnum.POLICIES, VertexTypeEnum.POLICIES, policyDefinition, JsonPresentationFields.UNIQUE_ID);
    }

    public StorageOperationStatus updatePoliciesOfToscaElement(GraphVertex componentV, List<PolicyDefinition> policiesDefinitions) {
        return updateToscaDataOfToscaElement(componentV, EdgeLabelEnum.POLICIES, VertexTypeEnum.POLICIES, policiesDefinitions, JsonPresentationFields.UNIQUE_ID);
    }

    public StorageOperationStatus removePolicyFromToscaElement(GraphVertex componentV, String policyId) {
        return  deleteToscaDataElement(componentV, EdgeLabelEnum.POLICIES, VertexTypeEnum.POLICIES, policyId, JsonPresentationFields.UNIQUE_ID);
    }

	public StorageOperationStatus updateGroupOfToscaElement(GraphVertex componentV, GroupDefinition groupDefinition) {
		return updateToscaDataOfToscaElement(componentV, EdgeLabelEnum.GROUPS, VertexTypeEnum.GROUPS, groupDefinition, JsonPresentationFields.NAME);
	}

	private void fillPolicyDefinition(GraphVertex componentV, PolicyDefinition policyDefinition, int counter) {
		String policyName = buildSubComponentName((String) componentV.getJsonMetadataField(JsonPresentationFields.NAME), policyDefinition.getPolicyTypeName(), counter);
		policyDefinition.setName(policyName);
		policyDefinition.setInvariantName(policyName);
		policyDefinition.setComponentName((String) componentV.getJsonMetadataField(JsonPresentationFields.NAME));
		policyDefinition.setUniqueId(UniqueIdBuilder.buildPolicyUniqueId(componentV.getUniqueId(), policyName));
		policyDefinition.setInvariantUUID(UniqueIdBuilder.buildInvariantUUID());
		policyDefinition.setPolicyUUID(UniqueIdBuilder.generateUUID());
	}
	
	public static String buildSubComponentName(String componentName, String subComponentTypeName, int counter) {
        String normalizedComponentName = ValidationUtils.normalizeComponentInstanceName(componentName);
		String typeSuffix = subComponentTypeName.substring(subComponentTypeName.lastIndexOf('.') + 1, subComponentTypeName.length());
		return normalizedComponentName + Constants.GROUP_POLICY_NAME_DELIMETER + typeSuffix + Constants.GROUP_POLICY_NAME_DELIMETER + counter;
	}

    void revertNamesOfCalculatedCapabilitiesRequirements(String componentId, TopologyTemplate toscaElement) {
        if(MapUtils.isNotEmpty(toscaElement.getComponentInstances()) || MapUtils.isNotEmpty(toscaElement.getGroups())){
            GraphVertex toscaElementV = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse)
                    .left()
                    .on(this::throwStorageException);
            if(MapUtils.isNotEmpty(toscaElement.getComponentInstances())){
                toscaElement.getComponentInstances().values().forEach(i -> revertNamesOfCalculatedCapabilitiesRequirements(toscaElement, i.getUniqueId()));
            }
            if(MapUtils.isNotEmpty(toscaElement.getGroups())){
                toscaElement.getGroups().values().forEach(g -> revertNamesOfCalculatedCapabilitiesRequirements(toscaElement, g.getUniqueId()));
            }
            topologyTemplateOperation.updateFullToscaData(toscaElementV, EdgeLabelEnum.CALCULATED_CAPABILITIES, VertexTypeEnum.CALCULATED_CAPABILITIES,  toscaElement.getCalculatedCapabilities());
            topologyTemplateOperation.updateFullToscaData(toscaElementV, EdgeLabelEnum.CALCULATED_REQUIREMENTS, VertexTypeEnum.CALCULATED_REQUIREMENTS,  toscaElement.getCalculatedRequirements());
            topologyTemplateOperation.updateFullToscaData(toscaElementV, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, VertexTypeEnum.CALCULATED_CAP_PROPERTIES,  toscaElement.getCalculatedCapabilitiesProperties());
        }
    }

    public void updateNamesOfCalculatedCapabilitiesRequirements(String componentId, TopologyTemplate toscaElement) {
        if(MapUtils.isNotEmpty(toscaElement.getComponentInstances()) || MapUtils.isNotEmpty(toscaElement.getGroups())){
            GraphVertex toscaElementV = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse)
                    .left()
                    .on(this::throwStorageException);
            if(MapUtils.isNotEmpty(toscaElement.getComponentInstances())){
                toscaElement.getComponentInstances().values().forEach(i -> updateNamesOfCalculatedCapabilitiesRequirements(toscaElement, i.getUniqueId(), i.getNormalizedName()));
            }
            if(MapUtils.isNotEmpty(toscaElement.getGroups())){
                toscaElement.getGroups().values().forEach(g -> updateNamesOfCalculatedCapabilitiesRequirements(toscaElement, g.getUniqueId(), g.getName()));
            }
            topologyTemplateOperation.updateFullToscaData(toscaElementV, EdgeLabelEnum.CALCULATED_CAPABILITIES, VertexTypeEnum.CALCULATED_CAPABILITIES,  toscaElement.getCalculatedCapabilities());
            topologyTemplateOperation.updateFullToscaData(toscaElementV, EdgeLabelEnum.CALCULATED_REQUIREMENTS, VertexTypeEnum.CALCULATED_REQUIREMENTS,  toscaElement.getCalculatedRequirements());
            topologyTemplateOperation.updateFullToscaData(toscaElementV, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, VertexTypeEnum.CALCULATED_CAP_PROPERTIES,  toscaElement.getCalculatedCapabilitiesProperties());
        }
    }

    private void updateNamesOfCalculatedCapabilitiesRequirements(TopologyTemplate toscaElement, String ownerId, String ownerName) {
        updateCalculatedCapabilitiesNames(toscaElement, ownerId, ownerName);
        updateCalculatedRequirementsNames(toscaElement, ownerId, ownerName);
        updateCalculatedCapabilitiesPropertiesKeys(toscaElement, ownerId);
    }

    private void updateCalculatedCapabilitiesPropertiesKeys(TopologyTemplate toscaElement, String ownerId) {
        if(toscaElement.getCalculatedCapabilitiesProperties() != null && toscaElement.getCalculatedCapabilitiesProperties().containsKey(ownerId)){
            MapCapabilityProperty newProps =  new MapCapabilityProperty();
            toscaElement.getCalculatedCapabilitiesProperties().get(ownerId)
                    .getMapToscaDataDefinition()
                    .forEach((k, v)-> updateAndAddCalculatedCapabilitiesProperties(k, v, toscaElement.getCalculatedCapabilities().get(ownerId), newProps));
            if(MapUtils.isNotEmpty(newProps.getMapToscaDataDefinition())) {
                toscaElement.getCalculatedCapabilitiesProperties().put(ownerId, newProps);
            }
        }
    }

    private void updateCalculatedRequirementsNames(TopologyTemplate toscaElement, String ownerId, String ownerName) {
        if(toscaElement.getCalculatedRequirements() != null && toscaElement.getCalculatedRequirements().containsKey(ownerId)){
            String prefix = ownerName + ".";
            toscaElement.getCalculatedRequirements().get(ownerId)
                .getMapToscaDataDefinition().values().stream()
                .flatMap(l -> l.getListToscaDataDefinition().stream())
                .forEach(r -> {
                    if(ComponentUtilities.isNotUpdatedCapReqName(prefix, r.getName(), r.getPreviousName())) {
                        if(StringUtils.isNotEmpty(r.getPreviousName())){
                            r.setParentName(r.getPreviousName());
                        }
                        r.setPreviousName(r.getName());
                    }
                    r.setName(prefix + r.getPreviousName());
                });
        }
    }

    private void updateCalculatedCapabilitiesNames(TopologyTemplate toscaElement, String ownerId, String ownerName) {
        if(toscaElement.getCalculatedCapabilities() != null && toscaElement.getCalculatedCapabilities().containsKey(ownerId)){
            String prefix = ownerName + ".";
            toscaElement.getCalculatedCapabilities().get(ownerId)
                .getMapToscaDataDefinition().values().stream()
                .flatMap(l -> l.getListToscaDataDefinition().stream())
                .forEach(c -> {
                    if(ComponentUtilities.isNotUpdatedCapReqName(prefix, c.getName(), c.getPreviousName())) {
                        if(StringUtils.isNotEmpty(c.getPreviousName())){
                            c.setParentName(c.getPreviousName());
                        }
                        c.setPreviousName(c.getName());
                    }
                    c.setName(prefix + c.getPreviousName());
                });
        }
    }

    private void updateAndAddCalculatedCapabilitiesProperties(String stringKey, MapPropertiesDataDefinition properties, MapListCapabilityDataDefinition calculatedCapabilities, MapCapabilityProperty newProps) {
        String[] key = stringKey.split(ModelConverter.CAP_PROP_DELIM);
        String capType = key[key.length - 2];
        String capName = key[key.length - 1];
        Optional<CapabilityDataDefinition> foundCapOpt = calculatedCapabilities.getMapToscaDataDefinition().get(capType)
                .getListToscaDataDefinition().stream()
                .filter(c -> c.getPreviousName().equals(capName))
                .findFirst();
        if(foundCapOpt.isPresent()){
            key[key.length - 1] = foundCapOpt.get().getName();
            newProps.put(buildCaLCapPropKey(key),properties);
        }
    }

    private void revertNamesOfCalculatedCapabilitiesRequirements(TopologyTemplate toscaElement, String ownerId) {
        revertCalculatedCapabilitiesPropertiesKeys(toscaElement, ownerId);
        revertCalculatedCapabilitiesNames(toscaElement, ownerId);
        revertCalculatedRequirementsNames(toscaElement, ownerId);
    }

    private void revertCalculatedCapabilitiesPropertiesKeys(TopologyTemplate toscaElement, String ownerId) {
        if(toscaElement.getCalculatedCapabilitiesProperties() != null && toscaElement.getCalculatedCapabilitiesProperties().containsKey(ownerId)){
            MapCapabilityProperty newProps =  new MapCapabilityProperty();
            toscaElement.getCalculatedCapabilitiesProperties().get(ownerId)
                    .getMapToscaDataDefinition()
                    .forEach((k,v) -> revertAndAddCalculatedCapabilitiesProperties(k, v, toscaElement.getCalculatedCapabilities().get(ownerId), newProps));
            if(MapUtils.isNotEmpty(newProps.getMapToscaDataDefinition())) {
                toscaElement.getCalculatedCapabilitiesProperties().put(ownerId, newProps);
            }
        }
    }

    private void revertCalculatedRequirementsNames(TopologyTemplate toscaElement, String ownerId) {
        if(toscaElement.getCalculatedRequirements() != null && toscaElement.getCalculatedRequirements().containsKey(ownerId)){
            toscaElement.getCalculatedRequirements().get(ownerId)
                    .getMapToscaDataDefinition().values().stream()
                    .flatMap(l -> l.getListToscaDataDefinition().stream())
                    .forEach(r -> {r.setName(r.getPreviousName());r.setPreviousName(r.getParentName());});
        }
    }

    private void revertCalculatedCapabilitiesNames(TopologyTemplate toscaElement, String ownerId) {
        if(toscaElement.getCalculatedCapabilities() != null && toscaElement.getCalculatedCapabilities().containsKey(ownerId)){
            toscaElement.getCalculatedCapabilities().get(ownerId)
                    .getMapToscaDataDefinition().values().stream()
                    .flatMap(l -> l.getListToscaDataDefinition().stream())
                    .forEach(c -> {c.setName(c.getPreviousName());c.setPreviousName(c.getParentName());});
        }
    }

    private void revertAndAddCalculatedCapabilitiesProperties(String stringKey, MapPropertiesDataDefinition properties, MapListCapabilityDataDefinition calculatedCapabilities, MapCapabilityProperty newProps) {
        String[] key = stringKey.split(ModelConverter.CAP_PROP_DELIM);
        String capType = key[key.length - 2];
        String capName = key[key.length - 1];
        Optional<CapabilityDataDefinition> foundCapOpt = calculatedCapabilities.getMapToscaDataDefinition().get(capType)
                .getListToscaDataDefinition().stream()
                .filter(c -> c.getName().equals(capName) && StringUtils.isNotEmpty(c.getPreviousName()))
                .findFirst();
        if(foundCapOpt.isPresent()){
            key[key.length - 1] = foundCapOpt.get().getPreviousName();
        }
        newProps.put(buildCaLCapPropKey(key), properties);
    }

    private String buildCaLCapPropKey(String[] keyArray) {
        StringBuilder key = new StringBuilder();
        for(int i = 0; i< keyArray.length; ++i){
            key.append(keyArray[i]);
            if(i < keyArray.length - 1){
                key.append(ModelConverter.CAP_PROP_DELIM);
            }
        }
        return key.toString();
    }

    private GraphVertex throwStorageException(TitanOperationStatus status) {
        throw new StorageException(status);
    }

}
