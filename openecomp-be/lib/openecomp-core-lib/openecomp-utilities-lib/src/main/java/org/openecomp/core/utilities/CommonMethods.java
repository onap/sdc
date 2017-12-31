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

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * This class provides auxiliary static methods.
 */
public class CommonMethods {

  private static final char[] CHARS = new char[]{
      '0', '1', '2', '3', '4', '5', '6', '7',
      '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
  };

  /**
   * Private default constructor to prevent instantiation of the class objects.
   */
  private CommonMethods() {
  }

  /**
   * Checks whether the given collection is empty.
   *
   * @param collection A collection to be checked.
   * @return <tt>true</tt> - if the collection is null or empty, <tt>false</tt> - otherwise.
   */
  public static boolean isEmpty(Collection<?> collection) {
    return collection == null || collection.isEmpty();
  }

  /**
   * Gets an universally unique identifier (UUID).
   *
   * @return String representation of generated UUID.
   */
  public static String nextUuId() {
    UUID uuid = UUID.randomUUID();

    StringBuilder buff = new StringBuilder(32);
    long2string(uuid.getMostSignificantBits(), buff);
    long2string(uuid.getLeastSignificantBits(), buff);

    return buff.toString();
  }

  private static void long2string(long lng, StringBuilder buff) {
    long value = lng;
    for (int i = 0; i < 16; i++) {
      long nextByte = value & 0xF000000000000000L;
      value <<= 4;
      boolean isNegative = nextByte < 0;
      nextByte = nextByte >>> 60;

      if (isNegative) {
        nextByte |= 0x08;
      }

      buff.append(CHARS[(int) nextByte]);
    }
  }

  /**
   * Concatenates two Java arrays. The method allocates a new array and copies
   * all elements to it or returns one of input arrays if another one is
   * empty.
   *
   * @param <T>   the type parameter
   * @param left  Elements of this array will be copied to positions from 0 to <tt>left.length -
   *              1</tt> in the target array.
   * @param right Elements of this array will be copied to positions from <tt>left.length</tt> to
   *              <tt>left.length + right.length</tt>
   * @return A newly allocate Java array that accommodates elements of source (left/right) arrays
    orone of source arrays if another is empty, <tt>null</tt> - otherwise.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] concat(T[] left, T[] right) {
    T[] res;

    if (ArrayUtils.isEmpty(left)) {
      res = right;
    } else if (ArrayUtils.isEmpty(right)) {
      res = left;
    } else {
      res = (T[]) Array.newInstance(left[0].getClass(), left.length + right.length);
      System.arraycopy(left, 0, res, 0, left.length);
      System.arraycopy(right, 0, res, left.length, right.length);
    }

    return res;
  } // concat

  /**
   * New instance object.
   *
   * @param classname the classname
   * @return the object
   */
  public static Object newInstance(String classname) {
    return newInstance(classname, Object.class);
  }

  /**
   * New instance t.
   *
   * @param <T>       the type parameter
   * @param classname the classname
   * @param cls       the cls
   * @return the t
   */
  @SuppressWarnings("unchecked")
  public static <T> T newInstance(String classname, Class<T> cls) {

    if (StringUtils.isEmpty(classname)) {
      throw new IllegalArgumentException();
    }

    if (cls == null) {
      throw new IllegalArgumentException();
    }

    try {
      Class<?> temp = Class.forName(classname);

      if (!cls.isAssignableFrom(temp)) {
        throw new ClassCastException(
            String.format("Failed to cast from '%s' to '%s'", classname, cls.getName()));
      }

      Class<? extends T> impl = (Class<? extends T>) temp;

      return newInstance(impl);
    } catch (ClassNotFoundException exception) {
      throw new IllegalArgumentException(exception);
    }
  }

  /**
   * New instance t.
   *
   * @param <T> the type parameter
   * @param cls the cls
   * @return the t
   */
  public static <T> T newInstance(Class<T> cls) {
    try {
      return cls.newInstance();
    } catch (InstantiationException | IllegalAccessException exception) {
      throw new RuntimeException(exception);
    }
  }

  /**
   * Print stack trace string.
   *
   * @return the string
   */
  public static String printStackTrace() {
    StringBuilder sb = new StringBuilder();
    StackTraceElement[] trace = Thread.currentThread().getStackTrace();
    for (StackTraceElement traceElement : trace) {
      sb.append("\t  ").append(traceElement);
      sb.append(System.lineSeparator());
    }
    return sb.toString();
  }

