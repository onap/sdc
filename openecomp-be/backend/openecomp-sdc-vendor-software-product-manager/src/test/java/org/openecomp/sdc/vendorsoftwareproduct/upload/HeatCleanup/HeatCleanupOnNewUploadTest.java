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

package org.openecomp.sdc.vendorsoftwareproduct.upload.HeatCleanup;

public class HeatCleanupOnNewUploadTest {/*
  private static final String USER1 = "vspTestUser1";

  private static final VendorSoftwareProductManager
      vendorSoftwareProductManager = new VendorSoftwareProductManagerImpl();
  private static final VendorSoftwareProductDao vendorSoftwareProductDao =
      VendorSoftwareProductDaoFactory
          .getInstance().createInterface();
  private static final ServiceModelDao serviceModelDao =
      ServiceModelDaoFactory.getInstance().createInterface();

  private static String vspId = null;
  private static Version vspActiveVersion = null;

  private static void validateUploadContentExistence(boolean exist) {
    UploadDataEntity uploadDataEntity =
        vendorSoftwareProductDao.getUploadData(new UploadDataEntity(vspId, vspActiveVersion));
    Assert.assertTrue((uploadDataEntity.getContentData() != null) == exist);
    Assert.assertTrue((uploadDataEntity.getInfo() != null) == exist);
    Assert.assertTrue((uploadDataEntity.getPackageName() != null) == exist);
    Assert.assertTrue((uploadDataEntity.getPackageVersion() != null) == exist);
    //TODO: talio - delete enrich data on new upload
    //Assert.assertTrue((serviceModelDao.getServiceModel(vspId, vspActiveVersion) != null) == ex
  }

  private static void validateCompositionDataExistence(boolean exist) {
    Assert.assertTrue(CollectionUtils
        .isNotEmpty(vendorSoftwareProductDao.listNetworks(vspId, vspActiveVersion)) == exist);
    Assert.assertTrue(CollectionUtils
        .isNotEmpty(vendorSoftwareProductDao.listComponents(vspId, vspActiveVersion)) == exist);
    Assert.assertTrue(CollectionUtils
        .isNotEmpty(vendorSoftwareProductDao.listNicsByVsp(vspId, vspActiveVersion)) == exist);
  }

  private static InputStream getFileInputStream(String fileName) {
    URL url = HeatCleanupOnNewUploadTest.class.getResource(fileName);
    try {
      return url.openStream();
    } catch (IOException exception) {
      exception.printStackTrace();
      return null;
    }
  }

  @BeforeClass
  private void init() {
    UniqueValueUtil
        .deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
            "VSPTestEmpty");

    VspDetails vspDetails = vendorSoftwareProductManager.createVsp(VSPCommon
        .createVspDetails(null, null, "VSPTestEmpty", "Test-vsp-empty", "vendorName", "vlm1Id",
            "icon", "category", "subCategory", "123", null, VSPCommon.OnboardingMethod.HEAT.name()),
        USER1);
    vspId = vspDetails.getId();
    vspActiveVersion = vspDetails.getVersion();
  }

  @Test
  public void testUploadWithComposition() {
    InputStream zis = getFileInputStream("/vspmanager/zips/fullComposition.zip");

    vendorSoftwareProductManager.upload(vspId, zis, USER1);
    OrchestrationTemplateActionResponse orchestrationTemplateActionResponse =
        vendorSoftwareProductManager.process(vspId, USER1);

    Assert.assertEquals(orchestrationTemplateActionResponse.getStatus(), UploadFileStatus.Success);
    Assert.assertTrue(MapUtils.isEmpty(MessageContainerUtil
        .getMessageByLevel(ErrorLevel.ERROR, orchestrationTemplateActionResponse.getErrors())));

    validateUploadContentExistence(true);
    validateCompositionDataExistence(true);
  }

  @Test(dependsOnMethods = {"testUploadWithComposition"})
  public void testProccesesMIBsDeletionAfterNewUpload() {
    InputStream zis1 = getFileInputStream("/vspmanager/zips/fullComposition.zip");
    InputStream zis2 = getFileInputStream("/vspmanager/zips/fullComposition.zip");
    InputStream mib = getFileInputStream("/vspmanager/zips/vDNS.zip");

    vendorSoftwareProductManager.upload(vspId, zis1, USER1);
    vendorSoftwareProductManager.process(vspId, USER1);
    List<ComponentEntity> components =
        (List<ComponentEntity>) vendorSoftwareProductDao.listComponents(vspId, vspActiveVersion);
    String componentId = components.get(0).getId();

    vendorSoftwareProductManager
        .upload(mib, "vDNS.zip", vspId, componentId, MonitoringUploadType.SNMP_TRAP, USER1);
    vendorSoftwareProductManager
        .createProcess(new ProcessEntity(vspId, vspActiveVersion, componentId, null), USER1);

    vendorSoftwareProductManager.upload(vspId, zis2, USER1);
    vendorSoftwareProductManager.process(vspId, USER1);
    Assert.assertTrue(
        vendorSoftwareProductManager.listFilenames(vspId, componentId, USER1).getSnmpTrap() ==
            null);
    Assert.assertTrue(CollectionUtils
        .isEmpty(vendorSoftwareProductDao.listProcesses(vspId, vspActiveVersion, componentId)));
  }

  @Test(dependsOnMethods = {"testProccesesMIBsDeletionAfterNewUpload"})
  public void testInvalidUploadAfterFullComposition() {
    InputStream zis = getFileInputStream("/vspmanager/zips/missingYml.zip");

    vendorSoftwareProductManager.upload(vspId, zis, USER1);
    OrchestrationTemplateActionResponse uploadFileResponse =
        vendorSoftwareProductManager.process(vspId, USER1);
    Assert.assertEquals(uploadFileResponse.getStatus(), UploadFileStatus.Failure);
    Assert.assertTrue(MapUtils.isNotEmpty(
        MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, uploadFileResponse.getErrors())));

    validateUploadContentExistence(true);
    //TODO: talio - check upload cleanup
//    validateCompositionDataExistence(false);
  }

  @Test(dependsOnMethods = {"testInvalidUploadAfterFullComposition"})
  public void testEmptyCompositionUploadAfterFullComposition() throws IOException {
    testUploadWithComposition();

    InputStream zis = getFileInputStream("/vspmanager/zips/emptyComposition.zip");
    vendorSoftwareProductManager.upload(vspId, zis, USER1);
    OrchestrationTemplateActionResponse uploadFileResponse =
        vendorSoftwareProductManager.process(vspId, USER1);
    Assert.assertEquals(uploadFileResponse.getStatus(), UploadFileStatus.Success);
    Assert.assertTrue(MapUtils.isEmpty(
        MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, uploadFileResponse.getErrors())));

    validateUploadContentExistence(true);
    validateCompositionDataExistence(false);
  }
*/}
