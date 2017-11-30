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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaElementTypeEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.yaml.snakeyaml.Yaml;

import fj.data.Either;

public class ImportUtilsTest {
	@Test
	public void testStringTypeFindToscaElements() throws IOException {
		Either<List<Object>, ResultStatusEnum> toscaElements = ImportUtils.findToscaElements((Map<String, Object>) loadJsonFromFile("normative-types-string-list-test.yml"), "stringTestTag", ToscaElementTypeEnum.STRING, new ArrayList<>());
		assertTrue(toscaElements.isLeft());
		List<Object> list = toscaElements.left().value();
		assertTrue(list.size() == 4);
		int count = 1;
		for (Object element : list) {
			assertTrue(element instanceof String);
			String value = (String) element;
			assertTrue(value.equals("stringVal" + count));
			count++;
		}
	}

	@Test
	public void testBooleanTypeFindToscaElements() throws IOException {
		Either<List<Object>, ResultStatusEnum> toscaElements = ImportUtils.findToscaElements((Map<String, Object>) loadJsonFromFile("normative-types-all-map-test.yml"), "required", ToscaElementTypeEnum.BOOLEAN, new ArrayList<>());
		assertTrue(toscaElements.isLeft());
		List<Object> list = toscaElements.left().value();
		assertTrue(list.size() == 3);
		int count = 1;
		for (Object element : list) {
			assertTrue(element instanceof Boolean);
			Boolean value = (Boolean) element;
			if (count == 1 || count == 3) {
				assertFalse(value);
			} else if (count == 2) {
				assertTrue(value);
			}

			count++;
		}
	}

	@Test
	public void testListTypeFindToscaElements() throws IOException {
		Either<List<Object>, ResultStatusEnum> toscaElements = ImportUtils.findToscaElements((Map<String, Object>) loadJsonFromFile("normative-types-string-list-test.yml"), "listTestTag", ToscaElementTypeEnum.LIST, new ArrayList<>());
		assertTrue(toscaElements.isLeft());
		List<Object> list = toscaElements.left().value();
		assertTrue(list.size() == 3);
		int count = 1;
		for (Object element : list) {
			assertTrue(element instanceof List);

			if (count == 1) {
				verifyListElement1(element);
			} else if (count == 2) {
				verifyListElement2(element);
			}

			else if (count == 3) {
				verifyListElement3(element);
			}
			count++;
		}
	}

	@Test
	public void testAllTypeFindToscaElements() throws IOException {
		Either<List<Object>, ResultStatusEnum> toscaElements = ImportUtils.findToscaElements((Map<String, Object>) loadJsonFromFile("normative-types-all-map-test.yml"), "allTestTag", ToscaElementTypeEnum.ALL, new ArrayList<>());
		assertTrue(toscaElements.isLeft());
		List<Object> list = toscaElements.left().value();
		assertTrue(list.size() == 5);
		int count = 1;
		for (Object element : list) {
			if (count == 1) {
				assertTrue(element instanceof String);
				assertTrue(element.equals("tosca.nodes.Root"));
			} else if (count == 2) {
				assertTrue(element instanceof Map);
				Map<String, Object> mapElement = (Map<String, Object>) element;
				assertTrue(mapElement.size() == 2);
				Iterator<Entry<String, Object>> elementEntries = mapElement.entrySet().iterator();
				Entry<String, Object> elementEntry = elementEntries.next();
				assertTrue(elementEntry.getKey().equals("mapTestTag"));
				assertTrue(elementEntry.getValue().equals("string"));

				elementEntry = elementEntries.next();
				assertTrue(elementEntry.getKey().equals("required"));
				assertTrue(elementEntry.getValue() instanceof Boolean);
				assertTrue((Boolean) elementEntry.getValue());
			}

			else if (count == 3) {
				assertTrue(element instanceof String);
				assertTrue(element.equals("1 MB"));
			}

			else if (count == 4) {
				assertTrue(element instanceof List);
				List<Object> listElement = (List<Object>) element;
				assertTrue(listElement.size() == 2);

				assertTrue(listElement.get(0) instanceof Map);
				Map<String, Object> innerElement = (Map<String, Object>) listElement.get(0);
				assertTrue(innerElement.size() == 1);
				Entry<String, Object> innerEntry = innerElement.entrySet().iterator().next();
				assertTrue(innerEntry.getKey().equals("greater_or_equal"));
				assertTrue(innerEntry.getValue().equals("1 MB"));

				assertTrue(listElement.get(1) instanceof Map);
				innerElement = (Map<String, Object>) listElement.get(1);
				assertTrue(innerElement.size() == 1);
				innerEntry = innerElement.entrySet().iterator().next();
				assertTrue(innerEntry.getKey().equals("stringTestTag"));
				assertTrue(innerEntry.getValue().equals("stringVal3"));
			} else if (count == 5) {
				assertTrue(element instanceof Boolean);
				assertFalse((Boolean) element);
			}
			count++;
		}
	}

