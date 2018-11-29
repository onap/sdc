package org.openecomp.sdc.be.ecomp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.portalsdk.core.onboarding.exception.PortalAPIException;
import org.onap.portalsdk.core.restful.domain.EcompRole;
import org.onap.portalsdk.core.restful.domain.EcompUser;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PortalRestAPICentralServiceImplTest {
    @Mock
    ContextLoader conLoader;
    @Mock
    WebApplicationContext apContext;
    @Mock
    UserBusinessLogic userBusinessLogic;
    @Mock
    ContextLoader ctx;
    @InjectMocks
    PortalRestAPICentralServiceImpl testSubject;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAppCredentials() throws Exception {
        Map<String, String> appCredentials = testSubject.getAppCredentials();
        Assert.assertTrue(appCredentials.get(PortalRestAPICentralServiceImpl.PortalPropertiesEnum.PORTAL_APP_NAME.value()).equals("sdc"));
        Assert.assertTrue(appCredentials.get(PortalRestAPICentralServiceImpl.PortalPropertiesEnum.PORTAL_USER.value()).equals("sdc"));
        Assert.assertTrue(appCredentials.get(PortalRestAPICentralServiceImpl.PortalPropertiesEnum.PORTAL_PASS.value()).equals("asdc"));
    }

    /*@Test
    public void testPushUser() throws Exception {
        EcompUser user = new EcompUser();
        Set<EcompRole> roleSet = new HashSet<>();
        EcompRole role = new EcompRole();
        role.setId(1L);
        role.setName("Designer");
        roleSet.add(role);
        user.setRoles(roleSet);
        testSubject.pushUser(user);
    }*/

    @Test
    public void testPushUserNullRoles() throws Exception {
        EcompUser user = new EcompUser();
        try{
            testSubject.pushUser(user);
        } catch (PortalAPIException e){
            Assert.assertTrue(e.getMessage().equals("Received null roles for user" + user));
        }

    }

    @Test
    public void testPushUserUserNull() throws Exception {
        try {
            testSubject.pushUser(null);
        } catch (PortalAPIException e) {
            Assert.assertTrue(e.getMessage().equals("Received null for argument user"));
        }

    }

    /**
    *
    * Method: editUser(String loginId, EcompUser user)
    *
    */
    @Test
    public void testEditUser() throws Exception {
    //TODO: Test goes here...
    }

    /**
    *
    * Method: getUserId(HttpServletRequest request)
    *
    */
    @Test
    public void testGetUserId() throws Exception {
    //TODO: Test goes here...
    }

    /**
    *
    * Method: value()
    *
    */
    @Test
    public void testValue() throws Exception {
    //TODO: Test goes here...
    }

    /**
    *
    * Method: checkIfSingleRoleProvided(EcompUser user)
    *
    */
    @Test
    public void testCheckIfSingleRoleProvided() throws Exception {
    //TODO: Test goes here...
    /*
    try {
       Method method = PortalRestAPICentralServiceImpl.getClass().getMethod("checkIfSingleRoleProvided", EcompUser.class);
       method.setAccessible(true);
       method.invoke(<Object>, <Parameters>);
    } catch(NoSuchMethodException e) {
    } catch(IllegalAccessException e) {
    } catch(InvocationTargetException e) {
    }
    */
    }

} 
