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

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fj.data.Either;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.elasticsearch.common.Strings;
import org.openecomp.sdc.be.components.ArtifactsResolver;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.artifact.ArtifactTypeToPayloadTypeSelector;
import org.openecomp.sdc.be.components.impl.artifact.PayloadTypeEnum;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction.LifecycleChanceActionEnum;
import org.openecomp.sdc.be.components.utils.InterfaceOperationUtils;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.Configuration.ArtifactTypeConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.info.ArtifactTemplateInfo;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ArtifactType;
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
import org.openecomp.sdc.be.model.jsontitan.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IHeatParametersOperation;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.IUserAdminOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.tosca.ToscaError;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.tosca.ToscaRepresentation;
import org.openecomp.sdc.be.user.IUserBusinessLogic;
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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.http.HttpServletRequest;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component("artifactBusinessLogic")
public class ArtifactsBusinessLogic extends BaseBusinessLogic {
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

    public static final String HEAT_ENV_NAME = "heatEnv";
    public static final String HEAT_VF_ENV_NAME = "VfHeatEnv";
    public static final String HEAT_ENV_SUFFIX = "env";
    private static final String ARTIFACT_PLACEHOLDER_FILE_EXTENSION = "fileExtension";

    private static final Logger log = Logger.getLogger(ArtifactsBusinessLogic.class);
    public static final String FAILED_UPDATE_GROUPS = "Failed to update groups of the component {}. ";
    public static final String FAILED_UPDATE_ARTIFACT = "Failed to delete or update the artifact {}. Parent uniqueId is {}";
    public static final String FAILED_SAVE_ARTIFACT = "Failed to save the artifact.";
    public static final String UPDATE_ARTIFACT_LOCK = "Update Artifact - lock ";
    public static final String FAILED_DOWNLOAD_ARTIFACT = "Download artifact {} failed";
    public static final String FAILED_UPLOAD_ARTIFACT_TO_COMPONENT = "Failed to upload artifact to component with type {} and uuid {}. Status is {}. ";
    public static final String FAILED_UPLOAD_ARTIFACT_TO_INSTANCE = "Failed to upload artifact to component instance {} of component with type {} and uuid {}. Status is {}. ";
    public static final String FAILED_FETCH_COMPONENT = "Could not fetch component with type {} and uuid {}. Status is {}. ";
    public static final String NULL_PARAMETER = "One of the function parameteres is null";
    public static final String COMPONENT_INSTANCE_NOT_FOUND = "Component instance {} was not found for component {}";
    public static final String ROLLBACK = "all changes rollback";
    public static final String COMMIT = "all changes committed";
    public static final String ARTIFACT_SAVED = "Artifact saved into ES - {}";
    public static final String UPDATE_ARTIFACT = "Update Artifact";
    public static final String FOUND_DEPLOYMENT_ARTIFACT = "Found deployment artifact {}";
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @javax.annotation.Resource
    private IInterfaceLifecycleOperation interfaceLifecycleOperation;
    @javax.annotation.Resource
    private IUserAdminOperation userOperaton;

    @javax.annotation.Resource
    private IElementOperation elementOperation;

    @javax.annotation.Resource
    private ResourceBusinessLogic resourceBusinessLogic;

    @javax.annotation.Resource
    private ServiceBusinessLogic serviceBusinessLogic;

    @javax.annotation.Resource
    private UserBusinessLogic userAdminManager;

    @javax.annotation.Resource
    private IHeatParametersOperation heatParametersOperation;

    @Autowired
    private ArtifactCassandraDao artifactCassandraDao;

    @Autowired
    private ToscaExportHandler toscaExportUtils;

    @Autowired
    private CsarUtils csarUtils;

    @Autowired
    private LifecycleBusinessLogic lifecycleBusinessLogic;

    @Autowired
    private IUserBusinessLogic userBusinessLogic;

    @Autowired
    private NodeTemplateOperation nodeTemplateOperation;

    @Autowired
    private ArtifactsResolver artifactsResolver;

    public enum ArtifactOperationEnum {
        CREATE, UPDATE, DELETE, DOWNLOAD, LINK;

        public static boolean isCreateOrLink(ArtifactOperationEnum operation) {
            return operation == CREATE || operation == LINK;
        }
    }

    public class ArtifactOperationInfo {

        private ArtifactOperationEnum artifactOperationEnum;
        private boolean isExternalApi;
        private boolean ignoreLifecycleState;

        public ArtifactOperationInfo(boolean isExternalApi, boolean ignoreLifecycleState, ArtifactOperationEnum artifactOperationEnum) {
            this.artifactOperationEnum = artifactOperationEnum;
            this.isExternalApi = isExternalApi;
            this.ignoreLifecycleState = ignoreLifecycleState;
        }

        public boolean isExternalApi() {
            return isExternalApi;
        }

        public boolean ignoreLifecycleState() {
            return ignoreLifecycleState;
        }

        public ArtifactOperationEnum getArtifactOperationEnum() {
            return artifactOperationEnum;
        }

    }

    // new flow US556184
    public Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleArtifactRequest(String componentId, String userId, ComponentTypeEnum componentType, ArtifactOperationInfo operation, String artifactId, ArtifactDefinition artifactInfo,
                                                                                               String origMd5, String originData, String interfaceUuid, String operationUuid, String parentId, String containerComponentType) {
        return handleArtifactRequest(componentId, userId, componentType, operation, artifactId, artifactInfo, origMd5, originData, interfaceUuid, operationUuid, parentId, containerComponentType, true, false);
    }

