/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.servlets;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.api.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/v1/catalog/directives")
@Tags({@Tag(name = "SDCE-2 APIs")})
@Singleton
public class DirectiveServlet extends BeGenericServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectiveServlet.class);

    @Inject
    public DirectiveServlet(final ComponentsUtils componentsUtils) {
        super(componentsUtils);
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve all Directives values from configuration file", method = "GET", summary = "Retrieve all Directives", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
        @ApiResponse(responseCode = "200", description = "Returns Directive values from configuration file Ok"),
        @ApiResponse(responseCode = "404", description = "Directive not found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getConfCategoriesAndVersion(@Context final HttpServletRequest request,
                                                @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        final String url = request.getMethod() + " " + request.getRequestURI();
        LOGGER.debug("Start handle request of {}", url);
        final Map<String, Object> directivesMap = new HashMap<>();
        try {
            final List<String> directives = getDirectiveValues();
            if (CollectionUtils.isEmpty(directives)) {
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
            }
            directivesMap.put("directives", directives);
        } catch (final Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("SDC Failed to retrieve all Directives");
            LOGGER.debug("Method getDirectiveValues failed with unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), directivesMap);
    }

    private List<String> getDirectiveValues() {
        return ConfigurationManager.getConfigurationManager().getConfiguration().getDirectives();
    }
}
