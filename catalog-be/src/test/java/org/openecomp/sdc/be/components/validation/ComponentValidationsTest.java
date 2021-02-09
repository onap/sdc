/*
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 *
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
 */

package org.openecomp.sdc.be.components.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fj.data.Either;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

public class ComponentValidationsTest {

    @InjectMocks
    ComponentValidations testSubject;

    @Mock
    ToscaOperationFacade toscaOperationFacadeMock;

    @Mock
    GraphLockOperation graphLockOperationMock;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
            "src/test/resources/config/catalog-be"));
    }

    @Test
    public void testValidateComponentInstanceExist() throws Exception {
        String instanceId = "test";

        ComponentInstance instance = new ComponentInstance();
        instance.setUniqueId(instanceId);
        List<ComponentInstance> instances = new ArrayList<>();
        instances.add(instance);

        Component component = new Resource();
        component.setComponentInstances(instances);

        // default test
        boolean result = ComponentValidations.validateComponentInstanceExist(component, instanceId);
        assertTrue(result);
    }

    @Test
    public void testGetNormalizedName() throws Exception {
        String name = "mock";
        ToscaDataDefinition toscaDataDefinition = new AdditionalInfoParameterDataDefinition();
        toscaDataDefinition.setToscaPresentationValue(JsonPresentationFields.NAME, name);

        // default test
        String result = ComponentValidations.getNormalizedName(toscaDataDefinition);
        assertEquals(name, result);
    }

    @Test
    public void testValidateNameIsUniqueInComponent() throws Exception {
        String currentName = "curr_name";
        String newName = "curr_name";
        String newName2 = "mock";

        ComponentInstance instance = new ComponentInstance();
        instance.setName(currentName);
        instance.setNormalizedName(currentName);
        List<ComponentInstance> instances = new ArrayList<>();
        instances.add(instance);

        Component component = new Resource();
        component.setComponentInstances(instances);

        // default test
        boolean result = ComponentValidations.validateNameIsUniqueInComponent(currentName, newName, component);
        assertTrue(result);
        result = ComponentValidations.validateNameIsUniqueInComponent(currentName, newName2, component);
        assertTrue(result);
    }

    @Test(expected = ComponentException.class)
    public void testValidateComponentIsCheckedOutByUserAndLockIt() throws Exception {
        String componentId = "";
        String userId = "";
        Component result;
        Resource resource = new Resource();
        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);

        Mockito.when(
            toscaOperationFacadeMock.getToscaElement(Mockito.anyString(), Mockito.any(ComponentParametersView.class)))
            .thenReturn(Either.left(resource));

        // default test
        result = testSubject.validateComponentIsCheckedOutByUser("", ComponentTypeEnum.RESOURCE,
            userId);
    }

    @Test
    public void testGetComponent() throws Exception {
        String componentId = "mock";
        ComponentTypeEnum componentType = null;
        Component result;
        Component resource = new Resource();
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        Mockito.when(
            toscaOperationFacadeMock.getToscaElement(Mockito.anyString(), Mockito.any(ComponentParametersView.class)))
            .thenReturn(Either.left(resource));

        // default test
        result = testSubject.getComponent(componentId, ComponentTypeEnum.RESOURCE);
        assertThat(result).isInstanceOf(Component.class);
    }

    @Test(expected = StorageException.class)
    public void testOnToscaOperationError() throws Exception {
        Component result;

        // default test
        result = testSubject.onToscaOperationError(
            StorageOperationStatus.ARTIFACT_NOT_FOUND, "");
    }
}
