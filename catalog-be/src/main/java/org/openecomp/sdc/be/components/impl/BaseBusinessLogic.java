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

import com.google.gson.JsonElement;
import fj.data.Either;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.validation.UserValidations;
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
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.IComplexDefaultValue;
import org.openecomp.sdc.be.model.IPropertyInputCommon;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsontitan.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsontitan.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.impl.PolicyTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.openecomp.sdc.be.model.tosca.validators.DataTypeValidatorConverter;
import org.openecomp.sdc.be.model.tosca.validators.PropertyTypeValidator;
import org.openecomp.sdc.be.user.IUserBusinessLogic;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseBusinessLogic {

    private static final String FAILED_TO_LOCK_COMPONENT_ERROR = "Failed to lock component {} error - {}";

	private static final Logger log = Logger.getLogger(BaseBusinessLogic.class.getName());

    private static final String EMPTY_VALUE = null;
    private static final String SCHEMA_DOESN_T_EXISTS_FOR_PROPERTY_OF_TYPE = "Schema doesn't exists for property of type {}";
    private static final String PROPERTY_IN_SCHEMA_DEFINITION_INSIDE_PROPERTY_OF_TYPE_DOESN_T_EXIST = "Property in Schema Definition inside property of type {} doesn't exist";
    private static final String ADD_PROPERTY_VALUE = "Add property value";
    private static final String THE_VALUE_OF_PROPERTY_FROM_TYPE_IS_INVALID = "The value {} of property from type {} is invalid";
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
    IGroupInstanceOperation groupInstanceOperation;

    @Autowired
    protected IGroupTypeOperation groupTypeOperation;

    @Autowired
    protected GroupBusinessLogic groupBusinessLogic;

    @Autowired
    PolicyTypeOperation policyTypeOperation;

    @javax.annotation.Resource
    protected ArtifactsOperations artifactToscaOperation;

    @Autowired
    protected PropertyOperation propertyOperation;

    @Autowired
    protected ApplicationDataTypeCache applicationDataTypeCache;

    @Autowired
    protected ToscaOperationFacade toscaOperationFacade;

    @Autowired
    protected ApplicationDataTypeCache dataTypeCache;

    @Autowired
    protected InterfaceOperation interfaceOperation;

    @Autowired
    protected InterfaceOperationBusinessLogic interfaceOperationBusinessLogic;

    @Autowired
    protected InterfaceLifecycleOperation interfaceLifecycleTypeOperation;

    @javax.annotation.Resource
    private UserValidations userValidations;

    DataTypeValidatorConverter dataTypeValidatorConverter = DataTypeValidatorConverter.getInstance();


    public void setUserAdmin(UserBusinessLogic userAdmin) {
        this.userAdmin = userAdmin;
    }

    public void setUserValidations(UserValidations userValidations) {
        this.userValidations = userValidations;
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

    public void setPolicyTypeOperation(PolicyTypeOperation policyTypeOperation) {
        this.policyTypeOperation = policyTypeOperation;
    }

    public void setDataTypeCache(ApplicationDataTypeCache dataTypeCache) {
        this.dataTypeCache = dataTypeCache;
    }

    public void setPropertyOperation(PropertyOperation propertyOperation) {
        this.propertyOperation = propertyOperation;
    }

    public void setInterfaceOperation(InterfaceOperation interfaceOperation) {
        this.interfaceOperation = interfaceOperation;
    }
    public void setInterfaceOperationBusinessLogic(InterfaceOperationBusinessLogic interfaceOperationBusinessLogic) {
        this.interfaceOperationBusinessLogic = interfaceOperationBusinessLogic;
    }


    User validateUserNotEmpty(User user, String ecompErrorContext) {
        return userValidations.validateUserNotEmpty(user, ecompErrorContext);
    }

    protected User validateUserExists(User user, String ecompErrorContext, boolean inTransaction) {
        return userValidations.validateUserExists(user.getUserId(), ecompErrorContext, inTransaction);
    }

    protected void validateUserExist(String userId, String ecompErrorContext) {
      userValidations.validateUserExist(userId, ecompErrorContext);
    }

    public void setGroupTypeOperation(IGroupTypeOperation groupTypeOperation) {
        this.groupTypeOperation = groupTypeOperation;
    }


    Either<User, ActionStatus> validateUserExistsActionStatus(String userId, String ecompErrorContext) {
        return userValidations.validateUserExistsActionStatus(userId, ecompErrorContext);
    }

    public User validateUserExists(String userId, String ecompErrorContext, boolean inTransaction) {
        return userValidations.validateUserExists(userId, ecompErrorContext, inTransaction);
    }

    protected void validateUserRole(User user, List<Role> roles) {
        userValidations.validateUserRole(user, roles);
    }

    protected Either<Boolean, ResponseFormat> lockComponent(Component component, String ecompErrorContext) {
        return lockComponent(component.getUniqueId(), component, ecompErrorContext);
    }

    protected Either<Component, ResponseFormat> lockComponent(Component component, boolean shoulLock, String ecompErrorContext) {
        return shoulLock ? lockComponent(component.getUniqueId(), component, ecompErrorContext)
                .either(l -> Either.left(component), Either::right) : Either.left(component);
    }

    protected Either<Boolean, ResponseFormat> lockComponent(String componentId, Component component, String ecompErrorContext) {
        return lockElement( componentId,  component,  ecompErrorContext)
                .right()
                .map(r -> logAndConvertError(r, component.getUniqueId(), component.getName()) );
    }

    protected void lockComponent(String componentId, Component component, boolean needLock, String ecompErrorContext) {
        if (needLock){
            lockElement( componentId,  component,  ecompErrorContext)
                   .left()
                   .on(r -> logAndThrowException(r, component.getUniqueId(), component.getName()) );
        }
    }

    private Boolean logAndThrowException(ActionStatus status, String componentId, String name){
        log.debug(FAILED_TO_LOCK_COMPONENT_ERROR, componentId, status);
        throw new ComponentException(status, name);
    }

    private ResponseFormat logAndConvertError(ActionStatus status, String componentId, String name){
        ResponseFormat responseFormat = componentsUtils.getResponseFormat(status, name);
        log.debug(FAILED_TO_LOCK_COMPONENT_ERROR, componentId, status);
        return responseFormat;
    }

    private Either<Boolean, ActionStatus> lockElement(String componentId, Component component, String ecompErrorContext) {
        ComponentTypeEnum componentType = component.getComponentType();
        NodeTypeEnum nodeType = componentType.getNodeType();
        StorageOperationStatus lockResourceStatus = graphLockOperation.lockComponent(componentId, nodeType);

        if (lockResourceStatus.equals(StorageOperationStatus.OK)) {
            return Either.left(true);
        } else {
            BeEcompErrorManager.getInstance().logBeFailedLockObjectError(ecompErrorContext, nodeType.getName(), componentId);
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(lockResourceStatus, componentType);
            return Either.right(actionStatus);
        }
    }

    protected void unlockComponent(Either<?, ?> either, Component component, boolean inTransaction) {
        ComponentTypeEnum componentType = component.getComponentType();
        NodeTypeEnum nodeType = componentType.getNodeType();
        if (!inTransaction) {
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
    void unlockComponentById(Either<?, ?> either, String componentId) {
        Either<Component, StorageOperationStatus> component = toscaOperationFacade.getToscaElement(componentId);
        if(component.isLeft()) {
            unlockComponent(either, component.left().value(), false);
        }
    }

    <T> Either<Boolean, ResponseFormat> validateJsonBody(T bodyObject, Class<T> clazz) {
        if (bodyObject == null) {
            log.debug("Invalid JSON received for object of type {}", clazz.getSimpleName());
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
        } else {
            return Either.left(true);
        }
    }

    Either<ComponentTypeEnum, ResponseFormat> validateComponentType(String componentType) {
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
        if (componentTypeEnum == null) {
            log.debug("Invalid component type {}", componentType);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, componentType));
        } else {
            return Either.left(componentTypeEnum);
        }
    }

    protected Either<Component, ResponseFormat> validateComponentExists(String componentId, ComponentTypeEnum componentType, ComponentParametersView filter) {
        return toscaOperationFacade.getToscaElement(componentId, filter == null ? new ComponentParametersView() : filter)
                .right()
                .map(err -> handleGetComponentError(componentId, componentType, err))
                .left()
                .bind(cmpt -> validateComponentType(cmpt, componentType));
    }

    private Either<Component, ResponseFormat> validateComponentType(Component cmpt, ComponentTypeEnum componentType) {
        if (componentType != cmpt.getComponentType()) {
            log.debug("component {} is not of requested type {}", cmpt.getUniqueId(), componentType);
            ActionStatus cmptNotFoundError = componentsUtils.convertFromStorageResponse(StorageOperationStatus.NOT_FOUND, componentType);
            return Either.right(componentsUtils.getResponseFormat(cmptNotFoundError));
        }
        return Either.left(cmpt);
    }

    <T extends PropertyDataDefinition> Either<String, ResponseFormat> updateInputPropertyObjectValue(T property) {
        Either<Map<String, DataTypeDefinition>, TitanOperationStatus> allDataTypesEither = dataTypeCache.getAll();
        if (allDataTypesEither.isRight()) {
            TitanOperationStatus status = allDataTypesEither.right().value();
            BeEcompErrorManager.getInstance().logInternalFlowError("UpdatePropertyValueOnComponentInstance", "Failed to update property value on instance. Status is " + status, ErrorSeverity.ERROR);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(DaoStatusConverter.convertTitanStatusToStorageStatus(status))));
        }
        Map<String, DataTypeDefinition> allDataTypes = allDataTypesEither.left().value();
        String propertyType = property.getType();
        String innerType = getInnerType(property);
        // Specific Update Logic
        Either<Object, Boolean> isValid =
            propertyOperation.validateAndUpdatePropertyValue(propertyType, (String) property.getValue(), true,
                innerType, allDataTypes);
        String newValue = property.getValue();
        if (isValid.isRight()) {
            Boolean res = isValid.right().value();
            if (Boolean.FALSE.equals(res)) {
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.ILLEGAL_ARGUMENT))));
            }
        } else {
            Object object = isValid.left().value();
            if (object != null) {
                newValue = object.toString();
            }
        }
        return Either.left(newValue);
    }

    private <T extends PropertyDataDefinition> String getInnerType(T property){
        ToscaPropertyType type = ToscaPropertyType.isValidType(property.getType());
        log.debug("#getInnerType - The type of the property {} is {}", property.getUniqueId(), property.getType());
        String innerType = null;
        if (type == ToscaPropertyType.LIST || type == ToscaPropertyType.MAP) {
            if (property.getSchema() == null) {
                log.debug(SCHEMA_DOESN_T_EXISTS_FOR_PROPERTY_OF_TYPE, type);
                throw new ComponentException(componentsUtils.convertFromStorageResponse(StorageOperationStatus.INVALID_VALUE));
            }
            PropertyDataDefinition innerProperty = property.getSchema().getProperty();
            if (innerProperty == null) {
                log.debug(PROPERTY_IN_SCHEMA_DEFINITION_INSIDE_PROPERTY_OF_TYPE_DOESN_T_EXIST, type);
                throw new ComponentException(componentsUtils.convertFromStorageResponse(StorageOperationStatus.INVALID_VALUE));
            }
            innerType = innerProperty.getType();
        }
        return innerType;
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
        if (Boolean.TRUE.equals(component.getIsDeleted())) {
            log.debug("Component {} is marked as deleted", component.getUniqueId());
            return canWork;
        }

        return Either.left(true);
    }

    ComponentTypeEnum getComponentTypeByParentComponentType(ComponentTypeEnum parentComponentType) {
        switch (parentComponentType) {
            case SERVICE:
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

    Either<Boolean, ResponseFormat> validatePropertyDefaultValue(IComplexDefaultValue property, Map<String, DataTypeDefinition> dataTypes) {
        String type;
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
            if (!propertyInnerTypeValid.getRight()) {
                log.info("Invalid inner type for property {} type {}, dataTypeCount {}", property.getName(), property.getType(), dataTypes.size());
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_PROPERTY_INNER_TYPE, innerType, property.getName());
                return Either.right(responseFormat);
            }
        }
        if (!propertyOperation.isPropertyDefaultValueValid(property, dataTypes)) {
            log.info("Invalid default value for property {} type {}", property.getName(), property.getType());
            ResponseFormat responseFormat;
            if (type.equals(ToscaPropertyType.LIST.getType()) || type.equals(ToscaPropertyType.MAP.getType())) {
                responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_COMPLEX_DEFAULT_VALUE, property.getName(), type, innerType,
                    property.getDefaultValue());
            } else {
                responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_DEFAULT_VALUE, property.getName(), type,
                    property.getDefaultValue());
            }
            return Either.right(responseFormat);

        }
        return Either.left(true);
    }


    void handleDefaultValue(IComplexDefaultValue newAttributeDef, Map<String, DataTypeDefinition> dataTypes) {
        // convert property
        ToscaPropertyType type = ToscaPropertyType.isValidType(newAttributeDef.getType());
        PropertyValueConverter converter = type.getConverter();
        // get inner type
        String innerType = null;

        SchemaDefinition schema = newAttributeDef.getSchema();
        if (schema != null) {
            PropertyDataDefinition prop = schema.getProperty();
            if (schema.getProperty() != null) {
                innerType = prop.getType();
            }
        }
        String convertedValue;
        if (newAttributeDef.getDefaultValue() != null) {
            convertedValue = converter.convert(newAttributeDef.getDefaultValue(), innerType, dataTypes);
            newAttributeDef.setDefaultValue(convertedValue);
        }
    }

    void validateComponentTypeEnum(ComponentTypeEnum componentTypeEnum, String errorContext, Wrapper<ResponseFormat> errorWrapper) {
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

    void validateComponentLock(String componentId, ComponentTypeEnum componentTypeEnum, Wrapper<ResponseFormat> errorWrapper) {
        StorageOperationStatus lockStatus = graphLockOperation.lockComponent(componentId, componentTypeEnum.getNodeType());
        if (lockStatus != StorageOperationStatus.OK) {
            log.debug("Failed to lock {} {}", componentTypeEnum.getValue(), componentId);
            errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(lockStatus)));
        }

    }

    protected ToscaPropertyType getType(String propertyType) {
        return ToscaPropertyType.isValidType(propertyType);
    }

    void commitOrRollback(Either<?, ResponseFormat> result) {
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
            log.debug(FAILED_TO_LOCK_COMPONENT_ERROR, name, actionStatus);
            return Either.right(responseFormat);
        }
    }

    protected Either<Component, ResponseFormat> validateComponentExistsByFilter(String componentId, ComponentTypeEnum componentType, ComponentParametersView componentParametersView) {
        return toscaOperationFacade.getToscaElement(componentId, componentParametersView)
                .right()
                .map(err -> handleGetComponentError(componentId, componentType, err));

    }

    private ResponseFormat handleGetComponentError(String componentId, ComponentTypeEnum componentType, StorageOperationStatus getComponentError) {
        ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentError, componentType);
        ResponseFormat responseFormat = componentsUtils.getResponseFormat(actionStatus, componentId);
        log.debug("error fetching component with id {}. error status: {}", componentId, getComponentError);
        return responseFormat;
    }

    @SafeVarargs
    static <T extends Enum<T>> boolean enumHasValueFilter(String name, Function<String, T> enumGetter, T... enumValues) {
        T enumFound = enumGetter.apply(name);
        return Arrays.asList(enumValues).contains(enumFound);
    }

    String validatePropValueBeforeCreate(IPropertyInputCommon property, String value, boolean isValidate, String innerType, Map<String, DataTypeDefinition> allDataTypes) {
        String propertyType = property.getType();
        String updatedInnerType = updateInnerType(property, innerType);
        Either<Object, Boolean> isValid = validateAndUpdatePropertyValue(propertyType, value, isValidate, updatedInnerType, allDataTypes);
        String newValue = value;
        if (isValid.isRight()) {
            Boolean res = isValid.right().value();
            if (Boolean.FALSE.equals(res)) {
                throw new StorageException(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.ILLEGAL_ARGUMENT));
            }
        } else {
            Object object = isValid.left().value();
            if (object != null) {
                newValue = object.toString();
            }
        }
        ImmutablePair<String, Boolean> pair = validateAndUpdateRules(propertyType, property.getRules(), updatedInnerType, allDataTypes, isValidate);
        log.trace("After validateAndUpdateRules. pair = {}", pair);
        if (Boolean.FALSE.equals(pair.getRight())) {
            BeEcompErrorManager.getInstance().logBeInvalidValueError(ADD_PROPERTY_VALUE, pair.getLeft(), property.getName(), propertyType);
            throw new StorageException(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.ILLEGAL_ARGUMENT));
        }
        return newValue;
    }

    private String updateInnerType(IPropertyInputCommon property, String innerType) {
        ToscaPropertyType type = ToscaPropertyType.isValidType(property.getType());
        if (type == ToscaPropertyType.LIST || type == ToscaPropertyType.MAP) {
            SchemaDefinition def = property.getSchema();
            if (def == null) {
                log.debug(SCHEMA_DOESN_T_EXISTS_FOR_PROPERTY_OF_TYPE, type);
                failOnIllegalArgument();
            }
            PropertyDataDefinition propDef = def.getProperty();
            if (propDef == null) {
                log.debug(PROPERTY_IN_SCHEMA_DEFINITION_INSIDE_PROPERTY_OF_TYPE_DOESN_T_EXIST, type);
                failOnIllegalArgument();
            }
            return propDef.getType();
        }
        return innerType;
    }

    private void failOnIllegalArgument() {
        throw new ComponentException(
                componentsUtils.convertFromStorageResponse(
                        DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.ILLEGAL_ARGUMENT)));
    }

    public Either<Object, Boolean> validateAndUpdatePropertyValue(String propertyType, String value, boolean isValidate, String innerType, Map<String, DataTypeDefinition> dataTypes) {
        log.trace("Going to validate property value and its type. type = {}, value = {}", propertyType, value);
        ToscaPropertyType type = getType(propertyType);

        if (isValidate) {

            if (type == null) {
                DataTypeDefinition dataTypeDefinition = dataTypes.get(propertyType);
                ImmutablePair<JsonElement, Boolean> validateResult = dataTypeValidatorConverter.validateAndUpdate(value, dataTypeDefinition, dataTypes);
                if (Boolean.FALSE.equals(validateResult.right)) {
                    log.debug(THE_VALUE_OF_PROPERTY_FROM_TYPE_IS_INVALID, value, propertyType);
                    return Either.right(false);
                }
                JsonElement jsonElement = validateResult.left;
                String valueFromJsonElement = getValueFromJsonElement(jsonElement);
                return Either.left(valueFromJsonElement);
            }
            log.trace("before validating property type {}", propertyType);
            boolean isValidProperty = isValidValue(type, value, innerType, dataTypes);
            if (!isValidProperty) {
                log.debug(THE_VALUE_OF_PROPERTY_FROM_TYPE_IS_INVALID, value, type);
                return Either.right(false);
            }
        }
        Object convertedValue = value;
        if (!isEmptyValue(value) && isValidate) {
            PropertyValueConverter converter = type.getConverter();
            convertedValue = converter.convert(value, innerType, dataTypes);
        }
        return Either.left(convertedValue);
    }

    private ImmutablePair<String, Boolean> validateAndUpdateRules(String propertyType, List<PropertyRule> rules, String innerType, Map<String, DataTypeDefinition> dataTypes, boolean isValidate) {

        if (rules == null || rules.isEmpty()) {
            return ImmutablePair.of(null, true);
        }

        for (PropertyRule rule : rules) {
            String value = rule.getValue();
            Either<Object, Boolean> updateResult = validateAndUpdatePropertyValue(propertyType, value, isValidate, innerType, dataTypes);
            if (updateResult.isRight()) {
                Boolean status = updateResult.right().value();
                if (Boolean.FALSE.equals(status)) {
                    return ImmutablePair.of(value, status);
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

        return ImmutablePair.of(null, true);
    }

    protected boolean isValidValue(ToscaPropertyType type, String value, String innerType, Map<String, DataTypeDefinition> dataTypes) {
        if (isEmptyValue(value)) {
            return true;
        }

        PropertyTypeValidator validator = type.getValidator();

        return validator.isValid(value, innerType, dataTypes);
    }

    public boolean isEmptyValue(String value) {
        return value == null;
    }

    protected String getValueFromJsonElement(JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return EMPTY_VALUE;
        }
        if (jsonElement.toString().isEmpty()) {
            return "";
        }
        return jsonElement.toString();
    }

    protected void rollbackWithException(ActionStatus actionStatus, String... params) {
        titanDao.rollback();
        throw new ComponentException(actionStatus, params);
    }

    public  <T extends ToscaDataDefinition> Either<List<T>, ResponseFormat> declareProperties(String userId, String componentId,
            ComponentTypeEnum componentTypeEnum, ComponentInstInputsMap componentInstInputsMap) {

        return Either.left(new ArrayList<>());
    }

    public <T extends PropertyDataDefinition> List<PropertyConstraint> setInputConstraint(T inputDefinition) {
        if (StringUtils.isNotBlank(inputDefinition.getParentPropertyType())
                && StringUtils.isNotBlank(inputDefinition.getSubPropertyInputPath())) {
                return setConstraint(inputDefinition);
        }

        return Collections.emptyList();
    }

    private <T extends PropertyDataDefinition> List<PropertyConstraint> setConstraint(T inputDefinition) {
        List<PropertyConstraint> constraints = new ArrayList<>();
        String[] inputPathArr = inputDefinition.getSubPropertyInputPath().split("#");
        if (inputPathArr.length > 1) {
            inputPathArr = ArrayUtils.remove(inputPathArr, 0);
        }

        Map<String, DataTypeDefinition> dataTypeDefinitionMap =
                applicationDataTypeCache.getAll().left().value();

        String propertyType = inputDefinition.getParentPropertyType();

        for (String anInputPathArr : inputPathArr) {
            if (ToscaType.isPrimitiveType(propertyType)) {
                constraints.addAll(
                        dataTypeDefinitionMap.get(propertyType).getConstraints());
            } else if (!ToscaType.isCollectionType(propertyType)) {
                propertyType = setConstraintForComplexType(dataTypeDefinitionMap, propertyType, anInputPathArr,
                        constraints);
            }
        }

        return constraints;
    }

    private String setConstraintForComplexType(Map<String, DataTypeDefinition> dataTypeDefinitionMap,
                                               String propertyType,
                                               String anInputPathArr,
                                               List<PropertyConstraint> constraints) {
        String type = null;
        List<PropertyDefinition> propertyDefinitions =
                dataTypeDefinitionMap.get(propertyType).getProperties();
        for (PropertyDefinition propertyDefinition : propertyDefinitions) {
            if (propertyDefinition.getName().equals(anInputPathArr)) {
                if (ToscaType.isPrimitiveType(propertyDefinition.getType())) {
                    constraints.addAll(propertyDefinition.getConstraints());
                } else {
                    type = propertyDefinition.getType();
                }
                break;
            }
        }

        return type;
    }
}
