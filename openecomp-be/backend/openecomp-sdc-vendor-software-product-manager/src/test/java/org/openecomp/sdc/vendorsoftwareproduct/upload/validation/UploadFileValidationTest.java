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

package org.openecomp.sdc.vendorsoftwareproduct.upload.validation;

public class UploadFileValidationTest {
/*

  private static final String USER1 = "UploadFileValidationTest";
  private static final String EMPTY_ZIP_FILE = "/validation/zips/emptyZip.zip";
  private static final String MISSING_MANIFEST_IN_ZIP_FILE =
      "/validation/zips/missingManifestInZip.zip";
  private static final String ZIP_FILE_WITH_FOLDER = "/validation/zips/zipFileWithFolder.zip";
  private static VendorSoftwareProductManager vendorSoftwareProductManager =
      new VendorSoftwareProductManagerImpl();
  private static String vspId;

  public static VendorLicenseModelEntity createVendorLicenseModel(String name, String desc,
                                                                  String icon) {
    VendorLicenseModelEntity vendorLicenseModel = new VendorLicenseModelEntity();
    vendorLicenseModel.setVendorName(name);
    vendorLicenseModel.setDescription(desc);
    vendorLicenseModel.setIconRef(icon);
    return vendorLicenseModel;
  }

  @BeforeTest
  private void init() {
    VspDetails vspDetails = new VspDetails();
    vspDetails.setVendorName("vspName_" + CommonMethods.nextUuId());
    vspId = vendorSoftwareProductManager.createVsp(vspDetails, USER1).getId();

    UniqueValueUtil
        .deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
            "VSP_syb_upload_various");
    UniqueValueUtil
        .deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
            "vsp_syb_upload_no_error");
    UniqueValueUtil
        .deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
            "vsp_syb");
  }

  @Test
  public void testUploadZipNull() {
    try {
      vendorSoftwareProductManager.upload(vspId, null, USER1);
      OrchestrationTemplateActionResponse response =
          vendorSoftwareProductManager.process(vspId, USER1);
    } catch (Exception exception) {
      Assert.assertEquals(exception.getMessage(),
          "Failed to get orchestration template for VSP with id " + vspId);
    }
  }

  @Test(dependsOnMethods = "testUploadZipNull")
  public void testUploadEmptyFile() {
    UploadFileResponse uploadFileResponse = vendorSoftwareProductManager
        .upload(vspId, new ByteArrayInputStream("".getBytes()),
            USER1);
    Assert.assertEquals(uploadFileResponse.getErrors().get(SdcCommon.UPLOAD_FILE).get(0)
            .getMessage(),
        Messages.NO_ZIP_FILE_WAS_UPLOADED_OR_ZIP_NOT_EXIST.getErrorMessage());
  }

  @Test(dependsOnMethods = "testUploadEmptyFile")
  public void testUploadEmptyZip() {
      UploadFileResponse uploadFileResponse = vendorSoftwareProductManager
          .upload(vspId, getFileInputStream(EMPTY_ZIP_FILE), USER1);
      Assert.assertEquals(uploadFileResponse.getErrors().get(SdcCommon.UPLOAD_FILE).get(0)
              .getMessage(),
          Messages.CREATE_MANIFEST_FROM_ZIP.getErrorMessage());
      */
/*Assert.assertEquals(exception.getMessage(),
          Messages.CREATE_MANIFEST_FROM_ZIP.getErrorMessage());*//*

  }

  @Test(dependsOnMethods = "testUploadEmptyZip")
  public void testUploadMissingManifestInZip() {
    vendorSoftwareProductManager.upload(vspId,
        getFileInputStream(MISSING_MANIFEST_IN_ZIP_FILE), USER1);
    OrchestrationTemplateActionResponse response =
        vendorSoftwareProductManager.process(vspId, USER1);
    Assert.assertEquals(response.getErrors().size(), 2);
  }

  @Test(dependsOnMethods = "testUploadMissingManifestInZip")
  public void testUploadZipWithFolder() {
    vendorSoftwareProductManager
        .upload(vspId, getFileInputStream(ZIP_FILE_WITH_FOLDER),
            USER1);
    OrchestrationTemplateActionResponse response =
        vendorSoftwareProductManager.process(vspId, USER1);
    Assert.assertEquals(response.getErrors().size(), 2);
  }

  @Test(dependsOnMethods = "testUploadZipWithFolder")
  public void testUploadVariousZips() {

    File[] files = getFileList("/validation/zips/various");
    InputStream is;
    for (File file : files) {
      if (file.isFile()) {
        OrchestrationTemplateActionResponse response;
        try {

          is = new FileInputStream(file);
          vendorSoftwareProductManager.upload(vspId, is, USER1);
          response =
              vendorSoftwareProductManager.process(vspId, USER1);

        } catch (FileNotFoundException exception) {
          throw new RuntimeException(exception);
        } catch (RuntimeException re) {

          throw new RuntimeException("failed upload:" + file.getName(), re);
        }
        System.out.println("zip:" + file.getName() + " Errors:" + calculateNumberOfMessages(
            MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, response.getErrors())) +
            " Warnings:" + calculateNumberOfMessages(
            MessageContainerUtil.getMessageByLevel(ErrorLevel.WARNING, response.getErrors())));
      }
    }
  }

  @Test(dependsOnMethods = "testUploadVariousZips")
  public void testUploadNoErrorVariousZips() {


    File[] files = getFileList("/validation/zips/various/noError");
    InputStream is;
    for (File file : files) {
      if (file.isFile()) {
        try {
          is = new FileInputStream(file);
          vendorSoftwareProductManager.upload(vspId, is, USER1);
          OrchestrationTemplateActionResponse response =
              vendorSoftwareProductManager.process(vspId, USER1);
          Map<String, List<ErrorMessage>> errors = response.getErrors();
          Assert.assertTrue(
              MapUtils.isEmpty(MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, errors)));


        } catch (FileNotFoundException exception) {
          throw new RuntimeException(exception);
        } catch (RuntimeException re) {
          Assert.fail("failed upload:" + file.getName() + " exception:" + re.getMessage());
        }
      }
    }
  }

  private InputStream getFileInputStream(String fileName) {
    URL url = this.getClass().getResource(fileName);
    try {
      return url.openStream();
    } catch (IOException exception) {
      exception.printStackTrace();
      return null;
    }
  }

  private File[] getFileList(String dir) {
    URL url = UploadFileValidationTest.class.getResource(dir);

    String path = url.getPath();
    File pathFile = new File(path);
    return pathFile.listFiles();


  }

  private int calculateNumberOfMessages(Map<String, List<ErrorMessage>> messages) {
    int sum = 0;
    for (List<ErrorMessage> errors : messages.values()) {
      sum += errors.size();
    }
    return sum;
  }

*/

}
