/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import fj.data.Either;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.codehaus.jackson.map.ObjectMapper;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.components.impl.utils.NodeFilterConstraintAction;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.utils.ConstraintConvertor;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.tosca.utils.NodeFilterConverter;
import org.openecomp.sdc.be.ui.model.UIConstraint;
import org.openecomp.sdc.be.ui.model.UINodeFilter;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/v1/catalog/services/{serviceId}/resourceInstances/{resourceInstanceId}/nodeFilter")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(info = @Info(title = "Service Filter", description = "Service Filter Servlet"))
@Singleton
public class ServiceFilterServlet extends AbstractValidationsServlet {

    private static final Logger log = LoggerFactory.getLogger(ServiceFilterServlet.class);
    private static final String START_HANDLE_REQUEST_OF = "Start handle request of {}";
    private static final String MODIFIER_ID_IS = "modifier id is {}";
    private static final String FAILED_TO_UPDATE_OR_CREATE_NODE_FILTER = "failed to update or create node filter";
    private static final String FAILED_TO_PARSE_SERVICE = "failed to parse service";
    private static final String NODE_FILTER_CREATION_OR_UPDATE = "Node Filter Creation or update";
    private static final String CREATE_OR_UPDATE_NODE_FILTER_WITH_AN_ERROR =
            "create or update node filter with an error";
    private final ServiceBusinessLogic serviceBusinessLogic;

