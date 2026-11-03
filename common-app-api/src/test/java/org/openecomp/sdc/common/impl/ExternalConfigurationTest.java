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

package org.openecomp.sdc.common.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.common.api.ConfigurationSource;

class ExternalConfigurationTest {

    @AfterEach
    void stopWatching() {
        ExternalConfiguration.stopListenForFileChanges();
    }

    @Test
    void loadReadsTheApplicationDirectoryUnderConfigHome(@TempDir Path configHome) throws IOException {
        Files.createDirectory(configHome.resolve("catalog-be"));
        Files.writeString(configHome.resolve("catalog-be").resolve("configuration.yaml"), "beFqdn: probe.example\n");

        ConfigurationSource source = ExternalConfiguration.load("catalog-be", "1.2.3", configHome.toString());

        assertSame(source, ExternalConfiguration.getConfigurationSource());
        assertEquals("catalog-be", ExternalConfiguration.getAppName());
        assertEquals("1.2.3", ExternalConfiguration.getAppVersion());
        assertEquals(configHome.toString(), ExternalConfiguration.getConfigDir());
        assertEquals("probe.example", source.getAndWatchConfiguration(Configuration.class, null).getBeFqdn());
    }
}
