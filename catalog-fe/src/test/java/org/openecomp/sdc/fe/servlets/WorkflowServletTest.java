/*
 * Copyright © 2016-2018 European Support Limited
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

package org.openecomp.sdc.fe.servlets;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.config.PluginsConfiguration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class WorkflowServletTest extends JerseyTest {
  private static final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
  private static final HttpSession httpSession = Mockito.mock(HttpSession.class);
  private static final ServletContext servletContext = Mockito.mock(ServletContext.class);
  private static final ConfigurationManager configurationManager = Mockito.mock(ConfigurationManager.class);
  private static final Configuration configuration = Mockito.mock(Configuration.class);
  private static final HttpServletResponse servletResponse = Mockito.spy(HttpServletResponse.class);
  private static final PluginsConfiguration pluginsConfiguration = Mockito.mock(PluginsConfiguration.class);
  private static final PluginsConfiguration.Plugin plugin = Mockito.mock(PluginsConfiguration.Plugin.class);

  private static final String WF_PROTOCOL = "http";
  private static final String WF_HOST = "localhost";
  private static final int WF_PORT = 8091;

  private static final String BE_PROTOCOL = "http";
  private static final String BE_HOST = "localhost";
  private static final int BE_PORT = 8092;

  private static final String path = "wf/resourceId/operationId/workflow/workflowId/version/versionId/artifact/artifactId";
  private static final String saveURL = "/sdc2/rest/v1/catalog/resources/resourceId/interfaces/operationId/artifacts/artifactId";
  private static final String getURL = "/wf/workflows/workflowId/versions/versionId/artifact";

  private final static String USER_ID_HEADER_VAL = "cs0008";

  @Rule
  public WireMockRule service1 = new WireMockRule(WF_PORT);

  @Rule
  public WireMockRule service2 = new WireMockRule(BE_PORT);

  @BeforeClass
  public static void beforeClass() {
    when(servletRequest.getSession()).thenReturn(httpSession);
    when(httpSession.getServletContext()).thenReturn(servletContext);
    when(servletContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).thenReturn(configurationManager);
    when(configurationManager.getConfiguration()).thenReturn(configuration);

    when(configuration.getBeProtocol()).thenReturn(BE_PROTOCOL);
    when(configuration.getBeHost()).thenReturn(BE_HOST);
    when(configuration.getBeHttpPort()).thenReturn(BE_PORT);

    List<PluginsConfiguration.Plugin> pluginList = new ArrayList<PluginsConfiguration.Plugin>();
    when(plugin.getPluginId()).thenReturn("WORKFLOW");
    when(plugin.getPluginSourceUrl()).thenReturn(WF_PROTOCOL + "://" + WF_HOST + ":" + WF_PORT);
    pluginList.add(plugin);

    when(configurationManager.getPluginsConfiguration()).thenReturn(pluginsConfiguration);
    when(pluginsConfiguration.getPluginsList()).thenReturn(pluginList);

  }

  @Override
  protected Application configure() {
    ResourceConfig resourceConfig = new ResourceConfig(WorkflowServlet.class);
    resourceConfig.register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(servletRequest).to(HttpServletRequest.class);
        bind(servletResponse).to(HttpServletResponse.class);
      }
    });
    return resourceConfig;
  }

  @Test
  public void testAttachWorkflowSuccess() throws Exception {
    service1.stubFor(WireMock.get(WireMock.urlEqualTo(getURL)).willReturn(WireMock.aResponse()
        .withHeader(Constants.CONTENT_DISPOSITION_HEADER, "attachment:filename=TestArtifact.bpmn")
        .withBody("Test Artifact Data")));
    service2.stubFor(WireMock.put(WireMock.urlEqualTo(saveURL)).willReturn(WireMock.aResponse().withStatus(200)));
    Response response = target()
        .path(path)
        .request(MediaType.APPLICATION_JSON)
        .header(Constants.USER_ID_HEADER, USER_ID_HEADER_VAL)
        .header(Constants.CONTENT_TYPE_HEADER, MediaType.APPLICATION_JSON)
        .put(Entity.entity("TestData", MediaType.APPLICATION_JSON),Response.class);

    assertEquals(HttpStatus.OK_200.getStatusCode(), response.getStatus());
  }

  @Test
  public void testAttachWorkflowGetRequestFailure() throws Exception {
    service1.stubFor(WireMock.get(WireMock.urlEqualTo(getURL)).willReturn(WireMock.aResponse()
        .withBody("Test Artifact Data")));
    service2.stubFor(WireMock.put(WireMock.urlEqualTo(saveURL)).willReturn(WireMock.aResponse().withStatus(200)));
    Response response = target()
        .path(path)
        .request(MediaType.APPLICATION_JSON)
        .header(Constants.USER_ID_HEADER, USER_ID_HEADER_VAL)
        .header(Constants.CONTENT_TYPE_HEADER, MediaType.APPLICATION_JSON)
        .put(Entity.entity("TestData", MediaType.APPLICATION_JSON),Response.class);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode(), response.getStatus());
  }

  @Test
  public void testAttachWorkflowSaveRequestFailure() throws Exception {
    service1.stubFor(WireMock.get(WireMock.urlEqualTo(getURL)).willReturn(WireMock.aResponse()
        .withHeader(Constants.CONTENT_DISPOSITION_HEADER, "attachment:filename=TestArtifact.bpmn")
        .withBody("Test Artifact Data")));
    service2.stubFor(WireMock.put(WireMock.urlEqualTo(saveURL)).willReturn(WireMock.aResponse().withStatus(500)));
    Response response = target()
        .path(path)
        .request(MediaType.APPLICATION_JSON)
        .header(Constants.USER_ID_HEADER, USER_ID_HEADER_VAL)
        .header(Constants.CONTENT_TYPE_HEADER, MediaType.APPLICATION_JSON)
        .put(Entity.entity("TestData", MediaType.APPLICATION_JSON),Response.class);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode(), response.getStatus());
  }

}