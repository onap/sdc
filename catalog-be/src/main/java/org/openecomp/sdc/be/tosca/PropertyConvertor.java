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
package org.openecomp.sdc.be.tosca;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import fj.data.Either;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunctionType;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.converters.DataTypePropertyConverter;
import org.openecomp.sdc.be.model.tosca.converters.ToscaMapValueConverter;
import org.openecomp.sdc.be.model.tosca.converters.ToscaValueBaseConverter;
import org.openecomp.sdc.be.model.tosca.converters.ToscaValueConverter;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.openecomp.sdc.be.tosca.model.ToscaSchemaDefinition;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

@Service
public class PropertyConvertor {

    private static final Logger log = Logger.getLogger(PropertyConvertor.class);

    public Either<ToscaNodeType, ToscaError> convertProperties(Component component, ToscaNodeType toscaNodeType,
                                                               Map<String, DataTypeDefinition> dataTypes) {
        if (component instanceof Resource) {
            Resource resource = (Resource) component;
            List<PropertyDefinition> props = resource.getProperties();
            if (props != null) {
                Map<String, ToscaProperty> properties = new HashMap<>();
                // take only the properties of this resource
                props.stream().filter(p -> p.getOwnerId() == null || p.getOwnerId().equals(component.getUniqueId())).forEach(property -> {
                    properties.put(property.getName(), convertProperty(dataTypes, property, PropertyType.PROPERTY));
                });
                if (!properties.isEmpty()) {
                    toscaNodeType.setProperties(properties);
                }
            }
        }
        return Either.left(toscaNodeType);
    }

    public ToscaProperty convertProperty(Map<String, DataTypeDefinition> dataTypes, PropertyDefinition property, PropertyType propertyType) {
        ToscaProperty prop = new ToscaProperty();
        log.trace("try to convert property {} from type {} with default value [{}]", property.getName(), property.getType(),
            property.getDefaultValue());
        SchemaDefinition schema = property.getSchema();
        if (schema != null && schema.getProperty() != null && schema.getProperty().getType() != null && !schema.getProperty().getType().isEmpty()) {
            final ToscaSchemaDefinition toscaSchemaDefinition = new ToscaSchemaDefinition();
            toscaSchemaDefinition.setType(schema.getProperty().getType());
            toscaSchemaDefinition.setDescription(schema.getProperty().getDescription());
            prop.setEntry_schema(toscaSchemaDefinition);
        }
        String defaultValue = property.getDefaultValue();
        if (Objects.isNull(defaultValue)) {
            defaultValue = property.getValue();
        }
        Object convertedObj = convertToToscaObject(property, defaultValue, dataTypes, false);
        if (convertedObj != null) {
            prop.setDefaultp(convertedObj);
        }
        prop.setType(property.getType());
        prop.setDescription(property.getDescription());
        prop.setRequired(property.isRequired());
        if (propertyType.equals(PropertyType.CAPABILITY)) {
            prop.setStatus(property.getStatus());
        }
        prop.setMetadata(property.getMetadata());
        return prop;
    }

