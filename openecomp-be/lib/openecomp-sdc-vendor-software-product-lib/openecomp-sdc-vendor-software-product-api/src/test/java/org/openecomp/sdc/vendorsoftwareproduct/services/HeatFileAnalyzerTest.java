/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.vendorsoftwareproduct.services;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class HeatFileAnalyzerTest {

    @Test
    public void testIsEnvFile() {
        assertTrue(HeatFileAnalyzer.isEnvFile("test.env"));
        assertFalse(HeatFileAnalyzer.isEnvFile("test.txt"));
    }

    @Test
    public void testIsYamlFile() {
        assertTrue(HeatFileAnalyzer.isYamlFile("test.yml"));
        assertTrue(HeatFileAnalyzer.isYamlFile("test.yaml"));
        assertFalse(HeatFileAnalyzer.isYamlFile("test.txt"));
    }

    @Test
    public void testIsYamlOrEnvFile() {
        assertTrue(HeatFileAnalyzer.isYamlOrEnvFile("test.env"));
        assertTrue(HeatFileAnalyzer.isYamlOrEnvFile("test.yml"));
        assertTrue(HeatFileAnalyzer.isYamlOrEnvFile("test.yaml"));
        assertFalse(HeatFileAnalyzer.isYamlOrEnvFile("test.txt"));
    }
}
