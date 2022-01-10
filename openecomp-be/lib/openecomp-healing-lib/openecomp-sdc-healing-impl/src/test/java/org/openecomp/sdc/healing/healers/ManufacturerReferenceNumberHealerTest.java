/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.healing.healers;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.vendorlicense.dao.FeatureGroupDao;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ManufacturerReferenceNumberHealerTest {

  @Mock
  private VendorLicenseFacade vendorLicenseFacade;
  @Mock
  private FeatureGroupDao featureGroupDao;
  private ManufacturerReferenceNumberHealer manufacturerReferenceNumberHealer;

  private final String ITEM_ID = "ITEM_ID";
  private final Version version = new Version();

  @Before
  public void init(){
    MockitoAnnotations.openMocks(this);
    manufacturerReferenceNumberHealer = new ManufacturerReferenceNumberHealer
        (vendorLicenseFacade, featureGroupDao);
  }

  @Test
  public void testIsHealingNeeded_Positive() {
    Collection<EntitlementPoolEntity> entitlementPoolEntities = new ArrayList<>();
    entitlementPoolEntities.add(new EntitlementPoolEntity(ITEM_ID, version, ""));

    when(vendorLicenseFacade.listEntitlementPools(ITEM_ID, version )).thenReturn(entitlementPoolEntities);
    Assert.assertEquals(TRUE, manufacturerReferenceNumberHealer.isHealingNeeded("ITEM_ID", version));
  }

  @Test
  public void testIsHealingNeeded_Negative() {
    Collection<EntitlementPoolEntity> entitlementPoolEntities = new ArrayList<>();
    EntitlementPoolEntity entitlementPoolEntity = new EntitlementPoolEntity(ITEM_ID, version, "");
    entitlementPoolEntity.setManufacturerReferenceNumber("MRN");
    entitlementPoolEntities.add(entitlementPoolEntity);

    when(vendorLicenseFacade.listEntitlementPools(ITEM_ID, version )).thenReturn(entitlementPoolEntities);
    Assert.assertEquals(FALSE, manufacturerReferenceNumberHealer.isHealingNeeded("ITEM_ID", version));
  }

  @Test
  public void testHeal() throws Exception {

    Collection<EntitlementPoolEntity> entitlementPoolEntities = getEntitlementPoolEntities();
    when(vendorLicenseFacade.listEntitlementPools(ITEM_ID, version)).thenReturn(entitlementPoolEntities);
    doNothing().when(vendorLicenseFacade).updateEntitlementPool(any());
    Collection<LicenseKeyGroupEntity> licenseKeyGroupEntities = getLicenseKeyGroupEntities();
    when(vendorLicenseFacade.listLicenseKeyGroups(ITEM_ID, version)).thenReturn
        (licenseKeyGroupEntities);
    doNothing().when(vendorLicenseFacade).updateLicenseKeyGroup(any());

    FeatureGroupEntity featureGroupEntity = new FeatureGroupEntity();
    when(vendorLicenseFacade.getFeatureGroup(any())).thenReturn(featureGroupEntity);

    Collection<FeatureGroupEntity> featureGroupEntities = new ArrayList<>();
    featureGroupEntities.add(featureGroupEntity);
    when(vendorLicenseFacade.listFeatureGroups(ITEM_ID, version)).thenReturn
        (featureGroupEntities);
    doNothing().when(featureGroupDao).update(any());

    manufacturerReferenceNumberHealer.heal(ITEM_ID, version);

    verify(vendorLicenseFacade, times(1)).updateEntitlementPool(any());
    verify(vendorLicenseFacade,times(1)).updateLicenseKeyGroup(any());
  }

  private Collection<EntitlementPoolEntity> getEntitlementPoolEntities() {
    Set<String> oneReferencingFeatureGroups = new HashSet<String>(Arrays.asList("1"));
    Collection<EntitlementPoolEntity> entitlementPoolEntities = new ArrayList<>();
    EntitlementPoolEntity entitlementPoolEntity = new EntitlementPoolEntity();
    entitlementPoolEntity.setReferencingFeatureGroups(oneReferencingFeatureGroups);
    entitlementPoolEntities.add(entitlementPoolEntity);
    return entitlementPoolEntities;
  }

  private Collection<LicenseKeyGroupEntity> getLicenseKeyGroupEntities() {
    Set<String> oneReferencingFeatureGroups = new HashSet<String>(Arrays.asList("1"));
    Collection<LicenseKeyGroupEntity> licenseKeyGroupEntities = new ArrayList<>();
    LicenseKeyGroupEntity licenseKeyGroupEntity = new LicenseKeyGroupEntity();
    licenseKeyGroupEntity.setReferencingFeatureGroups(oneReferencingFeatureGroups);
    licenseKeyGroupEntities.add(licenseKeyGroupEntity);
    return  licenseKeyGroupEntities;
  }
}
