/*

 * Copyright (c) 2018 Huawei Intellectual Property.

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *     http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */
package org.openecomp.sdc.be.model.jsonjanusgraph.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.NodeType;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElementTypeEnum;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ModelConverterTest {
    @InjectMocks
    private ModelConverter test;

    @Test
    public void testConvertToToscaElementService()
    {
        Service service = new Service();
        service.setComponentType(ComponentTypeEnum.SERVICE);
        TopologyTemplate template = test.convertToToscaElement(service);
        assertThat(template.getToscaType()).isEqualTo(ToscaElementTypeEnum.TOPOLOGY_TEMPLATE);
    }

    @Test
    public void testConvertToToscaElementResource()
    {
        Resource resource = new Resource();
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        NodeType nodeType = test.convertToToscaElement(resource);
        assertThat(nodeType.getToscaType()).isEqualTo(ToscaElementTypeEnum.NODE_TYPE);
    }

    @Test
    public void testConvertFromToscaElementService()
    {
        TopologyTemplate topologyTemplate = new TopologyTemplate();
        topologyTemplate.setComponentType(ComponentTypeEnum.SERVICE);
        Component component = test.convertFromToscaElement(topologyTemplate);
        assertThat(component.getToscaType()).isEqualTo(ToscaElementTypeEnum.TOPOLOGY_TEMPLATE.getValue());
    }

    @Test
    public void testConvertFromToscaElementResource()
    {
        TopologyTemplate topologyTemplate = new TopologyTemplate();
        topologyTemplate.setComponentType(ComponentTypeEnum.RESOURCE);
        Component component = test.convertFromToscaElement(topologyTemplate);
        assertThat(component.getToscaType()).isEqualTo(ToscaElementTypeEnum.TOPOLOGY_TEMPLATE.getValue());
    }

    @Test
    public void testConvertFromToscaElementResourceType()
    {
        TopologyTemplate topologyTemplate = new TopologyTemplate();
        topologyTemplate.setComponentType(ComponentTypeEnum.RESOURCE);
        topologyTemplate.setResourceType(ResourceTypeEnum.PNF);
        Resource resource = test.convertFromToscaElement(topologyTemplate);
        assertSame(ResourceTypeEnum.PNF, resource.getResourceType());
    }

    @Test
    public void testIsAtomicComponent()
    {
        Resource component = new Resource();
        component.setComponentType(ComponentTypeEnum.RESOURCE);
        boolean result = test.isAtomicComponent(component);
        assertTrue(result);
    }

    @Test
    public void testGetVertexType()
    {
        VertexTypeEnum result;
        Resource component = new Resource();
        component.setComponentType(ComponentTypeEnum.RESOURCE);
        result = test.getVertexType(component);
        assertThat(result.getName()).isEqualTo("node_type");
    }
}
