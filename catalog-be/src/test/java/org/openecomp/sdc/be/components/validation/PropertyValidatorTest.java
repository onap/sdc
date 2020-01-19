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
package org.openecomp.sdc.be.components.validation;

import fj.data.Either;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.utils.ExceptionUtils;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class PropertyValidatorTest {

    private static final String TEST = "test";
    private static final String VALUE = "VALUE";
    private static final String TYPE = "TYPE";

    @Mock
    private PropertyOperation propertyOperation;
    @Mock
    private ExceptionUtils exceptionUtils;
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private ApplicationDataTypeCache applicationDataTypeCache;

    @Test
    public void shouldValidateThinProperties() {
        Mockito.when(propertyOperation.validateAndUpdatePropertyValue(
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Either.left(""));

        PropertyValidator propertyValidator = new PropertyValidator(propertyOperation, componentsUtils,
            applicationDataTypeCache, exceptionUtils);
        List<PropertyDefinition> props = new ArrayList<>();
        List<PropertyDefinition> dbProps = new ArrayList<>();
        PropertyDefinition prop = new PropertyDefinition();
        prop.setName(TEST);
        prop.setValue(VALUE);
        prop.setType(TYPE);
        props.add(prop);
        dbProps.add(prop);
        propertyValidator.thinPropertiesValidator(props, dbProps, Collections.emptyMap());

        Mockito.verify(propertyOperation).validateAndUpdatePropertyValue(TYPE, VALUE, null, Collections.emptyMap());
    }

    @Test
    public void shouldIterateOverPropertiesOnInvalidType() {
        PropertyValidator propertyValidator = new PropertyValidator(propertyOperation, componentsUtils,
            applicationDataTypeCache, exceptionUtils);
        List<PropertyDefinition> props = new ArrayList<>();
        PropertyDefinition prop = new PropertyDefinition();
        prop.setName(TEST);
        prop.setValue(VALUE);
        prop.setType(TYPE);
        props.add(prop);
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = propertyValidator.iterateOverProperties(props);

        assertTrue(booleanResponseFormatEither.isRight());
    }
}