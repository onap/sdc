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

import java.util.Set;

class FileExtensionUtils {

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
    private static final Set<String> VALID_YAML_EXTENSIONS = Set.of(
        ".yaml",
        ".yml",
        ".env"
    );

    static boolean isYaml(String fileName) {
        return isValidExt(fileName, VALID_YAML_EXTENSIONS);
    }

    static boolean isPmDictionary(String fileName) {
        return isValidExt(fileName, VALID_PM_DICTIONARY_EXTENSIONS);
    }

    private static boolean isValidExt(String fileName, Set<String> validExtensions) {
        String fileNameLower = fileName.toLowerCase();
        return validExtensions.stream()
            .anyMatch(fileNameLower::endsWith);
    }
}
