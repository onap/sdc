/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.components.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.utils.ExceptionUtils;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.AnnotationTypeDefinition;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;

@RunWith(MockitoJUnitRunner.class)
public class AnnotationValidatorTest {

    private static final String TYPE = "type";
    private static final String UNIQUE_ID = "1";
    private static final String OWNER = "owner";
    private static final String DESC = "desc";
    private static final String TEST = "test";
    private static final String PROP_NAME = "propName";
    private Map<String, DataTypeDefinition> allData;
    private List<PropertyDefinition> annotationTypeProperties;
    private AnnotationValidator annotationValidator;
    private List<PropertyDataDefinition> propertyDataDefinitions = new ArrayList<>();
    private PropertyDataDefinition propertyDataDefinition = new PropertyDataDefinition();;

    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private ApplicationDataTypeCache dataTypeCache;
    @Mock
    private PropertyValidator propertyValidator;
    @Mock
    private ExceptionUtils exceptionUtils;

    @Before
    public void setUp() throws Exception {
        annotationValidator = new AnnotationValidator(propertyValidator, exceptionUtils, dataTypeCache, componentsUtils);
        allData = Collections.emptyMap();
        Either<Map<String, DataTypeDefinition>, TitanOperationStatus> cacheResponse = Either.left(allData);
        Mockito.when(dataTypeCache.getAll()).thenReturn(cacheResponse);
        annotationTypeProperties = Collections.emptyList();
        propertyDataDefinitions = new ArrayList<>();
    }

    @Test
    public void testValidateAnnotationsProperties() {
        Annotation annotation = prepareAnnotation();
        AnnotationTypeDefinition annotationTypeDefinition = prepareAnnotationTypeDefinition();
        List<PropertyDefinition> properties = new ArrayList<>();
        properties.add(new PropertyDefinition(propertyDataDefinition));

        List<Annotation> annotations = annotationValidator
            .validateAnnotationsProperties(annotation, annotationTypeDefinition);

        Mockito.verify(propertyValidator).thinPropertiesValidator(properties, annotationTypeProperties, allData);
        assertThat(annotations.get(0), is(annotation));
    }

    private AnnotationTypeDefinition prepareAnnotationTypeDefinition() {
        AnnotationTypeDefinition annotationTypeDefinition = new AnnotationTypeDefinition();
        annotationTypeDefinition.setProperties(annotationTypeProperties);
        annotationTypeDefinition.setCreationTime(System.currentTimeMillis());
        annotationTypeDefinition.setType(TYPE);
        annotationTypeDefinition.setHighestVersion(true);
        annotationTypeDefinition.setUniqueId(UNIQUE_ID);
        annotationTypeDefinition.setOwnerId(OWNER);
        annotationTypeDefinition.setDescription(DESC);
        return annotationTypeDefinition;
    }

    private Annotation prepareAnnotation(){
        Annotation annotation = new Annotation();
        annotation.setName(TEST);
        annotation.setDescription(DESC);
        propertyDataDefinition.setName(PROP_NAME);
        propertyDataDefinitions.add(propertyDataDefinition);
        annotation.setProperties(propertyDataDefinitions);
        annotation.setType(TYPE);
        return annotation;
    }

}