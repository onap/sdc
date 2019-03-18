/*
 * Copyright © 2016-2018 European Support Limited
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

package org.openecomp.sdc.be.tosca.utils;

import static org.openecomp.sdc.be.tosca.utils.InterfacesOperationsToscaUtil.SELF;
import static org.openecomp.sdc.be.tosca.utils.InterfacesOperationsToscaUtil.addInterfaceDefinitionElement;
import static org.openecomp.sdc.be.tosca.utils.InterfacesOperationsToscaUtil.addInterfaceTypeElement;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.be.DummyConfigurationManager;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationOutputDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.tosca.ToscaFunctions;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.tosca.ToscaRepresentation;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaTemplate;
import org.openecomp.sdc.common.util.YamlToObjectConverter;


public class InterfacesOperationsToscaUtilTest {

    private static final String MAPPED_PROPERTY_NAME = "mapped_property";
    private static final String INPUT_NAME_PREFIX = "input_";
    private static final String OUTPUT_NAME_PREFIX = "output_";
    private static final String NODE_TYPE_NAME = "test";
    private String[] inputTypes = {"string", "integer", "float", "boolean"};
    private static ObjectMapper mapper;
    private static final Map<String, DataTypeDefinition> dataTypes = new HashMap<>();


    @BeforeClass
    public static void setUp() {
        new DummyConfigurationManager();
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    @Test
    public void addInterfaceTypeElementToResource() {
        Component component = new Resource();
        component.setNormalizedName("normalizedComponentName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setType("interface.types.test_resource_name");
        addOperationsToInterface(component, addedInterface, 5, 3, true, false);
        final String interfaceType = "normalizedComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);
        final Map<String, Object> interfaceTypeElement =
                addInterfaceTypeElement(component, new ArrayList<>());

        ToscaExportHandler handler = new ToscaExportHandler(null,null,null,null,null,null, null);
        ToscaTemplate template = new ToscaTemplate("test");
        template.setInterface_types(interfaceTypeElement);
        final ToscaRepresentation toscaRepresentation = handler.createToscaRepresentation(template);

        Assert.assertFalse(toscaRepresentation.getMainYaml().contains("operations"));
        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("interface.types.test_resource_name"));
    }

    @Test
    public void addInterfaceTypeElementToService() {
        Component component = new Service();
        component.setNormalizedName("normalizedServiceComponentName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setType("interface.types.test_service_name");
        addOperationsToInterface(component, addedInterface, 5, 3, true, false);
        final String interfaceType = "normalizedServiceComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);
        final Map<String, Object> interfaceTypeElement =
                addInterfaceTypeElement(component, new ArrayList<>());

        ToscaExportHandler handler = new ToscaExportHandler(null,null,null,null,null,null, null);
        ToscaTemplate template = new ToscaTemplate("testService");
        template.setInterface_types(interfaceTypeElement);
        final ToscaRepresentation toscaRepresentation = handler.createToscaRepresentation(template);

        Assert.assertFalse(toscaRepresentation.getMainYaml().contains("operations"));
        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("interface.types.test_service_name"));
    }

    @Test
    public void addInterfaceDefinitionElementToResource() {
        Component component = new Resource();
        component.setNormalizedName("normalizedComponentName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setType("com.some.resource.or.other.resourceName");

        addOperationsToInterface(component, addedInterface, 3, 2, true, false);
        final String interfaceType = "normalizedComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);
        ToscaNodeType nodeType = new ToscaNodeType();
        addInterfaceDefinitionElement(component, nodeType, dataTypes, false);

        ToscaExportHandler handler = new ToscaExportHandler(null,null,null,null,null,null, null);
        ToscaTemplate template = new ToscaTemplate(NODE_TYPE_NAME);
        Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
        nodeTypes.put(NODE_TYPE_NAME, nodeType);
        template.setNode_types(nodeTypes);
        final ToscaRepresentation toscaRepresentation = handler.createToscaRepresentation(template);

        String mainYaml = toscaRepresentation.getMainYaml();
        Assert.assertFalse(mainYaml.contains("operations"));
        Assert.assertTrue(mainYaml.contains("resourceName:"));
        Assert.assertTrue(mainYaml.contains("inputs:"));
        validateOperationInputs(mainYaml, 2, null);
        Assert.assertFalse(mainYaml.contains("defaultp"));
        Assert.assertTrue(mainYaml.contains("has description"));
        Assert.assertTrue(mainYaml.contains(MAPPED_PROPERTY_NAME));
        Assert.assertTrue(mainYaml.contains("com.some.resource.or.other.resourceName"));
    }

    @Test
    public void addInterfaceDefinitionElementToService() {
        Component component = new Service();
        component.setNormalizedName("normalizedServiceComponentName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setType("com.some.service.or.other.serviceName");
        addOperationsToInterface(component, addedInterface, 3, 2, true, false);
        final String interfaceType = "normalizedServiceComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);
        ToscaNodeType nodeType = new ToscaNodeType();
        addInterfaceDefinitionElement(component, nodeType, dataTypes, false);

        ToscaExportHandler handler = new ToscaExportHandler(null,null,null,null,null,null, null);
        ToscaTemplate template = new ToscaTemplate("testService");
        Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
        nodeTypes.put(NODE_TYPE_NAME, nodeType);
        template.setNode_types(nodeTypes);
        final ToscaRepresentation toscaRepresentation = handler.createToscaRepresentation(template);
        String mainYaml = toscaRepresentation.getMainYaml();
        Assert.assertFalse(mainYaml.contains("operations"));
        Assert.assertTrue(mainYaml.contains("serviceName:"));
        Assert.assertTrue(mainYaml.contains("inputs:"));
        validateOperationInputs(mainYaml, 2, null);
        Assert.assertFalse(mainYaml.contains("defaultp"));
        Assert.assertTrue(mainYaml.contains("has description"));
        Assert.assertTrue(mainYaml.contains(MAPPED_PROPERTY_NAME));
        Assert.assertTrue(mainYaml.contains("com.some.service.or.other.serviceName"));
    }


    @Test
    public void testGetInterfaceAsMapServiceProxy() {
        Component component = new Resource();
        component.setNormalizedName("normalizedComponentName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setToscaResourceName("com.some.resource.or.other.resourceName");
        addedInterface.setType("com.some.resource.or.other.resourceName");
        addOperationsToInterface(component, addedInterface, 3, 2, true, false);
        final String interfaceType = "normalizedComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);
        Map<String, Object> interfacesMap = InterfacesOperationsToscaUtil
                .getInterfacesMap(component, component.getInterfaces(), null, false, true);
        ToscaNodeType nodeType = new ToscaNodeType();
        nodeType.setInterfaces(interfacesMap);
        ToscaExportHandler handler = new ToscaExportHandler();
        ToscaTemplate template = new ToscaTemplate(NODE_TYPE_NAME);
        Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
        nodeTypes.put(NODE_TYPE_NAME, nodeType);
        template.setNode_types(nodeTypes);
        final ToscaRepresentation toscaRepresentation = handler.createToscaRepresentation(template);

        String mainYaml = toscaRepresentation.getMainYaml();
        Assert.assertFalse(mainYaml.contains("operations"));
        Assert.assertTrue(mainYaml.contains("resourceName:"));
        Assert.assertTrue(mainYaml.contains("inputs:"));
        validateServiceProxyOperationInputs(mainYaml);
        Assert.assertFalse(mainYaml.contains("defaultp"));
        Assert.assertTrue(mainYaml.contains("has description"));
        Assert.assertTrue(mainYaml.contains(MAPPED_PROPERTY_NAME));
        Assert.assertTrue(mainYaml.contains("com.some.resource.or.other.resourceName"));
    }

//    @Test
//    public void addInterfaceDefinitionElementToService() {
//        Component component = new Service();
//        component.setNormalizedName("normalizedServiceComponentName");
//        InterfaceDefinition addedInterface = new InterfaceDefinition();
//        addedInterface.setToscaResourceName("com.some.service.or.other.serviceName");
//
//        addOperationsToInterface(addedInterface, 3, 2, true);
//        final String interfaceType = "normalizedServiceComponentName-interface";
//        component.setInterfaces(new HashMap<>());
//        component.getInterfaces().put(interfaceType, addedInterface);
//        ToscaNodeType nodeType = new ToscaNodeType();
//        InterfacesOperationsToscaUtil.addInterfaceDefinitionElement(component, nodeType);
//
//        ToscaExportHandler handler = new ToscaExportHandler();
//        ToscaTemplate template = new ToscaTemplate("testService");
//        Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
//        nodeTypes.put("test", nodeType);
//        template.setNode_types(nodeTypes);
//        final ToscaRepresentation toscaRepresentation = handler.createToscaRepresentation(template);
//
//        Assert.assertFalse(toscaRepresentation.getMainYaml().contains("operations"));
//        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("serviceName:"));
//        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("inputs:"));
//        Assert.assertFalse(toscaRepresentation.getMainYaml().contains("defaultp"));
//        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("has description"));
//        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("naming_function_"));
//        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("com.some.service.or.other.serviceName"));
//    }

    @Test
    public void addInterfaceDefinitionElement_noInputs() {
        Component component = new Resource();
        component.setNormalizedName("normalizedComponentName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setType("com.some.resource.or.other.resourceNameNoInputs");
        addOperationsToInterface(component, addedInterface, 3, 3, false, false);
        final String interfaceType = "normalizedComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);
        ToscaNodeType nodeType = new ToscaNodeType();
        addInterfaceDefinitionElement(component, nodeType, null, false);

        ToscaExportHandler handler = new ToscaExportHandler(null,null,null,null,null,null, null);
        ToscaTemplate template = new ToscaTemplate("test");
        Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
        nodeTypes.put("test", nodeType);
        template.setNode_types(nodeTypes);
        final ToscaRepresentation toscaRepresentation = handler.createToscaRepresentation(template);

        Assert.assertFalse(toscaRepresentation.getMainYaml().contains("operations"));
        Assert.assertFalse(toscaRepresentation.getMainYaml().contains(INPUT_NAME_PREFIX));
        Assert.assertFalse(toscaRepresentation.getMainYaml().contains("defaultp"));
        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("resourceNameNoInputs:"));
        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("has description"));
        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("com.some.resource.or.other.resourceName"));
    }

    @Test
    public void addInterfaceDefinitionElementInputMappedToOtherOperationOutput() {
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
        addInterfaceDefinitionElement(component, nodeType, dataTypes, false);

        ToscaExportHandler handler = new ToscaExportHandler(null,null,null,null,null,null, null);
        ToscaTemplate template = new ToscaTemplate("test");
        Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
        nodeTypes.put("test", nodeType);
        template.setNode_types(nodeTypes);
        final ToscaRepresentation toscaRepresentation = handler.createToscaRepresentation(template);
        String mainYaml = toscaRepresentation.getMainYaml();
        Assert.assertFalse(mainYaml.contains("operations"));
        Assert.assertTrue(mainYaml.contains("resourceNameInputMappedToOutput:"));
        Assert.assertTrue(mainYaml.contains("inputs:"));
        validateOperationInputs(mainYaml, 2, "name_for_op_1");
    }

    @Test
    public void addInterfaceDefinitionElementInputMappedToOtherOperationOutputFromOtherInterface() {
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
                        .filter(opInputDef -> opInputDef.getName().contains("integer"))
                        .forEach(opInputDef -> opInputDef.setInputId(
                                addedInterfaceType +".name_for_op_1.output_integer_1")));
        //Mapping to operation from another interface
        String secondInterfaceType = "org.test.lifecycle.standard.interfaceType.second";
        InterfaceDefinition secondInterface = new InterfaceDefinition();
        secondInterface.setType(secondInterfaceType);
        addOperationsToInterface(component, secondInterface, 2, 2, true, true);
        secondInterface.getOperationsMap().values().stream()
                .filter(operationInputDefinition -> operationInputDefinition.getName().equalsIgnoreCase(
                        "name_for_op_0"))
                .forEach(operation -> operation.getInputs().getListToscaDataDefinition().stream()
                        .filter(opInputDef -> opInputDef.getName().contains("integer"))
                        .forEach(opInputDef -> opInputDef.setInputId(
                                addedInterfaceType +".name_for_op_1.output_integer_1")));
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(addedInterfaceType, addedInterface);
        component.getInterfaces().put(secondInterfaceType, secondInterface);

        ToscaNodeType nodeType = new ToscaNodeType();
        addInterfaceDefinitionElement(component, nodeType, dataTypes, false);

        ToscaExportHandler handler = new ToscaExportHandler(null,null,null,null,null,null, null);
        ToscaTemplate template = new ToscaTemplate("test");
        Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
        nodeTypes.put("test", nodeType);
        template.setNode_types(nodeTypes);
        final ToscaRepresentation toscaRepresentation = handler.createToscaRepresentation(template);

        String mainYaml = toscaRepresentation.getMainYaml();
        Assert.assertFalse(mainYaml.contains("operations"));
        Assert.assertTrue(mainYaml.contains("resourceNameInputMappedToOutput:"));
        Assert.assertTrue(mainYaml.contains("inputs:"));
        validateOperationInputs(mainYaml, 2, "name_for_op_1");
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

    private ListDataDefinition<OperationInputDefinition> createInputs(Component component, int numOfInputs) {
        ListDataDefinition<OperationInputDefinition> operationInputDefinitionList = new ListDataDefinition<>();
        for (int i = 0; i < numOfInputs; i++) {
            String mappedPropertyName = java.util.UUID.randomUUID().toString() + "." + MAPPED_PROPERTY_NAME + i;
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
        Map<String, List<String>> toscaDefaultValueMap = new HashMap<>();
        List<String> toscaDefaultValues = new ArrayList<>();
        toscaDefaultValues.add(SELF);
        toscaDefaultValues.add(interfaceName);
        toscaDefaultValues.add(operationName);
        toscaDefaultValues.add(outputName);
        toscaDefaultValueMap.put(ToscaFunctions.GET_OPERATION_OUTPUT.getFunctionName(), toscaDefaultValues);
        return operationInputDefinition;
    }

    private void validateOperationInputs(String mainYaml, int numOfInputsPerOp, String mappedOperationName) {
        String nodeTypeKey = NODE_TYPE_NAME + ":";
        String nodeTypesRepresentation = mainYaml.substring(mainYaml.indexOf(nodeTypeKey) + nodeTypeKey.length(),
                mainYaml.lastIndexOf(MAPPED_PROPERTY_NAME) + MAPPED_PROPERTY_NAME.length()
                        + String.valueOf(numOfInputsPerOp).length());
        YamlToObjectConverter objectConverter = new YamlToObjectConverter();
        ToscaNodeType toscaNodeType = objectConverter.convert(nodeTypesRepresentation.getBytes(), ToscaNodeType.class);
        Map<String, Object> interfaces = toscaNodeType.getInterfaces();
        for (Map.Entry<String, Object> interfaceEntry : interfaces.entrySet()) {
            Map<String, Object> interfaceDefinition = mapper.convertValue(interfaceEntry.getValue(), Map.class);
            for (Map.Entry<String, Object> operationEntry : interfaceDefinition.entrySet()) {
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
            Assert.assertEquals(inputNameSplit[1], inputValueObject.get("type"));
            Boolean expectedIsRequired = Integer.parseInt(inputNameSplit[2]) % 2 == 0;
            Assert.assertEquals(expectedIsRequired, inputValueObject.get("required"));
            validateOperationInputDefinitionDefaultValue(interfaceType, operationName, inputNameSplit[1],
                    Integer.parseInt(inputNameSplit[2]), inputValueObject);
        }
    }


    private void validateOperationInputDefinitionDefaultValue(String interfaceType, String operationName,
                                                              String inputType, int index,
                                                              Map<String, Object> inputValueObject) {
        Map<String, Object> mappedInputValue = (Map<String, Object>) inputValueObject.get("default");
        if(mappedInputValue.containsKey(ToscaFunctions.GET_PROPERTY.getFunctionName())) {
            String mappedPropertyValue = MAPPED_PROPERTY_NAME + index;
            List<String> mappedPropertyDefaultValue = (List<String>) mappedInputValue.get(ToscaFunctions.GET_PROPERTY.getFunctionName());
            Assert.assertEquals(2, mappedPropertyDefaultValue.size());
            Assert.assertTrue(mappedPropertyDefaultValue.contains(SELF));
            Assert.assertTrue(mappedPropertyDefaultValue.contains(mappedPropertyValue));
        } else if(mappedInputValue.containsKey(ToscaFunctions.GET_OPERATION_OUTPUT.getFunctionName())) {
            List<String> mappedPropertyDefaultValue = (List<String>) mappedInputValue.get(ToscaFunctions.GET_OPERATION_OUTPUT.getFunctionName());
            Assert.assertEquals(4, mappedPropertyDefaultValue.size());
            String mappedPropertyValue = OUTPUT_NAME_PREFIX + inputType + "_" + index;
            Assert.assertTrue(mappedPropertyDefaultValue.contains(SELF));
            Assert.assertTrue(mappedPropertyDefaultValue.contains(interfaceType));
            Assert.assertTrue(mappedPropertyDefaultValue.contains(operationName));
            Assert.assertTrue(mappedPropertyDefaultValue.contains(mappedPropertyValue));
        } else {
            Assert.fail("Invalid Tosca function in default value. Allowed values: "+ ToscaFunctions.GET_PROPERTY.getFunctionName() +
                    "/"+ ToscaFunctions.GET_OPERATION_OUTPUT.getFunctionName());
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
                        Assert.assertFalse(inputValueAsMap.keySet().contains("type"));
                        Assert.assertFalse(inputValueAsMap.keySet().contains("required"));
                        Assert.assertFalse(inputValueAsMap.keySet().contains("default"));
                    }
                }
            }
        }
    }
}
