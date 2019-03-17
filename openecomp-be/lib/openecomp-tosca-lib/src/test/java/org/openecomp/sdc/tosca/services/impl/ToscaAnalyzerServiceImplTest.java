/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.tosca.services.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.onap.sdc.tosca.datatypes.model.CapabilityType;
import org.onap.sdc.tosca.datatypes.model.Constraint;
import org.onap.sdc.tosca.datatypes.model.DataType;
import org.onap.sdc.tosca.datatypes.model.DefinitionOfDataType;
import org.onap.sdc.tosca.datatypes.model.Import;
import org.onap.sdc.tosca.datatypes.model.InterfaceDefinitionType;
import org.onap.sdc.tosca.datatypes.model.InterfaceType;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.NodeType;
import org.onap.sdc.tosca.datatypes.model.OperationDefinitionType;
import org.onap.sdc.tosca.datatypes.model.ParameterDefinition;
import org.onap.sdc.tosca.datatypes.model.PropertyDefinition;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.RequirementDefinition;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.datatypes.model.Status;
import org.onap.sdc.tosca.datatypes.model.SubstitutionMapping;
import org.onap.sdc.tosca.datatypes.model.TopologyTemplate;
import org.onap.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.SdcRuntimeException;
import org.openecomp.sdc.tosca.TestUtil;
import org.openecomp.sdc.tosca.datatypes.ToscaElementTypes;
import org.openecomp.sdc.tosca.datatypes.ToscaFlatData;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.ToscaConstants;


@RunWith(MockitoJUnitRunner.class)
public class ToscaAnalyzerServiceImplTest {

    private static final String CAPABILITY_TYPE_A = "capabilityTypeA";
    private static final String CAPABILITY_TYPE_B = "capabilityTypeB";
    private static final String TOSCA_CAPABILITIES_ROOT = "tosca.capabilities.Root";
    private static final String CMAUI_IMAGE_EXTEND = "org.openecomp.resource.vfc.nodes.heat.cmaui_image_extend";
    private static final String STANDARD_INTERFACE_KEY = "Standard";
    private static final String TOSCA_LIFECYCLE_STANDARD = "tosca.interfaces.node.lifecycle.Standard";
    private static final String CMAUI_INTERFACE_TEST =
            "org.openecomp.resource.vfc.nodes.heat.cmaui_image_interfaceTest";

    /*
    Dictionary:
    SrvTmp: ServiceTemplate
    NdTmp: NodeTemplate
    NdTy: NodeType
    */

    private static ToscaAnalyzerService toscaAnalyzerService;
    private static ToscaServiceModel toscaServiceModel;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private NodeTemplate nodeTemplateMock;
    @Mock
    private ParameterDefinition parameterDefinitionMock;
    @Mock
    private PropertyDefinition propertyDefinitionMock;
    @Mock
    private InterfaceDefinitionType interfaceDefinitionMock;
    @Mock
    private ToscaServiceModel toscaServiceModelMock;

    @BeforeClass
    public static void onlyOnceSetUp() throws IOException {
        toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
        toscaServiceModel = TestUtil.loadToscaServiceModel("/mock/analyzerService/toscasubstitution/",
                "/mock/globalServiceTemplates/", null);
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetFlatEntityNotFound() throws Exception {
        thrown.expect(CoreException.class);
        thrown.expectMessage(
                "Entity Type 'org.openecomp.resource.vfc.notFound' or one of its derivedFrom type "
                        + "hierarchy, is not defined in tosca service model");
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (InputStream yamlFile = toscaExtensionYamlUtil.loadYamlFileIs(
                "/mock/analyzerService/NestedServiceTemplateReqTest.yaml")) {

            ServiceTemplate serviceTemplateFromYaml =
                    toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);

            toscaAnalyzerService.getFlatEntity(ToscaElementTypes.NODE_TYPE, "org.openecomp.resource.vfc.notFound",
                    serviceTemplateFromYaml, toscaServiceModel);
        }
    }

