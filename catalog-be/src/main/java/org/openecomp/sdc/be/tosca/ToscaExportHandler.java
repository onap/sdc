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
import java.util.Map.Entry;
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
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsontitan.utils.ModelConverter;
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
	@Autowired
	private CapabiltyRequirementConvertor capabiltyRequirementConvertor;
	private PropertyConvertor propertyConvertor = PropertyConvertor.getInstance();
	Map<String, Component> originComponents = new HashMap<>();

	private static Logger log = LoggerFactory.getLogger(ToscaExportHandler.class.getName());

	public static final String TOSCA_VERSION = "tosca_simple_yaml_1_1";
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
	private static final String FAILED_TO_GET_DEFAULT_IMPORTS_CONFIGURATION = "convertToToscaTemplate - failed to get Default Imports section from configuration";
	private static final String NOT_SUPPORTED_COMPONENT_TYPE = "Not supported component type {}";
	protected static final List<Map<String, Map<String, String>>> DEFAULT_IMPORTS = ConfigurationManager
			.getConfigurationManager().getConfiguration().getDefaultImports();

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
		if (null == DEFAULT_IMPORTS) {
			log.debug(FAILED_TO_GET_DEFAULT_IMPORTS_CONFIGURATION);
			return Either.right(ToscaError.GENERAL_ERROR);
		}

		ToscaTemplate toscaTemplate = new ToscaTemplate(TOSCA_VERSION);
		toscaTemplate.setImports(new ArrayList<>(DEFAULT_IMPORTS));
		Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
		Either<ToscaTemplate, ToscaError> toscaTemplateRes = convertInterfaceNodeType(component, toscaTemplate,
				nodeTypes);
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
		if (null == DEFAULT_IMPORTS) {
			log.debug(FAILED_TO_GET_DEFAULT_IMPORTS_CONFIGURATION);
			return Either.right(ToscaError.GENERAL_ERROR);
		}

		log.trace("start tosca export for {}", component.getUniqueId());
		ToscaTemplate toscaTemplate = new ToscaTemplate(TOSCA_VERSION);

		toscaTemplate.setMetadata(convertMetadata(component));
		toscaTemplate.setImports(new ArrayList<>(DEFAULT_IMPORTS));
		Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
		if (ModelConverter.isAtomicComponent(component)) {
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
		Either<Map<String, ToscaNodeType>, ToscaError> nodeTypesMapEither = createProxyNodeTypes(component);
		if (nodeTypesMapEither.isRight()) {
			log.debug("Failed to fetch normative service proxy resource by tosca name, error {}",
					nodeTypesMapEither.right().value());
			return Either.right(nodeTypesMapEither.right().value());
		}
		Map<String, ToscaNodeType> nodeTypesMap = nodeTypesMapEither.left().value();
		if (nodeTypesMap != null && !nodeTypesMap.isEmpty())
			toscaNode.setNode_types(nodeTypesMap);

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
		Map<String, ToscaGroupTemplate> groupsMap;
		if (groups != null && !groups.isEmpty()) {
			groupsMap = new HashMap<>();
			for (GroupDefinition group : groups) {
				boolean addToTosca = true;
				if (group.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE)) {
					List<String> artifacts = group.getArtifacts();
					if (artifacts == null || artifacts.isEmpty()) {
						addToTosca = false;
					}
				}
				if (addToTosca) {
					ToscaGroupTemplate toscaGroup = convertGroup(group);
					groupsMap.put(group.getName(), toscaGroup);
				}

			}
			log.debug("groups converted");
			topologyTemplate.addGroups(groupsMap);
		}
		SubstitutionMapping substitutionMapping = new SubstitutionMapping();
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
			log.debug(NOT_SUPPORTED_COMPONENT_TYPE, component.getComponentType());
			return Either.right(ToscaError.NOT_SUPPORTED_TOSCA_TYPE);
		}
		substitutionMapping.setNode_type(toscaResourceName);

		Either<SubstitutionMapping, ToscaError> capabilities = convertCapabilities(component, substitutionMapping);
		if (capabilities.isRight()) {
			return Either.right(capabilities.right().value());
		}
		substitutionMapping = capabilities.left().value();

		Either<SubstitutionMapping, ToscaError> requirements = capabiltyRequirementConvertor
				.convertSubstitutionMappingRequirements(originComponents, component, substitutionMapping);
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
		toscaMetadata.setInvariantUUID(component.getInvariantUUID());
		toscaMetadata.setUUID(component.getUUID());
		toscaMetadata.setDescription(component.getDescription());
		toscaMetadata.setName(component.getComponentMetadataDefinition().getMetadataDataDefinition().getName());

		List<CategoryDefinition> categories = component.getCategories();
		CategoryDefinition categoryDefinition = categories.get(0);
		toscaMetadata.setCategory(categoryDefinition.getName());

		if (isInstance) {
			toscaMetadata.setVersion(component.getVersion());
			toscaMetadata.setCustomizationUUID(componentInstance.getCustomizationUUID());
			if (componentInstance.getSourceModelInvariant() != null
					&& !componentInstance.getSourceModelInvariant().isEmpty()) {
				toscaMetadata.setVersion(componentInstance.getComponentVersion());
				toscaMetadata.setSourceModelInvariant(componentInstance.getSourceModelInvariant());
				toscaMetadata.setSourceModelUuid(componentInstance.getSourceModelUuid());
				toscaMetadata.setSourceModelName(componentInstance.getSourceModelName());
				toscaMetadata.setName(
						componentInstance.getSourceModelName() + " " + OriginTypeEnum.ServiceProxy.getDisplayValue());
				toscaMetadata.setDescription(componentInstance.getDescription());
			}

		}
		switch (component.getComponentType()) {
		case RESOURCE:
			Resource resource = (Resource) component;

			if (isInstance && componentInstance.getOriginType() == OriginTypeEnum.ServiceProxy) {
				toscaMetadata.setType(componentInstance.getOriginType().getDisplayValue());
			} else
				toscaMetadata.setType(resource.getResourceType().name());
			toscaMetadata.setSubcategory(categoryDefinition.getSubcategories().get(0).getName());
			toscaMetadata.setResourceVendor(resource.getVendorName());
			toscaMetadata.setResourceVendorRelease(resource.getVendorRelease());
			toscaMetadata.setResourceVendorModelNumber(resource.getResourceVendorModelNumber());
			break;
		case SERVICE:
			Service service = (Service) component;
			toscaMetadata.setType(component.getComponentType().getValue());
			toscaMetadata.setServiceType(service.getServiceType());
			toscaMetadata.setServiceRole(service.getServiceRole());
			toscaMetadata.setEnvironmentContext(service.getEnvironmentContext());
			if (!isInstance) {
				// DE268546
				toscaMetadata.setServiceEcompNaming(((Service) component).isEcompGeneratedNaming());
				toscaMetadata.setEcompGeneratedNaming(((Service) component).isEcompGeneratedNaming());
				toscaMetadata.setNamingPolicy(((Service) component).getNamingPolicy());
			}
			break;
		default:
			log.debug(NOT_SUPPORTED_COMPONENT_TYPE, component.getComponentType());
		}
		return toscaMetadata;
	}

	private Either<ImmutablePair<ToscaTemplate, Map<String, Component>>, ToscaError> fillImports(Component component,
			ToscaTemplate toscaTemplate) {

		if (null == DEFAULT_IMPORTS) {
			log.debug(FAILED_TO_GET_DEFAULT_IMPORTS_CONFIGURATION);
			return Either.right(ToscaError.GENERAL_ERROR);
		}

		Map<String, Component> componentCache = new HashMap<>();

		if (!ModelConverter.isAtomicComponent(component)) {
			List<ComponentInstance> componentInstances = component.getComponentInstances();
			if (componentInstances != null && !componentInstances.isEmpty()) {

				List<Map<String, Map<String, String>>> additionalImports = toscaTemplate.getImports() == null
						? new ArrayList<>(DEFAULT_IMPORTS) : new ArrayList<>(toscaTemplate.getImports());

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

				componentInstances.forEach(ci -> createDependency(componentCache, additionalImports, dependecies, ci));
				originComponents.putAll(componentCache);
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
			Either<Component, StorageOperationStatus> resource = toscaOperationFacade
					.getToscaFullElement(ci.getComponentUid());
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

				if (!ModelConverter.isAtomicComponent(componentRI)) {
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
		return artifactName.substring(0, artifactName.lastIndexOf('.')) + ToscaExportHandler.TOSCA_INTERFACE_NAME;
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

		// Extracted to method for code reuse
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

		// Extracted to method for code reuse
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
			log.debug(NOT_SUPPORTED_COMPONENT_TYPE, component.getComponentType());
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

			Either<Component, Boolean> originComponentRes = capabiltyRequirementConvertor
					.getOriginComponent(componentCache, componentInstance);
			if (originComponentRes.isRight()) {
				convertNodeTemplatesRes = Either.right(ToscaError.NODE_TYPE_REQUIREMENT_ERROR);
				break;
			}
			Either<ToscaNodeTemplate, ToscaError> requirements = convertComponentInstanceRequirements(component,
					componentInstance, component.getComponentInstancesRelations(), nodeTemplate,
					originComponentRes.left().value());
			if (requirements.isRight()) {
				convertNodeTemplatesRes = Either.right(requirements.right().value());
				break;
			}
			String instanceUniqueId = componentInstance.getUniqueId();
			log.debug("Component instance Requirements converted for instance {}", instanceUniqueId);

			nodeTemplate = requirements.left().value();

			Component componentOfInstance = componentCache.get(componentInstance.getActualComponentUid());
			nodeTemplate.setMetadata(convertMetadata(componentOfInstance, true, componentInstance));

			Either<ToscaNodeTemplate, ToscaError> capabilities = capabiltyRequirementConvertor
					.convertComponentInstanceCapabilties(componentInstance, dataTypes, nodeTemplate);
			if (capabilities.isRight()) {
				convertNodeTemplatesRes = Either.right(requirements.right().value());
				break;
			}
			log.debug("Component instance Capabilities converted for instance {}", instanceUniqueId);

			nodeTemplate = capabilities.left().value();
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
					boolean addToTosca = true;
					
					List<String> artifacts = groupInst.getArtifacts();
					if (artifacts == null || artifacts.isEmpty()) {
						addToTosca = false;
					}

					if (addToTosca) {
						ToscaGroupTemplate toscaGroup = convertGroupInstance(groupInst);
						groupsMap.put(groupInst.getName(), toscaGroup);
					}
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

				Supplier<String> supplier = () -> input.getValue() != null && !input.getValue().isEmpty()
						? input.getValue() : input.getDefaultValue();
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
		Supplier<List<? extends GroupProperty>> supplProperties = () -> groupInstance
				.convertToGroupInstancesProperties();
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
			if (!properties.containsKey(VF_MODULE_DESC_KEY)
					|| StringUtils.isEmpty((String) properties.get(VF_MODULE_DESC_KEY))) {
				properties.put(VF_MODULE_DESC_KEY, description.get());
			}
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
		if (groupProps != null) {
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
		if (ModelConverter.isAtomicComponent(component)) {
			if (((Resource) component).getDerivedFrom() != null) {
				toscaNodeType.setDerived_from(((Resource) component).getDerivedFrom().get(0));
			}
			toscaNodeType.setDescription(component.getDescription()); // or
																		// name??
		} else {
			String derivedFrom = null != component.getDerivedFromGenericType() ? component.getDerivedFromGenericType()
					: "tosca.nodes.Root";
			toscaNodeType.setDerived_from(derivedFrom);
		}
		return toscaNodeType;
	}

	private Either<Map<String, ToscaNodeType>, ToscaError> createProxyNodeTypes(Component container) {

		Map<String, ToscaNodeType> nodeTypesMap = null;
		Either<Map<String, ToscaNodeType>, ToscaError> res = Either.left(nodeTypesMap);

		List<ComponentInstance> componetInstances = container.getComponentInstances();

		if (componetInstances == null || componetInstances.isEmpty())
			return res;
		Map<String, ComponentInstance> serviceProxyInstanceList = new HashMap<>();
		List<ComponentInstance> proxyInst = componetInstances.stream()
				.filter(p -> p.getOriginType().name().equals(OriginTypeEnum.ServiceProxy.name()))
				.collect(Collectors.toList());
		if (proxyInst != null && !proxyInst.isEmpty()) {
			for (ComponentInstance inst : proxyInst) {
				serviceProxyInstanceList.put(inst.getToscaComponentName(), inst);
			}
		}

		if (serviceProxyInstanceList.isEmpty())
			return res;
		ComponentParametersView filter = new ComponentParametersView(true);
		filter.setIgnoreCapabilities(false);
		filter.setIgnoreComponentInstances(false);
		Either<Resource, StorageOperationStatus> serviceProxyOrigin = toscaOperationFacade
				.getLatestByName("serviceProxy");
		if (serviceProxyOrigin.isRight()) {
			log.debug("Failed to fetch normative service proxy resource by tosca name, error {}",
					serviceProxyOrigin.right().value());
			return Either.right(ToscaError.NOT_SUPPORTED_TOSCA_TYPE);
		}
		Component origComponent = serviceProxyOrigin.left().value();

		nodeTypesMap = new HashMap<>();
		for (Entry<String, ComponentInstance> entryProxy : serviceProxyInstanceList.entrySet()) {
			Component serviceComponent = null;
			ComponentParametersView componentParametersView = new ComponentParametersView();
			componentParametersView.disableAll();
			componentParametersView.setIgnoreCategories(false);
			Either<Component, StorageOperationStatus> service = toscaOperationFacade
					.getToscaElement(entryProxy.getValue().getSourceModelUid(), componentParametersView);
			if (service.isRight()) {
				log.debug("Failed to fetch resource with id {} for instance {}");
			} else
				serviceComponent = service.left().value();

			ToscaNodeType toscaNodeType = createProxyNodeType(origComponent, serviceComponent, entryProxy.getValue());
			nodeTypesMap.put(entryProxy.getKey(), toscaNodeType);
		}

		return Either.left(nodeTypesMap);
	}

	private ToscaNodeType createProxyNodeType(Component origComponent, Component proxyComponent,
			ComponentInstance instance) {
		ToscaNodeType toscaNodeType = new ToscaNodeType();
		String derivedFrom = ((Resource) origComponent).getToscaResourceName();

		toscaNodeType.setDerived_from(derivedFrom);
		Either<Map<String, DataTypeDefinition>, TitanOperationStatus> dataTypesEither = dataTypeCache.getAll();
		if (dataTypesEither.isRight()) {
			log.debug("Failed to retrieve all data types {}", dataTypesEither.right().value());
		}
		Map<String, DataTypeDefinition> dataTypes = dataTypesEither.left().value();
		Map<String, ToscaCapability> capabilities = this.capabiltyRequirementConvertor
				.convertProxyCapabilities(origComponent, proxyComponent, instance, dataTypes);

		toscaNodeType.setCapabilities(capabilities);

		return toscaNodeType;
	}

	private Either<ToscaNodeTemplate, ToscaError> convertComponentInstanceRequirements(Component component,
			ComponentInstance componentInstance, List<RequirementCapabilityRelDef> relations,
			ToscaNodeTemplate nodeTypeTemplate, Component originComponent) {

		List<Map<String, ToscaTemplateRequirement>> toscaRequirements = new ArrayList<>();
		if (!addRequirements(component, componentInstance, relations, originComponent, toscaRequirements)) {
			log.debug("Failed to convert component instance requirements for the component instance {}. ",
					componentInstance.getName());
			return Either.right(ToscaError.NODE_TYPE_REQUIREMENT_ERROR);
		}
		if (!toscaRequirements.isEmpty()) {
			nodeTypeTemplate.setRequirements(toscaRequirements);
		}
		log.debug("Finished to convert requirements for the node type {} ", componentInstance.getName());
		return Either.left(nodeTypeTemplate);
	}

	private boolean addRequirements(Component component, ComponentInstance componentInstance,
			List<RequirementCapabilityRelDef> relations, Component originComponent,
			List<Map<String, ToscaTemplateRequirement>> toscaRequirements) {
		boolean result;
		List<RequirementCapabilityRelDef> filteredRelations = relations.stream()
				.filter(p -> componentInstance.getUniqueId().equals(p.getFromNode())).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(filteredRelations)) {
			result = true;
		} else {
			result = !filteredRelations.stream().filter(rel -> !addRequirement(componentInstance, originComponent,
					component.getComponentInstances(), rel, toscaRequirements)).findFirst().isPresent();
		}
		return result;
	}

	private boolean addRequirement(ComponentInstance fromInstance, Component fromOriginComponent,
			List<ComponentInstance> instancesList, RequirementCapabilityRelDef rel,
			List<Map<String, ToscaTemplateRequirement>> toscaRequirements) {

		boolean result = true;
		Map<String, List<RequirementDefinition>> reqMap = fromOriginComponent.getRequirements();
		RelationshipInfo reqAndRelationshipPair = rel.getRelationships().get(0).getRelation();
		Either<Component, StorageOperationStatus> getOriginRes = null;
		Optional<RequirementDefinition> reqOpt = null;
		Component toOriginComponent = null;
		Optional<CapabilityDefinition> cap = null;

		ComponentInstance toInstance = instancesList.stream().filter(i -> rel.getToNode().equals(i.getUniqueId()))
				.findFirst().orElse(null);
		if (toInstance == null) {
			log.debug("Failed to find a relation from the node {} to the node {}", fromInstance.getName(),
					rel.getToNode());
			result = false;
		}
		if (result) {
			reqOpt = findRequirement(fromOriginComponent, reqMap, reqAndRelationshipPair, toInstance.getUniqueId());
			if (!reqOpt.isPresent()) {
				log.debug("Failed to find a requirement with uniqueId {} on a component with uniqueId {}",
						reqAndRelationshipPair.getRequirementUid(), fromOriginComponent.getUniqueId());
				result = false;
			}
		}
		if (result) {
			ComponentParametersView filter = new ComponentParametersView(true);
			filter.setIgnoreComponentInstances(false);
			filter.setIgnoreCapabilities(false);
			getOriginRes = toscaOperationFacade.getToscaElement(toInstance.getActualComponentUid(), filter);
			if (getOriginRes.isRight()) {
				log.debug("Failed to build substituted name for the requirement {}. Failed to get an origin component with uniqueId {}",
						reqOpt.get().getName(), toInstance.getActualComponentUid());
				result = false;
			}
		}
		if (result) {
			toOriginComponent = getOriginRes.left().value();
			cap = toOriginComponent.getCapabilities().get(reqOpt.get().getCapability()).stream()
					.filter(c -> c.getName().equals(reqAndRelationshipPair.getCapability())).findFirst();
			if (!cap.isPresent()) {
				cap = findCapability(reqMap, reqAndRelationshipPair, toOriginComponent, fromOriginComponent, reqOpt.get(), fromInstance);
				if(!cap.isPresent()){
				result = false;
				log.debug("Failed to find a capability with name {} on a component with uniqueId {}",
						reqAndRelationshipPair.getCapability(), fromOriginComponent.getUniqueId());
				}
			}
		}
		if (result) {
			result = buildAndAddRequirement(toscaRequirements, fromOriginComponent, toOriginComponent, cap.get(),
					reqOpt.get(), reqAndRelationshipPair, toInstance);
		}
		return result;
	}

	private Optional<CapabilityDefinition> findCapability(Map<String, List<RequirementDefinition>> reqMap, RelationshipInfo reqAndRelationshipPair, Component toOriginComponent, Component fromOriginComponent, RequirementDefinition requirement, ComponentInstance fromInstance) {
		Optional<CapabilityDefinition> cap = Optional.empty(); 
		Optional<RequirementDefinition> findAny = reqMap.values().stream().flatMap(e -> e.stream()).filter(e -> e.getName().equals(reqAndRelationshipPair.getRequirement())).findAny();
		 if (findAny.isPresent()) {
			 RequirementDefinition reqDefinition = findAny.get();
			 cap = toOriginComponent.getCapabilities().get(requirement.getCapability()).stream().filter(c -> c.getType().equals(reqDefinition.getCapability())).findFirst();
			 if (!cap.isPresent()) {
					log.debug("Failed to find a capability with name {} on a component with uniqueId {}", reqAndRelationshipPair.getCapability(), fromOriginComponent.getUniqueId());
			 }
		 } 
		return cap;
	}

	private boolean buildAndAddRequirement(List<Map<String, ToscaTemplateRequirement>> toscaRequirements, Component fromOriginComponent, Component toOriginComponent, CapabilityDefinition capability, RequirementDefinition requirement, RelationshipInfo reqAndRelationshipPair, ComponentInstance toInstance) {
		boolean result = true;
		Either<String, Boolean> buildReqNameRes = null;
		Either<String, Boolean> buildCapNameRes = capabiltyRequirementConvertor.buildSubstitutedName(originComponents,
				toOriginComponent, capability.getPath(), reqAndRelationshipPair.getCapability());
		if (buildCapNameRes.isRight()) {
			log.debug(
					"Failed to build a substituted capability name for the capability with name {} on a component with uniqueId {}",
					reqAndRelationshipPair.getCapability(), fromOriginComponent.getUniqueId());
			result = false;
		}
		if (result) {
			buildReqNameRes = capabiltyRequirementConvertor.buildSubstitutedName(originComponents, fromOriginComponent,
					requirement.getPath(), reqAndRelationshipPair.getRequirement());
			if (buildReqNameRes.isRight()) {
				log.debug(
						"Failed to build a substituted requirement name for the requirement with name {} on a component with uniqueId {}",
						reqAndRelationshipPair.getRequirement(), fromOriginComponent.getUniqueId());
				result = false;
			}
		}
		if (result) {
			ToscaTemplateRequirement toscaRequirement = new ToscaTemplateRequirement();
			Map<String, ToscaTemplateRequirement> toscaReqMap = new HashMap<>();
			toscaRequirement.setNode(toInstance.getName());
			toscaRequirement.setCapability(buildCapNameRes.left().value());
			toscaReqMap.put(buildReqNameRes.left().value(), toscaRequirement);
			toscaRequirements.add(toscaReqMap);
		}
		return result;
	}

	private Optional<RequirementDefinition> findRequirement(Component fromOriginComponent, Map<String, List<RequirementDefinition>> reqMap, RelationshipInfo reqAndRelationshipPair,  String toInstanceId) {
		for(List<RequirementDefinition> reqList: reqMap.values()){
			Optional<RequirementDefinition> reqOpt = reqList.stream().filter(r -> isRequirementBelongToRelation(fromOriginComponent, reqAndRelationshipPair, r, toInstanceId)).findFirst();
			if(reqOpt.isPresent()){
				return reqOpt;
			}
		}
		return Optional.empty();
	}

	/**
	 * Allows detecting the requirement belonging to the received relationship
	 * The detection logic is: A requirement belongs to a relationship IF 1.The
	 * name of the requirement equals to the "requirement" field of the
	 * relation; AND 2. In case of a non-atomic resource, OwnerId of the
	 * requirement equals to requirementOwnerId of the relation OR uniqueId of
	 * toInstance equals to capabilityOwnerId of the relation
	 * 
	 * @param fromOriginComponent
	 * @param reqAndRelationshipPair
	 * @param requirement
	 * @param toInstanceId
	 * @return
	 */
	private boolean isRequirementBelongToRelation(Component originComponent, RelationshipInfo reqAndRelationshipPair, RequirementDefinition requirement, String toInstanceId) {
		if (!StringUtils.equals(requirement.getName(), reqAndRelationshipPair.getRequirement()))
			return false;
		if (!ModelConverter.isAtomicComponent(originComponent))
			return isRequirementBelongToOwner(reqAndRelationshipPair, requirement, toInstanceId);
		return true;
	}

	private boolean isRequirementBelongToOwner(RelationshipInfo reqAndRelationshipPair, RequirementDefinition requirement, String toInstanceId) {
		return StringUtils.equals(requirement.getOwnerId(), reqAndRelationshipPair.getRequirementOwnerId())	|| StringUtils.equals(toInstanceId, reqAndRelationshipPair.getCapabilityOwnerId());
	}

	private Either<SubstitutionMapping, ToscaError> convertCapabilities(Component component,
			SubstitutionMapping substitutionMappings) {

		Either<SubstitutionMapping, ToscaError> result = Either.left(substitutionMappings);
		Either<Map<String, String[]>, ToscaError> toscaCapabilitiesRes = capabiltyRequirementConvertor
				.convertSubstitutionMappingCapabilities(originComponents, component);
		if (toscaCapabilitiesRes.isRight()) {
			result = Either.right(toscaCapabilitiesRes.right().value());
			log.error("Failed convert capabilities for the component {}. ", component.getName());
		} else if (MapUtils.isNotEmpty(toscaCapabilitiesRes.left().value())) {
			substitutionMappings.setCapabilities(toscaCapabilitiesRes.left().value());
			log.debug("Finish convert capabilities for the component {}. ", component.getName());
		}
		log.debug("Finished to convert capabilities for the component {}. ", component.getName());
		return result;
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
				if ("dependencies".equals(property.getName())) {
					return null;
				}
				NodeTuple defaultNode = super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);

				return "_defaultp_".equals(property.getName())
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
			@Override
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
			return new LinkedHashSet<>(fields);
		}
	}

}
