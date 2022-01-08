/*
 *
 *  Copyright © 2017-2018 European Support Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 *
 */

package org.openecomp.sdc.heat.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.heat.datatypes.model.ResourceReferenceFunctions;

public class HeatStructureUtilTest {

    @Mock
    private GlobalValidationContext globalValidationContextMock;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testIsResourceNestedFalse() {
        Assert.assertFalse(HeatStructureUtil.isNestedResource("Test.txt"));
    }

    @Test
    public void testIsResourceNestedNull() {
        Assert.assertFalse(HeatStructureUtil.isNestedResource(null));
    }

    @Test
    public void testIsResourceNestedTrue() {
        Assert.assertTrue(HeatStructureUtil.isNestedResource("Test.yml"));
    }

    @Test
    public void testGetReferencedValuesByFunctionNameAddMessageCall() {
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(ResourceReferenceFunctions.GET_RESOURCE.getFunction(), Collections.emptyList());

        Mockito.doNothing().when(globalValidationContextMock).addMessage(Mockito.anyString(), Mockito.any(),
                Mockito.anyString());

        Set<String> valueNames = HeatStructureUtil.getReferencedValuesByFunctionName("Main.yml",
                ResourceReferenceFunctions.GET_RESOURCE.getFunction(), propertyMap, globalValidationContextMock);

        Mockito.verify(globalValidationContextMock, Mockito.times(1))
                .addMessage(Mockito.anyString(), Mockito.any(), Mockito.anyString());
        Assert.assertTrue(valueNames.isEmpty());
    }

    @Test
    public void testGetReferencedValuesByFunctionNameGetFile() {
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(ResourceReferenceFunctions.GET_FILE.getFunction(), "file:///filename");

        Set<String> valueNames = HeatStructureUtil.getReferencedValuesByFunctionName("Main.yml",
                ResourceReferenceFunctions.GET_FILE.getFunction(), propertyMap, globalValidationContextMock);

        Assert.assertFalse(valueNames.isEmpty());
        Assert.assertTrue(valueNames.contains("filename"));
    }

    @Test
    public void testGetReferencedValuesByFunctionNameGetFileValueList() {
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap
                .put(ResourceReferenceFunctions.GET_FILE.getFunction(), Collections.singletonList("file:///filename"));

        Set<String> valueNames = HeatStructureUtil.getReferencedValuesByFunctionName("Main.yml",
                ResourceReferenceFunctions.GET_FILE.getFunction(), propertyMap, globalValidationContextMock);

        Assert.assertFalse(valueNames.isEmpty());
        Assert.assertTrue(valueNames.contains("filename"));
    }

    @Test
    public void testGetReferencedValuesByFunctionNameGetFileValueListWithAnotherList() {
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(ResourceReferenceFunctions.GET_FILE.getFunction(),
                Collections.singletonList(Collections.emptyList()));

        Set<String> valueNames = HeatStructureUtil.getReferencedValuesByFunctionName("Main.yml",
                ResourceReferenceFunctions.GET_FILE.getFunction(), propertyMap, globalValidationContextMock);

        Assert.assertTrue(valueNames.isEmpty());
    }

    @Test
    public void testGetReferencedValuesByFunctionNamePassingPropertyMapWithSet() {
        Set<String> valueNames = HeatStructureUtil.getReferencedValuesByFunctionName("Main.yml",
                ResourceReferenceFunctions.GET_FILE.getFunction(), Collections.singletonList(new HashSet<>()),
                globalValidationContextMock);

        Assert.assertTrue(valueNames.isEmpty());
    }

    @Test
    public void testGetReferencedValuesByFunctionNameGetFileValueListSet() {
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(ResourceReferenceFunctions.GET_FILE.getFunction(),
                new HashSet<>());

        Set<String> valueNames = HeatStructureUtil.getReferencedValuesByFunctionName("Main.yml",
                ResourceReferenceFunctions.GET_FILE.getFunction(), propertyMap, globalValidationContextMock);

        Assert.assertTrue(valueNames.isEmpty());
    }

    @Test
    public void testGetReferencedValuesByFunctionNameIncorrectKeyWithSet() {
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("test", new HashSet<>());

        Set<String> valueNames = HeatStructureUtil.getReferencedValuesByFunctionName("Main.yml",
                ResourceReferenceFunctions.GET_FILE.getFunction(), propertyMap, globalValidationContextMock);

        Assert.assertTrue(valueNames.isEmpty());
    }
}
