/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.tosca.model;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ToscaNodeTemplateTest {

    private ToscaNodeTemplate createTestSubject() {
        return new ToscaNodeTemplate();
    }

    @Test
    void testGetType() throws Exception {
        ToscaNodeTemplate testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getType();
    }

    @Test
    void testSetType() throws Exception {
        ToscaNodeTemplate testSubject;
        String type = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setType(type);
    }

    @Test
    void testGetProperties() throws Exception {
        ToscaNodeTemplate testSubject;
        Map<String, Object> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getProperties();
    }

    @Test
    void testSetProperties() throws Exception {
        ToscaNodeTemplate testSubject;
        Map<String, Object> properties = null;

        // default test
        testSubject = createTestSubject();
        testSubject.setProperties(properties);
    }

    @Test
    void testGetRequirements() throws Exception {
        ToscaNodeTemplate testSubject;
        List<Map<String, ToscaTemplateRequirement>> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getRequirements();
    }

    @Test
    void testSetRequirements() throws Exception {
        ToscaNodeTemplate testSubject;
        List<Map<String, ToscaTemplateRequirement>> requirements = null;

        // default test
        testSubject = createTestSubject();
        testSubject.setRequirements(requirements);
    }

    @Test
    void testGetCapabilities() throws Exception {
        ToscaNodeTemplate testSubject;
        Map<String, ToscaTemplateCapability> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getCapabilities();
    }

    @Test
    void testSetCapabilities() throws Exception {
        ToscaNodeTemplate testSubject;
        Map<String, ToscaTemplateCapability> capabilities = null;

        // default test
        testSubject = createTestSubject();
        testSubject.setCapabilities(capabilities);
    }

    @Test
    void testGetMetadata() throws Exception {
        ToscaNodeTemplate testSubject;
        Map<String, Object> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getMetadata();
    }

    @Test
    void testSetMetadata() throws Exception {
        ToscaNodeTemplate testSubject;
        Map<String, Object> metadata = null;

        // default test
        testSubject = createTestSubject();
        testSubject.setMetadata(metadata);
    }
}
