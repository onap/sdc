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

import org.apache.commons.collections4.CollectionUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.enrichment.types.ArtifactCategory;
import org.openecomp.core.enrichment.types.MonitoringUploadType;
import org.openecomp.core.model.dao.EnrichedServiceModelDao;
import org.openecomp.core.model.types.ServiceArtifact;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.enrichment.EnrichmentInfo;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentArtifactDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentMonitoringUploadEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;


public class MonitoringMibEnricherTest {
  @Mock
  private ComponentArtifactDao componentArtifactDaoMock;
  @Mock
  private EnrichedServiceModelDao enrichedServiceModelDaoMock;
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
    String componentId = "1";
    Version version = new Version();
    version.setMajor(1);
    version.setMinor(0);

    ComponentEntity componentEntity = getComponentEntity(vspId, version, componentId, vspId +
        "enrichMib_server");
    setMockToEnrichComponent(vspId, version, componentId);
    String componentName = componentEntity.getComponentCompositionData().getName();
    String unifiedComponentName =
        ToscaNodeType.ABSTRACT_NODE_TYPE_PREFIX + DataModelUtil.getNamespaceSuffix(componentName);

    ArgumentCaptor<ServiceArtifact> expectedServiceArtifact =
        ArgumentCaptor.forClass(ServiceArtifact.class);
    monitoringMibEnricher.enrichComponent(vspId, version, componentEntity,
        Stream.of(unifiedComponentName).collect(Collectors.toSet()));

    Mockito.verify(enrichedServiceModelDaoMock, atLeastOnce())
        .storeExternalArtifact(expectedServiceArtifact.capture());
    Assert.assertEquals(expectedServiceArtifact.getValue().getName()
        .startsWith(unifiedComponentName), true);
    Assert.assertEquals(expectedServiceArtifact.getValue().getName(),
        unifiedComponentName + File.separator + ArtifactCategory.DEPLOYMENT.getDisplayName() +
            File.separator + MonitoringUploadType.VES_EVENTS + File.separator + "mib1.yml");

  }

  @Test
  public void testEnrichmentOfTwoComponentsFromSameType() throws IOException {
    EnrichmentInfo enrichmentInfo = new EnrichmentInfo();
    Version version = new Version();
    version.setMajor(1);
    version.setMinor(0);
    String vspId = "123";
    enrichmentInfo.setKey(vspId);
    enrichmentInfo.setVersion(version);
    String componentId1 = "1";
    String componentId2 = "2";
    String abstType = "org.openecomp.resource.abstract.nodes.pd_server";
    String abstType1 = "org.openecomp.resource.abstract.nodes.pd_server_1";

    List<ComponentEntity> returnedComponents = new ArrayList<>();
    returnedComponents.add(getComponentEntity(vspId, version, componentId1,
        "pd_server"));
    returnedComponents.add(getComponentEntity(vspId, version, componentId2,
        "pd_server"));
    Mockito.when(componentDaoMock.list(anyObject()))
        .thenReturn(returnedComponents);
    setMockToEnrichComponent(vspId, version, componentId1);

    ToscaServiceModel mockServiceModel =
        getMockServiceModel("/mock/enrichMib/toscaModel/twoAbstractNodesFromSameType");

    ArgumentCaptor<ServiceArtifact> expectedServiceArtifact =
        ArgumentCaptor.forClass(ServiceArtifact.class);
    monitoringMibEnricher.enrich(enrichmentInfo, mockServiceModel);

    Mockito.verify(enrichedServiceModelDaoMock, times(24))
        .storeExternalArtifact(expectedServiceArtifact.capture());

    Set<String> prefixes = getAllMibDirectoryPrefixes(expectedServiceArtifact.getAllValues());

    validateExpectedAbstractTypes(Stream.of(abstType, abstType1).collect(Collectors.toSet()), prefixes);
  }

  private void validateExpectedAbstractTypes(Set<String> expectedAbstractTypes,
                                             Set<String> prefixes) {
    for(String abstType : expectedAbstractTypes){
      Assert.assertTrue(prefixes.contains(abstType));
    }
  }

  private void setMockToEnrichComponent(String vspId, Version version, String componentId) {
    ComponentMonitoringUploadEntity returnedArtifact = new ComponentMonitoringUploadEntity();
    returnedArtifact.setVspId(vspId);
    returnedArtifact.setVersion(version);
    returnedArtifact.setComponentId(componentId);
    returnedArtifact.setType(MonitoringUploadType.SNMP_POLL);
    returnedArtifact.setArtifactName("mib.zip");
    returnedArtifact.setArtifact(getMibByteBuffer("/mock/enrichMib/MIB.zip"));

    Mockito.when(componentArtifactDaoMock.getByType(anyObject()))
        .thenReturn(Optional.of(returnedArtifact));
    Mockito.doNothing().when(enrichedServiceModelDaoMock).storeExternalArtifact(anyObject());
  }

  private ComponentEntity getComponentEntity(String vspId,
                                             Version version,
                                             String componentId,
                                             String componentNameSuffix) {
    ComponentEntity componentEntity = new ComponentEntity();
    componentEntity.setId(componentId);
    componentEntity.setVspId(vspId);
    componentEntity.setVersion(version);

    String compositionData = "{\n" +
        "  \"name\": \"org.openecomp.resource.vfc.nodes.heat." + componentNameSuffix + "\",\n" +
        "  \"displayName\": \"" + componentNameSuffix + "\"\n" +
        "}";
    componentEntity.setCompositionData(compositionData);
    return componentEntity;
  }

  private ByteBuffer getMibByteBuffer(String fileName) {
    byte[] mibBytes = FileUtils.readViaInputStream(this.getClass().getResource(fileName),
        FileUtils::toByteArray);
    return ByteBuffer.wrap(mibBytes);
  }

  private ToscaServiceModel getMockServiceModel(String serviceTemplatesDirectory)
      throws IOException {
    File directory = new File(this.getClass().getResource(serviceTemplatesDirectory).getFile());
    ToscaServiceModel serviceModel = new ToscaServiceModel();
    Map<String, ServiceTemplate> serviceTemplates = new HashMap<>();

    for (final File serviceTemplateFile : directory.listFiles()) {
      byte[] content = FileUtils
          .readViaInputStream(this.getClass().getResource(serviceTemplatesDirectory + File
                  .separator + serviceTemplateFile.getName()),
              FileUtils::toByteArray);
      ServiceTemplate serviceTemplate =
          new YamlUtil().yamlToObject(new String(content), ServiceTemplate.class);
      serviceTemplates.put(ToscaUtil.getServiceTemplateFileName(serviceTemplate), serviceTemplate);
    }

    serviceModel.setServiceTemplates(serviceTemplates);
    return serviceModel;
  }

  private Set<String> getAllMibDirectoryPrefixes(List<ServiceArtifact> serviceArtifacts) {
    if(CollectionUtils.isEmpty(serviceArtifacts)){
      return new HashSet<>();
    }

    Set<String> prefixes = new HashSet<>();
    for(ServiceArtifact serviceArtifact : serviceArtifacts){
      String absolutePath = serviceArtifact.getName();
      prefixes.add(absolutePath.split(Pattern.quote(File.separator))[0]);
    }

    return prefixes;
  }

}
