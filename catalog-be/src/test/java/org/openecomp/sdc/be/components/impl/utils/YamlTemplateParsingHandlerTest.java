package org.openecomp.sdc.be.components.impl.utils;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.csar.YamlTemplateParsingHandler;
import org.openecomp.sdc.be.components.impl.AnnotationBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupTypeBusinessLogic;
import org.openecomp.sdc.be.components.validation.AnnotationValidator;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.operations.impl.AnnotationTypeOperations;
import org.openecomp.sdc.common.util.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
@RunWith(MockitoJUnitRunner.class)
public class YamlTemplateParsingHandlerTest {

    private final static String VFC_GROUP_TYPE = "org.openecomp.groups.VfcInstanceGroup";
    private final static String HEAT_GROUP_TYPE = "org.openecomp.groups.heat.HeatStack";
    private final static String ROOT_GROUP_TYPE = "tosca.groups.Root";
    private final static GroupTypeDefinition VfcInstanceGroupType = buildVfcInstanceGroupType();
    private final static GroupTypeDefinition heatGroupType = buildHeatStackGroupType();
    private final static GroupTypeDefinition rootGroupType = buildRootGroupType();
    private final static String CAPABILITY_TYPE = "org.openecomp.capabilities.VLANAssignment";
    private final static String CAPABILITY_NAME = "vlan_assignment";
    public static final String csarsFilePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "csars" ;

    private YamlTemplateParsingHandler handler;
    private AnnotationBusinessLogic annotationBusinessLogic;
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private GroupTypeBusinessLogic groupTypeBusinessLogic;
    @Mock
    private AnnotationTypeOperations annotationTypeOperations;
    @Mock
    private AnnotationValidator annotationValidator;
    @Mock
    private JanusGraphDao janusGraphDao;

    @Before
    public void init(){
        annotationBusinessLogic = new AnnotationBusinessLogic(annotationTypeOperations, annotationValidator);
        handler = new YamlTemplateParsingHandler(janusGraphDao, groupTypeBusinessLogic, annotationBusinessLogic);
    }

