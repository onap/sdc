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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.ICapabilityInstanceOperation;
import org.openecomp.sdc.be.resources.data.CapabilityData;
import org.openecomp.sdc.be.resources.data.CapabilityInstData;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.PropertyValueData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

/**
 * public class CapabilityInstanceOperation provides methods for CRUD operations for CapabilityInstance on component instance level
 * 
 * @author ns019t
 *
 */
@Component("capability-instance-operation")
public class CapabilityInstanceOperation extends AbstractOperation implements ICapabilityInstanceOperation {

	private static Logger log = LoggerFactory.getLogger(CapabilityOperation.class.getName());

	@Autowired
	private PropertyOperation propertyOperation;

	@Autowired
	private CapabilityOperation capabilityOperation;

	/**
	 * String constants for logger
	 */
	private String statusIs = ". status is ";
	private String dot = ".";
	private String onGraph = " on graph ";
	private String ofRI = " of resource instance ";
	private String toCapability = " to capability ";
	private String toCI = " to capability instance ";
	private String toProperty = " to property ";
	private String forRI = " for resource instance ";
	private String failedCreateCI = "Failed to create capability instance of capability ";
	private String failedAddProperties = "Failed to add properties to capability instance ";
	private String ofCI = " of component instance ";
	private String failedDeletePropertyValues = "Failed to delete property values of capability instance ";
	private String toValue = " to property value ";
	private String fromRI = " from resource instance ";

	/**
	 * create capability instance of capability with property values for resource instance
	 * 
	 * @param resourceInstanceId
	 * @param capabilityId
	 * @param propertyValues
	 * @param validateCapabilityInstExistence
	 * @param capabilityName
	 * @return
	 */
	@Override
	public Either<Map<CapabilityInstData, List<PropertyValueData>>, TitanOperationStatus> createCapabilityInstanceOfCapabilityWithPropertyValuesForResourceInstance(String resourceInstanceId, String capabilityId, String capabilityName,
			List<ComponentInstanceProperty> propertyValues, boolean validateCapabilityInstExistence) {
		Wrapper<TitanOperationStatus> errorWrapper = new Wrapper<>();
		Wrapper<CapabilityData> overrideCapabilityDataWrapper = new Wrapper<>();
		Wrapper<CapabilityDefinition> overrideCapabilityDefinitionWrapper = new Wrapper<>();
		Either<CapabilityInstData, TitanOperationStatus> createCapabilityRes = null;
		CapabilityInstData createdCapabilityInstance = null;

		Wrapper<Map<String, PropertyDefinition>> defaultPropertiesWrapper = new Wrapper<>();
		Either<ImmutablePair<CapabilityData, GraphEdge>, TitanOperationStatus> getCapabilityRes = null;
		Either<CapabilityDefinition, TitanOperationStatus> getCapabilityDefinitionRes = null;
		Either<List<PropertyValueData>, TitanOperationStatus> addPropertyValuesRes = null;
		Wrapper<String> createdCapabilityInstanceIdWrapper = new Wrapper<>();
		if (validateCapabilityInstExistence) {
			validateCapabilityInstanceExistence(resourceInstanceId, capabilityId, errorWrapper);
		}
		if (errorWrapper.isEmpty()) {
			getCapabilityRes = getCapabilitiesOfResourceInstance(resourceInstanceId, capabilityId, capabilityName, errorWrapper);
		}
		if (errorWrapper.isEmpty()) {
			getCapabilityDefinitionRes = getCapabiityDefinition(resourceInstanceId, capabilityId, errorWrapper, overrideCapabilityDataWrapper, getCapabilityRes);
		}
		if (errorWrapper.isEmpty()) {
			createCapabilityRes = createCapabilityInstanceOnGraph(resourceInstanceId, capabilityId, errorWrapper, overrideCapabilityDataWrapper, overrideCapabilityDefinitionWrapper, getCapabilityDefinitionRes);
		}
		if (errorWrapper.isEmpty() && overrideCapabilityDefinitionWrapper.getInnerElement().getProperties() != null) {
			createdCapabilityInstance = validateCapabilityInstanceProperties(resourceInstanceId, propertyValues, errorWrapper, overrideCapabilityDefinitionWrapper, createCapabilityRes, defaultPropertiesWrapper, createdCapabilityInstanceIdWrapper);
		}
		if (errorWrapper.isEmpty()) {
			addPropertyValuesRes = addPropertyValueToCapabilityInstance(resourceInstanceId, propertyValues, errorWrapper, createCapabilityRes, defaultPropertiesWrapper, createdCapabilityInstanceIdWrapper);
		}
		Either<Map<CapabilityInstData, List<PropertyValueData>>, TitanOperationStatus> result;
		if (errorWrapper.isEmpty()) {
			Map<CapabilityInstData, List<PropertyValueData>> resultMap = new HashMap<>();
			resultMap.put(createdCapabilityInstance, addPropertyValuesRes.left().value());
			result = Either.left(resultMap);
		} else {
			result = Either.right(errorWrapper.getInnerElement());
		}
		return result;
	}

	@Override
	public TitanOperationStatus createCapabilityInstanceOfCapabilityWithPropertyValuesForResourceInstance(TitanVertex resourceInstanceVertex, String resourceInstanceId, String capabilityId, String capabilityName,
			List<ComponentInstanceProperty> propertyValues, boolean validateCapabilityInstExistence) {
		Wrapper<TitanOperationStatus> errorWrapper = new Wrapper<>();
		Wrapper<TitanVertex> overrideCapabilityDataWrapper = new Wrapper<>();
		Wrapper<CapabilityDefinition> overrideCapabilityDefinitionWrapper = new Wrapper<>();
		Either<TitanVertex, TitanOperationStatus> createCapabilityRes = null;
		TitanVertex createdCapabilityInstance = null;

		Wrapper<Map<String, PropertyDefinition>> defaultPropertiesWrapper = new Wrapper<>();
		Either<ImmutablePair<TitanVertex, Edge>, TitanOperationStatus> getCapabilityRes = null;
		Either<CapabilityDefinition, TitanOperationStatus> getCapabilityDefinitionRes = null;
		TitanOperationStatus addPropertyValuesRes = null;
		Wrapper<String> createdCapabilityInstanceIdWrapper = new Wrapper<>();
		if (validateCapabilityInstExistence) {
			validateCapabilityInstanceExistence(resourceInstanceVertex, resourceInstanceId, capabilityId, errorWrapper);
		}
		if (errorWrapper.isEmpty()) {
			getCapabilityRes = getCapabilitiesOfResourceInstance(resourceInstanceVertex, resourceInstanceId, capabilityId, capabilityName, errorWrapper);
		}
		if (errorWrapper.isEmpty()) {
			getCapabilityDefinitionRes = getCapabiityDefinitionByVertex(resourceInstanceId, capabilityId, errorWrapper, overrideCapabilityDataWrapper, getCapabilityRes);
		}
		if (errorWrapper.isEmpty()) {
			createCapabilityRes = createCapabilityInstanceOnGraphByVertex(resourceInstanceVertex, resourceInstanceId, capabilityId, errorWrapper, overrideCapabilityDataWrapper, overrideCapabilityDefinitionWrapper, getCapabilityDefinitionRes);
		}
		if (errorWrapper.isEmpty() && overrideCapabilityDefinitionWrapper.getInnerElement().getProperties() != null) {
			createdCapabilityInstance = validateCapabilityInstancePropertiesByVertex(resourceInstanceId, propertyValues, errorWrapper, overrideCapabilityDefinitionWrapper, createCapabilityRes.left().value(), defaultPropertiesWrapper,
					createdCapabilityInstanceIdWrapper);
		}
		if (errorWrapper.isEmpty()) {
			addPropertyValuesRes = addPropertyValueToCapabilityInstanceByVertex(resourceInstanceId, propertyValues, errorWrapper, createCapabilityRes, defaultPropertiesWrapper, createdCapabilityInstanceIdWrapper);
		}

		return addPropertyValuesRes;
	}

	private Either<List<PropertyValueData>, TitanOperationStatus> addPropertyValueToCapabilityInstance(String resourceInstanceId, List<ComponentInstanceProperty> propertyValues, Wrapper<TitanOperationStatus> errorWrapper,
			Either<CapabilityInstData, TitanOperationStatus> createCapabilityRes, Wrapper<Map<String, PropertyDefinition>> defaultPropertiesWrapper, Wrapper<String> createdCapabilityInstanceIdWrapper) {
		Either<List<PropertyValueData>, TitanOperationStatus> addPropertyValuesRes;
		log.debug("Before adding property values to capability instance {} dot", createdCapabilityInstanceIdWrapper.getInnerElement());
		addPropertyValuesRes = addPropertyValuesToCapabilityInstance(createCapabilityRes.left().value(), propertyValues, defaultPropertiesWrapper.getInnerElement());
		if (addPropertyValuesRes.isRight()) {
			errorWrapper.setInnerElement(addPropertyValuesRes.right().value());
			log.debug("failedAddProperties {} ofRI {} statusIs {} dot", createdCapabilityInstanceIdWrapper.getInnerElement(), resourceInstanceId, errorWrapper.getInnerElement());
		}
		log.debug("After adding property values to capability instance {} status is {}.", createdCapabilityInstanceIdWrapper.getInnerElement(), errorWrapper.getInnerElement());
		return addPropertyValuesRes;
	}

