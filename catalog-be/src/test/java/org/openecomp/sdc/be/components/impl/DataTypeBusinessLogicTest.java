/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Fujitsu Limited. All rights reserved.
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import fj.data.Either;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.user.UserBusinessLogic;

public class DataTypeBusinessLogicTest {

    private static final String COMPONENT_INSTANCE_ID = "instanceId";
    private static final String COMPONENT_ID = "componentId";
    private static final String USER_ID = "userId";
    private static final String INSTANCE_INPUT_ID = "inputId";
    private static final String DATATYPE_NAME = "org.onap.datatypes.mytype";

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Mock
    private UserBusinessLogic userAdminMock;

    @Mock
    private ToscaOperationFacade toscaOperationFacadeMock;

    @Mock
    private UserValidations userValidationsMock;

    @Mock
    private PropertyOperation propertyOperationMock;

    @Mock
    private ApplicationDataTypeCache applicationDataTypeCacheMock;

    @InjectMocks
    private DataTypeBusinessLogic dataTypeBusinessLogic;

    private Service service;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        service = new Service();
        service.setUniqueId(COMPONENT_INSTANCE_ID);
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(COMPONENT_INSTANCE_ID);
        service.setComponentInstances(Collections.singletonList(componentInstance));
        final DataTypeDefinition dataType = createDataTypeDefinition(DATATYPE_NAME, ToscaPropertyType.ROOT.getType());
        List<DataTypeDefinition> dataTypes = Arrays.asList(dataType);
        service.setDataTypes(dataTypes);

