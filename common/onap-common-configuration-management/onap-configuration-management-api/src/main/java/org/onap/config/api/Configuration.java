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

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public interface Configuration {

    ThreadLocal<String> TENANT = new ThreadLocal<>();

    /**
     * Sets tenant for current thread.
     *
     * @param id tenant id; may be <code>null</code> in which case a default will be used.
     */
    static void setTenantId(String id) {
        if (id != null && id.trim().length() > 0) {
            TENANT.set(id);
        } else {
            TENANT.remove();
        }
    }

    default String getAsString(String key) {
        return getAsString(null, key);
    }

    default String getAsString(String namespace, String key) {
        return getAsString(TENANT.get(), namespace, key);
    }

    default String getAsString(String tenantId, String namespace, String key) {
        return get(tenantId, namespace, key, String.class);
    }

    <T> T get(String tenant, String namespace, String key, Class<T> clazz, Hint... hints);

    default Byte getAsByteValue(String key) {
        return getAsByteValue(null, key);
    }

    default Byte getAsByteValue(String namespace, String key) {
        return getAsByteValue(TENANT.get(), namespace, key);
    }

    default Byte getAsByteValue(String tenantId, String namespace, String key) {
        return get(tenantId, namespace, key, Byte.class);
    }

    default Short getAsShortValue(String key) {
        return getAsShortValue(null, key);
    }

    default Short getAsShortValue(String namespace, String key) {
        return getAsShortValue(TENANT.get(), namespace, key);
    }

    default Short getAsShortValue(String tenantId, String namespace, String key) {
        return get(tenantId, namespace, key, Short.class);
    }

    default Integer getAsIntegerValue(String key) {
        return getAsIntegerValue(null, key);
    }

    default Integer getAsIntegerValue(String namespace, String key) {
        return getAsIntegerValue(TENANT.get(), namespace, key);
    }

    default Integer getAsIntegerValue(String tenantId, String namespace, String key) {
        return get(tenantId, namespace, key, Integer.class);
    }

    default Long getAsLongValue(String key) {
        return getAsLongValue(null, key);
    }

    default Long getAsLongValue(String namespace, String key) {
        return getAsLongValue(TENANT.get(), namespace, key);
    }

    default Long getAsLongValue(String tenantId, String namespace, String key) {
        return get(tenantId, namespace, key, Long.class);
    }

    default Float getAsFloatValue(String key) {
        return getAsFloatValue(null, key);
    }

    default Float getAsFloatValue(String namespace, String key) {
        return getAsFloatValue(TENANT.get(), namespace, key);
    }

    default Float getAsFloatValue(String tenantId, String namespace, String key) {
        return get(tenantId, namespace, key, Float.class);
    }

    default Double getAsDoubleValue(String key) {
        return getAsDoubleValue(null, key);
    }

    default Double getAsDoubleValue(String namespace, String key) {
        return getAsDoubleValue(TENANT.get(), namespace, key);
    }

    default Double getAsDoubleValue(String tenantId, String namespace, String key) {
        return get(tenantId, namespace, key, Double.class);
    }

    default Boolean getAsBooleanValue(String key) {
        return getAsBooleanValue(null, key);
    }

    default Boolean getAsBooleanValue(String namespace, String key) {
        return getAsBooleanValue(TENANT.get(), namespace, key);
    }

    default Boolean getAsBooleanValue(String tenantId, String namespace, String key) {
        return get(tenantId, namespace, key, Boolean.class);
    }

    default Character getAsCharValue(String key) {
        return getAsCharValue(null, key);
    }

    default Character getAsCharValue(String namespace, String key) {
        return getAsCharValue(TENANT.get(), namespace, key);
    }

    default Character getAsCharValue(String tenantId, String namespace, String key) {
        return get(tenantId, namespace, key, Character.class);
    }

    default <T> T populateConfiguration(Class<T> clazz) {
        return populateConfiguration(null, clazz);
    }

    default <T> T populateConfiguration(String namespace, Class<T> clazz) {
        return populateConfiguration(TENANT.get(), namespace, clazz);
    }

    default <T> T populateConfiguration(String tenantId, String namespace, Class<T> clazz) {
        return get(tenantId, namespace, null, clazz, Hint.EXTERNAL_LOOKUP);
    }

    default List<String> getAsStringValues(String key) {
        return getAsStringValues(null, key);
    }

    default List<String> getAsStringValues(String namespace, String key) {
        return getAsStringValues(TENANT.get(), namespace, key);
    }

    default List<String> getAsStringValues(String tenantId, String namespace, String key) {
        String[] tempArray = get(tenantId, namespace, key, String[].class);
        return tempArray == null ? Collections.emptyList() : Arrays.asList(tempArray);
    }

    default List<Byte> getAsByteValues(String key) {
        return getAsByteValues(null, key);
    }

    default List<Byte> getAsByteValues(String namespace, String key) {
        return getAsByteValues(TENANT.get(), namespace, key);
    }

    default List<Byte> getAsByteValues(String tenantId, String namespace, String key) {
        Byte[] tempArray = get(tenantId, namespace, key, Byte[].class);
        return tempArray == null ? Collections.emptyList() : Arrays.asList(tempArray);
    }

    default List<Short> getAsShortValues(String key) {
        return getAsShortValues(null, key);
    }

    default List<Short> getAsShortValues(String namespace, String key) {
        return getAsShortValues(TENANT.get(), namespace, key);
    }

    default List<Short> getAsShortValues(String tenantId, String namespace, String key) {
        Short[] tempArray = get(tenantId, namespace, key, Short[].class);
        return tempArray == null ? Collections.emptyList() : Arrays.asList(tempArray);
    }

    default List<Integer> getAsIntegerValues(String key) {
        return getAsIntegerValues(null, key);
    }

    default List<Integer> getAsIntegerValues(String namespace, String key) {
        return getAsIntegerValues(TENANT.get(), namespace, key);
    }

    default List<Integer> getAsIntegerValues(String tenantId, String namespace, String key) {
        Integer[] tempArray = get(tenantId, namespace, key, Integer[].class);
        return tempArray == null ? Collections.emptyList() : Arrays.asList(tempArray);
    }

    default List<Double> getAsDoubleValues(String key) {
        return getAsDoubleValues(null, key);
    }

    default List<Double> getAsDoubleValues(String namespace, String key) {
        return getAsDoubleValues(TENANT.get(), namespace, key);
    }

    default List<Double> getAsDoubleValues(String tenantId, String namespace, String key) {
        Double[] tempArray = get(tenantId, namespace, key, Double[].class);
        return tempArray == null ? Collections.emptyList() : Arrays.asList(tempArray);
    }

    default List<Float> getAsFloatValues(String key) {
        return getAsFloatValues(null, key);
    }

    default List<Float> getAsFloatValues(String namespace, String key) {
        return getAsFloatValues(TENANT.get(), namespace, key);
    }

    default List<Float> getAsFloatValues(String tenantId, String namespace, String key) {
        Float[] tempArray = get(tenantId, namespace, key, Float[].class);
        return tempArray == null ? Collections.emptyList() : Arrays.asList(tempArray);
    }

    default List<Boolean> getAsBooleanValues(String key) {
        return getAsBooleanValues(null, key);
    }

    default List<Boolean> getAsBooleanValues(String namespace, String key) {
        return getAsBooleanValues(TENANT.get(), namespace, key);
    }

    default List<Boolean> getAsBooleanValues(String tenantId, String namespace, String key) {
        Boolean[] tempArray = get(tenantId, namespace, key, Boolean[].class);
        return tempArray == null ? Collections.emptyList() : Arrays.asList(tempArray);
    }

    default List<Character> getAsCharacterValues(String key) {
        return getAsCharacterValues(null, key);
    }

    default List<Character> getAsCharacterValues(String namespace, String key) {
        return getAsCharacterValues(TENANT.get(), namespace, key);
    }

    default List<Character> getAsCharacterValues(String tenantId, String namespace, String key) {
        Character[] tempArray = get(tenantId, namespace, key, Character[].class);
        return tempArray == null ? Collections.emptyList() : Arrays.asList(tempArray);
    }

    default <T> Map<String, T> populateMap(String key, Class<T> clazz) {
        return populateMap(null, key, clazz);
    }

    default <T> Map<String, T> populateMap(String namespace, String key, Class<T> clazz) {
        return populateMap(TENANT.get(), namespace, key, clazz);
    }

    <T> Map<String, T> populateMap(String tenantId, String namespace, String key, Class<T> clazz);

    default Map generateMap(String key) {
        return generateMap(null, key);
    }

    default Map generateMap(String namespace, String key) {
        return generateMap(TENANT.get(), namespace, key);
    }

    Map generateMap(String tenantId, String namespace, String key);
}
