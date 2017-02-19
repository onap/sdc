package org.openecomp.sdc.vendorsoftwareproduct.upload.HeatCleanup;

import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.vendorsoftwareproduct.VSPCommon;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.UploadDataEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.impl.VendorSoftwareProductManagerImpl;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileStatus;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.dao.ServiceModelDaoFactory;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.validation.types.MessageContainerUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class HeatCleanupOnNewUploadTest {
  private static final String USER1 = "vspTestUser1";

  private static final VendorSoftwareProductManager vendorSoftwareProductManager =
      new VendorSoftwareProductManagerImpl();
  private static final VendorSoftwareProductDao vendorSoftwareProductDao =
      VendorSoftwareProductDaoFactory.getInstance().createInterface();
  private static final ServiceModelDao serviceModelDao =
      ServiceModelDaoFactory.getInstance().createInterface();

  private static String vspId = null;
  private static Version vspActiveVersion = null;

  private static void validateUploadContentExistence(boolean exist) {
    UploadDataEntity uploadDataEntity =
        vendorSoftwareProductDao.getUploadData(new UploadDataEntity(vspId, vspActiveVersion));
    Assert.assertTrue((uploadDataEntity.getContentData() != null) == exist);
    Assert.assertTrue((uploadDataEntity.getValidationData() != null) == exist);
    Assert.assertTrue((uploadDataEntity.getPackageName() != null) == exist);
    Assert.assertTrue((uploadDataEntity.getPackageVersion() != null) == exist);
    Assert.assertTrue((serviceModelDao.getServiceModel(vspId, vspActiveVersion) != null) == exist);
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
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @BeforeClass
  private void init() {
    UniqueValueUtil
        .deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
            "VSPTestEmpty");

    VspDetails vspDetails = vendorSoftwareProductManager.createNewVsp(VSPCommon
        .createVspDetails(null, null, "VSPTestEmpty", "Test-vsp-empty", "vendorName", "vlm1Id",
            "icon", "category", "subCategory", "123", null), USER1);
    vspId = vspDetails.getId();
    vspActiveVersion = vspDetails.getVersion();
  }

  @Test
  public void testUploadWithComposition() {
    InputStream zis = getFileInputStream("/vspmanager/zips/fullComposition.zip");

    UploadFileResponse uploadFileResponse =
        vendorSoftwareProductManager.uploadFile(vspId, zis, USER1);

    Assert.assertEquals(uploadFileResponse.getStatus(), UploadFileStatus.Success);
    Assert.assertTrue(MapUtils.isEmpty(
        MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, uploadFileResponse.getErrors())));

    validateUploadContentExistence(true);
    validateCompositionDataExistence(true);
  }

  @Test(dependsOnMethods = {"testUploadWithComposition"})
  public void testProccesesMIBsDeletionAfterNewUpload() {
    InputStream zis1 = getFileInputStream("/vspmanager/zips/fullComposition.zip");
    InputStream zis2 = getFileInputStream("/vspmanager/zips/fullComposition.zip");
    InputStream mib = getFileInputStream("/vspmanager/zips/vDNS.zip");

    vendorSoftwareProductManager.uploadFile(vspId, zis1, USER1);
    List<ComponentEntity> components =
        (List<ComponentEntity>) vendorSoftwareProductDao.listComponents(vspId, vspActiveVersion);
    String componentId = components.get(0).getId();

    vendorSoftwareProductManager
        .uploadComponentMib(mib, "vDNS.zip", vspId, componentId, true, USER1);
    vendorSoftwareProductManager
        .createProcess(new org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity(vspId, vspActiveVersion, componentId, null), USER1);

    vendorSoftwareProductManager.uploadFile(vspId, zis2, USER1);
    Assert.assertTrue(
        vendorSoftwareProductManager.listMibFilenames(vspId, componentId, USER1).getSnmpTrap() ==
            null);
    Assert.assertTrue(CollectionUtils
        .isEmpty(vendorSoftwareProductDao.listProcesses(vspId, vspActiveVersion, componentId)));
  }

  @Test(dependsOnMethods = {"testProccesesMIBsDeletionAfterNewUpload"})
  public void testInvalidStructureUploadAfterFullComposition() {
    InputStream zis = getFileInputStream("/vspmanager/zips/withoutManifest.zip");

    UploadFileResponse uploadFileResponse =
        vendorSoftwareProductManager.uploadFile(vspId, zis, USER1);
    Assert.assertEquals(uploadFileResponse.getStatus(), UploadFileStatus.Failure);
    Assert.assertTrue(MapUtils.isNotEmpty(
        MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, uploadFileResponse.getErrors())));

    validateUploadContentExistence(true);
    validateCompositionDataExistence(true);
  }

  @Test(dependsOnMethods = {"testInvalidStructureUploadAfterFullComposition"})
  public void testInvalidUploadAfterFullComposition() {
    InputStream zis = getFileInputStream("/vspmanager/zips/missingYml.zip");

    UploadFileResponse uploadFileResponse =
        vendorSoftwareProductManager.uploadFile(vspId, zis, USER1);
    Assert.assertEquals(uploadFileResponse.getStatus(), UploadFileStatus.Success);
    Assert.assertTrue(MapUtils.isNotEmpty(
        MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, uploadFileResponse.getErrors())));

    validateUploadContentExistence(true);
    validateCompositionDataExistence(false);
  }

  @Test(dependsOnMethods = {"testInvalidUploadAfterFullComposition"})
  public void testEmptyCompositionUploadAfterFullComposition() throws IOException {
    testUploadWithComposition();

    InputStream zis = getFileInputStream("/vspmanager/zips/emptyComposition.zip");
    UploadFileResponse uploadFileResponse =
        vendorSoftwareProductManager.uploadFile(vspId, zis, USER1);
    Assert.assertEquals(uploadFileResponse.getStatus(), UploadFileStatus.Success);
    Assert.assertTrue(MapUtils.isEmpty(
        MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, uploadFileResponse.getErrors())));

    validateUploadContentExistence(true);
    validateCompositionDataExistence(false);
  }
}
