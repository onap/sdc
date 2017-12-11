/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.sdc.logging.servlet;

import org.slf4j.MDC;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.servlet.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * TODO: Add more tests
 *
 * @author EVITALIY
 * @since 17/08/2016.
 */
public class LoggingFilterTest {

  private static final String REMOTE_HOST = UUID.randomUUID().toString();

  @Test
  public void testDoFilter() throws Exception {
    LoggingFilter loggingFilter = new LoggingFilter();
    ServletRequest mockRequest = new TestServletRequest();
    ServletResponse mockResponse = new TestServletResponse();
    TestFilterChain mockChain = new TestFilterChain();
    loggingFilter.doFilter(mockRequest, mockResponse, mockChain);
    assertEquals(1, mockChain.getCount());
    assertNull(MDC.getCopyOfContextMap());
  }

  private static class TestServletRequest implements ServletRequest {

    @Override
    public Object getAttribute(String s) {
      return null;
    }

    @Override
    public Enumeration getAttributeNames() {
      return null;
    }

    @Override
    public String getCharacterEncoding() {
      return null;
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
      return 0;
    }

    @Override
    public String getContentType() {
      return null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
      return null;
    }

    @Override
    public String getParameter(String s) {
      return null;
    }

    @Override
    public Enumeration getParameterNames() {
      return null;
    }

    @Override
    public String[] getParameterValues(String s) {
      return new String[0];
    }

    @Override
    public Map getParameterMap() {
      return null;
    }

    @Override
    public String getProtocol() {
      return null;
    }

    @Override
    public String getScheme() {
      return null;
    }

    @Override
    public String getServerName() {
      return null;
    }

    @Override
    public int getServerPort() {
      return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
      return null;
    }

    @Override
    public String getRemoteAddr() {
      return null;
    }

    @Override
    public String getRemoteHost() {
      return REMOTE_HOST;
    }

    @Override
    public void setAttribute(String s, Object o) {

    }

    @Override
    public void removeAttribute(String s) {

    }

    @Override
    public Locale getLocale() {
      return null;
    }

    @Override
    public Enumeration getLocales() {
      return null;
    }

    @Override
    public boolean isSecure() {
      return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
      return null;
    }

    @Override
    public String getRealPath(String s) {
      return null;
    }

    @Override
    public int getRemotePort() {
      return 0;
    }

    @Override
    public String getLocalName() {
      return null;
    }

    @Override
    public String getLocalAddr() {
      return null;
    }

    @Override
    public int getLocalPort() {
      return 0;
    }
  }

  private static class TestFilterChain implements FilterChain {

    private AtomicInteger count = new AtomicInteger(0);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
        throws IOException, ServletException {

      Assert.assertNotNull(MDC.get("RequestId"));
      Assert.assertEquals(MDC.get("ServiceInstanceId"), "N/A");
      Assert.assertEquals(MDC.get("ServiceName"), "ASDC");
      Assert.assertEquals(MDC.get("InstanceUUID"), "N/A");
      Assert.assertEquals(MDC.get("RemoteHost"), REMOTE_HOST);

      InetAddress host = InetAddress.getLocalHost();
      Assert.assertEquals(MDC.get("ServerIPAddress"), host.getHostAddress());
      Assert.assertEquals(MDC.get("ServerFQDN"), host.getHostName());

      count.incrementAndGet();
    }

    public int getCount() {
      return count.get();
    }
  }

  private static class TestServletResponse implements ServletResponse {

    @Override
    public String getCharacterEncoding() {
      return null;
    }

    @Override
    public void setCharacterEncoding(String s) {

    }

    @Override
    public String getContentType() {
      return null;
    }

    @Override
    public void setContentType(String s) {

    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
      return null;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
      return null;
    }

    @Override
    public void setContentLength(int i) {

    }

    @Override
    public int getBufferSize() {
      return 0;
    }

    @Override
    public void setBufferSize(int i) {

    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
      return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public Locale getLocale() {
      return null;
    }

    @Override
    public void setLocale(Locale locale) {

    }
  }
}
