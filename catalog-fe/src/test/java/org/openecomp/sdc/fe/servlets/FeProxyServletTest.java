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

package org.openecomp.sdc.fe.servlets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpFields;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.config.PluginsConfiguration;

class FeProxyServletTest {

    /*
     * Example Url Mappings:
     * http://localhost:8080/sdc1/feProxy/rest/services/MichaelTest2/0.0.1/csar
     * --> http://localhost:8090/sdc2/rest/services/MichaelTest2/0.0.1/csar
     * http://localhost:8080/sdc1/feProxy/dummy/not/working -->
     * http://localhost:8090/sdc2/dummy/not/working
     */
    private static final FeProxyServletForTest feProxy = new FeProxyServletForTest();
    private static final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
    private static final HttpSession httpSession = Mockito.mock(HttpSession.class);
    private static final ServletContext servletContext = Mockito.mock(ServletContext.class);
    private static final ConfigurationManager configurationManager = Mockito.mock(ConfigurationManager.class);
    private static final Configuration configuration = Mockito.mock(Configuration.class);
    private static final Configuration.OnboardingConfig onboardingConfiguration = Mockito.mock(Configuration.OnboardingConfig.class);
    private static final Configuration.CatalogFacadeMsConfig catalogFacadeMsConfig = Mockito.mock(Configuration.CatalogFacadeMsConfig.class);
    private static final Request proxyRequest = Mockito.spy(Request.class);
    private static final HttpFields httpFields = Mockito.mock(HttpFields.class);
    private static final PluginsConfiguration pluginsConfiguration = Mockito.mock(PluginsConfiguration.class);
    private static final PluginsConfiguration.Plugin plugin = Mockito.mock(PluginsConfiguration.Plugin.class);

    private static final String BE_PROTOCOL = "http";
    private static final String BE_HOST = "172.20.43.124";
    private static final int BE_PORT = 8090;
    private static final String ONBOARDING_BE_PROTOCOL = "http";
    private static final String ONBOARDING_BE_HOST = "172.20.43.125";
    private static final int ONBOARDING_BE_PORT = 8091;
    private static final String WF_PROTOCOL = "http";
    private static final String WF_HOST = "172.20.43.126";
    private static final int WF_PORT = 8092;
    private static final String HEADER_1 = "Header1";
    private static final String HEADER_2 = "Header2";
    private static final String HEADER_3 = "Header3";
    private static final String HEADER_1_VAL = "Header1_Val";
    private static final String HEADER_2_VAL = "Header2_Val";
    private static final String HEADER_3_VAL = "Header3_Val";
    private static final String REQUEST_ID_VAL = "4867495a-5ed7-49e4-8be2-cc8d66fdd52b";
    private static final String msProtocol = "http";
    private static final String msHealth = "/healthCheck";
    private static final String msHost = "localhost";
    private static final Integer msPort = 8080;
    private static final String msPath = "/uicache";
    private static final String msUrl = String.format("%s://%s:%s", msProtocol, msHost, msPort);

    @BeforeAll
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

        List<String> strList = new ArrayList<>();
        strList.add(HEADER_1);
        strList.add(HEADER_2);
        strList.add(HEADER_3);

        when(servletRequest.getHeaderNames()).thenReturn(Collections.enumeration(strList));
        when(servletRequest.getHeader(HEADER_1)).thenReturn(HEADER_1_VAL);
        when(servletRequest.getHeader(HEADER_2)).thenReturn(HEADER_2_VAL);
        when(servletRequest.getHeader(HEADER_3)).thenReturn(HEADER_3_VAL);
        when(servletRequest.getHeader(Constants.X_ECOMP_REQUEST_ID_HEADER)).thenReturn(REQUEST_ID_VAL);

        when(proxyRequest.getHeaders()).thenReturn(httpFields);
        when(httpFields.contains(HEADER_1)).thenReturn(true);
        when(httpFields.contains(HEADER_2)).thenReturn(true);
        when(httpFields.contains(HEADER_3)).thenReturn(false);

