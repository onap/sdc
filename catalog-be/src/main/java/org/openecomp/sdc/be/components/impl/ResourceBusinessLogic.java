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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections.MapUtils.isEmpty;
import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static org.openecomp.sdc.be.components.impl.ImportUtils.findFirstToscaStringElement;
import static org.openecomp.sdc.be.components.impl.ImportUtils.getPropertyJsonStringValue;
import static org.openecomp.sdc.be.tosca.CsarUtils.VF_NODE_TYPE_ARTIFACTS_PATH_PATTERN;
import static org.openecomp.sdc.common.api.Constants.DEFAULT_GROUP_VF_MODULE;

import com.google.common.annotations.VisibleForTesting;
import fj.data.Either;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.catalog.enums.ChangeTypeEnum;
import org.openecomp.sdc.be.components.csar.CsarArtifactsAndGroupsBusinessLogic;
import org.openecomp.sdc.be.components.csar.CsarBusinessLogic;
import org.openecomp.sdc.be.components.csar.CsarInfo;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.artifact.ArtifactOperationInfo;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.utils.CINodeFilterUtils;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction.LifecycleChanceActionEnum;
import org.openecomp.sdc.be.components.merge.TopologyComparator;
import org.openecomp.sdc.be.components.merge.property.PropertyDataValueMergeBusinessLogic;
import org.openecomp.sdc.be.components.merge.resource.ResourceDataMergeBusinessLogic;
import org.openecomp.sdc.be.components.merge.utils.MergeInstanceUtils;
import org.openecomp.sdc.be.components.property.PropertyConstraintsUtils;
import org.openecomp.sdc.be.components.validation.component.ComponentContactIdValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentDescriptionValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentIconValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentNameValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentProjectCodeValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentTagsValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentValidator;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.datamodel.api.HighestFilterEnum;
import org.openecomp.sdc.be.datamodel.utils.ArtifactUtils;
import org.openecomp.sdc.be.datamodel.utils.UiComponentDataConverter;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentFieldsEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.CreatedFrom;
import org.openecomp.sdc.be.datatypes.enums.ModelTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.info.NodeTypeInfoToUpdateArtifacts;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Model;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.ParsedToscaYamlInfo;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.UploadArtifactInfo;
import org.openecomp.sdc.be.model.UploadCapInfo;
import org.openecomp.sdc.be.model.UploadComponentInstanceInfo;
import org.openecomp.sdc.be.model.UploadInfo;
import org.openecomp.sdc.be.model.UploadNodeFilterInfo;
import org.openecomp.sdc.be.model.UploadPropInfo;
import org.openecomp.sdc.be.model.UploadReqInfo;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.ICapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.tosca.CsarUtils.NonMetaArtifactInfo;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.be.utils.CommonBeUtils;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.kpi.api.ASDCKpiApi;
import org.openecomp.sdc.common.log.elements.LoggerSupportability;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.enums.LoggerSupportabilityActions;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

@org.springframework.stereotype.Component("resourceBusinessLogic")
public class ResourceBusinessLogic extends ComponentBusinessLogic {

    private static final String DELETE_RESOURCE = "Delete Resource";
    private static final String IN_RESOURCE = "  in resource {} ";
    private static final String PLACE_HOLDER_RESOURCE_TYPES = "validForResourceTypes";
    private static final String INITIAL_VERSION = "0.1";
    private static final Logger log = Logger.getLogger(ResourceBusinessLogic.class);
    private static final String CERTIFICATION_ON_IMPORT = "certification on import";
    private static final String CREATE_RESOURCE = "Create Resource";
    private static final String VALIDATE_DERIVED_BEFORE_UPDATE = "validate derived before update";
    private static final String CATEGORY_IS_EMPTY = "Resource category is empty";
    private static final String CREATE_RESOURCE_VALIDATE_CAPABILITY_TYPES = "Create Resource - validateCapabilityTypesCreate";
    private static final String COMPONENT_INSTANCE_WITH_NAME = "component instance with name ";
    private static final String COMPONENT_INSTANCE_WITH_NAME_IN_RESOURCE = "component instance with name {}  in resource {} ";
    private static final String VALID_CHARACTERS_ARTIFACT_NAME = "'A-Z', 'a-z', '0-9', '.', '_', '-', '@' and space";
    private static final LoggerSupportability loggerSupportability = LoggerSupportability.getLogger(ResourceBusinessLogic.class.getName());
    private final ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    private final ResourceImportManager resourceImportManager;
    private final InputsBusinessLogic inputsBusinessLogic;
    private final OutputsBusinessLogic outputsBusinessLogic;
    private final CompositionBusinessLogic compositionBusinessLogic;
    private final ResourceDataMergeBusinessLogic resourceDataMergeBusinessLogic;
    private final CsarArtifactsAndGroupsBusinessLogic csarArtifactsAndGroupsBusinessLogic;
    private final MergeInstanceUtils mergeInstanceUtils;
    private final UiComponentDataConverter uiComponentDataConverter;
    private final CsarBusinessLogic csarBusinessLogic;
    private final PropertyBusinessLogic propertyBusinessLogic;
    private final PolicyBusinessLogic policyBusinessLogic;
    private final ModelBusinessLogic modelBusinessLogic;
    private final DataTypeBusinessLogic dataTypeBusinessLogic;
    private final PolicyTypeBusinessLogic policyTypeBusinessLogic;
    private final ModelOperation modelOperation;
    private IInterfaceLifecycleOperation interfaceTypeOperation;
    private LifecycleBusinessLogic lifecycleBusinessLogic;
    @Autowired
    private ICapabilityTypeOperation capabilityTypeOperation;
    @Autowired
    private TopologyComparator topologyComparator;
    @Autowired
    private ComponentValidator componentValidator;
    @Autowired
    private PropertyDataValueMergeBusinessLogic propertyDataValueMergeBusinessLogic;
    @Autowired
    private SoftwareInformationBusinessLogic softwareInformationBusinessLogic;


