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

/*
package org.openecomp.sdc.vendorlicense;

import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorlicense.dao.FeatureGroupDao;
import org.openecomp.sdc.vendorlicense.dao.FeatureGroupDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDao;
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.types.ChoiceOrOther;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseTerm;
import org.openecomp.sdc.vendorlicense.impl.VendorLicenseManagerImpl;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class LicenseAgreementTest {
  private static final Version VERSION01 = new Version(0, 1);
  private static final String USER1 = "user1";
  private static final String LA1_NAME = "LA1 Name";

  private static VendorLicenseManager vendorLicenseManager = new VendorLicenseManagerImpl();
  private static FeatureGroupDao featureGroupDao;
  private static LicenseAgreementDao licenseAgreementDao;

  private static String vlm1Id;
  private static String vlm2Id;
  private static String la1Id;
  private static String la2Id;

  public static LicenseAgreementEntity createLicenseAgreement(String vlmId, Version version,
                                                              String id, String name, String desc,
                                                              String requirementsAndConstrains,
                                                              ChoiceOrOther<LicenseTerm> term,
                                                              String... fgIds) {
    LicenseAgreementEntity la = new LicenseAgreementEntity();
    la.setVendorLicenseModelId(vlmId);
    la.setVersion(version);
    la.setId(id);
    la.setName(name);
    la.setDescription(desc);
    la.setLicenseTerm(term);
    la.setRequirementsAndConstrains(requirementsAndConstrains);
    for (String fgId : fgIds) {
      la.getFeatureGroupIds().add(fgId);
    }
    return la;
  }

  public static FeatureGroupEntity createFeatureGroup(String vendorId, Version version, String id,
                                                      String name, String description,
                                                      Set<String> entitlementPoolIds,
                                                      Set<String> licenseKeyGroupIds) {
    FeatureGroupEntity featureGroup = new FeatureGroupEntity();
    featureGroup.setVendorLicenseModelId(vendorId);
    featureGroup.setVersion(version);
    featureGroup.setId(id);
    featureGroup.setName(name);
    featureGroup.setDescription(description);
    featureGroup.setEntitlementPoolIds(entitlementPoolIds);
    featureGroup.setLicenseKeyGroupIds(licenseKeyGroupIds);
    return featureGroup;
  }

  @BeforeClass
  private void init() {
    licenseAgreementDao = LicenseAgreementDaoFactory.getInstance().createInterface();
    featureGroupDao = FeatureGroupDaoFactory.getInstance().createInterface();
    vlm1Id = vendorLicenseManager.createVendorLicenseModel(VendorLicenseModelTest
            .createVendorLicenseModel("vendor1 name " + CommonMethods.nextUuId(), "vlm1 dec", "icon1"),
        USER1).getId();
    vlm2Id = vendorLicenseManager.createVendorLicenseModel(VendorLicenseModelTest
            .createVendorLicenseModel("vendor2 name " + CommonMethods.nextUuId(), "vlm2 dec", "icon2"),
        USER1).getId();
  }

  @Test
  public void createLicenseAgreementTest() {
    la1Id = testCreate(vlm1Id, LA1_NAME);
  }

  private String testCreate(String vlmId, String name) {
    FeatureGroupEntity
        fg1 = createFeatureGroup(vlmId, VERSION01, "fg11", "FG1", "FG1 desc", null, null);
    featureGroupDao.create(fg1);

    LicenseAgreementEntity la1 = createLicenseAgreement(vlmId, VERSION01, null, name, "LA1 desc",
        "RequirementsAndConstrains1", new ChoiceOrOther<>(
            LicenseTerm.Unlimited, null), "fg11");
    la1 = vendorLicenseManager.createLicenseAgreement(la1, USER1);
    String la1Id = la1.getId();

    LicenseAgreementEntity loadedLa1 = licenseAgreementDao.get(la1);
    Assert.assertTrue(loadedLa1.equals(la1));
    return la1Id;
  }

  @Test(dependsOnMethods = {"createLicenseAgreementTest"})
  public void testCreateWithExistingName_negative() {
    try {
      LicenseAgreementEntity la1 =
          createLicenseAgreement(vlm1Id, VERSION01, null, LA1_NAME, "LA1 desc",
              "RequirementsAndConstrains1", new ChoiceOrOther<>(LicenseTerm.Unlimited, null),
              "fg11");
      vendorLicenseManager.createLicenseAgreement(la1, USER1);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), UniqueValueUtil.UNIQUE_VALUE_VIOLATION);
    }
  }

  @Test(dependsOnMethods = {"createLicenseAgreementTest"})
  public void testCreateWithExistingNameUnderOtherVlm() {
    testCreate(vlm2Id, LA1_NAME);
  }

  @Test(dependsOnMethods = {"testCreateWithExistingName_negative"})
  public void updateLicenseAgreementTest() {
    FeatureGroupEntity fg2 =
        createFeatureGroup(vlm1Id, VERSION01, "fg2", "FG2", "FG2 desc", null, null);
    featureGroupDao.create(fg2);

    FeatureGroupEntity fg3 =
        createFeatureGroup(vlm1Id, VERSION01, "fg3", "FG3", "FG3 desc", null, null);
    featureGroupDao.create(fg3);

    LicenseAgreementEntity la1 =
        licenseAgreementDao.get(new LicenseAgreementEntity(vlm1Id, VERSION01, la1Id));
    la1.setDescription("LA1 desc updated");
    la1.setLicenseTerm(new ChoiceOrOther<>(LicenseTerm.Other, "bla bla term"));
    la1.getFeatureGroupIds().add("fg2");
    la1.getFeatureGroupIds().add("fg3");
    la1.getFeatureGroupIds().remove("fg11");

    Set<String> addedFeatureGroupIds = new HashSet<>();
    addedFeatureGroupIds.add("fg2");
    addedFeatureGroupIds.add("fg3");

    Set<String> removedFeatureGroupIds = new HashSet<>();
    removedFeatureGroupIds.add("fg11");

    vendorLicenseManager
        .updateLicenseAgreement(la1, addedFeatureGroupIds, removedFeatureGroupIds, USER1);

    LicenseAgreementEntity loadedLa1 =
        licenseAgreementDao.get(new LicenseAgreementEntity(vlm1Id, VERSION01, la1Id));
    Assert.assertTrue(loadedLa1.equals(la1));

  }

  @Test(dependsOnMethods = {"updateLicenseAgreementTest"})
  public void listLicenseAgreementsTest() {
    LicenseAgreementEntity la2 = createLicenseAgreement(vlm1Id, VERSION01, null, "LA2", "LA2 desc",
        "RequirementsAndConstrains2", new ChoiceOrOther<>(LicenseTerm.Unlimited, null), "fg2");
    la2 = vendorLicenseManager.createLicenseAgreement(la2, USER1);
    la2Id = la2.getId();

    Collection<LicenseAgreementEntity> loadedLas =
        vendorLicenseManager.listLicenseAgreements(vlm1Id, null, USER1);
    Assert.assertEquals(loadedLas.size(), 2);
    boolean la2Exists = false;
    for (LicenseAgreementEntity loadedLa : loadedLas) {
      if (la2Id.equals(loadedLa.getId())) {
        Assert.assertTrue(loadedLa.equals(la2));
        la2Exists = true;
      }
    }

    Assert.assertTrue(la2Exists);
  }

  @Test(dependsOnMethods = {"listLicenseAgreementsTest"})
  public void featureGroupDeletedLicenseAgreementUpdated() {
    LicenseAgreementEntity licenseAgreement =
        createLicenseAgreement(vlm1Id, VERSION01, "laId", "LA2", "LA2 desc",
            "RequirementsAndConstrains2", new ChoiceOrOther<>(LicenseTerm.Unlimited, null), "fg2");
    licenseAgreementDao.create(licenseAgreement);
    String featureGroupId = "FeatureGroupId";
    FeatureGroupEntity created =
        createFeatureGroup(vlm1Id, VERSION01, "fg11", "FG1", "FG1 desc", null, null);
    featureGroupDao.create(created);
    featureGroupDao.addReferencingLicenseAgreement(created, licenseAgreement.getId());

    vendorLicenseManager.deleteFeatureGroup(created, USER1);
    LicenseAgreementEntity afterDeletingFG = licenseAgreementDao.get(licenseAgreement);
    Assert.assertEquals(afterDeletingFG.getFeatureGroupIds().size(), 1);
    Assert.assertTrue(afterDeletingFG.getFeatureGroupIds().contains("fg2"));
  }

  @Test(dependsOnMethods = {"listLicenseAgreementsTest"})
  public void deleteLicenseAgreementsTest() {
    vendorLicenseManager.deleteLicenseAgreement(vlm1Id, null, la1Id, USER1);

    LicenseAgreementEntity loadedLa1 =
        licenseAgreementDao.get(new LicenseAgreementEntity(vlm1Id, VERSION01, la1Id));
    Assert.assertEquals(loadedLa1, null);

    Collection<LicenseAgreementEntity> loadedLas =
        licenseAgreementDao.list(new LicenseAgreementEntity(vlm1Id, VERSION01, null));
    Assert.assertEquals(loadedLas.size(), 1);
    Assert.assertEquals(loadedLas.iterator().next().getId(), la2Id);
  }

  @Test(dependsOnMethods = "deleteLicenseAgreementsTest")
  public void testCreateWithRemovedName() {
    testCreate(vlm1Id, LA1_NAME);
  }
}

*/