	@Test
	public void testMapTypeFindToscaElements() throws IOException {
		Either<List<Object>, ResultStatusEnum> toscaElements = ImportUtils.findToscaElements((Map<String, Object>) loadJsonFromFile("normative-types-all-map-test.yml"), "mapTestTag", ToscaElementTypeEnum.MAP, new ArrayList<>());
		assertTrue(toscaElements.isLeft());
		List<Object> list = toscaElements.left().value();
		assertTrue(list.size() == 2);
		int count = 1;
		for (Object element : list) {
			assertTrue(element instanceof Map);

			if (count == 1) {
				Map<String, Object> mapElement = (Map<String, Object>) element;
				assertTrue(mapElement.size() == 2);
				Iterator<Entry<String, Object>> iterator = mapElement.entrySet().iterator();
				Entry<String, Object> inerElementEntry = iterator.next();
				assertTrue(inerElementEntry.getKey().equals("stringTestTag"));
				assertTrue(inerElementEntry.getValue().equals("stringVal1"));

				inerElementEntry = iterator.next();
				assertTrue(inerElementEntry.getKey().equals("listTestTag"));
				assertTrue(inerElementEntry.getValue() instanceof List);
				List<Object> innerValue = (List<Object>) inerElementEntry.getValue();

				assertTrue(innerValue.size() == 3);

			} else if (count == 2) {
				Map<String, Object> mapElement = (Map<String, Object>) element;
				assertTrue(mapElement.size() == 2);
				Iterator<Entry<String, Object>> entryItr = mapElement.entrySet().iterator();
				Entry<String, Object> inerElementEntry = entryItr.next();
				assertTrue(inerElementEntry.getKey().equals("type"));
				assertTrue(inerElementEntry.getValue().equals("tosca.capabilities.Attachment"));
				inerElementEntry = entryItr.next();
				assertTrue(inerElementEntry.getKey().equals("allTestTag"));
				assertTrue(inerElementEntry.getValue() instanceof Boolean);
			}

			count++;
		}
	}

	@Test
	public void testCreateFullHeatParameterModuleWithString() {

		testCreateFullHeatParameterModule("string", "default value");

	}

	@Test
	public void testCreateFullHeatParameterModuleWithNumber() {

		testCreateFullHeatParameterModule("number", "777");
		testCreateFullHeatParameterModule("number", "777.23");

	}

	@Test
	public void testCreateFullHeatParameterModuleWithBoolean() {

		testCreateFullHeatParameterModule("boolean", "true");
		testCreateFullHeatParameterModule("boolean", "on");
		testCreateFullHeatParameterModule("boolean", "n");

	}

	@Test
	public void testCreateFullHeatParameterModuleWithList() {

		testCreateFullHeatParameterModule("comma_delimited_list", "[one, two]");

	}

	// @Test
	// public void testCreateFullHeatParameterModuleWithInvalidType(){
	//
	// String name = "fullParameter";
	// String description = "description_text";
	//
	// Map<String, Object> parametersMap = new HashMap<String, Object>();
	// Map<String, Object> firstParam = createParameterMap("aaa", "aaa",
	// name, description);
	// parametersMap.put(ToscaTagNamesEnum.PARAMETERS.getElementName(),
	// firstParam);
	//
	// Either<List<HeatParameterDefinition>,ResultStatusEnum> heatParameters =
	// ImportUtils.getHeatParameters(parametersMap);
	// assertTrue(heatParameters.isRight());
	// assertEquals(ResultStatusEnum.INVALID_PROPERTY_TYPE,
	// heatParameters.right().value());
	//
	// }

