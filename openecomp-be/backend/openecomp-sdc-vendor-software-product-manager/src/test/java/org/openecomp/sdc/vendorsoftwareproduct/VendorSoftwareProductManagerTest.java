package org.openecomp.sdc.vendorsoftwareproduct;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.ValidationErrorBuilder;
import org.openecomp.sdc.common.utils.AsdcCommon;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;

import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.UploadDataEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes;
import org.openecomp.sdc.vendorsoftwareproduct.impl.VendorSoftwareProductManagerImpl;
import org.openecomp.sdc.vendorsoftwareproduct.tree.UploadFileTest;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.VersionedVendorSoftwareProductInfo;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersioningErrorCodes;
import org.openecomp.core.model.dao.EnrichedServiceModelDaoFactory;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.validation.errors.Messages;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class VendorSoftwareProductManagerTest {
  public static final Version VERSION01 = new Version(0, 1);
  public static final Version VERSION10 = new Version(1, 0);
  private static final String USER1 = "vspTestUser1";
  private static final String USER2 = "vspTestUser2";
  private static final String USER3 = "vspTestUser3";
  public static String id001 = null;
  public static String id002 = null;
  public static String id003 = null;
  public static String id004 = null;
  public static String id005 = null;
  public static String id006 = null;
  public static String id007 = null;
  public static Version activeVersion002 = null;
  private static VendorSoftwareProductManager vendorSoftwareProductManager =
      new VendorSoftwareProductManagerImpl();
  private static org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao
      vendorSoftwareProductDao =
      VendorSoftwareProductDaoFactory.getInstance().createInterface();
  private static VendorLicenseFacade vendorLicenseFacade =
      org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacadeFactory.getInstance().createInterface();
  private static String vlm1Id;
  private static String licenseAgreementId;
  private static String featureGroupId;
  private static VspDetails vsp1;
  private static VspDetails vsp2;
  UploadFileTest ut = new UploadFileTest();

  static void assertVspsEquals(VspDetails actual, VspDetails expected) {
    Assert.assertEquals(actual.getId(), expected.getId());
    Assert.assertEquals(actual.getVersion(), expected.getVersion());
    Assert.assertEquals(actual.getName(), expected.getName());
    Assert.assertEquals(actual.getDescription(), expected.getDescription());
    Assert.assertEquals(actual.getIcon(), expected.getIcon());
    Assert.assertEquals(actual.getCategory(), expected.getCategory());
    Assert.assertEquals(actual.getSubCategory(), expected.getSubCategory());
    Assert.assertEquals(actual.getVendorName(), expected.getVendorName());
    Assert.assertEquals(actual.getVendorId(), expected.getVendorId());
    Assert.assertEquals(actual.getLicenseAgreement(), expected.getLicenseAgreement());
    Assert.assertEquals(actual.getFeatureGroups(), expected.getFeatureGroups());
  }

  @BeforeTest
  private void init() {
    UniqueValueUtil
        .deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
            "VSP1");
    UniqueValueUtil
        .deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
            "VSP3");
    UniqueValueUtil
        .deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
            "VSP4");
    UniqueValueUtil
        .deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
            "VSP5");
    UniqueValueUtil
        .deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
            "vsp1_test");
    UniqueValueUtil
        .deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
            "vsp2_test");
    createVlm();
  }

  private void createVlm() {
    vlm1Id = vendorLicenseFacade.createVendorLicenseModel(VSPCommon
            .createVendorLicenseModel("vlmName " + CommonMethods.nextUuId(), "vlm1Id desc", "icon1"),
        USER1).getId();

    String entitlementPoolId = vendorLicenseFacade
        .createEntitlementPool(new EntitlementPoolEntity(vlm1Id, null, null), USER1).getId();

    org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity
        featureGroup = new org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity(vlm1Id, null, null);
    featureGroup.getEntitlementPoolIds().add(entitlementPoolId);
    featureGroupId = vendorLicenseFacade.createFeatureGroup(featureGroup, USER1).getId();

    LicenseAgreementEntity licenseAgreement = new LicenseAgreementEntity(vlm1Id, null, null);
    licenseAgreement.getFeatureGroupIds().add(featureGroupId);
    licenseAgreementId =
        vendorLicenseFacade.createLicenseAgreement(licenseAgreement, USER1).getId();

    vendorLicenseFacade.checkin(vlm1Id, USER1);
    vendorLicenseFacade.submit(vlm1Id, USER1);
  }

  @Test
  public void testHeatSet() {
    Set<HeatStructureTree> set = new HashSet<>();
    HeatStructureTree heatStructureTree1 = new HeatStructureTree();
    HeatStructureTree heatStructureTree2 = new HeatStructureTree();

    heatStructureTree1.setFileName("file");

    HeatStructureTree env = new HeatStructureTree();
    env.setFileName("env");
    heatStructureTree1.setEnv(env);

    heatStructureTree2.setFileName("file");
    heatStructureTree2.setEnv(env);

    set.add(heatStructureTree1);
    set.add(heatStructureTree2);

    Assert.assertEquals(set.size(), 1);
  }

  @Test(dependsOnMethods = {"testHeatSet"})
  public void testCreateVSP() {
    VspDetails expectedVsp = VSPCommon
        .createVspDetails(null, null, "VSP1", "Test-vsp", "vendorName", vlm1Id, "icon", "category",
            "subCategory", "123", null);

    VspDetails createdVsp = vendorSoftwareProductManager.createNewVsp(expectedVsp, USER1);
    id001 = createdVsp.getId();
    Assert.assertNotNull(id001);
    Assert.assertNotNull(createdVsp.getVersion());

    VspDetails actualVsp =
        vendorSoftwareProductDao.getVendorSoftwareProductInfo(new VspDetails(id001, VERSION01));
    expectedVsp.setId(id001);
    expectedVsp.setVersion(VERSION01);

    assertVspsEquals(actualVsp, expectedVsp);
    Assert.assertNotNull(
        vendorSoftwareProductManager.getVspQuestionnaire(id001, null, USER1).getData());
  }

  @Test(dependsOnMethods = {"testCreateVSP"})
  public void testCreateWithExistingName_negative() {
    try {
      VspDetails expectedVsp = VSPCommon
          .createVspDetails(null, null, "Vsp1", "Test-vsp", "vendorName", vlm1Id, "icon",
              "category", "subCategory", "123", null);
      vendorSoftwareProductManager.createNewVsp(expectedVsp, USER1);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), UniqueValueUtil.UNIQUE_VALUE_VIOLATION);
    }
  }

  @Test(dependsOnMethods = {"testCreateWithExistingName_negative"})
  public void testGetVSPDetails() {
    VersionedVendorSoftwareProductInfo actualVsp =
        vendorSoftwareProductManager.getVspDetails(id001, null, USER1);

    VspDetails expectedVsp =
        vendorSoftwareProductDao.getVendorSoftwareProductInfo(new VspDetails(id001, VERSION01));
    assertVspsEquals(actualVsp.getVspDetails(), expectedVsp);
    Assert.assertEquals(actualVsp.getVersionInfo().getActiveVersion(), VERSION01);
    Assert.assertEquals(actualVsp.getVersionInfo().getStatus(), org.openecomp.sdc.versioning.dao.types.VersionStatus.Locked);
    Assert.assertEquals(actualVsp.getVersionInfo().getLockingUser(), USER1);
  }

  @Test(dependsOnMethods = {"testGetVSPDetails"})
  public void testUpdateVSP() {
    VspDetails expectedVsp = VSPCommon
        .createVspDetails(id001, VERSION01, "VSP1", null, "vendorName", vlm1Id, "icon", "category",
            "subCategory", "456", null);
    vendorSoftwareProductManager.updateVsp(expectedVsp, USER1);

    VspDetails actualVsp =
        vendorSoftwareProductDao.getVendorSoftwareProductInfo(new VspDetails(id001, VERSION01));

    assertVspsEquals(actualVsp, expectedVsp);
  }

  @Test(dependsOnMethods = {"testUpdateVSP"})
  public void testGetVSPDetailsAfterUpdate() {
    VersionedVendorSoftwareProductInfo vspDetails =
        vendorSoftwareProductManager.getVspDetails(id001, null, USER1);
    Assert.assertEquals(vspDetails.getVspDetails().getName(), "VSP1");
    Assert.assertEquals(vspDetails.getVspDetails().getCategory(), "category");
    Assert.assertEquals(vspDetails.getVspDetails().getSubCategory(), "subCategory");
    Assert.assertEquals(vspDetails.getVspDetails().getVendorId(), vlm1Id);
    Assert.assertEquals(vspDetails.getVersionInfo().getActiveVersion(), VERSION01);
    Assert.assertEquals(vspDetails.getVersionInfo().getStatus(), org.openecomp.sdc.versioning.dao.types.VersionStatus.Locked);
    Assert.assertEquals(vspDetails.getVersionInfo().getLockingUser(), USER1);
  }

  @Test(dependsOnMethods = {"testGetVSPDetailsAfterUpdate"})
  public void testGetVSPList() {
    String licenseAgreementId = "bla bla";
    VspDetails vspDetails = vendorSoftwareProductManager.createNewVsp(VSPCommon
        .createVspDetails(null, null, "VSP3", "Test-vsp", "vendorName", vlm1Id, "icon", "category",
            "subCategory", licenseAgreementId, null), USER1);
    id002 = vspDetails.getId();
    vspDetails = vendorSoftwareProductManager.createNewVsp(VSPCommon
        .createVspDetails(null, null, "VSP4", "Test-vsp", "vendorName", vlm1Id, "icon", "category",
            "subCategory", licenseAgreementId, null), USER1);
    id003 = vspDetails.getId();

    List<VersionedVendorSoftwareProductInfo> vspDetailsList =
        vendorSoftwareProductManager.getVspList(null, USER1);
    int foundCount = 0;
    for (VersionedVendorSoftwareProductInfo vsp : vspDetailsList) {
      if (vsp.getVspDetails().getId().equals(id001) || vsp.getVspDetails().getId().equals(id002) ||
          vsp.getVspDetails().getId().equals(id003)) {
        foundCount++;
      }
    }

    Assert.assertEquals(foundCount, 3);
  }

  @Test(dependsOnMethods = {"testGetVSPList"})
  // Unsupported operation for 1607 release.
