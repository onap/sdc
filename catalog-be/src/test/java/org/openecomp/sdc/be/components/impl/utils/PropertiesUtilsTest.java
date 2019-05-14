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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@RunWith(MockitoJUnitRunner.class)
public class PropertiesUtilsTest {
    @Mock
    Service service;


    @Test
    public void testProxyServiceProperties(){
        when(service.getProperties()).thenReturn(Arrays.asList(buildPropertyDefinition("a"),buildPropertyDefinition("b")));
        when(service.getInputs()).thenReturn(Arrays.asList(buildInputDefiniton("a"),buildInputDefiniton("c")));

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
        when(service.getInputs()).thenReturn(Arrays.asList(buildInputDefiniton("a"),buildInputDefiniton("c")));

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

    private PropertyDefinition buildPropertyDefinition(String name){
        PropertyDefinition retVal = new PropertyDefinition();
        retVal.setName(name);
        return retVal;
    }

    private InputDefinition buildInputDefiniton(String name){
        InputDefinition retVal = new InputDefinition();
        retVal.setName(name);
        return retVal;
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
