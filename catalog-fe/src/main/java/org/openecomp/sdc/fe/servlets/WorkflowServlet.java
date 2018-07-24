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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.config.PluginsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Path("/wf")
public class WorkflowServlet extends LoggingServlet {

  private static final Logger log = LoggerFactory.getLogger(WorkflowServlet.class.getName());
  private static final String GET_WORKFLOW_ARTIFACT_URL = "%s/wf/workflows/%s/versions/%s/artifact";
  private static final String SAVE_WORKFLOW_ARTIFACT_URL = "%s://%s:%s/sdc2/rest/v1/catalog/resources/%s/interfaces/%s/artifacts/%s";
  private static final String PLUGIN_ID_WORKFLOW = "WORKFLOW";
  private static final String WORKFLOW_ARTIFACT_TYPE = "WORKFLOW";
  private static final String WORKFLOW_ARTIFACT_DESCRIPTION = "Workflow Artifact Description";

  @PUT
  @Path("/{resourceUUID}/{operationUUID}/workflow/{workflowId}/version/{versionId}/artifact/{artifactUUID}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response attachWorkflowToOperation(@PathParam("resourceUUID") String resourceId,
                                            @PathParam("operationUUID") String operationId,
                                            @PathParam("workflowId") String workflowId,
                                            @PathParam("versionId") String versionId,
                                            @PathParam("artifactUUID") String artifactId,
                                            @Context final HttpServletRequest httpRequest) {
    try {
      HttpClient client = createHTTPClient();

      ContentResponse wfGetResponse = getWorkflowArtifact(workflowId, versionId, httpRequest, client);
      if (wfGetResponse.getStatus() != 200) {
        log.error("Failed while fetching artifact from WF with error {} ", wfGetResponse);
        return Response.status(Response.Status.EXPECTATION_FAILED).entity(wfGetResponse).build();
      }
      String workflowArtifact = getFormattedWorkflowArtifact(wfGetResponse);

      ContentResponse wfSaveResponse = saveWorkflowArtifact(resourceId, operationId, artifactId, httpRequest, client,
          workflowArtifact);
      if (wfSaveResponse.getStatus() != 200) {
        log.error("Failed while saving artifact in SDC with error {} ", wfSaveResponse);
        return Response.status(Response.Status.EXPECTATION_FAILED).entity(wfSaveResponse).build();
      }

    } catch (Exception e) {
      log.error("Failed while attaching workflow artifact to Operation in SDC {} ", e);
      return Response.status(Response.Status.EXPECTATION_FAILED).entity(e).build();
    }
    return Response.status(Response.Status.OK).entity(null).build();
  }

  @Override
  protected void inHttpRequest(HttpServletRequest httpRequest) {
    log.info("{} {} {}", httpRequest.getMethod(), httpRequest.getRequestURI(), httpRequest.getProtocol());
  }

  @Override
  protected void outHttpResponse(Response response) {
    log.info("SC=\"{}\"", response.getStatus());
  }

  private HttpClient createHTTPClient() throws ServletException {
    HttpClient httpClient = new HttpClient();
    httpClient.setFollowRedirects(false);
    httpClient.setIdleTimeout(600000);
    httpClient.setStopTimeout(600000);
    try {
      httpClient.start();
      return httpClient;
    }
    catch (Exception ex) {
      throw new ServletException(ex);
    }
  }

  private ContentResponse saveWorkflowArtifact(String resourceId, String operationId,
                                               String artifactId, HttpServletRequest httpRequest,
                                               HttpClient client, String workflowArtifact)
      throws InterruptedException, TimeoutException, ExecutionException {

    Configuration config = ((ConfigurationManager) httpRequest.getSession().getServletContext()
        .getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).getConfiguration();

    Request wfSaveRequest = client.newRequest(String.format(SAVE_WORKFLOW_ARTIFACT_URL, config
        .getBeProtocol(), config.getBeHost(), config.getBeHttpPort(), resourceId, operationId, artifactId))
        .method(HttpMethod.PUT)
        .header(Constants.USER_ID_HEADER, httpRequest.getHeader(Constants.USER_ID_HEADER))
        .header(Constants.CONTENT_TYPE_HEADER, httpRequest.getHeader(Constants.CONTENT_TYPE_HEADER))
        .header(Constants.MD5_HEADER, GeneralUtility.calculateMD5Base64EncodedByString(workflowArtifact))
        .content(new StringContentProvider(workflowArtifact, StandardCharsets.UTF_8));
    return wfSaveRequest.send();
  }

  private ContentResponse getWorkflowArtifact(String workflowId, String versionId,
                                              HttpServletRequest httpRequest, HttpClient client)
      throws InterruptedException, TimeoutException, ExecutionException {

    PluginsConfiguration pluginsConfiguration = ((ConfigurationManager) httpRequest.getSession()
        .getServletContext().getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).getPluginsConfiguration();

    String workflowPluginURL = pluginsConfiguration.getPluginsList().stream().filter(plugin ->
        plugin.getPluginId().equalsIgnoreCase(PLUGIN_ID_WORKFLOW)).map(plugin -> plugin
        .getPluginSourceUrl()).findFirst().orElse(null);

    Request wfGetRequest = client.newRequest(String.format(GET_WORKFLOW_ARTIFACT_URL, workflowPluginURL,
        workflowId, versionId)).method(HttpMethod.GET)
        .header(Constants.USER_ID_HEADER, httpRequest.getHeader(Constants.USER_ID_HEADER));
    return wfGetRequest.send();
  }

  private String getFormattedWorkflowArtifact(ContentResponse getArtifactResponse) throws JsonProcessingException {
    byte[] encodeBase64 = Base64.encodeBase64(getArtifactResponse.getContentAsString().getBytes());
    String encodedPayloadData = new String(encodeBase64);

    String disposition = getArtifactResponse.getHeaders().get(Constants.CONTENT_DISPOSITION_HEADER);
    String artifactName = disposition.replaceFirst("(?i)^.*filename=\"?([^\"]+)\"?.*$", "$1");

    Map<String, String> artifactInfo = new HashMap<>();
    artifactInfo.put("artifactName", artifactName);
    artifactInfo.put("payloadData", encodedPayloadData);
    artifactInfo.put("artifactType", WORKFLOW_ARTIFACT_TYPE);
    artifactInfo.put("description", WORKFLOW_ARTIFACT_DESCRIPTION);

    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(artifactInfo);
  }
}
