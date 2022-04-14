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
package org.openecomp.sdc.be.model.jsonjanusgraph.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionaryExtractor;
import org.openecomp.sdc.be.datatypes.category.MetadataKeyDataDefinition;
import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CompositionDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.DataTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapAttributesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapCapabilityProperty;
import org.openecomp.sdc.be.datatypes.elements.MapGroupsDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapInterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.PolicyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ProductMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RelationshipInstDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.CreatedFrom;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFieldsExtractor;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceAttribute;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceInterface;
import org.openecomp.sdc.be.model.ComponentInstanceOutput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.MapInterfaceInstanceDataDefinition;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.NodeType;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.enums.JsonConstantKeysEnum;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.ProductMetadataData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.ServiceMetadataData;
import org.openecomp.sdc.be.ui.model.OperationUi;
import org.openecomp.sdc.be.ui.model.PropertyAssignmentUi;
import org.openecomp.sdc.common.log.wrappers.Logger;

public class ModelConverter {

    public static final String CAP_PROP_DELIM = "#";
    private static final Logger log = Logger.getLogger(ModelConverter.class);

    private ModelConverter() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends ToscaElement> T convertToToscaElement(Component component) {
        if (isAtomicComponent(component)) {
            return (T) convertToNodeType(component);
        }
        return (T) convertToTopologyTemplate(component);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Component> T convertFromToscaElement(ToscaElement toscaElement) {
        switch (toscaElement.getComponentType()) {
            case RESOURCE:
                return (T) convertToResource(toscaElement);
            case SERVICE:
            case PRODUCT:
                return (T) convertToService(toscaElement);
            default:
                return null;
        }
    }

    public static boolean isAtomicComponent(Component component) {
        ComponentTypeEnum componentType = component.getComponentType();
        if (!componentType.equals(ComponentTypeEnum.RESOURCE)) {
            return false;
        }
        Resource resource = (Resource) component;
        ResourceTypeEnum resType = resource.getResourceType();
        return isAtomicComponent(resType);
    }

    public static boolean isAtomicComponent(ResourceTypeEnum resourceType) {
        if (resourceType == null) {
            return false;
        }
        return resourceType.isAtomicType();
    }

    // **********************************************************
    public static VertexTypeEnum getVertexType(Component component) {
        VertexTypeEnum vertexType;
        if (isAtomicComponent(component)) {
            vertexType = VertexTypeEnum.NODE_TYPE;
        } else {
            vertexType = VertexTypeEnum.TOPOLOGY_TEMPLATE;
        }
        return vertexType;
    }

    public static VertexTypeEnum getVertexType(String resourceTypeName) {
        ResourceTypeEnum resourceType = ResourceTypeEnum.getTypeByName(resourceTypeName);
        if (isAtomicComponent(resourceType)) {
            return VertexTypeEnum.NODE_TYPE;
        } else {
            return VertexTypeEnum.TOPOLOGY_TEMPLATE;
        }
    }

    private static Service convertToService(ToscaElement toscaElement) {
        Service service = new Service();
        convertComponentFields(service, toscaElement);
        convertServiceSpecificFields(toscaElement, service);
        TopologyTemplate topologyTemplate = (TopologyTemplate) toscaElement;
        convertComponentInstances(topologyTemplate, service);
        convertInputs(topologyTemplate, service);
        convertOutputs(topologyTemplate, service);
        convertProperties(topologyTemplate, service);
        convertPolicies(topologyTemplate, service);
        convertAttributesToComponent(topologyTemplate, service);
        convertGroups(topologyTemplate, service);
        setCapabilitiesToComponentAndGroups(topologyTemplate, service);
        convertPolicies(topologyTemplate, service);
        convertRelations(topologyTemplate, service);
        convertArtifacts(topologyTemplate, service);
        convertServiceApiArtifacts(topologyTemplate, service);
        convertServicePaths(topologyTemplate, service);
        convertServiceInterfaces(topologyTemplate, service);
        convertDataTypes(topologyTemplate, service);
        convertNodeFiltersComponents(topologyTemplate, service);
        convertSubstitutionFiltersComponents(topologyTemplate, service);
        setCapabilitiesToComponent(topologyTemplate, service);
        setRequirementsToComponent(topologyTemplate, service);
        return service;
    }

    private static void convertServiceSpecificFields(ToscaElement toscaElement, Service service) {
        service.setProjectCode((String) toscaElement.getMetadataValue(JsonPresentationFields.PROJECT_CODE));
        service.setDistributionStatus(
            DistributionStatusEnum.findState((String) toscaElement.getMetadataValue(JsonPresentationFields.DISTRIBUTION_STATUS)));
        service.setEcompGeneratedNaming((Boolean) toscaElement.getMetadataValueOrDefault(JsonPresentationFields.ECOMP_GENERATED_NAMING, true));
        service.setNamingPolicy((String) toscaElement.getMetadataValueOrDefault(JsonPresentationFields.NAMING_POLICY, StringUtils.EMPTY));
        service.setEnvironmentContext((String) toscaElement.getMetadataValue(JsonPresentationFields.ENVIRONMENT_CONTEXT));
        service.setInstantiationType((String) toscaElement.getMetadataValueOrDefault(JsonPresentationFields.INSTANTIATION_TYPE, StringUtils.EMPTY));
    }

    private static Resource convertToResource(ToscaElement toscaElement) {
        Resource resource = new Resource();
        convertComponentFields(resource, toscaElement);
        resource.setResourceType(toscaElement.getResourceType());
        if (toscaElement.getToscaType() == ToscaElementTypeEnum.NODE_TYPE) {
            NodeType nodeType = (NodeType) toscaElement;
            resource.setDerivedFrom(nodeType.getDerivedFrom());
            resource.setDerivedList(nodeType.getDerivedList());
            resource.setDerivedFromMapOfIdToName(nodeType.getDerivedFromMapOfIdToName());
            resource.setAbstract((Boolean) nodeType.getMetadataValue(JsonPresentationFields.IS_ABSTRACT));
            final String toscaVersion = nodeType.getToscaVersion();
            if (toscaVersion != null) {
                resource.setToscaVersion(toscaVersion);
            }
            convertAttributesToResource(nodeType, resource);
            convertCapabilities(nodeType, resource);
            convertRequirements(nodeType, resource);
            convertInterfaces(nodeType, resource);
            convertDataTypes(nodeType, resource);
        } else {
            TopologyTemplate topologyTemplate = (TopologyTemplate) toscaElement;
            if (resource.getResourceType() == ResourceTypeEnum.VF || resource.getResourceType() == ResourceTypeEnum.PNF) {
                resource.setCsarUUID((String) topologyTemplate.getMetadataValue(JsonPresentationFields.CSAR_UUID));
                resource.setCsarVersionId((String) topologyTemplate.getMetadataValue(JsonPresentationFields.CSAR_VERSION_ID));
                resource.setCsarVersion((String) topologyTemplate.getMetadataValue(JsonPresentationFields.CSAR_VERSION));
                resource.setImportedToscaChecksum((String) topologyTemplate.getMetadataValue(JsonPresentationFields.IMPORTED_TOSCA_CHECKSUM));
                convertInterfaces(topologyTemplate, resource);
            }
            convertComponentInstances(topologyTemplate, resource);
            convertRelations(topologyTemplate, resource);
            convertInputs(topologyTemplate, resource);
            convertOutputs(topologyTemplate, resource);
            convertGroups(topologyTemplate, resource);
            setCapabilitiesToComponentAndGroups(topologyTemplate, resource);
            convertPolicies(topologyTemplate, resource);
            convertNodeFiltersComponents(topologyTemplate, resource);
            convertSubstitutionFiltersComponents(topologyTemplate, resource);
            convertProperties(topologyTemplate, resource);
            convertAttributesToComponent(topologyTemplate, resource);
            setCapabilitiesToComponent(topologyTemplate, resource);
            setRequirementsToComponent(topologyTemplate, resource);
            convertDataTypes(topologyTemplate, resource);
        }
        convertArtifacts(toscaElement, resource);
        convertAdditionalInformation(toscaElement, resource);
        return resource;
    }

    private static void convertInterfaces(TopologyTemplate toscaElement, Resource resource) {
        Map<String, InterfaceDataDefinition> interfaces = toscaElement.getInterfaces();
        Map<String, InterfaceDefinition> copy;
        if (interfaces != null) {
            copy = interfaces.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new InterfaceDefinition(e.getValue())));
        } else {
            copy = new HashMap<>();
        }
        resource.setInterfaces(copy);
    }

    private static void convertServiceInterfaces(TopologyTemplate toscaElement, Service service) {
        Map<String, InterfaceDataDefinition> interfaces = toscaElement.getInterfaces();
        Map<String, InterfaceDefinition> copy;
        if (interfaces != null) {
            copy = interfaces.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new InterfaceDefinition(e.getValue())));
        } else {
            copy = new HashMap<>();
        }
        service.setInterfaces(copy);
    }

    private static void convertAttributesToResource(NodeType nodeType, Resource resource) {
        Map<String, AttributeDataDefinition> attributes = nodeType.getAttributes();
        if (attributes != null) {
            final Map<String, AttributeDefinition> attributeDefinitionMap = attributes.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, o -> new AttributeDefinition(o.getValue())));
            resource.setAttributes(new ArrayList<>(attributeDefinitionMap.values()));
        }
    }

    private static void convertInterfaces(NodeType nodeType, Resource resource) {
        Map<String, InterfaceDataDefinition> interfaceArtifacts = nodeType.getInterfaceArtifacts();
        if (interfaceArtifacts != null) {
            Map<String, InterfaceDefinition> interfaces = interfaceArtifacts.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, en -> new InterfaceDefinition(en.getValue())));
            resource.setInterfaces(interfaces);
        }
    }

    /**
     * Converts component instances of topology template to component instances of resource
     *
     * @param topologyTemplate
     * @param component
     */
    public static void convertComponentInstances(TopologyTemplate topologyTemplate, Component component) {
        if (MapUtils.isNotEmpty(topologyTemplate.getComponentInstances())) {
            setComponentInstancesAttributesToComponent(topologyTemplate, component);
            setComponentInstancesPropertiesToComponent(topologyTemplate, component);
            setComponentInstancesInputsToComponent(topologyTemplate, component);
            setComponentInstancesOutputsToComponent(topologyTemplate, component);
            setComponentInstancesToComponent(topologyTemplate, component);
            setComponentInstancesCapabilitiesToComponentAndCI(topologyTemplate, component);
            setComponentInstancesRequirementsToComponent(topologyTemplate, component);
            setComponentInstancesArtifactsToComponent(topologyTemplate, component);
            setComponentInstancesInterfacesToComponent(topologyTemplate, component);
        }
    }

    private static void setComponentInstancesArtifactsToComponent(TopologyTemplate topologyTemplate, Component component) {
        Map<String, MapArtifactDataDefinition> instDeploymentArtifacts = topologyTemplate.getInstDeploymentArtifacts();
        Map<String, MapArtifactDataDefinition> instanceArtifacts = topologyTemplate.getInstanceArtifacts();
        setComponentInstancesDeploymentArtifactsToComponent(instDeploymentArtifacts, component);
        setComponentInstancesInformationalArtifactsToComponent(instanceArtifacts, component);
    }

    public static void setComponentInstancesInformationalArtifactsToComponent(Map<String, MapArtifactDataDefinition> instanceArtifacts,
                                                                              Component component) {
        List<ComponentInstance> instances = component.getComponentInstances();
        if (instanceArtifacts != null && instances != null) {
            instanceArtifacts.entrySet().forEach(e -> {
                Optional<ComponentInstance> ci = instances.stream().filter(i -> i.getUniqueId().equals(e.getKey())).findFirst();
                if (ci.isPresent()) {
                    Map<String, ArtifactDataDefinition> mapToscaDataDefinition = e.getValue().getMapToscaDataDefinition();
                    Map<String, ArtifactDefinition> deplArt = mapToscaDataDefinition.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, en -> new ArtifactDefinition(en.getValue())));
                    ci.get().setArtifacts(deplArt);
                }
            });
        }
    }

    public static void setComponentInstancesDeploymentArtifactsToComponent(Map<String, MapArtifactDataDefinition> instDeploymentArtifacts,
                                                                           Component component) {
        List<ComponentInstance> instances = component.getComponentInstances();
        if (instDeploymentArtifacts != null && instances != null) {
            instDeploymentArtifacts.entrySet().forEach(e -> {
                Optional<ComponentInstance> ci = instances.stream().filter(i -> i.getUniqueId().equals(e.getKey())).findFirst();
                if (ci.isPresent()) {
                    Map<String, ArtifactDataDefinition> mapToscaDataDefinition = e.getValue().getMapToscaDataDefinition();
                    Map<String, ArtifactDefinition> deplArt = mapToscaDataDefinition.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, en -> new ArtifactDefinition(en.getValue())));
                    ci.get().setDeploymentArtifacts(deplArt);
                }
            });
        }
    }

    public static void convertComponentInstances(Component component, TopologyTemplate topologyTemplate) {
        if (!CollectionUtils.isEmpty(component.getComponentInstances())) {
            setComponentInstancesAttributesToTopologyTemplate(component, topologyTemplate);
            setComponentInstancesPropertiesToTopologyTemplate(component, topologyTemplate);
            setComponentInstancesInputsToTopologyTemplate(component, topologyTemplate);
            setComponentInstancesToTopologyTemplate(component, topologyTemplate);
            setComponentInstancesArtifactsToTopologyTemplate(component, topologyTemplate);
        }
    }

    public static void convertRelations(TopologyTemplate topologyTemplate, Component component) {
        Map<String, RelationshipInstDataDefinition> relations = topologyTemplate.getRelations();
        List<RequirementCapabilityRelDef> componentRelations;
        if (relations != null && !relations.isEmpty()) {
            componentRelations = relations.values().stream().map(ModelConverter::convertRelation).collect(Collectors.toList());
        } else {
            componentRelations = new ArrayList<>();
        }
        component.setComponentInstancesRelations(componentRelations);
    }

    public static RequirementCapabilityRelDef convertRelation(RelationshipInstDataDefinition relation) {
        RequirementCapabilityRelDef requirementCapabilityRelDef = new RequirementCapabilityRelDef();
        requirementCapabilityRelDef.setFromNode(relation.getFromId());
        requirementCapabilityRelDef.setToNode(relation.getToId());
        requirementCapabilityRelDef.setOriginUI(BooleanUtils.isTrue(relation.isOriginUI()));
        CapabilityRequirementRelationship rel = new CapabilityRequirementRelationship();
        RelationshipInfo relationshipPair = getRelationshipInfo(relation);
        rel.setRelation(relationshipPair);
        rel.setOperations(convertToOperations(relation.getInterfaces()));
        requirementCapabilityRelDef.setRelationships(Arrays.asList(rel));
        return requirementCapabilityRelDef;
    }

    private static List<OperationUi> convertToOperations(final ListDataDefinition<InterfaceDataDefinition> interfaces) {
        if (interfaces == null || interfaces.isEmpty()) {
            return Collections.emptyList();
        }
        final List<OperationUi> operationUiList = new ArrayList<>();
        for (final InterfaceDataDefinition interfaceDataDefinition : interfaces.getListToscaDataDefinition()) {
            if (MapUtils.isEmpty(interfaceDataDefinition.getOperations())) {
                continue;
            }
            for (final Entry<String, OperationDataDefinition> operationEntry : interfaceDataDefinition.getOperations().entrySet()) {
                final OperationUi operationUi = new OperationUi();
                operationUi.setOperationType(operationEntry.getKey());
                operationUi.setInterfaceType(interfaceDataDefinition.getType());
                final OperationDataDefinition operationDataDefinition = operationEntry.getValue();
                final ArtifactDataDefinition artifactDataDefinition = operationDataDefinition.getImplementation();
                if (artifactDataDefinition != null) {
                    operationUi.setImplementation(artifactDataDefinition);
                }
                final ListDataDefinition<OperationInputDefinition> inputs = operationDataDefinition.getInputs();
                if (inputs != null && !inputs.isEmpty()) {
                    final List<OperationInputDefinition> operationInputDefinitionList = inputs.getListToscaDataDefinition();
                    operationInputDefinitionList.forEach(operationInputDefinition -> {
                        final PropertyAssignmentUi propertyAssignmentUi = new PropertyAssignmentUi();
                        propertyAssignmentUi.setName(operationInputDefinition.getLabel());
                        propertyAssignmentUi.setType(operationInputDefinition.getType());
                        propertyAssignmentUi.setValue(operationInputDefinition.getValue());
                        operationUi.addToInputs(propertyAssignmentUi);
                    });
                }
                operationUiList.add(operationUi);
            }
        }
        return operationUiList;
    }

    /**
     * @param relation
     * @return
     */
    private static RelationshipInfo getRelationshipInfo(RelationshipInstDataDefinition relation) {
        RelationshipInfo relationshipPair = new RelationshipInfo();
        relationshipPair.setId(relation.getUniqueId());
        relationshipPair.setCapabilityOwnerId(relation.getCapabilityOwnerId());
        relationshipPair.setCapabilityUid(relation.getCapabilityId());
        relationshipPair.setCapability(relation.getCapability());
        relationshipPair.setRequirementOwnerId(relation.getRequirementOwnerId());
        relationshipPair.setRequirementUid(relation.getRequirementId());
        relationshipPair.setRequirement(relation.getRequirement());
        RelationshipImpl relationship = new RelationshipImpl();
        relationship.setType(relation.getType());
        relationshipPair.setRelationships(relationship);
        return relationshipPair;
    }

    public static List<RelationshipInstDataDefinition> convertRelationToToscaRelation(RequirementCapabilityRelDef relation) {
        List<RelationshipInstDataDefinition> relationsList = new ArrayList<>();
        List<CapabilityRequirementRelationship> relationship = relation.getRelationships();
        relationship.forEach(p -> {
            RelationshipInstDataDefinition requirementCapabilityRelDef = new RelationshipInstDataDefinition();
            requirementCapabilityRelDef.setFromId(relation.getFromNode());
            requirementCapabilityRelDef.setToId(relation.getToNode());
            requirementCapabilityRelDef.setUniqueId(p.getRelation().getId());
            requirementCapabilityRelDef.setCapabilityOwnerId(p.getRelation().getCapabilityOwnerId());
            requirementCapabilityRelDef.setCapabilityId(p.getRelation().getCapabilityUid());
            requirementCapabilityRelDef.setRequirementOwnerId(p.getRelation().getRequirementOwnerId());
            requirementCapabilityRelDef.setRequirementId(p.getRelation().getRequirementUid());
            requirementCapabilityRelDef.setRequirement(p.getRelation().getRequirement());
            requirementCapabilityRelDef.setType(p.getRelation().getRelationship().getType());
            requirementCapabilityRelDef.setCapability(p.getRelation().getCapability());
            requirementCapabilityRelDef.setOriginUI(relation.isOriginUI());
            relationsList.add(requirementCapabilityRelDef);
        });
        return relationsList;
    }

    private static void convertCapabilities(Component component, TopologyTemplate topologyTemplate) {
        convertTopologyTemplateCapabilities(component, topologyTemplate);
        if (componentInstancesCapabilitiesExist(component) || groupsCapabilitiesExist(component)) {
            topologyTemplate.setCalculatedCapabilities(new HashMap<>());
            topologyTemplate.setCalculatedCapabilitiesProperties(new HashMap<>());
        }
        convertComponentInstancesCapabilities(component, topologyTemplate);
        convertGroupsCapabilities(component, topologyTemplate);
    }

    private static void convertGroupsCapabilities(Component component, TopologyTemplate topologyTemplate) {
        if (groupsCapabilitiesExist(component)) {
            component.getGroups().stream().filter(g -> MapUtils.isNotEmpty(g.getCapabilities()))
                .forEach(g -> addCapabilities(topologyTemplate, g.getCapabilities(), g.getUniqueId()));
        }
    }

    private static void convertTopologyTemplateCapabilities(Component component, ToscaElement toscaElement) {
        Map<String, List<CapabilityDefinition>> capabilities = component.getCapabilities();
        Map<String, ListCapabilityDataDefinition> toscaCapMap = new HashMap<>();
        Map<String, MapPropertiesDataDefinition> toscaCapPropMap = new HashMap<>();
        if (MapUtils.isNotEmpty(capabilities)) {
            capabilities.forEach((s, caps) -> {
                if (CollectionUtils.isNotEmpty(caps)) {
                    List<CapabilityDataDefinition> capList = caps.stream()
                        .filter(capabilityDefinition -> capabilityDefinition.getOwnerId().equals(component.getUniqueId()))
                        .map(CapabilityDataDefinition::new).collect(Collectors.toList());
                    populateCapabilityMap(toscaCapMap, toscaCapPropMap, s, caps, capList);
                }
            });
            toscaElement.setCapabilities(toscaCapMap);
            toscaElement.setCapabilitiesProperties(toscaCapPropMap);
        }
    }

    private static void populateCapabilityMap(Map<String, ListCapabilityDataDefinition> toscaCapMap,
                                              Map<String, MapPropertiesDataDefinition> toscaCapPropMap, String s, List<CapabilityDefinition> caps,
                                              List<CapabilityDataDefinition> capList) {
        ListCapabilityDataDefinition listCapabilityDataDefinition = new ListCapabilityDataDefinition(capList);
        toscaCapMap.put(s, listCapabilityDataDefinition);
        for (CapabilityDefinition cap : caps) {
            List<ComponentInstanceProperty> capPrps = cap.getProperties();
            if (CollectionUtils.isNotEmpty(capPrps)) {
                MapPropertiesDataDefinition dataToCreate = new MapPropertiesDataDefinition();
                for (ComponentInstanceProperty cip : capPrps) {
                    PropertyDataDefinition propertyDataDefinition = new PropertyDataDefinition(cip);
                    propertyDataDefinition.setParentUniqueId(cap.getUniqueId());
                    dataToCreate.put(cip.getName(), propertyDataDefinition);
                }
                toscaCapPropMap.put(s + CAP_PROP_DELIM + cap.getName(), dataToCreate);
            }
        }
    }

    private static void convertTopologyTemplateRequirements(Component component, ToscaElement toscaElement) {
        Map<String, List<RequirementDefinition>> requirements = component.getRequirements();
        Map<String, ListRequirementDataDefinition> toscaReqMap = new HashMap<>();
        if (MapUtils.isNotEmpty(requirements)) {
            requirements.forEach((s, reqs) -> {
                if (CollectionUtils.isNotEmpty(reqs)) {
                    List<RequirementDataDefinition> reqList = reqs.stream()
                        .filter(requirementDefinition -> requirementDefinition.getOwnerId().equals(component.getUniqueId()))
                        .map(RequirementDataDefinition::new).collect(Collectors.toList());
                    ListRequirementDataDefinition listRequirementDataDefinition = new ListRequirementDataDefinition(reqList);
                    toscaReqMap.put(s, listRequirementDataDefinition);
                }
            });
            toscaElement.setRequirements(toscaReqMap);
        }
    }

    private static void convertComponentInstancesCapabilities(Component component, TopologyTemplate topologyTemplate) {
        if (componentInstancesCapabilitiesExist(component)) {
            component.getComponentInstances().stream().filter(i -> MapUtils.isNotEmpty(i.getCapabilities()))
                .forEach(i -> addCapabilities(topologyTemplate, i.getCapabilities(), i.getUniqueId()));
        }
    }

    private static void addCapabilities(TopologyTemplate topologyTemplate, Map<String, List<CapabilityDefinition>> capabilities, String ownerId) {
        if (MapUtils.isNotEmpty(capabilities)) {
            if (topologyTemplate.getCalculatedCapabilities() == null) {
                topologyTemplate.setCalculatedCapabilities(new HashMap<>());
            }
            topologyTemplate.getCalculatedCapabilities().put(ownerId, convertToMapListCapabiltyDataDefinition(capabilities));
            if (topologyTemplate.getCalculatedCapabilitiesProperties() == null) {
                topologyTemplate.setCalculatedCapabilitiesProperties(new HashMap<>());
            }
            topologyTemplate.getCalculatedCapabilitiesProperties().put(ownerId, convertToMapOfMapCapabiltyProperties(capabilities, ownerId));
        }
    }

    private static boolean componentInstancesCapabilitiesExist(Component component) {
        return component.getCapabilities() != null && component.getComponentInstances() != null && component.getComponentInstances().stream()
            .anyMatch(ci -> MapUtils.isNotEmpty(ci.getCapabilities()));
    }

    private static boolean groupsCapabilitiesExist(Component component) {
        return component.getCapabilities() != null && component.getGroups() != null && component.getGroups().stream()
            .anyMatch(g -> MapUtils.isNotEmpty(g.getCapabilities()));
    }

    public static MapCapabilityProperty convertToMapOfMapCapabilityProperties(Map<String, List<CapabilityDefinition>> capabilities, String ownerId,
                                                                              boolean isAtomicType) {
        Map<String, MapPropertiesDataDefinition> toscaCapPropMap = new HashMap<>();
        if (MapUtils.isNotEmpty(capabilities)) {
            capabilities.forEach((s, caps) -> addCapsProperties(ownerId, isAtomicType, toscaCapPropMap, caps));
        }
        return new MapCapabilityProperty(toscaCapPropMap);
    }

    private static void addCapsProperties(String ownerId, boolean isAtomicType, Map<String, MapPropertiesDataDefinition> toscaCapPropMap,
                                          List<CapabilityDefinition> caps) {
        if (CollectionUtils.isNotEmpty(caps)) {
            caps.forEach(cap -> addCapProperties(ownerId, isAtomicType, toscaCapPropMap, cap));
        }
    }

    private static void addCapProperties(String ownerId, boolean isAtomicType, Map<String, MapPropertiesDataDefinition> toscaCapPropMap,
                                         CapabilityDefinition cap) {
        if (CollectionUtils.isNotEmpty(cap.getProperties())) {
            MapPropertiesDataDefinition dataToCreate = new MapPropertiesDataDefinition(
                cap.getProperties().stream().map(PropertyDataDefinition::new).collect(Collectors.toMap(PropertyDataDefinition::getName, p -> p)));
            toscaCapPropMap.put(buildCapabilityPropertyKey(isAtomicType, cap.getType(), cap.getName(), ownerId, cap),
                new MapPropertiesDataDefinition(dataToCreate));
        }
    }

    public static String buildCapabilityPropertyKey(boolean isAtomicType, String capabilityType, String capabilityName,
                                                    String componentInstanceUniqueId, CapabilityDefinition cap) {
        StringBuilder sb = new StringBuilder(componentInstanceUniqueId);
        sb.append(CAP_PROP_DELIM).append(cap.getOwnerId()).append(CAP_PROP_DELIM);
        if (!isAtomicType && !componentInstanceUniqueId.equals(cap.getOwnerId())) {
            sb.append(cap.getOwnerId()).append(CAP_PROP_DELIM);
        }
        return sb.append(capabilityType).append(CAP_PROP_DELIM).append(capabilityName).toString();
    }

    public static MapCapabilityProperty convertToMapOfMapCapabiltyProperties(Map<String, List<CapabilityDefinition>> instCapabilities,
                                                                             String ownerId) {
        return convertToMapOfMapCapabiltyProperties(instCapabilities, ownerId, false);
    }

    public static MapCapabilityProperty convertToMapOfMapCapabiltyProperties(Map<String, List<CapabilityDefinition>> capabilities, String ownerId,
                                                                             boolean fromCsar) {
        Map<String, MapPropertiesDataDefinition> toscaCapPropMap = new HashMap<>();
        if (MapUtils.isNotEmpty(capabilities)) {
            capabilities.forEach((s, caps) -> {
                if (caps != null && !caps.isEmpty()) {
                    MapPropertiesDataDefinition dataToCreate = new MapPropertiesDataDefinition();
                    for (CapabilityDefinition cap : caps) {
                        List<ComponentInstanceProperty> capPrps = cap.getProperties();
                        if (capPrps != null) {
                            for (ComponentInstanceProperty cip : capPrps) {
                                dataToCreate.put(cip.getName(), new PropertyDataDefinition(cip));
                            }
                            // format key of capability properties :

                            // VF instance in service : instanceId#ownerId#type#capName

                            // VFC instance ion VF : instanceId#ownerId#type#capName -> instanceId=ownerId
                            StringBuilder sb = new StringBuilder(ownerId);
                            sb.append(CAP_PROP_DELIM);
                            if (fromCsar) {
                                sb.append(ownerId);
                            } else {
                                sb.append(cap.getOwnerId());
                            }
                            sb.append(CAP_PROP_DELIM).append(s).append(CAP_PROP_DELIM).append(cap.getName());
                            toscaCapPropMap.put(sb.toString(), new MapPropertiesDataDefinition(dataToCreate));
                        }
                    }
                }
            });
        }
        return new MapCapabilityProperty(toscaCapPropMap);
    }

    private static MapListCapabilityDataDefinition convertToMapListCapabiltyDataDefinition(Map<String, List<CapabilityDefinition>> instCapabilities) {
        Map<String, ListCapabilityDataDefinition> mapToscaDataDefinition = new HashMap<>();
        for (Entry<String, List<CapabilityDefinition>> instCapability : instCapabilities.entrySet()) {
            mapToscaDataDefinition.put(instCapability.getKey(),
                new ListCapabilityDataDefinition(instCapability.getValue().stream().map(CapabilityDataDefinition::new).collect(Collectors.toList())));
        }
        return new MapListCapabilityDataDefinition(mapToscaDataDefinition);
    }

    private static void convertRequirements(Component component, TopologyTemplate topologyTemplate) {
        convertTopologyTemplateRequirements(component, topologyTemplate);
        if (component.getRequirements() != null && component.getComponentInstances() != null) {
            topologyTemplate.setCalculatedRequirements(new HashMap<>());
            for (ComponentInstance instance : component.getComponentInstances()) {
                Map<String, List<RequirementDefinition>> instRequirements = instance.getRequirements();
                if (MapUtils.isNotEmpty(instRequirements)) {
                    if (topologyTemplate.getCalculatedRequirements() == null) {
                        topologyTemplate.setCalculatedRequirements(new HashMap<>());
                    }
                    topologyTemplate.getCalculatedRequirements()
                        .put(instance.getUniqueId(), convertToMapListRequirementDataDefinition(instRequirements));
                }
            }
        }
    }

    private static MapListRequirementDataDefinition convertToMapListRequirementDataDefinition(
        Map<String, List<RequirementDefinition>> instRequirements) {
        Map<String, ListRequirementDataDefinition> mapToscaDataDefinition = new HashMap<>();
        for (Entry<String, List<RequirementDefinition>> instRequirement : instRequirements.entrySet()) {
            mapToscaDataDefinition.put(instRequirement.getKey(), new ListRequirementDataDefinition(
                instRequirement.getValue().stream().map(RequirementDataDefinition::new).collect(Collectors.toList())));
        }
        return new MapListRequirementDataDefinition(mapToscaDataDefinition);
    }

    @SuppressWarnings("unchecked")
    private static void convertComponentFields(Component component, ToscaElement toscaElement) {
        component.setName(toscaElement.getName());
        component.setAllVersions(toscaElement.getAllVersions());
        component.setCategories(toscaElement.getCategories());
        component.setComponentType(toscaElement.getComponentType());
        component.setCreationDate(toscaElement.getCreationDate());
        component.setCreatorUserId(toscaElement.getCreatorUserId());
        component.setCreatorFullName(toscaElement.getCreatorFullName());
        component.setLastUpdateDate(toscaElement.getLastUpdateDate());
        component.setLastUpdaterFullName(toscaElement.getLastUpdaterFullName());
        component.setLastUpdaterUserId(toscaElement.getLastUpdaterUserId());
        component.setNormalizedName(toscaElement.getNormalizedName());
        component.setLifecycleState(toscaElement.getLifecycleState());
        component.setVersion(toscaElement.getVersion());
        component.setHighestVersion(toscaElement.isHighestVersion());
        component.setUniqueId(toscaElement.getUniqueId());
        component.setSystemName((String) toscaElement.getMetadataValue(JsonPresentationFields.SYSTEM_NAME));
        component.setDerivedFromGenericType(toscaElement.getDerivedFromGenericType());
        component.setDerivedFromGenericVersion(toscaElement.getDerivedFromGenericVersion());
        if(toscaElement.getModel() != null) {
            component.setModel(toscaElement.getModel());
        }
        Map<String, PropertyDataDefinition> properties = toscaElement.getProperties();
        if (MapUtils.isNotEmpty(properties)) {
            List<PropertyDefinition> propertiesMap = properties.values().stream().map(x -> new PropertyDefinition(x)).collect(Collectors.toList());
            component.setProperties(propertiesMap);
        }
        //archive
        component.setArchived(toscaElement.isArchived() == null ? false : toscaElement.isArchived());
        component.setArchiveTime(toscaElement.getArchiveTime());
        component.setVspArchived(toscaElement.isVspArchived() == null ? false : toscaElement.isVspArchived());
        if (component.getComponentType() == ComponentTypeEnum.RESOURCE) {
            Resource resource = (Resource) component;
            resource.setAbstract((Boolean) toscaElement.getMetadataValue(JsonPresentationFields.IS_ABSTRACT));
            resource.setToscaResourceName((String) toscaElement.getMetadataValue(JsonPresentationFields.TOSCA_RESOURCE_NAME));
            resource.setVendorName((String) toscaElement.getMetadataValue(JsonPresentationFields.VENDOR_NAME));
            resource.setVendorRelease((String) toscaElement.getMetadataValue(JsonPresentationFields.VENDOR_RELEASE));
            // field isn't mandatory , but shouldn't be null(should be an empty string instead)
            if (((String) toscaElement.getMetadataValue(JsonPresentationFields.RESOURCE_VENDOR_MODEL_NUMBER)) != null) {
                resource.setResourceVendorModelNumber((String) toscaElement.getMetadataValue(JsonPresentationFields.RESOURCE_VENDOR_MODEL_NUMBER));
            } else {
                resource.setResourceVendorModelNumber("");
            }
            Boolean isNormative = toscaElement.getMetadataValue(JsonPresentationFields.NORMATIVE) == null ? false : (Boolean) toscaElement.getMetadataValue(JsonPresentationFields.NORMATIVE);
            resource.getComponentMetadataDefinition().getMetadataDataDefinition().setNormative(isNormative);
        } else if (component.getComponentType() == ComponentTypeEnum.SERVICE) {
            Service service = (Service) component;
            if (((String) toscaElement.getMetadataValue(JsonPresentationFields.SERVICE_TYPE)) != null) {
                service.setServiceType((String) toscaElement.getMetadataValue(JsonPresentationFields.SERVICE_TYPE));
            } else {
                service.setServiceType("");
            }
            if (((String) toscaElement.getMetadataValue(JsonPresentationFields.SERVICE_ROLE)) != null) {
                service.setServiceRole((String) toscaElement.getMetadataValue(JsonPresentationFields.SERVICE_ROLE));
            } else {
                service.setServiceRole("");
            }
            if (((String) toscaElement.getMetadataValue(JsonPresentationFields.SERVICE_FUNCTION)) != null) {
                service.setServiceFunction((String) toscaElement.getMetadataValue(JsonPresentationFields.SERVICE_FUNCTION));
            } else {
                service.setServiceFunction("");
            }
        }
        component.setConformanceLevel((String) toscaElement.getMetadataValue(JsonPresentationFields.CONFORMANCE_LEVEL));
        component.setIcon((String) toscaElement.getMetadataValue(JsonPresentationFields.ICON));
        component.setDescription((String) toscaElement.getMetadataValue(JsonPresentationFields.DESCRIPTION));
        component.setTags((List<String>) toscaElement.getMetadataValue(JsonPresentationFields.TAGS));
        component.setInvariantUUID((String) toscaElement.getMetadataValue(JsonPresentationFields.INVARIANT_UUID));
        component.setContactId((String) toscaElement.getMetadataValue(JsonPresentationFields.CONTACT_ID));
        component.setUUID((String) toscaElement.getMetadataValue(JsonPresentationFields.UUID));
        component.setIsDeleted((Boolean) toscaElement.getMetadataValue(JsonPresentationFields.IS_DELETED));
        component.setModel((String) toscaElement.getMetadataValue(JsonPresentationFields.MODEL));
        component.setToscaType(toscaElement.getToscaType().getValue());
        final List<MetadataKeyDataDefinition> metadataKeys = getCategorySpecificMetadataKeys(toscaElement);
        if (CollectionUtils.isNotEmpty(metadataKeys)) {
            final Map<String, String> categorySpecificMetadata = new HashMap<>();
            for (final MetadataKeyDataDefinition metadataKey : metadataKeys) {
                if (toscaElement.getMetadata().get(metadataKey.getName()) != null) {
                    categorySpecificMetadata.put(metadataKey.getName(), (String) toscaElement.getMetadata().get(metadataKey.getName()));
                } else if (metadataKey.getDefaultValue() != null && metadataKey.isMandatory()) {
                    categorySpecificMetadata.put(metadataKey.getName(), metadataKey.getDefaultValue());
                }
            }
            component.setCategorySpecificMetadata(categorySpecificMetadata);
        }
    }
    
    private static List<MetadataKeyDataDefinition> getCategorySpecificMetadataKeys(final ToscaElement toscaElement) {
        final List<MetadataKeyDataDefinition> metadataKeys = new ArrayList<>();
        final Optional<CategoryDefinition> category = getCategory(toscaElement);
        if (category.isPresent()) {
            if (CollectionUtils.isNotEmpty(category.get().getMetadataKeys())) {
                metadataKeys.addAll(category.get().getMetadataKeys());
            }
            final Optional<SubCategoryDefinition> subCategory = getSubCategory(category.get());
            if (subCategory.isPresent() && CollectionUtils.isNotEmpty(subCategory.get().getMetadataKeys())) {
                metadataKeys.addAll(subCategory.get().getMetadataKeys());
            }
        }
        return metadataKeys;
    }

    private static Optional<CategoryDefinition> getCategory(ToscaElement toscaElement) {
        return CollectionUtils.isEmpty(toscaElement.getCategories()) ? Optional.empty() : Optional.of(toscaElement.getCategories().get(0));
    }

    private static Optional<SubCategoryDefinition> getSubCategory(CategoryDefinition category) {
        return CollectionUtils.isEmpty(category.getSubcategories()) ? Optional.empty() : Optional.of(category.getSubcategories().get(0));
    }

    private static NodeType convertToNodeType(Component component) {
        Resource resource = (Resource) component;
        NodeType nodeType = new NodeType();
        nodeType.setDerivedFrom(resource.getDerivedFrom());
        nodeType.setDerivedList(resource.getDerivedList());
        nodeType.setResourceType(resource.getResourceType());
        final String toscaVersion = resource.getToscaVersion();
        if (toscaVersion != null) {
            nodeType.setToscaVersion(toscaVersion);
        }
        final List<DataTypeDefinition> dataTypes = resource.getDataTypes();
        if (CollectionUtils.isNotEmpty(dataTypes)) {
            nodeType
                .setDataTypes(dataTypes.stream().collect(Collectors.toMap(DataTypeDefinition::getName, dataTypeDefinition -> dataTypeDefinition)));
        }
        convertCommonToscaData(component, nodeType);
        convertAdditionalInformation(component, nodeType);
        convertArtifacts(resource, nodeType);
        convertCapabilities(resource, nodeType);
        convertRequirements(resource, nodeType);
        convertAttributesToNodeType(resource, nodeType);
        convertProperties(resource, nodeType);
        convertInterfaces(resource, nodeType);
        return nodeType;
    }

    private static void convertProperties(Resource resource, NodeType nodeType) {
        List<PropertyDefinition> properties = resource.getProperties();
        if (properties != null && !properties.isEmpty()) {
            Map<String, PropertyDataDefinition> propertiesMap = properties.stream()
                .collect(Collectors.toMap(PropertyDefinition::getName, PropertyDataDefinition::new));
            nodeType.setProperties(propertiesMap);
        }
    }

    private static void convertInterfaces(Resource resource, NodeType nodeType) {
        Map<String, InterfaceDefinition> interfaces = resource.getInterfaces();
        if (interfaces != null) {
            Map<String, InterfaceDataDefinition> interfaceArtifacts = interfaces.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, x -> new InterfaceDataDefinition(x.getValue())));
            nodeType.setInterfaceArtifacts(interfaceArtifacts);
        }
    }

    private static void convertAdditionalInformation(Component component, ToscaElement toscaElement) {
        List<AdditionalInformationDefinition> additionalInformation = component.getAdditionalInformation();
        if (additionalInformation != null) {
            Map<String, AdditionalInfoParameterDataDefinition> addInfo = additionalInformation.stream()
                .collect(Collectors.toMap(AdditionalInformationDefinition::getUniqueId, AdditionalInfoParameterDataDefinition::new));
            toscaElement.setAdditionalInformation(addInfo);
        }
    }

    private static void convertAdditionalInformation(ToscaElement toscaElement, Component resource) {
        Map<String, AdditionalInfoParameterDataDefinition> additionalInformation = toscaElement.getAdditionalInformation();
        if (additionalInformation != null) {
            List<AdditionalInformationDefinition> addInfo = additionalInformation.values().stream().map(AdditionalInformationDefinition::new)
                .collect(Collectors.toList());
            resource.setAdditionalInformation(addInfo);
        }
    }

    private static void convertArtifacts(ToscaElement toscaElement, Component component) {
        Map<String, ArtifactDataDefinition> artifacts = toscaElement.getArtifacts();
        Map<String, ArtifactDefinition> copy;
        if (artifacts != null) {
            copy = artifacts.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDefinition(e.getValue())));
        } else {
            copy = new HashMap<>();
        }
        component.setArtifacts(copy);
        Map<String, ArtifactDataDefinition> toscaArtifacts = toscaElement.getToscaArtifacts();
        if (toscaArtifacts != null) {
            copy = toscaArtifacts.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDefinition(e.getValue())));
        } else {
            copy = new HashMap<>();
        }
        component.setToscaArtifacts(copy);
        Map<String, ArtifactDataDefinition> deploymentArtifacts = toscaElement.getDeploymentArtifacts();
        if (deploymentArtifacts != null) {
            copy = deploymentArtifacts.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDefinition(e.getValue())));
        } else {
            copy = new HashMap<>();
        }
        component.setDeploymentArtifacts(copy);
    }

    private static void convertNodeFiltersComponents(final TopologyTemplate topologyTemplate, final Component component) {
        final Map<String, CINodeFilterDataDefinition> filters = topologyTemplate.getNodeFilterComponents();
        final Map<String, CINodeFilterDataDefinition> copy;
        if (MapUtils.isNotEmpty(filters)) {
            copy = filters.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new CINodeFilterDataDefinition(e.getValue())));
        } else {
            copy = new HashMap<>();
        }
        component.setNodeFilterComponents(copy);
    }

    private static void convertSubstitutionFiltersComponents(final TopologyTemplate topologyTemplate, final Component component) {
        final SubstitutionFilterDataDefinition filters = topologyTemplate.getSubstitutionFilters();
        if (filters == null){
            component.setSubstitutionFilter(null);
            return;
        }
        component.setSubstitutionFilter(new SubstitutionFilterDataDefinition(filters));
    }

    private static void convertServiceApiArtifacts(TopologyTemplate topologyTemplate, Service service) {
        Map<String, ArtifactDataDefinition> serviceApiArtifacts = topologyTemplate.getServiceApiArtifacts();
        Map<String, ArtifactDefinition> copy;
        if (serviceApiArtifacts != null) {
            copy = serviceApiArtifacts.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDefinition(e.getValue())));
        } else {
            copy = new HashMap<>();
        }
        service.setServiceApiArtifacts(copy);
    }

    private static void convertServicePaths(TopologyTemplate topologyTemplate, Service service) {
        Map<String, ForwardingPathDataDefinition> servicePaths = topologyTemplate.getForwardingPaths();
        Map<String, ForwardingPathDataDefinition> copy;
        if (servicePaths != null) {
            copy = servicePaths.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ForwardingPathDataDefinition(e.getValue())));
        } else {
            copy = new HashMap<>();
        }
        service.setForwardingPaths(copy);
    }

    private static void convertArtifacts(Component component, ToscaElement toscaElement) {
        Map<String, ArtifactDefinition> artifacts = component.getArtifacts();
        if (artifacts != null) {
            Map<String, ArtifactDataDefinition> copy = artifacts.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDataDefinition(e.getValue())));
            toscaElement.setArtifacts(copy);
        }
        Map<String, ArtifactDefinition> toscaArtifacts = component.getToscaArtifacts();
        if (toscaArtifacts != null) {
            Map<String, ArtifactDataDefinition> copy = toscaArtifacts.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDataDefinition(e.getValue())));
            toscaElement.setToscaArtifacts(copy);
        }
        Map<String, ArtifactDefinition> deploymentArtifacts = component.getDeploymentArtifacts();
        if (deploymentArtifacts != null) {
            Map<String, ArtifactDataDefinition> copy = deploymentArtifacts.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDataDefinition(e.getValue())));
            toscaElement.setDeploymentArtifacts(copy);
        }
    }

    private static void convertServiceApiArtifacts(Service service, TopologyTemplate topologyTemplate) {
        Map<String, ArtifactDefinition> serviceApiArtifacts = service.getServiceApiArtifacts();
        if (serviceApiArtifacts != null) {
            Map<String, ArtifactDataDefinition> copy = serviceApiArtifacts.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDataDefinition(e.getValue())));
            topologyTemplate.setServiceApiArtifacts(copy);
        }
    }

    private static void convertCapabilities(Component component, NodeType toscaElement) {
        Map<String, List<CapabilityDefinition>> capabilities = component.getCapabilities();
        Map<String, ListCapabilityDataDefinition> toscaCapMap = new HashMap<>();
        Map<String, MapPropertiesDataDefinition> toscaCapPropMap = new HashMap<>();
        if (capabilities != null && !capabilities.isEmpty()) {
            capabilities.forEach((s, caps) -> {
                if (caps != null && !caps.isEmpty()) {
                    List<CapabilityDataDefinition> capList = caps.stream().map(CapabilityDataDefinition::new).collect(Collectors.toList());
                    populateCapabilityMap(toscaCapMap, toscaCapPropMap, s, caps, capList);
                }
            });
            toscaElement.setCapabilities(toscaCapMap);
            toscaElement.setCapabilitiesProperties(toscaCapPropMap);
        }
    }

    private static void convertAttributesToNodeType(Resource component, NodeType nodeType) {
        List<AttributeDefinition> attributes = component.getAttributes();
        if (CollectionUtils.isNotEmpty(attributes)) {
            Map<String, AttributeDataDefinition> attrsByName = attributes.stream()
                .collect(Collectors.toMap(AttributeDefinition::getName, Function.identity()));
            nodeType.setAttributes(attrsByName);
        }
    }

    private static void convertRequirements(Resource component, NodeType nodeType) {
        Map<String, List<RequirementDefinition>> requirements = component.getRequirements();
        Map<String, ListRequirementDataDefinition> toscaReqMap = new HashMap<>();
        if (requirements != null && !requirements.isEmpty()) {
            requirements.forEach((s, reqs) -> {
                if (reqs != null && !reqs.isEmpty()) {
                    List<RequirementDataDefinition> reqList = reqs.stream().map(RequirementDataDefinition::new).collect(Collectors.toList());
                    ListRequirementDataDefinition listRequirementDataDefinition = new ListRequirementDataDefinition(reqList);
                    toscaReqMap.put(s, listRequirementDataDefinition);
                }
            });
            nodeType.setRequirements(toscaReqMap);
        }
    }

    private static void convertCapabilities(NodeType toscaElement, Component component) {
        Map<String, ListCapabilityDataDefinition> toscaCapabilities = toscaElement.getCapabilities();
        Map<String, MapPropertiesDataDefinition> toscaCapPropMap = toscaElement.getCapabilitiesProperties();
        Map<String, List<CapabilityDefinition>> capabilitiesMapFromMapObject = getCapabilitiesMapFromMapObject(toscaCapabilities, toscaCapPropMap);
        if (capabilitiesMapFromMapObject != null) {
            component.setCapabilities(capabilitiesMapFromMapObject);
        }
    }

    public static Map<String, List<CapabilityDefinition>> getCapabilitiesMapFromMapObject(Map<String, ListCapabilityDataDefinition> toscaCapabilities,
                                                                                          Map<String, MapPropertiesDataDefinition> toscaCapPropMap) {
        Map<String, List<CapabilityDefinition>> compCap = new HashMap<>();
        if (toscaCapabilities == null || toscaCapabilities.isEmpty()) {
            return null;
        }
        toscaCapabilities.forEach((s, cap) -> {
            if (cap != null) {
                List<CapabilityDataDefinition> capDataList = cap.getListToscaDataDefinition();
                if (capDataList != null && !capDataList.isEmpty()) {
                    List<CapabilityDefinition> capList = capDataList.stream().map(CapabilityDefinition::new).collect(Collectors.toList());
                    compCap.put(s, capList);
                }
            }
        });
        if (toscaCapPropMap != null && !toscaCapPropMap.isEmpty()) {
            toscaCapPropMap.forEach((s, capProp) -> {
                String[] result = s.split(CAP_PROP_DELIM);
                if (capProp != null) {
                    Map<String, PropertyDataDefinition> capMap = capProp.getMapToscaDataDefinition();
                    if (capMap != null && !capMap.isEmpty()) {
                        List<ComponentInstanceProperty> capPropsList = capMap.values().stream().map(ComponentInstanceProperty::new)
                            .collect(Collectors.toList());
                        List<CapabilityDefinition> cap = compCap.get(result[0]);
                        Optional<CapabilityDefinition> op = cap.stream().filter(c -> c.getName().equals(result[1])).findFirst();
                        op.ifPresent(capabilityDefinition -> capabilityDefinition.setProperties(capPropsList));
                    }
                }
            });
        }
        return compCap;
    }

    private static void convertGroups(TopologyTemplate toscaElement, Component component) {
        Map<String, GroupDataDefinition> toscaGroups = toscaElement.getGroups();
        List<GroupDefinition> groupDefinitions = null;
        if (MapUtils.isNotEmpty(toscaGroups)) {
            groupDefinitions = toscaGroups.values().stream().map(GroupDefinition::new).collect(Collectors.toList());
        }
        component.setGroups(groupDefinitions);
    }

    private static void convertPolicies(TopologyTemplate toscaElement, Component component) {
        Map<String, PolicyDataDefinition> policies = toscaElement.getPolicies();
        Map<String, PolicyDefinition> policyDefinitions = null;
        if (MapUtils.isNotEmpty(policies)) {
            policyDefinitions = policies.values().stream().map(PolicyDefinition::new)
                .collect(Collectors.toMap(PolicyDefinition::getUniqueId, Function.identity()));
        }
        component.setPolicies(policyDefinitions);
    }

    private static void convertGroups(Component component, TopologyTemplate toscaElement) {
        List<GroupDefinition> groupDefinitions = component.getGroups();
        Map<String, GroupDataDefinition> groups = new HashMap<>();
        if (groupDefinitions != null && groups.isEmpty()) {
            groups = groupDefinitions.stream().collect(Collectors.toMap(GroupDefinition::getInvariantName, GroupDefinition::new));
        }
        toscaElement.setGroups(groups);
    }

    private static void convertPolicies(Component component, TopologyTemplate toscaElement) {
        Map<String, PolicyDefinition> policyDefinitions = component.getPolicies();
        Map<String, PolicyDataDefinition> policies = new HashMap<>();
        if (MapUtils.isNotEmpty(policyDefinitions)) {
            policies = policyDefinitions.values().stream().collect((Collectors.toMap(PolicyDefinition::getUniqueId, PolicyDataDefinition::new)));
        }
        toscaElement.setPolicies(policies);
    }

    private static void convertRequirements(NodeType toscaElement, Component component) {
        Map<String, ListRequirementDataDefinition> toscaRequirements = toscaElement.getRequirements();
        Map<String, List<RequirementDefinition>> compReqs = new HashMap<>();
        if (toscaRequirements == null || toscaRequirements.isEmpty()) {
            return;
        }
        toscaRequirements.forEach((s, req) -> {
            if (req != null) {
                List<RequirementDataDefinition> reqDataList = req.getListToscaDataDefinition();
                if (reqDataList != null && !reqDataList.isEmpty()) {
                    List<RequirementDefinition> reqList = reqDataList.stream().map(RequirementDefinition::new).collect(Collectors.toList());
                    compReqs.put(s, reqList);
                }
            }
        });
        component.setRequirements(compReqs);
    }

    private static TopologyTemplate convertToTopologyTemplate(Component component) {
        TopologyTemplate topologyTemplate;
        ComponentTypeEnum componentType = component.getComponentType();
        topologyTemplate = new TopologyTemplate();
        if (componentType == ComponentTypeEnum.RESOURCE) {
            Resource resource = (Resource) component;
            topologyTemplate.setResourceType(resource.getResourceType());
            topologyTemplate.setMetadataValue(JsonPresentationFields.CSAR_UUID, resource.getCsarUUID());
            topologyTemplate.setMetadataValue(JsonPresentationFields.CSAR_VERSION, resource.getCsarVersion());
            topologyTemplate.setMetadataValue(JsonPresentationFields.CSAR_VERSION_ID, resource.getCsarVersionId());
            topologyTemplate.setMetadataValue(JsonPresentationFields.IMPORTED_TOSCA_CHECKSUM, resource.getImportedToscaChecksum());
            convertTopologyTemplateInterfaces(resource, topologyTemplate);
        }
        if (componentType == ComponentTypeEnum.SERVICE) {
            convertServiceSpecificEntities((Service) component, topologyTemplate);
        }
        convertCommonToscaData(component, topologyTemplate);
        convertArtifacts(component, topologyTemplate);
        convertAdditionalInformation(component, topologyTemplate);
        convertComponentInstances(component, topologyTemplate);
        convertInputs(component, topologyTemplate);
        convertProperties(component, topologyTemplate);
        convertAttributesToTopologyTemplate(component, topologyTemplate);
        convertCapabilities(component, topologyTemplate);
        convertGroups(component, topologyTemplate);
        convertPolicies(component, topologyTemplate);
        convertRequirements(component, topologyTemplate);
        convertRelationsToComposition(component, topologyTemplate);
        return topologyTemplate;
    }

    private static void convertTopologyTemplateInterfaces(Resource resource, TopologyTemplate topologyTemplate) {
        Map<String, InterfaceDefinition> interfaces = resource.getInterfaces();
        if (interfaces != null && !interfaces.isEmpty()) {
            Map<String, InterfaceDataDefinition> copy = interfaces.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new InterfaceDataDefinition(e.getValue())));
            topologyTemplate.setInterfaces(copy);
        }
    }

    private static void convertServiceSpecificEntities(Service service, TopologyTemplate topologyTemplate) {
        convertServiceMetaData(service, topologyTemplate);
        convertServiceApiArtifacts(service, topologyTemplate);
        convertServicePaths(service, topologyTemplate);
        convertServiceInterfaces(topologyTemplate, service);
    }

    private static void convertServicePaths(Service service, TopologyTemplate topologyTemplate) {
        Map<String, ForwardingPathDataDefinition> servicePaths = service.getForwardingPaths();
        if (servicePaths != null && !servicePaths.isEmpty()) {
            Map<String, ForwardingPathDataDefinition> copy = servicePaths.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new ForwardingPathDataDefinition(e.getValue())));
            topologyTemplate.setForwardingPaths(copy);
        }
    }

    private static void convertServiceMetaData(Service service, TopologyTemplate topologyTemplate) {
        if (service.getDistributionStatus() != null) {
            topologyTemplate.setMetadataValue(JsonPresentationFields.DISTRIBUTION_STATUS, service.getDistributionStatus().name());
        }
        topologyTemplate.setMetadataValue(JsonPresentationFields.PROJECT_CODE, service.getProjectCode());
        topologyTemplate.setMetadataValue(JsonPresentationFields.ECOMP_GENERATED_NAMING, service.isEcompGeneratedNaming());
        topologyTemplate.setMetadataValue(JsonPresentationFields.NAMING_POLICY, service.getNamingPolicy());
        topologyTemplate.setMetadataValue(JsonPresentationFields.ENVIRONMENT_CONTEXT, service.getEnvironmentContext());
        topologyTemplate.setMetadataValue(JsonPresentationFields.INSTANTIATION_TYPE, service.getInstantiationType());
    }

    private static void convertRelationsToComposition(Component component, TopologyTemplate topologyTemplate) {
        List<RequirementCapabilityRelDef> componentInstancesRelations = component.getComponentInstancesRelations();
        if (componentInstancesRelations != null) {
            Map<String, CompositionDataDefinition> compositions = topologyTemplate.getCompositions();
            if (compositions == null) {
                compositions = new HashMap<>();
            }
            CompositionDataDefinition compositionDataDefinition = compositions.get(JsonConstantKeysEnum.COMPOSITION.getValue());
            if (compositionDataDefinition == null) {
                compositionDataDefinition = new CompositionDataDefinition();
                compositions.put(JsonConstantKeysEnum.COMPOSITION.getValue(), compositionDataDefinition);
            }
            Map<String, RelationshipInstDataDefinition> relations = componentInstancesRelations.stream()
                .flatMap(x -> convertRelationToToscaRelation(x).stream()).filter(i -> i.getUniqueId() != null)
                .collect(Collectors.toMap(RelationshipInstDataDefinition::getUniqueId, Function.identity()));
            compositionDataDefinition.setRelations(relations);
        }
    }

    private static void convertInputs(Component component, TopologyTemplate topologyTemplate) {
        List<InputDefinition> inputsList = component.getInputs();
        if (inputsList != null && !inputsList.isEmpty()) {
            Map<String, PropertyDataDefinition> inputsMap = inputsList.stream().map(PropertyDataDefinition::new)
                .collect(Collectors.toMap(PropertyDataDefinition::getName, Function.identity()));
            topologyTemplate.setInputs(inputsMap);
        }
    }

    private static void convertInputs(TopologyTemplate topologyTemplate, Component component) {
        Map<String, PropertyDataDefinition> inputsMap = topologyTemplate.getInputs();
        if (inputsMap != null && !inputsMap.isEmpty()) {
            List<InputDefinition> inputsList = inputsMap.values().stream().map(InputDefinition::new).collect(Collectors.toList());
            component.setInputs(inputsList);
        }
    }

    private static void convertOutputs(final TopologyTemplate topologyTemplate, final Component component) {
        final Map<String, AttributeDataDefinition> outputsMap = topologyTemplate.getOutputs();
        if (MapUtils.isEmpty(outputsMap)) {
            return;
        }
        final List<OutputDefinition> outputList = outputsMap.values().stream().map(OutputDefinition::new).collect(Collectors.toList());
        component.setOutputs(outputList);
    }

    private static void convertProperties(Component component, TopologyTemplate topologyTemplate) {
        List<PropertyDefinition> propertiesList = component.getProperties();
        if (propertiesList != null && !propertiesList.isEmpty()) {
            Map<String, PropertyDataDefinition> propertiesMap = propertiesList.stream().map(i -> new PropertyDataDefinition(i))
                .collect(Collectors.toMap(i -> i.getName(), i -> i));
            topologyTemplate.setProperties(propertiesMap);
        }
    }

    private static void convertAttributesToTopologyTemplate(final Component component, final TopologyTemplate topologyTemplate) {
        final List<AttributeDefinition> attributes = component.getAttributes();
        if (CollectionUtils.isNotEmpty(attributes)) {
            final Map<String, AttributeDataDefinition> attributeDataDefinitionMap = attributes.stream().map(AttributeDataDefinition::new)
                .collect(Collectors.toMap(AttributeDataDefinition::getName, i -> i));
            topologyTemplate.setAttributes(attributeDataDefinitionMap);
        }
    }

    private static void convertProperties(TopologyTemplate topologyTemplate, Component component) {
        Map<String, PropertyDataDefinition> propertiesMap = topologyTemplate.getProperties();
        if (propertiesMap != null && !propertiesMap.isEmpty()) {
            Map<String, PropertyDefinition> copy = propertiesMap.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> new PropertyDefinition(entry.getValue())));
            component.setProperties(new ArrayList<>(copy.values()));
        }
    }

    private static void convertAttributesToComponent(final TopologyTemplate topologyTemplate, final Component component) {
        final Map<String, AttributeDataDefinition> attributes = topologyTemplate.getAttributes();
        if (MapUtils.isNotEmpty(attributes)) {
            Map<String, AttributeDefinition> copy = attributes.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> new AttributeDefinition(entry.getValue())));
            component.setAttributes(new ArrayList<>(copy.values()));
        }
    }

    private static void convertDataTypes(final ToscaElement toscaElement, final Component component) {
        final Map<String, DataTypeDataDefinition> dataTypeDataMap = toscaElement.getDataTypes();
        if (MapUtils.isNotEmpty(dataTypeDataMap)) {
            final List<DataTypeDefinition> dataTypeDefinitionList = dataTypeDataMap.values().stream().map(dataTypeDataDefinition -> {
                final DataTypeDefinition dataTypeDefinition = new DataTypeDefinition(dataTypeDataDefinition);
                if (CollectionUtils.isNotEmpty(dataTypeDataDefinition.getPropertiesData())) {
                    log.debug("#convertDataTypes - propertiesData is not null. {}",
                        ReflectionToStringBuilder.toString(dataTypeDataDefinition.getPropertiesData()));
                    dataTypeDefinition
                        .setProperties(dataTypeDataDefinition.getPropertiesData().stream().map(PropertyDefinition::new).collect(Collectors.toList()));
                } else {
                    log.debug("#convertDataTypes - propertiesData is null. ignore.");
                }
                return dataTypeDefinition;
            }).collect(Collectors.toList());
            component.setDataTypes(dataTypeDefinitionList);
        }
    }

    private static void convertCommonToscaData(Component component, ToscaElement toscaElement) {
        toscaElement.setUUID(component.getUUID());
        toscaElement.setUniqueId(component.getUniqueId());
        toscaElement.setSystemName(component.getSystemName());
        toscaElement.setLifecycleState(component.getLifecycleState());
        toscaElement.setComponentType(component.getComponentType());
        toscaElement.setNormalizedName(component.getNormalizedName());
        toscaElement.setMetadataValue(JsonPresentationFields.NAME, component.getName());
        toscaElement.setCategories(component.getCategories());
        toscaElement.setCreatorUserId(component.getCreatorUserId());
        toscaElement.setCreationDate(component.getCreationDate());
        toscaElement.setCreatorFullName(component.getCreatorFullName());
        toscaElement.setHighestVersion(component.isHighestVersion());
        toscaElement.setLastUpdateDate(component.getLastUpdateDate());
        toscaElement.setLastUpdaterFullName(component.getLastUpdaterFullName());
        toscaElement.setLastUpdaterUserId(component.getLastUpdaterUserId());
        toscaElement.setDerivedFromGenericType(component.getDerivedFromGenericType());
        toscaElement.setDerivedFromGenericVersion(component.getDerivedFromGenericVersion());
        //Archive
        toscaElement.setArchived(component.isArchived() == null ? false : component.isArchived());
        toscaElement.setArchiveTime(component.getArchiveTime() == null ? 0L : component.getArchiveTime());
        toscaElement.setVspArchived(component.isVspArchived() == null ? false : component.isVspArchived());
        toscaElement.setLifecycleState(component.getLifecycleState());
        toscaElement.setMetadataValue(JsonPresentationFields.VERSION, component.getVersion());
        if (component.getComponentType() == ComponentTypeEnum.RESOURCE) {
            toscaElement.setMetadataValue(JsonPresentationFields.IS_ABSTRACT, ((Resource) component).isAbstract());
            toscaElement.setMetadataValue(JsonPresentationFields.TOSCA_RESOURCE_NAME, ((Resource) component).getToscaResourceName());
            toscaElement.setMetadataValue(JsonPresentationFields.VENDOR_NAME, ((Resource) component).getVendorName());
            toscaElement.setMetadataValue(JsonPresentationFields.VENDOR_RELEASE, ((Resource) component).getVendorRelease());
            toscaElement.setMetadataValue(JsonPresentationFields.MODEL, component.getModel());
            // field isn't mandatory , but shouldn't be null(should be an empty string instead)
            if (((Resource) component).getResourceVendorModelNumber() != null) {
                toscaElement
                    .setMetadataValue(JsonPresentationFields.RESOURCE_VENDOR_MODEL_NUMBER, ((Resource) component).getResourceVendorModelNumber());
            } else {
                toscaElement.setMetadataValue(JsonPresentationFields.RESOURCE_VENDOR_MODEL_NUMBER, "");
            }
            toscaElement.setMetadataValue(JsonPresentationFields.NORMATIVE, ((Resource) component).getComponentMetadataDefinition().getMetadataDataDefinition().isNormative());
        } else if (component.getComponentType() == ComponentTypeEnum.SERVICE) {
            // field isn't mandatory , but shouldn't be null(should be an empty string instead)
            if (((Service) component).getServiceType() != null) {
                toscaElement.setMetadataValue(JsonPresentationFields.SERVICE_TYPE, ((Service) component).getServiceType());
            } else {
                toscaElement.setMetadataValue(JsonPresentationFields.SERVICE_TYPE, "");
            }
            if (((Service) component).getServiceRole() != null) {
                toscaElement.setMetadataValue(JsonPresentationFields.SERVICE_ROLE, ((Service) component).getServiceRole());
            } else {
                toscaElement.setMetadataValue(JsonPresentationFields.SERVICE_ROLE, "");
            }
            if (((Service) component).getServiceFunction() != null) {
                toscaElement.setMetadataValue(JsonPresentationFields.SERVICE_FUNCTION, ((Service) component).getServiceFunction());
            } else {
                toscaElement.setMetadataValue(JsonPresentationFields.SERVICE_FUNCTION, "");
            }
        }
        toscaElement.setMetadataValue(JsonPresentationFields.CONFORMANCE_LEVEL, component.getConformanceLevel());
        toscaElement.setMetadataValue(JsonPresentationFields.IS_DELETED, component.getIsDeleted());
        toscaElement.setMetadataValue(JsonPresentationFields.ICON, component.getIcon());
        toscaElement.setMetadataValue(JsonPresentationFields.DESCRIPTION, component.getDescription());
        toscaElement.setMetadataValue(JsonPresentationFields.TAGS, component.getTags());
        toscaElement.setMetadataValue(JsonPresentationFields.INVARIANT_UUID, component.getInvariantUUID());
        toscaElement.setMetadataValue(JsonPresentationFields.CONTACT_ID, component.getContactId());
        toscaElement.setMetadataValue(JsonPresentationFields.MODEL, component.getModel());
        toscaElement.setModel(component.getModel());
        for (final String key : component.getCategorySpecificMetadata().keySet()) {
            toscaElement.setMetadataValue(key, component.getCategorySpecificMetadata().get(key));
        }
        final List<DataTypeDefinition> dataTypes = component.getDataTypes();
        if (CollectionUtils.isNotEmpty(dataTypes)) {
            toscaElement
                .setDataTypes(dataTypes.stream().collect(Collectors.toMap(DataTypeDefinition::getName, dataTypeDefinition -> dataTypeDefinition)));
        }
    }

    private static void setComponentInstancesToComponent(TopologyTemplate topologyTemplate, Component component) {
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance currComponentInstance;
        Map<String, CINodeFilterDataDefinition> nodeFilterComponents = topologyTemplate.getNodeFilterComponents();
        for (Map.Entry<String, ComponentInstanceDataDefinition> entry : topologyTemplate.getComponentInstances().entrySet()) {
            String key = entry.getKey();
            currComponentInstance = new ComponentInstance(topologyTemplate.getComponentInstances().get(key));
            if (topologyTemplate.getInstGroups() != null && topologyTemplate.getInstGroups().containsKey(key)) {
                List<GroupInstance> groupInstances = topologyTemplate.getInstGroups().get(key).getMapToscaDataDefinition().entrySet().stream()
                    .map(e -> new GroupInstance(e.getValue())).collect(Collectors.toList());
                currComponentInstance.setGroupInstances(groupInstances);
            }
            setComponentInstanceSource(currComponentInstance, component);
            if (MapUtils.isNotEmpty(nodeFilterComponents) && nodeFilterComponents.containsKey(key)) {
                currComponentInstance.setNodeFilter(nodeFilterComponents.get(key));
            }
            if (topologyTemplate.getInstProperties() != null && topologyTemplate.getInstProperties().containsKey(key)
                && topologyTemplate.getInstProperties().get(key) != null) {
                List<PropertyDefinition> instanceProps = topologyTemplate.getInstProperties().get(key).getMapToscaDataDefinition().entrySet().stream()
                    .map(e -> new PropertyDefinition(e.getValue())).collect(Collectors.toList());
                currComponentInstance.setProperties(instanceProps);
            }
            if (topologyTemplate.getInstInputs() != null && topologyTemplate.getInstInputs().containsKey(key)
                && topologyTemplate.getInstInputs().get(key) != null) {
                List<InputDefinition> instanceInputs = topologyTemplate.getInstInputs().get(key).getMapToscaDataDefinition().entrySet().stream()
                    .map(e -> new InputDefinition(e.getValue())).collect(Collectors.toList());
                currComponentInstance.setInputs(instanceInputs);
            }
            final Map<String, MapAttributesDataDefinition> instAttributes = topologyTemplate.getInstAttributes();
            if (instAttributes != null && instAttributes.containsKey(key) && instAttributes.get(key) != null) {
                currComponentInstance.setAttributes(
                    instAttributes.get(key).getMapToscaDataDefinition().values().stream().map(AttributeDefinition::new).collect(Collectors.toList()));
            }
            if (topologyTemplate.getComponentInstInterfaces() != null && topologyTemplate.getComponentInstInterfaces().containsKey(key)
                && topologyTemplate.getComponentInstInterfaces().get(key) != null) {
                Map<String, Object> interfacesMap = topologyTemplate.getComponentInstInterfaces().get(key).getMapToscaDataDefinition().entrySet()
                    .stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                currComponentInstance.setInterfaces(interfacesMap);
            }
            componentInstances.add(currComponentInstance);
        }
        component.setComponentInstances(componentInstances);
    }

    public static List<ComponentInstance> getComponentInstancesFromMapObject(
        Map<String, ComponentInstanceDataDefinition> componentInstancesDefinitionMap, Component component) {
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance currComponentInstance;
        for (Map.Entry<String, ComponentInstanceDataDefinition> entry : componentInstancesDefinitionMap.entrySet()) {
            String key = entry.getKey();
            currComponentInstance = new ComponentInstance(componentInstancesDefinitionMap.get(key));
            setComponentInstanceSource(currComponentInstance, component);
            componentInstances.add(currComponentInstance);
        }
        return componentInstances;
    }

    private static void setComponentInstanceSource(ComponentInstance currComponentInstance, Component component) {
        if (Objects.isNull(currComponentInstance.getCreatedFrom())) {
            if (ComponentTypeEnum.RESOURCE.equals(component.getComponentType())) {
                Resource resource = (Resource) component;
                if (isFromCsar(resource)) {
                    currComponentInstance.setCreatedFrom(CreatedFrom.CSAR);
                } else {
                    currComponentInstance.setCreatedFrom(CreatedFrom.UI);
                }
            }
        }
    }

    private static boolean isFromCsar(Resource resource) {
        return resource.getResourceType() == ResourceTypeEnum.VF || resource.getResourceType() == ResourceTypeEnum.CVFC
            || resource.getResourceType() == ResourceTypeEnum.PNF;
    }

    private static void setComponentInstancesInputsToComponent(TopologyTemplate topologyTemplate, Component component) {
        if (topologyTemplate.getInstInputs() != null) {
            Map<String, List<ComponentInstanceInput>> inputs = new HashMap<>();
            for (Entry<String, MapPropertiesDataDefinition> entry : topologyTemplate.getInstInputs().entrySet()) {
                if (entry.getValue() != null && entry.getValue().getMapToscaDataDefinition() != null) {
                    String key = entry.getKey();
                    List<ComponentInstanceInput> componentInstanceAttributes = entry.getValue().getMapToscaDataDefinition().entrySet().stream()
                        .map(e -> new ComponentInstanceInput(e.getValue())).collect(Collectors.toList());
                    inputs.put(key, componentInstanceAttributes);
                }
            }
            component.setComponentInstancesInputs(inputs);
        }
    }

    private static void setComponentInstancesOutputsToComponent(final TopologyTemplate topologyTemplate, final Component component) {
        if (topologyTemplate.getInstOutputs() != null) {
            final Map<String, List<ComponentInstanceOutput>> outputs = new HashMap<>();
            for (final Entry<String, MapAttributesDataDefinition> entry : topologyTemplate.getInstOutputs().entrySet()) {
                if (entry.getValue() != null && entry.getValue().getMapToscaDataDefinition() != null) {
                    final String key = entry.getKey();
                    final List<ComponentInstanceOutput> componentInstanceAttributes = entry.getValue().getMapToscaDataDefinition().entrySet().stream()
                        .map(e -> new ComponentInstanceOutput(e.getValue())).collect(Collectors.toList());
                    outputs.put(key, componentInstanceAttributes);
                }
            }
            component.setComponentInstancesOutputs(outputs);
        }
    }

    private static void setComponentInstancesPropertiesToComponent(TopologyTemplate topologyTemplate, Component component) {
        if (topologyTemplate.getInstProperties() != null) {
            Map<String, List<ComponentInstanceProperty>> properties = new HashMap<>();
            for (Entry<String, MapPropertiesDataDefinition> entry : topologyTemplate.getInstProperties().entrySet()) {
                if (entry.getValue() != null && entry.getValue().getMapToscaDataDefinition() != null) {
                    String key = entry.getKey();
                    List<ComponentInstanceProperty> componentInstanceAttributes = entry.getValue().getMapToscaDataDefinition().entrySet().stream()
                        .map(e -> new ComponentInstanceProperty(new PropertyDefinition(e.getValue()))).collect(Collectors.toList());
                    properties.put(key, componentInstanceAttributes);
                }
            }
            component.setComponentInstancesProperties(properties);
        }
    }

    public static Map<String, List<ComponentInstanceAttribute>> getComponentInstancesAttributes(
        Map<String, MapAttributesDataDefinition> mapAttributesDataDefinitionMap) {
        Map<String, List<ComponentInstanceAttribute>> attributes = new HashMap<>();
        for (Map.Entry<String, MapAttributesDataDefinition> entry : mapAttributesDataDefinitionMap.entrySet()) {
            if (entry.getValue() != null && entry.getValue().getMapToscaDataDefinition() != null) {
                String key = entry.getKey();
                List<ComponentInstanceAttribute> componentInstanceAttributes = entry.getValue().getMapToscaDataDefinition().entrySet().stream()
                    .map(e -> new ComponentInstanceAttribute(new AttributeDefinition(e.getValue()))).collect(Collectors.toList());
                attributes.put(key, componentInstanceAttributes);
            }
        }
        return attributes;
    }

    private static void setComponentInstancesInterfacesToComponent(TopologyTemplate topologyTemplate, Component component) {
        if (topologyTemplate.getInstInterfaces() != null) {
            Map<String, List<ComponentInstanceInterface>> interfaces = new HashMap<>();
            for (Map.Entry<String, MapInterfaceInstanceDataDefinition> entry : topologyTemplate.getInstInterfaces().entrySet()) {
                if (entry.getValue() != null && entry.getValue().getMapToscaDataDefinition() != null) {
                    String key = entry.getKey();
                    List<ComponentInstanceInterface> componentInstanceInterfaces = entry.getValue().getMapToscaDataDefinition().entrySet().stream()
                        .map(e -> new ComponentInstanceInterface(e.getKey(), e.getValue())).collect(Collectors.toList());
                    interfaces.put(key, componentInstanceInterfaces);
                }
            }
            component.setComponentInstancesInterfaces(interfaces);
        } else if (topologyTemplate.getComponentInstInterfaces() != null) {
            Map<String, List<ComponentInstanceInterface>> interfaces = new HashMap<>();
            for (Map.Entry<String, MapInterfaceDataDefinition> entry : topologyTemplate.getComponentInstInterfaces().entrySet()) {
                if (entry.getValue() != null && entry.getValue().getMapToscaDataDefinition() != null) {
                    String key = entry.getKey();
                    List<ComponentInstanceInterface> componentInstanceInterfaces = entry.getValue().getMapToscaDataDefinition().entrySet().stream()
                        .map(e -> new ComponentInstanceInterface(e.getKey(), e.getValue())).collect(Collectors.toList());
                    interfaces.put(key, componentInstanceInterfaces);
                }
            }
            component.setComponentInstancesInterfaces(interfaces);
        }
    }

    private static void setComponentInstancesAttributesToComponent(TopologyTemplate topologyTemplate, Component component) {
        if (topologyTemplate.getInstAttributes() != null) {
            Map<String, List<ComponentInstanceAttribute>> attributes = new HashMap<>();
            for (Map.Entry<String, MapAttributesDataDefinition> entry : topologyTemplate.getInstAttributes().entrySet()) {
                if (entry.getValue() != null && entry.getValue().getMapToscaDataDefinition() != null) {
                    String key = entry.getKey();
                    List<ComponentInstanceAttribute> componentInstanceAttributes = entry.getValue().getMapToscaDataDefinition().entrySet().stream()
                        .map(e -> new ComponentInstanceAttribute(new AttributeDefinition(e.getValue()))).collect(Collectors.toList());
                    attributes.put(key, componentInstanceAttributes);
                }
            }
            component.setComponentInstancesAttributes(attributes);
        }
    }

    public static void setComponentInstancesRequirementsToComponent(TopologyTemplate topologyTemplate, Component component) {
        if (topologyTemplate.getCalculatedRequirements() != null) {
            // Requirements of component organized by capability
            Map<String, List<RequirementDefinition>> instanceRequiermentsFromMapObject = getInstanceRequirementsFromMapObject(
                topologyTemplate.getCalculatedRequirements(), component);
            component.setRequirements(instanceRequiermentsFromMapObject);
        }
    }

    public static Map<String, List<RequirementDefinition>> getInstanceRequirementsFromMapObject(
        Map<String, MapListRequirementDataDefinition> mapListRequirements, Component component) {
        // Requirements of component organized by capability
        Map<String, List<RequirementDefinition>> instancesRequirements = new HashMap<>();
        if (mapListRequirements != null) {
            Map<String, ComponentInstance> instancesMap = new HashMap<>();
            for (ComponentInstance currInstance : component.getComponentInstances()) {
                instancesMap.put(currInstance.getUniqueId(), currInstance);
            }
            for (Map.Entry<String, MapListRequirementDataDefinition> entry : mapListRequirements.entrySet()) {
                String instanceId = entry.getKey();
                // Requirements of instance organized by capability
                Map<String, ListRequirementDataDefinition> capsMapList = entry.getValue().getMapToscaDataDefinition();
                if (capsMapList != null) {
                    for (Entry<String, ListRequirementDataDefinition> entryTypeList : capsMapList.entrySet()) {
                        String capabilityType = entryTypeList.getKey();
                        List<RequirementDefinition> caps = entryTypeList.getValue().getListToscaDataDefinition().stream()
                            .map(RequirementDefinition::new).collect(Collectors.toList());
                        if (instancesRequirements.containsKey(capabilityType)) {
                            instancesRequirements.get(capabilityType).addAll(caps);
                        } else {
                            instancesRequirements.put(capabilityType, caps);
                        }
                        if (MapUtils.isEmpty(instancesMap.get(instanceId).getRequirements())) {
                            instancesMap.get(instanceId).setRequirements(new HashMap<>());
                        }
                        instancesMap.get(instanceId).getRequirements().put(capabilityType, new ArrayList<>(caps));
                    }
                }
            }
            component.setRequirements(instancesRequirements);
        }
        return instancesRequirements;
    }

    public static void setComponentInstancesCapabilitiesToComponentAndCI(TopologyTemplate topologyTemplate, Component component) {
        Map<String, MapCapabilityProperty> calculatedCapProperties = topologyTemplate.getCalculatedCapabilitiesProperties();
        if (topologyTemplate.getCalculatedCapabilities() != null) {
            // capabilities of component organized by type
            Map<String, List<CapabilityDefinition>> instancesCapabilities = new HashMap<>();
            Map<String, ComponentInstance> instancesMap = new HashMap<>();
            for (ComponentInstance currInstance : component.getComponentInstances()) {
                instancesMap.put(currInstance.getUniqueId(), currInstance);
            }
            for (Map.Entry<String, MapListCapabilityDataDefinition> entry : topologyTemplate.getCalculatedCapabilities().entrySet()) {
                String instanceId = entry.getKey();
                // capabilities of instance organized by type
                Map<String, ListCapabilityDataDefinition> capsMapList = entry.getValue().getMapToscaDataDefinition();
                if (capsMapList != null) {
                    for (Entry<String, ListCapabilityDataDefinition> entryTypeList : capsMapList.entrySet()) {
                        String capabilityType = entryTypeList.getKey();
                        List<CapabilityDefinition> caps = entryTypeList.getValue().getListToscaDataDefinition().stream()
                            .map(cap -> mergeInstCapabiltyWithProperty(cap, instanceId, calculatedCapProperties)).collect(Collectors.toList());
                        if (instancesCapabilities.containsKey(capabilityType)) {
                            instancesCapabilities.get(capabilityType).addAll(caps);
                        } else {
                            instancesCapabilities.put(capabilityType, caps);
                        }
                        ComponentInstance instance = instancesMap.get(instanceId);
                        if (instance == null) {
                            log.error("instance is null for id {} entry {}", instanceId,
                                entry.getValue().getToscaPresentationValue(JsonPresentationFields.NAME));
                        } else {
                            if (MapUtils.isEmpty(instance.getCapabilities())) {
                                instance.setCapabilities(new HashMap<>());
                            }
                            instance.getCapabilities().put(capabilityType, new ArrayList<>(caps));
                        }
                    }
                }
            }
            component.setCapabilities(instancesCapabilities);
        }
    }

    private static void setCapabilitiesToComponent(TopologyTemplate topologyTemplate, Component component) {
        Map<String, ListCapabilityDataDefinition> capabilities = topologyTemplate.getCapabilities();
        Map<String, MapPropertiesDataDefinition> capabilitiesProperties = topologyTemplate.getCapabilitiesProperties();
        Map<String, List<CapabilityDefinition>> allCapabilities = new HashMap<>();
        if (MapUtils.isNotEmpty(capabilities)) {
            allCapabilities.putAll(groupCapabilityByType(capabilities));
        }
        if (MapUtils.isNotEmpty(capabilitiesProperties)) {
            capabilitiesProperties.forEach((s, capProp) -> {
                String[] result = s.split(CAP_PROP_DELIM);
                if (capProp != null) {
                    Map<String, PropertyDataDefinition> capMap = capProp.getMapToscaDataDefinition();
                    if (MapUtils.isNotEmpty(capMap)) {
                        List<ComponentInstanceProperty> capPropsList = capMap.values().stream().map(ComponentInstanceProperty::new)
                            .collect(Collectors.toList());
                        List<CapabilityDefinition> cap = allCapabilities.get(result[0]);
                        if (cap != null) {
                            Optional<CapabilityDefinition> op = cap.stream().filter(c -> c.getName().equals(result[1])).findFirst();
                            op.ifPresent(capabilityDefinition -> capabilityDefinition.setProperties(capPropsList));
                        }
                    }
                }
            });
        }
        Map<String, List<CapabilityDefinition>> componentCapabilities = component.getCapabilities();
        if (MapUtils.isNotEmpty(componentCapabilities)) {
            mergeCapabilityMap(allCapabilities, componentCapabilities);
        }
        component.setCapabilities(allCapabilities);
    }

    private static void mergeCapabilityMap(Map<String, List<CapabilityDefinition>> map1, Map<String, List<CapabilityDefinition>> map2) {
        map1.forEach((key1, val1) -> map2.forEach((key2, val2) -> {
            if (key1.equals(key2)) {
                val2.addAll(val1);
            }
        }));
        map1.putAll(map2);
    }

    private static Map<String, List<CapabilityDefinition>> groupCapabilityByType(Map<String, ListCapabilityDataDefinition> capabilities) {
        Map<String, List<CapabilityDefinition>> groupedCapabilities = new HashMap<>();
        Set<String> typesSet = new HashSet<>();
        List<CapabilityDefinition> allCapabilityDefinitions = new ArrayList<>();
        for (Entry<String, ListCapabilityDataDefinition> capabilitiesEntry : capabilities.entrySet()) {
            typesSet.addAll(capabilitiesEntry.getValue().getListToscaDataDefinition().stream().map(CapabilityDataDefinition::getType)
                .collect(Collectors.toSet()));
            allCapabilityDefinitions.addAll(
                capabilitiesEntry.getValue().getListToscaDataDefinition().stream().map(CapabilityDefinition::new).collect(Collectors.toList()));
        }
        for (String capType : typesSet) {
            groupedCapabilities.put(capType,
                allCapabilityDefinitions.stream().filter(capabilityDefinition -> capabilityDefinition.getType().equals(capType))
                    .collect(Collectors.toList()));
        }
        return groupedCapabilities;
    }

    private static void setRequirementsToComponent(TopologyTemplate topologyTemplate, Component component) {
        Map<String, ListRequirementDataDefinition> requirements = topologyTemplate.getRequirements();
        Map<String, List<RequirementDefinition>> componentRequirements = component.getRequirements();
        if (MapUtils.isNotEmpty(requirements)) {
            if (componentRequirements == null) {
                componentRequirements = new HashMap<>();
            }
            componentRequirements.putAll(groupRequirementByType(requirements));
            component.setRequirements(componentRequirements);
        }
    }

    private static Map<String, List<RequirementDefinition>> groupRequirementByType(Map<String, ListRequirementDataDefinition> requirements) {
        Map<String, List<RequirementDefinition>> groupedRequirement = new HashMap<>();
        Set<String> typesSet = new HashSet<>();
        List<RequirementDefinition> allRequirements = new ArrayList<>();
        for (Entry<String, ListRequirementDataDefinition> requirementsEntry : requirements.entrySet()) {
            typesSet.addAll(requirementsEntry.getValue().getListToscaDataDefinition().stream().map(RequirementDataDefinition::getCapability)
                .collect(Collectors.toSet()));
            allRequirements.addAll(
                requirementsEntry.getValue().getListToscaDataDefinition().stream().map(RequirementDefinition::new).collect(Collectors.toList()));
        }
        for (String capType : typesSet) {
            groupedRequirement.put(capType,
                allRequirements.stream().filter(requirementDefinition -> requirementDefinition.getCapability().equals(capType))
                    .collect(Collectors.toList()));
        }
        return groupedRequirement;
    }

    private static void setCapabilitiesToComponentAndGroups(TopologyTemplate topologyTemplate, Component component) {
        Map<String, MapCapabilityProperty> calculatedCapProperties = topologyTemplate.getCalculatedCapabilitiesProperties();
        if (capabilitiesAndGroupsExist(topologyTemplate, component)) {
            Map<String, GroupDefinition> groupsMap = component.getGroups().stream()
                .collect(Collectors.toMap(GroupDefinition::getUniqueId, Function.identity()));
            for (Map.Entry<String, MapListCapabilityDataDefinition> entry : topologyTemplate.getCalculatedCapabilities().entrySet()) {
                findSetCapabilitiesToComponentAndGroup(calculatedCapProperties, component, groupsMap, entry);
            }
        }
    }

    private static boolean capabilitiesAndGroupsExist(TopologyTemplate topologyTemplate, Component component) {
        return MapUtils.isNotEmpty(topologyTemplate.getCalculatedCapabilities()) && CollectionUtils.isNotEmpty(component.getGroups());
    }

    private static void findSetCapabilitiesToComponentAndGroup(Map<String, MapCapabilityProperty> calculatedCapProperties, Component component,
                                                               Map<String, GroupDefinition> groupsMap,
                                                               Map.Entry<String, MapListCapabilityDataDefinition> entry) {
        String uniqueId = entry.getKey();
        if (groupsMap.containsKey(uniqueId)) {
            setCapabilitiesToComponentAndGroup(calculatedCapProperties, component, entry, groupsMap.get(uniqueId));
        } else {
            log.warn("The group with uniqueId {} was not found", uniqueId);
        }
    }

    private static void setCapabilitiesToComponentAndGroup(Map<String, MapCapabilityProperty> calculatedCapProperties, Component component,
                                                           Map.Entry<String, MapListCapabilityDataDefinition> entry, GroupDefinition group) {
        for (Entry<String, ListCapabilityDataDefinition> entryTypeList : entry.getValue().getMapToscaDataDefinition().entrySet()) {
            String capabilityType = entryTypeList.getKey();
            List<CapabilityDefinition> caps = entryTypeList.getValue().getListToscaDataDefinition().stream()
                .map(cap -> mergeInstCapabiltyWithProperty(cap, group.getUniqueId(), calculatedCapProperties)).collect(Collectors.toList());
            if (component.getCapabilities().containsKey(capabilityType)) {
                component.getCapabilities().get(capabilityType).addAll(caps);
            } else {
                component.getCapabilities().put(capabilityType, caps);
            }
            group.getCapabilities().put(capabilityType, Lists.newArrayList(caps));
        }
    }

    private static CapabilityDefinition mergeInstCapabiltyWithProperty(CapabilityDataDefinition cap, String ownerId,
                                                                       Map<String, MapCapabilityProperty> calculatedCapProperties) {
        CapabilityDefinition capability = new CapabilityDefinition(cap);
        if (calculatedCapProperties != null) {
            MapCapabilityProperty mapOfMapPropertiesDataDefinition = calculatedCapProperties.get(ownerId);
            if (mapOfMapPropertiesDataDefinition != null && mapOfMapPropertiesDataDefinition.getMapToscaDataDefinition() != null) {
                Map<String, MapPropertiesDataDefinition> toscaCapPropMap = mapOfMapPropertiesDataDefinition.getMapToscaDataDefinition();
                toscaCapPropMap.forEach((keyPath, capProp) -> findConvertSetProperties(cap, ownerId, capability, keyPath, capProp));
            }
        }
        return capability;
    }

    private static void findConvertSetProperties(CapabilityDataDefinition cap, String primaryPathKey, CapabilityDefinition capability, String path,
                                                 MapPropertiesDataDefinition capProp) {
        // format key of capability properties :

        // VF instance in service : instanceId#ownerId#type#capName

        // VFC instance in VF : instanceId#type#capName -> instanceId=ownerId

        // Group in service : groupName#ownerId#type#capName

        // Group in VF : groupName#type#capName -> groupName=ownerId
        String[] result = path.split(CAP_PROP_DELIM);
        if (result.length < 4) {
            log.debug("wrong key format for capabilty, key {}", capProp);
            return;
        }
        if (relatedPropertiesExist(cap, primaryPathKey, capProp, result)) {
            capability.setProperties(
                capProp.getMapToscaDataDefinition().values().stream().map(ComponentInstanceProperty::new).collect(Collectors.toList()));
        }
    }

    private static boolean relatedPropertiesExist(CapabilityDataDefinition cap, String primaryPathKey, MapPropertiesDataDefinition capProp,
                                                  String[] result) {
        return capProp != null && MapUtils.isNotEmpty(capProp.getMapToscaDataDefinition()) && areRelatedProperties(cap, primaryPathKey, result);
    }

    private static boolean areRelatedProperties(CapabilityDataDefinition cap, String primaryPathKey, String[] result) {
        int primaryKeyIndex = 0;
        int ownerIndex = 1;
        int typeIndex = result.length - 2;
        int nameIndex = result.length - 1;
        return result[typeIndex].equals(cap.getType()) && result[nameIndex].equals(cap.getName()) && cap.getOwnerId().equals(result[ownerIndex])
            && primaryPathKey.equals(result[primaryKeyIndex]);
    }

    private static void setComponentInstancesToTopologyTemplate(Component component, TopologyTemplate topologyTemplate) {
        Map<String, ComponentInstanceDataDefinition> componentInstances = new HashMap<>();
        ComponentInstanceDataDefinition convertedInstance;
        if (component.getComponentInstances() != null) {
            for (ComponentInstance instance : component.getComponentInstances()) {
                convertedInstance = new ComponentInstanceDataDefinition(instance);
                if (instance.getGroupInstances() != null) {
                    MapGroupsDataDefinition groupsMap = new MapGroupsDataDefinition();
                    groupsMap.setMapToscaDataDefinition(instance.getGroupInstances().stream().map(GroupInstanceDataDefinition::new)
                        .collect(Collectors.toMap(GroupInstanceDataDefinition::getName, Function.identity())));
                    if (topologyTemplate.getInstGroups() == null) {
                        topologyTemplate.setInstGroups(new HashMap<>());
                    }
                    topologyTemplate.getInstGroups().put(instance.getUniqueId(), groupsMap);
                }
                componentInstances.put(instance.getUniqueId(), convertedInstance);
            }
        }
        topologyTemplate.setComponentInstances(componentInstances);
    }

    private static void setComponentInstancesInputsToTopologyTemplate(Component component, TopologyTemplate topologyTemplate) {
        if (component.getComponentInstancesInputs() != null) {
            topologyTemplate.setInstInputs(new HashMap<>());
            MapPropertiesDataDefinition inputsMap;
            for (Entry<String, List<ComponentInstanceInput>> entry : component.getComponentInstancesInputs().entrySet()) {
                inputsMap = new MapPropertiesDataDefinition();
                inputsMap.setMapToscaDataDefinition(entry.getValue().stream().map(PropertyDataDefinition::new)
                    .collect(Collectors.toMap(PropertyDataDefinition::getName, Function.identity())));
                topologyTemplate.getInstInputs().put(entry.getKey(), inputsMap);
            }
        }
    }

    private static void setComponentInstancesPropertiesToTopologyTemplate(Component component, TopologyTemplate topologyTemplate) {
        if (component.getComponentInstancesProperties() != null) {
            topologyTemplate.setInstProperties(new HashMap<>());
            MapPropertiesDataDefinition propertiesMap;
            for (Entry<String, List<ComponentInstanceProperty>> entry : component.getComponentInstancesProperties().entrySet()) {
                propertiesMap = new MapPropertiesDataDefinition();
                propertiesMap.setMapToscaDataDefinition(entry.getValue().stream().map(PropertyDataDefinition::new)
                    .collect(Collectors.toMap(PropertyDataDefinition::getName, Function.identity())));
                topologyTemplate.getInstProperties().put(entry.getKey(), propertiesMap);
            }
        }
    }

    private static void setComponentInstancesArtifactsToTopologyTemplate(Component component, TopologyTemplate topologyTemplate) {
        List<ComponentInstance> componentInstances = component.getComponentInstances();
        if (componentInstances != null) {
            topologyTemplate.setInstanceArtifacts(new HashMap<>());
            topologyTemplate.setInstDeploymentArtifacts(new HashMap<>());
            for (ComponentInstance ci : componentInstances) {
                Map<String, ArtifactDefinition> artifacts = ci.getArtifacts();
                if (artifacts != null) {
                    Map<String, ArtifactDataDefinition> mapToscaDataDefinitionArtifact = artifacts.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDataDefinition(e.getValue())));
                    MapArtifactDataDefinition insArtifact = new MapArtifactDataDefinition(mapToscaDataDefinitionArtifact);
                    topologyTemplate.getInstanceArtifacts().put(ci.getUniqueId(), insArtifact);
                }
                Map<String, ArtifactDefinition> deplArtifacts = ci.getDeploymentArtifacts();
                if (deplArtifacts != null) {
                    Map<String, ArtifactDataDefinition> mapToscaDataDefinitionDepArtifact = deplArtifacts.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDataDefinition(e.getValue())));
                    MapArtifactDataDefinition insDepArtifact = new MapArtifactDataDefinition(mapToscaDataDefinitionDepArtifact);
                    topologyTemplate.getInstDeploymentArtifacts().put(ci.getUniqueId(), insDepArtifact);
                }
            }
        }
    }

    private static void setComponentInstancesAttributesToTopologyTemplate(Component component, TopologyTemplate topologyTemplate) {
        if (component.getComponentInstancesAttributes() != null) {
            topologyTemplate.setInstAttributes(new HashMap<>());
            for (Entry<String, List<ComponentInstanceAttribute>> entry : component.getComponentInstancesAttributes().entrySet()) {
                final MapAttributesDataDefinition attributesMap = new MapAttributesDataDefinition();
                attributesMap.setMapToscaDataDefinition(entry.getValue().stream().map(AttributeDefinition::new)
                    .collect(Collectors.toMap(AttributeDefinition::getName, Function.identity(), (entity1, entity2) -> entity1)));
                topologyTemplate.getInstAttributes().put(entry.getKey(), attributesMap);
            }
        }
    }

    public static ComponentMetadataData convertToComponentMetadata(GraphVertex vertex) {
        ComponentMetadataData metadata;
        switch (vertex.getType()) {
            case SERVICE:
                metadata = new ServiceMetadataData(new GraphPropertiesDictionaryExtractor(vertex.getMetadataJson()));
                break;
            case RESOURCE:
                metadata = new ResourceMetadataData(new GraphPropertiesDictionaryExtractor(vertex.getMetadataJson()));
                break;
            case PRODUCT:
                metadata = new ProductMetadataData(new GraphPropertiesDictionaryExtractor(vertex.getMetadataJson()));
                break;
            default:
                throw new StorageException(JanusGraphOperationStatus.INVALID_TYPE);
        }
        metadata.getMetadataDataDefinition().setUniqueId(vertex.getUniqueId());
        metadata.getMetadataDataDefinition().setLastUpdateDate((Long) vertex.getJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE));
        metadata.getMetadataDataDefinition().setUUID((String) vertex.getJsonMetadataField(JsonPresentationFields.UUID));
        metadata.getMetadataDataDefinition().setState((String) vertex.getJsonMetadataField(JsonPresentationFields.LIFECYCLE_STATE));
        return metadata;
    }

    public static ComponentMetadataDataDefinition convertToComponentMetadataDataDefinition(GraphVertex vertex) {
        ComponentMetadataDataDefinition metadata;
        switch (vertex.getType()) {
            case SERVICE:
                metadata = new ServiceMetadataDataDefinition(new JsonPresentationFieldsExtractor(vertex.getMetadataJson()));
                break;
            case RESOURCE:
                metadata = new ResourceMetadataDataDefinition(new JsonPresentationFieldsExtractor(vertex.getMetadataJson()));
                break;
            case PRODUCT:
                metadata = new ProductMetadataDataDefinition(new JsonPresentationFieldsExtractor(vertex.getMetadataJson()));
                break;
            default:
                throw new StorageException(JanusGraphOperationStatus.INVALID_TYPE);
        }
        metadata.setUniqueId(vertex.getUniqueId());
        metadata.setLastUpdateDate((Long) vertex.getJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE));
        metadata.setUUID((String) vertex.getJsonMetadataField(JsonPresentationFields.UUID));
        metadata.setState((String) vertex.getJsonMetadataField(JsonPresentationFields.LIFECYCLE_STATE));
        return metadata;
    }

    public static List<GroupDefinition> convertToGroupDefinitions(Map<String, GroupDataDefinition> groups) {
        List<GroupDefinition> groupDefinitions = new ArrayList<>();
        if (MapUtils.isNotEmpty(groups)) {
            groupDefinitions = groups.values().stream().map(GroupDefinition::new).collect(Collectors.toList());
        }
        return groupDefinitions;
    }

    public static Map<String, MapCapabilityProperty> extractCapabilityProperteisFromInstances(List<ComponentInstance> instances, boolean fromCsar) {
        return instances.stream().collect(Collectors.toMap(ComponentInstanceDataDefinition::getUniqueId,
            ci -> convertToMapOfMapCapabiltyProperties(ci.getCapabilities(), ci.getUniqueId(), fromCsar)));
    }

    public static Map<String, MapCapabilityProperty> extractCapabilityPropertiesFromGroups(List<GroupDefinition> groups, boolean fromCsar) {
        if (CollectionUtils.isNotEmpty(groups)) {
            return groups.stream().collect(Collectors
                .toMap(GroupDefinition::getUniqueId, g -> convertToMapOfMapCapabiltyProperties(g.getCapabilities(), g.getUniqueId(), fromCsar)));
        }
        return Maps.newHashMap();
    }

    public static Map<String, MapListCapabilityDataDefinition> extractCapabilitiesFromGroups(final List<GroupDefinition> groupDefinitions) {
        Map<String, MapListCapabilityDataDefinition> calculatedCapabilities = Maps.newHashMap();
        for (GroupDefinition groupDefinition : groupDefinitions) {
            calculatedCapabilities
                .put(groupDefinition.getUniqueId(), new MapListCapabilityDataDefinition(buildMapOfListsOfCapabilities(groupDefinition)));
        }
        return calculatedCapabilities;
    }

    private static Map<String, ListCapabilityDataDefinition> buildMapOfListsOfCapabilities(GroupDefinition groupDefinition) {
        return groupDefinition.getCapabilities().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
            e -> new ListCapabilityDataDefinition(e.getValue().stream().map(CapabilityDataDefinition::new).collect(Collectors.toList()))));
    }
}
