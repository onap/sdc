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

package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDependencyModelDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.OrchestrationUtil;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.types.OrchestrationTemplateActionResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.vendorsoftwareproduct.utils.ZipFileUtils;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

public class OrchestrationTemplateCandidateManagerImplTest {
  private static final String VSP_ID = "vspId";
  private static final Version VERSION01 = new Version("versionId");

  private static final String COMPONENT_ORIG_ID_1 = "Component_Pd_Server_Id_Orig";
  private static final String COMPONENT_ORIG_ID_2 = "Component_Sm_Server_Id_Orig";
  private static final String COMPONENT_ORIG_ID_3 = "Component_Oam_Server_Id_Orig";
  private static final String COMPONENT_ORIG_ID_4 = "Component_Ps_Server_Id_Orig";

  private static final String COMPONENT_NEW_ID_1 = "Component_Pd_Server_Id_New";
  private static final String COMPONENT_NEW_ID_2 = "Component_Sm_Server_Id_New";
  private static final String COMPONENT_NEW_ID_3 = "Component_Oam_Server_Id_New";
  private static final String COMPONENT_NEW_ID_4 = "Component_Ps_Server_Id_New";

  private static final String COMPONENT_NAME_1 = "pd_server";
  private static final String COMPONENT_NAME_2 = "sm_server";
  private static final String COMPONENT_NAME_3 = "oam_server";
  private static final String COMPONENT_NAME_4 = "ps_server";

  @Mock
  private VendorSoftwareProductInfoDao vspInfoDaoMock;
  @Mock
  private CandidateService candidateServiceMock;
  @Mock
  private OrchestrationTemplateDao orchestrationTemplateDaoMock;
  @Mock
  private ComponentDependencyModelDao componentDependencyModelDaoMock;
  @Mock
  private ComponentDao componentDaoMock;

  @InjectMocks
  private OrchestrationTemplateCandidateManagerImpl candidateManager;

  @InjectMocks
  private OrchestrationUtil orchestrationUtil;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void resetMocks() {
    Mockito.reset(vspInfoDaoMock);
    Mockito.reset(candidateServiceMock);
    Mockito.reset(orchestrationTemplateDaoMock);
    Mockito.reset(componentDependencyModelDaoMock);
    Mockito.reset(componentDaoMock);
    candidateManager = null;
    orchestrationUtil = null;
  }

  @Test
  public void testProcessEmptyUpload() {
    OrchestrationTemplateCandidateData orchTemplate =
        new OrchestrationTemplateCandidateData();
    orchTemplate
        .setContentData(ByteBuffer.wrap(FileUtils.toByteArray(new ZipFileUtils().getZipInputStream
            ("/vspmanager/zips/emptyComposition.zip"))));
    orchTemplate.setFilesDataStructure("{\n" +
        "  \"modules\": [\n" +
        "    {\n" +
        "      \"isBase\": false,\n" +
        "      \"yaml\": \"ep-jsa_net.yaml\"\n" +
        "    }\n" +
        "  ]\n" +
        "}");
    doReturn(Optional.of(orchTemplate))
        .when(candidateServiceMock).getOrchestrationTemplateCandidate(any(), any());

    doReturn(new VspDetails(VSP_ID, VERSION01))
        .when(vspInfoDaoMock).get(any());
    doReturn(null)
        .when(orchestrationTemplateDaoMock).getInfo(any(), any());

    doReturn("{}").when(candidateServiceMock).createManifest(any(), any());
    doReturn(Optional.empty()).when(candidateServiceMock)
        .fetchZipFileByteArrayInputStream(any(), any(), any(),
            eq(OnboardingTypesEnum.ZIP), any());


    OrchestrationTemplateActionResponse response =
        candidateManager.process(VSP_ID, VERSION01);

    assertNotNull(response);
  }

