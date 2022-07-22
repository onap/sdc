/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.tosca.constraints.ConstraintType;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.exception.NsdException;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.model.Nsd;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.model.VnfDescriptor;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.tosca.yaml.ToscaTemplateYamlGenerator;
import org.openecomp.sdc.be.tosca.ToscaError;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.tosca.model.SubstitutionMapping;
import org.openecomp.sdc.be.tosca.model.ToscaNodeTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyConstraint;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyConstraintValidValues;
import org.openecomp.sdc.be.tosca.model.ToscaRequirement;
import org.openecomp.sdc.be.tosca.model.ToscaTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaTemplateRequirement;
import org.openecomp.sdc.be.tosca.model.ToscaTopolgyTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

@org.springframework.stereotype.Component("nsDescriptorGenerator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NsDescriptorGeneratorImpl implements NsDescriptorGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NsDescriptorGeneratorImpl.class);
    private static final String TOSCA_VERSION = "tosca_simple_yaml_1_1";
    private static final String NS_TOSCA_TYPE = "tosca.nodes.nfv.NS";
    private static final List<Map<String, Map<String, String>>> DEFAULT_IMPORTS = ConfigurationManager.getConfigurationManager().getConfiguration()
        .getDefaultImports();
    private static final List<String> PROPERTIES_TO_EXCLUDE_FROM_ETSI_SOL_NSD_NS_NODE_TYPE = Arrays
        .asList("cds_model_name", "cds_model_version", "skip_post_instantiation_configuration", "controller_actor");
    private static final List<String> ETSI_SOL_NSD_NS_NODE_TYPE_PROPERTIES = Arrays
        .asList("descriptor_id", "designer", "version", "name", "invariant_id", "flavour_id", "ns_profile", "service_availability_level");
    private static final List<String> PROPERTIES_TO_EXCLUDE_FROM_ETSI_SOL_NSD_NS_NODE_TEMPLATE = Arrays
        .asList("nf_function", "nf_role", "nf_naming_code", "nf_type", "nf_naming", "availability_zone_max_count", "min_instances", "max_instances",
            "multi_stage_design", "sdnc_model_name", "sdnc_model_version", "sdnc_artifact_name", "skip_post_instantiation_configuration",
            "controller_actor");
    private final ToscaExportHandler toscaExportHandler;
    private final ObjectProvider<ToscaTemplateYamlGenerator> toscaTemplateYamlGeneratorProvider;

    public NsDescriptorGeneratorImpl(final ToscaExportHandler toscaExportHandler,
                                     final ObjectProvider<ToscaTemplateYamlGenerator> toscaTemplateYamlGeneratorProvider) {
        this.toscaExportHandler = toscaExportHandler;
        this.toscaTemplateYamlGeneratorProvider = toscaTemplateYamlGeneratorProvider;
    }

    public Optional<Nsd> generate(final Component component, final List<VnfDescriptor> vnfDescriptorList) throws NsdException {
        if (!ComponentTypeEnum.SERVICE.equals(component.getComponentType())) {
            return Optional.empty();
        }
        final ToscaTemplate toscaTemplate = createNetworkServiceDescriptor(component, vnfDescriptorList);
        final ToscaNodeType nsNodeType = toscaTemplate.getNode_types().values().stream()
            .filter(toscaNodeType -> NS_TOSCA_TYPE.equals(toscaNodeType.getDerived_from())).findFirst().orElse(null);
        if (nsNodeType == null) {
            return Optional.empty();
        }
        return Optional.of(buildNsd(toscaTemplate, nsNodeType));
    }

    private Nsd buildNsd(final ToscaTemplate toscaTemplate, final ToscaNodeType nsNodeType) {
        final Nsd nsd = new Nsd();
        nsd.setDesigner(getProperty(nsNodeType, Nsd.DESIGNER_PROPERTY));
        nsd.setVersion(getProperty(nsNodeType, Nsd.VERSION_PROPERTY));
        nsd.setName(getProperty(nsNodeType, Nsd.NAME_PROPERTY));
        nsd.setInvariantId(getProperty(nsNodeType, Nsd.INVARIANT_ID_PROPERTY));
        final ToscaTemplateYamlGenerator yamlParserProvider = toscaTemplateYamlGeneratorProvider.getObject(toscaTemplate);
        final byte[] contents = yamlParserProvider.parseToYamlString().getBytes();
        nsd.setContents(contents);
        final List<String> interfaceImplementations = getInterfaceImplementations(toscaTemplate);
        nsd.setArtifactReferences(interfaceImplementations);
        return nsd;
    }

    private List<String> getInterfaceImplementations(final ToscaTemplate template) {
        if (template.getTopology_template().getNode_templates() == null) {
            return Collections.emptyList();
        }
        final List<String> interfaceImplementations = new ArrayList<>();
        final Collection<ToscaNodeTemplate> nodeTemplates = template.getTopology_template().getNode_templates().values();
        nodeTemplates.stream().filter(toscaNodeTemplate -> toscaNodeTemplate.getInterfaces() != null).forEach(
            toscaNodeTemplate -> toscaNodeTemplate.getInterfaces().values()
                .forEach(interfaceInstance -> interfaceImplementations.addAll(getInterfaceImplementations(interfaceInstance))));
        return interfaceImplementations;
    }

    private Collection<String> getInterfaceImplementations(final Object interfaceInstance) {
        final Collection<String> interfaceImplementations = new ArrayList<>();
        if (interfaceInstance instanceof Map) {
            for (final Object value : ((Map<?, ?>) interfaceInstance).values()) {
                if (value instanceof Map && ((Map<?, ?>) value).get("implementation") != null) {
                    interfaceImplementations.add(((Map<?, ?>) value).get("implementation").toString());
                }
            }
        }
        return interfaceImplementations;
    }

    private String getProperty(final ToscaNodeType nodeType, final String propertyName) {
        final ToscaProperty toscaProperty = nodeType.getProperties().get(propertyName);
        final String errorMsg = String.format("Property '%s' must be defined and must have a valid values constraint", propertyName);
        final String returnValueOnError = "unknown";
        if (toscaProperty == null || CollectionUtils.isEmpty(toscaProperty.getConstraints())) {
            LOGGER.error(errorMsg);
            return returnValueOnError;
        }
        final ToscaPropertyConstraint toscaPropertyConstraint = toscaProperty.getConstraints().get(0);
        if (ConstraintType.VALID_VALUES != toscaPropertyConstraint.getConstraintType()) {
            LOGGER.error(errorMsg);
            return returnValueOnError;
        }
        final ToscaPropertyConstraintValidValues validValuesConstraint = (ToscaPropertyConstraintValidValues) toscaPropertyConstraint;
        final List<String> validValues = validValuesConstraint.getValidValues();
        if (CollectionUtils.isEmpty(validValues)) {
            LOGGER.error(errorMsg);
            return returnValueOnError;
        }
        return validValues.get(0);
    }

    private ToscaTemplate createNetworkServiceDescriptor(final Component component, final List<VnfDescriptor> vnfDescriptorList) throws NsdException {
        final ToscaTemplate componentToscaTemplate = parseToToscaTemplate(component);
        final ToscaTemplate componentToscaTemplateInterface = exportComponentInterfaceAsToscaTemplate(component);
        final Entry<String, ToscaNodeType> firstNodeTypeEntry = componentToscaTemplateInterface.getNode_types().entrySet().stream().findFirst()
            .orElse(null);
        if (firstNodeTypeEntry == null) {
            throw new NsdException("Could not find abstract Service type");
        }
        final String nsNodeTypeName = firstNodeTypeEntry.getKey();
        final ToscaNodeType nsNodeType = firstNodeTypeEntry.getValue();
        final Map<String, ToscaNodeType> nodeTypeMap = new HashMap<>();
        nodeTypeMap.put(nsNodeTypeName, createEtsiSolNsNodeType(nsNodeType, componentToscaTemplate));
        if (componentToscaTemplate.getNode_types() == null) {
            componentToscaTemplate.setNode_types(nodeTypeMap);
        } else {
            componentToscaTemplate.getNode_types().putAll(nodeTypeMap);
        }
        handleNodeTemplates(componentToscaTemplate);
        removeOnapAndEtsiNsdPropertiesFromInputs(componentToscaTemplate);
        handleSubstitutionMappings(componentToscaTemplate, nsNodeTypeName);
        final Map<String, ToscaNodeTemplate> nodeTemplates = new HashMap<>();
        nodeTemplates.put(nsNodeTypeName,
            createNodeTemplateForNsNodeType(nsNodeTypeName, componentToscaTemplateInterface.getNode_types().get(nsNodeTypeName)));
        if (componentToscaTemplate.getTopology_template().getNode_templates() == null) {
            componentToscaTemplate.getTopology_template().setNode_templates(nodeTemplates);
        } else {
            setNodeTemplateTypesForVnfs(componentToscaTemplate, vnfDescriptorList);
            componentToscaTemplate.getTopology_template().getNode_templates().putAll(nodeTemplates);
        }
        removeOnapMetaData(componentToscaTemplate);
        setDefaultImportsForEtsiSolNsNsd(componentToscaTemplate, vnfDescriptorList);
        return componentToscaTemplate;
    }

    private void handleSubstitutionMappings(final ToscaTemplate componentToscaTemplate, final String nsNodeTypeName) {
        final SubstitutionMapping substitutionMapping = new SubstitutionMapping();
        substitutionMapping.setNode_type(nsNodeTypeName);
        final SubstitutionMapping onapSubstitutionMapping = componentToscaTemplate.getTopology_template().getSubstitution_mappings();
        if (onapSubstitutionMapping != null && onapSubstitutionMapping.getRequirements() != null) {
            substitutionMapping.setRequirements(adjustRequirementNamesToMatchVnfd(onapSubstitutionMapping.getRequirements()));
        }
        componentToscaTemplate.getTopology_template().setSubstitution_mappings(substitutionMapping);
    }

    private Map<String, String[]> adjustRequirementNamesToMatchVnfd(final Map<String, String[]> requirements) {
        for (final Map.Entry<String, String[]> entry : requirements.entrySet()) {
            try {
                final String[] adjustedValue = {entry.getValue()[0], entry.getValue()[1].substring(entry.getValue()[1].lastIndexOf('.') + 1)};
                entry.setValue(adjustedValue);
            } catch (final ArrayIndexOutOfBoundsException exception) {
                LOGGER.error("Malformed requirement: {}", entry);
            }
        }
        return requirements;
    }

    private void setNodeTemplateTypesForVnfs(final ToscaTemplate template, final List<VnfDescriptor> vnfDescriptorList) {
        if (CollectionUtils.isEmpty(vnfDescriptorList)) {
            return;
        }
        final Map<String, ToscaNodeTemplate> nodeTemplateMap = template.getTopology_template().getNode_templates();
        if (MapUtils.isEmpty(nodeTemplateMap)) {
            return;
        }
        nodeTemplateMap.forEach(
            (key, toscaNodeTemplate) -> vnfDescriptorList.stream().filter(vnfDescriptor -> key.equals(vnfDescriptor.getName())).findFirst()
                .ifPresent(vnfDescriptor -> toscaNodeTemplate.setType(vnfDescriptor.getNodeType())));
    }

    private void handleNodeTemplates(final ToscaTemplate template) {
        final Map<String, ToscaNodeTemplate> nodeTemplateMap = template.getTopology_template().getNode_templates();
        if (MapUtils.isEmpty(nodeTemplateMap)) {
            return;
        }
        for (final Entry<String, ToscaNodeTemplate> nodeTemplate : nodeTemplateMap.entrySet()) {
            setPropertiesForNodeTemplate(nodeTemplate);
            setRequirementsForNodeTemplate(nodeTemplate);
            removeCapabilitiesFromNodeTemplate(nodeTemplate);
        }
    }

    private void setPropertiesForNodeTemplate(final Entry<String, ToscaNodeTemplate> nodeTemplate) {
        final Map<String, Object> propertyMap = nodeTemplate.getValue().getProperties();
        if (MapUtils.isEmpty(propertyMap)) {
            nodeTemplate.getValue().setProperties(null);
            return;
        }
        final Map<String, Object> editedPropertyMap = new HashMap<>();
        for (final Entry<String, Object> property : propertyMap.entrySet()) {
            if (!PROPERTIES_TO_EXCLUDE_FROM_ETSI_SOL_NSD_NS_NODE_TEMPLATE.contains(property.getKey()) && propertyIsDefinedInNodeType(
                property.getKey())) {
                editedPropertyMap.put(property.getKey(), property.getValue());
            }
        }
        if (editedPropertyMap.isEmpty()) {
            nodeTemplate.getValue().setProperties(null);
        } else {
            nodeTemplate.getValue().setProperties(editedPropertyMap);
        }
    }

    private void setRequirementsForNodeTemplate(final Entry<String, ToscaNodeTemplate> nodeTemplateMap) {
        final List<Map<String, ToscaTemplateRequirement>> requirementAssignments = nodeTemplateMap.getValue().getRequirements();
        if (requirementAssignments != null) {
            final List<Map<String, ToscaTemplateRequirement>> requirementAssignmentsMatchingVnfdRequirements = new ArrayList<>();
            for (final Map<String, ToscaTemplateRequirement> requirementAssignment : requirementAssignments) {
                final Map<String, ToscaTemplateRequirement> requirementAssignmentMatchingVnfd =
                    requirementAssignment.entrySet().stream()
                        .collect(Collectors.toMap(entry -> entry.getKey().substring(entry.getKey().lastIndexOf('.') + 1), Map.Entry::getValue));
                requirementAssignmentsMatchingVnfdRequirements.add(requirementAssignmentMatchingVnfd);
            }
            nodeTemplateMap.getValue().setRequirements(requirementAssignmentsMatchingVnfdRequirements);
        }
    }


    private void removeCapabilitiesFromNodeTemplate(final Entry<String, ToscaNodeTemplate> nodeTemplate) {
        nodeTemplate.getValue().setCapabilities(null);
    }

    private void removeOnapAndEtsiNsdPropertiesFromInputs(final ToscaTemplate template) {
        final ToscaTopolgyTemplate topologyTemplate = template.getTopology_template();
        final Map<String, ToscaProperty> inputMap = topologyTemplate.getInputs();

        if (MapUtils.isNotEmpty(inputMap)) {
            inputMap.entrySet().removeIf(entry -> PROPERTIES_TO_EXCLUDE_FROM_ETSI_SOL_NSD_NS_NODE_TYPE.contains(entry.getKey())
                || ETSI_SOL_NSD_NS_NODE_TYPE_PROPERTIES.contains(entry.getKey()));
        }
        if (MapUtils.isEmpty(inputMap)) {
            topologyTemplate.setInputs(null);
        }
    }

    private void removeOnapMetaData(final ToscaTemplate template) {
        template.setMetadata(null);
        final Map<String, ToscaNodeTemplate> nodeTemplateMap = template.getTopology_template().getNode_templates();
        if (MapUtils.isEmpty(nodeTemplateMap)) {
            return;
        }
        nodeTemplateMap.values().forEach(toscaNodeTemplate -> toscaNodeTemplate.setMetadata(null));
    }

    private void setDefaultImportsForEtsiSolNsNsd(final ToscaTemplate template, final List<VnfDescriptor> vnfDescriptorList) {
        final List<Map<String, Map<String, String>>> importEntryMap = new ArrayList<>();
        final Map<String, Map<String, String>> defaultImportEntryMap = generateDefaultImportEntry();
        if (MapUtils.isNotEmpty(defaultImportEntryMap)) {
            importEntryMap.add(defaultImportEntryMap);
        }
        if (CollectionUtils.isNotEmpty(vnfDescriptorList)) {
            for (final VnfDescriptor vnfDescriptor : vnfDescriptorList) {
                final Map<String, String> vnfImportChildEntry = new HashMap<>();
                vnfImportChildEntry.put("file", vnfDescriptor.getVnfdFileName());
                final Map<String, Map<String, String>> vnfdImportVnfdEntry = new HashMap<>();
                vnfdImportVnfdEntry.put(vnfDescriptor.getName(), vnfImportChildEntry);
                importEntryMap.add(vnfdImportVnfdEntry);
            }
        }
        template.setImports(importEntryMap);
    }

    private Map<String, Map<String, String>> generateDefaultImportEntry() {
        return Map.of("etsi_nfv_sol001_nsd_types", Map.of("file", "etsi_nfv_sol001_nsd_types.yaml"));
    }

    private ToscaNodeType createEtsiSolNsNodeType(final ToscaNodeType nsNodeType, final ToscaTemplate componentToscaTemplate) {
        final ToscaNodeType toscaNodeType = new ToscaNodeType();
        toscaNodeType.setDerived_from(NS_TOSCA_TYPE);
        final Map<String, ToscaProperty> propertiesInNsNodeType = nsNodeType.getProperties();
        for (final Entry<String, ToscaProperty> property : propertiesInNsNodeType.entrySet()) {
            final ToscaProperty toscaProperty = property.getValue();
            if (toscaProperty.getDefaultp() != null && ETSI_SOL_NSD_NS_NODE_TYPE_PROPERTIES.contains(property.getKey())) {
                final ToscaPropertyConstraintValidValues constraint = new ToscaPropertyConstraintValidValues(
                    Collections.singletonList(toscaProperty.getDefaultp().toString()));
                toscaProperty.setConstraints(Collections.singletonList(constraint));
            }
        }
        propertiesInNsNodeType.entrySet().removeIf(entry -> PROPERTIES_TO_EXCLUDE_FROM_ETSI_SOL_NSD_NS_NODE_TYPE.contains(entry.getKey()));
        toscaNodeType.setProperties(propertiesInNsNodeType);

        final List<Map<String, ToscaRequirement>> requirementsInNsNodeType = getRequirementsForNsNodeType(nsNodeType.getRequirements(),
            componentToscaTemplate);
        if (!requirementsInNsNodeType.isEmpty()) {
            toscaNodeType.setRequirements(requirementsInNsNodeType);
        }

        return toscaNodeType;
    }

    private List<Map<String, ToscaRequirement>> getRequirementsForNsNodeType(final List<Map<String, ToscaRequirement>> requirements,
                                                                             final ToscaTemplate componentToscaTemplate) {
        final Map<String, String[]> requirementsInSubstitutionMapping = componentToscaTemplate.getTopology_template().getSubstitution_mappings()
            .getRequirements();
        if (requirements == null || MapUtils.isEmpty(requirementsInSubstitutionMapping)) {
            return Collections.emptyList();
        }
        final List<Map<String, ToscaRequirement>> requirementsToAdd = new ArrayList<>();
        for (final Map<String, ToscaRequirement> requirementMap : requirements) {
            final Map<String, ToscaRequirement> neededRequirements = requirementMap.entrySet().stream()
                .filter(entry -> requirementsInSubstitutionMapping.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
            if (!neededRequirements.isEmpty()) {
                requirementsToAdd.add(neededRequirements);
            }
        }
        return requirementsToAdd;

    }

    private boolean propertyIsDefinedInNodeType(final String propertyName) {
        // This will achieve what we want for now, but will look into a more generic solution which would involve

        // checking the node_type definition in the VNFD
        return !propertyName.equals("additional_parameters");
    }

    private ToscaNodeTemplate createNodeTemplateForNsNodeType(final String nodeType, final ToscaNodeType toscaNodeType) {
        final ToscaNodeTemplate nodeTemplate = new ToscaNodeTemplate();
        nodeTemplate.setType(nodeType);
        final Map<String, ToscaProperty> properties = toscaNodeType.getProperties();
        final Map<String, Object> nodeTemplateProperties = new HashMap<>();
        for (final Entry<String, ToscaProperty> property : properties.entrySet()) {
            final var defaultp = property.getValue().getDefaultp();
            if (defaultp != null) {
                nodeTemplateProperties.put(property.getKey(), defaultp);
            }
        }
        if (MapUtils.isNotEmpty(nodeTemplateProperties)) {
            nodeTemplate.setProperties(nodeTemplateProperties);
        }
        final Map<String, Object> interfaces = toscaNodeType.getInterfaces();
        if (MapUtils.isNotEmpty(interfaces)) {
            for (final Entry<String, Object> nodeInterface : interfaces.entrySet()) {
                if ("Nslcm".equals(nodeInterface.getKey()) && nodeInterface.getValue() instanceof Map) {
                    ((Map<?, ?>) nodeInterface.getValue()).remove("type");
                }
            }
            nodeTemplate.setInterfaces(interfaces);
        }
        return nodeTemplate;
    }

    private ToscaTemplate parseToToscaTemplate(final Component component) throws NsdException {
        final Either<ToscaTemplate, ToscaError> toscaTemplateRes = toscaExportHandler.convertToToscaTemplate(component);
        if (toscaTemplateRes.isRight()) {
            String errorMsg = String
                .format("Could not parse component '%s' to tosca template. Error '%s'", component.getName(), toscaTemplateRes.right().value().name());
            throw new NsdException(errorMsg);
        }
        return toscaTemplateRes.left().value();
    }

    private ToscaTemplate exportComponentInterfaceAsToscaTemplate(final Component component) throws NsdException {
        if (null == DEFAULT_IMPORTS) {
            throw new NsdException("Could not load default CSAR imports from configuration");
        }
        final ToscaTemplate toscaTemplate = new ToscaTemplate(TOSCA_VERSION);
        toscaTemplate.setImports(new ArrayList<>(DEFAULT_IMPORTS));
        final Either<ToscaTemplate, ToscaError> toscaTemplateRes = toscaExportHandler
            .convertInterfaceNodeType(new HashMap<>(), component, toscaTemplate, new HashMap<>(), false);
        if (toscaTemplateRes.isRight()) {
            throw new NsdException(String.format("Could not create abstract service from component '%s'", component.getName()));
        }
        return toscaTemplateRes.left().value();
    }
}
