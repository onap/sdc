/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.api.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/v1/catalog/customToscaFunctions")
@Tags({@Tag(name = "SDCE-2 APIs")})
@Singleton
public class DefaultCustomToscaFunctionServlet extends BeGenericServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCustomToscaFunctionServlet.class);

    @Inject
    public DefaultCustomToscaFunctionServlet(final ComponentsUtils componentsUtils) {
        super(componentsUtils);
    }

    @GET
    @Path("/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve default custom tosca functions values from the configuration file based on type", method = "GET",
        summary = "Retrieve all custom tosca functions",
        responses = {
            @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
            @ApiResponse(responseCode = "200", description = "Returns default custom tosca functions values from configuration file Ok"),
            @ApiResponse(responseCode = "404", description = "Default custom tosca functions not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getDefaultCustomToscaFunctionValues(@Context final HttpServletRequest request,
                                                        @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                                        @PathParam("type") Type type) {
        final String url = request.getMethod() + " " + request.getRequestURI();
        LOGGER.debug("Start handle request of {}", url);
        final Map<String, Object> defaultCustomToscaFunctionssMap = new HashMap<>();
        try {
            List<Configuration.CustomToscaFunction> defaultCustomToscaFunction = getDefaultCustomToscaFunctionValues();
            if (!type.equals(Type.ALL)) {
                defaultCustomToscaFunction = defaultCustomToscaFunction.stream()
                    .filter(func -> type.name().toLowerCase().equals(func.getType())).collect(
                        Collectors.toList());
            }
            if (CollectionUtils.isEmpty(defaultCustomToscaFunction)) {
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
            }
            defaultCustomToscaFunctionssMap.put("defaultCustomToscaFunction", defaultCustomToscaFunction);
        } catch (final Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("SDC Failed to retrieve all default custom tosca functions");
            LOGGER.debug("Method getDefaultCustomToscaFunctionValues failed with unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), defaultCustomToscaFunctionssMap);
    }

    private List<Configuration.CustomToscaFunction> getDefaultCustomToscaFunctionValues() {
        List<Configuration.CustomToscaFunction> customFunctions =
            ConfigurationManager.getConfigurationManager().getConfiguration().getDefaultCustomToscaFunctions();
        return customFunctions == null ? Collections.emptyList() : customFunctions;
    }

    public enum Type {ALL, CUSTOM, GET_INPUT}
}
