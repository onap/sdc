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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ToscaDefaultImportHelperTest {

    @Test
    void buildImportEntryPath_originalPathNotUnique() {
        final Path originalPath = Path.of("anImport.yaml");
        final Set<Path> existingPath = Set.of(
            Path.of("anImport.yaml"),
            Path.of("anImport1.yaml"),
            Path.of("anImport2.yaml"),
            Path.of("test/anImport2.yaml")
        );
        final Path path = ToscaDefaultImportHelper.buildImportEntryPath(originalPath, existingPath);
        assertEquals(Path.of("anImport3.yaml"), path);
    }

    @Test
    void buildImportEntryPathTest_originalPathAlreadyUnique() {
        final Path originalPath = Path.of("anImport.yaml");
        final Set<Path> existingPath = Collections.emptySet();
        final Path path = ToscaDefaultImportHelper.buildImportEntryPath(originalPath, existingPath);
        assertEquals(Path.of("anImport.yaml"), path);
    }

    @Test
    void buildImportEntryPathTest_originalWithParent() {
        final Path originalPath = Path.of("parentPath/anImport.yaml");
        final Set<Path> existingPath = Set.of(
            Path.of("parentPath/anImport.yaml"),
            Path.of("parentPath/anImport1.yaml")
        );
        final Path path = ToscaDefaultImportHelper.buildImportEntryPath(originalPath, existingPath);
        assertEquals(Path.of("parentPath/anImport2.yaml"), path);
    }

    @Test
    void buildImportEntryPathTest_originalWithoutExtension() {
        final Path originalPath = Path.of("anImport");
        final Set<Path> existingPath = Set.of(
            Path.of("anImport")
        );
        final Path path = ToscaDefaultImportHelper.buildImportEntryPath(originalPath, existingPath);
        assertEquals(Path.of("anImport1"), path);
    }
}