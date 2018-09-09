/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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

import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.annotations.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.openecomp.sdc.be.components.impl.CombinationBusinessLogic;
import org.openecomp.sdc.be.components.impl.CsarValidationUtils;
import org.openecomp.sdc.be.components.impl.ImportUtils;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.servlets.ResourceUploadServlet.ResourceAuthorityTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.IOException;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Resources Catalog", description = "Resources Servlet")
@Singleton
public class CombinationServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(CombinationServlet.class);

    @POST
    @Path("/combination/{serviceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create Combination", httpMethod = "POST", notes = "Returns created combination", response = Combination.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Combination created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 409, message = "Combination already exist") })
    public Response createCombination(@ApiParam(value = "Combination object to be created", required = true) String data, @PathParam("serviceId") final String serviceId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        userId = (userId != null) ? userId : request.getHeader(Constants.USER_ID_HEADER);
        init();

        ServletContext context = request.getSession().getServletContext();

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);

        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);

        Response response;
        try {

            Wrapper<Response> responseWrapper = new Wrapper<>();
            // UI Import
            if (isUIImport(data)) {
                performUIImport(responseWrapper, data, request, userId, null);
            }
            // UI Create
            else {

                CombinationBusinessLogic businessLogic = getCombinationBL(context);

                Either<Service, ResponseFormat> getServiceResponse = getServiceBL(context).getService(serviceId.toLowerCase(), modifier);
                if (getServiceResponse.isRight()) {
                    log.debug("failed to get service");
                    response = buildErrorResponse(getServiceResponse.right().value());
                    return response;
                }
                Service service = getServiceResponse.left().value();

                Either<UICombination, ResponseFormat> convertResponse = parseToCombination(data);
                if (convertResponse.isRight()) {
                    log.debug("failed to parse combination");
                    response = buildErrorResponse(convertResponse.right().value());
                    return response;
                }

                UICombination UICombination = convertResponse.left().value();
                Combination combination = new Combination(UICombination);
                Either<Combination, ResponseFormat> createdCombination = businessLogic.createCombination(combination, service);
                Object representation = RepresentationUtils.toRepresentation(createdCombination);
                response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), representation);
                responseWrapper.setInnerElement(response);
            }
            return responseWrapper.getInnerElement();
        } catch (IOException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create Resource");
            log.debug("create resource failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;
        }
    }

    private Either<UICombination, ResponseFormat> parseToCombination(String data) {
        UICombination combination = gson.fromJson(data, UICombination.class);
        return Either.left(combination);
    }

    private boolean isUIImport(String data) {
        boolean isUIImport;
        try {
            JSONObject json = new JSONObject(data);
            String payloadName = json.getString(ImportUtils.Constants.UI_JSON_PAYLOAD_NAME);
            isUIImport = payloadName != null && !payloadName.isEmpty();
        } catch (JSONException e) {
            log.debug("failed to parse json sent from client, json:{}", data, e);
            isUIImport = false;
        }
        return isUIImport;
    }

    private void performUIImport(Wrapper<Response> responseWrapper, String data, final HttpServletRequest request, String userId, String resourceUniqueId) throws FileNotFoundException {

        Wrapper<User> userWrapper = new Wrapper<>();
        Wrapper<UploadResourceInfo> uploadResourceInfoWrapper = new Wrapper<>();
        Wrapper<String> yamlStringWrapper = new Wrapper<>();

        ResourceAuthorityTypeEnum resourceAuthorityEnum = ResourceAuthorityTypeEnum.USER_TYPE_UI;

        commonGeneralValidations(responseWrapper, userWrapper, uploadResourceInfoWrapper, resourceAuthorityEnum, userId, data);

        if (!CsarValidationUtils.isCsarPayloadName(uploadResourceInfoWrapper.getInnerElement().getPayloadName())) {
            fillPayload(responseWrapper, uploadResourceInfoWrapper, yamlStringWrapper, userWrapper.getInnerElement(), data, resourceAuthorityEnum, null);

            // PayLoad Validations
            commonPayloadValidations(responseWrapper, yamlStringWrapper, userWrapper.getInnerElement(), uploadResourceInfoWrapper.getInnerElement());
        }
        specificResourceAuthorityValidations(responseWrapper, uploadResourceInfoWrapper, yamlStringWrapper, userWrapper.getInnerElement(), request, data, resourceAuthorityEnum);

        if (responseWrapper.isEmpty()) {
            handleImport(responseWrapper, userWrapper.getInnerElement(), uploadResourceInfoWrapper.getInnerElement(), yamlStringWrapper.getInnerElement(), resourceAuthorityEnum, true, resourceUniqueId);
        }
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/combinationInstance")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create ComponentInstance", httpMethod = "POST", notes = "Returns created ComponentInstance", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Component created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 409, message = "Component instance already exist") })
    public Response createCombinationInstance(@ApiParam(value = "RI object to be created", required = true) String data, @PathParam("componentId") final String containerComponentId,
                                              @ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
                                              @HeaderParam(value = Constants.USER_ID_HEADER) @ApiParam(value = "USER_ID of modifier user", required = true) String userId, @Context final HttpServletRequest request) {
        ServletContext context = request.getSession().getServletContext();

        try {

            ComponentInstance componentInstance = RepresentationUtils.fromRepresentation(data, ComponentInstance.class);
            CombinationBusinessLogic businessLogic = getCombinationBL(context);
            if (businessLogic == null) {
                log.debug("unsupported type", containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            Either<ComponentInstance, ResponseFormat> actionResponse = businessLogic.createCombinationInstance(containerComponentType, containerComponentId, userId, componentInstance);

            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), actionResponse.left().value());

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create Component Instance");
            log.debug("create component instance failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }
}