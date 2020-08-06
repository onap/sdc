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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.exceptions.DataTypeNotProvidedException;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
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
        final List<DataTypeDefinition> dataTypes = new ArrayList<>();
        dataTypes.add(dataType);
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
    public void testAddToComponentSuccess() throws DataTypeNotProvidedException {
        //given
        final DataTypeDefinition dataTypeDefinition1 =
            createDataTypeDefinition("data.type.1", ToscaPropertyType.ROOT.getType());
        final DataTypeDefinition dataTypeDefinition2 =
            createDataTypeDefinition("data.type.2", ToscaPropertyType.ROOT.getType());
        final DataTypeDefinition dataTypeDefinition3 =
            createDataTypeDefinition("data.type.child", dataTypeDefinition2.getName());

        final DataTypeDefinition dataTypeDefinitionRoot =
            createDataTypeDefinition(ToscaPropertyType.ROOT.getType(), null);


        final List<DataTypeDefinition> expectedDataTypeDefinitionList
            = Arrays.asList(dataTypeDefinition1, dataTypeDefinition2, dataTypeDefinition3);

        final ImmutableMap<String, DataTypeDefinition> dataTypeDefinitionMap =
            ImmutableMap.of(dataTypeDefinition1.getName(), dataTypeDefinition1,
                dataTypeDefinition2.getName(), dataTypeDefinition2,
                dataTypeDefinition3.getName(), dataTypeDefinition3);

        when(propertyOperationMock.findDataTypeByName(ToscaPropertyType.ROOT.getType()))
            .thenReturn(Optional.of(dataTypeDefinitionRoot));
        when(propertyOperationMock.findDataTypeByName(dataTypeDefinition1.getName())).thenReturn(Optional.empty());
        when(propertyOperationMock.findDataTypeByName(dataTypeDefinition2.getName())).thenReturn(Optional.empty());
        when(propertyOperationMock.findDataTypeByName(dataTypeDefinition3.getName())).thenReturn(Optional.empty());
        //when
        dataTypeBusinessLogic.addToComponent(service, dataTypeDefinitionMap);
        final List<DataTypeDefinition> dataTypes = service.getDataTypes();

        //then
        assertThat("Result should not be empty", dataTypes, is(not(empty())));
        assertThat("Result should contains expected data type definitions",
            dataTypes, hasItems(expectedDataTypeDefinitionList.toArray(new DataTypeDefinition[]{})));
    }

    @Test
    public void testAddToComponentEmptyMapOrNull() throws DataTypeNotProvidedException {
        final int expectedSize = service.getDataTypes().size();
        dataTypeBusinessLogic.addToComponent(service, Collections.emptyMap());
        assertThat("DataTypes list should keep the same size", service.getDataTypes(), hasSize(expectedSize));
        dataTypeBusinessLogic.addToComponent(service, null);
        assertThat("DataTypes list should keep the same size", service.getDataTypes(), hasSize(expectedSize));
    }

    @Test
    public void testAddToComponentDataTypeNotProvidedException() throws DataTypeNotProvidedException {
        //given
        final DataTypeDefinition dataTypeDefinition =
            createDataTypeDefinition("data.type.child", "not.provided.data.type");
        final ImmutableMap<String, DataTypeDefinition> dataTypeDefinitionMap =
            ImmutableMap.of(dataTypeDefinition.getName(), dataTypeDefinition);
        //then
        exceptionRule.expect(DataTypeNotProvidedException.class);
        //when
        when(propertyOperationMock.findDataTypeByName("not.provided.data.type")).thenReturn(Optional.empty());
        dataTypeBusinessLogic.addToComponent(service, dataTypeDefinitionMap);
    }


    private void setMockitoWhenGetToscaElementCalled() {
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), Mockito.any(ComponentParametersView.class)))
                .thenReturn(Either.left(service));
    }
}
