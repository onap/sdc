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

package org.onap.sdc.tosca.datatypes.model;

import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class AttributeDefinitionTest {

    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(AttributeDefinition.class, hasValidGettersAndSetters());
    }

    @Test
    public void shouldHaveValidCloneMethod() {
        AttributeDefinition attributeDefinition = new AttributeDefinition();
        attributeDefinition.setType("type");
        attributeDefinition.setDescription("description");
        attributeDefinition.set_default("default");
        attributeDefinition.setStatus(Status.SUPPORTED.getName());
        attributeDefinition.setEntry_schema(new EntrySchema());

        AttributeDefinition cloned = attributeDefinition.clone();
        assertEquals(attributeDefinition.getType(), cloned.getType());
        assertEquals(attributeDefinition.getDescription(), cloned.getDescription());
        assertEquals(attributeDefinition.get_default(), cloned.get_default());
        assertEquals(attributeDefinition.getStatus(), cloned.getStatus());

        assertThat(attributeDefinition.getEntry_schema().getConstraints(),
                is(cloned.getEntry_schema().getConstraints()));
        assertThat(attributeDefinition.getEntry_schema().getDescription(),
                is(cloned.getEntry_schema().getDescription()));
        assertThat(attributeDefinition.getEntry_schema().getType(),
                is(cloned.getEntry_schema().getType()));
    }
}