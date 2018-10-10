package org.openecomp.sdc.be.ecomp;

import org.junit.Test;
import org.mockito.Mockito;
import org.onap.portalsdk.core.onboarding.exception.PortalAPIException;
import org.onap.portalsdk.core.restful.domain.EcompRole;
import org.onap.portalsdk.core.restful.domain.EcompUser;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class EcompIntImplTest {

    private EcompIntImpl createTestSubject() {
        return new EcompIntImpl();
    }

    @Test(expected=PortalAPIException.class)
    public void testPushUser() throws Exception {
        EcompIntImpl testSubject;
        EcompUser user = null;

        // default test
        testSubject = createTestSubject();
        testSubject.pushUser(user);
    }

    @Test(expected=PortalAPIException.class)
    public void testEditUser() throws Exception {
        EcompIntImpl testSubject;
        String loginId = "";
        EcompUser user = null;

        // default test
        testSubject = createTestSubject();
        testSubject.editUser(loginId, user);
    }

    @Test(expected=PortalAPIException.class)
    public void testGetUser() throws Exception {
        EcompIntImpl testSubject;
        String loginId = "";
        EcompUser result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getUser(loginId);
    }

    @Test(expected=PortalAPIException.class)
    public void testGetUsers() throws Exception {
        EcompIntImpl testSubject;
        List<EcompUser> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getUsers();
    }

    @Test
    public void testGetAvailableRoles() throws Exception {
        EcompIntImpl testSubject;
        List<EcompRole> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getAvailableRoles("Mock");
    }

    @Test(expected= PortalAPIException.class)
    public void testGetUserRoles() throws Exception {
        EcompIntImpl testSubject;
        String loginId = "";
        List<EcompRole> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getUserRoles(loginId);
    }

    @Test
    public void testIsAppAuthenticated() throws Exception {
        EcompIntImpl testSubject;
        boolean result;
        HttpServletRequest httpServletRequestImpl = Mockito.mock(HttpServletRequest.class);
        // default test
        testSubject = createTestSubject();
        result = testSubject.isAppAuthenticated(httpServletRequestImpl);
    }

    @Test
    public void testGetUserId() throws Exception {
        EcompIntImpl testSubject;
        HttpServletRequest httpServletRequestImpl = Mockito.mock(HttpServletRequest.class);
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getUserId(httpServletRequestImpl);
    }
}