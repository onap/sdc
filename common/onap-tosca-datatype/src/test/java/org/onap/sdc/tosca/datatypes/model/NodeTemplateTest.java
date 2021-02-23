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
 *
 * Modifications copyright (c) 2019 Nokia
 * Modifications copyright (c) 2021 AT&T Intellectual Property
 */

package org.onap.sdc.tosca.datatypes.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.onap.sdc.tosca.services.ToscaExtensionYamlUtil;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.junit.Assert.assertThat;


public class NodeTemplateTest {

    private static final String NODE_WITH_INTERFACE = "nodeWithInterface";
    public static final String INTERFACE_KEY = "newInterface";
    public static final String INPUT_KEY = "newInput";
    public static final String INPUT_VAL = "myVal";
    public static final String OPER_KEY = "oper1";
    public static final String MY_WF_JSON = "myWf.json";
    public static final String STANDARD_INTERFACE_KEY = "Standard";
    public static final String CREATE_OPER = "create";
    public static final String NORMALIZE_INTERFACE_DEFINITION = "/mock/nodeTemplate/normalizeInterfaceDefinition.yaml";
    public static final String INTERFACE_DEFINITION_FOR_UPD_RESULT =
            "/mock/nodeTemplate/interfaceDefinitionForUpdResult.yaml";
    public static final String INTERFACE_DEFINITION_FOR_UPD = "/mock/nodeTemplate/interfaceDefinitionForUpd.yaml";

    @Test
    public void getNormalizeInterfacesTest() throws IOException {
        ServiceTemplate serviceTemplateFromYaml =
                getServiceTemplate(NORMALIZE_INTERFACE_DEFINITION);
        NodeTemplate nodeTemplate =
                serviceTemplateFromYaml.getTopology_template().getNode_templates().get(NODE_WITH_INTERFACE);
        Map<String, InterfaceDefinitionTemplate> normalizeInterfaces = nodeTemplate.getNormalizeInterfaces();
        chkData(normalizeInterfaces);

    }

