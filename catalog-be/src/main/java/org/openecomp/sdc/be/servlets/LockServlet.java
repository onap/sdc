/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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

import com.jcabi.aspects.Loggable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Controller;


@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tag(name = "SDCE-2 APIs")
@Server(url = "/sdc2/rest")
@Controller
public class LockServlet extends BeGenericServlet {

    private static final Logger log = Logger.getLogger(LockServlet.class);
    private final UserValidations userValidations;
    private final IGraphLockOperation graphLockOperation;

    @Inject
    public LockServlet(final ComponentsUtils componentsUtils,
                       final UserValidations userValidations, IGraphLockOperation graphLockOperation) {
        super(componentsUtils);
        this.userValidations = userValidations;
        this.graphLockOperation = graphLockOperation;
    }

    @POST
    @Path("/lock")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    @Operation(description = "Toggle disable locking", method = "POST", responses = {
        @ApiResponse(content = @Content(schema = @Schema(implementation = Response.class))),
        @ApiResponse(responseCode = "200", description = "Disable locking successfully updated"),
        @ApiResponse(responseCode = "500", description = "Update disable locking failed")
    })
    public Response toggleDisableLocking(@Context final HttpServletRequest request, @HeaderParam("USER_ID") String userId,
                                         @Parameter(description = "Disable Locking") boolean disable) {
        log.info("User {} attempting to set disable locking with value {}", userId, disable);
        userValidations.validateUserRole(userValidations.validateUserExists(userId), Arrays.asList(Role.DESIGNER, Role.ADMIN));
        try {
            return Response.ok().entity(graphLockOperation.disableLocking(disable)).build();
        } catch (final Exception e) {
            log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, LockServlet.class.getName(), "Failed to set disable locking", e);
            return Response.serverError().build();
        }
    }
}