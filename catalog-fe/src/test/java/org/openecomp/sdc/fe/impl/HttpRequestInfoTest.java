package org.openecomp.sdc.fe.impl;

import org.junit.Test;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

public class HttpRequestInfoTest {

    private HttpRequestInfo createTestSubject() {
        return new HttpRequestInfo(new HttpServletRequestTest(), null, "");
    }

    @Test
    public void testGetHeaders() {
        HttpRequestInfo testSubject;
        Map<String, String> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getHeaders();
    }

    @Test
    public void testSetHeaders() {
        HttpRequestInfo testSubject;
        Map<String, String> headers = null;

        // default test
        testSubject = createTestSubject();
        testSubject.setHeaders(headers);
    }

    @Test
    public void testGetRequestURL() {
        HttpRequestInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getRequestURL();
    }

    @Test
    public void testSetRequestURL() {
        HttpRequestInfo testSubject;
        String requestURL = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setRequestURL(requestURL);
    }

    @Test
    public void testGetRequestData() throws IOException {
        HttpRequestInfo testSubject;

        // default test
        testSubject = createTestSubject();
        InputStream result = testSubject.getRequestData();
        if (result != null) {
            result.close();
        }
    }

    @Test
    public void testSetRequestData() {
        HttpRequestInfo testSubject;
        InputStream requestData = null;

        // default test
        testSubject = createTestSubject();
        testSubject.setRequestData(requestData);
    }

    @Test
    public void testGetOriginServletContext() {
        HttpRequestInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getOriginServletContext();
    }

    @Test
    public void testSetOriginServletContext() {
        HttpRequestInfo testSubject;
        String originServletContext = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setOriginServletContext(originServletContext);
    }
    
    private class HttpServletRequestTest implements HttpServletRequest {
        
        private HttpServletRequestTest() {
            
        }
        @Override
        public Object getAttribute(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Enumeration getAttributeNames() {
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
        public Enumeration getParameterNames() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String[] getParameterValues(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Map getParameterMap() {
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
        public Enumeration getLocales() {
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
            return null;
        }

        @Override
        public Enumeration getHeaders(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Enumeration getHeaderNames() {
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
        public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) throws IOException, ServletException {
            return null;
        }

    }
}