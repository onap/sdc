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

package org.openecomp.sdc.vendorlicense.impl;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.vendorlicense.VendorLicenseConstants;
import org.openecomp.sdc.vendorlicense.dao.EntitlementPoolDao;
import org.openecomp.sdc.vendorlicense.dao.FeatureGroupDao;
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDao;
import org.openecomp.sdc.vendorlicense.dao.LicenseKeyGroupDao;
import org.openecomp.sdc.vendorlicense.dao.LimitDao;
import org.openecomp.sdc.vendorlicense.dao.VendorLicenseModelDao;
import org.openecomp.sdc.vendorlicense.dao.types.ChoiceOrOther;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseTerm;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class LicenseAgreementTest {

  private static final String USER1 = "TestUser1";
  private static final String USER2 = "TestUser2";

  private static String vlm1_id = "vlm1_id";
  private static String vlm2_id = "vlm2_id";
  private static String la1_id = "la1_id";
  private static String la2_id = "la2_id";
  private static String fg1_id = "fg1_id";
  private static String fg2_id = "fg2_id";
  public static final Version VERSION01 = new Version(0, 1);
  private static final Version VERSION10 = new Version(1, 0);

  @Mock
  private VersioningManager versioningManagerMcok;
  @Mock
  private VendorLicenseFacade vendorLicenseFacadeMcok;
  @Mock
  private VendorLicenseModelDao vendorLicenseModelDaoMcok;
  @Mock
  private LicenseAgreementDao licenseAgreementDaoMcok;
  @Mock
  private FeatureGroupDao featureGroupDaoMcok;
  @Mock
  private EntitlementPoolDao entitlementPoolDaoMcok;
  @Mock
  private LicenseKeyGroupDao licenseKeyGroupDaoMcok;
  @Mock
  private LimitDao limitDaoMcok;


  @Spy
  @InjectMocks
  private VendorLicenseManagerImpl vendorLicenseManager;


  @Captor
  private ArgumentCaptor<ActivityLogEntity> activityLogEntityArg;


  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

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


  @Test
  public void listLicenseAgreementsTest() {

    LicenseAgreementEntity la =
        new LicenseAgreementEntity(vlm1_id, VERSION01, null); // TODO: 8/13/2017

    doReturn(Arrays.asList(
        createLicenseAgreement(vlm1_id, VERSION01, la1_id, "LA1", "LA1 " +
                "desc", "RequirementsAndConstrains2", new ChoiceOrOther<>(LicenseTerm.Unlimited, null),
            "fg1"),
        createLicenseAgreement(vlm1_id, VERSION01, la2_id, "LA2", "LA2 desc",
            "RequirementsAndConstrains2", new ChoiceOrOther<>(LicenseTerm.Unlimited, null),
            "fg2")))
        .when(licenseAgreementDaoMcok).list(la);

    Collection<LicenseAgreementEntity> LAs =
        vendorLicenseManager.listLicenseAgreements(vlm1_id, VERSION01);
    Assert.assertEquals(LAs.size(), 2);
    LAs.forEach(
        licseAgreement -> Assert.assertTrue(licseAgreement.getId().matches(la1_id + "|" + la2_id)));
  }

  @Test
  public void testListLicenseAgreementsWhenNone() {

    LicenseAgreementEntity la =
        new LicenseAgreementEntity(vlm1_id, VERSION01, null); // TODO: 8/13/2017

    doReturn(new ArrayList<LicenseAgreementEntity>())
        .when(licenseAgreementDaoMcok).list(la);

    Collection<LicenseAgreementEntity> LAs =
        vendorLicenseManager.listLicenseAgreements(vlm1_id, VERSION01);

    verify(licenseAgreementDaoMcok).list(la);
    Assert.assertEquals(LAs.size(), 0);
  }


  @Test
  public void testCreateLicenseAgreement() {

    LicenseAgreementEntity licenseAgreementEntity = new LicenseAgreementEntity(vlm1_id, VERSION01,
        la2_id);

    doReturn(licenseAgreementEntity).when(vendorLicenseFacadeMcok).createLicenseAgreement
        (licenseAgreementEntity);

    vendorLicenseManager.createLicenseAgreement(licenseAgreementEntity);

    verify(vendorLicenseFacadeMcok).createLicenseAgreement(licenseAgreementEntity);
  }

  @Test
  public void testUpdateLicenseAgreement() {
    LicenseAgreementEntity existingLA = new LicenseAgreementEntity(vlm1_id, VERSION01, la1_id);

    existingLA.setFeatureGroupIds(new HashSet<String>());

    doReturn(existingLA).when(licenseAgreementDaoMcok).get(existingLA);

    Set<String> removedFGs = new HashSet<>();
    Set<String> addedFGs = new HashSet<>();
    addedFGs.add(fg1_id);
    addedFGs.add(fg2_id);
    FeatureGroupEntity fg1 = new FeatureGroupEntity(vlm1_id, VERSION01, fg1_id);
    FeatureGroupEntity fg2 = new FeatureGroupEntity(vlm1_id, VERSION01, fg2_id);
    doReturn(fg1).when(featureGroupDaoMcok).get(fg1);
    doReturn(fg2).when(featureGroupDaoMcok).get(fg2);
    doNothing().when(vendorLicenseManager).updateUniqueName(anyObject(), anyObject(), anyObject(),
        anyObject(), anyObject());

    vendorLicenseManager.updateLicenseAgreement(existingLA, addedFGs, removedFGs);

    verify(licenseAgreementDaoMcok)
        .updateColumnsAndDeltaFeatureGroupIds(existingLA, addedFGs, removedFGs);
    verify(vendorLicenseManager).addFeatureGroupsToLicenseAgreementRef(addedFGs, existingLA);
    verify(vendorLicenseManager).removeFeatureGroupsToLicenseAgreementRef(removedFGs, existingLA);

  }

  @Test
  public void deleteLicenseAgreementsTest() {
    LicenseAgreementEntity existingLA = new LicenseAgreementEntity(vlm1_id, VERSION01, la1_id);
    existingLA.setName("LA");
    existingLA.setFeatureGroupIds(new HashSet<>());

    doReturn(existingLA).when(licenseAgreementDaoMcok).get(anyObject());

    doNothing().when(vendorLicenseManager).deleteUniqueName(VendorLicenseConstants.UniqueValues
        .LICENSE_AGREEMENT_NAME, vlm1_id, VERSION01.toString(), existingLA.getName());

    vendorLicenseManager.deleteLicenseAgreement(vlm1_id, VERSION01, la1_id);

    verify(licenseAgreementDaoMcok).delete(existingLA);
    verify(vendorLicenseManager).removeFeatureGroupsToLicenseAgreementRef(existingLA
        .getFeatureGroupIds(), existingLA);
  }

  @Test
  public void testGetLicenseAgreement() {
    vendorLicenseManager.getLicenseAgreementModel(vlm1_id, VERSION01, la1_id);
    verify(vendorLicenseFacadeMcok).getLicenseAgreementModel(vlm1_id, VERSION01, la1_id);
  }

/*
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

    vendorLicenseManager.deleteFeatureGroup(created);
    LicenseAgreementEntity afterDeletingFG = licenseAgreementDao.get(licenseAgreement);
    Assert.assertEquals(afterDeletingFG.getFeatureGroupIds().size(), 1);
    Assert.assertTrue(afterDeletingFG.getFeatureGroupIds().contains("fg2"));
  }

  */
}

