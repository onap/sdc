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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.heat.HeatParameterType;
import org.openecomp.sdc.be.model.operations.api.IHeatParametersOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.openecomp.sdc.be.model.tosca.validators.PropertyTypeValidator;
import org.openecomp.sdc.be.resources.data.HeatParameterData;
import org.openecomp.sdc.be.resources.data.HeatParameterValueData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import fj.data.Either;

@Component("heat-parameter-operation")
public class HeatParametersOperation implements IHeatParametersOperation {

	public static final String EMPTY_VALUE = null;

	private static Logger log = LoggerFactory.getLogger(HeatParametersOperation.class.getName());

	@javax.annotation.Resource
	private TitanGenericDao titanGenericDao;

	private Gson gson = new Gson();

	public TitanGenericDao getTitanGenericDao() {
		return titanGenericDao;
	}

	public void setTitanGenericDao(TitanGenericDao titanGenericDao) {
		this.titanGenericDao = titanGenericDao;
	}

	public StorageOperationStatus getHeatParametersOfNode(NodeTypeEnum nodeType, String uniqueId, List<HeatParameterDefinition> properties) {

		Either<List<ImmutablePair<HeatParameterData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(nodeType), uniqueId, GraphEdgeLabels.HEAT_PARAMETER, NodeTypeEnum.HeatParameter,
				HeatParameterData.class);

