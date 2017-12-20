package org.openecomp.sdc.enrichment.impl.external.artifact;

public class ProcessArtifactEnricherTest {
  /*@Mock
  ProcessDao processDaoMock;
  @Mock
  EnrichedServiceModelDao enrichedServiceModelDaoMock;
  @Mock
  ComponentDao componentDaoMock;
  @InjectMocks
  ProcessArtifactEnricher processArtifactEnricher;

  @BeforeMethod(alwaysRun = true)
  public void injectDoubles() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testEnrichComponent() throws Exception {
    String vspId = "123";
    String componentId = "1111111111";
    Version version = new Version();
    version.setMajor(1);
    version.setMinor(0);

    ComponentEntity componentEntity = getComponentEntity(vspId, version, componentId);

    ProcessEntity entity = new ProcessEntity(vspId, version, componentId, null);
    ProcessEntity processEntity = new ProcessEntity();
    processEntity.setType(ProcessType.Lifecycle_Operations);
    processEntity.setVspId(vspId);
    processEntity.setVersion(version);
    processEntity.setComponentId(componentId);
    processEntity.setArtifactName("artifact_1kb.txt");
    processEntity.setArtifact(getMibByteBuffer("/mock/enrichProcess/artifact_1kb.txt"));

    Collection<ComponentEntity> componentList = new ArrayList<>();
    componentList.add(componentEntity);
    when(componentDaoMock.list(anyObject())).thenReturn(componentList);

    Collection<ProcessEntity> list = new ArrayList<>();
    list.add(processEntity);
    when(processDaoMock.list(entity)).thenReturn(list);

    when(processDaoMock.get(anyObject())).thenReturn(processEntity);

    EnrichmentInfo info = new EnrichmentInfo();
    info.setVersion(version);
    info.setKey(vspId);
    processArtifactEnricher.enrich(info);

    String componentName = componentEntity.getComponentCompositionData().getName();

    ArgumentCaptor<ServiceArtifact> expectedServiceArtifact =
        ArgumentCaptor.forClass(ServiceArtifact.class);
    Mockito.verify(enrichedServiceModelDaoMock, atLeastOnce())
        .storeExternalArtifact(expectedServiceArtifact.capture());
    Assert
        .assertEquals(expectedServiceArtifact.getValue().getName().startsWith(componentName), true);
    Assert.assertEquals(expectedServiceArtifact.getValue().getName(),
        componentName + File.separator + ArtifactCategory.DEPLOYMENT.getDisplayName() +
            File.separator + "Lifecycle Operations" + File.separator + "artifact_1kb.txt");

  }

  private ComponentEntity getComponentEntity(String vspId, Version version, String componentId) {
    ComponentEntity componentEntity = new ComponentEntity();
    componentEntity.setId(componentId);
    componentEntity.setVspId(vspId);
    componentEntity.setVersion(version);

    String componentName = vspId + "enrichMib_server";
    String compositionData = "{\n" +
        "  \"name\": \"org.openecomp.resource.vfc.nodes.heat." + componentName + "\",\n" +
        "  \"displayName\": \"" + componentName + "\"\n" +
        "}";
    componentEntity.setCompositionData(compositionData);
    return componentEntity;
  }

  private ByteBuffer getMibByteBuffer(String fileName) {
    byte[] mibBytes = FileUtils.readViaInputStream(this.getClass().getResource(fileName),
        stream -> FileUtils.toByteArray(stream));
    return ByteBuffer.wrap(mibBytes);
  }*/
}
