package org.openecomp.sdc.vendorsoftwareproduct;

import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacadeFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.impl.VendorSoftwareProductManagerImpl;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.MibUploadStatus;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.utilities.CommonMethods;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ComponentsUploadTest {

  private static final String USER1 = "vspTestUser1";

  private static VendorSoftwareProductManager vendorSoftwareProductManager =
      new VendorSoftwareProductManagerImpl();
  private static VendorSoftwareProductDao vendorSoftwareProductDao =
      VendorSoftwareProductDaoFactory.getInstance().createInterface();
  private static VendorLicenseFacade vendorLicenseFacade =
      VendorLicenseFacadeFactory.getInstance().createInterface();

  private static String vspId = null;
  private static Version activeVersion = null;
  private static String trapFileName = "MMSC.zip";
  private static String pollFileName = "MNS OAM FW.zip";
  private static String notZipFileName = "notZipFile";
  private static String zipWithFoldersFileName = "zipFileWithFolder.zip";
  private static String emptyZipFileName = "emptyZip.zip";
  private String vlm1Id;
  private String componentId;

  @BeforeTest
  private void init() {
    UniqueValueUtil
        .deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
            "VSPTestMib");
    vlm1Id = vendorLicenseFacade.createVendorLicenseModel(VSPCommon
            .createVendorLicenseModel("vlmName " + CommonMethods.nextUuId(), "vlm1Id desc", "icon1"),
        USER1).getId();
    VspDetails vspDetails = vendorSoftwareProductManager.createNewVsp(VSPCommon
        .createVspDetails(null, null, "VSPTestMib", "Test-vsp-mib", "vendorName", vlm1Id, "icon",
            "category", "subCategory", "123", null), USER1);

    vspId = vspDetails.getId();
    activeVersion = vspDetails.getVersion();
    componentId = createComponent(new ComponentEntity(vspId, activeVersion, null)).getId();
  }


  @Test
  public void testUploadAndFilenamesList() {
    InputStream zis1 = getFileInputStream("/validation/zips/various/MMSC.zip");
    InputStream zis2 = getFileInputStream("/validation/zips/various/MNS OAM FW.zip");

    vendorSoftwareProductManager
        .uploadComponentMib(zis1, "MMSC.zip", vspId, componentId, true, USER1);
    vendorSoftwareProductManager
        .uploadComponentMib(zis2, "MNS OAM FW.zip", vspId, componentId, false, USER1);

    MibUploadStatus mibUploadStatus =
        vendorSoftwareProductManager.listMibFilenames(vspId, componentId, USER1);
    Assert.assertEquals(mibUploadStatus.getSnmpTrap(), trapFileName);
    Assert.assertEquals(mibUploadStatus.getSnmpPoll(), pollFileName);
  }

  @Test(dependsOnMethods = "testUploadAndFilenamesList")
  public void testMibsExistentAfterCheckout() throws IOException {
    activeVersion = vendorSoftwareProductManager.checkin(vspId, USER1);
//        UniqueValueUtil.deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.COMPONENT_ARTIFACT_NAME, "MMSC.zip");
//        UniqueValueUtil.deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.COMPONENT_ARTIFACT_NAME, "MNS OAM FW.zip");
    activeVersion = vendorSoftwareProductManager.checkout(vspId, USER1);

    MibUploadStatus mibUploadStatus =
        vendorSoftwareProductManager.listMibFilenames(vspId, componentId, USER1);
    Assert.assertNotNull(mibUploadStatus.getSnmpTrap());
    Assert.assertNotNull(mibUploadStatus.getSnmpPoll());
  }

  @Test(dependsOnMethods = "testMibsExistentAfterCheckout")
  public void testDeleteFile() {
    vendorSoftwareProductManager.deleteComponentMib(vspId, componentId, true, USER1);
    vendorSoftwareProductManager.deleteComponentMib(vspId, componentId, false, USER1);

    MibUploadStatus mibUploadStatus =
        vendorSoftwareProductManager.listMibFilenames(vspId, componentId, USER1);
    Assert.assertNull(mibUploadStatus.getSnmpTrap());
    Assert.assertNull(mibUploadStatus.getSnmpPoll());
  }

  @Test(dependsOnMethods = "testDeleteFile")
  public void testUploadInvalidZip() {
    URL url = this.getClass().getResource("/notZipFile");

    try {
      vendorSoftwareProductManager
          .uploadComponentMib(url.openStream(), notZipFileName, vspId, componentId, true, USER1);
      Assert.fail();
    } catch (Exception e) {
//            Assert.assertEquals(e.getMessage(), "MIB uploaded for vendor software product with Id " + vspId + " and version " + activeVersion + " is invalid: Invalid zip file");
      Assert.assertEquals(e.getMessage(), "Invalid zip file");
    }
  }

  @Test(dependsOnMethods = "testUploadInvalidZip")
  public void testUploadZipWithFolders() {
    InputStream zis = getFileInputStream("/vspmanager/zips/zipFileWithFolder.zip");

    try {
      vendorSoftwareProductManager
          .uploadComponentMib(zis, zipWithFoldersFileName, vspId, componentId, true, USER1);
      Assert.fail();
    } catch (Exception e) {
      Assert.assertEquals(e.getMessage(), "Zip file should not contain folders");
    }
  }

  @Test(dependsOnMethods = "testUploadZipWithFolders")
  public void testUploadEmptyZip() {
    InputStream zis = getFileInputStream("/vspmanager/zips/emptyZip.zip");

    try {
      vendorSoftwareProductManager
          .uploadComponentMib(zis, emptyZipFileName, vspId, componentId, true, USER1);
      Assert.fail();
    } catch (Exception e) {
      Assert.assertEquals(e.getMessage(), "Invalid zip file");
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


  private ComponentEntity createComponent(ComponentEntity component) {
    component.setId(CommonMethods.nextUuId());
    vendorSoftwareProductDao.createComponent(component);
    return component;
  }
}
