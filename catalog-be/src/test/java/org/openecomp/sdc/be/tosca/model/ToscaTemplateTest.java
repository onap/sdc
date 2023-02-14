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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ToscaTemplateTest {

    private ToscaTemplate createTestSubject() {
        return new ToscaTemplate("");
    }

    @Test
    void testGetNode_types() throws Exception {
        ToscaTemplate testSubject;
        Map<String, ToscaNodeType> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getNode_types();
    }

    @Test
    void testSetNode_types() throws Exception {
        ToscaTemplate testSubject;
        Map<String, ToscaNodeType> node_types = null;

        // default test
        testSubject = createTestSubject();
        testSubject.setNode_types(node_types);
    }

    @Test
    void testGetImports() throws Exception {
        ToscaTemplate testSubject;
        List<Map<String, Map<String, String>>> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getImports();
    }

    @Test
    void testSetImports() throws Exception {
        ToscaTemplate testSubject;
        List<Map<String, Map<String, String>>> imports = null;

        // default test
        testSubject = createTestSubject();
        testSubject.setImports(imports);
    }

    @Test
    void testGetTosca_definitions_version() throws Exception {
        ToscaTemplate testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getTosca_definitions_version();
    }

    @Test
    void testSetTosca_definitions_version() throws Exception {
        ToscaTemplate testSubject;
        String tosca_definitions_version = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setTosca_definitions_version(tosca_definitions_version);
    }

    @Test
    void testGetMetadata() throws Exception {
        ToscaTemplate testSubject;
        Map<String, Object> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getMetadata();
    }

    @Test
    void testSetMetadata() throws Exception {
        ToscaTemplate testSubject;
        Map<String, Object> metadata = null;

        // default test
        testSubject = createTestSubject();
        testSubject.setMetadata(metadata);
    }

    @Test
    void testGetTopology_template() throws Exception {
        ToscaTemplate testSubject;
        ToscaTopolgyTemplate result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getTopology_template();
    }

    @Test
    void testSetTopology_template() throws Exception {
        ToscaTemplate testSubject;
        ToscaTopolgyTemplate topology_template = null;

        // default test
        testSubject = createTestSubject();
        testSubject.setTopology_template(topology_template);
    }

    @Test
    void testGetSetData_types() throws Exception {
        ToscaTemplate testSubject = createTestSubject();
        Map<String, ToscaDataType> dataTypes = new HashMap<>();
        dataTypes.put("datatype", new ToscaDataType());
        testSubject.setData_types(dataTypes);
        assertEquals(dataTypes, testSubject.getData_types());
    }

    @Test
    void testGetSetInterface_types() throws Exception {
        ToscaTemplate testSubject = createTestSubject();
        Map<String, Object> interfaceTypes = new HashMap<>();
        interfaceTypes.put("id", new Object());
        testSubject.setInterface_types(interfaceTypes);
        assertEquals(interfaceTypes, testSubject.getInterface_types());
    }

}
