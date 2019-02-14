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

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpFields;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.config.PluginsConfiguration;
import org.openecomp.sdc.fe.servlets.FeProxyServlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class FeProxyServletTest {
	/*
	 * Example Url Mappings:
	 * http://localhost:8080/sdc1/feProxy/rest/services/MichaelTest2/0.0.1/csar
	 * --> http://localhost:8090/sdc2/rest/services/MichaelTest2/0.0.1/csar
	 * http://localhost:8080/sdc1/feProxy/dummy/not/working -->
	 * http://localhost:8090/sdc2/dummy/not/working
	 */
	FeProxyServletForTest feProxy = new FeProxyServletForTest();
	final static HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
	final static HttpSession httpSession = Mockito.mock(HttpSession.class);
	final static ServletContext servletContext = Mockito.mock(ServletContext.class);
	final static ConfigurationManager configurationManager = Mockito.mock(ConfigurationManager.class);
	final static Configuration configuration = Mockito.mock(Configuration.class);
	final static Configuration.OnboardingConfig onboardingConfiguration = Mockito.mock(Configuration.OnboardingConfig.class);
	final static Request proxyRequest = Mockito.spy(Request.class);
	final static HttpFields httpFields = Mockito.mock(HttpFields.class);
	private static final PluginsConfiguration pluginsConfiguration = Mockito.mock(PluginsConfiguration.class);
	private static final PluginsConfiguration.Plugin plugin = Mockito.mock(PluginsConfiguration.Plugin.class);

	final static String BE_PROTOCOL = "http";
	final static String BE_HOST = "172.20.43.124";
	final static int BE_PORT = 8090;
	final static String ONBOARDING_BE_PROTOCOL = "http";
	final static String ONBOARDING_BE_HOST = "172.20.43.125";
	final static int ONBOARDING_BE_PORT = 8091;
	final static String WF_PROTOCOL = "http";
	final static String WF_HOST = "172.20.43.126";
	final static int WF_PORT = 8092;
	final static String HEADER_1 = "Header1";
	final static String HEADER_2 = "Header2";
	final static String HEADER_3 = "Header3";
	final static String HEADER_1_VAL = "Header1_Val";
	final static String HEADER_2_VAL = "Header2_Val";
	final static String HEADER_3_VAL = "Header3_Val";
	final static String REQUEST_ID_VAL = "4867495a-5ed7-49e4-8be2-cc8d66fdd52b";

	@BeforeClass
	public static void beforeClass() {
		when(servletRequest.getSession()).thenReturn(httpSession);
		when(httpSession.getServletContext()).thenReturn(servletContext);
		when(servletContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).thenReturn(configurationManager);
		when(configurationManager.getConfiguration()).thenReturn(configuration);
		when(configuration.getBeProtocol()).thenReturn(BE_PROTOCOL);
		when(configuration.getBeHost()).thenReturn(BE_HOST);
		when(configuration.getBeHttpPort()).thenReturn(BE_PORT);
		when(configuration.getOnboarding()).thenReturn(onboardingConfiguration);
		when(configuration.getOnboarding().getProtocolBe()).thenReturn(ONBOARDING_BE_PROTOCOL);
		when(configuration.getOnboarding().getHostBe()).thenReturn(ONBOARDING_BE_HOST);
		when(configuration.getOnboarding().getPortBe()).thenReturn(ONBOARDING_BE_PORT);

		List<String> strList = new ArrayList<String>();
		strList.add(HEADER_1);
		strList.add(HEADER_2);
		strList.add(HEADER_3);

		when(servletRequest.getHeaderNames()).thenReturn(Collections.enumeration(strList));
		when(servletRequest.getHeader(HEADER_1)).thenReturn(HEADER_1_VAL);
		when(servletRequest.getHeader(HEADER_2)).thenReturn(HEADER_2_VAL);
		when(servletRequest.getHeader(HEADER_3)).thenReturn(HEADER_3_VAL);
		when(servletRequest.getHeader(Constants.X_ECOMP_REQUEST_ID_HEADER)).thenReturn(REQUEST_ID_VAL);

		when(proxyRequest.getHeaders()).thenReturn(httpFields);
		when(httpFields.containsKey(HEADER_1)).thenReturn(true);
		when(httpFields.containsKey(HEADER_2)).thenReturn(true);
		when(httpFields.containsKey(HEADER_3)).thenReturn(false);

		List<PluginsConfiguration.Plugin> pluginList = new ArrayList<PluginsConfiguration.Plugin>();
		when(plugin.getPluginId()).thenReturn("WORKFLOW");
		when(plugin.getPluginSourceUrl()).thenReturn(WF_PROTOCOL + "://" + WF_HOST + ":" + WF_PORT);
		when(plugin.getPluginDiscoveryUrl()).thenReturn(WF_PROTOCOL + "://" + WF_HOST + ":" + WF_PORT + "/workflows");
		pluginList.add(plugin);
		when(configurationManager.getPluginsConfiguration()).thenReturn(pluginsConfiguration);
		when(pluginsConfiguration.getPluginsList()).thenReturn(pluginList);

	}

	@Test
	public void testRewriteURI_APIRequest() {
		when(servletRequest.getRequestURI()).thenReturn("/sdc1/feProxy/rest/dummyBeAPI");
		String requestResourceUrl = "http://localhost:8080/sdc1/feProxy/rest/dummyBeAPI";
		String expectedChangedUrl = BE_PROTOCOL + "://" + BE_HOST + ":" + BE_PORT + "/sdc2/rest/dummyBeAPI";
		when(servletRequest.getRequestURL()).thenReturn(new StringBuffer(requestResourceUrl));

		when(servletRequest.getContextPath()).thenReturn("/sdc1");
		when(servletRequest.getServletPath()).thenReturn("/feProxy/rest/dummyBeAPI");

		String rewriteURI = feProxy.rewriteTarget(servletRequest);

		assertTrue(rewriteURI.equals(expectedChangedUrl));
	}

	@Test
	public void testRewriteURIWithOnboardingAPIRequest() {
		when(servletRequest.getRequestURI()).thenReturn("/sdc1/feProxy/onboarding-api/gg%20g?subtype=VF");
		String requestResourceUrl = "http://localhost:8080/sdc1/feProxy/onboarding-api/gg%20g?subtype=VF";
		String expectedChangedUrl = ONBOARDING_BE_PROTOCOL + "://" + ONBOARDING_BE_HOST + ":" + ONBOARDING_BE_PORT + "/onboarding-api/gg%20g?subtype=VF";
		when(servletRequest.getRequestURL()).thenReturn(new StringBuffer(requestResourceUrl));

		when(servletRequest.getContextPath()).thenReturn("/sdc1");
		when(servletRequest.getServletPath()).thenReturn("/feProxy/onboarding-api/gg%20g?subtype=VF");

		String rewriteURI = feProxy.rewriteTarget(servletRequest);

		assertTrue(rewriteURI.equals(expectedChangedUrl));
	}


	@Test
	public void testRewriteURIWithQureyParam_APIRequest() {
		when(servletRequest.getRequestURI()).thenReturn("/sdc1/feProxy/dcae-api/gg%20g?subtype=VF");
		String requestResourceUrl = "http://localhost:8080/sdc1/feProxy/dcae-api/gg%20g?subtype=VF";
		String expectedChangedUrl = BE_PROTOCOL + "://" + BE_HOST + ":" + BE_PORT + "/dcae-api/gg%20g?subtype=VF";
		when(servletRequest.getRequestURL()).thenReturn(new StringBuffer(requestResourceUrl));

		when(servletRequest.getContextPath()).thenReturn("/sdc1");
		when(servletRequest.getServletPath()).thenReturn("/feProxy/dcae-api/gg%20g?subtype=VF");

		String rewriteURI = feProxy.rewriteTarget(servletRequest);

		assertTrue(rewriteURI.equals(expectedChangedUrl));
	}

	@Test
	public void testRewriteTargetWithRedeirectAPIRequest() {
		when(servletRequest.getRequestURI()).thenReturn("/sdc1/feProxy/rest/gg%20g?subtype=VF");
		String requestResourceUrl = "http://localhost:8080/sdc1/feProxy/rest/gg%20g?subtype=VF";
		String expectedChangedUrl = BE_PROTOCOL + "://" + BE_HOST + ":" + BE_PORT + "/sdc2/rest/gg%20g?subtype=VF";
		when(servletRequest.getRequestURL()).thenReturn(new StringBuffer(requestResourceUrl));

		when(servletRequest.getContextPath()).thenReturn("/sdc1");
		when(servletRequest.getServletPath()).thenReturn("/feProxy/rest/gg%20g?subtype=VF");

		String rewriteURI = feProxy.rewriteTarget(servletRequest);

		assertTrue(rewriteURI.equals(expectedChangedUrl));
	}

	@Test
	public void testRewriteURIWithWFAPIRequest() {
		when(servletRequest.getRequestURI()).thenReturn("/sdc1/feProxy/wf/workflows");
		String requestResourceUrl = "http://localhost:8080/sdc1/feProxy/wf/workflows";
		String expectedChangedUrl = WF_PROTOCOL + "://" + WF_HOST + ":" + WF_PORT + "/workflows/wf/workflows";
		when(servletRequest.getRequestURL()).thenReturn(new StringBuffer(requestResourceUrl));

		when(servletRequest.getContextPath()).thenReturn("/sdc1");
		when(servletRequest.getServletPath()).thenReturn("/feProxy/wf/workflows");

		String rewriteURI = feProxy.rewriteTarget(servletRequest);

		assertEquals(expectedChangedUrl, rewriteURI);
	}

	/**
	 * class for testing only exposes the protected method.
	 */
	public static class FeProxyServletForTest extends FeProxyServlet{

		@Override
		public String rewriteTarget(HttpServletRequest request) {
			return super.rewriteTarget(request);
		}
	}
}
