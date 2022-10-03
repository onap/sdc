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
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ModelBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.exception.BusinessException;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.NodeTypesMetadataList;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.ModelOperationExceptionSupplier;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

/**
 * Root resource (exposed at "/" path)
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog/upload")
@Tag(name = "SDCE-2 APIs")
@Server(url = "/sdc2/rest")
@Controller
public class ResourceUploadServlet extends AbstractValidationsServlet {

    public static final String NORMATIVE_TYPE_RESOURCE = "multipart";
    public static final String CSAR_TYPE_RESOURCE = "csar";
    public static final String USER_TYPE_RESOURCE = "user-resource";
    public static final String USER_TYPE_RESOURCE_UI_IMPORT = "user-resource-ui-import";
    private static final Logger log = LoggerFactory.getLogger(ResourceUploadServlet.class);

    private final ModelBusinessLogic modelBusinessLogic;

    @Inject
    public ResourceUploadServlet(ComponentInstanceBusinessLogic componentInstanceBL,
                                 ComponentsUtils componentsUtils, ServletUtils servletUtils, ResourceImportManager resourceImportManager,
                                 ModelBusinessLogic modelBusinessLogic) {
        super(componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.modelBusinessLogic = modelBusinessLogic;
    }

    @POST
    @Path("/{resourceAuthority}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create Resource from yaml", method = "POST", summary = "Returns created resource", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Resource created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Resource already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response uploadMultipart(
        @Parameter(description = "validValues: normative-resource / user-resource", schema = @Schema(allowableValues = {NORMATIVE_TYPE_RESOURCE,
            USER_TYPE_RESOURCE, USER_TYPE_RESOURCE_UI_IMPORT})) @PathParam(value = "resourceAuthority") final String resourceAuthority,
        @Parameter(description = "FileInputStream") @FormDataParam("resourceZip") File file,
        @Parameter(description = "ContentDisposition") @FormDataParam("resourceZip") FormDataContentDisposition contentDispositionHeader,
        @Parameter(description = "resourceMetadata") @FormDataParam("resourceMetadata") String resourceInfoJsonString,
        @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
        // updateResource Query Parameter if false checks if already exist
        @DefaultValue("true") @QueryParam("createNewVersion") boolean createNewVersion) {
        try {
            Wrapper<Response> responseWrapper = new Wrapper<>();
            Wrapper<User> userWrapper = new Wrapper<>();
            Wrapper<UploadResourceInfo> uploadResourceInfoWrapper = new Wrapper<>();
            Wrapper<String> yamlStringWrapper = new Wrapper<>();
            String url = request.getMethod() + " " + request.getRequestURI();
            log.debug("Start handle request of {}", url);
            // When we get an errorResponse it will be filled into the responseWrapper
            validateAuthorityType(responseWrapper, resourceAuthority);
            ResourceAuthorityTypeEnum resourceAuthorityEnum = ResourceAuthorityTypeEnum.findByUrlPath(resourceAuthority);
            commonGeneralValidations(responseWrapper, userWrapper, uploadResourceInfoWrapper, resourceAuthorityEnum, userId, resourceInfoJsonString);
            final String modelNameToBeAssociated = uploadResourceInfoWrapper.getInnerElement().getModel();
            if (modelNameToBeAssociated != null) {
                log.debug("Model Name to be validated {}", modelNameToBeAssociated);
                validateModel(modelNameToBeAssociated);
            }
            fillPayload(responseWrapper, uploadResourceInfoWrapper, yamlStringWrapper, userWrapper.getInnerElement(), resourceInfoJsonString,
                resourceAuthorityEnum, file);
            // PayLoad Validations
            if (resourceAuthorityEnum != ResourceAuthorityTypeEnum.CSAR_TYPE_BE) {
                commonPayloadValidations(responseWrapper, yamlStringWrapper, userWrapper.getInnerElement(),
                    uploadResourceInfoWrapper.getInnerElement());
                specificResourceAuthorityValidations(responseWrapper, uploadResourceInfoWrapper, yamlStringWrapper, userWrapper.getInnerElement(),
                    request, resourceInfoJsonString, resourceAuthorityEnum);
            }
            if (responseWrapper.isEmpty()) {
                handleImport(responseWrapper, userWrapper.getInnerElement(), uploadResourceInfoWrapper.getInnerElement(),
                    yamlStringWrapper.getInnerElement(), resourceAuthorityEnum, createNewVersion, null);
            }
            return responseWrapper.getInnerElement();
        } catch (final BusinessException e) {
            throw e;
        } catch (final Exception e) {
            var errorMsg = String.format("Unexpected error while uploading Resource '%s'", resourceInfoJsonString);
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(errorMsg);
            log.error(errorMsg, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @POST
    @Path("/resource/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    @Operation(description = "Import node types from a TOSCA yaml, along with the types metadata", method = "POST",
        summary = "Creates node types from a TOSCA yaml file", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Resources created"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "409", description = "One of the resources already exists")}
    )
    public Response bulkImport(@Parameter(description = "The nodes metadata JSON", required = true)
                               @NotNull @FormDataParam("nodeTypeMetadataJson") final NodeTypesMetadataList nodeTypeMetadata,
                               @Parameter(description = "The node types TOSCA definition yaml", required = true)
                               @NotNull @FormDataParam("nodeTypesYaml") final InputStream nodeTypesYamlInputStream,
                               @Parameter(description = "The model name to associate the node types to")
                               @DefaultValue("true") @FormDataParam("createNewVersion") boolean createNewVersion,
                               @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                               @Context final HttpServletRequest request) {
        userId = ValidationUtils.sanitizeInputString(userId);
        final Either<User, ResponseFormat> userEither = getUser(request, userId);
        if (userEither.isRight()) {
            return buildErrorResponse(userEither.right().value());
        }

        final User user = userEither.left().value();

        final String nodeTypesYamlString;
        try {
            nodeTypesYamlString = new String(nodeTypesYamlInputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            var errorMsg = "Could not read the given node types yaml";
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(errorMsg);
            log.error(errorMsg, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_NODE_TYPES_YAML));
        }

        try {
            resourceImportManager
                .importAllNormativeResource(nodeTypesYamlString, nodeTypeMetadata, user, createNewVersion, false);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), null);
        } catch (final BusinessException e) {
            throw e;
        } catch (final Exception e) {
            var errorMsg = "Unexpected error while importing the node types";
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(errorMsg);
            log.error(errorMsg, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    /**
     * The Model field is an optional entry when uploading a resource. If the field is present, it validates if the Model name exists.
     *
     * @param modelName Model names declared on the resource json representation
     */
    private void validateModel(final String modelName) {
        if (modelBusinessLogic.findModel(modelName).isEmpty()) {
            log.error("Could not find model name {}", modelName);
            throw ModelOperationExceptionSupplier.invalidModel(modelName).get();
        }
    }

    public enum ResourceAuthorityTypeEnum {
        // @formatter:off
        NORMATIVE_TYPE_BE(NORMATIVE_TYPE_RESOURCE, true, false),
        USER_TYPE_BE(USER_TYPE_RESOURCE, true, true),
        USER_TYPE_UI(USER_TYPE_RESOURCE_UI_IMPORT, false, true),
        CSAR_TYPE_BE(CSAR_TYPE_RESOURCE, true, true);
        // @formatter:on

        private String urlPath;
        private boolean isBackEndImport;
        private boolean isUserTypeResource;

        public static ResourceAuthorityTypeEnum findByUrlPath(String urlPath) {
            ResourceAuthorityTypeEnum found = null;
            for (ResourceAuthorityTypeEnum curr : ResourceAuthorityTypeEnum.values()) {
                if (curr.getUrlPath().equals(urlPath)) {
                    found = curr;
                    break;
                }
            }
            return found;
        }

        ResourceAuthorityTypeEnum(String urlPath, boolean isBackEndImport, boolean isUserTypeResource) {
            this.urlPath = urlPath;
            this.isBackEndImport = isBackEndImport;
            this.isUserTypeResource = isUserTypeResource;
        }

        public String getUrlPath() {
            return urlPath;
        }

        public boolean isBackEndImport() {
            return isBackEndImport;
        }

        public boolean isUserTypeResource() {
            return isUserTypeResource;
        }
    }
}