    public Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleArtifactRequest(String componentId, String userId, ComponentTypeEnum componentType, ArtifactOperationInfo operation, String artifactId, ArtifactDefinition artifactInfo,
                                                                                               String origMd5, String originData, String interfaceUuid, String operationUuid, String parentId, String containerComponentType, boolean shouldLock, boolean inTransaction) {

        // step 1 - detect auditing type
        AuditingActionEnum auditingAction = detectAuditingType(operation, origMd5);
        // step 2 - check header
        if (userId == null) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION);
            log.debug("handleArtifactRequest - no HTTP_CSP_HEADER , component id {}", componentId);
            handleAuditing(auditingAction, null, componentId, null, null, null, artifactId, responseFormat, componentType, null);
            return Either.right(responseFormat);
        }
        // step 3 - check user existence
        Either<User, ResponseFormat> userResult = validateUserExists(userId, auditingAction, componentId, artifactId, componentType, inTransaction);
        if (userResult.isRight()) {
            return Either.right(userResult.right().value());
        }

        // step 4 - check user's role
        User user = userResult.left().value();
        Either<Boolean, ResponseFormat> validateUserRole = validateUserRole(user, auditingAction, componentId, artifactId, componentType, operation);
        if (validateUserRole.isRight()) {
            return Either.right(validateUserRole.right().value());
        }

        // steps 5 - 6 - 7
        // 5. check service/resource existence
        // 6. check service/resource check out
        // 7. user is owner of checkout state
        org.openecomp.sdc.be.model.Component component = null;
        String realComponentId = componentType == ComponentTypeEnum.RESOURCE_INSTANCE ? parentId : componentId;
        Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponent = validateComponentExists(realComponentId, auditingAction, user, artifactId, componentType, containerComponentType);
        if (validateComponent.isRight()) {
            return Either.right(validateComponent.right().value());
        }
        component = validateComponent.left().value();
        Either<Boolean, ResponseFormat> validateWorkOnResource = validateWorkOnComponent(component, userId, auditingAction, user, artifactId, operation);
        if (validateWorkOnResource.isRight()) {
            return Either.right(validateWorkOnResource.right().value());
        }
        // step 8

        return validateAndHandleArtifact(componentId, componentType, operation, artifactId, artifactInfo, origMd5, originData, interfaceUuid, operationUuid, user, component,
                shouldLock, inTransaction, true);
    }

    /**
     * This Method validates only the Artifact and does not validate user / role / component ect...<br>
     * For regular usage use <br>
     * {@link #handleArtifactRequest(String, String, ComponentTypeEnum, ArtifactOperationInfo, String, ArtifactDefinition, String, String, String, String, String, String)}
     *
     * @return
     */
    public Either<Either<ArtifactDefinition, Operation>, ResponseFormat> validateAndHandleArtifact(String componentUniqueId, ComponentTypeEnum componentType, ArtifactOperationInfo operation, String artifactUniqueId,
                                                                                                   ArtifactDefinition artifactDefinition, String origMd5, String originData, String interfaceUuid, String operationName, User user, Component component, boolean shouldLock, boolean inTransaction, boolean needUpdateGroup) {
        Component parent = component;
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();

        AuditingActionEnum auditingAction = detectAuditingType(operation, origMd5);
        artifactDefinition = validateArtifact(componentUniqueId, componentType, operation, artifactUniqueId, artifactDefinition, auditingAction, user, component, parent, errorWrapper, shouldLock, inTransaction);

        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> result;
        if (errorWrapper.isEmpty()) {
            // step 10
            result = doAction(componentUniqueId, componentType, operation, artifactUniqueId, artifactDefinition, origMd5, originData, interfaceUuid, operationName, auditingAction, user, parent, shouldLock, inTransaction, needUpdateGroup);
        }
        else {
            result = Either.right(errorWrapper.getInnerElement());
        }
        return result;
    }

    private ArtifactDefinition validateArtifact(String componentId, ComponentTypeEnum componentType, ArtifactOperationInfo operation, String artifactId, ArtifactDefinition artifactInfo, AuditingActionEnum auditingAction, User user,
                                                Component component, Component parent, Wrapper<ResponseFormat> errorWrapper, boolean shouldLock, boolean inTransaction) {
        ArtifactDefinition validatedArtifactInfo = artifactInfo;
        if (operation.getArtifactOperationEnum() == ArtifactOperationEnum.UPDATE || operation.getArtifactOperationEnum() == ArtifactOperationEnum.DELETE || operation
                .getArtifactOperationEnum() == ArtifactOperationEnum.DOWNLOAD) {
            Either<ArtifactDefinition, ResponseFormat> validateArtifact = validateArtifact(componentId, componentType, artifactId, component);
            if (validateArtifact.isRight()) {
                ResponseFormat responseFormat = validateArtifact.right().value();
                handleAuditing(auditingAction, parent, componentId, user, null, null, artifactId, responseFormat, componentType, null);
                errorWrapper.setInnerElement(validateArtifact.right().value());
            }
            else if (operation.getArtifactOperationEnum() == ArtifactOperationEnum.DOWNLOAD) {
                validatedArtifactInfo = validateArtifact.left().value();
                    handleHeatEnvDownload(componentId, componentType, user, component, validateArtifact, errorWrapper, shouldLock, inTransaction);
            }
        }
        return validatedArtifactInfo;
    }

    private void handleHeatEnvDownload(String componentId, ComponentTypeEnum componentType, User user, org.openecomp.sdc.be.model.Component component, Either<ArtifactDefinition,
            ResponseFormat> validateArtifact, Wrapper<ResponseFormat> errorWrapper, boolean shouldLock, boolean inTransaction) {
        ArtifactDefinition validatedArtifact = validateArtifact.left().value();

        if (validatedArtifact.getArtifactType().equalsIgnoreCase(ArtifactTypeEnum.HEAT_ENV.getType())
                && ComponentTypeEnum.SERVICE == component.getComponentType()) {
            ComponentInstance componentInstance = component.getComponentInstances()
                                                           .stream()
                                                           .filter(p -> p.getUniqueId().equals(componentId))
                                                           .findAny()
                                                           .get();
            Map<String, ArtifactDefinition> deploymentArtifacts = componentInstance.getDeploymentArtifacts();

            ArtifactDefinition heatEnvWithHeatParams = deploymentArtifacts.values()
                                                                          .stream()
                                                                          .filter(p -> p.getUniqueId()
                                                                                        .equals(validatedArtifact.getUniqueId()))
                                                                          .findAny()
                                                                          .get();
            Either<ArtifactDefinition, ResponseFormat> eitherGenerated = generateHeatEnvArtifact(heatEnvWithHeatParams, componentType, component, componentInstance
                    .getName(), user, componentId, shouldLock, inTransaction);
            if (eitherGenerated.isRight()) {
                errorWrapper.setInnerElement(eitherGenerated.right().value());
            }
        }
    }

    private boolean artifactGenerationRequired(org.openecomp.sdc.be.model.Component component, ArtifactDefinition artifactInfo) {
        boolean needGenerate;
        needGenerate = artifactInfo.getArtifactGroupType() == ArtifactGroupTypeEnum.TOSCA && (component.getLifecycleState() == LifecycleStateEnum.NOT_CERTIFIED_CHECKIN || component
                .getLifecycleState() == LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        needGenerate = needGenerate || (ComponentTypeEnum.RESOURCE == component.getComponentType() && (artifactInfo.getArtifactType()
                                                                                                                   .equalsIgnoreCase(ArtifactTypeEnum.HEAT_ENV
                                                                                                                           .getType()) || isAbstractVfcEmptyCsar((Resource) component, artifactInfo)));
        return needGenerate;
    }

    private boolean isAbstractVfcEmptyCsar(Resource resource, ArtifactDefinition artifactInfo) {
        return resource.isAbstract() && artifactInfo.getArtifactGroupType() == ArtifactGroupTypeEnum.TOSCA && artifactInfo
                .getArtifactType()
                .equals(ArtifactTypeEnum.TOSCA_CSAR.getType()) && StringUtils.isEmpty(artifactInfo.getArtifactChecksum());
    }

    public Either<Either<ArtifactDefinition, Operation>, ResponseFormat> generateAndSaveToscaArtifact(ArtifactDefinition artifactDefinition, org.openecomp.sdc.be.model.Component component, User user, boolean isInCertificationRequest,
                                                                                                      boolean shouldLock, boolean inTransaction, boolean fetchTemplatesFromDB) {

        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> generated = generateToscaArtifact(component, artifactDefinition, isInCertificationRequest, fetchTemplatesFromDB);
        if (generated.isRight()) {
            return generated;
        }
        byte[] decodedPayload = artifactDefinition.getPayloadData();
        artifactDefinition.setEsId(artifactDefinition.getUniqueId());
        artifactDefinition.setArtifactChecksum(GeneralUtility.calculateMD5Base64EncodedByByteArray(decodedPayload));
        return lockComponentAndUpdateArtifact(component.getUniqueId(), artifactDefinition, AuditingActionEnum.ARTIFACT_PAYLOAD_UPDATE, artifactDefinition
                        .getUniqueId(), user, component.getComponentType(), component, decodedPayload, null, null,
                shouldLock, inTransaction);

    }

    private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> generateToscaArtifact(Component parent, ArtifactDefinition artifactInfo, boolean isInCertificationRequest, boolean fetchTemplatesFromDB) {
        log.debug("tosca artifact generation");
        if (artifactInfo.getArtifactType().equals(ArtifactTypeEnum.TOSCA_CSAR.getType())) {
            Either<byte[], ResponseFormat> generated = csarUtils.createCsar(parent, fetchTemplatesFromDB, isInCertificationRequest);

            if (generated.isRight()) {
                log.debug("Failed to export tosca csar for component {} error {}", parent.getUniqueId(), generated.right()
                                                                                                                  .value());

                return Either.right(generated.right().value());
            }
            byte[] value = generated.left().value();
            artifactInfo.setPayload(value);

        }
        else {
            Either<ToscaRepresentation, ToscaError> exportComponent = toscaExportUtils.exportComponent(parent);
            if (exportComponent.isRight()) {
                log.debug("Failed export tosca yaml for component {} error {}", parent.getUniqueId(), exportComponent.right()
                                                                                                                     .value());
                ActionStatus status = componentsUtils.convertFromToscaError(exportComponent.right().value());
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(status);
                return Either.right(responseFormat);
            }
            log.debug("Tosca yaml exported for component {} ", parent.getUniqueId());
            String payload = exportComponent.left().value().getMainYaml();
            artifactInfo.setPayloadData(payload);
        }
        return Either.left(Either.left(artifactInfo));
    }

    private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> doAction(String componentId, ComponentTypeEnum componentType, ArtifactOperationInfo operation, String artifactId, ArtifactDefinition artifactInfo, String origMd5,
                                                                                   String originData, String interfaceName, String operationName, AuditingActionEnum auditingAction, User user, org.openecomp.sdc.be.model.Component parent, boolean shouldLock, boolean inTransaction, boolean needUpdateGroup) {
        switch (operation.getArtifactOperationEnum()) {
            case DOWNLOAD:
                if (artifactGenerationRequired(parent, artifactInfo)) {
                    return generateNotSavedArtifact(parent, artifactInfo);
                }
                return handleDownload(componentId, artifactId, user, auditingAction, componentType, parent);
            case DELETE:
                return handleDelete(componentId, artifactId, user, auditingAction, componentType, parent, shouldLock, inTransaction);
            case UPDATE:
                Either<Either<ArtifactDefinition, Operation>, ResponseFormat> result = null;
                ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifactInfo.getArtifactType());
                if (componentType.equals(ComponentTypeEnum.RESOURCE_INSTANCE)
                        && (artifactType == ArtifactTypeEnum.HEAT || artifactType == ArtifactTypeEnum.HEAT_VOL || artifactType == ArtifactTypeEnum.HEAT_NET || artifactType == ArtifactTypeEnum.HEAT_ENV)) {
                    result = handleUpdateHeatEnv(componentId, artifactInfo, auditingAction, artifactId, user, componentType, parent, originData, origMd5, operation, shouldLock, inTransaction);
                    if (needUpdateGroup && result.isLeft()) {
                        Either<ArtifactDefinition, Operation> updateResult = result.left().value();
                        ActionStatus error = updateGroupInstance(artifactInfo, updateResult.left()
                                                                                           .value(), parent, componentType, componentId);
                        if (error != ActionStatus.OK) {
                            result = Either.right(componentsUtils.getResponseFormat(error));
                        }
                    }
                }
                else {
                    if (componentType.equals(ComponentTypeEnum.RESOURCE) && artifactType == ArtifactTypeEnum.HEAT_ENV) {
                        result = handleUpdateHeatWithHeatEnvParams(componentId, artifactInfo, auditingAction, artifactId, user, componentType, parent, originData, origMd5, operation, shouldLock, inTransaction, needUpdateGroup);
                    }
                }
                if (result == null) {
                    result = handleUpdate(componentId, artifactInfo, operation, auditingAction, artifactId, user, componentType, parent, origMd5, originData, interfaceName, operationName, shouldLock, inTransaction);
                    if (needUpdateGroup && result.isLeft()) {
                        Either<ArtifactDefinition, Operation> updateResult = result.left().value();

                        ActionStatus error = updateGroupForHeat(artifactInfo, updateResult.left()
                                                                                          .value(), parent, componentType);
                        if (error != ActionStatus.OK) {
                            result = Either.right(componentsUtils.getResponseFormat(error));
                        }
                    }
                }
                return result;
            case CREATE:
                return handleCreate(componentId, artifactInfo, operation, auditingAction, user, componentType, parent, origMd5, originData, interfaceName, operationName, shouldLock, inTransaction);
            case LINK:
                return handleLink(componentId, artifactInfo, auditingAction, user, componentType, parent, shouldLock, inTransaction);
        }
        return null;
    }

    private ActionStatus updateGroupForHeat(ArtifactDefinition artifactInfo, ArtifactDefinition artAfterUpdate, Component parent, ComponentTypeEnum componentType) {
        List<GroupDefinition> groups = parent.getGroups();
        if (groups != null && !groups.isEmpty()) {
            List<GroupDataDefinition> groupToUpdate = groups.stream()
                                                            .filter(g -> g.getArtifacts() != null && g.getArtifacts()
                                                                                                      .contains(artifactInfo
                                                                                                              .getUniqueId()))
                                                            .collect(Collectors.toList());
            if (groupToUpdate != null && !groupToUpdate.isEmpty()) {
                groupToUpdate.forEach(g -> {
                    g.getArtifacts().remove(artifactInfo.getUniqueId());
                    g.getArtifactsUuid().remove(artifactInfo.getArtifactUUID());
                    g.getArtifacts().add(artAfterUpdate.getUniqueId());
                    g.getArtifactsUuid().add(artAfterUpdate.getArtifactUUID());
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

    private ActionStatus updateGroupForHeat(ArtifactDefinition artifactInfoHeat, ArtifactDefinition artHeatAfterUpdate, ArtifactDefinition artifactInfoHeatE, ArtifactDefinition artHEAfterUpdate, Component parent, ComponentTypeEnum componentType) {
        List<GroupDefinition> groups = parent.getGroups();
        if (groups != null && !groups.isEmpty()) {
            List<GroupDataDefinition> groupToUpdate = groups.stream()
                                                            .filter(g -> g.getArtifacts() != null && g.getArtifacts()
                                                                                                      .contains(artifactInfoHeat
                                                                                                              .getUniqueId()))
                                                            .collect(Collectors.toList());
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

    private ActionStatus updateGroupInstance(ArtifactDefinition artifactInfo, ArtifactDefinition artAfterUpdate, Component parent, ComponentTypeEnum componentType, String parentId) {
        List<GroupInstance> updatedGroupInstances = new ArrayList<>();
        List<GroupInstance> groupInstances = null;
        Optional<ComponentInstance> componentInstOp = parent.getComponentInstances()
                                                            .stream()
                                                            .filter(ci -> ci.getUniqueId().equals(parentId))
                                                            .findFirst();
        if (componentInstOp.isPresent()) {
            groupInstances = componentInstOp.get().getGroupInstances();
        }
        if (CollectionUtils.isNotEmpty(groupInstances)) {
            boolean isUpdated = false;
            for (GroupInstance groupInstance : groupInstances) {
                isUpdated = false;
                if (CollectionUtils.isNotEmpty(groupInstance.getGroupInstanceArtifacts()) && groupInstance.getGroupInstanceArtifacts()
                                                                                                          .contains(artifactInfo
                                                                                                                  .getUniqueId())) {
                    groupInstance.getGroupInstanceArtifacts().remove(artifactInfo.getUniqueId());
                    groupInstance.getGroupInstanceArtifacts().add(artAfterUpdate.getUniqueId());
                    isUpdated = true;
                }
                if (CollectionUtils.isNotEmpty(groupInstance.getGroupInstanceArtifactsUuid()) && groupInstance.getGroupInstanceArtifactsUuid()
                                                                                                              .contains(artifactInfo
                                                                                                                      .getArtifactUUID())) {
                    groupInstance.getGroupInstanceArtifactsUuid().remove(artifactInfo.getArtifactUUID());
                    groupInstance.getGroupInstanceArtifacts().add(artAfterUpdate.getArtifactUUID());
                    isUpdated = true;
                }
                if (isUpdated) {
                    updatedGroupInstances.add(groupInstance);
                }
            }
        }
        Either<List<GroupInstance>, StorageOperationStatus> status = toscaOperationFacade.updateGroupInstancesOnComponent(parent, parentId, updatedGroupInstances);
        if (status.isRight()) {
            log.debug(FAILED_UPDATE_GROUPS, parent.getUniqueId());
            return componentsUtils.convertFromStorageResponse(status.right().value());
        }
        return ActionStatus.OK;
    }

    Either<Either<ArtifactDefinition, Operation>, ResponseFormat> generateNotSavedArtifact(Component parent, ArtifactDefinition artifactInfo) {
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> result;
        if (artifactInfo.getArtifactGroupType() == ArtifactGroupTypeEnum.TOSCA) {
            result = generateToscaArtifact(parent, artifactInfo, false, false);
        }
        else {
            String heatArtifactId = artifactInfo.getGeneratedFromId();
            Either<ArtifactDefinition, StorageOperationStatus> heatRes = artifactToscaOperation.getArtifactById(parent.getUniqueId(), heatArtifactId);
            if (heatRes.isRight()) {
                log.debug("Failed to fetch heat artifact by generated id {} for heat env {}", heatArtifactId, artifactInfo
                        .getUniqueId());
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(heatRes
                        .right()
                        .value()), "");
                return Either.right(responseFormat);
            }
            String generatedPayload = generateHeatEnvPayload(heatRes.left().value());
            artifactInfo.setPayloadData(generatedPayload);
            result = Either.left(Either.left(artifactInfo));
        }
        return result;
    }

    private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleUpdateHeatWithHeatEnvParams(String componentId, ArtifactDefinition artifactInfo, AuditingActionEnum auditingAction, String artifactId, User user,
                                                                                                            ComponentTypeEnum componentType, Component parent, String originData, String origMd5, ArtifactOperationInfo operation, boolean shouldLock, boolean inTransaction, boolean needToUpdateGroup) {
        convertParentType(componentType);
        String parentId = parent.getUniqueId();
        Either<ArtifactDefinition, StorageOperationStatus> artifactHeatRes = artifactToscaOperation.getArtifactById(componentId, artifactInfo
                .getGeneratedFromId());
        ArtifactDefinition currHeatArtifact = artifactHeatRes.left().value();

        if (origMd5 != null) {
            Either<Boolean, ResponseFormat> validateMd5 = validateMd5(origMd5, originData, artifactInfo.getPayloadData(), operation);
            if (validateMd5.isRight()) {
                ResponseFormat responseFormat = validateMd5.right().value();
                handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
                return Either.right(responseFormat);
            }

            if (artifactInfo.getPayloadData() != null && artifactInfo.getPayloadData().length != 0) {

                Either<byte[], ResponseFormat> payloadEither = handlePayload(artifactInfo, isArtifactMetadataUpdate(auditingAction));
                if (payloadEither.isRight()) {
                    ResponseFormat responseFormat = payloadEither.right().value();
                    handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
                    return Either.right(responseFormat);
                }
            }
            else { // duplicate
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, ARTIFACT_PAYLOAD);
                handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
                return Either.right(responseFormat);
            }
        }

        // lock resource
        if (shouldLock) {
            Either<Boolean, ResponseFormat> lockComponent = lockComponent(parent, UPDATE_ARTIFACT_LOCK);
            if (lockComponent.isRight()) {
                handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, lockComponent.right()
                                                                                                            .value(), componentType, null);
                return Either.right(lockComponent.right().value());
            }
        }
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> resultOp = null;
        try {
            resultOp = updateHeatParams(componentId, artifactId, artifactInfo, user, auditingAction, parent, componentType, currHeatArtifact, needToUpdateGroup);
            return resultOp;

        }
        finally {
            // unlock resource
            if (resultOp == null || resultOp.isRight()) {
                log.debug(ROLLBACK);
                if (!inTransaction) {
                    titanDao.rollback();
                }
            }
            else {
                log.debug(COMMIT);
                if (!inTransaction) {
                    titanDao.commit();
                }
            }
            if (shouldLock) {
                graphLockOperation.unlockComponent(parent.getUniqueId(), parent.getComponentType().getNodeType());
            }
        }
    }

    public Either<ImmutablePair<String, byte[]>, ResponseFormat> handleDownloadToscaModelRequest(Component component, ArtifactDefinition csarArtifact) {
        if (artifactGenerationRequired(component, csarArtifact)) {
            Either<byte[], ResponseFormat> generated = csarUtils.createCsar(component, false, false);

            if (generated.isRight()) {
                log.debug("Failed to export tosca csar for component {} error {}", component.getUniqueId(), generated.right()
                                                                                                                     .value());

                return Either.right(generated.right().value());
            }
            return Either.left(new ImmutablePair<String, byte[]>(csarArtifact.getArtifactName(), generated.left()
                                                                                                          .value()));
        }
        return downloadArtifact(csarArtifact);
    }

    public Either<ImmutablePair<String, byte[]>, ResponseFormat> handleDownloadRequestById(String componentId, String artifactId, String userId, ComponentTypeEnum componentType, String parentId, String containerComponentType) {
        // perform all validation in common flow
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> result = handleArtifactRequest(componentId, userId, componentType, new ArtifactOperationInfo(false, false, ArtifactOperationEnum.DOWNLOAD), artifactId, null, null, null, null,
                null, parentId, containerComponentType);
        if (result.isRight()) {
            return Either.right(result.right().value());
        }
        ArtifactDefinition artifactDefinition;
        Either<ArtifactDefinition, Operation> insideValue = result.left().value();
        if (insideValue.isLeft()) {
            artifactDefinition = insideValue.left().value();
        }
        else {
            artifactDefinition = insideValue.right().value().getImplementationArtifact();
        }
        // for tosca artifacts and heat env on VF level generated on download without saving
        if (artifactDefinition.getPayloadData() != null) {
            return Either.left(new ImmutablePair<String, byte[]>(artifactDefinition.getArtifactName(), artifactDefinition
                    .getPayloadData()));
        }
        return downloadArtifact(artifactDefinition);
    }

    public Either<Map<String, ArtifactDefinition>, ResponseFormat> handleGetArtifactsByType(String containerComponentType, String parentId, ComponentTypeEnum componentType, String componentId, String artifactGroupType, String userId) {
        // step 1
        // detect auditing type
        Map<String, ArtifactDefinition> resMap = null;
        Either<Map<String, ArtifactDefinition>, ResponseFormat> resultOp = null;

        new Wrapper<>();
        // step 2
        // check header
        if (userId == null) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION);
            log.debug("handleGetArtifactsByType - no HTTP_CSP_HEADER , component id {}", componentId);

            resultOp = Either.right(responseFormat);
            return resultOp;
        }
        // step 3
        // check user existence
        // step 4
        // check user's role

        validateUserExists(userId, "get artifacts", false);
        // steps 5 - 6 - 7
        // 5. check service/resource existence
        // 6. check service/resource check out
        // 7. user is owner of checkout state
        org.openecomp.sdc.be.model.Component component = null;
        String realComponentId = componentType == ComponentTypeEnum.RESOURCE_INSTANCE ? parentId : componentId;
        ComponentParametersView componentFilter = new ComponentParametersView();
        componentFilter.disableAll();
        componentFilter.setIgnoreArtifacts(false);
        if (componentType == ComponentTypeEnum.RESOURCE_INSTANCE) {
            componentFilter.setIgnoreComponentInstances(false);
        }

        Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponent = validateComponentExistsByFilter(realComponentId, ComponentTypeEnum
                .findByParamName(containerComponentType), componentFilter);

        if (validateComponent.isRight()) {
            resultOp = Either.right(validateComponent.right().value());
            return resultOp;
        }
        component = validateComponent.left().value();
        Either<Boolean, ResponseFormat> lockComponent = lockComponent(component, UPDATE_ARTIFACT_LOCK);
        if (lockComponent.isRight()) {

            resultOp = Either.right(lockComponent.right().value());
            return resultOp;
        }

        try {
            ArtifactGroupTypeEnum groupType = ArtifactGroupTypeEnum.findType(artifactGroupType);

            if (groupType == null) {
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION);
                log.debug("handleGetArtifactsByType - not falid groupType {} , component id {}", artifactGroupType, componentId);

                resultOp = Either.right(responseFormat);
                return resultOp;

            }
            if (groupType == ArtifactGroupTypeEnum.DEPLOYMENT) {
                List<ArtifactDefinition> list = getDeploymentArtifacts(component, componentType.getNodeType(), componentId);
                if (list != null && !list.isEmpty()) {
                    resMap = list.stream().collect(Collectors.toMap(a -> a.getArtifactLabel(), a -> a));
                }
                else {
                    resMap = new HashMap<>();
                }
                resultOp = Either.left(resMap);
                return resultOp;
            }
            else {

                Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifactsMapStatus = getArtifacts(realComponentId, componentType
                        .getNodeType(), groupType, componentId);
                if (artifactsMapStatus.isRight()) {
                    if (artifactsMapStatus.right().value() != StorageOperationStatus.NOT_FOUND) {
                        ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION);
                        log.debug("handleGetArtifactsByType - not falid groupType {} , component id {}", artifactGroupType, componentId);
                        resultOp = Either.right(responseFormat);
                    }
                    else {
                        resMap = new HashMap<>();
                        resultOp = Either.left(resMap);
                    }
                }
                else {
                    resMap = artifactsMapStatus.left().value();
                    resultOp = Either.left(resMap);
                }
                return resultOp;
            }
        }
        finally {
            // unlock resource
            if (resultOp == null || resultOp.isRight()) {
                log.debug(ROLLBACK);
                titanDao.rollback();
            }
            else {
                log.debug(COMMIT);
                titanDao.commit();
            }

            componentType = component.getComponentType();
            NodeTypeEnum nodeType = componentType.getNodeType();
            graphLockOperation.unlockComponent(component.getUniqueId(), nodeType);
        }

    }

    private Either<ArtifactDefinition, ResponseFormat> validateArtifact(String componentId, ComponentTypeEnum componentType, String artifactId, Component component) {
        // step 9
        // check artifact existence
        Either<ArtifactDefinition, StorageOperationStatus> artifactResult = artifactToscaOperation.getArtifactById(componentId, artifactId, componentType, component
                .getUniqueId());
        if (artifactResult.isRight()) {
            if (artifactResult.right().value().equals(StorageOperationStatus.ARTIFACT_NOT_FOUND)) {
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, "");
                log.debug("addArtifact - artifact {} not found", artifactId);
                return Either.right(responseFormat);

            }
            else {
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(artifactResult
                        .right()
                        .value()));
                log.debug("addArtifact - failed to fetch artifact {}, error {}", artifactId, artifactResult.right()
                                                                                                           .value());
                return Either.right(responseFormat);
            }
        }
        // step 9.1
        // check artifact belong to component
        boolean found = false;
        switch (componentType) {
            case RESOURCE:
            case SERVICE:
                found = checkArtifactInComponent(component, artifactId);
                break;
            case RESOURCE_INSTANCE:
                found = checkArtifactInResourceInstance(component, componentId, artifactId);
                break;
            default:

        }
        if (!found) {
            String componentName = componentType.name().toLowerCase();
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_ARTIFACT_NOT_FOUND, componentName);
            log.debug("addArtifact - Component artifact not found component Id {}, artifact id {}", componentId, artifactId);
            return Either.right(responseFormat);
        }
        return Either.left(artifactResult.left().value());
    }

    private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleCreate(String componentId, ArtifactDefinition artifactInfo, ArtifactOperationInfo operation, AuditingActionEnum auditingAction, User user, ComponentTypeEnum componentType,
                                                                                       org.openecomp.sdc.be.model.Component parent, String origMd5, String originData, String interfaceType, String operationName, boolean shouldLock, boolean inTransaction) {

        String artifactId = null;

        // step 11
        Either<byte[], ResponseFormat> payloadEither = validateInput(componentId, artifactInfo, operation, auditingAction, artifactId, user, componentType, parent, origMd5, originData, interfaceType, operationName);
        if (payloadEither.isRight()) {
            return Either.right(payloadEither.right().value());
        }
        byte[] decodedPayload = payloadEither.left().value();
        convertParentType(componentType);

        if (shouldLock) {
            Either<Boolean, ResponseFormat> lockComponent = lockComponent(parent, "Upload Artifact - lock ");
            if (lockComponent.isRight()) {
                handleAuditing(auditingAction, parent, componentId, user, null, null, null, lockComponent.right()
                                                                                                         .value(), componentType, null);
                return Either.right(lockComponent.right().value());
            }
        }
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> resultOp = null;

        try {
            resultOp = createArtifact(parent, componentId, artifactInfo, decodedPayload, user, componentType, auditingAction, interfaceType, operationName);
            return resultOp;
        }
        finally {
            if (shouldLock) {
                unlockComponent(resultOp, parent, inTransaction);
            }

        }

    }

    private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleLink(String componentId, ArtifactDefinition artifactInfo, AuditingActionEnum auditingAction, User user, ComponentTypeEnum componentType,
                                                                                     Component parent, boolean shouldLock, boolean inTransaction) {

        if (shouldLock) {
            Either<Boolean, ResponseFormat> lockComponent = lockComponent(parent, "Upload Artifact - lock ");
            if (lockComponent.isRight()) {
                handleAuditing(auditingAction, parent, componentId, user, null, null, null, lockComponent.right()
                                                                                                         .value(), componentType, null);
                return Either.right(lockComponent.right().value());
            }
        }
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> resultOp = null;

        try {
            resultOp = createAndLinkArtifact(parent, componentId, artifactInfo, user, componentType, auditingAction);
            return resultOp;
        }
        finally {
            if (shouldLock) {
                unlockComponent(resultOp, parent, inTransaction);
            }

        }

    }

    private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> lockComponentAndUpdateArtifact(String parentId, ArtifactDefinition artifactInfo, AuditingActionEnum auditingAction, String artifactId, User user,
                                                                                                         ComponentTypeEnum componentType, org.openecomp.sdc.be.model.Component parent, byte[] decodedPayload, String interfaceType, String operationName, boolean shouldLock, boolean inTransaction) {

        convertParentType(componentType);

        // lock resource
        if (shouldLock) {
            Either<Boolean, ResponseFormat> lockComponent = lockComponent(parent, UPDATE_ARTIFACT_LOCK);

            if (lockComponent.isRight()) {
                handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, lockComponent.right()
                                                                                                            .value(), componentType, null);
                return Either.right(lockComponent.right().value());
            }
        }

        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> resultOp = null;
        try {
            resultOp = updateArtifactFlow(parent, parentId, artifactId, artifactInfo, user, decodedPayload, componentType, auditingAction, interfaceType, operationName);
            return resultOp;

        }
        finally {
            if (shouldLock) {
                unlockComponent(resultOp, parent, inTransaction);
            }
        }
    }

    private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleUpdate(String parentId, ArtifactDefinition artifactInfo, ArtifactOperationInfo operation, AuditingActionEnum auditingAction, String artifactId, User user,
                                                                                       ComponentTypeEnum componentType, org.openecomp.sdc.be.model.Component parent, String origMd5, String originData, String interfaceType, String operationName, boolean shouldLock, boolean inTransaction) {

        Either<byte[], ResponseFormat> payloadEither = validateInput(parentId, artifactInfo, operation, auditingAction, artifactId, user, componentType, parent, origMd5, originData, interfaceType, operationName);

        if (payloadEither.isRight()) {
            return Either.right(payloadEither.right().value());
        }
        byte[] decodedPayload = payloadEither.left().value();

        return lockComponentAndUpdateArtifact(parentId, artifactInfo, auditingAction, artifactId, user, componentType, parent, decodedPayload, interfaceType, operationName, shouldLock, inTransaction);
    }

    private Either<byte[], ResponseFormat> validateInput(String componentId, ArtifactDefinition artifactInfo, ArtifactOperationInfo operation, AuditingActionEnum auditingAction, String artifactId, User user, ComponentTypeEnum componentType,
                                                         Component parent, String origMd5, String originData, String interfaceType, String operationName) {
        // Md5 validations
        Either<Boolean, ResponseFormat> validateMd5 = validateMd5(origMd5, originData, artifactInfo.getPayloadData(), operation);
        if (validateMd5.isRight()) {
            ResponseFormat responseFormat = validateMd5.right().value();
            handleAuditing(auditingAction, parent, componentId, user, null, null, artifactId, responseFormat, componentType, null);
            return Either.right(responseFormat);
        }

        // step 11
        Either<ArtifactDefinition, ResponseFormat> validateResult = validateInput(componentId, artifactInfo, operation, artifactId, user, interfaceType, operationName, componentType, parent);
        if (validateResult.isRight()) {
            ResponseFormat responseFormat = validateResult.right().value();
            handleAuditing(auditingAction, parent, componentId, user, null, null, artifactId, responseFormat, componentType, null);
            return Either.right(validateResult.right().value());
        }

        Either<byte[], ResponseFormat> payloadEither = handlePayload(artifactInfo, isArtifactMetadataUpdate(auditingAction));
        if (payloadEither.isRight()) {
            ResponseFormat responseFormat = payloadEither.right().value();
            handleAuditing(auditingAction, parent, componentId, user, null, null, artifactId, responseFormat, componentType, null);
            log.debug("Error during handle payload");
            return Either.right(responseFormat);
        }

        // validate heat parameters. this part must be after the parameters are
        // extracted in "handlePayload"
        Either<ArtifactDefinition, ResponseFormat> validateAndConvertHeatParamers = validateAndConvertHeatParamers(artifactInfo, artifactInfo
                .getArtifactType());
        if (validateAndConvertHeatParamers.isRight()) {
            ResponseFormat responseFormat = validateAndConvertHeatParamers.right().value();
            handleAuditing(auditingAction, parent, componentId, user, artifactInfo, null, artifactId, responseFormat, componentType, null);
            log.debug("Error during handle payload");
            return Either.right(responseFormat);
        }
        return payloadEither;
    }

    public void handleAuditing(AuditingActionEnum auditingActionEnum, Component component, String componentId, User user, ArtifactDefinition artifactDefinition, String prevArtifactUuid, String currentArtifactUuid, ResponseFormat responseFormat,
                               ComponentTypeEnum componentTypeEnum, String resourceInstanceName) {

        if (componentsUtils.isExternalApiEvent(auditingActionEnum)) {
            return;
        }

        if (user == null) {
            user = new User();
            user.setUserId("UNKNOWN");
        }
        handleInternalAuditEvent(auditingActionEnum, component, componentId, user, artifactDefinition, prevArtifactUuid, currentArtifactUuid, responseFormat, componentTypeEnum, resourceInstanceName);
    }

    private void handleInternalAuditEvent(AuditingActionEnum auditingActionEnum, Component component, String componentId, User user, ArtifactDefinition artifactDefinition, String prevArtifactUuid, String currentArtifactUuid, ResponseFormat responseFormat, ComponentTypeEnum componentTypeEnum, String resourceInstanceName) {
        switch (componentTypeEnum) {
            case RESOURCE:
                Resource resource = (Resource) component;
                if (resource == null) {
                    // In that case, component ID should be instead of name
                    resource = new Resource();
                    resource.setName(componentId);
                }
                componentsUtils.auditResource(responseFormat, user, resource, resource.getName(), auditingActionEnum,
                        ResourceVersionInfo.newBuilder()
                                .artifactUuid(prevArtifactUuid)
                                .build(), currentArtifactUuid, artifactDefinition);
                break;

            case SERVICE:
                Service service = (Service) component;
                if (service == null) {
                    // In that case, component ID should be instead of name
                    service = new Service();
                    service.setName(componentId);
                }
                componentsUtils.auditComponent(responseFormat, user, service, auditingActionEnum, new ResourceCommonInfo(ComponentTypeEnum.SERVICE.getValue()),
                        ResourceVersionInfo.newBuilder()
                                .artifactUuid(prevArtifactUuid)
                                .build(),
                        ResourceVersionInfo.newBuilder()
                                .artifactUuid(currentArtifactUuid)
                                .build(),
                        null, artifactDefinition, null);
                break;

            case RESOURCE_INSTANCE:
                if (resourceInstanceName == null) {
                    resourceInstanceName = getResourceInstanceNameFromComponent(component, componentId);
                }
                componentsUtils.auditComponent(responseFormat, user, component, auditingActionEnum,
                        new ResourceCommonInfo(resourceInstanceName, ComponentTypeEnum.RESOURCE_INSTANCE.getValue()),
                        ResourceVersionInfo.newBuilder()
                                .artifactUuid(prevArtifactUuid)
                                .build(),
                        ResourceVersionInfo.newBuilder()
                                .artifactUuid(currentArtifactUuid)
                                .build(),
                        null, artifactDefinition, null);
                break;
            default:
                break;
        }
    }

    private String getResourceInstanceNameFromComponent(Component component, String componentId) {
        ComponentInstance resourceInstance = component.getComponentInstances()
                                                      .stream()
                                                      .filter(p -> p.getUniqueId().equals(componentId))
                                                      .findFirst()
                                                      .orElse(null);
        String resourceInstanceName = null;
        if (resourceInstance != null) {
            resourceInstanceName = resourceInstance.getName();
        }
        return resourceInstanceName;
    }

    private String buildAuditingArtifactData(ArtifactDefinition artifactDefinition) {
        StringBuilder sb = new StringBuilder();
        if (artifactDefinition != null) {
            sb.append(artifactDefinition.getArtifactGroupType().getType())
              .append(",")
              .append("'")
              .append(artifactDefinition.getArtifactLabel())
              .append("'")
              .append(",")
              .append(artifactDefinition.getArtifactType())
              .append(",")
              .append(artifactDefinition.getArtifactName())
              .append(",")
              .append(artifactDefinition.getTimeout())
              .append(",")
              .append(artifactDefinition.getEsId());

            sb.append(",");
            if (artifactDefinition.getArtifactVersion() != null) {

                sb.append(artifactDefinition.getArtifactVersion());
            }
            else {
                sb.append(" ");
            }
            sb.append(",");
            if (artifactDefinition.getArtifactUUID() != null) {
                sb.append(artifactDefinition.getArtifactUUID());
            }
            else {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private Either<Boolean, ResponseFormat> validateMd5(String origMd5, String originData, byte[] payload, ArtifactOperationInfo operation) {

        if (origMd5 != null) {
            String encodeBase64Str = GeneralUtility.calculateMD5Base64EncodedByString(originData);
            if (!encodeBase64Str.equals(origMd5)) {
                log.debug("The calculated md5 is different then the received one");
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_INVALID_MD5));
            }
        }
        else {
            if (ArtifactOperationEnum.isCreateOrLink(operation.getArtifactOperationEnum()) && payload != null && payload.length != 0) {
                log.debug("Missing md5 header during artifact create");
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_INVALID_MD5));
            }
            // Update metadata
            if (payload != null && payload.length != 0) {
                log.debug("Cannot have payload while md5 header is missing");
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
            }
        }
        return Either.left(true);
    }

    private Either<ArtifactDefinition, ResponseFormat> validateInput(String componentId, ArtifactDefinition artifactInfo, ArtifactOperationInfo operation, String artifactId, User user, String interfaceName, String operationName,
                                                                     ComponentTypeEnum componentType, Component parentComponent) {

        Either<ArtifactDefinition, ResponseFormat> artifactById = findArtifactOnParentComponent(parentComponent, componentType, componentId, operation, artifactId);
        if (artifactById.isRight()) {
            return Either.right(artifactById.right().value());
        }
        ArtifactDefinition currentArtifactInfo = artifactById.left().value();

        ignoreUnupdateableFieldsInUpdate(operation, artifactInfo, currentArtifactInfo);
        Either<Boolean, ResponseFormat> validateInformationalArtifactRes = validateInformationalArtifact(artifactInfo, parentComponent);
        if (validateInformationalArtifactRes.isRight()) {
            return Either.right(validateInformationalArtifactRes.right().value());
        }
        Either<Boolean, ResponseFormat> validateAndSetArtifactname = validateAndSetArtifactname(artifactInfo);
        if (validateAndSetArtifactname.isRight()) {
            return Either.right(validateAndSetArtifactname.right().value());
        }
        if (operationName != null && interfaceName != null) {
            operationName = operationName.toLowerCase();
            interfaceName = interfaceName.toLowerCase();
        }
        Either<ActionStatus, ResponseFormat> logicalNameStatus = handleArtifactLabel(componentId, parentComponent, operation, artifactInfo, operationName, componentType);
        if (logicalNameStatus.isRight()) {
            return Either.right(logicalNameStatus.right().value());
        }
        // This is a patch to block possibility of updating service api fields
        // through other artifacts flow

        ArtifactGroupTypeEnum artifactGroupType = operationName != null ? ArtifactGroupTypeEnum.LIFE_CYCLE : ArtifactGroupTypeEnum.INFORMATIONAL;
        if (!ArtifactOperationEnum.isCreateOrLink(operation.getArtifactOperationEnum())) {
            checkAndSetUnUpdatableFields(user, artifactInfo, currentArtifactInfo, artifactGroupType);
        }
        else {
            checkCreateFields(user, artifactInfo, artifactGroupType);
        }

        composeArtifactId(componentId, artifactId, artifactInfo, interfaceName, operationName);
        if (currentArtifactInfo != null) {
            artifactInfo.setMandatory(currentArtifactInfo.getMandatory());
        }

        // artifactGroupType is not allowed to be updated
        if (!ArtifactOperationEnum.isCreateOrLink(operation.getArtifactOperationEnum())) {
            Either<ArtifactDefinition, ResponseFormat> validateGroupType = validateOrSetArtifactGroupType(artifactInfo, currentArtifactInfo);
            if (validateGroupType.isRight()) {
                return Either.right(validateGroupType.right().value());
            }
        }
        NodeTypeEnum parentType = convertParentType(componentType);

        boolean isCreate = ArtifactOperationEnum.isCreateOrLink(operation.getArtifactOperationEnum());

        if (isDeploymentArtifact(artifactInfo)) {
            Either<Boolean, ResponseFormat> deploymentValidationResult = validateDeploymentArtifact(parentComponent, componentId, isCreate, artifactInfo, currentArtifactInfo, parentType);
            if (deploymentValidationResult.isRight()) {
                return Either.right(deploymentValidationResult.right().value());
            }
        }
        else {
            artifactInfo.setTimeout(NodeTemplateOperation.NON_HEAT_TIMEOUT);
        }

        Either<Boolean, ResponseFormat> descriptionResult = validateAndCleanDescription(artifactInfo);
        if (descriptionResult.isRight()) {
            return Either.right(descriptionResult.right().value());
        }

        if (currentArtifactInfo != null && currentArtifactInfo.getArtifactGroupType()
                                                              .equals(ArtifactGroupTypeEnum.SERVICE_API)) {
            Either<ActionStatus, ResponseFormat> validateServiceApiType = validateArtifactType(user.getUserId(), artifactInfo, parentType);
            if (validateServiceApiType.isRight()) {
                return Either.right(validateServiceApiType.right().value());
            }
            // Change of type is not allowed and should be ignored

            artifactInfo.setArtifactType(ARTIFACT_TYPE_OTHER);

            Either<Boolean, ResponseFormat> validateUrl = validateAndServiceApiUrl(artifactInfo);
            if (validateUrl.isRight()) {
                return Either.right(validateUrl.right().value());
            }

            Either<Boolean, ResponseFormat> validateUpdate = validateFirstUpdateHasPayload(artifactInfo, currentArtifactInfo);
            if (validateUpdate.isRight()) {
                log.debug("serviceApi first update cnnot be without payload.");
                return Either.right(validateUpdate.right().value());
            }
        }
        else {
            Either<ActionStatus, ResponseFormat> validateArtifactType = validateArtifactType(user.getUserId(), artifactInfo, parentType);
            if (validateArtifactType.isRight()) {
                return Either.right(validateArtifactType.right().value());
            }
            if (artifactInfo.getApiUrl() != null) {
                artifactInfo.setApiUrl(null);
                log.error("Artifact URL cannot be set through this API - ignoring");
            }

            if (artifactInfo.getServiceApi() != null && artifactInfo.getServiceApi()) {
                artifactInfo.setServiceApi(false);
                log.error("Artifact service API flag cannot be changed - ignoring");
            }
        }

        return Either.left(artifactInfo);
    }

    private void ignoreUnupdateableFieldsInUpdate(ArtifactOperationInfo operation, ArtifactDefinition artifactInfo, ArtifactDefinition currentArtifactInfo) {
        if (operation.getArtifactOperationEnum().equals(ArtifactOperationEnum.UPDATE)) {
            artifactInfo.setArtifactType(currentArtifactInfo.getArtifactType());
            artifactInfo.setArtifactGroupType(currentArtifactInfo.getArtifactGroupType());
            artifactInfo.setArtifactLabel(currentArtifactInfo.getArtifactLabel());
        }
    }

    private Either<ArtifactDefinition, ResponseFormat> findArtifactOnParentComponent(Component parentComponent, ComponentTypeEnum componentType, String parentId, ArtifactOperationInfo operation, String artifactId) {

        Either<ArtifactDefinition, ResponseFormat> result = null;
        ArtifactDefinition foundArtifact = null;
        if (StringUtils.isNotEmpty(artifactId)) {
            foundArtifact = findArtifact(parentComponent, componentType, parentId, artifactId);
        }
        if (foundArtifact != null && ArtifactOperationEnum.isCreateOrLink(operation.getArtifactOperationEnum())) {
            log.debug("Artifact {} already exist", artifactId);
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_EXIST, foundArtifact.getArtifactLabel()));
        }
        if (foundArtifact == null && !ArtifactOperationEnum.isCreateOrLink(operation.getArtifactOperationEnum())) {
            log.debug("The artifact {} was not found on parent {}. ", artifactId, parentId);
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, ""));
        }
        if (result == null) {
            result = Either.left(foundArtifact);
        }
        return result;
    }

    private ArtifactDefinition findArtifact(Component parentComponent, ComponentTypeEnum componentType, String parentId, String artifactId) {
        ArtifactDefinition foundArtifact;
        if (parentComponent.getUniqueId().equals(parentId)) {
            foundArtifact = artifactsResolver.findArtifactOnComponent(parentComponent, componentType, artifactId);
        }
        else {
            ComponentInstance instance = findComponentInstance(parentId, parentComponent);
            foundArtifact = artifactsResolver.findArtifactOnComponentInstance(instance, artifactId);
        }
        return foundArtifact;
    }

    private Either<Boolean, ResponseFormat> validateInformationalArtifact(ArtifactDefinition artifactInfo, Component parentComponent) {
        ComponentTypeEnum parentComponentType = parentComponent.getComponentType();
        ArtifactGroupTypeEnum groupType = artifactInfo.getArtifactGroupType();
        Either<Boolean, ResponseFormat> validationResult = Either.left(true);
        ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifactInfo.getArtifactType());
        if (artifactType == null) {
            validationResult = Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED, artifactInfo
                    .getArtifactType()));
        }
        else if (parentComponentType == ComponentTypeEnum.RESOURCE && groupType == ArtifactGroupTypeEnum.INFORMATIONAL) {
            String artifactTypeName = artifactType.getType();
            ResourceTypeEnum parentResourceType = ((Resource) parentComponent).getResourceType();
            Map<String, ArtifactTypeConfig> resourceInformationalArtifacts = ConfigurationManager.getConfigurationManager()
                                                                                                 .getConfiguration()
                                                                                                 .getResourceInformationalArtifacts();
            Set<String> validArtifactTypes = resourceInformationalArtifacts.keySet();
            if (!validArtifactTypes.contains(artifactTypeName)) {
                validationResult = Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED, artifactTypeName));
            }
            else {
                List<String> validResourceType = resourceInformationalArtifacts.get(artifactTypeName)
                                                                               .getValidForResourceTypes();
                if (!validResourceType.contains(parentResourceType.name())) {
                    validationResult = Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED, artifactTypeName));
                }
            }
        }
        return validationResult;
    }

    private NodeTypeEnum convertParentType(ComponentTypeEnum componentType) {
        if (componentType.equals(ComponentTypeEnum.RESOURCE)) {
            return NodeTypeEnum.Resource;
        }
        else if (componentType.equals(ComponentTypeEnum.RESOURCE_INSTANCE)) {
            return NodeTypeEnum.ResourceInstance;
        }
        else {
            return NodeTypeEnum.Service;
        }
    }

    public Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleDelete(String parentId, String artifactId, User user, AuditingActionEnum auditingAction, ComponentTypeEnum componentType, Component parent,
                                                                                      boolean shouldLock, boolean inTransaction) {

        NodeTypeEnum parentType = convertParentType(componentType);
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> resultOp = null;
        Either<ImmutablePair<ArtifactDefinition, ComponentInstance>, ActionStatus> getArtifactRes = null;
        ArtifactDefinition foundArtifact = null;
        ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getContainerRes = null;
        org.openecomp.sdc.be.model.Component fetchedContainerComponent = null;
        boolean isDuplicated = false;
        String esId = null;
        Either<Boolean, StorageOperationStatus> needCloneRes = null;
        try {
            if (shouldLock) {
                Either<Boolean, ResponseFormat> lockComponent = lockComponent(parent, "Delete Artifact - lock resource: ");
                if (lockComponent.isRight()) {
                    handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, lockComponent.right()
                                                                                                                .value(), componentType, null);
                    resultOp = Either.right(lockComponent.right().value());
                }
            }
            if (resultOp == null) {
                log.debug("Going to fetch the container component {}. ", parent.getUniqueId());
                getContainerRes = toscaOperationFacade.getToscaElement(parent.getUniqueId());
                if (getContainerRes.isRight()) {
                    log.debug("Failed to fetch the container component {}. ", parentId);
                    responseFormat = componentsUtils.getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(getContainerRes
                            .right()
                            .value()), artifactId);
                    handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
                    resultOp = Either.right(responseFormat);
                }
            }
            if (resultOp == null) {
                fetchedContainerComponent = getContainerRes.left().value();
                log.debug("Going to find the artifact {} on the component {}", artifactId, fetchedContainerComponent.getUniqueId());
                getArtifactRes = findArtifact(artifactId, fetchedContainerComponent, parentId, componentType);
                if (getArtifactRes.isRight()) {
                    log.debug("Failed to find the artifact {} belonging to {} on the component {}", artifactId, parentId, fetchedContainerComponent
                            .getUniqueId());
                    responseFormat = componentsUtils.getResponseFormatByArtifactId(getArtifactRes.right()
                                                                                                 .value(), artifactId);
                    handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
                    resultOp = Either.right(responseFormat);
                }
                else {
                    foundArtifact = getArtifactRes.left().value().getLeft();
                    esId = foundArtifact.getEsId();
                }
            }
            if (resultOp == null && StringUtils.isNotEmpty(esId)) {
                needCloneRes = artifactToscaOperation.isCloneNeeded(parent.getUniqueId(), foundArtifact, convertParentType(parent
                        .getComponentType()));
                if (needCloneRes.isRight()) {
                    log.debug(FAILED_UPDATE_ARTIFACT, artifactId, parentId);
                    responseFormat = componentsUtils.getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(needCloneRes
                            .right()
                            .value()), foundArtifact.getArtifactDisplayName());
                    handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
                    resultOp = Either.right(responseFormat);
                }
            }
            boolean isNeedToDeleteArtifactFromDB = true;
            if (resultOp == null) {

                if (componentType == ComponentTypeEnum.RESOURCE_INSTANCE) {
                    String instanceId = parentId;
                    Either<Boolean, ActionStatus> isOnlyResourceInstanceArtifact = isArtifactOnlyResourceInstanceArtifact(foundArtifact, fetchedContainerComponent, instanceId);

                    if (isOnlyResourceInstanceArtifact.isRight()) {
                        log.debug(FAILED_UPDATE_ARTIFACT, artifactId, parentId);
                        responseFormat = componentsUtils.getResponseFormatByArtifactId(isOnlyResourceInstanceArtifact.right()
                                                                                                                     .value(), foundArtifact
                                .getArtifactDisplayName());
                        handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
                        return Either.right(responseFormat);
                    }
                    isNeedToDeleteArtifactFromDB = isOnlyResourceInstanceArtifact.left().value();
                }

                Either<ArtifactDataDefinition, StorageOperationStatus> updatedArtifactRes = deleteOrUpdateArtifactOnGraph(parent, parentId, artifactId, parentType, foundArtifact, needCloneRes
                        .left()
                        .value());
                if (updatedArtifactRes.isRight()) {
                    log.debug(FAILED_UPDATE_ARTIFACT, artifactId, parentId);
                    responseFormat = componentsUtils.getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(updatedArtifactRes
                            .right()
                            .value()), foundArtifact.getArtifactDisplayName());
                    handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
                    resultOp = Either.right(responseFormat);
                }
                else {
                    isDuplicated = updatedArtifactRes.left().value().getDuplicated();
                }
            }

            if (resultOp == null && (!needCloneRes.left().value() && !isDuplicated) && isNeedToDeleteArtifactFromDB) {
                log.debug("Going to delete the artifact {} from the database. ", artifactId);
                CassandraOperationStatus cassandraStatus = artifactCassandraDao.deleteArtifact(esId);
                if (cassandraStatus != CassandraOperationStatus.OK) {
                    log.debug("Failed to delete the artifact {} from the database. ", artifactId);
                    responseFormat = componentsUtils.getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(componentsUtils.convertToStorageOperationStatus(cassandraStatus)), foundArtifact
                            .getArtifactDisplayName());
                    handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
                    resultOp = Either.right(responseFormat);
                }
            }
            if (resultOp == null && componentType == ComponentTypeEnum.RESOURCE_INSTANCE) {

                List<GroupInstance> updatedGroupInstances = getUpdatedGroupInstances(artifactId, foundArtifact, getArtifactRes
                        .left()
                        .value()
                        .getRight()
                        .getGroupInstances());
                if (CollectionUtils.isNotEmpty(updatedGroupInstances)) {
                    Either<List<GroupInstance>, StorageOperationStatus> status = toscaOperationFacade.updateGroupInstancesOnComponent(fetchedContainerComponent, parentId, updatedGroupInstances);
                    if (status.isRight()) {
                        log.debug(FAILED_UPDATE_GROUPS, fetchedContainerComponent.getUniqueId());
                        responseFormat = componentsUtils.getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(status
                                .right()
                                .value()), foundArtifact.getArtifactDisplayName());
                        handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
                        resultOp = Either.right(responseFormat);
                    }
                }
            }
            if (resultOp == null && componentType == ComponentTypeEnum.RESOURCE_INSTANCE) {
                StorageOperationStatus status = generateCustomizationUUIDOnInstance(parent.getUniqueId(), parentId, componentType);
                if (status != StorageOperationStatus.OK) {
                    log.debug("Failed to generate new customization UUID for the component instance {}. ", parentId);
                    responseFormat = componentsUtils.getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(status), foundArtifact
                            .getArtifactDisplayName());
                    handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
                    resultOp = Either.right(responseFormat);
                }
            }
            if (resultOp == null && componentType != ComponentTypeEnum.RESOURCE_INSTANCE) {
                List<GroupDataDefinition> updatedGroups = getUpdatedGroups(artifactId, foundArtifact, fetchedContainerComponent
                        .getGroups());
                if (CollectionUtils.isNotEmpty(updatedGroups)) {
                    Either<List<GroupDefinition>, StorageOperationStatus> status = toscaOperationFacade.updateGroupsOnComponent(fetchedContainerComponent, updatedGroups);
                    if (status.isRight()) {
                        log.debug(FAILED_UPDATE_GROUPS, fetchedContainerComponent.getUniqueId());
                        responseFormat = componentsUtils.getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(status
                                .right()
                                .value()), foundArtifact.getArtifactDisplayName());
                        handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
                        resultOp = Either.right(responseFormat);
                    }
                }
            }
            if (resultOp == null) {
                resultOp = Either.left(Either.left(foundArtifact));
                handleAuditing(auditingAction, parent, parentId, user, foundArtifact, null, artifactId, responseFormat, componentType, null);
            }
            return resultOp;
        }
        finally {
            if (shouldLock) {
                unlockComponent(resultOp, parent, inTransaction);
            }
        }
    }

    private Either<Boolean, ActionStatus> isArtifactOnlyResourceInstanceArtifact(ArtifactDefinition foundArtifact, Component parent, String instanceId) {
        Either<Boolean, ActionStatus> result = Either.left(true);
        ComponentInstance foundInstance = null;
        Optional<ComponentInstance> componentInstanceOpt = parent.getComponentInstances()
                                                                 .stream()
                                                                 .filter(i -> i.getUniqueId().equals(instanceId))
                                                                 .findFirst();
        if (!componentInstanceOpt.isPresent()) {
            result = Either.right(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER);
        }
        else {
            foundInstance = componentInstanceOpt.get();
            String componentUid = foundInstance.getComponentUid();
            Either<Component, StorageOperationStatus> getContainerRes = toscaOperationFacade.getToscaElement(componentUid);
            if (getContainerRes.isRight()) {
                log.debug("Failed to fetch the container component {}. ", componentUid);
                return Either.right(componentsUtils.convertFromStorageResponse(getContainerRes.right().value()));
            }
            Component origComponent = getContainerRes.left().value();
            Map<String, ArtifactDefinition> deploymentArtifacts = origComponent.getDeploymentArtifacts();
            if (deploymentArtifacts != null && !deploymentArtifacts.isEmpty()) {
                Optional<String> op = deploymentArtifacts.keySet()
                                                         .stream()
                                                         .filter(a -> a.equals(foundArtifact.getArtifactLabel()))
                                                         .findAny();
                if (op.isPresent()) {
                    return Either.left(false);
                }
            }
            Map<String, ArtifactDefinition> artifacts = origComponent.getArtifacts();
            if (artifacts != null && !artifacts.isEmpty()) {
                Optional<String> op = artifacts.keySet()
                                               .stream()
                                               .filter(a -> a.equals(foundArtifact.getArtifactLabel()))
                                               .findAny();
                if (op.isPresent()) {
                    return Either.left(false);
                }
            }

        }
        return result;
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
                if (CollectionUtils.isNotEmpty(group.getArtifactsUuid()) && group.getArtifactsUuid()
                                                                                 .contains(foundArtifact.getArtifactUUID())) {
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
        List<GroupInstance> updatedGroupInstances = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(groupInstances)) {
            boolean isUpdated = false;
            for (GroupInstance groupInstance : groupInstances) {
                isUpdated = false;
                if (CollectionUtils.isNotEmpty(groupInstance.getGroupInstanceArtifacts()) && groupInstance.getGroupInstanceArtifacts()
                                                                                                          .contains(artifactId)) {
                    groupInstance.getGroupInstanceArtifacts().remove(artifactId);
                    isUpdated = true;
                }
                if (CollectionUtils.isNotEmpty(groupInstance.getGroupInstanceArtifactsUuid()) && groupInstance.getGroupInstanceArtifactsUuid()
                                                                                                              .contains(foundArtifact
                                                                                                                      .getArtifactUUID())) {
                    groupInstance.getGroupInstanceArtifactsUuid().remove(foundArtifact.getArtifactUUID());
                    isUpdated = true;
                }
                if (isUpdated) {
                    updatedGroupInstances.add(groupInstance);
                }
            }
        }
        return updatedGroupInstances;
    }

    private Either<ArtifactDataDefinition, StorageOperationStatus> deleteOrUpdateArtifactOnGraph(Component component, String parentId, String artifactId, NodeTypeEnum parentType, ArtifactDefinition foundArtifact, Boolean cloneIsNeeded) {

        Either<ArtifactDataDefinition, StorageOperationStatus> result;
        boolean isMandatory = foundArtifact.getMandatory() || foundArtifact.getServiceApi();
        String componentId = component.getUniqueId();
        String instanceId = componentId.equals(parentId) ? null : parentId;
        if (isMandatory) {
            log.debug("Going to update mandatory artifact {} from the component {}", artifactId, parentId);
            resetMandatoryArtifactFields(foundArtifact);
            result = artifactToscaOperation.updateArtifactOnGraph(componentId, foundArtifact, parentType, artifactId, instanceId, true, true);
        }
        else if (cloneIsNeeded) {
            log.debug("Going to clone artifacts and to delete the artifact {} from the component {}", artifactId, parentId);
            result = artifactToscaOperation.deleteArtifactWithCloningOnGraph(componentId, foundArtifact, parentType, instanceId, false);
        }
        else {
            log.debug("Going to delete the artifact {} from the component {}", artifactId, parentId);
            result = artifactToscaOperation.removeArtifactOnGraph(foundArtifact, componentId, instanceId, parentType, false);
        }
        return result;
    }

    private Either<ImmutablePair<ArtifactDefinition, ComponentInstance>, ActionStatus> findArtifact(String artifactId, Component fetchedContainerComponent, String parentId, ComponentTypeEnum componentType) {

        Either<ImmutablePair<ArtifactDefinition, ComponentInstance>, ActionStatus> result = null;
        Map<String, ArtifactDefinition> artifacts = new HashMap<>();
        ComponentInstance foundInstance = null;
        if (componentType == ComponentTypeEnum.RESOURCE_INSTANCE && StringUtils.isNotEmpty(parentId)) {
            Optional<ComponentInstance> componentInstanceOpt = fetchedContainerComponent.getComponentInstances()
                                                                                        .stream()
                                                                                        .filter(i -> i.getUniqueId()
                                                                                                      .equals(parentId))
                                                                                        .findFirst();
            if (!componentInstanceOpt.isPresent()) {
                result = Either.right(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER);
            }
            else {
                foundInstance = componentInstanceOpt.get();
                fetchArtifactsFromInstance(artifactId, artifacts, foundInstance);
            }
        }
        else {
            fetchArtifactsFromComponent(artifactId, fetchedContainerComponent, artifacts);
        }
        if (result == null) {
            if (artifacts.containsKey(artifactId)) {
                result = Either.left(new ImmutablePair<>(artifacts.get(artifactId), foundInstance));
            }
            else {
                result = Either.right(ActionStatus.ARTIFACT_NOT_FOUND);
            }
        }
        return result;
    }

    private void fetchArtifactsFromComponent(String artifactId, Component component, Map<String, ArtifactDefinition> artifacts) {
        Map<String, ArtifactDefinition> currArtifacts;
        if (!artifacts.containsKey(artifactId) && MapUtils.isNotEmpty(component.getDeploymentArtifacts())) {
            currArtifacts = component.getDeploymentArtifacts()
                                     .values()
                                     .stream()
                                     .collect(Collectors.toMap(i -> i.getUniqueId(), i -> i));
            if (MapUtils.isNotEmpty(currArtifacts)) {
                artifacts.putAll(currArtifacts);
            }
        }
        if (!artifacts.containsKey(artifactId) && MapUtils.isNotEmpty(component.getArtifacts())) {
            currArtifacts = component.getArtifacts()
                                     .values()
                                     .stream()
                                     .collect(Collectors.toMap(i -> i.getUniqueId(), i -> i));
            if (MapUtils.isNotEmpty(currArtifacts)) {
                artifacts.putAll(currArtifacts);
            }
        }
        if (!artifacts.containsKey(artifactId) && MapUtils.isNotEmpty(component.getArtifacts())) {
            currArtifacts = component.getToscaArtifacts()
                                     .values()
                                     .stream()
                                     .collect(Collectors.toMap(i -> i.getUniqueId(), i -> i));
            if (MapUtils.isNotEmpty(currArtifacts)) {
                artifacts.putAll(currArtifacts);
            }
        }
    }

    private void fetchArtifactsFromInstance(String artifactId, Map<String, ArtifactDefinition> artifacts, ComponentInstance instance) {
        Map<String, ArtifactDefinition> currArtifacts;
        if (MapUtils.isNotEmpty(instance.getDeploymentArtifacts())) {
            currArtifacts = instance.getDeploymentArtifacts()
                                    .values()
                                    .stream()
                                    .collect(Collectors.toMap(i -> i.getUniqueId(), i -> i));
            if (MapUtils.isNotEmpty(currArtifacts)) {
                artifacts.putAll(currArtifacts);
            }
        }
        if (!artifacts.containsKey(artifactId) && MapUtils.isNotEmpty(instance.getArtifacts())) {
            currArtifacts = instance.getArtifacts()
                                    .values()
                                    .stream()
                                    .collect(Collectors.toMap(i -> i.getUniqueId(), i -> i));
            if (MapUtils.isNotEmpty(currArtifacts)) {
                artifacts.putAll(currArtifacts);
            }
        }
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

    private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleDownload(String componentId, String artifactId, User user, AuditingActionEnum auditingAction, ComponentTypeEnum componentType,
                                                                                         Component parent) {
        Either<ArtifactDefinition, StorageOperationStatus> artifactById = artifactToscaOperation.getArtifactById(componentId, artifactId, componentType, parent
                .getUniqueId());
        if (artifactById.isRight()) {
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(artifactById.right().value());
            log.debug("Error when getting artifact info by id{}, error: {}", artifactId, actionStatus);
            ResponseFormat responseFormat = componentsUtils.getResponseFormatByArtifactId(actionStatus, "");
            handleAuditing(auditingAction, parent, componentId, user, null, null, artifactId, responseFormat, componentType, null);
            return Either.right(responseFormat);
        }
        ArtifactDefinition artifactDefinition = artifactById.left().value();
        if (artifactDefinition == null) {
            log.debug("Empty artifact definition returned from DB by artifact id {}", artifactId);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, "");
            handleAuditing(auditingAction, parent, componentId, user, null, null, artifactId, responseFormat, componentType, null);
            return Either.right(responseFormat);
        }

        Either<ArtifactDefinition, Operation> insideEither = Either.left(artifactDefinition);
        ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
        handleAuditing(auditingAction, parent, componentId, user, artifactDefinition, null, artifactId, responseFormat, componentType, null);
        return Either.left(insideEither);
    }

    private Either<ActionStatus, ResponseFormat> handleArtifactLabel(String componentId, Component parentComponent, ArtifactOperationInfo operation, ArtifactDefinition artifactInfo, String operationName,
                                                                     ComponentTypeEnum componentType) {

        String artifactLabel = artifactInfo.getArtifactLabel();
        if (operationName == null && (artifactInfo.getArtifactLabel() == null || artifactInfo.getArtifactLabel()
                                                                                             .isEmpty())) {
            BeEcompErrorManager.getInstance()
                               .logBeMissingArtifactInformationError("Artifact Update / Upload", "artifactLabel");
            log.debug("missing artifact logical name for component {}", componentId);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, ARTIFACT_LABEL));
        }
        if (ArtifactOperationEnum.isCreateOrLink(operation.getArtifactOperationEnum()) && !artifactInfo.getMandatory()) {

            if (operationName != null) {
                if (artifactInfo.getArtifactLabel() != null && !operationName.equals(artifactInfo.getArtifactLabel())) {
                    log.debug("artifact label cannot be set {}", artifactLabel);
                    return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_LOGICAL_NAME_CANNOT_BE_CHANGED));
                }
                else {
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
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
            }
            artifactLabel = ValidationUtils.normalizeArtifactLabel(artifactLabel);

            if (artifactLabel.isEmpty()) {
                log.debug("missing normalized artifact logical name for component {}", componentId);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, ARTIFACT_LABEL));
            }

            if (!ValidationUtils.validateArtifactLabelLength(artifactLabel)) {
                log.debug("Invalid lenght form Artifact label : {}", artifactLabel);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.EXCEEDS_LIMIT, ARTIFACT_LABEL, String
                        .valueOf(ValidationUtils.ARTIFACT_LABEL_LENGTH)));
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
        if (componentType.equals(ComponentTypeEnum.RESOURCE_INSTANCE)) {
            artifacts = artifactToscaOperation.getAllInstanceArtifacts(parentComponent.getUniqueId(), componentId);
        }
        else {
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
        if (componentType.equals(ComponentTypeEnum.RESOURCE)) {
            Either<Map<String, InterfaceDefinition>, StorageOperationStatus> allInterfacesOfResource = interfaceLifecycleOperation
                    .getAllInterfacesOfResource(componentId, true, true);
            if (allInterfacesOfResource.isLeft()) {
                for (InterfaceDefinition interace : allInterfacesOfResource.left().value().values()) {
                    for (Operation operation : interace.getOperationsMap().values()) {
                        if (operation.getImplementation() != null && operation.getImplementation()
                                                                              .getArtifactLabel()
                                                                              .equals(artifactLabel)) {
                            isUnique = false;
                            break;
                        }
                    }
                }
            }
        }
        return isUnique;
    }

    boolean validateArtifactNameUniqueness(String componentId, Component parentComponent, ArtifactDefinition artifactInfo,
                                           ComponentTypeEnum componentType) {
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifacts = getArtifacts(componentType,
                parentComponent, componentId, artifactInfo.getArtifactGroupType());
        String artifactName = artifactInfo.getArtifactName();
        if (artifacts.isLeft() && Objects.nonNull(artifacts.left().value())){
            if (artifacts.left().value().values().stream()
                    .anyMatch(ad -> artifactName.equals(ad.getArtifactName())
                            //check whether it is the same artifact we hold (by label)
                            && !artifactInfo.getArtifactLabel().equals(ad.getArtifactLabel()))){
                return false;
            }
        }
        if (ComponentTypeEnum.RESOURCE.equals(componentType)) {
            return isUniqueArtifactNameInResourceInterfaces(componentId, artifactName, artifactInfo.getArtifactLabel());
        }
        return true;
    }

    private boolean isUniqueArtifactNameInResourceInterfaces(String componentId, String artifactName, String artifactLabel) {
        Either<Map<String, InterfaceDefinition>, StorageOperationStatus> allInterfacesOfResource = interfaceLifecycleOperation
                .getAllInterfacesOfResource(componentId, true, true);

        if (allInterfacesOfResource.isLeft() && Objects.nonNull(allInterfacesOfResource)){
            return !allInterfacesOfResource.left().value()
                    .values()
                    .stream().map(InterfaceDefinition :: getOperationsMap)
                    .flatMap(map -> map.values().stream())
                    .map(OperationDataDefinition::getImplementation)
                    .filter(Objects::nonNull)
                    .anyMatch(add -> artifactName.equals(add.getArtifactName())
                            && !artifactLabel.equals(add.getArtifactLabel()));
        }
        return true;
    }

    private boolean isUniqueLabelInResourceInterfaces(String componentId, String artifactLabel) {
        Either<Map<String, InterfaceDefinition>, StorageOperationStatus> allInterfacesOfResource = interfaceLifecycleOperation
                .getAllInterfacesOfResource(componentId, true, true);

        if (allInterfacesOfResource.isLeft()){
            return !allInterfacesOfResource.left().value()
                    .values()
                    .stream().map(InterfaceDefinition :: getOperationsMap)
                    .flatMap(map -> map.values().stream())
                    .map(OperationDataDefinition::getImplementation)
                    .filter(Objects::nonNull)
                    .anyMatch(add -> artifactLabel.equals(add.getArtifactLabel()));
        }
        return true;
    }

    private Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getArtifacts(ComponentTypeEnum componentType, Component parentComponent,
                                                                                         String componentId, ArtifactGroupTypeEnum artifactGroupType) {
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifactsResponse;
        if (componentType.equals(ComponentTypeEnum.RESOURCE_INSTANCE)) {
            artifactsResponse = artifactToscaOperation.getAllInstanceArtifacts(parentComponent.getUniqueId(), componentId);
        }
        else {
            artifactsResponse = artifactToscaOperation.getArtifacts(componentId);
        }
        if (artifactsResponse.isRight() && artifactsResponse.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
            log.debug("failed to retrieve artifacts for {} ", componentId);
            return Either.right(artifactsResponse.right().value());
        }
        return Either.left(artifactsResponse.left().value().entrySet()
                .stream()
                .filter(x -> artifactGroupType.equals(x.getValue().getArtifactGroupType()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
    }

    private List<String> getListOfArtifactName(Map<String, ArtifactDefinition> artifacts) {
        return artifacts.entrySet()
                .stream()
                .map(x -> x.getValue().getArtifactName())
                .collect(Collectors.toList());
    }

    // ***************************************************************

    private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> createAndLinkArtifact(org.openecomp.sdc.be.model.Component parent, String parentId, ArtifactDefinition artifactInfo, User user,
                                                                                                ComponentTypeEnum componentTypeEnum, AuditingActionEnum auditingActionEnum) {
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> resultOp = null;
        Either<ArtifactDefinition, Operation> insideEither = null;
        ComponentInstance foundInstance = findComponentInstance(parentId, parent);
        String instanceId = null;
        String instanceName = null;
        if (foundInstance != null) {
            instanceId = foundInstance.getUniqueId();
            instanceName = foundInstance.getName();
        }
        boolean isLeft = false;
        String artifactUniqueId = null;
        StorageOperationStatus error = null;
        // information/deployment/api aritfacts
        log.trace("Try to create entry on graph");
        NodeTypeEnum nodeType = convertParentType(componentTypeEnum);
        Either<ArtifactDefinition, StorageOperationStatus> result = artifactToscaOperation.addArifactToComponent(artifactInfo, parent
                .getUniqueId(), nodeType, true, instanceId);

        isLeft = result.isLeft();
        if (isLeft) {
            artifactUniqueId = result.left().value().getUniqueId();
            result.left().value();

            insideEither = Either.left(result.left().value());
            resultOp = Either.left(insideEither);

            error = generateCustomizationUUIDOnInstance(parent.getUniqueId(), parentId, componentTypeEnum);
            if (error != StorageOperationStatus.OK) {
                isLeft = false;
            }

        }
        if (isLeft) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
            handleAuditing(auditingActionEnum, parent, parentId, user, artifactInfo, artifactUniqueId, artifactUniqueId, responseFormat, componentTypeEnum, instanceName);
            return resultOp;
        }
        else {
            log.debug("Failed to create entry on graph for artifact {}", artifactInfo.getArtifactName());
            ResponseFormat responseFormat = componentsUtils.getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(error), artifactInfo
                    .getArtifactDisplayName());
            handleAuditing(auditingActionEnum, parent, parentId, user, artifactInfo, null, null, responseFormat, componentTypeEnum, instanceName);
            resultOp = Either.right(responseFormat);
            return resultOp;

        }
    }

    private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> createArtifact(org.openecomp.sdc.be.model.Component parent, String parentId, ArtifactDefinition artifactInfo, byte[] decodedPayload, User user,
                                                                                         ComponentTypeEnum componentTypeEnum, AuditingActionEnum auditingActionEnum, String interfaceType, String operationName) {

        ESArtifactData artifactData = createEsArtifactData(artifactInfo, decodedPayload);
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> resultOp = null;
        Either<ArtifactDefinition, Operation> insideEither = null;
        ComponentInstance foundInstance = findComponentInstance(parentId, parent);
        String instanceId = null;
        String instanceName = null;
        if (foundInstance != null) {
            if (foundInstance.isArtifactExists(artifactInfo.getArtifactGroupType(), artifactInfo.getArtifactLabel())) {
                log.debug("Failed to create artifact, already exists");
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_EXIST, artifactInfo
                        .getArtifactLabel());
                handleAuditing(auditingActionEnum, parent, parentId, user, artifactInfo, null, artifactInfo.getUniqueId(), responseFormat, componentTypeEnum, foundInstance
                        .getName());
                resultOp = Either.right(responseFormat);
                return resultOp;
            }

            instanceId = foundInstance.getUniqueId();
            instanceName = foundInstance.getName();
        }
        if (artifactData == null) {
            BeEcompErrorManager.getInstance().logBeDaoSystemError("Upload Artifact");
            log.debug("Failed to create artifact object for ES.");
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            handleAuditing(auditingActionEnum, parent, parentId, user, artifactInfo, null, null, responseFormat, componentTypeEnum, null);
            resultOp = Either.right(responseFormat);
            return resultOp;

        }
        // set on graph object id of artifact in ES!
        artifactInfo.setEsId(artifactData.getId());

        boolean isLeft = false;
        String artifactUniqueId = null;
        StorageOperationStatus error = null;
        if (interfaceType != null && operationName != null) {
            // lifecycle artifact
            Operation operation = convertToOperation(artifactInfo, operationName);

            Either<Operation, StorageOperationStatus> result = interfaceLifecycleOperation.updateInterfaceOperation(parentId, interfaceType, operationName, operation);

            isLeft = result.isLeft();
            if (isLeft) {
                artifactUniqueId = result.left().value().getImplementation().getUniqueId();
                result.left().value().getImplementation();

                insideEither = Either.right(result.left().value());
                resultOp = Either.left(insideEither);
            }
            else {
                error = result.right().value();
            }
        }
        else {
            // information/deployment/api aritfacts
            log.trace("Try to create entry on graph");
            NodeTypeEnum nodeType = convertParentType(componentTypeEnum);
            Either<ArtifactDefinition, StorageOperationStatus> result = artifactToscaOperation.addArifactToComponent(artifactInfo, parent
                    .getUniqueId(), nodeType, true, instanceId);

            isLeft = result.isLeft();
            if (isLeft) {
                artifactUniqueId = result.left().value().getUniqueId();
                artifactData.setId(result.left().value().getEsId());
                insideEither = Either.left(result.left().value());
                resultOp = Either.left(insideEither);

                error = generateCustomizationUUIDOnInstance(parent.getUniqueId(), parentId, componentTypeEnum);
                if (error != StorageOperationStatus.OK) {
                    isLeft = false;
                }

            }
            else {
                error = result.right().value();
            }
        }
        if (isLeft) {
            boolean res = saveArtifacts(artifactData, parentId);

            if (res) {
                log.debug(ARTIFACT_SAVED, artifactUniqueId);

                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
                handleAuditing(auditingActionEnum, parent, parentId, user, artifactInfo, artifactUniqueId, artifactUniqueId, responseFormat, componentTypeEnum, instanceName);
                return resultOp;
            }
            else {
                BeEcompErrorManager.getInstance().logBeDaoSystemError("Upload Artifact");
                log.debug(FAILED_SAVE_ARTIFACT);
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
                handleAuditing(auditingActionEnum, parent, parentId, user, artifactInfo, null, artifactUniqueId, responseFormat, componentTypeEnum, instanceName);

                resultOp = Either.right(responseFormat);
                return resultOp;
            }
        }
        else {
            log.debug("Failed to create entry on graph for artifact {}", artifactInfo.getArtifactName());
            ResponseFormat responseFormat = componentsUtils.getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(error), artifactInfo
                    .getArtifactDisplayName());
            handleAuditing(auditingActionEnum, parent, parentId, user, artifactInfo, null, null, responseFormat, componentTypeEnum, instanceName);
            resultOp = Either.right(responseFormat);
            return resultOp;
        }

    }

    private ComponentInstance findComponentInstance(String componentInstanceId, Component containerComponent) {
        ComponentInstance foundInstance = null;
        if (CollectionUtils.isNotEmpty(containerComponent.getComponentInstances())) {
            foundInstance = containerComponent.getComponentInstances()
                                              .stream()
                                              .filter(i -> i.getUniqueId().equals(componentInstanceId))
                                              .findFirst()
                                              .orElse(null);
        }
        return foundInstance;
    }

    private Either<Boolean, ResponseFormat> validateDeploymentArtifact(Component parentComponent, String parentId, boolean isCreate, ArtifactDefinition artifactInfo, ArtifactDefinition currentArtifact, NodeTypeEnum parentType) {

        Either<Boolean, ResponseFormat> result = Either.left(true);
        Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();

        validateArtifactTypeExists(responseWrapper, artifactInfo);

        ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifactInfo.getArtifactType());

        Map<String, ArtifactTypeConfig> resourceDeploymentArtifacts = fillDeploymentArtifactTypeConf(parentType);

        if (responseWrapper.isEmpty()) {
            validateDeploymentArtifactConf(artifactInfo, responseWrapper, artifactType, resourceDeploymentArtifacts);
        }

        // Common code for all types
        // not allowed to change artifactType
        if (responseWrapper.isEmpty() && !isCreate) {
            Either<Boolean, ResponseFormat> validateServiceApiType = validateArtifactTypeNotChanged(artifactInfo, currentArtifact);
            if (validateServiceApiType.isRight()) {
                responseWrapper.setInnerElement(validateServiceApiType.right().value());
            }
        }
        if (responseWrapper.isEmpty()) {
            if (parentType.equals(NodeTypeEnum.Resource)) {
                Resource resource = (Resource) parentComponent;
                ResourceTypeEnum resourceType = resource.getResourceType();
                ArtifactTypeConfig config = resourceDeploymentArtifacts.get(artifactType.getType());
                if (config == null) {
                    responseWrapper.setInnerElement(ResponseFormatManager.getInstance()
                                                                         .getResponseFormat(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED, artifactInfo
                                                                                 .getArtifactType()));
                }
                else {
                    List<String> myList = config.getValidForResourceTypes();
                    Either<Boolean, ResponseFormat> either = validateResourceType(resourceType, artifactInfo, myList);
                    if (either.isRight()) {
                        responseWrapper.setInnerElement(either.right().value());
                    }
                }
            }

            validateFileExtension(responseWrapper, () -> getDeploymentArtifactTypeConfig(parentType, artifactType), artifactInfo, parentType, artifactType);
        }

        if (responseWrapper.isEmpty() && !NodeTypeEnum.ResourceInstance.equals(parentType)) {
            String artifactName = artifactInfo.getArtifactName();
            if (isCreate || !artifactName.equalsIgnoreCase(currentArtifact.getArtifactName())) {
                validateSingleDeploymentArtifactName(responseWrapper, artifactName, parentComponent, parentType);
            }
        }

        if (responseWrapper.isEmpty()) {
            switch (artifactType) {
                case HEAT:
                case HEAT_VOL:
                case HEAT_NET:
                    result = validateHeatDeploymentArtifact(isCreate, artifactInfo, currentArtifact);
                    break;
                case HEAT_ENV:
                    result = validateHeatEnvDeploymentArtifact(parentComponent, parentId, artifactInfo, parentType);
                    artifactInfo.setTimeout(NodeTemplateOperation.NON_HEAT_TIMEOUT);
                    break;
                case DCAE_INVENTORY_TOSCA:
                case DCAE_INVENTORY_JSON:
                case DCAE_INVENTORY_POLICY:
                    // Validation is done in handle payload.
                case DCAE_INVENTORY_DOC:
                case DCAE_INVENTORY_BLUEPRINT:
                case DCAE_INVENTORY_EVENT:
                    // No specific validation
                default:
                    artifactInfo.setTimeout(NodeTemplateOperation.NON_HEAT_TIMEOUT);
                    break;
            }

        }

        if (!responseWrapper.isEmpty()) {
            result = Either.right(responseWrapper.getInnerElement());
        }
        return result;
    }

    private void validateDeploymentArtifactConf(ArtifactDefinition artifactInfo, Wrapper<ResponseFormat> responseWrapper, ArtifactTypeEnum artifactType, Map<String, ArtifactTypeConfig> resourceDeploymentArtifacts) {
        if ((resourceDeploymentArtifacts == null) || !resourceDeploymentArtifacts.containsKey(artifactType.name())) {
            ResponseFormat responseFormat = ResponseFormatManager.getInstance()
                                                                 .getResponseFormat(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED, artifactInfo
                                                                         .getArtifactType());
            responseWrapper.setInnerElement(responseFormat);
            log.debug("Artifact Type: {} Not found !", artifactInfo.getArtifactType());
        }
    }

    private Map<String, ArtifactTypeConfig> fillDeploymentArtifactTypeConf(NodeTypeEnum parentType) {
        Map<String, ArtifactTypeConfig> resourceDeploymentArtifacts = null;
        if (parentType.equals(NodeTypeEnum.Resource)) {
            resourceDeploymentArtifacts = ConfigurationManager.getConfigurationManager()
                                                              .getConfiguration()
                                                              .getResourceDeploymentArtifacts();
        }
        else if (parentType.equals(NodeTypeEnum.ResourceInstance)) {
            resourceDeploymentArtifacts = ConfigurationManager.getConfigurationManager()
                                                              .getConfiguration()
                                                              .getResourceInstanceDeploymentArtifacts();
        }
        else {
            resourceDeploymentArtifacts = ConfigurationManager.getConfigurationManager()
                                                              .getConfiguration()
                                                              .getServiceDeploymentArtifacts();
        }
        return resourceDeploymentArtifacts;
    }

    public void validateArtifactTypeExists(Wrapper<ResponseFormat> responseWrapper, ArtifactDefinition artifactInfo) {
        ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifactInfo.getArtifactType());
        if (artifactType == null) {
            ResponseFormat responseFormat = ResponseFormatManager.getInstance()
                                                                 .getResponseFormat(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED, artifactInfo
                                                                         .getArtifactType());
            responseWrapper.setInnerElement(responseFormat);
            log.debug("Artifact Type: {} Not found !", artifactInfo.getArtifactType());
        }
    }

    private ArtifactTypeConfig getDeploymentArtifactTypeConfig(NodeTypeEnum parentType, ArtifactTypeEnum artifactType) {
        ArtifactTypeConfig retConfig = null;
        String fileType = artifactType.getType();
        if (parentType.equals(NodeTypeEnum.Resource)) {
            retConfig = ConfigurationManager.getConfigurationManager()
                                            .getConfiguration()
                                            .getResourceDeploymentArtifacts()
                                            .get(fileType);
        }
        else if (parentType.equals(NodeTypeEnum.Service)) {
            retConfig = ConfigurationManager.getConfigurationManager()
                                            .getConfiguration()
                                            .getServiceDeploymentArtifacts()
                                            .get(fileType);
        }
        else if (parentType.equals(NodeTypeEnum.ResourceInstance)) {
            retConfig = ConfigurationManager.getConfigurationManager()
                                            .getConfiguration()
                                            .getResourceInstanceDeploymentArtifacts()
                                            .get(fileType);
        }
        return retConfig;
    }

    private Either<Boolean, ResponseFormat> extractHeatParameters(ArtifactDefinition artifactInfo) {
        // extract heat parameters
        if (artifactInfo.getPayloadData() != null) {
            String heatDecodedPayload = new String(Base64.decodeBase64(artifactInfo.getPayloadData()));
            Either<List<HeatParameterDefinition>, ResultStatusEnum> heatParameters = ImportUtils.getHeatParamsWithoutImplicitTypes(heatDecodedPayload, artifactInfo
                    .getArtifactType());
            if (heatParameters.isRight() && (!heatParameters.right()
                                                            .value()
                                                            .equals(ResultStatusEnum.ELEMENT_NOT_FOUND))) {
                log.info("failed to parse heat parameters ");
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_DEPLOYMENT_ARTIFACT_HEAT, artifactInfo
                        .getArtifactType());
                return Either.right(responseFormat);
            }
            else if (heatParameters.isLeft() && heatParameters.left().value() != null) {
                artifactInfo.setListHeatParameters(heatParameters.left().value());
            }
        }
        return Either.left(true);

    }

    // Valid extension
    public void validateFileExtension(Wrapper<ResponseFormat> responseWrapper, IDeploymentArtifactTypeConfigGetter deploymentConfigGetter, ArtifactDefinition artifactInfo, NodeTypeEnum parentType, ArtifactTypeEnum artifactType) {
        String fileType = artifactType.getType();
        List<String> acceptedTypes = null;
        ArtifactTypeConfig deploymentAcceptedTypes = deploymentConfigGetter.getDeploymentArtifactConfig();
        if (!parentType.equals(NodeTypeEnum.Resource) && !parentType.equals(NodeTypeEnum.Service) && !parentType.equals(NodeTypeEnum.ResourceInstance)) {
            log.debug("parent type of artifact can be either resource or service");
            responseWrapper.setInnerElement(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            return;
        }

        if (deploymentAcceptedTypes == null) {
            log.debug("parent type of artifact can be either resource or service");
            responseWrapper.setInnerElement(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED, artifactInfo
                    .getArtifactType()));
            return;
        }
        else {
            acceptedTypes = deploymentAcceptedTypes.getAcceptedTypes();
        }
        /*
         * No need to check specific types. In case there are no acceptedTypes in configuration, then any type is accepted.
         */

        String artifactName = artifactInfo.getArtifactName();
        String fileExtension = GeneralUtility.getFilenameExtension(artifactName);
        // Pavel - File extension validation is case-insensitive - Ella,
        // 21/02/2016
        if (acceptedTypes != null && !acceptedTypes.isEmpty() && !acceptedTypes.contains(fileExtension.toLowerCase())) {
            log.debug("File extension \"{}\" is not allowed for {} which is of type:{}", fileExtension, artifactName, fileType);
            responseWrapper.setInnerElement(componentsUtils.getResponseFormat(ActionStatus.WRONG_ARTIFACT_FILE_EXTENSION, fileType));
            return;
        }
    }

    private Either<Boolean, ResponseFormat> validateHeatEnvDeploymentArtifact(Component parentComponent, String parentId, ArtifactDefinition artifactInfo, NodeTypeEnum parentType) {

        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        Wrapper<ArtifactDefinition> heatMDWrapper = new Wrapper<>();
        Wrapper<byte[]> payloadWrapper = new Wrapper<>();

        if (errorWrapper.isEmpty()) {
            validateValidYaml(errorWrapper, artifactInfo);
        }

        if (errorWrapper.isEmpty()) {
            // Validate Heat Exist
            validateHeatExist(parentComponent.getUniqueId(), parentId, errorWrapper, heatMDWrapper, artifactInfo, parentType, parentComponent
                    .getComponentType());
        }

        if (errorWrapper.isEmpty() && !heatMDWrapper.isEmpty()) {
            fillArtifactPayloadValidation(errorWrapper, payloadWrapper, heatMDWrapper.getInnerElement());
        }

        if (errorWrapper.isEmpty() && !heatMDWrapper.isEmpty()) {
            validateEnvVsHeat(errorWrapper, artifactInfo, heatMDWrapper.getInnerElement(), payloadWrapper.getInnerElement());
        }

        // init Response
        Either<Boolean, ResponseFormat> eitherResponse;
        if (errorWrapper.isEmpty()) {
            eitherResponse = Either.left(true);
        }
        else {
            eitherResponse = Either.right(errorWrapper.getInnerElement());
        }
        return eitherResponse;
    }

    public void fillArtifactPayloadValidation(Wrapper<ResponseFormat> errorWrapper, Wrapper<byte[]> payloadWrapper, ArtifactDefinition artifactDefinition) {
        if (artifactDefinition.getPayloadData() == null || artifactDefinition.getPayloadData().length == 0) {
            Either<Boolean, ResponseFormat> fillArtifactPayload = fillArtifactPayload(payloadWrapper, artifactDefinition);
            if (fillArtifactPayload.isRight()) {
                errorWrapper.setInnerElement(fillArtifactPayload.right().value());
                log.debug("Error getting payload for artifact:{}", artifactDefinition.getArtifactName());
            }
        }
        else {
            payloadWrapper.setInnerElement(artifactDefinition.getPayloadData());
        }
    }

    public Either<Boolean, ResponseFormat> fillArtifactPayload(Wrapper<byte[]> payloadWrapper, ArtifactDefinition artifactMD) {
        Either<Boolean, ResponseFormat> result = Either.left(true);
        Either<ESArtifactData, CassandraOperationStatus> eitherArtifactData = artifactCassandraDao.getArtifact(artifactMD
                .getEsId());
        if (eitherArtifactData.isLeft()) {
            byte[] data = eitherArtifactData.left().value().getDataAsArray();
            data = Base64.encodeBase64(data);
            payloadWrapper.setInnerElement(data);
        }
        else {
            StorageOperationStatus storageStatus = DaoStatusConverter.convertCassandraStatusToStorageStatus(eitherArtifactData
                    .right()
                    .value());
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(storageStatus));
            result = Either.right(responseFormat);
        }
        return result;

    }

    @SuppressWarnings("unchecked")
    private void validateEnvVsHeat(Wrapper<ResponseFormat> errorWrapper, ArtifactDefinition envArtifact, ArtifactDefinition heatArtifact, byte[] heatPayloadData) {
        String envPayload = new String(Base64.decodeBase64(envArtifact.getPayloadData()));
        Map<String, Object> heatEnvToscaJson = (Map<String, Object>) new Yaml().load(envPayload);
        String heatDecodedPayload = new String(Base64.decodeBase64(heatPayloadData));
        Map<String, Object> heatToscaJson = (Map<String, Object>) new Yaml().load(heatDecodedPayload);

        Either<Map<String, Object>, ResultStatusEnum> eitherHeatEnvProperties = ImportUtils.findFirstToscaMapElement(heatEnvToscaJson, TypeUtils.ToscaTagNamesEnum.PARAMETERS);
        Either<Map<String, Object>, ResultStatusEnum> eitherHeatProperties = ImportUtils.findFirstToscaMapElement(heatToscaJson, TypeUtils.ToscaTagNamesEnum.PARAMETERS);
        if (eitherHeatEnvProperties.isRight()) {
            ResponseFormat responseFormat = ResponseFormatManager.getInstance()
                                                                 .getResponseFormat(ActionStatus.CORRUPTED_FORMAT, "Heat Env");
            errorWrapper.setInnerElement(responseFormat);
            log.debug("Invalid heat env format for file:{}", envArtifact.getArtifactName());
        }
        else if (eitherHeatProperties.isRight()) {
            ResponseFormat responseFormat = ResponseFormatManager.getInstance()
                                                                 .getResponseFormat(ActionStatus.MISMATCH_HEAT_VS_HEAT_ENV, envArtifact
                                                                         .getArtifactName(), heatArtifact.getArtifactName());
            errorWrapper.setInnerElement(responseFormat);
            log.debug("Validation of heat_env for artifact:{} vs heat artifact for artifact :{} failed", envArtifact.getArtifactName(), heatArtifact
                    .getArtifactName());
        }
        else {
            Set<String> heatPropertiesKeys = eitherHeatProperties.left().value().keySet();
            Set<String> heatEnvPropertiesKeys = eitherHeatEnvProperties.left().value().keySet();
            heatEnvPropertiesKeys.removeAll(heatPropertiesKeys);
            if (!heatEnvPropertiesKeys.isEmpty()) {
                ResponseFormat responseFormat = ResponseFormatManager.getInstance()
                                                                     .getResponseFormat(ActionStatus.MISMATCH_HEAT_VS_HEAT_ENV, envArtifact
                                                                             .getArtifactName(), heatArtifact.getArtifactName());
                errorWrapper.setInnerElement(responseFormat);
            }
        }
    }

    private void validateValidYaml(Wrapper<ResponseFormat> errorWrapper, ArtifactDefinition artifactInfo) {
        YamlToObjectConverter yamlConvertor = new YamlToObjectConverter();
        boolean isYamlValid = yamlConvertor.isValidYamlEncoded64(artifactInfo.getPayloadData());
        if (!isYamlValid) {
            ResponseFormat responseFormat = ResponseFormatManager.getInstance()
                                                                 .getResponseFormat(ActionStatus.INVALID_YAML, artifactInfo
                                                                         .getArtifactType());
            errorWrapper.setInnerElement(responseFormat);
            log.debug("Yaml is not valid for artifact : {}", artifactInfo.getArtifactName());
        }
    }

    private boolean isValidXml(byte[] xmlToParse) {
        boolean isXmlValid = true;
        try {
            XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            setFeatures(reader);
            reader.parse(new InputSource(new ByteArrayInputStream(xmlToParse)));
        }
        catch (ParserConfigurationException | IOException | SAXException e) {
            log.debug("Xml is invalid : {}", e.getMessage(), e);
            isXmlValid = false;
        }
        return isXmlValid;
    }

    private void setFeatures(XMLReader reader) throws SAXNotSupportedException {
        try {
            reader.setFeature("http://apache.org/xml/features/validation/schema", false);
            reader.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        }
        catch (SAXNotRecognizedException e) {
            log.debug("Xml parser couldn't set feature: \"http://apache.org/xml/features/validation/schema\", false", e.getMessage(), e);
        }
    }

    private void validateSingleDeploymentArtifactName(Wrapper<ResponseFormat> errorWrapper, String artifactName, Component parentComponent, NodeTypeEnum parentType) {
        boolean artifactNameFound = false;
        Iterator<ArtifactDefinition> parentDeploymentArtifactsItr = getDeploymentArtifacts(parentComponent, parentType, null)
                .iterator();

        while (!artifactNameFound && parentDeploymentArtifactsItr.hasNext()) {
            artifactNameFound = artifactName.equalsIgnoreCase(parentDeploymentArtifactsItr.next().getArtifactName());
        }
        if (artifactNameFound) {
            String parentName = parentComponent.getName();
            ResponseFormat responseFormat = ResponseFormatManager.getInstance()
                                                                 .getResponseFormat(ActionStatus.DEPLOYMENT_ARTIFACT_NAME_ALREADY_EXISTS, parentType
                                                                         .name(), parentName, artifactName);

            errorWrapper.setInnerElement(responseFormat);
            log.debug("Can't upload artifact: {}, because another artifact with this name already exist.", artifactName);

        }
    }

    private void validateHeatExist(String componentId, String parentRiId, Wrapper<ResponseFormat> errorWrapper, Wrapper<ArtifactDefinition> heatArtifactMDWrapper, ArtifactDefinition heatEnvArtifact, NodeTypeEnum parentType,
                                   ComponentTypeEnum componentType) {
        Either<ArtifactDefinition, StorageOperationStatus> res = artifactToscaOperation.getHeatArtifactByHeatEnvId(parentRiId, heatEnvArtifact, parentType, componentId, componentType);
        if (res.isRight()) {
            ResponseFormat responseFormat;
            if (res.right().value() == StorageOperationStatus.NOT_FOUND) {
                responseFormat = ResponseFormatManager.getInstance().getResponseFormat(ActionStatus.MISSING_HEAT);
            }
            else {
                responseFormat = ResponseFormatManager.getInstance().getResponseFormat(ActionStatus.MISSING_HEAT);
            }
            errorWrapper.setInnerElement(responseFormat);
            return;
        }
        ArtifactDefinition heatArtifact = res.left().value();
        heatArtifactMDWrapper.setInnerElement(heatArtifact);
    }

    private Either<Boolean, ResponseFormat> validateHeatDeploymentArtifact(boolean isCreate, ArtifactDefinition artifactInfo, ArtifactDefinition currentArtifact) {
        log.trace("Started HEAT pre-payload validation for artifact {}", artifactInfo.getArtifactLabel());
        // timeout > 0 for HEAT artifacts
        Integer timeout = artifactInfo.getTimeout();
        if (timeout == null) {
            Integer defaultTimeout = isCreate ? NodeTemplateOperation.getDefaultHeatTimeout() : currentArtifact.getTimeout();
            artifactInfo.setTimeout(defaultTimeout);
            // HEAT artifact but timeout is invalid
        }
        else if (timeout < 1) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_INVALID_TIMEOUT));
        }

        // US649856 - Allow several HEAT files on Resource
        log.trace("Ended HEAT validation for artifact {}", artifactInfo.getArtifactLabel());
        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateResourceType(ResourceTypeEnum resourceType, ArtifactDefinition artifactInfo, List<String> typeList) {
        String listToString = (typeList != null) ? typeList.toString() : "";
        ResponseFormat responseFormat = ResponseFormatManager.getInstance()
                                                             .getResponseFormat(ActionStatus.MISMATCH_BETWEEN_ARTIFACT_TYPE_AND_COMPONENT_TYPE, artifactInfo
                                                                     .getArtifactName(), listToString, resourceType.getValue());
        Either<Boolean, ResponseFormat> either = Either.right(responseFormat);
        String resourceTypeName = resourceType.name();
        if (typeList != null && typeList.contains(resourceTypeName)) {
            either = Either.left(true);
        }
        return either;
    }

    private Either<ArtifactDefinition, ResponseFormat> validateAndConvertHeatParamers(ArtifactDefinition artifactInfo, String artifactType) {
        if (artifactInfo.getHeatParameters() != null) {
            for (HeatParameterDefinition heatParam : artifactInfo.getListHeatParameters()) {
                String parameterType = heatParam.getType();
                HeatParameterType heatParameterType = HeatParameterType.isValidType(parameterType);
                String artifactTypeStr = artifactType != null ? artifactType : ArtifactTypeEnum.HEAT.getType();
                if (heatParameterType == null) {
                    ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_HEAT_PARAMETER_TYPE, artifactTypeStr, heatParam
                            .getType());
                    return Either.right(responseFormat);
                }

                StorageOperationStatus validateAndUpdateProperty = heatParametersOperation.validateAndUpdateProperty(heatParam);
                if (validateAndUpdateProperty != StorageOperationStatus.OK) {
                    log.debug("Heat parameter {} is invalid. Status is {}", heatParam.getName(), validateAndUpdateProperty);
                    ActionStatus status = ActionStatus.INVALID_HEAT_PARAMETER_VALUE;
                    ResponseFormat responseFormat = componentsUtils.getResponseFormat(status, artifactTypeStr, heatParam
                            .getType(), heatParam.getName());
                    return Either.right(responseFormat);
                }
            }
        }
        return Either.left(artifactInfo);
    }

    public List<ArtifactDefinition> getDeploymentArtifacts(Component parentComponent, NodeTypeEnum parentType, String ciId) {
        List<ArtifactDefinition> deploymentArtifacts = new ArrayList<>();
        if (parentComponent.getDeploymentArtifacts() != null) {
            if (NodeTypeEnum.ResourceInstance == parentType && ciId != null) {
                Either<ComponentInstance, ResponseFormat> getRI = getRIFromComponent(parentComponent, ciId, null, null, null);
                if (getRI.isRight()) {
                    return deploymentArtifacts;
                }
                ComponentInstance ri = getRI.left().value();
                if (ri.getDeploymentArtifacts() != null) {
                    deploymentArtifacts.addAll(ri.getDeploymentArtifacts().values());
                }
            }
            else if (parentComponent.getDeploymentArtifacts() != null) {
                deploymentArtifacts.addAll(parentComponent.getDeploymentArtifacts().values());
            }
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


    private String composeArtifactId(String resourceId, String artifactId, ArtifactDefinition artifactInfo, String interfaceName, String operationName) {
        String id = artifactId;
        if (artifactId == null || artifactId.isEmpty()) {
            String uniqueId = null;
            if (interfaceName != null && operationName != null) {
                uniqueId = UniqueIdBuilder.buildArtifactByInterfaceUniqueId(resourceId, interfaceName, operationName, artifactInfo
                        .getArtifactLabel());
            }
            else {
                uniqueId = UniqueIdBuilder.buildPropertyUniqueId(resourceId, artifactInfo.getArtifactLabel());
            }
            artifactInfo.setUniqueId(uniqueId);
            artifactInfo.setEsId(uniqueId);
            id = uniqueId;
        }
        else {
            artifactInfo.setUniqueId(artifactId);
            artifactInfo.setEsId(artifactId);
        }
        return id;
    }

    private Either<ActionStatus, ResponseFormat> validateArtifactType(String userId, ArtifactDefinition artifactInfo, NodeTypeEnum parentType) {
        if (Strings.isNullOrEmpty(artifactInfo.getArtifactType())) {
            BeEcompErrorManager.getInstance()
                               .logBeMissingArtifactInformationError("Artifact Update / Upload", "artifactLabel");
            log.debug("Missing artifact type for artifact {}", artifactInfo.getArtifactName());
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_ARTIFACT_TYPE));
        }

        boolean artifactTypeExist = false;
        Either<List<ArtifactType>, ActionStatus> allArtifactTypes = null;
        ArtifactGroupTypeEnum artifactGroupType = artifactInfo.getArtifactGroupType();

        if ((artifactGroupType != null) && artifactGroupType.equals(ArtifactGroupTypeEnum.DEPLOYMENT)) {
            allArtifactTypes = getDeploymentArtifactTypes(parentType);
        }
        else {

            allArtifactTypes = elementOperation.getAllArtifactTypes();
        }
        if (allArtifactTypes.isRight()) {
            BeEcompErrorManager.getInstance()
                               .logBeInvalidConfigurationError("Artifact Upload / Update", "artifactTypes", allArtifactTypes
                                       .right()
                                       .value()
                                       .name());
            log.debug("Failed to retrieve list of suported artifact types. error: {}", allArtifactTypes.right()
                                                                                                       .value());
            return Either.right(componentsUtils.getResponseFormatByUserId(allArtifactTypes.right().value(), userId));
        }

        for (ArtifactType type : allArtifactTypes.left().value()) {
            if (type.getName().equalsIgnoreCase(artifactInfo.getArtifactType())) {
                artifactInfo.setArtifactType(artifactInfo.getArtifactType().toUpperCase());
                artifactTypeExist = true;
                break;
            }
        }

        if (!artifactTypeExist) {
            BeEcompErrorManager.getInstance()
                               .logBeInvalidTypeError("Artifact Upload / Delete / Update - Not supported artifact type", artifactInfo
                                       .getArtifactType(), "Artifact " + artifactInfo.getArtifactName());
            log.debug("Not supported artifact type = {}", artifactInfo.getArtifactType());
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED, artifactInfo
                    .getArtifactType()));
        }

        return Either.left(ActionStatus.OK);
    }

    private Either<List<ArtifactType>, ActionStatus> getDeploymentArtifactTypes(NodeTypeEnum parentType) {

        Map<String, ArtifactTypeConfig> deploymentArtifacts ;
        List<ArtifactType> artifactTypes = new ArrayList<>();

        if (parentType.equals(NodeTypeEnum.Service)) {
            deploymentArtifacts = ConfigurationManager.getConfigurationManager()
                                                      .getConfiguration()
                                                      .getServiceDeploymentArtifacts();
        }
        else if (parentType.equals(NodeTypeEnum.ResourceInstance)) {
            deploymentArtifacts = ConfigurationManager.getConfigurationManager()
                                                      .getConfiguration()
                                                      .getResourceInstanceDeploymentArtifacts();
        }
        else {
            deploymentArtifacts = ConfigurationManager.getConfigurationManager()
                                                      .getConfiguration()
                                                      .getResourceDeploymentArtifacts();
        }
        if (deploymentArtifacts != null) {
            for (String artifactType : deploymentArtifacts.keySet()) {
                ArtifactType artifactT = new ArtifactType();
                artifactT.setName(artifactType);
                artifactTypes.add(artifactT);
            }
            return Either.left(artifactTypes);
        }
        else {
            return Either.right(ActionStatus.GENERAL_ERROR);
        }

    }

    private Either<Boolean, ResponseFormat> validateFirstUpdateHasPayload(ArtifactDefinition artifactInfo, ArtifactDefinition currentArtifact) {
        if (currentArtifact.getEsId() == null && (artifactInfo.getPayloadData() == null || artifactInfo.getPayloadData().length == 0)) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, ARTIFACT_PAYLOAD));
        }
        return Either.left(true);

    }

    private Either<Boolean, ResponseFormat> validateAndSetArtifactname(ArtifactDefinition artifactInfo) {
        if (artifactInfo.getArtifactName() == null || artifactInfo.getArtifactName().isEmpty()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_ARTIFACT_NAME));
        }

        String normalizeFileName = ValidationUtils.normalizeFileName(artifactInfo.getArtifactName());
        if (normalizeFileName == null || normalizeFileName.isEmpty()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_ARTIFACT_NAME));
        }
        artifactInfo.setArtifactName(normalizeFileName);

        if (!ValidationUtils.validateArtifactNameLength(artifactInfo.getArtifactName())) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.EXCEEDS_LIMIT, ARTIFACT_NAME, String.valueOf(ValidationUtils.ARTIFACT_NAME_LENGTH)));
        }

        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateArtifactTypeNotChanged(ArtifactDefinition artifactInfo, ArtifactDefinition currentArtifact) {
        if (artifactInfo.getArtifactType() == null || artifactInfo.getArtifactType().isEmpty()) {
            log.info("artifact type is missing operation ignored");
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_ARTIFACT_TYPE));
        }

        if (!currentArtifact.getArtifactType().equalsIgnoreCase(artifactInfo.getArtifactType())) {
            log.info("artifact type cannot be changed operation ignored");
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
        }
        return Either.left(true);
    }

    private Either<ArtifactDefinition, ResponseFormat> validateOrSetArtifactGroupType(ArtifactDefinition artifactInfo, ArtifactDefinition currentArtifact) {
        if (artifactInfo.getArtifactGroupType() == null) {
            artifactInfo.setArtifactGroupType(currentArtifact.getArtifactGroupType());
        }

        else if (!currentArtifact.getArtifactGroupType()
                                 .getType()
                                 .equalsIgnoreCase(artifactInfo.getArtifactGroupType().getType())) {
            log.info("artifact group type cannot be changed. operation failed");
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
        }
        return Either.left(artifactInfo);
    }

    private void checkAndSetUnUpdatableFields(User user, ArtifactDefinition artifactInfo, ArtifactDefinition currentArtifact, ArtifactGroupTypeEnum type) {

        // on update if null add informational to current
        if (currentArtifact.getArtifactGroupType() == null && type != null) {
            currentArtifact.setArtifactGroupType(type);
        }

        if (artifactInfo.getUniqueId() != null && !currentArtifact.getUniqueId().equals(artifactInfo.getUniqueId())) {
            log.error("artifact uniqid cannot be set ignoring");
        }
        artifactInfo.setUniqueId(currentArtifact.getUniqueId());

        if (artifactInfo.getArtifactRef() != null && !currentArtifact.getArtifactRef()
                                                                     .equals(artifactInfo.getArtifactRef())) {
            log.error("artifact ref cannot be set ignoring");
        }
        artifactInfo.setArtifactRef(currentArtifact.getArtifactRef());

        if (artifactInfo.getArtifactRepository() != null && !currentArtifact.getArtifactRepository()
                                                                            .equals(artifactInfo.getArtifactRepository())) {
            log.error("artifact repository cannot be set ignoring");
        }
        artifactInfo.setArtifactRepository(currentArtifact.getArtifactRepository());

        if (artifactInfo.getUserIdCreator() != null && !currentArtifact.getUserIdCreator()
                                                                       .equals(artifactInfo.getUserIdCreator())) {
            log.error("creator uuid cannot be set ignoring");
        }
        artifactInfo.setUserIdCreator(currentArtifact.getUserIdCreator());

        if (artifactInfo.getArtifactCreator() != null && !currentArtifact.getArtifactCreator()
                                                                         .equals(artifactInfo.getArtifactCreator())) {
            log.error("artifact creator cannot be set ignoring");
        }
        artifactInfo.setArtifactCreator(currentArtifact.getArtifactCreator());

        if (artifactInfo.getUserIdLastUpdater() != null && !currentArtifact.getUserIdLastUpdater()
                                                                           .equals(artifactInfo.getUserIdLastUpdater())) {
            log.error("userId of last updater cannot be set ignoring");
        }
        artifactInfo.setUserIdLastUpdater(user.getUserId());

        if (artifactInfo.getCreatorFullName() != null && !currentArtifact.getCreatorFullName()
                                                                         .equals(artifactInfo.getCreatorFullName())) {
            log.error("creator Full name cannot be set ignoring");
        }
        artifactInfo.setCreatorFullName(currentArtifact.getCreatorFullName());

        if (artifactInfo.getUpdaterFullName() != null && !currentArtifact.getUpdaterFullName()
                                                                         .equals(artifactInfo.getUpdaterFullName())) {
            log.error("updater Full name cannot be set ignoring");
        }
        String fullName = user.getFirstName() + " " + user.getLastName();
        artifactInfo.setUpdaterFullName(fullName);

        if (artifactInfo.getCreationDate() != null && !currentArtifact.getCreationDate()
                                                                      .equals(artifactInfo.getCreationDate())) {
            log.error("Creation Date cannot be set ignoring");
        }
        artifactInfo.setCreationDate(currentArtifact.getCreationDate());

        if (artifactInfo.getLastUpdateDate() != null && !currentArtifact.getLastUpdateDate()
                                                                        .equals(artifactInfo.getLastUpdateDate())) {
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

        if (artifactInfo.getServiceApi() != null && !currentArtifact.getServiceApi()
                                                                    .equals(artifactInfo.getServiceApi())) {
            log.debug("serviceApi cannot be set. ignoring.");
        }
        artifactInfo.setServiceApi(currentArtifact.getServiceApi());

        if (artifactInfo.getArtifactGroupType() != null && !currentArtifact.getArtifactGroupType()
                                                                           .equals(artifactInfo.getArtifactGroupType())) {
            log.debug("artifact group cannot be set. ignoring.");
        }
        artifactInfo.setArtifactGroupType(currentArtifact.getArtifactGroupType());

        artifactInfo.setArtifactVersion(currentArtifact.getArtifactVersion());

        if (artifactInfo.getArtifactUUID() != null && !artifactInfo.getArtifactUUID()
                                                                   .isEmpty() && !currentArtifact.getArtifactUUID()
                                                                                                 .equals(artifactInfo.getArtifactUUID())) {
            log.debug("artifact UUID cannot be set. ignoring.");
        }
        artifactInfo.setArtifactUUID(currentArtifact.getArtifactUUID());

        if ((artifactInfo.getHeatParameters() != null) && (currentArtifact.getHeatParameters() != null) && !artifactInfo
                .getHeatParameters()
                .isEmpty() && !currentArtifact.getHeatParameters().isEmpty()) {
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
                if (parameter.getDefaultValue() != null && !parameter.getDefaultValue()
                                                                     .equalsIgnoreCase(currentParam.getDefaultValue())) {
                    log.debug("heat parameter defaultValue cannot be updated  ({}). ignoring.", parameter.getDefaultValue());
                    parameter.setDefaultValue(currentParam.getDefaultValue());
                }
                if (parameter.getType() != null && !parameter.getType().equalsIgnoreCase(currentParam.getType())) {
                    log.debug("heat parameter type cannot be updated  ({}). ignoring.", parameter.getType());
                    parameter.setType(currentParam.getType());
                }
                if (parameter.getDescription() != null && !parameter.getDescription()
                                                                    .equalsIgnoreCase(currentParam.getDescription())) {
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

        Map<String, HeatParameterDefinition> currentParamsMap = new HashMap<String, HeatParameterDefinition>();
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
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.EXCEEDS_LIMIT, ARTIFACT_URL, String.valueOf(ValidationUtils.API_URL_LENGTH)));
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
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.EXCEEDS_LIMIT, ARTIFACT_DESCRIPTION, String
                    .valueOf(ValidationUtils.ARTIFACT_DESCRIPTION_MAX_LENGTH)));
        }
        artifactInfo.setDescription(description);
        return Either.left(true);
    }

    private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> updateArtifactFlow(org.openecomp.sdc.be.model.Component parent, String parentId, String artifactId, ArtifactDefinition artifactInfo, User user, byte[] decodedPayload,
                                                                                             ComponentTypeEnum componentType, AuditingActionEnum auditingAction, String interfaceType, String operationUuid) {
        ESArtifactData artifactData = createEsArtifactData(artifactInfo, decodedPayload);
        String prevArtifactId = null;
        String currArtifactId = artifactId;

        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> resultOp = null;
        Either<ArtifactDefinition, Operation> insideEither = null;

        log.trace("Try to update entry on graph");
        String artifactUniqueId = null;
        ArtifactDefinition artifactDefinition = artifactInfo;
        StorageOperationStatus error;

        boolean isLeft;
        if (interfaceType == null || operationUuid == null) {
            log.debug("Entity on graph is updated. Update artifact in ES");
            boolean res = true;
            // Changing previous and current artifactId for auditing
            prevArtifactId = currArtifactId;
            currArtifactId = artifactDefinition.getUniqueId();


            if (decodedPayload == null) {
                if (!artifactDefinition.getMandatory() || artifactDefinition.getEsId() != null) {
                    Either<ESArtifactData, CassandraOperationStatus> artifactFromCassandra = artifactCassandraDao.getArtifact(artifactDefinition
                            .getEsId());
                    if (artifactFromCassandra.isRight()) {
                        log.debug("Failed to get artifact data from ES for artifact id  {}", artifactId);
                        error = DaoStatusConverter.convertCassandraStatusToStorageStatus(artifactFromCassandra.right()
                                .value());
                        ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(error));
                        handleAuditing(auditingAction, parent, parentId, user, artifactInfo, prevArtifactId, currArtifactId, responseFormat, componentType, null);
                        resultOp = Either.right(responseFormat);
                        return resultOp;
                    }
                    // clone data to new artifact
                    artifactData.setData(artifactFromCassandra.left().value().getData());
                    artifactData.setId(artifactFromCassandra.left().value().getId());
                }
            } else {
                if (artifactDefinition.getEsId() == null) {
                    artifactDefinition.setEsId(artifactDefinition.getUniqueId());
                    artifactData.setId(artifactDefinition.getUniqueId());
                }
            }

            NodeTypeEnum convertParentType = convertParentType(componentType);
            Either<ArtifactDefinition, StorageOperationStatus> result = artifactToscaOperation.updateArtifactOnResource(artifactInfo, parent
                    .getUniqueId(), artifactId, convertParentType, parentId);
            isLeft = result.isLeft();
            if (isLeft) {
                artifactUniqueId = result.left().value().getUniqueId();
                artifactDefinition = result.left().value();
                String artifactType = artifactInfo.getArtifactType();
                if (NodeTypeEnum.Resource == convertParentType
                        && (ArtifactTypeEnum.HEAT.getType().equalsIgnoreCase(artifactType)
                        || ArtifactTypeEnum.HEAT_VOL.getType().equalsIgnoreCase(artifactType)
                        || ArtifactTypeEnum.HEAT_NET.getType().equalsIgnoreCase(artifactType))
                        && !artifactUniqueId.equals(artifactId)) {
                    // need to update the generated id in heat env
                    Map<String, ArtifactDefinition> deploymentArtifacts = parent.getDeploymentArtifacts();
                    Optional<Entry<String, ArtifactDefinition>> findFirst = deploymentArtifacts.entrySet()
                            .stream()
                            .filter(a -> a.getValue()
                                    .getGeneratedFromId() != null && a
                                    .getValue()
                                    .getGeneratedFromId()
                                    .equals(artifactId))
                            .findFirst();
                    if (findFirst.isPresent()) {
                        ArtifactDefinition artifactEnvInfo = findFirst.get().getValue();
                        artifactEnvInfo.setArtifactChecksum(null);
                        artifactToscaOperation.updateHeatEnvArtifact(parent.getUniqueId(), artifactEnvInfo, artifactId, artifactUniqueId, convertParentType, parentId);
                    }
                }
                error = generateCustomizationUUIDOnInstance(parent.getUniqueId(), parentId, componentType);

                insideEither = Either.left(result.left().value());
                resultOp = Either.left(insideEither);
                if (error != StorageOperationStatus.OK) {
                    isLeft = false;
                }

            } else {
                error = result.right().value();
            }
            if (isLeft) {

                // create new entry in ES
                res = true;
                if (artifactData.getData() != null) {
                    if (!artifactDefinition.getDuplicated() || artifactData.getId() == null) {
                        artifactData.setId(artifactDefinition.getEsId());
                    }
                    res = saveArtifacts(artifactData, parentId);

                }
            }

            if (res) {
                log.debug(ARTIFACT_SAVED, artifactUniqueId);
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
                handleAuditing(auditingAction, parent, parentId, user, artifactInfo, prevArtifactId, currArtifactId, responseFormat, componentType, null);
            } else {
                BeEcompErrorManager.getInstance().logBeDaoSystemError(UPDATE_ARTIFACT);
                log.debug(FAILED_SAVE_ARTIFACT);
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
                handleAuditing(auditingAction, parent, parentId, user, artifactInfo, prevArtifactId, currArtifactId, responseFormat, componentType, null);
                resultOp = Either.right(responseFormat);
            }
        } else {
            return updateArtifactsFlowForInterfaceOperations(parent, parentId, artifactId, artifactInfo, user,
                    decodedPayload, componentType, auditingAction, interfaceType, operationUuid, artifactData, prevArtifactId,
                    currArtifactId, artifactDefinition);
        }

        return resultOp;
    }

    private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> updateArtifactsFlowForInterfaceOperations(
            Component parent, String parentId, String artifactId, ArtifactDefinition artifactInfo, User user,
            byte[] decodedPayload, ComponentTypeEnum componentType, AuditingActionEnum auditingAction, String interfaceType,
            String operationUuid, ESArtifactData artifactData, String prevArtifactId, String currArtifactId,
            ArtifactDefinition artifactDefinition) {
        StorageOperationStatus error;
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> resultOp;
        if (decodedPayload == null) {
            if (!artifactDefinition.getMandatory() || artifactDefinition.getEsId() != null) {
                Either<ESArtifactData, CassandraOperationStatus> artifactFromCassandra = artifactCassandraDao.getArtifact(artifactDefinition
                        .getEsId());
                if (artifactFromCassandra.isRight()) {
                    log.debug("Failed to get artifact data from ES for artifact id  {}", artifactId);
                    error = DaoStatusConverter.convertCassandraStatusToStorageStatus(artifactFromCassandra.right()
                            .value());
                    ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(error));
                    handleAuditing(auditingAction, parent, parentId, user, artifactInfo, prevArtifactId, currArtifactId, responseFormat, componentType, null);
                    resultOp = Either.right(responseFormat);
                    return resultOp;
                }
                // clone data to new artifact
                artifactData.setData(artifactFromCassandra.left().value().getData());
                artifactData.setId(artifactFromCassandra.left().value().getId());
            } else {
                // todo if not exist(first time)
            }

        } else {
            if (artifactDefinition.getEsId() == null) {
                artifactDefinition.setEsId(artifactDefinition.getUniqueId());
                artifactData.setId(artifactDefinition.getUniqueId());
            }
        }
        NodeTypeEnum convertParentType = convertParentType(componentType);
        // Set additional fields for artifact
        artifactInfo.setArtifactLabel(artifactInfo.getArtifactName());
        artifactInfo.setArtifactDisplayName(artifactInfo.getArtifactName());

        Either<ArtifactDefinition, StorageOperationStatus> updateArtifactOnResourceEither =
                artifactToscaOperation.updateArtifactOnResource(artifactInfo, parent.getUniqueId(), artifactId, convertParentType, parentId);
        if(updateArtifactOnResourceEither.isRight()){
            log.debug("Failed to persist operation artifact {} in resource, error is {}",artifactInfo.getArtifactName(), updateArtifactOnResourceEither.right().value());
            ActionStatus convertedFromStorageResponse = componentsUtils.convertFromStorageResponse(updateArtifactOnResourceEither.right().value());
            return Either.right(componentsUtils.getResponseFormat(convertedFromStorageResponse));
        }
        if (artifactData.getData() != null) {
            CassandraOperationStatus cassandraOperationStatus = artifactCassandraDao.saveArtifact(artifactData);
            if(cassandraOperationStatus != CassandraOperationStatus.OK){
                log.debug("Failed to persist operation artifact {}, error is {}",artifactInfo.getArtifactName(),cassandraOperationStatus);
                StorageOperationStatus storageStatus = DaoStatusConverter.convertCassandraStatusToStorageStatus(cassandraOperationStatus);
                ActionStatus convertedFromStorageResponse = componentsUtils.convertFromStorageResponse(storageStatus);
                return Either.right(componentsUtils.getResponseFormat(convertedFromStorageResponse));
            }
        }

        Either<ArtifactDefinition, ResponseFormat> updateOprEither = updateOperationArtifact(parentId, interfaceType, operationUuid, updateArtifactOnResourceEither.left().value());
        if(updateOprEither.isRight()){
            return Either.right(updateOprEither.right().value());
        }

        return Either.left(Either.left(updateOprEither.left().value()));
    }

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
                log.debug("In artifact: {} Payload is missing.",artifactInfo.getArtifactName());
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, ARTIFACT_PAYLOAD);
                return Either.right(responseFormat);
            }
        }
        log.trace("Ended payload handling");
        return Either.left(decodedPayload);
    }

    public Either<Operation, ResponseFormat> deleteArtifactByInterface(String resourceId, String userUserId, String artifactId,
                                                                       boolean inTransaction) {
        User user = new User();
        user.setUserId(userUserId);
        Either<Resource, StorageOperationStatus> parent = toscaOperationFacade.getToscaElement(resourceId, JsonParseFlagEnum.ParseMetadata);
        if (parent.isRight()) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(parent
                    .right()
                    .value()));
            return Either.right(responseFormat);
        }
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleDelete = handleDelete(resourceId, artifactId, user, AuditingActionEnum.ARTIFACT_DELETE, ComponentTypeEnum.RESOURCE, parent
                        .left()
                        .value(),
                false, inTransaction);
        if (handleDelete.isRight()) {
            return Either.right(handleDelete.right().value());
        }
        Either<ArtifactDefinition, Operation> result = handleDelete.left().value();
        return Either.left(result.right().value());

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
    public Either<byte[], ResponseFormat> downloadRsrcArtifactByNames(String serviceName, String serviceVersion, String resourceName, String resourceVersion, String artifactName) {

        // General validation
        if (serviceName == null || serviceVersion == null || resourceName == null || resourceVersion == null || artifactName == null) {
            log.debug(NULL_PARAMETER);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
        }

        // Normalizing artifact name
        artifactName = ValidationUtils.normalizeFileName(artifactName);

        // Resource validation
        Either<Resource, ResponseFormat> validateResourceNameAndVersion = validateResourceNameAndVersion(resourceName, resourceVersion);
        if (validateResourceNameAndVersion.isRight()) {
            return Either.right(validateResourceNameAndVersion.right().value());
        }

        Resource resource = validateResourceNameAndVersion.left().value();
        String resourceId = resource.getUniqueId();

        // Service validation
        Either<Service, ResponseFormat> validateServiceNameAndVersion = validateServiceNameAndVersion(serviceName, serviceVersion);
        if (validateServiceNameAndVersion.isRight()) {
            return Either.right(validateServiceNameAndVersion.right().value());
        }

        Map<String, ArtifactDefinition> artifacts = resource.getDeploymentArtifacts();
        if (artifacts == null || artifacts.isEmpty()) {
            log.debug("Deployment artifacts of resource {} are not found", resourceId);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, artifactName));
        }

        ArtifactDefinition deploymentArtifact = null;

        for (ArtifactDefinition artifactDefinition : artifacts.values()) {
            if (artifactDefinition.getArtifactName() != null && artifactDefinition.getArtifactName()
                                                                                  .equals(artifactName)) {
                log.debug(FOUND_DEPLOYMENT_ARTIFACT, artifactName);
                deploymentArtifact = artifactDefinition;
                break;
            }
        }

        if (deploymentArtifact == null) {
            log.debug("No deployment artifact {} was found for resource {}", artifactName, resourceId);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, artifactName));
        }

        // Downloading the artifact
        Either<ImmutablePair<String, byte[]>, ResponseFormat> downloadArtifactEither = downloadArtifact(deploymentArtifact);
        if (downloadArtifactEither.isRight()) {
            log.debug(FAILED_DOWNLOAD_ARTIFACT, artifactName);
            return Either.right(downloadArtifactEither.right().value());
        }
        log.trace("Download of resource artifact succeeded, uniqueId {}", deploymentArtifact.getUniqueId());
        return Either.left(downloadArtifactEither.left().value().getRight());
    }

    // download by MSO
    public Either<byte[], ResponseFormat> downloadRsrcInstArtifactByNames(String serviceName, String serviceVersion, String resourceInstanceName, String artifactName) {

        // General validation
        if (serviceName == null || serviceVersion == null || resourceInstanceName == null || artifactName == null) {
            log.debug(NULL_PARAMETER);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
        }

        // Normalizing artifact name
        artifactName = ValidationUtils.normalizeFileName(artifactName);

        // Service validation
        Either<Service, ResponseFormat> validateServiceNameAndVersion = validateServiceNameAndVersion(serviceName, serviceVersion);
        if (validateServiceNameAndVersion.isRight()) {
            return Either.right(validateServiceNameAndVersion.right().value());
        }

        Service service = validateServiceNameAndVersion.left().value();

        // ResourceInstance validation
        Either<ComponentInstance, ResponseFormat> validateResourceInstance = validateResourceInstance(service, resourceInstanceName);
        if (validateResourceInstance.isRight()) {
            return Either.right(validateResourceInstance.right().value());
        }

        ComponentInstance resourceInstance = validateResourceInstance.left().value();

        Map<String, ArtifactDefinition> artifacts = resourceInstance.getDeploymentArtifacts();

        final String finalArtifactName = artifactName;
        Predicate<ArtifactDefinition> filterArtifactByName = p -> p.getArtifactName().equals(finalArtifactName);

        boolean hasDeploymentArtifacts = artifacts != null && artifacts.values()
                                                                       .stream()
                                                                       .anyMatch(filterArtifactByName);
        ArtifactDefinition deployableArtifact;

        if (!hasDeploymentArtifacts) {
            log.debug("Deployment artifact with name {} not found", artifactName);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, artifactName));
        }

        log.debug(FOUND_DEPLOYMENT_ARTIFACT, artifactName);
        deployableArtifact = artifacts.values().stream().filter(filterArtifactByName).findFirst().get();
        // Downloading the artifact
        Either<ImmutablePair<String, byte[]>, ResponseFormat> downloadArtifactEither = downloadArtifact(deployableArtifact);

        if (downloadArtifactEither.isRight()) {
            log.debug(FAILED_DOWNLOAD_ARTIFACT, artifactName);
            return Either.right(downloadArtifactEither.right().value());
        }
        log.trace("Download of resource artifact succeeded, uniqueId {}", deployableArtifact.getUniqueId());
        return Either.left(downloadArtifactEither.left().value().getRight());
    }

    private Either<ComponentInstance, ResponseFormat> validateResourceInstance(Service service, String resourceInstanceName) {

        List<ComponentInstance> riList = service.getComponentInstances();
        for (ComponentInstance ri : riList) {
            if (ri.getNormalizedName().equals(resourceInstanceName)) {
                return Either.left(ri);
            }
        }

        return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND, resourceInstanceName));
    }

    private Either<Service, ResponseFormat> validateServiceNameAndVersion(String serviceName, String serviceVersion) {

        Either<List<Service>, StorageOperationStatus> serviceListBySystemName = toscaOperationFacade.getBySystemName(ComponentTypeEnum.SERVICE, serviceName);
        if (serviceListBySystemName.isRight()) {
            log.debug("Couldn't fetch any service with name {}", serviceName);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(serviceListBySystemName
                    .right()
                    .value(), ComponentTypeEnum.SERVICE), serviceName));
        }
        List<Service> serviceList = serviceListBySystemName.left().value();
        if (serviceList == null || serviceList.isEmpty()) {
            log.debug("Couldn't fetch any service with name {}", serviceName);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.SERVICE_NOT_FOUND, serviceName));
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
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_VERSION_NOT_FOUND, ComponentTypeEnum.SERVICE
                    .getValue(), serviceVersion));
        }
        return Either.left(foundService);
    }

    private Either<Resource, ResponseFormat> validateResourceNameAndVersion(String resourceName, String resourceVersion) {

        Either<Resource, StorageOperationStatus> resourceListBySystemName = toscaOperationFacade.getComponentByNameAndVersion(ComponentTypeEnum.RESOURCE, resourceName, resourceVersion, JsonParseFlagEnum.ParseMetadata);
        if (resourceListBySystemName.isRight()) {
            log.debug("Couldn't fetch any resource with name {} and version {}. ", resourceName, resourceVersion);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(resourceListBySystemName
                    .right()
                    .value()), resourceName));
        }
        return Either.left(resourceListBySystemName.left().value());
    }

    public Either<byte[], ResponseFormat> downloadServiceArtifactByNames(String serviceName, String serviceVersion, String artifactName) {
        // Validation
        log.trace("Starting download of service interface artifact, serviceName {}, serviceVersion {}, artifact name {}", serviceName, serviceVersion, artifactName);
        if (serviceName == null || serviceVersion == null || artifactName == null) {
            log.debug(NULL_PARAMETER);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
        }

        // Normalizing artifact name
        final String normalizedArtifactName = ValidationUtils.normalizeFileName(artifactName);

        // Service validation
        Either<Service, ResponseFormat> validateServiceNameAndVersion = validateServiceNameAndVersion(serviceName, serviceVersion);
        if (validateServiceNameAndVersion.isRight()) {
            return Either.right(validateServiceNameAndVersion.right().value());
        }

        String serviceId = validateServiceNameAndVersion.left().value().getUniqueId();

        // Looking for deployment or tosca artifacts
        Service service = validateServiceNameAndVersion.left().value();

        if (MapUtils.isEmpty(service.getDeploymentArtifacts()) && MapUtils.isEmpty(service.getToscaArtifacts())) {
            log.debug("Neither Deployment nor Tosca artifacts of service {} are found", serviceId);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, normalizedArtifactName));
        }

        Optional<ArtifactDefinition> foundArtifactOptl = null;

        if (!MapUtils.isEmpty(service.getDeploymentArtifacts())) {
            foundArtifactOptl = service.getDeploymentArtifacts().values().stream()
                                       // filters artifact by name
                                       .filter(a -> a.getArtifactName().equals(normalizedArtifactName)).findAny();
        }
        if ((foundArtifactOptl == null || !foundArtifactOptl.isPresent()) && !MapUtils.isEmpty(service.getToscaArtifacts())) {
            foundArtifactOptl = service.getToscaArtifacts().values().stream()
                                       // filters TOSCA artifact by name
                                       .filter(a -> a.getArtifactName().equals(normalizedArtifactName)).findAny();
        }
        if (!foundArtifactOptl.isPresent()) {
            log.debug("The artifact {} was not found for service {}", normalizedArtifactName, serviceId);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, normalizedArtifactName));
        }
        log.debug(FOUND_DEPLOYMENT_ARTIFACT, normalizedArtifactName);
        // Downloading the artifact
        Either<ImmutablePair<String, byte[]>, ResponseFormat> downloadArtifactEither = downloadArtifact(foundArtifactOptl
                .get());
        if (downloadArtifactEither.isRight()) {
            log.debug(FAILED_DOWNLOAD_ARTIFACT, normalizedArtifactName);
            return Either.right(downloadArtifactEither.right().value());
        }
        log.trace("Download of service artifact succeeded, uniqueId {}", foundArtifactOptl.get().getUniqueId());
        return Either.left(downloadArtifactEither.left().value().getRight());
    }

    public Either<ImmutablePair<String, byte[]>, ResponseFormat> downloadArtifact(String parentId, String artifactUniqueId) {
        log.trace("Starting download of artifact, uniqueId {}", artifactUniqueId);
        Either<ArtifactDefinition, StorageOperationStatus> artifactById = artifactToscaOperation.getArtifactById(parentId, artifactUniqueId);
        if (artifactById.isRight()) {
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(artifactById.right().value());
            log.debug("Error when getting artifact info by id{}, error: {}", artifactUniqueId, actionStatus);
            return Either.right(componentsUtils.getResponseFormatByArtifactId(actionStatus, ""));
        }
        ArtifactDefinition artifactDefinition = artifactById.left().value();
        if (artifactDefinition == null) {
            log.debug("Empty artifact definition returned from DB by artifact id {}", artifactUniqueId);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, ""));
        }

        return downloadArtifact(artifactDefinition);
    }

    private boolean checkArtifactInComponent(org.openecomp.sdc.be.model.Component component, String artifactId) {
        boolean found = false;
        Map<String, ArtifactDefinition> artifactsS = component.getArtifacts();
        if (artifactsS != null) {
            for (Map.Entry<String, ArtifactDefinition> entry : artifactsS.entrySet()) {
                if (entry.getValue().getUniqueId().equals(artifactId)) {
                    found = true;
                    break;
                }
            }
        }
        Map<String, ArtifactDefinition> deploymentArtifactsS = component.getDeploymentArtifacts();
        if (!found && deploymentArtifactsS != null) {
            for (Map.Entry<String, ArtifactDefinition> entry : deploymentArtifactsS.entrySet()) {
                if (entry.getValue().getUniqueId().equals(artifactId)) {
                    found = true;
                    break;
                }
            }
        }
        Map<String, ArtifactDefinition> toscaArtifactsS = component.getToscaArtifacts();
        if (!found && toscaArtifactsS != null) {
            for (Map.Entry<String, ArtifactDefinition> entry : toscaArtifactsS.entrySet()) {
                if (entry.getValue().getUniqueId().equals(artifactId)) {
                    found = true;
                    break;
                }
            }
        }

        Map<String, InterfaceDefinition> interfaces = component.getInterfaces();
        if (!found && interfaces != null) {
            for (Map.Entry<String, InterfaceDefinition> entry : interfaces.entrySet()) {
                Map<String, Operation> operations = entry.getValue().getOperationsMap();
                for (Map.Entry<String, Operation> entryOp : operations.entrySet()) {
                    if (entryOp.getValue().getImplementation() != null && entryOp.getValue()
                            .getImplementation()
                            .getUniqueId()
                            .equals(artifactId)) {
                        found = true;
                        break;
                    }
                }
            }
        }

        switch (component.getComponentType()) {
            case RESOURCE:
                break;
            case SERVICE:
                Map<String, ArtifactDefinition> apiArtifacts = ((Service) component).getServiceApiArtifacts();
                if (!found && apiArtifacts != null) {
                    for (Map.Entry<String, ArtifactDefinition> entry : apiArtifacts.entrySet()) {
                        if (entry.getValue().getUniqueId().equals(artifactId)) {
                            found = true;
                            break;
                        }
                    }
                }
                break;
            default:

        }

        return found;
    }

    private boolean checkArtifactInResourceInstance(Component component, String resourceInstanceId, String artifactId) {

        boolean found = false;
        List<ComponentInstance> resourceInstances = component.getComponentInstances();
        ComponentInstance resourceInstance = null;
        for (ComponentInstance ri : resourceInstances) {
            if (ri.getUniqueId().equals(resourceInstanceId)) {
                resourceInstance = ri;
                break;
            }
        }
        if (resourceInstance != null) {
            Map<String, ArtifactDefinition> artifacts = resourceInstance.getDeploymentArtifacts();
            if (artifacts != null) {
                for (Map.Entry<String, ArtifactDefinition> entry : artifacts.entrySet()) {
                    if (entry.getValue().getUniqueId().equals(artifactId)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                artifacts = resourceInstance.getArtifacts();
                if (artifacts != null) {
                    for (Map.Entry<String, ArtifactDefinition> entry : artifacts.entrySet()) {
                        if (entry.getValue().getUniqueId().equals(artifactId)) {
                            found = true;
                            break;
                        }
                    }
                }
            }
        }
        return found;
    }

    private Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponentExists(String componentId, AuditingActionEnum auditingAction, User user, String artifactId, ComponentTypeEnum componentType,
                                                                                                           String containerComponentType) {

        ComponentTypeEnum componentForAudit = null == containerComponentType ? componentType : ComponentTypeEnum.findByParamName(containerComponentType);
        componentForAudit.getNodeType();

        Either<? extends org.openecomp.sdc.be.model.Component, StorageOperationStatus> componentResult = toscaOperationFacade
                .getToscaFullElement(componentId);

        if (componentResult.isRight()) {
            ActionStatus status = componentForAudit == ComponentTypeEnum.RESOURCE ? ActionStatus.RESOURCE_NOT_FOUND : componentType == ComponentTypeEnum.SERVICE ? ActionStatus.SERVICE_NOT_FOUND : ActionStatus.PRODUCT_NOT_FOUND;
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(status, componentId);
            log.debug("Service not found, serviceId {}", componentId);
            handleAuditing(auditingAction, null, componentId, user, null, null, artifactId, responseFormat, componentForAudit, null);
            return Either.right(responseFormat);
        }
        return Either.left(componentResult.left().value());
    }

    private Either<Boolean, ResponseFormat> validateWorkOnComponent(Component component, String userId, AuditingActionEnum auditingAction, User user, String artifactId, ArtifactOperationInfo operation) {
        if (operation.getArtifactOperationEnum() != ArtifactOperationEnum.DOWNLOAD && !operation.ignoreLifecycleState()) {
            Either<Boolean, ResponseFormat> canWork = validateCanWorkOnComponent(component, userId);
            if (canWork.isRight()) {
                String uniqueId = component.getUniqueId();
                log.debug("Service status isn't  CHECKOUT or user isn't owner, serviceId {}", uniqueId);
                handleAuditing(auditingAction, component, uniqueId, user, null, null, artifactId, canWork.right()
                                                                                                         .value(), component
                        .getComponentType(), null);
                return Either.right(canWork.right().value());
            }
        }
        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateUserRole(User user, AuditingActionEnum auditingAction, String componentId, String artifactId, ComponentTypeEnum componentType, ArtifactOperationInfo operation) {

        if (operation.getArtifactOperationEnum() != ArtifactOperationEnum.DOWNLOAD) {
            String role = user.getRole();
            if (!role.equals(Role.ADMIN.name()) && !role.equals(Role.DESIGNER.name())) {
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
                log.debug("addArtifact - user isn't permitted to perform operation, userId {}, role {}", user.getUserId(), role);
                handleAuditing(auditingAction, null, componentId, user, null, null, artifactId, responseFormat, componentType, null);
                return Either.right(responseFormat);
            }
        }
        return Either.left(true);
    }

    private Either<User, ResponseFormat> validateUserExists(String userId, AuditingActionEnum auditingAction, String componentId, String artifactId, ComponentTypeEnum componentType, boolean inTransaction) {
        User user;
        try{
            user = validateUserExists(userId, auditingAction.getName(), inTransaction);
        } catch(ComponentException e){
            user = new User();
            user.setUserId(userId);
            ResponseFormat responseFormat = e.getResponseFormat() != null ? e.getResponseFormat() :
                    componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams());
            handleAuditing(auditingAction, null, componentId, user, null, null, artifactId, responseFormat, componentType, null);
            throw e;
        }
        return Either.left(user);
    }

    protected AuditingActionEnum detectAuditingType(ArtifactOperationInfo operation, String origMd5) {
        AuditingActionEnum auditingAction = null;
        switch (operation.getArtifactOperationEnum()) {
            case CREATE:
                auditingAction = operation.isExternalApi() ? AuditingActionEnum.ARTIFACT_UPLOAD_BY_API : AuditingActionEnum.ARTIFACT_UPLOAD;
                break;
            case UPDATE:
                auditingAction = operation.isExternalApi() ? AuditingActionEnum.ARTIFACT_UPLOAD_BY_API : origMd5 == null ? AuditingActionEnum.ARTIFACT_METADATA_UPDATE : AuditingActionEnum.ARTIFACT_PAYLOAD_UPDATE;
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

    private Either<ImmutablePair<String, byte[]>, ResponseFormat> downloadArtifact(ArtifactDefinition artifactDefinition) {
        String esArtifactId = artifactDefinition.getEsId();
        Either<ESArtifactData, CassandraOperationStatus> artifactfromES = artifactCassandraDao.getArtifact(esArtifactId);
        if (artifactfromES.isRight()) {
            CassandraOperationStatus resourceUploadStatus = artifactfromES.right().value();
            StorageOperationStatus storageResponse = DaoStatusConverter.convertCassandraStatusToStorageStatus(resourceUploadStatus);
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(storageResponse);
            log.debug("Error when getting artifact from ES, error: {}", actionStatus);
            ResponseFormat responseFormat = componentsUtils.getResponseFormatByArtifactId(actionStatus, artifactDefinition
                    .getArtifactDisplayName());

            return Either.right(responseFormat);
        }

        ESArtifactData esArtifactData = artifactfromES.left().value();
        byte[] data = esArtifactData.getDataAsArray();
        if (data == null) {
            log.debug("Artifact data from ES is null");
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, artifactDefinition.getArtifactDisplayName()));
        }
        String artifactName = artifactDefinition.getArtifactName();
        log.trace("Download of artifact succeeded, uniqueId {}, artifact file name {}", artifactDefinition.getUniqueId(), artifactName);
        return Either.left(new ImmutablePair<String, byte[]>(artifactName, data));
    }

    public ESArtifactData createEsArtifactData(ArtifactDataDefinition artifactInfo, byte[] artifactPayload) {
        return new ESArtifactData(artifactInfo.getEsId(), artifactPayload);
    }

    private boolean saveArtifacts(ESArtifactData artifactData, String resourceId) {
        CassandraOperationStatus resourceUploadStatus = artifactCassandraDao.saveArtifact(artifactData);

        if (resourceUploadStatus.equals(CassandraOperationStatus.OK)) {
            log.debug("Artifact {} was saved in component .", artifactData.getId(), resourceId);
        }
        else {
            log.info("Failed to save artifact {}.", artifactData.getId());
            return false;
        }
        return true;
    }

    private boolean isArtifactMetadataUpdate(AuditingActionEnum auditingActionEnum) {
        return auditingActionEnum.equals(AuditingActionEnum.ARTIFACT_METADATA_UPDATE);
    }

    private boolean isDeploymentArtifact(ArtifactDefinition artifactInfo) {
        return ArtifactGroupTypeEnum.DEPLOYMENT.equals(artifactInfo.getArtifactGroupType());
    }

    public Either<ArtifactDefinition, ResponseFormat> createArtifactPlaceHolderInfo(String resourceId, String logicalName, Map<String, Object> artifactInfoMap, String userUserId, ArtifactGroupTypeEnum groupType, boolean inTransaction) {
        Either<User, ActionStatus> user = userAdminManager.getUser(userUserId, inTransaction);
        if (user.isRight()) {
            ResponseFormat responseFormat;
            if (user.right().value().equals(ActionStatus.USER_NOT_FOUND)) {
                log.debug("create artifact placeholder - not authorized user, userId {}", userUserId);
                responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
            }
            else {
                log.debug("create artifact placeholder - failed to authorize user, userId {}", userUserId);
                responseFormat = componentsUtils.getResponseFormat(user.right().value());
            }
            return Either.right(responseFormat);
        }

        ArtifactDefinition artifactDefinition = createArtifactPlaceHolderInfo(resourceId, logicalName, artifactInfoMap, user
                .left()
                .value(), groupType);
        return Either.left(artifactDefinition);
    }

    public ArtifactDefinition createArtifactPlaceHolderInfo(String resourceId, String logicalName, Map<String, Object> artifactInfoMap, User user, ArtifactGroupTypeEnum groupType) {
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
            uniqueId = UniqueIdBuilder.buildPropertyUniqueId(resourceId.toLowerCase(), artifactInfo.getArtifactLabel()
                                                                                                   .toLowerCase());
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

    public Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getArtifacts(String parentId, NodeTypeEnum parentType, ArtifactGroupTypeEnum groupType, String instanceId) {
        return artifactToscaOperation.getArtifacts(parentId, parentType, groupType, instanceId);
    }

    public Either<ArtifactDefinition, StorageOperationStatus> addHeatEnvArtifact(ArtifactDefinition artifactHeatEnv, ArtifactDefinition artifact, String componentId, NodeTypeEnum parentType, String instanceId) {
        return artifactToscaOperation.addHeatEnvArtifact(artifactHeatEnv, artifact, componentId, parentType, true, instanceId);
    }

    private Either<ESArtifactData, ResponseFormat> createEsHeatEnvArtifactDataFromString(ArtifactDefinition artifactDefinition, String payloadStr) {

        byte[] payload = payloadStr.getBytes();

        ESArtifactData artifactData = createEsArtifactData(artifactDefinition, payload);
        return Either.left(artifactData);
    }

    /**
     * @param artifactDefinition
     * @return
     */
    public Either<ArtifactDefinition, ResponseFormat> generateHeatEnvArtifact(ArtifactDefinition artifactDefinition, ComponentTypeEnum componentType, org.openecomp.sdc.be.model.Component component, String resourceInstanceName, User modifier,
                                                                              String instanceId, boolean shouldLock, boolean inTransaction) {
        String payload = generateHeatEnvPayload(artifactDefinition);
        String prevUUID = artifactDefinition.getArtifactUUID();
        ArtifactDefinition clonedBeforeGenerate = new ArtifactDefinition(artifactDefinition);
        return generateAndSaveHeatEnvArtifact(artifactDefinition, payload, componentType, component, resourceInstanceName, modifier, instanceId, shouldLock, inTransaction)
                .left()
                .bind(artifactDef -> updateArtifactOnGroupInstance(componentType, component, instanceId, prevUUID, clonedBeforeGenerate, artifactDef));
    }

    public Either<ArtifactDefinition, ResponseFormat> forceGenerateHeatEnvArtifact(ArtifactDefinition artifactDefinition, ComponentTypeEnum componentType, org.openecomp.sdc.be.model.Component component, String resourceInstanceName, User modifier,
                                                                                   boolean shouldLock, boolean inTransaction, String instanceId) {
        String payload = generateHeatEnvPayload(artifactDefinition);
        String prevUUID = artifactDefinition.getArtifactUUID();
        ArtifactDefinition clonedBeforeGenerate = new ArtifactDefinition(artifactDefinition);
        return forceGenerateAndSaveHeatEnvArtifact(artifactDefinition, payload, componentType, component, resourceInstanceName, modifier, instanceId, shouldLock, inTransaction)
                .left()
                .bind(artifactDef -> updateArtifactOnGroupInstance(componentType, component, instanceId, prevUUID, clonedBeforeGenerate, artifactDef));
    }

    private Either<ArtifactDefinition, ResponseFormat> updateArtifactOnGroupInstance(ComponentTypeEnum componentType, Component component, String instanceId, String prevUUID, ArtifactDefinition clonedBeforeGenerate, ArtifactDefinition updatedArtDef) {
        if (prevUUID == null || !prevUUID.equals(updatedArtDef.getArtifactUUID())) {
            List<ComponentInstance> componentInstances = component.getComponentInstances();
            if (componentInstances != null) {
                Optional<ComponentInstance> findFirst = componentInstances.stream()
                                                                          .filter(ci -> ci.getUniqueId()
                                                                                          .equals(instanceId))
                                                                          .findFirst();
                if (findFirst.isPresent()) {
                    ComponentInstance relevantInst = findFirst.get();
                    List<GroupInstance> updatedGroupInstances = getUpdatedGroupInstances(updatedArtDef.getUniqueId(), clonedBeforeGenerate, relevantInst
                            .getGroupInstances());

                    if (CollectionUtils.isNotEmpty(updatedGroupInstances)) {
                        updatedGroupInstances.forEach(gi -> {
                            gi.getGroupInstanceArtifacts().add(updatedArtDef.getUniqueId());
                            gi.getGroupInstanceArtifactsUuid().add(updatedArtDef.getArtifactUUID());
                        });
                        Either<List<GroupInstance>, StorageOperationStatus> status = toscaOperationFacade.updateGroupInstancesOnComponent(component, instanceId, updatedGroupInstances);
                        if (status.isRight()) {
                            log.debug(FAILED_UPDATE_GROUPS, component.getUniqueId());
                            ResponseFormat responseFormat = componentsUtils.getResponseFormatByArtifactId(componentsUtils
                                    .convertFromStorageResponse(status.right()
                                                                      .value()), clonedBeforeGenerate.getArtifactDisplayName());
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
            heatParameters.sort(Comparator.comparing(e -> e.getName()));

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
                            sb.append("  ")
                              .append(heatParameterDefinition.getName())
                              .append(":")
                              .append(" ")
                              .append(Boolean.parseBoolean(heatValue))
                              .append("\n");
                            break;
                        case NUMBER:
                            sb.append("  ")
                              .append(heatParameterDefinition.getName())
                              .append(":")
                              .append(" ")
                              .append(new BigDecimal(heatValue).toPlainString())
                              .append("\n");
                            break;
                        case COMMA_DELIMITED_LIST:
                        case JSON:
                            sb.append("  ")
                              .append(heatParameterDefinition.getName())
                              .append(":")
                              .append(" ")
                              .append(heatValue)
                              .append("\n");
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
                            sb.append("  ")
                              .append(heatParameterDefinition.getName())
                              .append(":")
                              .append(" ")
                              .append(value);
                            sb.append("\n");
                            break;

                    }
                }
            }
            if (!empltyHeatValues.isEmpty()) {
                empltyHeatValues.sort(Comparator.comparing(e -> e.getName()));
                empltyHeatValues.forEach(hv -> {
                    sb.append("  ").append(hv.getName()).append(":");
                    HeatParameterType type = HeatParameterType.isValidType(hv.getType());
                    if (type != null && type == HeatParameterType.STRING && (hv.getCurrentValue() != null && "".equals(hv
                            .getCurrentValue()) || hv.getDefaultValue() != null && "".equals(hv.getDefaultValue()))) {
                        sb.append(" \"\"").append("\n");
                    }
                    else {
                        sb.append(" ").append("\n");
                    }
                });
            }
        }
        sb.append(ConfigurationManager.getConfigurationManager().getConfiguration().getHeatEnvArtifactFooter());

        // DE265919 fix
        return sb.toString().replaceAll("\\\\n", "\n");
    }

    /**
     * @param artifactDefinition
     * @param payload
     * @return
     */
    public Either<ArtifactDefinition, ResponseFormat> generateAndSaveHeatEnvArtifact(ArtifactDefinition artifactDefinition, String payload, ComponentTypeEnum componentType, org.openecomp.sdc.be.model.Component component, String resourceInstanceName,
                                                                                     User modifier, String instanceId, boolean shouldLock, boolean inTransaction) {
        return generateArtifactPayload(artifactDefinition, componentType, component, resourceInstanceName, modifier, shouldLock, inTransaction, () -> artifactDefinition
                        .getHeatParamsUpdateDate(),
                () -> createEsHeatEnvArtifactDataFromString(artifactDefinition, payload), instanceId);

    }

    public Either<ArtifactDefinition, ResponseFormat> forceGenerateAndSaveHeatEnvArtifact(ArtifactDefinition artifactDefinition, String payload, ComponentTypeEnum componentType, org.openecomp.sdc.be.model.Component component, String resourceInstanceName,
                                                                                          User modifier, String instanceId, boolean shouldLock, boolean inTransaction) {
        return generateArtifactPayload(artifactDefinition, componentType, component, resourceInstanceName, modifier, shouldLock, inTransaction, System::currentTimeMillis,
                () -> createEsHeatEnvArtifactDataFromString(artifactDefinition, payload), instanceId);

    }

    protected Either<ArtifactDefinition, ResponseFormat> generateArtifactPayload(ArtifactDefinition artifactDefinition, ComponentTypeEnum componentType, org.openecomp.sdc.be.model.Component component, String resourceInstanceName, User modifier,
                                                                                 boolean shouldLock, boolean inTransaction, Supplier<Long> payloadUpdateDateGen, Supplier<Either<ESArtifactData, ResponseFormat>> esDataCreator, String instanceId) {

        log.trace("Start generating payload for {} artifact {}", artifactDefinition.getArtifactType(), artifactDefinition
                .getEsId());
        if (artifactDefinition.getPayloadUpdateDate() == null || artifactDefinition.getPayloadUpdateDate() == 0 || artifactDefinition
                .getPayloadUpdateDate() <= payloadUpdateDateGen.get()) {

            log.trace("Generating payload for {} artifact {}", artifactDefinition.getArtifactType(), artifactDefinition.getEsId());
            Either<ESArtifactData, ResponseFormat> artifactDataRes = esDataCreator.get();
            ESArtifactData artifactData = null;

            if (artifactDataRes.isLeft()) {
                artifactData = artifactDataRes.left().value();
            }
            else {
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
                handleAuditing(AuditingActionEnum.ARTIFACT_PAYLOAD_UPDATE, component, component.getUniqueId(), modifier, artifactDefinition, artifactDefinition
                                .getUniqueId(), artifactDefinition.getUniqueId(), responseFormat,
                        ComponentTypeEnum.RESOURCE_INSTANCE, resourceInstanceName);

                return Either.right(artifactDataRes.right().value());
            }
            String newCheckSum = GeneralUtility.calculateMD5Base64EncodedByByteArray(artifactData.getDataAsArray());
            String oldCheckSum;
            String esArtifactId = artifactDefinition.getEsId();
            Either<ESArtifactData, CassandraOperationStatus> artifactfromES;
            ESArtifactData esArtifactData;
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
                esArtifactData = artifactfromES.left().value();
                oldCheckSum = GeneralUtility.calculateMD5Base64EncodedByByteArray(esArtifactData.getDataAsArray());
            }
            else {
                oldCheckSum = artifactDefinition.getArtifactChecksum();

            }
            Either<ArtifactDefinition, StorageOperationStatus> updateArifactDefinitionStatus = null;

            if (shouldLock) {
                Either<Boolean, ResponseFormat> lockComponent = lockComponent(component, "Update Artifact - lock resource: ");
                if (lockComponent.isRight()) {
                    handleAuditing(AuditingActionEnum.ARTIFACT_METADATA_UPDATE, component, component.getUniqueId(), modifier, null, null, artifactDefinition
                            .getUniqueId(), lockComponent.right().value(), component.getComponentType(), null);
                    return Either.right(lockComponent.right().value());
                }
            }
            try {
                if (oldCheckSum != null && oldCheckSum.equals(newCheckSum)) {

                    artifactDefinition.setPayloadUpdateDate(payloadUpdateDateGen.get());
                    updateArifactDefinitionStatus = artifactToscaOperation.updateArtifactOnResource(artifactDefinition, component
                            .getUniqueId(), artifactDefinition.getUniqueId(), componentType.getNodeType(), instanceId);
                    log.trace("No real update done in payload for {} artifact, updating payloadUpdateDate {}", artifactDefinition
                            .getArtifactType(), artifactDefinition.getEsId());
                    if (updateArifactDefinitionStatus.isRight()) {
                        ResponseFormat responseFormat = componentsUtils.getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(updateArifactDefinitionStatus
                                .right()
                                .value()), artifactDefinition.getArtifactDisplayName());
                        log.trace("Failed to update payloadUpdateDate {}", artifactDefinition.getEsId());
                        handleAuditing(AuditingActionEnum.ARTIFACT_PAYLOAD_UPDATE, component, component.getUniqueId(), modifier, artifactDefinition, artifactDefinition
                                        .getUniqueId(), artifactDefinition.getUniqueId(), responseFormat,
                                ComponentTypeEnum.RESOURCE_INSTANCE, resourceInstanceName);

                        return Either.right(responseFormat);
                    }
                }
                else {

                    oldCheckSum = artifactDefinition.getArtifactChecksum();
                    artifactDefinition.setArtifactChecksum(newCheckSum);
                    artifactDefinition.setEsId(artifactDefinition.getUniqueId());
                    log.trace("No real update done in payload for {} artifact, updating payloadUpdateDate {}", artifactDefinition
                            .getArtifactType(), artifactDefinition.getEsId());
                    updateArifactDefinitionStatus = artifactToscaOperation.updateArtifactOnResource(artifactDefinition, component
                            .getUniqueId(), artifactDefinition.getUniqueId(), componentType.getNodeType(), instanceId);

                    log.trace("Update Payload  ", artifactDefinition.getEsId());
                }
                if (updateArifactDefinitionStatus != null && updateArifactDefinitionStatus.isLeft()) {

                    artifactDefinition = updateArifactDefinitionStatus.left().value();
                    artifactData.setId(artifactDefinition.getUniqueId());
                    CassandraOperationStatus saveArtifactStatus = artifactCassandraDao.saveArtifact(artifactData);

                    if (saveArtifactStatus.equals(CassandraOperationStatus.OK)) {
                        if (!inTransaction) {
                            titanDao.commit();
                        }
                        log.debug("Artifact Saved In ES {}", artifactData.getId());
                        ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
                        handleAuditing(AuditingActionEnum.ARTIFACT_PAYLOAD_UPDATE, component, component.getUniqueId(), modifier, artifactDefinition, artifactDefinition
                                        .getUniqueId(), artifactDefinition.getUniqueId(), responseFormat,
                                ComponentTypeEnum.RESOURCE_INSTANCE, resourceInstanceName);

                    }
                    else {
                        if (!inTransaction) {
                            titanDao.rollback();
                        }
                        log.info("Failed to save artifact {}.", artifactData.getId());
                        ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
                        handleAuditing(AuditingActionEnum.ARTIFACT_PAYLOAD_UPDATE, component, component.getUniqueId(), modifier, artifactDefinition, artifactDefinition
                                        .getUniqueId(), artifactDefinition.getUniqueId(), responseFormat,
                                ComponentTypeEnum.RESOURCE_INSTANCE, resourceInstanceName);

                        return Either.right(responseFormat);
                    }
                }
                else {
                    ResponseFormat responseFormat = componentsUtils.getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(updateArifactDefinitionStatus
                            .right()
                            .value()), artifactDefinition.getArtifactDisplayName());
                    log.debug("Failed To update artifact {}", artifactData.getId());
                    handleAuditing(AuditingActionEnum.ARTIFACT_PAYLOAD_UPDATE, component, component.getUniqueId(), modifier, artifactDefinition, artifactDefinition
                                    .getUniqueId(), artifactDefinition.getUniqueId(), responseFormat,
                            ComponentTypeEnum.RESOURCE_INSTANCE, resourceInstanceName);

                    return Either.right(responseFormat);

                }
            }
            finally {
                if (shouldLock) {
                    graphLockOperation.unlockComponent(component.getUniqueId(), component.getComponentType()
                                                                                         .getNodeType());
                }
            }
        }

        return Either.left(artifactDefinition);
    }


    public Map<String, Object> buildJsonForUpdateArtifact(ArtifactDefinition artifactDef, ArtifactGroupTypeEnum artifactGroupType, List<ArtifactTemplateInfo> updatedRequiredArtifacts) {
        return this.buildJsonForUpdateArtifact(artifactDef.getUniqueId(), artifactDef.getArtifactName(), artifactDef.getArtifactType(), artifactGroupType, artifactDef
                        .getArtifactLabel(), artifactDef.getArtifactDisplayName(),
                artifactDef.getDescription(), artifactDef.getPayloadData(), updatedRequiredArtifacts, artifactDef.getListHeatParameters());

    }

    public Map<String, Object> buildJsonForUpdateArtifact(String artifactId, String artifactName, String artifactType, ArtifactGroupTypeEnum artifactGroupType, String label, String displayName, String description, byte[] artifactContent,
                                                          List<ArtifactTemplateInfo> updatedRequiredArtifacts, List<HeatParameterDefinition> heatParameters) {

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
                : updatedRequiredArtifacts.stream()
                                          .filter(e -> e.getType().equals(ArtifactTypeEnum.HEAT_ARTIFACT.getType()) || e
                                                  .getType()
                                                  .equals(ArtifactTypeEnum.HEAT_NESTED.getType()))
                                          .map(e -> e.getFileName())
                                          .collect(Collectors.toList()));
        json.put(Constants.ARTIFACT_HEAT_PARAMS, (heatParameters == null || heatParameters.isEmpty()) ? new ArrayList<>()
                : heatParameters);
        return json;
    }

    public Either<Either<ArtifactDefinition, Operation>, ResponseFormat> updateResourceInstanceArtifactNoContent(String resourceId, Component containerComponent, User user, Map<String, Object> json, ArtifactOperationInfo operation, ArtifactDefinition artifactInfo) {

        String jsonStr = gson.toJson(json);
        ArtifactDefinition artifactDefinitionFromJson = artifactInfo == null ? RepresentationUtils.convertJsonToArtifactDefinition(jsonStr, ArtifactDefinition.class) : artifactInfo;
        String artifactUniqueId = artifactDefinitionFromJson == null ? null : artifactDefinitionFromJson.getUniqueId();
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> uploadArtifactToService = validateAndHandleArtifact(resourceId, ComponentTypeEnum.RESOURCE_INSTANCE, operation, artifactUniqueId,
                artifactDefinitionFromJson, null, jsonStr, null, null, user, containerComponent, false, false, true);
        if (uploadArtifactToService.isRight()) {
            return Either.right(uploadArtifactToService.right().value());
        }

        return Either.left(uploadArtifactToService.left().value());
    }

    private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleUpdateHeatEnv(String componentId, ArtifactDefinition artifactInfo, AuditingActionEnum auditingAction, String artifactId, User user, ComponentTypeEnum componentType,
                                                                                              org.openecomp.sdc.be.model.Component parent, String originData, String origMd5, ArtifactOperationInfo operation, boolean shouldLock, boolean inTransaction) {
        convertParentType(componentType);
        String parentId = parent.getUniqueId();
        ArtifactDefinition currArtifact = artifactInfo;

        if (origMd5 != null) {
            Either<Boolean, ResponseFormat> validateMd5 = validateMd5(origMd5, originData, artifactInfo.getPayloadData(), operation);
            if (validateMd5.isRight()) {
                ResponseFormat responseFormat = validateMd5.right().value();
                handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
                return Either.right(responseFormat);
            }

            if (artifactInfo.getPayloadData() != null && artifactInfo.getPayloadData().length != 0) {
                Either<Boolean, ResponseFormat> deploymentValidationResult = validateDeploymentArtifact(parent, componentId, false, artifactInfo, currArtifact, NodeTypeEnum.ResourceInstance);
                if (deploymentValidationResult.isRight()) {
                    ResponseFormat responseFormat = deploymentValidationResult.right().value();
                    handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
                    return Either.right(responseFormat);
                }

                Either<byte[], ResponseFormat> payloadEither = handlePayload(artifactInfo, isArtifactMetadataUpdate(auditingAction));
                if (payloadEither.isRight()) {
                    ResponseFormat responseFormat = payloadEither.right().value();
                    handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
                    return Either.right(responseFormat);
                }
            }
            else { // duplicate
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, ARTIFACT_PAYLOAD);
                handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, responseFormat, componentType, null);
                return Either.right(responseFormat);
            }
        }

        // lock resource
        if (shouldLock) {
            Either<Boolean, ResponseFormat> lockComponent = lockComponent(parent, UPDATE_ARTIFACT_LOCK);
            if (lockComponent.isRight()) {
                handleAuditing(auditingAction, parent, parentId, user, null, null, artifactId, lockComponent.right()
                                                                                                            .value(), componentType, null);
                return Either.right(lockComponent.right().value());
            }
        }
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> resultOp = null;
        try {
            resultOp = updateHeatEnvParams(componentId, artifactId, artifactInfo, user, auditingAction, parent, componentType, origMd5);
            return resultOp;

        }
        finally {
            // unlock resource
            if (resultOp == null || resultOp.isRight()) {
                log.debug(ROLLBACK);
                if (!inTransaction) {
                    titanDao.rollback();
                }
            }
            else {
                log.debug(COMMIT);
                if (!inTransaction) {
                    titanDao.commit();
                }
            }
            if (shouldLock) {
                componentType = parent.getComponentType();
            }
            NodeTypeEnum nodeType = componentType.getNodeType();
            graphLockOperation.unlockComponent(parent.getUniqueId(), nodeType);
        }
    }

    private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> updateHeatEnvParams(String componentId, String artifactId, ArtifactDefinition artifactInfo, User user, AuditingActionEnum auditingAction, Component parent,
                                                                                              ComponentTypeEnum componentType, String origMd5) {

        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> resultOp = null;
        Either<ArtifactDefinition, Operation> insideEither = null;
        Either<ComponentInstance, ResponseFormat> getRI = getRIFromComponent(parent, componentId, artifactId, auditingAction, user);
        if (getRI.isRight()) {
            return Either.right(getRI.right().value());
        }
        ComponentInstance ri = getRI.left().value();
        Either<ArtifactDefinition, ResponseFormat> getArtifactRes = getArtifactFromRI(parent, ri, componentId, artifactId, auditingAction, user);
        if (getArtifactRes.isRight()) {
            return Either.right(getArtifactRes.right().value());
        }
        ArtifactDefinition currArtifact = getArtifactRes.left().value();

        if (currArtifact.getArtifactType().equals(ArtifactTypeEnum.HEAT.getType()) || currArtifact.getArtifactType()
                                                                                                  .equals(ArtifactTypeEnum.HEAT_VOL
                                                                                                          .getType()) || currArtifact
                .getArtifactType()
                .equals(ArtifactTypeEnum.HEAT_NET.getType())) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
            handleAuditing(auditingAction, parent, parent.getUniqueId(), user, artifactInfo, null, artifactId, responseFormat, componentType, ri
                    .getName());
            return Either.right(responseFormat);
        }
        List<HeatParameterDefinition> currentHeatEnvParams = currArtifact.getListHeatParameters();
        List<HeatParameterDefinition> updatedHeatEnvParams = artifactInfo.getListHeatParameters();
        new ArrayList<HeatParameterDefinition>();

        // upload
        if (origMd5 != null) {
            Either<List<HeatParameterDefinition>, ResponseFormat> uploadParamsValidationResult = validateUploadParamsFromEnvFile(auditingAction, parent, user, artifactInfo, artifactId, componentType, ri
                            .getName(), currentHeatEnvParams,
                    updatedHeatEnvParams, currArtifact.getArtifactName());
            if (uploadParamsValidationResult.isRight()) {
                ResponseFormat responseFormat = uploadParamsValidationResult.right().value();
                handleAuditing(auditingAction, parent, parent.getUniqueId(), user, artifactInfo, null, artifactId, responseFormat, componentType, ri
                        .getName());
                return Either.right(responseFormat);
            }
            artifactInfo.setListHeatParameters(updatedHeatEnvParams);
        }

        Either<ArtifactDefinition, ResponseFormat> validateAndConvertHeatParamers = validateAndConvertHeatParamers(artifactInfo, ArtifactTypeEnum.HEAT_ENV
                .getType());
        if (validateAndConvertHeatParamers.isRight()) {
            ResponseFormat responseFormat = validateAndConvertHeatParamers.right().value();
            handleAuditing(auditingAction, parent, parent.getUniqueId(), user, artifactInfo, null, artifactId, responseFormat, componentType, ri
                    .getName());
            return Either.right(responseFormat);
        }

        if (updatedHeatEnvParams != null && !updatedHeatEnvParams.isEmpty()) {
            // fill reduced heat env parameters List for updating
            replaceCurrHeatValueWithUpdatedValue(currentHeatEnvParams, updatedHeatEnvParams);
            currArtifact.setHeatParamsUpdateDate(System.currentTimeMillis());
            currArtifact.setListHeatParameters(currentHeatEnvParams);

            Either<ArtifactDefinition, StorageOperationStatus> updateArifactRes = artifactToscaOperation.updateArtifactOnResource(currArtifact, parent
                    .getUniqueId(), currArtifact.getUniqueId(), componentType.getNodeType(), componentId);
            if (updateArifactRes.isRight()) {
                log.debug("Failed to update artifact on graph  - {}", artifactId);
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(updateArifactRes
                        .right()
                        .value()));
                handleAuditing(auditingAction, parent, parent.getUniqueId(), user, artifactInfo, null, artifactId, responseFormat, componentType, ri
                        .getName());
                return Either.right(responseFormat);
            }
            StorageOperationStatus error = generateCustomizationUUIDOnInstance(parent.getUniqueId(), ri.getUniqueId(), componentType);
            if (error != StorageOperationStatus.OK) {
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(error));
                handleAuditing(auditingAction, parent, parent.getUniqueId(), user, artifactInfo, null, artifactId, responseFormat, componentType, ri
                        .getName());
                return Either.right(responseFormat);
            }

            error = generateCustomizationUUIDOnGroupInstance(ri, updateArifactRes.left()
                                                                                 .value()
                                                                                 .getUniqueId(), parent.getUniqueId());
            if (error != StorageOperationStatus.OK) {
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(error));
                handleAuditing(auditingAction, parent, parent.getUniqueId(), user, artifactInfo, null, artifactId, responseFormat, componentType, ri
                        .getName());
                return Either.right(responseFormat);
            }

        }
        insideEither = Either.left(currArtifact);
        resultOp = Either.left(insideEither);
        ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
        handleAuditing(auditingAction, parent, parent.getUniqueId(), user, currArtifact, null, artifactId, responseFormat, componentType, ri
                .getName());
        return resultOp;
    }

    private void replaceCurrHeatValueWithUpdatedValue(List<HeatParameterDefinition> currentHeatEnvParams, List<HeatParameterDefinition> updatedHeatEnvParams) {
        for (HeatParameterDefinition heatEnvParam : updatedHeatEnvParams) {
            String paramName = heatEnvParam.getName();
            for (HeatParameterDefinition currHeatParam : currentHeatEnvParams) {
                if (paramName.equalsIgnoreCase(currHeatParam.getName())) {
                    String updatedParamValue = heatEnvParam.getCurrentValue();
                    currHeatParam.setCurrentValue(updatedParamValue);
                }
            }
        }
    }

    private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> updateHeatParams(String componentId, String artifactId, ArtifactDefinition artifactEnvInfo, User user, AuditingActionEnum auditingAction, Component parent,
                                                                                           ComponentTypeEnum componentType, ArtifactDefinition currHeatArtifact, boolean needToUpdateGroup) {

        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> resultOp;
        Either<ArtifactDefinition, Operation> insideEither = null;
        String currentHeatId = currHeatArtifact.getUniqueId();

        String esArtifactId = currHeatArtifact.getEsId();
        Either<ESArtifactData, CassandraOperationStatus> artifactFromES = artifactCassandraDao.getArtifact(esArtifactId);
        if (artifactFromES.isRight()) {
            CassandraOperationStatus resourceUploadStatus = artifactFromES.right().value();
            StorageOperationStatus storageResponse = DaoStatusConverter.convertCassandraStatusToStorageStatus(resourceUploadStatus);
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(storageResponse);
            log.debug("Error when getting artifact from ES, error: {}", actionStatus);
            return Either.right(componentsUtils.getResponseFormatByArtifactId(actionStatus, currHeatArtifact.getArtifactDisplayName()));
        }

        ESArtifactData esArtifactData = artifactFromES.left().value();

        ArtifactDefinition updatedHeatArt = currHeatArtifact;

        List<HeatParameterDefinition> updatedHeatEnvParams = artifactEnvInfo.getListHeatParameters();
        List<HeatParameterDefinition> currentHeatEnvParams = currHeatArtifact.getListHeatParameters();
        List<HeatParameterDefinition> newHeatEnvParams = new ArrayList<HeatParameterDefinition>();

        if (updatedHeatEnvParams != null && !updatedHeatEnvParams.isEmpty() && currentHeatEnvParams != null && !currentHeatEnvParams
                .isEmpty()) {

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
                            ActionStatus status = ActionStatus.INVALID_HEAT_PARAMETER_VALUE;
                            ResponseFormat responseFormat = componentsUtils.getResponseFormat(status, ArtifactTypeEnum.HEAT_ENV
                                    .getType(), paramType.getType(), paramName);
                            handleAuditing(auditingAction, parent, parent.getUniqueId(), user, artifactEnvInfo, null, artifactId, responseFormat, componentType, "");
                            return Either.right(responseFormat);

                        }
                        currHeatParam.setCurrentValue(paramType.getConverter().convert(updatedParamValue, null, null));
                        newHeatEnvParams.add(currHeatParam);
                        break;
                    }
                }
            }
            if (!newHeatEnvParams.isEmpty()) {
                currHeatArtifact.setListHeatParameters(currentHeatEnvParams);
                Either<ArtifactDefinition, StorageOperationStatus> operationStatus = artifactToscaOperation.updateArtifactOnResource(currHeatArtifact, parent
                        .getUniqueId(), currHeatArtifact.getUniqueId(), componentType.getNodeType(), componentId);

                if (operationStatus.isRight()) {
                    log.debug("Failed to update artifact on graph  - {}", currHeatArtifact.getUniqueId());

                    ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(operationStatus
                            .right()
                            .value()));
                    return Either.right(responseFormat);

                }
                updatedHeatArt = operationStatus.left().value();
                boolean res = true;
                if (!updatedHeatArt.getDuplicated() || esArtifactData.getId() == null) {
                    esArtifactData.setId(updatedHeatArt.getEsId());
                }
                res = saveArtifacts(esArtifactData, parent.getUniqueId());

                if (res) {
                    log.debug(ARTIFACT_SAVED, updatedHeatArt.getUniqueId());
                    ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
                    handleAuditing(auditingAction, parent, parent.getUniqueId(), user, updatedHeatArt, currentHeatId, updatedHeatArt
                            .getUniqueId(), responseFormat, componentType, null);
                }
                else {
                    BeEcompErrorManager.getInstance().logBeDaoSystemError(UPDATE_ARTIFACT);
                    log.debug(FAILED_SAVE_ARTIFACT);
                    ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
                    handleAuditing(auditingAction, parent, parent.getUniqueId(), user, updatedHeatArt, currentHeatId, updatedHeatArt
                            .getUniqueId(), responseFormat, componentType, null);
                    resultOp = Either.right(responseFormat);
                }

                insideEither = Either.left(updatedHeatArt);
            }
        }
        Either<ArtifactDefinition, StorageOperationStatus> updateHeatEnvArtifact;
        if (!currentHeatId.equals(updatedHeatArt.getUniqueId())) {
            artifactEnvInfo.setArtifactChecksum(null);
            updateHeatEnvArtifact = artifactToscaOperation.updateHeatEnvArtifact(parent.getUniqueId(), artifactEnvInfo, currentHeatId, updatedHeatArt
                    .getUniqueId(), componentType.getNodeType(), componentId);
        }
        else {
            updateHeatEnvArtifact = artifactToscaOperation.updateHeatEnvPlaceholder(artifactEnvInfo, componentId, componentType
                    .getNodeType());

        }
        if (needToUpdateGroup && updateHeatEnvArtifact.isLeft()) {
            ActionStatus result = updateGroupForHeat(currHeatArtifact, updatedHeatArt, artifactEnvInfo, updateHeatEnvArtifact
                    .left()
                    .value(), parent, componentType);
            if (result != ActionStatus.OK) {
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(result);
                return Either.right(responseFormat);
            }
        }

        if (updatedHeatEnvParams.isEmpty()) {
            return getResponseAndAuditInvalidEmptyHeatEnvFile(auditingAction, parent, user, currHeatArtifact, artifactId, componentType);
        }
        resultOp = Either.left(insideEither);
        ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
        handleAuditing(auditingAction, parent, parent.getUniqueId(), user, currHeatArtifact, null, artifactId, responseFormat, componentType, "");
        return resultOp;

    }

    private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> getResponseAndAuditInvalidEmptyHeatEnvFile(AuditingActionEnum auditingAction, Component parent, User user, ArtifactDefinition currHeatArtifact, String artifactId, ComponentTypeEnum componentType) {
        ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_YAML, currHeatArtifact.getArtifactName());
        handleAuditing(auditingAction, parent, parent.getUniqueId(), user, currHeatArtifact, null, artifactId, responseFormat, componentType, "");
        return Either.right(responseFormat);
    }


    private StorageOperationStatus generateCustomizationUUIDOnGroupInstance(ComponentInstance ri, String artifactId, String componentId) {
        StorageOperationStatus error = StorageOperationStatus.OK;
        log.debug("Need to re-generate  customization UUID for group instance on component instance  {}", ri.getUniqueId());
        List<GroupInstance> groupsInstances = ri.getGroupInstances();
        List<String> groupInstancesId = null;
        if (groupsInstances != null && !groupsInstances.isEmpty()) {
            groupInstancesId = groupsInstances.stream()
                                              .filter(p -> p.getGroupInstanceArtifacts() != null && p.getGroupInstanceArtifacts()
                                                                                                     .contains(artifactId))
                                              .map(GroupInstanceDataDefinition::getUniqueId)
                                              .collect(Collectors.toList());
        }
        if (groupInstancesId != null && !groupInstancesId.isEmpty()) {
            toscaOperationFacade.generateCustomizationUUIDOnInstanceGroup(componentId, ri.getUniqueId(), groupInstancesId);
        }
        return error;

    }

    public Either<List<HeatParameterDefinition>, ResponseFormat> validateUploadParamsFromEnvFile(AuditingActionEnum auditingAction, Component parent, User user, ArtifactDefinition artifactInfo, String artifactId, ComponentTypeEnum componentType,
                                                                                                 String riName, List<HeatParameterDefinition> currentHeatEnvParams, List<HeatParameterDefinition> updatedHeatEnvParams, String currArtifactName) {

        if (updatedHeatEnvParams == null || updatedHeatEnvParams.isEmpty()) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_DEPLOYMENT_ARTIFACT_HEAT, artifactInfo
                    .getArtifactName(), currArtifactName);
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
                handleAuditing(auditingAction, parent, parent.getUniqueId(), user, artifactInfo, null, artifactId, responseFormat, componentType, riName);
                return Either.right(responseFormat);
            }
        }
        return Either.left(updatedHeatEnvParams);
    }

    private Either<ComponentInstance, ResponseFormat> getRIFromComponent(Component component, String riID, String artifactId, AuditingActionEnum auditingAction, User user) {
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

    private Either<ArtifactDefinition, ResponseFormat> getArtifactFromRI(Component component, ComponentInstance ri, String riID, String artifactId, AuditingActionEnum auditingAction, User user) {
        ResponseFormat responseFormat = null;
        Map<String, ArtifactDefinition> rtifactsMap = ri.getDeploymentArtifacts();
        for (ArtifactDefinition artifact : rtifactsMap.values()) {
            if (artifactId.equals(artifact.getUniqueId())) {
                return Either.left(artifact);
            }
        }
        responseFormat = componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, riID, component.getUniqueId());
        handleAuditing(auditingAction, component, riID, user, null, null, artifactId, responseFormat, ComponentTypeEnum.RESOURCE_INSTANCE, ri
                .getName());
        return Either.right(responseFormat);
    }

    public ArtifactDefinition extractArtifactDefinition(Either<ArtifactDefinition, Operation> eitherArtifact) {
        ArtifactDefinition ret;
        if (eitherArtifact.isLeft()) {
            ret = eitherArtifact.left().value();
        }
        else {
            ret = eitherArtifact.right().value().getImplementationArtifact();
        }
        return ret;
    }

    /**
     * downloads artifact of component by UUIDs
     *
     * @param componentType
     * @param componentUuid
     * @param artifactUUID
     * @param resourceCommonInfo
     * @return
     */
    public Either<byte[], ResponseFormat> downloadComponentArtifactByUUIDs(ComponentTypeEnum componentType, String componentUuid, String artifactUUID, ResourceCommonInfo resourceCommonInfo) {
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        Either<byte[], ResponseFormat> result;
        byte[] downloadedArtifact = null;
        Component component = getComponentByUuid(componentType, componentUuid, errorWrapper);
        if (errorWrapper.isEmpty() && component != null) {
            resourceCommonInfo.setResourceName(component.getName());
            downloadedArtifact = downloadArtifact(component.getAllArtifacts(), artifactUUID, errorWrapper, component.getName());
        }
        if (errorWrapper.isEmpty()) {
            result = Either.left(downloadedArtifact);
        }
        else {
            result = Either.right(errorWrapper.getInnerElement());
        }
        return result;
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
    public Either<byte[], ResponseFormat> downloadResourceInstanceArtifactByUUIDs(ComponentTypeEnum componentType, String componentUuid, String resourceInstanceName, String artifactUUID) {
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        Either<byte[], ResponseFormat> result;
        byte[] downloadedArtifact = null;
        ComponentInstance resourceInstance = getRelatedComponentInstance(componentType, componentUuid, resourceInstanceName, errorWrapper);
        if (errorWrapper.isEmpty()) {
            downloadedArtifact = downloadArtifact(resourceInstance.getDeploymentArtifacts(), artifactUUID, errorWrapper, resourceInstance
                    .getName());
        }
        if (errorWrapper.isEmpty()) {
            result = Either.left(downloadedArtifact);
        }
        else {
            result = Either.right(errorWrapper.getInnerElement());
        }
        return result;
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
    public Either<ArtifactDefinition, ResponseFormat> uploadArtifactToComponentByUUID(String data, HttpServletRequest request, ComponentTypeEnum componentType, String componentUuid, ResourceCommonInfo resourceCommonInfo,ArtifactOperationInfo operation) {
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> actionResult = null;
        Component component = null;
        String componentId = null;
        ArtifactDefinition artifactInfo = RepresentationUtils.convertJsonToArtifactDefinition(data, ArtifactDefinition.class);
        String origMd5 = request.getHeader(Constants.MD5_HEADER);
        String userId = request.getHeader(Constants.USER_ID_HEADER);

        Either<ComponentMetadataData, StorageOperationStatus> getComponentRes = toscaOperationFacade.getLatestComponentMetadataByUuid(componentUuid, JsonParseFlagEnum.ParseMetadata, true);
        if (getComponentRes.isRight()) {
            StorageOperationStatus status = getComponentRes.right().value();
            log.debug(FAILED_FETCH_COMPONENT, componentType, componentUuid, status);
            errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status, componentType), componentUuid));
        }
        if (errorWrapper.isEmpty()) {
            componentId = getComponentRes.left().value().getMetadataDataDefinition().getUniqueId();
            String componentName = getComponentRes.left().value().getMetadataDataDefinition().getName();

            if (!getComponentRes.left()
                    .value()
                    .getMetadataDataDefinition()
                    .getState()
                    .equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
                component = checkoutParentComponent(componentType, componentId, userId, errorWrapper);
                if (component != null) {
                    componentId = component.getUniqueId();
                    componentName = component.getName();
                }
            }
            resourceCommonInfo.setResourceName(componentName);
        }
        if (errorWrapper.isEmpty()) {
            actionResult = handleArtifactRequest(componentId, userId, componentType, operation, null, artifactInfo, origMd5, data, null, null, null, null);
            if (actionResult.isRight()) {
                log.debug(FAILED_UPLOAD_ARTIFACT_TO_COMPONENT, componentType, componentUuid, actionResult
                        .right()
                        .value());
                return Either.right(actionResult.right().value());
            }
            return Either.left(actionResult.left().value().left().value());
        }
        return Either.right(errorWrapper.getInnerElement());
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
    public Either<ArtifactDefinition, ResponseFormat> uploadArtifactToRiByUUID(String data, HttpServletRequest request, ComponentTypeEnum componentType, String componentUuid, String resourceInstanceName,
                                                                                ArtifactOperationInfo operation) {
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        Either<ArtifactDefinition, ResponseFormat> uploadArtifactResult;
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> actionResult = null;
        ArtifactDefinition uploadArtifact = null;
        Component component = null;
        String componentInstanceId;
        String componentId;
        String origMd5 = request.getHeader(Constants.MD5_HEADER);
        String userId = request.getHeader(Constants.USER_ID_HEADER);

        ImmutablePair<Component, ComponentInstance> componentRiPair = null;
        Either<ComponentMetadataData, StorageOperationStatus> getComponentRes = toscaOperationFacade.getLatestComponentMetadataByUuid(componentUuid, JsonParseFlagEnum.ParseMetadata, true);
        if (getComponentRes.isRight()) {
            StorageOperationStatus status = getComponentRes.right().value();
            log.debug("Could not fetch component with type {} and uuid {}. Status is {}. ", componentType, componentUuid, status);
            errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status, componentType), resourceInstanceName));
        }
        if (errorWrapper.isEmpty() && !getComponentRes.left()
                                                      .value()
                                                      .getMetadataDataDefinition()
                                                      .getState()
                                                      .equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
            component = checkoutParentComponent(componentType, getComponentRes.left()
                                                                              .value()
                                                                              .getMetadataDataDefinition()
                                                                              .getUniqueId(), userId, errorWrapper);
        }
        if (errorWrapper.isEmpty()) {
            if (component == null) {
                componentRiPair = getRelatedComponentComponentInstance(componentType, componentUuid, resourceInstanceName, errorWrapper);
            }
            else {
                componentRiPair = getRelatedComponentComponentInstance(component, resourceInstanceName, errorWrapper);
            }
        }
        if (errorWrapper.isEmpty()) {
            componentInstanceId = componentRiPair.getRight().getUniqueId();
            componentId = componentRiPair.getLeft().getUniqueId();
            ArtifactDefinition artifactInfo = RepresentationUtils.convertJsonToArtifactDefinition(data, ArtifactDefinition.class);

            actionResult = handleArtifactRequest(componentInstanceId, userId, ComponentTypeEnum.RESOURCE_INSTANCE, operation, null, artifactInfo, origMd5, data, null, null, componentId, ComponentTypeEnum
                    .findParamByType(componentType));
            if (actionResult.isRight()) {
                log.debug(FAILED_UPLOAD_ARTIFACT_TO_INSTANCE, resourceInstanceName, componentType, componentUuid, actionResult
                        .right()
                        .value());
                errorWrapper.setInnerElement(actionResult.right().value());
            }
        }
        if (errorWrapper.isEmpty()) {
            uploadArtifact = actionResult.left().value().left().value();
            uploadArtifactResult = Either.left(uploadArtifact);
        }
        else {
            uploadArtifactResult = Either.right(errorWrapper.getInnerElement());
        }
        return uploadArtifactResult;
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
    public Either<ArtifactDefinition, ResponseFormat> updateArtifactOnComponentByUUID(String data, HttpServletRequest request, ComponentTypeEnum componentType, String componentUuid, String artifactUUID,
                                                                                      ResourceCommonInfo resourceCommonInfo, ArtifactOperationInfo operation) {
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        Either<ArtifactDefinition, ResponseFormat> updateArtifactResult;
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> actionResult = null;
        ArtifactDefinition updateArtifact = null;
        Component component = null;
        String componentId = null;
        String artifactId = null;
        ArtifactDefinition artifactInfo = RepresentationUtils.convertJsonToArtifactDefinitionForUpdate(data, ArtifactDefinition.class);
        String origMd5 = request.getHeader(Constants.MD5_HEADER);
        String userId = request.getHeader(Constants.USER_ID_HEADER);

        Either<ComponentMetadataData, StorageOperationStatus> getComponentRes = toscaOperationFacade.getLatestComponentMetadataByUuid(componentUuid, JsonParseFlagEnum.ParseMetadata, true);
        if (getComponentRes.isRight()) {
            StorageOperationStatus status = getComponentRes.right().value();
            log.debug("Could not fetch component with type {} and uuid {}. Status is {}. ", componentType, componentUuid, status);
            errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status)));
        }
        if (errorWrapper.isEmpty()) {
            componentId = getComponentRes.left().value().getMetadataDataDefinition().getUniqueId();
            String componentName = getComponentRes.left().value().getMetadataDataDefinition().getName();

            if (!getComponentRes.left()
                                .value()
                                .getMetadataDataDefinition()
                                .getState()
                                .equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
                component = checkoutParentComponent(componentType, componentId, userId, errorWrapper);
                if (component != null) {
                    componentId = component.getUniqueId();
                    componentName = component.getName();
                }
            }
            resourceCommonInfo.setResourceName(componentName);
        }
        if (errorWrapper.isEmpty()) {
            artifactId = getLatestParentArtifactDataIdByArtifactUUID(artifactUUID, errorWrapper, componentId, componentType);
        }
        if (errorWrapper.isEmpty()) {
            actionResult = handleArtifactRequest(componentId, userId, componentType, operation, artifactId, artifactInfo, origMd5, data, null, null, null, null);
            if (actionResult.isRight()) {
                log.debug(FAILED_UPLOAD_ARTIFACT_TO_COMPONENT, componentType, componentUuid, actionResult
                        .right()
                        .value());
                errorWrapper.setInnerElement(actionResult.right().value());
            }
        }
        if (errorWrapper.isEmpty()) {
            updateArtifact = actionResult.left().value().left().value();
            updateArtifactResult = Either.left(updateArtifact);

        }
        else {
            updateArtifactResult = Either.right(errorWrapper.getInnerElement());
        }
        return updateArtifactResult;
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
    public Either<ArtifactDefinition, ResponseFormat> updateArtifactOnRiByUUID(String data, HttpServletRequest request, ComponentTypeEnum componentType, String componentUuid, String resourceInstanceName, String artifactUUID,
                                                                                ArtifactOperationInfo operation) {

        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        Either<ArtifactDefinition, ResponseFormat> updateArtifactResult;
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> actionResult = null;
        ArtifactDefinition updateArtifact = null;
        Component component = null;
        String componentInstanceId = null;
        String componentId = null;
        String artifactId = null;
        String origMd5 = request.getHeader(Constants.MD5_HEADER);
        String userId = request.getHeader(Constants.USER_ID_HEADER);

        ImmutablePair<Component, ComponentInstance> componentRiPair = null;
        Either<ComponentMetadataData, StorageOperationStatus> getComponentRes = toscaOperationFacade.getLatestComponentMetadataByUuid(componentUuid, JsonParseFlagEnum.ParseMetadata, true);
        if (getComponentRes.isRight()) {
            StorageOperationStatus status = getComponentRes.right().value();
            log.debug("Could not fetch component with type {} and uuid {}. Status is {}. ", componentType, componentUuid, status);
            errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status)));
        }
        if (errorWrapper.isEmpty() && !getComponentRes.left()
                                                      .value()
                                                      .getMetadataDataDefinition()
                                                      .getState()
                                                      .equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
            component = checkoutParentComponent(componentType, getComponentRes.left()
                                                                              .value()
                                                                              .getMetadataDataDefinition()
                                                                              .getUniqueId(), userId, errorWrapper);
        }
        if (errorWrapper.isEmpty()) {
            if (component == null) {
                componentRiPair = getRelatedComponentComponentInstance(componentType, componentUuid, resourceInstanceName, errorWrapper);
            }
            else {
                componentRiPair = getRelatedComponentComponentInstance(component, resourceInstanceName, errorWrapper);
            }
        }
        if (errorWrapper.isEmpty()) {
            componentInstanceId = componentRiPair.getRight().getUniqueId();
            componentId = componentRiPair.getLeft().getUniqueId();
            artifactId = findArtifactId(componentRiPair.getRight(), artifactUUID, errorWrapper);
        }
        if (errorWrapper.isEmpty()) {
            ArtifactDefinition artifactInfo = RepresentationUtils.convertJsonToArtifactDefinition(data, ArtifactDefinition.class);

            actionResult = handleArtifactRequest(componentInstanceId, userId, ComponentTypeEnum.RESOURCE_INSTANCE, operation, artifactId, artifactInfo, origMd5, data, null, null, componentId, ComponentTypeEnum
                    .findParamByType(componentType));
            if (actionResult.isRight()) {
                log.debug(FAILED_UPLOAD_ARTIFACT_TO_INSTANCE, resourceInstanceName, componentType, componentUuid, actionResult
                        .right()
                        .value());
                errorWrapper.setInnerElement(actionResult.right().value());
            }
        }
        if (errorWrapper.isEmpty()) {
            updateArtifact = actionResult.left().value().left().value();
            updateArtifactResult = Either.left(updateArtifact);
        }
        else {
            updateArtifactResult = Either.right(errorWrapper.getInnerElement());
        }
        return updateArtifactResult;
    }

    private Either<ArtifactDefinition, ResponseFormat> updateOperationArtifact(String componentId, String interfaceType, String operationUuid, ArtifactDefinition artifactInfo){
        Either<Component, StorageOperationStatus> componentStorageOperationStatusEither = toscaOperationFacade.getToscaElement(componentId);
        if (componentStorageOperationStatusEither.isRight()) {
            StorageOperationStatus errorStatus = componentStorageOperationStatusEither.right().value();
            log.debug("Failed to fetch resource information by resource id, error {}", errorStatus);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(errorStatus)));
        }
        Component storedComponent = componentStorageOperationStatusEither.left().value();

        Optional<InterfaceDefinition> optionalInterface = InterfaceOperationUtils.getInterfaceDefinitionFromComponentByInterfaceType(storedComponent, interfaceType);
        if(!optionalInterface.isPresent()) {
            log.debug("Failed to get resource interface for resource Id {}", componentId);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_NOT_FOUND_IN_COMPONENT, interfaceType));
        }

        //fetch the operation from storage
        InterfaceDefinition gotInterface = optionalInterface.get();
        Map<String, Operation> operationsMap = gotInterface.getOperationsMap();
        Optional<Operation> optionalOperation = operationsMap.values()
                                                        .stream()
                                                        .filter(o -> o.getUniqueId().equals(operationUuid))
                                                        .findFirst();
        if (!optionalOperation.isPresent()) {
            log.debug("Failed to get resource interface operation for resource Id {} and operationId {}", componentId, operationUuid);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND, componentId);
            return Either.right(responseFormat);
        }

        Operation operation = optionalOperation.get();
        ArtifactDefinition implementationArtifact =  operation.getImplementationArtifact();
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
        Either<List<InterfaceDefinition>, StorageOperationStatus> interfaceDefinitionStorageOperationStatusEither =
                interfaceOperation.updateInterfaces(storedComponent.getUniqueId(), Collections.singletonList(gotInterface));
        if (interfaceDefinitionStorageOperationStatusEither.isRight()){
            StorageOperationStatus storageOperationStatus = interfaceDefinitionStorageOperationStatusEither.right().value();
            ActionStatus actionStatus =
                    componentsUtils.convertFromStorageResponseForDataType(storageOperationStatus);
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
    public Either<ArtifactDefinition, ResponseFormat> updateArtifactOnInterfaceOperationByResourceUUID(
            String data, HttpServletRequest request, ComponentTypeEnum componentType,
            String componentUuid, String interfaceUUID, String operationUUID, String artifactUUID,
        ResourceCommonInfo resourceCommonInfo,ArtifactOperationInfo operation) {
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        Either<ArtifactDefinition, ResponseFormat> updateArtifactResult;
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> actionResult = null;
        ArtifactDefinition updateArtifact = null;
        String componentId = null;
        ArtifactDefinition artifactInfo = RepresentationUtils.convertJsonToArtifactDefinitionForUpdate(data, ArtifactDefinition.class);
        String origMd5 = request.getHeader(Constants.MD5_HEADER);
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        ArtifactDefinition existingArtifactInfo = null;
        String interfaceName = null;

        Either<ComponentMetadataData, StorageOperationStatus> getComponentRes = toscaOperationFacade.getLatestComponentMetadataByUuid(componentUuid, JsonParseFlagEnum.ParseMetadata, true);
        if (getComponentRes.isRight()) {
            StorageOperationStatus status = getComponentRes.right().value();
            log.debug("Could not fetch component with type {} and uuid {}. Status is {}. ", componentType, componentUuid, status);
            errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status)));
        }

        if (errorWrapper.isEmpty()) {
            componentId = getComponentRes.left().value().getMetadataDataDefinition().getUniqueId();
            String componentName = getComponentRes.left().value().getMetadataDataDefinition().getName();
            if (!getComponentRes.left()
                    .value()
                    .getMetadataDataDefinition()
                    .getState()
                    .equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
                Component component = checkoutParentComponent(componentType, componentId, userId, errorWrapper);
                if (component != null) {
                    componentId = component.getUniqueId();
                    componentName = component.getName();
                }

            }
            resourceCommonInfo.setResourceName(componentName);
        }

        if(errorWrapper.isEmpty()){
            Either<String, ResponseFormat> interfaceNameEither = fetchInterfaceName(componentId, interfaceUUID);
            if (interfaceNameEither.isRight()) {
                errorWrapper.setInnerElement(interfaceNameEither.right().value());
            }
            else {
                interfaceName = interfaceNameEither.left().value();
            }

            if(errorWrapper.isEmpty()){
                Either<Component, StorageOperationStatus> toscaComponentEither = toscaOperationFacade.getToscaElement(componentId);
                if (toscaComponentEither.isRight()) {
                    StorageOperationStatus status = toscaComponentEither.right().value();
                    log.debug("Could not fetch component with type {} and id {}. Status is {}. ", componentType, componentId, status);
                    errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status)));
                }

                if (errorWrapper.isEmpty()) {
                    NodeTypeEnum parentType = convertParentType(componentType);
                    List<ArtifactDefinition> existingDeploymentArtifacts = getDeploymentArtifacts(toscaComponentEither.left().value(), parentType,null);
                    for (ArtifactDefinition artifactDefinition: existingDeploymentArtifacts){
                        if(artifactDefinition.getArtifactName().equalsIgnoreCase(artifactInfo.getArtifactName())){
                            existingArtifactInfo = artifactDefinition;
                            break;
                        }
                    }
                    if(existingArtifactInfo != null){
                        return updateOperationArtifact(componentId, interfaceName, operationUUID, existingArtifactInfo);
                    }
                }
            }
        }

        if (errorWrapper.isEmpty()) {
            actionResult = handleArtifactRequest(componentId, userId, componentType, operation,
                    artifactUUID, artifactInfo, origMd5, data, interfaceName,
                    operationUUID, null, null);
            if (actionResult.isRight()) {
                log.debug(FAILED_UPLOAD_ARTIFACT_TO_COMPONENT, componentType, componentUuid, actionResult
                                                                                                     .right()
                                                                                                     .value());
                errorWrapper.setInnerElement(actionResult.right().value());
            }
        }

        if (errorWrapper.isEmpty()) {
            updateArtifact = actionResult.left().value().left().value();
            updateArtifactResult = Either.left(updateArtifact);

        }
        else {
            updateArtifactResult = Either.right(errorWrapper.getInnerElement());
        }
        return updateArtifactResult;
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
        if(!optionalInterface.isPresent()) {
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
     * @param operation        TODO
     * @return
     */
    public Either<ArtifactDefinition, ResponseFormat> deleteArtifactOnComponentByUUID(HttpServletRequest request, ComponentTypeEnum componentType, String componentUuid, String artifactUUID, ResourceCommonInfo resourceCommonInfo,
                                                                                      ArtifactOperationInfo operation) {

        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        Either<ArtifactDefinition, ResponseFormat> deleteArtifactResult;
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> actionResult = null;
        ArtifactDefinition deleteArtifact = null;
        Component component = null;
        String componentId = null;
        String artifactId = null;
        String origMd5 = request.getHeader(Constants.MD5_HEADER);
        String userId = request.getHeader(Constants.USER_ID_HEADER);

        Either<ComponentMetadataData, StorageOperationStatus> getComponentRes = toscaOperationFacade.getLatestComponentMetadataByUuid(componentUuid, JsonParseFlagEnum.ParseMetadata, true);
        if (getComponentRes.isRight()) {
            StorageOperationStatus status = getComponentRes.right().value();
            log.debug("Could not fetch component with type {} and uuid {}. Status is {}. ", componentType, componentUuid, status);
            errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status, componentType), componentUuid));
        }
        if (errorWrapper.isEmpty()) {
            componentId = getComponentRes.left().value().getMetadataDataDefinition().getUniqueId();
            String componentName = getComponentRes.left().value().getMetadataDataDefinition().getName();
            if (!getComponentRes.left()
                                .value()
                                .getMetadataDataDefinition()
                                .getState()
                                .equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
                component = checkoutParentComponent(componentType, componentId, userId, errorWrapper);
                if (component != null) {
                    componentId = component.getUniqueId();
                    componentName = component.getName();
                }
            }
            resourceCommonInfo.setResourceName(componentName);
        }
        if (errorWrapper.isEmpty()) {
            artifactId = getLatestParentArtifactDataIdByArtifactUUID(artifactUUID, errorWrapper, componentId, componentType);
        }
        if (errorWrapper.isEmpty()) {
            actionResult = handleArtifactRequest(componentId, userId, componentType, operation, artifactId, null, origMd5, null, null, null, null, null);
            if (actionResult.isRight()) {
                log.debug(FAILED_UPLOAD_ARTIFACT_TO_COMPONENT, componentType, componentUuid, actionResult
                        .right()
                        .value());
                errorWrapper.setInnerElement(actionResult.right().value());
            }
        }
        if (errorWrapper.isEmpty()) {
            deleteArtifact = actionResult.left().value().left().value();
            deleteArtifactResult = Either.left(deleteArtifact);
        }
        else {
            deleteArtifactResult = Either.right(errorWrapper.getInnerElement());
        }
        return deleteArtifactResult;
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
    public Either<ArtifactDefinition, ResponseFormat> deleteArtifactOnRiByUUID(HttpServletRequest request, ComponentTypeEnum componentType, String componentUuid, String resourceInstanceName, String artifactUUID,
                                                                               ArtifactOperationInfo operation) {

        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        Either<ArtifactDefinition, ResponseFormat> deleteArtifactResult;
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> actionResult = null;
        ArtifactDefinition deleteArtifact = null;
        Component component = null;
        String componentInstanceId = null;
        String componentId = null;
        String artifactId = null;
        String origMd5 = request.getHeader(Constants.MD5_HEADER);
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        ImmutablePair<Component, ComponentInstance> componentRiPair = null;
        Either<ComponentMetadataData, StorageOperationStatus> getComponentRes = toscaOperationFacade.getLatestComponentMetadataByUuid(componentUuid, JsonParseFlagEnum.ParseMetadata, true);
        if (getComponentRes.isRight()) {
            StorageOperationStatus status = getComponentRes.right().value();
            log.debug("Could not fetch component with type {} and uuid {}. Status is {}. ", componentType, componentUuid, status);
            errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status)));
        }
        if (errorWrapper.isEmpty() && !getComponentRes.left()
                                                      .value()
                                                      .getMetadataDataDefinition()
                                                      .getState()
                                                      .equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
            component = checkoutParentComponent(componentType, getComponentRes.left()
                                                                              .value()
                                                                              .getMetadataDataDefinition()
                                                                              .getUniqueId(), userId, errorWrapper);
        }
        if (errorWrapper.isEmpty()) {
            if (component == null) {
                componentRiPair = getRelatedComponentComponentInstance(componentType, componentUuid, resourceInstanceName, errorWrapper);
            }
            else {
                componentRiPair = getRelatedComponentComponentInstance(component, resourceInstanceName, errorWrapper);
            }
        }
        if (errorWrapper.isEmpty()) {
            componentInstanceId = componentRiPair.getRight().getUniqueId();
            componentId = componentRiPair.getLeft().getUniqueId();
            artifactId = findArtifactId(componentRiPair.getRight(), artifactUUID, errorWrapper);
        }
        if (errorWrapper.isEmpty()) {

            actionResult = handleArtifactRequest(componentInstanceId, userId, ComponentTypeEnum.RESOURCE_INSTANCE, operation, artifactId, null, origMd5, null, null, null, componentId, ComponentTypeEnum
                    .findParamByType(componentType));

            if (actionResult.isRight()) {
                log.debug(FAILED_UPLOAD_ARTIFACT_TO_INSTANCE, resourceInstanceName, componentType, componentUuid, actionResult
                        .right()
                        .value());
                errorWrapper.setInnerElement(actionResult.right().value());
            }
        }
        if (errorWrapper.isEmpty()) {
            deleteArtifact = actionResult.left().value().left().value();
            deleteArtifactResult = Either.left(deleteArtifact);
        }
        else {
            deleteArtifactResult = Either.right(errorWrapper.getInnerElement());
        }
        return deleteArtifactResult;
    }

    private String findArtifactId(ComponentInstance instance, String artifactUUID, Wrapper<ResponseFormat> errorWrapper) {
        String artifactId = null;
        ArtifactDefinition foundArtifact = null;
        if (instance.getDeploymentArtifacts() != null) {
            foundArtifact = instance.getDeploymentArtifacts()
                                    .values()
                                    .stream()
                                    .filter(e -> e.getArtifactUUID() != null && e.getArtifactUUID()
                                                                                 .equals(artifactUUID))
                                    .findFirst()
                                    .orElse(null);
        }
        if (foundArtifact == null && instance.getArtifacts() != null) {
            foundArtifact = instance.getArtifacts()
                                    .values()
                                    .stream()
                                    .filter(e -> e.getArtifactUUID() != null && e.getArtifactUUID()
                                                                                 .equals(artifactUUID))
                                    .findFirst()
                                    .orElse(null);
        }
        if (foundArtifact == null) {
            log.debug("The artifact {} was not found on instance {}. ", artifactUUID, instance.getUniqueId());
            errorWrapper.setInnerElement(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, artifactUUID));
        }
        else {
            artifactId = foundArtifact.getUniqueId();
        }
        return artifactId;
    }

    @SuppressWarnings("unchecked")
    public Either<ArtifactDefinition, ResponseFormat> createHeatEnvPlaceHolder(ArtifactDefinition heatArtifact, String envType, String parentId, NodeTypeEnum parentType, String parentName, User user, Component component,
                                                                               Map<String, String> existingEnvVersions) {
        Map<String, Object> deploymentResourceArtifacts = ConfigurationManager.getConfigurationManager()
                                                                              .getConfiguration()
                                                                              .getDeploymentResourceInstanceArtifacts();
        if (deploymentResourceArtifacts == null) {
            log.debug("no deployment artifacts are configured for generated artifacts");
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        Map<String, Object> placeHolderData = (Map<String, Object>) deploymentResourceArtifacts.get(envType);
        if (placeHolderData == null) {
            log.debug("no env type {} are configured for generated artifacts", envType);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

        String envLabel = (heatArtifact.getArtifactLabel() + HEAT_ENV_SUFFIX).toLowerCase();
        Either<ArtifactDefinition, ResponseFormat> createArtifactPlaceHolder = createArtifactPlaceHolderInfo(parentId, envLabel, placeHolderData, user
                .getUserId(), ArtifactGroupTypeEnum.DEPLOYMENT, true);
        if (createArtifactPlaceHolder.isRight()) {
            return Either.right(createArtifactPlaceHolder.right().value());
        }
        ArtifactDefinition artifactHeatEnv = createArtifactPlaceHolder.left().value();
        artifactHeatEnv.setGeneratedFromId(heatArtifact.getUniqueId());
        artifactHeatEnv.setHeatParamsUpdateDate(System.currentTimeMillis());
        artifactHeatEnv.setTimeout(0);
        buildHeatEnvFileName(heatArtifact, artifactHeatEnv, placeHolderData);
        // rbetzer - keep env artifactVersion - changeComponentInstanceVersion flow
        handleEnvArtifactVersion(artifactHeatEnv, existingEnvVersions);
        ArtifactDefinition heatEnvPlaceholder;
        // Evg : for resource instance artifact will be added later as block with other env artifacts from BL
        if (parentType != NodeTypeEnum.ResourceInstance) {
            Either<ArtifactDefinition, StorageOperationStatus> addHeatEnvArtifact = addHeatEnvArtifact(artifactHeatEnv, heatArtifact, component
                    .getUniqueId(), parentType, parentId);
            if (addHeatEnvArtifact.isRight()) {
                log.debug("failed to create heat env artifact on resource instance");
                return Either.right(componentsUtils.getResponseFormatForResourceInstance(componentsUtils.convertFromStorageResponseForResourceInstance(addHeatEnvArtifact
                        .right()
                        .value(), false), "", null));
            }
            heatEnvPlaceholder = createArtifactPlaceHolder.left().value();
        }
        else {
            heatEnvPlaceholder = artifactHeatEnv;
            artifactToscaOperation.generateUUID(heatEnvPlaceholder, heatEnvPlaceholder.getArtifactVersion());
            setHeatCurrentValuesOnHeatEnvDefaultValues(heatArtifact, heatEnvPlaceholder);
        }

        ComponentTypeEnum componentType = component.getComponentType();
        if (parentType == NodeTypeEnum.ResourceInstance) {
            componentType = ComponentTypeEnum.RESOURCE_INSTANCE;
        }
        componentsUtils.auditComponent(componentsUtils.getResponseFormat(ActionStatus.OK), user, component, AuditingActionEnum.ARTIFACT_UPLOAD,
                new ResourceCommonInfo(parentName, componentType.getValue()),
                ResourceVersionInfo.newBuilder().build(),
                ResourceVersionInfo.newBuilder().artifactUuid(heatEnvPlaceholder.getUniqueId()).build(),
                null, heatEnvPlaceholder, null);
        return Either.left(heatEnvPlaceholder);
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
        }
        else {
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

    /**
     * Handles Artifacts Request For Inner Component
     *
     * @param artifactsToHandle
     * @param component
     * @param user
     * @param vfcsNewCreatedArtifacts
     * @param operation
     * @param shouldLock
     * @param inTransaction
     * @return
     */
    public Either<List<ArtifactDefinition>, ResponseFormat> handleArtifactsRequestForInnerVfcComponent(List<ArtifactDefinition> artifactsToHandle, Resource component, User user, List<ArtifactDefinition> vfcsNewCreatedArtifacts,
                                                                                                       ArtifactOperationInfo operation, boolean shouldLock, boolean inTransaction) {

        Either<List<ArtifactDefinition>, ResponseFormat> handleArtifactsResult = null;
        ComponentTypeEnum componentType = component.getComponentType();
        List<ArtifactDefinition> uploadedArtifacts = new ArrayList<>();
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> actionResult;
        String originData;
        String origMd5;
        try {
            for (ArtifactDefinition artifact : artifactsToHandle) {
                originData = buildJsonStringForCsarVfcArtifact(artifact);
                origMd5 = GeneralUtility.calculateMD5Base64EncodedByString(originData);
                actionResult = handleArtifactRequest(component.getUniqueId(), user.getUserId(), componentType, operation, artifact
                        .getUniqueId(), artifact, origMd5, originData, null, null, null, null, shouldLock, inTransaction);
                if (actionResult.isRight()) {
                    log.debug("Failed to upload artifact to component with type {} and name {}. Status is {}. ", componentType, component
                            .getName(), actionResult.right().value());
                    errorWrapper.setInnerElement(actionResult.right().value());
                    if (ArtifactOperationEnum.isCreateOrLink(operation.getArtifactOperationEnum())) {
                        vfcsNewCreatedArtifacts.addAll(uploadedArtifacts);
                    }
                    break;
                }
                uploadedArtifacts.add(actionResult.left().value().left().value());
            }
            if (errorWrapper.isEmpty()) {
                handleArtifactsResult = Either.left(uploadedArtifacts);
            }
            else {
                handleArtifactsResult = Either.right(errorWrapper.getInnerElement());
            }
        }
        catch (Exception e) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            handleArtifactsResult = Either.right(responseFormat);
            log.debug("Exception occured when handleArtifactsRequestForInnerVfcComponent, error is:{}", e.getMessage(), e);
        }
        return handleArtifactsResult;
    }

    private ComponentInstance getRelatedComponentInstance(ComponentTypeEnum componentType, String componentUuid, String resourceInstanceName, Wrapper<ResponseFormat> errorWrapper) {
        ComponentInstance componentInstance = null;
        String normalizedName = ValidationUtils.normalizeComponentInstanceName(resourceInstanceName);
        Component component = getComponentByUuid(componentType, componentUuid, errorWrapper);
        if (errorWrapper.isEmpty()) {
            componentInstance = component.getComponentInstances()
                                         .stream()
                                         .filter(ci -> ValidationUtils.normalizeComponentInstanceName(ci.getName())
                                                                      .equals(normalizedName))
                                         .findFirst()
                                         .orElse(null);
            if (componentInstance == null) {
                errorWrapper.setInnerElement(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, resourceInstanceName, RESOURCE_INSTANCE, component
                        .getComponentType()
                        .getValue(), component.getName()));
                log.debug(COMPONENT_INSTANCE_NOT_FOUND, resourceInstanceName, component.getName());
            }
        }
        return componentInstance;
    }

    private ImmutablePair<Component, ComponentInstance> getRelatedComponentComponentInstance(Component component, String resourceInstanceName, Wrapper<ResponseFormat> errorWrapper) {

        ImmutablePair<Component, ComponentInstance> relatedComponentComponentInstancePair = null;
        String normalizedName = ValidationUtils.normalizeComponentInstanceName(resourceInstanceName);
        ComponentInstance componentInstance = component.getComponentInstances()
                                                       .stream()
                                                       .filter(ci -> ValidationUtils.normalizeComponentInstanceName(ci.getName())
                                                                                    .equals(normalizedName))
                                                       .findFirst()
                                                       .orElse(null);
        if (componentInstance == null) {
            errorWrapper.setInnerElement(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, resourceInstanceName, RESOURCE_INSTANCE, component
                    .getComponentType()
                    .getValue(), component.getName()));
            log.debug(COMPONENT_INSTANCE_NOT_FOUND, resourceInstanceName, component.getName());
        }
        else {
            relatedComponentComponentInstancePair = new ImmutablePair<>(component, componentInstance);
        }
        return relatedComponentComponentInstancePair;
    }

    private ImmutablePair<Component, ComponentInstance> getRelatedComponentComponentInstance(ComponentTypeEnum componentType, String componentUuid, String resourceInstanceName, Wrapper<ResponseFormat> errorWrapper) {
        ComponentInstance componentInstance;
        ImmutablePair<Component, ComponentInstance> relatedComponentComponentInstancePair = null;
        Component component = getLatestComponentByUuid(componentType, componentUuid, errorWrapper);
        if (errorWrapper.isEmpty()) {
            componentInstance = component.getComponentInstances()
                                         .stream()
                                         .filter(ci -> ci.getNormalizedName().equals(resourceInstanceName))
                                         .findFirst()
                                         .orElse(null);
            if (componentInstance == null) {
                errorWrapper.setInnerElement(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, resourceInstanceName, RESOURCE_INSTANCE, component
                        .getComponentType()
                        .getValue(), component.getName()));
                log.debug(COMPONENT_INSTANCE_NOT_FOUND, resourceInstanceName, component.getName());
            }
            else {
                relatedComponentComponentInstancePair = new ImmutablePair<>(component, componentInstance);
            }
        }
        return relatedComponentComponentInstancePair;
    }

    private byte[] downloadArtifact(Map<String, ArtifactDefinition> artifacts, String artifactUUID, Wrapper<ResponseFormat> errorWrapper, String componentName) {

        byte[] downloadedArtifact = null;
        Either<ImmutablePair<String, byte[]>, ResponseFormat> downloadArtifactEither = null;
        List<ArtifactDefinition> artifactsList = null;
        ArtifactDefinition deploymentArtifact = null;
        if (artifacts != null && !artifacts.isEmpty()) {
            artifactsList = artifacts.values()
                                     .stream()
                                     .filter(art -> art.getArtifactUUID() != null && art.getArtifactUUID()
                                                                                        .equals(artifactUUID))
                                     .collect(Collectors.toList());
        }
        if (artifactsList == null || artifactsList.isEmpty()) {
            log.debug("Deployment artifact with uuid {} was not found for component {}", artifactUUID, componentName);
            errorWrapper.setInnerElement(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, artifactUUID));
        }
        if (errorWrapper.isEmpty()) {
            deploymentArtifact = artifactsList.get(0);
            downloadArtifactEither = downloadArtifact(deploymentArtifact);
            if (downloadArtifactEither.isRight()) {
                log.debug("Failed to download artifact {}. ", deploymentArtifact.getArtifactName());
                errorWrapper.setInnerElement(downloadArtifactEither.right().value());
            }
        }
        if (errorWrapper.isEmpty()) {
            log.trace("Succeeded to download artifact with uniqueId {}", deploymentArtifact.getUniqueId());
            downloadedArtifact = downloadArtifactEither.left().value().getRight();
        }
        return downloadedArtifact;
    }

    private Component getLatestComponentByUuid(ComponentTypeEnum componentType, String componentUuid, Wrapper<ResponseFormat> errorWrapper) {
        Component component = null;
        Either<Component, StorageOperationStatus> getComponentRes = toscaOperationFacade.getLatestComponentByUuid(componentUuid);
        if (getComponentRes.isRight()) {
            StorageOperationStatus status = getComponentRes.right().value();
            log.debug("Could not fetch component with type {} and uuid {}. Status is {}. ", componentType, componentUuid, status);
            errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status)));
        }
        else {
            component = getComponentRes.left().value();
        }
        return component;
    }

    private Component getComponentByUuid(ComponentTypeEnum componentType, String componentUuid, Wrapper<ResponseFormat> errorWrapper) {
        Component component = null;
        Either<List<Component>, StorageOperationStatus> getComponentRes = toscaOperationFacade.getComponentListByUuid(componentUuid, null);
        if (getComponentRes.isRight()) {
            StorageOperationStatus status = getComponentRes.right().value();
            log.debug("Could not fetch component with type {} and uuid {}. Status is {}. ", componentType, componentUuid, status);
            errorWrapper.setInnerElement(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status)));
        }
        else {
            List<Component> value = getComponentRes.left().value();
            if (value.isEmpty()) {
                log.debug("Could not fetch component with type {} and uuid {}.", componentType, componentUuid);
                ActionStatus status = componentType == ComponentTypeEnum.RESOURCE ? ActionStatus.RESOURCE_NOT_FOUND : ActionStatus.SERVICE_NOT_FOUND;
                errorWrapper.setInnerElement(componentsUtils.getResponseFormat(status));
            }
            else {
                component = value.get(0);
            }
        }
        return component;
    }

    private String getLatestParentArtifactDataIdByArtifactUUID(String artifactUUID, Wrapper<ResponseFormat> errorWrapper, String parentId, ComponentTypeEnum componentType) {
        String artifactId = null;
        ActionStatus actionStatus = ActionStatus.ARTIFACT_NOT_FOUND;
        StorageOperationStatus storageStatus;
        ArtifactDefinition latestArtifact = null;
        List<ArtifactDefinition> artifacts = null;
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getArtifactsRes = artifactToscaOperation.getArtifacts(parentId);
        if (getArtifactsRes.isRight()) {
            storageStatus = getArtifactsRes.right().value();
            log.debug("Couldn't fetch artifacts data for parent component {} with uid {}, error: {}", componentType, parentId, storageStatus);
            if (!storageStatus.equals(StorageOperationStatus.NOT_FOUND)) {
                actionStatus = componentsUtils.convertFromStorageResponse(storageStatus);
            }
            errorWrapper.setInnerElement(componentsUtils.getResponseFormat(actionStatus, artifactUUID));
        }
        if (errorWrapper.isEmpty()) {
            artifacts = getArtifactsRes.left()
                                       .value()
                                       .values()
                                       .stream()
                                       .filter(a -> a.getArtifactUUID() != null && a.getArtifactUUID()
                                                                                    .equals(artifactUUID))
                                       .collect(Collectors.toList());
            if (artifacts == null || artifacts.isEmpty()) {
                log.debug("Couldn't fetch artifact with UUID {} data for parent component {} with uid {}, error: {}", artifactUUID, componentType, parentId, actionStatus);
                errorWrapper.setInnerElement(componentsUtils.getResponseFormat(actionStatus, artifactUUID));
            }
        }
        if (errorWrapper.isEmpty()) {
            latestArtifact = artifacts.stream().max((a1, a2) -> {
                int compareRes = Double.compare(Double.parseDouble(a1.getArtifactVersion()), Double.parseDouble(a2.getArtifactVersion()));
                if (compareRes == 0) {
                    compareRes = Long.compare(a1.getLastUpdateDate() == null ? 0 : a1.getLastUpdateDate(), a2.getLastUpdateDate() == null ? 0 : a2
                            .getLastUpdateDate());
                }
                return compareRes;
            }).get();
            if (latestArtifact == null) {
                log.debug("Couldn't fetch latest artifact with UUID {} data for parent component {} with uid {}, error: {}", artifactUUID, componentType, parentId, actionStatus);
                errorWrapper.setInnerElement(componentsUtils.getResponseFormat(actionStatus, artifactUUID));
            }
        }
        if (errorWrapper.isEmpty()) {
            artifactId = latestArtifact.getUniqueId();
        }
        return artifactId;
    }

    private Component checkoutParentComponent(ComponentTypeEnum componentType, String parentId, String userId, Wrapper<ResponseFormat> errorWrapper) {

        Component component = null;
        Either<User, ActionStatus> getUserRes = userBusinessLogic.getUser(userId, false);
        if (getUserRes.isRight()) {
            log.debug("Could not fetch User of component {} with uid {} to checked out. Status is {}. ", componentType.getNodeType(), parentId, getUserRes
                    .right()
                    .value());
            errorWrapper.setInnerElement(componentsUtils.getResponseFormat(getUserRes.right().value()));
        }
        if (errorWrapper.isEmpty()) {
            User modifier = getUserRes.left().value();
            LifecycleChangeInfoWithAction changeInfo = new LifecycleChangeInfoWithAction("External API checkout", LifecycleChanceActionEnum.UPDATE_FROM_EXTERNAL_API);
            Either<? extends Component, ResponseFormat> checkoutRes = lifecycleBusinessLogic.changeComponentState(componentType, parentId, modifier, LifeCycleTransitionEnum.CHECKOUT, changeInfo, false, true);
            if (checkoutRes.isRight()) {
                log.debug("Could not change state of component {} with uid {} to checked out. Status is {}. ", componentType
                        .getNodeType(), parentId, checkoutRes.right().value().getStatus());
                errorWrapper.setInnerElement(checkoutRes.right().value());
            }
            else {
                component = checkoutRes.left().value();
            }
        }
        return component;
    }

    private String buildJsonStringForCsarVfcArtifact(ArtifactDefinition artifact) {
        Map<String, Object> json = new HashMap<>();
        String artifactName = artifact.getArtifactName();
        json.put(Constants.ARTIFACT_NAME, artifactName);
        json.put(Constants.ARTIFACT_LABEL, artifact.getArtifactLabel());
        json.put(Constants.ARTIFACT_TYPE, artifact.getArtifactType());
        json.put(Constants.ARTIFACT_GROUP_TYPE, ArtifactGroupTypeEnum.DEPLOYMENT.getType());
        json.put(Constants.ARTIFACT_DESCRIPTION, artifact.getDescription());
        json.put(Constants.ARTIFACT_PAYLOAD_DATA, artifact.getPayloadData());
        json.put(Constants.ARTIFACT_DISPLAY_NAME, artifact.getArtifactDisplayName());
        return gson.toJson(json);
    }

    @VisibleForTesting
    void setNodeTemplateOperation(NodeTemplateOperation nodeTemplateOperation) {
        this.nodeTemplateOperation = nodeTemplateOperation;
    }
}
