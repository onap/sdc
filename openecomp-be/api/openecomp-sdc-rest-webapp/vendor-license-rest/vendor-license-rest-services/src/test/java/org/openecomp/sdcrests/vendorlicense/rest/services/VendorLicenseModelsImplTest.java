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
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.tenant.TenantContext;
import org.openecomp.sdc.common.tenant.TenantGuard;
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
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.types.ItemStatus;
import org.openecomp.sdcrests.item.types.ItemDto;
import org.openecomp.sdcrests.vendorlicense.rest.exception.VendorLicenseModelExceptionSupplier;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelRequestDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void deleteLicenseModel_cantDeleteCertifiedAndNotArchivedTest() {
        //given
        final String vlmId = "vlmId";
        final String vlmName = "vlmName";
        final String userId = "userId";

        final Item vlmItem = new Item();
        vlmItem.setId(vlmId);
        vlmItem.setType(ItemType.vlm.getName());
        vlmItem.setName(vlmName);
        vlmItem.setVersionStatusCounters(Map.of(VersionStatus.Certified, 1));
        vlmItem.setStatus(ItemStatus.ACTIVE);
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
    void deleteLicenseModel_CertifiedAndArchivedTest() {
        //given
        final String vlmId = "vlmId";
        final String userId = "userId";
        final Item vlmItem = new Item();
        vlmItem.setId(vlmId);
        vlmItem.setType(ItemType.vlm.getName());
        vlmItem.setStatus(ItemStatus.ARCHIVED);
        vlmItem.addVersionStatus(VersionStatus.Certified);
        when(asdcItemManager.get(vlmId)).thenReturn(vlmItem);

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
    void createLicenseModelRefusesWrongTenantWhenEnabled(@TempDir Path dir) throws IOException {
        String previousConfig = enableMultitenancy(dir);
        try {
            HttpServletRequest hreq = mock(HttpServletRequest.class);
            when(hreq.getAttribute(TenantContext.ATTRIBUTE)).thenReturn(new TenantContext(List.of("tenant-a")));

            Response response = vendorLicenseModels.createLicenseModel(vlmRequest("tenant-b"), "userId", hreq);

            assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
            assertEquals(TenantGuard.TENANT_NOT_PERMITTED, response.getStatusInfo().getReasonPhrase());
            verify(uniqueValueUtil, never()).validateUniqueValue(any(), any());
        } finally {
            restoreConfig(previousConfig);
        }
    }

    @Test
    void createLicenseModelStoresTenantWhenEnabled(@TempDir Path dir) throws IOException {
        String previousConfig = enableMultitenancy(dir);
        try {
            HttpServletRequest hreq = mock(HttpServletRequest.class);
            when(hreq.getAttribute(TenantContext.ATTRIBUTE)).thenReturn(new TenantContext(List.of("tenant-a")));
            when(asdcItemManager.create(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(versioningManager.create(any(), any(), any())).thenReturn(new Version("version-id"));

            Response response = vendorLicenseModels.createLicenseModel(vlmRequest("tenant-a"), "userId", hreq);

            assertEquals(Status.OK.getStatusCode(), response.getStatus());
            ArgumentCaptor<Item> created = ArgumentCaptor.forClass(Item.class);
            verify(asdcItemManager).create(created.capture());
            assertEquals("tenant-a", created.getValue().getTenant());
        } finally {
            restoreConfig(previousConfig);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void listLicenseModelsFiltersToTheCallersTenantWhenEnabled(@TempDir Path dir) throws IOException {
        String previousConfig = enableMultitenancy(dir);
        try {
            HttpServletRequest hreq = mock(HttpServletRequest.class);
            when(hreq.getAttribute(TenantContext.ATTRIBUTE)).thenReturn(new TenantContext(List.of("tenant-a")));
            when(asdcItemManager.list(any())).thenReturn(List.of(
                vlmItem("no-tenant", null, 1_000), vlmItem("other-tenant", "tenant-b", 2_000), vlmItem("visible", "tenant-a", 3_000)));

            Response response = vendorLicenseModels.listLicenseModels(null, null, "userId", hreq);

            List<ItemDto> results = ((GenericCollectionWrapper<ItemDto>) response.getEntity()).getResults();
            assertEquals(1, results.size());
            assertEquals("visible", results.get(0).getName());
        } finally {
            restoreConfig(previousConfig);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void listLicenseModelsWithMultitenancyDoesNotMatchTenantBySubstring(@TempDir Path dir) throws IOException {
        String previousConfig = enableMultitenancy(dir);
        try {
            HttpServletRequest hreq = mock(HttpServletRequest.class);
            when(hreq.getAttribute(TenantContext.ATTRIBUTE)).thenReturn(new TenantContext(List.of("a")));
            when(asdcItemManager.list(any())).thenReturn(List.of(vlmItem("tnap-item", "tnap", 1_000)));

            Response response = vendorLicenseModels.listLicenseModels(null, null, "userId", hreq);

            List<ItemDto> results = ((GenericCollectionWrapper<ItemDto>) response.getEntity()).getResults();
            assertEquals(0, results.size());
        } finally {
            restoreConfig(previousConfig);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void listLicenseModelsWithMultitenancyReturnsItemOnceWhenSeveralRolesMatchIt(@TempDir Path dir) throws IOException {
        String previousConfig = enableMultitenancy(dir);
        try {
            HttpServletRequest hreq = mock(HttpServletRequest.class);
            when(hreq.getAttribute(TenantContext.ATTRIBUTE)).thenReturn(new TenantContext(List.of("tenant", "tenant-a")));
            when(asdcItemManager.list(any())).thenReturn(List.of(vlmItem("item", "tenant-a", 1_000)));

            Response response = vendorLicenseModels.listLicenseModels(null, null, "userId", hreq);

            List<ItemDto> results = ((GenericCollectionWrapper<ItemDto>) response.getEntity()).getResults();
            assertEquals(1, results.size());
        } finally {
            restoreConfig(previousConfig);
        }
    }

    @Test
    void createLicenseModelDoesNotStoreTenantWhenDisabled() {
        String previousConfig = System.getProperty("configuration.yaml");
        System.clearProperty("configuration.yaml");
        try {
            when(asdcItemManager.create(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(versioningManager.create(any(), any(), any())).thenReturn(new Version("version-id"));

            Response response = vendorLicenseModels.createLicenseModel(vlmRequest("tenant-a"), "userId", null);

            assertEquals(Status.OK.getStatusCode(), response.getStatus());
            ArgumentCaptor<Item> created = ArgumentCaptor.forClass(Item.class);
            verify(asdcItemManager).create(created.capture());
            assertNull(created.getValue().getTenant());
        } finally {
            restoreConfig(previousConfig);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void listLicenseModelsUnfilteredNewestFirstWhenDisabled() {
        String previousConfig = System.getProperty("configuration.yaml");
        System.clearProperty("configuration.yaml");
        try {
            when(asdcItemManager.list(any())).thenReturn(List.of(vlmItem("older", "tenant-a", 1_000), vlmItem("newer", null, 2_000)));

            Response response = vendorLicenseModels.listLicenseModels(null, null, "userId", null);

            List<ItemDto> results = ((GenericCollectionWrapper<ItemDto>) response.getEntity()).getResults();
            assertEquals(2, results.size());
            assertEquals("newer", results.get(0).getName());
            assertEquals("older", results.get(1).getName());
        } finally {
            restoreConfig(previousConfig);
        }
    }

    private static String enableMultitenancy(Path dir) throws IOException {
        String previousConfig = System.getProperty("configuration.yaml");
        Path config = dir.resolve("configuration.yaml");
        Files.write(config, "multitenancy:\n    enabled: true\n    issuer: http://unused.invalid/realms/x\n".getBytes(StandardCharsets.UTF_8));
        System.setProperty("configuration.yaml", config.toString());
        return previousConfig;
    }

    private static void restoreConfig(String previousConfig) {
        if (previousConfig == null) {
            System.clearProperty("configuration.yaml");
        } else {
            System.setProperty("configuration.yaml", previousConfig);
        }
    }

    private static VendorLicenseModelRequestDto vlmRequest(String tenant) {
        VendorLicenseModelRequestDto request = new VendorLicenseModelRequestDto();
        request.setVendorName("vendor-" + tenant);
        request.setDescription("description");
        request.setTenant(tenant);
        return request;
    }

    private static Item vlmItem(String name, String tenant, long modified) {
        Item item = new Item();
        item.setId(name);
        item.setName(name);
        item.setTenant(tenant);
        item.setStatus(ItemStatus.ACTIVE);
        item.setModificationTime(new Date(modified));
        return item;
    }
}
