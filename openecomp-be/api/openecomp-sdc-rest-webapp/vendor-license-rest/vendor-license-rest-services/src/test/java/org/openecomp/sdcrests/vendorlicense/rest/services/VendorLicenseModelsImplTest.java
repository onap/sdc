/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdcrests.vendorlicense.rest.services;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.datatypes.model.ItemType;
import org.openecomp.sdc.itempermissions.PermissionsManager;
import org.openecomp.sdc.notification.dtos.Event;
import org.openecomp.sdc.notification.services.NotificationPropagationManager;
import org.openecomp.sdc.vendorlicense.VendorLicenseConstants;
import org.openecomp.sdc.vendorlicense.VendorLicenseManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.versioning.AsdcItemManager;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.types.ItemStatus;
import org.openecomp.sdcrests.vendorlicense.rest.exception.VendorLicenseModelExceptionSupplier;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class VendorLicenseModelsImplTest {

    @Mock
    private PermissionsManager permissionsManager;
    @Mock
    private NotificationPropagationManager notifier;
    @Mock
    private AsdcItemManager asdcItemManager;
    @Mock
    private VersioningManager versioningManager;
    @Mock
    private VendorLicenseManager vendorLicenseManager;
    @Mock
    private ActivityLogManager activityLogManager;
    @Mock
    private UniqueValueUtil uniqueValueUtil;
    @Mock
    private VendorSoftwareProductInfoDao vendorSoftwareProductInfoDao;

    @InjectMocks
    private VendorLicenseModelsImpl vendorLicenseModels;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deleteLicenseModelSuccessTest() {
        //given
        final String vlmId = "vlmId";
        final String vlmName = "vlmName";
        final String userId = "userId";

        final Item vlmItem = new Item();
        vlmItem.setId(vlmId);
        vlmItem.setType(ItemType.vlm.getName());
        vlmItem.setName(vlmName);
        when(asdcItemManager.get(vlmId)).thenReturn(vlmItem);

        final VspDetails vspDetailsThatDontUseVlm1 = new VspDetails();
        vspDetailsThatDontUseVlm1.setVendorId("otherVendorId");
        final VspDetails vspDetailsThatDontUseVlm2 = new VspDetails();
        vspDetailsThatDontUseVlm2.setVendorId("otherVendorId");
        final List<VspDetails> vspDetailsList = List.of(vspDetailsThatDontUseVlm1, vspDetailsThatDontUseVlm2);
        when(vendorSoftwareProductInfoDao.list(null)).thenReturn(vspDetailsList);

        //when
        final Response response = vendorLicenseModels.deleteLicenseModel(vlmId, userId);
        //then
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        verify(asdcItemManager).delete(vlmItem);
        verify(permissionsManager).deleteItemPermissions(vlmItem.getId());
        verify(uniqueValueUtil).deleteUniqueValue(VendorLicenseConstants.UniqueValues.VENDOR_NAME, vlmItem.getName());
        verify(notifier).notifySubscribers(any(Event.class), eq(userId));
    }

    @Test
    void deleteLicenseModel_cantDeleteVlmInUseTest() {
        //given
        final String vlmId = "vlmId";
        final String vlmName = "vlmName";
        final String userId = "userId";

        final Item vlmItem = new Item();
        vlmItem.setId(vlmId);
        vlmItem.setType(ItemType.vlm.getName());
        vlmItem.setName(vlmName);
        when(asdcItemManager.get(vlmId)).thenReturn(vlmItem);

        final VspDetails vspDetailsThatUsesVlm = new VspDetails();
        vspDetailsThatUsesVlm.setName("VspThatUsesVlm");
        vspDetailsThatUsesVlm.setVendorId(vlmId);
        final VspDetails vspDetailsThatDontUseVlm = new VspDetails();
        vspDetailsThatDontUseVlm.setName("VspThatDontUseVlm");
        vspDetailsThatDontUseVlm.setVendorId("otherVendorId");
        final List<VspDetails> vspDetailsList = List.of(vspDetailsThatUsesVlm, vspDetailsThatDontUseVlm);
        when(vendorSoftwareProductInfoDao.list(null)).thenReturn(vspDetailsList);

        //when
        final CoreException actualException = assertThrows(CoreException.class, () -> vendorLicenseModels.deleteLicenseModel(vlmId, userId));
        //then
        final CoreException expectedException =
            VendorLicenseModelExceptionSupplier.cantDeleteUsedVlm(vlmId, List.of(vspDetailsThatUsesVlm.getName())).get();
        assertEquals(expectedException.code().id(), actualException.code().id());
        assertEquals(expectedException.code().message(), actualException.code().message());
        assertEquals(expectedException.code().category(), actualException.code().category());
        verify(asdcItemManager, never()).delete(vlmItem);
        verify(permissionsManager, never()).deleteItemPermissions(vlmItem.getId());
        verify(uniqueValueUtil, never()).deleteUniqueValue(VendorLicenseConstants.UniqueValues.VENDOR_NAME, vlmItem.getName());
        verify(notifier, never()).notifySubscribers(any(Event.class), eq(userId));
    }

    @Test
    void deleteLicenseModel_cantDeleteCertifiedTest() {
        //given
        final String vlmId = "vlmId";
        final String vlmName = "vlmName";
        final String userId = "userId";

        final Item vlmItem = new Item();
        vlmItem.setId(vlmId);
        vlmItem.setType(ItemType.vlm.getName());
        vlmItem.setName(vlmName);
        vlmItem.setVersionStatusCounters(Map.of(VersionStatus.Certified, 1));
        when(asdcItemManager.get(vlmId)).thenReturn(vlmItem);
        when(vendorSoftwareProductInfoDao.list(null)).thenReturn(Collections.emptyList());

        //when
        final CoreException actualException = assertThrows(CoreException.class, () -> vendorLicenseModels.deleteLicenseModel(vlmId, userId));
        //then
        final CoreException expectedException = VendorLicenseModelExceptionSupplier.cantDeleteCertifiedAndNotArchivedVlm(vlmId).get();
        assertEquals(expectedException.code().id(), actualException.code().id());
        assertEquals(expectedException.code().message(), actualException.code().message());
        assertEquals(expectedException.code().category(), actualException.code().category());
        verify(asdcItemManager, never()).delete(vlmItem);
        verify(permissionsManager, never()).deleteItemPermissions(vlmItem.getId());
        verify(uniqueValueUtil, never()).deleteUniqueValue(VendorLicenseConstants.UniqueValues.VENDOR_NAME, vlmItem.getName());
        verify(notifier, never()).notifySubscribers(any(Event.class), eq(userId));
    }

    @Test
    void deleteLicenseModel_incorrectItemTypeTest() {
        //given
        final String vlmId = "vlmId";

        final Item vlmItem = new Item();
        vlmItem.setId(vlmId);
        vlmItem.setType("incorrectType");
        when(asdcItemManager.get(vlmId)).thenReturn(vlmItem);

        //when/then
        final CoreException actualException = assertThrows(CoreException.class, () -> vendorLicenseModels.deleteLicenseModel(vlmId, "userId"));

        final CoreException expectedException = VendorLicenseModelExceptionSupplier.couldNotFindVlm(vlmId).get();
        assertEquals(expectedException.code().id(), actualException.code().id());
        assertEquals(expectedException.code().message(), actualException.code().message());
    }

    @Test
    void deleteLicenseModel_CertifiedAndArchived_SuccessTest() {
        final String vlmId = "vlmId";
        final String userId = "userId";
        final Item vlmItem = new Item();
        vlmItem.setId(vlmId);
        vlmItem.setType(ItemType.vlm.getName());
        vlmItem.setStatus(ItemStatus.ARCHIVED);
        vlmItem.addVersionStatus(VersionStatus.Certified);
        when(asdcItemManager.get(vlmId)).thenReturn(vlmItem);

        final Response response = vendorLicenseModels.deleteLicenseModel(vlmId, userId);

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        verify(asdcItemManager).delete(vlmItem);
        verify(permissionsManager).deleteItemPermissions(vlmItem.getId());
        verify(uniqueValueUtil).deleteUniqueValue(VendorLicenseConstants.UniqueValues.VENDOR_NAME, vlmItem.getName());
        verify(notifier).notifySubscribers(any(Event.class), eq(userId));
    }

    @Test
    void deleteLicenseModel_CertifiedAndNotArchived_FailTest() {
        final String vlmId = "vlmId";
        final String userId = "userId";
        final String errorMessageText = "Vendor License Model 'vlmId' has been certified, but not archived and cannot be deleted.";
        final Item vlmItem = new Item();
        vlmItem.setId(vlmId);
        vlmItem.setType(ItemType.vlm.getName());
        vlmItem.setStatus(ItemStatus.ACTIVE);
        vlmItem.addVersionStatus(VersionStatus.Certified);
        when(asdcItemManager.get(vlmId)).thenReturn(vlmItem);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            vendorLicenseModels.deleteLicenseModel(vlmId, userId);
        });

        assertEquals(errorMessageText, exception.getMessage());
    }
}