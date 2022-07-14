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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Lists;
import fj.data.Either;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaElementTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.impl.AnnotationTypeOperations;
import org.openecomp.sdc.be.model.tosca.constraints.ValidValuesConstraint;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.yaml.snakeyaml.Yaml;

public class ImportUtilsTest {

    public static String loadFileNameToJsonString(String fileName) throws IOException {
        String sourceDir = "src/test/resources/normativeTypes";
        return loadFileNameToJsonString(sourceDir, fileName);
    }

    public static String loadCustomTypeFileNameToJsonString(String fileName) throws IOException {
        String sourceDir = "src/test/resources/customTypes";
        return loadFileNameToJsonString(sourceDir, fileName);
    }

    private static String loadFileNameToJsonString(String sourceDir, String fileName) throws IOException {
        java.nio.file.Path filePath = FileSystems.getDefault().getPath(sourceDir, fileName);
        byte[] fileContent = Files.readAllBytes(filePath);
        return new String(fileContent);
    }

    private static Object loadJsonFromFile(String fileName) throws IOException {
        String content = loadFileNameToJsonString(fileName);
        return new Yaml().load(content);
    }

    @Test
    void testStringTypeFindToscaElements() throws IOException {
        Either<List<Object>, ResultStatusEnum> toscaElements = ImportUtils.findToscaElements(
            (Map<String, Object>) loadJsonFromFile("normative-types-string-list-test.yml"), "stringTestTag", ToscaElementTypeEnum.STRING,
            new ArrayList<>());
        assertTrue(toscaElements.isLeft());
        List<Object> list = toscaElements.left().value();
        assertEquals(4, list.size());
        int count = 1;
        for (Object element : list) {
            assertTrue(element instanceof String);
            String value = (String) element;
            assertEquals(value, "stringVal" + count);
            count++;
        }
    }

