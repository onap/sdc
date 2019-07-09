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

import com.jcabi.aspects.Loggable;
import io.swagger.annotations.*;
import org.openecomp.sdc.be.components.impl.GroupTypeBusinessLogic;
import org.openecomp.sdc.be.mixin.GroupTypeMixin;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.view.ResponseView;
import org.openecomp.sdc.common.api.Constants;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "group types resource")
@Controller
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GroupTypesEndpoint {

    private final GroupTypeBusinessLogic groupTypeBusinessLogic;

    public GroupTypesEndpoint(GroupTypeBusinessLogic groupTypeBusinessLogic) {
        this.groupTypeBusinessLogic = groupTypeBusinessLogic;
    }

    @GET
    @Path("/groupTypes")
    @ApiOperation(value = "Get group types ", httpMethod = "GET", notes = "Returns group types", response = GroupTypeDefinition.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "group types found"),
            @ApiResponse(code = 400, message = "field name invalid type/length, characters;  mandatory field is absent, already exists (name)"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 500, message = "Internal Error")
    })
    @ResponseView(mixin = {GroupTypeMixin.class})
    public List<GroupTypeDefinition> getGroupTypes(@HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                                   @ApiParam(value = "An optional parameter to indicate the type of the container from where this call is executed")
                                                   @QueryParam("internalComponentType") String internalComponentType) {
        return groupTypeBusinessLogic.getAllGroupTypes(userId, internalComponentType);
    }

}
