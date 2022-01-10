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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fj.data.Either;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.exception.NsdException;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.model.Nsd;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.model.VnfDescriptor;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.tosca.yaml.ToscaTemplateYamlGenerator;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.tosca.model.SubstitutionMapping;
import org.openecomp.sdc.be.tosca.model.ToscaNodeTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyConstraint;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyConstraintValidValues;
import org.openecomp.sdc.be.tosca.model.ToscaRequirement;
import org.openecomp.sdc.be.tosca.model.ToscaTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaTemplateCapability;
import org.openecomp.sdc.be.tosca.model.ToscaTopolgyTemplate;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.springframework.beans.factory.ObjectProvider;
import org.yaml.snakeyaml.Yaml;

class NsDescriptorGeneratorImplTest {

    private static final String VNFD_AMF_NODE_NAME = "vnfd_amf";
    private static final String VIRTUAL_LINK_REQUIREMENT_NAME = "virtual_link";
    private static final String VIRTUAL_BINDING_REQUIREMENT_NAME = "virtual_binding";
    private static final String DOT = ".";
    private static final String PREFIX = "VNF";
    private final ObjectProvider<ToscaTemplateYamlGenerator> toscaTemplateYamlGeneratorProvider = new ObjectProvider<>() {
        @Override
        public ToscaTemplateYamlGenerator getObject(Object... args) {
            return new ToscaTemplateYamlGenerator((ToscaTemplate) args[0]);
        }

        @Override
        public ToscaTemplateYamlGenerator getIfAvailable() {
            return null;
        }

        @Override
        public ToscaTemplateYamlGenerator getIfUnique() {
            return null;
        }

        @Override
        public ToscaTemplateYamlGenerator getObject() {
            return null;
        }
    };
    @Mock
    private ToscaExportHandler toscaExportHandler;
    private NsDescriptorGeneratorImpl nsDescriptorGenerator;

    @BeforeEach
    void setUp() {
        setUpConfigurationMock();
        MockitoAnnotations.openMocks(this);
        nsDescriptorGenerator = new NsDescriptorGeneratorImpl(toscaExportHandler, toscaTemplateYamlGeneratorProvider);
    }

