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

package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import fj.data.Either;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.*;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DerivedNodeTypeResolver;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.NodeType;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility.LogLevelEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component("node-type-operation")
public class NodeTypeOperation extends ToscaElementOperation {
	public final static Pattern uuidNewVersion = Pattern.compile("^\\d{1,}.1");
	public final static Pattern uuidNormativeNewVersion = Pattern.compile("^\\d{1,}.0");
    private static final Logger log = Logger.getLogger(NodeTypeOperation.class);
    private DerivedNodeTypeResolver derivedResourceResolver;

    
    public NodeTypeOperation(@Qualifier("derived-resource-resolver") DerivedNodeTypeResolver derivedNodeTypeResolver) {
        this.derivedResourceResolver = derivedNodeTypeResolver;
    }

    public Either<NodeType, StorageOperationStatus> createNodeType(NodeType nodeType) {

        Either<NodeType, StorageOperationStatus> result = null;

        nodeType.generateUUID();

		//Set missing props such as names, default lifecycle state, dates etc...
        nodeType = getResourceMetaDataFromResource(nodeType);

		//Set unique ID
        String resourceUniqueId = nodeType.getUniqueId();
        if (resourceUniqueId == null) {
            resourceUniqueId = UniqueIdBuilder.buildResourceUniqueId();
            nodeType.setUniqueId(resourceUniqueId);
        }

        // get derived from resources
        List<GraphVertex> derivedResources = null;
        Either<List<GraphVertex>, StorageOperationStatus> derivedResourcesResult = findDerivedResources(nodeType);
        if (derivedResourcesResult.isRight()) {
            result = Either.right(derivedResourcesResult.right().value());
            return result;
        } else {
            derivedResources = derivedResourcesResult.left().value();
        }

		//Create Vertext Object and fill according to given NodeType
        GraphVertex nodeTypeVertex = new GraphVertex(VertexTypeEnum.NODE_TYPE);
        fillToscaElementVertexData(nodeTypeVertex, nodeType, JsonParseFlagEnum.ParseAll);

		//Create Node Type in Graph
        Either<GraphVertex, JanusGraphOperationStatus> createdVertex = janusGraphDao.createVertex(nodeTypeVertex);
        if (createdVertex.isRight()) {
            JanusGraphOperationStatus status = createdVertex.right().value();
            log.error("Error returned after creating resource data node {}. status returned is ", nodeTypeVertex, status);
            result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            return result;
        }
        nodeTypeVertex = createdVertex.left().value();

        StorageOperationStatus assosiateCommon = assosiateCommonForToscaElement(nodeTypeVertex, nodeType, derivedResources);
        if (assosiateCommon != StorageOperationStatus.OK) {
            result = Either.right(assosiateCommon);
            return result;
        }

        StorageOperationStatus associateDerived = assosiateToDerived(nodeTypeVertex, derivedResources);
        if (associateDerived != StorageOperationStatus.OK) {
            result = Either.right(associateDerived);
            return result;
        }
        StorageOperationStatus associateCategory = assosiateResourceMetadataToCategory(nodeTypeVertex, nodeType);
        if (associateCategory != StorageOperationStatus.OK) {
            result = Either.right(associateCategory);
            return result;
        }

        StorageOperationStatus associateAttributes = associateAttributesToResource(nodeTypeVertex, nodeType, derivedResources);
        if (associateAttributes != StorageOperationStatus.OK) {
            result = Either.right(associateAttributes);
            return result;
        }

        StorageOperationStatus associateRequirements = associateRequirementsToResource(nodeTypeVertex, nodeType, derivedResources);
        if (associateRequirements != StorageOperationStatus.OK) {
            result = Either.right(associateRequirements);
            return result;
        }

        StorageOperationStatus associateCapabilities = associateCapabilitiesToResource(nodeTypeVertex, nodeType, derivedResources);
        if (associateCapabilities != StorageOperationStatus.OK) {
            result = Either.right(associateCapabilities);
            return result;
        }
        StorageOperationStatus associateCapabilitiesProps = associateCapabilitiesPropertiesToResource(nodeTypeVertex, nodeType, derivedResources);
        if (associateCapabilitiesProps != StorageOperationStatus.OK) {
            result = Either.right(associateCapabilitiesProps);
            return result;
        }

        StorageOperationStatus associateInterfaces = associateInterfacesToResource(nodeTypeVertex, nodeType, derivedResources);
        if (associateInterfaces != StorageOperationStatus.OK) {
            result = Either.right(associateInterfaces);
            return result;
        }

        StorageOperationStatus addAdditionalInformation = addAdditionalInformationToResource(nodeTypeVertex, nodeType, derivedResources);
        if (addAdditionalInformation != StorageOperationStatus.OK) {
            result = Either.right(addAdditionalInformation);
            return result;
        }
        result = Either.left(nodeType);
        return result;

    }

