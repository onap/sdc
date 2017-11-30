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
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.ICapabilityOperation;
import org.openecomp.sdc.be.resources.data.CapabilityTypeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("capability-operation")
public class CapabilityOperation extends AbstractOperation implements ICapabilityOperation {

	public CapabilityOperation() {
		super();
	}

	private static Logger log = LoggerFactory.getLogger(CapabilityOperation.class.getName());

	@Autowired
	private PropertyOperation propertyOperation;

	@Autowired
	private TitanGenericDao titanGenericDao;

	/**
	 * FOR TEST ONLY
	 * 
	 * @param titanGenericDao
	 */
	public void setTitanGenericDao(TitanGenericDao titanGenericDao) {
		this.titanGenericDao = titanGenericDao;
	}


	@Override
	public Either<List<PropertyDefinition>, TitanOperationStatus> validatePropertyUniqueness(Map<String, PropertyDefinition> propertiesOfCapabilityType, List<PropertyDefinition> properties) {
		Either<List<PropertyDefinition>, TitanOperationStatus> result = Either.left(properties);

		for (PropertyDefinition property : properties) {
			String propertyName = property.getName();
			String propertyType = property.getType();
			PropertyDefinition defaultProperty = null;

			if (propertiesOfCapabilityType.containsKey(propertyName)) {
				defaultProperty = propertiesOfCapabilityType.get(propertyName);
				if (propertyType != null && defaultProperty.getType() != null && !defaultProperty.getType().equals(propertyType)) {
					log.error(" Property with name {} and different type already exists.", propertyName);
					result = Either.right(TitanOperationStatus.PROPERTY_NAME_ALREADY_EXISTS);
				} else {
					property.setType(defaultProperty.getType());
					String innerType = defaultProperty.getSchema() == null ? null : defaultProperty.getSchema().getProperty() == null ? null : defaultProperty.getSchema().getProperty().getType();

					if (property.getSchema() != null && property.getSchema().getProperty() != null) {
						property.getSchema().getProperty().setType(innerType);
					}
				}
			}
		}
		return result;
	}

	@Override
	public Either<Map<String, PropertyDefinition>, TitanOperationStatus> getAllCapabilityTypePropertiesFromAllDerivedFrom(String firstParentType) {
		Map<String, PropertyDefinition> allProperies = new HashMap<>();
		return getCapabilityTypePropertiesFromDerivedFromRecursively(firstParentType, allProperies);
	}

	private Either<Map<String, PropertyDefinition>, TitanOperationStatus> getCapabilityTypePropertiesFromDerivedFromRecursively(String nextParentType, Map<String, PropertyDefinition> allProperies) {
		TitanOperationStatus error;
		Either<List<ImmutablePair<CapabilityTypeData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.CapabilityType), nextParentType, GraphEdgeLabels.DERIVED_FROM,
				NodeTypeEnum.CapabilityType, CapabilityTypeData.class);
		if (childrenNodes.isRight()) {
			if (childrenNodes.right().value() != TitanOperationStatus.NOT_FOUND) {
				error = childrenNodes.right().value();
				log.debug("Couldn't fetch derived from node for capability type {}, error: {}", nextParentType, error);
				return Either.right(error);
			} else {
				log.debug("Derived from node is not found for type {} - this is OK for root capability.");
				return Either.left(allProperies);
			}
		} else {

			Either<Map<String, PropertyDefinition>, TitanOperationStatus> allPropertiesOfCapabilityTypeRes = propertyOperation.findPropertiesOfNode(NodeTypeEnum.CapabilityType, nextParentType);
			if (allPropertiesOfCapabilityTypeRes.isRight() && !allPropertiesOfCapabilityTypeRes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
				error = allPropertiesOfCapabilityTypeRes.right().value();
				log.error("Failed to retrieve properties for capability type {} from graph. status is {}", nextParentType, error);
				return Either.right(error);
			} else if (allPropertiesOfCapabilityTypeRes.isLeft()) {
				if (allProperies.isEmpty()) {
					allProperies.putAll(allPropertiesOfCapabilityTypeRes.left().value());
				} else {
					allProperies.putAll(allPropertiesOfCapabilityTypeRes.left().value().entrySet().stream().filter(e -> !allProperies.containsKey(e.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
				}
			}
			return getCapabilityTypePropertiesFromDerivedFromRecursively(childrenNodes.left().value().get(0).getLeft().getUniqueId(), allProperies);
		}
	}
}
