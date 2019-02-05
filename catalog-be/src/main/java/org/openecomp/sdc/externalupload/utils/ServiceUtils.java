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

package org.openecomp.sdc.externalupload.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.Field;
import java.util.*;

public class ServiceUtils {

  private static ImmutableSet<Class> collectionClasses = ImmutableSet.of(Map.class, List.class, Set.class);
  private static ImmutableSet<Class> primitiveTypesClasses = ImmutableSet.of(String.class, Integer.class, Double.class, Float.class);

  private ServiceUtils() {}

  public static <T> Optional<T> createObjectUsingSetters(Object objectCandidate,
                                                         Class<T> classToCreate)
      throws Exception {
    if (Objects.isNull(objectCandidate)) {
      return Optional.empty();
    }

    Map<String, Object> objectAsMap = getObjectAsMap(objectCandidate);
    T result = classToCreate.newInstance();

    List<Field> declaredFields = getAllFields(classToCreate);
    for( Field field : declaredFields){
      if(isComplexClass(field)){
        Optional<?> objectUsingSetters =
            createObjectUsingSetters(objectAsMap.get(field.getName()), field.getType());
        if( objectUsingSetters.isPresent()){
          objectAsMap.remove(field.getName());
          objectAsMap.put(field.getName(), objectUsingSetters.get());
        }
      }
    }
    BeanUtils.populate(result, objectAsMap);

    return Optional.of(result);
  }

  private static <T> List<Field> getAllFields(Class<T> clazz) {
    List<Field> fields = new ArrayList<>();
    for(Class<?> c = clazz; c != null; c = c.getSuperclass()) {
      fields.addAll(Arrays.asList(c.getDeclaredFields()));
    }

    return fields;
  }

  private static boolean isComplexClass(Field field) {
    return !isCollectionClass(field)
        && !isPrimitiveClass(field)
        && !field.getType().equals(Object.class);
  }

  private static boolean isCollectionClass(Field field) {
    return collectionClasses.contains(field.getType());
  }

  private static boolean isPrimitiveClass(Field field) {
    return primitiveTypesClasses.contains(field.getType());
  }

  public static Map<String, Object> getObjectAsMap(Object obj) {
    return new ObjectMapper().convertValue(obj, Map.class);
  }

  public static Set<String> getClassFieldNames(Class<? extends Object> classType) {
    Set<String> fieldNames = new HashSet<>();
    List<Field> allFields = getAllFields(classType);
    allFields.forEach(field -> fieldNames.add(field.getName()));

    return fieldNames;
  }
}