    @Test
    void testBooleanTypeFindToscaElements() throws IOException {
        Either<List<Object>, ResultStatusEnum> toscaElements = ImportUtils.findToscaElements(
            (Map<String, Object>) loadJsonFromFile("normative-types-all-map-test.yml"), "required", ToscaElementTypeEnum.BOOLEAN, new ArrayList<>());
        assertTrue(toscaElements.isLeft());
        List<Object> list = toscaElements.left().value();
        assertEquals(3, list.size());
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
    void testListTypeFindToscaElements() throws IOException {
        Either<List<Object>, ResultStatusEnum> toscaElements = ImportUtils.findToscaElements(
            (Map<String, Object>) loadJsonFromFile("normative-types-string-list-test.yml"), "listTestTag", ToscaElementTypeEnum.LIST,
            new ArrayList<>());
        assertTrue(toscaElements.isLeft());
        List<Object> list = toscaElements.left().value();
        assertEquals(3, list.size());
        int count = 1;
        for (Object element : list) {
            assertTrue(element instanceof List);

            if (count == 1) {
                verifyListElement1(element);
            } else if (count == 2) {
                verifyListElement2(element);
            } else if (count == 3) {
                verifyListElement3(element);
            }
            count++;
        }
    }

    @Test
    void testAllTypeFindToscaElements() throws IOException {
        Either<List<Object>, ResultStatusEnum> toscaElements = ImportUtils.findToscaElements(
            (Map<String, Object>) loadJsonFromFile("normative-types-all-map-test.yml"), "allTestTag", ToscaElementTypeEnum.ALL, new ArrayList<>());
        assertTrue(toscaElements.isLeft());
        List<Object> list = toscaElements.left().value();
        assertEquals(5, list.size());
        int count = 1;
        for (Object element : list) {
            if (count == 1) {
                assertTrue(element instanceof String);
                assertEquals("tosca.nodes.Root", element);
            } else if (count == 2) {
                assertTrue(element instanceof Map);
                Map<String, Object> mapElement = (Map<String, Object>) element;
                assertEquals(2, mapElement.size());
                Iterator<Entry<String, Object>> elementEntries = mapElement.entrySet().iterator();
                Entry<String, Object> elementEntry = elementEntries.next();
                assertEquals("mapTestTag", elementEntry.getKey());
                assertEquals("string", elementEntry.getValue());

                elementEntry = elementEntries.next();
                assertEquals("required", elementEntry.getKey());
                assertTrue(elementEntry.getValue() instanceof Boolean);
                assertTrue((Boolean) elementEntry.getValue());
            } else if (count == 3) {
                assertTrue(element instanceof String);
                assertEquals("1 MB", element);
            } else if (count == 4) {
                assertTrue(element instanceof List);
                List<Object> listElement = (List<Object>) element;
                assertEquals(2, listElement.size());

                assertTrue(listElement.get(0) instanceof Map);
                Map<String, Object> innerElement = (Map<String, Object>) listElement.get(0);
                assertEquals(1, innerElement.size());
                Entry<String, Object> innerEntry = innerElement.entrySet().iterator().next();
                assertEquals("greater_or_equal", innerEntry.getKey());
                assertEquals("1 MB", innerEntry.getValue());

                assertTrue(listElement.get(1) instanceof Map);
                innerElement = (Map<String, Object>) listElement.get(1);
                assertEquals(1, innerElement.size());
                innerEntry = innerElement.entrySet().iterator().next();
                assertEquals("stringTestTag", innerEntry.getKey());
                assertEquals("stringVal3", innerEntry.getValue());
            } else if (count == 5) {
                assertTrue(element instanceof Boolean);
                assertFalse((Boolean) element);
            }
            count++;
        }
    }

    @Test
    void testMapTypeFindToscaElements() throws IOException {
        Either<List<Object>, ResultStatusEnum> toscaElements = ImportUtils.findToscaElements(
            (Map<String, Object>) loadJsonFromFile("normative-types-all-map-test.yml"), "mapTestTag", ToscaElementTypeEnum.MAP, new ArrayList<>());
        assertTrue(toscaElements.isLeft());
        List<Object> list = toscaElements.left().value();
        assertEquals(2, list.size());
        int count = 1;
        for (Object element : list) {
            assertTrue(element instanceof Map);

            if (count == 1) {
                Map<String, Object> mapElement = (Map<String, Object>) element;
                assertEquals(2, mapElement.size());
                Iterator<Entry<String, Object>> iterator = mapElement.entrySet().iterator();
                Entry<String, Object> inerElementEntry = iterator.next();
                assertEquals("stringTestTag", inerElementEntry.getKey());
                assertEquals("stringVal1", inerElementEntry.getValue());

                inerElementEntry = iterator.next();
                assertEquals("listTestTag", inerElementEntry.getKey());
                assertTrue(inerElementEntry.getValue() instanceof List);
                List<Object> innerValue = (List<Object>) inerElementEntry.getValue();

                assertEquals(3, innerValue.size());

            } else if (count == 2) {
                Map<String, Object> mapElement = (Map<String, Object>) element;
                assertEquals(2, mapElement.size());
                Iterator<Entry<String, Object>> entryItr = mapElement.entrySet().iterator();
                Entry<String, Object> inerElementEntry = entryItr.next();
                assertEquals("type", inerElementEntry.getKey());
                assertEquals("tosca.capabilities.Attachment", inerElementEntry.getValue());
                inerElementEntry = entryItr.next();
                assertEquals("allTestTag", inerElementEntry.getKey());
                assertTrue(inerElementEntry.getValue() instanceof Boolean);
            }

            count++;
        }
    }

    @Test
    void testCreateFullHeatParameterModuleWithString() {

        testCreateFullHeatParameterModule("string", "default value");

    }

    @Test
    void testCreateFullHeatParameterModuleWithNumber() {

        testCreateFullHeatParameterModule("number", "777");
        testCreateFullHeatParameterModule("number", "777.23");

    }

    @Test
    void testCreateFullHeatParameterModuleWithBoolean() {

        testCreateFullHeatParameterModule("boolean", "true");
        testCreateFullHeatParameterModule("boolean", "on");
        testCreateFullHeatParameterModule("boolean", "n");

    }

    @Test
    void testCreateFullHeatParameterModuleWithList() {

        testCreateFullHeatParameterModule("comma_delimited_list", "[one, two]");

    }

    @Test
    void testCreateFullHeatParameterModuleWithMissingType() {

        String name = "fullParameter";
        String description = "description_text";

        Map<String, Object> parametersMap = new HashMap<>();
        Map<String, Object> firstParam = createParameterMap(null, "aaa", name, description);
        parametersMap.put(TypeUtils.ToscaTagNamesEnum.PARAMETERS.getElementName(), firstParam);

        Either<List<HeatParameterDefinition>, ResultStatusEnum> heatParameters = ImportUtils.getHeatParameters(parametersMap,
            ArtifactTypeEnum.HEAT.getType());
        assertTrue(heatParameters.isRight());
        assertEquals(ResultStatusEnum.INVALID_PROPERTY_TYPE, heatParameters.right().value());

    }

    @Test
    void testCreateFullHeatParameterModuleWithMissingFields() {

        String name = "fullParameter";

        Map<String, Object> parametersMap = new HashMap<>();
        String type = "number";
        String defValue = "defvalue";
        // default value cannot be empty in heat in case tag exists
        Map<String, Object> firstParam = createParameterMap(type, defValue, name, null);
        parametersMap.put(TypeUtils.ToscaTagNamesEnum.PARAMETERS.getElementName(), firstParam);

        Either<List<HeatParameterDefinition>, ResultStatusEnum> heatParameters = ImportUtils.getHeatParameters(parametersMap,
            ArtifactTypeEnum.HEAT.getType());
        assertTrue(heatParameters.isLeft());
        List<HeatParameterDefinition> parameterDefList = heatParameters.left().value();
        assertFalse(parameterDefList.isEmpty());
        HeatParameterDefinition parameterDefinition = parameterDefList.get(0);

        assertParameter(parameterDefinition, name, type, null, defValue);

    }

    @Test
    void testGetPropertiesFromYml() throws IOException {

        Map<String, Object> toscaJson = (Map<String, Object>) loadJsonFromFile("importToscaProperties.yml");
        Either<Map<String, PropertyDefinition>, ResultStatusEnum> actualProperties = ImportUtils.getProperties(toscaJson);
        assertTrue(actualProperties.isLeft());
        Map<String, Map<String, Object>> expectedProperties = getElements(toscaJson, TypeUtils.ToscaTagNamesEnum.PROPERTIES);
        compareProperties(expectedProperties, actualProperties.left().value());

    }

    @Test
    void testGetPropertiesWithConstraintsFromYml() throws IOException {

        Map<String, Object> toscaJson = (Map<String, Object>) loadJsonFromFile("propertyConstraintsTest.yml");
        Either<Map<String, PropertyDefinition>, ResultStatusEnum> actualProperties = ImportUtils.getProperties(toscaJson);
        assertTrue(actualProperties.isLeft());
        Map<String, PropertyDefinition> properties = actualProperties.left().value();
        assertTrue(properties.containsKey("service_type"));
        PropertyDefinition property = properties.get("service_type");
        assertTrue(property.getConstraints() != null && property.getConstraints().size() == 1);
        assertTrue(property.getConstraints().get(0) instanceof ValidValuesConstraint);
        assertNotNull(((ValidValuesConstraint) property.getConstraints().get(0)).getValidValues());
        List<String> validValues = ((ValidValuesConstraint) property.getConstraints().get(0)).getValidValues();
        assertTrue(validValues.containsAll(Lists.newArrayList("firewall", "analyzer", "source-nat", "loadbalancer")));

        assertTrue(properties.containsKey("service_interface_type_list"));
        property = properties.get("service_interface_type_list");
        assertTrue(property.getSchema() != null && property.getSchema().getProperty() != null);
        PropertyDefinition innerProperty = new PropertyDefinition(property.getSchema().getProperty());
        List<PropertyConstraint> innerConstraints = innerProperty.getConstraints();
        assertTrue(innerConstraints.get(0) instanceof ValidValuesConstraint);
        assertNotNull(((ValidValuesConstraint) innerConstraints.get(0)).getValidValues());
        validValues = ((ValidValuesConstraint) innerConstraints.get(0)).getValidValues();
        assertTrue(validValues.containsAll(Lists.newArrayList("management", "left", "right", "other")));
    }

    @Test
    void testGetInputsFromYml() throws IOException {

        Map<String, Object> toscaJson = (Map<String, Object>) loadJsonFromFile("importToscaInputsOutputs.yml");

        AnnotationTypeOperations annotationTypeOperations = Mockito.mock(AnnotationTypeOperations.class);
        Mockito.when(annotationTypeOperations.getLatestType(Mockito.anyString())).thenReturn(null);

        Either<Map<String, InputDefinition>, ResultStatusEnum> actualInputs = ImportUtils.getInputs(toscaJson, annotationTypeOperations);
        assertTrue(actualInputs.isLeft());
        Map<String, Map<String, Object>> expectedProperties = getElements(toscaJson, TypeUtils.ToscaTagNamesEnum.INPUTS);
        compareProperties(expectedProperties, actualInputs.left().value());

        actualInputs = ImportUtils.getInputs(toscaJson);
        assertTrue(actualInputs.isLeft());
        expectedProperties = getElements(toscaJson, TypeUtils.ToscaTagNamesEnum.INPUTS);
        compareProperties(expectedProperties, actualInputs.left().value());
    }

    @Test
    void testGetOutputsFromYml() throws IOException {

        Map<String, Object> toscaJson = (Map<String, Object>) loadJsonFromFile("importToscaInputsOutputs.yml");

        Either<Map<String, OutputDefinition>, ResultStatusEnum> actualOutputs = ImportUtils.getOutputs(toscaJson);
        assertTrue(actualOutputs.isLeft());
        Map<String, Map<String, Object>> expectedProperties = getElements(toscaJson, TypeUtils.ToscaTagNamesEnum.OUTPUTS);
        compareAttributes(expectedProperties, actualOutputs.left().value());
    }

    private void compareAttributes(Map<String, Map<String, Object>> expected, Map<String, OutputDefinition> actual) {

        Map<String, Object> singleExpectedAttribute;
        AttributeDataDefinition actualAttribute, expectedAttributeModel;
        // attributes of resource
        for (Map.Entry<String, Map<String, Object>> expectedAttribute : expected.entrySet()) {

            singleExpectedAttribute = expectedAttribute.getValue();
            assertNotNull(singleExpectedAttribute);
            actualAttribute = actual.get(expectedAttribute.getKey());
            assertNotNull(actualAttribute);
            actualAttribute.setName(expectedAttribute.getKey().toString());
            expectedAttributeModel = ImportUtils.createModuleAttribute(singleExpectedAttribute);
            expectedAttributeModel.setName(expectedAttribute.getKey().toString());

            assertEquals(((AttributeDefinition) expectedAttributeModel).getDefaultValue(), ((AttributeDefinition) actualAttribute).getDefaultValue());
            assertEquals(((AttributeDefinition) expectedAttributeModel).getDescription(), ((AttributeDefinition) actualAttribute).getDescription());
            assertEquals(((AttributeDefinition) expectedAttributeModel).getName(), ((AttributeDefinition) actualAttribute).getName());
            assertEquals(((AttributeDefinition) expectedAttributeModel).getStatus(), ((AttributeDefinition) actualAttribute).getStatus());
            assertEquals(((AttributeDefinition) expectedAttributeModel).getType(), ((AttributeDefinition) actualAttribute).getType());

            compareSchemas(expectedAttributeModel.getSchema(), actualAttribute.getSchema());

        }

    }

    private void compareProperties(Map<String, Map<String, Object>> expected, Map<String, ? extends PropertyDefinition> actual) {

        Map<String, Object> singleExpectedProperty;
        PropertyDefinition actualProperty, expectedPropertyModel;

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

    private <T> Map<String, T> getElements(Map<String, Object> toscaJson, TypeUtils.ToscaTagNamesEnum elementType) {

        Either<Map<String, T>, ResultStatusEnum> toscaExpectedElements = ImportUtils.findFirstToscaMapElement(toscaJson, elementType);
        assertTrue(toscaExpectedElements.isLeft());

        return toscaExpectedElements.left().value();

    }

    private void testCreateFullHeatParameterModule(String type, Object defaultVal) {

        String name = "fullParameter";
        String description = "description_text";

        Map<String, Object> parametersMap = new HashMap<>();
        Map<String, Object> firstParam = createParameterMap(type, defaultVal, name, description);
        parametersMap.put(TypeUtils.ToscaTagNamesEnum.PARAMETERS.getElementName(), firstParam);

        Either<List<HeatParameterDefinition>, ResultStatusEnum> heatParameters = ImportUtils.getHeatParameters(parametersMap,
            ArtifactTypeEnum.HEAT.getType());
        assertTrue(heatParameters.isLeft());
        List<HeatParameterDefinition> parameterDefList = heatParameters.left().value();
        assertFalse(parameterDefList.isEmpty());
        HeatParameterDefinition parameterDefinition = parameterDefList.get(0);

        assertParameter(parameterDefinition, name, type, description, defaultVal);

    }

    private Map<String, Object> createParameterMap(String type, Object defaultVal, String name, String description) {
        Map<String, Object> firstParam = new HashMap<>();
        Map<String, Object> valuesMap = new HashMap<>();

        valuesMap.put(TypeUtils.ToscaTagNamesEnum.TYPE.getElementName(), type);
        valuesMap.put(TypeUtils.ToscaTagNamesEnum.DESCRIPTION.getElementName(), description);
        valuesMap.put(TypeUtils.ToscaTagNamesEnum.DEFAULT_VALUE.getElementName(), defaultVal);

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
        assertEquals(2, listElement.size());

        Map<String, String> innerElement = (Map<String, String>) listElement.get(0);
        assertEquals(1, innerElement.size());
        Entry<String, String> innerEntry = innerElement.entrySet().iterator().next();
        assertEquals("testTag1", innerEntry.getKey());
        assertEquals("1 MB", innerEntry.getValue());

        innerElement = (Map<String, String>) listElement.get(1);
        assertEquals(1, innerElement.size());
        innerEntry = innerElement.entrySet().iterator().next();
        assertEquals("type", innerEntry.getKey());
        assertEquals("stringVal2", innerEntry.getValue());
    }

    private void verifyListElement2(Object element) {
        List<Object> listElement = (List<Object>) element;
        assertEquals(2, listElement.size());

        Map<String, Object> innerElement = (Map<String, Object>) listElement.get(0);
        assertEquals(1, innerElement.size());
        Entry<String, Object> innerEntry = innerElement.entrySet().iterator().next();
        assertEquals("testTag1", innerEntry.getKey());
        assertEquals("1 MB", innerEntry.getValue());

        assertTrue(listElement.get(1) instanceof Map);
        innerElement = (Map<String, Object>) listElement.get(1);
        assertEquals(1, innerElement.size());
        innerEntry = innerElement.entrySet().iterator().next();
        assertEquals("listTestTag", innerEntry.getKey());
        assertTrue(innerEntry.getValue() instanceof List);
    }

    private void verifyListElement1(Object element) {
        List<Object> listElement = (List<Object>) element;
        assertEquals(3, listElement.size());

        Map<String, String> innerElement = (Map<String, String>) listElement.get(0);
        assertEquals(1, innerElement.size());
        Entry<String, String> innerEntry = innerElement.entrySet().iterator().next();
        assertEquals("listTestTag", innerEntry.getKey());
        assertEquals("1 MB", innerEntry.getValue());

        innerElement = (Map<String, String>) listElement.get(1);
        assertEquals(1, innerElement.size());
        innerEntry = innerElement.entrySet().iterator().next();
        assertEquals("listTestTag", innerEntry.getKey());
        assertEquals("2 MB", innerEntry.getValue());

        innerElement = (Map<String, String>) listElement.get(2);
        assertEquals(1, innerElement.size());
        innerEntry = innerElement.entrySet().iterator().next();
        assertEquals("stringTestTag", innerEntry.getKey());
        assertEquals("stringVal2", innerEntry.getValue());
    }

}