	@Test
	public void testCreateFullHeatParameterModuleWithMissingType() {

		String name = "fullParameter";
		String description = "description_text";

		Map<String, Object> parametersMap = new HashMap<String, Object>();
		Map<String, Object> firstParam = createParameterMap(null, "aaa", name, description);
		parametersMap.put(ToscaTagNamesEnum.PARAMETERS.getElementName(), firstParam);

		Either<List<HeatParameterDefinition>, ResultStatusEnum> heatParameters = ImportUtils.getHeatParameters(parametersMap, ArtifactTypeEnum.HEAT.getType());
		assertTrue(heatParameters.isRight());
		assertEquals(ResultStatusEnum.INVALID_PROPERTY_TYPE, heatParameters.right().value());

	}

	@Test
	public void testCreateFullHeatParameterModuleWithMissingFields() {

		String name = "fullParameter";

		Map<String, Object> parametersMap = new HashMap<String, Object>();
		String type = "number";
		String defValue = "defvalue";
		// default value cannot be empty in heat in case tag exists
		Map<String, Object> firstParam = createParameterMap(type, defValue, name, null);
		parametersMap.put(ToscaTagNamesEnum.PARAMETERS.getElementName(), firstParam);

		Either<List<HeatParameterDefinition>, ResultStatusEnum> heatParameters = ImportUtils.getHeatParameters(parametersMap, ArtifactTypeEnum.HEAT.getType());
		assertTrue(heatParameters.isLeft());
		List<HeatParameterDefinition> parameterDefList = heatParameters.left().value();
		assertFalse(parameterDefList.isEmpty());
		HeatParameterDefinition parameterDefinition = parameterDefList.get(0);

		assertParameter(parameterDefinition, name, type, null, defValue);

	}

	@Test
	public void testGetAttributesFromYml() throws IOException {

		Map<String, Object> toscaJson = (Map<String, Object>) loadJsonFromFile("importToscaWithAttribute.yml");
		Either<Map<String, PropertyDefinition>, ResultStatusEnum> actualAttributes = ImportUtils.getAttributes(toscaJson);
		assertTrue(actualAttributes.isLeft());
		Map<String, Map<String, Object>> expectedAttributes = getElements(toscaJson, ToscaTagNamesEnum.ATTRIBUTES);
		compareAttributes(expectedAttributes, actualAttributes.left().value());

	}

	@Test
	public void testGetPropertiesFromYml() throws IOException {

		Map<String, Object> toscaJson = (Map<String, Object>) loadJsonFromFile("importToscaProperties.yml");
		Either<Map<String, PropertyDefinition>, ResultStatusEnum> actualProperties = ImportUtils.getProperties(toscaJson);
		assertTrue(actualProperties.isLeft());
		Map<String, Map<String, Object>> expectedProperties = getElements(toscaJson, ToscaTagNamesEnum.PROPERTIES);
		compareProperties(expectedProperties, actualProperties.left().value());

	}

	private void compareAttributes(Map<String, Map<String, Object>> expected, Map<String, PropertyDefinition> actual) {

		Map<String, Object> singleExpectedAttribute;
		PropertyDefinition actualAttribute, expectedAttributeModel;
		// attributes of resource
		for (Map.Entry<String, Map<String, Object>> expectedAttribute : expected.entrySet()) {

			singleExpectedAttribute = expectedAttribute.getValue();
			assertNotNull(singleExpectedAttribute);
			actualAttribute = actual.get(expectedAttribute.getKey());
			assertNotNull(actualAttribute);
			actualAttribute.setName(expectedAttribute.getKey().toString());
			expectedAttributeModel = ImportUtils.createModuleAttribute(singleExpectedAttribute);
			expectedAttributeModel.setName(expectedAttribute.getKey().toString());

			assertEquals(expectedAttributeModel.getDefaultValue(), actualAttribute.getDefaultValue());
			assertEquals(expectedAttributeModel.getDescription(), actualAttribute.getDescription());
			assertEquals(expectedAttributeModel.getName(), actualAttribute.getName());
			assertEquals(expectedAttributeModel.getStatus(), actualAttribute.getStatus());
			assertEquals(expectedAttributeModel.getType(), actualAttribute.getType());

			compareSchemas(expectedAttributeModel.getSchema(), actualAttribute.getSchema());

		}

	}

