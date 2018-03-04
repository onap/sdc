package org.openecomp.sdc.be.externalapi.servlet;

import fj.data.Either;
import org.openecomp.sdc.be.components.impl.ExternalRefsBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dto.ExternalRefDTO;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.servlets.AbstractValidationsServlet;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;


@Path("/v1/catalog")
@Controller
public class ExternalRefsServlet extends AbstractValidationsServlet {

    private static final Logger log = LoggerFactory.getLogger(ExternalRefsServlet.class);

    private ExternalRefsBusinessLogic businessLogic;

    public ExternalRefsServlet(ExternalRefsBusinessLogic businessLogic, ComponentsUtils componentsUtils){
        this.businessLogic = businessLogic;
        this.componentsUtils = componentsUtils;
    }

    @GET
    @Path("/{assetType}/{uuid}/version/{version}/resourceInstances/{componentInstanceName}/externalReferences/{objectType}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getComponentInstanceExternalRef(
            @Context final HttpServletRequest request,
            @PathParam("assetType") String assetType,
            @PathParam("uuid") String uuid,
            @PathParam("version") String version,
            @PathParam("componentInstanceName") String componentInstanceName,
            @PathParam("objectType") String objectType, @HeaderParam("USER_ID") String userId) {

        log.debug("GET component instance external interfaces {} {} {} {}", assetType, uuid, componentInstanceName, objectType);

        Response r = authorizeAndValidateRequest(request, userId);
        if (r != null){
            return r;
        }

        Either<List<String>, ActionStatus> refsResult = this.businessLogic.getExternalReferences(uuid, version, componentInstanceName, objectType);
        if (refsResult.isLeft()){
            return this.buildOkResponse(refsResult.left().value());
        } else {
            return this.buildExtRefErrorResponse(refsResult.right().value(), uuid, version, componentInstanceName, objectType, "");
        }
    }

    @GET
    @Path("/{assetType}/{uuid}/version/{version}/externalReferences/{objectType}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, List<String>> getAssetExternalRefByObjectType(
            @Context final HttpServletRequest request,
            @PathParam("assetType") String assetType,
            @PathParam("uuid") String uuid,
            @PathParam("version") String version,
            @PathParam("objectType") String objectType, @HeaderParam("USER_ID") String userId) {

        log.debug("GET asset external references {} {} {}", assetType, uuid, objectType);

        Response r = authorizeAndValidateRequest(request, userId);
        if (r != null){
            throw new WebApplicationException(r);
        }

        Either<Map<String, List<String>>, ActionStatus> refsResult = this.businessLogic.getExternalReferences(uuid, version, objectType);
        if (refsResult.isLeft()){
            return refsResult.left().value();
        } else {
            throw new WebApplicationException(this.buildExtRefErrorResponse(refsResult.right().value(), uuid, version, "", objectType, ""));
        }
    }

    @POST
    @Path("/{assetType}/{uuid}/resourceInstances/{componentInstanceName}/externalReferences/{objectType}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addComponentInstanceExternalRef(
            @Context final HttpServletRequest request,
            @PathParam("assetType") String assetType,
            @PathParam("uuid") String uuid,
            @PathParam("componentInstanceName") String componentInstanceName,
            @PathParam("objectType") String objectType, ExternalRefDTO ref, @HeaderParam("USER_ID") String userId) {

        log.debug("POST component instance external interfaces {} {} {} {} {}", assetType, uuid, componentInstanceName, objectType, ref);

        Response r = authorizeAndValidateRequest(request, userId);
        if (r != null){
            return r;
        }

        Either<String, ActionStatus> addResult = this.businessLogic.addExternalReference(uuid, componentInstanceName, objectType, ref);
        if (addResult.isLeft()) {
            return Response.status(Response.Status.CREATED)
                    .entity(ref)
                    .build();
        } else {
            return this.buildExtRefErrorResponse(addResult.right().value(), uuid, "", componentInstanceName, objectType, ref.getReferenceUUID());
        }

    }

