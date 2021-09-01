/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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

package org.openecomp.sdc.be.tosca;

import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

/**
 * Helper class for TOSCA default imports.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ToscaDefaultImportHelper {

    /**
     * Add the model as a file prefix in the given path, e.g.: "path/to/entry.yaml -> path/to/modelId-entry.yaml".
     *
     * @param originalPath the entry original path
     * @param modelId      the model id to add as prefix
     * @return the modified file path with a model prefix.
     */
    public static Path addModelAsFilePrefix(final Path originalPath, final String modelId) {
        if (StringUtils.isEmpty(modelId)) {
            return originalPath;
        }
        final var fileName = originalPath.getFileName().toString();
        final var newFileName = String.format("%s-%s", modelId, fileName);
        if (originalPath.getParent() == null) {
            return Path.of(newFileName);
        }
        return originalPath.getParent().resolve(newFileName);
    }

}
