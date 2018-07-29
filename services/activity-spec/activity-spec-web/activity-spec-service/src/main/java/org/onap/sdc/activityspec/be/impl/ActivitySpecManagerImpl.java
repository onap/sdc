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

package org.onap.sdc.activityspec.be.impl;

import org.onap.sdc.activityspec.api.rest.types.ActivitySpecAction;
import org.onap.sdc.activityspec.be.ActivitySpecManager;
import org.onap.sdc.activityspec.be.dao.ActivitySpecDao;
import org.onap.sdc.activityspec.be.dao.types.ActivitySpecEntity;
import org.onap.sdc.activityspec.be.datatypes.ItemType;
import org.onap.sdc.activityspec.errors.ActivitySpecNotFoundException;
import org.onap.sdc.activityspec.utils.ActivitySpecConstant;
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

import java.util.*;
import java.util.function.Predicate;

import static org.openecomp.sdc.versioning.dao.types.VersionStatus.*;

public class ActivitySpecManagerImpl implements ActivitySpecManager {

    private static final Map<VersionStatus, VersionStatus> EXPECTED_PREV_STATUS;

    static {
        EXPECTED_PREV_STATUS = new EnumMap<VersionStatus, VersionStatus>(VersionStatus.class);
        EXPECTED_PREV_STATUS.put(Certified, Draft);
        EXPECTED_PREV_STATUS.put(Deprecated, Certified);
        EXPECTED_PREV_STATUS.put(Deleted, Deprecated);
    }

    private final ItemManager itemManager;
    private final VersioningManager versioningManager;
    private final ActivitySpecDao activitySpecDao;
    private final UniqueValueUtil uniqueValueUtil;
    private static final String ACTIVITY_SPEC_NAME = "ActivitySpec.Name";
    private static final Logger LOGGER = LoggerFactory.getLogger(ActivitySpecManagerImpl.class);

    public ActivitySpecManagerImpl(ItemManager itemManager, VersioningManager versioningManager,
                                          ActivitySpecDao activitySpecDao, UniqueValueDao uniqueValueDao) {
        this.itemManager = itemManager;
        this.versioningManager = versioningManager;
        this.activitySpecDao = activitySpecDao;
        this.uniqueValueUtil = new UniqueValueUtil(uniqueValueDao);
    }

    @Override
    public ActivitySpecEntity createActivitySpec(ActivitySpecEntity activitySpecEntity) {

        validateUniqueValue(activitySpecEntity);

        Item item = getActivitySpecItem(activitySpecEntity);
        item = itemManager.create(item);

        Version version = getActivitySpecVersion(activitySpecEntity);
        versioningManager.create(item.getId(), version, VersionCreationMethod.major);

        enrichActivitySpec(item, version, activitySpecEntity);
        activitySpecDao.create(activitySpecEntity);

        uniqueValueUtil.createUniqueValue(ACTIVITY_SPEC_NAME, activitySpecEntity.getName());
        return activitySpecEntity;
    }

