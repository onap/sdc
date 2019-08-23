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

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoBase;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.utils.UiComponentDataConverter;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.ui.model.UiComponentMetadata;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcabi.aspects.Loggable;
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

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@OpenAPIDefinition(info = @Info(title = "Lifecycle Actions Servlet", description = "Lifecycle Actions Servlet"))
@Singleton
public class LifecycleServlet extends BeGenericServlet {

    private static final Logger log = Logger.getLogger(LifecycleServlet.class);
    private final LifecycleBusinessLogic lifecycleBusinessLogic;

    @Inject
    public LifecycleServlet(UserBusinessLogic userBusinessLogic,
        ComponentsUtils componentsUtils,
        LifecycleBusinessLogic lifecycleBusinessLogic) {
        super(userBusinessLogic, componentsUtils);
        this.lifecycleBusinessLogic = lifecycleBusinessLogic;
    }


    @POST
    @Path("/{componentCollection}/{componentId}/lifecycleState/{lifecycleOperation}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Change Resource lifecycle State", method = "POST", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Resource state changed"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "409", description = "Resource already exist")})
    public Response changeResourceState(
            @Parameter(
                    description = "LifecycleChangeInfo - relevant for checkin, failCertification, cancelCertification",
                    required = false) String jsonChangeInfo,
            @Parameter(description = "validValues: resources / services / products",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
                            ComponentTypeEnum.SERVICE_PARAM_NAME, ComponentTypeEnum.PRODUCT_PARAM_NAME})) @PathParam(
                                    value = "componentCollection") final String componentCollection,
            @Parameter(schema = @Schema(allowableValues = {
                    "checkout, undoCheckout, checkin, certificationRequest, startCertification, failCertification,  cancelCertification, certify"}),
                    required = true) @PathParam(value = "lifecycleOperation") final String lifecycleTransition,
            @Parameter(description = "id of component to be changed") @PathParam(
                    value = "componentId") final String componentId,
            @Context final HttpServletRequest request,
            @Parameter(description = "id of user initiating the operation") @HeaderParam(
                    value = Constants.USER_ID_HEADER) String userId) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);

        Response response = null;

        // get modifier from graph
        log.debug("get modifier properties");
        Either<User, ResponseFormat> eitherGetUser = getUser(request, userId);
        if (eitherGetUser.isRight()) {
            return buildErrorResponse(eitherGetUser.right().value());
        }
        User user = eitherGetUser.left().value();

        String resourceIdLower = componentId.toLowerCase();
        log.debug("perform {} operation to resource with id {} ", lifecycleTransition, resourceIdLower);
        Either<LifeCycleTransitionEnum, Response> validateEnum = validateTransitionEnum(lifecycleTransition, user);
        if (validateEnum.isRight()) {
            return validateEnum.right().value();
        }

        LifecycleChangeInfoWithAction changeInfo = new LifecycleChangeInfoWithAction();

        try {
            if (jsonChangeInfo != null && !jsonChangeInfo.isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                changeInfo = new LifecycleChangeInfoWithAction(
                        mapper.readValue(jsonChangeInfo, LifecycleChangeInfoBase.class).getUserRemarks());
            }
        }

        catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeInvalidJsonInput("convertJsonToObject");
            log.debug("failed to convert from json {}", jsonChangeInfo, e);
            ResponseFormat responseFormat = getComponentsUtils().getInvalidContentErrorAndAudit(user, componentId,
                    AuditingActionEnum.CHECKOUT_RESOURCE);
            return buildErrorResponse(responseFormat);
        }

        try {
            LifeCycleTransitionEnum transitionEnum = validateEnum.left().value();
            ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(componentCollection);
            if (componentType != null) {
                Either<? extends Component, ResponseFormat> actionResponse =
                        lifecycleBusinessLogic.changeComponentState(componentType, componentId, user, transitionEnum,
                                changeInfo, false, true);

                if (actionResponse.isRight()) {
                    log.info("failed to change resource state");
                    response = buildErrorResponse(actionResponse.right().value());
                    return response;
                }

                log.debug("change state successful !!!");
                UiComponentMetadata componentMetatdata =
                        UiComponentDataConverter.convertToUiComponentMetadata(actionResponse.left().value());
                Object value = RepresentationUtils.toRepresentation(componentMetatdata);
                response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), value);
                return response;
            } else {
                log.info(
                        "componentCollection \"{}\" is not valid. Supported componentCollection values are \"{}\", \"{}\" or \"{}\"",
                        componentCollection, ComponentTypeEnum.RESOURCE_PARAM_NAME,
                        ComponentTypeEnum.SERVICE_PARAM_NAME, ComponentTypeEnum.PRODUCT_PARAM_NAME);
                ResponseFormat error = getComponentsUtils().getInvalidContentErrorAndAudit(user, componentId,
                        AuditingActionEnum.CHECKOUT_RESOURCE);
                return buildErrorResponse(error);
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Change Lifecycle State");
            log.debug("change lifecycle state failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }
    }

    private Either<LifeCycleTransitionEnum, Response> validateTransitionEnum(final String lifecycleTransition, User user) {
        LifeCycleTransitionEnum transitionEnum = LifeCycleTransitionEnum.CHECKOUT;
        try {
            transitionEnum = LifeCycleTransitionEnum.getFromDisplayName(lifecycleTransition);
        } catch (IllegalArgumentException e) {
            log.info("state operation is not valid. operations allowed are: {}", LifeCycleTransitionEnum.valuesAsString(), e);
            ResponseFormat error = getComponentsUtils().getInvalidContentErrorAndAudit(user, "", AuditingActionEnum.CHECKOUT_RESOURCE);
            return Either.right(buildErrorResponse(error));
        }
        return Either.left(transitionEnum);
    }

}
