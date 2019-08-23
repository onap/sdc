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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
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
import org.openecomp.sdc.be.components.impl.ElementBusinessLogic;
import org.openecomp.sdc.be.components.scheduledtasks.ComponentsCleanBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.info.ArtifactTypesInfo;
import org.openecomp.sdc.be.model.ArtifactType;
import org.openecomp.sdc.be.model.Category;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.PropertyScope;
import org.openecomp.sdc.be.model.Tag;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.catalog.CatalogComponent;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.ui.model.UiCategories;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path("/v1/")

/****
 * 
 * UI oriented servlet - to return elements in specific format UI needs
 * 
 *
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@OpenAPIDefinition(info = @Info(title = "Element Servlet",description = "Element Servlet"))
@Singleton
public class ElementServlet extends BeGenericServlet {

    private static final Logger log = Logger.getLogger(ElementServlet.class);
    private final ComponentsCleanBusinessLogic componentsCleanBusinessLogic;
    private final ElementBusinessLogic elementBusinessLogic;
    private final UserBusinessLogic userBusinessLogic;

    @Inject
    public ElementServlet(UserBusinessLogic userBusinessLogic,
        ComponentsUtils componentsUtils,
        ComponentsCleanBusinessLogic componentsCleanBusinessLogic,
        ElementBusinessLogic elementBusinessLogic) {
        super(userBusinessLogic, componentsUtils);
        this.componentsCleanBusinessLogic = componentsCleanBusinessLogic;
        this.elementBusinessLogic = elementBusinessLogic;
        this.userBusinessLogic = userBusinessLogic;
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
    @Operation(description = "Retrieve the list of all resource/service/product categories/sub-categories/groupings",
            method = "GET",
            summary = "Retrieve the list of all resource/service/product categories/sub-categories/groupings.",
                    responses = @ApiResponse(
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Returns categories Ok"),
            @ApiResponse(responseCode = "403", description = "Missing information"),
            @ApiResponse(responseCode = "400", description = "Invalid component type"),
            @ApiResponse(responseCode = "409", description = "Restricted operation"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public Response getComponentCategories(
            @Parameter(description = "allowed values are resources / services/ products", schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME ,
                    ComponentTypeEnum.SERVICE_PARAM_NAME,ComponentTypeEnum.PRODUCT_PARAM_NAME}),required = true)
                     @PathParam(value = "componentType") final String componentType,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @Context final HttpServletRequest request) {

        try {
            Either<List<CategoryDefinition>, ResponseFormat> either =
                    elementBusinessLogic.getAllCategories(componentType, userId);
            if (either.isRight()) {
                log.debug("No categories were found for type {}", componentType);
                return buildErrorResponse(either.right().value());
            } else {
                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), either.left().value());
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Component Categories");
            log.debug("getComponentCategories failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @GET
    @Path("/categories")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve the all resource, service and product categories", method = "GET",
            summary = "Retrieve the all resource, service and product categories", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Returns categories Ok"),
            @ApiResponse(responseCode = "403", description = "Missing information"),
            @ApiResponse(responseCode = "409", description = "Restricted operation"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public Response getAllCategories(@Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        try {
            Either<UiCategories, ResponseFormat> either = elementBusinessLogic.getAllCategories(userId);
            if (either.isRight()) {
                log.debug("No categories were found");
                return buildErrorResponse(either.right().value());
            } else {
                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), either.left().value());
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get All Categories");
            log.debug("getAllCategories failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }


    @POST
    @Path("/category/{componentType}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create new component category", method = "POST",
            summary = "Create new component category")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Category created"),
            @ApiResponse(responseCode = "400", description = "Invalid category data"),
            @ApiResponse(responseCode = "403", description = "USER_ID header is missing"),
            @ApiResponse(responseCode = "409",
                    description = "Category already exists / User not permitted to perform the action"),
            @ApiResponse(responseCode = "500", description = "General Error")})
    public Response createComponentCategory(
            @Parameter(description = "allowed values are resources /services / products",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME ,
                            ComponentTypeEnum.SERVICE_PARAM_NAME,ComponentTypeEnum.PRODUCT_PARAM_NAME}),
                    required = true) @PathParam(value = "componentType") final String componentType,
            @Parameter(description = "Category to be created", required = true) String data,
            @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        try {
            CategoryDefinition category = RepresentationUtils.fromRepresentation(data, CategoryDefinition.class);

            Either<CategoryDefinition, ResponseFormat> createResourceCategory =
                    elementBusinessLogic.createCategory(category, componentType, userId);
            if (createResourceCategory.isRight()) {
                return buildErrorResponse(createResourceCategory.right().value());
            }

            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.CREATED);
            return buildOkResponse(responseFormat, createResourceCategory.left().value());

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create resource category");
            log.debug("createResourceCategory failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

        }
    }

    @DELETE
    @Path("/category/{componentType}/{categoryUniqueId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete component category", method = "DELETE", summary = "Delete component category",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Category.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Category deleted"),
            @ApiResponse(responseCode = "403", description = "USER_ID header is missing"),
            @ApiResponse(responseCode = "409", description = "User not permitted to perform the action"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "General Error")})
    public Response deleteComponentCategory(@PathParam(value = "categoryUniqueId") final String categoryUniqueId,
            @PathParam(value = "componentType") final String componentType, @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        try {
            Either<CategoryDefinition, ResponseFormat> createResourceCategory =
                    elementBusinessLogic.deleteCategory(categoryUniqueId, componentType, userId);

            if (createResourceCategory.isRight()) {
                return buildErrorResponse(createResourceCategory.right().value());
            }
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT);
            return buildOkResponse(responseFormat, null);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create resource category");
            log.debug("createResourceCategory failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

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
    @Operation(description = "Create new component sub-category", method = "POST",
            summary = "Create new component sub-category for existing category")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Subcategory created"),
            @ApiResponse(responseCode = "400", description = "Invalid subcategory data"),
            @ApiResponse(responseCode = "403", description = "USER_ID header is missing"),
            @ApiResponse(responseCode = "404", description = "Parent category wasn't found"),
            @ApiResponse(responseCode = "409",
                    description = "Subcategory already exists / User not permitted to perform the action"),
            @ApiResponse(responseCode = "500", description = "General Error")})
    public Response createComponentSubCategory(
            @Parameter(description = "allowed values are resources / products",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME ,
                            ComponentTypeEnum.PRODUCT_PARAM_NAME}),
                    required = true) @PathParam(value = "componentType") final String componentType,
            @Parameter(description = "Parent category unique ID",
                    required = true) @PathParam(value = "categoryId") final String categoryId,
            @Parameter(description = "Subcategory to be created. \ne.g. {\"name\":\"Resource-subcat\"}",
                    required = true) String data,
            @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        try {
            SubCategoryDefinition subCategory =
                    RepresentationUtils.fromRepresentation(data, SubCategoryDefinition.class);

            Either<SubCategoryDefinition, ResponseFormat> createSubcategory =
                    elementBusinessLogic.createSubCategory(subCategory, componentType, categoryId, userId);
            if (createSubcategory.isRight()) {
                return buildErrorResponse(createSubcategory.right().value());
            }
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.CREATED);
            return buildOkResponse(responseFormat, createSubcategory.left().value());

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create sub-category");
            log.debug("createComponentSubCategory failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

        }
    }

    @DELETE
    @Path("/category/{componentType}/{categoryUniqueId}/subCategory/{subCategoryUniqueId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete component category", method = "DELETE", summary = "Delete component category",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Category.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Category deleted"),
            @ApiResponse(responseCode = "403", description = "USER_ID header is missing"),
            @ApiResponse(responseCode = "409", description = "User not permitted to perform the action"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "General Error")})
    public Response deleteComponentSubCategory(@PathParam(value = "categoryUniqueId") final String categoryUniqueId,
            @PathParam(value = "subCategoryUniqueId") final String subCategoryUniqueId,
            @PathParam(value = "componentType") final String componentType, @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        try {
            Either<SubCategoryDefinition, ResponseFormat> deleteSubResourceCategory =
                    elementBusinessLogic.deleteSubCategory(subCategoryUniqueId, componentType, userId);
            if (deleteSubResourceCategory.isRight()) {
                return buildErrorResponse(deleteSubResourceCategory.right().value());
            }
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT);
            return buildOkResponse(responseFormat, null);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete component category");
            log.debug("deleteComponentSubCategory failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

        }
    }

    /*
     * GROUPINGS
     */
    @POST
    @Path("/category/{componentType}/{categoryId}/subCategory/{subCategoryId}/grouping")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create new component grouping", method = "POST",
            summary = "Create new component grouping for existing sub-category")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Grouping created"),
            @ApiResponse(responseCode = "400", description = "Invalid grouping data"),
            @ApiResponse(responseCode = "403", description = "USER_ID header is missing"),
            @ApiResponse(responseCode = "404", description = "Parent category or subcategory were not found"),
            @ApiResponse(responseCode = "409",
                    description = "Grouping already exists / User not permitted to perform the action"),
            @ApiResponse(responseCode = "500", description = "General Error")})
    public Response createComponentGrouping(
            @Parameter(description = "allowed values are products",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.PRODUCT_PARAM_NAME}),
                    required = true) @PathParam(value = "componentType") final String componentType,
            @Parameter(description = "Parent category unique ID",
                    required = true) @PathParam(value = "categoryId") final String grandParentCategoryId,
            @Parameter(description = "Parent sub-category unique ID",
                    required = true) @PathParam(value = "subCategoryId") final String parentSubCategoryId,
            @Parameter(description = "Subcategory to be created", required = true) String data,
            @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        try {
            GroupingDefinition grouping = RepresentationUtils.fromRepresentation(data, GroupingDefinition.class);

            Either<GroupingDefinition, ResponseFormat> createGrouping = elementBusinessLogic.createGrouping(grouping,
                    componentType, grandParentCategoryId, parentSubCategoryId, userId);
            if (createGrouping.isRight()) {
                return buildErrorResponse(createGrouping.right().value());
            }
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.CREATED);
            return buildOkResponse(responseFormat, createGrouping.left().value());

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create grouping");
            log.debug("createComponentGrouping failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

        }
    }

    @DELETE
    @Path("/category/{componentType}/{categoryUniqueId}/subCategory/{subCategoryUniqueId}/grouping/{groupingUniqueId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete component category", method = "DELETE", summary = "Delete component category",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Category.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Category deleted"),
            @ApiResponse(responseCode = "403", description = "USER_ID header is missing"),
            @ApiResponse(responseCode = "409", description = "User not permitted to perform the action"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "General Error")})
    public Response deleteComponentGrouping(
            @PathParam(value = "categoryUniqueId") final String grandParentCategoryUniqueId,
            @PathParam(value = "subCategoryUniqueId") final String parentSubCategoryUniqueId,
            @PathParam(value = "groupingUniqueId") final String groupingUniqueId,
            @PathParam(value = "componentType") final String componentType, @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        try {
            Either<GroupingDefinition, ResponseFormat> deleteGrouping =
                    elementBusinessLogic.deleteGrouping(groupingUniqueId, componentType, userId);
            if (deleteGrouping.isRight()) {
                return buildErrorResponse(deleteGrouping.right().value());
            }
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT);
            return buildOkResponse(responseFormat, null);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete component grouping");
            log.debug("deleteGrouping failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // retrieve all tags
    @GET
    @Path("/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve all tags", method = "GET", summary = "Retrieve all tags",responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Returns tags Ok"),
            @ApiResponse(responseCode = "404", description = "No tags were found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public Response getTags(@Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(getTags) Start handle request of {}", url);

        try {
            Either<List<Tag>, ActionStatus> either = elementBusinessLogic.getAllTags(userId);
            if (either.isRight() || either.left().value() == null) {
                log.debug("No tags were found");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
            } else {
                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), either.left().value());
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get All Tags");
            log.debug("getAllTags failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // retrieve all property scopes
    @GET
    @Path("/propertyScopes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve all propertyScopes", method = "GET", summary = "Retrieve all propertyScopes",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Returns propertyScopes Ok"),
            @ApiResponse(responseCode = "404", description = "No propertyScopes were found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public Response getPropertyScopes(@Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(getPropertyScopes) Start handle request of {}", url);

        try {
            Either<List<PropertyScope>, ActionStatus> either = elementBusinessLogic.getAllPropertyScopes(userId);
            if (either.isRight() || either.left().value() == null) {
                log.debug("No property scopes were found");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
            } else {
                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), either.left().value());
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Property Scopes Categories");
            log.debug("getPropertyScopes failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // retrieve all artifact types
    @GET
    @Path("/artifactTypes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve all artifactTypes", method = "GET", summary = "Retrieve all artifactTypes",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Returns artifactTypes Ok"),
            @ApiResponse(responseCode = "404", description = "No artifactTypes were found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public Response getArtifactTypes(@Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(GET - getArtifactTypes) Start handle request of {}", url);

        try {
            Either<List<ArtifactType>, ActionStatus> either = elementBusinessLogic.getAllArtifactTypes(userId);
            if (either.isRight() || either.left().value() == null) {
                log.debug("No artifact types were found");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
            } else {

                Integer defaultHeatTimeout = ConfigurationManager.getConfigurationManager().getConfiguration()
                        .getDefaultHeatArtifactTimeoutMinutes();
                ArtifactTypesInfo typesResponse = new ArtifactTypesInfo();
                typesResponse.setArtifactTypes(either.left().value());
                typesResponse.setHeatDefaultTimeout(defaultHeatTimeout);

                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), typesResponse);
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Artifact Types");
            log.debug("getArtifactTypes failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // retrieve all artifact types
    @GET
    @Path("/configuration/ui")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve all artifactTypes", method = "GET", summary = "Retrieve all artifactTypes",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Returns artifactTypes Ok"),
            @ApiResponse(responseCode = "404", description = "No artifactTypes were found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public Response getConfiguration(@Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(getConfiguration) Start handle request of {}", url);

        try {
            Either<List<ArtifactType>, ActionStatus> otherEither = elementBusinessLogic.getAllArtifactTypes(userId);
            Either<Integer, ActionStatus> defaultHeatTimeout = elementBusinessLogic.getDefaultHeatTimeout();
            Either<Map<String, Object>, ActionStatus> deploymentEither =
                    elementBusinessLogic.getAllDeploymentArtifactTypes();
            Either<Map<String, String>, ActionStatus> resourceTypesMap = elementBusinessLogic.getResourceTypesMap();

            if (otherEither.isRight() || otherEither.left().value() == null) {
                log.debug("No other artifact types were found");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
            } else if (deploymentEither.isRight() || deploymentEither.left().value() == null) {
                log.debug("No deployment artifact types were found");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
            } else if (defaultHeatTimeout.isRight() || defaultHeatTimeout.left().value() == null) {
                log.debug("heat default timeout was not found");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
            } else if (resourceTypesMap.isRight() || resourceTypesMap.left().value() == null) {
                log.debug("No resource types were found");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
            } else {
                Map<String, Object> artifacts = new HashMap<>();
                Map<String, Object> configuration = new HashMap<>();

                artifacts.put("other", otherEither.left().value());
                artifacts.put("deployment", deploymentEither.left().value());
                configuration.put("artifacts", artifacts);
                configuration.put("defaultHeatTimeout", defaultHeatTimeout.left().value());
                configuration.put("componentTypes", elementBusinessLogic.getAllComponentTypesParamNames());
                configuration.put("roles", elementBusinessLogic.getAllSupportedRoles());
                configuration.put("resourceTypes", resourceTypesMap.left().value());
                configuration.put("environmentContext",
                        ConfigurationManager.getConfigurationManager().getConfiguration().getEnvironmentContext());
                configuration.put("gab",
                        ConfigurationManager.getConfigurationManager().getConfiguration().getGabConfig());

                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), configuration);
            }

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Artifact Types");
            log.debug("getArtifactTypes failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // retrieve all followed resources and services
    @GET
    @Path("/followed")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve all followed", method = "GET", summary = "Retrieve all followed",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Returns followed Ok"),
            @ApiResponse(responseCode = "404", description = "No followed were found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public Response getFollowedResourcesServices(@Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        Response res = null;
        User userData = null;
        try {
            String url = request.getMethod() + " " + request.getRequestURI();
            log.debug("Start handle request of {}", url);

            // Getting the user
            Either<User, ActionStatus> either = userBusinessLogic.getUser(userId, false);
            if (either.isRight()) {
                // Couldn't find or otherwise fetch the user
                return buildErrorResponse(
                        getComponentsUtils().getResponseFormatByUserId(either.right().value(), userId));
            }

            if (either.left().value() != null) {
                userData = either.left().value();
                Either<Map<String, List<? extends Component>>, ResponseFormat> followedResourcesServices =
                        elementBusinessLogic.getFollowed(userData);
                if (followedResourcesServices.isRight()) {
                    log.debug("failed to get followed resources services ");
                    return buildErrorResponse(followedResourcesServices.right().value());
                }
                Object data = RepresentationUtils.toRepresentation(followedResourcesServices.left().value());
                res = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), data);
            } else {
                res = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Followed Resources / Services Categories");
            log.debug("Getting followed resources/services failed with exception", e);
            res = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        return res;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // retrieve all certified resources and services and their last version
    @GET
    @Path("/screen")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve catalog resources and services", method = "GET",
            summary = "Retrieve catalog resources and services", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Returns resources and services Ok"),
            @ApiResponse(responseCode = "404", description = "No resources and services were found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public Response getCatalogComponents(@Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @QueryParam("excludeTypes") List<OriginTypeEnum> excludeTypes) {

        Response res = null;
        try {
            String url = request.getMethod() + " " + request.getRequestURI();
            log.debug("Start handle request of {}", url);

            Either<Map<String, List<CatalogComponent>>, ResponseFormat> catalogData =
                    elementBusinessLogic.getCatalogComponents(userId, excludeTypes);

            if (catalogData.isRight()) {
                log.debug("failed to get catalog data");
                return buildErrorResponse(catalogData.right().value());
            }
            Object data = RepresentationUtils.toRepresentation(catalogData.left().value());
            res = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), data);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Catalog Components");
            log.debug("Getting catalog components failed with exception", e);
            res = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        return res;
    }

    @DELETE
    @Path("/inactiveComponents/{componentType}")
    public Response deleteMarkedResources(@PathParam("componentType") final String componentType, @Context final HttpServletRequest request) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);

        // get modifier id
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);

        Response response = null;

        NodeTypeEnum nodeType = NodeTypeEnum.getByNameIgnoreCase(componentType);
        if (nodeType == null) {
            log.info("componentType is not valid: {}", componentType);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
        }

        List<NodeTypeEnum> componentsList = new ArrayList<>();
        componentsList.add(nodeType);
        try {
            Map<NodeTypeEnum, Either<List<String>, ResponseFormat>> cleanComponentsResult = componentsCleanBusinessLogic.cleanComponents(componentsList);
            Either<List<String>, ResponseFormat> cleanResult = cleanComponentsResult.get(nodeType);

            if (cleanResult.isRight()) {
                log.debug("failed to delete marked components of type {}", nodeType);
                response = buildErrorResponse(cleanResult.right().value());
                return response;
            }
            response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), cleanResult.left().value());
            return response;

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Marked Components");
            log.debug("delete marked components failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }
    }

    @GET
    @Path("/ecompPortalMenu")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve ecomp portal menu - MOC", method = "GET", summary = "Retrieve ecomp portal menu", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))))
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Retrieve ecomp portal menu") })
    public Response getListOfCsars(@Context final HttpServletRequest request) {
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
                "[{\"menuId\":1,\"column\":2,\"text\":\"Design\",\"parentMenuId\":null,\"url\":\"\",\"appid\":null,\"roles\":null,\"children\":[{\"menuId\":11,\"column\":1,\"text\":\"ProductDesign\",\"parentMenuId\":1,\"url\":\"\",\"appid\":null,\"roles\":null},{\"menuId\":12,\"column\":2,\"text\":\"Service\",\"parentMenuId\":1,\"url\":\"\",\"appid\":null,\"roles\":null,\"children\":[{\"menuId\":21,\"column\":1,\"text\":\"ViewPolicies\",\"parentMenuId\":12,\"url\":\"\",\"appid\":null,\"roles\":null,\"children\":[{\"menuId\":90,\"column\":1,\"text\":\"4thLevelApp1aR16\",\"parentMenuId\":21,\"url\":\"http://google.com\",\"appid\":null,\"roles\":null}]},{\"menuId\":22,\"column\":2,\"text\":\"UpdatePolicies\",\"parentMenuId\":12,\"url\":\"\",\"appid\":null,\"roles\":null,\"children\":[{\"menuId\":91,\"column\":1,\"text\":\"4thLevelApp1bR16\",\"parentMenuId\":22,\"url\":\"http://jsonlint.com/\",\"appid\":null,\"roles\":null}]},{\"menuId\":23,\"column\":3,\"text\":\"UpdateRules\",\"parentMenuId\":12,\"url\":\"\",\"appid\":null,\"roles\":null},{\"menuId\":24,\"column\":4,\"text\":\"CreateSignatures?\",\"parentMenuId\":12,\"url\":\"\",\"appid\":null,\"roles\":null},{\"menuId\":25,\"column\":5,\"text\":\"Definedata\",\"parentMenuId\":12,\"url\":\"\",\"appid\":null,\"roles\":null}]}]}]");
    }

}
