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

package org.openecomp.sdc.common.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openecomp.sdc.be.config.Configuration.MultitenancyConfig;

class MultitenancyConfigLoaderTest {

    @TempDir
    Path dir;

    private String write(String yaml) throws IOException {
        Path file = dir.resolve("configuration.yaml");
        Files.write(file, yaml.getBytes(StandardCharsets.UTF_8));
        return file.toString();
    }

    @Test
    void readsTheMultitenancySection() throws IOException {
        MultitenancyConfig config = MultitenancyConfigLoader.fromFile(write(
            "basicAuth:\n  enabled: false\nmultitenancy:\n  enabled: true\n  issuer: https://kc/realms/sdc\n  audience: sdc-backend\n"));
        assertTrue(config.isEnabled());
        assertEquals("https://kc/realms/sdc", config.getIssuer());
        assertEquals("sdc-backend", config.getAudience());
    }

    @Test
    void missingSectionMeansDisabled() throws IOException {
        assertFalse(MultitenancyConfigLoader.fromFile(write("basicAuth:\n  enabled: false\n")).isEnabled());
    }

    @Test
    void missingFileMeansDisabled() {
        MultitenancyConfig config = MultitenancyConfigLoader.fromFile(dir.resolve("absent.yaml").toString());
        assertFalse(config.isEnabled());
        assertNull(config.getIssuer());
    }

    @Test
    void emptyFileMeansDisabled() throws IOException {
        assertFalse(MultitenancyConfigLoader.fromFile(write("")).isEnabled());
    }

    @Test
    void malformedSectionFailsClosed() throws IOException {
        String file = write("multitenancy:\n  enabled: [not, a, boolean]\n");
        assertThrows(IllegalStateException.class, () -> MultitenancyConfigLoader.fromFile(file));
    }

    @Test
    void unsetPropertyMeansDisabled() {
        String previous = System.getProperty("configuration.yaml");
        System.clearProperty("configuration.yaml");
        try {
            assertFalse(MultitenancyConfigLoader.fromConfigurationFileProperty().isEnabled());
        } finally {
            if (previous != null) {
                System.setProperty("configuration.yaml", previous);
            }
        }
    }

    @Test
    void propertyIsReadOncePerPath() throws IOException {
        String file = write("multitenancy:\n  enabled: true\n  issuer: https://kc/realms/one\n");
        String previous = System.getProperty("configuration.yaml");
        System.setProperty("configuration.yaml", file);
        try {
            MultitenancyConfig first = MultitenancyConfigLoader.fromConfigurationFileProperty();
            write("multitenancy:\n  enabled: false\n");
            assertTrue(MultitenancyConfigLoader.fromConfigurationFileProperty() == first);
        } finally {
            if (previous == null) {
                System.clearProperty("configuration.yaml");
            } else {
                System.setProperty("configuration.yaml", previous);
            }
        }
    }
}
