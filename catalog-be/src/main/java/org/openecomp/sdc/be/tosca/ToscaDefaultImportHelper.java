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
import java.util.Set;
import org.apache.commons.io.FilenameUtils;

/**
 * Helper class for TOSCA default imports.
 */
public class ToscaDefaultImportHelper {

    private ToscaDefaultImportHelper() {

    }

    /**
     * Build a unique import entry path for TOSCA file generation. If the original entry already exists in the {@code existingEntryList}, it will
     * create a new entry name by concatenating a counter, i.e.: "this/is/an/entry.yaml -> this/is/an/entry1.yaml".
     * <p>
     * If the "this/is/an/entry1.yaml" also exists, it will keep counting until it finds an entry that does not exist, then the new name is created.
     *
     * @param originalPath      the entry original path
     * @param existingEntryList the already existing entries in the TOSCA file/CSAR.
     * @return the original path if it does not exist, or a new path if it already exists.
     */
    public static Path buildImportEntryPath(final Path originalPath, final Set<Path> existingEntryList) {
        Path importPath = originalPath;
        final String fileName = originalPath.getFileName().toString();
        final String baseName = FilenameUtils.getBaseName(fileName);
        final String extension = FilenameUtils.getExtension(fileName);
        var count = 1;
        while (existingEntryList.contains(importPath)) {
            final String newFileName = baseName + count + (extension.isEmpty() ? extension : "." + extension);
            if (importPath.getParent() == null) {
                importPath = Path.of(newFileName);
            } else {
                importPath = importPath.getParent().resolve(newFileName);
            }
            count++;
        }
        return importPath;
    }

}
