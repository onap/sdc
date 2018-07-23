package org.openecomp.sdc.be.ecomp;

import java.io.BufferedReader;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import org.junit.Test;
import org.openecomp.portalsdk.core.onboarding.exception.PortalAPIException;
import org.openecomp.portalsdk.core.restful.domain.EcompRole;
import org.openecomp.portalsdk.core.restful.domain.EcompUser;

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
        result = testSubject.getAvailableRoles();
    }

    @Test(expected=PortalAPIException.class)
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
        HttpServletRequestImpl httpServletRequestImpl = new HttpServletRequestImpl();
        // default test
        testSubject = createTestSubject();
        result = testSubject.isAppAuthenticated(httpServletRequestImpl);
    }

    @Test
    public void testGetUserId() throws Exception {
        EcompIntImpl testSubject;
        HttpServletRequestImpl httpServletRequestImpl = new HttpServletRequestImpl();
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getUserId(httpServletRequestImpl);
    }
    
    private class HttpServletRequestImpl implements HttpServletRequest {

        @Override
        public Object getAttribute(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getCharacterEncoding() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setCharacterEncoding(String env) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public int getContentLength() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public long getContentLengthLong() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public String getContentType() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ServletInputStream getInputStream() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getParameter(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Enumeration<String> getParameterNames() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String[] getParameterValues(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getProtocol() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getScheme() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getServerName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getServerPort() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public BufferedReader getReader() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getRemoteAddr() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getRemoteHost() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setAttribute(String name, Object o) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void removeAttribute(String name) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public Locale getLocale() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Enumeration<Locale> getLocales() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isSecure() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String path) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getRealPath(String path) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getRemotePort() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public String getLocalName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getLocalAddr() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getLocalPort() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public ServletContext getServletContext() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public AsyncContext startAsync() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isAsyncStarted() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isAsyncSupported() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public AsyncContext getAsyncContext() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public DispatcherType getDispatcherType() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getAuthType() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Cookie[] getCookies() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getDateHeader(String name) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public String getHeader(String name) {
            // TODO Auto-generated method stub
            return "mock";
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getIntHeader(String name) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public String getMethod() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getPathInfo() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getPathTranslated() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getContextPath() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getQueryString() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getRemoteUser() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isUserInRole(String role) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Principal getUserPrincipal() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getRequestedSessionId() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getRequestURI() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public StringBuffer getRequestURL() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getServletPath() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public HttpSession getSession(boolean create) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public HttpSession getSession() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String changeSessionId() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isRequestedSessionIdValid() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromCookie() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromURL() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromUrl() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean authenticate(HttpServletResponse httpServletResponse) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void login(String s, String s1) {
            // TODO Auto-generated method stub
        }

        @Override
        public void logout() {
            // TODO Auto-generated method stub
        }

        @Override
        public Collection<Part> getParts() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Part getPart(String s) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) {
            // TODO Auto-generated method stub
            return null;
        }
    }
}