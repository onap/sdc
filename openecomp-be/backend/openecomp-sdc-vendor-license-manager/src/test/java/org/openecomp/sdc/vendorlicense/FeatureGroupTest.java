package org.openecomp.sdc.vendorlicense;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.impl.VendorLicenseManagerImpl;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.utilities.CommonMethods;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

public class FeatureGroupTest {
  protected static final Version VERSION01 = new Version(0, 1);
  protected static final String USER1 = "FeatureGroupTest_User1";
  protected static VendorLicenseManager vendorLicenseManager = new VendorLicenseManagerImpl();
  protected static VendorLicenseFacade vendorLicenseFacade = org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacadeFactory
      .getInstance().createInterface();


  @Test
  public void testListFeatureGroups() throws Exception {
    String vlmId = vendorLicenseFacade.createVendorLicenseModel(VendorLicenseModelTest.createVendorLicenseModel("vlmId_" + CommonMethods.nextUuId(), "vlm2Id desc", "icon2"), USER1).getId();
    org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity
        fg22 = LicenseAgreementTest.createFeatureGroup(vlmId, VERSION01, "fg2", "FG2", "FG2 desc", null, null);
    String fg22Id = vendorLicenseManager.createFeatureGroup(fg22, USER1).getId();
    org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity
        fg33 = LicenseAgreementTest.createFeatureGroup(vlmId, VERSION01, "fg3", "FG3", "FG3 desc", null, null);
    String fg33Id = vendorLicenseManager.createFeatureGroup(fg33, USER1).getId();

    Collection<org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity> featureGroupEntities = vendorLicenseManager.listFeatureGroups(vlmId, null, USER1);

    Assert.assertEquals(featureGroupEntities.size(), 2);
    Set<String> actualIds = new HashSet<>();
    for (org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity featureGroupEntity : featureGroupEntities) {
      actualIds.add(featureGroupEntity.getId());
    }

    Set<String> expectedIds = new HashSet<>();
    expectedIds.add(fg22Id);
    expectedIds.add(fg33Id);
    for (String id : actualIds) {
      Assert.assertTrue(expectedIds.contains(id));
    }

  }

