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

/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.core.utilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

public class CommonMethodsTest {

    private static final String[] ARRAY = {"A", "B", "C"};
    private static final String JAVA_LANG_STRING = "java.lang.String";

    @Test
    public void testPrintStackTrace() {

        String trace = CommonMethods.printStackTrace();
        assertTrue(trace.contains("org.openecomp.core.utilities" +
                ".CommonMethods.printStackTrace(CommonMethods.java:"));
        assertTrue(trace.contains("org.openecomp.core.utilities" +
                ".CommonMethodsTest.testPrintStackTrace(CommonMethodsTest.java"));
    }

    @Test
    public void testArrayToCommaSeparatedString() {
        assertEquals(CommonMethods.arrayToCommaSeparatedString(ARRAY), "A,B,C");
    }

    @Test
    public void testArrayToCommaSeparatedStringEmpty() {
        assertEquals(CommonMethods.arrayToCommaSeparatedString(new String[0]), "");
    }

    @Test
    public void testArrayToCommaSeparatedStringNulls() {
        assertEquals(CommonMethods.arrayToCommaSeparatedString(new String[] {null, null}), "null,null");
    }

    @Test
    public void testArrayToCommaSeparatedStringEmptyStrings() {
        assertEquals(CommonMethods.arrayToCommaSeparatedString(new String[] {"", ""}), ",");
    }

    @Test(expected = NullPointerException.class)
    public void testArrayToCommaSeparatedStringNull() {
        CommonMethods.arrayToCommaSeparatedString(null);
    }

    @Test
    public void testArrayToSeparatedString() {
        assertEquals(CommonMethods.arrayToSeparatedString(ARRAY, '/'), "A/B/C");
    }

    @Test
    public void testArrayToSeparatedStringEmpty() {
        assertEquals(CommonMethods.arrayToSeparatedString(new String[0], '/'), "");
    }

    @Test
    public void testArrayToSeparatedStringNulls() {
        assertEquals(CommonMethods.arrayToSeparatedString(new String[] {null, null}, '/'), "null/null");
    }

    @Test
    public void testArrayToSeparatedStringEmptyStrings() {
        assertEquals(CommonMethods.arrayToSeparatedString(new String[] {"", ""}, '/'), "/");
    }

    @Test(expected = NullPointerException.class)
    public void testArrayToSeparatedStringNull() {
        CommonMethods.arrayToSeparatedString(null, '/');
    }

    @Test
    public void testCollectionToCommaSeparatedString() {
        assertEquals(CommonMethods.collectionToCommaSeparatedString(Arrays.asList(ARRAY)), "A,B,C");
    }

    @Test
    public void testCollectionToCommaSeparatedStringNulls() {
        assertEquals(CommonMethods.collectionToCommaSeparatedString(Arrays.asList(null, null)), "null,null");
    }

    @Test
    public void testCollectionToCommaSeparatedStringEmptyStrings() {
        assertEquals(CommonMethods.collectionToCommaSeparatedString(Arrays.asList("", "")), ",");
    }

    @Test
    public void testCollectionToCommaSeparatedStringEmtpy() {
        assertEquals(CommonMethods.collectionToCommaSeparatedString(Collections.emptySet()), "");
    }

    @Test(expected = NullPointerException.class)
    public void testCollectionToCommaSeparatedStringNull() {
        assertNull(CommonMethods.collectionToCommaSeparatedString(null));
    }

    @Test
    public void testNextUuId() {
        assertNotNull(CommonMethods.nextUuId());
    }

    @Test
    public void testConcatBothValuePresent() {
        String []firstArray = {"1", "2"};
        String []secondArray = {"3", "4"};

        String []resultArray = CommonMethods.concat(firstArray, secondArray);

        assertEquals(resultArray.length, 4);
        assertTrue(ArrayUtils.contains(resultArray, secondArray[0])
                && ArrayUtils.contains(resultArray, firstArray[0]));
    }

    @Test
    public void testConcatBothFirstValuePresent() {
        String []firstArray = {"1", "2"};

        String []resultArray = CommonMethods.concat(firstArray, null);

        assertEquals(resultArray.length, 2);
        assertTrue(Arrays.asList(resultArray).containsAll(Arrays.asList(firstArray)));
    }

    @Test
    public void testConcatBothSecondValuePresent() {
        String []secondArray = {"3", "4"};

        String []resultArray = CommonMethods.concat(null, secondArray);

        assertEquals(resultArray.length, 2);
        assertTrue(Arrays.asList(resultArray).containsAll(Arrays.asList(secondArray)));
    }

    @Test
    public void testConcatBothValueNull() {
        assertNull(CommonMethods.concat(null, null));
    }

