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

package org.openecomp.sdc.be.tosca;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datamodel.utils.ArtifactUtils;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.RequirementAndRelationshipPair;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.IResourceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.tosca.model.IToscaMetadata;
import org.openecomp.sdc.be.tosca.model.SubstitutionMapping;
import org.openecomp.sdc.be.tosca.model.ToscaCapability;
import org.openecomp.sdc.be.tosca.model.ToscaGroupTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaMetadata;
import org.openecomp.sdc.be.tosca.model.ToscaNodeTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.openecomp.sdc.be.tosca.model.ToscaTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaTemplateRequirement;
import org.openecomp.sdc.be.tosca.model.ToscaTopolgyTemplate;
import org.openecomp.sdc.be.tosca.model.VfModuleToscaMetadata;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import fj.data.Either;

@org.springframework.stereotype.Component("tosca-export-handler")
public class ToscaExportHandler {

	@Autowired
	private ApplicationDataTypeCache dataTypeCache;

	@Autowired
	private IResourceOperation resourceOperation;

	private CapabiltyRequirementConvertor capabiltyRequirementConvertor = CapabiltyRequirementConvertor.getInstance();
	private PropertyConvertor propertyConvertor = PropertyConvertor.getInstance();

	private static Logger log = LoggerFactory.getLogger(ToscaExportHandler.class.getName());

	public static final String TOSCA_VERSION = "tosca_simple_yaml_1_0";
	public static final String SERVICE_NODE_TYPE_PREFIX = "org.openecomp.service.";
	public static final String IMPORTS_FILE_KEY = "file";
	public static final String TOSCA_TEMPLATE_NAME = "-template.yml";
	public static final String ASSET_TOSCA_TEMPLATE = "assettoscatemplate";
	public static final String VF_MODULE_TYPE_KEY = "vf_module_type";
	public static final String VF_MODULE_DESC_KEY = "vf_module_description";
	public static final String VOLUME_GROUP_KEY = "volume_group";
	public static final String VF_MODULE_TYPE_BASE = "Base";
	public static final String VF_MODULE_TYPE_EXPANSION = "Expansion";

	public Either<ToscaRepresentation, ToscaError> exportComponent(Component component) {

		Either<ToscaTemplate, ToscaError> toscaTemplateRes = convertToToscaTemplate(component);
		if (toscaTemplateRes.isRight()) {
			return Either.right(toscaTemplateRes.right().value());
		}

		CustomRepresenter representer = new CustomRepresenter();
		DumperOptions options = new DumperOptions();
		options.setAllowReadOnlyProperties(false);
		options.setPrettyFlow(true);

		options.setDefaultFlowStyle(FlowStyle.FLOW);
		options.setCanonical(false);

		ToscaTemplate toscaTemplate = toscaTemplateRes.left().value();
		representer.addClassTag(toscaTemplate.getClass(), Tag.MAP);

		representer.setPropertyUtils(new UnsortedPropertyUtils());
		Yaml yaml = new Yaml(representer, options);

		String yamlAsString = yaml.dumpAsMap(toscaTemplate);

		StringBuilder sb = new StringBuilder();
		sb.append(ConfigurationManager.getConfigurationManager().getConfiguration().getHeatEnvArtifactHeader());
		sb.append(yamlAsString);
		sb.append(ConfigurationManager.getConfigurationManager().getConfiguration().getHeatEnvArtifactFooter());

		ToscaRepresentation toscaRepresentation = new ToscaRepresentation();
		toscaRepresentation.setMainYaml(sb.toString());
		toscaRepresentation.setDependencies(toscaTemplate.getDependencies());

		return Either.left(toscaRepresentation);
	}

	public Either<ToscaTemplate, ToscaError> getDependencies(Component component) {
		ToscaTemplate toscaTemplate = new ToscaTemplate(null);
		Either<ImmutablePair<ToscaTemplate, Map<String, Component>>, ToscaError> fillImports = fillImports(component, toscaTemplate);
		if (fillImports.isRight()) {
			return Either.right(fillImports.right().value());
		}
		return Either.left(fillImports.left().value().left);
	}

