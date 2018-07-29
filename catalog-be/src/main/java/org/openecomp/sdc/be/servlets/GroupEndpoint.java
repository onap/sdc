package org.openecomp.sdc.be.servlets;

import com.jcabi.aspects.Loggable;
import io.swagger.annotations.*;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogicNew;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.common.api.Constants;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Here new APIs for group will be written in an attempt to gradually clean BL code
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Group Servlet")
@Controller
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GroupEndpoint {

    private final GroupBusinessLogicNew groupBusinessLogic;

    public GroupEndpoint(GroupBusinessLogicNew groupBusinessLogic) {
        this.groupBusinessLogic = groupBusinessLogic;
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/groups/{groupUniqueId}/members")
    @ApiOperation(value = "Update group members ", httpMethod = "POST", notes = "Updates list of members and returns it", response = String.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Group members updated"),
            @ApiResponse(code = 400, message = "field name invalid type/length, characters;  mandatory field is absent, already exists (name)"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 404, message = "Component not found"),
            @ApiResponse(code = 500, message = "Internal Error")
    })
    public List<String> updateGroupMembers(
            @PathParam("containerComponentType") final String containerComponentType,
            @PathParam("componentId") final String componentId,
            @PathParam("groupUniqueId") final String groupUniqueId,
            @ApiParam(value = "List of members unique ids", required = true) List<String> members,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
        return groupBusinessLogic.updateMembers(componentId, componentTypeEnum, userId, groupUniqueId, members);
    }

    @GET
    @Path("/{containerComponentType}/{componentId}/groups/{groupUniqueId}/properties")
    @ApiOperation(value = "Get List of properties on a group", httpMethod = "GET", notes = "Returns list of properties", response = GroupProperty.class, responseContainer="List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Group Updated"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content") })
    public List<PropertyDataDefinition> getGroupProperties(@PathParam("containerComponentType") final String containerComponentType,
                                                           @PathParam("componentId") final String componentId,
                                                           @PathParam("groupUniqueId") final String groupUniqueId,
                                                           @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return groupBusinessLogic.getProperties(containerComponentType, userId, componentId, groupUniqueId);
    }

    @PUT
    @Path("/{containerComponentType}/{componentId}/groups/{groupUniqueId}/properties")
    @ApiOperation(value = "Updates List of properties on a group (only values)", httpMethod = "PUT", notes = "Returns updated list of properties", response = GroupProperty.class, responseContainer="List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Group Updated"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content") })
    public List<GroupProperty> updateGroupProperties(@PathParam("containerComponentType") final String containerComponentType,
                                                     @PathParam("componentId") final String componentId,
                                                     @PathParam("groupUniqueId") final String groupUniqueId,
                                                     @ApiParam(value = "Group Properties to be Updated", required = true) List<GroupProperty> properties,
                                                     @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
        return groupBusinessLogic.updateProperties(componentId, componentTypeEnum, userId, groupUniqueId, properties);
    }

}
