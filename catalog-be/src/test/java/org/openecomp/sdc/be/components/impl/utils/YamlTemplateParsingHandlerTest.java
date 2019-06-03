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

package org.openecomp.sdc.be.components.impl.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.openecomp.sdc.ZipUtil;
import org.openecomp.sdc.be.components.csar.CsarInfo;
import org.openecomp.sdc.be.components.csar.YamlTemplateParsingHandler;
import org.openecomp.sdc.be.components.impl.AnnotationBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupTypeBusinessLogic;
import org.openecomp.sdc.be.components.validation.AnnotationValidator;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.operations.impl.AnnotationTypeOperations;

@RunWith(MockitoJUnitRunner.class)
public class YamlTemplateParsingHandlerTest {

    @Mock
    private GroupTypeBusinessLogic groupTypeBusinessLogic;
    @Mock
    private AnnotationTypeOperations annotationTypeOperations;
    @Mock
    private AnnotationValidator annotationValidator;
    @Mock
    private TitanDao titanDao;
    @Mock
    private User user;

    private YamlTemplateParsingHandler handler;

    private static Map<String, byte[]> csar;
    private static String resourceYml;

    private final static String VFC_GROUP_TYPE = "org.openecomp.groups.VfcInstanceGroup";
    private final static String HEAT_GROUP_TYPE = "org.openecomp.groups.heat.HeatStack";
    private final static String ROOT_GROUP_TYPE = "tosca.groups.Root";
    private final static GroupTypeDefinition VfcInstanceGroupType = buildVfcInstanceGroupType();
    private final static GroupTypeDefinition heatGroupType = buildHeatStackGroupType();
    private final static GroupTypeDefinition rootGroupType = buildRootGroupType();
    private final static String CAPABILITY_TYPE = "org.openecomp.capabilities.VLANAssignment";
    private final static String CAPABILITY_NAME = "vlan_assignment";
    private static final String CSAR_FILE_PATH = "/csars/with_groups.csar";

    private static final String FILE_NAME = "MainServiceTemplate.yaml";

    private static final String CSAR_UUID = "csarUUID";
    private static final String RESOURCE_NAME = "resourceName";
    private static final String MAIN_TEMPLATE_NAME = "Definitions/MainServiceTemplate.yaml";
    private static final String NODE_NAME = "org.openecomp.resource.abstract.nodes.heat.mg";
    private static final String MAIN_GROUP_NAME = "x_group";
    private static final String NESTED_GROUP_NAME = "nested_mg_vepdg_group";

    @BeforeClass()
    public static void prepareData() throws IOException, URISyntaxException {
        csar = ZipUtil.readData(CSAR_FILE_PATH);

        Optional<String> keyOp = csar.keySet().stream().filter(k -> k.endsWith(FILE_NAME)).findAny();
        byte[] mainTemplateService = keyOp.map(csar::get).orElse(null);
        assertNotNull(mainTemplateService);

        resourceYml = new String(mainTemplateService);
    }

    @Before
    public void setup() {

        AnnotationBusinessLogic annotationBusinessLogic = new AnnotationBusinessLogic(annotationTypeOperations,
                annotationValidator);
        handler = new YamlTemplateParsingHandler(titanDao, groupTypeBusinessLogic, annotationBusinessLogic);
        stubGetGroupType();
    }

    @Test
    public void parseResourceInfoFromOneNodeTest() {

        String main_template_content = new String(csar.get(MAIN_TEMPLATE_NAME));
        CsarInfo csarInfo = new CsarInfo(user, CSAR_UUID, csar, RESOURCE_NAME,
                MAIN_TEMPLATE_NAME, main_template_content, true);

        ParsedToscaYamlInfo parsedYaml = handler.parseResourceInfoFromYAML(FILE_NAME, resourceYml, new HashMap<>(),
                csarInfo.extractNodeTypesInfo(), NODE_NAME);

        validateParsedYaml(parsedYaml, NESTED_GROUP_NAME,
                Lists.newArrayList("heat_file", "description"));
    }

    @Test
    public void parseResourceInfoFromYAMLTest() {

        ParsedToscaYamlInfo parsedYaml = handler.parseResourceInfoFromYAML(FILE_NAME, resourceYml, new HashMap<>(),
                new HashMap<>(), "");
        validateParsedYamlWithCapability(parsedYaml);
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

	private void validateParsedYamlWithCapability(ParsedToscaYamlInfo parsedYaml) {

        final List<String> expectedProp = Lists.newArrayList("vfc_parent_port_role",
                "network_collection_function", "vfc_instance_group_function", "subinterface_role");

        validateParsedYaml(parsedYaml, MAIN_GROUP_NAME, expectedProp);

        assertThat(parsedYaml.getGroups().get(MAIN_GROUP_NAME).getCapabilities()
                .get(CAPABILITY_TYPE)
                .get(0).getProperties().get(0).getValue()).isEqualTo("success");
        assertThat(parsedYaml.getGroups().get(MAIN_GROUP_NAME).getCapabilities()).isNotNull();
	}

	private void stubGetGroupType() {
		when(groupTypeBusinessLogic.getLatestGroupTypeByType(eq(VFC_GROUP_TYPE))).thenReturn(VfcInstanceGroupType);
		when(groupTypeBusinessLogic.getLatestGroupTypeByType(eq(HEAT_GROUP_TYPE))).thenReturn(heatGroupType);
        when(groupTypeBusinessLogic.getLatestGroupTypeByType(eq(ROOT_GROUP_TYPE))).thenReturn(rootGroupType);
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

    private static GroupTypeDefinition createGroupTypeDefinition(String type, String derivedFrom, String description){
        GroupTypeDefinition property = new GroupTypeDefinition();

        if (type != null)
            property.setType(type);

        if (derivedFrom != null) {
            property.setDerivedFrom(derivedFrom);
        }

        if (description != null) {
            property.setDescription(description);
        }

        return property;
    }
    private static GroupProperty createGroupProperty(String name, String description,
                                                     String status){
        GroupProperty property = new GroupProperty();
        if (name != null)
            property.setName(name);

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
}
