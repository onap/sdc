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

import fj.data.Either;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.CommonImportManager.ElementTypeEnum;
import org.openecomp.sdc.be.components.impl.model.ToscaTypeImportData;
import org.openecomp.sdc.be.components.impl.utils.PolicyTypeImportUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.Model;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IPolicyTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GroupOperation;
import org.openecomp.sdc.be.model.operations.impl.GroupTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;

@Component("policyTypeImportManager")
public class PolicyTypeImportManager {

    private final IPolicyTypeOperation policyTypeOperation;
    private final ComponentsUtils componentsUtils;
    private final GroupOperation groupOperation;
    private final ToscaOperationFacade toscaOperationFacade;
    private final CommonImportManager commonImportManager;
    private final GroupTypeOperation groupTypeOperation;
    private final ModelOperation modelOperation;

    public PolicyTypeImportManager(IPolicyTypeOperation policyTypeOperation, ComponentsUtils componentsUtils, GroupOperation groupOperation,
                                   ToscaOperationFacade toscaOperationFacade, CommonImportManager commonImportManager,
                                   GroupTypeOperation groupTypeOperation, ModelOperation modelOperation) {
        this.policyTypeOperation = policyTypeOperation;
        this.componentsUtils = componentsUtils;
        this.groupOperation = groupOperation;
        this.toscaOperationFacade = toscaOperationFacade;
        this.commonImportManager = commonImportManager;
        this.groupTypeOperation = groupTypeOperation;
        this.modelOperation = modelOperation;
    }

    public Either<List<ImmutablePair<PolicyTypeDefinition, Boolean>>, ResponseFormat> createPolicyTypes(final ToscaTypeImportData toscaTypeImportData,
                                                                                                        final String modelName,
                                                                                                        final boolean includeToModelDefaultImports) {
        final Either<List<ImmutablePair<PolicyTypeDefinition, Boolean>>, ResponseFormat> elementTypes = commonImportManager.createElementTypes(
            toscaTypeImportData, this::createPolicyTypesFromYml, this::upsertPolicyTypesByDao, modelName);
        if (includeToModelDefaultImports && StringUtils.isNotEmpty(modelName)) {
            commonImportManager.addTypesToDefaultImports(toscaTypeImportData.getToscaTypesYml(), modelName);
        }
        return elementTypes;
    }

    private Either<List<PolicyTypeDefinition>, ActionStatus> createPolicyTypesFromYml(String policyTypesYml, String modelName) {
        Either<List<PolicyTypeDefinition>, ActionStatus> policyTypes = commonImportManager.createElementTypesFromYml(policyTypesYml,
            this::createPolicyType);
        if (policyTypes.isLeft() && StringUtils.isNotEmpty(modelName)) {
            final Optional<Model> modelOptional = modelOperation.findModelByName(modelName);
            if (modelOptional.isPresent()) {
                policyTypes.left().value().forEach(policyType -> policyType.setModel(modelName));
                return policyTypes;
            }
            return Either.right(ActionStatus.INVALID_MODEL);
        }
        return policyTypes;
    }

    private Either<List<ImmutablePair<PolicyTypeDefinition, Boolean>>, ResponseFormat> upsertPolicyTypesByDao(
        List<PolicyTypeDefinition> policyTypesToCreate, String modelName) {
        return commonImportManager.createElementTypesWithVersionByDao(policyTypesToCreate, this::validatePolicyType,
            policyType -> new ImmutablePair<>(ElementTypeEnum.POLICY_TYPE, UniqueIdBuilder.buildPolicyTypeUid(policyType.getModel(),
                policyType.getType(), policyType.getVersion(), NodeTypeEnum.PolicyType.getName()).toLowerCase()),
            policyTypeOperation::getLatestPolicyTypeByType,
            policyTypeOperation::addPolicyType, this::updatePolicyType, modelName);
    }

    private Either<PolicyTypeDefinition, StorageOperationStatus> updatePolicyType(PolicyTypeDefinition newPolicyType,
                                                                                  PolicyTypeDefinition oldPolicyType) {
        if (PolicyTypeImportUtils.isPolicyTypesEquals(newPolicyType, oldPolicyType)) {
            return policyTypeAlreadyExists();
        }
        return policyTypeOperation.updatePolicyType(newPolicyType, oldPolicyType);
    }

    private Either<PolicyTypeDefinition, StorageOperationStatus> policyTypeAlreadyExists() {
        return Either.right(StorageOperationStatus.OK);
    }

    private Either<ActionStatus, ResponseFormat> validatePolicyType(PolicyTypeDefinition policyType) {
        Either<ActionStatus, ResponseFormat> result = Either.left(ActionStatus.OK);
        if (policyType.getTargets() != null) {
            if (policyType.getTargets().isEmpty()) {
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.TARGETS_EMPTY, policyType.getType());
                result = Either.right(responseFormat);
            }
            if (result.isLeft()) {
                for (String targetId : policyType.getTargets()) {
                    boolean isValid = toscaOperationFacade.getLatestByToscaResourceName(targetId, policyType.getModel()).isLeft();
                    if (!isValid) { // check if it is a groupType
                        final Either<GroupTypeDefinition, StorageOperationStatus> groupTypeFound = groupTypeOperation
                            .getLatestGroupTypeByType(targetId, policyType.getModel(), false);
                        isValid = groupTypeFound.isLeft() && !groupTypeFound.left().value().isEmpty();
                    }
                    if (!isValid) {
                        isValid = groupOperation.isGroupExist(targetId, false);
                    }
                    if (!isValid) {
                        ResponseFormat responseFormat = componentsUtils
                            .getResponseFormat(ActionStatus.TARGETS_NON_VALID, policyType.getType(), targetId);
                        result = Either.right(responseFormat);
                        break;
                    }
                }
            }
        }
        return result;
    }

    private PolicyTypeDefinition createPolicyType(String groupTypeName, Map<String, Object> toscaJson) {
        PolicyTypeDefinition policyType = new PolicyTypeDefinition();
        if (toscaJson != null) {
            // Description
            final Consumer<String> descriptionSetter = policyType::setDescription;
            commonImportManager.setField(toscaJson, TypeUtils.ToscaTagNamesEnum.DESCRIPTION.getElementName(), descriptionSetter);
            // Derived From
            final Consumer<String> derivedFromSetter = policyType::setDerivedFrom;
            commonImportManager.setField(toscaJson, TypeUtils.ToscaTagNamesEnum.DERIVED_FROM.getElementName(), derivedFromSetter);
            // Properties
            CommonImportManager.setProperties(toscaJson, policyType::setProperties);
            // Metadata
            final Consumer<Map<String, String>> metadataSetter = policyType::setMetadata;
            commonImportManager.setField(toscaJson, TypeUtils.ToscaTagNamesEnum.METADATA.getElementName(), metadataSetter);
            // Targets
            final Consumer<List<String>> targetsSetter = policyType::setTargets;
            commonImportManager.setField(toscaJson, TypeUtils.ToscaTagNamesEnum.TARGETS.getElementName(), targetsSetter);
            policyType.setType(groupTypeName);
            policyType.setHighestVersion(true);
            policyType.setVersion(TypeUtils.getFirstCertifiedVersionVersion());
        }
        return policyType;
    }
}
