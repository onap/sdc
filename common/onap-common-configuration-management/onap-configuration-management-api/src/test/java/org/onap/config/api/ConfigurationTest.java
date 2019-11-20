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
 */

package org.onap.config.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author evitaliy
 * @since 28 Oct 2018
 */
public class ConfigurationTest implements Configuration {

    @After
    public void cleanUp() {
        Configuration.TENANT.remove();
    }

    @Test
    public void tenantRetrievedWhenPreviouslySet() {
        final String tenantId = "abc";
        Configuration.setTenantId(tenantId);
        Assert.assertEquals(tenantId, Configuration.TENANT.get());
    }

    @Test
    public void tenantEmptyWhenNeverSet() {
        Assert.assertNull(Configuration.TENANT.get());
    }

    @Test
    public void tenantNullWhenNullSet() {
        Configuration.setTenantId("xyz");
        Configuration.setTenantId(null);
        Assert.assertNull(Configuration.TENANT.get());
    }

    @Test
    public void tenantNullWhenEmptySet() {
        Configuration.setTenantId("xyz");
        Configuration.setTenantId("");
        Assert.assertNull(Configuration.TENANT.get());
    }

    @Test
    public void tenantDoesNotPropagateToAnotherThread() throws ExecutionException, InterruptedException {
        final String currentTenant = "xyz";
        Configuration.setTenantId(currentTenant);
        CompletableFuture<String> result = new CompletableFuture<>();
        Thread otherThread = new Thread(() -> result.complete(Configuration.TENANT.get()));
        otherThread.start();
        Assert.assertNull("Tenant in the other thread expected to be null", result.get());
        Assert.assertEquals(currentTenant, Configuration.TENANT.get());
    }

    @Test
    public void testGetAsString() {
        final String tenantId = "test-string";
        final String key = "key-string";
        Configuration.setTenantId(tenantId);
        populateMap(tenantId, null, key, String.class);
        Assert.assertEquals(String.class, getAsString(key).getClass());
        Assert.assertEquals("42", getAsString(key));
    }

    @Test
    public void testGetAsByte() {
        final String tenantId = "test-byte";
        final String key = "key-byte";
        Configuration.setTenantId(tenantId);
        populateMap(tenantId, null, key, Byte.class);
        Assert.assertEquals(Byte.class, getAsByteValue(key).getClass());
        Assert.assertEquals(Byte.valueOf((byte) 42), getAsByteValue(key));
    }

    @Test
    public void testGetAsShort() {
        final String tenantId = "test-short";
        final String key = "key-short";
        Configuration.setTenantId(tenantId);
        populateMap(tenantId, null, key, Short.class);
        Assert.assertEquals(Short.class, getAsShortValue(key).getClass());
        Assert.assertEquals(Short.valueOf((short) 42), getAsShortValue(key));
    }

    @Test
    public void testGetAsInteger() {
        final String tenantId = "test-integer";
        final String key = "key-integer";
        Configuration.setTenantId(tenantId);
        populateMap(tenantId, null, key, Integer.class);
        Assert.assertEquals(Integer.class, getAsIntegerValue(key).getClass());
        Assert.assertEquals(Integer.valueOf(42), getAsIntegerValue(key));
    }

    @Test
    public void testGetAsLong() {
        final String tenantId = "test-long";
        final String key = "key-long";
        Configuration.setTenantId(tenantId);
        populateMap(tenantId, null, key, Long.class);
        Assert.assertEquals(Long.class, getAsLongValue(key).getClass());
        Assert.assertEquals(Long.valueOf(42), getAsLongValue(key));
    }

    @Test
    public void testGetAsDouble() {
        final String tenantId = "test-double";
        final String key = "key-double";
        Configuration.setTenantId(tenantId);
        populateMap(tenantId, null, key, Double.class);
        Assert.assertEquals(Double.class, getAsDoubleValue(key).getClass());
        Assert.assertEquals(Double.valueOf(42), getAsDoubleValue(key));
    }

    @Test
    public void testGetAsFloat() {
        final String tenantId = "test-float";
        final String key = "key-float";
        Configuration.setTenantId(tenantId);
        populateMap(tenantId, null, key, Float.class);
        Assert.assertEquals(Float.class, getAsFloatValue(key).getClass());
        Assert.assertEquals(Float.valueOf(42), getAsFloatValue(key));
    }

    @Test
    public void testGetAsBoolean() {
        final String tenantId = "test-bool";
        final String key = "key-bool";
        Configuration.setTenantId(tenantId);
        populateMap(tenantId, null, key, Boolean.class);
        Assert.assertEquals(Boolean.class, getAsBooleanValue(key).getClass());
        Assert.assertEquals(Boolean.TRUE, getAsBooleanValue(key));
    }

    @Test
    public void testGetAsCharacter() {
        final String tenantId = "test-char";
        final String key = "key-char";
        Configuration.setTenantId(tenantId);
        populateMap(tenantId, null, key, Character.class);
        Assert.assertEquals(Character.class, getAsCharValue(key).getClass());
        Assert.assertEquals(Character.valueOf('\u0042'), getAsCharValue(key));
    }

    // mocking implementation of Configuration non-default methods
    @Override
    public <T> T get(String tenant, String namespace, String key, Class<T> clazz, Hint... hints) {
        String typeName = clazz.getSimpleName();
        switch (typeName) { //Byte, Short, Integer, Long, Double, Float, Boolean, String, Char,
            case "Byte":
                return (T) Byte.valueOf((byte) 42);
            case "Short":
                return (T) Short.valueOf((short) 42);
            case "Long":
                return (T) Long.valueOf(42);
            case "Integer":
                return (T) Integer.valueOf(42);
            case "Double":
                return (T) Double.valueOf(42);
            case "Float":
                return (T) Float.valueOf(42);
            case "Boolean":
                return (T) Boolean.valueOf(true);
            case "Character":
                return (T) Character.valueOf('\u0042');
            case "String":
                return (T) String.valueOf(42);
            default:
                return null;
        }
    }

    @Override
    public <T> Map<String, T> populateMap(String tenantId, String namespace, String key, Class<T> clazz) {
        Map<String, T> map = new HashMap<>();
        map.put(tenantId + namespace + key, get(null, null, null, clazz));
        return map;
    }

    @Override
    public Map generateMap(String tenantId, String namespace, String key) {
        Map map = new HashMap();
        map.put(tenantId + namespace + key, getAsString(key));
        return map;
    }
}
