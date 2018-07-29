package org.openecomp.sdc.be.servlets;

import com.google.common.annotations.VisibleForTesting;
import com.jcabi.aspects.Loggable;
import io.swagger.annotations.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.openecomp.sdc.be.components.impl.CommonImportManager;
import org.openecomp.sdc.be.components.validation.AccessValidations;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.AnnotationTypeDefinition;
import org.openecomp.sdc.be.model.operations.impl.AnnotationTypeOperations;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Here new APIs for types upload written in an attempt to gradually servlet code
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog/uploadType")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Catalog Types Upload")
@Controller
public class TypesUploadEndpoint {

    private final CommonImportManager commonImportManager;
    private final AnnotationTypeOperations annotationTypeOperations;
    private final AccessValidations accessValidations;

    public TypesUploadEndpoint(CommonImportManager commonImportManager, AnnotationTypeOperations annotationTypeOperations, AccessValidations accessValidations) {
        this.commonImportManager = commonImportManager;
        this.annotationTypeOperations = annotationTypeOperations;
        this.accessValidations = accessValidations;
    }

    @POST
    @Path("/annotationtypes")
    @ApiOperation(value = "Create AnnotationTypes from yaml", httpMethod = "POST", notes = "Returns created annotation types", response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "annotation types created"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 409, message = "annotation types already exist")})
    public Response uploadAnnotationTypes(
            @ApiParam("FileInputStream") @FormDataParam("annotationTypesZip") File file,
            @HeaderParam("USER_ID") String userId) throws IOException {
        accessValidations.validateUserExists(userId, "Annotation Types Creation");
        Wrapper<String> yamlStringWrapper = new Wrapper<>();
        AbstractValidationsServlet.extractZipContents(yamlStringWrapper, file);
        List<ImmutablePair<AnnotationTypeDefinition, Boolean>> typesResults = commonImportManager.createElementTypes(yamlStringWrapper.getInnerElement(), TypesUploadEndpoint::buildAnnotationFromFieldMap, annotationTypeOperations);
        HttpStatus status = getHttpStatus(typesResults);
        return Response.status(status.value())
                .entity(typesResults)
                .build();
    }

    @VisibleForTesting
    static <T extends ToscaDataDefinition> HttpStatus getHttpStatus(List<ImmutablePair<T, Boolean>> typesResults) {
        boolean typeActionFailed = false;
        boolean typeExists = false;
        boolean typeActionSucceeded = false;
        for (ImmutablePair<T, Boolean> typeResult : typesResults) {
            Boolean result = typeResult.getRight();
            if (result==null) {
                typeExists = true;
            } else if (result) {
                typeActionSucceeded = true;
            } else {
                typeActionFailed = true;
            }
        }
        HttpStatus status = HttpStatus.OK;
        if (typeActionFailed) {
            status =  HttpStatus.BAD_REQUEST;
        } else if (typeActionSucceeded) {
            status = HttpStatus.CREATED;
        } else if (typeExists) {
            status = HttpStatus.CONFLICT;
        }
        return status;
    }

    private static <T extends ToscaDataDefinition> T buildAnnotationFromFieldMap(String typeName, Map<String, Object> toscaJson) {
        AnnotationTypeDefinition annotationType = new AnnotationTypeDefinition();
        annotationType.setVersion(TypeUtils.FIRST_CERTIFIED_VERSION_VERSION);
        annotationType.setHighestVersion(true);
        annotationType.setType(typeName);
        TypeUtils.setField(toscaJson, TypeUtils.ToscaTagNamesEnum.DESCRIPTION, annotationType::setDescription);
        CommonImportManager.setProperties(toscaJson, annotationType::setProperties);
        return (T) annotationType;
    }


}
