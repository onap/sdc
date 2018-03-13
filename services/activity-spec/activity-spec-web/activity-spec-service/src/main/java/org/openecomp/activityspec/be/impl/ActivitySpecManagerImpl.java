/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */

package org.openecomp.activityspec.be.impl;

import static org.openecomp.activityspec.api.rest.types.ActivitySpecAction.CERTIFY;
import static org.openecomp.activityspec.api.rest.types.ActivitySpecAction.DELETE;
import static org.openecomp.activityspec.api.rest.types.ActivitySpecAction.DEPRECATE;
import static org.openecomp.sdc.versioning.dao.types.VersionStatus.Certified;
import static org.openecomp.sdc.versioning.dao.types.VersionStatus.Deleted;
import static org.openecomp.sdc.versioning.dao.types.VersionStatus.Deprecated;
import static org.openecomp.sdc.versioning.dao.types.VersionStatus.Draft;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import org.openecomp.activityspec.api.rest.types.ActivitySpecAction;
import org.openecomp.activityspec.be.ActivitySpecManager;
import org.openecomp.activityspec.be.dao.ActivitySpecDao;
import org.openecomp.activityspec.be.dao.types.ActivitySpecEntity;
import org.openecomp.activityspec.be.datatypes.ItemType;
import org.openecomp.activityspec.utils.ActivitySpecConstant;
import org.openecomp.core.dao.UniqueValueDao;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.SdcRuntimeException;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.versioning.ItemManager;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.types.VersionCreationMethod;

public class ActivitySpecManagerImpl implements ActivitySpecManager {

  private static final Map<VersionStatus, Transition> TRANSITIONS;

  static {
    TRANSITIONS = new EnumMap<>(VersionStatus.class);
    TRANSITIONS.put(Certified, new Transition("Certify", Draft));
    TRANSITIONS.put(Deprecated, new Transition("Deprecate", Certified));
    TRANSITIONS.put(Deleted, new Transition("Delete", Deprecated));
  }

