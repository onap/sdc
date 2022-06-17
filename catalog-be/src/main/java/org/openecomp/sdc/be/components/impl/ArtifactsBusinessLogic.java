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
 * Modifications copyright (c) 2020 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.components.impl;

import static org.openecomp.sdc.be.dao.api.ActionStatus.MISMATCH_BETWEEN_ARTIFACT_TYPE_AND_COMPONENT_TYPE;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fj.data.Either;
import io.vavr.control.Option;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.ArtifactsResolver;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.artifact.ArtifactOperationInfo;
import org.openecomp.sdc.be.components.impl.artifact.ArtifactTypeToPayloadTypeSelector;
import org.openecomp.sdc.be.components.impl.artifact.PayloadTypeEnum;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.utils.ComponentUtils;
import org.openecomp.sdc.be.components.impl.validation.PMDictionaryValidator;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction.LifecycleChanceActionEnum;
import org.openecomp.sdc.be.components.utils.ArtifactUtils;
import org.openecomp.sdc.be.components.utils.InterfaceOperationUtils;
import org.openecomp.sdc.be.config.ArtifactConfiguration;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.Configuration.ArtifactTypeConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.HeatParameterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.info.ArtifactTemplateInfo;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ArtifactTypeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.heat.HeatParameterType;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.IHeatParametersOperation;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ArtifactTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.operations.impl.UserAdminOperation;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.DAOArtifactData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.common.util.YamlToObjectConverter;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.yaml.snakeyaml.Yaml;

@org.springframework.stereotype.Component("artifactBusinessLogic")
public class ArtifactsBusinessLogic extends BaseBusinessLogic {

    public static final String HEAT_ENV_NAME = "heatEnv";
    public static final String HEAT_VF_ENV_NAME = "VfHeatEnv";
    public static final String HEAT_ENV_SUFFIX = "env";
    public static final String ARTIFACT_ACTION_LOCK = "Artifact action - lock ";
    public static final String FAILED_UPLOAD_ARTIFACT_TO_COMPONENT = "Failed to upload artifact to component with type {} and uuid {}. Status is {}. ";
    public static final String COMPONENT_INSTANCE_NOT_FOUND = "Component instance {} was not found for component {}";
    private static final String RESOURCE_INSTANCE = "resource instance";
    private static final String ARTIFACT_TYPE_OTHER = "OTHER";
    private static final String ARTIFACT_DESCRIPTION = "artifact description";
    private static final String ARTIFACT_LABEL = "artifact label";
    private static final String ARTIFACT_URL = "artifact url";
    private static final String ARTIFACT_NAME = "artifact name";
    private static final String ARTIFACT_PAYLOAD = "artifact payload";
    private static final String ARTIFACT_PLACEHOLDER_TYPE = "type";
    private static final String ARTIFACT_PLACEHOLDER_DISPLAY_NAME = "displayName";
    private static final Object ARTIFACT_PLACEHOLDER_DESCRIPTION = "description";
    private static final String ARTIFACT_PLACEHOLDER_FILE_EXTENSION = "fileExtension";
    private static final Logger log = Logger.getLogger(ArtifactsBusinessLogic.class.getName());
    private static final String FAILED_UPDATE_GROUPS = "Failed to update groups of the component {}. ";
    private static final String FAILED_SAVE_ARTIFACT = "Failed to save the artifact.";
    private static final String FAILED_FETCH_COMPONENT = "Could not fetch component with type {} and uuid {}. Status is {}. ";
    private static final String NULL_PARAMETER = "One of the function parameteres is null";
    private static final String ROLLBACK = "all changes rollback";
    private static final String COMMIT = "all changes committed";
    private static final String UPDATE_ARTIFACT = "Update Artifact";
    private static final String FOUND_DEPLOYMENT_ARTIFACT = "Found deployment artifact {}";
    private static final String VALID_ARTIFACT_LABEL_NAME = "'A-Z', 'a-z', '0-9', '-', '@', '+' and space.";
    private final ArtifactTypeOperation artifactTypeOperation;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    @javax.annotation.Resource
    private IInterfaceLifecycleOperation interfaceLifecycleOperation;
    @javax.annotation.Resource
    private UserAdminOperation userOperaton;
    @javax.annotation.Resource
    private IElementOperation elementOperation;
    @javax.annotation.Resource
    private IHeatParametersOperation heatParametersOperation;
    private ArtifactCassandraDao artifactCassandraDao;
    private ToscaExportHandler toscaExportUtils;
    private CsarUtils csarUtils;
    private LifecycleBusinessLogic lifecycleBusinessLogic;
    private UserBusinessLogic userBusinessLogic;
    private ArtifactsResolver artifactsResolver;
    private NodeTemplateOperation nodeTemplateOperation;

