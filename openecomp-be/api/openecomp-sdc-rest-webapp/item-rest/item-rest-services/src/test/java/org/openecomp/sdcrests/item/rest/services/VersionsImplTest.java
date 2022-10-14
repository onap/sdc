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
import static org.openecomp.sdcrests.item.types.VersionAction.Clean;
import static org.openecomp.sdcrests.item.types.VersionAction.Commit;
import static org.openecomp.sdcrests.item.types.VersionAction.Reset;
import static org.openecomp.sdcrests.item.types.VersionAction.Revert;
import static org.openecomp.sdcrests.item.types.VersionAction.Sync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.conflicts.ConflictsManager;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.itempermissions.PermissionsManager;
import org.openecomp.sdc.notification.services.NotificationPropagationManager;
import org.openecomp.sdc.versioning.AsdcItemManager;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Revision;
import org.openecomp.sdc.versioning.dao.types.SynchronizationState;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionState;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdcrests.item.types.RevisionRequestDto;
import org.openecomp.sdcrests.item.types.VersionActionRequestDto;

@RunWith(MockitoJUnitRunner.class)
public class VersionsImplTest {

    private static final String ITEM_ID = "ITEM_ID";
    private static final String VERSION_ID = "VERSION_ID";
    private static final String USER = "USER";
    private static final String REVISION_ID = "REVISION_ID";

    @Mock
    private ManagersProvider managersProvider;
    @Mock
    private VersionActionRequestDto request;
    @Mock
    private RevisionRequestDto revisionRequest;
    @Mock
    private VersioningManager versioningManager;
    @Mock
    private PermissionsManager permManager;
    @Mock
    private ActivityLogManager activityManager;
    @Mock
    private AsdcItemManager asdcManager;
    @Mock
    private Item item;
    @Mock
    private Version version;
    @Mock
    private NotificationPropagationManager notificationManager;
    @Mock
    private ConflictsManager conflictsManager;
    @Mock
    private VersionState state;

    @Test
    public void shouldActOnSync() {
        VersionsImpl versions = new VersionsImpl();
        versions.setManagersProvider(managersProvider);
        Mockito.when(request.getAction()).thenReturn(Sync);
        Mockito.when(managersProvider.getVersioningManager()).thenReturn(versioningManager);
        Mockito.when(managersProvider.getConflictsManager()).thenReturn(conflictsManager);
        versions.actOn(request, ITEM_ID, VERSION_ID, USER);
        Mockito.verify(versioningManager).sync(Mockito.any(), Mockito.any());
        Mockito.verify(conflictsManager).finalizeMerge(Mockito.any(), Mockito.any());
    }

