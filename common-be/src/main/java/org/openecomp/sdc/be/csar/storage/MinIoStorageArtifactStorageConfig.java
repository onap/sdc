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

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MinIoStorageArtifactStorageConfig implements ArtifactStorageConfig {

    private final boolean isEnabled;
    private final EndPoint endPoint;
    private final Credentials credentials;
    private final String tempPath;
    private final int uploadPartSize;

    @AllArgsConstructor
    @Getter
    public static class EndPoint {

        private final String host;
        private final int port;
        private final boolean secure;
    }

    @AllArgsConstructor
    @Getter
    public static class Credentials {

        private final String accessKey;
        private final String secretKey;
    }

}
