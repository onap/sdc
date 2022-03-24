/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 *
 *
 */

package org.openecomp.sdcrests.vsp.rest.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.openecomp.sdc.common.errors.Messages.DELETE_NOT_ARCHIVED_VSP_ERROR;
import static org.openecomp.sdc.common.errors.Messages.DELETE_VSP_FROM_STORAGE_ERROR;

import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.activitylog.dao.type.ActivityType;
import org.openecomp.sdc.be.csar.storage.ArtifactStorageManager;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.datatypes.model.ItemType;
import org.openecomp.sdc.itempermissions.PermissionsManager;
import org.openecomp.sdc.notification.services.NotificationPropagationManager;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.versioning.AsdcItemManager;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.types.ItemStatus;
import org.openecomp.sdcrests.vsp.rest.exception.VendorSoftwareProductsExceptionSupplier;

class VendorSoftwareProductsImplTest {

    private final String vspId = UUID.randomUUID().toString();
    private final String user = "cs0008";

    @Mock
    private AsdcItemManager itemManager;
    @Mock
    private PermissionsManager permissionsManager;
    @Mock
    private VersioningManager versioningManager;
    @Mock
    private VendorSoftwareProductManager vendorSoftwareProductManager;
    @Mock
    private ActivityLogManager activityLogManager;
    @Mock
    private NotificationPropagationManager notificationPropagationManager;
    @Mock
    private UniqueValueUtil uniqueValueUtil;
    @Mock
    private ArtifactStorageManager artifactStorageManager;

    @InjectMocks
    private VendorSoftwareProductsImpl vendorSoftwareProducts;

    private Item item;

    @BeforeEach
    public void setUp() {
        openMocks(this);

        item = new Item();
        item.setType(ItemType.vsp.getName());
        item.setId(vspId);
        when(itemManager.get(vspId)).thenReturn(item);
    }

    @Test
    void deleteVspOk() {
        when(itemManager.list(any())).thenReturn(List.of(item));
        Response actualResponse = vendorSoftwareProducts.deleteVsp(vspId, user);
        assertEquals(HttpStatus.SC_OK, actualResponse.getStatus());
        assertNull(actualResponse.getEntity());
    }

    @Test
    void deleteVspWithS3Ok() {
        when(artifactStorageManager.isEnabled()).thenReturn(true);
        Response rsp = vendorSoftwareProducts.deleteVsp(vspId, user);
        assertEquals(HttpStatus.SC_OK, rsp.getStatus());
        assertNull(rsp.getEntity());
    }

