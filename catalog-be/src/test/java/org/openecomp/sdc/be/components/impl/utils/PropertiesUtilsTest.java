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
package org.openecomp.sdc.be.components.impl.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.utils.PropertiesUtils;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PropertiesUtilsTest {
    @Mock
    Service service;


    @Test
    public void testProxyServiceProperties(){
        when(service.getProperties()).thenReturn(Arrays.asList(buildPropertyDefinition("a"),buildPropertyDefinition("b")));
        when(service.getInputs()).thenReturn(Arrays.asList(buildInputDefinition("a"), buildInputDefinition("c")));

        final List<PropertyDefinition> properties = PropertiesUtils.getProperties(service);
        assertEquals(3, properties.size());
    }

    @Test
    public void testProxyServiceNullInputs(){
        when(service.getProperties()).thenReturn(Arrays.asList(buildPropertyDefinition("a"),buildPropertyDefinition("b")));
        when(service.getInputs()).thenReturn(null);

        final List<PropertyDefinition> properties = PropertiesUtils.getProperties(service);
        assertEquals(2, properties.size());
    }

    @Test
    public void testProxyServiceNullProperties(){
        when(service.getProperties()).thenReturn(null);
        when(service.getInputs()).thenReturn(Arrays.asList(buildInputDefinition("a"), buildInputDefinition("c")));

        final List<PropertyDefinition> properties = PropertiesUtils.getProperties(service);
        assertEquals(2, properties.size());
    }

    @Test
    public void testGetCapabilityProperty() {

        Assert.assertEquals(1, PropertiesUtils.getCapabilityProperty(createProperties(),
                "inputId").size());
    }

    @Test
    public void testGetPropertyCapabilityOfChildInstance() {
        CapabilityDefinition capabilityDefinition = createCapabilityDefinition();
        capabilityDefinition.setPath(Collections.singletonList("path"));
        Map<String, List<CapabilityDefinition>> capMap = new HashMap<>();
        capMap.put(capabilityDefinition.getType(), Collections.singletonList(capabilityDefinition));
        Assert.assertTrue(PropertiesUtils.getPropertyCapabilityOfChildInstance("capUniqueId",
                capMap).isPresent());
    }

    @Test
    public void testGetPropertyCapabilityFromAllCapProps() {
        CapabilityDefinition capabilityDefinition = createCapabilityDefinition();
        Map<String, List<CapabilityDefinition>> capMap = new HashMap<>();
        capMap.put(capabilityDefinition.getType(), Collections.singletonList(capabilityDefinition));
        Assert.assertTrue(PropertiesUtils.getPropertyCapabilityOfChildInstance("capUniqueId",
                capMap).isPresent());
    }

    @Test

    public void testGetPropertyByInputId() {
        Resource resource = new ResourceBuilder().setComponentType(ComponentTypeEnum.RESOURCE).setUniqueId("resourceId")
                .setName("name").build();
        CapabilityDefinition capabilityDefinition = createCapabilityDefinition();

        List<ComponentInstanceProperty> properties = new ArrayList<>();
        ComponentInstanceProperty instanceProperty = createProperties();

        List<GetInputValueDataDefinition> valueDataDefinitionList = new ArrayList<>();
        GetInputValueDataDefinition getInputValueDataDefinition = new GetInputValueDataDefinition();
        getInputValueDataDefinition.setInputId("inputId");
        getInputValueDataDefinition.setPropName("prop_name");
        valueDataDefinitionList.add(getInputValueDataDefinition);

        instanceProperty.setGetInputValues(valueDataDefinitionList);
        properties.add(instanceProperty);
        capabilityDefinition.setProperties(properties);
        Map<String, List<CapabilityDefinition>> capabilityMap = new HashMap<>();
        capabilityMap.put(capabilityDefinition.getType(), Collections.singletonList(capabilityDefinition));
        resource.setCapabilities(capabilityMap);

        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setUniqueId("inputId");
        inputDefinition.setInputId("inputId");
        inputDefinition.setPropertyId("inputId");
        resource.setInputs(Collections.singletonList(inputDefinition));
        Assert.assertTrue(PropertiesUtils.getPropertyByInputId(resource, "inputId").isPresent());
    }

    @Test
    public void testIsNodeServiceProxy() {
       Resource resource = new ResourceBuilder().setComponentType(ComponentTypeEnum.RESOURCE).setUniqueId("resourceId")
                .setName("name").build();
       resource.setResourceType(ResourceTypeEnum.ServiceProxy);
        Assert.assertTrue( PropertiesUtils.isNodeServiceProxy(resource));
    }

    @Test
    public void testProxyServiceAllNull(){
        when(service.getProperties()).thenReturn(null);
        when(service.getInputs()).thenReturn(null);

        final List<PropertyDefinition> properties = PropertiesUtils.getProperties(service);
        assertEquals(0, properties.size());
    }

    @Test
    public void testProxyInstanceGetPropertiesUndeclaredPropertyWithValue(){
        String undeclaredPropertyValue = "testPropDefaultValue";
        List<PropertyDefinition> propertyDefinitions =
                Collections.singletonList(buildPropertyDefinition("undeclaredProperty", undeclaredPropertyValue));
        when(service.getProperties()).thenReturn(propertyDefinitions);
        when(service.getInputs()).thenReturn(null);
        final List<PropertyDefinition> properties = PropertiesUtils.getProperties(service);
        assertEquals(1, properties.size());
        assertEquals(undeclaredPropertyValue, properties.get(0).getValue());
    }

    @Test
    public void testProxyInstanceGetPropertiesUndeclaredPropertyWithoutValue(){
        List<PropertyDefinition> propertyDefinitions =
                Collections.singletonList(buildPropertyDefinition("undeclaredProperty"));
        when(service.getProperties()).thenReturn(propertyDefinitions);
        when(service.getInputs()).thenReturn(null);
        final List<PropertyDefinition> properties = PropertiesUtils.getProperties(service);
        assertEquals(1, properties.size());
        assertNull(properties.get(0).getValue());
    }

    @Test
    public void testProxyInstanceGetPropertiesResolvePropertyValueFromInput() {
        String declaredPropertyName = "declaredProperty";
        String mappedInputName = "mappedInput";
        //Setting default value in input
        String inputValue = "testDefaultValue";
        List<PropertyDefinition> propertyDefinitions =
                Collections.singletonList(buildPropertyDefinitionForDeclaredProperty(
                        declaredPropertyName, mappedInputName));
        when(service.getProperties()).thenReturn(propertyDefinitions);
        List<InputDefinition> inputDefinitions =
                Collections.singletonList(buildInputDefinitionForMappedProperty(mappedInputName, inputValue,
                        "componentUUID." + declaredPropertyName));
        when(service.getInputs()).thenReturn(inputDefinitions);
        final List<PropertyDefinition> properties = PropertiesUtils.getProperties(service);
        assertEquals(2, properties.size());

        Optional<PropertyDefinition> declaredProperty = properties.stream()
                .filter(propertyDefinition -> propertyDefinition.getName().equals(declaredPropertyName))
                .findFirst();
        Assert.assertTrue(declaredProperty.isPresent());
        assertEquals(inputValue, declaredProperty.get().getValue());
    }


    @Test
    public void testResolvePropertyValueFromInput() {
        String mappedInputValue = "Default String Input Value";
        PropertyDefinition mappedProperty =
                buildPropertyDefinitionForDeclaredProperty("componentPropStr1", "componentInputStr1");
        List<InputDefinition> componentInputs =
                Collections.singletonList(buildInputDefinitionForMappedProperty("componentInputStr1", mappedInputValue,
                        "componentUUID.componentPropStr1"));
        PropertyDefinition updatedPropertyDefinition =
                PropertiesUtils.resolvePropertyValueFromInput(mappedProperty, componentInputs);
        Assert.assertNotNull(updatedPropertyDefinition);
        Assert.assertEquals(mappedInputValue, updatedPropertyDefinition.getValue());
    }


    @Test
    public void testResolvePropertyValueFromInputNoInputs() {
        PropertyDefinition mappedProperty =
                buildPropertyDefinitionForDeclaredProperty("componentPropStr1", "componentInputStr1");
        PropertyDefinition updatedPropertyDefinition =
                PropertiesUtils.resolvePropertyValueFromInput(mappedProperty, null);
        Assert.assertNotNull(updatedPropertyDefinition);
        Assert.assertEquals(mappedProperty.getValue(), updatedPropertyDefinition.getValue());
    }

    @Test
    public void testResolvePropertyValueFromInputPropertyDefinitionNull() {
        List<InputDefinition> componentInputs =
                Arrays.asList(buildInputDefinitionForMappedProperty("componentInputStr1", "Default Value",
                        "componentPropStr1"), buildInputDefinitionForMappedProperty("componentInputStr2",
                        "Default String Input2", "componentPropStr2"));
        PropertyDefinition updatedPropertyDefinition =
                PropertiesUtils.resolvePropertyValueFromInput(null, componentInputs);
        Assert.assertNull(updatedPropertyDefinition);
    }

    @Test
    public void testResolvePropertyValueFromInputUndeclaredProperty() {
        String propertyValue = "Default String Property Value";
        PropertyDefinition undeclaredProperty =
                buildPropertyDefinition("componentPropStr1", propertyValue);
        List<InputDefinition> componentInputs =
                Arrays.asList(buildInputDefinition("componentInputStr1"), buildInputDefinition("componentInputStr2"));
        PropertyDefinition updatedPropertyDefinition =
                PropertiesUtils.resolvePropertyValueFromInput(undeclaredProperty, componentInputs);
        Assert.assertNotNull(updatedPropertyDefinition);
        Assert.assertEquals(undeclaredProperty.getValue(), updatedPropertyDefinition.getValue());
    }

    private PropertyDefinition buildPropertyDefinition(String name) {
        PropertyDefinition retVal = new PropertyDefinition();
        retVal.setUniqueId("componentUUID." + name);
        retVal.setName(name);
        return retVal;
    }

    private PropertyDefinition buildPropertyDefinition(String name, String value) {
        PropertyDefinition retVal = buildPropertyDefinition(name);
        retVal.setValue(value);
        return retVal;
    }

    private InputDefinition buildInputDefinition(String name){
        InputDefinition retVal = new InputDefinition();
        retVal.setName(name);
        return retVal;
    }

    private PropertyDefinition buildPropertyDefinitionForDeclaredProperty(String propertyName, String inputName){
        String declaredPropertyValue =  "{get_input : " + inputName + " }";
        return buildPropertyDefinition(propertyName, declaredPropertyValue);
    }

    private InputDefinition buildInputDefinitionForMappedProperty(String inputName, String inputValue,
                                                                  String mappedPropertyId){
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setName(inputName);
        inputDefinition.setType("string");
        inputDefinition.setPropertyId(mappedPropertyId);
        inputDefinition.setDefaultValue(inputValue);
        inputDefinition.setValue(inputValue);
        return inputDefinition;
    }

    private ComponentInstanceProperty createProperties() {
        ComponentInstanceProperty instanceProperty = new ComponentInstanceProperty();
        instanceProperty.setUniqueId("inputId");
        instanceProperty.setType("Integer");
        instanceProperty.setName("prop_name");
        instanceProperty.setDescription("prop_description_prop_desc");
        instanceProperty.setOwnerId("capUniqueId");
        instanceProperty.setValue("{\"get_input\":\"extcp20_order\"}");
        instanceProperty.setSchema(new SchemaDefinition());

        List<GetInputValueDataDefinition> valueDataDefinitionList = new ArrayList<>();
        GetInputValueDataDefinition getInputValueDataDefinition = new GetInputValueDataDefinition();
        getInputValueDataDefinition.setInputId("inputId");
        getInputValueDataDefinition.setPropName("prop_name");
        valueDataDefinitionList.add(getInputValueDataDefinition);
        instanceProperty.setGetInputValues(valueDataDefinitionList);
        return instanceProperty;
    }

    private CapabilityDefinition createCapabilityDefinition() {
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName("cap" + Math.random());
        capabilityDefinition.setType("tosca.capabilities.network.Bindable");
        capabilityDefinition.setOwnerId("resourceId");
        capabilityDefinition.setUniqueId("capUniqueId");
        List<String> path = new ArrayList<>();
        path.add("path1");
        capabilityDefinition.setPath(path);
        return capabilityDefinition;
    }

}
