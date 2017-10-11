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

package org.openecomp.sdc.be.components.impl;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.datamodel.api.HighestFilterEnum;
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.*;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapReqDef;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.IComponentInstanceConnectedElement;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ComponentCache;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.utils.CommonBeUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fj.data.Either;

public abstract class ComponentBusinessLogic extends BaseBusinessLogic {

	@Autowired
	protected ArtifactsBusinessLogic artifactsBusinessLogic;

	@Autowired
	protected ComponentCache componentCache;


	private static Logger log = LoggerFactory.getLogger(ComponentBusinessLogic.class.getName());

	private static final String TAG_FIELD_LABEL = "tag";

	public abstract Either<List<String>, ResponseFormat> deleteMarkedComponents();

	public abstract ComponentInstanceBusinessLogic getComponentInstanceBL();

	public abstract Either<List<ComponentInstance>, ResponseFormat> getComponentInstancesFilteredByPropertiesAndInputs(String componentId, ComponentTypeEnum componentTypeEnum, String userId, String searchText);

	/**
	 * 
	 * @param componentId
	 * @param dataParamsToReturn
	 * @return
	 */
	public abstract  Either<UiComponentDataTransfer, ResponseFormat> getUiComponentDataTransferByComponentId(String componentId, List<String> dataParamsToReturn);

	protected Either<User, ResponseFormat> validateUser(User user, String ecompErrorContext, Component component, AuditingActionEnum auditAction, boolean inTransaction) {
		Either<User, ResponseFormat> userValidationResult = validateUserNotEmpty(user, ecompErrorContext);
		ResponseFormat responseFormat;
		if (userValidationResult.isRight()) {
			user.setUserId("UNKNOWN");
			responseFormat = userValidationResult.right().value();
			componentsUtils.auditComponentAdmin(responseFormat, user, component, "", "", auditAction, component.getComponentType());
			return Either.right(responseFormat);
		}
		Either<User, ResponseFormat> userResult = validateUserExists(user, ecompErrorContext, inTransaction);
		if (userResult.isRight()) {
			responseFormat = userResult.right().value();
			componentsUtils.auditComponentAdmin(responseFormat, user, component, "", "", auditAction, component.getComponentType());
			return Either.right(responseFormat);
		}
		user = userResult.left().value();
		return userResult;
	}

