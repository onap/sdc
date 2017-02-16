package org.openecomp.sdc.vendorlicense;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorlicense.impl.VendorLicenseManagerImpl;
import org.openecomp.core.util.UniqueValueUtil;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VendorLicenseModelTest {
  private static final org.openecomp.sdc.versioning.dao.types.Version
      VERSION01 = new org.openecomp.sdc.versioning.dao.types.Version(0, 1);
  private static final org.openecomp.sdc.versioning.dao.types.Version
      VERSION02 = new org.openecomp.sdc.versioning.dao.types.Version(0, 2);
  private static final String USER1 = "vlmTestUser1";
  private static final String USER2 = "vlmTestUser2";
  private static final String USER3 = "vlmTestUser3";
  private static final String VLM1_NAME = "Vlm1 name";
  private static VendorLicenseManager vendorLicenseManager = new VendorLicenseManagerImpl();
  private static org.openecomp.sdc.vendorlicense.dao.VendorLicenseModelDao vendorLicenseModelDao =
      org.openecomp.sdc.vendorlicense.dao.VendorLicenseModelDaoFactory.getInstance().createInterface();

  private static String vlm1Id;
  private static String vlm2Id;
  private static String vlm3Id;
  private static String vlm4Id;
  private static String laId;
  private static String fg1Id;
  private static String fg2Id;

  private static String testCreate() {
    org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity vlm1 = createVendorLicenseModel(VLM1_NAME, "VLM1 dec", "icon1");
    String vlmId = vendorLicenseManager.createVendorLicenseModel(vlm1, USER1).getId();

    vlm1.setVersion(VERSION01);
    org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity loadedVlm1 = vendorLicenseModelDao.get(vlm1);
    Assert.assertTrue(loadedVlm1.equals(vlm1));
    return vlmId;
  }

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
    UniqueValueUtil.deleteUniqueValue(VendorLicenseConstants.UniqueValues.VENDOR_NAME, VLM1_NAME);
    UniqueValueUtil
        .deleteUniqueValue(VendorLicenseConstants.UniqueValues.VENDOR_NAME, "VLM1 updated");
    UniqueValueUtil.deleteUniqueValue(VendorLicenseConstants.UniqueValues.VENDOR_NAME, "VLM2");
    UniqueValueUtil.deleteUniqueValue(VendorLicenseConstants.UniqueValues.VENDOR_NAME, "test_vlm1");
    UniqueValueUtil.deleteUniqueValue(VendorLicenseConstants.UniqueValues.VENDOR_NAME, "test_vlm2");
    UniqueValueUtil.deleteUniqueValue(VendorLicenseConstants.UniqueValues.VENDOR_NAME, "test_vlm3");
  }

  @Test
  public void createTest() {
    vlm1Id = testCreate();
  }

  @Test(dependsOnMethods = {"createTest"})
  public void testCreateWithExistingVendorName_negative() {
    try {
      vendorLicenseManager
          .createVendorLicenseModel(createVendorLicenseModel(VLM1_NAME, "VLM1 dec", "icon1"),
              USER1);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), UniqueValueUtil.UNIQUE_VALUE_VIOLATION);
    }
  }

  @Test(dependsOnMethods = {"testCreateWithExistingVendorName_negative"})
  public void updateTest() {
    org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity
        expectedVlm1 = new org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity(vlm1Id, VERSION01);
    expectedVlm1.setVendorName("VLM1 updated");
    expectedVlm1.setDescription("VLM1 dec updated");
    expectedVlm1.setIconRef("icon1 updated");
    vendorLicenseManager.updateVendorLicenseModel(expectedVlm1, USER1);

    org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity actualVlm1 =
        vendorLicenseModelDao.get(new org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity(vlm1Id, VERSION01));
    Assert.assertTrue(actualVlm1.equals(expectedVlm1));
  }

  @Test(dependsOnMethods = {"updateTest"})
  public void testUpdateWithSimilarVendorName() {
    org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity
        expectedVlm1 = new org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity(vlm1Id, VERSION01);
    expectedVlm1.setVendorName("vlm1 UPDATED");
    vendorLicenseManager.updateVendorLicenseModel(expectedVlm1, USER1);

    org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity actualVlm1 =
        vendorLicenseModelDao.get(new org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity(vlm1Id, VERSION01));
    Assert.assertTrue(actualVlm1.equals(expectedVlm1));
  }

  @Test(dependsOnMethods = {"updateTest"})
  public void testCreateWithRemovedVendorName() {
    testCreate();
  }

  @Test(dependsOnMethods = {"updateTest"})
  public void getTest() {
    org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity expectedVlm1 =
        vendorLicenseModelDao.get(new org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity(vlm1Id, VERSION01));
    org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel actualVlm1 =
        vendorLicenseManager.getVendorLicenseModel(vlm1Id, null, USER1);

    Assert.assertTrue(actualVlm1.getVendorLicenseModel().equals(expectedVlm1));
    Assert.assertEquals(actualVlm1.getVersionInfo().getActiveVersion(), VERSION01);
    Assert.assertEquals(actualVlm1.getVersionInfo().getStatus(), org.openecomp.sdc.versioning.dao.types.VersionStatus.Locked);
    Assert.assertEquals(actualVlm1.getVersionInfo().getLockingUser(), USER1);
  }

  @Test(dependsOnMethods = {"getTest"})
  public void listTest() {
    org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity vlm2 = createVendorLicenseModel("VLM2", "VLM2 dec", "icon2");
    vlm2Id = vendorLicenseManager.createVendorLicenseModel(vlm2, USER1).getId();
    vlm2.setId(vlm2Id);

    Collection<org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel> loadedVlms =
        vendorLicenseManager.listVendorLicenseModels(null, USER1);
    boolean vlm1Exists = false;
    boolean vlm2Exists = false;
    for (org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel loadedVlm : loadedVlms) {
      if (vlm1Id.equals(loadedVlm.getVendorLicenseModel().getId())) {
        vlm1Exists = true;
        continue;
      }
      if (vlm2Id.equals(loadedVlm.getVendorLicenseModel().getId())) {
        Assert.assertTrue(loadedVlm.getVendorLicenseModel().equals(vlm2));

        vlm2Exists = true;
      }
    }

    Assert.assertTrue(vlm1Exists);
    Assert.assertTrue(vlm2Exists);
  }

  @Test(dependsOnMethods = {"listTest"})
  public void listFinalVersionWhenNoneTest() {
    Collection<org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel> loadedVlms =
        vendorLicenseManager.listVendorLicenseModels(
            org.openecomp.sdc.versioning.dao.types.VersionStatus.Final.name(), USER1);
    boolean vlm1Exists = false;
    boolean vlm2Exists = false;
    for (org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel loadedVlm : loadedVlms) {
      if (vlm1Id.equals(loadedVlm.getVendorLicenseModel().getId())) {
        vlm1Exists = true;
        continue;
      }
      if (vlm2Id.equals(loadedVlm.getVendorLicenseModel().getId())) {
        vlm2Exists = true;
      }
    }

    Assert.assertFalse(vlm1Exists);
    Assert.assertFalse(vlm2Exists);
  }

  @Test(dependsOnMethods = {"listFinalVersionWhenNoneTest"})

  // Unsupported operation for 1607 release.
