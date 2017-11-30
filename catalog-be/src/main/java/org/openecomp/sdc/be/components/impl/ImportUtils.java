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

package org.openecomp.sdc.be.components.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang3.StringEscapeUtils;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.heat.HeatParameterType;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation.PropertyConstraintDeserialiser;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.util.GsonFactory;
import org.springframework.beans.factory.config.YamlProcessor;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import fj.data.Either;

public final class ImportUtils {
	private ImportUtils() {

	}
	
	private static CustomResolver customResolver = new CustomResolver();
	private static Yaml STRICT_MAPPING_YAML_LOADER =  new YamlLoader().getStrictYamlLoader();

	private static class CustomResolver extends Resolver {
		@Override
		protected void addImplicitResolvers() {
			// avoid implicit resolvers for strings that can be interpreted as boolean values
			addImplicitResolver(Tag.STR, EMPTY, "");
			addImplicitResolver(Tag.STR, NULL, null);
			addImplicitResolver(Tag.NULL, NULL, "~nN\0");
			addImplicitResolver(Tag.NULL, EMPTY, null);
			addImplicitResolver(Tag.INT, INT, "-+0123456789");
			addImplicitResolver(Tag.FLOAT, FLOAT, "-+0123456789.");
			addImplicitResolver(Tag.YAML, YAML, "!&*");
		}
	}


