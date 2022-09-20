/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation. All rights reserved.
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Controller;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tag(name = "SDCE-2 APIs")
@Server(url = "/sdc2/rest")
@Controller
public class DataTypeServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(DataTypeServlet.class);

    private final ResourceBusinessLogic resourceBusinessLogic;

    @Inject
    public DataTypeServlet(UserBusinessLogic userBusinessLogic,
                           ComponentInstanceBusinessLogic componentInstanceBL,
                           ComponentsUtils componentsUtils, ServletUtils servletUtils,
                           ResourceImportManager resourceImportManager, ResourceBusinessLogic resourceBusinessLogic) {
        super(userBusinessLogic, componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.resourceBusinessLogic = resourceBusinessLogic;
    }

    @GET
    @Path("{dataType}/{model}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get data types", method = "GET", summary = "Returns data types", responses = {
        @ApiResponse(content = @Content(schema = @Schema(implementation = Response.class))),
        @ApiResponse(responseCode = "200", description = "datatype"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Data types not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getAllDataTypesServlet(@Context final HttpServletRequest request,
                                           @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                           @PathParam("dataType") String dataType,
                                           @PathParam("model") String modelName) {
        Wrapper<Response> responseWrapper = new Wrapper<>();
        Wrapper<User> userWrapper = new Wrapper<>();
        init();
        validateUserExist(responseWrapper, userWrapper, userId);
        if (responseWrapper.isEmpty()) {
            String url = request.getMethod() + " " + request.getRequestURI();
            log.debug("Start handle request of {} - modifier id is {}", url, userId);
            resourceBusinessLogic.getApplicationDataTypeCache().refreshDataTypesCacheIfStale();
            final Map<String, DataTypeDefinition> dataTypes = resourceBusinessLogic.getComponentsUtils()
                .getAllDataTypes(resourceBusinessLogic.getApplicationDataTypeCache(), modelName);
            Optional<DataTypeDefinition> optionalDataTypeDefinition = dataTypes.values().stream()
                .filter(dataTypeDefinition -> dataTypeDefinition.getName().equals(dataType)).findFirst();
            if (optionalDataTypeDefinition.isPresent()) {
                String dataTypeJson = gson.toJson(optionalDataTypeDefinition.get());
                Response okResponse = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), dataTypeJson);
                responseWrapper.setInnerElement(okResponse);
            }
        }
        return responseWrapper.getInnerElement();
    }

}
