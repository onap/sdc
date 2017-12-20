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
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorlicense.dao.LicenseKeyGroupDao;
import org.openecomp.sdc.vendorlicense.dao.LimitDao;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyType;
import org.openecomp.sdc.vendorlicense.dao.types.MultiChoiceOrOther;
import org.openecomp.sdc.vendorlicense.dao.types.OperationalScope;
import org.openecomp.sdc.vendorlicense.errors.VendorLicenseErrorCodes;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class LicenseKeyGroupTest {

  //JUnit Test Cases using Mockito
  private final String USER = "lkgTestUser";
  private final String LKG_NAME = "LKG name";
  private final String LKG2_NAME = "LKG2 name";
  private final String LT_NAME = "LT name";
  private final String LKG1_NAME = "LKG1 name";
  private final String USER1 = "user1";
  private static String lkg1_id = "lkg1_id";
  private static String lkg2_id = "lkg2_id";
  private static String vlm1_id = "vlm1_id";
  public static final Version VERSION01 = new Version(0, 1);


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

  private LicenseKeyGroupEntity createLicenseKeyGroup(LicenseKeyType type,
                                                      Set<OperationalScope> operationalScopeChoices,
                                                      String operationalScopeOther) {
    LicenseKeyGroupEntity licenseKeyGroupEntity = new LicenseKeyGroupEntity();
    licenseKeyGroupEntity.setType(type);
    licenseKeyGroupEntity.setOperationalScope(
        new MultiChoiceOrOther<>(operationalScopeChoices, operationalScopeOther));
    return licenseKeyGroupEntity;
  }

  /*
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
                  "Core",AggregationFunction.Average,10,"Hour");

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
                  "Core",AggregationFunction.Average,10,"Hour");

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
  */
  @Test
  public void createTest() {
    Set<OperationalScope> opScopeChoices;
    opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Core);
    opScopeChoices.add(OperationalScope.CPU);
    opScopeChoices.add(OperationalScope.Network_Wide);
    LicenseKeyGroupEntity lkg =
        createLicenseKeyGroup("vlm1Id", null, lkg1_id, LKG1_NAME, "LKG1 dec", LicenseKeyType.Unique,
            new MultiChoiceOrOther<>(opScopeChoices, null));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    lkg.setStartDate(LocalDate.now().format(formatter));
    lkg.setExpiryDate(LocalDate.now().plusDays(1L).format(formatter));

    vendorLicenseManagerImpl.createLicenseKeyGroup(lkg);
    verify(vendorLicenseFacade).createLicenseKeyGroup(lkg);
  }

  @Test
  public void createWithInvalidStartExpiryDateTest() {
    try {

      Set<OperationalScope> opScopeChoices;
      opScopeChoices = new HashSet<>();
      opScopeChoices.add(OperationalScope.Core);
      opScopeChoices.add(OperationalScope.CPU);
      opScopeChoices.add(OperationalScope.Network_Wide);
      LicenseKeyGroupEntity lkg =
          createLicenseKeyGroup("vlm1Id", null, lkg1_id, LKG1_NAME, "LKG1 dec",
              LicenseKeyType.Unique,
              new MultiChoiceOrOther<>(opScopeChoices, null));
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
      lkg.setStartDate(LocalDate.now().format(formatter));
      lkg.setExpiryDate(LocalDate.now().minusDays(2L).format(formatter));
      vendorLicenseManagerImpl.createLicenseKeyGroup(lkg);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), VendorLicenseErrorCodes.DATE_RANGE_INVALID);
    }
  }

  @Test
  public void createWithoutStartDateTest() {
    try {

      Set<OperationalScope> opScopeChoices;
      opScopeChoices = new HashSet<>();
      opScopeChoices.add(OperationalScope.Core);
      opScopeChoices.add(OperationalScope.CPU);
      opScopeChoices.add(OperationalScope.Network_Wide);
      LicenseKeyGroupEntity lkg =
          createLicenseKeyGroup("vlm1Id", null, lkg1_id, LKG1_NAME, "LKG1 dec",
              LicenseKeyType.Unique,
              new MultiChoiceOrOther<>(opScopeChoices, null));
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
      lkg.setExpiryDate(LocalDate.now().plusDays(2L).format(formatter));
      vendorLicenseManagerImpl.createLicenseKeyGroup(lkg).getId();
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), VendorLicenseErrorCodes.DATE_RANGE_INVALID);
    }
  }

  @Test
  public void createWithSameStartExpiryDateTest() {
    try {

      Set<OperationalScope> opScopeChoices;
      opScopeChoices = new HashSet<>();
      opScopeChoices.add(OperationalScope.Core);
      opScopeChoices.add(OperationalScope.CPU);
      opScopeChoices.add(OperationalScope.Network_Wide);
      LicenseKeyGroupEntity lkg =
          createLicenseKeyGroup("vlm1Id", null, lkg1_id, LKG1_NAME, "LKG1 dec",
              LicenseKeyType.Unique,
              new MultiChoiceOrOther<>(opScopeChoices, null));
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
      lkg.setStartDate(LocalDate.now().plusDays(2L).format(formatter));
      lkg.setExpiryDate(LocalDate.now().plusDays(2L).format(formatter));
      vendorLicenseManagerImpl.createLicenseKeyGroup(lkg).getId();
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), VendorLicenseErrorCodes.DATE_RANGE_INVALID);
    }
  }

  @Test
  public void testUpdate() {
    Set<OperationalScope> opScopeChoices;
    opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Core);
    opScopeChoices.add(OperationalScope.CPU);
    opScopeChoices.add(OperationalScope.Network_Wide);
    LicenseKeyGroupEntity lkg =
        createLicenseKeyGroup(vlm1_id, null, lkg1_id, LKG1_NAME, "LKG1 dec", LicenseKeyType.Unique,
            new MultiChoiceOrOther<>(opScopeChoices, null));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    lkg.setStartDate(LocalDate.now().minusDays(3L).format(formatter));
    lkg.setExpiryDate(LocalDate.now().minusDays(2L).format(formatter));

    vendorLicenseManagerImpl.updateLicenseKeyGroup(lkg);
    verify(vendorLicenseFacade).updateLicenseKeyGroup(lkg);
  }

  @Test
  public void updateWithInvalidStartExpiryDateTest() {
    try {

      Set<OperationalScope> opScopeChoices;
      opScopeChoices = new HashSet<>();
      opScopeChoices.add(OperationalScope.Core);
      opScopeChoices.add(OperationalScope.CPU);
      opScopeChoices.add(OperationalScope.Network_Wide);
      LicenseKeyGroupEntity lkg =
          createLicenseKeyGroup("vlm1Id", null, lkg1_id, LKG1_NAME, "LKG1 dec",
              LicenseKeyType.Unique,
              new MultiChoiceOrOther<>(opScopeChoices, null));
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
      lkg.setStartDate(LocalDate.now().format(formatter));
      lkg.setExpiryDate(LocalDate.now().minusDays(2L).format(formatter));
      vendorLicenseManagerImpl.updateLicenseKeyGroup(lkg);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), VendorLicenseErrorCodes.DATE_RANGE_INVALID);
    }
  }

  @Test
  public void updateWithoutStartDateTest() {
    try {

      Set<OperationalScope> opScopeChoices;
      opScopeChoices = new HashSet<>();
      opScopeChoices.add(OperationalScope.Core);
      opScopeChoices.add(OperationalScope.CPU);
      opScopeChoices.add(OperationalScope.Network_Wide);
      LicenseKeyGroupEntity lkg =
          createLicenseKeyGroup("vlm1Id", null, lkg1_id, LKG1_NAME, "LKG1 dec",
              LicenseKeyType.Unique,
              new MultiChoiceOrOther<>(opScopeChoices, null));
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
      lkg.setExpiryDate(LocalDate.now().plusDays(2L).format(formatter));
      vendorLicenseManagerImpl.updateLicenseKeyGroup(lkg);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), VendorLicenseErrorCodes.DATE_RANGE_INVALID);
    }
  }

  @Test
  public void updateWithSameStartExpiryDateTest() {
    try {

      Set<OperationalScope> opScopeChoices;
      opScopeChoices = new HashSet<>();
      opScopeChoices.add(OperationalScope.Core);
      opScopeChoices.add(OperationalScope.CPU);
      opScopeChoices.add(OperationalScope.Network_Wide);
      LicenseKeyGroupEntity lkg =
          createLicenseKeyGroup("vlm1Id", null, lkg1_id, LKG1_NAME, "LKG1 dec",
              LicenseKeyType.Unique,
              new MultiChoiceOrOther<>(opScopeChoices, null));
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
      lkg.setStartDate(LocalDate.now().format(formatter));
      lkg.setExpiryDate(LocalDate.now().format(formatter));
      vendorLicenseManagerImpl.updateLicenseKeyGroup(lkg);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), VendorLicenseErrorCodes.DATE_RANGE_INVALID);
    }
  }

  @Test
  public void testListlistLicenseKeyGroups() {

    MultiChoiceOrOther<OperationalScope> multiChoiceOrOther =
        new MultiChoiceOrOther<OperationalScope>();
    Set<OperationalScope> opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Core);
    opScopeChoices.add(OperationalScope.CPU);
    opScopeChoices.add(OperationalScope.Network_Wide);
    multiChoiceOrOther.setChoices(opScopeChoices);
    multiChoiceOrOther.setOther("Other");

    doReturn(Arrays.asList(
        createLicenseKeyGroup(vlm1_id, VERSION01, lkg1_id, LKG1_NAME, "LKG1 dec",
            LicenseKeyType.Universal,
            multiChoiceOrOther),
        createLicenseKeyGroup(vlm1_id, VERSION01, lkg2_id, LKG2_NAME, "LKG2 dec", LicenseKeyType
            .Universal, multiChoiceOrOther)))
        .when(vendorLicenseFacade).listLicenseKeyGroups(vlm1_id, VERSION01);

    Collection<LicenseKeyGroupEntity> LKGs =
        vendorLicenseManagerImpl.listLicenseKeyGroups(vlm1_id, VERSION01);

    verify(vendorLicenseFacade).listLicenseKeyGroups(vlm1_id, VERSION01);
    Assert.assertEquals(LKGs.size(), 2);
    LKGs.forEach(lkg -> Assert.assertTrue(lkg.getId().matches(lkg1_id + "|" + lkg2_id)));
  }

  @Test
  public void testGetLicenseKeyGroup() {
    MultiChoiceOrOther<OperationalScope> multiChoiceOrOther =
        new MultiChoiceOrOther<OperationalScope>();
    Set<OperationalScope> opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Core);
    opScopeChoices.add(OperationalScope.CPU);
    opScopeChoices.add(OperationalScope.Network_Wide);
    multiChoiceOrOther.setChoices(opScopeChoices);
    multiChoiceOrOther.setOther("Other");

    LicenseKeyGroupEntity lkg = createLicenseKeyGroup(vlm1_id, VERSION01, lkg1_id, LKG1_NAME,
        "LKG1 dec", LicenseKeyType.Universal, multiChoiceOrOther);

    doReturn(lkg).when(licenseKeyGroupDao).get(anyObject());

    LicenseKeyGroupEntity lkgRetrived = vendorLicenseManagerImpl.getLicenseKeyGroup(lkg);
    verify(licenseKeyGroupDao).get(lkg);

    Assert.assertEquals(lkgRetrived.getId(), lkg.getId());
    Assert.assertEquals(lkgRetrived.getVendorLicenseModelId(), lkg.getVendorLicenseModelId());
    Assert.assertEquals(lkgRetrived.getVersion(), lkg.getVersion());

  }

  @Test
  public void testDeleteLicenseKeyGroup() {
    MultiChoiceOrOther<OperationalScope> multiChoiceOrOther =
        new MultiChoiceOrOther<OperationalScope>();
    Set<OperationalScope> opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Core);
    opScopeChoices.add(OperationalScope.CPU);
    opScopeChoices.add(OperationalScope.Network_Wide);
    multiChoiceOrOther.setChoices(opScopeChoices);
    multiChoiceOrOther.setOther("Other");

    LicenseKeyGroupEntity lkg = createLicenseKeyGroup(vlm1_id, VERSION01, lkg1_id, LKG1_NAME,
        "LKG1 dec", LicenseKeyType.Universal, multiChoiceOrOther);

    lkg.setReferencingFeatureGroups(new HashSet<>());

    doReturn(lkg).when(licenseKeyGroupDao).get(anyObject());

    doNothing().when(vendorLicenseManagerImpl).deleteChildLimits(vlm1_id, VERSION01, lkg1_id);

    doNothing().when(vendorLicenseManagerImpl).deleteUniqueName(anyObject(), anyObject(),
        anyObject(), anyObject());

    vendorLicenseManagerImpl.deleteLicenseKeyGroup(lkg);

    verify(licenseKeyGroupDao).delete(lkg);

  }

  public static LicenseKeyGroupEntity createLicenseKeyGroup(String vlmId, Version version,
                                                            String id,
                                                            String name, String desc,
                                                            LicenseKeyType type,
                                                            MultiChoiceOrOther<OperationalScope> operationalScope) {
    LicenseKeyGroupEntity licenseKeyGroup = new LicenseKeyGroupEntity();
    licenseKeyGroup.setVendorLicenseModelId(vlmId);
    licenseKeyGroup.setVersion(version);
    licenseKeyGroup.setId(id);
    licenseKeyGroup.setName(name);
    licenseKeyGroup.setDescription(desc);
    licenseKeyGroup.setType(type);
    licenseKeyGroup.setOperationalScope(operationalScope);
    return licenseKeyGroup;
  }

  /*public static final String LKG1_NAME = "LKG1 name";
  private static final Version VERSION01 = new Version(0, 1);
  public static final String LKG1_NAME = "LKG1 name";
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

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  /*@BeforeClass
  private void init() {
    licenseKeyGroupDao = LicenseKeyGroupDaoFactory.getInstance().createInterface();
    noSqlDb = NoSqlDbFactory.getInstance().createInterface();

    vlm1Id = vendorLicenseManager.createVendorLicenseModel(VendorLicenseModelTest
        .createVendorLicenseModel("vendor1 name " + CommonMethods.nextUuId(), "vlm1Id dec",
            "icon1")1).getId();
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
    String lkg1Id = vendorLicenseManager.createLicenseKeyGroup(lkg11).getId();
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
      vendorLicenseManager.createLicenseKeyGroup(lkg11).getId();
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

    vendorLicenseManager.updateLicenseKeyGroup(lkg11);

    LicenseKeyGroupEntity loadedLkg1 = vendorLicenseManager.getLicenseKeyGroup(lkg11);
    Assert.assertTrue(loadedLkg1.equals(lkg1));

  }

  @Test(dependsOnMethods = {"updateAndGetTest"})
  public void listTest() {
    Set<OperationalScope> opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Network_Wide);
    LicenseKeyGroupEntity lkg2 =
        createLicenseKeyGroup(vlm1Id, VERSION01, "LKG2", "LKG2 dec", LicenseKeyType.Universal,
            new MultiChoiceOrOther<>(opScopeChoices, null));
    lkg2Id = vendorLicenseManager.createLicenseKeyGroup(lkg21).getId();
    lkg2.setId(lkg2Id);

    Collection<LicenseKeyGroupEntity> loadedLkgs =
        vendorLicenseManager.listLicenseKeyGroups(vlm1Id, null1);
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
        .deleteLicenseKeyGroup(new LicenseKeyGroupEntity(vlm1Id, VERSION01, lkg1Id)1);

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
