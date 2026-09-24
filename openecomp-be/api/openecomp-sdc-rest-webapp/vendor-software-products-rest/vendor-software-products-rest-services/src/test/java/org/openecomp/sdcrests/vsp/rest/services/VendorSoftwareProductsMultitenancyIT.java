/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.openecomp.sdcrests.vsp.rest.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.be.csar.storage.StorageFactory;
import org.openecomp.sdc.be.test.util.KeycloakTestRealm;
import org.openecomp.sdc.common.tenant.TenantContext;
import org.openecomp.sdc.common.tenant.TenantGuard;
import org.openecomp.sdc.itempermissions.PermissionsManager;
import org.openecomp.sdc.notification.services.NotificationPropagationManager;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.versioning.AsdcItemManager;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.types.ItemStatus;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspDetailsDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspRequestDto;
import org.openecomp.sdcrests.vsp.rest.CatalogVspClient;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.mock.web.MockHttpServletRequest;

class VendorSoftwareProductsMultitenancyIT {

    private static KeycloakTestRealm keycloak;
    private static String previousConfig;

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
    @Mock
    private StorageFactory storageFactory;

    @InjectMocks
    private VendorSoftwareProductsImpl vendorSoftwareProducts;

    @BeforeAll
    static void enableMultitenancy(@TempDir Path dir) throws Exception {
        previousConfig = System.getProperty("configuration.yaml");
        keycloak = KeycloakTestRealm.get();
        Path config = dir.resolve("onboarding_configuration.yaml");
        Files.write(config, ("multitenancy:\n  enabled: true\n  issuer: " + keycloak.issuer() + "\n").getBytes(StandardCharsets.UTF_8));
        System.setProperty("configuration.yaml", config.toString());
    }

    @AfterAll
    static void restoreConfig() {
        if (previousConfig == null) {
            System.clearProperty("configuration.yaml");
        } else {
            System.setProperty("configuration.yaml", previousConfig);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        // VendorSoftwareProductsImpl#cachedValidationVsp is a static, class-wide cache: without resetting it,
        // whichever test runs getValidationVsp first decides whether every later test's itemManager.create is invoked.
        resetCachedValidationVsp();
        openMocks(this);
        when(itemManager.create(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setId("vsp-id");
            return item;
        });
        when(versioningManager.create(eq("vsp-id"), any(Version.class), isNull())).thenReturn(new Version("version-id"));
    }

    private static void resetCachedValidationVsp() throws Exception {
        Field field = VendorSoftwareProductsImpl.class.getDeclaredField("cachedValidationVsp");
        field.setAccessible(true);
        field.set(null, null);
    }

    private MockHttpServletRequest requestAs(String username) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(TenantContext.ATTRIBUTE, keycloak.contextFor(username));
        return request;
    }

    private static VspRequestDto vsp(String tenant) {
        VspRequestDto dto = new VspRequestDto();
        dto.setName("vsp-" + tenant);
        dto.setOnboardingMethod("NetworkPackage");
        dto.setTenant(tenant);
        return dto;
    }

    private static Item item(String name, String tenant, long modified) {
        Item item = new Item();
        item.setId(name);
        item.setName(name);
        item.setTenant(tenant);
        item.setStatus(ItemStatus.ACTIVE);
        item.setModificationTime(new Date(modified));
        return item;
    }

    private void verifyNoCreateSideEffects() {
        verify(uniqueValueUtil, never()).validateUniqueValue(any(), any());
        verify(itemManager, never()).create(any(Item.class));
        verify(versioningManager, never()).create(any(), any(), any());
    }

    @Test
    void createsVspForTheCallersTenant() throws Exception {
        Response response = vendorSoftwareProducts.createVsp(vsp("tenant-a"), "cs0008", requestAs("alice"));
        assertEquals(200, response.getStatus());
        ArgumentCaptor<Item> created = ArgumentCaptor.forClass(Item.class);
        verify(itemManager).create(created.capture());
        assertEquals("tenant-a", created.getValue().getTenant());
    }

    @Test
    void refusesVspForAnotherTenant() throws Exception {
        Response response = vendorSoftwareProducts.createVsp(vsp("tenant-a"), "cs0008", requestAs("bob"));
        assertEquals(403, response.getStatus());
        assertEquals(TenantGuard.TENANT_NOT_PERMITTED, response.getStatusInfo().getReasonPhrase());
        verifyNoCreateSideEffects();
    }

    @Test
    void refusesVspWithBlankTenant() throws Exception {
        Response response = vendorSoftwareProducts.createVsp(vsp(""), "cs0008", requestAs("alice"));
        assertEquals(403, response.getStatus());
        assertEquals(TenantGuard.TENANT_NOT_PERMITTED, response.getStatusInfo().getReasonPhrase());
        verifyNoCreateSideEffects();
    }

    @Test
    void refusesVspWithNullTenant() throws Exception {
        Response response = vendorSoftwareProducts.createVsp(vsp(null), "cs0008", requestAs("alice"));
        assertEquals(403, response.getStatus());
        assertEquals(TenantGuard.TENANT_NOT_PERMITTED, response.getStatusInfo().getReasonPhrase());
        verifyNoCreateSideEffects();
    }

    @Test
    @SuppressWarnings("unchecked")
    void listsEachVisibleVspOnceNewestFirst() throws Exception {
        when(itemManager.list(any())).thenReturn(new ArrayList<>(Arrays.asList(
            item("old-a", "tenant-a", 1_000), item("b", "tenant-b", 2_000), item("none", null, 3_000), item("new-a", "tenant-a", 4_000))));
        Response response = vendorSoftwareProducts.listVsps(null, null, "cs0008", requestAs("carol"));
        List<VspDetailsDto> results = ((GenericCollectionWrapper<VspDetailsDto>) response.getEntity()).getResults();
        assertEquals(Arrays.asList("new-a", "b", "old-a"), Arrays.asList(results.stream().map(VspDetailsDto::getName).toArray()));
    }

    @Test
    void getValidationVspSucceedsWithoutTenantContext() {
        Response response = vendorSoftwareProducts.getValidationVsp("cs0008");
        assertEquals(200, response.getStatus());
        verify(itemManager).create(any(Item.class));
    }
}
