/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.tosca;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.openecomp.sdc.be.tosca.InterfacesOperationsConverter.SELF;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.DEFAULT;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.DESCRIPTION;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.INPUTS;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.INTERFACES;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.NODE_TYPES;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.REQUIRED;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.TYPE;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.MapUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.be.DummyConfigurationManager;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InputDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationOutputDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.ServiceMetadataDefinition;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.tosca.model.ToscaInterfaceDefinition;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.common.util.YamlToObjectConverter;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.yaml.snakeyaml.Yaml;

class InterfacesOperationsConverterTest {

    private static final String MAPPED_PROPERTY_NAME = "mapped_property";
    private static final String INPUT_NAME_PREFIX = "input_";
    private static final String OUTPUT_NAME_PREFIX = "output_";
    private static final String NODE_TYPE_NAME = "test";
    private final String[] inputTypes = {"string", "integer", "float", "boolean"};
    private static ObjectMapper mapper;
    private static final Map<String, DataTypeDefinition> dataTypes = new HashMap<>();

    private InterfacesOperationsConverter interfacesOperationsConverter;

    @BeforeAll
    public static void setUp() {
        //initialize the static configuration manager
        new DummyConfigurationManager();
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @BeforeEach
    public void setUpBeforeTest() {
        interfacesOperationsConverter = new InterfacesOperationsConverter(new PropertyConvertor());
    }

    @Test
    void addInterfaceTypeElementToResource() {
        Component component = new Resource();
        component.setNormalizedName("normalizedComponentName");
        component.setComponentMetadataDefinition(new ServiceMetadataDefinition());
        component.getComponentMetadataDefinition().getMetadataDataDefinition().setName("NodeTypeName");
        component.getComponentMetadataDefinition().getMetadataDataDefinition().setSystemName("NodeTypeName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setType("Local");
        addOperationsToInterface(component, addedInterface, 5, 3, true, false);
        final String interfaceType = "normalizedComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);
        final Map<String, Object> interfaceTypeElement = interfacesOperationsConverter.addInterfaceTypeElement(component, new ArrayList<>());
        assertNotNull(interfaceTypeElement);
        assertTrue(interfaceTypeElement.containsKey("org.openecomp.interfaces.node.lifecycle.NodeTypeName"));
        Object o = interfaceTypeElement.get("org.openecomp.interfaces.node.lifecycle.NodeTypeName");
        assertNotNull(o);
        assertTrue(o instanceof Map);
        assertEquals(7, ((Map) o).size());

    }

    @Test
    void addInterfaceTypeElementToService() {
        Component component = new Service();
        component.setNormalizedName("normalizedServiceComponentName");
        component.setComponentMetadataDefinition(new ServiceMetadataDefinition());
        component.getComponentMetadataDefinition().getMetadataDataDefinition().setName("NodeTypeName");
        component.getComponentMetadataDefinition().getMetadataDataDefinition().setSystemName("NodeTypeName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setType("Local");
        addOperationsToInterface(component, addedInterface, 5, 3, true, false);
        final String interfaceType = "normalizedServiceComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);
        final Map<String, Object> interfaceTypeElement = interfacesOperationsConverter.addInterfaceTypeElement(component, new ArrayList<>());
        assertNotNull(interfaceTypeElement);
        assertTrue(interfaceTypeElement.containsKey("org.openecomp.interfaces.node.lifecycle.NodeTypeName"));
        Object o = interfaceTypeElement.get("org.openecomp.interfaces.node.lifecycle.NodeTypeName");
        assertNotNull(o);
        assertTrue(o instanceof Map);
        assertEquals(7, ((Map) o).size());

    }

    @Test
    void addInterfaceDefinitionElementToResource() {
        Component component = new Resource();
        component.setNormalizedName("normalizedComponentName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setType("com.some.resource.or.other.resourceName");

        addOperationsToInterface(component, addedInterface, 3, 2, true, false);
        final String interfaceType = "normalizedComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);
        ToscaNodeType nodeType = new ToscaNodeType();
        interfacesOperationsConverter.addInterfaceDefinitionElement(component, nodeType, dataTypes, false);
        Map<String, Object> interfaces = nodeType.getInterfaces();
        assertNotNull(interfaces);
        assertEquals(1, interfaces.size());
        assertTrue(interfaces.containsKey("resourceName"));
        Object resourceName = interfaces.get("resourceName");
        assertNotNull(resourceName);
        assertTrue(resourceName instanceof Map);
        assertEquals(4, ((Map) resourceName).size());

    }

    @Test
    void addInterfaceDefinitionElementToService() {
        Component component = new Service();
        component.setNormalizedName("normalizedServiceComponentName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setType("com.some.service.or.other.serviceName");
        addOperationsToInterface(component, addedInterface, 3, 2, true, false);
        final String interfaceType = "normalizedServiceComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);
        ToscaNodeType nodeType = new ToscaNodeType();
        interfacesOperationsConverter.addInterfaceDefinitionElement(component, nodeType, dataTypes, false);
        Map<String, Object> interfaces = nodeType.getInterfaces();
        assertNotNull(interfaces);
        assertEquals(1, interfaces.size());
        assertTrue(interfaces.containsKey("serviceName"));
        Object resourceName = interfaces.get("serviceName");
        assertNotNull(resourceName);
        assertTrue(resourceName instanceof Map);
        assertEquals(4, ((Map) resourceName).size());

    }


    @Test
    void testGetInterfaceAsMapServiceProxy() {
        Component component = new Resource();
        component.setNormalizedName("normalizedComponentName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setToscaResourceName("com.some.resource.or.other.resourceName");
        addedInterface.setType("com.some.resource.or.other.resourceName");
        addOperationsToInterface(component, addedInterface, 3, 2, true, false);
        final String interfaceType = "normalizedComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);
        final var interfacesMap = interfacesOperationsConverter.getInterfacesMap(component, null, component.getInterfaces(), null, false, true);
        assertNotNull(interfacesMap);
        assertEquals(1, interfacesMap.size());
        assertTrue(interfacesMap.containsKey("resourceName"));
        Object resourceName = interfacesMap.get("resourceName");
        assertNotNull(resourceName);
        assertTrue(resourceName instanceof Map);
        assertEquals(4, ((Map) resourceName).size());

    }

    @Test
    void addInterfaceDefinitionElement_noInputs() {
        Component component = new Resource();
        component.setNormalizedName("normalizedComponentName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setType("com.some.resource.or.other.resourceNameNoInputs");
        addOperationsToInterface(component, addedInterface, 3, 3, false, false);
        final String interfaceType = "normalizedComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);
        ToscaNodeType nodeType = new ToscaNodeType();
        interfacesOperationsConverter.addInterfaceDefinitionElement(component, nodeType, null, false);
        Map<String, Object> interfaces = nodeType.getInterfaces();
        assertNotNull(interfaces);
        assertEquals(1, interfaces.size());
        assertTrue(interfaces.containsKey("resourceNameNoInputs"));
        Object resourceName = interfaces.get("resourceNameNoInputs");
        assertNotNull(resourceName);
        assertTrue(resourceName instanceof Map);
        assertEquals(4, ((Map) resourceName).size());

    }

    @Test
    void addInterfaceDefinitionElementInputMappedToOtherOperationOutput() {
        String addedInterfaceType = "com.some.resource.or.other.resourceNameInputMappedToOutput";
        Component component = new Resource();
        component.setNormalizedName("normalizedComponentName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setType(addedInterfaceType);
        addOperationsToInterface(component, addedInterface, 2, 2, true, true);
        addedInterface.getOperationsMap().values().stream()
            .filter(operationInputDefinition -> operationInputDefinition.getName().equalsIgnoreCase(
                "name_for_op_0"))
            .forEach(operation -> operation.getInputs().getListToscaDataDefinition().stream()
                .filter(operationInputDefinition -> operationInputDefinition.getName().contains("integer"))
                .forEach(operationInputDefinition -> operationInputDefinition.setInputId(addedInterfaceType +
                    ".name_for_op_1.output_integer_1")));
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(addedInterfaceType, addedInterface);
        ToscaNodeType nodeType = new ToscaNodeType();
        interfacesOperationsConverter.addInterfaceDefinitionElement(component, nodeType, dataTypes, false);
        Map<String, Object> interfaces = nodeType.getInterfaces();
        assertNotNull(interfaces);
        assertEquals(1, interfaces.size());
        assertTrue(interfaces.containsKey("resourceNameInputMappedToOutput"));
        Object resourceName = interfaces.get("resourceNameInputMappedToOutput");
        assertNotNull(resourceName);
        assertTrue(resourceName instanceof Map);
        assertEquals(3, ((Map) resourceName).size());

    }

    @Test
    void addInterfaceDefinitionElementInputMappedToOtherOperationOutputFromOtherInterface() {
        String addedInterfaceType = "com.some.resource.or.other.resourceNameInputMappedToOutput";
        Component component = new Resource();
        component.setNormalizedName("normalizedComponentName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setType(addedInterfaceType);
        addedInterface.setToscaResourceName("com.some.resource.or.other.resourceName");
        addOperationsToInterface(component, addedInterface, 2, 2, true, true);
        addedInterface.getOperationsMap().values().stream()
            .filter(operationInputDefinition -> operationInputDefinition.getName().equalsIgnoreCase(
                "name_for_op_0"))
            .forEach(operation -> operation.getInputs().getListToscaDataDefinition().stream()
                .filter(opInputDef -> opInputDef.getName().contains("integer"))
                .forEach(opInputDef -> opInputDef.setInputId(
                    addedInterfaceType + ".name_for_op_1.output_integer_1")));
        //Mapping to operation from another interface
        String secondInterfaceType = "org.test.lifecycle.standard.interfaceType.second";
        InterfaceDefinition secondInterface = new InterfaceDefinition();
        secondInterface.setType(secondInterfaceType);
        secondInterface.setToscaResourceName("com.some.resource.or.other.resourceName");
        addOperationsToInterface(component, secondInterface, 2, 2, true, true);
        secondInterface.getOperationsMap().values().stream()
            .filter(operationInputDefinition -> operationInputDefinition.getName().equalsIgnoreCase(
                "name_for_op_0"))
            .forEach(operation -> operation.getInputs().getListToscaDataDefinition().stream()
                .filter(opInputDef -> opInputDef.getName().contains("integer"))
                .forEach(opInputDef -> opInputDef.setInputId(
                    addedInterfaceType + ".name_for_op_1.output_integer_1")));
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(addedInterfaceType, addedInterface);
        component.getInterfaces().put(secondInterfaceType, secondInterface);

        ToscaNodeType nodeType = new ToscaNodeType();
        interfacesOperationsConverter.addInterfaceDefinitionElement(component, nodeType, dataTypes, false);
        Map<String, Object> interfaces = nodeType.getInterfaces();
        assertNotNull(interfaces);
        assertEquals(2, interfaces.size());
        assertTrue(interfaces.containsKey("resourceNameInputMappedToOutput"));
        Object resourceName = interfaces.get("resourceNameInputMappedToOutput");
        assertNotNull(resourceName);
        assertTrue(resourceName instanceof Map);
        assertEquals(3, ((Map) resourceName).size());

        assertTrue(interfaces.containsKey("second"));
        resourceName = interfaces.get("second");
        assertNotNull(resourceName);
        assertTrue(resourceName instanceof Map);
        assertEquals(3, ((Map) resourceName).size());

    }

    @Test
    void interfaceWithInputsToscaExportTest() {
        final Component component = new Service();
        final InterfaceDefinition anInterfaceWithInput = new InterfaceDefinition();
        final String interfaceName = "myInterfaceName";
        final String interfaceType = "my.type." + interfaceName;
        anInterfaceWithInput.setType(interfaceType);
        final String input1Name = "input1";
        final InputDataDefinition input1 = createInput("string", "input1 description", false, "input1 value");
        final String input2Name = "input2";
        final InputDataDefinition input2 = createInput("string", "input2 description", true, "input2 value");
        final Map<String, InputDataDefinition> inputMap = new HashMap<>();
        inputMap.put(input1Name, input1);
        inputMap.put(input2Name, input2);
        anInterfaceWithInput.setInputs(inputMap);
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceName, anInterfaceWithInput);
        final ToscaNodeType nodeType = new ToscaNodeType();
        interfacesOperationsConverter.addInterfaceDefinitionElement(component, nodeType, dataTypes, false);
        Map<String, Object> interfaces = nodeType.getInterfaces();
        assertNotNull(interfaces);
        assertEquals(1, interfaces.size());
        assertTrue(interfaces.containsKey("myInterfaceName"));
        Object resourceName = interfaces.get("myInterfaceName");
        assertNotNull(resourceName);
        assertTrue(resourceName instanceof Map);
        assertEquals(2, ((Map) resourceName).size());

    }

    @Test
    void interfaceWithOperationImplementationArtifactPropertiesTest() {
        //given
        final Component component = new Service();
        final InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        final String interfaceName = "myInterfaceName";
        interfaceDefinition.setType("my.type." + interfaceName);
        final var operation1DataDefinition = new OperationDataDefinition();
        operation1DataDefinition.setName("anOperation");

        final PropertyDataDefinition listOfStringProperty = new PropertyDataDefinition();
        listOfStringProperty.setName("listProperty");
        listOfStringProperty.setType(ToscaType.LIST.getType());
        final PropertyDataDefinition listOfStringSchemaProperty = new PropertyDataDefinition();
        listOfStringSchemaProperty.setType(ToscaType.STRING.getType());
        final SchemaDefinition listPropertySchema = new SchemaDefinition();
        listPropertySchema.setProperty(listOfStringProperty);
        listOfStringProperty.setSchema(listPropertySchema);
        listOfStringProperty.setValue("[ \"value1\", \"value2\", \"value3\" ]");
        final ArrayList<Object> propertyList = new ArrayList<>();
        propertyList.add(listOfStringProperty);
        final HashMap<String, Object> artifactDefinitionMapInitializer = new HashMap<>();
        artifactDefinitionMapInitializer.put(JsonPresentationFields.PROPERTIES.getPresentation(), propertyList);
        final ArtifactDataDefinition artifactDataDefinition = new ArtifactDataDefinition(artifactDefinitionMapInitializer);
        artifactDataDefinition.setArtifactName("artifact1");
        artifactDataDefinition.setArtifactType("my.artifact.Type");
        operation1DataDefinition.setImplementation(artifactDataDefinition);
        interfaceDefinition.setOperations(Map.of(operation1DataDefinition.getName(), operation1DataDefinition));
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceName, interfaceDefinition);
        //when
        Map<String, Object> interfacesMap = interfacesOperationsConverter
            .getInterfacesMap(component, null, component.getInterfaces(), null, false, true);
        //then
        assertTrue(interfacesMap.containsKey(interfaceName));
        final Map<String, Object> actualInterfaceMap = (Map<String, Object>) interfacesMap.get(interfaceName);
        assertTrue(actualInterfaceMap.containsKey(operation1DataDefinition.getName()));
        final Map<String, Object> actualOperationMap = (Map<String, Object>) actualInterfaceMap.get(operation1DataDefinition.getName());
        assertTrue(actualOperationMap.containsKey("implementation"));
        final Map<String, Object> actualImplementationMap = (Map<String, Object>) actualOperationMap.get("implementation");
        assertTrue(actualImplementationMap.containsKey("primary"));
        final Map<String, Object> actualArtifactImplementationMap = (Map<String, Object>) actualImplementationMap.get("primary");
        assertTrue(actualArtifactImplementationMap.containsKey("properties"));
        final Map<String, Object> actualArtifactPropertiesMap = (Map<String, Object>) actualArtifactImplementationMap.get("properties");
        assertEquals(1, actualArtifactPropertiesMap.keySet().size());
        assertTrue(actualArtifactPropertiesMap.containsKey(listOfStringProperty.getName()));
        final Object expectedListObject = actualArtifactPropertiesMap.get(listOfStringProperty.getName());
        assertTrue(expectedListObject instanceof List);
        final List<String> expectedListOfStringPropValue = (List<String>) expectedListObject;
        assertEquals(3, expectedListOfStringPropValue.size());
        assertTrue(expectedListOfStringPropValue.contains("value1"));
        assertTrue(expectedListOfStringPropValue.contains("value2"));
        assertTrue(expectedListOfStringPropValue.contains("value3"));
    }


    private void validateInterfaceInputs(final String yaml, final String interfaceName, final Map<String, InputDataDefinition> expectedInputMap) {
        String fixedMainYaml = yaml;
        final String nullString = "null";
        if (fixedMainYaml.startsWith(nullString)) {
            fixedMainYaml = yaml.substring(nullString.length());
        }
        if (fixedMainYaml.endsWith(nullString)) {
            fixedMainYaml = fixedMainYaml.substring(0, fixedMainYaml.length() - nullString.length());
        }
        final Map<String, Object> yamlMap = new Yaml().load(fixedMainYaml);
        final Map<String, Object> nodeTypesMap = (Map<String, Object>) yamlMap.get(NODE_TYPES.getElementName());
        final Map<String, Object> node = (Map<String, Object>) nodeTypesMap.get(NODE_TYPE_NAME);
        final Map<String, Object> interfacesMap = (Map<String, Object>) node.get(INTERFACES.getElementName());
        final Map<String, Object> interface1 = (Map<String, Object>) interfacesMap.get(interfaceName);
        final Map<String, Object> actualInputsMap = (Map<String, Object>) interface1.get(INPUTS.getElementName());
        assertThat(actualInputsMap.keySet(), containsInAnyOrder(expectedInputMap.keySet().toArray()));
        expectedInputMap.forEach((inputName, inputDataDefinition) -> {
            final Map<String, Object> actualInput = (Map<String, Object>) actualInputsMap.get(inputName);
            compareInputYaml(inputName, actualInput, inputDataDefinition);
        });
    }

    private void compareInputYaml(final String inputName, final Map<String, Object> actualInput,
                                  final InputDataDefinition expectedInput) {
        final String msgFormat = "%s should be equal in input %s";
        String field = TYPE.getElementName();
        assertThat(String.format(msgFormat, field, inputName),
            actualInput.get(field), equalTo(expectedInput.getType()));
        field = DESCRIPTION.getElementName();
        assertThat(String.format(msgFormat, field, inputName),
            actualInput.get(field), equalTo(expectedInput.getDescription()));
        field = REQUIRED.getElementName();
        assertThat(String.format(msgFormat, field, inputName),
            actualInput.get(field), equalTo(expectedInput.getRequired()));
        field = DEFAULT.getElementName();
        assertThat(String.format(msgFormat, field, inputName),
            actualInput.get(field), equalTo(expectedInput.getDefaultValue()));
    }

    @FunctionalInterface
    interface MainYamlAssertion extends Function<String, Boolean> {

    }

    private static Function<String, Boolean> all(MainYamlAssertion... fs) {
        return s -> io.vavr.collection.List.of(fs).map(f -> f.apply(s)).fold(true, (l, r) -> l && r);
    }

    private static MainYamlAssertion containsNone(String... expected) {
        return s -> io.vavr.collection.List.of(expected).map(e -> !s.contains(e)).fold(true, (l, r) -> l && r);
    }

    private static MainYamlAssertion containsAll(String... expected) {
        return s -> io.vavr.collection.List.of(expected).map(s::contains).fold(true, (l, r) -> l && r);
    }

    private void addOperationsToInterface(Component component, InterfaceDefinition addedInterface, int numOfOps,
                                          int numOfInputsPerOp, boolean hasInputs, boolean hasOutputs) {

        addedInterface.setOperations(new HashMap<>());
        for (int i = 0; i < numOfOps; i++) {
            final OperationDataDefinition operation = new OperationDataDefinition();
            operation.setName("name_for_op_" + i);
            operation.setDescription("op " + i + " has description");
            final ArtifactDataDefinition implementation = new ArtifactDataDefinition();
            implementation.setArtifactName(i + "_createBPMN.bpmn");
            operation.setImplementation(implementation);
            if (hasInputs) {
                operation.setInputs(createInputs(component, numOfInputsPerOp));
            }
            if (hasOutputs) {
                operation.setOutputs(createOutputs(addedInterface.getToscaResourceName(),
                    operation.getName(), numOfInputsPerOp));
            }
            addedInterface.getOperations().put(operation.getName(), operation);
        }
    }

    private InputDataDefinition createInput(final String type, final String description, final Boolean isRequired,
                                            final String defaultValue) {
        final PropertyDataDefinition propertyDataDefinition = new PropertyDataDefinition();
        if (type != null) {
            propertyDataDefinition.setType(type);
        }
        if (description != null) {
            propertyDataDefinition.setDescription(description);
        }
        if (defaultValue != null) {
            propertyDataDefinition.setDefaultValue(defaultValue);
        }
        if (isRequired != null) {
            propertyDataDefinition.setRequired(isRequired);
        }
        return new InputDataDefinition(propertyDataDefinition);
    }

    private ListDataDefinition<OperationInputDefinition> createInputs(Component component, int numOfInputs) {
        ListDataDefinition<OperationInputDefinition> operationInputDefinitionList = new ListDataDefinition<>();
        for (int i = 0; i < numOfInputs; i++) {
            String mappedPropertyName = java.util.UUID.randomUUID() + "." + MAPPED_PROPERTY_NAME + i;
            operationInputDefinitionList.add(createMockOperationInputDefinition(
                INPUT_NAME_PREFIX + inputTypes[i] + "_" + i, mappedPropertyName, i));
            addMappedPropertyAsComponentInput(component, mappedPropertyName);

        }
        return operationInputDefinitionList;
    }

    private void addMappedPropertyAsComponentInput(Component component, String mappedPropertyName) {
        InputDefinition componentInput = new InputDefinition();
        componentInput.setUniqueId(mappedPropertyName.split("\\.")[0]);
        componentInput.setName(mappedPropertyName.split("\\.")[1]);
        if (Objects.isNull(component.getInputs())) {
            component.setInputs(new ArrayList<>());
        }
        component.getInputs().add(componentInput);
    }

    private ListDataDefinition<OperationOutputDefinition> createOutputs(String interfaceName,
                                                                        String operationName,
                                                                        int numOfOutputs) {
        ListDataDefinition<OperationOutputDefinition> operationOutputDefinitionList = new ListDataDefinition<>();
        for (int i = 0; i < numOfOutputs; i++) {
            operationOutputDefinitionList.add(createMockOperationOutputDefinition(interfaceName, operationName,
                OUTPUT_NAME_PREFIX + inputTypes[i] + "_" + i, i));
        }
        return operationOutputDefinitionList;
    }

    private OperationInputDefinition createMockOperationInputDefinition(String name, String id, int index) {
        OperationInputDefinition operationInputDefinition = new OperationInputDefinition();
        operationInputDefinition.setName(name);
        operationInputDefinition.setInputId(id);
        operationInputDefinition.setType(inputTypes[index]);
        operationInputDefinition.setRequired(index % 2 == 0);
        Map<String, List<String>> toscaDefaultValueMap = new HashMap<>();
        List<String> toscaDefaultValues = new ArrayList<>();
        toscaDefaultValues.add(SELF);
        toscaDefaultValues.add(id.substring(id.lastIndexOf('.') + 1));
        toscaDefaultValueMap.put(ToscaFunctions.GET_PROPERTY.getFunctionName(), toscaDefaultValues);
        operationInputDefinition.setToscaDefaultValue(new Gson().toJson(toscaDefaultValueMap));
        operationInputDefinition.setSource("ServiceInput");
        return operationInputDefinition;
    }

    private OperationOutputDefinition createMockOperationOutputDefinition(String interfaceName, String operationName,
                                                                          String outputName, int index) {
        OperationOutputDefinition operationInputDefinition = new OperationOutputDefinition();
        operationInputDefinition.setName(outputName);
        operationInputDefinition.setType(inputTypes[index]);
        operationInputDefinition.setRequired(index % 2 == 0);
        List<String> toscaDefaultValues = new ArrayList<>();
        toscaDefaultValues.add(SELF);
        toscaDefaultValues.add(interfaceName);
        toscaDefaultValues.add(operationName);
        toscaDefaultValues.add(outputName);
        Map<String, List<String>> toscaDefaultValueMap = new HashMap<>();
        toscaDefaultValueMap.put(ToscaFunctions.GET_OPERATION_OUTPUT.getFunctionName(), toscaDefaultValues);
        return operationInputDefinition;
    }

    private void validateOperationInputs(final String mainYaml, int numOfInputsPerOp, String mappedOperationName) {
        String nodeTypeKey = NODE_TYPE_NAME + ":";
        String nodeTypesRepresentation = mainYaml.substring(mainYaml.indexOf(nodeTypeKey) + nodeTypeKey.length(),
            mainYaml.lastIndexOf(MAPPED_PROPERTY_NAME) + MAPPED_PROPERTY_NAME.length()
                + String.valueOf(numOfInputsPerOp).length());
        YamlToObjectConverter objectConverter = new YamlToObjectConverter();
        ToscaNodeType toscaNodeType = objectConverter.convert(nodeTypesRepresentation.getBytes(), ToscaNodeType.class);
        Map<String, Object> interfaces = toscaNodeType.getInterfaces();
        for (Map.Entry<String, Object> interfaceEntry : interfaces.entrySet()) {
            Map<String, Object> interfaceDefinition = mapper.convertValue(interfaceEntry.getValue(), Map.class);
            final Map<String, Object> operationsMap = interfaceDefinition.entrySet().stream()
                .filter(entry -> !INPUTS.getElementName().equals(entry.getKey()) &&
                    !TYPE.getElementName().equals(entry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
            for (Map.Entry<String, Object> operationEntry : operationsMap.entrySet()) {
                Object operationVal = operationEntry.getValue();
                if (operationVal instanceof Map) {
                    //Since the inputs are mapped to output operations from only first interface so using that name
                    validateOperationInputDefinition(interfaces.keySet().iterator().next(), mappedOperationName,
                        operationVal);
                }
            }
        }
    }

    private void validateOperationInputDefinition(String interfaceType, String operationName, Object operationVal) {
        Map<String, Object> operation = mapper.convertValue(operationVal, Map.class);
        Map<String, Object> inputs = (Map<String, Object>) operation.get("inputs");
        for (Map.Entry<String, Object> inputEntry : inputs.entrySet()) {
            String[] inputNameSplit = inputEntry.getKey().split("_");
            Map<String, Object> inputValueObject = (Map<String, Object>) inputEntry.getValue();
            validateOperationInputDefinitionDefaultValue(interfaceType, operationName, inputNameSplit[1],
                Integer.parseInt(inputNameSplit[2]), inputValueObject);
        }
    }


    private void validateOperationInputDefinitionDefaultValue(String interfaceType, String operationName,
                                                              String inputType, int index,
                                                              Map<String, Object> inputValueObject) {
        if (inputValueObject.containsKey(ToscaFunctions.GET_PROPERTY.getFunctionName())) {
            String mappedPropertyValue = MAPPED_PROPERTY_NAME + index;
            List<String> mappedPropertyDefaultValue = (List<String>) inputValueObject
                .get(ToscaFunctions.GET_PROPERTY.getFunctionName());
            assertEquals(2, mappedPropertyDefaultValue.size());
            assertTrue(mappedPropertyDefaultValue.contains(SELF));
            assertTrue(mappedPropertyDefaultValue.contains(mappedPropertyValue));
        } else if (inputValueObject.containsKey(ToscaFunctions.GET_OPERATION_OUTPUT.getFunctionName())) {
            List<String> mappedPropertyDefaultValue = (List<String>) inputValueObject
                .get(ToscaFunctions.GET_OPERATION_OUTPUT.getFunctionName());
            assertEquals(4, mappedPropertyDefaultValue.size());
            String mappedPropertyValue = OUTPUT_NAME_PREFIX + inputType + "_" + index;
            assertTrue(mappedPropertyDefaultValue.contains(SELF));
            assertTrue(mappedPropertyDefaultValue.contains(interfaceType));
            assertTrue(mappedPropertyDefaultValue.contains(operationName));
            assertTrue(mappedPropertyDefaultValue.contains(mappedPropertyValue));
        } else {
            fail("Invalid Tosca function in default value. Allowed values: " + ToscaFunctions.GET_PROPERTY.getFunctionName() +
                "/" + ToscaFunctions.GET_OPERATION_OUTPUT.getFunctionName());
        }
    }

    private void validateServiceProxyOperationInputs(String mainYaml) {
        String nodeTypeKey = NODE_TYPE_NAME + ":";
        String nodeTypesRepresentation = mainYaml.substring(mainYaml.indexOf(nodeTypeKey) + nodeTypeKey.length(),
            mainYaml.lastIndexOf(MAPPED_PROPERTY_NAME) + MAPPED_PROPERTY_NAME.length());
        YamlUtil yamlUtil = new YamlUtil();
        ToscaNodeType toscaNodeType = yamlUtil.yamlToObject(nodeTypesRepresentation, ToscaNodeType.class);
        for (Object interfaceVal : toscaNodeType.getInterfaces().values()) {
            Map<String, Object> interfaceDefinition = mapper.convertValue(interfaceVal, Map.class);
            for (Object operationVal : interfaceDefinition.values()) {
                if (operationVal instanceof Map) {
                    Map<String, Object> operation = (Map<String, Object>) mapper.convertValue(operationVal, Map.class);
                    Map<String, Object> operationInputs = (Map<String, Object>) operation.get("inputs");
                    for (Object inputValue : operationInputs.values()) {
                        Map<String, Object> inputValueAsMap = (Map<String, Object>) inputValue;
                        assertFalse(inputValueAsMap.keySet().contains("type"));
                        assertFalse(inputValueAsMap.keySet().contains("required"));
                        assertFalse(inputValueAsMap.keySet().contains("default"));
                    }
                }
            }
        }
    }

    @Test
    void testAddInterfaceTypeElementGetCorrectLocalInterfaceName() {
        Service service = new Service();
        service.setComponentMetadataDefinition(new ServiceMetadataDefinition());
        service.getComponentMetadataDefinition().getMetadataDataDefinition().setName("LocalInterface");
        service.getComponentMetadataDefinition().getMetadataDataDefinition().setSystemName("LocalInterface");
        service.setInterfaces(Collections.singletonMap("Local", new InterfaceDefinition("Local", null, new HashMap<>())));

        Map<String, Object> resultMap = interfacesOperationsConverter.addInterfaceTypeElement(service,
            Collections.singletonList("org.openecomp.interfaces.node.lifecycle.Standard"));

        assertTrue(MapUtils.isNotEmpty(resultMap)
            && resultMap.containsKey("org.openecomp.interfaces.node.lifecycle.LocalInterface"));
    }

    @Test
    void testAddInterfaceTypeElementNoTypeChangeIfNotLocal() {
        Service service = new Service();
        service.setComponentMetadataDefinition(new ServiceMetadataDefinition());
        service.getComponentMetadataDefinition().getMetadataDataDefinition().setName("LocalInterface");
        service.setInterfaces(Collections.singletonMap("NotLocal", new InterfaceDefinition("NotLocal", null,
            new HashMap<>())));

        Map<String, Object> resultMap = interfacesOperationsConverter.getInterfacesMap(service, null,
            service.getInterfaces(), null, false, false);

        assertTrue(MapUtils.isNotEmpty(resultMap)
            && resultMap.containsKey("NotLocal"));
    }

    @Test
    void testRemoveInterfacesWithoutOperationsEmptyMap() {
        final Map<String, Object> interfaceMap = new HashMap<>();
        interfacesOperationsConverter.removeInterfacesWithoutOperations(interfaceMap);
        assertNotNull(interfaceMap);
        assertTrue(interfaceMap.isEmpty());
    }

    @Test
    void testRemoveInterfacesWithoutOperationsNullParameter() {
        final Map<String, Object> interfaceMap = null;
        interfacesOperationsConverter.removeInterfacesWithoutOperations(interfaceMap);
        assertNull(interfaceMap);
    }

    @Test
    void testRemoveInterfacesWithoutOperationsSuccess() {
        final Map<String, Object> interfaceMap = new HashMap<>();
        final ToscaInterfaceDefinition toscaInterfaceDefinition1 = new ToscaInterfaceDefinition();
        interfaceMap.put("toscaInterfaceDefinition1", toscaInterfaceDefinition1);

        final ToscaInterfaceDefinition toscaInterfaceDefinition2 = new ToscaInterfaceDefinition();
        final Map<String, Object> toscaInterfaceDefinition2OperationMap = new HashMap<>();
        toscaInterfaceDefinition2OperationMap.put("operation1", new Object());
        toscaInterfaceDefinition2.setOperations(toscaInterfaceDefinition2OperationMap);
        interfaceMap.put("toscaInterfaceDefinition2", toscaInterfaceDefinition2);

        final Map<String, Object> toscaInterfaceDefinition3 = new HashMap<>();
        interfaceMap.put("toscaInterfaceDefinition3", toscaInterfaceDefinition3);

        final Map<String, Object> toscaInterfaceDefinition4 = new HashMap<>();
        toscaInterfaceDefinition4.put("operation1", new Object());
        interfaceMap.put("toscaInterfaceDefinition4", toscaInterfaceDefinition4);

        final Object notAToscaInterfaceDefinition = new Object();
        interfaceMap.put("notAToscaInterfaceDefinition", notAToscaInterfaceDefinition);

        interfacesOperationsConverter.removeInterfacesWithoutOperations(interfaceMap);
        assertFalse(interfaceMap.containsKey("toscaInterfaceDefinition1"));
        assertTrue(interfaceMap.containsKey("toscaInterfaceDefinition2"));
        assertFalse(interfaceMap.containsKey("toscaInterfaceDefinition3"));
        assertTrue(interfaceMap.containsKey("toscaInterfaceDefinition4"));
        assertTrue(interfaceMap.containsKey("notAToscaInterfaceDefinition"));
    }
}
