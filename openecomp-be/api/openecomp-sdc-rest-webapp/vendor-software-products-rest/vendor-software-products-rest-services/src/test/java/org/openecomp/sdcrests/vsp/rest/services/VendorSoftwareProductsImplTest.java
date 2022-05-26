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

import static ch.qos.logback.classic.util.ContextInitializer.CONFIG_FILE_PROPERTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import static org.openecomp.sdc.common.errors.Messages.DELETE_VSP_FROM_STORAGE_ERROR;

import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
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
import org.openecomp.sdc.be.csar.storage.StorageFactory;
import org.openecomp.sdc.common.errors.CatalogRestClientException;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.model.ItemType;
import org.openecomp.sdc.itempermissions.PermissionsManager;
import org.openecomp.sdc.notification.services.NotificationPropagationManager;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.versioning.AsdcItemManager;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.types.ItemStatus;
import org.openecomp.sdcrests.vsp.rest.CatalogVspClient;
import org.openecomp.sdcrests.vsp.rest.exception.VendorSoftwareProductsExceptionSupplier;

class VendorSoftwareProductsImplTest {

    public static final String SOME_INTERNAL_ERROR = "Some internal error";
    public static final String VF_NAME = "Vf_name";
    private final String vspId = UUID.randomUUID().toString();
    private final String user = "cs0008";

    private final Path testResourcesPath = Paths.get("src", "test", "resources");

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
    @Mock
    private CatalogVspClient catalogVspClient;
    @Mock
    private StorageFactory storageFactory;

    @InjectMocks
    private VendorSoftwareProductsImpl vendorSoftwareProducts;

    private Item item;

    @BeforeEach
    public void setUp() {
        openMocks(this);

        System.setProperty("configuration.yaml", Paths.get(testResourcesPath.toString(), "configuration.yaml").toAbsolutePath().toString());

        item = new Item();
        item.setType(ItemType.vsp.getName());
        item.setId(vspId);
        when(itemManager.get(vspId)).thenReturn(item);
        when(storageFactory.createArtifactStorageManager()).thenReturn(artifactStorageManager);
    }

    @Test
    void deleteNotCertifiedVspOk() {
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
        final CoreException actualException = assertThrows(CoreException.class, () -> vendorSoftwareProducts.deleteVsp(vspId, user));
        final CoreException expectedException = VendorSoftwareProductsExceptionSupplier.deleteVspFromStorageFailure(vspId).get();
        assertErrorCode(actualException.code(), expectedException.code());
    }

    @Test
    void deleteCertifiedVsp() {
        item.addVersionStatus(VersionStatus.Certified);
        when(itemManager.get(vspId)).thenReturn(item);

        final CoreException actualException = assertThrows(CoreException.class, () -> vendorSoftwareProducts.deleteVsp(vspId, user));
        final CoreException expectedException = VendorSoftwareProductsExceptionSupplier.deleteNotArchivedVsp(vspId).get();
        assertErrorCode(actualException.code(), expectedException.code());
    }

    @Test
    void deleteCertifiedArchivedVsp() throws FileNotFoundException {
        String configPath = getConfigPath("configuration.yaml");
        System.setProperty(CONFIG_FILE_PROPERTY, configPath);
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

        final CoreException actualException = assertThrows(CoreException.class, () -> vendorSoftwareProducts.deleteVsp(vspId, user));
        final CoreException expectedException = VendorSoftwareProductsExceptionSupplier.deleteNotArchivedVsp(vspId).get();
        assertErrorCode(actualException.code(), expectedException.code());
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
        VspDetails vspDetails = new VspDetails();
        vspDetails.setCategory("cat");
        vspDetails.setSubCategory("sub");
        when(vendorSoftwareProductManager.getVsp(vspId, version1)).thenReturn(vspDetails);
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
            assertEquals(ActivityType.Delete_From_Storage, actualLogActivityArgument.getType());
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
        final CoreException actualException = assertThrows(CoreException.class, () -> vendorSoftwareProducts.deleteVsp(vspId, user));

        //then
        final CoreException expectedException = VendorSoftwareProductsExceptionSupplier.deleteVspFromStorageFailure(vspId).get();
        assertErrorCode(actualException.code(), expectedException.code());

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
            assertEquals(ActivityType.Delete_From_Storage, actualLogActivityArgument.getType());
            assertEquals(DELETE_VSP_FROM_STORAGE_ERROR.formatMessage(vspId), actualLogActivityArgument.getMessage());
            assertEquals(DELETE_VSP_FROM_STORAGE_ERROR.formatMessage(vspId), actualLogActivityArgument.getComment());
        }
    }

    @Test
    void deleteVspUsedInVfKo() {
        item.addVersionStatus(VersionStatus.Certified);
        when(itemManager.get(vspId)).thenReturn(item);
        when(catalogVspClient.findNameOfVfUsingVsp(vspId, user)).thenReturn(Optional.of(VF_NAME));

        final CoreException actualException = assertThrows(CoreException.class, () -> vendorSoftwareProducts.deleteVsp(vspId, user));
        final CoreException expectedException = VendorSoftwareProductsExceptionSupplier.vspInUseByVf(VF_NAME).get();
        assertErrorCode(actualException.code(), expectedException.code());
    }

    @Test
    void deleteVspUsedInVfThrowsExceptionKo() {
        item.addVersionStatus(VersionStatus.Certified);
        when(itemManager.get(vspId)).thenReturn(item);
        when(catalogVspClient.findNameOfVfUsingVsp(vspId, user)).thenThrow(new CatalogRestClientException(SOME_INTERNAL_ERROR));

        final CoreException actualException = assertThrows(CoreException.class, () -> vendorSoftwareProducts.deleteVsp(vspId, user));
        final CoreException expectedException = VendorSoftwareProductsExceptionSupplier.deleteGenericError(vspId).get();
        assertErrorCode(actualException.code(), expectedException.code());
    }

    @Test
    void deleteCertifiedArchivedVspNotInVfOk() throws FileNotFoundException {
        String configPath = getConfigPath("configuration.yaml");
        System.setProperty(CONFIG_FILE_PROPERTY, configPath);
        Item item = new Item();
        item.setType("vsp");
        item.setId(vspId);
        item.setStatus(ItemStatus.ARCHIVED);
        item.addVersionStatus(VersionStatus.Certified);
        when(itemManager.get(vspId)).thenReturn(item);
        when(itemManager.list(any())).thenReturn(List.of(item));
        when(catalogVspClient.findNameOfVfUsingVsp(vspId, user)).thenReturn(Optional.empty());
        Response rsp = vendorSoftwareProducts.deleteVsp(vspId, user);
        assertEquals(HttpStatus.SC_OK, rsp.getStatus());
        assertNull(rsp.getEntity());
    }

    private String getConfigPath(String classpathFile) throws FileNotFoundException {

        URL resource = Thread.currentThread().getContextClassLoader().getResource(classpathFile);
        if (resource == null) {
            throw new FileNotFoundException("Cannot find resource: " + classpathFile);
        }
        return resource.getPath();
    }

    private void assertErrorCode(final ErrorCode actualErrorCode, final ErrorCode expectedErrorCode) {
        assertEquals(expectedErrorCode.id(), actualErrorCode.id());
        assertEquals(expectedErrorCode.category(), actualErrorCode.category());
        assertEquals(expectedErrorCode.message(), actualErrorCode.message());
    }

}
