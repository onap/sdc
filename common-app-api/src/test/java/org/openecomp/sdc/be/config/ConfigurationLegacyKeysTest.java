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
package org.openecomp.sdc.be.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.common.util.YamlToObjectConverter;

/**
 * The resource is configuration.yaml as OOM renders it for sdc-be. Keep its DMaaP/UEB keys: they are what this test pins.
 */
class ConfigurationLegacyKeysTest {

    @Test
    void oomRenderedConfigWithDmaapKeysStillLoads() throws Exception {
        Configuration configuration = new YamlToObjectConverter().convert(
            "src/test/resources/config/legacy", Configuration.class, "configuration.yaml");
        assertNotNull(configuration);
        assertNotNull(configuration.getBeFqdn());
    }
}
