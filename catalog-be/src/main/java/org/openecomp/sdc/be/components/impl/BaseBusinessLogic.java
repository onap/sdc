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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyRule;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.IComplexDefaultValue;
import org.openecomp.sdc.be.model.IPropertyInputCommon;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsontitan.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.openecomp.sdc.be.model.tosca.validators.DataTypeValidatorConverter;
import org.openecomp.sdc.be.model.tosca.validators.PropertyTypeValidator;
import org.openecomp.sdc.be.resources.data.PropertyValueData;
import org.openecomp.sdc.be.user.IUserBusinessLogic;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonElement;

import fj.data.Either;

public abstract class BaseBusinessLogic {

	@Autowired
	protected ComponentsUtils componentsUtils;

	@Autowired
	protected IUserBusinessLogic userAdmin;

	@Autowired
	protected IGraphLockOperation graphLockOperation;

	@Autowired
	protected TitanDao titanDao;
	
	@Autowired
	protected TitanGenericDao titanGenericDao;

	@Autowired
	protected IElementOperation elementDao;

	@Autowired
	protected IGroupOperation groupOperation;
	
	@Autowired
	protected IGroupInstanceOperation groupInstanceOperation;

	@Autowired
	protected IGroupTypeOperation groupTypeOperation;

	/*@Autowired
	protected IArtifactOperation artifactOperation;*/
	@javax.annotation.Resource
	protected ArtifactsOperations artifactToscaOperation;

//	@Autowired
//	protected IAttributeOperation attributeOperation;

	@Autowired
	protected PropertyOperation propertyOperation;

	@Autowired
	protected ApplicationDataTypeCache applicationDataTypeCache;

	@Autowired
	protected ToscaOperationFacade toscaOperationFacade; 
	
	protected DataTypeValidatorConverter dataTypeValidatorConverter = DataTypeValidatorConverter.getInstance();

	public void setUserAdmin(UserBusinessLogic userAdmin) {
		this.userAdmin = userAdmin;
	}

	public void setComponentsUtils(ComponentsUtils componentsUtils) {
		this.componentsUtils = componentsUtils;
	}

	public void setGraphLockOperation(IGraphLockOperation graphLockOperation) {
		this.graphLockOperation = graphLockOperation;
	}

	public void setToscaOperationFacade(ToscaOperationFacade toscaOperationFacade) {
		this.toscaOperationFacade = toscaOperationFacade;
	}


	private static Logger log = LoggerFactory.getLogger(BaseBusinessLogic.class.getName());
	
	public static final String EMPTY_VALUE = null;

