/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.servlets;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Application;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.servlets.PortalServlet;

public class PortalServletTest extends JerseyTest {
	
	final static HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
	final static HttpSession httpSession = Mockito.mock(HttpSession.class);
	final static ServletContext servletContext = Mockito.mock(ServletContext.class);
	final static ConfigurationManager configurationManager = Mockito.mock(ConfigurationManager.class);
	final static Configuration configuration = Mockito.mock(Configuration.class);
	final static HttpServletResponse response = Mockito.spy(HttpServletResponse.class);
	final static RequestDispatcher rd = Mockito.spy(RequestDispatcher.class);

	@SuppressWarnings("serial")
	@BeforeClass
	public static void setUpTests() {
		when(request.getRequestDispatcher(Mockito.anyString())).thenReturn(rd);
		when(request.getSession()).thenReturn(httpSession);
		when(httpSession.getServletContext()).thenReturn(servletContext);
		when(servletContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).thenReturn(configurationManager);
		when(configurationManager.getConfiguration()).thenReturn(configuration);
		List<List<String>> mandatoryHeaders = new ArrayList<List<String>>();
		mandatoryHeaders.add(new ArrayList<String>() {
			{
				add("HTTP_IV_USER");
				add("iv-user");
			}
		});
		mandatoryHeaders.add(new ArrayList<String>() {
			{
				add("HTTP_CSP_ATTUID");
				add("csp-attuid");
			}
		});
		mandatoryHeaders.add(new ArrayList<String>() {
			{
				add("USER_ID");
				add("csp-userId");
			}
		});
		mandatoryHeaders.add(new ArrayList<String>() {
			{
				add("HTTP_CSP_WSTYPE");
				add("csp-wstype csp-wstype");
			}
		});

		List<List<String>> optionalHeaders = new ArrayList<List<String>>();
		optionalHeaders.add(new ArrayList<String>() {
			{
				add("HTTP_CSP_FIRSTNAME");
				add("csp-firstname");
			}
		});
		optionalHeaders.add(new ArrayList<String>() {
			{
				add("HTTP_CSP_LASTNAME");
				add("csp-lastname");
			}
		});
		optionalHeaders.add(new ArrayList<String>() {
			{
				add("HTTP_IV_REMOTE_ADDRESS");
				add("iv-remote-address");
			}
		});

		when(configuration.getIdentificationHeaderFields()).thenReturn(mandatoryHeaders);
		when(configuration.getOptionalHeaderFields()).thenReturn(optionalHeaders);

	}

	@Test
	public void testMissingHeadersRequest() throws IOException {
		when(request.getHeader(Mockito.anyString())).thenReturn(null);
		target().path("/portal").request().get();
		Mockito.verify(response, times(1)).sendError(HttpServletResponse.SC_USE_PROXY, PortalServlet.MISSING_HEADERS_MSG);
		Mockito.reset(response, rd);
	}

	@Test
	public void testSuccesfulRequest() throws IOException, ServletException {
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				String headerName = (String) args[0];
				return headerName;
			}
		}).when(request).getHeader(Mockito.anyString());
		target().path("/portal").request().get();
		verify(rd).forward(Mockito.any(ServletRequest.class), Mockito.any(ServletResponse.class));
		Mockito.reset(response, rd);
	}

	@Override
	protected Application configure() {
		ResourceConfig resourceConfig = new ResourceConfig(PortalServlet.class);

		resourceConfig.register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(request).to(HttpServletRequest.class);
				bind(response).to(HttpServletResponse.class);
			}
		});

		return resourceConfig;
	}
}
