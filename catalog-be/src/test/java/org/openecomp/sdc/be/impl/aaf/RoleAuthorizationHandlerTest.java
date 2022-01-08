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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.hibernate.validator.internal.util.annotation.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotation.AnnotationDescriptor.Builder;
import org.hibernate.validator.internal.util.annotation.AnnotationFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission.PermNames;
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

@ExtendWith(MockitoExtension.class)
class RoleAuthorizationHandlerTest {

    private RoleAuthorizationHandler roleAuthorizationHandler;
    @Mock
    private JoinPoint joinPoint;
    @Mock
    private Signature signature;
    @Mock
    private BeGenericServlet beGenericServlet;
    @Mock
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("methodName");
        when(joinPoint.getThis()).thenReturn(beGenericServlet);
        when(beGenericServlet.getServletRequest()).thenReturn(httpServletRequest);
        ThreadLocalsHolder.setApiType(FilterDecisionEnum.EXTERNAL);
        new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be/auth"));
        roleAuthorizationHandler = new RoleAuthorizationHandler();
    }

    @Test
    void testAuthorizeRoleOnePermittedRole() {
        final String[] permsAllowed = {PermNames.WRITE_VALUE};
        final AnnotationDescriptor<PermissionAllowed> permissionDescriptor = createTestSubject(permsAllowed);
        final PermissionAllowed rolesAllowed = AnnotationFactory.create(permissionDescriptor);
        when(httpServletRequest.isUserInRole(AafPermission.getEnumByString(permsAllowed[0]).getFullPermission())).thenReturn(true);
        roleAuthorizationHandler.authorizeRole(joinPoint, rolesAllowed);
    }

    @Test
    void testAuthorizeRoleTwoPermittedRole() {
        final String[] permsAllowed = {PermNames.WRITE_VALUE, PermNames.READ_VALUE};
        final AnnotationDescriptor<PermissionAllowed> permissionDescriptor = createTestSubject(permsAllowed);
        final PermissionAllowed rolesAllowed = AnnotationFactory.create(permissionDescriptor);
        when(httpServletRequest.isUserInRole(AafPermission.getEnumByString(permsAllowed[0]).getFullPermission())).thenReturn(true);
        roleAuthorizationHandler.authorizeRole(joinPoint, rolesAllowed);
    }

    @Test
    void testAuthorizeRoleNonPermittedRole() {
        final String[] permsAllowed = {PermNames.WRITE_VALUE, PermNames.READ_VALUE};
        final AnnotationDescriptor<PermissionAllowed> permissionDescriptor = createTestSubject(permsAllowed);
        final PermissionAllowed rolesAllowed = AnnotationFactory.create(permissionDescriptor);
        when(httpServletRequest.isUserInRole(AafPermission.getEnumByString(permsAllowed[0]).getFullPermission())).thenReturn(false);

        final ComponentException thrown = (ComponentException) catchThrowable(() -> roleAuthorizationHandler.authorizeRole(joinPoint, rolesAllowed));
        assertThat(thrown.getActionStatus()).isEqualTo(ActionStatus.AUTH_FAILED);
    }

    @Test
    void testAuthorizeRoleEmptyRole() {
        final String[] permsAllowed = {};
        final AnnotationDescriptor<PermissionAllowed> permissionDescriptor = createTestSubject(permsAllowed);
        final PermissionAllowed rolesAllowed = AnnotationFactory.create(permissionDescriptor);

        final ComponentException thrown = (ComponentException) catchThrowable(() -> roleAuthorizationHandler.authorizeRole(joinPoint, rolesAllowed));
        assertThat(thrown.getActionStatus()).isEqualTo(ActionStatus.AUTH_FAILED);
    }

    private AnnotationDescriptor<PermissionAllowed> createTestSubject(final String[] permsAllowed) {
        return new Builder<>(PermissionAllowed.class, Collections.singletonMap("value", permsAllowed)).build();
    }

}
