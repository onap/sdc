/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.sdcrests.item.rest.services;

import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.PERMISSION_USER;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.ITEM_ID;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.ITEM_NAME;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.SUBMIT_DESCRIPTION;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.VERSION_ID;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.VERSION_NAME;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.activitylog.dao.type.ActivityType;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.notification.dtos.Event;
import org.openecomp.sdc.versioning.dao.types.Revision;
import org.openecomp.sdc.versioning.dao.types.SynchronizationState;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.RevisionIdNotFoundErrorBuilder;
import org.openecomp.sdc.versioning.types.NotificationEventTypes;
import org.openecomp.sdc.versioning.types.VersionCreationMethod;
import org.openecomp.sdcrests.item.rest.Versions;
import org.openecomp.sdcrests.item.rest.mapping.MapActivityLogEntityToDto;
import org.openecomp.sdcrests.item.rest.mapping.MapRevisionToDto;
import org.openecomp.sdcrests.item.rest.mapping.MapVersionToDto;
import org.openecomp.sdcrests.item.rest.models.SyncEvent;
import org.openecomp.sdcrests.item.types.ActivityLogDto;
import org.openecomp.sdcrests.item.types.CommitRequestDto;
import org.openecomp.sdcrests.item.types.RevisionDto;
import org.openecomp.sdcrests.item.types.RevisionRequestDto;
import org.openecomp.sdcrests.item.types.VersionActionRequestDto;
import org.openecomp.sdcrests.item.types.VersionDto;
import org.openecomp.sdcrests.item.types.VersionRequestDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Named
@Service("versions")
@Scope(value = "prototype")
public class VersionsImpl implements Versions {

