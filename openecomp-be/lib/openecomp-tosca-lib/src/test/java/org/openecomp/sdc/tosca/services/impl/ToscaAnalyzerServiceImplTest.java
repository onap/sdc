package org.openecomp.sdc.tosca.services.impl;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.tosca.TestUtil;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.yamlutil.ToscaExtensionYamlUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.openecomp.sdc.tosca.datatypes.model.Import;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.datatypes.model.SubstitutionMapping;
import org.openecomp.sdc.tosca.datatypes.model.TopologyTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ToscaAnalyzerServiceImplTest {
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
  NodeTemplate nodeTemplateMock;
  @Mock
  ToscaServiceModel toscaServiceModelMock;

  @BeforeClass
  public static void onlyOnceSetUp() throws IOException {
    toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
    toscaServiceModel = TestUtil.loadToscaServiceModel("/mock/analyzerService/toscasubstitution/",
        "/mock/globalServiceTemplates/", null);
  }

  @Before
  public void init() throws IOException {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testGetRequirements() throws Exception {
    ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
    InputStream yamlFile = toscaExtensionYamlUtil
        .loadYamlFileIs("/mock/analyzerService/NestedServiceTemplateReqTest.yaml");
    ServiceTemplate serviceTemplateFromYaml =
        toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);

    NodeTemplate port_0 =
        serviceTemplateFromYaml.getTopology_template().getNode_templates().get("cmaui_port_0");
    List<RequirementAssignment> reqList =
        toscaAnalyzerService.getRequirements(port_0, ToscaConstants.BINDING_REQUIREMENT_ID);
    assertEquals(1, reqList.size());

    reqList.clear();
    NodeTemplate port_1 =
        serviceTemplateFromYaml.getTopology_template().getNode_templates().get("cmaui1_port_1");
    reqList = toscaAnalyzerService.getRequirements(port_1, ToscaConstants.LINK_REQUIREMENT_ID);
    assertEquals(2, reqList.size());

    reqList.clear();
    reqList = toscaAnalyzerService.getRequirements(port_0, ToscaConstants.LINK_REQUIREMENT_ID);
    assertEquals(0, reqList.size());
  }

  @Test
  public void testGetNodeTemplateById() throws Exception {
    ServiceTemplate emptyServiceTemplate = new ServiceTemplate();
    Optional<NodeTemplate> nodeTemplate =
        toscaAnalyzerService.getNodeTemplateById(emptyServiceTemplate, "test_net222");
    assertEquals(false, nodeTemplate.isPresent());

    ServiceTemplate mainServiceTemplate = toscaServiceModel.getServiceTemplates()
        .get(toscaServiceModel.getEntryDefinitionServiceTemplate());
    nodeTemplate = toscaAnalyzerService.getNodeTemplateById(mainServiceTemplate, "test_net");
    assertEquals(true, nodeTemplate.isPresent());

    nodeTemplate = toscaAnalyzerService.getNodeTemplateById(mainServiceTemplate, "test_net222");
    assertEquals(false, nodeTemplate.isPresent());
  }

  @Test
  public void testGetSubstituteServiceTemplateName() throws Exception {
    thrown.expect(CoreException.class);
    thrown.expectMessage(
        "Invalid Substitute Node Template invalid2, mandatory map property service_template_filter with mandatory key substitute_service_template must be defined.");

    ServiceTemplate mainServiceTemplate = toscaServiceModel.getServiceTemplates()
        .get(toscaServiceModel.getEntryDefinitionServiceTemplate());
    Optional<NodeTemplate> notSubstitutableNodeTemplate =
        toscaAnalyzerService.getNodeTemplateById(mainServiceTemplate, "test_net");
    Optional<String> substituteServiceTemplateName = toscaAnalyzerService
        .getSubstituteServiceTemplateName("test_net", notSubstitutableNodeTemplate.get());
    assertEquals(false, substituteServiceTemplateName.isPresent());

    Optional<NodeTemplate> substitutableNodeTemplate =
        toscaAnalyzerService.getNodeTemplateById(mainServiceTemplate, "test_nested");
    substituteServiceTemplateName = toscaAnalyzerService
        .getSubstituteServiceTemplateName("test_nested", substitutableNodeTemplate.get());
    assertEquals(true, substituteServiceTemplateName.isPresent());
    assertEquals("nestedServiceTemplate.yaml", substituteServiceTemplateName.get());

    NodeTemplate invalidSubstitutableNodeTemplate1 = new NodeTemplate();
    substituteServiceTemplateName = toscaAnalyzerService
        .getSubstituteServiceTemplateName("invalid1", invalidSubstitutableNodeTemplate1);
    assertEquals(false, substituteServiceTemplateName.isPresent());


    NodeTemplate invalidSubstitutableNodeTemplate2 = substitutableNodeTemplate.get();
    Object serviceTemplateFilter = invalidSubstitutableNodeTemplate2.getProperties()
        .get(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME);
    ((Map) serviceTemplateFilter).clear();
    toscaAnalyzerService
        .getSubstituteServiceTemplateName("invalid2", invalidSubstitutableNodeTemplate2);


  }


  @Test
  public void testGetSubstitutableNodeTemplates() throws Exception {
    ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
    InputStream yamlFile = toscaExtensionYamlUtil
        .loadYamlFileIs("/mock/analyzerService/ServiceTemplateSubstituteTest.yaml");
    ServiceTemplate serviceTemplateFromYaml =
        toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);

    Map<String, NodeTemplate> substitutableNodeTemplates =
        toscaAnalyzerService.getSubstitutableNodeTemplates(serviceTemplateFromYaml);
    assertEquals(2, substitutableNodeTemplates.size());
    assertNotNull(substitutableNodeTemplates.get("test_nested1"));
    assertNotNull(substitutableNodeTemplates.get("test_nested2"));

    ServiceTemplate emptyServiceTemplate = new ServiceTemplate();
    emptyServiceTemplate.setTopology_template(new TopologyTemplate());
    substitutableNodeTemplates =
        toscaAnalyzerService.getSubstitutableNodeTemplates(emptyServiceTemplate);
    assertEquals(0, substitutableNodeTemplates.size());

    yamlFile = toscaExtensionYamlUtil
        .loadYamlFileIs("/mock/analyzerService/NestedServiceTemplateReqTest.yaml");
    serviceTemplateFromYaml = toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);
    substitutableNodeTemplates =
        toscaAnalyzerService.getSubstitutableNodeTemplates(serviceTemplateFromYaml);
    assertEquals(0, substitutableNodeTemplates.size());
  }

  @Test
  public void testGetSubstitutionMappedNodeTemplateByExposedReq() throws Exception {
    thrown.expect(CoreException.class);
    thrown.expectMessage(
        "Invalid Tosca model data, missing 'Node Template' entry for 'Node Template' id cmaui_port_9");
    ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
    InputStream yamlFile = toscaExtensionYamlUtil
        .loadYamlFileIs("/mock/analyzerService/NestedServiceTemplateReqTest.yaml");
    ServiceTemplate nestedServiceTemplateFromYaml =
        toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);

    Optional<Map.Entry<String, NodeTemplate>> mappedNodeTemplate = toscaAnalyzerService
        .getSubstitutionMappedNodeTemplateByExposedReq("NestedServiceTemplateSubstituteTest.yaml",
            nestedServiceTemplateFromYaml, "local_storage_server_cmaui");
    assertEquals("server_cmaui", mappedNodeTemplate.get().getKey());
    assertNotNull(mappedNodeTemplate.get().getValue());

    mappedNodeTemplate = toscaAnalyzerService
        .getSubstitutionMappedNodeTemplateByExposedReq("NestedServiceTemplateSubstituteTest.yaml",
            nestedServiceTemplateFromYaml, "link_cmaui_port_invalid");
    assertEquals("server_cmaui", mappedNodeTemplate.get().getKey());
    assertNotNull(mappedNodeTemplate.get().getValue());

    ServiceTemplate mainServiceTemplate = toscaServiceModel.getServiceTemplates()
        .get(toscaServiceModel.getEntryDefinitionServiceTemplate());
    mappedNodeTemplate = toscaAnalyzerService.getSubstitutionMappedNodeTemplateByExposedReq(
        toscaServiceModel.getEntryDefinitionServiceTemplate(), mainServiceTemplate,
        "local_storage_server_cmaui");
    assertEquals(false, mappedNodeTemplate.isPresent());
  }

  @Test
  public void invalidSubstitutableMapping() throws Exception {
    thrown.expect(CoreException.class);
    thrown.expectMessage(
        "Invalid Substitution Service Template invalidMappingServiceTemplate.yaml, missing mandatory file 'Node type' in substitution mapping.");
    ServiceTemplate invalidMappingServiceTemplate = new ServiceTemplate();
    invalidMappingServiceTemplate.setTopology_template(new TopologyTemplate());
    invalidMappingServiceTemplate.getTopology_template()
        .setSubstitution_mappings(new SubstitutionMapping());
    toscaAnalyzerService
        .getSubstitutionMappedNodeTemplateByExposedReq("invalidMappingServiceTemplate.yaml",
            invalidMappingServiceTemplate, "local_storage_server_cmaui");
  }

  @Test
  public void substitutableMappingWithNoReqMap() throws Exception {
    ServiceTemplate mainServiceTemplate = toscaServiceModel.getServiceTemplates()
        .get(toscaServiceModel.getEntryDefinitionServiceTemplate());
    ServiceTemplate emptyReqMapping = new ServiceTemplate();
    emptyReqMapping.setTopology_template(new TopologyTemplate());
    emptyReqMapping.getTopology_template().setSubstitution_mappings(new SubstitutionMapping());
    emptyReqMapping.getTopology_template().getSubstitution_mappings().setNode_type("temp");
    Optional<Map.Entry<String, NodeTemplate>> mappedNodeTemplate = toscaAnalyzerService
        .getSubstitutionMappedNodeTemplateByExposedReq(
            toscaServiceModel.getEntryDefinitionServiceTemplate(), mainServiceTemplate,
            "local_storage_server_cmaui");
    assertEquals(false, mappedNodeTemplate.isPresent());
  }

  @Test
  public void testGetSubstitutionMappedNodeTemplateByExposedReqInvalid() throws Exception {
    thrown.expect(CoreException.class);
    thrown.expectMessage(
        "Invalid Tosca model data, missing 'Node Template' entry for 'Node Template' id cmaui_port_9");
    ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
    InputStream yamlFile = toscaExtensionYamlUtil
        .loadYamlFileIs("/mock/analyzerService/NestedServiceTemplateReqTest.yaml");
    ServiceTemplate nestedServiceTemplateFromYaml =
        toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);

    toscaAnalyzerService
        .getSubstitutionMappedNodeTemplateByExposedReq("NestedServiceTemplateSubstituteTest.yaml",
            nestedServiceTemplateFromYaml, "link_cmaui_port_invalid");
  }

  @Test
  public void testIsDesiredRequirementAssignmentMatch() throws Exception {

    RequirementAssignment requirementAssignment = new RequirementAssignment();
    String capability = "Test.Capability";
    String node = "Test.node";
    String relationship = "Test.relationship";
    requirementAssignment.setCapability(capability);
    requirementAssignment.setNode(node);
    requirementAssignment.setRelationship(relationship);

    assertEquals(true, toscaAnalyzerService
        .isDesiredRequirementAssignment(requirementAssignment, capability, node, relationship));
    assertEquals(true, toscaAnalyzerService
        .isDesiredRequirementAssignment(requirementAssignment, null, node, relationship));
    assertEquals(true, toscaAnalyzerService
        .isDesiredRequirementAssignment(requirementAssignment, capability, null, relationship));
    assertEquals(true, toscaAnalyzerService
        .isDesiredRequirementAssignment(requirementAssignment, capability, node, null));
    assertEquals(true, toscaAnalyzerService
        .isDesiredRequirementAssignment(requirementAssignment, null, null, relationship));
    assertEquals(true, toscaAnalyzerService
        .isDesiredRequirementAssignment(requirementAssignment, capability, null, null));
    assertEquals(true, toscaAnalyzerService
        .isDesiredRequirementAssignment(requirementAssignment, null, node, null));

  }

  @Test
  public void testIsDesiredRequirementAssignmentNoMatch() throws Exception {

    RequirementAssignment requirementAssignment = new RequirementAssignment();
    String capability = "Test.Capability";
    String node = "Test.node";
    String relationship = "Test.relationship";
    requirementAssignment.setCapability(capability);
    requirementAssignment.setNode(node);
    requirementAssignment.setRelationship(relationship);

    assertEquals(false, toscaAnalyzerService
        .isDesiredRequirementAssignment(requirementAssignment, "no", node, relationship));
    assertEquals(false, toscaAnalyzerService
        .isDesiredRequirementAssignment(requirementAssignment, "no", "no", relationship));
    assertEquals(false, toscaAnalyzerService
        .isDesiredRequirementAssignment(requirementAssignment, "no", "no", "no"));
    assertEquals(false, toscaAnalyzerService
        .isDesiredRequirementAssignment(requirementAssignment, capability, "no", relationship));
    assertEquals(false, toscaAnalyzerService
        .isDesiredRequirementAssignment(requirementAssignment, capability, node, "no"));
    assertEquals(false, toscaAnalyzerService
        .isDesiredRequirementAssignment(requirementAssignment, capability, "no", "no"));
    assertEquals(false, toscaAnalyzerService
        .isDesiredRequirementAssignment(requirementAssignment, "no", null, null));
    assertEquals(false, toscaAnalyzerService
        .isDesiredRequirementAssignment(requirementAssignment, null, null, null));


  }

  @Test
  public void shouldReturnFalseIfNdTmpIsNull() {
    assertFalse(toscaAnalyzerService
        .isTypeOf(null, ToscaNodeType.NETWORK.getDisplayName(), new ServiceTemplate(),
            toscaServiceModelMock));
  }

  @Test
  public void shouldReturnTrueIfNdTmpTypeIsOfRequestedType() {
    NodeTemplate nodeTemplate = new NodeTemplate();
    ToscaNodeType nodeTypeToSearch = ToscaNodeType.BLOCK_STORAGE;
    nodeTemplate.setType(nodeTypeToSearch.getDisplayName());
    assertTrue(toscaAnalyzerService
        .isTypeOf(nodeTemplate, nodeTypeToSearch.getDisplayName(), new ServiceTemplate(),
            toscaServiceModelMock));
  }

  @Test
  public void shouldReturnTrueIfNdTmpTypeIsFoundInSrvTmpNdTyAndNdTyDerivedFromRequestedType() {
    String typeToMatch = ToscaNodeType.CINDER_VOLUME.getDisplayName();
    when(nodeTemplateMock.getType()).thenReturn(typeToMatch);
    Map<String, NodeType> stNodeTypes = new HashMap<>();
    addNodeType(stNodeTypes, ToscaNodeType.COMPUTE.getDisplayName(), new NodeType());
    NodeType nodeType = createNodeType(ToscaNodeType.BLOCK_STORAGE.getDisplayName());
    addNodeType(stNodeTypes, typeToMatch, nodeType);
    ServiceTemplate serviceTemplate = new ServiceTemplate();
    serviceTemplate.setNode_types(stNodeTypes);
    assertTrue(toscaAnalyzerService
        .isTypeOf(nodeTemplateMock, ToscaNodeType.BLOCK_STORAGE.getDisplayName(), serviceTemplate,
            toscaServiceModelMock));

  }

  @Test
  public void shouldThrowCoreExceptionForInvalidNodeType() {
    thrown.expect(CoreException.class);
    thrown.expectMessage(
        "NodeType 'AAA' or one of its derivedFrom node type hierarchy, is not defined in tosca service model");
    when(nodeTemplateMock.getType()).thenReturn("AAA");
    Map<String, NodeType> stNodeTypes = new HashMap<>();
    addNodeType(stNodeTypes, "notImportant", new NodeType());
    ServiceTemplate serviceTemplate = new ServiceTemplate();
    serviceTemplate.setNode_types(stNodeTypes);
    toscaAnalyzerService
        .isTypeOf(nodeTemplateMock, ToscaNodeType.COMPUTE.getDisplayName(), serviceTemplate,
            toscaServiceModelMock);
  }

  @Test
  public void shouldThrowCoreExceptionForInvalidNodeType2Level() {
    thrown.expect(CoreException.class);
    thrown.expectMessage(
        "NodeType 'A' or one of its derivedFrom node type hierarchy, is not defined in tosca service model");
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
  public void shouldReturnTrueIfNdTmpTypeIsFoundInSrvTmpNdTyAndNotDerivedFromRequestedTypeBut2ndLevelDerivedFromMatch() {
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

  private void addNodeType(Map<String, NodeType> stNodeTypes, String key, NodeType nodeType) {
    stNodeTypes.put(key, nodeType);
  }

  @Test
  public void shouldReturnTrueIfNdTmpTypeIsFoundInSrvTmpNdTyButRequestedTypeNotMatchButFoundIn1stLevelImports() {
    String typeToMatch = ToscaNodeType.CINDER_VOLUME.getDisplayName();
    when(nodeTemplateMock.getType()).thenReturn(typeToMatch);
    ServiceTemplate mainST = new ServiceTemplate();
    Map<String, Import> imports = new HashMap<>();
    Import anImport = new Import();
    anImport.setFile("mainImport");
    imports.put("bla bla", anImport);
    mainST.setImports(imports);

    //create searchable service template
    Map<String, NodeType> stNodeTypes = new HashMap<>();
    addNodeType(stNodeTypes, ToscaNodeType.COMPUTE.getDisplayName(), new NodeType());
    NodeType nodeType = createNodeType(ToscaNodeType.BLOCK_STORAGE.getDisplayName());
    addNodeType(stNodeTypes, typeToMatch, nodeType);
    ServiceTemplate serviceTemplate = new ServiceTemplate();
    serviceTemplate.setNode_types(stNodeTypes);

    // add service templates to tosca service model
    Map<String, ServiceTemplate> serviceTemplates = toscaServiceModelMock.getServiceTemplates();
    serviceTemplates.put("testMainServiceTemplate", mainST);
    serviceTemplates.put("mainImport", serviceTemplate);
    when(toscaServiceModelMock.getServiceTemplates()).thenReturn(serviceTemplates);

    assertTrue(toscaAnalyzerService
        .isTypeOf(nodeTemplateMock, ToscaNodeType.BLOCK_STORAGE.getDisplayName(), mainST,
            toscaServiceModelMock));
  }

  @Test
  public void shouldReturnTrueIfNdTmpTypeIsFoundInSrvTmpNdTyButRequestedTypeNotMatchButFoundIn2ndLevelImports() {
    String typeToMatch = ToscaNodeType.CINDER_VOLUME.getDisplayName();
    when(nodeTemplateMock.getType()).thenReturn(typeToMatch);
    ServiceTemplate mainST = new ServiceTemplate();
    Map<String, Import> imports = new HashMap<>();
    Import anImport = new Import();
    anImport.setFile("refToMainImport");
    imports.put("bla bla", anImport);
    mainST.setImports(imports);

    //create searchable service template
    Map<String, NodeType> stNodeTypes = new HashMap<>();
    addNodeType(stNodeTypes, ToscaNodeType.COMPUTE.getDisplayName(), new NodeType());
    NodeType nodeType = createNodeType(ToscaNodeType.BLOCK_STORAGE.getDisplayName());
    addNodeType(stNodeTypes, typeToMatch, nodeType);
    ServiceTemplate serviceTemplate = new ServiceTemplate();
    serviceTemplate.setNode_types(stNodeTypes);

    // create 1st level service template with import only
    ServiceTemplate firstLevelST = new ServiceTemplate();
    Map<String, Import> firstLevelImports = new HashMap<>();
    Import firstLevelImport = new Import();
    firstLevelImport.setFile("mainImport");
    firstLevelImports.put("bla bla 2", firstLevelImport);

    firstLevelST.setImports(firstLevelImports);

    // add service templates to tosca service model
    Map<String, ServiceTemplate> serviceTemplates = toscaServiceModelMock.getServiceTemplates();
    serviceTemplates.put("testMainServiceTemplate", mainST);
    serviceTemplates.put("refToMainImport", firstLevelST);
    serviceTemplates.put("mainImport", serviceTemplate);
    when(toscaServiceModelMock.getServiceTemplates()).thenReturn(serviceTemplates);

    assertTrue(toscaAnalyzerService
        .isTypeOf(nodeTemplateMock, ToscaNodeType.BLOCK_STORAGE.getDisplayName(), mainST,
            toscaServiceModelMock));
  }

  // not found at all should throw core exception


}