    @Inject
    public ServiceFilterServlet(UserBusinessLogic userBusinessLogic,
        ComponentInstanceBusinessLogic componentInstanceBL,
        ComponentsUtils componentsUtils, ServletUtils servletUtils,
        ResourceImportManager resourceImportManager,
        ServiceBusinessLogic serviceBusinessLogic) {
        super(userBusinessLogic, componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.serviceBusinessLogic = serviceBusinessLogic;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    @Operation(description = "Add Service Filter Constraint", method = "POST", summary = "Add Service Filter Constraint",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Create Service Filter"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response addServiceFilterConstraint(@Parameter(description = "Service data", required = true) String data,
            @Parameter(description = "Service Id") @PathParam("serviceId") String serviceId,
            @Parameter(description = "Resource Instance Id") @PathParam("resourceInstanceId") String ciId,
            @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        final HttpSession session = request.getSession();
        ServletContext context = session.getServletContext();
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug(MODIFIER_ID_IS, userId);

        Response response;

        try {
            String serviceIdLower = serviceId.toLowerCase();

            Either<UIConstraint, ResponseFormat> convertResponse = parseToConstraint(data, modifier);
            if (convertResponse.isRight()) {
                log.debug(FAILED_TO_PARSE_SERVICE);
                response = buildErrorResponse(convertResponse.right().value());
                return response;
            }
            UIConstraint uiConstraint = convertResponse.left().value();
            if (uiConstraint == null) {
                log.debug(FAILED_TO_PARSE_SERVICE);
                response = buildErrorResponse(convertResponse.right().value());
                return response;
            }
            Either<CINodeFilterDataDefinition, ResponseFormat> actionResponse;
            String constraint = new ConstraintConvertor().convert(uiConstraint);
            actionResponse = serviceBusinessLogic
                                     .addOrDeleteServiceFilter(serviceIdLower, ciId, NodeFilterConstraintAction.ADD, uiConstraint.getServicePropertyName(),
                                             constraint, -1, modifier, true);

            if (actionResponse.isRight()) {
                log.debug(FAILED_TO_UPDATE_OR_CREATE_NODE_FILTER);
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }

            CINodeFilterDataDefinition value = actionResponse.left().value();
            UINodeFilter nodeFilter = new NodeFilterConverter().convertToUi(value);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), nodeFilter);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(NODE_FILTER_CREATION_OR_UPDATE);
            log.debug(CREATE_OR_UPDATE_NODE_FILTER_WITH_AN_ERROR, e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    @Operation(description = "Update Service Filter Constraint", method = "PUT",
            summary = "Update Service Filter Constraint", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Create Service Filter"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateServiceFilterConstraint(@Parameter(description = "Service data", required = true) String data,
            @Parameter(description = "Service Id") @PathParam("serviceId") String serviceId,
            @Parameter(description = "Resource Instance Id") @PathParam("resourceInstanceId") String ciId,
            @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);

        User modifier = new User();
        modifier.setUserId(userId);
        log.debug(MODIFIER_ID_IS, userId);

        Response response;

        try {
            String serviceIdLower = serviceId.toLowerCase();

            Either<List, ResponseFormat> convertResponse = parseToConstraints(data, modifier);
            if (convertResponse.isRight()) {
                log.debug(FAILED_TO_PARSE_SERVICE);
                response = buildErrorResponse(convertResponse.right().value());
                return response;
            }
            List<Map<String,String>> uiConstraintsMaps = (List<Map<String,String>>) convertResponse.left().value();
            if (uiConstraintsMaps == null) {
                log.debug("failed to parse data");
                response = buildErrorResponse(convertResponse.right().value());
                return response;
            }
            final ObjectMapper objectMapper = new ObjectMapper();
            List<UIConstraint> uiConstraints = uiConstraintsMaps.stream().map(dataMap -> objectMapper.convertValue(dataMap, UIConstraint.class)).collect(
                    Collectors.toList());
            if (uiConstraints == null) {
                log.debug("failed to parse data");
                response = buildErrorResponse(convertResponse.right().value());
                return response;
            }
            Either<CINodeFilterDataDefinition, ResponseFormat> actionResponse;
            List<String> constraints = new ConstraintConvertor().convertToList(uiConstraints);
            actionResponse = serviceBusinessLogic.updateServiceFilter(serviceIdLower, ciId, constraints, modifier, true);

            if (actionResponse.isRight()) {
                log.debug(FAILED_TO_UPDATE_OR_CREATE_NODE_FILTER);
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }

            CINodeFilterDataDefinition value = actionResponse.left().value();
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
                    new NodeFilterConverter().convertToUi(value));

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(NODE_FILTER_CREATION_OR_UPDATE);
            log.debug(CREATE_OR_UPDATE_NODE_FILTER_WITH_AN_ERROR, e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{constraintIndex}")
    @Operation(description = "Delete Service Filter Constraint", method = "Delete",
            summary = "Delete Service Filter Constraint", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Delete Service Filter Constraint"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteServiceFilterConstraint(
            @Parameter(description = "Service Id") @PathParam("serviceId") String serviceId,
            @Parameter(description = "Resource Instance Id") @PathParam("resourceInstanceId") String ciId,
            @Parameter(description = "Constraint Index") @PathParam("constraintIndex") int index,
            @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);

        User modifier = new User();
        modifier.setUserId(userId);
        log.debug(MODIFIER_ID_IS, userId);

        Response response;

        try {
            String serviceIdLower = serviceId.toLowerCase();

            Either<CINodeFilterDataDefinition, ResponseFormat> actionResponse;
            actionResponse = serviceBusinessLogic
                                     .addOrDeleteServiceFilter(serviceIdLower, ciId, NodeFilterConstraintAction.DELETE,
                                             null, null,  index, modifier, true);

            if (actionResponse.isRight()) {

                log.debug(FAILED_TO_UPDATE_OR_CREATE_NODE_FILTER);
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }

            final CINodeFilterDataDefinition value = actionResponse.left().value();
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
                    new NodeFilterConverter().convertToUi(value));

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(NODE_FILTER_CREATION_OR_UPDATE);
            log.debug(CREATE_OR_UPDATE_NODE_FILTER_WITH_AN_ERROR, e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }
    }

    private Either<UIConstraint, ResponseFormat> parseToConstraint(String serviceJson, User user) {
        return getComponentsUtils().convertJsonToObjectUsingObjectMapper(serviceJson, user, UIConstraint.class,
                AuditingActionEnum.CREATE_RESOURCE, ComponentTypeEnum.SERVICE);
    }

    private Either<List, ResponseFormat> parseToConstraints(String serviceJson, User user) {
        return getComponentsUtils().convertJsonToObjectUsingObjectMapper(serviceJson, user, List.class,
                AuditingActionEnum.CREATE_RESOURCE, ComponentTypeEnum.SERVICE);
    }
}
