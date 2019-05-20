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

package org.openecomp.sdc.be.model.operations.impl;

import org.janusgraph.graphdb.query.JanusGraphPredicate;
import fj.data.Either;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.elements.PolicyTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.DerivedFromOperation;
import org.openecomp.sdc.be.model.operations.api.IPolicyTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.PolicyTypeData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.openecomp.sdc.be.dao.janusgraph.JanusGraphUtils.buildNotInPredicate;

@Component("policy-type-operation")
public class PolicyTypeOperation extends AbstractOperation implements IPolicyTypeOperation {

    private static final Logger log = Logger.getLogger(PolicyTypeOperation.class.getName());
    private static final String CREATE_FLOW_CONTEXT = "CreatePolicyType";
    private static final String GET_FLOW_CONTEXT = "GetPolicyType";

    @Autowired
    private PropertyOperation propertyOperation;
    @Autowired
    private DerivedFromOperation derivedFromOperation;
    @Autowired
    private OperationUtils operationUtils;

    @Override
    public Either<PolicyTypeDefinition, StorageOperationStatus> getLatestPolicyTypeByType(String type) {
        Map<String, Object> mapCriteria = new HashMap<>();
        mapCriteria.put(GraphPropertiesDictionary.TYPE.getProperty(), type);
        mapCriteria.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);
        return getPolicyTypeByCriteria(type, mapCriteria);
    }

    @Override
    public Either<PolicyTypeDefinition, StorageOperationStatus> addPolicyType(PolicyTypeDefinition policyTypeDef) {
        Either<PolicyTypeDefinition, StorageOperationStatus> result;
        Either<PolicyTypeData, StorageOperationStatus> eitherStatus = addPolicyTypeToGraph(policyTypeDef);
        if (eitherStatus.isRight()) {
            BeEcompErrorManager.getInstance().logBeFailedCreateNodeError(CREATE_FLOW_CONTEXT, policyTypeDef.getType(), eitherStatus.right().value().name());
            result = Either.right(eitherStatus.right().value());
        } else {
            PolicyTypeData policyTypeData = eitherStatus.left().value();
            String uniqueId = policyTypeData.getUniqueId();
            Either<PolicyTypeDefinition, StorageOperationStatus> policyTypeRes = this.getPolicyTypeByUid(uniqueId);

            if (policyTypeRes.isRight()) {
                BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError(GET_FLOW_CONTEXT, policyTypeDef.getType(), eitherStatus.right().value().name());
            }
            result = policyTypeRes;
        }
        return result;
    }

    @Override
    public Either<PolicyTypeDefinition, StorageOperationStatus> updatePolicyType(PolicyTypeDefinition updatedPolicyType, PolicyTypeDefinition currPolicyType) {
        log.debug("updating policy type {}", updatedPolicyType.getType());
        return updatePolicyTypeOnGraph(updatedPolicyType, currPolicyType);
    }

    @Override
    public List<PolicyTypeDefinition> getAllPolicyTypes(Set<String> excludedPolicyTypes) {
        Map<String, Map.Entry<JanusGraphPredicate, Object>> predicateCriteria = buildNotInPredicate(GraphPropertiesDictionary.TYPE.getProperty(), excludedPolicyTypes);
        return janusGraphGenericDao
            .getByCriteriaWithPredicate(NodeTypeEnum.PolicyType, predicateCriteria, PolicyTypeData.class)
                .left()
                .map(this::convertPolicyTypesToDefinition)
                .left()
                .on(operationUtils::onJanusGraphOperationFailure);
    }

    private List<PolicyTypeDefinition> convertPolicyTypesToDefinition(List<PolicyTypeData> policiesTypes) {
        return policiesTypes.stream().map(type -> new PolicyTypeDefinition(type.getPolicyTypeDataDefinition())).collect(Collectors.toList());
    }


    private Either<PolicyTypeData, StorageOperationStatus> addPolicyTypeToGraph(PolicyTypeDefinition policyTypeDef) {
        log.debug("Got policy type {}", policyTypeDef);

        String ptUniqueId = UniqueIdBuilder.buildPolicyTypeUid(policyTypeDef.getType(), policyTypeDef.getVersion(), "policytype");
        PolicyTypeData policyTypeData = buildPolicyTypeData(policyTypeDef, ptUniqueId);
        log.debug("Before adding policy type to graph. policyTypeData = {}", policyTypeData);
        Either<PolicyTypeData, JanusGraphOperationStatus> eitherPolicyTypeData = janusGraphGenericDao
            .createNode(policyTypeData, PolicyTypeData.class);
        log.debug("After adding policy type to graph. status is = {}", eitherPolicyTypeData);
        if (eitherPolicyTypeData.isRight()) {
            JanusGraphOperationStatus operationStatus = eitherPolicyTypeData.right().value();
            log.error("Failed to add policy type {} to graph. status is {}", policyTypeDef.getType(), operationStatus);
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(operationStatus));
        }
        List<PropertyDefinition> properties = policyTypeDef.getProperties();
        Either<Map<String, PropertyData>, JanusGraphOperationStatus> addPropertiesToPolicyType = propertyOperation.addPropertiesToElementType(ptUniqueId, NodeTypeEnum.PolicyType, properties);
        if (addPropertiesToPolicyType.isRight()) {
            log.error("Failed add properties {} to policy {}", properties, policyTypeDef.getType());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(addPropertiesToPolicyType.right().value()));
        }
        return addDerivedFromRelation(policyTypeDef, ptUniqueId)
            .left()
            .map(updatedDerivedFrom -> eitherPolicyTypeData.left().value());
    }

    private Either<PolicyTypeDefinition, StorageOperationStatus> getPolicyTypeByCriteria(String type, Map<String, Object> properties) {
        Either<PolicyTypeDefinition, StorageOperationStatus> result;
        if (type == null || type.isEmpty()) {
            log.error("type is empty");
            result = Either.right(StorageOperationStatus.INVALID_ID);
            return result;
        }

        Either<List<PolicyTypeData>, JanusGraphOperationStatus> eitherPolicyData = janusGraphGenericDao
            .getByCriteria(NodeTypeEnum.PolicyType, properties, PolicyTypeData.class);
        if (eitherPolicyData.isRight()) {
            result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(eitherPolicyData.right().value()));
        } else {
            PolicyTypeDataDefinition dataDefinition = eitherPolicyData.left().value().stream().map(PolicyTypeData::getPolicyTypeDataDefinition).findFirst().get();
            result = getPolicyTypeByUid(dataDefinition.getUniqueId());
        }
        return result;

    }

    private Either<PolicyTypeDefinition, StorageOperationStatus> getPolicyTypeByUid(String uniqueId) {
        log.debug("#getPolicyTypeByUid - fetching policy type with id {}", uniqueId);
        return janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.PolicyType), uniqueId, PolicyTypeData.class)
                .right()
                .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus)
                .left()
                .bind(policyType -> createPolicyTypeDefinition(uniqueId, policyType));
    }

    private Either<PolicyTypeDefinition, StorageOperationStatus> createPolicyTypeDefinition(String uniqueId, PolicyTypeData policyTypeNode) {
        PolicyTypeDefinition policyType = new PolicyTypeDefinition(policyTypeNode.getPolicyTypeDataDefinition());
        return fillDerivedFrom(uniqueId, policyType)
                .left()
                .map(derivedFrom -> fillProperties(uniqueId, policyType, derivedFrom))
                .left()
                .map(props -> policyType);
    }

    private Either<List<PropertyDefinition>, StorageOperationStatus> fillProperties(String uniqueId, PolicyTypeDefinition policyType, PolicyTypeData derivedFromNode) {
        log.debug("#fillProperties - fetching all properties for policy type {}", policyType.getType());
        return propertyOperation.findPropertiesOfNode(NodeTypeEnum.PolicyType, uniqueId)
                .right()
                .bind(this::handlePolicyTypeHasNoProperties)
                .left()
                .bind(propsMap -> fillDerivedFromProperties(policyType, derivedFromNode, new ArrayList<>(propsMap.values())));
    }

    private Either<List<PropertyDefinition>, StorageOperationStatus> fillDerivedFromProperties(PolicyTypeDefinition policyType, PolicyTypeData derivedFromNode, List<PropertyDefinition> policyTypeDirectProperties) {
        if (derivedFromNode == null) {
            policyType.setProperties(policyTypeDirectProperties);
            return Either.left(policyTypeDirectProperties);
        }
        log.debug("#fillDerivedFromProperties - fetching all properties of derived from chain for policy type {}", policyType.getType());
        return propertyOperation.getAllPropertiesRec(derivedFromNode.getUniqueId(), NodeTypeEnum.PolicyType, PolicyTypeData.class)
                .left()
                .map(derivedFromProps -> {policyTypeDirectProperties.addAll(derivedFromProps); return policyTypeDirectProperties;})
                .left()
                .map(allProps -> {policyType.setProperties(allProps);return allProps;});
    }

    private Either<PolicyTypeData, StorageOperationStatus> fillDerivedFrom(String uniqueId, PolicyTypeDefinition policyType) {
        log.debug("#fillDerivedFrom - fetching policy type {} derived node", policyType.getType());
        return derivedFromOperation.getDerivedFromChild(uniqueId, NodeTypeEnum.PolicyType, PolicyTypeData.class)
                .right()
                .bind(this::handleDerivedFromNotExist)
                .left()
                .map(derivedFrom -> setDerivedFrom(policyType, derivedFrom));

    }

    private Either<PolicyTypeData, StorageOperationStatus> handleDerivedFromNotExist(StorageOperationStatus err) {
        if (err == StorageOperationStatus.NOT_FOUND) {
            return Either.left(null);
        }
        return Either.right(err);
    }

    Either<Map<String, PropertyDefinition>, StorageOperationStatus> handlePolicyTypeHasNoProperties(JanusGraphOperationStatus err) {
        if (err == JanusGraphOperationStatus.NOT_FOUND) {
            return Either.left(new HashMap<>());
        }
        return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(err));
    }

    private PolicyTypeData setDerivedFrom(PolicyTypeDefinition policyTypeDefinition, PolicyTypeData derivedFrom) {
        if (derivedFrom != null) {
            policyTypeDefinition.setDerivedFrom(derivedFrom.getPolicyTypeDataDefinition().getType());
        }
        return derivedFrom;
    }

    private PolicyTypeData buildPolicyTypeData(PolicyTypeDefinition policyTypeDefinition, String ptUniqueId) {

        PolicyTypeData policyTypeData = new PolicyTypeData(policyTypeDefinition);

        policyTypeData.getPolicyTypeDataDefinition().setUniqueId(ptUniqueId);
        Long creationDate = policyTypeData.getPolicyTypeDataDefinition().getCreationTime();
        if (creationDate == null) {
            creationDate = System.currentTimeMillis();
        }

        policyTypeData.getPolicyTypeDataDefinition().setCreationTime(creationDate);
        policyTypeData.getPolicyTypeDataDefinition().setModificationTime(creationDate);
        return policyTypeData;
    }

    private Either<PolicyTypeDefinition, StorageOperationStatus> updatePolicyTypeOnGraph(PolicyTypeDefinition updatedPolicyType, PolicyTypeDefinition currPolicyType) {
        updatePolicyTypeData(updatedPolicyType, currPolicyType);
        return janusGraphGenericDao.updateNode(new PolicyTypeData(updatedPolicyType), PolicyTypeData.class)
                .right()
                .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus)
                .left()
                .bind(updatedNode -> updatePolicyProperties(updatedPolicyType.getUniqueId(), updatedPolicyType.getProperties()))
                .left()
                .bind(updatedProperties -> updatePolicyDerivedFrom(updatedPolicyType, currPolicyType.getDerivedFrom()))
                .left()
                .map(updatedDerivedFrom -> updatedPolicyType);
    }

    private Either<Map<String, PropertyData>, StorageOperationStatus> updatePolicyProperties(String policyId, List<PropertyDefinition> properties) {
        log.debug("#updatePolicyProperties - updating policy type properties for policy type with id {}", policyId);
        return propertyOperation.deletePropertiesAssociatedToNode(NodeTypeEnum.PolicyType, policyId)
                .left()
                .bind(deleteProps -> addPropertiesToPolicy(policyId, properties));
    }

    private Either<GraphRelation, StorageOperationStatus> updatePolicyDerivedFrom(PolicyTypeDefinition updatedPolicyType, String currDerivedFromPolicyType) {
        String policyTypeId = updatedPolicyType.getUniqueId();
        log.debug("#updatePolicyDerivedFrom - updating policy derived from relation for policy type with id {}. old derived type {}. new derived type {}", policyTypeId, currDerivedFromPolicyType, updatedPolicyType.getDerivedFrom());
        StorageOperationStatus deleteDerivedRelationStatus = deleteDerivedFromPolicyType(policyTypeId, currDerivedFromPolicyType);
        if (deleteDerivedRelationStatus != StorageOperationStatus.OK) {
            return Either.right(deleteDerivedRelationStatus);
        }
        return addDerivedFromRelation(updatedPolicyType, policyTypeId);
    }

    private Either<GraphRelation, StorageOperationStatus> addDerivedFromRelation(PolicyTypeDataDefinition policyTypeDef, String ptUniqueId) {
        String derivedFrom = policyTypeDef.getDerivedFrom();
        if (derivedFrom == null) {
            return Either.left(null);
        }
        log.debug("#addDerivedFromRelationBefore - adding derived from relation between policy type {} to its parent {}", policyTypeDef.getType(), derivedFrom);
        return this.getLatestPolicyTypeByType(derivedFrom)
                .left()
                .bind(derivedFromPolicy -> derivedFromOperation.addDerivedFromRelation(ptUniqueId, derivedFromPolicy.getUniqueId(), NodeTypeEnum.PolicyType));
    }

    private StorageOperationStatus deleteDerivedFromPolicyType(String policyTypeId, String derivedFromType) {
        if (derivedFromType == null) {
            return StorageOperationStatus.OK;
        }
        log.debug("#deleteDerivedFromPolicyType - deleting derivedFrom relation for policy type with id {} and its derived type {}", policyTypeId, derivedFromType);
        return getLatestPolicyTypeByType(derivedFromType)
                .either(derivedFromNode -> derivedFromOperation.removeDerivedFromRelation(policyTypeId, derivedFromNode.getUniqueId(), NodeTypeEnum.PolicyType),
                        err -> err);
    }

    private  Either<Map<String, PropertyData>, StorageOperationStatus> addPropertiesToPolicy(String policyTypeId, List<PropertyDefinition> properties) {
        log.debug("#addPropertiesToPolicy - adding policy type properties for policy type with id {}", policyTypeId);
        return propertyOperation.addPropertiesToElementType(policyTypeId, NodeTypeEnum.PolicyType, properties)
                .right()
                .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
    }

    private void updatePolicyTypeData(PolicyTypeDefinition updatedTypeDefinition, PolicyTypeDefinition currTypeDefinition) {
        updatedTypeDefinition.setUniqueId(currTypeDefinition.getUniqueId());
        updatedTypeDefinition.setCreationTime(currTypeDefinition.getCreationTime());
        updatedTypeDefinition.setModificationTime(System.currentTimeMillis());
    }

}
