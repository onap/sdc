/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.versioning.impl;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.VersionInfoDao;
import org.openecomp.sdc.versioning.dao.VersionInfoDeletedDao;
import org.openecomp.sdc.versioning.dao.VersionableEntityDaoFactory;
import org.openecomp.sdc.versioning.dao.types.UserCandidateVersion;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionHistoryEntity;
import org.openecomp.sdc.versioning.dao.types.VersionInfoDeletedEntity;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.dao.types.VersionType;
import org.openecomp.sdc.versioning.dao.types.VersionableEntityId;
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
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;
import org.slf4j.MDC;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class VersioningManagerImpl implements VersioningManager {

  private static final Version INITIAL_ACTIVE_VERSION = new Version(0, 0);
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private static Map<String, Set<VersionableEntityMetadata>> versionableEntities = new HashMap<>();

  private VersionInfoDao versionInfoDao;
  private VersionInfoDeletedDao versionInfoDeletedDao;

  public VersioningManagerImpl(VersionInfoDao versionInfoDao,
                               VersionInfoDeletedDao versionInfoDeletedDao) {
    this.versionInfoDao = versionInfoDao;
    this.versionInfoDeletedDao = versionInfoDeletedDao;
  }

  private static VersionInfo getVersionInfo(VersionInfoEntity versionInfoEntity, String user,
                                            VersionableEntityAction action) {
    return getVersionInfo(versionInfoEntity.getEntityId(),
        versionInfoEntity.getEntityType(),
        versionInfoEntity.getActiveVersion(),
        versionInfoEntity.getCandidate(),
        versionInfoEntity.getStatus(),
        versionInfoEntity.getLatestFinalVersion(),
        versionInfoEntity.getViewableVersions(),
        action,
        user);
  }

  private static VersionInfo getVersionInfo(VersionInfoDeletedEntity versionInfoEntity, String user,
                                            VersionableEntityAction action) {
    return getVersionInfo(versionInfoEntity.getEntityId(),
        versionInfoEntity.getEntityType(),
        versionInfoEntity.getActiveVersion(),
        versionInfoEntity.getCandidate(),
        versionInfoEntity.getStatus(),
        versionInfoEntity.getLatestFinalVersion(),
        versionInfoEntity.getViewableVersions(),
        action,
        user);
  }

  private static VersionInfo getVersionInfo(String entityId, String entityType, Version activeVer,
                                            UserCandidateVersion candidate, VersionStatus status,
                                            Version latestFinalVersion,
                                            Set<Version> viewableVersions,
                                            VersionableEntityAction action, String user) {


    mdcDataDebugMessage.debugEntryMessage("entity Id", entityId);

    Version activeVersion;

    if (action == VersionableEntityAction.Write) {
      if (candidate != null) {
        if (user.equals(candidate.getUser())) {
          activeVersion = candidate.getVersion();
        } else {
          MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
              LoggerTragetServiceName.GET_VERSION_INFO, ErrorLevel.ERROR.name(),
              LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), "Can't get entity version info");
          throw new CoreException(
              new EditOnEntityLockedByOtherErrorBuilder(entityType, entityId, candidate.getUser())
                  .build());
        }
      } else {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.GET_VERSION_INFO, ErrorLevel.ERROR.name(),
            LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), "Can't get entity version info");
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
    if(latestFinalVersion!= null) latestFinalVersion.setStatus(status);
    if(viewableVersions != null) viewableVersions.forEach(version->version.setStatus(status));
    versionInfo.setActiveVersion(activeVersion);
    versionInfo.setLatestFinalVersion(latestFinalVersion);
    versionInfo.setViewableVersions(toSortedList(viewableVersions));
    versionInfo.setFinalVersions(getFinalVersions(viewableVersions));
    if (candidate != null) {
      candidate.getVersion().setStatus(status);
      versionInfo.setLockingUser(candidate.getUser());
      if (user.equals(candidate.getUser())) {
        versionInfo.getViewableVersions().add(candidate.getVersion());
      }
    }

    mdcDataDebugMessage.debugExitMessage("entity Id", entityId);
    return versionInfo;
  }

  private static List<Version> toSortedList(
      Set<Version> versions) { // changing the Set to List in DB will require migration...
    return versions.stream().sorted((o1, o2) -> {
      return o1.getMajor() > o2.getMajor() ? 1
          : o1.getMajor() == o2.getMajor() ? (o1.getMinor() > o2.getMinor() ? 1
              : o1.getMinor() == o2.getMinor() ? 0 : -1) : -1;
    }).collect(Collectors.toList());
  }

  private static List<Version> getFinalVersions(Set<Version> versions) {
    return versions.stream().filter(Version::isFinal).collect(Collectors.toList());
  }

  @Override
  public void register(String entityType, VersionableEntityMetadata entityMetadata) {
    Set<VersionableEntityMetadata> entitiesMetadata =
        versionableEntities.computeIfAbsent(entityType, k -> new HashSet<>());
    entitiesMetadata.add(entityMetadata);
  }

  @Override
  public Version create(String entityType, String entityId, String user) {
    VersionInfoEntity
        versionInfoEntity = versionInfoDao.get(new VersionInfoEntity(entityType, entityId));
    if (versionInfoEntity != null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.CREATE_ENTITY, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Can't create versionable entity");
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
    VersionInfoEntity versionInfoEntity =
        versionInfoDao.get(new VersionInfoEntity(entityType, entityId));
    if (versionInfoEntity == null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.DELETE_ENTITY, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Can't delete versionable entity");
      throw new CoreException(new EntityNotExistErrorBuilder(entityType, entityId).build());
    }

    switch (versionInfoEntity.getStatus()) {
      case Locked:
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.DELETE_ENTITY, ErrorLevel.ERROR.name(),
            LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), "Can't delete versionable entity");
        throw new CoreException(new DeleteOnLockedEntityErrorBuilder(entityType, entityId,
            versionInfoEntity.getCandidate().getUser()).build());
      default:
        //do nothing
        break;
    }

    doDelete(versionInfoEntity);
  }

  @Override
  public void undoDelete(String entityType, String entityId, String user) {
    VersionInfoDeletedEntity versionInfoDeletedEntity =
        versionInfoDeletedDao.get(new VersionInfoDeletedEntity(entityType, entityId));
    if (versionInfoDeletedEntity == null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.UNDO_DELETE_ENTITY, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Can't undo delete for versionable entity");
      throw new CoreException(new EntityNotExistErrorBuilder(entityType, entityId).build());
    }

    doUndoDelete(versionInfoDeletedEntity);
  }

  @Override
  public Version checkout(String entityType, String entityId, String user) {
    VersionInfoEntity versionInfoEntity =
        versionInfoDao.get(new VersionInfoEntity(entityType, entityId));
    MDC.put(LoggerConstants.TARGET_SERVICE_NAME, LoggerTragetServiceName.CHECKOUT_ENTITY);
    if (versionInfoEntity == null) {
      MDC.put(LoggerConstants.ERROR_CATEGORY, ErrorLevel.ERROR.name());
      MDC.put(LoggerConstants.TARGET_ENTITY, LoggerConstants.TARGET_ENTITY_DB);
      MDC.put(LoggerConstants.ERROR_DESCRIPTION, LoggerErrorDescription.CHECKOUT_ENTITY);
      throw new CoreException(new EntityNotExistErrorBuilder(entityType, entityId).build());
    }

    Version checkoutVersion = null;
    switch (versionInfoEntity.getStatus()) {
      case Locked:
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.CHECKOUT_ENTITY, ErrorLevel.ERROR.name(),
            LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), "Can't checkout versionable entity");
        throw new CoreException(new CheckoutOnLockedEntityErrorBuilder(entityType, entityId,
            versionInfoEntity.getCandidate().getUser()).build());
      case Final:
      case Available:
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
    VersionInfoEntity versionInfoEntity =
        versionInfoDao.get(new VersionInfoEntity(entityType, entityId));
    if (versionInfoEntity == null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.UNDO_CHECKOUT_ENTITY, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Can't undo checkout for versionable entity");
      throw new CoreException(new EntityNotExistErrorBuilder(entityType, entityId).build());
    }

    Version activeVersion = null;
    switch (versionInfoEntity.getStatus()) {
      case Locked:
        if (!user.equals(versionInfoEntity.getCandidate().getUser())) {
          MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
              LoggerTragetServiceName.UNDO_CHECKOUT_ENTITY, ErrorLevel.ERROR.name(),
              LoggerErrorCode.PERMISSION_ERROR.getErrorCode(),
              "Can't undo checkout for versionable entity");
          throw new CoreException(
              new UndoCheckoutOnEntityLockedByOtherErrorBuilder(entityType, entityId,
                  versionInfoEntity.getCandidate().getUser()).build());
        }
        activeVersion = undoCheckout(versionInfoEntity);
        break;
      case Final:
      case Available:
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.UNDO_CHECKOUT_ENTITY, ErrorLevel.ERROR.name(),
            LoggerErrorCode.PERMISSION_ERROR.getErrorCode(),
            "Can't undo checkout for versionable entity");
        throw new CoreException(
            new UndoCheckoutOnUnlockedEntityErrorBuilder(entityType, entityId).build());
      default:
        //do nothing
        break;
    }

    return activeVersion;
  }

  private Version undoCheckout(VersionInfoEntity versionInfoEntity) {
    deleteVersionFromEntity(versionInfoEntity.getEntityType(), versionInfoEntity.getEntityId(),
        versionInfoEntity.getCandidate().getVersion(), versionInfoEntity.getActiveVersion());

    versionInfoEntity.setStatus(versionInfoEntity.getActiveVersion().isFinal() ? VersionStatus.Final
        : VersionStatus.Available);
    versionInfoEntity.setCandidate(null);
    versionInfoDao.update(versionInfoEntity);
    return versionInfoEntity.getActiveVersion();
  }

  @Override
  public Version checkin(String entityType, String entityId, String user,
                         String checkinDescription) {
    VersionInfoEntity versionInfoEntity =
        versionInfoDao.get(new VersionInfoEntity(entityType, entityId));
    if (versionInfoEntity == null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.CHECKIN_ENTITY, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Can't checkin versionable entity");
      throw new CoreException(new EntityNotExistErrorBuilder(entityType, entityId).build());
    }

    Version checkedInVersion = null;
    switch (versionInfoEntity.getStatus()) {
      case Available:
      case Final:
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.CHECKIN_ENTITY, ErrorLevel.ERROR.name(),
            LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), "Can't checkin versionable entity");
        throw new CoreException(
            new CheckinOnUnlockedEntityErrorBuilder(entityType, entityId).build());
      case Locked:
        if (!user.equals(versionInfoEntity.getCandidate().getUser())) {
          MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
              LoggerTragetServiceName.CHECKIN_ENTITY, ErrorLevel.ERROR.name(),
              LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), "Can't checkin versionable entity");
          throw new CoreException(new CheckinOnEntityLockedByOtherErrorBuilder(entityType, entityId,
              versionInfoEntity.getCandidate().getUser()).build());
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
    VersionInfoEntity versionInfoEntity =
        versionInfoDao.get(new VersionInfoEntity(entityType, entityId));
    if (versionInfoEntity == null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.SUBMIT_ENTITY, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Can't submit versionable entity");
      throw new CoreException(new EntityNotExistErrorBuilder(entityType, entityId).build());
    }

    Version submitVersion = null;
    switch (versionInfoEntity.getStatus()) {
      case Final:
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.SUBMIT_ENTITY, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(), "Can't submit versionable entity");
        throw new CoreException(
            new EntityAlreadyFinalizedErrorBuilder(entityType, entityId).build());
      case Locked:
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.SUBMIT_ENTITY, ErrorLevel.ERROR.name(),
            LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), "Can't submit versionable entity");
        throw new CoreException(new SubmitLockedEntityNotAllowedErrorBuilder(entityType, entityId,
            versionInfoEntity.getCandidate().getUser()).build());
      case Available:
        submitVersion = doSubmit(versionInfoEntity, user, submitDescription);
        break;
      default:
        //do nothing
        break;
    }

    return submitVersion;
  }

  @Override
  public VersionInfo getEntityVersionInfo(String entityType, String entityId, String user,
                                          VersionableEntityAction action) {
    VersionInfoEntity versionInfoEntity =
        versionInfoDao.get(new VersionInfoEntity(entityType, entityId));
    if (versionInfoEntity == null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.GET_VERSION_INFO, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Can't get entity version info");
      throw new CoreException(new EntityNotExistErrorBuilder(entityType, entityId).build());
    }
    return getVersionInfo(versionInfoEntity, user, action);
  }

  @Override
  public Map<String, VersionInfo> listEntitiesVersionInfo(String entityType, String user,
                                                          VersionableEntityAction action) {
    Collection<VersionInfoEntity> versionInfoEntities =
        versionInfoDao.list(new VersionInfoEntity(entityType, null));
    Map<String, VersionInfo> activeVersions = new HashMap<>();
    for (VersionInfoEntity versionInfoEntity : versionInfoEntities) {
      activeVersions
          .put(versionInfoEntity.getEntityId(), getVersionInfo(versionInfoEntity, user, action));
    }
    return activeVersions;
  }

  @Override
  public Map<String, VersionInfo> listDeletedEntitiesVersionInfo(String entityType, String user,
                                                                 VersionableEntityAction action) {
    Collection<VersionInfoDeletedEntity> versionInfoDeletedEntities =
        versionInfoDeletedDao.list(new VersionInfoDeletedEntity(entityType, null));
    Map<String, VersionInfo> activeVersions = new HashMap<>();


    for (VersionInfoDeletedEntity versionInfoDeletedEntity : versionInfoDeletedEntities) {
      activeVersions.put(versionInfoDeletedEntity.getEntityId(),
          getVersionInfo(versionInfoDeletedEntity, user, action));
    }
    return activeVersions;
  }

  private void markAsCheckedOut(VersionInfoEntity versionInfoEntity, String checkingOutUser) {
    versionInfoEntity.setStatus(VersionStatus.Locked);
    versionInfoEntity.setCandidate(new UserCandidateVersion(checkingOutUser,
        versionInfoEntity.getActiveVersion().calculateNextCandidate()));
  }

  private Version doCheckout(VersionInfoEntity versionInfoEntity, String user) {
    markAsCheckedOut(versionInfoEntity, user);
    versionInfoDao.update(versionInfoEntity);

    initVersionOnEntity(versionInfoEntity.getEntityType(), versionInfoEntity.getEntityId(),
        versionInfoEntity.getActiveVersion(), versionInfoEntity.getCandidate().getVersion());

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
    versionInfoEntity.setStatus(VersionStatus.Available);
    versionInfoDao.update(versionInfoEntity);

    closeVersionOnEntity(versionInfoEntity.getEntityType(), versionInfoEntity.getEntityId(),
        versionInfoEntity.getActiveVersion());

    return versionInfoEntity.getActiveVersion();
  }

  private Version doSubmit(VersionInfoEntity versionInfoEntity, String submittingUser,
                           String submitDescription) {
    Version finalVersion = versionInfoEntity.getActiveVersion().calculateNextFinal();
    initVersionOnEntity(versionInfoEntity.getEntityType(), versionInfoEntity.getEntityId(),
        versionInfoEntity.getActiveVersion(), finalVersion);
    closeVersionOnEntity(versionInfoEntity.getEntityType(), versionInfoEntity.getEntityId(),
        finalVersion);

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
    versionInfoEntity.setStatus(VersionStatus.Final);
    versionInfoDao.update(versionInfoEntity);

    return finalVersion;
  }

  private void createVersionHistory(VersionableEntityId entityId, Version version, String user,
                                    String description, VersionType type) {
    VersionHistoryEntity versionHistory = new VersionHistoryEntity(entityId);
    versionHistory.setVersion(version);
    versionHistory.setUser(user);
    versionHistory.setDescription(description);
    versionHistory.setType(type);
    //versionHistoryDao.create(versionHistory);
  }

  private void initVersionOnEntity(String entityType, String entityId, Version baseVersion,
                                   Version newVersion) {
    Set<VersionableEntityMetadata> entityMetadatas = versionableEntities.get(entityType);
    if (entityMetadatas != null) {
      for (VersionableEntityMetadata entityMetadata : entityMetadatas) {
        VersionableEntityDaoFactory.getInstance().createInterface(entityMetadata.getStoreType())
            .initVersion(entityMetadata, entityId, baseVersion, newVersion);
      }
    }
  }

  private void deleteVersionFromEntity(String entityType, String entityId,
                                       Version versionToDelete, Version backToVersion) {
    Set<VersionableEntityMetadata> entityMetadatas = versionableEntities.get(entityType);
    if (entityMetadatas != null) {
      for (VersionableEntityMetadata entityMetadata : entityMetadatas) {
        VersionableEntityDaoFactory.getInstance().createInterface(entityMetadata.getStoreType())
            .deleteVersion(entityMetadata, entityId, versionToDelete, backToVersion);
      }
    }
  }

  private void closeVersionOnEntity(String entityType, String entityId, Version versionToClose) {
    Set<VersionableEntityMetadata> entityMetadatas = versionableEntities.get(entityType);
    if (entityMetadatas != null) {
      for (VersionableEntityMetadata entityMetadata : entityMetadatas) {
        VersionableEntityDaoFactory.getInstance().createInterface(entityMetadata.getStoreType())
            .closeVersion(entityMetadata, entityId, versionToClose);
      }
    }
  }
}