    @Test
    public void testGetFlatEntityFileNotFound() throws Exception {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Tosca file 'missingFile.yaml' was not found in tosca service model");
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (InputStream yamlFile = toscaExtensionYamlUtil.loadYamlFileIs(
                "/mock/analyzerService/ServiceTemplateFileNotFoundTest.yaml")) {

            ServiceTemplate serviceTemplateFromYaml =
                    toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);

            toscaAnalyzerService
                    .getFlatEntity(ToscaElementTypes.NODE_TYPE, "org.openecomp.resource.vfc.nodes.heat.cmaui_image",
                            serviceTemplateFromYaml, toscaServiceModel);
        }
    }

    @Test
    public void testGetFlatEntityNodeType() throws Exception {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (InputStream yamlFile = toscaExtensionYamlUtil.loadYamlFileIs(
                "/mock/analyzerService/NestedServiceTemplateReqTest.yaml")) {

            ServiceTemplate serviceTemplateFromYaml =
                    toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);

            ToscaFlatData flatData = toscaAnalyzerService.getFlatEntity(ToscaElementTypes.NODE_TYPE,
                    "org.openecomp.resource.vfc.nodes.heat" + ".cmaui_image", serviceTemplateFromYaml,
                    toscaServiceModel);

            Assert.assertNotNull(flatData);
            checkNodeTypeFlatEntity(flatData);
            checkNodeTypeInheritanceHierarchy(flatData);
        }
    }

    private void checkNodeTypeInheritanceHierarchy(ToscaFlatData flatData) {
        List<String> inheritanceHierarchyType = flatData.getInheritanceHierarchyType();
        Assert.assertNotNull(inheritanceHierarchyType);
        Assert.assertEquals(4, inheritanceHierarchyType.size());
        Assert.assertTrue(inheritanceHierarchyType.contains("org.openecomp.resource.vfc.nodes.heat.cmaui_image"));
        Assert.assertTrue(inheritanceHierarchyType.contains("org.openecomp.resource.vfc.nodes.heat.nova.Server"));
        Assert.assertTrue(inheritanceHierarchyType.contains("tosca.nodes.Compute"));
        Assert.assertTrue(inheritanceHierarchyType.contains("tosca.nodes.Root"));
    }

    private void checkNodeTypeFlatEntity(ToscaFlatData flatData) {
        Assert.assertNotNull(flatData.getFlatEntity());
        NodeType flatEntity = (NodeType) flatData.getFlatEntity();
        Assert.assertEquals("org.openecomp.resource.vfc.nodes.heat.nova.Server", flatEntity.getDerived_from());
        Assert.assertEquals(20, flatEntity.getProperties().size());
        Assert.assertEquals("overridden default value", flatEntity.getProperties().get("admin_pass").get_default());
        Assert.assertEquals("REBUILD", flatEntity.getProperties().get("image_update_policy").get_default());
        Assert.assertNotNull(flatEntity.getProperties().get("new_property"));
    }

    @Test
    public void testGetFlatEntityNodeTypeInterface() throws Exception {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (InputStream yamlFile = toscaExtensionYamlUtil.loadYamlFileIs(
                "/mock/analyzerService/ServiceTemplateInterfaceInheritanceTest.yaml")) {

            ServiceTemplate serviceTemplateFromYaml =
                    toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);

            ToscaFlatData flatData = toscaAnalyzerService.getFlatEntity(ToscaElementTypes.NODE_TYPE, CMAUI_IMAGE_EXTEND,
                    serviceTemplateFromYaml, toscaServiceModel);

            Assert.assertNotNull(flatData);
            Assert.assertNotNull(flatData.getFlatEntity());
            NodeType flatEntity = (NodeType) flatData.getFlatEntity();
            Assert.assertNotNull(flatEntity.getInterfaces());
            Object standardInterfaceObj = flatEntity.getInterfaces().get(STANDARD_INTERFACE_KEY);
            Assert.assertNotNull(standardInterfaceObj);
            InterfaceDefinitionType standardInterface = new InterfaceDefinitionType(standardInterfaceObj);
            Assert.assertEquals(2, standardInterface.getInputs().size());
            Assert.assertEquals(3, standardInterface.getOperations().size());
            OperationDefinitionType createOperation = toscaExtensionYamlUtil.yamlToObject(
                    toscaExtensionYamlUtil.objectToYaml(standardInterface.getOperations().get("create")),
                    OperationDefinitionType.class);
            Assert.assertEquals(2, createOperation.getInputs().size());

            List<String> inheritanceHierarchyType = flatData.getInheritanceHierarchyType();
            Assert.assertNotNull(inheritanceHierarchyType);
            Assert.assertEquals(5, inheritanceHierarchyType.size());
        }
    }


    @Test
    public void testGetFlatEntityDataType() throws Exception {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (InputStream yamlFile = toscaExtensionYamlUtil.loadYamlFileIs(
                "/mock/analyzerService/NestedServiceTemplateReqTest.yaml")) {

            ServiceTemplate serviceTemplateFromYaml =
                    toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);

            ToscaFlatData flatData = toscaAnalyzerService.getFlatEntity(ToscaElementTypes.DATA_TYPE,
                    "org.openecomp.datatypes.heat.network.MyNewAddressPair", serviceTemplateFromYaml,
                    toscaServiceModel);

            Assert.assertNotNull(flatData);
            Assert.assertNotNull(flatData.getFlatEntity());
            DataType flatEntity = (DataType) flatData.getFlatEntity();
            Assert.assertEquals("org.openecomp.datatypes.heat.network.MyAddressPair", flatEntity.getDerived_from());
            Assert.assertEquals(3, flatEntity.getProperties().size());
            Assert.assertEquals("overridden default value",
                    flatEntity.getProperties().get("mac_address").get_default());
            Assert.assertEquals(true, flatEntity.getProperties().get("mac_address").getRequired());
            Assert.assertNotNull(flatEntity.getProperties().get("new_property"));

            List<String> inheritanceHierarchyType = flatData.getInheritanceHierarchyType();
            Assert.assertNotNull(inheritanceHierarchyType);
            Assert.assertEquals(4, inheritanceHierarchyType.size());
        }
    }

    @Test
    public void testGetFlatEntityDataTypeDerivedFromPrimitive() throws Exception {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (InputStream yamlFile = toscaExtensionYamlUtil.loadYamlFileIs(
                "/mock/analyzerService/ServiceTemplateDatatypeFlatTest.yaml")) {

            ServiceTemplate serviceTemplateFromYaml =
                    toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);

            ToscaFlatData flatData = toscaAnalyzerService.getFlatEntity(ToscaElementTypes.DATA_TYPE,
                    "org.openecomp.datatypes.heat.network.MyNewString", serviceTemplateFromYaml, toscaServiceModel);

            Assert.assertNotNull(flatData);
            Assert.assertNotNull(flatData.getFlatEntity());
            DataType flatEntity = (DataType) flatData.getFlatEntity();
            Assert.assertEquals("org.openecomp.datatypes.heat.network.MyString", flatEntity.getDerived_from());
            Assert.assertEquals(2, flatEntity.getConstraints().size());
            Assert.assertNotNull(flatEntity.getConstraints().get(0).getValid_values());
            Assert.assertNotNull(flatEntity.getConstraints().get(1).getMax_length());

            List<String> inheritanceHierarchyType = flatData.getInheritanceHierarchyType();
            Assert.assertNotNull(inheritanceHierarchyType);
            Assert.assertEquals(2, inheritanceHierarchyType.size());
        }
    }

    @Test
    public void testCalculateExposedRequirementsNull() {
        assertTrue(toscaAnalyzerService.calculateExposedRequirements(null, null).isEmpty());
    }

    @Test
    public void testCalculateExposedRequirements() {
        RequirementDefinition rd = new RequirementDefinition();
        rd.setCapability("tosca.capabilities.Node");
        rd.setNode("tosca.nodes.Root");
        rd.setRelationship("tosca.relationships.DependsOn");
        Object[] occurences = new Object[] {0, "UNBOUNDED"};
        rd.setOccurrences(occurences);

        rd.setCapability("tosca.capabilities.network.Bindable");
        rd.setNode(null);
        rd.setRelationship("tosca.relationships.network.BindsTo");
        Object[] occurences1 = new Object[] {1, 1};
        RequirementDefinition rd1 = new RequirementDefinition();
        rd1.setOccurrences(occurences1);

        Map<String, RequirementDefinition> nodeTypeRequirementDefinition = new HashMap<>();
        nodeTypeRequirementDefinition.put("binding", rd1);
        nodeTypeRequirementDefinition.put("dependency", rd);

        RequirementAssignment ra = new RequirementAssignment();
        ra.setCapability("tosca.capabilities.network.Bindable");
        ra.setNode("pd_server");
        ra.setRelationship("tosca.relationships.network.BindsTo");
        Map<String, RequirementAssignment> nodeTemplateRequirementsAssignment = new HashMap<>();
        nodeTemplateRequirementsAssignment.put("binding", ra);

        List<Map<String, RequirementDefinition>> nodeTypeRequirementsDefinition = new ArrayList<>();
        nodeTypeRequirementsDefinition.add(nodeTypeRequirementDefinition);

        List<Map<String, RequirementDefinition>> exposedRequirements = toscaAnalyzerService
                .calculateExposedRequirements(
                        nodeTypeRequirementsDefinition,
                        nodeTemplateRequirementsAssignment);
        Assert.assertEquals(1, exposedRequirements.size());
    }

    @Test
    public void testCalExpReqWithNullNodeInReqAssignment() {
        RequirementDefinition rd = new RequirementDefinition();
        rd.setCapability("tosca.capabilities.Node");
        rd.setNode("tosca.nodes.Root");
        rd.setRelationship("tosca.relationships.DependsOn");
        Object[] occurences = new Object[] {0, "UNBOUNDED"};
        rd.setOccurrences(occurences);

        rd.setCapability("tosca.capabilities.network.Bindable");
        rd.setNode(null);
        rd.setRelationship("tosca.relationships.network.BindsTo");
        Object[] occurences1 = new Object[] {1, 1};
        RequirementDefinition rd1 = new RequirementDefinition();
        rd1.setOccurrences(occurences1);

        Map<String, RequirementDefinition> nodeTypeRequirementDefinition = new HashMap<>();
        nodeTypeRequirementDefinition.put("binding", rd1);
        nodeTypeRequirementDefinition.put("dependency", rd);

        RequirementAssignment ra = new RequirementAssignment();
        ra.setCapability("tosca.capabilities.network.Bindable");
        ra.setNode(null);
        ra.setRelationship("tosca.relationships.network.BindsTo");
        Map<String, RequirementAssignment> nodeTemplateRequirementsAssignment = new HashMap<>();
        nodeTemplateRequirementsAssignment.put("binding", ra);

        List<Map<String, RequirementDefinition>> nodeTypeRequirementsDefinition = new ArrayList<>();
        nodeTypeRequirementsDefinition.add(nodeTypeRequirementDefinition);

        List<Map<String, RequirementDefinition>> exposedRequirements = toscaAnalyzerService
                .calculateExposedRequirements(
                        nodeTypeRequirementsDefinition,
                        nodeTemplateRequirementsAssignment);
        Assert.assertEquals(1, exposedRequirements.size());
    }

    @Test
    public void testCalculateExposedCapabilities() {
        Map<String, CapabilityDefinition> nodeTypeCapabilitiesDefinition = new HashMap<>();
        CapabilityDefinition cd = new CapabilityDefinition();
        cd.setType("tosca.capabilities.Scalable");
        nodeTypeCapabilitiesDefinition.put("tosca.capabilities.network.Bindable_pd_server", cd);
        RequirementAssignment ra = new RequirementAssignment();
        ra.setCapability("tosca.capabilities.network.Bindable");
        ra.setNode("pd_server");
        ra.setRelationship("tosca.relationships.network.BindsTo");
        Map<String, RequirementAssignment> nodeTemplateRequirementsAssignment = new HashMap<>();
        nodeTemplateRequirementsAssignment.put("binding", ra);
        Map<String, Map<String, RequirementAssignment>> fullFilledRequirementsDefinition = new HashMap<>();
        fullFilledRequirementsDefinition.put("pd_server", nodeTemplateRequirementsAssignment);
        Map<String, CapabilityDefinition> exposedCapabilities = toscaAnalyzerService.calculateExposedCapabilities(
                nodeTypeCapabilitiesDefinition, fullFilledRequirementsDefinition);
        Assert.assertEquals(1, exposedCapabilities.size());
    }

    @Test
    public void testIsRequirementExistsWithInvalidReqId() throws Exception {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (InputStream yamlFile = toscaExtensionYamlUtil.loadYamlFileIs(
                "/mock/analyzerService/NestedServiceTemplateReqTest.yaml")) {

            ServiceTemplate serviceTemplateFromYaml =
                    toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);

            TestUtil.createConcreteRequirementObjectsInServiceTemplate(serviceTemplateFromYaml, toscaExtensionYamlUtil);


            RequirementAssignment ra = new RequirementAssignment();
            ra.setCapability("tosca.capabilities.network.Bindable");
            ra.setNode("server_cmaui");
            ra.setRelationship("tosca.relationships.network.BindsTo");

            NodeTemplate port0 = serviceTemplateFromYaml.getTopology_template().getNode_templates().get("cmaui_port_0");
            //Test With Empty requirementId
            Assert.assertFalse(toscaAnalyzerService.isRequirementExistInNodeTemplate(port0, "", ra));

            //Test With valid requirementId
            Assert.assertTrue(toscaAnalyzerService.isRequirementExistInNodeTemplate(port0, "binding", ra));

            //Test With invalid requirement assignment
            RequirementAssignment ra1 = new RequirementAssignment();
            ra1.setCapability("tosca.capabilities.network.Bindable1");
            ra1.setNode("server_cmaui1");
            ra1.setRelationship("tosca.relationships.network.BindsTo1");
            Assert.assertFalse(toscaAnalyzerService.isRequirementExistInNodeTemplate(port0, "binding", ra1));
        }
    }

    @Test
    public void testGetRequirements() throws Exception {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (InputStream yamlFile = toscaExtensionYamlUtil.loadYamlFileIs(
                "/mock/analyzerService/NestedServiceTemplateReqTest.yaml")) {

            ServiceTemplate serviceTemplateFromYaml =
                    toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);

            NodeTemplate port0 = serviceTemplateFromYaml.getTopology_template().getNode_templates().get("cmaui_port_0");
            List<RequirementAssignment> reqList =
                    toscaAnalyzerService.getRequirements(port0, ToscaConstants.BINDING_REQUIREMENT_ID);
            assertEquals(1, reqList.size());

            reqList.clear();
            NodeTemplate port1 =
                    serviceTemplateFromYaml.getTopology_template().getNode_templates().get("cmaui1_port_1");
            reqList = toscaAnalyzerService.getRequirements(port1, ToscaConstants.LINK_REQUIREMENT_ID);
            assertEquals(2, reqList.size());

            reqList.clear();
            reqList = toscaAnalyzerService.getRequirements(port0, ToscaConstants.LINK_REQUIREMENT_ID);
            assertEquals(0, reqList.size());
        }
    }

    @Test
    public void testGetNodeTemplateById() {
        ServiceTemplate emptyServiceTemplate = new ServiceTemplate();
        Optional<NodeTemplate> nodeTemplate =
                toscaAnalyzerService.getNodeTemplateById(emptyServiceTemplate, "test_net222");
        assertFalse(nodeTemplate.isPresent());

        ServiceTemplate mainServiceTemplate =
                toscaServiceModel.getServiceTemplates().get(toscaServiceModel.getEntryDefinitionServiceTemplate());
        nodeTemplate = toscaAnalyzerService.getNodeTemplateById(mainServiceTemplate, "test_net");
        assertTrue(nodeTemplate.isPresent());

        nodeTemplate = toscaAnalyzerService.getNodeTemplateById(mainServiceTemplate, "test_net222");
        assertFalse(nodeTemplate.isPresent());
    }

    @Test
    public void testGetSubstituteServiceTemplateName() {
        thrown.expect(CoreException.class);
        thrown.expectMessage(
                "Invalid Substitute Node Template invalid2, mandatory map property service_template_filter "
                        + "with mandatory key substitute_service_template must be defined.");

        Optional<String> substituteServiceTemplateName;

        ServiceTemplate mainServiceTemplate =
                toscaServiceModel.getServiceTemplates().get(toscaServiceModel.getEntryDefinitionServiceTemplate());
        Optional<NodeTemplate> notSubstitutableNodeTemplate =
                toscaAnalyzerService.getNodeTemplateById(mainServiceTemplate, "test_net");
        assertTrue(notSubstitutableNodeTemplate.isPresent());

        substituteServiceTemplateName = toscaAnalyzerService.getSubstituteServiceTemplateName("test_net",
                notSubstitutableNodeTemplate.get());
        assertFalse(substituteServiceTemplateName.isPresent());

        Optional<NodeTemplate> substitutableNodeTemplate =
                toscaAnalyzerService.getNodeTemplateById(mainServiceTemplate, "test_nested");
        assertTrue(substitutableNodeTemplate.isPresent());

        substituteServiceTemplateName = toscaAnalyzerService.getSubstituteServiceTemplateName("test_nested",
                substitutableNodeTemplate.get());
        assertTrue(substituteServiceTemplateName.isPresent());
        assertEquals("nestedServiceTemplate.yaml", substituteServiceTemplateName.get());

        NodeTemplate invalidSubstitutableNodeTemplate1 = new NodeTemplate();
        substituteServiceTemplateName =
                toscaAnalyzerService.getSubstituteServiceTemplateName("invalid1", invalidSubstitutableNodeTemplate1);
        assertFalse(substituteServiceTemplateName.isPresent());

        substitutableNodeTemplate.ifPresent(nodeTemplate -> {
            Object serviceTemplateFilter =
                    nodeTemplate.getProperties().get(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME);
            ((Map) serviceTemplateFilter).clear();
            toscaAnalyzerService.getSubstituteServiceTemplateName("invalid2", nodeTemplate);

        });
    }


    @Test
    public void testGetSubstitutableNodeTemplates() throws Exception {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (InputStream yamlFile = toscaExtensionYamlUtil.loadYamlFileIs(
                "/mock/analyzerService/ServiceTemplateSubstituteTest.yaml")) {
            ServiceTemplate serviceTemplateFromYaml =
                    toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);

            Map<String, NodeTemplate> substitutableNodeTemplates =
                    toscaAnalyzerService.getSubstitutableNodeTemplates(serviceTemplateFromYaml);
            assertEquals(2, substitutableNodeTemplates.size());
            assertNotNull(substitutableNodeTemplates.get("test_nested1"));
            assertNotNull(substitutableNodeTemplates.get("test_nested2"));

            ServiceTemplate emptyServiceTemplate = new ServiceTemplate();
            emptyServiceTemplate.setTopology_template(new TopologyTemplate());
            substitutableNodeTemplates = toscaAnalyzerService.getSubstitutableNodeTemplates(emptyServiceTemplate);
            assertEquals(0, substitutableNodeTemplates.size());
        }

        try (InputStream yamlFile = toscaExtensionYamlUtil.loadYamlFileIs(
                "/mock/analyzerService/NestedServiceTemplateReqTest.yaml")) {
            ServiceTemplate serviceTemplateFromYaml =
                    toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);
            Map<String, NodeTemplate> substitutableNodeTemplates =
                    toscaAnalyzerService.getSubstitutableNodeTemplates(serviceTemplateFromYaml);
            assertEquals(0, substitutableNodeTemplates.size());
        }
    }

    @Test
    public void testGetSubstitutionMappedNodeTemplateByExposedReq() throws Exception {
        thrown.expect(CoreException.class);
        thrown.expectMessage(
                "Invalid Tosca model data, missing 'Node Template' entry for 'Node Template' id cmaui_port_9");
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (InputStream yamlFile = toscaExtensionYamlUtil.loadYamlFileIs(
                "/mock/analyzerService/NestedServiceTemplateReqTest.yaml")) {
            ServiceTemplate nestedServiceTemplateFromYaml =
                    toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);

            Optional<Map.Entry<String, NodeTemplate>> mappedNodeTemplate =
                    toscaAnalyzerService.getSubstitutionMappedNodeTemplateByExposedReq(
                            "NestedServiceTemplateSubstituteTest.yaml",
                            nestedServiceTemplateFromYaml,
                            "local_storage_server_cmaui");

            assertTrue(mappedNodeTemplate.isPresent());
            mappedNodeTemplate.ifPresent(stringNodeTemplateEntry -> {
                assertEquals("server_cmaui", stringNodeTemplateEntry.getKey());
                assertNotNull(stringNodeTemplateEntry.getValue());
            });

            mappedNodeTemplate = toscaAnalyzerService.getSubstitutionMappedNodeTemplateByExposedReq(
                    "NestedServiceTemplateSubstituteTest.yaml", nestedServiceTemplateFromYaml,
                    "link_cmaui_port_invalid");
            assertTrue(mappedNodeTemplate.isPresent());
            mappedNodeTemplate.ifPresent(stringNodeTemplateEntry -> {
                assertEquals("server_cmaui", stringNodeTemplateEntry.getKey());
                assertNotNull(stringNodeTemplateEntry.getValue());
            });

            ServiceTemplate mainServiceTemplate =
                    toscaServiceModel.getServiceTemplates().get(toscaServiceModel.getEntryDefinitionServiceTemplate());
            mappedNodeTemplate = toscaAnalyzerService.getSubstitutionMappedNodeTemplateByExposedReq(
                    toscaServiceModel.getEntryDefinitionServiceTemplate(), mainServiceTemplate,
                    "local_storage_server_cmaui");
            assertFalse(mappedNodeTemplate.isPresent());
        }
    }

    @Test
    public void invalidSubstitutableMapping() {
        thrown.expect(CoreException.class);
        thrown.expectMessage(
                "Invalid Substitution Service Template invalidMappingServiceTemplate.yaml, "
                        + "missing mandatory file 'Node type' in substitution mapping.");
        ServiceTemplate invalidMappingServiceTemplate = new ServiceTemplate();
        invalidMappingServiceTemplate.setTopology_template(new TopologyTemplate());
        invalidMappingServiceTemplate.getTopology_template().setSubstitution_mappings(new SubstitutionMapping());
        toscaAnalyzerService.getSubstitutionMappedNodeTemplateByExposedReq("invalidMappingServiceTemplate.yaml",
                invalidMappingServiceTemplate, "local_storage_server_cmaui");
    }

    @Test
    public void substitutableMappingWithNoReqMap() {
        ServiceTemplate emptyReqMapping = new ServiceTemplate();
        emptyReqMapping.setTopology_template(new TopologyTemplate());
        emptyReqMapping.getTopology_template().setSubstitution_mappings(new SubstitutionMapping());
        emptyReqMapping.getTopology_template().getSubstitution_mappings().setNode_type("temp");
        ServiceTemplate mainServiceTemplate =
                toscaServiceModel.getServiceTemplates().get(toscaServiceModel.getEntryDefinitionServiceTemplate());
        Optional<Map.Entry<String, NodeTemplate>> mappedNodeTemplate =
                toscaAnalyzerService.getSubstitutionMappedNodeTemplateByExposedReq(
                        toscaServiceModel.getEntryDefinitionServiceTemplate(), mainServiceTemplate,
                        "local_storage_server_cmaui");
        assertFalse(mappedNodeTemplate.isPresent());
    }

    @Test
    public void testGetSubstitutionMappedNodeTemplateByExposedReqInvalid() throws Exception {
        thrown.expect(CoreException.class);
        thrown.expectMessage(
                "Invalid Tosca model data, missing 'Node Template' entry for 'Node Template' id cmaui_port_9");
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (InputStream yamlFile = toscaExtensionYamlUtil.loadYamlFileIs(
                "/mock/analyzerService/NestedServiceTemplateReqTest.yaml")) {
            ServiceTemplate nestedServiceTemplateFromYaml =
                    toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);

            toscaAnalyzerService
                    .getSubstitutionMappedNodeTemplateByExposedReq("NestedServiceTemplateSubstituteTest.yaml",
                            nestedServiceTemplateFromYaml, "link_cmaui_port_invalid");
        }
    }

    @Test
    public void testIsDesiredRequirementAssignmentMatch() {

        RequirementAssignment requirementAssignment = new RequirementAssignment();
        String capability = "Test.Capability";
        String node = "Test.node";
        String relationship = "Test.relationship";
        requirementAssignment.setCapability(capability);
        requirementAssignment.setNode(node);
        requirementAssignment.setRelationship(relationship);

        assertTrue(toscaAnalyzerService
                .isDesiredRequirementAssignment(requirementAssignment, capability, node, relationship));
        assertTrue(
                toscaAnalyzerService.isDesiredRequirementAssignment(requirementAssignment, null, node, relationship));
        assertTrue(toscaAnalyzerService
                .isDesiredRequirementAssignment(requirementAssignment, capability, null, relationship));
        assertTrue(toscaAnalyzerService.isDesiredRequirementAssignment(requirementAssignment, capability, node, null));
        assertTrue(
                toscaAnalyzerService.isDesiredRequirementAssignment(requirementAssignment, null, null, relationship));
        assertTrue(toscaAnalyzerService.isDesiredRequirementAssignment(requirementAssignment, capability, null, null));
        assertTrue(toscaAnalyzerService.isDesiredRequirementAssignment(requirementAssignment, null, node, null));

    }

    @Test
    public void testIsDesiredRequirementAssignmentNoMatch() {

        RequirementAssignment requirementAssignment = new RequirementAssignment();
        String capability = "Test.Capability";
        String node = "Test.node";
        String relationship = "Test.relationship";
        requirementAssignment.setCapability(capability);
        requirementAssignment.setNode(node);
        requirementAssignment.setRelationship(relationship);

        assertFalse(
                toscaAnalyzerService.isDesiredRequirementAssignment(requirementAssignment, "no", node, relationship));
        assertFalse(
                toscaAnalyzerService.isDesiredRequirementAssignment(requirementAssignment, "no", "no", relationship));
        assertFalse(toscaAnalyzerService.isDesiredRequirementAssignment(requirementAssignment, "no", "no", "no"));
        assertFalse(toscaAnalyzerService
                .isDesiredRequirementAssignment(requirementAssignment, capability, "no", relationship));
        assertFalse(toscaAnalyzerService.isDesiredRequirementAssignment(requirementAssignment, capability, node, "no"));
        assertFalse(toscaAnalyzerService.isDesiredRequirementAssignment(requirementAssignment, capability, "no", "no"));
        assertFalse(toscaAnalyzerService.isDesiredRequirementAssignment(requirementAssignment, "no", null, null));
        assertFalse(toscaAnalyzerService.isDesiredRequirementAssignment(requirementAssignment, null, null, null));


    }

    @Test
    public void shouldReturnFalseIfNdTmpIsNull() {
        NodeTemplate nodeTemplate = null;
        assertFalse(toscaAnalyzerService.isTypeOf(nodeTemplate, ToscaNodeType.NATIVE_NETWORK, new ServiceTemplate(),
                toscaServiceModelMock));
    }

    @Test
    public void shouldReturnTrueIfNdTmpTypeIsOfRequestedType() {
        NodeTemplate nodeTemplate = new NodeTemplate();
        String nodeTypeToSearch = ToscaNodeType.NATIVE_BLOCK_STORAGE;
        nodeTemplate.setType(nodeTypeToSearch);
        assertTrue(toscaAnalyzerService
                .isTypeOf(nodeTemplate, nodeTypeToSearch, new ServiceTemplate(), toscaServiceModelMock));
    }

    @Test
    public void shouldReturnTrueIfDataTypeIsOfRequestedType() {
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        String propertyTypeToSearch = "tosca.datatypes.TimeInterval";
        propertyDefinition.setType(propertyTypeToSearch);
        assertTrue(toscaAnalyzerService.isTypeOf(propertyDefinition, propertyTypeToSearch, new ServiceTemplate(),
                toscaServiceModelMock));
    }

    @Test
    public void shouldReturnTrueIfInterfaceTypeIsOfRequestedType() {
        InterfaceDefinitionType interfaceDefinition = new InterfaceDefinitionType();
        String interfaceTypeToSearch = "test.interface.A";
        interfaceDefinition.setType(interfaceTypeToSearch);
        assertTrue(toscaAnalyzerService.isTypeOf(interfaceDefinition, interfaceTypeToSearch, new ServiceTemplate(),
                toscaServiceModelMock));
    }

    @Test
    public void interfaceInheritanceNoOperIsTypeTrue() throws IOException {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (InputStream yamlFile = toscaExtensionYamlUtil.loadYamlFileIs(
                "/mock/analyzerService/ServiceTemplateInterfaceInheritanceTest.yaml")) {
            interfaceIsTypeTest(toscaExtensionYamlUtil, CMAUI_IMAGE_EXTEND, yamlFile);
        }
    }

    private void interfaceIsTypeTest(ToscaExtensionYamlUtil toscaExtensionYamlUtil, String nodeTypeKey,
                                     InputStream yamlFile) {
        ServiceTemplate serviceTemplateFromYaml = toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);

        ToscaFlatData flatData = toscaAnalyzerService.getFlatEntity(ToscaElementTypes.NODE_TYPE, nodeTypeKey,
                serviceTemplateFromYaml, toscaServiceModel);

        Assert.assertNotNull(flatData);
        Object standardInterfaceDefinition =
                ((NodeType) flatData.getFlatEntity()).getInterfaces().get(STANDARD_INTERFACE_KEY);
        InterfaceDefinitionType standardInterfaceDefinitionType =
                new InterfaceDefinitionType(standardInterfaceDefinition);
        assertTrue(toscaAnalyzerService
                .isTypeOf(standardInterfaceDefinitionType, TOSCA_LIFECYCLE_STANDARD, serviceTemplateFromYaml,
                        toscaServiceModel));
    }

    @Test
    public void interfaceInheritanceWithOperIsTypeTrue() throws IOException {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (InputStream yamlFile = toscaExtensionYamlUtil.loadYamlFileIs(
                "/mock/analyzerService/ServiceTemplateInterfaceInheritanceTest.yaml")) {
            interfaceIsTypeTest(toscaExtensionYamlUtil, CMAUI_INTERFACE_TEST, yamlFile);
        }
    }


    @Test
    public void shouldReturnTrueIfNdTmpTypeIsFoundInSrvTmpNdTyAndNdTyDerivedFromRequestedType() {
        String typeToMatch = ToscaNodeType.CINDER_VOLUME;
        when(nodeTemplateMock.getType()).thenReturn(typeToMatch);
        Map<String, NodeType> stNodeTypes = new HashMap<>();
        addNodeType(stNodeTypes, ToscaNodeType.NATIVE_COMPUTE, new NodeType());
        NodeType nodeType = createNodeType(ToscaNodeType.NATIVE_BLOCK_STORAGE);
        addNodeType(stNodeTypes, typeToMatch, nodeType);
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setNode_types(stNodeTypes);
        assertTrue(toscaAnalyzerService.isTypeOf(nodeTemplateMock, ToscaNodeType.NATIVE_BLOCK_STORAGE, serviceTemplate,
                toscaServiceModelMock));

    }

    @Test
    public void dataTypeParameterExistInHierarchy() {
        String testedDataTypeKey = "test.dataType.B";
        when(parameterDefinitionMock.getType()).thenReturn(testedDataTypeKey);
        dataTypeExistInHierarchy(testedDataTypeKey, parameterDefinitionMock);

    }

    @Test
    public void dataTypePropertyExistInHierarchy() {
        String testedDataTypeKey = "test.dataType.B";
        when(propertyDefinitionMock.getType()).thenReturn(testedDataTypeKey);
        dataTypeExistInHierarchy(testedDataTypeKey, propertyDefinitionMock);
    }

    private void dataTypeExistInHierarchy(String testedDataTypeKey, DefinitionOfDataType testedDefinitionDataType) {
        String typeToMatch = "test.dataType.A";
        Map<String, DataType> stDataTypes = new HashMap<>();
        addDataType(stDataTypes, "tosca.datatypes.network.NetworkInfo", new DataType());
        DataType testedDataType = createDataType(typeToMatch);
        addDataType(stDataTypes, testedDataTypeKey, testedDataType);
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setData_types(stDataTypes);
        assertTrue(toscaAnalyzerService
                .isTypeOf(testedDefinitionDataType, typeToMatch, serviceTemplate, toscaServiceModelMock));
    }

    @Test
    public void interfaceTypeExistInHierarchy() {
        String typeToMatch = "test.interfaceType.A";
        String testedInterfaceTypeKey = "test.interfaceType.B";
        when(interfaceDefinitionMock.getType()).thenReturn(testedInterfaceTypeKey);
        Map<String, Object> stInterfaceTypes = new HashMap<>();
        stInterfaceTypes.put("tosca.interfaces.network.NetworkInfo", new InterfaceType());
        InterfaceType testedInterfaceType = createInterfaceType(typeToMatch);
        stInterfaceTypes.put(testedInterfaceTypeKey, testedInterfaceType);
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setInterface_types(stInterfaceTypes);
        assertTrue(toscaAnalyzerService.isTypeOf(interfaceDefinitionMock, "test.interfaceType.A", serviceTemplate,
                toscaServiceModelMock));
    }

    @Test
    public void shouldThrowCoreExceptionForInvalidNodeType() {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Entity Type 'AAA' or one of its derivedFrom type hierarchy, is not defined in "
                + "tosca service model");
        when(nodeTemplateMock.getType()).thenReturn("AAA");
        Map<String, NodeType> stNodeTypes = new HashMap<>();
        addNodeType(stNodeTypes, "notImportant", new NodeType());
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setNode_types(stNodeTypes);
        toscaAnalyzerService
                .isTypeOf(nodeTemplateMock, ToscaNodeType.NATIVE_COMPUTE, serviceTemplate, toscaServiceModelMock);
    }

    @Test
    public void shouldThrowCoreExceptionForInvalidNodeType2Level() {
        thrown.expect(CoreException.class);
        thrown.expectMessage(
                "Entity Type 'A' or one of its derivedFrom type hierarchy, is not defined in tosca " + "service model");
        String typeToMatch = "A";
        when(nodeTemplateMock.getType()).thenReturn(typeToMatch);
        Map<String, NodeType> stNodeTypes = new HashMap<>();
        addNodeType(stNodeTypes, "notImportant", new NodeType());
        addNodeType(stNodeTypes, "A", createNodeType("ADerivedFromB"));
        addNodeType(stNodeTypes, "ADerivedFromB'", createNodeType("BDerivedFromC"));
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setNode_types(stNodeTypes);
        assertTrue(toscaAnalyzerService
                .isTypeOf(nodeTemplateMock, "BDerivedFromC", serviceTemplate, toscaServiceModelMock));
    }

    @Test
    public void
    shouldReturnTrueIfNdTmpTypeIsFoundInSrvTmpNdTyAndNotDerivedFromRequestedTypeBut2ndLevelDerivedFromMatch() {
        String typeToMatch = "A";
        when(nodeTemplateMock.getType()).thenReturn(typeToMatch);
        Map<String, NodeType> stNodeTypes = new HashMap<>();
        addNodeType(stNodeTypes, "notImportant", new NodeType());
        addNodeType(stNodeTypes, "A", createNodeType("ADerivedFromB"));
        addNodeType(stNodeTypes, "ADerivedFromB", createNodeType("BDerivedFromC"));
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setNode_types(stNodeTypes);
        assertTrue(toscaAnalyzerService
                .isTypeOf(nodeTemplateMock, "BDerivedFromC", serviceTemplate, toscaServiceModelMock));
    }

    private NodeType createNodeType(String derivedFrom) {
        NodeType nodeType = new NodeType();
        nodeType.setDerived_from(derivedFrom);
        return nodeType;
    }

    private DataType createDataType(String derivedFrom) {
        DataType dataType = new DataType();
        dataType.setDerived_from(derivedFrom);
        return dataType;
    }

    private InterfaceType createInterfaceType(String derivedFrom) {
        InterfaceType interfaceType = new InterfaceType();
        interfaceType.setDerived_from(derivedFrom);
        return interfaceType;
    }

    private void addNodeType(Map<String, NodeType> stNodeTypes, String key, NodeType nodeType) {
        stNodeTypes.put(key, nodeType);
    }

    private void addDataType(Map<String, DataType> stDataTypes, String key, DataType dataType) {
        stDataTypes.put(key, dataType);
    }

    @Test
    public void shouldReturnTrueIfNdTmpTypeIsFoundInSrvTmpNdTyButRequestedTypeNotMatchButFoundIn1stLevelImports() {
        String typeToMatch = ToscaNodeType.CINDER_VOLUME;
        when(nodeTemplateMock.getType()).thenReturn(typeToMatch);
        List<Map<String, Import>> imports = new ArrayList<>();
        Map<String, Import> importMap = new HashMap<>();
        Import anImport = new Import();
        anImport.setFile("mainImport");
        importMap.put("bla bla", anImport);
        imports.add(importMap);
        ServiceTemplate mainSt = new ServiceTemplate();
        mainSt.setImports(imports);

        //create searchable service template
        Map<String, NodeType> stNodeTypes = new HashMap<>();
        addNodeType(stNodeTypes, ToscaNodeType.NATIVE_COMPUTE, new NodeType());
        NodeType nodeType = createNodeType(ToscaNodeType.NATIVE_BLOCK_STORAGE);
        addNodeType(stNodeTypes, typeToMatch, nodeType);
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setNode_types(stNodeTypes);

        // add service templates to tosca service model
        Map<String, ServiceTemplate> serviceTemplates = toscaServiceModelMock.getServiceTemplates();
        serviceTemplates.put("testMainServiceTemplate", mainSt);
        serviceTemplates.put("mainImport", serviceTemplate);
        when(toscaServiceModelMock.getServiceTemplates()).thenReturn(serviceTemplates);

        assertTrue(toscaAnalyzerService.isTypeOf(nodeTemplateMock, ToscaNodeType.NATIVE_BLOCK_STORAGE, mainSt,
                toscaServiceModelMock));
    }

    @Test
    public void shouldReturnTrueIfNdTmpTypeIsFoundInSrvTmpNdTyButRequestedTypeNotMatchButFoundIn2ndLevelImports() {
        String typeToMatch = ToscaNodeType.CINDER_VOLUME;
        when(nodeTemplateMock.getType()).thenReturn(typeToMatch);
        List<Map<String, Import>> imports = new ArrayList<>();
        Map<String, Import> importMap = new HashMap<>();
        Import anImport = new Import();
        anImport.setFile("refToMainImport");
        importMap.put("bla bla", anImport);
        imports.add(importMap);
        ServiceTemplate mainSt = new ServiceTemplate();
        mainSt.setImports(imports);

        //create searchable service template
        Map<String, NodeType> stNodeTypes = new HashMap<>();
        addNodeType(stNodeTypes, ToscaNodeType.NATIVE_COMPUTE, new NodeType());
        NodeType nodeType = createNodeType(ToscaNodeType.NATIVE_BLOCK_STORAGE);
        addNodeType(stNodeTypes, typeToMatch, nodeType);
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setNode_types(stNodeTypes);

        // create 1st level service template with import only
        List<Map<String, Import>> firstLevelImports = new ArrayList<>();
        Map<String, Import> firstLevelImportsMap = new HashMap<>();
        Import firstLevelImport = new Import();
        firstLevelImport.setFile("mainImport");
        firstLevelImportsMap.put("bla bla 2", firstLevelImport);
        firstLevelImports.add(firstLevelImportsMap);
        ServiceTemplate firstLevelSt = new ServiceTemplate();
        firstLevelSt.setImports(firstLevelImports);

        // add service templates to tosca service model
        Map<String, ServiceTemplate> serviceTemplates = toscaServiceModelMock.getServiceTemplates();
        serviceTemplates.put("testMainServiceTemplate", mainSt);
        serviceTemplates.put("refToMainImport", firstLevelSt);
        serviceTemplates.put("mainImport", serviceTemplate);
        when(toscaServiceModelMock.getServiceTemplates()).thenReturn(serviceTemplates);

        assertTrue(toscaAnalyzerService.isTypeOf(nodeTemplateMock, ToscaNodeType.NATIVE_BLOCK_STORAGE, mainSt,
                toscaServiceModelMock));
    }

    // not found at all should throw core exception


    @Test
    public void capabilityDefinitionIsTypeOfDirectTypeFound() {
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setType(CAPABILITY_TYPE_A);
        assertTrue(toscaAnalyzerService.isTypeOf(capabilityDefinition, CAPABILITY_TYPE_A, new ServiceTemplate(),
                toscaServiceModelMock));
    }

    @Test
    public void capabilityDefinitionIsTypeOfReturnNo() {
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setType(CAPABILITY_TYPE_A);
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setCapability_types(new HashMap<>());
        CapabilityType capabilityType = new CapabilityType();
        capabilityType.setDerived_from(TOSCA_CAPABILITIES_ROOT);
        serviceTemplate.getCapability_types().put(CAPABILITY_TYPE_A, capabilityType);
        assertFalse(toscaAnalyzerService
                .isTypeOf(capabilityDefinition, CAPABILITY_TYPE_B, serviceTemplate, toscaServiceModelMock));
    }

    @Test
    public void capabilityDefinitionIsTypeOfInheritanceTypeFound() {
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setType(CAPABILITY_TYPE_A);
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setCapability_types(new HashMap<>());
        CapabilityType capabilityType = new CapabilityType();
        capabilityType.setDerived_from(CAPABILITY_TYPE_B);
        serviceTemplate.getCapability_types().put(CAPABILITY_TYPE_A, capabilityType);
        assertTrue(toscaAnalyzerService
                .isTypeOf(capabilityDefinition, CAPABILITY_TYPE_B, serviceTemplate, toscaServiceModelMock));
    }

    @Test
    public void testGetNodeTemplatesByTypeNodeTemplateIsEmpty() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());

        assertTrue(toscaAnalyzerService.getNodeTemplatesByType(serviceTemplate, null, null).isEmpty());
    }

    @Test
    public void testGetNodeTemplatesByTypeDifferentType() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());
        serviceTemplate.setNode_types(Collections.singletonMap("nodeType", new NodeType()));

        NodeTemplate nodeTemplate = new NodeTemplate();
        nodeTemplate.setType("nodeType");

        serviceTemplate.getTopology_template().setNode_templates(Collections.singletonMap("node1", nodeTemplate));

        assertEquals(0, toscaAnalyzerService.getNodeTemplatesByType(
                serviceTemplate, "nodeType1", new ToscaServiceModel()).size());
    }

    @Test
    public void testGetNodeTemplatesByTypeSameType() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());

        NodeTemplate nodeTemplate = new NodeTemplate();
        nodeTemplate.setType("nodeType");

        serviceTemplate.getTopology_template().setNode_templates(Collections.singletonMap("node1", nodeTemplate));

        assertEquals(1, toscaAnalyzerService.getNodeTemplatesByType(
                serviceTemplate, "nodeType", new ToscaServiceModel()).size());
    }

    @Test
    public void testFetchNodeTypeNodeTypePresent() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setNode_types(Collections.singletonMap("nodeType", new NodeType()));

        Optional<NodeType> nodeType =
                toscaAnalyzerService.fetchNodeType("nodeType", Collections.singletonList(serviceTemplate));

        assertTrue(nodeType.isPresent());
    }

    @Test
    public void testFetchNodeTypeNodeTypeAbsent() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setNode_types(Collections.singletonMap("nodeType", new NodeType()));

        Optional<NodeType> nodeType =
                toscaAnalyzerService.fetchNodeType("nodeTypeAbsent", Collections.singletonList(serviceTemplate));

        assertFalse(nodeType.isPresent());
    }

    @Test
    public void testGetFlatEntityForCapability() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        CapabilityType capabilityType = new CapabilityType();
        capabilityType.setDescription("Capability");
        capabilityType.setVersion("1.0");

        serviceTemplate.setCapability_types(Collections.singletonMap("capabilityTypeId", capabilityType));
        ToscaFlatData toscaFlatData =
                toscaAnalyzerService.getFlatEntity(ToscaElementTypes.CAPABILITY_TYPE, "capabilityTypeId",
                        serviceTemplate, new ToscaServiceModel());

        assertNotNull(toscaFlatData);
        assertEquals(ToscaElementTypes.CAPABILITY_TYPE, toscaFlatData.getElementType());
    }

    @Test(expected = CoreException.class)
    public void testGetFlatEntityForCapabilityThrowsException() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();

        CapabilityType capabilityType = new CapabilityType();
        capabilityType.setDerived_from("tosca.capabilities.Root");

        serviceTemplate.setCapability_types(Collections.singletonMap("capabilityTypeId", capabilityType));

        toscaAnalyzerService.getFlatEntity(ToscaElementTypes.CAPABILITY_TYPE, "capabilityTypeId",
                serviceTemplate, new ToscaServiceModel());
    }

    @Test(expected = CoreException.class)
    public void testGetFlatEntityForCapabilityNullThrowsException() {
        toscaAnalyzerService.getFlatEntity(ToscaElementTypes.CAPABILITY_TYPE, "capabilityTypeId",
                new ServiceTemplate(), new ToscaServiceModel());
    }

    @Test
    public void testCreateInitSubstitutionNodeType() {
        ParameterDefinition parameterDefinitionInput = new ParameterDefinition();
        parameterDefinitionInput.setRequired(true);
        parameterDefinitionInput.set_default("default");
        parameterDefinitionInput.setConstraints(Collections.singletonList(new Constraint()));
        parameterDefinitionInput.setStatus(Status.SUPPORTED);

        ParameterDefinition parameterDefinitionOutput = new ParameterDefinition();
        parameterDefinitionOutput.setStatus(Status.SUPPORTED);

        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());
        serviceTemplate.getTopology_template()
                .setInputs(Collections.singletonMap("parameterDef1", parameterDefinitionInput));
        serviceTemplate.getTopology_template()
                .setOutputs(Collections.singletonMap("parameterDef1", parameterDefinitionOutput));

        NodeType nodeType = toscaAnalyzerService.createInitSubstitutionNodeType(serviceTemplate, "tosca.nodes.Root");

        assertNotNull(nodeType);
        assertTrue(nodeType.getProperties().size() ==1
                        && nodeType.getAttributes().size() == 1);
    }

    @Test(expected = CoreException.class)
    public void testGetSubstituteServiceTemplateNameThrowsException() {
        NodeTemplate nodeTemplate = new NodeTemplate();
        nodeTemplate.setDirectives(Collections.singletonList(ToscaConstants.NODE_TEMPLATE_DIRECTIVE_SUBSTITUTABLE));

        toscaAnalyzerService.getSubstituteServiceTemplateName(null, nodeTemplate);
    }

    @Test(expected = SdcRuntimeException.class)
    public void testGetFlatEntityThrowsExceptionIncorrectSwitchProvided() {
        toscaAnalyzerService.getFlatEntity(ToscaElementTypes.RELATIONSHIP_TYPE, null, null, null);
    }

    @Test
    public void getFullPathFromRelativePathBackwards(){
        ToscaAnalyzerServiceImpl toscaAnalyzerServiceImpl = new ToscaAnalyzerServiceImpl();
        String importFile = "../ImportedServiceTemplate";
        ServiceTemplate mainServiceTemplate = new ServiceTemplate();
        ServiceTemplate importedServiceTemplate = new ServiceTemplate();
        ToscaServiceModel toscaServiceModel = new ToscaServiceModel();
        toscaServiceModel.addServiceTemplate("Definitions/service/MainServiceTemplate", mainServiceTemplate);
        toscaServiceModel.addServiceTemplate("Definitions/ImportedServiceTemplate", importedServiceTemplate);

        String fileNameForImport = toscaAnalyzerServiceImpl
                           .fetchFullFileNameForImport(importFile, null, mainServiceTemplate, toscaServiceModel);
        assertEquals("Definitions/ImportedServiceTemplate", fileNameForImport);
    }

    @Test
    public void getFullPathFromRelativePathForwards(){
        ToscaAnalyzerServiceImpl toscaAnalyzerServiceImpl = new ToscaAnalyzerServiceImpl();
        String importFile = "services/ImportedServiceTemplate";
        ServiceTemplate mainServiceTemplate = new ServiceTemplate();
        ServiceTemplate importedServiceTemplate = new ServiceTemplate();
        ToscaServiceModel toscaServiceModel = new ToscaServiceModel();
        toscaServiceModel.addServiceTemplate("Definitions/MainServiceTemplate", mainServiceTemplate);
        toscaServiceModel.addServiceTemplate("Definitions/services/ImportedServiceTemplate", importedServiceTemplate);

        String fileNameForImport = toscaAnalyzerServiceImpl
                                           .fetchFullFileNameForImport(importFile, null, mainServiceTemplate, toscaServiceModel);
        assertEquals("Definitions/services/ImportedServiceTemplate", fileNameForImport);
    }

    @Test
    public void getFullPathFromRelativePathMix(){
        ToscaAnalyzerServiceImpl toscaAnalyzerServiceImpl = new ToscaAnalyzerServiceImpl();
        String importFile = "../types/global/ImportedServiceTemplate";
        ServiceTemplate mainServiceTemplate = new ServiceTemplate();
        ServiceTemplate importedServiceTemplate = new ServiceTemplate();
        ToscaServiceModel toscaServiceModel = new ToscaServiceModel();
        toscaServiceModel.addServiceTemplate("Definitions/services/MainServiceTemplate", mainServiceTemplate);
        toscaServiceModel.addServiceTemplate("Definitions/types/global/ImportedServiceTemplate", importedServiceTemplate);

        String fileNameForImport = toscaAnalyzerServiceImpl
                                           .fetchFullFileNameForImport(importFile, null, mainServiceTemplate, toscaServiceModel);
        assertEquals("Definitions/types/global/ImportedServiceTemplate", fileNameForImport);
    }

    @Test
    public void testConvertToscaImport() throws Exception {
        String inputResourceName = "/mock/analyzerService/importConvertTest.yml";
        byte[] uploadedFileData = IOUtils.toByteArray(this.getClass().getResource(inputResourceName));

        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        ToscaAnalyzerServiceImpl toscaAnalyzerServiceImpl = new ToscaAnalyzerServiceImpl();
        String convertServiceTemplateImport =
                toscaAnalyzerServiceImpl.convertServiceTemplateImport(toscaExtensionYamlUtil, uploadedFileData);

        Assert.assertNotNull(convertServiceTemplateImport);
        ServiceTemplate serviceTemplate =
                new YamlUtil().yamlToObject(convertServiceTemplateImport, ServiceTemplate.class);
        Assert.assertNotNull(serviceTemplate.getImports().get(0).get("data"));
        Assert.assertNotNull(serviceTemplate.getImports().get(1).get("artifacts"));
        Assert.assertNotNull(serviceTemplate.getImports().get(2).get("capabilities"));
        Assert.assertNotNull(serviceTemplate.getImports().get(3).get("api_interfaces"));
        Assert.assertNotNull(serviceTemplate.getImports().get(4).get("api_util_relationships"));
        Assert.assertNotNull(serviceTemplate.getImports().get(5).get("common"));
        Assert.assertNotNull(serviceTemplate.getImports().get(6).get("api_util"));
        Assert.assertNotNull(serviceTemplate.getImports().get(7).get("relationshipsExt"));
    }

    @Test
    public void loadValidToscaYamlFileTest() throws Exception {
        String inputResourceName = "/mock/analyzerService/ServiceTemplateInterfaceInheritanceTest.yaml";
        byte[] uploadedFileData = IOUtils.toByteArray(this.getClass().getResource(inputResourceName));

        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        ToscaAnalyzerServiceImpl toscaAnalyzerServiceImpl = new ToscaAnalyzerServiceImpl();
        ToscaServiceModel toscaServiceModel = new ToscaServiceModel();
        String fileFullName = "Definition/service.yaml";
        toscaAnalyzerServiceImpl
                .loadToscaYamlFile(toscaServiceModel, toscaExtensionYamlUtil, uploadedFileData, fileFullName);
        Assert.assertNotNull(toscaServiceModel.getServiceTemplate(fileFullName));
    }

    @Test
    public void loadInvalidToscaYamlFileTest() throws Exception {
        thrown.expect(CoreException.class);
        thrown.expectMessage(StringContains.containsString(
                "Tosca file 'Definition/service.yaml' is not following TOSCA spec, can't be parsed. Related error - "));
        String inputResourceName = "/mock/analyzerService/invalidToscaFileTest.yml";
        byte[] uploadedFileData = IOUtils.toByteArray(this.getClass().getResource(inputResourceName));

        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        ToscaAnalyzerServiceImpl toscaAnalyzerServiceImpl = new ToscaAnalyzerServiceImpl();
        ToscaServiceModel toscaServiceModel = new ToscaServiceModel();
        String fileFullName = "Definition/service.yaml";
        toscaAnalyzerServiceImpl
                .loadToscaYamlFile(toscaServiceModel, toscaExtensionYamlUtil, uploadedFileData, fileFullName);
    }

    @Test
    public void loadValidToscaMetadataFileTest() throws Exception {
        String inputResourceName = "/mock/analyzerService/validTosca.meta";
        byte[] uploadedFileData = IOUtils.toByteArray(this.getClass().getResource(inputResourceName));

        ToscaAnalyzerServiceImpl toscaAnalyzerServiceImpl = new ToscaAnalyzerServiceImpl();
        ToscaServiceModel toscaServiceModel = new ToscaServiceModel();
        toscaAnalyzerServiceImpl
                .loadToscaMetaFile(toscaServiceModel, uploadedFileData);
        Assert.assertEquals("Definitions/service-Service2-template.yml",
                toscaServiceModel.getEntryDefinitionServiceTemplate());
    }

    @Test
    public void loadInvalidToscaMetadataFileTest() throws Exception {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Missing data - TOSCA.meta file must include 'Entry-Definitions' data.");
        String inputResourceName = "/mock/analyzerService/invalidTosca.meta";
        byte[] uploadedFileData = IOUtils.toByteArray(this.getClass().getResource(inputResourceName));

        ToscaAnalyzerServiceImpl toscaAnalyzerServiceImpl = new ToscaAnalyzerServiceImpl();
        ToscaServiceModel toscaServiceModel = new ToscaServiceModel();
        toscaAnalyzerServiceImpl
                .loadToscaMetaFile(toscaServiceModel, uploadedFileData);
    }

    @Test
    public void loadToscaCsarPackageWithMetadataTest() throws Exception {
        String inputResourceName = "/mock/analyzerService/toscaPackageWithMetadata.csar";
        byte[] uploadedFileData = IOUtils.toByteArray(this.getClass().getResource(inputResourceName));
        //InputStream toscaPackage = new ByteArrayInputStream(uploadedFileData);
        ToscaAnalyzerServiceImpl toscaAnalyzerServiceImpl = new ToscaAnalyzerServiceImpl();
        ToscaServiceModel toscaServiceModel = toscaAnalyzerServiceImpl.loadToscaCsarPackage(uploadedFileData);
        assertNotNull(toscaServiceModel);
        assertEquals("Definitions/service.yaml", toscaServiceModel.getEntryDefinitionServiceTemplate());
        assertEquals(10, toscaServiceModel.getServiceTemplates().size());
        assertEquals(1, toscaServiceModel.getArtifactFiles().getFiles().size());
    }

    @Test
    public void loadToscaCsarPackageWithoutMetadataTest() throws Exception {
        String inputResourceName = "/mock/analyzerService/toscaPackageWithoutMetadata.csar";
        byte[] uploadedFileData = IOUtils.toByteArray(this.getClass().getResource(inputResourceName));
        //InputStream toscaPackage = new ByteArrayInputStream(uploadedFileData);
        ToscaAnalyzerServiceImpl toscaAnalyzerServiceImpl = new ToscaAnalyzerServiceImpl();
        ToscaServiceModel toscaServiceModel = toscaAnalyzerServiceImpl.loadToscaCsarPackage(uploadedFileData);
        assertNotNull(toscaServiceModel);
        assertEquals("service.yaml", toscaServiceModel.getEntryDefinitionServiceTemplate());
        assertEquals(10, toscaServiceModel.getServiceTemplates().size());
        assertEquals(1, toscaServiceModel.getArtifactFiles().getFiles().size());
    }

    @Test
    public void loadInvalidToscaCsarPackageWithoutEntryDefTest() throws Exception {
        thrown.expect(CoreException.class);
        thrown.expectMessage("TOSCA Entry Definition was not found");
        String inputResourceName = "/mock/analyzerService/toscaPackageInvalidEntryDef.csar";
        byte[] uploadedFileData = IOUtils.toByteArray(this.getClass().getResource(inputResourceName));
        //InputStream toscaPackage = new ByteArrayInputStream(uploadedFileData);
        ToscaAnalyzerServiceImpl toscaAnalyzerServiceImpl = new ToscaAnalyzerServiceImpl();
        toscaAnalyzerServiceImpl.loadToscaCsarPackage(uploadedFileData);
    }
}

