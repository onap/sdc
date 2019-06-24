package org.openecomp.sdc.be.servlets;

import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
import org.codehaus.jackson.map.ObjectMapper;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.impl.utils.NodeFilterConstraintAction;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.utils.ConstraintConvertor;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.tosca.utils.NodeFilterConverter;
import org.openecomp.sdc.be.ui.model.UIConstraint;
import org.openecomp.sdc.be.ui.model.UINodeFilter;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/v1/catalog/services/{serviceId}/resourceInstances/{resourceInstanceId}/nodeFilter")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Service Filter", description = "Service Filter Servlet")
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    @ApiOperation(value = "Add Service Filter Constraint", httpMethod = "POST", notes = "Add Service Filter Constraint",
            response = Response.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Create Service Filter"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content")})
    public Response addServiceFilterConstraint(@ApiParam(value = "Service data", required = true) String data,
            @ApiParam(value = "Service Id") @PathParam("serviceId") String serviceId,
            @ApiParam(value = "Resource Instance Id") @PathParam("resourceInstanceId") String ciId,
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
            ServiceBusinessLogic businessLogic = getServiceBL(context);

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
            actionResponse = businessLogic
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
    @ApiOperation(value = "Update Service Filter Constraint", httpMethod = "PUT",
            notes = "Update Service Filter Constraint", response = Response.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Create Service Filter"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content")})
    public Response updateServiceFilterConstraint(@ApiParam(value = "Service data", required = true) String data,
            @ApiParam(value = "Service Id") @PathParam("serviceId") String serviceId,
            @ApiParam(value = "Resource Instance Id") @PathParam("resourceInstanceId") String ciId,
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
            ServiceBusinessLogic businessLogic = getServiceBL(context);

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
            actionResponse = businessLogic.updateServiceFilter(serviceIdLower, ciId, constraints, modifier, true);

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
    @ApiOperation(value = "Delete Service Filter Constraint", httpMethod = "Delete",
            notes = "Delete Service Filter Constraint", response = Response.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Delete Service Filter Constraint"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content")})
    public Response deleteServiceFilterConstraint(
            @ApiParam(value = "Service Id") @PathParam("serviceId") String serviceId,
            @ApiParam(value = "Resource Instance Id") @PathParam("resourceInstanceId") String ciId,
            @ApiParam(value = "Constraint Index") @PathParam("constraintIndex") int index,
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
            ServiceBusinessLogic businessLogic = getServiceBL(context);

            Either<CINodeFilterDataDefinition, ResponseFormat> actionResponse;
            actionResponse = businessLogic
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