	private TitanOperationStatus addPropertyValueToCapabilityInstanceByVertex(String resourceInstanceId, List<ComponentInstanceProperty> propertyValues, Wrapper<TitanOperationStatus> errorWrapper,
			Either<TitanVertex, TitanOperationStatus> createCapabilityRes, Wrapper<Map<String, PropertyDefinition>> defaultPropertiesWrapper, Wrapper<String> createdCapabilityInstanceIdWrapper) {
		log.trace("Before adding property values to capability instance {}", createdCapabilityInstanceIdWrapper.getInnerElement());
		TitanOperationStatus addPropertyValuesRes = addPropertyValuesToCapabilityInstance(createCapabilityRes.left().value(), propertyValues, defaultPropertiesWrapper.getInnerElement());
		if (!addPropertyValuesRes.equals(TitanOperationStatus.OK)) {
			errorWrapper.setInnerElement(addPropertyValuesRes);
			log.debug("Failed to add properties to capability instance {} {} {} {} {}", createdCapabilityInstanceIdWrapper.getInnerElement(), ofRI, resourceInstanceId, statusIs, errorWrapper.getInnerElement());
		}
		log.trace("After adding property values to capability instance {} {} {}", createdCapabilityInstanceIdWrapper.getInnerElement(), statusIs, errorWrapper.getInnerElement());
		return addPropertyValuesRes;
	}

	private CapabilityInstData validateCapabilityInstanceProperties(String resourceInstanceId, List<ComponentInstanceProperty> propertyValues, Wrapper<TitanOperationStatus> errorWrapper,
			Wrapper<CapabilityDefinition> overrideCapabilityDefinitionWrapper, Either<CapabilityInstData, TitanOperationStatus> createCapabilityRes, Wrapper<Map<String, PropertyDefinition>> defaultPropertiesWrapper,
			Wrapper<String> createdCapabilityInstanceIdWrapper) {
		CapabilityInstData createdCapabilityInstance;
		createdCapabilityInstance = createCapabilityRes.left().value();
		createdCapabilityInstanceIdWrapper.setInnerElement(createdCapabilityInstance.getUniqueId());
		Map<String, PropertyDefinition> defaultProperties = overrideCapabilityDefinitionWrapper.getInnerElement().getProperties().stream().collect(Collectors.toMap(PropertyDefinition::getName, Function.identity()));
		defaultPropertiesWrapper.setInnerElement(defaultProperties);
		log.debug("Before validating property values of capability instance {}.", createdCapabilityInstanceIdWrapper.getInnerElement());
		Either<Boolean, TitanOperationStatus> result = validateCapabilityInstanceProperties(defaultProperties, propertyValues);
		if (result.isRight()) {
			errorWrapper.setInnerElement(result.right().value());
			log.debug("failedAddProperties {} ofRI {} statusIs {}.", createdCapabilityInstanceIdWrapper.getInnerElement(), resourceInstanceId, errorWrapper.getInnerElement());
		}
		log.debug("After validating property values of capability instance {} status is {}.", createdCapabilityInstanceIdWrapper.getInnerElement(), errorWrapper.getInnerElement());
		return createdCapabilityInstance;
	}

