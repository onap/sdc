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
 *
 */

package org.onap.sdc.tosca.services;

import org.junit.Assert;
import org.junit.Test;
import org.onap.sdc.tosca.datatypes.model.CapabilityFilter;
import org.onap.sdc.tosca.datatypes.model.Constraint;
import java.util.*;

public class DataModelNormalizeUtilTest {

    @Test
    public void getNormalizeCapabilitiesFilterTest() {
        //given
        List<Map<String, CapabilityFilter>> capabilities = new ArrayList<>();
        List<Map<String, CapabilityFilter>> returnedCapabilities;
        //when
        returnedCapabilities = DataModelNormalizeUtil.getNormalizeCapabilitiesFilter(capabilities);
        //then
        Assert.assertEquals(returnedCapabilities, capabilities);

        //given
        Map<String, CapabilityFilter> outerMap = new HashMap<>();
        Map<String, List<Constraint>> innerMap = new HashMap<>();
        CapabilityFilter filterProperties = new CapabilityFilter();
        Constraint constraint = new Constraint();
        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put("key", "siteName" + UUID.randomUUID().getMostSignificantBits());
        constraint.setEqual(propertyMap);
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(constraint);
        innerMap.put("queue_model", constraints);
        List<Map<String, List<Constraint>>> innerList = new ArrayList<>();
        innerList.add(innerMap);
        filterProperties.setProperties(innerList);
        outerMap.put("diffserv", filterProperties);
        capabilities.add(outerMap);
        //when
        returnedCapabilities = DataModelNormalizeUtil.getNormalizeCapabilitiesFilter(capabilities);
        //then
        Assert.assertNotEquals(capabilities, returnedCapabilities);
    }

    @Test
    public void getNormalizePropertiesFilterTest() {
        //given
        List<Map<String, List<Constraint>>> properties = new ArrayList<>();
        List<Map<String, List<Constraint>>> returnedProperties;
        //when
        returnedProperties = DataModelNormalizeUtil.getNormalizePropertiesFilter(properties);
        //then
        Assert.assertEquals(returnedProperties, properties);

        //given
        Map<String, List<Constraint>> innerMap = new HashMap<>();
        Constraint constraint = new Constraint();
        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put("key", "siteName" + UUID.randomUUID().getMostSignificantBits());
        constraint.setEqual(propertyMap);
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(constraint);
        innerMap.put("queue_model", constraints);
        properties.add(innerMap);
        //when
        returnedProperties = DataModelNormalizeUtil.getNormalizePropertiesFilter(properties);
        //then
        Assert.assertNotEquals(properties, returnedProperties);
    }
}
