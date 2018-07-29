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

import com.google.common.collect.Sets;
import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.annotations.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.components.impl.InterfaceOperationBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.utils.InterfaceUIDataConverter;
import org.openecomp.sdc.be.datatypes.elements.InterfaceOperationDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentFieldsEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.utils.InterfaceUtils;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.ui.model.UiResourceDataTransfer;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

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
      InterfaceOperationBusinessLogic businessLogic = getInterfaceOperationBL(context);
      Either<UiComponentDataTransfer, ResponseFormat> resourceResponse = businessLogic.getComponentDataFilteredByParams(resourceId, modifier, Collections
          .singletonList(ComponentFieldsEnum.INTERFACES.getValue()));
      if (resourceResponse.isRight()) {
        return buildErrorResponse(resourceResponse.right().value());
      }

      UiResourceDataTransfer uiResourceDataTransfer = (UiResourceDataTransfer) resourceResponse.left().value();
      InterfaceOperationDataDefinition interfaceOperationDataDefinition = getInterfaceOperationForResponse(interfaceOperationId, uiResourceDataTransfer.getInterfaces());

      return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), RepresentationUtils.toFilteredRepresentation(interfaceOperationDataDefinition));

    } catch (Exception e) {
      BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Resource interface operations");
      log.debug("get resource interface operations failed with exception", e);
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

    Response response;

    try {
      String resourceIdLower = resourceId.toLowerCase();
      InterfaceOperationBusinessLogic businessLogic = getInterfaceOperationBL(context);

      Either<Resource, ResponseFormat> actionResponse = businessLogic.deleteInterfaceOperation(resourceIdLower, Sets.newHashSet(interfaceOperationId), modifier, true);

      if (actionResponse.isRight()) {
        log.debug("failed to delete interface operation");
        response = buildErrorResponse(actionResponse.right().value());
        return response;
      }

      Resource resource = actionResponse.left().value();
      InterfaceOperationDataDefinition interfaceOperationDataDefinition = getInterfaceOperationForResponse(interfaceOperationId, resource.getInterfaces());
      Object result = RepresentationUtils.toFilteredRepresentation(interfaceOperationDataDefinition);
      return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);

    } catch (Exception e) {
      BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Interface Operation");
      log.debug("Delete interface operation with an error", e);
      response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
      return response;

    }
  }

  private Response createOrUpdate (String data, String resourceId, HttpServletRequest request, String userId, boolean isUpdate) {
    ServletContext context = request.getSession().getServletContext();
    String url = request.getMethod() + " " + request.getRequestURI();

    User modifier = new User();
    modifier.setUserId(userId);
    log.debug("Start create or update request of {} with modifier id {}", url, userId);

    Response response;
    try {
      String resourceIdLower = resourceId.toLowerCase();
      InterfaceOperationBusinessLogic businessLogic = getInterfaceOperationBL(context);

      Either<Resource, ResponseFormat> resourceEither = businessLogic.getResourceDetails(resourceId);
      Resource origResource = resourceEither.left().value();

      Either<Resource, ResponseFormat> convertResponse = parseToResource(data, origResource, isUpdate, modifier);
      if (convertResponse.isRight()) {
        log.debug("failed to parse resource");
        response = buildErrorResponse(convertResponse.right().value());
        return response;
      }

      Resource updatedResource = convertResponse.left().value();
      Either<Resource, ResponseFormat> actionResponse ;
      if (isUpdate) {
        actionResponse = businessLogic.updateInterfaceOperation(resourceIdLower, updatedResource, modifier, true);
      } else {
        actionResponse = businessLogic.createInterfaceOperation(resourceIdLower, updatedResource, modifier, true);
      }

      if (actionResponse.isRight()) {
        log.debug("failed to update or create interface operation");
        response = buildErrorResponse(actionResponse.right().value());
        return response;
      }

      Resource resource = actionResponse.left().value();
      List<Operation> operationData = InterfaceUtils.getOperationsFromInterface(updatedResource.getInterfaces());
      InterfaceOperationDataDefinition interfaceOperationDataDefinition = getInterfaceOperationForResponse(operationData.get(0).getUniqueId(), resource.getInterfaces());

      Object result = RepresentationUtils.toFilteredRepresentation(interfaceOperationDataDefinition);
      return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);

    } catch (Exception e) {
      BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Interface Operation Creation or update");
      log.debug("create or update interface Operation with an error", e);
      response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
      return response;

    }
  }

  private Either<Resource, ResponseFormat> parseToResource(String resourceJson, Resource origResource, boolean isUpdate, User user) {

    Resource resource = convertToResourceObject(resourceJson, user).left().value();

    Either<UiResourceDataTransfer, ResponseFormat> uiResourceEither = getComponentsUtils().convertJsonToObjectUsingObjectMapper(resourceJson, user, UiResourceDataTransfer.class, AuditingActionEnum.CREATE_RESOURCE, ComponentTypeEnum.RESOURCE);
    Optional<InterfaceOperationDataDefinition> opDef = uiResourceEither.left().value().getInterfaceOperations().values().stream().findFirst();
    InterfaceOperationDataDefinition interfaceOperationDataDefinition;
    if(opDef.isPresent()) {
      interfaceOperationDataDefinition = opDef.get();

      if(!isUpdate)
        interfaceOperationDataDefinition.setUniqueId(UUID.randomUUID().toString());

      Map<String, Operation> interfaceOperations = new HashMap<>();
      interfaceOperations.put(interfaceOperationDataDefinition.getUniqueId(), InterfaceUIDataConverter.convertInterfaceDataToOperationData(interfaceOperationDataDefinition));
      InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
      interfaceDefinition.setUniqueId(UUID.randomUUID().toString());
      interfaceDefinition.setToscaResourceName(InterfaceUtils.createInterfaceToscaResourceName(origResource.getName()));
      interfaceDefinition.setOperationsMap(interfaceOperations);

      Map<String, InterfaceDefinition> interfaceMap = new HashMap<>();
      interfaceMap.put(interfaceDefinition.getUniqueId(), interfaceDefinition);

      resource.setInterfaces(interfaceMap);
    }

    return Either.left(resource);
  }

  private Either<Resource, ResponseFormat> convertToResourceObject(String resourceJson, User user) {
    return getComponentsUtils().convertJsonToObjectUsingObjectMapper(resourceJson, user, Resource.class, AuditingActionEnum.CREATE_RESOURCE, ComponentTypeEnum.RESOURCE);
  }

  private InterfaceOperationDataDefinition getInterfaceOperationForResponse(String interfaceOperationId, Map<String, InterfaceDefinition> interfaces){
    InterfaceOperationDataDefinition interfaceOperationDataDefinition = new InterfaceOperationDataDefinition();
    if(!MapUtils.isEmpty(interfaces)){
      List<Operation> operationData = InterfaceUtils.getOperationsFromInterface(interfaces);
      if(CollectionUtils.isNotEmpty(operationData)){
        Optional<Operation> matchedOp = operationData.stream().filter(a -> a.getUniqueId().equals(interfaceOperationId)).findAny();
        if(matchedOp.isPresent()) {
          interfaceOperationDataDefinition = InterfaceUIDataConverter.convertOperationDataToInterfaceData(matchedOp.get());
        }
      }
    }
    return interfaceOperationDataDefinition;
  }

}