	protected Either<Boolean, ResponseFormat> validateUserRole(User user, Component component, List<Role> roles, AuditingActionEnum auditAction, String comment) {
		if (roles != null && roles.isEmpty()) {
			roles.add(Role.ADMIN);
			roles.add(Role.DESIGNER);
		}
		Either<Boolean, ResponseFormat> validationResult = validateUserRole(user, roles);
		if (validationResult.isRight()) {
			ComponentTypeEnum componentType = component.getComponentType();
			EnumMap<AuditingFieldsKeysEnum, Object> additionalParams = new EnumMap<>(AuditingFieldsKeysEnum.class);
			if (componentType.equals(ComponentTypeEnum.SERVICE)) {
				additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_COMMENT, comment);
				String distributionStatus = ((ServiceMetadataDataDefinition) component.getComponentMetadataDefinition().getMetadataDataDefinition()).getDistributionStatus();
				additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_DPREV_STATUS, distributionStatus);
				additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_DCURR_STATUS, distributionStatus);
			}
			componentsUtils.auditComponent(validationResult.right().value(), user, component, "", "", auditAction, componentType, additionalParams);
		}
		return validationResult;
	}

	protected Either<Boolean, ResponseFormat> validateComponentName(User user, Component component, AuditingActionEnum actionEnum) {
		ComponentTypeEnum type = component.getComponentType();
		String componentName = component.getName();
		if (!ValidationUtils.validateStringNotEmpty(componentName)) {
			log.debug("component name is empty");
			ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_COMPONENT_NAME, type.getValue());
			componentsUtils.auditComponentAdmin(errorResponse, user, component, "", "", actionEnum, type);
			return Either.right(errorResponse);
		}

		if (!ValidationUtils.validateComponentNameLength(componentName)) {
			log.debug("Component name exceeds max length {} ", ValidationUtils.COMPONENT_NAME_MAX_LENGTH);
			ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_NAME_EXCEEDS_LIMIT, type.getValue(), "" + ValidationUtils.COMPONENT_NAME_MAX_LENGTH);
			componentsUtils.auditComponentAdmin(errorResponse, user, component, "", "", actionEnum, type);
			return Either.right(errorResponse);
		}

		if (!validateTagPattern(componentName)) {
			log.debug("Component name {} has invalid format", componentName);
			ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_COMPONENT_NAME, type.getValue());
			componentsUtils.auditComponentAdmin(errorResponse, user, component, "", "", actionEnum, type);
			return Either.right(errorResponse);
		}
		component.setNormalizedName(ValidationUtils.normaliseComponentName(componentName));
		component.setSystemName(ValidationUtils.convertToSystemName(componentName));

		return Either.left(true);
	}

	protected Either<Boolean, ResponseFormat> validateDescriptionAndCleanup(User user, Component component, AuditingActionEnum actionEnum) {
		ComponentTypeEnum type = component.getComponentType();
		String description = component.getDescription();
		if (!ValidationUtils.validateStringNotEmpty(description)) {
			ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_DESCRIPTION, type.getValue());
			componentsUtils.auditComponentAdmin(errorResponse, user, component, "", "", actionEnum, type);
			return Either.right(errorResponse);
		}

		description = cleanUpText(description);
		Either<Boolean, ResponseFormat> validatDescription = validateComponentDescription(description, type);
		if (validatDescription.isRight()) {
			ResponseFormat responseFormat = validatDescription.right().value();
			componentsUtils.auditComponentAdmin(responseFormat, user, component, "", "", actionEnum, type);
			return Either.right(responseFormat);
		}
		component.setDescription(description);
		return Either.left(true);
	}

	public Either<Boolean, ResponseFormat> validateComponentDescription(String description, ComponentTypeEnum type) {
		if (description != null) {
			if (!ValidationUtils.validateDescriptionLength(description)) {
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_DESCRIPTION_EXCEEDS_LIMIT, type.getValue(), "" + ValidationUtils.COMPONENT_DESCRIPTION_MAX_LENGTH));
			}

			if (!ValidationUtils.validateIsEnglish(description)) {
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INVALID_DESCRIPTION, type.getValue()));
			}
			return Either.left(true);
		}
		return Either.left(false);
	}

	protected Either<Boolean, ResponseFormat> validateComponentNameUnique(User user, Component component, AuditingActionEnum actionEnum) {
		ComponentTypeEnum type = component.getComponentType();
		ResourceTypeEnum resourceType = null;
		if(component instanceof Resource){
			resourceType = ((Resource)component).getResourceType();
		}
		Either<Boolean, StorageOperationStatus> dataModelResponse = toscaOperationFacade.validateComponentNameExists(component.getName(), resourceType, type);

		if (dataModelResponse.isLeft()) {
			if ( !dataModelResponse.left().value()) {
				return Either.left(true);
			} else {
				log.info("Component with name {} already exists", component.getName());
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, type.getValue(), component.getName());
				componentsUtils.auditComponentAdmin(errorResponse, user, component, "", "", actionEnum, type);
				return Either.right(errorResponse);
			}
		}
		BeEcompErrorManager.getInstance().logBeSystemError("validateComponentNameUnique");
		log.debug("Error while validateComponentNameUnique for component: {}", component.getName());
		ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
		componentsUtils.auditComponentAdmin(errorResponse, user, component, "", "", actionEnum, type);
		return Either.right(errorResponse);
	}

	protected Either<Boolean, ResponseFormat> validateContactId(User user, Component component, AuditingActionEnum actionEnum) {
		log.debug("validate component contactId");
		ComponentTypeEnum type = component.getComponentType();
		String contactId = component.getContactId();

		if (!ValidationUtils.validateStringNotEmpty(contactId)) {
			log.info("contact is missing.");
			ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_CONTACT, type.getValue());
			componentsUtils.auditComponentAdmin(errorResponse, user, component, "", "", actionEnum, type);
			return Either.right(errorResponse);
		}

		Either<Boolean, ResponseFormat> validateContactIdResponse = validateContactId(contactId, type);
		if (validateContactIdResponse.isRight()) {
			ResponseFormat responseFormat = validateContactIdResponse.right().value();
			componentsUtils.auditComponentAdmin(responseFormat, user, component, "", "", actionEnum, type);
		}
		return validateContactIdResponse;
	}

	private Either<Boolean, ResponseFormat> validateContactId(String contactId, ComponentTypeEnum type) {
		if (contactId != null) {
			if (!ValidationUtils.validateContactId(contactId)) {
				log.info("contact is invalid.");
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INVALID_CONTACT, type.getValue());
				return Either.right(errorResponse);
			}
			return Either.left(true);
		}
		return Either.left(false);
	}

	
	public Either<Boolean, ResponseFormat> validateConformanceLevel(String componentUuid, ComponentTypeEnum componentTypeEnum, String userId) {
		log.trace("validate conformance level");
		
		if (componentTypeEnum != ComponentTypeEnum.SERVICE) {
			log.error("conformance level validation for non service component, id {}", componentUuid);
			ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
			return Either.right(errorResponse);
		}
		
		Either<User, ResponseFormat> resp = validateUserExists(userId, "validateConformanceLevel", false);
		if (resp.isRight()) {
			log.error("can't validate conformance level, user is not validated, uuid {}, userId {}", componentUuid, userId);
			return Either.right(resp.right().value());
		}
		
		Either<ComponentMetadataData, StorageOperationStatus> eitherComponent = toscaOperationFacade.getLatestComponentMetadataByUuid(componentUuid, JsonParseFlagEnum.ParseMetadata, null);
		if (eitherComponent.isRight()) {
			log.error("can't validate conformance level, component not found, uuid {}", componentUuid);
			BeEcompErrorManager.getInstance().logBeComponentMissingError("validateConformanceLevel", componentTypeEnum.getValue(), componentUuid);
			
			StorageOperationStatus status = eitherComponent.right().value();
			ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(status, componentTypeEnum);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(actionStatus);
			return Either.right(responseFormat);
		}
		
		String componentConformanceLevel = eitherComponent.left().value().getMetadataDataDefinition().getConformanceLevel();
		if (StringUtils.isBlank(componentConformanceLevel)) {
			log.error("component conformance level property is null or empty, uuid {}", componentUuid);
			ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
			return Either.right(errorResponse);
		}
		
		String configConformanceLevel = ConfigurationManager.getConfigurationManager().getConfiguration().getMinToscaConformanceLevel();
		Boolean result = true;
		if (CommonBeUtils.conformanceLevelCompare(componentConformanceLevel, configConformanceLevel) < 0) {
			log.error("invalid asset conformance level, uuid {}, asset conformanceLevel {}, config conformanceLevel {}", componentUuid, componentConformanceLevel, configConformanceLevel);
			result = false;
		}
		log.trace("conformance level validation finished");
		
		return Either.left(result);
	}
	
	protected Either<Boolean, ResponseFormat> validateIcon(User user, Component component, AuditingActionEnum actionEnum) {
		log.debug("validate Icon");
		ComponentTypeEnum type = component.getComponentType();
		String icon = component.getIcon();
		if (!ValidationUtils.validateStringNotEmpty(icon)) {
			log.info("icon is missing.");
			ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_ICON, type.getValue());
			componentsUtils.auditComponentAdmin(errorResponse, user, component, "", "", actionEnum, type);
			return Either.right(errorResponse);
		}

		Either<Boolean, ResponseFormat> validateIcon = validateIcon(icon, type);
		if (validateIcon.isRight()) {
			ResponseFormat responseFormat = validateIcon.right().value();
			componentsUtils.auditComponentAdmin(responseFormat, user, component, "", "", actionEnum, type);
		}
		return validateIcon;
	}

	private Either<Boolean, ResponseFormat> validateIcon(String icon, ComponentTypeEnum type) {
		if (icon != null) {
			if (!ValidationUtils.validateIconLength(icon)) {
				log.debug("icon exceeds max length");
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_ICON_EXCEEDS_LIMIT, type.getValue(), "" + ValidationUtils.ICON_MAX_LENGTH));
			}

			if (!ValidationUtils.validateIcon(icon)) {
				log.info("icon is invalid.");
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INVALID_ICON, type.getValue());
				return Either.right(errorResponse);
			}
			return Either.left(true);
		}
		return Either.left(false);
	}

	protected Either<Boolean, ResponseFormat> validateTagsListAndRemoveDuplicates(User user, Component component, AuditingActionEnum actionEnum) {
		List<String> tagsList = component.getTags();

		Either<Boolean, ResponseFormat> validateTags = validateComponentTags(tagsList, component.getName(), component.getComponentType());
		if (validateTags.isRight()) {
			ResponseFormat responseFormat = validateTags.right().value();
			componentsUtils.auditComponentAdmin(responseFormat, user, component, "", "", actionEnum, component.getComponentType());
			return Either.right(responseFormat);
		}
		ValidationUtils.removeDuplicateFromList(tagsList);
		return Either.left(true);
	}

	protected Either<Boolean, ResponseFormat> validateComponentTags(List<String> tags, String name, ComponentTypeEnum componentType) {
		log.debug("validate component tags");
		boolean includesComponentName = false;
		int tagListSize = 0;
		if (tags != null && !tags.isEmpty()) {
			for (String tag : tags) {
				if (!ValidationUtils.validateTagLength(tag)) {
					log.debug("tag length exceeds limit {}", ValidationUtils.TAG_MAX_LENGTH);
					return Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_SINGLE_TAG_EXCEED_LIMIT, "" + ValidationUtils.TAG_MAX_LENGTH));
				}
				if (validateTagPattern(tag)) {
					if (!includesComponentName) {
						includesComponentName = name.equals(tag);
					}
				} else {
					log.debug("invalid tag {}", tag);
					return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_FIELD_FORMAT, componentType.getValue(), TAG_FIELD_LABEL));
				}
				tagListSize += tag.length() + 1;
			}
			if (tagListSize > 0) {
				tagListSize--;
			}

			if (!includesComponentName) {
				log.debug("tags must include component name");
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INVALID_TAGS_NO_COMP_NAME));
			}
			if (!ValidationUtils.validateTagListLength(tagListSize)) {
				log.debug("overall tags length exceeds limit {}", ValidationUtils.TAG_LIST_MAX_LENGTH);
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_TAGS_EXCEED_LIMIT, "" + ValidationUtils.TAG_LIST_MAX_LENGTH));
			}
			return Either.left(true);
		}
		return Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_TAGS));
	}

	protected boolean validateTagPattern(String tag) {
		return ValidationUtils.validateComponentNamePattern(tag);
	}

	protected Either<Boolean, ResponseFormat> validateProjectCode(User user, Component component, AuditingActionEnum actionEnum) {
		if (ComponentTypeEnum.RESOURCE.equals(component.getComponentType())) {
			return Either.left(true);
		}
		log.debug("validate ProjectCode name ");
		String projectCode = component.getProjectCode();

		if (!ValidationUtils.validateStringNotEmpty(projectCode)) {
			log.info("projectCode is missing.");
			ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_PROJECT_CODE);
			componentsUtils.auditComponentAdmin(errorResponse, user, component, "", "", actionEnum, component.getComponentType());
			return Either.right(errorResponse);
		}

		Either<Boolean, ResponseFormat> validateProjectCodeResponse = validateProjectCode(projectCode);
		if (validateProjectCodeResponse.isRight()) {
			ResponseFormat responseFormat = validateProjectCodeResponse.right().value();
			componentsUtils.auditComponentAdmin(responseFormat, user, component, "", "", actionEnum, component.getComponentType());
		}
		return validateProjectCodeResponse;

	}

	private Either<Boolean, ResponseFormat> validateProjectCode(String projectCode) {
		if (projectCode != null) {
			if (!ValidationUtils.validateProjectCode(projectCode)) {
				log.info("projectCode  is not valid.");
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_PROJECT_CODE);
				return Either.right(errorResponse);
			}
			return Either.left(true);
		}
		return Either.left(false);
	}

	protected void checkComponentFieldsForOverrideAttempt(Component component) {
		if (component.getLifecycleState() != null) {
			log.info("LifecycleState cannot be defined by user. This field will be overridden by the application");
		}
		if (component.getVersion() != null) {
			log.info("Version cannot be defined by user. This field will be overridden by the application");
		}
		if ((component.getCreatorUserId() != null) || (component.getCreatorFullName() != null)) {
			log.info("Creator cannot be defined by user. This field will be overridden by the application");
		}
		if ((component.getLastUpdaterUserId() != null) || (component.getLastUpdaterFullName() != null)) {
			log.info("Last Updater cannot be defined by user. This field will be overridden by the application");
		}
		if ((component.getCreationDate() != null)) {
			log.info("Creation Date cannot be defined by user. This field will be overridden by the application");
		}
		if ((component.isHighestVersion() != null)) {
			log.info("Is Highest Version cannot be defined by user. This field will be overridden by the application");
		}
		if ((component.getUUID() != null)) {
			log.info("UUID cannot be defined by user. This field will be overridden by the application");
		}
		if ((component.getLastUpdateDate() != null)) {
			log.info("Last Update Date cannot be defined by user. This field will be overridden by the application");
		}
		if (component.getUniqueId() != null) {
			log.info("uid cannot be defined by user. This field will be overridden by the application.");
			component.setUniqueId(null);
		}
		if (component.getInvariantUUID() != null) {
			log.info("Invariant UUID cannot be defined by user. This field will be overridden by the application.");
		}
	}

	protected Either<Boolean, ResponseFormat> validateComponentFieldsBeforeCreate(User user, Component component, AuditingActionEnum actionEnum) {
		// validate component name uniqueness
		log.debug("validate component name ");
		Either<Boolean, ResponseFormat> componentNameValidation = validateComponentName(user, component, actionEnum);
		if (componentNameValidation.isRight()) {
			return componentNameValidation;
		}

		// validate description
		log.debug("validate description");
		Either<Boolean, ResponseFormat> descValidation = validateDescriptionAndCleanup(user, component, actionEnum);
		if (descValidation.isRight()) {
			return descValidation;
		}

		// validate tags
		log.debug("validate tags");
		Either<Boolean, ResponseFormat> tagsValidation = validateTagsListAndRemoveDuplicates(user, component, actionEnum);
		if (tagsValidation.isRight()) {
			return tagsValidation;
		}

		// validate contact info
		log.debug("validate contact info");
		Either<Boolean, ResponseFormat> contactIdValidation = validateContactId(user, component, actionEnum);
		if (contactIdValidation.isRight()) {
			return contactIdValidation;
		}

		// validate icon
		log.debug("validate icon");
		Either<Boolean, ResponseFormat> iconValidation = validateIcon(user, component, actionEnum);
		if (iconValidation.isRight()) {
			return iconValidation;
		}
		return Either.left(true);
	}

	/***
	 * Fetches Component From the DB
	 * 
	 * @param componentId
	 * @param componentTypeEnum
	 * @return
	 */
	public <R extends Component> Either<R, StorageOperationStatus> getComponent(String componentId, ComponentTypeEnum componentTypeEnum) {
		return toscaOperationFacade.getToscaElement(componentId);
	}

	public Either<CapReqDef, ResponseFormat> getRequirementsAndCapabilities(String componentId, ComponentTypeEnum componentTypeEnum, String userId) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "create Component Instance", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}
		Either<CapReqDef, ResponseFormat> eitherRet = null;
		ComponentParametersView filter = new ComponentParametersView(true);
		filter.setIgnoreCapabilities(false);
		filter.setIgnoreRequirements(false);
		filter.setIgnoreComponentInstances(false);
		Either<Component, ResponseFormat> eitherComponent = validateComponentExists(componentId, componentTypeEnum, filter);
		if (eitherComponent.isLeft()) {
			eitherRet = Either.left(new CapReqDef(eitherComponent.left().value().getRequirements(), eitherComponent.left().value().getCapabilities()));
		} else {
			BeEcompErrorManager.getInstance().logBeComponentMissingError("getRequirementsAndCapabilities", componentTypeEnum.getValue(), componentId);
			eitherRet = Either.right(eitherComponent.right().value());
		}
		return eitherRet;
	}

	public Either<List<Component>, ResponseFormat> getLatestVersionNotAbstractComponents(boolean isAbstractAbstract, HighestFilterEnum highestFilter, ComponentTypeEnum componentTypeEnum, String internalComponentType, List<String> componentUids,
			String userId) {
		ResponseFormat responseFormat = null;
		try{
			Either<User, ResponseFormat> resp = validateUserExists(userId, "get Latest Version Not Abstract Components", false);
			
			if (resp.isLeft()) {
				List<Component> result = new ArrayList<>();
				List<String> componentsUidToFetch = new ArrayList<>();
				componentsUidToFetch.addAll(componentUids);
	
				if (componentsUidToFetch.size() > 0) {
					log.debug("Number of Components to fetch from graph is {}", componentsUidToFetch.size());
					Boolean isHighest = isHighest(highestFilter);
					Either<List<Component>, StorageOperationStatus> nonCheckoutCompResponse = toscaOperationFacade.getLatestVersionNotAbstractComponents(isAbstractAbstract, isHighest, componentTypeEnum, internalComponentType, componentsUidToFetch);
	
					if (nonCheckoutCompResponse.isLeft()) {
						log.debug("Retrived Resource successfully.");
						result.addAll(nonCheckoutCompResponse.left().value());
					} else {
						responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(nonCheckoutCompResponse.right().value()));
					}
				}
				return Either.left(result);
			} else {
				responseFormat = resp.right().value();
			}
		}
		finally{
			titanDao.commit();
		}
		return Either.right(responseFormat);
	}

	private Boolean isHighest(HighestFilterEnum highestFilter) {
		Boolean isHighest = null;
		switch (highestFilter) {
		case ALL:
			break;
		case HIGHEST_ONLY:
			isHighest = true;
			break;
		case NON_HIGHEST_ONLY:
			isHighest = false;
			break;
		default:
			break;
		}
		return isHighest;
	}

	public Either<List<Component>, ResponseFormat> getLatestVersionNotAbstractComponentsMetadata(boolean isAbstractAbstract, HighestFilterEnum highestFilter, ComponentTypeEnum componentTypeEnum, String internalComponentType, String userId) {
		ResponseFormat responseFormat = null;

		try{
			Either<User, ResponseFormat> resp = validateUserExists(userId, "get Latest Version Not Abstract Components", false);
			if (resp.isLeft()) {
	
				Boolean isHighest = isHighest(highestFilter);
				Either<List<Component>, StorageOperationStatus> nonCheckoutCompResponse = toscaOperationFacade.getLatestVersionNotAbstractMetadataOnly(isAbstractAbstract, isHighest, componentTypeEnum, internalComponentType);

				if (nonCheckoutCompResponse.isLeft()) {
					log.debug("Retrived Resource successfully.");
					return Either.left(nonCheckoutCompResponse.left().value());
				}
				responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(nonCheckoutCompResponse.right().value()));
			} else {
				responseFormat = resp.right().value();
			}
		} finally {
			titanDao.commit();
		}
		return Either.right(responseFormat);
	}

	public void setDeploymentArtifactsPlaceHolder(Component component, User user) {

	}

	@SuppressWarnings("unchecked")
	public void setToscaArtifactsPlaceHolders(Component component, User user) {
		Map<String, ArtifactDefinition> artifactMap = component.getToscaArtifacts();
		if (artifactMap == null) {
			artifactMap = new HashMap<String, ArtifactDefinition>();
		}
		String componentUniqueId = component.getUniqueId();
		String componentSystemName = component.getSystemName();
		String componentType = component.getComponentType().getValue().toLowerCase();
		Map<String, Object> toscaArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration().getToscaArtifacts();

		if (toscaArtifacts != null) {
			for (Entry<String, Object> artifactInfoMap : toscaArtifacts.entrySet()) {
				Map<String, Object> artifactInfo = (Map<String, Object>) artifactInfoMap.getValue();
				ArtifactDefinition artifactDefinition = artifactsBusinessLogic.createArtifactPlaceHolderInfo(componentUniqueId, artifactInfoMap.getKey(), artifactInfo, user, ArtifactGroupTypeEnum.TOSCA);
				artifactDefinition.setArtifactName(componentType + "-" + componentSystemName + artifactInfo.get("artifactName"));
				artifactMap.put(artifactDefinition.getArtifactLabel(), artifactDefinition);
			}
		}
		component.setToscaArtifacts(artifactMap);
	}

	public Either<Either<ArtifactDefinition, Operation>, ResponseFormat> populateToscaArtifacts(Component component, User user, boolean isInCertificationRequest, boolean inTransaction, boolean shouldLock) {
		return populateToscaArtifacts(component, user, isInCertificationRequest, inTransaction, shouldLock, true);
	}

	public Either<Either<ArtifactDefinition, Operation>, ResponseFormat> populateToscaArtifacts(Component component, User user, boolean isInCertificationRequest, boolean inTransaction, boolean shouldLock, boolean fetchTemplatesFromDB) {
		Either<Component, StorageOperationStatus> toscaElement = toscaOperationFacade.getToscaFullElement(component.getUniqueId());
		if ( toscaElement.isRight() ){
			ResponseFormat response = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(toscaElement.right().value(), component.getComponentType()));
			return Either.right(response);
		}
		component = toscaElement.left().value();
		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> generateToscaRes = null;
		if (component.getToscaArtifacts() != null && !component.getToscaArtifacts().isEmpty()) {
			ArtifactDefinition toscaArtifact = component.getToscaArtifacts().values().stream()
					.filter(p -> p.getArtifactType().equals(ArtifactTypeEnum.TOSCA_TEMPLATE.getType()))
					.findAny().get();
			generateToscaRes = saveToscaArtifactPayload(toscaArtifact, component, user, isInCertificationRequest, shouldLock, inTransaction, fetchTemplatesFromDB);
			if (generateToscaRes.isRight()) {
				return generateToscaRes;
			}
			toscaArtifact = generateToscaRes.left().value().left().value();
			component.getToscaArtifacts().put(toscaArtifact.getArtifactLabel(), toscaArtifact);
			if(!isAbstractResource(component)){
				toscaArtifact = component.getToscaArtifacts().values().stream()
						.filter(p -> p.getArtifactType().equals(ArtifactTypeEnum.TOSCA_CSAR.getType()))
						.findAny().get();
				generateToscaRes = saveToscaArtifactPayload(toscaArtifact, component, user, isInCertificationRequest, shouldLock, inTransaction, true);
				if (generateToscaRes.isRight()) {
					return generateToscaRes;
				}
				toscaArtifact = generateToscaRes.left().value().left().value();
				component.getToscaArtifacts().put(toscaArtifact.getArtifactLabel(), toscaArtifact);
			}
		}
		return generateToscaRes;
	}

	private boolean isAbstractResource(Component component) {
		return component.getComponentType() == ComponentTypeEnum.RESOURCE && ((Resource)component).isAbstract();
	}

	public Either<Either<ArtifactDefinition, Operation>, ResponseFormat> saveToscaArtifactPayload(ArtifactDefinition artifactDefinition, org.openecomp.sdc.be.model.Component component, User user, boolean isInCertificationRequest, boolean shouldLock,
			boolean inTransaction, boolean fetchTemplatesFromDB) {
		return artifactsBusinessLogic.generateAndSaveToscaArtifact(artifactDefinition, component, user, isInCertificationRequest, shouldLock, inTransaction, fetchTemplatesFromDB);
	}

	public Either<ImmutablePair<String, byte[]>, ResponseFormat> getToscaModelByComponentUuid(ComponentTypeEnum componentType, String uuid, EnumMap<AuditingFieldsKeysEnum, Object> additionalParam) {

		Map<GraphPropertyEnum, Object> additionalPropertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
		additionalPropertiesToMatch.put(GraphPropertyEnum.COMPONENT_TYPE, componentType.name());

		Either<List<Component>, StorageOperationStatus> latestVersionEither = toscaOperationFacade.getComponentListByUuid(uuid, additionalPropertiesToMatch);
		
		if (latestVersionEither.isRight()) {
			ResponseFormat response = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(latestVersionEither.right().value(), componentType));
			return Either.right(response);
		}
		
		List<Component> components = latestVersionEither.left().value();
		
		Component component = components.stream().filter(c -> c.isHighestVersion()).findFirst().orElse(null);
		if(component == null){
			component = components.stream().filter(c -> c.getLifecycleState() == LifecycleStateEnum.CERTIFIED).findFirst().orElse(null);
		}
		
		if(component == null){
			ResponseFormat response = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(StorageOperationStatus.NOT_FOUND, componentType));
			return Either.right(response);
		}
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, component.getName());
		// TODO remove after migration - handle artifact not found(no
		// placeholder)
		if (null == component.getToscaArtifacts() || component.getToscaArtifacts().isEmpty()) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, ArtifactTypeEnum.TOSCA_CSAR.name()));
		}
		ArtifactDefinition csarArtifact = component.getToscaArtifacts().values().stream()
				.filter(p -> p.getArtifactType().equals(ArtifactTypeEnum.TOSCA_CSAR.getType()))
				.findAny().get();
		return artifactsBusinessLogic.handleDownloadToscaModelRequest(component, csarArtifact);
	}

	protected StorageOperationStatus markComponentToDelete(Component component) {

		ComponentTypeEnum componentType = component.getComponentType();
		String uniqueId = component.getUniqueId();
		if ((component.getIsDeleted() != null) && (component.getIsDeleted() == true)) {
			log.info("component {} already marked as deleted. id= {}, type={}", component.getName(), uniqueId, componentType);
			return StorageOperationStatus.NOT_FOUND;
		}

		StorageOperationStatus markResourceToDelete = toscaOperationFacade.markComponentToDelete(component);
		if (StorageOperationStatus.OK != markResourceToDelete) {
			log.debug("failed to mark component {} of type {} for delete. error = {}", uniqueId, componentType, markResourceToDelete);
			return markResourceToDelete;
		} else {
			log.debug("Component {}  of type {} was marked as deleted", uniqueId, componentType);
			return StorageOperationStatus.OK;
		}
	}

	public Either<Boolean, ResponseFormat> validateAndUpdateDescription(User user, Component currentComponent, Component updatedComponent, AuditingActionEnum auditingAction) {
		String descriptionUpdated = updatedComponent.getDescription();
		String descriptionCurrent = currentComponent.getDescription();
		if (descriptionUpdated != null && !descriptionCurrent.equals(descriptionUpdated)) {
			Either<Boolean, ResponseFormat> validateDescriptionResponse = validateDescriptionAndCleanup(user, updatedComponent, auditingAction);
			if (validateDescriptionResponse.isRight()) {
				ResponseFormat errorRespons = validateDescriptionResponse.right().value();
				return Either.right(errorRespons);
			}
			currentComponent.setDescription(updatedComponent.getDescription());
		}
		return Either.left(true);
	}

	public Either<Boolean, ResponseFormat> validateAndUpdateProjectCode(User user, Component currentComponent, Component updatedComponent) {
		String projectCodeUpdated = updatedComponent.getProjectCode();
		String projectCodeCurrent = currentComponent.getProjectCode();
		if (projectCodeUpdated != null && !projectCodeCurrent.equals(projectCodeUpdated)) {
			Either<Boolean, ResponseFormat> validatProjectCodeResponse = validateProjectCode(user, updatedComponent, null);
			if (validatProjectCodeResponse.isRight()) {
				ResponseFormat errorRespons = validatProjectCodeResponse.right().value();
				return Either.right(errorRespons);
			}
			currentComponent.setProjectCode(updatedComponent.getProjectCode());
		}
		return Either.left(true);
	}

	public Either<Boolean, ResponseFormat> validateAndUpdateIcon(User user, Component currentComponent, Component updatedComponent, boolean hasBeenCertified) {
		String iconUpdated = updatedComponent.getIcon();
		String iconCurrent = currentComponent.getIcon();
		if (iconUpdated != null && !iconCurrent.equals(iconUpdated)) {
			if (!hasBeenCertified) {
				Either<Boolean, ResponseFormat> validatIconResponse = validateIcon(user, updatedComponent, null);
				if (validatIconResponse.isRight()) {
					ResponseFormat errorRespons = validatIconResponse.right().value();
					return Either.right(errorRespons);
				}
				currentComponent.setIcon(updatedComponent.getIcon());
			} else {
				log.info("icon {} cannot be updated once the component has been certified once.", iconUpdated);
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_PARAMETER_CANNOT_BE_CHANGED, "Icon", currentComponent.getComponentType().name().toLowerCase());
				return Either.right(errorResponse);
			}
		}
		return Either.left(true);
	}

	protected Either<List<String>, ResponseFormat> deleteMarkedComponents(ComponentTypeEnum componentType) {

//		List<String> deletedComponents = new ArrayList<String>();
		log.trace("start deleteMarkedComponents");
		Either<List<String>, StorageOperationStatus> deleteMarkedElements = toscaOperationFacade.deleteMarkedElements(componentType);
		
		titanDao.commit();
		if ( deleteMarkedElements.isRight()){
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(deleteMarkedElements.right().value(), componentType));
			return Either.right(responseFormat);
		}
