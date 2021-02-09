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

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class CapabilityFilterTest {
    private CapabilityFilter createCapabilityFilter() {
        return new CapabilityFilter();
    }

    private Map<String, List<Object>> createProperty() {
        Map<String, List<Object>> property = new HashMap<>();
        List<Object> mockValue = new ArrayList<>();
        mockValue.add("mock-value-0");
        property.put("mock-key", mockValue);
        return property;
    }

    @Test
    public void testDefaultCtor() {
        assertThat(CapabilityFilter.class, hasValidBeanConstructor());
    }

    @Test
    public void testGettersSetters() {
        assertThat(CapabilityFilter.class, hasValidGettersAndSetters());
    }

    @Test
    public void testAddProperty() {
        CapabilityFilter capabilityFilter = createCapabilityFilter();
        Map<String, List<Object>> property = createProperty();
        capabilityFilter.addProperty(property);
        assertThat(capabilityFilter.getProperties(), hasItem(property));
    }
}
