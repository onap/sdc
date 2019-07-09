/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openecomp.sdc.be.utils.FixtureHelpers.fixture;

public class JsonTester {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public JsonTester() {

    }

    public static <T> void testJson(T object, String fixturePath) throws Exception {
        testJson(object, fixturePath, MAPPER);
    }

    @SuppressWarnings("unchecked")
    public static <T> void testJson(T object, String fixturePath, ObjectMapper mapper) throws Exception {
        T expectedObject = (T) mapper.readValue(fixture(fixturePath), object.getClass());
        String expectedJson = mapper.writeValueAsString(expectedObject);
        String actualJson = mapper.writeValueAsString(object);

        assertThat(actualJson).isEqualTo(expectedJson);
    }

    @SuppressWarnings("unchecked")
    public static <T> void testJsonMap(Map<String, T> map, Class<T> valueClass, String fixturePath, ObjectMapper mapper) throws Exception {
        MapType mapType = TypeFactory.defaultInstance().constructMapType(Map.class, String.class, valueClass);
        Map<String, T> expectedObject = mapper.readValue(fixture(fixturePath), mapType);
        String expectedJson = mapper.writeValueAsString(expectedObject);

        String actualJson = mapper.writeValueAsString(map);

        assertThat(actualJson).isEqualTo(expectedJson);
    }
}
