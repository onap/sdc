/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.csar.storage;

import static org.openecomp.sdc.be.csar.storage.StorageFactory.StorageType.NONE;
import static org.openecomp.sdc.be.csar.storage.StorageFactory.StorageType.findByName;

import java.util.Optional;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.common.CommonConfigurationManager;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

@NoArgsConstructor
public class StorageFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageFactory.class);
    private static final String EXTERNAL_CSAR_STORE = "externalCsarStore";

    public ArtifactStorageManager createArtifactStorageManager() {
        switch (getConfiguredArtifactStorageType()) {
            case MINIO: //  MinIoStorage enabled
                return new MinIoStorageArtifactStorageManager();
            default://  all configured, nothing enabled
                return new NoneStorageManager();
        }
    }

    public Optional<PackageSizeReducer> createPackageSizeReducer() {
        switch (getConfiguredArtifactStorageType()) {
            case MINIO: //  MinIoStorage enabled
                return Optional.of(new MinIoStorageCsarSizeReducer());
            default://  all configured, nothing enabled
                return Optional.empty();
        }
    }

    private StorageType getConfiguredArtifactStorageType() {
        final var commonConfigurationManager = CommonConfigurationManager.getInstance();
        commonConfigurationManager.reload();
        final String storageType = commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, "storageType", NONE.name());
        LOGGER.info("ArtifactConfig.storageType: '{}'", storageType);
        return findByName(storageType);
    }

    public enum StorageType {
        NONE,
        MINIO;

        public static StorageType findByName(String name) {
            for (StorageType curr : StorageType.values()) {
                if (curr.name().equals(name)) {
                    return curr;
                }
            }
            return NONE;
        }
    }
}
