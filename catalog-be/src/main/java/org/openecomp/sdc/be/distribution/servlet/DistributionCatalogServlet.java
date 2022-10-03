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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.distribution.servlet;

import com.jcabi.aspects.Loggable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.Servers;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.servlets.BeGenericServlet;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

/**
 * This Servlet serves external users to download artifacts.
 *
 * @author tgitelman
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tags({@Tag(name = "SDCE-7 APIs")})
@Servers({@Server(url = "/sdc")})
@Singleton
public class DistributionCatalogServlet extends BeGenericServlet {

    private static final String DOWNLOAD_ARTIFACT_FAILED_WITH_EXCEPTION = "download artifact failed with exception";
    private static final String MISSING_X_ECOMP_INSTANCE_ID_HEADER = "Missing X-ECOMP-InstanceID header";
    private static final Logger log = Logger.getLogger(DistributionCatalogServlet.class);
    private final ArtifactsBusinessLogic artifactsBusinessLogic;
    @Context
    private HttpServletRequest request;

    @Inject
    public DistributionCatalogServlet(ComponentsUtils componentsUtils,
                                      ArtifactsBusinessLogic artifactsBusinessLogic) {
        super(componentsUtils);
        this.artifactsBusinessLogic = artifactsBusinessLogic;
    }
    // *******************************************************
    // Download (GET) artifacts
    // **********************************************************/

    /**
     * @param requestId
     * @param instanceIdHeader
     * @param accept
     * @param authorization
     * @param serviceName
     * @param serviceVersion
     * @param artifactName
     * @return
     */
    @GET
    @Path("/services/{serviceName}/{serviceVersion}/artifacts/{artifactName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(description = "Download service artifact", method = "GET", summary = "Returns downloaded artifact", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "200", description = "The artifact is found and streamed.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "400", description = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
        @ApiResponse(responseCode = "401", description = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
        @ApiResponse(responseCode = "403", description = "ECOMP component is not authorized - POL5003"),
        @ApiResponse(responseCode = "404", description = "Specified Service is not found - SVC4503"),
        @ApiResponse(responseCode = "404", description = "Specified Service Version is  not  found - SVC4504"),
        @ApiResponse(responseCode = "404", description = "Specified artifact is  not found - SVC4505"),
        @ApiResponse(responseCode = "405", description = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
        @ApiResponse(responseCode = "500", description = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000")})
    @PermissionAllowed({AafPermission.PermNames.READ_VALUE})
    public Response downloadServiceArtifact(
        @Parameter(description = "X-ECOMP-RequestID header", required = false) @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
        @Parameter(description = "X-ECOMP-InstanceID header", required = true) @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
        @Parameter(description = "Determines the format of the body of the response", required = false) @HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
        @Parameter(description = "The username and password", required = true) @HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
        @PathParam("serviceName") final String serviceName, @PathParam("serviceVersion") final String serviceVersion,
        @PathParam("artifactName") final String artifactName) {
        String requestURI = request.getRequestURI();
        Wrapper<Response> responseWrapper = validateInstanceIdHeader(new Wrapper<>(), instanceIdHeader, requestURI);
        if (!responseWrapper.isEmpty()) {
            return responseWrapper.getInnerElement();
        }
        try {
            byte[] downloadRsrcArtifactEither = artifactsBusinessLogic.downloadServiceArtifactByNames(serviceName, serviceVersion, artifactName);
            byte[] value = downloadRsrcArtifactEither;
            InputStream is = new ByteArrayInputStream(value);
            Map<String, String> headers = new HashMap<>();
            headers.put(Constants.CONTENT_DISPOSITION_HEADER, getContentDispositionValue(artifactName));
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
            getComponentsUtils().auditDistributionDownload(responseFormat, new DistributionData(instanceIdHeader, requestURI));
            return buildOkResponse(responseFormat, is, headers);
        } catch (ComponentException e) {
            getComponentsUtils().auditDistributionDownload(e.getResponseFormat(), new DistributionData(instanceIdHeader, requestURI));
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("download Murano package artifact for service - external API");
            log.debug(DOWNLOAD_ARTIFACT_FAILED_WITH_EXCEPTION, e);
            return buildErrorResponse(e.getResponseFormat());
        }
    }

    private Wrapper<Response> validateInstanceIdHeader(Wrapper<Response> responseWrapper, String instanceIdHeader, String requestURI) {
        if (instanceIdHeader == null || instanceIdHeader.isEmpty()) {
            log.debug(MISSING_X_ECOMP_INSTANCE_ID_HEADER);
            ResponseFormat errorResponseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
            getComponentsUtils().auditDistributionDownload(errorResponseFormat, new DistributionData(instanceIdHeader, requestURI));
            responseWrapper.setInnerElement(buildErrorResponse(errorResponseFormat));
        }
        return responseWrapper;
    }

    /**
     * @param requestId
     * @param instanceIdHeader
     * @param accept
     * @param authorization
     * @param serviceName
     * @param serviceVersion
     * @param resourceName
     * @param resourceVersion
     * @param artifactName
     * @return
     */
    @GET
    @Path("/services/{serviceName}/{serviceVersion}/resources/{resourceName}/{resourceVersion}/artifacts/{artifactName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(description = "Download resource artifact", method = "GET", summary = "Returns downloaded artifact", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "200", description = "The artifact is found and streamed.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "400", description = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
        @ApiResponse(responseCode = "401", description = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
        @ApiResponse(responseCode = "403", description = "ECOMP component is not authorized - POL5003"),
        @ApiResponse(responseCode = "404", description = "Specified Service is not found - SVC4503"),
        @ApiResponse(responseCode = "404", description = "Specified Resource Instance  is not found - SVC4526"),
        @ApiResponse(responseCode = "404", description = "Specified Service Version is  not  found - SVC4504"),
        @ApiResponse(responseCode = "404", description = "Specified artifact is  not found - SVC4505"),
        @ApiResponse(responseCode = "405", description = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
        @ApiResponse(responseCode = "500", description = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000")})
    @PermissionAllowed({AafPermission.PermNames.READ_VALUE})
    public Response downloadResourceArtifact(
        @Parameter(description = "X-ECOMP-RequestID header", required = false) @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
        @Parameter(description = "X-ECOMP-InstanceID header", required = true) @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
        @Parameter(description = "Determines the format of the body of the response", required = false) @HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
        @Parameter(description = "The username and password", required = true) @HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
        @PathParam("serviceName") final String serviceName, @PathParam("serviceVersion") final String serviceVersion,
        @PathParam("resourceName") final String resourceName, @PathParam("resourceVersion") final String resourceVersion,
        @PathParam("artifactName") final String artifactName) {
        String requestURI = request.getRequestURI();
        Wrapper<Response> responseWrapper = validateInstanceIdHeader(new Wrapper<>(), instanceIdHeader, requestURI);
        if (!responseWrapper.isEmpty()) {
            return responseWrapper.getInnerElement();
        }
        try {
            ArtifactsBusinessLogic artifactsLogic = getArtifactBL(request.getSession().getServletContext());
            byte[] downloadRsrcArtifactEither = artifactsLogic
                .downloadRsrcArtifactByNames(serviceName, serviceVersion, resourceName, resourceVersion, artifactName);
            byte[] value = downloadRsrcArtifactEither;
            // Returning 64-encoded as it was received during upload
            InputStream is = new ByteArrayInputStream(value);
            Map<String, String> headers = new HashMap<>();
            headers.put(Constants.CONTENT_DISPOSITION_HEADER, getContentDispositionValue(artifactName));
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
            getComponentsUtils().auditDistributionDownload(responseFormat, new DistributionData(instanceIdHeader, requestURI));
            return buildOkResponse(responseFormat, is, headers);
        } catch (ComponentException e) {
            getComponentsUtils().auditDistributionDownload(e.getResponseFormat(), new DistributionData(instanceIdHeader, requestURI));
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("download interface artifact for resource - external API");
            log.debug(DOWNLOAD_ARTIFACT_FAILED_WITH_EXCEPTION, e);
            return buildErrorResponse(e.getResponseFormat());
        }
    }

    /**
     * @param requestId
     * @param instanceIdHeader
     * @param accept
     * @param authorization
     * @param serviceName
     * @param serviceVersion
     * @param resourceInstanceName
     * @param artifactName
     * @return
     */
    @GET
    @Path("/services/{serviceName}/{serviceVersion}/resourceInstances/{resourceInstanceName}/artifacts/{artifactName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(description = "Download resource instance artifact", method = "GET", summary = "Returns downloaded artifact", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "200", description = "The artifact is found and streamed.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "400", description = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
        @ApiResponse(responseCode = "401", description = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
        @ApiResponse(responseCode = "403", description = "ECOMP component is not authorized - POL5003"),
        @ApiResponse(responseCode = "404", description = "Specified Service is not found - SVC4503"),
        @ApiResponse(responseCode = "404", description = "Specified Resource Instance  is not found - SVC4526"),
        @ApiResponse(responseCode = "404", description = "Specified Service Version is  not  found - SVC4504"),
        @ApiResponse(responseCode = "404", description = "Specified artifact is  not found - SVC4505"),
        @ApiResponse(responseCode = "405", description = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
        @ApiResponse(responseCode = "500", description = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000")})
    @PermissionAllowed({AafPermission.PermNames.READ_VALUE})
    public Response downloadResourceInstanceArtifactByName(
        @Parameter(description = "X-ECOMP-RequestID header", required = false) @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
        @Parameter(description = "X-ECOMP-InstanceID header", required = true) @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
        @Parameter(description = "Determines the format of the body of the response", required = false) @HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
        @Parameter(description = "The username and password", required = true) @HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
        @PathParam("serviceName") final String serviceName, @PathParam("serviceVersion") final String serviceVersion,
        @PathParam("resourceInstanceName") final String resourceInstanceName, @PathParam("artifactName") final String artifactName) {
        String requestURI = request.getRequestURI();
        Wrapper<Response> responseWrapper = validateInstanceIdHeader(new Wrapper<>(), instanceIdHeader, requestURI);
        if (!responseWrapper.isEmpty()) {
            return responseWrapper.getInnerElement();
        }
        try {
            byte[] downloadRsrcArtifactEither = artifactsBusinessLogic
                .downloadRsrcInstArtifactByNames(serviceName, serviceVersion, resourceInstanceName, artifactName);
            byte[] value = downloadRsrcArtifactEither;
            // Returning 64-encoded as it was received during upload
            InputStream is = new ByteArrayInputStream(value);
            Map<String, String> headers = new HashMap<>();
            headers.put(Constants.CONTENT_DISPOSITION_HEADER, getContentDispositionValue(artifactName));
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
            getComponentsUtils().auditDistributionDownload(responseFormat, new DistributionData(instanceIdHeader, requestURI));
            return buildOkResponse(responseFormat, is, headers);
        } catch (ComponentException e) {
            getComponentsUtils().auditDistributionDownload(e.getResponseFormat(), new DistributionData(instanceIdHeader, requestURI));
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("download interface artifact for resource - external API");
            log.debug(DOWNLOAD_ARTIFACT_FAILED_WITH_EXCEPTION, e);
            return buildErrorResponse(e.getResponseFormat());
        }
    }
}
