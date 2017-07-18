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


package org.openecomp.sdc.vendorlicense;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.vendorlicense.dao.*;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.facade.impl.VendorLicenseFacadeImpl;
import org.openecomp.sdc.vendorlicense.impl.VendorLicenseManagerImpl;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Created by KATYR on 4/10/2016
 */

public class FeatureGroupTest {
    //JUnit Test Cases using Mockito
    private static final Version VERSION01 = new Version(0, 1);
    private final String FG1_NAME = "FG1 name";

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

    public FeatureGroupEntity updateFeatureGroup(String vlmId, Version version, String id, String name, String desc,
                                                 String partNumber, String manufacturerReferenceNumber, Set<String>
                                                         licenseKeyGroupIds, Set<String> entitlementPoolIds, Set<String>
                                                         referencingLicenseAgreements){
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
    public void setUp() throws Exception{
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testUpdate(){
        Set<String> licenseKeyGroupIds;
        licenseKeyGroupIds = new HashSet<>();
        licenseKeyGroupIds.add("lkg1");

        Set<String> entitlementPoolIds;
        entitlementPoolIds = new HashSet<>();
        entitlementPoolIds.add("ep1");

        Set<String> referencingLicenseAgreements;
        referencingLicenseAgreements = new HashSet<>();
        referencingLicenseAgreements.add("la1");

        FeatureGroupEntity featureGroupEntity = updateFeatureGroup("vlmId", VERSION01, "fgId", FG1_NAME, "fg1 desc",
                "partNumber", "MRN", licenseKeyGroupIds, entitlementPoolIds,
                referencingLicenseAgreements);

        doReturn(featureGroupEntity).when(featureGroupDao).get(anyObject());

        /*if(featureGroupEntity.getManufacturerReferenceNumber() != null)
            featureGroupDao.update(featureGroupEntity);
        verify(featureGroupDao).update(anyObject());*/
    }

    @Test
    public void testUpdateWithoutManufacturingReferenceNumber(){
        Set<String> licenseKeyGroupIds;
        licenseKeyGroupIds = new HashSet<>();
        licenseKeyGroupIds.add("lkg1");

        Set<String> entitlementPoolIds;
        entitlementPoolIds = new HashSet<>();
        entitlementPoolIds.add("ep1");

        Set<String> referencingLicenseAgreements;
        referencingLicenseAgreements = new HashSet<>();
        referencingLicenseAgreements.add("la1");

        FeatureGroupEntity featureGroupEntity = updateFeatureGroup("vlmId", VERSION01, "fgId", FG1_NAME, "fg1 desc",
                "partNumber", null, licenseKeyGroupIds, entitlementPoolIds,
                referencingLicenseAgreements);

        doReturn(featureGroupEntity).when(featureGroupDao).get(anyObject());

        /*if(featureGroupEntity.getManufacturerReferenceNumber() != null)
            featureGroupDao.update(featureGroupEntity);
        verify(featureGroupDao, never()).update(anyObject());*/
    }


}

/*
  protected static final Version VERSION01 = new Version(0, 1);
  protected static final String USER1 = "FeatureGroupTest_User1";
  protected static VendorLicenseManager vendorLicenseManager = new VendorLicenseManagerImpl();
  protected static VendorLicenseFacade vendorLicenseFacade =
      VendorLicenseFacadeFactory.getInstance().createInterface();


  @Test
  public void testListFeatureGroups() throws Exception {
    String vlmId = vendorLicenseFacade.createVendorLicenseModel(VendorLicenseModelTest
            .createVendorLicenseModel("vlmId_" + CommonMethods.nextUuId(), "vlm2Id desc", "icon2"),
        USER1).getId();
    FeatureGroupEntity
        fg22 = LicenseAgreementTest
        .createFeatureGroup(vlmId, VERSION01, "fg2", "FG2", "FG2 desc", null, null);
    String fg22Id = vendorLicenseManager.createFeatureGroup(fg22, USER1).getId();
    FeatureGroupEntity fg33 = LicenseAgreementTest
        .createFeatureGroup(vlmId, VERSION01, "fg3", "FG3", "FG3 desc", null, null);
    String fg33Id = vendorLicenseManager.createFeatureGroup(fg33, USER1).getId();

    Collection<FeatureGroupEntity> featureGroupEntities =
        vendorLicenseManager.listFeatureGroups(vlmId, null, USER1);

    Assert.assertEquals(featureGroupEntities.size(), 2);
    Set<String> actualIds = new HashSet<>();
    for (FeatureGroupEntity featureGroupEntity : featureGroupEntities) {
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
    String vlmId = vendorLicenseFacade.createVendorLicenseModel(VendorLicenseModelTest
        .createVendorLicenseModel(testName + CommonMethods.nextUuId(), testName, "icon1"), USER1)
        .getId();
    Set<OperationalScope> opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Other);
    opScopeChoices.add(OperationalScope.Data_Center);
    opScopeChoices.add(OperationalScope.Network_Wide);
    EntitlementPoolEntity
        ep = EntitlementPoolTest
        .createEntitlementPool(vlmId, VERSION01, "EP1" + CommonMethods.nextUuId(), "EP1 dec", 80,
            ThresholdUnit.Absolute, EntitlementMetric.Core, null, "inc1", AggregationFunction.Other,
            "agg func1", opScopeChoices, null, EntitlementTime.Hour, null, "sku1");
    String epId = vendorLicenseManager.createEntitlementPool(ep, USER1).getId();
    Set<OperationalScope> opScopeChoicesLKG = new HashSet<>();
    opScopeChoicesLKG.add(OperationalScope.CPU);
    opScopeChoicesLKG.add(OperationalScope.VM);
    opScopeChoicesLKG.add(OperationalScope.Availability_Zone);
    opScopeChoicesLKG.add(OperationalScope.Data_Center);

    LicenseKeyGroupEntity
        lkg = LicenseKeyGroupTest
        .createLicenseKeyGroup(vlmId, VERSION01, "LKG1", "LKG1 dec", LicenseKeyType.One_Time,
            new MultiChoiceOrOther<>(opScopeChoicesLKG, null));
    String lkgId = vendorLicenseManager.createLicenseKeyGroup(lkg, USER1).getId();
    lkg.setId(lkgId);
    FeatureGroupEntity fg1 =
        createFGForTest(vlmId, "created" + CommonMethods.nextUuId(), Collections.singleton(epId),
            Collections.singleton(lkgId));
    FeatureGroupEntity fg1FromDB =
        vendorLicenseManager.getFeatureGroupModel(fg1, USER1).getFeatureGroup();
    Assert.assertTrue(fg1FromDB.equals(fg1));
  }


  @Test
  public void testCreateWithExistingName_negative() {
    String testName = "createExistingName";
    String vlmId = vendorLicenseFacade.createVendorLicenseModel(VendorLicenseModelTest
        .createVendorLicenseModel(testName + CommonMethods.nextUuId(), testName, "icon1"), USER1)
        .getId();
    createFGForTest(vlmId, "created", Collections.emptySet(), Collections.emptySet());
    try {
      FeatureGroupEntity created = LicenseAgreementTest
          .createFeatureGroup(vlmId, null, "created", "created", "created desc",
              Collections.emptySet(), Collections.emptySet());
      vendorLicenseManager.createFeatureGroup(created, USER1);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), UniqueValueUtil.UNIQUE_VALUE_VIOLATION);
    }
  }

  private FeatureGroupEntity createFGForTest(String vlmId, String fgName, Set<String> epIds,
                                             Set<String> lkgIds) {
    FeatureGroupEntity created = LicenseAgreementTest
        .createFeatureGroup(vlmId, null, null, fgName, "created desc", epIds, lkgIds);
    return vendorLicenseManager.createFeatureGroup(created, USER1);
  }

  @Test
  public void testUpdateFeatureGroup_addEP_andGET() throws Exception {
    String testName = "testUpdateFeatureGroup_addEP_andGET";
    String vlmId = vendorLicenseFacade.createVendorLicenseModel(VendorLicenseModelTest
        .createVendorLicenseModel(testName + CommonMethods.nextUuId(), testName, "icon1"), USER1)
        .getId();

    FeatureGroupEntity fg5 = LicenseAgreementTest
        .createFeatureGroup(vlmId, VERSION01, "id" + CommonMethods.nextUuId(),
            "created" + CommonMethods.nextUuId(), "created desc", null, null);
    vendorLicenseManager.createFeatureGroup(fg5, USER1).getId();


    Set<OperationalScope> opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Other);
    opScopeChoices.add(OperationalScope.Data_Center);

    EntitlementPoolEntity epToAdd = EntitlementPoolTest
        .createEntitlementPool(vlmId, VERSION01, "epToAdd", "epToAdd dec", 80,
            ThresholdUnit.Absolute, EntitlementMetric.Core, null, "inc1", AggregationFunction.Other,
            "agg func1", opScopeChoices, null, EntitlementTime.Hour, null, "sku1");
    String epToAddId = vendorLicenseManager.createEntitlementPool(epToAdd, USER1).getId();

    vendorLicenseManager
        .updateFeatureGroup(fg5, null, null, CommonMethods.toSingleElementSet(epToAddId), null,
            USER1);
    FeatureGroupModel updatedFG = vendorLicenseManager.getFeatureGroupModel(fg5, USER1);
    Set<EntitlementPoolEntity> updatedEPs = updatedFG.getEntitlementPools();

    epToAdd.setReferencingFeatureGroups(CommonMethods.toSingleElementSet(fg5.getId()));

    Assert.assertEquals(updatedEPs.size(), 1);
    for (EntitlementPoolEntity updatedEP : updatedEPs) {
      Assert.assertTrue(updatedEP.getReferencingFeatureGroups().contains(fg5.getId()));
      Assert.assertEquals(updatedEP.getId(), epToAddId);
    }
  }

  @Test
  public void testUpdateFeatureGroup_removeLKG_andGET() throws Exception {
    String testName = "testUpdateFeatureGroup_removeLKG_andGET";
    String vlmId = vendorLicenseFacade.createVendorLicenseModel(VendorLicenseModelTest
        .createVendorLicenseModel(testName + CommonMethods.nextUuId(), testName, "icon1"), USER1)
        .getId();

    Set<OperationalScope> opScopeChoicesLKG = new HashSet<>();
    opScopeChoicesLKG.add(OperationalScope.CPU);
    opScopeChoicesLKG.add(OperationalScope.VM);
    opScopeChoicesLKG.add(OperationalScope.Availability_Zone);
    opScopeChoicesLKG.add(OperationalScope.Data_Center);
    LicenseKeyGroupEntity lkg = LicenseKeyGroupTest
        .createLicenseKeyGroup(vlmId, VERSION01, "lkg" + CommonMethods.nextUuId(), "lkg desc",
            LicenseKeyType.Unique, new MultiChoiceOrOther<>(opScopeChoicesLKG, null));
    String lkgId = vendorLicenseManager.createLicenseKeyGroup(lkg, USER1).getId();
    lkg.setId(lkgId);

    LicenseKeyGroupEntity lkg_1 = LicenseKeyGroupTest
        .createLicenseKeyGroup(vlmId, VERSION01, "lkg" + CommonMethods.nextUuId(), "lkg_1 desc",
            LicenseKeyType.Unique, new MultiChoiceOrOther<>(opScopeChoicesLKG, null));
    String lkgId_1 = vendorLicenseManager.createLicenseKeyGroup(lkg_1, USER1).getId();
    lkg.setId(lkgId);

    Set<OperationalScope> opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Other);
    opScopeChoices.add(OperationalScope.Data_Center);
    opScopeChoices.add(OperationalScope.Network_Wide);
    EntitlementPoolEntity ep = EntitlementPoolTest
        .createEntitlementPool(vlmId, VERSION01, "EP1" + CommonMethods.nextUuId(), "EP1 dec", 80,
            ThresholdUnit.Absolute, EntitlementMetric.Core, null, "inc1", AggregationFunction.Other,
            "agg func1", opScopeChoices, null, EntitlementTime.Hour, null, "sku1");
    String epId = vendorLicenseManager.createEntitlementPool(ep, USER1).getId();

    Set<String> lkgs = new HashSet<>();
    lkgs.add(lkgId);
    lkgs.add(lkgId_1);

    FeatureGroupEntity fg = LicenseAgreementTest
        .createFeatureGroup(vlmId, VERSION01, "fg11" + CommonMethods.nextUuId(), "FG1", "FG1 desc",
            CommonMethods.toSingleElementSet(epId), lkgs);
    String fgId = vendorLicenseManager.createFeatureGroup(fg, USER1).getId();
    vendorLicenseManager
        .updateFeatureGroup(fg, null, CommonMethods.toSingleElementSet(lkgId), null, null, USER1);

    FeatureGroupModel featureGroup = vendorLicenseManager.getFeatureGroupModel(fg, USER1);
    Set<LicenseKeyGroupEntity> licenseKeyGroups = featureGroup.getLicenseKeyGroups();
    Assert.assertEquals(licenseKeyGroups.size(), 1);
    List<String> lkgIds = new ArrayList<>();
    for (LicenseKeyGroupEntity licenseKeyGroup : licenseKeyGroups) {
      lkgIds.add(licenseKeyGroup.getId());
    }

    Assert.assertTrue(lkgIds.contains(lkgId_1));
    Assert.assertFalse(lkgIds.contains(lkgId));

  }


  @Test
  public void testDeleteFeatureGroup() throws Exception {
    String testName = "testDeleteFeatureGroup";
    String vlmId = vendorLicenseFacade.createVendorLicenseModel(VendorLicenseModelTest
        .createVendorLicenseModel(testName + CommonMethods.nextUuId(), testName, "icon1"), USER1)
        .getId();

    FeatureGroupEntity fg1 =
        createFGForTest(vlmId, "new", Collections.emptySet(), Collections.emptySet());
    FeatureGroupEntity fg2 =
        createFGForTest(vlmId, "newer", Collections.emptySet(), Collections.emptySet());
    Collection<FeatureGroupEntity> featureGroupEntities =
        vendorLicenseManager.listFeatureGroups(vlmId, null, USER1);
    Assert.assertEquals(featureGroupEntities.size(), 2); //precondition

    vendorLicenseManager.deleteFeatureGroup(fg1, USER1);
    Assert.assertEquals(vendorLicenseManager.listFeatureGroups(vlmId, null, USER1).size(), 1);


  }


}
*/