	private void compareProperties(Map<String, Map<String, Object>> expected, Map<String, PropertyDefinition> actual) {

		Map<String, Object> singleExpectedProperty;
		PropertyDefinition actualProperty, expectedPropertyModel;
		// attributes of resource
		for (Map.Entry<String, Map<String, Object>> expectedProperty : expected.entrySet()) {

			singleExpectedProperty = expectedProperty.getValue();
			assertNotNull(singleExpectedProperty);
			actualProperty = actual.get(expectedProperty.getKey());
			assertNotNull(actualProperty);
			actualProperty.setName(expectedProperty.getKey().toString());
			expectedPropertyModel = ImportUtils.createModuleProperty(singleExpectedProperty);
			expectedPropertyModel.setName(expectedProperty.getKey().toString());

			assertEquals(expectedPropertyModel.getDefaultValue(), actualProperty.getDefaultValue());
			assertEquals(expectedPropertyModel.getDescription(), actualProperty.getDescription());
			assertEquals(expectedPropertyModel.getName(), actualProperty.getName());
			assertEquals(expectedPropertyModel.getStatus(), actualProperty.getStatus());
			assertEquals(expectedPropertyModel.getType(), actualProperty.getType());

			compareSchemas(expectedPropertyModel.getSchema(), actualProperty.getSchema());

		}

	}

	private void compareSchemas(SchemaDefinition expected, SchemaDefinition actual) {

		if (expected == null && actual == null) {
			return;
		}
		PropertyDataDefinition actualPropertySchema = actual.getProperty();
		PropertyDataDefinition expectedPropertySchema = expected.getProperty();
		assertNotNull(actualPropertySchema);
		assertNotNull(expectedPropertySchema);
		assertEquals(expectedPropertySchema.getDescription(), actualPropertySchema.getDescription());
		assertEquals(expectedPropertySchema.getType(), actualPropertySchema.getType());

	}

	private <T> Map<String, T> getElements(Map<String, Object> toscaJson, ToscaTagNamesEnum elementType) {

		Either<Map<String, T>, ResultStatusEnum> toscaExpectedElements = ImportUtils.findFirstToscaMapElement(toscaJson, elementType);
		assertTrue(toscaExpectedElements.isLeft());

		return toscaExpectedElements.left().value();

	}

	private void testCreateFullHeatParameterModule(String type, Object defaultVal) {

		String name = "fullParameter";
		String description = "description_text";

		Map<String, Object> parametersMap = new HashMap<String, Object>();
		Map<String, Object> firstParam = createParameterMap(type, defaultVal, name, description);
		parametersMap.put(ToscaTagNamesEnum.PARAMETERS.getElementName(), firstParam);

		Either<List<HeatParameterDefinition>, ResultStatusEnum> heatParameters = ImportUtils.getHeatParameters(parametersMap, ArtifactTypeEnum.HEAT.getType());
		assertTrue(heatParameters.isLeft());
		List<HeatParameterDefinition> parameterDefList = heatParameters.left().value();
		assertFalse(parameterDefList.isEmpty());
		HeatParameterDefinition parameterDefinition = parameterDefList.get(0);

		assertParameter(parameterDefinition, name, type, description, defaultVal);

	}

