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

import static org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaElementOperation.createDataType;
import static org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaElementOperation.createDataTypeDefinitionWithName;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.ServletContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.auditing.impl.resourceadmin.AuditImportResourceAdminEventFactory;
import org.openecomp.sdc.be.components.csar.CsarInfo;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.Constants;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaElementTypeEnum;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.DefaultUploadResourceInfo;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.NodeTypesMetadataList;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.UploadComponentInstanceInfo;
import org.openecomp.sdc.be.model.UploadInterfaceInfo;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.mapper.NodeTypeMetadataMapper;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CapabilityTypeOperation;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.yaml.snakeyaml.Yaml;

@org.springframework.stereotype.Component("resourceImportManager")
public class ResourceImportManager {

    static final Pattern PROPERTY_NAME_PATTERN_IGNORE_LENGTH = Pattern.compile("['\\w\\s\\-\\.\\:]+");
    private static final Logger log = Logger.getLogger(ResourceImportManager.class);
    private final InterfaceDefinitionHandler interfaceDefinitionHandler;
    private final ComponentsUtils componentsUtils;
    private final CapabilityTypeOperation capabilityTypeOperation;
    private final JanusGraphDao janusGraphDao;
    private ServletContext servletContext;
    private AuditingManager auditingManager;
    private ResourceBusinessLogic resourceBusinessLogic;
    @Autowired
    private ServiceBusinessLogic serviceBusinessLogic;
    private IGraphLockOperation graphLockOperation;
    private ToscaOperationFacade toscaOperationFacade;
    private ResponseFormatManager responseFormatManager;

    @Autowired
    public ResourceImportManager(final ComponentsUtils componentsUtils,
            final CapabilityTypeOperation capabilityTypeOperation,
            final InterfaceDefinitionHandler interfaceDefinitionHandler, final JanusGraphDao janusGraphDao) {
        this.componentsUtils = componentsUtils;
        this.capabilityTypeOperation = capabilityTypeOperation;
        this.interfaceDefinitionHandler = interfaceDefinitionHandler;
        this.janusGraphDao = janusGraphDao;
    }

    public ServiceBusinessLogic getServiceBusinessLogic() {
        return serviceBusinessLogic;
    }

    public void setServiceBusinessLogic(ServiceBusinessLogic serviceBusinessLogic) {
        this.serviceBusinessLogic = serviceBusinessLogic;
    }

    @Autowired
    public void setToscaOperationFacade(ToscaOperationFacade toscaOperationFacade) {
        this.toscaOperationFacade = toscaOperationFacade;
    }

    public ImmutablePair<Resource, ActionStatus> importNormativeResource(final String resourceYml,
            final UploadResourceInfo resourceMetaData,
            final Map<String, UploadComponentInstanceInfo> instancesFromCsar,
            final User creator, final boolean createNewVersion, final boolean needLock,
            final boolean isInTransaction) {
        LifecycleChangeInfoWithAction lifecycleChangeInfo = new LifecycleChangeInfoWithAction();
        lifecycleChangeInfo.setUserRemarks("certification on import");
        Function<Resource, Boolean> validator = resource -> resourceBusinessLogic
                .validatePropertiesDefaultValues(resource);
        return importCertifiedResource(resourceYml, resourceMetaData, creator, validator, lifecycleChangeInfo,
                isInTransaction, createNewVersion,
                needLock, null, null, false, null, null, false, instancesFromCsar);
    }