  @Test
  public void testUpdateVspComponentDependenciesHeatReuploadMoreComponents() {
    Collection<ComponentDependencyModelEntity> existingComponentsDependencies =
        getExistingComponentDependencies();
    Collection<ComponentEntity> componentListWithMoreComponentsInHeat =
        getComponentListWithMoreComponentsInHeat();

    doReturn(componentListWithMoreComponentsInHeat).when(componentDaoMock).list(any());
    Map<String, String> componentIdNameInfoBeforeProcess = getVspInitComponentIdNameInfo();
    orchestrationUtil.updateVspComponentDependencies(VSP_ID, VERSION01,
        componentIdNameInfoBeforeProcess, existingComponentsDependencies);
    Mockito.verify(componentDependencyModelDaoMock, Mockito.times(2)).create(any());
  }

  @Test
  public void testUpdateVspComponentDependenciesHeatReuploadLessComponents() {
    Collection<ComponentDependencyModelEntity> existingComponentsDependencies =
        getExistingComponentDependencies();
    Collection<ComponentEntity> componentListWithLessComponentsInHeat =
        getComponentListWithLessComponentsInHeat();
    doReturn(componentListWithLessComponentsInHeat).when(componentDaoMock).list(any());
    Map<String, String> componentIdNameInfoBeforeProcess = getVspInitComponentIdNameInfo();
    orchestrationUtil.updateVspComponentDependencies(VSP_ID, VERSION01,
        componentIdNameInfoBeforeProcess, existingComponentsDependencies);
    Mockito.verify(componentDependencyModelDaoMock, Mockito.times(1)).create(any());
  }

  @Test
  public void testUpdateVspComponentDependenciesHeatReuploadSameComponents() {
    Collection<ComponentDependencyModelEntity> existingComponentsDependencies =
        getExistingComponentDependencies();
    Collection<ComponentEntity> componentListWithSameComponentsInHeat =
        getComponentListWithSameComponentsInHeat();
    doReturn(componentListWithSameComponentsInHeat).when(componentDaoMock).list(any());
    Map<String, String> componentIdNameInfoBeforeProcess = getVspInitComponentIdNameInfo();
    orchestrationUtil.updateVspComponentDependencies(VSP_ID, VERSION01,
        componentIdNameInfoBeforeProcess, existingComponentsDependencies);
    Mockito.verify(componentDependencyModelDaoMock, Mockito.times(2)).create(any());
  }

  @Test
  public void testUpdateVspComponentDependenciesHeatReuploadNoComponents() {
    Collection<ComponentDependencyModelEntity> existingComponentsDependencies =
        getExistingComponentDependencies();
    Collection<ComponentEntity> componentListWithMoreComponentsInHeat =
        new ArrayList<>();
    doReturn(componentListWithMoreComponentsInHeat).when(componentDaoMock).list(any());
    Map<String, String> componentIdNameInfoBeforeProcess = getVspInitComponentIdNameInfo();
    orchestrationUtil.updateVspComponentDependencies(VSP_ID, VERSION01,
        componentIdNameInfoBeforeProcess, existingComponentsDependencies);
    Mockito.verify(componentDependencyModelDaoMock, Mockito.times(0)).create(any());
  }

  @Test
  public void testVspComponentIdNameInfoNoComponents() {
    Collection<ComponentEntity> initialVspComponents = new ArrayList<>();
    doReturn(initialVspComponents).when(componentDaoMock).list(any());
    Map<String, String> vspComponentIdNameInfo =
        orchestrationUtil.getVspComponentIdNameInfo(VSP_ID, VERSION01);
    assertEquals(vspComponentIdNameInfo.size(), 0);
  }

  @Test
  public void testVspComponentIdNameInfo() {
    doReturn(getInitialVspComponents()).when(componentDaoMock).list(any());
    Map<String, String> vspComponentIdNameInfo =
        orchestrationUtil.getVspComponentIdNameInfo(VSP_ID, VERSION01);

    assertEquals(vspComponentIdNameInfo.size(), 3);
    assertNotNull(vspComponentIdNameInfo.get(COMPONENT_ORIG_ID_1));
    assertNotNull(vspComponentIdNameInfo.get(COMPONENT_ORIG_ID_2));
    assertNotNull(vspComponentIdNameInfo.get(COMPONENT_ORIG_ID_3));
  }

