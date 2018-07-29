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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
public class RequirementsServlet extends BeGenericServlet {

    private static final Logger log = Logger.getLogger(RequirementsServlet.class);

    @PUT
    @Path("resources/{resourceId}/requirements/{requirementId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update Resource Requirement", httpMethod = "PUT", notes = "Returns updated requirement", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Resource requirement updated"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
    public Response updateRequirement(@ApiParam(value = "resource id to update with new requirement", required = true) @PathParam("resourceId") final String resourceId,
                                      @ApiParam(value = "requirement id to update", required = true) @PathParam("requirementId") final String requirementId, @ApiParam(value = "Resource property to update", required = true) String requirementData,
                                      @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        // Convert RequirementDefinition from JSON
        // TODO: it's going to be another object, probably. This is placeholder
        // for sake of JSON validation
        // RequirementDefinition requirementDefinition;
        ResponseFormat responseFormat;
        try {
            // TODO pass real entity
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), null);
        }  catch (Exception e) {
            log.debug("Unexpected error: ", e);
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            return buildErrorResponse(responseFormat);
        }
    }
}
