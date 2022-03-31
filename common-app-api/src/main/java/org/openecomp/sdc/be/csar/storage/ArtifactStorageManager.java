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

import java.io.InputStream;

/**
 * Manages the artifact storage and handles operations on the artifacts
 */
public interface ArtifactStorageManager {

    /**
     * Persists the uploaded artifact in the storage.
     *
     * @param vspId                the VSP id
     * @param versionId            the VSP version id
     * @param uploadedArtifactInfo the uploaded
     * @return the information about the persisted artifact
     */
    ArtifactInfo persist(String vspId, String versionId, ArtifactInfo uploadedArtifactInfo);

    /**
     * Uploads a file to the Artifact Storage. This file will be temporary until persisted by {@link #persist(String, String, ArtifactInfo)}.
     *
     * @param vspId        the VSP id
     * @param versionId    the VSP version id
     * @param fileToUpload the file input stream
     * @return the information about the uploaded artifact
     */
    ArtifactInfo upload(String vspId, String versionId, InputStream fileToUpload);

    void put(String vspId, String name, InputStream fileToUpload);

    /**
     * Checks if the Artifact Storage is enabled.
     *
     * @return {@code true} if enable, {@code false} otherwise
     */
    default boolean isEnabled() {
        return false;
    }

    /**
     * @return Storage Configuration
     */
    ArtifactStorageConfig getStorageConfiguration();

    InputStream get(final ArtifactInfo artifactInfo);

    InputStream get(final String vspId, final String versionId);

    void delete(ArtifactInfo artifactInfo);

    /**
     * Delete all versions and VSP itself
     *
     * @param vspId - VSP ID
     */
    void delete(String vspId);

    /**
     * Check if VSP exists
     *
     * @param vspId - VSP ID
     * @return {@code true} if exists, {@code false} otherwise
     */
    boolean exists(String vspId);
}
