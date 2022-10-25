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
import fj.data.Either;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import org.keycloak.representations.AccessToken;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ElementBusinessLogic;
import org.openecomp.sdc.be.components.impl.ModelBusinessLogic;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.components.scheduledtasks.ComponentsCleanBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.info.ArtifactTypesInfo;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.catalog.CatalogComponent;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.ui.model.UiCategories;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.Multitenancy;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Path("/v1/")
/**
 *
 * UI oriented servlet - to return elements in specific format UI needs
 *
 *
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@io.swagger.v3.oas.annotations.tags.Tag(name = "SDCE-2 APIs")
@Server(url = "/sdc2/rest")
@Controller
public class ElementServlet extends BeGenericServlet {

    private static final Logger log = Logger.getLogger(ElementServlet.class);
    private static final String START_HANDLE_REQUEST_OF = "Start handle request of {}";
    private final ComponentsCleanBusinessLogic componentsCleanBusinessLogic;
    private final ElementBusinessLogic elementBusinessLogic;
    private final ArtifactsBusinessLogic artifactsBusinessLogic;
    private final ModelBusinessLogic modelBusinessLogic;

    @Inject
    public ElementServlet(final ComponentsUtils componentsUtils,
                          final ComponentsCleanBusinessLogic componentsCleanBusinessLogic, final ElementBusinessLogic elementBusinessLogic,
                          final ArtifactsBusinessLogic artifactsBusinessLogic, final ModelBusinessLogic modelBusinessLogic) {
        super(componentsUtils);
        this.componentsCleanBusinessLogic = componentsCleanBusinessLogic;
        this.elementBusinessLogic = elementBusinessLogic;
        this.artifactsBusinessLogic = artifactsBusinessLogic;
        this.modelBusinessLogic = modelBusinessLogic;
    }
    /*
     ******************************************************************************
     * NEW CATEGORIES category / \ subcategory subcategory / grouping
     ******************************************************************************/

    /*
     *
     *
     * CATEGORIES
     */

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    // retrieve all component categories
    @GET
    @Path("/categories/{componentType}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve the list of all resource/service/product categories/sub-categories/groupings", method = "GET", summary = "Retrieve the list of all resource/service/product categories/sub-categories/groupings.", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Returns categories Ok"),
        @ApiResponse(responseCode = "403", description = "Missing information"),
        @ApiResponse(responseCode = "400", description = "Invalid component type"),
        @ApiResponse(responseCode = "409", description = "Restricted operation"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getComponentCategories(
        @Parameter(description = "allowed values are resources / services/ products", schema = @Schema(allowableValues = {
            ComponentTypeEnum.RESOURCE_PARAM_NAME, ComponentTypeEnum.SERVICE_PARAM_NAME,
            ComponentTypeEnum.PRODUCT_PARAM_NAME}), required = true) @PathParam(value = "componentType") final String componentType,
        @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @Context final HttpServletRequest request) {
        try {
            ElementBusinessLogic elementBL = getElementBL(request.getSession().getServletContext());
            Either<List<CategoryDefinition>, ResponseFormat> either = elementBL.getAllCategories(componentType, userId);
            if (either.isRight()) {
                log.debug("No categories were found for type {}", componentType);
                return buildErrorResponse(either.right().value());
            } else {
                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), either.left().value());
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Component Categories");
            log.debug("getComponentCategories failed with exception", e);
            throw e;
        }
    }

    @POST
    @Path("/category/{componentType}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create new component category", method = "POST", summary = "Create new component category", responses = {
        @ApiResponse(responseCode = "201", description = "Category created"),
        @ApiResponse(responseCode = "400", description = "Invalid category data"),
        @ApiResponse(responseCode = "403", description = "USER_ID header is missing"),
        @ApiResponse(responseCode = "409", description = "Category already exists / User not permitted to perform the action"),
        @ApiResponse(responseCode = "500", description = "General Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createComponentCategory(
        @Parameter(description = "allowed values are resources /services / products", schema = @Schema(allowableValues = {
            ComponentTypeEnum.RESOURCE_PARAM_NAME, ComponentTypeEnum.SERVICE_PARAM_NAME,
            ComponentTypeEnum.PRODUCT_PARAM_NAME}), required = true) @PathParam(value = "componentType") final String componentType,
        @Parameter(description = "Category to be created", required = true) String data, @Context final HttpServletRequest request,
        @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        try {
            ElementBusinessLogic elementBL = getElementBL(request.getSession().getServletContext());
            CategoryDefinition category = RepresentationUtils.fromRepresentation(data, CategoryDefinition.class);
            Either<CategoryDefinition, ResponseFormat> createResourceCategory = elementBL.createCategory(category, componentType, userId);
            if (createResourceCategory.isRight()) {
                return buildErrorResponse(createResourceCategory.right().value());
            }
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.CREATED);
            return buildOkResponse(responseFormat, createResourceCategory.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create resource category");
            log.debug("createResourceCategory failed with exception", e);
            throw e;
        }
    }

    @GET
    @Path("/category/{componentType}/{categoryName}/baseTypes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get base types for category", method = "GET", summary = "Get base types for category",
        responses = {@ApiResponse(responseCode = "200", description = "Returns base types Ok"),
            @ApiResponse(responseCode = "404", description = "No base types were found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getCategoryBaseTypes(@PathParam(value = "categoryName") final String categoryName,
                                         @PathParam(value = "componentType") final String componentType,
                                         @Context final HttpServletRequest request,
                                         @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                         @Parameter(description = "model", required = false) @QueryParam("model") String modelName) {
        try {
            final ElementBusinessLogic elementBL = getElementBL(request.getSession().getServletContext());
            final Either<List<BaseType>, ActionStatus> either = elementBL.getBaseTypes(categoryName, userId, modelName);

            if (either.isRight() || either.left().value() == null) {
                log.debug("No base types were found");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
            } else {
                final Map<String, Object> baseTypesMap = new HashMap<>();
                baseTypesMap.put("baseTypes", either.left().value());
                baseTypesMap.put("required", elementBL.isBaseTypeRequired(categoryName));
                baseTypesMap.put("defaultBaseType",elementBL.getDefaultBaseType(categoryName));

                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), baseTypesMap);
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get base types of category");
            log.debug("getCategoryBaseTypes failed with exception", e);
            throw e;
        }
    }

    @DELETE
    @Path("/category/{componentType}/{categoryUniqueId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete component category", method = "DELETE", summary = "Delete component category", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Category.class)))),
        @ApiResponse(responseCode = "204", description = "Category deleted"),
        @ApiResponse(responseCode = "403", description = "USER_ID header is missing"),
        @ApiResponse(responseCode = "409", description = "User not permitted to perform the action"),
        @ApiResponse(responseCode = "404", description = "Category not found"), @ApiResponse(responseCode = "500", description = "General Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteComponentCategory(@PathParam(value = "categoryUniqueId") final String categoryUniqueId,
                                            @PathParam(value = "componentType") final String componentType, @Context final HttpServletRequest request,
                                            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        try {
            ElementBusinessLogic elementBL = getElementBL(request.getSession().getServletContext());
            Either<CategoryDefinition, ResponseFormat> createResourceCategory = elementBL.deleteCategory(categoryUniqueId, componentType, userId);
            if (createResourceCategory.isRight()) {
                return buildErrorResponse(createResourceCategory.right().value());
            }
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT);
            return buildOkResponse(responseFormat, null);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create resource category");
            log.debug("createResourceCategory failed with exception", e);
            throw e;
        }
    }

    /*
     *
     *
     * SUBCATEGORIES
     *
     */
    @POST
    @Path("/category/{componentType}/{categoryId}/subCategory")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create new component sub-category", method = "POST", summary = "Create new component sub-category for existing category", responses = {
        @ApiResponse(responseCode = "201", description = "Subcategory created"),
        @ApiResponse(responseCode = "400", description = "Invalid subcategory data"),
        @ApiResponse(responseCode = "403", description = "USER_ID header is missing"),
        @ApiResponse(responseCode = "404", description = "Parent category wasn't found"),
        @ApiResponse(responseCode = "409", description = "Subcategory already exists / User not permitted to perform the action"),
        @ApiResponse(responseCode = "500", description = "General Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createComponentSubCategory(
        @Parameter(description = "allowed values are resources / products", schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
            ComponentTypeEnum.PRODUCT_PARAM_NAME}), required = true) @PathParam(value = "componentType") final String componentType,
        @Parameter(description = "Parent category unique ID", required = true) @PathParam(value = "categoryId") final String categoryId,
        @Parameter(description = "Subcategory to be created. \ne.g. {\"name\":\"Resource-subcat\"}", required = true) String data,
        @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        try {
            ElementBusinessLogic elementBL = getElementBL(request.getSession().getServletContext());
            SubCategoryDefinition subCategory = RepresentationUtils.fromRepresentation(data, SubCategoryDefinition.class);
            Either<SubCategoryDefinition, ResponseFormat> createSubcategory = elementBL
                .createSubCategory(subCategory, componentType, categoryId, userId);
            if (createSubcategory.isRight()) {
                return buildErrorResponse(createSubcategory.right().value());
            }
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.CREATED);
            return buildOkResponse(responseFormat, createSubcategory.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create sub-category");
            log.debug("createComponentSubCategory failed with exception", e);
            throw e;
        }
    }

    @DELETE
    @Path("/category/{componentType}/{categoryUniqueId}/subCategory/{subCategoryUniqueId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete component category", method = "DELETE", summary = "Delete component category", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Category.class)))),
        @ApiResponse(responseCode = "204", description = "Category deleted"),
        @ApiResponse(responseCode = "403", description = "USER_ID header is missing"),
        @ApiResponse(responseCode = "409", description = "User not permitted to perform the action"),
        @ApiResponse(responseCode = "404", description = "Category not found"), @ApiResponse(responseCode = "500", description = "General Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteComponentSubCategory(@PathParam(value = "categoryUniqueId") final String categoryUniqueId,
                                               @PathParam(value = "subCategoryUniqueId") final String subCategoryUniqueId,
                                               @PathParam(value = "componentType") final String componentType,
                                               @Context final HttpServletRequest request,
                                               @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        try {
            ElementBusinessLogic elementBL = getElementBL(request.getSession().getServletContext());
            Either<SubCategoryDefinition, ResponseFormat> deleteSubResourceCategory = elementBL
                .deleteSubCategory(subCategoryUniqueId, componentType, userId);
            if (deleteSubResourceCategory.isRight()) {
                return buildErrorResponse(deleteSubResourceCategory.right().value());
            }
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT);
            return buildOkResponse(responseFormat, null);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete component subcategory");
            log.debug("deleteComponentSubCategory failed with exception", e);
            throw e;
        }
    }

    /*
     * GROUPINGS
     */
    @POST
    @Path("/category/{componentType}/{categoryId}/subCategory/{subCategoryId}/grouping")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create new component grouping", method = "POST", summary = "Create new component grouping for existing sub-category", responses = {
        @ApiResponse(responseCode = "201", description = "Grouping created"),
        @ApiResponse(responseCode = "400", description = "Invalid grouping data"),
        @ApiResponse(responseCode = "403", description = "USER_ID header is missing"),
        @ApiResponse(responseCode = "404", description = "Parent category or subcategory were not found"),
        @ApiResponse(responseCode = "409", description = "Grouping already exists / User not permitted to perform the action"),
        @ApiResponse(responseCode = "500", description = "General Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createComponentGrouping(@Parameter(description = "allowed values are products", schema = @Schema(allowableValues = {
        ComponentTypeEnum.PRODUCT_PARAM_NAME}), required = true) @PathParam(value = "componentType") final String componentType,
                                            @Parameter(description = "Parent category unique ID", required = true) @PathParam(value = "categoryId") final String grandParentCategoryId,
                                            @Parameter(description = "Parent sub-category unique ID", required = true) @PathParam(value = "subCategoryId") final String parentSubCategoryId,
                                            @Parameter(description = "Subcategory to be created", required = true) String data,
                                            @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        try {
            ElementBusinessLogic elementBL = getElementBL(request.getSession().getServletContext());
            GroupingDefinition grouping = RepresentationUtils.fromRepresentation(data, GroupingDefinition.class);
            Either<GroupingDefinition, ResponseFormat> createGrouping = elementBL
                .createGrouping(grouping, componentType, grandParentCategoryId, parentSubCategoryId, userId);
            if (createGrouping.isRight()) {
                return buildErrorResponse(createGrouping.right().value());
            }
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.CREATED);
            return buildOkResponse(responseFormat, createGrouping.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create grouping");
            log.debug("createComponentGrouping failed with exception", e);
            throw e;
        }
    }

    @DELETE
    @Path("/category/{componentType}/{categoryUniqueId}/subCategory/{subCategoryUniqueId}/grouping/{groupingUniqueId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete component category", method = "DELETE", summary = "Delete component category", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Category.class)))),
        @ApiResponse(responseCode = "204", description = "Category deleted"),
        @ApiResponse(responseCode = "403", description = "USER_ID header is missing"),
        @ApiResponse(responseCode = "409", description = "User not permitted to perform the action"),
        @ApiResponse(responseCode = "404", description = "Category not found"), @ApiResponse(responseCode = "500", description = "General Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteComponentGrouping(@PathParam(value = "categoryUniqueId") final String grandParentCategoryUniqueId,
                                            @PathParam(value = "subCategoryUniqueId") final String parentSubCategoryUniqueId,
                                            @PathParam(value = "groupingUniqueId") final String groupingUniqueId,
                                            @PathParam(value = "componentType") final String componentType, @Context final HttpServletRequest request,
                                            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        try {
            ElementBusinessLogic elementBL = getElementBL(request.getSession().getServletContext());
            Either<GroupingDefinition, ResponseFormat> deleteGrouping = elementBL.deleteGrouping(groupingUniqueId, componentType, userId);
            if (deleteGrouping.isRight()) {
                return buildErrorResponse(deleteGrouping.right().value());
            }
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT);
            return buildOkResponse(responseFormat, null);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete component grouping");
            log.debug("deleteGrouping failed with exception", e);
            throw e;
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    // retrieve all tags
    @GET
    @Path("/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve all tags", method = "GET", summary = "Retrieve all tags", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
        @ApiResponse(responseCode = "200", description = "Returns tags Ok"), @ApiResponse(responseCode = "404", description = "No tags were found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getTags(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(getTags) Start handle request of {}", url);
        try {
            ElementBusinessLogic elementBL = getElementBL(request.getSession().getServletContext());
            Either<List<Tag>, ActionStatus> either = elementBL.getAllTags(userId);
            if (either.isRight() || either.left().value() == null) {
                log.debug("No tags were found");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
            } else {
                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), either.left().value());
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get All Tags");
            log.debug("getAllTags failed with exception", e);
            throw e;
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    // retrieve all property scopes
    @GET
    @Path("/propertyScopes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve all propertyScopes", method = "GET", summary = "Retrieve all propertyScopes", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
        @ApiResponse(responseCode = "200", description = "Returns propertyScopes Ok"),
        @ApiResponse(responseCode = "404", description = "No propertyScopes were found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getPropertyScopes(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(getPropertyScopes) Start handle request of {}", url);
        try {
            ElementBusinessLogic elementBL = getElementBL(request.getSession().getServletContext());
            Either<List<PropertyScope>, ActionStatus> either = elementBL.getAllPropertyScopes(userId);
            if (either.isRight() || either.left().value() == null) {
                log.debug("No property scopes were found");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
            } else {
                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), either.left().value());
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Property Scopes Categories");
            log.debug("getPropertyScopes failed with exception", e);
            throw e;
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    // retrieve all artifact types
    @GET
    @Path("/artifactTypes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve all artifactTypes", method = "GET", summary = "Retrieve all artifactTypes", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
        @ApiResponse(responseCode = "200", description = "Returns artifactTypes Ok"),
        @ApiResponse(responseCode = "404", description = "No artifactTypes were found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getArtifactTypes(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(GET - getArtifactTypes) Start handle request of {}", url);
        try {
            ElementBusinessLogic elementBL = getElementBL(request.getSession().getServletContext());
            Either<List<ArtifactType>, ActionStatus> either = elementBL.getAllArtifactTypes(userId);
            if (either.isRight() || either.left().value() == null) {
                log.debug("No artifact types were found");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
            } else {
                Integer defaultHeatTimeout = ConfigurationManager.getConfigurationManager().getConfiguration().getHeatArtifactDeploymentTimeout()
                    .getDefaultMinutes();
                ArtifactTypesInfo typesResponse = new ArtifactTypesInfo();
                typesResponse.setArtifactTypes(either.left().value());
                typesResponse.setHeatDefaultTimeout(defaultHeatTimeout);
                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), typesResponse);
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Artifact Types");
            log.debug("getArtifactTypes failed with exception", e);
            throw e;
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    // retrieve all followed resources and services
    @GET
    @Path("/followed")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve all followed", method = "GET", summary = "Retrieve all followed", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
        @ApiResponse(responseCode = "200", description = "Returns followed Ok"),
        @ApiResponse(responseCode = "404", description = "No followed were found"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getFollowedResourcesServices(@Context final HttpServletRequest request,
                                                 @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        try {
            String url = request.getMethod() + " " + request.getRequestURI();
            log.debug(START_HANDLE_REQUEST_OF, url);
            UserBusinessLogic userAdminManager = getUserAdminManager(request.getSession().getServletContext());
            User userData = userAdminManager.getUser(userId, false);
            Either<Map<String, List<? extends Component>>, ResponseFormat> followedResourcesServices = getElementBL(
                request.getSession().getServletContext()).getFollowed(userData);
            if (followedResourcesServices.isRight()) {
                log.debug("failed to get followed resources services ");
                return buildErrorResponse(followedResourcesServices.right().value());
            }
            Multitenancy keyaccess= new Multitenancy();
            if (keyaccess.multitenancycheck() == true) {
                AccessToken.Access realmAccess = keyaccess.getAccessToken(request).getRealmAccess();
                Set<String> realmroles = realmAccess.getRoles();
                Map<String, List<? extends Component>> dataResponse = new HashMap<>();
               followedResourcesServices.left().value().entrySet().stream()
                        .forEach(component->{component.setValue(component.getValue().stream().filter(cm->realmroles.stream()
                                .anyMatch(role->cm.getTenant().equals(role))).collect(Collectors.toList()));
                            dataResponse.put(component.getKey(), component.getValue());
                        });
                Object data = RepresentationUtils.toRepresentation(dataResponse);
                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), data);
            }
            else{
                Object data = RepresentationUtils.toRepresentation(followedResourcesServices.left().value());
                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), data);
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Followed Resources / Services Categories");
            log.debug("Getting followed resources/services failed with exception", e);
            throw e;
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    // retrieve all certified resources and services and their last version
    @GET
    @Path("/screen")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve catalog resources and services", method = "GET", summary = "Retrieve catalog resources and services", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
        @ApiResponse(responseCode = "200", description = "Returns resources and services Ok"),
        @ApiResponse(responseCode = "404", description = "No resources and services were found"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getCatalogComponents(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                         @QueryParam("excludeTypes") List<OriginTypeEnum> excludeTypes) throws IOException {
        try {
            String url = request.getMethod() + " " + request.getRequestURI();
            log.debug(START_HANDLE_REQUEST_OF, url);
            Either<Map<String, List<CatalogComponent>>, ResponseFormat> catalogData = getElementBL(request.getSession().getServletContext())
                .getCatalogComponents(excludeTypes);
            if (catalogData.isRight()) {
                log.debug("failed to get catalog data");
                return buildErrorResponse(catalogData.right().value());
            }
            Object data = RepresentationUtils.toRepresentation(catalogData.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), data);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Catalog Components");
            log.debug("Getting catalog components failed with exception", e);
            throw e;
        }
    }

    @DELETE
    @Path("/inactiveComponents/{componentType}")
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteMarkedResources(@PathParam("componentType") final String componentType, @Context final HttpServletRequest request) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        // get modifier id
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);
        NodeTypeEnum nodeType = NodeTypeEnum.getByNameIgnoreCase(componentType);
        if (nodeType == null) {
            log.info("componentType is not valid: {}", componentType);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
        }
        List<NodeTypeEnum> componentsList = new ArrayList<>();
        componentsList.add(nodeType);
        try {
            Map<NodeTypeEnum, Either<List<String>, ResponseFormat>> cleanComponentsResult = componentsCleanBusinessLogic
                .cleanComponents(componentsList);
            Either<List<String>, ResponseFormat> cleanResult = cleanComponentsResult.get(nodeType);
            if (cleanResult.isRight()) {
                log.debug("failed to delete marked components of type {}", nodeType);
                return buildErrorResponse(cleanResult.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), cleanResult.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Marked Components");
            log.debug("delete marked components failed with exception", e);
            throw e;
        }
    }

    @GET
    @Path("/ecompPortalMenu")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve ecomp portal menu - MOC", method = "GET", summary = "Retrieve ecomp portal menu", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
        @ApiResponse(responseCode = "200", description = "Retrieve ecomp portal menu")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getListOfCsars(@Context final HttpServletRequest request) {
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
            "[{\"menuId\":1,\"column\":2,\"text\":\"Design\",\"parentMenuId\":null,\"url\":\"\",\"appid\":null,\"roles\":null,\"children\":[{\"menuId\":11,\"column\":1,\"text\":\"ProductDesign\",\"parentMenuId\":1,\"url\":\"\",\"appid\":null,\"roles\":null},{\"menuId\":12,\"column\":2,\"text\":\"Service\",\"parentMenuId\":1,\"url\":\"\",\"appid\":null,\"roles\":null,\"children\":[{\"menuId\":21,\"column\":1,\"text\":\"ViewPolicies\",\"parentMenuId\":12,\"url\":\"\",\"appid\":null,\"roles\":null,\"children\":[{\"menuId\":90,\"column\":1,\"text\":\"4thLevelApp1aR16\",\"parentMenuId\":21,\"url\":\"http://google.com\",\"appid\":null,\"roles\":null}]},{\"menuId\":22,\"column\":2,\"text\":\"UpdatePolicies\",\"parentMenuId\":12,\"url\":\"\",\"appid\":null,\"roles\":null,\"children\":[{\"menuId\":91,\"column\":1,\"text\":\"4thLevelApp1bR16\",\"parentMenuId\":22,\"url\":\"http://jsonlint.com/\",\"appid\":null,\"roles\":null}]},{\"menuId\":23,\"column\":3,\"text\":\"UpdateRules\",\"parentMenuId\":12,\"url\":\"\",\"appid\":null,\"roles\":null},{\"menuId\":24,\"column\":4,\"text\":\"CreateSignatures?\",\"parentMenuId\":12,\"url\":\"\",\"appid\":null,\"roles\":null},{\"menuId\":25,\"column\":5,\"text\":\"Definedata\",\"parentMenuId\":12,\"url\":\"\",\"appid\":null,\"roles\":null}]}]}]");
    }

    @GET
    @Path("/catalogUpdateTime")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve previus and current catalog update time", method = "GET", summary = "Retrieve previus and current catalog update time", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Retrieve previus and current catalog update time")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getCatalogUpdateTime(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(post) Start handle request of {}", url);
        CatalogUpdateTimestamp catalogUpdateTimestamp = getElementBL(request.getSession().getServletContext()).getCatalogUpdateTime();
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), catalogUpdateTimestamp);
    }

    // retrieve all artifact types, ui configuration and sdc version
    @GET
    @Path("/setup/ui")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve all artifactTypes, ui configuration and sdc version", method = "GET", summary = "Retrieve all artifactTypes, ui configuration and sdc version", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
        @ApiResponse(responseCode = "200", description = "Returns artifactTypes, ui configuration and sdc version Ok"),
        @ApiResponse(responseCode = "404", description = "No artifactTypes were found/no ui configuration were found/no sdc version were found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getConfCategoriesAndVersion(@Context final HttpServletRequest request,
                                                @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(getConsolidated) Start handle request of {}", url);
        Map<String, Object> consolidatedObject = new HashMap<>();
        try {
            ServletContext servletContext = request.getSession().getServletContext();
            Map<String, Object> configuration = getConfigurationUi(elementBusinessLogic);
            if (!configuration.isEmpty()) {
                consolidatedObject.put("configuration", configuration);
            } else {
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
            }
            Either<UiCategories, ResponseFormat> either = elementBusinessLogic.getAllCategories(userId);
            if (either.isRight()) {
                log.debug("No categories were found");
                return buildErrorResponse(either.right().value());
            }
            consolidatedObject.put("categories", either.left().value());
            consolidatedObject.put("models", modelBusinessLogic.listModels());
            consolidatedObject.put("version", getVersion(servletContext));
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("getSDCVersion");
            log.debug("method getConfCategoriesAndVersion failed with unexpected exception", e);
            throw e;
        }
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), consolidatedObject);
    }

    private String getVersion(ServletContext servletContext) {
        String version = (String) servletContext.getAttribute(Constants.ASDC_RELEASE_VERSION_ATTR);
        log.debug("sdc version from manifest is: {}", version);
        return version;
    }

    private Map<String, Object> getConfigurationUi(final ElementBusinessLogic elementBL) {
        Either<Configuration.HeatDeploymentArtifactTimeout, ActionStatus> defaultHeatTimeout = elementBL.getDefaultHeatTimeout();
        Either<Map<String, String>, ActionStatus> resourceTypesMap = elementBL.getResourceTypesMap();
        Map<String, Object> configuration = new HashMap<>();
        if (defaultHeatTimeout.isRight() || defaultHeatTimeout.left().value() == null) {
            log.debug("heat default timeout was not found");
            return configuration;
        }
        if (resourceTypesMap.isRight() || resourceTypesMap.left().value() == null) {
            log.debug("No resource types were found");
            return configuration;
        }
        configuration.put("artifact", artifactsBusinessLogic.getConfiguration());
        configuration.put("heatDeploymentTimeout", defaultHeatTimeout.left().value());
        configuration.put("componentTypes", elementBL.getAllComponentTypesParamNames());
        configuration.put("roles", elementBL.getAllSupportedRoles());
        configuration.put("resourceTypes", resourceTypesMap.left().value());
        configuration.put("environmentContext", ConfigurationManager.getConfigurationManager().getConfiguration().getEnvironmentContext());
        configuration.put("gab", ConfigurationManager.getConfigurationManager().getConfiguration().getGabConfig());
        return configuration;
    }
}
