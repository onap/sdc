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

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static org.openecomp.sdc.be.components.utils.PropertiesUtils.resolvePropertyValueFromInput;
import static org.openecomp.sdc.tosca.datatypes.ToscaFunctions.GET_ATTRIBUTE;
import static org.openecomp.sdc.tosca.datatypes.ToscaFunctions.GET_INPUT;
import static org.openecomp.sdc.tosca.datatypes.ToscaFunctions.GET_PROPERTY;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Ints;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import fj.data.Either;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.be.components.impl.exceptions.SdcResourceNotFoundException;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.data.model.ToscaImportByModel;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterPropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementSubstitutionFilterPropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.exception.ToscaExportException;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceAttribute;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceInterface;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;
import org.openecomp.sdc.be.model.tosca.converters.ToscaMapValueConverter;
import org.openecomp.sdc.be.tosca.PropertyConvertor.PropertyType;
import org.openecomp.sdc.be.tosca.builder.ToscaRelationshipBuilder;
import org.openecomp.sdc.be.tosca.exception.ToscaConversionException;
import org.openecomp.sdc.be.tosca.model.CapabilityFilter;
import org.openecomp.sdc.be.tosca.model.NodeFilter;
import org.openecomp.sdc.be.tosca.model.SubstitutionMapping;
import org.openecomp.sdc.be.tosca.model.ToscaAttribute;
import org.openecomp.sdc.be.tosca.model.ToscaCapability;
import org.openecomp.sdc.be.tosca.model.ToscaDataType;
import org.openecomp.sdc.be.tosca.model.ToscaGroupTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaNodeTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaPolicyTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyAssignment;
import org.openecomp.sdc.be.tosca.model.ToscaRelationshipTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaRequirement;
import org.openecomp.sdc.be.tosca.model.ToscaTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaTemplateArtifact;
import org.openecomp.sdc.be.tosca.model.ToscaTemplateRequirement;
import org.openecomp.sdc.be.tosca.model.ToscaTopolgyTemplate;
import org.openecomp.sdc.be.tosca.utils.ForwardingPathToscaUtil;
import org.openecomp.sdc.be.tosca.utils.InputConverter;
import org.openecomp.sdc.be.tosca.utils.OutputConverter;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
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

@org.springframework.stereotype.Component("tosca-export-handler")
public class ToscaExportHandler {

    public static final String ASSET_TOSCA_TEMPLATE = "assettoscatemplate";
    private static final Logger log = Logger.getLogger(ToscaExportHandler.class);
    private static final String INVARIANT_UUID = "invariantUUID";
    private static final String TOSCA_VERSION = "tosca_simple_yaml_1_3";
    private static final String SERVICE_NODE_TYPE_PREFIX = "org.openecomp.service.";
    private static final String IMPORTS_FILE_KEY = "file";
    private static final String TOSCA_INTERFACE_NAME = "-interface.yml";
    private static final String FAILED_TO_GET_DEFAULT_IMPORTS_CONFIGURATION = "convertToToscaTemplate - failed to get Default Imports section from configuration";
    private static final String NOT_SUPPORTED_COMPONENT_TYPE = "Not supported component type {}";
    private static final String NATIVE_ROOT = "tosca.nodes.Root";
    private static final List<String> EXCLUDED_CATEGORY_SPECIFIC_METADATA = List
        .of("Service Function", "Service Role", "Naming Policy", "Service Type");
    private static final YamlUtil yamlUtil = new YamlUtil();
    private final ApplicationDataTypeCache applicationDataTypeCache;
    private final ToscaOperationFacade toscaOperationFacade;
    private final CapabilityRequirementConverter capabilityRequirementConverter;
    private final PolicyExportParser policyExportParser;
    private final GroupExportParser groupExportParser;
    private final PropertyConvertor propertyConvertor;
    private final AttributeConverter attributeConverter;
    private final InputConverter inputConverter;
    private final OutputConverter outputConverter;
    private final InterfaceLifecycleOperation interfaceLifecycleOperation;
    private final InterfacesOperationsConverter interfacesOperationsConverter;
    private final ModelOperation modelOperation;

    @Autowired
    public ToscaExportHandler(final ApplicationDataTypeCache applicationDataTypeCache,
                              final ToscaOperationFacade toscaOperationFacade,
                              final CapabilityRequirementConverter capabilityRequirementConverter,
                              final PolicyExportParser policyExportParser,
                              final GroupExportParser groupExportParser,
                              final PropertyConvertor propertyConvertor,
                              final AttributeConverter attributeConverter,
                              final InputConverter inputConverter,
                              final OutputConverter outputConverter,
                              final InterfaceLifecycleOperation interfaceLifecycleOperation,
                              final InterfacesOperationsConverter interfacesOperationsConverter,
                              final ModelOperation modelOperation) {
        this.applicationDataTypeCache = applicationDataTypeCache;
        this.toscaOperationFacade = toscaOperationFacade;
        this.capabilityRequirementConverter = capabilityRequirementConverter;
        this.policyExportParser = policyExportParser;
        this.groupExportParser = groupExportParser;
        this.propertyConvertor = propertyConvertor;
        this.attributeConverter = attributeConverter;
        this.inputConverter = inputConverter;
        this.outputConverter = outputConverter;
        this.interfaceLifecycleOperation = interfaceLifecycleOperation;
        this.interfacesOperationsConverter = interfacesOperationsConverter;
        this.modelOperation = modelOperation;
    }

    public static String getInterfaceFilename(String artifactName) {
        return artifactName.substring(0, artifactName.lastIndexOf('.')) + TOSCA_INTERFACE_NAME;
    }

    private static void removeOperationImplementationForProxyNodeType(Map<String, InterfaceDefinition> proxyComponentInterfaces) {
        if (MapUtils.isEmpty(proxyComponentInterfaces)) {
            return;
        }
        proxyComponentInterfaces.values().stream().map(InterfaceDataDefinition::getOperations).filter(MapUtils::isNotEmpty)
            .forEach(operations -> operations.values().forEach(operation -> operation.setImplementation(null)));
    }

    public Either<ToscaRepresentation, ToscaError> exportComponent(Component component) {
        return convertToToscaTemplate(component).left().map(this::createToscaRepresentation);
    }

    public Either<ToscaRepresentation, ToscaError> exportComponentInterface(final Component component, final boolean isAssociatedComponent) {
        final List<Map<String, Map<String, String>>> imports = new ArrayList<>(getDefaultToscaImports(component.getModel()));
        if (CollectionUtils.isEmpty(imports)) {
            log.debug(FAILED_TO_GET_DEFAULT_IMPORTS_CONFIGURATION);
            return Either.right(ToscaError.GENERAL_ERROR);
        }
        List<Triple<String, String, Component>> dependencies = new ArrayList<>();
        if (component.getDerivedFromGenericType() != null && !component.getDerivedFromGenericType()
            .startsWith("org.openecomp.resource.abstract.nodes.")) {
            final Either<Component, StorageOperationStatus> baseType = toscaOperationFacade
                .getByToscaResourceNameAndVersion(component.getDerivedFromGenericType(), component.getDerivedFromGenericVersion(),
                    component.getModel());
            if (baseType.isLeft() && baseType.left().value() != null) {
                addDependencies(imports, dependencies, baseType.left().value());
            } else {
                log.debug("Failed to fetch derived from type {}", component.getDerivedFromGenericType());
            }
        }

        String toscaVersion = null;
        if (component instanceof Resource) {
            toscaVersion = ((Resource) component).getToscaVersion();
        }
        ToscaTemplate toscaTemplate = new ToscaTemplate(toscaVersion != null ? toscaVersion : TOSCA_VERSION);
        toscaTemplate.setImports(imports);
        final Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
        final Either<ToscaTemplate, ToscaError> toscaTemplateRes = convertInterfaceNodeType(new HashMap<>(), component, toscaTemplate, nodeTypes,
            isAssociatedComponent);
        if (toscaTemplateRes.isRight()) {
            return Either.right(toscaTemplateRes.right().value());
        }
        toscaTemplate = toscaTemplateRes.left().value();
        toscaTemplate.setDependencies(dependencies);
        ToscaRepresentation toscaRepresentation = this.createToscaRepresentation(toscaTemplate);
        return Either.left(toscaRepresentation);
    }

    private ToscaRepresentation createToscaRepresentation(ToscaTemplate toscaTemplate) {
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
        sb.append(getConfiguration().getHeatEnvArtifactHeader());
        sb.append(yamlAsString);
        sb.append(getConfiguration().getHeatEnvArtifactFooter());
        return ToscaRepresentation.make(sb.toString().getBytes(), toscaTemplate);
    }

    public Either<ToscaTemplate, ToscaError> getDependencies(Component component) {
        ToscaTemplate toscaTemplate = new ToscaTemplate(null);
        Either<ImmutablePair<ToscaTemplate, Map<String, Component>>, ToscaError> fillImports = fillImports(component, toscaTemplate);
        if (fillImports.isRight()) {
            return Either.right(fillImports.right().value());
        }
        return Either.left(fillImports.left().value().left);
    }

    public Either<ToscaTemplate, ToscaError> convertToToscaTemplate(final Component component) {
        final List<Map<String, Map<String, String>>> defaultToscaImportConfig = getDefaultToscaImports(component.getModel());
        if (CollectionUtils.isEmpty(defaultToscaImportConfig)) {
            log.debug(FAILED_TO_GET_DEFAULT_IMPORTS_CONFIGURATION);
            return Either.right(ToscaError.GENERAL_ERROR);
        }
        log.trace("start tosca export for {}", component.getUniqueId());
        String toscaVersion = null;
        if (component instanceof Resource) {
            toscaVersion = ((Resource) component).getToscaVersion();
        }
        final ToscaTemplate toscaTemplate = new ToscaTemplate(toscaVersion != null ? toscaVersion : TOSCA_VERSION);
        toscaTemplate.setMetadata(convertMetadata(component));
        toscaTemplate.setImports(new ArrayList<>(defaultToscaImportConfig));
        final Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
        if (ModelConverter.isAtomicComponent(component)) {
            log.trace("convert component as node type");
            return convertNodeType(new HashMap<>(), component, toscaTemplate, nodeTypes);
        } else {
            log.trace("convert component as topology template");
            return convertToscaTemplate(component, toscaTemplate);
        }
    }

    private List<Map<String, Map<String, String>>> getDefaultToscaImports(final String modelId) {
        if (modelId == null) {
            return getDefaultToscaImportConfig();
        }

        final List<ToscaImportByModel> allModelImports = modelOperation.findAllModelImports(modelId, true);
        final List<Map<String, Map<String, String>>> importList = new ArrayList<>();
        final Set<Path> addedPathList = new HashSet<>();
        for (final ToscaImportByModel toscaImportByModel : allModelImports) {
            var importPath = Path.of(toscaImportByModel.getFullPath());
            if (addedPathList.contains(importPath)) {
                importPath = ToscaDefaultImportHelper.addModelAsFilePrefix(importPath, toscaImportByModel.getModelId());
            }
            final String fileName = FilenameUtils.getBaseName(importPath.toString());
            importList.add(Map.of(fileName, Map.of("file", importPath.toString())));
            addedPathList.add(importPath);
        }
        return importList;
    }

