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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.user.IUserBusinessLogic;

public class DataTypeBusinessLogicTest {

    private static final String COMPONENT_INSTANCE_ID = "instanceId";
    private static final String COMPONENT_ID = "componentId";
    private static final String USER_ID = "userId";
    private static final String INSTANCE_INPUT_ID = "inputId";
    private static final String DATATYPE_NAME = "org.onap.datatypes.mytype";

    @Mock
    private ComponentsUtils componentsUtilsMock;

    @Mock
    private IUserBusinessLogic userAdminMock;

    @Mock
    private ToscaOperationFacade toscaOperationFacadeMock;

    @Mock
    private UserValidations userValidations;

    @Mock
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;

    @InjectMocks
    private DataTypeBusinessLogic testInstance;

    private Service service;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        service = new Service();
        service.setUniqueId(COMPONENT_INSTANCE_ID);
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(COMPONENT_INSTANCE_ID);
        service.setComponentInstances(Collections.singletonList(componentInstance));
        DataTypeDefinition dataType = new DataTypeDefinition();
        dataType.setName(DATATYPE_NAME);
        dataType.setDerivedFromName(ToscaPropertyType.Root.getType());
        List<DataTypeDefinition> dataTypes = Arrays.asList(dataType);
        service.setDataTypes(dataTypes);

        Map<String, List<ComponentInstanceInput>> instanceInputMap = new HashMap<>();
        ComponentInstanceInput componentInstanceInput = new ComponentInstanceInput();
        componentInstanceInput.setInputId(INSTANCE_INPUT_ID);
        instanceInputMap.put(COMPONENT_INSTANCE_ID, Collections.singletonList(componentInstanceInput));
        instanceInputMap.put("someInputId", Collections.singletonList(new ComponentInstanceInput()));
        service.setComponentInstancesInputs(instanceInputMap);
        when(userValidations.validateUserExists(eq(USER_ID), anyString(), eq(false))).thenReturn(new User());
        when(userAdminMock.getUser(USER_ID, false)).thenReturn(Either.left(new User()));
    }

    @Test
    public void test_getPrivateDataTypes() throws Exception {
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), Mockito.any(ComponentParametersView.class)))
            .thenReturn(Either.left(service));

        Either<List<DataTypeDefinition>, StorageOperationStatus> result = testInstance.getPrivateDataTypes(COMPONENT_ID);
        assertTrue(result.isLeft());
        List<DataTypeDefinition> dataTypes = result.left().value();
        assertEquals(service.getDataTypes(), dataTypes);
    }

    @Test
    public void test_getPrivateDataType() throws Exception {
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), Mockito.any(ComponentParametersView.class)))
            .thenReturn(Either.left(service));

        Either<DataTypeDefinition, StorageOperationStatus> result =
            testInstance.getPrivateDataType(COMPONENT_ID, DATATYPE_NAME);
        assertTrue(result.isLeft());
        DataTypeDefinition dataType = result.left().value();
        assertEquals(service.getDataTypes().get(0), dataType);
    }

    @Test
    public void test_deletePrivateDataType1() throws Exception {
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), Mockito.any(ComponentParametersView.class)))
            .thenReturn(Either.left(service));
        when(toscaOperationFacadeMock.deleteDataTypeOfComponent(service, DATATYPE_NAME))
            .thenReturn(StorageOperationStatus.OK);

        Either<DataTypeDefinition, StorageOperationStatus> result =
            testInstance.deletePrivateDataType(COMPONENT_ID, DATATYPE_NAME);
        assertTrue(result.isLeft());
        DataTypeDefinition dataType = result.left().value();
        assertEquals(service.getDataTypes().get(0), dataType);
    }

    @Test
    public void test_deletePrivateDataType2() throws Exception {
        when(toscaOperationFacadeMock.deleteDataTypeOfComponent(service, DATATYPE_NAME))
            .thenReturn(StorageOperationStatus.OK);

        Either<DataTypeDefinition, StorageOperationStatus> result =
            testInstance.deletePrivateDataType(service, DATATYPE_NAME);
        assertTrue(result.isLeft());
        DataTypeDefinition dataType = result.left().value();
        assertEquals(service.getDataTypes().get(0), dataType);
    }
}
