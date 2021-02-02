/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021, Nordix Foundation. All rights reserved.
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
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
import org.openecomp.sdc.be.components.impl.OutputsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.ComponentInstanceOutput;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Tag(name = "SDC Internal APIs")
@Server(url = "/sdc2/rest")
@Path("/v1/catalog")
@Controller
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OutputsServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(OutputsServlet.class);
    private static final String START_HANDLE_REQUEST_OF = "(get) Start handle request of {}";

    private final OutputsBusinessLogic outputsBusinessLogic;

    @Inject
    public OutputsServlet(final UserBusinessLogic userBusinessLogic,
                          final OutputsBusinessLogic outputsBusinessLogic,
                          final ComponentInstanceBusinessLogic componentInstanceBL,
                          final ComponentsUtils componentsUtils,
                          final ServletUtils servletUtils,
                          final ResourceImportManager resourceImportManager) {
        super(userBusinessLogic, componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.outputsBusinessLogic = outputsBusinessLogic;
    }

    @GET
    @Path("/{componentType}/{componentId}/componentInstances/{instanceId}/{originComponentUid}/outputs")
    @Operation(description = "Get Outputs only", method = "GET", summary = "Returns Outputs list", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Component found"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    public Response getComponentInstanceOutputs(@PathParam("componentType") final String componentType,
                                                @PathParam("componentId") final String componentId,
                                                @PathParam("instanceId") final String instanceId,
                                                @PathParam("originComponentUid") final String originComponentUid,
                                                @Context final HttpServletRequest request,
                                                @HeaderParam(value = Constants.USER_ID_HEADER) final String userId) throws IOException {

        final String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);

        try {
            final Either<List<ComponentInstanceOutput>, ResponseFormat> outputsResponse =
                outputsBusinessLogic.getComponentInstanceOutputs(userId, componentId, instanceId);
            if (outputsResponse.isRight()) {
                log.debug("failed to get component instance outputs {}", componentType);
                return buildErrorResponse(outputsResponse.right().value());
            }
            final Object outputs = RepresentationUtils.toRepresentation(outputsResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), outputs);

        } catch (final Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Outputs " + componentType);
            log.debug("getOutputs failed with exception", e);
            throw e;
        }
    }

}
