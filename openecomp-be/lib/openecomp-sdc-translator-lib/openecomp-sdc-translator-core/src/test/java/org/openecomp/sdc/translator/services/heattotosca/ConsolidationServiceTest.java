package org.openecomp.sdc.translator.services.heattotosca;

/**
 * Created by TALIO on 3/7/2017.
 */
public class ConsolidationServiceTest {

  /*
  @Spy
  private TranslationContext translationContext = new TranslationContext();
  private static String mainST = "MainServiceTemplate.yaml";

  @Mock
  private UnifiedCompositionService unifiedCompositionServiceMock;

  @Captor
  private ArgumentCaptor<List<UnifiedCompositionData>> unifiedModelListArg;
  @Captor
  private ArgumentCaptor<UnifiedCompositionMode> unifiedCompositionModeArg;

  @Spy
  @InjectMocks
  private ConsolidationService consolidationService;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testConsolidationValidPreCondition() throws IOException {

    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel(
        "/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/valid_pre_condition/",
        null,
        null);

    TestUtils.initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName,
        consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1",
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.ScalingInstances);

    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testConsolidationFalsePreConditionOneComputeNode() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/one_compute_node/",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1",
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.SingleSubstitution);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testConsolidationFalsePreConditionMoreThanOnePortPerType() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/more_than_one_port/",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_2"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);


    TestUtils.initPortConsolidationData(mainST, consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.SingleSubstitution);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testConsolidationFalsePreConditionDifferentPortTypesBetweenComputes()
      throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/different_port_types",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1",
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.SingleSubstitution);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);

  }

  @Test
  public void testConsolidationResultTrueWithMoreThanTwoCompute() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/three_compute_valid",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1",
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);

    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testConsolidationResultFalseWithMoreThanTwoComputeOneIsDiff() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/three_compute_valid",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1",
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.SingleSubstitution);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testConsolidationResultFalseForTwoTypesOfComputeWithOneInstanceEach()
      throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName1 = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    String computeNodeTypeName2 = "org.openecomp.resource.vfc.nodes.heat.cmaui";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/three_compute_valid",
            null, null);

    TestUtils.initComputeNodeTypeInConsolidationData
        (mainST, computeNodeTypeName1, consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData
        (mainST, computeNodeTypeName2, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName1, "server_ps01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName2, "server_ps02",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1",
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);

    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.SingleSubstitution,
            UnifiedCompositionMode.SingleSubstitution);
    verifyMainServiceTemplateConsolidation(2, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testConsolidationValidForTwoSimilarComputeAndFalseForSingleCompute()
      throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName1 = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    String computeNodeTypeName2 = "org.openecomp.resource.vfc.nodes.heat.cmaui";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/three_compute_two_similar_one_diff",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName1, consolidationData);
    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName2, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName1, "server_ps01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName1, "server_ps02",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName2, "server_ps03",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1",
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(
            UnifiedCompositionMode.ScalingInstances, UnifiedCompositionMode.SingleSubstitution);
    verifyMainServiceTemplateConsolidation(2, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testValidComputeAndPortConsolidation() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_compute_valid",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1",
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testFalseComputeConsolidationForTwoSimilarImageNamesAndOneDiff() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName1 = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    String computeNodeTypeName2 = "org.openecomp.resource.vfc.nodes.heat.pd_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/one_image_name_diff",
            null, null);

    TestUtils.initComputeNodeTypeInConsolidationData(
        mainST, computeNodeTypeName1, consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(
        mainST, computeNodeTypeName2, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName1, "server_ps01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName1, "server_ps02",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName1, "server_ps03",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName2, "server_pd01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName2, "server_pd02",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName2, "server_pd03",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1",
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays
            .asList(UnifiedCompositionMode.CatalogInstance, UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(2, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testFalseComputeConsolidationOneImageNameMissing() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.pd_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/one_image_missing",
            null, null);

    TestUtils.initComputeNodeTypeInConsolidationData(
        mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_pd01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_pd02",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_pd03",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1",
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testFalseComputeConsolidationForTwoSimilarFlavorNamesAndOneDiff() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/one_flavor_name_diff",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1",
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testFalsePortConsolidationForOneDiffFixedIpsValue() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/one_exCP_naming_diff",
            null, null);

    TestUtils.initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testFalsePortConsolidationForOneDiffExpcNaming(){

  }

  @Test
  public void testFalsePortConsolidationForTwoPortsWithFixedIpsAndOneWithout() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/one_port_without_fixed_ips",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testFalsePortConsolidationForTwoPortsWithAddressPairsAndOneWithout()
      throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/one_port_without_allowed_address_pairs",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);
    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testFalsePortConsolidationForTwoPortsWithMacAddressAndOneWithout()
      throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/one_port_without_mac_address",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);
    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testValidPortConsolidationForPortsWithNoneOfTheCheckedProperties()
      throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/ports_with_none_of_the_properties",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"), Arrays.asList("group_id_1"), null,
        null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_2",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"), Arrays.asList("group_id_1"), null,
        null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_3",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"), Arrays.asList("group_id_1"), null,
        null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testComputeRelationsSimilarBetweenComputeNodes() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_compute_with_same_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"),
        Arrays.asList("cmaui_volume"), Arrays.asList("group_id1"), null, null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"), Arrays.asList("group_id_1"), null,
        null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_2",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"), Arrays.asList("group_id_1"), null,
        null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_3",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"), Arrays.asList("group_id_1"), null,
        null,
        consolidationData);


    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testNodesInRelationsDiffBetweenThreeComputes() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Arrays.asList("cm01_port_1"), Arrays.asList
            ("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id1"), null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Arrays.asList("cm01_port_2"), Arrays.asList
            ("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id1"), null, null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", null, null, null, null, null,
        consolidationData);


    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testNodesOutRelationsDiffBetweenThreeComputes() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Arrays.asList("cm01_port_1"), Arrays.asList
            ("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id1"), null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume_1"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", null, null, null, null, null,
        consolidationData);


    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testVolumeRelationsDiffBetweenThreeComputes() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_compute_valid",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Arrays.asList("cm01_port_1"), Arrays.asList
            ("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id1"), null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume_1"), Arrays.asList("group_id1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", null, null, null, null, null,
        consolidationData);


    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testGroupRelationsDiffBetweenThreeComputes() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_compute_valid",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Arrays.asList("cm01_port_1"), Arrays.asList
            ("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"), null,
        null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_2"),
        null, null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", null, null, null, null, null,
        consolidationData);


    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testPortConsolidationDataRelationsSimilar() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Arrays.asList("cm01_port_1"), Arrays.asList
            ("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"), null,
        null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"), Arrays.asList("group_id_1"), null,
        null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_2",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"), Arrays.asList("group_id_1"), null,
        null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_3",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"), Arrays.asList("group_id_1"), null,
        null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testNodesInRelationsDiffBetweenThreePortConsolidationDatas() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Arrays.asList("cm01_port_1"), Arrays.asList
            ("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"), null,
        null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"), Arrays.asList("group_id_1"), null,
        null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_2",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"), Arrays.asList("group_id_1"), null,
        null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_3",
        Arrays.asList("node_in_2"), Arrays.asList("net_id_1"), Arrays.asList("group_id_1"), null,
        null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testNodesOutRelationsDiffBetweenThreePortConsolidationDatas() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Arrays.asList("cm01_port_1"), Arrays.asList
            ("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"), null,
        null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"), Arrays.asList("group_id_1"), null,
        null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_2",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"), Arrays.asList("group_id_1"), null,
        null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_3",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_2"), Arrays.asList("group_id_1"), null,
        null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testGroupIdsRelationsDiffBetweenThreePortConsolidationDatas() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Arrays.asList("cm01_port_1"), Arrays.asList
            ("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"), null,
        null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"), null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_2",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"), null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_3",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_3"), null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testValidConsolidationForComputesWithValidGetAttr() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Arrays.asList("cm01_port_1"), Arrays.asList
            ("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("node_1"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("node_2"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("node_3"), null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"), null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_2",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"), null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_3",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"), null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testInvalidConsolidationForComputesWithGetAttrForEachOther() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Arrays.asList("cm01_port_1"), Arrays.asList
            ("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("server_ps02"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("node_1"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("node_2"), null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"), null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_2",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"), null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_3",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"), null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testValidConsolidationForPortsWithValidGetAttr() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Arrays.asList("cm01_port_1"), Arrays.asList
            ("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"), null,
        null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    List<String> portNodeTemplateIds =
        Arrays.asList("cm01_port_1", "cm01_port_2", "cm01_port_3");
    List<List<String>> nodesConnectedInIds =
        Arrays.asList(Arrays.asList("node_in_1"), Arrays.asList("node_in_1"),
            Arrays.asList("node_in_1"));
    List<List<String>> nodesConnectedOutIds =
        Arrays.asList(Arrays.asList("node_out_1"), Arrays.asList("node_out_1"),
            Arrays.asList("node_out_1"));
    List<List<String>> getAttrInIds =
        Arrays.asList(Arrays.asList("get_attr_1"), Arrays.asList("get_attr_2"),
            Arrays.asList("get_attr_3"));
    List<List<String>> groupIds =
        Arrays.asList(Arrays.asList("group_id_1", "group_id_2"), Arrays.asList("group_id_1",
            "group_id_2"), Arrays.asList("group_id_1", "group_id_2"));
    List<List<Pair<String, GetAttrFuncData>>> getAttrOut = Arrays.asList(null, null, null);

    TestUtils.updateMultiplePortConsolidationDatas(
        mainST, portNodeTemplateIds, nodesConnectedInIds, nodesConnectedOutIds, groupIds,
        getAttrInIds, getAttrOut, consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testInvalidConsolidationForPortsWithGetAttrInForEachOther() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation" +
                "/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Arrays.asList("cm01_port_1"), Arrays.asList
            ("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("node_1"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("node_2"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("node_3"), null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    List<String> portNodeTemplateIds =
        Arrays.asList("cm01_port_1", "cm01_port_2", "cm01_port_3");
    List<List<String>> nodesConnectedInIds =
        Arrays.asList(Arrays.asList("node_in_1"), Arrays.asList("node_in_1"),
            Arrays.asList("node_in_1"));
    List<List<String>> nodesConnectedOutIds =
        Arrays.asList(Arrays.asList("node_out_1"), Arrays.asList("node_out_1"),
            Arrays.asList("node_out_1"));
    List<List<String>> getAttrInIds =
        Arrays.asList(Arrays.asList("get_attr_1"), Arrays.asList("get_attr_2"),
            Arrays.asList("cm01_port_1"));
    List<List<String>> groupIds =
        Arrays.asList(Arrays.asList("group_id_1", "group_id_2"), Arrays.asList("group_id_1",
            "group_id_2"), Arrays.asList("group_id_1", "group_id_2"));
    List<List<Pair<String, GetAttrFuncData>>> getAttrOut = Arrays.asList(null, null, null);

    TestUtils.updateMultiplePortConsolidationDatas(
        mainST, portNodeTemplateIds, nodesConnectedInIds, nodesConnectedOutIds, groupIds,
        getAttrInIds, getAttrOut, consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testValidConsolidationForComputesWithSamePortTypesPointingByGetAttrIn() throws
      IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Arrays.asList("cm01_port_1"), Arrays.asList
            ("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("cm01_port_1"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("cm01_port_2"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("cm01_port_3"), null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    List<String> portNodeTemplateIds =
        Arrays.asList("cm01_port_1", "cm01_port_2", "cm01_port_3");
    List<List<String>> nodesConnectedInIds =
        Arrays.asList(Arrays.asList("node_in_1"), Arrays.asList("node_in_1"),
            Arrays.asList("node_in_1"));
    List<List<String>> nodesConnectedOutIds =
        Arrays.asList(Arrays.asList("node_out_1"), Arrays.asList("node_out_1"),
            Arrays.asList("node_out_1"));
    List<List<String>> getAttrInIds =
        Arrays.asList(Arrays.asList("get_attr_1"), Arrays.asList("get_attr_2"),
            Arrays.asList("get_attr_3"));
    List<List<String>> groupIds =
        Arrays.asList(Arrays.asList("group_id_1", "group_id_2"), Arrays.asList("group_id_1",
            "group_id_2"), Arrays.asList("group_id_1", "group_id_2"));
    List<List<Pair<String, GetAttrFuncData>>> getAttrOut = Arrays.asList(null, null, null);

    TestUtils.updateMultiplePortConsolidationDatas(
        mainST, portNodeTemplateIds, nodesConnectedInIds, nodesConnectedOutIds, groupIds,
        getAttrInIds, getAttrOut, consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testValidGetAttrOutFromComputes() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    GetAttrFuncData getAttrFuncData = new GetAttrFuncData("name", "c1_name");
    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Arrays.asList("cm01_port_1"), Arrays.asList
            ("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("cm01_port_1"),
        Arrays.asList(new ImmutablePair<String, GetAttrFuncData>("cm01_port_1", getAttrFuncData)),
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("cm01_port_1"),
        Arrays.asList(new ImmutablePair<String, GetAttrFuncData>("cm01_port_1", getAttrFuncData)),
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("cm01_port_1"),
        Arrays.asList(new ImmutablePair<String, GetAttrFuncData>("cm01_port_1", getAttrFuncData)),
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"), Arrays.asList("sm01_port_2"), null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_2",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"), Arrays.asList("sm01_port_1"), null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_3",
        Arrays.asList("node_in_1"), Arrays.asList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"), Arrays.asList("rm01_port_1"), null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testInValidGetAttrOutFromComputesPortTypeDifferent() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    GetAttrFuncData getAttrFuncData = new GetAttrFuncData("name", "c1_name");
    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Arrays.asList("cm01_port_1"), Arrays.asList
            ("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("cm01_port_1"),
        Arrays.asList(new ImmutablePair<String, GetAttrFuncData>("cm01_port_1", getAttrFuncData)),
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("cm01_port_1"),
        Arrays.asList(new ImmutablePair<String, GetAttrFuncData>("cm01_port_1", getAttrFuncData)),
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("cm01_port_1"),
        Arrays.asList(new ImmutablePair<String, GetAttrFuncData>("sm01_port_1", getAttrFuncData)),
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);

    List<String> portNodeTemplateIds =
        Arrays.asList("cm01_port_1", "cm01_port_2", "cm01_port_3", "sm01_port_1", "sm01_port_2",
            "sm01_port_3");
    List<List<String>> nodesConnectedInIds =
        Arrays.asList(Arrays.asList("node_in_1"), Arrays.asList("node_in_1"), Arrays.asList
                ("node_in_1"), Arrays.asList("node_in_1"), Arrays.asList("node_in_1"),
            Arrays.asList("node_in_1"));
    List<List<String>> nodesConnectedOutIds =
        Arrays.asList(Arrays.asList("node_out_1"), Arrays.asList("node_out_1"), Arrays.asList
                ("node_out_1"), Arrays.asList("node_out_1"), Arrays.asList("node_out_1"),
            Arrays.asList("node_out_1"));
    List<List<String>> getAttrInIds =
        Arrays.asList(Arrays.asList("get_attr_1"), Arrays.asList("get_attr_2"), Arrays.asList
                ("get_attr_3"), Arrays.asList("get_attr_1"), Arrays.asList("get_attr_2"),
            Arrays.asList("get_attr_3"));
    List<List<String>> groupIds =
        Arrays.asList(Arrays.asList("group_id_1", "group_id_2"), Arrays.asList("group_id_1",
            "group_id_2"), Arrays.asList("group_id_1", "group_id_2"),
            Arrays.asList("group_id_1", "group_id_2"), Arrays.asList("group_id_1",
                "group_id_2"), Arrays.asList("group_id_1", "group_id_2"));
    List<List<Pair<String, GetAttrFuncData>>> getAttrOut = Arrays.asList(null, null, null, null,
        null, null);

    TestUtils.updateMultiplePortConsolidationDatas(mainST, portNodeTemplateIds,
        nodesConnectedInIds, nodesConnectedOutIds, groupIds, getAttrInIds, getAttrOut,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.SingleSubstitution);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testValidGetAttrOutFromPorts() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Arrays.asList("cm01_port_1"), Arrays.asList
            ("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("cm01_port_1"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("cm01_port_1"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("cm01_port_1"), null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);

    GetAttrFuncData getAttrFuncData = new GetAttrFuncData("name", "s1_name");
    List<String> portNodeTemplateIds =
        Arrays.asList("cm01_port_1", "cm01_port_2", "cm01_port_3", "sm01_port_1", "sm01_port_2",
            "sm01_port_3");
    List<List<String>> nodesConnectedInIds =
        Arrays.asList(Arrays.asList("node_in_1"), Arrays.asList("node_in_1"), Arrays.asList
                ("node_in_1"), Arrays.asList("node_in_1"), Arrays.asList("node_in_1"),
            Arrays.asList("node_in_1"));
    List<List<String>> nodesConnectedOutIds =
        Arrays.asList(Arrays.asList("node_out_1"), Arrays.asList("node_out_1"), Arrays.asList
                ("node_out_1"), Arrays.asList("node_out_1"), Arrays.asList("node_out_1"),
            Arrays.asList("node_out_1"));
    List<List<String>> getAttrInIds =
        Arrays.asList(null, null, null, null, null, null);
    List<List<String>> groupIds =
        Arrays.asList(Arrays.asList("group_id_1", "group_id_2"), Arrays.asList("group_id_1",
            "group_id_2"), Arrays.asList("group_id_1", "group_id_2"),
            Arrays.asList("group_id_1", "group_id_2"), Arrays.asList("group_id_1",
                "group_id_2"), Arrays.asList("group_id_1", "group_id_2"));
    List<List<Pair<String, GetAttrFuncData>>> getAttrOut = Arrays.asList(
        Arrays.asList(new ImmutablePair<String, GetAttrFuncData>("sm01_port_1", getAttrFuncData)),
        Arrays.asList(new ImmutablePair<String, GetAttrFuncData>("sm01_port_2", getAttrFuncData)),
        Arrays.asList(new ImmutablePair<String, GetAttrFuncData>("sm01_port_3", getAttrFuncData)),
        Arrays.asList(new ImmutablePair<String, GetAttrFuncData>("cm01_port_1", getAttrFuncData)),
        Arrays.asList(new ImmutablePair<String, GetAttrFuncData>("cm01_port_2", getAttrFuncData)),
        Arrays.asList(new ImmutablePair<String, GetAttrFuncData>("cm01_port_3", getAttrFuncData)));

    TestUtils.updateMultiplePortConsolidationDatas(mainST, portNodeTemplateIds,
        nodesConnectedInIds, nodesConnectedOutIds, groupIds, getAttrInIds, getAttrOut,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testInvalidGetAttrOutFromPorts() throws IOException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Arrays.asList("cm01_port_1"), Arrays.asList
            ("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("cm01_port_1"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_2"));
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("cm01_port_1"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>("cm01_port", "cm01_port_3"));
    portTypeToIdList.add(new ImmutablePair<>("sm01_port", "sm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Arrays.asList("cm01_port_1"),
        Arrays.asList("cmaui_volume"), Arrays.asList("cmaui_volume"), Arrays.asList("group_id_1"),
        Arrays.asList("cm01_port_1"), null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);

    GetAttrFuncData getAttrFuncData = new GetAttrFuncData("name", "s1_name");
    List<String> portNodeTemplateIds =
        Arrays.asList("cm01_port_1", "cm01_port_2", "cm01_port_3", "sm01_port_1", "sm01_port_2",
            "sm01_port_3");
    List<List<String>> nodesConnectedInIds =
        Arrays.asList(Arrays.asList("node_in_1"), Arrays.asList("node_in_1"), Arrays.asList
                ("node_in_1"), Arrays.asList("node_in_1"), Arrays.asList("node_in_1"),
            Arrays.asList("node_in_1"));
    List<List<String>> nodesConnectedOutIds =
        Arrays.asList(Arrays.asList("node_out_1"), Arrays.asList("node_out_1"), Arrays.asList
                ("node_out_1"), Arrays.asList("node_out_1"), Arrays.asList("node_out_1"),
            Arrays.asList("node_out_1"));
    List<List<String>> getAttrInIds =
        Arrays.asList(null, null, null, null, null, null);
    List<List<String>> groupIds =
        Arrays.asList(Arrays.asList("group_id_1", "group_id_2"), Arrays.asList("group_id_1",
            "group_id_2"), Arrays.asList("group_id_1", "group_id_2"),
            Arrays.asList("group_id_1", "group_id_2"), Arrays.asList("group_id_1",
                "group_id_2"), Arrays.asList("group_id_1", "group_id_2"));
    List<List<Pair<String, GetAttrFuncData>>> getAttrOut = Arrays.asList(
        Arrays.asList(new ImmutablePair<String, GetAttrFuncData>("server_ps01", getAttrFuncData)),
        Arrays.asList(new ImmutablePair<String, GetAttrFuncData>("server_ps01", getAttrFuncData)),
        Arrays.asList(new ImmutablePair<String, GetAttrFuncData>("sm01_port_3", getAttrFuncData)),
        Arrays.asList(new ImmutablePair<String, GetAttrFuncData>("cm01_port_1", getAttrFuncData)),
        Arrays.asList(new ImmutablePair<String, GetAttrFuncData>("cm01_port_2", getAttrFuncData)),
        Arrays.asList(new ImmutablePair<String, GetAttrFuncData>("cm01_port_3", getAttrFuncData)));

    TestUtils.updateMultiplePortConsolidationDatas(mainST, portNodeTemplateIds,
        nodesConnectedInIds, nodesConnectedOutIds, groupIds, getAttrInIds, getAttrOut,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.SingleSubstitution);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testSubstitutionConsolidationPreConditionTrue() throws IOException {
    translationContext = new TranslationContext();
    String mainSTName = "MainServiceTemplate.yaml";
    String nestedServiceTemplateName = "nested-pcm_v0.1ServiceTemplate.yaml";

    ConsolidationData consolidationData = new ConsolidationData();
    TestUtils.updateNestedConsolidationData(mainSTName, Arrays.asList("server_pcm_001"),
        consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(mainSTName,
        "org.openecomp.resource.vfc.nodes.heat" +
            ".pcm_server", consolidationData);
    TestUtils.initComputeNodeTemplateIdInConsolidationData(mainSTName,
        "org.openecomp.resource.vfc.nodes.heat.pcm_server", "server_pcm_001", consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(nestedServiceTemplateName,
        "org.openecomp.resource.vfc.nodes.heat" +
            ".pcm_server", consolidationData);
    TestUtils.initComputeNodeTemplateIdInConsolidationData(nestedServiceTemplateName,
        "org.openecomp.resource.vfc.nodes" +
            ".heat.pcm_server", "pcm_server", consolidationData);

    translationContext.setConsolidationData(consolidationData);

    addMockServiceTemplateToContext(mainSTName, "Main");
    addMockServiceTemplateToContext(nestedServiceTemplateName, "nested-pcm_v0.1");

    verifySubstitutionServiceTemplateConsolidation
        (1, translationContext.getTranslatedServiceTemplates().get(mainSTName),
            translationContext.getTranslatedServiceTemplates().get(nestedServiceTemplateName),
            UnifiedCompositionMode.NestedSingleCompute);
  }

  @Test
  public void testSubstitutionConsolidationPreConditionFalseMoreThanOneComputeType()
      throws IOException {
    translationContext = new TranslationContext();
    String mainSTName = "MainServiceTemplate.yaml";
    String nestedServiceTemplateName = "nested-pcm_v0.1ServiceTemplate.yaml";

    ConsolidationData consolidationData = new ConsolidationData();
    TestUtils.updateNestedConsolidationData(mainSTName, Arrays.asList("server_pcm_001"),
        consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(mainSTName,
        "org.openecomp.resource.vfc.nodes.heat" +
            ".pcm_server", consolidationData);
    TestUtils.initComputeNodeTemplateIdInConsolidationData(mainSTName,
        "org.openecomp.resource.vfc.nodes" +
            ".heat.pcm_server", "server_pcm_001", consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(nestedServiceTemplateName,
        "org.openecomp.resource.vfc.nodes.heat" +
            ".pcm_server", consolidationData);

    TestUtils.updateNestedConsolidationData(mainSTName, Arrays.asList("server_oam_001"),
        consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(mainSTName,
        "org.openecomp.resource.vfc.nodes.heat" +
            ".oam_server", consolidationData);
    TestUtils.initComputeNodeTemplateIdInConsolidationData(mainSTName,
        "org.openecomp.resource.vfc.nodes.heat.oam_server", "server_oam_001", consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(nestedServiceTemplateName,
        "org.openecomp.resource.vfc.nodes.heat" +
            ".oam_server", consolidationData);

    translationContext.setConsolidationData(consolidationData);

    addMockServiceTemplateToContext(mainSTName, "Main");
    addMockServiceTemplateToContext(nestedServiceTemplateName, "nested-pcm_v0.1");

    verifySubstitutionServiceTemplateConsolidation
        (1, translationContext.getTranslatedServiceTemplates().get(mainSTName),
            translationContext.getTranslatedServiceTemplates().get
                (nestedServiceTemplateName), UnifiedCompositionMode.SingleSubstitution);
  }

  @Test
  public void testSubstitutionConsolidationPreConditionFalseMoreThanOneComputeOfSameType()
      throws IOException {
    translationContext = new TranslationContext();
    String mainSTName = "MainServiceTemplate.yaml";
    String nestedServiceTemplateName = "nested-pcm_v0.1ServiceTemplate.yaml";

    ConsolidationData consolidationData = new ConsolidationData();
    TestUtils.updateNestedConsolidationData(mainSTName, Arrays.asList("server_pcm_001"),
        consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(nestedServiceTemplateName,
        "org.openecomp.resource.vfc.nodes.heat" +
            ".pcm_server", consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(mainSTName,
        "org.openecomp.resource.vfc.nodes.heat" +
            ".pcm_server", consolidationData);
    TestUtils.initComputeNodeTemplateIdInConsolidationData(nestedServiceTemplateName,
        "org.openecomp.resource.vfc.nodes" +
            ".heat.pcm_server", "pcm_server_1", consolidationData);

    TestUtils
        .updateNestedConsolidationData(nestedServiceTemplateName, Arrays.asList("server_pcm_002"),
            consolidationData);
    TestUtils.initComputeNodeTemplateIdInConsolidationData(nestedServiceTemplateName,
        "org.openecomp.resource.vfc.nodes" +
            ".heat.pcm_server", "pcm_server_2", consolidationData);

    translationContext.setConsolidationData(consolidationData);

    addMockServiceTemplateToContext(mainSTName, "Main");
    addMockServiceTemplateToContext(nestedServiceTemplateName, "nested-pcm_v0.1");

    Mockito.doNothing().when(consolidationService).serviceTemplateConsolidation
        (translationContext.getTranslatedServiceTemplates().get(nestedServiceTemplateName), translationContext);
    verifySubstitutionServiceTemplateConsolidation
        (1, translationContext.getTranslatedServiceTemplates().get(mainSTName),
            translationContext.getTranslatedServiceTemplates().get(nestedServiceTemplateName),
            UnifiedCompositionMode.SingleSubstitution);
  }

  @Test
  public void testSubstitutionConsolidationPreConditionFalseNestedInsideNested() {
    translationContext = new TranslationContext();
    String mainSTName = "MainServiceTemplate.yaml";
    String nestedServiceTemplateName = "nested-pcm_v0.1ServiceTemplate.yaml";

    ConsolidationData consolidationData = new ConsolidationData();
    TestUtils.updateNestedConsolidationData(mainSTName, Arrays.asList("server_pcm_001"),
        consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(mainSTName,
        "org.openecomp.resource.vfc.nodes.heat" +
            ".pcm_server", consolidationData);
    TestUtils.initComputeNodeTemplateIdInConsolidationData(mainSTName,
        "org.openecomp.resource.vfc.nodes.heat.pcm_server", "server_pcm_001", consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(nestedServiceTemplateName,
        "org.openecomp.resource.vfc.nodes.heat" +
            ".pcm_server", consolidationData);
    TestUtils.initComputeNodeTemplateIdInConsolidationData(nestedServiceTemplateName,
        "org.openecomp.resource.vfc.nodes" +
            ".heat.pcm_server", "pcm_server", consolidationData);

    TestUtils
        .updateNestedConsolidationData(nestedServiceTemplateName, Arrays.asList("nested_resource"),
            consolidationData);

    translationContext.setConsolidationData(consolidationData);

    addMockServiceTemplateToContext(mainSTName, "Main");
    addMockServiceTemplateToContext(nestedServiceTemplateName, "nested-pcm_v0.1");

    verifySubstitutionServiceTemplateConsolidation
        (0, translationContext.getTranslatedServiceTemplates().get(mainSTName),
            translationContext.getTranslatedServiceTemplates().get(nestedServiceTemplateName),
            UnifiedCompositionMode.SingleSubstitution);
  }


  private void verifyMainServiceTemplateConsolidation(int times,
                                                      List<UnifiedCompositionMode> expectedUnifiedCompositionModes,
                                                      ToscaServiceModel toscaServiceModel) {
    consolidationService
        .serviceTemplateConsolidation(toscaServiceModel.getServiceTemplates().get(mainST),
            translationContext);

    ServiceTemplate mainServiceTemplate = toscaServiceModel.getServiceTemplates().get(mainST);
    verify(unifiedCompositionServiceMock, times(times))
        .createUnifiedComposition(
            eq(mainServiceTemplate),
            any(),
            unifiedModelListArg.capture(),
            unifiedCompositionModeArg.capture(),
            eq(translationContext));

    List<UnifiedCompositionMode> actualUnifiedCompositionModes =
        unifiedCompositionModeArg.getAllValues();
    Assert.assertEquals(expectedUnifiedCompositionModes, actualUnifiedCompositionModes);
  }

  private void verifySubstitutionServiceTemplateConsolidation(int times,
                                                              ServiceTemplate mainST,
                                                              ServiceTemplate nestedST,
                                                              UnifiedCompositionMode expectedMode) {
    consolidationService.substitutionServiceTemplateConsolidation(null, mainST,
        nestedST, translationContext);

    if (expectedMode.equals(UnifiedCompositionMode.NestedSingleCompute)) {
      verify(unifiedCompositionServiceMock, times(times)).
          createUnifiedComposition(
              eq(mainST),
              eq(nestedST),
              unifiedModelListArg.capture(),
              unifiedCompositionModeArg.capture(),
              eq(translationContext));
      Assert.assertEquals(expectedMode, unifiedCompositionModeArg.getValue());

    }
  }

  private void addMockServiceTemplateToContext(String serviceTemplateFileName,
                                               String templateName) {
    ServiceTemplate serviceTemplate = new ServiceTemplate();
    Map<String, String> metadata = new HashMap<>();
    metadata.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME, templateName);
    serviceTemplate.setMetadata(metadata);
    translationContext.getTranslatedServiceTemplates()
        .put(serviceTemplateFileName, serviceTemplate);
  }

  */
}