	private Either<ToscaTemplate, ToscaError> convertToToscaTemplate(Component component) {
		log.debug("start tosca export for {}", component.getUniqueId());
		ToscaTemplate toscaTemplate = new ToscaTemplate(TOSCA_VERSION);

		toscaTemplate.setMetadata(convertMetadata(component, false));

		Map<String, ToscaNodeType> node_types = new HashMap<>();
		if (ToscaUtils.isNodeType(component)) {
			log.debug("convert component as node type");
			return convertNodeType(component, toscaTemplate, node_types);
		} else {
			log.debug("convert component as topology template");
			return convertToscaTemplate(component, toscaTemplate);
		}

	}

	private Either<ToscaTemplate, ToscaError> convertToscaTemplate(Component component, ToscaTemplate toscaNode) {

		Either<ImmutablePair<ToscaTemplate, Map<String, Component>>, ToscaError> importsRes = fillImports(component, toscaNode);
		if (importsRes.isRight()) {
			return Either.right(importsRes.right().value());
		}
		toscaNode = importsRes.left().value().left;

		Map<String, Component> componentCache = importsRes.left().value().right;
		Either<Map<String, DataTypeDefinition>, TitanOperationStatus> dataTypesEither = dataTypeCache.getAll();
		if (dataTypesEither.isRight()) {
			log.debug("Failed to retrieve all data types {}", dataTypesEither.right().value());
			return Either.right(ToscaError.GENERAL_ERROR);
		}
		Map<String, DataTypeDefinition> dataTypes = dataTypesEither.left().value();

		ToscaTopolgyTemplate topology_template = new ToscaTopolgyTemplate();

		Either<ToscaTopolgyTemplate, ToscaError> inputs = fillInputs(component, topology_template, dataTypes);
		if (inputs.isRight()) {
			return Either.right(inputs.right().value());
		}
		topology_template = inputs.left().value();

		List<ComponentInstance> componentInstances = component.getComponentInstances();
		Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = component.getComponentInstancesProperties();
		List<GroupDefinition> groups = component.getGroups();
		if (componentInstances != null && !componentInstances.isEmpty()) {

			Either<Map<String, ToscaNodeTemplate>, ToscaError> node_templates = convertNodeTemplates(component, componentInstances, componentInstancesProperties, componentCache, dataTypes);
			if (node_templates.isRight()) {
				return Either.right(node_templates.right().value());
			}
			log.debug("node templates converted");

			topology_template.setNode_templates(node_templates.left().value());
		}
		if (groups != null && !groups.isEmpty()) {
			Map<String, ToscaGroupTemplate> groupsMap = new HashMap<String, ToscaGroupTemplate>();
			for (GroupDefinition group : groups) {
				ToscaGroupTemplate toscaGroup = convertGroup(group, component);
				groupsMap.put(group.getName(), toscaGroup);

			}
			topology_template.setGroups(groupsMap);
			log.debug("groups converted");

		}

		SubstitutionMapping substitutionMapping = new SubstitutionMapping();
		String toscaResourceName = null;
		switch (component.getComponentType()) {
		case RESOURCE:
			toscaResourceName = ((ResourceMetadataDataDefinition) component.getComponentMetadataDefinition().getMetadataDataDefinition()).getToscaResourceName();
			break;
		case SERVICE:
			toscaResourceName = SERVICE_NODE_TYPE_PREFIX + component.getComponentMetadataDefinition().getMetadataDataDefinition().getSystemName();
			break;
		default:
			log.debug("Not supported component type {}", component.getComponentType());
			return Either.right(ToscaError.NOT_SUPPORTED_TOSCA_TYPE);
		}
		substitutionMapping.setNode_type(toscaResourceName);

		Either<SubstitutionMapping, ToscaError> capabilities = convertCapabilities(component, substitutionMapping, dataTypes);
		if (capabilities.isRight()) {
			return Either.right(capabilities.right().value());
		}
		substitutionMapping = capabilities.left().value();

		Either<SubstitutionMapping, ToscaError> requirements = capabiltyRequirementConvertor.convertRequirements(component, substitutionMapping);
		if (requirements.isRight()) {
			return Either.right(requirements.right().value());
		}
		substitutionMapping = requirements.left().value();

		topology_template.setSubstitution_mappings(substitutionMapping);

		toscaNode.setTopology_template(topology_template);
		return Either.left(toscaNode);
	}

