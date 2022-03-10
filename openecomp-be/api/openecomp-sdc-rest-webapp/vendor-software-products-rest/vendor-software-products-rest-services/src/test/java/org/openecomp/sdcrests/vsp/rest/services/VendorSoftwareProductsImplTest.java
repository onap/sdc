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

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.itempermissions.PermissionsManager;
import org.openecomp.sdc.notification.services.NotificationPropagationManager;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.versioning.AsdcItemManager;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.types.ItemStatus;
import org.openecomp.sdcrests.vsp.rest.CatalogVspClient;

import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ch.qos.logback.classic.util.ContextInitializer.CONFIG_FILE_PROPERTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.openecomp.sdc.common.errors.Messages.DELETE_VSP_ERROR;
import static org.openecomp.sdc.common.errors.Messages.DELETE_VSP_ERROR_USED_BY_VF;

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
    private CatalogVspClient catalogVspClient;

    private VendorSoftwareProductsImpl vendorSoftwareProducts;

    @BeforeEach
    public void setUp() {
        openMocks(this);

        System.setProperty("configuration.yaml", Paths.get(testResourcesPath.toString(), "configuration.yaml").toAbsolutePath().toString());

        vendorSoftwareProducts = new VendorSoftwareProductsImpl(
                itemManager,
                permissionsManager,
                versioningManager,
                vendorSoftwareProductManager,
                activityLogManager,
                notificationPropagationManager,
                uniqueValueUtil,
                catalogVspClient);

        Item item = new Item();
        item.setType("vsp");
        item.setId(vspId);
        when(itemManager.get(
                ArgumentMatchers.eq(vspId))).thenReturn(item);
    }

    @Test
    void deleteNotCertifiedVspOk() {
        Response rsp = vendorSoftwareProducts.deleteVsp(vspId, user);
        assertEquals(HttpStatus.SC_OK, rsp.getStatus());
        assertNull(rsp.getEntity());
    }

    @Test
    void deleteCertifiedVspKo() {
        Item item = new Item();
        item.setType("vsp");
        item.setId(vspId);
        item.addVersionStatus(VersionStatus.Certified);
        when(itemManager.get(
                ArgumentMatchers.eq(vspId))).thenReturn(item);

        Response rsp = vendorSoftwareProducts.deleteVsp(vspId, user);
        assertEquals(HttpStatus.SC_FORBIDDEN, rsp.getStatus());
        assertEquals(rsp.getEntity().getClass(), Exception.class);
        assertEquals(((Exception)rsp.getEntity()).getLocalizedMessage(), DELETE_VSP_ERROR.getErrorMessage());
    }

    @Test
    void deleteCertifiedArchivedVspOk() throws FileNotFoundException {
        String configPath = getConfigPath("configuration.yaml");
        System.setProperty(CONFIG_FILE_PROPERTY, configPath);
        Item item = new Item();
        item.setType("vsp");
        item.setId(vspId);
        item.setStatus(ItemStatus.ARCHIVED);
        item.addVersionStatus(VersionStatus.Certified);
        when(itemManager.get(
                ArgumentMatchers.eq(vspId))).thenReturn(item);
        when(itemManager.list(any())).thenReturn(List.of(item));
        Response rsp = vendorSoftwareProducts.deleteVsp(vspId, user);
        assertEquals(HttpStatus.SC_OK, rsp.getStatus());
        assertNull(rsp.getEntity());
    }

    @Test
    void deleteCertifiedNotArchivedVspKo() {
        Item item = new Item();
        item.setType("vsp");
        item.setId(vspId);
        item.addVersionStatus(VersionStatus.Certified);
        when(itemManager.get(
                ArgumentMatchers.eq(vspId))).thenReturn(item);

        Response rsp = vendorSoftwareProducts.deleteVsp(vspId, user);
        assertEquals(HttpStatus.SC_FORBIDDEN, rsp.getStatus());
        assertEquals(rsp.getEntity().getClass(), Exception.class);
        assertEquals(((Exception)rsp.getEntity()).getLocalizedMessage(), DELETE_VSP_ERROR.getErrorMessage());
    }

    @Test
    void deleteVspUsedInVfKo() throws Exception {
        Item item = new Item();
        item.setType("vsp");
        item.setId(vspId);
        item.addVersionStatus(VersionStatus.Certified);
        when(itemManager.get(
                ArgumentMatchers.eq(vspId))).thenReturn(item);
        when(catalogVspClient.findNameOfVfUsingVsp(
                ArgumentMatchers.eq(vspId), ArgumentMatchers.eq(user))).thenReturn(Optional.of(VF_NAME));

        Response rsp = vendorSoftwareProducts.deleteVsp(vspId, user);
        assertEquals(HttpStatus.SC_FORBIDDEN, rsp.getStatus());
        assertEquals(rsp.getEntity().getClass(), Exception.class);
        assertEquals(((Exception)rsp.getEntity()).getLocalizedMessage(), String.format(DELETE_VSP_ERROR_USED_BY_VF.getErrorMessage(), VF_NAME, VF_NAME));
    }

    @Test
    void deleteVspUsedInVfThrowsExceptionKo() throws Exception {
        Item item = new Item();
        item.setType("vsp");
        item.setId(vspId);
        item.addVersionStatus(VersionStatus.Certified);
        when(itemManager.get(
                ArgumentMatchers.eq(vspId))).thenReturn(item);
        final String vf_name = "Vf_name";
        when(catalogVspClient.findNameOfVfUsingVsp(
                ArgumentMatchers.eq(vspId), ArgumentMatchers.eq(user))).thenThrow(new Exception(SOME_INTERNAL_ERROR));

        Response rsp = vendorSoftwareProducts.deleteVsp(vspId, user);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, rsp.getStatus());
        assertEquals(rsp.getEntity().getClass(), CoreException.class);
        assertEquals(((Exception)rsp.getEntity()).getLocalizedMessage(), String.format("Vsp with id %s cannot be deleted due to error %s.", vspId, SOME_INTERNAL_ERROR));
    }

    @Test
    void deleteCertifiedArchivedVspNotInVfOk() throws Exception {
        String configPath = getConfigPath("configuration.yaml");
        System.setProperty(CONFIG_FILE_PROPERTY, configPath);
        Item item = new Item();
        item.setType("vsp");
        item.setId(vspId);
        item.setStatus(ItemStatus.ARCHIVED);
        item.addVersionStatus(VersionStatus.Certified);
        when(itemManager.get(
                ArgumentMatchers.eq(vspId))).thenReturn(item);
        when(itemManager.list(any())).thenReturn(List.of(item));
        when(catalogVspClient.findNameOfVfUsingVsp(
                ArgumentMatchers.eq(vspId), ArgumentMatchers.eq(user))).thenReturn(Optional.empty());
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
}