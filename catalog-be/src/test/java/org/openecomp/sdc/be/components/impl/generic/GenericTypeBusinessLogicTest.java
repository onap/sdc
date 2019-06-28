/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.components.impl.generic;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class GenericTypeBusinessLogicTest {

    @InjectMocks
    private GenericTypeBusinessLogic testInstance;

    @Mock
    private ToscaOperationFacade toscaOperationFacadeMock;

    @Mock
    private ComponentsUtils componentsUtils;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testInstance = new GenericTypeBusinessLogic(componentsUtils, toscaOperationFacadeMock);
    }

    @Test
    public void fetchDerivedFromGenericType_cvfv_getGenericResourceTypeFromDerivedFrom() throws Exception {
        Resource cvfc = new Resource();
        cvfc.setResourceType(ResourceTypeEnum.CVFC);
        cvfc.setDerivedFrom(Arrays.asList("genericType", "someOtherType"));
        cvfc.setDerivedFromGenericType("genericType");
        Resource genericResource = new Resource();
        when(toscaOperationFacadeMock.getLatestCertifiedNodeTypeByToscaResourceName("genericType")).thenReturn(Either.left(genericResource));
        Either<Resource, ResponseFormat> fetchedGenericType = testInstance.fetchDerivedFromGenericType(cvfc);
        assertEquals(genericResource, fetchedGenericType.left().value());
    }

    @Test
    public void fetchDerivedFromGenericType_getGenericResourceTypeFromConfiguration() throws Exception {
        Resource resource = Mockito.mock(Resource.class);
        when(resource.getResourceType()).thenReturn(ResourceTypeEnum.VF);
        when(resource.fetchGenericTypeToscaNameFromConfig()).thenReturn("genericType");
        Resource genericResource = new Resource();
        when(toscaOperationFacadeMock.getLatestCertifiedNodeTypeByToscaResourceName("genericType")).thenReturn(Either.left(genericResource));
        Either<Resource, ResponseFormat> fetchedGenericType = testInstance.fetchDerivedFromGenericType(resource);
        assertEquals(genericResource, fetchedGenericType.left().value());
    }

    @Test
    public void generateInputsFromGenericTypeProperties() throws Exception {
        Resource genericNodeType = new Resource();
        genericNodeType.setUniqueId("genericUid");
        PropertyDefinition propertyDefinition = generatePropDefinition("prop1");
        PropertyDefinition propertyDefinition2 = generatePropDefinition("prop2");

        genericNodeType.setProperties(Arrays.asList(propertyDefinition, propertyDefinition2));

        List<InputDefinition> genericInputs = testInstance.generateInputsFromGenericTypeProperties(genericNodeType);
        assertEquals(2, genericInputs.size());
        assertInput(genericInputs.get(0), propertyDefinition);
        assertInput(genericInputs.get(1), propertyDefinition2);
    }

    @Test
    public void generateInputsFromGenericTypeProperties_genericHasNoProps() throws Exception {
        Resource genericNodeType = new Resource();
        assertTrue(testInstance.generateInputsFromGenericTypeProperties(genericNodeType).isEmpty());
    }

    private void assertInput(InputDefinition inputDefinition, PropertyDefinition propertyDefinition) {
        assertEquals(inputDefinition.getOwnerId(), "genericUid");
        assertEquals(inputDefinition.getValue(), propertyDefinition.getValue());
        assertEquals(inputDefinition.getName(), propertyDefinition.getName());
    }

    private PropertyDefinition generatePropDefinition(String name) {
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setName(name);
        propertyDefinition.setValue(name + "value");
        return propertyDefinition;
    }


}