  private final ItemManager itemManager;
  private final VersioningManager versioningManager;
  private final ActivitySpecDao activitySpecDao;
  private final UniqueValueUtil uniqueValueUtil;
  private static final String ACTIVITY_SPEC_NAME = "ActivitySpec.Name";
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ActivitySpecManagerImpl.class);

  public ActivitySpecManagerImpl(ItemManager itemManager,
                                 VersioningManager versioningManager,
                                 ActivitySpecDao activitySpecDao,
                                 UniqueValueDao uniqueValueDao) {
    this.itemManager = itemManager;
    this.versioningManager = versioningManager;
    this.activitySpecDao = activitySpecDao;
    this.uniqueValueUtil = new UniqueValueUtil(uniqueValueDao);
  }

  @Override
  public ActivitySpecEntity createActivitySpec(ActivitySpecEntity activitySpecEntity) {

    uniqueValueUtil.validateUniqueValue(ACTIVITY_SPEC_NAME, activitySpecEntity.getName());

    Item item = getActivitySpecItem(activitySpecEntity);
    item = itemManager.create(item);

    Version version = getActivitySpecVersion(activitySpecEntity);
    versioningManager.create(item.getId(), version, VersionCreationMethod.major);

    enrichActivitySpec(item, version, activitySpecEntity);
    activitySpecDao.create(activitySpecEntity);

    uniqueValueUtil.createUniqueValue(ACTIVITY_SPEC_NAME, activitySpecEntity.getName());
    return activitySpecEntity;
  }

  private Version getActivitySpecVersion(ActivitySpecEntity activitySpecEntity) {
    return activitySpecEntity.getVersion() == null ? new Version()
        : activitySpecEntity.getVersion();

  }

  @Override
  public ActivitySpecEntity get(ActivitySpecEntity activitySpec) {
    activitySpec.setVersion(calculateLatestVersion(activitySpec.getId(),activitySpec.getVersion()
        ));
    ActivitySpecEntity retrieved = null;
    try {
      retrieved = activitySpecDao.get(activitySpec);
    } catch (SdcRuntimeException runtimeException) {
      LOGGER.error("Failed to retrieve activity spec for activitySpecId: " + activitySpec.getId() + " and version: "
          + activitySpec.getVersion().getId(), runtimeException);
      validateActivitySpecExistence(activitySpec.getId(), activitySpec.getVersion());
    }
    if (retrieved != null) {
      final Version retrievedVersion = versioningManager.get(activitySpec.getId(),
          activitySpec.getVersion());
      retrieved.setStatus(Objects.nonNull(retrievedVersion) ? retrievedVersion.getStatus().name()
          : null);
    }
    return retrieved;
  }

  @Override
  public void update(ActivitySpecEntity activitySpec) {

    ActivitySpecEntity previousActivitySpec = get(activitySpec);

    if (!activitySpec.getName().equals(previousActivitySpec.getName())) {
      uniqueValueUtil.validateUniqueValue(ACTIVITY_SPEC_NAME, activitySpec.getName());
    }

    activitySpecDao.update(activitySpec);

    if (!activitySpec.getName().equals(previousActivitySpec.getName())) {
      uniqueValueUtil.createUniqueValue(ACTIVITY_SPEC_NAME, activitySpec.getName());
      // TODO: Use correct API
      //itemManager.updateName(activitySpec.getId(), activitySpec.getName());
      uniqueValueUtil.deleteUniqueValue(ACTIVITY_SPEC_NAME, previousActivitySpec.getName());
    }
  }

  @Override
  public void actOnAction(String activitySpecId, String versionId, ActivitySpecAction action) {
    Version version = new Version(versionId);
    version = calculateLatestVersion(activitySpecId, version);
    if (action == CERTIFY) {
      version.setStatus(Certified);
    }
    if (action == DEPRECATE) {
      version.setStatus(Deprecated);
    }
    if (action == DELETE) {
      version.setStatus(Deleted);
    }

    updateVersionStatus(activitySpecId, action, version);
    if (action == DELETE) {
      ActivitySpecEntity entity = new ActivitySpecEntity();
      entity.setId(activitySpecId);
      entity.setVersion(version);
      final ActivitySpecEntity activitySpecEntity = get(entity);
      uniqueValueUtil.deleteUniqueValue(ACTIVITY_SPEC_NAME, activitySpecEntity.getName());
    }
  }

  private void updateVersionStatus(String activitySpecId, ActivitySpecAction action,
      Version version) {
    VersionStatus prevVersionStatus = null;
    Version retrievedVersion = null;
    try {
      retrievedVersion = versioningManager.get(activitySpecId, version);
    } catch (SdcRuntimeException exception) {
      LOGGER.error("Failed to get version for activitySpecId: " + activitySpecId + " and version: " + version.getId(),
          exception);
      validateActivitySpecExistence(activitySpecId, version);

    }

    VersionStatus status = version.getStatus();
    Transition transition = TRANSITIONS.get(status);
    if (transition != null) {
      String errMsg = String.format("%s ActivitySpec With Id %s failed since it is not in %s status",
          transition.action, activitySpecId, transition.prevStatus);
      validateStatus(Objects.nonNull(retrievedVersion) ? retrievedVersion.getStatus() : null,
          transition.prevStatus, errMsg);
      prevVersionStatus = transition.prevStatus;
    }

    if (Objects.nonNull(retrievedVersion)) {
      retrievedVersion.setStatus(status);
      versioningManager.updateVersion(activitySpecId, retrievedVersion);
      itemManager.updateVersionStatus(activitySpecId, status, prevVersionStatus);
      versioningManager.publish(activitySpecId, retrievedVersion, "actionOnActivitySpec :"
          + action.name());
    }
  }

  private void validateActivitySpecExistence(String activitySpecId, Version version) {
    throw new CoreException(new ErrorCode.ErrorCodeBuilder()
        .withCategory(ErrorCategory.APPLICATION)
        .withId("ACTIVITYSPEC_NOT_FOUND")
        .withMessage(String.format("Activity Spec With Id %s and version %s not found",
            activitySpecId, version.getId()))
        .build());
  }

  private void validateStatus(VersionStatus retrievedVersionStatus,
      VersionStatus expectedVersionStatus, String errorMessage) {
    if (retrievedVersionStatus != expectedVersionStatus) {
      throw new CoreException(new ErrorCode.ErrorCodeBuilder()
          .withCategory(ErrorCategory.APPLICATION)
          .withId("STATUS_NOT_" + expectedVersionStatus.name().toUpperCase())
          .withMessage(errorMessage).build());
    }
  }

  private Version calculateLatestVersion(String activitySpecId, Version version) {
    if (ActivitySpecConstant.VERSION_ID_DEFAULT_VALUE.equalsIgnoreCase(version.getId())) {
      List<Version> list = null;
      try {
        list = versioningManager.list(activitySpecId);
      } catch (SdcRuntimeException runtimeException) {
        LOGGER.error("Failed to list versions for activitySpecId " + activitySpecId, runtimeException);
        validateActivitySpecExistence(activitySpecId, version);
      }
      if (Objects.nonNull(list) && !list.isEmpty()) {
        return list.get(0);
      }
    }
    return version;
  }

  @Override
  public Collection<Item> list(String versionStatus) {
    Predicate<Item> itemPredicate;
    if (Objects.nonNull(versionStatus)) {

      VersionStatus statusEnumValue;

      try {
        statusEnumValue = VersionStatus.valueOf(versionStatus);
      } catch (IllegalArgumentException e) {
        LOGGER.debug("Unexpected value of VersionStatus {}", versionStatus);
        return Collections.emptyList();
      }

      itemPredicate = item -> ItemType.ACTIVITYSPEC.name().equals(item.getType())
          && item.getVersionStatusCounters().containsKey(statusEnumValue);

    } else {
      itemPredicate = item -> ItemType.ACTIVITYSPEC.name().equals(item.getType());
    }

    return itemManager.list(itemPredicate);
  }

  private void enrichActivitySpec(Item item, Version version,
                                  ActivitySpecEntity activitySpecEntity) {
    activitySpecEntity.setId(item.getId());
    activitySpecEntity.setVersion(version);
  }

  private Item getActivitySpecItem(ActivitySpecEntity activitySpecEntity) {
    Item item = new Item();
    item.setType(ItemType.ACTIVITYSPEC.name());
    item.setName(activitySpecEntity.getName());
    if (activitySpecEntity.getId() != null) {
      item.setId(activitySpecEntity.getId());
    }
    item.addProperty(ActivitySpecConstant.CATEGORY_ATTRIBUTE_NAME,
        activitySpecEntity.getCategoryList());
    return item;
  }

  private static class Transition {

    private final String action;
    private final VersionStatus prevStatus;

    private Transition(String action, VersionStatus prevStatus) {
      this.action = action;
      this.prevStatus = prevStatus;
    }
  }

}