	private static void buildMap(Map<String, Object> output, Map<String, Object> map) {
		for (Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof Map) {
				Map<String, Object> result = new LinkedHashMap<>();
				buildMap(result, (Map) value);
				output.put(key, result);
			}
			else if (value instanceof Collection) {
				Map<String, Object> result = new LinkedHashMap<>();
				int i = 0;
				for(Object item : (Collection<Object>) value) {
					buildMap(result, Collections.singletonMap("[" + (i++) + "]", item));
				}
				output.put(key, new ArrayList<>(result.values()));
			}
			else {
				output.put(key, value);
			}
		}
	}

	public static Map<String, Object> loadYamlAsStrictMap(String content){
		Map<String, Object> result = new LinkedHashMap<>();
		Object map = STRICT_MAPPING_YAML_LOADER.load(content);
		buildMap(result, (Map<String, Object>)map);
		return result;
	}

	private static class YamlLoader extends YamlProcessor {
		public Yaml getStrictYamlLoader() {
			return createYaml();
		}
	}

	@SuppressWarnings("unchecked")
	public static Either<List<HeatParameterDefinition>, ResultStatusEnum> getHeatParamsWithoutImplicitTypes(String heatDecodedPayload, String artifactType) {
		Map<String, Object> heatData = (Map<String, Object>) new Yaml(new Constructor(), new Representer(), new DumperOptions(), customResolver).load(heatDecodedPayload);	
		return getHeatParameters(heatData, artifactType);
	}

	public static class Constants {

		public static final String FIRST_CERTIFIED_VERSION_VERSION = "1.0";
		public static final String FIRST_NON_CERTIFIED_VERSION = "0.1";
		public static final String VENDOR_NAME = "ATT (Tosca)";
		public static final String VENDOR_RELEASE = "1.0.0.wd03";
		public static final LifecycleStateEnum NORMATIVE_TYPE_LIFE_CYCLE = LifecycleStateEnum.CERTIFIED;
		public static final LifecycleStateEnum NORMATIVE_TYPE_LIFE_CYCLE_NOT_CERTIFIED_CHECKOUT = LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT;
		public static final boolean NORMATIVE_TYPE_HIGHEST_VERSION = true;
		// public static final String ABSTRACT_CATEGORY = "Generic/Abstract";
		public static final String ABSTRACT_CATEGORY_NAME = "Generic";
		public static final String ABSTRACT_SUBCATEGORY = "Abstract";
		public static final String DEFAULT_ICON = "defaulticon";
		public static final String INNER_VFC_DESCRIPTION = "Not reusable inner VFC";
		public static final String USER_DEFINED_RESOURCE_NAMESPACE_PREFIX = "org.openecomp.resource.";
		public static final String TOSCA_SIMPLE_YAML_PREFIX = "tosca_simple_yaml_";
		public static final List<String> TOSCA_DEFINITION_VERSIONS = Arrays.asList(TOSCA_SIMPLE_YAML_PREFIX + "1_0_0", TOSCA_SIMPLE_YAML_PREFIX + "1_1_0", "tosca_simple_profile_for_nfv_1_0_0", TOSCA_SIMPLE_YAML_PREFIX + "1_0", TOSCA_SIMPLE_YAML_PREFIX + "1_1");
		public static final List<String> TOSCA_YML_CSAR_VALID_SUFFIX = Arrays.asList(".yml", ".yaml", ".csar");
		public static final String UI_JSON_PAYLOAD_NAME = "payloadName";
		public static final String CVFC_DESCRIPTION = "Complex node type that is used as nested type in VF";
	}

	public enum ResultStatusEnum {
		ELEMENT_NOT_FOUND, GENERAL_ERROR, OK, INVALID_PROPERTY_DEFAULT_VALUE, INVALID_PROPERTY_TYPE, INVALID_PROPERTY_VALUE, MISSING_ENTRY_SCHEMA_TYPE, INVALID_PROPERTY_NAME
	}

	public enum ToscaElementTypeEnum {
		BOOLEAN, STRING, MAP, LIST, ALL
	}

	public enum ToscaTagNamesEnum {
		DERIVED_FROM("derived_from"), IS_PASSWORD("is_password"),
		// Properties
		PROPERTIES("properties"), TYPE("type"), STATUS("status"), ENTRY_SCHEMA("entry_schema"), REQUIRED("required"), DESCRIPTION("description"), DEFAULT_VALUE("default"), VALUE("value"), CONSTRAINTS("constraints"),
		// Group Types
		MEMBERS("members"), METADATA("metadata"),
		// Policy Types
		TARGETS("targets"),
		// Capabilities
		CAPABILITIES("capabilities"), VALID_SOURCE_TYPES("valid_source_types"),
		// Requirements
		REQUIREMENTS("requirements"), NODE("node"), RELATIONSHIP("relationship"), CAPABILITY("capability"), INTERFACES("interfaces"),
		// Heat env Validation
		PARAMETERS("parameters"),
		// Import Validations
		TOSCA_VERSION("tosca_definitions_version"), TOPOLOGY_TEMPLATE("topology_template"), NODE_TYPES("node_types"), OCCURRENCES("occurrences"), NODE_TEMPLATES("node_templates"), GROUPS("groups"), INPUTS("inputs"),
		SUBSTITUTION_MAPPINGS("substitution_mappings"),  NODE_TYPE("node_type"),
		// Attributes
		ATTRIBUTES("attributes"), LABEL("label"), HIDDEN("hidden"), IMMUTABLE("immutable"), GET_INPUT("get_input");

		private String elementName;

		private ToscaTagNamesEnum(String elementName) {
			this.elementName = elementName;
		}

		public String getElementName() {
			return elementName;
		}
	}

	@SuppressWarnings("unchecked")
	private static void handleElementNameNotFound(String elementName, Object elementValue, ToscaElementTypeEnum elementType, List<Object> returnedList) {
		if (elementValue instanceof Map) {
			ImportUtils.findToscaElements((Map<String, Object>) elementValue, elementName, elementType, returnedList);
		} else if (elementValue instanceof List) {
			ImportUtils.findAllToscaElementsInList((List<Object>) elementValue, elementName, elementType, returnedList);
		}
	}

	@SuppressWarnings("unchecked")
	private static void handleElementNameFound(String elementName, ToscaElementTypeEnum elementType, List<Object> returnedList, Object elementValue) {

		if (elementValue instanceof Boolean) {
			if (elementType == ToscaElementTypeEnum.BOOLEAN || elementType == ToscaElementTypeEnum.ALL) {
				returnedList.add(elementValue);
			}
		}

		else if (elementValue instanceof String) {
			if (elementType == ToscaElementTypeEnum.STRING || elementType == ToscaElementTypeEnum.ALL) {
				returnedList.add(elementValue);
			}
		} else if (elementValue instanceof Map) {
			if (elementType == ToscaElementTypeEnum.MAP || elementType == ToscaElementTypeEnum.ALL) {
				returnedList.add(elementValue);
			}
			ImportUtils.findToscaElements((Map<String, Object>) elementValue, elementName, elementType, returnedList);

		} else if (elementValue instanceof List) {
			if (elementType == ToscaElementTypeEnum.LIST || elementType == ToscaElementTypeEnum.ALL) {
				returnedList.add(elementValue);
			}
			ImportUtils.findAllToscaElementsInList((List<Object>) elementValue, elementName, elementType, returnedList);

		}
		// For Integer, Double etc...
		else if (elementType == ToscaElementTypeEnum.ALL) {
			if (elementValue != null) {
				returnedList.add(String.valueOf(elementValue));
			} 
		}
	}

	private static void findAllToscaElementsInList(List<Object> list, String elementName, ToscaElementTypeEnum elementType, List<Object> returnedList) {
		Iterator<Object> listItr = list.iterator();
		while (listItr.hasNext()) {
			Object elementValue = listItr.next();
			handleElementNameNotFound(elementName, elementValue, elementType, returnedList);
		}

	}

	public static Either<Object, ResultStatusEnum> findToscaElement(Map<String, Object> toscaJson, ToscaTagNamesEnum elementName, ToscaElementTypeEnum elementType) {
		List<Object> foundElements = new ArrayList<>();
		Either<Object, ResultStatusEnum> returnedElement = Either.right(ResultStatusEnum.ELEMENT_NOT_FOUND);
		ImportUtils.findToscaElements(toscaJson, elementName.getElementName(), elementType, foundElements);
		if (foundElements.size() > 0) {
			returnedElement = Either.left(foundElements.get(0));
		}
		return returnedElement;

	}

	/**
	 * Recursively searches for all tosca elements with key equals to elementName and value equals to elementType. <br>
	 * Returns Either element with:<br>
	 * List with all value if values found<br>
	 * Or ELEMENT_NOT_FOUND ActionStatus
	 * 
	 * @param toscaJson
	 * @param toscaTagName
	 * @return
	 */
	public static Either<List<Object>, ResultStatusEnum> findToscaElements(Map<String, Object> toscaJson, String elementName, ToscaElementTypeEnum elementType, List<Object> returnedList) {
		Either<List<Object>, ResultStatusEnum> returnedElement = Either.right(ResultStatusEnum.ELEMENT_NOT_FOUND);
		String skipKey = null;
		if (toscaJson.containsKey(elementName)) {
			Object elementValue = toscaJson.get(elementName);
			handleElementNameFound(elementName, elementType, returnedList, elementValue);
			skipKey = elementName;
		}

		Iterator<Entry<String, Object>> keyValItr = toscaJson.entrySet().iterator();
		while (keyValItr.hasNext()) {
			Entry<String, Object> keyValEntry = keyValItr.next();
			if (!String.valueOf(keyValEntry.getKey()).equals(skipKey)) {
				handleElementNameNotFound(elementName, keyValEntry.getValue(), elementType, returnedList);
			}
		}

		if (returnedList.size() > 0) {
			returnedElement = Either.left(returnedList);
		}

		return returnedElement;
	}

	@SuppressWarnings("unchecked")
	public static <T> Either<List<T>, ResultStatusEnum> findFirstToscaListElement(Map<String, Object> toscaJson, ToscaTagNamesEnum toscaTagName) {
		Either<List<T>, ResultStatusEnum> returnedElement = Either.right(ResultStatusEnum.ELEMENT_NOT_FOUND);
		Either<Object, ResultStatusEnum> findFirstToscaElement = findToscaElement(toscaJson, toscaTagName, ToscaElementTypeEnum.LIST);
		if (findFirstToscaElement.isLeft()) {
			returnedElement = Either.left((List<T>) findFirstToscaElement.left().value());
		}
		return returnedElement;

	}

	@SuppressWarnings("unchecked")
	public static <T> Either<Map<String, T>, ResultStatusEnum> findFirstToscaMapElement(Map<String, Object> toscaJson, ToscaTagNamesEnum toscaTagName) {
		Either<Map<String, T>, ResultStatusEnum> returnedElement = Either.right(ResultStatusEnum.ELEMENT_NOT_FOUND);
		Either<Object, ResultStatusEnum> findFirstToscaElement = findToscaElement(toscaJson, toscaTagName, ToscaElementTypeEnum.MAP);
		if (findFirstToscaElement.isLeft()) {
			returnedElement = Either.left((Map<String, T>) findFirstToscaElement.left().value());
		}
		return returnedElement;

	}

	public static Either<String, ResultStatusEnum> findFirstToscaStringElement(Map<String, Object> toscaJson, ToscaTagNamesEnum toscaTagName) {
		Either<String, ResultStatusEnum> returnedElement = Either.right(ResultStatusEnum.ELEMENT_NOT_FOUND);
		Either<Object, ResultStatusEnum> findFirstToscaElements = findToscaElement(toscaJson, toscaTagName, ToscaElementTypeEnum.STRING);
		if (findFirstToscaElements.isLeft()) {
			returnedElement = Either.left((String) findFirstToscaElements.left().value());
		}
		return returnedElement;
	}

	/**
	 * searches for first Tosca in Json map (toscaJson) boolean element by name (toscaTagName) returns found element or ELEMENT_NOT_FOUND status
	 * 
	 * @param toscaJson
	 * @param toscaTagName
	 * @return
	 */
	public static Either<String, ResultStatusEnum> findFirstToscaBooleanElement(Map<String, Object> toscaJson, ToscaTagNamesEnum toscaTagName) {
		Either<String, ResultStatusEnum> returnedElement = Either.right(ResultStatusEnum.ELEMENT_NOT_FOUND);
		Either<Object, ResultStatusEnum> findFirstToscaElements = findToscaElement(toscaJson, toscaTagName, ToscaElementTypeEnum.BOOLEAN);
		if (findFirstToscaElements.isLeft()) {
			returnedElement = Either.left(String.valueOf(findFirstToscaElements.left().value()));
		}
		return returnedElement;
	}

	private static void setPropertyConstraints(Map<String, Object> propertyValue, PropertyDefinition property) {
		Either<List<Object>, ResultStatusEnum> propertyFieldconstraints = findFirstToscaListElement(propertyValue, ToscaTagNamesEnum.CONSTRAINTS);
		if (propertyFieldconstraints.isLeft()) {
			List<Object> jsonConstraintList = propertyFieldconstraints.left().value();

			List<PropertyConstraint> constraintList = new ArrayList<>();
			Type constraintType = new TypeToken<PropertyConstraint>() {
			}.getType();
			Gson gson = new GsonBuilder().registerTypeAdapter(constraintType, new PropertyConstraintDeserialiser()).create();

			for (Object constraintJson : jsonConstraintList) {
				PropertyConstraint propertyConstraint = gson.fromJson(gson.toJson(constraintJson), constraintType);
				constraintList.add(propertyConstraint);
			}
			property.setConstraints(constraintList);
		}
	}

	public static PropertyDefinition createModuleProperty(Map<String, Object> propertyValue) {

		PropertyDefinition propertyDef = new PropertyDefinition();
		ImportUtils.setField(propertyValue, ToscaTagNamesEnum.TYPE, type -> propertyDef.setType(type));
		ImportUtils.setPropertyFieldRequired(propertyValue, propertyDef);
		ImportUtils.setField(propertyValue, ToscaTagNamesEnum.DESCRIPTION, desc -> propertyDef.setDescription(desc));

		Either<Object, ResultStatusEnum> findToscaElement = ImportUtils.findToscaElement(propertyValue, ToscaTagNamesEnum.DEFAULT_VALUE, ToscaElementTypeEnum.ALL);
		if (findToscaElement.isLeft()) {
			String propertyJsonStringValue = getPropertyJsonStringValue(findToscaElement.left().value(), propertyDef.getType());
			propertyDef.setDefaultValue(propertyJsonStringValue);
		}
		ImportUtils.setField(propertyValue, ToscaTagNamesEnum.IS_PASSWORD, pass -> propertyDef.setPassword(Boolean.parseBoolean(pass)));
		ImportUtils.setField(propertyValue, ToscaTagNamesEnum.STATUS, status -> propertyDef.setStatus(status));
		ImportUtils.setPropertyScheme(propertyValue, propertyDef);
		ImportUtils.setPropertyConstraints(propertyValue, propertyDef);

		return propertyDef;
	}

	public static InputDefinition createModuleInput(Map<String, Object> inputValue) {

		InputDefinition inputDef = new InputDefinition();
		ImportUtils.setField(inputValue, ToscaTagNamesEnum.TYPE, type -> inputDef.setType(type));
		ImportUtils.setField(inputValue, ToscaTagNamesEnum.REQUIRED, req -> inputDef.setRequired(Boolean.parseBoolean(req)));
		ImportUtils.setField(inputValue, ToscaTagNamesEnum.DESCRIPTION, desc -> inputDef.setDescription(desc));

		Either<Object, ResultStatusEnum> findToscaElement = ImportUtils.findToscaElement(inputValue, ToscaTagNamesEnum.DEFAULT_VALUE, ToscaElementTypeEnum.ALL);
		if (findToscaElement.isLeft()) {
			String propertyJsonStringValue = getPropertyJsonStringValue(findToscaElement.left().value(), inputDef.getType());
			inputDef.setDefaultValue(propertyJsonStringValue);
		}
		ImportUtils.setField(inputValue, ToscaTagNamesEnum.IS_PASSWORD, pass -> inputDef.setPassword(Boolean.parseBoolean(pass)));
		ImportUtils.setField(inputValue, ToscaTagNamesEnum.STATUS, status -> inputDef.setStatus(status));
		ImportUtils.setField(inputValue, ToscaTagNamesEnum.LABEL, label -> inputDef.setLabel(label));
		ImportUtils.setField(inputValue, ToscaTagNamesEnum.HIDDEN, hidden -> inputDef.setHidden(Boolean.parseBoolean(hidden)));
		ImportUtils.setField(inputValue, ToscaTagNamesEnum.HIDDEN, immutable -> inputDef.setImmutable(Boolean.parseBoolean(immutable)));
		ImportUtils.setField(inputValue, ToscaTagNamesEnum.LABEL, label -> inputDef.setLabel(label));
		ImportUtils.setPropertyScheme(inputValue, inputDef);
		ImportUtils.setPropertyConstraints(inputValue, inputDef);

		return inputDef;
	}

	public static PropertyDefinition createModuleAttribute(Map<String, Object> attributeMap) {

		PropertyDefinition attributeDef = new PropertyDefinition();
		ImportUtils.setField(attributeMap, ToscaTagNamesEnum.TYPE, type -> attributeDef.setType(type));
		ImportUtils.setField(attributeMap, ToscaTagNamesEnum.DESCRIPTION, desc -> attributeDef.setDescription(desc));
		ImportUtils.setField(attributeMap, ToscaTagNamesEnum.STATUS, status -> attributeDef.setStatus(status));
		Either<Object, ResultStatusEnum> eitherDefaultValue = ImportUtils.findToscaElement(attributeMap, ToscaTagNamesEnum.DEFAULT_VALUE, ToscaElementTypeEnum.ALL);
		if (eitherDefaultValue.isLeft()) {
			String attributeDefaultValue = getPropertyJsonStringValue(eitherDefaultValue.left().value(), attributeDef.getType());
			attributeDef.setDefaultValue(attributeDefaultValue);
		}
		Either<Object, ResultStatusEnum> eitherValue = ImportUtils.findToscaElement(attributeMap, ToscaTagNamesEnum.VALUE, ToscaElementTypeEnum.ALL);
		if (eitherValue.isLeft()) {
			String attributeValue = getPropertyJsonStringValue(eitherValue.left().value(), attributeDef.getType());
			attributeDef.setValue(attributeValue);
		}
		ImportUtils.setAttributeScheme(attributeMap, attributeDef);
		return attributeDef;
	}

	private static void setPropertyFieldStatus(Map<String, Object> propertyValue, PropertyDefinition propertyDef) {
		Either<String, ResultStatusEnum> propertyFieldIsStatus = findFirstToscaStringElement(propertyValue, ToscaTagNamesEnum.STATUS);
		if (propertyFieldIsStatus.isLeft()) {
			propertyDef.setStatus(propertyFieldIsStatus.left().value());
		}

	}

	private static void setAttributeFieldStatus(Map<String, Object> propertyValue, PropertyDefinition propertyDef) {
		Either<String, ResultStatusEnum> propertyFieldIsStatus = findFirstToscaStringElement(propertyValue, ToscaTagNamesEnum.STATUS);
		if (propertyFieldIsStatus.isLeft()) {
			propertyDef.setStatus(propertyFieldIsStatus.left().value());
		}

	}

	private static void setPropertyScheme(Map<String, Object> propertyValue, PropertyDefinition propertyDefinition) {
		Either<SchemaDefinition, ResultStatusEnum> eitherSchema = getSchema(propertyValue);
		if (eitherSchema.isLeft()) {
			SchemaDefinition schemaDef = new SchemaDefinition();
			schemaDef.setProperty(eitherSchema.left().value().getProperty());
			propertyDefinition.setSchema(schemaDef);
		}

	}

	private static void setAttributeScheme(Map<String, Object> propertyValue, PropertyDefinition propertyDefinition) {
		Either<SchemaDefinition, ResultStatusEnum> eitherSchema = getSchema(propertyValue);
		if (eitherSchema.isLeft()) {
			SchemaDefinition schemaDef = new SchemaDefinition();
			schemaDef.setProperty(eitherSchema.left().value().getProperty());
			propertyDefinition.setSchema(schemaDef);
		}

	}

	private static Either<SchemaDefinition, ResultStatusEnum> getSchema(Map<String, Object> propertyValue) {
		Either<SchemaDefinition, ResultStatusEnum> result = Either.right(ResultStatusEnum.ELEMENT_NOT_FOUND);
		Either<Object, ResultStatusEnum> propertyFieldEntryScheme = findToscaElement(propertyValue, ToscaTagNamesEnum.ENTRY_SCHEMA, ToscaElementTypeEnum.ALL);
		if (propertyFieldEntryScheme.isLeft()) {
			if (propertyFieldEntryScheme.left().value() instanceof String) {
				String schemaType = (String) propertyFieldEntryScheme.left().value();
				SchemaDefinition schema = new SchemaDefinition();
				PropertyDefinition schemeProperty = new PropertyDefinition();
				schemeProperty.setType(schemaType);
				schema.setProperty(schemeProperty);
				result = Either.left(schema);

			} else if (propertyFieldEntryScheme.left().value() instanceof Map) {
				PropertyDefinition schemeProperty = createModuleProperty((Map<String, Object>) propertyFieldEntryScheme.left().value());
				SchemaDefinition schema = new SchemaDefinition();
				schema.setProperty(schemeProperty);
				result = Either.left(schema);

			}

		}
		return result;
	}

	private static void setPropertyFieldIsPassword(Map<String, Object> propertyValue, PropertyDefinition dataDefinition) {
		Either<String, ResultStatusEnum> propertyFieldIsPassword = findFirstToscaStringElement(propertyValue, ToscaTagNamesEnum.IS_PASSWORD);
		if (propertyFieldIsPassword.isLeft()) {
			dataDefinition.setPassword(Boolean.parseBoolean(propertyFieldIsPassword.left().value()));
		}
	}

	private static ResultStatusEnum setPropertyFieldDefaultValue(Map<String, Object> propertyValue, PropertyDefinition dataDefinition) {
		Either<Object, ResultStatusEnum> propertyFieldDefaultValue = findToscaElement(propertyValue, ToscaTagNamesEnum.DEFAULT_VALUE, ToscaElementTypeEnum.ALL);
		Gson gson = GsonFactory.getGson();
		if (propertyFieldDefaultValue.isLeft()) {
			Object defaultValue = propertyFieldDefaultValue.left().value();
			String type = dataDefinition.getType();
			ToscaPropertyType innerToscaType = ToscaPropertyType.isValidType(type);
			// esofer - supporting customized data types. The validation of the
			// type will be in the creation of the property.
			// if(innerToscaType == null){
			// return ResultStatusEnum.MISSING_ENTRY_SCHEMA_TYPE;
			// }
			// customized data types value is represented as json.
			// Also customized data types which are scalar ones, for example,
			// data type which derived from integer, their value will be
			// represented as json.
			if (innerToscaType == null || innerToscaType.equals(ToscaPropertyType.LIST) || innerToscaType.equals(ToscaPropertyType.MAP)) {
				String jsonObj = null;
				if (defaultValue != null) {
					jsonObj = gson.toJson(defaultValue);
				}

				dataDefinition.setDefaultValue(jsonObj);
			} else {
				dataDefinition.setDefaultValue(String.valueOf(defaultValue));
			}

		}

		return ResultStatusEnum.OK;
	}

	private static ResultStatusEnum setAttributeFieldDefaultValue(Map<String, Object> propertyValue, PropertyDefinition dataDefinition) {
		Either<Object, ResultStatusEnum> propertyFieldDefaultValue = findToscaElement(propertyValue, ToscaTagNamesEnum.DEFAULT_VALUE, ToscaElementTypeEnum.ALL);
		Gson gson = GsonFactory.getGson();
		if (propertyFieldDefaultValue.isLeft()) {
			Object defaultValue = propertyFieldDefaultValue.left().value();
			String type = dataDefinition.getType();
			ToscaPropertyType innerToscaType = ToscaPropertyType.isValidType(type);
			// esofer - supporting customized data types. The validation of the
			// type will be in the creation of the property.
			// if(innerToscaType == null){
			// return ResultStatusEnum.MISSING_ENTRY_SCHEMA_TYPE;
			// }
			// customized data types value is represented as json.
			// Also customized data types which are scalar ones, for example,
			// data type which derived from integer, their value will be
			// represented as json.
			if (innerToscaType == null || innerToscaType.equals(ToscaPropertyType.LIST) || innerToscaType.equals(ToscaPropertyType.MAP)) {
				String jsonObj = null;
				if (defaultValue != null) {
					jsonObj = gson.toJson(defaultValue);
				}

				dataDefinition.setDefaultValue(jsonObj);
			} else {
				dataDefinition.setDefaultValue(String.valueOf(defaultValue));
			}

		}

		return ResultStatusEnum.OK;
	}

	public static void setField(Map<String, Object> toscaJson, ToscaTagNamesEnum tagName, Consumer<String> setter) {
		Either<String, ResultStatusEnum> fieldStringValue = findFirstToscaStringElement(toscaJson, tagName);
		if (fieldStringValue.isLeft()) {
			setter.accept(fieldStringValue.left().value());
		}

	}

	private static void setPropertyFieldDescription(Map<String, Object> propertyValue, PropertyDefinition dataDefinition) {
		Either<String, ResultStatusEnum> propertyFieldDescription = findFirstToscaStringElement(propertyValue, ToscaTagNamesEnum.DESCRIPTION);
		if (propertyFieldDescription.isLeft()) {
			dataDefinition.setDescription(propertyFieldDescription.left().value());
		}
	}

	private static void setPropertyFieldRequired(Map<String, Object> propertyValue, PropertyDefinition dataDefinition) {
		Either<String, ResultStatusEnum> propertyFieldRequired = findFirstToscaBooleanElement(propertyValue, ToscaTagNamesEnum.REQUIRED);
		if (propertyFieldRequired.isLeft()) {
			dataDefinition.setRequired(Boolean.parseBoolean(propertyFieldRequired.left().value()));
		}
	}

	private static void setAttributeFieldType(Map<String, Object> propertyValue, PropertyDefinition dataDefinition) {
		Either<String, ResultStatusEnum> propertyFieldType = findFirstToscaStringElement(propertyValue, ToscaTagNamesEnum.TYPE);
		if (propertyFieldType.isLeft()) {
			dataDefinition.setType(propertyFieldType.left().value());
		}
	}

	private static void setPropertyFieldType(Map<String, Object> propertyValue, PropertyDefinition dataDefinition) {
		Either<String, ResultStatusEnum> propertyFieldType = findFirstToscaStringElement(propertyValue, ToscaTagNamesEnum.TYPE);
		if (propertyFieldType.isLeft()) {
			dataDefinition.setType(propertyFieldType.left().value());
		}
	}

	private static void setAttributeFieldDescription(Map<String, Object> propertyValue, PropertyDefinition dataDefinition) {
		Either<String, ResultStatusEnum> propertyFieldDescription = findFirstToscaStringElement(propertyValue, ToscaTagNamesEnum.DESCRIPTION);
		if (propertyFieldDescription.isLeft()) {
			dataDefinition.setDescription(propertyFieldDescription.left().value());
		}
	}

	public static Either<Map<String, PropertyDefinition>, ResultStatusEnum> getProperties(Map<String, Object> toscaJson) {
		Function<String, PropertyDefinition> elementGenByName = elementName -> createProperties(elementName);
		Function<Map<String, Object>, PropertyDefinition> func = map -> createModuleProperty(map);

		return getElements(toscaJson, ToscaTagNamesEnum.PROPERTIES, elementGenByName, func);

	}

	public static Either<Map<String, InputDefinition>, ResultStatusEnum> getInputs(Map<String, Object> toscaJson) {
		Function<String, InputDefinition> elementGenByName = elementName -> createInputs(elementName);
		Function<Map<String, Object>, InputDefinition> func = map -> createModuleInput(map);

		return getElements(toscaJson, ToscaTagNamesEnum.INPUTS, elementGenByName, func);

	}

	public static Either<Map<String, PropertyDefinition>, ResultStatusEnum> getAttributes(Map<String, Object> toscaJson) {
		Function<String, PropertyDefinition> elementGenByName = elementName -> createAttribute(elementName);
		Function<Map<String, Object>, PropertyDefinition> func = map -> createModuleAttribute(map);

		return getElements(toscaJson, ToscaTagNamesEnum.ATTRIBUTES, elementGenByName, func);
	}

	public static <ElementDefinition> Either<Map<String, ElementDefinition>, ResultStatusEnum> getElements(Map<String, Object> toscaJson, ToscaTagNamesEnum elementTagName, Function<String, ElementDefinition> elementGenByName,
			Function<Map<String, Object>, ElementDefinition> func) {
		Either<Map<String, ElementDefinition>, ResultStatusEnum> eitherResult = Either.right(ResultStatusEnum.ELEMENT_NOT_FOUND);
		Either<Map<String, Object>, ResultStatusEnum> toscaAttributes = findFirstToscaMapElement(toscaJson, elementTagName);
		if (toscaAttributes.isLeft()) {
			Map<String, Object> jsonAttributes = toscaAttributes.left().value();
			Map<String, ElementDefinition> moduleAttributes = new HashMap<>();
			Iterator<Entry<String, Object>> propertiesNameValue = jsonAttributes.entrySet().iterator();
			while (propertiesNameValue.hasNext()) {
				Entry<String, Object> attributeNameValue = propertiesNameValue.next();
				if (attributeNameValue.getValue() instanceof Map) {
					@SuppressWarnings("unchecked")
					ElementDefinition attribute = func.apply((Map<String, Object>) attributeNameValue.getValue());
					moduleAttributes.put(String.valueOf(attributeNameValue.getKey()), attribute);
				} else {

					ElementDefinition element = elementGenByName.apply(String.valueOf(attributeNameValue.getValue()));

					moduleAttributes.put(String.valueOf(attributeNameValue.getKey()), element);
				}

			}

			if (moduleAttributes.size() > 0) {
				eitherResult = Either.left(moduleAttributes);
			}

		}
		return eitherResult;

	}

	private static PropertyDefinition createAttribute(String name) {
		PropertyDefinition attribute = new PropertyDefinition();

		attribute.setName(name);
		return attribute;
	}

	private static PropertyDefinition createProperties(String name) {
		PropertyDefinition property = new PropertyDefinition();
		property.setDefaultValue(name);
		property.setName(name);
		return property;
	}

	private static InputDefinition createInputs(String name) {
		InputDefinition input = new InputDefinition();

		input.setName(name);
		return input;
	}

	public static Either<List<HeatParameterDefinition>, ResultStatusEnum> getHeatParameters(Map<String, Object> heatData, String artifactType) {

		Either<List<HeatParameterDefinition>, ResultStatusEnum> eitherResult = Either.right(ResultStatusEnum.ELEMENT_NOT_FOUND);
		Either<Map<String, Object>, ResultStatusEnum> toscaProperties = findFirstToscaMapElement(heatData, ToscaTagNamesEnum.PARAMETERS);
		if (toscaProperties.isLeft()) {
			Map<String, Object> jsonProperties = toscaProperties.left().value();
			List<HeatParameterDefinition> moduleProperties = new ArrayList<>();
			Iterator<Entry<String, Object>> propertiesNameValue = jsonProperties.entrySet().iterator();
			while (propertiesNameValue.hasNext()) {
				Entry<String, Object> propertyNameValue = propertiesNameValue.next();
				if (propertyNameValue.getValue() instanceof Map || propertyNameValue.getValue() instanceof List) {
					if (!artifactType.equals(ArtifactTypeEnum.HEAT_ENV.getType())) {
						@SuppressWarnings("unchecked")
						Either<HeatParameterDefinition, ResultStatusEnum> propertyStatus = createModuleHeatParameter((Map<String, Object>) propertyNameValue.getValue());
						if (propertyStatus.isRight()) {
							return Either.right(propertyStatus.right().value());
						}
						HeatParameterDefinition property = propertyStatus.left().value();
						property.setName(String.valueOf(propertyNameValue.getKey()));
						moduleProperties.add(property);
					} else {
						addHeatParamDefinition(moduleProperties, propertyNameValue, true);
					}
				} else {
					addHeatParamDefinition(moduleProperties, propertyNameValue, false);
				}

			}

			if (moduleProperties.size() > 0) {
				eitherResult = Either.left(moduleProperties);
			}

		}
		return eitherResult;

	}

	private static void addHeatParamDefinition(List<HeatParameterDefinition> moduleProperties, Entry<String, Object> propertyNameValue, boolean isJson) {
		HeatParameterDefinition property = new HeatParameterDefinition();
		Object value = propertyNameValue.getValue();
		if (value != null) {
			property.setDefaultValue(isJson ? new Gson().toJson(value).toString() : StringEscapeUtils.escapeJava(String.valueOf(value)));
		}
		property.setName(String.valueOf(propertyNameValue.getKey()));
		moduleProperties.add(property);
	}

	private static Either<HeatParameterDefinition, ResultStatusEnum> createModuleHeatParameter(Map<String, Object> propertyValue) {
		HeatParameterDefinition propertyDef = new HeatParameterDefinition();
		String type;
		Either<String, ResultStatusEnum> propertyFieldType = findFirstToscaStringElement(propertyValue, ToscaTagNamesEnum.TYPE);
		if (propertyFieldType.isLeft()) {
			type = propertyFieldType.left().value();
			propertyDef.setType(type);
		} else {
			return Either.right(ResultStatusEnum.INVALID_PROPERTY_TYPE);
		}
		Either<String, ResultStatusEnum> propertyFieldDescription = findFirstToscaStringElement(propertyValue, ToscaTagNamesEnum.DESCRIPTION);
		if (propertyFieldDescription.isLeft()) {
			propertyDef.setDescription(propertyFieldDescription.left().value());
		}

		Either<Object, ResultStatusEnum> propertyFieldDefaultVal = findToscaElement(propertyValue, ToscaTagNamesEnum.DEFAULT_VALUE, ToscaElementTypeEnum.ALL);
		if (propertyFieldDefaultVal.isLeft()) {
			if (propertyFieldDefaultVal.left().value() == null) {
				return Either.right(ResultStatusEnum.INVALID_PROPERTY_VALUE);
			}
			Object value = propertyFieldDefaultVal.left().value();
			String defaultValue = type.equals(HeatParameterType.JSON.getType()) ? new Gson().toJson(value).toString() : StringEscapeUtils.escapeJava(String.valueOf(value));
			propertyDef.setDefaultValue(defaultValue);
			propertyDef.setCurrentValue(defaultValue);
		}

		return Either.left(propertyDef);
	}

	public static String getPropertyJsonStringValue(Object value, String type) {
		Gson gson = new Gson();
		if (type == null) {
			return null;
		}
		ToscaPropertyType validType = ToscaPropertyType.isValidType(type);
		if (validType == null ||  validType.equals(ToscaPropertyType.JSON) ||validType.equals(ToscaPropertyType.MAP) || validType.equals(ToscaPropertyType.LIST)) {
			return gson.toJson(value);
		}
		return value.toString();
	}

	/**
	 * removes from Json map (toscaJson) first element found by name (elementName) note that this method could update the received argument toscaJson
	 * 
	 * @param toscaJson
	 * @param elementName
	 */
	public static void removeElementFromJsonMap(Map<String, Object> toscaJson, String elementName) {
		for (Entry<String, Object> entry : toscaJson.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (key.equals(elementName)) {
				toscaJson.remove(elementName);
				return;
			} else if (value instanceof Map) {
				removeElementFromJsonMap((Map<String, Object>) value, elementName);
			}
		}
	}
}
