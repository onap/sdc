package org.openecomp.sdc.be.model.tosca.converters;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.common.util.JsonUtils;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataTypePropertyConverter {

    private static final DataTypePropertyConverter INSTANCE = new DataTypePropertyConverter();
    private static final JsonParser jsonParser = new JsonParser();
    private static final Gson gson = new Gson();

    private DataTypePropertyConverter() {
    }

    public static DataTypePropertyConverter getInstance() {
        return INSTANCE;
    }

    /**
     *
     * @param propertyDataType the data type
     * @param dataTypes all data types in the system mapped by their name
     * @return a json representation of all the given data type properties default values and recursively their data type properties
     */
    public String getDataTypePropertiesDefaultValuesRec(String propertyDataType, Map<String, DataTypeDefinition> dataTypes) {
        JsonObject defaultValues = getDataTypePropsDefaultValuesRec(propertyDataType, dataTypes);
        return !JsonUtils.isJsonNullOrEmpty(defaultValues) ? gson.toJson(defaultValues) : null;
    }

    /**
     * Takes a json representation of a tosca property value and merges all the default values of the property data type
     * @param value the json representation of a tosca property value
     * @param propertyDataType data type from which to merge default values
     * @param dataTypes all data types in the system mapped by their name
     * for example: for value {a: {b: c}} we could have a default value {a: {d: e}} (property a has two sub properties but only b was overridden)
     * so the complete value is {a: {b: c, d: e}}
     */
    public void mergeDataTypeDefaultValuesWithPropertyValue(JsonObject value, String propertyDataType, Map<String, DataTypeDefinition> dataTypes) {
        JsonObject dataTypeDefaultValues = getDataTypePropsDefaultValuesRec(propertyDataType, dataTypes);
        mergeDefaultValuesRec(dataTypeDefaultValues, value);
    }

    private void mergeDefaultValuesRec(JsonObject defaultValue, JsonObject value) {
        if (ToscaConverterUtils.isGetInputValue(value)) {
            return;
        }
        for (Map.Entry<String, JsonElement> defVal : defaultValue.entrySet()) {
            mergeDefaultValue(value, defVal.getKey(), defVal.getValue());
        }
    }

    private void mergeDefaultValue(JsonObject value, String propName, JsonElement defValue) {
        if (defValueWasNotOverridden(value, propName)) {
            value.add(propName, defValue);
        } else if (notPrimitivePropValue(value, defValue, propName)) {
            JsonElement propValue = value.get(propName);
            mergeDefaultValuesRec(defValue.getAsJsonObject(), propValue.getAsJsonObject());
        }
    }

    private boolean notPrimitivePropValue(JsonObject value, JsonElement defValue, String propName) {
        return value.get(propName).isJsonObject() && defValue.isJsonObject();
    }

    private boolean defValueWasNotOverridden(JsonObject value, String propName) {
        return !value.has(propName);
    }

    private JsonObject getDataTypePropsDefaultValuesRec(String propertyDataType, Map<String, DataTypeDefinition> dataTypes) {
        Map<String, PropertyDefinition> allParentsProps = getAllDataTypeProperties(dataTypes.get(propertyDataType));
        JsonObject dataTypeDefaultsJson = new JsonObject();
        for (Map.Entry<String, PropertyDefinition> propertyEntry : allParentsProps.entrySet()) {
            PropertyDefinition propertyDefinition = propertyEntry.getValue();
            addDefaultValueToJson(dataTypes, dataTypeDefaultsJson, propertyDefinition);
        }
        return dataTypeDefaultsJson;
    }

    private void addDefaultValueToJson(Map<String, DataTypeDefinition> dataTypes, JsonObject dataTypePropsDefaults, PropertyDefinition propertyDefinition) {
        String propName = propertyDefinition.getName();
        JsonElement defVal = getDefaultValue(dataTypes, dataTypePropsDefaults, propertyDefinition);
        if (!JsonUtils.isEmptyJson(defVal)) {
            dataTypePropsDefaults.add(propName, defVal);
        }
    }

    private JsonElement getDefaultValue(Map<String, DataTypeDefinition> dataTypes, JsonObject dataTypePropsDefaults, PropertyDefinition propertyDefinition) {
        JsonElement defVal = new JsonObject();
        String propName = propertyDefinition.getName();
        String propDefaultVal = propertyDefinition.getDefaultValue();
        if(!JsonUtils.containsEntry(dataTypePropsDefaults, propName) && propDefaultVal != null){
            defVal = convertToJson(propDefaultVal);
        } else if (!JsonUtils.containsEntry(dataTypePropsDefaults, propName)) {
            defVal = getDataTypePropsDefaultValuesRec(propertyDefinition.getType(), dataTypes);
        }
        return defVal;
    }

    private JsonElement convertToJson(String propDefaultVal) {
        JsonReader jsonReader = new JsonReader(new StringReader(propDefaultVal));
        jsonReader.setLenient(true);
        return jsonParser.parse(jsonReader);
    }

    private Map<String, PropertyDefinition> getAllDataTypeProperties(DataTypeDefinition dataTypeDefinition) {
        Map<String, PropertyDefinition> allParentsProps = new HashMap<>();
        while (dataTypeDefinition != null) {

            List<PropertyDefinition> currentParentsProps = dataTypeDefinition.getProperties();
            if (currentParentsProps != null) {
                currentParentsProps.stream().forEach(p -> allParentsProps.put(p.getName(), p));
            }

            dataTypeDefinition = dataTypeDefinition.getDerivedFrom();
        }
        return allParentsProps;
    }

}
