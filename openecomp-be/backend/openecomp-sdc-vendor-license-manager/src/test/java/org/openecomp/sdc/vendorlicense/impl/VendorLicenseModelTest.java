/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.datatypes.model.ItemType;
import org.openecomp.sdc.vendorlicense.VendorLicenseConstants;
import org.openecomp.sdc.vendorlicense.dao.*;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.types.ItemStatus;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by ayalaben on 7/19/2017
 */
public class VendorLicenseModelTest {

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

  private static final boolean MULTITENANCY_ENABLED = true;

  private static final String TEST_TENANT = "test_tenant";

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


  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
  }

  @After
  public void tearDown(){
    vendorLicenseManager = null;
  }

  @Test
  public void testValidate() {
    // TODO: 8/13/2017
    vendorLicenseManager.validate(vlm1_id, null);
    verify(vendorLicenseFacadeMcok).validate(vlm1_id, null);
  }

  @Test
  public void testCreate() {
    VendorLicenseModelEntity vlmEntity = new VendorLicenseModelEntity(vlm1_id, VERSION01);

    vendorLicenseManager.createVendorLicenseModel(vlmEntity);

    verify(vendorLicenseModelDaoMcok).create(vlmEntity);
  }

  @Test
  public void testUpdate() {

    VendorLicenseModelEntity existingVlm = new VendorLicenseModelEntity();
    existingVlm.setVersion(VERSION01);
    existingVlm.setId(vlm1_id);
    existingVlm.setIconRef("icon");
    existingVlm.setVendorName("VLM1");
    existingVlm.setDescription("decription");

    doReturn("VLM1").when(vendorLicenseModelDaoMcok).get(existingVlm);

    VendorLicenseModelEntity updatedVlm = new VendorLicenseModelEntity();
    updatedVlm.setVersion(VERSION01);
    updatedVlm.setId(vlm1_id);
    updatedVlm.setIconRef("icon");
    updatedVlm.setVendorName("VLM1_updated");
    updatedVlm.setDescription("decription");

    doNothing().when(vendorLicenseManager)
        .updateUniqueName(VendorLicenseConstants.UniqueValues.VENDOR_NAME,
            existingVlm.getVendorName(), updatedVlm.getVendorName());

    doReturn(existingVlm).when(vendorLicenseModelDaoMcok).get(any(VendorLicenseModelEntity.class));

    vendorLicenseManager.updateVendorLicenseModel(updatedVlm);

    verify(vendorLicenseModelDaoMcok).update(updatedVlm);
  }

  @Test
  public void testGetVendorLicenseModel() {
    vendorLicenseManager.getVendorLicenseModel(vlm1_id, VERSION01);
    verify(vendorLicenseFacadeMcok).getVendorLicenseModel(vlm1_id, VERSION01);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testDeleteVLMUnsupportedOperation() {
    vendorLicenseManager.deleteVendorLicenseModel(vlm1_id, null); // TODO: 8/13/2017
  }


//  @Test(expectedExceptions = CoreException.class)
//  public void testGetNonExistingVersion_negative() {
//    Version notExistversion = new Version(43, 8);
//    doReturn(null).when(vspInfoDaoMock).get(any(VspDetails.class));
//    vendorSoftwareProductManager.getVsp(VSP_ID, notExistversion);
//  }


  @Test
  public void testCreate_withMultitenancyValidTenant_Success() {
    Assert.assertEquals(MULTITENANCY_ENABLED,true);
    VendorLicenseModelEntity vlmEntity = new VendorLicenseModelEntity();
    vlmEntity.setId(vlm1_id);
    vlmEntity.setVersion(VERSION01);
    vlmEntity.setTenant(TEST_TENANT);
    assertThat("Unauthorized Tenant", getTestRoles().contains(vlmEntity.getTenant()));
    vendorLicenseManager.createVendorLicenseModel(vlmEntity);
    verify(vendorLicenseModelDaoMcok).create(vlmEntity);
  }

 @Test
  public void testCreate_withMultitenancyInvalidTenant_Failure() {
    String invalidTenant="invalid_tenant";
    Assert.assertEquals(MULTITENANCY_ENABLED,true);
    VendorLicenseModelEntity vlmEntity = new VendorLicenseModelEntity();
    vlmEntity.setId(vlm1_id);
    vlmEntity.setVersion(VERSION01);
    vlmEntity.setTenant(invalidTenant);
    Assert.assertFalse(getTestRoles().contains(invalidTenant));
    Assert.assertNotNull(vlmEntity.getTenant());
    vendorLicenseManager.createVendorLicenseModel(vlmEntity);
    assertThat("Unauthorized Tenant", !getTestRoles().contains(vlmEntity.getTenant()));
  }

  @Test
  public void testListVLM_multitenancyWithTenant_FilterList() {
    Assert.assertEquals(MULTITENANCY_ENABLED,true);
    Assert.assertNotNull(getTestRoles());
    assertThat("Unauthorized Tenant", getTestRoles().contains(TEST_TENANT));
    String tenant = TEST_TENANT;
    Assert.assertNotNull(tenant);
    List<Item> expectedItems=new ArrayList<>();
      getTestRoles().stream().forEach(role -> getLicenseModelItems().stream()
              .filter(item -> item.getTenant().contains(role))
              .forEach(item -> expectedItems.add(item)));
    Assert.assertEquals(expectedItems.size(), 1);
  }

  @Test
  public void testListVLM_multitenancyWithInvalidTenant_ReturnEmptylist() {
    Assert.assertEquals(MULTITENANCY_ENABLED,true);
    Assert.assertNotNull(getTestRoles());
    String tenant= "invalid_tenant";
    List<Item> expectedItems=new ArrayList<>();
    List<Item> actualItems=getLicenseModelItems();
    Assert.assertNotNull(tenant);
    getTestRoles().stream().forEach(role -> getLicenseModelItems().stream()
            .filter(item -> item.getTenant()!=null)
            .filter(item -> item.getTenant().contains(tenant))
            .forEach(item -> expectedItems.add(item)));

    Assert.assertEquals(expectedItems.size(), 0);
    Assert.assertNotEquals(expectedItems.containsAll(actualItems), actualItems.containsAll(expectedItems));
  }

  private Set<String> getTestRoles(){
    Set<String> roles = new HashSet<>();
    roles.add("test_admin");
    roles.add("test_tenant");
    return roles;
  }

  private List<Item> getLicenseModelItems(){
    List<Item> items=new ArrayList<>();

    Item itemOne = new Item();
    itemOne.setType(ItemType.vlm.name());
    itemOne.setOwner(USER1);
    itemOne.setStatus(ItemStatus.ACTIVE);
    itemOne.setName("TEST_VENDOR_ONE");
    itemOne.setDescription("TEST_DESCRIPTION");
    itemOne.setTenant(TEST_TENANT);

    Item itemTwo = new Item();
    itemTwo.setType(ItemType.vlm.name());
    itemTwo.setOwner(USER1);
    itemTwo.setStatus(ItemStatus.ACTIVE);
    itemTwo.setName("TEST_VENDOR_TWO");
    itemTwo.setDescription("TEST_DESCRIPTION");
    itemTwo.setTenant("admin_tenant");

    items.add(itemOne);
    items.add(itemTwo);
    return items;
  }

}
