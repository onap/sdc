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
 * Modifications copyright (c) 2018 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.dao.utils;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JsonUtilTest {

    @Test
    void testReadObject_1() throws Exception {
        final String objectText = "{}";
        final Class objectClass = Object.class;
        final Object result = JsonUtil.readObject(objectText, objectClass);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result instanceof Map);
        Assertions.assertEquals(0, ((Map) result).size());
    }

    @Test
    void testReadObject_2() throws Exception {
        final String objectText = "{}";
        final Object result = JsonUtil.readObject(objectText);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result instanceof Map);
        Assertions.assertEquals(0, ((Map) result).size());
    }

    @Test
    void testToMap_1() throws Exception {
        final String json = "{\"name\":\"mock\",\"age\":0}";
        final Map<String, Object> result = JsonUtil.toMap(json);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
    }

    @Test
    void testToMap_2() throws Exception {
        final String json = "{\"name\":\"mock\",\"age\":0}";
        final Class keyTypeClass = Object.class;
        final Class valueTypeClass = Object.class;
        final Map result = JsonUtil.toMap(json, keyTypeClass, valueTypeClass);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
    }

    @Test
    void testToArray() throws Exception {
        final String json = "[]";
        final Class valueTypeClass = Object.class;
        final Object[] result = JsonUtil.toArray(json, valueTypeClass);
        Assertions.assertNotNull(result);
    }

    @Test
    void testToList_1() throws Exception {
        final String json = "[]";
        final Class clazz = Object.class;
        final List result = JsonUtil.toList(json, clazz);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

}
