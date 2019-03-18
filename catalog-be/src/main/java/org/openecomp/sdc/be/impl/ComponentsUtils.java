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

package org.openecomp.sdc.be.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fj.data.Either;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.auditing.impl.*;
import org.openecomp.sdc.be.auditing.impl.category.AuditCategoryEventFactory;
import org.openecomp.sdc.be.auditing.impl.category.AuditGetCategoryHierarchyEventFactory;
import org.openecomp.sdc.be.auditing.impl.distribution.*;
import org.openecomp.sdc.be.auditing.impl.externalapi.*;
import org.openecomp.sdc.be.auditing.impl.resourceadmin.AuditResourceEventFactoryManager;
import org.openecomp.sdc.be.auditing.impl.usersadmin.AuditGetUsersListEventFactory;
import org.openecomp.sdc.be.auditing.impl.usersadmin.AuditUserAccessEventFactory;
import org.openecomp.sdc.be.auditing.impl.usersadmin.AuditUserAdminEventFactory;
import org.openecomp.sdc.be.components.impl.ImportUtils;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.graph.datatype.AdditionalInformationEnum;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterInfo;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation.PropertyConstraintDeserialiser;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation.PropertyConstraintJacksonDeserializer;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.openecomp.sdc.be.resources.data.auditing.model.*;
import org.openecomp.sdc.be.tosca.ToscaError;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

@org.springframework.stereotype.Component("componentUtils")
public class ComponentsUtils {

    private static final String CONVERT_STORAGE_RESPONSE_TO_ACTION_RESPONSE = "convert storage response {} to action response {}";
	private static final String INSIDE_AUDITING_FOR_AUDIT_ACTION = "Inside auditing for audit action {}";
	private static final String AUDIT_BEFORE_SENDING_RESPONSE = "audit before sending response";
	private static final String CONVERT_JSON_TO_OBJECT = "convertJsonToObject";
	private static final Logger log = Logger.getLogger(ComponentsUtils.class);
    private final AuditingManager auditingManager;
    private final ResponseFormatManager responseFormatManager;

    public ComponentsUtils(AuditingManager auditingManager) {
        this.auditingManager = auditingManager;
        this.responseFormatManager = ResponseFormatManager.getInstance();
    }

    public AuditingManager getAuditingManager() {
        return auditingManager;
    }

    public <T> Either<T, ResponseFormat> convertJsonToObject(String data, User user, Class<T> clazz, AuditingActionEnum actionEnum) {
        if (data == null) {
            BeEcompErrorManager.getInstance().logBeInvalidJsonInput(CONVERT_JSON_TO_OBJECT);
            log.debug("object is null after converting from json");
            ResponseFormat responseFormat = getInvalidContentErrorAndAudit(user, actionEnum);
            return Either.right(responseFormat);
        }
        try {
            T obj = parseJsonToObject(data, clazz);
            return Either.left(obj);
        } catch (Exception e) {
            // INVALID JSON
            BeEcompErrorManager.getInstance().logBeInvalidJsonInput(CONVERT_JSON_TO_OBJECT);
            log.debug("failed to convert from json {}", data, e);
            ResponseFormat responseFormat = getInvalidContentErrorAndAudit(user, actionEnum);
            return Either.right(responseFormat);
        }
    }

    public static <T> T parseJsonToObject(String data, Class<T> clazz) {
        Type constraintType = new TypeToken<PropertyConstraint>() {}.getType();
        Gson gson = new GsonBuilder().registerTypeAdapter(constraintType, new PropertyConstraintDeserialiser()).create();
        log.trace("convert json to object. json=\n{}", data);
        return gson.fromJson(data, clazz);
    }

    public <T> Either<T, ResponseFormat> convertJsonToObjectUsingObjectMapper(String data, User user, Class<T> clazz, AuditingActionEnum actionEnum, ComponentTypeEnum typeEnum) {
        T component = null;
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        try {
            log.trace("convert json to object. json=\n{}", data);

            SimpleModule module = new SimpleModule("customDeserializationModule");
            module.addDeserializer(PropertyConstraint.class, new PropertyConstraintJacksonDeserializer());
            mapper.registerModule(module);

            component = mapper.readValue(data, clazz);
            if (component == null) {
                BeEcompErrorManager.getInstance().logBeInvalidJsonInput(CONVERT_JSON_TO_OBJECT);
                log.debug("object is null after converting from json");
                ResponseFormat responseFormat = getInvalidContentErrorAndAuditComponent(user, actionEnum, typeEnum);
                return Either.right(responseFormat);
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeInvalidJsonInput(CONVERT_JSON_TO_OBJECT);
            log.debug("failed to convert from json {}", data, e);
            ResponseFormat responseFormat = getInvalidContentErrorAndAuditComponent(user, actionEnum, typeEnum);
            return Either.right(responseFormat);
        }
        return Either.left(component);
    }

    public ResponseFormat getResponseFormat(ActionStatus actionStatus, String... params) {
        return responseFormatManager.getResponseFormat(actionStatus, params);
    }
   
    public ResponseFormat getResponseFormat(StorageOperationStatus storageStatus, String... params) {
        return responseFormatManager.getResponseFormat(this.convertFromStorageResponse(storageStatus), params);
    }

    public <T> Either<List<T>, ResponseFormat> convertToResponseFormatOrNotFoundErrorToEmptyList(StorageOperationStatus storageOperationStatus) {
        return storageOperationStatus.equals(StorageOperationStatus.NOT_FOUND) ? Either.left(Collections.emptyList()) :
                                                                                 Either.right(getResponseFormat(storageOperationStatus));
    }

    /**
     * Returns the response format of resource error with respective variables according to actionStatus. This is needed for cases where actionStatus is anonymously converted from storage operation, and the caller doesn't know what actionStatus he
     * received. It's caller's Responsibility to fill the resource object passed to this function with needed fields.
     * <p>
     * Note that RESOURCE_IN_USE case passes hardcoded "resource" string to the error parameter. This means that if Resource object will also be used for Service, this code needs to be refactored and we should tell Resource from Service.
     *
     * @param actionStatus
     * @param resource
     * @return
     */
    public ResponseFormat getResponseFormatByResource(ActionStatus actionStatus, Resource resource) {
        if (resource == null) {
            return getResponseFormat(actionStatus);
        }
        ResponseFormat responseFormat;

        switch (actionStatus) {
            case COMPONENT_VERSION_ALREADY_EXIST:
                responseFormat = getResponseFormat(ActionStatus.COMPONENT_VERSION_ALREADY_EXIST, ComponentTypeEnum.RESOURCE.getValue(), resource.getVersion());
                break;
            case RESOURCE_NOT_FOUND:
                responseFormat = getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, resource.getName());
                break;
            case COMPONENT_NAME_ALREADY_EXIST:
                responseFormat = getResponseFormat(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, ComponentTypeEnum.RESOURCE.getValue(), resource.getName());
                break;
            case COMPONENT_IN_USE:
                responseFormat = getResponseFormat(ActionStatus.COMPONENT_IN_USE, ComponentTypeEnum.RESOURCE.name().toLowerCase(), resource.getUniqueId());
                break;
            default:
                responseFormat = getResponseFormat(actionStatus);
                break;
        }
        return responseFormat;
    }

