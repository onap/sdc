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

package org.openecomp.sdc.be.dao.jsongraph.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Strings;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JsonParserUtils {
    private static Logger log = Logger.getLogger(JsonParserUtils.class.getName());
    private static final ObjectMapper mapper = buildObjectMapper();

    private JsonParserUtils() {
        // No instances allowed
    }

    private static ObjectMapper buildObjectMapper() {
        return new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static <T> String toJson(T object) throws IOException {
        return mapper.writer()
                     .writeValueAsString(object);
    }

    public static Map<String, Object> toMap(String json) {
        if (Strings.isNullOrEmpty(json)) {
            return null;
        }

        Map<String, Object> object = null;
        try {
            TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {
            };
            object = mapper.readerFor(typeRef)
                           .readValue(json);
        }
        catch (Exception e) {
            log.debug("Failed to parse json {}", json, e);
        }
        return object;
    }

    public static <T extends ToscaDataDefinition> Map<String, T> toMap(String json, Class<T> clazz) {
        if (Strings.isNullOrEmpty(json)) {
            return null;
        }

        Map<String, T> object = null;
        try {
            JavaType type = mapper.getTypeFactory()
                                  .constructMapType(Map.class, String.class, clazz);
            object = mapper.readerFor(type)
                           .readValue(json);
        }
        catch (Exception e) {
            log.debug("Failed to parse json {} to map", json, e);
        }
        return object;
    }
    public static <T> List<T> toList(String json, Class<T> clazz) {
        if (Strings.isNullOrEmpty(json)) {
            return null;
        }
        List<T> object = null;
        try {
            JavaType type = mapper.getTypeFactory()
                                  .constructCollectionType(List.class, clazz);

            object = mapper.readerFor(type)
                    .readValue(json);
        }
        catch (Exception e) {
            log.debug("Failed to parse json {} to list", json, e);
        }
        return object;
    }
}