  @Test
  public void testCreateFeatureGroup() throws Exception {
    String testName = "testCreateFeatureGroup";
    String vlmId = vendorLicenseFacade.createVendorLicenseModel(VendorLicenseModelTest.createVendorLicenseModel(testName + CommonMethods.nextUuId(), testName, "icon1"), USER1).getId();
    Set<org.openecomp.sdc.vendorlicense.dao.types.OperationalScope> opScopeChoices = new HashSet<>();
    opScopeChoices.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Other);
    opScopeChoices.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Data_Center);
    opScopeChoices.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Network_Wide);
    org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity
        ep = EntitlementPoolTest.createEntitlementPool(vlmId, VERSION01, "EP1" + CommonMethods.nextUuId(), "EP1 dec", 80, org.openecomp.sdc.vendorlicense.dao.types.ThresholdUnit.Absolute, org.openecomp.sdc.vendorlicense.dao.types.EntitlementMetric.Core, null, "inc1", org.openecomp.sdc.vendorlicense.dao.types.AggregationFunction.Other, "agg func1", opScopeChoices, null, org.openecomp.sdc.vendorlicense.dao.types.EntitlementTime.Hour, null, "sku1");
    String epId = vendorLicenseManager.createEntitlementPool(ep, USER1).getId();
    Set<org.openecomp.sdc.vendorlicense.dao.types.OperationalScope> opScopeChoicesLKG = new HashSet<>();
    opScopeChoicesLKG.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.CPU);
    opScopeChoicesLKG.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.VM);
    opScopeChoicesLKG.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Availability_Zone);
    opScopeChoicesLKG.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Data_Center);

    org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity
        lkg = LicenseKeyGroupTest.createLicenseKeyGroup(vlmId, VERSION01, "LKG1", "LKG1 dec", org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyType.One_Time, new org.openecomp.sdc.vendorlicense.dao.types.MultiChoiceOrOther<>(opScopeChoicesLKG, null));
    String lkgId = vendorLicenseManager.createLicenseKeyGroup(lkg, USER1).getId();
    lkg.setId(lkgId);
    org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity
        fg1 = createFGForTest(vlmId, "created" + CommonMethods.nextUuId(), Collections.singleton(epId), Collections.singleton(lkgId));
    org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity
        fg1FromDB = vendorLicenseManager.getFeatureGroupModel(fg1, USER1).getFeatureGroup();
    Assert.assertTrue(fg1FromDB.equals(fg1));
  }


  @Test
  public void testCreateWithExistingName_negative() {
    String testName = "createExistingName";
    String vlmId = vendorLicenseFacade.createVendorLicenseModel(VendorLicenseModelTest.createVendorLicenseModel(testName + CommonMethods.nextUuId(), testName, "icon1"), USER1).getId();
    createFGForTest(vlmId, "created", Collections.emptySet(), Collections.emptySet());
    try {
      org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity
          created = LicenseAgreementTest.createFeatureGroup(vlmId, null, "created", "created", "created desc", Collections.emptySet(), Collections.emptySet());
      vendorLicenseManager.createFeatureGroup(created, USER1);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), UniqueValueUtil.UNIQUE_VALUE_VIOLATION);
    }
  }

  private org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity createFGForTest(String vlmId, String fgName, Set<String> epIds, Set<String> lkgIds) {
    org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity
        created = LicenseAgreementTest.createFeatureGroup(vlmId, null, null, fgName, "created desc", epIds, lkgIds);
    return vendorLicenseManager.createFeatureGroup(created, USER1);
  }

  @Test
  public void testUpdateFeatureGroup_addEP_andGET() throws Exception {
    String testName = "testUpdateFeatureGroup_addEP_andGET";
    String vlmId = vendorLicenseFacade.createVendorLicenseModel(VendorLicenseModelTest.createVendorLicenseModel(testName + CommonMethods.nextUuId(), testName, "icon1"), USER1).getId();

    org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity
        fg5 = LicenseAgreementTest.createFeatureGroup(vlmId, VERSION01, "id" + CommonMethods.nextUuId(), "created" + CommonMethods.nextUuId(), "created desc", null, null);
    vendorLicenseManager.createFeatureGroup(fg5, USER1).getId();


    Set<org.openecomp.sdc.vendorlicense.dao.types.OperationalScope> opScopeChoices = new HashSet<>();
    opScopeChoices.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Other);
    opScopeChoices.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Data_Center);

    org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity
        epToAdd = EntitlementPoolTest.createEntitlementPool(vlmId, VERSION01, "epToAdd", "epToAdd dec", 80, org.openecomp.sdc.vendorlicense.dao.types.ThresholdUnit.Absolute, org.openecomp.sdc.vendorlicense.dao.types.EntitlementMetric.Core, null, "inc1", org.openecomp.sdc.vendorlicense.dao.types.AggregationFunction.Other, "agg func1", opScopeChoices, null, org.openecomp.sdc.vendorlicense.dao.types.EntitlementTime.Hour, null, "sku1");
    String epToAddId = vendorLicenseManager.createEntitlementPool(epToAdd, USER1).getId();

    vendorLicenseManager.updateFeatureGroup(fg5, null, null, CommonMethods.toSingleElementSet(epToAddId), null, USER1);
    org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupModel
        updatedFG = vendorLicenseManager.getFeatureGroupModel(fg5, USER1);
    Set<org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity> updatedEPs = updatedFG.getEntitlementPools();

    epToAdd.setReferencingFeatureGroups(CommonMethods.toSingleElementSet(fg5.getId()));

    Assert.assertEquals(updatedEPs.size(), 1);
    for (org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity updatedEP : updatedEPs) {
      Assert.assertTrue(updatedEP.getReferencingFeatureGroups().contains(fg5.getId()));
      Assert.assertEquals(updatedEP.getId(), epToAddId);
    }
  }

  @Test
  public void testUpdateFeatureGroup_removeLKG_andGET() throws Exception {
    String testName = "testUpdateFeatureGroup_removeLKG_andGET";
    String vlmId = vendorLicenseFacade.createVendorLicenseModel(VendorLicenseModelTest.createVendorLicenseModel(testName + CommonMethods.nextUuId(), testName, "icon1"), USER1).getId();

    Set<org.openecomp.sdc.vendorlicense.dao.types.OperationalScope> opScopeChoicesLKG = new HashSet<>();
    opScopeChoicesLKG.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.CPU);
    opScopeChoicesLKG.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.VM);
    opScopeChoicesLKG.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Availability_Zone);
    opScopeChoicesLKG.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Data_Center);
    org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity
        lkg = LicenseKeyGroupTest.createLicenseKeyGroup(vlmId, VERSION01, "lkg" + CommonMethods.nextUuId(), "lkg desc", org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyType.Unique, new org.openecomp.sdc.vendorlicense.dao.types.MultiChoiceOrOther<>(opScopeChoicesLKG, null));
    String lkgId = vendorLicenseManager.createLicenseKeyGroup(lkg, USER1).getId();
    lkg.setId(lkgId);

    org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity
        lkg_1 = LicenseKeyGroupTest.createLicenseKeyGroup(vlmId, VERSION01, "lkg" + CommonMethods.nextUuId(), "lkg_1 desc", org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyType.Unique, new org.openecomp.sdc.vendorlicense.dao.types.MultiChoiceOrOther<>(opScopeChoicesLKG, null));
    String lkgId_1 = vendorLicenseManager.createLicenseKeyGroup(lkg_1, USER1).getId();
    lkg.setId(lkgId);

    Set<org.openecomp.sdc.vendorlicense.dao.types.OperationalScope> opScopeChoices = new HashSet<>();
    opScopeChoices.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Other);
    opScopeChoices.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Data_Center);
    opScopeChoices.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Network_Wide);
    org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity
        ep = EntitlementPoolTest.createEntitlementPool(vlmId, VERSION01, "EP1" + CommonMethods.nextUuId(), "EP1 dec", 80, org.openecomp.sdc.vendorlicense.dao.types.ThresholdUnit.Absolute, org.openecomp.sdc.vendorlicense.dao.types.EntitlementMetric.Core, null, "inc1", org.openecomp.sdc.vendorlicense.dao.types.AggregationFunction.Other, "agg func1", opScopeChoices, null, org.openecomp.sdc.vendorlicense.dao.types.EntitlementTime.Hour, null, "sku1");
    String epId = vendorLicenseManager.createEntitlementPool(ep, USER1).getId();

    Set<String> lkgs = new HashSet<>();
    lkgs.add(lkgId);
    lkgs.add(lkgId_1);

    org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity
        fg = LicenseAgreementTest.createFeatureGroup(vlmId, VERSION01, "fg11" + CommonMethods.nextUuId(), "FG1", "FG1 desc", CommonMethods.toSingleElementSet(epId), lkgs);
    String fgId = vendorLicenseManager.createFeatureGroup(fg, USER1).getId();
    vendorLicenseManager.updateFeatureGroup(fg, null, CommonMethods.toSingleElementSet(lkgId), null, null, USER1);

    org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupModel
        featureGroup = vendorLicenseManager.getFeatureGroupModel(fg, USER1);
    Set<org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity> licenseKeyGroups = featureGroup.getLicenseKeyGroups();
    Assert.assertEquals(licenseKeyGroups.size(), 1);
    List<String> lkgIds = new ArrayList<>();
    for (org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity licenseKeyGroup : licenseKeyGroups) {
      lkgIds.add(licenseKeyGroup.getId());
    }

    Assert.assertTrue(lkgIds.contains(lkgId_1));
    Assert.assertFalse(lkgIds.contains(lkgId));

  }


  @Test
  public void testDeleteFeatureGroup() throws Exception {
    String testName = "testDeleteFeatureGroup";
    String vlmId = vendorLicenseFacade.createVendorLicenseModel(VendorLicenseModelTest.createVendorLicenseModel(testName + CommonMethods.nextUuId(), testName, "icon1"), USER1).getId();

    org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity
        fg1 = createFGForTest(vlmId, "new", Collections.emptySet(), Collections.emptySet());
    org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity
        fg2 = createFGForTest(vlmId, "newer", Collections.emptySet(), Collections.emptySet());
    Collection<org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity> featureGroupEntities = vendorLicenseManager.listFeatureGroups(vlmId, null, USER1);
    Assert.assertEquals(featureGroupEntities.size(), 2); //precondition

    vendorLicenseManager.deleteFeatureGroup(fg1, USER1);
    Assert.assertEquals(vendorLicenseManager.listFeatureGroups(vlmId, null, USER1).size(), 1);


  }


}