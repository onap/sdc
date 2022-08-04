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

import fj.data.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.catalog.enums.ChangeTypeEnum;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.generic.GenericTypeBusinessLogic;
import org.openecomp.sdc.be.components.validation.component.ComponentContactIdValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentDescriptionValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentIconValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentNameValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentProjectCodeValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentTagsValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentValidator;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datamodel.api.HighestFilterEnum;
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.FilterKeyEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.facade.operations.CatalogOperation;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.CapReqDef;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.IComponentInstanceConnectedElement;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.utils.CommonBeUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ComponentBusinessLogic extends BaseBusinessLogic {

    private static final Logger log = Logger.getLogger(ComponentBusinessLogic.class.getName());

    protected final GroupBusinessLogic groupBusinessLogic;
    protected ArtifactsBusinessLogic artifactsBusinessLogic;
    protected GenericTypeBusinessLogic genericTypeBusinessLogic;
    protected ComponentDescriptionValidator componentDescriptionValidator;
    protected ComponentProjectCodeValidator componentProjectCodeValidator;
    protected CatalogOperation catalogOperations;
    protected ComponentIconValidator componentIconValidator;
    protected ComponentTagsValidator componentTagsValidator;
    protected ComponentNameValidator componentNameValidator;
    protected ComponentContactIdValidator componentContactIdValidator;

    public ComponentBusinessLogic(IElementOperation elementDao, IGroupOperation groupOperation, IGroupInstanceOperation groupInstanceOperation,
                                  IGroupTypeOperation groupTypeOperation, GroupBusinessLogic groupBusinessLogic,
                                  InterfaceOperation interfaceOperation, InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
                                  ArtifactsBusinessLogic artifactsBusinessLogic, ArtifactsOperations artifactToscaOperation,
                                  ComponentContactIdValidator componentContactIdValidator, ComponentNameValidator componentNameValidator,
                                  ComponentTagsValidator componentTagsValidator, ComponentValidator componentValidator,
                                  ComponentIconValidator componentIconValidator, ComponentProjectCodeValidator componentProjectCodeValidator,
                                  ComponentDescriptionValidator componentDescriptionValidator) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation,
            artifactToscaOperation);
        this.artifactsBusinessLogic = artifactsBusinessLogic;
        this.groupBusinessLogic = groupBusinessLogic;
        this.componentContactIdValidator = componentContactIdValidator;
        this.componentNameValidator = componentNameValidator;
        this.componentTagsValidator = componentTagsValidator;
        this.componentIconValidator = componentIconValidator;
        this.componentProjectCodeValidator = componentProjectCodeValidator;
        this.componentDescriptionValidator = componentDescriptionValidator;
    }

    private static Either<ArtifactDefinition, Operation> saveToscaArtifactAndPopulateToscaArtifactsWithResult(Component component,
                                                                                                              final ComponentsUtils componentsUtils,
                                                                                                              final ArtifactTypeEnum artifactEnum,
                                                                                                              final BiFunction<Component, ArtifactDefinition, Either<ArtifactDefinition, Operation>> saveToscaArtifactPayloadFunction) {
        ArtifactDefinition artifactDefinition = getToscaArtifactByTypeOrThrowException(component, artifactEnum, componentsUtils);
        Either<ArtifactDefinition, Operation> result = saveToscaArtifactPayloadFunction.apply(component, artifactDefinition);
        if (result.isLeft()) {
            ArtifactDefinition def = result.left().value();
            component.getToscaArtifacts().put(def.getArtifactLabel(), def);
        }
        return result;
    }

    private static Optional<ArtifactDefinition> getToscaArtifactByType(final Map<String, ArtifactDefinition> toscaArtifacts,
                                                                       final ArtifactTypeEnum typeEnum) {
        return toscaArtifacts.values().stream().filter(p -> p.getArtifactType().equals(typeEnum.getType())).findAny();
    }

    private static ArtifactDefinition getToscaArtifactByTypeOrThrowException(final Component component, final ArtifactTypeEnum typeEnum,
                                                                             final ComponentsUtils componentsUtils) {
        return Optional.ofNullable(component.getToscaArtifacts()).flatMap(toscaArtifacts -> getToscaArtifactByType(toscaArtifacts, typeEnum))
            .orElseThrow(() -> {
                log.debug("Impossible to find a ToscaArtifact with type '{}' for {}", typeEnum.getType(), component);
                return new ByResponseFormatComponentException(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, typeEnum.name()));
            });
    }

    public void setComponentDescriptionValidator(ComponentDescriptionValidator componentDescriptionValidator) {
        this.componentDescriptionValidator = componentDescriptionValidator;
    }

    public void setComponentProjectCodeValidator(ComponentProjectCodeValidator componentProjectCodeValidator) {
        this.componentProjectCodeValidator = componentProjectCodeValidator;
    }

    public void setComponentIconValidator(ComponentIconValidator componentIconValidator) {
        this.componentIconValidator = componentIconValidator;
    }

    public void setComponentContactIdValidator(ComponentContactIdValidator componentContactIdValidator) {
        this.componentContactIdValidator = componentContactIdValidator;
    }

    public void setComponentTagsValidator(ComponentTagsValidator componentTagsValidator) {
        this.componentTagsValidator = componentTagsValidator;
    }

    public void setComponentNameValidator(ComponentNameValidator componentNameValidator) {
        this.componentNameValidator = componentNameValidator;
    }

    @Autowired
    public void setGenericTypeBusinessLogic(GenericTypeBusinessLogic genericTypeBusinessLogic) {
        this.genericTypeBusinessLogic = genericTypeBusinessLogic;
    }

    public abstract Either<List<String>, ResponseFormat> deleteMarkedComponents();

    public abstract ComponentInstanceBusinessLogic getComponentInstanceBL();

    public abstract Either<List<ComponentInstance>, ResponseFormat> getComponentInstancesFilteredByPropertiesAndInputs(String componentId,
                                                                                                                       String userId);

    /**
     * @param componentId
     * @param dataParamsToReturn
     * @return
     */
    public abstract Either<UiComponentDataTransfer, ResponseFormat> getUiComponentDataTransferByComponentId(String componentId,
                                                                                                            List<String> dataParamsToReturn);
    
    public Either<ComponentMetadataData, StorageOperationStatus> getComponentMetadata(final String componentId) {
        return toscaOperationFacade.getComponentMetadata(componentId);
    }

    User validateUser(User user, String ecompErrorContext, Component component, AuditingActionEnum auditAction, boolean inTransaction) {
        User validatedUser;
        ResponseFormat responseFormat;
        try {
            validateUserNotEmpty(user, ecompErrorContext);
            validatedUser = validateUserExists(user);
        } catch (ByActionStatusComponentException e) {
            if (e.getActionStatus() == ActionStatus.MISSING_INFORMATION) {
                user.setUserId("UNKNOWN");
            }
            responseFormat = componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams());
            componentsUtils.auditComponentAdmin(responseFormat, user, component, auditAction, component.getComponentType());
            throw e;
        } catch (ByResponseFormatComponentException e) {
            responseFormat = e.getResponseFormat();
            componentsUtils.auditComponentAdmin(responseFormat, user, component, auditAction, component.getComponentType());
            throw e;
        }
        return validatedUser;
    }

    protected void validateUserRole(User user, Component component, List<Role> roles, AuditingActionEnum auditAction, String comment) {
        if (roles != null && roles.isEmpty()) {
            roles.add(Role.ADMIN);
            roles.add(Role.DESIGNER);
        }
        try {
            validateUserRole(user, roles);
        } catch (ByActionStatusComponentException e) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams());
            handleComponentException(component, comment, responseFormat, user, auditAction);
            throw e;
        } catch (ByResponseFormatComponentException e) {
            ResponseFormat responseFormat = e.getResponseFormat();
            handleComponentException(component, comment, responseFormat, user, auditAction);
            throw e;
        }
    }

    private void handleComponentException(Component component, String comment, ResponseFormat responseFormat, User user,
                                          AuditingActionEnum auditAction) {
        String commentStr = null;
        String distrStatus = null;
        ComponentTypeEnum componentType = component.getComponentType();
        if (componentType == ComponentTypeEnum.SERVICE) {
            distrStatus = ((ServiceMetadataDataDefinition) component.getComponentMetadataDefinition().getMetadataDataDefinition())
                .getDistributionStatus();
            commentStr = comment;
        }
        componentsUtils.auditComponent(responseFormat, user, component, auditAction, new ResourceCommonInfo(componentType.getValue()),
            ResourceVersionInfo.newBuilder().distributionStatus(distrStatus).build(),
            ResourceVersionInfo.newBuilder().distributionStatus(distrStatus).build(), commentStr, null, null);
    }

    public Either<Boolean, ResponseFormat> validateConformanceLevel(String componentUuid, ComponentTypeEnum componentTypeEnum, String userId) {
        log.trace("validate conformance level");
        if (componentTypeEnum != ComponentTypeEnum.SERVICE) {
            log.error("conformance level validation for non service component, id {}", componentUuid);
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
            return Either.right(errorResponse);
        }
        validateUserExists(userId);
        Either<ComponentMetadataData, StorageOperationStatus> eitherComponent = toscaOperationFacade
            .getLatestComponentMetadataByUuid(componentUuid, JsonParseFlagEnum.ParseMetadata, null);
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
            log.error("invalid asset conformance level, uuid {}, asset conformanceLevel {}, config conformanceLevel {}", componentUuid,
                componentConformanceLevel, configConformanceLevel);
            result = false;
        }
        log.trace("conformance level validation finished");
        return Either.left(result);
    }

    protected void validateIcon(User user, Component component, AuditingActionEnum actionEnum) {
        log.debug("validate Icon");
        ComponentTypeEnum type = component.getComponentType();
        String icon = component.getIcon();
        if (!ValidationUtils.validateStringNotEmpty(icon)) {
            log.info("icon is missing.");
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_ICON, type.getValue());
            componentsUtils.auditComponentAdmin(errorResponse, user, component, actionEnum, type);
            throw new ComponentException(ActionStatus.COMPONENT_MISSING_ICON, type.getValue());
        }
        try {
            validateIcon(icon, type);
        } catch (ComponentException e) {
            ResponseFormat responseFormat =
                e.getResponseFormat() != null ? e.getResponseFormat() : componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams());
            componentsUtils.auditComponentAdmin(responseFormat, user, component, actionEnum, type);
            throw e;
        }
    }

    private void validateIcon(String icon, ComponentTypeEnum type) {
        if (icon != null) {
            if (!ValidationUtils.validateIconLength(icon)) {
                log.debug("icon exceeds max length");
                throw new ComponentException(ActionStatus.COMPONENT_ICON_EXCEEDS_LIMIT, type.getValue(), "" + ValidationUtils.ICON_MAX_LENGTH);
            }
            if (!ValidationUtils.validateIcon(icon)) {
                log.info("icon is invalid.");
                throw new ComponentException(ActionStatus.COMPONENT_INVALID_ICON, type.getValue());
            }
        }
    }

    protected void checkComponentFieldsForOverrideAttempt(Component component) {
        if (component.getLifecycleState() != null) {
            log.info("LifecycleState cannot be defined by user. This field will be overridden by the application");
        }
        if (component.getVersion() != null) {
            log.info("Version cannot be defined by user. This field will be overridden by the application");
        }
        if (component.getCreatorUserId() != null || component.getCreatorFullName() != null) {
            log.info("Creator cannot be defined by user. This field will be overridden by the application");
        }
        if (component.getLastUpdaterUserId() != null || component.getLastUpdaterFullName() != null) {
            log.info("Last Updater cannot be defined by user. This field will be overridden by the application");
        }
        if (component.getCreationDate() != null) {
            log.info("Creation Date cannot be defined by user. This field will be overridden by the application");
        }
        if (component.isHighestVersion() != null) {
            log.info("Is Highest Version cannot be defined by user. This field will be overridden by the application");
        }
        if (component.getUUID() != null) {
            log.info("UUID cannot be defined by user. This field will be overridden by the application");
        }
        if (component.getLastUpdateDate() != null) {
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

    protected void validateComponentFieldsBeforeCreate(User user, Component component, AuditingActionEnum actionEnum) {
        // validate component name uniqueness
        log.debug("validate component name ");
        componentNameValidator.validateAndCorrectField(user, component, actionEnum);
        // validate description
        log.debug("validate description");
        componentDescriptionValidator.validateAndCorrectField(user, component, actionEnum);
        // validate tags
        log.debug("validate tags");
        componentTagsValidator.validateAndCorrectField(user, component, actionEnum);
        // validate contact info
        log.debug("validate contact info");
        componentContactIdValidator.validateAndCorrectField(user, component, actionEnum);
        // validate icon
        log.debug("validate icon");
        validateIcon(user, component, actionEnum);
    }

    public CapReqDef getRequirementsAndCapabilities(String componentId, ComponentTypeEnum componentTypeEnum, String userId) {
        validateUserExists(userId);
        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreCapabilities(false);
        filter.setIgnoreRequirements(false);
        filter.setIgnoreComponentInstances(false);
        try {
            Component component = validateComponentExists(componentId, componentTypeEnum, filter);
            return new CapReqDef(component.getRequirements(), component.getCapabilities());
        } catch (ComponentException e) {
            BeEcompErrorManager.getInstance().logBeComponentMissingError("getRequirementsAndCapabilities", componentTypeEnum.getValue(), componentId);
            throwComponentException(e.getResponseFormat());
        }
        return null;
    }

    public Either<List<Component>, ResponseFormat> getLatestVersionNotAbstractComponents(boolean isAbstractAbstract,
                                                                                         ComponentTypeEnum componentTypeEnum,
                                                                                         String internalComponentType, List<String> componentUids,
                                                                                         String userId) {
        try {
            validateUserExists(userId);
            List<Component> result = new ArrayList<>();
            List<String> componentsUidToFetch = new ArrayList<>();
            componentsUidToFetch.addAll(componentUids);
            if (!componentsUidToFetch.isEmpty()) {
                log.debug("Number of Components to fetch from graph is {}", componentsUidToFetch.size());
                Either<List<Component>, StorageOperationStatus> nonCheckoutCompResponse = toscaOperationFacade
                    .getLatestVersionNotAbstractComponents(isAbstractAbstract, componentTypeEnum, internalComponentType, componentsUidToFetch);
                if (nonCheckoutCompResponse.isLeft()) {
                    log.debug("Retrived Resource successfully.");
                    result.addAll(nonCheckoutCompResponse.left().value());
                } else {
                    return Either.right(
                        componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(nonCheckoutCompResponse.right().value())));
                }
            }
            return Either.left(result);
        } finally {
            janusGraphDao.commit();
        }
    }

    public Either<List<Component>, ResponseFormat> getLatestVersionNotAbstractComponentsMetadata(final boolean isAbstractAbstract,
                                                                                                 final HighestFilterEnum highestFilter,
                                                                                                 final ComponentTypeEnum componentTypeEnum,
                                                                                                 final String internalComponentType, String userId,
                                                                                                 final String modelName,
                                                                                                 final boolean includeNormativeExtensionModels) {
        Either<List<Component>, StorageOperationStatus> nonCheckoutCompResponse = null;
        try {
            validateUserExists(userId);
            nonCheckoutCompResponse = toscaOperationFacade
                .getLatestVersionNotAbstractMetadataOnly(isAbstractAbstract, componentTypeEnum, internalComponentType, modelName,
                    includeNormativeExtensionModels);
            if (nonCheckoutCompResponse.isLeft()) {
                log.debug("Retrieved Resource successfully.");
                return Either.left(nonCheckoutCompResponse.left().value());
            }
            return Either
                .right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(nonCheckoutCompResponse.right().value())));
        } finally {
            if (nonCheckoutCompResponse != null && nonCheckoutCompResponse.isLeft()) {
                janusGraphDao.commit();
            }
        }
    }

    public void setDeploymentArtifactsPlaceHolder(Component component, User user) {
    }

    @SuppressWarnings("unchecked")
    public void setToscaArtifactsPlaceHolders(Component component, User user) {
        Map<String, ArtifactDefinition> artifactMap = component.getToscaArtifacts();
        if (artifactMap == null) {
            artifactMap = new HashMap<>();
        }
        String componentUniqueId = component.getUniqueId();
        String componentSystemName = component.getSystemName();
        String componentType = component.getComponentType().getValue().toLowerCase();
        Map<String, Object> toscaArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration().getToscaArtifacts();
        if (toscaArtifacts != null) {
            for (Entry<String, Object> artifactInfoMap : toscaArtifacts.entrySet()) {
                Map<String, Object> artifactInfo = (Map<String, Object>) artifactInfoMap.getValue();
                ArtifactDefinition artifactDefinition = artifactsBusinessLogic
                    .createArtifactPlaceHolderInfo(componentUniqueId, artifactInfoMap.getKey(), artifactInfo, user, ArtifactGroupTypeEnum.TOSCA);
                artifactDefinition
                    .setArtifactName(ValidationUtils.normalizeFileName(componentType + "-" + componentSystemName + artifactInfo.get("artifactName")));
                artifactMap.put(artifactDefinition.getArtifactLabel(), artifactDefinition);
            }
        }
        component.setToscaArtifacts(artifactMap);
    }

    public Either<ArtifactDefinition, Operation> populateToscaArtifacts(Component component, User user, boolean isInCertificationRequest,
                                                                        boolean inTransaction, boolean shouldLock) {
        return populateToscaArtifacts(component, user, isInCertificationRequest, inTransaction, shouldLock, true, true);
    }

    public Either<ArtifactDefinition, Operation> populateToscaArtifacts(Component component, User user, boolean isInCertificationRequest,
                                                                        boolean inTransaction, boolean shouldLock, boolean retrieveResource) {
        return populateToscaArtifacts(component, user, isInCertificationRequest, inTransaction, shouldLock, true, retrieveResource);
    }

    private Either<ArtifactDefinition, Operation> populateToscaArtifacts(Component component, User user, boolean isInCertificationRequest,
                                                                         boolean inTransaction, boolean shouldLock, boolean fetchTemplatesFromDB,
                                                                         boolean retrieveResource) {
        if (retrieveResource) {
            Either<Component, StorageOperationStatus> toscaElement = toscaOperationFacade.getToscaFullElement(component.getUniqueId());
            if (toscaElement.isRight()) {
                throw new ByActionStatusComponentException(
                    componentsUtils.convertFromStorageResponse(toscaElement.right().value(), component.getComponentType()));
            }
            component = toscaElement.left().value();
        }
        Either<ArtifactDefinition, Operation> generateToscaRes = saveToscaArtifactAndPopulateToscaArtifactsWithResult(component, componentsUtils,
            ArtifactTypeEnum.TOSCA_TEMPLATE,
            (comp, toscaArtifact) -> saveToscaArtifactPayload(toscaArtifact, comp, user, isInCertificationRequest, shouldLock, inTransaction,
                fetchTemplatesFromDB));
        if (!isAbstractResource(component)) {
            generateToscaRes = saveToscaArtifactAndPopulateToscaArtifactsWithResult(component, componentsUtils, ArtifactTypeEnum.TOSCA_CSAR,
                (comp, toscaArtifactArg) -> saveToscaArtifactPayload(toscaArtifactArg, comp, user, isInCertificationRequest, shouldLock,
                    inTransaction, true));
        }
        return generateToscaRes;
    }

    private boolean isAbstractResource(Component component) {
        return component.getComponentType() == ComponentTypeEnum.RESOURCE && ((Resource) component).isAbstract();
    }

    private Either<ArtifactDefinition, Operation> saveToscaArtifactPayload(ArtifactDefinition artifactDefinition,
                                                                           org.openecomp.sdc.be.model.Component component, User user,
                                                                           boolean isInCertificationRequest, boolean shouldLock,
                                                                           boolean inTransaction, boolean fetchTemplatesFromDB) {
        return artifactsBusinessLogic
            .generateAndSaveToscaArtifact(artifactDefinition, component, user, isInCertificationRequest, shouldLock, inTransaction,
                fetchTemplatesFromDB);
    }

    public ImmutablePair<String, byte[]> getToscaModelByComponentUuid(ComponentTypeEnum componentType, String uuid,
                                                                      ResourceCommonInfo resourceCommonInfo) {
        Either<List<Component>, StorageOperationStatus> latestVersionEither = toscaOperationFacade.getComponentListByUuid(uuid, null);
        if (latestVersionEither.isRight()) {
            throw new ByActionStatusComponentException(
                componentsUtils.convertFromStorageResponse(latestVersionEither.right().value(), componentType));
        }
        List<Component> components = latestVersionEither.left().value();
        Component component = components.stream().filter(Component::isHighestVersion).findFirst().orElse(null);
        if (component == null) {
            component = components.stream().filter(c -> c.getLifecycleState() == LifecycleStateEnum.CERTIFIED).findFirst().orElse(null);
        }
        if (component == null) {
            throw new ByResponseFormatComponentException(
                componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(StorageOperationStatus.NOT_FOUND, componentType)));
        }
        resourceCommonInfo.setResourceName(component.getName());
        // TODO remove after migration - handle artifact not found(no

        // placeholder)
        if (null == component.getToscaArtifacts() || component.getToscaArtifacts().isEmpty()) {
            throw new ByResponseFormatComponentException(
                componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, ArtifactTypeEnum.TOSCA_CSAR.name()));
        }

        final ArtifactDefinition csarArtifact = component.getToscaArtifacts().values().stream()
            .filter(p -> p.getArtifactType().equals(ArtifactTypeEnum.TOSCA_CSAR.getType())).findAny().orElseThrow(() -> {
                throw new ByResponseFormatComponentException(
                    componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, ArtifactTypeEnum.TOSCA_CSAR.name()));
            });
        return artifactsBusinessLogic.handleDownloadToscaModelRequest(component, csarArtifact);
    }

    protected StorageOperationStatus markComponentToDelete(Component component) {
        ComponentTypeEnum componentType = component.getComponentType();
        String uniqueId = component.getUniqueId();
        if (Boolean.TRUE.equals(component.getIsDeleted())) {
            log.info("component {} already marked as deleted. id= {}, type={}", component.getName(), uniqueId, componentType);
            return StorageOperationStatus.NOT_FOUND;
        }
        StorageOperationStatus markResourceToDelete = toscaOperationFacade.markComponentToDelete(component);
        if (StorageOperationStatus.OK != markResourceToDelete) {
            log.debug("failed to mark component {} of type {} for delete. error = {}", uniqueId, componentType, markResourceToDelete);
            return markResourceToDelete;
        } else {
            log.debug("Component {}  of type {} was marked as deleted", uniqueId, componentType);
            updateCatalog(component, ChangeTypeEnum.DELETE);
            return StorageOperationStatus.OK;
        }
    }

    public Either<Boolean, ResponseFormat> validateAndUpdateDescription(User user, Component currentComponent, Component updatedComponent,
                                                                        AuditingActionEnum auditingAction) {
        String descriptionUpdated = updatedComponent.getDescription();
        String descriptionCurrent = currentComponent.getDescription();
        if (descriptionUpdated != null && !descriptionCurrent.equals(descriptionUpdated)) {
            componentDescriptionValidator.validateAndCorrectField(user, updatedComponent, auditingAction);
            currentComponent.setDescription(updatedComponent.getDescription());
        }
        return Either.left(true);
    }

    public Either<Boolean, ResponseFormat> validateAndUpdateProjectCode(User user, Component currentComponent, Component updatedComponent) {
        String projectCodeUpdated = updatedComponent.getProjectCode();
        String projectCodeCurrent = currentComponent.getProjectCode();
        if (projectCodeUpdated != null && !projectCodeCurrent.equals(projectCodeUpdated)) {
            try {
                componentProjectCodeValidator.validateAndCorrectField(user, updatedComponent, null);
            } catch (ComponentException exp) {
                ResponseFormat errorRespons = exp.getResponseFormat();
                return Either.right(errorRespons);
            }
            currentComponent.setProjectCode(updatedComponent.getProjectCode());
        }
        return Either.left(true);
    }

    public Either<Boolean, ResponseFormat> validateAndUpdateIcon(User user, Component currentComponent, Component updatedComponent,
                                                                 boolean hasBeenCertified) {
        String iconUpdated = updatedComponent.getIcon();
        String iconCurrent = currentComponent.getIcon();
        if (iconUpdated != null && !iconCurrent.equals(iconUpdated)) {
            if (!hasBeenCertified) {
                componentIconValidator.validateAndCorrectField(user, updatedComponent, null);
                currentComponent.setIcon(updatedComponent.getIcon());
            } else {
                log.info("icon {} cannot be updated once the component has been certified once.", iconUpdated);
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_PARAMETER_CANNOT_BE_CHANGED, "Icon",
                    currentComponent.getComponentType().name().toLowerCase());
                return Either.right(errorResponse);
            }
        }
        return Either.left(true);
    }

    protected Either<List<String>, ResponseFormat> deleteMarkedComponents(ComponentTypeEnum componentType) {
        log.trace("start deleteMarkedComponents");
        Either<List<String>, StorageOperationStatus> deleteMarkedElements = toscaOperationFacade.deleteMarkedElements(componentType);
        if (deleteMarkedElements.isRight()) {
            janusGraphDao.rollback();
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(deleteMarkedElements.right().value(), componentType));
            return Either.right(responseFormat);
        }
        log.trace("end deleteMarkedComponents");
        janusGraphDao.commit();
        return Either.left(deleteMarkedElements.left().value());
    }

    public Either<List<ArtifactDefinition>, StorageOperationStatus> getComponentArtifactsForDelete(String parentId, NodeTypeEnum parentType) {
        List<ArtifactDefinition> artifacts = new ArrayList<>();
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifactsResponse = artifactToscaOperation.getArtifacts(parentId);
        if (artifactsResponse.isRight()) {
            if (artifactsResponse.right().value() != StorageOperationStatus.NOT_FOUND) {
                log.debug("failed to retrieve artifacts for {} {}", parentType, parentId);
                return Either.right(artifactsResponse.right().value());
            }
        } else {
            artifacts.addAll(artifactsResponse.left().value().values());
        }
        return Either.left(artifacts);
    }

    /**
     * @param componentId
     * @param user
     * @param dataParamsToReturn - ui list of params to return
     * @return
     */
    public Either<UiComponentDataTransfer, ResponseFormat> getComponentDataFilteredByParams(String componentId, User user,
                                                                                            List<String> dataParamsToReturn) {
        if (user != null) {
            validateUserExists(user);
        }
        UiComponentDataTransfer result = new UiComponentDataTransfer();
        if (dataParamsToReturn == null || dataParamsToReturn.isEmpty()) {
            Either.left(result);
        } else {
            Either<UiComponentDataTransfer, ResponseFormat> uiDataTransferEither = getUiComponentDataTransferByComponentId(componentId,
                dataParamsToReturn);
            if (uiDataTransferEither.isRight()) {
                return Either.right(uiDataTransferEither.right().value());
            }
            result = uiDataTransferEither.left().value();
        }
        return Either.left(result);
    }

    protected <T extends Component> void generateAndAddInputsFromGenericTypeProperties(T component, Resource genericType) {
        List<InputDefinition> genericAndComponentInputs = new ArrayList<>();
        List<InputDefinition> genericInputs = genericTypeBusinessLogic.generateInputsFromGenericTypeProperties(genericType);
        genericAndComponentInputs.addAll(genericInputs);
        if (null != component.getInputs()) {
            List<InputDefinition> nonGenericInputsFromComponent = getAllNonGenericInputsFromComponent(genericInputs, component.getInputs());
            genericAndComponentInputs.addAll(nonGenericInputsFromComponent);
        }
        component.setInputs(genericAndComponentInputs);
    }

    private List<InputDefinition> getAllNonGenericInputsFromComponent(List<InputDefinition> genericInputs, List<InputDefinition> componentInputs) {
        if (genericInputs == null) {
            return componentInputs;
        }
        Map<String, InputDefinition> inputByNameMap = MapUtil.toMap(genericInputs, InputDefinition::getName);
        List<InputDefinition> componentNonGenericInputs = new ArrayList<>();
        componentInputs.stream().forEach(input -> {
            if (!inputByNameMap.containsKey(input.getName())) {
                componentNonGenericInputs.add(input);
            }
        });
        return componentNonGenericInputs;
    }

    protected void generatePropertiesFromGenericType(final Component component, final Resource genericType) {
        if (CollectionUtils.isEmpty(genericType.getProperties())) {
            return;
        }
        final List<PropertyDefinition> genericTypePropertyList = genericType.getProperties().stream().map(PropertyDefinition::new)
            .peek(propertyDefinition -> propertyDefinition.setUniqueId(null)).collect(Collectors.toList());
        if (component.getProperties() == null) {
            component.setProperties(new ArrayList<>(genericTypePropertyList));
        } else {
            List<PropertyDefinition> servicePropertyList = component.getProperties();
            genericTypePropertyList.stream()
                .filter(property -> servicePropertyList.stream().noneMatch(property1 -> property1.getName().equals(property.getName())))
                .forEach(servicePropertyList::add);
        }
        component.getProperties().forEach(propertyDefinition -> propertyDefinition.setUniqueId(null));
    }

    protected <T extends Component> Resource fetchAndSetDerivedFromGenericType(final T component) {
        return fetchAndSetDerivedFromGenericType(component, null);
    }

    protected <T extends Component> Resource fetchAndSetDerivedFromGenericType(final T component, final String toscaType) {
        final Either<Resource, ResponseFormat> genericTypeEither = this.genericTypeBusinessLogic.fetchDerivedFromGenericType(component, toscaType);
        if (genericTypeEither.isRight()) {
            log.debug("Failed to fetch latest generic type for component {} of type", component.getName(), component.assetType());
            throw new ByActionStatusComponentException(ActionStatus.GENERIC_TYPE_NOT_FOUND, component.assetType());
        }
        final Resource genericTypeResource = genericTypeEither.left().value();
        component.setDerivedFromGenericInfo(genericTypeResource);
        return genericTypeResource;
    }

    public Either<Map<String, List<IComponentInstanceConnectedElement>>, ResponseFormat> getFilteredComponentInstanceProperties(String componentId,
                                                                                                                                Map<FilterKeyEnum, List<String>> filters,
                                                                                                                                String userId) {
        Either<Map<String, List<IComponentInstanceConnectedElement>>, ResponseFormat> response = null;
        Either<Component, StorageOperationStatus> getResourceRes = null;
        try {
            if (!filters.containsKey(FilterKeyEnum.NAME_FRAGMENT) && StringUtils.isEmpty(filters.get(FilterKeyEnum.NAME_FRAGMENT).get(0))) {
                response = Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
            }
            if (userId != null && response == null) {
                validateUserExists(userId);
            }
            if (response == null) {
                getResourceRes = toscaOperationFacade.getToscaElement(componentId);
                if (getResourceRes.isRight()) {
                    response = Either
                        .right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(getResourceRes.right().value())));
                }
            }
            if (response == null) {
                response = getFilteredComponentInstancesProperties(getResourceRes.left().value(), filters);
            }
        } catch (Exception e) {
            log.debug("The exception {} occured during filtered instance properties fetching. the  containing component is {}. ", e, componentId);
            response = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            if (response != null && response.isLeft()) {
                toscaOperationFacade.commit();
            } else {
                toscaOperationFacade.rollback();
            }
        }
        return response;
    }

    private Either<Map<String, List<IComponentInstanceConnectedElement>>, ResponseFormat> getFilteredComponentInstancesProperties(Component component,
                                                                                                                                  Map<FilterKeyEnum, List<String>> filters) {
        Map<String, List<IComponentInstanceConnectedElement>> filteredProperties = new HashMap<>();
        Either<Map<String, List<IComponentInstanceConnectedElement>>, ResponseFormat> result = Either.left(filteredProperties);
        List<ComponentInstance> filteredInstances = getFilteredInstances(component, filters.get(FilterKeyEnum.RESOURCE_TYPE));
        String propertyNameFragment = filters.get(FilterKeyEnum.NAME_FRAGMENT).get(0);
        boolean searchByFragment = propertyNameFragment.length() > 3;
        if (CollectionUtils.isNotEmpty(filteredInstances)) {
            for (ComponentInstance instance : filteredInstances) {
                if (component.getComponentInstancesProperties() != null && component.getComponentInstancesProperties()
                    .containsKey(instance.getUniqueId())) {
                    List<IComponentInstanceConnectedElement> currProperties = getFilteredComponentInstanceProperties(
                        component.getComponentInstancesProperties().get(instance.getUniqueId()), propertyNameFragment, searchByFragment);
                    setFilteredProperties(filteredProperties, instance, currProperties);
                }
                if (component.getComponentInstancesInputs() != null && component.getComponentInstancesInputs().containsKey(instance.getUniqueId())) {
                    List<IComponentInstanceConnectedElement> currInputs = getFilteredComponentInstanceInputs(
                        component.getComponentInstancesInputs().get(instance.getUniqueId()), propertyNameFragment, searchByFragment);
                    if (CollectionUtils.isNotEmpty(currInputs)) {
                        checkFilteredProperties(filteredProperties, instance, currInputs);
                    }
                }
            }
        }
        return result;
    }

    private void setFilteredProperties(Map<String, List<IComponentInstanceConnectedElement>> filteredProperties, ComponentInstance instance,
                                       List<IComponentInstanceConnectedElement> currProperties) {
        if (CollectionUtils.isNotEmpty(currProperties)) {
            filteredProperties.put(instance.getUniqueId(), currProperties);
        }
    }

    private void checkFilteredProperties(Map<String, List<IComponentInstanceConnectedElement>> filteredProperties, ComponentInstance instance,
                                         List<IComponentInstanceConnectedElement> currInputs) {
        if (filteredProperties.get(instance.getUniqueId()) != null) {
            filteredProperties.get(instance.getUniqueId()).addAll(currInputs);
        } else {
            filteredProperties.put(instance.getUniqueId(), currInputs);
        }
    }

    private List<IComponentInstanceConnectedElement> getFilteredComponentInstanceInputs(List<ComponentInstanceInput> inputs,
                                                                                        String propertyNameFragment, boolean searchByFragment) {
        return inputs.stream().filter(i -> isMatchingInput(i, propertyNameFragment, searchByFragment)).collect(Collectors.toList());
    }

    private List<IComponentInstanceConnectedElement> getFilteredComponentInstanceProperties(List<ComponentInstanceProperty> instanceProperties,
                                                                                            String propertyNameFragment, boolean searchByFragment) {
        return instanceProperties.stream().filter(p -> isMatchingProperty(p, propertyNameFragment, searchByFragment)).collect(Collectors.toList());
    }

    private boolean isMatchingInput(ComponentInstanceInput input, String propertyNameFragment, boolean searchByFragment) {
        boolean isMatching = false;
        if (searchByFragment && input.getName().toLowerCase().contains(propertyNameFragment)) {
            isMatching = true;
        }
        if (!searchByFragment && input.getName().equalsIgnoreCase(propertyNameFragment)) {
            isMatching = true;
        }
        return isMatching;
    }

    private boolean isMatchingProperty(ComponentInstanceProperty property, String propertyNameFragment, boolean searchByFragment) {
        boolean isMatching = false;
        if (searchByFragment && property.getName().toLowerCase().contains(propertyNameFragment)) {
            isMatching = true;
        }
        if (!searchByFragment && property.getName().equalsIgnoreCase(propertyNameFragment)) {
            isMatching = true;
        }
        if (!isMatching && !ToscaPropertyType.isPrimitiveType(property.getType())) {
            isMatching = isMatchingComplexPropertyByRecursively(property, propertyNameFragment, searchByFragment);
        }
        return isMatching;
    }

    private boolean isMatchingComplexPropertyByRecursively(PropertyDataDefinition property, String propertyNameFragment, boolean searchByFragment) {
        String propertyType;
        List<PropertyDefinition> dataTypeProperties;
        DataTypeDefinition currentProperty;
        if (searchByFragment && property.getName().toLowerCase().contains(propertyNameFragment.toLowerCase())) {
            return true;
        }
        if (!searchByFragment && property.getName().equalsIgnoreCase(propertyNameFragment)) {
            return true;
        }
        propertyType = isEmptyInnerType(property) ? property.getType() : property.getSchema().getProperty().getType();
        if (ToscaPropertyType.isScalarType(propertyType)) {
            return false;
        }
        Either<DataTypeDefinition, StorageOperationStatus> getDataTypeByNameRes = propertyOperation.getDataTypeByName(propertyType, null);
        if (getDataTypeByNameRes.isRight()) {
            return false;
        }
        currentProperty = getDataTypeByNameRes.left().value();
        dataTypeProperties = currentProperty.getProperties();
        boolean dataPropertiesNotNull = CollectionUtils.isNotEmpty(dataTypeProperties);
        BooleanSupplier dataMatchesComplexProperty = () -> isMatchingComplexProperty(propertyNameFragment, searchByFragment, dataTypeProperties);
        BooleanSupplier parentPropertiesNotNull = () -> CollectionUtils.isNotEmpty(currentProperty.getDerivedFrom().getProperties());
        BooleanSupplier parentDataMatchesComplexProperty = () -> isMatchingComplexProperty(propertyNameFragment, searchByFragment,
            currentProperty.getDerivedFrom().getProperties());
        return ((dataPropertiesNotNull && dataMatchesComplexProperty.getAsBoolean()) || (parentPropertiesNotNull.getAsBoolean()
            && parentDataMatchesComplexProperty.getAsBoolean()));
    }

    private boolean isMatchingComplexProperty(String propertyNameFragment, boolean searchByFragment, List<PropertyDefinition> dataTypeProperties) {
        for (PropertyDefinition prop : dataTypeProperties) {
            if (isMatchingComplexPropertyByRecursively(prop, propertyNameFragment, searchByFragment)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEmptyInnerType(PropertyDataDefinition property) {
        return property == null || property.getSchema() == null || property.getSchema().getProperty() == null
            || property.getSchema().getProperty().getType() == null;
    }

    public Either<Boolean, ResponseFormat> shouldUpgradeToLatestGeneric(Component clonedComponent) {
        if (!clonedComponent.deriveFromGeneric() || StringUtils.isNotEmpty(clonedComponent.getModel())) {
            return Either.left(false);
        }
        Boolean shouldUpgrade = false;
        String currentGenericType = clonedComponent.getDerivedFromGenericType();
        String currentGenericVersion = clonedComponent.getDerivedFromGenericVersion();
        Resource genericTypeResource = fetchAndSetDerivedFromGenericType(clonedComponent);
        if (null == currentGenericType || !currentGenericType.equals(genericTypeResource.getToscaResourceName()) || !currentGenericVersion
            .equals(genericTypeResource.getVersion())) {
            shouldUpgrade = upgradeToLatestGeneric(clonedComponent, genericTypeResource);
            if (!shouldUpgrade) {
                reverntUpdateOfGenericVersion(clonedComponent, currentGenericType, currentGenericVersion);
            }
        }
        return Either.left(shouldUpgrade);
    }

    private void reverntUpdateOfGenericVersion(Component clonedComponent, String currentGenericType, String currentGenericVersion) {
        clonedComponent.setDerivedFromGenericType(currentGenericType);
        clonedComponent.setDerivedFromGenericVersion(currentGenericVersion);
    }

    private <T extends ToscaDataDefinition> Either<Map<String, T>, String> validateNoConflictingProperties(List<T> currentList,
                                                                                                           List<T> upgradedList) {
        Map<String, T> currentMap = ToscaDataDefinition.listToMapByName(currentList);
        Map<String, T> upgradedMap = ToscaDataDefinition.listToMapByName(upgradedList);
        return ToscaDataDefinition.mergeDataMaps(upgradedMap, currentMap, true);
    }

    private boolean shouldUpgradeNodeType(Component componentToCheckOut, Resource latestGeneric) {
        List<PropertyDefinition> genericTypeProps = latestGeneric.getProperties();
        Either<Map<String, PropertyDefinition>, String> validPropertiesMerge = validateNoConflictingProperties(genericTypeProps,
            ((Resource) componentToCheckOut).getProperties());
        if (validPropertiesMerge.isRight()) {
            if (log.isDebugEnabled()) {
                log.debug("property {} cannot be overriden, check out performed without upgrading to latest generic",
                    validPropertiesMerge.right().value());
            }
            return false;
        }
        List<AttributeDefinition> genericTypeAttributes = latestGeneric.getAttributes();
        final Either<Map<String, AttributeDefinition>, String> validAttributesMerge = validateNoConflictingProperties(genericTypeAttributes,
            ((Resource) componentToCheckOut).getAttributes());
        if (validAttributesMerge.isRight()) {
            if (log.isDebugEnabled()) {
                log.debug("attribute {} cannot be overriden, check out performed without upgrading to latest generic",
                    validAttributesMerge.right().value());
            }
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
        List<InputDefinition> genericTypeInputs = null == genericTypeProps ? null
            : genericTypeBusinessLogic.convertGenericTypePropertiesToInputsDefintion(genericTypeProps, latestGeneric.getUniqueId());
        List<InputDefinition> currentList = new ArrayList<>();
        // nullify existing ownerId from existing list and merge into updated list
        if (null != componentToCheckOut.getInputs()) {
            for (InputDefinition input : componentToCheckOut.getInputs()) {
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
            if (log.isDebugEnabled()) {
                log.debug("input {} cannot be overriden, check out performed without upgrading to latest generic", eitherMerged.right().value());
            }
            return false;
        }
        componentToCheckOut.setInputs(new ArrayList<>(eitherMerged.left().value().values()));
        return true;
    }

    private List<ComponentInstance> getFilteredInstances(Component component, List<String> resourceTypes) {
        List<ComponentInstance> filteredInstances = null;
        if (CollectionUtils.isEmpty(resourceTypes)) {
            filteredInstances = component.getComponentInstances();
        } else if (CollectionUtils.isNotEmpty(component.getComponentInstances())) {
            filteredInstances = component.getComponentInstances().stream().filter(i -> isMatchingType(i.getOriginType(), resourceTypes))
                .collect(Collectors.toList());
        }
        if (filteredInstances == null) {
            filteredInstances = new ArrayList<>();
        }
        return filteredInstances;
    }

    private boolean isMatchingType(OriginTypeEnum originType, List<String> resourceTypes) {
        boolean isMatchingType = false;
        for (String resourceType : resourceTypes) {
            if (originType == OriginTypeEnum.findByValue(resourceType.toUpperCase())) {
                isMatchingType = true;
                break;
            }
        }
        return isMatchingType;
    }

    public Either<Component, ActionStatus> shouldUpgradeToLatestDerived(Component clonedComponent) {
        //general implementation. Must be error for service, VF . In ResourceBuisnessLogic exist override
        return Either.right(ActionStatus.GENERAL_ERROR);
    }

    protected Either<Component, ResponseFormat> updateCatalog(Component component, ChangeTypeEnum changeStatus) {
        if (log.isDebugEnabled()) {
            log.debug("update Catalog start with Component Type {} And Componet Name {} with change status {}",
                component.getComponentType().name(), component.getName(), changeStatus.name());
        }
        ActionStatus status = catalogOperations.updateCatalog(changeStatus, component);
        if (status != ActionStatus.OK) {
            return Either.right(componentsUtils.getResponseFormat(status));
        }
        return Either.left(component);
    }

    public CatalogOperation getCatalogOperations() {
        return catalogOperations;
    }

    @Autowired
    public void setCatalogOperations(CatalogOperation catalogOperations) {
        this.catalogOperations = catalogOperations;
    }

    public List<GroupDefinition> throwComponentException(ResponseFormat responseFormat) {
        throw new ByResponseFormatComponentException(responseFormat);
    }
}
