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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.user.IUserBusinessLogic;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;

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

        Mockito.when(userAdminMock.getUser(USER_ID, false)).thenReturn(Either.left(new User()));
    }

    @Test
    public void getComponentInstanceInputs_ComponentInstanceNotExist() throws Exception {
        Mockito.when(toscaOperationFacadeMock.getToscaElement(Mockito.eq(COMPONENT_ID), Mockito.any(ComponentParametersView.class))).thenReturn(Either.left(service));
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
        Mockito.when(toscaOperationFacadeMock.getToscaElement(Mockito.eq(COMPONENT_ID), Mockito.any(ComponentParametersView.class))).thenReturn(Either.left(service));
        Either<List<ComponentInstanceInput>, ResponseFormat> componentInstanceInputs = testInstance.getComponentInstanceInputs(USER_ID, COMPONENT_ID, COMPONENT_INSTANCE_ID);
        assertEquals("inputId", componentInstanceInputs.left().value().get(0).getInputId());
    }

    private void getComponents_emptyInputs(Service service) {
        Mockito.when(toscaOperationFacadeMock.getToscaElement(Mockito.eq(COMPONENT_ID), Mockito.any(ComponentParametersView.class))).thenReturn(Either.left(service));
        Either<List<ComponentInstanceInput>, ResponseFormat> componentInstanceInputs = testInstance.getComponentInstanceInputs(USER_ID, COMPONENT_ID, COMPONENT_INSTANCE_ID);
        assertEquals(Collections.emptyList(), componentInstanceInputs.left().value());
    }

}
