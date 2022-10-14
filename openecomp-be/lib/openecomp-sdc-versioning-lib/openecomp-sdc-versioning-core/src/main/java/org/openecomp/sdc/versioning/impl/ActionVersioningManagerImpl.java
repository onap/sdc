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
package org.openecomp.sdc.versioning.impl;

import com.google.common.annotations.VisibleForTesting;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.versioning.ActionVersioningManager;
import org.openecomp.sdc.versioning.AsdcItemManager;
import org.openecomp.sdc.versioning.VersionCalculator;
import org.openecomp.sdc.versioning.dao.VersionDao;
import org.openecomp.sdc.versioning.dao.VersionInfoDao;
import org.openecomp.sdc.versioning.dao.VersionInfoDeletedDao;
import org.openecomp.sdc.versioning.dao.VersionableEntityDaoFactory;
import org.openecomp.sdc.versioning.dao.types.Revision;
import org.openecomp.sdc.versioning.dao.types.SynchronizationState;
import org.openecomp.sdc.versioning.dao.types.UserCandidateVersion;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionInfoDeletedEntity;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.errors.CheckinOnEntityLockedByOtherErrorBuilder;
import org.openecomp.sdc.versioning.errors.CheckinOnUnlockedEntityErrorBuilder;
import org.openecomp.sdc.versioning.errors.CheckoutOnLockedEntityErrorBuilder;
import org.openecomp.sdc.versioning.errors.DeleteOnLockedEntityErrorBuilder;
import org.openecomp.sdc.versioning.errors.EditOnEntityLockedByOtherErrorBuilder;
import org.openecomp.sdc.versioning.errors.EditOnUnlockedEntityErrorBuilder;
import org.openecomp.sdc.versioning.errors.EntityAlreadyExistErrorBuilder;
import org.openecomp.sdc.versioning.errors.EntityAlreadyFinalizedErrorBuilder;
import org.openecomp.sdc.versioning.errors.EntityNotExistErrorBuilder;
import org.openecomp.sdc.versioning.errors.SubmitLockedEntityNotAllowedErrorBuilder;
import org.openecomp.sdc.versioning.errors.UndoCheckoutOnEntityLockedByOtherErrorBuilder;
import org.openecomp.sdc.versioning.errors.UndoCheckoutOnUnlockedEntityErrorBuilder;
import org.openecomp.sdc.versioning.types.VersionCreationMethod;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;

