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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.BaseBusinessLogic;
import org.openecomp.sdc.be.components.impl.CsarValidationUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.ParsedToscaYamlInfo;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CsarOperation;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@org.springframework.stereotype.Component("csarBusinessLogic")
public class CsarBusinessLogic extends BaseBusinessLogic {

    private static final Logger log = Logger.getLogger(CsarBusinessLogic.class);

    private static final String CREATING_RESOURCE_FROM_CSAR_FETCHING_CSAR_WITH_ID = "Creating resource from CSAR: fetching CSAR with id ";
    private static final String FAILED = " failed";

    @Autowired
    private CsarOperation csarOperation;

    @Autowired
    private YamlTemplateParsingHandler yamlHandler;

    public void setCsarOperation(CsarOperation csarOperation) {
        this.csarOperation = csarOperation;
    }

    public void validateCsarBeforeCreate(Resource resource, AuditingActionEnum auditingAction, User user, String csarUUID) {
        // check if VF with the same Csar UUID or with he same name already
        // exists
        StorageOperationStatus status = toscaOperationFacade.validateCsarUuidUniqueness(csarUUID);
        if(status == StorageOperationStatus.ENTITY_ALREADY_EXISTS){
            log.debug("Failed to create resource {}, csarUUID {} already exist for a different VF ",
                    resource.getSystemName(), csarUUID);
            auditAndThrowException(resource, user, auditingAction, ActionStatus.VSP_ALREADY_EXISTS,
                    csarUUID);
        } else if (status != StorageOperationStatus.OK) {
            log.debug("Failed to validate uniqueness of CsarUUID {} for resource", csarUUID,
                    resource.getSystemName());
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(status));
        }
    }

    public CsarInfo getCsarInfo(Resource resource, Resource oldResource,User user, Map<String, byte[]> payload, String csarUUID){
        Map<String, byte[]> csar = getCsar(resource, user, payload, csarUUID);
        ImmutablePair<String, String> toscaYamlCsarStatus = validateAndParseCsar(resource,
                user, csar, csarUUID)
                .left().on(this::throwComponentException);

        String checksum = CsarValidationUtils.getToscaYamlChecksum(csar,
                csarUUID, componentsUtils).left().on(r->logAndThrowComponentException(r, "Failed to calculate checksum for casrUUID {} error {} ", csarUUID));
        if (oldResource!=null && !checksum.equals(
                oldResource.getComponentMetadataDefinition().getMetadataDataDefinition().getImportedToscaChecksum())) {
            log.debug("The checksum of main template yaml of csar with csarUUID {} is not equal to the previous one, existing checksum is {}, new one is {}.", csarUUID,
                    oldResource.getComponentMetadataDefinition().getMetadataDataDefinition()
                            .getImportedToscaChecksum(),
                    checksum);
            oldResource.getComponentMetadataDefinition().getMetadataDataDefinition()
                    .setImportedToscaChecksum(checksum);
        }

        return new CsarInfo(user, csarUUID, csar, resource.getName(),
                toscaYamlCsarStatus.getKey(), toscaYamlCsarStatus.getValue(), true);
    }


    public ParsedToscaYamlInfo getParsedToscaYamlInfo(String topologyTemplateYaml, String yamlName, Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo, String nodeName) {
        return yamlHandler.parseResourceInfoFromYAML(
                yamlName, topologyTemplateYaml, csarInfo.getCreatedNodesToscaResourceNames(), nodeTypesInfo,
                nodeName);
    }

    private String logAndThrowComponentException(ResponseFormat responseFormat, String logMessage, String ...params) {
        log.debug(logMessage, params, responseFormat);
        throw new ByResponseFormatComponentException(responseFormat);
    }

    private ImmutablePair<String,String> throwComponentException(ResponseFormat responseFormat) {
        throw new ByResponseFormatComponentException(responseFormat);
    }

    private Either<ImmutablePair<String, String>, ResponseFormat> validateAndParseCsar(Resource resource, User user,
                                                                                      Map<String, byte[]> payload, String csarUUID) {
        Map<String, byte[]> csar = getCsar(resource, user, payload, csarUUID);
        Either<Boolean, ResponseFormat> validateCsarStatus = CsarValidationUtils.validateCsar(csar,
                csarUUID, componentsUtils);
        if (validateCsarStatus.isRight()) {
            ResponseFormat responseFormat = validateCsarStatus.right().value();
            log.debug("Error when validate csar with ID {}, error: {}", csarUUID, responseFormat);
            BeEcompErrorManager.getInstance()
                    .logBeDaoSystemError(CREATING_RESOURCE_FROM_CSAR_FETCHING_CSAR_WITH_ID + csarUUID + FAILED);
            componentsUtils.auditResource(responseFormat, user, resource, AuditingActionEnum.CREATE_RESOURCE);
            return Either.right(responseFormat);
        }

        Either<ImmutablePair<String, String>, ResponseFormat> toscaYamlCsarStatus = CsarValidationUtils
                .getToscaYaml(csar, csarUUID, componentsUtils);

        if (toscaYamlCsarStatus.isRight()) {
            ResponseFormat responseFormat = toscaYamlCsarStatus.right().value();
            log.debug("Error when try to get csar toscayamlFile with csar ID {}, error: {}", csarUUID, responseFormat);
            BeEcompErrorManager.getInstance()
                    .logBeDaoSystemError(CREATING_RESOURCE_FROM_CSAR_FETCHING_CSAR_WITH_ID + csarUUID + FAILED);
            componentsUtils.auditResource(responseFormat, user, resource, AuditingActionEnum.CREATE_RESOURCE);
            return Either.right(responseFormat);
        }
        return toscaYamlCsarStatus;
    }

    private Map<String, byte[]> getCsar(Resource resource, User user, Map<String, byte[]> payload, String csarUUID) {
        if (payload != null) {
            return payload;
        }
        Either<Map<String, byte[]>, StorageOperationStatus> csar = csarOperation.getCsar(csarUUID, user);
        if (csar.isRight()) {
            StorageOperationStatus value = csar.right().value();
            log.debug("#getCsar - failed to fetch csar with ID {}, error: {}", csarUUID, value);
            BeEcompErrorManager.getInstance()
                    .logBeDaoSystemError(CREATING_RESOURCE_FROM_CSAR_FETCHING_CSAR_WITH_ID + csarUUID + FAILED);
            ResponseFormat responseFormat = componentsUtils
                    .getResponseFormat(componentsUtils.convertFromStorageResponse(value), csarUUID);
            componentsUtils.auditResource(responseFormat, user, resource, AuditingActionEnum.CREATE_RESOURCE);
            throw new StorageException(csar.right().value());
        }
        return csar.left().value();
    }

    private void auditAndThrowException(Resource resource, User user, AuditingActionEnum auditingAction, ActionStatus status, String... params){
        ResponseFormat errorResponse = componentsUtils.getResponseFormat(status, params);
        componentsUtils.auditResource(errorResponse, user, resource, auditingAction);
        throw new ByResponseFormatComponentException(errorResponse);
    }
}
