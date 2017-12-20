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
import org.openecomp.sdc.vendorlicense.dao.EntitlementPoolDao;
import org.openecomp.sdc.vendorlicense.dao.LimitDao;
import org.openecomp.sdc.vendorlicense.dao.types.AggregationFunction;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementMetric;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementTime;
import org.openecomp.sdc.vendorlicense.dao.types.MultiChoiceOrOther;
import org.openecomp.sdc.vendorlicense.dao.types.OperationalScope;
import org.openecomp.sdc.vendorlicense.dao.types.ThresholdUnit;
import org.openecomp.sdc.vendorlicense.errors.VendorLicenseErrorCodes;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class EntitlementPoolTest {

  //JUnit Test Cases using Mockito
  private final String USER1 = "epTestUser1";
  private final String EP1_NAME = "EP1 name";
  private final String EP2_NAME = "EP2 name";
  private final String LT1_NAME = "LT1 name";
  private static String vlm1_id = "vlm1_id";
  private static String ep1_id = "ep1_id";
  private static String ep2_id = "ep2_id";
  public static final Version VERSION01 = new Version(0, 1);

  @Mock
  private VendorLicenseFacade vendorLicenseFacade;

  @Mock
  private EntitlementPoolDao entitlementPoolDao;
  @Mock
  private LimitDao limitDao;

  @InjectMocks
  @Spy
  private VendorLicenseManagerImpl vendorLicenseManagerImpl;

  public EntitlementPoolEntity createEntitlementPool(String vlmId, Version version, String id,
                                                     String name, String desc, int threshold,
                                                     ThresholdUnit thresholdUnit,
                                                     EntitlementMetric entitlementMetricChoice,
                                                     String entitlementMetricOther,
                                                     String increments,
                                                     AggregationFunction aggregationFunctionChoice,
                                                     String aggregationFunctionOther,
                                                     Set<OperationalScope> operationalScopeChoices,
                                                     String operationalScopeOther,
                                                     EntitlementTime timeChoice,
                                                     String timeOther, String sku) {
    EntitlementPoolEntity entitlementPool = new EntitlementPoolEntity();
    entitlementPool.setVendorLicenseModelId(vlmId);
    entitlementPool.setId(id);
    entitlementPool.setVersion(version);
    entitlementPool.setName(name);
    entitlementPool.setDescription(desc);
    entitlementPool.setThresholdValue(threshold);
    entitlementPool.setThresholdUnit(thresholdUnit);
    entitlementPool.setIncrements(increments);
    entitlementPool.setOperationalScope(
        new MultiChoiceOrOther<>(operationalScopeChoices, operationalScopeOther));
    return entitlementPool;
  }

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void createTest() {
    Set<OperationalScope> opScopeChoices;
    opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Core);
    opScopeChoices.add(OperationalScope.CPU);
    opScopeChoices.add(OperationalScope.Network_Wide);
    EntitlementPoolEntity ep2 =
        createEntitlementPool("vlm1Id", null, ep1_id, EP1_NAME, "EP2 dec", 70, ThresholdUnit
                .Absolute,
            EntitlementMetric.Other, "exception metric2", "inc2", AggregationFunction.Average, null,
            opScopeChoices, null, EntitlementTime.Other, "time2", "sku2");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    ep2.setStartDate(LocalDate.now().format(formatter));
    ep2.setExpiryDate(LocalDate.now().plusDays(1L).format(formatter));

    vendorLicenseManagerImpl.createEntitlementPool(ep2);
    verify(vendorLicenseFacade).createEntitlementPool(ep2);

  }

  @Test(expectedExceptions = CoreException.class, expectedExceptionsMessageRegExp = "Vendor " +
      "license model with id vlm1_id has invalid date range.")
  public void createWithInvalidStartExpiryDateTest() {

    Set<OperationalScope> opScopeChoices;
    opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Core);
    opScopeChoices.add(OperationalScope.CPU);
    opScopeChoices.add(OperationalScope.Network_Wide);
    EntitlementPoolEntity ep2 =
        createEntitlementPool("vlm2Id", null, ep1_id, EP1_NAME, "EP2 dec", 70,
            ThresholdUnit.Absolute,
            EntitlementMetric.Other, "exception metric2", "inc2", AggregationFunction.Average, null,
            opScopeChoices, null, EntitlementTime.Other, "time2", "sku2");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    ep2.setStartDate(LocalDate.now().format(formatter));
    ep2.setExpiryDate(LocalDate.now().minusDays(2L).format(formatter));
    ep2.setVendorLicenseModelId(vlm1_id);
    vendorLicenseManagerImpl.createEntitlementPool(ep2).getId();
    Assert.fail();

  }

  @Test(expectedExceptions = CoreException.class, expectedExceptionsMessageRegExp = "Vendor " +
      "license model with id vlm1_id has invalid date range.")
  public void createWithoutStartDateTest() {

    Set<OperationalScope> opScopeChoices;
    opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Core);
    opScopeChoices.add(OperationalScope.CPU);
    opScopeChoices.add(OperationalScope.Network_Wide);
    EntitlementPoolEntity ep2 =
        createEntitlementPool("vlm3Id", null, ep1_id, EP1_NAME, "EP2 dec", 70,
            ThresholdUnit.Absolute,
            EntitlementMetric.Other, "exception metric2", "inc2", AggregationFunction.Average, null,
            opScopeChoices, null, EntitlementTime.Other, "time2", "sku2");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    ep2.setExpiryDate(LocalDate.now().plusDays(2L).format(formatter));
    ep2.setVendorLicenseModelId(vlm1_id);
    vendorLicenseManagerImpl.createEntitlementPool(ep2).getId();
    Assert.fail();

  }

  @Test(expectedExceptions = CoreException.class, expectedExceptionsMessageRegExp = "Vendor " +
      "license model with id vlm1_id has invalid date range.")
  public void createWithSameStartExpiryDateTest() {

    Set<OperationalScope> opScopeChoices;
    opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Core);
    opScopeChoices.add(OperationalScope.CPU);
    opScopeChoices.add(OperationalScope.Network_Wide);
    EntitlementPoolEntity ep2 =
        createEntitlementPool("vlm4Id", null, ep1_id, EP1_NAME, "EP2 dec", 70,
            ThresholdUnit.Absolute,
            EntitlementMetric.Other, "exception metric2", "inc2", AggregationFunction.Average, null,
            opScopeChoices, null, EntitlementTime.Other, "time2", "sku2");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    ep2.setStartDate(LocalDate.now().format(formatter));
    ep2.setExpiryDate(LocalDate.now().format(formatter));
    ep2.setVendorLicenseModelId(vlm1_id);
    vendorLicenseManagerImpl.createEntitlementPool(ep2).getId();
    Assert.fail();
  }

  @Test
  public void testUpdate() {
    Set<OperationalScope> opScopeChoices;
    opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Core);
    opScopeChoices.add(OperationalScope.CPU);
    opScopeChoices.add(OperationalScope.Network_Wide);
    EntitlementPoolEntity ep2 =
        createEntitlementPool(vlm1_id, VERSION01, ep1_id, EP1_NAME, "EP2 dec", 70,
            ThresholdUnit
                .Absolute,
            EntitlementMetric.Other, "exception metric2", "inc2", AggregationFunction.Average, null,
            opScopeChoices, null, EntitlementTime.Other, "time2", "sku2");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    ep2.setStartDate(LocalDate.now().minusDays(3L).format(formatter));
    ep2.setExpiryDate(LocalDate.now().minusDays(2L).format(formatter));

    vendorLicenseManagerImpl.updateEntitlementPool(ep2);
  }

  @Test(expectedExceptions = CoreException.class, expectedExceptionsMessageRegExp = "Vendor " +
      "license model with id vlm1_id has invalid date range.")
  public void updateWithInvalidStartExpiryDateTest() {

    Set<OperationalScope> opScopeChoices;
    opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Core);
    opScopeChoices.add(OperationalScope.CPU);
    opScopeChoices.add(OperationalScope.Network_Wide);
    EntitlementPoolEntity ep2 =
        createEntitlementPool("vlm2Id", null, ep1_id, EP1_NAME, "EP2 dec", 70,
            ThresholdUnit.Absolute,
            EntitlementMetric.Other, "exception metric2", "inc2", AggregationFunction.Average, null,
            opScopeChoices, null, EntitlementTime.Other, "time2", "sku2");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    ep2.setStartDate(LocalDate.now().format(formatter));
    ep2.setExpiryDate(LocalDate.now().minusDays(2L).format(formatter));
    ep2.setVendorLicenseModelId(vlm1_id);
    vendorLicenseManagerImpl.updateEntitlementPool(ep2);
    Assert.fail();

  }

  @Test
  public void updateWithoutStartDateTest() {
    try {

      Set<OperationalScope> opScopeChoices;
      opScopeChoices = new HashSet<>();
      opScopeChoices.add(OperationalScope.Core);
      opScopeChoices.add(OperationalScope.CPU);
      opScopeChoices.add(OperationalScope.Network_Wide);
      EntitlementPoolEntity ep2 =
          createEntitlementPool("vlm3Id", null, ep1_id, EP1_NAME, "EP2 dec", 70,
              ThresholdUnit.Absolute,
              EntitlementMetric.Other, "exception metric2", "inc2", AggregationFunction.Average,
              null,
              opScopeChoices, null, EntitlementTime.Other, "time2", "sku2");
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
      ep2.setExpiryDate(LocalDate.now().plusDays(2L).format(formatter));
      vendorLicenseManagerImpl.updateEntitlementPool(ep2);
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
      EntitlementPoolEntity ep2 =
          createEntitlementPool("vlm4Id", null, ep1_id, EP1_NAME, "EP2 dec", 70,
              ThresholdUnit.Absolute,
              EntitlementMetric.Other, "exception metric2", "inc2", AggregationFunction.Average,
              null,
              opScopeChoices, null, EntitlementTime.Other, "time2", "sku2");
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
      ep2.setStartDate(LocalDate.now().format(formatter));
      ep2.setExpiryDate(LocalDate.now().format(formatter));
      vendorLicenseManagerImpl.updateEntitlementPool(ep2);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), VendorLicenseErrorCodes.DATE_RANGE_INVALID);
    }
  }

  @Test
  public void deleteEntitlementPoolTest() {
    Set<OperationalScope> opScopeChoices;
    opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Core);
    opScopeChoices.add(OperationalScope.CPU);
    opScopeChoices.add(OperationalScope.Network_Wide);

    EntitlementPoolEntity entitlementPool =
        createEntitlementPool(vlm1_id, VERSION01, ep1_id, EP1_NAME, "EP2 dec", 70,
            ThresholdUnit.Absolute, EntitlementMetric.Other, "exception metric2", "inc2",
            AggregationFunction.Average, null,
            opScopeChoices, null, EntitlementTime.Other, "time2", "sku2");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    entitlementPool.setStartDate(LocalDate.now().format(formatter));
    entitlementPool.setExpiryDate(LocalDate.now().plusDays(1L).format(formatter));

    doReturn(entitlementPool).when(entitlementPoolDao).get(anyObject());

    doNothing().when(vendorLicenseManagerImpl).deleteChildLimits(vlm1_id, VERSION01, ep1_id);

    doNothing().when(vendorLicenseManagerImpl).deleteUniqueName(anyObject(), anyObject(),
        anyObject(), anyObject());

    vendorLicenseManagerImpl.deleteEntitlementPool(entitlementPool);

    verify(entitlementPoolDao).delete(entitlementPool);
  }

  @Test
  public void testGetEntitlementPool() {
    Set<OperationalScope> opScopeChoices;
    opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Core);
    opScopeChoices.add(OperationalScope.CPU);
    opScopeChoices.add(OperationalScope.Network_Wide);

    EntitlementPoolEntity entitlementPool =
        createEntitlementPool(vlm1_id, VERSION01, ep1_id, EP1_NAME, "EP2 dec", 70,
            ThresholdUnit.Absolute, EntitlementMetric.Other, "exception metric2", "inc2",
            AggregationFunction.Average, null,
            opScopeChoices, null, EntitlementTime.Other, "time2", "sku2");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy'T'HH:mm:ss'Z'");

    entitlementPool.setStartDate(LocalDateTime.now().format(formatter));
    entitlementPool.setExpiryDate(LocalDateTime.now().plusDays(1L).format(formatter));

    doReturn(entitlementPool).when(entitlementPoolDao).get(anyObject());

    EntitlementPoolEntity retrived = vendorLicenseManagerImpl.getEntitlementPool(entitlementPool);

    Assert.assertEquals(retrived.getId(), entitlementPool.getId());
    Assert.assertEquals(retrived.getVendorLicenseModelId(),
        entitlementPool.getVendorLicenseModelId());
    Assert.assertEquals(retrived.getVersion(), entitlementPool.getVersion());
  }

  @Test
  public void testListEntitlmentPool() {

    Set<OperationalScope> opScopeChoices;
    opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Core);
    opScopeChoices.add(OperationalScope.CPU);
    opScopeChoices.add(OperationalScope.Network_Wide);

    doReturn(Arrays.asList(
        createEntitlementPool(vlm1_id, VERSION01, ep1_id, EP1_NAME, "EP1 dec", 70,
            ThresholdUnit.Absolute, EntitlementMetric.Other, "exception metric1",
            "inc1", AggregationFunction.Average, null,
            opScopeChoices, null, EntitlementTime.Other, "time1", "sku1"),
        createEntitlementPool(vlm1_id, VERSION01, ep2_id, EP2_NAME, "EP2 dec", 70,
            ThresholdUnit.Absolute, EntitlementMetric.Other, "exception metric2",
            "inc2", AggregationFunction.Average, null,
            opScopeChoices, null, EntitlementTime.Other, "time2", "sku2")))
        .when(vendorLicenseFacade).listEntitlementPools(vlm1_id, VERSION01);

    Collection<EntitlementPoolEntity> EPs =
        vendorLicenseManagerImpl.listEntitlementPools(vlm1_id, VERSION01);

    verify(vendorLicenseFacade).listEntitlementPools(vlm1_id, VERSION01);
    Assert.assertEquals(EPs.size(), 2);
    EPs.forEach(ep -> Assert.assertTrue(ep.getId().matches(ep1_id + "|" + ep2_id)));
  }


  /*  @Test
    public void deleteEntitlementPoolTest() {
        Set<OperationalScope> opScopeChoices;
        opScopeChoices = new HashSet<>();
        opScopeChoices.add(OperationalScope.Core);
        opScopeChoices.add(OperationalScope.CPU);
        opScopeChoices.add(OperationalScope.Network_Wide);

        EntitlementPoolEntity entitlementPool =
                createEntitlementPool("vlm1Id", null, EP1_NAME, "EP2 dec", 70, ThresholdUnit.Absolute,
                        EntitlementMetric.Other, "exception metric2", "inc2", AggregationFunction.Average, null,
                        opScopeChoices, null, EntitlementTime.Other, "time2", "sku2");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        entitlementPool.setStartDate(LocalDate.now().format(formatter));
        entitlementPool.setExpiryDate(LocalDate.now().plusDays(1L).format(formatter));

        VersionInfo info = new VersionInfo();
        Version version = new Version();
        info.getViewableVersions().add(version);
        info.setActiveVersion(version);
        doReturn(info).when(vendorLicenseFacade).getVersionInfo(anyObject(),anyObject(),anyObject());

        LimitEntity limitEntity = LimitTest.createLimitEntity(LT1_NAME,LimitType.Vendor,"string",version,
                "Core",AggregationFunction.Average,10,"Hour");

        ArrayList<LimitEntity> limitEntityList = new ArrayList();
        limitEntityList.add(limitEntity);

        doReturn(entitlementPool).when(entitlementPoolDao).get(anyObject());
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

            Field epField = VendorLicenseManagerImpl.class.getDeclaredField("entitlementPoolDao");
            epField.setAccessible(true);
            modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(epField, epField.getModifiers() & ~Modifier.FINAL);
            epField.set(null, entitlementPoolDao);
        } catch(NoSuchFieldException | IllegalAccessException e)
        {
            Assert.fail();
        }

        vendorLicenseManagerImpl.deleteEntitlementPool(entitlementPool, USER1);

        verify(limitDao).delete(anyObject());
    }

    @Test
    public void deleteEntitlementPoolInvalidTest() {
        try {
            Set<OperationalScope> opScopeChoices;
            opScopeChoices = new HashSet<>();
            opScopeChoices.add(OperationalScope.Core);
            opScopeChoices.add(OperationalScope.CPU);
            opScopeChoices.add(OperationalScope.Network_Wide);

            EntitlementPoolEntity entitlementPool =
                createEntitlementPool("vlm1Id", null, EP1_NAME, "EP2 dec", 70, ThresholdUnit.Absolute,
                    EntitlementMetric.Other, "exception metric2", "inc2", AggregationFunction.Average, null,
                    opScopeChoices, null, EntitlementTime.Other, "time2", "sku2");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            entitlementPool.setStartDate(LocalDate.now().format(formatter));
            entitlementPool.setExpiryDate(LocalDate.now().plusDays(1L).format(formatter));

            VersionInfo info = new VersionInfo();
            Version version = new Version();
            info.getViewableVersions().add(version);
            info.setActiveVersion(version);
            doReturn(info).when(vendorLicenseFacade).getVersionInfo(anyObject(),anyObject(),anyObject());

            LimitEntity limitEntity = LimitTest.createLimitEntity(LT1_NAME,LimitType.Vendor,"string",version,
                "Core",AggregationFunction.Average,10,"Hour");

            ArrayList<LimitEntity> limitEntityList = new ArrayList();
            limitEntityList.add(limitEntity);

            doReturn(entitlementPool).when(entitlementPoolDao).get(anyObject());
            doReturn(limitEntityList).when(vendorLicenseFacade).listLimits(anyObject(), anyObject(), anyObject(), anyObject());
            doReturn(false).when(limitDao).isLimitPresent(anyObject());

            try {
                Field limitField = VendorLicenseManagerImpl.class.getDeclaredField("limitDao");
                limitField.setAccessible(true);
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(limitField, limitField.getModifiers() & ~Modifier.FINAL);
                limitField.set(null, limitDao);

                Field epField = VendorLicenseManagerImpl.class.getDeclaredField("entitlementPoolDao");
                epField.setAccessible(true);
                modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(epField, epField.getModifiers() & ~Modifier.FINAL);
                epField.set(null, entitlementPoolDao);
            } catch(NoSuchFieldException | IllegalAccessException e)
            {
                Assert.fail();
            }

            vendorLicenseManagerImpl.deleteEntitlementPool(entitlementPool, USER1);
        } catch (CoreException exception) {
            Assert.assertEquals(exception.code().id(), VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
        }
    } */

 /* private static final String USER1 = "epTestUser1";
  private static final String USER2 = "epTestUser2";
  private static final String EP1_V01_DESC = "EP1 desc";
  private static final Version VERSION01 = new Version(0, 1);
  private static final Version VERSION03 = new Version(0, 3);
  private static final String EP1_NAME = "EP1 name";
  private static final String EP2_NAME = "EP2 name";

  private static VendorLicenseManager vendorLicenseManager = new VendorLicenseManagerImpl();
  private static EntitlementPoolDao entitlementPoolDao;

  private static String vlm1Id;
  private static String vlm2Id;
  private static String ep1Id;
  private static String ep2Id;

  public static EntitlementPoolEntity createEntitlementPool(String vlmId, Version version,
                                                            String name, String desc, int threshold,
                                                            ThresholdUnit thresholdUnit,
                                                            EntitlementMetric entitlementMetricChoice,
                                                            String entitlementMetricOther,
                                                            String increments,
                                                            AggregationFunction aggregationFunctionChoice,
                                                            String aggregationFunctionOther,
                                                            Set<OperationalScope> operationalScopeChoices,
                                                            String operationalScopeOther,
                                                            EntitlementTime timeChoice,
                                                            String timeOther, String sku) {
    EntitlementPoolEntity entitlementPool = new EntitlementPoolEntity();
    entitlementPool.setVendorLicenseModelId(vlmId);
    entitlementPool.setVersion(version);
    entitlementPool.setName(name);
    entitlementPool.setDescription(desc);
    entitlementPool.setThresholdValue(threshold);
    entitlementPool.setThresholdUnit(thresholdUnit);
    entitlementPool
        .setEntitlementMetric(new ChoiceOrOther<>(entitlementMetricChoice, entitlementMetricOther));
    entitlementPool.setIncrements(increments);
    entitlementPool.setAggregationFunction(
        new ChoiceOrOther<>(aggregationFunctionChoice, aggregationFunctionOther));
    entitlementPool.setOperationalScope(
        new MultiChoiceOrOther<>(operationalScopeChoices, operationalScopeOther));
    entitlementPool.setTime(new ChoiceOrOther<>(timeChoice, timeOther));
    return entitlementPool;
  }

  private static void assertEntitlementPoolsEquals(EntitlementPoolEntity actual,
                                                   EntitlementPoolEntity expected) {
    Assert.assertEquals(actual.getVendorLicenseModelId(), expected.getVendorLicenseModelId());
    Assert.assertEquals(actual.getVersion(), expected.getVersion());
    Assert.assertEquals(actual.getId(), expected.getId());
    Assert.assertEquals(actual.getName(), expected.getName());
    Assert.assertEquals(actual.getDescription(), expected.getDescription());
    Assert.assertEquals(actual.getThresholdValue(), expected.getThresholdValue());
    Assert.assertEquals(actual.getThresholdUnit(), expected.getThresholdUnit());
    Assert.assertEquals(actual.getEntitlementMetric(), expected.getEntitlementMetric());
    Assert.assertEquals(actual.getIncrements(), expected.getIncrements());
    Assert.assertEquals(actual.getAggregationFunction(), expected.getAggregationFunction());
    Assert.assertEquals(actual.getOperationalScope(), expected.getOperationalScope());
    Assert.assertEquals(actual.getTime(), expected.getTime());
  }

  @BeforeClass
  private void init() {
    entitlementPoolDao = EntitlementPoolDaoFactory.getInstance().createInterface();
    vlm1Id = vendorLicenseManager.createVendorLicenseModel(VendorLicenseModelTest
            .createVendorLicenseModel("vendor1 name " + CommonMethods.nextUuId(), "vlm1 dec", "icon1"),
        USER1).getId();
    vlm2Id = vendorLicenseManager.createVendorLicenseModel(VendorLicenseModelTest
            .createVendorLicenseModel("vendor2 name " + CommonMethods.nextUuId(), "vlm2 dec", "icon2"),
        USER1).getId();
  }

  @Test
  public void emptyListTest() {
    Collection<EntitlementPoolEntity> entitlementPools =
        vendorLicenseManager.listEntitlementPools(vlm1Id, null, USER1);
    Assert.assertEquals(entitlementPools.size(), 0);
  }

  @Test(dependsOnMethods = "emptyListTest")
  public void createTest() {
    ep1Id = testCreate(vlm1Id, EP1_NAME);

    Set<OperationalScope> opScopeChoices;
    opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Core);
    opScopeChoices.add(OperationalScope.CPU);
    opScopeChoices.add(OperationalScope.Network_Wide);
    EntitlementPoolEntity ep2 =
        createEntitlementPool(vlm1Id, null, EP2_NAME, "EP2 dec", 70, ThresholdUnit.Absolute,
            EntitlementMetric.Other, "exception metric2", "inc2", AggregationFunction.Average, null,
            opScopeChoices, null, EntitlementTime.Other, "time2", "sku2");
    ep2Id = vendorLicenseManager.createEntitlementPool(ep2, USER1).getId();
    ep2.setId(ep2Id);
  }

  private String testCreate(String vlmId, String name) {
    Set<OperationalScope> opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Other);
    EntitlementPoolEntity ep1 =
        createEntitlementPool(vlmId, null, name, EP1_V01_DESC, 80, ThresholdUnit.Percentage,
            EntitlementMetric.Core, null, "inc1", AggregationFunction.Other, "agg func1",
            opScopeChoices, "op scope1", EntitlementTime.Other, "time1", "sku1");
    String ep1Id = vendorLicenseManager.createEntitlementPool(ep1, USER1).getId();
    ep1.setId(ep1Id);

    EntitlementPoolEntity loadedEp1 = entitlementPoolDao.get(ep1);
    Assert.assertTrue(loadedEp1.equals(ep1));
    return ep1Id;
  }

  @Test(dependsOnMethods = {"createTest"})
  public void testCreateWithExistingName_negative() {
    testCreateWithExistingName_negative(vlm1Id, EP1_NAME);
  }

  @Test(dependsOnMethods = {"createTest"})
  public void testCreateWithExistingNameUnderOtherVlm() {
    testCreate(vlm2Id, EP1_NAME);
  }

  @Test(dependsOnMethods = {"testCreateWithExistingName_negative"})
  public void updateAndGetTest() {
    EntitlementPoolEntity emptyEp1 = new EntitlementPoolEntity(vlm1Id, VERSION01, ep1Id);

    EntitlementPoolEntity ep1 = entitlementPoolDao.get(emptyEp1);
    ep1.setEntitlementMetric(new ChoiceOrOther<>(EntitlementMetric.Other, "exception metric1 updated"));
    ep1.setAggregationFunction(new ChoiceOrOther<>(AggregationFunction.Other, "agg func1 updated"));

    vendorLicenseManager.updateEntitlementPool(ep1, USER1);

    EntitlementPoolEntity loadedEp1 = vendorLicenseManager.getEntitlementPool(emptyEp1, USER1);
    assertEntitlementPoolsEquals(loadedEp1, ep1);
  }

  @Test(dependsOnMethods = {"updateAndGetTest"})
  public void testGetNonExistingVersion_negative() {
    try {
      vendorLicenseManager
          .getEntitlementPool(new EntitlementPoolEntity(vlm1Id, new Version(48, 83), ep1Id), USER1);
      Assert.assertTrue(false);
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), VersioningErrorCodes.REQUESTED_VERSION_INVALID);
    }
  }

  @Test(dependsOnMethods = {"updateAndGetTest"})
  public void testGetOtherUserCandidateVersion_negative() {
    vendorLicenseManager.checkin(vlm1Id, USER1);
    vendorLicenseManager.checkout(vlm1Id, USER2);
    try {
      vendorLicenseManager
          .getEntitlementPool(new EntitlementPoolEntity(vlm1Id, new Version(0, 2), ep1Id), USER1);
      Assert.assertTrue(false);
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), VersioningErrorCodes.REQUESTED_VERSION_INVALID);
    }
  }

  @Test(dependsOnMethods = {"testGetOtherUserCandidateVersion_negative"})
  public void testGetCandidateVersion() {
    EntitlementPoolEntity ep = new EntitlementPoolEntity(vlm1Id, new Version(0, 2), ep1Id);
    ep.setDescription("updated!");
    vendorLicenseManager.updateEntitlementPool(ep, USER2);

    EntitlementPoolEntity actualEp = vendorLicenseManager.getEntitlementPool(ep, USER2);
    EntitlementPoolEntity expectedEp = entitlementPoolDao.get(ep);

    Assert.assertEquals(actualEp.getDescription(), ep.getDescription());
    assertEntitlementPoolsEquals(actualEp, expectedEp);
  }

  @Test(dependsOnMethods = {"testGetCandidateVersion"})
  public void testGetOldVersion() {
    vendorLicenseManager.checkin(vlm1Id, USER2);
    EntitlementPoolEntity actualEp = vendorLicenseManager
        .getEntitlementPool(new EntitlementPoolEntity(vlm1Id, new Version(0, 1), ep1Id), USER2);
    Assert.assertEquals(actualEp.getDescription(), EP1_V01_DESC);
  }

  @Test(dependsOnMethods = {"testGetOldVersion"})
  public void listTest() {
    Collection<EntitlementPoolEntity> loadedEps =
        vendorLicenseManager.listEntitlementPools(vlm1Id, null, USER1);
    Assert.assertEquals(loadedEps.size(), 2);

    int existingCounter = 0;
    for (EntitlementPoolEntity loadedEp : loadedEps) {
      if (ep2Id.equals(loadedEp.getId()) || ep1Id.equals(loadedEp.getId())) {
        existingCounter++;
      }
    }

    Assert.assertEquals(existingCounter, 2);
  }

  @Test(dependsOnMethods = {"listTest"})
  public void deleteTest() {
    vendorLicenseManager.checkout(vlm1Id, USER1);
    EntitlementPoolEntity emptyEp1 = new EntitlementPoolEntity(vlm1Id, null, ep1Id);
    vendorLicenseManager.deleteEntitlementPool(emptyEp1, USER1);

    emptyEp1.setVersion(VERSION03);
    EntitlementPoolEntity loadedEp1 = entitlementPoolDao.get(emptyEp1);
    Assert.assertEquals(loadedEp1, null);

    Collection<EntitlementPoolEntity> loadedEps =
        entitlementPoolDao.list(new EntitlementPoolEntity(vlm1Id, VERSION03, null));
    Assert.assertEquals(loadedEps.size(), 1);
    Assert.assertEquals(loadedEps.iterator().next().getId(), ep2Id);
  }

  @Test(dependsOnMethods = "deleteTest")
  public void listOldVersionTest() {
    Collection<EntitlementPoolEntity> loadedEps =
        vendorLicenseManager.listEntitlementPools(vlm1Id, VERSION01, USER1);
    Assert.assertEquals(loadedEps.size(), 2);
  }

  @Test(dependsOnMethods = "deleteTest")
  public void testCreateWithRemovedName() {
    testCreate(vlm1Id, EP1_NAME);
  }

  @Test(dependsOnMethods = "deleteTest")
  public void testCreateWithExistingNameAfterCheckout_negative() {
    testCreateWithExistingName_negative(vlm1Id, EP2_NAME);
  }

  private void testCreateWithExistingName_negative(String vlmId, String epName) {
    try {
      EntitlementPoolEntity ep1 =
          createEntitlementPool(vlmId, null, epName, EP1_V01_DESC, 80, ThresholdUnit.Percentage,
              EntitlementMetric.Core, null, "inc1", AggregationFunction.Other, "agg func1",
              Collections.singleton(OperationalScope.Other), "op scope1", EntitlementTime.Other,
              "time1", "sku1");
      vendorLicenseManager.createEntitlementPool(ep1, USER1).getId();
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), UniqueValueUtil.UNIQUE_VALUE_VIOLATION);
    }
  }
*/
}

