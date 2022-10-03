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

import com.google.common.annotations.VisibleForTesting;
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
import java.io.File;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.openecomp.sdc.be.components.impl.CommonImportManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.components.validation.AccessValidations;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.AnnotationTypeDefinition;
import org.openecomp.sdc.be.model.operations.impl.AnnotationTypeOperations;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.zip.exception.ZipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

/**
 * Here new APIs for types upload written in an attempt to gradually servlet code
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog/uploadType")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)
@Tags({@Tag(name = "SDCE-2 APIs")})
@Servers({@Server(url = "/sdc2/rest")})
@Controller
public class TypesUploadEndpoint extends BeGenericServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(TypesUploadEndpoint.class);
    private final CommonImportManager commonImportManager;
    private final AnnotationTypeOperations annotationTypeOperations;
    private final AccessValidations accessValidations;

    public TypesUploadEndpoint(ComponentsUtils componentsUtils, CommonImportManager commonImportManager,
                               AnnotationTypeOperations annotationTypeOperations, AccessValidations accessValidations) {
        super(componentsUtils);
        this.commonImportManager = commonImportManager;
        this.annotationTypeOperations = annotationTypeOperations;
        this.accessValidations = accessValidations;
    }

    @VisibleForTesting
    static <T extends ToscaDataDefinition> HttpStatus getHttpStatus(List<ImmutablePair<T, Boolean>> typesResults) {
        boolean typeActionFailed = false;
        boolean typeExists = false;
        boolean typeActionSucceeded = false;
        for (ImmutablePair<T, Boolean> typeResult : typesResults) {
            Boolean result = typeResult.getRight();
            if (result == null) {
                typeExists = true;
            } else if (result) {
                typeActionSucceeded = true;
            } else {
                typeActionFailed = true;
            }
        }
        HttpStatus status = HttpStatus.OK;
        if (typeActionFailed) {
            status = HttpStatus.BAD_REQUEST;
        } else if (typeActionSucceeded) {
            status = HttpStatus.CREATED;
        } else if (typeExists) {
            status = HttpStatus.CONFLICT;
        }
        return status;
    }

    private static <T extends ToscaDataDefinition> T buildAnnotationFromFieldMap(String typeName, Map<String, Object> toscaJson) {
        AnnotationTypeDefinition annotationType = new AnnotationTypeDefinition();
        annotationType.setVersion(TypeUtils.getFirstCertifiedVersionVersion());
        annotationType.setHighestVersion(true);
        annotationType.setType(typeName);
        TypeUtils.setField(toscaJson, TypeUtils.ToscaTagNamesEnum.DESCRIPTION, annotationType::setDescription);
        CommonImportManager.setProperties(toscaJson, annotationType::setProperties);
        return (T) annotationType;
    }

    @POST
    @Path("/annotationtypes")
    @Operation(description = "Create AnnotationTypes from yaml", method = "POST", summary = "Returns created annotation types", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "annotation types created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "annotation types already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response uploadAnnotationTypes(@Parameter(description = "FileInputStream") @FormDataParam("annotationTypesZip") File file,
                                          @HeaderParam("USER_ID") String userId) {
        accessValidations.validateUserExists(userId, "Annotation Types Creation");
        final Wrapper<String> yamlStringWrapper = new Wrapper<>();
        try {
            AbstractValidationsServlet.extractZipContents(yamlStringWrapper, file);
        } catch (final ZipException e) {
            LOGGER.error("Could not extract zip contents", e);
        }
        List<ImmutablePair<AnnotationTypeDefinition, Boolean>> typesResults = commonImportManager
            .createElementTypes(yamlStringWrapper.getInnerElement(), TypesUploadEndpoint::buildAnnotationFromFieldMap, annotationTypeOperations);
        HttpStatus status = getHttpStatus(typesResults);
        return Response.status(status.value()).entity(typesResults).build();
    }
}