public class ActionVersioningManagerImpl implements ActionVersioningManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionVersioningManagerImpl.class);
    private static final Version INITIAL_ACTIVE_VERSION = new Version(0, 0);
    private static Map<String, Set<VersionableEntityMetadata>> VERSIONABLE_ENTITIES = new HashMap<>();
    private final VersionInfoDao versionInfoDao;
    private final VersionInfoDeletedDao versionInfoDeletedDao;
    private VersionDao versionDao;
    private VersionCalculator versionCalculator;
    private AsdcItemManager asdcItemManager;

    public ActionVersioningManagerImpl(VersionInfoDao versionInfoDao, VersionInfoDeletedDao versionInfoDeletedDao, VersionDao versionDao,
                                       VersionCalculator versionCalculator, AsdcItemManager asdcItemManager) {
        this.versionInfoDao = versionInfoDao;
        this.versionInfoDeletedDao = versionInfoDeletedDao;
        this.versionDao = versionDao;
        this.versionCalculator = versionCalculator;
        this.asdcItemManager = asdcItemManager;
    }

    @VisibleForTesting
    ActionVersioningManagerImpl(Map<String, Set<VersionableEntityMetadata>> map) {
        this.versionInfoDao = null;
        this.versionInfoDeletedDao = null;
        VERSIONABLE_ENTITIES = map;
    }

    private static VersionInfo getVersionInfo(VersionInfoEntity versionInfoEntity, String user, VersionableEntityAction action) {
        return getVersionInfo(versionInfoEntity.getEntityId(), versionInfoEntity.getEntityType(), versionInfoEntity.getActiveVersion(),
            versionInfoEntity.getCandidate(), versionInfoEntity.getStatus(), versionInfoEntity.getLatestFinalVersion(),
            versionInfoEntity.getViewableVersions(), action, user);
    }

    private static VersionInfo getVersionInfo(VersionInfoDeletedEntity versionInfoEntity, String user, VersionableEntityAction action) {
        return getVersionInfo(versionInfoEntity.getEntityId(), versionInfoEntity.getEntityType(), versionInfoEntity.getActiveVersion(),
            versionInfoEntity.getCandidate(), versionInfoEntity.getStatus(), versionInfoEntity.getLatestFinalVersion(),
            versionInfoEntity.getViewableVersions(), action, user);
    }

    private static VersionInfo getVersionInfo(String entityId, String entityType, Version activeVer, UserCandidateVersion candidate,
                                              VersionStatus status, Version latestFinalVersion, Set<Version> viewableVersions,
                                              VersionableEntityAction action, String user) {
        Version activeVersion;
        if (action == VersionableEntityAction.Write) {
            if (candidate != null) {
                if (user.equals(candidate.getUser())) {
                    activeVersion = candidate.getVersion();
                } else {
                    throw new CoreException(new EditOnEntityLockedByOtherErrorBuilder(entityType, entityId, candidate.getUser()).build());
                }
            } else {
                throw new CoreException(new EditOnUnlockedEntityErrorBuilder(entityType, entityId).build());
            }
        } else {
            if (candidate != null && user.equals(candidate.getUser())) {
                activeVersion = candidate.getVersion();
            } else {
                activeVersion = activeVer;
            }
        }
        VersionInfo versionInfo = new VersionInfo();
        versionInfo.setStatus(status);
        activeVersion.setStatus(status);
        if (latestFinalVersion != null) {
            latestFinalVersion.setStatus(status);
        }
        if (viewableVersions != null) {
            viewableVersions.forEach(version -> version.setStatus(status));
            versionInfo.setViewableVersions(toSortedList(viewableVersions));
            versionInfo.setFinalVersions(getFinalVersions(viewableVersions));
        }
        versionInfo.setActiveVersion(activeVersion);
        versionInfo.setLatestFinalVersion(latestFinalVersion);
        if (candidate != null) {
            candidate.getVersion().setStatus(status);
            versionInfo.setLockingUser(candidate.getUser());
            if (user.equals(candidate.getUser())) {
                versionInfo.getViewableVersions().add(candidate.getVersion());
            }
        }
        return versionInfo;
    }

    private static List<Version> toSortedList(
        Set<Version> versions) { // changing the Set to List in DB will require migration...
        return versions.stream().sorted((o1, o2) -> o1.getMajor() > o2.getMajor() ? 1
                : o1.getMajor() == o2.getMajor() ? (o1.getMinor() > o2.getMinor() ? 1 : o1.getMinor() == o2.getMinor() ? 0 : -1) : -1)
            .collect(Collectors.toList());
    }

    private static List<Version> getFinalVersions(Set<Version> versions) {
        return versions.stream().filter(Version::isFinal).collect(Collectors.toList());
    }

    @Override
    public void register(String entityType, VersionableEntityMetadata entityMetadata) {
        Set<VersionableEntityMetadata> entitiesMetadata = VERSIONABLE_ENTITIES.computeIfAbsent(entityType, k -> new HashSet<>());
        entitiesMetadata.add(entityMetadata);
    }

    @Override
    public Version create(String entityType, String entityId, String user) {
        VersionInfoEntity versionInfoEntity = versionInfoDao.get(new VersionInfoEntity(entityType, entityId));
        if (versionInfoEntity != null) {
            throw new CoreException(new EntityAlreadyExistErrorBuilder(entityType, entityId).build());
        }
        versionInfoEntity = new VersionInfoEntity(entityType, entityId);
        versionInfoEntity.setActiveVersion(INITIAL_ACTIVE_VERSION);
        markAsCheckedOut(versionInfoEntity, user);
        versionInfoDao.create(versionInfoEntity);
        return versionInfoEntity.getCandidate().getVersion();
    }

    @Override
    public void delete(String entityType, String entityId, String user) {
        VersionInfoEntity versionInfoEntity = versionInfoDao.get(new VersionInfoEntity(entityType, entityId));
        if (versionInfoEntity == null) {
            throw new CoreException(new EntityNotExistErrorBuilder(entityType, entityId).build());
        }
        switch (versionInfoEntity.getStatus()) {
            case Locked:
                throw new CoreException(
                    new DeleteOnLockedEntityErrorBuilder(entityType, entityId, versionInfoEntity.getCandidate().getUser()).build());
            default:
                //do nothing
                break;
        }
        doDelete(versionInfoEntity);
    }

    @Override
    public void undoDelete(String entityType, String entityId, String user) {
        VersionInfoDeletedEntity versionInfoDeletedEntity = versionInfoDeletedDao.get(new VersionInfoDeletedEntity(entityType, entityId));
        if (versionInfoDeletedEntity == null) {
            throw new CoreException(new EntityNotExistErrorBuilder(entityType, entityId).build());
        }
        doUndoDelete(versionInfoDeletedEntity);
    }

    @Override
    public Version checkout(String entityType, String entityId, String user) {
        VersionInfoEntity versionInfoEntity = versionInfoDao.get(new VersionInfoEntity(entityType, entityId));
        if (versionInfoEntity == null) {
            throw new CoreException(new EntityNotExistErrorBuilder(entityType, entityId).build());
        }
        Version checkoutVersion = null;
        switch (versionInfoEntity.getStatus()) {
            case Locked:
                throw new CoreException(
                    new CheckoutOnLockedEntityErrorBuilder(entityType, entityId, versionInfoEntity.getCandidate().getUser()).build());
            case Certified:
            case Draft:
                checkoutVersion = doCheckout(versionInfoEntity, user);
                break;
            default:
                //do nothing
                break;
        }
        return checkoutVersion;
    }

    @Override
    public Version undoCheckout(String entityType, String entityId, String user) {
        VersionInfoEntity versionInfoEntity = versionInfoDao.get(new VersionInfoEntity(entityType, entityId));
        if (versionInfoEntity == null) {
            throw new CoreException(new EntityNotExistErrorBuilder(entityType, entityId).build());
        }
        Version activeVersion = null;
        switch (versionInfoEntity.getStatus()) {
            case Locked:
                if (!user.equals(versionInfoEntity.getCandidate().getUser())) {
                    throw new CoreException(
                        new UndoCheckoutOnEntityLockedByOtherErrorBuilder(entityType, entityId, versionInfoEntity.getCandidate().getUser()).build());
                }
                activeVersion = undoCheckout(versionInfoEntity);
                break;
            case Certified:
            case Draft:
                throw new CoreException(new UndoCheckoutOnUnlockedEntityErrorBuilder(entityType, entityId).build());
            default:
                //do nothing
                break;
        }
        return activeVersion;
    }

    private Version undoCheckout(VersionInfoEntity versionInfoEntity) {
        deleteVersionFromEntity(versionInfoEntity.getEntityType(), versionInfoEntity.getEntityId(), versionInfoEntity.getCandidate().getVersion(),
            versionInfoEntity.getActiveVersion());
        versionInfoEntity.setStatus(versionInfoEntity.getActiveVersion().isFinal() ? VersionStatus.Certified : VersionStatus.Draft);
        versionInfoEntity.setCandidate(null);
        versionInfoDao.update(versionInfoEntity);
        return versionInfoEntity.getActiveVersion();
    }

    @Override
    public Version checkin(String entityType, String entityId, String user, String checkinDescription) {
        VersionInfoEntity versionInfoEntity = versionInfoDao.get(new VersionInfoEntity(entityType, entityId));
        if (versionInfoEntity == null) {
            throw new CoreException(new EntityNotExistErrorBuilder(entityType, entityId).build());
        }
        Version checkedInVersion = null;
        switch (versionInfoEntity.getStatus()) {
            case Draft:
            case Certified:
                throw new CoreException(new CheckinOnUnlockedEntityErrorBuilder(entityType, entityId).build());
            case Locked:
                if (!user.equals(versionInfoEntity.getCandidate().getUser())) {
                    throw new CoreException(
                        new CheckinOnEntityLockedByOtherErrorBuilder(entityType, entityId, versionInfoEntity.getCandidate().getUser()).build());
                }
                checkedInVersion = doCheckin(versionInfoEntity, checkinDescription);
                break;
            default:
                //do nothing
                break;
        }
        return checkedInVersion;
    }

    @Override
    public Version submit(String entityType, String entityId, String user, String submitDescription) {
        VersionInfoEntity versionInfoEntity = versionInfoDao.get(new VersionInfoEntity(entityType, entityId));
        if (versionInfoEntity == null) {
            throw new CoreException(new EntityNotExistErrorBuilder(entityType, entityId).build());
        }
        Version submitVersion = null;
        switch (versionInfoEntity.getStatus()) {
            case Certified:
                throw new CoreException(new EntityAlreadyFinalizedErrorBuilder(entityType, entityId).build());
            case Locked:
                throw new CoreException(
                    new SubmitLockedEntityNotAllowedErrorBuilder(entityType, entityId, versionInfoEntity.getCandidate().getUser()).build());
            case Draft:
                submitVersion = doSubmit(versionInfoEntity, user, submitDescription);
                break;
            default:
                //do nothing
                break;
        }
        return submitVersion;
    }

    @Override
    public VersionInfo getEntityVersionInfo(String entityType, String entityId, String user, VersionableEntityAction action) {
        VersionInfoEntity versionInfoEntity = versionInfoDao.get(new VersionInfoEntity(entityType, entityId));
        if (versionInfoEntity == null) {
            throw new CoreException(new EntityNotExistErrorBuilder(entityType, entityId).build());
        }
        return getVersionInfo(versionInfoEntity, user, action);
    }

    @Override
    public Map<String, VersionInfo> listEntitiesVersionInfo(String entityType, String user, VersionableEntityAction action) {
        Collection<VersionInfoEntity> versionInfoEntities = versionInfoDao.list(new VersionInfoEntity(entityType, null));
        Map<String, VersionInfo> activeVersions = new HashMap<>();
        for (VersionInfoEntity versionInfoEntity : versionInfoEntities) {
            activeVersions.put(versionInfoEntity.getEntityId(), getVersionInfo(versionInfoEntity, user, action));
        }
        return activeVersions;
    }

    @Override
    public Map<String, VersionInfo> listDeletedEntitiesVersionInfo(String entityType, String user, VersionableEntityAction action) {
        Collection<VersionInfoDeletedEntity> versionInfoDeletedEntities = versionInfoDeletedDao.list(new VersionInfoDeletedEntity(entityType, null));
        Map<String, VersionInfo> activeVersions = new HashMap<>();
        for (VersionInfoDeletedEntity versionInfoDeletedEntity : versionInfoDeletedEntities) {
            activeVersions.put(versionInfoDeletedEntity.getEntityId(), getVersionInfo(versionInfoDeletedEntity, user, action));
        }
        return activeVersions;
    }

    @Override
    public List<Version> list(String itemId) {
        List<Version> versions = versionDao.list(itemId);
        Set<String> versionsNames = versions.stream().map(Version::getName).collect(Collectors.toSet());
        versions.forEach(version -> {
            version.setAdditionalInfo(new HashMap<>());
            versionCalculator.injectAdditionalInfo(version, versionsNames);
        });
        return versions;
    }

    @Override
    public Version get(String itemId, Version version) {
        return versionDao.get(itemId, version).map(retrievedVersion -> getUpdateRetrievedVersion(itemId, retrievedVersion))
            .orElseGet(() -> getSyncedVersion(itemId, version));
    }

    private Version getUpdateRetrievedVersion(String itemId, Version version) {
        if (version.getStatus() == VersionStatus.Certified && (version.getState().getSynchronizationState() == SynchronizationState.OutOfSync
            || version.getState().isDirty())) {
            forceSync(itemId, version);
            LOGGER.info("Item Id {}, version Id {}: Force sync is done", itemId, version.getId());
            version = versionDao.get(itemId, version)
                .orElseThrow(() -> new IllegalStateException("Get version after a successful force sync must return the version"));
        }
        return version;
    }

    private Version getSyncedVersion(String itemId, Version version) {
        sync(itemId, version);
        LOGGER.info("Item Id {}, version Id {}: First time sync is done", itemId, version.getId());
        return versionDao.get(itemId, version)
            .orElseThrow(() -> new IllegalStateException("Get version after a successful sync must return the version"));
    }

    @Override
    public Version create(String itemId, Version version, VersionCreationMethod creationMethod) {
        String baseVersionName = null;
        if (version.getBaseId() == null) {
            version.setDescription("Initial version");
        } else {
            baseVersionName = get(itemId, new Version(version.getBaseId())).getName();
        }
        String versionName = versionCalculator.calculate(baseVersionName, creationMethod);
        validateVersionName(itemId, versionName);
        version.setName(versionName);
        versionDao.create(itemId, version);
        asdcItemManager.updateVersionStatus(itemId, VersionStatus.Draft, null);
        publish(itemId, version, String.format("Create version: %s", version.getName()));
        return version;
    }

    private void validateVersionName(String itemId, String versionName) {
        if (versionDao.list(itemId).stream().anyMatch(version -> versionName.equals(version.getName()))) {
            String errorDescription = String.format("Item %s: create version failed, a version with the name %s already exist", itemId, versionName);
            throw new CoreException(new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION).withId("VERSION_NAME_ALREADY_EXIST")
                .withMessage(errorDescription).build());
        }
    }

    @Override
    public void submit(String itemId, Version version, String submitDescription) {
        version = get(itemId, version);
        validateSubmit(itemId, version);
        version.setStatus(VersionStatus.Certified);
        versionDao.update(itemId, version);
        publish(itemId, version, submitDescription);
        asdcItemManager.updateVersionStatus(itemId, VersionStatus.Certified, VersionStatus.Draft);
    }

    private void validateSubmit(String itemId, Version version) {
        if (version.getStatus() == VersionStatus.Certified) {
            String errorDescription = String.format("Item %s: submit version failed, version %s is already Certified", itemId, version.getId());
            throw new CoreException(new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION).withId("VERSION_ALREADY_CERTIFIED")
                .withMessage(errorDescription).build());
        }
    }

    @Override
    public void publish(String itemId, Version version, String message) {
        versionDao.publish(itemId, version, message);
    }

    @Override
    public void sync(String itemId, Version version) {
        versionDao.sync(itemId, version);
    }

    @Override
    public void forceSync(String itemId, Version version) {
        versionDao.forceSync(itemId, version);
    }

    @Override
    public void revert(String itemId, Version version, String revisionId) {
        versionDao.revert(itemId, version, revisionId);
    }

    @Override
    public List<Revision> listRevisions(String itemId, Version version) {
        return versionDao.listRevisions(itemId, version);
    }

    private void markAsCheckedOut(VersionInfoEntity versionInfoEntity, String checkingOutUser) {
        versionInfoEntity.setStatus(VersionStatus.Locked);
        versionInfoEntity.setCandidate(new UserCandidateVersion(checkingOutUser, versionInfoEntity.getActiveVersion().calculateNextCandidate()));
    }

    private Version doCheckout(VersionInfoEntity versionInfoEntity, String user) {
        markAsCheckedOut(versionInfoEntity, user);
        versionInfoDao.update(versionInfoEntity);
        initVersionOnEntity(versionInfoEntity.getEntityType(), versionInfoEntity.getEntityId(), versionInfoEntity.getActiveVersion(),
            versionInfoEntity.getCandidate().getVersion());
        return versionInfoEntity.getCandidate().getVersion();
    }

    private void doDelete(VersionInfoEntity versionInfoEntity) {
        VersionInfoDeletedEntity versionInfoDeletedEntity = new VersionInfoDeletedEntity();
        versionInfoDeletedEntity.setStatus(versionInfoEntity.getStatus());
        versionInfoDeletedEntity.setViewableVersions(versionInfoEntity.getViewableVersions());
        versionInfoDeletedEntity.setActiveVersion(versionInfoEntity.getActiveVersion());
        versionInfoDeletedEntity.setCandidate(versionInfoEntity.getCandidate());
        versionInfoDeletedEntity.setEntityId(versionInfoEntity.getEntityId());
        versionInfoDeletedEntity.setEntityType(versionInfoEntity.getEntityType());
        versionInfoDeletedEntity.setLatestFinalVersion(versionInfoEntity.getLatestFinalVersion());
        versionInfoDeletedDao.create(versionInfoDeletedEntity);
        versionInfoDao.delete(versionInfoEntity);
    }

    private void doUndoDelete(VersionInfoDeletedEntity versionInfoDeletedEntity) {
        VersionInfoEntity versionInfoEntity = new VersionInfoEntity();
        versionInfoEntity.setStatus(versionInfoDeletedEntity.getStatus());
        versionInfoEntity.setViewableVersions(versionInfoDeletedEntity.getViewableVersions());
        versionInfoEntity.setActiveVersion(versionInfoDeletedEntity.getActiveVersion());
        versionInfoEntity.setCandidate(versionInfoDeletedEntity.getCandidate());
        versionInfoEntity.setEntityId(versionInfoDeletedEntity.getEntityId());
        versionInfoEntity.setEntityType(versionInfoDeletedEntity.getEntityType());
        versionInfoEntity.setLatestFinalVersion(versionInfoDeletedEntity.getLatestFinalVersion());
        versionInfoDao.create(versionInfoEntity);
        versionInfoDeletedDao.delete(versionInfoDeletedEntity);
    }

    private Version doCheckin(VersionInfoEntity versionInfoEntity, String checkinDescription) {
        UserCandidateVersion userCandidateVersion = versionInfoEntity.getCandidate();
        versionInfoEntity.setCandidate(null);
        versionInfoEntity.setActiveVersion(userCandidateVersion.getVersion());
        versionInfoEntity.getViewableVersions().add(versionInfoEntity.getActiveVersion());
        versionInfoEntity.setStatus(VersionStatus.Draft);
        closeVersionOnEntity(versionInfoEntity.getEntityType(), versionInfoEntity.getEntityId(), versionInfoEntity.getActiveVersion());
        versionInfoDao.update(versionInfoEntity);
        return versionInfoEntity.getActiveVersion();
    }

    private Version doSubmit(VersionInfoEntity versionInfoEntity, String submittingUser, String submitDescription) {
        Version finalVersion = versionInfoEntity.getActiveVersion().calculateNextFinal();
        initVersionOnEntity(versionInfoEntity.getEntityType(), versionInfoEntity.getEntityId(), versionInfoEntity.getActiveVersion(), finalVersion);
        closeVersionOnEntity(versionInfoEntity.getEntityType(), versionInfoEntity.getEntityId(), finalVersion);
        Set<Version> viewableVersions = new HashSet<>();
        for (Version version : versionInfoEntity.getViewableVersions()) {
            if (version.isFinal()) {
                viewableVersions.add(version);
            }
        }
        viewableVersions.add(finalVersion);
        versionInfoEntity.setViewableVersions(viewableVersions);
        versionInfoEntity.setActiveVersion(finalVersion);
        versionInfoEntity.setLatestFinalVersion(finalVersion);
        versionInfoEntity.setStatus(VersionStatus.Certified);
        versionInfoDao.update(versionInfoEntity);
        return finalVersion;
    }

    private void initVersionOnEntity(String entityType, String entityId, Version baseVersion, Version newVersion) {
        Set<VersionableEntityMetadata> entityMetadatas = VERSIONABLE_ENTITIES.get(entityType);
        if (entityMetadatas != null) {
            for (VersionableEntityMetadata entityMetadata : entityMetadatas) {
                VersionableEntityDaoFactory.getInstance().createInterface(entityMetadata.getStoreType())
                    .initVersion(entityMetadata, entityId, baseVersion, newVersion);
            }
        }
    }

    private void deleteVersionFromEntity(String entityType, String entityId, Version versionToDelete, Version backToVersion) {
        Set<VersionableEntityMetadata> entityMetadatas = VERSIONABLE_ENTITIES.get(entityType);
        if (entityMetadatas != null) {
            for (VersionableEntityMetadata entityMetadata : entityMetadatas) {
                VersionableEntityDaoFactory.getInstance().createInterface(entityMetadata.getStoreType())
                    .deleteVersion(entityMetadata, entityId, versionToDelete, backToVersion);
            }
        }
    }

    private void closeVersionOnEntity(String entityType, String entityId, Version versionToClose) {
        Set<VersionableEntityMetadata> entityMetadatas = VERSIONABLE_ENTITIES.get(entityType);
        if (entityMetadatas != null) {
            for (VersionableEntityMetadata entityMetadata : entityMetadatas) {
                VersionableEntityDaoFactory.getInstance().createInterface(entityMetadata.getStoreType())
                    .closeVersion(entityMetadata, entityId, versionToClose);
            }
        }
    }
}
