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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.PolicyTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.IPolicyTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.PolicyTypeData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("policy-type-operation")
public class PolicyTypeOperation extends AbstractOperation implements IPolicyTypeOperation {

	private static final String CREATE_FLOW_CONTEXT = "CreatePolicyType";
	private static final String GET_FLOW_CONTEXT = "GetPolicyType";

	@Resource
	private PropertyOperation propertyOperation;

	public PolicyTypeOperation() {
		super();
	}

	private static Logger log = LoggerFactory.getLogger(PolicyTypeOperation.class.getName());

	@Override
	public Either<PolicyTypeDefinition, StorageOperationStatus> getLatestPolicyTypeByType(String policyTypeName) {
		return getLatestPolicyTypeByType(policyTypeName, false);
	}

	private Either<PolicyTypeDefinition, StorageOperationStatus> getLatestPolicyTypeByType(String type, boolean inTransaction) {
		Map<String, Object> mapCriteria = new HashMap<>();
		mapCriteria.put(GraphPropertiesDictionary.TYPE.getProperty(), type);
		mapCriteria.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);

		return getPolicyTypeByCriteria(type, mapCriteria, inTransaction);
	}

	@Override
	public Either<PolicyTypeDefinition, StorageOperationStatus> addPolicyType(PolicyTypeDefinition policyType) {
		return addPolicyType(policyType, false);
	}

	@Override
	public Either<PolicyTypeDefinition, StorageOperationStatus> addPolicyType(PolicyTypeDefinition policyTypeDef, boolean inTransaction) {

		Either<PolicyTypeDefinition, StorageOperationStatus> result = null;

		try {

			Either<PolicyTypeData, TitanOperationStatus> eitherStatus = addPolicyTypeToGraph(policyTypeDef);

			if (eitherStatus.isRight()) {
				BeEcompErrorManager.getInstance().logBeFailedCreateNodeError(CREATE_FLOW_CONTEXT, policyTypeDef.getType(), eitherStatus.right().value().name());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(eitherStatus.right().value()));

			} else {
				PolicyTypeData policyTypeData = eitherStatus.left().value();

				String uniqueId = policyTypeData.getUniqueId();
				Either<PolicyTypeDefinition, StorageOperationStatus> policyTypeRes = this.getPolicyType(uniqueId, true);

				if (policyTypeRes.isRight()) {
					BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError(GET_FLOW_CONTEXT, policyTypeDef.getType(), eitherStatus.right().value().name());
				}

				result = policyTypeRes;

			}

			return result;

		} finally {
			handleTransactionCommitRollback(inTransaction, result);
		}

	}

	private Either<PolicyTypeData, TitanOperationStatus> addPolicyTypeToGraph(PolicyTypeDefinition policyTypeDef) {
		log.debug("Got policy type {}", policyTypeDef);

		String ptUniqueId = UniqueIdBuilder.buildPolicyTypeUid(policyTypeDef.getType(), policyTypeDef.getVersion());

		PolicyTypeData policyTypeData = buildPolicyTypeData(policyTypeDef, ptUniqueId);

		log.debug("Before adding policy type to graph. policyTypeData = {}", policyTypeData);

		Either<PolicyTypeData, TitanOperationStatus> eitherPolicyTypeData = titanGenericDao.createNode(policyTypeData, PolicyTypeData.class);
		log.debug("After adding policy type to graph. status is = {}", eitherPolicyTypeData);

		if (eitherPolicyTypeData.isRight()) {
			TitanOperationStatus operationStatus = eitherPolicyTypeData.right().value();
			log.error("Failed to add policy type {} to graph. status is {}", policyTypeDef.getType(), operationStatus);
			return Either.right(operationStatus);
		}

		PolicyTypeData resultCTD = eitherPolicyTypeData.left().value();
		List<PropertyDefinition> properties = policyTypeDef.getProperties();
		Either<Map<String, PropertyData>, TitanOperationStatus> addPropertiesToPolicyType = propertyOperation.addPropertiesToElementType(resultCTD.getUniqueId(), NodeTypeEnum.PolicyType, properties);
		if (addPropertiesToPolicyType.isRight()) {
			log.error("Failed add properties {} to policy {}", properties, policyTypeDef.getType());
			return Either.right(addPropertiesToPolicyType.right().value());
		}

		return Either.left(eitherPolicyTypeData.left().value());
	}

	public Either<PolicyTypeDefinition, StorageOperationStatus> getPolicyTypeByCriteria(String type, Map<String, Object> properties, boolean inTransaction) {
		Either<PolicyTypeDefinition, StorageOperationStatus> result = null;
		try {
			if (type == null || type.isEmpty()) {
				log.error("type is empty");
				result = Either.right(StorageOperationStatus.INVALID_ID);
				return result;
			}

			Either<List<PolicyTypeData>, TitanOperationStatus> eitherPolicyData = titanGenericDao.getByCriteria(NodeTypeEnum.PolicyType, properties, PolicyTypeData.class);
			if (eitherPolicyData.isRight()) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(eitherPolicyData.right().value()));
			} else {
				PolicyTypeDataDefinition dataDefinition = eitherPolicyData.left().value().stream().map(e -> e.getPolicyTypeDataDefinition()).findFirst().get();
				result = getPolicyType(dataDefinition.getUniqueId(), inTransaction);
			}

			return result;

		} finally {
			handleTransactionCommitRollback(inTransaction, result);
		}
	}

	@Override
	public Either<PolicyTypeDefinition, StorageOperationStatus> getPolicyType(String uniqueId, boolean inTransaction) {
		return getElementType(this::getPolicyTypeByUid, uniqueId, inTransaction);
	}

	private Either<PolicyTypeDefinition, TitanOperationStatus> getPolicyTypeByUid(String uniqueId) {
		Either<PolicyTypeDefinition, TitanOperationStatus> result = null;

		Either<PolicyTypeData, TitanOperationStatus> eitherPolicyTypeData = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.PolicyType), uniqueId, PolicyTypeData.class);

		if (eitherPolicyTypeData.isRight()) {
			TitanOperationStatus status = eitherPolicyTypeData.right().value();
			log.debug("Policy type {} cannot be found in graph. status is {}", uniqueId, status);
			return Either.right(status);
		}

		PolicyTypeData policyTypeData = eitherPolicyTypeData.left().value();
		PolicyTypeDefinition policyTypeDefinition = new PolicyTypeDefinition(policyTypeData.getPolicyTypeDataDefinition());

		TitanOperationStatus propertiesStatus = propertyOperation.fillProperties(uniqueId, propList -> policyTypeDefinition.setProperties(propList));
		if (propertiesStatus != TitanOperationStatus.OK) {
			log.error("Failed to fetch properties of policy type {}", uniqueId);
			return Either.right(propertiesStatus);
		}

		result = Either.left(policyTypeDefinition);

		return result;
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

}
