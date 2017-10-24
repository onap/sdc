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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import fj.data.Either;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.converters.DataTypePropertyConverter;
import org.openecomp.sdc.be.model.tosca.converters.ToscaMapValueConverter;
import org.openecomp.sdc.be.model.tosca.converters.ToscaValueConverter;
import org.openecomp.sdc.be.tosca.model.EntrySchema;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyConvertor {
	private static PropertyConvertor instance;
	private JsonParser jsonParser = new JsonParser();
	private static Logger log = LoggerFactory.getLogger(PropertyConvertor.class.getName());
	Gson gson = new Gson();
	protected PropertyConvertor() {

	}

	public static synchronized PropertyConvertor getInstance() {
		if (instance == null) {
			instance = new PropertyConvertor();
		}
		return instance;
	}

	public Either<ToscaNodeType, ToscaError> convertProperties(Component component, ToscaNodeType toscaNodeType, Map<String, DataTypeDefinition> dataTypes) {

		if (component instanceof Resource) {
			Resource resource = (Resource) component;
			List<PropertyDefinition> props = resource.getProperties();
			if (props != null) {
				Map<String, ToscaProperty> properties = new HashMap<>();

				// take only the properties of this resource
				props.stream().filter(p -> p.getOwnerId() == null || p.getOwnerId().equals(component.getUniqueId())).forEach(property -> {
					ToscaProperty prop = convertProperty(dataTypes, property, false);

					if (prop != null) {
					    properties.put(property.getName(), prop);
                    }
				});
				if (!properties.isEmpty()) {
					toscaNodeType.setProperties(properties);
				}
			}
		}
		return Either.left(toscaNodeType);
	}

	public ToscaProperty convertProperty(Map<String, DataTypeDefinition> dataTypes, PropertyDefinition property, boolean isCapabiltyProperty) {
		ToscaProperty prop = new ToscaProperty();

		String innerType = null;
		SchemaDefinition schema = property.getSchema();
		if (schema != null && schema.getProperty() != null && schema.getProperty().getType() != null && !schema.getProperty().getType().isEmpty()) {
			innerType = schema.getProperty().getType();
			EntrySchema eschema = new EntrySchema();
			eschema.setType(innerType);
			eschema.setDescription(schema.getProperty().getDescription());
			prop.setEntry_schema(eschema);
		}
		log.trace("try to convert property {} from type {} with default value [{}]", property.getName(), property.getType(), property.getDefaultValue());
		prop.setDefaultp(convertToToscaObject(property.getType(), property.getDefaultValue(), innerType, dataTypes));
		
		if (prop.getDefaultp() == null) {
		    return null;
        }
		prop.setType(property.getType());
        prop.setDescription(property.getDescription());
        if (isCapabiltyProperty) {
            prop.setStatus(property.getStatus());
            prop.setRequired(property.isRequired());
        }
        return prop;
	}

	public Object convertToToscaObject(String propertyType, String value, String innerType, Map<String, DataTypeDefinition> dataTypes) {
		log.trace("try to convert propertyType {} , value [{}], innerType {}", propertyType, value, innerType);
		if (StringUtils.isEmpty(value)) {
			value = DataTypePropertyConverter.getInstance().getDataTypePropertiesDefaultValuesRec(propertyType, dataTypes);
			if(StringUtils.isEmpty(value)){
				return null;
			}

		}
		try {
			ToscaMapValueConverter mapConverterInst = ToscaMapValueConverter.getInstance();
			ToscaValueConverter innerConverter = null;
			Boolean isScalar = true;
	
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
				if (ToscaPropertyType.STRING.equals(type) && value.startsWith("/")) {
					return innerConverter.convertToToscaValue(value, innerType, dataTypes);
				}
			}
			JsonElement jsonElement = null;
		
			StringReader reader = new StringReader(value);
			JsonReader jsonReader = new JsonReader(reader);
			jsonReader.setLenient(true);

			jsonElement = jsonParser.parse(jsonReader);

			if (value.equals("")) {
				return value;
			}

			if (jsonElement.isJsonPrimitive() && isScalar) {
				log.trace("It's well defined type. convert it");
				ToscaValueConverter converter = type.getValueConverter();
				return converter.convertToToscaValue(value, innerType, dataTypes);
			} else {
				log.trace("It's data type or inputs in primitive type. convert as map");
				Object convertedValue;
				if (innerConverter != null && (ToscaPropertyType.MAP.equals(type) || ToscaPropertyType.LIST.equals(type))) {
					convertedValue = innerConverter.convertToToscaValue(value, innerType, dataTypes);
				} else {
					if (isScalar) {
						// complex json for scalar type
						convertedValue = mapConverterInst.handleComplexJsonValue(jsonElement);
					} else {
						if (innerConverter != null) {
							convertedValue = innerConverter.convertToToscaValue(value, innerType, dataTypes);
						} else {
							convertedValue = mapConverterInst.convertDataTypeToToscaObject(innerType, dataTypes, innerConverter, isScalar, jsonElement);
						}
					}
				}
				return convertedValue;
			}

		} catch (Exception e) {
			log.debug("convertToToscaValue failed to parse json value :", e);
			return null;
		}

	}

}
