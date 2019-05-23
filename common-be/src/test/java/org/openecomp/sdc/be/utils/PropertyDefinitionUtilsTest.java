/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.utils;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

public class PropertyDefinitionUtilsTest {

    private static final String NAME = "NAME";
    private static final String UNIQUE_ID = "UNIQUE_ID";
    private static final String PROP_NAME = "PROP_NAME";
    private static final String FIRST = "FIRST";
    private static final String SECOND = "SECOND";
    private static final String THIRD = "THIRD";

    @Test
    public void testConvertListOfProperties() {
        List<PropertyDataDefinition> inputDataDefinitions = createDataDefinitions();
        List<PropertyDataDefinition> propertyDataDefinitions = PropertyDefinitionUtils
            .convertListOfProperties(inputDataDefinitions);
        assertEquals(propertyDataDefinitions.size(), 1);
        assertEquals(propertyDataDefinitions.get(0).getName(), NAME);
        assertEquals(propertyDataDefinitions.get(0).getUniqueId(), UNIQUE_ID);
        assertNotSame(inputDataDefinitions, propertyDataDefinitions);
        assertEquals(inputDataDefinitions, propertyDataDefinitions);
        assertNotSame(inputDataDefinitions.get(0), propertyDataDefinitions.get(0));
        assertEquals(inputDataDefinitions.get(0), propertyDataDefinitions.get(0));
    }

    @Test
    public void testResolveGetInputPropertiesForEmptyMap() {
        Map<String, List<PropertyDataDefinition>> inputProps = PropertyDefinitionUtils
            .resolveGetInputProperties(null);
        assertEquals(inputProps, emptyMap());
    }

    @Test
    public void testResolveGetInputPropertiesForNotEmptyMap() {
        Map<String, List<PropertyDataDefinition>> props = new HashMap<>();
        props.put(FIRST, createDataDefinitions());
        props.put(SECOND, createDataDefinitions());
        props.put(THIRD, createDataDefinitionsWithEmptyGetInputValueDefinition());
        Map<String, List<PropertyDataDefinition>> result = PropertyDefinitionUtils.resolveGetInputProperties(props);
        assertEquals(result.size(), 3);
        assertEquals(result.get(FIRST).size(), 1);
        assertEquals(result.get(FIRST).get(0).getUniqueId(), UNIQUE_ID);
        assertEquals(result.get(SECOND).size(), 1);
        assertEquals(result.get(THIRD).size(), 0);
    }

    private List<PropertyDataDefinition> createDataDefinitions(){
        ArrayList<PropertyDataDefinition> propertyDataDefinitions = new ArrayList<>();
        PropertyDataDefinition dataDefinition = new PropertyDataDefinition();
        dataDefinition.setUniqueId(UNIQUE_ID);
        dataDefinition.setName(NAME);
        List<GetInputValueDataDefinition> getInputValues = new ArrayList<>();
        GetInputValueDataDefinition getInputValueDataDefinition = new GetInputValueDataDefinition();
        getInputValueDataDefinition.setPropName(PROP_NAME);
        getInputValues.add(getInputValueDataDefinition);
        dataDefinition.setGetInputValues(getInputValues);
        propertyDataDefinitions.add(dataDefinition);
        return propertyDataDefinitions;
    }

    private List<PropertyDataDefinition> createDataDefinitionsWithEmptyGetInputValueDefinition(){
        ArrayList<PropertyDataDefinition> propertyDataDefinitions = new ArrayList<>();
        PropertyDataDefinition dataDefinition = new PropertyDataDefinition();
        dataDefinition.setUniqueId(UNIQUE_ID);
        dataDefinition.setName(NAME);
        propertyDataDefinitions.add(dataDefinition);
        return propertyDataDefinitions;
    }
}