    @DELETE
    @Path("/{assetType}/{uuid}/resourceInstances/{componentInstanceName}/externalReferences/{objectType}/{reference}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteComponentInstanceReference(
            @Context final HttpServletRequest request,
            @PathParam("assetType") String assetType,
            @PathParam("uuid") String uuid,
            @PathParam("componentInstanceName") String componentInstanceName,
            @PathParam("objectType") String objectType,
            @PathParam("reference") String reference, @HeaderParam("USER_ID") String userId) {

        log.debug("DELETE component instance external interfaces {} {} {} {}", assetType, uuid, componentInstanceName, objectType);

        Response r = authorizeAndValidateRequest(request, userId);
        if (r != null){
            return r;
        }

        Either<String, ActionStatus> deleteStatus = this.businessLogic.deleteExternalReference(uuid, componentInstanceName, objectType, reference);
        if (deleteStatus.isLeft()){
            return this.buildOkResponse(new ExternalRefDTO(reference));
        } else {
            return this.buildExtRefErrorResponse(deleteStatus.right().value(), uuid, "", componentInstanceName, objectType, reference);
        }
    }

    @PUT
    @Path("/{assetType}/{uuid}/resourceInstances/{componentInstanceName}/externalReferences/{objectType}/{oldRefValue}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateComponentInstanceReference(
            @Context final HttpServletRequest request,
            @PathParam("assetType") String assetType,
            @PathParam("uuid") String uuid,
            @PathParam("componentInstanceName") String componentInstanceName,
            @PathParam("objectType") String objectType,
            @PathParam("oldRefValue") String oldRefValue,
            ExternalRefDTO newRefValueDTO, @HeaderParam("USER_ID") String userId) {

        log.debug("PUT component instance external interfaces {} {} {} {}", assetType, uuid, componentInstanceName, objectType);

        Response r = authorizeAndValidateRequest(request, userId);
        if (r != null){
            return r;
        }

        String newRefValue = newRefValueDTO.getReferenceUUID();
        Either<String, ActionStatus> updateResult = this.businessLogic.updateExternalReference(uuid, componentInstanceName, objectType, oldRefValue, newRefValue);
        if (updateResult.isLeft()){
            return this.buildOkResponse(new ExternalRefDTO(newRefValue));
        } else {
            return this.buildExtRefErrorResponse(updateResult.right().value(), uuid, "", componentInstanceName, objectType, oldRefValue);
        }

    }

    private Response authorizeAndValidateRequest(final HttpServletRequest request, String userId) {
        init(log);

        Wrapper<Response> responseWrapper = new Wrapper<>();
        Wrapper<User> userWrapper = new Wrapper<>();

        //Validate X-ECOMP_INSTANCE_ID_HEADER
        if (request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER) == null || request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER).isEmpty()){
            return this.buildExtRefErrorResponse(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID, "", "", "", "", "");
        }

        String method = request.getMethod();
        if (responseWrapper.isEmpty() && !"GET".equals(method)) {
            validateUserExist(responseWrapper, userWrapper, userId);
            validateUserRole(responseWrapper, userWrapper.getInnerElement());
        }

        return responseWrapper.getInnerElement();
    }

    private Response buildExtRefErrorResponse(ActionStatus status, String uuid, String version, String componentInstanceName, String objectType, String ref){
        switch (status) {
            case RESOURCE_NOT_FOUND:
                return buildErrorResponse(componentsUtils.getResponseFormat(status, uuid));
            case COMPONENT_VERSION_NOT_FOUND:
                return buildErrorResponse(componentsUtils.getResponseFormat(status, uuid, version));
            case COMPONENT_INSTANCE_NOT_FOUND:
                return buildErrorResponse(componentsUtils.getResponseFormat(status, componentInstanceName, uuid));
            case EXT_REF_ALREADY_EXIST:
                return Response.status(Response.Status.OK)
                        .entity(new ExternalRefDTO(ref))
                        .build();
            case EXT_REF_NOT_FOUND:
                return buildErrorResponse(componentsUtils.getResponseFormat(status, objectType + "/" + ref));
            case MISSING_X_ECOMP_INSTANCE_ID:
                return buildErrorResponse(componentsUtils.getResponseFormat(status));
            default:
                return this.buildGeneralErrorResponse();
        }
    }
}
