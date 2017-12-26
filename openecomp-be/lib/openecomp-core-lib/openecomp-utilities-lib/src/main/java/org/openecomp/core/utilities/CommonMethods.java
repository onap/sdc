/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.core.utilities;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
  private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
  private static final Logger LOGGER = LoggerFactory.getLogger(CommonMethods.class);

  /**
   * Private default constructor to prevent instantiation of the class objects.
   */
  private CommonMethods() {
  }

  /**
   * Serializes an object instance into byte array.
   *
   * @param object An instance to be serialized.
   * @return Java array of bytes.
   * @see #deserializeObject(byte[]) #deserializeObject(byte[])
   */
  public static byte[] serializeObject(Serializable object) {
    ByteArrayOutputStream byteArray = new ByteArrayOutputStream(2048);
    try {
      ObjectOutputStream ds = new ObjectOutputStream(byteArray);
      ds.writeObject(object);
      ds.close();
    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }

    return byteArray.toByteArray();
  } // serializeObject

  /**
   * Deserializes an object instance.
   *
   * @param bytes Java array of bytes.
   * @return Deserialized instance of an object.
   * @see #serializeObject(Serializable) #serializeObject(Serializable)
   */
  public static Serializable deserializeObject(byte[] bytes) {
    Serializable obj = null;
    try {
      ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(bytes));
      obj = (Serializable) stream.readObject();
      stream.close();
    } catch (IOException | ClassNotFoundException exception) {
      throw new RuntimeException(exception);
    }

    return obj;
  } // deserializeObject

  /**
   * Encodes binary byte stream to ASCII format.
   *
   * @param binary An Java array of bytes in binary format.
   * @return An Java array of bytes encoded in ASCII format.
   * @see #decode(byte[]) #decode(byte[])
   */
  public static byte[] encode(byte[] binary) {
    return Base64.encodeBase64(binary);
  }

  /**
   * Decodes ASCII byte stream into binary format.
   *
   * @param ascii An Java array of bytes in ASCII format.
   * @return An Java array of bytes encoded in binary format.
   * @see #encode(byte[]) #encode(byte[])
   */
  public static byte[] decode(byte[] ascii) {
    return Base64.decodeBase64(ascii);
  }

  /**
   * Checks whether the given <tt>Object</tt> is empty.
   *
   * @param obj Object to be checked.
   * @return <tt>true</tt> - if the Object is null, <tt>false</tt> otherwise.
   */
  public static boolean isEmpty(Object obj) {
    return obj == null;
  }

  /**
   * Checks whether the given <tt>Object</tt> is empty.
   *
   * @param byteArray Object to be checked.
   * @return <tt>true</tt> - if the Object is null, <tt>false</tt> otherwise.
   */
  public static boolean isEmpty(byte[] byteArray) {
    return byteArray == null || byteArray.length == 0;
  }

  /**
   * Checks whether the given <tt>String</tt> is empty.
   *
   * @param str String object to be checked.
   * @return <tt>true</tt> - if the String is null or empty, <tt>false</tt> - otherwise.
   */
  public static boolean isEmpty(String str) {
    return str == null || str.length() == 0;
  }

  /**
   * Checks whether the given Java array is empty.
   *
   * @param array Java array to be checked.
   * @return <tt>true</tt> - if the array is null or empty, <tt>false</tt> - otherwise.
   */
  public static boolean isEmpty(Object[] array) {
    return array == null || array.length == 0;
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
   * Checks whether the given map is empty.
   *
   * @param map A map to be checked.
   * @return <tt>true</tt> - if the map is null or empty, <tt>false</tt> - otherwise.
   */
  public static boolean isEmpty(Map<?, ?> map) {
    return map == null || map.isEmpty();
  }

  /**
   * Converts the array with Long elements to the array with long (primitive type).
   *
   * @param array input array with Long elements
   * @return array with the same elements converted to the long type (primitive)
   */
  public static long[] toPrimitive(Long[] array) {
    if (array == null) {
      return new long[0];
    }

    long[] result = new long[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i] != null ? array[i] : 0L;
    }
    return result;
  }

  /**
   * Converts a collection to Java array.
   *
   * @param <T>  Java type of the collection element.
   * @param col  Collection to be converted to array
   * @param type Java type of collection/array element
   * @return An Java array of collection elements, or empty array if collection is null or empty.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] toArray(Collection<? extends T> col, Class<T> type) {
    int length = isEmpty(col) ? 0 : col.size();
    T[] array = (T[]) Array.newInstance(type, length);
    return col != null ? col.toArray(array) : array;
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
    for (int i = 0; i < 16; i++) {
      long nextByte = lng;
      nextByte = nextByte & 0xF000000000000000L;
      nextByte <<= 4;
      boolean isNegative = nextByte < 0;
      nextByte = rightShift(nextByte, 60);

      if (isNegative) {
        nextByte |= 0x08;
      }

      buff.append(CHARS[(int) nextByte]);
    }
  }

  private static long rightShift(long lng, int num) {
    return lng >>> num;
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

    if (isEmpty(left)) {
      res = right;
    } else if (isEmpty(right)) {
      res = left;
    } else {
      res = (T[]) Array.newInstance(left[0].getClass(), left.length + right.length);
      System.arraycopy(left, 0, res, 0, left.length);
      System.arraycopy(right, 0, res, left.length, right.length);
    }

    return res;
  } // concat

  /**
   * Casts an object to the class or interface represented by the specified
   * <tt>Class</tt> object. The method logic is similar to Java method
   * <tt>Class.cast(Object)</tt> with the only difference that unlike Java's
   * version the type name of the current object instance is specified in the
   * error message if casting fails to simplify error tracking.
   *
   * @param <B> the type parameter
   * @param <D> the type parameter
   * @param b1  An object instance to be casted to the specified Java type.
   * @param cls Target Java type.
   * @return Object instance safely casted to the requested Java type.
   * @throws ClassCastException In case which is the given object is not instance of the specified
   *                            Java type.
   */
  @SuppressWarnings("unchecked")
  public static <B, D> D cast(B b1, Class<D> cls) {
    D d1 = null;
    if (b1 != null) {
      if (!cls.isInstance(b1)) {
        throw new ClassCastException(String
            .format("Failed to cast from '%s' to '%s'", b1.getClass().getName(), cls.getName()));
      } else {
        d1 = (D) b1;
      }
    }

    return d1;
  } // cast

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

    if (isEmpty(classname)) {
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
   * Gets resources path.
   *
   * @param resourceName the resource name
   * @return the resources path
   */
  public static String getResourcesPath(String resourceName) {
    URL resourceUrl = CommonMethods.class.getClassLoader().getResource(resourceName);
    return resourceUrl != null ? resourceUrl.getPath()
        .substring(0, resourceUrl.getPath().lastIndexOf("/") + 1) : null;
  }

  /**
   * Gets stack trace.
   *
   * @param throwable the throwable
   * @return the stack trace
   */
  public static String getStackTrace(Throwable throwable) {
    if (null == throwable) {
      return "";
    }
    StringWriter sw = new StringWriter();
    throwable.printStackTrace(new PrintWriter(sw));
    return sw.toString();
  }

  /**
   * Print stack trace string.
   *
   * @return the string
   */
  public static String printStackTrace() {

    StringWriter sw = new StringWriter();
    StackTraceElement[] trace = Thread.currentThread().getStackTrace();
    for (StackTraceElement traceElement : trace) {
      sw.write("\t  " + traceElement);
      sw.write(System.lineSeparator());
    }
    String str = sw.toString();
    try {
      sw.close();
    } catch (IOException exception) {
      LOGGER.error("Error while printing stacktrace" + exception);
    }
    return str;

  }

  /**
   * Is equal object boolean.
   *
   * @param obj1 the obj 1
   * @param obj2 the obj 2
   * @return the boolean
   */
  public static boolean isEqualObject(Object obj1, Object obj2) {
    boolean isEqualValue = false;
    if (obj1 == null && obj2 == null) {
      isEqualValue = true;
    }

    if (!isEqualValue && obj1 != null && obj2 != null && obj1.equals(obj2)) {
      isEqualValue = true;
    }
    return isEqualValue;
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
    List<String> list = new ArrayList<>();
    list.addAll(elementCollection);
    return listToSeparatedString(list, ',');
  }

  /**
   * Converts array of strings to string separated with specified character.
   *
   * @param arr       array of strings
   * @param separator the separator
   * @return the string
   */
  public static String arrayToSeparatedString(String[] arr, char separator) {
    return listToSeparatedString(Arrays.asList(arr), separator);
  }

  /**
   * Converts array of strings to string separated with specified character.
   *
   * @param list      array of strings
   * @param separator the separator
   * @return the string
   */
  public static String listToSeparatedString(List<String> list, char separator) {
    String res = null;
    if (null != list) {
      StringBuilder sb = new StringBuilder();
      int sz = list.size();
      for (int i = 0; i < sz; i++) {
        if (i > 0) {
          sb.append(separator);
        }
        sb.append(list.get(i));
      }
      res = sb.toString();
    }
    return res;
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
    String res;
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < numberOfDuplications; i++) {
      if (i > 0) {
        sb.append(separator);
      }
      sb.append(arg);
    }
    res = sb.toString();
    return res;
  }

  /**
   * Bytes to hex string.
   *
   * @param bytes the bytes
   * @return the string
   */
  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int var = bytes[j] & 0xFF;
      int x1 = j << 1;
      hexChars[x1] = hexArray[var >>> 4];
      hexChars[x1 + 1] = hexArray[var & 0x0F];
    }
    return new String(hexChars);
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

