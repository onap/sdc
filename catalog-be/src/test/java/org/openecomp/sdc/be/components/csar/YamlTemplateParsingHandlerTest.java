/*-
 * ============LICENSE_START===============================================
 * ONAP SDC
 * ========================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=================================================
 */

package org.openecomp.sdc.be.components.csar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.ARTIFACTS;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import mockit.Deencapsulation;
import org.apache.commons.collections.MapUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.components.impl.AnnotationBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupTypeBusinessLogic;
import org.openecomp.sdc.be.components.impl.PolicyTypeBusinessLogic;
import org.openecomp.sdc.be.components.validation.AnnotationValidator;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.ParsedToscaYamlInfo;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.UploadArtifactInfo;
import org.openecomp.sdc.be.model.UploadComponentInstanceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.impl.AnnotationTypeOperations;
import org.openecomp.sdc.common.zip.ZipUtils;
import org.openecomp.sdc.common.zip.exception.ZipException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class YamlTemplateParsingHandlerTest {

    private static final String VFC_GROUP_TYPE = "org.openecomp.groups.VfcInstanceGroup";
    private static final String HEAT_GROUP_TYPE = "org.openecomp.groups.heat.HeatStack";
    private static final String ROOT_GROUP_TYPE = "tosca.groups.Root";
    private static final String OPENECOMP_ANTILOCATE_POLICY_TYPE = "org.openecomp.policies.placement.Antilocate";
    private static final String ROOT_POLICIES_TYPE = "tosca.policies.Root";
    private static final GroupTypeDefinition heatGroupType = buildHeatStackGroupType();
    private static final GroupTypeDefinition rootGroupType = buildRootGroupType();
    private static final String OPENECOMP_POLICY_NAME = "vepdg_server_group_policy";
    private static final PolicyTypeDefinition OPENECOMP_POLICY_TYPE = buildOpenecompPolicyType();
    private static final String CAPABILITY_TYPE = "org.openecomp.capabilities.VLANAssignment";
    private static final String CAPABILITY_NAME = "vlan_assignment";
    private static final GroupTypeDefinition VfcInstanceGroupType = buildVfcInstanceGroupType();
    private static final String CSAR_FILE_PATH = "csars/with_groups.csar";
    private static final String FILE_NAME = "MainServiceTemplate.yaml";
    private static final String CSAR_UUID = "csarUUID";
    private static final String RESOURCE_NAME = "resourceName";
    private static final String MAIN_TEMPLATE_NAME = "Definitions/MainServiceTemplate.yaml";
    private static final String NODE_NAME = "org.openecomp.resource.abstract.nodes.heat.mg";
    private static final String MAIN_GROUP_NAME = "x_group";
    private static final String NESTED_GROUP_NAME = "nested_mg_vepdg_group";
    private static Map<String, byte[]> csar;
    private static String resourceYml;
    @Mock
    private GroupTypeBusinessLogic groupTypeBusinessLogic;
    @Mock
    private AnnotationTypeOperations annotationTypeOperations;
    @Mock
    private AnnotationValidator annotationValidator;
    @Mock
    private JanusGraphDao janusGraphDao;
    @Mock
    private User user;
    @Mock
    private PolicyTypeBusinessLogic policyTypeBusinessLogic;
    private YamlTemplateParsingHandler handler;
    @InjectMocks
    private YamlTemplateParsingHandler testSubject;

    @BeforeAll
    static void prepareData() throws URISyntaxException, ZipException {
        final File csarFile = new File(
            YamlTemplateParsingHandlerTest.class.getClassLoader().getResource(CSAR_FILE_PATH).toURI());
        csar = ZipUtils.readZip(csarFile, false);

        Optional<String> keyOp = csar.keySet().stream().filter(k -> k.endsWith(FILE_NAME)).findAny();
        byte[] mainTemplateService = keyOp.map(csar::get).orElse(null);
        assertNotNull(mainTemplateService);

        resourceYml = new String(mainTemplateService);
    }

    private static GroupTypeDefinition buildRootGroupType() {
        return createGroupTypeDefinition(ROOT_GROUP_TYPE, null,
            "The TOSCA Group Type all other TOSCA Group Types derive from");
    }

    private static GroupTypeDefinition buildHeatStackGroupType() {
        GroupTypeDefinition groupType = createGroupTypeDefinition(HEAT_GROUP_TYPE, "tosca.groups.Root",
            "Grouped all heat resources which are in the same heat stack");

        GroupProperty property1 = createGroupProperty("heat_file",
            "Heat file which associate to this group/heat stack", "SUPPORTED");

        GroupProperty property2 = createGroupProperty("description",
            "Group description", "SUPPORTED");

        groupType.setProperties(Lists.newArrayList(property1, property2));
        return groupType;
    }

    private static GroupTypeDefinition buildVfcInstanceGroupType() {
        GroupTypeDefinition groupType = createGroupTypeDefinition(VFC_GROUP_TYPE, "tosca.groups.Root",
            "Groups of VFCs with same parent port role");

        GroupProperty property1 = createGroupProperty("vfc_instance_group_function",
            "Function of this VFC group", null);

        GroupProperty property2 = createGroupProperty("vfc_parent_port_role",
            "Common role of parent ports of VFCs in this group", null);

        GroupProperty property3 = createGroupProperty("network_collection_function",
            "Network collection function assigned to this group", null);

        GroupProperty property4 = createGroupProperty("subinterface_role",
            "Common role of subinterfaces of VFCs in this group, criteria the group is created", null);

        groupType.setProperties(Lists.newArrayList(property1, property2, property3, property4));

        CapabilityDefinition capability = new CapabilityDefinition();
        capability.setType(CAPABILITY_TYPE);
        capability.setName(CAPABILITY_NAME);
        ComponentInstanceProperty capabilityProperty = new ComponentInstanceProperty();
        capabilityProperty.setName("vfc_instance_group_reference");
        capabilityProperty.setType("string");
        capability.setProperties(Collections.singletonList(capabilityProperty));

        Map<String, CapabilityDefinition> capabilityMap = new HashMap<>();
        capabilityMap.put(CAPABILITY_NAME, capability);
        groupType.setCapabilities(capabilityMap);
        return groupType;
    }

    private static GroupTypeDefinition createGroupTypeDefinition(String type, String derivedFrom, String description) {
        GroupTypeDefinition property = new GroupTypeDefinition();

        if (type != null) {
            property.setType(type);
        }

        if (derivedFrom != null) {
            property.setDerivedFrom(derivedFrom);
        }

        if (description != null) {
            property.setDescription(description);
        }

        return property;
    }

    private static GroupProperty createGroupProperty(String name, String description,
                                                     String status) {
        GroupProperty property = new GroupProperty();
        if (name != null) {
            property.setName(name);
        }

        if (description != null) {
            property.setDescription(description);
        }

        if (status != null) {
            property.setStatus(status);
        }

        property.setType("string");
        property.setRequired(true);

        return property;
    }

    private static PolicyTypeDefinition buildOpenecompPolicyType() {
        return createPolicyTypeDefinition(OPENECOMP_POLICY_NAME, OPENECOMP_ANTILOCATE_POLICY_TYPE, ROOT_POLICIES_TYPE,
            "The Openecomp Antilocate policy");
    }

    private static PolicyTypeDefinition createPolicyTypeDefinition(String policyName, String policyType, String derivedFrom, String description) {
        PolicyTypeDefinition policyTypeDefinition = new PolicyTypeDefinition();
        if (policyName != null && !policyName.isEmpty()) {
            policyTypeDefinition.setName(policyName);
        }
        if (policyType != null) {
            policyTypeDefinition.setType(policyType);
        }
        if (derivedFrom != null) {
            policyTypeDefinition.setDerivedFrom(derivedFrom);
        }
        if (description != null) {
            policyTypeDefinition.setDescription(description);
        }
        return policyTypeDefinition;
    }

    @BeforeEach
    void setup() {

        AnnotationBusinessLogic annotationBusinessLogic = new AnnotationBusinessLogic(annotationTypeOperations,
            annotationValidator);
        handler = new YamlTemplateParsingHandler(janusGraphDao, groupTypeBusinessLogic, annotationBusinessLogic, policyTypeBusinessLogic);
        ReflectionTestUtils.setField(handler, "policyTypeBusinessLogic", policyTypeBusinessLogic);
    }

    @Test
    void parseResourceInfoFromOneNodeTest() {
        when(groupTypeBusinessLogic.getLatestGroupTypeByType(eq(HEAT_GROUP_TYPE), any())).thenReturn(heatGroupType);

        String main_template_content = new String(csar.get(MAIN_TEMPLATE_NAME));
        CsarInfo csarInfo = new CsarInfo(user, CSAR_UUID, csar, RESOURCE_NAME,
            MAIN_TEMPLATE_NAME, main_template_content, true);

        Resource resource = new Resource();
        ParsedToscaYamlInfo parsedYaml = handler.parseResourceInfoFromYAML(FILE_NAME, resourceYml, new HashMap<>(),
            csarInfo.extractTypesInfo(), NODE_NAME, resource, getInterfaceTemplateYaml(csarInfo).get());

        validateParsedYaml(parsedYaml, NESTED_GROUP_NAME, Lists.newArrayList("heat_file", "description"));
    }

    @Test
    void parseServicePropertiesInfoFromYamlTest() {
        when(groupTypeBusinessLogic.getLatestGroupTypeByType(eq(HEAT_GROUP_TYPE), any())).thenReturn(heatGroupType);

        String main_template_content = new String(csar.get(MAIN_TEMPLATE_NAME));
        CsarInfo csarInfo = new CsarInfo(user, CSAR_UUID, csar, RESOURCE_NAME,
            MAIN_TEMPLATE_NAME, main_template_content, true);

        Service service = new Service();
        ParsedToscaYamlInfo parsedYaml = handler.parseResourceInfoFromYAML(FILE_NAME, resourceYml, new HashMap<>(),
            csarInfo.extractTypesInfo(), NODE_NAME, service, getInterfaceTemplateYaml(csarInfo).get());

        assertThat(parsedYaml.getProperties()).isNotNull();
        assertEquals(5, parsedYaml.getProperties().size());
        assertTrue(parsedYaml.getProperties().containsKey("skip_post_instantiation_configuration"));
        assertTrue(parsedYaml.getProperties().containsKey("controller_actor"));
        assertTrue(parsedYaml.getProperties().containsKey("cds_model_version"));
        assertTrue(parsedYaml.getProperties().containsKey("cds_model_name"));
        assertTrue(parsedYaml.getProperties().containsKey("default_software_version"));
    }

    @Test
    void parseResourceInfoFromYAMLTest() {
        stubGetGroupType();
        stubGetPolicyType();

        Resource resource = new Resource();
        ParsedToscaYamlInfo parsedYaml = handler.parseResourceInfoFromYAML(FILE_NAME, resourceYml, new HashMap<>(),
            new HashMap<>(), "", resource, null);
        validateParsedYamlWithCapabilityAndOutputs(parsedYaml);
    }

    @Test
    void testSetArtifacts() {
        UploadComponentInstanceInfo nodeTemplateInfo = new UploadComponentInstanceInfo();
        Map<String, Object> nodeTemplateJsonMap = new HashMap<>();
        Map<String, String> nodeMap = new HashMap<>();
        nodeMap.put("name", "test_name");
        nodeMap.put("type", "test_type");
        nodeTemplateJsonMap.put(ARTIFACTS.getElementName(), nodeMap);
        Deencapsulation.invoke(testSubject, "setArtifacts", nodeTemplateInfo, nodeTemplateJsonMap);
        assertNotNull(nodeTemplateInfo.getArtifacts());
    }

    @Test
    void testCreateArtifactsModuleFromYaml() {
        Map<String, Map<String, Map<String, String>>> nodeTemplateJsonMap = new HashMap<>();
        Map<String, Map<String, String>> map0 = new HashMap<>();
        Map<String, String> map1 = new HashMap<>();
        map1.put("file", "test_file");
        map1.put("type", "test_type");
        map0.put("test_art", map1);
        nodeTemplateJsonMap.put(ARTIFACTS.getElementName(), map0);
        Map<String, Map<String, UploadArtifactInfo>> result;
        result = Deencapsulation.invoke(testSubject, "createArtifactsModuleFromYaml", nodeTemplateJsonMap);
        assertTrue(MapUtils.isNotEmpty(result));
        assertTrue(MapUtils.isNotEmpty(result.get(ARTIFACTS.getElementName())));
        assertEquals("test_file", result.get(ARTIFACTS.getElementName()).get("test_art").getFile());
        assertEquals("test_type", result.get(ARTIFACTS.getElementName()).get("test_art").getType());
    }

    @Test
    void testAddModuleNodeTemplateArtifacts() {
        Map<String, Map<String, UploadArtifactInfo>> result = new HashMap<>();
        Map<String, String> map1 = new HashMap<>();
        map1.put("file", "test_file");
        map1.put("type", "test_type");
        Deencapsulation.invoke(testSubject, "addModuleNodeTemplateArtifacts", result, map1, "test_art");
        assertTrue(MapUtils.isNotEmpty(result));
        assertTrue(MapUtils.isNotEmpty(result.get(ARTIFACTS.getElementName())));
        assertEquals("test_file", result.get(ARTIFACTS.getElementName()).get("test_art").getFile());
        assertEquals("test_type", result.get(ARTIFACTS.getElementName()).get("test_art").getType());
    }

    @Test
    void testBuildModuleNodeTemplateArtifact() {
        Map<String, String> map1 = new HashMap<>();
        map1.put("file", "test_file");
        map1.put("type", "test_type");
        UploadArtifactInfo result;
        result = Deencapsulation.invoke(testSubject, "buildModuleNodeTemplateArtifact", map1);
        assertNotNull(result);
        assertEquals("test_file", result.getFile());
        assertEquals("test_type", result.getType());
    }

    @Test
    void testFillArtifact() {
        Map<String, String> map1 = new HashMap<>();
        map1.put("file", "test_file");
        map1.put("type", "test_type");
        UploadArtifactInfo result = new UploadArtifactInfo();
        Deencapsulation.invoke(testSubject, "fillArtifact", result, map1);
        assertNotNull(result);
        assertEquals("test_file", result.getFile());
        assertEquals("test_type", result.getType());
    }

    @Test
    void parseResourceWithPoliciesDefined() {
        stubGetGroupType();
        stubGetPolicyType();
        Resource resource = new Resource();
        ParsedToscaYamlInfo parsedYaml = handler.parseResourceInfoFromYAML(FILE_NAME, resourceYml, new HashMap<>(),
            new HashMap<>(), "", resource, "");
        validateParsedYamlWithPolicies(parsedYaml);
    }

    private void validateParsedYaml(ParsedToscaYamlInfo parsedYaml, String group, List<String> expectedProp) {
        assertThat(parsedYaml).isNotNull();
        assertThat(parsedYaml.getGroups()).isNotNull().containsKey(group);
        assertThat(parsedYaml.getGroups().get(group)).isNotNull();

        assertThat(parsedYaml.getGroups().get(group).getProperties()).isNotNull();
        assertThat(parsedYaml.getGroups().get(group).getProperties()
            .stream()
            .map(PropertyDataDefinition::getName)
            .collect(Collectors.toList()))
            .containsAll(expectedProp);

        assertThat(parsedYaml.getGroups().get(group).getMembers()).isNotNull();
    }

    private void validateParsedYamlWithCapabilityAndOutputs(ParsedToscaYamlInfo parsedYaml) {

        final List<String> expectedProp = Lists.newArrayList("vfc_parent_port_role",
            "network_collection_function", "vfc_instance_group_function", "subinterface_role");

        validateParsedYaml(parsedYaml, MAIN_GROUP_NAME, expectedProp);

        assertThat(parsedYaml.getGroups().get(MAIN_GROUP_NAME).getCapabilities()
            .get(CAPABILITY_TYPE)
            .get(0).getProperties().get(0).getValue()).isEqualTo("success");
        assertThat(parsedYaml.getGroups().get(MAIN_GROUP_NAME).getCapabilities()).isNotNull();
        assertThat(parsedYaml.getSubstitutionMappingNodeType()).isEqualTo("org.openecomp.resource.abstract.nodes.VF");
        final var outputs = parsedYaml.getOutputs();
        assertNotNull(outputs);
        assertEquals(4, outputs.size());
        assertEquals(4, outputs.keySet().stream().filter(s -> s.matches("oam\\_\\d\\_volume\\_id\\_\\d")).count());
        assertEquals(4, outputs.values().stream().filter(outputDefinition -> (outputDefinition instanceof AttributeDefinition)).count());
    }

    private void stubGetGroupType() {
        when(groupTypeBusinessLogic.getLatestGroupTypeByType(eq(VFC_GROUP_TYPE), any())).thenReturn(VfcInstanceGroupType);
        when(groupTypeBusinessLogic.getLatestGroupTypeByType(eq(HEAT_GROUP_TYPE), any())).thenReturn(heatGroupType);
        when(groupTypeBusinessLogic.getLatestGroupTypeByType(eq(ROOT_GROUP_TYPE), any())).thenReturn(rootGroupType);
    }

    private void validateParsedYamlWithPolicies(ParsedToscaYamlInfo parsedYaml) {
        // validate  policies
        assertThat(parsedYaml.getPolicies()).isNotNull();
        assertThat(parsedYaml.getPolicies()).containsKey(OPENECOMP_POLICY_NAME);
        assertThat(parsedYaml.getPolicies().get(OPENECOMP_POLICY_NAME)).isInstanceOf(PolicyDefinition.class);
    }

    private void stubGetPolicyType() {
        when(policyTypeBusinessLogic.getLatestPolicyTypeByType(eq(OPENECOMP_ANTILOCATE_POLICY_TYPE), any())).thenReturn(
            OPENECOMP_POLICY_TYPE);
    }

    private Optional<String> getInterfaceTemplateYaml(CsarInfo csarInfo) {
        String[] yamlFile;
        String interfaceTemplateYaml = "";
        if (csarInfo.getMainTemplateName().contains(".yml")) {
            yamlFile = csarInfo.getMainTemplateName().split(".yml");
            interfaceTemplateYaml = yamlFile[0] + "-interface.yml";
        } else if (csarInfo.getMainTemplateName().contains(".yaml")) {
            yamlFile = csarInfo.getMainTemplateName().split(".yaml");
            interfaceTemplateYaml = yamlFile[0] + "-interface.yaml";
        }
        if (csarInfo.getCsar().containsKey(interfaceTemplateYaml)) {
            return Optional.of(new String(csarInfo.getCsar().get(interfaceTemplateYaml)));
        }
        return Optional.empty();
    }
}
