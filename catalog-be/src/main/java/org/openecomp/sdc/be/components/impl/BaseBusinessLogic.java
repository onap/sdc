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
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.IComplexDefaultValue;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.operations.api.IArtifactOperation;
import org.openecomp.sdc.be.model.operations.api.IAttributeOperation;
import org.openecomp.sdc.be.model.operations.api.IComponentOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.IPropertyOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ComponentOperation;
import org.openecomp.sdc.be.model.operations.impl.ProductOperation;
import org.openecomp.sdc.be.model.operations.impl.ResourceOperation;
import org.openecomp.sdc.be.model.operations.impl.ServiceOperation;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.IUserBusinessLogic;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fj.data.Either;

public abstract class BaseBusinessLogic {

	@Autowired
	protected ComponentsUtils componentsUtils;

	@Autowired
	protected IUserBusinessLogic userAdmin;

	@Autowired
	protected ResourceOperation resourceOperation;

	@Autowired
	protected IGraphLockOperation graphLockOperation;

	@Autowired
	protected ServiceOperation serviceOperation;

	@Autowired
	protected ProductOperation productOperation;

	@Autowired
	protected TitanGenericDao titanGenericDao;

	@Autowired
	protected IElementOperation elementDao;

	@Autowired
	protected IGroupOperation groupOperation;

	@Autowired
	protected IGroupTypeOperation groupTypeOperation;

	@Autowired
	protected IArtifactOperation artifactOperation;

	@Autowired
	protected IAttributeOperation attributeOperation;

	@Autowired
	protected IPropertyOperation propertyOperation;

	@Autowired
	protected ApplicationDataTypeCache applicationDataTypeCache;

	public void setUserAdmin(UserBusinessLogic userAdmin) {
		this.userAdmin = userAdmin;
	}

	public void setComponentsUtils(ComponentsUtils componentsUtils) {
		this.componentsUtils = componentsUtils;
	}

	public void setGraphLockOperation(IGraphLockOperation graphLockOperation) {
		this.graphLockOperation = graphLockOperation;
	}

	private static Logger log = LoggerFactory.getLogger(BaseBusinessLogic.class.getName());