		if (childrenNodes.isRight()) {
			TitanOperationStatus status = childrenNodes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.OK;
			}
			return DaoStatusConverter.convertTitanStatusToStorageStatus(status);
		}

		List<ImmutablePair<HeatParameterData, GraphEdge>> values = childrenNodes.left().value();
		if (values != null) {

			for (ImmutablePair<HeatParameterData, GraphEdge> immutablePair : values) {
				GraphEdge edge = immutablePair.getValue();
				String propertyName = (String) edge.getProperties().get(GraphPropertiesDictionary.NAME.getProperty());
				if (log.isDebugEnabled())
					log.debug("Property {} is associated to node {}", propertyName, uniqueId);
				HeatParameterData propertyData = immutablePair.getKey();
				HeatParameterDefinition propertyDefinition = convertParameterDataToParameterDefinition(propertyData, propertyName, uniqueId);

				properties.add(propertyDefinition);

				if (log.isTraceEnabled()) {
					log.trace("getHeatParametersOfNode - property {} associated to node {}", propertyDefinition, uniqueId);
				}
			}

		}

		return StorageOperationStatus.OK;
	}

	public StorageOperationStatus getParametersValueNodes(NodeTypeEnum parentNodeType, String parentUniqueId, List<HeatParameterValueData> heatValues) {

		Either<List<ImmutablePair<HeatParameterValueData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(parentNodeType), parentUniqueId, GraphEdgeLabels.PARAMETER_VALUE,
				NodeTypeEnum.HeatParameterValue, HeatParameterValueData.class);

		if (childrenNodes.isRight()) {
			TitanOperationStatus status = childrenNodes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.OK;
			}
			return DaoStatusConverter.convertTitanStatusToStorageStatus(status);
		}

		List<ImmutablePair<HeatParameterValueData, GraphEdge>> values = childrenNodes.left().value();
		if (values != null) {

			for (ImmutablePair<HeatParameterValueData, GraphEdge> immutablePair : values) {
				GraphEdge edge = immutablePair.getValue();
				String propertyName = (String) edge.getProperties().get(GraphPropertiesDictionary.NAME.getProperty());
				log.trace("Heat value {} is associated to node {}", propertyName,parentUniqueId);
				HeatParameterValueData propertyData = immutablePair.getKey();

				heatValues.add(propertyData);
			}

		}

		return StorageOperationStatus.OK;
	}

	@Override
	public Either<List<HeatParameterDefinition>, StorageOperationStatus> deleteAllHeatParametersAssociatedToNode(NodeTypeEnum nodeType, String uniqueId) {

		List<HeatParameterDefinition> heatParams = new ArrayList<HeatParameterDefinition>();
		StorageOperationStatus propertiesOfNodeRes = getHeatParametersOfNode(nodeType, uniqueId, heatParams);

		if (!propertiesOfNodeRes.equals(StorageOperationStatus.OK) && !propertiesOfNodeRes.equals(StorageOperationStatus.NOT_FOUND)) {
			return Either.right(propertiesOfNodeRes);
		}

		for (HeatParameterDefinition propertyDefinition : heatParams) {

			String propertyUid = propertyDefinition.getUniqueId();
			Either<HeatParameterData, TitanOperationStatus> deletePropertyRes = deleteHeatParameterFromGraph(propertyUid);
			if (deletePropertyRes.isRight()) {
				log.error("Failed to delete heat parameter with id {}", propertyUid);
				TitanOperationStatus status = deletePropertyRes.right().value();
				if (status == TitanOperationStatus.NOT_FOUND) {
					status = TitanOperationStatus.INVALID_ID;
				}
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}

		}

		log.debug("The heat parameters deleted from node {} are {}", uniqueId, heatParams);
		return Either.left(heatParams);
	}

	@Override
	public StorageOperationStatus deleteAllHeatValuesAssociatedToNode(NodeTypeEnum parentNodeType, String parentUniqueId) {

		List<HeatParameterValueData> heatValues = new ArrayList<HeatParameterValueData>();
		StorageOperationStatus propertiesOfNodeRes = getParametersValueNodes(parentNodeType, parentUniqueId, heatValues);

		if (!propertiesOfNodeRes.equals(StorageOperationStatus.OK) && !propertiesOfNodeRes.equals(StorageOperationStatus.NOT_FOUND)) {
			return propertiesOfNodeRes;
		}

		for (HeatParameterValueData propertyDefinition : heatValues) {

			String propertyUid = (String) propertyDefinition.getUniqueId();
			Either<HeatParameterValueData, TitanOperationStatus> deletePropertyRes = deleteHeatParameterValueFromGraph(propertyUid);
			if (deletePropertyRes.isRight()) {
				log.error("Failed to delete heat parameter value with id {}", propertyUid);
				TitanOperationStatus status = deletePropertyRes.right().value();
				if (status == TitanOperationStatus.NOT_FOUND) {
					status = TitanOperationStatus.INVALID_ID;
				}
				return DaoStatusConverter.convertTitanStatusToStorageStatus(status);
			}

		}

		log.debug("The heat values deleted from node {} are {}" , parentUniqueId, heatValues);
		return StorageOperationStatus.OK;
	}

	private Either<HeatParameterData, TitanOperationStatus> deleteHeatParameterFromGraph(String propertyId) {
		log.debug("Before deleting heat parameter from graph {}" , propertyId);
		return titanGenericDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.HeatParameter), propertyId, HeatParameterData.class);
	}

	private Either<HeatParameterValueData, TitanOperationStatus> deleteHeatParameterValueFromGraph(String propertyId) {
		log.debug("Before deleting heat parameter from graph {}" , propertyId);
		return titanGenericDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.HeatParameterValue), propertyId, HeatParameterValueData.class);
	}

	@Override
	public StorageOperationStatus addPropertiesToGraph(List<HeatParameterDefinition> properties, String parentId, NodeTypeEnum nodeType) {

		if (properties != null) {
			for (HeatParameterDefinition propertyDefinition : properties) {

				String propertyName = propertyDefinition.getName();

				Either<HeatParameterData, TitanOperationStatus> addPropertyToGraph = addPropertyToGraph(propertyName, propertyDefinition, parentId, nodeType);

				if (addPropertyToGraph.isRight()) {
					return DaoStatusConverter.convertTitanStatusToStorageStatus(addPropertyToGraph.right().value());
				}
			}
		}

		return StorageOperationStatus.OK;

	}

	@Override
	public StorageOperationStatus updateHeatParameters(List<HeatParameterDefinition> properties) {

		if (properties == null) {
			return StorageOperationStatus.OK;
		}
		for (HeatParameterDefinition property : properties) {

			HeatParameterData heatParameterData = new HeatParameterData(property);
			Either<HeatParameterData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(heatParameterData, HeatParameterData.class);
			if (updateNode.isRight()) {
				log.debug("failed to update heat parameter in graph. id = {}", property.getUniqueId());
				return DaoStatusConverter.convertTitanStatusToStorageStatus(updateNode.right().value());
			}
		}

		return StorageOperationStatus.OK;
	}

	public Either<HeatParameterData, TitanOperationStatus> addPropertyToGraph(String propertyName, HeatParameterDefinition propertyDefinition, String parentId, NodeTypeEnum nodeType) {

		UniqueIdData parentNode = new UniqueIdData(nodeType, parentId);

		propertyDefinition.setUniqueId(UniqueIdBuilder.buildHeatParameterUniqueId(parentId, propertyName));
		HeatParameterData propertyData = new HeatParameterData(propertyDefinition);

		log.debug("Before adding property to graph {}" , propertyData);
		Either<HeatParameterData, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(propertyData, HeatParameterData.class);
		log.debug("After adding property to graph {}" , propertyData);
		if (createNodeResult.isRight()) {
			TitanOperationStatus operationStatus = createNodeResult.right().value();
			log.error("Failed to add property {} to graph. status is {}", propertyName, operationStatus);
			return Either.right(operationStatus);
		}

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), propertyName);
		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(parentNode, propertyData, GraphEdgeLabels.HEAT_PARAMETER, props);
		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createRelResult.right().value();
			log.error("Failed to associate {} {} to heat parameter {} in graph. status is {}", nodeType.getName(), parentId, propertyName, operationStatus);
			return Either.right(operationStatus);
		}

		return Either.left(createNodeResult.left().value());

	}

	public StorageOperationStatus validateAndUpdateProperty(HeatParameterDefinition propertyDefinition) {

		log.trace("Going to validate property type and value. {}" , propertyDefinition);

		String propertyType = propertyDefinition.getType();
		HeatParameterType type = getType(propertyType);

		if (type == null) {
			log.info("The type {} of heat parameter is invalid", type);

			return StorageOperationStatus.INVALID_TYPE;
		}
		propertyDefinition.setType(type.getType());

		log.trace("After validating property type {}", propertyType);

		// validate default value
		String defaultValue = propertyDefinition.getDefaultValue();
		boolean isValidProperty = isValidValue(type, defaultValue);
		if (false == isValidProperty) {
			log.info("The value {} of property from type {} is invalid", defaultValue, type);
			return StorageOperationStatus.INVALID_VALUE;
		}

		PropertyValueConverter converter = type.getConverter();

		if (isEmptyValue(defaultValue)) {
			log.debug("Default value was not sent for property {}. Set default value to {}", propertyDefinition.getName() , EMPTY_VALUE);
					
			propertyDefinition.setDefaultValue(EMPTY_VALUE);
		} else if (false == isEmptyValue(defaultValue)) {
			String convertedValue = converter.convert(defaultValue, null, null);
			propertyDefinition.setDefaultValue(convertedValue);
		}

		// validate current value
		String value = propertyDefinition.getCurrentValue();
		isValidProperty = isValidValue(type, value);
		if (false == isValidProperty) {
			log.info("The value {} of property from type {} is invalid", value, type);
			return StorageOperationStatus.INVALID_VALUE;
		}

		if (isEmptyValue(value)) {
			log.debug("Value was not sent for property {}. Set value to {}", propertyDefinition.getName(), EMPTY_VALUE);
					
			propertyDefinition.setCurrentValue(EMPTY_VALUE);
		} else if (!value.equals("")) {
			String convertedValue = converter.convert(value, null, null);
			propertyDefinition.setCurrentValue(convertedValue);
		}

		return StorageOperationStatus.OK;
	}

	public HeatParameterDefinition convertParameterDataToParameterDefinition(HeatParameterData propertyDataResult, String propertyName, String resourceId) {
		log.debug("convert to HeatParamereDefinition {}", propertyDataResult);

		HeatParameterDefinition propertyDefResult = new HeatParameterDefinition(propertyDataResult.getHeatDataDefinition());

		propertyDefResult.setName(propertyName);

		return propertyDefResult;
	}

	private HeatParameterType getType(String propertyType) {

		HeatParameterType type = HeatParameterType.isValidType(propertyType);

		return type;

	}

	protected boolean isValidValue(HeatParameterType type, String value) {
		if (isEmptyValue(value)) {
			return true;
		}

		PropertyTypeValidator validator = type.getValidator();

		boolean isValid = validator.isValid(value, null, null);
		if (true == isValid) {
			return true;
		} else {
			return false;
		}

	}

	public boolean isEmptyValue(String value) {
		if (value == null) {
			return true;
		}
		return false;
	}

	public boolean isNullParam(String value) {
		if (value == null) {
			return true;
		}
		return false;
	}

	@Override
	public Either<HeatParameterValueData, StorageOperationStatus> updateHeatParameterValue(HeatParameterDefinition heatParam, String artifactId, String resourceInstanceId, String artifactLabel) {
		String heatEnvId = UniqueIdBuilder.buildHeatParameterValueUniqueId(resourceInstanceId, artifactLabel, heatParam.getName());
		Either<HeatParameterValueData, TitanOperationStatus> getNode = titanGenericDao.getNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), heatEnvId, HeatParameterValueData.class);
		if (getNode.isRight() || getNode.left().value() == null) {
			if (heatParam.getCurrentValue() == null || (heatParam.getDefaultValue() != null && heatParam.getCurrentValue().equals(heatParam.getDefaultValue()))) {
				log.debug("Updated heat parameter value equals default value. No need to create heat parameter value for heat parameter {}", heatParam.getUniqueId());
				return Either.left(null);
			}
			return createHeatParameterValue(heatParam, artifactId, resourceInstanceId, artifactLabel);
		} else {
			heatParam.setUniqueId(heatEnvId);
			return updateHeatParameterValue(heatParam);
		}
	}

	public Either<HeatParameterValueData, StorageOperationStatus> updateHeatParameterValue(HeatParameterDefinition heatParam) {
		HeatParameterValueData heatParameterValue = new HeatParameterValueData();
		heatParameterValue.setUniqueId(heatParam.getUniqueId());
		if (heatParam.getCurrentValue() == null || (heatParam.getDefaultValue() != null && heatParam.getCurrentValue().equals(heatParam.getDefaultValue()))) {
			Either<GraphRelation, TitanOperationStatus> deleteParameterValueIncomingRelation = titanGenericDao.deleteIncomingRelationByCriteria(heatParameterValue, GraphEdgeLabels.PARAMETER_VALUE, null);
			if (deleteParameterValueIncomingRelation.isRight()) {
				log.debug("Failed to delete heat parameter value incoming relation on graph. id = {}", heatParameterValue.getUniqueId());
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(deleteParameterValueIncomingRelation.right().value()));
			}
			Either<Edge, TitanOperationStatus> getOutgoingRelation = titanGenericDao.getOutgoingEdgeByCriteria(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), (String) heatParameterValue.getUniqueId(), GraphEdgeLabels.PARAMETER_IMPL, null);
			if (getOutgoingRelation.isRight()) {
				log.debug("Failed to get heat parameter value outgoing relation from graph. id = {}", heatParameterValue.getUniqueId());
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getOutgoingRelation.right().value()));
			}
			Edge edge = getOutgoingRelation.left().value();
			if (edge == null) {
				log.debug("Failed to get heat parameter value outgoing relation from graph. id = {}", heatParameterValue.getUniqueId());
				return Either.right(StorageOperationStatus.GENERAL_ERROR);
			}
			edge.remove();

			Either<HeatParameterValueData, TitanOperationStatus> deleteNode = titanGenericDao.deleteNode(heatParameterValue, HeatParameterValueData.class);
			if (deleteNode.isRight()) {
				log.debug("Failed to delete heat parameter value on graph. id = {}", heatParameterValue.getUniqueId());
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(deleteNode.right().value()));
			}
			return Either.left(deleteNode.left().value());
		}
		heatParameterValue.setValue(heatParam.getCurrentValue());
		Either<HeatParameterValueData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(heatParameterValue, HeatParameterValueData.class);
		if (updateNode.isRight()) {
			log.debug("Failed to update heat parameter value in graph. id = {}", heatParameterValue.getUniqueId());
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(updateNode.right().value()));
		}
		return Either.left(updateNode.left().value());
	}

	public Either<HeatParameterValueData, StorageOperationStatus> createHeatParameterValue(HeatParameterDefinition heatParam, String artifactId, String resourceInstanceId, String artifactLabel) {

		Either<HeatParameterValueData, TitanOperationStatus> addHeatValueToGraph = addHeatValueToGraph(heatParam, artifactLabel, artifactId, resourceInstanceId);
		if (addHeatValueToGraph.isRight()) {
			log.debug("Failed to create heat parameters value on graph for artifact {}", artifactId);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(addHeatValueToGraph.right().value()));
		}
		return Either.left(addHeatValueToGraph.left().value());
	}

	public Either<HeatParameterValueData, TitanOperationStatus> addHeatValueToGraph(HeatParameterDefinition heatParameter, String artifactLabel, String artifactId, String resourceInstanceId) {

		UniqueIdData heatEnvNode = new UniqueIdData(NodeTypeEnum.ArtifactRef, artifactId);
		HeatParameterValueData heatValueData = new HeatParameterValueData();
		heatValueData.setUniqueId(UniqueIdBuilder.buildHeatParameterValueUniqueId(resourceInstanceId, artifactLabel, heatParameter.getName()));
		heatValueData.setValue(heatParameter.getCurrentValue());

		log.debug("Before adding property to graph {}", heatValueData);
		Either<HeatParameterValueData, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(heatValueData, HeatParameterValueData.class);
		log.debug("After adding property to graph {}", heatValueData);
		if (createNodeResult.isRight()) {
			TitanOperationStatus operationStatus = createNodeResult.right().value();
			log.error("Failed to add heat value {} to graph. status is {}", heatValueData.getUniqueId(), operationStatus);
			return Either.right(operationStatus);
		}

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), heatParameter.getName());
		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(heatEnvNode, heatValueData, GraphEdgeLabels.PARAMETER_VALUE, props);
		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createRelResult.right().value();
			log.error("Failed to associate heat value {} to heat env artifact {} in graph. status is {}", heatValueData.getUniqueId(), artifactId, operationStatus);
			return Either.right(operationStatus);
		}
		UniqueIdData heatParameterNode = new UniqueIdData(NodeTypeEnum.HeatParameter, heatParameter.getUniqueId());
		Either<GraphRelation, TitanOperationStatus> createRel2Result = titanGenericDao.createRelation(heatValueData, heatParameterNode, GraphEdgeLabels.PARAMETER_IMPL, null);
		if (createRel2Result.isRight()) {
			TitanOperationStatus operationStatus = createRel2Result.right().value();
			log.error("Failed to associate heat value {} to heat parameter {} in graph. status is {}", heatValueData.getUniqueId(), heatParameter.getName(), operationStatus);
			return Either.right(operationStatus);
		}

		return Either.left(createNodeResult.left().value());

	}

}