	protected Either<User, ResponseFormat> validateUserNotEmpty(User user, String ecompErrorContext) {
		String userId = user.getUserId();

		if (StringUtils.isEmpty(userId)) {
			log.debug("User header is missing ");
			BeEcompErrorManager.getInstance().logBeUserMissingError(ecompErrorContext, user.getUserId());
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION);
			return Either.right(responseFormat);
		}
		return Either.left(user);
	}

	protected Either<User, ResponseFormat> validateUserExists(User user, String ecompErrorContext, boolean inTransaction) {
		return validateUserExists(user.getUserId(), ecompErrorContext, inTransaction);
	}

	protected void validateUserExist(String userId, String ecompErrorContext, Wrapper<ResponseFormat> errorWrapper) {
		Either<User, ResponseFormat> resp = validateUserExists(userId, ecompErrorContext, false);
		if (resp.isRight()) {
			errorWrapper.setInnerElement(resp.right().value());
		}
	}

	public Either<User, ActionStatus> validateUserExistsActionStatus(String userId, String ecompErrorContext) {
		Either<User, ActionStatus> eitherCreator = userAdmin.getUser(userId, false);
		if (eitherCreator.isRight() || eitherCreator.left().value() == null) {
			if (eitherCreator.right().value().equals(ActionStatus.USER_NOT_FOUND)) {
				log.debug("validateUserExists - not authorized user, userId {}", userId);
				Either.right(ActionStatus.RESTRICTED_OPERATION);
			} else {
				log.debug("validateUserExists - failed to authorize user, userId {}", userId);
			}
			log.debug("User is not listed. userId {}", userId);
			BeEcompErrorManager.getInstance().logBeUserMissingError(ecompErrorContext, userId);
			return Either.right(eitherCreator.right().value());
		}
		return Either.left(eitherCreator.left().value());
	}

	public Either<User, ResponseFormat> validateUserExists(String userId, String ecompErrorContext, boolean inTransaction) {
		Either<User, ActionStatus> eitherCreator = userAdmin.getUser(userId, inTransaction);
		if (eitherCreator.isRight() || eitherCreator.left().value() == null) {
			ResponseFormat responseFormat;
			if (eitherCreator.right().value().equals(ActionStatus.USER_NOT_FOUND)) {
				if (log.isDebugEnabled())
					log.debug("validateUserExists - not authorized user, userId {}", userId);
				responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
			} else {
				if (log.isDebugEnabled())
					log.debug("validateUserExists - failed to authorize user, userId {}", userId);
				responseFormat = componentsUtils.getResponseFormat(eitherCreator.right().value());
			}
			if (log.isDebugEnabled())
				log.debug("User is not listed. userId {}", userId);
			BeEcompErrorManager.getInstance().logBeUserMissingError(ecompErrorContext, userId);
			return Either.right(responseFormat);
		}
		return Either.left(eitherCreator.left().value());
	}

	protected Either<Boolean, ResponseFormat> validateUserRole(User user, List<Role> roles) {
		Role userRole = Role.valueOf(user.getRole());
		if (roles != null) {
			if (!roles.contains(userRole)) {
				if (log.isDebugEnabled())
					log.debug("user is not in appropriate role to perform action");
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
				return Either.right(responseFormat);
			}
			return Either.left(Boolean.TRUE);
		}
		return Either.left(Boolean.FALSE);
	}

	protected Either<Boolean, ResponseFormat> lockComponent(Component component, String ecompErrorContext) {
		return lockComponent(component.getUniqueId(), component, ecompErrorContext);
	}

	protected Either<Boolean, ResponseFormat> lockComponent(String componentId, Component component, String ecompErrorContext) {
		ComponentTypeEnum componentType = component.getComponentType();
		NodeTypeEnum nodeType = componentType.getNodeType();
		StorageOperationStatus lockResourceStatus = graphLockOperation.lockComponent(componentId, nodeType);

		if (lockResourceStatus.equals(StorageOperationStatus.OK)) {
			return Either.left(true);
		} else {
			BeEcompErrorManager.getInstance().logBeFailedLockObjectError(ecompErrorContext, nodeType.getName(), componentId);
			ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(lockResourceStatus, componentType);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(actionStatus, component.getName());
			log.debug("Failed to lock component {} error - {}" ,componentId, actionStatus);
			return Either.right(responseFormat);
		}
	}

	protected void unlockComponent(Either<?, ?> either, Component component, boolean inTransaction) {
		ComponentTypeEnum componentType = component.getComponentType();
		NodeTypeEnum nodeType = componentType.getNodeType();
		if (false == inTransaction) {
			if (either == null || either.isRight()) {
				titanDao.rollback();
			} else {
				titanDao.commit();
			}
		}
		// unlock resource
		graphLockOperation.unlockComponent(component.getUniqueId(), nodeType);
	}

	protected void unlockComponent(Either<?, ?> either, Component component) {
		unlockComponent(either, component, false);
	}

	protected <T> Either<Boolean, ResponseFormat> validateJsonBody(T bodyObject, Class<T> clazz) {
		if (bodyObject == null) {
			log.debug("Invalid JSON received for object of type {}", clazz.getSimpleName());
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
		} else {
			return Either.left(true);
		}
	}

	protected Either<ComponentTypeEnum, ResponseFormat> validateComponentType(String componentType) {
		ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
		if (componentTypeEnum == null) {
			log.debug("Invalid component type {}", componentType);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, componentType));
		} else {
			return Either.left(componentTypeEnum);
		}
	}

	protected Either<Component, ResponseFormat> validateComponentExists(String componentId, ComponentTypeEnum componentType, ComponentParametersView filter) {
		
		if(filter == null){
			filter = new ComponentParametersView();
		}
		Either<Component, StorageOperationStatus> componentFound  = toscaOperationFacade.getToscaElement(componentId, filter);
		if (componentFound.isRight()) {
			StorageOperationStatus storageOperationStatus = componentFound.right().value();
			ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(storageOperationStatus, componentType);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(actionStatus, Constants.EMPTY_STRING);
			log.debug("Component with id {} was not found", componentId);
			return Either.right(responseFormat);
		}
		return Either.left(componentFound.left().value());
	}

	public Either<Boolean, ResponseFormat> validateCanWorkOnComponent(Component component, String userId) {
		Either<Boolean, ResponseFormat> canWork = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
		if (component.getLifecycleState() != LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT) {
			log.debug("Component {} is not checked-out", component.getName());
			return canWork;
		}

		// verify userId is not null
		if (userId == null) {
			log.debug("Current user userId is null");
			return canWork;
		}

		// verify component last update user is the current user
		String lastUpdaterUserId = component.getLastUpdaterUserId();
		if (!userId.equals(lastUpdaterUserId)) {
			log.debug("Current user is not last updater, last updater userId: {}, current user userId: {}", lastUpdaterUserId, userId);
			return canWork;
		}

		// verify resource is not deleted
		if ((component.getIsDeleted() != null) && (component.getIsDeleted() == true)) {
			log.debug("Component {} is marked as deleted", component.getUniqueId());
			return canWork;
		}

		return Either.left(true);
	}

	public ComponentTypeEnum getComponentTypeByParentComponentType(ComponentTypeEnum parentComponentType) {
		switch (parentComponentType) {
		case SERVICE:
			return ComponentTypeEnum.RESOURCE;
		case RESOURCE:
			return ComponentTypeEnum.RESOURCE;
		case PRODUCT:
			return ComponentTypeEnum.SERVICE;
		default:
			break;
		}
		return null;
	}

	// For UT
	public void setTitanGenericDao(TitanDao titanDao) {
		this.titanDao = titanDao;
	}

	protected Either<Map<String, DataTypeDefinition>, ResponseFormat> getAllDataTypes(ApplicationDataTypeCache applicationDataTypeCache) {
		Either<Map<String, DataTypeDefinition>, TitanOperationStatus> allDataTypes = applicationDataTypeCache.getAll();
		if (allDataTypes.isRight()) {
			TitanOperationStatus operationStatus = allDataTypes.right().value();
			if (operationStatus == TitanOperationStatus.NOT_FOUND) {
				BeEcompErrorManager.getInstance().logInternalDataError("FetchDataTypes", "Data types are not loaded", ErrorSeverity.ERROR);
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.DATA_TYPE_CANNOT_BE_EMPTY));
			} else {
				BeEcompErrorManager.getInstance().logInternalFlowError("FetchDataTypes", "Failed to fetch data types", ErrorSeverity.ERROR);
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
			}
		}
		return Either.left(allDataTypes.left().value());
	}

	protected Either<Boolean, ResponseFormat> validatePropertyDefaultValue(IComplexDefaultValue property, Map<String, DataTypeDefinition> dataTypes) {
		String type = null;
		String innerType = null;
		if (!propertyOperation.isPropertyTypeValid(property)) {
			log.info("Invalid type for property {} type {}", property.getName(), property.getType());
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_PROPERTY_TYPE, property.getType(), property.getName());
			return Either.right(responseFormat);
		}
		type = property.getType();
		if (type.equals(ToscaPropertyType.LIST.getType()) || type.equals(ToscaPropertyType.MAP.getType())) {
			ImmutablePair<String, Boolean> propertyInnerTypeValid = propertyOperation.isPropertyInnerTypeValid(property, dataTypes);
			innerType = propertyInnerTypeValid.getLeft();
			if (!propertyInnerTypeValid.getRight().booleanValue()) {
				log.info("Invalid inner type for property {} type {}", property.getName(), property.getType() );
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_PROPERTY_INNER_TYPE, innerType, property.getName());
				return Either.right(responseFormat);
			}
		}
		if (!propertyOperation.isPropertyDefaultValueValid(property, dataTypes)) {
			log.info("Invalid default value for property {} type {}", property.getName(), property.getType() );
			ResponseFormat responseFormat;
			if (type.equals(ToscaPropertyType.LIST.getType()) || type.equals(ToscaPropertyType.MAP.getType())) {
				responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_COMPLEX_DEFAULT_VALUE, property.getName(), type, innerType, property.getDefaultValue());
			} else {
				responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_DEFAULT_VALUE, property.getName(), type, property.getDefaultValue());
			}
			return Either.right(responseFormat);

		}
		return Either.left(true);
	}

