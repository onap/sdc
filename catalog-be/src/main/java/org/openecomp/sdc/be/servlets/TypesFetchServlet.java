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

import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Types Fetch Servlet", description = "Types Fetch Servlet")
@Singleton
public class TypesFetchServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(TypesFetchServlet.class);

    @GET
    @Path("dataTypes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get data types", httpMethod = "GET", notes = "Returns data types", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "datatypes"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 404, message = "Data types not found") })
    public Response getAllDataTypesServlet(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        Wrapper<Response> responseWrapper = new Wrapper<>();
        Wrapper<User> userWrapper = new Wrapper<>();
        ServletContext context = request.getSession().getServletContext();

        try {
            init();
            validateUserExist(responseWrapper, userWrapper, userId);

            if (responseWrapper.isEmpty()) {
                String url = request.getMethod() + " " + request.getRequestURI();
                log.debug("Start handle request of {} | modifier id is {}", url, userId);

                PropertyBusinessLogic businessLogic = getPropertyBL(context);
                Either<Map<String, DataTypeDefinition>, ResponseFormat> allDataTypes = businessLogic.getAllDataTypes();

                if (allDataTypes.isRight()) {
                    log.info("Failed to get all dara types. Reason - {}", allDataTypes.right().value());
                    Response errorResponse = buildErrorResponse(allDataTypes.right().value());
                    responseWrapper.setInnerElement(errorResponse);

                } else {

                    Map<String, DataTypeDefinition> dataTypes = allDataTypes.left().value();
                    String dataTypeJson = gson.toJson(dataTypes);
                    Response okResponse = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), dataTypeJson);
                    responseWrapper.setInnerElement(okResponse);

                }
            }

            return responseWrapper.getInnerElement();
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Property");
            log.debug("get all data types failed with exception", e);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            return buildErrorResponse(responseFormat);
        }
    }

    private PropertyBusinessLogic getPropertyBL(ServletContext context) {
        WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
        WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
        return webApplicationContext.getBean(PropertyBusinessLogic.class);
    }

}