/*    public void testDeleteVSP() {
        vendorSoftwareProductManager.deleteVsp(id001, USER1);

        VspDetails vspDetails = vendorSoftwareProductDao.getVendorSoftwareProductInfo(new VspDetails(id001, VERSION01));
        Assert.assertNull(vspDetails);

        List<VersionedVendorSoftwareProductInfo> vspDetailsList = vendorSoftwareProductManager.getVspList(null, USER1);
        boolean found001 = false;
        for (VersionedVendorSoftwareProductInfo vsp : vspDetailsList) {
            if (vsp.getVspDetails().getId().equals(id001)) {
                found001 = true;
            }
        }

        Assert.assertFalse(found001);
    }


    @Test(dependsOnMethods = {"testDeleteVSP"})*/
  public void testCheckin() {
    vendorSoftwareProductManager.checkin(id002, USER1);

    VersionedVendorSoftwareProductInfo vsp2 =
        vendorSoftwareProductManager.getVspDetails(id002, null, USER1);
    Assert.assertEquals(vsp2.getVersionInfo().getActiveVersion(), VERSION01);
    Assert.assertEquals(vsp2.getVersionInfo().getStatus(), org.openecomp.sdc.versioning.dao.types.VersionStatus.Available);
    Assert.assertNull(vsp2.getVersionInfo().getLockingUser());
  }

  @Test(dependsOnMethods = {"testCheckin"})
  public void testCheckout() {
    vendorSoftwareProductManager.checkout(id002, USER2);

    VersionedVendorSoftwareProductInfo vsp2 =
        vendorSoftwareProductManager.getVspDetails(id002, null, USER2);
    Assert.assertEquals(vsp2.getVersionInfo().getActiveVersion(), new Version(0, 2));
    Assert.assertEquals(vsp2.getVersionInfo().getStatus(), org.openecomp.sdc.versioning.dao.types.VersionStatus.Locked);
    Assert.assertEquals(vsp2.getVersionInfo().getLockingUser(), USER2);

    vsp2 = vendorSoftwareProductManager.getVspDetails(id002, null, USER1);
    Assert.assertEquals(vsp2.getVersionInfo().getActiveVersion(), VERSION01);
    Assert.assertEquals(vsp2.getVersionInfo().getStatus(), org.openecomp.sdc.versioning.dao.types.VersionStatus.Locked);
    Assert.assertEquals(vsp2.getVersionInfo().getLockingUser(), USER2);
  }

  @Test(dependsOnMethods = {"testCheckout"})
  public void testUndoCheckout() {
    vendorSoftwareProductManager.undoCheckout(id002, USER2);

    VersionedVendorSoftwareProductInfo vsp2 =
        vendorSoftwareProductManager.getVspDetails(id002, null, USER2);
    Assert.assertEquals(vsp2.getVersionInfo().getActiveVersion(), VERSION01);
    Assert.assertEquals(vsp2.getVersionInfo().getStatus(), org.openecomp.sdc.versioning.dao.types.VersionStatus.Available);
    Assert.assertNull(vsp2.getVersionInfo().getLockingUser());
  }

  @Test(dependsOnMethods = {"testUndoCheckout"})
  public void testListFinalVspsWhenNone() {
    List<VersionedVendorSoftwareProductInfo> vspDetailsList =
        vendorSoftwareProductManager.getVspList(
            org.openecomp.sdc.versioning.dao.types.VersionStatus.Final.name(), USER1);
    int nonFinalFoundCount = 0;
    for (VersionedVendorSoftwareProductInfo vsp : vspDetailsList) {
      if (vsp.getVspDetails().getId().equals(id001) || vsp.getVspDetails().getId().equals(id002) ||
          vsp.getVspDetails().getId().equals(id003)) {
        nonFinalFoundCount++;
      }
    }

    Assert.assertEquals(nonFinalFoundCount, 0);
  }

  @Test(dependsOnMethods = "testListFinalVspsWhenNone")
  public void testSubmitWithoutLicencingData() throws IOException {
    ValidationResponse validationResponse = vendorSoftwareProductManager.submit(id002, USER2);
    Assert.assertNotNull(validationResponse);
    Assert.assertFalse(validationResponse.isValid());
    List<String> errorIds = validationResponse.getVspErrors().stream().map(ErrorCode::id).distinct()
        .collect(Collectors.toList());
    Assert.assertTrue(errorIds.contains(ValidationErrorBuilder.FIELD_VALIDATION_ERROR_ERR_ID));
    Assert.assertTrue(errorIds.contains(VendorSoftwareProductErrorCodes.VSP_INVALID));
  }

  @Test(dependsOnMethods = {"testSubmitWithoutLicencingData"})
  public void testSubmitWithoutUploadData() throws IOException {
    vendorSoftwareProductManager.checkout(id002, USER2);

    VspDetails updatedVsp2 =
        vendorSoftwareProductManager.getVspDetails(id002, null, USER2).getVspDetails();
    updatedVsp2.setFeatureGroups(new ArrayList<>());
    updatedVsp2.getFeatureGroups().add(featureGroupId);
    updatedVsp2.setLicenseAgreement(licenseAgreementId);

    vendorSoftwareProductManager.updateVsp(updatedVsp2, USER2);
    activeVersion002 = vendorSoftwareProductManager.checkin(id002, USER2);

    ValidationResponse validationResponse = vendorSoftwareProductManager.submit(id002, USER2);
    Assert.assertNotNull(validationResponse);
    Assert.assertFalse(validationResponse.isValid());
    Assert.assertTrue(validationResponse.getVspErrors().size() > 0);
  }

  @Test(dependsOnMethods = {"testSubmitWithoutUploadData"})
  public void testUploadFile() throws IOException {
    activeVersion002 = vendorSoftwareProductManager.checkout(id002, USER1);
    testLegalUpload(id002, activeVersion002,
        getFileInputStream("/vspmanager/zips/emptyComposition.zip"), USER1);
  }

