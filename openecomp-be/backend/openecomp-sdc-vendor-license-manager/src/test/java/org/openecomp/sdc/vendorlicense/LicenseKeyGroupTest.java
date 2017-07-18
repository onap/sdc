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
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorlicense.dao.LicenseKeyGroupDao;
import org.openecomp.sdc.vendorlicense.dao.LimitDao;
import org.openecomp.sdc.vendorlicense.dao.types.*;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.impl.VendorLicenseManagerImpl;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersioningErrorCodes;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class LicenseKeyGroupTest {

    //JUnit Test Cases using Mockito
    private  final String USER = "lkgTestUser";
    private  final String LKG_NAME = "LKG name";
    private  final String LT_NAME = "LT name";

    @Mock
    private VendorLicenseFacade vendorLicenseFacade;

    @Mock
    private LicenseKeyGroupDao licenseKeyGroupDao;
    @Mock
    private LimitDao limitDao;

    @InjectMocks
    @Spy
    private VendorLicenseManagerImpl vendorLicenseManagerImpl;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    private LicenseKeyGroupEntity createLicenseKeyGroup(LicenseKeyType type, Set<OperationalScope> operationalScopeChoices,
                                                            String operationalScopeOther)
    {
        LicenseKeyGroupEntity licenseKeyGroupEntity = new LicenseKeyGroupEntity();
        licenseKeyGroupEntity.setType(type);
        licenseKeyGroupEntity.setOperationalScope(
                new MultiChoiceOrOther<>(operationalScopeChoices, operationalScopeOther));
        return licenseKeyGroupEntity;
    }

    @Test
    public void deleteLicenseKeyGroupTest() {
        Set<OperationalScope> opScopeChoices;
        opScopeChoices = new HashSet<>();
        opScopeChoices.add(OperationalScope.Core);
        opScopeChoices.add(OperationalScope.CPU);
        opScopeChoices.add(OperationalScope.Network_Wide);

        LicenseKeyGroupEntity licenseKeyGroup =
                createLicenseKeyGroup(LicenseKeyType.Unique, opScopeChoices, null);

        VersionInfo info = new VersionInfo();
        Version version = new Version();
        info.getViewableVersions().add(version);
        info.setActiveVersion(version);
        doReturn(info).when(vendorLicenseFacade).getVersionInfo(anyObject(),anyObject(),anyObject());

        LimitEntity limitEntity = LimitTest.createLimitEntity(LT_NAME,LimitType.Vendor,"string",version,
                EntitlementMetric.Core,AggregationFunction.Average,10,EntitlementTime.Hour);

        ArrayList<LimitEntity> limitEntityList = new ArrayList();
        limitEntityList.add(limitEntity);

        doReturn(licenseKeyGroup).when(licenseKeyGroupDao).get(anyObject());
        doReturn(limitEntityList).when(vendorLicenseFacade).listLimits(anyObject(), anyObject(), anyObject(), anyObject());
        doReturn(true).when(limitDao).isLimitPresent(anyObject());
        doReturn(limitEntity).when(limitDao).get(anyObject());
        try {
            Field limitField = VendorLicenseManagerImpl.class.getDeclaredField("limitDao");
            limitField.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(limitField, limitField.getModifiers() & ~Modifier.FINAL);
            limitField.set(null, limitDao);

            Field lkgField = VendorLicenseManagerImpl.class.getDeclaredField("licenseKeyGroupDao");
            lkgField.setAccessible(true);
            modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(lkgField, lkgField.getModifiers() & ~Modifier.FINAL);
            lkgField.set(null, licenseKeyGroupDao);
        } catch(NoSuchFieldException | IllegalAccessException e)
        {
            Assert.fail();
        }

        vendorLicenseManagerImpl.deleteLicenseKeyGroup(licenseKeyGroup, USER);

        verify(limitDao).delete(anyObject());
    }

    @Test
    public void deleteLicenseKeyGroupInvalidTest() {
        try {
            Set<OperationalScope> opScopeChoices;
            opScopeChoices = new HashSet<>();
            opScopeChoices.add(OperationalScope.Core);
            opScopeChoices.add(OperationalScope.CPU);
            opScopeChoices.add(OperationalScope.Network_Wide);

            LicenseKeyGroupEntity licenseKeyGroup =
                createLicenseKeyGroup(LicenseKeyType.Unique, opScopeChoices, null);

            VersionInfo info = new VersionInfo();
            Version version = new Version();
            info.getViewableVersions().add(version);
            info.setActiveVersion(version);
            doReturn(info).when(vendorLicenseFacade).getVersionInfo(anyObject(),anyObject(),anyObject());

            LimitEntity limitEntity = LimitTest.createLimitEntity(LT_NAME,LimitType.Vendor,"string",version,
                EntitlementMetric.Core,AggregationFunction.Average,10,EntitlementTime.Hour);

            ArrayList<LimitEntity> limitEntityList = new ArrayList();
            limitEntityList.add(limitEntity);

            doReturn(licenseKeyGroup).when(licenseKeyGroupDao).get(anyObject());
            doReturn(limitEntityList).when(vendorLicenseFacade).listLimits(anyObject(), anyObject(), anyObject(), anyObject());
            doReturn(false).when(limitDao).isLimitPresent(anyObject());

            try {
                Field limitField = VendorLicenseManagerImpl.class.getDeclaredField("limitDao");
                limitField.setAccessible(true);
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(limitField, limitField.getModifiers() & ~Modifier.FINAL);
                limitField.set(null, limitDao);

                Field lkgField = VendorLicenseManagerImpl.class.getDeclaredField("licenseKeyGroupDao");
                lkgField.setAccessible(true);
                modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(lkgField, lkgField.getModifiers() & ~Modifier.FINAL);
                lkgField.set(null, licenseKeyGroupDao);
            } catch(NoSuchFieldException | IllegalAccessException e)
            {
                Assert.fail();
            }

            vendorLicenseManagerImpl.deleteLicenseKeyGroup(licenseKeyGroup, USER);
        } catch (CoreException exception) {
            Assert.assertEquals(exception.code().id(), VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
        }
    }

  /*public static final String LKG1_NAME = "LKG1 name";
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
    LicenseKeyGroupEntity
        lkg1 = createLicenseKeyGroup(vlmId, VERSION01, name, "LKG1 dec", LicenseKeyType.One_Time,
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
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), UniqueValueUtil.UNIQUE_VALUE_VIOLATION);
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
  */
}
