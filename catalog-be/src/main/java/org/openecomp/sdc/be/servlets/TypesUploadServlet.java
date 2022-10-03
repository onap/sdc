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

import com.google.gson.reflect.TypeToken;
import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.openecomp.sdc.be.components.impl.ArtifactTypeImportManager;
import org.openecomp.sdc.be.components.impl.CapabilityTypeImportManager;
import org.openecomp.sdc.be.components.impl.CategoriesImportManager;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.DataTypeImportManager;
import org.openecomp.sdc.be.components.impl.GroupTypeImportManager;
import org.openecomp.sdc.be.components.impl.InterfaceLifecycleTypeImportManager;
import org.openecomp.sdc.be.components.impl.PolicyTypeImportManager;
import org.openecomp.sdc.be.components.impl.RelationshipTypeImportManager;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.components.impl.model.ToscaTypeImportData;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.RelationshipTypeDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.normatives.ToscaTypeMetadata;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces.ConsumerFourParam;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces.ConsumerTwoParam;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog/uploadType")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "SDCE-2 APIs")
@Server(url = "/sdc2/rest")
@Controller
public class TypesUploadServlet extends AbstractValidationsServlet {

    private static final String CREATE = "Create ";
    private static final String START_HANDLE_REQUEST_OF = "Start handle request of {}";
    private static final String CREATE_FAILED_WITH_EXCEPTION = "create {} failed with exception:";
    private static final Logger log = Logger.getLogger(TypesUploadServlet.class);
    private final CapabilityTypeImportManager capabilityTypeImportManager;
    private final InterfaceLifecycleTypeImportManager interfaceLifecycleTypeImportManager;
    private final CategoriesImportManager categoriesImportManager;
    private final DataTypeImportManager dataTypeImportManager;
    private final GroupTypeImportManager groupTypeImportManager;
    private final PolicyTypeImportManager policyTypeImportManager;
    private final RelationshipTypeImportManager relationshipTypeImportManager;
    private final ArtifactTypeImportManager artifactTypeImportManager;