  /**
   * Converts array of strings to comma-separated string.
   *
   * @param arr array of strings
   * @return the string
   */
  public static String arrayToCommaSeparatedString(String[] arr) {
    return arrayToSeparatedString(arr, ',');
  }

  /**
   * Collection to comma separated string string.
   *
   * @param elementCollection the element collection
   * @return the string
   */
  public static String collectionToCommaSeparatedString(Collection<String> elementCollection) {
    return String.join(",", elementCollection);
  }

  /**
   * Converts array of strings to string separated with specified character.
   *
   * @param arr       array of strings
   * @param separator the separator
   * @return the string
   */
  public static String arrayToSeparatedString(String[] arr, char separator) {
    return String.join(Character.toString(separator), arr);
  }

  /**
   * Converts array of strings to string separated with specified character.
   *
   * @param list      array of strings
   * @param separator the separator
   * @return the string
   */
  public static String listToSeparatedString(List<String> list, char separator) {
    return String.join(Character.toString(separator), list);
  }

  /**
   * Duplicate string with delimiter string.
   *
   * @param arg                  the arg
   * @param separator            the separator
   * @param numberOfDuplications the number of duplications
   * @return the string
   */
  public static String duplicateStringWithDelimiter(String arg, char separator,
                                                    int numberOfDuplications) {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < numberOfDuplications; i++) {
      if (i > 0) {
        sb.append(separator);
      }
      sb.append(arg);
    }
    return sb.toString();
  }

  /**
   * To single element set set.
   *
   * @param <T>     the class of the objects in the set
   * @param element the single element to be contained in the returned Set
   * @return an immutable set containing only the specified object. The returned set is
    serializable.
   */
  public static <T> Set<T> toSingleElementSet(T element) {
    return Collections.singleton(element);

  }

  /**
   * Merge lists of map list.
   *
   * @param <T>    the type parameter
   * @param <S>    the type parameter
   * @param target the target
   * @param source the source
   * @return the list
   */
  public static <T, S> List<Map<T, S>> mergeListsOfMap(List<Map<T, S>> target,
                                                       List<Map<T, S>> source) {
    List<Map<T, S>> retList = new ArrayList<>();
    if (Objects.nonNull(target)) {
      retList.addAll(target);
    }

    if (Objects.nonNull(source)) {
      for (Map<T, S> sourceMap : source) {
        for (Map.Entry<T, S> entry : sourceMap.entrySet()) {
          mergeEntryInList(entry.getKey(), entry.getValue(), retList);
        }
      }
    }
    return retList;
  }

  /**
   * Merge lists list.
   *
   * @param <T>    the type parameter
   * @param target the target
   * @param source the source
   * @return the list
   */
  public static <T> List<T> mergeLists(List<T> target, List<T> source) {
    List<T> retList = new ArrayList<>();

    if (Objects.nonNull(source)) {
      retList.addAll(source);
    }
    if (Objects.nonNull(target)) {
      retList.addAll(target);
    }

    return retList;
  }

  /**
   * Merge entry in list.
   *
   * @param <T>    the type parameter
   * @param <S>    the type parameter
   * @param key    the key
   * @param value  the value
   * @param target the target
   */
  public static <T, S> void mergeEntryInList(T key, S value, List<Map<T, S>> target) {
    boolean found = false;
    for (Map<T, S> map : target) {
      if (map.containsKey(key)) {
        map.put(key, value);
        found = true;
      }
    }

    if (!found) {
      Map<T, S> newMap = new HashMap<>();
      newMap.put(key, value);
      target.add(newMap);
    }
  }


  /**
   * Merge maps map.
   *
   * @param <T>    the type parameter
   * @param <S>    the type parameter
   * @param target the target
   * @param source the source
   * @return the map
   */
  public static <T, S> Map<T, S> mergeMaps(Map<T, S> target, Map<T, S> source) {
    Map<T, S> retMap = new HashMap<>();
    if (MapUtils.isNotEmpty(source)) {
      retMap.putAll(source);
    }
    if (MapUtils.isNotEmpty(target)) {
      retMap.putAll(target);
    }
    return retMap;
  }

}

