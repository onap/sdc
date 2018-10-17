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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.sdc.be.DummyConfigurationManager;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.tosca.ToscaRepresentation;
import org.openecomp.sdc.be.tosca.model.ToscaLifecycleOperationDefinition;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.openecomp.sdc.be.tosca.model.ToscaTemplate;
import org.openecomp.sdc.common.util.YamlToObjectConverter;

public class InterfacesOperationsToscaUtilTest {

    private static final String MAPPED_PROPERTY_NAME = "mapped_property";
    private static final String INPUT_NAME_PREFIX = "input_";
    private static final String NODE_TYPE_NAME = "test";
    private String[] inputTypes = {"string", "integer", "float", "boolean"};
    private static ObjectMapper mapper;

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
        addedInterface.setToscaResourceName("interface.types.test_resource_name");
        addOperationsToInterface(addedInterface, 5, 3, true);
        final String interfaceType = "normalizedComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);
        final Map<String, Object> interfaceTypeElement =
                InterfacesOperationsToscaUtil.addInterfaceTypeElement(component);

        ToscaExportHandler handler = new ToscaExportHandler(null,null,null,null,null,null);
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
        addedInterface.setToscaResourceName("interface.types.test_service_name");
        addOperationsToInterface(addedInterface, 5, 3, true);
        final String interfaceType = "normalizedServiceComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);
        final Map<String, Object> interfaceTypeElement =
                InterfacesOperationsToscaUtil.addInterfaceTypeElement(component);

        ToscaExportHandler handler = new ToscaExportHandler(null,null,null,null,null,null);
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
        addedInterface.setToscaResourceName("com.some.resource.or.other.resourceName");

        addOperationsToInterface(addedInterface, 3, 2, true);
        final String interfaceType = "normalizedComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);
        ToscaNodeType nodeType = new ToscaNodeType();
        InterfacesOperationsToscaUtil.addInterfaceDefinitionElement(component, nodeType, false);

        ToscaExportHandler handler = new ToscaExportHandler(null,null,null,null,null,null);
        ToscaTemplate template = new ToscaTemplate(NODE_TYPE_NAME);
        Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
        nodeTypes.put(NODE_TYPE_NAME, nodeType);
        template.setNode_types(nodeTypes);
        final ToscaRepresentation toscaRepresentation = handler.createToscaRepresentation(template);

        String mainYaml = toscaRepresentation.getMainYaml();
        Assert.assertFalse(mainYaml.contains("operations"));
        Assert.assertTrue(mainYaml.contains("resourceName:"));
        Assert.assertTrue(mainYaml.contains("inputs:"));
        validateOperationInputs(mainYaml);
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
        addedInterface.setToscaResourceName("com.some.service.or.other.serviceName");

        addOperationsToInterface(addedInterface, 3, 2, true);
        final String interfaceType = "normalizedServiceComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);
        ToscaNodeType nodeType = new ToscaNodeType();
        InterfacesOperationsToscaUtil.addInterfaceDefinitionElement(component, nodeType, true);

