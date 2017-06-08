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

package org.openecomp.sdc.vendorsoftwareproduct;

public class VspHealTest {/*
  private static VendorSoftwareProductManager vendorSoftwareProductManager = null;
  //new VendorSoftwareProductManagerImpl();
  private VendorSoftwareProductManagerImplTest vendorSoftwareProductManagerTest =
      new VendorSoftwareProductManagerImplTest();
  private static OrchestrationTemplateCandidateDao orchestrationTemplateCandidateDataDao =
      OrchestrationTemplateCandidateDaoFactory.getInstance().createInterface();
  private static VendorSoftwareProductDao vendorSoftwareProductDao =
      VendorSoftwareProductDaoFactory.getInstance().createInterface();
  private static ComponentDao componentDao =
      ComponentDaoFactory.getInstance().createInterface();
  private static CompositionDataExtractor compositionDataExtractor =
      CompositionDataExtractorFactory.getInstance().createInterface();
  private static NetworkDao networkDao = NetworkDaoFactory.getInstance().createInterface();
  private static NicDao nicDao = NicDaoFactory.getInstance().createInterface();
  private static VspDetails vspDetails;
  private static final String USER = "vspTestUser1";
  public static final Version VERSION01 = new Version(0, 1);
  private static String vspId;
  private OrchestrationTemplateCandidateManager candidateManager;

  @BeforeTest
  private void init() {
    try {
      vspDetails = new VspDetails();
      vspDetails.setName("vspName_" + CommonMethods.nextUuId());
      vspDetails.setVendorName("vendor");
      vspId = vendorSoftwareProductManager.createVsp(vspDetails, USER).getId();

    } catch (Exception ignored) {
      System.out.println(ignored.getMessage());
    }
  }

  @Test
  public void shouldReturnEmptyFileDataStructureBeforeZipUpload() {
    Optional<FilesDataStructure> candidateFilesDataStructure = candidateManager
        .getFilesDataStructure(vspId, VERSION01, USER);
    Assert.assertNotNull(candidateFilesDataStructure);
    Assert.assertTrue(candidateFilesDataStructure.isPresent());

    checkFileDataStructureListsAreEmpty(candidateFilesDataStructure.get());
  }

  @Test(dependsOnMethods = "shouldReturnEmptyFileDataStructureBeforeZipUpload")
  public void shouldReturnFileDataStructureOnEmptyFileDataStructureInDB() {
    uploadAndProcessOrchestrationTemplate(vspId, USER, "/vspmanager/zips/emptyComposition.zip");

    orchestrationTemplateCandidateDataDao
        .deleteOrchestrationTemplateCandidateFileDataStructure(vspId, VERSION01);
    Assert.assertEquals(Optional.empty(), orchestrationTemplateCandidateDataDao
        .getOrchestrationTemplateCandidateFileDataStructure(vspId, VERSION01));

    Optional<FilesDataStructure> candidateFilesDataStructure = candidateManager
        .getFilesDataStructure(vspId, VERSION01, USER);
    Assert.assertNotNull(candidateFilesDataStructure);
    Assert.assertTrue(candidateFilesDataStructure.isPresent());
  }

  @Test(dependsOnMethods = "shouldReturnEmptyFileDataStructureBeforeZipUpload")
  public void shouldReturnEmptyFileDataStructureOnEmptyUpload() {
    try {
      uploadAndProcessOrchestrationTemplate(vspId, USER, "/vspmanager/zips/zipFileWithFolder.zip");
    } catch (Exception e) {
      Assert.assertEquals(e.getMessage(),
          "Failed to get orchestration template for VSP with id " + vspId);
    }
  }

  @Test(dependsOnMethods = {"shouldReturnEmptyFileDataStructureOnEmptyUpload"})
  public void shouldHealVspOnIsOldTrue() {
    vspDetails.setOldVersion(VersionHealingValues.True);
    vendorSoftwareProductDao.updateQuestionnaire(vspId, VERSION01, null);

    vendorSoftwareProductManager.heal(vspId, VERSION01, USER);

    VspQuestionnaireEntity questionnaire =
        vendorSoftwareProductDao.getQuestionnaire(vspId, VERSION01);

    Assert.assertNotNull(questionnaire.getQuestionnaireData());
  }

  @Test(dependsOnMethods = {"shouldHealVspOnIsOldTrue"})
  public void shouldHealNullQuestionnaire() {
    vendorSoftwareProductDao.updateQuestionnaire(vspId, VERSION01, null);
    vendorSoftwareProductManager.heal(vspId, VERSION01, USER);
    QuestionnaireResponse vspQuestionnaire =
        vendorSoftwareProductManager.getVspQuestionnaire(vspId, VERSION01, USER);

    Assert.assertNotNull(vspQuestionnaire.getData());
  }

  @Test(dependsOnMethods = {"shouldHealNullQuestionnaire"})
  public void shouldHealNullCompositionData() {
    uploadAndProcessOrchestrationTemplate(vspId, USER, "/vspmanager/zips/fullComposition.zip");

    Collection<ComponentEntity> componentEntitiesBeforeHeal =
        vendorSoftwareProductDao.listComponents(vspId, VERSION01);
    Collection<NetworkEntity> networkEntitiesBeforeHeal =
        vendorSoftwareProductDao.listNetworks(vspId, VERSION01);

    deleteCompositionData(vspId, VERSION01);

    vendorSoftwareProductManager.heal(vspId, VERSION01, USER);

    Collection<ComponentEntity> componentEntitiesAfterHeal =
        vendorSoftwareProductDao.listComponents(vspId, VERSION01);
    Collection<NetworkEntity> networkEntitiesAfterHeal =
        vendorSoftwareProductDao.listNetworks(vspId, VERSION01);

    checkCompositionDataIsHealed(componentEntitiesBeforeHeal, networkEntitiesBeforeHeal,
        componentEntitiesAfterHeal, networkEntitiesAfterHeal);
  }

  @Test(dependsOnMethods = {"shouldHealNullCompositionData"})
  public void shouldChangeComponentDisplayName() {
    uploadAndProcessOrchestrationTemplate(vspId, USER, "/vspmanager/zips/vCDN.zip");

    List<ComponentEntity> componentEntitiesBeforeHeal =
        (List<ComponentEntity>) vendorSoftwareProductDao.listComponents(vspId, VERSION01);
    Collection<ComponentEntity> componentsToHeal = new ArrayList<>();

    for (ComponentEntity component : componentEntitiesBeforeHeal) {
      changeComponentDisplayNameToOldVersion(component);
    }

    vendorSoftwareProductManager.heal(vspId, VERSION01, USER);

    List<ComponentEntity> componentEntitiesAfterHeal =
        (List<ComponentEntity>) vendorSoftwareProductDao.listComponents(vspId, VERSION01);

    assertComponentdisplayNameAsExpected(componentEntitiesBeforeHeal, componentEntitiesAfterHeal);
  }

  private void assertComponentdisplayNameAsExpected(
      List<ComponentEntity> componentEntitiesBeforeHeal,
      List<ComponentEntity> componentEntitiesAfterHeal) {
    ComponentEntity componentBefore = componentEntitiesBeforeHeal.get(0);
    ComponentEntity componentAfter = componentEntitiesAfterHeal.get(0);
    Assert.assertNotEquals(componentBefore, componentAfter);

    ComponentData componsitionDataBefore = componentBefore.getComponentCompositionData();
    ComponentData compositionDataAfter = componentAfter.getComponentCompositionData();
    Assert.assertTrue(
        componsitionDataBefore.getDisplayName().contains(compositionDataAfter.getDisplayName()));
    Assert.assertEquals(
        compositionDataExtractor.getComponentDisplayName(componsitionDataBefore.getName()),
        compositionDataAfter.getDisplayName());
  }

  private void changeComponentDisplayNameToOldVersion(ComponentEntity component) {
    ComponentData componentData = component.getComponentCompositionData();
    componentData.setDisplayName(componentData.getName());
    componentData.setVfcCode(componentData.getDisplayName());
    component.setComponentCompositionData(componentData);
    vendorSoftwareProductDao.updateComponent(component);
  }


  private void uploadAndProcessOrchestrationTemplate(String vspId, String user,
                                                     String filePath) {

    candidateManager.upload(vspId, VERSION01,
        vendorSoftwareProductManagerTest
            .getFileInputStream(filePath), user);
    candidateManager.process(vspId, VERSION01, user);
  }

  private void deleteCompositionData(String vspId, Version version) {
    componentDao.deleteAll(vspId, version);
    networkDao.deleteAll(vspId, version);
    nicDao.deleteByVspId(vspId, version);
  }

  private void checkCompositionDataIsHealed(Collection<ComponentEntity> componentEntitiesBeforeHeal,
                                            Collection<NetworkEntity> networkEntitiesBeforeHeal,
                                            Collection<ComponentEntity> componentEntitiesAfterHeal,
                                            Collection<NetworkEntity> networkEntitiesAfterHeal) {
    Assert.assertNotNull(componentEntitiesAfterHeal);
    Assert.assertNotNull(networkEntitiesAfterHeal);

    Assert.assertEquals(componentEntitiesBeforeHeal.size(), componentEntitiesAfterHeal.size());
    Assert.assertEquals(networkEntitiesBeforeHeal.size(), networkEntitiesAfterHeal.size());
  }


  private void checkFileDataStructureListsAreEmpty(FilesDataStructure filesDataStructure) {
    Assert.assertEquals(filesDataStructure.getArtifacts().size(), 0);
    Assert.assertEquals(filesDataStructure.getModules().size(), 0);
    Assert.assertEquals(filesDataStructure.getNested().size(), 0);
    Assert.assertEquals(filesDataStructure.getUnassigned().size(), 0);
  }

*/
}
