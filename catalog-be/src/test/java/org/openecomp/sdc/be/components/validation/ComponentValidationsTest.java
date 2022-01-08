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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

class ComponentValidationsTest {

    @InjectMocks
    private ComponentValidations testSubject;

    @Mock
    private ToscaOperationFacade toscaOperationFacadeMock;

    @Mock
    private GraphLockOperation graphLockOperationMock;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
    }

    @Test
    void testValidateComponentInstanceExist() throws Exception {
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
    void testValidateNameIsUniqueInComponent() throws Exception {
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

        final GroupDefinition groupDefinition = new GroupDefinition();
        groupDefinition.setName(newName2);
        component.setGroups(Arrays.asList(groupDefinition));
        result = ComponentValidations.validateNameIsUniqueInComponent(currentName, newName2, component);
        assertFalse(result);
    }

    @Test
    void testValidateComponentIsCheckedOutByUserAndLockIt() throws Exception {
        String componentId = "";
        String userId = "";
        Resource resource = new Resource();
        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);

        Mockito.when(toscaOperationFacadeMock.getToscaElement(Mockito.anyString(), Mockito.any(ComponentParametersView.class)))
            .thenReturn(Either.left(resource));

        Assertions.assertThrows(ComponentException.class, () -> {
            // default test
            testSubject.validateComponentIsCheckedOutByUser("", ComponentTypeEnum.RESOURCE, userId);
        });
    }

    @Test
    void testGetComponentInstance() {
        String instanceId = "test";

        ComponentInstance instance = new ComponentInstance();
        instance.setUniqueId(instanceId);
        List<ComponentInstance> instances = new ArrayList<>();
        instances.add(instance);

        Component component = new Resource();
        component.setComponentInstances(instances);

        final Optional<ComponentInstance> result = testSubject.getComponentInstance(component, instanceId);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertNotNull(result.get());
    }

}
