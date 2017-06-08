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

package org.openecomp.core.utilities.json;

import org.json.JSONException;
import org.json.JSONObject;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class JsonSchemaDataGenerator {

  private static final String ROOT = "root";
  private static final Logger logger =
      (Logger) LoggerFactory.getLogger(JsonSchemaDataGenerator.class);
  boolean includeDefaults = true;
  private JSONObject root;
  private Map<String, Object> referencesData;

  /**
   * Instantiates a new Json schema data generator.
   *
   * @param jsonSchema the json schema
   */
  public JsonSchemaDataGenerator(String jsonSchema) {
    if (jsonSchema == null) {
      throw new IllegalArgumentException("Input string jsonSchema can not be null");
    }
    root = new JSONObject(jsonSchema);
  }

  public void setIncludeDefaults(boolean includeDefaults) {
    this.includeDefaults = includeDefaults;
  }

  /**
   * Generates json data that conform to the schema according to turned on flags.
   *
   * @return json that conform to the schema
   */
  public String generateData() {
    referencesData = new HashMap<>();
    JSONObject data = new JSONObject();

    generateData(ROOT, root,
        data);
    // "root" is dummy name to represent the top level object
    // (which, as apposed to inner objects, doesn't have a name in the schema)
    return data.has(ROOT) ? data.get(ROOT).toString() : data.toString();
  }

  private void generateData(String propertyName, JSONObject property, JSONObject propertyData) {
    if (property.has(JsonSchemaKeyword.TYPE)) {
      String propertyType = property.getString(JsonSchemaKeyword.TYPE);
      if (JsonSchemaKeyword.OBJECT.equals(propertyType)) {
        generateObjectData(propertyName, property, propertyData);
      } else {
        generatePrimitiveData(propertyType, propertyName, property, propertyData);
      }
    } else if (property.has(JsonSchemaKeyword.REF)) {
      generateReferenceData(propertyName, property.getString(JsonSchemaKeyword.REF), propertyData);
    }
  }

  private void generateObjectData(String propertyName, JSONObject property,
                                  JSONObject propertyData) {
    JSONObject subProperties = property.getJSONObject(JsonSchemaKeyword.PROPERTIES);

    JSONObject subPropertiesData = new JSONObject();
    for (String subPropertyName : subProperties.keySet()) {
      generateData(subPropertyName, subProperties.getJSONObject(subPropertyName),
          subPropertiesData);
    }

    if (subPropertiesData.length() > 0) {
      propertyData.put(propertyName, subPropertiesData);
    }
  }

  private void generateReferenceData(String propertyName, String referencePath,
                                     JSONObject propertyData) {
    if (referencesData.containsKey(referencePath)) {
      Object referenceData = referencesData.get(referencePath);
      if (referenceData != null) {
        propertyData.put(propertyName, referenceData);
      }
    } else {
      generateData(propertyName, resolveReference(referencePath), propertyData);
      referencesData.put(referencePath, propertyData.opt(propertyName));
    }
  }

  private JSONObject resolveReference(String referencePath) {
    String[] keys = referencePath.replaceFirst("#/", "").split("/");

    JSONObject reference = root;
    for (String key : keys) {
      reference = reference.getJSONObject(key);
    }
    return reference;
  }

  private void generatePrimitiveData(String propertyType, String propertyName, JSONObject property,
                                     JSONObject propertyData) {
    if (includeDefaults) {
      populateWithDefaultValue(propertyType, propertyName, property, propertyData);
    }
  }

  private void populateWithDefaultValue(String propertyType, String propertyName,
                                        JSONObject property, JSONObject propertyData) {
    if (!property.has(JsonSchemaKeyword.DEFAULT)) {
      return;
    }
    try {
      switch (propertyType) {
        case JsonSchemaKeyword.ARRAY:
          propertyData.put(propertyName, property.getJSONArray(JsonSchemaKeyword.DEFAULT));
          break;
        case JsonSchemaKeyword.BOOLEAN:
          propertyData.put(propertyName, property.getBoolean(JsonSchemaKeyword.DEFAULT));
          break;
        case JsonSchemaKeyword.INTEGER:
          propertyData.put(propertyName, property.getInt(JsonSchemaKeyword.DEFAULT));
          break;
        case JsonSchemaKeyword.NUMBER:
          propertyData.put(propertyName, property.getDouble(JsonSchemaKeyword.DEFAULT));
          break;
        case JsonSchemaKeyword.STRING:
          propertyData.put(propertyName, property.getString(JsonSchemaKeyword.DEFAULT));
          break;
        default:
          break;
      }
    } catch (JSONException exception) {
      Object defaultValue = property.get(JsonSchemaKeyword.DEFAULT);
      logger.error(String.format(
          "Invalid schema: '%s' property type is '%s' but it has a default value which is not: %s.",
          propertyName, propertyType, defaultValue), exception);
      throw exception;
    }
  }

  private static class JsonSchemaKeyword {
    private static final String DEFAULT = "default";
    private static final String TYPE = "type";
    private static final String PROPERTIES = "properties";
    private static final String ARRAY = "array";
    private static final String BOOLEAN = "boolean";
    private static final String INTEGER = "integer";
    private static final String NUMBER = "number";
    private static final String STRING = "string";
    private static final String OBJECT = "object";
    private static final String REF = "$ref";
  }
}
