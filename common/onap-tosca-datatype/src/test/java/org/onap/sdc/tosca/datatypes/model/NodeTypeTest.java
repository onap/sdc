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
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.onap.sdc.tosca.services.ToscaExtensionYamlUtil;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.junit.Assert.assertThat;


public class NodeTypeTest {

    private static final String NODE_WITH_INTERFACE = "amdocs.nodes.nodeWithInterface";
    public static final String NORMALIZE_INTERFACE_DEFINITION = "/mock/nodeType/normalizeInterfaceDefinition.yaml";
    public static final String STANDARD_INTERFACE_DEF = "Standard";

    @Test
    public void getNormalizeInterfacesTest() throws IOException {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (InputStream yamlFile = toscaExtensionYamlUtil.loadYamlFileIs(NORMALIZE_INTERFACE_DEFINITION)) {

            ServiceTemplate serviceTemplateFromYaml =
                    toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);
            Map<String, InterfaceDefinitionType> normalizeInterfacesNoEvent =
                    serviceTemplateFromYaml.getNode_types().get(NODE_WITH_INTERFACE).getNormalizeInterfaces();
            InterfaceDefinitionType interfaceDefinitionType = chkData(normalizeInterfacesNoEvent);
        }

    }

    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(NodeType.class, hasValidGettersAndSettersExcluding("normalizeInterfaces"));
    }

    protected InterfaceDefinitionType chkData(Map<String, InterfaceDefinitionType> normalizeInterfacesNoEvent) {
        Assert.assertNotNull(normalizeInterfacesNoEvent);
        InterfaceDefinitionType interfaceDefinitionType = normalizeInterfacesNoEvent.get(STANDARD_INTERFACE_DEF);
        Assert.assertNotNull(interfaceDefinitionType);
        Assert.assertNotNull(interfaceDefinitionType.getInputs());
        Assert.assertEquals(1, interfaceDefinitionType.getInputs().size());
        Assert.assertNotNull(interfaceDefinitionType.getOperations());
        Assert.assertEquals(1, interfaceDefinitionType.getOperations().size());
        return interfaceDefinitionType;
    }
}
