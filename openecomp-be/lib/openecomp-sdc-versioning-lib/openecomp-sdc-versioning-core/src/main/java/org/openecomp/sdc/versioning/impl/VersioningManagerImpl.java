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

import static org.openecomp.sdc.versioning.dao.types.VersionStatus.Certified;
import static org.openecomp.sdc.versioning.dao.types.VersionStatus.Draft;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.versioning.ItemManager;
import org.openecomp.sdc.versioning.VersionCalculator;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.VersionDao;
import org.openecomp.sdc.versioning.dao.types.Revision;
import org.openecomp.sdc.versioning.dao.types.SynchronizationState;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionCreationMethod;

public class VersioningManagerImpl implements VersioningManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersioningManagerImpl.class);
    private VersionDao versionDao;
    private VersionCalculator versionCalculator;
    private ItemManager itemManager;

    public VersioningManagerImpl(VersionDao versionDao, VersionCalculator versionCalculator, ItemManager itemManager) {
        this.versionDao = versionDao;
        this.versionCalculator = versionCalculator;
        this.itemManager = itemManager;
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
        if (version.getStatus() == Certified && (version.getState().getSynchronizationState() == SynchronizationState.OutOfSync || version.getState()
            .isDirty())) {
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
        itemManager.updateVersionStatus(itemId, Draft, null);
        publish(itemId, version, String.format("Create version: %s", version.getName()));
        return version;
    }

    private void validateVersionName(String itemId, String versionName) {
        if (versionDao.list(itemId).stream().anyMatch(version -> versionName.equals(version.getName()))) {
            String errorDescription = String.format("Item %s: create version failed, a version with the name %s already exist", itemId, versionName);
            throw new CoreException(new ErrorCode.ErrorCodeBuilder().withMessage(errorDescription).build());
        }
    }

    @Override
    public void submit(String itemId, Version version, String submitDescription) {
        version = get(itemId, version);
        validateSubmit(itemId, version);
        version.setStatus(Certified);
        versionDao.update(itemId, version);
        publish(itemId, version, submitDescription);
        itemManager.updateVersionStatus(itemId, Certified, Draft);
    }

    private void validateSubmit(String itemId, Version version) {
        if (version.getStatus() == Certified) {
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

    @Override
    public void updateVersion(String itemId, Version version) {
        versionDao.update(itemId, version);
    }

    @Override
    public void clean(String itemId, Version version) {
        versionDao.clean(itemId, version);
    }
}
