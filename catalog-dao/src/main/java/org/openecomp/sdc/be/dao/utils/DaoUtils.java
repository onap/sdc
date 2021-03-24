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
package org.openecomp.sdc.be.dao.utils;

import com.google.gson.Gson;

/**
 * @author Evgenia Alberstein
 * <p>
 * Utility class for convertation to/from JSON string
 */
public class DaoUtils {

    private DaoUtils() {
    }

    /**
     * Convert from Object to Json string
     *
     * @param object
     * @return json string
     */
    public static String convertToJson(Object object) {
        if (object == null) {
            throw new RuntimeException("The object cannot be NULL!!!");
        }
        Gson gson = new Gson(); // Or use new GsonBuilder().create();

        return gson.toJson(object); // serializes target to Json
    }

    /**
     * Convert from Json string to object
     *
     * @param clazz
     * @param json
     * @return object
     */
    public static <T> T convertFromJson(Class<T> clazz, String json) {
        if (clazz == null) {
            throw new RuntimeException("The Class cannot be NULL!!!");
        }
        Gson gson = new Gson(); // Or use new GsonBuilder().create();

        return gson.fromJson(json, clazz); // deserializes json into target2
    }
}