/*    @Test(dependsOnMethods = {"testUploadFile"})
    public void testUploadFile2() throws IOException {
        testLegalUpload(id002, activeVersion002, ut.getZipInputStream("/legalUpload2"), USER1);
    }*/

  @Test
  public void testDownloadFile() throws IOException {
    VspDetails expectedVsp = VSPCommon
        .createVspDetails(null, null, String.format("VSP-test-%s", vlm1Id), "Test-vsp",
            "vendorName", vlm1Id, "icon", "category", "subCategory", "123", null);
    VspDetails createdVsp = vendorSoftwareProductManager.createNewVsp(expectedVsp, USER1);

    id005 = createdVsp.getId();
    Assert.assertNotNull(id005);
    Assert.assertNotNull(createdVsp.getVersion());

    //InputStream zipInputStream = getFileInputStream("/legalUpload/zip/legalUpload.zip")
    try (InputStream zipInputStream = ut.getZipInputStream("/legalUpload")) {

      UploadFileResponse resp =
          vendorSoftwareProductManager.uploadFile(id005, zipInputStream, USER1);
      File latestHeatPackage = vendorSoftwareProductManager.getLatestHeatPackage(id005, USER1);

      zipInputStream.reset();
      byte[] uploaded = IOUtils.toByteArray(zipInputStream);

      byte[] downloaded;
      try (BufferedInputStream fileStream = new BufferedInputStream(
          new FileInputStream(latestHeatPackage))) {
        downloaded = IOUtils.toByteArray(fileStream);
      }

      Assert.assertTrue(Arrays.equals(uploaded, downloaded));
    }
  }

  @Test(dependsOnMethods = {"testUploadFile"})
  public void testUploadNotExistingFile() throws IOException {
    URL url = this.getClass().getResource("notExist.zip");
    testLegalUpload(id002, activeVersion002, url == null ? null : url.openStream(), USER1);
  }

  @Test(dependsOnMethods = {"testUploadFile"}, expectedExceptions = CoreException.class)
  public void negativeTestCreatePackageBeforeSubmit() throws IOException {
    vendorSoftwareProductManager.createPackage(id002, USER1);
  }

  @Test(dependsOnMethods = {"negativeTestCreatePackageBeforeSubmit"})
  public void negativeTestGetVSPDetailsNonExistingVersion() {
    try {
      vendorSoftwareProductManager.getVspDetails(id002, new Version(43, 8), USER1);
      Assert.assertTrue(false);
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), VersioningErrorCodes.REQUESTED_VERSION_INVALID);
    }
  }

  @Test(dependsOnMethods = {"negativeTestCreatePackageBeforeSubmit"})
  public void negativeTestGetVSPDetailsCheckoutByOtherVersion() {
    try {
      vendorSoftwareProductManager.getVspDetails(id002, activeVersion002, USER2);
      Assert.assertTrue(false);
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), VersioningErrorCodes.REQUESTED_VERSION_INVALID);
    }
  }

  @Test(dependsOnMethods = {"negativeTestCreatePackageBeforeSubmit"})
  public void testGetVSPDetailsCandidateVersion() {
    VersionedVendorSoftwareProductInfo actualVsp =
        vendorSoftwareProductManager.getVspDetails(id002, new Version(0, 3), USER1);

    VspDetails expectedVsp = vendorSoftwareProductDao
        .getVendorSoftwareProductInfo(new VspDetails(id002, new Version(0, 3)));
    assertVspsEquals(actualVsp.getVspDetails(), expectedVsp);
    Assert.assertEquals(actualVsp.getVersionInfo().getActiveVersion(), new Version(0, 3));
    Assert.assertEquals(actualVsp.getVersionInfo().getStatus(), org.openecomp.sdc.versioning.dao.types.VersionStatus.Locked);
    Assert.assertEquals(actualVsp.getVersionInfo().getLockingUser(), USER1);
  }

  @Test(dependsOnMethods = {"negativeTestCreatePackageBeforeSubmit"})
  public void testGetVSPDetailsOldVersion() {
    VersionedVendorSoftwareProductInfo actualVsp =
        vendorSoftwareProductManager.getVspDetails(id002, new Version(0, 1), USER2);

    VspDetails expectedVsp = vendorSoftwareProductDao
        .getVendorSoftwareProductInfo(new VspDetails(id002, new Version(0, 1)));
    assertVspsEquals(actualVsp.getVspDetails(), expectedVsp);
    Assert.assertEquals(actualVsp.getVersionInfo().getActiveVersion(), new Version(0, 2));
    Assert.assertEquals(actualVsp.getVersionInfo().getStatus(), org.openecomp.sdc.versioning.dao.types.VersionStatus.Locked);
    Assert.assertEquals(actualVsp.getVersionInfo().getLockingUser(), USER1);
  }

  @Test(dependsOnMethods = {"negativeTestGetVSPDetailsNonExistingVersion",
      "negativeTestGetVSPDetailsCheckoutByOtherVersion", "testGetVSPDetailsCandidateVersion",
      "testGetVSPDetailsOldVersion"})
  public void testSubmit() throws IOException {
    activeVersion002 = vendorSoftwareProductManager.checkin(id002, USER1);
    ValidationResponse validationResponse = vendorSoftwareProductManager.submit(id002, USER1);
    Assert.assertTrue(validationResponse.isValid());

    VersionedVendorSoftwareProductInfo vsp2 =
        vendorSoftwareProductManager.getVspDetails(id002, null, USER1);
    Assert.assertEquals(vsp2.getVersionInfo().getActiveVersion(), VERSION10);
    Assert.assertEquals(vsp2.getVersionInfo().getStatus(), org.openecomp.sdc.versioning.dao.types.VersionStatus.Final);
    Assert.assertNull(vsp2.getVersionInfo().getLockingUser());
  }

  @Test(dependsOnMethods = {"testSubmit"})
  public void testListFinalVspsWhenExist() {
    List<VersionedVendorSoftwareProductInfo> vspDetailsList =
        vendorSoftwareProductManager.getVspList(
            org.openecomp.sdc.versioning.dao.types.VersionStatus.Final.name(), USER1);
    int nonFinalFoundCount = 0;
    boolean found002 = false;
    for (VersionedVendorSoftwareProductInfo vsp : vspDetailsList) {
      if (vsp.getVspDetails().getId().equals(id002)) {
        found002 = true;
      }
      if (vsp.getVspDetails().getId().equals(id001) || vsp.getVspDetails().getId().equals(id003)) {
        nonFinalFoundCount++;
      }
    }

    Assert.assertEquals(nonFinalFoundCount, 0);
    Assert.assertTrue(found002);
  }

  @Test(dependsOnMethods = {"testSubmit"})
  public void testCreatePackage() throws IOException {
    PackageInfo packageInfo = vendorSoftwareProductManager.createPackage(id002, USER1);
    Assert.assertNotNull(packageInfo.getVspId());
  }

  @Test
  public void testUploadFileWithoutManifest() {
    InputStream zis = getFileInputStream("/vspmanager/zips/withoutManifest.zip");
    VspDetails vspDetails = vendorSoftwareProductManager.createNewVsp(VSPCommon
        .createVspDetails(null, null, "VSP5", "Test-vsp", "vendorName", vlm1Id, "icon", "category",
            "subCategory", "456", null), USER1);
    id004 = vspDetails.getId();

    UploadFileResponse uploadFileResponse =
        vendorSoftwareProductManager.uploadFile(id004, zis, USER1);

    Assert.assertNotNull(uploadFileResponse.getErrors());
    Assert.assertEquals(uploadFileResponse.getErrors().size(), 1);
  }

  @Test(dependsOnMethods = {"testUploadFileWithoutManifest"})
  public void testUploadFileMissingFile() {
    InputStream zis = getFileInputStream("/vspmanager/zips/missingYml.zip");

    UploadFileResponse uploadFileResponse =
        vendorSoftwareProductManager.uploadFile(id004, zis, USER1);

    Assert.assertEquals(uploadFileResponse.getErrors().size(), 3);
  }

  @Test(dependsOnMethods = {"testUploadFileMissingFile"})
  public void testUploadNotZipFile() throws IOException {
    URL url = this.getClass().getResource("/notZipFile");
    UploadFileResponse uploadFileResponse =
        vendorSoftwareProductManager.uploadFile(id004, url.openStream(), USER1);

    Assert.assertNotNull(uploadFileResponse.getErrors());
    Assert.assertEquals(
        uploadFileResponse.getErrors().get(AsdcCommon.UPLOAD_FILE).get(0).getMessage(),
        Messages.INVALID_ZIP_FILE.getErrorMessage());
  }

  @Test
  public void testEnrichModelInSubmit() {
    UniqueValueUtil
        .deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
            "VSP_syb");
    VspDetails vspDetails = vendorSoftwareProductManager.createNewVsp(VSPCommon
        .createVspDetails(null, null, "VSP_syb", "Test-vsp_syb", "vendorName", vlm1Id, "icon",
            "category", "subCategory", "456", null), USER1);
    String id = vspDetails.getId();

    //upload file
    InputStream zis = getFileInputStream("/vspmanager/zips/fullComposition.zip");
    UploadFileResponse uploadFileResponse = vendorSoftwareProductManager.uploadFile(id, zis, USER1);

    //check in
    vendorSoftwareProductManager.checkin(id, USER1);
    //submit
    try {
      ValidationResponse result = vendorSoftwareProductManager.submit(id, USER1);
    } catch (IOException e) {
      Assert.fail();
    }
    VersionedVendorSoftwareProductInfo details =
        vendorSoftwareProductManager.getVspDetails(id, null, USER1);
    Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity> components = vendorSoftwareProductManager
        .listComponents(id, details.getVersionInfo().getActiveVersion(), USER1);

    ToscaServiceModel model =
        (ToscaServiceModel) EnrichedServiceModelDaoFactory.getInstance().createInterface()
            .getServiceModel(id, details.getVersionInfo().getActiveVersion());

    Map<String, CapabilityDefinition> capabilities = new HashMap<>();
    for (org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity component : components) {
      model.getServiceTemplates().
          entrySet().
          stream().
          filter(entryValue -> entryValue.getValue() != null &&
              entryValue.getValue().getNode_types() != null &&
              entryValue.getValue().
                  getNode_types().
                  containsKey(component.getComponentCompositionData().getName())).
          forEach(entryValue -> entryValue.getValue().getNode_types().
              values().
              stream().
              filter(type -> MapUtils.isNotEmpty(type.getCapabilities())).
              forEach(type -> type.getCapabilities().
                  entrySet().
                  forEach(entry -> addCapability(entryValue.getKey(), capabilities, entry.getKey(),
                      entry.getValue()))));

    }

    Assert.assertNotNull(capabilities);
  }

  @Test(dependsOnMethods = {"testEnrichModelInSubmit"})
  public void testVSPListSortedByModificationTimeDescOreder() {
    vsp1 = VSPCommon
        .createVspDetails(null, null, "vsp1_test", "Test-vsp", "vendorName", vlm1Id, "icon",
            "category", "subCategory", "123", null);
    id006 = vendorSoftwareProductManager.createNewVsp(vsp1, USER3).getId();

    vsp2 = VSPCommon
        .createVspDetails(null, null, "vsp2_test", "Test-vsp", "vendorName", vlm1Id, "icon",
            "category", "subCategory", "123", null);
    id007 = vendorSoftwareProductManager.createNewVsp(vsp2, USER3).getId();

    assertVSPInWantedLocationInVSPList(id007, 0, USER3);
    assertVSPInWantedLocationInVSPList(id006, 1, USER3);
  }

  @Test(dependsOnMethods = {"testVSPListSortedByModificationTimeDescOreder"})
  public void testUpdatedVSPShouldBeInBeginningOfList() {
    vendorSoftwareProductManager.updateVsp(vsp1, USER3);
    assertVSPInWantedLocationInVSPList(id006, 0, USER3);

    vendorSoftwareProductManager
        .uploadFile(id007, getFileInputStream("/vspmanager/zips/emptyComposition.zip"), USER3);
    assertVSPInWantedLocationInVSPList(id007, 0, USER3);
  }

  @Test(dependsOnMethods = {"testUpdatedVSPShouldBeInBeginningOfList"})
  public void testVSPInBeginningOfListAfterCheckin() {
    vendorSoftwareProductManager.checkin(id006, USER3);
    assertVSPInWantedLocationInVSPList(id006, 0, USER3);

    vendorSoftwareProductManager.checkin(id007, USER3);
    assertVSPInWantedLocationInVSPList(id007, 0, USER3);
  }

  @Test(dependsOnMethods = {"testVSPInBeginningOfListAfterCheckin"})
  public void testVSPInBeginningOfListAfterCheckout() {
    vendorSoftwareProductManager.checkout(id006, USER3);
    assertVSPInWantedLocationInVSPList(id006, 0, USER3);
  }

  @Test(dependsOnMethods = {"testVSPInBeginningOfListAfterCheckout"})
  public void testVSPInBeginningOfListAfterUndoCheckout() {
    vendorSoftwareProductManager.checkout(id007, USER3);
    assertVSPInWantedLocationInVSPList(id007, 0, USER3);

    vendorSoftwareProductManager.undoCheckout(id006, USER3);
    assertVSPInWantedLocationInVSPList(id006, 0, USER3);
  }

  @Test(dependsOnMethods = {"testVSPInBeginningOfListAfterUndoCheckout"})
  public void testVSPInBeginningOfListAfterSubmit() throws IOException {
    vendorSoftwareProductManager.checkin(id007, USER3);
    vendorSoftwareProductManager.submit(id007, USER3);

    assertVSPInWantedLocationInVSPList(id007, 0, USER3);
  }

  private void testLegalUpload(String vspId, Version version, InputStream upload, String user) {
    vendorSoftwareProductManager.uploadFile(vspId, upload, user);

    UploadDataEntity uploadData =
        vendorSoftwareProductDao.getUploadData(new UploadDataEntity(vspId, version));
    Assert.assertNotNull(uploadData);
    Assert.assertNotNull(uploadData.getContentData());
  }

  private void addCapability(String entryValueKey, Map<String, CapabilityDefinition> capabilities,
                             String key, CapabilityDefinition value) {

    capabilities.put(entryValueKey + "_" + key, value);
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

  private void assertVSPInWantedLocationInVSPList(String vspId, int location, String user) {
    List<VersionedVendorSoftwareProductInfo> vspList =
        vendorSoftwareProductManager.getVspList(null, user);
    Assert.assertEquals(vspList.get(location).getVspDetails().getId(), vspId);
  }
}