//	protected Either<Resource, StorageOperationStatus> getResource(final String resourceId) {
//
//		log.debug("Get resource with id {}", resourceId);
//		Either<Resource, StorageOperationStatus> status = resourceOperation.getResource(resourceId);
//		if (status.isRight()) {
//			log.debug("Resource with id {} was not found", resourceId);
//			return Either.right(status.right().value());
//		}
//
//		Resource resource = status.left().value();
//		if (resource == null) {
//			BeEcompErrorManager.getInstance().logBeComponentMissingError("Property Business Logic", ComponentTypeEnum.RESOURCE.getValue(), resourceId);
//			log.debug("General Error while get resource with id {}", resourceId);
//			return Either.right(StorageOperationStatus.GENERAL_ERROR);
//		}
//		return Either.left(resource);
//	}

	protected void handleDefaultValue(IComplexDefaultValue newAttributeDef, Map<String, DataTypeDefinition> dataTypes) {
		// convert property
		ToscaPropertyType type = ToscaPropertyType.isValidType(newAttributeDef.getType());
		PropertyValueConverter converter = type.getConverter();
		// get inner type
		String innerType = null;

		if (newAttributeDef != null) {
			SchemaDefinition schema = newAttributeDef.getSchema();
			if (schema != null) {
				PropertyDataDefinition prop = schema.getProperty();
				if (schema.getProperty() != null) {
					innerType = prop.getType();
				}
			}
			String convertedValue = null;
			if (newAttributeDef.getDefaultValue() != null) {
				convertedValue = converter.convert(newAttributeDef.getDefaultValue(), innerType, dataTypes);
				newAttributeDef.setDefaultValue(convertedValue);
			}
		}
	}

	protected void validateComponentTypeEnum(ComponentTypeEnum componentTypeEnum, String errorContext, Wrapper<ResponseFormat> errorWrapper) {
		if (componentTypeEnum == null) {
			BeEcompErrorManager.getInstance().logInvalidInputError(errorContext, "invalid component type", ErrorSeverity.INFO);
			errorWrapper.setInnerElement(componentsUtils.getResponseFormat(ActionStatus.NOT_ALLOWED));
		}

	}

	protected void validateCanWorkOnComponent(String componentId, ComponentTypeEnum componentTypeEnum, String userId, Wrapper<ResponseFormat> errorWrapper) {
		if (!ComponentValidationUtils.canWorkOnComponent(componentId, toscaOperationFacade, userId)) {
			log.info("Restricted operation for user {} on {} {}", userId, componentTypeEnum.getValue(), componentId);
			errorWrapper.setInnerElement(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
		}

	}

	protected void validateComponentLock(String componentId, ComponentTypeEnum componentTypeEnum, Wrapper<ResponseFormat> errorWrapper) {
		StorageOperationStatus lockStatus = graphLockOperation.lockComponent(componentId, componentTypeEnum.getNodeType());
		if (lockStatus != StorageOperationStatus.OK) {
			log.debug("Failed to lock {} {}", componentTypeEnum.getValue(), componentId);
			errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(lockStatus)));
		}

	}

	protected ToscaPropertyType getType(String propertyType) {

		ToscaPropertyType type = ToscaPropertyType.isValidType(propertyType);

		return type;

	}

	protected void commitOrRollback(Either<? extends Object, ResponseFormat> result) {
		if (result == null || result.isRight()) {
			log.warn("operation failed. do rollback");
			titanDao.rollback();
		} else {
			log.debug("operation success. do commit");
			titanDao.commit();
		}
	}

	protected Either<Boolean, ResponseFormat> lockComponentByName(String name, Component component, String ecompErrorContext) {
		ComponentTypeEnum componentType = component.getComponentType();
		NodeTypeEnum nodeType = componentType.getNodeType();
		StorageOperationStatus lockResourceStatus = graphLockOperation.lockComponentByName(name, nodeType);

		if (lockResourceStatus.equals(StorageOperationStatus.OK)) {
			return Either.left(true);
		} else {
			BeEcompErrorManager.getInstance().logBeFailedLockObjectError(ecompErrorContext, nodeType.getName(), name);
			ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(lockResourceStatus, componentType);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(actionStatus, component.getName());
			log.debug("Failed to lock component {} error - {}", name, actionStatus);
			return Either.right(responseFormat);
		}
	}

	protected Either<Component, ResponseFormat> validateComponentExistsByFilter(String componentId, ComponentTypeEnum componentType, ComponentParametersView componentParametersView, boolean inTransaction) {

		Either<Component, StorageOperationStatus> componentFound = null;
		componentFound = toscaOperationFacade.getToscaElement(componentId, componentParametersView);

		if (componentFound.isRight()) {
			StorageOperationStatus storageOperationStatus = componentFound.right().value();
			ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(storageOperationStatus, componentType);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(actionStatus, Constants.EMPTY_STRING);
			log.debug("Component with id {} was not found", componentId);
			return Either.right(responseFormat);
		}
		return Either.left(componentFound.left().value());
	}
	
	protected Either<GroupProperty, ResponseFormat> validateFreeText(GroupProperty groupPropertyToUpdate) {

		Either<GroupProperty, ResponseFormat> ret;
		final String groupTypeValue = groupPropertyToUpdate.getValue();
		if (!StringUtils.isEmpty(groupTypeValue)) {
			if (!ValidationUtils.validateDescriptionLength(groupTypeValue)) {
				ret = Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_DESCRIPTION_EXCEEDS_LIMIT,
						NodeTypeEnum.Property.getName(),
						String.valueOf(ValidationUtils.COMPONENT_DESCRIPTION_MAX_LENGTH)));
			}

			else if (!ValidationUtils.validateIsEnglish(groupTypeValue)) {
				ret = Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INVALID_DESCRIPTION,
						NodeTypeEnum.Property.getName()));
			} else {
				ret = Either.left(groupPropertyToUpdate);
			}

		} else {
			ret = Either.left(groupPropertyToUpdate);
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends Enum<T>> boolean  enumHasValueFilter(String name, Function<String, T> enumGetter, T... enumValues) {
		T enumFound = enumGetter.apply(name);
		return Arrays.asList(enumValues).contains(enumFound);
	}
	
	protected Either<String, StorageOperationStatus> validatePropValueBeforeCreate(IPropertyInputCommon property, String value, boolean isValidate, String innerType, Map<String, DataTypeDefinition> allDataTypes) {
		String propertyType = property.getType();
		ToscaPropertyType type = ToscaPropertyType.isValidType(propertyType);

		if (type == ToscaPropertyType.LIST || type == ToscaPropertyType.MAP) {
			SchemaDefinition def = property.getSchema();
			if (def == null) {
				log.debug("Schema doesn't exists for property of type {}", type);
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.ILLEGAL_ARGUMENT));
			}
			PropertyDataDefinition propDef = def.getProperty();
			if (propDef == null) {
				log.debug("Property in Schema Definition inside property of type {} doesn't exist", type);

				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.ILLEGAL_ARGUMENT));
			}
			innerType = propDef.getType();
		}	

		Either<Object, Boolean> isValid = validateAndUpdatePropertyValue(propertyType, value, isValidate, innerType, allDataTypes);

		String newValue = value;
		if (isValid.isRight()) {
			Boolean res = isValid.right().value();
			if (res == false) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.ILLEGAL_ARGUMENT));
			}
		} else {
			Object object = isValid.left().value();
			if (object != null) {
				newValue = object.toString();
			}
		}

		ImmutablePair<String, Boolean> pair = validateAndUpdateRules(propertyType, property.getRules(), innerType, allDataTypes, isValidate);
		log.trace("After validateAndUpdateRules. pair = {}", pair);
		if (pair.getRight() != null && pair.getRight() == false) {
			BeEcompErrorManager.getInstance().logBeInvalidValueError("Add property value", pair.getLeft(), property.getName(), propertyType);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.ILLEGAL_ARGUMENT));
		}

		return Either.left(newValue);
	}
	
	protected Either<String, StorageOperationStatus> validateInputValueBeforeCreate(ComponentInstanceInput property, String value, boolean isValidate, String innerType, Map<String, DataTypeDefinition> allDataTypes) {
		String propertyType = property.getType();
		ToscaPropertyType type = ToscaPropertyType.isValidType(propertyType);

		if (type == ToscaPropertyType.LIST || type == ToscaPropertyType.MAP) {
			SchemaDefinition def = property.getSchema();
			if (def == null) {
				log.debug("Schema doesn't exists for property of type {}", type);
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.ILLEGAL_ARGUMENT));
			}
			PropertyDataDefinition propDef = def.getProperty();
			if (propDef == null) {
				log.debug("Property in Schema Definition inside property of type {} doesn't exist", type);

				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.ILLEGAL_ARGUMENT));
			}
			innerType = propDef.getType();
		}	

		Either<Object, Boolean> isValid = validateAndUpdatePropertyValue(propertyType, value, isValidate, innerType, allDataTypes);

		String newValue = value;
		if (isValid.isRight()) {
			Boolean res = isValid.right().value();
			if (res == false) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.ILLEGAL_ARGUMENT));
			}
		} else {
			Object object = isValid.left().value();
			if (object != null) {
				newValue = object.toString();
			}
		}

		ImmutablePair<String, Boolean> pair = validateAndUpdateRules(propertyType, property.getRules(), innerType, allDataTypes, isValidate);
		log.debug("After validateAndUpdateRules. pair = {}", pair);
		if (pair.getRight() != null && pair.getRight() == false) {
			BeEcompErrorManager.getInstance().logBeInvalidValueError("Add property value", pair.getLeft(), property.getName(), propertyType);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.ILLEGAL_ARGUMENT));
		}

		return Either.left(newValue);
	}
	
	public Either<Object, Boolean> validateAndUpdatePropertyValue(String propertyType, String value, boolean isValidate, String innerType, Map<String, DataTypeDefinition> dataTypes) {
		log.trace("Going to validate property value and its type. type = {}, value = {}", propertyType, value);
		ToscaPropertyType type = getType(propertyType);

		if (isValidate) {

			if (type == null) {
				DataTypeDefinition dataTypeDefinition = dataTypes.get(propertyType);
				ImmutablePair<JsonElement, Boolean> validateResult = dataTypeValidatorConverter.validateAndUpdate(value, dataTypeDefinition, dataTypes);
				if (validateResult.right.booleanValue() == false) {
					log.debug("The value {} of property from type {} is invalid", value, propertyType);
					return Either.right(false);
				}
				JsonElement jsonElement = validateResult.left;
				String valueFromJsonElement = getValueFromJsonElement(jsonElement);
				return Either.left(valueFromJsonElement);
			}
			log.trace("before validating property type {}", propertyType);
			boolean isValidProperty = isValidValue(type, value, innerType, dataTypes);
			if (false == isValidProperty) {
				log.debug("The value {} of property from type {} is invalid", value, type);
				return Either.right(false);
			}
		}
		Object convertedValue = value;
		if (false == isEmptyValue(value) && isValidate) {
			PropertyValueConverter converter = type.getConverter();
			convertedValue = converter.convert(value, innerType, dataTypes);
		}
		return Either.left(convertedValue);
	}

	public ImmutablePair<String, Boolean> validateAndUpdateRules(String propertyType, List<PropertyRule> rules, String innerType, Map<String, DataTypeDefinition> dataTypes, boolean isValidate) {

		if (rules == null || rules.isEmpty() == true) {
			return new ImmutablePair<String, Boolean>(null, true);
		}

		for (PropertyRule rule : rules) {
			String value = rule.getValue();
			Either<Object, Boolean> updateResult = validateAndUpdatePropertyValue(propertyType, value, isValidate, innerType, dataTypes);
			if (updateResult.isRight()) {
				Boolean status = updateResult.right().value();
				if (status == false) {
					return new ImmutablePair<String, Boolean>(value, status);
				}
			} else {
				String newValue = null;
				Object object = updateResult.left().value();
				if (object != null) {
					newValue = object.toString();
				}
				rule.setValue(newValue);
			}
		}

		return new ImmutablePair<String, Boolean>(null, true);
	}
	
	protected boolean isValidValue(ToscaPropertyType type, String value, String innerType, Map<String, DataTypeDefinition> dataTypes) {
		if (isEmptyValue(value)) {
			return true;
		}

		PropertyTypeValidator validator = type.getValidator();

		boolean isValid = validator.isValid(value, innerType, dataTypes);
		if (true == isValid) {
			return true;
		} else {
			return false;
		}

	}

	public boolean isEmptyValue(String value) {
		if (value == null) {
			return true;
		}
		return false;
	}

	public boolean isNullParam(String value) {
		if (value == null) {
			return true;
		}
		return false;
	}

	public void addRulesToNewPropertyValue(PropertyValueData propertyValueData, ComponentInstanceProperty resourceInstanceProperty, String resourceInstanceId) {

		List<PropertyRule> rules = resourceInstanceProperty.getRules();
		if (rules == null) {
			PropertyRule propertyRule = buildRuleFromPath(propertyValueData, resourceInstanceProperty, resourceInstanceId);
			rules = new ArrayList<>();
			rules.add(propertyRule);
		} else {
			rules = sortRules(rules);
		}

		propertyValueData.setRules(rules);
	}

	private PropertyRule buildRuleFromPath(PropertyValueData propertyValueData, ComponentInstanceProperty resourceInstanceProperty, String resourceInstanceId) {
		List<String> path = resourceInstanceProperty.getPath();
		// FOR BC. Since old Property values on VFC/VF does not have rules on
		// graph.
		// Update could be done on one level only, thus we can use this
		// operation to avoid migration.
		if (path == null || path.isEmpty() == true) {
			path = new ArrayList<>();
			path.add(resourceInstanceId);
		}
		PropertyRule propertyRule = new PropertyRule();
		propertyRule.setRule(path);
		propertyRule.setValue(propertyValueData.getValue());
		return propertyRule;
	}

	private List<PropertyRule> sortRules(List<PropertyRule> rules) {

		// TODO: sort the rules by size and binary representation.
		// (x, y, .+) --> 110 6 priority 1
		// (x, .+, z) --> 101 5 priority 2

		return rules;
	}
	
	protected String getValueFromJsonElement(JsonElement jsonElement) {
		String value = null;

		if (jsonElement == null || jsonElement.isJsonNull()) {
			value = EMPTY_VALUE;
		} else {
			if (jsonElement.toString().isEmpty()) {
				value = "";
			} else {
				value = jsonElement.toString();
			}
		}

		return value;
	}

}
