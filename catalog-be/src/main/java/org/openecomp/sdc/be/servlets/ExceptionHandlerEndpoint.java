/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
import com.google.gson.GsonBuilder;
import com.jcabi.aspects.Loggable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.Servers;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tags({@Tag(name = "SDC Internal APIs")})
@Servers({@Server(url = "/sdc2/rest")})
@Produces(MediaType.APPLICATION_JSON)
@Controller
public class ExceptionHandlerEndpoint {

    private static final Logger log = Logger.getLogger(ExceptionHandlerEndpoint.class);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ComponentsUtils componentsUtils;

    ExceptionHandlerEndpoint(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }

    @GET
    @Path("/handleException")
    @Operation(description = "Handle exception", method = "GET", responses = {
        @ApiResponse(responseCode = "500", description = "Internal Error", content = @Content(schema = @Schema(implementation = Response.class)))})
    public Response sendError() {
        log.debug("Request is received");
        ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
        return Response.status(responseFormat.getStatus()).entity(gson.toJson(responseFormat.getRequestError())).build();
    }
}
