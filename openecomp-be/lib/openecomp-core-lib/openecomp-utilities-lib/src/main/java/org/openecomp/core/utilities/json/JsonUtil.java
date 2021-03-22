/*
 * Copyright © 2018 European Support Limited
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
package org.openecomp.core.utilities.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.everit.json.schema.EnumSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.onap.sdc.tosca.datatypes.model.RequirementDefinition;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.utilities.deserializers.RequirementDefinitionDeserializer;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

/**
 * The type Json util.
 */
public class JsonUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);
    private static final GsonBuilder gsonBuilder;
    private static final Gson gson;

    static {
        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(RequirementDefinition.class, new RequirementDefinitionDeserializer());
        gson = gsonBuilder.create();
    }

    private JsonUtil() {
    }

    /**
     * Object 2 json string.
     *
     * @param obj the obj
     * @return the string
     */
    public static String object2Json(Object obj) {
        return sbObject2Json(obj).toString();
    }

    /**
     * Sb object 2 json string builder.
     *
     * @param obj the obj
     * @return the string builder
     */
    public static StringBuilder sbObject2Json(Object obj) {
        return new StringBuilder(new GsonBuilder().setPrettyPrinting().create().toJson(obj));
    }

    /**
     * Json 2 object t.
     *
     * @param <T>      the type parameter
     * @param json     the json
     * @param classOfT the class of t
     * @return the t
     */
    public static <T> T json2Object(String json, Class<T> classOfT) {
        T typ;
        try {
            try (Reader br = new StringReader(json)) {
                typ = gson.fromJson(br, classOfT);
            }
        } catch (JsonIOException | JsonSyntaxException | IOException exception) {
            throw new RuntimeException(exception);
        }
        return typ;
    }

    /**
     * Json 2 object t.
     *
     * @param <T>      the type parameter
     * @param is       the is
     * @param classOfT the class of t
     * @return the t
     */
    public static <T> T json2Object(InputStream is, Class<T> classOfT) {
        T type;
        try (Reader br = new BufferedReader(new InputStreamReader(is))) {
            type = new Gson().fromJson(br, classOfT);
        } catch (JsonIOException | JsonSyntaxException | IOException exception) {
            throw new RuntimeException(exception);
        }
        return type;
    }

    /**
     * Is valid json boolean.
     *
     * @param json the json
     * @return the boolean
     */

    //todo check https://github.com/stleary/JSON-java as replacement for this code
    public static boolean isValidJson(String json) {
        try {
            return new JsonParser().parse(json).isJsonObject();
        } catch (JsonSyntaxException jse) {
            LOGGER.error("Invalid json, Failed to parse json", jse);
            return false;
        }
    }

    /**
     * Validate list.
     *
     * @param json       the json
     * @param jsonSchema the json schema
     * @return the list
     */
    public static List<String> validate(String json, String jsonSchema) {
        List<ValidationException> validationErrors = validateUsingEverit(json, jsonSchema);
        return validationErrors == null ? null
            : validationErrors.stream().map(JsonUtil::mapValidationExceptionToMessage).collect(Collectors.toList());
    }

    private static String mapValidationExceptionToMessage(ValidationException exception) {
        Object schema = exception.getViolatedSchema();
        if (schema instanceof EnumSchema) {
            return mapEnumViolationToMessage(exception);
        } else if (schema instanceof StringSchema) {
            return mapStringViolationToMessage(exception);
        }
        return exception.getMessage();
    }

    private static String mapEnumViolationToMessage(ValidationException exception) {
        Set<Object> possibleValues = ((EnumSchema) exception.getViolatedSchema()).getPossibleValues();
        return exception.getMessage().replaceFirst("enum value",
            possibleValues.size() == 1 ? String.format("value. %s is the only possible value for this field", possibleValues.iterator().next())
                : String.format("value. Possible values: %s",
                    CommonMethods.collectionToCommaSeparatedString(possibleValues.stream().map(Object::toString).collect(Collectors.toList()))));
    }

    private static String mapStringViolationToMessage(ValidationException validationException) {
        if (ValidationType.PATTERN.getKeyword().equals(validationException.getKeyword())) {
            String message = validationException.getMessage();
            String value = message.substring(message.indexOf("["), message.indexOf("]") + 1);
            return message.replace("string " + value, value + " is not valid value. It");
        }
        return validationException.getMessage();
    }

    private static List<ValidationException> validateUsingEverit(String json, String jsonSchema) {
        LOGGER.debug(String.format("validateUsingEverit start, json=%s, jsonSchema=%s", json, jsonSchema));
        if (json == null || jsonSchema == null) {
            throw new IllegalArgumentException("Input strings json and jsonSchema can not be null");
        }
        Schema schemaObj = SchemaLoader.load(new JSONObject(jsonSchema));
        try {
            schemaObj.validate(new JSONObject(json));
        } catch (ValidationException ve) {
            return CollectionUtils.isEmpty(ve.getCausingExceptions()) ? Collections.singletonList(ve) : ve.getCausingExceptions();
        }
        return null;
    }

    private enum ValidationType {
        PATTERN("pattern");
        private String keyword;

        ValidationType(String keyword) {
            this.keyword = keyword;
        }

        String getKeyword() {
            return keyword;
        }
    }
}
