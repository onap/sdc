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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.components.impl;

import static org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR;
import static org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode.DATA_ERROR;

import com.google.gson.JsonElement;
import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datamodel.utils.ArtifactUtils;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyRule;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstOutputsMap;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.IComplexDefaultValue;
import org.openecomp.sdc.be.model.IPropertyInputCommon;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.AttributeOperation;
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
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseBusinessLogic {

    private static final String FAILED_TO_LOCK_COMPONENT_ERROR = "Failed to lock component {} error - {}";
    private static final Logger log = Logger.getLogger(BaseBusinessLogic.class);
    private static final String EMPTY_VALUE = null;
    private static final String SCHEMA_DOESN_T_EXISTS_FOR_PROPERTY_OF_TYPE = "Schema doesn't exists for property of type {}";
    private static final String PROPERTY_IN_SCHEMA_DEFINITION_INSIDE_PROPERTY_OF_TYPE_DOESN_T_EXIST = "Property in Schema Definition inside property of type {} doesn't exist";
    private static final String ADD_PROPERTY_VALUE = "Add property value";
    private static final String THE_VALUE_OF_PROPERTY_FROM_TYPE_IS_INVALID = "The value {} of property from type {} is invalid";
    private static final String INVALID_PROPERTY_TYPE = "The property type {} is invalid";
    protected IGroupTypeOperation groupTypeOperation;
    protected InterfaceOperation interfaceOperation;
    protected IElementOperation elementDao;
    protected ComponentsUtils componentsUtils;
    protected UserBusinessLogic userAdmin;
    protected IGraphLockOperation graphLockOperation;
    protected JanusGraphDao janusGraphDao;
    protected JanusGraphGenericDao janusGraphGenericDao;
    protected PropertyOperation propertyOperation;
    protected AttributeOperation attributeOperation;
    protected ApplicationDataTypeCache applicationDataTypeCache;
    protected ToscaOperationFacade toscaOperationFacade;
    protected IGroupOperation groupOperation;
    protected IGroupInstanceOperation groupInstanceOperation;
    protected InterfaceLifecycleOperation interfaceLifecycleTypeOperation;
    protected PolicyTypeOperation policyTypeOperation;
    protected ArtifactsOperations artifactToscaOperation;
    protected UserValidations userValidations;
    DataTypeValidatorConverter dataTypeValidatorConverter = DataTypeValidatorConverter.getInstance();

    protected BaseBusinessLogic(IElementOperation elementDao, IGroupOperation groupOperation, IGroupInstanceOperation groupInstanceOperation,
                                IGroupTypeOperation groupTypeOperation, InterfaceOperation interfaceOperation,
                                InterfaceLifecycleOperation interfaceLifecycleTypeOperation, ArtifactsOperations artifactToscaOperation) {
        this.elementDao = elementDao;
        this.groupOperation = groupOperation;
        this.groupInstanceOperation = groupInstanceOperation;
        this.groupTypeOperation = groupTypeOperation;
        this.interfaceOperation = interfaceOperation;
        this.interfaceLifecycleTypeOperation = interfaceLifecycleTypeOperation;
        this.artifactToscaOperation = artifactToscaOperation;
    }

    @SafeVarargs
    static <T extends Enum<T>> boolean enumHasValueFilter(String name, Function<String, T> enumGetter, T... enumValues) {
        T enumFound = enumGetter.apply(name);
        return Arrays.asList(enumValues).contains(enumFound);
    }

    @Autowired
    public void setUserAdmin(UserBusinessLogic userAdmin) {
        this.userAdmin = userAdmin;
    }

    @Autowired
    public void setUserValidations(UserValidations userValidations) {
        this.userValidations = userValidations;
    }

    @Autowired
    public void setComponentsUtils(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }

    @Autowired
    public void setJanusGraphDao(JanusGraphDao janusGraphDao) {
        this.janusGraphDao = janusGraphDao;
    }

    @Autowired
    public void setApplicationDataTypeCache(ApplicationDataTypeCache applicationDataTypeCache) {
        this.applicationDataTypeCache = applicationDataTypeCache;
    }

    @Autowired
    public void setJanusGraphGenericDao(JanusGraphGenericDao janusGraphGenericDao) {
        this.janusGraphGenericDao = janusGraphGenericDao;
    }

    @Autowired
    public void setGraphLockOperation(IGraphLockOperation graphLockOperation) {
        this.graphLockOperation = graphLockOperation;
    }

    @Autowired
    public void setToscaOperationFacade(ToscaOperationFacade toscaOperationFacade) {
        this.toscaOperationFacade = toscaOperationFacade;
    }

    @Autowired
    void setPolicyTypeOperation(PolicyTypeOperation policyTypeOperation) {
        this.policyTypeOperation = policyTypeOperation;
    }

    @Autowired
    public void setPropertyOperation(PropertyOperation propertyOperation) {
        this.propertyOperation = propertyOperation;
    }

    @Autowired
    public void setAttributeOperation(AttributeOperation attributeOperation) {
        this.attributeOperation = attributeOperation;
    }

    User validateUserNotEmpty(User user, String ecompErrorContext) {
        return userValidations.validateUserNotEmpty(user, ecompErrorContext);
    }

    protected User validateUserExists(String userId) {
        return userValidations.validateUserExists(userId);
    }

    public User validateUserExists(User user) {
        return userValidations.validateUserExists(user);
    }

    ActionStatus validateUserExistsActionStatus(String userId) {
        return userValidations.validateUserExistsActionStatus(userId);
    }

    protected void validateUserRole(User user, List<Role> roles) {
        userValidations.validateUserRole(user, roles);
    }

    protected void lockComponent(Component component, String ecompErrorContext) {
        lockComponent(component.getUniqueId(), component, ecompErrorContext);
    }

    protected boolean isVolumeGroup(List<String> artifactsInGroup, List<ArtifactDefinition> deploymentArtifacts) {
        for (String artifactId : artifactsInGroup) {
            ArtifactDefinition artifactDef = ArtifactUtils.findArtifactInList(deploymentArtifacts, artifactId);
            if (artifactDef != null && artifactDef.getArtifactType().equalsIgnoreCase(ArtifactTypeEnum.HEAT_VOL.getType())) {
                return true;
            }
        }
        return false;
    }

    protected void lockComponent(Component component, boolean shouldLock, String ecompErrorContext) {
        if (shouldLock) {
            lockComponent(component.getUniqueId(), component, ecompErrorContext);
        }
    }

    protected void lockComponent(String componentId, Component component, String ecompErrorContext) {
        ActionStatus lock = lockElement(componentId, component, ecompErrorContext);
        if (lock != ActionStatus.OK) {
            logAndThrowComponentException(lock, component.getUniqueId(), component.getName());
        }
    }

    protected void lockComponent(String componentId, Component component, boolean needLock, String ecompErrorContext) {
        if (needLock) {
            lockComponent(componentId, component, ecompErrorContext);
        }
    }

    private ResponseFormat logAndThrowComponentException(ActionStatus status, String componentId, String name) {
        log.debug(FAILED_TO_LOCK_COMPONENT_ERROR, componentId, status);
        throw new ByActionStatusComponentException(status, name);
    }

    private ActionStatus lockElement(String componentId, Component component, String ecompErrorContext) {
        ComponentTypeEnum componentType = component.getComponentType();
        NodeTypeEnum nodeType = componentType.getNodeType();
        StorageOperationStatus lockResourceStatus = graphLockOperation.lockComponent(componentId, nodeType);
        if (lockResourceStatus == StorageOperationStatus.OK) {
            return ActionStatus.OK;
        } else {
            BeEcompErrorManager.getInstance().logBeFailedLockObjectError(ecompErrorContext, nodeType.getName(), componentId);
            return componentsUtils.convertFromStorageResponse(lockResourceStatus, componentType);
        }
    }

    protected void unlockComponent(boolean failed, Component component, boolean inTransaction) {
        if (component != null) {
            ComponentTypeEnum componentType = component.getComponentType();
            NodeTypeEnum nodeType = componentType.getNodeType();
            if (!inTransaction) {
                if (failed) {
                    janusGraphDao.rollback();
                } else {
                    janusGraphDao.commit();
                }
            }
            // unlock resource
            graphLockOperation.unlockComponent(component.getUniqueId(), nodeType);
        } else {
            log.debug("component is NULL");
        }
    }

    protected void unlockComponent(boolean failed, Component component) {
        unlockComponent(failed, component, false);
    }

    void unlockComponentById(boolean failed, String componentId) {
        Either<Component, StorageOperationStatus> component = toscaOperationFacade.getToscaElement(componentId);
        if (component.isLeft()) {
            unlockComponent(failed, component.left().value(), false);
        }
    }

    <T> Boolean validateJsonBody(T bodyObject, Class<T> clazz) {
        if (bodyObject == null) {
            log.debug("Invalid JSON received for object of type {}", clazz.getSimpleName());
            throw new ByActionStatusComponentException(ActionStatus.INVALID_CONTENT);
        } else {
            return true;
        }
    }

    ComponentTypeEnum validateComponentType(String componentType) {
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
        if (componentTypeEnum == null) {
            log.debug("Invalid component type {}", componentType);
            throw new ByActionStatusComponentException(ActionStatus.UNSUPPORTED_ERROR, componentType);
        } else {
            return componentTypeEnum;
        }
    }

    Component validateComponentExists(String componentId, ComponentTypeEnum componentType, ComponentParametersView filter) {
        Either<Component, StorageOperationStatus> toscaElement = toscaOperationFacade
            .getToscaElement(componentId, filter == null ? new ComponentParametersView() : filter);
        if (toscaElement.isRight()) {
            handleGetComponentError(componentId, componentType, toscaElement.right().value());
        }
        return validateComponentType(toscaElement.left().value(), componentType);
    }

    private Component validateComponentType(Component cmpt, ComponentTypeEnum componentType) {
        if (componentType != cmpt.getComponentType()) {
            log.debug("component {} is not of requested type {}", cmpt.getUniqueId(), componentType);
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(StorageOperationStatus.NOT_FOUND, componentType));
        }
        return cmpt;
    }

    <T extends PropertyDataDefinition> String updateInputPropertyObjectValue(T property) {
        String propertyType = property.getType();
        String innerType = getInnerType(property);
        // Specific Update Logic
        Either<Object, Boolean> isValid = Either.right(false);
        if (property.hasGetFunction()) {
            ToscaGetFunctionDataDefinition propertyToscaFunctions = property.getToscaGetFunction();
            Either<List<InputDefinition>, StorageOperationStatus> componentInputs = toscaOperationFacade
                .getComponentInputs(propertyToscaFunctions.getSourceUniqueId());
            if (componentInputs.isLeft()) {
                Optional<InputDefinition> input = componentInputs.left().value().stream()
                    .filter(inp -> inp.getUniqueId().equals(propertyToscaFunctions.getPropertyUniqueId())).findFirst();
                if (input.isPresent()) {
                    String inpType = input.get().getType();
                    SchemaDefinition inpSchema = input.get().getSchema();
                    String propType = property.getType();
                    SchemaDefinition propSchema = property.getSchema();
                    if (propType.equals(inpType) && (inpSchema == null || propSchema.equals(inpSchema))) {
                        isValid = Either.left(property.getValue());
                    }
                }
            }
        } else {
            isValid = propertyOperation
                .validateAndUpdatePropertyValue(propertyType, property.getValue(), true, innerType,
                    componentsUtils.getAllDataTypes(applicationDataTypeCache, property.getModel()));
        }
        String newValue = property.getValue();
        if (isValid.isRight()) {
            Boolean res = isValid.right().value();
            if (Boolean.FALSE.equals(res)) {
                throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(
                    DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.ILLEGAL_ARGUMENT)));
            }
        } else {
            Object object = isValid.left().value();
            if (object != null) {
                newValue = object.toString();
            }
        }
        return newValue;
    }

    <T extends PropertyDataDefinition> String getInnerType(T property) {
        ToscaPropertyType type = ToscaPropertyType.isValidType(property.getType());
        log.debug("#getInnerType - The type of the property {} is {}", property.getUniqueId(), property.getType());
        String innerType = null;
        if (type == ToscaPropertyType.LIST || type == ToscaPropertyType.MAP) {
            if (property.getSchema() == null) {
                log.debug(SCHEMA_DOESN_T_EXISTS_FOR_PROPERTY_OF_TYPE, type);
                throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(StorageOperationStatus.INVALID_VALUE));
            }
            PropertyDataDefinition innerProperty = property.getSchema().getProperty();
            if (innerProperty == null) {
                log.debug(PROPERTY_IN_SCHEMA_DEFINITION_INSIDE_PROPERTY_OF_TYPE_DOESN_T_EXIST, type);
                throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(StorageOperationStatus.INVALID_VALUE));
            }
            innerType = innerProperty.getType();
        }
        return innerType;
    }

    public void validateCanWorkOnComponent(Component component, String userId) {
        ActionStatus actionStatus = ActionStatus.RESTRICTED_OPERATION;
        // verify resource is not archived
        if (Boolean.TRUE.equals(component.isArchived())) {
            actionStatus = ActionStatus.COMPONENT_IS_ARCHIVED;
            throw new ByActionStatusComponentException(actionStatus, component.getName());
        }
        if (component.getLifecycleState() != LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT) {
            log.debug("Component {} is not checked-out", component.getName());
            throw new ByActionStatusComponentException(actionStatus);
        }
        // verify userId is not null
        if (userId == null) {
            log.debug("Current user userId is null");
            throw new ByActionStatusComponentException(actionStatus);
        }
        // verify component last update user is the current user
        String lastUpdaterUserId = component.getLastUpdaterUserId();
        if (!userId.equals(lastUpdaterUserId)) {
            log.debug("Current user is not last updater, last updater userId: {}, current user userId: {}", lastUpdaterUserId, userId);
            throw new ByActionStatusComponentException(actionStatus);
        }
        // verify resource is not deleted
        if (Boolean.TRUE.equals(component.getIsDeleted())) {
            log.debug("Component {} is marked as deleted", component.getUniqueId());
            throw new ByActionStatusComponentException(actionStatus);
        }
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

    Either<Boolean, ResponseFormat> validatePropertyDefaultValue(IComplexDefaultValue property, Map<String, DataTypeDefinition> dataTypes) {
        String type;
        String innerType = null;
        if (!propertyOperation.isPropertyTypeValid(property, dataTypes)) {
            log.info("Invalid type for property '{}' type '{}'", property.getName(), property.getType());
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(ActionStatus.INVALID_PROPERTY_TYPE, property.getType(), property.getName());
            return Either.right(responseFormat);
        }
        type = property.getType();
        if (type.equals(ToscaPropertyType.LIST.getType()) || type.equals(ToscaPropertyType.MAP.getType())) {
            ImmutablePair<String, Boolean> propertyInnerTypeValid = propertyOperation.isPropertyInnerTypeValid(property, dataTypes);
            innerType = propertyInnerTypeValid.getLeft();
            if (Boolean.FALSE.equals(propertyInnerTypeValid.getRight())) {
                log.info("Invalid inner type for property '{}' type '{}', dataTypeCount '{}'", property.getName(), property.getType(),
                    dataTypes.size());
                ResponseFormat responseFormat = componentsUtils
                    .getResponseFormat(ActionStatus.INVALID_PROPERTY_INNER_TYPE, innerType, property.getName());
                return Either.right(responseFormat);
            }
        }
        if (!propertyOperation.isPropertyDefaultValueValid(property, dataTypes)) {
            log.info("Invalid default value for property '{}' type '{}'", property.getName(), property.getType());
            ResponseFormat responseFormat;
            if (type.equals(ToscaPropertyType.LIST.getType()) || type.equals(ToscaPropertyType.MAP.getType())) {
                responseFormat = componentsUtils
                    .getResponseFormat(ActionStatus.INVALID_COMPLEX_DEFAULT_VALUE, property.getName(), type, innerType, property.getDefaultValue());
            } else {
                responseFormat = componentsUtils
                    .getResponseFormat(ActionStatus.INVALID_DEFAULT_VALUE, property.getName(), type, property.getDefaultValue());
            }
            return Either.right(responseFormat);
        }
        return Either.left(true);
    }

    void validateComponentTypeEnum(ComponentTypeEnum componentTypeEnum, String errorContext, Wrapper<ResponseFormat> errorWrapper) {
        if (componentTypeEnum == null) {
            BeEcompErrorManager.getInstance().logInvalidInputError(errorContext, "invalid component type", ErrorSeverity.INFO);
            errorWrapper.setInnerElement(componentsUtils.getResponseFormat(ActionStatus.NOT_ALLOWED));
        }
    }

    protected void validateCanWorkOnComponent(String componentId, ComponentTypeEnum componentTypeEnum, String userId,
                                              Wrapper<ResponseFormat> errorWrapper) {
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
            janusGraphDao.rollback();
        } else {
            log.debug("operation success. do commit");
            janusGraphDao.commit();
        }
    }

    protected Either<Boolean, ResponseFormat> lockComponentByName(String name, Component component, String ecompErrorContext) {
        ComponentTypeEnum componentType = component.getComponentType();
        NodeTypeEnum nodeType = componentType.getNodeType();
        StorageOperationStatus lockResourceStatus = graphLockOperation.lockComponentByName(name, nodeType);
        if (lockResourceStatus == StorageOperationStatus.OK) {
            return Either.left(true);
        } else {
            BeEcompErrorManager.getInstance().logBeFailedLockObjectError(ecompErrorContext, nodeType.getName(), name);
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(lockResourceStatus, componentType);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(actionStatus, component.getName());
            log.debug(FAILED_TO_LOCK_COMPONENT_ERROR, name, actionStatus);
            return Either.right(responseFormat);
        }
    }

    protected Component validateComponentExistsByFilter(String componentId, ComponentTypeEnum componentType,
                                                        ComponentParametersView componentParametersView) {
        return toscaOperationFacade.getToscaElement(componentId, componentParametersView).left()
            .on(err -> handleGetComponentError(componentId, componentType, err));
    }

    private Component handleGetComponentError(String componentId, ComponentTypeEnum componentType, StorageOperationStatus getComponentError) {
        ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentError, componentType);
        log.debug("error fetching component with id {}. error status: {}", componentId, getComponentError);
        throw new ByActionStatusComponentException(actionStatus, componentId);
    }

    String validatePropValueBeforeCreate(IPropertyInputCommon property, String value, boolean isValidate,
                                         Map<String, DataTypeDefinition> allDataTypes) {
        String propertyType = property.getType();
        String updatedInnerType = updateInnerType(property);
        Either<Object, Boolean> isValid = validateAndUpdatePropertyValue(propertyType, value, isValidate, updatedInnerType, allDataTypes);
        String newValue = value;
        if (isValid.isRight()) {
            Boolean res = isValid.right().value();
            if (Boolean.FALSE.equals(res)) {
                log.error(DATA_ERROR, this.getClass().getName(), "Dropping invalid value for property: {} , value: ", property, value);
                return "";
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
            throw new StorageException(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.ILLEGAL_ARGUMENT));
        }
        return newValue;
    }

    private String updateInnerType(IPropertyInputCommon property) {
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
        return null;
    }

    private void failOnIllegalArgument() {
        throw new ByActionStatusComponentException(componentsUtils
            .convertFromStorageResponse(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.ILLEGAL_ARGUMENT)));
    }

    public Either<Object, Boolean> validateAndUpdatePropertyValue(String propertyType, String value, boolean isValidate, String innerType,
                                                                  Map<String, DataTypeDefinition> dataTypes) {
        log.trace("Going to validate property value and its type. type = {}, value = {}", propertyType, value);
        ToscaPropertyType type = getType(propertyType);
        if (isValidate) {
            if (type == null) {
                DataTypeDefinition dataTypeDefinition = dataTypes.get(propertyType);
                if (dataTypeDefinition == null) {
                    log.debug(INVALID_PROPERTY_TYPE, propertyType);
                    return Either.right(false);
                }
                ImmutablePair<JsonElement, Boolean> validateResult = dataTypeValidatorConverter
                    .validateAndUpdate(value, dataTypeDefinition, dataTypes);
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

    private ImmutablePair<String, Boolean> validateAndUpdateRules(String propertyType, List<PropertyRule> rules, String innerType,
                                                                  Map<String, DataTypeDefinition> dataTypes, boolean isValidate) {
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
        janusGraphDao.rollback();
        throw new ByActionStatusComponentException(actionStatus, params);
    }

    public <T extends ToscaDataDefinition> Either<List<T>, ResponseFormat> declareProperties(final String userId, final String componentId,
                                                                                             final ComponentTypeEnum componentTypeEnum,
                                                                                             final ComponentInstInputsMap componentInstInputsMap) {
        return Either.left(new ArrayList<>());
    }

    public <T extends ToscaDataDefinition> Either<List<T>, ResponseFormat> declareAttributes(final String userId, final String componentId,
                                                                                             final ComponentTypeEnum componentTypeEnum,
                                                                                             final ComponentInstOutputsMap componentInstOutputsMap) {
        return Either.left(new ArrayList<>());
    }

    public <T extends PropertyDataDefinition> List<PropertyConstraint> setInputConstraint(T inputDefinition) {
        if (StringUtils.isNotBlank(inputDefinition.getParentPropertyType()) && StringUtils.isNotBlank(inputDefinition.getSubPropertyInputPath())) {
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
        final Map<String, DataTypeDefinition> dataTypeDefinitionMap =
            componentsUtils.getAllDataTypes(applicationDataTypeCache, inputDefinition.getModel());
        String propertyType = inputDefinition.getParentPropertyType();
        for (String anInputPathArr : inputPathArr) {
            if (ToscaType.isPrimitiveType(propertyType)) {
                constraints.addAll(dataTypeDefinitionMap.get(propertyType).getConstraints());
            } else if (!ToscaType.isCollectionType(propertyType)) {
                propertyType = setConstraintForComplexType(dataTypeDefinitionMap, propertyType, anInputPathArr, constraints);
            }
        }
        return constraints;
    }

    private String setConstraintForComplexType(Map<String, DataTypeDefinition> dataTypeDefinitionMap, String propertyType, String anInputPathArr,
                                               List<PropertyConstraint> constraints) {
        String type = null;
        List<PropertyDefinition> propertyDefinitions = dataTypeDefinitionMap.get(propertyType).getProperties();
        for (PropertyDefinition propertyDefinition : propertyDefinitions) {
            if (propertyDefinition.getName().equals(anInputPathArr)) {
                if (ToscaType.isPrimitiveType(propertyDefinition.getType())) {
                    constraints.addAll(propertyDefinition.safeGetConstraints());
                } else {
                    type = propertyDefinition.getType();
                }
                break;
            }
        }
        return type;
    }

    protected void unlockRollbackWithException(Component component, RuntimeException e) {
        janusGraphDao.rollback();
        graphLockOperation.unlockComponent(component.getUniqueId(), component.getComponentType().getNodeType());
        throw e;
    }

    protected void unlockWithCommit(Component component) {
        ComponentTypeEnum componentType = component.getComponentType();
        NodeTypeEnum nodeType = componentType.getNodeType();
        janusGraphDao.commit();
        graphLockOperation.unlockComponent(component.getUniqueId(), nodeType);
    }

    protected ComponentInstance componentInstanceException(StorageOperationStatus storageOperationStatus) {
        throw new StorageException(storageOperationStatus);
    }

    protected Component componentException(StorageOperationStatus storageOperationStatus) {
        throw new StorageException(storageOperationStatus);
    }

    protected PolicyDefinition componentExceptionPolicyDefinition(ResponseFormat responseFormat) {
        throw new ByResponseFormatComponentException(responseFormat);
    }

    protected List<ComponentInstanceProperty> componentInstancePropertyListException(StorageOperationStatus storageOperationStatus) {
        throw new StorageException(storageOperationStatus);
    }

    protected Component getComponent(final String componentId) throws BusinessLogicException {
        final Either<Component, StorageOperationStatus> result = toscaOperationFacade.getToscaElement(componentId);
        if (result.isRight()) {
            final StorageOperationStatus errorStatus = result.right().value();
            log.error(BUSINESS_PROCESS_ERROR, this.getClass().getName(), "Failed to fetch component information by component id, error {}",
                errorStatus);
            throw new BusinessLogicException(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(errorStatus)));
        }
        return result.left().value();
    }

    public String getComponentModelByComponentId(final String componentId) throws BusinessLogicException {
        return getComponent(componentId).getModel();
    }
}
