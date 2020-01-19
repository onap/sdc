package org.openecomp.sdc.be.impl.aaf;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.components.impl.aaf.RoleAuthorizationHandler;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.servlets.BeGenericServlet;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.FilterDecisionEnum;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import sun.reflect.annotation.AnnotationParser;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RoleAuthorizationHandlerTest {

    private RoleAuthorizationHandler roleAuthorizationHandler = new RoleAuthorizationHandler();
    @Mock
    JoinPoint joinPoint;
    @Mock
    Signature signature;
    @Mock
    BeGenericServlet beGenericServlet;
    @Mock
    HttpServletRequest httpServletRequest;


    private static ConfigurationSource configurationSource = new FSConfigurationSource(
            ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be/auth");
    static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

    @Before
    public void setUp() {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("methodName");
        when(joinPoint.getThis()).thenReturn(beGenericServlet);
        when(beGenericServlet.getServletRequest()).thenReturn(httpServletRequest);
        ThreadLocalsHolder.setApiType(FilterDecisionEnum.EXTERNAL);
    }

    @Test
    public void testAuthorizeRoleOnePermittedRole() {
        String[] permsAllowed = {AafPermission.PermNames.WRITE_VALUE};
        PermissionAllowed rolesAllowed =
                (PermissionAllowed) AnnotationParser.annotationForMap(PermissionAllowed.class, Collections.singletonMap("value", permsAllowed));
        when(httpServletRequest.isUserInRole(AafPermission.getEnumByString(permsAllowed[0]).getFullPermission()))
                .thenReturn(true);
        roleAuthorizationHandler.authorizeRole(joinPoint, rolesAllowed);
    }

    @Test
    public void testAuthorizeRoleTwoPermittedRole() {
        String[] permsAllowed = {AafPermission.PermNames.WRITE_VALUE, AafPermission.PermNames.READ_VALUE};
        PermissionAllowed rolesAllowed =
                (PermissionAllowed) AnnotationParser.annotationForMap(PermissionAllowed.class, Collections.singletonMap("value", permsAllowed));
        when(httpServletRequest.isUserInRole(AafPermission.getEnumByString(permsAllowed[0]).getFullPermission()))
                .thenReturn(true);
        roleAuthorizationHandler.authorizeRole(joinPoint, rolesAllowed);
    }

    @Test
    public void testAuthorizeRoleNonPermittedRole() {
        String[] permsAllowed = {AafPermission.PermNames.WRITE_VALUE, AafPermission.PermNames.READ_VALUE};
        PermissionAllowed rolesAllowed =
                (PermissionAllowed) AnnotationParser.annotationForMap(PermissionAllowed.class, Collections.singletonMap("value", permsAllowed));
        when(httpServletRequest.isUserInRole(AafPermission.getEnumByString(permsAllowed[0]).getFullPermission()))
                .thenReturn(false);

        ComponentException thrown = (ComponentException) catchThrowable(()->roleAuthorizationHandler.authorizeRole(joinPoint, rolesAllowed));
        assertThat(thrown.getActionStatus()).isEqualTo(ActionStatus.AUTH_FAILED);
    }

    @Test
    public void testAuthorizeRoleEmptyRole() {
        String[] permsAllowed = {};
        PermissionAllowed rolesAllowed =
                (PermissionAllowed) AnnotationParser.annotationForMap(PermissionAllowed.class, Collections.singletonMap("value", permsAllowed));

        ComponentException thrown = (ComponentException) catchThrowable(()->roleAuthorizationHandler.authorizeRole(joinPoint, rolesAllowed));
        assertThat(thrown.getActionStatus()).isEqualTo(ActionStatus.AUTH_FAILED);
    }
}
