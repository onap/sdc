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
 */

package org.onap.sdc.tosca.datatypes.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
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
        Assert.assertEquals(expectedServiceTemplate, actualServiceTemplate);
    }

    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(NodeTemplate.class, hasValidGettersAndSettersExcluding("normalizeInterfaces"));
    }

    @Test
    public void shouldHaveValidEquals() {
        assertThat(NodeTemplate.class, hasValidBeanEqualsExcluding("normalizeInterfaces"));
    }

    @Test
    public void shouldHaveValidHashCode() {
        assertThat(NodeTemplate.class, hasValidBeanHashCodeExcluding("normalizeInterfaces"));
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
        Assert.assertNotNull(normalizeInterfaces);
        InterfaceDefinitionTemplate interfaceDefinitionTemplate = normalizeInterfaces.get(STANDARD_INTERFACE_KEY);
        Assert.assertNotNull(interfaceDefinitionTemplate);
        Assert.assertNotNull(interfaceDefinitionTemplate.getInputs());
        Assert.assertEquals(1, interfaceDefinitionTemplate.getInputs().size());
        Assert.assertNotNull(interfaceDefinitionTemplate.getOperations());
        Assert.assertEquals(1, interfaceDefinitionTemplate.getOperations().size());
        OperationDefinitionTemplate createOperation = interfaceDefinitionTemplate.getOperations().get(CREATE_OPER);
        Assert.assertNotNull(createOperation);
        Assert.assertNotNull(createOperation.getInputs());
        return interfaceDefinitionTemplate;
    }

    protected ServiceTemplate getServiceTemplate(String inputPath) throws IOException {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (InputStream yamlFile = toscaExtensionYamlUtil.loadYamlFileIs(inputPath)) {
            return toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);
        }
    }

}