	private Either<ToscaTopolgyTemplate, ToscaError> fillInputs(Component component, ToscaTopolgyTemplate topology_template, Map<String, DataTypeDefinition> dataTypes) {
		if (log.isDebugEnabled())
			log.debug("fillInputs for component {}", component.getUniqueId());
		List<InputDefinition> inputDef = component.getInputs();
		Map<String, ToscaProperty> inputs = new HashMap<>();

		if (inputDef != null) {
			inputDef.forEach(i -> {
				ToscaProperty property = propertyConvertor.convertProperty(dataTypes, i, false);
				inputs.put(i.getName(), property);
			});
			if (inputs != null && !inputs.isEmpty()) {
				topology_template.setInputs(inputs);
			}
		}
		return Either.left(topology_template);
	}

	private ToscaMetadata convertMetadata(Component component, boolean isInstance) {
		ToscaMetadata toscaMetadata = new ToscaMetadata();
		toscaMetadata.setName(component.getComponentMetadataDefinition().getMetadataDataDefinition().getName());
		toscaMetadata.setInvariantUUID(component.getInvariantUUID());
		toscaMetadata.setUUID(component.getUUID());
		toscaMetadata.setDescription(component.getDescription());

		List<CategoryDefinition> categories = component.getCategories();
		CategoryDefinition categoryDefinition = categories.get(0);
		toscaMetadata.setCategory(categoryDefinition.getName());

		if (isInstance) {
			toscaMetadata.setVersion(component.getVersion());
		}
		switch (component.getComponentType()) {
		case RESOURCE:
			Resource resource = (Resource) component;
			toscaMetadata.setType(resource.getResourceType().name());
			toscaMetadata.setSubcategory(categoryDefinition.getSubcategories().get(0).getName());
			if (!isInstance) {
				toscaMetadata.setResourceVendor(resource.getVendorName());
				toscaMetadata.setResourceVendorRelease(resource.getVendorRelease());
			}
			break;
		case SERVICE:
			toscaMetadata.setType(component.getComponentType().getValue());
			if (!isInstance) {
				toscaMetadata.setServiceEcompNaming(false);
				toscaMetadata.setServiceHoming(false);
			}
			break;
		default:
			log.debug("Not supported component type {}", component.getComponentType());
		}
		return toscaMetadata;
	}

	private Either<ImmutablePair<ToscaTemplate, Map<String, Component>>, ToscaError> fillImports(Component component, ToscaTemplate toscaTemplate) {
		Map<String, Component> componentCache = new HashMap<>();
		if (!ToscaUtils.isNodeType(component)) {
			List<ComponentInstance> componentInstances = component.getComponentInstances();
			if (componentInstances != null && !componentInstances.isEmpty()) {
				List<Map<String, Map<String, String>>> imports = new LinkedList<Map<String, Map<String, String>>>();
				List<Triple<String, String, Component>> dependecies = new ArrayList<>();

				componentInstances.forEach(ci -> {
					createDependency(componentCache, imports, dependecies, ci);
				});
				toscaTemplate.setImports(imports);
				toscaTemplate.setDependencies(dependecies);
			}
		} else {
			log.debug("currently imports supported for VF and service only");
		}
		return Either.left(new ImmutablePair<ToscaTemplate, Map<String, Component>>(toscaTemplate, componentCache));
	}

	private void createDependency(Map<String, Component> componentCache, List<Map<String, Map<String, String>>> imports, List<Triple<String, String, Component>> dependecies, ComponentInstance ci) {
		Map<String, String> files = new HashMap<>();
		Map<String, Map<String, String>> importsListMember = new HashMap<>();

		Component componentRI = componentCache.get(ci.getComponentUid());
		if (componentRI == null) {
			// all resource must be only once!
			Either<Resource, StorageOperationStatus> resource = resourceOperation.getResource(ci.getComponentUid(), true);
			if (resource.isRight()) {
				log.debug("Failed to fetch resource with id {} for instance {}");
			}
			Resource fetchedComponent = resource.left().value();
			componentCache.put(fetchedComponent.getUniqueId(), fetchedComponent);
			componentRI = fetchedComponent;

			Map<String, ArtifactDefinition> toscaArtifacts = componentRI.getToscaArtifacts();
			ArtifactDefinition artifactDefinition = toscaArtifacts.get(ASSET_TOSCA_TEMPLATE);
			if (artifactDefinition != null) {
				String artifactName = artifactDefinition.getArtifactName();
				files.put(IMPORTS_FILE_KEY, artifactName);
				importsListMember.put(ci.getComponentName(), files);
				dependecies.add(new ImmutableTriple<String, String, Component>(artifactName, artifactDefinition.getEsId(), fetchedComponent));
			}
		}
		if (!importsListMember.isEmpty()) {
			imports.add(importsListMember);
		}
	}

