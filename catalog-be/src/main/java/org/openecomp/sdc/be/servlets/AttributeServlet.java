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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.components.impl.AttributeBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

/**
 * Web Servlet for actions on Attributes
 *
 * @author mshitrit
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tags({@Tag(name = "SDCE-2 APIs")})
@Server(url = "/sdc2/rest")
@Controller
public class AttributeServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(AttributeServlet.class);
    private static final String ATTRIBUTE_CONTENT_IS_INVALID = "Attribute content is invalid - {}";

    @Inject
    public AttributeServlet(ComponentInstanceBusinessLogic componentInstanceBL, ComponentsUtils componentsUtils,
                            ServletUtils servletUtils, ResourceImportManager resourceImportManager) {
        super(componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
    }

    /**
     * Creates new Attribute on a resource with given resource ID
     *
     * @param resourceId
     * @param data
     * @param request
     * @param userId
     * @return
     */
    @POST
    @Path("resources/{resourceId}/attributes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create Resource Attribute", method = "POST", summary = "Returns created resource attribute", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Resource property created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Resource attribute already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createAttribute(
        @Parameter(description = "resource id to update with new attribute", required = true) @PathParam("resourceId") final String resourceId,
        @Parameter(description = "Resource attribute to be created", required = true) String data, @Context final HttpServletRequest request,
        @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {} modifier id is {} data is {}", url, userId, data);
        try {
            final Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
            AttributeDefinition attributeDataDefinition = convertJsonToObject(data, errorWrapper);
            if (errorWrapper.isEmpty()) {
                AttributeBusinessLogic businessLogic = getClassFromWebAppContext(context, () -> AttributeBusinessLogic.class);
                Either<AttributeDefinition, ResponseFormat> createAttribute = businessLogic
                    .createAttribute(resourceId, attributeDataDefinition, userId);
                if (createAttribute.isRight()) {
                    errorWrapper.setInnerElement(createAttribute.right().value());
                } else {
                    attributeDataDefinition = createAttribute.left().value();
                }
            }
            if (!errorWrapper.isEmpty()) {
                log.info("Failed to create Attribute. Reason - ", errorWrapper.getInnerElement());
                return buildErrorResponse(errorWrapper.getInnerElement());
            } else {
                log.debug("Attribute {} created successfully with id {}", attributeDataDefinition.getName(), attributeDataDefinition.getUniqueId());
                ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.CREATED);
                return buildOkResponse(responseFormat, RepresentationUtils.toRepresentation(attributeDataDefinition));
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create Attribute");
            log.debug("create property failed with exception", e);
            throw e;
        }
    }

    /**
     * Updates existing Attribute with given attributeID on a resource with given resourceID
     *
     * @param resourceId
     * @param attributeId
     * @param data
     * @param request
     * @param userId
     * @return
     */
    @PUT
    @Path("resources/{resourceId}/attributes/{attributeId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update Resource Attribute", method = "PUT", summary = "Returns updated attribute", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Resource attribute updated"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateAttribute(
        @Parameter(description = "resource id to update with new attribute", required = true) @PathParam("resourceId") final String resourceId,
        @Parameter(description = "attribute id to update", required = true) @PathParam("attributeId") final String attributeId,
        @Parameter(description = "Resource attribute to update", required = true) String data, @Context final HttpServletRequest request,
        @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);
        try {
            final Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
            AttributeDefinition attributeDataDefinition = convertJsonToObject(data, errorWrapper);
            if (errorWrapper.isEmpty()) {
                AttributeBusinessLogic businessLogic = getClassFromWebAppContext(context, () -> AttributeBusinessLogic.class);
                Either<AttributeDefinition, ResponseFormat> eitherUpdateAttribute = businessLogic
                    .updateAttribute(resourceId, attributeId, attributeDataDefinition, userId);
                if (eitherUpdateAttribute.isRight()) {
                    errorWrapper.setInnerElement(eitherUpdateAttribute.right().value());
                } else {
                    attributeDataDefinition = eitherUpdateAttribute.left().value();
                }
            }
            if (!errorWrapper.isEmpty()) {
                log.info("Failed to update Attribute. Reason - ", errorWrapper.getInnerElement());
                return buildErrorResponse(errorWrapper.getInnerElement());
            } else {
                log.debug("Attribute id {} updated successfully ", attributeDataDefinition.getUniqueId());
                ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
                return buildOkResponse(responseFormat, RepresentationUtils.toRepresentation(attributeDataDefinition));
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Attribute");
            log.debug("update attribute failed with exception", e);
            throw e;
        }
    }

    /**
     * Deletes existing Attribute with given attributeID on a resource with given resourceID
     *
     * @param resourceId
     * @param attributeId
     * @param request
     * @param userId
     * @return
     */
    @DELETE
    @Path("resources/{resourceId}/attributes/{attributeId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create Resource Attribute", method = "DELETE", summary = "Returns deleted attribute", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "204", description = "deleted attribute"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Resource property not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteAttribute(
        @Parameter(description = "resource id of attribute", required = true) @PathParam("resourceId") final String resourceId,
        @Parameter(description = "Attribute id to delete", required = true) @PathParam("attributeId") final String attributeId,
        @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        log.debug("modifier id is {}", userId);
        try {
            // delete the property
            AttributeBusinessLogic businessLogic = getClassFromWebAppContext(context, () -> AttributeBusinessLogic.class);
            Either<AttributeDefinition, ResponseFormat> eitherAttribute = businessLogic.deleteAttribute(resourceId, attributeId, userId);
            if (eitherAttribute.isRight()) {
                log.debug("Failed to delete Attribute. Reason - ", eitherAttribute.right().value());
                return buildErrorResponse(eitherAttribute.right().value());
            }
            AttributeDefinition attributeDefinition = eitherAttribute.left().value();
            String name = attributeDefinition.getName();
            log.debug("Attribute {} deleted successfully with id {}", name, attributeDefinition.getUniqueId());
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT);
            return buildOkResponse(responseFormat, RepresentationUtils.toRepresentation(attributeDefinition));
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Attribute");
            log.debug("delete attribute failed with exception", e);
            throw e;
        }
    }

    private AttributeDefinition convertJsonToObject(final String data, final Wrapper<ResponseFormat> errorWrapper) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(data, AttributeDefinition.class);
        } catch (final IOException e) {
            log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, ATTRIBUTE_CONTENT_IS_INVALID, data);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
            errorWrapper.setInnerElement(responseFormat);
            return null;
        }
    }
}
