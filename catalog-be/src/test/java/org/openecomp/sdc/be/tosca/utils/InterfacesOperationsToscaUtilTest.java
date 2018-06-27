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
import org.openecomp.sdc.be.datatypes.elements.OperationOutputDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.tosca.ToscaRepresentation;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaTemplate;

public class InterfacesOperationsToscaUtilTest {

    @BeforeClass
    public static void setUp() {
        new DummyConfigurationManager();
    }


    @Test
    public void addInterfaceTypeElement() {
        Component component = new Resource();
        component.setNormalizedName("normalizedComponentName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setToscaResourceName("interface.types.test_resource_name");
        addOperationsToInterface(addedInterface, 5, 3, 0, true, false);
        final String interfaceType = "normalizedComponentName-interface";
        ((Resource) component).setInterfaces(new HashMap<>());
        ((Resource) component).getInterfaces().put(interfaceType, addedInterface);
        final Map<String, Object> interfaceTypeElement =
                InterfacesOperationsToscaUtil.addInterfaceTypeElement(component);

        ToscaExportHandler handler = new ToscaExportHandler();
        ToscaTemplate template = new ToscaTemplate("test");
        template.setInterface_types(interfaceTypeElement);
        final ToscaRepresentation toscaRepresentation = handler.createToscaRepresentation(template);

        Assert.assertFalse(toscaRepresentation.getMainYaml().contains("operations"));
        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("interface.types.test_resource_name"));
    }

    @Test
    public void addInterfaceDefinitionElement() {
        Component component = new Resource();
        component.setNormalizedName("normalizedComponentName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setToscaResourceName("com.some.resource.or.other.resourceName");

        addOperationsToInterface(addedInterface, 3, 2, 0, true, false);
        final String interfaceType = "normalizedComponentName-interface";
        ((Resource) component).setInterfaces(new HashMap<>());
        ((Resource) component).getInterfaces().put(interfaceType, addedInterface);
        ToscaNodeType nodeType = new ToscaNodeType();
        InterfacesOperationsToscaUtil.addInterfaceDefinitionElement(component, nodeType);

        ToscaExportHandler handler = new ToscaExportHandler();
        ToscaTemplate template = new ToscaTemplate("test");
        Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
        nodeTypes.put("test", nodeType);
        template.setNode_types(nodeTypes);
        final ToscaRepresentation toscaRepresentation = handler.createToscaRepresentation(template);

        Assert.assertFalse(toscaRepresentation.getMainYaml().contains("operations"));
        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("resourceName:"));
        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("inputs:"));
        Assert.assertFalse(toscaRepresentation.getMainYaml().contains("defaultp"));
        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("has description"));
        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("naming_function_"));
        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("com.some.resource.or.other.resourceName"));
    }

    @Test
    public void addInterfaceDefinitionElement_noInputs() {
        Component component = new Resource();
        component.setNormalizedName("normalizedComponentName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setToscaResourceName("com.some.resource.or.other.resourceNameNoInputs");

        addOperationsToInterface(addedInterface, 3, 3, 0, false, false);
        final String interfaceType = "normalizedComponentName-interface";
        ((Resource) component).setInterfaces(new HashMap<>());
        ((Resource) component).getInterfaces().put(interfaceType, addedInterface);
        ToscaNodeType nodeType = new ToscaNodeType();
        InterfacesOperationsToscaUtil.addInterfaceDefinitionElement(component, nodeType);

        ToscaExportHandler handler = new ToscaExportHandler();
        ToscaTemplate template = new ToscaTemplate("test");
        Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
        nodeTypes.put("test", nodeType);
        template.setNode_types(nodeTypes);
        final ToscaRepresentation toscaRepresentation = handler.createToscaRepresentation(template);

        Assert.assertFalse(toscaRepresentation.getMainYaml().contains("operations"));
        Assert.assertFalse(toscaRepresentation.getMainYaml().contains("input_"));
        Assert.assertFalse(toscaRepresentation.getMainYaml().contains("defaultp"));
        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("resourceNameNoInputs:"));
        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("has description"));
        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("com.some.resource.or.other.resourceName"));
    }

    @Test
    public void addInterfaceDefinitionElementWithOutputs() {
        Component component = new Resource();
        component.setNormalizedName("normalizedComponentName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setToscaResourceName("com.some.resource.or.other.resourceName");

        addOperationsToInterface(addedInterface, 2, 0, 2, false, true);
        final String interfaceType = "normalizedComponentName-interface";
        ((Resource) component).setInterfaces(new HashMap<>());
        ((Resource) component).getInterfaces().put(interfaceType, addedInterface);
        ToscaNodeType nodeType = new ToscaNodeType();
        InterfacesOperationsToscaUtil.addInterfaceDefinitionElement(component, nodeType);

        ToscaExportHandler handler = new ToscaExportHandler();
        ToscaTemplate template = new ToscaTemplate("test");
        Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
        nodeTypes.put("test", nodeType);
        template.setNode_types(nodeTypes);
        final ToscaRepresentation toscaRepresentation = handler.createToscaRepresentation(template);

        Assert.assertFalse(toscaRepresentation.getMainYaml().contains("operations"));
        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("resourceName:"));
        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("outputs:"));
        Assert.assertFalse(toscaRepresentation.getMainYaml().contains("defaultp"));
        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("has description"));
        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("naming_function_"));
        Assert.assertTrue(toscaRepresentation.getMainYaml().contains("com.some.resource.or.other.resourceName"));
    }

    private void addOperationsToInterface(InterfaceDefinition addedInterface, int numOfOps, int numOfInputsPerOp,
                                          int numOfOutputsPerOp, boolean hasInputs, boolean hasOutputs) {

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
            if (hasOutputs) {
                operation.setOutputs(createOutputs(numOfOutputsPerOp));
            }
            addedInterface.getOperations().put(operation.getName(), operation);
        }
    }


    private ListDataDefinition<OperationInputDefinition> createInputs(int numOfInputs) {
        ListDataDefinition<OperationInputDefinition> operationInputDefinitionList = new ListDataDefinition<>();
        for (int i = 0; i < numOfInputs; i++) {
            operationInputDefinitionList.add(createMockOperationInputDefinition("input_" + i,
                    java.util.UUID.randomUUID().toString() + "." + "naming_function_" + i));
        }
        return operationInputDefinitionList;
    }

    private ListDataDefinition<OperationOutputDefinition> createOutputs(int numOfOutputs) {
        ListDataDefinition<OperationOutputDefinition> operationOutputDefinitionList = new ListDataDefinition<>();
        for (int i = 0; i < numOfOutputs; i++) {
            operationOutputDefinitionList.add(createMockOperationOutputDefinition("output_" + i,
                    java.util.UUID.randomUUID().toString() + "." + "naming_function_" + i, getTestOutputType(i)));
        }
        return operationOutputDefinitionList;
    }

    private String getTestOutputType(int i) {
        return i%2 == 0 ? "integer" : "boolean";
    }

    private OperationInputDefinition createMockOperationInputDefinition(String name, String id) {
        OperationInputDefinition operationInputDefinition = new OperationInputDefinition();
        operationInputDefinition.setName(name);
        operationInputDefinition.setInputId(id);
        return operationInputDefinition;
    }

    private OperationOutputDefinition createMockOperationOutputDefinition(String name, String id, String type) {
        OperationOutputDefinition operationOutputDefinition = new OperationOutputDefinition();
        operationOutputDefinition.setName(name);
        operationOutputDefinition.setInputId(id);
        operationOutputDefinition.setType(type);
        return operationOutputDefinition;
    }

}