    @Inject
    public TypesUploadServlet(ComponentInstanceBusinessLogic componentInstanceBL,
                              ComponentsUtils componentsUtils, ServletUtils servletUtils, ResourceImportManager resourceImportManager,
                              CapabilityTypeImportManager capabilityTypeImportManager,
                              InterfaceLifecycleTypeImportManager interfaceLifecycleTypeImportManager,
                              CategoriesImportManager categoriesImportManager, DataTypeImportManager dataTypeImportManager,
                              GroupTypeImportManager groupTypeImportManager, PolicyTypeImportManager policyTypeImportManager,
                              RelationshipTypeImportManager relationshipTypeImportManager, ArtifactTypeImportManager artifactTypeImportManager) {
        super(componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.capabilityTypeImportManager = capabilityTypeImportManager;
        this.interfaceLifecycleTypeImportManager = interfaceLifecycleTypeImportManager;
        this.categoriesImportManager = categoriesImportManager;
        this.dataTypeImportManager = dataTypeImportManager;
        this.groupTypeImportManager = groupTypeImportManager;
        this.policyTypeImportManager = policyTypeImportManager;
        this.relationshipTypeImportManager = relationshipTypeImportManager;
        this.artifactTypeImportManager = artifactTypeImportManager;
    }

    @POST
    @Path("/capability")
    @Operation(description = "Create Capability Type from yaml", method = "POST", summary = "Returns created Capability Type", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Capability Type created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Capability Type already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response uploadCapabilityType(@Parameter(description = "FileInputStream") @FormDataParam("capabilityTypeZip") File file,
                                         @Context final HttpServletRequest request, @HeaderParam("USER_ID") String creator,
                                         @Parameter(description = "model") @FormDataParam("model") String modelName,
                                         @Parameter(description = "includeToModelImport") @FormDataParam("includeToModelImport") boolean includeToModelDefaultImports) {
        ConsumerFourParam<Wrapper<Response>, String, String, Boolean> createElementsMethod = (responseWrapper, ymlPayload, model, includeToModelImport) ->
            createElementsType(responseWrapper, () -> capabilityTypeImportManager.createCapabilityTypes(ymlPayload, modelName,
                includeToModelDefaultImports));
        return uploadElementTypeServletLogic(createElementsMethod, file, request, creator, NodeTypeEnum.CapabilityType.name(), modelName,
            includeToModelDefaultImports);
    }

    @POST
    @Path("/relationship")
    @Operation(description = "Create Relationship Type from yaml", method = "POST", summary = "Returns created Relationship Type", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Relationship Type created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Relationship Type already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response uploadRelationshipType(@Parameter(description = "FileInputStream") @FormDataParam("relationshipTypeZip") File file,
                                           @Context final HttpServletRequest request, @HeaderParam("USER_ID") String creator,
                                           @Parameter(description = "model") @FormDataParam("model") String modelName,
                                           @Parameter(description = "includeToModelImport") @FormDataParam("includeToModelImport") boolean includeToModelDefaultImports) {
        return uploadElementTypeServletLogic(
            this::createRelationshipTypes, file, request, creator, NodeTypeEnum.RelationshipType.getName(), modelName, includeToModelDefaultImports);
    }

    @POST
    @Path("/interfaceLifecycle")
    @Operation(description = "Create Interface Lyfecycle Type from yaml", method = "POST", summary = "Returns created Interface Lifecycle Type", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Interface Lifecycle Type created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Interface Lifecycle Type already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response uploadInterfaceLifecycleType(@Parameter(description = "FileInputStream") @FormDataParam("interfaceLifecycleTypeZip") File file,
                                                 @Context final HttpServletRequest request, @HeaderParam("USER_ID") String creator,
                                                 @Parameter(description = "model") @FormDataParam("model") String modelName,
                                                 @Parameter(description = "includeToModelImport") @FormDataParam("includeToModelImport") boolean includeToModelDefaultImports) {
        ConsumerTwoParam<Wrapper<Response>, String> createElementsMethod = (responseWrapper, ymlPayload) ->
            createElementsType(responseWrapper, () -> interfaceLifecycleTypeImportManager.createLifecycleTypes(ymlPayload, modelName,
                includeToModelDefaultImports));
        return uploadElementTypeServletLogic(createElementsMethod, file, request, creator, "Interface Types");
    }

    @POST
    @Path("/artifactTypes")
    @Operation(description = "Create Tosca Artifact types from yaml", method = "POST", summary = "Returns created Tosca artifact types", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Tosca Artifact types created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Tosca Artifact Type already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response uploadArtifactTypes(@Parameter(description = "Zip file containing a yaml with the TOSCA artifact types definition")
                                        @FormDataParam("artifactsZip") File file,
                                        @Parameter(description = "model name") @FormDataParam("model") String modelName,
                                        @Context final HttpServletRequest request, @HeaderParam("USER_ID") String creator,
                                        @Parameter(description = "A flag to add types to the default imports")
                                        @FormDataParam("includeToModelImport") boolean includeToModelDefaultImports) {
        final ConsumerTwoParam<Wrapper<Response>, String> createElementsMethod = (responseWrapper, ymlPayload) ->
            createElementsType(responseWrapper,
                () -> artifactTypeImportManager.createArtifactTypes(ymlPayload, modelName, includeToModelDefaultImports));
        return uploadElementTypeServletLogic(createElementsMethod, file, request, creator, NodeTypeEnum.ArtifactType.getName());
    }

    @POST
    @Path("/categories")
    @Operation(description = "Create Categories from yaml", method = "POST", summary = "Returns created categories", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Categories created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Category already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response uploadCategories(@Parameter(description = "FileInputStream") @FormDataParam("categoriesZip") File file,
                                     @Context final HttpServletRequest request, @HeaderParam("USER_ID") String creator) {
        ConsumerTwoParam<Wrapper<Response>, String> createElementsMethod = (responseWrapper, ymlPayload) ->
            createElementsType(responseWrapper, () -> categoriesImportManager.createCategories(ymlPayload));
        return uploadElementTypeServletLogic(createElementsMethod, file, request, creator, "categories");
    }

    @POST
    @Path("/datatypes")
    @Operation(description = "Create Categories from yaml", method = "POST", summary = "Returns created data types", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Data types created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Data types already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response uploadDataTypes(@Parameter(description = "FileInputStream") @FormDataParam("dataTypesZip") File file,
                                    @Context final HttpServletRequest request, @HeaderParam("USER_ID") String creator,
                                    @Parameter(description = "model") @FormDataParam("model") String modelName,
                                    @Parameter(description = "includeToModelImport") @FormDataParam("includeToModelImport") boolean includeToModelDefaultImports) {
        return uploadElementTypeServletLogic(this::createDataTypes, file, request, creator, NodeTypeEnum.DataType.getName(), modelName,
            includeToModelDefaultImports);
    }

    @POST
    @Path("/grouptypes")
    @Operation(description = "Create GroupTypes from yaml", method = "POST", summary = "Returns created group types", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "group types created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "group types already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response uploadGroupTypes(@Parameter(description = "toscaTypeMetadata") @FormDataParam("toscaTypeMetadata") String toscaTypesMetaData,
                                     @Parameter(description = "model") @FormDataParam("model") String modelName,
                                     @Parameter(description = "FileInputStream") @FormDataParam("groupTypesZip") File file,
                                     @Context final HttpServletRequest request, @HeaderParam("USER_ID") String creator,
                                     @Parameter(description = "includeToModelImport") @FormDataParam("includeToModelImport") boolean includeToModelDefaultImports) {
        Map<String, ToscaTypeMetadata> typesMetadata = getTypesMetadata(toscaTypesMetaData);
        return uploadTypesWithMetaData(this::createGroupTypes, typesMetadata, file, request, creator, NodeTypeEnum.GroupType.getName(), modelName,
            includeToModelDefaultImports);
    }

    @POST
    @Path("/policytypes")
    @Operation(description = "Create PolicyTypes from yaml", method = "POST", summary = "Returns created policy types", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "policy types created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "policy types already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response uploadPolicyTypes(@Parameter(description = "toscaTypeMetadata") @FormDataParam("toscaTypeMetadata") String toscaTypesMetaData,
                                      @Parameter(description = "model") @FormDataParam("model") String modelName,
                                      @Parameter(description = "FileInputStream") @FormDataParam("policyTypesZip") File file,
                                      @Context final HttpServletRequest request, @HeaderParam("USER_ID") String creator,
                                      @Parameter(description = "includeToModelImport") @FormDataParam("includeToModelImport") boolean includeToModelDefaultImports) {
        Map<String, ToscaTypeMetadata> typesMetadata = getTypesMetadata(toscaTypesMetaData);
        return uploadTypesWithMetaData(this::createPolicyTypes, typesMetadata, file, request, creator, NodeTypeEnum.PolicyType.getName(), modelName,
            includeToModelDefaultImports);
    }

    private Map<String, ToscaTypeMetadata> getTypesMetadata(String toscaTypesMetaData) {
        return gson.fromJson(toscaTypesMetaData, new TypeToken<Map<String, ToscaTypeMetadata>>() {
        }.getType());
    }

    private Response uploadElementTypeServletLogic(ConsumerTwoParam<Wrapper<Response>, String> createElementsMethod, File file,
                                                   final HttpServletRequest request, String creator, String elementTypeName) {
        init();
        String userId = initHeaderParam(creator, request, Constants.USER_ID_HEADER);
        try {
            Wrapper<String> yamlStringWrapper = new Wrapper<>();
            String url = request.getMethod() + " " + request.getRequestURI();
            log.debug(START_HANDLE_REQUEST_OF, url);
            Wrapper<Response> responseWrapper = doUploadTypeValidations(request, userId, file);
            if (responseWrapper.isEmpty()) {
                fillZipContents(yamlStringWrapper, file);
            }
            if (responseWrapper.isEmpty()) {
                createElementsMethod.accept(responseWrapper, yamlStringWrapper.getInnerElement());
            }
            return responseWrapper.getInnerElement();
        } catch (Exception e) {
            log.debug(CREATE_FAILED_WITH_EXCEPTION, elementTypeName, e);
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(CREATE + elementTypeName);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private Response uploadElementTypeServletLogic(final ConsumerFourParam<Wrapper<Response>, String, String, Boolean> createElementsMethod,
                                                   final File file, final HttpServletRequest request, final String creator,
                                                   final String elementTypeName, final String modelName, final boolean includeToModelDefaultImports) {
        init();
        final String userId = initHeaderParam(creator, request, Constants.USER_ID_HEADER);
        try {
            final Wrapper<String> yamlStringWrapper = new Wrapper<>();
            final String url = request.getMethod() + " " + request.getRequestURI();
            log.debug(START_HANDLE_REQUEST_OF, url);
            final Wrapper<Response> responseWrapper = doUploadTypeValidations(request, userId, file);
            if (responseWrapper.isEmpty()) {
                fillZipContents(yamlStringWrapper, file);
            }
            if (responseWrapper.isEmpty()) {
                createElementsMethod.accept(responseWrapper, yamlStringWrapper.getInnerElement(), modelName, includeToModelDefaultImports);
            }
            return responseWrapper.getInnerElement();
        } catch (final Exception e) {
            log.debug(CREATE_FAILED_WITH_EXCEPTION, elementTypeName, e);
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(CREATE + elementTypeName);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private Wrapper<Response> doUploadTypeValidations(final HttpServletRequest request, String userId, File file) {
        Wrapper<Response> responseWrapper = new Wrapper<>();
        Wrapper<User> userWrapper = new Wrapper<>();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        validateUserExist(responseWrapper, userWrapper, userId);
        if (responseWrapper.isEmpty()) {
            validateUserRole(responseWrapper, userWrapper.getInnerElement());
        }
        if (responseWrapper.isEmpty()) {
            validateDataNotNull(responseWrapper, file);
        }
        return responseWrapper;
    }

    private Response uploadTypesWithMetaData(ConsumerFourParam<Wrapper<Response>, ToscaTypeImportData, String, Boolean> createElementsMethod,
                                             Map<String, ToscaTypeMetadata> typesMetaData, File file, final HttpServletRequest request,
                                             String creator, String elementTypeName, String modelName, final boolean includeToModelDefaultImports) {
        init();
        String userId = initHeaderParam(creator, request, Constants.USER_ID_HEADER);
        Wrapper<String> yamlStringWrapper = new Wrapper<>();
        try {
            Wrapper<Response> responseWrapper = doUploadTypeValidations(request, userId, file);
            if (responseWrapper.isEmpty()) {
                fillZipContents(yamlStringWrapper, file);
            }
            if (responseWrapper.isEmpty()) {
                ToscaTypeImportData toscaTypeImportData = new ToscaTypeImportData(yamlStringWrapper.getInnerElement(), typesMetaData);
                createElementsMethod.accept(responseWrapper, toscaTypeImportData, modelName, includeToModelDefaultImports);
            }
            return responseWrapper.getInnerElement();
        } catch (Exception e) {
            log.debug(CREATE_FAILED_WITH_EXCEPTION, elementTypeName, e);
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(CREATE + elementTypeName);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private <T> void createElementsType(Wrapper<Response> responseWrapper, Supplier<Either<T, ResponseFormat>> elementsCreater) {
        Either<T, ResponseFormat> eitherResult = elementsCreater.get();
        if (eitherResult.isRight()) {
            responseWrapper.setInnerElement(buildErrorResponse(eitherResult.right().value()));
        } else {
            try {
                Response response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED),
                    RepresentationUtils.toRepresentation(eitherResult.left().value()));
                responseWrapper.setInnerElement(response);
            } catch (Exception e) {
                responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
                log.error("#createElementsType - json serialization failed with error: ", e);
            }
        }
    }

    // data types
    private void createDataTypes(Wrapper<Response> responseWrapper, String dataTypesYml, final String modelName,
                                 final boolean includeToModelDefaultImports) {
        final Supplier<Either<List<ImmutablePair<DataTypeDefinition, Boolean>>, ResponseFormat>> generateElementTypeFromYml = () ->
            dataTypeImportManager.createDataTypes(dataTypesYml, modelName, includeToModelDefaultImports);
        buildStatusForElementTypeCreate(responseWrapper, generateElementTypeFromYml, ActionStatus.DATA_TYPE_ALREADY_EXIST,
            NodeTypeEnum.DataType.name());
    }

    // group types
    private void createGroupTypes(Wrapper<Response> responseWrapper, ToscaTypeImportData toscaTypeImportData, String modelName,
                                  final boolean includeToModelDefaultImports) {
        final Supplier<Either<List<ImmutablePair<GroupTypeDefinition, Boolean>>, ResponseFormat>> generateElementTypeFromYml = () ->
            groupTypeImportManager.createGroupTypes(toscaTypeImportData, modelName, includeToModelDefaultImports);
        buildStatusForElementTypeCreate(responseWrapper, generateElementTypeFromYml, ActionStatus.GROUP_TYPE_ALREADY_EXIST,
            NodeTypeEnum.GroupType.name());
    }

    // policy types
    private void createPolicyTypes(Wrapper<Response> responseWrapper, ToscaTypeImportData toscaTypeImportData, String modelName,
                                   final boolean includeToModelDefaultImports) {
        final Supplier<Either<List<ImmutablePair<PolicyTypeDefinition, Boolean>>, ResponseFormat>> generateElementTypeFromYml = () ->
            policyTypeImportManager.createPolicyTypes(toscaTypeImportData, modelName, includeToModelDefaultImports);
        buildStatusForElementTypeCreate(responseWrapper, generateElementTypeFromYml, ActionStatus.POLICY_TYPE_ALREADY_EXIST,
            NodeTypeEnum.PolicyType.name());
    }

    // data types
    private <T extends ToscaDataDefinition> void buildStatusForElementTypeCreate(Wrapper<Response> responseWrapper,
                                                                                 Supplier<Either<List<ImmutablePair<T, Boolean>>, ResponseFormat>> generateElementTypeFromYml,
                                                                                 ActionStatus alreadyExistStatus, String elementTypeName) {
        Either<List<ImmutablePair<T, Boolean>>, ResponseFormat> eitherResult = generateElementTypeFromYml.get();
        if (eitherResult.isRight()) {
            responseWrapper.setInnerElement(buildErrorResponse(eitherResult.right().value()));
        } else {

            try {
                final List<ImmutablePair<T, Boolean>> list = eitherResult.left().value();
                ActionStatus status = ActionStatus.OK;
                if (list != null) {
                    // Group result by the right value - true or false.
                    // I.e., get the number of data types which are new and which are old.
                    final Map<Boolean, List<ImmutablePair<T, Boolean>>> collect =
                        list.stream().collect(Collectors.groupingBy(ImmutablePair<T, Boolean>::getRight));
                    if (collect != null) {
                        Set<Boolean> keySet = collect.keySet();
                        if (keySet.size() == 1) {
                            Boolean isNew = keySet.iterator().next();
                            if (Boolean.TRUE.equals(isNew)) {
                                // all data types created at the first time
                                status = ActionStatus.CREATED;
                            } else {
                                // All data types already exists
                                status = alreadyExistStatus;
                            }
                        }
                    }
                }
                final Object representation = RepresentationUtils.toRepresentation(eitherResult.left().value());
                responseWrapper.setInnerElement(buildOkResponse(getComponentsUtils().getResponseFormat(status), representation));
            } catch (IOException e) {
                BeEcompErrorManager.getInstance().logBeRestApiGeneralError(CREATE + elementTypeName);
                log.debug("failed to convert {} to json", elementTypeName, e);
                responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
            }
        }
    }

    // relationship types
    private void createRelationshipTypes(final Wrapper<Response> responseWrapper,
                                         final String relationshipTypesYml,
                                         final String modelName,
                                         final boolean includeToModelDefaultImports) {
        final Supplier<Either<List<ImmutablePair<RelationshipTypeDefinition, Boolean>>, ResponseFormat>> generateElementTypeFromYml = () -> relationshipTypeImportManager
            .createRelationshipTypes(relationshipTypesYml, modelName, includeToModelDefaultImports);
        buildStatusForElementTypeCreate(responseWrapper, generateElementTypeFromYml, ActionStatus.RELATIONSHIP_TYPE_ALREADY_EXIST,
            NodeTypeEnum.RelationshipType.name());
    }
}
