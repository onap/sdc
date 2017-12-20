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

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.vendorlicense.VendorLicenseConstants;
import org.openecomp.sdc.vendorlicense.dao.EntitlementPoolDao;
import org.openecomp.sdc.vendorlicense.dao.FeatureGroupDao;
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDao;
import org.openecomp.sdc.vendorlicense.dao.LicenseKeyGroupDao;
import org.openecomp.sdc.vendorlicense.dao.LimitDao;
import org.openecomp.sdc.vendorlicense.dao.VendorLicenseModelDao;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/**
 * Created by KATYR on 4/10/2016
 */

public class FeatureGroupTest {
  //JUnit Test Cases using Mockito
  private static final Version VERSION01 = new Version(0, 1);
  private static final Version VERSION10 = new Version(1, 0);
  private final String FG1_NAME = "FG1 name";
  private static final String USER1 = "TestUser1";
  private static final String USER2 = "TestUser2";

  private static String vlm1_id = "vlm1_id";
  private static String vlm2_id = "vlm2_id";
  private static String lkg1_id = "lkg1_id";
  private static String lkg2_id = "lkg2_id";
  private static String fg1_id = "fg1_id";
  private static String fg2_id = "fg2_id";
  private static String ep1_id = "ep1_id";
  private static String ep2_id = "ep2_id";

  @Mock
  private VendorLicenseFacade vendorLicenseFacadeMcok;

  @Mock
  private LimitDao limitDaoMcok;

  @Mock
  private VendorLicenseModelDao vendorLicenseModelDao;

  @Mock
  private LicenseAgreementDao licenseAgreementDao;

  @Mock
  private FeatureGroupDao featureGroupDao;

  @Mock
  private EntitlementPoolDao entitlementPoolDao;

  @Mock
  private LicenseKeyGroupDao licenseKeyGroupDao;

  @Mock
  private VersioningManager versioningManager;

  @InjectMocks
  @Spy
  private VendorLicenseManagerImpl vendorLicenseManagerImpl;

  public FeatureGroupEntity updateFeatureGroup(String vlmId, Version version, String id,
                                               String name, String desc,
                                               String partNumber,
                                               String manufacturerReferenceNumber, Set<String>
                                                   licenseKeyGroupIds,
                                               Set<String> entitlementPoolIds, Set<String>
                                                   referencingLicenseAgreements) {
    FeatureGroupEntity featureGroup = new FeatureGroupEntity(vlmId, version, id);
    featureGroup.setVendorLicenseModelId(vlmId);
    featureGroup.setVersion(version);
    featureGroup.setId(id);
    featureGroup.setName(name);
    featureGroup.setDescription(desc);
    featureGroup.setPartNumber(partNumber);
    //featureGroup.setManufacturerReferenceNumber(manufacturerReferenceNumber);
    featureGroup.setLicenseKeyGroupIds(licenseKeyGroupIds);
    featureGroup.setEntitlementPoolIds(entitlementPoolIds);
    featureGroup.setReferencingLicenseAgreements(referencingLicenseAgreements);

    return featureGroup;
  }

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testUpdate() {
    Set<String> licenseKeyGroupIds;
    licenseKeyGroupIds = new HashSet<>();
    licenseKeyGroupIds.add("lkg1");

    Set<String> entitlementPoolIds;
    entitlementPoolIds = new HashSet<>();
    entitlementPoolIds.add("ep1");

    Set<String> referencingLicenseAgreements;
    referencingLicenseAgreements = new HashSet<>();
    referencingLicenseAgreements.add("la1");

    FeatureGroupEntity featureGroupEntity =
        updateFeatureGroup("vlmId", VERSION01, "fgId", FG1_NAME, "fg1 desc",
            "partNumber", "MRN", licenseKeyGroupIds, entitlementPoolIds,
            referencingLicenseAgreements);

    doReturn(featureGroupEntity).when(featureGroupDao).get(anyObject());

        /*if(featureGroupEntity.getManufacturerReferenceNumber() != null)
            featureGroupDao.update(featureGroupEntity);
        verify(featureGroupDao).update(anyObject());*/
  }

  @Test
  public void testUpdateWithoutManufacturingReferenceNumber() {
    Set<String> licenseKeyGroupIds;
    licenseKeyGroupIds = new HashSet<>();
    licenseKeyGroupIds.add("lkg1");

    Set<String> entitlementPoolIds;
    entitlementPoolIds = new HashSet<>();
    entitlementPoolIds.add("ep1");

    Set<String> referencingLicenseAgreements;
    referencingLicenseAgreements = new HashSet<>();
    referencingLicenseAgreements.add("la1");

    FeatureGroupEntity featureGroupEntity =
        updateFeatureGroup("vlmId", VERSION01, "fgId", FG1_NAME, "fg1 desc",
            "partNumber", null, licenseKeyGroupIds, entitlementPoolIds,
            referencingLicenseAgreements);

    doReturn(featureGroupEntity).when(featureGroupDao).get(anyObject());

        /*if(featureGroupEntity.getManufacturerReferenceNumber() != null)
            featureGroupDao.update(featureGroupEntity);
        verify(featureGroupDao, never()).update(anyObject());*/
  }

