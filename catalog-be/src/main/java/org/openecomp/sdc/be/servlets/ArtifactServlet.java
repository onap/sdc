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

package org.openecomp.sdc.be.servlets;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ArtifactUiDownloadData;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * Root resource (exposed at "/" path)
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@OpenAPIDefinition(info = @Info(title = "Resource Artifact Servlet", description = "Resource Artifact Servlet"))
@Singleton
public class ArtifactServlet extends BeGenericServlet {

    private final ArtifactsBusinessLogic artifactsBusinessLogic;

    @Inject
    public ArtifactServlet(UserBusinessLogic userBusinessLogic,
        ComponentsUtils componentsUtils, ArtifactsBusinessLogic artifactsBusinessLogic) {
        super(userBusinessLogic, componentsUtils);
        this.artifactsBusinessLogic = artifactsBusinessLogic;
    }

    private static final Logger log = Logger.getLogger(ArtifactServlet.class);

    // *************** Resources
    @POST
    @Path("/resources/{resourceId}/artifacts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "Create Artifact", method = "POST", summary = "Returns created ArtifactDefinition",
            responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Resource created"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
            @ApiResponse(responseCode = "409", description = "Artifact already exist")})
    public Response loadArtifact(@PathParam("resourceId") final String resourceId,
            @Parameter(description = "json describe the artifact", required = true) String data,
            @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        try {
            return handleUploadRequest(data, request, resourceId, ComponentTypeEnum.RESOURCE);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("loadArtifact");
            log.debug("loadArtifact unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @POST
    @Path("/resources/{resourceId}/artifacts/{artifactId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "Update Artifact", method = "POST", summary = "Returns updated artifact",
            responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Resource created"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    public Response updateArtifact(@PathParam("resourceId") final String resourceId,
            @PathParam("artifactId") final String artifactId,
            @Parameter(description = "json describe the artifact", required = true) String data,
            @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}" , url);
        try {
            return handleUpdateRequest(data, request, resourceId, artifactId, ComponentTypeEnum.RESOURCE);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("updateArtifact");
            log.debug("updateArtifact unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @DELETE
    @Path("/resources/{resourceId}/artifacts/{artifactId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "Delete Artifact", method = "DELETE",
            summary = "Returns delete artifact", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Resource created"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    public Response deleteArtifact(@PathParam("resourceId") final String resourceId,
            @PathParam("artifactId") final String artifactId, @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}" , url);
        try {
            return handleDeleteRequest(request, resourceId, artifactId, ComponentTypeEnum.RESOURCE, null, null);
        } catch (Exception e) {
            log.debug("deleteArtifact unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    // *************** Services
    @POST
    @Path("/services/{serviceId}/artifacts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "Create Artifact", method = "POST",
            summary = "Returns created ArtifactDefinition", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Resource created"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
            @ApiResponse(responseCode = "409", description = "Artifact already exist")})
    public Response loadInformationArtifact(@PathParam("serviceId") final String serviceId,
            @Parameter(description = "json describe the artifact", required = true) String data,
            @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        try {
            return handleUploadRequest(data, request, serviceId, ComponentTypeEnum.SERVICE);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("loadInformationArtifact");
            log.debug("loadInformationArtifact unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @POST
    @Path("/services/{serviceId}/artifacts/{artifactId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "Update Artifact", method = "POST",
            summary = "Returns updated artifact", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Service artifact created"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    public Response updateInformationArtifact(@PathParam("serviceId") final String serviceId,
            @PathParam("artifactId") final String artifactId,
            @Parameter(description = "json describe the artifact", required = true) String data,
            @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        try {
            return handleUpdateRequest(data, request, serviceId, artifactId, ComponentTypeEnum.SERVICE);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("updateInformationArtifact");
            log.debug("updateInformationArtifact unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    // *************** Services api artifacts
    @POST
    @Path("/services/{serviceId}/artifacts/api/{artifactId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "Update Api Artifact", method = "POST",
            summary = "Returns created ArtifactDefinition", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Api Artifact Updated"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    public Response updateApiArtifact(@PathParam("serviceId") final String serviceId,
            @PathParam("artifactId") final String artifactId,
            @Parameter(description = "json describe the artifact", required = true) String data,
            @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @HeaderParam(value = Constants.MD5_HEADER) String origMd5) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        try {
            return handleUpdateRequest(data, request, serviceId, artifactId, ComponentTypeEnum.SERVICE);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("updateApiArtifact");
            log.debug("updateApiArtifact unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @DELETE
    @Path("/services/{serviceId}/artifacts/api/{artifactId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "Delete Api Artifact", method = "DELETE",
            summary = "Returns Deleted ArtifactDefinition", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Api Artifact deleted"),
            @ApiResponse(responseCode = "403", description = "Restricted operation")})
    public Response deleteApiArtifact(@PathParam("serviceId") final String serviceId,
            @PathParam("artifactId") final String artifactId, @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @HeaderParam(value = Constants.MD5_HEADER) String origMd5) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        try {
            return handleDeleteRequest(request, serviceId, artifactId, ComponentTypeEnum.SERVICE, null, null);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("deleteApiArtifact");
            log.debug("deleteApiArtifact unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @DELETE
    @Path("/services/{serviceId}/artifacts/{artifactId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "Delete Artifact", method = "DELETE",
            summary = "Returns delete artifact", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Service artifact deleted"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    public Response deleteInformationalArtifact(@PathParam("serviceId") final String serviceId,
            @PathParam("artifactId") final String artifactId, @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        try {
            return handleDeleteRequest(request, serviceId, artifactId, ComponentTypeEnum.SERVICE, null, null);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("deleteInformationalArtifact");
            log.debug("deleteInformationalArtifact unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    /*
     * DOWNLOAD Artifacts by json body in base 64 (because of userId problem with href)
     */

    @GET
    @Path("/services/{serviceId}/artifacts/{artifactId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "Download service Artifact in Base64", method = "GET",
            summary = "Returns downloaded artifact", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Service artifact downloaded"),
            @ApiResponse(responseCode = "404", description = "Service/Artifact not found")})
    public Response downloadServiceArtifactBase64(@PathParam("serviceId") final String serviceId,
            @PathParam("artifactId") final String artifactId, @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        try {
            return handleDownloadRequest(request, serviceId, artifactId, null, ComponentTypeEnum.SERVICE, null);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("downloadServiceArtifactBase64");
            log.debug("downloadServiceArtifactBase64 unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @GET
    @Path("/resources/{resourceId}/artifacts/{artifactId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "Download resource Artifact in Base64", method = "GET",
            summary = "Returns downloaded artifact", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Resource artifact downloaded"),
            @ApiResponse(responseCode = "404", description = "Resource/Artifact not found")})
    public Response downloadResourceArtifactBase64(@PathParam("resourceId") final String resourceId,
            @PathParam("artifactId") final String artifactId, @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        try {
            return handleDownloadRequest(request, resourceId, artifactId, null, ComponentTypeEnum.RESOURCE, null);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("downloadResourceArtifactBase64");
            log.debug("downloadResourceArtifactBase64 unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @GET
    @Path("/{containerComponentType}/{componentId}/resourceInstances/{componentInstanceId}/artifacts/{artifactId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "Download component Artifact in Base64", method = "GET",
            summary = "Returns downloaded artifact", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "ResourceInstance artifact downloaded"),
            @ApiResponse(responseCode = "404", description = "ResourceInstance/Artifact not found")})
    public Response downloadResourceInstanceArtifactBase64(@Parameter(
            description = "valid values: resources / services",
            schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME ,
                     ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
            @PathParam("componentId") final String componentId,
            @PathParam("componentInstanceId") final String componentInstanceId,
            @PathParam("artifactId") final String artifactId, @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        try {
            return handleDownloadRequest(request, componentInstanceId, artifactId, componentId,
                    ComponentTypeEnum.RESOURCE_INSTANCE, containerComponentType);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("downloadResourceInstanceArtifactBase64");
            log.debug("downloadResourceInstanceArtifactBase64 unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    // *************** Resource lifecycle ( interfces )

    @POST
    @Path("/resources/{resourceId}/{interfaceType}/{operation}/artifacts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "Create Artifact and Attach to interface", method = "POST",
            summary = "Returns created resource", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Resource created"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
            @ApiResponse(responseCode = "409", description = "Artifact already exist")})
    public Response loadArtifactToInterface(@PathParam("resourceId") final String resourceId,
            @PathParam("interfaceType") final String interfaceType, @PathParam("operation") final String operation,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @HeaderParam(value = Constants.MD5_HEADER) String origMd5,
            @Parameter(description = "json describe the artifact", required = true) String data,
            @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        try {
            return handleArtifactRequest(data, request, resourceId, interfaceType, operation, null,
                    ComponentTypeEnum.RESOURCE, ArtifactOperationEnum.CREATE, null, null);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("loadArtifactToInterface");
            log.debug("loadArtifactToInterface unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

    }

    @DELETE
    @Path("/resources/{resourceId}/{interfaceType}/{operation}/artifacts/{artifactId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "delete Artifact from interface", method = "delete",
            summary = "delete matching artifact from interface", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "delete artifact under interface deleted"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
            @ApiResponse(responseCode = "409", description = "Artifact already exist")})
    public Response deleteArtifactToInterface(@PathParam("resourceId") final String resourceId,
            @PathParam("interfaceType") final String interfaceType, @PathParam("operation") final String operation,
            @PathParam("artifactId") final String artifactId, @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        try {
            return handleDeleteRequest(request, resourceId, artifactId, ComponentTypeEnum.RESOURCE, interfaceType,
                    operation);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("deleteArtifactToInterface");
            log.debug("deleteArtifactToInterface unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @POST
    @Path("/resources/{resourceId}/{interfaceType}/{operation}/artifacts/{artifactId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "update Artifact  Attach to interface", method = "post",
            summary = "updates artifact by interface", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "delete artifact under interface deleted"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
            @ApiResponse(responseCode = "409", description = "Artifact already exist")})
    public Response updateArtifactToInterface(@PathParam("resourceId") final String resourceId,
            @PathParam("interfaceType") final String interfaceType, @PathParam("operation") final String operation,
            @PathParam("artifactId") final String artifactId,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @HeaderParam(value = Constants.MD5_HEADER) String origMd5, @Context final HttpServletRequest request,
            @Parameter(description = "json describe the artifact", required = true) String data) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        try {
            return handleArtifactRequest(data, request, resourceId, interfaceType, operation, artifactId,
                    ComponentTypeEnum.RESOURCE, ArtifactOperationEnum.UPDATE, null, null);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("updateArtifactToInterface");
            log.debug("updateArtifactToInterface unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/artifacts/{artifactId}/heatParams")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "Update Resource Instance HEAT_ENV parameters",
            method = "POST", summary = "Returns updated artifact", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Artifact updated"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    public Response updateRIArtifact(@Parameter(description = "valid values: resources / services",
            schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME ,
                     ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
            @PathParam("componentId") final String componentId,
            @PathParam("componentInstanceId") final String componentInstanceId,
            @PathParam("artifactId") final String artifactId,
            @Parameter(description = "json describe the artifact", required = true) String data,
            @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        try {
            return handleArtifactRequest(data, request, componentInstanceId, null, null, artifactId,
                    ComponentTypeEnum.RESOURCE_INSTANCE, ArtifactOperationEnum.UPDATE, componentId,
                    containerComponentType);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("updateRIArtifact");
            log.debug("updateRIArtifact unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/artifacts/{artifactId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "Update Resource Instance artifact payload", method = "POST",
            summary = "Returns updated artifact", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Artifact updated"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    public Response updateComponentInstanceArtifact(@HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @HeaderParam(value = Constants.MD5_HEADER) String origMd5,
            @Parameter(description = "valid values: resources / services",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME ,
                             ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
            @PathParam("componentId") final String componentId,
            @PathParam("componentInstanceId") final String componentInstanceId,
            @PathParam("artifactId") final String artifactId,
            @Parameter(description = "json describe the artifact", required = true) String data,
            @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        try {
            return handleArtifactRequest(data, request, componentInstanceId, null, null, artifactId,
                    ComponentTypeEnum.RESOURCE_INSTANCE, ArtifactOperationEnum.UPDATE, componentId,
                    containerComponentType);
        } catch (Exception e) {
            log.debug("loadResourceInstanceHeatEnvArtifact unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/artifacts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "Load Resource Instance artifact payload", method = "POST",
            summary = "Returns updated artifact", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Artifact updated"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    public Response loadComponentInstanceArtifact(@HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @HeaderParam(value = Constants.MD5_HEADER) String origMd5,
            @Parameter(description = "valid values: resources / services",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME ,
                             ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
            @PathParam("componentId") final String componentId,
            @PathParam("componentInstanceId") final String componentInstanceId,
            @Parameter(description = "json describe the artifact", required = true) String data,
            @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        try {
            return handleArtifactRequest(data, request, componentInstanceId, null, null, null,
                    ComponentTypeEnum.RESOURCE_INSTANCE, ArtifactOperationEnum.CREATE, componentId,
                    containerComponentType);
        } catch (Exception e) {
            log.debug("loadResourceInstanceHeatEnvArtifact unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @DELETE
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/artifacts/{artifactId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "Delete Resource Instance artifact", method = "POST",
            summary = "Returns deleted artifact", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Artifact updated"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    public Response deleteComponentInstanceArtifact(@HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @HeaderParam(value = Constants.MD5_HEADER) String origMd5,
            @Parameter(description = "valid values: resources / services",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME ,
                             ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
            @PathParam("componentId") final String componentId,
            @PathParam("componentInstanceId") final String componentInstanceId,
            @PathParam("artifactId") final String artifactId,
            @Parameter(description = "json describe the artifact", required = true) String data,
            @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        try {
            return handleDeleteRequest(request, componentInstanceId, artifactId, ComponentTypeEnum.RESOURCE_INSTANCE,
                    null, null, componentId);
        } catch (Exception e) {
            log.debug("deleteArtifact unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }


    @GET
    @Path("/{containerComponentType}/{componentId}/artifactsByType/{artifactGroupType}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "Get component Artifacts", method = "GET",
            summary = "Returns artifacts", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Component artifacts"),
            @ApiResponse(responseCode = "404", description = "Resource/Artifact not found")})
    public Response getComponentArtifacts(@Parameter(description = "valid values: resources / services",
            schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME ,
                     ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
            @PathParam("componentId") final String componentId,
            @PathParam("artifactGroupType") final String artifactGroupType, @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        try {
            return handleGetArtifactsRequest(request, componentId, null, artifactGroupType, containerComponentType);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("downloadResourceInstanceArtifactBase64");
            log.debug("downloadResourceInstanceArtifactBase64 unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @GET
    @Path("/{containerComponentType}/{componentId}/resourceInstances/{componentInstanceId}/artifactsByType/{artifactGroupType}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "Get component Artifacts", method = "GET",
            summary = "Returns artifacts", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Component artifacts"),
            @ApiResponse(responseCode = "404", description = "Resource/Artifact not found")})
    public Response getComponentInstanceArtifacts(@Parameter(description = "valid values: resources / services",
            schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME ,
                     ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
            @PathParam("componentId") final String componentId,
            @PathParam("componentInstanceId") final String componentInstanceId,
            @PathParam("artifactGroupType") final String artifactGroupType, @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        try {
            return handleGetArtifactsRequest(request, componentInstanceId, componentId, artifactGroupType,
                    containerComponentType);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("downloadResourceInstanceArtifactBase64");
            log.debug("downloadResourceInstanceArtifactBase64 unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }


    @POST
    @Path("/{assetType}/{uuid}/interfaces/{interfaceUUID}/operations/{operationUUID}/artifacts/{artifactUUID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "uploads of artifact to component operation workflow", method = "POST", summary = "uploads of artifact to component operation workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Artifact uploaded",content = @Content(array = @ArraySchema(schema = @Schema(implementation = ArtifactDefinition.class)))),
            @ApiResponse(responseCode = "404", description = "Specified resource is not found - SVC4063"),
            @ApiResponse(responseCode = "400", description = "Invalid artifactType was defined as input - SVC4122"),
            @ApiResponse(responseCode = "400", description = "Artifact type (mandatory field) is missing in request - SVC4124"),
            @ApiResponse(responseCode = "400", description = "Artifact name given in input already exists in the context of the asset - SVC4125"),
            @ApiResponse(responseCode = "400", description = "Artifact name is missing in input - SVC4128"),
            @ApiResponse(responseCode = "400", description = "Asset is being edited by different user. Only one user can checkout and edit an asset on given time. The asset will be available for checkout after the other user will checkin the asset - SVC4086"),
            @ApiResponse(responseCode = "400", description = "Restricted Operation – the user provided does not have role of Designer or the asset is being used by another designer - SVC4301")})
    //@ApiImplicitParams({@ApiImplicitParam(required = true, dataType = "org.openecomp.sdc.be.model.ArtifactDefinition", paramType = "body", value = "json describe the artifact")})
    public Response uploadInterfaceOperationArtifact(
            @Parameter(description = "Asset type") @PathParam("assetType") String assetType,
            @Parameter(description = "The uuid of the asset as published in the metadata", required = true)@PathParam("uuid") final String uuid,
            @Parameter(description = "The uuid of the interface", required = true)@PathParam("interfaceUUID") final String interfaceUUID,
            @Parameter(description = "The uuid of the operation", required = true)@PathParam("operationUUID") final String operationUUID,
            @Parameter(description = "The uuid of the artifact", required = true)@PathParam("artifactUUID") final String artifactUUID,
            @Parameter( hidden = true) String data,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @HeaderParam(value = Constants.MD5_HEADER) String origMd5,
            @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}" , url);

        try {
            Either<ArtifactDefinition, ResponseFormat> uploadArtifactEither =
                artifactsBusinessLogic.updateArtifactOnInterfaceOperationByResourceUUID(data, request,
                            ComponentTypeEnum.findByParamName(assetType), uuid, interfaceUUID, operationUUID, artifactUUID,
                            new ResourceCommonInfo(assetType), artifactsBusinessLogic.new ArtifactOperationInfo(true,
                                    false, ArtifactOperationEnum.UPDATE));
            if (uploadArtifactEither.isRight()) {
                log.debug("failed to update artifact");
                return buildErrorResponse(uploadArtifactEither.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), uploadArtifactEither.left().value());
        }
        catch (Exception e) {
            final String message = "failed to update artifact on a resource or service";
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
            log.debug(message, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    // ////////// API END ///////////////////////////

    // ************ private *********************

    private Response handleUploadRequest(String data, HttpServletRequest request, String componentId, ComponentTypeEnum componentType) {
        return handleArtifactRequest(data, componentId, null, componentType, ArtifactOperationEnum.CREATE);
    }

    private Response handleUpdateRequest(String data, HttpServletRequest request, String componentId, String artifactId, ComponentTypeEnum componentType) {
        return handleArtifactRequest(data, componentId, artifactId, componentType, ArtifactOperationEnum.UPDATE);
    }

    private Response handleDownloadRequest(HttpServletRequest request, String componentId, String artifactId, String parentId, ComponentTypeEnum componentType, String containerComponentType) {
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        ServletContext context = request.getSession().getServletContext();
        Either<ImmutablePair<String, byte[]>, ResponseFormat> actionResult = artifactsBusinessLogic
            .handleDownloadRequestById(componentId, artifactId, userId, componentType, parentId, containerComponentType);

        Response response;
        if (actionResult.isRight()) {
            response = buildErrorResponse(actionResult.right().value());
        } else {
            byte[] file = actionResult.left().value().getRight();
            String base64Contents = new String(Base64.encodeBase64(file));
            String artifactName = actionResult.left().value().getLeft();
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
            ArtifactUiDownloadData artifactUiDownloadData = new ArtifactUiDownloadData();
            artifactUiDownloadData.setArtifactName(artifactName);
            artifactUiDownloadData.setBase64Contents(base64Contents);
            response = buildOkResponse(responseFormat, artifactUiDownloadData);
        }
        return response;
    }

    private Response handleGetArtifactsRequest(HttpServletRequest request, String componentId, String parentId, String artifactGroupType, String containerComponentType) {
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        ComponentTypeEnum componentTypeEnum  = parentId == null || parentId.isEmpty()? ComponentTypeEnum.findByParamName(containerComponentType): ComponentTypeEnum.RESOURCE_INSTANCE;
        Either<Map<String, ArtifactDefinition>, ResponseFormat> actionResult = artifactsBusinessLogic.handleGetArtifactsByType(containerComponentType, parentId, componentTypeEnum, componentId, artifactGroupType, userId);

        Response response;
        if (actionResult.isRight()) {
            response = buildErrorResponse(actionResult.right().value());
        } else {

            response =  buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResult.left().value());
        }

        return response;
    }


    private Response handleDeleteRequest(HttpServletRequest request, String componentId, String artifactId, ComponentTypeEnum componentType, String interfaceType, String operationName) {
        return handleDeleteRequest(request, componentId, artifactId, componentType, interfaceType, operationName, null);
    }

    private Response handleDeleteRequest(HttpServletRequest request, String componentId, String artifactId, ComponentTypeEnum componentType, String interfaceType, String operationName, String parentId) {
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        ServletContext context = request.getSession().getServletContext();
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> actionResult = artifactsBusinessLogic.handleArtifactRequest(componentId, userId, componentType, artifactsBusinessLogic.new ArtifactOperationInfo (false, false, ArtifactOperationEnum.DELETE), artifactId, null, null, null, interfaceType, operationName,
                parentId, null);
        Response response;
        if (actionResult.isRight()) {
            response = buildErrorResponse(actionResult.right().value());
        } else {
            Either<ArtifactDefinition, Operation> result = actionResult.left().value();
            if (result.isLeft()) {
                response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result.left().value());
            } else {
                response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result.right().value());
            }
        }
        return response;

    }

    private Response handleArtifactRequest(String data, HttpServletRequest request, String componentId, String interfaceName, String operationName, String artifactId, ComponentTypeEnum componentType, ArtifactOperationEnum operationEnum, String parentId,
            String containerComponentType) {
        ArtifactDefinition artifactInfo = RepresentationUtils.convertJsonToArtifactDefinition(data, ArtifactDefinition.class);
        String origMd5 = request.getHeader(Constants.MD5_HEADER);

        String userId = request.getHeader(Constants.USER_ID_HEADER);

        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> actionResult = artifactsBusinessLogic.handleArtifactRequest(componentId, userId, componentType,
            artifactsBusinessLogic.new ArtifactOperationInfo (false, false,operationEnum), artifactId, artifactInfo, origMd5, data, interfaceName, operationName, parentId,
                containerComponentType);
        Response response;
        if (actionResult.isRight()) {
            response = buildErrorResponse(actionResult.right().value());
        } else {
            Either<ArtifactDefinition, Operation> result = actionResult.left().value();
            if (result.isLeft()) {
                response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result.left().value());
            } else {
                response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result.right().value());
            }
        }
        return response;

    }

    private Response handleArtifactRequest(String data, String componentId, String artifactId, ComponentTypeEnum componentType, ArtifactOperationEnum operation) {
        return handleArtifactRequest(data, servletRequest, componentId, null, null, artifactId, componentType, operation, null, null);
    }

}
