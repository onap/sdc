/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
 */
package org.openecomp.sdc.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommonConfigurationManagerTest {

    @TempDir
    Path tempDir;

    private String previousConfigurationFile;

    @BeforeEach
    void writeConfiguration() throws IOException {
        final Path configurationFile = tempDir.resolve("onboarding_configuration.yaml");
        Files.writeString(configurationFile, String.join("\n",
            "zipValidation:",
            "  ignoreManifest: true",
            "permittedAncestors: https://portal.example.com",
            "dataValidatorFilterExcludedUrls: \"/healthCheck,/followed\"",
            "emptyValue:",
            ""));
        previousConfigurationFile = System.getProperty(CommonConfigurationManager.JVM_PARAM_CONFIGURATION_FILE);
        System.setProperty(CommonConfigurationManager.JVM_PARAM_CONFIGURATION_FILE, configurationFile.toString());
    }

    @AfterEach
    void restoreConfigurationProperty() {
        if (previousConfigurationFile == null) {
            System.clearProperty(CommonConfigurationManager.JVM_PARAM_CONFIGURATION_FILE);
        } else {
            System.setProperty(CommonConfigurationManager.JVM_PARAM_CONFIGURATION_FILE, previousConfigurationFile);
        }
    }

    @Test
    void withoutSectionReadsTopLevelValues() {
        final CommonConfigurationManager manager = new CommonConfigurationManager();

        assertEquals("https://portal.example.com", manager.getConfigValue("permittedAncestors", ""));
        assertEquals("/healthCheck,/followed", manager.getConfigValue("dataValidatorFilterExcludedUrls", ""));
    }

    @Test
    void withoutSectionFallsBackToDefaultForMissingOrEmptyValues() {
        final CommonConfigurationManager manager = new CommonConfigurationManager();

        assertEquals("fallback", manager.getConfigValue("notConfigured", "fallback"));
        assertEquals("fallback", manager.getConfigValue("emptyValue", "fallback"));
    }

    @Test
    void withSectionReadsValuesInsideIt() {
        final CommonConfigurationManager manager = new CommonConfigurationManager("zipValidation");

        assertEquals(true, manager.getConfigValue("ignoreManifest", false));
        assertEquals(true, manager.getConfigValue("zipValidation", "ignoreManifest", false));
    }
}
