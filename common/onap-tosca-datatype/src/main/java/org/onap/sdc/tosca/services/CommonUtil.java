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
 * ============LICENSE_END=========================================================
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.onap.sdc.tosca.services;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import com.google.common.collect.ImmutableSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.beanutils.BeanUtils;

public class CommonUtil {

    public static final String DEFAULT = "default";
    public static final String UNDERSCORE_DEFAULT = "_default";
    private static ImmutableSet<Class<?>> complexClassType = ImmutableSet.of(Map.class, String.class, Integer.class, Float.class,
            Double.class, Set.class, Object.class, List.class);

    private CommonUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> Optional<T> createObjectUsingSetters(Object objectCandidate, Class<? extends T> classToCreate)
            throws Exception {
        if (Objects.isNull(objectCandidate)) {
            return Optional.empty();
        }
        Map<String, Object> objectAsMap = getObjectAsMap(objectCandidate);

        Field[] declaredFields = classToCreate.getDeclaredFields();
        createSubObjectsUsingSetters(objectAsMap, declaredFields);
        T result = populateBean(objectAsMap, classToCreate);

        return Optional.of(result);
    }

    public static void createSubObjectsUsingSetters(Map<String, Object> objectAsMap, Field[] declaredFields)
        throws Exception {
        for (Field field : declaredFields) {
            if (isComplexClass(field)) {
                Optional<?> objectUsingSetters =
                        createObjectUsingSetters(objectAsMap.get(field.getName()), field.getType());
                if (objectUsingSetters.isPresent()) {
                    objectAsMap.remove(field.getName());
                    objectAsMap.put(field.getName(), objectUsingSetters.get());
                }
            }
        }
    }

    public static <T> T populateBean(Map<String, Object> propertiesMap, Class<T> classToCreate)
        throws IllegalAccessException, InstantiationException, InvocationTargetException {
        T result = classToCreate.newInstance();
        BeanUtils.populate(result, propertiesMap);
        return result;
    }

    public static Map<String, Object> getObjectAsMap(Object obj) {
        Map<String, Object> objectAsMap =
                obj instanceof Map ? (Map<String, Object>) obj : new ObjectMapper().convertValue(obj, Map.class);

        if (objectAsMap.containsKey(DEFAULT)) {
            Object defaultValue = objectAsMap.get(DEFAULT);
            objectAsMap.remove(DEFAULT);
            objectAsMap.put(UNDERSCORE_DEFAULT, defaultValue);
        }
        return objectAsMap;
    }

    private static boolean isComplexClass(Field field) {
        return !complexClassType.contains(field.getType());
    }

    public static Set<String> getClassFieldNames(Class<? extends Object> classType) {
        Set<String> fieldNames = new HashSet<>();
        Class superClass = classType.getSuperclass();
        if (superClass != null) {
            fieldNames.addAll(getClassFieldNames(superClass));
        }
        Arrays.stream(classType.getDeclaredFields()).forEach(field -> fieldNames.add(field.getName()));
        return fieldNames;
    }
}
