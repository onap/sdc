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

package org.openecomp.sdc.generator.data;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.util.Base64;

/**
 * Utility method class for artifact generation.
 */
public class GeneratorUtil {
  /**
   * Translate tosca yaml into the provided model class.
   *
   * @param tosca    Tosca file content
   * @param classOfT Model class for the translated object
   * @param <T>      Template parameter for the return object
   * @return Object model for the provided tosca yaml file
   */
  public static <T> T translateTosca(String tosca, Class<T> classOfT) throws IOException {
    T tos;
    //changing file
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory()); // jackson data-bind
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    tos = mapper.readValue(tosca, classOfT);
    return tos;
  }

  /**
   * Decodes Base64 encode byte array input.
   *
   * @param input Base64 encoded byte array
   * @return Decoded byte array
   */
  public static byte[] decoder(byte[] input) {
    if (input != null) {
      byte[] output = Base64.getDecoder().decode(input);
      return output;
    }
    return null;
  }

  /**
   * Encode a byte array input using Base64 encoding.
   *
   * @param input Input byte array to be encoded
   * @return Base64 encoded byte array
   */
  public static byte[] encode(byte[] input) {
    if (input != null) {
      byte[] output = Base64.getEncoder().encode(input);
      return output;
    }
    return null;
  }

  /**
   * Calculate the checksum for a given input.
   *
   * @param input Byte array for which the checksum has to be calculated
   * @return Calculated checksum of the input byte array
   */
  public static String checkSum(byte[] input) {
    String checksum = null;
    if (input != null) {
      checksum = (DigestUtils.md5Hex(input)).toUpperCase();
    }
    return checksum;
  }

  /**
   * Check if string is empty or null.
   *
   * @param input Input String
   * @return true if string is empty/null and false otherwise
   */
  public static boolean isEmpty(String input) {
    return input == null || input.length() == 0;
  }

}
