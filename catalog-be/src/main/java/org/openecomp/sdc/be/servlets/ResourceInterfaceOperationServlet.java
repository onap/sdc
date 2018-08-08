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

package org.openecomp.sdc.be.servlets;

import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.components.impl.InterfaceOperationBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.utils.InterfaceUIDataConverter;
import org.openecomp.sdc.be.datatypes.elements.InterfaceOperationDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.ui.model.UiResourceDataTransfer;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog/resources/{resourceId}/interfaceOperations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Interface Operation", description = "Interface Operation Servlet")
@Singleton
public class ResourceInterfaceOperationServlet extends AbstractValidationsServlet {

  private static final Logger log = LoggerFactory.getLogger(ResourceInterfaceOperationServlet.class);

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/")
  @ApiOperation(value = "Create Interface Operation", httpMethod = "POST", notes = "Create Interface Operation", response = InterfaceOperationDataDefinition.class)
  @ApiResponses(value = {@ApiResponse(code = 201, message = "Create Interface Operation"),
      @ApiResponse(code = 403, message = "Restricted operation"),
      @ApiResponse(code = 400, message = "Invalid content / Missing content"),
      @ApiResponse(code = 409, message = "Interface Operation already exist")})
  public Response createInterfaceOperation(
      @ApiParam(value = "Interface Operation to create", required = true) String data,
      @ApiParam(value = "Resource Id") @PathParam("resourceId") String resourceId,
      @Context final HttpServletRequest request,
      @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
    return createOrUpdate(data, resourceId, request, userId, false);
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/")
  @ApiOperation(value = "Update Interface Operation", httpMethod = "PUT", notes = "Update Interface Operation", response = InterfaceOperationDataDefinition.class)
  @ApiResponses(value = {@ApiResponse(code = 201, message = "Update Interface Operation"),
      @ApiResponse(code = 403, message = "Restricted operation"),
      @ApiResponse(code = 400, message = "Invalid content / Missing content")})
  public Response updateInterfaceOperation(
      @ApiParam(value = "Interface Operation to update", required = true) String data,
      @ApiParam(value = "Resource Id") @PathParam("resourceId") String resourceId,
      @Context final HttpServletRequest request,
      @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
    return createOrUpdate(data, resourceId, request, userId, true);
  }

  @DELETE
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{interfaceOperationId}")
  @ApiOperation(value = "Delete Interface Operation", httpMethod = "DELETE", notes = "Delete Interface Operation", response = InterfaceOperationDataDefinition.class)
  @ApiResponses(value = {@ApiResponse(code = 201, message = "Delete Interface Operation"),
      @ApiResponse(code = 403, message = "Restricted operation"),
      @ApiResponse(code = 400, message = "Invalid content / Missing content")})
  public Response deleteInterfaceOperation(
      @ApiParam(value = "Interface Operation Id") @PathParam("interfaceOperationId") String interfaceOperationId,
      @ApiParam(value = "Resource Id") @PathParam("resourceId") String resourceId,
      @Context final HttpServletRequest request,
      @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
    return delete(interfaceOperationId, resourceId, request, userId);
  }

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{interfaceOperationId}")
  @ApiOperation(value = "Get Interface Operation", httpMethod = "GET", notes = "GET Interface Operation", response = InterfaceOperationDataDefinition.class)
  @ApiResponses(value = {@ApiResponse(code = 201, message = "Get Interface Operation"),
      @ApiResponse(code = 403, message = "Restricted operation"),
      @ApiResponse(code = 400, message = "Invalid content / Missing content")})
  public Response getInterfaceOperation(
      @ApiParam(value = "Interface Operation Id") @PathParam("interfaceOperationId") String interfaceOperationId,
      @ApiParam(value = "Resource Id") @PathParam("resourceId") String resourceId,
      @Context final HttpServletRequest request,
      @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

    return get(interfaceOperationId, resourceId, request, userId);
  }

  private Response get (String interfaceOperationId, String resourceId, HttpServletRequest request, String userId){
    ServletContext context = request.getSession().getServletContext();
    String url = request.getMethod() + " " + request.getRequestURI();

    User modifier = new User();
    modifier.setUserId(userId);
    log.debug("Start get request of {} with modifier id {}", url, userId);

    try {
      String resourceIdLower = resourceId.toLowerCase();
      InterfaceOperationBusinessLogic businessLogic = getInterfaceOperationBL(context);

      Either<Operation, ResponseFormat> actionResponse = businessLogic.getInterfaceOperation(resourceIdLower, interfaceOperationId, modifier, true);
      if (actionResponse.isRight()) {
        log.error("failed to get interface operation");
        return buildErrorResponse(actionResponse.right().value());
      }

      InterfaceOperationDataDefinition interfaceOperationDataDefinition = InterfaceUIDataConverter.convertOperationDataToInterfaceData(actionResponse.left().value());
      Object result = RepresentationUtils.toFilteredRepresentation(interfaceOperationDataDefinition);
      return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
    }
    catch (Exception e) {
      BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Resource interface operations");
      log.error("get resource interface operations failed with exception", e);
      return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
    }
  }

  private Response delete (String interfaceOperationId, String resourceId, HttpServletRequest
      request, String userId){

    ServletContext context = request.getSession().getServletContext();
    String url = request.getMethod() + " " + request.getRequestURI();

    User modifier = new User();
    modifier.setUserId(userId);
    log.debug("Start delete request of {} with modifier id {}", url, userId);

    try {
      String resourceIdLower = resourceId.toLowerCase();
      InterfaceOperationBusinessLogic businessLogic = getInterfaceOperationBL(context);

      Either<Operation, ResponseFormat> actionResponse = businessLogic.deleteInterfaceOperation(resourceIdLower, interfaceOperationId, modifier, true);
      if (actionResponse.isRight()) {
        log.error("failed to delete interface operation");
        return buildErrorResponse(actionResponse.right().value());
      }

      InterfaceOperationDataDefinition interfaceOperationDataDefinition = InterfaceUIDataConverter.convertOperationDataToInterfaceData(actionResponse.left().value());
      Object result = RepresentationUtils.toFilteredRepresentation(interfaceOperationDataDefinition);
      return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
    }
    catch (Exception e) {
      BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Interface Operation");
      log.error("Delete interface operation with an error", e);
      return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
    }
  }

  private Response createOrUpdate (String data, String resourceId, HttpServletRequest request, String userId, boolean isUpdate) {
    ServletContext context = request.getSession().getServletContext();
    String url = request.getMethod() + " " + request.getRequestURI();

    User modifier = new User();
    modifier.setUserId(userId);
    log.debug("Start create or update request of {} with modifier id {}", url, userId);

    try {
      String resourceIdLower = resourceId.toLowerCase();
      InterfaceOperationBusinessLogic businessLogic = getInterfaceOperationBL(context);

      Operation operation = getMappedOperationData(data, isUpdate, modifier);
      Either<Operation, ResponseFormat> actionResponse ;
      if (isUpdate) {
        actionResponse = businessLogic.updateInterfaceOperation(resourceIdLower, operation, modifier, true);
      } else {
        actionResponse = businessLogic.createInterfaceOperation(resourceIdLower, operation, modifier, true);
      }

      if (actionResponse.isRight()) {
        log.error("failed to update or create interface operation");
        return buildErrorResponse(actionResponse.right().value());
      }

      InterfaceOperationDataDefinition interfaceOperationDataDefinition = InterfaceUIDataConverter.convertOperationDataToInterfaceData(actionResponse.left().value());
      Object result = RepresentationUtils.toFilteredRepresentation(interfaceOperationDataDefinition);
      return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
    }
    catch (Exception e) {
      BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Interface Operation Creation or update");
      log.error("create or update interface Operation with an error", e);
      return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
    }
  }

  private Operation getMappedOperationData(String inputJson, boolean isUpdate, User user){
    Either<UiResourceDataTransfer, ResponseFormat> uiResourceEither = getComponentsUtils().convertJsonToObjectUsingObjectMapper(inputJson, user, UiResourceDataTransfer.class, AuditingActionEnum.CREATE_RESOURCE, ComponentTypeEnum.RESOURCE);
    Optional<InterfaceOperationDataDefinition> opDef = uiResourceEither.left().value().getInterfaceOperations().values().stream().findFirst();
    InterfaceOperationDataDefinition interfaceOperationDataDefinition = new InterfaceOperationDataDefinition();
    if(opDef.isPresent()) {
      interfaceOperationDataDefinition = opDef.get();
      if(!isUpdate)
        interfaceOperationDataDefinition.setUniqueId(UUID.randomUUID().toString());
    }
    return InterfaceUIDataConverter.convertInterfaceDataToOperationData(interfaceOperationDataDefinition);
  }

}

