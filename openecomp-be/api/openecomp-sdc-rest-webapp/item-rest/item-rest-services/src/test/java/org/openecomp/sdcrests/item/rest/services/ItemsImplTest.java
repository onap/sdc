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

import static org.junit.Assert.assertEquals;
import static org.openecomp.sdcrests.item.types.ItemAction.ARCHIVE;
import static org.openecomp.sdcrests.item.types.ItemAction.RESTORE;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.versioning.ItemManager;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdcrests.item.types.ItemActionRequestDto;

@RunWith(MockitoJUnitRunner.class)
public class ItemsImplTest {

    private static final String ITEM_ID = "ITEM_ID";
    private static final String USER = "USER";

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

    @Test
    public void shouldInitActionSideAffectsMap() {
        ItemsImpl items = new ItemsImpl();
        items.initActionSideAffectsMap();
        assertEquals(items.getActionSideAffectsMap().size(),2);
    }

    @Test
    public void shouldActOnEmptyItem() {
        ItemsImpl items = new ItemsImpl();
        items.initActionSideAffectsMap();
        items.setManagersProvider(managersProvider);
        Mockito.when(managersProvider.getItemManager()).thenReturn(itemManager);
        Mockito.when(itemManager.get(Mockito.any())).thenReturn(null);
        Response response = items.actOn(request, ITEM_ID, USER);
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void shouldActOnARCHIVE() {
        ItemsImpl items = new ItemsImpl();
        items.initActionSideAffectsMap();
        items.setManagersProvider(managersProvider);
        Mockito.when(itemManager.get(Mockito.any())).thenReturn(item);
        Mockito.when(request.getAction()).thenReturn(ARCHIVE);
        Mockito.when(managersProvider.getItemManager()).thenReturn(itemManager);
        Mockito.when(managersProvider.getVersioningManager()).thenReturn(versioningManager);
        Mockito.when(versioningManager.list(Mockito.any())).thenReturn(getVersions());
        Mockito.when(managersProvider.getActivityLogManager()).thenReturn(activityManager);
        items.actOn(request, ITEM_ID, USER);
        Mockito.verify(itemManager).archive(Mockito.any());
    }

    @Test
    public void shouldActOnRESTORE() {
        ItemsImpl items = new ItemsImpl();
        items.initActionSideAffectsMap();
        items.setManagersProvider(managersProvider);
        Mockito.when(itemManager.get(Mockito.any())).thenReturn(item);
        Mockito.when(request.getAction()).thenReturn(RESTORE);
        Mockito.when(managersProvider.getItemManager()).thenReturn(itemManager);
        Mockito.when(managersProvider.getVersioningManager()).thenReturn(versioningManager);
        Mockito.when(versioningManager.list(Mockito.any())).thenReturn(getVersions());
        Mockito.when(managersProvider.getActivityLogManager()).thenReturn(activityManager);
        items.actOn(request, ITEM_ID, USER);
        Mockito.verify(itemManager).restore(Mockito.any());
    }

    @Test
    public void shouldGetItem() {
        ItemsImpl items = new ItemsImpl();
        items.initActionSideAffectsMap();
        items.setManagersProvider(managersProvider);
        Mockito.when(managersProvider.getItemManager()).thenReturn(itemManager);
        Response response = items.getItem(ITEM_ID, USER);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void shouldList() {
        ItemsImpl items = new ItemsImpl();
        items.initActionSideAffectsMap();
        items.setManagersProvider(managersProvider);
        Mockito.when(managersProvider.getItemManager()).thenReturn(itemManager);
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