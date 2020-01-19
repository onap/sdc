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

import java.io.File;
import java.io.FileNotFoundException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
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
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
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
import org.openecomp.sdc.common.zip.exception.ZipException;
import org.springframework.stereotype.Controller;

/**
 * Root resource (exposed at "/" path)
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog/upload")
@OpenAPIDefinition(info = @Info(title = "Resources Catalog Upload", description = "Upload resource yaml"))
@Controller
public class ResourceUploadServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(ResourceUploadServlet.class);
    public static final String NORMATIVE_TYPE_RESOURCE = "multipart";
    public static final String CSAR_TYPE_RESOURCE = "csar";
    public static final String USER_TYPE_RESOURCE = "user-resource";
    public static final String USER_TYPE_RESOURCE_UI_IMPORT = "user-resource-ui-import";

    @Inject
    public ResourceUploadServlet(UserBusinessLogic userBusinessLogic,
        ComponentInstanceBusinessLogic componentInstanceBL,
        ComponentsUtils componentsUtils, ServletUtils servletUtils,
        ResourceImportManager resourceImportManager) {
        super(userBusinessLogic, componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
    }

    public enum ResourceAuthorityTypeEnum {
        NORMATIVE_TYPE_BE(NORMATIVE_TYPE_RESOURCE, true, false), USER_TYPE_BE(USER_TYPE_RESOURCE, true, true), USER_TYPE_UI(USER_TYPE_RESOURCE_UI_IMPORT, false, true), CSAR_TYPE_BE(CSAR_TYPE_RESOURCE, true, true);

        private String urlPath;
        private boolean isBackEndImport, isUserTypeResource;

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

        private ResourceAuthorityTypeEnum(String urlPath, boolean isBackEndImport, boolean isUserTypeResource) {
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

    @POST
    @Path("/{resourceAuthority}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create Resource from yaml", method = "POST", summary = "Returns created resource",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Resource created"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
            @ApiResponse(responseCode = "409", description = "Resource already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response uploadMultipart(
            @Parameter(description = "validValues: normative-resource / user-resource",
                    schema = @Schema(allowableValues = {NORMATIVE_TYPE_RESOURCE ,
                            USER_TYPE_RESOURCE,USER_TYPE_RESOURCE_UI_IMPORT})) @PathParam(
                                    value = "resourceAuthority") final String resourceAuthority,
            @Parameter(description = "FileInputStream") @FormDataParam("resourceZip") File file,
            @Parameter(description = "ContentDisposition") @FormDataParam("resourceZip") FormDataContentDisposition contentDispositionHeader,
            @Parameter(description = "resourceMetadata") @FormDataParam("resourceMetadata") String resourceInfoJsonString,
            @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            // updateResourse Query Parameter if false checks if already exist
            @DefaultValue("true") @QueryParam("createNewVersion") boolean createNewVersion) throws FileNotFoundException, ZipException {

        try {

            Wrapper<Response> responseWrapper = new Wrapper<>();
            Wrapper<User> userWrapper = new Wrapper<>();
            Wrapper<UploadResourceInfo> uploadResourceInfoWrapper = new Wrapper<>();
            Wrapper<String> yamlStringWrapper = new Wrapper<>();

            String url = request.getMethod() + " " + request.getRequestURI();
            log.debug("Start handle request of {}", url);

            // When we get an errorResponse it will be filled into the
            // responseWrapper
            validateAuthorityType(responseWrapper, resourceAuthority);

            ResourceAuthorityTypeEnum resourceAuthorityEnum = ResourceAuthorityTypeEnum.findByUrlPath(resourceAuthority);

            commonGeneralValidations(responseWrapper, userWrapper, uploadResourceInfoWrapper, resourceAuthorityEnum, userId, resourceInfoJsonString);

            fillPayload(responseWrapper, uploadResourceInfoWrapper, yamlStringWrapper, userWrapper.getInnerElement(), resourceInfoJsonString, resourceAuthorityEnum, file);

            // PayLoad Validations
            if(resourceAuthorityEnum != ResourceAuthorityTypeEnum.CSAR_TYPE_BE){
                commonPayloadValidations(responseWrapper, yamlStringWrapper, userWrapper.getInnerElement(), uploadResourceInfoWrapper.getInnerElement());

                specificResourceAuthorityValidations(responseWrapper, uploadResourceInfoWrapper, yamlStringWrapper, userWrapper.getInnerElement(), request, resourceInfoJsonString, resourceAuthorityEnum);
            }

            if (responseWrapper.isEmpty()) {
                handleImport(responseWrapper, userWrapper.getInnerElement(), uploadResourceInfoWrapper.getInnerElement(), yamlStringWrapper.getInnerElement(), resourceAuthorityEnum, createNewVersion, null);
            }

            return responseWrapper.getInnerElement();

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Upload Resource");
            log.debug("upload resource failed with exception", e);
            throw e;
        }
    }

}
