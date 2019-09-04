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
import static org.hamcrest.MatcherAssert.assertThat;

public class ServiceTemplateTest {

    private static final String INTERFACE_NO_OPER = "amdocs.interfaces.interfaceNoOper";
    private static final String LIFECYCLE_STANDARD = "tosca.interfaces.node.lifecycle.Standard";
    private static final String INTERFACE_WITH_OPER = "amdocs.interfaces.interfaceWithOper";
    public static final String NORMALIZE_INTERFACE_TYPE = "/mock/serviceTemplate/normalizeInterfaceType.yaml";
    public static final String NEW_OPER_1 = "newOper1";
    public static final String NEW_OPER_2 = "newOper2";

    @Test
    public void getNormalizeInterfaceTypesTest() throws IOException {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (InputStream yamlFile = toscaExtensionYamlUtil
                                            .loadYamlFileIs(NORMALIZE_INTERFACE_TYPE)) {

            ServiceTemplate serviceTemplateFromYaml =
                    toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);
            Map<String, InterfaceType> normalizeInterfaceTypes = serviceTemplateFromYaml.getNormalizeInterfaceTypes();
            Assert.assertNotNull(normalizeInterfaceTypes);

            InterfaceType interfaceNoOper = normalizeInterfaceTypes.get(INTERFACE_NO_OPER);
            Assert.assertNotNull(interfaceNoOper);
            Assert.assertEquals(LIFECYCLE_STANDARD, interfaceNoOper.getDerived_from());
            Assert.assertNull(interfaceNoOper.getOperations());

            InterfaceType interfaceWithOper = normalizeInterfaceTypes.get(INTERFACE_WITH_OPER);
            Assert.assertNotNull(interfaceWithOper);
            Assert.assertEquals(LIFECYCLE_STANDARD, interfaceWithOper.getDerived_from());
            Assert.assertNotNull(interfaceWithOper.getOperations());
            Assert.assertEquals(2, interfaceWithOper.getOperations().size());
            Assert.assertNull(interfaceWithOper.getOperations().get(NEW_OPER_1));
            Assert.assertNotNull(interfaceWithOper.getOperations().get(NEW_OPER_2));
            Assert.assertNotNull(interfaceWithOper.getOperations().get(NEW_OPER_2).getDescription());
        }

    }

    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(ServiceTemplate.class,
                hasValidGettersAndSettersExcluding("imports", "normalizeInterfaceTypes"));
    }
}