//		ComponentOperation componentOperation = getComponentOperation(componentType);
//		Either<List<String>, StorageOperationStatus> resourcesToDelete = componentOperation.getAllComponentsMarkedForDeletion();
//		if (resourcesToDelete.isRight()) {
//			ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(resourcesToDelete.right().value(), componentType));
//			return Either.right(responseFormat);
//		}
//
//		for (String resourceToDelete : resourcesToDelete.left().value()) {
//
//			Either<String, ResponseFormat> deleteMarkedResource = deleteMarkedComponent(resourceToDelete, componentType);
//			if (deleteMarkedResource.isLeft()) {
//				deletedComponents.add(deleteMarkedResource.left().value());
//			}
//		}
//		if(deletedComponents.size() == 0) {
//			log.debug("Component list to delete is empty. do commit");
//			titanGenericDao.commit();
//		}
		log.trace("end deleteMarkedComponents");
		return Either.left(deleteMarkedElements.left().value());
	}

	public Either<List<ArtifactDefinition>, StorageOperationStatus> getComponentArtifactsForDelete(String parentId, NodeTypeEnum parentType, boolean inTransacton) {
		List<ArtifactDefinition> artifacts = new ArrayList<ArtifactDefinition>();
		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifactsResponse = artifactToscaOperation.getArtifacts(parentId);
		if (artifactsResponse.isRight()) {
			if (!artifactsResponse.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
				log.debug("failed to retrieve artifacts for {} {}", parentType, parentId);
				return Either.right(artifactsResponse.right().value());
			}
		} else {
			artifacts.addAll(artifactsResponse.left().value().values());
		}

//		if (NodeTypeEnum.Resource.equals(parentType)) {
//			Either<List<ArtifactDefinition>, StorageOperationStatus> interfacesArtifactsForResource = getAdditionalArtifacts(parentId, false, true);
//			if (artifactsResponse.isRight() && !interfacesArtifactsForResource.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
//				log.debug("failed to retrieve interface artifacts for {} {}", parentType, parentId);
//				return Either.right(interfacesArtifactsForResource.right().value());
//			} else if (artifactsResponse.isLeft()) {
//				artifacts.addAll(interfacesArtifactsForResource.left().value());
//			}
//		}
		return Either.left(artifacts);
	}
	
	/**
	 * 
	 * @param componentId
	 * @param user
	 * @param dataParamsToReturn - ui list of params to return
	 * @return
	 */
	
	public Either<UiComponentDataTransfer, ResponseFormat> getComponentDataFilteredByParams(String componentId, User user, List<String> dataParamsToReturn) {

		if (user != null) {
			Either<User, ResponseFormat> eitherCreator = validateUserExists(user, "Get Component by filtered by ui params", false);
			if (eitherCreator.isRight()) {
				return Either.right(eitherCreator.right().value());
			}
		}

		UiComponentDataTransfer result =  new UiComponentDataTransfer();
		
	    if(dataParamsToReturn == null ||  dataParamsToReturn.isEmpty()) {
	    	Either.left(result);
	    	
	    } else {
	    	Either<UiComponentDataTransfer, ResponseFormat> uiDataTransferEither = getUiComponentDataTransferByComponentId(componentId, dataParamsToReturn);
	    	if(uiDataTransferEither.isRight()){
	    		return Either.right(uiDataTransferEither.right().value());
	    	}
	    	result = uiDataTransferEither.left().value();
	    }

	    return Either.left(result);
	}
	
	protected <T extends Component> void generateInputsFromGenericTypeProperties(T component, Resource genericType) {
	
		List<PropertyDefinition> genericTypeProps = genericType.getProperties();
		if(null != genericTypeProps) {
			String genericUniqueId = genericType.getUniqueId();
			List<InputDefinition> inputs = convertGenericTypePropertiesToInputsDefintion(genericTypeProps, genericUniqueId);
			if(null != component.getInputs())
				inputs.addAll(component.getInputs());
			component.setInputs(inputs);
		}
	}
	
	private List<InputDefinition> convertGenericTypePropertiesToInputsDefintion(List<PropertyDefinition> genericTypeProps, String genericUniqueId) {
		return genericTypeProps.stream()
			.map(p -> setInputDefinitionFromProp(p, genericUniqueId))
			.collect(Collectors.toList());
	}
	
	private InputDefinition setInputDefinitionFromProp(PropertyDefinition prop, String genericUniqueId){
		InputDefinition input = new InputDefinition(prop);
		input.setOwnerId(genericUniqueId);
		return input;
	}
	
	protected <T extends Component> Either<Resource, ResponseFormat> fetchAndSetDerivedFromGenericType(T component){
		String genericTypeToscaName = null;
		if(component.getComponentType() == ComponentTypeEnum.RESOURCE && ((Resource)component).getResourceType() == ResourceTypeEnum.CVFC && CollectionUtils.isNotEmpty(((Resource)component).getDerivedFrom())){
			genericTypeToscaName = ((Resource)component).getDerivedFrom().get(0);
		} else {
			genericTypeToscaName = component.fetchGenericTypeToscaNameFromConfig();
		}
		log.debug("Fetching generic tosca name {}", genericTypeToscaName);
		if(null == genericTypeToscaName) {
			log.debug("Failed to fetch certified node type by tosca resource name {}", genericTypeToscaName);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
		
		Either<Resource, StorageOperationStatus> findLatestGeneric = toscaOperationFacade.getLatestCertifiedNodeTypeByToscaResourceName(genericTypeToscaName);
		if(findLatestGeneric.isRight()){
			log.debug("Failed to fetch certified node type by tosca resource name {}", genericTypeToscaName);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERIC_TYPE_NOT_FOUND, component.assetType(), genericTypeToscaName));
		}
		
		Resource genericTypeResource = findLatestGeneric.left().value();
		component.setDerivedFromGenericInfo(genericTypeResource);
		return Either.left(genericTypeResource);
	}

	public Either<Map<String, List<IComponentInstanceConnectedElement>>, ResponseFormat> getFilteredComponentInstanceProperties(String componentId, Map<FilterKeyEnum, List<String>> filters, String userId) {
		Either<Map<String, List<IComponentInstanceConnectedElement>>, ResponseFormat> response = null;
		Either<Component, StorageOperationStatus> getResourceRes = null;
		try{
			if(!filters.containsKey(FilterKeyEnum.NAME_FRAGMENT) && StringUtils.isEmpty(filters.get(FilterKeyEnum.NAME_FRAGMENT).get(0))){
				response = Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
			}
			if (userId != null && response == null) {
				Either<User, ResponseFormat> validateUserRes = validateUserExists(userId, "Get filtered component instance properties", false);
				if (validateUserRes.isRight()) {
					response = Either.right(validateUserRes.right().value());
				}
			}
			if(response == null){
				getResourceRes = toscaOperationFacade.getToscaElement(componentId);
				if(getResourceRes.isRight()){
					response = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(getResourceRes.right().value())));
				}
			}
			if(response == null){
				response = getFilteredComponentInstancesProperties(getResourceRes.left().value(), filters);
			}
		} catch(Exception e){
			log.debug("The exception {} occured during filtered instance properties fetching. the  containing component is {}. ", e, componentId);
			response = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
		} finally{
			if (response.isLeft()){
				toscaOperationFacade.commit();
			} else {
				toscaOperationFacade.rollback();
			}
		}
		return response;
	}

	private Either<Map<String, List<IComponentInstanceConnectedElement>>, ResponseFormat> getFilteredComponentInstancesProperties(Component component, Map<FilterKeyEnum, List<String>> filters) {
		
		Map<String,  List<IComponentInstanceConnectedElement>> filteredProperties = new HashMap<>();
		Either<Map<String, List<IComponentInstanceConnectedElement>>, ResponseFormat> result = Either.left(filteredProperties);
		List<ComponentInstance> filteredInstances = getFilteredInstances(component, filters.get(FilterKeyEnum.RESOURCE_TYPE));
		String propertyNameFragment= filters.get(FilterKeyEnum.NAME_FRAGMENT).get(0);
		boolean searchByFragment = propertyNameFragment.length() > 3 ;
		if(CollectionUtils.isNotEmpty(filteredInstances)){
			for(ComponentInstance instance : filteredInstances){
				if(component.getComponentInstancesProperties()!=null &&component.getComponentInstancesProperties().containsKey(instance.getUniqueId())){
					List<IComponentInstanceConnectedElement> currProperties =  getFilteredComponentInstanceProperties(component.getComponentInstancesProperties().get(instance.getUniqueId()), propertyNameFragment, searchByFragment);
					if(CollectionUtils.isNotEmpty(currProperties)){
						filteredProperties.put(instance.getUniqueId(), currProperties);
					}
				}
				if(component.getComponentInstancesInputs()!=null && component.getComponentInstancesInputs().containsKey(instance.getUniqueId())){
					List<IComponentInstanceConnectedElement> currInputs =  getFilteredComponentInstanceInputs(component.getComponentInstancesInputs().get(instance.getUniqueId()), propertyNameFragment, searchByFragment);
					if(CollectionUtils.isNotEmpty(currInputs)){
						if(filteredProperties.get(instance.getUniqueId())!=null){
							filteredProperties.get(instance.getUniqueId()).addAll(currInputs);
						} else {
							filteredProperties.put(instance.getUniqueId(), currInputs);
						}
					}
				}
			}
		}
		return result;
	}
	
	private List<IComponentInstanceConnectedElement> getFilteredComponentInstanceInputs(List<ComponentInstanceInput> inputs, String propertyNameFragment, boolean searchByFragment) {
		return inputs.stream().filter(i -> isMatchingInput(i, propertyNameFragment, searchByFragment)).collect(Collectors.toList());
	}

	private List<IComponentInstanceConnectedElement> getFilteredComponentInstanceProperties(List<ComponentInstanceProperty> instanceProperties, String propertyNameFragment, boolean searchByFragment) {
		return instanceProperties.stream().filter(p -> isMatchingProperty(p, propertyNameFragment, searchByFragment)).collect(Collectors.toList());
	}

	private boolean isMatchingInput(ComponentInstanceInput input, String propertyNameFragment, boolean searchByFragment) {
		boolean isMatching = false;
		if(searchByFragment && input.getName().toLowerCase().contains(propertyNameFragment)){
			isMatching = true;
		}
		if(!searchByFragment && input.getName().equalsIgnoreCase(propertyNameFragment)){
			isMatching = true;
		}
		return isMatching;
	}
	
	private boolean isMatchingProperty(ComponentInstanceProperty property, String propertyNameFragment, boolean searchByFragment) {
		boolean isMatching = false;
		if(searchByFragment && property.getName().toLowerCase().contains(propertyNameFragment)){
			isMatching = true;
		}
		if(!searchByFragment && property.getName().equalsIgnoreCase(propertyNameFragment)){
			isMatching = true;
		}
		if (!isMatching && !ToscaPropertyType.isPrimitiveType(property.getType())){
			isMatching = isMatchingComplexPropertyByRecursively(property, propertyNameFragment, searchByFragment);
		}
		return isMatching;
	}

	private boolean isMatchingComplexPropertyByRecursively(PropertyDataDefinition property, String propertyNameFragment,  boolean searchByFragment) {
		String propertyType;
		List<PropertyDefinition>  dataTypeProperties;
		DataTypeDefinition currentProperty;
		if(searchByFragment && property.getName().toLowerCase().contains(propertyNameFragment.toLowerCase())){
			return true;
		}
		if(!searchByFragment && property.getName().equalsIgnoreCase(propertyNameFragment)){
			return true;
		}
		
		propertyType = isEmptyInnerType(property) ? property.getType() : property.getSchema().getProperty().getType();

		if(ToscaPropertyType.isScalarType(propertyType)){
			return false;
		}
		Either<DataTypeDefinition, StorageOperationStatus>  getDataTypeByNameRes = propertyOperation.getDataTypeByName(propertyType);
		if(getDataTypeByNameRes.isRight()){
			return false;
		}
		currentProperty = getDataTypeByNameRes.left().value();
		dataTypeProperties = currentProperty.getProperties();
		
		if(CollectionUtils.isNotEmpty(dataTypeProperties)){
			for(PropertyDefinition prop : dataTypeProperties){
				if(isMatchingComplexPropertyByRecursively(prop, propertyNameFragment, searchByFragment)){
					return true;
				}
			}
		}
		dataTypeProperties = currentProperty.getDerivedFrom().getProperties();
		if(CollectionUtils.isNotEmpty(dataTypeProperties)){
			for(PropertyDefinition prop : dataTypeProperties){
				if(isMatchingComplexPropertyByRecursively(prop, propertyNameFragment, searchByFragment)){
					return true;
				}
			}
		}
		return false;
	}

	private boolean isEmptyInnerType(PropertyDataDefinition property) {
		return property == null|| property.getSchema() == null || property.getSchema().getProperty() == null || property.getSchema().getProperty().getType() == null;
	}

	public Either<Boolean, ResponseFormat> shouldUpgradeToLatestGeneric(Component clonedComponent) {
	    
		if(!clonedComponent.deriveFromGeneric())
			return Either.left(false);
		Boolean shouldUpgrade = false;
		String currentGenericType = clonedComponent.getDerivedFromGenericType();
		String currentGenericVersion = clonedComponent.getDerivedFromGenericVersion();
		Either<Resource, ResponseFormat> fetchAndSetLatestGeneric = fetchAndSetDerivedFromGenericType(clonedComponent);
		if(fetchAndSetLatestGeneric.isRight())
			return Either.right(fetchAndSetLatestGeneric.right().value());
		Resource genericTypeResource = fetchAndSetLatestGeneric.left().value();
		if(null == currentGenericType || !currentGenericType.equals(genericTypeResource.getToscaResourceName()) || !currentGenericVersion.equals(genericTypeResource.getVersion())){
			shouldUpgrade = upgradeToLatestGeneric(clonedComponent, genericTypeResource);
			if(!shouldUpgrade) {
				reverntUpdateOfGenericVersion(clonedComponent, currentGenericType, currentGenericVersion);
			}
		}
		return Either.left(shouldUpgrade);
	}

	private void reverntUpdateOfGenericVersion(Component clonedComponent, String currentGenericType, String currentGenericVersion) {
		clonedComponent.setDerivedFromGenericType(currentGenericType);
		clonedComponent.setDerivedFromGenericVersion(currentGenericVersion);
	} 
	
	private <T extends PropertyDataDefinition> Either<Map<String, T>, String> validateNoConflictingProperties(List<T> currentList, List<T> upgradedList) {
		Map<String, T> currentMap = ToscaDataDefinition.listToMapByName(currentList);
		Map<String, T> upgradedMap = ToscaDataDefinition.listToMapByName(upgradedList);
		return ToscaDataDefinition.mergeDataMaps(upgradedMap, currentMap, true);
	}
	
	private boolean shouldUpgradeNodeType(Component componentToCheckOut, Resource latestGeneric){
		
		List<PropertyDefinition> genericTypeProps = latestGeneric.getProperties();
		Either<Map<String, PropertyDefinition>, String> validMerge = validateNoConflictingProperties(genericTypeProps, ((Resource)componentToCheckOut).getProperties());
		if (validMerge.isRight()) { 
			log.debug("property {} cannot be overriden, check out performed without upgrading to latest generic", validMerge.right().value());
			return false;
		}
		List<PropertyDefinition> genericTypeAttributes = latestGeneric.getAttributes();
		validMerge = validateNoConflictingProperties(genericTypeAttributes, ((Resource)componentToCheckOut).getAttributes());
		if (validMerge.isRight()) { 
			log.debug("attribute {} cannot be overriden, check out performed without upgrading to latest generic", validMerge.right().value());
			return false;
		}
		return true;
	}
	
    private boolean upgradeToLatestGeneric(Component componentToCheckOut, Resource latestGeneric) {
    	
		if (!componentToCheckOut.shouldGenerateInputs()) {
			//node type - validate properties and attributes
			return shouldUpgradeNodeType(componentToCheckOut, latestGeneric);
		}
		List<PropertyDefinition> genericTypeProps = latestGeneric.getProperties();	
		List<InputDefinition> genericTypeInputs = null == genericTypeProps? null : convertGenericTypePropertiesToInputsDefintion(genericTypeProps, latestGeneric.getUniqueId());
		List<InputDefinition> currentList = new ArrayList<>();	
	    // nullify existing ownerId from existing list and merge into updated list
		if (null != componentToCheckOut.getInputs()) {
			for(InputDefinition input : componentToCheckOut.getInputs()) {
				InputDefinition copy = new InputDefinition(input);
				copy.setOwnerId(null);
				currentList.add(copy);
			}
		}
		if (null == genericTypeInputs) {
			componentToCheckOut.setInputs(currentList);
			return true;
		}
		
		Either<Map<String, InputDefinition>, String> eitherMerged = validateNoConflictingProperties(genericTypeInputs, currentList);
		if (eitherMerged.isRight()) { 
			log.debug("input {} cannot be overriden, check out performed without upgrading to latest generic", eitherMerged.right().value());
			return false;
		}
		componentToCheckOut.setInputs(new ArrayList<InputDefinition>(eitherMerged.left().value().values()));
		return true;
	}
	

	private List<ComponentInstance> getFilteredInstances(Component component, List<String> resourceTypes) {
		List<ComponentInstance> filteredInstances = null;
		if(CollectionUtils.isEmpty(resourceTypes)){
			filteredInstances = component.getComponentInstances();
		}
		else if(CollectionUtils.isNotEmpty(component.getComponentInstances())){
			filteredInstances = component.getComponentInstances()
					.stream().filter(i -> isMatchingType(i.getOriginType(), resourceTypes)).collect(Collectors.toList());
		}
		if(filteredInstances == null){
			filteredInstances = new ArrayList<>();
		}
		return filteredInstances;
	}

	private boolean isMatchingType(OriginTypeEnum originType, List<String> resourceTypes) {
		boolean isMatchingType = false;
		for(String resourceType : resourceTypes){
			if(originType == OriginTypeEnum.findByValue(resourceType.toUpperCase())){
				isMatchingType = true;
				break;
			}
		}
		return isMatchingType;
	}
	
	protected String cleanUpText(String text){
		text = ValidationUtils.removeNoneUtf8Chars(text);
		text = ValidationUtils.normaliseWhitespace(text);
		text = ValidationUtils.stripOctets(text);
		text = ValidationUtils.removeHtmlTagsOnly(text);
		return text;
	}

}


