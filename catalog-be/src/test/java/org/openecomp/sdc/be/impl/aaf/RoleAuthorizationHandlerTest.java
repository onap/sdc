/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.impl.aaf;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.components.impl.aaf.RoleAuthorizationHandler;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.servlets.BeGenericServlet;
import org.openecomp.sdc.common.api.FilterDecisionEnum;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RoleAuthorizationHandlerTest {

    private RoleAuthorizationHandler roleAuthorizationHandler;
    @Mock
    JoinPoint joinPoint;
    @Mock
    Signature signature;
    @Mock
    BeGenericServlet beGenericServlet;
    @Mock
    HttpServletRequest httpServletRequest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("methodName");
        when(joinPoint.getThis()).thenReturn(beGenericServlet);
        when(beGenericServlet.getServletRequest()).thenReturn(httpServletRequest);
        ThreadLocalsHolder.setApiType(FilterDecisionEnum.EXTERNAL);
        new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be/auth"));
        roleAuthorizationHandler = new RoleAuthorizationHandler();
    }

    @Test
    public void testAuthorizeRoleOnePermittedRole() {
        String[] permsAllowed = {AafPermission.PermNames.WRITE_VALUE};
        AnnotationDescriptor<PermissionAllowed> permissionDescriptor = new AnnotationDescriptor<PermissionAllowed>(PermissionAllowed.class);
        permissionDescriptor.setValue("value", permsAllowed);
        PermissionAllowed rolesAllowed = (PermissionAllowed) AnnotationFactory.create(permissionDescriptor);
        when(httpServletRequest.isUserInRole(AafPermission.getEnumByString(permsAllowed[0]).getFullPermission()))
                .thenReturn(true);
        roleAuthorizationHandler.authorizeRole(joinPoint, rolesAllowed);
    }

    @Test
    public void testAuthorizeRoleTwoPermittedRole() {
        String[] permsAllowed = {AafPermission.PermNames.WRITE_VALUE, AafPermission.PermNames.READ_VALUE};
        AnnotationDescriptor<PermissionAllowed> permissionDescriptor = new AnnotationDescriptor<PermissionAllowed>(PermissionAllowed.class);
        permissionDescriptor.setValue("value", permsAllowed);
        PermissionAllowed rolesAllowed = (PermissionAllowed)AnnotationFactory.create(permissionDescriptor);
        when(httpServletRequest.isUserInRole(AafPermission.getEnumByString(permsAllowed[0]).getFullPermission()))
                .thenReturn(true);
        roleAuthorizationHandler.authorizeRole(joinPoint, rolesAllowed);
    }

    @Test
    public void testAuthorizeRoleNonPermittedRole() {
        String[] permsAllowed = {AafPermission.PermNames.WRITE_VALUE, AafPermission.PermNames.READ_VALUE};
        AnnotationDescriptor<PermissionAllowed> permissionDescriptor = new AnnotationDescriptor<PermissionAllowed>(PermissionAllowed.class);
        permissionDescriptor.setValue("value", permsAllowed);
        PermissionAllowed rolesAllowed = (PermissionAllowed)AnnotationFactory.create(permissionDescriptor);
        when(httpServletRequest.isUserInRole(AafPermission.getEnumByString(permsAllowed[0]).getFullPermission()))
                .thenReturn(false);

        ComponentException thrown = (ComponentException) catchThrowable(()->roleAuthorizationHandler.authorizeRole(joinPoint, rolesAllowed));
        assertThat(thrown.getActionStatus()).isEqualTo(ActionStatus.AUTH_FAILED);
    }

    @Test
    public void testAuthorizeRoleEmptyRole() {
        String[] permsAllowed = {};
        AnnotationDescriptor<PermissionAllowed> permissionDescriptor = new AnnotationDescriptor<PermissionAllowed>(PermissionAllowed.class);
        permissionDescriptor.setValue("value", permsAllowed);
        PermissionAllowed rolesAllowed = (PermissionAllowed)AnnotationFactory.create(permissionDescriptor);

        ComponentException thrown = (ComponentException) catchThrowable(()->roleAuthorizationHandler.authorizeRole(joinPoint, rolesAllowed));
        assertThat(thrown.getActionStatus()).isEqualTo(ActionStatus.AUTH_FAILED);
    }
}
