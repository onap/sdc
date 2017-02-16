package org.openecomp.sdc.vendorlicense;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorlicense.dao.LicenseKeyGroupDao;
import org.openecomp.sdc.vendorlicense.dao.LicenseKeyGroupDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyType;
import org.openecomp.sdc.vendorlicense.dao.types.MultiChoiceOrOther;
import org.openecomp.sdc.vendorlicense.dao.types.OperationalScope;
import org.openecomp.sdc.vendorlicense.impl.VendorLicenseManagerImpl;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.utilities.CommonMethods;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LicenseKeyGroupTest {

  public static final String LKG1_NAME = "LKG1 name";
  private static final Version VERSION01 = new Version(0, 1);
  private static final String USER1 = "user1";
  public static String vlm1Id;
  public static String vlm2Id;
  private static VendorLicenseManager vendorLicenseManager = new VendorLicenseManagerImpl();
  private static LicenseKeyGroupDao licenseKeyGroupDao;
  private static NoSqlDb noSqlDb;
  private static String lkg1Id;
  private static String lkg2Id;

  public static LicenseKeyGroupEntity createLicenseKeyGroup(String vlmId, Version version,
                                                            String name, String desc,
                                                            LicenseKeyType type,
                                                            MultiChoiceOrOther<OperationalScope> operationalScope) {
    LicenseKeyGroupEntity licenseKeyGroup = new LicenseKeyGroupEntity();
    licenseKeyGroup.setVendorLicenseModelId(vlmId);
    licenseKeyGroup.setVersion(version);
    licenseKeyGroup.setName(name);
    licenseKeyGroup.setDescription(desc);
    licenseKeyGroup.setType(type);
    licenseKeyGroup.setOperationalScope(operationalScope);
    return licenseKeyGroup;
  }

  @BeforeClass
  private void init() {
    licenseKeyGroupDao = LicenseKeyGroupDaoFactory.getInstance().createInterface();
    noSqlDb = NoSqlDbFactory.getInstance().createInterface();

    vlm1Id = vendorLicenseManager.createVendorLicenseModel(VendorLicenseModelTest
        .createVendorLicenseModel("vendor1 name " + CommonMethods.nextUuId(), "vlm1Id dec",
            "icon1"), USER1).getId();
    vlm2Id = vendorLicenseManager.createVendorLicenseModel(VendorLicenseModelTest
            .createVendorLicenseModel("vendor2 name " + CommonMethods.nextUuId(), "vlm2 dec", "icon2"),
        USER1).getId();
  }

  @Test
  public void createTest() {
    lkg1Id = testCreate(vlm1Id, LKG1_NAME);
  }

  private String testCreate(String vlmId, String name) {
    Set<OperationalScope> opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.CPU);
    opScopeChoices.add(OperationalScope.VM);
    opScopeChoices.add(OperationalScope.Tenant);
    opScopeChoices.add(OperationalScope.Data_Center);
    LicenseKeyGroupEntity lkg1 =
        createLicenseKeyGroup(vlmId, VERSION01, name, "LKG1 dec", LicenseKeyType.One_Time,
            new MultiChoiceOrOther<>(opScopeChoices, null));
    String lkg1Id = vendorLicenseManager.createLicenseKeyGroup(lkg1, USER1).getId();
    lkg1.setId(lkg1Id);

    LicenseKeyGroupEntity loadedLkg1 = licenseKeyGroupDao.get(lkg1);
    Assert.assertTrue(loadedLkg1.equals(lkg1));
    return lkg1Id;
  }

  @Test(dependsOnMethods = {"createTest"})
  public void testCreateWithExistingName_negative() {
    try {
      LicenseKeyGroupEntity lkg1 =
          createLicenseKeyGroup(vlm1Id, VERSION01, LKG1_NAME, "LKG1 dec", LicenseKeyType.One_Time,
              new MultiChoiceOrOther<>(Collections.singleton(OperationalScope.Other),
                  "other op scope"));
      vendorLicenseManager.createLicenseKeyGroup(lkg1, USER1).getId();
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), UniqueValueUtil.UNIQUE_VALUE_VIOLATION);
    }
  }

  @Test(dependsOnMethods = {"createTest"})
  public void testCreateWithExistingNameUnderOtherVlm() {
    testCreate(vlm2Id, LKG1_NAME);
  }

  @Test(dependsOnMethods = {"testCreateWithExistingName_negative"})
  public void updateAndGetTest() {
    LicenseKeyGroupEntity lkg1 =
        licenseKeyGroupDao.get(new LicenseKeyGroupEntity(vlm1Id, VERSION01, lkg1Id));
    Set<OperationalScope> opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Other);
    lkg1.setOperationalScope(new MultiChoiceOrOther<>(opScopeChoices, "op scope1 updated"));
    lkg1.setDescription("LKG1 dec updated");

    vendorLicenseManager.updateLicenseKeyGroup(lkg1, USER1);

    LicenseKeyGroupEntity loadedLkg1 = vendorLicenseManager.getLicenseKeyGroup(lkg1, USER1);
    Assert.assertTrue(loadedLkg1.equals(lkg1));

  }

  @Test(dependsOnMethods = {"updateAndGetTest"})
  public void listTest() {
    Set<OperationalScope> opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Network_Wide);
    LicenseKeyGroupEntity lkg2 =
        createLicenseKeyGroup(vlm1Id, VERSION01, "LKG2", "LKG2 dec", LicenseKeyType.Universal,
            new MultiChoiceOrOther<>(opScopeChoices, null));
    lkg2Id = vendorLicenseManager.createLicenseKeyGroup(lkg2, USER1).getId();
    lkg2.setId(lkg2Id);

    Collection<LicenseKeyGroupEntity> loadedLkgs =
        vendorLicenseManager.listLicenseKeyGroups(vlm1Id, null, USER1);
    Assert.assertEquals(loadedLkgs.size(), 2);
    for (LicenseKeyGroupEntity loadedLkg : loadedLkgs) {
      if (lkg2Id.equals(loadedLkg.getId())) {
        Assert.assertTrue(loadedLkg.equals(lkg2));
      }
    }
  }

  @Test(dependsOnMethods = {"listTest"})
  public void deleteTest() {
    vendorLicenseManager
        .deleteLicenseKeyGroup(new LicenseKeyGroupEntity(vlm1Id, VERSION01, lkg1Id), USER1);

    LicenseKeyGroupEntity loadedLkg1 =
        licenseKeyGroupDao.get(new LicenseKeyGroupEntity(vlm1Id, VERSION01, lkg1Id));
    Assert.assertEquals(loadedLkg1, null);

    Collection<LicenseKeyGroupEntity> loadedLkgs =
        licenseKeyGroupDao.list(new LicenseKeyGroupEntity(vlm1Id, VERSION01, null));
    Assert.assertEquals(loadedLkgs.size(), 1);
    Assert.assertEquals(loadedLkgs.iterator().next().getId(), lkg2Id);
  }

  @Test(dependsOnMethods = "deleteTest")
  public void testCreateWithRemovedName() {
    testCreate(vlm1Id, LKG1_NAME);
  }
}

