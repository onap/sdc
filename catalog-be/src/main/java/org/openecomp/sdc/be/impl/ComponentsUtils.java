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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.openecomp.sdc.be.auditing.api.IAuditingManager;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.graph.datatype.AdditionalInformationEnum;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterInfo;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ConsumerDefinition;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation.PropertyConstraintDeserialiser;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation.PropertyConstraintJacksonDeserialiser;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.tosca.ToscaError;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import fj.data.Either;

@org.springframework.stereotype.Component("componentUtils")
public class ComponentsUtils {

	@javax.annotation.Resource
	private IAuditingManager auditingManager;

	private ResponseFormatManager responseFormatManager;

	private static Logger log = LoggerFactory.getLogger(ComponentsUtils.class.getName());

	@PostConstruct
	public void Init() {
		this.responseFormatManager = ResponseFormatManager.getInstance();
	}

	public IAuditingManager getAuditingManager() {
		return auditingManager;
	}

	public void setAuditingManager(IAuditingManager auditingManager) {
		this.auditingManager = auditingManager;
	}

	public <T> Either<T, ResponseFormat> convertJsonToObject(String data, User user, Class<T> clazz, AuditingActionEnum actionEnum) {
		if (data == null) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeInvalidJsonInput, "convertJsonToObject");
			BeEcompErrorManager.getInstance().logBeInvalidJsonInput("convertJsonToObject");
			log.debug("object is null after converting from json");
			ResponseFormat responseFormat = getInvalidContentErrorAndAudit(user, actionEnum);
			return Either.right(responseFormat);
		}
		try {
			T obj = parseJsonToObject(data, clazz);
			return Either.left(obj);
		} catch (Exception e) {
			// INVALID JSON
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeInvalidJsonInput, "convertJsonToObject");
			BeEcompErrorManager.getInstance().logBeInvalidJsonInput("convertJsonToObject");
			log.debug("failed to convert from json {}", data, e);
			ResponseFormat responseFormat = getInvalidContentErrorAndAudit(user, actionEnum);
			return Either.right(responseFormat);
		}
	}

	public static <T> T parseJsonToObject(String data, Class<T> clazz) {
		Type constraintType = new TypeToken<PropertyConstraint>() {
		}.getType();
		Gson gson = new GsonBuilder().registerTypeAdapter(constraintType, new PropertyConstraintDeserialiser()).create();
		log.trace("convert json to object. json=\n{}", data);
		T resource = gson.fromJson(data, clazz);
		return resource;
	}

	public <T> Either<T, ResponseFormat> convertJsonToObjectUsingObjectMapper(String data, User user, Class<T> clazz, AuditingActionEnum actionEnum, ComponentTypeEnum typeEnum) {
		T component = null;
		ObjectMapper mapper = new ObjectMapper().configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		try {
			log.trace("convert json to object. json=\n{}", data);

			final SimpleModule module = new SimpleModule("customerSerializationModule", new Version(1, 0, 0, "static version"));
			JsonDeserializer<PropertyConstraint> desrializer = new PropertyConstraintJacksonDeserialiser();
			addDeserializer(module, PropertyConstraint.class, desrializer);
			//
			mapper.registerModule(module);

			component = mapper.readValue(data, clazz);
			if (component == null) {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeInvalidJsonInput, "convertJsonToObject");
				BeEcompErrorManager.getInstance().logBeInvalidJsonInput("convertJsonToObject");
				log.debug("object is null after converting from json");
				ResponseFormat responseFormat = getInvalidContentErrorAndAuditComponent(user, actionEnum, typeEnum);
				return Either.right(responseFormat);
			}
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeInvalidJsonInput, "convertJsonToObject");
			BeEcompErrorManager.getInstance().logBeInvalidJsonInput("convertJsonToObject");
			log.debug("failed to convert from json {}", data, e);
			ResponseFormat responseFormat = getInvalidContentErrorAndAuditComponent(user, actionEnum, typeEnum);
			return Either.right(responseFormat);
		}
		return Either.left(component);
	}

	public <T> void addDeserializer(SimpleModule module, Class<T> clazz, final JsonDeserializer<T> deserializer) {
		module.addDeserializer(clazz, deserializer);
	}

	// Error response

	public ResponseFormat getResponseFormat(ActionStatus actionStatus, String... params) {
		return responseFormatManager.getResponseFormat(actionStatus, params);
	}

	public ResponseFormat getResponseFormat(StorageOperationStatus storageStatus, String... params) {
		return responseFormatManager.getResponseFormat(this.convertFromStorageResponse(storageStatus), params);
	}

	/**
	 * Returns the response format of resource error with respective variables according to actionStatus. This is needed for cases where actionStatus is anonymously converted from storage operation, and the caller doesn't know what actionStatus he
	 * received. It's caller's Responsibility to fill the resource object passed to this function with needed fields.
	 * 
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

		switch (actionStatus) {
		case RESOURCE_NOT_FOUND:
			responseFormat = getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, resourceName);
			break;
		default:
			responseFormat = getResponseFormat(actionStatus);
			break;
		}
		return responseFormat;
	}

	public ResponseFormat getResponseFormatByCapabilityType(ActionStatus actionStatus, CapabilityTypeDefinition capabilityType) {
		if (capabilityType == null) {
			return getResponseFormat(actionStatus);
		}
		ResponseFormat responseFormat;

		switch (actionStatus) {
		case CAPABILITY_TYPE_ALREADY_EXIST:
			responseFormat = getResponseFormat(ActionStatus.CAPABILITY_TYPE_ALREADY_EXIST, capabilityType.getType());
			break;
		default:
			responseFormat = getResponseFormat(actionStatus);
			break;
		}
		return responseFormat;
	}

	public <T> ResponseFormat getResponseFormatByElement(ActionStatus actionStatus, T  obj) {
		if (obj == null) {
			return getResponseFormat(actionStatus);
		}
		ResponseFormat responseFormat = null;

		switch (actionStatus) {
			case MISSING_CAPABILITY_TYPE:
				if (obj instanceof List && org.apache.commons.collections.CollectionUtils.isNotEmpty((List) obj)){
					List list = (List)obj;
					if ( list.get(0) instanceof RequirementDefinition ) {
						responseFormat = getResponseFormat(ActionStatus.MISSING_CAPABILITY_TYPE, ((RequirementDefinition) list.get(0)).getName());    //Arbitray index, all we need is single object
						return responseFormat;
					}
				}
				log.debug("UNKNOWN TYPE : expecting obj as a non empty List<RequirmentsDefinitions>");
				break;
			default:
				responseFormat = getResponseFormat(actionStatus);
				break;
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

	public ResponseFormat getResponseFormatByDE(ActionStatus actionStatus, String serviceId, String envName) {
		ResponseFormat responseFormat;

		switch (actionStatus) {
		case DISTRIBUTION_ENVIRONMENT_NOT_AVAILABLE:
			responseFormat = getResponseFormat(ActionStatus.DISTRIBUTION_ENVIRONMENT_NOT_AVAILABLE, envName);
			break;
		case DISTRIBUTION_ENVIRONMENT_NOT_FOUND:
			responseFormat = getResponseFormat(ActionStatus.DISTRIBUTION_ENVIRONMENT_NOT_FOUND, envName);
			break;
		case DISTRIBUTION_ARTIFACT_NOT_FOUND:
			responseFormat = getResponseFormat(ActionStatus.DISTRIBUTION_ARTIFACT_NOT_FOUND, serviceId);
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

	public ResponseFormat getInvalidContentErrorAndAudit(User user, AuditingActionEnum actionEnum) {
		ResponseFormat responseFormat = responseFormatManager.getResponseFormat(ActionStatus.INVALID_CONTENT);
		log.debug("audit before sending response");
		auditResource(responseFormat, user, null, "", "", actionEnum, null);
		return responseFormat;
	}

	public ResponseFormat getInvalidContentErrorAndAuditComponent(User user, AuditingActionEnum actionEnum, ComponentTypeEnum typeEnum) {
		ResponseFormat responseFormat = responseFormatManager.getResponseFormat(ActionStatus.INVALID_CONTENT);
		log.debug("audit before sending response");
		auditComponentAdmin(responseFormat, user, null, "", "", actionEnum, typeEnum);
		return responseFormat;
	}

	public void auditResource(ResponseFormat responseFormat, User modifier, Resource resource, String prevState, String prevVersion, AuditingActionEnum actionEnum, EnumMap<AuditingFieldsKeysEnum, Object> additionalParams) {
		if (actionEnum != null) {
			log.trace("Inside auditing for audit action {}", actionEnum.name());
			EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
			int status = responseFormat.getStatus();
			String message = "";

			if (responseFormat.getMessageId() != null) {
				message = responseFormat.getMessageId() + ": ";
			}
			message += responseFormat.getFormattedMessage();

			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, actionEnum.getName());

			updateUserFields(modifier, auditingFields);

			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, ComponentTypeEnum.RESOURCE.getValue());
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_VERSION, prevVersion);
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_STATE, prevState);
			if (resource != null) {
				// fields that are filled during creation and might still be
				// empty
				String resourceCurrVersion = (resource.getVersion() != null) ? resource.getVersion() : "";
				String resourceCurrState = (resource.getLifecycleState() != null) ? resource.getLifecycleState().name() : "";
				String uuid = (resource.getUUID() != null) ? resource.getUUID() : "";
				String invariantUUID = (resource.getInvariantUUID() != null) ? resource.getInvariantUUID() : "";

				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resource.getName());
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION, resourceCurrVersion);
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE, resourceCurrState);
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, uuid);
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_INVARIANT_UUID, invariantUUID);

			} else {
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_INVARIANT_UUID, "");
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, "");
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, "");
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION, "");
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE, "");
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_INVARIANT_UUID, "");
			}
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, message);

			// In those specific cases we want some specific fields from
			// resource object
			switch (actionEnum) {
			case IMPORT_RESOURCE:
				if (resource != null) {
					auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TOSCA_NODE_TYPE, resource.getToscaResourceName());
				} else {
					auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TOSCA_NODE_TYPE, "");
				}
				break;
			default:
				break;
			}

			// This is to add/overwrite anything set in this function if some
			// params were passed from above,
			// for example resourceName of resource import
			if (additionalParams != null) {
				auditingFields.putAll(additionalParams);
			}

			getAuditingManager().auditEvent(auditingFields);
		}
	}

	private void updateUserFields(User modifier, EnumMap<AuditingFieldsKeysEnum, Object> auditingFields) {
		if (modifier != null) {
			String firstName = modifier.getFirstName();
			String lastName = modifier.getLastName();
			if (firstName != null || lastName != null) {// to prevent "null
														// null" names
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, firstName + " " + lastName);
			}
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, modifier.getUserId());
		}
	}

	public void auditDistributionDownload(ResponseFormat responseFormat, AuditingActionEnum actionEnum, EnumMap<AuditingFieldsKeysEnum, Object> additionalParams) {
		log.trace("Inside auditing for audit action {}", actionEnum.name());
		int status = responseFormat.getStatus();
		String message = "";
		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		if (responseFormat.getMessageId() != null) {
			message = responseFormat.getMessageId() + ": ";
		}
		message += responseFormat.getFormattedMessage();
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, actionEnum.getName());
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, message);

		// This is to add/overwrite anything set in this function if some params
		// were passed from above,
		// for example resourceName of resource import
		if (additionalParams != null) {
			auditingFields.putAll(additionalParams);
		}

		getAuditingManager().auditEvent(auditingFields);
	}

	public void auditExternalGetAsset(ResponseFormat responseFormat, AuditingActionEnum actionEnum, EnumMap<AuditingFieldsKeysEnum, Object> additionalParams) {
		log.trace("Inside auditing for audit action {}", actionEnum.name());
		int status = responseFormat.getStatus();
		String message = "";
		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		if (responseFormat.getMessageId() != null) {
			message = responseFormat.getMessageId() + ": ";
		}
		message += responseFormat.getFormattedMessage();
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, actionEnum.getName());
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, message);

		if (additionalParams != null) {
			auditingFields.putAll(additionalParams);
		}

		getAuditingManager().auditEvent(auditingFields);
	}

	public void auditExternalCrudApi(ResponseFormat responseFormat, String componentType, String actionEnum, HttpServletRequest request, EnumMap<AuditingFieldsKeysEnum, Object> additionalParams) {
		log.trace("Inside auditing for audit action {}", actionEnum);
		String instanceIdHeader = request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER);
		String requestURI = request.getRequestURI();
		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		int status = 0;
		String message = "";
		if(responseFormat != null){
			status = responseFormat.getStatus();
			if (responseFormat.getMessageId() != null) {
				message = responseFormat.getMessageId() + ": ";
			}
			message += responseFormat.getFormattedMessage();
		}
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, actionEnum);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, instanceIdHeader);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL, requestURI);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, componentType);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, message);
		
		if (additionalParams != null) {
			auditingFields.putAll(additionalParams);
			if(!additionalParams.containsKey(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME)){
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, "");
			}
		}

		getAuditingManager().auditEvent(auditingFields);
	}

	public void auditExternalDownloadArtifact(ResponseFormat responseFormat, String componentType, HttpServletRequest request, EnumMap<AuditingFieldsKeysEnum, Object> additionalParams) {
		auditExternalCrudApi(responseFormat, componentType, AuditingActionEnum.DOWNLOAD_ARTIFACT.getName(), request, additionalParams);
	}

	public void auditExternalUploadArtifact(ResponseFormat responseFormat, String componentType, HttpServletRequest request, EnumMap<AuditingFieldsKeysEnum, Object> additionalParams) {
		additionalParams.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, request.getHeader(Constants.USER_ID_HEADER));
		additionalParams.put(AuditingFieldsKeysEnum.AUDIT_PREV_ARTIFACT_UUID, "");
		auditExternalCrudApi(responseFormat, componentType, AuditingActionEnum.ARTIFACT_UPLOAD_BY_API.getName(), request, additionalParams);
	}

	public void auditExternalUpdateArtifact(ResponseFormat responseFormat, String componentType, HttpServletRequest request, EnumMap<AuditingFieldsKeysEnum, Object> additionalParams) {
		additionalParams.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, request.getHeader(Constants.USER_ID_HEADER));
		additionalParams.put(AuditingFieldsKeysEnum.AUDIT_PREV_ARTIFACT_UUID, "");
		auditExternalCrudApi(responseFormat, componentType, AuditingActionEnum.ARTIFACT_UPDATE_BY_API.getName(), request, additionalParams);
	}

	public void auditExternalDeleteArtifact(ResponseFormat responseFormat, String componentType, HttpServletRequest request, EnumMap<AuditingFieldsKeysEnum, Object> additionalParams) {
		additionalParams.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, request.getHeader(Constants.USER_ID_HEADER));
		additionalParams.put(AuditingFieldsKeysEnum.AUDIT_PREV_ARTIFACT_UUID, "");
		auditExternalCrudApi(responseFormat, componentType, AuditingActionEnum.ARTIFACT_DELETE_BY_API.getName(), request, additionalParams);
	}

	public void auditCategory(ResponseFormat responseFormat, User modifier, String categoryName, String subCategoryName, String groupingName, AuditingActionEnum actionEnum, String componentType) {
		log.trace("Inside auditing for audit action {}", actionEnum.name());
		int status = responseFormat.getStatus();
		String message = "";

		if (responseFormat.getMessageId() != null) {
			message = responseFormat.getMessageId() + ": ";
		}
		message += responseFormat.getFormattedMessage();

		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		updateUserFields(modifier, auditingFields);
		// String componentTypeStr = (componentTypeEnum != null ?
		// componentTypeEnum.getValue() : null);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, componentType);

		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, actionEnum.getName());
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, message);

		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_CATEGORY_NAME, categoryName);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SUB_CATEGORY_NAME, subCategoryName);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_GROUPING_NAME, groupingName);
		getAuditingManager().auditEvent(auditingFields);
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
		case DISTR_ARTIFACT_NOT_FOUND:
			responseEnum = ActionStatus.DISTRIBUTION_ARTIFACT_NOT_FOUND;
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
		default:
			responseEnum = ActionStatus.GENERAL_ERROR;
			break;
		}
		log.debug("convert storage response {} to action response {}", storageResponse.name(), responseEnum.name());
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
		log.debug("convert storage response {} to action response {}", storageResponse.name(), responseEnum.name());
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
		log.debug("convert storage response {} to action response {}", storageResponse.name(), responseEnum.name());
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
		log.debug("convert storage response {} to action response {}", storageResponse.name(), responseEnum.name());
		return responseEnum;
	}

	public ResponseFormat getResponseFormatForResourceInstance(ActionStatus actionStatus, String serviceName, String resourceInstanceName) {
		ResponseFormat responseFormat;

		switch (actionStatus) {
		case RESOURCE_INSTANCE_NOT_FOUND:
			responseFormat = getResponseFormat(actionStatus, resourceInstanceName);
			break;
		default:
			responseFormat = getResponseFormat(actionStatus, serviceName);
			break;
		}
		return responseFormat;
	}

	public ResponseFormat getResponseFormatForResourceInstanceProperty(ActionStatus actionStatus, String resourceInstanceName) {
		ResponseFormat responseFormat;

		switch (actionStatus) {
		case RESOURCE_INSTANCE_NOT_FOUND:
			responseFormat = getResponseFormat(actionStatus, resourceInstanceName);
			break;
		default:
			responseFormat = getResponseFormat(actionStatus);
			break;
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
		log.debug("convert storage response {} to action response {}", storageResponse.name(), responseEnum.name());
		return responseEnum;
	}

	public void auditComponentAdmin(ResponseFormat responseFormat, User modifier, Component component, String prevState, String prevVersion, AuditingActionEnum actionEnum, ComponentTypeEnum type) {
		auditComponent(responseFormat, modifier, component, prevState, prevVersion, actionEnum, type, null);
	}

	public void auditComponent(ResponseFormat responseFormat, User modifier, Component component, String prevState, String prevVersion, AuditingActionEnum actionEnum, ComponentTypeEnum type, EnumMap<AuditingFieldsKeysEnum, Object> additionalParams) {
		if (actionEnum != null) {
			log.trace("Inside auditing for audit action {}", actionEnum.name());
			EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);

			int status = responseFormat.getStatus();
			String message = "";

			if (responseFormat.getMessageId() != null) {
				message = responseFormat.getMessageId() + ": ";
			}
			message += responseFormat.getFormattedMessage();

			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, actionEnum.getName());

			updateUserFields(modifier, auditingFields);
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, type.getValue().replace(" ", ""));

			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_VERSION, prevVersion);
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_STATE, prevState);
			if (component != null) {
				// fields that are filled during creation and might still be
				// empty
				String resourceCurrVersion = component.getVersion();
				String resourceCurrState = (component.getLifecycleState() != null) ? component.getLifecycleState().name() : "";
				String resourceUuid = (component.getUUID() != null) ? component.getUUID() : "";
				String invariantUUID = (component.getInvariantUUID() != null) ? component.getInvariantUUID() : "";

				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, component.getComponentMetadataDefinition().getMetadataDataDefinition().getName());
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION, resourceCurrVersion);
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE, resourceCurrState);
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, resourceUuid);
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_INVARIANT_UUID, invariantUUID);
			} else {
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, "");
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION, "");
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE, "");
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, "");
				auditingFields.put(AuditingFieldsKeysEnum.AUDIT_INVARIANT_UUID, "");
			}
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, message);

			// This is meant to overwrite anything set in this function if some
			// params were passed from above,
			// for example resourceName of resource import
			if (additionalParams != null) {
				auditingFields.putAll(additionalParams);
			}

			getAuditingManager().auditEvent(auditingFields);
		}
	}

	public void auditDistributionEngine(AuditingActionEnum actionEnum, String environmentName, String topicName, String role, String apiKey, String status) {

		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, actionEnum.getName());

		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ENVRIONMENT_NAME, environmentName);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_TOPIC_NAME, topicName);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ROLE, role);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_API_KEY, apiKey);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);

		getAuditingManager().auditEvent(auditingFields);
	}

	public void auditDistributionNotification(AuditingActionEnum actionEnum, String serviceUUID, String resourceName, String resourceType, String currVersion, String modifierUid, String modifierName, String environmentName, String currState,
			String topicName, String distributionId, String description, String status) {

		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, actionEnum.getName());
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, serviceUUID);

		// auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ENVRIONMENT_NAME,
		// environmentName);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_TOPIC_NAME, topicName);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID, distributionId);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION, currVersion);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE, currState);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, resourceType);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, description);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, modifierUid);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, modifierName);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceName);

		getAuditingManager().auditEvent(auditingFields);
	}

	public void auditAuthEvent(AuditingActionEnum actionEnum, String url, String user, String authStatus, String realm) {

		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, actionEnum.getName());
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_AUTH_URL, url);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_AUTH_USER, user);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_AUTH_STATUS, authStatus);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_AUTH_REALM, realm);
		getAuditingManager().auditEvent(auditingFields);
	}

	public void auditDistributionStatusNotification(AuditingActionEnum actionEnum, String distributionId, String consumerId, String topicName, String resourceUrl, String statusTime, String status, String errorReason) {

		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, actionEnum.getName());
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, distributionId);
		ThreadLocalsHolder.setUuid(distributionId);

		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID, distributionId);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, consumerId);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_TOPIC_NAME, topicName);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL, resourceUrl);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_TIME, statusTime);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, errorReason);

		getAuditingManager().auditEvent(auditingFields);
	}

	public void auditGetUebCluster(AuditingActionEnum actionEnum, String consumerId, String statusTime, String status, String description) {

		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, actionEnum.getName());
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, consumerId);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_TIME, statusTime);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_DESC, description);

		getAuditingManager().auditEvent(auditingFields);
	}

	public void auditMissingInstanceId(AuditingActionEnum actionEnum, String status, String description) {

		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, actionEnum.getName());
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, null);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP, null);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, description);

		getAuditingManager().auditEvent(auditingFields);
	}

	public void auditTopicACLKeys(AuditingActionEnum actionEnum, String envName, String topicName, String role, String apiPublicKey, String status) {

		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, actionEnum.getName());
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ENVRIONMENT_NAME, envName);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_TOPIC_NAME, topicName);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ROLE, role);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_API_KEY, apiPublicKey);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);

		getAuditingManager().auditEvent(auditingFields);
	}

	public void auditRegisterOrUnRegisterEvent(AuditingActionEnum actionEnum, String consumerId, String apiPublicKey, String envName, String status, String statusDesc, String notifTopicName, String statusTopicName) {
		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, actionEnum.getName());
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, consumerId);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_API_KEY, apiPublicKey);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ENVRIONMENT_NAME, envName);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_DESC, statusDesc);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_NOTIFICATION_TOPIC_NAME, notifTopicName);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_TOPIC_NAME, statusTopicName);
		getAuditingManager().auditEvent(auditingFields);
	}

	public void auditServiceDistributionDeployed(AuditingActionEnum actionEnum, String serviceName, String serviceVersion, String serviceUUID, String distributionId, String status, String desc, User modifier) {

		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, actionEnum.getName());
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, serviceUUID);

		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, modifier.getFirstName() + " " + modifier.getLastName());
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, modifier.getUserId());
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, "Service");
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, serviceName);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION, serviceVersion);

		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID, distributionId);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, desc);

		getAuditingManager().auditEvent(auditingFields);
	}

	public void auditConsumerCredentialsEvent(AuditingActionEnum actionEnum, ConsumerDefinition consumer, ResponseFormat responseFormat, User modifier) {

		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, actionEnum.getName());
		if (modifier != null) {
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, modifier.getFirstName() + " " + modifier.getLastName());
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, modifier.getUserId());
		}
		StringBuilder ecompUser = new StringBuilder();
		if (consumer != null) {
			if (consumer.getConsumerName() != null && !consumer.getConsumerName().trim().isEmpty()) {
				ecompUser.append(consumer.getConsumerName());
			}
			if (consumer.getConsumerSalt() != null && !consumer.getConsumerSalt().trim().isEmpty()) {
				if (ecompUser.length() > 0) {
					ecompUser.append(",");
				}
				ecompUser.append(consumer.getConsumerSalt());
			}
			if (consumer.getConsumerPassword() != null && !consumer.getConsumerPassword().trim().isEmpty()) {
				if (ecompUser.length() > 0) {
					ecompUser.append(",");
				}
				ecompUser.append(consumer.getConsumerPassword());
			}
		}
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ECOMP_USER, ecompUser.toString());
		int status = responseFormat.getStatus();
		String message = "";

		if (responseFormat.getMessageId() != null) {
			message = responseFormat.getMessageId() + ": ";
		}
		message += responseFormat.getFormattedMessage();
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, message);

		getAuditingManager().auditEvent(auditingFields);
	}

	public void auditGetUsersList(AuditingActionEnum actionEnum, User modifier, String details, ResponseFormat responseFormat) {

		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, actionEnum.getName());
		if (modifier != null) {
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, modifier.getFirstName() + " " + modifier.getLastName());
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, modifier.getUserId());
		}

		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_USER_DETAILS, details);
		int status = responseFormat.getStatus();
		String message = "";

		if (responseFormat.getMessageId() != null) {
			message = responseFormat.getMessageId() + ": ";
		}
		message += responseFormat.getFormattedMessage();
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, message);
		getAuditingManager().auditEvent(auditingFields);
	}

	public void auditAdminUserAction(AuditingActionEnum actionEnum, User modifier, User userBefore, User userAfter, ResponseFormat responseFormat) {

		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, actionEnum.getName());
		if (modifier != null) {
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, modifier.getFirstName() + " " + modifier.getLastName());
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, modifier.getUserId());
		} else {
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, "");
		}
		if (userBefore != null) {
			String userBeforeUserId = (userBefore.getUserId() != null) ? userBefore.getUserId() : "";
			String userBeforFirstName = (userBefore.getFirstName() != null) ? ", " + userBefore.getFirstName() + " " : "";
			String userBeforLastName = (userBefore.getLastName() != null) ? userBefore.getLastName() : "";
			String userBeforEmail = (userBefore.getEmail() != null) ? ", " + userBefore.getEmail() : "";
			String userBeforRloe = (userBefore.getRole() != null) ? ", " + userBefore.getRole() : "";

			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_USER_BEFORE, userBeforeUserId + userBeforFirstName + userBeforLastName + userBeforEmail + userBeforRloe);
		} else {
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_USER_BEFORE, "");
		}
		if (userAfter != null) {
			String userAfterUserId = (userAfter.getUserId() != null) ? userAfter.getUserId() : "";
			String userAfterFirstName = (userAfter.getFirstName() != null) ? ", " + userAfter.getFirstName() + " " : "";
			String userAfterLastName = (userAfter.getLastName() != null) ? userAfter.getLastName() : "";
			String userAfterEmail = (userAfter.getEmail() != null) ? ", " + userAfter.getEmail() : "";
			String userAfterRloe = (userAfter.getRole() != null) ? ", " + userAfter.getRole() : "";

			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_USER_AFTER, userAfterUserId + userAfterFirstName + userAfterLastName + userAfterEmail + userAfterRloe);
		} else {
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_USER_AFTER, "");
		}

		int status = responseFormat.getStatus();
		String message = "";

		if (responseFormat.getMessageId() != null) {
			message = responseFormat.getMessageId() + ": ";
		}
		message += responseFormat.getFormattedMessage();
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, message);
		getAuditingManager().auditEvent(auditingFields);
	}

	public void auditUserAccess(AuditingActionEnum actionEnum, User user, ResponseFormat responseFormat) {

		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, actionEnum.getName());
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_USER_UID, user.getFirstName() + " " + user.getLastName() + '(' + user.getUserId() + ')');
		int status = responseFormat.getStatus();
		String message = "";

		if (responseFormat.getMessageId() != null) {
			message = responseFormat.getMessageId() + ": ";
		}
		message += responseFormat.getFormattedMessage();
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, message);
		getAuditingManager().auditEvent(auditingFields);
	}

	public void auditGetCategoryHierarchy(AuditingActionEnum actionEnum, User modifier, String details, ResponseFormat responseFormat) {

		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, actionEnum.getName());
		if (modifier != null) {
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, modifier.getFirstName() + " " + modifier.getLastName());
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, modifier.getUserId());
		}
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DETAILS, details);
		int status = responseFormat.getStatus();
		String message = "";

		if (responseFormat.getMessageId() != null) {
			message = responseFormat.getMessageId() + ": ";
		}
		message += responseFormat.getFormattedMessage();
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, message);
		getAuditingManager().auditEvent(auditingFields);
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

	public Either<Boolean, ResponseFormat> validateStringNotEmpty(User user, Component component, String value, ActionStatus badResult, AuditingActionEnum actionEnum) {
		if ((value == null) || (value.trim().isEmpty())) {
			log.info(badResult.name());
			ResponseFormat errorResponse = getResponseFormat(badResult);
			if (actionEnum != null) {
				log.debug("audit before sending response");
				auditComponentAdmin(errorResponse, user, component, "", "", actionEnum, getComponentType(component));
			}
			return Either.right(errorResponse);
		}
		return Either.left(true);
	}

	public Boolean validateStringNotEmpty(String value) {
		if ((value == null) || (value.trim().isEmpty())) {
			return false;
		}
		return true;
	}

	private ComponentTypeEnum getComponentType(Component component) {
		if (component instanceof Service) {
			return ComponentTypeEnum.SERVICE;
		} // else if(component instanceof Resource){
		return null;
	}

	public Either<Boolean, ResponseFormat> validateStringMatchesPattern(User user, Component component, String value, Pattern pattern, ActionStatus badResult, AuditingActionEnum actionEnum) {
		if (!pattern.matcher(value).matches()) {
			log.error(badResult.name());
			ResponseFormat errorResponse = getResponseFormat(badResult);
			if (actionEnum != null) {
				log.debug("audit before sending response");
				auditComponentAdmin(errorResponse, user, component, "", "", actionEnum, getComponentType(component));
			}
			return Either.right(errorResponse);
		}
		return Either.left(true);
	}

	/**
	 * 
	 * " Error: Missing Mandatory Informational %s1 %s2 : %s3 " where %s1 - "resource"/"service" %s2 - "artifact"/ "artifacts" %s3 - Comma separated list of missing informational artifact types
	 * 
	 * @param resource
	 * @param componentMissingMandatoryArtifacts
	 * @param value
	 * @return
	 */
	public ResponseFormat getResponseFormatByMissingArtifacts(ComponentTypeEnum componentType, Map<String, ArtifactDefinition> artifacts) {

		String artifactTitle = "artifact";
		if (artifacts.size() > 1) {
			artifactTitle = "artifacts";
		}
		Collection<ArtifactDefinition> artifactsLabels = artifacts.values();
		StringBuilder artifactsLabelBuilder = new StringBuilder();

		List<ArtifactDefinition> artifactsLabelsList = new ArrayList<ArtifactDefinition>();
		artifactsLabelsList.addAll(artifactsLabels);
		for (int i = 0; i < artifactsLabelsList.size(); i++) {
			ArtifactDefinition artifactDef = artifactsLabelsList.get(i);
			artifactsLabelBuilder.append(artifactDef.getArtifactDisplayName());
			if (i < artifactsLabelsList.size() - 1) {
				artifactsLabelBuilder.append(";");
			}
		}
		ResponseFormat responseFormat = getResponseFormat(ActionStatus.COMPONENT_MISSING_MANDATORY_ARTIFACTS, componentType.name().toLowerCase(), artifactTitle, artifactsLabelBuilder.toString());

		return responseFormat;
	}

	public ActionStatus convertFromStorageResponseForAdditionalInformation(StorageOperationStatus storageResponse) {
		ActionStatus responseEnum = ActionStatus.GENERAL_ERROR;

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
		log.debug("convert storage response {} to action response {}", storageResponse.name(), responseEnum.name());
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
		log.debug("convert storage response {} to action response {}", storageResponse.name(), responseEnum.name());
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
		log.debug("convert storage response {} to action response {}", storageResponse.name(), responseEnum.name());
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
		log.debug("convert storage response {} to action response {}", storageResponse.name(), responseEnum.name());
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

		switch (actionStatus) {
		case POLICY_TYPE_ALREADY_EXIST:
			responseFormat = getResponseFormat(actionStatus, policyType.getType());
			break;
		default:
			responseFormat = getResponseFormat(actionStatus);
			break;
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
}