    public Object convertToToscaObject(PropertyDataDefinition property, String value, Map<String, DataTypeDefinition> dataTypes,
                                       boolean preserveEmptyValue) {
        String propertyType = property.getType();
        String innerType = property.getSchemaType();
        log.trace("try to convert propertyType {} , value [{}], innerType {}", propertyType, value, innerType);
        if (StringUtils.isEmpty(value)) {
            value = DataTypePropertyConverter.getInstance().getDataTypePropertiesDefaultValuesRec(propertyType, dataTypes);
            if (StringUtils.isEmpty(value)) {
                return null;
            }
        }
        if (property.isToscaFunction() && property.getToscaFunction().getType() == ToscaFunctionType.YAML) {
            return new Yaml().load(property.getValue());
        }
        try {
            ToscaMapValueConverter mapConverterInst = ToscaMapValueConverter.getInstance();
            ToscaValueConverter innerConverter = null;
            boolean isScalar = true;
            ToscaPropertyType type = ToscaPropertyType.isValidType(propertyType);
            if (type == null) {
                log.trace("isn't prederfined type, get from all data types");
                DataTypeDefinition dataTypeDefinition = dataTypes.get(propertyType);
                if (innerType == null) {
                    innerType = propertyType;
                }
                if ((type = mapConverterInst.isScalarType(dataTypeDefinition)) != null) {
                    log.trace("This is scalar type. get suitable converter for type {}", type);
                    innerConverter = type.getValueConverter();
                } else {
                    isScalar = false;
                }
            } else {
                ToscaPropertyType typeIfScalar = ToscaPropertyType.getTypeIfScalar(type.getType());
                if (typeIfScalar == null) {
                    isScalar = false;
                }
                innerConverter = type.getValueConverter();
                if (ToscaPropertyType.STRING == type && valueStartsWithNonJsonChar(value)) {
                    return innerConverter.convertToToscaValue(value, innerType, dataTypes);
                }
            }
            JsonElement jsonElement = null;
            StringReader reader = new StringReader(value);
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(true);
            jsonElement = JsonParser.parseReader(jsonReader);
            if (value.equals("")) {
                return value;
            }
            if (jsonElement.isJsonPrimitive() && isScalar) {
                log.trace("It's well defined type. convert it");
                ToscaValueConverter converter = type.getValueConverter();
                return converter.convertToToscaValue(value, innerType, dataTypes);
            }
            log.trace("It's data type or inputs in primitive type. convert as map");
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObj = jsonElement.getAsJsonObject();
                // check if value is a get_input function
                if (jsonObj.entrySet().size() == 1 && jsonObj.has(ToscaFunctions.GET_INPUT.getFunctionName())) {
                    Object obj = mapConverterInst.handleComplexJsonValue(jsonElement);
                    log.debug("It's get_input function. obj={}", obj);
                    return obj;
                }
            }
            Object convertedValue;
            if (innerConverter != null && (ToscaPropertyType.MAP == type || ToscaPropertyType.LIST == type)) {
                convertedValue = innerConverter.convertToToscaValue(value, innerType, dataTypes);
            } else if (isScalar) {
                // complex json for scalar type
                convertedValue = mapConverterInst.handleComplexJsonValue(jsonElement);
            } else if (innerConverter != null) {
                convertedValue = innerConverter.convertToToscaValue(value, innerType, dataTypes);
            } else {
                convertedValue = mapConverterInst
                    .convertDataTypeToToscaObject(innerType, dataTypes, innerConverter, isScalar, jsonElement, preserveEmptyValue);
            }
            return convertedValue;
        
        } catch (JsonParseException e) {
            log.trace("{} not parsable as JSON. Convert as YAML instead", value);
            return  new Yaml().load(value);
        } catch (Exception e) {
            log.debug("convertToToscaValue failed to parse json value :", e);
            return null;
        }
    }

    private boolean valueStartsWithNonJsonChar(String value) {
        return value.startsWith("/") || value.startsWith(":");
    }

    public void convertAndAddValue(Map<String, DataTypeDefinition> dataTypes, Map<String, Object> props, PropertyDataDefinition prop,
                                   Supplier<String> supplier) {
        Object convertedValue = convertValue(dataTypes, prop, supplier);
        if (!ToscaValueBaseConverter.isEmptyObjectValue(convertedValue)) {
            props.put(prop.getName(), convertedValue);
        }
    }

    private <T extends PropertyDataDefinition> Object convertValue(Map<String, DataTypeDefinition> dataTypes, T input, Supplier<String> supplier) {
        return convertToToscaObject(input, supplier.get(), dataTypes, false);
    }

    public enum PropertyType {CAPABILITY, INPUT, PROPERTY}
}