    @Test
    public void parseResourceInfoFromYAMLTest(){
        Path path = Paths.get(csarsFilePath + File.separator + "with_groups.csar");
        try {
            Map<String, byte[]> csar = ZipUtil.readZip(Files.readAllBytes(path));
            String fileName = "MainServiceTemplate.yaml";
            Optional<String> keyOp = csar.keySet().stream().filter(k -> k.endsWith(fileName)).findAny();
            byte[] mainTemplateService = csar.get(keyOp.get());
            Properties props = new Properties();
            String resourceYml = new String(mainTemplateService);
            props.load(new StringReader(resourceYml.replace("\\","\\\\")));
            Resource resource = new Resource();
            
            stubGetGroupType();
            
            ParsedToscaYamlInfo parsedYaml = handler.parseResourceInfoFromYAML(fileName, resourceYml, new HashMap<>(), new HashMap<>(), "");
            
            validateParsedYaml(parsedYaml);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



	private void validateParsedYaml(ParsedToscaYamlInfo parsedYaml) {
		assertThat(parsedYaml).isNotNull();
		assertThat(parsedYaml.getGroups()).isNotNull().containsKey("x_group");
		assertThat(parsedYaml.getGroups().get("x_group")).isNotNull();
		assertThat(parsedYaml.getGroups().get("x_group").getProperties()).isNotNull();
		assertThat(parsedYaml.getGroups().get("x_group").getProperties()
				.stream()
				.map(PropertyDataDefinition::getName)
				.collect(Collectors.toList()))
		.containsAll(Lists.newArrayList("vfc_parent_port_role", "network_collection_function", "vfc_instance_group_function", "subinterface_role"));
		assertThat(parsedYaml.getGroups().get("x_group").getCapabilities()
				.get(CAPABILITY_TYPE)
				.get(0).getProperties().get(0).getValue()).isEqualTo("success");
		assertThat(parsedYaml.getGroups().get("x_group").getProperties()
				.stream()
				.map(PropertyDataDefinition::getName)
				.collect(Collectors.toList()))
		.containsAll(Lists.newArrayList("vfc_parent_port_role", "network_collection_function", "vfc_instance_group_function", "subinterface_role"));
		assertThat(parsedYaml.getGroups().get("x_group").getCapabilities()).isNotNull();
		assertThat(parsedYaml.getGroups().get("x_group").getMembers()).isNotNull();
	}

	private void stubGetGroupType() {
		when(groupTypeBusinessLogic.getLatestGroupTypeByType(eq(VFC_GROUP_TYPE))).thenReturn(VfcInstanceGroupType);
		when(groupTypeBusinessLogic.getLatestGroupTypeByType(eq(HEAT_GROUP_TYPE))).thenReturn(heatGroupType);
        when(groupTypeBusinessLogic.getLatestGroupTypeByType(eq(ROOT_GROUP_TYPE))).thenReturn(rootGroupType);
//        when(annotationBusinessLogic.validateAndMergeAnnotationsAndAssignToInput(any(Map.class))).thenReturn(null);
    }

    private static GroupTypeDefinition buildRootGroupType() {
        GroupTypeDefinition groupType = new GroupTypeDefinition();
        groupType.setType(ROOT_GROUP_TYPE);
        groupType.setDescription("The TOSCA Group Type all other TOSCA Group Types derive from");
        return groupType;
	}

	private static GroupTypeDefinition buildHeatStackGroupType() {
        GroupTypeDefinition groupType = new GroupTypeDefinition();
        groupType.setType(HEAT_GROUP_TYPE);
        groupType.setDerivedFrom("tosca.groups.Root");
        groupType.setDescription("Grouped all heat resources which are in the same heat stack");
        	      
        GroupProperty property1 = new GroupProperty();
        property1.setName("heat_file");
        property1.setType("string");
        property1.setRequired(true);
        property1.setDescription("Heat file which associate to this group/heat stack");
        property1.setStatus("SUPPORTED");

        GroupProperty property2 = new GroupProperty();
        property2.setName("description");
        property2.setType("string");
        property2.setRequired(true);
        property2.setDescription("group description");
        property2.setStatus("SUPPORTED");
        groupType.setProperties(Lists.newArrayList(property1, property2));
        return groupType;
	}

	private static GroupTypeDefinition buildVfcInstanceGroupType() {
        GroupTypeDefinition groupType = new GroupTypeDefinition();
        groupType.setType(VFC_GROUP_TYPE);
        groupType.setDerivedFrom("tosca.groups.Root");
        groupType.setDescription("groups VFCs with same parent port role");
        GroupProperty property1 = new GroupProperty();
        property1.setName("vfc_instance_group_function");
        property1.setType("string");
        property1.setRequired(true);
        property1.setDescription("function of this VFC group");

        GroupProperty property2 = new GroupProperty();
        property2.setName("vfc_parent_port_role");
        property2.setType("string");
        property2.setRequired(true);
        property2.setDescription("common role of parent ports of VFCs in this group");

        GroupProperty property3 = new GroupProperty();
        property3.setName("network_collection_function");
        property3.setType("string");
        property3.setRequired(true);
        property3.setDescription("network collection function assigned to this group");

        GroupProperty property4 = new GroupProperty();
        property4.setName("subinterface_role");
        property4.setType("string");
        property4.setRequired(true);
        property4.setDescription("common role of subinterfaces of VFCs in this group, criteria the group is created");

        groupType.setProperties(Lists.newArrayList(property1, property2, property3, property4));

        CapabilityDefinition capability = new CapabilityDefinition();
        capability.setType(CAPABILITY_TYPE);
        capability.setName(CAPABILITY_NAME);
        ComponentInstanceProperty capabilityProperty = new ComponentInstanceProperty();
        capabilityProperty.setName("vfc_instance_group_reference");
        capabilityProperty.setType("string");
        capability.setProperties(Arrays.asList(capabilityProperty));

        Map<String, CapabilityDefinition> capabilityMap = new HashMap<>();
        capabilityMap.put(CAPABILITY_NAME, capability);
        groupType.setCapabilities(capabilityMap);
        return groupType;
    }

}
