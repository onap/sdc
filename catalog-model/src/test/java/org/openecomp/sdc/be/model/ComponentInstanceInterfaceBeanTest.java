/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InterfaceInstanceDataDefinition;

public class ComponentInstanceInterfaceBeanTest {

    private static final String INTERFACE_ID = "interfaceId";
    private static final InterfaceDataDefinition INTERFACE_DATA_DEFINITION = new InterfaceDataDefinition();
    private static final InterfaceInstanceDataDefinition INTERFACE_INSTANCE_DATA_DEFINITION = new InterfaceInstanceDataDefinition();
    private static final String ID = "ID";

    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(ComponentInstanceInterface.class,
            hasValidGettersAndSettersExcluding(
                "definition",
                "ownerIdIfEmpty",
                "empty",
                "operationsMap",
                "version"));
    }

    @Test
    public void verifyConstructors() {
        INTERFACE_DATA_DEFINITION.setUniqueId(ID);
        ComponentInstanceInterface componentInstanceInterface1 = new ComponentInstanceInterface(INTERFACE_ID,
            INTERFACE_DATA_DEFINITION);
        ComponentInstanceInterface componentInstanceInterface2 = new ComponentInstanceInterface(INTERFACE_ID,
            INTERFACE_INSTANCE_DATA_DEFINITION);

        assertEquals(componentInstanceInterface1.getInterfaceId(), INTERFACE_ID);
        assertEquals(componentInstanceInterface2.getInterfaceId(), INTERFACE_ID);
        assertEquals(componentInstanceInterface1.getUniqueId(), ID);
        assertEquals(componentInstanceInterface2.getInterfaceInstanceDataDefinition(), INTERFACE_INSTANCE_DATA_DEFINITION);
    }
}