    @Test
    public void testNewInstance() {
        Object obj = CommonMethods.newInstance(JAVA_LANG_STRING);
        assertNotNull(obj);
        assertTrue(obj instanceof String);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewInstanceIncorrectClassProvided() {
        assertNull(CommonMethods.newInstance("java.lang.Stringss"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewInstanceClassNotProvided() {
        assertNull(CommonMethods.newInstance(null, Object.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewInstanceObjectNotProvided() {
        assertNull(CommonMethods.newInstance(JAVA_LANG_STRING, null));
    }

    @Test(expected = ClassCastException.class)
    public void testNewInstanceClassCastException() {
        assertNull(CommonMethods.newInstance(JAVA_LANG_STRING, ArrayList.class));
    }

    @Test(expected = RuntimeException.class)
    public void testNewInstanceInvalidClassProvided() {
        assertNull(CommonMethods.newInstance(List.class));
    }

    @Test
    public void testListToSeparatedString() {
        String str = "Concat,String";
        String result = CommonMethods.listToSeparatedString(
                Stream.of("Concat", "String").collect(Collectors.toList()), ',');

        assertNotNull(result);
        assertEquals(str, result);
    }

    @Test
    public void testDuplicateStringWithDelimiter() {
        String duplicateStr = CommonMethods.duplicateStringWithDelimiter("Duplicate", '#', 4);

        assertNotNull(duplicateStr);

        String[] duplicateStrArray = duplicateStr.split("#");
        assertTrue(duplicateStr.contains("Duplicate"));
        assertEquals(duplicateStrArray.length, 4);
    }

    @Test
    public void testRoSingleElement() {
        Set<String> stringSet = CommonMethods.toSingleElementSet("Set Element");
        assertNotNull(stringSet);
        assertTrue(stringSet.contains("Set Element"));
    }

    @Test
    public void testMergeListsOfMap() {
        Map<String, String> map1 = new HashMap<>();
        map1.put("Port1", "NeutronPort_CP_1");
        map1.put("Port2", "NeutronPort_CP_2");

        Map<String, String> map2 = new HashMap<>();
        map2.put("Server1", "NovaServer_1");
        map2.put("Server2", "NovaServer_2");

        List<Map<String, String>> list1 = Stream.of(map1, map2).collect(Collectors.toList());

        Map<String, String> map3 = new HashMap<>();
        map3.put("Port3", "NeutronPort_CP_3");
        map3.put("Port4", "NeutronPort_CP_4");

        Map<String, String> map4 = new HashMap<>();
        map4.put("Server3", "NovaServer_3");
        map4.put("Server4", "NovaServer_4");
        map4.put("Server2", "NovaServer_2");

        List<Map<String, String>> list2 = Stream.of(map3, map4).collect(Collectors.toList());

        List<Map<String, String>> resultList = CommonMethods.mergeListsOfMap(list1, list2);

        assertEquals(resultList.size(), 6);

        //Verify for duplicate key
        int count = 0;
        for(Map<String, String> map : resultList) {
            if(map.containsKey("Server2"))
                count++;
        }

        assertEquals(1, count);
    }

    @Test
    public void testMergeLists() {
        List<String> list1 = Stream.of("First", "Second").collect(Collectors.toList());
        List<String> list2 = Stream.of("Third", "Fourth").collect(Collectors.toList());

        List<String> resultList = CommonMethods.mergeLists(list1, list2);

        assertEquals(resultList.size(), 4);
        assertTrue(resultList.containsAll(list1));
        assertTrue(resultList.containsAll(list2));
    }

    @Test
    public void testMergeMaps() {
        Map<String, String> map1 = Stream.of(new AbstractMap.SimpleEntry<>("Port", "Neutron"),
                                             new AbstractMap.SimpleEntry<>("Compute", "NOVA"))
                                    .collect(Collectors.toMap(
                                            AbstractMap.SimpleEntry::getKey,
                                            AbstractMap.SimpleEntry::getValue));

        Map<String, String> map2 = Stream.of(new AbstractMap.SimpleEntry<>("VLAN", "VMI"),
                new AbstractMap.SimpleEntry<>("Volume", "Cinder"),
                new AbstractMap.SimpleEntry<>("Port", "VMI"))
                .collect(Collectors.toMap(
                        AbstractMap.SimpleEntry::getKey,
                        AbstractMap.SimpleEntry::getValue));

        Map<String, String> resultMap = CommonMethods.mergeMaps(map1, map2);

        assertEquals(resultMap.size(), 4);
        assertEquals(resultMap.get("Port"), "VMI");
    }
}
