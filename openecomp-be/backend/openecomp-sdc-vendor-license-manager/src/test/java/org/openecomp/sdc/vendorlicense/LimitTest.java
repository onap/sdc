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
import org.openecomp.sdc.vendorlicense.dao.LimitDao;
import org.openecomp.sdc.vendorlicense.dao.types.AggregationFunction;
import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LimitType;
import org.openecomp.sdc.vendorlicense.errors.VendorLicenseErrorCodes;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.impl.VendorLicenseManagerImpl;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersioningErrorCodes;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class LimitTest {

  private final String USER1 = "limitTestUser1";
  private final String LT1_NAME = "LT1 name";

  private static final String VLM_ID = "VLM_ID";
  private static final Version VERSION = new Version(0, 1);
  private static final String EPLKG_ID = "ID";
  private static final String LIMIT1_ID = "limit1";
  private static final String LIMIT2_ID = "limit2";

  @Mock
  private VendorLicenseFacade vendorLicenseFacade;

  @Mock
  private LimitDao limitDao;

  @InjectMocks
  @Spy
  private VendorLicenseManagerImpl vendorLicenseManagerImpl;

  public static LimitEntity createLimitEntity(String name, LimitType type, String description,
                                              Version version, String metric,
                                              AggregationFunction aggregationFunction, int unit,
                                              String time) {
    LimitEntity limitEntity = new LimitEntity();
    limitEntity.setName(name);
    limitEntity.setType(type);
    limitEntity.setDescription(description);
    limitEntity.setVersion(version);
    limitEntity.setMetric(metric);
    limitEntity.setAggregationFunction(aggregationFunction);
    limitEntity.setUnit(String.valueOf(unit));
    limitEntity.setTime(time);
    return limitEntity;
  }

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testUpdateLimit() {
    Version version = new Version();
    LimitEntity limitEntity1 = createLimitEntity(LT1_NAME, LimitType.Vendor, "string", version,
        "Core", AggregationFunction.Average, 10, "Hour");
    LimitEntity limitEntity2 = createLimitEntity(LT1_NAME, LimitType.Vendor, "string", version,
        "Tokens", AggregationFunction.Peak, 12, "Month");
    VersionInfo info = new VersionInfo();
    info.getViewableVersions().add(version);
    info.setActiveVersion(version);

    /*doReturn(info).when(vendorLicenseFacade).getVersionInfo(anyObject(), anyObject(), anyObject());*/
    doReturn(true).when(limitDao).isLimitPresent(anyObject());
    doReturn(limitEntity1).when(limitDao).get(anyObject());

    List<LimitEntity> limitEntityList = new ArrayList<>();
    limitEntityList.add(limitEntity1);
    limitEntityList.add(limitEntity2);
    limitEntity1.setId("1234");
    limitEntity2.setId("1234");
    doReturn(limitEntityList).when(vendorLicenseFacade)
        .listLimits(anyObject(), anyObject(), anyObject());

    vendorLicenseManagerImpl.updateLimit(limitEntity2);

    verify(vendorLicenseFacade).updateLimit(anyObject());
  }

  @Test
  public void testUpdateLimitErrorWithSameNameType() {
    try {
      Version version = new Version();
      LimitEntity limitEntity1 = createLimitEntity(LT1_NAME, LimitType.Vendor, "string", version,
          "Core", AggregationFunction.Average, 10, "Hour");
      LimitEntity limitEntity2 = createLimitEntity(LT1_NAME, LimitType.Vendor, "string", version,
          "Tokens", AggregationFunction.Peak, 12, "Month");
      VersionInfo info = new VersionInfo();
      info.getViewableVersions().add(version);
      info.setActiveVersion(version);

/*      doReturn(info).when(vendorLicenseFacade)
          .getVersionInfo(anyObject(), anyObject(), anyObject());*/
      doReturn(limitEntity1).when(limitDao).get(anyObject());

      List<LimitEntity> limitEntityList = new ArrayList<>();
      limitEntityList.add(limitEntity1);
      limitEntityList.add(limitEntity2);
      limitEntity1.setId("1234");
      limitEntity2.setId("9632");
      doReturn(limitEntityList).when(vendorLicenseFacade)
          .listLimits(anyObject(), anyObject(), anyObject());

      vendorLicenseManagerImpl.updateLimit(limitEntity2);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(),
          VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    }
  }

  @Test
  public void testDeleteLimit() {
    Version version = new Version();
    LimitEntity limitEntity = createLimitEntity(LT1_NAME, LimitType.Vendor, "string", version,
        "Core", AggregationFunction.Average, 10, "Hour");
    VersionInfo info = new VersionInfo();
    info.getViewableVersions().add(version);
    info.setActiveVersion(version);

    /*doReturn(info).when(vendorLicenseFacade).getVersionInfo(anyObject(), anyObject(), anyObject());*/
    doReturn(true).when(limitDao).isLimitPresent(anyObject());
    doReturn(limitEntity).when(limitDao).get(anyObject());

    List<LimitEntity> limitEntityList = new ArrayList<>();
    limitEntityList.add(limitEntity);
    limitEntity.setId("1234");

    vendorLicenseManagerImpl.deleteLimit(limitEntity);

    verify(vendorLicenseManagerImpl).deleteLimit(anyObject());
  }

  @Test
  public void testUpdateLimitErrorWithInvalidId() {
    try {
      Version version = new Version();
      LimitEntity limitEntity1 = createLimitEntity(LT1_NAME, LimitType.Vendor, "string", version,
          "Core", AggregationFunction.Average, 10, "Hour");
      LimitEntity limitEntity2 = createLimitEntity(LT1_NAME, LimitType.Vendor, "string", version,
          "Tokens", AggregationFunction.Peak, 12, "Month");
      VersionInfo info = new VersionInfo();
      info.getViewableVersions().add(version);
      info.setActiveVersion(version);

/*      doReturn(info).when(vendorLicenseFacade)
          .getVersionInfo(anyObject(), anyObject(), anyObject());*/
      doReturn(null).when(limitDao).get(anyObject());

      vendorLicenseManagerImpl.updateLimit(limitEntity2);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(),
          VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    }
  }

  @Test
  public void testList() {
    doReturn(Arrays.asList(
        createLimit(VLM_ID, VERSION, EPLKG_ID, LIMIT1_ID),
        createLimit(VLM_ID, VERSION, EPLKG_ID, LIMIT2_ID)))
        .when(vendorLicenseFacade).listLimits(VLM_ID, VERSION, EPLKG_ID);

    final Collection<LimitEntity> limits =
        vendorLicenseManagerImpl.listLimits(VLM_ID, VERSION, EPLKG_ID);
    Assert.assertEquals(limits.size(), 2);
    for (LimitEntity limit : limits) {
      Assert.assertEquals(limit.getName(),
          LIMIT1_ID.equals(limit.getId()) ? LIMIT1_ID + " name" : LIMIT2_ID + " name");
    }
  }

  @Test
  public void testCreateLimit() {
    LimitEntity expected = createLimit(VLM_ID, VERSION, EPLKG_ID, LIMIT1_ID);
    VersionInfo info = new VersionInfo();
    info.getViewableVersions().add(VERSION);
    info.setActiveVersion(VERSION);

    /*doReturn(info).when(vendorLicenseFacade).getVersionInfo(anyObject(), anyObject(), anyObject());*/

    vendorLicenseManagerImpl.createLimit(expected);
    verify(vendorLicenseFacade).createLimit(expected);
  }

  @Test
  public void testCreateWithDuplicateName() {
    LimitEntity expected = createLimit(VLM_ID, VERSION, EPLKG_ID, LIMIT1_ID);
    expected.setType(LimitType.Vendor);

    LimitEntity expectedDiffName = createLimit(VLM_ID, VERSION, EPLKG_ID, LIMIT2_ID);
    expectedDiffName.setName(LIMIT1_ID + " name");
    expectedDiffName.setType(LimitType.Vendor);

    List<LimitEntity> vfcImageList = new ArrayList<LimitEntity>();
    vfcImageList.add(expectedDiffName);
    doReturn(vfcImageList).when(vendorLicenseFacade)
        .listLimits(anyObject(), anyObject(), anyObject());

    VersionInfo info = new VersionInfo();
    info.getViewableVersions().add(VERSION);
    info.setActiveVersion(VERSION);

/*    doReturn(info).when(vendorLicenseFacade).getVersionInfo(anyObject(), anyObject(), anyObject());*/

    try {
      vendorLicenseManagerImpl.createLimit(expected);
      Assert.fail();
    } catch (CoreException ex) {
      Assert.assertEquals(ex.code().id(),
          VendorLicenseErrorCodes.DUPLICATE_LIMIT_NAME_NOT_ALLOWED);
    }
  }

  @Test
  public void testGetNonExistingLimitId_negative() {
    LimitEntity limit = createLimit(VLM_ID, VERSION, EPLKG_ID, "non existing limit id");
    VersionInfo info = new VersionInfo();
    info.getViewableVersions().add(VERSION);
    info.setActiveVersion(VERSION);

    /*doReturn(info).when(vendorLicenseFacade).getVersionInfo(anyObject(), anyObject(), anyObject());*/

    try {
      vendorLicenseManagerImpl.getLimit(limit);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(),
          VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    }
  }

  @Test
  public void testGet() {
    LimitEntity expected = createLimit(VLM_ID, VERSION, EPLKG_ID, LIMIT1_ID);
    expected.setType(LimitType.Vendor);
    expected.setValue(String.valueOf(100));
    expected.setUnit(String.valueOf(10));
    expected.setAggregationFunction(AggregationFunction.Average);
    expected.setMetric("BWTH");
    expected.setTime("Day");

    doReturn(true).when(limitDao).isLimitPresent(anyObject());
    doReturn(expected).when(limitDao).get(anyObject());
    VersionInfo info = new VersionInfo();
    info.getViewableVersions().add(VERSION);
    info.setActiveVersion(VERSION);

    /*doReturn(info).when(vendorLicenseFacade).getVersionInfo(anyObject(), anyObject(), anyObject());*/

    LimitEntity actual = createLimit(VLM_ID, VERSION, EPLKG_ID, LIMIT1_ID);
    vendorLicenseManagerImpl.getLimit(actual);
    Assert.assertEquals(actual.getId(), expected.getId());
    Assert.assertEquals(actual.getName(), expected.getName());
    Assert.assertEquals(actual.getUnit(), expected.getUnit());
    Assert.assertEquals(actual.getValue(), expected.getValue());
    Assert.assertEquals(actual.getAggregationFunction().name(), expected.getAggregationFunction()
        .name());
    Assert.assertEquals(actual.getMetric(), expected.getMetric());

  }

  static LimitEntity createLimit(String vlmId, Version version, String epLkgId, String limitId) {
    LimitEntity limitEntity = new LimitEntity(vlmId, version, epLkgId, limitId);
    limitEntity.setName(limitId + " name");
    limitEntity.setDescription(limitId + " desc");
    limitEntity.setVersion(version);
    limitEntity.setMetric("BWTH");
    limitEntity.setAggregationFunction(AggregationFunction.Average);
    limitEntity.setUnit(String.valueOf(10));
    limitEntity.setTime("Day");
    limitEntity.setValue(String.valueOf(100));
    return limitEntity;
  }
}
