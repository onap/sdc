/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.openecomp.sdc.be.dao.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class DaoUtilsTest {

    private static Map<String, String> testObject = Collections.singletonMap("key", "value");

    private static final String json = "{\"key\":\"value\"}";

    @Test
    public void testConvertToJson() {

        String result = DaoUtils.convertToJson(testObject);
        Assert.assertEquals(json, result);

        try {
            DaoUtils.convertToJson(null);
            fail("The exception wasn't thrown!");
        } catch (RuntimeException e) {

            assertEquals("The object cannot be NULL!!!", e.getMessage());
        }
    }

    @Test
    public void testConvertFromJson() {
        Class<Map> clazz = Map.class;

        // default test
        Map result = DaoUtils.convertFromJson(clazz, json);
        assertEquals(testObject, result);

        try {
            DaoUtils.convertFromJson(null, json);
            fail("The exception wasn't thrown!");
        } catch (RuntimeException e) {

            assertEquals("The Class cannot be NULL!!!", e.getMessage());
        }
    }
}