    @Test
    void deleteVspWithS3Fail() {
        when(artifactStorageManager.isEnabled()).thenReturn(true);
        doThrow(new RuntimeException()).when(artifactStorageManager).delete(anyString());
        Response rsp = vendorSoftwareProducts.deleteVsp(vspId, user);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, rsp.getStatus());
        assertEquals(rsp.getEntity().getClass(), Exception.class);
        assertEquals(((Exception) rsp.getEntity()).getLocalizedMessage(), DELETE_VSP_FROM_STORAGE_ERROR.formatMessage(vspId));
    }

    @Test
    void deleteCertifiedVsp() {
        item.addVersionStatus(VersionStatus.Certified);
        when(itemManager.get(vspId)).thenReturn(item);

        Response rsp = vendorSoftwareProducts.deleteVsp(vspId, user);
        assertEquals(HttpStatus.SC_FORBIDDEN, rsp.getStatus());
        assertEquals(rsp.getEntity().getClass(), Exception.class);
        assertEquals(((Exception) rsp.getEntity()).getLocalizedMessage(), DELETE_NOT_ARCHIVED_VSP_ERROR.formatMessage(vspId));
    }

    @Test
    void deleteCertifiedArchivedVsp() {
        item.setStatus(ItemStatus.ARCHIVED);
        item.addVersionStatus(VersionStatus.Certified);
        when(itemManager.get(vspId)).thenReturn(item);
        when(itemManager.list(any())).thenReturn(List.of(item));
        Response rsp = vendorSoftwareProducts.deleteVsp(vspId, user);
        assertEquals(HttpStatus.SC_OK, rsp.getStatus());
        assertNull(rsp.getEntity());
    }

    @Test
    void deleteCertifiedAndNotArchivedVsp() {
        item.setStatus(ItemStatus.ACTIVE);
        item.addVersionStatus(VersionStatus.Certified);
        when(itemManager.get(vspId)).thenReturn(item);

        final Response actualResponse = vendorSoftwareProducts.deleteVsp(vspId, user);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), actualResponse.getStatus());
        assertNotNull(actualResponse.getEntity());
        assertEquals(((Exception)actualResponse.getEntity()).getLocalizedMessage(), DELETE_NOT_ARCHIVED_VSP_ERROR.formatMessage(vspId));
    }

    @Test
    void deleteVspNotFoundTest() {
        when(itemManager.get(vspId)).thenReturn(new Item());
        final CoreException actualException = assertThrows(CoreException.class, () -> vendorSoftwareProducts.deleteVsp(vspId, user));
        final CoreException expectedException = VendorSoftwareProductsExceptionSupplier.vspNotFound(vspId).get();
        assertEquals(expectedException.code().id(), actualException.code().id());
        assertEquals(expectedException.code().message(), actualException.code().message());
    }

    @Test
    void deleteCertifiedArchivedVspWithS3OK() {
        when(artifactStorageManager.isEnabled()).thenReturn(true);
        item.setStatus(ItemStatus.ARCHIVED);
        item.addVersionStatus(VersionStatus.Certified);
        when(itemManager.get(vspId)).thenReturn(item);

        final Version version1 = new Version("version1Id");
        final Version version2 = new Version("version2Id");
        final List<Version> versionList = List.of(version1, version2);
        when(versioningManager.list(vspId)).thenReturn(versionList);

        when(itemManager.list(any())).thenReturn(List.of(item));
        Response rsp = vendorSoftwareProducts.deleteVsp(vspId, user);
        assertEquals(HttpStatus.SC_OK, rsp.getStatus());
        assertNull(rsp.getEntity());

        final ArgumentCaptor<ActivityLogEntity> logActivityArgument = ArgumentCaptor.forClass(ActivityLogEntity.class);
        verify(activityLogManager, times(2)).logActivity(logActivityArgument.capture());
        final List<ActivityLogEntity> logActivityArgumentList = logActivityArgument.getAllValues();
        assertEquals(versionList.size(), logActivityArgumentList.size());
        for (int i = 0; i < versionList.size(); i++) {
            final Version expectedVersion = versionList.get(i);
            final ActivityLogEntity actualLogActivityArgument = logActivityArgumentList.get(i);
            assertTrue(actualLogActivityArgument.isSuccess());
            assertEquals(vspId, actualLogActivityArgument.getItemId());
            assertEquals(expectedVersion.getId(), actualLogActivityArgument.getVersionId());
            assertEquals(user, actualLogActivityArgument.getUser());
            assertEquals(ActivityType.DELETE_FROM_STORAGE, actualLogActivityArgument.getType());
        }
    }

    @Test
    void deleteCertifiedArchivedVspWithS3Fail() {
        //given
        when(artifactStorageManager.isEnabled()).thenReturn(true);
        doThrow(new RuntimeException()).when(artifactStorageManager).delete(anyString());
        item.setStatus(ItemStatus.ARCHIVED);
        item.addVersionStatus(VersionStatus.Certified);
        when(itemManager.get(vspId)).thenReturn(item);
        when(itemManager.list(any())).thenReturn(List.of(item));

        final Version version1 = new Version("version1Id");
        final Version version2 = new Version("version2Id");
        final List<Version> versionList = List.of(version1, version2);
        when(versioningManager.list(vspId)).thenReturn(versionList);

        //when
        Response rsp = vendorSoftwareProducts.deleteVsp(vspId, user);

        //then
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, rsp.getStatus());
        assertEquals(rsp.getEntity().getClass(), Exception.class);
        assertEquals(((Exception) rsp.getEntity()).getLocalizedMessage(), DELETE_VSP_FROM_STORAGE_ERROR.formatMessage(vspId));

        final ArgumentCaptor<ActivityLogEntity> logActivityArgument = ArgumentCaptor.forClass(ActivityLogEntity.class);
        verify(activityLogManager, times(2)).logActivity(logActivityArgument.capture());
        final List<ActivityLogEntity> logActivityArgumentList = logActivityArgument.getAllValues();
        assertEquals(versionList.size(), logActivityArgumentList.size());
        for (int i = 0; i < versionList.size(); i++) {
            final Version expectedVersion = versionList.get(i);
            final ActivityLogEntity actualLogActivityArgument = logActivityArgumentList.get(i);
            assertFalse(actualLogActivityArgument.isSuccess());
            assertEquals(vspId, actualLogActivityArgument.getItemId());
            assertEquals(expectedVersion.getId(), actualLogActivityArgument.getVersionId());
            assertEquals(user, actualLogActivityArgument.getUser());
            assertEquals(ActivityType.DELETE_FROM_STORAGE, actualLogActivityArgument.getType());
            assertEquals(DELETE_VSP_FROM_STORAGE_ERROR.formatMessage(vspId), actualLogActivityArgument.getMessage());
            assertEquals(DELETE_VSP_FROM_STORAGE_ERROR.formatMessage(vspId), actualLogActivityArgument.getComment());
        }
    }

}