  @Test
  public void testVspComponentIdNameInfoEmptyCompositionData() {
    Collection<ComponentEntity> initialVspComponents = getInitialVspComponents();
    ComponentEntity componentEntity = new ComponentEntity();
    componentEntity.setId(COMPONENT_ORIG_ID_4);
    initialVspComponents.add(componentEntity);

    doReturn(initialVspComponents).when(componentDaoMock).list(any());
    Map<String, String> vspComponentIdNameInfo =
        orchestrationUtil.getVspComponentIdNameInfo(VSP_ID, VERSION01);

    assertEquals(vspComponentIdNameInfo.size(), 3);
    assertNotNull(vspComponentIdNameInfo.get(COMPONENT_ORIG_ID_1));
    assertNotNull(vspComponentIdNameInfo.get(COMPONENT_ORIG_ID_2));
    assertNotNull(vspComponentIdNameInfo.get(COMPONENT_ORIG_ID_3));
    Assert.assertNull(vspComponentIdNameInfo.get(COMPONENT_ORIG_ID_4));
  }

  @Test
  public void testGetComponentDependenciesBeforeDeleteInvalid() {
    Collection<ComponentDependencyModelEntity> componentDependenciesBeforeDelete =
        orchestrationUtil.getComponentDependenciesBeforeDelete(null, null);
    assertEquals(componentDependenciesBeforeDelete.size(), 0);
  }

  @Test
  public void testGetComponentDependenciesBeforeDeleteValid() {
    Collection<ComponentDependencyModelEntity> existingComponentsDependencies =
        getExistingComponentDependencies();
    doReturn(existingComponentsDependencies).when(componentDependencyModelDaoMock).list(any());
    Collection<ComponentDependencyModelEntity> componentDependenciesBeforeDelete =
        orchestrationUtil.getComponentDependenciesBeforeDelete(VSP_ID, VERSION01);
    assertEquals(componentDependenciesBeforeDelete.size(), 2);
  }

  @Test
  public void testGetFileDataStructure() {
    Optional<String> jsonFileDataStructure = Optional.of("{\n" +
        "  \"modules\": [\n" +
        "    {\n" +
        "      \"yaml\": \"hot-mog-0108-bs1271.yml\",\n" +
        "      \"env\": \"hot-mog-0108-bs1271.env\"\n" +
        "    }\n" +
        "  ],\n" +
        "  \"unassigned\": [],\n" +
        "  \"artifacts\": [],\n" +
        "  \"nested\": []\n" +
        "}");
    Optional<FilesDataStructure> filesDataStructureOptional = Optional.of(JsonUtil.json2Object
        (jsonFileDataStructure.get(), FilesDataStructure.class));
    doReturn(filesDataStructureOptional).when(candidateServiceMock)
        .getOrchestrationTemplateCandidateFileDataStructure(VSP_ID, VERSION01);
    Optional<FilesDataStructure> filesDataStructure = candidateManager.getFilesDataStructure(VSP_ID,
        VERSION01);
    assertNotNull(filesDataStructure);
    assertEquals(filesDataStructureOptional.get().getModules().size(), filesDataStructure.get()
        .getModules().size());
    assertEquals(filesDataStructureOptional.get().getModules().get(0).getName(),
        filesDataStructure.get().getModules().get(0).getName());
  }

  @Test
  public void testAbort() {

    Mockito.doNothing().when(candidateServiceMock).deleteOrchestrationTemplateCandidate(VSP_ID,
        VERSION01);
    candidateManager.abort(VSP_ID, VERSION01);

    Mockito.verify(candidateServiceMock, Mockito.times(1)).deleteOrchestrationTemplateCandidate
        (VSP_ID, VERSION01);
  }

  private Map<String, String> getVspInitComponentIdNameInfo() {
    Map<String, String> componentIdNameInfoBeforeProcess = new HashMap<>();
    componentIdNameInfoBeforeProcess.put(COMPONENT_ORIG_ID_1, COMPONENT_NAME_1);
    componentIdNameInfoBeforeProcess.put(COMPONENT_ORIG_ID_2, COMPONENT_NAME_2);
    componentIdNameInfoBeforeProcess.put(COMPONENT_ORIG_ID_3, COMPONENT_NAME_3);
    return componentIdNameInfoBeforeProcess;
  }

