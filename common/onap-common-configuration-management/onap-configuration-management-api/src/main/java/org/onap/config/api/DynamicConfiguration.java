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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class DynamicConfiguration<T> {

    private String tenant;

    private String namespace;

    private String key;

    private Configuration configuration;

    private Class clazz;

    private T defaultValue;

    public static <K> DynamicConfiguration<List<K>> getDynConfiguration(String tenant, String namespace, String key,
            Class<K> clazz, K defaultValue, Configuration configuration) {
        if (clazz.isPrimitive()) {
            throw new RuntimeException("Only Wrapper classes like Integer, Long, Double, "
                                               + "Boolean etc including String are supported.");
        }
        return getDynamicConfiguration(tenant, namespace, key, getArrayClass(clazz),
                Collections.singletonList(defaultValue), configuration);
    }

    public static <T> DynamicConfiguration<T> getDynamicConfiguration(String tenant, String namespace, String key,
            Class<T> clazz, T defaultValue, Configuration configuration) {
        DynamicConfiguration<T> dynamicConfiguration = new DynamicConfiguration<>();
        dynamicConfiguration.tenant = tenant;
        dynamicConfiguration.namespace = namespace;
        dynamicConfiguration.key = key;
        dynamicConfiguration.clazz = clazz;
        dynamicConfiguration.defaultValue = defaultValue;
        dynamicConfiguration.configuration = configuration;
        return dynamicConfiguration;
    }

    public static Class getArrayClass(Class clazz) {
        Class arrayClass = null;
        switch (clazz.getName()) {
            case "java.lang.Byte":
                arrayClass = Byte[].class;
                break;
            case "java.lang.Short":
                arrayClass = Short[].class;
                break;
            case "java.lang.Integer":
                arrayClass = Integer[].class;
                break;
            case "java.lang.Long":
                arrayClass = Long[].class;
                break;
            case "java.lang.Float":
                arrayClass = Float[].class;
                break;
            case "java.lang.Double":
                arrayClass = Double[].class;
                break;
            case "java.lang.Boolean":
                arrayClass = Boolean[].class;
                break;
            case "java.lang.Character":
                arrayClass = Character[].class;
                break;
            case "java.lang.String":
                arrayClass = String[].class;
                break;
            default:
        }
        return arrayClass;
    }

    public T get() {
        Object toReturn = configuration.get(tenant, namespace, key, clazz, Hint.LATEST_LOOKUP, Hint.EXTERNAL_LOOKUP,
                Hint.NODE_SPECIFIC);
        if (toReturn != null && toReturn.getClass().isArray()) {
            toReturn = Arrays.asList((Object[]) toReturn);
        }
        return toReturn == null ? defaultValue : (T) toReturn;
    }

}
