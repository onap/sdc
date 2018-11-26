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
 */

package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.user.IUserBusinessLogic;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class InputsBusinessLogicTest {

    private static final String COMPONENT_INSTANCE_ID = "instanceId";
    private static final String COMPONENT_ID = "componentId";
    private static final String USER_ID = "userId";
    public static final String INSTANCE_INPUT_ID = "inputId";
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
    private InputsBusinessLogic testInstance;

    private Service service;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        service = new Service();
        service.setUniqueId(COMPONENT_INSTANCE_ID);
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(COMPONENT_INSTANCE_ID);
        service.setComponentInstances(Collections.singletonList(componentInstance));

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
    public void getComponentInstanceInputs_ComponentInstanceNotExist() throws Exception {
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), Mockito.any(ComponentParametersView.class))).thenReturn(Either.left(service));
        Either<List<ComponentInstanceInput>, ResponseFormat> componentInstanceInputs = testInstance.getComponentInstanceInputs(USER_ID, COMPONENT_ID, "nonExisting");
        assertTrue(componentInstanceInputs.isRight());
        Mockito.verify(componentsUtilsMock).getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND);
    }

    @Test
    public void getComponentInstanceInputs_emptyInputsMap() throws Exception {
        service.setComponentInstancesInputs(Collections.emptyMap());
        getComponents_emptyInputs(service);
    }

    @Test
    public void getComponentInstanceInputs_nullInputsMap() throws Exception {
        service.setComponentInstancesInputs(null);
        getComponents_emptyInputs(service);
    }

    @Test
    public void getComponentInstanceInputs_instanceHasNoInputs() throws Exception {
        service.setComponentInstancesInputs(Collections.singletonMap("someInputId", new ArrayList<>()));
        getComponents_emptyInputs(service);
    }

    @Test
    public void getComponentInstanceInputs() throws Exception {
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), Mockito.any(ComponentParametersView.class))).thenReturn(Either.left(service));
        Either<List<ComponentInstanceInput>, ResponseFormat> componentInstanceInputs = testInstance.getComponentInstanceInputs(USER_ID, COMPONENT_ID, COMPONENT_INSTANCE_ID);
        assertEquals("inputId", componentInstanceInputs.left().value().get(0).getInputId());
    }

    @Test
    public void testGetInputs() {
        String userId = "userId";
        String componentId = "compId";
        Component component = new Resource();
        when(toscaOperationFacadeMock.getToscaElement(Mockito.any(String.class), Mockito.any(ComponentParametersView.class))).thenReturn(Either.left(component));
        testInstance.getInputs(userId, componentId);
        assertEquals(null, component.getInputs());
    }

    @Test
    public void testGetCIPropertiesByInputId() {
        Either<List<ComponentInstanceProperty>, ResponseFormat> result;
        String userId = "userId";
        String componentId = "compId";
        Component component = new Resource();
        List<InputDefinition> listDef = new ArrayList<>();
        InputDefinition inputDef = new InputDefinition();
        inputDef.setUniqueId(componentId);
        listDef.add(inputDef);
        component.setInputs(listDef);
        when(toscaOperationFacadeMock.getToscaElement(Mockito.any(String.class), Mockito.any(ComponentParametersView.class))).thenReturn(Either.left(component));
        result = testInstance.getComponentInstancePropertiesByInputId(userId, componentId, componentId, componentId);
        assertTrue(result.isLeft());
    }

    private void getComponents_emptyInputs(Service service) {
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), Mockito.any(ComponentParametersView.class))).thenReturn(Either.left(service));
        Either<List<ComponentInstanceInput>, ResponseFormat> componentInstanceInputs = testInstance.getComponentInstanceInputs(USER_ID, COMPONENT_ID, COMPONENT_INSTANCE_ID);
        assertEquals(Collections.emptyList(), componentInstanceInputs.left().value());
    }

}