	private Either<ToscaTemplate, ToscaError> convertNodeType(Component component, ToscaTemplate toscaNode, Map<String, ToscaNodeType> node_types) {
		log.debug("start convert node type for {}", component.getUniqueId());
		ToscaNodeType toscaNodeType = createNodeType(component);

		Either<Map<String, DataTypeDefinition>, TitanOperationStatus> dataTypesEither = dataTypeCache.getAll();
		if (dataTypesEither.isRight()) {
			log.debug("Failed to fetch all data types :", dataTypesEither.right().value());
			return Either.right(ToscaError.GENERAL_ERROR);
		}

		Map<String, DataTypeDefinition> dataTypes = dataTypesEither.left().value();
		Either<ToscaNodeType, ToscaError> properties = propertyConvertor.convertProperties(component, toscaNodeType, dataTypes);
		if (properties.isRight()) {
			return Either.right(properties.right().value());
		}
		toscaNodeType = properties.left().value();
		log.debug("Properties converted for {}", component.getUniqueId());

		Either<ToscaNodeType, ToscaError> capabilities = convertCapabilities(component, toscaNodeType, dataTypes);
		if (capabilities.isRight()) {
			return Either.right(capabilities.right().value());
		}
		toscaNodeType = capabilities.left().value();
		log.debug("Capabilities converted for {}", component.getUniqueId());

		Either<ToscaNodeType, ToscaError> requirements = capabiltyRequirementConvertor.convertRequirements(component, toscaNodeType);
		if (requirements.isRight()) {
			return Either.right(requirements.right().value());
		}
		toscaNodeType = requirements.left().value();
		log.debug("Requirements converted for {}", component.getUniqueId());

		String toscaResourceName = ((ResourceMetadataDataDefinition) component.getComponentMetadataDefinition().getMetadataDataDefinition()).getToscaResourceName();
		node_types.put(toscaResourceName, toscaNodeType);
		toscaNode.setNode_types(node_types);
		log.debug("finish convert node type for {}", component.getUniqueId());
		return Either.left(toscaNode);
	}

	private Either<Map<String, ToscaNodeTemplate>, ToscaError> convertNodeTemplates(Component component, List<ComponentInstance> componentInstances, Map<String, List<ComponentInstanceProperty>> componentInstancesProperties,
			Map<String, Component> componentCache, Map<String, DataTypeDefinition> dataTypes) {
		log.debug("start convert topology template for {} for type {}", component.getUniqueId(), component.getComponentType());
		Map<String, ToscaNodeTemplate> node_templates = new HashMap<>();

		for (ComponentInstance componentInstance : componentInstances) {
			ToscaNodeTemplate nodeTemplate = new ToscaNodeTemplate();
			nodeTemplate.setType(componentInstance.getToscaComponentName());

			Either<ToscaNodeTemplate, ToscaError> requirements = convertComponentInstanceRequirements(component, componentInstance, component.getComponentInstancesRelations(), nodeTemplate);
			if (requirements.isRight()) {
				return Either.right(requirements.right().value());
			}
			log.debug("Component instance Requirements converted for instance {}", componentInstance.getUniqueId());

			nodeTemplate = requirements.left().value();

			Component componentOfInstance = componentCache.get(componentInstance.getComponentUid());
			nodeTemplate.setMetadata(convertMetadata(componentOfInstance, true));

			Either<ToscaNodeTemplate, ToscaError> capabilties = capabiltyRequirementConvertor.convertComponentInstanceCapabilties(componentInstance, dataTypes, nodeTemplate);
			if (capabilties.isRight()) {
				return Either.right(requirements.right().value());
			}
			log.debug("Component instance Capabilties converted for instance {}", componentInstance.getUniqueId());

			nodeTemplate = capabilties.left().value();

			if (componentInstancesProperties.containsKey(componentInstance.getUniqueId())) {
				Map<String, Object> props = null;
				List<ComponentInstanceProperty> propList = componentInstancesProperties.get(componentInstance.getUniqueId());
				List<ComponentInstanceProperty> collect = propList.stream().filter(e -> e.getValueUniqueUid() != null && !e.getValueUniqueUid().isEmpty()).collect(Collectors.toList());
				if (collect != null && !collect.isEmpty()) {
					props = new HashMap<String, Object>();
					for (ComponentInstanceProperty prop : collect) {
						Object convertedValue = convertInstanceProperty(dataTypes, componentInstance, prop);
						props.put(prop.getName(), convertedValue);
					}
				}
				if (props != null && !props.isEmpty()) {
					nodeTemplate.setProperties(props);
				}
			}
			node_templates.put(componentInstance.getName(), nodeTemplate);
		}
		log.debug("finish convert topology template for {} for type {}", component.getUniqueId(), component.getComponentType());
		return Either.left(node_templates);
	}

