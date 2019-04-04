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

import static org.mockito.Mockito.atLeast;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.Role;

@RunWith(MockitoJUnitRunner.class)
public class AccessValidationsTest {

    private static final String ANY_CONTEXT = "anyContext";
    private static final String RESOURCES = "resources";
    private static final String COMPONENT_ID = "1";
    private static final String USER_ID = "2";
    private AccessValidations accessValidations;

    @Mock
    private UserValidations userValidations;
    @Mock
    private ComponentValidations componentValidations;

    @Before
    public void setUp() throws Exception {
        accessValidations = new AccessValidations(userValidations, componentValidations);
    }

    @Test
    public void testValidateUserCanRetrieveComponentData() {
        accessValidations.validateUserCanRetrieveComponentData(COMPONENT_ID, RESOURCES, USER_ID, ANY_CONTEXT);

        Mockito.verify(userValidations).validateUserExists(USER_ID, ANY_CONTEXT, true);
        Mockito.verify(componentValidations).getComponent(COMPONENT_ID, ComponentTypeEnum.RESOURCE);
    }

    @Test
    public void testValidateUserCanWorkOnComponent() {
        User user = new User();
        List<Role> adminRoles = new ArrayList<>();
        adminRoles.add(Role.ADMIN);
        adminRoles.add(Role.DESIGNER);
        Mockito.when(userValidations.validateUserExists(USER_ID, ANY_CONTEXT, true)).thenReturn(user);

        accessValidations.validateUserCanWorkOnComponent(COMPONENT_ID, ComponentTypeEnum.RESOURCE, USER_ID, ANY_CONTEXT);

        Mockito.verify(userValidations).validateUserExists(USER_ID, ANY_CONTEXT, true);
        Mockito.verify(userValidations).validateUserRole(user, adminRoles);
        Mockito.verify(componentValidations).validateComponentIsCheckedOutByUser(COMPONENT_ID, ComponentTypeEnum.RESOURCE,
            USER_ID);
    }

    @Test
    public void testValidateUserCanWorkOnComponentGivingComponent() {
        User user = new User();
        Component component = Mockito.mock(Component.class);
        List<Role> adminRoles = new ArrayList<>();
        adminRoles.add(Role.ADMIN);
        adminRoles.add(Role.DESIGNER);
        Mockito.when(userValidations.validateUserExists(USER_ID, ANY_CONTEXT, true)).thenReturn(user);

        accessValidations.validateUserCanWorkOnComponent(component, USER_ID, ANY_CONTEXT);

        Mockito.verify(userValidations, atLeast(1)).validateUserExists(USER_ID, ANY_CONTEXT, true);
        Mockito.verify(userValidations).validateUserRole(user, adminRoles);
        Mockito.verify(componentValidations).validateComponentIsCheckedOutByUser(component, USER_ID);
    }

    @Test
    public void testValidateUserExists() {
        accessValidations.validateUserExists(COMPONENT_ID, ANY_CONTEXT);
        Mockito.verify(userValidations).validateUserExists(COMPONENT_ID, ANY_CONTEXT, true);
    }

    @Test
    public void validateUserExist() {
        accessValidations.validateUserExist(COMPONENT_ID, ANY_CONTEXT);
        Mockito.verify(userValidations).validateUserExists(COMPONENT_ID, ANY_CONTEXT, false);
    }

    @Test
    public void userIsAdminOrDesigner() {
        User user = new User();
        List<Role> adminRoles = new ArrayList<>();
        adminRoles.add(Role.ADMIN);
        adminRoles.add(Role.DESIGNER);
        Mockito.when(userValidations.validateUserExists(COMPONENT_ID, ANY_CONTEXT, true)).thenReturn(user);

        accessValidations.userIsAdminOrDesigner(COMPONENT_ID, ANY_CONTEXT);

        Mockito.verify(userValidations).validateUserRole(user, adminRoles);
    }
}