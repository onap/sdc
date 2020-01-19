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

import com.google.gson.Gson;
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
import org.openecomp.sdc.be.components.impl.ConsumerBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ConsumerDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

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

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/consumers")
@OpenAPIDefinition(info = @Info(title = "Consumer Servlet",description = "Consumer Servlet"))
@Singleton
public class ConsumerServlet extends BeGenericServlet {

    private static final String MODIFIER_ID_IS = "modifier id is {}";
	private static final String START_HANDLE_REQUEST_OF = "Start handle request of {}";
	private static final Logger log = Logger.getLogger(ConsumerServlet.class);
    private final ConsumerBusinessLogic businessLogic;

	  @Inject
    public ConsumerServlet(UserBusinessLogic userBusinessLogic,
        ComponentsUtils componentsUtils,
        ConsumerBusinessLogic businessLogic) {
        super(userBusinessLogic, componentsUtils);
        this.businessLogic = businessLogic;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Consumer credentials", method = "POST",
            summary = "Returns created ECOMP consumer credentials",responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Consumer credentials created"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    public Response createConsumer(@Parameter(description = "Consumer Object to be created", required = true) String data,
            @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        ServletContext context = request.getSession().getServletContext();

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);

        User modifier = new User();
        modifier.setUserId(userId);
        log.debug(MODIFIER_ID_IS, userId);

        try {
            Either<ConsumerDefinition, ResponseFormat> convertionResponse = convertJsonToObject(data, modifier, AuditingActionEnum.ADD_ECOMP_USER_CREDENTIALS);

            if (convertionResponse.isRight()) {
                log.debug("failed to create Consumer");
                return buildErrorResponse(convertionResponse.right().value());
            }

            ConsumerDefinition consumer = convertionResponse.left().value();

            Either<ConsumerDefinition, ResponseFormat> actionResult = businessLogic.createConsumer(modifier, consumer);

            if (actionResult.isRight()) {
                log.debug("failed to create Consumer");
                return buildErrorResponse(actionResult.right().value());
            }

            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), actionResult.left().value());

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create consumer");
            log.debug("create consumer failed with exception", e);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            return buildErrorResponse(responseFormat);

        }
    }

    @GET
    @Path("/{consumerId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve Consumer", method = "GET", summary = "Returns consumer according to ConsumerID",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ConsumerDefinition.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Consumer found"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "404", description = "Consumer not found")})
    public Response getConsumer(@PathParam("consumerId") final String consumerId,
            @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        ServletContext context = request.getSession().getServletContext();

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);

        User modifier = new User();
        modifier.setUserId(userId);
        log.debug(MODIFIER_ID_IS, userId);

        Response response = null;
        try {
            Either<ConsumerDefinition, ResponseFormat> actionResponse = businessLogic.getConsumer(consumerId, modifier);

            if (actionResponse.isRight()) {
                log.debug("failed to get consumer");
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Consumer");
            log.debug("get consumer failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

        }
    }

    @DELETE
    @Path("/{consumerId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Deletes Consumer", method = "DELETE",
            summary = "Returns deleted consumer according to ConsumerID",  responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ConsumerDefinition.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Consumer deleted"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "404", description = "Consumer not found")})
    public Response deleteConsumer(@PathParam("consumerId") final String consumerId,
            @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        ServletContext context = request.getSession().getServletContext();

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);

        User modifier = new User();
        modifier.setUserId(userId);
        log.debug(MODIFIER_ID_IS, userId);

        Response response = null;
        try {
            Either<ConsumerDefinition, ResponseFormat> actionResponse = businessLogic.deleteConsumer(consumerId, modifier);

            if (actionResponse.isRight()) {
                log.debug("failed to delete consumer");
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Consumer");
            log.debug("delete consumer failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

        }
    }

    public Either<ConsumerDefinition, ResponseFormat> convertJsonToObject(String data, User user, AuditingActionEnum actionEnum) {
        ConsumerDefinition consumer;
        Gson gson = new Gson();
        try {
            log.trace("convert json to object. json=\n {}", data);
            consumer = gson.fromJson(data, ConsumerDefinition.class);
            if (consumer == null) {
                BeEcompErrorManager.getInstance().logBeInvalidJsonInput("convertJsonToObject");
                log.debug("object is null after converting from json");
                ResponseFormat responseFormat = getComponentsUtils().getInvalidContentErrorForConsumerAndAudit(user, null, actionEnum);
                return Either.right(responseFormat);
            }
        } catch (Exception e) {
            // INVALID JSON
            BeEcompErrorManager.getInstance().logBeInvalidJsonInput("convertJsonToObject");
            log.debug("failed to convert from json {}", data, e);
            ResponseFormat responseFormat = getComponentsUtils().getInvalidContentErrorForConsumerAndAudit(user, null, actionEnum);
            return Either.right(responseFormat);
        }
        return Either.left(consumer);
    }

}
