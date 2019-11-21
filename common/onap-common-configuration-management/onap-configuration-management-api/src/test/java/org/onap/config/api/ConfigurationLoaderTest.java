/*-
 * ============LICENSE_START=======================================================
 * ONAP SDC
 * ================================================================================
 * Copyright (C) 2019 Samsung. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 * ===================================================================
 */


package org.onap.config.api;

import org.junit.Test;
import org.onap.config.api.impl.ConfigurationManagerTestImpl;

import static org.junit.Assert.assertTrue;

public class ConfigurationLoaderTest {

    @Test
    public void loadServiceProvider() {
        Configuration config = ConfigurationLoader.load();
        assertTrue(config instanceof ConfigurationManagerTestImpl);
    }
}