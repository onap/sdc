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
package org.openecomp.sdc.be.components.csar;

import fj.data.Either;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.BaseBusinessLogic;
import org.openecomp.sdc.be.components.impl.CsarValidationUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.ParsedToscaYamlInfo;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.VendorSoftwareProduct;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CsarOperation;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("csarBusinessLogic")
public class CsarBusinessLogic extends BaseBusinessLogic {

    private static final Logger log = Logger.getLogger(CsarBusinessLogic.class);
    private static final String CREATING_RESOURCE_FROM_CSAR_FETCHING_CSAR_WITH_ID = "Creating resource from CSAR: fetching CSAR with id ";
    private static final String FAILED = " failed";
    private final YamlTemplateParsingHandler yamlHandler;
    private CsarOperation csarOperation;

    @Autowired
    public CsarBusinessLogic(IElementOperation elementDao, IGroupOperation groupOperation, IGroupInstanceOperation groupInstanceOperation,
                             IGroupTypeOperation groupTypeOperation, InterfaceOperation interfaceOperation,
                             InterfaceLifecycleOperation interfaceLifecycleTypeOperation, YamlTemplateParsingHandler yamlHandler,
                             ArtifactsOperations artifactToscaOperation) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation,
            artifactToscaOperation);
        this.yamlHandler = yamlHandler;
    }

    @Autowired
    public void setCsarOperation(CsarOperation csarOperation) {
        this.csarOperation = csarOperation;
    }

    public void validateCsarBeforeCreate(Resource resource, AuditingActionEnum auditingAction, User user, String csarUUID) {
        // check if VF with the same Csar UUID or with he same name already

        // exists
        StorageOperationStatus status = toscaOperationFacade.validateCsarUuidUniqueness(csarUUID);
        if (status == StorageOperationStatus.ENTITY_ALREADY_EXISTS) {
            log.debug("Failed to create resource {}, csarUUID {} already exist for a different VF ", resource.getSystemName(), csarUUID);
            auditAndThrowException(resource, user, auditingAction, ActionStatus.VSP_ALREADY_EXISTS, csarUUID);
        } else if (status != StorageOperationStatus.OK) {
            log.debug("Failed to validate uniqueness of CsarUUID {} for resource", csarUUID, resource.getSystemName());
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(status));
        }
    }

    public void validateCsarBeforeCreate(Service resource, String csarUUID) {
        // check if VF with the same Csar UUID or with he same name already

        // exists
        StorageOperationStatus status = toscaOperationFacade.validateCsarUuidUniqueness(csarUUID);
        log.debug("enter validateCsarBeforeCreate,get status:{}", status);
        if (status == StorageOperationStatus.ENTITY_ALREADY_EXISTS) {
            log.debug("Failed to create resource {}, csarUUID {} already exist for a different VF ", resource.getSystemName(), csarUUID);
        } else if (status != StorageOperationStatus.OK) {
            log.debug("Failed to validate uniqueness of CsarUUID '{}' for resource '{}'", csarUUID, resource.getSystemName());
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(status));
        }
    }

    public OnboardedCsarInfo getCsarInfo(Resource resource, Resource oldResource, User user, Map<String, byte[]> payload, String csarUUID) {
        Map<String, byte[]> csar = payload;
        if (csar == null) {
            final var vendorSoftwareProduct = getCsar(resource, user);
            validateModel(resource, vendorSoftwareProduct);

            csar = vendorSoftwareProduct.getFileMap();
        }
        ImmutablePair<String, String> toscaYamlCsarStatus = validateAndParseCsar(resource, user, csar, csarUUID).left()
            .on(this::throwComponentException);
        String checksum = CsarValidationUtils.getToscaYamlChecksum(csar, csarUUID, componentsUtils).left()
            .on(r -> logAndThrowComponentException(r, "Failed to calculate checksum for casrUUID {} error {} ", csarUUID));
        if (oldResource != null && !checksum
            .equals(oldResource.getComponentMetadataDefinition().getMetadataDataDefinition().getImportedToscaChecksum())) {
            log.debug(
                "The checksum of main template yaml of csar with csarUUID {} is not equal to the previous one, existing checksum is {}, new one is {}.",
                csarUUID, oldResource.getComponentMetadataDefinition().getMetadataDataDefinition().getImportedToscaChecksum(), checksum);
            oldResource.getComponentMetadataDefinition().getMetadataDataDefinition().setImportedToscaChecksum(checksum);
        }
        return new OnboardedCsarInfo(user, csarUUID, resource.getCsarVersionId(), csar, resource.getName(), toscaYamlCsarStatus.getKey(),
            toscaYamlCsarStatus.getValue(), true);
    }

    private void validateModel(final Resource resource, final VendorSoftwareProduct vendorSoftwareProduct) {
        if (resource.getModel() == null) {
            if (!vendorSoftwareProduct.getModelList().isEmpty()) {
                var modelStringList = String.join(", ", vendorSoftwareProduct.getModelList());
                throw new ByActionStatusComponentException(ActionStatus.VSP_MODEL_NOT_ALLOWED, "SDC AID", modelStringList);
            }
            return;
        }
        if (!vendorSoftwareProduct.getModelList().contains(resource.getModel())) {
            var modelStringList =
                vendorSoftwareProduct.getModelList().isEmpty() ? "SDC AID" : String.join(", ", vendorSoftwareProduct.getModelList());
            throw new ByActionStatusComponentException(ActionStatus.VSP_MODEL_NOT_ALLOWED, resource.getModel(), modelStringList);
        }
    }

    public ServiceCsarInfo getCsarInfo(Service service, Service oldResource, User user, Map<String, byte[]> payload, String csarUUID) {
        Map<String, byte[]> csar = getCsar(service, user, payload, csarUUID);
        ImmutablePair<String, String> toscaYamlCsarStatus = validateAndParseCsar(service, user, csar, csarUUID).left()
            .on(this::throwComponentException);
        String checksum = CsarValidationUtils.getToscaYamlChecksum(csar, csarUUID, componentsUtils).left()
            .on(r -> logAndThrowComponentException(r, "Failed to calculate checksum for casrUUID {} error {} ", csarUUID));
        if (oldResource != null && !checksum
            .equals(oldResource.getComponentMetadataDefinition().getMetadataDataDefinition().getImportedToscaChecksum())) {
            log.debug(
                "The checksum of main template yaml of csar with csarUUID {} is not equal to the previous one, existing checksum is {}, new one is {}.",
                csarUUID, oldResource.getComponentMetadataDefinition().getMetadataDataDefinition().getImportedToscaChecksum(), checksum);
            oldResource.getComponentMetadataDefinition().getMetadataDataDefinition().setImportedToscaChecksum(checksum);
        }
        return new ServiceCsarInfo(user, csarUUID, csar, service.getName(), toscaYamlCsarStatus.getKey(), toscaYamlCsarStatus.getValue(), true);
    }

    public ParsedToscaYamlInfo getParsedToscaYamlInfo(String topologyTemplateYaml, String yamlName, Map<String, NodeTypeInfo> nodeTypesInfo,
                                                      CsarInfo csarInfo, String nodeName, Component component) {
        return yamlHandler
            .parseResourceInfoFromYAML(yamlName, topologyTemplateYaml, csarInfo.getCreatedNodesToscaResourceNames(), nodeTypesInfo, nodeName,
                component, getInterfaceTemplateYaml(csarInfo).orElse(""));
    }

    public ParsedToscaYamlInfo getParsedToscaYamlInfoMapped(Map<String, Object> mappedTopologyTemplateYaml, String yamlName,
                                                            Map<String, String> createdNodesToscaResourceNames, Component component) {
        return yamlHandler
            .parseResourceInfoFromMappedYAML(yamlName, mappedTopologyTemplateYaml, createdNodesToscaResourceNames, component, "");
    }

    private Optional<String> getInterfaceTemplateYaml(CsarInfo csarInfo) {
        String[] yamlFile;
        String interfaceTemplateYaml = "";
        if (csarInfo.getMainTemplateName().contains(".yml")) {
            yamlFile = csarInfo.getMainTemplateName().split(".yml");
            interfaceTemplateYaml = yamlFile[0] + "-interface.yml";
        } else if (csarInfo.getMainTemplateName().contains(".yaml")) {
            yamlFile = csarInfo.getMainTemplateName().split(".yaml");
            interfaceTemplateYaml = yamlFile[0] + "-interface.yaml";
        }
        if (csarInfo.getCsar().containsKey(interfaceTemplateYaml)) {
            return Optional.of(new String(csarInfo.getCsar().get(interfaceTemplateYaml)));
        }
        return Optional.empty();
    }

    private String logAndThrowComponentException(ResponseFormat responseFormat, String logMessage, String... params) {
        log.debug(logMessage, params, responseFormat);
        throw new ByResponseFormatComponentException(responseFormat);
    }

    private ImmutablePair<String, String> throwComponentException(ResponseFormat responseFormat) {
        throw new ByResponseFormatComponentException(responseFormat);
    }

    private Either<ImmutablePair<String, String>, ResponseFormat> validateAndParseCsar(Component component, User user, Map<String, byte[]> csar,
                                                                                       String csarUUID) {
        Either<Boolean, ResponseFormat> validateCsarStatus = CsarValidationUtils.validateCsar(csar, csarUUID, componentsUtils);
        if (validateCsarStatus.isRight()) {
            ResponseFormat responseFormat = validateCsarStatus.right().value();
            log.debug("Error when validate csar with ID {}, error: {}", csarUUID, responseFormat);
            BeEcompErrorManager.getInstance().logBeDaoSystemError(CREATING_RESOURCE_FROM_CSAR_FETCHING_CSAR_WITH_ID + csarUUID + FAILED);
            if (component instanceof Resource) {
                componentsUtils.auditResource(responseFormat, user, (Resource) component, AuditingActionEnum.CREATE_RESOURCE);
            }
            return Either.right(responseFormat);
        }
        Either<ImmutablePair<String, String>, ResponseFormat> toscaYamlCsarStatus = CsarValidationUtils.getToscaYaml(csar, csarUUID, componentsUtils);
        if (toscaYamlCsarStatus.isRight()) {
            ResponseFormat responseFormat = toscaYamlCsarStatus.right().value();
            log.debug("Error when try to get csar toscayamlFile with csar ID {}, error: {}", csarUUID, responseFormat);
            BeEcompErrorManager.getInstance().logBeDaoSystemError(CREATING_RESOURCE_FROM_CSAR_FETCHING_CSAR_WITH_ID + csarUUID + FAILED);
            if (component instanceof Resource) {
                componentsUtils.auditResource(responseFormat, user, (Resource) component, AuditingActionEnum.CREATE_RESOURCE);
            }
            return Either.right(responseFormat);
        }
        return toscaYamlCsarStatus;
    }

    private Map<String, byte[]> getCsar(Component component, User user, Map<String, byte[]> payload, String csarUUID) {
        if (payload != null) {
            return payload;
        }
        Either<Map<String, byte[]>, StorageOperationStatus> csar = csarOperation.findVspLatestPackage(csarUUID, user);
        if (csar.isRight()) {
            StorageOperationStatus value = csar.right().value();
            log.debug("#getCsar - failed to fetch csar with ID {}, error: {}", csarUUID, value);
            BeEcompErrorManager.getInstance().logBeDaoSystemError(CREATING_RESOURCE_FROM_CSAR_FETCHING_CSAR_WITH_ID + csarUUID + FAILED);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(value), csarUUID);
            if (component instanceof Resource) {
                Resource newResource = (Resource) component;
                componentsUtils.auditResource(responseFormat, user, newResource, AuditingActionEnum.CREATE_RESOURCE);
            }
            throw new StorageException(csar.right().value());
        }
        return csar.left().value();
    }

    private VendorSoftwareProduct getCsar(final Resource resource, final User user) {
        final Optional<VendorSoftwareProduct> vendorSoftwareProductOpt;
        try {
            if (resource.getCsarVersionId() == null) {
                vendorSoftwareProductOpt = csarOperation.findLatestVsp(resource.getCsarUUID(), user);
            } else {
                vendorSoftwareProductOpt = csarOperation.findVsp(resource.getCsarUUID(), resource.getCsarVersionId(), user);
            }
        } catch (final Exception exception) {
            log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, CsarBusinessLogic.class.getName(), exception.getMessage());
            auditGetCsarError(resource, user, resource.getCsarUUID(), StorageOperationStatus.GENERAL_ERROR);
            throw new ByActionStatusComponentException(ActionStatus.VSP_FIND_ERROR, resource.getCsarUUID(), resource.getCsarVersionId());
        }
        if (vendorSoftwareProductOpt.isEmpty()) {
            auditGetCsarError(resource, user, resource.getCsarUUID(), StorageOperationStatus.CSAR_NOT_FOUND);
            throw new ByActionStatusComponentException(ActionStatus.VSP_NOT_FOUND, resource.getCsarUUID(), resource.getCsarVersionId());
        }
        return vendorSoftwareProductOpt.get();
    }

    private void auditGetCsarError(Component component, User user, String csarUUID, StorageOperationStatus storageOperationStatus) {
        log.debug("#getCsar - failed to fetch csar with ID {}, error: {}", csarUUID, storageOperationStatus);
        BeEcompErrorManager.getInstance().logBeDaoSystemError(CREATING_RESOURCE_FROM_CSAR_FETCHING_CSAR_WITH_ID + csarUUID + FAILED);
        var responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(storageOperationStatus), csarUUID);
        if (component instanceof Resource) {
            componentsUtils.auditResource(responseFormat, user, (Resource) component, AuditingActionEnum.CREATE_RESOURCE);
        }
    }

    private void auditAndThrowException(Resource resource, User user, AuditingActionEnum auditingAction, ActionStatus status, String... params) {
        ResponseFormat errorResponse = componentsUtils.getResponseFormat(status, params);
        componentsUtils.auditResource(errorResponse, user, resource, auditingAction);
        throw new ByResponseFormatComponentException(errorResponse, params);
    }

    private Either<ImmutablePair<String, String>, ResponseFormat> validateAndParseCsar(Service service, User user, Map<String, byte[]> payload,
                                                                                       String csarUUID) {
        Map<String, byte[]> csar = getCsar(service, user, payload, csarUUID);
        Either<Boolean, ResponseFormat> validateCsarStatus = CsarValidationUtils.validateCsar(csar, csarUUID, componentsUtils);
        if (validateCsarStatus.isRight()) {
            ResponseFormat responseFormat = validateCsarStatus.right().value();
            log.debug("Error when validate csar with ID {}, error: {}", csarUUID, responseFormat);
            BeEcompErrorManager.getInstance().logBeDaoSystemError(CREATING_RESOURCE_FROM_CSAR_FETCHING_CSAR_WITH_ID + csarUUID + FAILED);
            return Either.right(responseFormat);
        }
        Either<ImmutablePair<String, String>, ResponseFormat> toscaYamlCsarStatus = CsarValidationUtils.getToscaYaml(csar, csarUUID, componentsUtils);
        if (toscaYamlCsarStatus.isRight()) {
            ResponseFormat responseFormat = toscaYamlCsarStatus.right().value();
            log.debug("Error when try to get csar toscayamlFile with csar ID {}, error: {}", csarUUID, responseFormat);
            BeEcompErrorManager.getInstance().logBeDaoSystemError(CREATING_RESOURCE_FROM_CSAR_FETCHING_CSAR_WITH_ID + csarUUID + FAILED);
            return Either.right(responseFormat);
        }
        return toscaYamlCsarStatus;
    }
}
