package org.openecomp.sdc.vendorsoftwareproduct.upload.validation;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.utils.AsdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorMessage;

import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.impl.VendorSoftwareProductManagerImpl;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.validation.errors.Messages;
import org.openecomp.core.validation.types.MessageContainerUtil;
import org.apache.commons.collections4.MapUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class UploadFileValidationTest {

  private static final String USER1 = "UploadFileValidationTest";
  private static final String EMPTY_ZIP_FILE = "/validation/zips/emptyZip.zip";
  private static final String MISSING_MANIFEST_IN_ZIP_FILE =
      "/validation/zips/missingManifestInZip.zip";
  private static final String ZIP_FILE_WITH_FOLDER = "/validation/zips/zipFileWithFolder.zip";
  private static VendorSoftwareProductManager vendorSoftwareProductManager =
      new VendorSoftwareProductManagerImpl();
  private static String vspId;

  public static org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity createVendorLicenseModel(String name, String desc,
                                                                                                            String icon) {
    org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity
        vendorLicenseModel = new org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity();
    vendorLicenseModel.setVendorName(name);
    vendorLicenseModel.setDescription(desc);
    vendorLicenseModel.setIconRef(icon);
    return vendorLicenseModel;
  }

  @BeforeTest
  private void init() {
    VspDetails vspDetails = new VspDetails();
    vspDetails.setVendorName("vspName_" + CommonMethods.nextUuId());
    vspId = vendorSoftwareProductManager.createNewVsp(vspDetails, USER1).getId();

    UniqueValueUtil
        .deleteUniqueValue(org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
            "VSP_syb_upload_various");
    UniqueValueUtil
        .deleteUniqueValue(org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
            "vsp_syb_upload_no_error");
    UniqueValueUtil
        .deleteUniqueValue(org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
            "vsp_syb");
  }

  @Test
  public void testUploadZipNull() {
    UploadFileResponse response = vendorSoftwareProductManager.uploadFile(vspId, null, USER1);
    Assert.assertEquals(response.getErrors().size(), 1);
    Assert.assertTrue(response.getErrors().containsKey(AsdcCommon.UPLOAD_FILE));
    Assert.assertEquals(response.getErrors().get(AsdcCommon.UPLOAD_FILE).get(0).getMessage(),
        Messages.NO_ZIP_FILE_WAS_UPLOADED_OR_ZIP_NOT_EXIST.getErrorMessage());
  }

  @Test(dependsOnMethods = "testUploadZipNull")
  public void testUploadEmptyFile() {
    UploadFileResponse response = vendorSoftwareProductManager
        .uploadFile(vspId, new ByteArrayInputStream("".getBytes()), USER1);
    Assert.assertEquals(response.getErrors().size(), 1);
    Assert.assertTrue(response.getErrors().containsKey(AsdcCommon.UPLOAD_FILE));
    Assert.assertEquals(response.getErrors().get(AsdcCommon.UPLOAD_FILE).get(0).getMessage(),
        Messages.NO_ZIP_FILE_WAS_UPLOADED_OR_ZIP_NOT_EXIST.getErrorMessage());
  }

  @Test(dependsOnMethods = "testUploadEmptyFile")
  public void testUploadEmptyZip() {
    UploadFileResponse response =
        vendorSoftwareProductManager.uploadFile(vspId, getFileInputStream(EMPTY_ZIP_FILE), USER1);
    Assert.assertEquals(response.getErrors().size(), 1);
    Assert.assertTrue(response.getErrors().containsKey(AsdcCommon.UPLOAD_FILE));
    Assert.assertEquals(response.getErrors().get(AsdcCommon.UPLOAD_FILE).get(0).getMessage(),
        Messages.INVALID_ZIP_FILE.getErrorMessage());
  }

  @Test(dependsOnMethods = "testUploadEmptyZip")
  public void testUploadMissingManifestInZip() {
    UploadFileResponse response = vendorSoftwareProductManager
        .uploadFile(vspId, getFileInputStream(MISSING_MANIFEST_IN_ZIP_FILE), USER1);
    Assert.assertEquals(response.getErrors().size(), 1);
    Assert.assertTrue(response.getErrors().containsKey(AsdcCommon.MANIFEST_NAME));
    Assert.assertEquals(response.getErrors().get(AsdcCommon.MANIFEST_NAME).get(0).getMessage(),
        Messages.MANIFEST_NOT_EXIST.getErrorMessage());
  }

  @Test(dependsOnMethods = "testUploadMissingManifestInZip")
  public void testUploadZipWithFolder() {
    UploadFileResponse response = vendorSoftwareProductManager
        .uploadFile(vspId, getFileInputStream(ZIP_FILE_WITH_FOLDER), USER1);
    Assert.assertEquals(response.getErrors().size(), 1);
    Assert.assertTrue(response.getErrors().containsKey(AsdcCommon.UPLOAD_FILE));
    Assert.assertEquals(response.getErrors().get(AsdcCommon.UPLOAD_FILE).get(0).getMessage(),
        Messages.ZIP_SHOULD_NOT_CONTAIN_FOLDERS.getErrorMessage());
  }

  @Test(dependsOnMethods = "testUploadZipWithFolder")
  public void testUploadVariousZips() {

    File[] files = getFileList("/validation/zips/various");
    InputStream is;
    for (File file : files) {
      if (file.isFile()) {
        UploadFileResponse response = null;
        try {

          is = new FileInputStream(file);
          response = vendorSoftwareProductManager.uploadFile(vspId, is, USER1);

        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        } catch (CoreException ce) {
          throw new RuntimeException("failed upload:" + file.getName(), ce);
        } catch (RuntimeException re) {

          throw new RuntimeException("failed upload:" + file.getName(), re);
        }
        System.out.println("zip:" + file.getName() + " Errors:" + calculateNumberOfMessages(
            MessageContainerUtil.getMessageByLevel(org.openecomp.sdc.datatypes.error.ErrorLevel.ERROR, response.getErrors())) +
            " Warnings:" + calculateNumberOfMessages(
            MessageContainerUtil.getMessageByLevel(org.openecomp.sdc.datatypes.error.ErrorLevel.WARNING, response.getErrors())));
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
          UploadFileResponse response = vendorSoftwareProductManager.uploadFile(vspId, is, USER1);
          Map<String, List<ErrorMessage>> errors = response.getErrors();
          Assert.assertTrue(
              MapUtils.isEmpty(MessageContainerUtil.getMessageByLevel(
                  org.openecomp.sdc.datatypes.error.ErrorLevel.ERROR, errors)));


        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        } catch (CoreException ce) {
          Assert.fail("failed upload:" + file.getName() + " exception:" + ce.getMessage());

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
    } catch (IOException e) {
      e.printStackTrace();
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


}