    private StorageOperationStatus associateInterfacesToResource(GraphVertex nodeTypeVertex, NodeType nodeType, List<GraphVertex> derivedResources) {
        // Note : currently only one derived supported!!!!
        Either<Map<String, InterfaceDataDefinition>, StorageOperationStatus> dataFromDerived = getDataFromDerived(derivedResources, EdgeLabelEnum.INTERFACE_ARTIFACTS);
        if (dataFromDerived.isRight()) {
            return dataFromDerived.right().value();
        }
        Map<String, InterfaceDataDefinition> interfacArtsAll = dataFromDerived.left().value();

        Map<String, InterfaceDataDefinition> interfacArts = nodeType.getInterfaceArtifacts();
        if (interfacArts != null) {
            interfacArtsAll.putAll(interfacArts);
        }
        if (!interfacArtsAll.isEmpty()) {
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.INTERFACE_ARTIFACTS, EdgeLabelEnum.INTERFACE_ARTIFACTS, interfacArtsAll);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
        }
        return StorageOperationStatus.OK;
    }

    @Override
    public Either<ToscaElement, StorageOperationStatus> getToscaElement(String uniqueId, ComponentParametersView componentParametersView) {

        Either<GraphVertex, StorageOperationStatus> componentByLabelAndId = getComponentByLabelAndId(uniqueId, ToscaElementTypeEnum.NODE_TYPE, JsonParseFlagEnum.ParseMetadata);
        if (componentByLabelAndId.isRight()) {
            return Either.right(componentByLabelAndId.right().value());
        }
        GraphVertex componentV = componentByLabelAndId.left().value();

        return getToscaElement(componentV, componentParametersView);

    }

    // -------------------------------------------------------------
    @Override
    public Either<ToscaElement, StorageOperationStatus> getToscaElement(GraphVertex componentV, ComponentParametersView componentParametersView) {
        NodeType toscaElement;
        toscaElement = convertToComponent(componentV);
        JanusGraphOperationStatus status = null;
        if (!componentParametersView.isIgnoreUsers()) {
            status = setCreatorFromGraph(componentV, toscaElement);
            if (status != JanusGraphOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            }

            status = setLastModifierFromGraph(componentV, toscaElement);
            if (status != JanusGraphOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            }
        }

        if (!componentParametersView.isIgnoreProperties()) {
            status = setResourcePropertiesFromGraph(componentV, toscaElement);
            if (status != JanusGraphOperationStatus.OK && status != JanusGraphOperationStatus.NOT_FOUND) {
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            }
        }

        if (!componentParametersView.isIgnoreAttributesFrom()) {
            status = setResourceAttributesFromGraph(componentV, toscaElement);
            if (status != JanusGraphOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            }
        }

        if (!componentParametersView.isIgnoreDerivedFrom()) {
            status = setResourceDerivedFromGraph(componentV, toscaElement);
            if (status != JanusGraphOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            }
        }

        if (!componentParametersView.isIgnoreCategories()) {
            status = setResourceCategoryFromGraph(componentV, toscaElement);
            if (status != JanusGraphOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            }
        }
        if (!componentParametersView.isIgnoreRequirements()) {
            status = setResourceRequirementsFromGraph(componentV, toscaElement);
            if (status != JanusGraphOperationStatus.OK) {
                log.error("Failed to set requirement of resource {}. status is {}", componentV.getUniqueId(), status);
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            }
        }
        if (!componentParametersView.isIgnoreCapabilities()) {
            status = setResourceCapabilitiesFromGraph(componentV, toscaElement);
            if (status != JanusGraphOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            }
        }

        if (!componentParametersView.isIgnoreArtifacts()) {
            status = setArtifactsFromGraph(componentV, toscaElement);
            if (status != JanusGraphOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            }
        }
        if (!componentParametersView.isIgnoreAdditionalInformation()) {
            status = setAdditionalInformationFromGraph(componentV, toscaElement);
            if (status != JanusGraphOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            }
        }
        if (!componentParametersView.isIgnoreInterfaces()) {
            status = setInterfacesFromGraph(componentV, toscaElement);
            if (status != JanusGraphOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            }
        }
        if (!componentParametersView.isIgnoreAllVersions()) {
            status = setAllVersions(componentV, toscaElement);
            if (status != JanusGraphOperationStatus.OK && status != JanusGraphOperationStatus.NOT_FOUND) {
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            }
        }

        if (!componentParametersView.isIgnoreCapabiltyProperties()) {
            status = setComponentCapPropertiesFromGraph(componentV, toscaElement);
            if (status != JanusGraphOperationStatus.OK) {
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));

            }
        }
        return Either.left(toscaElement);
    }

    private JanusGraphOperationStatus setComponentCapPropertiesFromGraph(GraphVertex componentV, NodeType toscaElement) {
        Either<Map<String, MapPropertiesDataDefinition>, JanusGraphOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.CAPABILITIES_PROPERTIES);
        if (result.isLeft()) {
            toscaElement.setCapabilitiesProperties(result.left().value());
        } else {
            if (result.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
                return result.right().value();
            }
        }
        return JanusGraphOperationStatus.OK;
    }

    private JanusGraphOperationStatus setInterfacesFromGraph(GraphVertex componentV, NodeType toscaElement) {
        Either<Map<String, InterfaceDataDefinition>, JanusGraphOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.INTERFACE_ARTIFACTS);
        if (result.isLeft()) {
            toscaElement.setInterfaceArtifacts(result.left().value());
        } else {
            if (result.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
                return result.right().value();
            }
        }
        return JanusGraphOperationStatus.OK;
    }

    protected <T extends ToscaElement> JanusGraphOperationStatus setCapabilitiesFromGraph(GraphVertex componentV, T toscaElement) {
        return setResourceCapabilitiesFromGraph(componentV, (NodeType) toscaElement);
    }

    private JanusGraphOperationStatus setResourceCapabilitiesFromGraph(GraphVertex componentV, NodeType toscaElement) {
        Either<Map<String, ListCapabilityDataDefinition>, JanusGraphOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.CAPABILITIES);
        if (result.isLeft()) {
            toscaElement.setCapabilities(result.left().value());
        } else {
            if (result.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
                return result.right().value();
            }
        }
        return JanusGraphOperationStatus.OK;
    }

    private JanusGraphOperationStatus setResourceDerivedFromGraph(GraphVertex componentV, NodeType toscaElement) {
        List<String> derivedFromList = new ArrayList<>();

        JanusGraphOperationStatus
            listFromGraphStatus = findResourcesPathRecursively(componentV, derivedFromList);
        if (JanusGraphOperationStatus.OK != listFromGraphStatus) {
            return listFromGraphStatus;
        }

        if (!derivedFromList.isEmpty()) {
            if (derivedFromList.size() > 1) {
                List<String> lastDerivedFrom = new ArrayList<>();
                lastDerivedFrom.add(derivedFromList.get(1));
                toscaElement.setDerivedFrom(lastDerivedFrom);
                toscaElement.setDerivedList(derivedFromList);
            } else {
                toscaElement.setDerivedFrom(null);
                toscaElement.setDerivedList(derivedFromList);
            }

        }
        return JanusGraphOperationStatus.OK;
    }

    protected JanusGraphOperationStatus findResourcesPathRecursively(GraphVertex nodeTypeV, List<String> resourcesPathList) {
        Either<GraphVertex, JanusGraphOperationStatus> parentResourceRes = janusGraphDao
            .getChildVertex(nodeTypeV, EdgeLabelEnum.DERIVED_FROM, JsonParseFlagEnum.NoParse);
        resourcesPathList.add((String) nodeTypeV.getMetadataProperty(GraphPropertyEnum.TOSCA_RESOURCE_NAME));
        while (parentResourceRes.isLeft()) {

            GraphVertex parent = parentResourceRes.left().value();
            resourcesPathList.add((String) parent.getMetadataProperty(GraphPropertyEnum.TOSCA_RESOURCE_NAME));
            parentResourceRes = janusGraphDao
                .getChildVertex(parent, EdgeLabelEnum.DERIVED_FROM, JsonParseFlagEnum.NoParse);
        }
        JanusGraphOperationStatus operationStatus = parentResourceRes.right().value();

        if (operationStatus != JanusGraphOperationStatus.NOT_FOUND) {
            return operationStatus;
        } else {
            return JanusGraphOperationStatus.OK;
        }

    }

    protected <T extends ToscaElement> JanusGraphOperationStatus setRequirementsFromGraph(GraphVertex componentV, T toscaElement) {
        return setResourceRequirementsFromGraph(componentV, (NodeType) toscaElement);
    }

    private JanusGraphOperationStatus setResourceRequirementsFromGraph(GraphVertex componentV, NodeType toscaElement) {
        Either<Map<String, ListRequirementDataDefinition>, JanusGraphOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.REQUIREMENTS);
        if (result.isLeft()) {
            toscaElement.setRequirements(result.left().value());
        } else {
            if (result.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
                return result.right().value();
            }
        }
        return JanusGraphOperationStatus.OK;
    }

    private JanusGraphOperationStatus setResourceAttributesFromGraph(GraphVertex componentV, NodeType toscaElement) {
        Either<Map<String, PropertyDataDefinition>, JanusGraphOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.ATTRIBUTES);
        if (result.isLeft()) {
            toscaElement.setAttributes(result.left().value());
        } else {
            if (result.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
                return result.right().value();
            }
        }
        return JanusGraphOperationStatus.OK;
    }

    private JanusGraphOperationStatus setResourcePropertiesFromGraph(GraphVertex componentV, NodeType toscaElement) {
        Either<Map<String, PropertyDataDefinition>, JanusGraphOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.PROPERTIES);
        if (result.isLeft()) {
            toscaElement.setProperties(result.left().value());
        } else {
            if (result.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
                return result.right().value();
            }
        }
        return JanusGraphOperationStatus.OK;
    }

    private StorageOperationStatus assosiateToDerived(GraphVertex nodeTypeVertex, List<GraphVertex> derivedResources) {
        for (GraphVertex derivedV : derivedResources) {
            JanusGraphOperationStatus
                createEdge = janusGraphDao
                .createEdge(nodeTypeVertex, derivedV, EdgeLabelEnum.DERIVED_FROM, null);
            if (createEdge != JanusGraphOperationStatus.OK) {
                log.trace("Failed to associate resource {} to derived with id {}", nodeTypeVertex.getUniqueId(), derivedV.getUniqueId());
                return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(createEdge);
            }
        }
        return StorageOperationStatus.OK;
    }

    private StorageOperationStatus addAdditionalInformationToResource(GraphVertex nodeTypeVertex, NodeType nodeType, List<GraphVertex> derivedResources) {
        // Note : currently only one derived supported!!!!
        Either<Map<String, AdditionalInfoParameterDataDefinition>, StorageOperationStatus> dataFromDerived = getDataFromDerived(derivedResources, EdgeLabelEnum.ADDITIONAL_INFORMATION);
        if (dataFromDerived.isRight()) {
            return dataFromDerived.right().value();
        }
        Map<String, AdditionalInfoParameterDataDefinition> addInformationAll = dataFromDerived.left().value();

        Map<String, AdditionalInfoParameterDataDefinition> addInformation = nodeType.getAdditionalInformation();
        if (addInformation != null) {
            ToscaDataDefinition.mergeDataMaps(addInformationAll, addInformation);
        }
        if (!addInformationAll.isEmpty()) {
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.ADDITIONAL_INFORMATION, EdgeLabelEnum.ADDITIONAL_INFORMATION, addInformationAll);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
        }
        return StorageOperationStatus.OK;
    }

    private StorageOperationStatus associateCapabilitiesToResource(GraphVertex nodeTypeVertex, NodeType nodeType, List<GraphVertex> derivedResources) {
        // Note : currently only one derived supported!!!!
        Either<Map<String, ListCapabilityDataDefinition>, StorageOperationStatus> dataFromDerived = getDataFromDerived(derivedResources, EdgeLabelEnum.CAPABILITIES);
        if (dataFromDerived.isRight()) {
            return dataFromDerived.right().value();
        }
        Map<String, ListCapabilityDataDefinition> capabiltiesAll = dataFromDerived.left().value();

        Map<String, ListCapabilityDataDefinition> capabilties = nodeType.getCapabilities();
        if (capabilties != null) {
            if (capabiltiesAll == null) {
                capabiltiesAll = new HashMap<>();
            }
            capabilties.values().forEach(l -> {
                l.getListToscaDataDefinition().stream().filter(p -> p.getUniqueId() == null).forEach(p -> {
                    String uid = UniqueIdBuilder.buildCapabilityUid(nodeTypeVertex.getUniqueId(), p.getName());
                    p.setUniqueId(uid);
                });
            });

            ToscaDataDefinition.mergeDataMaps(capabiltiesAll, capabilties);
            capabiltiesAll.values().forEach(l -> {
                l.getListToscaDataDefinition().forEach(c -> {
                    List<String> capabilitySources = c.getCapabilitySources();
                    if (capabilitySources == null) {
                        capabilitySources = new ArrayList<>();
                    }
                    capabilitySources.add((String) nodeType.getMetadataValue(JsonPresentationFields.TOSCA_RESOURCE_NAME));
                    c.setCapabilitySources(capabilitySources);
                });
            });
        }
        capabiltiesAll.values().forEach(l -> {
            l.getListToscaDataDefinition().forEach(c -> {
                List<String> capabilitySources = c.getCapabilitySources();
                if (capabilitySources == null) {
                    capabilitySources = new ArrayList<>();
                }
                capabilitySources.add((String) nodeType.getMetadataValue(JsonPresentationFields.TOSCA_RESOURCE_NAME));
                c.setCapabilitySources(capabilitySources);
            });
        });
        if (!capabiltiesAll.isEmpty()) {
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.CAPABILITIES, EdgeLabelEnum.CAPABILITIES, capabiltiesAll);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
        }
        return StorageOperationStatus.OK;
    }

    private StorageOperationStatus associateRequirementsToResource(GraphVertex nodeTypeVertex, NodeType nodeType, List<GraphVertex> derivedResources) {
        // Note : currently only one derived supported!!!!
        Either<Map<String, ListRequirementDataDefinition>, StorageOperationStatus> dataFromDerived = getDataFromDerived(derivedResources, EdgeLabelEnum.REQUIREMENTS);
        if (dataFromDerived.isRight()) {
            return dataFromDerived.right().value();
        }
        Map<String, ListRequirementDataDefinition> requirementsAll = dataFromDerived.left().value();

        Map<String, ListRequirementDataDefinition> requirements = nodeType.getRequirements();
        if (requirements != null) {
            if (requirementsAll == null) {
                requirementsAll = new HashMap<>();
            }
            requirements.values().forEach(l -> {
                l.getListToscaDataDefinition().stream().filter(p -> p.getUniqueId() == null).forEach(p -> {
                    String uid = UniqueIdBuilder.buildRequirementUid(nodeTypeVertex.getUniqueId(), p.getName());
                    p.setUniqueId(uid);
                });
            });

            ToscaDataDefinition.mergeDataMaps(requirementsAll, requirements);

        }
        if (!requirementsAll.isEmpty()) {
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.REQUIREMENTS, EdgeLabelEnum.REQUIREMENTS, requirementsAll);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
        }
        return StorageOperationStatus.OK;
    }

    private StorageOperationStatus associateAttributesToResource(GraphVertex nodeTypeVertex, NodeType nodeType, List<GraphVertex> derivedResources) {
        // Note : currently only one derived supported!!!!
        Either<Map<String, PropertyDataDefinition>, StorageOperationStatus> dataFromDerived = getDataFromDerived(derivedResources, EdgeLabelEnum.ATTRIBUTES);
        if (dataFromDerived.isRight()) {
            return dataFromDerived.right().value();
        }
        Map<String, PropertyDataDefinition> attributesAll = dataFromDerived.left().value();

        Map<String, PropertyDataDefinition> attributes = nodeType.getAttributes();
        if (attributes != null) {
            attributes.values().stream().filter(p -> p.getUniqueId() == null).forEach(p -> {
                String uid = UniqueIdBuilder.buildAttributeUid(nodeTypeVertex.getUniqueId(), p.getName());
                p.setUniqueId(uid);
            });
            ToscaDataDefinition.mergeDataMaps(attributesAll, attributes);
        }
        if (!attributesAll.isEmpty()) {
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.ATTRIBUTES, EdgeLabelEnum.ATTRIBUTES, attributesAll);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
        }
        return StorageOperationStatus.OK;
    }

    // TODO get from derived
    private StorageOperationStatus associateCapabilitiesPropertiesToResource(GraphVertex nodeTypeVertex, NodeType nodeType, List<GraphVertex> derivedResources) {
        // // Note : currently only one derived supported!!!!
        Either<Map<String, MapPropertiesDataDefinition>, StorageOperationStatus> dataFromDerived = getDataFromDerived(derivedResources, EdgeLabelEnum.CAPABILITIES_PROPERTIES);
        if (dataFromDerived.isRight()) {
            return dataFromDerived.right().value();
        }
        Map<String, MapPropertiesDataDefinition> propertiesAll = dataFromDerived.left().value();
        Map<String, MapPropertiesDataDefinition> capabiltiesProps = nodeType.getCapabilitiesProperties();
        if (capabiltiesProps != null) {
            capabiltiesProps.values().forEach(l -> {
                if (l.getMapToscaDataDefinition() != null && l.getMapToscaDataDefinition().values() != null) {
                    Collection<PropertyDataDefinition> mapToscaDataDefinition = l.getMapToscaDataDefinition().values();
                    mapToscaDataDefinition.stream().filter(p -> p != null && p.getUniqueId() == null).forEach(p -> {
                        String uid = UniqueIdBuilder.buildRequirementUid(nodeTypeVertex.getUniqueId(), p.getName());
                        p.setUniqueId(uid);
                    });
                }
            });
            ToscaDataDefinition.mergeDataMaps(propertiesAll, capabiltiesProps);
        }
        if (!propertiesAll.isEmpty()) {
            Either<GraphVertex, StorageOperationStatus> assosiateElementToData = associateElementToData(nodeTypeVertex, VertexTypeEnum.CAPABILITIES_PROPERTIES, EdgeLabelEnum.CAPABILITIES_PROPERTIES, propertiesAll);
            if (assosiateElementToData.isRight()) {
                return assosiateElementToData.right().value();
            }
        }
        return StorageOperationStatus.OK;
    }

    public Either<List<GraphVertex>, StorageOperationStatus> findDerivedResources(NodeType nodeType) {

        List<GraphVertex> derivedResources = new ArrayList<>();
        List<String> derivedFromResources = nodeType.getDerivedFrom();
        if (derivedFromResources != null && !derivedFromResources.isEmpty()) {

            for (String parentResource : derivedFromResources) {
                Either<List<GraphVertex>, JanusGraphOperationStatus> getParentResources = derivedResourceResolver.findDerivedResources(parentResource);
                List<GraphVertex> resources = null;
                if (getParentResources.isRight()) {
                    log.error("Cannot find parent resource by tosca resource name {} in the graph.", parentResource);
                    return Either.right(StorageOperationStatus.PARENT_RESOURCE_NOT_FOUND);

                } else {
                    resources = getParentResources.left().value();
                    if (resources == null || resources.size() == 0) {
                        log.error("Cannot find parent resource by tosca name {} in the graph. resources size is empty", parentResource);
                        return Either.right(StorageOperationStatus.PARENT_RESOURCE_NOT_FOUND);
                    } else {
                        if (resources.size() > 1) {
                            return handleMultipleParent(parentResource, derivedResources, resources);
                        } else {
                            GraphVertex parentResourceData = resources.get(0);
                            derivedResources.add(parentResourceData);
                        }
                    }

                }

            }
        }
        return Either.left(derivedResources);
    }

    Either<List<GraphVertex>, StorageOperationStatus> handleMultipleParent(String parentResource, List<GraphVertex> derivedResource, List<GraphVertex> fetchedDerivedResources) {

        Either<List<GraphVertex>, StorageOperationStatus> result = Either.left(derivedResource);
        try {
            fetchedDerivedResources.sort((d1, d2) -> {
                return new Double(Double.parseDouble((String) d1.getMetadataProperty(GraphPropertyEnum.VERSION))).compareTo(Double.parseDouble((String) d2.getMetadataProperty(GraphPropertyEnum.VERSION)));
            });

            int actualHighestIndex = fetchedDerivedResources.size() - 1;
            derivedResource.add(fetchedDerivedResources.get(actualHighestIndex));
            fetchedDerivedResources.remove(actualHighestIndex);

            StorageOperationStatus status = fixMultipleParent(fetchedDerivedResources);
            if (status != StorageOperationStatus.OK) {
                result = Either.right(status);
            }
        } catch (Exception e) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Exception occured during handle multiple parent {}. Exception is  {}", parentResource, e.getMessage());
            result = Either.right(StorageOperationStatus.GENERAL_ERROR);
        }
        return result;
    }

    private StorageOperationStatus fixMultipleParent(List<GraphVertex> fetchedDerivedResources) {
        StorageOperationStatus result = StorageOperationStatus.OK;
        for (GraphVertex fetchedDerivedResource : fetchedDerivedResources) {
            fetchedDerivedResource.addMetadataProperty(GraphPropertyEnum.IS_HIGHEST_VERSION, false);
            Either<GraphVertex, JanusGraphOperationStatus> updateVertexRes = janusGraphDao.updateVertex(fetchedDerivedResource);
            if (updateVertexRes.isRight()) {
                JanusGraphOperationStatus titatStatus = updateVertexRes.right().value();
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to set highest version of node type {} to false. Status is  {}", fetchedDerivedResource.getMetadataProperty(GraphPropertyEnum.TOSCA_RESOURCE_NAME), titatStatus);
                result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(titatStatus);
                break;
            }
        }
        return result;
    }

    private GraphVertex fillMetadata(GraphVertex nodeTypeVertex, NodeType nodeType) {
        nodeTypeVertex.setLabel(VertexTypeEnum.NODE_TYPE);

        fillCommonMetadata(nodeTypeVertex, nodeType);

        return nodeTypeVertex;
    }

    @Override
    public Either<ToscaElement, StorageOperationStatus> deleteToscaElement(GraphVertex toscaElementVertex) {
        Either<ToscaElement, StorageOperationStatus> nodeType = getToscaElement(toscaElementVertex, new ComponentParametersView());
        if (nodeType.isRight()) {
            log.debug("Failed to fetch tosca element {} error {}", toscaElementVertex.getUniqueId(), nodeType.right().value());
            return nodeType;
        }
        JanusGraphOperationStatus status = disassociateAndDeleteCommonElements(toscaElementVertex);
        if (status != JanusGraphOperationStatus.OK) {
            Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
        }
        status = janusGraphDao
            .disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.CAPABILITIES);
        if (status != JanusGraphOperationStatus.OK) {
            log.debug("Failed to disassociate capabilties for {} error {}", toscaElementVertex.getUniqueId(), status);
            Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
        }
        status = janusGraphDao
            .disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.CAPABILITIES_PROPERTIES);
        if (status != JanusGraphOperationStatus.OK) {
            log.debug("Failed to disassociate capabilties properties for {} error {}", toscaElementVertex.getUniqueId(), status);
            Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
        }
        status = janusGraphDao
            .disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.REQUIREMENTS);
        if (status != JanusGraphOperationStatus.OK) {
            log.debug("Failed to disassociate requirements for {} error {}", toscaElementVertex.getUniqueId(), status);
            Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
        }
        status = janusGraphDao
            .disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.ATTRIBUTES);
        if (status != JanusGraphOperationStatus.OK) {
            log.debug("Failed to disassociate attributes for {} error {}", toscaElementVertex.getUniqueId(), status);
            Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
        }
        status = janusGraphDao
            .disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.INTERFACE_ARTIFACTS);
        if (status != JanusGraphOperationStatus.OK) {
            log.debug("Failed to disassociate interface artifacts for {} error {}", toscaElementVertex.getUniqueId(), status);
            Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
        }
        toscaElementVertex.getVertex().remove();
        log.trace("Tosca element vertex for {} was removed", toscaElementVertex.getUniqueId());

        return nodeType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Either<NodeType, StorageOperationStatus> createToscaElement(ToscaElement toscaElement) {
        return createNodeType((NodeType) toscaElement);
    }

    @Override
    protected <T extends ToscaElement> JanusGraphOperationStatus setCategoriesFromGraph(GraphVertex vertexComponent, T toscaElement) {
        return setResourceCategoryFromGraph(vertexComponent, toscaElement);
    }

    @Override
    protected <T extends ToscaElement> StorageOperationStatus validateCategories(T toscaElementToUpdate, GraphVertex elementV) {
        return validateResourceCategory(toscaElementToUpdate, elementV);
    }

    @Override
    protected <T extends ToscaElement> StorageOperationStatus updateDerived(T toscaElementToUpdate, GraphVertex nodeTypeV) {

        NodeType nodeType = (NodeType) toscaElementToUpdate;

        List<String> derivedFromResources = nodeType.getDerivedFrom();

        // now supported only single derived from
        if (derivedFromResources != null && !derivedFromResources.isEmpty() && derivedFromResources.get(0) != null) {
            String firstDerived = derivedFromResources.get(0);
            boolean derivedFromGenericType = null != nodeType.getDerivedFromGenericType();
            Either<GraphVertex, JanusGraphOperationStatus> childVertex = janusGraphDao
                .getChildVertex(nodeTypeV, EdgeLabelEnum.DERIVED_FROM, JsonParseFlagEnum.NoParse);
            if (childVertex.isRight()) {
                JanusGraphOperationStatus getchieldError = childVertex.right().value();
                log.debug("Failed to fetch derived resource for element {} error {}", nodeTypeV.getUniqueId(), getchieldError);
                return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getchieldError);
            }
            GraphVertex firstDerivedInChain = childVertex.left().value();

            String firstCurrentDerived = (String) firstDerivedInChain.getMetadataProperty(GraphPropertyEnum.TOSCA_RESOURCE_NAME);
            if (!firstDerived.equals(firstCurrentDerived) || derivedFromGenericType) {

                Map<GraphPropertyEnum, Object> propertiesToMatch = new HashMap<>();
                propertiesToMatch.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());

                propertiesToMatch.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, firstDerived);
                propertiesToMatch.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);

                Either<List<GraphVertex>, JanusGraphOperationStatus> getParentResources = janusGraphDao
                    .getByCriteria(VertexTypeEnum.NODE_TYPE, propertiesToMatch, JsonParseFlagEnum.NoParse);

                if (getParentResources.isRight()) {
                    JanusGraphOperationStatus error = getParentResources.right().value();
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to fetch derived by criteria {}. error {} ", propertiesToMatch, error);
                    return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(error);
                }
                // must be only one
                GraphVertex newDerivedV = getParentResources.left().value().get(0);
                return updateDerived(toscaElementToUpdate, nodeTypeV, firstDerivedInChain, newDerivedV, false);
            }
        }
        return StorageOperationStatus.OK;
    }

    /**
     *
     * @param toscaElementToUpdate
     * @param nodeTypeV
     * @param preDerivedV
     * @param newDerivedV
     * @param mergeValues
     * @return
     */
    protected <T extends ToscaElement> StorageOperationStatus updateDerived(T toscaElementToUpdate, GraphVertex nodeTypeV, GraphVertex preDerivedV, GraphVertex newDerivedV, boolean mergeValues) {
        Set<String> preDerivedChainIdList = new HashSet();
        preDerivedChainIdList.add(preDerivedV.getUniqueId());
        Either<GraphVertex, JanusGraphOperationStatus> childVertex = janusGraphDao
            .getChildVertex(preDerivedV, EdgeLabelEnum.DERIVED_FROM, JsonParseFlagEnum.NoParse);
        while (childVertex.isLeft()) {
            GraphVertex currentChield = childVertex.left().value();
            preDerivedChainIdList.add(currentChield.getUniqueId());
            childVertex = janusGraphDao
                .getChildVertex(currentChield, EdgeLabelEnum.DERIVED_FROM, JsonParseFlagEnum.NoParse);
        }

        List<GraphVertex> derivedResources = new ArrayList<>();
        derivedResources.add(newDerivedV);
        StorageOperationStatus updateStatus = updateDataFromNewDerived(derivedResources, nodeTypeV, (NodeType) toscaElementToUpdate, mergeValues, preDerivedChainIdList);
        if (updateStatus != StorageOperationStatus.OK) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update data for {} from new derived {} ", nodeTypeV.getUniqueId(), newDerivedV.getUniqueId(), updateStatus);
            return updateStatus;
        }

        Either<Edge, JanusGraphOperationStatus> deleteEdge = janusGraphDao
            .deleteEdge(nodeTypeV, preDerivedV, EdgeLabelEnum.DERIVED_FROM);
        if (deleteEdge.isRight()) {
            JanusGraphOperationStatus deleteError = deleteEdge.right().value();
            log.debug("Failed to disassociate element {} from derived {} , error {}", nodeTypeV.getUniqueId(), preDerivedV.getUniqueId(), deleteError);
            return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(deleteError);
        }

        janusGraphDao.createEdge(nodeTypeV, newDerivedV, EdgeLabelEnum.DERIVED_FROM, new HashMap<>());

        return StorageOperationStatus.OK;
    }

    private StorageOperationStatus associateDerivedDataByType(EdgeLabelEnum edgeLabel, GraphVertex nodeTypeV, NodeType nodeToUpdate, List<GraphVertex> newDerived) {

        switch (edgeLabel) {
        case CAPABILITIES:
            return associateCapabilitiesToResource(nodeTypeV, nodeToUpdate, newDerived);
        case REQUIREMENTS:
            return associateRequirementsToResource(nodeTypeV, nodeToUpdate, newDerived);
        case PROPERTIES:
            return associatePropertiesToResource(nodeTypeV, nodeToUpdate, newDerived);
        case ATTRIBUTES:
            return associateAttributesToResource(nodeTypeV, nodeToUpdate, newDerived);
        case ADDITIONAL_INFORMATION:
            return addAdditionalInformationToResource(nodeTypeV, nodeToUpdate, newDerived);
        case CAPABILITIES_PROPERTIES:
            return associateCapabilitiesPropertiesToResource(nodeTypeV, nodeToUpdate, newDerived);
        default:
            return StorageOperationStatus.OK;
        }

    }

    private StorageOperationStatus updateDataFromNewDerived(List<GraphVertex> newDerived, GraphVertex nodeTypeV, NodeType nodeToUpdate, boolean mergeValues, Set<String> preDerivedChainIdList) {
        EnumSet<EdgeLabelEnum> edgeLabels = EnumSet.of(EdgeLabelEnum.CAPABILITIES, EdgeLabelEnum.REQUIREMENTS, EdgeLabelEnum.PROPERTIES, EdgeLabelEnum.ATTRIBUTES, EdgeLabelEnum.CAPABILITIES_PROPERTIES, EdgeLabelEnum.ADDITIONAL_INFORMATION);
        StorageOperationStatus status = null;
        for (EdgeLabelEnum edge : edgeLabels) {
            status = updateDataByType(newDerived, nodeTypeV, edge, nodeToUpdate, mergeValues, preDerivedChainIdList);
            if (status != StorageOperationStatus.OK) {
                break;
            }
        }
        return status;

    }

    private <T extends ToscaDataDefinition> StorageOperationStatus updateDataByType(List<GraphVertex> newDerivedList, GraphVertex nodeTypeV, EdgeLabelEnum label, NodeType nodeElement, boolean mergeValues, Set<String> preDerivedChainIdList) {
        log.debug("Update data from derived for element {} type {}", nodeTypeV.getUniqueId(), label);
        Either<GraphVertex, JanusGraphOperationStatus> dataFromGraph = getDataVertex(nodeTypeV, label);
        if (dataFromGraph.isRight()) {
            if (JanusGraphOperationStatus.NOT_FOUND == dataFromGraph.right().value())
                return associateDerivedDataByType(label, nodeTypeV, nodeElement, newDerivedList);
            return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(dataFromGraph.right().value());
        }
        GraphVertex dataV = dataFromGraph.left().value();

        Map<String, T> mapFromGraph = (Map<String, T>) dataV.getJson();
        Map<String, T> valuesFrmPrev = null;
        if (isSimpleHierarchy(label)) {
            if (mergeValues) {
                valuesFrmPrev = mapFromGraph.entrySet().stream().filter(e -> e.getValue().getOwnerId() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            }
            mapFromGraph.entrySet().removeIf(e -> preDerivedChainIdList.contains(e.getValue().getOwnerId()));
        } else {
            final Map<String, T> valuesFrmPrevFinal = new HashMap<>();
            mapFromGraph.entrySet().stream().forEach(e -> {
                T value = e.getValue();
                value = ToscaDataDefinition.removeAndCollectByOwnerId(value, preDerivedChainIdList);
                valuesFrmPrevFinal.put(e.getKey(), value);
            });
            valuesFrmPrev = valuesFrmPrevFinal;
            mapFromGraph.entrySet().removeIf(e->e.getValue().isEmpty());
        }

        Either<Map<String, T>, StorageOperationStatus> dataFromDerived = getDataFromDerived(newDerivedList, label);
        if (dataFromDerived.isRight()) {
            return dataFromDerived.right().value();
        }
        Map<String, T> dataFromDerivedAll = dataFromDerived.left().value();

        Either<Map<String, T>, String> merged = ToscaDataDefinition.mergeDataMaps(dataFromDerivedAll, mapFromGraph);
        if (merged.isRight()) {
            log.debug("property {} cannot be overriden", merged.right().value());
            return StorageOperationStatus.INVALID_PROPERTY;
        }
        if (mergeValues && valuesFrmPrev != null) {
            valuesFrmPrev.entrySet().forEach(e -> {
                T newData = merged.left().value().get(e.getKey());
                if (newData != null) {
                    if (isSimpleHierarchy(label)) {
                        e.getValue().mergeFunction(newData, true);
                    }else{
                        e.getValue().updateIfExist(newData, true);
                    }
                }
            });
        }
        dataV.setJson(dataFromDerivedAll);
        Either<GraphVertex, JanusGraphOperationStatus> updateDataV = updateOrCopyOnUpdate(dataV, nodeTypeV, label);
        if (updateDataV.isRight()) {
            return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(updateDataV.right().value());
        }
        return StorageOperationStatus.OK;
    }

    private boolean isSimpleHierarchy(EdgeLabelEnum label) {
        switch (label) {
        case PROPERTIES:
        case ATTRIBUTES:
        case ADDITIONAL_INFORMATION:
        case ARTIFACTS:
        case GROUPS:
        case INPUTS:
            return true;
        default:
            return false;
        }
    }

    @Override
    public <T extends ToscaElement> void fillToscaElementVertexData(GraphVertex elementV, T toscaElementToUpdate, JsonParseFlagEnum flag) {
        fillMetadata(elementV, (NodeType) toscaElementToUpdate);
    }

    public Either<ToscaElement, StorageOperationStatus> shouldUpdateDerivedVersion(ToscaElement toscaElementToUpdate, GraphVertex nodeTypeV) {
        NodeType nodeType = (NodeType) toscaElementToUpdate;

        Either<GraphVertex, JanusGraphOperationStatus> childVertex = janusGraphDao
            .getChildVertex(nodeTypeV, EdgeLabelEnum.DERIVED_FROM, JsonParseFlagEnum.NoParse);
        if (childVertex.isRight()) {
            JanusGraphOperationStatus getchildError = childVertex.right().value();
            if (getchildError == JanusGraphOperationStatus.NOT_FOUND) {
                log.debug("derived resource for element {} not found", nodeTypeV.getUniqueId());
                return Either.right(StorageOperationStatus.OK);
            }

            log.debug("Failed to fetch derived resource for element {} error {}", nodeTypeV.getUniqueId(), getchildError);
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getchildError));
        }
        GraphVertex firstDerivedInChain = childVertex.left().value();

        String currentVersion = (String) firstDerivedInChain.getMetadataProperty(GraphPropertyEnum.VERSION);

        Map<GraphPropertyEnum, Object> props = new HashMap<>();
        props.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, nodeType.getDerivedFrom().get(0));
        props.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        props.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);

        Map<GraphPropertyEnum, Object> propsHasNot = new HashMap<>();
        propsHasNot.put(GraphPropertyEnum.IS_DELETED, true);
        Either<List<GraphVertex>, JanusGraphOperationStatus> byCriteria = janusGraphDao
            .getByCriteria(VertexTypeEnum.NODE_TYPE, props, propsHasNot, JsonParseFlagEnum.NoParse);
        if (byCriteria.isRight()) {
            log.debug("Failed to fetch derived by props {} error {}", props, byCriteria.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(byCriteria.right().value()));
        }
        List<GraphVertex> lastDerived = byCriteria.left().value();
        // now supported only one derived!!! Change in future!(Evg)
        GraphVertex derivedFromHighest = lastDerived.get(0);
        String highestVersion = (String) derivedFromHighest.getMetadataProperty(GraphPropertyEnum.VERSION);
        if (!highestVersion.equals(currentVersion)) {

            // need to update to latest version of derived from
            StorageOperationStatus updateDerived = updateDerived(toscaElementToUpdate, nodeTypeV, firstDerivedInChain, derivedFromHighest, true);

            if (updateDerived != StorageOperationStatus.OK) {
                log.debug("Failed to update {} to highest derived {} from error {}", nodeTypeV.getUniqueId(), derivedFromHighest.getUniqueId(), updateDerived);
                return Either.right(updateDerived);
            }
            return getToscaElement(nodeTypeV.getUniqueId(), new ComponentParametersView());
        }
        // no version changes
        return Either.right(StorageOperationStatus.OK);
    }

}
