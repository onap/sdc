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

import java.util.List;

import javax.servlet.ServletContext;

import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.graph.datatype.AdditionalInformationEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterInfo;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.IAdditionalInformationOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.be.model.tosca.converters.StringConvertor;
import org.openecomp.sdc.be.model.tosca.validators.StringValidator;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import fj.data.Either;

@Component("additionalInformationBusinessLogic")
public class AdditionalInformationBusinessLogic extends BaseBusinessLogic {

	private static final String CREATE_ADDITIONAL_INFORMATION = "CreateAdditionalInformation";

	private static final String UPDATE_ADDITIONAL_INFORMATION = "UpdateAdditionalInformation";

	private static final String DELETE_ADDITIONAL_INFORMATION = "DeleteAdditionalInformation";

	private static final String GET_ADDITIONAL_INFORMATION = "GetAdditionalInformation";

	private static Logger log = LoggerFactory.getLogger(AdditionalInformationBusinessLogic.class.getName());

	@javax.annotation.Resource
	private IAdditionalInformationOperation additionalInformationOperation = null;

	@javax.annotation.Resource
	private IGraphLockOperation graphLockOperation;

	@javax.annotation.Resource
	private ComponentsUtils componentsUtils;

	protected static IElementOperation getElementDao(Class<IElementOperation> class1, ServletContext context) {
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);

		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);

		return webApplicationContext.getBean(class1);
	}

	/**
	 * Create new additional information on resource/service on graph
	 * 
	 * @param resourceId
	 * @param propertyName
	 * @param newPropertyDefinition
	 * @param userId
	 * @return Either<PropertyDefinition, ActionStatus>
	 */
	public Either<AdditionalInfoParameterInfo, ResponseFormat> createAdditionalInformation(NodeTypeEnum nodeType, String resourceId, AdditionalInfoParameterInfo additionalInfoParameterInfo, String additionalInformationUid, String userId) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "create Additional Information", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}
		Either<AdditionalInfoParameterInfo, ResponseFormat> result = null;

		ResponseFormat responseFormat = verifyCanWorkOnComponent(nodeType, resourceId, userId);
		if (responseFormat != null) {
			result = Either.right(responseFormat);
			return result;
		}

		// lock component
		StorageOperationStatus lockResult = graphLockOperation.lockComponent(resourceId, nodeType);
		if (!lockResult.equals(StorageOperationStatus.OK)) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedLockObjectError, CREATE_ADDITIONAL_INFORMATION);
			BeEcompErrorManager.getInstance().logBeFailedLockObjectError(CREATE_ADDITIONAL_INFORMATION, nodeType.getName(), resourceId);
			log.info("Failed to lock component {} error - {}", resourceId, lockResult);
			result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
			return result;
		}
		try {
			responseFormat = validateMaxSizeNotReached(nodeType, resourceId, additionalInfoParameterInfo);
			if (responseFormat != null) {
				result = Either.right(responseFormat);
				return result;
			}

			// validate label
			responseFormat = validateAndConvertKey(additionalInfoParameterInfo, CREATE_ADDITIONAL_INFORMATION);
			if (responseFormat != null) {
				result = Either.right(responseFormat);
				return result;
			}

			// validate value
			responseFormat = validateAndConvertValue(additionalInfoParameterInfo, CREATE_ADDITIONAL_INFORMATION);
			if (responseFormat != null) {
				result = Either.right(responseFormat);
				return result;
			}

			Either<AdditionalInformationDefinition, StorageOperationStatus> addResult = additionalInformationOperation.createAdditionalInformationParameter(nodeType, resourceId, additionalInfoParameterInfo.getKey(),
					additionalInfoParameterInfo.getValue(), true);

			if (addResult.isRight()) {
				StorageOperationStatus status = addResult.right().value();
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, CREATE_ADDITIONAL_INFORMATION);
				BeEcompErrorManager.getInstance().logBeSystemError(CREATE_ADDITIONAL_INFORMATION);
				ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForAdditionalInformation(status);
				result = Either.right(componentsUtils.getResponseFormatAdditionalProperty(actionStatus, additionalInfoParameterInfo, nodeType, AdditionalInformationEnum.Label));
				return result;

			} else {
				AdditionalInformationDefinition informationDefinition = addResult.left().value();

				AdditionalInfoParameterInfo createdAI = findAdditionInformationKey(informationDefinition.getParameters(), additionalInfoParameterInfo.getKey());
				result = Either.left(createdAI);
				return result;
			}

		} finally {
			commitOrRollback(result);
			// unlock component
			graphLockOperation.unlockComponent(resourceId, nodeType);
		}

	}

	/**
	 * Validate the value format. Format the value.
	 * 
	 * @param additionalInfoParameterInfo
	 * @return null in case of success. Otherwise response format.
	 */
	private ResponseFormat validateAndConvertValue(AdditionalInfoParameterInfo additionalInfoParameterInfo, String context) {
		ResponseFormat result = null;

		String value = additionalInfoParameterInfo.getValue();
		log.debug("Going to validate additional information value {}", value);

		Either<String, ResponseFormat> valueValidRes = validateValue(value);
		if (valueValidRes.isRight()) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeInvalidValueError, context, additionalInfoParameterInfo.getValue(), "additional information value", "string");
			BeEcompErrorManager.getInstance().logBeInvalidValueError(context, additionalInfoParameterInfo.getValue(), "additional information value", "string");
			result = valueValidRes.right().value();
		} else {
			String newValue = valueValidRes.left().value();
			if (log.isTraceEnabled()) {
				if (value != null && false == value.equals(newValue)) {
					log.trace("The additional information value was normalized from {} to {}", value, newValue);
				}
			}
			additionalInfoParameterInfo.setValue(newValue);
		}
		return result;
	}

	/**
	 * @param additionalInfoParameterInfo
	 * @return
	 */
	private ResponseFormat validateAndConvertKey(AdditionalInfoParameterInfo additionalInfoParameterInfo, String context) {

		String key = additionalInfoParameterInfo.getKey();
		log.debug("Going to validate additional information key {}", key);

		ResponseFormat result = null;
		ResponseFormat responseFormat;
		Either<String, ResponseFormat> validateKeyRes = validateAndNormalizeKey(key);
		if (validateKeyRes.isRight()) {
			responseFormat = validateKeyRes.right().value();
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeInvalidValueError, context, additionalInfoParameterInfo.getKey(), "additional information label", "string");
			BeEcompErrorManager.getInstance().logBeInvalidValueError(context, additionalInfoParameterInfo.getKey(), "additional information label", "string");
			result = responseFormat;

		} else {
			String convertedKey = validateKeyRes.left().value();

			if (log.isTraceEnabled()) {
				if (key != null && false == key.equals(convertedKey)) {
					log.trace("The additional information key {} was normalized to {}", key, convertedKey);
				}
			}
			additionalInfoParameterInfo.setKey(convertedKey);
		}
		return result;
	}

	/**
	 * verify that the maximal number of additional information properties has not been reached.
	 * 
	 * @param nodeType
	 * @param componentId
	 * @param additionalInfoParameterInfo
	 * @return response format in case the maximal number has been reached.
	 */
	private ResponseFormat validateMaxSizeNotReached(NodeTypeEnum nodeType, String componentId, AdditionalInfoParameterInfo additionalInfoParameterInfo) {

		ResponseFormat result;
		Integer additionalInformationMaxNumberOfKeys = ConfigurationManager.getConfigurationManager().getConfiguration().getAdditionalInformationMaxNumberOfKeys();

		Either<Integer, StorageOperationStatus> checkRes = additionalInformationOperation.getNumberOfAdditionalInformationParameters(nodeType, componentId, true);
		if (checkRes.isRight()) {
			StorageOperationStatus status = checkRes.right().value();

			ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForAdditionalInformation(status);
			result = componentsUtils.getResponseFormatAdditionalProperty(actionStatus, additionalInfoParameterInfo, nodeType, AdditionalInformationEnum.None);
			return result;
		}
		Integer currentNumberOfProperties = checkRes.left().value();
		if (currentNumberOfProperties >= additionalInformationMaxNumberOfKeys) {
			log.info("The current number of additional information properties is {}. The maximum allowed additional information properties is {}", currentNumberOfProperties, currentNumberOfProperties);
			result = componentsUtils.getResponseFormatAdditionalProperty(ActionStatus.ADDITIONAL_INFORMATION_MAX_NUMBER_REACHED, additionalInfoParameterInfo, nodeType, AdditionalInformationEnum.None);
			return result;
		}

		return null;
	}

	/**
	 * validate additional information value
	 * 
	 * @param value
	 * @return
	 */
	private Either<String, ResponseFormat> validateValue(String value) {

		boolean isNonEmptyString = ValidationUtils.validateStringNotEmpty(value);
		if (false == isNonEmptyString) {
			return Either.right(componentsUtils.getResponseFormatAdditionalProperty(ActionStatus.ADDITIONAL_INFORMATION_EMPTY_STRING_NOT_ALLOWED));
		}

		boolean valid = StringValidator.getInstance().isValid(value, null);
		if (false == valid) {
			return Either.right(componentsUtils.getResponseFormatAdditionalProperty(ActionStatus.ADDITIONAL_INFORMATION_VALUE_NOT_ALLOWED_CHARACTERS, new AdditionalInfoParameterInfo(null, value), null, AdditionalInformationEnum.Value));
		}

		String converted = StringConvertor.getInstance().convert(value, null, null);

		return Either.left(converted);
	}

	private AdditionalInfoParameterInfo findAdditionInformationKey(List<AdditionalInfoParameterInfo> parameters, String key) {

		for (AdditionalInfoParameterInfo infoParameterInfo : parameters) {
			if (infoParameterInfo.getKey().equals(key)) {
				return infoParameterInfo;
			}
		}
		return null;
	}

	/**
	 * validate and normalize the key
	 * 
	 * @param additionalInfoParameterInfo
	 * @return
	 */
	private Either<String, ResponseFormat> validateAndNormalizeKey(String key) {

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo();
		additionalInfoParameterInfo.setKey(key);

		key = ValidationUtils.normalizeAdditionalInformation(key);
		boolean isNonEmptyString = ValidationUtils.validateStringNotEmpty(key);
		if (false == isNonEmptyString) {
			return Either.right(componentsUtils.getResponseFormatAdditionalProperty(ActionStatus.ADDITIONAL_INFORMATION_EMPTY_STRING_NOT_ALLOWED, null, null, AdditionalInformationEnum.Label));
		}
		boolean isValidString = ValidationUtils.validateAdditionalInformationKeyName(key);
		if (false == isValidString) {
			if (false == ValidationUtils.validateLength(key, ValidationUtils.ADDITIONAL_INFORMATION_KEY_MAX_LENGTH)) {
				return Either.right(componentsUtils.getResponseFormatAdditionalProperty(ActionStatus.ADDITIONAL_INFORMATION_EXCEEDS_LIMIT, additionalInfoParameterInfo, null, AdditionalInformationEnum.Label));
			}
			return Either.right(componentsUtils.getResponseFormatAdditionalProperty(ActionStatus.ADDITIONAL_INFORMATION_KEY_NOT_ALLOWED_CHARACTERS, additionalInfoParameterInfo, null, AdditionalInformationEnum.Label));
		}

		return Either.left(key);
	}

	/**
	 * update key and value of a given additional information.
	 * 
	 * @param nodeType
	 * @param resourceId
	 * @param additionalInfoParameterInfo
	 * @param additionalInformationUid
	 *            - Future use
	 * @param userId
	 * @return
	 */
	public Either<AdditionalInfoParameterInfo, ResponseFormat> updateAdditionalInformation(NodeTypeEnum nodeType, String resourceId, AdditionalInfoParameterInfo additionalInfoParameterInfo, String additionalInformationUid, String userId) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "create Additional Information", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}
		Either<AdditionalInfoParameterInfo, ResponseFormat> result = null;

		ResponseFormat responseFormat = verifyCanWorkOnComponent(nodeType, resourceId, userId);
		if (responseFormat != null) {
			result = Either.right(responseFormat);
			return result;
		}
		// lock component
		StorageOperationStatus lockResult = graphLockOperation.lockComponent(resourceId, nodeType);
		if (!lockResult.equals(StorageOperationStatus.OK)) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedLockObjectError, UPDATE_ADDITIONAL_INFORMATION);
			BeEcompErrorManager.getInstance().logBeFailedLockObjectError(UPDATE_ADDITIONAL_INFORMATION, nodeType.getName(), resourceId);
			log.info("Failed to lock component {} error - {}", resourceId, lockResult);
			result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
			return result;
		}
		try {

			// validate input
			responseFormat = validateAndConvertKey(additionalInfoParameterInfo, UPDATE_ADDITIONAL_INFORMATION);
			if (responseFormat != null) {
				result = Either.right(responseFormat);
				return result;
			}

			responseFormat = validateAndConvertValue(additionalInfoParameterInfo, UPDATE_ADDITIONAL_INFORMATION);
			if (responseFormat != null) {
				result = Either.right(responseFormat);
				return result;
			}

			Either<AdditionalInformationDefinition, StorageOperationStatus> addResult = additionalInformationOperation.updateAdditionalInformationParameter(nodeType, resourceId, additionalInfoParameterInfo.getUniqueId(),
					additionalInfoParameterInfo.getKey(), additionalInfoParameterInfo.getValue(), true);

			if (addResult.isRight()) {
				StorageOperationStatus status = addResult.right().value();
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, UPDATE_ADDITIONAL_INFORMATION);
				BeEcompErrorManager.getInstance().logBeSystemError(UPDATE_ADDITIONAL_INFORMATION);
				ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForAdditionalInformation(status);
				result = Either.right(componentsUtils.getResponseFormatAdditionalProperty(actionStatus, additionalInfoParameterInfo, nodeType, AdditionalInformationEnum.None));
				return result;
			} else {
				AdditionalInformationDefinition informationDefinition = addResult.left().value();
				AdditionalInfoParameterInfo parameterInfo = findAdditionInformationKey(informationDefinition.getParameters(), additionalInfoParameterInfo.getKey());
				result = Either.left(parameterInfo);
				return result;
			}

		} finally {
			commitOrRollback(result);
			// unlock component
			graphLockOperation.unlockComponent(resourceId, nodeType);
		}

	}

	/**
	 * Delete an additional information label
	 * 
	 * @param nodeType
	 * @param resourceId
	 * @param additionalInfoParameterInfo
	 * @param additionalInformationUid
	 *            - Null. Future use.
	 * @param userId
	 * @return
	 */
	public Either<AdditionalInfoParameterInfo, ResponseFormat> deleteAdditionalInformation(NodeTypeEnum nodeType, String resourceId, AdditionalInfoParameterInfo additionalInfoParameterInfo, String additionalInformationUid, String userId) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "delete Additional Information", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}
		Either<AdditionalInfoParameterInfo, ResponseFormat> result = null;

		ResponseFormat responseFormat = verifyCanWorkOnComponent(nodeType, resourceId, userId);
		if (responseFormat != null) {
			return Either.right(responseFormat);
		}
		// lock component
		StorageOperationStatus lockResult = graphLockOperation.lockComponent(resourceId, nodeType);
		if (!lockResult.equals(StorageOperationStatus.OK)) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedLockObjectError, DELETE_ADDITIONAL_INFORMATION);
			BeEcompErrorManager.getInstance().logBeFailedLockObjectError(DELETE_ADDITIONAL_INFORMATION, nodeType.getName(), resourceId);
			log.info("Failed to lock component {} error - {}", resourceId, lockResult);
			result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
			return result;
		}

		try {

			Either<AdditionalInfoParameterInfo, StorageOperationStatus> findIdRes = additionalInformationOperation.getAdditionalInformationParameter(nodeType, resourceId, additionalInfoParameterInfo.getUniqueId(), true);
			if (findIdRes.isRight()) {
				StorageOperationStatus status = findIdRes.right().value();
				if (status != StorageOperationStatus.NOT_FOUND) {
					BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, GET_ADDITIONAL_INFORMATION);
					BeEcompErrorManager.getInstance().logBeSystemError(GET_ADDITIONAL_INFORMATION);
				}
				ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForAdditionalInformation(status);
				result = Either.right(componentsUtils.getResponseFormatAdditionalProperty(actionStatus, additionalInfoParameterInfo, nodeType, AdditionalInformationEnum.None));
				return result;
			}

			AdditionalInfoParameterInfo foundAdditionalInfo = findIdRes.left().value();

			Either<AdditionalInformationDefinition, StorageOperationStatus> addResult = additionalInformationOperation.deleteAdditionalInformationParameter(nodeType, resourceId, additionalInfoParameterInfo.getUniqueId(), true);

			if (addResult.isRight()) {
				StorageOperationStatus status = addResult.right().value();
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, DELETE_ADDITIONAL_INFORMATION);
				BeEcompErrorManager.getInstance().logBeDaoSystemError(DELETE_ADDITIONAL_INFORMATION);
				ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForAdditionalInformation(status);
				result = Either.right(componentsUtils.getResponseFormatAdditionalProperty(actionStatus, additionalInfoParameterInfo, nodeType, AdditionalInformationEnum.None));
				return result;
			} else {
				result = Either.left(foundAdditionalInfo);
				return result;
			}

		} finally {
			commitOrRollback(result);
			// unlock component
			graphLockOperation.unlockComponent(resourceId, nodeType);
		}

	}

	/**
	 * @param nodeType
	 * @param resourceId
	 * @param additionalInfoParameterInfo
	 * @param additionalInformationUid
	 * @param userId
	 * @return
	 */
	public Either<AdditionalInfoParameterInfo, ResponseFormat> getAdditionalInformation(NodeTypeEnum nodeType, String resourceId, AdditionalInfoParameterInfo additionalInfoParameterInfo, String additionalInformationUid, String userId) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "get Additional Information", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}
		Either<AdditionalInfoParameterInfo, ResponseFormat> result = null;

		try {

			Either<AdditionalInfoParameterInfo, StorageOperationStatus> findIdRes = additionalInformationOperation.getAdditionalInformationParameter(nodeType, resourceId, additionalInfoParameterInfo.getUniqueId(), true);

			if (findIdRes.isRight()) {
				StorageOperationStatus status = findIdRes.right().value();
				ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForAdditionalInformation(status);
				result = Either.right(componentsUtils.getResponseFormatAdditionalProperty(actionStatus, additionalInfoParameterInfo, nodeType, AdditionalInformationEnum.None));
			}

			AdditionalInfoParameterInfo foundAdditionalInfo = findIdRes.left().value();

			result = Either.left(foundAdditionalInfo);

			return result;

		} finally {
			commitOrRollback(result);
		}

	}

	/**
	 * Get all additional information properties of a given resource/service
	 * 
	 * @param nodeType
	 * @param resourceId
	 * @param additionalInformationUid
	 *            - Future use
	 * @param userId
	 * @return
	 */
	public Either<AdditionalInformationDefinition, ResponseFormat> getAllAdditionalInformation(NodeTypeEnum nodeType, String resourceId, String additionalInformationUid, String userId) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "get All Additional Information", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		Either<AdditionalInformationDefinition, ResponseFormat> result = null;

		try {

			Either<AdditionalInformationDefinition, TitanOperationStatus> findIdRes = additionalInformationOperation.getAllAdditionalInformationParameters(nodeType, resourceId, false);
			if (findIdRes.isRight()) {
				StorageOperationStatus status = DaoStatusConverter.convertTitanStatusToStorageStatus(findIdRes.right().value());
				ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForAdditionalInformation(status);
				result = Either.right(componentsUtils.getResponseFormatAdditionalProperty(actionStatus));
			} else {
				AdditionalInformationDefinition informationDefinition = findIdRes.left().value();
				result = Either.left(informationDefinition);
			}

			return result;

		} finally {
			commitOrRollback(result);
		}

	}

	private ResponseFormat verifyCanWorkOnComponent(NodeTypeEnum nodeType, String resourceId, String userId) {

		switch (nodeType) {
		case Resource:

			// verify that resource is checked-out and the user is the last
			// updater
			if (!ComponentValidationUtils.canWorkOnComponent(resourceId, toscaOperationFacade, userId)) {
				return componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
			}
			break;
		case Service:

			// verify that resource is checked-out and the user is the last
			// updater
			if (!ComponentValidationUtils.canWorkOnComponent(resourceId, toscaOperationFacade, userId)) {
				return componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
			}
			break;
		default:
			return componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT, nodeType.getName());
		}

		return null;
	}

}
