/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.enrichment.impl.external.artifact;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.enrichment.types.ArtifactCategory;
import org.openecomp.core.enrichment.types.ArtifactType;
import org.openecomp.core.model.dao.EnrichedServiceModelDao;
import org.openecomp.core.model.types.ServiceArtifact;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.enrichment.EnrichmentInfo;
import org.openecomp.sdc.vendorsoftwareproduct.dao.MibDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.MibEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;


/**
 * @author shiria
 * @since November 06, 2016.
 */

public class MonitoringMibEnricherTest {
  @Mock
  private MibDao mibDaoMock;
  @Mock
  private EnrichedServiceModelDao enrichedServiceModelDaoMock;
  @Mock
  private VendorSoftwareProductDao vendorSoftwareProductDaoMock;
  @Mock
  private ComponentDao componentDaoMock;

  @InjectMocks
  private MonitoringMibEnricher monitoringMibEnricher;


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
    setMockToEnrichComponent(vspId, componentId, version);
    monitoringMibEnricher.enrichComponent(componentEntity, vspId, version);

    String componentName = componentEntity.getComponentCompositionData().getName();

    ArgumentCaptor<ServiceArtifact> expectedServiceArtifact =
        ArgumentCaptor.forClass(ServiceArtifact.class);
    Mockito.verify(enrichedServiceModelDaoMock, atLeastOnce())
        .storeExternalArtifact(expectedServiceArtifact.capture());
    Assert
        .assertEquals(expectedServiceArtifact.getValue().getName().startsWith(componentName), true);
    Assert.assertEquals(expectedServiceArtifact.getValue().getName(),
        componentName + File.separator + ArtifactCategory.DEPLOYMENT.getDisplayName() +
            File.separator + ArtifactType.SNMP_POLL + File.separator + "mib1.yml");

  }

  @Test
  public void testEnrich() throws Exception {
    EnrichmentInfo enrichmentInfo = new EnrichmentInfo();
    Version version = new Version();
    version.setMajor(1);
    version.setMinor(0);
    String vspId = "123";
    enrichmentInfo.setKey(vspId);
    enrichmentInfo.setVersion(version);
    String componentId1 = "1111111111";
    String componentId2 = "2222222222";


    Collection<ComponentEntity> returnedComponents = new ArrayList<>();
    returnedComponents.add(getComponentEntity(vspId, version, componentId1));
    returnedComponents.add(getComponentEntity(vspId, version, componentId2));

    Mockito.when(componentDaoMock.list(anyObject()))
        .thenReturn(returnedComponents);
    setMockToEnrichComponent(vspId, componentId1, version);

    monitoringMibEnricher.enrich(enrichmentInfo);
    Mockito.verify(enrichedServiceModelDaoMock, times(8)).storeExternalArtifact(anyObject());

  }

  private void setMockToEnrichComponent(String vspId, String componentId, Version version) {
    MibEntity returnedArtifact = new MibEntity();
    returnedArtifact.setVspId(vspId);
    returnedArtifact.setVersion(version);
    returnedArtifact.setComponentId(componentId);
    returnedArtifact.setType(ArtifactType.SNMP_POLL);
    returnedArtifact.setArtifactName("mib.zip");
    returnedArtifact.setArtifact(getMibByteBuffer("/mock/enrichMib/MIB.zip"));

    Mockito.when(mibDaoMock.getByType(anyObject()))
        .thenReturn(Optional.of(returnedArtifact));
    Mockito.doNothing().when(enrichedServiceModelDaoMock).storeExternalArtifact(anyObject());
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
    InputStream mibFile = FileUtils.getFileInputStream(this.getClass().getResource(fileName));
    byte[] mibBytes = FileUtils.toByteArray(mibFile);
    return ByteBuffer.wrap(mibBytes);
  }

}