    private Either<ToscaTemplate, ToscaError> convertToscaTemplate(Component component, ToscaTemplate toscaNode) {
        Either<ImmutablePair<ToscaTemplate, Map<String, Component>>, ToscaError> importsRes = fillImports(component, toscaNode);
        if (importsRes.isRight()) {
            return Either.right(importsRes.right().value());
        }
        toscaNode = importsRes.left().value().left;
        Map<String, Component> componentCache = importsRes.left().value().right;
        Either<Map<String, ToscaNodeType>, ToscaError> nodeTypesMapEither = createProxyNodeTypes(componentCache, component);
        if (nodeTypesMapEither.isRight()) {
            log.debug("Failed to fetch normative service proxy resource by tosca name, error {}", nodeTypesMapEither.right().value());
            return Either.right(nodeTypesMapEither.right().value());
        }
        Map<String, ToscaNodeType> nodeTypesMap = nodeTypesMapEither.left().value();
        if (nodeTypesMap != null && !nodeTypesMap.isEmpty()) {
            toscaNode.setNode_types(nodeTypesMap);
        }
        createServiceSubstitutionNodeTypes(componentCache, component, toscaNode);
        Either<Map<String, Object>, ToscaError> proxyInterfaceTypesEither = createProxyInterfaceTypes(component);
        if (proxyInterfaceTypesEither.isRight()) {
            log.debug("Failed to populate service proxy local interface types in tosca, error {}", nodeTypesMapEither.right().value());
            return Either.right(proxyInterfaceTypesEither.right().value());
        }
        Map<String, Object> proxyInterfaceTypes = proxyInterfaceTypesEither.left().value();
        if (MapUtils.isNotEmpty(proxyInterfaceTypes)) {
            toscaNode.setInterface_types(proxyInterfaceTypes);
        }
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> dataTypesEither = applicationDataTypeCache.getAll(component.getModel());
        if (dataTypesEither.isRight()) {
            log.debug("Failed to retrieve all data types {}", dataTypesEither.right().value());
            return Either.right(ToscaError.GENERAL_ERROR);
        }
        Map<String, DataTypeDefinition> dataTypes = dataTypesEither.left().value();
        ToscaTopolgyTemplate topologyTemplate = new ToscaTopolgyTemplate();
        List<InputDefinition> inputDef = component.getInputs();
        Map<String, ToscaProperty> inputs = inputConverter.convertInputs(inputDef, dataTypes);
        if (!inputs.isEmpty()) {
            topologyTemplate.setInputs(inputs);
        }
        final Map<String, ToscaProperty> outputs;
        try {
            outputs = outputConverter.convert(component.getOutputs(), dataTypes);
        } catch (final ToscaConversionException e) {
            log.error(EcompLoggerErrorCode.SCHEMA_ERROR, ToscaExportHandler.class.getName(),
                "Could not parse component '{}' outputs. Component unique id '{}'.", component.getName(), component.getUniqueId(), e);
            return Either.right(ToscaError.GENERAL_ERROR);
        }
        if (!outputs.isEmpty()) {
            topologyTemplate.setOutputs(outputs);
        }
        if (CollectionUtils.isNotEmpty(component.getComponentInstances())) {
            final Either<Map<String, ToscaNodeTemplate>, ToscaError> nodeTemplates =
                convertNodeTemplates(component, componentCache, dataTypes, topologyTemplate);
            if (nodeTemplates.isRight()) {
                return Either.right(nodeTemplates.right().value());
            }
            log.debug("node templates converted");
            topologyTemplate.setNode_templates(nodeTemplates.left().value());
        }
        final Map<String, ToscaRelationshipTemplate> relationshipTemplatesMap = new ToscaExportRelationshipTemplatesHandler()
            .createFrom(topologyTemplate.getNode_templates());
        if (!relationshipTemplatesMap.isEmpty()) {
            topologyTemplate.setRelationshipTemplates(relationshipTemplatesMap);
        }
        addGroupsToTopologyTemplate(component, topologyTemplate);
        try {
            addPoliciesToTopologyTemplate(component, topologyTemplate);
        } catch (SdcResourceNotFoundException e) {
            log.debug("Fail to add policies to topology template:", e);
            return Either.right(ToscaError.GENERAL_ERROR);
        }
        try {
            createSubstitutionMapping(component, componentCache).ifPresent(topologyTemplate::setSubstitution_mappings);
        } catch (final ToscaExportException e) {
            log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, ToscaExportHandler.class.getName(), e.getMessage());
            return Either.right(e.getToscaError());
        }
        if (!topologyTemplate.isEmpty()) {
            toscaNode.setTopology_template(topologyTemplate);
        }
        return Either.left(toscaNode);
    }

    private Either<String, ToscaError> createComponentToscaName(final Component component) {
        switch (component.getComponentType()) {
            case RESOURCE:
                final ResourceMetadataDataDefinition resourceMetadata =
                    (ResourceMetadataDataDefinition) component.getComponentMetadataDefinition().getMetadataDataDefinition();
                return Either.left(resourceMetadata.getToscaResourceName());
            case SERVICE:
                return Either.left(SERVICE_NODE_TYPE_PREFIX + component.getComponentMetadataDefinition().getMetadataDataDefinition().getSystemName());
            default:
                log.debug(NOT_SUPPORTED_COMPONENT_TYPE, component.getComponentType());
                return Either.right(ToscaError.NOT_SUPPORTED_TOSCA_TYPE);
        }
    }

    private Optional<SubstitutionMapping> createSubstitutionMapping(final Component component,
                                                                    final Map<String, Component> componentCache) throws ToscaExportException {
        if (component instanceof Service && !((Service) component).isSubstituteCandidate()) {
            return Optional.empty();
        }

        final Either<String, ToscaError> toscaResourceNameEither = createComponentToscaName(component);
        if (toscaResourceNameEither.isRight()) {
            throw new ToscaExportException("Could not create component TOSCA name", toscaResourceNameEither.right().value());
        }
        final String toscaResourceName = toscaResourceNameEither.left().value();

        final SubstitutionMapping substitutionMapping = new SubstitutionMapping();
        substitutionMapping.setNode_type(toscaResourceName);
        convertSubstitutionMappingFilter(component).ifPresent(substitutionMapping::setSubstitution_filter);

        final Either<Map<String, String[]>, ToscaError> capabilitiesEither = convertSubstitutionMappingCapabilities(component, componentCache);
        if (capabilitiesEither.isRight()) {
            throw new ToscaExportException("Could not convert substitution mapping capabilities", capabilitiesEither.right().value());
        }
        final Map<String, String[]> capabilityMap = capabilitiesEither.left().value();
        if (!capabilityMap.isEmpty()) {
            substitutionMapping.setCapabilities(capabilityMap);
        }

        final Either<Map<String, String[]>, ToscaError> requirements =
            capabilityRequirementConverter.convertSubstitutionMappingRequirements(component, componentCache);
        if (requirements.isRight()) {
            throw new ToscaExportException("Could not convert substitution mapping requirements", requirements.right().value());
        }
        final Map<String, String[]> requirementMap = requirements.left().value();
        if (MapUtils.isNotEmpty(requirementMap)) {
            substitutionMapping.setRequirements(requirementMap);
        }

        final Map<String, String[]> propertyMappingMap = buildSubstitutionMappingPropertyMapping(component);
        if (MapUtils.isNotEmpty(propertyMappingMap)) {
            substitutionMapping.setProperties(propertyMappingMap);
        }

        final Map<String, String[]> attributesMappingMap = buildSubstitutionMappingAttributesMapping(component);
        if (MapUtils.isNotEmpty(attributesMappingMap)) {
            substitutionMapping.setAttributes(attributesMappingMap);
        }

        return Optional.of(substitutionMapping);
    }

    private Optional<NodeFilter> convertSubstitutionMappingFilter(final Component component) {
        if (component.getSubstitutionFilter() == null || (component.getSubstitutionFilter().getProperties()).getListToscaDataDefinition() == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(convertToSubstitutionFilterComponent(component.getSubstitutionFilter()));
    }

    private void addGroupsToTopologyTemplate(Component component, ToscaTopolgyTemplate topologyTemplate) {
        Map<String, ToscaGroupTemplate> groups = groupExportParser.getGroups(component);
        if (groups != null) {
            topologyTemplate.addGroups(groups);
        }
    }

    private void addPoliciesToTopologyTemplate(Component component, ToscaTopolgyTemplate topologyTemplate) throws SdcResourceNotFoundException {
        Map<String, ToscaPolicyTemplate> policies = policyExportParser.getPolicies(component);
        if (policies != null) {
            topologyTemplate.addPolicies(policies);
        }
    }

    private Map<String, String> convertMetadata(Component component) {
        return convertMetadata(component, false, null);
    }

    private Map<String, String> convertMetadata(Component component, boolean isInstance, ComponentInstance componentInstance) {
        Map<String, String> toscaMetadata = new LinkedHashMap<>();
        toscaMetadata.put(convertMetadataKey(JsonPresentationFields.INVARIANT_UUID), component.getInvariantUUID());
        toscaMetadata.put(JsonPresentationFields.UUID.getPresentation(), component.getUUID());
        toscaMetadata
            .put(JsonPresentationFields.NAME.getPresentation(), component.getComponentMetadataDefinition().getMetadataDataDefinition().getName());
        toscaMetadata.put(JsonPresentationFields.DESCRIPTION.getPresentation(), component.getDescription());
        List<CategoryDefinition> categories = component.getCategories();
        CategoryDefinition categoryDefinition = categories.get(0);
        toscaMetadata.put(JsonPresentationFields.MODEL.getPresentation(), component.getModel());
        toscaMetadata.put(JsonPresentationFields.CATEGORY.getPresentation(), categoryDefinition.getName());
        if (isInstance) {
            toscaMetadata.put(JsonPresentationFields.VERSION.getPresentation(), component.getVersion());
            toscaMetadata.put(JsonPresentationFields.CUSTOMIZATION_UUID.getPresentation(), componentInstance.getCustomizationUUID());
            if (componentInstance.getSourceModelInvariant() != null && !componentInstance.getSourceModelInvariant().isEmpty()) {
                toscaMetadata.put(JsonPresentationFields.VERSION.getPresentation(), componentInstance.getComponentVersion());
                toscaMetadata.put(JsonPresentationFields.CI_SOURCE_MODEL_INVARIANT.getPresentation(), componentInstance.getSourceModelInvariant());
                toscaMetadata.put(JsonPresentationFields.CI_SOURCE_MODEL_UUID.getPresentation(), componentInstance.getSourceModelUuid());
                toscaMetadata.put(JsonPresentationFields.CI_SOURCE_MODEL_NAME.getPresentation(), componentInstance.getSourceModelName());
                if (componentInstance.getOriginType() == OriginTypeEnum.ServiceProxy) {
                    toscaMetadata.put(JsonPresentationFields.NAME.getPresentation(),
                        componentInstance.getSourceModelName() + " " + OriginTypeEnum.ServiceProxy.getDisplayValue());
                } else if (componentInstance.getOriginType() == OriginTypeEnum.ServiceSubstitution) {
                    toscaMetadata.put(JsonPresentationFields.NAME.getPresentation(),
                        componentInstance.getSourceModelName() + " " + OriginTypeEnum.ServiceSubstitution.getDisplayValue());
                }
                toscaMetadata.put(JsonPresentationFields.DESCRIPTION.getPresentation(), componentInstance.getDescription());
            }
        }
        switch (component.getComponentType()) {
            case RESOURCE:
                Resource resource = (Resource) component;
                if (isInstance && (componentInstance.getOriginType() == OriginTypeEnum.ServiceProxy
                    || componentInstance.getOriginType() == OriginTypeEnum.ServiceSubstitution)) {
                    toscaMetadata.put(JsonPresentationFields.TYPE.getPresentation(), componentInstance.getOriginType().getDisplayValue());
                } else {
                    toscaMetadata.put(JsonPresentationFields.TYPE.getPresentation(), resource.getResourceType().name());
                }
                toscaMetadata.put(JsonPresentationFields.SUB_CATEGORY.getPresentation(), categoryDefinition.getSubcategories().get(0).getName());
                toscaMetadata.put(JsonPresentationFields.RESOURCE_VENDOR.getPresentation(), resource.getVendorName());
                toscaMetadata.put(JsonPresentationFields.RESOURCE_VENDOR_RELEASE.getPresentation(), resource.getVendorRelease());
                toscaMetadata.put(JsonPresentationFields.RESOURCE_VENDOR_MODEL_NUMBER.getPresentation(), resource.getResourceVendorModelNumber());
                break;
            case SERVICE:
                Service service = (Service) component;
                toscaMetadata.put(JsonPresentationFields.TYPE.getPresentation(), component.getComponentType().getValue());
                toscaMetadata.put(JsonPresentationFields.SERVICE_TYPE.getPresentation(), service.getServiceType());
                toscaMetadata.put(JsonPresentationFields.SERVICE_ROLE.getPresentation(), service.getServiceRole());
                toscaMetadata.put(JsonPresentationFields.SERVICE_FUNCTION.getPresentation(), service.getServiceFunction());
                toscaMetadata.put(JsonPresentationFields.ENVIRONMENT_CONTEXT.getPresentation(), service.getEnvironmentContext());
                toscaMetadata.put(JsonPresentationFields.INSTANTIATION_TYPE.getPresentation(),
                    service.getEnvironmentContext() == null ? StringUtils.EMPTY : service.getInstantiationType());
                if (!isInstance) {
                    // DE268546
                    toscaMetadata.put(JsonPresentationFields.ECOMP_GENERATED_NAMING.getPresentation(), service.isEcompGeneratedNaming().toString());
                    toscaMetadata.put(JsonPresentationFields.ECOMP_GENERATED_NAMING.getPresentation(), service.isEcompGeneratedNaming().toString());
                    toscaMetadata.put(JsonPresentationFields.NAMING_POLICY.getPresentation(), service.getNamingPolicy());
                }
                break;
            default:
                log.debug(NOT_SUPPORTED_COMPONENT_TYPE, component.getComponentType());
        }
        for (final String key : component.getCategorySpecificMetadata().keySet()) {
            if (!EXCLUDED_CATEGORY_SPECIFIC_METADATA.contains(key)) {
                toscaMetadata.put(key, component.getCategorySpecificMetadata().get(key));
            }
        }
        return toscaMetadata;
    }

    private String convertMetadataKey(JsonPresentationFields jsonPresentationField) {
        if (JsonPresentationFields.INVARIANT_UUID.equals(jsonPresentationField)) {
            return INVARIANT_UUID;
        }
        return jsonPresentationField.getPresentation();
    }

    private Either<ImmutablePair<ToscaTemplate, Map<String, Component>>, ToscaError> fillImports(Component component, ToscaTemplate toscaTemplate) {
        final List<Map<String, Map<String, String>>> defaultToscaImportConfig = getDefaultToscaImports(component.getModel());
        if (CollectionUtils.isEmpty(defaultToscaImportConfig)) {
            log.debug(FAILED_TO_GET_DEFAULT_IMPORTS_CONFIGURATION);
            return Either.right(ToscaError.GENERAL_ERROR);
        }
        Map<String, Component> componentCache = new HashMap<>();
        if (!ModelConverter.isAtomicComponent(component)) {
            final List<Map<String, Map<String, String>>> additionalImports =
                toscaTemplate.getImports() == null ? new ArrayList<>(defaultToscaImportConfig) : new ArrayList<>(toscaTemplate.getImports());
            List<Triple<String, String, Component>> dependencies = new ArrayList<>();
            Map<String, ArtifactDefinition> toscaArtifacts = component.getToscaArtifacts();
            final Map<String, Map<String, String>> substituteTypeImportEntry = generateComponentSubstituteTypeImport(component, toscaArtifacts);
            if (!substituteTypeImportEntry.isEmpty()) {
                additionalImports.add(substituteTypeImportEntry);
            }
            List<ComponentInstance> componentInstances = component.getComponentInstances();
            if (componentInstances != null && !componentInstances.isEmpty()) {
                componentInstances.forEach(ci -> createDependency(componentCache, additionalImports, dependencies, ci));
            }
            toscaTemplate.setDependencies(dependencies);
            toscaTemplate.setImports(additionalImports);
        } else {
            log.debug("currently imports supported for VF and service only");
        }
        return Either.left(new ImmutablePair<>(toscaTemplate, componentCache));
    }

    private Map<String, Map<String, String>> generateComponentSubstituteTypeImport(final Component component,
                                                                                   final Map<String, ArtifactDefinition> toscaArtifacts) {

        if (component instanceof Service && !((Service) component).isSubstituteCandidate()) {
            return Collections.emptyMap();
        }
        if (MapUtils.isEmpty(toscaArtifacts)) {
            return Collections.emptyMap();
        }
        final ArtifactDefinition artifactDefinition = toscaArtifacts.get(ASSET_TOSCA_TEMPLATE);
        if (artifactDefinition == null) {
            return Collections.emptyMap();
        }
        final var importEntryName = component.getComponentType().toString().toLowerCase() + "-" + component.getName() + "-interface";
        return Map.of(importEntryName,
            Map.of(IMPORTS_FILE_KEY, getInterfaceFilename(artifactDefinition.getArtifactName()))
        );
    }

    private List<Map<String, Map<String, String>>> getDefaultToscaImportConfig() {
        return getConfiguration().getDefaultImports();
    }

    private void createDependency(final Map<String, Component> componentCache, final List<Map<String, Map<String, String>>> imports,
                                  final List<Triple<String, String, Component>> dependencies, final ComponentInstance componentInstance) {
        log.debug("createDependency componentCache {}", componentCache);
        Component componentRI = componentCache.get(componentInstance.getComponentUid());
        if (componentRI == null || componentInstance.getOriginType() == OriginTypeEnum.ServiceSubstitution) {
            // all resource must be only once!
            final Either<Component, StorageOperationStatus> resource = toscaOperationFacade.getToscaFullElement(componentInstance.getComponentUid());
            if ((resource.isRight()) && (log.isDebugEnabled())) {
                log.debug("Failed to fetch resource with id {} for instance {}", componentInstance.getComponentUid(),
                    componentInstance.getUniqueId());
                return;
            }
            final Component fetchedComponent = resource.left().value();
            componentRI = setComponentCache(componentCache, componentInstance, fetchedComponent);
            addDependencies(imports, dependencies, componentRI);
        }
    }

    /**
     * Sets a componentCache from the given component/resource.
     */
    private Component setComponentCache(final Map<String, Component> componentCache, final ComponentInstance componentInstance,
                                        final Component fetchedComponent) {
        componentCache.put(fetchedComponent.getUniqueId(), fetchedComponent);
        if (componentInstance.getOriginType() == OriginTypeEnum.ServiceProxy
            || componentInstance.getOriginType() == OriginTypeEnum.ServiceSubstitution) {
            final Either<Component, StorageOperationStatus> sourceService = toscaOperationFacade
                .getToscaFullElement(componentInstance.getSourceModelUid());
            if (sourceService.isRight() && (log.isDebugEnabled())) {
                log.debug("Failed to fetch source service with id {} for proxy {}", componentInstance.getSourceModelUid(),
                    componentInstance.getUniqueId());
            }
            final Component fetchedSource = sourceService.left().value();
            componentCache.put(fetchedSource.getUniqueId(), fetchedSource);
            return fetchedSource;
        }
        return fetchedComponent;
    }

    /**
     * Retrieves all derived_from nodes and stores it in a predictable order.
     */
    private void addDependencies(final List<Map<String, Map<String, String>>> imports, final List<Triple<String, String, Component>> dependencies,
                                 final Component fetchedComponent) {
        final Set<Component> componentsList = new LinkedHashSet<>();
        if (fetchedComponent instanceof Resource) {
            log.debug("fetchedComponent is a resource {}", fetchedComponent);
            final Optional<Map<String, String>> derivedFromMapOfIdToName = getDerivedFromMapOfIdToName(fetchedComponent, componentsList);
            if (derivedFromMapOfIdToName.isPresent() && !derivedFromMapOfIdToName.get().isEmpty()) {
                derivedFromMapOfIdToName.get().entrySet().forEach(entry -> {
                    log.debug("Started entry.getValue() : {}", entry.getValue());
                    if (!NATIVE_ROOT.equals(entry.getValue())) {
                        Either<Resource, StorageOperationStatus> resourcefetched = toscaOperationFacade.getToscaElement(entry.getKey());
                        if (resourcefetched != null && resourcefetched.isLeft()) {
                            componentsList.add(resourcefetched.left().value());
                        }
                    }
                });
                setImports(imports, dependencies, componentsList);
            } else {
                setImports(imports, dependencies, fetchedComponent);
            }
        }
    }

    /**
     * Returns all derived_from nodes found.
     */
    private Optional<Map<String, String>> getDerivedFromMapOfIdToName(final Component fetchedComponent, final Set<Component> componentsList) {
        final Resource parentResource = (Resource) fetchedComponent;
        Map<String, String> derivedFromMapOfIdToName = new HashMap<>();
        if (CollectionUtils.isNotEmpty(parentResource.getComponentInstances())) {
            componentsList.add(fetchedComponent);
            for (final ComponentInstance componentInstance : parentResource.getComponentInstances()) {
                final Either<Resource, StorageOperationStatus> resourcefetched = toscaOperationFacade
                    .getToscaElement(componentInstance.getComponentUid());
                if (resourcefetched != null && resourcefetched.isLeft()) {
                    final Map<String, String> derivedWithId = resourcefetched.left().value().getDerivedFromMapOfIdToName();
                    if (MapUtils.isNotEmpty(derivedWithId)) {
                        derivedFromMapOfIdToName.putAll(derivedWithId);
                    }
                }
            }
        } else {
            derivedFromMapOfIdToName = parentResource.getDerivedFromMapOfIdToName();
        }
        log.debug("Started derivedFromMapOfIdToName: {}", derivedFromMapOfIdToName);
        return Optional.ofNullable(derivedFromMapOfIdToName);
    }

    /**
     * Creates a resource map and adds it to the import list.
     */
    private void setImports(final List<Map<String, Map<String, String>>> imports, final List<Triple<String, String, Component>> dependencies,
                            final Set<Component> componentsList) {
        componentsList.forEach(component -> setImports(imports, dependencies, component));
    }

    private void setImports(final List<Map<String, Map<String, String>>> imports, final List<Triple<String, String, Component>> dependencies,
                            final Component component) {
        final Map<String, ArtifactDefinition> toscaArtifacts = component.getToscaArtifacts();
        final ArtifactDefinition artifactDefinition = toscaArtifacts.get(ASSET_TOSCA_TEMPLATE);
        if (artifactDefinition != null) {
            final Map<String, String> files = new HashMap<>();
            final String artifactName = artifactDefinition.getArtifactName();
            files.put(IMPORTS_FILE_KEY, artifactName);
            final StringBuilder keyNameBuilder = new StringBuilder();
            keyNameBuilder.append(component.getComponentType().toString().toLowerCase());
            keyNameBuilder.append("-");
            keyNameBuilder.append(component.getName());
            addImports(imports, keyNameBuilder, files);
            dependencies.add(new ImmutableTriple<>(artifactName, artifactDefinition.getEsId(), component));
            if (!ModelConverter.isAtomicComponent(component)) {
                final Map<String, String> interfaceFiles = new HashMap<>();
                interfaceFiles.put(IMPORTS_FILE_KEY, getInterfaceFilename(artifactName));
                keyNameBuilder.append("-interface");
                addImports(imports, keyNameBuilder, interfaceFiles);
            }
        }
    }

    /**
     * Adds the found resource to the import definition list.
     */
    private void addImports(final List<Map<String, Map<String, String>>> imports, final StringBuilder keyNameBuilder,
                            final Map<String, String> files) {
        final String mapKey = keyNameBuilder.toString();
        if (imports.stream().allMatch(stringMapMap -> stringMapMap.get(mapKey) == null)) {
            final Map<String, Map<String, String>> importsListMember = new HashMap<>();
            importsListMember.put(keyNameBuilder.toString(), files);
            imports.add(importsListMember);
        }
    }

    private Either<ToscaTemplate, ToscaError> convertNodeType(Map<String, Component> componentsCache, Component component, ToscaTemplate toscaNode,
                                                              Map<String, ToscaNodeType> nodeTypes) {
        return convertInterfaceNodeType(componentsCache, component, toscaNode, nodeTypes, false);
    }

    public Either<ToscaTemplate, ToscaError> convertInterfaceNodeType(Map<String, Component> componentsCache, Component component,
                                                                      ToscaTemplate toscaNode, Map<String, ToscaNodeType> nodeTypes,
                                                                      boolean isAssociatedComponent) {
        log.debug("start convert node type for {}", component.getUniqueId());
        ToscaNodeType toscaNodeType = createNodeType(component);
        Either<Map<String, InterfaceDefinition>, StorageOperationStatus> lifecycleTypeEither = interfaceLifecycleOperation
            .getAllInterfaceLifecycleTypes(component.getModel());
        if (lifecycleTypeEither.isRight() && !StorageOperationStatus.NOT_FOUND.equals(lifecycleTypeEither.right().value())) {
            log.debug("Failed to fetch all interface types :", lifecycleTypeEither.right().value());
            return Either.right(ToscaError.GENERAL_ERROR);
        }
        if (lifecycleTypeEither.isLeft()) {
            List<String> allGlobalInterfaceTypes = lifecycleTypeEither.left().value().values().stream().map(InterfaceDataDefinition::getType)
                .collect(Collectors.toList());
            toscaNode.setInterface_types(interfacesOperationsConverter.addInterfaceTypeElement(component, allGlobalInterfaceTypes));
        }
        final var dataTypesEither = applicationDataTypeCache.getAll(component.getModel());
        if (dataTypesEither.isRight()) {
            log.debug("Failed to fetch all data types :", dataTypesEither.right().value());
            return Either.right(ToscaError.GENERAL_ERROR);
        }
        Map<String, DataTypeDefinition> dataTypes = dataTypesEither.left().value();
        List<InputDefinition> inputDef = component.getInputs();
        interfacesOperationsConverter.addInterfaceDefinitionElement(component, toscaNodeType, dataTypes, isAssociatedComponent);
        final var toscaAttributeMap = convertToToscaAttributes(component.getAttributes(), dataTypes);
        if (!toscaAttributeMap.isEmpty()) {
            toscaNodeType.setAttributes(toscaAttributeMap);
        }
        final var mergedProperties = convertInputsToProperties(dataTypes, inputDef, component.getUniqueId());
        if (CollectionUtils.isNotEmpty(component.getProperties())) {
            List<PropertyDefinition> properties = component.getProperties();
            Map<String, ToscaProperty> convertedProperties = properties.stream()
                .map(propertyDefinition -> resolvePropertyValueFromInput(propertyDefinition, component.getInputs())).collect(Collectors
                    .toMap(PropertyDataDefinition::getName,
                        property -> propertyConvertor.convertProperty(dataTypes, property, PropertyConvertor.PropertyType.PROPERTY)));
            // merge component properties and inputs properties
            mergedProperties.putAll(convertedProperties);
        }
        if (MapUtils.isNotEmpty(mergedProperties)) {
            toscaNodeType.setProperties(mergedProperties);
        }
        /* convert private data_types */
        List<DataTypeDefinition> privateDataTypes = component.getDataTypes();
        if (CollectionUtils.isNotEmpty(privateDataTypes)) {
            Map<String, ToscaDataType> toscaDataTypeMap = new HashMap<>();
            for (DataTypeDefinition dataType : privateDataTypes) {
                log.debug("Emitting private data type: component.name={} dataType.name={}",
                    component.getNormalizedName(), dataType.getName());
                ToscaDataType toscaDataType = new ToscaDataType();
                toscaDataType.setDerived_from(dataType.getDerivedFromName());
                toscaDataType.setDescription(dataType.getDescription());
                toscaDataType.setVersion(dataType.getVersion());
                if (CollectionUtils.isNotEmpty(dataType.getProperties())) {
                    toscaDataType.setProperties(dataType.getProperties().stream()
                        .collect(Collectors.toMap(
                            PropertyDataDefinition::getName,
                            s -> propertyConvertor.convertProperty(dataTypes, s, PropertyType.PROPERTY),
                            (toscaPropertyTobeValidated, toscaProperty) -> validateToscaProperty(privateDataTypes, toscaPropertyTobeValidated,
                                toscaProperty)
                        )));
                }
                toscaDataTypeMap.put(dataType.getName(), toscaDataType);
            }
            toscaNode.setData_types(toscaDataTypeMap);
        }

        // Extracted to method for code reuse
        return convertReqCapAndTypeName(componentsCache, component, toscaNode, nodeTypes, toscaNodeType, dataTypes);
    }

    private ToscaProperty validateToscaProperty(final List<DataTypeDefinition> privateDataTypes, final ToscaProperty toscaPropertyTobeValidated,
                                                final ToscaProperty toscaProperty) {
        final Optional<DataTypeDefinition> match = privateDataTypes.stream()
            .filter(dataType -> dataType.getName().equals(toscaPropertyTobeValidated.getType())).findFirst();
        return match.isPresent() ? toscaPropertyTobeValidated : toscaProperty;
    }

    private Map<String, ToscaAttribute> convertToToscaAttributes(final List<AttributeDefinition> attributeList,
                                                                 final Map<String, DataTypeDefinition> dataTypes) {
        if (CollectionUtils.isEmpty(attributeList)) {
            return Collections.emptyMap();
        }
        final AttributeConverter converter = new AttributeConverter(dataTypes);
        final Map<String, ToscaAttribute> toscaAttributeMap = new HashMap<>();
        for (final AttributeDefinition attributeDefinition : attributeList) {
            toscaAttributeMap.put(attributeDefinition.getName(), converter.convert(attributeDefinition));
        }
        return toscaAttributeMap;
    }

    private Either<ToscaTemplate, ToscaError> convertReqCapAndTypeName(Map<String, Component> componentsCache,
                                                                       Component component, ToscaTemplate toscaNode,
                                                                       Map<String, ToscaNodeType> nodeTypes,
                                                                       ToscaNodeType toscaNodeType,
                                                                       Map<String, DataTypeDefinition> dataTypes) {
        Either<ToscaNodeType, ToscaError> capabilities = convertCapabilities(componentsCache, component, toscaNodeType,
            dataTypes);
        if (capabilities.isRight()) {
            return Either.right(capabilities.right().value());
        }
        toscaNodeType = capabilities.left().value();
        log.debug("Capabilities converted for {}", component.getUniqueId());

        Either<ToscaNodeType, ToscaError> requirements = capabilityRequirementConverter
            .convertRequirements(componentsCache, component, toscaNodeType);
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

    private Either<Map<String, ToscaNodeTemplate>, ToscaError> convertNodeTemplates(final Component component,
                                                                                    final Map<String, Component> componentCache,
                                                                                    final Map<String, DataTypeDefinition> dataTypes,
                                                                                    final ToscaTopolgyTemplate topologyTemplate) {

        final Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = component.getComponentInstancesProperties();
        final Map<String, List<ComponentInstanceAttribute>> componentInstancesAttributes = component.getComponentInstancesAttributes();
        final Map<String, List<ComponentInstanceInput>> componentInstancesInputs = component.getComponentInstancesInputs();
        final Map<String, List<ComponentInstanceInterface>> componentInstanceInterfaces = component.getComponentInstancesInterfaces();
        final List<RequirementCapabilityRelDef> componentInstancesRelations = component.getComponentInstancesRelations();

        Either<Map<String, ToscaNodeTemplate>, ToscaError> convertNodeTemplatesRes = null;
        log.debug("start convert topology template for {} for type {}", component.getUniqueId(), component.getComponentType());
        final Map<String, ToscaNodeTemplate> nodeTemplates = new HashMap<>();

        Map<String, ToscaGroupTemplate> groupsMap = null;
        for (final ComponentInstance componentInstance : component.getComponentInstances()) {
            ToscaNodeTemplate nodeTemplate = new ToscaNodeTemplate();
            if (MapUtils.isNotEmpty(componentInstance.getToscaArtifacts())) {
                nodeTemplate.setArtifacts(convertToNodeTemplateArtifacts(componentInstance.getToscaArtifacts()));
            }
            if (componentInstance.getMinOccurrences() != null && componentInstance.getMaxOccurrences() != null) {
                List<Object> occur = new ArrayList<>();
                occur.add(parseToIntIfPossible(componentInstance.getMinOccurrences()));
                occur.add(parseToIntIfPossible(componentInstance.getMaxOccurrences()));
                nodeTemplate.setOccurrences(occur);
            }
            if (componentInstance.getInstanceCount() != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                Object obj = convertToToscaObject(componentInstance.getInstanceCount());
                if (obj != null) {
                    Map<String, String> map = objectMapper.convertValue(obj, Map.class);
                    nodeTemplate.setInstance_count(map);
                }
            }
            nodeTemplate.setType(componentInstance.getToscaComponentName());
            nodeTemplate.setDirectives(componentInstance.getDirectives());
            nodeTemplate.setNode_filter(convertToNodeTemplateNodeFilterComponent(componentInstance.getNodeFilter()));

            final Either<Component, Boolean> originComponentRes = capabilityRequirementConverter
                .getOriginComponent(componentCache, componentInstance);
            if (originComponentRes.isRight()) {
                convertNodeTemplatesRes = Either.right(ToscaError.NODE_TYPE_REQUIREMENT_ERROR);
                break;
            }
            final Either<ToscaNodeTemplate, ToscaError> requirements = convertComponentInstanceRequirements(component, componentInstance,
                componentInstancesRelations, nodeTemplate, originComponentRes.left().value(), componentCache);
            if (requirements.isRight()) {
                convertNodeTemplatesRes = Either.right(requirements.right().value());
                break;
            }
            final String instanceUniqueId = componentInstance.getUniqueId();
            log.debug("Component instance Requirements converted for instance {}", instanceUniqueId);

            nodeTemplate = requirements.left().value();

            final Component originalComponent = componentCache.get(componentInstance.getActualComponentUid());

            if (componentInstance.getOriginType() == OriginTypeEnum.ServiceProxy) {
                final Component componentOfProxy = componentCache.get(componentInstance.getComponentUid());
                nodeTemplate.setMetadata(convertMetadata(componentOfProxy, true, componentInstance));
            } else {
                nodeTemplate.setMetadata(convertMetadata(originalComponent, true, componentInstance));
            }

            final Either<ToscaNodeTemplate, ToscaError> capabilities =
                capabilityRequirementConverter.convertComponentInstanceCapabilities(componentInstance, dataTypes, nodeTemplate);
            if (capabilities.isRight()) {
                convertNodeTemplatesRes = Either.right(capabilities.right().value());
                break;
            }
            log.debug("Component instance Capabilities converted for instance {}", instanceUniqueId);

            nodeTemplate = capabilities.left().value();
            final Map<String, Object> props = new HashMap<>();
            final Map<String, Object> attribs = new HashMap<>();

            if (originalComponent.getComponentType() == ComponentTypeEnum.RESOURCE) {
                // Adds the properties of parent component to map
                addPropertiesOfParentComponent(dataTypes, originalComponent, props);
                addAttributesOfParentComponent(originalComponent, attribs);
            }

            if (null != componentInstancesProperties && componentInstancesProperties.containsKey(instanceUniqueId)) {
                addPropertiesOfComponentInstance(componentInstancesProperties, dataTypes, instanceUniqueId, props);
            }
            if (null != componentInstancesAttributes && componentInstancesAttributes.containsKey(instanceUniqueId)) {
                addAttributesOfComponentInstance(componentInstancesAttributes, instanceUniqueId, attribs);
            }

            if (componentInstancesInputs != null
                && componentInstancesInputs.containsKey(instanceUniqueId)
                && !isComponentOfTypeServiceProxy(componentInstance)) {
                //For service proxy the inputs are already handled under instance properties above
                addComponentInstanceInputs(dataTypes, componentInstancesInputs, instanceUniqueId, props);
            }

            //M3[00001] - NODE TEMPLATE INTERFACES  - START
            handleInstanceInterfaces(componentInstanceInterfaces, componentInstance, dataTypes, nodeTemplate, instanceUniqueId, component);
            //M3[00001] - NODE TEMPLATE INTERFACES  - END
            if (MapUtils.isNotEmpty(props)) {
                nodeTemplate.setProperties(props);
            }
            if (MapUtils.isNotEmpty(attribs)) {
                nodeTemplate.setAttributes(attribs);
            }

            final List<GroupInstance> groupInstances = componentInstance.getGroupInstances();
            if (CollectionUtils.isNotEmpty(groupInstances)) {
                if (groupsMap == null) {
                    groupsMap = new HashMap<>();
                }
                for (final GroupInstance groupInst : groupInstances) {
                    if (CollectionUtils.isNotEmpty(groupInst.getArtifacts())) {
                        groupsMap.put(groupInst.getName(), groupExportParser.getToscaGroupTemplate(groupInst, componentInstance.getInvariantName()));
                    }
                }
            }

            nodeTemplates.put(componentInstance.getName(), nodeTemplate);
        }
        if (groupsMap != null) {
            log.debug("instance groups added");
            topologyTemplate.addGroups(groupsMap);
        }
        if (component.getComponentType() == ComponentTypeEnum.SERVICE && isNotEmpty(
            ((Service) component).getForwardingPaths())) {
            log.debug("Starting converting paths for component {}, name {}", component.getUniqueId(), component.getName());
            ForwardingPathToscaUtil
                .addForwardingPaths((Service) component, nodeTemplates, capabilityRequirementConverter, componentCache, toscaOperationFacade);
            log.debug("Finished converting paths for component {}, name {}", component.getUniqueId(), component.getName());
        }
        if (convertNodeTemplatesRes == null) {
            convertNodeTemplatesRes = Either.left(nodeTemplates);
        }
        log.debug("finish convert topology template for {} for type {}", component.getUniqueId(), component.getComponentType());
        return convertNodeTemplatesRes;
    }

    private Object convertToToscaObject(String value) {
        try {
            ToscaMapValueConverter mapConverterInst = ToscaMapValueConverter.getInstance();
            JsonParser jsonParser = new JsonParser();
            StringReader reader = new StringReader(value);
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(true);
            JsonElement jsonElement = jsonParser.parse(jsonReader);
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObj = jsonElement.getAsJsonObject();
                if (jsonObj.entrySet().size() == 1 && jsonObj.has(ToscaFunctions.GET_INPUT.getFunctionName())) {
                    return mapConverterInst.handleComplexJsonValue(jsonElement);
                }
            }
            return null;
        } catch (Exception e) {
            log.debug("convertToToscaValue failed to parse json value :", e);
            return null;
        }
    }

    private Object parseToIntIfPossible(final String value) {
        final Integer intValue = Ints.tryParse(value);
        return intValue == null ? value : intValue;
    }

    private void handleInstanceInterfaces(
        Map<String, List<ComponentInstanceInterface>> componentInstanceInterfaces,
        ComponentInstance componentInstance, Map<String, DataTypeDefinition> dataTypes, ToscaNodeTemplate nodeTemplate,
        String instanceUniqueId,
        Component parentComponent) {

        if (MapUtils.isEmpty(componentInstanceInterfaces)
            || !componentInstanceInterfaces.containsKey(instanceUniqueId)) {
            nodeTemplate.setInterfaces(null);
            return;
        }

        final List<ComponentInstanceInterface> currServiceInterfaces =
            componentInstanceInterfaces.get(instanceUniqueId);

        final Map<String, InterfaceDefinition> tmpInterfaces = new HashMap<>();
        currServiceInterfaces.forEach(instInterface -> tmpInterfaces.put(instInterface
            .getUniqueId(), instInterface));

        final Map<String, Object> interfaceMap = interfacesOperationsConverter
            .getInterfacesMap(parentComponent, componentInstance, tmpInterfaces, dataTypes, isComponentOfTypeServiceProxy(componentInstance),
                isComponentOfTypeServiceProxy(componentInstance));

        interfacesOperationsConverter.removeInterfacesWithoutOperations(interfaceMap);
        nodeTemplate.setInterfaces(MapUtils.isEmpty(interfaceMap) ? null : interfaceMap);
    }

    private boolean isComponentOfTypeServiceProxy(ComponentInstance componentInstance) {
        return Objects.nonNull(componentInstance.getOriginType())
            && componentInstance.getOriginType().getValue().equals("Service Proxy");
    }

    private void addComponentInstanceInputs(Map<String, DataTypeDefinition> dataTypes,
                                            Map<String, List<ComponentInstanceInput>> componentInstancesInputs,
                                            String instanceUniqueId, Map<String, Object> props) {

        List<ComponentInstanceInput> instanceInputsList = componentInstancesInputs.get(instanceUniqueId);
        if (instanceInputsList != null) {
            instanceInputsList.forEach(input -> {
                Supplier<String> supplier = () -> input.getValue() != null && !Objects.isNull(input.getValue()) ? input.getValue()
                    : input.getDefaultValue();
                propertyConvertor.convertAndAddValue(dataTypes, props, input, supplier);
            });
        }
    }

    private void addPropertiesOfComponentInstance(final Map<String, List<ComponentInstanceProperty>> componentInstancesProperties,
                                                  final Map<String, DataTypeDefinition> dataTypes,
                                                  final String instanceUniqueId,
                                                  final Map<String, Object> props) {

        if (isNotEmpty(componentInstancesProperties)) {
            componentInstancesProperties.get(instanceUniqueId)
                // Converts and adds each value to property map
                .forEach(prop -> propertyConvertor.convertAndAddValue(dataTypes, props, prop, prop::getValue));
        }
    }

    private void addAttributesOfComponentInstance(final Map<String, List<ComponentInstanceAttribute>> componentInstancesAttributes,
                                                  final String instanceUniqueId,
                                                  final Map<String, Object> attribs) {

        if (isNotEmpty(componentInstancesAttributes) && componentInstancesAttributes.containsKey(instanceUniqueId)) {
            componentInstancesAttributes.get(instanceUniqueId)
                .forEach(attributeDefinition -> attributeConverter.convertAndAddValue(attribs, attributeDefinition));
        }
    }

    private void addPropertiesOfParentComponent(Map<String, DataTypeDefinition> dataTypes,
                                                Component componentOfInstance, Map<String, Object> props) {

        List<PropertyDefinition> componentProperties = componentOfInstance.getProperties();
        if (isNotEmpty(componentProperties)) {
            componentProperties.stream()
                // Filters out properties with empty default values
                .filter(prop -> StringUtils.isNotEmpty(prop.getDefaultValue()))
                // Converts and adds each value to property map
                .forEach(prop -> propertyConvertor.convertAndAddValue(dataTypes, props, prop, prop::getDefaultValue));
        }
    }

    private void addAttributesOfParentComponent(final Component componentOfInstance, final Map<String, Object> attribs) {

        final List<AttributeDefinition> componentAttributes = componentOfInstance.getAttributes();
        if (isNotEmpty(componentAttributes)) {
            componentAttributes.stream()
                // Filters out Attributes with empty default values
                .filter(attrib -> StringUtils.isNotEmpty(attrib.getDefaultValue()))
                // Converts and adds each value to attribute map
                .forEach(attributeDefinition -> attributeConverter.convertAndAddValue(attribs, attributeDefinition));
        }
    }

    private ToscaNodeType createNodeType(Component component) {
        ToscaNodeType toscaNodeType = new ToscaNodeType();
        if (ModelConverter.isAtomicComponent(component)) {
            if (((Resource) component).getDerivedFrom() != null) {
                toscaNodeType.setDerived_from(((Resource) component).getDerivedFrom().get(0));
            }
            toscaNodeType.setDescription(component.getDescription());
        } else {
            String derivedFrom = null != component.getDerivedFromGenericType() ? component.getDerivedFromGenericType()
                : NATIVE_ROOT;
            toscaNodeType.setDerived_from(derivedFrom);
        }
        return toscaNodeType;
    }

    private Either<Map<String, Object>, ToscaError> createProxyInterfaceTypes(Component container) {

        Map<String, Object> proxyInterfaceTypes = new HashMap<>();
        Either<Map<String, Object>, ToscaError> res = Either.left(proxyInterfaceTypes);
        List<ComponentInstance> componentInstances = container.getComponentInstances();
        if (CollectionUtils.isEmpty(componentInstances)) {
            return res;
        }
        Map<String, ComponentInstance> serviceProxyInstanceList = new HashMap<>();
        componentInstances.stream()
            .filter(this::isComponentOfTypeServiceProxy)
            .forEach(inst -> serviceProxyInstanceList.put(inst.getToscaComponentName(), inst));
        if (MapUtils.isEmpty(serviceProxyInstanceList)) {
            return res;
        }
        for (Entry<String, ComponentInstance> entryProxy : serviceProxyInstanceList.entrySet()) {
            Component serviceComponent;
            ComponentParametersView componentParametersView = new ComponentParametersView();
            componentParametersView.disableAll();
            componentParametersView.setIgnoreInterfaces(false);
            Either<Component, StorageOperationStatus> service = toscaOperationFacade
                .getToscaElement(entryProxy.getValue().getSourceModelUid(), componentParametersView);
            if (service.isRight()) {
                log.debug("Failed to fetch original service component with id {} for instance {}",
                    entryProxy.getValue().getSourceModelUid(), entryProxy.getValue().getName());
                return Either.right(ToscaError.GENERAL_ERROR);
            } else {
                serviceComponent = service.left().value();
            }

            Either<Map<String, InterfaceDefinition>, StorageOperationStatus> lifecycleTypeEither =
                interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(serviceComponent.getModel());
            if (lifecycleTypeEither.isRight()) {
                log.debug("Failed to retrieve global interface types :", lifecycleTypeEither.right().value());
                return Either.right(ToscaError.GENERAL_ERROR);
            }

            List<String> allGlobalInterfaceTypes = lifecycleTypeEither.left().value().values().stream()
                .map(InterfaceDataDefinition::getType)
                .collect(Collectors.toList());
            //Add interface types for local interfaces in the original service component for proxy
            Map<String, Object> localInterfaceTypes = interfacesOperationsConverter.addInterfaceTypeElement(serviceComponent,
                allGlobalInterfaceTypes);
            if (MapUtils.isNotEmpty(localInterfaceTypes)) {
                proxyInterfaceTypes.putAll(localInterfaceTypes);
            }

        }
        return Either.left(proxyInterfaceTypes);
    }

    private Either<Map<String, ToscaNodeType>, ToscaError> createProxyNodeTypes(Map<String, Component> componentCache,
                                                                                Component container) {

        Map<String, ToscaNodeType> nodeTypesMap = new HashMap<>();
        Either<Map<String, ToscaNodeType>, ToscaError> res = Either.left(nodeTypesMap);

        List<ComponentInstance> componentInstances = container.getComponentInstances();

        if (componentInstances == null || componentInstances.isEmpty()) {
            return res;
        }
        Map<String, ComponentInstance> serviceProxyInstanceList = new HashMap<>();
        List<ComponentInstance> proxyInst = componentInstances.stream()
            .filter(p -> p.getOriginType().name().equals(OriginTypeEnum.ServiceProxy.name()))
            .collect(Collectors.toList());
        if (proxyInst != null && !proxyInst.isEmpty()) {
            for (ComponentInstance inst : proxyInst) {
                serviceProxyInstanceList.put(inst.getToscaComponentName(), inst);
            }
        }

        if (serviceProxyInstanceList.isEmpty()) {
            return res;
        }
        Either<Resource, StorageOperationStatus> serviceProxyOrigin = toscaOperationFacade
            .getLatestByName("serviceProxy", null);
        if (serviceProxyOrigin.isRight()) {
            log.debug("Failed to fetch normative service proxy resource by tosca name, error {}",
                serviceProxyOrigin.right().value());
            return Either.right(ToscaError.NOT_SUPPORTED_TOSCA_TYPE);
        }
        Component origComponent = serviceProxyOrigin.left().value();

        for (Entry<String, ComponentInstance> entryProxy : serviceProxyInstanceList.entrySet()) {
            Component serviceComponent = null;
            ComponentParametersView componentParametersView = new ComponentParametersView();
            componentParametersView.disableAll();
            componentParametersView.setIgnoreCategories(false);
            componentParametersView.setIgnoreProperties(false);
            componentParametersView.setIgnoreInputs(false);
            componentParametersView.setIgnoreInterfaces(false);
            componentParametersView.setIgnoreRequirements(false);
            Either<Component, StorageOperationStatus> service = toscaOperationFacade
                .getToscaElement(entryProxy.getValue().getSourceModelUid(), componentParametersView);
            if (service.isRight()) {
                log.debug("Failed to fetch resource with id {} for instance {}",
                    entryProxy.getValue().getSourceModelUid(), entryProxy.getValue().getName());
            } else {
                serviceComponent = service.left().value();
            }

            ToscaNodeType toscaNodeType = createProxyNodeType(componentCache, origComponent, serviceComponent,
                entryProxy.getValue());
            nodeTypesMap.put(entryProxy.getKey(), toscaNodeType);
        }

        return Either.left(nodeTypesMap);
    }

    private void createServiceSubstitutionNodeTypes(final Map<String, Component> componentCache,
                                                    final Component container, final ToscaTemplate toscaNode) {
        final List<ComponentInstance> componentInstances = container.getComponentInstances();

        if (CollectionUtils.isEmpty(componentInstances)) {
            return;
        }
        final List<ComponentInstance> serviceSubstitutionInstanceList = componentInstances.stream()
            .filter(p -> p.getOriginType().name().equals(OriginTypeEnum.ServiceSubstitution.name()))
            .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(serviceSubstitutionInstanceList)) {
            for (ComponentInstance inst : serviceSubstitutionInstanceList) {
                final Map<String, ToscaNodeType> nodeTypes =
                    toscaNode.getNode_types() == null ? new HashMap<>() : toscaNode.getNode_types();
                convertInterfaceNodeType(new HashMap<>(), componentCache.get(inst.getSourceModelUid()), toscaNode,
                    nodeTypes, true);
            }
        }
    }

    private ToscaNodeType createProxyNodeType(Map<String, Component> componentCache, Component origComponent,
                                              Component proxyComponent, ComponentInstance componentInstance) {
        ToscaNodeType toscaNodeType = new ToscaNodeType();
        String derivedFrom = ((Resource) origComponent).getToscaResourceName();

        toscaNodeType.setDerived_from(derivedFrom);
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> dataTypesEither = applicationDataTypeCache.getAll(
            origComponent.getModel());
        if (dataTypesEither.isRight()) {
            log.debug("Failed to retrieve all data types {}", dataTypesEither.right().value());
        }
        Map<String, DataTypeDefinition> dataTypes = dataTypesEither.left().value();
        Map<String, ToscaCapability> capabilities = this.capabilityRequirementConverter
            .convertProxyCapabilities(componentCache, componentInstance, dataTypes);

        if (MapUtils.isNotEmpty(capabilities)) {
            toscaNodeType.setCapabilities(capabilities);
        }
        List<Map<String, ToscaRequirement>> proxyNodeTypeRequirements = this.capabilityRequirementConverter
            .convertProxyRequirements(componentCache, componentInstance);
        if (CollectionUtils.isNotEmpty(proxyNodeTypeRequirements)) {
            toscaNodeType.setRequirements(proxyNodeTypeRequirements);
        }
        Optional<Map<String, ToscaProperty>> proxyProperties = getProxyNodeTypeProperties(proxyComponent, dataTypes);
        proxyProperties.ifPresent(toscaNodeType::setProperties);

        Map<String, Object> interfaceMap = new HashMap<>();
        if (MapUtils.isEmpty(componentInstance.getInterfaces())) {
            final Optional<Map<String, Object>> proxyInterfaces = getProxyNodeTypeInterfaces(proxyComponent, dataTypes);
            if (proxyInterfaces.isPresent()) {
                interfaceMap = proxyInterfaces.get();
            }
        } else {
            interfaceMap = interfacesOperationsConverter
                .getInterfacesMapFromComponentInstance(proxyComponent, componentInstance, dataTypes, false, false);

        }
        interfacesOperationsConverter.removeInterfacesWithoutOperations(interfaceMap);
        toscaNodeType.setInterfaces(MapUtils.isEmpty(interfaceMap) ? null : interfaceMap);

        return toscaNodeType;
    }

    private Either<ToscaNodeTemplate, ToscaError> convertComponentInstanceRequirements(Component component,
                                                                                       ComponentInstance componentInstance,
                                                                                       List<RequirementCapabilityRelDef> relations,
                                                                                       ToscaNodeTemplate nodeTypeTemplate,
                                                                                       Component originComponent,
                                                                                       Map<String, Component> componentCache) {

        final List<RequirementCapabilityRelDef> requirementDefinitionList = filterRequirements(componentInstance,
            relations);
        if (isNotEmpty(requirementDefinitionList)) {
            try {
                final List<Map<String, ToscaTemplateRequirement>> toscaRequirements = buildRequirements(component, componentInstance,
                    requirementDefinitionList, originComponent, componentCache);
                if (!toscaRequirements.isEmpty()) {
                    nodeTypeTemplate.setRequirements(toscaRequirements);
                }
            } catch (final Exception e) {
                log.debug("Failed to convert component instance requirements for the component instance {}. ",
                    componentInstance.getName(), e);
                return Either.right(ToscaError.NODE_TYPE_REQUIREMENT_ERROR);
            }
        }
        log.debug("Finished to convert requirements for the node type {} ", componentInstance.getName());
        return Either.left(nodeTypeTemplate);
    }

    private List<Map<String, ToscaTemplateRequirement>> buildRequirements(final Component component,
                                                                          final ComponentInstance componentInstance,
                                                                          final List<RequirementCapabilityRelDef> filteredRelations,
                                                                          final Component originComponent,
                                                                          final Map<String, Component> componentCache)
        throws ToscaExportException {

        final List<Map<String, ToscaTemplateRequirement>> toscaRequirements = new ArrayList<>();
        for (RequirementCapabilityRelDef relationshipDefinition : filteredRelations) {
            final Map<String, ToscaTemplateRequirement> toscaTemplateRequirementMap =
                buildRequirement(componentInstance, originComponent, component.getComponentInstances(), relationshipDefinition, componentCache);
            toscaRequirements.add(toscaTemplateRequirementMap);
        }

        return toscaRequirements;
    }

    private List<RequirementCapabilityRelDef> filterRequirements(ComponentInstance componentInstance,
                                                                 List<RequirementCapabilityRelDef> relations) {
        return relations.stream()
            .filter(p -> componentInstance.getUniqueId().equals(p.getFromNode())).collect(Collectors.toList());
    }

    private Map<String, ToscaTemplateRequirement> buildRequirement(final ComponentInstance fromInstance,
                                                                   final Component fromOriginComponent,
                                                                   final List<ComponentInstance> instancesList,
                                                                   final RequirementCapabilityRelDef relationshipDefinition,
                                                                   final Map<String, Component> componentCache)
        throws ToscaExportException {

        final Map<String, List<RequirementDefinition>> reqMap = fromOriginComponent.getRequirements();
        final CapabilityRequirementRelationship capabilityRequirementRelationship = relationshipDefinition
            .getRelationships().get(0);
        final RelationshipInfo relationshipInfo = capabilityRequirementRelationship.getRelation();

        final ComponentInstance toInstance = instancesList.stream()
            .filter(i -> relationshipDefinition.getToNode().equals(i.getUniqueId()))
            .findFirst().orElse(null);
        if (toInstance == null) {
            final String errorMsg = String
                .format("Failed to find a relation from the node %s to the node %s", fromInstance.getName(),
                    relationshipDefinition.getToNode());
            log.debug(errorMsg);
            throw new ToscaExportException(errorMsg);
        }
        final Optional<RequirementDefinition> reqOpt =
            findRequirement(fromOriginComponent, reqMap, relationshipInfo, fromInstance.getUniqueId());
        if (reqOpt.isEmpty()) {
            final String errorMsg = String
                .format("Failed to find a requirement with uniqueId %s on a component with uniqueId %s",
                    relationshipInfo.getRequirementUid(), fromOriginComponent.getUniqueId());
            log.debug(errorMsg);
            throw new ToscaExportException(errorMsg);
        }
        final ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreComponentInstances(false);
        filter.setIgnoreCapabilities(false);
        filter.setIgnoreGroups(false);
        final Either<Component, StorageOperationStatus> getOriginRes =
            toscaOperationFacade.getToscaElement(toInstance.getActualComponentUid(), filter);
        if (getOriginRes.isRight()) {
            final String errorMsg = String.format(
                "Failed to build substituted name for the requirement %s. "
                    + "Failed to get an origin component with uniqueId %s",
                reqOpt.get().getName(), toInstance.getActualComponentUid());
            log.debug(errorMsg);
            throw new ToscaExportException(errorMsg);
        }
        final Component toOriginComponent = getOriginRes.left().value();
        Optional<CapabilityDefinition> capOpt = toOriginComponent.getCapabilities().get(reqOpt.get().getCapability()).stream()
            .filter(c -> isCapabilityBelongToRelation(relationshipInfo, c)).findFirst();
        if (capOpt.isEmpty()) {
            capOpt = findCapability(relationshipInfo, toOriginComponent, fromOriginComponent, reqOpt.get());
            if (capOpt.isEmpty()) {
                final String errorMsg = String
                    .format("Failed to find a capability with name %s on a component with uniqueId %s",
                        relationshipInfo.getCapability(), fromOriginComponent.getUniqueId());
                log.debug(errorMsg);
                throw new ToscaExportException(errorMsg);
            }
        }
        return buildRequirement(fromOriginComponent, toOriginComponent, capOpt.get(), reqOpt.get(),
            capabilityRequirementRelationship, toInstance, componentCache);
    }

    private boolean isCapabilityBelongToRelation(RelationshipInfo reqAndRelationshipPair,
                                                 CapabilityDefinition capability) {
        return capability.getName().equals(reqAndRelationshipPair.getCapability()) && (capability.getOwnerId() != null
            && capability.getOwnerId().equals(reqAndRelationshipPair.getCapabilityOwnerId()));
    }

    private Optional<CapabilityDefinition> findCapability(RelationshipInfo reqAndRelationshipPair,
                                                          Component toOriginComponent, Component fromOriginComponent,
                                                          RequirementDefinition requirement) {
        Optional<CapabilityDefinition> cap = toOriginComponent.getCapabilities().get(requirement.getCapability())
            .stream().filter(c -> c.getType().equals(requirement.getCapability())).findFirst();
        if (!cap.isPresent()) {
            log.debug("Failed to find a capability with name {} on a component with uniqueId {}",
                reqAndRelationshipPair.getCapability(), fromOriginComponent.getUniqueId());
        }
        return cap;
    }

    private Map<String, ToscaTemplateRequirement> buildRequirement(final Component fromOriginComponent,
                                                                   final Component toOriginComponent,
                                                                   final CapabilityDefinition capability,
                                                                   final RequirementDefinition requirement,
                                                                   final CapabilityRequirementRelationship capabilityRequirementRelationship,
                                                                   final ComponentInstance toInstance,
                                                                   final Map<String, Component> componentCache)
        throws ToscaExportException {

        List<String> reducedPath = capability.getPath();
        if (capability.getOwnerId() != null) {
            reducedPath = capabilityRequirementConverter
                .getReducedPathByOwner(capability.getPath(), capability.getOwnerId());
        }
        final RelationshipInfo relationshipInfo = capabilityRequirementRelationship.getRelation();
        final Either<String, Boolean> capabilityNameEither = capabilityRequirementConverter.buildSubstitutedName(componentCache,
            toOriginComponent, reducedPath, relationshipInfo.getCapability(), capability.getPreviousName(), capability.getExternalName());
        if (capabilityNameEither.isRight()) {
            final String errorMsg = String.format(
                "Failed to build a substituted capability name for the capability with name %s on a component with uniqueId %s",
                capabilityRequirementRelationship.getCapability(), toOriginComponent.getUniqueId());
            log.debug(
                errorMsg);
            throw new ToscaExportException(errorMsg);
        }
        final Either<String, Boolean> requirementNameEither = capabilityRequirementConverter
            .buildSubstitutedName(componentCache, fromOriginComponent,
                requirement.getPath(), relationshipInfo.getRequirement(), requirement.getPreviousName(), requirement.getExternalName());
        if (requirementNameEither.isRight()) {
            final String errorMsg = String.format("Failed to build a substituted requirement name for the requirement "
                    + "with name %s on a component with uniqueId %s",
                capabilityRequirementRelationship.getRequirement(), fromOriginComponent.getUniqueId());
            log.debug(errorMsg);
            throw new ToscaExportException(errorMsg);
        }
        final ToscaTemplateRequirement toscaRequirement = new ToscaTemplateRequirement();
        final Map<String, ToscaTemplateRequirement> toscaReqMap = new HashMap<>();
        toscaRequirement.setNode(toInstance.getName());
        toscaRequirement.setCapability(capabilityNameEither.left().value());
        if (isNotEmpty(capabilityRequirementRelationship.getOperations())) {
            toscaRequirement.setRelationship(new ToscaRelationshipBuilder().from(capabilityRequirementRelationship));
        }
        toscaReqMap.put(requirementNameEither.left().value(), toscaRequirement);
        return toscaReqMap;
    }

    private Optional<RequirementDefinition> findRequirement(Component fromOriginComponent,
                                                            Map<String, List<RequirementDefinition>> reqMap,
                                                            RelationshipInfo reqAndRelationshipPair,
                                                            String fromInstanceId) {
        for (List<RequirementDefinition> reqList : reqMap.values()) {
            Optional<RequirementDefinition> reqOpt = reqList.stream().filter(
                    r -> isRequirementBelongToRelation(fromOriginComponent, reqAndRelationshipPair, r, fromInstanceId))
                .findFirst();
            if (reqOpt.isPresent()) {
                return reqOpt;
            }
        }
        return Optional.empty();
    }

    /**
     * Allows detecting the requirement belonging to the received relationship The detection logic is: A requirement belongs to a relationship IF
     * 1.The name of the requirement equals to the "requirement" field of the relation; AND 2. In case of a non-atomic resource, OwnerId of the
     * requirement equals to requirementOwnerId of the relation OR uniqueId of toInstance equals to capabilityOwnerId of the relation
     */
    private boolean isRequirementBelongToRelation(Component originComponent, RelationshipInfo reqAndRelationshipPair,
                                                  RequirementDefinition requirement, String fromInstanceId) {
        if (!StringUtils.equals(requirement.getName(), reqAndRelationshipPair.getRequirement())) {
            log.debug("Failed to find a requirement with name {} and  reqAndRelationshipPair {}", requirement.getName(),
                reqAndRelationshipPair.getRequirement());
            return false;
        }
        return ModelConverter.isAtomicComponent(originComponent) || isRequirementBelongToOwner(reqAndRelationshipPair, requirement, fromInstanceId,
            originComponent);
    }

    private boolean isRequirementBelongToOwner(RelationshipInfo reqAndRelationshipPair, RequirementDefinition requirement, String fromInstanceId,
                                               Component originComponent) {
        return StringUtils.equals(requirement.getOwnerId(), reqAndRelationshipPair.getRequirementOwnerId()) || (
            isCvfc(originComponent) && StringUtils.equals(fromInstanceId, reqAndRelationshipPair.getRequirementOwnerId()) || StringUtils
                .equals(requirement.getOwnerId(), originComponent.getUniqueId()));
    }

    private boolean isCvfc(Component component) {
        return component.getComponentType() == ComponentTypeEnum.RESOURCE && ((Resource) component).getResourceType() == ResourceTypeEnum.CVFC;
    }

    private Either<Map<String, String[]>, ToscaError> convertSubstitutionMappingCapabilities(final Component component,
                                                                                             final Map<String, Component> componentCache) {
        Either<Map<String, String[]>, ToscaError> toscaCapabilitiesRes =
            capabilityRequirementConverter.convertSubstitutionMappingCapabilities(componentCache, component);
        if (toscaCapabilitiesRes.isRight()) {
            log.debug("Failed convert capabilities for the component {}. ", component.getName());
            return Either.right(toscaCapabilitiesRes.right().value());
        }
        if (isNotEmpty(toscaCapabilitiesRes.left().value())) {
            log.debug("Finish convert capabilities for the component {}. ", component.getName());
            return Either.left(toscaCapabilitiesRes.left().value());
        }
        log.debug("Finished to convert capabilities for the component {}. ", component.getName());

        return Either.left(Collections.emptyMap());
    }

    private Either<ToscaNodeType, ToscaError> convertCapabilities(Map<String, Component> componentsCache, Component component, ToscaNodeType nodeType,
                                                                  Map<String, DataTypeDefinition> dataTypes) {
        Map<String, ToscaCapability> toscaCapabilities = capabilityRequirementConverter.convertCapabilities(componentsCache, component, dataTypes);
        if (!toscaCapabilities.isEmpty()) {
            nodeType.setCapabilities(toscaCapabilities);
        }
        log.debug("Finish convert Capabilities for node type");
        return Either.left(nodeType);
    }

    private Map<String, ToscaTemplateArtifact> convertToNodeTemplateArtifacts(Map<String, ToscaArtifactDataDefinition> artifacts) {
        if (artifacts == null) {
            return null;
        }
        Map<String, ToscaTemplateArtifact> arts = new HashMap<>();
        for (Map.Entry<String, ToscaArtifactDataDefinition> entry : artifacts.entrySet()) {
            ToscaTemplateArtifact artifact = new ToscaTemplateArtifact();
            artifact.setFile(entry.getValue().getFile());
            artifact.setType(entry.getValue().getType());
            artifact.setProperties(entry.getValue().getProperties());
            arts.put(entry.getKey(), artifact);
        }
        return arts;
    }

    private NodeFilter convertToNodeTemplateNodeFilterComponent(CINodeFilterDataDefinition inNodeFilter) {
        if (inNodeFilter == null) {
            return null;
        }
        NodeFilter nodeFilter = new NodeFilter();
        ListDataDefinition<RequirementNodeFilterCapabilityDataDefinition> origCapabilities = inNodeFilter.getCapabilities();
        ListDataDefinition<RequirementNodeFilterPropertyDataDefinition> origProperties = inNodeFilter.getProperties();
        List<Map<String, CapabilityFilter>> capabilitiesCopy = new ArrayList<>();
        List<Map<String, List<Object>>> propertiesCopy = new ArrayList<>();
        copyNodeFilterCapabilitiesTemplate(origCapabilities, capabilitiesCopy);
        copyNodeFilterProperties(origProperties, propertiesCopy);
        if (CollectionUtils.isNotEmpty(capabilitiesCopy)) {
            nodeFilter.setCapabilities(capabilitiesCopy);
        }
        if (CollectionUtils.isNotEmpty(propertiesCopy)) {
            nodeFilter.setProperties(propertiesCopy);
        }
        nodeFilter.setTosca_id(cloneToscaId(inNodeFilter.getTosca_id()));
        nodeFilter = (NodeFilter) cloneObjectFromYml(nodeFilter, NodeFilter.class);
        return nodeFilter;
    }

    private NodeFilter convertToSubstitutionFilterComponent(final SubstitutionFilterDataDefinition substitutionFilterDataDefinition) {
        if (substitutionFilterDataDefinition == null) {
            return null;
        }
        NodeFilter nodeFilter = new NodeFilter();
        ListDataDefinition<RequirementSubstitutionFilterPropertyDataDefinition> origProperties = substitutionFilterDataDefinition.getProperties();
        List<Map<String, List<Object>>> propertiesCopy = new ArrayList<>();
        copySubstitutionFilterProperties(origProperties, propertiesCopy);
        if (CollectionUtils.isNotEmpty(propertiesCopy)) {
            nodeFilter.setProperties(propertiesCopy);
        }
        nodeFilter.setTosca_id(cloneToscaId(substitutionFilterDataDefinition.getTosca_id()));
        return (NodeFilter) cloneObjectFromYml(nodeFilter, NodeFilter.class);
    }

    private Object cloneToscaId(Object toscaId) {
        return Objects.isNull(toscaId) ? null : cloneObjectFromYml(toscaId, toscaId.getClass());
    }

    private Object cloneObjectFromYml(Object objToClone, Class classOfObj) {
        String objectAsYml = yamlUtil.objectToYaml(objToClone);
        return yamlUtil.yamlToObject(objectAsYml, classOfObj);
    }

    private void copyNodeFilterCapabilitiesTemplate(ListDataDefinition<RequirementNodeFilterCapabilityDataDefinition> origCapabilities,
                                                    List<Map<String, CapabilityFilter>> capabilitiesCopy) {
        if (origCapabilities == null || origCapabilities.getListToscaDataDefinition() == null || origCapabilities.getListToscaDataDefinition()
            .isEmpty()) {
            return;
        }
        for (RequirementNodeFilterCapabilityDataDefinition capability : origCapabilities.getListToscaDataDefinition()) {
            Map<String, CapabilityFilter> capabilityFilterCopyMap = new HashMap<>();
            CapabilityFilter capabilityFilter = new CapabilityFilter();
            List<Map<String, List<Object>>> propertiesCopy = new ArrayList<>();
            copyNodeFilterProperties(capability.getProperties(), propertiesCopy);
            capabilityFilter.setProperties(propertiesCopy);
            capabilityFilterCopyMap.put(capability.getName(), capabilityFilter);
            capabilitiesCopy.add(capabilityFilterCopyMap);
        }
    }

    private void copyNodeFilterProperties(ListDataDefinition<RequirementNodeFilterPropertyDataDefinition> origProperties,
                                          List<Map<String, List<Object>>> propertiesCopy) {
        if (origProperties == null || origProperties.getListToscaDataDefinition() == null || origProperties.isEmpty()) {
            return;
        }
        Map<String, List<Object>> propertyMapCopy = new HashMap<>();
        for (RequirementNodeFilterPropertyDataDefinition propertyDataDefinition : origProperties.getListToscaDataDefinition()) {
            for (String propertyInfoEntry : propertyDataDefinition.getConstraints()) {
                Map<String, List<Object>> propertyValObj = new YamlUtil().yamlToObject(propertyInfoEntry, Map.class);
                String propertyName = propertyDataDefinition.getName();
                if (propertyMapCopy.containsKey(propertyName)) {
                    addPropertyConstraintValueToList(propertyName, propertyValObj, propertyMapCopy.get(propertyName));
                } else {
                    if (propertyName != null) {
                        List<Object> propsList = new ArrayList<>();
                        addPropertyConstraintValueToList(propertyName, propertyValObj, propsList);
                        propertyMapCopy.put(propertyName, propsList);
                    } else {
                        propertyMapCopy.putAll(propertyValObj);
                    }
                }
            }
        }
        propertyMapCopy.entrySet().stream().forEach(entry -> addCalculatedConstraintsIntoPropertiesList(propertiesCopy, entry));
    }

    private void copySubstitutionFilterProperties(final ListDataDefinition<RequirementSubstitutionFilterPropertyDataDefinition> origProperties,
                                                  final List<Map<String, List<Object>>> propertiesCopy) {
        if (origProperties == null || origProperties.getListToscaDataDefinition() == null || origProperties.isEmpty()) {
            return;
        }
        final Map<String, List<Object>> propertyMapCopy = new HashMap<>();
        for (final RequirementSubstitutionFilterPropertyDataDefinition propertyDataDefinition : origProperties.getListToscaDataDefinition()) {
            for (final String propertyInfoEntry : propertyDataDefinition.getConstraints()) {
                final Map<String, List<Object>> propertyValObj = new YamlUtil().yamlToObject(propertyInfoEntry, Map.class);
                final String propertyName = propertyDataDefinition.getName();
                if (propertyMapCopy.containsKey(propertyName)) {
                    addPropertyConstraintValueToList(propertyName, propertyValObj, propertyMapCopy.get(propertyName));
                } else {
                    if (propertyName != null) {
                        final List<Object> propsList = new ArrayList<>();
                        addPropertyConstraintValueToList(propertyName, propertyValObj, propsList);
                        propertyMapCopy.put(propertyName, propsList);
                    } else {
                        propertyMapCopy.putAll(propertyValObj);
                    }
                }
            }
        }
        propertyMapCopy.entrySet().forEach(entry -> addCalculatedConstraintsIntoPropertiesList(propertiesCopy, entry));
    }

    private void addPropertyConstraintValueToList(String propertyName, Map<String, List<Object>> propertyValObj, List<Object> propsList) {
        if (propertyValObj.containsKey(propertyName)) {
            propsList.add(propertyValObj.get(propertyName));
        } else {
            propsList.add(propertyValObj);
        }
    }

    private void addCalculatedConstraintsIntoPropertiesList(List<Map<String, List<Object>>> propertiesCopy, Entry<String, List<Object>> entry) {
        Map<String, List<Object>> tempMap = new HashMap<>();
        tempMap.put(entry.getKey(), entry.getValue());
        propertiesCopy.add(tempMap);
    }

    private Map<String, String[]> buildSubstitutionMappingPropertyMapping(final Component component) {
        if (component == null || CollectionUtils.isEmpty(component.getInputs())) {
            return Collections.emptyMap();
        }
        return component.getInputs().stream().filter(InputDefinition::isMappedToComponentProperty).map(PropertyDataDefinition::getName)
            .collect(Collectors.toMap(inputName -> inputName, inputName -> new String[]{inputName}, (inputName1, inputName2) -> inputName1));
    }

    private Map<String, String[]> buildSubstitutionMappingAttributesMapping(final Component component) {
        if (component == null || CollectionUtils.isEmpty(component.getOutputs())) {
            return Collections.emptyMap();
        }
        return component.getOutputs().stream().map(AttributeDataDefinition::getName)
            .collect(Collectors.toMap(outputName -> outputName, outputName -> new String[]{outputName}, (outputName1, outputName2) -> outputName1));
    }

    private Optional<Map<String, ToscaProperty>> getProxyNodeTypeProperties(Component proxyComponent, Map<String, DataTypeDefinition> dataTypes) {
        if (Objects.isNull(proxyComponent)) {
            return Optional.empty();
        }
        final var proxyProperties = convertInputsToProperties(dataTypes, proxyComponent.getInputs(), proxyComponent.getUniqueId());
        if (CollectionUtils.isNotEmpty(proxyComponent.getProperties())) {
            proxyProperties.putAll(proxyComponent.getProperties().stream()
                .map(propertyDefinition -> resolvePropertyValueFromInput(propertyDefinition, proxyComponent.getInputs())).collect(Collectors
                    .toMap(PropertyDataDefinition::getName,
                        property -> propertyConvertor.convertProperty(dataTypes, property, PropertyType.PROPERTY))));
        }
        return MapUtils.isNotEmpty(proxyProperties) ? Optional.of(proxyProperties) : Optional.empty();
    }

    private Map<String, ToscaProperty> convertInputsToProperties(Map<String, DataTypeDefinition> dataTypes, List<InputDefinition> componentInputs,
                                                             String componentUniqueId) {
        if (CollectionUtils.isEmpty(componentInputs)) {
            return new HashMap<>();
        }
        return componentInputs.stream().filter(input -> componentUniqueId.equals(input.getInstanceUniqueId()))
            .collect(Collectors.toMap(InputDefinition::getName, i -> propertyConvertor.convertProperty(dataTypes, i, PropertyType.INPUT)));
    }

    private Optional<Map<String, Object>> getProxyNodeTypeInterfaces(Component proxyComponent, Map<String, DataTypeDefinition> dataTypes) {
        if (Objects.isNull(proxyComponent) || MapUtils.isEmpty(proxyComponent.getInterfaces())) {
            return Optional.empty();
        }
        Map<String, InterfaceDefinition> proxyComponentInterfaces = proxyComponent.getInterfaces();
        //Unset artifact path for operation implementation for proxy node types as for operations with artifacts it is

        // always available in the proxy node template
        removeOperationImplementationForProxyNodeType(proxyComponentInterfaces);
        return Optional
            .ofNullable(interfacesOperationsConverter.getInterfacesMap(proxyComponent, null, proxyComponentInterfaces, dataTypes, false, false));
    }

    private static class CustomRepresenter extends Representer {

        CustomRepresenter() {
            super();
            this.representers.put(ToscaPropertyAssignment.class, new RepresentToscaPropertyAssignment());
            this.representers.put(ToscaAttribute.class, new RepresentToscaAttribute());
            // null representer is exceptional and it is stored as an instance

            // variable.
            this.nullRepresenter = new RepresentNull();
        }

        public boolean validateGetInputValue(final Object valueObj) {
            if (!(valueObj instanceof List) && !(valueObj instanceof String)) {
                return false;
            }
            if (valueObj instanceof List) {
                return ((List) valueObj).size() > 1;
            }
            return true;
        }

        public boolean validateGetPropertyOrAttributeValue(final Object valueObj) {
            if (valueObj instanceof List) {
                return ((List) valueObj).size() > 1;
            }
            return false;
        }

        @Override
        protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
            if (propertyValue == null) {
                return null;
            }
            // skip not relevant for Tosca property
            if ("dependencies".equals(property.getName())) {
                return null;
            }
            if (javaBean instanceof ToscaRelationshipTemplate && "name".equals(property.getName())) {
                return null;
            }
            removeDefaultP(propertyValue);
            NodeTuple defaultNode = super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
            if (javaBean instanceof ToscaTopolgyTemplate && "relationshipTemplates".equals(property.getName())) {
                return new NodeTuple(representData("relationship_templates"), defaultNode.getValueNode());
            }
            return "_defaultp_".equals(property.getName()) ? new NodeTuple(representData("default"), defaultNode.getValueNode()) : defaultNode;
        }

        private void removeDefaultP(final Object propertyValue) {
            if (propertyValue instanceof Map) {
                final Map mapPropertyValue = ((Map) propertyValue);
                final Iterator<Entry> iter = mapPropertyValue.entrySet().iterator();
                Object defaultValue = null;
                while (iter.hasNext()) {
                    final Map.Entry entry = iter.next();
                    if ("_defaultp_".equals(entry.getKey())) {
                        defaultValue = entry.getValue();
                        iter.remove();
                    } else if (entry.getValue() instanceof Map) {
                        removeDefaultP(entry.getValue());
                    }
                }
                if (defaultValue != null) {
                    mapPropertyValue.putIfAbsent("default", defaultValue);
                }
            }
        }

        @Override
        protected MappingNode representJavaBean(Set<Property> properties, Object javaBean) {
            // remove the bean type from the output yaml (!! ...)
            if (!classTags.containsKey(javaBean.getClass())) {
                addClassTag(javaBean.getClass(), Tag.MAP);
            }
            return super.representJavaBean(properties, javaBean);
        }

        private class RepresentToscaAttribute implements Represent {

            @Override
            public Node representData(Object data) {
                final ToscaAttribute toscaAttribute = (ToscaAttribute) data;
                return represent(toscaAttribute.asToscaMap());
            }
        }

        private class RepresentToscaPropertyAssignment implements Represent {

            public Node representData(Object data) {
                final ToscaPropertyAssignment toscaOperationAssignment = (ToscaPropertyAssignment) data;
                if (toscaOperationAssignment.getValue() instanceof String) {
                    final String stringValue = (String) toscaOperationAssignment.getValue();
                    if (isPropertyOrAttributeFunction(stringValue)) {
                        return representGetAttribute(stringValue);
                    }
                    return representScalar(Tag.STR, stringValue);
                }
                return represent(null);
            }

            public Node representGetAttribute(final String getAttributeFunction) {
                return represent(new Yaml().load(getAttributeFunction));
            }

            public boolean isPropertyOrAttributeFunction(final String value) {
                try {
                    final Yaml yaml = new Yaml();
                    final Object yamlObj = yaml.load(value);
                    if (!(yamlObj instanceof Map)) {
                        return false;
                    }
                    final Map<String, Object> getAttributeMap = (Map) yamlObj;
                    if (getAttributeMap.size() != 1) {
                        return false;
                    }
                    final List<String> functionList = Arrays
                        .asList(GET_ATTRIBUTE.getFunctionName(), GET_INPUT.getFunctionName(), GET_PROPERTY.getFunctionName());
                    final Optional<String> function = getAttributeMap.keySet().stream()
                        .filter(key -> functionList.stream().anyMatch(function1 -> function1.equals(key))).findFirst();
                    if (function.isEmpty()) {
                        return false;
                    }
                    final String functionName = function.get();
                    final Object getAttributeValueObj = getAttributeMap.get(functionName);
                    if (GET_INPUT.getFunctionName().equals(functionName)) {
                        return validateGetInputValue(getAttributeValueObj);
                    } else {
                        return validateGetPropertyOrAttributeValue(getAttributeValueObj);
                    }
                } catch (final Exception ignored) {
                    return false;
                }
            }
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
        protected Set<Property> createPropertySet(Class type, BeanAccess bAccess) {
            Collection<Property> fields = getPropertiesMap(type, BeanAccess.FIELD).values();
            return new LinkedHashSet<>(fields);
        }
    }

    private Configuration getConfiguration() {
        return ConfigurationManager.getConfigurationManager().getConfiguration();
    }

}
