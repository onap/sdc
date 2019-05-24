/*
 * Copyright © 2016-2018 European Support Limited
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
 *
 * Modifications Copyright (c) 2019 Samsung
 *
 */

package org.onap.config;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;

public class ConfigurationUtilsTest {

    @Test
    public void testCommaList() {
        List<?> list = Arrays.asList("1", "2", 3);
        String commaSeparatedList = ConfigurationUtils.getCommaSeparatedList(list);
        list.forEach(o -> assertTrue(commaSeparatedList.contains(o.toString())));
    }

    @Test
    public void testCommaListWithNullAndEmptyStrings() {
        List list = Arrays.asList(null, "", " ");
        String commaSeparatedList = ConfigurationUtils.getCommaSeparatedList(list);
        assertTrue(commaSeparatedList.isEmpty());
    }

    @Test
    public void testGetArrayClassFunction() {
        assertEquals(String[].class, ConfigurationUtils.getArrayClass(String.class));
        assertNull(ConfigurationUtils.getArrayClass(ConfigurationUtilsTest.class));
    }

    @Test
    public void testGetCollectionGenericType() throws NoSuchFieldException {

        class DummyClass {
            public Map<String, String> testParameterizedTypeField;
        }
        Field field = DummyClass.class.getField("testParameterizedTypeField");
        assertEquals(String.class, ConfigurationUtils.getCollectionGenericType(field));
    }

    @Test
    public void testCastingArray() {
        int arraySize = 2;
        final Class[] primitiveType = new Class[]{boolean.class, byte.class,
                double.class, float.class, int.class, long.class, short.class};

        for (Class clazz: primitiveType) {
            Class expectedResultClass = Array.newInstance(clazz, 0).getClass();
            List defaultCollection = IntStream.range(0, arraySize).mapToObj(i ->
                    ConfigurationUtils.getDefaultFor(clazz)).collect(toList());

            Object resultArray = ConfigurationUtils.getPrimitiveArray(defaultCollection, clazz);

            assertNotNull(resultArray);
            assertFalse(ConfigurationUtils.isZeroLengthArray(expectedResultClass, resultArray));
            assertNotNull(expectedResultClass.cast(resultArray));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCompatibleCollection() {
        final Map<Class, Class> testClasses = Stream.of(new Class[][] {
                {BlockingQueue.class, LinkedBlockingQueue.class},
                {TransferQueue.class, LinkedTransferQueue.class},
                {Deque.class, ArrayDeque.class},
                {Queue.class, ConcurrentLinkedQueue.class},
                {SortedSet.class, TreeSet.class},
                {Set.class, HashSet.class},
                {List.class, ArrayList.class}
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        testClasses.forEach((entryClass, expResultClass) -> {
                Class resultClass = ConfigurationUtils.getCompatibleCollectionForAbstractDef(entryClass).getClass();
                assertEquals(expResultClass, resultClass);
            }
        );

        ConfigurationUtils.getCompatibleCollectionForAbstractDef(Collection.class);

    }
}