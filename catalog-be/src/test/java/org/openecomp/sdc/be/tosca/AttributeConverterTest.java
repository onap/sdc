/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021, Nordix Foundation. All rights reserved.
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
package org.openecomp.sdc.be.tosca;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.sdc.tosca.datatypes.model.EntrySchema;
import org.openecomp.sdc.be.datatypes.elements.GetOutputValueDataDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.tosca.model.ToscaAttribute;
import org.openecomp.sdc.be.tosca.utils.OutputConverter;

@ExtendWith(MockitoExtension.class)
class AttributeConverterTest {

    @Test
    void testCtor() {
        assertThat(new AttributeConverter(new HashMap<>()), instanceOf(AttributeConverter.class));
    }

    @Test
    void test_convert() {
        final AttributeConverter testSubject = Mockito.spy(new AttributeConverter(new HashMap<>()));
        final String descriptionMock = "DescriptionMock";
        final String statusMock = "StatusMock";
        final String type = "string";

        final AttributeDefinition attributeDefinition = new AttributeDefinition();
        attributeDefinition.setName("mock");
        attributeDefinition.setType(type);
        attributeDefinition.setDefaultValue("DefaultValueMock");
        attributeDefinition.setDescription(descriptionMock);
        attributeDefinition.setStatus(statusMock);
        attributeDefinition.setEntry_schema(new EntrySchema());

        final ToscaAttribute result = testSubject.convert(attributeDefinition);

        assertNotNull(result);
        assertNull(result.getValue());
        assertNotNull(result.getDescription());
        assertEquals(descriptionMock, result.getDescription());
        assertNotNull(result.getStatus());
        assertEquals(statusMock, result.getStatus());
        assertNotNull(result.getType());
        assertEquals(type, result.getType());

    }
}