    private static final String COMMIT_ITEM_ACTION = "Commit_Item";
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionsImpl.class);
    private ManagersProvider managersProvider;

    @Override
    public Response list(String itemId, String user) {
        GenericCollectionWrapper<VersionDto> results = new GenericCollectionWrapper<>();
        MapVersionToDto mapper = new MapVersionToDto();
        getManagersProvider().getVersioningManager().list(itemId).forEach(version -> results.add(mapper.applyMapping(version, VersionDto.class)));
        return Response.ok(results).build();
    }

    @Override
    public Response create(VersionRequestDto request, String itemId, String baseVersionId, String user) {
        Version version = new Version();
        version.setBaseId(baseVersionId);
        version.setDescription(request.getDescription());
        version = getManagersProvider().getVersioningManager()
            .create(itemId, version, VersionCreationMethod.valueOf(request.getCreationMethod().name()));
        VersionDto versionDto = new MapVersionToDto().applyMapping(version, VersionDto.class);
        getManagersProvider().getActivityLogManager()
            .logActivity(new ActivityLogEntity(itemId, version, ActivityType.Create_Version, user, true, "", ""));
        return Response.ok(versionDto).build();
    }

    @Override
    public Response get(String itemId, String versionId, String user) {
        Version version = getVersion(itemId, new Version(versionId));
        VersionDto versionDto = new MapVersionToDto().applyMapping(version, VersionDto.class);
        return Response.ok(versionDto).build();
    }

    @Override
    public Response getActivityLog(String itemId, String versionId, String user) {
        GenericCollectionWrapper<ActivityLogDto> results = new GenericCollectionWrapper<>();
        MapActivityLogEntityToDto mapper = new MapActivityLogEntityToDto();
        getManagersProvider().getActivityLogManager().listLoggedActivities(itemId, new Version(versionId))
            .forEach(loggedActivity -> results.add(mapper.applyMapping(loggedActivity, ActivityLogDto.class)));
        return Response.ok(results).build();
    }

    @Override
    public Response listRevisions(String itemId, String versionId, String user) {
        List<Revision> revisions = getManagersProvider().getVersioningManager().listRevisions(itemId, new Version(versionId));
        filterRevisions(revisions);
        GenericCollectionWrapper<RevisionDto> results = new GenericCollectionWrapper<>();
        MapRevisionToDto mapper = new MapRevisionToDto();
        revisions.forEach(revision -> results.add(mapper.applyMapping(revision, RevisionDto.class)));
        return Response.ok(results).build();
    }

    @Override
    public Response actOn(VersionActionRequestDto request, String itemId, String versionId, String user) {
        Version version = new Version(versionId);
        switch (request.getAction()) {
            case Sync:
                sync(itemId, version);
                break;
            case Commit:
                if (!getManagersProvider().getPermissionsManager().isAllowed(itemId, user, COMMIT_ITEM_ACTION)) {
                    return Response.status(Response.Status.FORBIDDEN).entity(new Exception(Messages.PERMISSIONS_ERROR.getErrorMessage())).build();
                }
                commit(request.getCommitRequest(), itemId, version, user);
                break;
            case Revert:
                revert(request.getRevisionRequest(), itemId, versionId);
                break;
            case Clean:
                getManagersProvider().getVersioningManager().clean(itemId, version);
                break;
            default:
                throw new UnsupportedOperationException(String.format("Action %s not supported.", request.getAction()));
        }
        return Response.ok().build();
    }

    private Version getVersion(String itemId, Version version) {
        Version retrievedVersion = getManagersProvider().getVersioningManager().get(itemId, version);
        if (retrievedVersion.getState().getSynchronizationState() != SynchronizationState.Merging &&
            // looks for sdc applicative conflicts
            getManagersProvider().getConflictsManager().isConflicted(itemId, retrievedVersion)) {
            retrievedVersion.getState().setSynchronizationState(SynchronizationState.Merging);
        }
        return retrievedVersion;
    }

    private void sync(String itemId, Version version) {
        getManagersProvider().getVersioningManager().sync(itemId, version);
        getManagersProvider().getConflictsManager().finalizeMerge(itemId, version);
    }

    private void commit(CommitRequestDto request, String itemId, Version version, String user) {
        String message = request == null ? "" : request.getMessage();
        getManagersProvider().getVersioningManager().publish(itemId, version, message);
        notifyUsers(itemId, version, message, user, NotificationEventTypes.COMMIT);
        getManagersProvider().getActivityLogManager()
            .logActivity(new ActivityLogEntity(itemId, version, ActivityType.Commit, user, true, "", message));
    }

    private void revert(RevisionRequestDto request, String itemId, String versionId) {
        if (request.getRevisionId() == null) {
            throw new CoreException(new RevisionIdNotFoundErrorBuilder().build());
        }
        getManagersProvider().getVersioningManager().revert(itemId, new Version(versionId), request.getRevisionId());
    }

    private void filterRevisions(List<Revision> revisions) {
  /* When creating a new item an initial version is created with invalid data.
     This revision is not an applicable revision. The logic of identifying this revision is:
     1- only the first version of item has this issue
     2- only in the first item version there are 2 revisions created
     3- the second revision is in format "Initial <vlm/vsp>: <name of the vlm/vsp>"
     4- only if a revision in this format exists we remove the first revision. */
        int numOfRevisions = revisions.size();
        if (numOfRevisions > 1 && revisions.get(numOfRevisions - 2).getMessage().matches("Initial .*:.*")) {
            revisions.remove(numOfRevisions - 1);
        }
    }

    private void notifyUsers(String itemId, Version version, String message, String userName, NotificationEventTypes eventType) {
        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(ITEM_NAME, getManagersProvider().getAsdcItemManager().get(itemId).getName());
        eventProperties.put(ITEM_ID, itemId);
        Version ver = getManagersProvider().getVersioningManager().get(itemId, version);
        eventProperties.put(VERSION_NAME, ver.getName());
        eventProperties.put(VERSION_ID, ver.getId());
        eventProperties.put(SUBMIT_DESCRIPTION, message);
        eventProperties.put(PERMISSION_USER, userName);
        Event syncEvent = new SyncEvent(eventType.getEventName(), itemId, eventProperties, itemId);
        try {
            getManagersProvider().getNotificationPropagationManager().notifySubscribers(syncEvent, userName);
        } catch (Exception e) {
            LOGGER.error("Failed to send sync notification to users subscribed o item '" + itemId);
        }
    }

    private ManagersProvider getManagersProvider() {
        if (managersProvider == null) {
            managersProvider = new ManagersProvider();
        }
        return managersProvider;
    }

    @VisibleForTesting
    void setManagersProvider(ManagersProvider managersProvider) {
        this.managersProvider = managersProvider;
    }
}