  @Test
  public void testListFeatureGroups() {
    doReturn(Arrays.asList(
        createFeatureGroup(vlm1_id, VERSION01, fg1_id, "FG1", "FG1 desc", new HashSet<String>(),
            new HashSet<String>()),
        createFeatureGroup(vlm1_id, VERSION01, fg2_id, "FG2", "FG2 desc", new HashSet<String>(),
            new HashSet<String>())))
        .when(vendorLicenseFacadeMcok).listFeatureGroups(vlm1_id, VERSION01);

    Collection<FeatureGroupEntity> FGs =
        vendorLicenseManagerImpl.listFeatureGroups(vlm1_id, VERSION01);

    verify(vendorLicenseFacadeMcok).listFeatureGroups(vlm1_id, VERSION01);
    Assert.assertEquals(FGs.size(), 2);
    FGs.forEach(fg -> Assert.assertTrue(fg.getId().matches(fg1_id + "|" + fg2_id)));
  }

  @Test
  public void testCreateFeatureGroup() {
    FeatureGroupEntity featureGroupEntity = new FeatureGroupEntity(vlm1_id, VERSION01,
        fg1_id);

    doReturn(featureGroupEntity).when(vendorLicenseFacadeMcok).createFeatureGroup
        (featureGroupEntity);

    vendorLicenseManagerImpl.createFeatureGroup(featureGroupEntity);

    verify(vendorLicenseFacadeMcok).createFeatureGroup(featureGroupEntity);
  }


  @Test
  public void testUpdateFeatureGroup() {
    FeatureGroupEntity existingFG = new FeatureGroupEntity(vlm1_id, VERSION01, fg1_id);

    existingFG.setEntitlementPoolIds(new HashSet<String>());
    existingFG.setLicenseKeyGroupIds(new HashSet<String>());

    doReturn(existingFG).when(featureGroupDao).get(existingFG);

    Set<String> removedEPs = new HashSet<>();
    Set<String> addedEPs = new HashSet<>();

    addedEPs.add(ep1_id);
    addedEPs.add(ep2_id);
    EntitlementPoolEntity ep1 = new EntitlementPoolEntity(vlm1_id, VERSION01, ep1_id);
    EntitlementPoolEntity ep2 = new EntitlementPoolEntity(vlm1_id, VERSION01, ep2_id);
    doReturn(ep1).when(entitlementPoolDao).get(ep1);
    doReturn(ep2).when(entitlementPoolDao).get(ep2);

    Set<String> removedLKGs = new HashSet<>();
    Set<String> addedLKGs = new HashSet<>();

    addedLKGs.add(lkg1_id);
    addedLKGs.add(lkg2_id);
    LicenseKeyGroupEntity lkg1 = new LicenseKeyGroupEntity(vlm1_id, VERSION01, lkg1_id);
    LicenseKeyGroupEntity lkg2 = new LicenseKeyGroupEntity(vlm1_id, VERSION01, lkg2_id);
    doReturn(lkg1).when(licenseKeyGroupDao).get(lkg1);
    doReturn(lkg2).when(licenseKeyGroupDao).get(lkg2);

    doNothing().when(vendorLicenseManagerImpl).updateUniqueName(anyObject(), anyObject(),
        anyObject(), anyObject(), anyObject());

    vendorLicenseManagerImpl.updateFeatureGroup(existingFG, addedLKGs, removedLKGs, addedEPs,
        removedEPs);

    verify(vendorLicenseManagerImpl).addLicenseKeyGroupsToFeatureGroupsRef(addedLKGs,
        existingFG);
    verify(vendorLicenseManagerImpl).removeLicenseKeyGroupsToFeatureGroupsRef(removedLKGs,
        existingFG);
    verify(vendorLicenseManagerImpl).addEntitlementPoolsToFeatureGroupsRef(addedEPs, existingFG);
    verify(vendorLicenseManagerImpl).removeEntitlementPoolsToFeatureGroupsRef(removedEPs,
        existingFG);

    verify(featureGroupDao)
        .updateFeatureGroup(existingFG, addedEPs, removedEPs, addedLKGs, removedLKGs);
  }


  @Test
  public void testGetFeatureGroup() {
    FeatureGroupEntity featureGroupEntity = new FeatureGroupEntity(vlm1_id, VERSION01, fg1_id);
    vendorLicenseManagerImpl.getFeatureGroupModel(featureGroupEntity);
    verify(vendorLicenseFacadeMcok).getFeatureGroupModel(featureGroupEntity);
  }

  @Test
  public void deleteFeatureGroupTest() {

    FeatureGroupEntity existingFG = new FeatureGroupEntity(vlm1_id, VERSION01, fg1_id);
    existingFG.setName("FG_name");
    existingFG.setLicenseKeyGroupIds(new HashSet<String>());
    existingFG.setEntitlementPoolIds(new HashSet<String>());
    existingFG.setLicenseKeyGroupIds(new HashSet<String>());

    doReturn(existingFG).when(featureGroupDao).get(anyObject());

    doNothing().when(vendorLicenseManagerImpl).deleteUniqueName(VendorLicenseConstants
        .UniqueValues.FEATURE_GROUP_NAME, vlm1_id, VERSION01.toString(), existingFG.getName());

    vendorLicenseManagerImpl.deleteFeatureGroup(existingFG);

    verify(featureGroupDao).delete(existingFG);

    verify(vendorLicenseManagerImpl).removeLicenseKeyGroupsToFeatureGroupsRef(existingFG
        .getLicenseKeyGroupIds(), existingFG);
    verify(vendorLicenseManagerImpl).removeEntitlementPoolsToFeatureGroupsRef(existingFG
        .getEntitlementPoolIds(), existingFG);
    verify(vendorLicenseManagerImpl).deleteUniqueName(VendorLicenseConstants
        .UniqueValues.FEATURE_GROUP_NAME, vlm1_id, VERSION01.toString(), existingFG.getName());
  }

  private FeatureGroupEntity createFeatureGroup(String vendorId, Version version, String id,
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
}