    @Autowired
    public ResourceBusinessLogic(final IElementOperation elementDao, final IGroupOperation groupOperation,
                                 final IGroupInstanceOperation groupInstanceOperation, final IGroupTypeOperation groupTypeOperation,
                                 final GroupBusinessLogic groupBusinessLogic, final InterfaceOperation interfaceOperation,
                                 final InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
                                 final ArtifactsBusinessLogic artifactsBusinessLogic,
                                 final ComponentInstanceBusinessLogic componentInstanceBusinessLogic,
                                 final @Lazy ResourceImportManager resourceImportManager, final InputsBusinessLogic inputsBusinessLogic,
                                 final OutputsBusinessLogic outputsBusinessLogic, final CompositionBusinessLogic compositionBusinessLogic,
                                 final ResourceDataMergeBusinessLogic resourceDataMergeBusinessLogic,
                                 final CsarArtifactsAndGroupsBusinessLogic csarArtifactsAndGroupsBusinessLogic,
                                 final MergeInstanceUtils mergeInstanceUtils, final UiComponentDataConverter uiComponentDataConverter,
                                 final CsarBusinessLogic csarBusinessLogic, final ArtifactsOperations artifactToscaOperation,
                                 final PropertyBusinessLogic propertyBusinessLogic, final ComponentContactIdValidator componentContactIdValidator,
                                 final ComponentNameValidator componentNameValidator, final ComponentTagsValidator componentTagsValidator,
                                 final ComponentValidator componentValidator, final ComponentIconValidator componentIconValidator,
                                 final ComponentProjectCodeValidator componentProjectCodeValidator,
                                 final ComponentDescriptionValidator componentDescriptionValidator, final PolicyBusinessLogic policyBusinessLogic,
                                 final ModelBusinessLogic modelBusinessLogic,
                                 final DataTypeBusinessLogic dataTypeBusinessLogic, final PolicyTypeBusinessLogic policyTypeBusinessLogic,
                                 final ModelOperation modelOperation) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, groupBusinessLogic, interfaceOperation,
            interfaceLifecycleTypeOperation, artifactsBusinessLogic, artifactToscaOperation, componentContactIdValidator, componentNameValidator,
            componentTagsValidator, componentValidator, componentIconValidator, componentProjectCodeValidator, componentDescriptionValidator);
        this.componentInstanceBusinessLogic = componentInstanceBusinessLogic;
        this.resourceImportManager = resourceImportManager;
        this.inputsBusinessLogic = inputsBusinessLogic;
        this.outputsBusinessLogic = outputsBusinessLogic;
        this.compositionBusinessLogic = compositionBusinessLogic;
        this.resourceDataMergeBusinessLogic = resourceDataMergeBusinessLogic;
        this.csarArtifactsAndGroupsBusinessLogic = csarArtifactsAndGroupsBusinessLogic;
        this.mergeInstanceUtils = mergeInstanceUtils;
        this.uiComponentDataConverter = uiComponentDataConverter;
        this.csarBusinessLogic = csarBusinessLogic;
        this.propertyBusinessLogic = propertyBusinessLogic;
        this.policyBusinessLogic = policyBusinessLogic;
        this.modelBusinessLogic = modelBusinessLogic;
        this.dataTypeBusinessLogic = dataTypeBusinessLogic;
        this.policyTypeBusinessLogic = policyTypeBusinessLogic;
        this.modelOperation = modelOperation;
    }

    static <T> Either<T, RuntimeException> rollbackWithEither(final JanusGraphDao janusGraphDao, final ActionStatus actionStatus,
                                                              final String... params) {
        if (janusGraphDao != null) {
            janusGraphDao.rollback();
        }
        return Either.right(new ByActionStatusComponentException(actionStatus, params));
    }

    public LifecycleBusinessLogic getLifecycleBusinessLogic() {
        return lifecycleBusinessLogic;
    }

    @Autowired
    public void setLifecycleManager(LifecycleBusinessLogic lifecycleBusinessLogic) {
        this.lifecycleBusinessLogic = lifecycleBusinessLogic;
    }

    @VisibleForTesting
    protected void setComponentValidator(ComponentValidator componentValidator) {
        this.componentValidator = componentValidator;
    }

    public IElementOperation getElementDao() {
        return elementDao;
    }

    public void setElementDao(IElementOperation elementDao) {
        this.elementDao = elementDao;
    }

    public UserBusinessLogic getUserAdmin() {
        return this.userAdmin;
    }

    @Autowired
    @Override
    public void setUserAdmin(UserBusinessLogic userAdmin) {
        this.userAdmin = userAdmin;
    }

    public ComponentsUtils getComponentsUtils() {
        return this.componentsUtils;
    }

    @Autowired
    @Override
    public void setComponentsUtils(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }

    public ArtifactsBusinessLogic getArtifactsManager() {
        return artifactsBusinessLogic;
    }

    public void setArtifactsManager(ArtifactsBusinessLogic artifactsManager) {
        this.artifactsBusinessLogic = artifactsManager;
    }

    public ApplicationDataTypeCache getApplicationDataTypeCache() {
        return applicationDataTypeCache;
    }

    @Autowired
    @Override
    public void setApplicationDataTypeCache(ApplicationDataTypeCache applicationDataTypeCache) {
        this.applicationDataTypeCache = applicationDataTypeCache;
    }

    @Autowired
    public void setInterfaceTypeOperation(IInterfaceLifecycleOperation interfaceTypeOperation) {
        this.interfaceTypeOperation = interfaceTypeOperation;
    }

    /**
     * the method returns a list of all the resources that are certified, the returned resources are only abstract or only none abstract according to
     * the given param
     *
     * @param getAbstract
     * @param userId      TODO
     * @return
     */
    public List<Resource> getAllCertifiedResources(boolean getAbstract, HighestFilterEnum highestFilter, String userId) {
        User user = validateUserExists(userId);
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
        Either<List<Resource>, StorageOperationStatus> getResponse = toscaOperationFacade.getAllCertifiedResources(getAbstract, isHighest);
        if (getResponse.isRight()) {
            throw new StorageException(getResponse.right().value());
        }
        return getResponse.left().value();
    }

    public Either<Map<String, Boolean>, ResponseFormat> validateResourceNameExists(String resourceName, ResourceTypeEnum resourceTypeEnum,
                                                                                   String userId) {
        validateUserExists(userId);
        Either<Boolean, StorageOperationStatus> dataModelResponse = toscaOperationFacade
            .validateComponentNameUniqueness(resourceName, resourceTypeEnum, ComponentTypeEnum.RESOURCE);
        // DE242223
        janusGraphDao.commit();
        if (dataModelResponse.isLeft()) {
            Map<String, Boolean> result = new HashMap<>();
            result.put("isValid", dataModelResponse.left().value());
            log.debug("validation was successfully performed.");
            return Either.left(result);
        }
        ResponseFormat responseFormat = componentsUtils
            .getResponseFormat(componentsUtils.convertFromStorageResponse(dataModelResponse.right().value()));
        return Either.right(responseFormat);
    }

    public Resource createResource(Resource resource, AuditingActionEnum auditingAction, User user, Map<String, byte[]> csarUIPayload,
                                   String payloadName) {
        validateResourceBeforeCreate(resource, user, false);
        String csarUUID = payloadName == null ? resource.getCsarUUID() : payloadName;
        loggerSupportability.log(LoggerSupportabilityActions.CREATE_RESOURCE, resource.getComponentMetadataForSupportLog(), StatusCode.STARTED,
            "Starting to create resource from CSAR by user {} ", user.getUserId());
        if (StringUtils.isNotEmpty(csarUUID)) {
            csarBusinessLogic.validateCsarBeforeCreate(resource, auditingAction, user, csarUUID);
            log.debug("CsarUUID is {} - going to create resource from CSAR", csarUUID);
            Resource createResourceFromCsar = createResourceFromCsar(resource, user, csarUIPayload, csarUUID);
            return updateCatalog(createResourceFromCsar, ChangeTypeEnum.LIFECYCLE).left().map(Resource.class::cast).left().value();
        }
        final Resource createResourceByDao = createResourceByDao(resource, user, auditingAction, false, false);
        return updateCatalog(createResourceByDao, ChangeTypeEnum.LIFECYCLE).left().map(Resource.class::cast).left().value();
    }

    public Resource validateAndUpdateResourceFromCsar(Resource resource, User user, Map<String, byte[]> csarUIPayload, String payloadName,
                                                      String resourceUniqueId) {
        String csarUUID = payloadName;
        String csarVersion = null;
        Resource updatedResource = null;
        if (payloadName == null) {
            csarUUID = resource.getCsarUUID();
            csarVersion = resource.getCsarVersion();
        }
        if (csarUUID != null && !csarUUID.isEmpty()) {
            Resource oldResource = getResourceByUniqueId(resourceUniqueId);
            validateCsarUuidMatching(oldResource, resource, csarUUID, resourceUniqueId, user);
            validateCsarIsNotAlreadyUsed(oldResource, resource, csarUUID, user);
            if (oldResource != null && ValidationUtils.hasBeenCertified(oldResource.getVersion())) {
                overrideImmutableMetadata(oldResource, resource);
            }
            validateResourceBeforeCreate(resource, user, false);
            String oldCsarVersion = oldResource != null ? oldResource.getCsarVersion() : null;
            log.debug("CsarUUID is {} - going to update resource with UniqueId {} from CSAR", csarUUID, resourceUniqueId);
            // (on boarding flow): If the update includes same csarUUID and

            // same csarVersion as already in the VF - no need to import the

            // csar (do only metadata changes if there are).
            if (csarVersion != null && oldCsarVersion != null && oldCsarVersion.equals(csarVersion)) {
                updatedResource = updateResourceMetadata(resourceUniqueId, resource, oldResource, user, false);
            } else {
                updatedResource = updateResourceFromCsar(oldResource, resource, user, AuditingActionEnum.UPDATE_RESOURCE_METADATA, false,
                    csarUIPayload, csarUUID);
            }
        } else {
            log.debug("Failed to update resource {}, csarUUID or payload name is missing", resource.getSystemName());
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_CSAR_UUID, resource.getName());
            componentsUtils.auditResource(errorResponse, user, resource, AuditingActionEnum.CREATE_RESOURCE);
            throw new ByActionStatusComponentException(ActionStatus.MISSING_CSAR_UUID, resource.getName());
        }
        return updatedResource;
    }

    private void validateCsarIsNotAlreadyUsed(Resource oldResource, Resource resource, String csarUUID, User user) {
        // (on boarding flow): If the update includes a csarUUID: verify this

        // csarUUID is not in use by another VF, If it is - use same error as

        // above:

        // "Error: The VSP with UUID %1 was already imported for VF %2. Please

        // select another or update the existing VF." %1 - csarUUID, %2 - VF

        // name
        Either<Resource, StorageOperationStatus> resourceLinkedToCsarRes = toscaOperationFacade
            .getLatestComponentByCsarOrName(ComponentTypeEnum.RESOURCE, csarUUID, resource.getSystemName());
        if (resourceLinkedToCsarRes.isRight()) {
            if (StorageOperationStatus.NOT_FOUND != resourceLinkedToCsarRes.right().value()) {
                log.debug("Failed to find previous resource by CSAR {} and system name {}", csarUUID, resource.getSystemName());
                throw new StorageException(resourceLinkedToCsarRes.right().value());
            }
        } else if (!resourceLinkedToCsarRes.left().value().getUniqueId().equals(oldResource.getUniqueId()) && !resourceLinkedToCsarRes.left().value()
            .getName().equals(oldResource.getName())) {
            ResponseFormat errorResponse = componentsUtils
                .getResponseFormat(ActionStatus.VSP_ALREADY_EXISTS, csarUUID, resourceLinkedToCsarRes.left().value().getName());
            componentsUtils.auditResource(errorResponse, user, resource, AuditingActionEnum.UPDATE_RESOURCE_METADATA);
            throw new ByActionStatusComponentException(ActionStatus.VSP_ALREADY_EXISTS, csarUUID, resourceLinkedToCsarRes.left().value().getName());
        }
    }

    private void validateCsarUuidMatching(Resource resource, Resource oldResource, String csarUUID, String resourceUniqueId, User user) {
        // (on boarding flow): If the update includes csarUUID which is

        // different from the csarUUID of the VF - fail with

        // error: "Error: Resource %1 cannot be updated using since it is linked

        // to a different VSP" %1 - VF name
        String oldCsarUUID = oldResource.getCsarUUID();
        if (oldCsarUUID != null && !oldCsarUUID.isEmpty() && !csarUUID.equals(oldCsarUUID)) {
            log.debug("Failed to update resource with UniqueId {} using Csar {}, since the resource is linked to a different VSP {}",
                resourceUniqueId, csarUUID, oldCsarUUID);
            ResponseFormat errorResponse = componentsUtils
                .getResponseFormat(ActionStatus.RESOURCE_LINKED_TO_DIFFERENT_VSP, resource.getName(), csarUUID, oldCsarUUID);
            componentsUtils.auditResource(errorResponse, user, resource, AuditingActionEnum.UPDATE_RESOURCE_METADATA);
            throw new ByActionStatusComponentException(ActionStatus.RESOURCE_LINKED_TO_DIFFERENT_VSP, resource.getName(), csarUUID, oldCsarUUID);
        }
    }

    private Resource getResourceByUniqueId(String resourceUniqueId) {
        Either<Resource, StorageOperationStatus> oldResourceRes = toscaOperationFacade.getToscaFullElement(resourceUniqueId);
        if (oldResourceRes.isRight()) {
            log.debug("Failed to find previous resource by UniqueId {}, status: {}", resourceUniqueId, oldResourceRes.right().value());
            throw new StorageException(oldResourceRes.right().value());
        }
        return oldResourceRes.left().value();
    }

    private void overrideImmutableMetadata(Resource oldResource, Resource resource) {
        resource.setName(oldResource.getName());
        resource.setIcon(oldResource.getIcon());
        resource.setTags(oldResource.getTags());
        resource.setCategories(oldResource.getCategories());
        resource.setDerivedFrom(oldResource.getDerivedFrom());
    }

    private Resource updateResourceFromCsar(Resource oldResource, Resource newResource, User user, AuditingActionEnum updateResource,
                                            boolean inTransaction, Map<String, byte[]> csarUIPayload, String csarUUID) {
        Resource updatedResource = null;
        validateLifecycleState(oldResource, user);
        String lockedResourceId = oldResource.getUniqueId();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        CsarInfo csarInfo = csarBusinessLogic.getCsarInfo(newResource, oldResource, user, csarUIPayload, csarUUID);
        lockComponent(lockedResourceId, oldResource, "update Resource From Csar");
        Map<String, NodeTypeInfo> nodeTypesInfo = csarInfo.extractTypesInfo();
        Either<Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>>, ResponseFormat> findNodeTypesArtifactsToHandleRes = findNodeTypesArtifactsToHandle(
            nodeTypesInfo, csarInfo, oldResource);
        if (findNodeTypesArtifactsToHandleRes.isRight()) {
            log.debug("failed to find node types for update with artifacts during import csar {}. ", csarInfo.getCsarUUID());
            throw new ByResponseFormatComponentException(findNodeTypesArtifactsToHandleRes.right().value());
        }
        Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = findNodeTypesArtifactsToHandleRes.left()
            .value();
        try {
            updatedResource = updateResourceFromYaml(oldResource, newResource, updateResource, createdArtifacts, csarInfo.getMainTemplateName(),
                csarInfo.getMainTemplateContent(), csarInfo, nodeTypesInfo, nodeTypesArtifactsToHandle, null, false);
        } catch (ComponentException | StorageException e) {
            rollback(inTransaction, newResource, createdArtifacts, null);
            throw e;
        } finally {
            janusGraphDao.commit();
            log.debug("unlock resource {}", lockedResourceId);
            graphLockOperation.unlockComponent(lockedResourceId, NodeTypeEnum.Resource);
        }
        return updatedResource;
    }

    private void validateLifecycleState(Resource oldResource, User user) {
        if (LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT == oldResource.getLifecycleState() && !oldResource.getLastUpdaterUserId()
            .equals(user.getUserId())) {
            log.debug("#validateLifecycleState - Current user is not last updater, last updater userId: {}, current user userId: {}",
                oldResource.getLastUpdaterUserId(), user.getUserId());
            throw new ByActionStatusComponentException(ActionStatus.RESTRICTED_OPERATION);
        }
    }

    private Resource updateResourceFromYaml(Resource oldResource, Resource newResource, AuditingActionEnum actionEnum,
                                            List<ArtifactDefinition> createdArtifacts, String yamlFileName, String yamlFileContent, CsarInfo csarInfo,
                                            Map<String, NodeTypeInfo> nodeTypesInfo,
                                            Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle,
                                            String nodeName, boolean isNested) {
        boolean inTransaction = true;
        boolean shouldLock = false;
        Resource preparedResource = null;
        ParsedToscaYamlInfo uploadComponentInstanceInfoMap;
        try {
            uploadComponentInstanceInfoMap = csarBusinessLogic
                .getParsedToscaYamlInfo(yamlFileContent, yamlFileName, nodeTypesInfo, csarInfo, nodeName, oldResource);
            Map<String, UploadComponentInstanceInfo> instances = uploadComponentInstanceInfoMap.getInstances();
            if (MapUtils.isEmpty(instances) && newResource.getResourceType() != ResourceTypeEnum.PNF) {
                throw new ByActionStatusComponentException(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlFileName);
            }
            preparedResource = updateExistingResourceByImport(newResource, oldResource, csarInfo.getModifier(), inTransaction, shouldLock,
                isNested).left;
            log.trace("YAML topology file found in CSAR, file name: {}, contents: {}", yamlFileName, yamlFileContent);
            handleResourceGenericType(preparedResource, yamlFileContent, uploadComponentInstanceInfoMap,
                uploadComponentInstanceInfoMap.getSubstitutionMappingNodeType());
            handleNodeTypes(yamlFileName, preparedResource, yamlFileContent, shouldLock, nodeTypesArtifactsToHandle, createdArtifacts, nodeTypesInfo,
                csarInfo, nodeName, newResource.getModel());
            preparedResource = createInputsOnResource(preparedResource, uploadComponentInstanceInfoMap.getInputs());
            Map<String, Resource> existingNodeTypesByResourceNames = new HashMap<>();
            final Map<String, UploadComponentInstanceInfo> instancesToCreate = getInstancesToCreate(uploadComponentInstanceInfoMap,
                newResource.getModel());
            preparedResource = createResourceInstances(yamlFileName, preparedResource, oldResource, instancesToCreate, csarInfo.getCreatedNodes(),
                existingNodeTypesByResourceNames);
            preparedResource = createResourceInstancesRelations(csarInfo.getModifier(), yamlFileName, preparedResource, oldResource,
                instancesToCreate,
                existingNodeTypesByResourceNames);
        } catch (ComponentException e) {
            ResponseFormat responseFormat =
                e.getResponseFormat() == null ? componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams()) : e.getResponseFormat();
            log.debug("#updateResourceFromYaml - failed to update newResource from yaml {} .The error is {}", yamlFileName, responseFormat);
            componentsUtils
                .auditResource(responseFormat, csarInfo.getModifier(), preparedResource == null ? oldResource : preparedResource, actionEnum);
            throw e;
        } catch (StorageException e) {
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(e.getStorageOperationStatus()));
            log.debug("#updateResourceFromYaml - failed to update newResource from yaml {} .The error is {}", yamlFileName, responseFormat);
            componentsUtils
                .auditResource(responseFormat, csarInfo.getModifier(), preparedResource == null ? oldResource : preparedResource, actionEnum);
            throw e;
        }
        Either<Map<String, GroupDefinition>, ResponseFormat> validateUpdateVfGroupNamesRes = groupBusinessLogic
            .validateUpdateVfGroupNames(uploadComponentInstanceInfoMap.getGroups(), preparedResource.getSystemName());
        if (validateUpdateVfGroupNamesRes.isRight()) {
            throw new ByResponseFormatComponentException(validateUpdateVfGroupNamesRes.right().value());
        }
        // add groups to newResource
        Map<String, GroupDefinition> groups;
        if (!validateUpdateVfGroupNamesRes.left().value().isEmpty()) {
            groups = validateUpdateVfGroupNamesRes.left().value();
        } else {
            groups = uploadComponentInstanceInfoMap.getGroups();
        }
        handleGroupsProperties(preparedResource, groups);
        Either<Boolean, ActionStatus> isTopologyChanged = topologyComparator.isTopologyChanged(oldResource, preparedResource);
        preparedResource = updateGroupsOnResource(preparedResource, groups);
        NodeTypeInfoToUpdateArtifacts nodeTypeInfoToUpdateArtifacts = new NodeTypeInfoToUpdateArtifacts(nodeName, nodeTypesArtifactsToHandle);
        Either<Resource, ResponseFormat> updateArtifactsEither = createOrUpdateArtifacts(ArtifactOperationEnum.UPDATE, createdArtifacts, yamlFileName,
            csarInfo, preparedResource, nodeTypeInfoToUpdateArtifacts, inTransaction, shouldLock);
        if (updateArtifactsEither.isRight()) {
            log.debug("failed to update artifacts {}", updateArtifactsEither.right().value());
            throw new ByResponseFormatComponentException(updateArtifactsEither.right().value());
        }
        preparedResource = getResourceWithGroups(updateArtifactsEither.left().value().getUniqueId());
        updateGroupsName(oldResource, preparedResource, isTopologyChanged.left().value());
        updateResourceInstancesNames(oldResource, csarInfo, preparedResource, isTopologyChanged.left().value());
        final String preparedResourceId = preparedResource != null ? preparedResource.getUniqueId() : "";
        preparedResource = getResourceWithGroups(preparedResourceId);
        updateVolumeGroup(preparedResource);
        ActionStatus mergingPropsAndInputsStatus = resourceDataMergeBusinessLogic.mergeResourceEntities(oldResource, preparedResource);
        if (mergingPropsAndInputsStatus != ActionStatus.OK) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(mergingPropsAndInputsStatus, preparedResource);
            throw new ByResponseFormatComponentException(responseFormat);
        }
        compositionBusinessLogic.setPositionsForComponentInstances(preparedResource, csarInfo.getModifier().getUserId());
        return preparedResource;
    }

    protected void updateVolumeGroup(Resource preparedResource) {
        List<GroupDefinition> groups = preparedResource.safeGetGroups();
        for (GroupDefinition group : groups) {
            Map<String, ArtifactDefinition> createdNewArtifacts = preparedResource.getDeploymentArtifacts();
            if (DEFAULT_GROUP_VF_MODULE.equals(group.getType())) {
                List<PropertyDataDefinition> volumePropList = group.getProperties().stream().filter(p -> "volume_group".equals(p.getName()))
                    .collect(Collectors.toList());
                if (!volumePropList.isEmpty()) {
                    PropertyDataDefinition volumeProp = volumePropList.get(0);
                    if (volumeProp != null) {
                        boolean isVolumeGroup = isVolumeGroup(group.getArtifacts(), new ArrayList<>(createdNewArtifacts.values()));
                        if (!volumePropList.get(0).getValue().equals(String.valueOf(isVolumeGroup))) {
                            volumeProp.setValue(String.valueOf(isVolumeGroup));
                            volumeProp.setDefaultValue(String.valueOf(isVolumeGroup));
                        }
                    }
                }
            }
        }
    }

    private void updateGroupsName(Resource oldResource, Resource preparedResource, boolean isTopologyChanged) {
        if (oldResource == null || preparedResource == null) {
            log.debug("Failed to update groups name : oldResource or preparedResource is null");
        } else if (CollectionUtils.isNotEmpty(oldResource.getGroups()) && CollectionUtils.isNotEmpty(preparedResource.getGroups())) {
            Map<String, String> oldGroups = oldResource.getGroups().stream()
                .collect(toMap(GroupDataDefinition::getInvariantName, GroupDataDefinition::getName));
            List<GroupDefinition> updatedGroups = preparedResource.getGroups().stream()
                .filter(group -> oldGroups.containsKey(group.getInvariantName()) && !group.getName().equals(oldGroups.get(group.getInvariantName())))
                .collect(toList());
            if (CollectionUtils.isNotEmpty(updatedGroups)) {
                if (isTopologyChanged) {
                    updatedGroups.stream().filter(group -> !group.isVspOriginated())
                        .forEach(group -> group.setName(oldGroups.get(group.getInvariantName())));
                } else {
                    updatedGroups.forEach(group -> group.setName(oldGroups.get(group.getInvariantName())));
                }
                groupBusinessLogic.updateGroups(preparedResource, updatedGroups, false);
            }
        }
    }

    private void updateResourceInstancesNames(Resource oldResource, CsarInfo csarInfo, Resource preparedResource, boolean isTopologyChanged) {
        if (oldResource == null || preparedResource == null) {
            log.debug("Failed to update resource instances names : oldResource or preparedResource is null");
        } else {
            if (CollectionUtils.isNotEmpty(oldResource.getComponentInstances())) {
                Map<String, String> oldInstances = oldResource.getComponentInstances().stream()
                    .collect(toMap(ComponentInstance::getInvariantName, ComponentInstance::getName));
                List<ComponentInstance> updatedInstances = preparedResource.getComponentInstances().stream()
                    .filter(i -> oldInstances.containsKey(i.getInvariantName()) && !i.getName().equals(oldInstances.get(i.getInvariantName())))
                    .collect(toList());
                if (CollectionUtils.isNotEmpty(updatedInstances)) {
                    if (isTopologyChanged) {
                        updatedInstances.stream().filter(i -> !i.isCreatedFromCsar()).forEach(i -> i.setName(oldInstances.get(i.getInvariantName())));
                    } else {
                        updatedInstances.forEach(i -> i.setName(oldInstances.get(i.getInvariantName())));
                    }
                }
            }
            componentInstanceBusinessLogic.updateComponentInstance(ComponentTypeEnum.RESOURCE_PARAM_NAME, null, preparedResource.getUniqueId(),
                csarInfo.getModifier().getUserId(), preparedResource.getComponentInstances(), false);
        }
    }

    private Either<Resource, ResponseFormat> createOrUpdateArtifacts(ArtifactOperationEnum operation, List<ArtifactDefinition> createdArtifacts,
                                                                     String yamlFileName, CsarInfo csarInfo, Resource preparedResource,
                                                                     NodeTypeInfoToUpdateArtifacts nodeTypeInfoToUpdateArtifacts,
                                                                     boolean inTransaction, boolean shouldLock) {
        String nodeName = nodeTypeInfoToUpdateArtifacts.getNodeName();
        Resource resource = preparedResource;
        Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = nodeTypeInfoToUpdateArtifacts
            .getNodeTypesArtifactsToHandle();
        if (preparedResource.getResourceType() == ResourceTypeEnum.CVFC) {
            if (nodeName != null && nodeTypesArtifactsToHandle.get(nodeName) != null && !nodeTypesArtifactsToHandle.get(nodeName).isEmpty()) {
                Either<List<ArtifactDefinition>, ResponseFormat> handleNodeTypeArtifactsRes = handleNodeTypeArtifacts(preparedResource,
                    nodeTypesArtifactsToHandle.get(nodeName), createdArtifacts, csarInfo.getModifier(), inTransaction, true);
                if (handleNodeTypeArtifactsRes.isRight()) {
                    return Either.right(handleNodeTypeArtifactsRes.right().value());
                }
            }
        } else {
            Either<Resource, ResponseFormat> createdCsarArtifactsEither = handleVfCsarArtifacts(preparedResource, csarInfo, createdArtifacts,
                new ArtifactOperationInfo(false, false, operation), shouldLock, inTransaction);
            log.trace("************* Finished to add artifacts from yaml {}", yamlFileName);
            if (createdCsarArtifactsEither.isRight()) {
                return createdCsarArtifactsEither;
            }
            resource = createdCsarArtifactsEither.left().value();
        }
        return Either.left(resource);
    }

    private Resource handleResourceGenericType(Resource resource) {
        Resource genericResource = fetchAndSetDerivedFromGenericType(resource);

        if (resource.shouldGenerateInputs()) {
            generateAndAddInputsFromGenericTypeProperties(resource, genericResource);
        }
        return genericResource;
    }

    private Resource handleResourceGenericType(final Resource resource, final String topologyTemplateYaml,
                                               final ParsedToscaYamlInfo parsedToscaYamlInfo, final String substitutionMappingNodeType) {
        if (processSubstitutableAsNodeType(resource, parsedToscaYamlInfo)) {
            final Map<String, Object> substitutableAsNodeType = getSubstitutableAsNodeTypeFromTemplate(
                (Map<String, Object>) new Yaml().load(topologyTemplateYaml), substitutionMappingNodeType);
            final Resource genericResource = fetchAndSetDerivedFromGenericType(resource,
                (String) substitutableAsNodeType.get(TypeUtils.ToscaTagNamesEnum.DERIVED_FROM.getElementName()));

            generatePropertiesFromGenericType(resource, genericResource);
            generatePropertiesFromNodeType(resource, substitutableAsNodeType);
            final String resourceId = resource.getUniqueId();
            resource.getProperties().forEach(propertyDefinition -> propertyDefinition.setUniqueId(
                UniqueIdBuilder.buildPropertyUniqueId(resourceId, propertyDefinition.getName())));
            createResourcePropertiesOnGraph(resource);
            return genericResource;
        }
        return handleResourceGenericType(resource);
    }

    private Either<Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>>, ResponseFormat> findNodeTypesArtifactsToHandle(
        final Map<String, NodeTypeInfo> nodeTypesInfo, final CsarInfo csarInfo, final Resource oldResource) {
        final Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        Either<Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>>, ResponseFormat> nodeTypesArtifactsToHandleRes = Either
            .left(nodeTypesArtifactsToHandle);
        try {
            final Map<String, List<ArtifactDefinition>> extractedVfcsArtifacts = CsarUtils.extractVfcsArtifactsFromCsar(csarInfo.getCsar());
            final Map<String, ImmutablePair<String, String>> extractedVfcToscaNames = extractVfcToscaNames(nodeTypesInfo, oldResource.getName(),
                csarInfo);
            log.debug("Going to fetch node types for resource with name {} during import csar with UUID {}. ", oldResource.getName(),
                csarInfo.getCsarUUID());
            extractedVfcToscaNames.forEach(
                (namespace, vfcToscaNames) -> findAddNodeTypeArtifactsToHandle(csarInfo, nodeTypesArtifactsToHandle, oldResource,
                    extractedVfcsArtifacts, namespace, vfcToscaNames));
        } catch (Exception e) {
            final ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            nodeTypesArtifactsToHandleRes = Either.right(responseFormat);
            log.debug("Exception occurred when findNodeTypesUpdatedArtifacts, error is:{}", e.getMessage(), e);
        }
        return nodeTypesArtifactsToHandleRes;
    }

    private void findAddNodeTypeArtifactsToHandle(CsarInfo csarInfo,
                                                  Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle,
                                                  Resource resource, Map<String, List<ArtifactDefinition>> extractedVfcsArtifacts, String namespace,
                                                  ImmutablePair<String, String> vfcToscaNames) {
        EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>> curNodeTypeArtifactsToHandle = null;
        log.debug("Going to fetch node type with tosca name {}. ", vfcToscaNames.getLeft());
        Resource curNodeType = findVfcResource(csarInfo, resource, vfcToscaNames.getLeft(), vfcToscaNames.getRight(), null);
        if (!isEmpty(extractedVfcsArtifacts)) {
            List<ArtifactDefinition> currArtifacts = new ArrayList<>();
            if (extractedVfcsArtifacts.containsKey(namespace)) {
                handleAndAddExtractedVfcsArtifacts(currArtifacts, extractedVfcsArtifacts.get(namespace));
            }
            curNodeTypeArtifactsToHandle = findNodeTypeArtifactsToHandle(curNodeType, currArtifacts);
        } else if (curNodeType != null) {
            // delete all artifacts if have not received artifacts from

            // csar
            curNodeTypeArtifactsToHandle = new EnumMap<>(ArtifactOperationEnum.class);
            List<ArtifactDefinition> artifactsToDelete = new ArrayList<>();
            // delete all informational artifacts
            artifactsToDelete.addAll(
                curNodeType.getArtifacts().values().stream().filter(a -> a.getArtifactGroupType() == ArtifactGroupTypeEnum.INFORMATIONAL)
                    .collect(toList()));
            // delete all deployment artifacts
            artifactsToDelete.addAll(curNodeType.getDeploymentArtifacts().values());
            if (!artifactsToDelete.isEmpty()) {
                curNodeTypeArtifactsToHandle.put(ArtifactOperationEnum.DELETE, artifactsToDelete);
            }
        }
        if (isNotEmpty(curNodeTypeArtifactsToHandle)) {
            nodeTypesArtifactsToHandle.put(namespace, curNodeTypeArtifactsToHandle);
        }
    }

    private Resource findVfcResource(CsarInfo csarInfo, Resource resource, String currVfcToscaName, String previousVfcToscaName,
                                     StorageOperationStatus status) {
        if (status != null && status != StorageOperationStatus.NOT_FOUND) {
            log.debug("Error occurred during fetching node type with tosca name {}, error: {}", currVfcToscaName, status);
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(status), csarInfo.getCsarUUID());
            componentsUtils.auditResource(responseFormat, csarInfo.getModifier(), resource, AuditingActionEnum.CREATE_RESOURCE);
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(status), csarInfo.getCsarUUID());
        } else if (StringUtils.isNotEmpty(currVfcToscaName)) {
            return (Resource) toscaOperationFacade.getLatestByToscaResourceName(currVfcToscaName, resource.getModel()).left()
                .on(st -> findVfcResource(csarInfo, resource, previousVfcToscaName, null, st));
        }
        return null;
    }

    private EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>> findNodeTypeArtifactsToHandle(Resource curNodeType,
                                                                                                   List<ArtifactDefinition> extractedArtifacts) {
        try {
            List<ArtifactDefinition> artifactsToUpload = new ArrayList<>(extractedArtifacts);
            List<ArtifactDefinition> artifactsToUpdate = new ArrayList<>();
            List<ArtifactDefinition> artifactsToDelete = new ArrayList<>();
            processExistingNodeTypeArtifacts(extractedArtifacts, artifactsToUpload, artifactsToUpdate, artifactsToDelete,
                collectExistingArtifacts(curNodeType));
            return putFoundArtifacts(artifactsToUpload, artifactsToUpdate, artifactsToDelete);
        } catch (Exception e) {
            log.debug("Exception occurred when findNodeTypeArtifactsToHandle, error is:{}", e.getMessage(), e);
            throw new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    private EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>> putFoundArtifacts(List<ArtifactDefinition> artifactsToUpload,
                                                                                       List<ArtifactDefinition> artifactsToUpdate,
                                                                                       List<ArtifactDefinition> artifactsToDelete) {
        EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle = null;
        if (!artifactsToUpload.isEmpty() || !artifactsToUpdate.isEmpty() || !artifactsToDelete.isEmpty()) {
            nodeTypeArtifactsToHandle = new EnumMap<>(ArtifactOperationEnum.class);
            if (!artifactsToUpload.isEmpty()) {
                nodeTypeArtifactsToHandle.put(ArtifactOperationEnum.CREATE, artifactsToUpload);
            }
            if (!artifactsToUpdate.isEmpty()) {
                nodeTypeArtifactsToHandle.put(ArtifactOperationEnum.UPDATE, artifactsToUpdate);
            }
            if (!artifactsToDelete.isEmpty()) {
                nodeTypeArtifactsToHandle.put(ArtifactOperationEnum.DELETE, artifactsToDelete);
            }
        }
        return nodeTypeArtifactsToHandle;
    }

    private void processExistingNodeTypeArtifacts(List<ArtifactDefinition> extractedArtifacts, List<ArtifactDefinition> artifactsToUpload,
                                                  List<ArtifactDefinition> artifactsToUpdate, List<ArtifactDefinition> artifactsToDelete,
                                                  Map<String, ArtifactDefinition> existingArtifacts) {
        if (!existingArtifacts.isEmpty()) {
            extractedArtifacts.forEach(a -> processNodeTypeArtifact(artifactsToUpload, artifactsToUpdate, existingArtifacts, a));
            artifactsToDelete.addAll(existingArtifacts.values());
        }
    }

    private void processNodeTypeArtifact(List<ArtifactDefinition> artifactsToUpload, List<ArtifactDefinition> artifactsToUpdate,
                                         Map<String, ArtifactDefinition> existingArtifacts, ArtifactDefinition currNewArtifact) {
        Optional<ArtifactDefinition> foundArtifact = existingArtifacts.values().stream()
            .filter(a -> a.getArtifactName().equals(currNewArtifact.getArtifactName())).findFirst();
        if (foundArtifact.isPresent()) {
            if (foundArtifact.get().getArtifactType().equals(currNewArtifact.getArtifactType())) {
                updateFoundArtifact(artifactsToUpdate, currNewArtifact, foundArtifact.get());
                existingArtifacts.remove(foundArtifact.get().getArtifactLabel());
                artifactsToUpload.remove(currNewArtifact);
            } else {
                log.debug("Can't upload two artifact with the same name {}.", currNewArtifact.getArtifactName());
                throw new ByActionStatusComponentException(ActionStatus.ARTIFACT_ALREADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR,
                    currNewArtifact.getArtifactName(), currNewArtifact.getArtifactType(), foundArtifact.get().getArtifactType());
            }
        }
    }

    private void updateFoundArtifact(List<ArtifactDefinition> artifactsToUpdate, ArtifactDefinition currNewArtifact,
                                     ArtifactDefinition foundArtifact) {
        if (!foundArtifact.getArtifactChecksum().equals(currNewArtifact.getArtifactChecksum())) {
            foundArtifact.setPayload(currNewArtifact.getPayloadData());
            foundArtifact.setPayloadData(Base64.encodeBase64String(currNewArtifact.getPayloadData()));
            foundArtifact.setArtifactChecksum(GeneralUtility.calculateMD5Base64EncodedByByteArray(currNewArtifact.getPayloadData()));
            artifactsToUpdate.add(foundArtifact);
        }
    }

    private Map<String, ArtifactDefinition> collectExistingArtifacts(Resource curNodeType) {
        Map<String, ArtifactDefinition> existingArtifacts = new HashMap<>();
        if (curNodeType == null) {
            return existingArtifacts;
        }
        if (MapUtils.isNotEmpty(curNodeType.getDeploymentArtifacts())) {
            existingArtifacts.putAll(curNodeType.getDeploymentArtifacts());
        }
        if (MapUtils.isNotEmpty(curNodeType.getArtifacts())) {
            existingArtifacts.putAll(
                curNodeType.getArtifacts().entrySet().stream().filter(e -> e.getValue().getArtifactGroupType() == ArtifactGroupTypeEnum.INFORMATIONAL)
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
        return existingArtifacts;
    }

    /**
     * Changes resource life cycle state to checked out
     *
     * @param resource
     * @param user
     * @param inTransaction
     * @return
     */
    private Either<Resource, ResponseFormat> checkoutResource(Resource resource, User user, boolean inTransaction) {
        Either<Resource, ResponseFormat> checkoutResourceRes;
        try {
            if (!resource.getComponentMetadataDefinition().getMetadataDataDefinition().getState()
                .equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
                log.debug("************* Going to change life cycle state of resource {} to not certified checked out. ", resource.getName());
                Either<? extends Component, ResponseFormat> checkoutRes = lifecycleBusinessLogic
                    .changeComponentState(resource.getComponentType(), resource.getUniqueId(), user, LifeCycleTransitionEnum.CHECKOUT,
                        new LifecycleChangeInfoWithAction(CERTIFICATION_ON_IMPORT, LifecycleChanceActionEnum.CREATE_FROM_CSAR), inTransaction, true);
                if (checkoutRes.isRight()) {
                    log.debug("Could not change state of component {} with uid {} to checked out. Status is {}. ",
                        resource.getComponentType().getNodeType(), resource.getUniqueId(), checkoutRes.right().value().getStatus());
                    checkoutResourceRes = Either.right(checkoutRes.right().value());
                } else {
                    checkoutResourceRes = Either.left((Resource) checkoutRes.left().value());
                }
            } else {
                checkoutResourceRes = Either.left(resource);
            }
        } catch (Exception e) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            checkoutResourceRes = Either.right(responseFormat);
            log.debug("Exception occurred when checkoutResource {} , error is:{}", resource.getName(), e.getMessage(), e);
        }
        return checkoutResourceRes;
    }

    /**
     * Handles Artifacts of NodeType
     *
     * @param nodeTypeResource
     * @param nodeTypeArtifactsToHandle
     * @param user
     * @param inTransaction
     * @return
     */
    public Either<List<ArtifactDefinition>, ResponseFormat> handleNodeTypeArtifacts(Resource nodeTypeResource,
                                                                                    Map<ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle,
                                                                                    List<ArtifactDefinition> createdArtifacts, User user,
                                                                                    boolean inTransaction, boolean ignoreLifecycleState) {
        List<ArtifactDefinition> handleNodeTypeArtifactsRequestRes;
        Either<List<ArtifactDefinition>, ResponseFormat> handleNodeTypeArtifactsRes = null;
        Either<Resource, ResponseFormat> changeStateResponse;
        try {
            changeStateResponse = checkoutResource(nodeTypeResource, user, inTransaction);
            if (changeStateResponse.isRight()) {
                return Either.right(changeStateResponse.right().value());
            }
            nodeTypeResource = changeStateResponse.left().value();
            List<ArtifactDefinition> handledNodeTypeArtifacts = new ArrayList<>();
            log.debug("************* Going to handle artifacts of node type resource {}. ", nodeTypeResource.getName());
            for (Entry<ArtifactOperationEnum, List<ArtifactDefinition>> curOperationEntry : nodeTypeArtifactsToHandle.entrySet()) {
                ArtifactOperationEnum curOperation = curOperationEntry.getKey();
                List<ArtifactDefinition> curArtifactsToHandle = curOperationEntry.getValue();
                if (curArtifactsToHandle != null && !curArtifactsToHandle.isEmpty()) {
                    log.debug("************* Going to {} artifact to vfc {}", curOperation.name(), nodeTypeResource.getName());
                    handleNodeTypeArtifactsRequestRes = artifactsBusinessLogic
                        .handleArtifactsRequestForInnerVfcComponent(curArtifactsToHandle, nodeTypeResource, user, createdArtifacts,
                            new ArtifactOperationInfo(false, ignoreLifecycleState, curOperation), false, inTransaction);
                    if (ArtifactOperationEnum.isCreateOrLink(curOperation)) {
                        createdArtifacts.addAll(handleNodeTypeArtifactsRequestRes);
                    }
                    handledNodeTypeArtifacts.addAll(handleNodeTypeArtifactsRequestRes);
                }
            }
            handleNodeTypeArtifactsRes = Either.left(handledNodeTypeArtifacts);
        } catch (Exception e) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            handleNodeTypeArtifactsRes = Either.right(responseFormat);
            log.debug("Exception occurred when handleVfcArtifacts, error is:{}", e.getMessage(), e);
        }
        return handleNodeTypeArtifactsRes;
    }

    private Map<String, ImmutablePair<String, String>> extractVfcToscaNames(final Map<String, NodeTypeInfo> nodeTypesInfo,
                                                                            final String vfResourceName, final CsarInfo csarInfo) {
        final Map<String, ImmutablePair<String, String>> vfcToscaNames = new HashMap<>();
        final Map<String, Object> nodes = extractAllNodes(nodeTypesInfo, csarInfo);
        if (!nodes.isEmpty()) {
            for (Entry<String, Object> nodeType : nodes.entrySet()) {
                final ImmutablePair<String, String> toscaResourceName = buildNestedToscaResourceName(ResourceTypeEnum.VFC.name(), vfResourceName,
                    nodeType.getKey());
                vfcToscaNames.put(nodeType.getKey(), toscaResourceName);
            }
        }
        for (final NodeTypeInfo cvfc : nodeTypesInfo.values()) {
            vfcToscaNames.put(cvfc.getType(), buildNestedToscaResourceName(ResourceTypeEnum.CVFC.name(), vfResourceName, cvfc.getType()));
        }
        return vfcToscaNames;
    }

    private Map<String, Object> extractAllNodes(Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo) {
        Map<String, Object> nodes = new HashMap<>();
        for (NodeTypeInfo nodeTypeInfo : nodeTypesInfo.values()) {
            extractNodeTypes(nodes, nodeTypeInfo.getMappedToscaTemplate());
        }
        extractNodeTypes(nodes, csarInfo.getMappedToscaMainTemplate());
        return nodes;
    }

    private void extractNodeTypes(Map<String, Object> nodes, Map<String, Object> mappedToscaTemplate) {
        Either<Map<String, Object>, ResultStatusEnum> eitherNodeTypes = ImportUtils
            .findFirstToscaMapElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.NODE_TYPES);
        if (eitherNodeTypes.isLeft()) {
            nodes.putAll(eitherNodeTypes.left().value());
        }
    }

    public Resource createResourceFromCsar(Resource resource, User user, Map<String, byte[]> csarUIPayload, String csarUUID) {
        log.trace("************* created successfully from YAML, resource TOSCA ");
        loggerSupportability
            .log(LoggerSupportabilityActions.CREATE_RESOURCE_FROM_YAML, StatusCode.STARTED, "Starting to create Resource From Csar by user {}",
                user.getUserId());
        CsarInfo csarInfo = csarBusinessLogic.getCsarInfo(resource, null, user, csarUIPayload, csarUUID);
        Map<String, NodeTypeInfo> nodeTypesInfo = csarInfo.extractTypesInfo();
        final String model = resource.getModel();
        if (StringUtils.isNotEmpty(model)) {
            final Map<String, Object> dataTypesToCreate = getDatatypesToCreate(model, csarInfo.getDataTypes());
            final Map<String, Object> policyTypesToCreate = getPolicytypesToCreate(model, csarInfo.getPolicyTypes());
            if (MapUtils.isNotEmpty(dataTypesToCreate) || MapUtils.isNotEmpty(policyTypesToCreate)) {
                createModel(resource, csarInfo.getVfResourceName());
            }
            if (MapUtils.isNotEmpty(dataTypesToCreate)) {
                dataTypeBusinessLogic.createDataTypeFromYaml(new Yaml().dump(dataTypesToCreate), model, true);
            }
            if (MapUtils.isNotEmpty(policyTypesToCreate)) {
                policyTypeBusinessLogic.createPolicyTypeFromYaml(new Yaml().dump(policyTypesToCreate), model, true);
            }
        }

        Either<Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>>, ResponseFormat> findNodeTypesArtifactsToHandleRes = findNodeTypesArtifactsToHandle(
            nodeTypesInfo, csarInfo, resource);
        if (findNodeTypesArtifactsToHandleRes.isRight()) {
            log.debug("failed to find node types for update with artifacts during import csar {}. ", csarInfo.getCsarUUID());
            loggerSupportability
                .log(LoggerSupportabilityActions.CREATE_RESOURCE_FROM_YAML, resource.getComponentMetadataForSupportLog(), StatusCode.ERROR,
                    "error: {}", findNodeTypesArtifactsToHandleRes.right().value());
            throw new ByResponseFormatComponentException(findNodeTypesArtifactsToHandleRes.right().value());
        }
        Resource vfResource = createResourceFromYaml(resource, csarInfo.getMainTemplateContent(), csarInfo.getMainTemplateName(), nodeTypesInfo,
            csarInfo, findNodeTypesArtifactsToHandleRes.left().value(), true, false, null);
        log.trace("*************VF Resource created successfully from YAML, resource TOSCA name: {}", vfResource.getToscaResourceName());
        loggerSupportability
            .log(LoggerSupportabilityActions.CREATE_RESOURCE_FROM_YAML, StatusCode.COMPLETE, "Ended create Resource From Csar by user {}",
                user.getUserId());
        return vfResource;
    }

    private Resource validateResourceBeforeCreate(Resource resource, User user, boolean inTransaction) {
        log.trace("validating resource before create");
        user.copyData(validateUser(user, CREATE_RESOURCE, resource, AuditingActionEnum.CREATE_RESOURCE, false));
        // validate user role
        validateUserRole(user, resource, new ArrayList<>(), AuditingActionEnum.CREATE_RESOURCE, null);
        // VF / PNF "derivedFrom" should be null (or ignored)
        if (ModelConverter.isAtomicComponent(resource)) {
            validateDerivedFromNotEmpty(user, resource, AuditingActionEnum.CREATE_RESOURCE);
        }
        return validateResourceBeforeCreate(resource, user, AuditingActionEnum.CREATE_RESOURCE, inTransaction, null);
    }

    private Resource createResourceFromYaml(Resource resource, String topologyTemplateYaml, String yamlName, Map<String, NodeTypeInfo> nodeTypesInfo,
                                            CsarInfo csarInfo,
                                            Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate,
                                            boolean shouldLock, boolean inTransaction, String nodeName) {
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Resource createdResource;
        try {
            ParsedToscaYamlInfo parsedToscaYamlInfo = csarBusinessLogic
                .getParsedToscaYamlInfo(topologyTemplateYaml, yamlName, nodeTypesInfo, csarInfo, nodeName, resource);
            if (MapUtils.isEmpty(parsedToscaYamlInfo.getInstances()) && resource.getResourceType() != ResourceTypeEnum.PNF) {
                throw new ByActionStatusComponentException(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
            }
            log.debug("#createResourceFromYaml - Going to create resource {} and RIs ", resource.getName());
            loggerSupportability
                .log(LoggerSupportabilityActions.CREATE_RESOURCE_FROM_YAML, resource.getComponentMetadataForSupportLog(), StatusCode.STARTED, "");
            createdResource = createResourceAndRIsFromYaml(yamlName, resource, parsedToscaYamlInfo, AuditingActionEnum.IMPORT_RESOURCE, false,
                createdArtifacts, topologyTemplateYaml, nodeTypesInfo, csarInfo, nodeTypesArtifactsToCreate, shouldLock, inTransaction, nodeName);
            log.debug("#createResourceFromYaml - The resource {} has been created ", resource.getName());
            loggerSupportability
                .log(LoggerSupportabilityActions.CREATE_RESOURCE_FROM_YAML, resource.getComponentMetadataForSupportLog(), StatusCode.COMPLETE,
                    "The resource has been created: {}", resource.getName());
        } catch (ComponentException e) {
            ResponseFormat responseFormat =
                e.getResponseFormat() == null ? componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams()) : e.getResponseFormat();
            componentsUtils.auditResource(responseFormat, csarInfo.getModifier(), resource, AuditingActionEnum.IMPORT_RESOURCE);
            throw e;
        } catch (StorageException e) {
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(e.getStorageOperationStatus()));
            componentsUtils.auditResource(responseFormat, csarInfo.getModifier(), resource, AuditingActionEnum.IMPORT_RESOURCE);
            throw e;
        }
        return createdResource;
    }

    public Map<String, Resource> createResourcesFromYamlNodeTypesList(String yamlName, Resource resource, Map<String, Object> mappedToscaTemplate,
                                                                      boolean needLock,
                                                                      Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle,
                                                                      List<ArtifactDefinition> nodeTypesNewCreatedArtifacts,
                                                                      Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo,
                                                                      final String substitutableAsNodeType) {
        Either<String, ResultStatusEnum> toscaVersion = findFirstToscaStringElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.TOSCA_VERSION);
        if (toscaVersion.isRight()) {
            throw new ByActionStatusComponentException(ActionStatus.INVALID_TOSCA_TEMPLATE);
        }
        Map<String, Object> mapToConvert = new HashMap<>();
        mapToConvert.put(TypeUtils.ToscaTagNamesEnum.TOSCA_VERSION.getElementName(), toscaVersion.left().value());
        final Map<String, Object> nodeTypes = getNodeTypesFromTemplate(mappedToscaTemplate, substitutableAsNodeType);
        createNodeTypes(yamlName, resource, needLock, nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo, mapToConvert,
            nodeTypes);
        return csarInfo.getCreatedNodes();
    }

    private Map<String, Object> getNodeTypesFromTemplate(final Map<String, Object> mappedToscaTemplate, final String substitutableAsNodeType) {
        final Map<String, Object> nodeTypes = getAllNodeTypesInTemplate(mappedToscaTemplate);
        if (StringUtils.isNotEmpty(substitutableAsNodeType)) {
            nodeTypes.remove(substitutableAsNodeType);
        }
        return nodeTypes;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getSubstitutableAsNodeTypeFromTemplate(final Map<String, Object> mappedToscaTemplate,
                                                                       final String substitutableAsNodeType) {
        return (Map<String, Object>) getAllNodeTypesInTemplate(mappedToscaTemplate).get(substitutableAsNodeType);
    }

    private Map<String, Object> getAllNodeTypesInTemplate(final Map<String, Object> mappedToscaTemplate) {
        return ImportUtils.findFirstToscaMapElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.NODE_TYPES).left().orValue(HashMap::new);
    }

    private void createModel(final Resource resource, final String vfResourcename) {
        final String nameForGeneratedModel = resource.getModel() + "_" + vfResourcename + resource.getCsarVersion();
        Model model = new Model(nameForGeneratedModel, resource.getModel(), ModelTypeEnum.NORMATIVE_EXTENSION);
        modelBusinessLogic.createModel(model);
        resource.setModel(nameForGeneratedModel);
    }

    private Map<String, Object> getDatatypesToCreate(final String model, final Map<String, Object> dataTypes) {
        final Map<String, Object> dataTypesToCreate = new HashMap<>();
        for (final String dataType : dataTypes.keySet()) {
            final Either<DataTypeDefinition, StorageOperationStatus> result =
                propertyOperation.getDataTypeByName(dataType, model);
            if (result.isRight() && result.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
                dataTypesToCreate.put(dataType, dataTypes.get(dataType));
            }
        }
        return dataTypesToCreate;
    }

    private Map<String, Object> getPolicytypesToCreate(final String model, final Map<String, Object> policyTypes) {
        final Map<String, Object> policyTypesToCreate = new HashMap<>();
        for (final String policyType : policyTypes.keySet()) {
            final Either<PolicyTypeDefinition, StorageOperationStatus> result =
                policyTypeOperation.getLatestPolicyTypeByType(policyType, model);
            if (result.isRight() && result.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
                policyTypesToCreate.put(policyType, policyTypes.get(policyType));
            }
        }
        return policyTypesToCreate;
    }

    private void createNodeTypes(String yamlName, Resource resource, boolean needLock,
                                 Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle,
                                 List<ArtifactDefinition> nodeTypesNewCreatedArtifacts, Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo,
                                 Map<String, Object> mapToConvert, Map<String, Object> nodeTypes) {
        Iterator<Entry<String, Object>> nodesNameValueIter = nodeTypes.entrySet().iterator();
        Resource vfcCreated = null;
        while (nodesNameValueIter.hasNext()) {
            Entry<String, Object> nodeType = nodesNameValueIter.next();
            Map<ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle =
                nodeTypesArtifactsToHandle == null || nodeTypesArtifactsToHandle.isEmpty() ? null : nodeTypesArtifactsToHandle.get(nodeType.getKey());
            if (nodeTypesInfo.containsKey(nodeType.getKey())) {
                log.trace("************* Going to handle nested vfc {}", nodeType.getKey());
                vfcCreated = handleNestedVfc(resource, nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo,
                    nodeType.getKey());
                log.trace("************* Finished to handle nested vfc {}", nodeType.getKey());
            } else if (csarInfo.getCreatedNodesToscaResourceNames() != null && !csarInfo.getCreatedNodesToscaResourceNames()
                .containsKey(nodeType.getKey())) {
                log.trace("************* Going to create node {}", nodeType.getKey());
                ImmutablePair<Resource, ActionStatus> resourceCreated = createNodeTypeResourceFromYaml(yamlName, nodeType, csarInfo.getModifier(),
                    mapToConvert, resource, needLock, nodeTypeArtifactsToHandle, nodeTypesNewCreatedArtifacts, true, csarInfo, true);
                log.debug("************* Finished to create node {}", nodeType.getKey());
                vfcCreated = resourceCreated.getLeft();
                csarInfo.getCreatedNodesToscaResourceNames().put(nodeType.getKey(), vfcCreated.getToscaResourceName());
            }
            if (vfcCreated != null) {
                csarInfo.getCreatedNodes().put(nodeType.getKey(), vfcCreated);
            }
            mapToConvert.remove(TypeUtils.ToscaTagNamesEnum.NODE_TYPES.getElementName());
        }
    }

    private Resource handleNestedVfc(Resource resource, Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodesArtifactsToHandle,
                                     List<ArtifactDefinition> createdArtifacts, Map<String, NodeTypeInfo> nodesInfo, CsarInfo csarInfo,
                                     String nodeName) {
        String yamlName = nodesInfo.get(nodeName).getTemplateFileName();
        Map<String, Object> nestedVfcJsonMap = nodesInfo.get(nodeName).getMappedToscaTemplate();
        log.debug("************* Going to create node types from yaml {}", yamlName);
        createResourcesFromYamlNodeTypesList(yamlName, resource, nestedVfcJsonMap, false, nodesArtifactsToHandle, createdArtifacts,
            Collections.emptyMap(), csarInfo, resource.getModel());
        log.debug("************* Finished to create node types from yaml {}", yamlName);
        if (nestedVfcJsonMap.containsKey(TypeUtils.ToscaTagNamesEnum.TOPOLOGY_TEMPLATE.getElementName())) {
            log.debug("************* Going to handle complex VFC from yaml {}", yamlName);
            resource = handleComplexVfc(resource, nodesArtifactsToHandle, createdArtifacts, nodesInfo, csarInfo, nodeName, yamlName);
        }
        return resource;
    }

    private Resource handleComplexVfc(final Resource resource,
                                      final Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodesArtifactsToHandle,
                                      final List<ArtifactDefinition> createdArtifacts, Map<String, NodeTypeInfo> nodesInfo, CsarInfo csarInfo,
                                      final String nodeName, final String yamlName) {
        Resource oldComplexVfc = null;
        Resource newComplexVfc = buildValidComplexVfc(resource, csarInfo, nodeName, nodesInfo);
        Either<Resource, StorageOperationStatus> oldComplexVfcRes = toscaOperationFacade
            .getFullLatestComponentByToscaResourceName(newComplexVfc.getToscaResourceName());
        if (oldComplexVfcRes.isRight() && oldComplexVfcRes.right().value() == StorageOperationStatus.NOT_FOUND) {
            oldComplexVfcRes = toscaOperationFacade.getFullLatestComponentByToscaResourceName(
                buildNestedToscaResourceName(ResourceTypeEnum.CVFC.name(), csarInfo.getVfResourceName(), nodeName).getRight());
        }
        if (oldComplexVfcRes.isRight() && oldComplexVfcRes.right().value() != StorageOperationStatus.NOT_FOUND) {
            log.debug("Failed to fetch previous complex VFC by tosca resource name {}. Status is {}. ", newComplexVfc.getToscaResourceName(),
                oldComplexVfcRes.right().value());
            throw new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR);
        } else if (oldComplexVfcRes.isLeft()) {
            log.debug(VALIDATE_DERIVED_BEFORE_UPDATE);
            final Either<Boolean, ResponseFormat> eitherValidation = validateNestedDerivedFromDuringUpdate(oldComplexVfcRes.left().value(),
                newComplexVfc, ValidationUtils.hasBeenCertified(oldComplexVfcRes.left().value().getVersion()));
            if (eitherValidation.isLeft()) {
                oldComplexVfc = oldComplexVfcRes.left().value();
            }
        }
        newComplexVfc = handleComplexVfc(nodesArtifactsToHandle, createdArtifacts, nodesInfo, csarInfo, nodeName, yamlName, oldComplexVfc,
            newComplexVfc);
        csarInfo.getCreatedNodesToscaResourceNames().put(nodeName, newComplexVfc.getToscaResourceName());
        final LifecycleChangeInfoWithAction lifecycleChangeInfo = new LifecycleChangeInfoWithAction(CERTIFICATION_ON_IMPORT,
            LifecycleChanceActionEnum.CREATE_FROM_CSAR);
        log.debug("Going to certify cvfc {}. ", newComplexVfc.getName());
        final Resource result = propagateStateToCertified(csarInfo.getModifier(), newComplexVfc, lifecycleChangeInfo, true, false, true);
        csarInfo.getCreatedNodes().put(nodeName, result);
        csarInfo.removeNodeFromQueue();
        return result;
    }

    private Resource handleComplexVfc(Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodesArtifactsToHandle,
                                      List<ArtifactDefinition> createdArtifacts, Map<String, NodeTypeInfo> nodesInfo, CsarInfo csarInfo,
                                      String nodeName, String yamlName, Resource oldComplexVfc, Resource newComplexVfc) {
        Resource handleComplexVfcRes;
        Map<String, Object> mappedToscaTemplate = nodesInfo.get(nodeName).getMappedToscaTemplate();
        String yamlContent = new String(csarInfo.getCsar().get(yamlName));
        Map<String, NodeTypeInfo> newNodeTypesInfo = nodesInfo.entrySet().stream().collect(toMap(Entry::getKey, e -> e.getValue().getUnmarkedCopy()));
        CsarInfo.markNestedVfc(mappedToscaTemplate, newNodeTypesInfo);
        if (oldComplexVfc == null) {
            handleComplexVfcRes = createResourceFromYaml(newComplexVfc, yamlContent, yamlName, newNodeTypesInfo, csarInfo, nodesArtifactsToHandle,
                false, true, nodeName);
        } else {
            handleComplexVfcRes = updateResourceFromYaml(oldComplexVfc, newComplexVfc, AuditingActionEnum.UPDATE_RESOURCE_METADATA, createdArtifacts,
                yamlContent, yamlName, csarInfo, newNodeTypesInfo, nodesArtifactsToHandle, nodeName, true);
        }
        return handleComplexVfcRes;
    }

    private Resource buildValidComplexVfc(Resource resource, CsarInfo csarInfo, String nodeName, Map<String, NodeTypeInfo> nodesInfo) {
        Resource complexVfc = buildComplexVfcMetadata(resource, csarInfo, nodeName, nodesInfo);
        log.debug("************* Going to validate complex VFC from yaml {}", complexVfc.getName());
        csarInfo.addNodeToQueue(nodeName);
        return validateResourceBeforeCreate(complexVfc, csarInfo.getModifier(), AuditingActionEnum.IMPORT_RESOURCE, true, csarInfo);
    }

    private String getNodeTypeActualName(final String nodeTypefullName, final String nodeTypeNamePrefix) {
        final String nameWithouNamespacePrefix = nodeTypefullName.substring(nodeTypeNamePrefix.length());
        final String[] findTypes = nameWithouNamespacePrefix.split("\\.");
        if (findTypes.length > 1) {
            final String resourceType = findTypes[0];
            return nameWithouNamespacePrefix.substring(resourceType.length());
        }
        return nameWithouNamespacePrefix;
    }

    private ImmutablePair<Resource, ActionStatus> createNodeTypeResourceFromYaml(final String yamlName, final Entry<String, Object> nodeNameValue,
                                                                                 User user, final Map<String, Object> mapToConvert,
                                                                                 final Resource resourceVf, final boolean needLock,
                                                                                 final Map<ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle,
                                                                                 final List<ArtifactDefinition> nodeTypesNewCreatedArtifacts,
                                                                                 final boolean forceCertificationAllowed, final CsarInfo csarInfo,
                                                                                 final boolean isNested) {
        final UploadResourceInfo resourceMetaData = fillResourceMetadata(yamlName, resourceVf, nodeNameValue.getKey(), user);
        final String singleVfcYaml = buildNodeTypeYaml(nodeNameValue, mapToConvert, resourceMetaData.getResourceType(), csarInfo);
        user = validateUser(user, "CheckIn Resource", resourceVf, AuditingActionEnum.CHECKIN_RESOURCE, true);
        return createResourceFromNodeType(singleVfcYaml, resourceMetaData, user, true, needLock, nodeTypeArtifactsToHandle,
            nodeTypesNewCreatedArtifacts, forceCertificationAllowed, csarInfo, nodeNameValue.getKey(), isNested);
    }

    private String buildNodeTypeYaml(final Entry<String, Object> nodeNameValue, final Map<String, Object> mapToConvert, final String nodeResourceType,
                                     final CsarInfo csarInfo) {
        // We need to create a Yaml from each node_types in order to create

        // resource from each node type using import normative flow.
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        final Yaml yaml = new Yaml(options);
        final Map<String, Object> node = new HashMap<>();
        node.put(buildNestedToscaResourceName(nodeResourceType, csarInfo.getVfResourceName(), nodeNameValue.getKey()).getLeft(),
            nodeNameValue.getValue());
        mapToConvert.put(TypeUtils.ToscaTagNamesEnum.NODE_TYPES.getElementName(), node);
        return yaml.dumpAsMap(mapToConvert);
    }

    public Boolean validateResourceCreationFromNodeType(Resource resource, User creator) {
        validateDerivedFromNotEmpty(creator, resource, AuditingActionEnum.CREATE_RESOURCE);
        return true;
    }

    public ImmutablePair<Resource, ActionStatus> createResourceFromNodeType(String nodeTypeYaml, UploadResourceInfo resourceMetaData, User creator,
                                                                            boolean isInTransaction, boolean needLock,
                                                                            Map<ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle,
                                                                            List<ArtifactDefinition> nodeTypesNewCreatedArtifacts,
                                                                            boolean forceCertificationAllowed, CsarInfo csarInfo, String nodeName,
                                                                            boolean isNested) {
        LifecycleChangeInfoWithAction lifecycleChangeInfo = new LifecycleChangeInfoWithAction(CERTIFICATION_ON_IMPORT,
            LifecycleChanceActionEnum.CREATE_FROM_CSAR);
        Function<Resource, Boolean> validator = resource -> validateResourceCreationFromNodeType(resource, creator);
        return resourceImportManager
            .importCertifiedResource(nodeTypeYaml, resourceMetaData, creator, validator, lifecycleChangeInfo, isInTransaction, true, needLock,
                nodeTypeArtifactsToHandle, nodeTypesNewCreatedArtifacts, forceCertificationAllowed, csarInfo, nodeName, isNested);
    }

    /**
     * Validates if a given node type name has a valid prefix.
     *
     * @param nodeName                     node name from definition file
     * @param definedResourceNamespaceList is a list of all node type name prefix allowed
     * @return a valid node type name prefix if it`s found
     */
    public Optional<String> validateNodeTypeNamePrefix(final String nodeName, final List<String> definedResourceNamespaceList) {
        for (final String validNamespace : definedResourceNamespaceList) {
            if (nodeName.startsWith(validNamespace)) {
                return Optional.of(validNamespace);
            }
        }
        return Optional.empty();
    }

    private List<String> getDefinedNodeTypeNamespaceList() {
        return ConfigurationManager.getConfigurationManager().getConfiguration().getDefinedResourceNamespace();
    }

    private UploadResourceInfo fillResourceMetadata(final String yamlName, final Resource resourceVf, final String nodeName, final User user) {
        final UploadResourceInfo resourceMetaData = new UploadResourceInfo();
        final String nodeTypeNamePrefix = getNodeTypeNamePrefix(nodeName);
        log.debug("Node type Name prefix {}", nodeTypeNamePrefix);
        if (!nodeName.startsWith(nodeTypeNamePrefix)) {
            log.debug("invalid nodeName:{} does not start with {}.", nodeName, getDefinedNodeTypeNamespaceList());
            throw new ByActionStatusComponentException(ActionStatus.INVALID_NODE_TEMPLATE, yamlName, resourceMetaData.getName(), nodeName);
        }
        final String actualName = this.getNodeTypeActualName(nodeName, nodeTypeNamePrefix);
        final String namePrefix = nodeName.replace(actualName, "");
        String resourceType = namePrefix.substring(nodeTypeNamePrefix.length());
        log.debug("initial  namePrefix:{} resourceType {}. nodeName {} , actualName {} prefix {}", namePrefix, resourceType, nodeName, actualName,
            nodeTypeNamePrefix);
        // if we import from csar, the node_type name can be

        // org.openecomp.resource.abstract.node_name - in this case we always

        // create a vfc
        if (resourceType.equals(Constants.ABSTRACT)) {
            resourceType = ResourceTypeEnum.VFC.name().toLowerCase();
        }
        if (!ResourceTypeEnum.containsIgnoreCase(resourceType)) {
            resourceType = ResourceTypeEnum.VFC.name().toLowerCase();
        }
        // validating type
        if (!ResourceTypeEnum.containsName(resourceType.toUpperCase())) {
            log.debug("invalid resourceType:{} the type is not one of the valide types:{}.", resourceType.toUpperCase(), ResourceTypeEnum.values());
            throw new ByActionStatusComponentException(ActionStatus.INVALID_NODE_TEMPLATE, yamlName, resourceMetaData.getName(), nodeName);
        }
        // Setting name
        resourceMetaData.setName(resourceVf.getSystemName() + actualName);
        // Setting type from name
        final String type = resourceType.toUpperCase();
        resourceMetaData.setResourceType(type);
        resourceMetaData.setDescription(ImportUtils.Constants.INNER_VFC_DESCRIPTION);
        resourceMetaData.setIcon(ImportUtils.Constants.DEFAULT_ICON);
        resourceMetaData.setContactId(user.getUserId());
        resourceMetaData.setVendorName(resourceVf.getVendorName());
        resourceMetaData.setVendorRelease(resourceVf.getVendorRelease());
        resourceMetaData.setModel(resourceVf.getModel());
        // Setting tag
        final List<String> tags = new ArrayList<>();
        tags.add(resourceMetaData.getName());
        resourceMetaData.setTags(tags);
        // Setting category
        final CategoryDefinition category = new CategoryDefinition();
        category.setName(ImportUtils.Constants.ABSTRACT_CATEGORY_NAME);
        final SubCategoryDefinition subCategory = new SubCategoryDefinition();
        subCategory.setName(ImportUtils.Constants.ABSTRACT_SUBCATEGORY);
        category.addSubCategory(subCategory);
        final List<CategoryDefinition> categories = new ArrayList<>();
        categories.add(category);
        resourceMetaData.setCategories(categories);
        return resourceMetaData;
    }

    private Resource buildComplexVfcMetadata(final Resource resourceVf, final CsarInfo csarInfo, final String nodeName,
                                             final Map<String, NodeTypeInfo> nodesInfo) {
        final Resource cvfc = new Resource();
        final NodeTypeInfo nodeTypeInfo = nodesInfo.get(nodeName);
        cvfc.setName(buildCvfcName(csarInfo.getVfResourceName(), nodeName));
        cvfc.setNormalizedName(ValidationUtils.normaliseComponentName(cvfc.getName()));
        cvfc.setSystemName(ValidationUtils.convertToSystemName(cvfc.getName()));
        cvfc.setResourceType(ResourceTypeEnum.CVFC);
        cvfc.setAbstract(true);
        cvfc.setDerivedFrom(nodeTypeInfo.getDerivedFrom());
        cvfc.setDescription(ImportUtils.Constants.CVFC_DESCRIPTION);
        cvfc.setIcon(ImportUtils.Constants.DEFAULT_ICON);
        cvfc.setContactId(csarInfo.getModifier().getUserId());
        cvfc.setCreatorUserId(csarInfo.getModifier().getUserId());
        cvfc.setVendorName(resourceVf.getVendorName());
        cvfc.setVendorRelease(resourceVf.getVendorRelease());
        cvfc.setModel(resourceVf.getModel());
        cvfc.setResourceVendorModelNumber(resourceVf.getResourceVendorModelNumber());
        cvfc.setToscaResourceName(buildNestedToscaResourceName(ResourceTypeEnum.CVFC.name(), csarInfo.getVfResourceName(), nodeName).getLeft());
        cvfc.setInvariantUUID(UniqueIdBuilder.buildInvariantUUID());
        final List<String> tags = new ArrayList<>();
        tags.add(cvfc.getName());
        cvfc.setTags(tags);
        final CategoryDefinition category = new CategoryDefinition();
        category.setName(ImportUtils.Constants.ABSTRACT_CATEGORY_NAME);
        SubCategoryDefinition subCategory = new SubCategoryDefinition();
        subCategory.setName(ImportUtils.Constants.ABSTRACT_SUBCATEGORY);
        category.addSubCategory(subCategory);
        final List<CategoryDefinition> categories = new ArrayList<>();
        categories.add(category);
        cvfc.setCategories(categories);
        cvfc.setVersion(ImportUtils.Constants.FIRST_NON_CERTIFIED_VERSION);
        cvfc.setLifecycleState(ImportUtils.Constants.NORMATIVE_TYPE_LIFE_CYCLE_NOT_CERTIFIED_CHECKOUT);
        cvfc.setHighestVersion(ImportUtils.Constants.NORMATIVE_TYPE_HIGHEST_VERSION);
        return cvfc;
    }

    private String buildCvfcName(final String resourceVfName, final String nodeName) {
        String nameWithouNamespacePrefix = nodeName.substring(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX.length());
        String[] findTypes = nameWithouNamespacePrefix.split("\\.");
        String resourceType = findTypes[0];
        String resourceName = resourceVfName + "-" + nameWithouNamespacePrefix.substring(resourceType.length() + 1);
        return addCvfcSuffixToResourceName(resourceName);
    }

    private Resource createResourceAndRIsFromYaml(final String yamlName, Resource resource, final ParsedToscaYamlInfo parsedToscaYamlInfo,
                                                  final AuditingActionEnum actionEnum, final boolean isNormative,
                                                  final List<ArtifactDefinition> createdArtifacts, final String topologyTemplateYaml,
                                                  final Map<String, NodeTypeInfo> nodeTypesInfo, final CsarInfo csarInfo,
                                                  final Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate,
                                                  final boolean shouldLock, final boolean inTransaction, final String nodeName) {
        final List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
        if (shouldLock) {
            final Either<Boolean, ResponseFormat> lockResult = lockComponentByName(resource.getSystemName(), resource, CREATE_RESOURCE);
            if (lockResult.isRight()) {
                rollback(inTransaction, resource, createdArtifacts, nodeTypesNewCreatedArtifacts);
                throw new ByResponseFormatComponentException(lockResult.right().value());
            }
            log.debug("name is locked {} status = {}", resource.getSystemName(), lockResult);
        }
        try {
            log.trace("************* createResourceFromYaml before full create resource {}", yamlName);
            loggerSupportability.log(LoggerSupportabilityActions.CREATE_INPUTS, resource.getComponentMetadataForSupportLog(), StatusCode.STARTED,
                "Starting to add inputs from yaml: {}", yamlName);
            if (processSubstitutableAsNodeType(resource, parsedToscaYamlInfo)) {
                final Map<String, Object> substitutableAsNodeType = getSubstitutableAsNodeTypeFromTemplate(
                    (Map<String, Object>) new Yaml().load(topologyTemplateYaml), parsedToscaYamlInfo.getSubstitutionMappingNodeType());
                resource.setToscaResourceName(parsedToscaYamlInfo.getSubstitutionMappingNodeType());
                final Resource genericResource = fetchAndSetDerivedFromGenericType(resource,
                    (String) substitutableAsNodeType.get(TypeUtils.ToscaTagNamesEnum.DERIVED_FROM.getElementName()));
                resource = createResourceTransaction(resource, csarInfo.getModifier(), isNormative);
                generatePropertiesFromGenericType(resource, genericResource);
                generatePropertiesFromNodeType(resource, substitutableAsNodeType);
                final String resourceId = resource.getUniqueId();
                resource.getProperties().forEach(propertyDefinition -> propertyDefinition.setUniqueId(
                    UniqueIdBuilder.buildPropertyUniqueId(resourceId, propertyDefinition.getName())));

                createResourcePropertiesOnGraph(resource);
                final Map<String, UploadComponentInstanceInfo> instancesToCreate = getInstancesToCreate(parsedToscaYamlInfo, resource.getModel());

                log.trace("************* Going to create nodes, RI's and Relations  from yaml {}", yamlName);
                loggerSupportability
                    .log(LoggerSupportabilityActions.CREATE_RESOURCE_FROM_YAML, resource.getComponentMetadataForSupportLog(), StatusCode.STARTED,
                        "Start create nodes, RI and Relations  from yaml: {}", yamlName);
                resource = createRIAndRelationsFromYaml(yamlName, resource, instancesToCreate, topologyTemplateYaml,
                    nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo, nodeTypesArtifactsToCreate, nodeName,
                    parsedToscaYamlInfo.getSubstitutionMappingNodeType());
            } else {
                final Resource genericResource = fetchAndSetDerivedFromGenericType(resource, null);
                resource = createResourceTransaction(resource, csarInfo.getModifier(), isNormative);
                log.trace("************* createResourceFromYaml after full create resource {}", yamlName);
                log.trace("************* Going to add inputs from yaml {}", yamlName);
                if (resource.shouldGenerateInputs()) {
                    generateAndAddInputsFromGenericTypeProperties(resource, genericResource);
                }
                final Map<String, InputDefinition> inputs = parsedToscaYamlInfo.getInputs();
                resource = createInputsOnResource(resource, inputs);

                log.trace("************* Finish to add inputs from yaml {}", yamlName);
                loggerSupportability.log(LoggerSupportabilityActions.CREATE_INPUTS, resource.getComponentMetadataForSupportLog(), StatusCode.COMPLETE,
                    "Finish to add inputs from yaml: {}", yamlName);
                if (resource.getResourceType() == ResourceTypeEnum.PNF) {
                    log.trace("************* Adding generic properties to PNF");
                    resource = (Resource) propertyBusinessLogic.copyPropertyToComponent(resource, genericResource.getProperties());
                    log.trace("************* Adding software information to PNF");
                    softwareInformationBusinessLogic.setSoftwareInformation(resource, csarInfo);
                    log.trace("************* Removing non-mano software information file from PNF");
                    if (csarInfo.getSoftwareInformationPath().isPresent() && !softwareInformationBusinessLogic.removeSoftwareInformationFile(
                        csarInfo)) {
                        log.warn(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, ResourceBusinessLogic.class.getName(), "catalog-be",
                            "Could not remove the software information file.");
                    }
                }
                final Map<String, UploadComponentInstanceInfo> instancesToCreate = getInstancesToCreate(parsedToscaYamlInfo);

                log.trace("************* Going to create nodes, RI's and Relations  from yaml {}", yamlName);
                loggerSupportability
                    .log(LoggerSupportabilityActions.CREATE_RESOURCE_FROM_YAML, resource.getComponentMetadataForSupportLog(), StatusCode.STARTED,
                        "Start create nodes, RI and Relations  from yaml: {}", yamlName);
                resource = createRIAndRelationsFromYaml(yamlName, resource, instancesToCreate, topologyTemplateYaml,
                    nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo, nodeTypesArtifactsToCreate, nodeName, null);
            }
            log.trace("************* Finished to create nodes, RI and Relation  from yaml {}", yamlName);
            loggerSupportability.log(LoggerSupportabilityActions.CREATE_RELATIONS, resource.getComponentMetadataForSupportLog(), StatusCode.COMPLETE,
                "Finished to create nodes, RI and Relation  from yaml: {}", yamlName);
            // validate update vf module group names
            Optional<Map<String, GroupDefinition>> asdGroups = checkAndCreateAsdTypeVfModules(parsedToscaYamlInfo.getInstances());
            Map<String, GroupDefinition> parsedGroups = parsedToscaYamlInfo.getGroups();
            if (asdGroups.isPresent()) {
                parsedGroups.putAll(asdGroups.get());
            }
            final Either<Map<String, GroupDefinition>, ResponseFormat> validateUpdateVfGroupNamesRes = groupBusinessLogic
                .validateUpdateVfGroupNames(parsedGroups, resource.getSystemName());
            if (validateUpdateVfGroupNamesRes.isRight()) {
                rollback(inTransaction, resource, createdArtifacts, nodeTypesNewCreatedArtifacts);
                throw new ByResponseFormatComponentException(validateUpdateVfGroupNamesRes.right().value());
            }
            // add groups to resource
            final Map<String, GroupDefinition> groups;
            log.trace("************* Going to add groups from yaml {}", yamlName);
            loggerSupportability.log(LoggerSupportabilityActions.CREATE_GROUPS, resource.getComponentMetadataForSupportLog(), StatusCode.STARTED,
                "Start to add groups from yaml: {}", yamlName);
            if (!validateUpdateVfGroupNamesRes.left().value().isEmpty()) {
                groups = validateUpdateVfGroupNamesRes.left().value();
            } else {
                groups = parsedGroups;
            }
            final Either<Resource, ResponseFormat> createGroupsOnResource = createGroupsOnResource(resource, groups);
            if (createGroupsOnResource.isRight()) {
                rollback(inTransaction, resource, createdArtifacts, nodeTypesNewCreatedArtifacts);
                loggerSupportability.log(LoggerSupportabilityActions.CREATE_GROUPS, resource.getComponentMetadataForSupportLog(), StatusCode.ERROR,
                    "ERROR while adding groups from yaml: {}", yamlName);
                throw new ByResponseFormatComponentException(createGroupsOnResource.right().value());
            }
            resource = createGroupsOnResource.left().value();
            log.trace("************* Finished to add groups from yaml {}", yamlName);
            loggerSupportability.log(LoggerSupportabilityActions.CREATE_GROUPS, resource.getComponentMetadataForSupportLog(), StatusCode.COMPLETE,
                "Finished to add groups from yaml: {}", yamlName);
            log.trace("************* Going to add artifacts from yaml {}", yamlName);
            loggerSupportability.log(LoggerSupportabilityActions.CREATE_ARTIFACTS, resource.getComponentMetadataForSupportLog(), StatusCode.STARTED,
                "Started to add artifacts from yaml: {}", yamlName);
            log.trace("************* Starting to add policies from yaml {}", yamlName);
            Map<String, PolicyDefinition> policies = parsedToscaYamlInfo.getPolicies();
            if (MapUtils.isNotEmpty(policies)) {
                resource = createPoliciesOnResource(resource, policies);
            }
            log.trace("************* Finished to add policies from yaml {}", yamlName);
            final NodeTypeInfoToUpdateArtifacts nodeTypeInfoToUpdateArtifacts = new NodeTypeInfoToUpdateArtifacts(nodeName,
                nodeTypesArtifactsToCreate);
            final Either<Resource, ResponseFormat> createArtifactsEither = createOrUpdateArtifacts(ArtifactOperationEnum.CREATE, createdArtifacts,
                yamlName, csarInfo, resource, nodeTypeInfoToUpdateArtifacts, inTransaction, shouldLock);
            if (createArtifactsEither.isRight()) {
                rollback(inTransaction, resource, createdArtifacts, nodeTypesNewCreatedArtifacts);
                loggerSupportability.log(LoggerSupportabilityActions.CREATE_ARTIFACTS, resource.getComponentMetadataForSupportLog(), StatusCode.ERROR,
                    "error happened {}", createArtifactsEither.right().value());
                throw new ByResponseFormatComponentException(createArtifactsEither.right().value());
            }
            loggerSupportability.log(LoggerSupportabilityActions.CREATE_ARTIFACTS, resource.getComponentMetadataForSupportLog(), StatusCode.COMPLETE,
                "Finished to add artifacts from yaml: " + resource.getToscaResourceName());
            final ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.CREATED);
            componentsUtils.auditResource(responseFormat, csarInfo.getModifier(), resource, actionEnum);
            ASDCKpiApi.countCreatedResourcesKPI();
            return resource;
        } catch (final BusinessLogicException e) {
            log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, ResourceBusinessLogic.class.getName(),
                "An error has occurred during resource and resource instance creation", e);
            rollback(inTransaction, resource, createdArtifacts, nodeTypesNewCreatedArtifacts);
            throw new ByResponseFormatComponentException(e.getResponseFormat());
        } catch (final ComponentException e) {
            log.error(EcompLoggerErrorCode.SCHEMA_ERROR, ResourceBusinessLogic.class.getName(),
                "An error has occurred during resource and resource instance creation", e);
            rollback(inTransaction, resource, createdArtifacts, nodeTypesNewCreatedArtifacts);
            throw new ByResponseFormatComponentException(e.getResponseFormat());
        } catch (final Exception e) {
            log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, ResourceBusinessLogic.class.getName(),
                "An error has occurred during resource and resource instance creation", e);
            rollback(inTransaction, resource, createdArtifacts, nodeTypesNewCreatedArtifacts);
            throw new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR);
        } finally {
            if (!inTransaction) {
                janusGraphDao.commit();
            }
            if (shouldLock) {
                graphLockOperation.unlockComponentByName(resource.getSystemName(), resource.getUniqueId(), NodeTypeEnum.Resource);
            }
        }
    }

    private Optional<Map<String, GroupDefinition>> checkAndCreateAsdTypeVfModules(Map<String, UploadComponentInstanceInfo> instances) {
        Map<String, GroupDefinition> addAsdGroups = new HashMap<>();
        if (isNotEmpty(instances) || instances != null) {
            for (Map.Entry<String, UploadComponentInstanceInfo> instance : instances.entrySet()) {
                if (isNotEmpty(instance.getValue().getArtifacts()) || instance.getValue().getArtifacts() != null) {
                    Map<String, UploadArtifactInfo> artifactsMap = instance.getValue().getArtifacts()
                        .get(ToscaTagNamesEnum.ARTIFACTS.getElementName());
                    if (isNotEmpty(artifactsMap) || artifactsMap != null) {
                        for (Map.Entry<String, UploadArtifactInfo> artifact : artifactsMap.entrySet()) {
                            if (artifact.getValue().getType().equals(Constants.ASD_DEPLOYMENT_ITEM)) {
                                GroupDefinition groupDefinition = new GroupDefinition();
                                groupDefinition.setName(artifact.getKey());
                                groupDefinition.setType(Constants.DEFAULT_GROUP_VF_MODULE);
                                addAsdTypeProperties(groupDefinition);
                                addAsdGroups.put(groupDefinition.getName(), groupDefinition);
                            }
                        }
                    }
                }
            }
        }
        return Optional.of(addAsdGroups);
    }

    private void addAsdTypeProperties(GroupDefinition groupDefinition) {
        List<GroupProperty> properties = new ArrayList<>();
        GroupProperty propIsBase = new GroupProperty();
        propIsBase.setName(Constants.IS_BASE);
        propIsBase.setValue("true");
        properties.add(propIsBase);
        GroupProperty propVfModuleLabel = new GroupProperty();
        propVfModuleLabel.setName(Constants.VF_MODULE_LABEL);
        propVfModuleLabel.setValue(groupDefinition.getName());
        properties.add(propVfModuleLabel);
        GroupProperty propVfModuleDescription = new GroupProperty();
        propVfModuleDescription.setName(Constants.VF_MODULE_DESCRIPTION);
        propVfModuleDescription.setValue("VF Module representing deployment item " + groupDefinition.getName());
        properties.add(propVfModuleDescription);
        GroupProperty propMinVfModuleInstances = new GroupProperty();
        propMinVfModuleInstances.setName(Constants.MIN_VF_MODULE_INSTANCES);
        propMinVfModuleInstances.setValue("1");
        properties.add(propMinVfModuleInstances);
        GroupProperty propMaxVfModuleInstances = new GroupProperty();
        propMaxVfModuleInstances.setName(Constants.MAX_VF_MODULE_INSTANCES);
        propMaxVfModuleInstances.setValue("1");
        properties.add(propMaxVfModuleInstances);
        GroupProperty propInitialCount = new GroupProperty();
        propInitialCount.setName(Constants.INITIAL_COUNT);
        propInitialCount.setValue("1");
        properties.add(propInitialCount);
        GroupProperty propVfModuleType = new GroupProperty();
        propVfModuleType.setName(Constants.VF_MODULE_TYPE);
        propVfModuleType.setValue("Base");
        properties.add(propVfModuleType);
        GroupProperty propVolumeGroup = new GroupProperty();
        propVolumeGroup.setName(Constants.VOLUME_GROUP);
        propVolumeGroup.setValue("false");
        properties.add(propVolumeGroup);
        groupDefinition.convertFromGroupProperties(properties);
    }

    private boolean processSubstitutableAsNodeType(final Resource resource, final ParsedToscaYamlInfo parsedToscaYamlInfo) {
        return !resource.getResourceType().isAtomicType() && StringUtils.isNotEmpty(resource.getModel())
            && parsedToscaYamlInfo.getSubstitutionMappingNodeType() != null;
    }

    private Map<String, UploadComponentInstanceInfo> getInstancesToCreate(final ParsedToscaYamlInfo parsedToscaYamlInfo) {
        return getInstancesToCreate(parsedToscaYamlInfo, null);
    }

    private Map<String, UploadComponentInstanceInfo> getInstancesToCreate(final ParsedToscaYamlInfo parsedToscaYamlInfo, final String model) {
        if (StringUtils.isEmpty(model) || StringUtils.isEmpty(parsedToscaYamlInfo.getSubstitutionMappingNodeType())) {
            return parsedToscaYamlInfo.getInstances();
        }
        return parsedToscaYamlInfo.getInstances().entrySet().stream()
            .filter(entry -> !parsedToscaYamlInfo.getSubstitutionMappingNodeType().equals(entry.getValue().getType()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void rollback(boolean inTransaction, Resource resource, List<ArtifactDefinition> createdArtifacts,
                          List<ArtifactDefinition> nodeTypesNewCreatedArtifacts) {
        if (!inTransaction) {
            janusGraphDao.rollback();
        }
        if (isNotEmpty(createdArtifacts) && isNotEmpty(nodeTypesNewCreatedArtifacts)) {
            createdArtifacts.addAll(nodeTypesNewCreatedArtifacts);
            log.debug("Found {} newly created artifacts to deleted, the component name: {}", createdArtifacts.size(), resource.getName());
        }
    }

    private Resource getResourceWithGroups(String resourceId) {
        ComponentParametersView filter = new ComponentParametersView();
        filter.setIgnoreGroups(false);
        Either<Resource, StorageOperationStatus> updatedResource = toscaOperationFacade.getToscaElement(resourceId, filter);
        if (updatedResource.isRight()) {
            rollbackWithException(componentsUtils.convertFromStorageResponse(updatedResource.right().value()), resourceId);
        }
        return updatedResource.left().value();
    }

    private Either<Resource, ResponseFormat> createGroupsOnResource(Resource resource, Map<String, GroupDefinition> groups) {
        if (groups != null && !groups.isEmpty()) {
            List<GroupDefinition> groupsAsList = updateGroupsMembersUsingResource(groups, resource);
            handleGroupsProperties(resource, groups);
            fillGroupsFinalFields(groupsAsList);
            Either<List<GroupDefinition>, ResponseFormat> createGroups = groupBusinessLogic.createGroups(resource, groupsAsList, true);
            if (createGroups.isRight()) {
                return Either.right(createGroups.right().value());
            }
        }
        return Either.left(resource);
    }

    private void handleGroupsProperties(Resource resource, Map<String, GroupDefinition> groups) {
        List<InputDefinition> inputs = resource.getInputs();
        if (MapUtils.isNotEmpty(groups)) {
            groups.values().stream().filter(g -> isNotEmpty(g.getProperties())).flatMap(g -> g.getProperties().stream())
                .forEach(p -> handleGetInputs(p, inputs));
        }
    }

    private Resource createPoliciesOnResource(Resource resource, Map<String, PolicyDefinition> policies) {
        policyBusinessLogic.createPoliciesFromParsedCsar(resource, policies);
        return resource;
    }

    private void handleGetInputs(PropertyDataDefinition property, List<InputDefinition> inputs) {
        if (isNotEmpty(property.getGetInputValues())) {
            if (inputs == null || inputs.isEmpty()) {
                log.debug("Failed to add property {} to group. Inputs list is empty ", property);
                rollbackWithException(ActionStatus.INPUTS_NOT_FOUND,
                    property.getGetInputValues().stream().map(GetInputValueDataDefinition::getInputName).collect(toList()).toString());
            }
            ListIterator<GetInputValueDataDefinition> getInputValuesIter = property.getGetInputValues().listIterator();
            while (getInputValuesIter.hasNext()) {
                GetInputValueDataDefinition getInput = getInputValuesIter.next();
                Either<InputDefinition, RuntimeException> inputEither = findInputByName(inputs, getInput);
                if (inputEither.isRight()) {
                    throw inputEither.right().value();
                } else {
                    InputDefinition input = inputEither.left().value();
                    getInput.setInputId(input.getUniqueId());
                    if (getInput.getGetInputIndex() != null) {
                        GetInputValueDataDefinition getInputIndex = getInput.getGetInputIndex();
                        Either<InputDefinition, RuntimeException> newInputEither = findInputByName(inputs, getInputIndex);
                        if (newInputEither.isRight()) {
                            throw newInputEither.right().value();
                        } else {
                            InputDefinition newInput = newInputEither.left().value();
                            getInputIndex.setInputId(newInput.getUniqueId());
                        }
                        getInputValuesIter.add(getInputIndex);
                    }
                }
            }
        }
    }

    <T> Either<T, RuntimeException> rollbackWithEither(final ActionStatus actionStatus, final String... params) {
        return rollbackWithEither(janusGraphDao, actionStatus, params);
    }

    private Either<InputDefinition, RuntimeException> findInputByName(List<InputDefinition> inputs, GetInputValueDataDefinition getInput) {
        final String inputName = getInput != null ? getInput.getInputName() : "";
        if (inputs == null || inputs.isEmpty()) {
            log.debug("#findInputByName - Inputs list is empty");
            return rollbackWithEither(ActionStatus.INPUTS_NOT_FOUND, inputName);
        } else {
            Optional<InputDefinition> inputOpt = inputs.stream().filter(p -> p.getName().equals(inputName)).findFirst();
            if (inputOpt.isEmpty()) {
                log.debug("#findInputByName - Failed to find the input {} ", inputName);
                return rollbackWithEither(ActionStatus.INPUTS_NOT_FOUND, inputName);
            } else {
                return Either.left(inputOpt.get());
            }
        }
    }

    private void fillGroupsFinalFields(List<GroupDefinition> groupsAsList) {
        groupsAsList.forEach(groupDefinition -> {
            groupDefinition.setInvariantName(groupDefinition.getName());
            groupDefinition.setCreatedFrom(CreatedFrom.CSAR);
        });
    }

    private Resource updateGroupsOnResource(Resource resource, Map<String, GroupDefinition> groups) {
        if (isEmpty(groups)) {
            return resource;
        }
        return updateOrCreateGroups(resource, groups);
    }

    private Resource updateOrCreateGroups(Resource resource, Map<String, GroupDefinition> groups) {
        List<GroupDefinition> groupsFromResource = resource.getGroups();
        List<GroupDefinition> groupsAsList = updateGroupsMembersUsingResource(groups, resource);
        List<GroupDefinition> groupsToUpdate = new ArrayList<>();
        List<GroupDefinition> groupsToDelete = new ArrayList<>();
        List<GroupDefinition> groupsToCreate = new ArrayList<>();
        if (isNotEmpty(groupsFromResource)) {
            addGroupsToCreateOrUpdate(groupsFromResource, groupsAsList, groupsToUpdate, groupsToCreate);
            addGroupsToDelete(groupsFromResource, groupsAsList, groupsToDelete);
        } else {
            groupsToCreate.addAll(groupsAsList);
        }
        if (isNotEmpty(groupsToCreate)) {
            fillGroupsFinalFields(groupsToCreate);
            if (isNotEmpty(groupsFromResource)) {
                groupBusinessLogic.addGroups(resource, groupsToCreate, true).left().on(this::throwComponentException);
            } else {
                groupBusinessLogic.createGroups(resource, groupsToCreate, true).left().on(this::throwComponentException);
            }
        }
        if (isNotEmpty(groupsToDelete)) {
            groupBusinessLogic.deleteGroups(resource, groupsToDelete).left().on(this::throwComponentException);
        }
        if (isNotEmpty(groupsToUpdate)) {
            groupBusinessLogic.updateGroups(resource, groupsToUpdate, true).left().on(this::throwComponentException);
        }
        return resource;
    }

    private void addGroupsToDelete(List<GroupDefinition> groupsFromResource, List<GroupDefinition> groupsAsList,
                                   List<GroupDefinition> groupsToDelete) {
        for (GroupDefinition group : groupsFromResource) {
            Optional<GroupDefinition> op = groupsAsList.stream().filter(p -> p.getInvariantName().equalsIgnoreCase(group.getInvariantName()))
                .findAny();
            if (op.isEmpty() && (group.getArtifacts() == null || group.getArtifacts().isEmpty())) {
                groupsToDelete.add(group);
            }
        }
    }

    private void addGroupsToCreateOrUpdate(List<GroupDefinition> groupsFromResource, List<GroupDefinition> groupsAsList,
                                           List<GroupDefinition> groupsToUpdate, List<GroupDefinition> groupsToCreate) {
        for (GroupDefinition group : groupsAsList) {
            Optional<GroupDefinition> op = groupsFromResource.stream().filter(p -> p.getInvariantName().equalsIgnoreCase(group.getInvariantName()))
                .findAny();
            if (op.isPresent()) {
                GroupDefinition groupToUpdate = op.get();
                groupToUpdate.setMembers(group.getMembers());
                groupToUpdate.setCapabilities(group.getCapabilities());
                groupToUpdate.setProperties(group.getProperties());
                groupsToUpdate.add(groupToUpdate);
            } else {
                groupsToCreate.add(group);
            }
        }
    }

    private Resource createInputsOnResource(Resource resource, Map<String, InputDefinition> inputs) {
        List<InputDefinition> resourceProperties = resource.getInputs();
        if (MapUtils.isNotEmpty(inputs) || isNotEmpty(resourceProperties)) {
            Either<List<InputDefinition>, ResponseFormat> createInputs = inputsBusinessLogic.createInputsInGraph(inputs, resource);
            if (createInputs.isRight()) {
                loggerSupportability.log(LoggerSupportabilityActions.CREATE_INPUTS, resource.getComponentMetadataForSupportLog(), StatusCode.ERROR,
                    "failed to add inputs from yaml: {}", createInputs.right().value());
                throw new ByResponseFormatComponentException(createInputs.right().value());
            }
            resource.setInputs(createInputs.left().value());
        }
        return resource;
    }

    private Resource generatePropertiesFromNodeType(final Resource resource, final Map<String, Object> nodeType) {
        final Either<Map<String, PropertyDefinition>, ResultStatusEnum> properties = ImportUtils.getProperties(nodeType);
        if (properties.isLeft()) {
            final List<PropertyDefinition> propertiesList = new ArrayList<>();
            final Map<String, PropertyDefinition> value = properties.left().value();
            if (value != null) {
                for (Entry<String, PropertyDefinition> entry : value.entrySet()) {
                    final String name = entry.getKey();
                    final PropertyDefinition propertyDefinition = entry.getValue();
                    propertyDefinition.setName(name);
                    propertiesList.add(propertyDefinition);
                    resource.getProperties().removeIf(p -> p.getName().equals(name));
                }
            }
            resource.getProperties().addAll(propertiesList);
        }
        return resource;
    }

    private Resource createResourcePropertiesOnGraph(final Resource resource) {
        final List<PropertyDefinition> resourceProperties = resource.getProperties();
        for (PropertyDefinition propertyDefinition : resourceProperties) {
            final Either<PropertyDefinition, StorageOperationStatus> addPropertyEither = toscaOperationFacade
                .addPropertyToComponent(propertyDefinition, resource);

            if (addPropertyEither.isRight()) {
                final String error = String.format("failed to add properties from yaml: {}", addPropertyEither.right().value());
                loggerSupportability.log(LoggerSupportabilityActions.CREATE_PROPERTIES, resource.getComponentMetadataForSupportLog(),
                    StatusCode.ERROR,
                    error);
                throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(addPropertyEither.right().value()), error);
            }
        }
        return resource;
    }

    private List<GroupDefinition> updateGroupsMembersUsingResource(Map<String, GroupDefinition> groups, Resource component) {
        List<GroupDefinition> result = new ArrayList<>();
        List<ComponentInstance> componentInstances = component.getComponentInstances();
        if (groups != null) {
            for (Entry<String, GroupDefinition> entry : groups.entrySet()) {
                String groupName = entry.getKey();
                GroupDefinition groupDefinition = entry.getValue();
                GroupDefinition updatedGroupDefinition = new GroupDefinition(groupDefinition);

                updatedGroupDefinition.setMembers(null);
                Map<String, String> members = groupDefinition.getMembers();
                if (members != null) {
                    updateGroupMembers(groups, updatedGroupDefinition, component, componentInstances, groupName, members);
                }
                result.add(updatedGroupDefinition);
            }
        }
        return result;
    }

    private void updateGroupMembers(Map<String, GroupDefinition> groups, GroupDefinition updatedGroupDefinition, Resource component,
                                    List<ComponentInstance> componentInstances, String groupName, Map<String, String> members) {
        Set<String> compInstancesNames = members.keySet();
        if (CollectionUtils.isEmpty(componentInstances)) {
            String membersAstString = String.join(",", compInstancesNames);
            log.debug("The members: {}, in group: {}, cannot be found in component {}. There are no component instances.", membersAstString,
                groupName, component.getNormalizedName());
            throw new ByActionStatusComponentException(ActionStatus.GROUP_INVALID_COMPONENT_INSTANCE, membersAstString, groupName,
                component.getNormalizedName(), getComponentTypeForResponse(component));
        }
        // Find all component instances with the member names
        Map<String, String> memberNames = componentInstances.stream().collect(toMap(ComponentInstance::getName, ComponentInstance::getUniqueId));
        memberNames.putAll(groups.keySet().stream().collect(toMap(g -> g, g -> "")));
        Map<String, String> relevantInstances = memberNames.entrySet().stream().filter(n -> compInstancesNames.contains(n.getKey()))
            .collect(toMap(Entry::getKey, Entry::getValue));
        if (relevantInstances.size() != compInstancesNames.size()) {
            List<String> foundMembers = new ArrayList<>(relevantInstances.keySet());
            foundMembers.forEach(compInstancesNames::remove);
            String membersAstString = String.join(",", compInstancesNames);
            log.debug("The members: {}, in group: {}, cannot be found in component: {}", membersAstString, groupName, component.getNormalizedName());
            throw new ByActionStatusComponentException(ActionStatus.GROUP_INVALID_COMPONENT_INSTANCE, membersAstString, groupName,
                component.getNormalizedName(), getComponentTypeForResponse(component));
        }
        updatedGroupDefinition.setMembers(relevantInstances);
    }

    private Resource createRIAndRelationsFromYaml(String yamlName, Resource resource,
                                                  Map<String, UploadComponentInstanceInfo> uploadComponentInstanceInfoMap,
                                                  String topologyTemplateYaml, List<ArtifactDefinition> nodeTypesNewCreatedArtifacts,
                                                  Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo,
                                                  Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate,
                                                  String nodeName, final String substitutableAsNodeType) {
        log.debug("************* Going to create all nodes {}", yamlName);
        handleNodeTypes(yamlName, resource, topologyTemplateYaml, false, nodeTypesArtifactsToCreate, nodeTypesNewCreatedArtifacts, nodeTypesInfo,
            csarInfo, nodeName, substitutableAsNodeType);
        log.debug("************* Finished to create all nodes {}", yamlName);
        log.debug("************* Going to create all resource instances {}", yamlName);
        Map<String, Resource> existingNodeTypesByResourceNames = new HashMap<>();
        resource = createResourceInstances(yamlName, resource, null, uploadComponentInstanceInfoMap, csarInfo.getCreatedNodes(),
            existingNodeTypesByResourceNames);
        log.debug("************* Finished to create all resource instances {}", yamlName);
        log.debug("************* Going to create all relations {}", yamlName);
        resource = createResourceInstancesRelations(csarInfo.getModifier(), yamlName, resource, null, uploadComponentInstanceInfoMap,
            existingNodeTypesByResourceNames);
        log.debug("************* Finished to create all relations {}", yamlName);
        log.debug("************* Going to create positions {}", yamlName);
        compositionBusinessLogic.setPositionsForComponentInstances(resource, csarInfo.getModifier().getUserId());
        log.debug("************* Finished to set positions {}", yamlName);
        return resource;
    }

    private void handleAndAddExtractedVfcsArtifacts(List<ArtifactDefinition> vfcArtifacts, List<ArtifactDefinition> artifactsToAdd) {
        List<String> vfcArtifactNames = vfcArtifacts.stream().map(ArtifactDataDefinition::getArtifactName).collect(toList());
        artifactsToAdd.forEach(a -> {
            if (!vfcArtifactNames.contains(a.getArtifactName())) {
                vfcArtifacts.add(a);
            } else {
                log.debug("Can't upload two artifact with the same name {}. ", a.getArtifactName());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleNodeTypes(String yamlName, Resource resource, String topologyTemplateYaml, boolean needLock,
                                 Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle,
                                 List<ArtifactDefinition> nodeTypesNewCreatedArtifacts, Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo,
                                 String nodeName, String substitutableAsNodeType) {
        try {
            for (Entry<String, NodeTypeInfo> nodeTypeEntry : nodeTypesInfo.entrySet()) {
                if (nodeTypeEntry.getValue().isNested() && !nodeTypeAlreadyExists(nodeTypeEntry.getKey(), resource.getModel())) {
                    handleNestedVfc(resource, nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo,
                        nodeTypeEntry.getKey());
                    log.trace("************* finished to create node {}", nodeTypeEntry.getKey());
                }
            }
            Map<String, Object> mappedToscaTemplate = null;
            if (StringUtils.isNotEmpty(nodeName) && isNotEmpty(nodeTypesInfo) && nodeTypesInfo.containsKey(nodeName)) {
                mappedToscaTemplate = nodeTypesInfo.get(nodeName).getMappedToscaTemplate();
            }
            if (isEmpty(mappedToscaTemplate)) {
                mappedToscaTemplate = (Map<String, Object>) new Yaml().load(topologyTemplateYaml);
            }
            createResourcesFromYamlNodeTypesList(yamlName, resource, mappedToscaTemplate, needLock, nodeTypesArtifactsToHandle,
                nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo, substitutableAsNodeType);
        } catch (ComponentException e) {
            ResponseFormat responseFormat =
                e.getResponseFormat() != null ? e.getResponseFormat() : componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams());
            componentsUtils.auditResource(responseFormat, csarInfo.getModifier(), resource, AuditingActionEnum.IMPORT_RESOURCE);
            throw e;
        } catch (StorageException e) {
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(e.getStorageOperationStatus()));
            componentsUtils.auditResource(responseFormat, csarInfo.getModifier(), resource, AuditingActionEnum.IMPORT_RESOURCE);
            throw e;
        }
    }

    private boolean nodeTypeAlreadyExists(final String toscaResourceName, String modelName) {
        return toscaOperationFacade.getLatestByToscaResourceName(toscaResourceName, modelName).isLeft();
    }

    private Either<Resource, ResponseFormat> handleVfCsarArtifacts(Resource resource, CsarInfo csarInfo, List<ArtifactDefinition> createdArtifacts,
                                                                   ArtifactOperationInfo artifactOperation, boolean shouldLock,
                                                                   boolean inTransaction) {
        if (csarInfo.getCsar() != null) {
            String vendorLicenseModelId = null;
            String vfLicenseModelId = null;
            if (artifactOperation.isUpdate()) {
                Map<String, ArtifactDefinition> deploymentArtifactsMap = resource.getDeploymentArtifacts();
                if (deploymentArtifactsMap != null && !deploymentArtifactsMap.isEmpty()) {
                    for (Entry<String, ArtifactDefinition> artifactEntry : deploymentArtifactsMap.entrySet()) {
                        if (artifactEntry.getValue().getArtifactName().equalsIgnoreCase(Constants.VENDOR_LICENSE_MODEL)) {
                            vendorLicenseModelId = artifactEntry.getValue().getUniqueId();
                        }
                        if (artifactEntry.getValue().getArtifactName().equalsIgnoreCase(Constants.VF_LICENSE_MODEL)) {
                            vfLicenseModelId = artifactEntry.getValue().getUniqueId();
                        }
                    }
                }
            }
            // Specific Behavior for license artifacts
            createOrUpdateSingleNonMetaArtifact(resource, csarInfo, CsarUtils.ARTIFACTS_PATH + Constants.VENDOR_LICENSE_MODEL,
                Constants.VENDOR_LICENSE_MODEL, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ArtifactGroupTypeEnum.DEPLOYMENT,
                Constants.VENDOR_LICENSE_LABEL, Constants.VENDOR_LICENSE_DISPLAY_NAME, Constants.VENDOR_LICENSE_DESCRIPTION, vendorLicenseModelId,
                artifactOperation, null, true, shouldLock, inTransaction);
            createOrUpdateSingleNonMetaArtifact(resource, csarInfo, CsarUtils.ARTIFACTS_PATH + Constants.VF_LICENSE_MODEL, Constants.VF_LICENSE_MODEL,
                ArtifactTypeEnum.VF_LICENSE.getType(), ArtifactGroupTypeEnum.DEPLOYMENT, Constants.VF_LICENSE_LABEL,
                Constants.VF_LICENSE_DISPLAY_NAME, Constants.VF_LICENSE_DESCRIPTION, vfLicenseModelId, artifactOperation, null, true, shouldLock,
                inTransaction);
            Either<Resource, ResponseFormat> eitherCreateResult = createOrUpdateNonMetaArtifacts(csarInfo, resource, createdArtifacts, shouldLock,
                inTransaction, artifactOperation);
            if (eitherCreateResult.isRight()) {
                return Either.right(eitherCreateResult.right().value());
            }
            Either<ImmutablePair<String, String>, ResponseFormat> artifacsMetaCsarStatus = CsarValidationUtils
                .getArtifactsMeta(csarInfo.getCsar(), csarInfo.getCsarUUID(), componentsUtils);
            if (artifacsMetaCsarStatus.isLeft()) {
                String artifactsFileName = artifacsMetaCsarStatus.left().value().getKey();
                String artifactsContents = artifacsMetaCsarStatus.left().value().getValue();
                Either<Resource, ResponseFormat> createArtifactsFromCsar;
                if (artifactOperation.isCreateOrLink()) {
                    createArtifactsFromCsar = csarArtifactsAndGroupsBusinessLogic
                        .createResourceArtifactsFromCsar(csarInfo, resource, artifactsContents, artifactsFileName, createdArtifacts);
                } else {
                    Either<Component, ResponseFormat> result = csarArtifactsAndGroupsBusinessLogic
                        .updateResourceArtifactsFromCsar(csarInfo, resource, artifactsContents, artifactsFileName, createdArtifacts, shouldLock,
                            inTransaction);
                    if ((result.left().value() instanceof Resource) && result.isLeft()) {
                        Resource service1 = (Resource) result.left().value();
                        createArtifactsFromCsar = Either.left(service1);
                    } else {
                        createArtifactsFromCsar = Either.right(result.right().value());
                    }
                }
                if (createArtifactsFromCsar.isRight()) {
                    log.debug("Couldn't create artifacts from artifacts.meta");
                    return Either.right(createArtifactsFromCsar.right().value());
                }
                return Either.left(createArtifactsFromCsar.left().value());
            }
        }
        return Either.left(resource);
    }

    private Either<Boolean, ResponseFormat> createOrUpdateSingleNonMetaArtifact(Resource resource, CsarInfo csarInfo, String artifactPath,
                                                                                String artifactFileName, String artifactType,
                                                                                ArtifactGroupTypeEnum artifactGroupType, String artifactLabel,
                                                                                String artifactDisplayName, String artifactDescription,
                                                                                String artifactId, ArtifactOperationInfo operation,
                                                                                List<ArtifactDefinition> createdArtifacts, boolean isFromCsar,
                                                                                boolean shouldLock, boolean inTransaction) {
        byte[] artifactFileBytes = null;
        if (csarInfo.getCsar().containsKey(artifactPath)) {
            artifactFileBytes = csarInfo.getCsar().get(artifactPath);
        }
        Either<Boolean, ResponseFormat> result = Either.left(true);
        if (operation.isUpdate() || operation.isDelete()) {
            if (isArtifactDeletionRequired(artifactId, artifactFileBytes, isFromCsar)) {
                Either<ArtifactDefinition, ResponseFormat> handleDelete = artifactsBusinessLogic
                    .handleDelete(resource.getUniqueId(), artifactId, csarInfo.getModifier(), resource, shouldLock, inTransaction);
                if (handleDelete.isRight()) {
                    result = Either.right(handleDelete.right().value());
                } else {
                    ArtifactDefinition value = handleDelete.left().value();
                    String updatedArtifactId = value.getUniqueId();
                    if (artifactGroupType == ArtifactGroupTypeEnum.DEPLOYMENT) {
                        resource.getDeploymentArtifacts().remove(updatedArtifactId);
                    } else {
                        resource.getArtifacts().remove(updatedArtifactId);
                    }
                }
                return result;
            }
            if (StringUtils.isEmpty(artifactId) && artifactFileBytes != null) {
                operation = new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE);
            }
        }
        if (artifactFileBytes != null) {
            Map<String, Object> vendorLicenseModelJson = ArtifactUtils
                .buildJsonForUpdateArtifact(artifactId, artifactFileName, artifactType, artifactGroupType, artifactLabel, artifactDisplayName,
                    artifactDescription, artifactFileBytes, null, isFromCsar);
            Either<Either<ArtifactDefinition, Operation>, ResponseFormat> eitherNonMetaArtifacts = csarArtifactsAndGroupsBusinessLogic
                .createOrUpdateCsarArtifactFromJson(resource, csarInfo.getModifier(), vendorLicenseModelJson, operation);
            addNonMetaCreatedArtifactsToSupportRollback(operation, createdArtifacts, eitherNonMetaArtifacts);
            if (eitherNonMetaArtifacts.isRight()) {
                BeEcompErrorManager.getInstance().logInternalFlowError("UploadLicenseArtifact",
                    "Failed to upload license artifact: " + artifactFileName + "With csar uuid: " + csarInfo.getCsarUUID(), ErrorSeverity.WARNING);
                return Either.right(eitherNonMetaArtifacts.right().value());
            }
            ArtifactDefinition artifactDefinition = eitherNonMetaArtifacts.left().value().left().value();
            createOrUpdateResourceWithUpdatedArtifact(artifactDefinition, resource, artifactGroupType);
        }
        return result;
    }

    private void createOrUpdateResourceWithUpdatedArtifact(ArtifactDefinition artifact, Resource resource, ArtifactGroupTypeEnum groupTypeEnum) {
        if (groupTypeEnum == ArtifactGroupTypeEnum.DEPLOYMENT) {
            resource.getDeploymentArtifacts().put(artifact.getArtifactLabel(), artifact);
        } else {
            resource.getArtifacts().put(artifact.getArtifactLabel(), artifact);
        }
    }

    private boolean isArtifactDeletionRequired(String artifactId, byte[] artifactFileBytes, boolean isFromCsar) {
        return !StringUtils.isEmpty(artifactId) && artifactFileBytes == null && isFromCsar;
    }

    private void addNonMetaCreatedArtifactsToSupportRollback(ArtifactOperationInfo operation, List<ArtifactDefinition> createdArtifacts,
                                                             Either<Either<ArtifactDefinition, Operation>, ResponseFormat> eitherNonMetaArtifacts) {
        if (operation.isCreateOrLink() && createdArtifacts != null && eitherNonMetaArtifacts.isLeft()) {
            Either<ArtifactDefinition, Operation> eitherResult = eitherNonMetaArtifacts.left().value();
            if (eitherResult.isLeft()) {
                createdArtifacts.add(eitherResult.left().value());
            }
        }
    }

    private Either<Resource, ResponseFormat> createOrUpdateNonMetaArtifacts(CsarInfo csarInfo, Resource resource,
                                                                            List<ArtifactDefinition> createdArtifacts, boolean shouldLock,
                                                                            boolean inTransaction, ArtifactOperationInfo artifactOperation) {
        Either<Resource, ResponseFormat> resStatus = null;
        Map<String, Set<List<String>>> collectedWarningMessages = new HashMap<>();
        try {
            Either<List<NonMetaArtifactInfo>, String> artifactPathAndNameList = getValidArtifactNames(csarInfo, collectedWarningMessages);
            if (artifactPathAndNameList.isRight()) {
                return Either.right(
                    getComponentsUtils().getResponseFormatByArtifactId(ActionStatus.ARTIFACT_NAME_INVALID, artifactPathAndNameList.right().value(),
                        VALID_CHARACTERS_ARTIFACT_NAME));
            }
            EnumMap<ArtifactOperationEnum, List<NonMetaArtifactInfo>> vfCsarArtifactsToHandle = null;
            if (artifactOperation.isCreateOrLink()) {
                vfCsarArtifactsToHandle = new EnumMap<>(ArtifactOperationEnum.class);
                vfCsarArtifactsToHandle.put(artifactOperation.getArtifactOperationEnum(), artifactPathAndNameList.left().value());
            } else {
                Either<EnumMap<ArtifactOperationEnum, List<NonMetaArtifactInfo>>, ResponseFormat> findVfCsarArtifactsToHandleRes = findVfCsarArtifactsToHandle(
                    resource, artifactPathAndNameList.left().value(), csarInfo.getModifier());
                if (findVfCsarArtifactsToHandleRes.isRight()) {
                    resStatus = Either.right(findVfCsarArtifactsToHandleRes.right().value());
                }
                if (resStatus == null) {
                    vfCsarArtifactsToHandle = findVfCsarArtifactsToHandleRes.left().value();
                }
            }
            if (resStatus == null && vfCsarArtifactsToHandle != null) {
                resStatus = processCsarArtifacts(csarInfo, resource, createdArtifacts, shouldLock, inTransaction, resStatus, vfCsarArtifactsToHandle);
            }
            if (resStatus == null) {
                resStatus = Either.left(resource);
            }
        } catch (Exception e) {
            resStatus = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            log.debug("Exception occurred in createNonMetaArtifacts, message:{}", e.getMessage(), e);
        } finally {
            CsarUtils.handleWarningMessages(collectedWarningMessages);
        }
        return resStatus;
    }

    private Either<Resource, ResponseFormat> processCsarArtifacts(CsarInfo csarInfo, Resource resource, List<ArtifactDefinition> createdArtifacts,
                                                                  boolean shouldLock, boolean inTransaction,
                                                                  Either<Resource, ResponseFormat> resStatus,
                                                                  EnumMap<ArtifactOperationEnum, List<NonMetaArtifactInfo>> vfCsarArtifactsToHandle) {
        for (Entry<ArtifactOperationEnum, List<NonMetaArtifactInfo>> currArtifactOperationPair : vfCsarArtifactsToHandle.entrySet()) {
            Optional<ResponseFormat> optionalCreateInDBError =
                // Stream of artifacts to be created
                currArtifactOperationPair.getValue().stream()
                    // create each artifact
                    .map(e -> createOrUpdateSingleNonMetaArtifact(resource, csarInfo, e.getPath(), e.getArtifactName(), e.getArtifactType(),
                        e.getArtifactGroupType(), e.getArtifactLabel(), e.getDisplayName(), CsarUtils.ARTIFACT_CREATED_FROM_CSAR,
                        e.getArtifactUniqueId(), new ArtifactOperationInfo(false, false, currArtifactOperationPair.getKey()), createdArtifacts,
                        e.isFromCsar(), shouldLock, inTransaction))
                    // filter in only error
                    .filter(Either::isRight).
                    // Convert the error from either to

                    // ResponseFormat
                        map(e -> e.right().value()).
                    // Check if an error occurred
                        findAny();
            // Error found on artifact Creation
            if (optionalCreateInDBError.isPresent()) {
                resStatus = Either.right(optionalCreateInDBError.get());
                break;
            }
        }
        return resStatus;
    }

    private Either<List<NonMetaArtifactInfo>, String> getValidArtifactNames(CsarInfo csarInfo,
                                                                            Map<String, Set<List<String>>> collectedWarningMessages) {
        List<NonMetaArtifactInfo> artifactPathAndNameList =
            // Stream of file paths contained in csar
            csarInfo.getCsar().entrySet().stream()
                // Filter in only VF artifact path location
                .filter(e -> Pattern.compile(VF_NODE_TYPE_ARTIFACTS_PATH_PATTERN).matcher(e.getKey()).matches())
                // Validate and add warnings
                .map(e -> CsarUtils.validateNonMetaArtifact(e.getKey(), e.getValue(), collectedWarningMessages))
                // Filter in Non Warnings
                .filter(Either::isLeft)
                // Convert from Either to NonMetaArtifactInfo
                .map(e -> e.left().value())
                // collect to List
                .collect(toList());
        Pattern englishNumbersAndUnderScoresOnly = Pattern.compile(CsarUtils.VALID_ENGLISH_ARTIFACT_NAME);
        for (NonMetaArtifactInfo nonMetaArtifactInfo : artifactPathAndNameList) {
            if (!englishNumbersAndUnderScoresOnly.matcher(nonMetaArtifactInfo.getDisplayName()).matches()) {
                return Either.right(nonMetaArtifactInfo.getArtifactName());
            }
        }
        return Either.left(artifactPathAndNameList);
    }

    private Either<EnumMap<ArtifactOperationEnum, List<NonMetaArtifactInfo>>, ResponseFormat> findVfCsarArtifactsToHandle(Resource resource,
                                                                                                                          List<NonMetaArtifactInfo> artifactPathAndNameList,
                                                                                                                          User user) {
        List<ArtifactDefinition> existingArtifacts = new ArrayList<>();
        // collect all Deployment and Informational artifacts of VF
        if (resource.getDeploymentArtifacts() != null && !resource.getDeploymentArtifacts().isEmpty()) {
            existingArtifacts.addAll(resource.getDeploymentArtifacts().values());
        }
        if (resource.getArtifacts() != null && !resource.getArtifacts().isEmpty()) {
            existingArtifacts.addAll(resource.getArtifacts().values());
        }
        existingArtifacts = existingArtifacts.stream()
            // filter MANDATORY artifacts, LICENSE artifacts and artifacts

            // was created from HEAT.meta
            .filter(this::isNonMetaArtifact).collect(toList());
        List<String> artifactsToIgnore = new ArrayList<>();
        // collect IDs of Artifacts of VF which belongs to any group
        if (resource.getGroups() != null) {
            resource.getGroups().forEach(g -> {
                if (g.getArtifacts() != null && !g.getArtifacts().isEmpty()) {
                    artifactsToIgnore.addAll(g.getArtifacts());
                }
            });
        }
        existingArtifacts = existingArtifacts.stream()
            // filter artifacts which belongs to any group
            .filter(a -> !artifactsToIgnore.contains(a.getUniqueId())).collect(toList());
        return organizeVfCsarArtifactsByArtifactOperation(artifactPathAndNameList, existingArtifacts, resource, user);
    }

    private boolean isNonMetaArtifact(ArtifactDefinition artifact) {
        return !artifact.getMandatory() && artifact.getArtifactName() != null && isValidArtifactType(artifact);
    }

    private boolean isValidArtifactType(ArtifactDefinition artifact) {
        return artifact.getArtifactType() != null && ArtifactTypeEnum.parse(artifact.getArtifactType()) != ArtifactTypeEnum.VENDOR_LICENSE
            && ArtifactTypeEnum.parse(artifact.getArtifactType()) != ArtifactTypeEnum.VF_LICENSE;
    }

    private Resource createResourceInstancesRelations(User user, String yamlName, Resource resource, Resource oldResource,
                                                      Map<String, UploadComponentInstanceInfo> uploadResInstancesMap,
                                                      Map<String, Resource> existingNodeTypesByResourceNames) {
        log.debug("#createResourceInstancesRelations - Going to create relations ");
        loggerSupportability.log(LoggerSupportabilityActions.CREATE_RELATIONS, resource.getComponentMetadataForSupportLog(), StatusCode.STARTED,
            "Start to create relations");
        List<ComponentInstance> componentInstancesList = resource.getComponentInstances();
        if (isEmpty(uploadResInstancesMap) || CollectionUtils.isEmpty(componentInstancesList) &&
            resource.getResourceType() != ResourceTypeEnum.PNF) { // PNF can have no resource instances {
            log.debug("#createResourceInstancesRelations - No instances found in the resource {} is empty, yaml template file name {}, ",
                resource.getUniqueId(), yamlName);
            loggerSupportability.log(LoggerSupportabilityActions.CREATE_RELATIONS, resource.getComponentMetadataForSupportLog(), StatusCode.ERROR,
                "No instances found in the resource: {}, is empty, yaml template file name: {}", resource.getName(), yamlName);
            BeEcompErrorManager.getInstance()
                .logInternalDataError("createResourceInstancesRelations", "No instances found in a resource or nn yaml template. ",
                    ErrorSeverity.ERROR);
            throw new ByActionStatusComponentException(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
        }
        Map<String, List<ComponentInstanceProperty>> instProperties = new HashMap<>();
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilities = new HashMap<>();
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements = new HashMap<>();
        Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts = new HashMap<>();
        Map<String, Map<String, ArtifactDefinition>> instArtifacts = new HashMap<>();
        Map<String, List<AttributeDefinition>> instAttributes = new HashMap<>();
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();
        Map<String, List<ComponentInstanceInput>> instInputs = new HashMap<>();
        Resource finalResource = resource;
        uploadResInstancesMap.values().forEach(
            i -> processComponentInstance(yamlName, finalResource, componentInstancesList,
                componentsUtils.getAllDataTypes(applicationDataTypeCache, resource.getModel()), instProperties, instCapabilities,
                instRequirements, instDeploymentArtifacts, instArtifacts, instAttributes, existingNodeTypesByResourceNames, instInputs, i));
        resource.getComponentInstances().stream().filter(i -> !i.isCreatedFromCsar()).forEach(
            i -> processUiComponentInstance(oldResource, i, instCapabilities, instRequirements, instDeploymentArtifacts, instArtifacts,
                instProperties, instInputs, instAttributes));
        associateComponentInstancePropertiesToComponent(yamlName, resource, instProperties);
        associateComponentInstanceInputsToComponent(yamlName, resource, instInputs);
        associateDeploymentArtifactsToInstances(user, yamlName, resource, instDeploymentArtifacts);
        associateArtifactsToInstances(yamlName, resource, instArtifacts);
        associateOrAddCalculatedCapReq(yamlName, resource, instCapabilities, instRequirements);
        associateInstAttributeToComponentToInstances(yamlName, resource, instAttributes);
        addRelationsToRI(yamlName, resource, uploadResInstancesMap, componentInstancesList, relations);
        associateResourceInstances(yamlName, resource, relations);
        handleSubstitutionMappings(resource, uploadResInstancesMap);
        log.debug("************* in create relations, getResource start");
        loggerSupportability
            .log(LoggerSupportabilityActions.CREATE_RELATIONS, resource.getComponentMetadataForSupportLog(), StatusCode.COMPLETE, "create relations");
        Either<Resource, StorageOperationStatus> eitherGetResource = toscaOperationFacade.getToscaFullElement(resource.getUniqueId());
        log.debug("************* in create relations, getResource end");
        if (eitherGetResource.isRight()) {
            loggerSupportability.log(LoggerSupportabilityActions.CREATE_RELATIONS, resource.getComponentMetadataForSupportLog(), StatusCode.ERROR,
                "ERROR while create relations");
            throw new ByResponseFormatComponentException(
                componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(eitherGetResource.right().value()), resource));
        }
        return eitherGetResource.left().value();
    }

    private void processUiComponentInstance(Resource oldResource, ComponentInstance instance,
                                            Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilities,
                                            Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements,
                                            Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts,
                                            Map<String, Map<String, ArtifactDefinition>> instArtifacts,
                                            Map<String, List<ComponentInstanceProperty>> instProperties,
                                            Map<String, List<ComponentInstanceInput>> instInputs,
                                            Map<String, List<AttributeDefinition>> instAttributes) {
        Optional<ComponentInstance> foundInstance = findInstance(oldResource, instance);
        if (foundInstance.isPresent()) {
            if (MapUtils.isNotEmpty(foundInstance.get().getCapabilities())) {
                instCapabilities.put(instance, foundInstance.get().getCapabilities());
            }
            if (MapUtils.isNotEmpty(foundInstance.get().getRequirements())) {
                instRequirements.put(instance, foundInstance.get().getRequirements());
            }
            if (MapUtils.isNotEmpty(foundInstance.get().getDeploymentArtifacts())) {
                instDeploymentArtifacts.put(instance.getUniqueId(), foundInstance.get().getDeploymentArtifacts());
            }
            if (MapUtils.isNotEmpty(foundInstance.get().getArtifacts())) {
                instArtifacts.put(instance.getUniqueId(), foundInstance.get().getArtifacts());
            }
            if (MapUtils.isNotEmpty(oldResource.getComponentInstancesProperties()) && CollectionUtils
                .isNotEmpty(oldResource.getComponentInstancesProperties().get(foundInstance.get().getUniqueId()))) {
                instProperties.put(instance.getUniqueId(), oldResource.getComponentInstancesProperties().get(foundInstance.get().getUniqueId()));
            }
            if (MapUtils.isNotEmpty(oldResource.getComponentInstancesInputs()) && CollectionUtils
                .isNotEmpty(oldResource.getComponentInstancesInputs().get(foundInstance.get().getUniqueId()))) {
                instInputs.put(instance.getUniqueId(), oldResource.getComponentInstancesInputs().get(foundInstance.get().getUniqueId()));
            }
            if (MapUtils.isNotEmpty(oldResource.getComponentInstancesAttributes()) && CollectionUtils
                .isNotEmpty(oldResource.getComponentInstancesAttributes().get(foundInstance.get().getUniqueId()))) {
                instAttributes.put(instance.getUniqueId(),
                    oldResource.getComponentInstancesAttributes().get(foundInstance.get().getUniqueId()).stream().map(AttributeDefinition::new)
                        .collect(toList()));
            }
        }
    }

    private Optional<ComponentInstance> findInstance(Resource oldResource, ComponentInstance instance) {
        if (oldResource != null && CollectionUtils.isNotEmpty(oldResource.getComponentInstances())) {
            return oldResource.getComponentInstances().stream().filter(i -> i.getName().equals(instance.getName())).findFirst();
        }
        return Optional.empty();
    }

    private void associateResourceInstances(String yamlName, Resource resource, List<RequirementCapabilityRelDef> relations) {
        Either<List<RequirementCapabilityRelDef>, StorageOperationStatus> relationsEither = toscaOperationFacade
            .associateResourceInstances(resource, resource.getUniqueId(), relations);
        if (relationsEither.isRight() && relationsEither.right().value() != StorageOperationStatus.NOT_FOUND) {
            StorageOperationStatus status = relationsEither.right().value();
            log.debug("failed to associate instances of resource {} status is {}", resource.getUniqueId(), status);
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(status), yamlName);
        } else {
            setResourceInstanceRelationsOnComponent(resource, relationsEither.left().value());
        }
    }

    private void associateInstAttributeToComponentToInstances(String yamlName, Resource resource,
                                                              Map<String, List<AttributeDefinition>> instAttributes) {
        StorageOperationStatus addArtToInst;
        addArtToInst = toscaOperationFacade.associateInstAttributeToComponentToInstances(instAttributes, resource);
        if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate attributes of resource {} status is {}", resource.getUniqueId(), addArtToInst);
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName);
        }
    }

    private void associateOrAddCalculatedCapReq(String yamlName, Resource resource,
                                                Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilities,
                                                Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements) {
        StorageOperationStatus addArtToInst;
        addArtToInst = toscaOperationFacade.associateOrAddCalculatedCapReq(instCapabilities, instRequirements, resource);
        if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate cap and req of resource {} status is {}", resource.getUniqueId(), addArtToInst);
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName);
        }
    }

    private void associateArtifactsToInstances(String yamlName, Resource resource, Map<String, Map<String, ArtifactDefinition>> instArtifacts) {
        StorageOperationStatus addArtToInst;
        addArtToInst = toscaOperationFacade.associateArtifactsToInstances(instArtifacts, resource);
        if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate artifact of resource {} status is {}", resource.getUniqueId(), addArtToInst);
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName);
        }
    }

    private void associateDeploymentArtifactsToInstances(User user, String yamlName, Resource resource,
                                                         Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts) {
        StorageOperationStatus addArtToInst = toscaOperationFacade.associateDeploymentArtifactsToInstances(instDeploymentArtifacts, resource, user);
        if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate artifact of resource {} status is {}", resource.getUniqueId(), addArtToInst);
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName);
        }
    }

    private void associateComponentInstanceInputsToComponent(String yamlName, Resource resource,
                                                             Map<String, List<ComponentInstanceInput>> instInputs) {
        if (MapUtils.isNotEmpty(instInputs)) {
            Either<Map<String, List<ComponentInstanceInput>>, StorageOperationStatus> addInputToInst = toscaOperationFacade
                .associateComponentInstanceInputsToComponent(instInputs, resource.getUniqueId());
            if (addInputToInst.isRight()) {
                StorageOperationStatus addInputToInstError = addInputToInst.right().value();
                log.debug("failed to associate inputs value of resource {} status is {}", resource.getUniqueId(), addInputToInstError);
                throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(addInputToInstError), yamlName);
            }
            setComponentInstanceInputsOnComponent(resource, instInputs);
        }
    }

    private void setComponentInstanceInputsOnComponent(Resource resource, Map<String, List<ComponentInstanceInput>> instInputs) {
        Map<String, List<ComponentInstanceInput>> componentInstancesInputs = resource.getComponentInstancesInputs();
        if (componentInstancesInputs == null) {
            componentInstancesInputs = new HashMap<>();
        }
        componentInstancesInputs.putAll(instInputs);
        resource.setComponentInstancesInputs(componentInstancesInputs);
    }

    private void associateComponentInstancePropertiesToComponent(String yamlName, Resource resource,
                                                                 Map<String, List<ComponentInstanceProperty>> instProperties) {
        Either<Map<String, List<ComponentInstanceProperty>>, StorageOperationStatus> addPropToInst = toscaOperationFacade
            .associateComponentInstancePropertiesToComponent(instProperties, resource.getUniqueId());
        if (addPropToInst.isRight()) {
            loggerSupportability.log(LoggerSupportabilityActions.CREATE_RELATIONS, resource.getComponentMetadataForSupportLog(), StatusCode.ERROR,
                "ERROR while  associate compnent insatnce properties of resource: {} status is: {}", resource.getName(),
                addPropToInst.right().value());
            StorageOperationStatus storageOperationStatus = addPropToInst.right().value();
            log.debug("failed to associate properties of resource {} status is {}", resource.getUniqueId(), storageOperationStatus);
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(storageOperationStatus), yamlName);
        }
        setComponentInstancePropertiesOnComponent(resource, instProperties);
    }

    private void setComponentInstancePropertiesOnComponent(Resource resource, Map<String, List<ComponentInstanceProperty>> instProperties) {
        Map<String, List<ComponentInstanceProperty>> componentInstanceProps = resource.getComponentInstancesProperties();
        if (componentInstanceProps == null) {
            componentInstanceProps = new HashMap<>();
        }
        componentInstanceProps.putAll(instProperties);
        resource.setComponentInstancesProperties(componentInstanceProps);
    }

    private void handleSubstitutionMappings(Resource resource, Map<String, UploadComponentInstanceInfo> uploadResInstancesMap) {
        Either<Resource, StorageOperationStatus> getResourceRes = null;
        if (resource.getResourceType() == ResourceTypeEnum.CVFC) {
            getResourceRes = updateCalculatedCapReqWithSubstitutionMappings(resource, uploadResInstancesMap);
        } else if (StringUtils.isNotEmpty(resource.getModel()) && resource.getResourceType() == ResourceTypeEnum.VF) {
            getResourceRes = updateCalculatedCapReqWithSubstitutionMappingsForVf(resource, uploadResInstancesMap);
        }
        if (getResourceRes != null && getResourceRes.isRight()) {
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(getResourceRes.right().value()), resource);
            throw new ByResponseFormatComponentException(responseFormat);
        }

    }

    private void addRelationsToRI(String yamlName, Resource resource, Map<String, UploadComponentInstanceInfo> uploadResInstancesMap,
                                  List<ComponentInstance> componentInstancesList, List<RequirementCapabilityRelDef> relations) {
        for (Entry<String, UploadComponentInstanceInfo> entry : uploadResInstancesMap.entrySet()) {
            UploadComponentInstanceInfo uploadComponentInstanceInfo = entry.getValue();
            ComponentInstance currentCompInstance = null;
            for (ComponentInstance compInstance : componentInstancesList) {
                if (compInstance.getName().equals(uploadComponentInstanceInfo.getName())) {
                    currentCompInstance = compInstance;
                    break;
                }
            }
            if (currentCompInstance == null) {
                log.debug(COMPONENT_INSTANCE_WITH_NAME_IN_RESOURCE, uploadComponentInstanceInfo.getName(), resource.getUniqueId());
                BeEcompErrorManager.getInstance()
                    .logInternalDataError(COMPONENT_INSTANCE_WITH_NAME + uploadComponentInstanceInfo.getName() + IN_RESOURCE, resource.getUniqueId(),
                        ErrorSeverity.ERROR);
                throw new ByActionStatusComponentException(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
            }
            ResponseFormat addRelationToRiRes = addRelationToRI(yamlName, resource, entry.getValue(), relations);
            if (addRelationToRiRes.getStatus() != 200) {
                throw new ByResponseFormatComponentException(addRelationToRiRes);
            }
        }
    }

    private void setResourceInstanceRelationsOnComponent(Resource resource, List<RequirementCapabilityRelDef> relations) {
        if (resource.getComponentInstancesRelations() != null) {
            resource.getComponentInstancesRelations().addAll(relations);
        } else {
            resource.setComponentInstancesRelations(relations);
        }
    }

    private void processComponentInstance(String yamlName, Resource resource, List<ComponentInstance> componentInstancesList,
                                          Map<String, DataTypeDefinition> allDataTypes,
                                          Map<String, List<ComponentInstanceProperty>> instProperties,
                                          Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilties,
                                          Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements,
                                          Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts,
                                          Map<String, Map<String, ArtifactDefinition>> instArtifacts,
                                          Map<String, List<AttributeDefinition>> instAttributes, Map<String, Resource> originCompMap,
                                          Map<String, List<ComponentInstanceInput>> instInputs,
                                          UploadComponentInstanceInfo uploadComponentInstanceInfo) {
        Optional<ComponentInstance> currentCompInstanceOpt = componentInstancesList.stream()
            .filter(i -> i.getName().equals(uploadComponentInstanceInfo.getName())).findFirst();
        if (currentCompInstanceOpt.isEmpty()) {
            log.debug(COMPONENT_INSTANCE_WITH_NAME_IN_RESOURCE, uploadComponentInstanceInfo.getName(), resource.getUniqueId());
            BeEcompErrorManager.getInstance()
                .logInternalDataError(COMPONENT_INSTANCE_WITH_NAME + uploadComponentInstanceInfo.getName() + IN_RESOURCE, resource.getUniqueId(),
                    ErrorSeverity.ERROR);
            throw new ByActionStatusComponentException(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
        }
        ComponentInstance currentCompInstance = currentCompInstanceOpt.get();
        String resourceInstanceId = currentCompInstance.getUniqueId();
        Resource originResource = getOriginResource(originCompMap, currentCompInstance);
        if (isNotEmpty(originResource.getRequirements())) {
            instRequirements.put(currentCompInstance, originResource.getRequirements());
        }
        if (isNotEmpty(originResource.getCapabilities())) {
            processComponentInstanceCapabilities(allDataTypes, instCapabilties, uploadComponentInstanceInfo, currentCompInstance, originResource);
        }
        if (originResource.getDeploymentArtifacts() != null && !originResource.getDeploymentArtifacts().isEmpty()) {
            instDeploymentArtifacts.put(resourceInstanceId, originResource.getDeploymentArtifacts());
        }
        if (originResource.getArtifacts() != null && !originResource.getArtifacts().isEmpty()) {
            instArtifacts.put(resourceInstanceId, originResource.getArtifacts());
        }
        if (originResource.getAttributes() != null && !originResource.getAttributes().isEmpty()) {
            instAttributes.put(resourceInstanceId, originResource.getAttributes());
        }
        if (originResource.getResourceType() != ResourceTypeEnum.CVFC) {
            ResponseFormat addPropertiesValueToRiRes = addPropertyValuesToRi(uploadComponentInstanceInfo, resource, originResource,
                currentCompInstance, instProperties, allDataTypes);
            if (addPropertiesValueToRiRes.getStatus() != 200) {
                throw new ByResponseFormatComponentException(addPropertiesValueToRiRes);
            }
        } else {
            addInputsValuesToRi(uploadComponentInstanceInfo, resource, originResource, currentCompInstance, instInputs, allDataTypes);
        }
    }

    private Resource getOriginResource(Map<String, Resource> originCompMap, ComponentInstance currentCompInstance) {
        Resource originResource;
        if (!originCompMap.containsKey(currentCompInstance.getComponentUid())) {
            Either<Resource, StorageOperationStatus> getOriginResourceRes = toscaOperationFacade
                .getToscaFullElement(currentCompInstance.getComponentUid());
            if (getOriginResourceRes.isRight()) {
                log.debug("failed to fetch resource with uniqueId {} and tosca component name {} status is {}", currentCompInstance.getComponentUid(),
                    currentCompInstance.getToscaComponentName(), getOriginResourceRes);
                throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(getOriginResourceRes.right().value()),
                    currentCompInstance.getComponentUid());
            }
            originResource = getOriginResourceRes.left().value();
            originCompMap.put(originResource.getUniqueId(), originResource);
        } else {
            originResource = originCompMap.get(currentCompInstance.getComponentUid());
        }
        return originResource;
    }

    private void processComponentInstanceCapabilities(Map<String, DataTypeDefinition> allDataTypes,
                                                      Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilties,
                                                      UploadComponentInstanceInfo uploadComponentInstanceInfo, ComponentInstance currentCompInstance,
                                                      Resource originResource) {
        Map<String, List<CapabilityDefinition>> originCapabilities;
        if (isNotEmpty(uploadComponentInstanceInfo.getCapabilities())) {
            originCapabilities = new HashMap<>();
            Map<String, Map<String, UploadPropInfo>> newPropertiesMap = new HashMap<>();
            originResource.getCapabilities().forEach((k, v) -> addCapabilities(originCapabilities, k, v));
            uploadComponentInstanceInfo.getCapabilities().values().forEach(l -> addCapabilitiesProperties(newPropertiesMap, l));
            updateCapabilityPropertiesValues(originCapabilities, newPropertiesMap, allDataTypes);
        } else {
            originCapabilities = originResource.getCapabilities();
        }
        instCapabilties.put(currentCompInstance, originCapabilities);
    }

    private void updateCapabilityPropertiesValues(Map<String, List<CapabilityDefinition>> originCapabilities,
                                                  Map<String, Map<String, UploadPropInfo>> newPropertiesMap,
                                                  Map<String, DataTypeDefinition> allDataTypes) {
        originCapabilities.values().stream().flatMap(Collection::stream).filter(c -> newPropertiesMap.containsKey(c.getName()))
            .forEach(c -> updatePropertyValues(c.getProperties(), newPropertiesMap.get(c.getName()), allDataTypes));
    }

    private void addCapabilitiesProperties(Map<String, Map<String, UploadPropInfo>> newPropertiesMap, List<UploadCapInfo> capabilities) {
        for (UploadCapInfo capability : capabilities) {
            if (isNotEmpty(capability.getProperties())) {
                newPropertiesMap.put(capability.getName(), capability.getProperties().stream().collect(toMap(UploadInfo::getName, p -> p)));
            }
        }
    }

    private void addCapabilities(Map<String, List<CapabilityDefinition>> originCapabilities, String type, List<CapabilityDefinition> capabilities) {
        List<CapabilityDefinition> list = capabilities.stream().map(CapabilityDefinition::new).collect(toList());
        originCapabilities.put(type, list);
    }

    private void updatePropertyValues(List<ComponentInstanceProperty> properties, Map<String, UploadPropInfo> newProperties,
                                      Map<String, DataTypeDefinition> allDataTypes) {
        properties.forEach(p -> updatePropertyValue(p, newProperties.get(p.getName()), allDataTypes));
    }

    private String updatePropertyValue(ComponentInstanceProperty property, UploadPropInfo propertyInfo,
                                       Map<String, DataTypeDefinition> allDataTypes) {
        String value = null;
        List<GetInputValueDataDefinition> getInputs = null;
        boolean isValidate = true;
        if (null != propertyInfo && propertyInfo.getValue() != null) {
            getInputs = propertyInfo.getGet_input();
            isValidate = getInputs == null || getInputs.isEmpty();
            if (isValidate) {
                value = getPropertyJsonStringValue(propertyInfo.getValue(), property.getType());
            } else {
                value = getPropertyJsonStringValue(propertyInfo.getValue(), TypeUtils.ToscaTagNamesEnum.GET_INPUT.getElementName());
            }
        }
        property.setValue(value);
        return validatePropValueBeforeCreate(property, value, isValidate, allDataTypes);
    }

    private Either<Resource, StorageOperationStatus> updateCalculatedCapReqWithSubstitutionMappings(Resource resource,
                                                                                                    Map<String, UploadComponentInstanceInfo> uploadResInstancesMap) {
        Either<Resource, StorageOperationStatus> updateRes = null;
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> updatedInstCapabilities = new HashMap<>();
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> updatedInstRequirements = new HashMap<>();

        StorageOperationStatus status = toscaOperationFacade.deleteAllCalculatedCapabilitiesRequirements(resource.getUniqueId());
        if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
            log.debug("Failed to delete all calculated capabilities and requirements of resource {} upon update. Status is {}",
                resource.getUniqueId(), status);
            updateRes = Either.right(status);
        }
        if (updateRes == null) {
            fillUpdatedInstCapabilitiesRequirements(resource.getComponentInstances(), uploadResInstancesMap, updatedInstCapabilities,
                updatedInstRequirements);
            status = toscaOperationFacade.associateOrAddCalculatedCapReq(updatedInstCapabilities, updatedInstRequirements, resource);
            if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
                log.debug(
                    "Failed to associate capabilities and requirementss of resource {}, updated according to a substitution mapping. Status is {}",
                    resource.getUniqueId(), status);
                updateRes = Either.right(status);
            }
        }
        if (updateRes == null) {
            updateRes = Either.left(resource);
        }
        return updateRes;
    }

    private Either<Resource, StorageOperationStatus> updateCalculatedCapReqWithSubstitutionMappingsForVf(final Resource resource,
                                                                                                         final Map<String, UploadComponentInstanceInfo> uploadResInstancesMap) {
        Either<Resource, StorageOperationStatus> updateRes = null;
        final Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> updatedInstCapabilities = new HashMap<>();
        final Map<ComponentInstance, Map<String, List<RequirementDefinition>>> updatedInstRequirements = new HashMap<>();

        resource.getComponentInstances().forEach(i -> {
            setExternalCapabilities(updatedInstCapabilities, i, uploadResInstancesMap.get(i.getName()).getCapabilitiesNamesToUpdate());
            setExternalRequirements(updatedInstRequirements, i, uploadResInstancesMap.get(i.getName()).getRequirementsNamesToUpdate());
        });

        final StorageOperationStatus status = toscaOperationFacade.updateCalculatedCapabilitiesRequirements(updatedInstCapabilities,
            updatedInstRequirements, resource);
        if (status != StorageOperationStatus.OK) {
            log.debug(
                "Failed to update capabilities and requirements of resource {}. Status is {}",
                resource.getUniqueId(), status);
            updateRes = Either.right(status);
        }

        if (updateRes == null) {
            updateRes = Either.left(resource);
        }
        return updateRes;
    }

    private void fillUpdatedInstCapabilitiesRequirements(List<ComponentInstance> componentInstances,
                                                         Map<String, UploadComponentInstanceInfo> uploadResInstancesMap,
                                                         Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> updatedInstCapabilities,
                                                         Map<ComponentInstance, Map<String, List<RequirementDefinition>>> updatedInstRequirements) {
        componentInstances.forEach(i -> {
            fillUpdatedInstCapabilities(updatedInstCapabilities, i, uploadResInstancesMap.get(i.getName()).getCapabilitiesNamesToUpdate());
            fillUpdatedInstRequirements(updatedInstRequirements, i, uploadResInstancesMap.get(i.getName()).getRequirementsNamesToUpdate());
        });
    }

    private void fillUpdatedInstRequirements(Map<ComponentInstance, Map<String, List<RequirementDefinition>>> updatedInstRequirements,
                                             ComponentInstance instance, Map<String, String> requirementsNamesToUpdate) {
        Map<String, List<RequirementDefinition>> updatedRequirements = new HashMap<>();
        Set<String> updatedReqNames = new HashSet<>();
        if (isNotEmpty(requirementsNamesToUpdate)) {
            for (Map.Entry<String, List<RequirementDefinition>> requirements : instance.getRequirements().entrySet()) {
                updatedRequirements.put(requirements.getKey(), requirements.getValue().stream().filter(
                        r -> requirementsNamesToUpdate.containsKey(r.getName()) && !updatedReqNames.contains(requirementsNamesToUpdate.get(r.getName())))
                    .map(r -> {
                        r.setParentName(r.getName());
                        r.setName(requirementsNamesToUpdate.get(r.getName()));
                        updatedReqNames.add(r.getName());
                        return r;
                    }).collect(toList()));
            }
        }
        if (isNotEmpty(updatedRequirements)) {
            updatedInstRequirements.put(instance, updatedRequirements);
        }
    }

    private void setExternalRequirements(
        final Map<ComponentInstance, Map<String, List<RequirementDefinition>>> updatedInstRequirements,
        final ComponentInstance instance, final Map<String, String> requirementsNamesToUpdate) {
        final Map<String, List<RequirementDefinition>> updatedRequirements = new HashMap<>();
        final Set<String> updatedReqNames = new HashSet<>();
        if (isNotEmpty(requirementsNamesToUpdate)) {
            for (Map.Entry<String, List<RequirementDefinition>> requirements : instance.getRequirements().entrySet()) {
                updatedRequirements.put(requirements.getKey(),
                    requirements.getValue().stream()
                        .filter(r -> requirementsNamesToUpdate.containsKey(r.getName())
                            && !updatedReqNames.contains(requirementsNamesToUpdate.get(r.getName())))
                        .map(r -> {
                            r.setExternal(true);
                            r.setExternalName(requirementsNamesToUpdate.get(r.getName()));
                            updatedReqNames.add(r.getName());
                            return r;
                        }).collect(toList()));
            }
        }
        if (isNotEmpty(updatedRequirements)) {
            updatedInstRequirements.put(instance, updatedRequirements);
        }
    }

    private void setExternalCapabilities(
        final Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> updatedInstCapabilties,
        final ComponentInstance instance, Map<String, String> capabilitiesNamesToUpdate) {
        final Map<String, List<CapabilityDefinition>> updatedCapabilities = new HashMap<>();
        final Set<String> updatedCapNames = new HashSet<>();
        if (isNotEmpty(capabilitiesNamesToUpdate)) {
            for (Map.Entry<String, List<CapabilityDefinition>> requirements : instance.getCapabilities().entrySet()) {
                updatedCapabilities.put(requirements.getKey(),
                    requirements.getValue().stream()
                        .filter(c -> capabilitiesNamesToUpdate.containsKey(c.getName())
                            && !updatedCapNames.contains(capabilitiesNamesToUpdate.get(c.getName())))
                        .map(c -> {
                            c.setExternal(true);
                            c.setExternalName(capabilitiesNamesToUpdate.get(c.getName()));
                            updatedCapNames.add(c.getName());
                            return c;
                        }).collect(toList()));
            }
        }
        if (isNotEmpty(updatedCapabilities)) {
            updatedInstCapabilties.put(instance, updatedCapabilities);
        }
    }

    private void fillUpdatedInstCapabilities(Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> updatedInstCapabilties,
                                             ComponentInstance instance, Map<String, String> capabilitiesNamesToUpdate) {
        Map<String, List<CapabilityDefinition>> updatedCapabilities = new HashMap<>();
        Set<String> updatedCapNames = new HashSet<>();
        if (isNotEmpty(capabilitiesNamesToUpdate)) {
            for (Map.Entry<String, List<CapabilityDefinition>> requirements : instance.getCapabilities().entrySet()) {
                updatedCapabilities.put(requirements.getKey(), requirements.getValue().stream().filter(
                        c -> capabilitiesNamesToUpdate.containsKey(c.getName()) && !updatedCapNames.contains(capabilitiesNamesToUpdate.get(c.getName())))
                    .map(c -> {
                        c.setParentName(c.getName());
                        c.setName(capabilitiesNamesToUpdate.get(c.getName()));
                        updatedCapNames.add(c.getName());
                        return c;
                    }).collect(toList()));
            }
        }
        if (isNotEmpty(updatedCapabilities)) {
            updatedInstCapabilties.put(instance, updatedCapabilities);
        }
    }

    private ResponseFormat addRelationToRI(String yamlName, Resource resource, UploadComponentInstanceInfo nodesInfoValue,
                                           List<RequirementCapabilityRelDef> relations) {
        List<ComponentInstance> componentInstancesList = resource.getComponentInstances();
        ComponentInstance currentCompInstance = null;
        for (ComponentInstance compInstance : componentInstancesList) {
            if (compInstance.getName().equals(nodesInfoValue.getName())) {
                currentCompInstance = compInstance;
                break;
            }
        }
        if (currentCompInstance == null) {
            log.debug(COMPONENT_INSTANCE_WITH_NAME_IN_RESOURCE, nodesInfoValue.getName(), resource.getUniqueId());
            BeEcompErrorManager.getInstance()
                .logInternalDataError(COMPONENT_INSTANCE_WITH_NAME + nodesInfoValue.getName() + IN_RESOURCE, resource.getUniqueId(),
                    ErrorSeverity.ERROR);
            return componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
        }
        String resourceInstanceId = currentCompInstance.getUniqueId();
        Map<String, List<UploadReqInfo>> regMap = nodesInfoValue.getRequirements();
        if (regMap != null) {
            for (Entry<String, List<UploadReqInfo>> nodesRegInfoEntry : regMap.entrySet()) {
                List<UploadReqInfo> uploadRegInfoList = nodesRegInfoEntry.getValue();
                for (UploadReqInfo uploadRegInfo : uploadRegInfoList) {
                    log.debug("Going to create  relation {}", uploadRegInfo.getName());
                    loggerSupportability
                        .log(LoggerSupportabilityActions.CREATE_RELATIONS, resource.getComponentMetadataForSupportLog(), StatusCode.STARTED,
                            "Started to create relations on instance: {}", uploadRegInfo.getName());
                    String regName = uploadRegInfo.getName();
                    RequirementCapabilityRelDef regCapRelDef = new RequirementCapabilityRelDef();
                    regCapRelDef.setFromNode(resourceInstanceId);
                    log.debug("try to find available requirement {} ", regName);
                    Either<RequirementDefinition, ResponseFormat> eitherReqStatus = findAviableRequiremen(regName, yamlName, nodesInfoValue,
                        currentCompInstance, uploadRegInfo.getCapabilityName());
                    if (eitherReqStatus.isRight()) {
                        log.debug("failed to find available requirement {} status is {}", regName, eitherReqStatus.right().value());
                        loggerSupportability
                            .log(LoggerSupportabilityActions.CREATE_RELATIONS, resource.getComponentMetadataForSupportLog(), StatusCode.ERROR,
                                "ERROR while search available requirement {} status is: {}", regName, eitherReqStatus.right().value());
                        return eitherReqStatus.right().value();
                    }
                    RequirementDefinition validReq = eitherReqStatus.left().value();
                    List<CapabilityRequirementRelationship> reqAndRelationshipPairList = regCapRelDef.getRelationships();
                    if (reqAndRelationshipPairList == null) {
                        reqAndRelationshipPairList = new ArrayList<>();
                    }
                    RelationshipInfo reqAndRelationshipPair = new RelationshipInfo();
                    reqAndRelationshipPair.setRequirement(regName);
                    reqAndRelationshipPair.setRequirementOwnerId(validReq.getOwnerId());
                    reqAndRelationshipPair.setRequirementUid(validReq.getUniqueId());
                    RelationshipImpl relationship = new RelationshipImpl();
                    relationship.setType(validReq.getCapability());
                    reqAndRelationshipPair.setRelationships(relationship);
                    ComponentInstance currentCapCompInstance = null;
                    for (ComponentInstance compInstance : componentInstancesList) {
                        if (compInstance.getName().equals(uploadRegInfo.getNode())) {
                            currentCapCompInstance = compInstance;
                            break;
                        }
                    }
                    if (currentCapCompInstance == null) {
                        log.debug("The component instance  with name {} not found on resource {} ", uploadRegInfo.getNode(), resource.getUniqueId());
                        loggerSupportability
                            .log(LoggerSupportabilityActions.CREATE_RELATIONS, resource.getComponentMetadataForSupportLog(), StatusCode.ERROR,
                                "ERROR component instance  with name: {} not found on resource: {}", uploadRegInfo.getNode(), resource.getUniqueId());
                        BeEcompErrorManager.getInstance()
                            .logInternalDataError(COMPONENT_INSTANCE_WITH_NAME + uploadRegInfo.getNode() + IN_RESOURCE, resource.getUniqueId(),
                                ErrorSeverity.ERROR);
                        return componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
                    }
                    regCapRelDef.setToNode(currentCapCompInstance.getUniqueId());
                    log.debug("try to find aviable Capability  req name is {} ", validReq.getName());
                    CapabilityDefinition aviableCapForRel = findAvailableCapabilityByTypeOrName(validReq, currentCapCompInstance, uploadRegInfo);
                    if (aviableCapForRel == null) {
                        log.debug("aviable capability was not found. req name is {} component instance is {}", validReq.getName(),
                            currentCapCompInstance.getUniqueId());
                        loggerSupportability
                            .log(LoggerSupportabilityActions.CREATE_RELATIONS, resource.getComponentMetadataForSupportLog(), StatusCode.ERROR,
                                "ERROR available capability was not found. req name is: {} component instance is: {}", validReq.getName(),
                                currentCapCompInstance.getUniqueId());
                        BeEcompErrorManager.getInstance().logInternalDataError(
                            "aviable capability was not found. req name is " + validReq.getName() + " component instance is " + currentCapCompInstance
                                .getUniqueId(), resource.getUniqueId(), ErrorSeverity.ERROR);
                        return componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
                    }
                    reqAndRelationshipPair.setCapability(aviableCapForRel.getName());
                    reqAndRelationshipPair.setCapabilityUid(aviableCapForRel.getUniqueId());
                    reqAndRelationshipPair.setCapabilityOwnerId(aviableCapForRel.getOwnerId());
                    CapabilityRequirementRelationship capReqRel = new CapabilityRequirementRelationship();
                    capReqRel.setRelation(reqAndRelationshipPair);
                    reqAndRelationshipPairList.add(capReqRel);
                    regCapRelDef.setRelationships(reqAndRelationshipPairList);
                    relations.add(regCapRelDef);
                }
            }
        } else if (resource.getResourceType() != ResourceTypeEnum.CVFC) {
            return componentsUtils.getResponseFormat(ActionStatus.OK, yamlName);
        }
        return componentsUtils.getResponseFormat(ActionStatus.OK);
    }

    private void addInputsValuesToRi(UploadComponentInstanceInfo uploadComponentInstanceInfo, Resource resource, Resource originResource,
                                     ComponentInstance currentCompInstance, Map<String, List<ComponentInstanceInput>> instInputs,
                                     Map<String, DataTypeDefinition> allDataTypes) {
        Map<String, List<UploadPropInfo>> propMap = uploadComponentInstanceInfo.getProperties();
        if (MapUtils.isNotEmpty(propMap)) {
            Map<String, InputDefinition> currPropertiesMap = new HashMap<>();
            List<ComponentInstanceInput> instPropList = new ArrayList<>();
            if (CollectionUtils.isEmpty(originResource.getInputs())) {
                log.debug("failed to find properties ");
                loggerSupportability.log(LoggerSupportabilityActions.CREATE_INPUTS, resource.getComponentMetadataForSupportLog(), StatusCode.ERROR,
                    "ERROR while try to find properties");
                throw new ByActionStatusComponentException(ActionStatus.PROPERTY_NOT_FOUND);
            }
            originResource.getInputs().forEach(p -> addInput(currPropertiesMap, p));
            for (List<UploadPropInfo> propertyList : propMap.values()) {
                processProperty(resource, currentCompInstance, allDataTypes, currPropertiesMap, instPropList, propertyList);
            }
            currPropertiesMap.values().forEach(p -> instPropList.add(new ComponentInstanceInput(p)));
            instInputs.put(currentCompInstance.getUniqueId(), instPropList);
        }
    }

    private void processProperty(Resource resource, ComponentInstance currentCompInstance, Map<String, DataTypeDefinition> allDataTypes,
                                 Map<String, InputDefinition> currPropertiesMap, List<ComponentInstanceInput> instPropList,
                                 List<UploadPropInfo> propertyList) {
        UploadPropInfo propertyInfo = propertyList.get(0);
        String propName = propertyInfo.getName();
        if (!currPropertiesMap.containsKey(propName)) {
            loggerSupportability.log(LoggerSupportabilityActions.PROPERTY, resource.getComponentMetadataForSupportLog(), StatusCode.ERROR,
                "ERROR failed to find property: {}", propName);
            log.debug("failed to find property {} ", propName);
            throw new ByActionStatusComponentException(ActionStatus.PROPERTY_NOT_FOUND, propName);
        }
        InputDefinition curPropertyDef = currPropertiesMap.get(propName);
        ComponentInstanceInput property = null;
        String value = null;
        List<GetInputValueDataDefinition> getInputs = null;
        boolean isValidate = true;
        if (propertyInfo.getValue() != null) {
            getInputs = propertyInfo.getGet_input();
            isValidate = getInputs == null || getInputs.isEmpty();
            if (isValidate) {
                value = getPropertyJsonStringValue(propertyInfo.getValue(), curPropertyDef.getType());
            } else {
                value = getPropertyJsonStringValue(propertyInfo.getValue(), TypeUtils.ToscaTagNamesEnum.GET_INPUT.getElementName());
            }
        }
        property = new ComponentInstanceInput(curPropertyDef, value, null);
        String validPropertyVAlue = validatePropValueBeforeCreate(property, value, isValidate, allDataTypes);
        property.setValue(validPropertyVAlue);
        if (isNotEmpty(getInputs)) {
            List<GetInputValueDataDefinition> getInputValues = new ArrayList<>();
            for (GetInputValueDataDefinition getInput : getInputs) {
                List<InputDefinition> inputs = resource.getInputs();
                if (CollectionUtils.isEmpty(inputs)) {
                    loggerSupportability.log(LoggerSupportabilityActions.PROPERTY, resource.getComponentMetadataForSupportLog(), StatusCode.ERROR,
                        "ERROR Failed to add property: " + propName + " to resource instance: {}. Inputs list is empty ",
                        currentCompInstance.getUniqueId());
                    log.debug("Failed to add property {} to resource instance {}. Inputs list is empty ", property,
                        currentCompInstance.getUniqueId());
                    throw new ByActionStatusComponentException(ActionStatus.INVALID_CONTENT);
                }
                Optional<InputDefinition> optional = inputs.stream().filter(p -> p.getName().equals(getInput.getInputName())).findAny();
                if (optional.isEmpty()) {
                    loggerSupportability.log(LoggerSupportabilityActions.PROPERTY, resource.getComponentMetadataForSupportLog(), StatusCode.ERROR,
                        "ERROR Failed to find input: " + getInput.getInputName());
                    log.debug("Failed to find input {} ", getInput.getInputName());
                    // @@TODO error message
                    throw new ByActionStatusComponentException(ActionStatus.INVALID_CONTENT);
                }
                InputDefinition input = optional.get();
                getInput.setInputId(input.getUniqueId());
                getInputValues.add(getInput);
                GetInputValueDataDefinition getInputIndex = getInput.getGetInputIndex();
                processGetInput(getInputValues, inputs, getInputIndex);
            }
            property.setGetInputValues(getInputValues);
        }
        instPropList.add(property);
        // delete overriden property
        currPropertiesMap.remove(property.getName());
    }

    private void processGetInput(List<GetInputValueDataDefinition> getInputValues, List<InputDefinition> inputs,
                                 GetInputValueDataDefinition getInputIndex) {
        Optional<InputDefinition> optional;
        if (getInputIndex != null) {
            optional = inputs.stream().filter(p -> p.getName().equals(getInputIndex.getInputName())).findAny();
            if (optional.isEmpty()) {
                log.debug("Failed to find input {} ", getInputIndex.getInputName());
                // @@TODO error message
                throw new ByActionStatusComponentException(ActionStatus.INVALID_CONTENT);
            }
            InputDefinition inputIndex = optional.get();
            getInputIndex.setInputId(inputIndex.getUniqueId());
            getInputValues.add(getInputIndex);
        }
    }

    private void addInput(Map<String, InputDefinition> currPropertiesMap, InputDefinition prop) {
        String propName = prop.getName();
        if (!currPropertiesMap.containsKey(propName)) {
            currPropertiesMap.put(propName, prop);
        }
    }

    private ResponseFormat addPropertyValuesToRi(UploadComponentInstanceInfo uploadComponentInstanceInfo, Resource resource, Resource originResource,
                                                 ComponentInstance currentCompInstance, Map<String, List<ComponentInstanceProperty>> instProperties,
                                                 Map<String, DataTypeDefinition> allDataTypes) {
        Map<String, List<UploadPropInfo>> propMap = uploadComponentInstanceInfo.getProperties();
        Map<String, PropertyDefinition> currPropertiesMap = new HashMap<>();
        List<PropertyDefinition> listFromMap = originResource.getProperties();
        if ((propMap != null && !propMap.isEmpty()) && (listFromMap == null || listFromMap.isEmpty())) {
            loggerSupportability.log(LoggerSupportabilityActions.PROPERTY, resource.getComponentMetadataForSupportLog(), StatusCode.ERROR,
                "ERROR Failed to find properties");
            log.debug("failed to find properties");
            return componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND);
        }
        if (listFromMap == null || listFromMap.isEmpty()) {
            return componentsUtils.getResponseFormat(ActionStatus.OK);
        }
        for (PropertyDefinition prop : listFromMap) {
            String propName = prop.getName();
            if (!currPropertiesMap.containsKey(propName)) {
                currPropertiesMap.put(propName, prop);
            }
        }
        List<ComponentInstanceProperty> instPropList = new ArrayList<>();
        if (propMap != null && propMap.size() > 0) {
            for (List<UploadPropInfo> propertyList : propMap.values()) {
                UploadPropInfo propertyInfo = propertyList.get(0);
                String propName = propertyInfo.getName();
                if (!currPropertiesMap.containsKey(propName)) {
                    log.debug("failed to find property {} ", propName);
                    loggerSupportability.log(LoggerSupportabilityActions.PROPERTY, resource.getComponentMetadataForSupportLog(), StatusCode.ERROR,
                        "ERROR Failed to find property: {}", propName);
                    return componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, propName);
                }
                PropertyDefinition curPropertyDef = currPropertiesMap.get(propName);
                ComponentInstanceProperty property = null;
                String value = null;
                List<GetInputValueDataDefinition> getInputs = null;
                boolean isValidate = true;
                if (propertyInfo.getValue() != null) {
                    getInputs = propertyInfo.getGet_input();
                    isValidate = getInputs == null || getInputs.isEmpty();
                    if (isValidate) {
                        value = getPropertyJsonStringValue(propertyInfo.getValue(), curPropertyDef.getType());
                    } else {
                        value = getPropertyJsonStringValue(propertyInfo.getValue(), TypeUtils.ToscaTagNamesEnum.GET_INPUT.getElementName());
                    }
                }
                property = new ComponentInstanceProperty(curPropertyDef, value, null);
                String validatePropValue = validatePropValueBeforeCreate(property, value, isValidate, allDataTypes);
                property.setValue(validatePropValue);
                if (getInputs != null && !getInputs.isEmpty()) {
                    List<GetInputValueDataDefinition> getInputValues = new ArrayList<>();
                    for (GetInputValueDataDefinition getInput : getInputs) {
                        List<InputDefinition> inputs = resource.getInputs();
                        if (inputs == null || inputs.isEmpty()) {
                            log.debug("Failed to add property {} to instance. Inputs list is empty ", property);
                            loggerSupportability
                                .log(LoggerSupportabilityActions.PROPERTY, resource.getComponentMetadataForSupportLog(), StatusCode.ERROR,
                                    "Failed to add property: {} to instance. Inputs list is empty", propName);
                            rollbackWithException(ActionStatus.INPUTS_NOT_FOUND,
                                property.getGetInputValues().stream().map(GetInputValueDataDefinition::getInputName).collect(toList()).toString());
                        }
                        Either<InputDefinition, RuntimeException> inputEither = findInputByName(inputs, getInput);
                        if (inputEither.isRight()) {
                            throw inputEither.right().value();
                        } else {
                            InputDefinition input = inputEither.left().value();
                            getInput.setInputId(input.getUniqueId());
                            getInputValues.add(getInput);
                            GetInputValueDataDefinition getInputIndex = getInput.getGetInputIndex();
                            if (getInputIndex != null) {
                                Either<InputDefinition, RuntimeException> newInputEither = findInputByName(inputs, getInputIndex);
                                if (inputEither.isRight()) {
                                    throw newInputEither.right().value();
                                } else {
                                    InputDefinition newInput = newInputEither.left().value();
                                    getInputIndex.setInputId(newInput.getUniqueId());
                                }
                                getInputValues.add(getInputIndex);
                            }
                        }
                    }
                    property.setGetInputValues(getInputValues);
                }
                instPropList.add(property);
                // delete overriden property
                currPropertiesMap.remove(property.getName());
            }
        }
        // add rest of properties
        if (!currPropertiesMap.isEmpty()) {
            for (PropertyDefinition value : currPropertiesMap.values()) {
                instPropList.add(new ComponentInstanceProperty(value));
            }
        }
        instProperties.put(currentCompInstance.getUniqueId(), instPropList);
        return componentsUtils.getResponseFormat(ActionStatus.OK);
    }

    // US740820 Relate RIs according to capability name
    private CapabilityDefinition findAvailableCapabilityByTypeOrName(RequirementDefinition validReq, ComponentInstance currentCapCompInstance,
                                                                     UploadReqInfo uploadReqInfo) {
        if (null == uploadReqInfo.getCapabilityName() || validReq.getCapability()
            .equals(uploadReqInfo.getCapabilityName())) {// get

            // by

            // capability

            // type
            return findAvailableCapability(validReq, currentCapCompInstance);
        }
        return findAvailableCapability(validReq, currentCapCompInstance, uploadReqInfo);
    }

    private CapabilityDefinition findAvailableCapability(RequirementDefinition validReq, ComponentInstance currentCapCompInstance,
                                                         UploadReqInfo uploadReqInfo) {
        CapabilityDefinition cap = null;
        Map<String, List<CapabilityDefinition>> capMap = currentCapCompInstance.getCapabilities();
        if (!capMap.containsKey(validReq.getCapability())) {
            return null;
        }
        Optional<CapabilityDefinition> capByName = capMap.get(validReq.getCapability()).stream()
            .filter(p -> p.getName().equals(uploadReqInfo.getCapabilityName())).findAny();
        if (capByName.isEmpty()) {
            return null;
        }
        cap = capByName.get();
        if (isBoundedByOccurrences(cap)) {
            String leftOccurrences = cap.getLeftOccurrences();
            int left = Integer.parseInt(leftOccurrences);
            if (left > 0) {
                --left;
                cap.setLeftOccurrences(String.valueOf(left));
            }
        }
        return cap;
    }

    private CapabilityDefinition findAvailableCapability(RequirementDefinition validReq, ComponentInstance instance) {
        Map<String, List<CapabilityDefinition>> capMap = instance.getCapabilities();
        if (capMap.containsKey(validReq.getCapability())) {
            List<CapabilityDefinition> capList = capMap.get(validReq.getCapability());
            for (CapabilityDefinition cap : capList) {
                if (isBoundedByOccurrences(cap)) {
                    String leftOccurrences = cap.getLeftOccurrences() != null ? cap.getLeftOccurrences() : cap.getMaxOccurrences();
                    int left = Integer.parseInt(leftOccurrences);
                    if (left > 0) {
                        --left;
                        cap.setLeftOccurrences(String.valueOf(left));
                        return cap;
                    }
                } else {
                    return cap;
                }
            }
        }
        return null;
    }

    private boolean isBoundedByOccurrences(CapabilityDefinition cap) {
        return cap.getMaxOccurrences() != null && !cap.getMaxOccurrences().equals(CapabilityDataDefinition.MAX_OCCURRENCES);
    }

    private Either<RequirementDefinition, ResponseFormat> findAviableRequiremen(String regName, String yamlName,
                                                                                UploadComponentInstanceInfo uploadComponentInstanceInfo,
                                                                                ComponentInstance currentCompInstance, String capName) {
        Map<String, List<RequirementDefinition>> comInstRegDefMap = currentCompInstance.getRequirements();
        List<RequirementDefinition> list = comInstRegDefMap.get(capName);
        RequirementDefinition validRegDef = null;
        if (list == null) {
            for (Entry<String, List<RequirementDefinition>> entry : comInstRegDefMap.entrySet()) {
                for (RequirementDefinition reqDef : entry.getValue()) {
                    if (reqDef.getName().equals(regName)) {
                        if (reqDef.getMaxOccurrences() != null && !reqDef.getMaxOccurrences().equals(RequirementDataDefinition.MAX_OCCURRENCES)) {
                            String leftOccurrences = reqDef.getLeftOccurrences();
                            if (leftOccurrences == null) {
                                leftOccurrences = reqDef.getMaxOccurrences();
                            }
                            int left = Integer.parseInt(leftOccurrences);
                            if (left > 0) {
                                --left;
                                reqDef.setLeftOccurrences(String.valueOf(left));
                                validRegDef = reqDef;
                                break;
                            } else {
                                continue;
                            }
                        } else {
                            validRegDef = reqDef;
                            break;
                        }
                    }
                }
                if (validRegDef != null) {
                    break;
                }
            }
        } else {
            for (RequirementDefinition reqDef : list) {
                if (reqDef.getName().equals(regName)) {
                    if (reqDef.getMaxOccurrences() != null && !reqDef.getMaxOccurrences().equals(RequirementDataDefinition.MAX_OCCURRENCES)) {
                        String leftOccurrences = reqDef.getLeftOccurrences();
                        if (leftOccurrences == null) {
                            leftOccurrences = reqDef.getMaxOccurrences();
                        }
                        int left = Integer.parseInt(leftOccurrences);
                        if (left > 0) {
                            --left;
                            reqDef.setLeftOccurrences(String.valueOf(left));
                            validRegDef = reqDef;
                            break;
                        } else {
                            continue;
                        }
                    } else {
                        validRegDef = reqDef;
                        break;
                    }
                }
            }
        }
        if (validRegDef == null) {
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(ActionStatus.INVALID_NODE_TEMPLATE, yamlName, uploadComponentInstanceInfo.getName(),
                    uploadComponentInstanceInfo.getType());
            return Either.right(responseFormat);
        }
        return Either.left(validRegDef);
    }

    private Resource createResourceInstances(String yamlName, Resource resource, Resource oldResource,
                                             Map<String, UploadComponentInstanceInfo> uploadResInstancesMap, Map<String, Resource> nodeNamespaceMap,
                                             Map<String, Resource> existingNodeTypesByResourceNames) {
        Either<Resource, ResponseFormat> eitherResource;
        log.debug("createResourceInstances is {} - going to create resource instanse from CSAR", yamlName);
        if (isEmpty(uploadResInstancesMap) && resource.getResourceType() != ResourceTypeEnum.PNF) { // PNF can have no resource instances
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE);
            throw new ByResponseFormatComponentException(responseFormat);
        }
        if (MapUtils.isNotEmpty(nodeNamespaceMap)) {
            nodeNamespaceMap.forEach((k, v) -> existingNodeTypesByResourceNames.put(v.getToscaResourceName(), v));
        }
        Map<ComponentInstance, Resource> resourcesInstancesMap = new HashMap<>();
        uploadResInstancesMap.values().forEach(
            i -> createAndAddResourceInstance(i, yamlName, resource, nodeNamespaceMap, existingNodeTypesByResourceNames, resourcesInstancesMap));
        if (oldResource != null && oldResource.getResourceType() != ResourceTypeEnum.CVFC && oldResource.getComponentInstances() != null) {
            Map<String, Resource> existingNodeTypesByUids = existingNodeTypesByResourceNames.values().stream()
                .collect(toMap(Resource::getUniqueId, r -> r));
            oldResource.getComponentInstances().stream().filter(i -> !i.isCreatedFromCsar())
                .forEach(uiInst -> resourcesInstancesMap.put(uiInst, getOriginResource(existingNodeTypesByUids, uiInst)));
        }
        if (isNotEmpty(resourcesInstancesMap)) {
            try {
                toscaOperationFacade.associateComponentInstancesToComponent(resource, resourcesInstancesMap, false, oldResource != null);
            } catch (StorageException exp) {
                if (exp.getStorageOperationStatus() != null && exp.getStorageOperationStatus() != StorageOperationStatus.OK) {
                    log.debug("Failed to add component instances to container component {}", resource.getName());
                    ResponseFormat responseFormat = componentsUtils
                        .getResponseFormat(componentsUtils.convertFromStorageResponse(exp.getStorageOperationStatus()));
                    eitherResource = Either.right(responseFormat);
                    throw new ByResponseFormatComponentException(eitherResource.right().value());
                }
            }
        }
        if (CollectionUtils.isEmpty(resource.getComponentInstances()) &&
            resource.getResourceType() != ResourceTypeEnum.PNF) { // PNF can have no resource instances
            log.debug("Error when create resource instance from csar. ComponentInstances list empty");
            BeEcompErrorManager.getInstance().logBeDaoSystemError("Error when create resource instance from csar. ComponentInstances list empty");
            throw new ByResponseFormatComponentException(componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE));
        }
        return resource;
    }

    private void createAndAddResourceInstance(UploadComponentInstanceInfo uploadComponentInstanceInfo, String yamlName, Resource resource,
                                              Map<String, Resource> nodeNamespaceMap, Map<String, Resource> existingnodeTypeMap,
                                              Map<ComponentInstance, Resource> resourcesInstancesMap) {
        Either<Resource, ResponseFormat> eitherResource;
        log.debug("*************Going to create  resource instances {}", yamlName);
        // updating type if the type is node type name - we need to take the

        // updated name
        log.debug("*************Going to create  resource instances {}", uploadComponentInstanceInfo.getName());
        if (nodeNamespaceMap.containsKey(uploadComponentInstanceInfo.getType())) {
            uploadComponentInstanceInfo.setType(nodeNamespaceMap.get(uploadComponentInstanceInfo.getType()).getToscaResourceName());
        }
        Resource refResource = validateResourceInstanceBeforeCreate(yamlName, uploadComponentInstanceInfo, existingnodeTypeMap, resource);
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setComponentUid(refResource.getUniqueId());
        Collection<String> directives = uploadComponentInstanceInfo.getDirectives();
        if (directives != null && !directives.isEmpty()) {
            componentInstance.setDirectives(new ArrayList<>(directives));
        }
        UploadNodeFilterInfo uploadNodeFilterInfo = uploadComponentInstanceInfo.getUploadNodeFilterInfo();
        if (uploadNodeFilterInfo != null) {
            componentInstance
                .setNodeFilter(new CINodeFilterUtils().getNodeFilterDataDefinition(uploadNodeFilterInfo, componentInstance.getUniqueId()));
        }
        ComponentTypeEnum containerComponentType = resource.getComponentType();
        NodeTypeEnum containerNodeType = containerComponentType.getNodeType();
        if (containerNodeType == NodeTypeEnum.Resource && isNotEmpty(uploadComponentInstanceInfo.getCapabilities()) && isNotEmpty(
            refResource.getCapabilities())) {
            setCapabilityNamesTypes(refResource.getCapabilities(), uploadComponentInstanceInfo.getCapabilities());
            Map<String, List<CapabilityDefinition>> validComponentInstanceCapabilities = getValidComponentInstanceCapabilities(
                refResource.getUniqueId(), refResource.getCapabilities(), uploadComponentInstanceInfo.getCapabilities());
            componentInstance.setCapabilities(validComponentInstanceCapabilities);
        }
        if (isNotEmpty(uploadComponentInstanceInfo.getArtifacts())) {
            Map<String, Map<String, UploadArtifactInfo>> artifacts = uploadComponentInstanceInfo.getArtifacts();
            Map<String, ToscaArtifactDataDefinition> toscaArtifacts = new HashMap<>();
            Map<String, Map<String, UploadArtifactInfo>> arts = artifacts.entrySet().stream()
                .filter(e -> e.getKey().contains(TypeUtils.ToscaTagNamesEnum.ARTIFACTS.getElementName()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
            Map<String, UploadArtifactInfo> artifact = arts.get(TypeUtils.ToscaTagNamesEnum.ARTIFACTS.getElementName());
            for (Map.Entry<String, UploadArtifactInfo> entry : artifact.entrySet()) {
                ToscaArtifactDataDefinition to = new ToscaArtifactDataDefinition();
                to.setFile(entry.getValue().getFile());
                to.setType(entry.getValue().getType());
                if (isNotEmpty(entry.getValue().getProperties())) {
                    Map<String, Object> newPropertiesMap = new HashMap<>();
                    List<UploadPropInfo> artifactPropsInfo = entry.getValue().getProperties();
                    for (UploadPropInfo propInfo : artifactPropsInfo) {
                        newPropertiesMap.put(propInfo.getName(), propInfo.getValue());
                    }
                    to.setProperties(newPropertiesMap);
                }
                toscaArtifacts.put(entry.getKey(), to);
            }
            componentInstance.setToscaArtifacts(toscaArtifacts);
        }
        if (!existingnodeTypeMap.containsKey(uploadComponentInstanceInfo.getType())) {
            log.debug("createResourceInstances - not found lates version for resource instance with name {} and type {}",
                uploadComponentInstanceInfo.getName(), uploadComponentInstanceInfo.getType());
            throw new ByActionStatusComponentException(ActionStatus.INVALID_NODE_TEMPLATE, yamlName, uploadComponentInstanceInfo.getName(),
                uploadComponentInstanceInfo.getType());
        }
        Resource origResource = existingnodeTypeMap.get(uploadComponentInstanceInfo.getType());
        componentInstance.setName(uploadComponentInstanceInfo.getName());
        componentInstance.setIcon(origResource.getIcon());
        componentInstance.setCreatedFrom(CreatedFrom.CSAR);
        resourcesInstancesMap.put(componentInstance, origResource);
    }

    private void setCapabilityNamesTypes(Map<String, List<CapabilityDefinition>> originCapabilities,
                                         Map<String, List<UploadCapInfo>> uploadedCapabilities) {
        for (Entry<String, List<UploadCapInfo>> currEntry : uploadedCapabilities.entrySet()) {
            if (originCapabilities.containsKey(currEntry.getKey())) {
                currEntry.getValue().forEach(cap -> cap.setType(currEntry.getKey()));
            }
        }
        for (Map.Entry<String, List<CapabilityDefinition>> capabilities : originCapabilities.entrySet()) {
            capabilities.getValue().forEach(cap -> {
                if (uploadedCapabilities.containsKey(cap.getName())) {
                    uploadedCapabilities.get(cap.getName()).forEach(c -> {
                        c.setName(cap.getName());
                        c.setType(cap.getType());
                    });
                }
            });
        }
    }

    private Resource validateResourceInstanceBeforeCreate(String yamlName, UploadComponentInstanceInfo uploadComponentInstanceInfo,
                                                          Map<String, Resource> nodeNamespaceMap, Resource resource) {
        log.debug("validateResourceInstanceBeforeCreate - going to validate resource instance with name {} and type {} before create",
            uploadComponentInstanceInfo.getName(), uploadComponentInstanceInfo.getType());
        Resource refResource;
        if (nodeNamespaceMap.containsKey(uploadComponentInstanceInfo.getType())) {
            refResource = nodeNamespaceMap.get(uploadComponentInstanceInfo.getType());
        } else {
            Either<Resource, StorageOperationStatus> findResourceEither = StringUtils.isEmpty(resource.getModel()) ?
                toscaOperationFacade.getByToscaResourceNameMatchingVendorRelease(uploadComponentInstanceInfo.getType(),
                    ((ResourceMetadataDataDefinition) resource.getComponentMetadataDefinition().getMetadataDataDefinition()).getVendorRelease()) :
                toscaOperationFacade.getLatestByToscaResourceNameAndModel(uploadComponentInstanceInfo.getType(), resource.getModel());
            if (findResourceEither.isRight()) {
                log.debug("validateResourceInstanceBeforeCreate - not found latest version for resource instance with name {} and type {}",
                    uploadComponentInstanceInfo.getName(), uploadComponentInstanceInfo.getType());
                throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(findResourceEither.right().value()));
            }
            refResource = findResourceEither.left().value();
            nodeNamespaceMap.put(refResource.getToscaResourceName(), refResource);
        }
        String componentState = refResource.getComponentMetadataDefinition().getMetadataDataDefinition().getState();
        if (componentState.equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
            log.debug(
                "validateResourceInstanceBeforeCreate - component instance of component {} can not be created because the component is in an illegal state {}.",
                refResource.getName(), componentState);
            throw new ByActionStatusComponentException(ActionStatus.ILLEGAL_COMPONENT_STATE, refResource.getComponentType().getValue(),
                refResource.getName(), componentState);
        }
        if (!ModelConverter.isAtomicComponent(refResource) && refResource.getResourceType() != ResourceTypeEnum.CVFC) {
            log.debug("validateResourceInstanceBeforeCreate -  ref resource type is {} ", refResource.getResourceType());
            throw new ByActionStatusComponentException(ActionStatus.INVALID_NODE_TEMPLATE, yamlName, uploadComponentInstanceInfo.getName(),
                uploadComponentInstanceInfo.getType());
        }
        return refResource;
    }

    public Resource propagateStateToCertified(User user, Resource resource, LifecycleChangeInfoWithAction lifecycleChangeInfo, boolean inTransaction,
                                              boolean needLock, boolean forceCertificationAllowed) {
        boolean failed = false;
        try {
            if (resource.getLifecycleState() != LifecycleStateEnum.CERTIFIED && forceCertificationAllowed && lifecycleBusinessLogic
                .isFirstCertification(resource.getVersion())) {
                nodeForceCertification(resource, user, lifecycleChangeInfo, inTransaction, needLock);
            }
            if (resource.getLifecycleState() == LifecycleStateEnum.CERTIFIED) {
                Either<ArtifactDefinition, Operation> eitherPopulated = populateToscaArtifacts(resource, user, false, inTransaction, needLock, false);
                return resource;
            }
            return nodeFullCertification(resource.getUniqueId(), user, lifecycleChangeInfo, inTransaction, needLock);
        } catch (ComponentException e) {
            failed = true;
            log.debug("The exception has occurred upon certification of resource {}. ", resource.getName(), e);
            throw e;
        } finally {
            if (failed) {
                BeEcompErrorManager.getInstance().logBeSystemError("Change LifecycleState - Certify");
                if (!inTransaction) {
                    janusGraphDao.rollback();
                }
            } else if (!inTransaction) {
                janusGraphDao.commit();
            }
        }
    }

    private Resource nodeFullCertification(String uniqueId, User user, LifecycleChangeInfoWithAction lifecycleChangeInfo, boolean inTransaction,
                                           boolean needLock) {
        Either<Resource, ResponseFormat> resourceResponse = lifecycleBusinessLogic
            .changeState(uniqueId, user, LifeCycleTransitionEnum.CERTIFY, lifecycleChangeInfo, inTransaction, needLock);
        if (resourceResponse.isRight()) {
            throw new ByResponseFormatComponentException(resourceResponse.right().value());
        }
        return resourceResponse.left().value();
    }

    private Resource nodeForceCertification(Resource resource, User user, LifecycleChangeInfoWithAction lifecycleChangeInfo, boolean inTransaction,
                                            boolean needLock) {
        return lifecycleBusinessLogic.forceResourceCertification(resource, user, lifecycleChangeInfo, inTransaction, needLock);
    }

    public ImmutablePair<Resource, ActionStatus> createOrUpdateResourceByImport(final Resource resource, final User user, final boolean isNormative,
                                                                                final boolean isInTransaction, final boolean needLock,
                                                                                final CsarInfo csarInfo, final String nodeName,
                                                                                final boolean isNested) {
        ImmutablePair<Resource, ActionStatus> result = null;
        // check if resource already exists (search by tosca name = type)
        final boolean isNestedResource = isNestedResourceUpdate(csarInfo, nodeName);
        final String resourceName = resource.getToscaResourceName();
        final Either<Resource, StorageOperationStatus> latestByToscaName = toscaOperationFacade
            .getLatestByToscaResourceNameAndModel(resourceName, resource.getModel());
        if (latestByToscaName.isLeft() && Objects.nonNull(latestByToscaName.left().value())) {
            final Resource foundResource = latestByToscaName.left().value();
            // we don't allow updating names of top level types
            if (!isNestedResource && !StringUtils.equals(resource.getName(), foundResource.getName())) {
                BeEcompErrorManager.getInstance()
                    .logBeComponentMissingError("Create / Update resource by import", ComponentTypeEnum.RESOURCE.getValue(), resource.getName());
                log.debug("resource already exist new name={} old name={} same type={}", resource.getName(), foundResource.getName(),
                    resource.getToscaResourceName());
                final ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESOURCE_ALREADY_EXISTS);
                componentsUtils.auditResource(responseFormat, user, resource, AuditingActionEnum.IMPORT_RESOURCE);
                throwComponentException(responseFormat);
            }
            result = updateExistingResourceByImport(resource, foundResource, user, isNormative, needLock, isNested);
        } else if (isNotFound(latestByToscaName)) {
            if (isNestedResource) {
                result = createOrUpdateNestedResource(resource, user, isNormative, isInTransaction, needLock, csarInfo, isNested, nodeName);
            } else {
                result = createResourceByImport(resource, user, isNormative, isInTransaction, csarInfo);
            }
        } else {
            StorageOperationStatus status = latestByToscaName.right().value();
            log.debug("failed to get latest version of resource {}. status={}", resource.getName(), status);
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(latestByToscaName.right().value()), resource);
            componentsUtils.auditResource(responseFormat, user, resource, AuditingActionEnum.IMPORT_RESOURCE);
            throwComponentException(responseFormat);
        }
        return result;
    }

    private boolean isNestedResourceUpdate(CsarInfo csarInfo, String nodeName) {
        return csarInfo != null && csarInfo.isUpdate() && nodeName != null;
    }

    private ImmutablePair<Resource, ActionStatus> createOrUpdateNestedResource(final Resource resource, final User user, final boolean isNormative,
                                                                               final boolean isInTransaction, final boolean needLock,
                                                                               final CsarInfo csarInfo, final boolean isNested,
                                                                               final String nodeName) {
        final Either<Component, StorageOperationStatus> latestByToscaName = toscaOperationFacade.getLatestByToscaResourceName(
            buildNestedToscaResourceName(resource.getResourceType().name(), csarInfo.getVfResourceName(), nodeName).getRight(), resource.getModel());
        if (latestByToscaName.isLeft()) {
            final Resource nestedResource = (Resource) latestByToscaName.left().value();
            log.debug(VALIDATE_DERIVED_BEFORE_UPDATE);
            final Either<Boolean, ResponseFormat> eitherValidation = validateNestedDerivedFromDuringUpdate(nestedResource, resource,
                ValidationUtils.hasBeenCertified(nestedResource.getVersion()));
            if (eitherValidation.isRight()) {
                return createResourceByImport(resource, user, isNormative, isInTransaction, csarInfo);
            }
            return updateExistingResourceByImport(resource, nestedResource, user, isNormative, needLock, isNested);
        } else {
            return createResourceByImport(resource, user, isNormative, isInTransaction, csarInfo);
        }
    }

    private boolean isNotFound(Either<Resource, StorageOperationStatus> getResourceEither) {
        return getResourceEither.isRight() && getResourceEither.right().value() == StorageOperationStatus.NOT_FOUND;
    }

    private ImmutablePair<Resource, ActionStatus> createResourceByImport(Resource resource, User user, boolean isNormative, boolean isInTransaction,
                                                                         CsarInfo csarInfo) {
        log.debug("resource with name {} does not exist. create new resource", resource.getName());
        validateResourceBeforeCreate(resource, user, AuditingActionEnum.IMPORT_RESOURCE, isInTransaction, csarInfo);
        final Resource createResourceByDao = createResourceByDao(resource, user, AuditingActionEnum.IMPORT_RESOURCE, isNormative, isInTransaction);
        Resource createdResource = updateCatalog(createResourceByDao, ChangeTypeEnum.LIFECYCLE).left().map(r -> (Resource) r).left().value();
        ImmutablePair<Resource, ActionStatus> resourcePair = new ImmutablePair<>(createdResource, ActionStatus.CREATED);
        ASDCKpiApi.countImportResourcesKPI();
        return resourcePair;
    }

    public boolean isResourceExist(String resourceName) {
        Either<Resource, StorageOperationStatus> latestByName = toscaOperationFacade.getLatestByName(resourceName, null);
        return latestByName.isLeft();
    }

    private ImmutablePair<Resource, ActionStatus> updateExistingResourceByImport(Resource newResource, Resource oldResource, User user,
                                                                                 boolean inTransaction, boolean needLock, boolean isNested) {
        String lockedResourceId = oldResource.getUniqueId();
        log.debug("found resource: name={}, id={}, version={}, state={}", oldResource.getName(), lockedResourceId, oldResource.getVersion(),
            oldResource.getLifecycleState());
        ImmutablePair<Resource, ActionStatus> resourcePair = null;
        try {
            lockComponent(lockedResourceId, oldResource, needLock, "Update Resource by Import");
            oldResource = prepareResourceForUpdate(oldResource, newResource, user, inTransaction, false);
            mergeOldResourceMetadataWithNew(oldResource, newResource);
            validateResourceFieldsBeforeUpdate(oldResource, newResource, inTransaction, isNested);
            validateCapabilityTypesCreate(user, getCapabilityTypeOperation(), newResource, AuditingActionEnum.IMPORT_RESOURCE, inTransaction);
            // contact info normalization
            newResource.setContactId(newResource.getContactId().toLowerCase());
            PropertyConstraintsUtils.validatePropertiesConstraints(newResource, oldResource);
            // non-updatable fields
            newResource.setCreatorUserId(user.getUserId());
            newResource.setCreatorFullName(user.getFullName());
            newResource.setLastUpdaterUserId(user.getUserId());
            newResource.setLastUpdaterFullName(user.getFullName());
            newResource.setUniqueId(oldResource.getUniqueId());
            newResource.setVersion(oldResource.getVersion());
            newResource.setInvariantUUID(oldResource.getInvariantUUID());
            newResource.setLifecycleState(oldResource.getLifecycleState());
            newResource.setUUID(oldResource.getUUID());
            newResource.setNormalizedName(oldResource.getNormalizedName());
            newResource.setSystemName(oldResource.getSystemName());
            newResource.setModel(oldResource.getModel());
            if (oldResource.getCsarUUID() != null) {
                newResource.setCsarUUID(oldResource.getCsarUUID());
            }
            if (oldResource.getImportedToscaChecksum() != null) {
                newResource.setImportedToscaChecksum(oldResource.getImportedToscaChecksum());
            }
            newResource.setAbstract(oldResource.isAbstract());
            if (CollectionUtils.isEmpty(newResource.getDerivedFrom())) {
                newResource.setDerivedFrom(oldResource.getDerivedFrom());
            }
            if (CollectionUtils.isEmpty(newResource.getDataTypes())) {
                newResource.setDataTypes(oldResource.getDataTypes());
            }
            if (StringUtils.isEmpty(newResource.getDerivedFromGenericType())) {
                newResource.setDerivedFromGenericType(oldResource.getDerivedFromGenericType());
            }
            if (StringUtils.isEmpty(newResource.getDerivedFromGenericVersion())) {
                newResource.setDerivedFromGenericVersion(oldResource.getDerivedFromGenericVersion());
            }
            // add for new)

            // created without tosca artifacts - add the placeholders
            if (MapUtils.isEmpty(newResource.getToscaArtifacts())) {
                setToscaArtifactsPlaceHolders(newResource, user);
            }
            if (MapUtils.isEmpty(newResource.getInterfaces())) {
                newResource.setInterfaces(oldResource.getInterfaces());
            }
            if (CollectionUtils.isEmpty(newResource.getAttributes())) {
                newResource.setAttributes(oldResource.getAttributes());
            }
            if (CollectionUtils.isEmpty(newResource.getProperties())) {
                newResource.setProperties(oldResource.getProperties());
            }
            Either<Resource, StorageOperationStatus> overrideResource = toscaOperationFacade.overrideComponent(newResource, oldResource);
            if (overrideResource.isRight()) {
                ResponseFormat responseFormat = componentsUtils
                    .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(overrideResource.right().value()), newResource);
                componentsUtils.auditResource(responseFormat, user, newResource, AuditingActionEnum.IMPORT_RESOURCE);
                throwComponentException(responseFormat);
            }
            updateCatalog(overrideResource.left().value(), ChangeTypeEnum.LIFECYCLE);
            log.debug("Resource updated successfully!!!");
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
            componentsUtils.auditResource(responseFormat, user, newResource, AuditingActionEnum.IMPORT_RESOURCE,
                ResourceVersionInfo.newBuilder().state(oldResource.getLifecycleState().name()).version(oldResource.getVersion()).build());
            resourcePair = new ImmutablePair<>(overrideResource.left().value(), ActionStatus.OK);
            return resourcePair;
        } finally {
            if (resourcePair == null) {
                BeEcompErrorManager.getInstance().logBeSystemError("Change LifecycleState - Certify");
                janusGraphDao.rollback();
            } else if (!inTransaction) {
                janusGraphDao.commit();
            }
            if (needLock) {
                log.debug("unlock resource {}", lockedResourceId);
                graphLockOperation.unlockComponent(lockedResourceId, NodeTypeEnum.Resource);
            }
        }
    }

    /**
     * Merge old resource with new. Keep old category and vendor name without change
     *
     * @param oldResource
     * @param newResource
     */
    private void mergeOldResourceMetadataWithNew(Resource oldResource, Resource newResource) {
        // keep old category and vendor name without change

        // merge the rest of the resource metadata
        if (newResource.getTags() == null || newResource.getTags().isEmpty()) {
            newResource.setTags(oldResource.getTags());
        }
        if (newResource.getDescription() == null) {
            newResource.setDescription(oldResource.getDescription());
        }
        if (newResource.getVendorRelease() == null) {
            newResource.setVendorRelease(oldResource.getVendorRelease());
        }
        if (newResource.getResourceVendorModelNumber() == null) {
            newResource.setResourceVendorModelNumber(oldResource.getResourceVendorModelNumber());
        }
        if (newResource.getModel() == null) {
            newResource.setModel(oldResource.getModel());
        }
        if (newResource.getContactId() == null) {
            newResource.setContactId(oldResource.getContactId());
        }
        newResource.setCategories(oldResource.getCategories());
        if (newResource.getVendorName() == null) {
            newResource.setVendorName(oldResource.getVendorName());
        }
        List<GroupDefinition> oldForUpdate = oldResource.getGroups();
        if (CollectionUtils.isNotEmpty(oldForUpdate)) {
            List<GroupDefinition> groupForUpdate = oldForUpdate.stream().map(GroupDefinition::new).collect(Collectors.toList());
            groupForUpdate.stream().filter(GroupDataDefinition::isVspOriginated).forEach(group -> group.setName(group.getInvariantName()));
            newResource.setGroups(groupForUpdate);
        }
        if (newResource.getResourceType().isAtomicType() && !newResource.getName().equals("Root")
            && newResource.getResourceType() != ResourceTypeEnum.CVFC) {
            ResourceTypeEnum updatedResourceType = newResource.getResourceType();
            Optional<Component> derivedFromResourceOptional = getParentComponent(newResource);
            if (derivedFromResourceOptional.isPresent() && derivedFromResourceOptional.get().getComponentType() == ComponentTypeEnum.RESOURCE) {
                Resource parentResource = (Resource) derivedFromResourceOptional.get();
                if (!(parentResource.isAbstract() && (ResourceTypeEnum.VFC == parentResource.getResourceType()
                    || ResourceTypeEnum.ABSTRACT == parentResource.getResourceType())) && parentResource.getResourceType() != updatedResourceType
                    && oldResource.getResourceType() != updatedResourceType) {
                    BeEcompErrorManager.getInstance().logInternalDataError("mergeOldResourceMetadataWithNew",
                        "resource type of the resource does not match to derived from resource type", ErrorSeverity.ERROR);
                    log.debug(
                        "#mergeOldResourceMetadataWithNew - resource type {} of the resource {} does not match to derived from resource type {}",
                        newResource.getResourceType(), newResource.getToscaResourceName(), parentResource.getResourceType());
                    throw new ByActionStatusComponentException(ActionStatus.INVALID_RESOURCE_TYPE);
                }
            }
        }
    }

    private Optional<Component> getParentComponent(Resource newResource) {
        if (newResource.getDerivedFrom() == null) {
            return Optional.empty();
        }
        String toscaResourceNameDerivedFrom = newResource.getDerivedFrom().get(0);
        Either<Component, StorageOperationStatus> latestByToscaResourceName = toscaOperationFacade
            .getLatestByToscaResourceName(toscaResourceNameDerivedFrom, newResource.getModel());
        if (latestByToscaResourceName.isRight()) {
            BeEcompErrorManager.getInstance()
                .logInternalDataError("mergeOldResourceMetadataWithNew", "derived from resource not found", ErrorSeverity.ERROR);
            log.debug("#mergeOldResourceMetadataWithNew - derived from resource {} not found", toscaResourceNameDerivedFrom);
            throw new ByActionStatusComponentException(ActionStatus.RESOURCE_NOT_FOUND, toscaResourceNameDerivedFrom);
        }
        return Optional.of(latestByToscaResourceName.left().value());
    }

    private Resource prepareResourceForUpdate(Resource oldResource, Resource newResource, User user, boolean inTransaction, boolean needLock) {
        if (!ComponentValidationUtils.canWorkOnResource(oldResource, user.getUserId())) {
            // checkout
            return lifecycleBusinessLogic
                .changeState(oldResource.getUniqueId(), user, LifeCycleTransitionEnum.CHECKOUT, new LifecycleChangeInfoWithAction("update by import"),
                    inTransaction, needLock).left().on(response -> failOnChangeState(response, user, oldResource, newResource));
        }
        return oldResource;
    }

    private Resource failOnChangeState(ResponseFormat response, User user, Resource oldResource, Resource newResource) {
        log.info("resource {} cannot be updated. reason={}", oldResource.getUniqueId(), response.getFormattedMessage());
        componentsUtils.auditResource(response, user, newResource, AuditingActionEnum.IMPORT_RESOURCE,
            ResourceVersionInfo.newBuilder().state(oldResource.getLifecycleState().name()).version(oldResource.getVersion()).build());
        throw new ByResponseFormatComponentException(response);
    }

    public Resource validateResourceBeforeCreate(Resource resource, User user, AuditingActionEnum actionEnum, boolean inTransaction,
                                                 CsarInfo csarInfo) {
        validateResourceFieldsBeforeCreate(user, resource, actionEnum, inTransaction);
        validateCapabilityTypesCreate(user, getCapabilityTypeOperation(), resource, actionEnum, inTransaction);
        validateLifecycleTypesCreate(user, resource, actionEnum);
        validateResourceType(user, resource, actionEnum);
        resource.setCreatorUserId(user.getUserId());
        resource.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
        resource.setContactId(resource.getContactId().toLowerCase());
        if (StringUtils.isEmpty(resource.getToscaResourceName()) && !ModelConverter.isAtomicComponent(resource)) {
            String resourceSystemName;
            if (csarInfo != null && StringUtils.isNotEmpty(csarInfo.getVfResourceName())) {
                resourceSystemName = ValidationUtils.convertToSystemName(csarInfo.getVfResourceName());
            } else {
                resourceSystemName = resource.getSystemName();
            }
            resource
                .setToscaResourceName(CommonBeUtils.generateToscaResourceName(resource.getResourceType().name().toLowerCase(), resourceSystemName));
        }
        // Generate invariant UUID - must be here and not in operation since it

        // should stay constant during clone

        // TODO
        String invariantUUID = UniqueIdBuilder.buildInvariantUUID();
        resource.setInvariantUUID(invariantUUID);
        return resource;
    }

    private Either<Boolean, ResponseFormat> validateResourceType(User user, Resource resource, AuditingActionEnum actionEnum) {
        Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
        if (resource.getResourceType() == null) {
            log.debug("Invalid resource type for resource");
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
            eitherResult = Either.right(errorResponse);
            componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
        }
        return eitherResult;
    }

    private Either<Boolean, ResponseFormat> validateLifecycleTypesCreate(User user, Resource resource, AuditingActionEnum actionEnum) {
        Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
        if (resource.getInterfaces() != null && resource.getInterfaces().size() > 0) {
            log.debug("validate interface lifecycle Types Exist");
            Iterator<InterfaceDefinition> intItr = resource.getInterfaces().values().iterator();
            while (intItr.hasNext() && eitherResult.isLeft()) {
                InterfaceDefinition interfaceDefinition = intItr.next();
                String intType = interfaceDefinition.getUniqueId();
                Either<InterfaceDefinition, StorageOperationStatus> eitherCapTypeFound = interfaceTypeOperation.getInterface(intType);
                if (eitherCapTypeFound.isRight()) {
                    if (eitherCapTypeFound.right().value() == StorageOperationStatus.NOT_FOUND) {
                        BeEcompErrorManager.getInstance()
                            .logBeGraphObjectMissingError("Create Resource - validateLifecycleTypesCreate", "Interface", intType);
                        log.debug("Lifecycle Type: {} is required by resource: {} but does not exist in the DB", intType, resource.getName());
                        BeEcompErrorManager.getInstance().logBeDaoSystemError("Create Resource - validateLifecycleTypesCreate");
                        log.debug("request to data model failed with error: {}", eitherCapTypeFound.right().value().name());
                    }
                    ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_LIFECYCLE_TYPE, intType);
                    eitherResult = Either.right(errorResponse);
                    componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
                }
            }
        }
        return eitherResult;
    }

    private Either<Boolean, ResponseFormat> validateCapabilityTypesCreate(User user, ICapabilityTypeOperation capabilityTypeOperation,
                                                                          Resource resource, AuditingActionEnum actionEnum, boolean inTransaction) {
        Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
        if (resource.getCapabilities() != null && resource.getCapabilities().size() > 0) {
            log.debug("validate capability Types Exist - capabilities section");
            for (Entry<String, List<CapabilityDefinition>> typeEntry : resource.getCapabilities().entrySet()) {
                eitherResult = validateCapabilityTypeExists(user, capabilityTypeOperation, resource, actionEnum, eitherResult, typeEntry,
                    inTransaction);
                if (eitherResult.isRight()) {
                    return Either.right(eitherResult.right().value());
                }
            }
        }
        if (resource.getRequirements() != null && resource.getRequirements().size() > 0) {
            log.debug("validate capability Types Exist - requirements section");
            for (String type : resource.getRequirements().keySet()) {
                eitherResult = validateCapabilityTypeExists(user, capabilityTypeOperation, resource, resource.getRequirements().get(type), actionEnum,
                    eitherResult, type, inTransaction);
                if (eitherResult.isRight()) {
                    return Either.right(eitherResult.right().value());
                }
            }
        }
        return eitherResult;
    }

    // @param typeObject- the object to which the validation is done
    private Either<Boolean, ResponseFormat> validateCapabilityTypeExists(User user, ICapabilityTypeOperation capabilityTypeOperation,
                                                                         Resource resource, List<?> validationObjects, AuditingActionEnum actionEnum,
                                                                         Either<Boolean, ResponseFormat> eitherResult, String type,
                                                                         boolean inTransaction) {
        Either<CapabilityTypeDefinition, StorageOperationStatus> eitherCapTypeFound = capabilityTypeOperation.getCapabilityType(
            UniqueIdBuilder.buildCapabilityTypeUid(resource.getModel(), type), inTransaction);
        if (eitherCapTypeFound.isRight()) {
            if (eitherCapTypeFound.right().value() == StorageOperationStatus.NOT_FOUND) {
                BeEcompErrorManager.getInstance().logBeGraphObjectMissingError(CREATE_RESOURCE_VALIDATE_CAPABILITY_TYPES, "Capability Type", type);
                log.debug("Capability Type: {} is required by resource: {} but does not exist in the DB", type, resource.getName());
                BeEcompErrorManager.getInstance().logBeDaoSystemError(CREATE_RESOURCE_VALIDATE_CAPABILITY_TYPES);
            }
            log.debug("Trying to get capability type {} failed with error: {}", type, eitherCapTypeFound.right().value().name());
            ResponseFormat errorResponse = null;
            if (type != null) {
                errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_CAPABILITY_TYPE, type);
            } else {
                errorResponse = componentsUtils.getResponseFormatByElement(ActionStatus.MISSING_CAPABILITY_TYPE, validationObjects);
            }
            eitherResult = Either.right(errorResponse);
            componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
        }
        return eitherResult;
    }

    private Either<Boolean, ResponseFormat> validateCapabilityTypeExists(User user, ICapabilityTypeOperation capabilityTypeOperation,
                                                                         Resource resource, AuditingActionEnum actionEnum,
                                                                         Either<Boolean, ResponseFormat> eitherResult,
                                                                         Entry<String, List<CapabilityDefinition>> typeEntry, boolean inTransaction) {
        Either<CapabilityTypeDefinition, StorageOperationStatus> eitherCapTypeFound = capabilityTypeOperation
            .getCapabilityType(UniqueIdBuilder.buildCapabilityTypeUid(resource.getModel(), typeEntry.getKey()), inTransaction);
        if (eitherCapTypeFound.isRight()) {
            if (eitherCapTypeFound.right().value() == StorageOperationStatus.NOT_FOUND) {
                BeEcompErrorManager.getInstance()
                    .logBeGraphObjectMissingError(CREATE_RESOURCE_VALIDATE_CAPABILITY_TYPES, "Capability Type", typeEntry.getKey());
                log.debug("Capability Type: {} is required by resource: {} but does not exist in the DB", typeEntry.getKey(), resource.getName());
                BeEcompErrorManager.getInstance().logBeDaoSystemError(CREATE_RESOURCE_VALIDATE_CAPABILITY_TYPES);
            }
            log.debug("Trying to get capability type {} failed with error: {}", typeEntry.getKey(), eitherCapTypeFound.right().value().name());
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_CAPABILITY_TYPE, typeEntry.getKey());
            componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
            return Either.right(errorResponse);
        }
        CapabilityTypeDefinition capabilityTypeDefinition = eitherCapTypeFound.left().value();
        if (capabilityTypeDefinition.getProperties() != null) {
            for (CapabilityDefinition capDef : typeEntry.getValue()) {
                List<ComponentInstanceProperty> properties = capDef.getProperties();
                List<ComponentInstanceProperty> changedProperties = new ArrayList<>();
                if (properties == null || properties.isEmpty()) {
                    for (Entry<String, PropertyDefinition> prop : capabilityTypeDefinition.getProperties().entrySet()) {
                        ComponentInstanceProperty newProp = new ComponentInstanceProperty(prop.getValue());
                        changedProperties.add(newProp);
                    }
                } else {
                    List<ComponentInstanceProperty> propsToAdd = new ArrayList<>();
                    for (Entry<String, PropertyDefinition> prop : capabilityTypeDefinition.getProperties().entrySet()) {
                        PropertyDefinition propFromDef = prop.getValue();
                        boolean propFound = false;
                        for (ComponentInstanceProperty cip : properties) {
                            if (propFromDef.getName().equals(cip.getName())) {
                                //merge property value and property description only, ignore other fields
                                if (cip.getDescription() != null && !cip.getDescription().equals(propFromDef.getDescription())) {
                                    propFromDef.setDescription(cip.getDescription());
                                }
                                propertyDataValueMergeBusinessLogic.mergePropertyValue(propFromDef, cip, new ArrayList<>());
                                if (cip.getValue() != null) {
                                    propFromDef.setValue(cip.getValue());
                                }
                                propsToAdd.add(new ComponentInstanceProperty(propFromDef));
                                propFound = true;
                                properties.remove(cip);
                                break;
                            }
                        }
                        if (!propFound) {
                            propsToAdd.add(new ComponentInstanceProperty(propFromDef));
                        }
                    }
                    if (!propsToAdd.isEmpty()) {
                        changedProperties.addAll(propsToAdd);
                    }
                }
                capDef.setProperties(changedProperties);
            }
        }
        return eitherResult;
    }

    public Resource createResourceByDao(Resource resource, User user, AuditingActionEnum actionEnum, boolean isNormative, boolean inTransaction) {
        // create resource

        // lock new resource name in order to avoid creation resource with same

        // name
        Resource createdResource = null;
        if (!inTransaction) {
            Either<Boolean, ResponseFormat> lockResult = lockComponentByName(resource.getSystemName(), resource, CREATE_RESOURCE);
            if (lockResult.isRight()) {
                ResponseFormat responseFormat = lockResult.right().value();
                componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
                throw new ByResponseFormatComponentException(responseFormat);
            }
            log.debug("name is locked {} status = {}", resource.getSystemName(), lockResult);
        }
        try {
            if (resource.deriveFromGeneric()) {
                handleResourceGenericType(resource);
            }
            createdResource = createResourceTransaction(resource, user, isNormative);
            componentsUtils.auditResource(componentsUtils.getResponseFormat(ActionStatus.CREATED), user, createdResource, actionEnum);
            ASDCKpiApi.countCreatedResourcesKPI();
        } catch (ComponentException e) {
            ResponseFormat responseFormat =
                e.getResponseFormat() == null ? componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams()) : e.getResponseFormat();
            componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
            throw e;
        } catch (StorageException e) {
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(e.getStorageOperationStatus()));
            componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
            throw e;
        } finally {
            if (!inTransaction) {
                graphLockOperation.unlockComponentByName(resource.getSystemName(), resource.getUniqueId(), NodeTypeEnum.Resource);
            }
        }
        return createdResource;
    }

    private Resource createResourceTransaction(Resource resource, User user, boolean isNormative) {
        final String resourceName = resource.getName();
        final String modelName = resource.getModel();
        final ResourceTypeEnum resourceType = resource.getResourceType();
        final ComponentTypeEnum componentType = resource.getComponentType();
        final Either<Boolean, StorageOperationStatus> eitherValidation = toscaOperationFacade
            .validateComponentNameAndModelExists(resourceName, modelName, resourceType, componentType);
        if (eitherValidation.isRight()) {
            loggerSupportability.log(LoggerSupportabilityActions.VALIDATE_NAME, resource.getComponentMetadataForSupportLog(), StatusCode.ERROR,
                "ERROR while validate component name {} Status is: {}", resource.getName(), eitherValidation.right().value());
            log.debug("Failed to validate component name {}. Status is {}. ", resource.getName(), eitherValidation.right().value());
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(eitherValidation.right().value()));
        }
        if (eitherValidation.left().value()) {
            log.debug("resource with name: {}, already exists", resource.getName());
            loggerSupportability
                .log(LoggerSupportabilityActions.CREATE_RESOURCE_FROM_YAML, resource.getComponentMetadataForSupportLog(), StatusCode.ERROR,
                    "resource with name: {} already exists", resource.getName());
            throw new ByActionStatusComponentException(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, ComponentTypeEnum.RESOURCE.getValue(),
                resource.getName());
        }
        log.debug("send resource {} to dao for create", resource.getName());
        createArtifactsPlaceHolderData(resource, user);
        // enrich object
        if (!isNormative) {
            log.debug("enrich resource with creator, version and state");
            resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
            resource.setVersion(INITIAL_VERSION);
            resource.setHighestVersion(true);
            if (resource.getResourceType() != null && resource.getResourceType() != ResourceTypeEnum.CVFC) {
                resource.setAbstract(false);
            }
        }
        return toscaOperationFacade.createToscaComponent(resource).left().on(r -> throwComponentExceptionByResource(r, resource));
    }

    private Resource throwComponentExceptionByResource(StorageOperationStatus status, Resource resource) {
        ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(status), resource);
        throw new ByResponseFormatComponentException(responseFormat);
    }

    private void createArtifactsPlaceHolderData(Resource resource, User user) {
        // create mandatory artifacts

        // TODO it must be removed after that artifact uniqueId creation will be

        // moved to ArtifactOperation
        setInformationalArtifactsPlaceHolder(resource, user);
        setDeploymentArtifactsPlaceHolder(resource, user);
        setToscaArtifactsPlaceHolders(resource, user);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setDeploymentArtifactsPlaceHolder(Component component, User user) {
        Resource resource = (Resource) component;
        Map<String, ArtifactDefinition> artifactMap = resource.getDeploymentArtifacts();
        if (artifactMap == null) {
            artifactMap = new HashMap<>();
        }
        Map<String, Object> deploymentResourceArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration()
            .getDeploymentResourceArtifacts();
        if (deploymentResourceArtifacts != null) {
            Map<String, ArtifactDefinition> finalArtifactMap = artifactMap;
            deploymentResourceArtifacts.forEach((k, v) -> processDeploymentResourceArtifacts(user, resource, finalArtifactMap, k, v));
        }
        resource.setDeploymentArtifacts(artifactMap);
    }

    private void processDeploymentResourceArtifacts(User user, Resource resource, Map<String, ArtifactDefinition> artifactMap, String k, Object v) {
        Map<String, Object> artifactDetails = (Map<String, Object>) v;
        Object object = artifactDetails.get(PLACE_HOLDER_RESOURCE_TYPES);
        if (object != null) {
            List<String> artifactTypes = (List<String>) object;
            if (!artifactTypes.contains(resource.getResourceType().name())) {
                return;
            }
        } else {
            log.info("resource types for artifact placeholder {} were not defined. default is all resources", k);
        }
        if (artifactsBusinessLogic != null) {
            ArtifactDefinition artifactDefinition = artifactsBusinessLogic
                .createArtifactPlaceHolderInfo(resource.getUniqueId(), k, (Map<String, Object>) v, user, ArtifactGroupTypeEnum.DEPLOYMENT);
            if (artifactDefinition != null && !artifactMap.containsKey(artifactDefinition.getArtifactLabel())) {
                artifactMap.put(artifactDefinition.getArtifactLabel(), artifactDefinition);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setInformationalArtifactsPlaceHolder(Resource resource, User user) {
        Map<String, ArtifactDefinition> artifactMap = resource.getArtifacts();
        if (artifactMap == null) {
            artifactMap = new HashMap<>();
        }
        String resourceUniqueId = resource.getUniqueId();
        List<String> exludeResourceCategory = ConfigurationManager.getConfigurationManager().getConfiguration().getExcludeResourceCategory();
        List<String> exludeResourceType = ConfigurationManager.getConfigurationManager().getConfiguration().getExcludeResourceType();
        Map<String, Object> informationalResourceArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration()
            .getInformationalResourceArtifacts();
        List<CategoryDefinition> categories = resource.getCategories();
        boolean isCreateArtifact = true;
        if (exludeResourceCategory != null) {
            String category = categories.get(0).getName();
            isCreateArtifact = exludeResourceCategory.stream().noneMatch(e -> e.equalsIgnoreCase(category));
        }
        if (isCreateArtifact && exludeResourceType != null) {
            String resourceType = resource.getResourceType().name();
            isCreateArtifact = exludeResourceType.stream().noneMatch(e -> e.equalsIgnoreCase(resourceType));
        }
        if (informationalResourceArtifacts != null && isCreateArtifact) {
            Set<String> keys = informationalResourceArtifacts.keySet();
            for (String informationalResourceArtifactName : keys) {
                Map<String, Object> artifactInfoMap = (Map<String, Object>) informationalResourceArtifacts.get(informationalResourceArtifactName);
                ArtifactDefinition artifactDefinition = artifactsBusinessLogic
                    .createArtifactPlaceHolderInfo(resourceUniqueId, informationalResourceArtifactName, artifactInfoMap, user,
                        ArtifactGroupTypeEnum.INFORMATIONAL);
                artifactMap.put(artifactDefinition.getArtifactLabel(), artifactDefinition);
            }
        }
        resource.setArtifacts(artifactMap);
    }

    /**
     * deleteResource
     *
     * @param resourceId
     * @param user
     * @return
     */
    public ResponseFormat deleteResource(String resourceId, User user) {
        ResponseFormat responseFormat;
        validateUserExists(user);
        Either<Resource, StorageOperationStatus> resourceStatus = toscaOperationFacade.getToscaElement(resourceId);
        if (resourceStatus.isRight()) {
            log.debug("failed to get resource {}", resourceId);
            return componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(resourceStatus.right().value()), "");
        }
        Resource resource = resourceStatus.left().value();
        StorageOperationStatus result = StorageOperationStatus.OK;
        lockComponent(resourceId, resource, "Mark resource to delete");
        try {
            result = markComponentToDelete(resource);
            if (result == StorageOperationStatus.OK) {
                responseFormat = componentsUtils.getResponseFormat(ActionStatus.NO_CONTENT);
            } else {
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(result);
                responseFormat = componentsUtils.getResponseFormatByResource(actionStatus, resource.getName());
            }
            return responseFormat;
        } finally {
            if (!StorageOperationStatus.OK.equals(result)) {
                janusGraphDao.rollback();
            } else {
                janusGraphDao.commit();
            }
            graphLockOperation.unlockComponent(resourceId, NodeTypeEnum.Resource);
        }
    }

    private boolean isComponentSystemDeployed(Resource resource) {
        return resource.getComponentMetadataDefinition().getMetadataDataDefinition().isNormative();
    }

    /**
     * Deletes every version of the provided resource
     *
     * @param resourceId the resource identifier
     * @param user       the user that performs the deletion
     * @return
     * @throws ComponentException if there is any error in the deletion of the resource operation
     */
    public void deleteResourceAllVersions(String resourceId, User user) {
        validateUserExists(user);
        Either<Resource, StorageOperationStatus> resourceStatus = toscaOperationFacade.getToscaElement(resourceId);
        if (resourceStatus.isRight()) {
            log.debug("Failed to get resource {}", resourceId);
            componentException(resourceStatus.right().value());
        }
        Resource resource = resourceStatus.left().value();
        if (isComponentSystemDeployed(resource)) {
            throw new ByActionStatusComponentException(ActionStatus.CANNOT_DELETE_SYSTEM_DEPLOYED_RESOURCES, ComponentTypeEnum.RESOURCE.getValue(),
                resource.getName());
        }
        if (Boolean.FALSE.equals(resource.isArchived())) {
            log.debug("The resource, {}, requested for delete has not been archived.", resourceId);
            throw new ComponentException(ActionStatus.COMPONENT_NOT_ARCHIVED, resourceId);
        }
        try {
            String model = resource.getModel();
            final Optional<Model> modelOptional = modelOperation.findModelByName(model);
            List<String> deletedResourceList = toscaOperationFacade.deleteComponent(resource.getInvariantUUID(), NodeTypeEnum.Resource, true);
            if (log.isDebugEnabled()) {
                deletedResourceList.forEach(deletedR -> log.debug("Component {} was deleted.", deletedR));
            }
            if (modelOptional.isPresent() && modelOptional.get().getModelType() == ModelTypeEnum.NORMATIVE_EXTENSION) {
                modelOperation.deleteModel(modelOptional.get(), true);
            }
            toscaOperationFacade.commitAndCheck(resource.getUniqueId());
            updateCatalog(resource, ChangeTypeEnum.DELETE);
        } catch (ComponentException exception) {
            log.debug("Failed to delete resource, {} ", resourceId);
            janusGraphDao.rollback();
            throw exception;
        }
    }

    public ResponseFormat deleteResourceByNameAndVersion(String resourceName, String version, User user) {
        ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.NO_CONTENT);
        validateUserExists(user);
        Resource resource = null;
        StorageOperationStatus result = StorageOperationStatus.OK;
        boolean failed = false;
        try {
            Either<Resource, StorageOperationStatus> resourceStatus = toscaOperationFacade
                .getComponentByNameAndVersion(ComponentTypeEnum.RESOURCE, resourceName, version);
            if (resourceStatus.isRight()) {
                log.debug("failed to get resource {} version {}", resourceName, version);
                return componentsUtils
                    .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(resourceStatus.right().value()), resourceName);
            }
            resource = resourceStatus.left().value();
        } finally {
            janusGraphDao.commit();
        }
        if (resource != null) {
            lockComponent(resource.getUniqueId(), resource, DELETE_RESOURCE);
            try {
                result = markComponentToDelete(resource);
                if (result != StorageOperationStatus.OK) {
                    ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(result);
                    responseFormat = componentsUtils.getResponseFormatByResource(actionStatus, resource.getName());
                    return responseFormat;
                }
            } catch (ComponentException e) {
                failed = true;
                throw e;
            } finally {
                if (failed || !StorageOperationStatus.OK.equals(result)) {
                    janusGraphDao.rollback();
                } else {
                    janusGraphDao.commit();
                }
                graphLockOperation.unlockComponent(resource.getUniqueId(), NodeTypeEnum.Resource);
            }
        }
        return responseFormat;
    }

    public Either<Resource, ResponseFormat> getResource(String resourceId, User user) {
        if (user != null) {
            validateUserExists(user);
        }
        Either<Resource, StorageOperationStatus> storageStatus = toscaOperationFacade.getToscaElement(resourceId);
        if (storageStatus.isRight()) {
            log.debug("failed to get resource by id {}", resourceId);
            return Either.right(
                componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(storageStatus.right().value()), resourceId));
        }
        if (storageStatus.left().value() == null) {
            return Either.right(componentsUtils
                .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(StorageOperationStatus.NOT_FOUND), resourceId));
        }
        return Either.left(storageStatus.left().value());
    }

    public Either<Resource, ResponseFormat> getResourceByNameAndVersion(String resourceName, String resourceVersion, String userId) {
        validateUserExists(userId);
        Either<Resource, StorageOperationStatus> getResource = toscaOperationFacade
            .getComponentByNameAndVersion(ComponentTypeEnum.RESOURCE, resourceName, resourceVersion);
        if (getResource.isRight()) {
            log.debug("failed to get resource by name {} and version {}", resourceName, resourceVersion);
            return Either.right(
                componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(getResource.right().value()), resourceName));
        }
        return Either.left(getResource.left().value());
    }

    /**
     * updateResourceMetadata
     *
     * @param user               - modifier data (userId)
     * @param inTransaction      TODO
     * @param resourceIdToUpdate - the resource identifier
     * @param newResource
     * @return Either<Resource, responseFormat>
     */
    public Resource updateResourceMetadata(String resourceIdToUpdate, Resource newResource, Resource currentResource, User user,
                                           boolean inTransaction) {
        validateUserExists(user.getUserId());
        log.debug("Get resource with id {}", resourceIdToUpdate);
        boolean needToUnlock = false;
        try {
            if (currentResource == null) {
                Either<Resource, StorageOperationStatus> storageStatus = toscaOperationFacade.getToscaElement(resourceIdToUpdate);
                if (storageStatus.isRight()) {
                    throw new ByResponseFormatComponentException(
                        componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(storageStatus.right().value()), ""));
                }
                currentResource = storageStatus.left().value();
            }
            // verify that resource is checked-out and the user is the last

            // updater
            if (!ComponentValidationUtils.canWorkOnResource(currentResource, user.getUserId())) {
                throw new ByResponseFormatComponentException(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
            }
            // lock resource
            StorageOperationStatus lockResult = graphLockOperation.lockComponent(resourceIdToUpdate, NodeTypeEnum.Resource);
            if (lockResult != StorageOperationStatus.OK) {
                BeEcompErrorManager.getInstance()
                    .logBeFailedLockObjectError("Upload Artifact - lock ", NodeTypeEnum.Resource.getName(), resourceIdToUpdate);
                log.debug("Failed to lock resource: {}, error - {}", resourceIdToUpdate, lockResult);
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(lockResult));
                throw new ByResponseFormatComponentException(responseFormat);
            }
            needToUnlock = true;
            // critical section starts here

            // convert json to object

            // Update and updated resource must have a non-empty "derivedFrom"

            // list

            // This code is not called from import resources, because of root

            // VF "derivedFrom" should be null (or ignored)
            if (ModelConverter.isAtomicComponent(currentResource)) {
                validateDerivedFromNotEmpty(null, newResource, null);
                validateDerivedFromNotEmpty(null, currentResource, null);
            } else {
                newResource.setDerivedFrom(null);
            }
            Either<Resource, ResponseFormat> dataModelResponse = updateResourceMetadata(resourceIdToUpdate, newResource, user, currentResource, false,
                true);
            if (dataModelResponse.isRight()) {
                log.debug("failed to update resource metadata!!!");
                throw new ByResponseFormatComponentException(dataModelResponse.right().value());
            }
            log.debug("Resource metadata updated successfully!!!");
            return dataModelResponse.left().value();
        } catch (ComponentException | StorageException e) {
            rollback(inTransaction, newResource, null, null);
            throw e;
        } finally {
            if (!inTransaction) {
                janusGraphDao.commit();
            }
            if (needToUnlock) {
                graphLockOperation.unlockComponent(resourceIdToUpdate, NodeTypeEnum.Resource);
            }
        }
    }

    private Either<Resource, ResponseFormat> updateResourceMetadata(String resourceIdToUpdate, Resource newResource, User user,
                                                                    Resource currentResource, boolean shouldLock, boolean inTransaction) {
        updateVfModuleGroupsNames(currentResource, newResource);
        validateResourceFieldsBeforeUpdate(currentResource, newResource, inTransaction, false);
        // Setting last updater and uniqueId
        newResource.setContactId(newResource.getContactId().toLowerCase());
        newResource.setLastUpdaterUserId(user.getUserId());
        newResource.setUniqueId(resourceIdToUpdate);
        // Cannot set highest version through UI
        newResource.setHighestVersion(currentResource.isHighestVersion());
        newResource.setCreationDate(currentResource.getCreationDate());
        Either<Boolean, ResponseFormat> processUpdateOfDerivedFrom = processUpdateOfDerivedFrom(currentResource, newResource, user.getUserId(),
            inTransaction);
        if (processUpdateOfDerivedFrom.isRight()) {
            log.debug("Couldn't update derived from for resource {}", resourceIdToUpdate);
            return Either.right(processUpdateOfDerivedFrom.right().value());
        }
        log.debug("send resource {} to dao for update", newResource.getUniqueId());
        if (isNotEmpty(newResource.getGroups())) {
            for (GroupDefinition group : newResource.getGroups()) {
                if (DEFAULT_GROUP_VF_MODULE.equals(group.getType())) {
                    groupBusinessLogic
                        .validateAndUpdateGroupMetadata(newResource.getComponentMetadataDefinition().getMetadataDataDefinition().getUniqueId(), user,
                            newResource.getComponentType(), group, true, false);
                }
            }
        }
        Either<Resource, StorageOperationStatus> dataModelResponse = toscaOperationFacade.updateToscaElement(newResource);
        if (dataModelResponse.isRight()) {
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(dataModelResponse.right().value()), newResource);
            return Either.right(responseFormat);
        } else if (dataModelResponse.left().value() == null) {
            log.debug("No response from updateResource");
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        return Either.left(dataModelResponse.left().value());
    }

    private void updateVfModuleGroupsNames(Resource currentResource, Resource newResource) {
        if (currentResource.getGroups() != null && !currentResource.getName().equals(newResource.getName())) {
            List<GroupDefinition> updatedGroups = currentResource.getGroups().stream()
                .map(group -> getUpdatedGroup(group, currentResource.getName(), newResource.getName())).collect(toList());
            newResource.setGroups(updatedGroups);
        }
    }

    private GroupDefinition getUpdatedGroup(GroupDefinition currGroup, String replacePattern, String with) {
        GroupDefinition updatedGroup = new GroupDefinition(currGroup);
        if (updatedGroup.isSamePrefix(replacePattern) && updatedGroup.getType().equals(DEFAULT_GROUP_VF_MODULE)) {
            String prefix = updatedGroup.getName().substring(0, replacePattern.length());
            String newGroupName = updatedGroup.getName().replaceFirst(prefix, with);
            updatedGroup.setName(newGroupName);
        }
        return updatedGroup;
    }

    /**
     * validateResourceFieldsBeforeCreate
     *
     * @param user - modifier data (userId)
     */
    private void validateResourceFieldsBeforeCreate(User user, Resource resource, AuditingActionEnum actionEnum, boolean inTransaction) {
        componentValidator.validate(user, resource, actionEnum);
        // validate category
        log.debug("validate category");
        validateCategory(user, resource, actionEnum, inTransaction);
        // validate vendor name & release & model number
        log.debug("validate vendor name");
        validateVendorName(user, resource, actionEnum);
        log.debug("validate vendor release");
        validateVendorReleaseName(user, resource, actionEnum);
        log.debug("validate resource vendor model number");
        validateResourceVendorModelNumber(user, resource, actionEnum);
        // validate cost
        log.debug("validate cost");
        validateCost(resource);
        // validate licenseType
        log.debug("validate licenseType");
        validateLicenseType(user, resource, actionEnum);
        // validate template (derived from)
        log.debug("validate derived from");
        if (!ModelConverter.isAtomicComponent(resource) && resource.getResourceType() != ResourceTypeEnum.CVFC) {
            resource.setDerivedFrom(null);
        }
        validateDerivedFromExist(user, resource, actionEnum);
        // warn about non-updatable fields
        checkComponentFieldsForOverrideAttempt(resource);
        String currentCreatorFullName = resource.getCreatorFullName();
        if (currentCreatorFullName != null) {
            log.debug("Resource Creator fullname is automatically set and cannot be updated");
        }
        String currentLastUpdaterFullName = resource.getLastUpdaterFullName();
        if (currentLastUpdaterFullName != null) {
            log.debug("Resource LastUpdater fullname is automatically set and cannot be updated");
        }
        Long currentLastUpdateDate = resource.getLastUpdateDate();
        if (currentLastUpdateDate != null) {
            log.debug("Resource last update date is automatically set and cannot be updated");
        }
        Boolean currentAbstract = resource.isAbstract();
        if (currentAbstract != null) {
            log.debug("Resource abstract is automatically set and cannot be updated");
        }
    }

    /**
     * validateResourceFieldsBeforeUpdate
     *
     * @param currentResource - Resource object to validate
     * @param isNested
     */
    private void validateResourceFieldsBeforeUpdate(Resource currentResource, Resource updateInfoResource, boolean inTransaction, boolean isNested) {
        validateFields(currentResource, updateInfoResource, inTransaction, isNested);
        warnNonEditableFields(currentResource, updateInfoResource);
    }

    private void warnNonEditableFields(Resource currentResource, Resource updateInfoResource) {
        String currentResourceVersion = currentResource.getVersion();
        String updatedResourceVersion = updateInfoResource.getVersion();
        if ((updatedResourceVersion != null) && (!updatedResourceVersion.equals(currentResourceVersion))) {
            log.debug("Resource version is automatically set and cannot be updated");
        }
        String currentCreatorUserId = currentResource.getCreatorUserId();
        String updatedCreatorUserId = updateInfoResource.getCreatorUserId();
        if ((updatedCreatorUserId != null) && (!updatedCreatorUserId.equals(currentCreatorUserId))) {
            log.debug("Resource Creator UserId is automatically set and cannot be updated");
        }
        String currentCreatorFullName = currentResource.getCreatorFullName();
        String updatedCreatorFullName = updateInfoResource.getCreatorFullName();
        if ((updatedCreatorFullName != null) && (!updatedCreatorFullName.equals(currentCreatorFullName))) {
            log.debug("Resource Creator fullname is automatically set and cannot be updated");
        }
        String currentLastUpdaterUserId = currentResource.getLastUpdaterUserId();
        String updatedLastUpdaterUserId = updateInfoResource.getLastUpdaterUserId();
        if ((updatedLastUpdaterUserId != null) && (!updatedLastUpdaterUserId.equals(currentLastUpdaterUserId))) {
            log.debug("Resource LastUpdater userId is automatically set and cannot be updated");
        }
        String currentLastUpdaterFullName = currentResource.getLastUpdaterFullName();
        String updatedLastUpdaterFullName = updateInfoResource.getLastUpdaterFullName();
        if ((updatedLastUpdaterFullName != null) && (!updatedLastUpdaterFullName.equals(currentLastUpdaterFullName))) {
            log.debug("Resource LastUpdater fullname is automatically set and cannot be updated");
        }
        Long currentCreationDate = currentResource.getCreationDate();
        Long updatedCreationDate = updateInfoResource.getCreationDate();
        if ((updatedCreationDate != null) && (!updatedCreationDate.equals(currentCreationDate))) {
            log.debug("Resource Creation date is automatically set and cannot be updated");
        }
        Long currentLastUpdateDate = currentResource.getLastUpdateDate();
        Long updatedLastUpdateDate = updateInfoResource.getLastUpdateDate();
        if ((updatedLastUpdateDate != null) && (!updatedLastUpdateDate.equals(currentLastUpdateDate))) {
            log.debug("Resource last update date is automatically set and cannot be updated");
        }
        LifecycleStateEnum currentLifecycleState = currentResource.getLifecycleState();
        LifecycleStateEnum updatedLifecycleState = updateInfoResource.getLifecycleState();
        if ((updatedLifecycleState != null) && (!updatedLifecycleState.equals(currentLifecycleState))) {
            log.debug("Resource lifecycle state date is automatically set and cannot be updated");
        }
        Boolean currentAbstract = currentResource.isAbstract();
        Boolean updatedAbstract = updateInfoResource.isAbstract();
        if ((updatedAbstract != null) && (!updatedAbstract.equals(currentAbstract))) {
            log.debug("Resource abstract is automatically set and cannot be updated");
        }
        Boolean currentHighestVersion = currentResource.isHighestVersion();
        Boolean updatedHighestVersion = updateInfoResource.isHighestVersion();
        if ((updatedHighestVersion != null) && (!updatedHighestVersion.equals(currentHighestVersion))) {
            log.debug("Resource highest version is automatically set and cannot be updated");
        }
        String currentUuid = currentResource.getUUID();
        String updatedUuid = updateInfoResource.getUUID();
        if ((updatedUuid != null) && (!updatedUuid.equals(currentUuid))) {
            log.debug("Resource UUID is automatically set and cannot be updated");
        }
        log.debug("Resource Type  cannot be updated");
        String currentInvariantUuid = currentResource.getInvariantUUID();
        String updatedInvariantUuid = updateInfoResource.getInvariantUUID();
        if ((updatedInvariantUuid != null) && (!updatedInvariantUuid.equals(currentInvariantUuid))) {
            log.debug("Resource invariant UUID is automatically set and cannot be updated");
            updateInfoResource.setInvariantUUID(currentInvariantUuid);
        }
    }

    private void validateFields(Resource currentResource, Resource updateInfoResource, boolean inTransaction, boolean isNested) {
        boolean hasBeenCertified = ValidationUtils.hasBeenCertified(currentResource.getVersion());
        log.debug("validate resource name before update");
        validateResourceName(currentResource, updateInfoResource, hasBeenCertified, isNested);
        log.debug("validate description before update");
        componentDescriptionValidator.validateAndCorrectField(null, updateInfoResource, null);
        log.debug("validate icon before update");
        validateIcon(currentResource, updateInfoResource, hasBeenCertified);
        log.debug("validate tags before update");
        componentTagsValidator.validateAndCorrectField(null, updateInfoResource, null);
        log.debug("validate vendor name before update");
        validateVendorName(null, updateInfoResource, null);
        log.debug("validate resource vendor model number before update");
        validateResourceVendorModelNumber(currentResource, updateInfoResource);
        log.debug("validate vendor release before update");
        validateVendorReleaseName(null, updateInfoResource, null);
        log.debug("validate contact info before update");
        componentContactIdValidator.validateAndCorrectField(null, updateInfoResource, null);
        log.debug(VALIDATE_DERIVED_BEFORE_UPDATE);
        validateDerivedFromDuringUpdate(currentResource, updateInfoResource, hasBeenCertified);
        log.debug("validate category before update");
        validateCategory(currentResource, updateInfoResource, hasBeenCertified, inTransaction);
    }

    private boolean isResourceNameEquals(Resource currentResource, Resource updateInfoResource) {
        String resourceNameUpdated = updateInfoResource.getName();
        String resourceNameCurrent = currentResource.getName();
        if (resourceNameCurrent.equals(resourceNameUpdated)) {
            return true;
        }
        // In case of CVFC type we should support the case of old VF with CVFC

        // instances that were created without the "Cvfc" suffix
        return currentResource.getResourceType() == ResourceTypeEnum.CVFC && resourceNameUpdated
            .equals(addCvfcSuffixToResourceName(resourceNameCurrent));
    }

    private String addCvfcSuffixToResourceName(String resourceName) {
        return resourceName + "Cvfc";
    }

    private void validateResourceName(Resource currentResource, Resource updateInfoResource, boolean hasBeenCertified, boolean isNested) {
        String resourceNameUpdated = updateInfoResource.getName();
        if (!isResourceNameEquals(currentResource, updateInfoResource)) {
            if (isNested || !hasBeenCertified) {
                componentNameValidator.validateAndCorrectField(null, updateInfoResource, null);
                validateResourceNameUniqueness(updateInfoResource);
                currentResource.setName(resourceNameUpdated);
                currentResource.setNormalizedName(ValidationUtils.normaliseComponentName(resourceNameUpdated));
                currentResource.setSystemName(ValidationUtils.convertToSystemName(resourceNameUpdated));
            } else {
                log.info("Resource name: {}, cannot be updated once the resource has been certified once.", resourceNameUpdated);
                throw new ByActionStatusComponentException(ActionStatus.RESOURCE_NAME_CANNOT_BE_CHANGED);
            }
        }
    }

    private void validateIcon(Resource currentResource, Resource updateInfoResource, boolean hasBeenCertified) {
        String iconUpdated = updateInfoResource.getIcon();
        String iconCurrent = currentResource.getIcon();
        if (!iconCurrent.equals(iconUpdated)) {
            if (!hasBeenCertified) {
                componentIconValidator.validateAndCorrectField(null, updateInfoResource, null);
            } else {
                log.info("Icon {} cannot be updated once the resource has been certified once.", iconUpdated);
                throw new ByActionStatusComponentException(ActionStatus.RESOURCE_ICON_CANNOT_BE_CHANGED);
            }
        }
    }

    private void validateResourceVendorModelNumber(Resource currentResource, Resource updateInfoResource) {
        String updatedResourceVendorModelNumber = updateInfoResource.getResourceVendorModelNumber();
        String currentResourceVendorModelNumber = currentResource.getResourceVendorModelNumber();
        if (!currentResourceVendorModelNumber.equals(updatedResourceVendorModelNumber)) {
            validateResourceVendorModelNumber(null, updateInfoResource, null);
        }
    }

    private Either<Boolean, ResponseFormat> validateCategory(Resource currentResource, Resource updateInfoResource, boolean hasBeenCertified,
                                                             boolean inTransaction) {
        validateCategory(null, updateInfoResource, null, inTransaction);
        if (hasBeenCertified) {
            CategoryDefinition currentCategory = currentResource.getCategories().get(0);
            SubCategoryDefinition currentSubCategory = currentCategory.getSubcategories().get(0);
            CategoryDefinition updateCategory = updateInfoResource.getCategories().get(0);
            SubCategoryDefinition updtaeSubCategory = updateCategory.getSubcategories().get(0);
            if (!currentCategory.getName().equals(updateCategory.getName()) || !currentSubCategory.getName().equals(updtaeSubCategory.getName())) {
                log.info("Category {} cannot be updated once the resource has been certified once.", currentResource.getCategories());
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.RESOURCE_CATEGORY_CANNOT_BE_CHANGED);
                return Either.right(errorResponse);
            }
        }
        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateDerivedFromDuringUpdate(Resource currentResource, Resource updateInfoResource,
                                                                            boolean hasBeenCertified) {
        List<String> currentDerivedFrom = currentResource.getDerivedFrom();
        List<String> updatedDerivedFrom = updateInfoResource.getDerivedFrom();
        if (currentDerivedFrom == null || currentDerivedFrom.isEmpty() || updatedDerivedFrom == null || updatedDerivedFrom.isEmpty()) {
            log.trace("Update normative types");
            return Either.left(true);
        }
        String derivedFromCurrent = currentDerivedFrom.get(0);
        String derivedFromUpdated = updatedDerivedFrom.get(0);
        if (!derivedFromCurrent.equals(derivedFromUpdated)) {
            if (!hasBeenCertified) {
                validateDerivedFromExist(null, updateInfoResource, null);
            } else {
                Either<Boolean, ResponseFormat> validateDerivedFromExtending = validateDerivedFromExtending(null, currentResource, updateInfoResource,
                    null);
                if (validateDerivedFromExtending.isRight() || !validateDerivedFromExtending.left().value()) {
                    log.debug("Derived from cannot be updated if it doesnt inherits directly or extends inheritance");
                    return validateDerivedFromExtending;
                }
            }
        } else {
            // For derived from, we must know whether it was actually changed,

            // otherwise we must do no action.

            // Due to changes it inflicts on data model (remove artifacts,

            // properties...), it's not like a flat field which can be

            // overwritten if not changed.

            // So we must indicate that derived from is not changed
            updateInfoResource.setDerivedFrom(null);
        }
        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateNestedDerivedFromDuringUpdate(Resource currentResource, Resource updateInfoResource,
                                                                                  boolean hasBeenCertified) {
        List<String> currentDerivedFrom = currentResource.getDerivedFrom();
        List<String> updatedDerivedFrom = updateInfoResource.getDerivedFrom();
        if (currentDerivedFrom == null || currentDerivedFrom.isEmpty() || updatedDerivedFrom == null || updatedDerivedFrom.isEmpty()) {
            log.trace("Update normative types");
            return Either.left(true);
        }
        String derivedFromCurrent = currentDerivedFrom.get(0);
        String derivedFromUpdated = updatedDerivedFrom.get(0);
        if (!derivedFromCurrent.equals(derivedFromUpdated)) {
            if (!hasBeenCertified) {
                validateDerivedFromExist(null, updateInfoResource, null);
            } else {
                Either<Boolean, ResponseFormat> validateDerivedFromExtending = validateDerivedFromExtending(null, currentResource, updateInfoResource,
                    null);
                if (validateDerivedFromExtending.isRight() || !validateDerivedFromExtending.left().value()) {
                    log.debug("Derived from cannot be updated if it doesnt inherits directly or extends inheritance");
                    return validateDerivedFromExtending;
                }
            }
        }
        return Either.left(true);
    }

    private void validateDerivedFromExist(User user, Resource resource, AuditingActionEnum actionEnum) {
        if (resource.getDerivedFrom() == null || resource.getDerivedFrom().isEmpty()) {
            return;
        }
        String templateName = resource.getDerivedFrom().get(0);
        Either<Boolean, StorageOperationStatus> dataModelResponse = toscaOperationFacade.validateToscaResourceNameExists(templateName);
        if (dataModelResponse.isRight()) {
            StorageOperationStatus storageStatus = dataModelResponse.right().value();
            BeEcompErrorManager.getInstance().logBeDaoSystemError("Create Resource - validateDerivedFromExist");
            log.debug("request to data model failed with error: {}", storageStatus);
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(storageStatus), resource);
            log.trace("audit before sending response");
            componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(storageStatus));
        } else if (!dataModelResponse.left().value()) {
            log.info("resource template with name: {}, does not exists", templateName);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.PARENT_RESOURCE_NOT_FOUND);
            componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
            throw new ByActionStatusComponentException(ActionStatus.PARENT_RESOURCE_NOT_FOUND);
        }
    }

    // Tal G for extending inheritance US815447
    private Either<Boolean, ResponseFormat> validateDerivedFromExtending(User user, Resource currentResource, Resource updateInfoResource,
                                                                         AuditingActionEnum actionEnum) {
        String currentTemplateName = currentResource.getDerivedFrom().get(0);
        String updatedTemplateName = updateInfoResource.getDerivedFrom().get(0);
        Either<Boolean, StorageOperationStatus> dataModelResponse = toscaOperationFacade
            .validateToscaResourceNameExtends(currentTemplateName, updatedTemplateName, currentResource.getModel());
        if (dataModelResponse.isRight()) {
            StorageOperationStatus storageStatus = dataModelResponse.right().value();
            BeEcompErrorManager.getInstance().logBeDaoSystemError("Create/Update Resource - validateDerivingFromExtendingType");
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(storageStatus), currentResource);
            log.trace("audit before sending response");
            componentsUtils.auditResource(responseFormat, user, currentResource, actionEnum);
            return Either.right(responseFormat);
        }
        if (!dataModelResponse.left().value()) {
            log.info("resource template with name {} does not inherit as original {}", updatedTemplateName, currentTemplateName);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.PARENT_RESOURCE_DOES_NOT_EXTEND);
            componentsUtils.auditResource(responseFormat, user, currentResource, actionEnum);
            return Either.right(responseFormat);
        }
        return Either.left(true);
    }

    public void validateDerivedFromNotEmpty(User user, Resource resource, AuditingActionEnum actionEnum) {
        log.debug("validate resource derivedFrom field");
        if ((resource.getDerivedFrom() == null) || (resource.getDerivedFrom().isEmpty()) || (resource.getDerivedFrom().get(0)) == null || (resource
            .getDerivedFrom().get(0).trim().isEmpty())) {
            log.info("derived from (template) field is missing for the resource");
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_DERIVED_FROM_TEMPLATE);
            componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
            throw new ByActionStatusComponentException(ActionStatus.MISSING_DERIVED_FROM_TEMPLATE);
        }
    }

    private void validateResourceNameUniqueness(Resource resource) {
        Either<Boolean, StorageOperationStatus> resourceOperationResponse = toscaOperationFacade
            .validateComponentNameExists(resource.getName(), resource.getResourceType(), resource.getComponentType());
        if (resourceOperationResponse.isLeft() && resourceOperationResponse.left().value()) {
            log.debug("resource with name: {}, already exists", resource.getName());
            throw new ByActionStatusComponentException(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, ComponentTypeEnum.RESOURCE.getValue(),
                resource.getName());
        } else if (resourceOperationResponse.isRight()) {
            log.debug("error while validateResourceNameExists for resource: {}", resource.getName());
            throw new StorageException(resourceOperationResponse.right().value());
        }
    }

    private void validateCategory(User user, Resource resource, AuditingActionEnum actionEnum, boolean inTransaction) {
        List<CategoryDefinition> categories = resource.getCategories();
        if (CollectionUtils.isEmpty(categories)) {
            log.debug(CATEGORY_IS_EMPTY);
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
            componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
            throw new ByActionStatusComponentException(ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
        }
        if (categories.size() > 1) {
            log.debug("Must be only one category for resource");
            throw new ByActionStatusComponentException(ActionStatus.COMPONENT_TOO_MUCH_CATEGORIES, ComponentTypeEnum.RESOURCE.getValue());
        }
        CategoryDefinition category = categories.get(0);
        List<SubCategoryDefinition> subcategories = category.getSubcategories();
        if (CollectionUtils.isEmpty(subcategories)) {
            log.debug("Missinig subcategory for resource");
            throw new ByActionStatusComponentException(ActionStatus.COMPONENT_MISSING_SUBCATEGORY);
        }
        if (subcategories.size() > 1) {
            log.debug("Must be only one sub category for resource");
            throw new ByActionStatusComponentException(ActionStatus.RESOURCE_TOO_MUCH_SUBCATEGORIES);
        }
        SubCategoryDefinition subcategory = subcategories.get(0);
        if (!ValidationUtils.validateStringNotEmpty(category.getName())) {
            log.debug(CATEGORY_IS_EMPTY);
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
            componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
            throw new ByActionStatusComponentException(ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
        }
        if (!ValidationUtils.validateStringNotEmpty(subcategory.getName())) {
            log.debug(CATEGORY_IS_EMPTY);
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(ActionStatus.COMPONENT_MISSING_SUBCATEGORY, ComponentTypeEnum.RESOURCE.getValue());
            componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
            throw new ByActionStatusComponentException(ActionStatus.COMPONENT_MISSING_SUBCATEGORY, ComponentTypeEnum.RESOURCE.getValue());
        }
        validateCategoryListed(category, subcategory, user, resource, actionEnum, inTransaction);
    }

    private void validateCategoryListed(CategoryDefinition category, SubCategoryDefinition subcategory, User user, Resource resource,
                                        AuditingActionEnum actionEnum, boolean inTransaction) {
        ResponseFormat responseFormat;
        if (category != null && subcategory != null) {
            log.debug("validating resource category {} against valid categories list", category);
            Either<List<CategoryDefinition>, ActionStatus> categories = elementDao.getAllCategories(NodeTypeEnum.ResourceNewCategory, inTransaction);
            if (categories.isRight()) {
                log.debug("failed to retrieve resource categories from JanusGraph");
                responseFormat = componentsUtils.getResponseFormat(categories.right().value());
                componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
                throw new ByActionStatusComponentException(categories.right().value());
            }
            List<CategoryDefinition> categoryList = categories.left().value();
            Optional<CategoryDefinition> foundCategory = categoryList.stream().filter(cat -> cat.getName().equals(category.getName())).findFirst();
            if (foundCategory.isEmpty()) {
                log.debug("Category {} is not part of resource category group. Resource category valid values are {}", category, categoryList);
                failOnInvalidCategory(user, resource, actionEnum);
                return; // explisite output even if failOnInvalidCategory throw an exception
            }
            Optional<SubCategoryDefinition> foundSubcategory = foundCategory.get().getSubcategories().stream()
                .filter(subcat -> subcat.getName().equals(subcategory.getName())).findFirst();
            if (foundSubcategory.isEmpty()) {
                log.debug("SubCategory {} is not part of resource category group. Resource subcategory valid values are {}", subcategory,
                    foundCategory.get().getSubcategories());
                failOnInvalidCategory(user, resource, actionEnum);
            }
        }
    }

    private void failOnInvalidCategory(User user, Resource resource, AuditingActionEnum actionEnum) {
        ResponseFormat responseFormat;
        responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INVALID_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
        componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
        throw new ByActionStatusComponentException(ActionStatus.COMPONENT_INVALID_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
    }

    public void validateVendorReleaseName(User user, Resource resource, AuditingActionEnum actionEnum) {
        String vendorRelease = resource.getVendorRelease();
        log.debug("validate vendor relese name");
        if (!ValidationUtils.validateStringNotEmpty(vendorRelease)) {
            log.info("vendor relese name is missing.");
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_VENDOR_RELEASE);
            componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
            throw new ByActionStatusComponentException(ActionStatus.MISSING_VENDOR_RELEASE);
        }
        validateVendorReleaseName(vendorRelease, user, resource, actionEnum);
    }

    public void validateVendorReleaseName(String vendorRelease, User user, Resource resource, AuditingActionEnum actionEnum) {
        if (vendorRelease != null) {
            if (!ValidationUtils.validateVendorReleaseLength(vendorRelease)) {
                log.info("vendor release exceds limit.");
                ResponseFormat errorResponse = componentsUtils
                    .getResponseFormat(ActionStatus.VENDOR_RELEASE_EXCEEDS_LIMIT, "" + ValidationUtils.VENDOR_RELEASE_MAX_LENGTH);
                componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
                throw new ByActionStatusComponentException(ActionStatus.VENDOR_RELEASE_EXCEEDS_LIMIT, "" + ValidationUtils.VENDOR_RELEASE_MAX_LENGTH);
            }
            if (!ValidationUtils.validateVendorRelease(vendorRelease)) {
                log.info("vendor release  is not valid.");
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_VENDOR_RELEASE);
                componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
                throw new ByActionStatusComponentException(ActionStatus.INVALID_VENDOR_RELEASE, vendorRelease);
            }
        }
    }

    private void validateVendorName(User user, Resource resource, AuditingActionEnum actionEnum) {
        String vendorName = resource.getVendorName();
        if (!ValidationUtils.validateStringNotEmpty(vendorName)) {
            log.info("vendor name is missing.");
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_VENDOR_NAME);
            componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
            throw new ByActionStatusComponentException(ActionStatus.MISSING_VENDOR_NAME);
        }
        validateVendorName(vendorName, user, resource, actionEnum);
    }

    private void validateVendorName(String vendorName, User user, Resource resource, AuditingActionEnum actionEnum) {
        if (vendorName != null) {
            if (!ValidationUtils.validateVendorNameLength(vendorName)) {
                log.info("vendor name exceds limit.");
                ResponseFormat errorResponse = componentsUtils
                    .getResponseFormat(ActionStatus.VENDOR_NAME_EXCEEDS_LIMIT, "" + ValidationUtils.VENDOR_NAME_MAX_LENGTH);
                componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
                throw new ByActionStatusComponentException(ActionStatus.VENDOR_NAME_EXCEEDS_LIMIT, "" + ValidationUtils.VENDOR_NAME_MAX_LENGTH);
            }
            if (!ValidationUtils.validateVendorName(vendorName)) {
                log.info("vendor name  is not valid.");
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_VENDOR_NAME);
                componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
                throw new ByActionStatusComponentException(ActionStatus.INVALID_VENDOR_NAME, vendorName);
            }
        }
    }

    private void validateResourceVendorModelNumber(User user, Resource resource, AuditingActionEnum actionEnum) {
        String resourceVendorModelNumber = resource.getResourceVendorModelNumber();
        if (StringUtils.isNotEmpty(resourceVendorModelNumber)) {
            if (!ValidationUtils.validateResourceVendorModelNumberLength(resourceVendorModelNumber)) {
                log.info("resource vendor model number exceeds limit.");
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.RESOURCE_VENDOR_MODEL_NUMBER_EXCEEDS_LIMIT,
                    "" + ValidationUtils.RESOURCE_VENDOR_MODEL_NUMBER_MAX_LENGTH);
                componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
                throw new ByActionStatusComponentException(ActionStatus.RESOURCE_VENDOR_MODEL_NUMBER_EXCEEDS_LIMIT,
                    "" + ValidationUtils.RESOURCE_VENDOR_MODEL_NUMBER_MAX_LENGTH);
            }
            // resource vendor model number is currently validated as vendor

            // name
            if (!ValidationUtils.validateVendorName(resourceVendorModelNumber)) {
                log.info("resource vendor model number  is not valid.");
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_RESOURCE_VENDOR_MODEL_NUMBER);
                componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
                throw new ByActionStatusComponentException(ActionStatus.INVALID_RESOURCE_VENDOR_MODEL_NUMBER);
            }
        }
    }

    private void validateCost(Resource resource) {
        String cost = resource.getCost();
        if (cost != null) {
            if (!ValidationUtils.validateCost(cost)) {
                log.debug("resource cost is invalid.");
                throw new ByActionStatusComponentException(ActionStatus.INVALID_CONTENT);
            }
        }
    }

    private void validateLicenseType(User user, Resource resource, AuditingActionEnum actionEnum) {
        log.debug("validate licenseType");
        String licenseType = resource.getLicenseType();
        if (licenseType != null) {
            List<String> licenseTypes = ConfigurationManager.getConfigurationManager().getConfiguration().getLicenseTypes();
            if (!licenseTypes.contains(licenseType)) {
                log.debug("License type {} isn't configured", licenseType);
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
                if (actionEnum != null) {
                    // In update case, no audit is required
                    componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
                }
                throw new ByActionStatusComponentException(ActionStatus.INVALID_CONTENT);
            }
        }
    }

    private Either<Boolean, ResponseFormat> processUpdateOfDerivedFrom(Resource currentResource, Resource updatedResource, String userId,
                                                                       boolean inTransaction) {
        if (updatedResource.getDerivedFrom() != null) {
            log.debug("Starting derived from update for resource {}", updatedResource.getUniqueId());
            log.debug("1. Removing interface artifacts from graph");
            // Remove all interface artifacts of resource
            String resourceId = updatedResource.getUniqueId();
            Map<String, InterfaceDefinition> interfaces = currentResource.getInterfaces();
            if (interfaces != null) {
                Collection<InterfaceDefinition> values = interfaces.values();
                for (InterfaceDefinition interfaceDefinition : values) {
                    String interfaceType = interfaceTypeOperation.getShortInterfaceName(interfaceDefinition);
                    log.trace("Starting interface artifacts removal for interface type {}", interfaceType);
                    Map<String, Operation> operations = interfaceDefinition.getOperationsMap();
                    if (operations != null) {
                        for (Entry<String, Operation> operationEntry : operations.entrySet()) {
                            Operation operation = operationEntry.getValue();
                            ArtifactDefinition implementation = operation.getImplementationArtifact();
                            if (implementation != null) {
                                String uniqueId = implementation.getUniqueId();
                                log.debug("Removing interface artifact definition {}, operation {}, interfaceType {}", uniqueId,
                                    operationEntry.getKey(), interfaceType);
                                // only thing that transacts and locks here
                                Either<ArtifactDefinition, ResponseFormat> deleteArtifactByInterface = artifactsBusinessLogic
                                    .deleteArtifactByInterface(resourceId, userId, uniqueId, true);
                                if (deleteArtifactByInterface.isRight()) {
                                    log.debug("Couldn't remove artifact definition with id {}", uniqueId);
                                    if (!inTransaction) {
                                        janusGraphDao.rollback();
                                    }
                                    return Either.right(deleteArtifactByInterface.right().value());
                                }
                            } else {
                                log.trace("No implementation found for operation {} - nothing to delete", operationEntry.getKey());
                            }
                        }
                    } else {
                        log.trace("No operations found for interface type {}", interfaceType);
                    }
                }
            }
            log.debug("2. Removing properties");
            Either<Map<String, PropertyDefinition>, StorageOperationStatus> findPropertiesOfNode = propertyOperation
                .deleteAllPropertiesAssociatedToNode(NodeTypeEnum.Resource, resourceId);
            if (findPropertiesOfNode.isRight() && findPropertiesOfNode.right().value() != StorageOperationStatus.OK) {
                log.debug("Failed to remove all properties of resource");
                if (!inTransaction) {
                    janusGraphDao.rollback();
                }
                return Either
                    .right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(findPropertiesOfNode.right().value())));
            }
        } else {
            log.debug("Derived from wasn't changed during update");
        }
        if (inTransaction) {
            return Either.left(true);
        }
        janusGraphDao.commit();
        return Either.left(true);
    }

    public ICapabilityTypeOperation getCapabilityTypeOperation() {
        return capabilityTypeOperation;
    }

    @Autowired
    public void setCapabilityTypeOperation(ICapabilityTypeOperation capabilityTypeOperation) {
        this.capabilityTypeOperation = capabilityTypeOperation;
    }

    public Boolean validatePropertiesDefaultValues(Resource resource) {
        log.debug("validate resource properties default values");
        List<PropertyDefinition> properties = resource.getProperties();
        if (properties != null) {
            iterateOverProperties(properties, resource.getModel());
        }
        return true;
    }

    public void iterateOverProperties(List<PropertyDefinition> properties, String model) {
        String type = null;
        String innerType = null;
        for (PropertyDefinition property : properties) {
            if (!propertyOperation.isPropertyTypeValid(property, model)) {
                log.info("Invalid type for property {}", property);
                throw new ByActionStatusComponentException(ActionStatus.INVALID_PROPERTY_TYPE, property.getType(), property.getName());
            }
            Map<String, DataTypeDefinition> allDataTypes = componentsUtils.getAllDataTypes(applicationDataTypeCache, model);
            type = property.getType();
            if (type.equals(ToscaPropertyType.LIST.getType()) || type.equals(ToscaPropertyType.MAP.getType())) {
                ResponseFormat responseFormat = validateMapOrListPropertyType(property, innerType, allDataTypes);
                if (responseFormat != null) {
                    break;
                }
            }
            validateDefaultPropertyValue(property, allDataTypes, type, innerType);
        }
    }

    private void validateDefaultPropertyValue(PropertyDefinition property, Map<String, DataTypeDefinition> allDataTypes, String type,
                                              String innerType) {
        if (!propertyOperation.isPropertyDefaultValueValid(property, allDataTypes)) {
            log.info("Invalid default value for property {}", property);
            ResponseFormat responseFormat;
            if (type.equals(ToscaPropertyType.LIST.getType()) || type.equals(ToscaPropertyType.MAP.getType())) {
                throw new ByActionStatusComponentException(ActionStatus.INVALID_COMPLEX_DEFAULT_VALUE, property.getName(), type, innerType,
                    property.getDefaultValue());
            }
            throw new ByActionStatusComponentException(ActionStatus.INVALID_DEFAULT_VALUE, property.getName(), type, property.getDefaultValue());
        }
    }

    private ResponseFormat validateMapOrListPropertyType(PropertyDefinition property, String innerType,
                                                         Map<String, DataTypeDefinition> allDataTypes) {
        ResponseFormat responseFormat = null;
        ImmutablePair<String, Boolean> propertyInnerTypeValid = propertyOperation.isPropertyInnerTypeValid(property, allDataTypes);
        innerType = propertyInnerTypeValid.getLeft();
        if (!propertyInnerTypeValid.getRight()) {
            log.info("Invalid inner type for property {}", property);
            responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_PROPERTY_INNER_TYPE, innerType, property.getName());
        }
        return responseFormat;
    }

    @Override
    public Either<List<String>, ResponseFormat> deleteMarkedComponents() {
        return deleteMarkedComponents(ComponentTypeEnum.RESOURCE);
    }

    @Override
    public ComponentInstanceBusinessLogic getComponentInstanceBL() {
        return componentInstanceBusinessLogic;
    }

    private String getComponentTypeForResponse(Component component) {
        String componentTypeForResponse = "SERVICE";
        if (component instanceof Resource) {
            componentTypeForResponse = ((Resource) component).getResourceType().name();
        }
        return componentTypeForResponse;
    }

    public Either<Resource, ResponseFormat> getLatestResourceFromCsarUuid(String csarUuid, User user) {
        // validate user
        if (user != null) {
            validateUserExists(user);
        }
        // get resource from csar uuid
        Either<Resource, StorageOperationStatus> either = toscaOperationFacade
            .getLatestComponentByCsarOrName(ComponentTypeEnum.RESOURCE, csarUuid, "");
        if (either.isRight()) {
            ResponseFormat resp = componentsUtils.getResponseFormat(ActionStatus.RESOURCE_FROM_CSAR_NOT_FOUND, csarUuid);
            return Either.right(resp);
        }
        return Either.left(either.left().value());
    }

    @Override
    public Either<List<ComponentInstance>, ResponseFormat> getComponentInstancesFilteredByPropertiesAndInputs(String componentId, String userId) {
        return null;
    }

    private Map<String, List<CapabilityDefinition>> getValidComponentInstanceCapabilities(String resourceId,
                                                                                          Map<String, List<CapabilityDefinition>> defaultCapabilities,
                                                                                          Map<String, List<UploadCapInfo>> uploadedCapabilities) {
        Map<String, List<CapabilityDefinition>> validCapabilitiesMap = new HashMap<>();
        uploadedCapabilities.forEach((k, v) -> addValidComponentInstanceCapabilities(k, v, resourceId, defaultCapabilities, validCapabilitiesMap));
        return validCapabilitiesMap;
    }

    private void addValidComponentInstanceCapabilities(String key, List<UploadCapInfo> capabilities, String resourceId,
                                                       Map<String, List<CapabilityDefinition>> defaultCapabilities,
                                                       Map<String, List<CapabilityDefinition>> validCapabilitiesMap) {
        String capabilityType = capabilities.get(0).getType();
        if (defaultCapabilities.containsKey(capabilityType)) {
            CapabilityDefinition defaultCapability = getCapability(resourceId, defaultCapabilities, capabilityType);
            validateCapabilityProperties(capabilities, resourceId, defaultCapability);
            List<CapabilityDefinition> validCapabilityList = new ArrayList<>();
            validCapabilityList.add(defaultCapability);
            validCapabilitiesMap.put(key, validCapabilityList);
        } else {
            throw new ByActionStatusComponentException(ActionStatus.MISSING_CAPABILITY_TYPE, capabilityType);
        }
    }

    private void validateCapabilityProperties(List<UploadCapInfo> capabilities, String resourceId, CapabilityDefinition defaultCapability) {
        if (CollectionUtils.isEmpty(defaultCapability.getProperties()) && isNotEmpty(capabilities.get(0).getProperties())) {
            log.debug("Failed to validate capability {} of component {}. Property list is empty. ", defaultCapability.getName(), resourceId);
            log.debug("Failed to update capability property values. Property list of fetched capability {} is empty. ", defaultCapability.getName());
            throw new ByActionStatusComponentException(ActionStatus.PROPERTY_NOT_FOUND, resourceId);
        } else if (isNotEmpty(capabilities.get(0).getProperties())) {
            validateUniquenessUpdateUploadedComponentInstanceCapability(defaultCapability, capabilities.get(0));
        }
    }

    private CapabilityDefinition getCapability(String resourceId, Map<String, List<CapabilityDefinition>> defaultCapabilities,
                                               String capabilityType) {
        CapabilityDefinition defaultCapability;
        if (isNotEmpty(defaultCapabilities.get(capabilityType).get(0).getProperties())) {
            defaultCapability = defaultCapabilities.get(capabilityType).get(0);
        } else {
            Either<Component, StorageOperationStatus> getFullComponentRes = toscaOperationFacade.getToscaFullElement(resourceId);
            if (getFullComponentRes.isRight()) {
                log.debug("Failed to get full component {}. Status is {}. ", resourceId, getFullComponentRes.right().value());
                throw new ByActionStatusComponentException(ActionStatus.COMPONENT_NOT_FOUND, resourceId);
            }
            defaultCapability = getFullComponentRes.left().value().getCapabilities().get(capabilityType).get(0);
        }
        return defaultCapability;
    }

    private void validateUniquenessUpdateUploadedComponentInstanceCapability(CapabilityDefinition defaultCapability,
                                                                             UploadCapInfo uploadedCapability) {
        List<ComponentInstanceProperty> validProperties = new ArrayList<>();
        Map<String, PropertyDefinition> defaultProperties = defaultCapability.getProperties().stream()
            .collect(toMap(PropertyDefinition::getName, Function.identity()));
        List<UploadPropInfo> uploadedProperties = uploadedCapability.getProperties();
        for (UploadPropInfo property : uploadedProperties) {
            String propertyName = property.getName().toLowerCase();
            String propertyType = property.getType();
            ComponentInstanceProperty validProperty;
            if (defaultProperties.containsKey(propertyName) && propertTypeEqualsTo(defaultProperties, propertyName, propertyType)) {
                throw new ByActionStatusComponentException(ActionStatus.PROPERTY_NAME_ALREADY_EXISTS, propertyName);
            }
            validProperty = new ComponentInstanceProperty();
            validProperty.setName(propertyName);
            if (property.getValue() != null) {
                validProperty.setValue(property.getValue().toString());
            }
            validProperty.setDescription(property.getDescription());
            validProperty.setPassword(property.isPassword());
            validProperties.add(validProperty);
        }
        defaultCapability.setProperties(validProperties);
    }

    private boolean propertTypeEqualsTo(Map<String, PropertyDefinition> defaultProperties, String propertyName, String propertyType) {
        return propertyType != null && !defaultProperties.get(propertyName).getType().equals(propertyType);
    }

    private Either<EnumMap<ArtifactOperationEnum, List<NonMetaArtifactInfo>>, ResponseFormat> organizeVfCsarArtifactsByArtifactOperation(
        List<NonMetaArtifactInfo> artifactPathAndNameList, List<ArtifactDefinition> existingArtifactsToHandle, Resource resource, User user) {
        EnumMap<ArtifactOperationEnum, List<NonMetaArtifactInfo>> nodeTypeArtifactsToHandle = new EnumMap<>(ArtifactOperationEnum.class);
        Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();
        Either<EnumMap<ArtifactOperationEnum, List<NonMetaArtifactInfo>>, ResponseFormat> nodeTypeArtifactsToHandleRes = Either
            .left(nodeTypeArtifactsToHandle);
        try {
            // add all found Csar artifacts to list to upload
            List<NonMetaArtifactInfo> artifactsToUpload = new ArrayList<>(artifactPathAndNameList);
            List<NonMetaArtifactInfo> artifactsToUpdate = new ArrayList<>();
            List<NonMetaArtifactInfo> artifactsToDelete = new ArrayList<>();
            for (NonMetaArtifactInfo currNewArtifact : artifactPathAndNameList) {
                ArtifactDefinition foundArtifact;
                if (!existingArtifactsToHandle.isEmpty()) {
                    foundArtifact = existingArtifactsToHandle.stream().filter(a -> a.getArtifactName().equals(currNewArtifact.getArtifactName()))
                        .findFirst().orElse(null);
                    if (foundArtifact != null) {
                        if (foundArtifact.getArtifactType().equals(currNewArtifact.getArtifactType())) {
                            if (!foundArtifact.getArtifactChecksum().equals(currNewArtifact.getArtifactChecksum())) {
                                currNewArtifact.setArtifactUniqueId(foundArtifact.getUniqueId());
                                // if current artifact already exists, but has

                                // different content, add him to the list to

                                // update
                                artifactsToUpdate.add(currNewArtifact);
                            }
                            // remove found artifact from the list of existing

                            // artifacts to handle, because it was already

                            // handled
                            existingArtifactsToHandle.remove(foundArtifact);
                            // and remove found artifact from the list to

                            // upload, because it should either be updated or be

                            // ignored
                            artifactsToUpload.remove(currNewArtifact);
                        } else {
                            log.debug("Can't upload two artifact with the same name {}.", currNewArtifact.getArtifactName());
                            ResponseFormat responseFormat = ResponseFormatManager.getInstance()
                                .getResponseFormat(ActionStatus.ARTIFACT_ALREADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR, currNewArtifact.getArtifactName(),
                                    currNewArtifact.getArtifactType(), foundArtifact.getArtifactType());
                            AuditingActionEnum auditingAction = artifactsBusinessLogic
                                .detectAuditingType(new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE),
                                    foundArtifact.getArtifactChecksum());
                            artifactsBusinessLogic
                                .handleAuditing(auditingAction, resource, resource.getUniqueId(), user, null, null, foundArtifact.getUniqueId(),
                                    responseFormat, resource.getComponentType(), null);
                            responseWrapper.setInnerElement(responseFormat);
                            break;
                        }
                    }
                }
            }
            if (responseWrapper.isEmpty()) {
                for (ArtifactDefinition currArtifact : existingArtifactsToHandle) {
                    if (currArtifact.getIsFromCsar()) {
                        artifactsToDelete.add(new NonMetaArtifactInfo(currArtifact.getArtifactName(), null, currArtifact.getArtifactType(),
                            currArtifact.getArtifactGroupType(), null, currArtifact.getUniqueId(), currArtifact.getIsFromCsar()));
                    } else {
                        artifactsToUpdate.add(new NonMetaArtifactInfo(currArtifact.getArtifactName(), null, currArtifact.getArtifactType(),
                            currArtifact.getArtifactGroupType(), null, currArtifact.getUniqueId(), currArtifact.getIsFromCsar()));
                    }
                }
            }
            if (responseWrapper.isEmpty()) {
                if (!artifactsToUpload.isEmpty()) {
                    nodeTypeArtifactsToHandle.put(ArtifactOperationEnum.CREATE, artifactsToUpload);
                }
                if (!artifactsToUpdate.isEmpty()) {
                    nodeTypeArtifactsToHandle.put(ArtifactOperationEnum.UPDATE, artifactsToUpdate);
                }
                if (!artifactsToDelete.isEmpty()) {
                    nodeTypeArtifactsToHandle.put(ArtifactOperationEnum.DELETE, artifactsToDelete);
                }
            }
            if (!responseWrapper.isEmpty()) {
                nodeTypeArtifactsToHandleRes = Either.right(responseWrapper.getInnerElement());
            }
        } catch (Exception e) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            responseWrapper.setInnerElement(responseFormat);
            log.debug("Exception occurred when findNodeTypeArtifactsToHandle, error is:{}", e.getMessage(), e);
        }
        return nodeTypeArtifactsToHandleRes;
    }

    ImmutablePair<String, String> buildNestedToscaResourceName(final String nodeResourceType, final String vfResourceName,
                                                               final String nodeTypeFullName) {
        String actualType;
        String actualVfName;
        if (ResourceTypeEnum.CVFC.name().equals(nodeResourceType)) {
            actualVfName = vfResourceName + ResourceTypeEnum.CVFC.name();
            actualType = ResourceTypeEnum.VFC.name();
        } else {
            actualVfName = vfResourceName;
            actualType = nodeResourceType;
        }
        String nameWithouNamespacePrefix;
        try {
            final String nodeTypeNamePrefix = getNodeTypeNamePrefix(nodeTypeFullName);
            log.debug("####### buildNestedToscaResourceName nodeResourceType {}, vfResourceName {}, "
                    + "nodeTypeFullName {}, actualType {}, vfResourceName {} ", nodeResourceType, vfResourceName, nodeTypeFullName, actualType,
                vfResourceName);
            final StringBuilder toscaResourceName = new StringBuilder(nodeTypeNamePrefix);
            if (!nodeTypeFullName.contains(nodeTypeNamePrefix)) {
                nameWithouNamespacePrefix = nodeTypeFullName;
            } else {
                nameWithouNamespacePrefix = nodeTypeFullName.substring(nodeTypeNamePrefix.length());
            }
            final String[] findTypes = nameWithouNamespacePrefix.split("\\.");
            String actualName;
            if (nodeResourceType.equalsIgnoreCase(findTypes[0])) {
                actualName = nameWithouNamespacePrefix.substring(nodeResourceType.length());
            } else {
                actualName = "." + nameWithouNamespacePrefix;
            }
            if (actualName.startsWith(Constants.ABSTRACT)) {
                toscaResourceName.append(nodeResourceType.toLowerCase()).append('.').append(ValidationUtils.convertToSystemName(actualVfName));
            } else {
                toscaResourceName.append(actualType.toLowerCase()).append('.').append(ValidationUtils.convertToSystemName(actualVfName)).append('.')
                    .append(Constants.ABSTRACT);
            }
            final StringBuilder previousToscaResourceName = new StringBuilder(toscaResourceName);
            final String[] actualNames = actualName.split("\\.");
            if (actualNames.length < 3) {
                return new ImmutablePair<>(toscaResourceName.append(actualName.toLowerCase()).toString(),
                    previousToscaResourceName.append(actualName).toString());
            }
            return new ImmutablePair<>(toscaResourceName.append(actualName.toLowerCase()).toString(),
                previousToscaResourceName.append(actualName.substring(actualNames[1].length() + 1).toLowerCase()).toString());
        } catch (final Exception e) {
            log.debug("Exception occured when buildNestedToscaResourceName, error is:{}", e.getMessage(), e);
            throw new ByActionStatusComponentException(ActionStatus.INVALID_TOSCA_TEMPLATE, vfResourceName);
        }
    }

    /**
     * Extracts a Node Type Name prefix from the given Node Type Name.
     *
     * @param fullName Node Type Name
     * @return Node Type Name Prefix
     */
    private String getNodeTypeNamePrefix(final String fullName) {
        String tempPrefix = Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX;
        final List<String> definedNodeTypeNamespaceList = getDefinedNodeTypeNamespaceList();
        log.debug("************* getPrefiX fullName {} FROM {}", fullName, definedNodeTypeNamespaceList);
        final Optional<String> validNameSpace = validateNodeTypeNamePrefix(fullName, definedNodeTypeNamespaceList);
        if (validNameSpace.isPresent()) {
            tempPrefix = validNameSpace.get();
        }
        log.debug("************* getNodeTypeNamePrefix return fullName {} ", tempPrefix);
        return tempPrefix;
    }

    @Override
    public Either<UiComponentDataTransfer, ResponseFormat> getUiComponentDataTransferByComponentId(String resourceId,
                                                                                                   List<String> dataParamsToReturn) {
        ComponentParametersView paramsToReturn = new ComponentParametersView(dataParamsToReturn);
        Either<Resource, StorageOperationStatus> resourceResultEither = toscaOperationFacade.getToscaElement(resourceId, paramsToReturn);
        if (resourceResultEither.isRight()) {
            if (resourceResultEither.right().value() == StorageOperationStatus.NOT_FOUND) {
                log.debug("Failed to found resource with id {} ", resourceId);
                Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, resourceId));
            }
            log.debug("failed to get resource by id {} with filters {}", resourceId, dataParamsToReturn);
            return Either.right(
                componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(resourceResultEither.right().value()), ""));
        }
        Resource resource = resourceResultEither.left().value();
        if (dataParamsToReturn.contains(ComponentFieldsEnum.INPUTS.getValue())) {
            ListUtils.emptyIfNull(resource.getInputs()).forEach(input -> input.setConstraints(setInputConstraint(input)));
        }
        UiComponentDataTransfer dataTransfer = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resource, dataParamsToReturn);
        return Either.left(dataTransfer);
    }

    @Override
    public Either<Component, ActionStatus> shouldUpgradeToLatestDerived(Component clonedComponent) {
        Resource resource = (Resource) clonedComponent;
        if (ModelConverter.isAtomicComponent(resource.getResourceType())) {
            Either<Component, StorageOperationStatus> shouldUpgradeToLatestDerived = toscaOperationFacade.shouldUpgradeToLatestDerived(resource);
            if (shouldUpgradeToLatestDerived.isRight()) {
                return Either.right(componentsUtils.convertFromStorageResponse(shouldUpgradeToLatestDerived.right().value()));
            }
            return Either.left(shouldUpgradeToLatestDerived.left().value());
        } else {
            return super.shouldUpgradeToLatestDerived(clonedComponent);
        }
    }
}