	private Object convertInstanceProperty(Map<String, DataTypeDefinition> dataTypes, ComponentInstance componentInstance, ComponentInstanceProperty prop) {
		log.debug("Convert property {} for instance {}", prop.getName(), componentInstance.getUniqueId());
		String propertyType = prop.getType();
		String innerType = null;
		if (prop.getSchema() != null && prop.getSchema().getProperty() != null) {
			innerType = prop.getSchema().getProperty().getType();
		}
		Object convertedValue = propertyConvertor.convertToToscaObject(propertyType, prop.getValue(), innerType, dataTypes);
		return convertedValue;
	}

	private ToscaGroupTemplate convertGroup(GroupDefinition group, Component component) {
		ToscaGroupTemplate toscaGroup = new ToscaGroupTemplate();
		Map<String, String> members = group.getMembers();
		toscaGroup.setType(group.getType());
		if (members != null)
			toscaGroup.setMembers(new ArrayList(members.keySet()));

		boolean isVfModule = group.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE) ? true : false;
		IToscaMetadata toscaMetadata;
		if (!isVfModule) {
			toscaMetadata = new ToscaMetadata();
		} else {
			toscaMetadata = new VfModuleToscaMetadata();
			Map<String, Object> properties = new HashMap<>();

			for (GroupProperty gp : group.getProperties()) {
				if (gp.getName().equals(Constants.IS_BASE)) {
					Boolean isBase = Boolean.parseBoolean(gp.getValue());
					String type = isBase ? VF_MODULE_TYPE_BASE : VF_MODULE_TYPE_EXPANSION;
					properties.put(VF_MODULE_TYPE_KEY, type);
					break;
				}
			}
			properties.put(VF_MODULE_DESC_KEY, group.getDescription());
			boolean isVolume = false;
			List<String> artifactsList = group.getArtifacts();
			if (artifactsList != null && !artifactsList.isEmpty()) {

				for (String artifactId : artifactsList) {
					Map<String, ArtifactDefinition> deploymentArtifacts = component.getDeploymentArtifacts();
					Optional<ArtifactDefinition> findFirst = deploymentArtifacts.values().stream().filter(p -> p.getUniqueId().equals(artifactId)).findFirst();
					if (findFirst.isPresent()) {
						ArtifactDefinition artifactDefinition = findFirst.get();
						if (artifactDefinition.getArtifactType().equalsIgnoreCase(ArtifactTypeEnum.HEAT_VOL.getType())) {
							isVolume = true;
							break;
						}
					}
				}
			}
			properties.put(VOLUME_GROUP_KEY, isVolume);
			toscaGroup.setProperties(properties);
		}
		toscaMetadata.setName(group.getName());
		toscaMetadata.setInvariantUUID(group.getInvariantUUID());
		toscaMetadata.setUUID(group.getGroupUUID());
		toscaMetadata.setVersion(group.getVersion());
		toscaGroup.setMetadata(toscaMetadata);
		return toscaGroup;
	}

	private ToscaNodeType createNodeType(Component component) {
		ToscaNodeType toscaNodeType = new ToscaNodeType();
		if (ToscaUtils.isNodeType(component) && ((Resource) component).getDerivedFrom() != null) {
			toscaNodeType.setDerived_from(((Resource) component).getDerivedFrom().get(0));
		}
		toscaNodeType.setDescription(component.getDescription()); // or name??
		return toscaNodeType;
	}

	private Either<ToscaNodeTemplate, ToscaError> convertComponentInstanceRequirements(Component component, ComponentInstance componentInstance, List<RequirementCapabilityRelDef> relations, ToscaNodeTemplate nodeTypeTemplate) {

		List<ComponentInstance> instancesList = component.getComponentInstances();
		List<Map<String, ToscaTemplateRequirement>> toscaRequirements = new ArrayList<>();
		Map<String, List<RequirementDefinition>> reqMap = componentInstance.getRequirements();

		relations.stream().filter(p -> componentInstance.getUniqueId().equals(p.getFromNode())).forEach(req -> {
			ComponentInstance toComponentInstance = instancesList.stream().filter(i -> req.getToNode().equals(i.getUniqueId())).findFirst().orElse(null);
			if (toComponentInstance == null) {
				log.debug("Faild to create relation between node {} to node {}", componentInstance.getName(), req.getToNode());
				return;

			}
			RequirementAndRelationshipPair reqAndRelationshopPair = req.getRelationships().get(0);
			ToscaTemplateRequirement toscaRequirement = new ToscaTemplateRequirement();
			toscaRequirement.setRelationship(reqAndRelationshopPair.getRelationship().getType());
			toscaRequirement.setNode(toComponentInstance.getName());
			Optional<RequirementDefinition> findAny = reqMap.values().stream().flatMap(e -> e.stream()).filter(e -> e.getName().equals(reqAndRelationshopPair.getRequirement())).findAny();
			if (findAny.isPresent()) {
				RequirementDefinition regDefinition = findAny.get();
				toscaRequirement.setCapability(regDefinition.getCapability());
			} else {
				log.debug("Faild to find relation between node {} to node {}", componentInstance.getName(), req.getToNode());
				return;
			}
			Map<String, ToscaTemplateRequirement> reqmap = new HashMap<String, ToscaTemplateRequirement>();
			reqmap.put(reqAndRelationshopPair.getRequirement(), toscaRequirement);
			toscaRequirements.add(reqmap);

		});

		if (!toscaRequirements.isEmpty()) {
			nodeTypeTemplate.setRequirements(toscaRequirements);
		}
		log.debug("Finish convert Requirements for node type");
		return Either.left(nodeTypeTemplate);
	}

	private Either<SubstitutionMapping, ToscaError> convertCapabilities(Component component, SubstitutionMapping substitutionMapping, Map<String, DataTypeDefinition> dataTypes) {
		Map<String, ToscaCapability> toscaCapabilities = capabiltyRequirementConvertor.convertCapabilities(component, dataTypes);
		if (!toscaCapabilities.isEmpty()) {
			substitutionMapping.setCapabilities(toscaCapabilities);
		}
		log.debug("Finish convert Capabilities for node type");

		return Either.left(substitutionMapping);
	}

	private Either<ToscaNodeType, ToscaError> convertCapabilities(Component component, ToscaNodeType nodeType, Map<String, DataTypeDefinition> dataTypes) {
		Map<String, ToscaCapability> toscaCapabilities = capabiltyRequirementConvertor.convertCapabilities(component, dataTypes);
		if (!toscaCapabilities.isEmpty()) {
			nodeType.setCapabilities(toscaCapabilities);
		}
		log.debug("Finish convert Capabilities for node type");

		return Either.left(nodeType);
	}

	private static class CustomRepresenter extends Representer {
		public CustomRepresenter() {
			super();
			// null representer is exceptional and it is stored as an instance
			// variable.
			this.nullRepresenter = new RepresentNull();

		}

		@Override
		protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
			if (propertyValue == null) {
				return null;
			} else {
				// skip not relevant for Tosca property
				if (property.getName().equals("dependencies")) {
					return null;
				}
				NodeTuple defaultNode = super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);

				return property.getName().equals("_defaultp_") ? new NodeTuple(representData("default"), defaultNode.getValueNode()) : defaultNode;
			}
		}

		@Override
		protected MappingNode representJavaBean(Set<Property> properties, Object javaBean) {
			// remove the bean type from the output yaml (!! ...)
			if (!classTags.containsKey(javaBean.getClass()))
				addClassTag(javaBean.getClass(), Tag.MAP);

			return super.representJavaBean(properties, javaBean);
		}

		private class RepresentNull implements Represent {
			public Node representData(Object data) {
				// possible values are here http://yaml.org/type/null.html
				return representScalar(Tag.NULL, "");
			}
		}
	}

	private static class UnsortedPropertyUtils extends PropertyUtils {
		@Override
		protected Set<Property> createPropertySet(Class<? extends Object> type, BeanAccess bAccess) throws IntrospectionException {
			Collection<Property> fields = getPropertiesMap(type, BeanAccess.FIELD).values();
			Set<Property> result = new LinkedHashSet<Property>(fields);
			return result;
		}
	}

}
