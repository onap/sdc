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
package org.openecomp.sdc.be.model.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.InputDefinition;

@RunWith(MockitoJUnitRunner.class)
public class ComponentUtilitiesTest {

    @Mock
    private Component component;
    @Mock
    private ComponentInstance componentInstance;
    private Optional<ComponentInstance> optionalComponentInstance;

    @Before
    public void setUp() throws Exception {
        optionalComponentInstance = Optional.of(componentInstance);
    }

    @Test
    public void shouldGetComponentInstanceNameByInstanceId() {
        Mockito.when(component.getComponentInstanceById("1")).thenReturn(optionalComponentInstance);
        Mockito.when(component.getComponentInstanceByName("1")).thenReturn(optionalComponentInstance);
        Mockito.when(componentInstance.getName()).thenReturn("1");
        Optional<String> componentInstanceNameByInstanceId = ComponentUtilities
            .getComponentInstanceNameByInstanceId(component, "1");
        assertEquals(componentInstanceNameByInstanceId.orElse(null), "1");
    }

    @Test
    public void shouldGetInputByName() {
        List<InputDefinition> inputs = new ArrayList<>();
        PropertyDataDefinition prop = new PropertyDataDefinition();
        prop.setName("test");
        InputDefinition input = new InputDefinition(prop);
        List<Annotation> annotations = new ArrayList<>();
        Annotation annotation = new Annotation();
        annotation.setName("annotation");
        annotations.add(annotation);
        input.setAnnotationsToInput(annotations);
        inputs.add(input);
        Mockito.when(component.safeGetInputs()).thenReturn(inputs);
        List<Annotation> result = ComponentUtilities.getInputAnnotations(component, "test");
        assertEquals(result.get(0), annotation);
    }
}