/*    public void deleteTest() {
        vendorLicenseManager.deleteVendorLicenseModel(vlm1Id, USER1);

        VendorLicenseModelEntity loadedVlm1 = vendorLicenseModelDao.get(new VendorLicenseModelEntity(vlm1Id, VERSION01));
        Assert.assertEquals(loadedVlm1, null);

        Collection<VendorLicenseModelEntity> loadedVlms = vendorLicenseModelDao.list(null);
        Assert.assertTrue(loadedVlms.size() > 1);
        boolean vlm1Exists = false;
        boolean vlm2Exists = false;
        for (VendorLicenseModelEntity loadedVlm : loadedVlms) {
            if (vlm1Id.equals(loadedVlm.getId())) {
                vlm1Exists = true;
            }
            if (vlm2Id.equals(loadedVlm.getId())) {
                vlm2Exists = true;
            }
        }
        Assert.assertFalse(vlm1Exists);
        Assert.assertTrue(vlm2Exists);
    }

    @Test(dependsOnMethods = {"deleteTest"})*/
  public void checkinTest() {
    vendorLicenseManager.checkin(vlm2Id, USER1);

    org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel versionedVlm2 =
        vendorLicenseManager.getVendorLicenseModel(vlm2Id, null, USER1);
    Assert.assertEquals(versionedVlm2.getVersionInfo().getActiveVersion(), VERSION01);
    Assert.assertEquals(versionedVlm2.getVersionInfo().getStatus(), org.openecomp.sdc.versioning.dao.types.VersionStatus.Available);
    Assert.assertNull(versionedVlm2.getVersionInfo().getLockingUser());
  }

  @Test(dependsOnMethods = {"checkinTest"})
  public void checkoutTest() {
    vendorLicenseManager.checkout(vlm2Id, USER2);

    org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel versionedVlm2 =
        vendorLicenseManager.getVendorLicenseModel(vlm2Id, null, USER2);
    Assert.assertEquals(versionedVlm2.getVersionInfo().getActiveVersion(), VERSION02);
    Assert.assertEquals(versionedVlm2.getVersionInfo().getStatus(), org.openecomp.sdc.versioning.dao.types.VersionStatus.Locked);
    Assert.assertEquals(versionedVlm2.getVersionInfo().getLockingUser(), USER2);

    versionedVlm2 = vendorLicenseManager.getVendorLicenseModel(vlm2Id, null, USER1);
    Assert.assertEquals(versionedVlm2.getVersionInfo().getActiveVersion(), VERSION01);
    Assert.assertEquals(versionedVlm2.getVersionInfo().getStatus(), org.openecomp.sdc.versioning.dao.types.VersionStatus.Locked);
    Assert.assertEquals(versionedVlm2.getVersionInfo().getLockingUser(), USER2);
  }

  @Test(dependsOnMethods = {"checkoutTest"})
  public void undoCheckoutTest() {
    vendorLicenseManager.undoCheckout(vlm2Id, USER2);

    org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel versionedVlm2 =
        vendorLicenseManager.getVendorLicenseModel(vlm2Id, null, USER2);
    Assert.assertEquals(versionedVlm2.getVersionInfo().getActiveVersion(), VERSION01);
    Assert.assertEquals(versionedVlm2.getVersionInfo().getStatus(), org.openecomp.sdc.versioning.dao.types.VersionStatus.Available);
    Assert.assertNull(versionedVlm2.getVersionInfo().getLockingUser());
  }

  @Test(dependsOnMethods = {"undoCheckoutTest"}, expectedExceptions = CoreException.class)
  public void submitUncompletedVlmNegativeTest() {
    vendorLicenseManager.submit(vlm2Id, USER2);
  }

  @Test(dependsOnMethods = {"submitUncompletedVlmNegativeTest"},
      expectedExceptions = CoreException.class)
  public void submitUncompletedVlmMissingFGNegativeTest() {
    vendorLicenseManager.checkout(vlm2Id, USER2);
    org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity
        licenseAgreement = new org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity(vlm2Id, null, null);
    org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity licenseAgreementEntity =
        vendorLicenseManager.createLicenseAgreement(licenseAgreement, USER2);
    laId = licenseAgreementEntity.getId();
    vendorLicenseManager.checkin(vlm2Id, USER2);
    vendorLicenseManager.submit(vlm2Id, USER2);
  }

  @Test(dependsOnMethods = {"submitUncompletedVlmMissingFGNegativeTest"},
      expectedExceptions = CoreException.class)
  public void submitUncompletedVlmMissingEPNegativeTest() {
    vendorLicenseManager.checkout(vlm2Id, USER2);
    org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity
        featureGroup = new org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity(vlm2Id, null, null);
    featureGroup = vendorLicenseManager.createFeatureGroup(featureGroup, USER2);
    fg1Id = featureGroup.getId();

    org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementModel licenseAgreementModel =
        vendorLicenseManager.getLicenseAgreementModel(vlm2Id, null, laId, USER2);
    Set<String> fgIdSet = new HashSet<>();
    fgIdSet.add(fg1Id);
    vendorLicenseManager
        .updateLicenseAgreement(licenseAgreementModel.getLicenseAgreement(), fgIdSet, null, USER2);
    vendorLicenseManager.checkin(vlm2Id, USER2);
    vendorLicenseManager.submit(vlm2Id, USER2);
  }

  @Test(dependsOnMethods = {"submitUncompletedVlmMissingEPNegativeTest"},
      expectedExceptions = CoreException.class)
  public void submitUncompletedVlmMissingEPInOneFGNegativeTest() {
    vendorLicenseManager.checkout(vlm2Id, USER2);
    org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity
        featureGroup = new org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity(vlm2Id, null, null);
    org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity entitlementPool = vendorLicenseManager
        .createEntitlementPool(new org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity(vlm2Id, null, null), USER2);
    featureGroup.getEntitlementPoolIds().add(entitlementPool.getId());
    featureGroup = vendorLicenseManager.createFeatureGroup(featureGroup, USER2);
    fg2Id = featureGroup.getId();
    org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementModel licenseAgreementModel =
        vendorLicenseManager.getLicenseAgreementModel(vlm2Id, null, laId, USER2);
    Set<String> fgIdSet = new HashSet<>();
    fgIdSet.add(fg2Id);
    vendorLicenseManager
        .updateLicenseAgreement(licenseAgreementModel.getLicenseAgreement(), fgIdSet, null, USER2);

    vendorLicenseManager.checkin(vlm2Id, USER2);
    vendorLicenseManager.submit(vlm2Id, USER2);
  }

  @Test(dependsOnMethods = {"submitUncompletedVlmMissingEPInOneFGNegativeTest"})
  public void submitTest() {
    vendorLicenseManager.checkout(vlm2Id, USER2);

    org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity entitlementPool = vendorLicenseManager
        .createEntitlementPool(new org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity(vlm2Id, null, null), USER2);
    Set<String> epSetId = new HashSet<>();
    epSetId.add(entitlementPool.getId());

    org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity
        featureGroup = new org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity(vlm2Id, null, fg1Id);
    featureGroup.getEntitlementPoolIds().add(entitlementPool.getId());
    org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupModel featureGroupModel =
        vendorLicenseManager.getFeatureGroupModel(featureGroup, USER2);

    vendorLicenseManager
        .updateFeatureGroup(featureGroupModel.getFeatureGroup(), null, null, epSetId, null, USER2);
    vendorLicenseManager.checkin(vlm2Id, USER2);
    vendorLicenseManager.submit(vlm2Id, USER2);

    org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel versionedVlm2 =
        vendorLicenseManager.getVendorLicenseModel(vlm2Id, null, USER1);
    Assert.assertEquals(versionedVlm2.getVersionInfo().getActiveVersion(), new org.openecomp.sdc.versioning.dao.types.Version(1, 0));
    Assert.assertEquals(versionedVlm2.getVersionInfo().getStatus(), org.openecomp.sdc.versioning.dao.types.VersionStatus.Final);
    Assert.assertNull(versionedVlm2.getVersionInfo().getLockingUser());
  }

  @Test(dependsOnMethods = {"submitTest"})
  public void listFinalVersionWhenOneTest() {
    Collection<org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel> loadedVlms =
        vendorLicenseManager.listVendorLicenseModels(
            org.openecomp.sdc.versioning.dao.types.VersionStatus.Final.name(), USER1);
    boolean vlm2Exists = false;
    for (org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel loadedVlm : loadedVlms) {
      if (vlm2Id.equals(loadedVlm.getVendorLicenseModel().getId())) {
        vlm2Exists = true;
        Assert.assertEquals(loadedVlm.getVersionInfo().getActiveVersion(), new org.openecomp.sdc.versioning.dao.types.Version(1, 0));
        Assert.assertEquals(loadedVlm.getVersionInfo().getStatus(), org.openecomp.sdc.versioning.dao.types.VersionStatus.Final);
        Assert.assertNull(loadedVlm.getVersionInfo().getLockingUser());
        break;
      }
    }

    Assert.assertTrue(vlm2Exists);
  }

  @Test(dependsOnMethods = {"listFinalVersionWhenOneTest"})
  public void testVLMListWithModificationTimeDescOrder() {
    org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity vlm1 = createVendorLicenseModel("test_vlm1", "desc", "icon");
    vlm3Id = vendorLicenseManager.createVendorLicenseModel(vlm1, USER3).getId();

    org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity vlm2 = createVendorLicenseModel("test_vlm2", "desc", "icon");
    vlm4Id = vendorLicenseManager.createVendorLicenseModel(vlm2, USER3).getId();

    assertVLMInWantedLocationInVSPList(vlm4Id, 0, USER3);
    assertVLMInWantedLocationInVSPList(vlm3Id, 1, USER3);
  }

  @Test(dependsOnMethods = {"testVLMListWithModificationTimeDescOrder"})
  public void testOldVLMAfterChangeShouldBeInBeginningOfList() {
    org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity ep = vendorLicenseManager
        .createEntitlementPool(new org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity(vlm3Id, null, null), USER3);

    assertVLMInWantedLocationInVSPList(vlm3Id, 0, USER3);
  }

  @Test(dependsOnMethods = {"testOldVLMAfterChangeShouldBeInBeginningOfList"})
  public void testAddNewVLMShouldBeInBeginningOfList() {
    org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity vlm = createVendorLicenseModel("test_vlm3", "desc", "icon");
    String vlm5Id = vendorLicenseManager.createVendorLicenseModel(vlm, USER3).getId();

    assertVLMInWantedLocationInVSPList(vlm5Id, 0, USER3);
  }

  @Test(dependsOnMethods = {"testAddNewVLMShouldBeInBeginningOfList"})
  public void testVLMInBeginningOfListAfterCheckin() {
    vendorLicenseManager.checkin(vlm4Id, USER3);
    assertVLMInWantedLocationInVSPList(vlm4Id, 0, USER3);
  }

  @Test(dependsOnMethods = {"testVLMInBeginningOfListAfterCheckin"})
  public void testVLMInBeginningOfListAfterCheckout() {
    vendorLicenseManager.checkin(vlm3Id, USER3);
    assertVLMInWantedLocationInVSPList(vlm3Id, 0, USER3);

    vendorLicenseManager.checkout(vlm4Id, USER3);
    assertVLMInWantedLocationInVSPList(vlm4Id, 0, USER3);
  }

  @Test(dependsOnMethods = {"testVLMInBeginningOfListAfterCheckout"})
  public void testVLMInBeginningOfListAfterUndoCheckout() {
    vendorLicenseManager.checkout(vlm3Id, USER3);
    vendorLicenseManager.undoCheckout(vlm3Id, USER3);
    assertVLMInWantedLocationInVSPList(vlm3Id, 0, USER3);
  }

  private void assertVLMInWantedLocationInVSPList(String vlmId, int location, String user) {
    List<org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel> vlmList =
        (List<org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel>) vendorLicenseManager
            .listVendorLicenseModels(null, user);
    Assert.assertEquals(vlmList.get(location).getVendorLicenseModel().getId(), vlmId);
  }


}