	private Map<String, Object> createParameterMap(String type, Object defaultVal, String name, String description) {
		Map<String, Object> firstParam = new HashMap<String, Object>();
		Map<String, Object> valuesMap = new HashMap<String, Object>();

		valuesMap.put(ToscaTagNamesEnum.TYPE.getElementName(), type);
		valuesMap.put(ToscaTagNamesEnum.DESCRIPTION.getElementName(), description);
		valuesMap.put(ToscaTagNamesEnum.DEFAULT_VALUE.getElementName(), defaultVal);

		firstParam.put(name, valuesMap);
		return firstParam;
	}

	private void assertParameter(HeatParameterDefinition parameterDefinition, String name, String type, String description, Object defaultVal) {
		assertEquals(name, parameterDefinition.getName());
		assertEquals(description, parameterDefinition.getDescription());
		assertEquals(type, parameterDefinition.getType());
		assertEquals(String.valueOf(defaultVal), parameterDefinition.getDefaultValue());
		assertEquals(String.valueOf(defaultVal), parameterDefinition.getCurrentValue());
	}

	private void verifyListElement3(Object element) {
		List<Object> listElement = (List<Object>) element;
		assertTrue(listElement.size() == 2);

		Map<String, String> innerElement = (Map<String, String>) listElement.get(0);
		assertTrue(innerElement.size() == 1);
		Entry<String, String> innerEntry = innerElement.entrySet().iterator().next();
		assertTrue(innerEntry.getKey().equals("testTag1"));
		assertTrue(innerEntry.getValue().equals("1 MB"));

		innerElement = (Map<String, String>) listElement.get(1);
		assertTrue(innerElement.size() == 1);
		innerEntry = innerElement.entrySet().iterator().next();
		assertTrue(innerEntry.getKey().equals("type"));
		assertTrue(innerEntry.getValue().equals("stringVal2"));
	}

	private void verifyListElement2(Object element) {
		List<Object> listElement = (List<Object>) element;
		assertTrue(listElement.size() == 2);

		Map<String, Object> innerElement = (Map<String, Object>) listElement.get(0);
		assertTrue(innerElement.size() == 1);
		Entry<String, Object> innerEntry = innerElement.entrySet().iterator().next();
		assertTrue(innerEntry.getKey().equals("testTag1"));
		assertTrue(innerEntry.getValue().equals("1 MB"));

		assertTrue(listElement.get(1) instanceof Map);
		innerElement = (Map<String, Object>) listElement.get(1);
		assertTrue(innerElement.size() == 1);
		innerEntry = innerElement.entrySet().iterator().next();
		assertTrue(innerEntry.getKey().equals("listTestTag"));
		assertTrue(innerEntry.getValue() instanceof List);
	}

	private void verifyListElement1(Object element) {
		List<Object> listElement = (List<Object>) element;
		assertTrue(listElement.size() == 3);

		Map<String, String> innerElement = (Map<String, String>) listElement.get(0);
		assertTrue(innerElement.size() == 1);
		Entry<String, String> innerEntry = innerElement.entrySet().iterator().next();
		assertTrue(innerEntry.getKey().equals("listTestTag"));
		assertTrue(innerEntry.getValue().equals("1 MB"));

		innerElement = (Map<String, String>) listElement.get(1);
		assertTrue(innerElement.size() == 1);
		innerEntry = innerElement.entrySet().iterator().next();
		assertTrue(innerEntry.getKey().equals("listTestTag"));
		assertTrue(innerEntry.getValue().equals("2 MB"));

		innerElement = (Map<String, String>) listElement.get(2);
		assertTrue(innerElement.size() == 1);
		innerEntry = innerElement.entrySet().iterator().next();
		assertTrue(innerEntry.getKey().equals("stringTestTag"));
		assertTrue(innerEntry.getValue().equals("stringVal2"));
	}

	public static String loadFileNameToJsonString(String fileName) throws IOException {
		String sourceDir = "src/test/resources/normativeTypes";
		java.nio.file.Path filePath = FileSystems.getDefault().getPath(sourceDir, fileName);
		byte[] fileContent = Files.readAllBytes(filePath);
		String content = new String(fileContent);
		return content;
	}

	private static Object loadJsonFromFile(String fileName) throws IOException {
		String content = loadFileNameToJsonString(fileName);
		Object load = new Yaml().load(content);
		return load;
	}

}
