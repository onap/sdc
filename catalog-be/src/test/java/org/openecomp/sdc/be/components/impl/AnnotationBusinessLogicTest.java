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
package org.openecomp.sdc.be.components.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.validation.AnnotationValidator;
import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.model.AnnotationTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.operations.impl.AnnotationTypeOperations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class AnnotationBusinessLogicTest {
    private static final String TEST = "TEST";

    @Mock
    private AnnotationTypeOperations annotationTypeOperations;
    @Mock
    private AnnotationValidator annotationValidator;
    @Mock
    private InputDefinition inputDefinition;
    @Mock
    private Annotation annotation;
    @Mock
    private AnnotationTypeDefinition dbAnnotationTypeDefinition;

    @Test
    public void checkGetter() {
        AnnotationBusinessLogic annotationBusinessLogic = new AnnotationBusinessLogic(annotationTypeOperations,
            annotationValidator);
        assertEquals(annotationBusinessLogic.getAnnotationTypeOperations(), annotationTypeOperations);
    }

    @Test
    public void shouldValidateAndMergeAnnotationsAndAssignToInput() {
        AnnotationBusinessLogic annotationBusinessLogic = new AnnotationBusinessLogic(annotationTypeOperations,
            annotationValidator);
        Map<String, InputDefinition> inputs = new HashMap<>();
        inputs.put(TEST, inputDefinition);
        List<Annotation> annotations = new ArrayList<>();
        annotations.add(annotation);
        Mockito.when(inputDefinition.getAnnotations()).thenReturn(annotations);
        Mockito.when(annotationTypeOperations.getLatestType(Mockito.any())).thenReturn(dbAnnotationTypeDefinition);
        annotationBusinessLogic.validateAndMergeAnnotationsAndAssignToInput(inputs);
        List<Annotation> result = inputDefinition.getAnnotations();
        assertEquals(result.size(),1);
    }
}