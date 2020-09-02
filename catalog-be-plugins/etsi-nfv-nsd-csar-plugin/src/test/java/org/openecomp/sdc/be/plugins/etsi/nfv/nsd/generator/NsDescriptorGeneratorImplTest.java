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
import org.openecomp.sdc.be.tosca.model.ToscaTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaTemplateCapability;
import org.openecomp.sdc.be.tosca.model.ToscaTopolgyTemplate;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.yaml.snakeyaml.Yaml;

class NsDescriptorGeneratorImplTest {

    private static final String VNFD_AMF_NODE_NAME = "vnfd_amf";
    private static final String VIRTUAL_LINK_REQUIREMENT_NAME = "virtual_link";

    @Mock
    private ToscaExportHandler toscaExportHandler;

    private final ObjectProvider<ToscaTemplateYamlGenerator> toscaTemplateYamlGeneratorProvider = new ObjectProvider<ToscaTemplateYamlGenerator>() {
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

    private NsDescriptorGeneratorImpl nsDescriptorGenerator;

    @BeforeEach
    void setUp() {
        setUpConfigurationMock();
        MockitoAnnotations.initMocks(this);
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
        //a property to be excluded
        vnfAmfNodeTemplate.setProperties(ImmutableMap.of("nf_naming_code", new ToscaProperty()));
        //a property that wont be excluded
        vnfAmfNodeTemplate.setProperties(ImmutableMap.of("will_not_be_excluded", new ToscaProperty()));
        nodeTemplateMap.put(VNFD_AMF_NODE_NAME, vnfAmfNodeTemplate);
        
        final Map<String, ToscaTemplateCapability> vnfAmfCapabilities = new HashMap<>();
        vnfAmfCapabilities.put("myCapability", new ToscaTemplateCapability());
		vnfAmfNodeTemplate.setCapabilities(vnfAmfCapabilities);
        componentToscaTopologyTemplate.setNode_templates(nodeTemplateMap);
        
        final SubstitutionMapping substitutionMapping = mock(SubstitutionMapping.class);
        Map<String, String[]> requirements = new HashMap<>();
        String[] requirementAssignment = {"VNF1", VIRTUAL_LINK_REQUIREMENT_NAME};
        requirements.put(VIRTUAL_LINK_REQUIREMENT_NAME, requirementAssignment);
		when(substitutionMapping.getRequirements()).thenReturn(requirements);
		Map<String, String[]> capabilities = new HashMap<>();
        String[] capabilitiesAssignment = {"VNF1", "capability1"};
        capabilities.put("capability", capabilitiesAssignment);
		when(substitutionMapping.getCapabilities()).thenReturn(capabilities);
		componentToscaTopologyTemplate.setSubstitution_mappings(substitutionMapping);

        final ToscaTemplate componentInterfaceToscaTemplate = new ToscaTemplate("");
        final ToscaNodeType interfaceToscaNodeType = new ToscaNodeType();
        interfaceToscaNodeType.setProperties(
            ImmutableMap.of("designer", createToscaProperty("designerValue"),
                "version", createToscaProperty("versionValue"),
                "name", createToscaProperty("nameValue"),
                "invariant_id", createToscaProperty("invariantIdValue"))
        );
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
        assertThat("Nsd should not be null", nsd, is(notNullValue()));
        assertThat("Nsd designer should be as expected", nsd.getDesigner(), is("designerValue"));
        assertThat("Nsd version should be as expected", nsd.getVersion(), is("versionValue"));
        assertThat("Nsd name should be as expected", nsd.getName(), is("nameValue"));
        assertThat("Nsd invariantId should be as expected", nsd.getInvariantId(), is("invariantIdValue"));
        assertThat("Nsd content should not be empty", nsd.getContents(), is(notNullValue()));
        assertThat("Nsd content should not be empty", nsd.getContents().length, is(greaterThan(0)));

        final Map<String, Object> toscaTemplateYaml = readYamlAsMap(nsd.getContents());
        @SuppressWarnings("unchecked")
        final Map<String, Object> topologyTemplate = (Map<String, Object>) toscaTemplateYaml.get("topology_template");
        assertThat("topology_template should not be empty", topologyTemplate, is(not(anEmptyMap())));
        @SuppressWarnings("unchecked")
        final Map<String, Object> substitutionMappings =
            (Map<String, Object>) topologyTemplate.get("substitution_mappings");
        assertThat("substitution_mappings should not be empty", substitutionMappings, is(not(anEmptyMap())));
        assertThat("substitution_mappings->node_type should not be null",
            substitutionMappings.get("node_type"), is(notNullValue()));
        assertThat("substitution_mappings->node_type should be as expected",
            substitutionMappings.get("node_type"), is(nsNodeTypeName));
        
        final Map<String, List<String>> subMappingRequirements = (Map<String, List<String>>) substitutionMappings.get("requirements");
        assertThat(subMappingRequirements.get(VIRTUAL_LINK_REQUIREMENT_NAME).get(0), is("VNF1"));
        assertThat(subMappingRequirements.get(VIRTUAL_LINK_REQUIREMENT_NAME).get(1), is(VIRTUAL_LINK_REQUIREMENT_NAME));
        final Map<String, List<String>> subMappingCapabilities = (Map<String, List<String>>) substitutionMappings.get("capabilities");
        assertThat(subMappingCapabilities.get("capability").get(0), is("VNF1"));
        assertThat(subMappingCapabilities.get("capability").get(1), is("capability1"));
        
        @SuppressWarnings("unchecked")
		final Map<String, Object> nodeTemplates =
                (Map<String, Object>) topologyTemplate.get("node_templates");
        @SuppressWarnings("unchecked")
		final Map<String, Object> nodeTemplate =
                (Map<String, Object>) nodeTemplates.get(VNFD_AMF_NODE_NAME);
        assertThat("capabilities should be null",
        		nodeTemplate.get("capabilities"), is(nullValue()));
    }

    private ToscaProperty createToscaProperty(final String value) {
        final ToscaProperty toscaProperty = new ToscaProperty();
        final ToscaPropertyConstraint toscaPropertyConstraint =
            new ToscaPropertyConstraintValidValues(ImmutableList.of(value));
        toscaProperty.setConstraints(ImmutableList.of(toscaPropertyConstraint));
        return toscaProperty;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readYamlAsMap(final byte[] yamlContents) throws IOException {
        final Yaml yaml = new Yaml();
        try (final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(yamlContents)) {
            return  (Map<String, Object>) yaml.load(byteArrayInputStream);
        }
    }
}