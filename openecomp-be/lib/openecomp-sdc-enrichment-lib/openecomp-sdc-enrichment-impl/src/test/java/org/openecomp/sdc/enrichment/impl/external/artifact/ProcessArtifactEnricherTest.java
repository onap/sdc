package org.openecomp.sdc.enrichment.impl.external.artifact;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.enrichment.types.ArtifactCategory;
import org.openecomp.core.model.dao.EnrichedServiceModelDao;
import org.openecomp.core.model.types.ServiceArtifact;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.enrichment.EnrichmentInfo;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessType;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.when;

public class ProcessArtifactEnricherTest {
  /*@Mock
  ProcessDao processDaoMock;
  @Mock
  EnrichedServiceModelDao enrichedServiceModelDaoMock;
  @Mock
  VendorSoftwareProductDao vendorSoftwareProductDaoMock;

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

    Collection<ComponentEntity> componentList = new ArrayList<ComponentEntity>();
    componentList.add(componentEntity);
    when(vendorSoftwareProductDaoMock.listComponents(vspId, version)).thenReturn(componentList);

    Collection<ProcessEntity> list = new ArrayList<ProcessEntity>();
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
