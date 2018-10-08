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

package org.onap.sdc.tosca.services;


import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.beanutils.BeanUtils;
import com.google.common.collect.ImmutableSet;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CommonUtil {

    private static final String DEFAULT = "default";
    private static final String _DEFAULT = "_default";
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
        T result = classToCreate.newInstance();

        Field[] declaredFields = classToCreate.getDeclaredFields();
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
        BeanUtils.populate(result, objectAsMap);

        return Optional.of(result);
    }

    public static Map<String, Object> getObjectAsMap(Object obj) {
        Map<String, Object> objectAsMap =
                obj instanceof Map ? (Map<String, Object>) obj : new ObjectMapper().convertValue(obj, Map.class);

        if (objectAsMap.containsKey(DEFAULT)) {
            Object defaultValue = objectAsMap.get(DEFAULT);
            objectAsMap.remove(DEFAULT);
            objectAsMap.put(_DEFAULT, defaultValue);
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
