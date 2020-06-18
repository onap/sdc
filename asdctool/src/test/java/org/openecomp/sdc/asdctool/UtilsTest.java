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

package org.openecomp.sdc.asdctool;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UtilsTest {

    @Test
    public void testBuildOkResponse() {
        int status = 0;
        Response result;

        Response responseToNulls = Utils.buildOkResponse(5, null, null);
        assertNotNull(responseToNulls);
        assertEquals(5, responseToNulls.getStatus());
        assertFalse(responseToNulls.hasEntity());

        // test with mock headers
        Map<String, String> mockAdditionalHeaders = new HashMap<>();
        mockAdditionalHeaders.put("stam", "stam");
        result = Utils.buildOkResponse(status, "entity", mockAdditionalHeaders);
        assertNotNull(result);
        assertEquals(0, result.getStatus());
        assertTrue(result.hasEntity());
    }

    @Test
    public void testOpenGraph() {
        assertNull(Utils.openGraph(null));
    }

    @Test
    public void testVertexLeftContainsRightProps() {
        assertTrue(Utils.vertexLeftContainsRightProps(new HashMap<>(), null));
        assertTrue(Utils.vertexLeftContainsRightProps(null, null));
        assertTrue(Utils.vertexLeftContainsRightProps(null, new HashMap<>()));

        // test 2 with mocks
        Map<String, Object> mockLeftProps = new HashMap<>();
        mockLeftProps.put("stam", new Object());
        Map<String, Object> mockRightProps = new HashMap<>();
        mockRightProps.put("stam", new Object());
        assertFalse(Utils.vertexLeftContainsRightProps(mockLeftProps, mockRightProps));

        // test 3 with mocks
        Object mockObject = new Object();
        mockLeftProps.put("stam", mockObject);
        mockRightProps.put("stam", mockObject);
        assertTrue(Utils.vertexLeftContainsRightProps(mockLeftProps, mockRightProps));
        mockLeftProps.put("stam", null);
        assertFalse(Utils.vertexLeftContainsRightProps(mockLeftProps, mockRightProps));
        mockLeftProps.put("stam", "Woops");
        assertFalse(Utils.vertexLeftContainsRightProps(mockLeftProps, mockRightProps));
    }

    @Test
    public void testSetProperties() {
        assertDoesNotThrow(() -> Utils.setProperties(null, null));
        assertDoesNotThrow(() -> Utils.setProperties(Mockito.mock(Element.class), null));
        Map<String, Object> properties = new HashMap<>();
        properties.put("stam", new Object());
        assertThrows(IllegalArgumentException.class, () -> Utils.setProperties(null, properties));
    }

    @Test
    public void testGetProperties() {
        assertThrows(NullPointerException.class, () -> Utils.getProperties(null));
    }
}
