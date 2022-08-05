/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 Samsung Intellectual Property. All rights reserved.
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
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class NodeFilterTest {

    @Test
    public void testHasDataTrue() {
        NodeFilter nodeFilter = new NodeFilter();
        List<Map<String, CapabilityFilter>> capabilitiesCopy = new ArrayList<>();
        Map<String, CapabilityFilter> capabilityDataMap= new HashMap<>();
        CapabilityFilter capabilityFilter = new CapabilityFilter();
        capabilityDataMap.put("test",capabilityFilter);
        capabilitiesCopy.add(capabilityDataMap);
        nodeFilter.setCapabilities(capabilitiesCopy);

        List<Map<String, List<Object>>> propertiesCopy = new ArrayList<>();
        Map<String, List<Object>> propertyDataMap = new HashMap<>();
        List<Object> dataObjectList = new ArrayList<>();
        Object object = new Object();
        dataObjectList.add(object);
        propertyDataMap.put("test",dataObjectList);
        propertiesCopy.add(propertyDataMap);
        nodeFilter.setProperties(propertiesCopy);
        boolean result = nodeFilter.hasData();
        assertTrue(result);
    }

    @Test
    public void testHasDataCapabilityTrue() {
        NodeFilter nodeFilter = new NodeFilter();
        List<Map<String, CapabilityFilter>> capabilitiesCopy = new ArrayList<>();
        Map<String, CapabilityFilter> capabilityDataMap= new HashMap<>();
        CapabilityFilter capabilityFilter = new CapabilityFilter();
        capabilityDataMap.put("test",capabilityFilter);
        capabilitiesCopy.add(capabilityDataMap);
        nodeFilter.setCapabilities(capabilitiesCopy);
        boolean result = nodeFilter.hasData();
        assertTrue(result);
    }

    @Test
    public void testHasDataPropertiesTrue() {
        NodeFilter nodeFilter = new NodeFilter();
        List<Map<String, List<Object>>> propertiesCopy = new ArrayList<>();
        Map<String, List<Object>> propertyDataMap = new HashMap<>();
        List<Object> dataObjectList = new ArrayList<>();
        Object object = new Object();
        dataObjectList.add(object);
        propertyDataMap.put("test",dataObjectList);
        propertiesCopy.add(propertyDataMap);
        nodeFilter.setProperties(propertiesCopy);
        boolean result = nodeFilter.hasData();
        assertTrue(result);
    }

    @Test
    public void testHasDataFalse() {
        NodeFilter nodeFilter = new NodeFilter();
        boolean result = nodeFilter.hasData();
        assertFalse(result);
    }
}