        Map<String, List<ComponentInstanceInput>> instanceInputMap = new HashMap<>();
        ComponentInstanceInput componentInstanceInput = new ComponentInstanceInput();
        componentInstanceInput.setInputId(INSTANCE_INPUT_ID);
        instanceInputMap.put(COMPONENT_INSTANCE_ID, Collections.singletonList(componentInstanceInput));
        instanceInputMap.put("someInputId", Collections.singletonList(new ComponentInstanceInput()));
        service.setComponentInstancesInputs(instanceInputMap);
        when(userValidationsMock.validateUserExists(eq(USER_ID))).thenReturn(new User());
        when(userAdminMock.getUser(USER_ID, false)).thenReturn(new User());
    }

    private DataTypeDefinition createDataTypeDefinition(final String name, final String derivedFrom) {
        DataTypeDefinition dataType = new DataTypeDefinition();
        dataType.setName(name);
        dataType.setDerivedFromName(derivedFrom);
        return dataType;
    }

    @Test
    public void test_getPrivateDataTypes() {
        setMockitoWhenGetToscaElementCalled();

        Either<List<DataTypeDefinition>, StorageOperationStatus> result = dataTypeBusinessLogic.getPrivateDataTypes(COMPONENT_ID);
        assertTrue(result.isLeft());
        List<DataTypeDefinition> dataTypes = result.left().value();
        assertEquals(service.getDataTypes(), dataTypes);
    }

    @Test
    public void test_getPrivateDataType() {
        setMockitoWhenGetToscaElementCalled();

        Either<DataTypeDefinition, StorageOperationStatus> result =
            dataTypeBusinessLogic.getPrivateDataType(COMPONENT_ID, DATATYPE_NAME);
        assertTrue(result.isLeft());
        DataTypeDefinition dataType = result.left().value();
        assertEquals(service.getDataTypes().get(0), dataType);
    }

    @Test
    public void test_deletePrivateDataType1() {
        setMockitoWhenGetToscaElementCalled();
        when(toscaOperationFacadeMock.deleteDataTypeOfComponent(service, DATATYPE_NAME))
            .thenReturn(StorageOperationStatus.OK);

        Either<DataTypeDefinition, StorageOperationStatus> result =
            dataTypeBusinessLogic.deletePrivateDataType(COMPONENT_ID, DATATYPE_NAME);
        assertTrue(result.isLeft());
        DataTypeDefinition dataType = result.left().value();
        assertEquals(service.getDataTypes().get(0), dataType);
    }

    @Test
    public void test_deletePrivateDataType2() {
        when(toscaOperationFacadeMock.deleteDataTypeOfComponent(service, DATATYPE_NAME))
            .thenReturn(StorageOperationStatus.OK);

        Either<DataTypeDefinition, StorageOperationStatus> result =
            dataTypeBusinessLogic.deletePrivateDataType(service, DATATYPE_NAME);
        assertTrue(result.isLeft());
        DataTypeDefinition dataType = result.left().value();
        assertEquals(service.getDataTypes().get(0), dataType);
    }

    @Test
    public void testCreateDataType() {
        final DataTypeDefinition dataTypeDefinition = new DataTypeDefinition();
        when(propertyOperationMock.addDataType(dataTypeDefinition)).thenReturn(Either.left(dataTypeDefinition));
        final DataTypeDefinition dataType = dataTypeBusinessLogic.create(dataTypeDefinition);
        assertThat("The creation time should not be null", dataType.getCreationTime(), is(notNullValue()));

        exceptionRule.expect(StorageException.class);
        when(propertyOperationMock.addDataType(dataTypeDefinition)).thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        dataTypeBusinessLogic.create(dataTypeDefinition);
    }

    @Test
    public void testAddToComponent() {
        //given
        final DataTypeDefinition dataTypeDefinition1 =
            createDataTypeDefinition("data.type.1", ToscaPropertyType.ROOT.getType());
        final DataTypeDefinition dataTypeDefinition2 =
            createDataTypeDefinition("data.type.2", ToscaPropertyType.ROOT.getType());

        final List<DataTypeDefinition> expectedDataTypeDefinitionList
            = Arrays.asList(dataTypeDefinition1, dataTypeDefinition2);

        final ImmutableMap<String, DataTypeDefinition> dataTypeDefinitionMap =
            ImmutableMap.of(dataTypeDefinition1.getName(), dataTypeDefinition1,
                dataTypeDefinition2.getName(), dataTypeDefinition2);

        //when
        when(propertyOperationMock.isDefinedInDataTypes(dataTypeDefinition1.getName())).thenReturn(Either.left(true));
        when(propertyOperationMock.isDefinedInDataTypes(dataTypeDefinition2.getName()))
            .thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        when(propertyOperationMock.addDataType(dataTypeDefinition2)).thenReturn(Either.left(dataTypeDefinition2));
        when(propertyOperationMock.findDataTypeByName(dataTypeDefinition1.getName())).thenReturn(dataTypeDefinition1);
        when(propertyOperationMock.findDataTypeByName(dataTypeDefinition2.getName())).thenReturn(dataTypeDefinition2);
        final BiFunction<Map<String, DataTypeDefinition>, DataTypeDefinition, Boolean> keyMatcher =
            (argument, dataTypeDefinition) -> argument != null && argument.containsKey(dataTypeDefinition.getName());
        when(toscaOperationFacadeMock
            .addDataTypesToComponent(argThat(map -> keyMatcher.apply(map, dataTypeDefinition1)),
                eq(service.getUniqueId())))
            .thenReturn(Either.left(Collections.singletonList(dataTypeDefinition1)));
        when(toscaOperationFacadeMock
            .addDataTypesToComponent(argThat(map -> keyMatcher.apply(map, dataTypeDefinition2)),
                eq(service.getUniqueId())))
            .thenReturn(Either.left(Collections.singletonList(dataTypeDefinition2)));

        final List<DataTypeDefinition> actualDataTypeDefinitionList =
            dataTypeBusinessLogic.addToComponent(service.getUniqueId(), dataTypeDefinitionMap);

        //then
        assertThat("Result should not be empty", actualDataTypeDefinitionList, is(not(empty())));
        assertThat("Result should contains expected data type definitions",
            actualDataTypeDefinitionList, containsInAnyOrder(expectedDataTypeDefinitionList.toArray()));

        //when
        when(toscaOperationFacadeMock
            .addDataTypesToComponent(argThat(map -> keyMatcher.apply(map, dataTypeDefinition2)),
                eq(service.getUniqueId())))
            .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        //then
        exceptionRule.expect(StorageException.class);
        dataTypeBusinessLogic.addToComponent(service.getUniqueId(), dataTypeDefinitionMap);
    }

    private void setMockitoWhenGetToscaElementCalled() {
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), Mockito.any(ComponentParametersView.class)))
                .thenReturn(Either.left(service));
    }
}