    @Test
    public void addInterfacesTest() throws IOException {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        ServiceTemplate expectedServiceTemplateFromYaml = getServiceTemplate(INTERFACE_DEFINITION_FOR_UPD_RESULT);
        ServiceTemplate serviceTemplateForUpdate = getServiceTemplate(INTERFACE_DEFINITION_FOR_UPD);
        NodeTemplate nodeTemplate =
                serviceTemplateForUpdate.getTopology_template().getNode_templates().get(NODE_WITH_INTERFACE);
        nodeTemplate.addInterface(INTERFACE_KEY, createInterfaceDefinitionTemplate());

        String expectedServiceTemplate = toscaExtensionYamlUtil.objectToYaml(expectedServiceTemplateFromYaml);
        String actualServiceTemplate = toscaExtensionYamlUtil.objectToYaml(serviceTemplateForUpdate);
        assertEquals(expectedServiceTemplate, actualServiceTemplate);
    }

    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(NodeTemplate.class, hasValidGettersAndSettersExcluding("requirements", "normalizeInterfaces"));
    }

    @Test
    public void shouldHaveValidEquals() {
        assertThat(NodeTemplate.class, hasValidBeanEqualsExcluding("requirements", "normalizeInterfaces"));
    }

    @Test
    public void shouldHaveValidHashCode() {
        assertThat(NodeTemplate.class, hasValidBeanHashCodeExcluding("requirements", "normalizeInterfaces"));
    }

    @Test
    public void setRequirementsTest() throws IOException {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        ServiceTemplate expectedServiceTemplateFromYaml = getServiceTemplate(INTERFACE_DEFINITION_FOR_UPD_RESULT);
        ServiceTemplate serviceTemplateForUpdate = getServiceTemplate(INTERFACE_DEFINITION_FOR_UPD);
        NodeTemplate nodeTemplate =
                serviceTemplateForUpdate.getTopology_template().getNode_templates().get(NODE_WITH_INTERFACE);
        nodeTemplate.addInterface(INTERFACE_KEY, createInterfaceDefinitionTemplate());

        List<RequirementAssignment> requirementAssignmentList = new LinkedList<>();
        RequirementAssignment requirement1 = new RequirementAssignment();
        requirement1.setNode("node1");
        requirement1.setCapability("cap1");
        requirementAssignmentList.add(requirement1);
        nodeTemplate.setRequirements(requirementAssignmentList);

        List<Map<String, RequirementAssignment>> res = nodeTemplate.getRequirements();
        assertNotNull(res);
        assertEquals(res.size(), 0);

        RequirementAssignment requirement2 = new RequirementAssignment();
        requirement2.setNode("node2");
        requirement2.setCapability("cap2");
        HashMap<String, RequirementAssignment> map = new HashMap<>();
        map.put("value2", requirement2);
        nodeTemplate.addRequirements(map);
        List<Map<String, RequirementAssignment>> res2 = nodeTemplate.getRequirements();
        assertNotNull(res2);
        assertEquals(res2.size(), 1);
        assertEquals(res2.get(0), requirement2);
    }

    @Test
    public void addInterfaceTest() throws IOException {
        ServiceTemplate serviceTemplateForUpdate = getServiceTemplate(INTERFACE_DEFINITION_FOR_UPD);
        NodeTemplate nodeTemplate =
                serviceTemplateForUpdate.getTopology_template().getNode_templates().get(NODE_WITH_INTERFACE);
        nodeTemplate.addInterface(INTERFACE_KEY, createInterfaceDefinitionTemplate());

        Map<String, Object> res = nodeTemplate.getInterfaces();
        assertEquals(res.size(), 2);
        assertNotNull(res.get("Standard"));
        assertNotNull(res.get(INTERFACE_KEY));
    }

    @Test
    public void cloneTest() throws IOException {
        ServiceTemplate serviceTemplateForUpdate = getServiceTemplate(INTERFACE_DEFINITION_FOR_UPD);
        NodeTemplate nodeTemplate =
                serviceTemplateForUpdate.getTopology_template().getNode_templates().get(NODE_WITH_INTERFACE);

        NodeTemplate res = nodeTemplate.clone();
        assertEquals(res, nodeTemplate);
    }

    @Test
    public void convertToscaRequirementAssignmentTest() throws IOException {
        List<?> requirementAssignmentObj = new LinkedList<>();
        List<Map<String, RequirementAssignment>> res = NodeTemplate.convertToscaRequirementAssignment(requirementAssignmentObj);
        assertNull(res);

        Map<String, Object> map = new HashMap<>();
        map.put("value1", new RequirementAssignment());
        Map<String, Object> requirementMap = new HashMap<>();
        requirementMap.put("capability", "capabilityValue");
        requirementMap.put("node", "nodeValue");
        requirementMap.put("relationship", "relationshipValue");
        requirementMap.put("node_filter", new NodeFilter());
        Object[] objectArr = {};
        requirementMap.put("occurrences", objectArr);
        map.put("value2", requirementMap);
        ((List<Map<String, Object>>)requirementAssignmentObj).add(map);
        List<Map<String, RequirementAssignment>> res2 = NodeTemplate.convertToscaRequirementAssignment(requirementAssignmentObj);
        assertNotNull(res2);
        assertEquals(res2.size(), 2);
        assertEquals(res2.get(0), new RequirementAssignment());
    }

    private InterfaceDefinitionTemplate createInterfaceDefinitionTemplate() {
        InterfaceDefinitionTemplate interfaceDefinitionTemplate = new InterfaceDefinitionTemplate();
        interfaceDefinitionTemplate.setInputs(new HashMap<>());
        interfaceDefinitionTemplate.getInputs().put(INPUT_KEY, INPUT_VAL);
        interfaceDefinitionTemplate.addOperation(OPER_KEY, createOperationDefinitionTemplate());
        return interfaceDefinitionTemplate;
    }

    private OperationDefinitionTemplate createOperationDefinitionTemplate() {
        OperationDefinitionTemplate operationDefinitionTemplate = new OperationDefinitionTemplate();
        operationDefinitionTemplate.setImplementation(createImpl());
        return operationDefinitionTemplate;

    }

    private Implementation createImpl() {
        Implementation implementation = new Implementation();
        implementation.setPrimary(MY_WF_JSON);
        return implementation;
    }

    protected InterfaceDefinitionTemplate chkData(Map<String, InterfaceDefinitionTemplate> normalizeInterfaces) {
        assertNotNull(normalizeInterfaces);
        InterfaceDefinitionTemplate interfaceDefinitionTemplate = normalizeInterfaces.get(STANDARD_INTERFACE_KEY);
        assertNotNull(interfaceDefinitionTemplate);
        assertNotNull(interfaceDefinitionTemplate.getInputs());
        assertEquals(1, interfaceDefinitionTemplate.getInputs().size());
        assertNotNull(interfaceDefinitionTemplate.getOperations());
        assertEquals(1, interfaceDefinitionTemplate.getOperations().size());
        OperationDefinitionTemplate createOperation = interfaceDefinitionTemplate.getOperations().get(CREATE_OPER);
        assertNotNull(createOperation);
        assertNotNull(createOperation.getInputs());
        return interfaceDefinitionTemplate;
    }

    protected ServiceTemplate getServiceTemplate(String inputPath) throws IOException {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (InputStream yamlFile = toscaExtensionYamlUtil.loadYamlFileIs(inputPath)) {
            return toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);
        }
    }

}