	private TitanVertex validateCapabilityInstancePropertiesByVertex(String resourceInstanceId, List<ComponentInstanceProperty> propertyValues, Wrapper<TitanOperationStatus> errorWrapper,
			Wrapper<CapabilityDefinition> overrideCapabilityDefinitionWrapper, TitanVertex createCapabilityRes, Wrapper<Map<String, PropertyDefinition>> defaultPropertiesWrapper, Wrapper<String> createdCapabilityInstanceIdWrapper) {
		String id = (String) titanGenericDao.getProperty(createCapabilityRes, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
		createdCapabilityInstanceIdWrapper.setInnerElement(id);
		Map<String, PropertyDefinition> defaultProperties = overrideCapabilityDefinitionWrapper.getInnerElement().getProperties().stream().collect(Collectors.toMap(PropertyDefinition::getName, Function.identity()));
		defaultPropertiesWrapper.setInnerElement(defaultProperties);
		log.trace("Before validating property values of capability instance {}", createdCapabilityInstanceIdWrapper.getInnerElement());
		Either<Boolean, TitanOperationStatus> result = validateCapabilityInstanceProperties(defaultProperties, propertyValues);
		if (result.isRight()) {
			errorWrapper.setInnerElement(result.right().value());
			log.debug("Failed to add properties to capability instance {} {} {} {} {}", createdCapabilityInstanceIdWrapper.getInnerElement(), ofRI, resourceInstanceId, statusIs, errorWrapper.getInnerElement());
		}
		log.trace("After validating property values of capability instance {} {} {}", createdCapabilityInstanceIdWrapper.getInnerElement(), statusIs, errorWrapper.getInnerElement());
		return createCapabilityRes;
	}

	private Either<CapabilityInstData, TitanOperationStatus> createCapabilityInstanceOnGraph(String resourceInstanceId, String capabilityId, Wrapper<TitanOperationStatus> errorWrapper, Wrapper<CapabilityData> overrideCapabilityDataWrapper,
			Wrapper<CapabilityDefinition> overrideCapabilityDefinitionWrapper, Either<CapabilityDefinition, TitanOperationStatus> getCapabilityDefinitionRes) {
		Either<CapabilityInstData, TitanOperationStatus> createCapabilityRes;
		log.debug("Before creating capability instance of capability {} on graph.", capabilityId);
		overrideCapabilityDefinitionWrapper.setInnerElement(getCapabilityDefinitionRes.left().value());
		CapabilityInstData capabilityInstance = buildCapabilityInstanceData(resourceInstanceId, overrideCapabilityDefinitionWrapper.getInnerElement());
		createCapabilityRes = createCapabilityInstanceOnGraph(resourceInstanceId, overrideCapabilityDataWrapper.getInnerElement(), capabilityInstance);
		if (createCapabilityRes.isRight()) {
			errorWrapper.setInnerElement(createCapabilityRes.right().value());
			log.debug("failedCreateCI {} ofRI {} statusIs {}.", capabilityId, resourceInstanceId, errorWrapper.getInnerElement());
		}
		log.debug("After creating capability instance of capability {} on graph. Status is {}", capabilityId, errorWrapper.getInnerElement());
		return createCapabilityRes;
	}

	private Either<TitanVertex, TitanOperationStatus> createCapabilityInstanceOnGraphByVertex(TitanVertex riVertex, String resourceInstanceId, String capabilityId, Wrapper<TitanOperationStatus> errorWrapper,
			Wrapper<TitanVertex> overrideCapabilityDataWrapper, Wrapper<CapabilityDefinition> overrideCapabilityDefinitionWrapper, Either<CapabilityDefinition, TitanOperationStatus> getCapabilityDefinitionRes) {
		Either<TitanVertex, TitanOperationStatus> createCapabilityRes;
		log.trace("Before creating capability instance of capability {} {}", capabilityId, onGraph);
		overrideCapabilityDefinitionWrapper.setInnerElement(getCapabilityDefinitionRes.left().value());
		CapabilityInstData capabilityInstance = buildCapabilityInstanceData(resourceInstanceId, overrideCapabilityDefinitionWrapper.getInnerElement());
		createCapabilityRes = createCapabilityInstanceOnGraph(riVertex, resourceInstanceId, overrideCapabilityDataWrapper.getInnerElement(), capabilityInstance);
		if (createCapabilityRes.isRight()) {
			errorWrapper.setInnerElement(createCapabilityRes.right().value());
			log.debug("Failed to create capability instance of capability {} {} {} {} {} ", capabilityId, ofRI, resourceInstanceId, statusIs, errorWrapper.getInnerElement());
		}
		log.debug("After creating capability instance of capability {} {} {} {} {}", capabilityId, onGraph, statusIs, errorWrapper.getInnerElement());
		return createCapabilityRes;
	}

	private Either<CapabilityDefinition, TitanOperationStatus> getCapabiityDefinition(String resourceInstanceId, String capabilityId, Wrapper<TitanOperationStatus> errorWrapper, Wrapper<CapabilityData> overrideCapabilityDataWrapper,
			Either<ImmutablePair<CapabilityData, GraphEdge>, TitanOperationStatus> getCapabilityRes) {
		Either<CapabilityDefinition, TitanOperationStatus> getCapabilityDefinitionRes;
		log.debug("Before getting capability definition {} forRI {}.", capabilityId, resourceInstanceId);
		CapabilityData overrideCapabilityData = getCapabilityRes.left().value().getLeft();
		overrideCapabilityDataWrapper.setInnerElement(overrideCapabilityData);
		getCapabilityDefinitionRes = capabilityOperation.getCapabilityByCapabilityData(overrideCapabilityData);
		if (getCapabilityDefinitionRes.isRight()) {
			errorWrapper.setInnerElement(getCapabilityDefinitionRes.right().value());
			log.debug("Failed to retrieve capability {} ofRI {} statusIs {}.", capabilityId, resourceInstanceId, errorWrapper.getInnerElement());
		}
		log.debug("After getting capability definition for {} forRI {} statusIs {}.", capabilityId, resourceInstanceId, errorWrapper.getInnerElement());
		return getCapabilityDefinitionRes;
	}

	private Either<CapabilityDefinition, TitanOperationStatus> getCapabiityDefinitionByVertex(String resourceInstanceId, String capabilityId, Wrapper<TitanOperationStatus> errorWrapper, Wrapper<TitanVertex> overrideCapabilityDataWrapper,
			Either<ImmutablePair<TitanVertex, Edge>, TitanOperationStatus> getCapabilityRes) {
		Either<CapabilityDefinition, TitanOperationStatus> getCapabilityDefinitionRes;
		log.trace("Before getting capability definition {} {} {}", capabilityId, forRI, resourceInstanceId);

		TitanVertex overrideCapabilityData = getCapabilityRes.left().value().getLeft();

		overrideCapabilityDataWrapper.setInnerElement(overrideCapabilityData);
		getCapabilityDefinitionRes = capabilityOperation.getCapabilityByCapabilityData(overrideCapabilityData);
		if (getCapabilityDefinitionRes.isRight()) {
			errorWrapper.setInnerElement(getCapabilityDefinitionRes.right().value());
			log.debug("Failed to retrieve capability {} ofRI {} statusIs {}", capabilityId, resourceInstanceId, errorWrapper.getInnerElement());
		}
		log.debug("After getting capability definition for {} forRI {} statusIs {}.", capabilityId, resourceInstanceId, errorWrapper.getInnerElement());
		return getCapabilityDefinitionRes;
	}

	private Either<ImmutablePair<CapabilityData, GraphEdge>, TitanOperationStatus> getCapabilitiesOfResourceInstance(String resourceInstanceId, String capabilityId, String capabilityName, Wrapper<TitanOperationStatus> errorWrapper) {
		Either<ImmutablePair<CapabilityData, GraphEdge>, TitanOperationStatus> getCapabilityRes;
		log.debug("Before getting capability {} forRI {}.", capabilityId, resourceInstanceId);
		Map<String, Object> props = new HashMap<>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), capabilityName);
		getCapabilityRes = titanGenericDao.getChildByEdgeCriteria(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), resourceInstanceId, GraphEdgeLabels.CALCULATED_CAPABILITY, NodeTypeEnum.Capability, CapabilityData.class, props);
		if (getCapabilityRes.isRight()) {
			errorWrapper.setInnerElement(getCapabilityRes.right().value());
			log.debug("Failed to get capability {} ofRI {} statusIs {}.", capabilityId, resourceInstanceId, errorWrapper.getInnerElement());
		}
		log.debug("After getting capability for {} forRI {} statusIs {}.", capabilityId, resourceInstanceId, errorWrapper.getInnerElement());
		return getCapabilityRes;
	}

	private Either<ImmutablePair<TitanVertex, Edge>, TitanOperationStatus> getCapabilitiesOfResourceInstance(TitanVertex instanceVertex, String resourceInstanceId, String capabilityId, String capabilityName,
			Wrapper<TitanOperationStatus> errorWrapper) {
		Either<ImmutablePair<TitanVertex, Edge>, TitanOperationStatus> getCapabilityRes;
		log.trace("Before getting capability {} {} {}", capabilityId, forRI, resourceInstanceId);
		Map<String, Object> props = new HashMap<>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), capabilityName);
		getCapabilityRes = titanGenericDao.getChildByEdgeCriteria(instanceVertex, GraphEdgeLabels.CALCULATED_CAPABILITY, props);
		if (getCapabilityRes.isRight()) {
			errorWrapper.setInnerElement(getCapabilityRes.right().value());
			log.debug("Failed to get capability {} {} {} {} {}", capabilityId, ofRI, resourceInstanceId, statusIs, errorWrapper.getInnerElement());
		}
		log.trace("After getting capability for {} {} {} {} {}", capabilityId, forRI, resourceInstanceId, statusIs, errorWrapper.getInnerElement());
		return getCapabilityRes;
	}

	private void validateCapabilityInstanceExistence(String resourceInstanceId, String capabilityId, Wrapper<TitanOperationStatus> errorWrapper) {
		log.debug("Before validation of existence of capability instance of capability {} forRI {}.", capabilityId, resourceInstanceId);
		boolean capabilityInstOfCapabilityAlreadyExists;
		Either<Boolean, TitanOperationStatus> validateCapabilityInstExistenceRes = validateCapabilityInstExistence(resourceInstanceId, capabilityId);
		if (validateCapabilityInstExistenceRes.isRight()) {
			errorWrapper.setInnerElement(validateCapabilityInstExistenceRes.right().value());
			log.debug("Failed to validate uniqueness of capability instance of capability {} ofRI {} statusIs {}.", capabilityId, resourceInstanceId, errorWrapper.getInnerElement());
		} else {
			capabilityInstOfCapabilityAlreadyExists = validateCapabilityInstExistenceRes.left().value();
			if (capabilityInstOfCapabilityAlreadyExists) {
				errorWrapper.setInnerElement(TitanOperationStatus.ALREADY_EXIST);
				log.debug("failedCreateCI {} ofRI {} statusIs {}.", capabilityId, resourceInstanceId, errorWrapper.getInnerElement());
			}
		}
		log.debug("After validation of existence of capability instance of capability {} forRI {} statusIs {}.", capabilityId, resourceInstanceId, errorWrapper.getInnerElement());
	}

	private void validateCapabilityInstanceExistence(TitanVertex resourceInstanceVertex, String resourceInstanceId, String capabilityId, Wrapper<TitanOperationStatus> errorWrapper) {
		log.trace("Before validation of existence of capability instance of capability {} {} {}", capabilityId, forRI, resourceInstanceId);
		boolean capabilityInstOfCapabilityAlreadyExists;
		Either<Boolean, TitanOperationStatus> validateCapabilityInstExistenceRes = validateCapabilityInstExistence(resourceInstanceId, capabilityId);
		if (validateCapabilityInstExistenceRes.isRight()) {
			errorWrapper.setInnerElement(validateCapabilityInstExistenceRes.right().value());
			log.debug("Failed to validate uniqueness of capability instance of capability {} ofRI {} statusIs {}.", capabilityId, resourceInstanceId, errorWrapper.getInnerElement());
		} else {
			capabilityInstOfCapabilityAlreadyExists = validateCapabilityInstExistenceRes.left().value();
			if (capabilityInstOfCapabilityAlreadyExists) {
				errorWrapper.setInnerElement(TitanOperationStatus.ALREADY_EXIST);
				log.debug("failedCreateCI {} ofRI {} statusIs {}.", capabilityId, resourceInstanceId, errorWrapper.getInnerElement());
			}
		}
		log.debug("After validation of existence of capability instance of capability {} forRI {} statusIs {}.", capabilityId, resourceInstanceId, errorWrapper.getInnerElement());
	}

	private Either<List<PropertyValueData>, TitanOperationStatus> addPropertyValuesToCapabilityInstance(CapabilityInstData createdCapabilityInstance, List<ComponentInstanceProperty> propertyValues, Map<String, PropertyDefinition> defaultProperties) {
		TitanOperationStatus error = null;
		List<PropertyValueData> createdPropertyValues = new ArrayList<>();
		for (ComponentInstanceProperty property : propertyValues) {
			log.debug("Before adding property value {} toCI {}.", property.getName(), createdCapabilityInstance.getUniqueId());
			PropertyValueData propertyData = buildPropertyValueData(property.getName(), property.getType(), property.getValue(), createdCapabilityInstance.getUniqueId());
			Either<PropertyValueData, TitanOperationStatus> addPropertyValueRes = addPropertyValueToCapabilityInstance(createdCapabilityInstance, propertyData, defaultProperties.get(property.getName()));
			if (addPropertyValueRes.isRight()) {
				error = addPropertyValueRes.right().value();
				log.debug("Failed to add property to capability instance {} ofRI. StatusIs {}.", createdCapabilityInstance.getUniqueId(), error);
				break;
			} else {
				createdPropertyValues.add(addPropertyValueRes.left().value());
			}
			log.debug("After adding property value {} toCI {} statusIs {}", property.getName(), createdCapabilityInstance.getUniqueId(), error);
		}
		if (error == null) {
			return Either.left(createdPropertyValues);
		}
		return Either.right(error);
	}

	private TitanOperationStatus addPropertyValuesToCapabilityInstance(TitanVertex createdCapabilityInstancevertex, List<ComponentInstanceProperty> propertyValues, Map<String, PropertyDefinition> defaultProperties) {
		TitanOperationStatus error = null;
		String id = (String) titanGenericDao.getProperty(createdCapabilityInstancevertex, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
		for (ComponentInstanceProperty property : propertyValues) {
			log.trace("Before adding property value {} {} {}", property.getName(), toCI, id);
			PropertyValueData propertyData = buildPropertyValueData(property.getName(), property.getType(), property.getValue(), id);
			TitanOperationStatus addPropertyValueRes = addPropertyValueToCapabilityInstance(createdCapabilityInstancevertex, propertyData, defaultProperties.get(property.getName()), id);
			if (!addPropertyValueRes.equals(TitanOperationStatus.OK)) {
				error = addPropertyValueRes;
				log.debug("Failed to add property to capability instance {} {} {} {}", id, ofRI, statusIs, error);
				break;
			}
			log.debug("After adding property value {} {} {} {} {}", property.getName(), toCI, id, statusIs, error);
		}
		if (error == null) {
			return TitanOperationStatus.OK;
		}
		return error;
	}

	private PropertyValueData buildPropertyValueData(String propertyName, String propertyType, String propertyValue, String capabilityInstanceId) {
		PropertyValueData propertyData = new PropertyValueData();
		String uniqueId = UniqueIdBuilder.buildPropertyValueUniqueId(capabilityInstanceId, propertyName);
		Long creationTime = System.currentTimeMillis();
		propertyData.setUniqueId(uniqueId);
		propertyData.setValue(propertyValue);
		propertyData.setType(propertyType);
		propertyData.setCreationTime(creationTime);
		propertyData.setModificationTime(creationTime);
		return propertyData;
	}

	private Either<PropertyValueData, TitanOperationStatus> addPropertyValueToCapabilityInstance(CapabilityInstData createdCapabilityInstance, PropertyValueData propertyValue, PropertyDefinition propertyDefinition) {
		TitanOperationStatus error = null;
		Map<String, Object> props = null;
		Either<GraphRelation, TitanOperationStatus> createRelationRes;
		PropertyValueData createdValue = null;
		log.debug("Before creating property value node {} onGraph.", propertyValue.getUniqueId());
		Either<PropertyValueData, TitanOperationStatus> createValueRes = titanGenericDao.createNode(propertyValue, PropertyValueData.class);
		if (createValueRes.isRight()) {
			error = createValueRes.right().value();
			log.debug("Failed to create property value for capability instance {} ofRI statusIs {}.", createdCapabilityInstance.getUniqueId(), error);
		}
		log.debug("After creating property value node {} onGraph statusIs {}.", propertyValue.getUniqueId(), error);
		if (error == null) {
			log.debug("Before creating relation from property value node {} toCI {}.", propertyValue.getUniqueId(), createdCapabilityInstance.getUniqueId());
			createdValue = createValueRes.left().value();
			props = new HashMap<>();
			props.put(GraphPropertiesDictionary.PROPERTY_NAME.name(), propertyDefinition.getName());
			props.put(GraphPropertiesDictionary.PROPERTY_ID.name(), propertyDefinition.getUniqueId());
			createRelationRes = titanGenericDao.createRelation(createdCapabilityInstance, createdValue, GraphEdgeLabels.PROPERTY_VALUE, props);
			if (createRelationRes.isRight()) {
				error = createRelationRes.right().value();
				log.debug("Failed to create relation from capability instance {} toValue {} statusIs {}.", createdCapabilityInstance.getUniqueId(), createdValue.getUniqueId(), error);
			}
			log.debug("After creating relation from property value node {} toCI {}  statusIs {}.", propertyValue.getUniqueId(), createdCapabilityInstance.getUniqueId(), error);
		}
		if (error == null) {
			log.debug("Before creating relation from property value node {} toProperty {}.", propertyValue.getUniqueId(), propertyDefinition.getUniqueId());
			createRelationRes = titanGenericDao.createRelation(propertyValue, new PropertyData(propertyDefinition, null), GraphEdgeLabels.PROPERTY_IMPL, props);
			if (createRelationRes.isRight()) {
				error = createRelationRes.right().value();
				log.debug("Failed to create relation from property value {} toProperty {} statusIs {}.", createdValue.getUniqueId(), propertyDefinition.getUniqueId(), error);
			}
			log.debug("After creating relation from property value node {} toProperty statusIs {}.", propertyValue.getUniqueId(), propertyDefinition.getUniqueId(), error);
		}
		if (error == null) {
			return Either.left(createdValue);
		}
		return Either.right(error);
	}

	private TitanOperationStatus addPropertyValueToCapabilityInstance(TitanVertex createdCapabilityInstanceVertex, PropertyValueData propertyValue, PropertyDefinition propertyDefinition, String id) {
		TitanOperationStatus error = null;
		Map<String, Object> props = null;
		TitanOperationStatus createRelationRes;
		log.trace("Before creating property value node {}  on graph.", propertyValue.getUniqueId());
		Either<TitanVertex, TitanOperationStatus> createValueRes = titanGenericDao.createNode(propertyValue);
		if (createValueRes.isRight()) {
			error = createValueRes.right().value();
			if (log.isDebugEnabled()){
				log.debug("Failed to create property value for capability instance {} {} {} {}", id, ofRI, statusIs, error);
			}
		}
		log.trace("After creating property value node {}  on graph status is {}", propertyValue.getUniqueId(), error);
		TitanVertex createdPropVertex = null;
		String createdId = null;
		if (error == null) {
			log.trace("Before creating relation from property value node {} {} {} ", propertyValue.getUniqueId(), toCI, id);
			props = new HashMap<>();
			props.put(GraphPropertiesDictionary.PROPERTY_NAME.name(), propertyDefinition.getName());
			props.put(GraphPropertiesDictionary.PROPERTY_ID.name(), propertyDefinition.getUniqueId());
			createdPropVertex = createValueRes.left().value();
			createRelationRes = titanGenericDao.createEdge(createdCapabilityInstanceVertex, createdPropVertex, GraphEdgeLabels.PROPERTY_VALUE, props);
			if (!createRelationRes.equals(TitanOperationStatus.OK)) {
				error = createRelationRes;
				createdId = (String) titanGenericDao.getProperty(createdPropVertex, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
				if (log.isDebugEnabled()) {
					log.debug("Failed to create relation from capability instance {} {} {} {} {}", id, toValue, createdId, statusIs, error);
				}
			}
			if (log.isTraceEnabled()){
				log.trace("After creating relation from property value node {} {} {} {} {}", propertyValue.getUniqueId(), toCI, id, statusIs, error);
			}
		}
		if (error == null) {
			log.trace("Before creating relation from property value node {} {} {}", propertyValue.getUniqueId(), toProperty, propertyDefinition.getUniqueId());
			createRelationRes = titanGenericDao.createEdge(createdPropVertex, new PropertyData(propertyDefinition, null), GraphEdgeLabels.PROPERTY_IMPL, props);
			if (!createRelationRes.equals(TitanOperationStatus.OK)) {
				error = createRelationRes;
				log.debug("Failed to create relation from property value {} {} {} {} {}", createdId, toProperty, propertyDefinition.getUniqueId(), statusIs, error);
			}
			log.debug("After creating relation from property value node {} {} {} {} {}", propertyValue.getUniqueId(), toProperty, propertyDefinition.getUniqueId(), statusIs, error);
		}
		if (error == null) {
			return TitanOperationStatus.OK;
		}
		return error;
	}

	private Either<Boolean, TitanOperationStatus> validateCapabilityInstanceProperties(Map<String, PropertyDefinition> defaultProperties, List<ComponentInstanceProperty> propertyValues) {
		Either<Boolean, TitanOperationStatus> result = Either.left(true);
		for (ComponentInstanceProperty property : propertyValues) {
			result = validateUpdateCapabilityInstancePropertyValue(property, defaultProperties);
			if (result.isRight()) {
				break;
			}
		}
		return result;
	}

	private Either<Boolean, TitanOperationStatus> validateUpdateCapabilityInstancePropertyValue(ComponentInstanceProperty property, Map<String, PropertyDefinition> defaultProperties) {
		PropertyDefinition defaultProperty;
		String propertyName = property.getName();
		Either<Boolean, TitanOperationStatus> result = null;
		if (defaultProperties.containsKey(propertyName)) {
			defaultProperty = defaultProperties.get(propertyName);
			String propertyType = property.getType() == null || property.getType().isEmpty() ? defaultProperty.getType() : property.getType();

			String innerType = null;
			if (property.getSchema() != null && property.getSchema().getProperty() != null)
				innerType = property.getSchema().getProperty().getType();
			if (innerType == null && defaultProperty.getSchema() != null && defaultProperty.getSchema().getProperty() != null)
				innerType = defaultProperty.getSchema().getProperty().getType();

			if (defaultProperty.getType().equals(propertyType)) {
				String propertyValue = property.getValue();
				Either<Map<String, DataTypeDefinition>, TitanOperationStatus> allDataTypes = applicationDataTypeCache.getAll();
				if (allDataTypes.isRight()) {
					TitanOperationStatus status = allDataTypes.right().value();
					log.debug("Failed to update property value statusIs {}.", status);
					result = Either.right(status);
				}
				if (result == null) {
					Either<Object, Boolean> updatedPropertyValueRes = propertyOperation.validateAndUpdatePropertyValue(propertyType, propertyValue, innerType, allDataTypes.left().value());
					if (updatedPropertyValueRes.isLeft()) {
						if (updatedPropertyValueRes.left().value() != null)
							property.setDefaultValue(updatedPropertyValueRes.left().value().toString());
						result = Either.left(true);
					} else {
						result = Either.right(TitanOperationStatus.INVALID_PROPERTY);
					}
				}
				log.debug("The property with name {} has invalid type {} or invalid value {}.", propertyName, propertyType, propertyValue);

			} else {
				result = Either.right(TitanOperationStatus.PROPERTY_NAME_ALREADY_EXISTS);
				log.debug("The property with name {} and different type already exists.", propertyName);
			}
		} else {
			result = Either.right(TitanOperationStatus.NOT_FOUND);
			log.debug("Failed to find property with name {}.", propertyName);
		}
		return result;
	}

	/**
	 * validate capability instance uniqueness
	 * 
	 * @param resourceInstanceId
	 * @param capabilityId
	 * @return
	 */
	@Override
	public Either<Boolean, TitanOperationStatus> validateCapabilityInstExistence(String resourceInstanceId, String capabilityId) {
		Either<Boolean, TitanOperationStatus> result = null;
		TitanOperationStatus error;
		Map<String, Object> props = new HashMap<>();
		props.put(GraphPropertiesDictionary.CAPABILITY_ID.getProperty(), capabilityId);
		Either<Edge, TitanOperationStatus> getCapabilityInstanceEdgeRes = titanGenericDao.getOutgoingEdgeByCriteria(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), resourceInstanceId, GraphEdgeLabels.CAPABILITY_INST, props);
		if (getCapabilityInstanceEdgeRes.isRight()) {
			error = getCapabilityInstanceEdgeRes.right().value();
			if (error.equals(TitanOperationStatus.NOT_FOUND)) {
				result = Either.left(false);
			} else {
				log.debug("Failed to get outgoing edge for resource instance {} statusIs {}.", resourceInstanceId, error);
				result = Either.right(error);
			}
		}
		if (result == null) {
			result = Either.left(true);
		}
		return result;
	}

	@Override
	public Either<Boolean, TitanOperationStatus> validateCapabilityInstExistence(TitanVertex instanceVertex, String resourceInstanceId, String capabilityId) {
		Either<Boolean, TitanOperationStatus> result = null;
		TitanOperationStatus error;
		Map<String, Object> props = new HashMap<>();
		props.put(GraphPropertiesDictionary.CAPABILITY_ID.getProperty(), capabilityId);
		Either<Edge, TitanOperationStatus> getCapabilityInstanceEdgeRes = titanGenericDao.getOutgoingEdgeByCriteria(instanceVertex, GraphEdgeLabels.CAPABILITY_INST, props);
		if (getCapabilityInstanceEdgeRes.isRight()) {
			error = getCapabilityInstanceEdgeRes.right().value();
			if (error.equals(TitanOperationStatus.NOT_FOUND)) {
				result = Either.left(false);
			} else {
				log.debug("Failed to get outgoing edge for resource instance {} {} {}", resourceInstanceId, statusIs, error);
				result = Either.right(error);
			}
		}
		if (result == null) {
			result = Either.left(true);
		}
		return result;
	}

	private Either<CapabilityInstData, TitanOperationStatus> createCapabilityInstanceOnGraph(String resourceInstanceId, CapabilityData overrideCapabilityData, CapabilityInstData capabilityInstance) {
		log.debug("Before creation of capability instance of capability {} forRI {}.", overrideCapabilityData.getUniqueId(), resourceInstanceId);

		Either<GraphRelation, TitanOperationStatus> createRelationRes;
		CapabilityInstData createdCapabilityInstance = null;
		String capabilityInstanceId = null;
		TitanOperationStatus error = null;
		Either<CapabilityInstData, TitanOperationStatus> createCapabilityInstanceRes = titanGenericDao.createNode(capabilityInstance, CapabilityInstData.class);
		if (createCapabilityInstanceRes.isRight()) {
			error = createCapabilityInstanceRes.right().value();
			log.debug("failedCreateCI {} forRI {} statusIs {}.", overrideCapabilityData.getUniqueId(), resourceInstanceId, error);
		}
		log.debug("After creation of capability instance of capability {} forRI {} statusIs {}.", overrideCapabilityData.getUniqueId(), resourceInstanceId, error);
		if (error == null) {
			createdCapabilityInstance = createCapabilityInstanceRes.left().value();
			capabilityInstanceId = createdCapabilityInstance.getUniqueId();
			Map<String, Object> props = new HashMap<>();
			props.put(GraphPropertiesDictionary.CAPABILITY_ID.getProperty(), overrideCapabilityData.getUniqueId());
			UniqueIdData resourceInstanceIdData = new UniqueIdData(NodeTypeEnum.ResourceInstance, resourceInstanceId);
			log.debug("Before associating resource instance {} to capability instance.", resourceInstanceId);
			createRelationRes = titanGenericDao.createRelation(resourceInstanceIdData, capabilityInstance, GraphEdgeLabels.CAPABILITY_INST, props);
			if (createRelationRes.isRight()) {
				error = createRelationRes.right().value();
				log.debug("Failed to assotiate resource instance {} toCI {} statusIs {}.", resourceInstanceId, capabilityInstanceId, error);
			}
			log.debug("After associating resource instance {} to CI {} statusIs {}.", resourceInstanceId, capabilityInstanceId, error);
		}
		if (error == null) {
			log.debug("Before associating capability instance {} toCapability {}.", capabilityInstanceId, overrideCapabilityData.getUniqueId());
			Map<String, Object> props = new HashMap<>();
			props.put(GraphPropertiesDictionary.CAPABILITY_ID.getProperty(), overrideCapabilityData.getUniqueId());
			createRelationRes = titanGenericDao.createRelation(createdCapabilityInstance, overrideCapabilityData, GraphEdgeLabels.INSTANCE_OF, props);
			if (createRelationRes.isRight()) {
				error = createRelationRes.right().value();
				log.debug("Failed to associate capability instance {} toCapability statusIs {}.", capabilityInstanceId, overrideCapabilityData.getUniqueId(), error);
			}
			log.debug("After associating capability instance {} toCapability statusIs {}.", capabilityInstanceId, overrideCapabilityData.getUniqueId(), error);
		}
		if (error == null) {
			return createCapabilityInstanceRes;
		}
		return Either.right(error);
	}

	private Either<TitanVertex, TitanOperationStatus> createCapabilityInstanceOnGraph(TitanVertex riVertex, String resourceInstanceId, TitanVertex overrideCapabilityDataVertex, CapabilityInstData capabilityInstance) {
		String overrideCapabilityDataId = (String) titanGenericDao.getProperty(overrideCapabilityDataVertex, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
		log.trace("Before creation of capability instance of capability {} {} {}", overrideCapabilityDataVertex, forRI, resourceInstanceId);

		TitanOperationStatus createRelationRes;
		TitanVertex createdCapabilityInstance = null;
		String capabilityInstanceId = null;
		TitanOperationStatus error = null;
		Either<TitanVertex, TitanOperationStatus> createCapabilityInstanceRes = titanGenericDao.createNode(capabilityInstance);
		if (createCapabilityInstanceRes.isRight()) {
			error = createCapabilityInstanceRes.right().value();
			log.debug("Failed to create capability instance of capability {} {} {} {} {}", overrideCapabilityDataId, forRI, resourceInstanceId, statusIs, error);
		}
		log.trace("After creation of capability instance of capability {} {} {} {} {}", overrideCapabilityDataId, forRI, resourceInstanceId, statusIs, error);
		if (error == null) {
			createdCapabilityInstance = createCapabilityInstanceRes.left().value();
			capabilityInstanceId = (String) titanGenericDao.getProperty(createdCapabilityInstance, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
			Map<String, Object> props = new HashMap<>();
			props.put(GraphPropertiesDictionary.CAPABILITY_ID.getProperty(), overrideCapabilityDataId);
			log.debug("Before associating resource instance {} to capability instance.", resourceInstanceId);

			createRelationRes = titanGenericDao.createEdge(riVertex, capabilityInstance, GraphEdgeLabels.CAPABILITY_INST, props);
			if (!createRelationRes.equals(TitanOperationStatus.OK)) {
				error = createRelationRes;
				log.debug("Failed to assotiate resource instance {} {} {} {} {}", resourceInstanceId, toCI, capabilityInstanceId, statusIs, error);
			}
			if (log.isTraceEnabled()) {
				log.trace("After associating resource instance {} {} {} {} {}", resourceInstanceId, toCI, capabilityInstanceId, statusIs, error);
			}
		}
		if (error == null) {
			log.trace("Before associating capability instance {} {} {}", capabilityInstanceId, toCapability, overrideCapabilityDataId);
			Map<String, Object> props = new HashMap<>();
			props.put(GraphPropertiesDictionary.CAPABILITY_ID.getProperty(), overrideCapabilityDataId);
			createRelationRes = titanGenericDao.createEdge(createdCapabilityInstance, overrideCapabilityDataVertex, GraphEdgeLabels.INSTANCE_OF, props);
			if (!createRelationRes.equals(TitanOperationStatus.OK)) {
				error = createRelationRes;
				log.debug("Failed to associate capability instance {} {} {} {} {}", capabilityInstanceId, toCapability, overrideCapabilityDataId, statusIs, error);
			}
			log.debug("After associating capability instance {} {} {} {} {}", capabilityInstanceId, toCapability, overrideCapabilityDataId, statusIs, error);
		}
		if (error == null) {
			return createCapabilityInstanceRes;
		}
		return Either.right(error);
	}

	private CapabilityInstData buildCapabilityInstanceData(String resourceInstanceId, CapabilityDefinition capability) {
		CapabilityInstData capabilityInstance = new CapabilityInstData();
		Long creationTime = System.currentTimeMillis();
		String uniqueId = UniqueIdBuilder.buildCapabilityInstanceUid(resourceInstanceId, capability.getName());

		capabilityInstance.setCreationTime(creationTime);
		capabilityInstance.setModificationTime(creationTime);
		capabilityInstance.setUniqueId(uniqueId);

		return capabilityInstance;
	}

	/**
	 * delete capability instance from resource instance
	 * 
	 * @param resourceInstanceId
	 * @param capabilityInstanceId
	 * @return
	 */
	@Override
	public Either<CapabilityInstData, TitanOperationStatus> deleteCapabilityInstanceFromResourceInstance(String resourceInstanceId, String capabilityInstanceId) {
		log.debug("Before deleting of capability instance {} fromRI {}.", capabilityInstanceId, resourceInstanceId);

		Either<CapabilityInstData, TitanOperationStatus> deleteCapabilityInstRes = null;
		TitanOperationStatus error = null;
		Either<Boolean, TitanOperationStatus> deleteProperyValuesRes = deleteAllPropertyValuesOfCapabilityInstance(resourceInstanceId, capabilityInstanceId);
		if (deleteProperyValuesRes.isRight()) {
			error = deleteProperyValuesRes.right().value();
			log.debug("failedDeletePropertyValues {} for RI {} statusIs {}.", capabilityInstanceId, resourceInstanceId, error);
		}
		if (error == null) {
			deleteCapabilityInstRes = titanGenericDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.CapabilityInst), capabilityInstanceId, CapabilityInstData.class);
			if (deleteCapabilityInstRes.isRight()) {
				error = deleteCapabilityInstRes.right().value();
				log.debug("Failed to delete capability instance {} forRI {} statusIs {}", capabilityInstanceId, resourceInstanceId, error);
			}
		}
		log.debug("After deleting of capability instance {} fromRI {} statusIs {}.", capabilityInstanceId, resourceInstanceId, error);
		if (error == null) {
			return Either.left(deleteCapabilityInstRes.left().value());
		}
		return Either.right(error);
	}

	private Either<Boolean, TitanOperationStatus> deleteAllPropertyValuesOfCapabilityInstance(String resourceInstanceId, String capabilityInstanceId) {
		log.debug("Before deleting all property values of capability instance {} fromRI {}.", capabilityInstanceId, resourceInstanceId);
		TitanOperationStatus error = null;
		List<ImmutablePair<PropertyValueData, GraphEdge>> deletePropertiesPairs;
		Either<List<ImmutablePair<PropertyValueData, GraphEdge>>, TitanOperationStatus> getPropertyValuesRes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.CapabilityInst), capabilityInstanceId,
				GraphEdgeLabels.PROPERTY_VALUE, NodeTypeEnum.PropertyValue, PropertyValueData.class);
		if (getPropertyValuesRes.isRight()) {
			error = getPropertyValuesRes.right().value();
			log.debug("Failed to retrieve property values of capability instance {} forRI {} status {}.", capabilityInstanceId, resourceInstanceId, error);
		}
		if (error == null) {
			deletePropertiesPairs = getPropertyValuesRes.left().value();
			for (ImmutablePair<PropertyValueData, GraphEdge> propertyPair : deletePropertiesPairs) {
				Either<PropertyValueData, TitanOperationStatus> deletePropertyRes = titanGenericDao.deleteNode(propertyPair.getLeft(), PropertyValueData.class);
				if (deletePropertyRes.isRight()) {
					error = deletePropertyRes.right().value();
					log.debug("failedDeletePropertyValues {} forRI {} statusIs {}.", capabilityInstanceId, resourceInstanceId, error);
					break;
				}
			}
		}
		log.debug("After deleting all property values of capability instance {} fromRI {} statusIs {}.", capabilityInstanceId, resourceInstanceId, error);
		if (error == null) {
			return Either.left(true);
		}
		return Either.right(error);
	}

	/**
	 * get all capability instances for resource instance returns all Capability Instances related to Resource Instance as List<CapabilityInstData> or TitanOperationStatus if error occurs or if Resource Instance have no any related Capability
	 * Instance
	 * 
	 * @param resourceInstanceId
	 * @return Either<List<CapabilityInstData>, TitanOperationStatus>
	 */
	@Override
	public Either<List<ImmutablePair<CapabilityInstData, GraphEdge>>, TitanOperationStatus> getAllCapabilityInstancesOfResourceInstance(String resourceInstanceId) {
		log.debug("Before deleting all capability instances of resource instance {}.", resourceInstanceId);
		TitanOperationStatus error = null;
		Either<List<ImmutablePair<CapabilityInstData, GraphEdge>>, TitanOperationStatus> getCapabilityInstancesRes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), resourceInstanceId,
				GraphEdgeLabels.CAPABILITY_INST, NodeTypeEnum.CapabilityInst, CapabilityInstData.class);
		if (getCapabilityInstancesRes.isRight()) {
			error = getCapabilityInstancesRes.right().value();
			log.debug("Failed to retrieve capability Instances of resource instance {} statusIs {}.", resourceInstanceId, error);
		}
		log.debug("After deleting all capability instances of resource instance {} statusIs {}", resourceInstanceId, error);
		if (error == null) {
			return getCapabilityInstancesRes;
		}
		return Either.right(error);
	}

	/**
	 * get capability instance of capability for resource instance
	 * 
	 * @param resourceInstanceId
	 * @param capabilityId
	 * @return
	 */
	@Override
	public Either<CapabilityInstData, TitanOperationStatus> getCapabilityInstanceOfCapabilityOfResourceInstance(String resourceInstanceId, String capabilityId) {
		TitanOperationStatus error = null;
		Map<String, Object> props = new HashMap<>();
		props.put(GraphPropertiesDictionary.CAPABILITY_ID.getProperty(), capabilityId);
		Either<ImmutablePair<CapabilityInstData, GraphEdge>, TitanOperationStatus> getCapabilityInstanceRes = titanGenericDao.getChildByEdgeCriteria(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), resourceInstanceId,
				GraphEdgeLabels.CAPABILITY_INST, NodeTypeEnum.CapabilityInst, CapabilityInstData.class, props);
		if (getCapabilityInstanceRes.isRight()) {
			error = getCapabilityInstanceRes.right().value();
			log.debug("Failed to retrieve capability Instance of capability {} ofRI {} statusIs {}.", capabilityId, resourceInstanceId, error);
		}
		if (error == null) {
			return Either.left(getCapabilityInstanceRes.left().value().getLeft());
		}
		return Either.right(error);
	}

	/**
	 * update capability property values
	 * 
	 * @param resourceInstanceId
	 * @param propertyValues
	 * @param capabilityId
	 * @return
	 */
	@Override
	public Either<List<PropertyValueData>, TitanOperationStatus> updateCapabilityPropertyValues(String resourceInstanceId, String capabilityId, List<ComponentInstanceProperty> propertyValues) {
		log.debug("Before updating property values of capability {} ofRI {}.", capabilityId, resourceInstanceId);
		TitanOperationStatus error = null;
		Map<String, Object> props = new HashMap<>();
		CapabilityInstData capabilityInstance = null;
		String capabilityInstanceId = null;
		Either<Boolean, TitanOperationStatus> deleteProperyValuesRes;

		CapabilityData overrideCapabilityData;
		CapabilityDefinition overrideCapabilityDefinition;
		Map<String, PropertyDefinition> defaultProperties = null;
		Either<ImmutablePair<CapabilityData, GraphEdge>, TitanOperationStatus> getCapabilityDataRes = null;
		Either<List<PropertyValueData>, TitanOperationStatus> addPropertyValuesRes = null;
		Either<CapabilityDefinition, TitanOperationStatus> getCapabilityDefinitionRes = null;

		log.debug("Before getting all capability instances of RI {}.", resourceInstanceId);
		props.put(GraphPropertiesDictionary.CAPABILITY_ID.getProperty(), capabilityId);
		Either<ImmutablePair<CapabilityInstData, GraphEdge>, TitanOperationStatus> getCapabilityInstancesRes = titanGenericDao.getChildByEdgeCriteria(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), resourceInstanceId,
				GraphEdgeLabels.CAPABILITY_INST, NodeTypeEnum.CapabilityInst, CapabilityInstData.class, props);
		if (getCapabilityInstancesRes.isRight()) {
			error = getCapabilityInstancesRes.right().value();
			log.debug("Failed to retrieve capability Instances of capability {} ofRI {} statusIs {}.", capabilityId, resourceInstanceId, error);
		}
		log.debug("After getting all capability instances ofRI {} statusIs {}.", resourceInstanceId, error);
		if (error == null) {
			log.debug("Before deleting all capability instances ofRI {}.", resourceInstanceId);
			capabilityInstance = getCapabilityInstancesRes.left().value().getLeft();
			capabilityInstanceId = capabilityInstance.getUniqueId();
			deleteProperyValuesRes = deleteAllPropertyValuesOfCapabilityInstance(resourceInstanceId, capabilityInstanceId);
			if (deleteProperyValuesRes.isRight()) {
				error = deleteProperyValuesRes.right().value();
				log.debug("failedDeletePropertyValues {} forRI {} statusIs {}", capabilityInstanceId, resourceInstanceId, statusIs, error);
			}
			log.debug("After deleting all capability instances ofRI {} statusIs {}.", resourceInstanceId, error);
		}
		if (error == null) {
			log.debug("Before getting capability {} ofRI {}.", capabilityId, resourceInstanceId);
			getCapabilityDataRes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), resourceInstanceId, GraphEdgeLabels.CALCULATED_CAPABILITY, NodeTypeEnum.Capability, CapabilityData.class);
			if (getCapabilityDataRes.isRight()) {
				error = getCapabilityDataRes.right().value();
				log.debug("Failed to get capability {} ofRI {} statusIs {}.", capabilityId, resourceInstanceId, error);
			}
			log.debug("After getting capability {} ofRI {} statusIs {}.", capabilityId, resourceInstanceId, error);
		}
		if (error == null) {
			log.debug("Before getting capability definition for capability {} ofRI {}.", capabilityId, resourceInstanceId);
			overrideCapabilityData = getCapabilityDataRes.left().value().getLeft();
			getCapabilityDefinitionRes = capabilityOperation.getCapabilityByCapabilityData(overrideCapabilityData);
			if (getCapabilityDefinitionRes.isRight()) {
				error = getCapabilityDefinitionRes.right().value();
				log.debug("Failed to retrieve capability {} ofRI {} statusIs {}", capabilityId, resourceInstanceId, error);
			}
			log.debug("After getting capability definition for capability {} ofRI {} statusIs {}.", capabilityId, resourceInstanceId, error);
		}
		if (error == null) {
			log.debug("Before validating capability properties of capability instance {} ofRI {}.", capabilityInstanceId, resourceInstanceId);
			overrideCapabilityDefinition = getCapabilityDefinitionRes.left().value();
			if (overrideCapabilityDefinition.getProperties() != null) {
				defaultProperties = overrideCapabilityDefinition.getProperties().stream().collect(Collectors.toMap(PropertyDefinition::getName, Function.identity()));
			}
			Either<Boolean, TitanOperationStatus> result = validateCapabilityInstanceProperties(defaultProperties, propertyValues);
			if (result.isRight()) {
				error = result.right().value();
				log.debug("failedAddProperties {} ofRI {} statusIs {}.", capabilityInstance.getUniqueId(), resourceInstanceId, error);
			}
			log.debug("After validating capability properties of capability instance {} of RI {} statusIs {}.", capabilityInstanceId, resourceInstanceId, error);
		}
		if (error == null) {
			log.debug("Before adding property values toCI {} ofRI {}.", capabilityInstanceId, resourceInstanceId);
			addPropertyValuesRes = addPropertyValuesToCapabilityInstance(capabilityInstance, propertyValues, defaultProperties);
			if (addPropertyValuesRes.isRight()) {
				error = addPropertyValuesRes.right().value();
				log.debug("failedAddProperties {} ofRI {} statusIs {}.", capabilityInstance.getUniqueId(), resourceInstanceId, error);
			}
			log.debug("Before adding property values toCI {} ofRI {}.", capabilityInstanceId, resourceInstanceId);
		}
		log.debug("After updating property values of capability {} ofRI {} statusIs {}.", capabilityId, resourceInstanceId, error);
		if (error == null) {
			return addPropertyValuesRes;
		}
		return Either.right(error);
	}

	/**
	 * clone and associate capability instance with property values
	 * 
	 * @param createdComponentInstance
	 * @param capability
	 * @param capabilityInstPair
	 * @return
	 */
	@Override
	public Either<ImmutablePair<CapabilityInstData, List<PropertyValueData>>, TitanOperationStatus> cloneAssociateCapabilityInstanceWithPropertyValues(ComponentInstanceData createdComponentInstance, CapabilityDefinition capability,
			ImmutablePair<CapabilityInstData, GraphEdge> capabilityInstPair) {

		TitanOperationStatus error = null;
		String componentInstanceId = createdComponentInstance.getUniqueId();
		String capabilityInstanceId = capabilityInstPair.getLeft().getUniqueId();

		log.debug("Before cloning capability instance with property values of capability instance {} ofRI {}.", capabilityInstanceId, componentInstanceId);
		List<ImmutablePair<PropertyValueData, GraphEdge>> propertyValuePairs;
		List<PropertyValueData> newPropertyValues = new ArrayList<>();
		CapabilityInstData cloneCapabilityInstance = null;
		Either<CapabilityInstData, TitanOperationStatus> cloneCapabilityInstanceNodeRes = null;

		log.debug("Before getting all property values ofCI {} ofRI {}.", capabilityInstanceId, componentInstanceId);
		Either<List<ImmutablePair<PropertyValueData, GraphEdge>>, TitanOperationStatus> getPropertyValuesRes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.CapabilityInst), capabilityInstPair.getLeft().getUniqueId(),
				GraphEdgeLabels.PROPERTY_VALUE, NodeTypeEnum.PropertyValue, PropertyValueData.class);
		if (getPropertyValuesRes.isRight()) {
			error = getPropertyValuesRes.right().value();
			log.debug("Failed to retrieve property values of capability instance {} ofCI {} statusIs {}.", capabilityInstPair.getLeft().getUniqueId(), componentInstanceId ,error);
		}
		log.debug("After getting all property values ofCI {} ofRI {} statusIs {}.", capabilityInstanceId, componentInstanceId, error);
		if (error == null) {
			CapabilityInstData cloneCapabilityInst = buildCapabilityInstanceData(componentInstanceId, capability);
			log.debug("Before creating capability instance node {} onGraph.", cloneCapabilityInst.getUniqueId());
			cloneCapabilityInstanceNodeRes = titanGenericDao.createNode(cloneCapabilityInst, CapabilityInstData.class);
			if (cloneCapabilityInstanceNodeRes.isRight()) {
				error = cloneCapabilityInstanceNodeRes.right().value();
				log.debug("Failed to create capability instance of capability {} ofCI {} statusIs {}.", capability.getUniqueId(), componentInstanceId, error);
			}
			log.debug("After creating capability instance node {} onGraph. statusIs {}", cloneCapabilityInst.getUniqueId(), error);
		}

		if (error == null) {
			log.debug("Before creating relation from capability instance {} toCapability {} onGraph.", cloneCapabilityInstanceNodeRes.left().value().getUniqueId(), capability.getUniqueId());
			cloneCapabilityInstance = cloneCapabilityInstanceNodeRes.left().value();
			CapabilityData capabilityData = buildCapabilityData(capability);
			Map<String, Object> props = new HashMap<>();
			props.put(GraphPropertiesDictionary.CAPABILITY_ID.getProperty(), capabilityData.getUniqueId());
			Either<GraphRelation, TitanOperationStatus> createRelationRes = titanGenericDao.createRelation(cloneCapabilityInstance, capabilityData, GraphEdgeLabels.INSTANCE_OF, props);
			if (createRelationRes.isRight()) {
				error = createRelationRes.right().value();
				log.debug("Failed to associate capability instance {} toCapability {} statusIs {}.", cloneCapabilityInstance.getUniqueId(), capability.getUniqueId(), error);
			}
			log.debug("After creating relation from capability instance {} toCapability {} onGraph. statusIs {}.", cloneCapabilityInstanceNodeRes.left().value().getUniqueId(), capability.getUniqueId(), error);
		}

		if (error == null) {
			log.debug("Before cloning property values ofCI {}.", capabilityInstanceId);
			propertyValuePairs = getPropertyValuesRes.left().value();
			for (ImmutablePair<PropertyValueData, GraphEdge> propertyValuePair : propertyValuePairs) {
				Either<PropertyValueData, TitanOperationStatus> clonePropertyValueRes = cloneAssociatePropertyValue(cloneCapabilityInstance, propertyValuePair);
				if (clonePropertyValueRes.isRight()) {
					error = clonePropertyValueRes.right().value();
					if (log.isDebugEnabled()) {
						log.debug("Failed to clone property value {} ofCapability {} ofCI {}. statusIs {}.", propertyValuePair.getLeft().getUniqueId(), capability.getUniqueId(), componentInstanceId, error);
					}
					break;
				} else {
					newPropertyValues.add(clonePropertyValueRes.left().value());
				}
			}
			log.debug("After cloning property values of CI {} statusIs {}.", capabilityInstanceId, error);
		}
		log.debug("After cloning capability instance with property values of capability instance {} ofRI {} statusIs {}.", capabilityInstanceId, componentInstanceId, error);
		if (error == null) {
			return Either.left(new ImmutablePair<CapabilityInstData, List<PropertyValueData>>(cloneCapabilityInstance, newPropertyValues));
		}
		return Either.right(error);
	}

	public Either<TitanVertex, TitanOperationStatus> cloneAssociateCapabilityInstanceWithPropertyValues(TitanVertex componentInstanceVertex, CapabilityDefinition capability, ImmutablePair<CapabilityInstData, GraphEdge> capabilityInstPair) {

		TitanOperationStatus error = null;
		String componentInstanceId = (String) titanGenericDao.getProperty(componentInstanceVertex, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
		String capabilityInstanceId = capabilityInstPair.getLeft().getUniqueId();

		if (log.isTraceEnabled()) {
			log.trace("Before cloning capability instance with property values of capability instance {} {} {}", capabilityInstanceId, ofRI, componentInstanceId);
		}
		List<ImmutablePair<TitanVertex, Edge>> propertyValuePairs;
		Either<TitanVertex, TitanOperationStatus> cloneCapabilityInstanceNodeRes = null;

		if (log.isTraceEnabled()) {
			log.trace("Before getting all property values {} {} {} {}", ofCI, capabilityInstanceId, ofRI, componentInstanceId);
		}
		Either<List<ImmutablePair<TitanVertex, Edge>>, TitanOperationStatus> getPropertyValuesRes = titanGenericDao.getChildrenVertecies(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.CapabilityInst), capabilityInstPair.getLeft().getUniqueId(),
				GraphEdgeLabels.PROPERTY_VALUE);
		if (getPropertyValuesRes.isRight()) {
			error = getPropertyValuesRes.right().value();
			if (log.isDebugEnabled()) {
				log.debug("Failed to retrieve property values of capability instance {} {} {} {} {}", capabilityInstPair.getLeft().getUniqueId(), ofCI, componentInstanceId, statusIs, error);
			}
		}
		if (log.isTraceEnabled()) {
			log.trace("After getting all property values {} {} {} {} {} {}", ofCI, capabilityInstanceId, ofRI, componentInstanceId, statusIs, error);
		}
		if (error == null) {
			CapabilityInstData cloneCapabilityInst = buildCapabilityInstanceData(componentInstanceId, capability);
			log.trace("Before creating capability instance node {} {} ", cloneCapabilityInst.getUniqueId(), onGraph);
			cloneCapabilityInstanceNodeRes = titanGenericDao.createNode(cloneCapabilityInst);
			if (cloneCapabilityInstanceNodeRes.isRight()) {
				error = cloneCapabilityInstanceNodeRes.right().value();
				if (log.isDebugEnabled()) {
					log.debug("Failed to create capability instance of capability {} {} {} {} {}", capability.getUniqueId(), ofCI, componentInstanceId, statusIs, error);
				}
			}
			if (log.isTraceEnabled()) {
				log.trace("After creating capability instance node {} {} {} {}", cloneCapabilityInst.getUniqueId(), onGraph, statusIs, error);
			}
		}
		CapabilityData capabilityData;
		TitanVertex cloneCapabilityInstance = null;
		if (error == null) {
			if (log.isTraceEnabled()) {
				log.trace("Before creating relation from capability instance {} {} {} {}", capability.getUniqueId(), toCapability, capability.getUniqueId(), onGraph);
			}
			capabilityData = buildCapabilityData(capability);
			Map<String, Object> props = new HashMap<>();
			props.put(GraphPropertiesDictionary.CAPABILITY_ID.getProperty(), capabilityData.getUniqueId());
			cloneCapabilityInstance = cloneCapabilityInstanceNodeRes.left().value();
			TitanOperationStatus createRelationRes = titanGenericDao.createEdge(cloneCapabilityInstance, capabilityData, GraphEdgeLabels.INSTANCE_OF, props);
			if (!createRelationRes.equals(TitanOperationStatus.OK)) {
				error = createRelationRes;
				if (log.isDebugEnabled()) {
					log.debug("Failed to associate capability instance {} {} {} {} {}", capabilityData.getUniqueId(), toCapability, capability.getUniqueId(), statusIs, createRelationRes);
				}
			}
			if (log.isTraceEnabled()) {
				log.trace("After creating relation from capability instance {} {} {} {} {} {}", capabilityData.getUniqueId(), toCapability, capability.getUniqueId(), onGraph, statusIs, error);
			}
		}

		if (error == null) {
			log.trace("Before cloning property values {} {} ", ofCI, capabilityInstanceId);
			propertyValuePairs = getPropertyValuesRes.left().value();
			for (ImmutablePair<TitanVertex, Edge> propertyValuePair : propertyValuePairs) {
				TitanOperationStatus clonePropertyValueRes = cloneAssociatePropertyValue(cloneCapabilityInstance, propertyValuePair);
				if (!clonePropertyValueRes.equals(TitanOperationStatus.OK)) {
					error = clonePropertyValueRes;
					if (log.isDebugEnabled()) {
						log.debug("Failed to clone property value  of capability {} {} {} {} {}", capability.getUniqueId(), ofCI, componentInstanceId, statusIs, error);
					}
					break;
				}
			}
			if (log.isDebugEnabled()) {
				log.debug("After cloning property values {} {} {} {}", ofCI, capabilityInstanceId, statusIs, error);
			}
		}
		log.debug("After cloning capability instance with property values of capability instance {} ofRI {} statusIs {}.", capabilityInstanceId, componentInstanceId, error);
		if (error == null) {
			return Either.left(cloneCapabilityInstance);
		}
		return Either.right(error);
	}

	private CapabilityData buildCapabilityData(CapabilityDefinition capability) {
		CapabilityData capabilityData = new CapabilityData();
		capabilityData.setUniqueId(capability.getUniqueId());
		capabilityData.setDescription(capability.getDescription());
		capabilityData.setType(capability.getType());
		capabilityData.setMaxOccurrences(capability.getMaxOccurrences());
		capabilityData.setMinOccurrences(capability.getMinOccurrences());
		List<String> validSourceTypes = capability.getValidSourceTypes();
		if (validSourceTypes != null) {
			capabilityData.setValidSourceTypes(validSourceTypes);
		}
		return capabilityData;
	}

	private Either<PropertyValueData, TitanOperationStatus> cloneAssociatePropertyValue(CapabilityInstData cloneCapabilityInstance, ImmutablePair<PropertyValueData, GraphEdge> propertyValuePair) {
		TitanOperationStatus error = null;
		String propertyValueID = propertyValuePair.getLeft().getUniqueId();
		String capabilityInstanceId = cloneCapabilityInstance.getUniqueId();
		log.debug("Before cloning property values {} ofCI {}.", propertyValueID, capabilityInstanceId);

		Map<String, Object> props = propertyValuePair.getRight().getProperties();
		PropertyData propertyData = new PropertyData();
		String propertyId = (String) props.get(GraphPropertiesDictionary.PROPERTY_ID.name());
		propertyData.getPropertyDataDefinition().setUniqueId(propertyId);

		PropertyValueData propertyValue = buildPropertyValueData((String) props.get(GraphPropertiesDictionary.PROPERTY_NAME.name()), propertyValuePair.getLeft().getType(), propertyValuePair.getLeft().getValue(), capabilityInstanceId);
		PropertyValueData createdValue = null;
		Either<GraphRelation, TitanOperationStatus> createRelationRes;

		log.debug("Before creating property values node {} onGraph.", propertyValue.getUniqueId());
		Either<PropertyValueData, TitanOperationStatus> createValueRes = titanGenericDao.createNode(propertyValue, PropertyValueData.class);
		if (createValueRes.isRight()) {
			error = createValueRes.right().value();
			log.debug("Failed to create property value for capability instance {} ofRI. statusIs {}.", cloneCapabilityInstance.getUniqueId(), error);
		}
		log.debug("After creating property values node {} onGraph. statusIs {}.", propertyValue.getUniqueId(), error);
		if (error == null) {
			createdValue = createValueRes.left().value();
			log.debug("Before creating relation from capability instance {} toValue {}.", capabilityInstanceId, createdValue.getUniqueId());
			createRelationRes = titanGenericDao.createRelation(cloneCapabilityInstance, createdValue, GraphEdgeLabels.PROPERTY_VALUE, props);
			if (createRelationRes.isRight()) {
				error = createRelationRes.right().value();
				log.debug("Failed to create relation from capability instance {} toValue {} statusIs {}.", cloneCapabilityInstance.getUniqueId(), createdValue.getUniqueId(), error);
			}
			log.debug("After creating relation from capability instance {} toValue {} statusIs {}", capabilityInstanceId, createdValue.getUniqueId(), error);
		}
		if (error == null) {
			log.debug("Before creating relation from property value {} toProperty {}.", createdValue, propertyData.getUniqueId());
			createRelationRes = titanGenericDao.createRelation(createdValue, propertyData, GraphEdgeLabels.PROPERTY_IMPL, props);
			if (createRelationRes.isRight()) {
				error = createRelationRes.right().value();
				log.debug("Failed to create relation from property value {} toProperty {} statusIs {}.", createdValue.getUniqueId(), propertyId, error);
			}
			log.debug("Before creating relation from property value {} toProperty {} statusIs {}.", createdValue, propertyData.getUniqueId(), error);
		}
		log.debug("After cloning property values {} ofCI {} statusIs {}.", propertyValueID, capabilityInstanceId, error);
		if (error == null) {
			return Either.left(createdValue);
		}
		return Either.right(error);
	}

	private TitanOperationStatus cloneAssociatePropertyValue(TitanVertex capabilityInstanceVertex, ImmutablePair<TitanVertex, Edge> propertyValuePair) {
		TitanOperationStatus error = null;
		TitanVertex propertyVertex = propertyValuePair.getLeft();
		String propertyValueID = (String) titanGenericDao.getProperty(propertyVertex, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
		String capabilityInstanceId = (String) titanGenericDao.getProperty(capabilityInstanceVertex, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
		if (log.isTraceEnabled()) {
			log.trace("Before cloning property values {} {} {}", propertyValueID, ofCI, capabilityInstanceId);
		}

		Map<String, Object> props = titanGenericDao.getProperties(propertyValuePair.getRight());
		PropertyData propertyData = new PropertyData();
		String propertyId = (String) props.get(GraphPropertiesDictionary.PROPERTY_ID.name());
		propertyData.getPropertyDataDefinition().setUniqueId(propertyId);

		String propertyType = (String) titanGenericDao.getProperty(propertyVertex, GraphPropertiesDictionary.TYPE.getProperty());
		String propertyValueStr = (String) titanGenericDao.getProperty(propertyVertex, GraphPropertiesDictionary.VALUE.getProperty());

		PropertyValueData propertyValue = buildPropertyValueData((String) props.get(GraphPropertiesDictionary.PROPERTY_NAME.name()), propertyType, propertyValueStr, capabilityInstanceId);
		TitanVertex createdValue = null;
		TitanOperationStatus createRelationRes;

		log.trace("Before creating property values node {} {} ", propertyValue.getUniqueId(), onGraph);
		Either<TitanVertex, TitanOperationStatus> createValueRes = titanGenericDao.createNode(propertyValue);
		String capabiltyInstId = (String) titanGenericDao.getProperty(capabilityInstanceVertex, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
		if (createValueRes.isRight()) {
			error = createValueRes.right().value();
			if (log.isDebugEnabled()) {
				log.debug("Failed to create property value for capability instance {} {} {} {}", capabiltyInstId, ofRI, statusIs, error);
			}
		}
		if (log.isTraceEnabled()) {
			log.trace("After creating property values node {} {} {} {} ", propertyValue.getUniqueId(), onGraph, statusIs, error);
		}
		if (error == null) {
			createdValue = createValueRes.left().value();
			log.trace("Before creating relation from capability instance {} {} {}", capabilityInstanceId, toValue, propertyValue.getUniqueId());
			createRelationRes = titanGenericDao.createEdge(capabilityInstanceVertex, createdValue, GraphEdgeLabels.PROPERTY_VALUE, props);
			if (!createRelationRes.equals(TitanOperationStatus.OK)) {
				error = createRelationRes;
				if (log.isDebugEnabled()) {
					log.debug("Failed to create relation from capability instance {} {} {} {} {}", capabiltyInstId, toValue, propertyValue.getUniqueId(), statusIs, error);
				}
			}
			if (log.isTraceEnabled()) {
				log.trace("After creating relation from capability instance {} {} {} {} {} ", capabilityInstanceId, toValue, propertyValue.getUniqueId(), statusIs, error);
			}
		}
		if (error == null) {
			log.trace("Before creating relation from property value {} {} {} ", createdValue, toProperty, propertyData.getUniqueId());
			createRelationRes = titanGenericDao.createEdge(createdValue, propertyData, GraphEdgeLabels.PROPERTY_IMPL, props);
			if (!createRelationRes.equals(TitanOperationStatus.OK)) {
				error = createRelationRes;
				if (log.isDebugEnabled()) {
					log.debug("Failed to create relation from property value {} {} {} {} {}", propertyValue.getUniqueId(), toProperty, propertyId, statusIs, error);
				}
			}
			if (log.isTraceEnabled()) {
				log.trace("Before creating relation from property value c", createdValue, toProperty, propertyData.getUniqueId(), statusIs, error);
			}
		}
		if (log.isTraceEnabled()) {
			log.trace("After cloning property values {} {} {} {} {}", propertyValueID, ofCI, capabilityInstanceId, statusIs, error);
		}
		if (error == null) {
			return TitanOperationStatus.OK;
		}
		return error;
	}
}
