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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.T
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.model.jsontitan.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.*;
import org.openecomp.sdc.be.datatypes.elements.MapCapabilityProperty;
import org.openecomp.sdc.be.datatypes.elements.MapListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsontitan.datamodel.NodeType;
import org.openecomp.sdc.be.model.jsontitan.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.jsontitan.enums.JsonConstantKeysEnum;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.ProductMetadataData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.ServiceMetadataData;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModelConverter {
    public static final String CAP_PROP_DELIM = "#";
    private static final Logger log = Logger.getLogger(ModelConverter.class);

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
        VertexTypeEnum vertexType = null;
        ResourceTypeEnum resourceType = ResourceTypeEnum.getTypeByName(resourceTypeName);
        if (isAtomicComponent(resourceType)) {
            vertexType = VertexTypeEnum.NODE_TYPE;
        } else {
            vertexType = VertexTypeEnum.TOPOLOGY_TEMPLATE;
        }
        return vertexType;
    }

	

	private static Service convertToService(ToscaElement toscaElement) {
		Service service = new Service();
		convertComponentFields(service, toscaElement);

        convertServiceSpecificFields(toscaElement, service);

        TopologyTemplate topologyTemplate = (TopologyTemplate) toscaElement;

        convertComponentInstances(topologyTemplate, service);

        convertInputs(topologyTemplate, service);

        convertGroups(topologyTemplate, service);

		setCapabilitiesToComponentAndGroups(topologyTemplate, service);

        convertPolicies(topologyTemplate, service);

        convertRelations(topologyTemplate, service);

        convertArtifacts(topologyTemplate, service);

        convertServiceApiArtifacts(topologyTemplate, service);

        convertServicePaths(topologyTemplate, service);

        convertServiceInterfaces(topologyTemplate, service);

        return service;
    }

	private static void convertServiceSpecificFields(ToscaElement toscaElement, Service service) {
		service.setProjectCode((String) toscaElement.getMetadataValue(JsonPresentationFields.PROJECT_CODE));
		service.setDistributionStatus(DistributionStatusEnum
				.findState((String) toscaElement.getMetadataValue(JsonPresentationFields.DISTRIBUTION_STATUS)));
		service.setEcompGeneratedNaming(
				(Boolean) toscaElement.getMetadataValueOrDefault(JsonPresentationFields.ECOMP_GENERATED_NAMING, true));
		service.setNamingPolicy((String) toscaElement.getMetadataValueOrDefault(JsonPresentationFields.NAMING_POLICY,
				StringUtils.EMPTY));
		service.setEnvironmentContext(
				(String) toscaElement.getMetadataValue(JsonPresentationFields.ENVIRONMENT_CONTEXT));
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
            resource.setAbstract((Boolean) nodeType.getMetadataValue(JsonPresentationFields.IS_ABSTRACT));
            convertAttributes(nodeType, resource);
            convertCapabilities(nodeType, resource);
            convertRequirements(nodeType, resource);
            convertInterfaces(nodeType, resource);

		} else {
			TopologyTemplate topologyTemplate = (TopologyTemplate) toscaElement;
			if (resource.getResourceType() == ResourceTypeEnum.VF) {
				resource.setCsarUUID((String) topologyTemplate.getMetadataValue(JsonPresentationFields.CSAR_UUID));
				resource.setCsarVersion((String) topologyTemplate.getMetadataValue(JsonPresentationFields.CSAR_VERSION));
				resource.setImportedToscaChecksum((String) topologyTemplate.getMetadataValue(JsonPresentationFields.IMPORTED_TOSCA_CHECKSUM));
				convertInterfaces(topologyTemplate, resource);

            }
            convertComponentInstances(topologyTemplate, resource);
            convertRelations(topologyTemplate, resource);
            convertInputs(topologyTemplate, resource);
            convertGroups(topologyTemplate, resource);
			setCapabilitiesToComponentAndGroups(topologyTemplate, resource);
            convertPolicies(topologyTemplate, resource);
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

    private static void convertAttributes(NodeType nodeType, Resource resource) {
        Map<String, PropertyDataDefinition> attributes = nodeType.getAttributes();
        if (attributes != null) {
            List<PropertyDefinition> attrs = attributes.values().stream().map(dataDef -> ModelConverter.fromDataDefinition(resource.getUniqueId(), dataDef)).collect(Collectors.toList());
            resource.setAttributes(attrs);
        }
    }

    private static PropertyDefinition fromDataDefinition(String resourceId, PropertyDataDefinition dataDefinition) {
        PropertyDefinition attributeDefinition = new PropertyDefinition(dataDefinition);
        attributeDefinition.setParentUniqueId(resourceId);
        return attributeDefinition;
    }

    private static void convertInterfaces(NodeType nodeType, Resource resource) {
        Map<String, InterfaceDataDefinition> interfaceArtifacts = nodeType.getInterfaceArtifacts();
        if (interfaceArtifacts != null) {
            Map<String, InterfaceDefinition> interfaces = interfaceArtifacts.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, en -> new InterfaceDefinition(en.getValue())));
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

            setComponentInstancesToComponent(topologyTemplate, component);

            setComponentInstancesCapabilitiesToComponentAndCI(topologyTemplate, component);

            setComponentInstancesRequirementsToComponent(topologyTemplate, component);

            setComponentInstancesArtifactsToComponent(topologyTemplate, component);

        }
    }

    private static void setComponentInstancesArtifactsToComponent(TopologyTemplate topologyTemplate, Component component) {
        Map<String, MapArtifactDataDefinition> instDeploymentArtifacts = topologyTemplate.getInstDeploymentArtifacts();
        Map<String, MapArtifactDataDefinition> instanceArtifacts = topologyTemplate.getInstanceArtifacts();

        List<ComponentInstance> instances = component.getComponentInstances();
        if (instDeploymentArtifacts != null && instances != null) {
            instDeploymentArtifacts.entrySet().forEach(e -> {
                Optional<ComponentInstance> ci = instances.stream().filter(i -> i.getUniqueId().equals(e.getKey())).findFirst();
                if (ci.isPresent()) {
                    Map<String, ArtifactDataDefinition> mapToscaDataDefinition = e.getValue().getMapToscaDataDefinition();
                    Map<String, ArtifactDefinition> deplArt = mapToscaDataDefinition.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, en -> new ArtifactDefinition(en.getValue())));

                    ci.get().setDeploymentArtifacts(deplArt);
                }
            });
        }
        if (instanceArtifacts != null && instances != null) {
            instanceArtifacts.entrySet().forEach(e -> {
                Optional<ComponentInstance> ci = instances.stream().filter(i -> i.getUniqueId().equals(e.getKey())).findFirst();
                if (ci.isPresent()) {
                    Map<String, ArtifactDataDefinition> mapToscaDataDefinition = e.getValue().getMapToscaDataDefinition();
                    Map<String, ArtifactDefinition> deplArt = mapToscaDataDefinition.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, en -> new ArtifactDefinition(en.getValue())));

                    ci.get().setArtifacts(deplArt);
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
        requirementCapabilityRelDef.setRelationships(Arrays.asList(rel));

        return requirementCapabilityRelDef;
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

            relationsList.add(requirementCapabilityRelDef);
        });

        return relationsList;
    }

    private static void convertCapabilities(Component component, TopologyTemplate topologyTemplate) {
        if(componentInstancesCapabilitiesExist(component) || groupsCapabilitiesExist(component)){
            topologyTemplate.setCalculatedCapabilities(new HashMap<>());
            topologyTemplate.setCalculatedCapabilitiesProperties(new HashMap<>());
        }
        convertComponentInstancesCapabilities(component, topologyTemplate);
        convertGroupsCapabilities(component, topologyTemplate);
    }

    private static void convertGroupsCapabilities(Component component, TopologyTemplate topologyTemplate) {
        if(groupsCapabilitiesExist(component)){
            component.getGroups()
                    .stream()
                    .filter(g -> MapUtils.isNotEmpty(g.getCapabilities()))
                    .forEach(g -> addCapabilities(topologyTemplate, g.getCapabilities(), g.getUniqueId()));
        }
    }

    private static void convertComponentInstancesCapabilities(Component component, TopologyTemplate topologyTemplate) {
        if (componentInstancesCapabilitiesExist(component)) {
            component.getComponentInstances()
                    .stream()
                    .filter(i -> MapUtils.isNotEmpty(i.getCapabilities()))
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
        return component.getCapabilities() != null && component.getComponentInstances() != null
                && component.getComponentInstances()
                .stream()
                .filter(ci->MapUtils.isNotEmpty(ci.getCapabilities()))
                .findFirst()
                .isPresent();
    }
    private static boolean groupsCapabilitiesExist(Component component) {
        return component.getCapabilities() != null && component.getGroups() != null
                && component.getGroups()
                .stream()
                .filter(g->MapUtils.isNotEmpty(g.getCapabilities()))
                .findFirst()
                .isPresent();
    }
    public static MapCapabilityProperty convertToMapOfMapCapabiltyProperties(Map<String, List<CapabilityDefinition>> instCapabilities, String ownerId) {
        return convertToMapOfMapCapabiltyProperties(instCapabilities, ownerId, false);
    }

	public static MapCapabilityProperty convertToMapOfMapCapabiltyProperties(Map<String, List<CapabilityDefinition>> capabilities, String ownerId, boolean fromCsar) {

        Map<String, MapPropertiesDataDefinition> toscaCapPropMap = new HashMap<>();
		if(MapUtils.isNotEmpty(capabilities))
			capabilities.forEach((s, caps)-> {

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
				}
			);
        return new MapCapabilityProperty(toscaCapPropMap);
    }

	private static MapListCapabilityDataDefinition convertToMapListCapabiltyDataDefinition(Map<String, List<CapabilityDefinition>> instCapabilities) {

        Map<String, ListCapabilityDataDefinition> mapToscaDataDefinition = new HashMap<>();
        for (Entry<String, List<CapabilityDefinition>> instCapability : instCapabilities.entrySet()) {
			mapToscaDataDefinition.put(instCapability.getKey(), new ListCapabilityDataDefinition(instCapability.getValue().stream().map(CapabilityDataDefinition::new).collect(Collectors.toList())));
        }

        return new MapListCapabilityDataDefinition(mapToscaDataDefinition);
    }

    private static void convertRequirements(Component component, TopologyTemplate topologyTemplate) {

        if (component.getRequirements() != null && component.getComponentInstances() != null) {
            topologyTemplate.setCalculatedRequirements(new HashMap<>());
            for (ComponentInstance instance : component.getComponentInstances()) {
                Map<String, List<RequirementDefinition>> instRequirements = instance.getRequirements();
                if (MapUtils.isNotEmpty(instRequirements)) {
                    if (topologyTemplate.getCalculatedRequirements() == null) {
                        topologyTemplate.setCalculatedRequirements(new HashMap<>());
                    }
                    topologyTemplate.getCalculatedRequirements().put(instance.getUniqueId(), convertToMapListRequirementDataDefinition(instRequirements));
                }
            }
        }
    }

    private static MapListRequirementDataDefinition convertToMapListRequirementDataDefinition(Map<String, List<RequirementDefinition>> instRequirements) {

        Map<String, ListRequirementDataDefinition> mapToscaDataDefinition = new HashMap<>();
        for (Entry<String, List<RequirementDefinition>> instRequirement : instRequirements.entrySet()) {
			mapToscaDataDefinition.put(instRequirement.getKey(), new ListRequirementDataDefinition(instRequirement.getValue().stream().map(RequirementDataDefinition::new).collect(Collectors.toList())));
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

        //archive
        component.setArchived(toscaElement.isArchived() == null ? false : toscaElement.isArchived());


        //component.setArchiveTime(toscaElement.getArchiveTime() == null ? 0L : toscaElement.getArchiveTime());
        component.setArchiveTime(toscaElement.getArchiveTime());
        component.setVspArchived(toscaElement.isVspArchived() == null ? false : toscaElement.isVspArchived());

        if (component.getComponentType() == ComponentTypeEnum.RESOURCE) {
            Resource resource = (Resource) component;
            resource.setAbstract((Boolean) toscaElement.getMetadataValue(JsonPresentationFields.IS_ABSTRACT));
            resource.setToscaResourceName((String) toscaElement.getMetadataValue(JsonPresentationFields.TOSCA_RESOURCE_NAME));
            resource.setVendorName((String) toscaElement.getMetadataValue(JsonPresentationFields.VENDOR_NAME));
            resource.setVendorRelease((String) toscaElement.getMetadataValue(JsonPresentationFields.VENDOR_RELEASE));
            // field isn't mandatory , but shouldn't be null(should be an empty string instead)
            if (((String) toscaElement.getMetadataValue(JsonPresentationFields.RESOURCE_VENDOR_MODEL_NUMBER)) != null){
                resource.setResourceVendorModelNumber((String) toscaElement.getMetadataValue(JsonPresentationFields.RESOURCE_VENDOR_MODEL_NUMBER));
            } else {
                resource.setResourceVendorModelNumber("");
            }
        } else if (component.getComponentType() == ComponentTypeEnum.SERVICE) {
            Service service = (Service) component;
            if (((String) toscaElement.getMetadataValue(JsonPresentationFields.SERVICE_TYPE)) != null){
                service.setServiceType((String) toscaElement.getMetadataValue(JsonPresentationFields.SERVICE_TYPE));
            } else {
                service.setServiceType("");
            }
            if (((String) toscaElement.getMetadataValue(JsonPresentationFields.SERVICE_ROLE)) != null){
                service.setServiceRole((String) toscaElement.getMetadataValue(JsonPresentationFields.SERVICE_ROLE));
            } else {
                service.setServiceRole("");
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


        Map<String, PropertyDataDefinition> properties = toscaElement.getProperties();
        if (properties != null && !properties.isEmpty()) {
			List<PropertyDefinition> propertiesMap = properties.values().stream().map(PropertyDefinition::new).collect(Collectors.toList());
            ((Resource) component).setProperties(propertiesMap);
        }

        component.setToscaType(toscaElement.getToscaType().getValue());
    }

    private static NodeType convertToNodeType(Component component) {
        Resource resource = (Resource) component;
        NodeType nodeType = new NodeType();
        nodeType.setDerivedFrom(resource.getDerivedFrom());
        nodeType.setDerivedList(resource.getDerivedList());
        nodeType.setResourceType(resource.getResourceType());
        convertCommonToscaData(component, nodeType);
        convertAdditionalInformation(component, nodeType);
        convertArtifacts(resource, nodeType);
        convertCapabilities(resource, nodeType);
        convertRequirements(resource, nodeType);
        convertAttributes(resource, nodeType);
        convertProperties(resource, nodeType);
        convertInterfaces(resource, nodeType);
        return nodeType;
    }

    private static void convertProperties(Resource resource, NodeType nodeType) {
        List<PropertyDefinition> properties = resource.getProperties();
        if (properties != null && !properties.isEmpty()) {
			Map<String, PropertyDataDefinition> propertiesMap = properties.stream().collect(Collectors.toMap(PropertyDefinition::getName, PropertyDataDefinition::new));
            nodeType.setProperties(propertiesMap);
        }
    }

    private static void convertInterfaces(Resource resource, NodeType nodeType) {
        Map<String, InterfaceDefinition> interfaces = resource.getInterfaces();
        if (interfaces != null) {
			Map<String, InterfaceDataDefinition> interfaceArtifacts = interfaces.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, x -> new InterfaceDataDefinition(x.getValue())));
            nodeType.setInterfaceArtifacts(interfaceArtifacts);
        }
    }

    private static void convertAdditionalInformation(Component component, ToscaElement toscaElement) {
        List<AdditionalInformationDefinition> additionalInformation = component.getAdditionalInformation();
        if (additionalInformation != null) {
			Map<String, AdditionalInfoParameterDataDefinition> addInfo = additionalInformation.stream().collect(Collectors.toMap(AdditionalInformationDefinition::getUniqueId, AdditionalInfoParameterDataDefinition::new));
            toscaElement.setAdditionalInformation(addInfo);
        }
    }

    private static void convertAdditionalInformation(ToscaElement toscaElement, Component resource) {
        Map<String, AdditionalInfoParameterDataDefinition> additionalInformation = toscaElement.getAdditionalInformation();
        if (additionalInformation != null) {
			List<AdditionalInformationDefinition> addInfo = additionalInformation.values().stream().map(AdditionalInformationDefinition::new).collect(Collectors.toList());
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
            Map<String, ArtifactDataDefinition> copy = artifacts.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDataDefinition(e.getValue())));
            toscaElement.setArtifacts(copy);
        }

        Map<String, ArtifactDefinition> toscaArtifacts = component.getToscaArtifacts();
        if (toscaArtifacts != null) {
            Map<String, ArtifactDataDefinition> copy = toscaArtifacts.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDataDefinition(e.getValue())));
            toscaElement.setToscaArtifacts(copy);
        }

        Map<String, ArtifactDefinition> deploymentArtifacts = component.getDeploymentArtifacts();
        if (deploymentArtifacts != null) {
            Map<String, ArtifactDataDefinition> copy = deploymentArtifacts.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDataDefinition(e.getValue())));
            toscaElement.setDeploymentArtifacts(copy);
        }
    }

    private static void convertServiceApiArtifacts(Service service, TopologyTemplate topologyTemplate) {
        Map<String, ArtifactDefinition> serviceApiArtifacts = service.getServiceApiArtifacts();
        if (serviceApiArtifacts != null) {
            Map<String, ArtifactDataDefinition> copy = serviceApiArtifacts.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDataDefinition(e.getValue())));
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

                        ListCapabilityDataDefinition listCapabilityDataDefinition = new ListCapabilityDataDefinition(capList);
                        toscaCapMap.put(s, listCapabilityDataDefinition);

                        for (CapabilityDefinition cap : caps) {
                            List<ComponentInstanceProperty> capPrps = cap.getProperties();
                            if (capPrps != null && !capPrps.isEmpty()) {

                                MapPropertiesDataDefinition dataToCreate = new MapPropertiesDataDefinition();
                                for (ComponentInstanceProperty cip : capPrps) {
                                    dataToCreate.put(cip.getName(), new PropertyDataDefinition(cip));
                                }

                                toscaCapPropMap.put(s + CAP_PROP_DELIM + cap.getName(), dataToCreate);
                            }
                        }
                    }
                }
			);

            toscaElement.setCapabilties(toscaCapMap);
            toscaElement.setCapabiltiesProperties(toscaCapPropMap);
        }
    }

    private static void convertAttributes(Resource component, NodeType nodeType) {
        List<PropertyDefinition> attributes = component.getAttributes();
        if (attributes != null) {
            Map<String, PropertyDataDefinition> attrsByName = attributes.stream().map(PropertyDataDefinition::new).collect(Collectors.toMap(PropertyDataDefinition::getName, Function.identity()));
            nodeType.setAttributes(attrsByName);
        }
    }

    private static void convertRequirements(Resource component, NodeType nodeType) {
        Map<String, List<RequirementDefinition>> requirements = component.getRequirements();

        Map<String, ListRequirementDataDefinition> toscaReqMap = new HashMap<>();

        if (requirements != null && !requirements.isEmpty()) {
			requirements.forEach((s, reqs)-> {

                    if (reqs != null && !reqs.isEmpty()) {
						List<RequirementDataDefinition> reqList = reqs.stream().map(RequirementDataDefinition::new).collect(Collectors.toList());

                        ListRequirementDataDefinition listRequirementDataDefinition = new ListRequirementDataDefinition(reqList);
                        toscaReqMap.put(s, listRequirementDataDefinition);
                    }
                }
			);
            nodeType.setRequirements(toscaReqMap);
        }
    }

    private static void convertCapabilities(NodeType toscaElement, Component component) {
        Map<String, ListCapabilityDataDefinition> toscaCapabilities = toscaElement.getCapabilties();
        Map<String, MapPropertiesDataDefinition> toscaCapPropMap = toscaElement.getCapabiltiesProperties();

        Map<String, List<CapabilityDefinition>> compCap = new HashMap<>();
        if (toscaCapabilities == null || toscaCapabilities.isEmpty())
            return;
		toscaCapabilities.forEach((s, cap)-> {

                if (cap != null) {
                    List<CapabilityDataDefinition> capDataList = cap.getListToscaDataDefinition();

                    if (capDataList != null && !capDataList.isEmpty()) {
						List<CapabilityDefinition> capList = capDataList.stream().map(CapabilityDefinition::new).collect(Collectors.toList());
                        compCap.put(s, capList);
                    }
                }

            }
		);
        if (toscaCapPropMap != null && !toscaCapPropMap.isEmpty()) {
			toscaCapPropMap.forEach((s, capProp)-> {
                    String[] result = s.split(CAP_PROP_DELIM);
                    if (capProp != null) {
                        Map<String, PropertyDataDefinition> capMap = capProp.getMapToscaDataDefinition();

                        if (capMap != null && !capMap.isEmpty()) {
							List<ComponentInstanceProperty> capPropsList = capMap.values().stream().map(ComponentInstanceProperty::new).collect(Collectors.toList());

                            List<CapabilityDefinition> cap = compCap.get(result[0]);
                            Optional<CapabilityDefinition> op = cap.stream().filter(c -> c.getName().equals(result[1])).findFirst();
                            if (op.isPresent()) {
                                op.get().setProperties(capPropsList);
                            }
                        }
                    }
                }
			);
        }

        component.setCapabilities(compCap);

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
			policyDefinitions = policies.values().stream().map(PolicyDefinition::new).collect(Collectors.toMap(PolicyDefinition::getUniqueId, Function.identity()));
        }
        component.setPolicies(policyDefinitions);
    }

    private static void convertGroups(Component component, TopologyTemplate toscaElement) {
        List<GroupDefinition> groupDefinitions = component.getGroups();
        Map<String, GroupDataDefinition> groups = new HashMap<>();

        if (groupDefinitions != null && groups.isEmpty()) {
			groups = groupDefinitions.stream().collect(Collectors.toMap(GroupDefinition::getName, GroupDefinition::new));
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
        if (toscaRequirements == null || toscaRequirements.isEmpty())
            return;
		toscaRequirements.forEach((s, req) -> {

                if (req != null) {
                    List<RequirementDataDefinition> reqDataList = req.getListToscaDataDefinition();

                    if (reqDataList != null && !reqDataList.isEmpty()) {
						List<RequirementDefinition> reqList = reqDataList.stream().map(RequirementDefinition::new).collect(Collectors.toList());
                        compReqs.put(s, reqList);
                    }
                }
            }
		);
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
            topologyTemplate.setMetadataValue(JsonPresentationFields.IMPORTED_TOSCA_CHECKSUM, resource.getImportedToscaChecksum());
			convertInterfaces(resource, topologyTemplate);
        }
        if (componentType == ComponentTypeEnum.SERVICE) {
            convertServiceSpecificEntities((Service) component, topologyTemplate);
        }
        convertCommonToscaData(component, topologyTemplate);
        convertArtifacts(component, topologyTemplate);

        convertAdditionalInformation(component, topologyTemplate);
        convertComponentInstances(component, topologyTemplate);

        convertInputs(component, topologyTemplate);
        convertCapabilities(component, topologyTemplate);
        convertGroups(component, topologyTemplate);
        convertPolicies(component, topologyTemplate);
        convertRequirements(component, topologyTemplate);
        convertRelationsToComposition(component, topologyTemplate);

        return topologyTemplate;
    }

	private static void convertInterfaces(Resource resource, TopologyTemplate topologyTemplate) {
		Map<String, InterfaceDefinition> interfaces = resource.getInterfaces();
		if (interfaces != null && !interfaces.isEmpty()) {
			Map<String, InterfaceDataDefinition> copy = interfaces.entrySet().stream()
					.collect(Collectors.toMap(Map.Entry::getKey, e -> new InterfaceDataDefinition(e.getValue())));
			topologyTemplate.setInterfaces(copy);
		}
	}

    private static void convertServiceInterfaces(Service service, TopologyTemplate topologyTemplate) {
        Map<String, InterfaceDefinition> interfaces = service.getInterfaces();
        if (interfaces != null && !interfaces.isEmpty()) {
            Map<String, InterfaceDataDefinition> copy = interfaces.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new InterfaceDataDefinition(e.getValue())));
            topologyTemplate.setInterfaces(copy);
        }
    }

    private static void convertServiceSpecificEntities(Service service, TopologyTemplate topologyTemplate) {
        convertServiceMetaData(service, topologyTemplate);
        convertServiceApiArtifacts(service, topologyTemplate);
        convertServicePaths(service,topologyTemplate);
        convertServiceInterfaces(service, topologyTemplate);
    }

    private static void convertServicePaths(Service service, TopologyTemplate topologyTemplate) {
        Map<String, ForwardingPathDataDefinition> servicePaths = service.getForwardingPaths();
        if (servicePaths != null && !servicePaths.isEmpty()) {
            Map<String, ForwardingPathDataDefinition> copy = servicePaths.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ForwardingPathDataDefinition(e.getValue())));
            topologyTemplate.setForwardingPaths(copy);
        }
    }

	private static void convertServiceMetaData(Service service, TopologyTemplate topologyTemplate) {
		if (service.getDistributionStatus() != null) {
			topologyTemplate.setMetadataValue(JsonPresentationFields.DISTRIBUTION_STATUS,
					service.getDistributionStatus().name());
		}
		topologyTemplate.setMetadataValue(JsonPresentationFields.PROJECT_CODE, service.getProjectCode());
		topologyTemplate.setMetadataValue(JsonPresentationFields.ECOMP_GENERATED_NAMING,
				service.isEcompGeneratedNaming());
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

            Map<String, RelationshipInstDataDefinition> relations = componentInstancesRelations.stream().flatMap(x -> convertRelationToToscaRelation(x).stream()).filter(i -> i.getUniqueId() != null)
					.collect(Collectors.toMap(RelationshipInstDataDefinition::getUniqueId, Function.identity()));
            compositionDataDefinition.setRelations(relations);
        }
    }

    private static void convertInputs(Component component, TopologyTemplate topologyTemplate) {
        List<InputDefinition> inputsList = component.getInputs();
        if (inputsList != null && !inputsList.isEmpty()) {

			Map<String, PropertyDataDefinition> inputsMap = inputsList.stream().map(PropertyDataDefinition::new).collect(Collectors.toMap(PropertyDataDefinition::getName, Function.identity()));
            topologyTemplate.setInputs(inputsMap);
        }

    }

    private static void convertInputs(TopologyTemplate topologyTemplate, Component component) {
        Map<String, PropertyDataDefinition> inputsMap = topologyTemplate.getInputs();
        if (inputsMap != null && !inputsMap.isEmpty()) {
			List<InputDefinition> inputsList = inputsMap.values()
                    .stream()
                    .map(InputDefinition::new)
                    .collect(Collectors.toList());
            component.setInputs(inputsList);
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
            // field isn't mandatory , but shouldn't be null(should be an empty string instead)
            if (((Resource) component).getResourceVendorModelNumber() != null){
                toscaElement.setMetadataValue(JsonPresentationFields.RESOURCE_VENDOR_MODEL_NUMBER, ((Resource) component).getResourceVendorModelNumber());
            } else {
                toscaElement.setMetadataValue(JsonPresentationFields.RESOURCE_VENDOR_MODEL_NUMBER, "");
            }
        } else if (component.getComponentType() == ComponentTypeEnum.SERVICE) {
            // field isn't mandatory , but shouldn't be null(should be an empty string instead)
            if (((Service) component).getServiceType() != null){
                toscaElement.setMetadataValue(JsonPresentationFields.SERVICE_TYPE, ((Service) component).getServiceType());
            } else {
                toscaElement.setMetadataValue(JsonPresentationFields.SERVICE_TYPE, "");
            }
            if (((Service) component).getServiceRole() != null){
                toscaElement.setMetadataValue(JsonPresentationFields.SERVICE_ROLE, ((Service) component).getServiceRole());
            } else {
                toscaElement.setMetadataValue(JsonPresentationFields.SERVICE_ROLE, "");
            }
        }
        toscaElement.setMetadataValue(JsonPresentationFields.CONFORMANCE_LEVEL, component.getConformanceLevel());
        toscaElement.setMetadataValue(JsonPresentationFields.IS_DELETED, component.getIsDeleted());
        toscaElement.setMetadataValue(JsonPresentationFields.ICON, component.getIcon());
        toscaElement.setMetadataValue(JsonPresentationFields.DESCRIPTION, component.getDescription());
        toscaElement.setMetadataValue(JsonPresentationFields.TAGS, component.getTags());
        toscaElement.setMetadataValue(JsonPresentationFields.INVARIANT_UUID, component.getInvariantUUID());
        toscaElement.setMetadataValue(JsonPresentationFields.CONTACT_ID, component.getContactId());
    }



    private static void setComponentInstancesToComponent(TopologyTemplate topologyTemplate, Component component) {

        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance currComponentInstance;
        for (Map.Entry<String, ComponentInstanceDataDefinition> entry : topologyTemplate.getComponentInstances().entrySet()) {
            String key = entry.getKey();
            currComponentInstance = new ComponentInstance(topologyTemplate.getComponentInstances().get(key));
            if (topologyTemplate.getInstGroups() != null && topologyTemplate.getInstGroups().containsKey(key)) {
                List<GroupInstance> groupInstances = topologyTemplate.getInstGroups().get(key).getMapToscaDataDefinition().entrySet().stream().map(e -> new GroupInstance(e.getValue())).collect(Collectors.toList());
                currComponentInstance.setGroupInstances(groupInstances);
            }
            componentInstances.add(currComponentInstance);
        }
        component.setComponentInstances(componentInstances);
    }

    private static void setComponentInstancesInputsToComponent(TopologyTemplate topologyTemplate, Component component) {
        if (topologyTemplate.getInstInputs() != null) {
            Map<String, List<ComponentInstanceInput>> inputs = new HashMap<>();
            for (Entry<String, MapPropertiesDataDefinition> entry : topologyTemplate.getInstInputs().entrySet()) {
                if (entry.getValue() != null && entry.getValue().getMapToscaDataDefinition() != null) {
                    String key = entry.getKey();
                    List<ComponentInstanceInput> componentInstanceAttributes = entry.getValue().getMapToscaDataDefinition().entrySet().stream().map(e -> new ComponentInstanceInput(e.getValue())).collect(Collectors.toList());
                    inputs.put(key, componentInstanceAttributes);
                }
            }
            component.setComponentInstancesInputs(inputs);
        }
    }

    private static void setComponentInstancesPropertiesToComponent(TopologyTemplate topologyTemplate, Component component) {
        if (topologyTemplate.getInstProperties() != null) {
            Map<String, List<ComponentInstanceProperty>> properties = new HashMap<>();
            for (Entry<String, MapPropertiesDataDefinition> entry : topologyTemplate.getInstProperties().entrySet()) {
                if (entry.getValue() != null && entry.getValue().getMapToscaDataDefinition() != null) {
                    String key = entry.getKey();
                    List<ComponentInstanceProperty> componentInstanceAttributes = entry.getValue().getMapToscaDataDefinition().entrySet().stream().map(e -> new ComponentInstanceProperty(new PropertyDefinition(e.getValue())))
                            .collect(Collectors.toList());
                    properties.put(key, componentInstanceAttributes);
                }
            }
            component.setComponentInstancesProperties(properties);
        }
    }

    private static void setComponentInstancesAttributesToComponent(TopologyTemplate topologyTemplate, Component component) {
        if (topologyTemplate.getInstAttributes() != null) {
            Map<String, List<ComponentInstanceProperty>> attributes = new HashMap<>();
            for (Map.Entry<String, MapPropertiesDataDefinition> entry : topologyTemplate.getInstAttributes().entrySet()) {
                if (entry.getValue() != null && entry.getValue().getMapToscaDataDefinition() != null) {
                    String key = entry.getKey();
                    List<ComponentInstanceProperty> componentInstanceAttributes = entry.getValue().getMapToscaDataDefinition().entrySet().stream().map(e -> new ComponentInstanceProperty(new ComponentInstanceProperty(e.getValue())))
                            .collect(Collectors.toList());
                    attributes.put(key, componentInstanceAttributes);
                }
            }
            component.setComponentInstancesAttributes(attributes);
        }
    }

    private static void setComponentInstancesRequirementsToComponent(TopologyTemplate topologyTemplate, Component component) {

        if (topologyTemplate.getCalculatedRequirements() != null) {
            // Requirements of component organized by capability
            Map<String, List<RequirementDefinition>> instancesRequirements = new HashMap<>();

            Map<String, ComponentInstance> instancesMap = new HashMap<>();
            for (ComponentInstance currInstance : component.getComponentInstances()) {
                instancesMap.put(currInstance.getUniqueId(), currInstance);
            }
            for (Map.Entry<String, MapListRequirementDataDefinition> entry : topologyTemplate.getCalculatedRequirements().entrySet()) {

                String instanceId = entry.getKey();
                // Requirements of instance organized by capability
                Map<String, ListRequirementDataDefinition> capsMapList = entry.getValue().getMapToscaDataDefinition();

                if(capsMapList != null) {
                    for (Entry<String, ListRequirementDataDefinition> entryTypeList : capsMapList.entrySet()) {
                        String capabilityType = entryTypeList.getKey();
					List<RequirementDefinition> caps = entryTypeList.getValue().getListToscaDataDefinition().stream().map(RequirementDefinition::new).collect(Collectors.toList());
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
    }

    private static void setComponentInstancesCapabilitiesToComponentAndCI(TopologyTemplate topologyTemplate, Component component) {
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

                if(capsMapList != null) {
                    for (Entry<String, ListCapabilityDataDefinition> entryTypeList : capsMapList.entrySet()) {
                        String capabilityType = entryTypeList.getKey();
                        List<CapabilityDefinition> caps = entryTypeList.getValue().getListToscaDataDefinition().stream().map(cap -> mergeInstCapabiltyWithProperty(cap, instanceId, calculatedCapProperties)).collect(Collectors.toList());
                        if (instancesCapabilities.containsKey(capabilityType)) {
                            instancesCapabilities.get(capabilityType).addAll(caps);
                        } else {
                            instancesCapabilities.put(capabilityType, caps);
                        }
                        ComponentInstance instance = instancesMap.get(instanceId);
                        if (instance == null) {
                            log.error("instance is null for id {} entry {}", instanceId, entry.getValue().getToscaPresentationValue(JsonPresentationFields.NAME));
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

	private static void setCapabilitiesToComponentAndGroups(TopologyTemplate topologyTemplate, Component component) {

		Map<String, MapCapabilityProperty> calculatedCapProperties = topologyTemplate.getCalculatedCapabilitiesProperties();

		if (capabilitiesAndGroupsExist(topologyTemplate, component)) {
			Map<String, GroupDefinition> groupsMap = component.getGroups().stream().collect(Collectors.toMap(GroupDefinition::getUniqueId,Function.identity()));

			for (Map.Entry<String, MapListCapabilityDataDefinition> entry : topologyTemplate.getCalculatedCapabilities().entrySet()) {
				findSetCapabilitiesToComponentAndGroup(calculatedCapProperties, component, groupsMap, entry);
			}
		}
	}

	private static boolean capabilitiesAndGroupsExist(TopologyTemplate topologyTemplate, Component component) {
		return MapUtils.isNotEmpty(topologyTemplate.getCalculatedCapabilities()) && CollectionUtils.isNotEmpty(component.getGroups());
	}

	private static void findSetCapabilitiesToComponentAndGroup(Map<String, MapCapabilityProperty> calculatedCapProperties, Component component, Map<String, GroupDefinition> groupsMap, Map.Entry<String, MapListCapabilityDataDefinition> entry) {

		String uniqueId = entry.getKey();
		if(groupsMap.containsKey(uniqueId)){
			setCapabilitiesToComponentAndGroup(calculatedCapProperties, component, entry, groupsMap.get(uniqueId));
		} else {
			log.warn("The group with uniqueId {} was not found", uniqueId);
		}
	}

	private static void setCapabilitiesToComponentAndGroup(Map<String, MapCapabilityProperty> calculatedCapProperties, Component component, Map.Entry<String, MapListCapabilityDataDefinition> entry, GroupDefinition group) {

		for (Entry<String, ListCapabilityDataDefinition> entryTypeList : entry.getValue().getMapToscaDataDefinition().entrySet()) {
			String capabilityType = entryTypeList.getKey();
			List<CapabilityDefinition> caps = entryTypeList.getValue().getListToscaDataDefinition().stream().map(cap -> mergeInstCapabiltyWithProperty(cap, group.getUniqueId(), calculatedCapProperties)).collect(Collectors.toList());
			if (component.getCapabilities().containsKey(capabilityType)) {
				component.getCapabilities().get(capabilityType).addAll(caps);
			} else {
				component.getCapabilities().put(capabilityType, caps);
			}
			group.getCapabilities().put(capabilityType, Lists.newArrayList(caps));
		}
	}

	private static CapabilityDefinition mergeInstCapabiltyWithProperty(CapabilityDataDefinition cap, String ownerId, Map<String, MapCapabilityProperty> calculatedCapProperties) {
        CapabilityDefinition capability = new CapabilityDefinition(cap);
        if (calculatedCapProperties != null) {
			MapCapabilityProperty mapOfMapPropertiesDataDefinition = calculatedCapProperties.get(ownerId);
            if (mapOfMapPropertiesDataDefinition != null && mapOfMapPropertiesDataDefinition.getMapToscaDataDefinition() != null) {
                Map<String, MapPropertiesDataDefinition> toscaCapPropMap = mapOfMapPropertiesDataDefinition.getMapToscaDataDefinition();
				toscaCapPropMap.forEach(( keyPath,  capProp)-> findConvertSetProperties(cap, ownerId, capability, keyPath, capProp));
			}
		}
		return capability;
	}

	private static void findConvertSetProperties(CapabilityDataDefinition cap, String primaryPathKey, CapabilityDefinition capability, String path, MapPropertiesDataDefinition capProp) {
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
			capability.setProperties(capProp.getMapToscaDataDefinition().values().stream().map(ComponentInstanceProperty::new).collect(Collectors.toList()));
		}
	}

	private static boolean relatedPropertiesExist(CapabilityDataDefinition cap, String primaryPathKey,	MapPropertiesDataDefinition capProp, String[] result) {
		return capProp != null && MapUtils.isNotEmpty(capProp.getMapToscaDataDefinition()) && areRelatedProperties(cap, primaryPathKey, result);
	}

	private static boolean areRelatedProperties(CapabilityDataDefinition cap, String primaryPathKey, String[] result) {
		int primaryKeyIndex = 0;
		int ownerIndex = 1;
		int typeIndex = result.length - 2;
		int nameIndex = result.length - 1;
		return result[typeIndex].equals(cap.getType()) && result[nameIndex].equals(cap.getName()) && cap.getOwnerId().equals(result[ownerIndex]) && primaryPathKey.equals(result[primaryKeyIndex]);
    }

    private static void setComponentInstancesToTopologyTemplate(Component component, TopologyTemplate topologyTemplate) {

        Map<String, ComponentInstanceDataDefinition> componentInstances = new HashMap<>();
        ComponentInstanceDataDefinition convertedInstance;
        if (component.getComponentInstances() != null) {
            for (ComponentInstance instance : component.getComponentInstances()) {
                convertedInstance = new ComponentInstanceDataDefinition(instance);
                if (instance.getGroupInstances() != null) {
                    MapGroupsDataDefinition groupsMap = new MapGroupsDataDefinition();

					groupsMap.setMapToscaDataDefinition(instance.getGroupInstances().stream().map(GroupInstanceDataDefinition::new).collect(Collectors.toMap(GroupInstanceDataDefinition::getName, Function.identity())));
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

				inputsMap.setMapToscaDataDefinition(entry.getValue().stream().map(PropertyDataDefinition::new).collect(Collectors.toMap(PropertyDataDefinition::getName, Function.identity())));

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

				propertiesMap.setMapToscaDataDefinition(entry.getValue().stream().map(PropertyDataDefinition::new).collect(Collectors.toMap(PropertyDataDefinition::getName, Function.identity())));

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
					Map<String, ArtifactDataDefinition> mapToscaDataDefinitionArtifact = artifacts.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDataDefinition(e.getValue())));
                    MapArtifactDataDefinition insArtifact = new MapArtifactDataDefinition(mapToscaDataDefinitionArtifact);
                    topologyTemplate.getInstanceArtifacts().put(ci.getUniqueId(), insArtifact);
                }

                Map<String, ArtifactDefinition> deplArtifacts = ci.getDeploymentArtifacts();
                if (deplArtifacts != null) {
					Map<String, ArtifactDataDefinition> mapToscaDataDefinitionDepArtifact = deplArtifacts.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDataDefinition(e.getValue())));
                    MapArtifactDataDefinition insDepArtifact = new MapArtifactDataDefinition(mapToscaDataDefinitionDepArtifact);
                    topologyTemplate.getInstDeploymentArtifacts().put(ci.getUniqueId(), insDepArtifact);
                }
            }
        }
    }

    private static void setComponentInstancesAttributesToTopologyTemplate(Component component, TopologyTemplate topologyTemplate) {

        if (component.getComponentInstancesAttributes() != null) {
            topologyTemplate.setInstAttributes(new HashMap<>());
            MapPropertiesDataDefinition attributesMap;
            for (Entry<String, List<ComponentInstanceProperty>> entry : component.getComponentInstancesAttributes().entrySet()) {
                attributesMap = new MapPropertiesDataDefinition();

				attributesMap.setMapToscaDataDefinition(entry.getValue().stream().map(PropertyDataDefinition::new).collect(Collectors.toMap(PropertyDataDefinition::getName, Function.identity())));

                topologyTemplate.getInstAttributes().put(entry.getKey(), attributesMap);
            }
        }
    }

    public static ComponentMetadataData convertToComponentMetadata(GraphVertex vertex) {
        ComponentMetadataData metadata = null;
        switch (vertex.getType()) {
        case SERVICE:
            metadata = new ServiceMetadataData(vertex.getMetadataJson());
            break;
        case RESOURCE:
            metadata = new ResourceMetadataData(vertex.getMetadataJson());
            break;
        case PRODUCT:
            metadata = new ProductMetadataData(vertex.getMetadataJson());
            break;
        default:
            break;
        }
        if (metadata != null) {
            metadata.getMetadataDataDefinition().setUniqueId(vertex.getUniqueId());
            metadata.getMetadataDataDefinition().setLastUpdateDate((Long) vertex.getJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE));
            metadata.getMetadataDataDefinition().setUUID((String) vertex.getJsonMetadataField(JsonPresentationFields.UUID));
            metadata.getMetadataDataDefinition().setState((String) vertex.getJsonMetadataField(JsonPresentationFields.LIFECYCLE_STATE));
        }
        return metadata;
    }

    public static List<GroupDefinition> convertToGroupDefinitions(Map<String, GroupDataDefinition> groups) {

        List<GroupDefinition> groupDefinitions = null;
        if (MapUtils.isNotEmpty(groups)) {
			groupDefinitions = groups.values().stream().map(GroupDefinition::new).collect(Collectors.toList());
        }
        return groupDefinitions;
	}

	public static Map<String, MapCapabilityProperty> extractCapabilityProperteisFromInstances(List<ComponentInstance> instances, boolean fromCsar) {
		return instances
				.stream()
				.collect(Collectors.toMap(ComponentInstanceDataDefinition::getUniqueId,
						ci -> convertToMapOfMapCapabiltyProperties(ci.getCapabilities(), ci.getUniqueId(), fromCsar)));
	}

	public static Map<String, MapCapabilityProperty> extractCapabilityPropertiesFromGroups(List<GroupDefinition> groups, boolean fromCsar) {
		if(CollectionUtils.isNotEmpty(groups))
			return groups
				.stream()
				.collect(Collectors.toMap(GroupDefinition::getUniqueId,
						g -> convertToMapOfMapCapabiltyProperties(g.getCapabilities(), g.getUniqueId(), fromCsar)));
		return Maps.newHashMap();
	}

	public static Map<String, MapListCapabilityDataDefinition> extractCapabilitiesFromGroups(final List<GroupDefinition> groupDefinitions) {
		Map<String, MapListCapabilityDataDefinition> calculatedCapabilities = Maps.newHashMap();
		for(GroupDefinition groupDefinition :groupDefinitions){
			calculatedCapabilities.put(groupDefinition.getUniqueId(), new MapListCapabilityDataDefinition(buildMapOfListsOfCapabilities(groupDefinition)));
		}
		return calculatedCapabilities;
	}

	public static Map<String, ListCapabilityDataDefinition> buildMapOfListsOfCapabilities(GroupDefinition groupDefinition) {
		return groupDefinition.getCapabilities().entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e-> new ListCapabilityDataDefinition(e.getValue()
						.stream()
						.map(CapabilityDataDefinition::new)
						.collect(Collectors.toList()))));
    }

}
