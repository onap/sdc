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

package org.openecomp.sdc.enrichment.impl.external.artifact;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessType;
import org.openecomp.sdc.versioning.dao.types.Version;

public class ProcessArtifactEnricherTest {
  @Mock
  ProcessDao processDaoMock;
  @Mock
  EnrichedServiceModelDao enrichedServiceModelDaoMock;
  @Mock
  ComponentDao componentDaoMock;
  @InjectMocks
  ProcessArtifactEnricher processArtifactEnricher;

  @Before
  public void injectDoubles() {
    MockitoAnnotations.openMocks(this);
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
    when(componentDaoMock.list(any())).thenReturn(componentList);

    Collection<ProcessEntity> list = new ArrayList<>();
    list.add(processEntity);
    when(processDaoMock.list(entity)).thenReturn(list);

    when(processDaoMock.getArtifact(any())).thenReturn(processEntity);

    EnrichmentInfo info = new EnrichmentInfo();
    info.setVersion(version);
    info.setKey(vspId);
    processArtifactEnricher.enrich(info, null);

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

  @Test
  public void testEnrichComponentArtifactNameIsNull() throws Exception {
    String vspId = "123";
    String componentId = "1111111111";
    Version version = new Version();
    version.setMajor(1);
    version.setMinor(0);

    ComponentEntity componentEntity = getComponentEntity(vspId, version, componentId);

    ProcessEntity entity = new ProcessEntity(vspId, version, componentId, null);
    ProcessEntity processEntity = new ProcessEntity();
    processEntity.setType(ProcessType.Other);
    processEntity.setVspId(vspId);
    processEntity.setVersion(version);
    processEntity.setComponentId(componentId);
    processEntity.setArtifactName("artifact_1kb.txt");
    processEntity.setArtifact(getMibByteBuffer("/mock/enrichProcess/artifact_1kb.txt"));

    Collection<ComponentEntity> componentList = new ArrayList<>();
    componentList.add(componentEntity);
    when(componentDaoMock.list(any())).thenReturn(componentList);

    Collection<ProcessEntity> list = new ArrayList<>();
    list.add(processEntity);
    when(processDaoMock.list(entity)).thenReturn(list);

    when(processDaoMock.getArtifact(any())).thenReturn(processEntity);

    EnrichmentInfo info = new EnrichmentInfo();
    info.setVersion(version);
    info.setKey(vspId);
    processArtifactEnricher.enrich(info, null);

    String componentName = componentEntity.getComponentCompositionData().getName();

    ArgumentCaptor<ServiceArtifact> expectedServiceArtifact =
        ArgumentCaptor.forClass(ServiceArtifact.class);
    Mockito.verify(enrichedServiceModelDaoMock, never())
        .storeExternalArtifact(expectedServiceArtifact.capture());
  }

  @Test
  public void testEnrichComponentProcessEntityIsNull() throws Exception {
    String vspId = "123";
    String componentId = "1111111111";
    Version version = new Version();
    version.setMajor(1);
    version.setMinor(0);

    ComponentEntity componentEntity = getComponentEntity(vspId, version, componentId);

    ProcessEntity entity = new ProcessEntity(vspId, version, componentId, null);
    ProcessEntity processEntity = new ProcessEntity();
    processEntity.setType(ProcessType.Other);
    processEntity.setVspId(vspId);
    processEntity.setVersion(version);
    processEntity.setComponentId(componentId);
    processEntity.setArtifactName("artifact_1kb.txt");
    processEntity.setArtifact(getMibByteBuffer("/mock/enrichProcess/artifact_1kb.txt"));

    Collection<ComponentEntity> componentList = new ArrayList<>();
    componentList.add(componentEntity);
    when(componentDaoMock.list(any())).thenReturn(componentList);

    Collection<ProcessEntity> list = new ArrayList<>();
    list.add(processEntity);
    when(processDaoMock.list(entity)).thenReturn(list);

    when(processDaoMock.getArtifact(any())).thenReturn(null);

    EnrichmentInfo info = new EnrichmentInfo();
    info.setVersion(version);
    info.setKey(vspId);
    processArtifactEnricher.enrich(info, null);

    ArgumentCaptor<ServiceArtifact> expectedServiceArtifact =
        ArgumentCaptor.forClass(ServiceArtifact.class);
    Mockito.verify(enrichedServiceModelDaoMock, never())
        .storeExternalArtifact(expectedServiceArtifact.capture());
  }

  @Test
  public void testEnrichComponentNotALifecycleOperations() throws Exception {
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
    processEntity.setArtifactName(null);
    processEntity.setArtifact(getMibByteBuffer("/mock/enrichProcess/artifact_1kb.txt"));

    Collection<ComponentEntity> componentList = new ArrayList<>();
    componentList.add(componentEntity);
    when(componentDaoMock.list(any())).thenReturn(componentList);

    Collection<ProcessEntity> list = new ArrayList<>();
    list.add(processEntity);
    when(processDaoMock.list(entity)).thenReturn(list);

    when(processDaoMock.getArtifact(any())).thenReturn(processEntity);

    EnrichmentInfo info = new EnrichmentInfo();
    info.setVersion(version);
    info.setKey(vspId);
    processArtifactEnricher.enrich(info, null);

    ArgumentCaptor<ServiceArtifact> expectedServiceArtifact =
        ArgumentCaptor.forClass(ServiceArtifact.class);
    Mockito.verify(enrichedServiceModelDaoMock, never())
        .storeExternalArtifact(expectedServiceArtifact.capture());
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
  }
}
