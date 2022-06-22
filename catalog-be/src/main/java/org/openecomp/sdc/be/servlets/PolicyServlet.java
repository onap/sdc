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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.PolicyBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.DeclarationTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PolicyTargetDTO;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.elements.LoggerSupportability;
import org.openecomp.sdc.common.log.enums.LoggerSupportabilityActions;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

/**
 * Provides REST API to create, retrieve, update, delete a policy
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tag(name = "SDCE-2 APIs")
@Server(url = "/sdc2/rest")
@Controller
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PolicyServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(PolicyServlet.class);
    private static final LoggerSupportability loggerSupportability = LoggerSupportability.getLogger(ServiceServlet.class.getName());
    private final PolicyBusinessLogic policyBusinessLogic;

    @Inject
    public PolicyServlet(UserBusinessLogic userBusinessLogic, ComponentInstanceBusinessLogic componentInstanceBL, ComponentsUtils componentsUtils,
                         ServletUtils servletUtils, ResourceImportManager resourceImportManager, PolicyBusinessLogic policyBusinessLogic) {
        super(userBusinessLogic, componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.policyBusinessLogic = policyBusinessLogic;
        this.servletUtils = servletUtils;
        this.resourceImportManager = resourceImportManager;
        this.componentsUtils = componentsUtils;
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/policies/{policyTypeName}")
    @Operation(description = "Create Policy", method = "POST", summary = "Returns created Policy", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Policy created"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Policy already exist"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createPolicy(@PathParam("componentId") final String containerComponentId,
                                 @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {
                                     ComponentTypeEnum.RESOURCE_PARAM_NAME,
                                     ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                                 @PathParam("policyTypeName") final String policyTypeName,
                                 @HeaderParam(value = Constants.USER_ID_HEADER) @Parameter(description = "USER_ID of modifier user", required = true) String userId,
                                 @Context final HttpServletRequest request) {
        init();
        loggerSupportability
            .log(LoggerSupportabilityActions.CREATE_POLICIES, StatusCode.STARTED, "Starting to create Policy by user {} containerComponentId={}",
                userId, containerComponentId);
        ComponentTypeEnum componentType = validateComponentTypeAndUserId(containerComponentType, userId);
        PolicyDefinition policy = policyBusinessLogic.createPolicy(componentType, containerComponentId, policyTypeName, userId, true);
        loggerSupportability
            .log(LoggerSupportabilityActions.CREATE_POLICIES, StatusCode.COMPLETE, "Ended create Policy by user {} containerComponentId={}", userId,
                containerComponentId);
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), policy);
    }

    @PUT
    @Path("/{containerComponentType}/{componentId}/policies/{policyId}")
    @Operation(description = "Update Policy metadata", method = "PUT", summary = "Returns updated Policy", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Policy updated"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "component / policy Not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updatePolicy(@PathParam("componentId") final String containerComponentId,
                                 @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {
                                     ComponentTypeEnum.RESOURCE_PARAM_NAME,
                                     ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                                 @PathParam("policyId") final String policyId,
                                 @HeaderParam(value = Constants.USER_ID_HEADER) @Parameter(description = "USER_ID of modifier user", required = true) String userId,
                                 @Parameter(description = "PolicyDefinition", required = true) String policyData,
                                 @Context final HttpServletRequest request) {
        init();
        loggerSupportability
            .log(LoggerSupportabilityActions.UPDATE_POLICY_TARGET, StatusCode.STARTED, "Starting to update Policy by user {} containerComponentId={}",
                userId, containerComponentId);
        PolicyDefinition policyDefinition = convertJsonToObjectOfClass(policyData, PolicyDefinition.class);
        policyDefinition.setUniqueId(policyId);
        policyDefinition = policyBusinessLogic
            .updatePolicy(validateComponentTypeAndUserId(containerComponentType, userId), containerComponentId, policyDefinition, userId, true);
        loggerSupportability
            .log(LoggerSupportabilityActions.UPDATE_POLICY_TARGET, StatusCode.COMPLETE, "Ended update Policy by user {} containerComponentId={}",
                userId, containerComponentId);
        return buildOkResponse(policyDefinition);
    }

    @GET
    @Path("/{containerComponentType}/{componentId}/policies/{policyId}")
    @Operation(description = "Get Policy", method = "GET", summary = "Returns Policy", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Policy was found"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "component / policy Not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getPolicy(@PathParam("componentId") final String containerComponentId,
                              @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {
                                  ComponentTypeEnum.RESOURCE_PARAM_NAME,
                                  ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                              @PathParam("policyId") final String policyId,
                              @HeaderParam(value = Constants.USER_ID_HEADER) @Parameter(description = "USER_ID of modifier user", required = true) String userId,
                              @Context final HttpServletRequest request) {
        init();
        PolicyDefinition policy = policyBusinessLogic
            .getPolicy(validateComponentTypeAndUserId(containerComponentType, userId), containerComponentId, policyId, userId);
        return buildOkResponse(policy);
    }

    @DELETE
    @Path("/{containerComponentType}/{componentId}/policies/{policyId}")
    @Operation(description = "Delete Policy", method = "DELETE", summary = "No body", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "204", description = "Policy was deleted"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "component / policy Not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deletePolicy(@PathParam("componentId") final String containerComponentId,
                                 @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {
                                     ComponentTypeEnum.RESOURCE_PARAM_NAME,
                                     ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                                 @PathParam("policyId") final String policyId,
                                 @HeaderParam(value = Constants.USER_ID_HEADER) @Parameter(description = "USER_ID of modifier user", required = true) String userId,
                                 @Context final HttpServletRequest request) {
        init();
        ComponentTypeEnum componentTypeEnum = validateComponentTypeAndUserId(containerComponentType, userId);
        PolicyDefinition policyDefinition = policyBusinessLogic.deletePolicy(componentTypeEnum, containerComponentId, policyId, userId, true);
        return buildOkResponse(policyDefinition);
    }

    @PUT
    @Path("/{containerComponentType}/{componentId}/policies/{policyId}/undeclare")
    @Operation(description = "undeclare Policy", method = "PUT", summary = "No body", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "204", description = "Policy was undeclared"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "component / policy Not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response undeclarePolicy(@PathParam("componentId") final String containerComponentId,
                                    @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {
                                        ComponentTypeEnum.RESOURCE_PARAM_NAME,
                                        ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                                    @PathParam("policyId") final String policyId,
                                    @HeaderParam(value = Constants.USER_ID_HEADER) @Parameter(description = "USER_ID of modifier user", required = true) String userId,
                                    @Context final HttpServletRequest request) {
        init();
        Response response = null;
        try {
            ComponentTypeEnum componentTypeEnum = validateComponentTypeAndUserId(containerComponentType, userId);
            Either<PolicyDefinition, ResponseFormat> undeclarePolicy = policyBusinessLogic
                .undeclarePolicy(componentTypeEnum, containerComponentId, policyId, userId, true);
            if (undeclarePolicy.isLeft()) {
                response = buildOkResponse(undeclarePolicy.left().value());
            } else {
                response = buildErrorResponse(undeclarePolicy.right().value());
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Undeclare Policy");
            log.error("Failed to undeclare policy. The exception {} occurred. ", e);
        }
        return response;
    }

    @GET
    @Path("/{containerComponentType}/{componentId}/policies/{policyId}/properties")
    @Operation(description = "Get component policy properties", method = "GET", summary = "Returns component policy properties", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = PropertyDataDefinition.class)))),
        @ApiResponse(responseCode = "200", description = "Properties found"),
        @ApiResponse(responseCode = "400", description = "invalid content - Error: containerComponentType is invalid"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Componentorpolicy  not found"),
        @ApiResponse(responseCode = "500", description = "The GET request failed due to internal SDC problem.")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getPolicyProperties(
        @Parameter(description = "the id of the component which is the container of the policy") @PathParam("componentId") final String containerComponentId,
        @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
            ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
        @Parameter(description = "the id of the policy which its properties are to return") @PathParam("policyId") final String policyId,
        @Parameter(description = "the userid", required = true) @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
        @Context final HttpServletRequest request) {
        init();
        List<PropertyDataDefinition> propertyDataDefinitionList = policyBusinessLogic
            .getPolicyProperties(convertToComponentType(containerComponentType), containerComponentId, policyId, userId);
        return buildOkResponse(propertyDataDefinitionList);
    }

    @PUT
    @Path("/{containerComponentType}/{componentId}/policies/{policyId}/properties")
    @Operation(description = "Update Policy properties", method = "PUT", summary = "Returns updated Policy", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Policy properties updated"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "component / policy Not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updatePolicyProperties(@PathParam("componentId") final String containerComponentId,
                                           @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {
                                               ComponentTypeEnum.RESOURCE_PARAM_NAME,
                                               ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                                           @PathParam("policyId") final String policyId,
                                           @HeaderParam(value = Constants.USER_ID_HEADER) @Parameter(description = "USER_ID of modifier user", required = true) String userId,
                                           @Parameter(description = "PolicyDefinition", required = true) String policyData,
                                           @Context final HttpServletRequest request) {
        init();
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_POLICIES_PROPERTIES, StatusCode.STARTED,
            "Starting to update Policy Properties by user {} containerComponentId={}", userId, containerComponentId);
        ComponentTypeEnum componentTypeEnum = validateComponentTypeAndUserId(containerComponentType, userId);
        PropertyDataDefinition[] propertyDataDefinitions = convertJsonToObjectOfClass(policyData, PropertyDataDefinition[].class);
        List<PropertyDataDefinition> propertyDataDefinitionList = policyBusinessLogic
            .updatePolicyProperties(componentTypeEnum, containerComponentId, policyId, propertyDataDefinitions, userId, true);
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_POLICIES_PROPERTIES, StatusCode.STARTED,
            "Starting to update Policy Properties by user {} containerComponentId={}", userId, containerComponentId);
        return buildOkResponse(propertyDataDefinitionList);
    }

    private ComponentTypeEnum validateComponentTypeAndUserId(final String containerComponentType, String userId) {
        if (StringUtils.isEmpty(userId)) {
            log.error("Missing userId HTTP header. ");
            throw new ByActionStatusComponentException(ActionStatus.MISSING_USER_ID);
        }
        return validateComponentType(containerComponentType);
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/policies/{policyId}/targets")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "update policy targets", method = "POST", summary = "Returns updated Policy", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Policy target updated"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updatePolicyTargets(@PathParam("componentId") final String containerComponentId,
                                        @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {
                                            ComponentTypeEnum.RESOURCE_PARAM_NAME,
                                            ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                                        @PathParam("policyId") final String policyId,
                                        @HeaderParam(value = Constants.USER_ID_HEADER) @Parameter(description = "USER_ID of modifier user", required = true) String userId,
                                        @Context final HttpServletRequest request, List<PolicyTargetDTO> requestJson) {
        Map<PolicyTargetType, List<String>> policyTargetTypeListMap = updatePolicyTargetsFromDTO(requestJson);
        PolicyDefinition policyDefinition = updatePolicyTargetsFromMap(policyTargetTypeListMap, containerComponentType, containerComponentId,
            policyId, userId);
        return buildOkResponse(policyDefinition);
    }

    @POST
    @Path("/{componentType}/{componentId}/create/policies")
    @Operation(description = "Create policies on service", method = "POST", summary = "Return policies list", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Component found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response declareProperties(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId,
                                      @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                      @Parameter(description = "ComponentIns policies Object to be created", required = true) String componentInstPoliciesMapObj) {
        return super.declareProperties(userId, componentId, componentType, componentInstPoliciesMapObj, DeclarationTypeEnum.POLICY, request);
    }

    private PolicyDefinition updatePolicyTargetsFromMap(Map<PolicyTargetType, List<String>> policyTarget, String containerComponentType,
                                                        String containerComponentId, String policyId, String userId) {
        ComponentTypeEnum componentTypeEnum = convertToComponentType(containerComponentType);
        return policyBusinessLogic.updatePolicyTargets(componentTypeEnum, containerComponentId, policyId, policyTarget, userId);
    }

    private Map<PolicyTargetType, List<String>> updatePolicyTargetsFromDTO(List<PolicyTargetDTO> targetDTOList) {
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_POLICY_TARGET, StatusCode.STARTED, "Starting to update Policy target");
        Map<PolicyTargetType, List<String>> policyTarget = new HashMap<>();
        for (PolicyTargetDTO currentTarget : targetDTOList) {
            if (!addTargetsByType(policyTarget, currentTarget.getType(), currentTarget.getUniqueIds())) {
                throw new ByActionStatusComponentException(ActionStatus.POLICY_TARGET_TYPE_DOES_NOT_EXIST, currentTarget.getType());
            }
        }
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_POLICY_TARGET, StatusCode.COMPLETE, "Ended update Policy target");
        return policyTarget;
    }

    public boolean addTargetsByType(Map<PolicyTargetType, List<String>> policyTarget, String type, List<String> uniqueIds) {
        PolicyTargetType targetTypeEnum = PolicyTargetType.getByNameIgnoreCase(type);
        if (targetTypeEnum != null) {
            policyTarget.put(targetTypeEnum, validateUniquenessOfIds(uniqueIds));
            return true;
        } else {
            return false;
        }
    }

    private List<String> validateUniquenessOfIds(List<String> uniqueIds) {
        return uniqueIds.stream().distinct().collect(Collectors.toList());
    }
}