    @Test
    public void shouldActOnCommitWhenAllowed() {
        VersionsImpl versions = new VersionsImpl();
        versions.setManagersProvider(managersProvider);
        Mockito.when(request.getAction()).thenReturn(Commit);
        Mockito.when(managersProvider.getPermissionsManager()).thenReturn(permManager);
        Mockito.when(permManager.isAllowed(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(managersProvider.getVersioningManager()).thenReturn(versioningManager);
        Mockito.when(managersProvider.getActivityLogManager()).thenReturn(activityManager);
        Mockito.when(managersProvider.getAsdcItemManager()).thenReturn(asdcManager);
        Mockito.when(asdcManager.get(Mockito.any())).thenReturn(item);
        Mockito.when(versioningManager.get(Mockito.any(), Mockito.any())).thenReturn(version);
        Mockito.when(managersProvider.getNotificationPropagationManager()).thenReturn(notificationManager);
        versions.actOn(request, ITEM_ID, VERSION_ID, USER);
        Mockito.verify(versioningManager).publish(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(notificationManager).notifySubscribers(Mockito.any(), Mockito.any());
        Mockito.verify(activityManager).logActivity(Mockito.any());
    }

    @Test
    public void shouldActOnCommitWhenNotAllowed() {
        VersionsImpl versions = new VersionsImpl();
        versions.setManagersProvider(managersProvider);
        Mockito.when(request.getAction()).thenReturn(Commit);
        Mockito.when(managersProvider.getPermissionsManager()).thenReturn(permManager);
        Mockito.when(permManager.isAllowed(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);

        Response response = versions.actOn(request, ITEM_ID, VERSION_ID, USER);
        assertEquals(response.getStatus(), Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void shouldActOnClean() {
        VersionsImpl versions = new VersionsImpl();
        versions.setManagersProvider(managersProvider);
        Mockito.when(request.getAction()).thenReturn(Clean);
        Mockito.when(managersProvider.getVersioningManager()).thenReturn(versioningManager);
        versions.actOn(request, ITEM_ID, VERSION_ID, USER);
        Mockito.verify(versioningManager).clean(Mockito.any(), Mockito.any());
    }

    @Test
    public void shouldActOnRevert() {
        VersionsImpl versions = new VersionsImpl();
        versions.setManagersProvider(managersProvider);
        Mockito.when(request.getAction()).thenReturn(Revert);
        Mockito.when(request.getRevisionRequest()).thenReturn(revisionRequest);
        Mockito.when(revisionRequest.getRevisionId()).thenReturn(REVISION_ID);
        Mockito.when(managersProvider.getVersioningManager()).thenReturn(versioningManager);
        versions.actOn(request, ITEM_ID, VERSION_ID, USER);
        Mockito.verify(versioningManager).revert(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test(expected = CoreException.class)
    public void shouldActOnRevertAndEmptyRevisionId() {
        VersionsImpl versions = new VersionsImpl();
        versions.setManagersProvider(managersProvider);
        Mockito.when(request.getAction()).thenReturn(Revert);
        Mockito.when(request.getRevisionRequest()).thenReturn(revisionRequest);
        Mockito.when(revisionRequest.getRevisionId()).thenReturn(null);
        versions.actOn(request, ITEM_ID, VERSION_ID, USER);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldActOnOther() {
        VersionsImpl versions = new VersionsImpl();
        versions.setManagersProvider(managersProvider);
        Mockito.when(request.getAction()).thenReturn(Reset);
        versions.actOn(request, ITEM_ID, VERSION_ID, USER);
    }

    @Test
    public void shouldListRevision() {
        VersionsImpl versions = new VersionsImpl();
        versions.setManagersProvider(managersProvider);
        Mockito.when(managersProvider.getVersioningManager()).thenReturn(versioningManager);
        List<Revision> revisions = getRevisions();
        Mockito.when(versioningManager.listRevisions(Mockito.any(), Mockito.any())).thenReturn(revisions);
        Response response = versions.listRevisions(ITEM_ID, VERSION_ID, USER);
        Mockito.verify(versioningManager).listRevisions(Mockito.any(), Mockito.any());
        assertEquals(response.getStatus(), Status.OK.getStatusCode());
    }

    @Test
    public void shouldGetActivityLog() {
        VersionsImpl versions = new VersionsImpl();
        versions.setManagersProvider(managersProvider);
        Mockito.when(managersProvider.getActivityLogManager()).thenReturn(activityManager);
        Mockito.when(activityManager.listLoggedActivities(Mockito.any(), Mockito.any())).thenReturn(Collections.emptyList());
        Response activityLog = versions.getActivityLog(ITEM_ID, VERSION_ID, USER);
        assertEquals(activityLog.getStatus(), Status.OK.getStatusCode());
    }

    @Test
    public void shouldGet() {
        VersionsImpl versions = new VersionsImpl();
        versions.setManagersProvider(managersProvider);
        Mockito.when(managersProvider.getVersioningManager()).thenReturn(versioningManager);
        Mockito.when(versioningManager.get(Mockito.any(), Mockito.any())).thenReturn(version);
        Mockito.when(version.getState()).thenReturn(state);
        Mockito.when(state.getSynchronizationState()).thenReturn(SynchronizationState.Merging);
        Response response = versions.get(ITEM_ID, VERSION_ID, USER);
        assertEquals(response.getStatus(), Status.OK.getStatusCode());
    }

    private List<Revision> getRevisions() {
        List<Revision> revisions = new ArrayList<>();
        Revision revision = new Revision();
        revision.setMessage("Initial TEST:TEST");
        revisions.add(revision);
        revisions.add(new Revision());
        return revisions;
    }
}
