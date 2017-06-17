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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementAndRelationshipPair;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.tosca.converters.ToscaValueBaseConverter;
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
	private ToscaOperationFacade toscaOperationFacade;

	private CapabiltyRequirementConvertor capabiltyRequirementConvertor = CapabiltyRequirementConvertor.getInstance();
	private PropertyConvertor propertyConvertor = PropertyConvertor.getInstance();

	
	private static Logger log = LoggerFactory.getLogger(ToscaExportHandler.class.getName());

	public static final String TOSCA_VERSION = "tosca_simple_yaml_1_0";
	public static final String SERVICE_NODE_TYPE_PREFIX = "org.openecomp.service.";
	public static final String IMPORTS_FILE_KEY = "file";
	public static final String TOSCA_TEMPLATE_NAME = "-template.yml";
	public static final String TOSCA_INTERFACE_NAME = "-interface.yml";
	public static final String ASSET_TOSCA_TEMPLATE = "assettoscatemplate";
	public static final String VF_MODULE_TYPE_KEY = "vf_module_type";
	public static final String VF_MODULE_DESC_KEY = "vf_module_description";
	public static final String VOLUME_GROUP_KEY = "volume_group";
	public static final String VF_MODULE_TYPE_BASE = "Base";
	public static final String VF_MODULE_TYPE_EXPANSION = "Expansion";
	public static final List<Map<String, Map<String, String>>> DEFAULT_IMPORTS = ConfigurationManager.getConfigurationManager().getConfiguration().getDefaultImports();
	
	
	
	public Either<ToscaRepresentation, ToscaError> exportComponent(Component component) {

		Either<ToscaTemplate, ToscaError> toscaTemplateRes = convertToToscaTemplate(component);
		if (toscaTemplateRes.isRight()) {
			return Either.right(toscaTemplateRes.right().value());
		}
		
		ToscaTemplate toscaTemplate = toscaTemplateRes.left().value();
		ToscaRepresentation toscaRepresentation = this.createToscaRepresentation(toscaTemplate);
		return Either.left(toscaRepresentation);
	}

	public Either<ToscaRepresentation, ToscaError> exportComponentInterface(Component component) {
		if(null == DEFAULT_IMPORTS) {
			log.debug("convertToToscaTemplate - failed to get Default Imports section from configuration");
			return Either.right(ToscaError.GENERAL_ERROR);
		}
		
		ToscaTemplate toscaTemplate = new ToscaTemplate(TOSCA_VERSION);
		toscaTemplate.setImports(new ArrayList<>(DEFAULT_IMPORTS));
		Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
		Either<ToscaTemplate, ToscaError> toscaTemplateRes = convertInterfaceNodeType(component, toscaTemplate, nodeTypes);
		if (toscaTemplateRes.isRight()) {
			return Either.right(toscaTemplateRes.right().value());
		}	
		
		toscaTemplate = toscaTemplateRes.left().value();
		ToscaRepresentation toscaRepresentation = this.createToscaRepresentation(toscaTemplate);
		return Either.left(toscaRepresentation);
	}

	public ToscaRepresentation createToscaRepresentation(ToscaTemplate toscaTemplate) {
		CustomRepresenter representer = new CustomRepresenter();
		DumperOptions options = new DumperOptions();
		options.setAllowReadOnlyProperties(false);
		options.setPrettyFlow(true);

		options.setDefaultFlowStyle(FlowStyle.FLOW);
		options.setCanonical(false);

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
		
		return toscaRepresentation;
	}
	
	public Either<ToscaTemplate, ToscaError> getDependencies(Component component) {
		ToscaTemplate toscaTemplate = new ToscaTemplate(null);
		Either<ImmutablePair<ToscaTemplate, Map<String, Component>>, ToscaError> fillImports = fillImports(component,
				toscaTemplate);
		if (fillImports.isRight()) {
			return Either.right(fillImports.right().value());
		}
		return Either.left(fillImports.left().value().left);
	}

	private Either<ToscaTemplate, ToscaError> convertToToscaTemplate(Component component) {
		if(null == DEFAULT_IMPORTS) {
			log.debug("convertToToscaTemplate - failed to get Default Imports section from configuration");
			return Either.right(ToscaError.GENERAL_ERROR);
		}
		
		log.trace("start tosca export for {}", component.getUniqueId());
		ToscaTemplate toscaTemplate = new ToscaTemplate(TOSCA_VERSION);

		toscaTemplate.setMetadata(convertMetadata(component));
		toscaTemplate.setImports(new ArrayList<>(DEFAULT_IMPORTS));
		Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
		if (ToscaUtils.isAtomicType(component)) {
			log.trace("convert component as node type");
			return convertNodeType(component, toscaTemplate, nodeTypes);
		} else {
			log.trace("convert component as topology template");
			return convertToscaTemplate(component, toscaTemplate);
		}

	}

	private Either<ToscaTemplate, ToscaError> convertToscaTemplate(Component component, ToscaTemplate toscaNode) {

		Either<ImmutablePair<ToscaTemplate, Map<String, Component>>, ToscaError> importsRes = fillImports(component,
				toscaNode);
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

		ToscaTopolgyTemplate topologyTemplate = new ToscaTopolgyTemplate();

		Either<ToscaTopolgyTemplate, ToscaError> inputs = fillInputs(component, topologyTemplate, dataTypes);
		if (inputs.isRight()) {
			return Either.right(inputs.right().value());
		}
		topologyTemplate = inputs.left().value();

		List<ComponentInstance> componentInstances = component.getComponentInstances();
		Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = component
				.getComponentInstancesProperties();
		List<GroupDefinition> groups = component.getGroups();
		if (componentInstances != null && !componentInstances.isEmpty()) {

			Either<Map<String, ToscaNodeTemplate>, ToscaError> nodeTemplates = convertNodeTemplates(component,
					componentInstances, componentInstancesProperties, componentCache, dataTypes, topologyTemplate);
			if (nodeTemplates.isRight()) {
				return Either.right(nodeTemplates.right().value());
			}
			log.debug("node templates converted");

			topologyTemplate.setNode_templates(nodeTemplates.left().value());
		}
		Map<String, ToscaGroupTemplate> groupsMap = null;
		if (groups != null && !groups.isEmpty()) {
			groupsMap = new HashMap<String, ToscaGroupTemplate>();
			for (GroupDefinition group : groups) {
				ToscaGroupTemplate toscaGroup = convertGroup(group);
				groupsMap.put(group.getName(), toscaGroup);

			}
			log.debug("groups converted");
			topologyTemplate.addGroups(groupsMap);
		}
		SubstitutionMapping substitutionMapping = new SubstitutionMapping();
		String toscaResourceName = null;
		switch (component.getComponentType()) {
		case RESOURCE:
			toscaResourceName = ((ResourceMetadataDataDefinition) component.getComponentMetadataDefinition()
					.getMetadataDataDefinition()).getToscaResourceName();
			break;
		case SERVICE:
			toscaResourceName = SERVICE_NODE_TYPE_PREFIX
					+ component.getComponentMetadataDefinition().getMetadataDataDefinition().getSystemName();
			break;
		default:
			log.debug("Not supported component type {}", component.getComponentType());
			return Either.right(ToscaError.NOT_SUPPORTED_TOSCA_TYPE);
		}
		substitutionMapping.setNode_type(toscaResourceName);

		Either<SubstitutionMapping, ToscaError> capabilities = convertCapabilities(component, substitutionMapping,
				dataTypes);
		if (capabilities.isRight()) {
			return Either.right(capabilities.right().value());
		}
		substitutionMapping = capabilities.left().value();

		Either<SubstitutionMapping, ToscaError> requirements = capabiltyRequirementConvertor.convertSubstitutionMappingRequirements(component, substitutionMapping);
		if (requirements.isRight()) {
			return Either.right(requirements.right().value());
		}
		substitutionMapping = requirements.left().value();

		topologyTemplate.setSubstitution_mappings(substitutionMapping);

		toscaNode.setTopology_template(topologyTemplate);
		return Either.left(toscaNode);
	}

	private Either<ToscaTopolgyTemplate, ToscaError> fillInputs(Component component,
			ToscaTopolgyTemplate topologyTemplate, Map<String, DataTypeDefinition> dataTypes) {
		if (log.isDebugEnabled())
			log.debug("fillInputs for component {}", component.getUniqueId());
		List<InputDefinition> inputDef = component.getInputs();
		Map<String, ToscaProperty> inputs = new HashMap<>();

		if (inputDef != null) {
			inputDef.forEach(i -> {
				ToscaProperty property = propertyConvertor.convertProperty(dataTypes, i, false);
				inputs.put(i.getName(), property);
			});
			if (!inputs.isEmpty()) {
				topologyTemplate.setInputs(inputs);
			}
		}
		return Either.left(topologyTemplate);
	}

	private ToscaMetadata convertMetadata(Component component) {
		return convertMetadata(component, false, null);
	}

	private ToscaMetadata convertMetadata(Component component, boolean isInstance,
			ComponentInstance componentInstance) {
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
			toscaMetadata.setCustomizationUUID(componentInstance.getCustomizationUUID());
		}
		switch (component.getComponentType()) {
		case RESOURCE:
			Resource resource = (Resource) component;
			toscaMetadata.setType(resource.getResourceType().name());
			toscaMetadata.setSubcategory(categoryDefinition.getSubcategories().get(0).getName());
			toscaMetadata.setResourceVendor(resource.getVendorName());
			toscaMetadata.setResourceVendorRelease(resource.getVendorRelease());
			
			break;
		case SERVICE:
			toscaMetadata.setType(component.getComponentType().getValue());
			if (!isInstance) {
				// DE268546	
				toscaMetadata.setServiceEcompNaming(((Service)component).isEcompGeneratedNaming());
				toscaMetadata.setEcompGeneratedNaming(((Service)component).isEcompGeneratedNaming());
				toscaMetadata.setNamingPolicy(((Service)component).getNamingPolicy());				
			}
			break;
		default:
			log.debug("Not supported component type {}", component.getComponentType());
		}
		return toscaMetadata;
	}

	private Either<ImmutablePair<ToscaTemplate, Map<String, Component>>, ToscaError> fillImports(Component component,
			ToscaTemplate toscaTemplate) {
		
		if(null == DEFAULT_IMPORTS) {
			log.debug("convertToToscaTemplate - failed to get Default Imports section from configuration");
			return Either.right(ToscaError.GENERAL_ERROR);
		}
		
		Map<String, Component> componentCache = new HashMap<>();

		if (!ToscaUtils.isAtomicType(component)) {
			List<ComponentInstance> componentInstances = component.getComponentInstances();
			if (componentInstances != null && !componentInstances.isEmpty()) {
				
				List<Map<String, Map<String, String>>> additionalImports = 
						toscaTemplate.getImports() == null ? new ArrayList<>(DEFAULT_IMPORTS) : new ArrayList<>(toscaTemplate.getImports());
				
				List<Triple<String, String, Component>> dependecies = new ArrayList<>();

				Map<String, ArtifactDefinition> toscaArtifacts = component.getToscaArtifacts();
				ArtifactDefinition artifactDefinition = toscaArtifacts.get(ToscaExportHandler.ASSET_TOSCA_TEMPLATE);
				
				Map<String, Map<String, String>> importsListMember = new HashMap<>();
				Map<String, String> interfaceFiles = new HashMap<>();
				interfaceFiles.put(IMPORTS_FILE_KEY, getInterfaceFilename(artifactDefinition.getArtifactName()));
				StringBuilder keyNameBuilder = new StringBuilder();
				keyNameBuilder.append(component.getComponentType().toString().toLowerCase());
				keyNameBuilder.append("-");
				keyNameBuilder.append(component.getName());
				keyNameBuilder.append("-interface");
				importsListMember.put(keyNameBuilder.toString(), interfaceFiles);
				additionalImports.add(importsListMember);
				
				componentInstances.forEach(ci -> {
					createDependency(componentCache, additionalImports, dependecies, ci);
				});
				toscaTemplate.setDependencies(dependecies);
				toscaTemplate.setImports(additionalImports);
			}
		} else {
			log.debug("currently imports supported for VF and service only");
		}	
		return Either.left(new ImmutablePair<ToscaTemplate, Map<String, Component>>(toscaTemplate, componentCache));
	}

	private void createDependency(Map<String, Component> componentCache, List<Map<String, Map<String, String>>> imports,
			List<Triple<String, String, Component>> dependecies, ComponentInstance ci) {
		Map<String, String> files = new HashMap<>();
		Map<String, Map<String, String>> importsListMember = new HashMap<>();
		StringBuilder keyNameBuilder;

		Component componentRI = componentCache.get(ci.getComponentUid());
		if (componentRI == null) {
			// all resource must be only once!
			Either<Component, StorageOperationStatus> resource = toscaOperationFacade.getToscaFullElement(ci.getComponentUid());
			if (resource.isRight()) {
				log.debug("Failed to fetch resource with id {} for instance {}");
			}
			Component fetchedComponent = resource.left().value();
			componentCache.put(fetchedComponent.getUniqueId(), fetchedComponent);
			componentRI = fetchedComponent;

			Map<String, ArtifactDefinition> toscaArtifacts = componentRI.getToscaArtifacts();
			ArtifactDefinition artifactDefinition = toscaArtifacts.get(ASSET_TOSCA_TEMPLATE);
			if (artifactDefinition != null) {
				String artifactName = artifactDefinition.getArtifactName();
				files.put(IMPORTS_FILE_KEY, artifactName);
				keyNameBuilder = new StringBuilder();
				keyNameBuilder.append(fetchedComponent.getComponentType().toString().toLowerCase());
				keyNameBuilder.append("-");
				keyNameBuilder.append(ci.getComponentName());
				importsListMember.put(keyNameBuilder.toString(), files);
				imports.add(importsListMember);
				dependecies.add(new ImmutableTriple<String, String, Component>(artifactName,
						artifactDefinition.getEsId(), fetchedComponent));
				
				if(!ToscaUtils.isAtomicType(componentRI)) {
					importsListMember = new HashMap<>();
					Map<String, String> interfaceFiles = new HashMap<>();
					interfaceFiles.put(IMPORTS_FILE_KEY, getInterfaceFilename(artifactName));
					keyNameBuilder.append("-interface");
					importsListMember.put(keyNameBuilder.toString(), interfaceFiles);
					imports.add(importsListMember);
				}
			}
		}
	}

	public static String getInterfaceFilename(String artifactName) {
		String interfaceFileName = artifactName.substring(0, artifactName.lastIndexOf('.')) + ToscaExportHandler.TOSCA_INTERFACE_NAME;
		return interfaceFileName;
	}

	private Either<ToscaTemplate, ToscaError> convertNodeType(Component component, ToscaTemplate toscaNode,
			Map<String, ToscaNodeType> nodeTypes) {
		log.debug("start convert node type for {}", component.getUniqueId());
		ToscaNodeType toscaNodeType = createNodeType(component);

		Either<Map<String, DataTypeDefinition>, TitanOperationStatus> dataTypesEither = dataTypeCache.getAll();
		if (dataTypesEither.isRight()) {
			log.debug("Failed to fetch all data types :", dataTypesEither.right().value());
			return Either.right(ToscaError.GENERAL_ERROR);
		}

		Map<String, DataTypeDefinition> dataTypes = dataTypesEither.left().value();
		Either<ToscaNodeType, ToscaError> properties = propertyConvertor.convertProperties(component, toscaNodeType,
				dataTypes);
		if (properties.isRight()) {
			return Either.right(properties.right().value());
		}
		toscaNodeType = properties.left().value();
		log.debug("Properties converted for {}", component.getUniqueId());

		//Extracted to method for code reuse
		return convertReqCapAndTypeName(component, toscaNode, nodeTypes, toscaNodeType, dataTypes);
	}
	
	private Either<ToscaTemplate, ToscaError> convertInterfaceNodeType(Component component, ToscaTemplate toscaNode,
			Map<String, ToscaNodeType> nodeTypes) {
		log.debug("start convert node type for {}", component.getUniqueId());
		ToscaNodeType toscaNodeType = createNodeType(component);

		Either<Map<String, DataTypeDefinition>, TitanOperationStatus> dataTypesEither = dataTypeCache.getAll();
		if (dataTypesEither.isRight()) {
			log.debug("Failed to fetch all data types :", dataTypesEither.right().value());
			return Either.right(ToscaError.GENERAL_ERROR);
		}

		Map<String, DataTypeDefinition> dataTypes = dataTypesEither.left().value();
		
		List<InputDefinition> inputDef = component.getInputs();
		Map<String, ToscaProperty> inputs = new HashMap<>();

		if (inputDef != null) {
			inputDef.forEach(i -> {
				ToscaProperty property = propertyConvertor.convertProperty(dataTypes, i, false);
				inputs.put(i.getName(), property);
			});
			if (!inputs.isEmpty()) {
				toscaNodeType.setProperties(inputs);
			}
		}

		//Extracted to method for code reuse
		return convertReqCapAndTypeName(component, toscaNode, nodeTypes, toscaNodeType, dataTypes);
	}

	private Either<ToscaTemplate, ToscaError> convertReqCapAndTypeName(Component component, ToscaTemplate toscaNode,
			Map<String, ToscaNodeType> nodeTypes, ToscaNodeType toscaNodeType,
			Map<String, DataTypeDefinition> dataTypes) {
		Either<ToscaNodeType, ToscaError> capabilities = convertCapabilities(component, toscaNodeType, dataTypes);
		if (capabilities.isRight()) {
			return Either.right(capabilities.right().value());
		}
		toscaNodeType = capabilities.left().value();
		log.debug("Capabilities converted for {}", component.getUniqueId());

		Either<ToscaNodeType, ToscaError> requirements = capabiltyRequirementConvertor.convertRequirements(component,
				toscaNodeType);
		if (requirements.isRight()) {
			return Either.right(requirements.right().value());
		}
		toscaNodeType = requirements.left().value();
		log.debug("Requirements converted for {}", component.getUniqueId());

		
		String toscaResourceName;
		switch (component.getComponentType()) {
		case RESOURCE:
			toscaResourceName = ((ResourceMetadataDataDefinition) component.getComponentMetadataDefinition()
					.getMetadataDataDefinition()).getToscaResourceName();
			break;
		case SERVICE:
			toscaResourceName = SERVICE_NODE_TYPE_PREFIX
					+ component.getComponentMetadataDefinition().getMetadataDataDefinition().getSystemName();
			break;
		default:
			log.debug("Not supported component type {}", component.getComponentType());
			return Either.right(ToscaError.NOT_SUPPORTED_TOSCA_TYPE);
		}

		nodeTypes.put(toscaResourceName, toscaNodeType);
		toscaNode.setNode_types(nodeTypes);
		log.debug("finish convert node type for {}", component.getUniqueId());
		return Either.left(toscaNode);
	}

	private Either<Map<String, ToscaNodeTemplate>, ToscaError> convertNodeTemplates(Component component,
			List<ComponentInstance> componentInstances,
			Map<String, List<ComponentInstanceProperty>> componentInstancesProperties,
			Map<String, Component> componentCache, Map<String, DataTypeDefinition> dataTypes,
			ToscaTopolgyTemplate topologyTemplate) {

		Either<Map<String, ToscaNodeTemplate>, ToscaError> convertNodeTemplatesRes = null;
		log.debug("start convert topology template for {} for type {}", component.getUniqueId(),
				component.getComponentType());
		Map<String, ToscaNodeTemplate> nodeTemplates = new HashMap<>();
		Map<String, List<ComponentInstanceInput>> componentInstancesInputs = component.getComponentInstancesInputs();

		Map<String, ToscaGroupTemplate> groupsMap = null;
		for (ComponentInstance componentInstance : componentInstances) {
			ToscaNodeTemplate nodeTemplate = new ToscaNodeTemplate();
			nodeTemplate.setType(componentInstance.getToscaComponentName());

			Either<ToscaNodeTemplate, ToscaError> requirements = convertComponentInstanceRequirements(component,
					componentInstance, component.getComponentInstancesRelations(), nodeTemplate);
			if (requirements.isRight()) {
				convertNodeTemplatesRes = Either.right(requirements.right().value());
				break;
			}
			String instanceUniqueId = componentInstance.getUniqueId();
			log.debug("Component instance Requirements converted for instance {}", instanceUniqueId);

			nodeTemplate = requirements.left().value();

			Component componentOfInstance = componentCache.get(componentInstance.getComponentUid());
			nodeTemplate.setMetadata(convertMetadata(componentOfInstance, true, componentInstance));

			Either<ToscaNodeTemplate, ToscaError> capabilties = capabiltyRequirementConvertor
					.convertComponentInstanceCapabilties(componentInstance, dataTypes, nodeTemplate);
			if (capabilties.isRight()) {
				convertNodeTemplatesRes = Either.right(requirements.right().value());
				break;
			}
			log.debug("Component instance Capabilties converted for instance {}", instanceUniqueId);

			nodeTemplate = capabilties.left().value();
			Map<String, Object> props = new HashMap<>();

			if (componentOfInstance.getComponentType() == ComponentTypeEnum.RESOURCE) {
				// Adds the properties of parent component to map
				addPropertiesOfParentComponent(dataTypes, componentInstance, componentOfInstance, props);
			}

			if (null != componentInstancesProperties && componentInstancesProperties.containsKey(instanceUniqueId)) {
				addPropertiesOfComponentInstance(componentInstancesProperties, dataTypes, componentInstance,
						instanceUniqueId, props);
			}

			if (componentInstancesInputs != null && componentInstancesInputs.containsKey(instanceUniqueId)) {
				addComponentInstanceInputs(dataTypes, componentInstancesInputs, componentInstance, instanceUniqueId,
						props);
			}
			if (props != null && !props.isEmpty()) {
				nodeTemplate.setProperties(props);
			}

			List<GroupInstance> groupInstances = componentInstance.getGroupInstances();
			if (groupInstances != null) {
				if (groupsMap == null) {
					groupsMap = new HashMap<>();
				}
				for (GroupInstance groupInst : groupInstances) {
					ToscaGroupTemplate toscaGroup = convertGroupInstance(groupInst);

					groupsMap.put(groupInst.getName(), toscaGroup);
				}
			}

			nodeTemplates.put(componentInstance.getName(), nodeTemplate);
		}
		if (groupsMap != null) {
			log.debug("instance groups added");
			topologyTemplate.addGroups(groupsMap);
		}

		if (convertNodeTemplatesRes == null) {
			convertNodeTemplatesRes = Either.left(nodeTemplates);
		}
		log.debug("finish convert topology template for {} for type {}", component.getUniqueId(),
				component.getComponentType());
		return convertNodeTemplatesRes;
	}

	private void addComponentInstanceInputs(Map<String, DataTypeDefinition> dataTypes,
			Map<String, List<ComponentInstanceInput>> componentInstancesInputs, ComponentInstance componentInstance,
			String instanceUniqueId, Map<String, Object> props) {

		List<ComponentInstanceInput> instanceInputsList = componentInstancesInputs.get(instanceUniqueId);
		if (instanceInputsList != null) {
			instanceInputsList.forEach(input -> {
				Supplier<String> supplier = () -> input.getValue();
				convertAndAddValue(dataTypes, componentInstance, props, input, supplier);
			});
		}
	}

	private void addPropertiesOfComponentInstance(
			Map<String, List<ComponentInstanceProperty>> componentInstancesProperties,
			Map<String, DataTypeDefinition> dataTypes, ComponentInstance componentInstance, String instanceUniqueId,
			Map<String, Object> props) {

		if (!MapUtils.isEmpty(componentInstancesProperties)) {
			componentInstancesProperties.get(instanceUniqueId).stream()
					// Filters out properties with empty ValueUniqueUid
					.filter(e -> e.getValue() != null && !e.getValue().isEmpty() )
					// Collects filtered properties to List
					.collect(Collectors.toList()).stream()
					// Converts and adds each value to property map
					.forEach(prop -> convertAndAddValue(dataTypes, componentInstance, props, prop,
							() -> prop.getValue()));
		}
	}

	private void addPropertiesOfParentComponent(Map<String, DataTypeDefinition> dataTypes,
			ComponentInstance componentInstance, Component componentOfInstance, Map<String, Object> props) {

		List<PropertyDefinition> componentProperties = ((Resource) componentOfInstance).getProperties();
		if (!CollectionUtils.isEmpty(componentProperties)) {
			componentProperties.stream()
					// Filters out properties with empty default values
					.filter(prop -> !StringUtils.isEmpty(prop.getDefaultValue()))
					// Collects filtered properties to List
					.collect(Collectors.toList()).stream()
					// Converts and adds each value to property map
					.forEach(prop -> convertAndAddValue(dataTypes, componentInstance, props, prop,
							() -> prop.getDefaultValue()));
		}
	}

	/**
	 * @param dataTypes
	 * @param componentInstance
	 * @param props
	 * @param prop
	 * @param supplier
	 */
	private void convertAndAddValue(Map<String, DataTypeDefinition> dataTypes, ComponentInstance componentInstance,
			Map<String, Object> props, PropertyDefinition prop, Supplier<String> supplier) {
		Object convertedValue = convertValue(dataTypes, componentInstance, prop, supplier);
		if (!ToscaValueBaseConverter.isEmptyObjectValue(convertedValue)) {
			props.put(prop.getName(), convertedValue);
		}
	}

	private <T extends PropertyDefinition> Object convertValue(Map<String, DataTypeDefinition> dataTypes,
			ComponentInstance componentInstance, T input, Supplier<String> supplier) {
		log.debug("Convert property or input value {} for instance {}", input.getName(),
				componentInstance.getUniqueId());
		String propertyType = input.getType();
		String innerType = null;
		if (input.getSchema() != null && input.getSchema().getProperty() != null) {
			innerType = input.getSchema().getProperty().getType();
		}
		return propertyConvertor.convertToToscaObject(propertyType, supplier.get(), innerType, dataTypes);
	}

	private ToscaGroupTemplate convertGroup(GroupDefinition group) {
		ToscaGroupTemplate toscaGroup = new ToscaGroupTemplate();
		Map<String, String> members = group.getMembers();
		if (members != null)
			toscaGroup.setMembers(new ArrayList<String>(members.keySet()));

		Supplier<String> supplGroupType = () -> group.getType();
		Supplier<String> supplDescription = () -> group.getDescription();
		Supplier<List<? extends GroupProperty>> supplProperties = () -> group.convertToGroupProperties();
		Supplier<String> supplgroupName = () -> group.getName();
		Supplier<String> supplInvariantUUID = () -> group.getInvariantUUID();
		Supplier<String> supplGroupUUID = () -> group.getGroupUUID();
		Supplier<String> supplVersion = () -> group.getVersion();

		IToscaMetadata toscaMetadata = fillGroup(toscaGroup, supplProperties, supplDescription, supplgroupName,
				supplInvariantUUID, supplGroupUUID, supplVersion, supplGroupType);
		toscaGroup.setMetadata(toscaMetadata);
		return toscaGroup;
	}

	private ToscaGroupTemplate convertGroupInstance(GroupInstance groupInstance) {
		ToscaGroupTemplate toscaGroup = new ToscaGroupTemplate();

		Supplier<String> supplGroupType = () -> groupInstance.getType();
		Supplier<String> supplDescription = () -> groupInstance.getDescription();
		Supplier<List<? extends GroupProperty>> supplProperties = () -> groupInstance.convertToGroupInstancesProperties();
		Supplier<String> supplgroupName = () -> groupInstance.getGroupName();
		Supplier<String> supplInvariantUUID = () -> groupInstance.getInvariantUUID();
		Supplier<String> supplGroupUUID = () -> groupInstance.getGroupUUID();
		Supplier<String> supplVersion = () -> groupInstance.getVersion();

		IToscaMetadata toscaMetadata = fillGroup(toscaGroup, supplProperties, supplDescription, supplgroupName,
				supplInvariantUUID, supplGroupUUID, supplVersion, supplGroupType);

		toscaMetadata.setCustomizationUUID(groupInstance.getCustomizationUUID());
		toscaGroup.setMetadata(toscaMetadata);
		return toscaGroup;
	}

	private IToscaMetadata fillGroup(ToscaGroupTemplate toscaGroup, Supplier<List<? extends GroupProperty>> props,
			Supplier<String> description, Supplier<String> groupName, Supplier<String> invariantUUID,
			Supplier<String> groupUUID, Supplier<String> version, Supplier<String> groupType) {
		boolean isVfModule = groupType.get().equals(Constants.DEFAULT_GROUP_VF_MODULE) ? true : false;
		toscaGroup.setType(groupType.get());

		IToscaMetadata toscaMetadata;
		if (!isVfModule) {
			toscaMetadata = new ToscaMetadata();
		} else {
			toscaMetadata = new VfModuleToscaMetadata();

			Map<String, Object> properties = fillGroupProperties(props.get());

			properties.put(VF_MODULE_DESC_KEY, description.get());
			toscaGroup.setProperties(properties);
		}
		toscaMetadata.setName(groupName.get());
		toscaMetadata.setInvariantUUID(invariantUUID.get());
		toscaMetadata.setUUID(groupUUID.get());
		toscaMetadata.setVersion(version.get());
		return toscaMetadata;
	}

	private Map<String, Object> fillGroupProperties(List<? extends GroupProperty> groupProps) {
		Map<String, Object> properties = new HashMap<>();
		if(groupProps != null){
			for (GroupProperty gp : groupProps) {
				if (gp.getName().equals(Constants.IS_BASE)) {
					Boolean isBase = Boolean.parseBoolean(gp.getValue());
					String type = isBase ? VF_MODULE_TYPE_BASE : VF_MODULE_TYPE_EXPANSION;
					properties.put(VF_MODULE_TYPE_KEY, type);
				} else {
					Object value = null;
					String type = gp.getType();
	
					switch (type) {
					case "integer":
						if (gp.getValue() != null) {
							value = Integer.valueOf(gp.getValue());
						}
						break;
					case "boolean":
						if (gp.getValue() != null) {
							value = Boolean.valueOf(gp.getValue());
						}
						break;
	
					default:
						value = gp.getValue();
						break;
					}
					properties.put(gp.getName(), value);
				}
			}
		}
		return properties;
	}

	private ToscaNodeType createNodeType(Component component) {
		ToscaNodeType toscaNodeType = new ToscaNodeType();
		if (ToscaUtils.isAtomicType(component)){
			if (((Resource) component).getDerivedFrom() != null){
				toscaNodeType.setDerived_from(((Resource) component).getDerivedFrom().get(0));
			}
			toscaNodeType.setDescription(component.getDescription()); // or name??
		} else {
			String derivedFrom = null != component.getDerivedFromGenericType()? component.getDerivedFromGenericType() : "tosca.nodes.Root"; 
			toscaNodeType.setDerived_from(derivedFrom);
		}
		return toscaNodeType;
	}
	
	private Either<ToscaNodeTemplate, ToscaError> convertComponentInstanceRequirements(Component component,
			ComponentInstance componentInstance, List<RequirementCapabilityRelDef> relations,
			ToscaNodeTemplate nodeTypeTemplate) {

		List<ComponentInstance> instancesList = component.getComponentInstances();
		List<Map<String, ToscaTemplateRequirement>> toscaRequirements = new ArrayList<>();
		Map<String, List<RequirementDefinition>> reqMap = componentInstance.getRequirements();

		relations.stream().filter(p -> componentInstance.getUniqueId().equals(p.getFromNode())).forEach(req -> {
			ComponentInstance toComponentInstance = instancesList.stream()
					.filter(i -> req.getToNode().equals(i.getUniqueId())).findFirst().orElse(null);
			if (toComponentInstance == null) {
				log.debug("Faild to create relation between node {} to node {}", componentInstance.getName(),
						req.getToNode());
				return;

			}
			RequirementAndRelationshipPair reqAndRelationshopPair = req.getRelationships().get(0);
			ToscaTemplateRequirement toscaRequirement = new ToscaTemplateRequirement();
			toscaRequirement.setRelationship(reqAndRelationshopPair.getRelationship().getType());
			toscaRequirement.setNode(toComponentInstance.getName());
			Optional<RequirementDefinition> findAny = reqMap.values().stream().flatMap(e -> e.stream())
					.filter(e -> e.getName().equals(reqAndRelationshopPair.getRequirement())).findAny();
			if (findAny.isPresent()) {
				RequirementDefinition regDefinition = findAny.get();
				toscaRequirement.setCapability(regDefinition.getCapability());
			} else {
				log.debug("Faild to find relation between node {} to node {}", componentInstance.getName(),
						req.getToNode());
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
		Map<String, String[]> toscaCapabilities = capabiltyRequirementConvertor.convertSubstitutionMappingCapabilities(component, dataTypes);
		
		if (!toscaCapabilities.isEmpty()) {
			substitutionMapping.setCapabilities(toscaCapabilities);
		}
		log.debug("Finish convert Capabilities for node type");

		return Either.left(substitutionMapping);
	}

	private Either<ToscaNodeType, ToscaError> convertCapabilities(Component component, ToscaNodeType nodeType,
			Map<String, DataTypeDefinition> dataTypes) {
		Map<String, ToscaCapability> toscaCapabilities = capabiltyRequirementConvertor.convertCapabilities(component,
				dataTypes);
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
		protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue,
				Tag customTag) {
			if (propertyValue == null) {
				return null;
			} else {
				// skip not relevant for Tosca property
				if (property.getName().equals("dependencies")) {
					return null;
				}
				NodeTuple defaultNode = super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);

				return property.getName().equals("_defaultp_")
						? new NodeTuple(representData("default"), defaultNode.getValueNode()) : defaultNode;
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
		protected Set<Property> createPropertySet(Class<? extends Object> type, BeanAccess bAccess)
				throws IntrospectionException {
			Collection<Property> fields = getPropertiesMap(type, BeanAccess.FIELD).values();
			return new LinkedHashSet<Property>(fields);
		}
	}

}
