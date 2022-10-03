/*
 * Copyright (C) 2020 CMCC, Inc. and others. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.sdc.be.externalapi.servlet;

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
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.components.impl.AbstractTemplateBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ElementBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.externalapi.servlet.representation.AbstractTemplateInfo;
import org.openecomp.sdc.be.externalapi.servlet.representation.CopyServiceInfo;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.servlets.AbstractValidationsServlet;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.elements.LoggerSupportability;
import org.openecomp.sdc.common.log.enums.LoggerSupportabilityActions;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

/**
 * This servlet provides external interfaces related to abstract templates.
 *
 * @author hekeguang
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tag(name = "SDCE-7 APIs")
@Server(url = "/sdc")
@Controller
public class AbstractTemplateServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(AbstractTemplateServlet.class);
    private static final LoggerSupportability loggerSupportability = LoggerSupportability.getLogger(AbstractTemplateServlet.class.getName());
    private final ElementBusinessLogic elementBusinessLogic;
    private final AbstractTemplateBusinessLogic abstractTemplateBusinessLogic;
    private final ServiceBusinessLogic serviceBusinessLogic;
    private final ResourceBusinessLogic resourceBusinessLogic;
    @Context
    private HttpServletRequest request;

    @Inject
    public AbstractTemplateServlet(ComponentInstanceBusinessLogic componentInstanceBL,
                                   ComponentsUtils componentsUtils, ServletUtils servletUtils, ResourceImportManager resourceImportManager,
                                   ElementBusinessLogic elementBusinessLogic, AbstractTemplateBusinessLogic abstractTemplateBusinessLogic,
                                   ServiceBusinessLogic serviceBusinessLogic, ResourceBusinessLogic resourceBusinessLogic) {
        super(componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.elementBusinessLogic = elementBusinessLogic;
        this.abstractTemplateBusinessLogic = abstractTemplateBusinessLogic;
        this.serviceBusinessLogic = serviceBusinessLogic;
        this.resourceBusinessLogic = resourceBusinessLogic;
    }

    /**
     * @param requestId
     * @param instanceIdHeader
     * @param accept
     * @param authorization
     * @param uuid
     * @return
     */
    @GET
    @Path("/abstract/service/serviceUUID/{uuid}/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Fetch abstract status of service", method = "GET", summary = "Return whether the service is a virtual service", responses = {
        @ApiResponse(responseCode = "200", description = "The check result of whether the service is an abstract service is returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AbstractTemplateInfo.class)))),
        @ApiResponse(responseCode = "400", description = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
        @ApiResponse(responseCode = "401", description = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic Authentication credentials - POL5002"),
        @ApiResponse(responseCode = "403", description = "ECOMP component is not authorized - POL5003"),
        @ApiResponse(responseCode = "404", description = "Error: Requested '%1' (uuid) resource was not found - SVC4063"),
        @ApiResponse(responseCode = "405", description = "Method  Not Allowed  :  Invalid HTTP method type used ( PUT,DELETE,POST will be rejected) - POL4050"),
        @ApiResponse(responseCode = "500", description = "The GET request failed either due to internal SDC problem. ECOMP Component should continue the attempts to get the needed information - POL5000")})
    @PermissionAllowed(AafPermission.PermNames.READ_VALUE)
    public Response getServiceAbstractStatus(
        @Parameter(description = "X-ECOMP-RequestID header", required = false) @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
        @Parameter(description = "X-ECOMP-InstanceID header", required = true) @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
        @Parameter(description = "Determines the format of the body of the response", required = false) @HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
        @Parameter(description = "The username and password", required = true) @HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
        @Parameter(description = "The requested asset uuid", required = true) @PathParam("uuid") final String uuid) throws IOException {
        ResponseFormat responseFormat = null;
        AuditingActionEnum auditingActionEnum = AuditingActionEnum.GET_TEMPLATE_ABSTRACT_STATUS;
        String requestURI = request.getRequestURI();
        String url = request.getMethod() + " " + requestURI;
        log.debug("getServiceAbstractStatus: Start handle request of {}", url);
        String assetType = "services";
        ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
        ResourceCommonInfo resourceCommonInfo = new ResourceCommonInfo(componentType.getValue());
        DistributionData distributionData = new DistributionData(instanceIdHeader, requestURI);
        // Mandatory
        if (instanceIdHeader == null || instanceIdHeader.isEmpty()) {
            log.debug("getServiceAbstractStatus: Missing X-ECOMP-InstanceID header");
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
            getComponentsUtils().auditExternalGetAsset(responseFormat, auditingActionEnum, distributionData, resourceCommonInfo, requestId, uuid);
            return buildErrorResponse(responseFormat);
        }
        try {
            Either<List<? extends Component>, ResponseFormat> assetTypeData = elementBusinessLogic
                .getCatalogComponentsByUuidAndAssetType(assetType, uuid);
            if (assetTypeData.isRight()) {
                log.debug("getServiceAbstractStatus: Service Fetching Failed");
                responseFormat = assetTypeData.right().value();
                getComponentsUtils().auditExternalGetAsset(responseFormat, auditingActionEnum, distributionData, resourceCommonInfo, requestId, uuid);
                return buildErrorResponse(responseFormat);
            }
            resourceCommonInfo.setResourceName(assetTypeData.left().value().iterator().next().getName());
            log.debug("getServiceAbstractStatus: Service Fetching Success");
            Either<AbstractTemplateInfo, ResponseFormat> resMetadata = abstractTemplateBusinessLogic
                .getServiceAbstractStatus(assetTypeData.left().value());
            if (resMetadata.isRight()) {
                log.debug("getServiceAbstractStatus: Service abstract status get Failed");
                responseFormat = resMetadata.right().value();
                getComponentsUtils().auditExternalGetAsset(responseFormat, auditingActionEnum, distributionData, resourceCommonInfo, requestId, uuid);
                return buildErrorResponse(responseFormat);
            }
            Object result = RepresentationUtils.toRepresentation(resMetadata.left().value());
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
            getComponentsUtils().auditExternalGetAsset(responseFormat, auditingActionEnum, distributionData, resourceCommonInfo, requestId, uuid);
            return buildOkResponse(responseFormat, result);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Fetch abstract status of service");
            log.debug("getServiceAbstractStatus: Fetch abstract status of service with exception", e);
            throw e;
        }
    }

    /**
     * @param requestId
     * @param instanceIdHeader
     * @param accept
     * @param authorization
     * @param uuid
     * @return
     */
    @POST
    @Path("/abstract/service/serviceUUID/{uuid}/copy")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Copy a new service based on the existing service", method = "POST", summary = "Return whether the copy service is successful", responses = {
        @ApiResponse(responseCode = "200", description = "ECOMP component is authenticated and list of Catalog Assets Metadata is returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AbstractTemplateInfo.class)))),
        @ApiResponse(responseCode = "400", description = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
        @ApiResponse(responseCode = "401", description = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic Authentication credentials - POL5002"),
        @ApiResponse(responseCode = "403", description = "ECOMP component is not authorized - POL5003"),
        @ApiResponse(responseCode = "404", description = "Error: Requested '%1' (uuid) resource was not found - SVC4063"),
        @ApiResponse(responseCode = "405", description = "Method  Not Allowed  :  Invalid HTTP method type used ( PUT,DELETE,POST will be rejected) - POL4050"),
        @ApiResponse(responseCode = "500", description = "The GET request failed either due to internal SDC problem. ECOMP Component should continue the attempts to get the needed information - POL5000"),
        @ApiResponse(responseCode = "409", description = "Service already exist")})
    @PermissionAllowed(AafPermission.PermNames.WRITE_VALUE)
    public Response copyExistService(
        @Parameter(description = "The user id", required = true) @HeaderParam(value = Constants.USER_ID_HEADER) final String userId,
        @Parameter(description = "X-ECOMP-RequestID header", required = false) @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
        @Parameter(description = "X-ECOMP-InstanceID header", required = true) @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
        @Parameter(description = "Determines the format of the body of the response", required = false) @HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
        @Parameter(description = "The username and password", required = true) @HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
        @Parameter(description = "The requested asset uuid", required = true) @PathParam("uuid") final String uuid,
        @Parameter(hidden = true) String data) throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("copyExistService: Start handle request of {}", url);
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);
        loggerSupportability.log(LoggerSupportabilityActions.CREATE_SERVICE, StatusCode.STARTED, "Starting to create a service by user {} ", userId);
        validateNotEmptyBody(data);
        Either<CopyServiceInfo, ResponseFormat> convertResponse = parseToCopyServiceInfo(data, modifier);
        if (convertResponse.isRight()) {
            throw new ByResponseFormatComponentException(convertResponse.right().value());
        }
        String assetType = "services";
        CopyServiceInfo copyServiceInfo = convertResponse.left().value();
        Either<List<? extends Component>, ResponseFormat> assetTypeData = elementBusinessLogic
            .getCatalogComponentsByUuidAndAssetType(assetType, uuid);
        if (assetTypeData.isRight() || assetTypeData.left().value().size() != 1) {
            log.debug("getServiceAbstractStatus: Service Fetching Failed");
            throw new ByResponseFormatComponentException(assetTypeData.right().value());
        }
        log.debug("getServiceAbstractStatus: Service Fetching Success");
        Service service = (Service) assetTypeData.left().value().get(0);
        List<String> tags = service.getTags();
        if (tags != null && !tags.isEmpty()) {
            for (int i = tags.size() - 1; i >= 0; i--) {
                String tag = tags.get(i);
                if (service.getName().equals(tag)) {
                    tags.remove(tag);
                }
            }
        }
        service.setName(copyServiceInfo.getNewServiceName());
        tags.add(copyServiceInfo.getNewServiceName());
        Either<Service, ResponseFormat> actionResponse = serviceBusinessLogic.createService(service, modifier);
        if (actionResponse.isRight()) {
            log.debug("Failed to create service");
            throw new ByResponseFormatComponentException(actionResponse.right().value());
        }
        loggerSupportability.log(LoggerSupportabilityActions.CREATE_SERVICE, service.getComponentMetadataForSupportLog(), StatusCode.COMPLETE,
            "Service {} has been copyied by user {} ", service.getName(), userId);
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), actionResponse.left().value());
    }

    public Either<CopyServiceInfo, ResponseFormat> parseToCopyServiceInfo(String serviceJson, User user) {
        return getComponentsUtils().convertJsonToObjectUsingObjectMapper(serviceJson, user, CopyServiceInfo.class, AuditingActionEnum.CREATE_RESOURCE,
            ComponentTypeEnum.SERVICE);
    }
}
