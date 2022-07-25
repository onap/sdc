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
package org.openecomp.sdc.be.model.tosca.converters;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.util.Map;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.common.util.GsonFactory;

public class JsonConverter implements PropertyValueConverter {

    private static JsonConverter jsonConverter = new JsonConverter();
    private static Gson gson = GsonFactory.getGson();

    private JsonConverter() {
    }

    public static JsonConverter getInstance() {
        return jsonConverter;
    }

    @Override
    public String convert(String value, String innerType, Map<String, DataTypeDefinition> dataTypes) {
        StringReader reader = new StringReader(value);
        JsonReader jsonReader = new JsonReader(reader);
        jsonReader.setLenient(true);
        JsonElement jsonElement = JsonParser.parseReader(jsonReader);
        if (jsonElement.isJsonPrimitive()) {
            return value;
        }
        return gson.toJson(jsonElement);
    }
}