        ToscaExportHandler handler = new ToscaExportHandler(null,null,null,null,null,null);
        ToscaTemplate template = new ToscaTemplate("testService");
        Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
        nodeTypes.put(NODE_TYPE_NAME, nodeType);
        template.setNode_types(nodeTypes);
        final ToscaRepresentation toscaRepresentation = handler.createToscaRepresentation(template);
        String mainYaml = toscaRepresentation.getMainYaml();
        Assert.assertFalse(mainYaml.contains("operations"));
        Assert.assertTrue(mainYaml.contains("serviceName:"));
        Assert.assertTrue(mainYaml.contains("inputs:"));
        validateOperationInputs(mainYaml);
        Assert.assertFalse(mainYaml.contains("defaultp"));
        Assert.assertTrue(mainYaml.contains("has description"));
        Assert.assertTrue(mainYaml.contains(MAPPED_PROPERTY_NAME));
        Assert.assertTrue(mainYaml.contains("com.some.service.or.other.serviceName"));
    }

    @Test
    public void addInterfaceDefinitionElement_noInputs() {
        Component component = new Resource();
        component.setNormalizedName("normalizedComponentName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setToscaResourceName("com.some.resource.or.other.resourceNameNoInputs");

        addOperationsToInterface(addedInterface, 3, 3, false);
        final String interfaceType = "normalizedComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);
        ToscaNodeType nodeType = new ToscaNodeType();
        InterfacesOperationsToscaUtil.addInterfaceDefinitionElement(component, nodeType, false);

        ToscaExportHandler handler = new ToscaExportHandler(null,null,null,null,null,null);
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


    private void addOperationsToInterface(InterfaceDefinition addedInterface, int numOfOps, int numOfInputsPerOp,
            boolean hasInputs) {

        addedInterface.setOperations(new HashMap<>());
        for (int i = 0; i < numOfOps; i++) {
            final OperationDataDefinition operation = new OperationDataDefinition();
            operation.setName("name_for_op_" + i);
            operation.setDescription( "op "+i+" has description");
            final ArtifactDataDefinition implementation = new ArtifactDataDefinition();
            implementation.setArtifactName(i + "_createBPMN.bpmn");
            operation.setImplementation(implementation);
            if (hasInputs) {
                operation.setInputs(createInputs(numOfInputsPerOp));
            }
            addedInterface.getOperations().put(operation.getName(), operation);
        }
    }


    private ListDataDefinition<OperationInputDefinition> createInputs(int numOfInputs) {
        ListDataDefinition<OperationInputDefinition> operationInputDefinitionList = new ListDataDefinition<>();
        for (int i = 0; i < numOfInputs; i++) {
            operationInputDefinitionList.add(createMockOperationInputDefinition(
                    INPUT_NAME_PREFIX + inputTypes[i] + "_" + i,
                    java.util.UUID.randomUUID().toString() + "." + MAPPED_PROPERTY_NAME, i));
        }
        return operationInputDefinitionList;
    }


    private OperationInputDefinition createMockOperationInputDefinition(String name, String id, int index) {
        OperationInputDefinition operationInputDefinition = new OperationInputDefinition();
        operationInputDefinition.setName(name);
        operationInputDefinition.setInputId(id);
        operationInputDefinition.setType(inputTypes[index]);
        operationInputDefinition.setRequired(index % 2 == 0);
        return operationInputDefinition;
    }

    private void validateOperationInputs(String mainYaml) {
        String nodeTypeKey = NODE_TYPE_NAME + ":";
        String nodeTypesRepresentation = mainYaml.substring(mainYaml.indexOf(nodeTypeKey) + nodeTypeKey.length(),
                mainYaml.lastIndexOf(MAPPED_PROPERTY_NAME) + MAPPED_PROPERTY_NAME.length());
        YamlToObjectConverter objectConverter = new YamlToObjectConverter();
        ToscaNodeType toscaNodeType = objectConverter.convert(nodeTypesRepresentation.getBytes(), ToscaNodeType.class);
        Map<String, Object> interfaces = toscaNodeType.getInterfaces();
        for (Object interfaceVal : interfaces.values()) {
            Map<String, Object> interfaceDefinition = mapper.convertValue(interfaceVal, Map.class);
            for (Object operationVal : interfaceDefinition.values()) {
                if (operationVal instanceof Map) {
                    validateOperationInputDefinition(operationVal);
                }
            }
        }
    }

    private void validateOperationInputDefinition(Object operationVal) {
        ToscaLifecycleOperationDefinition operation =
                mapper.convertValue(operationVal, ToscaLifecycleOperationDefinition.class);
        Map<String, ToscaProperty> inputs = operation.getInputs();
        for (Map.Entry<String, ToscaProperty> inputEntry : inputs.entrySet()) {
            Assert.assertEquals(inputEntry.getKey().split("_")[1], inputEntry.getValue().getType());
            Boolean expectedIsRequired = Integer.parseInt(inputEntry.getKey().split("_")[2]) % 2 == 0;
            Assert.assertEquals(expectedIsRequired, inputEntry.getValue().getRequired());
        }
    }
}
