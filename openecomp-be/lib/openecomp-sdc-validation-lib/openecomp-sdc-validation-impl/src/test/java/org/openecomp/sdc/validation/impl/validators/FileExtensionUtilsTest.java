/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 Nokia Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.validation.impl.validators;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class FileExtensionUtilsTest {

    private static final Set<String> VALID_YAML_EXTENSIONS = Set.of(
        ".yaml",
        ".yml",
        ".env"
    );
    private static final Set<String> INVALID_YAML_EXTENSIONS = Set.of(
        ".txt",
        ".java",
        ".properties"
    );

    private static final Set<String> VALID_PM_DICTIONARY_EXTENSIONS = Set.of(
        "pmdict.yml",
        "pmdict.yaml",
        "pm_dict.yml",
        "pm_dict.yaml",
        "pmdictionary.yml",
        "pmdictionary.yaml",
        "pm_dictionary.yml",
        "pm_dictionary.yaml"
    );
    private static final Set<String> INVALID_PM_DICTIONARY_EXTENSIONS = Set.of(
        "pmdict.txt",
        "pmdict.java",
        "pm.yml"
    );

    private static final Set<String> TEST_FILE_PREFIXES = Set.of(
        "test",
        "test_file"
    );

    @Test
    void shouldMatchProperYamlExtensions() {
        final boolean allValidFilesMatched = constructTestFilenamesWithExtensions(VALID_YAML_EXTENSIONS)
            .allMatch(FileExtensionUtils::isYaml);

        assertTrue(allValidFilesMatched);
    }

    @Test
    void shouldNotMatchImproperYamlExtensions() {
        final boolean allInvalidFilesNotMatched = constructTestFilenamesWithExtensions(INVALID_YAML_EXTENSIONS)
            .noneMatch(FileExtensionUtils::isYaml);

        assertTrue(allInvalidFilesNotMatched);
    }

    @Test
    void shouldMatchProperPmDictionaryExtensions() {
        final boolean allValidFilesMatched = constructTestFilenamesWithExtensions(VALID_PM_DICTIONARY_EXTENSIONS)
            .allMatch(FileExtensionUtils::isPmDictionary);

        assertTrue(allValidFilesMatched);
    }

    @Test
    void shouldNotMatchImproperPmDictionaryExtensions() {
        final boolean allInvalidFilesNotMatched = constructTestFilenamesWithExtensions(INVALID_PM_DICTIONARY_EXTENSIONS)
            .noneMatch(FileExtensionUtils::isPmDictionary);

        assertTrue(allInvalidFilesNotMatched);
    }

    private Stream<String> constructTestFilenamesWithExtensions(Set<String> extensions) {
        return extensions.stream()
            .flatMap(ext -> prepareFilenamesWithExtension(ext).stream());
    }

    private Set<String> prepareFilenamesWithExtension(String extension) {
        return Stream.concat(
            joinTestNamesWithExtension(extension.toLowerCase()),
            joinTestNamesWithExtension(extension.toUpperCase())
        ).collect(Collectors.toSet());
    }

    private Stream<String> joinTestNamesWithExtension(String extension) {
        return TEST_FILE_PREFIXES.stream()
            .map(prefix -> prefix + extension);
    }
}