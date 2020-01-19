/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.openecomp.sdc.be.components.impl.PolicyTypeBusinessLogic;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.mixin.PolicyTypeMixin;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.be.view.ResponseView;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Controller;
import com.jcabi.aspects.Loggable;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@OpenAPIDefinition(info = @Info(title = "policy types resource"))
@Controller
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PolicyTypesEndpoint extends BeGenericServlet{

    private static final Logger log = Logger.getLogger(PolicyTypesEndpoint.class);

    private final PolicyTypeBusinessLogic policyTypeBusinessLogic;

    public PolicyTypesEndpoint(UserBusinessLogic userBusinessLogic,
        ComponentsUtils componentsUtils, PolicyTypeBusinessLogic policyTypeBusinessLogic) {
        super(userBusinessLogic, componentsUtils);
        this.policyTypeBusinessLogic = policyTypeBusinessLogic;
    }

    @GET
    @Path("/policyTypes")
    @Operation(description = "Get policy types ", method = "GET", summary = "Returns policy types",responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PolicyTypeDefinition.class)))))
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "policy types found"),
                            @ApiResponse(responseCode = "403", description = "Restricted operation"),
                            @ApiResponse(responseCode = "500", description = "The GET request failed due to internal SDC problem.")})
    @ResponseView(mixin = {PolicyTypeMixin.class})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public List<PolicyTypeDefinition> getPolicyTypes(@Parameter(description = "An optional parameter to indicate the type of the container from where this call is executed")
                                   @QueryParam("internalComponentType") String internalComponentType,
                                   @Parameter(description = "The user id", required = true) @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        log.debug("(get) Start handle request of GET policyTypes");
        return policyTypeBusinessLogic.getAllPolicyTypes(userId, internalComponentType);
    }

}
