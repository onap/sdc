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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
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

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.junit.Test;
import org.onap.config.util.TestUtil;
import org.onap.config.api.Hint;
import org.onap.config.impl.ConfigurationRepository;

public class ConfigurationUtilsTest {

    public static final String TMP_DIR_PREFIX = "sdc-testing-";
    private static final String TEST_NAME_SPACE = "testNameSpaceOne";
    private static final String TEST_COMPOSITE_NAMESPACE = "testCOmpositeConfig";

    @Test
    public void testCommaList() {
        List<?> list = Arrays.asList("1", "2", 3);
        String commaSeparatedList = ConfigurationUtils.getCommaSeparatedList(list);
        list.forEach(o -> assertTrue(commaSeparatedList.contains(o.toString())));
    }

    @Test
    public void testCommaListWithNullAndEmptyStrings() {
        List<String> list = Arrays.asList(null, "", " ");
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
        final Class<?>[] primitiveType = new Class[]{boolean.class, byte.class,
                double.class, float.class, int.class, long.class, short.class};

        for (Class<?> clazz : primitiveType) {
            Class<?> expectedResultClass = Array.newInstance(clazz, 0).getClass();
            List<?> defaultCollection = IntStream.range(0, arraySize).mapToObj(i ->
                    ConfigurationUtils.getDefaultFor(clazz)).collect(toList());

            Object resultArray = ConfigurationUtils.getPrimitiveArray(defaultCollection, clazz);

            assertNotNull(resultArray);
            assertFalse(ConfigurationUtils.isZeroLengthArray(expectedResultClass, resultArray));
            assertNotNull(expectedResultClass.cast(resultArray));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCompatibleCollection() {
        final Map<Class<?>, Class<?>> testClasses = Stream.of(new Class<?>[][]{
                {BlockingQueue.class, LinkedBlockingQueue.class},
                {TransferQueue.class, LinkedTransferQueue.class},
                {Deque.class, ArrayDeque.class},
                {Queue.class, ConcurrentLinkedQueue.class},
                {SortedSet.class, TreeSet.class},
                {Set.class, HashSet.class},
                {List.class, ArrayList.class}
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        testClasses.forEach((entryClass, expResultClass) -> {
                    Class<?> resultClass = ConfigurationUtils.getCompatibleCollectionForAbstractDef(entryClass).getClass();
                    assertEquals(expResultClass, resultClass);
                }
        );

        ConfigurationUtils.getCompatibleCollectionForAbstractDef(Collection.class);

    }

    @Test
    public void testGetPrimitivesArrayZeroLength() {
            assertArrayEquals(new int[0], (int[]) ConfigurationUtils.getPrimitiveArray(Collections.emptyList(), int.class));
            assertArrayEquals(new byte[0], (byte[]) ConfigurationUtils.getPrimitiveArray(Collections.emptyList(), byte.class));
            assertArrayEquals(new short[0], (short[]) ConfigurationUtils.getPrimitiveArray(Collections.emptyList(), short.class));
            assertArrayEquals(new long[0], (long[]) ConfigurationUtils.getPrimitiveArray(Collections.emptyList(), long.class));
            assertArrayEquals(new double[0], (double[]) ConfigurationUtils.getPrimitiveArray(Collections.emptyList(), double.class), 0);
            assertArrayEquals(new float[0], (float[]) ConfigurationUtils.getPrimitiveArray(Collections.emptyList(), float.class), 0);
            assertArrayEquals(new boolean[0], (boolean[]) ConfigurationUtils.getPrimitiveArray(Collections.emptyList(), boolean.class));
            assertArrayEquals(new char[0], (char[]) ConfigurationUtils.getPrimitiveArray(Collections.emptyList(), char.class));
            assertNull(ConfigurationUtils.getPrimitiveArray(Collections.emptyList(), Integer.class));
    }

    @Test
    public void testGetWrappersArrayZeroLength() {
        assertArrayEquals(new Integer[0], (Integer[]) ConfigurationUtils.getWrappersArray(Collections.emptyList(), Integer.class));
        assertArrayEquals(new Byte[0], (Byte[]) ConfigurationUtils.getWrappersArray(Collections.emptyList(), Byte.class));
        assertArrayEquals(new Short[0], (Short[]) ConfigurationUtils.getWrappersArray(Collections.emptyList(), Short.class));
        assertArrayEquals(new Long[0], (Long[]) ConfigurationUtils.getWrappersArray(Collections.emptyList(), Long.class));
        assertArrayEquals(new Double[0], (Double[]) ConfigurationUtils.getWrappersArray(Collections.emptyList(), Double.class));
        assertArrayEquals(new Float[0], (Float[]) ConfigurationUtils.getWrappersArray(Collections.emptyList(), Float.class));
        assertArrayEquals(new Boolean[0], (Boolean[]) ConfigurationUtils.getWrappersArray(Collections.emptyList(), Boolean.class));
        assertArrayEquals(new Character[0], (Character[]) ConfigurationUtils.getWrappersArray(Collections.emptyList(), Character.class));
        assertNull(ConfigurationUtils.getWrappersArray(Collections.emptyList(), boolean.class));
    }

    @Test
    public void testPrimitivesArrayNonZeroLength() {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, (int[]) ConfigurationUtils.getPrimitiveArray(list, int.class));
    }

    @Test
    public void testWrappersArrayNonZeroLength() {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        assertArrayEquals(new Integer[]{1, 2, 3, 4, 5}, (Integer[]) ConfigurationUtils.getWrappersArray(list, Integer.class));
    }

    @Test
    public void testGetAllFilesRecursiveIncludeAll() throws IOException {
        Path tmpRoot = TestUtil.createTestDirsStructure(TMP_DIR_PREFIX);
        Collection<File> allFiles = ConfigurationUtils.getAllFiles(tmpRoot.toFile(), true, false);
        assertEquals(7, allFiles.size());
        TestUtil.deleteTestDirsStrucuture(tmpRoot);
    }

    @Test
    public void testGetAllFilesRecursiveIncludeDirsOnly() throws IOException {
        Path tmpRoot = TestUtil.createTestDirsStructure(TMP_DIR_PREFIX);
        Collection<File> allFiles = ConfigurationUtils.getAllFiles(tmpRoot.toFile(), true, true);
        assertEquals(3, allFiles.size());
        TestUtil.deleteTestDirsStrucuture(tmpRoot);
    }

    @Test
    public void testGetAllFilesNonRecursiveIncludeAll() throws IOException {
        Path tmpRoot = TestUtil.createTestDirsStructure(TMP_DIR_PREFIX);
        Collection<File> allFiles = ConfigurationUtils.getAllFiles(tmpRoot.toFile(), false, false);
        assertEquals(2, allFiles.size());
        TestUtil.deleteTestDirsStrucuture(tmpRoot);
    }

    @Test
    public void testGetAllFilesNonRecursiveIncludeDirsOnly() throws IOException {
        Path tmpRoot = TestUtil.createTestDirsStructure(TMP_DIR_PREFIX);
        Collection<File> allFiles = ConfigurationUtils.getAllFiles(tmpRoot.toFile(), false, true);
        assertEquals(1, allFiles.size());
        TestUtil.deleteTestDirsStrucuture(tmpRoot);
    }

    @Test
    public void testGetAllFilesEmptyDir() throws IOException {
        Path tmpRoot = TestUtil.createEmptyTmpDir(TMP_DIR_PREFIX);
        Collection<File> allFiles = ConfigurationUtils.getAllFiles(tmpRoot.toFile(), true, true);
        assertEquals(0, allFiles.size());
        TestUtil.deleteTestDirsStrucuture(tmpRoot);
    }

    @Test
    public void testGetAllFilesNonExistentDir() throws IOException {
        Path nonExistentDir = Paths.get("/tmp/nonexistentdir");
        Collection<File> allFiles = ConfigurationUtils.getAllFiles(nonExistentDir.toFile(), false, true);
        assertEquals(0, allFiles.size());
    }

    @Test
    public void testGetConfigPropertyBaseConfig() throws Exception {
        ConfigurationRepository repo = populateTestBaseConfig();
        Configuration config = repo.getConfigurationFor(Constants.DEFAULT_TENANT, TEST_NAME_SPACE);
        assertEquals(TEST_NAME_SPACE, ConfigurationUtils.getProperty(config, Constants.NAMESPACE_KEY, Hint.DEFAULT.value()));
    }

    @Test
    public void testGetCompositeConfigPropertyDefaultHints() throws Exception {
        ConfigurationRepository repo = populateTestCompositeConfig();
        Configuration config = repo.getConfigurationFor(Constants.DEFAULT_TENANT, TEST_COMPOSITE_NAMESPACE);
        assertEquals(TEST_NAME_SPACE, ConfigurationUtils.getProperty(config, Constants.NAMESPACE_KEY, Hint.DEFAULT.value()));
    }

    @Test
    public void testGetCompositeConfigPropertyNodeSpecificHints() throws Exception {
        ConfigurationRepository repo = populateTestCompositeConfig();
        Configuration config = repo.getConfigurationFor(Constants.DEFAULT_TENANT, TEST_COMPOSITE_NAMESPACE);
        assertEquals(TEST_NAME_SPACE, ConfigurationUtils.getProperty(config, Constants.NAMESPACE_KEY, Hint.NODE_SPECIFIC.value()));
    }

    @Test
    public void testIsAMap() {
        assertTrue(ConfigurationUtils.isAMap(HashMap.class));
        assertFalse(ConfigurationUtils.isAMap(ArrayList.class));
    }

    @Test
    public void testIsACollection() {
        assertTrue(ConfigurationUtils.isACollection(ArrayList.class));
        assertFalse(ConfigurationUtils.isACollection(HashMap.class));
    }

    @Test
    public void testIsAPrimitiveOrWrapper() {
        assertTrue(ConfigurationUtils.isAPrimitiveOrWrapper(int.class));
        assertTrue(ConfigurationUtils.isAPrimitiveOrWrapper(Integer.class));
        assertFalse(ConfigurationUtils.isAPrimitiveOrWrapper(HashMap.class));
    }

    @Test
    public void testIsAPrimitivesArray() {
        assertTrue(ConfigurationUtils.isAPrimitivesArray(int[].class));
        assertFalse(ConfigurationUtils.isAPrimitivesArray(Integer[].class));
        assertFalse(ConfigurationUtils.isAPrimitivesArray(HashMap[].class));
    }

    @Test
    public void testIsAWrapperArray() {
        assertTrue(ConfigurationUtils.isAWrappersArray(Integer[].class));
        assertFalse(ConfigurationUtils.isAWrappersArray(int[].class));
        assertFalse(ConfigurationUtils.isAWrappersArray(HashMap[].class));
    }

    @Test
    public void testIsAPrimitivesOrWrapperArray() {
        assertTrue(ConfigurationUtils.isAPrimitivesOrWrappersArray(int[].class));
        assertTrue(ConfigurationUtils.isAPrimitivesOrWrappersArray(Integer[].class));
        assertFalse(ConfigurationUtils.isAPrimitivesOrWrappersArray(HashMap[].class));
    }

    private ConfigurationRepository populateTestBaseConfig() {
        BaseConfiguration inputConfig = new PropertiesConfiguration();
        inputConfig.setProperty(Constants.NAMESPACE_KEY, TEST_NAME_SPACE);
        ConfigurationRepository repo = ConfigurationRepository.lookup();

        repo.populateConfiguration(
                Constants.DEFAULT_TENANT + Constants.KEY_ELEMENTS_DELIMITER + TEST_NAME_SPACE, inputConfig);

        return repo;
    }

    private ConfigurationRepository populateTestCompositeConfig() {
        CompositeConfiguration inputCompositeConfig = new CompositeConfiguration();
        Configuration inputConfig1 = new BaseConfiguration();
        Configuration inputConfig2 = new BaseConfiguration();
        inputConfig1.setProperty(Constants.NAMESPACE_KEY, TEST_NAME_SPACE);
        inputCompositeConfig.addConfiguration(inputConfig1);
        inputCompositeConfig.addConfigurationFirst(inputConfig2);

        ConfigurationRepository repo = ConfigurationRepository.lookup();
        repo.populateConfiguration(
                Constants.DEFAULT_TENANT + Constants.KEY_ELEMENTS_DELIMITER + TEST_COMPOSITE_NAMESPACE,
                inputCompositeConfig
        );

        return repo;
    }
}