        List<PluginsConfiguration.Plugin> pluginList = new ArrayList<PluginsConfiguration.Plugin>();
        when(plugin.getPluginId()).thenReturn("WORKFLOW");
        when(plugin.getPluginSourceUrl()).thenReturn(WF_PROTOCOL + "://" + WF_HOST + ":" + WF_PORT);
        when(plugin.getPluginDiscoveryUrl()).thenReturn(WF_PROTOCOL + "://" + WF_HOST + ":" + WF_PORT);
        pluginList.add(plugin);
        when(configurationManager.getPluginsConfiguration()).thenReturn(pluginsConfiguration);
        when(pluginsConfiguration.getPluginsList()).thenReturn(pluginList);

    }

    @BeforeEach
    public void setUp() {
        when(configuration.getCatalogFacadeMs()).thenReturn(catalogFacadeMsConfig);
        when(servletRequest.getQueryString()).thenReturn(null);
        when(catalogFacadeMsConfig.getPath()).thenReturn(null);
    }

    @Test
    void testRewriteURI_APIRequest() {
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
    void testRewriteURIWithOnboardingAPIRequest() {
        when(servletRequest.getRequestURI()).thenReturn("/sdc1/feProxy/onboarding-api/gg%20g?subtype=VF");
        String requestResourceUrl = "http://localhost:8080/sdc1/feProxy/onboarding-api/gg%20g?subtype=VF";
        String expectedChangedUrl =
            ONBOARDING_BE_PROTOCOL + "://" + ONBOARDING_BE_HOST + ":" + ONBOARDING_BE_PORT + "/onboarding-api/gg%20g?subtype=VF";
        when(servletRequest.getRequestURL()).thenReturn(new StringBuffer(requestResourceUrl));

        when(servletRequest.getContextPath()).thenReturn("/sdc1");
        when(servletRequest.getServletPath()).thenReturn("/feProxy/onboarding-api/gg%20g?subtype=VF");

        String rewriteURI = feProxy.rewriteTarget(servletRequest);

        assertTrue(rewriteURI.equals(expectedChangedUrl));
    }

    @Test
    void testRewriteURIWithQureyParam_APIRequest() {
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
    void testRewriteTargetWithRedeirectAPIRequest() {
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
    void testRewriteURIWithWFAPIRequest() {
        when(servletRequest.getRequestURI()).thenReturn("/sdc1/feProxy/wf/workflows");
        String requestResourceUrl = "http://localhost:8080/sdc1/feProxy/wf/workflows";
        String expectedChangedUrl = WF_PROTOCOL + "://" + WF_HOST + ":" + WF_PORT + "/wf/workflows";
        when(servletRequest.getRequestURL()).thenReturn(new StringBuffer(requestResourceUrl));

        when(servletRequest.getContextPath()).thenReturn("/sdc1");
        when(servletRequest.getServletPath()).thenReturn("/feProxy/wf/workflows");

        String rewriteURI = feProxy.rewriteTarget(servletRequest);

        assertEquals(expectedChangedUrl, rewriteURI);
    }

    @Test
    void testRedirectToMSWhenMsUrlExists() throws MalformedURLException {
        final String urlParams = "x=1&y=2&z=3";
        final String url = "http//test.com:8080/uicache/v1/catalog";
        setUpConfigMocks();
        when(servletRequest.getRequestURL()).thenReturn(new StringBuffer(url));
        when(servletRequest.getQueryString()).thenReturn(urlParams);
        assertTrue(feProxy.isMsRequest(url + urlParams));
        assertEquals(msUrl + "/uicache/v1/catalog?" + urlParams,
            feProxy.redirectMsRequestToMservice(servletRequest, configuration));
    }

    @Test
    void testRedirectToMSWhenMsUrlExistsWithoutParams() throws MalformedURLException {
        final String uri = "/uicache/v1/home";
        final String url = String.format("http//test.com:8080%s", uri);
        setUpConfigMocks();
        when(servletRequest.getRequestURL()).thenReturn(new StringBuffer(url));
        when(servletRequest.getRequestURI()).thenReturn(uri);
        assertTrue(feProxy.isMsRequest(url));
        assertEquals(msUrl + "/uicache/v1/home", feProxy.redirectMsRequestToMservice(servletRequest, configuration));
    }

    @Test
    void testRedirectToBeOnToggleOff() throws MalformedURLException {
        final String uri = "/uicache/v1/catalog";
        final String url = String.format("http//test.com:8080%s", uri);
        when(catalogFacadeMsConfig.getPath()).thenReturn(null);

        when(servletRequest.getRequestURL()).thenReturn(new StringBuffer(url));
        when(servletRequest.getRequestURI()).thenReturn(uri);
        assertTrue(feProxy.isMsRequest(url));
        String expectedUrl = String.format("%s://%s:%s/rest/v1/screen?excludeTypes=VFCMT&excludeTypes=Configuration",
            BE_PROTOCOL, BE_HOST, BE_PORT);
        assertEquals(expectedUrl, feProxy.redirectMsRequestToMservice(servletRequest, configuration));
    }

    @Test
    void testRedirectToMSWhenMsUrlExistsButItIsNotCatalogRequest() throws MalformedURLException {
        final String url = "http//test.com:8080/rest/v1/sc";
        final String urlParams = "x=1&y=2&z=3";
        setUpConfigMocks();
        when(servletRequest.getRequestURL()).thenReturn(new StringBuffer(url));
        when(servletRequest.getQueryString()).thenReturn(urlParams);
        assertFalse(feProxy.isMsRequest(url));
        assertThrows(StringIndexOutOfBoundsException.class, () -> {
            feProxy.redirectMsRequestToMservice(servletRequest, configuration);
        });
    }

    private void setUpConfigMocks() {
        when(catalogFacadeMsConfig.getPath()).thenReturn(msPath);
        when(catalogFacadeMsConfig.getProtocol()).thenReturn(msProtocol);
        when(catalogFacadeMsConfig.getHost()).thenReturn(msHost);
        when(catalogFacadeMsConfig.getPort()).thenReturn(msPort);
        when(catalogFacadeMsConfig.getHealthCheckUri()).thenReturn(msHealth);
    }

    /* class for testing only exposes the protected method.*/
    private static class FeProxyServletForTest extends FeProxyServlet {

        private static final long serialVersionUID = 1L;

        @Override
        public String rewriteTarget(HttpServletRequest request) {
            return super.rewriteTarget(request);
        }

        @Override
        boolean isMsRequest(String currentUrl) {
            return super.isMsRequest(currentUrl);
        }
    }
}
