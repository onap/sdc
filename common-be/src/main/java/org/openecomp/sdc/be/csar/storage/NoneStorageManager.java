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
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NoneStorageManager implements ArtifactStorageManager {

    @Override
    public ArtifactInfo persist(final String vspId, final String versionId, final ArtifactInfo uploadedArtifactInfo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArtifactInfo upload(final String vspId, final String versionId, final InputStream fileToUpload) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(final String vspId, final String versionId, final InputStream fileToUpload) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArtifactStorageConfig getStorageConfiguration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream get(final ArtifactInfo artifactInfo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream get(final String vspId, final String versionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(final ArtifactInfo artifactInfo) {
        throw new UnsupportedOperationException();
    }

}
