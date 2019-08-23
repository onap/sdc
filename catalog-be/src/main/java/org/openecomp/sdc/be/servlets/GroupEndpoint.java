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
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogicNew;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.common.api.Constants;
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

/**
 * Here new APIs for group will be written in an attempt to gradually clean BL code
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@OpenAPIDefinition(info = @Info(title = "Group Servlet"))
@Controller
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GroupEndpoint {

    private final GroupBusinessLogicNew groupBusinessLogic;

    @Inject
    public GroupEndpoint(GroupBusinessLogicNew groupBusinessLogic) {
        this.groupBusinessLogic = groupBusinessLogic;
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/groups/{groupUniqueId}/members")
    @Operation(description = "Update group members ", method = "POST",
            summary = "Updates list of members and returns it", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Group members updated"), @ApiResponse(
            responseCode = "400",
            description = "field name invalid type/length, characters;  mandatory field is absent, already exists (name)"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "404", description = "Component not found"),
            @ApiResponse(responseCode = "500", description = "Internal Error")})
    public List<String> updateGroupMembers(@PathParam("containerComponentType") final String containerComponentType,
            @PathParam("componentId") final String componentId, @PathParam("groupUniqueId") final String groupUniqueId,
            @Parameter(description = "List of members unique ids", required = true) List<String> members,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
        return groupBusinessLogic.updateMembers(componentId, componentTypeEnum, userId, groupUniqueId, members);
    }

    @GET
    @Path("/{containerComponentType}/{componentId}/groups/{groupUniqueId}/properties")
    @Operation(description = "Get List of properties on a group", method = "GET",
            summary = "Returns list of properties", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = GroupProperty.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Group Updated"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    public List<PropertyDataDefinition> getGroupProperties(
            @PathParam("containerComponentType") final String containerComponentType,
            @PathParam("componentId") final String componentId, @PathParam("groupUniqueId") final String groupUniqueId,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return groupBusinessLogic.getProperties(containerComponentType, userId, componentId, groupUniqueId);
    }

    @PUT
    @Path("/{containerComponentType}/{componentId}/groups/{groupUniqueId}/properties")
    @Operation(description = "Updates List of properties on a group (only values)", method = "PUT",
            summary = "Returns updated list of properties", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = GroupProperty.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Group Updated"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    public List<GroupProperty> updateGroupProperties(
            @PathParam("containerComponentType") final String containerComponentType,
            @PathParam("componentId") final String componentId, @PathParam("groupUniqueId") final String groupUniqueId,
            @Parameter(description = "Group Properties to be Updated", required = true) List<GroupProperty> properties,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
        return groupBusinessLogic.updateProperties(componentId, componentTypeEnum, userId, groupUniqueId, properties);
    }

}