    public ResponseFormat getResponseFormatByResource(ActionStatus actionStatus, String resourceName) {
        if (resourceName == null) {
            return getResponseFormat(actionStatus);
        }

        ResponseFormat responseFormat;
        if (actionStatus == ActionStatus.RESOURCE_NOT_FOUND) {
            responseFormat = getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, resourceName);
        }
        else {
            responseFormat = getResponseFormat(actionStatus);
        }
        return responseFormat;
    }

    public ResponseFormat getResponseFormatByCapabilityType(ActionStatus actionStatus, CapabilityTypeDefinition capabilityType) {
        if (capabilityType == null) {
            return getResponseFormat(actionStatus);
        }

        ResponseFormat responseFormat;
        if (actionStatus == ActionStatus.CAPABILITY_TYPE_ALREADY_EXIST) {
            responseFormat = getResponseFormat(ActionStatus.CAPABILITY_TYPE_ALREADY_EXIST, capabilityType.getType());
        }
        else {
            responseFormat = getResponseFormat(actionStatus);
        }
        return responseFormat;
    }

    public <T> ResponseFormat getResponseFormatByElement(ActionStatus actionStatus, T obj) {
        if (obj == null) {
            return getResponseFormat(actionStatus);
        }

        ResponseFormat responseFormat = null;
        if (actionStatus == ActionStatus.MISSING_CAPABILITY_TYPE) {
            if (obj instanceof List && org.apache.commons.collections.CollectionUtils.isNotEmpty((List) obj)) {
                List list = (List) obj;
                if (list.get(0) instanceof RequirementDefinition) {
                    responseFormat = getResponseFormat(ActionStatus.MISSING_CAPABILITY_TYPE, ((RequirementDefinition) list
                            .get(0)).getName());    //Arbitray index, all we need is single object
                    return responseFormat;
                }
            }
            log.debug("UNKNOWN TYPE : expecting obj as a non empty List<RequirmentsDefinitions>");
        }
        else {
            responseFormat = getResponseFormat(actionStatus);
        }
        return responseFormat;
    }

    /**
     * Returns the response format of resource error with respective variables according to actionStatus. This is needed for cases where actionStatus is anynomously converted from storage operation, and the caller doesn't know what actionStatus he
     * received. It's caller's responisibility to fill the passed resource object with needed fields.
     *
     * @param actionStatus
     * @param user
     * @return
     */
    public ResponseFormat getResponseFormatByUser(ActionStatus actionStatus, User user) {
        if (user == null) {
            return getResponseFormat(actionStatus);
        }
        ResponseFormat requestErrorWrapper;
        switch (actionStatus) {
            case INVALID_USER_ID:
                requestErrorWrapper = getResponseFormat(actionStatus, user.getUserId());
                break;
            case INVALID_EMAIL_ADDRESS:
                requestErrorWrapper = getResponseFormat(actionStatus, user.getEmail());
                break;
            case INVALID_ROLE:
                requestErrorWrapper = getResponseFormat(actionStatus, user.getRole());
                break;
            case USER_NOT_FOUND:
            case USER_ALREADY_EXIST:
            case USER_INACTIVE:
            case USER_HAS_ACTIVE_ELEMENTS:
                requestErrorWrapper = getResponseFormat(actionStatus, user.getUserId());
                break;
            default:
                requestErrorWrapper = getResponseFormat(actionStatus);
                break;
        }
        return requestErrorWrapper;
    }

    public ResponseFormat getResponseFormatByUserId(ActionStatus actionStatus, String userId) {
        User user = new User();
        user.setUserId(userId);
        return getResponseFormatByUser(actionStatus, user);
    }

    public ResponseFormat getResponseFormatByDE(ActionStatus actionStatus, String envName) {
        ResponseFormat responseFormat;

        switch (actionStatus) {
            case DISTRIBUTION_ENVIRONMENT_NOT_AVAILABLE:
                responseFormat = getResponseFormat(ActionStatus.DISTRIBUTION_ENVIRONMENT_NOT_AVAILABLE, envName);
                break;
            case DISTRIBUTION_ENVIRONMENT_NOT_FOUND:
                responseFormat = getResponseFormat(ActionStatus.DISTRIBUTION_ENVIRONMENT_NOT_FOUND, envName);
                break;
            default:
                responseFormat = getResponseFormat(actionStatus);
                break;
        }
        return responseFormat;
    }

    public ResponseFormat getResponseFormatByArtifactId(ActionStatus actionStatus, String artifactId) {
        ResponseFormat responseFormat;

        switch (actionStatus) {
            case RESOURCE_NOT_FOUND:
            case ARTIFACT_NOT_FOUND:
                responseFormat = getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, artifactId);
                break;
            default:
                responseFormat = getResponseFormat(actionStatus);
                break;
        }
        return responseFormat;
    }

    public ResponseFormat getInvalidContentErrorAndAudit(User user, String resourceName, AuditingActionEnum actionEnum) {
        ResponseFormat responseFormat = responseFormatManager.getResponseFormat(ActionStatus.INVALID_CONTENT);
        log.debug(AUDIT_BEFORE_SENDING_RESPONSE);
        auditResource(responseFormat, user, resourceName, actionEnum);
        return responseFormat;
    }

    public ResponseFormat getInvalidContentErrorForConsumerAndAudit(User user, ConsumerDefinition consumer, AuditingActionEnum actionEnum) {
        ResponseFormat responseFormat = responseFormatManager.getResponseFormat(ActionStatus.INVALID_CONTENT);
        log.debug(AUDIT_BEFORE_SENDING_RESPONSE);
        auditConsumerCredentialsEvent(actionEnum, consumer, responseFormat, user);
        return responseFormat;
    }

    public ResponseFormat getInvalidContentErrorAndAudit(User user, AuditingActionEnum actionEnum) {
        ResponseFormat responseFormat = responseFormatManager.getResponseFormat(ActionStatus.INVALID_CONTENT);
        log.debug(AUDIT_BEFORE_SENDING_RESPONSE);
        auditAdminUserAction(actionEnum, user, null, null, responseFormat);
        return responseFormat;
    }

    public ResponseFormat getInvalidContentErrorAndAuditComponent(User user, AuditingActionEnum actionEnum, ComponentTypeEnum typeEnum) {
        ResponseFormat responseFormat = responseFormatManager.getResponseFormat(ActionStatus.INVALID_CONTENT);
        log.debug(AUDIT_BEFORE_SENDING_RESPONSE);
        auditComponentAdmin(responseFormat, user, null,  actionEnum, typeEnum);
        return responseFormat;
    }

    public void auditResource(ResponseFormat responseFormat, User modifier, Resource resource, AuditingActionEnum actionEnum, ResourceVersionInfo prevResFields) {
        auditResource(responseFormat, modifier, resource, resource.getName(), actionEnum, prevResFields, null, null);
    }

    public void auditResource(ResponseFormat responseFormat, User modifier, String resourceName, AuditingActionEnum actionEnum) {
        auditResource(responseFormat, modifier, null, resourceName, actionEnum);
    }

    public void auditResource(ResponseFormat responseFormat, User modifier, Resource resource, AuditingActionEnum actionEnum) {
        auditResource(responseFormat, modifier, resource, resource.getName(), actionEnum);
    }

    public void auditResource(ResponseFormat responseFormat, User modifier, Resource resource, String resourceName, AuditingActionEnum actionEnum) {
        auditResource(responseFormat, modifier, resource, resourceName, actionEnum, ResourceVersionInfo.newBuilder().build(), null, null);
    }

    public void auditResource(ResponseFormat responseFormat, User modifier, Resource resource, String resourceName, AuditingActionEnum actionEnum,
                              ResourceVersionInfo prevResFields, String currentArtifactUuid, ArtifactDefinition artifactDefinition) {
        if (actionEnum != null) {
            int status = responseFormat.getStatus();

            String uuid = null;
            String resourceCurrVersion = null;
            String resourceCurrState = null;
            String invariantUUID = null;
            String resourceType = ComponentTypeEnum.RESOURCE.getValue();
            String toscaNodeType = null;

            log.trace(INSIDE_AUDITING_FOR_AUDIT_ACTION, actionEnum);

            String message = getMessageString(responseFormat);

            String artifactData = buildAuditingArtifactData(artifactDefinition);

            if (resource != null) {
                resourceName = resource.getName();
                resourceCurrVersion = resource.getVersion();
                if (resource.getLifecycleState() != null) {
                    resourceCurrState = resource.getLifecycleState().name();
                }
                if (resource.getResourceType() != null) {
                    resourceType = resource.getResourceType().name();
                }
                invariantUUID =  resource.getInvariantUUID();
                uuid =  resource.getUUID();
                toscaNodeType = resource.getToscaResourceName();
            }

            AuditEventFactory factory = AuditResourceEventFactoryManager.createResourceEventFactory(
                    actionEnum,
                    CommonAuditData.newBuilder()
                            .status(status)
                            .description(message)
                            .requestId(ThreadLocalsHolder.getUuid())
                            .serviceInstanceId(uuid)
                            .build(),
                    new ResourceCommonInfo(resourceName, resourceType),
                    prevResFields,
                    ResourceVersionInfo.newBuilder()
                            .artifactUuid(currentArtifactUuid)
                            .state(resourceCurrState)
                            .version(resourceCurrVersion)
                            .build(),
                    invariantUUID,
                    modifier,
                    artifactData, null, null, toscaNodeType);

            getAuditingManager().auditEvent(factory);
        }
    }

    private String getMessageString(ResponseFormat responseFormat) {
        String message = "";
        if (responseFormat.getMessageId() != null) {
            message = responseFormat.getMessageId() + ": ";
        }
        message += responseFormat.getFormattedMessage();
        return message;
    }

    public void auditDistributionDownload(ResponseFormat responseFormat, DistributionData distributionData) {
        log.trace("Inside auditing");
        int status = responseFormat.getStatus();

        String message = getMessageString(responseFormat);

        AuditDistributionDownloadEventFactory factory = new AuditDistributionDownloadEventFactory(
                CommonAuditData.newBuilder()
                        .status(status)
                        .description(message)
                        .requestId(ThreadLocalsHolder.getUuid())
                        .build(),
                        distributionData);
                getAuditingManager().auditEvent(factory);
    }

    public void auditExternalGetAsset(ResponseFormat responseFormat, AuditingActionEnum actionEnum, DistributionData distributionData,
                                      ResourceCommonInfo resourceCommonInfo, String requestId, String serviceInstanceId) {
        log.trace(INSIDE_AUDITING_FOR_AUDIT_ACTION, actionEnum);

        AuditEventFactory factory = new AuditAssetExternalApiEventFactory(actionEnum,
                CommonAuditData.newBuilder()
                        .status(responseFormat.getStatus())
                        .description(getMessageString(responseFormat))
                        .requestId(requestId)
                        .serviceInstanceId(serviceInstanceId)
                        .build(),
                resourceCommonInfo, distributionData);

        getAuditingManager().auditEvent(factory);
    }

    public void auditExternalGetAssetList(ResponseFormat responseFormat, AuditingActionEnum actionEnum, DistributionData distributionData, String requestId) {
        log.trace(INSIDE_AUDITING_FOR_AUDIT_ACTION, actionEnum);

        AuditEventFactory factory = new AuditAssetListExternalApiEventFactory(actionEnum,
                CommonAuditData.newBuilder()
                        .status(responseFormat.getStatus())
                        .description(getMessageString(responseFormat))
                        .requestId(requestId)
                         .build(),
                distributionData);

        getAuditingManager().auditEvent(factory);
    }

    public void auditChangeLifecycleAction(ResponseFormat responseFormat, ComponentTypeEnum componentType, String requestId,
                                           Component component, Component responseObject, DistributionData distributionData, User modifier) {

        String invariantUuid = "";
        String serviceInstanceId = "";
        ResourceVersionInfo currResourceVersionInfo = null;
        ResourceVersionInfo prevResourceVersionInfo = null;
        ResourceCommonInfo resourceCommonInfo = new ResourceCommonInfo(componentType.getValue());

        if (component != null) {
            prevResourceVersionInfo = buildResourceVersionInfoFromComponent(component);
            resourceCommonInfo.setResourceName(component.getName());
        }

        if (responseObject != null){
            currResourceVersionInfo = buildResourceVersionInfoFromComponent(responseObject);
            invariantUuid = responseObject.getInvariantUUID();
            serviceInstanceId = responseObject.getUUID();
        }
        else if (component != null){
            currResourceVersionInfo = buildResourceVersionInfoFromComponent(component);
            invariantUuid = component.getInvariantUUID();
            serviceInstanceId = component.getUUID();
        }

        if (prevResourceVersionInfo == null) {
            prevResourceVersionInfo = ResourceVersionInfo.newBuilder()
                    .build();
        }
        if (currResourceVersionInfo == null) {
            currResourceVersionInfo = ResourceVersionInfo.newBuilder()
                    .build();
        }
        AuditEventFactory factory = new AuditChangeLifecycleExternalApiEventFactory(
                CommonAuditData.newBuilder()
                    .serviceInstanceId(serviceInstanceId)
                    .requestId(requestId)
                    .description(getMessageString(responseFormat))
                    .status(responseFormat.getStatus())
                    .build(),
                resourceCommonInfo, distributionData,
                prevResourceVersionInfo, currResourceVersionInfo,
                invariantUuid, modifier);

        getAuditingManager().auditEvent(factory);
    }

    private ResourceVersionInfo buildResourceVersionInfoFromComponent(Component component) {
        return ResourceVersionInfo.newBuilder()
                .version(component.getVersion())
                .state(component.getLifecycleState().name())
                .build();
    }

    public void auditExternalCrudApi(ResponseFormat responseFormat, AuditingActionEnum actionEnum, ResourceCommonInfo resourceCommonInfo, HttpServletRequest request,
                                     ArtifactDefinition artifactDefinition, String artifactUuid) {
        log.trace(INSIDE_AUDITING_FOR_AUDIT_ACTION, actionEnum);

        ResourceVersionInfo currResourceVersionInfo;
        User modifier = new User();
        modifier.setUserId(request.getHeader(Constants.USER_ID_HEADER));
        String artifactData = "";
        DistributionData distributionData = new DistributionData(request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER), request.getRequestURI());
        String requestId = request.getHeader(Constants.X_ECOMP_REQUEST_ID_HEADER);


        if (artifactDefinition == null) {
            currResourceVersionInfo = ResourceVersionInfo.newBuilder()
                    .artifactUuid(artifactUuid)
                    .build();
        }
        else {
            currResourceVersionInfo = ResourceVersionInfo.newBuilder()
                    .artifactUuid(artifactDefinition.getArtifactUUID())
                    .version(artifactDefinition.getArtifactVersion())
                    .build();
            artifactData = buildAuditingArtifactData(artifactDefinition);
            modifier.setUserId(artifactDefinition.getUserIdLastUpdater());
        }
        AuditEventFactory factory = new AuditCrudExternalApiArtifactEventFactory(actionEnum,
                CommonAuditData.newBuilder()
                    .status(responseFormat.getStatus())
                    .description(getMessageString(responseFormat))
                    .requestId(requestId)
                    .build(),
                resourceCommonInfo, distributionData, ResourceVersionInfo.newBuilder().build(), currResourceVersionInfo,
                null, modifier, artifactData);

        getAuditingManager().auditEvent(factory);
    }

    public boolean isExternalApiEvent(AuditingActionEnum auditingActionEnum){
        return auditingActionEnum != null && auditingActionEnum.getAuditingEsType().equals(AuditingTypesConstants.EXTERNAL_API_EVENT_TYPE);
    }

    public void auditCreateResourceExternalApi(ResponseFormat responseFormat, ResourceCommonInfo resourceCommonInfo, HttpServletRequest request,
                                               Resource resource) {

        String invariantUuid = null;
        String serviceInstanceId = null;

        User modifier = new User();
        modifier.setUserId(request.getHeader(Constants.USER_ID_HEADER));
        DistributionData distributionData = new DistributionData(request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER), request.getRequestURI());
        String requestId = request.getHeader(Constants.X_ECOMP_REQUEST_ID_HEADER);

        ResourceVersionInfo currResourceVersionInfo;

        if( resource != null ){
            currResourceVersionInfo = ResourceVersionInfo.newBuilder()
                    .state(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())
                    .version(ImportUtils.Constants.FIRST_NON_CERTIFIED_VERSION)
                    .build();
            resourceCommonInfo.setResourceName(resource.getName());
            invariantUuid = resource.getInvariantUUID();
            serviceInstanceId = resource.getUUID();
        }
        else {
            currResourceVersionInfo = ResourceVersionInfo.newBuilder()
                    .build();
        }

        AuditEventFactory factory = new AuditCreateResourceExternalApiEventFactory(
                CommonAuditData.newBuilder()
                        .status(responseFormat.getStatus())
                        .description(getMessageString(responseFormat))
                        .requestId(requestId)
                        .serviceInstanceId(serviceInstanceId)
                        .build(),
                resourceCommonInfo, distributionData,
                currResourceVersionInfo, invariantUuid, modifier);

        getAuditingManager().auditEvent(factory);
    }

    public void auditExternalActivateService(ResponseFormat responseFormat, DistributionData distributionData, String requestId, String serviceInstanceUuid, User modifier) {
        AuditEventFactory factory = new AuditActivateServiceExternalApiEventFactory(
                CommonAuditData.newBuilder()
                    .serviceInstanceId(serviceInstanceUuid)
                    .description(getMessageString(responseFormat))
                    .status(responseFormat.getStatus())
                    .requestId(requestId)
                    .build(),
                new ResourceCommonInfo(ComponentTypeEnum.SERVICE.name()), distributionData, "", modifier);
        getAuditingManager().auditEvent(factory);
    }

    public void auditExternalDownloadArtifact(ResponseFormat responseFormat, ResourceCommonInfo resourceCommonInfo,
                                              DistributionData distributionData, String requestId, String currArtifactUuid, String userId) {
        User modifier = new User();
        modifier.setUserId(userId);

        AuditEventFactory factory = new AuditDownloadArtifactExternalApiEventFactory(
                CommonAuditData.newBuilder()
                        .description(getMessageString(responseFormat))
                        .status(responseFormat.getStatus())
                        .requestId(requestId)
                        .build(),
                resourceCommonInfo, distributionData,
                ResourceVersionInfo.newBuilder()
                        .artifactUuid(currArtifactUuid)
                        .build(),
                modifier);
        getAuditingManager().auditEvent(factory);
    }

    private String buildAuditingArtifactData(ArtifactDefinition artifactDefinition) {
        StringBuilder sb = new StringBuilder();
        if (artifactDefinition != null) {
            sb.append(artifactDefinition.getArtifactGroupType().getType())
                    .append(",")
                    .append("'")
                    .append(artifactDefinition.getArtifactLabel())
                    .append("'")
                    .append(",")
                    .append(artifactDefinition.getArtifactType())
                    .append(",")
                    .append(artifactDefinition.getArtifactName())
                    .append(",")
                    .append(artifactDefinition.getTimeout())
                    .append(",")
                    .append(artifactDefinition.getEsId());

            sb.append(",");
            sb.append(artifactDefinition.getArtifactVersion() != null ? artifactDefinition.getArtifactVersion() : " ");
            sb.append(",");
            sb.append(artifactDefinition.getArtifactUUID() != null ? artifactDefinition.getArtifactUUID() : " ");
        }
        return sb.toString();
    }

    public void auditCategory(ResponseFormat responseFormat, User modifier, String categoryName, String subCategoryName, String groupingName, AuditingActionEnum actionEnum, String componentType) {
        log.trace(INSIDE_AUDITING_FOR_AUDIT_ACTION, actionEnum);

        AuditEventFactory factory = new AuditCategoryEventFactory(actionEnum,
                CommonAuditData.newBuilder()
                        .description(getMessageString(responseFormat))
                        .status(responseFormat.getStatus())
                        .requestId(ThreadLocalsHolder.getUuid())
                        .build(),
                modifier, categoryName, subCategoryName, groupingName, componentType);

        getAuditingManager().auditEvent(factory);
    }

    public ActionStatus convertFromStorageResponse(StorageOperationStatus storageResponse) {

        return convertFromStorageResponse(storageResponse, ComponentTypeEnum.RESOURCE);
    }

    public ActionStatus convertFromStorageResponse(StorageOperationStatus storageResponse, ComponentTypeEnum type) {

        ActionStatus responseEnum = ActionStatus.GENERAL_ERROR;
        if (storageResponse == null) {
            return responseEnum;
        }
        switch (storageResponse) {
        case OK:
            responseEnum = ActionStatus.OK;
            break;
        case CONNECTION_FAILURE:
        case GRAPH_IS_LOCK:
            responseEnum = ActionStatus.GENERAL_ERROR;
            break;
        case BAD_REQUEST:
            responseEnum = ActionStatus.INVALID_CONTENT;
            break;
        case ENTITY_ALREADY_EXISTS:
            responseEnum = ActionStatus.COMPONENT_NAME_ALREADY_EXIST;
            break;
        case PARENT_RESOURCE_NOT_FOUND:
            responseEnum = ActionStatus.PARENT_RESOURCE_NOT_FOUND;
            break;
        case MULTIPLE_PARENT_RESOURCE_FOUND:
            responseEnum = ActionStatus.MULTIPLE_PARENT_RESOURCE_FOUND;
            break;
        case NOT_FOUND:
            if (ComponentTypeEnum.RESOURCE == type) {
                responseEnum = ActionStatus.RESOURCE_NOT_FOUND;
            } else if (ComponentTypeEnum.PRODUCT == type) {
                responseEnum = ActionStatus.PRODUCT_NOT_FOUND;
            } else {
                responseEnum = ActionStatus.SERVICE_NOT_FOUND;
            }
            break;
        case FAILED_TO_LOCK_ELEMENT:
            responseEnum = ActionStatus.COMPONENT_IN_USE;
            break;
        case ARTIFACT_NOT_FOUND:
            responseEnum = ActionStatus.ARTIFACT_NOT_FOUND;
            break;
        case DISTR_ENVIRONMENT_NOT_AVAILABLE:
            responseEnum = ActionStatus.DISTRIBUTION_ENVIRONMENT_NOT_AVAILABLE;
            break;
        case DISTR_ENVIRONMENT_NOT_FOUND:
            responseEnum = ActionStatus.DISTRIBUTION_ENVIRONMENT_NOT_FOUND;
            break;
        case DISTR_ENVIRONMENT_SENT_IS_INVALID:
            responseEnum = ActionStatus.DISTRIBUTION_ENVIRONMENT_INVALID;
            break;
        case INVALID_TYPE:
            responseEnum = ActionStatus.INVALID_CONTENT;
            break;
        case INVALID_VALUE:
            responseEnum = ActionStatus.INVALID_CONTENT;
            break;
        case CSAR_NOT_FOUND:
            responseEnum = ActionStatus.CSAR_NOT_FOUND;
            break;
        case PROPERTY_NAME_ALREADY_EXISTS:
            responseEnum = ActionStatus.PROPERTY_NAME_ALREADY_EXISTS;
            break;
        case MATCH_NOT_FOUND:
            responseEnum = ActionStatus.COMPONENT_SUB_CATEGORY_NOT_FOUND_FOR_CATEGORY;
            break;
        case CATEGORY_NOT_FOUND:
            responseEnum = ActionStatus.COMPONENT_CATEGORY_NOT_FOUND;
            break;
        case INVALID_PROPERTY:
            responseEnum = ActionStatus.INVALID_PROPERTY;
            break;
        case COMPONENT_IS_ARCHIVED:
            responseEnum = ActionStatus.COMPONENT_IS_ARCHIVED;
            break;
        case DECLARED_INPUT_USED_BY_OPERATION:
            responseEnum = ActionStatus.DECLARED_INPUT_USED_BY_OPERATION;
            break;
       default:
            responseEnum = ActionStatus.GENERAL_ERROR;
            break;
        }
        log.debug(CONVERT_STORAGE_RESPONSE_TO_ACTION_RESPONSE, storageResponse, responseEnum);
        return responseEnum;
    }

    public ActionStatus convertFromToscaError(ToscaError toscaError) {
        ActionStatus responseEnum = ActionStatus.GENERAL_ERROR;
        if (toscaError == null) {
            return responseEnum;
        }
        switch (toscaError) {// TODO match errors
            case NODE_TYPE_CAPABILITY_ERROR:
            case NOT_SUPPORTED_TOSCA_TYPE:
            case NODE_TYPE_REQUIREMENT_ERROR:
                responseEnum = ActionStatus.INVALID_CONTENT;
                break;
            default:
                responseEnum = ActionStatus.GENERAL_ERROR;
                break;
        }
        return responseEnum;
    }

    public ActionStatus convertFromStorageResponseForCapabilityType(StorageOperationStatus storageResponse) {
        ActionStatus responseEnum = ActionStatus.GENERAL_ERROR;

        switch (storageResponse) {
        case OK:
            responseEnum = ActionStatus.OK;
            break;
        case CONNECTION_FAILURE:
        case GRAPH_IS_LOCK:
            responseEnum = ActionStatus.GENERAL_ERROR;
            break;
        case BAD_REQUEST:
            responseEnum = ActionStatus.INVALID_CONTENT;
            break;
        case ENTITY_ALREADY_EXISTS:
            responseEnum = ActionStatus.CAPABILITY_TYPE_ALREADY_EXIST;
            break;
        case SCHEMA_VIOLATION:
            responseEnum = ActionStatus.CAPABILITY_TYPE_ALREADY_EXIST;
            break;
        default:
            responseEnum = ActionStatus.GENERAL_ERROR;
            break;
        }
        log.debug(CONVERT_STORAGE_RESPONSE_TO_ACTION_RESPONSE, storageResponse, responseEnum);
        return responseEnum;
    }

    public ActionStatus convertFromStorageResponseForLifecycleType(StorageOperationStatus storageResponse) {
        ActionStatus responseEnum = ActionStatus.GENERAL_ERROR;

        switch (storageResponse) {
        case OK:
            responseEnum = ActionStatus.OK;
            break;
        case CONNECTION_FAILURE:
        case GRAPH_IS_LOCK:
            responseEnum = ActionStatus.GENERAL_ERROR;
            break;
        case BAD_REQUEST:
            responseEnum = ActionStatus.INVALID_CONTENT;
            break;
        case ENTITY_ALREADY_EXISTS:
            responseEnum = ActionStatus.LIFECYCLE_TYPE_ALREADY_EXIST;
            break;
        case SCHEMA_VIOLATION:
            responseEnum = ActionStatus.LIFECYCLE_TYPE_ALREADY_EXIST;
            break;
        default:
            responseEnum = ActionStatus.GENERAL_ERROR;
            break;
        }
        log.debug(CONVERT_STORAGE_RESPONSE_TO_ACTION_RESPONSE, storageResponse, responseEnum);
        return responseEnum;
    }

    public ActionStatus convertFromStorageResponseForResourceInstance(StorageOperationStatus storageResponse, boolean isRelation) {
        ActionStatus responseEnum = ActionStatus.GENERAL_ERROR;

        switch (storageResponse) {
        case OK:
            responseEnum = ActionStatus.OK;
            break;
        case INVALID_ID:
            responseEnum = ActionStatus.RESOURCE_INSTANCE_BAD_REQUEST;
            break;
        case INVALID_PROPERTY:
            responseEnum = ActionStatus.INVALID_PROPERTY;
            break;
        case GRAPH_IS_LOCK:
            responseEnum = ActionStatus.GENERAL_ERROR;
            break;
        case BAD_REQUEST:
            responseEnum = ActionStatus.INVALID_CONTENT;
            break;
        case MATCH_NOT_FOUND:
            responseEnum = ActionStatus.RESOURCE_INSTANCE_MATCH_NOT_FOUND;
            break;
        case SCHEMA_VIOLATION:
            responseEnum = ActionStatus.RESOURCE_INSTANCE_ALREADY_EXIST;
            break;
        case NOT_FOUND:
            if (isRelation) {
                responseEnum = ActionStatus.RESOURCE_INSTANCE_RELATION_NOT_FOUND;
            } else {
                responseEnum = ActionStatus.RESOURCE_INSTANCE_NOT_FOUND;
            }
            break;
        default:
            responseEnum = ActionStatus.GENERAL_ERROR;
            break;
        }
        log.debug(CONVERT_STORAGE_RESPONSE_TO_ACTION_RESPONSE, storageResponse, responseEnum);
        return responseEnum;
    }

    public ResponseFormat getResponseFormatForResourceInstance(ActionStatus actionStatus, String serviceName, String resourceInstanceName) {
        ResponseFormat responseFormat;

        if (actionStatus == ActionStatus.RESOURCE_INSTANCE_NOT_FOUND) {
            responseFormat = getResponseFormat(actionStatus, resourceInstanceName);
        }
        else {
            responseFormat = getResponseFormat(actionStatus, serviceName);
        }
        return responseFormat;
    }

    public ResponseFormat getResponseFormatForResourceInstanceProperty(ActionStatus actionStatus, String resourceInstanceName) {
        ResponseFormat responseFormat;
        if (actionStatus == ActionStatus.RESOURCE_INSTANCE_NOT_FOUND) {
            responseFormat = getResponseFormat(actionStatus, resourceInstanceName);
        }
        else {
            responseFormat = getResponseFormat(actionStatus);
        }
        return responseFormat;
    }

    public ActionStatus convertFromStorageResponseForResourceInstanceProperty(StorageOperationStatus storageResponse) {
        ActionStatus responseEnum = ActionStatus.GENERAL_ERROR;

        switch (storageResponse) {
        case OK:
            responseEnum = ActionStatus.OK;
            break;
        case INVALID_ID:
            responseEnum = ActionStatus.RESOURCE_INSTANCE_BAD_REQUEST;
            break;
        case GRAPH_IS_LOCK:
            responseEnum = ActionStatus.GENERAL_ERROR;
            break;
        case BAD_REQUEST:
            responseEnum = ActionStatus.INVALID_CONTENT;
            break;
        case MATCH_NOT_FOUND:
            responseEnum = ActionStatus.RESOURCE_INSTANCE_MATCH_NOT_FOUND;
            break;
        case SCHEMA_VIOLATION:
            responseEnum = ActionStatus.RESOURCE_INSTANCE_ALREADY_EXIST;
            break;
        case NOT_FOUND:
            responseEnum = ActionStatus.RESOURCE_INSTANCE_NOT_FOUND;
            break;
        default:
            responseEnum = ActionStatus.GENERAL_ERROR;
            break;
        }
        log.debug(CONVERT_STORAGE_RESPONSE_TO_ACTION_RESPONSE, storageResponse, responseEnum);
        return responseEnum;
    }

    public void auditComponent(ResponseFormat responseFormat, User modifier, Component component, AuditingActionEnum actionEnum, ResourceCommonInfo resourceCommonInfo, ResourceVersionInfo prevComponent, String comment) {
        auditComponent(responseFormat, modifier, component, actionEnum, resourceCommonInfo, prevComponent, null, comment, null, null);
    }

    public void auditComponentAdmin(ResponseFormat responseFormat, User modifier, Component component, AuditingActionEnum actionEnum, ComponentTypeEnum typeEnum) {
        auditComponent(responseFormat, modifier, component, actionEnum, new ResourceCommonInfo(typeEnum.getValue()), ResourceVersionInfo.newBuilder().build());
    }

    public void auditComponentAdmin(ResponseFormat responseFormat, User modifier, Component component, AuditingActionEnum actionEnum, ComponentTypeEnum typeEnum, String comment) {
        auditComponent(responseFormat, modifier, component, actionEnum, new ResourceCommonInfo(typeEnum.getValue()), ResourceVersionInfo.newBuilder().build(), null,
                comment, null, null);
    }

    public void auditComponentAdmin(ResponseFormat responseFormat, User modifier, Component component, AuditingActionEnum actionEnum, ComponentTypeEnum typeEnum, ResourceVersionInfo prevComponent) {
        auditComponent(responseFormat, modifier, component, actionEnum, new ResourceCommonInfo(typeEnum.getValue()), prevComponent);
    }

    public void auditComponent(ResponseFormat responseFormat, User modifier, Component component, AuditingActionEnum actionEnum, ResourceCommonInfo resourceCommonInfo, ResourceVersionInfo prevComponent) {
        auditComponent(responseFormat, modifier, component, actionEnum, resourceCommonInfo, prevComponent, null, null, null, null);
    }

    public void auditComponent(ResponseFormat responseFormat, User modifier, AuditingActionEnum actionEnum, ResourceCommonInfo resourceCommonInfo, String comment) {
        auditComponent(responseFormat, modifier, null, actionEnum, resourceCommonInfo, ResourceVersionInfo.newBuilder().build(), null, comment, null, null);
    }

    public void auditComponent(ResponseFormat responseFormat, User modifier, Component component, AuditingActionEnum actionEnum, ResourceCommonInfo resourceCommonInfo, ResourceVersionInfo prevComponent, ResourceVersionInfo currComponent) {
        auditComponent(responseFormat, modifier, component, actionEnum, resourceCommonInfo, prevComponent, currComponent, null, null, null);
    }

    public void auditComponent(ResponseFormat responseFormat, User modifier, Component component, AuditingActionEnum actionEnum, ResourceCommonInfo resourceCommonInfo, ResourceVersionInfo prevComponent, ResourceVersionInfo currComponent,
                               String comment, ArtifactDefinition artifactDefinition, String did) {
        if (actionEnum != null) {
            String uuid = null;
            String currState = null;
            String invariantUUID = null;
            String currArtifactUid = null;
            String currVersion = null;
            String dcurrStatus = null;

            log.trace(INSIDE_AUDITING_FOR_AUDIT_ACTION, actionEnum);

            String message = getMessageString(responseFormat);
            int status = responseFormat.getStatus();
            String artifactData = buildAuditingArtifactData(artifactDefinition);

            if (component != null) {
                // fields that are filled during creation and might still be empty
                if (component.getLifecycleState() != null) {
                    currState = component.getLifecycleState().name();
                }
                uuid = component.getUUID();
                invariantUUID = component.getInvariantUUID();
                currVersion = component.getVersion();
                if (StringUtils.isEmpty(resourceCommonInfo.getResourceName())) {
                    resourceCommonInfo.setResourceName(component.getComponentMetadataDefinition().getMetadataDataDefinition().getName());
                }
            }
            if (currComponent != null) {
                currArtifactUid = currComponent.getArtifactUuid();
                dcurrStatus = currComponent.getDistributionStatus();
                if (currState == null) { //probably it was not set
                    currState = currComponent.getState();
                }
                if (currVersion == null) { //probably it was not set
                    currVersion = currComponent.getVersion();
                }
            }
            AuditEventFactory factory = AuditResourceEventFactoryManager.createResourceEventFactory(
                    actionEnum,
                    CommonAuditData.newBuilder()
                            .status(status)
                            .description(message)
                            .requestId(ThreadLocalsHolder.getUuid())
                            .serviceInstanceId(uuid)
                            .build(),
                    resourceCommonInfo, prevComponent,
                    ResourceVersionInfo.newBuilder()
                            .artifactUuid(currArtifactUid)
                            .state(currState)
                            .version(currVersion)
                            .distributionStatus(dcurrStatus)
                            .build(),
                    invariantUUID,
                    modifier, artifactData, comment, did, null);

            getAuditingManager().auditEvent(factory);
        }
    }

    public void auditDistributionEngine(AuditingActionEnum action, String environmentName, DistributionTopicData distributionTopicData, String status) {
        auditDistributionEngine(action, environmentName, distributionTopicData, null, null, status);
    }


     public void auditDistributionEngine(AuditingActionEnum action, String environmentName, DistributionTopicData distributionTopicData, String role, String apiKey, String status) {
        AuditEventFactory factory = AuditDistributionEngineEventFactoryManager.createDistributionEngineEventFactory(action,
                environmentName, distributionTopicData, role, apiKey, status);
        getAuditingManager().auditEvent(factory);
    }


    public void auditEnvironmentEngine(AuditingActionEnum actionEnum, String environmentID,
                                       String environmentType, String action, String environmentName, String tenantContext) {
        AuditEventFactory factory = new AuditEcompOpEnvEventFactory(actionEnum, environmentID, environmentName,
                environmentType, action, tenantContext);
        getAuditingManager().auditEvent(factory);
    }

    public void auditDistributionNotification(String serviceUUID, String resourceName, String resourceType, String currVersion, User modifier, String environmentName, String currState,
                                              String topicName, String distributionId, String description, String status, String workloadContext, String tenant) {

        AuditEventFactory factory = new AuditDistributionNotificationEventFactory(
                CommonAuditData.newBuilder()
                    .serviceInstanceId(serviceUUID)
                    .status(status)
                    .description(description)
                    .requestId(ThreadLocalsHolder.getUuid())
                    .build(),
                new ResourceCommonInfo(resourceName, resourceType),
                ResourceVersionInfo.newBuilder()
                    .state(currState)
                    .version(currVersion)
                    .build(),
                distributionId, modifier, topicName,
                new OperationalEnvAuditData(environmentName, workloadContext, tenant));

        getAuditingManager().auditEvent(factory);
    }

    public void auditAuthEvent(String url, String user, String authStatus, String realm) {
        AuditEventFactory factory = new AuditAuthRequestEventFactory(
                CommonAuditData.newBuilder()
                .requestId(ThreadLocalsHolder.getUuid())
                .build(),
                user, url, realm, authStatus);
        getAuditingManager().auditEvent(factory);
    }

    public void auditDistributionStatusNotification(String distributionId, String consumerId, String topicName, String resourceUrl, String statusTime, String status, String errorReason) {
        ThreadLocalsHolder.setUuid(distributionId);

        AuditEventFactory factory =  new AuditDistributionStatusEventFactory(
                CommonAuditData.newBuilder()
                .description(errorReason)
                .status(status)
                .requestId(distributionId)
                .build(),
                new DistributionData(consumerId, resourceUrl),
                distributionId, topicName, statusTime);

        getAuditingManager().auditEvent(factory);
    }

    public void auditGetUebCluster(String consumerId, String status, String description) {
        AuditEventFactory factory = new AuditGetUebClusterEventFactory(
                CommonAuditData.newBuilder()
                        .description(description)
                        .status(status)
                        .requestId(ThreadLocalsHolder.getUuid())
                        .build(),
                consumerId);

        getAuditingManager().auditEvent(factory);
    }

    public void auditMissingInstanceIdAsDistributionEngineEvent(AuditingActionEnum actionEnum, String status) {
        AuditEventFactory factory = AuditDistributionEngineEventFactoryManager.createDistributionEngineEventFactory(
                actionEnum, "",
                DistributionTopicData.newBuilder()
                        .build(), null, null, status);
        getAuditingManager().auditEvent(factory);
    }


    public void auditRegisterOrUnRegisterEvent(AuditingActionEnum action, String consumerId, String apiPublicKey, String envName, String status, String distributionStatus, String notifTopicName, String statusTopicName) {
        String appliedStatus = !StringUtils.isEmpty(status) ? status : distributionStatus;

        AuditEventFactory factory = new AuditRegUnregDistributionEngineEventFactory(action,
                CommonAuditData.newBuilder()
                    .requestId(ThreadLocalsHolder.getUuid())
                    .status(appliedStatus)
                    .build(),
                DistributionTopicData.newBuilder()
                    .statusTopic(statusTopicName)
                    .notificationTopic(notifTopicName)
                    .build(),
                consumerId, apiPublicKey, envName);

        getAuditingManager().auditEvent(factory);
    }

    public void auditServiceDistributionDeployed(String serviceName, String serviceVersion, String serviceUUID, String distributionId, String status, String desc, User modifier) {

        AuditEventFactory factory = new AuditDistributionDeployEventFactory(
                CommonAuditData.newBuilder()
                    .requestId(ThreadLocalsHolder.getUuid())
                    .serviceInstanceId(serviceUUID)
                    .status(status)
                    .description(desc)
                    .build(),
                new ResourceCommonInfo(serviceName, "Service"),
                distributionId,
                modifier,
                serviceVersion);

        getAuditingManager().auditEvent(factory);

    }

    public void auditConsumerCredentialsEvent(AuditingActionEnum actionEnum, ConsumerDefinition consumer, ResponseFormat responseFormat, User modifier) {
        AuditEventFactory factory = new AuditConsumerEventFactory(actionEnum,
                CommonAuditData.newBuilder()
                .description(getMessageString(responseFormat))
                .status(responseFormat.getStatus())
                .requestId(ThreadLocalsHolder.getUuid())
                .build(),
                modifier, consumer);

        getAuditingManager().auditEvent(factory);
    }

    public void auditGetUsersList(User user, String details, ResponseFormat responseFormat) {

        AuditEventFactory factory = new AuditGetUsersListEventFactory(
                CommonAuditData.newBuilder()
                        .description(getMessageString(responseFormat))
                        .status(responseFormat.getStatus())
                        .requestId(ThreadLocalsHolder.getUuid())
                        .build(),
                user, details);
        getAuditingManager().auditEvent(factory);
    }

    public void auditAdminUserAction(AuditingActionEnum actionEnum, User modifier, User userBefore, User userAfter, ResponseFormat responseFormat) {

        AuditEventFactory factory = new AuditUserAdminEventFactory(actionEnum,
                CommonAuditData.newBuilder()
                        .description(getMessageString(responseFormat))
                        .status(responseFormat.getStatus())
                        .requestId(ThreadLocalsHolder.getUuid())
                        .build(),
                modifier, userBefore, userAfter);

        getAuditingManager().auditEvent(factory);
    }

    public void auditUserAccess(User user, ResponseFormat responseFormat) {

        AuditEventFactory factory = new AuditUserAccessEventFactory(CommonAuditData.newBuilder()
                .description(getMessageString(responseFormat))
                .status(responseFormat.getStatus())
                .requestId(ThreadLocalsHolder.getUuid())
                .build(),
                user);

        getAuditingManager().auditEvent(factory);
    }

    public void auditGetCategoryHierarchy(User user, String details, ResponseFormat responseFormat) {

        AuditEventFactory factory = new AuditGetCategoryHierarchyEventFactory(CommonAuditData.newBuilder()
                        .description(getMessageString(responseFormat))
                        .status(responseFormat.getStatus())
                        .requestId(ThreadLocalsHolder.getUuid())
                        .build(),
                user, details);

        getAuditingManager().auditEvent(factory);
    }

    public ResponseFormat getResponseFormatByComponent(ActionStatus actionStatus, Component component, ComponentTypeEnum type) {
        if (component == null) {
            return getResponseFormat(actionStatus);
        }
        ResponseFormat responseFormat;

        switch (actionStatus) {
            case COMPONENT_VERSION_ALREADY_EXIST:
                responseFormat = getResponseFormat(ActionStatus.COMPONENT_VERSION_ALREADY_EXIST, type.getValue(), component.getVersion());
                break;
            case RESOURCE_NOT_FOUND:
                responseFormat = getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, component.getComponentMetadataDefinition().getMetadataDataDefinition().getName());
                break;
            case COMPONENT_NAME_ALREADY_EXIST:
                responseFormat = getResponseFormat(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, type.getValue(), component.getComponentMetadataDefinition().getMetadataDataDefinition().getName());
                break;
            case COMPONENT_IN_USE:
                responseFormat = getResponseFormat(ActionStatus.COMPONENT_IN_USE, type.name().toLowerCase(), component.getUniqueId());
                break;
            case SERVICE_DEPLOYMENT_ARTIFACT_NOT_FOUND:
                responseFormat = getResponseFormat(ActionStatus.SERVICE_DEPLOYMENT_ARTIFACT_NOT_FOUND, component.getComponentMetadataDefinition().getMetadataDataDefinition().getName());
                break;
            default:
                responseFormat = getResponseFormat(actionStatus);
                break;
        }
        return responseFormat;
    }

    public boolean validateStringNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public ActionStatus convertFromStorageResponseForAdditionalInformation(StorageOperationStatus storageResponse) {
        ActionStatus responseEnum;

        switch (storageResponse) {
        case OK:
            responseEnum = ActionStatus.OK;
            break;
        case ENTITY_ALREADY_EXISTS:
            responseEnum = ActionStatus.COMPONENT_NAME_ALREADY_EXIST;
            break;
        case INVALID_ID:
            responseEnum = ActionStatus.ADDITIONAL_INFORMATION_NOT_FOUND;
            break;
        default:
            responseEnum = ActionStatus.GENERAL_ERROR;
            break;
        }
        log.debug(CONVERT_STORAGE_RESPONSE_TO_ACTION_RESPONSE, storageResponse, responseEnum);
        return responseEnum;
    }

    public ActionStatus convertFromResultStatusEnum(ResultStatusEnum resultStatus, JsonPresentationFields elementType) {
        ActionStatus responseEnum = ActionStatus.GENERAL_ERROR;
        switch (resultStatus) {
        case OK:
            responseEnum = ActionStatus.OK;
            break;
        case ELEMENT_NOT_FOUND:
            if(elementType!= null && elementType == JsonPresentationFields.PROPERTY){
                responseEnum = ActionStatus.PROPERTY_NOT_FOUND;
            }
        break;
        case INVALID_PROPERTY_DEFAULT_VALUE:
        case INVALID_PROPERTY_TYPE:
        case INVALID_PROPERTY_VALUE:
        case INVALID_PROPERTY_NAME:
        case MISSING_ENTRY_SCHEMA_TYPE:
            responseEnum = ActionStatus.INVALID_PROPERTY;
            break;
        default:
            responseEnum = ActionStatus.GENERAL_ERROR;
            break;
        }
        return responseEnum;
    }

    public ResponseFormat getResponseFormatAdditionalProperty(ActionStatus actionStatus, AdditionalInfoParameterInfo additionalInfoParameterInfo, NodeTypeEnum nodeType, AdditionalInformationEnum labelOrValue) {

        if (additionalInfoParameterInfo == null) {
            additionalInfoParameterInfo = new AdditionalInfoParameterInfo();
        }
        if (labelOrValue == null) {
            labelOrValue = AdditionalInformationEnum.None;
        }

        ResponseFormat responseFormat = null;
        switch (actionStatus) {
            case COMPONENT_NAME_ALREADY_EXIST:
                responseFormat = getResponseFormat(actionStatus, "Additional parameter", additionalInfoParameterInfo.getKey());
                break;
            case ADDITIONAL_INFORMATION_EXCEEDS_LIMIT:
                responseFormat = getResponseFormat(actionStatus, labelOrValue.name().toLowerCase(), ValidationUtils.ADDITIONAL_INFORMATION_KEY_MAX_LENGTH.toString());
                break;
            case ADDITIONAL_INFORMATION_MAX_NUMBER_REACHED:
                responseFormat = getResponseFormat(actionStatus, nodeType.name().toLowerCase());
                break;
            case ADDITIONAL_INFORMATION_EMPTY_STRING_NOT_ALLOWED:
                responseFormat = getResponseFormat(actionStatus);
                break;
            case ADDITIONAL_INFORMATION_KEY_NOT_ALLOWED_CHARACTERS:
                responseFormat = getResponseFormat(actionStatus);
                break;
            case ADDITIONAL_INFORMATION_VALUE_NOT_ALLOWED_CHARACTERS:
                responseFormat = getResponseFormat(actionStatus);
                break;
            case ADDITIONAL_INFORMATION_NOT_FOUND:
                responseFormat = getResponseFormat(actionStatus);
                break;
            default:
                responseFormat = getResponseFormat(actionStatus);
                break;
        }

        return responseFormat;
    }

    public ResponseFormat getResponseFormatAdditionalProperty(ActionStatus actionStatus) {
        return getResponseFormatAdditionalProperty(actionStatus, null, null, null);
    }

    public ActionStatus convertFromStorageResponseForConsumer(StorageOperationStatus storageResponse) {
        ActionStatus responseEnum = ActionStatus.GENERAL_ERROR;

        switch (storageResponse) {
        case OK:
            responseEnum = ActionStatus.OK;
            break;
        case CONNECTION_FAILURE:
        case GRAPH_IS_LOCK:
            responseEnum = ActionStatus.GENERAL_ERROR;
            break;
        case BAD_REQUEST:
            responseEnum = ActionStatus.INVALID_CONTENT;
            break;
        case ENTITY_ALREADY_EXISTS:
            responseEnum = ActionStatus.CONSUMER_ALREADY_EXISTS;
            break;
        case SCHEMA_VIOLATION:
            responseEnum = ActionStatus.CONSUMER_ALREADY_EXISTS;
            break;
        case NOT_FOUND:
            responseEnum = ActionStatus.ECOMP_USER_NOT_FOUND;
            break;
        default:
            responseEnum = ActionStatus.GENERAL_ERROR;
            break;
        }
        log.debug(CONVERT_STORAGE_RESPONSE_TO_ACTION_RESPONSE, storageResponse, responseEnum);
        return responseEnum;
    }

    public ActionStatus convertFromStorageResponseForGroupType(StorageOperationStatus storageResponse) {
        ActionStatus responseEnum = ActionStatus.GENERAL_ERROR;

        switch (storageResponse) {
        case OK:
            responseEnum = ActionStatus.OK;
            break;
        case CONNECTION_FAILURE:
        case GRAPH_IS_LOCK:
            responseEnum = ActionStatus.GENERAL_ERROR;
            break;
        case BAD_REQUEST:
            responseEnum = ActionStatus.INVALID_CONTENT;
            break;
        case ENTITY_ALREADY_EXISTS:
            responseEnum = ActionStatus.GROUP_TYPE_ALREADY_EXIST;
            break;
        case SCHEMA_VIOLATION:
            responseEnum = ActionStatus.GROUP_TYPE_ALREADY_EXIST;
            break;
        default:
            responseEnum = ActionStatus.GENERAL_ERROR;
            break;
        }
        log.debug(CONVERT_STORAGE_RESPONSE_TO_ACTION_RESPONSE, storageResponse, responseEnum);
        return responseEnum;
    }

    public ActionStatus convertFromStorageResponseForDataType(StorageOperationStatus storageResponse) {
        ActionStatus responseEnum = ActionStatus.GENERAL_ERROR;

        switch (storageResponse) {
        case OK:
            responseEnum = ActionStatus.OK;
            break;
        case CONNECTION_FAILURE:
        case GRAPH_IS_LOCK:
            responseEnum = ActionStatus.GENERAL_ERROR;
            break;
        case BAD_REQUEST:
            responseEnum = ActionStatus.INVALID_CONTENT;
            break;
        case ENTITY_ALREADY_EXISTS:
            responseEnum = ActionStatus.DATA_TYPE_ALREADY_EXIST;
            break;
        case SCHEMA_VIOLATION:
            responseEnum = ActionStatus.DATA_TYPE_ALREADY_EXIST;
            break;
        case CANNOT_UPDATE_EXISTING_ENTITY:
            responseEnum = ActionStatus.DATA_TYPE_CANNOT_BE_UPDATED_BAD_REQUEST;
            break;
        default:
            responseEnum = ActionStatus.GENERAL_ERROR;
            break;
        }
        log.debug(CONVERT_STORAGE_RESPONSE_TO_ACTION_RESPONSE, storageResponse, responseEnum);
        return responseEnum;
    }

    public ResponseFormat getResponseFormatByGroupType(ActionStatus actionStatus, GroupTypeDefinition groupType) {
        if (groupType == null) {
            return getResponseFormat(actionStatus);
        }
        ResponseFormat responseFormat;

        switch (actionStatus) {
            case GROUP_MEMBER_EMPTY:
            case GROUP_TYPE_ALREADY_EXIST:
                responseFormat = getResponseFormat(actionStatus, groupType.getType());
                break;
            default:
                responseFormat = getResponseFormat(actionStatus);
                break;
        }
        return responseFormat;

    }

    public ResponseFormat getResponseFormatByPolicyType(ActionStatus actionStatus, PolicyTypeDefinition policyType) {
        if (policyType == null) {
            return getResponseFormat(actionStatus);
        }

        ResponseFormat responseFormat;
        if (actionStatus == ActionStatus.POLICY_TYPE_ALREADY_EXIST) {
            responseFormat = getResponseFormat(actionStatus, policyType.getType());
        }
        else {
            responseFormat = getResponseFormat(actionStatus);
        }
        return responseFormat;

    }

    public ResponseFormat getResponseFormatByDataType(ActionStatus actionStatus, DataTypeDefinition dataType, List<String> properties) {
        if (dataType == null) {
            return getResponseFormat(actionStatus);
        }
        ResponseFormat responseFormat;

        switch (actionStatus) {
            case DATA_TYPE_ALREADY_EXIST:
                responseFormat = getResponseFormat(actionStatus, dataType.getName());
                break;
            case DATA_TYPE_NOR_PROPERTIES_NEITHER_DERIVED_FROM:
                responseFormat = getResponseFormat(actionStatus, dataType.getName());
                break;
            case DATA_TYPE_PROPERTIES_CANNOT_BE_EMPTY:
                responseFormat = getResponseFormat(actionStatus, dataType.getName());
                break;
            case DATA_TYPE_PROPERTY_ALREADY_DEFINED_IN_ANCESTOR:
                responseFormat = getResponseFormat(actionStatus, dataType.getName(), properties == null ? "" : String.valueOf(properties));
                break;
            case DATA_TYPE_DERIVED_IS_MISSING:
                responseFormat = getResponseFormat(actionStatus, dataType.getDerivedFromName());
                break;
            case DATA_TYPE_DUPLICATE_PROPERTY:
                responseFormat = getResponseFormat(actionStatus, dataType.getName());
                break;
            case DATA_TYPE_PROEPRTY_CANNOT_HAVE_SAME_TYPE_OF_DATA_TYPE:
                responseFormat = getResponseFormat(actionStatus, dataType.getName(), properties == null ? "" : String.valueOf(properties));
                break;
            case DATA_TYPE_CANNOT_HAVE_PROPERTIES:
                responseFormat = getResponseFormat(actionStatus, dataType.getName());
                break;
            case DATA_TYPE_CANNOT_BE_UPDATED_BAD_REQUEST:
                responseFormat = getResponseFormat(actionStatus, dataType.getName());
                break;

            default:
                responseFormat = getResponseFormat(actionStatus);
                break;
        }
        return responseFormat;
    }

    public StorageOperationStatus convertToStorageOperationStatus(CassandraOperationStatus cassandraStatus) {
        StorageOperationStatus result;
        switch (cassandraStatus) {
            case OK:
                result = StorageOperationStatus.OK;
                break;
            case NOT_FOUND:
                result = StorageOperationStatus.NOT_FOUND;
                break;
            case CLUSTER_NOT_CONNECTED:
            case KEYSPACE_NOT_CONNECTED:
                result = StorageOperationStatus.CONNECTION_FAILURE;
                break;
            default:
                result = StorageOperationStatus.GENERAL_ERROR;
                break;
        }
        return result;
    }
    
    
    public ResponseFormat getResponseFormat(ComponentException exception) {
        ResponseFormat responseFormat = exception.getResponseFormat();
        if (responseFormat != null) {
            return responseFormat;
        }
        return getResponseFormat(exception.getActionStatus(), exception.getParams());
    }
    public ActionStatus convertFromStorageResponseForRelationshipType(
            StorageOperationStatus storageResponse) {
        ActionStatus responseEnum;

        switch (storageResponse) {
            case OK:
                responseEnum = ActionStatus.OK;
                break;
            case CONNECTION_FAILURE:
            case GRAPH_IS_LOCK:
                responseEnum = ActionStatus.GENERAL_ERROR;
                break;
            case BAD_REQUEST:
                responseEnum = ActionStatus.INVALID_CONTENT;
                break;
            case ENTITY_ALREADY_EXISTS:
                responseEnum = ActionStatus.RELATIONSHIP_TYPE_ALREADY_EXIST;
                break;
            case SCHEMA_VIOLATION:
                responseEnum = ActionStatus.RELATIONSHIP_TYPE_ALREADY_EXIST;
                break;
            default:
                responseEnum = ActionStatus.GENERAL_ERROR;
                break;
        }
        log.debug(CONVERT_STORAGE_RESPONSE_TO_ACTION_RESPONSE, storageResponse, responseEnum);
        return responseEnum;
    }
}