  private Collection<ComponentEntity> getInitialVspComponents() {
    Collection<ComponentEntity> vspComponents = new ArrayList<>();
    ComponentEntity component1 = createComponentEntity(COMPONENT_ORIG_ID_1, COMPONENT_NAME_1);
    ComponentEntity component2 = createComponentEntity(COMPONENT_ORIG_ID_2, COMPONENT_NAME_2);
    ComponentEntity component3 = createComponentEntity(COMPONENT_ORIG_ID_3, COMPONENT_NAME_3);
    vspComponents.add(component1);
    vspComponents.add(component2);
    vspComponents.add(component3);
    return vspComponents;
  }

  private Collection<ComponentEntity> getComponentListWithMoreComponentsInHeat() {
    Collection<ComponentEntity> vspComponents = getInitialVspComponents();
    createInitialComponentDependencies(vspComponents);
    ComponentEntity newComponent = createComponentEntity(COMPONENT_NEW_ID_4, COMPONENT_NAME_4);
    vspComponents.add(newComponent);
    return vspComponents;
  }

  private Collection<ComponentEntity> getComponentListWithLessComponentsInHeat() {
    Collection<ComponentEntity> vspComponents = getInitialVspComponents();
    for (Iterator<ComponentEntity> iterator = vspComponents.iterator(); iterator.hasNext(); ) {
      ComponentEntity componentEntity = iterator.next();
      switch (componentEntity.getComponentCompositionData().getName()) {
        case COMPONENT_NAME_1:
          iterator.remove();
          break;
        case COMPONENT_NAME_2:
          componentEntity.setId(COMPONENT_NEW_ID_2);
          break;
        case COMPONENT_NAME_3:
          componentEntity.setId(COMPONENT_NEW_ID_3);
          break;
      }
    }
    return vspComponents;
  }

  private Collection<ComponentEntity> getComponentListWithSameComponentsInHeat() {
    Collection<ComponentEntity> vspComponents = getInitialVspComponents();
    createInitialComponentDependencies(vspComponents);
    return vspComponents;
  }

  private Collection<ComponentDependencyModelEntity> getExistingComponentDependencies() {
    Collection<ComponentDependencyModelEntity> newComponents = new ArrayList<>();
    ComponentDependencyModelEntity entity =
        createComponentDependencyEntity(COMPONENT_ORIG_ID_1, COMPONENT_ORIG_ID_2);
    ComponentDependencyModelEntity entity2 =
        createComponentDependencyEntity(COMPONENT_ORIG_ID_2, COMPONENT_ORIG_ID_3);
    newComponents.add(entity);
    newComponents.add(entity2);
    return newComponents;
  }

  private ComponentEntity createComponentEntity(String componentId, String componentName) {
    ComponentEntity componentEntity = new ComponentEntity();
    componentEntity.setVspId(VSP_ID);
    componentEntity.setVersion(VERSION01);
    componentEntity.setId(componentId);
    ComponentData data = new ComponentData();
    data.setName(componentName);
    componentEntity.setComponentCompositionData(data);
    return componentEntity;
  }

  private ComponentDependencyModelEntity createComponentDependencyEntity(String sourceComponentId,
                                                                         String targetComponentId) {
    ComponentDependencyModelEntity componentDependency = new ComponentDependencyModelEntity();
    componentDependency.setVspId(VSP_ID);
    componentDependency.setVersion(VERSION01);
    componentDependency.setRelation("dependsOn");
    componentDependency.setSourceComponentId(sourceComponentId);
    componentDependency.setTargetComponentId(targetComponentId);
    return componentDependency;
  }

  private void createInitialComponentDependencies(Collection<ComponentEntity> vspComponents) {
    for (ComponentEntity componentEntity : vspComponents) {
      switch (componentEntity.getComponentCompositionData().getName()) {
        case COMPONENT_NAME_1:
          componentEntity.setId(COMPONENT_NEW_ID_1);
          break;
        case COMPONENT_NAME_2:
          componentEntity.setId(COMPONENT_NEW_ID_2);
          break;
        case COMPONENT_NAME_3:
          componentEntity.setId(COMPONENT_NEW_ID_3);
          break;
      }
    }
  }
}