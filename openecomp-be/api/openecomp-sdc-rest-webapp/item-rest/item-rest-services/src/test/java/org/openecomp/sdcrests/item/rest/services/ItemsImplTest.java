/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
package org.openecomp.sdcrests.item.rest.services;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.csar.storage.StorageFactory.StorageType.MINIO;
import static org.openecomp.sdc.be.csar.storage.StorageFactory.StorageType.NONE;
import static org.openecomp.sdcrests.item.types.ItemAction.ARCHIVE;
import static org.openecomp.sdcrests.item.types.ItemAction.RESTORE;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.common.CommonConfigurationManager;
import org.openecomp.sdc.common.errors.ErrorCodeAndMessage;
import org.openecomp.sdc.datatypes.model.ItemType;
import org.openecomp.sdc.versioning.ItemManager;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdcrests.item.types.ItemActionRequestDto;

@ExtendWith(MockitoExtension.class)
class ItemsImplTest {

    private static final String ITEM_ID = "item-id";
    private static final String USER = "USER";
    private static final String EXTERNAL_CSAR_STORE = "externalCsarStore";
    private static final String STORAGE_TYPE = "storageType";
    private static final String ENDPOINT = "endpoint";
    private static final String CREDENTIALS = "credentials";
    private static final String TEMP_PATH = "tempPath";
    private static final String UPLOAD_PARTSIZE = "uploadPartSize";

    @Mock
    private ManagersProvider managersProvider;
    @Mock
    private ItemManager itemManager;
    @Mock
    private ItemActionRequestDto request;
    @Mock
    private Item item;
    @Mock
    private VersioningManager versioningManager;
    @Mock
    private ActivityLogManager activityManager;
    @Mock
    private CommonConfigurationManager commonConfigurationManager;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MinioClient.Builder builderMinio;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private BucketExistsArgs.Builder builderBucketExistsArgs;

    @InjectMocks
    private ItemsImpl items;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldInitActionSideAffectsMap() {
        items.initActionSideAffectsMap();
        assertEquals(2, items.getActionSideAffectsMap().size());
    }

    @Test
    void shouldActOnEmptyItem() {
        items.initActionSideAffectsMap();
        items.setManagersProvider(managersProvider);
        when(managersProvider.getItemManager()).thenReturn(itemManager);
        when(itemManager.get(any())).thenReturn(null);
        Response response = items.actOn(request, ITEM_ID, USER);
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void shouldActOnARCHIVE() {
        items.initActionSideAffectsMap();
        items.setManagersProvider(managersProvider);
        when(itemManager.get(any())).thenReturn(item);
        when(request.getAction()).thenReturn(ARCHIVE);
        when(managersProvider.getItemManager()).thenReturn(itemManager);
        when(managersProvider.getVersioningManager()).thenReturn(versioningManager);
        when(versioningManager.list(any())).thenReturn(getVersions());
        when(managersProvider.getActivityLogManager()).thenReturn(activityManager);
        items.actOn(request, ITEM_ID, USER);
        verify(itemManager).archive(any());
    }

    @Test
    void shouldActOnRESTORE() {
        items.initActionSideAffectsMap();
        items.setManagersProvider(managersProvider);
        when(itemManager.get(any())).thenReturn(item);
        when(request.getAction()).thenReturn(RESTORE);
        when(managersProvider.getItemManager()).thenReturn(itemManager);
        when(managersProvider.getVersioningManager()).thenReturn(versioningManager);
        when(versioningManager.list(any())).thenReturn(getVersions());
        when(managersProvider.getActivityLogManager()).thenReturn(activityManager);
        items.actOn(request, ITEM_ID, USER);
        verify(itemManager).restore(any());
    }

    @Test
    void shouldActOnRESTORE_with_S3() throws Exception {
        items.initActionSideAffectsMap();
        items.setManagersProvider(managersProvider);
        when(itemManager.get(any())).thenReturn(item);
        when(item.getType()).thenReturn(ItemType.vsp.getName());
        when(request.getAction()).thenReturn(RESTORE);
        when(managersProvider.getItemManager()).thenReturn(itemManager);
        try (MockedStatic<CommonConfigurationManager> utilities = Mockito.mockStatic(CommonConfigurationManager.class)) {
            utilities.when(CommonConfigurationManager::getInstance).thenReturn(commonConfigurationManager);
            try (MockedStatic<MinioClient> minioUtilities = Mockito.mockStatic(MinioClient.class)) {
                minioUtilities.when(MinioClient::builder).thenReturn(builderMinio);
                when(commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, STORAGE_TYPE, NONE.name())).thenReturn(MINIO.name());
                when(commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, ENDPOINT, null)).thenReturn(new HashMap<String, Object>());
                when(commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, CREDENTIALS, null)).thenReturn(new HashMap<String, Object>());
                when(commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, TEMP_PATH, null)).thenReturn("");
                when(commonConfigurationManager.getConfigValue(eq(EXTERNAL_CSAR_STORE), eq(UPLOAD_PARTSIZE), anyInt())).thenReturn(0);
                when(builderBucketExistsArgs
                    .bucket(anyString())
                    .build()
                ).thenReturn(new BucketExistsArgs());

                final var response = items.actOn(request, ITEM_ID, USER);
                assertNotNull(response);
                assertEquals(INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
                assertNotNull(response.getEntity());
                assertTrue(response.getEntity() instanceof ErrorCodeAndMessage);
                assertEquals(INTERNAL_SERVER_ERROR.getStatusCode(), ((ErrorCodeAndMessage) response.getEntity()).getStatus().getStatusCode());
                assertEquals(INTERNAL_SERVER_ERROR.name(), ((ErrorCodeAndMessage) response.getEntity()).getErrorCode());
            }
        }
    }

    @Test
    void shouldGetItem() {
        items.initActionSideAffectsMap();
        items.setManagersProvider(managersProvider);
        when(managersProvider.getItemManager()).thenReturn(itemManager);
        Response response = items.getItem(ITEM_ID, USER);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    void shouldList() {
        items.initActionSideAffectsMap();
        items.setManagersProvider(managersProvider);
        when(managersProvider.getItemManager()).thenReturn(itemManager);
        Response response = items.list(null, null, null, null, null, USER);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    private List<Version> getVersions() {
        List<Version> versions = new ArrayList<>();
        versions.add(new Version("1"));
        versions.add(new Version("2"));
        versions.add(new Version("3"));
        return versions;
    }
}
