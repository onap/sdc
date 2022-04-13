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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.Servers;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.jetty.http.HttpStatus;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.be.user.UserBusinessLogicExt;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Controller;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/user")
@Tags({@Tag(name = "SDCE-2 APIs")})
@Servers({@Server(url = "/sdc2/rest")})
@Controller
public class UserAdminServlet extends BeGenericServlet {

    private static final String ROLE_DELIMITER = ",";
    private static final Logger log = Logger.getLogger(UserAdminServlet.class);
    private final UserBusinessLogic userBusinessLogic;
    private final UserBusinessLogicExt userBusinessLogicExt;

    UserAdminServlet(UserBusinessLogic userBusinessLogic, ComponentsUtils componentsUtils, UserBusinessLogicExt userBusinessLogicExt) {
        super(userBusinessLogic, componentsUtils);
        this.userBusinessLogic = userBusinessLogic;
        this.userBusinessLogicExt = userBusinessLogicExt;
    }

    // retrieve all user details
    @GET
    @Path("/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "retrieve user details", method = "GET", summary = "Returns user details according to userId", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
        @ApiResponse(responseCode = "200", description = "Returns user Ok"), @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "405", description = "Method Not Allowed"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public User get(@Parameter(description = "userId of user to get", required = true) @PathParam("userId") final String userId,
                    @Context final HttpServletRequest request) {
        return userBusinessLogic.getUser(userId, false);
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @GET
    @Path("/{userId}/role")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "retrieve user role", summary = "Returns user role according to userId", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "200", description = "Returns user role Ok"), @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "405", description = "Method Not Allowed"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public String getRole(@Parameter(description = "userId of user to get", required = true) @PathParam("userId") final String userId,
                          @Context final HttpServletRequest request) {
        User user = userBusinessLogic.getUser(userId, false);
        return "{ \"role\" : \"" + user.getRole() + "\" }";
    }

    // update user role
    @POST
    @Path("/{userId}/role")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "update user role", summary = "Update user role", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
        @ApiResponse(responseCode = "200", description = "Update user OK"), @ApiResponse(responseCode = "400", description = "Invalid Content."),
        @ApiResponse(responseCode = "403", description = "Missing information/Restricted operation"),
        @ApiResponse(responseCode = "404", description = "User not found"), @ApiResponse(responseCode = "405", description = "Method Not Allowed"),
        @ApiResponse(responseCode = "409", description = "User already exists"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public User updateUserRole(@Parameter(description = "userId of user to get", required = true) @PathParam("userId") final String userIdUpdateUser,
                               @Context final HttpServletRequest request,
                               @Parameter(description = "json describe the update role", required = true) UserRole newRole,
                               @HeaderParam(value = Constants.USER_ID_HEADER) String modifierUserId) {
        return userBusinessLogic.updateUserRole(modifierUserId, userIdUpdateUser, newRole.getRole().name());
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "add user", method = "POST", summary = "Provision new user", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
        @ApiResponse(responseCode = "201", description = "New user created"), @ApiResponse(responseCode = "400", description = "Invalid Content."),
        @ApiResponse(responseCode = "403", description = "Missing information"),
        @ApiResponse(responseCode = "405", description = "Method Not Allowed"),
        @ApiResponse(responseCode = "409", description = "User already exists"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public Response createUser(@Context final HttpServletRequest request,
                               @Parameter(description = "json describe the user", required = true) User newUser,
                               @HeaderParam(value = Constants.USER_ID_HEADER) String modifierAttId) {
        log.debug("modifier id is {}", modifierAttId);
        User user = userBusinessLogic.createUser(modifierAttId, newUser);
        return Response.status(HttpStatus.CREATED_201).entity(user).build();
    }

    @GET
    @Path("/authorize")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "authorize", summary = "authorize user", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
        @ApiResponse(responseCode = "200", description = "Returns user Ok"), @ApiResponse(responseCode = "403", description = "Restricted Access"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public User authorize(@HeaderParam(value = Constants.USER_ID_HEADER) String userId, @HeaderParam("HTTP_CSP_FIRSTNAME") String firstName,
                          @HeaderParam("HTTP_CSP_LASTNAME") String lastName, @HeaderParam("HTTP_CSP_EMAIL") String email) {
        User authUser = new User();
        authUser.setUserId(userId);
        authUser.setFirstName(firstName);
        authUser.setLastName(lastName);
        authUser.setEmail(email);
        return userBusinessLogic.authorize(authUser);
    }

    @GET
    @Path("/admins")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "retrieve all administrators", method = "GET", summary = "Returns all administrators", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
        @ApiResponse(responseCode = "200", description = "Returns user Ok"), @ApiResponse(responseCode = "405", description = "Method Not Allowed"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public List<User> getAdminsUser(@Context final HttpServletRequest request) {
        return userBusinessLogic.getAllAdminUsers();
    }

    @GET
    @Path("/users")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve the list of all active ASDC users or only group of users having specific roles.", method = "GET", summary = "Returns list of users with the specified roles, or all of users in the case of empty 'roles' header", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
        @ApiResponse(responseCode = "200", description = "Returns users Ok"),
        @ApiResponse(responseCode = "204", description = "No provisioned ASDC users of requested role"),
        @ApiResponse(responseCode = "403", description = "Restricted Access"), @ApiResponse(responseCode = "400", description = "Missing content"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public List<User> getUsersList(@Context final HttpServletRequest request,
                                   @Parameter(description = "Any active user's USER_ID ") @HeaderParam(Constants.USER_ID_HEADER) final String userId,
                                   @Parameter(description = "TESTER,DESIGNER,PRODUCT_STRATEGIST,OPS,PRODUCT_MANAGER,GOVERNOR, ADMIN OR all users by not typing anything") @QueryParam("roles") final String roles) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {} modifier id is {}", url, userId);
        List<String> rolesList = new ArrayList<>();
        if (roles != null && !roles.trim().isEmpty()) {
            String[] rolesArr = roles.split(ROLE_DELIMITER);
            for (String role : rolesArr) {
                rolesList.add(role.trim());
            }
        }
        return userBusinessLogic.getUsersList(userId, rolesList, roles);
    }

    @DELETE
    @Path("/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "delete user", summary = "Delete user", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
        @ApiResponse(responseCode = "200", description = "Update deleted OK"), @ApiResponse(responseCode = "400", description = "Invalid Content."),
        @ApiResponse(responseCode = "403", description = "Missing information"), @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "405", description = "Method Not Allowed"),
        @ApiResponse(responseCode = "409", description = "Restricted operation"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public User deActivateUser(@Parameter(description = "userId of user to get", required = true) @PathParam("userId") final String userId,
                               @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String modifierId) {
        return userBusinessLogicExt.deActivateUser(modifierId, userId);
    }

    static class UserRole {

        Role role;

        public Role getRole() {
            return role;
        }

        public void setRole(Role role) {
            this.role = role;
        }
    }
}
