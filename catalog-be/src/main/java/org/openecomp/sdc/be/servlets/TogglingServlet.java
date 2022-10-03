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

import com.jcabi.aspects.Loggable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.Servers;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.TogglingBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.common.log.wrappers.Logger;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog/toggle")
@Tags({@Tag(name = "SDCE-2 APIs")})
@Servers({@Server(url = "/sdc2/rest")})
@Singleton
public class TogglingServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(TogglingServlet.class);
    private static final String ALL_FEATURES_STATES_WERE_SET_SUCCESSFULLY = "All features states were set successfully";
    private static final String START_HANDLE_REQUEST_OF = "Start handle request of {}";
    private static final String FEATURE_STATE_WAS_UPDATED_SUCCESSFULLY = "Feature state was updated successfully";
    private final TogglingBusinessLogic togglingBusinessLogic;

    @Inject
    public TogglingServlet(ComponentInstanceBusinessLogic componentInstanceBL, ComponentsUtils componentsUtils,
                           ServletUtils servletUtils, ResourceImportManager resourceImportManager, TogglingBusinessLogic togglingBusinessLogic) {
        super(componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.togglingBusinessLogic = togglingBusinessLogic;
    }

    @GET
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get all Toggleable features", method = "GET", summary = "Returns list of toggleable features", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = javax.ws.rs.core.Response.class)))),
        @ApiResponse(responseCode = "200", description = "Success"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Toggleable features not found")})
    public Response getAllFeatures(@Context final HttpServletRequest request) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try {
            Map<String, Boolean> features = togglingBusinessLogic.getAllFeatureStates();
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), features);
        } catch (Exception e) {
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @GET
    @Path("/{featureName}/state")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get Toggleable feature state", method = "GET", summary = "Returns one toggleable feature state", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = javax.ws.rs.core.Response.class)))),
        @ApiResponse(responseCode = "200", description = "Success"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Toggleable feature not found")})
    public Response getToggleableFeature(@PathParam("featureName") String featureName, @Context final HttpServletRequest request) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try {
            boolean featureState = togglingBusinessLogic.getFeatureState(featureName);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), featureState);
        } catch (Exception e) {
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @PUT
    @Path("/state/{state}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update all feature toggle state", method = "PUT", summary = "Update all feature status", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = javax.ws.rs.core.Response.class)))),
        @ApiResponse(responseCode = "200", description = "Success"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Toggleable features not found")})
    public Response setAllFeatures(@PathParam("state") boolean state, @Context final HttpServletRequest request) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try {
            togglingBusinessLogic.setAllFeatures(state);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), ALL_FEATURES_STATES_WERE_SET_SUCCESSFULLY);
        } catch (Exception e) {
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @PUT
    @Path("/{featureName}/state/{state}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update feature toggle state", method = "PUT", summary = "Update feature status", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = javax.ws.rs.core.Response.class)))),
        @ApiResponse(responseCode = "200", description = "Success"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Toggleable features not found")})
    public Response updateFeatureState(@PathParam("featureName") String featureName, @PathParam("state") boolean state,
                                       @Context final HttpServletRequest request) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(get) Start handle request of {}", url);
        try {
            togglingBusinessLogic.updateFeatureState(featureName, state);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), FEATURE_STATE_WAS_UPDATED_SUCCESSFULLY);
        } catch (Exception e) {
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }
}
