package org.openecomp.sdc.translator.services.heattotosca;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.openecomp.sdc.translator.TestUtils;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionEntity;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedSubstitutionData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.*;
import org.openecomp.sdc.translator.services.heattotosca.impl.unifiedcomposition.UnifiedCompositionSingleSubstitution;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class UnifiedCompositionServiceTest {
  @InjectMocks
  @Spy
  UnifiedCompositionService unifiedCompositionService;
  @Spy
  TranslationContext context;

  private static final String IN_PREFIX = "/in";
  private static final String OUT_PREFIX = "/out";
  private static final String FSB1_template = "FSB1_template";
  private static final String FSB2_template = "FSB2_template";
  private static final String FSB3_template = "FSB3_template";
  private static final String FSB1_INTERNAL = "FSB1_Internal";
  private static final String FSB2_INTERNAL = "FSB2_Internal";
  private static final String FSB1_INTERNAL1 = "FSB1_Internal1";
  private static final String FSB1_INTERNAL2 = "FSB1_Internal2";
  private static final String FSB2_INTERNAL1 = "FSB2_Internal1";
  private static final String FSB2_INTERNAL2 = "FSB2_Internal2";
  private static final String PORT = "port";
  private static final String PORT1 = "port1";
  private static final String PORT2 = "port2";
  private static final String FSB1_OAM = "FSB1_OAM";
  private static final String ORG_OPENECOMP_RESOURCE_ABSTRACT_NODES_FSB1 = "org.openecomp.resource.abstract.nodes.FSB1";
  private static final String ORG_OPENECOMP_RESOURCE_VFC_NODES_HEAT_FSB = "org.openecomp.resource.vfc.nodes.heat.FSB";
  private static final String DEVICE_OWNER = "device_owner";
  private static final String COMPLEX_OUTPUT1 = "complexOutput1";
  private static final String COMPLEX_OUTPUT2 = "complexOutput2";
  private static final String COMPLEX_OUTPUT3 = "complexOutput3";
  private static final String USER_DATA_FORMAT = "user_data_format";
  private static final String TENANT_ID = "tenant_id";
  private static final String SIMPLE_OUTPUT1 = "simpleOutput1";
  private static final String SIMPLE_OUTPUT2 = "simpleOutput2";
  private static final String ADDRESSES = "addresses";
  private static final String CMAUI_VOLUME1 = "cmaui_volume1";
  private static final String CMAUI_VOLUME2 = "cmaui_volume2";
  private static final String CMAUI_VOLUME3 = "cmaui_volume3";
  private static final String ACCESS_IPv4 = "accessIPv4";
  private static final String ACCESS_IPv6 = "accessIPv6";
  private static final String FSB1 = "FSB1";
  private static final String MYATTR = "myAttr";
  private static final String VOLUME_TYPE = "volume_type";
  private static final String SIZE = "size";
  private static final String NETWORK_ID = "network_id";
  private static final String JSA_NET1 = "jsa_net1";
  private static final String STATUS = "status";
  private static final String AVAILABILITY_ZONE = "availability_zone";
  private static final String DEPENDENCY = "dependency";

  Map<String, ServiceTemplate> inputServiceTemplates;
  Map<String, ServiceTemplate> expectedOutserviceTemplates;
  private static String mainSTName = "MainServiceTemplate.yaml";
  private static String substitutionST = "SubstitutionServiceTemplate.yaml";
  private static String globalSubstitutionST = "GlobalSubstitutionTypesServiceTemplate.yaml";

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  //todo
  @Test
  public void createUnifiedComposition() throws Exception {

  }

  @Test
  public void createSubstitutionStNoConsolidationData() throws Exception {
    String path = "/mock/services/heattotosca/unifiedComposition/creSubstitutionServiceTemplate/NoOutParamDuplicatePortType";
    loadInputAndOutputData(path);
    ServiceTemplate expectedServiceTemplate =
        TestUtils.loadServiceTemplate("/mock/services/heattotosca/unifiedComposition/creSubstitutionServiceTemplate/NoOutParamDuplicatePortType" + OUT_PREFIX);

    List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>();
    Optional<ServiceTemplate> substitutionServiceTemplate = unifiedCompositionService
        .createUnifiedSubstitutionServiceTemplate(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList,
            context, "org.openecomp.resource.vfc.nodes.heat.FSB1", null);
    assertEquals(false, substitutionServiceTemplate.isPresent());
  }

  @Test
  public void createSubstitutionStNoOutputParamAndDuplicatePortType() throws Exception {
    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL1));
    portTypeToIdList.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL2));
    portTypeToIdList.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL1));
    portTypeToIdList.add(new ImmutablePair<>(FSB1_OAM, FSB1_OAM));

    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/creSubstitutionServiceTemplate/NoOutParamDuplicatePortType");
    UnifiedCompositionData unifiedCompositionData =
        createCompositionData(FSB1_template, portTypeToIdList);
    List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>();
    unifiedCompositionDataList.add(unifiedCompositionData);

    Optional<ServiceTemplate> substitutionServiceTemplate = unifiedCompositionService
        .createUnifiedSubstitutionServiceTemplate(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList,
            context, ORG_OPENECOMP_RESOURCE_ABSTRACT_NODES_FSB1, null);
    assertEquals(true, substitutionServiceTemplate.isPresent());
    substitutionServiceTemplate
        .ifPresent(
            subServiceTemplate -> checkSTResults(expectedOutserviceTemplates,
                subServiceTemplate, context.getGlobalSubstitutionServiceTemplate(), null));
  }

  @Test
  public void createSubstitutionStWithOutputParamNoConsolidation() throws Exception {
    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL1));
    portTypeToIdList.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL2));
    portTypeToIdList.add(new ImmutablePair<>(FSB1_OAM, FSB1_OAM));

    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/creSubstitutionServiceTemplate/WithOutputParameters/noConsolidation");
    UnifiedCompositionData unifiedCompositionData =
        createCompositionData(FSB1_template, portTypeToIdList);
    addGetAttrForCompute(unifiedCompositionData);
    addGetAttrForPort(unifiedCompositionData);
    List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>();
    unifiedCompositionDataList.add(unifiedCompositionData);

    Optional<ServiceTemplate> substitutionServiceTemplate = unifiedCompositionService
        .createUnifiedSubstitutionServiceTemplate(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList,
            context, ORG_OPENECOMP_RESOURCE_ABSTRACT_NODES_FSB1, null);
    assertEquals(true, substitutionServiceTemplate.isPresent());
    substitutionServiceTemplate
        .ifPresent(
            subServiceTemplate -> checkSTResults(expectedOutserviceTemplates,
                subServiceTemplate, context.getGlobalSubstitutionServiceTemplate(), null));
  }

  @Test
  public void createSubstitutionStWithOutputParamWithConsolidation() throws Exception {
    List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>();
    List<Pair<String, String>> portTypeToIdList1 = new ArrayList<>();
    portTypeToIdList1.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL1));
    portTypeToIdList1.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL2));

    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/creSubstitutionServiceTemplate/WithOutputParameters/consolidation");
    UnifiedCompositionData unifiedCompositionData1 =
        createCompositionData(FSB1_template, portTypeToIdList1);
    addGetAttrForCompute(unifiedCompositionData1);
    addGetAttrForPort(unifiedCompositionData1);
    unifiedCompositionDataList.add(unifiedCompositionData1);

    List<Pair<String, String>> portTypeToIdList2 = new ArrayList<>();
    portTypeToIdList2.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL2));
    portTypeToIdList2.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL1));

    UnifiedCompositionData unifiedCompositionData2 =
        createCompositionData(FSB2_template, portTypeToIdList2);
    addGetAttrForCompute2(unifiedCompositionData2);
    addGetAttrForPort2(unifiedCompositionData2);
    unifiedCompositionDataList.add(unifiedCompositionData2);

    Optional<ServiceTemplate> substitutionServiceTemplate = unifiedCompositionService
        .createUnifiedSubstitutionServiceTemplate(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList,
            context, ORG_OPENECOMP_RESOURCE_ABSTRACT_NODES_FSB1, null);
    assertEquals(true, substitutionServiceTemplate.isPresent());
    substitutionServiceTemplate
        .ifPresent(
            subServiceTemplate -> checkSTResults(expectedOutserviceTemplates,
                subServiceTemplate, context.getGlobalSubstitutionServiceTemplate(), null));
  }

  @Test
  public void createSubstitutionStNoPorts() throws Exception {
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/creSubstitutionServiceTemplate/NoPorts");

    UnifiedCompositionData unifiedCompositionData = new UnifiedCompositionData();
    String computeNodeTemplateId = FSB1_template;
    unifiedCompositionData.setComputeTemplateConsolidationData(
        TestUtils.createComputeTemplateConsolidationData(computeNodeTemplateId, null, null));
    List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>();
    unifiedCompositionDataList.add(unifiedCompositionData);

    Optional<ServiceTemplate> substitutionServiceTemplate = unifiedCompositionService
        .createUnifiedSubstitutionServiceTemplate(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList,
            context, ORG_OPENECOMP_RESOURCE_ABSTRACT_NODES_FSB1, null);
    assertEquals(true, substitutionServiceTemplate.isPresent());
    substitutionServiceTemplate
        .ifPresent(
            subServiceTemplate -> checkSTResults(expectedOutserviceTemplates,
                subServiceTemplate, context.getGlobalSubstitutionServiceTemplate(), null));
  }


  @Test
  public void createSubstitutionStWithIndex() throws Exception {
    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL1));
    portTypeToIdList.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL2));
    portTypeToIdList.add(new ImmutablePair<>(FSB1_OAM, FSB1_OAM));

    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/creSubstitutionServiceTemplate/WithIndex");
    UnifiedCompositionData unifiedCompositionData =
        createCompositionData(FSB1_template, portTypeToIdList);
    List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>();
    unifiedCompositionDataList.add(unifiedCompositionData);

    Optional<ServiceTemplate> substitutionServiceTemplate = unifiedCompositionService
        .createUnifiedSubstitutionServiceTemplate(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList,
            context,ORG_OPENECOMP_RESOURCE_ABSTRACT_NODES_FSB1, 2);
    assertEquals(true, substitutionServiceTemplate.isPresent());
    substitutionServiceTemplate
        .ifPresent(
            subServiceTemplate -> checkSTResults(expectedOutserviceTemplates,
                subServiceTemplate, context.getGlobalSubstitutionServiceTemplate(), null));
  }


  @Test
  public void createAbstractSubstituteOneComputeMultiplePortsDifferentTypesTest() throws Exception {
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/createAbstractSubstitute/oneComputeMultiplePortsDiffType");

    UnifiedCompositionData data = createComputeUnifiedCompositionData(FSB1_template);
    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL1));
    portTypeToIdList.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL2));
    portTypeToIdList.add(new ImmutablePair<>(FSB1_OAM, FSB1_OAM));
    addPortDataToCompositionData(portTypeToIdList, data);

    List<UnifiedCompositionData> unifiedCompositionDataList = new LinkedList<>();
    unifiedCompositionDataList.add(data);
    Optional<ServiceTemplate> substitutionServiceTemplate = unifiedCompositionService
        .createUnifiedSubstitutionServiceTemplate(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList,
            context, ORG_OPENECOMP_RESOURCE_VFC_NODES_HEAT_FSB, null);
    assertEquals(true, substitutionServiceTemplate.isPresent());
    if (substitutionServiceTemplate.isPresent()) {
      String substitutionNodeTypeId =
          unifiedCompositionService.getSubstitutionNodeTypeId(inputServiceTemplates.get(mainSTName),
              unifiedCompositionDataList.get(0), null, context);
      String nodeTemplateId = unifiedCompositionService.createAbstractSubstituteNodeTemplate(
          inputServiceTemplates.get(mainSTName), substitutionServiceTemplate.get(),
          unifiedCompositionDataList, substitutionNodeTypeId,
          context, null);
      validateAbstractSubstitute();
    }
  }


  @Test
  public void createAbstractSubstituteOneComputeMultiplePortsSameTypesTest() throws Exception {
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/createAbstractSubstitute/oneComputeMultiplePortsSameType");

    UnifiedCompositionData data = createComputeUnifiedCompositionData(FSB1_template);
    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL1));
    portTypeToIdList.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL2));
    addPortDataToCompositionData(portTypeToIdList, data);

    List<UnifiedCompositionData> unifiedCompositionDataList = new LinkedList<>();
    unifiedCompositionDataList.add(data);
    Optional<ServiceTemplate> substitutionServiceTemplate = unifiedCompositionService
        .createUnifiedSubstitutionServiceTemplate(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList
            , context, ORG_OPENECOMP_RESOURCE_VFC_NODES_HEAT_FSB, null);

    assertEquals(true, substitutionServiceTemplate.isPresent());
    if (substitutionServiceTemplate.isPresent()) {
      String substitutionNodeTypeId =
          unifiedCompositionService.getSubstitutionNodeTypeId(inputServiceTemplates.get(mainSTName),
              unifiedCompositionDataList.get(0), null, context);
      String nodeTemplateId = unifiedCompositionService.createAbstractSubstituteNodeTemplate(
          inputServiceTemplates.get(mainSTName), substitutionServiceTemplate.get(),
          unifiedCompositionDataList, substitutionNodeTypeId,
          context, null);
      validateAbstractSubstitute();
    }
  }


  @Test
  public void createAbstractSubstituteTwoComputesMultiplePorts() throws Exception {
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/createAbstractSubstitute/twoComputesMultiplePorts");
    List<UnifiedCompositionData> unifiedCompositionDataList =
        createAbstractSubstituteCompositionDataComputeAndPort();
    Optional<ServiceTemplate> substitutionServiceTemplate = unifiedCompositionService
        .createUnifiedSubstitutionServiceTemplate(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList
            , context, ORG_OPENECOMP_RESOURCE_VFC_NODES_HEAT_FSB, null);
    assertEquals(true, substitutionServiceTemplate.isPresent());
    if (substitutionServiceTemplate.isPresent()) {
      String substitutionNodeTypeId =
          unifiedCompositionService.getSubstitutionNodeTypeId(inputServiceTemplates.get(mainSTName),
              unifiedCompositionDataList.get(0), null, context);
      String nodeTemplateId = unifiedCompositionService.createAbstractSubstituteNodeTemplate(
          inputServiceTemplates.get(mainSTName), substitutionServiceTemplate.get(),
          unifiedCompositionDataList, substitutionNodeTypeId,
          context, null);
      validateAbstractSubstitute();
    }
  }


  @Test
  public void updNodesConnectedOutWithConsolidationTest() throws Exception {
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/updNodesConnectedOut/consolidation");
    List<UnifiedCompositionData> unifiedCompositionDataList =
        createAbstractSubstituteCompositionDataComputeAndPort();
    Optional<ServiceTemplate> substitutionServiceTemplate = unifiedCompositionService
        .createUnifiedSubstitutionServiceTemplate(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList
            , context, ORG_OPENECOMP_RESOURCE_VFC_NODES_HEAT_FSB, null);
    assertEquals(true, substitutionServiceTemplate.isPresent());
    if (substitutionServiceTemplate.isPresent()) {
      String substitutionNodeTypeId =
          unifiedCompositionService.getSubstitutionNodeTypeId(inputServiceTemplates.get(mainSTName),
              unifiedCompositionDataList.get(0), null, context);
      String nodeTemplateId = unifiedCompositionService.createAbstractSubstituteNodeTemplate(
          inputServiceTemplates.get(mainSTName), substitutionServiceTemplate.get(),
          unifiedCompositionDataList, substitutionNodeTypeId,
          context, null);

      unifiedCompositionService
          .updateCompositionConnectivity(inputServiceTemplates.get(mainSTName),
              unifiedCompositionDataList, context);
      validateAbstractSubstitute();
    }
  }

  private void validateAbstractSubstitute() {
    YamlUtil yamlUtil = new YamlUtil();
    assertEquals(yamlUtil.objectToYaml(expectedOutserviceTemplates.get(mainSTName)), yamlUtil
        .objectToYaml(inputServiceTemplates.get(mainSTName)));
  }


  @Test
  public void updNodesConnectedOutNoConsolidationTest() throws Exception {
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/updNodesConnectedOut/noConsolidation");

    UnifiedCompositionData data = createComputeUnifiedCompositionData(FSB1_template);
    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL1));
    portTypeToIdList.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL2));
    portTypeToIdList.add(new ImmutablePair<>(FSB1_OAM, FSB1_OAM));
    addPortDataToCompositionData(portTypeToIdList, data);

    List<UnifiedCompositionData> unifiedCompositionDataList = new LinkedList<>();
    unifiedCompositionDataList.add(data);

    Optional<ServiceTemplate> substitutionServiceTemplate = unifiedCompositionService
        .createUnifiedSubstitutionServiceTemplate(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList
            , context, ORG_OPENECOMP_RESOURCE_VFC_NODES_HEAT_FSB, null);
    assertEquals(true, substitutionServiceTemplate.isPresent());
    if (substitutionServiceTemplate.isPresent()) {
      String substitutionNodeTypeId =
          unifiedCompositionService.getSubstitutionNodeTypeId(inputServiceTemplates.get(mainSTName),
              unifiedCompositionDataList.get(0), null, context);
      String nodeTemplateId = unifiedCompositionService.createAbstractSubstituteNodeTemplate(
          inputServiceTemplates.get(mainSTName), substitutionServiceTemplate.get(),
          unifiedCompositionDataList, substitutionNodeTypeId,
          context, null);

      unifiedCompositionService
          .updateCompositionConnectivity(inputServiceTemplates.get(mainSTName),
              unifiedCompositionDataList, context);
      validateAbstractSubstitute();
    }
  }


  @Test
  public void updNodesConnectedInNoConsolidationTest() throws Exception {
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/updNodesConnectedIn/noConsolidation");

    UnifiedCompositionData data = createComputeUnifiedCompositionData("QRouter");
    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cdr_network_port", "cdr_network_port"));
    portTypeToIdList
        .add(new ImmutablePair<>("oam_private_net_network_port", "oam_private_net_network_port"));
    addPortDataToCompositionData(portTypeToIdList, data);

    List<UnifiedCompositionData> unifiedCompositionDataList = new LinkedList<>();
    unifiedCompositionDataList.add(data);

    Optional<ServiceTemplate> substitutionServiceTemplate = unifiedCompositionService
        .createUnifiedSubstitutionServiceTemplate(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList
            , context, "org.openecomp.resource.abstract.nodes.QRouter", null);

    String substitutionNodeTypeId =
        unifiedCompositionService.getSubstitutionNodeTypeId(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList.get(0), null, context);
    String nodeTemplateId = unifiedCompositionService.createAbstractSubstituteNodeTemplate(
        inputServiceTemplates.get(mainSTName), substitutionServiceTemplate.get(),
        unifiedCompositionDataList, substitutionNodeTypeId,
        context, null);

    unifiedCompositionService
        .updateCompositionConnectivity(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList, context);
    validateAbstractSubstitute();
  }


  @Test
  public void updNodesConnectedInWithConsolidationTest() throws Exception {
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/updNodesConnectedIn/consolidation");

    List<UnifiedCompositionData> unifiedCompositionDataList =
        createAbstractSubstituteCompositionDataComputeAndPort();

    Optional<ServiceTemplate> substitutionServiceTemplate = unifiedCompositionService
        .createUnifiedSubstitutionServiceTemplate(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList
            , context, "org.openecomp.resource.abstract.nodes.FSB", null);

    String substitutionNodeTypeId =
        unifiedCompositionService.getSubstitutionNodeTypeId(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList.get(0), null, context);
    String nodeTemplateId = unifiedCompositionService.createAbstractSubstituteNodeTemplate(
        inputServiceTemplates.get(mainSTName), substitutionServiceTemplate.get(),
        unifiedCompositionDataList, substitutionNodeTypeId,
        context, null);

    unifiedCompositionService
        .updateCompositionConnectivity(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList, context);
    validateAbstractSubstitute();
  }


  @Test
  public void updVolumesNoConsolidationTest() throws Exception {
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/updVolumes/noConsolidation");

    UnifiedCompositionData data = createComputeUnifiedCompositionData(FSB1_template);
    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL1));
    portTypeToIdList.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL2));
    portTypeToIdList.add(new ImmutablePair<>(FSB1_OAM, FSB1_OAM));
    addPortDataToCompositionData(portTypeToIdList, data);

    List<UnifiedCompositionData> unifiedCompositionDataList = new LinkedList<>();
    unifiedCompositionDataList.add(data);

    Optional<ServiceTemplate> substitutionServiceTemplate = unifiedCompositionService
        .createUnifiedSubstitutionServiceTemplate(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList
            , context, ORG_OPENECOMP_RESOURCE_VFC_NODES_HEAT_FSB, null);

    String substitutionNodeTypeId =
        unifiedCompositionService.getSubstitutionNodeTypeId(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList.get(0), null, context);
    String nodeTemplateId = unifiedCompositionService.createAbstractSubstituteNodeTemplate(
        inputServiceTemplates.get(mainSTName), substitutionServiceTemplate.get(),
        unifiedCompositionDataList, substitutionNodeTypeId,
        context, null);

    unifiedCompositionService
        .updateCompositionConnectivity(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList, context);
    validateAbstractSubstitute();
  }


  @Test
  public void updVolumesWithConsolidationTest() throws Exception {
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/updVolumes/consolidation");
    List<UnifiedCompositionData> unifiedCompositionDataList =
        createAbstractSubstituteCompositionDataComputeAndPort();
    Optional<ServiceTemplate> substitutionServiceTemplate = unifiedCompositionService
        .createUnifiedSubstitutionServiceTemplate(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList
            , context, ORG_OPENECOMP_RESOURCE_VFC_NODES_HEAT_FSB, null);

    String substitutionNodeTypeId =
        unifiedCompositionService.getSubstitutionNodeTypeId(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList.get(0), null, context);
    String nodeTemplateId = unifiedCompositionService.createAbstractSubstituteNodeTemplate(
        inputServiceTemplates.get(mainSTName), substitutionServiceTemplate.get(),
        unifiedCompositionDataList, substitutionNodeTypeId,
        context, null);

    unifiedCompositionService
        .updateCompositionConnectivity(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList, context);
    validateAbstractSubstitute();
  }


  @Test
  public void updGroupsNoConsolidationTest() throws Exception {
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/updGroupsConnectivity/noConsolidation");
    UnifiedCompositionData data = createComputeUnifiedCompositionData("server_smp1");
    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(PORT, PORT1));
    portTypeToIdList.add(new ImmutablePair<>(PORT, PORT2));
    addPortDataToCompositionData(portTypeToIdList, data);

    //Add groups
    List<String> computeGroupIdList =
        TestUtils.getGroupsForNode(inputServiceTemplates.get(mainSTName), "server_smp1");
    data.getComputeTemplateConsolidationData().setGroupIds(computeGroupIdList);

    List<UnifiedCompositionData> unifiedCompositionDataList = new LinkedList<>();
    unifiedCompositionDataList.add(data);

    Optional<ServiceTemplate> substitutionServiceTemplate = unifiedCompositionService
        .createUnifiedSubstitutionServiceTemplate(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList
            , context, "org.openecomp.resource.abstract.nodes.smp", null);

    String substitutionNodeTypeId =
        unifiedCompositionService.getSubstitutionNodeTypeId(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList.get(0), null, context);
    String nodeTemplateId = unifiedCompositionService.createAbstractSubstituteNodeTemplate(
        inputServiceTemplates.get(mainSTName), substitutionServiceTemplate.get(),
        unifiedCompositionDataList, substitutionNodeTypeId,
        context, null);

    unifiedCompositionService
        .updateCompositionConnectivity(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList, context);
    validateAbstractSubstitute();
  }


  @Test
  public void updGroupsWithConsolidationTest() throws Exception {
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/updGroupsConnectivity/consolidation");

    List<UnifiedCompositionData> unifiedCompositionDataList =
        createAbstractSubstituteCompositionDataComputeAndPort();

    Optional<ServiceTemplate> substitutionServiceTemplate = unifiedCompositionService
        .createUnifiedSubstitutionServiceTemplate(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList
            , context, ORG_OPENECOMP_RESOURCE_VFC_NODES_HEAT_FSB, null);

    String substitutionNodeTypeId =
        unifiedCompositionService.getSubstitutionNodeTypeId(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList.get(0), null, context);
    String nodeTemplateId = unifiedCompositionService.createAbstractSubstituteNodeTemplate(
        inputServiceTemplates.get(mainSTName), substitutionServiceTemplate.get(),
        unifiedCompositionDataList, substitutionNodeTypeId,
        context, null);

    unifiedCompositionService
        .updateCompositionConnectivity(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList, context);
    validateAbstractSubstitute();
  }

  @Test
  public void updOutParamGetAttrInNoConsolidationTest() throws Exception {
    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL1));
    portTypeToIdList.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL2));
    portTypeToIdList.add(new ImmutablePair<>(FSB1_OAM, FSB1_OAM));

    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/updOutputGetAttrIn/noConsolidation");
    UnifiedCompositionData unifiedCompositionData =
        createCompositionData(FSB1_template, portTypeToIdList);
    addOutputGetAttrInForComputeNoConsolidation(unifiedCompositionData);
    addOutputGetAttrInForPortNoConsolidation(unifiedCompositionData);

    List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>();
    unifiedCompositionDataList.add(unifiedCompositionData);
    Mockito.doNothing().when(unifiedCompositionService).updNodesConnectedOutConnectivity
        (inputServiceTemplates.get(mainSTName), unifiedCompositionDataList, context);
    Mockito.doReturn(FSB1).when(context).getUnifiedAbstractNodeTemplateId(anyObject(),
        anyString());
    unifiedCompositionService
        .updateCompositionConnectivity(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList, context);

    checkSTResults(expectedOutserviceTemplates, null, null, inputServiceTemplates.get(mainSTName));
  }

  @Test
  public void updOutParamGetAttrInWithConsolidationTest() throws Exception {
    List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>();
    List<Pair<String, String>> portTypeToIdList1 = new ArrayList<>();
    portTypeToIdList1.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL1));
    portTypeToIdList1.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL2));

    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/updOutputGetAttrIn/consolidation");
    UnifiedCompositionData unifiedCompositionData1 =
        createCompositionData(FSB1_template, portTypeToIdList1);
    addOutputGetAttrInForCompute1WithConsolidation(unifiedCompositionData1);
    addOutputGetAttrInForPortWithConsolidation1(unifiedCompositionData1);
    unifiedCompositionDataList.add(unifiedCompositionData1);

    List<Pair<String, String>> portTypeToIdList2 = new ArrayList<>();
    portTypeToIdList2.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL2));
    portTypeToIdList2.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL1));

    UnifiedCompositionData unifiedCompositionData2 =
        createCompositionData(FSB2_template, portTypeToIdList2);
    unifiedCompositionDataList.add(unifiedCompositionData2);
    addOutputGetAttrInForCompute2WithConsolidation(unifiedCompositionData2);
    addOutputGetAttrInForPortWithConsolidation2(unifiedCompositionData2);

    Mockito.doNothing().when(unifiedCompositionService).updNodesConnectedOutConnectivity
        (inputServiceTemplates.get(mainSTName), unifiedCompositionDataList, context);
    Mockito.doReturn(FSB1).when(context).getUnifiedAbstractNodeTemplateId(anyObject(),
        anyString());

    unifiedCompositionService
        .updateCompositionConnectivity(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList, context);

    checkSTResults(expectedOutserviceTemplates, null, null, inputServiceTemplates.get(mainSTName));
  }

  @Test
  public void updNodeGetAttrInNoConsolidationTest() throws Exception {
    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL1));
    portTypeToIdList.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL2));
    portTypeToIdList.add(new ImmutablePair<>(FSB1_OAM, FSB1_OAM));

    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/updNodesGetAttrIn/noConsolidation");
    UnifiedCompositionData unifiedCompositionData =
        createCompositionData(FSB1_template, portTypeToIdList);
    addGetAttrForCompute(unifiedCompositionData);
    addGetAttrForPort(unifiedCompositionData);
    addGetAttrForPortInnerUC(unifiedCompositionData);

    List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>();
    unifiedCompositionDataList.add(unifiedCompositionData);
    Mockito.doNothing().when(unifiedCompositionService).updNodesConnectedOutConnectivity
        (inputServiceTemplates.get(mainSTName), unifiedCompositionDataList, context);
    Mockito.doReturn(FSB1).when(context).getUnifiedAbstractNodeTemplateId(anyObject(),
        anyString());
    unifiedCompositionService
        .updateCompositionConnectivity(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList, context);

    checkSTResults(expectedOutserviceTemplates, null, null, inputServiceTemplates.get(mainSTName));
  }

  @Test
  public void updNodeGetAttrInWithConsolidationTest() throws Exception {
    List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>();
    List<Pair<String, String>> portTypeToIdList1 = new ArrayList<>();
    portTypeToIdList1.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL1));
    portTypeToIdList1.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL2));

    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/updNodesGetAttrIn/consolidation");
    UnifiedCompositionData unifiedCompositionData1 =
        createCompositionData(FSB1_template, portTypeToIdList1);
    addGetAttrForCompute(unifiedCompositionData1);
    addGetAttrForPort(unifiedCompositionData1);
    unifiedCompositionDataList.add(unifiedCompositionData1);

    List<Pair<String, String>> portTypeToIdList2 = new ArrayList<>();
    portTypeToIdList2.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL2));
    portTypeToIdList2.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL1));

    UnifiedCompositionData unifiedCompositionData2 =
        createCompositionData(FSB2_template, portTypeToIdList2);
    addGetAttrForCompute2(unifiedCompositionData2);
    addGetAttrForPort2(unifiedCompositionData2);
    unifiedCompositionDataList.add(unifiedCompositionData2);


    Mockito.doNothing().when(unifiedCompositionService).updNodesConnectedOutConnectivity
        (inputServiceTemplates.get(mainSTName), unifiedCompositionDataList, context);
    Mockito.doReturn(FSB1).when(context).getUnifiedAbstractNodeTemplateId(anyObject(),
        anyString());

    unifiedCompositionService
        .updateCompositionConnectivity(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList, context);

    checkSTResults(expectedOutserviceTemplates, null, null, inputServiceTemplates.get(mainSTName));
  }


  @Test
  public void updNodesGetAttrFromInnerNodesTest() throws Exception {
    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL1));
    portTypeToIdList.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL2));
    portTypeToIdList.add(new ImmutablePair<>(FSB1_OAM, FSB1_OAM));

    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/creSubstitutionServiceTemplate/updNodesGetAttrInFromInnerNodes/noConsolidation");
    UnifiedCompositionData unifiedCompositionData =
        createCompositionData(FSB1_template, portTypeToIdList);
    addGetAttrForCompute(unifiedCompositionData);
    addGetAttrForPort(unifiedCompositionData);
    addGetAttrForPortInnerUC(unifiedCompositionData);
    List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>();
    unifiedCompositionDataList.add(unifiedCompositionData);

    Mockito.doReturn(FSB1).when(context).getUnifiedAbstractNodeTemplateId(anyObject(),
        anyString());

    Optional<ServiceTemplate> substitutionServiceTemplate = unifiedCompositionService
        .createUnifiedSubstitutionServiceTemplate(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList,
            context, ORG_OPENECOMP_RESOURCE_ABSTRACT_NODES_FSB1, null);
    assertEquals(true, substitutionServiceTemplate.isPresent());
    substitutionServiceTemplate
        .ifPresent(
            subServiceTemplate -> checkSTResults(expectedOutserviceTemplates,
                subServiceTemplate, context.getGlobalSubstitutionServiceTemplate(), null));
  }


  @Test
  public void updNodesGetAttrFromConsolidationNodesTest() throws Exception {
    List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>();
    List<Pair<String, String>> portTypeToIdList1 = new ArrayList<>();
    portTypeToIdList1.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL1));
    portTypeToIdList1.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL2));

    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/creSubstitutionServiceTemplate/updNodesGetAttrInFromInnerNodes/consolidation");
    UnifiedCompositionData unifiedCompositionData1 =
        createCompositionData(FSB1_template, portTypeToIdList1);
    addGetAttrForCompute(unifiedCompositionData1);
    addGetAttrForPort(unifiedCompositionData1);
    unifiedCompositionDataList.add(unifiedCompositionData1);

    List<Pair<String, String>> portTypeToIdList2 = new ArrayList<>();
    portTypeToIdList2.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL2));
    portTypeToIdList2.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL1));

    UnifiedCompositionData unifiedCompositionData2 =
        createCompositionData(FSB2_template, portTypeToIdList2);
    addGetAttrForCompute2(unifiedCompositionData2);
    addGetAttrForPort2(unifiedCompositionData2);
    unifiedCompositionDataList.add(unifiedCompositionData2);

    Optional<ServiceTemplate> substitutionServiceTemplate = unifiedCompositionService
        .createUnifiedSubstitutionServiceTemplate(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList,
            context, ORG_OPENECOMP_RESOURCE_ABSTRACT_NODES_FSB1, null);
    assertEquals(true, substitutionServiceTemplate.isPresent());
    substitutionServiceTemplate
        .ifPresent(
            subServiceTemplate -> checkSTResults(expectedOutserviceTemplates,
                subServiceTemplate, context.getGlobalSubstitutionServiceTemplate(), null));
  }

  @Test
  public void cleanMainServiceTemplateTestNoConsolidation() throws IOException {
    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL1));
    portTypeToIdList.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL2));
    portTypeToIdList.add(new ImmutablePair<>("FSB_OAM", FSB1_OAM));

    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/cleanMainSt/noConsolidation");
    UnifiedCompositionData unifiedCompositionData =
        createCompositionData(FSB1_template, portTypeToIdList);
    addGetAttrForCompute(unifiedCompositionData);
    addGetAttrForPort(unifiedCompositionData);

    List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>();
    unifiedCompositionDataList.add(unifiedCompositionData);

    NodeTemplate abstractNodeTemplate = getMockNode(
        "/mock/services/heattotosca/unifiedComposition/cleanMainSt/mockAbstractNodeTemplate.yaml");
    inputServiceTemplates.get(mainSTName).getTopology_template().getNode_templates()
        .put(FSB1, abstractNodeTemplate);

    Mockito.doReturn(FSB1).when(context).getUnifiedAbstractNodeTemplateId(anyObject(),
        anyString());

    unifiedCompositionService.
        cleanUnifiedCompositionEntities(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList, context);

    checkSTResults(expectedOutserviceTemplates, null, null, inputServiceTemplates.get(mainSTName));
  }

  @Test
  public void cleanMainServiceTemplateTestWithConsolidation() throws IOException {
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/cleanMainSt/consolidation");

    List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>();
    List<Pair<String, String>> portTypeToIdList1 = new ArrayList<>();
    portTypeToIdList1.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL1));
    portTypeToIdList1.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL2));

    UnifiedCompositionData unifiedCompositionData1 =
        createCompositionData(FSB1_template, portTypeToIdList1);
    addOutputGetAttrInForCompute1WithConsolidation(unifiedCompositionData1);
    addOutputGetAttrInForPortWithConsolidation1(unifiedCompositionData1);
    unifiedCompositionDataList.add(unifiedCompositionData1);

    List<Pair<String, String>> portTypeToIdList2 = new ArrayList<>();
    portTypeToIdList2.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL2));
    portTypeToIdList2.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL1));

    UnifiedCompositionData unifiedCompositionData2 =
        createCompositionData(FSB2_template, portTypeToIdList2);
    addOutputGetAttrInForCompute2WithConsolidation(unifiedCompositionData2);
    addOutputGetAttrInForPortWithConsolidation2(unifiedCompositionData2);
    unifiedCompositionDataList.add(unifiedCompositionData2);

    NodeTemplate abstractNodeTemplate = getMockNode(
        "/mock/services/heattotosca/unifiedComposition/cleanMainSt/mockAbstractNodeTemplate.yaml");
    inputServiceTemplates.get(mainSTName).getTopology_template().getNode_templates()
        .put(FSB1, abstractNodeTemplate);

    Mockito.doReturn(FSB1).when(context).getUnifiedAbstractNodeTemplateId(anyObject(),
        anyString());

    unifiedCompositionService.
        cleanUnifiedCompositionEntities(inputServiceTemplates.get(mainSTName),
            unifiedCompositionDataList, context);

    checkSTResults(expectedOutserviceTemplates, null, null, inputServiceTemplates.get(mainSTName));
  }

  @Test
  public void updateNewAbstractNodeTemplateNoConsolidation() throws IOException {
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/fixNewAbstractNodeTemplate/noConsolidation");

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL1));
    portTypeToIdList.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL2));

    NodeTemplate cleanedComputeNodeTemplate =
        getMockNode(
            "/mock/services/heattotosca/unifiedComposition/fixNewAbstractNodeTemplate/mockComputeNodeTemplate.yaml");


    context.setConsolidationData(
        createConsolidationData(Arrays.asList(FSB1_template), portTypeToIdList));
    context.addCleanedNodeTemplate(mainSTName, FSB1_template,
        UnifiedCompositionEntity.Compute, cleanedComputeNodeTemplate);
    context.addCleanedNodeTemplate(mainSTName, FSB1_INTERNAL1,
        UnifiedCompositionEntity.Port, cleanedComputeNodeTemplate);
    context.addCleanedNodeTemplate(mainSTName, FSB1_INTERNAL2,
        UnifiedCompositionEntity.Port, cleanedComputeNodeTemplate);

    setUnifiedCompositionData(Arrays.asList(FSB1_template, FSB1_INTERNAL1, FSB1_INTERNAL2));

    unifiedCompositionService
        .updateUnifiedAbstractNodesConnectivity(inputServiceTemplates.get(mainSTName), context);

    checkSTResults(expectedOutserviceTemplates, null, null, inputServiceTemplates.get(mainSTName));
  }

  @Test
  public void updateNewAbstractNodeTemplateWithConsolidation() throws IOException {
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/fixNewAbstractNodeTemplate/consolidation");

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL1));
    portTypeToIdList.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL1));

    NodeTemplate cleanedComputeNodeTemplate =
        getMockNode(
            "/mock/services/heattotosca/unifiedComposition/fixNewAbstractNodeTemplate/mockComputeNodeTemplate.yaml");


    context.setConsolidationData(
        createConsolidationData(Arrays.asList("FSB1_template_1", "FSB1_template_2"),
            portTypeToIdList));
    context.addCleanedNodeTemplate(mainSTName, "FSB1_template_1",
        UnifiedCompositionEntity.Compute, cleanedComputeNodeTemplate);
    context.addCleanedNodeTemplate(mainSTName, "FSB1_template_2",
        UnifiedCompositionEntity.Compute, cleanedComputeNodeTemplate);
    context.addCleanedNodeTemplate(mainSTName, FSB1_INTERNAL1,
        UnifiedCompositionEntity.Port, cleanedComputeNodeTemplate);
    context.addCleanedNodeTemplate(mainSTName, FSB2_INTERNAL1,
        UnifiedCompositionEntity.Port, cleanedComputeNodeTemplate);

    setUnifiedCompositionData(
        Arrays.asList("FSB1_template_1", "FSB1_template_2", FSB1_INTERNAL1, FSB2_INTERNAL1));

    unifiedCompositionService
        .updateUnifiedAbstractNodesConnectivity(inputServiceTemplates.get(mainSTName), context);

    checkSTResults(expectedOutserviceTemplates, null, null, inputServiceTemplates.get(mainSTName));
  }

  @Ignore
  public void testThreeNovaOfSameTypePreConditionFalse() throws IOException {
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/pattern1b/noConsolidation");

    List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>();
    List<Pair<String, String>> portTypeToIdList1 = new ArrayList<>();
    portTypeToIdList1.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL1));
    portTypeToIdList1.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL2));

    UnifiedCompositionData unifiedCompositionData1 =
        createCompositionData(FSB1_template, portTypeToIdList1);
    addOutputGetAttrInForCompute1WithConsolidation(unifiedCompositionData1);
    addOutputGetAttrInForPortWithConsolidation1(unifiedCompositionData1);
    unifiedCompositionDataList.add(unifiedCompositionData1);

    UnifiedCompositionData unifiedCompositionData2 =
        createCompositionData(FSB2_template, portTypeToIdList1);
    addOutputGetAttrInForCompute1WithConsolidation(unifiedCompositionData2);
    addOutputGetAttrInForPortWithConsolidation1(unifiedCompositionData2);
    unifiedCompositionDataList.add(unifiedCompositionData2);

    portTypeToIdList1.remove(1);
    UnifiedCompositionData unifiedCompositionData3 =
        createCompositionData(FSB3_template, portTypeToIdList1);
    addOutputGetAttrInForCompute1WithConsolidation(unifiedCompositionData3);
    addOutputGetAttrInForPortWithConsolidation1(unifiedCompositionData3);
    unifiedCompositionDataList.add(unifiedCompositionData3);

    UnifiedCompositionSingleSubstitution unifiedCompositionSingleSubstitution =
        new UnifiedCompositionSingleSubstitution();
    unifiedCompositionSingleSubstitution
        .createUnifiedComposition(inputServiceTemplates.get(mainSTName), null,
            unifiedCompositionDataList, context);

    checkSTResults(expectedOutserviceTemplates, null, null, inputServiceTemplates.get(mainSTName));
  }

  @Test
  public void testUnifiedNestedCompositionOneComputeInNested() throws IOException {
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/pattern4/oneNestedNode");

    ConsolidationData consolidationData = new ConsolidationData();
    String nestedFileName = "nested-pcm_v0.1ServiceTemplate.yaml";
    TestUtils.updateNestedConsolidationData(mainSTName, Arrays.asList("server_pcm_001"),
        consolidationData);

    TestUtils.initComputeNodeTypeInConsolidationData(nestedFileName,
        "org.openecomp.resource.vfc.nodes.heat.pcm_server", consolidationData);
    TestUtils.initComputeNodeTemplateIdInConsolidationData(nestedFileName,
        "org.openecomp.resource.vfc.nodes.heat.pcm_server", "server_pcm", consolidationData);

    context.setConsolidationData(consolidationData);
    context.getTranslatedServiceTemplates()
        .put(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME,
            inputServiceTemplates.get(globalSubstitutionST));
    context.getTranslatedServiceTemplates()
        .put(nestedFileName, inputServiceTemplates.get(nestedFileName));
    context.getTranslatedServiceTemplates()
        .put(mainSTName, inputServiceTemplates.get(mainSTName));

    UnifiedCompositionData unifiedComposition = createUnifiedCompositionOnlyNested("server_pcm_001");
    unifiedCompositionService.handleUnifiedNestedDefinition(inputServiceTemplates.get(mainSTName),
        inputServiceTemplates.get(nestedFileName), unifiedComposition, context);

    checkSTResults(expectedOutserviceTemplates, nestedFileName,
        context.getTranslatedServiceTemplates().get(nestedFileName),
        context.getTranslatedServiceTemplates()
            .get(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME), null);
  }

  @Test
  public void testTwoNestedWithOneCompute() throws IOException {
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/pattern4/twoNestedWithOneCompute");

    ConsolidationData consolidationData = new ConsolidationData();
    String nestedFileName1 = "nested-pcm_v0.1ServiceTemplate.yaml";
    String nestedFileName2 = "nested-oam_v0.1ServiceTemplate.yaml";

    TestUtils.updateNestedConsolidationData(mainSTName,
        Arrays.asList("server_pcm_001", "server_oam_001"), consolidationData);

    TestUtils.initComputeNodeTypeInConsolidationData(nestedFileName1,
        "org.openecomp.resource.vfc.nodes.heat.pcm_server", consolidationData);
    TestUtils.initComputeNodeTemplateIdInConsolidationData(nestedFileName1,
        "org.openecomp.resource.vfc.nodes.heat.pcm_server", "server_pcm", consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(nestedFileName2,
        "org.openecomp.resource.vfc.nodes.heat.oam_server", consolidationData);
    TestUtils.initComputeNodeTemplateIdInConsolidationData(nestedFileName2,
        "org.openecomp.resource.vfc.nodes.heat.oam_server", "server_oam", consolidationData);

    context.setConsolidationData(consolidationData);
    context.getTranslatedServiceTemplates()
        .put(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME,
            inputServiceTemplates.get(globalSubstitutionST));
    context.getTranslatedServiceTemplates()
        .put(nestedFileName1, inputServiceTemplates.get(nestedFileName1));
    context.getTranslatedServiceTemplates()
        .put(nestedFileName2, inputServiceTemplates.get(nestedFileName2));
    context.getTranslatedServiceTemplates()
        .put(mainSTName, inputServiceTemplates.get(mainSTName));

    UnifiedCompositionData unifiedComposition =
        createUnifiedCompositionOnlyNested("server_pcm_001");
    unifiedCompositionService.handleUnifiedNestedDefinition(inputServiceTemplates.get(mainSTName),
        inputServiceTemplates.get(nestedFileName1), unifiedComposition, context);
    unifiedComposition = createUnifiedCompositionOnlyNested("server_oam_001");
    unifiedCompositionService.handleUnifiedNestedDefinition(inputServiceTemplates.get(mainSTName),
        inputServiceTemplates.get(nestedFileName2), unifiedComposition, context);

    checkSTResults(expectedOutserviceTemplates, nestedFileName1,
        context.getTranslatedServiceTemplates().get(nestedFileName1),
        context.getTranslatedServiceTemplates()
            .get(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME), null);
    checkSTResults(expectedOutserviceTemplates, nestedFileName2,
        context.getTranslatedServiceTemplates().get(nestedFileName2),
        null, null);
  }

  @Test
  public void testNestedCompositionNodesConnectedIn() throws IOException {
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/pattern4/nestedNodesConnectedIn");
    ConsolidationData consolidationData = new ConsolidationData();
    String nestedFileName = "nested-pcm_v0.1ServiceTemplate.yaml";
    TestUtils.updateNestedConsolidationData(mainSTName, Arrays.asList("server_pcm_001"),
        consolidationData);
    context.getTranslatedServiceTemplates()
        .put(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME,
            inputServiceTemplates.get(globalSubstitutionST));
    context.getTranslatedServiceTemplates()
        .put(nestedFileName, inputServiceTemplates.get(nestedFileName));
    context.getTranslatedServiceTemplates()
        .put(mainSTName, inputServiceTemplates.get(mainSTName));
    context.addUnifiedNestedNodeTemplateId(mainSTName, "server_pcm_001", "abstract_pcm_server_0");

    Map<String, List<RequirementAssignmentData>> nodeConnectedInList =
        TestUtils.getNodeConnectedInList("server_pcm_001", inputServiceTemplates.get(mainSTName),
            DEPENDENCY);
    UnifiedCompositionData unifiedComposition =
        createUnifiedCompositionOnlyNested("server_pcm_001");
    unifiedComposition.getNestedTemplateConsolidationData()
        .setNodesConnectedIn(nodeConnectedInList);

    unifiedCompositionService.updNestedCompositionNodesConnectedInConnectivity
        (inputServiceTemplates.get(mainSTName), unifiedComposition, context);
    checkSTResults(expectedOutserviceTemplates, nestedFileName,
        context.getTranslatedServiceTemplates().get(nestedFileName),
        context.getTranslatedServiceTemplates()
            .get(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME), context
            .getTranslatedServiceTemplates().get(mainSTName));
  }


  @Test
  public void testNestedCompositionNodesGetAttrIn() throws IOException {
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/pattern4/nestedNodesGetAttrIn");
    ConsolidationData consolidationData = new ConsolidationData();
    String nestedFileName = "nested-pcm_v0.1ServiceTemplate.yaml";
    TestUtils.updateNestedConsolidationData(mainSTName, Arrays.asList("server_pcm_001"),
        consolidationData);
    context.getTranslatedServiceTemplates()
        .put(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME,
            inputServiceTemplates.get(globalSubstitutionST));
    context.getTranslatedServiceTemplates()
        .put(nestedFileName, inputServiceTemplates.get(nestedFileName));
    context.getTranslatedServiceTemplates()
        .put(mainSTName, inputServiceTemplates.get(mainSTName));
    context.addUnifiedNestedNodeTemplateId(mainSTName, "server_pcm_001", "abstract_pcm_server_0");

    Map<String, List<RequirementAssignmentData>> nodeConnectedInList =
        TestUtils.getNodeConnectedInList("server_pcm_001", inputServiceTemplates.get(mainSTName),
            DEPENDENCY);
    UnifiedCompositionData unifiedComposition =
        createUnifiedCompositionOnlyNested("server_pcm_001");
    addGetAttInUnifiedCompositionData(unifiedComposition
        .getNestedTemplateConsolidationData(), TENANT_ID, "oam_net_gw", "packet_mirror_network");
    addGetAttInUnifiedCompositionData(unifiedComposition
        .getNestedTemplateConsolidationData(), USER_DATA_FORMAT, "oam_net_gw",
        "server_compute_get_attr_test");
    addGetAttInUnifiedCompositionData(unifiedComposition
        .getNestedTemplateConsolidationData(), "metadata", "server_pcm_id",
        "server_compute_get_attr_test");
    unifiedCompositionService.updNestedCompositionNodesGetAttrInConnectivity
        (inputServiceTemplates.get(mainSTName), unifiedComposition, context);
    checkSTResults(expectedOutserviceTemplates, nestedFileName,
        context.getTranslatedServiceTemplates().get(nestedFileName),
        context.getTranslatedServiceTemplates()
            .get(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME), context
            .getTranslatedServiceTemplates().get(mainSTName));
  }

  @Test
  public void testNestedCompositionOutputParamGetAttrIn() throws IOException {
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/pattern4/nestedOutputParamGetAttrIn");
    ConsolidationData consolidationData = new ConsolidationData();
    String nestedFileName = "nested-pcm_v0.1ServiceTemplate.yaml";
    TestUtils.updateNestedConsolidationData(mainSTName, Arrays.asList("server_pcm_001"),
        consolidationData);
    context.getTranslatedServiceTemplates()
        .put(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME,
            inputServiceTemplates.get(globalSubstitutionST));
    context.getTranslatedServiceTemplates()
        .put(nestedFileName, inputServiceTemplates.get(nestedFileName));
    context.getTranslatedServiceTemplates()
        .put(mainSTName, inputServiceTemplates.get(mainSTName));
    context.addUnifiedNestedNodeTemplateId(mainSTName, "server_pcm_001", "abstract_pcm_server_0");

    Map<String, List<RequirementAssignmentData>> nodeConnectedInList =
        TestUtils.getNodeConnectedInList("server_pcm_001", inputServiceTemplates.get(mainSTName),
            DEPENDENCY);
    UnifiedCompositionData unifiedComposition =
        createUnifiedCompositionOnlyNested("server_pcm_001");
    addOutputGetAttInUnifiedCompositionData(unifiedComposition
        .getNestedTemplateConsolidationData(), "output_attr_1", ACCESS_IPv4);
    addOutputGetAttInUnifiedCompositionData(unifiedComposition
            .getNestedTemplateConsolidationData(), "output_attr_2", ACCESS_IPv6);
    unifiedCompositionService.updNestedCompositionOutputParamGetAttrInConnectivity
        (inputServiceTemplates.get(mainSTName), unifiedComposition, context);
    checkSTResults(expectedOutserviceTemplates, nestedFileName,
        context.getTranslatedServiceTemplates().get(nestedFileName),
        context.getTranslatedServiceTemplates()
            .get(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME), context
            .getTranslatedServiceTemplates().get(mainSTName));
  }

  @Test
  public void testInputOutputParameterType() throws IOException{
    loadInputAndOutputData("/mock/services/heattotosca/unifiedComposition/inputoutputparamtype");
    ConsolidationData consolidationData = new ConsolidationData();
    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("FSB1_Port", "FSB1_Port1"));
    portTypeToIdList.add(new ImmutablePair<>("VMI", "VMI1"));

    UnifiedCompositionData unifiedCompositionData = createCompositionData(FSB1, portTypeToIdList);

    Map<String, NodeTemplate> nodeTemplates =
        inputServiceTemplates.get(mainSTName).getTopology_template().getNode_templates();
    for (Map.Entry<String, NodeTemplate> nodeTemplateEntry : nodeTemplates.entrySet() ) {
      String nodeTemplateId = nodeTemplateEntry.getKey();
      if (nodeTemplateId.equals("cmaui_volume_test_compute_properties")) {
        Map<String, List<GetAttrFuncData>> nodesGetAttrIn =
            TestUtils.getNodesGetAttrIn(nodeTemplateEntry.getValue(), nodeTemplateId);
        unifiedCompositionData.getComputeTemplateConsolidationData()
            .setNodesGetAttrIn(nodesGetAttrIn);
      }

      if (nodeTemplateId.equals("cmaui_volume_test_neutron_port_properties")) {
        Map<String, List<GetAttrFuncData>> nodesGetAttrIn =
            TestUtils.getNodesGetAttrIn(nodeTemplateEntry.getValue(), nodeTemplateId);
        unifiedCompositionData.getPortTemplateConsolidationDataList().get(0)
            .setNodesGetAttrIn(nodesGetAttrIn);
      }

      if (nodeTemplateId.equals("cmaui_volume_test_contrailv2_VMI_properties")) {
        Map<String, List<GetAttrFuncData>> nodesGetAttrIn =
            TestUtils.getNodesGetAttrIn(nodeTemplateEntry.getValue(), nodeTemplateId);
        unifiedCompositionData.getPortTemplateConsolidationDataList().get(1)
            .setNodesGetAttrIn(nodesGetAttrIn);
      }
    }

    List<UnifiedCompositionData> unifiedCompositionDataList = new LinkedList<>();
    unifiedCompositionDataList.add(unifiedCompositionData);

    UnifiedCompositionSingleSubstitution unifiedCompositionSingleSubstitution =
        new UnifiedCompositionSingleSubstitution();
    unifiedCompositionSingleSubstitution
        .createUnifiedComposition(inputServiceTemplates.get(mainSTName), null,
            unifiedCompositionDataList, context);
    checkSTResults(expectedOutserviceTemplates, null, null, inputServiceTemplates.get(mainSTName));
    System.out.println();

  }


  private UnifiedCompositionData createUnifiedCompositionOnlyNested(
      String nestedNodeTemplateId) {
    NestedTemplateConsolidationData nestedTemplateConsolidationData =
        new NestedTemplateConsolidationData();
    nestedTemplateConsolidationData.setNodeTemplateId(nestedNodeTemplateId);
    UnifiedCompositionData unifiedCompositionData = new UnifiedCompositionData();
    unifiedCompositionData.setNestedTemplateConsolidationData(nestedTemplateConsolidationData);
     return unifiedCompositionData;
  }

  private void setUnifiedCompositionData(List<String> nodeTemplateIds) {
    UnifiedSubstitutionData unifiedSubstitutionData =
        context.getUnifiedSubstitutionData().get(mainSTName) == null ? new UnifiedSubstitutionData()
            : context.getUnifiedSubstitutionData().get(mainSTName);
    Map<String, String> substitutionAbstractNodeIds = new HashMap<>();
    for (String id : nodeTemplateIds) {
      substitutionAbstractNodeIds.put(id, "FSB2");
    }

    substitutionAbstractNodeIds.put("", FSB1);

    unifiedSubstitutionData.setNodesRelatedAbstractNode(substitutionAbstractNodeIds);
  }

  private void checkSTResults(
      Map<String, ServiceTemplate> expectedOutserviceTemplates,
      ServiceTemplate substitutionServiceTemplate,
      ServiceTemplate gloablSubstitutionServiceTemplate, ServiceTemplate mainServiceTemplate) {
    YamlUtil yamlUtil = new YamlUtil();
    if (Objects.nonNull(substitutionServiceTemplate)) {
      assertEquals("difference substitution service template: ",
          yamlUtil.objectToYaml(expectedOutserviceTemplates.get(substitutionST)),
          yamlUtil.objectToYaml(substitutionServiceTemplate));
    }
    if (Objects.nonNull(gloablSubstitutionServiceTemplate)) {
      assertEquals("difference global substitution service template: ",
          yamlUtil.objectToYaml(expectedOutserviceTemplates.get(globalSubstitutionST)),
          yamlUtil.objectToYaml(gloablSubstitutionServiceTemplate));
    }
    if (Objects.nonNull(mainServiceTemplate)) {
      assertEquals("difference main service template: ",
          yamlUtil.objectToYaml(expectedOutserviceTemplates.get(mainSTName)),
          yamlUtil.objectToYaml(mainServiceTemplate));
    }
  }

  private void checkSTResults(
      Map<String, ServiceTemplate> expectedOutserviceTemplates,
      String nestedSTFileName, ServiceTemplate nestedServiceTemplate,
      ServiceTemplate gloablSubstitutionServiceTemplate, ServiceTemplate mainServiceTemplate) {
    YamlUtil yamlUtil = new YamlUtil();

    if (Objects.nonNull(nestedServiceTemplate)) {
      assertEquals("difference nested service template: ",
          yamlUtil.objectToYaml(expectedOutserviceTemplates.get(nestedSTFileName)),
          yamlUtil.objectToYaml(nestedServiceTemplate));
    }
    checkSTResults(expectedOutserviceTemplates, null, gloablSubstitutionServiceTemplate,
        mainServiceTemplate);
  }


  private void loadInputAndOutputData(String path) throws IOException {
    inputServiceTemplates = new HashMap<>();
    TestUtils.loadServiceTemplates(path + IN_PREFIX, new ToscaExtensionYamlUtil(),
        inputServiceTemplates);
    expectedOutserviceTemplates = new HashMap<>();
    TestUtils.loadServiceTemplates(path + OUT_PREFIX, new ToscaExtensionYamlUtil(),
        expectedOutserviceTemplates);
  }


  private void addGetAttInUnifiedCompositionData(EntityConsolidationData entityConsolidationData,
                                                 String propertyName, String attributeName,
                                                 String nodeTemplateId) {
    GetAttrFuncData getAttrFuncData = new GetAttrFuncData();
    getAttrFuncData.setAttributeName(attributeName);
    getAttrFuncData.setFieldName(propertyName);
    entityConsolidationData.addNodesGetAttrIn(nodeTemplateId, getAttrFuncData);
  }

  private void addOutputGetAttInUnifiedCompositionData(
      EntityConsolidationData entityConsolidationData,
      String outParamName, String attributeName) {
    GetAttrFuncData getAttrFuncData = new GetAttrFuncData();
    getAttrFuncData.setAttributeName(attributeName);
    getAttrFuncData.setFieldName(outParamName);
    entityConsolidationData.addOutputParamGetAttrIn(getAttrFuncData);
  }

  private ConsolidationData createConsolidationData(List<String> computeNodeIds,
                                                    List<Pair<String, String>> portTypeToIdList) {

    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.FSB2";

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainSTName, computeNodeTypeName, consolidationData);
    TestUtils.initPortConsolidationData(mainSTName, consolidationData);

    for (String computeId : computeNodeIds) {
      ComputeTemplateConsolidationData computeTemplateConsolidationData =
          new ComputeTemplateConsolidationData();
      TestUtils.updatePortsInComputeTemplateConsolidationData(portTypeToIdList,
          computeTemplateConsolidationData);
      consolidationData.getComputeConsolidationData().getFileComputeConsolidationData(mainSTName)
          .getTypeComputeConsolidationData(computeNodeTypeName)
          .setComputeTemplateConsolidationData(computeId,
              computeTemplateConsolidationData);
    }

    for (Pair<String, String> portTypeToId : portTypeToIdList) {
      consolidationData.getPortConsolidationData().getFilePortConsolidationData(mainSTName)
          .setPortTemplateConsolidationData(portTypeToId.getRight(),
              new PortTemplateConsolidationData());
    }

    return consolidationData;
  }

  private UnifiedCompositionData createCompositionData(String computeNodeTemplateId,
                                                       List<Pair<String, String>> portTypeToIdList) {

    UnifiedCompositionData unifiedCompositionData = new UnifiedCompositionData();
    NodeTemplate computeNodeTemplate =
        DataModelUtil.getNodeTemplate(inputServiceTemplates.get(mainSTName), computeNodeTemplateId);
    Optional<List<RequirementAssignmentData>> requirementAssignmentDataList =
        TestUtils.getRequirementAssignmentDataList(computeNodeTemplate, "local_storage");
    List<RequirementAssignmentData> requirementAssignmentList =
        (requirementAssignmentDataList.isPresent()) ? requirementAssignmentDataList.get() : null;
    Map<String, List<RequirementAssignmentData>> volume = null;
    if (requirementAssignmentList != null) {
      volume = getVolume(requirementAssignmentList);
    }
    unifiedCompositionData.setComputeTemplateConsolidationData(
        TestUtils.createComputeTemplateConsolidationData(computeNodeTemplateId, portTypeToIdList,
            volume));
    if (portTypeToIdList != null) {
      for (Pair<String, String> port : portTypeToIdList) {
        NodeTemplate portNodeTemplate =
            DataModelUtil.getNodeTemplate(inputServiceTemplates.get(mainSTName), port.getRight());

        Map<String, List<RequirementAssignmentData>> nodeConnectedOut =
            TestUtils.getNodeConnectedOutList(portNodeTemplate, "link");
        PortTemplateConsolidationData portTemplateConsolidationData =
            TestUtils.createPortTemplateConsolidationData(port.getRight());
        portTemplateConsolidationData.setNodesConnectedOut(nodeConnectedOut);
        unifiedCompositionData.addPortTemplateConsolidationData(portTemplateConsolidationData);
      }
    }
    return unifiedCompositionData;
  }

  private List<UnifiedCompositionData> createAbstractSubstituteCompositionDataComputeAndPort() {
    List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>();
    UnifiedCompositionData data1 = createComputeUnifiedCompositionData(FSB1_template);
    UnifiedCompositionData data2 = createComputeUnifiedCompositionData(FSB2_template);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    ImmutablePair<String, String> portTypePair1 = new ImmutablePair<>(FSB1_INTERNAL,
        FSB1_INTERNAL1);
    ImmutablePair<String, String> portTypePair2 = new ImmutablePair<>(FSB2_INTERNAL,
        FSB2_INTERNAL1);
    portTypeToIdList.add(portTypePair1);
    portTypeToIdList.add(portTypePair2);
    addPortDataToCompositionData(portTypeToIdList, data1);
    portTypeToIdList.remove(portTypePair1);
    portTypeToIdList.remove(portTypePair2);
    portTypeToIdList.add(new ImmutablePair<>(FSB1_INTERNAL, FSB1_INTERNAL2));
    portTypeToIdList.add(new ImmutablePair<>(FSB2_INTERNAL, FSB2_INTERNAL2));
    addPortDataToCompositionData(portTypeToIdList, data2);

    unifiedCompositionDataList.add(data1);
    unifiedCompositionDataList.add(data2);
    return unifiedCompositionDataList;
  }


  private UnifiedCompositionData createComputeUnifiedCompositionData(String computeNodeTemplateId) {
    NodeTemplate computeNodeTemplate =
        DataModelUtil.getNodeTemplate(inputServiceTemplates.get(mainSTName), computeNodeTemplateId);
    Optional<List<RequirementAssignmentData>> requirementAssignmentDataList =
        TestUtils.getRequirementAssignmentDataList(computeNodeTemplate, "local_storage");
    Map<String, List<RequirementAssignmentData>> volume = null;
    if (requirementAssignmentDataList.isPresent()) {
      volume = getVolume(requirementAssignmentDataList.get());
    }
    UnifiedCompositionData data = new UnifiedCompositionData();
    Map<String, List<RequirementAssignmentData>> computeNodeConnectedOut =
        TestUtils.getNodeConnectedOutList(computeNodeTemplate, DEPENDENCY);
    Map<String, List<RequirementAssignmentData>> computeNodeConnectedIn =
        TestUtils
            .getNodeConnectedInList(computeNodeTemplateId, inputServiceTemplates.get(mainSTName),
                DEPENDENCY);
    ComputeTemplateConsolidationData computeTemplateConsolidationData = TestUtils
        .createComputeTemplateConsolidationData(computeNodeTemplateId, null, volume);
    List<String> computeNodeGroups =
        TestUtils.getGroupsForNode(inputServiceTemplates.get(mainSTName),
            computeNodeTemplateId);
    if (!computeNodeGroups.isEmpty()) {
      computeTemplateConsolidationData.setGroupIds(computeNodeGroups);
    }
    computeTemplateConsolidationData.setNodesConnectedOut(computeNodeConnectedOut);
    computeTemplateConsolidationData.setNodesConnectedIn(computeNodeConnectedIn);
    data.setComputeTemplateConsolidationData(computeTemplateConsolidationData);
    return data;
  }

  private void addPortDataToCompositionData(List<Pair<String, String>> portTypeToIdList,
                                            UnifiedCompositionData data) {
    ComputeTemplateConsolidationData computeTemplateConsolidationData = data
        .getComputeTemplateConsolidationData();

    for (Pair<String, String> port : portTypeToIdList) {
      NodeTemplate portNodeTemplate =
          DataModelUtil.getNodeTemplate(inputServiceTemplates.get(mainSTName), port.getRight());

      Optional<List<RequirementAssignmentData>> bindingReqList =
          TestUtils.getRequirementAssignmentDataList(portNodeTemplate, "binding");

      if (bindingReqList.isPresent()) {
        for (RequirementAssignmentData reqData : bindingReqList.get()) {
          String nodeId = reqData.getRequirementAssignment().getNode();
          if (nodeId.equals(computeTemplateConsolidationData.getNodeTemplateId())) {
            computeTemplateConsolidationData.addPort(port.getLeft(), port.getRight());
          }
        }
      }
      Map<String, List<RequirementAssignmentData>> portNodeConnectedOut =
          TestUtils.getNodeConnectedOutList(portNodeTemplate, "link");
      PortTemplateConsolidationData portTemplateConsolidationData = TestUtils
          .createPortTemplateConsolidationData(port.getRight());
      portTemplateConsolidationData.setNodesConnectedOut(portNodeConnectedOut);

      //Add node connected in info to test data
      Map<String, List<RequirementAssignmentData>> portNodeConnectedIn =
          TestUtils.getNodeConnectedInList(port.getRight(), inputServiceTemplates.get(mainSTName),
              PORT);
      portTemplateConsolidationData.setNodesConnectedIn(portNodeConnectedIn);

      //Add group infromation for ports
      List<String> portGroups =
          TestUtils.getGroupsForNode(inputServiceTemplates.get(mainSTName), port.getRight());
      portTemplateConsolidationData.setGroupIds(portGroups);
      data.addPortTemplateConsolidationData(portTemplateConsolidationData);

    }
    addGetAttrForCompute(data);
    addGetAttrForPort(data);
  }

  private Map<String, List<RequirementAssignmentData>> getVolume(
      List<RequirementAssignmentData> requirementAssignmentList) {
    Map<String, List<RequirementAssignmentData>> volume = new HashMap<>();
    for (RequirementAssignmentData requirementAssignmentData : requirementAssignmentList) {
      String volumeNodeTemplateId = requirementAssignmentData.getRequirementAssignment().getNode();
      volume.computeIfAbsent(volumeNodeTemplateId, k -> new ArrayList<>());
      volume.get(volumeNodeTemplateId).add(requirementAssignmentData);
    }
    return volume;
  }

  private void addGetAttrForPort(UnifiedCompositionData unifiedCompositionData) {
    for (PortTemplateConsolidationData portTemplateConsolidationData : unifiedCompositionData
        .getPortTemplateConsolidationDataList()) {
      if (portTemplateConsolidationData.getNodeTemplateId().equals(FSB1_INTERNAL1)) {
        addGetAttInUnifiedCompositionData(portTemplateConsolidationData, "network_name",
            NETWORK_ID, JSA_NET1);
        addGetAttInUnifiedCompositionData(portTemplateConsolidationData, SIZE,
            DEVICE_OWNER, CMAUI_VOLUME1);
      } else if (portTemplateConsolidationData.getNodeTemplateId().equals(FSB2_INTERNAL2)) {
        addGetAttInUnifiedCompositionData(portTemplateConsolidationData, TENANT_ID,
            NETWORK_ID, JSA_NET1);
        addGetAttInUnifiedCompositionData(portTemplateConsolidationData, "qos_policy",
            NETWORK_ID, JSA_NET1);
        addGetAttInUnifiedCompositionData(portTemplateConsolidationData, VOLUME_TYPE,
            TENANT_ID, CMAUI_VOLUME1);
      } else if (portTemplateConsolidationData.getNodeTemplateId().equals(FSB1_OAM)) {
        addGetAttInUnifiedCompositionData(portTemplateConsolidationData, SIZE,
            STATUS, CMAUI_VOLUME1);
      }
    }
  }

  private void addGetAttrForPort2(UnifiedCompositionData unifiedCompositionData) {
    for (PortTemplateConsolidationData portTemplateConsolidationData : unifiedCompositionData
        .getPortTemplateConsolidationDataList()) {
      if (portTemplateConsolidationData.getNodeTemplateId().equals(FSB2_INTERNAL1)) {
        addGetAttInUnifiedCompositionData(portTemplateConsolidationData, VOLUME_TYPE,
            TENANT_ID, CMAUI_VOLUME3);
      } else if (portTemplateConsolidationData.getNodeTemplateId().equals(FSB1_INTERNAL2)) {
        addGetAttInUnifiedCompositionData(portTemplateConsolidationData, SIZE,
            DEVICE_OWNER, CMAUI_VOLUME3);
        addGetAttInUnifiedCompositionData(portTemplateConsolidationData, SIZE,
            STATUS, CMAUI_VOLUME1);
      }
    }
  }

  private void addGetAttrForPortInnerUC(UnifiedCompositionData unifiedCompositionData) {
    for (PortTemplateConsolidationData portTemplateConsolidationData : unifiedCompositionData
        .getPortTemplateConsolidationDataList()) {
      if (portTemplateConsolidationData.getNodeTemplateId().equals(FSB1_INTERNAL1)) {
        addGetAttInUnifiedCompositionData(portTemplateConsolidationData, AVAILABILITY_ZONE,
            MYATTR, FSB1_template);
        addGetAttInUnifiedCompositionData(portTemplateConsolidationData, "metadata",
            MYATTR, FSB1_template);
        addGetAttInUnifiedCompositionData(portTemplateConsolidationData, "name",
            MYATTR, FSB1_template);
        addGetAttInUnifiedCompositionData(portTemplateConsolidationData, AVAILABILITY_ZONE,
            TENANT_ID, FSB1_template);
      }
    }
  }

  private void addGetAttrForCompute(UnifiedCompositionData unifiedCompositionData) {
    addGetAttInUnifiedCompositionData(unifiedCompositionData.getComputeTemplateConsolidationData(),
        "dhcp_agent_ids", ADDRESSES, JSA_NET1);
    addGetAttInUnifiedCompositionData(unifiedCompositionData.getComputeTemplateConsolidationData(),
        VOLUME_TYPE, ADDRESSES, CMAUI_VOLUME1);
    addGetAttInUnifiedCompositionData(unifiedCompositionData.getComputeTemplateConsolidationData(),
        SIZE, ACCESS_IPv6, CMAUI_VOLUME2);
  }

  private void addGetAttrForCompute2(UnifiedCompositionData unifiedCompositionData) {
    addGetAttInUnifiedCompositionData(unifiedCompositionData.getComputeTemplateConsolidationData(),
        VOLUME_TYPE, ADDRESSES, CMAUI_VOLUME3);
    addGetAttInUnifiedCompositionData(unifiedCompositionData.getComputeTemplateConsolidationData(),
        SIZE, USER_DATA_FORMAT, CMAUI_VOLUME3);
  }

  private void addOutputGetAttrInForComputeNoConsolidation(
      UnifiedCompositionData unifiedCompositionData) {
    addOutputGetAttInUnifiedCompositionData(unifiedCompositionData
        .getComputeTemplateConsolidationData(), SIMPLE_OUTPUT1, ACCESS_IPv4);
    addOutputGetAttInUnifiedCompositionData(unifiedCompositionData
        .getComputeTemplateConsolidationData(), SIMPLE_OUTPUT2, ADDRESSES);
    addOutputGetAttInUnifiedCompositionData(unifiedCompositionData
        .getComputeTemplateConsolidationData(), COMPLEX_OUTPUT1, ADDRESSES);
    addOutputGetAttInUnifiedCompositionData(unifiedCompositionData
        .getComputeTemplateConsolidationData(), COMPLEX_OUTPUT3, ACCESS_IPv6);

  }

  private void addOutputGetAttrInForCompute1WithConsolidation(
      UnifiedCompositionData unifiedCompositionData) {
    addOutputGetAttInUnifiedCompositionData(unifiedCompositionData
        .getComputeTemplateConsolidationData(), SIMPLE_OUTPUT1, ACCESS_IPv4);
    addOutputGetAttInUnifiedCompositionData(unifiedCompositionData
        .getComputeTemplateConsolidationData(), COMPLEX_OUTPUT1, ADDRESSES);

  }

  private void addOutputGetAttrInForCompute2WithConsolidation(
      UnifiedCompositionData unifiedCompositionData) {
    addOutputGetAttInUnifiedCompositionData(unifiedCompositionData
        .getComputeTemplateConsolidationData(), SIMPLE_OUTPUT2, ADDRESSES);
  }

  private void addOutputGetAttrInForPortNoConsolidation(
      UnifiedCompositionData unifiedCompositionData) {
    for (PortTemplateConsolidationData portTemplateConsolidationData : unifiedCompositionData
        .getPortTemplateConsolidationDataList()) {
      if (portTemplateConsolidationData.getNodeTemplateId().equals(FSB1_INTERNAL1)) {
        addOutputGetAttInUnifiedCompositionData(portTemplateConsolidationData, COMPLEX_OUTPUT2,
            DEVICE_OWNER);
        addOutputGetAttInUnifiedCompositionData(portTemplateConsolidationData, COMPLEX_OUTPUT3,
            DEVICE_OWNER);
      } else if (portTemplateConsolidationData.getNodeTemplateId().equals(FSB2_INTERNAL2)) {
        addOutputGetAttInUnifiedCompositionData(portTemplateConsolidationData, COMPLEX_OUTPUT1,
            TENANT_ID);
      } else if (portTemplateConsolidationData.getNodeTemplateId().equals(FSB1_OAM)) {
        addOutputGetAttInUnifiedCompositionData(portTemplateConsolidationData, COMPLEX_OUTPUT2,
            USER_DATA_FORMAT);
      }
    }
  }

  private void addOutputGetAttrInForPortWithConsolidation1(
      UnifiedCompositionData unifiedCompositionData) {
    for (PortTemplateConsolidationData portTemplateConsolidationData : unifiedCompositionData
        .getPortTemplateConsolidationDataList()) {
      if (portTemplateConsolidationData.getNodeTemplateId().equals(FSB2_INTERNAL2)) {
        addOutputGetAttInUnifiedCompositionData(portTemplateConsolidationData, COMPLEX_OUTPUT1,
            TENANT_ID);
      } else if (portTemplateConsolidationData.getNodeTemplateId().equals(FSB1_INTERNAL1)) {
        addOutputGetAttInUnifiedCompositionData(portTemplateConsolidationData, COMPLEX_OUTPUT3,
            "admin_state_up");
      }
    }
  }

  private void addOutputGetAttrInForPortWithConsolidation2(
      UnifiedCompositionData unifiedCompositionData) {
    for (PortTemplateConsolidationData portTemplateConsolidationData : unifiedCompositionData
        .getPortTemplateConsolidationDataList()) {
      if (portTemplateConsolidationData.getNodeTemplateId().equals(FSB2_INTERNAL1)) {
        addOutputGetAttInUnifiedCompositionData(portTemplateConsolidationData, COMPLEX_OUTPUT2,
            USER_DATA_FORMAT);
      } else if (portTemplateConsolidationData.getNodeTemplateId().equals(FSB1_INTERNAL2)) {
        addOutputGetAttInUnifiedCompositionData(portTemplateConsolidationData, COMPLEX_OUTPUT2,
            DEVICE_OWNER);
      }
    }
  }

  private NodeTemplate getMockNode(String path) throws IOException {
    URL resource = this.getClass().getResource(path);
    YamlUtil yamlUtil = new YamlUtil();
    return yamlUtil.yamlToObject(resource.openStream(), NodeTemplate.class);
  }

}