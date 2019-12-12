/*
 * Copyright (C) 2019 Samsung. All rights reserved.
 *
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
 */

package org.onap.config.test;

import org.junit.Test;
import org.onap.config.type.ConfigurationQuery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConfigurationQueryTest {
    private static String TENANT = "OPENECOMP";
    private static String NAMESPACE = "tetsNamepspace";
    private static String KEY = "testKey";

    @Test
    public void testConfigurationQueryBuild() {
        // given
        ConfigurationQuery configurationQuery = new ConfigurationQuery();

        // when
        configurationQuery = configurationQuery
                .externalLookup(true)
                .fallback(true)
                .latest(true)
                .nodeSpecific(true)
                .namespace(NAMESPACE)
                .tenant(TENANT)
                .key(KEY);

        // then
        assertEquals(TENANT.toUpperCase(), configurationQuery.getTenant());
        assertEquals(NAMESPACE.toUpperCase(), configurationQuery.getNamespace());
        assertEquals(KEY, configurationQuery.getKey());
        assertTrue(configurationQuery.isExternalLookup());
        assertTrue(configurationQuery.isFallback());
        assertTrue(configurationQuery.isLatest());
        assertTrue(configurationQuery.isNodeSpecific());
    }
}
