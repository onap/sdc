/*
 * Copyright © 2016-2019 European Support Limited
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

package org.openecomp.sdc.be.tosca;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.openecomp.sdc.be.tosca.utils.ToscaExportUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ToscaExportUtilsTest {

    private static final Map<String, DataTypeDefinition> dataTypes = new HashMap<>();

    @Test
    public void testGetProxyNodeTypeInterfacesNoInterfaces() {
        Component service = new Service();
        Optional<Map<String, Object>> proxyNodeTypeInterfaces =
                ToscaExportUtils.getProxyNodeTypeInterfaces(service, dataTypes);
        Assert.assertFalse(proxyNodeTypeInterfaces.isPresent());
    }

    @Test
    public void testGetProxyNodeTypeInterfaces() {
        Component service = getTestComponent();
        Optional<Map<String, Object>> proxyNodeTypeInterfaces =
                ToscaExportUtils.getProxyNodeTypeInterfaces(service, dataTypes);
        Assert.assertTrue(proxyNodeTypeInterfaces.isPresent());
        Map<String, Object> componentInterfaces = proxyNodeTypeInterfaces.get();
        Assert.assertNotNull(componentInterfaces);
        Assert.assertEquals(1, componentInterfaces.size());
    }


    @Test
    public void testGetProxyNodeTypePropertiesComponentNull() {
        Optional<Map<String, ToscaProperty>> proxyNodeTypeProperties =
                ToscaExportUtils.getProxyNodeTypeProperties(null, dataTypes);
        Assert.assertFalse(proxyNodeTypeProperties.isPresent());
    }

    @Test
    public void testGetProxyNodeTypePropertiesNoProperties() {
        Component service = new Service();
        Optional<Map<String, ToscaProperty>> proxyNodeTypeProperties =
                ToscaExportUtils.getProxyNodeTypeProperties(service, dataTypes);
        Assert.assertFalse(proxyNodeTypeProperties.isPresent());
    }

    @Test
    public void testGetProxyNodeTypeProperties() {
        Component service = getTestComponent();
        service.setProperties(Arrays.asList(createMockProperty("componentPropStr", "Default String Prop"),
                createMockProperty("componentPropInt", null)));
        Optional<Map<String, ToscaProperty>> proxyNodeTypeProperties =
                ToscaExportUtils.getProxyNodeTypeProperties(service, dataTypes);
        Assert.assertTrue(proxyNodeTypeProperties.isPresent());
        Map<String, ToscaProperty> componentProperties = proxyNodeTypeProperties.get();
        Assert.assertNotNull(componentProperties);
        Assert.assertEquals(2, componentProperties.size());
    }

    @Test
    public void testAddInputsToPropertiesNoInputs() {
        Component service = getTestComponent();
        service.setProperties(Arrays.asList(createMockProperty("componentPropStr", "Default String Prop"),
                createMockProperty("componentPropInt", null)));
        Optional<Map<String, ToscaProperty>> proxyNodeTypePropertiesResult =
                ToscaExportUtils.getProxyNodeTypeProperties(service, dataTypes);

        Assert.assertTrue(proxyNodeTypePropertiesResult.isPresent());
        Map<String, ToscaProperty> proxyNodeTypeProperties = proxyNodeTypePropertiesResult.get();
        ToscaExportUtils.addInputsToProperties(dataTypes, null, proxyNodeTypeProperties);
        Assert.assertNotNull(proxyNodeTypeProperties);
        Assert.assertEquals(2, proxyNodeTypeProperties.size());
        ToscaExportUtils.addInputsToProperties(dataTypes, new ArrayList<>(), proxyNodeTypeProperties);
        Assert.assertEquals(2, proxyNodeTypeProperties.size());
    }

    @Test
    public void testAddInputsToPropertiesWithInputs() {
        Component service = getTestComponent();
        service.setProperties(Arrays.asList(createMockProperty("componentPropStr", "Default String Prop"),
                createMockProperty("componentPropInt", null)));
        service.setInputs(Arrays.asList(createMockInput("componentInputStr1",
                "Default String Input1"), createMockInput("componentInputStr2", "Default String Input2")));
        Optional<Map<String, ToscaProperty>> proxyNodeTypePropertiesResult =
                ToscaExportUtils.getProxyNodeTypeProperties(service, dataTypes);

        Assert.assertTrue(proxyNodeTypePropertiesResult.isPresent());
        Map<String, ToscaProperty> proxyNodeTypeProperties = proxyNodeTypePropertiesResult.get();
        ToscaExportUtils.addInputsToProperties(dataTypes, service.getInputs(), proxyNodeTypeProperties);
        Assert.assertNotNull(proxyNodeTypeProperties);
        Assert.assertEquals(4, proxyNodeTypeProperties.size());
        Assert.assertNotNull(proxyNodeTypeProperties.get("componentInputStr1"));
        Assert.assertNotNull(proxyNodeTypeProperties.get("componentInputStr2"));
    }

    @Test
    public void testAddInputsToPropertiesOnlyInputs() {
        Component service = getTestComponent();
        service.setInputs(Arrays.asList(createMockInput("componentInputStr1",
                "Default String Input1"), createMockInput("componentInputStr2", "Default String Input2")));
        Optional<Map<String, ToscaProperty>> proxyNodeTypePropertiesResult =
                ToscaExportUtils.getProxyNodeTypeProperties(service, dataTypes);

        Assert.assertTrue(proxyNodeTypePropertiesResult.isPresent());
        Map<String, ToscaProperty> proxyNodeTypeProperties = proxyNodeTypePropertiesResult.get();
        ToscaExportUtils.addInputsToProperties(dataTypes, service.getInputs(), proxyNodeTypeProperties);
        Assert.assertNotNull(proxyNodeTypeProperties);
        Assert.assertEquals(2, proxyNodeTypeProperties.size());
        Assert.assertNotNull(proxyNodeTypeProperties.get("componentInputStr1"));
        Assert.assertNotNull(proxyNodeTypeProperties.get("componentInputStr2"));
    }

    @Test
    public void testOperationImplementationInProxyNodeTypeNotPresent() {
        Component service = getTestComponent();
        InterfaceDefinition interfaceDefinition =
                service.getInterfaces().get("normalizedServiceComponentName-interface");
        interfaceDefinition.setOperations(new HashMap<>());
        final OperationDataDefinition operation = new OperationDataDefinition();
        operation.setName("start");
        operation.setDescription("op description");
        final ArtifactDataDefinition implementation = new ArtifactDataDefinition();
        implementation.setArtifactName("createBPMN.bpmn");
        operation.setImplementation(implementation);
        interfaceDefinition.getOperations().put(operation.getName(), operation);
        service.getInterfaces().put("normalizedServiceComponentName-interface", interfaceDefinition);
        service.setInputs(Arrays.asList(createMockInput("componentInputStr1",
                "Default String Input1"), createMockInput("componentInputStr2", "Default String Input2")));
        Optional<Map<String, Object>> proxyNodeTypeInterfaces =
                ToscaExportUtils.getProxyNodeTypeInterfaces(service, dataTypes);
        Assert.assertTrue(proxyNodeTypeInterfaces.isPresent());
        Map<String, Object> componentInterfaces = proxyNodeTypeInterfaces.get();
        Assert.assertNotNull(componentInterfaces);
        Assert.assertEquals(1, componentInterfaces.size());
        Map<String, Object> proxyInterfaceDefinition =
                (Map<String, Object>) componentInterfaces.get("serviceName");
        Map<String, Object> startOperationDefinition = (Map<String, Object>) proxyInterfaceDefinition.get("start");
        Assert.assertNotNull(startOperationDefinition);
        Assert.assertNull(startOperationDefinition.get("implementation"));
    }

    private Component getTestComponent() {
        Component component = new Service();
        component.setNormalizedName("normalizedServiceComponentName");
        InterfaceDefinition addedInterface = new InterfaceDefinition();
        addedInterface.setType("com.some.service.or.other.serviceName");
        final String interfaceType = "normalizedServiceComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);
        return component;
    }

    private PropertyDefinition createMockProperty(String propertyName, String defaultValue){
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setName(propertyName);
        propertyDefinition.setType("string");
        propertyDefinition.setDefaultValue(defaultValue);
        return propertyDefinition;
    }

    private InputDefinition createMockInput(String inputName, String defaultValue){
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setName(inputName);
        inputDefinition.setType("string");
        inputDefinition.setDefaultValue(defaultValue);
        return inputDefinition;
    }
}
