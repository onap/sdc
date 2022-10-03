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
package org.openecomp.sdc.be.externalapi.servlet;

import fj.data.Either;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.components.impl.ExternalRefsBusinessLogic;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.dto.ExternalRefDTO;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.servlets.BeGenericServlet;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Controller;

@Path("/v1/catalog")
@Tag(name = "SDCE-7 APIs")
@Server(url = "/sdc")
@Controller
public class ExternalRefsServlet extends BeGenericServlet {

    private static final Logger log = Logger.getLogger(ExternalRefsServlet.class);
    private final ExternalRefsBusinessLogic businessLogic;

    @Inject
    public ExternalRefsServlet(ComponentsUtils componentsUtils,
                               ExternalRefsBusinessLogic externalRefsBusinessLogic) {
        super(componentsUtils);
        this.businessLogic = externalRefsBusinessLogic;
    }

    @GET
    @Path("/{assetType}/{uuid}/version/{version}/resourceInstances/{componentInstanceName}/externalReferences/{objectType}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionAllowed({AafPermission.PermNames.READ_VALUE})
    public Response getComponentInstanceExternalRef(@PathParam("assetType") String assetType, @PathParam("uuid") String uuid,
                                                    @PathParam("version") String version,
                                                    @PathParam("componentInstanceName") String componentInstanceName,
                                                    @PathParam("objectType") String objectType, @HeaderParam("USER_ID") String userId,
                                                    @HeaderParam("X-ECOMP-InstanceID") String xEcompInstanceId) {
        log.debug("GET component instance external interfaces {} {} {} {}", assetType, uuid, componentInstanceName, objectType);
        Response r = validateRequest(xEcompInstanceId);
        if (r != null) {
            return r;
        }
        Either<List<String>, ActionStatus> refsResult = this.businessLogic.getExternalReferences(uuid, version, componentInstanceName, objectType);
        if (refsResult.isLeft()) {
            return this.buildOkResponse(refsResult.left().value());
        } else {
            return this.buildExtRefErrorResponse(refsResult.right().value(), uuid, version, componentInstanceName, objectType, "");
        }
    }

    @GET
    @Path("/{assetType}/{uuid}/version/{version}/externalReferences/{objectType}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionAllowed({AafPermission.PermNames.READ_VALUE})
    public Map<String, List<String>> getAssetExternalRefByObjectType(@PathParam("assetType") String assetType, @PathParam("uuid") String uuid,
                                                                     @PathParam("version") String version, @PathParam("objectType") String objectType,
                                                                     @HeaderParam("USER_ID") String userId,
                                                                     @HeaderParam("X-ECOMP-InstanceID") String xEcompInstanceId) {
        log.debug("GET asset external references {} {} {}", assetType, uuid, objectType);
        Response r = validateRequest(xEcompInstanceId);
        if (r != null) {
            throw new WebApplicationException(r);
        }
        Either<Map<String, List<String>>, ActionStatus> refsResult = this.businessLogic.getExternalReferences(uuid, version, objectType);
        if (refsResult.isLeft()) {
            return refsResult.left().value();
        } else {
            throw new WebApplicationException(this.buildExtRefErrorResponse(refsResult.right().value(), uuid, version, "", objectType, ""));
        }
    }

    @POST
    @Path("/{assetType}/{uuid}/resourceInstances/{componentInstanceName}/externalReferences/{objectType}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionAllowed({AafPermission.PermNames.WRITE_VALUE})
    public Response addComponentInstanceExternalRef(@PathParam("assetType") String assetType, @PathParam("uuid") String uuid,
                                                    @PathParam("componentInstanceName") String componentInstanceName,
                                                    @PathParam("objectType") String objectType, ExternalRefDTO ref,
                                                    @HeaderParam("USER_ID") String userId,
                                                    @HeaderParam("X-ECOMP-InstanceID") String xEcompInstanceId) {
        log.debug("POST component instance external interfaces {} {} {} {} {}", assetType, uuid, componentInstanceName, objectType, ref);
        Response r = validateRequest(xEcompInstanceId);
        if (r != null) {
            return r;
        }
        ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
        String uid = this.businessLogic.fetchComponentUniqueIdByUuid(uuid, componentType);
        Either<String, ActionStatus> addResult = this.businessLogic
            .addExternalReference(uid, componentType, userId, componentInstanceName, objectType, ref);
        if (addResult.isLeft()) {
            return Response.status(Response.Status.CREATED).entity(ref).build();
        } else {
            return this.buildExtRefErrorResponse(addResult.right().value(), uuid, "", componentInstanceName, objectType, ref.getReferenceUUID());
        }
    }

    @DELETE
    @Path("/{assetType}/{uuid}/resourceInstances/{componentInstanceName}/externalReferences/{objectType}/{reference}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionAllowed({AafPermission.PermNames.DELETE_VALUE})
    public Response deleteComponentInstanceReference(@PathParam("assetType") String assetType, @PathParam("uuid") String uuid,
                                                     @PathParam("componentInstanceName") String componentInstanceName,
                                                     @PathParam("objectType") String objectType, @PathParam("reference") String reference,
                                                     @HeaderParam("USER_ID") String userId,
                                                     @HeaderParam("X-ECOMP-InstanceID") String xEcompInstanceId) {
        log.debug("DELETE component instance external interfaces {} {} {} {}", assetType, uuid, componentInstanceName, objectType);
        Response r = validateRequest(xEcompInstanceId);
        if (r != null) {
            return r;
        }
        ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
        String uid = this.businessLogic.fetchComponentUniqueIdByUuid(uuid, componentType);
        Either<String, ActionStatus> deleteStatus = this.businessLogic
            .deleteExternalReference(uid, componentType, userId, componentInstanceName, objectType, reference);
        if (deleteStatus.isLeft()) {
            return this.buildOkResponse(new ExternalRefDTO(reference));
        } else {
            return this.buildExtRefErrorResponse(deleteStatus.right().value(), uuid, "", componentInstanceName, objectType, reference);
        }
    }

    @PUT
    @Path("/{assetType}/{uuid}/resourceInstances/{componentInstanceName}/externalReferences/{objectType}/{oldRefValue}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @PermissionAllowed({AafPermission.PermNames.WRITE_VALUE})
    public Response updateComponentInstanceReference(@PathParam("assetType") String assetType, @PathParam("uuid") String uuid,
                                                     @PathParam("componentInstanceName") String componentInstanceName,
                                                     @PathParam("objectType") String objectType, @PathParam("oldRefValue") String oldRefValue,
                                                     ExternalRefDTO newRefValueDTO, @HeaderParam("USER_ID") String userId,
                                                     @HeaderParam("X-ECOMP-InstanceID") String xEcompInstanceId) {
        log.debug("PUT component instance external interfaces {} {} {} {}", assetType, uuid, componentInstanceName, objectType);
        Response r = validateRequest(xEcompInstanceId);
        if (r != null) {
            return r;
        }
        String newRefValue = newRefValueDTO.getReferenceUUID();
        ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
        String uid = this.businessLogic.fetchComponentUniqueIdByUuid(uuid, componentType);
        Either<String, ActionStatus> updateResult = this.businessLogic
            .updateExternalReference(uid, componentType, userId, componentInstanceName, objectType, oldRefValue, newRefValue);
        if (updateResult.isLeft()) {
            return this.buildOkResponse(new ExternalRefDTO(newRefValue));
        } else {
            return this.buildExtRefErrorResponse(updateResult.right().value(), uuid, "", componentInstanceName, objectType, oldRefValue);
        }
    }

    private Response validateRequest(String xEcompInstanceIdHeader) {
        Wrapper<Response> responseWrapper = new Wrapper<>();
        //Validate X-ECOMP_INSTANCE_ID_HEADER
        if (xEcompInstanceIdHeader == null || xEcompInstanceIdHeader.isEmpty()) {
            return this.buildExtRefErrorResponse(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID, "", "", "", "", "");
        }
        return responseWrapper.getInnerElement();
    }

    private Response buildExtRefErrorResponse(ActionStatus status, String uuid, String version, String componentInstanceName, String objectType,
                                              String ref) {
        switch (status) {
            case RESOURCE_NOT_FOUND:
                return buildErrorResponse(componentsUtils.getResponseFormat(status, uuid));
            case COMPONENT_VERSION_NOT_FOUND:
                return buildErrorResponse(componentsUtils.getResponseFormat(status, uuid, version));
            case COMPONENT_INSTANCE_NOT_FOUND:
                return buildErrorResponse(componentsUtils.getResponseFormat(status, componentInstanceName, uuid));
            case EXT_REF_ALREADY_EXIST:
                return Response.status(Response.Status.OK).entity(new ExternalRefDTO(ref)).build();
            case EXT_REF_NOT_FOUND:
                return buildErrorResponse(componentsUtils.getResponseFormat(status, objectType + "/" + ref));
            case MISSING_X_ECOMP_INSTANCE_ID:
                return buildErrorResponse(componentsUtils.getResponseFormat(status));
            default:
                return this.buildGeneralErrorResponse();
        }
    }
}