    @Autowired
    public ArtifactsBusinessLogic(ArtifactCassandraDao artifactCassandraDao, ToscaExportHandler toscaExportUtils, CsarUtils csarUtils,
                                  LifecycleBusinessLogic lifecycleBusinessLogic, UserBusinessLogic userBusinessLogic,
                                  ArtifactsResolver artifactsResolver, IElementOperation elementDao, IGroupOperation groupOperation,
                                  IGroupInstanceOperation groupInstanceOperation, IGroupTypeOperation groupTypeOperation,
                                  InterfaceOperation interfaceOperation, InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
                                  ArtifactsOperations artifactToscaOperation,
                                  ArtifactTypeOperation artifactTypeOperation) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation,
            artifactToscaOperation);
        this.artifactCassandraDao = artifactCassandraDao;
        this.toscaExportUtils = toscaExportUtils;
        this.csarUtils = csarUtils;
        this.lifecycleBusinessLogic = lifecycleBusinessLogic;
        this.userBusinessLogic = userBusinessLogic;
        this.artifactsResolver = artifactsResolver;
        this.artifactTypeOperation = artifactTypeOperation;
    }

    public static <R> Either<Boolean, R> ifTrue(boolean predicate, Supplier<Either<Boolean, R>> ifTrue) {
        return predicate ? ifTrue.get() : Either.left(false);
    }

    public static <L, R> Either<L, R> forEach(Either<L, R> e, Consumer<L> c) {
        return e.left().map(l -> {
            c.accept(l);
            return l;
        });
    }

    private static Option<ComponentInstance> findFirstMatching(Component component, Predicate<ComponentInstance> filter) {
        return Option.ofOptional(component.getComponentInstances().stream().filter(filter).findFirst());
    }

    // new flow US556184
    public Either<ArtifactDefinition, Operation> handleArtifactRequest(String componentId, String userId, ComponentTypeEnum componentType,
                                                                       ArtifactOperationInfo operation, String artifactId,
                                                                       ArtifactDefinition artifactInfo, String origMd5, String originData,
                                                                       String interfaceName, String operationName, String parentId,
                                                                       String containerComponentType, boolean shouldLock, boolean inTransaction) {
        // step 1 - detect auditing type
        AuditingActionEnum auditingAction = detectAuditingType(operation, origMd5);
        // step 2 - check header
        if (userId == null) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION);
            log.debug("handleArtifactRequest - no HTTP_CSP_HEADER , component id {}", componentId);
            handleAuditing(auditingAction, null, componentId, null, null, null, artifactId, responseFormat, componentType, null);
            throw new ByActionStatusComponentException(ActionStatus.MISSING_INFORMATION);
        }
        // step 3 - check user existence

        // step 4 - check user's role
        User user = validateUserExists(userId, auditingAction, componentId, artifactId, componentType, inTransaction);
        validateUserRole(user, auditingAction, componentId, artifactId, componentType, operation);
        // steps 5 - 6 - 7

        // 5. check service/resource existence

        // 6. check service/resource check out

        // 7. user is owner of checkout state
        Component component = null;
        String realComponentId = componentType == ComponentTypeEnum.RESOURCE_INSTANCE ? parentId : componentId;
        component = validateComponentExists(realComponentId, auditingAction, user, artifactId, componentType, containerComponentType);
        validateWorkOnComponent(component, userId, auditingAction, user, artifactId, operation);
        if (componentType == ComponentTypeEnum.RESOURCE_INSTANCE) {
            validateResourceInstanceById(component, componentId);
        }
        // step 8
        return validateAndHandleArtifact(componentId, componentType, operation, artifactId, artifactInfo, origMd5, originData, interfaceName,
            operationName, user, component, shouldLock, inTransaction, true);
    }

    public Either<ArtifactDefinition, Operation> handleArtifactRequest(String componentId, String userId, ComponentTypeEnum componentType,
                                                                       ArtifactOperationInfo operation, String artifactId,
                                                                       ArtifactDefinition artifactInfo, String origMd5, String originData,
                                                                       String interfaceName, String operationName, String parentId,
                                                                       String containerComponentType) {
        return handleArtifactRequest(componentId, userId, componentType, operation, artifactId, artifactInfo, origMd5, originData, interfaceName,
            operationName, parentId, containerComponentType, true, false);
    }

    /**
     * This Method validates only the Artifact and does not validate user / role / component ect...<br> For regular usage use <br> {@link
     * #handleArtifactRequest(String, String, ComponentTypeEnum, ArtifactOperationInfo, String, ArtifactDefinition, String, String, String, String,
     * String, String)}
     *
     * @return
     */
    public Either<ArtifactDefinition, Operation> validateAndHandleArtifact(String componentUniqueId, ComponentTypeEnum componentType,
                                                                           ArtifactOperationInfo operation, String artifactUniqueId,
                                                                           ArtifactDefinition artifactDefinition, String origMd5, String originData,
                                                                           String interfaceName, String operationName, User user, Component component,
                                                                           boolean shouldLock, boolean inTransaction, boolean needUpdateGroup) {
        AuditingActionEnum auditingAction = detectAuditingType(operation, origMd5);
        artifactDefinition = validateArtifact(componentUniqueId, componentType, operation, artifactUniqueId, artifactDefinition, auditingAction, user,
            component, shouldLock, inTransaction);
        // step 10
        Either<ArtifactDefinition, Operation> result = doAction(componentUniqueId, componentType, operation, artifactUniqueId, artifactDefinition,
            origMd5, originData, interfaceName, operationName, auditingAction, user, component, shouldLock, inTransaction, needUpdateGroup);
        //TODO: audit positive action
        return result;
    }

    @VisibleForTesting
    ArtifactDefinition validateArtifact(String componentId, ComponentTypeEnum componentType, ArtifactOperationInfo operation, String artifactId,
                                        ArtifactDefinition artifactInfo, AuditingActionEnum auditingAction, User user, Component component,
                                        boolean shouldLock, boolean inTransaction) {
        ArtifactDefinition artifactInfoToReturn = artifactInfo;
        ArtifactOperationEnum operationEnum = operation.getArtifactOperationEnum();
        if (operationEnum == ArtifactOperationEnum.UPDATE || operationEnum == ArtifactOperationEnum.DELETE
            || operationEnum == ArtifactOperationEnum.DOWNLOAD) {
            ArtifactDefinition dbArtifact = getArtifactIfBelongsToComponent(componentId, componentType, artifactId, component);
            if (operation.isDownload()) {
                artifactInfoToReturn = dbArtifact;
                handleHeatEnvDownload(componentId, componentType, user, component, dbArtifact, shouldLock, inTransaction);
            }
        }
        return artifactInfoToReturn;
    }

    @VisibleForTesting
    void handleHeatEnvDownload(String componentId, ComponentTypeEnum componentType, User user, Component component,
                               ArtifactDefinition artifactDefinition, boolean shouldLock, boolean inTransaction) {
        if (artifactDefinition.getArtifactType().equalsIgnoreCase(ArtifactTypeEnum.HEAT_ENV.getType()) && ComponentTypeEnum.SERVICE == component
            .getComponentType()) {
            ComponentInstance componentInstance = component.getComponentInstances().stream().filter(p -> p.getUniqueId().equals(componentId))
                .findAny().orElse(null);
            if (componentInstance == null) {
                throw new ByActionStatusComponentException(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, componentId, "instance", "Service",
                    component.getName());
            }
            Map<String, ArtifactDefinition> deploymentArtifacts = componentInstance.getDeploymentArtifacts();
            ArtifactDefinition heatEnvWithHeatParams = deploymentArtifacts.values().stream()
                .filter(p -> p.getUniqueId().equals(artifactDefinition.getUniqueId())).findAny().orElse(null);
            Either<ArtifactDefinition, ResponseFormat> eitherGenerated = generateHeatEnvArtifact(heatEnvWithHeatParams, componentType, component,
                componentInstance.getName(), user, componentId, shouldLock, inTransaction);
            if (eitherGenerated.isRight()) {
                throw new ByResponseFormatComponentException((eitherGenerated.right().value()));
            }
        }
    }

    private boolean artifactGenerationRequired(Component component, ArtifactDefinition artifactInfo) {
        boolean needGenerate;
        needGenerate = artifactInfo.getArtifactGroupType() == ArtifactGroupTypeEnum.TOSCA && (
            component.getLifecycleState() == LifecycleStateEnum.NOT_CERTIFIED_CHECKIN
                || component.getLifecycleState() == LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        needGenerate = needGenerate || (ComponentTypeEnum.RESOURCE == component.getComponentType() && (
            artifactInfo.getArtifactType().equalsIgnoreCase(ArtifactTypeEnum.HEAT_ENV.getType()) || isAbstractVfcEmptyCsar((Resource) component,
                artifactInfo)));
        return needGenerate;
    }

    private boolean isAbstractVfcEmptyCsar(Resource resource, ArtifactDefinition artifactInfo) {
        return resource.isAbstract() && artifactInfo.getArtifactGroupType() == ArtifactGroupTypeEnum.TOSCA && artifactInfo.getArtifactType()
            .equals(ArtifactTypeEnum.TOSCA_CSAR.getType()) && StringUtils.isEmpty(artifactInfo.getArtifactChecksum());
    }

    public Either<ArtifactDefinition, Operation> generateAndSaveToscaArtifact(ArtifactDefinition artifactDefinition, Component component, User user,
                                                                              boolean isInCertificationRequest, boolean shouldLock,
                                                                              boolean inTransaction, boolean fetchTemplatesFromDB) {
        return decodeToscaArtifactPayload(component, isInCertificationRequest, fetchTemplatesFromDB, artifactDefinition.getArtifactType()).left()
            .bind(payload -> {
                // TODO: Avoid output argument
                artifactDefinition.setPayload(payload);
                artifactDefinition.setEsId(artifactDefinition.getUniqueId());
                artifactDefinition.setArtifactChecksum(GeneralUtility.calculateMD5Base64EncodedByByteArray(payload));
                return lockComponentAndUpdateArtifact(component.getUniqueId(), artifactDefinition, AuditingActionEnum.ARTIFACT_PAYLOAD_UPDATE,
                    artifactDefinition.getUniqueId(), user, component.getComponentType(), component, payload, shouldLock, inTransaction);
            }).right().map(ex -> {
                // TODO: This should not be done but in order to keep this refactoring small enough, we stop here.

                // Bubble up this exception
                throw ex;
            });
    }

    private Either<byte[], ComponentException> decodeToscaArtifactPayload(Component parent, boolean isInCertificationRequest,
                                                                          boolean fetchTemplatesFromDB, String artifactType) {
        log.debug("tosca artifact generation");
        if (ArtifactTypeEnum.TOSCA_CSAR.getType().equals(artifactType)) {
            return csarUtils.createCsar(parent, fetchTemplatesFromDB, isInCertificationRequest).right().map(error -> {
                log.debug("Failed to generate tosca csar for component {} error {}", parent.getUniqueId(), error);
                return new ByResponseFormatComponentException(error);
            });
        } else {
            return toscaExportUtils.exportComponent(parent).left().map(toscaRepresentation -> {
                log.debug("Tosca yaml exported for component {} ", parent.getUniqueId());
                return toscaRepresentation.getMainYaml();
            }).right().map(toscaError -> {
                log.debug("Failed export tosca yaml for component {} error {}", parent.getUniqueId(), toscaError);
                return new ByActionStatusComponentException(componentsUtils.convertFromToscaError(toscaError));
            });
        }
    }

    private Either<ArtifactDefinition, Operation> doAction(String componentId, ComponentTypeEnum componentType, ArtifactOperationInfo operation,
                                                           String artifactId, ArtifactDefinition artifactInfo, String origMd5, String originData,
                                                           String interfaceName, String operationName, AuditingActionEnum auditingAction, User user,
                                                           Component parent, boolean shouldLock, boolean inTransaction, boolean needUpdateGroup) {
        if (interfaceName != null && operationName != null) {
            interfaceName = interfaceName.toLowerCase();
            operationName = operationName.toLowerCase();
        }
        if (shouldLock) {
            lockComponent(componentType, artifactId, auditingAction, user, parent);
        }
        Either<ArtifactDefinition, Operation> result;
        boolean operationSucceeded = false;
        try {
            switch (operation.getArtifactOperationEnum()) {
                case DOWNLOAD:
                    if (artifactGenerationRequired(parent, artifactInfo)) {
                        result = Either.left(generateNotSavedArtifact(parent, artifactInfo));
                    } else {
                        result = Either.left(handleDownload(componentId, artifactId, componentType, parent));
                    }
                    break;
                case DELETE:
                    result = Either.left(handleDeleteInternal(componentId, artifactId, componentType, parent));
                    break;
                case UPDATE:
                    result = handleUpdate(componentId, componentType, operation, artifactId, artifactInfo, null, origMd5, originData, interfaceName,
                        operationName, auditingAction, user, parent, needUpdateGroup);
                    break;
                case CREATE:
                    result = handleCreate(componentId, artifactInfo, operation, auditingAction, user, componentType, parent, origMd5, originData,
                        interfaceName, operationName);
                    break;
                case LINK:
                    result = Either.left(handleLink(componentId, artifactInfo, componentType, parent));
                    break;
                default:
                    throw new UnsupportedOperationException(
                        "In ArtifactsBusinessLogic received illegal operation: " + operation.getArtifactOperationEnum());
            }
            operationSucceeded = true;
            return result;
        } finally {
            handleLockingAndCommit(parent, shouldLock, inTransaction, operationSucceeded);
        }
    }

    private void lockComponent(ComponentTypeEnum componentType, String artifactId, AuditingActionEnum auditingAction, User user, Component parent) {
        try {
            lockComponent(parent, ARTIFACT_ACTION_LOCK);
        } catch (ComponentException e) {
            handleAuditing(auditingAction, parent, parent.getUniqueId(), user, null, null, artifactId, e.getResponseFormat(), componentType, null);
            throw e;
        }
    }

    @VisibleForTesting
    public Either<ArtifactDefinition, Operation> handleUpdate(String componentId, ComponentTypeEnum componentType, ArtifactOperationInfo operation,
                                                              String artifactId, ArtifactDefinition artifactInfo, byte[] decodedPayload,
                                                              String origMd5, String originData, String interfaceName, String operationName,
                                                              AuditingActionEnum auditingAction, User user, Component parent,
                                                              boolean needUpdateGroup) {
        Either<ArtifactDefinition, Operation> result;
        validateArtifactType(artifactInfo);
        final String artifactType = artifactInfo.getArtifactType();
        if (componentType == ComponentTypeEnum.RESOURCE_INSTANCE && (ArtifactTypeEnum.HEAT.getType().equals(artifactType) || ArtifactTypeEnum.HEAT_VOL
            .getType().equals(artifactType) || ArtifactTypeEnum.HEAT_NET.getType().equals(artifactType) || ArtifactTypeEnum.HEAT_ENV.getType()
            .equals(artifactType))) {
            result = handleUpdateHeatEnvAndHeatMeta(componentId, artifactInfo, auditingAction, artifactId, user, componentType, parent, originData,
                origMd5, operation);
            if (needUpdateGroup) {
                ActionStatus error = updateGroupInstance(artifactInfo, result.left().value(), parent, componentId);
                if (error != ActionStatus.OK) {
                    throw new ByActionStatusComponentException(error);
                }
            }
        } else if (componentType == ComponentTypeEnum.RESOURCE && ArtifactTypeEnum.HEAT_ENV.getType().equals(artifactType)) {
            result = handleUpdateHeatWithHeatEnvParams(componentId, artifactInfo, auditingAction, componentType, parent, originData, origMd5,
                operation, needUpdateGroup);
        } else {
            if (decodedPayload == null) {
                decodedPayload = validateInput(componentId, artifactInfo, operation, auditingAction, artifactId, user, componentType, parent, origMd5,
                    originData, interfaceName, operationName);
            }
            result = updateArtifactFlow(parent, componentId, artifactId, artifactInfo, decodedPayload, componentType, auditingAction);
            if (needUpdateGroup && result.isLeft()) {
                ArtifactDefinition updatedArtifact = result.left().value();
                updateGroupForHeat(artifactInfo, updatedArtifact, parent);
            }
        }
        return result;
    }

    private void validateArtifactType(final ArtifactDefinition artifactInfo) {
        if (!isArtifactSupported(artifactInfo.getArtifactType())) {
            throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED, artifactInfo.getArtifactType());
        }
    }

    private void validateArtifactType(final ArtifactDefinition artifactInfo, final ComponentTypeEnum componentType) {
        final ArtifactConfiguration artifactConfiguration = loadArtifactTypeConfig(artifactInfo.getArtifactType()).orElse(null);
        if (artifactConfiguration == null) {
            BeEcompErrorManager.getInstance().logBeMissingArtifactInformationError("Artifact Update / Upload", "artifactLabel");
            log.debug("Missing artifact type for artifact {}", artifactInfo.getArtifactName());
            final ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_ARTIFACT_TYPE);
            throw new ByResponseFormatComponentException(responseFormat);
        }
        final ArtifactGroupTypeEnum artifactGroupType = artifactInfo.getArtifactGroupType();
        try {
            validateArtifactType(componentType, artifactGroupType, artifactConfiguration);
        } catch (final ComponentException e) {
            log.debug("Artifact is invalid", e);
            BeEcompErrorManager.getInstance()
                .logBeInvalidTypeError("Artifact Upload / Delete / Update - Not supported artifact type", artifactInfo.getArtifactType(),
                    "Artifact " + artifactInfo.getArtifactName());
            log.debug("Not supported artifact type = {}", artifactInfo.getArtifactType());
            final ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED, artifactInfo.getArtifactType());
            throw new ByResponseFormatComponentException(responseFormat);
        }
    }

    private void validateArtifactType(final ComponentTypeEnum componentType, final ArtifactGroupTypeEnum groupType,
                                      final ArtifactConfiguration artifactConfiguration) {
        final boolean supportComponentType =
            CollectionUtils.isNotEmpty(artifactConfiguration.getComponentTypes()) && artifactConfiguration.getComponentTypes().stream()
                .anyMatch(componentType1 -> componentType1.getValue().equalsIgnoreCase(componentType.getValue()));
        if (!supportComponentType) {
            log.debug("Artifact Type '{}' not supported for Component Type '{}'", artifactConfiguration.getType(), componentType.getValue());
            throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED, artifactConfiguration.getType());
        }
        final boolean supportResourceType = artifactConfiguration.hasSupport(groupType);
        if (!supportResourceType) {
            log.debug("Artifact Type '{}' not supported for Component Type '{}' and Category '{}'", artifactConfiguration.getType(),
                componentType.getValue(), groupType.getType());
            throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED, artifactConfiguration.getType());
        }
    }

    private boolean isArtifactSupported(final String artifactType) {
        final Configuration configuration = ConfigurationManager.getConfigurationManager().getConfiguration();
        final List<ArtifactConfiguration> artifactConfigurationList = configuration.getArtifacts();
        if (CollectionUtils.isEmpty(artifactConfigurationList)) {
            return false;
        }
        return artifactConfigurationList.stream().anyMatch(artifactConfiguration -> artifactConfiguration.getType().equalsIgnoreCase(artifactType));
    }

    @VisibleForTesting
    public ActionStatus updateGroupForHeat(ArtifactDefinition artifactInfo, ArtifactDefinition artAfterUpdate, Component parent) {
        List<GroupDefinition> groups = parent.getGroups();
        if (groups != null && !groups.isEmpty()) {
            List<GroupDataDefinition> groupToUpdate = groups.stream()
                .filter(g -> g.getArtifacts() != null && g.getArtifacts().contains(artifactInfo.getUniqueId())).collect(Collectors.toList());
            if (groupToUpdate != null && !groupToUpdate.isEmpty()) {
                groupToUpdate.forEach(g -> {
                    g.getArtifacts().remove(artifactInfo.getUniqueId());
                    g.getArtifactsUuid().remove(artifactInfo.getArtifactUUID());
                    g.getArtifacts().add(artAfterUpdate.getUniqueId());
                    g.getArtifactsUuid().add(artAfterUpdate.getArtifactUUID());
                    if (!artifactInfo.getArtifactUUID().equals(artAfterUpdate.getArtifactUUID())) {
                        g.setGroupUUID(UniqueIdBuilder.generateUUID());
                    }
                });
                Either<List<GroupDefinition>, StorageOperationStatus> status = toscaOperationFacade.updateGroupsOnComponent(parent, groupToUpdate);
                if (status.isRight()) {
                    log.debug(FAILED_UPDATE_GROUPS, parent.getUniqueId());
                    throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(status.right().value()));
                }
            }
        }
        return ActionStatus.OK;
    }

    @VisibleForTesting
    ActionStatus updateGroupForHeat(ArtifactDefinition artifactInfoHeat, ArtifactDefinition artHeatAfterUpdate, ArtifactDefinition artifactInfoHeatE,
                                    ArtifactDefinition artHEAfterUpdate, Component parent) {
        List<GroupDefinition> groups = parent.getGroups();
        if (groups != null && !groups.isEmpty()) {
            List<GroupDataDefinition> groupToUpdate = groups.stream()
                .filter(g -> g.getArtifacts() != null && g.getArtifacts().contains(artifactInfoHeat.getUniqueId())).collect(Collectors.toList());
            if (groupToUpdate != null && !groupToUpdate.isEmpty()) {
                groupToUpdate.forEach(g -> {
                    g.getArtifacts().remove(artifactInfoHeat.getUniqueId());
                    g.getArtifactsUuid().remove(artifactInfoHeat.getArtifactUUID());
                    g.getArtifacts().remove(artifactInfoHeatE.getUniqueId());
                    g.getArtifacts().add(artHeatAfterUpdate.getUniqueId());
                    g.getArtifactsUuid().add(artHeatAfterUpdate.getArtifactUUID());
                    g.getArtifacts().add(artHEAfterUpdate.getUniqueId());
                });
                Either<List<GroupDefinition>, StorageOperationStatus> status = toscaOperationFacade.updateGroupsOnComponent(parent, groupToUpdate);
                if (status.isRight()) {
                    log.debug(FAILED_UPDATE_GROUPS, parent.getUniqueId());
                    return componentsUtils.convertFromStorageResponse(status.right().value());
                }
            }
        }
        return ActionStatus.OK;
    }

    private ActionStatus updateGroupInstance(ArtifactDefinition artifactInfo, ArtifactDefinition artAfterUpdate, Component parent, String parentId) {
        List<GroupInstance> updatedGroupInstances = new ArrayList<>();
        List<GroupInstance> groupInstances = null;
        Optional<ComponentInstance> componentInstOp = parent.getComponentInstances().stream().filter(ci -> ci.getUniqueId().equals(parentId))
            .findFirst();
        if (componentInstOp.isPresent()) {
            groupInstances = componentInstOp.get().getGroupInstances();
        }
        if (CollectionUtils.isNotEmpty(groupInstances)) {
            boolean isUpdated = false;
            for (GroupInstance groupInstance : groupInstances) {
                isUpdated = false;
                if (CollectionUtils.isNotEmpty(groupInstance.getGroupInstanceArtifacts()) && groupInstance.getGroupInstanceArtifacts()
                    .contains(artifactInfo.getUniqueId())) {
                    groupInstance.getGroupInstanceArtifacts().remove(artifactInfo.getUniqueId());
                    groupInstance.getGroupInstanceArtifacts().add(artAfterUpdate.getUniqueId());
                    isUpdated = true;
                }
                if (CollectionUtils.isNotEmpty(groupInstance.getGroupInstanceArtifactsUuid()) && groupInstance.getGroupInstanceArtifactsUuid()
                    .contains(artifactInfo.getArtifactUUID())) {
                    groupInstance.getGroupInstanceArtifactsUuid().remove(artifactInfo.getArtifactUUID());
                    groupInstance.getGroupInstanceArtifacts().add(artAfterUpdate.getArtifactUUID());
                    isUpdated = true;
                }
                if (isUpdated) {
                    updatedGroupInstances.add(groupInstance);
                }
            }
        }
        Either<List<GroupInstance>, StorageOperationStatus> status = toscaOperationFacade
            .updateGroupInstancesOnComponent(parent, parentId, updatedGroupInstances);
        if (status.isRight()) {
            log.debug(FAILED_UPDATE_GROUPS, parent.getUniqueId());
            return componentsUtils.convertFromStorageResponse(status.right().value());
        }
        return ActionStatus.OK;
    }

    ArtifactDefinition generateNotSavedArtifact(Component parent, ArtifactDefinition artifactDefinition) {
        if (artifactDefinition.getArtifactGroupType() == ArtifactGroupTypeEnum.TOSCA) {
            Either<byte[], ComponentException> decodedPayload = decodeToscaArtifactPayload(parent, false, false,
                artifactDefinition.getArtifactType());
            // TODO: This should not be done, but in order to keep this refactoring relatively small, we stop here
            if (decodedPayload.isRight()) {
                throw decodedPayload.right().value();
            } else {
                artifactDefinition.setPayload(decodedPayload.left().value());
                return artifactDefinition;
            }
        } else {
            String heatArtifactId = artifactDefinition.getGeneratedFromId();
            Either<ArtifactDefinition, StorageOperationStatus> heatRes = artifactToscaOperation.getArtifactById(parent.getUniqueId(), heatArtifactId);
            if (heatRes.isRight()) {
                log.debug("Failed to fetch heat artifact by generated id {} for heat env {}", heatArtifactId, artifactDefinition.getUniqueId());
                throw new StorageException(heatRes.right().value());
            }
            String generatedPayload = generateHeatEnvPayload(heatRes.left().value());
            artifactDefinition.setPayloadData(generatedPayload);
            return artifactDefinition;
        }
    }

    private Either<ArtifactDefinition, Operation> handleUpdateHeatWithHeatEnvParams(String componentId, ArtifactDefinition artifactInfo,
                                                                                    AuditingActionEnum auditingAction,
                                                                                    ComponentTypeEnum componentType, Component parent,
                                                                                    String originData, String origMd5,
                                                                                    ArtifactOperationInfo operation, boolean needToUpdateGroup) {
        Either<ArtifactDefinition, StorageOperationStatus> artifactHeatRes = artifactToscaOperation
            .getArtifactById(componentId, artifactInfo.getGeneratedFromId());
        ArtifactDefinition currHeatArtifact = artifactHeatRes.left().value();
        if (origMd5 != null) {
            validateMd5(origMd5, originData, artifactInfo.getPayloadData(), operation);
            if (ArrayUtils.isNotEmpty(artifactInfo.getPayloadData())) {
                handlePayload(artifactInfo, isArtifactMetadataUpdate(auditingAction));
            } else { // duplicate
                throw new ByActionStatusComponentException(ActionStatus.MISSING_DATA, ARTIFACT_PAYLOAD);
            }
        }
        return updateHeatParams(componentId, artifactInfo, auditingAction, parent, componentType, currHeatArtifact, needToUpdateGroup);
    }

    private void handleLockingAndCommit(Component parent, boolean shouldLock, boolean inTransaction, boolean actionSucceeded) {
        if (actionSucceeded) {
            log.debug(COMMIT);
            if (!inTransaction) {
                janusGraphDao.commit();
            }
        } else {
            log.debug(ROLLBACK);
            if (!inTransaction) {
                janusGraphDao.rollback();
            }
        }
        if (shouldLock) {
            graphLockOperation.unlockComponent(parent.getUniqueId(), parent.getComponentType().getNodeType());
        }
    }

    public ImmutablePair<String, byte[]> handleDownloadToscaModelRequest(Component component, ArtifactDefinition csarArtifact) {
        if (artifactGenerationRequired(component, csarArtifact)) {
            Either<byte[], ResponseFormat> generated = csarUtils.createCsar(component, false, false);
            if (generated.isRight()) {
                log.debug("Failed to export tosca csar for component {} error {}", component.getUniqueId(), generated.right().value());
                throw new ByResponseFormatComponentException(generated.right().value());
            }
            return new ImmutablePair<>(csarArtifact.getArtifactName(), generated.left().value());
        }
        return downloadArtifact(csarArtifact);
    }

    public ImmutablePair<String, byte[]> handleDownloadRequestById(String componentId, String artifactId, String userId,
                                                                   ComponentTypeEnum componentType, String parentId, String containerComponentType) {
        // perform all validation in common flow
        Either<ArtifactDefinition, Operation> result = handleArtifactRequest(componentId, userId, componentType,
            new ArtifactOperationInfo(false, false, ArtifactOperationEnum.DOWNLOAD), artifactId, null, null, null, null, null, parentId,
            containerComponentType);
        ArtifactDefinition artifactDefinition;
        Either<ArtifactDefinition, Operation> insideValue = result;
        if (insideValue.isLeft()) {
            artifactDefinition = insideValue.left().value();
        } else {
            artifactDefinition = insideValue.right().value().getImplementationArtifact();
        }
        // for tosca artifacts and heat env on VF level generated on download without saving
        if (artifactDefinition.getPayloadData() != null) {
            return (new ImmutablePair<>(artifactDefinition.getArtifactName(), artifactDefinition.getPayloadData()));
        }
        return downloadArtifact(artifactDefinition);
    }

    public Map<String, ArtifactDefinition> handleGetArtifactsByType(String containerComponentType, String parentId, ComponentTypeEnum componentType,
                                                                    String componentId, String artifactGroupType, String userId) {
        // step 1

        // detect auditing type
        Map<String, ArtifactDefinition> resMap = null;
        new Wrapper<>();
        // step 2

        // check header
        if (userId == null) {
            log.debug("handleGetArtifactsByType - no HTTP_CSP_HEADER , component id {}", componentId);
            throw new ByActionStatusComponentException(ActionStatus.MISSING_INFORMATION);
        }
        // step 3

        // check user existence

        // step 4

        // check user's role
        validateUserExists(userId);
        // steps 5 - 6 - 7

        // 5. check service/resource existence

        // 6. check service/resource check out

        // 7. user is owner of checkout state
        String realComponentId = componentType == ComponentTypeEnum.RESOURCE_INSTANCE ? parentId : componentId;
        ComponentParametersView componentFilter = new ComponentParametersView();
        componentFilter.disableAll();
        componentFilter.setIgnoreArtifacts(false);
        if (componentType == ComponentTypeEnum.RESOURCE_INSTANCE) {
            componentFilter.setIgnoreComponentInstances(false);
        }
        Component component = validateComponentExistsByFilter(realComponentId, ComponentTypeEnum.findByParamName(containerComponentType),
            componentFilter);
        lockComponent(component, ARTIFACT_ACTION_LOCK);
        boolean failed = false;
        try {
            ArtifactGroupTypeEnum groupType = ArtifactGroupTypeEnum.findType(artifactGroupType);
            if (groupType == null) {
                log.debug("handleGetArtifactsByType - not failed groupType {} , component id {}", artifactGroupType, componentId);
                throw new ByActionStatusComponentException(ActionStatus.MISSING_INFORMATION);
            }
            if (parentId == null && groupType == ArtifactGroupTypeEnum.DEPLOYMENT) {
                List<ArtifactDefinition> list = getDeploymentArtifacts(component, componentId);
                if (list != null && !list.isEmpty()) {
                    resMap = list.stream().collect(Collectors.toMap(ArtifactDataDefinition::getArtifactLabel, Function.identity()));
                } else {
                    resMap = new HashMap<>();
                }
                return resMap;
            } else {
                Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifactsMapStatus = getArtifacts(realComponentId,
                    componentType.getNodeType(), groupType, componentId);
                if (artifactsMapStatus.isRight()) {
                    if (artifactsMapStatus.right().value() != StorageOperationStatus.NOT_FOUND) {
                        log.debug("handleGetArtifactsByType - not failed groupType {} , component id {}", artifactGroupType, componentId);
                        throw new ByActionStatusComponentException(ActionStatus.MISSING_INFORMATION);
                    } else {
                        resMap = new HashMap<>();
                    }
                } else {
                    resMap = artifactsMapStatus.left().value();
                }
                return resMap;
            }
        } catch (ComponentException e) {
            failed = true;
            throw e;
        } finally {
            // unlock resource
            if (failed) {
                log.debug(ROLLBACK);
                janusGraphDao.rollback();
            } else {
                log.debug(COMMIT);
                janusGraphDao.commit();
            }
            componentType = component.getComponentType();
            NodeTypeEnum nodeType = componentType.getNodeType();
            graphLockOperation.unlockComponent(component.getUniqueId(), nodeType);
        }
    }

    private ArtifactDefinition getArtifactIfBelongsToComponent(String componentId, ComponentTypeEnum componentType, String artifactId,
                                                               Component component) {
        // check artifact existence
        Either<ArtifactDefinition, StorageOperationStatus> artifactResult = artifactToscaOperation
            .getArtifactById(componentId, artifactId, componentType, component.getUniqueId());
        if (artifactResult.isRight()) {
            throw new ByActionStatusComponentException(ActionStatus.COMPONENT_ARTIFACT_NOT_FOUND, artifactId, componentId);
        }
        // verify artifact belongs to component
        boolean found;
        switch (componentType) {
            case RESOURCE:
            case SERVICE:
                found = ComponentUtils.checkArtifactInComponent(component, artifactId);
                break;
            case RESOURCE_INSTANCE:
                found = ComponentUtils.checkArtifactInResourceInstance(component, componentId, artifactId);
                break;
            default:
                found = false;
        }
        if (!found) {
            throw new ByActionStatusComponentException(ActionStatus.COMPONENT_ARTIFACT_NOT_FOUND, artifactId, componentType.name().toLowerCase());
        }
        return artifactResult.left().value();
    }

    private Either<ArtifactDefinition, Operation> handleCreate(String componentId, ArtifactDefinition artifactInfo, ArtifactOperationInfo operation,
                                                               AuditingActionEnum auditingAction, User user, ComponentTypeEnum componentType,
                                                               Component parent, String origMd5, String originData, String interfaceType,
                                                               String operationName) {
        byte[] decodedPayload = validateInput(componentId, artifactInfo, operation, auditingAction, null, user, componentType, parent, origMd5,
            originData, interfaceType, operationName);
        return createArtifact(parent, componentId, artifactInfo, decodedPayload, componentType, auditingAction, interfaceType, operationName);
    }

    private ArtifactDefinition handleLink(String componentId, ArtifactDefinition artifactInfo, ComponentTypeEnum componentType, Component parent) {
        ComponentInstance foundInstance = findComponentInstance(componentId, parent);
        String instanceId = null;
        if (foundInstance != null) {
            instanceId = foundInstance.getUniqueId();
        }
        NodeTypeEnum nodeType = convertParentType(componentType);
        Either<ArtifactDefinition, StorageOperationStatus> artifactDefinitionEither = artifactToscaOperation
            .addArtifactToComponent(artifactInfo, parent, nodeType, true, instanceId);
        if (artifactDefinitionEither.isRight()) {
            throw new StorageException(artifactDefinitionEither.right().value(), artifactInfo.getArtifactDisplayName());
        }
        if (generateCustomizationUUIDOnInstance(parent.getUniqueId(), componentId, componentType) != StorageOperationStatus.OK) {
            throw new StorageException(artifactDefinitionEither.right().value(), artifactInfo.getArtifactDisplayName());
        }
        return artifactDefinitionEither.left().value();
    }

    private <T> Either<ArtifactDefinition, T> lockComponentAndUpdateArtifact(String parentId, ArtifactDefinition artifactInfo,
                                                                             AuditingActionEnum auditingAction, String artifactId, User user,
                                                                             ComponentTypeEnum componentType, Component parent, byte[] decodedPayload,
                                                                             boolean shouldLock, boolean inTransaction) {
        boolean failed = false;
        boolean writeAudit = true;
        try {
            lockComponent(parent, shouldLock, ARTIFACT_ACTION_LOCK);
            writeAudit = false;
            return updateArtifactFlow(parent, parentId, artifactId, artifactInfo, decodedPayload, componentType, auditingAction);
        } catch (ComponentException ce) {
            if (writeAudit) {
                handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, ce.getResponseFormat(), componentType, null);
            }
            failed = true;
            throw ce;
        } catch (StorageException se) {
            //TODO: audit
            failed = true;
            throw se;
        } finally {
            if (shouldLock) {
                unlockComponent(failed, parent, inTransaction);
            }
        }
    }

    private byte[] validateInput(String componentId, ArtifactDefinition artifactInfo, ArtifactOperationInfo operation,
                                 AuditingActionEnum auditingAction, String artifactId, User user, ComponentTypeEnum componentType, Component parent,
                                 String origMd5, String originData, String interfaceType, String operationName) {
        validateMd5(origMd5, originData, artifactInfo.getPayloadData(), operation);
        return getValidPayload(componentId, artifactInfo, operation, auditingAction, artifactId, user, componentType, parent, interfaceType,
            operationName);
    }

    private byte[] getValidPayload(String componentId, ArtifactDefinition artifactInfo, ArtifactOperationInfo operation,
                                   AuditingActionEnum auditingAction, String artifactId, User user, ComponentTypeEnum componentType, Component parent,
                                   String interfaceType, String operationName) {
        // step 11
        Either<ArtifactDefinition, ResponseFormat> validateResult = validateInput(componentId, artifactInfo, operation, artifactId, user,
            interfaceType, operationName, componentType, parent);
        if (validateResult.isRight()) {
            ResponseFormat responseFormat = validateResult.right().value();
            handleAuditing(auditingAction, parent, componentId, user, null, null, artifactId, responseFormat, componentType, null);
            throw new ByResponseFormatComponentException(responseFormat);
        }
        Either<byte[], ResponseFormat> payloadEither = handlePayload(artifactInfo, isArtifactMetadataUpdate(auditingAction));
        if (payloadEither.isRight()) {
            ResponseFormat responseFormat = payloadEither.right().value();
            handleAuditing(auditingAction, parent, componentId, user, null, null, artifactId, responseFormat, componentType, null);
            log.debug("Error during handle payload");
            throw new ByResponseFormatComponentException(responseFormat);
        }
        // validate heat parameters. this part must be after the parameters are

        // extracted in "handlePayload"
        Either<ArtifactDefinition, ResponseFormat> validateAndConvertHeatParameters = validateAndConvertHeatParameters(artifactInfo,
            artifactInfo.getArtifactType());
        if (validateAndConvertHeatParameters.isRight()) {
            ResponseFormat responseFormat = validateAndConvertHeatParameters.right().value();
            handleAuditing(auditingAction, parent, componentId, user, artifactInfo, null, artifactId, responseFormat, componentType, null);
            log.debug("Error during handle payload");
            throw new ByResponseFormatComponentException(responseFormat);
        }
        return payloadEither.left().value();
    }

    public void handleAuditing(AuditingActionEnum auditingActionEnum, Component component, String componentId, User user,
                               ArtifactDefinition artifactDefinition, String prevArtifactUuid, String currentArtifactUuid,
                               ResponseFormat responseFormat, ComponentTypeEnum componentTypeEnum, String resourceInstanceName) {
        if (componentsUtils.isExternalApiEvent(auditingActionEnum)) {
            return;
        }
        if (user == null) {
            user = new User();
            user.setUserId("UNKNOWN");
        }
        handleInternalAuditEvent(auditingActionEnum, component, componentId, user, artifactDefinition, prevArtifactUuid, currentArtifactUuid,
            responseFormat, componentTypeEnum, resourceInstanceName);
    }

    private void handleInternalAuditEvent(AuditingActionEnum auditingActionEnum, Component component, String componentId, User user,
                                          ArtifactDefinition artifactDefinition, String prevArtifactUuid, String currentArtifactUuid,
                                          ResponseFormat responseFormat, ComponentTypeEnum componentTypeEnum, String resourceInstanceName) {
        switch (componentTypeEnum) {
            case RESOURCE:
                Resource resource = (Resource) component;
                if (resource == null) {
                    // In that case, component ID should be instead of name
                    resource = new Resource();
                    resource.setName(componentId);
                }
                componentsUtils.auditResource(responseFormat, user, resource, resource.getName(), auditingActionEnum,
                    ResourceVersionInfo.newBuilder().artifactUuid(prevArtifactUuid).build(), currentArtifactUuid, artifactDefinition);
                break;
            case SERVICE:
                Service service = (Service) component;
                if (service == null) {
                    // In that case, component ID should be instead of name
                    service = new Service();
                    service.setName(componentId);
                }
                componentsUtils
                    .auditComponent(responseFormat, user, service, auditingActionEnum, new ResourceCommonInfo(ComponentTypeEnum.SERVICE.getValue()),
                        ResourceVersionInfo.newBuilder().artifactUuid(prevArtifactUuid).build(),
                        ResourceVersionInfo.newBuilder().artifactUuid(currentArtifactUuid).build(), null, artifactDefinition, null);
                break;
            case RESOURCE_INSTANCE:
                if (resourceInstanceName == null) {
                    resourceInstanceName = getResourceInstanceNameFromComponent(component, componentId);
                }
                componentsUtils.auditComponent(responseFormat, user, component, auditingActionEnum,
                    new ResourceCommonInfo(resourceInstanceName, ComponentTypeEnum.RESOURCE_INSTANCE.getValue()),
                    ResourceVersionInfo.newBuilder().artifactUuid(prevArtifactUuid).build(),
                    ResourceVersionInfo.newBuilder().artifactUuid(currentArtifactUuid).build(), null, artifactDefinition, null);
                break;
            default:
                break;
        }
    }

    private String getResourceInstanceNameFromComponent(Component component, String componentId) {
        ComponentInstance resourceInstance = component.getComponentInstances().stream().filter(p -> p.getUniqueId().equals(componentId)).findFirst()
            .orElse(null);
        String resourceInstanceName = null;
        if (resourceInstance != null) {
            resourceInstanceName = resourceInstance.getName();
        }
        return resourceInstanceName;
    }

    private void validateMd5(String origMd5, String originData, byte[] payload, ArtifactOperationInfo operation) {
        if (origMd5 == null) {
            if (operation.isCreateOrLink() && ArrayUtils.isNotEmpty(payload)) {
                log.debug("Missing md5 header during artifact create");
                throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_INVALID_MD5);
            }
            // Update metadata
            if (ArrayUtils.isNotEmpty(payload)) {
                log.debug("Cannot have payload while md5 header is missing");
                throw new ByActionStatusComponentException(ActionStatus.INVALID_CONTENT);
            }
        } else {
            String encodeBase64Str = GeneralUtility.calculateMD5Base64EncodedByString(originData);
            if (!encodeBase64Str.equals(origMd5)) {
                log.debug("The calculated md5 is different then the received one");
                throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_INVALID_MD5);
            }
        }
    }

    private Either<ArtifactDefinition, ResponseFormat> validateInput(final String componentId, final ArtifactDefinition artifactInfo,
                                                                     final ArtifactOperationInfo operation, final String artifactId, final User user,
                                                                     String interfaceName, String operationName,
                                                                     final ComponentTypeEnum componentType, final Component parentComponent) {
        final ArtifactDefinition existingArtifactInfo = findArtifact(parentComponent, componentType, componentId, operation, artifactId);
        final boolean isCreateOrLinkOperation = ArtifactOperationEnum.isCreateOrLink(operation.getArtifactOperationEnum());
        if (!isCreateOrLinkOperation && existingArtifactInfo == null) {
            throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_NOT_FOUND, artifactId);
        }
        final Component component;
        if (parentComponent.getUniqueId().equals(componentId)) {
            component = parentComponent;
        } else {
            final ComponentInstance componentInstance = findComponentInstance(componentId, parentComponent);
            component = findComponent(componentInstance.getComponentUid());
            component.setComponentType(componentType);
        }
        if (!isCreateOrLinkOperation) {
            ignoreUnupdateableFieldsInUpdate(operation, artifactInfo, existingArtifactInfo);
        }
        if (isInformationalArtifact(artifactInfo)) {
            validateInformationalArtifact(artifactInfo, component);
        }
        Either<Boolean, ResponseFormat> validateAndSetArtifactname = validateAndSetArtifactName(artifactInfo);
        if (validateAndSetArtifactname.isRight()) {
            return Either.right(validateAndSetArtifactname.right().value());
        }
        if (!validateArtifactNameUniqueness(componentId, parentComponent, artifactInfo, componentType)) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_EXIST));
        }
        if (operationName != null && interfaceName != null) {
            operationName = operationName.toLowerCase();
            interfaceName = interfaceName.toLowerCase();
        }
        Either<ActionStatus, ResponseFormat> logicalNameStatus = handleArtifactLabel(componentId, parentComponent, operation, artifactInfo,
            operationName, componentType);
        if (logicalNameStatus.isRight()) {
            return Either.right(logicalNameStatus.right().value());
        }
        // This is a patch to block possibility of updating service api fields

        // through other artifacts flow
        final ArtifactGroupTypeEnum artifactGroupType =
            operationName != null ? ArtifactGroupTypeEnum.LIFE_CYCLE : ArtifactGroupTypeEnum.INFORMATIONAL;
        if (operation.isNotCreateOrLink()) {
            checkAndSetUnUpdatableFields(user, artifactInfo, existingArtifactInfo, artifactGroupType);
        } else {
            checkCreateFields(user, artifactInfo, artifactGroupType);
        }
        composeArtifactId(componentId, artifactId, artifactInfo, interfaceName, operationName);
        if (existingArtifactInfo != null) {
            artifactInfo.setMandatory(existingArtifactInfo.getMandatory());
            if (operation.isNotCreateOrLink()) {
                validateArtifactTypeNotChanged(artifactInfo, existingArtifactInfo);
            }
        }
        // artifactGroupType is not allowed to be updated
        if (operation.isNotCreateOrLink()) {
            Either<ArtifactDefinition, ResponseFormat> validateGroupType = validateOrSetArtifactGroupType(artifactInfo, existingArtifactInfo);
            if (validateGroupType.isRight()) {
                return Either.right(validateGroupType.right().value());
            }
        }
        setArtifactTimeout(artifactInfo, existingArtifactInfo);
        if (isHeatArtifact(artifactInfo)) {
            validateHeatArtifact(parentComponent, componentId, artifactInfo);
        }
        if (isDeploymentArtifact(artifactInfo)) {
            if (componentType != ComponentTypeEnum.RESOURCE_INSTANCE) {
                final String artifactName = artifactInfo.getArtifactName();
                final String existingArtifactName = (existingArtifactInfo == null) ? null : existingArtifactInfo.getArtifactName();
                if (operation.isCreateOrLink() || ((artifactName != null) && !artifactName.equalsIgnoreCase(existingArtifactName))) {
                    validateSingleDeploymentArtifactName(artifactName, parentComponent);
                }
            }
            validateDeploymentArtifact(artifactInfo, component);
        }
        Either<Boolean, ResponseFormat> descriptionResult = validateAndCleanDescription(artifactInfo);
        if (descriptionResult.isRight()) {
            return Either.right(descriptionResult.right().value());
        }
        validateArtifactType(artifactInfo, component.getComponentType());
        artifactInfo.setArtifactType(artifactInfo.getArtifactType().toUpperCase());
        if (existingArtifactInfo != null && existingArtifactInfo.getArtifactGroupType() == ArtifactGroupTypeEnum.SERVICE_API) {
            // Change of type is not allowed and should be ignored
            artifactInfo.setArtifactType(ARTIFACT_TYPE_OTHER);
            Either<Boolean, ResponseFormat> validateUrl = validateAndServiceApiUrl(artifactInfo);
            if (validateUrl.isRight()) {
                return Either.right(validateUrl.right().value());
            }
            Either<Boolean, ResponseFormat> validateUpdate = validateFirstUpdateHasPayload(artifactInfo, existingArtifactInfo);
            if (validateUpdate.isRight()) {
                log.debug("serviceApi first update cnnot be without payload.");
                return Either.right(validateUpdate.right().value());
            }
        } else {
            if (artifactInfo.getApiUrl() != null) {
                artifactInfo.setApiUrl(null);
                log.error("Artifact URL cannot be set through this API - ignoring");
            }
            if (Boolean.TRUE.equals(artifactInfo.getServiceApi())) {
                artifactInfo.setServiceApi(false);
                log.error("Artifact service API flag cannot be changed - ignoring");
            }
        }
        return Either.left(artifactInfo);
    }

    private Component findComponent(final String componentId) {
        Either<? extends Component, StorageOperationStatus> component = toscaOperationFacade.getToscaFullElement(componentId);
        if (component.isRight()) {
            log.debug("Component '{}' not found ", componentId);
            throw new ByActionStatusComponentException(ActionStatus.COMPONENT_NOT_FOUND, componentId);
        }
        return component.left().value();
    }

    private void ignoreUnupdateableFieldsInUpdate(final ArtifactOperationInfo operation, final ArtifactDefinition artifactInfo,
                                                  final ArtifactDefinition currentArtifactInfo) {
        if (operation.isUpdate()) {
            artifactInfo.setArtifactType(currentArtifactInfo.getArtifactType());
            artifactInfo.setArtifactGroupType(currentArtifactInfo.getArtifactGroupType());
            artifactInfo.setArtifactLabel(currentArtifactInfo.getArtifactLabel());
        }
    }

    private ArtifactDefinition findArtifact(final Component parentComponent, final ComponentTypeEnum componentType, final String parentId,
                                            final ArtifactOperationInfo operation, final String artifactId) {
        ArtifactDefinition foundArtifact = null;
        if (StringUtils.isNotEmpty(artifactId)) {
            foundArtifact = findArtifact(parentComponent, componentType, parentId, artifactId);
        }
        if (foundArtifact != null && operation.isCreateOrLink()) {
            log.debug("Artifact {} already exist", artifactId);
            throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_EXIST, foundArtifact.getArtifactLabel());
        }
        if (foundArtifact == null && operation.isNotCreateOrLink()) {
            log.debug("The artifact {} was not found on parent component or instance {}. ", artifactId, parentId);
            throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_NOT_FOUND, "");
        }
        return foundArtifact;
    }

    private ArtifactDefinition findArtifact(Component parentComponent, ComponentTypeEnum componentType, String parentId, String artifactId) {
        ArtifactDefinition foundArtifact;
        if (parentComponent.getUniqueId().equals(parentId)) {
            foundArtifact = artifactsResolver.findArtifactOnComponent(parentComponent, componentType, artifactId);
        } else {
            ComponentInstance instance = findComponentInstance(parentId, parentComponent);
            foundArtifact = artifactsResolver.findArtifactOnComponentInstance(instance, artifactId);
        }
        return foundArtifact;
    }

    private void validateInformationalArtifact(final ArtifactDefinition artifactInfo, final Component component) {
        final ArtifactGroupTypeEnum groupType = artifactInfo.getArtifactGroupType();
        if (groupType != ArtifactGroupTypeEnum.INFORMATIONAL) {
            return;
        }
        final ComponentTypeEnum parentComponentType = component.getComponentType();
        final String artifactType = artifactInfo.getArtifactType();
        final ArtifactConfiguration artifactConfiguration = loadArtifactTypeConfig(artifactType).orElse(null);
        if (artifactConfiguration == null) {
            throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED, artifactType);
        }
        validateArtifactType(parentComponentType, artifactInfo.getArtifactGroupType(), artifactConfiguration);
        if (component.getComponentType() == ComponentTypeEnum.RESOURCE || component.getComponentType() == ComponentTypeEnum.RESOURCE_INSTANCE) {
            final ResourceTypeEnum resourceType = ((Resource) component).getResourceType();
            validateResourceType(resourceType, artifactInfo, artifactConfiguration.getResourceTypes());
        }
        validateArtifactExtension(artifactConfiguration, artifactInfo);
    }

    private NodeTypeEnum convertParentType(ComponentTypeEnum componentType) {
        if (componentType == ComponentTypeEnum.RESOURCE) {
            return NodeTypeEnum.Resource;
        } else if (componentType == ComponentTypeEnum.RESOURCE_INSTANCE) {
            return NodeTypeEnum.ResourceInstance;
        } else {
            return NodeTypeEnum.Service;
        }
    }

    // This method is here for backward compatibility - when other parts of the code are cleaned can change to use the internal version
    public Either<ArtifactDefinition, ResponseFormat> handleDelete(String parentId, String artifactId, User user, Component parent,
                                                                   boolean shouldLock, boolean inTransaction) {
        ResponseFormat responseFormat;
        boolean operationSucceeded = false;
        if (shouldLock) {
            lockComponent(ComponentTypeEnum.RESOURCE, artifactId, AuditingActionEnum.ARTIFACT_DELETE, user, parent);
        }
        try {
            ArtifactDefinition artifactDefinition = handleDeleteInternal(parentId, artifactId, ComponentTypeEnum.RESOURCE, parent);
            operationSucceeded = true;
            return Either.left(artifactDefinition);
        } catch (ComponentException ce) {
            responseFormat = componentsUtils.getResponseFormat(ce);
            handleAuditing(AuditingActionEnum.ARTIFACT_DELETE, parent, parentId, user, null, null, artifactId, responseFormat,
                ComponentTypeEnum.RESOURCE, null);
            return Either.right(responseFormat);
        } catch (StorageException se) {
            responseFormat = componentsUtils.getResponseFormat(se);
            handleAuditing(AuditingActionEnum.ARTIFACT_DELETE, parent, parentId, user, null, null, artifactId, responseFormat,
                ComponentTypeEnum.RESOURCE, null);
            return Either.right(responseFormat);
        } finally {
            handleLockingAndCommit(parent, shouldLock, inTransaction, operationSucceeded);
        }
    }

    private ArtifactDefinition handleDeleteInternal(String parentId, String artifactId, ComponentTypeEnum componentType, Component parent) {
        NodeTypeEnum parentType = convertParentType(componentType);
        log.debug("Going to find the artifact {} on the component {}", artifactId, parent.getUniqueId());
        Either<ImmutablePair<ArtifactDefinition, ComponentInstance>, ActionStatus> getArtifactRes = findArtifact(artifactId, parent, parentId,
            componentType);
        if (getArtifactRes.isRight()) {
            log.debug("Failed to find the artifact {} belonging to {} on the component {}", artifactId, parentId, parent.getUniqueId());
            throw new ByActionStatusComponentException(getArtifactRes.right().value(), artifactId);
        }
        ArtifactDefinition foundArtifact = getArtifactRes.left().value().getLeft();
        ComponentInstance foundInstance = getArtifactRes.left().value().getRight();
        String esId = foundArtifact.getEsId();
        Either<Boolean, StorageOperationStatus> needClone = ifTrue(StringUtils.isNotEmpty(esId),
            () -> forEach(artifactToscaOperation.isCloneNeeded(parent.getUniqueId(), foundArtifact, parentType), b -> log
                .debug("handleDelete: clone is needed for deleting {} held by {} in component {} {}? {}", foundArtifact.getArtifactName(), parentType,
                    parent.getUniqueId(), parent.getName(), b)));
        boolean needToClone = false;
        // TODO: This should not be done, but in order to keep this refactoring small, we stop here.

        // Remove this block once the above refactoring is merged.
        if (needClone.isLeft()) {
            needToClone = needClone.left().value();
        } else {
            throw new StorageException(needClone.right().value(), foundArtifact.getArtifactDisplayName());
        }
        boolean isNeedToDeleteArtifactFromDB =
            componentType == ComponentTypeEnum.RESOURCE_INSTANCE && isArtifactOnlyResourceInstanceArtifact(foundArtifact, parent, parentId);
        boolean isDuplicated = false;
        ArtifactDataDefinition updatedArtifact = deleteOrUpdateArtifactOnGraph(parent, parentId, artifactId, parentType, foundArtifact, needToClone);
        isDuplicated = updatedArtifact.getDuplicated();
        if (!needToClone && !isDuplicated && isNeedToDeleteArtifactFromDB) {
            log.debug("Going to delete the artifact {} from the database. ", artifactId);
            CassandraOperationStatus cassandraStatus = artifactCassandraDao.deleteArtifact(esId);
            if (cassandraStatus != CassandraOperationStatus.OK) {
                log.debug("Failed to delete the artifact {} from the database. ", artifactId);
                throw new StorageException(convertToStorageOperationStatus(cassandraStatus), foundArtifact.getArtifactDisplayName());
            }
        }
        if (componentType == ComponentTypeEnum.RESOURCE_INSTANCE) {
            List<GroupInstance> updatedGroupInstances = getUpdatedGroupInstances(artifactId, foundArtifact, foundInstance.getGroupInstances());
            if (CollectionUtils.isNotEmpty(updatedGroupInstances)) {
                Either<List<GroupInstance>, StorageOperationStatus> status = toscaOperationFacade
                    .updateGroupInstancesOnComponent(parent, parentId, updatedGroupInstances);
                if (status.isRight()) {
                    log.debug(FAILED_UPDATE_GROUPS, parent.getUniqueId());
                    throw new StorageException(status.right().value(), foundArtifact.getArtifactDisplayName());
                }
            }
            StorageOperationStatus status = generateCustomizationUUIDOnInstance(parent.getUniqueId(), parentId, componentType);
            if (status != StorageOperationStatus.OK) {
                log.debug("Failed to generate new customization UUID for the component instance {}. ", parentId);
                throw new StorageException(status, foundArtifact.getArtifactDisplayName());
            }
        } else {
            List<GroupDataDefinition> updatedGroups = getUpdatedGroups(artifactId, foundArtifact, parent.getGroups());
            if (CollectionUtils.isNotEmpty(updatedGroups)) {
                Either<List<GroupDefinition>, StorageOperationStatus> status = toscaOperationFacade.updateGroupsOnComponent(parent, updatedGroups);
                if (status.isRight()) {
                    log.debug(FAILED_UPDATE_GROUPS, parent.getUniqueId());
                    throw new StorageException(status.right().value(), foundArtifact.getArtifactDisplayName());
                }
            }
        }
        return foundArtifact;
    }

    private boolean isArtifactOnlyResourceInstanceArtifact(ArtifactDefinition foundArtifact, Component parent, String instanceId) {
        Optional<ComponentInstance> componentInstanceOpt = parent.getComponentInstanceById(instanceId);
        if (!componentInstanceOpt.isPresent()) {
            throw new ByActionStatusComponentException(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, instanceId, "", "", parent.getName());
        }
        ComponentInstance foundInstance = componentInstanceOpt.get();
        String componentUid = foundInstance.getComponentUid();
        Either<Component, StorageOperationStatus> getContainerRes = toscaOperationFacade.getToscaElement(componentUid);
        if (getContainerRes.isRight()) {
            log.debug("Failed to fetch the container component {}. ", componentUid);
            throw new StorageException(getContainerRes.right().value());
        }
        Component origComponent = getContainerRes.left().value();
        Map<String, ArtifactDefinition> deploymentArtifacts = origComponent.getDeploymentArtifacts();
        if (MapUtils.isNotEmpty(deploymentArtifacts)) {
            Optional<String> op = deploymentArtifacts.keySet().stream().filter(a -> a.equals(foundArtifact.getArtifactLabel())).findAny();
            if (op.isPresent()) {
                return false;
            }
        }
        Map<String, ArtifactDefinition> artifacts = origComponent.getArtifacts();
        if (MapUtils.isNotEmpty(artifacts)) {
            Optional<String> op = artifacts.keySet().stream().filter(a -> a.equals(foundArtifact.getArtifactLabel())).findAny();
            if (op.isPresent()) {
                return false;
            }
        }
        return true;
    }

    private List<GroupDataDefinition> getUpdatedGroups(String artifactId, ArtifactDefinition foundArtifact, List<GroupDefinition> groups) {
        List<GroupDataDefinition> updatedGroups = new ArrayList<>();
        boolean isUpdated = false;
        if (groups != null) {
            for (GroupDefinition group : groups) {
                isUpdated = false;
                if (CollectionUtils.isNotEmpty(group.getArtifacts()) && group.getArtifacts().contains(artifactId)) {
                    group.getArtifacts().remove(artifactId);
                    isUpdated = true;
                }
                if (CollectionUtils.isNotEmpty(group.getArtifactsUuid()) && group.getArtifactsUuid().contains(foundArtifact.getArtifactUUID())) {
                    group.getArtifactsUuid().remove(foundArtifact.getArtifactUUID());
                    isUpdated = true;
                }
                if (isUpdated) {
                    updatedGroups.add(group);
                }
            }
        }
        return updatedGroups;
    }

    private List<GroupInstance> getUpdatedGroupInstances(String artifactId, ArtifactDefinition foundArtifact, List<GroupInstance> groupInstances) {
        if (CollectionUtils.isEmpty(groupInstances)) {
            return new ArrayList<>();
        }
        // TODO: A defensive copy should be created here for groupInstances. Modifying

        // arguments (aka output arguments) is overall a bad practice as explained in

        // Clean Code by Robert Martin.

        // A better approach would be to use Lenses.
        return groupInstances.stream().filter(gi -> {
            boolean groupInstanceArtifactRemoved = gi.getGroupInstanceArtifacts() != null && gi.getGroupInstanceArtifacts().remove(artifactId);
            boolean groupInstanceArtifactUUIDRemoved =
                gi.getGroupInstanceArtifactsUuid() != null && gi.getGroupInstanceArtifactsUuid().remove(foundArtifact.getArtifactUUID());
            return groupInstanceArtifactRemoved || groupInstanceArtifactUUIDRemoved;
        }).collect(Collectors.toList());
    }

    private ArtifactDataDefinition deleteOrUpdateArtifactOnGraph(Component component, String parentId, String artifactId, NodeTypeEnum parentType,
                                                                 ArtifactDefinition foundArtifact, Boolean cloneIsNeeded) {
        Either<ArtifactDataDefinition, StorageOperationStatus> result;
        boolean isMandatory = foundArtifact.getMandatory() || foundArtifact.getServiceApi();
        String componentId = component.getUniqueId();
        String instanceId = componentId.equals(parentId) ? null : parentId;
        if (isMandatory) {
            log.debug("Going to update mandatory artifact {} from the component {}", artifactId, parentId);
            resetMandatoryArtifactFields(foundArtifact);
            result = artifactToscaOperation.updateArtifactOnGraph(component, foundArtifact, parentType, artifactId, instanceId, true, true);
        } else if (cloneIsNeeded) {
            log.debug("Going to clone artifacts and to delete the artifact {} from the component {}", artifactId, parentId);
            result = artifactToscaOperation.deleteArtifactWithCloningOnGraph(componentId, foundArtifact, parentType, instanceId, false);
        } else {
            log.debug("Going to delete the artifact {} from the component {}", artifactId, parentId);
            result = artifactToscaOperation.removeArtifactOnGraph(foundArtifact, componentId, instanceId, parentType, false);
        }
        if (result.isRight()) {
            throw new StorageException(result.right().value(), foundArtifact.getArtifactDisplayName());
        }
        return result.left().value();
    }

    private Either<ImmutablePair<ArtifactDefinition, ComponentInstance>, ActionStatus> findArtifact(String artifactId,
                                                                                                    Component fetchedContainerComponent,
                                                                                                    String parentId,
                                                                                                    ComponentTypeEnum componentType) {
        Either<ImmutablePair<ArtifactDefinition, ComponentInstance>, ActionStatus> result = null;
        Map<String, ArtifactDefinition> artifacts = new HashMap<>();
        ComponentInstance foundInstance = null;
        if (componentType == ComponentTypeEnum.RESOURCE_INSTANCE && StringUtils.isNotEmpty(parentId)) {
            Optional<ComponentInstance> componentInstanceOpt = fetchedContainerComponent.getComponentInstances().stream()
                .filter(i -> i.getUniqueId().equals(parentId)).findFirst();
            if (!componentInstanceOpt.isPresent()) {
                result = Either.right(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER);
            } else {
                foundInstance = componentInstanceOpt.get();
                fetchArtifactsFromInstance(artifactId, artifacts, foundInstance);
            }
        } else {
            fetchArtifactsFromComponent(artifactId, fetchedContainerComponent, artifacts);
        }
        if (result == null) {
            if (artifacts.containsKey(artifactId)) {
                result = Either.left(new ImmutablePair<>(artifacts.get(artifactId), foundInstance));
            } else {
                result = Either.right(ActionStatus.ARTIFACT_NOT_FOUND);
            }
        }
        return result;
    }

    private void fetchArtifactsFromComponent(String artifactId, Component component, Map<String, ArtifactDefinition> artifacts) {
        Map<String, ArtifactDefinition> currArtifacts;
        if (!artifacts.containsKey(artifactId) && MapUtils.isNotEmpty(component.getDeploymentArtifacts())) {
            currArtifacts = component.getDeploymentArtifacts().values().stream()
                .collect(Collectors.toMap(ArtifactDataDefinition::getUniqueId, i -> i));
            if (MapUtils.isNotEmpty(currArtifacts)) {
                artifacts.putAll(currArtifacts);
            }
        }
        if (!artifacts.containsKey(artifactId) && MapUtils.isNotEmpty(component.getArtifacts())) {
            currArtifacts = component.getArtifacts().values().stream()
                .collect(Collectors.toMap(ArtifactDataDefinition::getUniqueId, Function.identity()));
            if (MapUtils.isNotEmpty(currArtifacts)) {
                artifacts.putAll(currArtifacts);
            }
        }
        if (!artifacts.containsKey(artifactId) && MapUtils.isNotEmpty(component.getArtifacts())) {
            currArtifacts = component.getToscaArtifacts().values().stream()
                .collect(Collectors.toMap(ArtifactDataDefinition::getUniqueId, Function.identity()));
            if (MapUtils.isNotEmpty(currArtifacts)) {
                artifacts.putAll(currArtifacts);
            }
        }
    }

    private void fetchArtifactsFromInstance(String artifactId, Map<String, ArtifactDefinition> artifacts, ComponentInstance instance) {
        Map<String, ArtifactDefinition> currArtifacts;
        if (MapUtils.isNotEmpty(instance.getDeploymentArtifacts())) {
            currArtifacts = instance.getDeploymentArtifacts().values().stream()
                .collect(Collectors.toMap(ArtifactDataDefinition::getUniqueId, Function.identity()));
            if (MapUtils.isNotEmpty(currArtifacts)) {
                artifacts.putAll(currArtifacts);
            }
        }
        if (!artifacts.containsKey(artifactId) && MapUtils.isNotEmpty(instance.getArtifacts())) {
            currArtifacts = instance.getArtifacts().values().stream()
                .collect(Collectors.toMap(ArtifactDataDefinition::getUniqueId, Function.identity()));
            if (MapUtils.isNotEmpty(currArtifacts)) {
                artifacts.putAll(currArtifacts);
            }
        }
    }

    private StorageOperationStatus convertToStorageOperationStatus(CassandraOperationStatus cassandraStatus) {
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

    private void resetMandatoryArtifactFields(ArtifactDefinition fetchedArtifact) {
        if (fetchedArtifact != null) {
            log.debug("Going to reset mandatory artifact {} fields. ", fetchedArtifact.getUniqueId());
            fetchedArtifact.setEsId(null);
            fetchedArtifact.setArtifactName(null);
            fetchedArtifact.setDescription(null);
            fetchedArtifact.setApiUrl(null);
            fetchedArtifact.setArtifactChecksum(null);
            nodeTemplateOperation.setDefaultArtifactTimeout(fetchedArtifact.getArtifactGroupType(), fetchedArtifact);
            fetchedArtifact.setArtifactUUID(null);
            long time = System.currentTimeMillis();
            fetchedArtifact.setPayloadUpdateDate(time);
            fetchedArtifact.setHeatParameters(null);
            fetchedArtifact.setHeatParamsUpdateDate(null);
        }
    }

    private StorageOperationStatus generateCustomizationUUIDOnInstance(String componentId, String instanceId, ComponentTypeEnum componentType) {
        StorageOperationStatus error = StorageOperationStatus.OK;
        if (componentType == ComponentTypeEnum.RESOURCE_INSTANCE) {
            log.debug("Need to re-generate  customization UUID for instance {}", instanceId);
            error = toscaOperationFacade.generateCustomizationUUIDOnInstance(componentId, instanceId);
        }
        return error;
    }

    private ArtifactDefinition handleDownload(String componentId, String artifactId, ComponentTypeEnum componentType, Component parent) {
        Either<ArtifactDefinition, StorageOperationStatus> artifactById = artifactToscaOperation
            .getArtifactById(componentId, artifactId, componentType, parent.getUniqueId());
        if (artifactById.isRight()) {
            throw new StorageException(artifactById.right().value());
        }
        ArtifactDefinition artifactDefinition = artifactById.left().value();
        if (artifactDefinition == null) {
            throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_NOT_FOUND, artifactId);
        }
        return artifactDefinition;
    }

    private Either<ActionStatus, ResponseFormat> handleArtifactLabel(String componentId, Component parentComponent, ArtifactOperationInfo operation,
                                                                     ArtifactDefinition artifactInfo, String operationName,
                                                                     ComponentTypeEnum componentType) {
        String artifactLabel = artifactInfo.getArtifactLabel();
        if (operationName == null && (artifactInfo.getArtifactLabel() == null || artifactInfo.getArtifactLabel().isEmpty())) {
            BeEcompErrorManager.getInstance().logBeMissingArtifactInformationError("Artifact Update / Upload", "artifactLabel");
            log.debug("missing artifact logical name for component {}", componentId);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, ARTIFACT_LABEL));
        }
        if (operation.isCreateOrLink() && !artifactInfo.getMandatory()) {
            if (operationName != null) {
                if (artifactInfo.getArtifactLabel() != null && !operationName.equals(artifactInfo.getArtifactLabel())) {
                    log.debug("artifact label cannot be set {}", artifactLabel);
                    return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_LOGICAL_NAME_CANNOT_BE_CHANGED));
                } else {
                    artifactLabel = operationName;
                }
            }
            String displayName = artifactInfo.getArtifactDisplayName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = artifactLabel;
            }
            displayName = ValidationUtils.cleanArtifactDisplayName(displayName);
            artifactInfo.setArtifactDisplayName(displayName);
            if (!ValidationUtils.validateArtifactLabel(artifactLabel)) {
                log.debug("Invalid format form Artifact label : {}", artifactLabel);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_ARTIFACT_LABEL_NAME, VALID_ARTIFACT_LABEL_NAME));
            }
            artifactLabel = ValidationUtils.normalizeArtifactLabel(artifactLabel);
            if (artifactLabel.isEmpty()) {
                log.debug("missing normalized artifact logical name for component {}", componentId);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, ARTIFACT_LABEL));
            }
            if (!ValidationUtils.validateArtifactLabelLength(artifactLabel)) {
                log.debug("Invalid lenght form Artifact label : {}", artifactLabel);
                return Either.right(componentsUtils
                    .getResponseFormat(ActionStatus.EXCEEDS_LIMIT, ARTIFACT_LABEL, String.valueOf(ValidationUtils.ARTIFACT_LABEL_LENGTH)));
            }
            if (!validateLabelUniqueness(componentId, parentComponent, artifactLabel, componentType)) {
                log.debug("Non unique Artifact label : {}", artifactLabel);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_EXIST, artifactLabel));
            }
        }
        artifactInfo.setArtifactLabel(artifactLabel);
        return Either.left(ActionStatus.OK);
    }

    private boolean validateLabelUniqueness(String componentId, Component parentComponent, String artifactLabel, ComponentTypeEnum componentType) {
        boolean isUnique = true;
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifacts;
        if (componentType == ComponentTypeEnum.RESOURCE_INSTANCE) {
            artifacts = artifactToscaOperation.getAllInstanceArtifacts(parentComponent.getUniqueId(), componentId);
        } else {
            artifacts = artifactToscaOperation.getArtifacts(componentId);
        }
        if (artifacts.isLeft()) {
            for (String label : artifacts.left().value().keySet()) {
                if (label.equals(artifactLabel)) {
                    isUnique = false;
                    break;
                }
            }
        }
        if (componentType == ComponentTypeEnum.RESOURCE && isUnique) {
            isUnique = isUniqueLabelInResourceInterfaces(componentId, artifactLabel);
        }
        return isUnique;
    }

    boolean validateArtifactNameUniqueness(String componentId, Component parentComponent, ArtifactDefinition artifactInfo,
                                           ComponentTypeEnum componentType) {
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifacts = getArtifacts(componentType, parentComponent, componentId,
            artifactInfo.getArtifactGroupType());
        String artifactName = artifactInfo.getArtifactName();
        if (artifacts.isLeft() && Objects.nonNull(artifacts.left().value())) {
            if (artifacts.left().value().values().stream().anyMatch(ad -> artifactName.equals(ad.getArtifactName())
                //check whether it is the same artifact we hold (by label)
                && !artifactInfo.getArtifactLabel().equals(ad.getArtifactLabel()))) {
                return false;
            }
        }
        if (ComponentTypeEnum.RESOURCE == componentType) {
            return isUniqueArtifactNameInResourceInterfaces(componentId, artifactName, artifactInfo.getArtifactLabel());
        }
        return true;
    }

    private boolean isUniqueArtifactNameInResourceInterfaces(String componentId, String artifactName, String artifactLabel) {
        Either<Map<String, InterfaceDefinition>, StorageOperationStatus> allInterfacesOfResource = interfaceLifecycleOperation
            .getAllInterfacesOfResource(componentId, true, true);
        if (allInterfacesOfResource.isLeft()) {
            return allInterfacesOfResource.left().value().values().stream().map(InterfaceDefinition::getOperationsMap)
                .flatMap(map -> map.values().stream()).map(OperationDataDefinition::getImplementation).filter(Objects::nonNull)
                .noneMatch(add -> artifactName.equals(add.getArtifactName()) && !artifactLabel.equals(add.getArtifactLabel()));
        }
        return true;
    }

    private boolean isUniqueLabelInResourceInterfaces(String componentId, String artifactLabel) {
        Either<Map<String, InterfaceDefinition>, StorageOperationStatus> allInterfacesOfResource = interfaceLifecycleOperation
            .getAllInterfacesOfResource(componentId, true, true);
        if (allInterfacesOfResource.isLeft()) {
            return allInterfacesOfResource.left().value().values().stream().map(InterfaceDefinition::getOperationsMap)
                .flatMap(map -> map.values().stream()).map(OperationDataDefinition::getImplementation).filter(Objects::nonNull)
                .noneMatch(add -> artifactLabel.equals(add.getArtifactLabel()));
        }
        return true;
    }

    private Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getArtifacts(ComponentTypeEnum componentType, Component parentComponent,
                                                                                         String componentId,
                                                                                         ArtifactGroupTypeEnum artifactGroupType) {
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifactsResponse;
        if (componentType == ComponentTypeEnum.RESOURCE_INSTANCE) {
            artifactsResponse = artifactToscaOperation.getAllInstanceArtifacts(parentComponent.getUniqueId(), componentId);
        } else {
            artifactsResponse = artifactToscaOperation.getArtifacts(componentId);
        }
        if (artifactsResponse.isRight() && artifactsResponse.right().value() == StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to retrieve artifacts for {} ", componentId);
            return Either.right(artifactsResponse.right().value());
        }
        return Either.left(artifactsResponse.left().value().entrySet().stream().filter(x -> artifactGroupType == x.getValue().getArtifactGroupType())
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
    }

    // ***************************************************************
    private Either<ArtifactDefinition, Operation> createArtifact(Component parent, String parentId, ArtifactDefinition artifactInfo,
                                                                 byte[] decodedPayload, ComponentTypeEnum componentTypeEnum,
                                                                 AuditingActionEnum auditingActionEnum, String interfaceType, String operationName) {
        DAOArtifactData artifactData = createEsArtifactData(artifactInfo, decodedPayload);
        if (artifactData == null) {
            BeEcompErrorManager.getInstance().logBeDaoSystemError("Upload Artifact");
            log.debug("Failed to create artifact object for ES.");
            throw new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR);
        }
        ComponentInstance foundInstance = findComponentInstance(parentId, parent);
        String instanceId = null;
        if (foundInstance != null) {
            if (foundInstance.isArtifactExists(artifactInfo.getArtifactGroupType(), artifactInfo.getArtifactLabel())) {
                log.debug("Failed to create artifact, already exists");
                throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_EXIST, artifactInfo.getArtifactLabel());
            }
            instanceId = foundInstance.getUniqueId();
        }
        // set on graph object id of artifact in ES!
        artifactInfo.setEsId(artifactData.getId());
        Either<ArtifactDefinition, Operation> operationResult;
        if (interfaceType != null && operationName != null) {
            // lifecycle artifact
            Operation operation = convertToOperation(artifactInfo, operationName);
            Either<Operation, StorageOperationStatus> result = interfaceLifecycleOperation
                .updateInterfaceOperation(parentId, interfaceType, operationName, operation);
            if (result.isRight()) {
                throw new StorageException(result.right().value());
            }
            operationResult = Either.right(result.left().value());
        } else {
            // information/deployment/api artifacts
            NodeTypeEnum nodeType = convertParentType(componentTypeEnum);
            Either<ArtifactDefinition, StorageOperationStatus> result = artifactToscaOperation
                .addArtifactToComponent(artifactInfo, parent, nodeType, true, instanceId);
            if (result.isRight()) {
                throw new StorageException(result.right().value());
            }
            ArtifactDefinition artifactDefinition = result.left().value();
            artifactData.setId(artifactDefinition.getEsId());
            operationResult = Either.left(artifactDefinition);
            if (generateCustomizationUUIDOnInstance(parent.getUniqueId(), parentId, componentTypeEnum) != StorageOperationStatus.OK) {
                throw new StorageException(generateCustomizationUUIDOnInstance(parent.getUniqueId(), parentId, componentTypeEnum));
            }
        }
        saveArtifactInCassandra(artifactData, parent, artifactInfo, "", "", auditingActionEnum, componentTypeEnum);
        return operationResult;
    }

    private ComponentInstance findComponentInstance(String componentInstanceId, Component containerComponent) {
        ComponentInstance foundInstance = null;
        if (CollectionUtils.isNotEmpty(containerComponent.getComponentInstances())) {
            foundInstance = containerComponent.getComponentInstances().stream().filter(i -> i.getUniqueId().equals(componentInstanceId)).findFirst()
                .orElse(null);
        }
        return foundInstance;
    }

    private void validateDeploymentArtifact(final ArtifactDefinition artifactInfo, final Component component) {
        final ComponentTypeEnum componentType = component.getComponentType();
        if (componentType != ComponentTypeEnum.RESOURCE && componentType != ComponentTypeEnum.SERVICE
            && componentType != ComponentTypeEnum.RESOURCE_INSTANCE) {
            log.debug("Invalid component type '{}' for artifact. " + "Expected Resource, Component or Resource Instance", componentType.getValue());
            throw new ByActionStatusComponentException(MISMATCH_BETWEEN_ARTIFACT_TYPE_AND_COMPONENT_TYPE, componentType.getValue(),
                "Service, Resource or ResourceInstance", componentType.getValue());
        }
        final String artifactType = artifactInfo.getArtifactType();
        final ArtifactConfiguration artifactConfiguration = loadArtifactTypeConfig(artifactType).orElse(null);
        if (artifactConfiguration == null) {
            throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED, artifactType);
        }
        validateArtifactType(componentType, artifactInfo.getArtifactGroupType(), artifactConfiguration);
        if (componentType == ComponentTypeEnum.RESOURCE || componentType == ComponentTypeEnum.RESOURCE_INSTANCE) {
            final Resource resource = (Resource) component;
            final ResourceTypeEnum resourceType = resource.getResourceType();
            validateResourceType(resourceType, artifactInfo, artifactConfiguration.getResourceTypes());
        }
        validateArtifactExtension(artifactConfiguration, artifactInfo);
    }

    private void validateHeatArtifact(final Component parentComponent, final String componentId, final ArtifactDefinition artifactDefinition) {
        final String artifactType = artifactDefinition.getArtifactType();
        final ArtifactTypeEnum artifactTypeEnum = ArtifactTypeEnum.parse(artifactType);
        if (artifactTypeEnum == null) {
            return;
        }
        switch (artifactTypeEnum) {
            case HEAT:
            case HEAT_VOL:
            case HEAT_NET:
                validateHeatTimeoutValue(artifactDefinition);
                break;
            case HEAT_ENV:
                validateHeatEnvDeploymentArtifact(parentComponent, componentId, artifactDefinition);
                break;
            default:
                break;
        }
    }

    private void setArtifactTimeout(final ArtifactDefinition newArtifactInfo, final ArtifactDefinition existingArtifactInfo) {
        final String artifactType = newArtifactInfo.getArtifactType();
        final ArtifactTypeEnum artifactTypeEnum = ArtifactTypeEnum.parse(artifactType);
        if (artifactTypeEnum == null) {
            newArtifactInfo.setTimeout(NodeTemplateOperation.NON_HEAT_TIMEOUT);
            return;
        }
        switch (artifactTypeEnum) {
            case HEAT:
            case HEAT_VOL:
            case HEAT_NET:
                if (newArtifactInfo.getTimeout() == null) {
                    if (existingArtifactInfo == null) {
                        newArtifactInfo.setTimeout(NodeTemplateOperation.getDefaultHeatTimeout());
                    } else {
                        newArtifactInfo.setTimeout(existingArtifactInfo.getTimeout());
                    }
                }
                break;
            default:
                newArtifactInfo.setTimeout(NodeTemplateOperation.NON_HEAT_TIMEOUT);
                break;
        }
    }

    @VisibleForTesting
    void validateDeploymentArtifactTypeIsLegalForParent(ArtifactDefinition artifactInfo, ArtifactTypeEnum artifactType,
                                                        Map<String, ArtifactTypeConfig> resourceDeploymentArtifacts) {
        if ((resourceDeploymentArtifacts == null) || !resourceDeploymentArtifacts.containsKey(artifactType.name())) {
            log.debug("Artifact Type: {} Not found !", artifactInfo.getArtifactType());
            throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED, artifactInfo.getArtifactType());
        }
    }

    Optional<ArtifactConfiguration> loadArtifactTypeConfig(final String artifactType) {
        if (artifactType == null) {
            return Optional.empty();
        }
        final List<ArtifactConfiguration> artifactConfigurationList = ConfigurationManager.getConfigurationManager().getConfiguration()
            .getArtifacts();
        if (CollectionUtils.isEmpty(artifactConfigurationList)) {
            return Optional.empty();
        }
        return artifactConfigurationList.stream().filter(artifactConfiguration -> artifactConfiguration.getType().equalsIgnoreCase(artifactType))
            .findFirst();
    }

    private Either<Boolean, ResponseFormat> extractHeatParameters(ArtifactDefinition artifactInfo) {
        // extract heat parameters
        if (artifactInfo.getPayloadData() != null) {
            String heatDecodedPayload = new String(Base64.decodeBase64(artifactInfo.getPayloadData()));
            Either<List<HeatParameterDefinition>, ResultStatusEnum> heatParameters = ImportUtils
                .getHeatParamsWithoutImplicitTypes(heatDecodedPayload, artifactInfo.getArtifactType());
            if (heatParameters.isRight() && (heatParameters.right().value() != ResultStatusEnum.ELEMENT_NOT_FOUND)) {
                log.info("failed to parse heat parameters ");
                ResponseFormat responseFormat = componentsUtils
                    .getResponseFormat(ActionStatus.INVALID_DEPLOYMENT_ARTIFACT_HEAT, artifactInfo.getArtifactType());
                return Either.right(responseFormat);
            } else if (heatParameters.isLeft() && heatParameters.left().value() != null) {
                artifactInfo.setListHeatParameters(heatParameters.left().value());
            }
        }
        return Either.left(true);
    }

    @VisibleForTesting
    void validateArtifactExtension(final ArtifactConfiguration artifactConfiguration, final ArtifactDefinition artifactDefinition) {
        final List<String> acceptedTypes = artifactConfiguration.getAcceptedTypes();
        /*
         * No need to check specific types. In case there are no acceptedTypes in configuration, then any type is accepted.
         */
        if (CollectionUtils.isEmpty(acceptedTypes)) {
            return;
        }
        final String artifactName = artifactDefinition.getArtifactName();
        final String fileExtension = FilenameUtils.getExtension(artifactName);
        if (fileExtension == null || !acceptedTypes.contains(fileExtension.toLowerCase())) {
            final String artifactType = artifactDefinition.getArtifactType();
            log.debug("File extension \"{}\" is not allowed for artifact type \"{}\"", fileExtension, artifactType);
            throw new ByActionStatusComponentException(ActionStatus.WRONG_ARTIFACT_FILE_EXTENSION, artifactType);
        }
    }

    @VisibleForTesting
    void validateHeatEnvDeploymentArtifact(final Component parentComponent, final String parentId, final ArtifactDefinition artifactInfo) {
        final Wrapper<ArtifactDefinition> heatMDWrapper = new Wrapper<>();
        final Wrapper<byte[]> payloadWrapper = new Wrapper<>();
        validateYaml(artifactInfo);
        validateHeatExist(parentComponent.getUniqueId(), parentId, heatMDWrapper, artifactInfo, parentComponent.getComponentType());
        if (!heatMDWrapper.isEmpty()) {
            fillArtifactPayload(payloadWrapper, heatMDWrapper.getInnerElement());
        }
        if (!heatMDWrapper.isEmpty()) {
            validateEnvVsHeat(artifactInfo, heatMDWrapper.getInnerElement(), payloadWrapper.getInnerElement());
        }
    }

    public void fillArtifactPayload(Wrapper<byte[]> payloadWrapper, ArtifactDefinition artifactDefinition) {
        if (ArrayUtils.isEmpty(artifactDefinition.getPayloadData())) {
            Either<DAOArtifactData, CassandraOperationStatus> eitherArtifactData = artifactCassandraDao.getArtifact(artifactDefinition.getEsId());
            if (eitherArtifactData.isLeft()) {
                byte[] data = eitherArtifactData.left().value().getDataAsArray();
                payloadWrapper.setInnerElement(Base64.encodeBase64(data));
            } else {
                log.debug("Error getting payload for artifact:{}", artifactDefinition.getArtifactName());
                throw new StorageException(DaoStatusConverter.convertCassandraStatusToStorageStatus(eitherArtifactData.right().value()));
            }
        } else {
            payloadWrapper.setInnerElement(artifactDefinition.getPayloadData());
        }
    }

    private void validateEnvVsHeat(ArtifactDefinition envArtifact, ArtifactDefinition heatArtifact, byte[] heatPayloadData) {
        String envPayload = new String(Base64.decodeBase64(envArtifact.getPayloadData()));
        Map<String, Object> heatEnvToscaJson = (Map<String, Object>) new Yaml().load(envPayload);
        String heatDecodedPayload = new String(Base64.decodeBase64(heatPayloadData));
        Map<String, Object> heatToscaJson = (Map<String, Object>) new Yaml().load(heatDecodedPayload);
        Either<Map<String, Object>, ResultStatusEnum> eitherHeatEnvProperties = ImportUtils
            .findFirstToscaMapElement(heatEnvToscaJson, TypeUtils.ToscaTagNamesEnum.PARAMETERS);
        if (eitherHeatEnvProperties.isRight()) {
            log.debug("Invalid heat env format for file:{}", envArtifact.getArtifactName());
            throw new ByActionStatusComponentException(ActionStatus.CORRUPTED_FORMAT, "Heat Env");
        }
        Either<Map<String, Object>, ResultStatusEnum> eitherHeatProperties = ImportUtils
            .findFirstToscaMapElement(heatToscaJson, TypeUtils.ToscaTagNamesEnum.PARAMETERS);
        if (eitherHeatProperties.isRight()) {
            log.debug("Invalid heat format for file:{}", heatArtifact.getArtifactName());
            throw new ByActionStatusComponentException(ActionStatus.CORRUPTED_FORMAT, "Heat");
        }
        Set<String> heatPropertiesKeys = eitherHeatProperties.left().value().keySet();
        Set<String> heatEnvPropertiesKeys = eitherHeatEnvProperties.left().value().keySet();
        heatEnvPropertiesKeys.removeAll(heatPropertiesKeys);
        if (!heatEnvPropertiesKeys.isEmpty()) {
            log.debug("Validation of heat_env for artifact:{} vs heat artifact for artifact :{} failed", envArtifact.getArtifactName(),
                heatArtifact.getArtifactName());
            throw new ByActionStatusComponentException(ActionStatus.MISMATCH_HEAT_VS_HEAT_ENV, envArtifact.getArtifactName(),
                heatArtifact.getArtifactName());
        }
    }

    private void validateYaml(ArtifactDefinition artifactInfo) {
        YamlToObjectConverter yamlConverter = new YamlToObjectConverter();
        boolean isYamlValid = yamlConverter.isValidYamlEncoded64(artifactInfo.getPayloadData());
        if (!isYamlValid) {
            log.debug("Yaml is not valid for artifact : {}", artifactInfo.getArtifactName());
            throw new ByActionStatusComponentException(ActionStatus.INVALID_YAML, artifactInfo.getArtifactType());
        }
    }

    private void validateSingleDeploymentArtifactName(final String artifactName, final Component parentComponent) {
        boolean artifactNameFound = false;
        final Iterator<ArtifactDefinition> parentDeploymentArtifactsItr = getDeploymentArtifacts(parentComponent, null).iterator();
        while (!artifactNameFound && parentDeploymentArtifactsItr.hasNext()) {
            artifactNameFound = artifactName.equalsIgnoreCase(parentDeploymentArtifactsItr.next().getArtifactName());
        }
        if (artifactNameFound) {
            final ComponentTypeEnum componentType = parentComponent.getComponentType();
            log.debug("Can't upload artifact: {}, because another artifact with this name already exist.", artifactName);
            throw new ByActionStatusComponentException(ActionStatus.DEPLOYMENT_ARTIFACT_NAME_ALREADY_EXISTS, componentType.getValue(),
                parentComponent.getName(), artifactName);
        }
    }

    private void validateHeatExist(String componentId, String parentRiId, Wrapper<ArtifactDefinition> heatArtifactMDWrapper,
                                   ArtifactDefinition heatEnvArtifact, ComponentTypeEnum componentType) {
        final Either<ArtifactDefinition, StorageOperationStatus> res = artifactToscaOperation
            .getHeatArtifactByHeatEnvId(parentRiId, heatEnvArtifact, componentId, componentType);
        if (res.isRight()) {
            throw new ByActionStatusComponentException(ActionStatus.MISSING_HEAT);
        }
        heatArtifactMDWrapper.setInnerElement(res.left().value());
    }

    @VisibleForTesting
    void validateHeatTimeoutValue(final ArtifactDefinition artifactInfo) {
        log.trace("Started HEAT pre-payload validation for artifact {}", artifactInfo.getArtifactLabel());
        // timeout > 0 for HEAT artifacts
        if (artifactInfo.getTimeout() == null || artifactInfo.getTimeout() < 1) {
            throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_INVALID_TIMEOUT);
        }
        // US649856 - Allow several HEAT files on Resource
        log.trace("Ended HEAT validation for artifact {}", artifactInfo.getArtifactLabel());
    }

    @VisibleForTesting
    void validateResourceType(final ResourceTypeEnum resourceType, final ArtifactDefinition artifactInfo, final List<String> typeList) {
        if (CollectionUtils.isEmpty(typeList) || typeList.contains(resourceType.getValue())) {
            return;
        }
        final String listToString = typeList.stream().collect(Collectors.joining(", "));
        throw new ByActionStatusComponentException(MISMATCH_BETWEEN_ARTIFACT_TYPE_AND_COMPONENT_TYPE, artifactInfo.getArtifactGroupType().getType(),
            listToString, resourceType.getValue());
    }

    @VisibleForTesting
    Either<ArtifactDefinition, ResponseFormat> validateAndConvertHeatParameters(ArtifactDefinition artifactInfo, String artifactType) {
        if (artifactInfo.getHeatParameters() != null) {
            for (HeatParameterDefinition heatParam : artifactInfo.getListHeatParameters()) {
                String parameterType = heatParam.getType();
                HeatParameterType heatParameterType = HeatParameterType.isValidType(parameterType);
                String artifactTypeStr = artifactType != null ? artifactType : ArtifactTypeEnum.HEAT.getType();
                if (heatParameterType == null) {
                    ResponseFormat responseFormat = componentsUtils
                        .getResponseFormat(ActionStatus.INVALID_HEAT_PARAMETER_TYPE, artifactTypeStr, heatParam.getType());
                    return Either.right(responseFormat);
                }
                StorageOperationStatus validateAndUpdateProperty = heatParametersOperation.validateAndUpdateProperty(heatParam);
                if (validateAndUpdateProperty != StorageOperationStatus.OK) {
                    log.debug("Heat parameter {} is invalid. Status is {}", heatParam.getName(), validateAndUpdateProperty);
                    ActionStatus status = ActionStatus.INVALID_HEAT_PARAMETER_VALUE;
                    ResponseFormat responseFormat = componentsUtils
                        .getResponseFormat(status, artifactTypeStr, heatParam.getType(), heatParam.getName());
                    return Either.right(responseFormat);
                }
            }
        }
        return Either.left(artifactInfo);
    }

    public List<ArtifactDefinition> getDeploymentArtifacts(final Component component, final String ciId) {
        final ComponentTypeEnum componentType = component.getComponentType();
        if (component.getDeploymentArtifacts() == null) {
            return Collections.emptyList();
        }
        final List<ArtifactDefinition> deploymentArtifacts = new ArrayList<>();
        if (ComponentTypeEnum.RESOURCE == componentType && ciId != null) {
            final Either<ComponentInstance, ResponseFormat> getRI = getRIFromComponent(component, ciId, null, null, null);
            if (getRI.isRight()) {
                return Collections.emptyList();
            }
            final ComponentInstance ri = getRI.left().value();
            if (ri.getDeploymentArtifacts() != null) {
                deploymentArtifacts.addAll(ri.getDeploymentArtifacts().values());
            }
        } else {
            deploymentArtifacts.addAll(component.getDeploymentArtifacts().values());
        }
        return deploymentArtifacts;
    }

    private void checkCreateFields(User user, ArtifactDefinition artifactInfo, ArtifactGroupTypeEnum type) {
        // on create if null add informational to current
        if (artifactInfo.getArtifactGroupType() == null) {
            artifactInfo.setArtifactGroupType(type);
        }
        if (artifactInfo.getUniqueId() != null) {
            log.error("artifact uniqid cannot be set ignoring");
        }
        artifactInfo.setUniqueId(null);
        if (artifactInfo.getArtifactRef() != null) {
            log.error("artifact ref cannot be set ignoring");
        }
        artifactInfo.setArtifactRef(null);
        if (artifactInfo.getArtifactRepository() != null) {
            log.error("artifact repository cannot be set ignoring");
        }
        artifactInfo.setArtifactRepository(null);
        if (artifactInfo.getUserIdCreator() != null) {
            log.error("creator uuid cannot be set ignoring");
        }
        artifactInfo.setArtifactCreator(user.getUserId());
        if (artifactInfo.getUserIdLastUpdater() != null) {
            log.error("userId of last updater cannot be set ignoring");
        }
        artifactInfo.setUserIdLastUpdater(user.getUserId());
        if (artifactInfo.getCreatorFullName() != null) {
            log.error("creator Full name cannot be set ignoring");
        }
        String fullName = user.getFirstName() + " " + user.getLastName();
        artifactInfo.setUpdaterFullName(fullName);
        if (artifactInfo.getUpdaterFullName() != null) {
            log.error("updater Full name cannot be set ignoring");
        }
        artifactInfo.setUpdaterFullName(fullName);
        if (artifactInfo.getCreationDate() != null) {
            log.error("Creation Date cannot be set ignoring");
        }
        long time = System.currentTimeMillis();
        artifactInfo.setCreationDate(time);
        if (artifactInfo.getLastUpdateDate() != null) {
            log.error("Last Update Date cannot be set ignoring");
        }
        artifactInfo.setLastUpdateDate(time);
        if (artifactInfo.getEsId() != null) {
            log.error("es id cannot be set ignoring");
        }
        artifactInfo.setEsId(null);
    }

    private String composeArtifactId(String resourceId, String artifactId, ArtifactDefinition artifactInfo, String interfaceName,
                                     String operationName) {
        String id = artifactId;
        if (artifactId == null || artifactId.isEmpty()) {
            String uniqueId = null;
            if (interfaceName != null && operationName != null) {
                uniqueId = UniqueIdBuilder
                    .buildArtifactByInterfaceUniqueId(resourceId, interfaceName, operationName, artifactInfo.getArtifactLabel());
            } else {
                uniqueId = UniqueIdBuilder.buildPropertyUniqueId(resourceId, artifactInfo.getArtifactLabel());
            }
            artifactInfo.setUniqueId(uniqueId);
            artifactInfo.setEsId(uniqueId);
            id = uniqueId;
        } else {
            artifactInfo.setUniqueId(artifactId);
            artifactInfo.setEsId(artifactId);
        }
        return id;
    }

    private Either<Boolean, ResponseFormat> validateFirstUpdateHasPayload(ArtifactDefinition artifactInfo, ArtifactDefinition currentArtifact) {
        if (currentArtifact.getEsId() == null && (artifactInfo.getPayloadData() == null || artifactInfo.getPayloadData().length == 0)) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, ARTIFACT_PAYLOAD));
        }
        return Either.left(true);
    }

    @VisibleForTesting
    Either<Boolean, ResponseFormat> validateAndSetArtifactName(ArtifactDefinition artifactInfo) {
        if (artifactInfo.getArtifactName() == null || artifactInfo.getArtifactName().isEmpty()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_ARTIFACT_NAME));
        }
        String normalizeFileName = ValidationUtils.normalizeFileName(artifactInfo.getArtifactName());
        if (normalizeFileName == null || normalizeFileName.isEmpty()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_ARTIFACT_NAME));
        }
        artifactInfo.setArtifactName(normalizeFileName);
        if (!ValidationUtils.validateArtifactNameLength(artifactInfo.getArtifactName())) {
            return Either.right(
                componentsUtils.getResponseFormat(ActionStatus.EXCEEDS_LIMIT, ARTIFACT_NAME, String.valueOf(ValidationUtils.ARTIFACT_NAME_LENGTH)));
        }
        return Either.left(true);
    }

    private void validateArtifactTypeNotChanged(ArtifactDefinition artifactInfo, ArtifactDefinition currentArtifact) {
        if (StringUtils.isEmpty(artifactInfo.getArtifactType())) {
            log.info("artifact type is missing operation ignored");
            throw new ByActionStatusComponentException(ActionStatus.MISSING_ARTIFACT_TYPE);
        }
        if (!currentArtifact.getArtifactType().equalsIgnoreCase(artifactInfo.getArtifactType())) {
            log.info("artifact type cannot be changed operation ignored");
            throw new ByActionStatusComponentException(ActionStatus.INVALID_CONTENT);
        }
    }

    private Either<ArtifactDefinition, ResponseFormat> validateOrSetArtifactGroupType(ArtifactDefinition artifactInfo,
                                                                                      ArtifactDefinition currentArtifact) {
        if (null != artifactInfo && null != currentArtifact) {
            if (artifactInfo.getArtifactGroupType() == null) {
                artifactInfo.setArtifactGroupType(currentArtifact.getArtifactGroupType());
            } else if (!currentArtifact.getArtifactGroupType().getType().equalsIgnoreCase(artifactInfo.getArtifactGroupType().getType())) {
                log.info("artifact group type cannot be changed. operation failed");
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
            }
        }
        return Either.left(artifactInfo);
    }

    private void checkAndSetUnUpdatableFields(User user, ArtifactDefinition artifactInfo, ArtifactDefinition currentArtifact,
                                              ArtifactGroupTypeEnum type) {
        // on update if null add informational to current
        if (currentArtifact.getArtifactGroupType() == null && type != null) {
            currentArtifact.setArtifactGroupType(type);
        }
        if (artifactInfo.getUniqueId() != null && !currentArtifact.getUniqueId().equals(artifactInfo.getUniqueId())) {
            log.error("artifact uniqid cannot be set ignoring");
        }
        artifactInfo.setUniqueId(currentArtifact.getUniqueId());
        if (artifactInfo.getArtifactRef() != null && !currentArtifact.getArtifactRef().equals(artifactInfo.getArtifactRef())) {
            log.error("artifact ref cannot be set ignoring");
        }
        artifactInfo.setArtifactRef(currentArtifact.getArtifactRef());
        if (artifactInfo.getArtifactRepository() != null && !currentArtifact.getArtifactRepository().equals(artifactInfo.getArtifactRepository())) {
            log.error("artifact repository cannot be set ignoring");
        }
        artifactInfo.setArtifactRepository(currentArtifact.getArtifactRepository());
        if (artifactInfo.getUserIdCreator() != null && !currentArtifact.getUserIdCreator().equals(artifactInfo.getUserIdCreator())) {
            log.error("creator uuid cannot be set ignoring");
        }
        artifactInfo.setUserIdCreator(currentArtifact.getUserIdCreator());
        if (artifactInfo.getArtifactCreator() != null && !currentArtifact.getArtifactCreator().equals(artifactInfo.getArtifactCreator())) {
            log.error("artifact creator cannot be set ignoring");
        }
        artifactInfo.setArtifactCreator(currentArtifact.getArtifactCreator());
        if (artifactInfo.getUserIdLastUpdater() != null && !currentArtifact.getUserIdLastUpdater().equals(artifactInfo.getUserIdLastUpdater())) {
            log.error("userId of last updater cannot be set ignoring");
        }
        artifactInfo.setUserIdLastUpdater(user.getUserId());
        if (artifactInfo.getCreatorFullName() != null && !currentArtifact.getCreatorFullName().equals(artifactInfo.getCreatorFullName())) {
            log.error("creator Full name cannot be set ignoring");
        }
        artifactInfo.setCreatorFullName(currentArtifact.getCreatorFullName());
        if (artifactInfo.getUpdaterFullName() != null && !currentArtifact.getUpdaterFullName().equals(artifactInfo.getUpdaterFullName())) {
            log.error("updater Full name cannot be set ignoring");
        }
        String fullName = user.getFirstName() + " " + user.getLastName();
        artifactInfo.setUpdaterFullName(fullName);
        if (artifactInfo.getCreationDate() != null && !currentArtifact.getCreationDate().equals(artifactInfo.getCreationDate())) {
            log.error("Creation Date cannot be set ignoring");
        }
        artifactInfo.setCreationDate(currentArtifact.getCreationDate());
        if (artifactInfo.getLastUpdateDate() != null && !currentArtifact.getLastUpdateDate().equals(artifactInfo.getLastUpdateDate())) {
            log.error("Last Update Date cannot be set ignoring");
        }
        long time = System.currentTimeMillis();
        artifactInfo.setLastUpdateDate(time);
        if (artifactInfo.getEsId() != null && !currentArtifact.getEsId().equals(artifactInfo.getEsId())) {
            log.error("es id cannot be set ignoring");
        }
        artifactInfo.setEsId(currentArtifact.getUniqueId());
        if (artifactInfo.getArtifactDisplayName() != null && !currentArtifact.getArtifactDisplayName()
            .equals(artifactInfo.getArtifactDisplayName())) {
            log.error(" Artifact Display Name cannot be set ignoring");
        }
        artifactInfo.setArtifactDisplayName(currentArtifact.getArtifactDisplayName());
        if (artifactInfo.getServiceApi() != null && !currentArtifact.getServiceApi().equals(artifactInfo.getServiceApi())) {
            log.debug("serviceApi cannot be set. ignoring.");
        }
        artifactInfo.setServiceApi(currentArtifact.getServiceApi());
        if (artifactInfo.getArtifactGroupType() != null && currentArtifact.getArtifactGroupType() != artifactInfo.getArtifactGroupType()) {
            log.debug("artifact group cannot be set. ignoring.");
        }
        artifactInfo.setArtifactGroupType(currentArtifact.getArtifactGroupType());
        artifactInfo.setArtifactVersion(currentArtifact.getArtifactVersion());
        if (artifactInfo.getArtifactUUID() != null && !artifactInfo.getArtifactUUID().isEmpty() && !currentArtifact.getArtifactUUID()
            .equals(artifactInfo.getArtifactUUID())) {
            log.debug("artifact UUID cannot be set. ignoring.");
        }
        artifactInfo.setArtifactUUID(currentArtifact.getArtifactUUID());
        if ((artifactInfo.getHeatParameters() != null) && (currentArtifact.getHeatParameters() != null) && !artifactInfo.getHeatParameters().isEmpty()
            && !currentArtifact.getHeatParameters().isEmpty()) {
            checkAndSetUnupdatableHeatParams(artifactInfo.getListHeatParameters(), currentArtifact.getListHeatParameters());
        }
    }

    private void checkAndSetUnupdatableHeatParams(List<HeatParameterDefinition> heatParameters, List<HeatParameterDefinition> currentParameters) {
        Map<String, HeatParameterDefinition> currentParametersMap = getMapOfParameters(currentParameters);
        for (HeatParameterDefinition parameter : heatParameters) {
            HeatParameterDefinition currentParam = currentParametersMap.get(parameter.getUniqueId());
            if (currentParam != null) {
                if (parameter.getName() != null && !parameter.getName().equalsIgnoreCase(currentParam.getName())) {
                    log.debug("heat parameter name cannot be updated  ({}). ignoring.", parameter.getName());
                    parameter.setName(currentParam.getName());
                }
                if (parameter.getDefaultValue() != null && !parameter.getDefaultValue().equalsIgnoreCase(currentParam.getDefaultValue())) {
                    log.debug("heat parameter defaultValue cannot be updated  ({}). ignoring.", parameter.getDefaultValue());
                    parameter.setDefaultValue(currentParam.getDefaultValue());
                }
                if (parameter.getType() != null && !parameter.getType().equalsIgnoreCase(currentParam.getType())) {
                    log.debug("heat parameter type cannot be updated  ({}). ignoring.", parameter.getType());
                    parameter.setType(currentParam.getType());
                }
                if (parameter.getDescription() != null && !parameter.getDescription().equalsIgnoreCase(currentParam.getDescription())) {
                    log.debug("heat parameter description cannot be updated  ({}). ignoring.", parameter.getDescription());
                    parameter.setDescription(currentParam.getDescription());
                }
                // check and set current value
                if ((parameter.getCurrentValue() == null) && (currentParam.getDefaultValue() != null)) {
                    log.debug("heat parameter current value is null. set it to default value {}). ignoring.", parameter.getDefaultValue());
                    parameter.setCurrentValue(currentParam.getDefaultValue());
                }
            }
        }
    }

    private Map<String, HeatParameterDefinition> getMapOfParameters(List<HeatParameterDefinition> currentParameters) {
        Map<String, HeatParameterDefinition> currentParamsMap = new HashMap<>();
        for (HeatParameterDefinition param : currentParameters) {
            currentParamsMap.put(param.getUniqueId(), param);
        }
        return currentParamsMap;
    }

    private Either<Boolean, ResponseFormat> validateAndServiceApiUrl(ArtifactDefinition artifactInfo) {
        if (!ValidationUtils.validateStringNotEmpty(artifactInfo.getApiUrl())) {
            log.debug("Artifact url cannot be empty.");
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, ARTIFACT_URL));
        }
        artifactInfo.setApiUrl(artifactInfo.getApiUrl().toLowerCase());
        if (!ValidationUtils.validateUrl(artifactInfo.getApiUrl())) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_SERVICE_API_URL));
        }
        if (!ValidationUtils.validateUrlLength(artifactInfo.getApiUrl())) {
            return Either
                .right(componentsUtils.getResponseFormat(ActionStatus.EXCEEDS_LIMIT, ARTIFACT_URL, String.valueOf(ValidationUtils.API_URL_LENGTH)));
        }
        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateAndCleanDescription(ArtifactDefinition artifactInfo) {
        if (artifactInfo.getDescription() == null || artifactInfo.getDescription().isEmpty()) {
            log.debug("Artifact description cannot be empty.");
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, ARTIFACT_DESCRIPTION));
        }
        String description = artifactInfo.getDescription();
        description = ValidationUtils.removeNoneUtf8Chars(description);
        description = ValidationUtils.normaliseWhitespace(description);
        description = ValidationUtils.stripOctets(description);
        description = ValidationUtils.removeHtmlTagsOnly(description);
        if (!ValidationUtils.validateIsEnglish(description)) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
        }
        if (!ValidationUtils.validateLength(description, ValidationUtils.ARTIFACT_DESCRIPTION_MAX_LENGTH)) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.EXCEEDS_LIMIT, ARTIFACT_DESCRIPTION,
                String.valueOf(ValidationUtils.ARTIFACT_DESCRIPTION_MAX_LENGTH)));
        }
        artifactInfo.setDescription(description);
        return Either.left(true);
    }

    private <T> Either<ArtifactDefinition, T> updateArtifactFlow(Component parent, String parentId, String artifactId,
                                                                 ArtifactDefinition artifactInfo, byte[] decodedPayload,
                                                                 ComponentTypeEnum componentType, AuditingActionEnum auditingAction) {
        DAOArtifactData artifactData = createEsArtifactData(artifactInfo, decodedPayload);
        if (artifactData == null) {
            BeEcompErrorManager.getInstance().logBeDaoSystemError(UPDATE_ARTIFACT);
            log.debug("Failed to create artifact object for ES.");
            throw new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR);
        }
        log.debug("Entry on graph is updated. Update artifact in ES");
        // Changing previous and current artifactId for auditing
        String currArtifactId = artifactInfo.getUniqueId();
        NodeTypeEnum parentType = convertParentType(componentType);
        if (decodedPayload == null) {
            if (!artifactInfo.getMandatory() || artifactInfo.getEsId() != null) {
                Either<DAOArtifactData, CassandraOperationStatus> artifactFromCassandra = artifactCassandraDao.getArtifact(artifactInfo.getEsId());
                if (artifactFromCassandra.isRight()) {
                    throw new StorageException(artifactFromCassandra.right().value());
                }
                // clone data to new artifact
                artifactData.setData(artifactFromCassandra.left().value().getData());
                artifactData.setId(artifactFromCassandra.left().value().getId());
            }
        } else if (artifactInfo.getEsId() == null) {
            artifactInfo.setEsId(artifactInfo.getUniqueId());
            artifactData.setId(artifactInfo.getUniqueId());
        }
        Either<ArtifactDefinition, StorageOperationStatus> result = artifactToscaOperation
            .updateArtifactOnResource(artifactInfo, parent, artifactId, parentType, parentId, true);
        if (result.isRight()) {
            throw new StorageException(result.right().value());
        }
        ArtifactDefinition artifactDefinition = result.left().value();
        updateGeneratedIdInHeatEnv(parent, parentId, artifactId, artifactInfo, artifactDefinition, parentType);
        StorageOperationStatus storageOperationStatus = generateCustomizationUUIDOnInstance(parent.getUniqueId(), parentId, componentType);
        if (storageOperationStatus != StorageOperationStatus.OK) {
            throw new StorageException(storageOperationStatus);
        }
        if (artifactData.getData() != null) {
            if (!artifactDefinition.getDuplicated() || artifactData.getId() == null) {
                artifactData.setId(artifactDefinition.getEsId());
            }
            saveArtifactInCassandra(artifactData, parent, artifactInfo, currArtifactId, artifactId, auditingAction, componentType);
        }
        return Either.left(artifactDefinition);
    }

    private String updateGeneratedIdInHeatEnv(Component parent, String parentId, String artifactId, ArtifactDefinition artifactInfo,
                                              ArtifactDefinition artifactDefinition, NodeTypeEnum parentType) {
        if (NodeTypeEnum.Resource == parentType) {
            return updateGeneratedIdInHeatEnv(parent.getDeploymentArtifacts(), parent, parentId, artifactId, artifactInfo, artifactDefinition,
                parentType, false);
        }
        return artifactDefinition.getUniqueId();
    }

    private String updateGeneratedIdInHeatEnv(Map<String, ArtifactDefinition> deploymentArtifacts, Component parentComponent, String parentId,
                                              String artifactId, ArtifactDefinition artifactInfo, ArtifactDefinition artifactDefinition,
                                              NodeTypeEnum parentType, boolean isInstanceArtifact) {
        String artifactUniqueId;
        artifactUniqueId = artifactDefinition.getUniqueId();
        String artifactType = artifactInfo.getArtifactType();
        if ((ArtifactTypeEnum.HEAT.getType().equalsIgnoreCase(artifactType) || ArtifactTypeEnum.HEAT_VOL.getType().equalsIgnoreCase(artifactType)
            || ArtifactTypeEnum.HEAT_NET.getType().equalsIgnoreCase(artifactType)) && !artifactUniqueId.equals(artifactId)) {
            // need to update the generated id in heat env
            Optional<Entry<String, ArtifactDefinition>> findFirst = deploymentArtifacts.entrySet().stream()
                .filter(a -> artifactId.equals(a.getValue().getGeneratedFromId())).findFirst();
            if (findFirst.isPresent()) {
                ArtifactDefinition artifactEnvInfo = findFirst.get().getValue();
                artifactEnvInfo.setIsFromCsar(artifactDefinition.getIsFromCsar());
                artifactEnvInfo.setArtifactChecksum(null);
                if (isInstanceArtifact) {
                    artifactToscaOperation
                        .updateHeatEnvArtifactOnInstance(parentComponent, artifactEnvInfo, artifactId, artifactUniqueId, parentType, parentId);
                } else {
                    artifactToscaOperation
                        .updateHeatEnvArtifact(parentComponent, artifactEnvInfo, artifactId, artifactUniqueId, parentType, parentId);
                }
            }
        }
        return artifactUniqueId;
    }

    private String updateGeneratedIdInHeatEnvOnInstance(ComponentInstance parent, Component parentComponent, String artifactId,
                                                        ArtifactDefinition artifactInfo, ArtifactDefinition artifactDefinition,
                                                        NodeTypeEnum parentType) {
        return updateGeneratedIdInHeatEnv(parent.getDeploymentArtifacts(), parentComponent, parent.getUniqueId(), artifactId, artifactInfo,
            artifactDefinition, parentType, true);
    }

    @VisibleForTesting
    private Either<byte[], ResponseFormat> handlePayload(ArtifactDefinition artifactInfo, boolean isArtifactMetadataUpdate) {
        log.trace("Starting payload handling");
        byte[] payload = artifactInfo.getPayloadData();
        byte[] decodedPayload = null;
        if (payload != null && payload.length != 0) {
            // the generated artifacts were already decoded by the handler
            decodedPayload = artifactInfo.getGenerated() ? payload : Base64.decodeBase64(payload);
            if (decodedPayload.length == 0) {
                log.debug("Failed to decode the payload.");
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
                return Either.right(responseFormat);
            }
            String checkSum = GeneralUtility.calculateMD5Base64EncodedByByteArray(decodedPayload);
            artifactInfo.setArtifactChecksum(checkSum);
            log.trace("Calculated checksum, base64 payload: {},  checksum: {}", payload, checkSum);
            // Specific payload validations of different types
            Either<Boolean, ResponseFormat> result = Either.left(true);
            if (isDeploymentArtifact(artifactInfo)) {
                log.trace("Starting deployment artifacts payload validation");
                String artifactType = artifactInfo.getArtifactType();
                String fileExtension = GeneralUtility.getFilenameExtension(artifactInfo.getArtifactName());
                PayloadTypeEnum payloadType = ArtifactTypeToPayloadTypeSelector.getPayloadType(artifactType, fileExtension);
                final Optional<ResponseFormat> pmDictionaryError = validateIfPmDictionary(artifactType, decodedPayload);
                if (pmDictionaryError.isPresent()) {
                    return Either.right(pmDictionaryError.get());
                }
                Either<Boolean, ActionStatus> isPayloadValid = payloadType.isValid(decodedPayload);
                if (isPayloadValid.isRight()) {
                    ResponseFormat responseFormat = componentsUtils.getResponseFormat(isPayloadValid.right().value(), artifactType);
                    return Either.right(responseFormat);
                }
                if (payloadType.isHeatRelated()) {
                    log.trace("Payload is heat related so going to extract heat parameters for artifact type {}", artifactType);
                    result = extractHeatParameters(artifactInfo);
                }
            }
            if (result.isRight()) {
                return Either.right(result.right().value());
            }
        } // null/empty payload is normal if called from metadata update ONLY.

        // The validation of whether this is metadata/payload update case is

        // currently done separately
        else {
            if (!isArtifactMetadataUpdate) {
                log.debug("In artifact: {} Payload is missing.", artifactInfo.getArtifactName());
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, ARTIFACT_PAYLOAD);
                return Either.right(responseFormat);
            }
        }
        log.trace("Ended payload handling");
        return Either.left(decodedPayload);
    }

    private Optional<ResponseFormat> validateIfPmDictionary(String artifactType, byte[] decodedPayload) {
        return new PMDictionaryValidator().validateIfPmDictionary(artifactType, decodedPayload).map(this::preparePmDictionaryResponse);
    }

    private ResponseFormat preparePmDictionaryResponse(String errorMessage) {
        return componentsUtils.getResponseFormat(ActionStatus.INVALID_PM_DICTIONARY_FILE, errorMessage);
    }

    public Either<ArtifactDefinition, ResponseFormat> deleteArtifactByInterface(String resourceId, String userUserId, String artifactId,
                                                                                boolean inTransaction) {
        return toscaOperationFacade.getToscaElement(resourceId, JsonParseFlagEnum.ParseMetadata).right().map(componentsUtils.toResponseFormat())
            .left().bind(parentComponent -> {
                User user = new User(userUserId);
                return handleDelete(resourceId, artifactId, user, parentComponent, false, inTransaction);
            });
    }

    private Operation convertToOperation(ArtifactDefinition artifactInfo, String operationName) {
        Operation op = new Operation();
        long time = System.currentTimeMillis();
        op.setCreationDate(time);
        String artifactName = artifactInfo.getArtifactName();
        artifactInfo.setArtifactName(createInterfaceArtifactNameFromOperation(operationName, artifactName));
        op.setImplementation(artifactInfo);
        op.setLastUpdateDate(time);
        return op;
    }

    private String createInterfaceArtifactNameFromOperation(String operationName, String artifactName) {
        String newArtifactName = operationName + "_" + artifactName;
        log.trace("converting artifact name {} to {}", artifactName, newArtifactName);
        return newArtifactName;
    }

    // download by MSO
    public byte[] downloadRsrcArtifactByNames(String serviceName, String serviceVersion, String resourceName, String resourceVersion,
                                              String artifactName) {
        // General validation
        if (serviceName == null || serviceVersion == null || resourceName == null || resourceVersion == null || artifactName == null) {
            log.debug(NULL_PARAMETER);
            throw new ByActionStatusComponentException(ActionStatus.INVALID_CONTENT);
        }
        // Normalizing artifact name
        artifactName = ValidationUtils.normalizeFileName(artifactName);
        // Resource validation
        Resource resource = validateResourceNameAndVersion(resourceName, resourceVersion);
        String resourceId = resource.getUniqueId();
        // Service validation
        Service validateServiceNameAndVersion = validateServiceNameAndVersion(serviceName, serviceVersion);
        Map<String, ArtifactDefinition> artifacts = resource.getDeploymentArtifacts();
        if (artifacts == null || artifacts.isEmpty()) {
            log.debug("Deployment artifacts of resource {} are not found", resourceId);
            throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_NOT_FOUND, artifactName);
        }
        ArtifactDefinition deploymentArtifact = null;
        for (ArtifactDefinition artifactDefinition : artifacts.values()) {
            if (artifactDefinition.getArtifactName() != null && artifactDefinition.getArtifactName().equals(artifactName)) {
                log.debug(FOUND_DEPLOYMENT_ARTIFACT, artifactName);
                deploymentArtifact = artifactDefinition;
                break;
            }
        }
        if (deploymentArtifact == null) {
            log.debug("No deployment artifact {} was found for resource {}", artifactName, resourceId);
            throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_NOT_FOUND, artifactName);
        }
        // Downloading the artifact
        ImmutablePair<String, byte[]> downloadArtifactEither = downloadArtifact(deploymentArtifact);
        log.trace("Download of resource artifact succeeded, uniqueId {}", deploymentArtifact.getUniqueId());
        return downloadArtifactEither.getRight();
    }

    // download by MSO
    public byte[] downloadRsrcInstArtifactByNames(String serviceName, String serviceVersion, String resourceInstanceName, String artifactName) {
        // General validation
        if (serviceName == null || serviceVersion == null || resourceInstanceName == null || artifactName == null) {
            log.debug(NULL_PARAMETER);
            throw new ByActionStatusComponentException(ActionStatus.INVALID_CONTENT);
        }
        // Normalizing artifact name
        artifactName = ValidationUtils.normalizeFileName(artifactName);
        // Service validation
        Service service = validateServiceNameAndVersion(serviceName, serviceVersion);
        // ResourceInstance validation
        ComponentInstance resourceInstance = validateResourceInstance(service, resourceInstanceName);
        Map<String, ArtifactDefinition> artifacts = resourceInstance.getDeploymentArtifacts();
        final String finalArtifactName = artifactName;
        Predicate<ArtifactDefinition> filterArtifactByName = p -> p.getArtifactName().equals(finalArtifactName);
        ArtifactDefinition deployableArtifact =
            artifacts == null ? null : artifacts.values().stream().filter(filterArtifactByName).findFirst().orElse(null);
        if (deployableArtifact == null) {
            log.debug("Deployment artifact with name {} not found", artifactName);
            throw new ByResponseFormatComponentException(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, artifactName));
        }
        log.debug(FOUND_DEPLOYMENT_ARTIFACT, artifactName);
        ImmutablePair<String, byte[]> downloadArtifactEither = downloadArtifact(deployableArtifact);
        log.trace("Download of resource artifact succeeded, uniqueId {}", deployableArtifact.getUniqueId());
        return downloadArtifactEither.getRight();
    }

    private ComponentInstance validateResourceInstance(Service service, String resourceInstanceName) {
        List<ComponentInstance> riList = service.getComponentInstances();
        for (ComponentInstance ri : riList) {
            if (ri.getNormalizedName().equals(resourceInstanceName)) {
                return ri;
            }
        }
        throw new ByActionStatusComponentException(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND, resourceInstanceName);
    }

    private ComponentInstance validateResourceInstanceById(Component component, String resourceInstanceId) {
        List<ComponentInstance> riList = component.getComponentInstances();
        for (ComponentInstance ri : riList) {
            if (ri.getUniqueId().equals(resourceInstanceId)) {
                return ri;
            }
        }
        throw new ByActionStatusComponentException(ActionStatus.RESOURCE_NOT_FOUND, resourceInstanceId);
    }

    private Service validateServiceNameAndVersion(String serviceName, String serviceVersion) {
        Either<List<Service>, StorageOperationStatus> serviceListBySystemName = toscaOperationFacade
            .getBySystemName(ComponentTypeEnum.SERVICE, serviceName);
        if (serviceListBySystemName.isRight()) {
            log.debug("Couldn't fetch any service with name {}", serviceName);
            throw new ByActionStatusComponentException(
                componentsUtils.convertFromStorageResponse(serviceListBySystemName.right().value(), ComponentTypeEnum.SERVICE), serviceName);
        }
        List<Service> serviceList = serviceListBySystemName.left().value();
        if (serviceList == null || serviceList.isEmpty()) {
            log.debug("Couldn't fetch any service with name {}", serviceName);
            throw new ByActionStatusComponentException(ActionStatus.SERVICE_NOT_FOUND, serviceName);
        }
        Service foundService = null;
        for (Service service : serviceList) {
            if (service.getVersion().equals(serviceVersion)) {
                log.trace("Found service with version {}", serviceVersion);
                foundService = service;
                break;
            }
        }
        if (foundService == null) {
            log.debug("Couldn't find version {} for service {}", serviceVersion, serviceName);
            throw new ByActionStatusComponentException(ActionStatus.COMPONENT_VERSION_NOT_FOUND, ComponentTypeEnum.SERVICE.getValue(),
                serviceVersion);
        }
        return foundService;
    }

    private Resource validateResourceNameAndVersion(String resourceName, String resourceVersion) {
        Either<Resource, StorageOperationStatus> resourceListBySystemName = toscaOperationFacade
            .getComponentByNameAndVersion(ComponentTypeEnum.RESOURCE, resourceName, resourceVersion, JsonParseFlagEnum.ParseMetadata);
        if (resourceListBySystemName.isRight()) {
            log.debug("Couldn't fetch any resource with name {} and version {}. ", resourceName, resourceVersion);
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(resourceListBySystemName.right().value()),
                resourceName);
        }
        return resourceListBySystemName.left().value();
    }

    public byte[] downloadServiceArtifactByNames(String serviceName, String serviceVersion, String artifactName) {
        // Validation
        log.trace("Starting download of service interface artifact, serviceName {}, serviceVersion {}, artifact name {}", serviceName, serviceVersion,
            artifactName);
        if (serviceName == null || serviceVersion == null || artifactName == null) {
            log.debug(NULL_PARAMETER);
            throw new ByActionStatusComponentException(ActionStatus.INVALID_CONTENT);
        }
        // Normalizing artifact name
        final String normalizedArtifactName = ValidationUtils.normalizeFileName(artifactName);
        // Service validation
        Service service = validateServiceNameAndVersion(serviceName, serviceVersion);
        // Looking for deployment or tosca artifacts
        String serviceId = service.getUniqueId();
        if (MapUtils.isEmpty(service.getDeploymentArtifacts()) && MapUtils.isEmpty(service.getToscaArtifacts())) {
            log.debug("Neither Deployment nor Tosca artifacts of service {} are found", serviceId);
            throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_NOT_FOUND, normalizedArtifactName);
        }
        Optional<ArtifactDefinition> foundArtifactOptl = Optional.empty();
        if (!MapUtils.isEmpty(service.getDeploymentArtifacts())) {
            foundArtifactOptl = service.getDeploymentArtifacts().values().stream()
                // filters artifact by name
                .filter(a -> a.getArtifactName().equals(normalizedArtifactName)).findAny();
        }
        if ((!foundArtifactOptl.isPresent()) && !MapUtils.isEmpty(service.getToscaArtifacts())) {
            foundArtifactOptl = service.getToscaArtifacts().values().stream()
                // filters TOSCA artifact by name
                .filter(a -> a.getArtifactName().equals(normalizedArtifactName)).findAny();
        }
        if (!foundArtifactOptl.isPresent()) {
            log.debug("The artifact {} was not found for service {}", normalizedArtifactName, serviceId);
            throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_NOT_FOUND, normalizedArtifactName);
        }
        log.debug(FOUND_DEPLOYMENT_ARTIFACT, normalizedArtifactName);
        // Downloading the artifact
        ImmutablePair<String, byte[]> downloadArtifactEither = downloadArtifact(foundArtifactOptl.get());
        log.trace("Download of service artifact succeeded, uniqueId {}", foundArtifactOptl.get().getUniqueId());
        return downloadArtifactEither.getRight();
    }

    public ImmutablePair<String, byte[]> downloadArtifact(String parentId, String artifactUniqueId) {
        log.trace("Starting download of artifact, uniqueId {}", artifactUniqueId);
        Either<ArtifactDefinition, StorageOperationStatus> artifactById = artifactToscaOperation.getArtifactById(parentId, artifactUniqueId);
        if (artifactById.isRight()) {
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(artifactById.right().value());
            log.debug("Error when getting artifact info by id{}, error: {}", artifactUniqueId, actionStatus);
            throw new ByResponseFormatComponentException(componentsUtils.getResponseFormatByArtifactId(actionStatus, ""));
        }
        ArtifactDefinition artifactDefinition = artifactById.left().value();
        if (artifactDefinition == null) {
            log.debug("Empty artifact definition returned from DB by artifact id {}", artifactUniqueId);
            throw new ByResponseFormatComponentException(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, ""));
        }
        return downloadArtifact(artifactDefinition);
    }

    private Component validateComponentExists(String componentId, AuditingActionEnum auditingAction, User user, String artifactId,
                                              ComponentTypeEnum componentType, String containerComponentType) {
        ComponentTypeEnum componentForAudit =
            null == containerComponentType ? componentType : ComponentTypeEnum.findByParamName(containerComponentType);
        componentForAudit.getNodeType();
        Either<? extends Component, StorageOperationStatus> componentResult = toscaOperationFacade.getToscaFullElement(componentId);
        if (componentResult.isRight()) {
            ActionStatus status = componentForAudit == ComponentTypeEnum.RESOURCE ? ActionStatus.RESOURCE_NOT_FOUND
                : componentForAudit == ComponentTypeEnum.SERVICE ? ActionStatus.SERVICE_NOT_FOUND : ActionStatus.PRODUCT_NOT_FOUND;
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(status, componentId);
            log.debug("Service not found, serviceId {}", componentId);
            handleAuditing(auditingAction, null, componentId, user, null, null, artifactId, responseFormat, componentForAudit, null);
            throw new ByActionStatusComponentException(status, componentId);
        }
        return componentResult.left().value();
    }

    private void validateWorkOnComponent(Component component, String userId, AuditingActionEnum auditingAction, User user, String artifactId,
                                         ArtifactOperationInfo operation) {
        if (operation.getArtifactOperationEnum() != ArtifactOperationEnum.DOWNLOAD && !operation.ignoreLifecycleState()) {
            try {
                validateCanWorkOnComponent(component, userId);
            } catch (ComponentException e) {
                String uniqueId = component.getUniqueId();
                log.debug("Service status isn't  CHECKOUT or user isn't owner, serviceId {}", uniqueId);
                handleAuditing(auditingAction, component, uniqueId, user, null, null, artifactId, e.getResponseFormat(), component.getComponentType(),
                    null);
                throw e;
            }
        }
    }

    private void validateUserRole(User user, AuditingActionEnum auditingAction, String componentId, String artifactId,
                                  ComponentTypeEnum componentType, ArtifactOperationInfo operation) {
        if (operation.isNotDownload()) {
            String role = user.getRole();
            if (!role.equals(Role.ADMIN.name()) && !role.equals(Role.DESIGNER.name())) {
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
                log.debug("addArtifact - user isn't permitted to perform operation, userId {}, role {}", user.getUserId(), role);
                handleAuditing(auditingAction, null, componentId, user, null, null, artifactId, responseFormat, componentType, null);
                throw new ByActionStatusComponentException(ActionStatus.RESTRICTED_OPERATION);
            }
        }
    }

    private User validateUserExists(String userId, AuditingActionEnum auditingAction, String componentId, String artifactId,
                                    ComponentTypeEnum componentType, boolean inTransaction) {
        User user;
        try {
            user = validateUserExists(userId);
        } catch (ByResponseFormatComponentException e) {
            ResponseFormat responseFormat = e.getResponseFormat();
            handleComponentException(auditingAction, componentId, artifactId, responseFormat, componentType, userId);
            throw e;
        } catch (ByActionStatusComponentException e) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams());
            handleComponentException(auditingAction, componentId, artifactId, responseFormat, componentType, userId);
            throw e;
        }
        return user;
    }

    private void handleComponentException(AuditingActionEnum auditingAction, String componentId, String artifactId, ResponseFormat responseFormat,
                                          ComponentTypeEnum componentType, String userId) {
        User user = new User();
        user.setUserId(userId);
        handleAuditing(auditingAction, null, componentId, user, null, null, artifactId, responseFormat, componentType, null);
    }

    protected AuditingActionEnum detectAuditingType(ArtifactOperationInfo operation, String origMd5) {
        AuditingActionEnum auditingAction = null;
        switch (operation.getArtifactOperationEnum()) {
            case CREATE:
                auditingAction = operation.isExternalApi() ? AuditingActionEnum.ARTIFACT_UPLOAD_BY_API : AuditingActionEnum.ARTIFACT_UPLOAD;
                break;
            case UPDATE:
                auditingAction = operation.isExternalApi() ? AuditingActionEnum.ARTIFACT_UPLOAD_BY_API
                    : origMd5 == null ? AuditingActionEnum.ARTIFACT_METADATA_UPDATE : AuditingActionEnum.ARTIFACT_PAYLOAD_UPDATE;
                break;
            case DELETE:
                auditingAction = operation.isExternalApi() ? AuditingActionEnum.ARTIFACT_DELETE_BY_API : AuditingActionEnum.ARTIFACT_DELETE;
                break;
            case DOWNLOAD:
                auditingAction = operation.isExternalApi() ? AuditingActionEnum.DOWNLOAD_ARTIFACT : AuditingActionEnum.ARTIFACT_DOWNLOAD;
                break;
            default:
                break;
        }
        return auditingAction;
    }

    private ImmutablePair<String, byte[]> downloadArtifact(ArtifactDefinition artifactDefinition) {
        String esArtifactId = artifactDefinition.getEsId();
        Either<DAOArtifactData, CassandraOperationStatus> artifactfromES = artifactCassandraDao.getArtifact(esArtifactId);
        if (artifactfromES.isRight()) {
            CassandraOperationStatus resourceUploadStatus = artifactfromES.right().value();
            StorageOperationStatus storageResponse = DaoStatusConverter.convertCassandraStatusToStorageStatus(resourceUploadStatus);
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(storageResponse);
            log.debug("Error when getting artifact from ES, error: {}", actionStatus);
            throw new ByActionStatusComponentException(actionStatus, artifactDefinition.getArtifactDisplayName());
        }
        DAOArtifactData DAOArtifactData = artifactfromES.left().value();
        byte[] data = DAOArtifactData.getDataAsArray();
        if (data == null) {
            log.debug("Artifact data from cassandra is null");
            throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_NOT_FOUND, artifactDefinition.getArtifactDisplayName());
        }
        String artifactName = artifactDefinition.getArtifactName();
        log.trace("Download of artifact succeeded, uniqueId {}, artifact file name {}", artifactDefinition.getUniqueId(), artifactName);
        return new ImmutablePair<>(artifactName, data);
    }

    public DAOArtifactData createEsArtifactData(ArtifactDataDefinition artifactInfo, byte[] artifactPayload) {
        return new DAOArtifactData(artifactInfo.getEsId(), artifactPayload);
    }

    private void saveArtifactInCassandra(DAOArtifactData artifactData, Component parent, ArtifactDefinition artifactInfo, String currArtifactId,
                                         String prevArtifactId, AuditingActionEnum auditingAction, ComponentTypeEnum componentType) {
        CassandraOperationStatus resourceUploadStatus = artifactCassandraDao.saveArtifact(artifactData);
        if (resourceUploadStatus == CassandraOperationStatus.OK) {
            log.debug("Artifact {} was saved in component {}.", artifactData.getId(), parent.getUniqueId());
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
            handleAuditing(auditingAction, parent, parent.getUniqueId(), null, artifactInfo, prevArtifactId, currArtifactId, responseFormat,
                componentType, null);
        } else {
            BeEcompErrorManager.getInstance().logBeDaoSystemError(UPDATE_ARTIFACT);
            log.info(FAILED_SAVE_ARTIFACT);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            handleAuditing(auditingAction, parent, parent.getUniqueId(), null, artifactInfo, prevArtifactId, currArtifactId, responseFormat,
                componentType, null);
            throw new StorageException(resourceUploadStatus);
        }
    }

    private boolean isArtifactMetadataUpdate(AuditingActionEnum auditingActionEnum) {
        return auditingActionEnum == AuditingActionEnum.ARTIFACT_METADATA_UPDATE;
    }

    private boolean isDeploymentArtifact(ArtifactDefinition artifactInfo) {
        return ArtifactGroupTypeEnum.DEPLOYMENT == artifactInfo.getArtifactGroupType();
    }

    private boolean isInformationalArtifact(final ArtifactDefinition artifactInfo) {
        return ArtifactGroupTypeEnum.INFORMATIONAL == artifactInfo.getArtifactGroupType();
    }

    private boolean isHeatArtifact(final ArtifactDefinition artifactInfo) {
        final String artifactType = artifactInfo.getArtifactType();
        final ArtifactTypeEnum artifactTypeEnum = ArtifactTypeEnum.parse(artifactType);
        if (artifactTypeEnum == null) {
            artifactInfo.setTimeout(NodeTemplateOperation.NON_HEAT_TIMEOUT);
            return false;
        }
        switch (artifactTypeEnum) {
            case HEAT:
            case HEAT_VOL:
            case HEAT_NET:
            case HEAT_ENV:
                return true;
            default:
                return false;
        }
    }

    public ArtifactDefinition createArtifactPlaceHolderInfo(String resourceId, String logicalName, Map<String, Object> artifactInfoMap,
                                                            String userUserId, ArtifactGroupTypeEnum groupType, boolean inTransaction) {
        User user = userBusinessLogic.getUser(userUserId, inTransaction);
        return createArtifactPlaceHolderInfo(resourceId, logicalName, artifactInfoMap, user, groupType);
    }

    public ArtifactDefinition createArtifactPlaceHolderInfo(String resourceId, String logicalName, Map<String, Object> artifactInfoMap, User user,
                                                            ArtifactGroupTypeEnum groupType) {
        ArtifactDefinition artifactInfo = new ArtifactDefinition();
        String artifactName = (String) artifactInfoMap.get(ARTIFACT_PLACEHOLDER_DISPLAY_NAME);
        String artifactType = (String) artifactInfoMap.get(ARTIFACT_PLACEHOLDER_TYPE);
        String artifactDescription = (String) artifactInfoMap.get(ARTIFACT_PLACEHOLDER_DESCRIPTION);
        artifactInfo.setArtifactDisplayName(artifactName);
        artifactInfo.setArtifactLabel(logicalName.toLowerCase());
        artifactInfo.setArtifactType(artifactType);
        artifactInfo.setDescription(artifactDescription);
        artifactInfo.setArtifactGroupType(groupType);
        nodeTemplateOperation.setDefaultArtifactTimeout(groupType, artifactInfo);
        setArtifactPlaceholderCommonFields(resourceId, user, artifactInfo);
        return artifactInfo;
    }

    private void setArtifactPlaceholderCommonFields(String resourceId, User user, ArtifactDefinition artifactInfo) {
        String uniqueId = null;
        if (resourceId != null) {
            uniqueId = UniqueIdBuilder.buildPropertyUniqueId(resourceId.toLowerCase(), artifactInfo.getArtifactLabel().toLowerCase());
            artifactInfo.setUniqueId(uniqueId);
        }
        artifactInfo.setUserIdCreator(user.getUserId());
        String fullName = user.getFullName();
        artifactInfo.setUpdaterFullName(fullName);
        long time = System.currentTimeMillis();
        artifactInfo.setCreatorFullName(fullName);
        artifactInfo.setCreationDate(time);
        artifactInfo.setLastUpdateDate(time);
        artifactInfo.setUserIdLastUpdater(user.getUserId());
        artifactInfo.setMandatory(true);
    }

    public Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getArtifacts(String parentId, NodeTypeEnum parentType,
                                                                                        ArtifactGroupTypeEnum groupType, String instanceId) {
        return artifactToscaOperation.getArtifacts(parentId, parentType, groupType, instanceId);
    }

    public Either<ArtifactDefinition, StorageOperationStatus> addHeatEnvArtifact(ArtifactDefinition artifactHeatEnv, ArtifactDefinition artifact,
                                                                                 Component component, NodeTypeEnum parentType, String instanceId) {
        return artifactToscaOperation.addHeatEnvArtifact(artifactHeatEnv, artifact, component, parentType, true, instanceId);
    }

    private Either<DAOArtifactData, ResponseFormat> createEsHeatEnvArtifactDataFromString(ArtifactDefinition artifactDefinition, String payloadStr) {
        byte[] payload = payloadStr.getBytes();
        DAOArtifactData artifactData = createEsArtifactData(artifactDefinition, payload);
        return Either.left(artifactData);
    }

    /**
     * @param artifactDefinition
     * @return
     */
    public Either<ArtifactDefinition, ResponseFormat> generateHeatEnvArtifact(ArtifactDefinition artifactDefinition, ComponentTypeEnum componentType,
                                                                              Component component, String resourceInstanceName, User modifier,
                                                                              String instanceId, boolean shouldLock, boolean inTransaction) {
        String payload = generateHeatEnvPayload(artifactDefinition);
        String prevUUID = artifactDefinition.getArtifactUUID();
        ArtifactDefinition clonedBeforeGenerate = new ArtifactDefinition(artifactDefinition);
        return generateAndSaveHeatEnvArtifact(artifactDefinition, payload, componentType, component, resourceInstanceName, modifier, instanceId,
            shouldLock, inTransaction).left()
            .bind(artifactDef -> updateArtifactOnGroupInstance(component, instanceId, prevUUID, clonedBeforeGenerate, artifactDef));
    }

    public Either<ArtifactDefinition, ResponseFormat> forceGenerateHeatEnvArtifact(ArtifactDefinition artifactDefinition,
                                                                                   ComponentTypeEnum componentType, Component component,
                                                                                   String resourceInstanceName, User modifier, boolean shouldLock,
                                                                                   boolean inTransaction, String instanceId) {
        String payload = generateHeatEnvPayload(artifactDefinition);
        String prevUUID = artifactDefinition.getArtifactUUID();
        ArtifactDefinition clonedBeforeGenerate = new ArtifactDefinition(artifactDefinition);
        return forceGenerateAndSaveHeatEnvArtifact(artifactDefinition, payload, componentType, component, resourceInstanceName, modifier, instanceId,
            shouldLock, inTransaction).left()
            .bind(artifactDef -> updateArtifactOnGroupInstance(component, instanceId, prevUUID, clonedBeforeGenerate, artifactDef));
    }

    @VisibleForTesting
    Either<ArtifactDefinition, ResponseFormat> updateArtifactOnGroupInstance(Component component, String instanceId, String prevUUID,
                                                                             ArtifactDefinition clonedBeforeGenerate,
                                                                             ArtifactDefinition updatedArtDef) {
        if (prevUUID == null || !prevUUID.equals(updatedArtDef.getArtifactUUID())) {
            List<ComponentInstance> componentInstances = component.getComponentInstances();
            if (componentInstances != null) {
                Optional<ComponentInstance> findFirst = componentInstances.stream().filter(ci -> ci.getUniqueId().equals(instanceId)).findFirst();
                if (findFirst.isPresent()) {
                    ComponentInstance relevantInst = findFirst.get();
                    List<GroupInstance> updatedGroupInstances = getUpdatedGroupInstances(updatedArtDef.getUniqueId(), clonedBeforeGenerate,
                        relevantInst.getGroupInstances());
                    if (CollectionUtils.isNotEmpty(updatedGroupInstances)) {
                        updatedGroupInstances.forEach(gi -> {
                            gi.getGroupInstanceArtifacts().add(updatedArtDef.getUniqueId());
                            gi.getGroupInstanceArtifactsUuid().add(updatedArtDef.getArtifactUUID());
                        });
                        Either<List<GroupInstance>, StorageOperationStatus> status = toscaOperationFacade
                            .updateGroupInstancesOnComponent(component, instanceId, updatedGroupInstances);
                        if (status.isRight()) {
                            log.debug(FAILED_UPDATE_GROUPS, component.getUniqueId());
                            ResponseFormat responseFormat = componentsUtils
                                .getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(status.right().value()),
                                    clonedBeforeGenerate.getArtifactDisplayName());
                            return Either.right(responseFormat);
                        }
                    }
                }
            }
        }
        return Either.left(updatedArtDef);
    }

    private String generateHeatEnvPayload(ArtifactDefinition artifactDefinition) {
        List<HeatParameterDefinition> heatParameters = artifactDefinition.getListHeatParameters();
        StringBuilder sb = new StringBuilder();
        sb.append(ConfigurationManager.getConfigurationManager().getConfiguration().getHeatEnvArtifactHeader());
        sb.append("parameters:\n");
        if (heatParameters != null) {
            heatParameters.sort(Comparator.comparing(HeatParameterDataDefinition::getName));
            List<HeatParameterDefinition> empltyHeatValues = new ArrayList<>();
            for (HeatParameterDefinition heatParameterDefinition : heatParameters) {
                String heatValue = heatParameterDefinition.getCurrentValue();
                if (!ValidationUtils.validateStringNotEmpty(heatValue)) {
                    heatValue = heatParameterDefinition.getDefaultValue();
                    if (!ValidationUtils.validateStringNotEmpty(heatValue)) {
                        empltyHeatValues.add(heatParameterDefinition);
                        continue;
                    }
                }
                HeatParameterType type = HeatParameterType.isValidType(heatParameterDefinition.getType());
                if (type != null) {
                    switch (type) {
                        case BOOLEAN:
                            sb.append("  ").append(heatParameterDefinition.getName()).append(":").append(" ").append(Boolean.parseBoolean(heatValue))
                                .append("\n");
                            break;
                        case NUMBER:
                            sb.append("  ").append(heatParameterDefinition.getName()).append(":").append(" ")
                                .append(new BigDecimal(heatValue).toPlainString()).append("\n");
                            break;
                        case COMMA_DELIMITED_LIST:
                        case JSON:
                            sb.append("  ").append(heatParameterDefinition.getName()).append(":").append(" ").append(heatValue).append("\n");
                            break;
                        default:
                            String value = heatValue;
                            boolean starts = value.startsWith("\"");
                            boolean ends = value.endsWith("\"");
                            if (!(starts && ends)) {
                                starts = value.startsWith("'");
                                ends = value.endsWith("'");
                                if (!(starts && ends)) {
                                    value = "\"" + value + "\"";
                                }
                            }
                            sb.append("  ").append(heatParameterDefinition.getName()).append(":").append(" ").append(value);
                            sb.append("\n");
                            break;
                    }
                }
            }
            if (!empltyHeatValues.isEmpty()) {
                empltyHeatValues.sort(Comparator.comparing(HeatParameterDataDefinition::getName));
                empltyHeatValues.forEach(hv -> {
                    sb.append("  ").append(hv.getName()).append(":");
                    HeatParameterType type = HeatParameterType.isValidType(hv.getType());
                    if (type != null && type == HeatParameterType.STRING && (hv.getCurrentValue() != null && "".equals(hv.getCurrentValue())
                        || hv.getDefaultValue() != null && "".equals(hv.getDefaultValue()))) {
                        sb.append(" \"\"").append("\n");
                    } else {
                        sb.append(" ").append("\n");
                    }
                });
            }
        }
        sb.append(ConfigurationManager.getConfigurationManager().getConfiguration().getHeatEnvArtifactFooter());
        // DE265919 fix
        return sb.toString().replace("\\\\n", "\n");
    }

    /**
     * @param artifactDefinition
     * @param payload
     * @return
     */
    public Either<ArtifactDefinition, ResponseFormat> generateAndSaveHeatEnvArtifact(ArtifactDefinition artifactDefinition, String payload,
                                                                                     ComponentTypeEnum componentType, Component component,
                                                                                     String resourceInstanceName, User modifier, String instanceId,
                                                                                     boolean shouldLock, boolean inTransaction) {
        return generateArtifactPayload(artifactDefinition, componentType, component, resourceInstanceName, modifier, shouldLock, inTransaction,
            artifactDefinition::getHeatParamsUpdateDate, () -> createEsHeatEnvArtifactDataFromString(artifactDefinition, payload), instanceId);
    }

    public Either<ArtifactDefinition, ResponseFormat> forceGenerateAndSaveHeatEnvArtifact(ArtifactDefinition artifactDefinition, String payload,
                                                                                          ComponentTypeEnum componentType, Component component,
                                                                                          String resourceInstanceName, User modifier,
                                                                                          String instanceId, boolean shouldLock,
                                                                                          boolean inTransaction) {
        return generateArtifactPayload(artifactDefinition, componentType, component, resourceInstanceName, modifier, shouldLock, inTransaction,
            System::currentTimeMillis, () -> createEsHeatEnvArtifactDataFromString(artifactDefinition, payload), instanceId);
    }

    protected Either<ArtifactDefinition, ResponseFormat> generateArtifactPayload(ArtifactDefinition artifactDefinition,
                                                                                 ComponentTypeEnum componentType, Component component,
                                                                                 String resourceInstanceName, User modifier, boolean shouldLock,
                                                                                 boolean inTransaction, Supplier<Long> payloadUpdateDateGen,
                                                                                 Supplier<Either<DAOArtifactData, ResponseFormat>> esDataCreator,
                                                                                 String instanceId) {
        log.trace("Start generating payload for {} artifact {}", artifactDefinition.getArtifactType(), artifactDefinition.getEsId());
        if (artifactDefinition.getPayloadUpdateDate() == null || artifactDefinition.getPayloadUpdateDate() == 0
            || artifactDefinition.getPayloadUpdateDate() <= payloadUpdateDateGen.get()) {
            log.trace("Generating payload for {} artifact {}", artifactDefinition.getArtifactType(), artifactDefinition.getEsId());
            Either<DAOArtifactData, ResponseFormat> artifactDataRes = esDataCreator.get();
            DAOArtifactData artifactData = null;
            if (artifactDataRes.isLeft()) {
                artifactData = artifactDataRes.left().value();
            } else {
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
                handleAuditing(AuditingActionEnum.ARTIFACT_PAYLOAD_UPDATE, component, component.getUniqueId(), modifier, artifactDefinition,
                    artifactDefinition.getUniqueId(), artifactDefinition.getUniqueId(), responseFormat, ComponentTypeEnum.RESOURCE_INSTANCE,
                    resourceInstanceName);
                return Either.right(artifactDataRes.right().value());
            }
            String newCheckSum = GeneralUtility.calculateMD5Base64EncodedByByteArray(artifactData.getDataAsArray());
            String oldCheckSum;
            String esArtifactId = artifactDefinition.getEsId();
            Either<DAOArtifactData, CassandraOperationStatus> artifactfromES;
            DAOArtifactData DAOArtifactData;
            if (esArtifactId != null && !esArtifactId.isEmpty() && artifactDefinition.getPayloadData() == null) {
                log.debug("Try to fetch artifact from cassandra with id : {}", esArtifactId);
                artifactfromES = artifactCassandraDao.getArtifact(esArtifactId);
                if (artifactfromES.isRight()) {
                    CassandraOperationStatus resourceUploadStatus = artifactfromES.right().value();
                    StorageOperationStatus storageResponse = DaoStatusConverter.convertCassandraStatusToStorageStatus(resourceUploadStatus);
                    ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(storageResponse);
                    log.debug("Error when getting artifact from ES, error: {} esid : {}", actionStatus, esArtifactId);
                    return Either.right(componentsUtils.getResponseFormatByArtifactId(actionStatus, artifactDefinition.getArtifactDisplayName()));
                }
                DAOArtifactData = artifactfromES.left().value();
                oldCheckSum = GeneralUtility.calculateMD5Base64EncodedByByteArray(DAOArtifactData.getDataAsArray());
            } else {
                oldCheckSum = artifactDefinition.getArtifactChecksum();
            }
            Either<ArtifactDefinition, StorageOperationStatus> updateArifactDefinitionStatus = null;
            if (shouldLock) {
                try {
                    lockComponent(component, "Update Artifact - lock resource: ");
                } catch (ComponentException e) {
                    handleAuditing(AuditingActionEnum.ARTIFACT_METADATA_UPDATE, component, component.getUniqueId(), modifier, null, null,
                        artifactDefinition.getUniqueId(), e.getResponseFormat(), component.getComponentType(), null);
                    throw e;
                }
            }
            try {
                if (oldCheckSum != null && oldCheckSum.equals(newCheckSum)) {
                    artifactDefinition.setPayloadUpdateDate(payloadUpdateDateGen.get());
                    updateArifactDefinitionStatus = artifactToscaOperation
                        .updateArtifactOnResource(artifactDefinition, component, artifactDefinition.getUniqueId(), componentType.getNodeType(),
                            instanceId, true);
                    log.trace("No real update done in payload for {} artifact, updating payloadUpdateDate {}", artifactDefinition.getArtifactType(),
                        artifactDefinition.getEsId());
                    if (updateArifactDefinitionStatus.isRight()) {
                        ResponseFormat responseFormat = componentsUtils
                            .getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(updateArifactDefinitionStatus.right().value()),
                                artifactDefinition.getArtifactDisplayName());
                        log.trace("Failed to update payloadUpdateDate {}", artifactDefinition.getEsId());
                        handleAuditing(AuditingActionEnum.ARTIFACT_PAYLOAD_UPDATE, component, component.getUniqueId(), modifier, artifactDefinition,
                            artifactDefinition.getUniqueId(), artifactDefinition.getUniqueId(), responseFormat, ComponentTypeEnum.RESOURCE_INSTANCE,
                            resourceInstanceName);
                        return Either.right(responseFormat);
                    }
                } else {
                    artifactDefinition.getArtifactChecksum();
                    artifactDefinition.setArtifactChecksum(newCheckSum);
                    artifactDefinition.setEsId(artifactDefinition.getUniqueId());
                    log.trace("No real update done in payload for {} artifact, updating payloadUpdateDate {}", artifactDefinition.getArtifactType(),
                        artifactDefinition.getEsId());
                    updateArifactDefinitionStatus = artifactToscaOperation
                        .updateArtifactOnResource(artifactDefinition, component, artifactDefinition.getUniqueId(), componentType.getNodeType(),
                            instanceId, true);
                    log.trace("Update Payload {}", artifactDefinition.getEsId());
                }
                if (updateArifactDefinitionStatus.isLeft()) {
                    artifactDefinition = updateArifactDefinitionStatus.left().value();
                    artifactData.setId(artifactDefinition.getUniqueId());
                    CassandraOperationStatus saveArtifactStatus = artifactCassandraDao.saveArtifact(artifactData);
                    if (saveArtifactStatus == CassandraOperationStatus.OK) {
                        if (!inTransaction) {
                            janusGraphDao.commit();
                        }
                        log.debug("Artifact Saved In cassandra {}", artifactData.getId());
                        ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
                        handleAuditing(AuditingActionEnum.ARTIFACT_PAYLOAD_UPDATE, component, component.getUniqueId(), modifier, artifactDefinition,
                            artifactDefinition.getUniqueId(), artifactDefinition.getUniqueId(), responseFormat, ComponentTypeEnum.RESOURCE_INSTANCE,
                            resourceInstanceName);
                    } else {
                        if (!inTransaction) {
                            janusGraphDao.rollback();
                        }
                        log.info("Failed to save artifact {}.", artifactData.getId());
                        ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
                        handleAuditing(AuditingActionEnum.ARTIFACT_PAYLOAD_UPDATE, component, component.getUniqueId(), modifier, artifactDefinition,
                            artifactDefinition.getUniqueId(), artifactDefinition.getUniqueId(), responseFormat, ComponentTypeEnum.RESOURCE_INSTANCE,
                            resourceInstanceName);
                        return Either.right(responseFormat);
                    }
                } else {
                    ResponseFormat responseFormat = componentsUtils
                        .getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(updateArifactDefinitionStatus.right().value()),
                            artifactDefinition.getArtifactDisplayName());
                    log.debug("Failed To update artifact {}", artifactData.getId());
                    handleAuditing(AuditingActionEnum.ARTIFACT_PAYLOAD_UPDATE, component, component.getUniqueId(), modifier, artifactDefinition,
                        artifactDefinition.getUniqueId(), artifactDefinition.getUniqueId(), responseFormat, ComponentTypeEnum.RESOURCE_INSTANCE,
                        resourceInstanceName);
                    return Either.right(responseFormat);
                }
            } finally {
                if (shouldLock) {
                    graphLockOperation.unlockComponent(component.getUniqueId(), component.getComponentType().getNodeType());
                }
            }
        }
        return Either.left(artifactDefinition);
    }

    public Map<String, Object> buildJsonForUpdateArtifact(ArtifactDefinition artifactDef, ArtifactGroupTypeEnum artifactGroupType,
                                                          List<ArtifactTemplateInfo> updatedRequiredArtifacts) {
        return this
            .buildJsonForUpdateArtifact(artifactDef.getUniqueId(), artifactDef.getArtifactName(), artifactDef.getArtifactType(), artifactGroupType,
                artifactDef.getArtifactLabel(), artifactDef.getArtifactDisplayName(), artifactDef.getDescription(), artifactDef.getPayloadData(),
                updatedRequiredArtifacts, artifactDef.getListHeatParameters());
    }

    public Map<String, Object> buildJsonForUpdateArtifact(String artifactId, String artifactName, String artifactType,
                                                          ArtifactGroupTypeEnum artifactGroupType, String label, String displayName,
                                                          String description, byte[] artifactContent,
                                                          List<ArtifactTemplateInfo> updatedRequiredArtifacts,
                                                          List<HeatParameterDefinition> heatParameters) {
        Map<String, Object> json = new HashMap<>();
        if (artifactId != null && !artifactId.isEmpty()) {
            json.put(Constants.ARTIFACT_ID, artifactId);
        }
        json.put(Constants.ARTIFACT_NAME, artifactName);
        json.put(Constants.ARTIFACT_TYPE, artifactType);
        json.put(Constants.ARTIFACT_DESCRIPTION, description);
        if (artifactContent != null) {
            log.debug("payload is encoded. perform decode");
            String encodedPayload = Base64.encodeBase64String(artifactContent);
            json.put(Constants.ARTIFACT_PAYLOAD_DATA, encodedPayload);
        }
        json.put(Constants.ARTIFACT_DISPLAY_NAME, displayName);
        json.put(Constants.ARTIFACT_LABEL, label);
        json.put(Constants.ARTIFACT_GROUP_TYPE, artifactGroupType.getType());
        json.put(Constants.REQUIRED_ARTIFACTS, (updatedRequiredArtifacts == null || updatedRequiredArtifacts.isEmpty()) ? new ArrayList<>()
            : updatedRequiredArtifacts.stream().filter(
                    e -> e.getType().equals(ArtifactTypeEnum.HEAT_ARTIFACT.getType()) || e.getType().equals(ArtifactTypeEnum.HEAT_NESTED.getType()))
                .map(ArtifactTemplateInfo::getFileName).collect(Collectors.toList()));
        json.put(Constants.ARTIFACT_HEAT_PARAMS, (heatParameters == null || heatParameters.isEmpty()) ? new ArrayList<>() : heatParameters);
        return json;
    }

    public Either<ArtifactDefinition, Operation> updateResourceInstanceArtifactNoContent(String resourceId, Component containerComponent, User user,
                                                                                         Map<String, Object> json, ArtifactOperationInfo operation,
                                                                                         ArtifactDefinition artifactInfo) {
        String jsonStr = gson.toJson(json);
        ArtifactDefinition artifactDefinitionFromJson =
            artifactInfo == null ? RepresentationUtils.convertJsonToArtifactDefinition(jsonStr, ArtifactDefinition.class, false) : artifactInfo;
        String artifactUniqueId = artifactDefinitionFromJson == null ? null : artifactDefinitionFromJson.getUniqueId();
        Either<ArtifactDefinition, Operation> uploadArtifactToService = validateAndHandleArtifact(resourceId, ComponentTypeEnum.RESOURCE_INSTANCE,
            operation, artifactUniqueId, artifactDefinitionFromJson, null, jsonStr, null, null, user, containerComponent, false, false, true);
        return Either.left(uploadArtifactToService.left().value());
    }

    private Either<ArtifactDefinition, Operation> handleUpdateHeatEnvAndHeatMeta(String componentId, ArtifactDefinition artifactInfo,
                                                                                 AuditingActionEnum auditingAction, String artifactId, User user,
                                                                                 ComponentTypeEnum componentType, Component parent, String originData,
                                                                                 String origMd5, ArtifactOperationInfo operation) {
        if (origMd5 != null) {
            validateMd5(origMd5, originData, artifactInfo.getPayloadData(), operation);
            if (ArrayUtils.isNotEmpty(artifactInfo.getPayloadData())) {
                validateDeploymentArtifact(artifactInfo, parent);
                handlePayload(artifactInfo, isArtifactMetadataUpdate(auditingAction));
            } else { // duplicate
                throw new ByActionStatusComponentException(ActionStatus.MISSING_DATA, ARTIFACT_PAYLOAD);
            }
        }
        return updateHeatEnvParamsAndMetadata(componentId, artifactId, artifactInfo, user, auditingAction, parent, componentType, origMd5);
    }

    private Either<ArtifactDefinition, Operation> updateHeatEnvParamsAndMetadata(String componentId, String artifactId,
                                                                                 ArtifactDefinition artifactInfo, User user,
                                                                                 AuditingActionEnum auditingAction, Component parent,
                                                                                 ComponentTypeEnum componentType, String origMd5) {
        Either<ComponentInstance, ResponseFormat> getRI = getRIFromComponent(parent, componentId, artifactId, auditingAction, user);
        if (getRI.isRight()) {
            throw new ByResponseFormatComponentException(getRI.right().value());
        }
        ComponentInstance ri = getRI.left().value();
        Either<ArtifactDefinition, ResponseFormat> getArtifactRes = getArtifactFromRI(parent, ri, componentId, artifactId, auditingAction, user);
        if (getArtifactRes.isRight()) {
            throw new ByResponseFormatComponentException(getArtifactRes.right().value());
        }
        ArtifactDefinition currArtifact = getArtifactRes.left().value();
        if (currArtifact.getArtifactType().equals(ArtifactTypeEnum.HEAT.getType()) || currArtifact.getArtifactType()
            .equals(ArtifactTypeEnum.HEAT_VOL.getType()) || currArtifact.getArtifactType().equals(ArtifactTypeEnum.HEAT_NET.getType())) {
            throw new ByActionStatusComponentException(ActionStatus.RESTRICTED_OPERATION);
        }
        List<HeatParameterDefinition> currentHeatEnvParams = currArtifact.getListHeatParameters();
        List<HeatParameterDefinition> updatedHeatEnvParams = artifactInfo.getListHeatParameters();
        // upload
        if (origMd5 != null) {
            Either<List<HeatParameterDefinition>, ResponseFormat> uploadParamsValidationResult = validateUploadParamsFromEnvFile(auditingAction,
                parent, user, artifactInfo, artifactId, componentType, ri.getName(), currentHeatEnvParams, updatedHeatEnvParams,
                currArtifact.getArtifactName());
            if (uploadParamsValidationResult.isRight()) {
                throw new ByResponseFormatComponentException(uploadParamsValidationResult.right().value());
            }
            artifactInfo.setListHeatParameters(updatedHeatEnvParams);
        }
        Either<ArtifactDefinition, ResponseFormat> validateAndConvertHeatParamers = validateAndConvertHeatParameters(artifactInfo,
            ArtifactTypeEnum.HEAT_ENV.getType());
        if (validateAndConvertHeatParamers.isRight()) {
            throw new ByResponseFormatComponentException(validateAndConvertHeatParamers.right().value());
        }
        if (updatedHeatEnvParams != null && !updatedHeatEnvParams.isEmpty()) {
            // fill reduced heat env parameters List for updating
            boolean updateRequired = replaceCurrHeatValueWithUpdatedValue(currentHeatEnvParams, updatedHeatEnvParams);
            if (updateRequired) {
                currArtifact.setHeatParamsUpdateDate(System.currentTimeMillis());
                currArtifact.setListHeatParameters(currentHeatEnvParams);
                Either<ArtifactDefinition, StorageOperationStatus> updateArtifactRes = artifactToscaOperation
                    .updateArtifactOnResource(currArtifact, parent, currArtifact.getUniqueId(), componentType.getNodeType(), componentId, true);
                if (updateArtifactRes.isRight()) {
                    log.debug("Failed to update artifact on graph  - {}", artifactId);
                    throw new StorageException(updateArtifactRes.right().value());
                }
                StorageOperationStatus error = generateCustomizationUUIDOnGroupInstance(ri, updateArtifactRes.left().value().getUniqueId(),
                    parent.getUniqueId());
                if (error != StorageOperationStatus.OK) {
                    throw new StorageException(error);
                }
            }
        }
        updateHeatMetaDataIfNeeded(componentId, user, auditingAction, componentType, parent, ri, artifactInfo);
        StorageOperationStatus error = generateCustomizationUUIDOnInstance(parent.getUniqueId(), ri.getUniqueId(), componentType);
        if (error != StorageOperationStatus.OK) {
            throw new StorageException(error);
        }
        return Either.left(currArtifact);
    }

    private void updateHeatMetaDataIfNeeded(String componentId, User user, AuditingActionEnum auditingAction, ComponentTypeEnum componentType,
                                            Component parent, ComponentInstance resourceInstance, ArtifactDefinition updatedHeatEnvArtifact) {
        String heatArtifactId = updatedHeatEnvArtifact.getGeneratedFromId();
        Either<ArtifactDefinition, ResponseFormat> getArtifactRes = getArtifactFromRI(parent, resourceInstance, componentId, heatArtifactId,
            auditingAction, user);
        if (getArtifactRes.isRight()) {
            throw new ByResponseFormatComponentException(getArtifactRes.right().value());
        }
        ArtifactDefinition heatArtifactToUpdate = getArtifactRes.left().value();
        if (isUpdateHeatMetaDataNeeded(updatedHeatEnvArtifact, heatArtifactToUpdate)) {
            validateHeatMetaData(updatedHeatEnvArtifact);
            updateHeatMetadataFromHeatEnv(updatedHeatEnvArtifact, heatArtifactToUpdate);
            Either<ArtifactDefinition, StorageOperationStatus> updateArtifactRes = artifactToscaOperation
                .updateArtifactOnResource(heatArtifactToUpdate, parent, heatArtifactToUpdate.getUniqueId(), componentType.getNodeType(), componentId,
                    false);
            if (updateArtifactRes.isRight()) {
                log.debug("Failed to update artifact on graph  - {}", heatArtifactId);
                throw new StorageException(updateArtifactRes.right().value());
            }
            ArtifactDefinition artifactDefinition = updateArtifactRes.left().value();
            updateGeneratedIdInHeatEnvOnInstance(resourceInstance, parent, heatArtifactId, heatArtifactToUpdate, artifactDefinition,
                componentType.getNodeType());
            StorageOperationStatus error = generateCustomizationUUIDOnGroupInstance(resourceInstance, artifactDefinition.getUniqueId(),
                parent.getUniqueId());
            if (error != StorageOperationStatus.OK) {
                throw new StorageException(error);
            }
        }
    }

    private void validateHeatMetaData(ArtifactDefinition updatedHeatEnv) {
        Integer maxMinutes = ConfigurationManager.getConfigurationManager().getConfiguration().getHeatArtifactDeploymentTimeout().getMaxMinutes();
        Integer minMinutes = ConfigurationManager.getConfigurationManager().getConfiguration().getHeatArtifactDeploymentTimeout().getMinMinutes();
        Integer updateTimeout = updatedHeatEnv.getTimeout();
        if (updateTimeout > maxMinutes || updateTimeout < minMinutes) {
            throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_INVALID_TIMEOUT);
        }
    }

    private boolean isUpdateHeatMetaDataNeeded(ArtifactDefinition updatedHeatEnv, ArtifactDefinition origHeat) {
        // currently only timeout metadata can be updated
        return !origHeat.getTimeout().equals(updatedHeatEnv.getTimeout());
    }

    private void updateHeatMetadataFromHeatEnv(ArtifactDefinition updatedHeatEnv, ArtifactDefinition origHeat) {
        // currently only timeout metadata can be updated
        origHeat.setTimeout(updatedHeatEnv.getTimeout());
    }

    private boolean replaceCurrHeatValueWithUpdatedValue(List<HeatParameterDefinition> currentHeatEnvParams,
                                                         List<HeatParameterDefinition> updatedHeatEnvParams) {
        boolean isUpdate = false;
        List<String> currentParamsNames = currentHeatEnvParams.stream().map(x -> x.getName()).collect(Collectors.toList());
        for (HeatParameterDefinition heatEnvParam : updatedHeatEnvParams) {
            String paramName = heatEnvParam.getName();
            validateParamName(paramName, currentParamsNames);
            for (HeatParameterDefinition currHeatParam : currentHeatEnvParams) {
                if (paramName.equalsIgnoreCase(currHeatParam.getName())) {
                    String updatedParamValue = heatEnvParam.getCurrentValue();
                    if (!Objects.equals(updatedParamValue, currHeatParam.getCurrentValue())) {
                        currHeatParam.setCurrentValue(updatedParamValue);
                        isUpdate = true;
                    }
                }
            }
        }
        return isUpdate;
    }

    private void validateParamName(String paramName, List<String> heatParamsNames) {
        if (!heatParamsNames.contains(paramName)) {
            throw new ByActionStatusComponentException(ActionStatus.PROPERTY_NOT_FOUND, paramName);
        }
    }

    private Either<ArtifactDefinition, Operation> updateHeatParams(String componentId, ArtifactDefinition artifactEnvInfo,
                                                                   AuditingActionEnum auditingAction, Component parent,
                                                                   ComponentTypeEnum componentType, ArtifactDefinition currHeatArtifact,
                                                                   boolean needToUpdateGroup) {
        Either<ArtifactDefinition, Operation> insideEither = null;
        String currentHeatId = currHeatArtifact.getUniqueId();
        String esArtifactId = currHeatArtifact.getEsId();
        Either<DAOArtifactData, CassandraOperationStatus> artifactFromES = artifactCassandraDao.getArtifact(esArtifactId);
        if (artifactFromES.isRight()) {
            StorageOperationStatus storageResponse = DaoStatusConverter.convertCassandraStatusToStorageStatus(artifactFromES.right().value());
            throw new StorageException(storageResponse, currHeatArtifact.getArtifactDisplayName());
        }
        DAOArtifactData DAOArtifactData = artifactFromES.left().value();
        ArtifactDefinition updatedHeatArt = currHeatArtifact;
        List<HeatParameterDefinition> updatedHeatEnvParams = artifactEnvInfo.getListHeatParameters();
        List<HeatParameterDefinition> currentHeatEnvParams = currHeatArtifact.getListHeatParameters();
        List<HeatParameterDefinition> newHeatEnvParams = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(updatedHeatEnvParams) && CollectionUtils.isNotEmpty(currentHeatEnvParams)) {
            //TODO: improve complexity - currently N^2
            String paramName;
            for (HeatParameterDefinition heatEnvParam : updatedHeatEnvParams) {
                paramName = heatEnvParam.getName();
                for (HeatParameterDefinition currHeatParam : currentHeatEnvParams) {
                    if (paramName.equalsIgnoreCase(currHeatParam.getName())) {
                        String updatedParamValue = heatEnvParam.getCurrentValue();
                        if (updatedParamValue == null) {
                            updatedParamValue = heatEnvParam.getDefaultValue();
                        }
                        HeatParameterType paramType = HeatParameterType.isValidType(currHeatParam.getType());
                        if (!paramType.getValidator().isValid(updatedParamValue, null)) {
                            throw new ByActionStatusComponentException(ActionStatus.INVALID_HEAT_PARAMETER_VALUE, ArtifactTypeEnum.HEAT_ENV.getType(),
                                paramType.getType(), paramName);
                        }
                        currHeatParam.setCurrentValue(paramType.getConverter().convert(updatedParamValue, null, null));
                        newHeatEnvParams.add(currHeatParam);
                        break;
                    }
                }
            }
            if (!newHeatEnvParams.isEmpty()) {
                currHeatArtifact.setListHeatParameters(currentHeatEnvParams);
                Either<ArtifactDefinition, StorageOperationStatus> operationStatus = artifactToscaOperation
                    .updateArtifactOnResource(currHeatArtifact, parent, currHeatArtifact.getUniqueId(), componentType.getNodeType(), componentId,
                        true);
                if (operationStatus.isRight()) {
                    log.debug("Failed to update artifact on graph  - {}", currHeatArtifact.getUniqueId());
                    throw new StorageException(operationStatus.right().value());
                }
                updatedHeatArt = operationStatus.left().value();
                if (!updatedHeatArt.getDuplicated() || DAOArtifactData.getId() == null) {
                    DAOArtifactData.setId(updatedHeatArt.getEsId());
                }
                saveArtifactInCassandra(DAOArtifactData, parent, artifactEnvInfo, currentHeatId, updatedHeatArt.getUniqueId(), auditingAction,
                    componentType);
                insideEither = Either.left(updatedHeatArt);
            }
        }
        Either<ArtifactDefinition, StorageOperationStatus> updateHeatEnvArtifact;
        if (!currentHeatId.equals(updatedHeatArt.getUniqueId())) {
            artifactEnvInfo.setArtifactChecksum(null);
            updateHeatEnvArtifact = artifactToscaOperation
                .updateHeatEnvArtifact(parent, artifactEnvInfo, currentHeatId, updatedHeatArt.getUniqueId(), componentType.getNodeType(),
                    componentId);
        } else {
            //TODO Andrey check if componentId = parent.getUniqeId
            updateHeatEnvArtifact = artifactToscaOperation.updateHeatEnvPlaceholder(artifactEnvInfo, parent, componentType.getNodeType());
        }
        if (needToUpdateGroup && updateHeatEnvArtifact.isLeft()) {
            ActionStatus result = updateGroupForHeat(currHeatArtifact, updatedHeatArt, artifactEnvInfo, updateHeatEnvArtifact.left().value(), parent);
            if (result != ActionStatus.OK) {
                throw new ByActionStatusComponentException(result);
            }
        }
        if (updatedHeatEnvParams.isEmpty()) {
            throw new ByActionStatusComponentException(ActionStatus.INVALID_YAML, currHeatArtifact.getArtifactName());
        }
        return insideEither;
    }

    private StorageOperationStatus generateCustomizationUUIDOnGroupInstance(ComponentInstance ri, String artifactId, String componentId) {
        StorageOperationStatus error = StorageOperationStatus.OK;
        log.debug("Need to re-generate  customization UUID for group instance on component instance  {}", ri.getUniqueId());
        List<GroupInstance> groupsInstances = ri.getGroupInstances();
        List<String> groupInstancesId = null;
        if (groupsInstances != null && !groupsInstances.isEmpty()) {
            groupInstancesId = groupsInstances.stream()
                .filter(p -> p.getGroupInstanceArtifacts() != null && p.getGroupInstanceArtifacts().contains(artifactId))
                .map(GroupInstanceDataDefinition::getUniqueId).collect(Collectors.toList());
        }
        if (groupInstancesId != null && !groupInstancesId.isEmpty()) {
            toscaOperationFacade.generateCustomizationUUIDOnInstanceGroup(componentId, ri.getUniqueId(), groupInstancesId);
        }
        return error;
    }

    public Either<List<HeatParameterDefinition>, ResponseFormat> validateUploadParamsFromEnvFile(AuditingActionEnum auditingAction, Component parent,
                                                                                                 User user, ArtifactDefinition artifactInfo,
                                                                                                 String artifactId, ComponentTypeEnum componentType,
                                                                                                 String riName,
                                                                                                 List<HeatParameterDefinition> currentHeatEnvParams,
                                                                                                 List<HeatParameterDefinition> updatedHeatEnvParams,
                                                                                                 String currArtifactName) {
        if (updatedHeatEnvParams == null || updatedHeatEnvParams.isEmpty()) {
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(ActionStatus.INVALID_DEPLOYMENT_ARTIFACT_HEAT, artifactInfo.getArtifactName(), currArtifactName);
            handleAuditing(auditingAction, parent, parent.getUniqueId(), user, artifactInfo, null, artifactId, responseFormat, componentType, riName);
            return Either.right(responseFormat);
        }
        for (HeatParameterDefinition uploadedHeatParam : updatedHeatEnvParams) {
            String paramName = uploadedHeatParam.getName();
            boolean isExistsInHeat = false;
            for (HeatParameterDefinition currHeatParam : currentHeatEnvParams) {
                if (paramName.equalsIgnoreCase(currHeatParam.getName())) {
                    isExistsInHeat = true;
                    uploadedHeatParam.setType(currHeatParam.getType());
                    uploadedHeatParam.setCurrentValue(uploadedHeatParam.getDefaultValue());
                    uploadedHeatParam.setDefaultValue(currHeatParam.getDefaultValue());
                    uploadedHeatParam.setUniqueId(currHeatParam.getUniqueId());
                    break;
                }
            }
            if (!isExistsInHeat) {
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISMATCH_HEAT_VS_HEAT_ENV, currArtifactName);
                handleAuditing(auditingAction, parent, parent.getUniqueId(), user, artifactInfo, null, artifactId, responseFormat, componentType,
                    riName);
                return Either.right(responseFormat);
            }
        }
        return Either.left(updatedHeatEnvParams);
    }

    private Either<ComponentInstance, ResponseFormat> getRIFromComponent(Component component, String riID, String artifactId,
                                                                         AuditingActionEnum auditingAction, User user) {
        ResponseFormat responseFormat = null;
        List<ComponentInstance> ris = component.getComponentInstances();
        for (ComponentInstance ri : ris) {
            if (riID.equals(ri.getUniqueId())) {
                return Either.left(ri);
            }
        }
        responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND_ON_SERVICE, riID);
        log.debug("Resource Instance not found, resourceInstanceId {}", riID);
        handleAuditing(auditingAction, null, riID, user, null, null, artifactId, responseFormat, ComponentTypeEnum.RESOURCE_INSTANCE, null);
        return Either.right(responseFormat);
    }

    private Either<ArtifactDefinition, ResponseFormat> getArtifactFromRI(Component component, ComponentInstance ri, String riID, String artifactId,
                                                                         AuditingActionEnum auditingAction, User user) {
        ResponseFormat responseFormat = null;
        Map<String, ArtifactDefinition> rtifactsMap = ri.getDeploymentArtifacts();
        for (ArtifactDefinition artifact : rtifactsMap.values()) {
            if (artifactId.equals(artifact.getUniqueId())) {
                return Either.left(artifact);
            }
        }
        responseFormat = componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, riID, component.getUniqueId());
        handleAuditing(auditingAction, component, riID, user, null, null, artifactId, responseFormat, ComponentTypeEnum.RESOURCE_INSTANCE,
            ri.getName());
        return Either.right(responseFormat);
    }

    public ArtifactDefinition extractArtifactDefinition(Either<ArtifactDefinition, Operation> eitherArtifact) {
        ArtifactDefinition ret;
        if (eitherArtifact.isLeft()) {
            ret = eitherArtifact.left().value();
        } else {
            ret = eitherArtifact.right().value().getImplementationArtifact();
        }
        return ret;
    }

    public byte[] downloadComponentArtifactByUUIDs(ComponentTypeEnum componentType, String componentUuid, String artifactUUID,
                                                   ResourceCommonInfo resourceCommonInfo) {
        Component component = getComponentByUuid(componentType, componentUuid);
        resourceCommonInfo.setResourceName(component.getName());
        return downloadArtifact(component.getAllArtifacts(), artifactUUID, component.getName());
    }

    /**
     * downloads an artifact of resource instance of component by UUIDs
     *
     * @param componentType
     * @param componentUuid
     * @param resourceInstanceName
     * @param artifactUUID
     * @return
     */
    public byte[] downloadResourceInstanceArtifactByUUIDs(ComponentTypeEnum componentType, String componentUuid, String resourceInstanceName,
                                                          String artifactUUID) {
        ComponentInstance resourceInstance = getRelatedComponentInstance(componentType, componentUuid, resourceInstanceName);
        if (resourceInstance != null) {
            return downloadArtifact(resourceInstance.getDeploymentArtifacts(), artifactUUID, resourceInstance.getName());
        } else {
            return downloadArtifact(null, artifactUUID, null);
        }
    }

    /**
     * uploads an artifact to a component by UUID
     *
     * @param data
     * @param request
     * @param componentType
     * @param componentUuid
     * @param resourceCommonInfo
     * @param operation
     * @return
     */
    public ArtifactDefinition uploadArtifactToComponentByUUID(String data, HttpServletRequest request, ComponentTypeEnum componentType,
                                                              String componentUuid, ResourceCommonInfo resourceCommonInfo,
                                                              ArtifactOperationInfo operation) {
        Either<ArtifactDefinition, Operation> actionResult;
        Component component;
        String componentId;
        ArtifactDefinition artifactInfo = RepresentationUtils.convertJsonToArtifactDefinition(data, ArtifactDefinition.class, false);
        String origMd5 = request.getHeader(Constants.MD5_HEADER);
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        Either<ComponentMetadataData, ActionStatus> getComponentRes = fetchLatestComponentMetadataOrThrow(componentType, componentUuid);
        ComponentMetadataDataDefinition componentMetadataDataDefinition = getComponentRes.left().value().getMetadataDataDefinition();
        componentId = componentMetadataDataDefinition.getUniqueId();
        String componentName = componentMetadataDataDefinition.getName();
        if (!componentMetadataDataDefinition.getState().equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
            component = checkoutParentComponent(componentType, componentId, userId);
            if (component != null) {
                componentId = component.getUniqueId();
                componentName = component.getName();
            }
        }
        resourceCommonInfo.setResourceName(componentName);
        actionResult = handleArtifactRequest(componentId, userId, componentType, operation, null, artifactInfo, origMd5, data, null, null, null,
            null);
        return actionResult.left().value();
    }

    /**
     * upload an artifact to a resource instance by UUID
     *
     * @param data
     * @param request
     * @param componentType
     * @param componentUuid
     * @param resourceInstanceName
     * @param operation
     * @return
     */
    public ArtifactDefinition uploadArtifactToRiByUUID(String data, HttpServletRequest request, ComponentTypeEnum componentType, String componentUuid,
                                                       String resourceInstanceName, ArtifactOperationInfo operation) {
        Either<ArtifactDefinition, Operation> actionResult;
        Component component = null;
        String componentInstanceId;
        String componentId;
        String origMd5 = request.getHeader(Constants.MD5_HEADER);
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        ImmutablePair<Component, ComponentInstance> componentRiPair = null;
        Either<ComponentMetadataData, ActionStatus> getComponentRes = fetchLatestComponentMetadataOrThrow(componentType, componentUuid,
            resourceInstanceName);
        if (!getComponentRes.left().value().getMetadataDataDefinition().getState().equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
            component = checkoutParentComponent(componentType, getComponentRes.left().value().getMetadataDataDefinition().getUniqueId(), userId);
        }
        if (component == null) {
            componentRiPair = getRelatedComponentComponentInstance(componentType, componentUuid, resourceInstanceName);
        } else {
            componentRiPair = getRelatedComponentComponentInstance(component, resourceInstanceName);
        }
        componentInstanceId = componentRiPair.getRight().getUniqueId();
        componentId = componentRiPair.getLeft().getUniqueId();
        ArtifactDefinition artifactInfo = RepresentationUtils.convertJsonToArtifactDefinition(data, ArtifactDefinition.class, false);
        actionResult = handleArtifactRequest(componentInstanceId, userId, ComponentTypeEnum.RESOURCE_INSTANCE, operation, null, artifactInfo, origMd5,
            data, null, null, componentId, ComponentTypeEnum.findParamByType(componentType));
        return actionResult.left().value();
    }

    /**
     * updates an artifact on a component by UUID
     *
     * @param data
     * @param request
     * @param componentType
     * @param componentUuid
     * @param artifactUUID
     * @param resourceCommonInfo
     * @param operation          TODO
     * @return
     */
    public ArtifactDefinition updateArtifactOnComponentByUUID(String data, HttpServletRequest request, ComponentTypeEnum componentType,
                                                              String componentUuid, String artifactUUID, ResourceCommonInfo resourceCommonInfo,
                                                              ArtifactOperationInfo operation) {
        Either<ArtifactDefinition, Operation> actionResult;
        Component component;
        String componentId;
        String artifactId;
        ArtifactDefinition artifactInfo = RepresentationUtils.convertJsonToArtifactDefinitionForUpdate(data, ArtifactDefinition.class);
        String origMd5 = request.getHeader(Constants.MD5_HEADER);
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        Either<ComponentMetadataData, ActionStatus> getComponentRes = fetchLatestComponentMetadataOrThrow(componentType, componentUuid);
        componentId = getComponentRes.left().value().getMetadataDataDefinition().getUniqueId();
        String componentName = getComponentRes.left().value().getMetadataDataDefinition().getName();
        if (!getComponentRes.left().value().getMetadataDataDefinition().getState().equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
            component = checkoutParentComponent(componentType, componentId, userId);
            if (component != null) {
                componentId = component.getUniqueId();
                componentName = component.getName();
            }
        }
        resourceCommonInfo.setResourceName(componentName);
        artifactId = getLatestParentArtifactDataIdByArtifactUUID(artifactUUID, componentId, componentType);
        actionResult = handleArtifactRequest(componentId, userId, componentType, operation, artifactId, artifactInfo, origMd5, data, null, null, null,
            null);
        if (actionResult.isRight()) {
            log.debug(FAILED_UPLOAD_ARTIFACT_TO_COMPONENT, componentType, componentUuid, actionResult.right().value());
        }
        return actionResult.left().value();
    }

    /**
     * updates an artifact on a resource instance by UUID
     *
     * @param data
     * @param request
     * @param componentType
     * @param componentUuid
     * @param resourceInstanceName
     * @param artifactUUID
     * @param operation            TODO
     * @return
     */
    public ArtifactDefinition updateArtifactOnRiByUUID(String data, HttpServletRequest request, ComponentTypeEnum componentType, String componentUuid,
                                                       String resourceInstanceName, String artifactUUID, ArtifactOperationInfo operation) {
        Either<ArtifactDefinition, Operation> actionResult;
        Component component = null;
        String componentInstanceId;
        String componentId;
        String artifactId;
        String origMd5 = request.getHeader(Constants.MD5_HEADER);
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        ImmutablePair<Component, ComponentInstance> componentRiPair = null;
        Either<ComponentMetadataData, ActionStatus> getComponentRes = fetchLatestComponentMetadataOrThrow(componentType, componentUuid);
        if (!getComponentRes.left().value().getMetadataDataDefinition().getState().equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
            component = checkoutParentComponent(componentType, getComponentRes.left().value().getMetadataDataDefinition().getUniqueId(), userId);
        }
        if (component == null) {
            componentRiPair = getRelatedComponentComponentInstance(componentType, componentUuid, resourceInstanceName);
        } else {
            componentRiPair = getRelatedComponentComponentInstance(component, resourceInstanceName);
        }
        componentInstanceId = componentRiPair.getRight().getUniqueId();
        componentId = componentRiPair.getLeft().getUniqueId();
        artifactId = findArtifactId(componentRiPair.getRight(), artifactUUID);
        ArtifactDefinition artifactInfo = RepresentationUtils.convertJsonToArtifactDefinition(data, ArtifactDefinition.class, false);
        actionResult = handleArtifactRequest(componentInstanceId, userId, ComponentTypeEnum.RESOURCE_INSTANCE, operation, artifactId, artifactInfo,
            origMd5, data, null, null, componentId, ComponentTypeEnum.findParamByType(componentType));
        return actionResult.left().value();
    }

    private Either<ArtifactDefinition, ResponseFormat> updateOperationArtifact(String componentId, String interfaceType, String operationUuid,
                                                                               ArtifactDefinition artifactInfo) {
        Either<Component, StorageOperationStatus> componentStorageOperationStatusEither = toscaOperationFacade.getToscaElement(componentId);
        if (componentStorageOperationStatusEither.isRight()) {
            StorageOperationStatus errorStatus = componentStorageOperationStatusEither.right().value();
            log.debug("Failed to fetch resource information by resource id, error {}", errorStatus);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(errorStatus)));
        }
        Component storedComponent = componentStorageOperationStatusEither.left().value();
        Optional<InterfaceDefinition> optionalInterface = InterfaceOperationUtils
            .getInterfaceDefinitionFromComponentByInterfaceType(storedComponent, interfaceType);
        if (!optionalInterface.isPresent()) {
            log.debug("Failed to get resource interface for resource Id {}", componentId);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_NOT_FOUND_IN_COMPONENT, interfaceType));
        }
        //fetch the operation from storage
        InterfaceDefinition gotInterface = optionalInterface.get();
        Map<String, Operation> operationsMap = gotInterface.getOperationsMap();
        Optional<Operation> optionalOperation = operationsMap.values().stream().filter(o -> o.getUniqueId().equals(operationUuid)).findFirst();
        if (!optionalOperation.isPresent()) {
            log.debug("Failed to get resource interface operation for resource Id {} and operationId {}", componentId, operationUuid);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND, componentId);
            return Either.right(responseFormat);
        }
        Operation operation = optionalOperation.get();
        ArtifactDefinition implementationArtifact = operation.getImplementationArtifact();
        implementationArtifact.setArtifactUUID(artifactInfo.getArtifactUUID());
        implementationArtifact.setUniqueId(artifactInfo.getUniqueId());
        implementationArtifact.setArtifactName(artifactInfo.getArtifactName());
        implementationArtifact.setDescription(artifactInfo.getDescription());
        implementationArtifact.setArtifactType(artifactInfo.getArtifactType());
        implementationArtifact.setArtifactLabel(artifactInfo.getArtifactLabel());
        implementationArtifact.setArtifactDisplayName(artifactInfo.getArtifactDisplayName());
        implementationArtifact.setEsId(artifactInfo.getEsId());
        operation.setImplementation(implementationArtifact);
        gotInterface.setOperationsMap(operationsMap);
        Either<List<InterfaceDefinition>, StorageOperationStatus> interfaceDefinitionStorageOperationStatusEither = interfaceOperation
            .updateInterfaces(storedComponent, Collections.singletonList(gotInterface));
        if (interfaceDefinitionStorageOperationStatusEither.isRight()) {
            StorageOperationStatus storageOperationStatus = interfaceDefinitionStorageOperationStatusEither.right().value();
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForDataType(storageOperationStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));
        }
        return Either.left(artifactInfo);
    }

    /**
     * updates an artifact on a component by UUID
     *
     * @param data
     * @param request
     * @param componentType
     * @param componentUuid
     * @param artifactUUID
     * @param operation
     * @return
     */
    public Either<ArtifactDefinition, ResponseFormat> updateArtifactOnInterfaceOperationByResourceUUID(String data, HttpServletRequest request,
                                                                                                       ComponentTypeEnum componentType,
                                                                                                       String componentUuid, String interfaceUUID,
                                                                                                       String operationUUID, String artifactUUID,
                                                                                                       ResourceCommonInfo resourceCommonInfo,
                                                                                                       ArtifactOperationInfo operation) {
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        Either<ArtifactDefinition, ResponseFormat> updateArtifactResult;
        String componentId = null;
        ArtifactDefinition existingArtifactInfo = null;
        String interfaceName = null;
        ArtifactDefinition artifactInfo = RepresentationUtils.convertJsonToArtifactDefinitionForUpdate(data, ArtifactDefinition.class);
        String origMd5 = request.getHeader(Constants.MD5_HEADER);
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        Either<ComponentMetadataData, ActionStatus> getComponentRes = fetchLatestComponentMetadata(componentType, componentUuid).right().map(as -> {
            errorWrapper.setInnerElement(componentsUtils.getResponseFormat(as));
            return as;
        });
        if (errorWrapper.isEmpty()) {
            componentId = getComponentRes.left().value().getMetadataDataDefinition().getUniqueId();
            String componentName = getComponentRes.left().value().getMetadataDataDefinition().getName();
            if (!getComponentRes.left().value().getMetadataDataDefinition().getState().equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
                Component component = checkoutParentComponent(componentType, componentId, userId);
                if (component != null) {
                    componentId = component.getUniqueId();
                    componentName = component.getName();
                }
            }
            resourceCommonInfo.setResourceName(componentName);
        }
        if (errorWrapper.isEmpty()) {
            Either<String, ResponseFormat> interfaceNameEither = fetchInterfaceName(componentId, interfaceUUID);
            if (interfaceNameEither.isRight()) {
                errorWrapper.setInnerElement(interfaceNameEither.right().value());
            } else {
                interfaceName = interfaceNameEither.left().value();
            }
            if (errorWrapper.isEmpty()) {
                Either<Component, StorageOperationStatus> toscaComponentEither = toscaOperationFacade.getToscaElement(componentId);
                if (toscaComponentEither.isRight()) {
                    StorageOperationStatus status = toscaComponentEither.right().value();
                    log.debug("Could not fetch component with type {} and id {}. Status is {}. ", componentType, componentId, status);
                    errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status)));
                }
                if (errorWrapper.isEmpty()) {
                    NodeTypeEnum parentType = convertParentType(componentType);
                    final List<ArtifactDefinition> existingDeploymentArtifacts = getDeploymentArtifacts(toscaComponentEither.left().value(), null);
                    for (ArtifactDefinition artifactDefinition : existingDeploymentArtifacts) {
                        if (artifactInfo.getArtifactName().equalsIgnoreCase(artifactDefinition.getArtifactName())) {
                            existingArtifactInfo = artifactDefinition;
                            break;
                        }
                    }
                    if (existingArtifactInfo != null) {
                        return updateOperationArtifact(componentId, interfaceName, operationUUID, existingArtifactInfo);
                    }
                }
            }
        }
        if (errorWrapper.isEmpty()) {
            updateArtifactResult = handleArtifactRequestAndFlatten(componentId, userId, componentType, operation, artifactUUID, artifactInfo, origMd5,
                data, interfaceName, operationUUID);
        } else {
            updateArtifactResult = Either.right(errorWrapper.getInnerElement());
        }
        return updateArtifactResult;
    }

    private Either<ArtifactDefinition, ResponseFormat> handleArtifactRequestAndFlatten(String componentId, String userId,
                                                                                       ComponentTypeEnum componentType,
                                                                                       ArtifactOperationInfo operation, String artifactId,
                                                                                       ArtifactDefinition artifactInfo, String origMd5,
                                                                                       String originData, String interfaceName,
                                                                                       String operationName) {
        try {
            return handleArtifactRequest(componentId, userId, componentType, operation, artifactId, artifactInfo, origMd5, originData, interfaceName,
                operationName, null, null).right().map(op -> {
                log.debug("Unexpected value returned while calling handleArtifactRequest: {}", op);
                return componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            });
        } catch (ComponentException e) {
            return Either.right(e.getResponseFormat());
        }
    }

    private Either<ComponentMetadataData, ActionStatus> fetchLatestComponentMetadataOrThrow(ComponentTypeEnum componentType, String componentUuid) {
        return fetchLatestComponentMetadataOrThrow(componentType, componentUuid, componentUuid);
    }

    private Either<ComponentMetadataData, ActionStatus> fetchLatestComponentMetadataOrThrow(ComponentTypeEnum componentType, String componentUuid,
                                                                                            String resourceInstanceName) {
        return fetchLatestComponentMetadata(componentType, componentUuid).right().map(as -> {
            throw new ByActionStatusComponentException(as, resourceInstanceName);
        });
    }

    private Either<ComponentMetadataData, ActionStatus> fetchLatestComponentMetadata(ComponentTypeEnum componentType, String componentUuid) {
        return toscaOperationFacade.getLatestComponentMetadataByUuid(componentUuid, JsonParseFlagEnum.ParseMetadata, true).right().map(sos -> {
            log.debug(FAILED_FETCH_COMPONENT, componentType, componentUuid, sos);
            return componentsUtils.convertFromStorageResponse(sos, componentType);
        });
    }

    private Either<String, ResponseFormat> fetchInterfaceName(String componentId, String interfaceUUID) {
        Either<Component, StorageOperationStatus> componentStorageOperationStatusEither = toscaOperationFacade.getToscaElement(componentId);
        if (componentStorageOperationStatusEither.isRight()) {
            StorageOperationStatus errorStatus = componentStorageOperationStatusEither.right().value();
            log.debug("Failed to fetch component information by component id, error {}", errorStatus);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(errorStatus)));
        }
        Component storedComponent = componentStorageOperationStatusEither.left().value();
        Optional<InterfaceDefinition> optionalInterface = InterfaceOperationUtils
            .getInterfaceDefinitionFromComponentByInterfaceId(storedComponent, interfaceUUID);
        if (!optionalInterface.isPresent()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_NOT_FOUND_IN_COMPONENT, interfaceUUID));
        }
        return Either.left(optionalInterface.get().getType());
    }

    /**
     * deletes an artifact on a component by UUID
     *
     * @param request
     * @param componentType
     * @param componentUuid
     * @param artifactUUID
     * @param resourceCommonInfo
     * @param operation          TODO
     * @return
     */
    public ArtifactDefinition deleteArtifactOnComponentByUUID(HttpServletRequest request, ComponentTypeEnum componentType, String componentUuid,
                                                              String artifactUUID, ResourceCommonInfo resourceCommonInfo,
                                                              ArtifactOperationInfo operation) {
        Either<ArtifactDefinition, Operation> actionResult;
        Component component;
        String componentId;
        String artifactId;
        String origMd5 = request.getHeader(Constants.MD5_HEADER);
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        Either<ComponentMetadataData, ActionStatus> getComponentRes = fetchLatestComponentMetadataOrThrow(componentType, componentUuid);
        componentId = getComponentRes.left().value().getMetadataDataDefinition().getUniqueId();
        String componentName = getComponentRes.left().value().getMetadataDataDefinition().getName();
        if (!getComponentRes.left().value().getMetadataDataDefinition().getState().equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
            component = checkoutParentComponent(componentType, componentId, userId);
            if (component != null) {
                componentId = component.getUniqueId();
                componentName = component.getName();
            }
        }
        resourceCommonInfo.setResourceName(componentName);
        artifactId = getLatestParentArtifactDataIdByArtifactUUID(artifactUUID, componentId, componentType);
        actionResult = handleArtifactRequest(componentId, userId, componentType, operation, artifactId, null, origMd5, null, null, null, null, null);
        return actionResult.left().value();
    }

    /**
     * deletes an artifact from a resource instance by UUID
     *
     * @param request
     * @param componentType
     * @param componentUuid
     * @param resourceInstanceName
     * @param artifactUUID
     * @param operation            TODO
     * @return
     */
    public ArtifactDefinition deleteArtifactOnRiByUUID(HttpServletRequest request, ComponentTypeEnum componentType, String componentUuid,
                                                       String resourceInstanceName, String artifactUUID, ArtifactOperationInfo operation) {
        Either<ArtifactDefinition, Operation> actionResult;
        Component component = null;
        String componentInstanceId;
        String componentId;
        String artifactId;
        String origMd5 = request.getHeader(Constants.MD5_HEADER);
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        ImmutablePair<Component, ComponentInstance> componentRiPair = null;
        Either<ComponentMetadataData, ActionStatus> getComponentRes = fetchLatestComponentMetadataOrThrow(componentType, componentUuid);
        if (!getComponentRes.left().value().getMetadataDataDefinition().getState().equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
            component = checkoutParentComponent(componentType, getComponentRes.left().value().getMetadataDataDefinition().getUniqueId(), userId);
        }
        if (component == null) {
            componentRiPair = getRelatedComponentComponentInstance(componentType, componentUuid, resourceInstanceName);
        } else {
            componentRiPair = getRelatedComponentComponentInstance(component, resourceInstanceName);
        }
        componentInstanceId = componentRiPair.getRight().getUniqueId();
        componentId = componentRiPair.getLeft().getUniqueId();
        artifactId = findArtifactId(componentRiPair.getRight(), artifactUUID);
        actionResult = handleArtifactRequest(componentInstanceId, userId, ComponentTypeEnum.RESOURCE_INSTANCE, operation, artifactId, null, origMd5,
            null, null, null, componentId, ComponentTypeEnum.findParamByType(componentType));
        return actionResult.left().value();
    }

    private String findArtifactId(ComponentInstance instance, String artifactUUID) {
        String artifactId = null;
        ArtifactDefinition foundArtifact = null;
        if (instance.getDeploymentArtifacts() != null) {
            foundArtifact = instance.getDeploymentArtifacts().values().stream()
                .filter(e -> e.getArtifactUUID() != null && e.getArtifactUUID().equals(artifactUUID)).findFirst().orElse(null);
        }
        if (foundArtifact == null && instance.getArtifacts() != null) {
            foundArtifact = instance.getArtifacts().values().stream()
                .filter(e -> e.getArtifactUUID() != null && e.getArtifactUUID().equals(artifactUUID)).findFirst().orElse(null);
        }
        if (foundArtifact == null) {
            log.debug("The artifact {} was not found on instance {}. ", artifactUUID, instance.getUniqueId());
            throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_NOT_FOUND, artifactUUID);
        } else {
            artifactId = foundArtifact.getUniqueId();
        }
        return artifactId;
    }

    @SuppressWarnings("unchecked")
    public ArtifactDefinition createHeatEnvPlaceHolder(List<ArtifactDefinition> createdArtifacts, ArtifactDefinition heatArtifact, String envType,
                                                       String parentId, NodeTypeEnum parentType, String parentName, User user, Component component,
                                                       Map<String, String> existingEnvVersions) {
        Map<String, Object> deploymentResourceArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration()
            .getDeploymentResourceInstanceArtifacts();
        if (deploymentResourceArtifacts == null) {
            log.debug("no deployment artifacts are configured for generated artifacts");
            throw new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR);
        }
        Map<String, Object> placeHolderData = (Map<String, Object>) deploymentResourceArtifacts.get(envType);
        if (placeHolderData == null) {
            log.debug("no env type {} are configured for generated artifacts", envType);
            throw new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR);
        }
        String envLabel = (heatArtifact.getArtifactLabel() + HEAT_ENV_SUFFIX).toLowerCase();
        ArtifactDefinition createArtifactPlaceHolder = createArtifactPlaceHolderInfo(parentId, envLabel, placeHolderData, user.getUserId(),
            ArtifactGroupTypeEnum.DEPLOYMENT, true);
        ArtifactDefinition artifactHeatEnv = createArtifactPlaceHolder;
        artifactHeatEnv.setGeneratedFromId(heatArtifact.getUniqueId());
        artifactHeatEnv.setHeatParamsUpdateDate(System.currentTimeMillis());
        artifactHeatEnv.setTimeout(0);
        artifactHeatEnv.setIsFromCsar(heatArtifact.getIsFromCsar());
        buildHeatEnvFileName(heatArtifact, artifactHeatEnv, placeHolderData);
        // rbetzer - keep env artifactVersion - changeComponentInstanceVersion flow
        handleEnvArtifactVersion(artifactHeatEnv, existingEnvVersions);
        ArtifactDefinition heatEnvPlaceholder;
        // Evg : for resource instance artifact will be added later as block with other env artifacts from BL
        if (parentType != NodeTypeEnum.ResourceInstance) {
            String checkSum = artifactToscaOperation.sortAndCalculateChecksumForHeatParameters(heatArtifact.getHeatParameters());
            artifactHeatEnv.setArtifactChecksum(checkSum);
            Either<ArtifactDefinition, StorageOperationStatus> addHeatEnvArtifact = addHeatEnvArtifact(artifactHeatEnv, heatArtifact, component,
                parentType, parentId);
            if (addHeatEnvArtifact.isRight()) {
                log.debug("failed to create heat env artifact on resource instance");
                throw new ByResponseFormatComponentException(componentsUtils.getResponseFormatForResourceInstance(
                    componentsUtils.convertFromStorageResponseForResourceInstance(addHeatEnvArtifact.right().value(), false), "", null));
            }
            heatEnvPlaceholder = createArtifactPlaceHolder;
        } else {
            heatEnvPlaceholder = artifactHeatEnv;
            artifactToscaOperation.generateUUID(heatEnvPlaceholder, heatEnvPlaceholder.getArtifactVersion());
            setHeatCurrentValuesOnHeatEnvDefaultValues(heatArtifact, heatEnvPlaceholder);
        }
        ComponentTypeEnum componentType = component.getComponentType();
        if (parentType == NodeTypeEnum.ResourceInstance) {
            componentType = ComponentTypeEnum.RESOURCE_INSTANCE;
        }
        createdArtifacts.add(heatEnvPlaceholder);
        componentsUtils.auditComponent(componentsUtils.getResponseFormat(ActionStatus.OK), user, component, AuditingActionEnum.ARTIFACT_UPLOAD,
            new ResourceCommonInfo(parentName, componentType.getValue()), ResourceVersionInfo.newBuilder().build(),
            ResourceVersionInfo.newBuilder().artifactUuid(heatEnvPlaceholder.getUniqueId()).build(), null, heatEnvPlaceholder, null);
        return heatEnvPlaceholder;
    }

    private void setHeatCurrentValuesOnHeatEnvDefaultValues(ArtifactDefinition artifact, ArtifactDefinition artifactDefinition) {
        if (artifact.getListHeatParameters() == null) {
            return;
        }
        List<HeatParameterDefinition> heatEnvParameters = new ArrayList<>();
        for (HeatParameterDefinition parameter : artifact.getListHeatParameters()) {
            HeatParameterDefinition heatEnvParameter = new HeatParameterDefinition(parameter);
            heatEnvParameter.setDefaultValue(parameter.getCurrentValue());
            heatEnvParameter.setCurrentValue(null);
            heatEnvParameters.add(heatEnvParameter);
        }
        artifactDefinition.setListHeatParameters(heatEnvParameters);
    }

    private void buildHeatEnvFileName(ArtifactDefinition heatArtifact, ArtifactDefinition heatEnvArtifact, Map<String, Object> placeHolderData) {
        String heatExtension = GeneralUtility.getFilenameExtension(heatArtifact.getArtifactName());
        String envExtension = (String) placeHolderData.get(ARTIFACT_PLACEHOLDER_FILE_EXTENSION);
        String name = heatArtifact.getArtifactName();
        String fileName;
        if (name == null) {
            name = heatArtifact.getArtifactLabel();
            fileName = name + "." + envExtension;
        } else {
            fileName = name.replaceAll("." + heatExtension, "." + envExtension);
        }
        heatEnvArtifact.setArtifactName(fileName);
    }

    private void handleEnvArtifactVersion(ArtifactDefinition heatEnvArtifact, Map<String, String> existingEnvVersions) {
        if (null != existingEnvVersions) {
            String prevVersion = existingEnvVersions.get(heatEnvArtifact.getArtifactName());
            if (null != prevVersion) {
                heatEnvArtifact.setArtifactVersion(prevVersion);
            }
        }
    }

    public List<ArtifactDefinition> handleArtifactsForInnerVfcComponent(List<ArtifactDefinition> artifactsToHandle, Resource component, User user,
                                                                        List<ArtifactDefinition> vfcsNewCreatedArtifacts,
                                                                        ArtifactOperationInfo operation, boolean shouldLock, boolean inTransaction) {
        ComponentTypeEnum componentType = component.getComponentType();
        List<ArtifactDefinition> uploadedArtifacts = new ArrayList<>();
        Either<ArtifactDefinition, Operation> result;
        try {
            for (ArtifactDefinition artifactDefinition : artifactsToHandle) {
                result = handleLoadedArtifact(component, user, operation, shouldLock, inTransaction, componentType, artifactDefinition);
                uploadedArtifacts.add(result.left().value());
            }
        } catch (ComponentException e) {
            log.debug(FAILED_UPLOAD_ARTIFACT_TO_COMPONENT, componentType, component.getName(), e.getResponseFormat());
            if (operation.isCreateOrLink()) {
                vfcsNewCreatedArtifacts.addAll(uploadedArtifacts);
            }
            throw e;
        }
        return uploadedArtifacts;
    }

    public Either<ArtifactDefinition, Operation> handleLoadedArtifact(Component component, User user, ArtifactOperationInfo operation,
                                                                      boolean shouldLock, boolean inTransaction, ComponentTypeEnum componentType,
                                                                      ArtifactDefinition artifactDefinition) {
        AuditingActionEnum auditingAction = detectAuditingType(operation, "");
        String componentId = component.getUniqueId();
        String artifactId = artifactDefinition.getUniqueId();
        Either<ArtifactDefinition, Operation> result;
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        //artifact validation
        artifactDefinition = validateArtifact(componentId, componentType, operation, artifactId, artifactDefinition, auditingAction, user, component,
            shouldLock, inTransaction);
        switch (operation.getArtifactOperationEnum()) {
            case CREATE:
                byte[] validPayload = getValidPayload(componentId, artifactDefinition, operation, auditingAction, artifactId, user, componentType,
                    component, null, null);
                result = createArtifact(component, componentId, artifactDefinition, validPayload, componentType, auditingAction, null, null);
                break;
            case UPDATE:
                validPayload = getValidPayload(componentId, artifactDefinition, operation, auditingAction, artifactId, user, componentType, component,
                    null, null);
                result = handleUpdate(componentId, componentType, operation, artifactId, artifactDefinition, validPayload, null, null, null, null,
                    auditingAction, user, component, true);
                break;
            case DELETE:
                result = Either.left(handleDeleteInternal(componentId, artifactId, componentType, component));
                break;
            case DOWNLOAD:
                if (artifactGenerationRequired(component, artifactDefinition)) {
                    result = Either.left(generateNotSavedArtifact(component, artifactDefinition));
                } else {
                    result = Either.left(handleDownload(componentId, artifactId, componentType, component));
                }
                break;
            case LINK:
                result = Either.left(handleLink(componentId, artifactDefinition, componentType, component));
                break;
            default:
                throw new UnsupportedOperationException(
                    "In ArtifactsBusinessLogic received illegal operation: " + operation.getArtifactOperationEnum());
        }
        return result;
    }

    public List<ArtifactDefinition> handleArtifactsRequestForInnerVfcComponent(List<ArtifactDefinition> artifactsToHandle, Resource component,
                                                                               User user, List<ArtifactDefinition> vfcsNewCreatedArtifacts,
                                                                               ArtifactOperationInfo operation, boolean shouldLock,
                                                                               boolean inTransaction) {
        List<ArtifactDefinition> handleArtifactsResult;
        ComponentTypeEnum componentType = component.getComponentType();
        List<ArtifactDefinition> uploadedArtifacts = new ArrayList<>();
        Either<ArtifactDefinition, Operation> actionResult;
        String originData;
        String origMd5;
        try {
            for (ArtifactDefinition artifact : artifactsToHandle) {
                originData = ArtifactUtils.buildJsonStringForCsarVfcArtifact(artifact);
                origMd5 = GeneralUtility.calculateMD5Base64EncodedByString(originData);
                actionResult = handleArtifactRequest(component.getUniqueId(), user.getUserId(), componentType, operation, artifact.getUniqueId(),
                    artifact, origMd5, originData, null, null, null, null, shouldLock, inTransaction);
                uploadedArtifacts.add(actionResult.left().value());
            }
            handleArtifactsResult = uploadedArtifacts;
        } catch (ComponentException e) {
            if (operation.isCreateOrLink()) {
                vfcsNewCreatedArtifacts.addAll(uploadedArtifacts);
            }
            throw e;
        }
        return handleArtifactsResult;
    }

    private ComponentInstance getRelatedComponentInstance(ComponentTypeEnum componentType, String componentUuid, String resourceInstanceName) {
        String normalizedName = ValidationUtils.normalizeComponentInstanceName(resourceInstanceName);
        Option<Component> oComponent = Option.of(getComponentByUuid(componentType, componentUuid));
        return oComponent.toTry(componentNotFound(componentType, componentUuid)).flatMap(
            component -> findFirstMatching(component, ci -> ValidationUtils.normalizeComponentInstanceName(ci.getName()).equals(normalizedName))
                .toTry(componentInstanceNotFound(componentType, resourceInstanceName, component))).get();
    }

    private ImmutablePair<Component, ComponentInstance> getRelatedComponentComponentInstance(Component component, String resourceInstanceName) {
        String normalizedName = ValidationUtils.normalizeComponentInstanceName(resourceInstanceName);
        ComponentInstance componentInstance = findFirstMatching(component,
            ci -> ValidationUtils.normalizeComponentInstanceName(ci.getName()).equals(normalizedName))
            .toTry(componentInstanceNotFound(component.getComponentType(), resourceInstanceName, component)).get();
        return new ImmutablePair<>(component, componentInstance);
    }

    private ImmutablePair<Component, ComponentInstance> getRelatedComponentComponentInstance(ComponentTypeEnum componentType, String componentUuid,
                                                                                             String resourceInstanceName) {
        Component component = getLatestComponentByUuid(componentType, componentUuid);
        ComponentInstance componentInstance = findFirstMatching(component, ci -> ci.getNormalizedName().equals(resourceInstanceName))
            .toTry(componentInstanceNotFound(component.getComponentType(), resourceInstanceName, component)).get();
        return new ImmutablePair<>(component, componentInstance);
    }

    private Supplier<Throwable> componentNotFound(ComponentTypeEnum componentType, String componentUuid) {
        return () -> {
            log.debug(FAILED_FETCH_COMPONENT, componentType.getValue(), componentUuid);
            return new ByActionStatusComponentException(ActionStatus.COMPONENT_NOT_FOUND, componentUuid);
        };
    }

    private Supplier<Throwable> componentInstanceNotFound(ComponentTypeEnum componentType, String resourceInstanceName, Component component) {
        return () -> {
            log.debug(COMPONENT_INSTANCE_NOT_FOUND, resourceInstanceName, component.getName());
            return new ByActionStatusComponentException(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, resourceInstanceName,
                RESOURCE_INSTANCE, componentType.getValue(), component.getName());
        };
    }

    private byte[] downloadArtifact(Map<String, ArtifactDefinition> artifacts, String artifactUUID, String componentName) {
        ImmutablePair<String, byte[]> downloadArtifact;
        List<ArtifactDefinition> artifactsList = null;
        ArtifactDefinition deploymentArtifact;
        if (artifacts != null && !artifacts.isEmpty()) {
            artifactsList = artifacts.values().stream().filter(art -> art.getArtifactUUID() != null && art.getArtifactUUID().equals(artifactUUID))
                .collect(Collectors.toList());
        }
        if (artifactsList == null || artifactsList.isEmpty()) {
            log.debug("Deployment artifact with uuid {} was not found for component {}", artifactUUID, componentName);
            throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_NOT_FOUND, artifactUUID);
        }
        deploymentArtifact = artifactsList.get(0);
        downloadArtifact = downloadArtifact(deploymentArtifact);
        log.trace("Succeeded to download artifact with uniqueId {}", deploymentArtifact.getUniqueId());
        return downloadArtifact.getRight();
    }

    private Component getLatestComponentByUuid(ComponentTypeEnum componentType, String componentUuid) {
        Component component;
        Either<Component, StorageOperationStatus> getComponentRes = toscaOperationFacade.getLatestComponentByUuid(componentUuid);
        if (getComponentRes.isRight()) {
            StorageOperationStatus status = getComponentRes.right().value();
            log.debug(FAILED_FETCH_COMPONENT, componentType, componentUuid, status);
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(status));
        } else {
            component = getComponentRes.left().value();
        }
        return component;
    }

    private Component getComponentByUuid(ComponentTypeEnum componentType, String componentUuid) {
        Component component;
        Either<List<Component>, StorageOperationStatus> getComponentRes = toscaOperationFacade.getComponentListByUuid(componentUuid, null);
        if (getComponentRes.isRight()) {
            StorageOperationStatus status = getComponentRes.right().value();
            log.debug(FAILED_FETCH_COMPONENT, componentType, componentUuid, status);
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(status));
        } else {
            List<Component> value = getComponentRes.left().value();
            if (value.isEmpty()) {
                log.debug("Could not fetch component with type {} and uuid {}.", componentType, componentUuid);
                ActionStatus status = componentType == ComponentTypeEnum.RESOURCE ? ActionStatus.RESOURCE_NOT_FOUND : ActionStatus.SERVICE_NOT_FOUND;
                throw new ByActionStatusComponentException(status);
            } else {
                component = value.get(0);
            }
        }
        return component;
    }

    private String getLatestParentArtifactDataIdByArtifactUUID(String artifactUUID, String parentId, ComponentTypeEnum componentType) {
        ActionStatus actionStatus = ActionStatus.ARTIFACT_NOT_FOUND;
        StorageOperationStatus storageStatus;
        ArtifactDefinition latestArtifact;
        List<ArtifactDefinition> artifacts;
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getArtifactsRes = artifactToscaOperation.getArtifacts(parentId);
        if (getArtifactsRes.isRight()) {
            storageStatus = getArtifactsRes.right().value();
            log.debug("Couldn't fetch artifacts data for parent component {} with uid {}, error: {}", componentType, parentId, storageStatus);
            if (storageStatus != StorageOperationStatus.NOT_FOUND) {
                actionStatus = componentsUtils.convertFromStorageResponse(storageStatus);
            }
            throw new ByActionStatusComponentException(actionStatus, artifactUUID);
        }
        artifacts = getArtifactsRes.left().value().values().stream()
            .filter(a -> a.getArtifactUUID() != null && a.getArtifactUUID().equals(artifactUUID)).collect(Collectors.toList());
        if (artifacts == null || artifacts.isEmpty()) {
            log.debug("Couldn't fetch artifact with UUID {} data for parent component {} with uid {}, error: {}", artifactUUID, componentType,
                parentId, actionStatus);
            throw new ByActionStatusComponentException(actionStatus, artifactUUID);
        }
        latestArtifact = artifacts.stream().max((a1, a2) -> {
            int compareRes = Double.compare(Double.parseDouble(a1.getArtifactVersion()), Double.parseDouble(a2.getArtifactVersion()));
            if (compareRes == 0) {
                compareRes = Long.compare(a1.getLastUpdateDate() == null ? 0 : a1.getLastUpdateDate(),
                    a2.getLastUpdateDate() == null ? 0 : a2.getLastUpdateDate());
            }
            return compareRes;
        }).get();
        if (latestArtifact == null) {
            log.debug("Couldn't fetch latest artifact with UUID {} data for parent component {} with uid {}, error: {}", artifactUUID, componentType,
                parentId, actionStatus);
            throw new ByActionStatusComponentException(actionStatus, artifactUUID);
        }
        return latestArtifact.getUniqueId();
    }

    private Component checkoutParentComponent(ComponentTypeEnum componentType, String parentId, String userId) {
        Component component = null;
        User modifier = userBusinessLogic.getUser(userId, false);
        LifecycleChangeInfoWithAction changeInfo = new LifecycleChangeInfoWithAction("External API checkout",
            LifecycleChanceActionEnum.UPDATE_FROM_EXTERNAL_API);
        Either<? extends Component, ResponseFormat> checkoutRes = lifecycleBusinessLogic
            .changeComponentState(componentType, parentId, modifier, LifeCycleTransitionEnum.CHECKOUT, changeInfo, false, true);
        if (checkoutRes.isRight()) {
            log.debug("Could not change state of component {} with uid {} to checked out. Status is {}. ", componentType.getNodeType(), parentId,
                checkoutRes.right().value().getStatus());
            throw new ByResponseFormatComponentException(checkoutRes.right().value());
        }
        return checkoutRes.left().value();
    }

    @Autowired
    void setNodeTemplateOperation(NodeTemplateOperation nodeTemplateOperation) {
        this.nodeTemplateOperation = nodeTemplateOperation;
    }

    public List<ArtifactConfiguration> getConfiguration() {
        return ConfigurationManager.getConfigurationManager().getConfiguration().getArtifacts();
    }

    public Map<String, ArtifactTypeDefinition> getAllToscaArtifacts(final String modelName) {
        if (StringUtils.isNotEmpty(modelName)) {
            artifactTypeOperation.validateModel(modelName);
        }
        return artifactTypeOperation.getAllArtifactTypes(modelName);
    }

    public enum ArtifactOperationEnum {
        CREATE, UPDATE, DELETE, DOWNLOAD, LINK;

        public static boolean isCreateOrLink(ArtifactOperationEnum operation) {
            return operation == CREATE || operation == LINK;
        }
    }
}
