package org.openecomp.sdc.enrichment.impl.tosca;

public class ComponentQuestionnaireDataTest {
  /*private static String VSP_ID = "vspId";
  public static final Version VERSION01 = new Version(0, 1);
  private static final Version VERSION10 = new Version(1, 0);

  @Mock
  private ComponentDao componentDaoMock;

  @Mock
  private ComponentDependencyModelDao componentDependencyDaoMock;

  @InjectMocks
  private static ComponentQuestionnaireData componentQuestionnaireData;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testGetData() {
    ComponentEntity componentEntity = new ComponentEntity(VSP_ID, VERSION01,"ID1" );
    componentEntity.setCompositionData("{\n" +
        "  \"name\": \"org.openecomp.resource.vfc.nodes.heat.be\",\n" +
        "  \"displayName\": \"be\",\n" +
        "  \"vfcCode\": \"be_1\",\n" +
        "  \"nfcCode\": \"code\",\n" +
        "  \"nfcFunction\": \"desc\"\n" +
        "}");
    componentEntity.setQuestionnaireData
        ("{\"highAvailabilityAndLoadBalancing\":{\"isComponentMandatory\" : \"NO\"," +
            "\"highAvailabilityMode\":\"geo-activeactive\"},\"compute\":{\"numOfVMs\" " +
            ":{\"maximum\" : 5, \"minimum\" : 0}}}");

    List<ComponentEntity> entitites = new ArrayList<ComponentEntity>();
    entitites.add(componentEntity);

    doReturn(entitites).when(componentDaoMock).listCompositionAndQuestionnaire(VSP_ID, VERSION01);

    final Map<String, Map<String, Object>> propertiesfromCompQuestionnaire =
        componentQuestionnaireData.getPropertiesfromCompQuestionnaire(VSP_ID, VERSION01);

    final Map<String, Object> be = propertiesfromCompQuestionnaire.get("be");
    Assert.assertEquals(be.get(VFC_NAMING_CODE) , "be_1");
    Assert.assertEquals(be.get(VFC_CODE), "code");
    Assert.assertEquals(be.get(VFC_FUNCTION), "desc");
    Assert.assertEquals(be.get(MANDATORY) ,"NO");
    Assert.assertEquals(be.get(HIGH_AVAIL_MODE) ,"geo-activeactive");
    Assert.assertEquals(be.get(MIN_INSTANCES) ,null);
    Assert.assertEquals(be.get(MAX_INSTANCES) ,5);

    final Map<String, String> sourceToTargetComponent =
        componentQuestionnaireData.getSourceToTargetComponent();

    Assert.assertEquals("be", sourceToTargetComponent.get("ID1"));
  }


  @Test
  public void testPopulateDepnendency() {
    ComponentDependencyModelEntity sourceComponent = new ComponentDependencyModelEntity(VSP_ID, VERSION01,"ID1" );
    sourceComponent.setSourceComponentId("Comp1");
    sourceComponent.setTargetComponentId("Comp2");
    sourceComponent.setRelation("dependsOn");

    ComponentDependencyModelEntity targetComponent = new ComponentDependencyModelEntity(VSP_ID,
        VERSION01,"ID2" );
    targetComponent.setSourceComponentId("Comp1");
    targetComponent.setTargetComponentId("Comp3");
    targetComponent.setRelation("dependsOn");

    List<ComponentDependencyModelEntity> entitites = new ArrayList<ComponentDependencyModelEntity>();
    entitites.add(sourceComponent);
    entitites.add(targetComponent);

    doReturn(entitites).when(componentDependencyDaoMock).list(new ComponentDependencyModelEntity
        (VSP_ID, VERSION01, null));

    final Map<String, String> sourceToTargetComponent = new HashMap<String, String>();
    sourceToTargetComponent.put("Comp1", "fe");
    sourceToTargetComponent.put("Comp2", "be");
    sourceToTargetComponent.put("Comp3", "smp");
    final Map<String, List<String>> dependencies =
        componentQuestionnaireData.populateDependencies(VSP_ID, VERSION01, sourceToTargetComponent);

    List<String> expectedTargets =  new ArrayList<String>();
    expectedTargets.add("be"); expectedTargets.add("smp");

    Assert.assertEquals(dependencies.get("fe"), expectedTargets);



  }
*/
}