    public void importAllNormativeResource(final String resourcesYaml,
            final NodeTypesMetadataList nodeTypesMetadataList, final User user,
            final boolean createNewVersion, final boolean needLock) {
        final Map<String, Object> nodeTypesYamlMap;
        try {
            nodeTypesYamlMap = new Yaml().load(resourcesYaml);
        } catch (final Exception e) {
            log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, ResourceImportManager.class.getName(),
                    "Could not parse node types YAML", e);
            throw new ByActionStatusComponentException(ActionStatus.INVALID_NODE_TYPES_YAML);
        }
        if (!nodeTypesYamlMap.containsKey(ToscaTagNamesEnum.NODE_TYPES.getElementName())) {
            return;
        }
        final Map<String, Object> nodeTypesMap = (Map<String, Object>) nodeTypesYamlMap
                .get(ToscaTagNamesEnum.NODE_TYPES.getElementName());
        importAllNormativeResource(nodeTypesMap, nodeTypesMetadataList, null, user, "", createNewVersion, needLock);
    }

    public void importAllNormativeResource(final Map<String, Object> nodeTypesMap,
            final NodeTypesMetadataList nodeTypesMetadataList,
            Map<String, UploadComponentInstanceInfo> instancesFromCsar, final User user, String model,
            final boolean createNewVersion, final boolean needLock) {
        try {
            nodeTypesMetadataList.getNodeMetadataList().forEach(nodeTypeMetadata -> {
                final String nodeTypeToscaName = nodeTypeMetadata.getToscaName();
                final Map<String, Object> nodeTypeMap = (Map<String, Object>) nodeTypesMap.get(nodeTypeToscaName);
                if (nodeTypeMap == null) {
                    log.warn(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, ResourceImportManager.class.getName(),
                            "Could not find given node type '{}'. The node will not be created.", nodeTypeToscaName);
                } else {
                    final Map<String, Map<String, Map<String, Object>>> nodeTypeDefinitionMap = Map.of(
                            ToscaTagNamesEnum.NODE_TYPES.getElementName(),
                            Map.of(nodeTypeToscaName, nodeTypeMap));
                    final String nodeTypeYaml = new Yaml().dump(nodeTypeDefinitionMap);
                    UploadResourceInfo uploadResourceInfo = NodeTypeMetadataMapper.mapTo(nodeTypeMetadata);
                    if (uploadResourceInfo instanceof DefaultUploadResourceInfo) {
                        uploadResourceInfo.setModel(model);
                        uploadResourceInfo.setContactId(user.getUserId());
                    }
                    importNormativeResource(nodeTypeYaml, uploadResourceInfo, instancesFromCsar, user, createNewVersion,
                            needLock, true);
                }
            });
            janusGraphDao.commit();
        } catch (final Exception e) {
            janusGraphDao.rollback();
            throw e;
        }
    }

    public ImmutablePair<Resource, ActionStatus> importNormativeResourceFromCsar(String resourceYml,
            UploadResourceInfo resourceMetaData,
            User creator, boolean createNewVersion, boolean needLock) {
        LifecycleChangeInfoWithAction lifecycleChangeInfo = new LifecycleChangeInfoWithAction();
        lifecycleChangeInfo.setUserRemarks("certification on import");
        Function<Resource, Boolean> validator = resource -> resourceBusinessLogic
                .validatePropertiesDefaultValues(resource);
        return importCertifiedResource(resourceYml, resourceMetaData, creator, validator, lifecycleChangeInfo, false,
                createNewVersion, needLock,
                null, null, false, null, null, false, null);
    }

    public ImmutablePair<Resource, ActionStatus> importCertifiedResource(
            String resourceYml,
            UploadResourceInfo resourceMetaData,
            User creator,
            Function<Resource, Boolean> validationFunction,
            LifecycleChangeInfoWithAction lifecycleChangeInfo,
            boolean isInTransaction,
            boolean createNewVersion,
            boolean needLock,
            Map<ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle,
            List<ArtifactDefinition> nodeTypesNewCreatedArtifacts,
            boolean forceCertificationAllowed,
            CsarInfo csarInfo,
            String nodeName,
            boolean isNested,
            Map<String, UploadComponentInstanceInfo> instancesFromCsar) {

        System.out.println("[importCertifiedResource] ENTER");
        System.out.println("[importCertifiedResource] resourceMetaData.name=" + resourceMetaData.getName());
        System.out.println("[importCertifiedResource] createNewVersion=" + createNewVersion);
        System.out.println("[importCertifiedResource] needLock=" + needLock);
        System.out.println("[importCertifiedResource] nodeName=" + nodeName);
        System.out.println("[importCertifiedResource] isNested=" + isNested);
        System.out.println("[importCertifiedResource] yaml present=" + (resourceYml != null));

        Resource resource = new Resource();
        ImmutablePair<Resource, ActionStatus> responsePair = new ImmutablePair<>(resource, ActionStatus.CREATED);

        Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> response = Either.left(responsePair);

        String latestCertifiedResourceId = null;

        try {
            boolean shouldBeCertified = nodeTypeArtifactsToHandle == null || nodeTypeArtifactsToHandle.isEmpty();

            System.out.println("[importCertifiedResource] shouldBeCertified=" + shouldBeCertified);
            System.out.println("[importCertifiedResource] Setting constant metadata...");
            setConstantMetaData(resource, shouldBeCertified);

            System.out.println("[importCertifiedResource] Setting resource metadata ...");
            setResourceMetaData(resource, resourceYml, resourceMetaData);

            System.out.println("[importCertifiedResource] Populating resource from YAML...");
            populateResourceFromYaml(resourceYml, resource, instancesFromCsar);

            System.out.println("[importCertifiedResource] Validating resource...");
            validationFunction.apply(resource);

            resource.getComponentMetadataDefinition()
                    .getMetadataDataDefinition()
                    .setNormative(resourceMetaData.isNormative());

            System.out.println("[importCertifiedResource] Checking resource existence (createNewVersion=" +
                    createNewVersion + ")...");
            checkResourceExists(createNewVersion, csarInfo, resource);

            System.out.println("[importCertifiedResource] Creating/updating resource by import...");
            resource = resourceBusinessLogic
                    .createOrUpdateResourceByImport(resource, creator, true, isInTransaction, needLock,
                            csarInfo, nodeName, isNested).left;

            // Handle artifacts if present
            if (nodeTypeArtifactsToHandle != null && !nodeTypeArtifactsToHandle.isEmpty()) {
                System.out.println("[importCertifiedResource] Handling Node Type Artifacts...");

                Either<List<ArtifactDefinition>, ResponseFormat> handleNodeTypeArtifactsRes = resourceBusinessLogic
                        .handleNodeTypeArtifacts(resource, nodeTypeArtifactsToHandle,
                                nodeTypesNewCreatedArtifacts, creator,
                                isInTransaction, false);

                if (handleNodeTypeArtifactsRes.isRight()) {
                    System.out.println("[importCertifiedResource] Artifact handling failed. Throwing error.");
                    throw new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR);
                }
                System.out.println("[importCertifiedResource] Node Type Artifacts processed successfully.");
            }

            System.out.println("[importCertifiedResource] Getting latest certified resource ID...");
            latestCertifiedResourceId = getLatestCertifiedResourceId(resource);
            System.out.println("[importCertifiedResource] latestCertifiedResourceId=" + latestCertifiedResourceId);

            System.out.println("[importCertifiedResource] Propagating state to CERTIFIED...");
            Resource changeStateResponse = resourceBusinessLogic.propagateStateToCertified(
                    creator, resource, lifecycleChangeInfo, isInTransaction, needLock, forceCertificationAllowed);

            responsePair = new ImmutablePair<>(changeStateResponse, response.left().value().right);

            System.out.println("[importCertifiedResource] Resource successfully certified.");

        } catch (RuntimeException e) {
            System.out.println("[importCertifiedResource] ERROR: " + e.getMessage());
            e.printStackTrace(System.out);
            handleImportResourceException(resourceMetaData, creator, true, e);

        } finally {
            if (latestCertifiedResourceId != null && needLock) {
                System.out.println("[importCertifiedResource] Unlocking resource: " + latestCertifiedResourceId);
                graphLockOperation.unlockComponent(latestCertifiedResourceId, NodeTypeEnum.Resource);
            }
            System.out.println("[importCertifiedResource] EXIT");
        }

        return responsePair;
    }

    private void checkResourceExists(final boolean isCreate, final CsarInfo csarInfo, final Resource resource) {
        if (isCreate) {
            checkResourceExistsOnCreate(resource, csarInfo);
        } else {
            checkResourceExistsOnUpdate(resource);
        }
    }

    private void checkResourceExistsOnCreate(final Resource resource, final CsarInfo csarInfo) {

        System.out.println("Entering checkResourceExistsOnCreate for resource: "
                + resource.getName() + ", vendorRelease=" + resource.getVendorRelease()
                + ", model=" + resource.getModel());

        if (isCsarPresent(csarInfo)) {
            System.out.println("CSAR is present. Skipping resource existence check.");
            return;
        }

        System.out.println("Calling getComponentByNameAndVendorRelease for resource: " + resource.getName());

        final Either<Resource, StorageOperationStatus> resourceEither = toscaOperationFacade
                .getComponentByNameAndVendorRelease(
                        resource.getComponentType(),
                        resource.getName(),
                        resource.getVendorRelease(),
                        JsonParseFlagEnum.ParseAll,
                        resource.getModel());

        System.out.println("Lookup result isLeft=" + resourceEither.isLeft());

        if (resourceEither.isLeft() && toscaOperationFacade.isNodeAssociatedToModel(resource.getModel(), resource)) {

            System.out.println("Resource already exists with same vendorRelease and model association.");

            if (resource.getModel() == null) {
                System.out.println("Throwing COMPONENT_WITH_VENDOR_RELEASE_ALREADY_EXISTS");
                throw new ByActionStatusComponentException(
                        ActionStatus.COMPONENT_WITH_VENDOR_RELEASE_ALREADY_EXISTS,
                        resource.getName(),
                        resource.getVendorRelease());
            }

            System.out.println("Throwing COMPONENT_WITH_VENDOR_RELEASE_ALREADY_EXISTS_IN_MODEL");
            throw new ByActionStatusComponentException(
                    ActionStatus.COMPONENT_WITH_VENDOR_RELEASE_ALREADY_EXISTS_IN_MODEL,
                    resource.getName(),
                    resource.getVendorRelease(),
                    resource.getModel());
        }

        System.out.println("No existing resource conflict found. Exiting checkResourceExistsOnCreate.");
    }

    private void checkResourceExistsOnUpdate(final Resource resource) {

        System.out.println("Entering checkResourceExistsOnUpdate for resource: "
                + resource.getName() + ", model=" + resource.getModel());

        final String model = resource.getModel();

        System.out.println("Calling getLatestByName for resource: " + resource.getName());

        final Either<Resource, StorageOperationStatus> latestByName = toscaOperationFacade
                .getLatestByName(resource.getName(), model);

        System.out.println("Lookup result isLeft=" + latestByName.isLeft());

        if (latestByName.isLeft() && toscaOperationFacade.isNodeAssociatedToModel(model, resource)) {

            System.out.println("Resource with same name already exists in model association.");

            if (model == null) {
                System.out.println("Throwing COMPONENT_NAME_ALREADY_EXIST");
                throw new ByActionStatusComponentException(
                        ActionStatus.COMPONENT_NAME_ALREADY_EXIST,
                        resource.getResourceType().name(),
                        resource.getName());
            }

            System.out.println("Throwing COMPONENT_WITH_MODEL_ALREADY_EXIST");
            throw new ByActionStatusComponentException(
                    ActionStatus.COMPONENT_WITH_MODEL_ALREADY_EXIST,
                    resource.getName(),
                    model);
        }

        System.out.println("No existing resource conflict found. Exiting checkResourceExistsOnUpdate.");
    }

    private boolean isCsarPresent(final CsarInfo csarInfo) {
        return csarInfo != null && StringUtils.isNotEmpty(csarInfo.getCsarUUID());
    }

    private String getLatestCertifiedResourceId(Resource resource) {
        Map<String, String> allVersions = resource.getAllVersions();
        Double latestCertifiedVersion = 0.0;
        if (allVersions != null) {
            for (String version : allVersions.keySet()) {
                Double dVersion = Double.valueOf(version);
                if ((dVersion > latestCertifiedVersion) && (version.endsWith(".0"))) {
                    latestCertifiedVersion = dVersion;
                }
            }
            return allVersions.get(String.valueOf(latestCertifiedVersion));
        } else {
            return null;
        }
    }

    public void populateResourceMetadata(UploadResourceInfo resourceMetaData, Resource resource) {
        if (resource != null && resourceMetaData != null) {
            resource.setDescription(resourceMetaData.getDescription());
            resource.setTags(resourceMetaData.getTags());
            resource.setCategories(resourceMetaData.getCategories());
            resource.setContactId(resourceMetaData.getContactId());
            resource.setName(resourceMetaData.getName());
            resource.setIcon(resourceMetaData.getResourceIconPath());
            resource.setResourceVendorModelNumber(resourceMetaData.getResourceVendorModelNumber());
            resource.setResourceType(ResourceTypeEnum.valueOf(resourceMetaData.getResourceType()));
            resource.setTenant(resourceMetaData.getTenant());
            if (resourceMetaData.getVendorName() != null) {
                resource.setVendorName(resourceMetaData.getVendorName());
            }
            if (resourceMetaData.getVendorRelease() != null) {
                resource.setVendorRelease(resourceMetaData.getVendorRelease());
            }
            if (resourceMetaData.getModel() != null) {
                resource.setModel(resourceMetaData.getModel());
            }
        }
    }

    public ImmutablePair<Resource, ActionStatus> importUserDefinedResource(String resourceYml,
            UploadResourceInfo resourceMetaData, User creator,
            boolean isInTransaction) {
        Resource resource = new Resource();
        ImmutablePair<Resource, ActionStatus> responsePair = new ImmutablePair<>(resource, ActionStatus.CREATED);
        try {
            setMetaDataFromJson(resourceMetaData, resource);
            populateResourceFromYaml(resourceYml, resource, null);
            // currently import VF isn't supported. In future will be supported import VF
            // only with CSAR file!!
            if (ResourceTypeEnum.VF == resource.getResourceType()) {
                log.debug("Now import VF isn't supported. It will be supported in future with CSAR file only");
                throw new ByActionStatusComponentException(ActionStatus.RESTRICTED_OPERATION);
            }
            resourceBusinessLogic.validateDerivedFromNotEmpty(creator, resource, AuditingActionEnum.CREATE_RESOURCE);
            resourceBusinessLogic.validatePropertiesDefaultValues(resource);
            responsePair = resourceBusinessLogic.createOrUpdateResourceByImport(resource, creator, false,
                    isInTransaction, true, null, null, false);
        } catch (RuntimeException e) {
            handleImportResourceException(resourceMetaData, creator, false, e);
        }
        return responsePair;
    }

    private void populateResourceFromYaml(final String resourceYml, Resource resource,
            Map<String, UploadComponentInstanceInfo> instancesFromCsar) {

        System.out.println("[populateResourceFromYaml] ENTER");
        System.out.println("[populateResourceFromYaml] yaml present=" + (resourceYml != null));
        System.out.println("[populateResourceFromYaml] resource name=" + resource.getName() +
                ", type=" + resource.getResourceType() +
                ", model=" + resource.getModel());

        @SuppressWarnings("unchecked")
        Object ymlObj = new Yaml().load(resourceYml);

        if (ymlObj instanceof Map) {
            System.out.println("[populateResourceFromYaml] YAML parsed as Map.");

            final Either<Resource, StorageOperationStatus> existingResource = getExistingResource(resource);
            System.out.println("[populateResourceFromYaml] existingResource.isLeft=" + existingResource.isLeft());

            final Map<String, Object> toscaJsonAll = (Map<String, Object>) ymlObj;
            Map<String, Object> toscaJson = toscaJsonAll;

            boolean hasNodeTypes = toscaJsonAll.containsKey(ToscaTagNamesEnum.NODE_TYPES.getElementName());
            System.out.println("[populateResourceFromYaml] NODE_TYPES present=" + hasNodeTypes);

            if (hasNodeTypes && resource.getResourceType() != ResourceTypeEnum.CVFC) {
                System.out.println("[populateResourceFromYaml] Focusing JSON down to NODE_TYPES only (non-CVFC).");
                toscaJson = new HashMap<>();
                toscaJson.put(
                        ToscaTagNamesEnum.NODE_TYPES.getElementName(),
                        toscaJsonAll.get(ToscaTagNamesEnum.NODE_TYPES.getElementName()));
            } else if (hasNodeTypes) {
                System.out.println("[populateResourceFromYaml] Resource type is CVFC; keeping full JSON.");
            }

            // Extract data types
            final List<Object> foundElements = new ArrayList<>();
            System.out.println("[populateResourceFromYaml] Searching for DATA_TYPES in TOSCA...");
            final Either<List<Object>, ResultStatusEnum> toscaElements = ImportUtils.findToscaElements(
                    toscaJsonAll,
                    ToscaTagNamesEnum.DATA_TYPES.getElementName(),
                    ToscaElementTypeEnum.MAP,
                    foundElements);

            if (toscaElements.isLeft()) {
                System.out
                        .println("[populateResourceFromYaml] DATA_TYPES found. elementsCount=" + foundElements.size());
                final Map<String, Object> toscaAttributes = (Map<String, Object>) foundElements.get(0);

                if (MapUtils.isNotEmpty(toscaAttributes)) {
                    System.out.println("[populateResourceFromYaml] Extracting DataTypes from JSON...");
                    resource.setDataTypes(
                            extractDataTypeFromJson(resourceBusinessLogic, toscaAttributes, resource.getModel()));
                    System.out.println("[populateResourceFromYaml] DataTypes set. count=" +
                            (resource.getDataTypes() != null ? resource.getDataTypes().size() : 0));
                } else {
                    System.out.println("[populateResourceFromYaml] DATA_TYPES map is empty.");
                }
            } else {
                System.out.println("[populateResourceFromYaml] DATA_TYPES not found in YAML.");
            }

            // Derived from
            System.out.println("[populateResourceFromYaml] Setting 'derived from' ...");
            final Resource parentResource = setDerivedFrom(toscaJson, resource);
            System.out.println("[populateResourceFromYaml] 'derived from' set. parentResource=" +
                    (parentResource != null ? parentResource.getName() : "null"));

            // TOSCA name
            if (StringUtils.isEmpty(resource.getToscaResourceName())) {
                System.out.println("[populateResourceFromYaml] TOSCA resource name empty. Setting from JSON...");
                setToscaResourceName(toscaJson, resource);
                System.out.println("[populateResourceFromYaml] toscaResourceName=" + resource.getToscaResourceName());
            } else {
                System.out.println("[populateResourceFromYaml] TOSCA resource name already present: " +
                        resource.getToscaResourceName());
            }

            // Capabilities
            System.out.println("[populateResourceFromYaml] Setting capabilities...");
            setCapabilities(toscaJson, resource, parentResource);
            System.out.println("[populateResourceFromYaml] Capabilities set. count=" +
                    (resource.getCapabilities() != null ? resource.getCapabilities().size() : 0));

            // Properties
            System.out.println("[populateResourceFromYaml] Setting properties...");
            setProperties(toscaJson, resource, existingResource);
            System.out.println("[populateResourceFromYaml] Properties set. count=" +
                    (resource.getProperties() != null ? resource.getProperties().size() : 0));

            // Attributes
            System.out.println("[populateResourceFromYaml] Setting attributes...");
            setAttributes(toscaJson, resource);
            System.out.println("[populateResourceFromYaml] Attributes set. count=" +
                    (resource.getAttributes() != null ? resource.getAttributes().size() : 0));

            // Requirements
            System.out.println("[populateResourceFromYaml] Setting requirements...");
            setRequirements(toscaJson, resource, parentResource);
            System.out.println("[populateResourceFromYaml] Requirements set. count=" +
                    (resource.getRequirements() != null ? resource.getRequirements().size() : 0));

            // Interfaces
            System.out.println("[populateResourceFromYaml] Setting interface lifecycle (instancesFromCsar present=" +
                    (instancesFromCsar != null && !instancesFromCsar.isEmpty()) + ") ...");
            setInterfaceLifecycle(toscaJson, resource, existingResource, instancesFromCsar);
            System.out.println("[populateResourceFromYaml] Interface lifecycle set.");

            System.out.println("[populateResourceFromYaml] EXIT (normal)");

        } else {
            System.out.println("[populateResourceFromYaml] YAML did not parse to Map. Throwing GENERAL_ERROR.");
            throw new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    private Either<Resource, StorageOperationStatus> getExistingResource(final Resource resource) {

        System.out.println("[getExistingResource] ENTER");
        System.out.println("[getExistingResource] resource name=" + resource.getName() +
                ", type=" + resource.getResourceType() +
                ", model=" + resource.getModel());

        final Either<List<GraphVertex>, JanusGraphOperationStatus> byCriteria = janusGraphDao.getByCriteria(
                getVertexTypeEnum(resource.getResourceType()),
                propertiesToMatch(resource),
                propertiesToNotMatch(),
                JsonParseFlagEnum.ParseAll,
                resource.getModel(),
                false);

        if (byCriteria.isLeft()) {
            final List<GraphVertex> graphVertexList = byCriteria.left().value();
            System.out.println("[getExistingResource] byCriteria LEFT. verticesFound=" +
                    (graphVertexList != null ? graphVertexList.size() : 0));

            if (CollectionUtils.isNotEmpty(graphVertexList)) {
                if (graphVertexList.size() == 1) {
                    final String uid = graphVertexList.get(0).getUniqueId();
                    System.out.println("[getExistingResource] Single vertex found. uniqueId=" + uid);
                    final Either<Resource, StorageOperationStatus> res = toscaOperationFacade.getToscaElement(uid);
                    System.out.println("[getExistingResource] EXIT (single vertex path)");
                    return res;
                } else {
                    System.out.println("[getExistingResource] Multiple vertices found. Selecting by max VERSION...");
                    final Optional<GraphVertex> vertex = graphVertexList.stream()
                            .max(Comparator.comparing(
                                    gv -> (String) gv.getMetadataProperties().get(GraphPropertyEnum.VERSION)));

                    if (vertex.isPresent()) {
                        final String uid = vertex.get().getUniqueId();
                        final Object verObj = vertex.get().getMetadataProperties().get(GraphPropertyEnum.VERSION);
                        System.out.println("[getExistingResource] Selected vertex uniqueId=" + uid +
                                ", version=" + (verObj != null ? verObj.toString() : "null"));
                        final Either<Resource, StorageOperationStatus> res = toscaOperationFacade.getToscaElement(uid);
                        System.out.println("[getExistingResource] EXIT (multi vertex path)");
                        return res;
                    } else {
                        System.out.println("[getExistingResource] No vertex selected from multiple (unexpected).");
                    }
                }
            } else {
                System.out.println("[getExistingResource] No vertices returned by criteria.");
            }
        } else {
            System.out.println("[getExistingResource] byCriteria RIGHT. status=" + byCriteria.right().value());
        }

        System.out.println("[getExistingResource] EXIT (NOT_FOUND)");
        return Either.right(StorageOperationStatus.NOT_FOUND);
    }

    private VertexTypeEnum getVertexTypeEnum(final ResourceTypeEnum resourceType) {
        return ModelConverter.isAtomicComponent(resourceType) ? VertexTypeEnum.NODE_TYPE
                : VertexTypeEnum.TOPOLOGY_TEMPLATE;
    }

    private Map<GraphPropertyEnum, Object> propertiesToMatch(final Resource resource) {
        final Map<GraphPropertyEnum, Object> graphProperties = new EnumMap<>(GraphPropertyEnum.class);
        graphProperties.put(GraphPropertyEnum.NORMALIZED_NAME,
                ValidationUtils.normaliseComponentName(resource.getName()));
        graphProperties.put(GraphPropertyEnum.COMPONENT_TYPE, resource.getComponentType().name());
        graphProperties.put(GraphPropertyEnum.RESOURCE_TYPE, resource.getResourceType().name());
        graphProperties.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        return graphProperties;
    }

    private Map<GraphPropertyEnum, Object> propertiesToNotMatch() {
        final Map<GraphPropertyEnum, Object> graphProperties = new EnumMap<>(GraphPropertyEnum.class);
        graphProperties.put(GraphPropertyEnum.IS_DELETED, true);
        graphProperties.put(GraphPropertyEnum.IS_ARCHIVED, true);
        return graphProperties;
    }

    private void setToscaResourceName(Map<String, Object> toscaJson, Resource resource) {
        resource.setToscaResourceName(getToscaResourceName(toscaJson));
    }

    private String getToscaResourceName(Map<String, Object> toscaJson) {

        System.out.println("[getToscaResourceName] ENTER");
        System.out.println("[getToscaResourceName] toscaJson null=" + (toscaJson == null));

        Either<Map<String, Object>, ResultStatusEnum> toscaElement = ImportUtils.findFirstToscaMapElement(toscaJson,
                ToscaTagNamesEnum.NODE_TYPES);

        if (toscaElement.isLeft()) {
            Map<String, Object> nodeTypesMap = toscaElement.left().value();

            System.out.println("[getToscaResourceName] NODE_TYPES found. size=" + nodeTypesMap.size());

            if (nodeTypesMap.size() == 1) {
                String name = nodeTypesMap.keySet().iterator().next();
                System.out.println("[getToscaResourceName] Single node type found. toscaName=" + name);
                System.out.println("[getToscaResourceName] EXIT");
                return name;
            } else {
                System.out.println("[getToscaResourceName] More than one node type — cannot determine unique name.");
            }
        } else {
            System.out.println("[getToscaResourceName] NODE_TYPES not found in TOSCA JSON.");
        }

        System.out.println("[getToscaResourceName] EXIT (returning null)");
        return null;
    }

    private void setInterfaceLifecycle(Map<String, Object> toscaJson, Resource resource,
            Either<Resource, StorageOperationStatus> existingResource,
            Map<String, UploadComponentInstanceInfo> instancesFromCsar) {
        final Either<Map<String, Object>, ResultStatusEnum> toscaInterfaces = ImportUtils
                .findFirstToscaMapElement(toscaJson, ToscaTagNamesEnum.INTERFACES);
        final Map<String, InterfaceDefinition> moduleInterfaces = new HashMap<>();
        final Map<String, Object> map;
        List<UploadInterfaceInfo> interfaceInfoList = null;
        if (MapUtils.isNotEmpty(instancesFromCsar)) {
            interfaceInfoList = instancesFromCsar.values().stream().filter(i -> MapUtils.isNotEmpty(i.getInterfaces()))
                    .flatMap(i -> i.getInterfaces().values().stream()).collect(Collectors.toList());
        }
        if (toscaInterfaces.isLeft()) {
            map = toscaInterfaces.left().value();
            for (final Entry<String, Object> interfaceNameValue : map.entrySet()) {
                final Either<InterfaceDefinition, ResultStatusEnum> eitherInterface = createModuleInterface(
                        interfaceNameValue.getValue(), resource.getModel());
                if (eitherInterface.isRight()) {
                    log.info("error when creating interface:{}, for resource:{}", interfaceNameValue.getKey(),
                            resource.getName());
                } else {
                    final InterfaceDefinition interfaceDefinition = eitherInterface.left().value();
                    if (CollectionUtils.isNotEmpty(interfaceInfoList)) {
                        updateInterfaceDefinition(interfaceDefinition, interfaceInfoList);
                    }
                    moduleInterfaces.put(interfaceDefinition.getType(), interfaceDefinition);
                }
            }
        } else {
            map = Collections.emptyMap();
        }
        if (existingResource.isLeft()) {
            final Map<String, InterfaceDefinition> interfaces = existingResource.left().value().getInterfaces();
            if (MapUtils.isNotEmpty(interfaces)) {
                final Map<String, InterfaceDefinition> userCreatedInterfaceDefinitions = interfaces.entrySet().stream()
                        .filter(i -> i.getValue().isUserCreated())
                        .filter(i -> !map.containsKey(i.getValue().getType()))
                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
                if (MapUtils.isNotEmpty(userCreatedInterfaceDefinitions)) {
                    moduleInterfaces.putAll(userCreatedInterfaceDefinitions);
                }
            }
        }

        if (MapUtils.isNotEmpty(moduleInterfaces)) {
            resource.setInterfaces(moduleInterfaces);
        }
    }

    private void updateInterfaceDefinition(InterfaceDefinition interfaceDefinition,
            List<UploadInterfaceInfo> interfaceInfoList) {
        Map<String, OperationDataDefinition> operations = new HashMap<>();
        interfaceInfoList.stream().filter(i -> interfaceDefinition.getType().endsWith(i.getKey())).forEach(i -> {
            i.getOperations().values().forEach(o -> {
                o.setImplementation(null);
            });
            operations.putAll(i.getOperations());
        });
        interfaceDefinition.setOperations(operations);
    }

    private Either<InterfaceDefinition, ResultStatusEnum> createModuleInterface(final Object interfaceJson,
            final String model) {
        try {
            if (interfaceJson instanceof String) {
                final InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
                interfaceDefinition.setType((String) interfaceJson);
                return Either.left(interfaceDefinition);
            }
            if (interfaceJson instanceof Map) {
                final Map<String, Object> interfaceJsonMap = (Map<String, Object>) interfaceJson;
                final InterfaceDefinition interfaceDefinition = interfaceDefinitionHandler.create(interfaceJsonMap,
                        model);
                return Either.left(interfaceDefinition);
            }
            return Either.right(ResultStatusEnum.GENERAL_ERROR);
        } catch (final Exception e) {
            BeEcompErrorManager.getInstance().logBeSystemError("Import Resource- create interface");
            log.debug("error when creating interface, message:{}", e.getMessage(), e);
            return Either.right(ResultStatusEnum.GENERAL_ERROR);
        }
    }

    private void setRequirements(Map<String, Object> toscaJson, Resource resource,
            Resource parentResource) {// Note that parentResource can be null
        Either<List<Object>, ResultStatusEnum> toscaRequirements = ImportUtils
                .findFirstToscaListElement(toscaJson, ToscaTagNamesEnum.REQUIREMENTS);
        if (toscaRequirements.isLeft()) {
            List<Object> jsonRequirements = toscaRequirements.left().value();
            Map<String, List<RequirementDefinition>> moduleRequirements = new HashMap<>();
            // Checking for name duplication
            Set<String> reqNames = new HashSet<>();
            // Getting flattened list of capabilities of parent node - cap name to cap type
            Map<String, String> reqName2TypeMap = getReqName2Type(parentResource);
            for (Object jsonRequirementObj : jsonRequirements) {
                // Requirement
                Map<String, Object> requirementJsonWrapper = (Map<String, Object>) jsonRequirementObj;
                String requirementName = requirementJsonWrapper.keySet().iterator().next();
                String reqNameLowerCase = requirementName.toLowerCase();
                if (reqNames.contains(reqNameLowerCase)) {
                    log.debug(
                            "More than one requirement with same name {} (case-insensitive) in imported TOSCA file is invalid",
                            reqNameLowerCase);
                    throw new ByActionStatusComponentException(ActionStatus.IMPORT_DUPLICATE_REQ_CAP_NAME,
                            "requirement", reqNameLowerCase);
                }
                reqNames.add(reqNameLowerCase);
                RequirementDefinition requirementDef = createRequirementFromImportFile(
                        requirementJsonWrapper.get(requirementName));
                requirementDef.setName(requirementName);
                if (moduleRequirements.containsKey(requirementDef.getCapability())) {
                    moduleRequirements.get(requirementDef.getCapability()).add(requirementDef);
                } else {
                    List<RequirementDefinition> list = new ArrayList<>();
                    list.add(requirementDef);
                    moduleRequirements.put(requirementDef.getCapability(), list);
                }
                // Validating against req/cap of "derived from" node
                Boolean validateVsParentCap = validateCapNameVsDerived(reqName2TypeMap, requirementDef.getCapability(),
                        requirementDef.getName());
                if (!validateVsParentCap) {
                    String parentResourceName = parentResource != null ? parentResource.getName() : "";
                    log.debug("Requirement with name {} already exists in parent {}", requirementDef.getName(),
                            parentResourceName);
                    throw new ByActionStatusComponentException(ActionStatus.IMPORT_REQ_CAP_NAME_EXISTS_IN_DERIVED,
                            "requirement",
                            requirementDef.getName().toLowerCase(), parentResourceName);
                }
            }
            if (moduleRequirements.size() > 0) {
                resource.setRequirements(moduleRequirements);
            }
        }
    }

    private RequirementDefinition createRequirementFromImportFile(Object requirementJson) {
        RequirementDefinition requirement = new RequirementDefinition();
        if (requirementJson instanceof String) {
            String requirementJsonString = (String) requirementJson;
            requirement.setCapability(requirementJsonString);
        } else if (requirementJson instanceof Map) {
            Map<String, Object> requirementJsonMap = (Map<String, Object>) requirementJson;
            if (requirementJsonMap.containsKey(ToscaTagNamesEnum.CAPABILITY.getElementName())) {
                requirement
                        .setCapability((String) requirementJsonMap.get(ToscaTagNamesEnum.CAPABILITY.getElementName()));
            }
            if (requirementJsonMap.containsKey(ToscaTagNamesEnum.NODE.getElementName())) {
                requirement.setNode((String) requirementJsonMap.get(ToscaTagNamesEnum.NODE.getElementName()));
            }
            if (requirementJsonMap.containsKey(ToscaTagNamesEnum.RELATIONSHIP.getElementName())) {
                requirement.setRelationship(
                        (String) requirementJsonMap.get(ToscaTagNamesEnum.RELATIONSHIP.getElementName()));
            }
            if (requirementJsonMap.containsKey(ToscaTagNamesEnum.OCCURRENCES.getElementName())) {
                List<Object> occurrencesList = (List) requirementJsonMap
                        .get(ToscaTagNamesEnum.OCCURRENCES.getElementName());
                validateOccurrences(occurrencesList);
                requirement.setMinOccurrences(occurrencesList.get(0).toString());
                requirement.setMaxOccurrences(occurrencesList.get(1).toString());
            }
        } else {
            throw new ByActionStatusComponentException(ActionStatus.INVALID_YAML);
        }
        return requirement;
    }

    private void setProperties(final Map<String, Object> toscaJson, final Resource resource,
            final Either<Resource, StorageOperationStatus> existingResource) {
        final Map<String, Object> reducedToscaJson = new HashMap<>(toscaJson);
        ImportUtils.removeElementFromJsonMap(reducedToscaJson, "capabilities");
        final Either<Map<String, PropertyDefinition>, ResultStatusEnum> properties = ImportUtils
                .getProperties(reducedToscaJson);
        if (properties.isLeft()) {
            final Map<String, PropertyDefinition> propertyDefinitionMap = properties.left().value();
            if (MapUtils.isNotEmpty(propertyDefinitionMap)) {
                final List<PropertyDefinition> propertiesList = new ArrayList<>();
                for (final Entry<String, PropertyDefinition> entry : propertyDefinitionMap.entrySet()) {
                    addPropertyToList(resource.getName(), propertiesList, entry);
                }
                if (existingResource.isLeft()
                        && CollectionUtils.isNotEmpty(existingResource.left().value().getProperties())) {
                    final List<PropertyDefinition> userCreatedResourceProperties = existingResource.left().value()
                            .getProperties().stream()
                            .filter(PropertyDataDefinition::isUserCreated)
                            .filter(propertyDefinition -> !propertyDefinitionMap
                                    .containsKey(propertyDefinition.getName()))
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(userCreatedResourceProperties)) {
                        propertiesList.addAll(userCreatedResourceProperties);
                    }
                }

                resource.setProperties(propertiesList);
            }
        } else if (properties.right().value() != ResultStatusEnum.ELEMENT_NOT_FOUND) {
            throw new ByActionStatusComponentException(
                    componentsUtils.convertFromResultStatusEnum(properties.right().value(),
                            JsonPresentationFields.PROPERTY));
        }
    }

    private void addPropertyToList(final String resourceName,
            final List<PropertyDefinition> propertiesList,
            final Entry<String, PropertyDefinition> entry) {
        final String propertyName = entry.getKey();
        if (!PROPERTY_NAME_PATTERN_IGNORE_LENGTH.matcher(propertyName).matches()) {
            log.debug("The property with invalid name {} occured upon import resource {}. ", propertyName,
                    resourceName);
            throw new ByActionStatusComponentException(
                    componentsUtils.convertFromResultStatusEnum(ResultStatusEnum.INVALID_PROPERTY_NAME,
                            JsonPresentationFields.PROPERTY));
        }
        final PropertyDefinition propertyDefinition = entry.getValue();
        propertyDefinition.setName(propertyName);
        propertiesList.add(propertyDefinition);
    }

    private void setAttributes(final Map<String, Object> originalToscaJsonMap, final Resource resource) {
        final Map<String, Object> toscaJsonMap = new HashMap<>(originalToscaJsonMap);
        ImportUtils.removeElementFromJsonMap(toscaJsonMap, "capabilities");
        final Either<Map<String, AttributeDefinition>, ResultStatusEnum> getAttributeEither = ImportUtils
                .getAttributes(toscaJsonMap);
        if (getAttributeEither.isRight()) {
            final ResultStatusEnum resultStatus = getAttributeEither.right().value();
            if (resultStatus == ResultStatusEnum.ELEMENT_NOT_FOUND) {
                return;
            }
            throw new ByActionStatusComponentException(
                    componentsUtils.convertFromResultStatusEnum(resultStatus, JsonPresentationFields.ATTRIBUTES));
        }
        final List<AttributeDefinition> attributeDefinitionList = new ArrayList<>();
        final Map<String, AttributeDefinition> attributeMap = getAttributeEither.left().value();
        if (MapUtils.isEmpty(attributeMap)) {
            return;
        }
        for (final Entry<String, AttributeDefinition> entry : attributeMap.entrySet()) {
            final String name = entry.getKey();
            if (!PROPERTY_NAME_PATTERN_IGNORE_LENGTH.matcher(name).matches()) {
                log.debug("Detected attribute with invalid name '{}' during resource '{}' import. ", name,
                        resource.getName());
                throw new ByActionStatusComponentException(
                        componentsUtils.convertFromResultStatusEnum(ResultStatusEnum.INVALID_ATTRIBUTE_NAME,
                                JsonPresentationFields.ATTRIBUTES));
            }
            final AttributeDefinition attributeDefinition = entry.getValue();
            attributeDefinition.setName(name);
            if (attributeDefinition.getEntry_schema() != null
                    && attributeDefinition.getEntry_schema().getType() != null) {
                attributeDefinition.setSchema(new SchemaDefinition());
                attributeDefinition.getSchema().setProperty(new PropertyDataDefinition());
                attributeDefinition.getSchema().getProperty().setType(entry.getValue().getEntry_schema().getType());
            }
            attributeDefinitionList.add(attributeDefinition);
        }
        resource.setAttributes(attributeDefinitionList);
    }

    private Resource setDerivedFrom(Map<String, Object> toscaJson, Resource resource) {

        System.out.println("[populateResourceFromYaml][setDerivedFrom] ENTER");
        System.out.println("[populateResourceFromYaml][setDerivedFrom] resource=" + resource.getName() +
                ", model=" + resource.getModel());

        Either<String, ResultStatusEnum> toscaDerivedFromElement = ImportUtils.findFirstToscaStringElement(toscaJson,
                ToscaTagNamesEnum.DERIVED_FROM);

        if (toscaDerivedFromElement.isLeft()) {
            String derivedFrom = toscaDerivedFromElement.left().value();
            System.out.println("[populateResourceFromYaml][setDerivedFrom] Found derivedFrom=" + derivedFrom);

            resource.setDerivedFrom(Arrays.asList(derivedFrom));
            System.out.println("[populateResourceFromYaml][setDerivedFrom] resource.derivedFrom set.");

            System.out.println("[populateResourceFromYaml][setDerivedFrom] Fetching parent resource by TOSCA name...");
            Either<Resource, StorageOperationStatus> latestByToscaResourceName = toscaOperationFacade
                    .getLatestByToscaResourceName(
                            derivedFrom,
                            resource.getModel());

            if (latestByToscaResourceName.isRight()) {
                StorageOperationStatus operationStatus = latestByToscaResourceName.right().value();
                System.out.println(
                        "[populateResourceFromYaml][setDerivedFrom] Parent fetch failed. status=" + operationStatus);

                if (operationStatus == StorageOperationStatus.NOT_FOUND) {
                    operationStatus = StorageOperationStatus.PARENT_RESOURCE_NOT_FOUND;
                    System.out.println(
                            "[populateResourceFromYaml][setDerivedFrom] Adjusted status to PARENT_RESOURCE_NOT_FOUND");
                }

                ActionStatus converted = componentsUtils.convertFromStorageResponse(operationStatus);

                BeEcompErrorManager.getInstance()
                        .logBeComponentMissingError("Import TOSCA YAML", "resource", derivedFrom);

                System.out.println("[populateResourceFromYaml][setDerivedFrom] THROWING exception for missing parent.");
                throw new ByActionStatusComponentException(converted, derivedFrom);
            }

            Resource parent = latestByToscaResourceName.left().value();
            System.out.println("[populateResourceFromYaml][setDerivedFrom] Parent resource found: " +
                    parent.getName());
            System.out.println("[populateResourceFromYaml][setDerivedFrom] EXIT (parent returned)");
            return parent;

        } else {
            System.out.println("[populateResourceFromYaml][setDerivedFrom] No derivedFrom element found.");
        }

        System.out.println("[populateResourceFromYaml][setDerivedFrom] EXIT (null parent)");
        return null;
    }

    private void setCapabilities(Map<String, Object> toscaJson, Resource resource,
            Resource parentResource) {// Note that parentResource can be null
        Either<Map<String, Object>, ResultStatusEnum> toscaCapabilities = ImportUtils
                .findFirstToscaMapElement(toscaJson, ToscaTagNamesEnum.CAPABILITIES);
        if (toscaCapabilities.isLeft()) {
            Map<String, Object> jsonCapabilities = toscaCapabilities.left().value();
            Map<String, List<CapabilityDefinition>> moduleCapabilities = new HashMap<>();
            Iterator<Entry<String, Object>> capabilitiesNameValue = jsonCapabilities.entrySet().iterator();
            Set<String> capNames = new HashSet<>();
            // Getting flattened list of capabilities of parent node - cap name

            // to cap type
            Map<String, String> capName2TypeMap = getCapName2Type(parentResource);
            while (capabilitiesNameValue.hasNext()) {
                Entry<String, Object> capabilityNameValue = capabilitiesNameValue.next();
                // Validating that no req/cap duplicates exist in imported YAML
                String capNameLowerCase = capabilityNameValue.getKey().toLowerCase();
                if (capNames.contains(capNameLowerCase)) {
                    log.debug(
                            "More than one capability with same name {} (case-insensitive) in imported TOSCA file is invalid",
                            capNameLowerCase);
                    throw new ByActionStatusComponentException(ActionStatus.IMPORT_DUPLICATE_REQ_CAP_NAME, "capability",
                            capNameLowerCase);
                }
                capNames.add(capNameLowerCase);
                CapabilityDefinition capabilityDef = createCapabilityFromImportFile(capabilityNameValue.getValue());
                capabilityDef.setName(capabilityNameValue.getKey());
                if (moduleCapabilities.containsKey(capabilityDef.getType())) {
                    moduleCapabilities.get(capabilityDef.getType()).add(capabilityDef);
                } else {
                    List<CapabilityDefinition> list = new ArrayList<>();
                    list.add(capabilityDef);
                    moduleCapabilities.put(capabilityDef.getType(), list);
                }
                // Validating against req/cap of "derived from" node
                Boolean validateVsParentCap = validateCapNameVsDerived(capName2TypeMap, capabilityDef.getType(),
                        capabilityDef.getName());
                if (!validateVsParentCap) {
                    // Here parentResource is for sure not null, so it's

                    // null-safe

                    // Check added to avoid sonar warning
                    String parentResourceName = parentResource != null ? parentResource.getName() : "";
                    log.debug("Capability with name {} already exists in parent {}", capabilityDef.getName(),
                            parentResourceName);
                    throw new ByActionStatusComponentException(ActionStatus.IMPORT_REQ_CAP_NAME_EXISTS_IN_DERIVED,
                            "capability",
                            capabilityDef.getName().toLowerCase(), parentResourceName);
                }
            }
            if (moduleCapabilities.size() > 0) {
                resource.setCapabilities(moduleCapabilities);
            }
        }
    }

    private Map<String, String> getCapName2Type(Resource parentResource) {
        Map<String, String> capName2type = new HashMap<>();
        if (parentResource != null) {
            Map<String, List<CapabilityDefinition>> capabilities = parentResource.getCapabilities();
            if (capabilities != null) {
                for (List<CapabilityDefinition> capDefinitions : capabilities.values()) {
                    for (CapabilityDefinition capDefinition : capDefinitions) {
                        String nameLowerCase = capDefinition.getName().toLowerCase();
                        if (capName2type.get(nameLowerCase) != null) {
                            String parentResourceName = parentResource.getName();
                            log.debug("Resource with name {} has more than one capability with name {}, ignoring case",
                                    parentResourceName,
                                    nameLowerCase);
                            BeEcompErrorManager.getInstance().logInternalDataError("Import resource",
                                    "Parent resource " + parentResourceName
                                            + " of imported resource has one or more capabilities with name "
                                            + nameLowerCase,
                                    ErrorSeverity.ERROR);
                            throw new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR);
                        }
                        capName2type.put(nameLowerCase, capDefinition.getType());
                    }
                }
            }
        }
        return capName2type;
    }

    private Map<String, String> getReqName2Type(Resource parentResource) {
        Map<String, String> reqName2type = new HashMap<>();
        if (parentResource != null) {
            Map<String, List<RequirementDefinition>> requirements = parentResource.getRequirements();
            if (requirements != null) {
                for (List<RequirementDefinition> reqDefinitions : requirements.values()) {
                    for (RequirementDefinition reqDefinition : reqDefinitions) {
                        String nameLowerCase = reqDefinition.getName().toLowerCase();
                        if (reqName2type.get(nameLowerCase) != null) {
                            String parentResourceName = parentResource.getName();
                            log.debug("Resource with name {} has more than one requirement with name {}, ignoring case",
                                    parentResourceName,
                                    nameLowerCase);
                            BeEcompErrorManager.getInstance().logInternalDataError("Import resource",
                                    "Parent resource " + parentResourceName
                                            + " of imported resource has one or more requirements with name "
                                            + nameLowerCase,
                                    ErrorSeverity.ERROR);
                            throw new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR);
                        }
                        reqName2type.put(nameLowerCase, reqDefinition.getCapability());
                    }
                }
            }
        }
        return reqName2type;
    }

    private Boolean validateCapNameVsDerived(Map<String, String> parentCapName2Type, String childCapabilityType,
            String reqCapName) {
        String capNameLowerCase = reqCapName.toLowerCase();
        log.trace("Validating capability {} vs parent resource", capNameLowerCase);
        String parentCapType = parentCapName2Type.get(capNameLowerCase);
        if (parentCapType != null) {
            if (childCapabilityType.equals(parentCapType)) {
                log.debug(
                        "Capability with name {} is of same type {} for imported resource and its parent - this is OK",
                        capNameLowerCase,
                        childCapabilityType);
                return true;
            }
            Either<Boolean, StorageOperationStatus> capabilityTypeDerivedFrom = capabilityTypeOperation
                    .isCapabilityTypeDerivedFrom(childCapabilityType, parentCapType);
            if (capabilityTypeDerivedFrom.isRight()) {
                log.debug("Couldn't check whether imported resource capability derives from its parent's capability");
                throw new ByActionStatusComponentException(
                        componentsUtils.convertFromStorageResponse(capabilityTypeDerivedFrom.right().value()));
            }
            return capabilityTypeDerivedFrom.left().value();
        }
        return true;
    }

    private CapabilityDefinition createCapabilityFromImportFile(Object capabilityJson) {
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        if (capabilityJson instanceof String) {
            String capabilityJsonString = (String) capabilityJson;
            capabilityDefinition.setType(capabilityJsonString);
        } else if (capabilityJson instanceof Map) {
            Map<String, Object> capabilityJsonMap = (Map<String, Object>) capabilityJson;
            // Type
            if (capabilityJsonMap.containsKey(ToscaTagNamesEnum.TYPE.getElementName())) {
                capabilityDefinition.setType((String) capabilityJsonMap.get(ToscaTagNamesEnum.TYPE.getElementName()));
            }
            // ValidSourceTypes
            if (capabilityJsonMap.containsKey(ToscaTagNamesEnum.VALID_SOURCE_TYPES.getElementName())) {
                capabilityDefinition
                        .setValidSourceTypes((List<String>) capabilityJsonMap
                                .get(ToscaTagNamesEnum.VALID_SOURCE_TYPES.getElementName()));
            }
            // ValidSourceTypes
            if (capabilityJsonMap.containsKey(ToscaTagNamesEnum.DESCRIPTION.getElementName())) {
                capabilityDefinition
                        .setDescription((String) capabilityJsonMap.get(ToscaTagNamesEnum.DESCRIPTION.getElementName()));
            }
            if (capabilityJsonMap.containsKey(ToscaTagNamesEnum.OCCURRENCES.getElementName())) {
                List<Object> occurrencesList = (List) capabilityJsonMap
                        .get(ToscaTagNamesEnum.OCCURRENCES.getElementName());
                validateOccurrences(occurrencesList);
                capabilityDefinition.setMinOccurrences(occurrencesList.get(0).toString());
                capabilityDefinition.setMaxOccurrences(occurrencesList.get(1).toString());
            }
            if (capabilityJsonMap.containsKey(ToscaTagNamesEnum.PROPERTIES.getElementName())) {
                Either<Map<String, PropertyDefinition>, ResultStatusEnum> propertiesRes = ImportUtils
                        .getProperties(capabilityJsonMap);
                if (propertiesRes.isRight()) {
                    throw new ByActionStatusComponentException(ActionStatus.PROPERTY_NOT_FOUND);
                } else {
                    propertiesRes.left().value().entrySet().stream()
                            .forEach(e -> e.getValue().setName(e.getKey().toLowerCase()));
                    List<ComponentInstanceProperty> capabilityProperties = propertiesRes.left().value().values()
                            .stream()
                            .map(p -> new ComponentInstanceProperty(p, p.getDefaultValue(), null))
                            .collect(Collectors.toList());
                    capabilityDefinition.setProperties(capabilityProperties);
                }
            }
        } else if (!(capabilityJson instanceof List)) {
            throw new ByActionStatusComponentException(ActionStatus.INVALID_YAML);
        }
        return capabilityDefinition;
    }

    private void handleImportResourceException(UploadResourceInfo resourceMetaData, User user, boolean isNormative,
            RuntimeException e) {
        ResponseFormat responseFormat;
        ComponentException newException;
        if (e instanceof ComponentException) {
            ComponentException componentException = (ComponentException) e;
            responseFormat = componentException.getResponseFormat();
            if (responseFormat == null) {
                responseFormat = getResponseFormatManager().getResponseFormat(componentException.getActionStatus(),
                        componentException.getParams());
            }
            newException = componentException;
        } else {
            responseFormat = getResponseFormatManager().getResponseFormat(ActionStatus.GENERAL_ERROR);
            newException = new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR);
        }
        String payloadName = (resourceMetaData != null) ? resourceMetaData.getPayloadName() : "";
        BeEcompErrorManager.getInstance().logBeSystemError("Import Resource " + payloadName);
        log.debug("Error when importing resource from payload:{} Exception text: {}", payloadName, e.getMessage(), e);
        auditErrorImport(resourceMetaData, user, responseFormat, isNormative);
        throw newException;
    }

    private void auditErrorImport(UploadResourceInfo resourceMetaData, User user, ResponseFormat errorResponseWrapper,
            boolean isNormative) {
        String version, lifeCycleState;
        if (isNormative) {
            version = TypeUtils.getFirstCertifiedVersionVersion();
            lifeCycleState = LifecycleStateEnum.CERTIFIED.name();
        } else {
            version = "";
            lifeCycleState = LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name();
        }
        String message = "";
        if (errorResponseWrapper.getMessageId() != null) {
            message = errorResponseWrapper.getMessageId() + ": ";
        }
        message += errorResponseWrapper.getFormattedMessage();
        AuditEventFactory factory = new AuditImportResourceAdminEventFactory(
                CommonAuditData.newBuilder().status(errorResponseWrapper.getStatus()).description(message)
                        .requestId(ThreadLocalsHolder.getUuid())
                        .build(),
                new ResourceCommonInfo(resourceMetaData.getName(), ComponentTypeEnum.RESOURCE.getValue()),
                ResourceVersionInfo.newBuilder().state(lifeCycleState).version(version).build(),
                ResourceVersionInfo.newBuilder().state("").version("").build(), "", user, "");
        getAuditingManager().auditEvent(factory);
    }

    private void setResourceMetaData(Resource resource, String resourceYml, UploadResourceInfo resourceMetaData) {

        System.out.println("[setResourceMetaData] ENTER");
        System.out.println("[setResourceMetaData] resourceMetaData.name=" + resourceMetaData.getName());
        System.out.println("[setResourceMetaData] yaml present=" + (resourceYml != null));

        Map<String, Object> ymlObj = new Yaml().load(resourceYml);

        System.out.println("[setResourceMetaData] Extracting TOSCA name...");
        String toscaName = getToscaResourceName(ymlObj);
        System.out.println("[setResourceMetaData] toscaName=" + toscaName);

        System.out.println("[setResourceMetaData] Fetching latest by TOSCA name...");
        final Either<Resource, StorageOperationStatus> latestByToscaName = toscaOperationFacade
                .getLatestByToscaResourceName(toscaName, resourceMetaData.getModel());

        if (latestByToscaName.isLeft() && resourceMetaData instanceof DefaultUploadResourceInfo) {
            System.out
                    .println("[setResourceMetaData] Latest resource found. Applying metadata from existing resource.");
            setMetaDataFromLatestResource(resource, latestByToscaName.left().value());
        } else {
            System.out.println("[setResourceMetaData] No matching latest resource OR not default upload type.");
            System.out.println("[setResourceMetaData] Applying metadata from JSON metadata object.");
            setMetaDataFromJson(resourceMetaData, resource);
        }

        System.out.println("[setResourceMetaData] EXIT");
    }

    private void setMetaDataFromJson(final UploadResourceInfo resourceMetaData, final Resource resource) {
        this.populateResourceMetadata(resourceMetaData, resource);
        resource.setCreatorUserId(resourceMetaData.getContactId());
        final String payloadData = resourceMetaData.getPayloadData();
        if (payloadData != null) {
            resource.setToscaVersion(getToscaVersion(payloadData));
        }
        final List<CategoryDefinition> categories = resourceMetaData.getCategories();
        calculateResourceIsAbstract(resource, categories);
    }

    private void setMetaDataFromLatestResource(Resource resource, Resource latestResource) {
        if (resource != null && latestResource != null) {
            resource.setCreatorUserId(latestResource.getContactId());
            resource.setDescription(latestResource.getDescription());
            resource.setTags(latestResource.getTags());
            resource.setCategories(latestResource.getCategories());
            resource.setContactId(latestResource.getContactId());
            resource.setName(latestResource.getName());
            resource.setIcon(latestResource.getIcon());
            resource.setResourceVendorModelNumber(latestResource.getResourceVendorModelNumber());
            resource.setResourceType(latestResource.getResourceType());
            if (latestResource.getVendorName() != null) {
                resource.setVendorName(latestResource.getVendorName());
            }
            if (latestResource.getVendorRelease() != null) {
                resource.setVendorRelease(latestResource.getVendorRelease());
            }
            if (latestResource.getModel() != null) {
                resource.setModel(latestResource.getModel());
            }
            if (latestResource.getToscaVersion() != null) {
                resource.setToscaVersion(latestResource.getToscaVersion());
            }
            final List<CategoryDefinition> categories = latestResource.getCategories();
            calculateResourceIsAbstract(resource, categories);
        }
    }

    private Map<String, Object> decodePayload(final String payloadData) {
        final String decodedPayload = new String(Base64.decodeBase64(payloadData));
        return (Map<String, Object>) new Yaml().load(decodedPayload);
    }

    private String getToscaVersion(final String payloadData) {
        final Map<String, Object> mappedToscaTemplate = decodePayload(payloadData);
        final Either<String, ResultStatusEnum> findFirstToscaStringElement = ImportUtils
                .findFirstToscaStringElement(mappedToscaTemplate, ToscaTagNamesEnum.TOSCA_VERSION);
        if (findFirstToscaStringElement.isLeft()) {
            return findFirstToscaStringElement.left().value();
        } else {
            return null;
        }
    }

    private void calculateResourceIsAbstract(Resource resource, List<CategoryDefinition> categories) {
        if (categories != null && !categories.isEmpty()) {
            CategoryDefinition categoryDef = categories.get(0);
            resource.setAbstract(false);
            if (categoryDef != null && categoryDef.getName() != null
                    && categoryDef.getName().equals(Constants.ABSTRACT_CATEGORY_NAME)) {
                SubCategoryDefinition subCategoryDef = categoryDef.getSubcategories().get(0);
                if (subCategoryDef != null && subCategoryDef.getName().equals(Constants.ABSTRACT_SUBCATEGORY)) {
                    resource.setAbstract(true);
                }
            }
        }
    }

    private void setConstantMetaData(Resource resource, boolean shouldBeCertified) {
        String version;
        LifecycleStateEnum state;
        if (shouldBeCertified) {
            version = TypeUtils.getFirstCertifiedVersionVersion();
            state = ImportUtils.Constants.NORMATIVE_TYPE_LIFE_CYCLE;
        } else {
            version = ImportUtils.Constants.FIRST_NON_CERTIFIED_VERSION;
            state = ImportUtils.Constants.NORMATIVE_TYPE_LIFE_CYCLE_NOT_CERTIFIED_CHECKOUT;
        }
        resource.setVersion(version);
        resource.setLifecycleState(state);
        resource.setHighestVersion(ImportUtils.Constants.NORMATIVE_TYPE_HIGHEST_VERSION);
        resource.setVendorName(ImportUtils.Constants.VENDOR_NAME);
        resource.setVendorRelease(ImportUtils.Constants.VENDOR_RELEASE);
    }

    private void validateOccurrences(List<Object> occurrensesList) {
        if (!ValidationUtils.validateListNotEmpty(occurrensesList)) {
            log.debug("Occurrenses list empty");
            throw new ByActionStatusComponentException(ActionStatus.INVALID_OCCURRENCES);
        }
        if (occurrensesList.size() < 2) {
            log.debug("Occurrenses list size not 2");
            throw new ByActionStatusComponentException(ActionStatus.INVALID_OCCURRENCES);
        }
        Object minObj = occurrensesList.get(0);
        Object maxObj = occurrensesList.get(1);
        Integer minOccurrences;
        Integer maxOccurrences;
        if (minObj instanceof Integer) {
            minOccurrences = (Integer) minObj;
        } else {
            log.debug("Invalid occurrenses format. low_bound occurrense must be Integer {}", minObj);
            throw new ByActionStatusComponentException(ActionStatus.INVALID_OCCURRENCES);
        }
        if (minOccurrences < 0) {
            log.debug("Invalid occurrenses format.low_bound occurrense negative {}", minOccurrences);
            throw new ByActionStatusComponentException(ActionStatus.INVALID_OCCURRENCES);
        }
        if (maxObj instanceof String) {
            if (!"UNBOUNDED".equals(maxObj)) {
                log.debug("Invalid occurrenses format. Max occurrence is {}", maxObj);
                throw new ByActionStatusComponentException(ActionStatus.INVALID_OCCURRENCES);
            }
        } else {
            if (maxObj instanceof Integer) {
                maxOccurrences = (Integer) maxObj;
            } else {
                log.debug("Invalid occurrenses format.  Max occurrence is {}", maxObj);
                throw new ByActionStatusComponentException(ActionStatus.INVALID_OCCURRENCES);
            }
            if (maxOccurrences < 0 || maxOccurrences < minOccurrences) {
                log.debug("Invalid occurrenses format.  min occurrence is {}, Max occurrence is {}", minOccurrences,
                        maxOccurrences);
                throw new ByActionStatusComponentException(ActionStatus.INVALID_OCCURRENCES);
            }
        }
    }

    public synchronized void init(ServletContext servletContext) {
        if (this.servletContext == null) {
            this.servletContext = servletContext;
            responseFormatManager = ResponseFormatManager.getInstance();
            resourceBusinessLogic = getResourceBL(servletContext);
        }
    }

    public boolean isResourceExist(String resourceName) {
        return resourceBusinessLogic.isResourceExist(resourceName);
    }

    private ResourceBusinessLogic getResourceBL(ServletContext context) {
        WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context
                .getAttribute(org.openecomp.sdc.common.api.Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
        WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
        return webApplicationContext.getBean(ResourceBusinessLogic.class);
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public AuditingManager getAuditingManager() {
        return auditingManager;
    }

    @Autowired
    public void setAuditingManager(AuditingManager auditingManager) {
        this.auditingManager = auditingManager;
    }

    public ResponseFormatManager getResponseFormatManager() {
        return responseFormatManager;
    }

    public void setResponseFormatManager(ResponseFormatManager responseFormatManager) {
        this.responseFormatManager = responseFormatManager;
    }

    public ResourceBusinessLogic getResourceBusinessLogic() {
        return resourceBusinessLogic;
    }

    @Autowired
    public void setResourceBusinessLogic(ResourceBusinessLogic resourceBusinessLogic) {
        this.resourceBusinessLogic = resourceBusinessLogic;
    }

    public IGraphLockOperation getGraphLockOperation() {
        return graphLockOperation;
    }

    @Autowired
    public void setGraphLockOperation(IGraphLockOperation graphLockOperation) {
        this.graphLockOperation = graphLockOperation;
    }

    private List<DataTypeDefinition> extractDataTypeFromJson(final ResourceBusinessLogic resourceBusinessLogic,
            final Map<String, Object> foundElements,
            final String model) {
        final List<DataTypeDefinition> dataTypeDefinitionList = new ArrayList<>();
        if (MapUtils.isNotEmpty(foundElements)) {
            final Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> dataTypeCacheAll = resourceBusinessLogic.applicationDataTypeCache
                    .getAll(model);
            if (dataTypeCacheAll.isLeft()) {
                for (final Entry<String, Object> attributeNameValue : foundElements.entrySet()) {
                    final Object value = attributeNameValue.getValue();
                    if (value instanceof Map) {
                        final DataTypeDefinition dataTypeDefinition = createDataTypeDefinitionWithName(
                                attributeNameValue);
                        final DataTypeDefinition dataTypeDefinitionParent = dataTypeCacheAll.left().value()
                                .get(dataTypeDefinition.getDerivedFromName());
                        dataTypeDefinition.setDerivedFrom(dataTypeDefinitionParent);
                        dataTypeDefinitionList.add(dataTypeDefinition);
                    } else {
                        dataTypeDefinitionList.add(createDataType(String.valueOf(value)));
                    }
                }
            }
        }
        return dataTypeDefinitionList;
    }
}
