package org.openecomp.sdc.be.servlets;


import com.google.common.collect.Sets;
import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.annotations.*;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentFieldsEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.ui.model.UiServiceDataTransfer;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Set;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog/services/{serviceId}/paths")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Service Forwarding Path", description = "Service Forwarding Path Servlet")
@Singleton
public class ServiceForwardingPathServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(ServiceForwardingPathServlet.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    @ApiOperation(value = "Create Forwarding Path", httpMethod = "POST", notes = "Create Forwarding Path", response = Service.class)
    @ApiResponses(value =
            {@ApiResponse(code = 201, message = "Create Forwarding Path"),
                    @ApiResponse(code = 403, message = "Restricted operation"),
                    @ApiResponse(code = 400, message = "Invalid content / Missing content"),
                    @ApiResponse(code = 409, message = "Forwarding Path already exist")})
    public Response createForwardingPath(
            @ApiParam(value = "Forwarding Path to create", required = true) String data,
            @ApiParam(value = "Service Id") @PathParam("serviceId") String serviceId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return createOrUpdate(data, serviceId, request, userId, false);
    }



    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    @ApiOperation(value = "Update Forwarding Path", httpMethod = "PUT", notes = "Update Forwarding Path", response = Service.class)
    @ApiResponses(value =
            {@ApiResponse(code = 201, message = "Update Forwarding Path"),
                    @ApiResponse(code = 403, message = "Restricted operation"),
                    @ApiResponse(code = 400, message = "Invalid content / Missing content"),
                    @ApiResponse(code = 409, message = "Forwarding Path already exist")})
    public Response updateForwardingPath(
            @ApiParam(value = "Update Path to create", required = true) String data,
            @ApiParam(value = "Service Id") @PathParam("serviceId") String serviceId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return createOrUpdate(data, serviceId, request, userId, true);
    }

    private Response createOrUpdate( String data, String serviceId, HttpServletRequest request, String userId, boolean isUpdate) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);

        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);

        Response response;

        try {
            String serviceIdLower = serviceId.toLowerCase();
            ServiceBusinessLogic businessLogic = getServiceBL(context);

            Either<Service, ResponseFormat> convertResponse = parseToService(data, modifier);
            if (convertResponse.isRight()) {
                log.debug("failed to parse service");
                response = buildErrorResponse(convertResponse.right().value());
                return response;
            }
            Service updatedService = convertResponse.left().value();
            Either<Service, ResponseFormat> actionResponse ;
            if (isUpdate) {
                actionResponse = businessLogic.updateForwardingPath(serviceIdLower, updatedService, modifier, true);
            } else {
                actionResponse = businessLogic.createForwardingPath(serviceIdLower, updatedService, modifier, true);
            }

            if (actionResponse.isRight()) {
                log.debug("failed to update or create paths");
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }

            Service service = actionResponse.left().value();
            Object result = RepresentationUtils.toRepresentation(service);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Forward Path Creation or update");
            log.debug("create or update forwarding path with an error", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{forwardingPathId}")
    @ApiOperation(value = "Get Forwarding Path", httpMethod = "GET", notes = "GET Forwarding Path", response = ForwardingPathDataDefinition.class)
    @ApiResponses(value =
            {@ApiResponse(code = 201, message = "Get Forwarding Path"),
                    @ApiResponse(code = 403, message = "Restricted operation"),
                    @ApiResponse(code = 400, message = "Invalid content / Missing content"),
                    @ApiResponse(code = 409, message = "Forwarding Path already exist")})
    public Response getForwardingPath(
            @ApiParam(value = "Forwarding Path to create", required = true) String datax,
            @ApiParam(value = "Service Id") @PathParam("serviceId") String serviceId,
            @ApiParam(value = "Forwarding Path Id") @PathParam("forwardingPathId") String forwardingPathId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);

        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);


        try {
            ServiceBusinessLogic businessLogic = getServiceBL(context);
            Either<UiComponentDataTransfer, ResponseFormat> serviceResponse = businessLogic.getComponentDataFilteredByParams(serviceId, modifier, Collections.singletonList(ComponentFieldsEnum.FORWARDING_PATHS.getValue()));
            if (serviceResponse.isRight()) {
                return buildErrorResponse(serviceResponse.right().value());
            }

            UiServiceDataTransfer uiServiceDataTransfer = (UiServiceDataTransfer) serviceResponse.left().value();

            ForwardingPathDataDefinition forwardingPathDataDefinition = new ForwardingPathDataDefinition();
            if (!MapUtils.isEmpty(uiServiceDataTransfer.getForwardingPaths())) {
                forwardingPathDataDefinition = uiServiceDataTransfer.getForwardingPaths().get(forwardingPathId);
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), RepresentationUtils.toRepresentation(forwardingPathDataDefinition));


        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Service Metadata");
            log.debug("update service metadata failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{forwardingPathId}")
    @ApiOperation(value = "Delete Forwarding Path", httpMethod = "DELETE", notes = "Delete Forwarding Path", response = Service.class)
    @ApiResponses(value =
            {@ApiResponse(code = 201, message = "Delete Forwarding Path"),
                    @ApiResponse(code = 403, message = "Restricted operation"),
                    @ApiResponse(code = 400, message = "Invalid content / Missing content"),
                    @ApiResponse(code = 409, message = "Forwarding Path already exist")})
    public Response deleteForwardingPath(
            @ApiParam(value = "Forwarding Path Id") @PathParam("forwardingPathId") String forwardingPathId,
            @ApiParam(value = "Service Id") @PathParam("serviceId") String serviceId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);

        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);

        Response response;

        try {
            String serviceIdLower = serviceId.toLowerCase();
            ServiceBusinessLogic businessLogic = getServiceBL(context);

            Either<Set<String>, ResponseFormat> actionResponse = businessLogic.deleteForwardingPaths(serviceIdLower, Sets.newHashSet(forwardingPathId), modifier, true);

            if (actionResponse.isRight()) {
                log.debug("failed to delete paths");
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }

            Set<String> deletedPaths = actionResponse.left().value();
            Object result = RepresentationUtils.toRepresentation(deletedPaths);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete forward paths");
            log.debug("Delete service paths with an error", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }
    }


    private Either<Service, ResponseFormat> parseToService(String serviceJson, User user) {
        return getComponentsUtils().convertJsonToObjectUsingObjectMapper(serviceJson, user, Service.class, AuditingActionEnum.CREATE_RESOURCE, ComponentTypeEnum.SERVICE);//TODO: change sSERVICE constant
    }
}