    private void validateUniqueValue(ActivitySpecEntity activitySpecEntity) {
        try {
            uniqueValueUtil.validateUniqueValue(ACTIVITY_SPEC_NAME, activitySpecEntity.getName());
        } catch (CoreException exception) {
            LOGGER.debug("Unique value error for activity spec name :" + activitySpecEntity.getName(), exception);
            throw new CoreException(new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION)
                                                                    .withId(exception.code().id())
                                                                    .withMessage("name already in use").build());
        }
    }

    private Version getActivitySpecVersion(ActivitySpecEntity activitySpecEntity) {
        return activitySpecEntity.getVersion() == null ? new Version() : activitySpecEntity.getVersion();

    }

    @Override
    public ActivitySpecEntity get(ActivitySpecEntity activitySpec) {
        activitySpec.setVersion(calculateLatestVersion(activitySpec.getId(), activitySpec.getVersion()));
        ActivitySpecEntity retrieved;
        try {
            retrieved = activitySpecDao.get(activitySpec);
        } catch (SdcRuntimeException runtimeException) {
            LOGGER.debug(
                    "Failed to retrieve activity spec for activitySpecId: " + activitySpec.getId() + " and version: "
                            + activitySpec.getVersion().getId(), runtimeException);
            throw new ActivitySpecNotFoundException(ActivitySpecConstant.ACTIVITY_SPEC_NOT_FOUND, runtimeException);
        }
        if (retrieved != null) {
            final Version retrievedVersion = versioningManager.get(activitySpec.getId(), activitySpec.getVersion());
            retrieved.setStatus(Objects.nonNull(retrievedVersion) ? retrievedVersion.getStatus().name() : null);
        }
        return retrieved;
    }

    @Override
    public void update(ActivitySpecEntity activitySpec) {

        ActivitySpecEntity previousActivitySpec = get(activitySpec);

        if (!activitySpec.getName().equals(previousActivitySpec.getName())) {
            validateUniqueValue(activitySpec);
        }

        activitySpecDao.update(activitySpec);

        if (!activitySpec.getName().equals(previousActivitySpec.getName())) {
            uniqueValueUtil.createUniqueValue(ACTIVITY_SPEC_NAME, activitySpec.getName());
            itemManager.updateName(activitySpec.getId(), activitySpec.getName());
            uniqueValueUtil.deleteUniqueValue(ACTIVITY_SPEC_NAME, previousActivitySpec.getName());
        }
    }

    @Override
    public void actOnAction(String activitySpecId, String versionId, ActivitySpecAction action) {
        Version version = new Version(versionId);
        version = calculateLatestVersion(activitySpecId, version);
        if (action == ActivitySpecAction.CERTIFY) {
            version.setStatus(Certified);
        }
        if (action == ActivitySpecAction.DEPRECATE) {
            version.setStatus(Deprecated);
        }
        if (action == ActivitySpecAction.DELETE) {
            version.setStatus(Deleted);
        }

        updateVersionStatus(activitySpecId, action, version);
        if (action == ActivitySpecAction.DELETE) {
            ActivitySpecEntity entity = new ActivitySpecEntity();
            entity.setId(activitySpecId);
            entity.setVersion(version);
            final ActivitySpecEntity activitySpecEntity = get(entity);
            uniqueValueUtil.deleteUniqueValue(ACTIVITY_SPEC_NAME, activitySpecEntity.getName());
        }
    }

    private void updateVersionStatus(String activitySpecId, ActivitySpecAction action, Version version) {
        VersionStatus prevVersionStatus = null;
        Version retrievedVersion;
        try {
            retrievedVersion = versioningManager.get(activitySpecId, version);
        } catch (SdcRuntimeException exception) {
            LOGGER.debug(
                    "Failed to get version for activitySpecId: " + activitySpecId + " and version: " + version.getId(),
                    exception);
            throw new ActivitySpecNotFoundException(ActivitySpecConstant.ACTIVITY_SPEC_NOT_FOUND, exception);

        }

        VersionStatus status = version.getStatus();
        VersionStatus expectedPrevStatus = EXPECTED_PREV_STATUS.get(status);
        if (expectedPrevStatus != null) {

            VersionStatus retrievedStatus = Objects.nonNull(retrievedVersion) ? retrievedVersion.getStatus() : null;
            if (retrievedStatus != expectedPrevStatus) {
                LOGGER.debug("Failed to " + version.getStatus() + " since activity spec is in " + retrievedStatus);
                throw new CoreException(new ErrorCode.ErrorCodeBuilder().withMessage(ActivitySpecConstant.INVALID_STATE)
                                                                        .build());
            }
            prevVersionStatus = expectedPrevStatus;
        }

        if (Objects.nonNull(retrievedVersion)) {
            retrievedVersion.setStatus(status);
            versioningManager.updateVersion(activitySpecId, retrievedVersion);
            itemManager.updateVersionStatus(activitySpecId, status, prevVersionStatus);
            versioningManager.publish(activitySpecId, retrievedVersion, "actionOnActivitySpec :" + action.name());
        }
    }

    private Version calculateLatestVersion(String activitySpecId, Version version) {
        if (ActivitySpecConstant.VERSION_ID_DEFAULT_VALUE.equalsIgnoreCase(version.getId())) {
            List<Version> list;
            try {
                list = versioningManager.list(activitySpecId);
            } catch (SdcRuntimeException runtimeException) {
                LOGGER.debug("Failed to list versions for activitySpecId " + activitySpecId, runtimeException);
                throw new ActivitySpecNotFoundException(ActivitySpecConstant.ACTIVITY_SPEC_NOT_FOUND, runtimeException);
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

            itemPredicate =
                    item -> ItemType.ACTIVITYSPEC.name().equals(item.getType()) && item.getVersionStatusCounters()
                                                                                       .containsKey(statusEnumValue);

        } else {
            itemPredicate = item -> ItemType.ACTIVITYSPEC.name().equals(item.getType());
        }

        return itemManager.list(itemPredicate);
    }

    private void enrichActivitySpec(Item item, Version version, ActivitySpecEntity activitySpecEntity) {
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
        item.addProperty(ActivitySpecConstant.CATEGORY_ATTRIBUTE_NAME, activitySpecEntity.getCategoryList());
        return item;
    }
}
