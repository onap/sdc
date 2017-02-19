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
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datamodel.api.HighestFilterEnum;
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapReqDef;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ComponentCache;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ComponentOperation;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.config.EcompErrorName;
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

		description = ValidationUtils.removeNoneUtf8Chars(description);
		description = ValidationUtils.normaliseWhitespace(description);
		description = ValidationUtils.stripOctets(description);
		description = ValidationUtils.removeHtmlTagsOnly(description);

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
		ComponentOperation componentOperation = getComponentOperation(type);
		Either<Boolean, StorageOperationStatus> dataModelResponse;
		dataModelResponse = componentOperation.validateComponentNameExists(component.getName());

		if (dataModelResponse.isLeft()) {
			if (dataModelResponse.left().value()) {
				return Either.left(true);
			} else {
				log.info("Component with name {} already exists", component.getName());
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, type.getValue(), component.getName());
				componentsUtils.auditComponentAdmin(errorResponse, user, component, "", "", actionEnum, type);
				return Either.right(errorResponse);
			}
		}
		BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, "validateComponentNameUnique");
		BeEcompErrorManager.getInstance().logBeSystemError("validateComponentNameUnique");
		log.debug("Error while validateComponentNameUnique for component: {}", component.getName());
		ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
		componentsUtils.auditComponentAdmin(errorResponse, user, component, "", "", actionEnum, type);
		return Either.right(errorResponse);
	}

	protected Either<Boolean, ResponseFormat> validateContactId(User user, Component component, AuditingActionEnum actionEnum) {
		log.debug("validate component contact info");
		ComponentTypeEnum type = component.getComponentType();
		String contactId = component.getContactId();

		if (!ValidationUtils.validateStringNotEmpty(contactId)) {
			log.info("contact info is missing.");
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
				log.info("contact info is invalid.");
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INVALID_CONTACT, type.getValue());
				return Either.right(errorResponse);
			}
			return Either.left(true);
		}
		return Either.left(false);
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
		log.debug("validate PROJECT_CODE name ");
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
		ComponentOperation componentOperation = getComponentOperation(componentTypeEnum);
		Either<R, StorageOperationStatus> eitherComponent = componentOperation.getComponent(componentId, false);
		return eitherComponent;
	}

	public Either<CapReqDef, ResponseFormat> getRequirementsAndCapabilities(String componentId, ComponentTypeEnum componentTypeEnum, String userId) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "create Component Instance", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
		Map<String, List<RequirementDefinition>> requirements = new HashMap<>();
		Either<CapReqDef, ResponseFormat> eitherRet;
		ComponentOperation componentOperation = getComponentOperation(componentTypeEnum);
		Either<Component, ResponseFormat> eitherComponent = validateComponentExists(componentId, componentTypeEnum, false, true);
		if (eitherComponent.isLeft()) {
			Either<Map<String, List<CapabilityDefinition>>, TitanOperationStatus> eitherCapabilities = componentOperation.getCapabilities(eitherComponent.left().value(), componentTypeEnum.getNodeType(), false);
			if (eitherCapabilities.isRight()) {
				ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
				eitherRet = Either.right(errorResponse);
			} else {
				Either<Map<String, List<RequirementDefinition>>, TitanOperationStatus> eitherRequirements = componentOperation.getRequirements(eitherComponent.left().value(), componentTypeEnum.getNodeType(), false);
				if (eitherRequirements.isRight()) {
					ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
					eitherRet = Either.right(errorResponse);
				} else {
					requirements = eitherRequirements.left().value();
					capabilities = eitherCapabilities.left().value();
					eitherRet = Either.left(new CapReqDef(requirements, capabilities));
				}
			}
		} else {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeResourceMissingError, "getRequirementsAndCapabilities", componentId);
			BeEcompErrorManager.getInstance().logBeComponentMissingError("getRequirementsAndCapabilities", componentTypeEnum.getValue(), componentId);
			eitherRet = Either.right(eitherComponent.right().value());
		}

		return eitherRet;
	}

	public Either<List<Component>, ResponseFormat> getLatestVersionNotAbstractComponents(boolean isAbstractAbstract, HighestFilterEnum highestFilter, ComponentTypeEnum componentTypeEnum, String internalComponentType, List<String> componentUids,
			String userId) {

		long startUser = System.currentTimeMillis();
		Either<User, ResponseFormat> resp = validateUserExists(userId, "get Latest Version Not Abstract Components", false);
		long endUser = System.currentTimeMillis();
		log.debug("Activation time of get user {} ms", (endUser - startUser));
		ResponseFormat responseFormat;
		if (resp.isLeft()) {

			List<Component> result = new ArrayList<Component>();
			Set<String> nonProcessesComponents = new HashSet<>();
			nonProcessesComponents.addAll(componentUids);

			long startGetComp = System.currentTimeMillis();
			// Read components from cache
			Set<String> filteredComponents = new HashSet<>();
			filteredComponents.addAll(componentUids);

			Either<ImmutableTriple<List<Component>, List<Component>, Set<String>>, ActionStatus> allPartialComponents = componentCache.getComponentsForLeftPanel(componentTypeEnum, internalComponentType, filteredComponents);

			if (allPartialComponents.isRight()) {
				log.debug("Components was not fetched from cache. Status is {}", allPartialComponents.right().value());
			} else {
				ImmutableTriple<List<Component>, List<Component>, Set<String>> immutableTriple = allPartialComponents.left().value();
				List<Component> processedComponents = immutableTriple.left;
				if (processedComponents != null) {
					result.addAll(processedComponents);
				}
				List<Component> dirtyComponents = immutableTriple.middle;
				if (dirtyComponents != null) {
					result.addAll(dirtyComponents);
				}

				Set<String> nonProcessesComponentsFromCache = immutableTriple.right;
				nonProcessesComponents = nonProcessesComponentsFromCache;
			}
			long endGetComp = System.currentTimeMillis();
			log.debug("Activation time of get Comp from cache {} ms", (endGetComp - startGetComp));

			// Fecth non cached components
			List<String> componentsUidToFetch = new ArrayList<String>();
			componentsUidToFetch.addAll(nonProcessesComponents);

			long startGetCompFromGraph = System.currentTimeMillis();
			if (componentsUidToFetch.size() > 0) {
				log.debug("Number of Components to fetch from graph is {}", componentsUidToFetch.size());
				ComponentOperation componentOperation = getComponentOperation(componentTypeEnum);
				Boolean isHighest = isHighest(highestFilter);
				Either<List<Component>, StorageOperationStatus> nonCheckoutCompResponse = componentOperation.getLatestVersionNotAbstractComponents(isAbstractAbstract, isHighest, componentTypeEnum, internalComponentType, componentsUidToFetch);

				if (nonCheckoutCompResponse.isLeft()) {
					log.debug("Retrived Resource successfully.");
					result.addAll(nonCheckoutCompResponse.left().value());
				} else {
					responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(nonCheckoutCompResponse.right().value()));
				}
			}
			long endGetCompFromGraph = System.currentTimeMillis();
			log.debug("Activation time of get Comp from graph {} ms", (endGetCompFromGraph - startGetCompFromGraph));

			return Either.left(result);
		} else {
			responseFormat = resp.right().value();
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

	public Either<List<Map<String, String>>, ResponseFormat> getLatestVersionNotAbstractComponentsUidOnly(boolean isAbstractAbstract, HighestFilterEnum highestFilter, ComponentTypeEnum componentTypeEnum, String internalComponentType, String userId) {
		Either<User, ResponseFormat> resp = validateUserExists(userId, "get Latest Version Not Abstract Components", false);
		ResponseFormat responseFormat;
		if (resp.isLeft()) {

			ComponentOperation componentOperation = getComponentOperation(componentTypeEnum);
			Boolean isHighest = isHighest(highestFilter);
			Either<Collection<ComponentMetadataData>, StorageOperationStatus> nonCheckoutCompResponse = componentOperation.getLatestVersionNotAbstractComponentsMetadataOnly(isAbstractAbstract, isHighest, componentTypeEnum, internalComponentType);

			if (nonCheckoutCompResponse.isLeft()) {
				log.debug("Retrived Resource successfully.");
				List<Map<String, String>> res = new ArrayList<>();

				// Map<String,String>resMap =
				// nonCheckoutCompResponse.left().value().stream().collect()
				// .collect(Collectors.toMap(
				// p -> p.getMetadataDataDefinition().getUniqueId(),
				// p-> p.getMetadataDataDefinition().getVersion()));

				res = nonCheckoutCompResponse.left().value().stream().map(p -> {
					HashMap<String, String> map = new HashMap<>();
					map.put("uid", p.getMetadataDataDefinition().getUniqueId());
					map.put("version", p.getMetadataDataDefinition().getVersion());
					Long lastUpdateDate = p.getMetadataDataDefinition().getLastUpdateDate();
					String lastUpdateDateStr = lastUpdateDate != null ? String.valueOf(lastUpdateDate.longValue()) : "0";
					map.put("timestamp", lastUpdateDateStr);
					return map;
				}).collect(Collectors.toList());

				return Either.left(res);
			}
			responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(nonCheckoutCompResponse.right().value()));
		} else {
			responseFormat = resp.right().value();
		}

		return Either.right(responseFormat);
	}

	public void setDeploymentArtifactsPlaceHolder(Component component, User user) {

	}

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
		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> generateToscaRes = null;
		if (component.getToscaArtifacts() != null && !component.getToscaArtifacts().isEmpty()) {
			ArtifactDefinition toscaArtifact = component.getToscaArtifacts().values().stream().filter(p -> p.getArtifactType().equals(ArtifactTypeEnum.TOSCA_TEMPLATE.getType())).findAny().get();
			generateToscaRes = saveToscaArtifactPayload(toscaArtifact, component, user, isInCertificationRequest, shouldLock, inTransaction, true);
			if (generateToscaRes.isRight()) {
				return generateToscaRes;
			}
			toscaArtifact = component.getToscaArtifacts().values().stream().filter(p -> p.getArtifactType().equals(ArtifactTypeEnum.TOSCA_CSAR.getType())).findAny().get();
			generateToscaRes = saveToscaArtifactPayload(toscaArtifact, component, user, isInCertificationRequest, shouldLock, inTransaction, true);
		}
		// TODO if csar artifact fails delete template artifact
		return generateToscaRes;
	}

	public Either<Either<ArtifactDefinition, Operation>, ResponseFormat> saveToscaArtifactPayload(ArtifactDefinition artifactDefinition, org.openecomp.sdc.be.model.Component component, User user, boolean isInCertificationRequest, boolean shouldLock,
			boolean inTransaction, boolean fetchTemplatesFromDB) {
		return artifactsBusinessLogic.generateAndSaveToscaArtifact(artifactDefinition, component, user, isInCertificationRequest, shouldLock, inTransaction, fetchTemplatesFromDB);
	}

	public Either<ImmutablePair<String, byte[]>, ResponseFormat> getToscaModelByComponentUuid(ComponentTypeEnum componentType, String uuid, EnumMap<AuditingFieldsKeysEnum, Object> additionalParam) {
		// get info
		ComponentOperation componentOperation = getComponentOperation(componentType);
		Either<Component, StorageOperationStatus> latestVersion = componentOperation.getLatestComponentByUuid(componentType.getNodeType(), uuid);
		if (latestVersion.isRight()) {
			ResponseFormat response = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(latestVersion.right().value(), componentType));
			return Either.right(response);

		}
		Component component = latestVersion.left().value();
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, component.getName());
		// TODO remove after migration - handle artifact not found(no
		// placeholder)
		if (null == component.getToscaArtifacts() || component.getToscaArtifacts().isEmpty()) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, ArtifactTypeEnum.TOSCA_CSAR.name()));
		}
		ArtifactDefinition csarArtifact = component.getToscaArtifacts().values().stream().filter(p -> p.getArtifactType().equals(ArtifactTypeEnum.TOSCA_CSAR.getType())).findAny().get();
		return artifactsBusinessLogic.handleDownloadToscaModelRequest(component, csarArtifact, true, false);
	}

	protected StorageOperationStatus markComponentToDelete(Component component) {

		ComponentTypeEnum componentType = component.getComponentType();
		String uniqueId = component.getUniqueId();
		if ((component.getIsDeleted() != null) && (component.getIsDeleted() == true)) {
			log.info("component {} already marked as deleted. id= {}, type={}", component.getName(), uniqueId, componentType);
			return StorageOperationStatus.NOT_FOUND;
		}

		ComponentOperation componentOperation = getComponentOperation(componentType);

		Either<Component, StorageOperationStatus> markResourceToDelete = componentOperation.markComponentToDelete(component, true);
		if (markResourceToDelete.isRight()) {
			StorageOperationStatus result = markResourceToDelete.right().value();
			log.debug("failed to mark component {} of type {} for delete. error = {}", uniqueId, componentType, result);
			return result;
		} else {
			log.debug("Component {}  of type {} was marked as deleted", uniqueId, componentType);
			return StorageOperationStatus.OK;
		}
	}

	public Either<Boolean, ResponseFormat> validateAndUpdateDescription(User user, Component currentComponent, Component updatedComponent, AuditingActionEnum audatingAction) {
		String descriptionUpdated = updatedComponent.getDescription();
		String descriptionCurrent = currentComponent.getDescription();
		if (descriptionUpdated != null && !descriptionCurrent.equals(descriptionUpdated)) {
			Either<Boolean, ResponseFormat> validateDescriptionResponse = validateDescriptionAndCleanup(user, updatedComponent, audatingAction);
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

		List<String> deletedComponents = new ArrayList<String>();
		log.trace("start deleteMarkedComponents");
		ComponentOperation componentOperation = getComponentOperation(componentType);
		Either<List<String>, StorageOperationStatus> resourcesToDelete = componentOperation.getAllComponentsMarkedForDeletion();
		if (resourcesToDelete.isRight()) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(resourcesToDelete.right().value(), componentType));
			return Either.right(responseFormat);
		}

		for (String resourceToDelete : resourcesToDelete.left().value()) {

			Either<String, ResponseFormat> deleteMarkedResource = deleteMarkedComponent(resourceToDelete, componentType);
			if (deleteMarkedResource.isLeft()) {
				deletedComponents.add(deleteMarkedResource.left().value());
			}
		}

		log.trace("end deleteMarkedComponents");
		return Either.left(deletedComponents);
	}

	private Either<String, ResponseFormat> deleteMarkedComponent(String componentToDelete, ComponentTypeEnum componentType) {

		Either<String, ResponseFormat> result = null;
		ComponentOperation componentOperation = getComponentOperation(componentType);
		NodeTypeEnum compNodeType = componentType.getNodeType();
		StorageOperationStatus lockResult = graphLockOperation.lockComponent(componentToDelete, compNodeType);
		if (!lockResult.equals(StorageOperationStatus.OK)) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedLockObjectError, "Delete marked component");
			log.debug("Failed to lock component {}. error - {}", componentToDelete, lockResult);
			result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
			return result;
		}
		try {

			// check if resource has relations
			Either<Boolean, StorageOperationStatus> isResourceInUse = componentOperation.isComponentInUse(componentToDelete);
			if (isResourceInUse.isRight()) {
				log.info("deleteMarkedResource - failed to find relations to resource. id = {}, type = {}, error = {}", componentToDelete, componentType, isResourceInUse.right().value().name());
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
				result = Either.right(responseFormat);
				return result;
			}

			if (isResourceInUse.isLeft() && isResourceInUse.left().value() == false) {

				// delete resource and its artifacts in one transaction
				Either<List<ArtifactDefinition>, StorageOperationStatus> artifactsRes = componentOperation.getComponentArtifactsForDelete(componentToDelete, compNodeType, true);
				if (artifactsRes.isRight() && !artifactsRes.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
					log.info("failed to check artifacts for component node. id = {}, type = {}, error = {}", componentToDelete, componentType, artifactsRes.right().value().name());
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
					result = Either.right(responseFormat);
					return result;
				}
				List<ArtifactDefinition> artifactsToDelete = new ArrayList<>();
				if (artifactsRes.isLeft()) {
					artifactsToDelete = artifactsRes.left().value();
				}

				Either<Component, StorageOperationStatus> deleteComponentRes = componentOperation.deleteComponent(componentToDelete, true);
				if (deleteComponentRes.isRight()) {
					log.info("failed to delete component. id = {}, type = {}, error = {}", componentToDelete, componentType, deleteComponentRes.right().value().name());
					ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(deleteComponentRes.right().value());
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(actionStatus, componentToDelete);
					result = Either.right(responseFormat);
				} else {
					log.trace("component was deleted, id = {}, type = {}", componentToDelete, componentType);
					// delete related artifacts
					StorageOperationStatus deleteFromEsRes = artifactsBusinessLogic.deleteAllComponentArtifactsIfNotOnGraph(artifactsToDelete);
					if (!deleteFromEsRes.equals(StorageOperationStatus.OK)) {
						ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(deleteFromEsRes);
						ResponseFormat responseFormat = componentsUtils.getResponseFormat(actionStatus, componentToDelete);
						result = Either.right(responseFormat);
						return result;
					}
					log.debug("component and all its artifacts were deleted, id = {}, type = {}", componentToDelete, componentType);
					result = Either.left(componentToDelete);
				}
			} else {
				// resource in use
				log.debug("componentis marked for delete but still in use, id = {}, type = {}", componentToDelete, componentType);
				ActionStatus actionStatus = ActionStatus.RESTRICTED_OPERATION;
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(actionStatus, componentToDelete);
				result = Either.right(responseFormat);
				return result;
			}
		} finally {
			if (result == null || result.isRight()) {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, "delete marked component");
				log.debug("operation failed. do rollback");
				titanGenericDao.rollback();
			} else {
				log.debug("operation success. do commit");
				titanGenericDao.commit();
			}
			graphLockOperation.unlockComponent(componentToDelete, compNodeType);
		}

		return result;
	}

}