    private void setUpConfigurationMock() {
        final List<Map<String, Map<String, String>>> defaultImports = new ArrayList<>();
        final Map<String, Map<String, String>> importMap = new HashMap<>();
        final Map<String, String> nodeImportEntry = new HashMap<>();
        nodeImportEntry.put("file", "nodes.yml");
        importMap.put("nodes", nodeImportEntry);
        defaultImports.add(importMap);
        final ConfigurationSource configurationSource = mock(ConfigurationSource.class);
        final Configuration configuration = new Configuration();
        configuration.setDefaultImports(defaultImports);
        configuration.setHeatEnvArtifactHeader("");
        configuration.setHeatEnvArtifactFooter("");
        when(configurationSource.getAndWatchConfiguration(any(), any())).thenReturn(configuration);
        new ConfigurationManager(configurationSource);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGenerate() throws IOException, NsdException {
        //given
        final Component component = mock(Component.class);
        when(component.getComponentType()).thenReturn(ComponentTypeEnum.SERVICE);
        final ToscaTemplate componentToscaTemplate = new ToscaTemplate("");
        final ToscaTopolgyTemplate componentToscaTopologyTemplate = new ToscaTopolgyTemplate();
        componentToscaTemplate.setTopology_template(componentToscaTopologyTemplate);
        final HashMap<String, ToscaNodeTemplate> nodeTemplateMap = new HashMap<>();
        final ToscaNodeTemplate vnfAmfNodeTemplate = new ToscaNodeTemplate();
        vnfAmfNodeTemplate.setType("com.ericsson.resource.abstract.Ericsson.AMF");
        final Map<String, Object> propertyMap = new HashMap<>();
        //a property to be excluded
        propertyMap.put("nf_naming_code", new ToscaProperty());
        //a property that wont be excluded
        propertyMap.put("will_not_be_excluded", new ToscaProperty());
        vnfAmfNodeTemplate.setProperties(propertyMap);
        nodeTemplateMap.put(VNFD_AMF_NODE_NAME, vnfAmfNodeTemplate);
        final Map<String, ToscaTemplateCapability> vnfAmfCapabilities = new HashMap<>();
        vnfAmfCapabilities.put("myCapability", new ToscaTemplateCapability());
        vnfAmfNodeTemplate.setCapabilities(vnfAmfCapabilities);
        componentToscaTopologyTemplate.setNode_templates(nodeTemplateMap);
        final SubstitutionMapping substitutionMapping = mock(SubstitutionMapping.class);
        Map<String, String[]> requirements = new HashMap<>();
        String[] requirementAssignmentVl = {"VNF1", PREFIX + DOT + VIRTUAL_LINK_REQUIREMENT_NAME};
        requirements.put("VNF1" + DOT + VIRTUAL_LINK_REQUIREMENT_NAME, requirementAssignmentVl);
        when(substitutionMapping.getRequirements()).thenReturn(requirements);
        Map<String, String[]> capabilities = new HashMap<>();
        String[] capabilitiesAssignment = {"VNF1", "capability1"};
        capabilities.put("capability", capabilitiesAssignment);
        when(substitutionMapping.getCapabilities()).thenReturn(capabilities);
        componentToscaTopologyTemplate.setSubstitution_mappings(substitutionMapping);
        
        Map<String, ToscaProperty> inputs = new HashMap<>();
        inputs.put("invariant_id", new ToscaProperty());
        inputs.put("other_property", new ToscaProperty());
        componentToscaTopologyTemplate.setInputs(inputs );
        final ToscaTemplate componentInterfaceToscaTemplate = new ToscaTemplate("");
        final String designerPropertyValue = "designerValue";
        final String versionPropertyValue = "versionValue";
        final String namePropertyValue = "nameValue";
        final String invariantIdPropertyValue = "invariantIdValue";
        final String otherPropertyValue = "otherValue";
        final ToscaNodeType interfaceToscaNodeType = createDefaultInterfaceToscaNodeType(designerPropertyValue, versionPropertyValue,
            namePropertyValue, invariantIdPropertyValue, otherPropertyValue);
        List<Map<String, ToscaRequirement>> interfaceNodeTypeRequirements = new ArrayList<>();
        Map<String, ToscaRequirement> interfaceNodeTypeRequirementMap = new HashMap<>();
        interfaceNodeTypeRequirementMap.put("VNF1" + DOT + VIRTUAL_LINK_REQUIREMENT_NAME, mock(ToscaRequirement.class));
        interfaceNodeTypeRequirementMap.put("VNF1" + DOT + VIRTUAL_BINDING_REQUIREMENT_NAME, mock(ToscaRequirement.class));
        interfaceNodeTypeRequirements.add(interfaceNodeTypeRequirementMap);
        interfaceToscaNodeType.setRequirements(interfaceNodeTypeRequirements);
        final String nsNodeTypeName = "nsNodeTypeName";
        componentInterfaceToscaTemplate.setNode_types(ImmutableMap.of(nsNodeTypeName, interfaceToscaNodeType));
        when(toscaExportHandler.convertToToscaTemplate(component)).thenReturn(Either.left(componentToscaTemplate));
        when(toscaExportHandler.convertInterfaceNodeType(any(), any(), any(), any(), anyBoolean()))
            .thenReturn(Either.left(componentInterfaceToscaTemplate));
        final List<VnfDescriptor> vnfDescriptorList = new ArrayList<>();
        VnfDescriptor vnfDescriptor1 = new VnfDescriptor();
        vnfDescriptor1.setName(VNFD_AMF_NODE_NAME);
        vnfDescriptor1.setVnfdFileName("vnfd_amf.yaml");
        vnfDescriptor1.setNodeType("com.ericsson.resource.abstract.Ericsson.AMF");
        vnfDescriptorList.add(vnfDescriptor1);
        //when
        final Nsd nsd = nsDescriptorGenerator.generate(component, vnfDescriptorList).orElse(null);
        //then
        assertNotEmpty(nsd);
        assertThat("Nsd designer should be as expected", nsd.getDesigner(), is(designerPropertyValue));
        assertThat("Nsd version should be as expected", nsd.getVersion(), is(versionPropertyValue));
        assertThat("Nsd name should be as expected", nsd.getName(), is(namePropertyValue));
        assertThat("Nsd invariantId should be as expected", nsd.getInvariantId(), is(invariantIdPropertyValue));
        final Map<String, Object> toscaTemplateYaml = readYamlAsMap(nsd.getContents());
        final Map<String, Object> topologyTemplate = (Map<String, Object>) toscaTemplateYaml.get("topology_template");
        assertThat("topology_template should not be empty", topologyTemplate, is(not(anEmptyMap())));
        final Map<String, Object> substitutionMappings = (Map<String, Object>) topologyTemplate
            .get("substitution_mappings");
        assertThat("substitution_mappings should not be empty", substitutionMappings, is(not(anEmptyMap())));
        assertThat("substitution_mappings->node_type should not be null", substitutionMappings.get("node_type"), is(notNullValue()));
        assertThat("substitution_mappings->node_type should be as expected", substitutionMappings.get("node_type"), is(nsNodeTypeName));
        final Map<String, List<String>> subMappingRequirements = (Map<String, List<String>>) substitutionMappings.get("requirements");
        assertThat(subMappingRequirements.get("VNF1" + DOT + VIRTUAL_LINK_REQUIREMENT_NAME).get(0), is("VNF1"));
        assertThat(subMappingRequirements.get("VNF1" + DOT + VIRTUAL_LINK_REQUIREMENT_NAME).get(1), is(VIRTUAL_LINK_REQUIREMENT_NAME));
        assertEquals(1, subMappingRequirements.size());
        final Map<String, List<String>> subMappingCapabilities = (Map<String, List<String>>) substitutionMappings.get("capabilities");
        assertNull(subMappingCapabilities);
        
        final Map<String, Object> topologyInputs = (Map<String, Object>) topologyTemplate.get("inputs");
        assertTrue(topologyInputs.containsKey("other_property"));
        assertEquals(1, topologyInputs.size());
        
        final Map<String, Object> nodeTemplates = (Map<String, Object>) ((Map<String, Object>) topologyTemplate.get("node_templates"));
        final Map<String, Object> nodeTemplateVnfd = (Map<String, Object>) ((Map<String, Object>) nodeTemplates.get(VNFD_AMF_NODE_NAME));
        final Map<String, Object> nodeTemplateProperties = (Map<String, Object>) ((Map<String, Object>) nodeTemplateVnfd.get("properties"));
        assertTrue(nodeTemplateProperties.containsKey("will_not_be_excluded"));

        final Map<String, Object> nodeType = (Map<String, Object>) ((Map<String, Object>) toscaTemplateYaml.get("node_types")).get(nsNodeTypeName);
        assertTrue(((List<Map<String, Map>>)nodeType.get("requirements")).get(0).containsKey("VNF1" + DOT + VIRTUAL_LINK_REQUIREMENT_NAME));
        assertFalse(((List<Map<String, Map>>)nodeType.get("requirements")).get(0).containsKey("VNF1" + DOT + VIRTUAL_BINDING_REQUIREMENT_NAME));
        
        assertEquals(5, ((Map<String, Map>)nodeType.get("properties")).size());
        for (final Entry<String, Map> property: ((Map<String, Map>)nodeType.get("properties")).entrySet()) {
            if (property.getKey().equals("other_property")) {
                assertNull(property.getValue().get("constraints"));
            } else {
                assertEquals(1, ((List<Map>)property.getValue().get("constraints")).size());
            }
        }

    }

    private ToscaNodeType createDefaultInterfaceToscaNodeType(final String designerPropertyValue, final String versionPropertyValue,
                                                              final String namePropertyValue, final String invariantIdPropertyValue, String otherPropertyValue) {
        final ToscaNodeType interfaceToscaNodeType = new ToscaNodeType();
        final Map<String, ToscaProperty> propertyMap = new HashMap<>();
        propertyMap.put("designer", createToscaProperty(designerPropertyValue));
        propertyMap.put("version", createToscaProperty(versionPropertyValue));
        propertyMap.put("name", createToscaProperty(namePropertyValue));
        propertyMap.put("invariant_id", createToscaProperty(invariantIdPropertyValue));
        propertyMap.put("other_property", createToscaProperty(otherPropertyValue));
        interfaceToscaNodeType.setProperties(propertyMap);
        return interfaceToscaNodeType;
    }

    private void assertNotEmpty(Nsd nsd) {
        assertThat("Nsd should not be null", nsd, is(notNullValue()));
        assertThat("Nsd content should not be empty", nsd.getContents(), is(notNullValue()));
        assertThat("Nsd content should not be empty", nsd.getContents().length, is(greaterThan(0)));
    }

    private ToscaProperty createToscaProperty(final String value) {
        final ToscaProperty toscaProperty = new ToscaProperty();
        toscaProperty.setDefaultp(value);
        return toscaProperty;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readYamlAsMap(final byte[] yamlContents) throws IOException {
        final Yaml yaml = new Yaml();
        try (final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(yamlContents)) {
            return (Map<String, Object>) yaml.load(byteArrayInputStream);
        }
    }
}