	protected Either<User, ResponseFormat> validateUserNotEmpty(User user, String ecompErrorContext) {
		String userId = user.getUserId();

		if (StringUtils.isEmpty(userId)) {
			// user.setUserId("UNKNOWN");
			log.debug("User header is missing ");
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeUserMissingError, ecompErrorContext, user.getUserId());
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
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeUserMissingError, ecompErrorContext, userId);
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
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeUserMissingError, ecompErrorContext, userId);
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
			log.debug("Failed to lock component {} error - {}", componentId, actionStatus);
			return Either.right(responseFormat);
		}
	}

	protected void unlockComponent(Either<?, ?> either, Component component, boolean inTransaction) {
		ComponentTypeEnum componentType = component.getComponentType();
		NodeTypeEnum nodeType = componentType.getNodeType();
		if (false == inTransaction) {
			if (either == null || either.isRight()) {
				titanGenericDao.rollback();
			} else {
				titanGenericDao.commit();
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

	protected Either<Component, ResponseFormat> validateComponentExists(String componentId, ComponentTypeEnum componentType, boolean inTransaction, boolean createNewTransaction) {
		ComponentOperation componentOperation = getComponentOperation(componentType);
		Either<Component, StorageOperationStatus> componentFound = null;
		// if(createNewTransaction){
		// componentFound = componentOperation.getComponent_tx(componentId,
		// inTransaction);
		// }
		// else{
		componentFound = componentOperation.getComponent(componentId, inTransaction);
		// }

		if (componentFound.isRight()) {
			StorageOperationStatus storageOperationStatus = componentFound.right().value();
			ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(storageOperationStatus, componentType);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(actionStatus, Constants.EMPTY_STRING);
			log.debug("Component with id {} was not found", componentId);
			return Either.right(responseFormat);
		}
		return Either.left(componentFound.left().value());
	}

	protected Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponentExists(String componentId, ComponentTypeEnum componentType, ComponentParametersView componentParametersView, String userId,
			AuditingActionEnum auditingAction, User user) {

		ComponentOperation componentOperation = getComponentOperation(componentType);

		if (componentOperation == null) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
			log.debug("addGroup - not supported component type {}", componentType);
			// handleAuditing(auditingAction, null, componentId, user, null,
			// null, artifactId, responseFormat, componentType, null);
			return Either.right(responseFormat);
		}
		Either<? extends org.openecomp.sdc.be.model.Component, StorageOperationStatus> componentResult = componentOperation.getComponent(componentId, componentParametersView, true);

		if (componentResult.isRight()) {
			ActionStatus status = (componentType.equals(ComponentTypeEnum.RESOURCE)) ? ActionStatus.RESOURCE_NOT_FOUND : ActionStatus.SERVICE_NOT_FOUND;

			ResponseFormat responseFormat = componentsUtils.getResponseFormat(status, componentId);

			log.debug("Service not found, serviceId {}", componentId);
			// ComponentTypeEnum componentForAudit =
			// (componentType.equals(ComponentTypeEnum.RESOURCE)) ?
			// ComponentTypeEnum.RESOURCE : ComponentTypeEnum.SERVICE;
			// handleAuditing(auditingAction, null, componentId, user, null,
			// null, artifactId, responseFormat, componentForAudit, null);
			return Either.right(responseFormat);
		}
		return Either.left(componentResult.left().value());
	}

	public Either<Boolean, ResponseFormat> validateCanWorkOnComponent(Component component, String userId) {
		Either<Boolean, ResponseFormat> canWork = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
		if (component.getLifecycleState() != LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT) {
			log.debug("Component {} is not checked-out", component.getName());
			return canWork;
		}

		// verify user id is not null
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

	public ComponentOperation getComponentOperation(ComponentTypeEnum componentTypeEnum) {
		if (ComponentTypeEnum.SERVICE == componentTypeEnum) {
			return serviceOperation;
		} else if (ComponentTypeEnum.RESOURCE == componentTypeEnum) {
			return resourceOperation;
		} else if (ComponentTypeEnum.PRODUCT == componentTypeEnum) {
			return productOperation;
		}
		return null;
	}

	public IComponentOperation getIComponentOperation(ComponentTypeEnum componentTypeEnum) {

		switch (componentTypeEnum) {
		case SERVICE:
			return serviceOperation;
		case RESOURCE:
			return resourceOperation;
		case PRODUCT:
			return productOperation;
		default:
			break;
		}

		return null;
	}

	public ComponentOperation getComponentOperationByParentComponentType(ComponentTypeEnum parentComponentType) {
		switch (parentComponentType) {
		case SERVICE:
			return resourceOperation;
		case RESOURCE:
			return resourceOperation;
		case PRODUCT:
			return serviceOperation;
		default:
			break;
		}
		return null;
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
	public void setTitanGenericDao(TitanGenericDao titanGenericDao) {
		this.titanGenericDao = titanGenericDao;
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
		log.debug("validate property");
		String type = null;
		String innerType = null;
		if (!propertyOperation.isPropertyTypeValid(property)) {
			log.info("Invalid type for property");
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_PROPERTY_TYPE, property.getType(), property.getName());
			return Either.right(responseFormat);
		}
		type = property.getType();
		if (type.equals(ToscaPropertyType.LIST.getType()) || type.equals(ToscaPropertyType.MAP.getType())) {
			ImmutablePair<String, Boolean> propertyInnerTypeValid = propertyOperation.isPropertyInnerTypeValid(property, dataTypes);
			innerType = propertyInnerTypeValid.getLeft();
			if (!propertyInnerTypeValid.getRight().booleanValue()) {
				log.info("Invalid inner type for property");
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_PROPERTY_INNER_TYPE, innerType, property.getName());
				return Either.right(responseFormat);
			}
		}
		if (!propertyOperation.isPropertyDefaultValueValid(property, dataTypes)) {
			log.info("Invalid default value for property");
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

	protected Either<Resource, StorageOperationStatus> getResource(final String resourceId) {

		log.debug("Get resource with id {}", resourceId);
		Either<Resource, StorageOperationStatus> status = resourceOperation.getResource(resourceId);
		if (status.isRight()) {
			log.debug("Resource with id {} was not found", resourceId);
			return Either.right(status.right().value());
		}

		Resource resource = status.left().value();
		if (resource == null) {
			BeEcompErrorManager.getInstance().logBeComponentMissingError("Property Business Logic", ComponentTypeEnum.RESOURCE.getValue(), resourceId);
			log.debug("General Error while get resource with id {}", resourceId);
			return Either.right(StorageOperationStatus.GENERAL_ERROR);
		}
		return Either.left(resource);
	}

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
		IComponentOperation componentOperation = getIComponentOperation(componentTypeEnum);
		if (!ComponentValidationUtils.canWorkOnComponent(componentId, componentOperation, userId)) {
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
			titanGenericDao.rollback();
		} else {
			log.debug("operation success. do commit");
			titanGenericDao.commit();
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
		ComponentOperation componentOperation = getComponentOperation(componentType);
		Either<Component, StorageOperationStatus> componentFound = null;
		componentFound = componentOperation.getComponent(componentId, componentParametersView, inTransaction);

		if (componentFound.isRight()) {
			StorageOperationStatus storageOperationStatus = componentFound.right().value();
			ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(storageOperationStatus, componentType);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(actionStatus, Constants.EMPTY_STRING);
			log.debug("Component with id {} was not found", componentId);
			return Either.right(responseFormat);
		}
		return Either.left(componentFound.left().value());
	}
}
