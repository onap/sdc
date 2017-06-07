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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.IComplexDefaultValue;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation.PropertyConstraintDeserialiser;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.openecomp.sdc.be.model.tosca.validators.DataTypeValidatorConverter;
import org.openecomp.sdc.be.model.tosca.validators.PropertyTypeValidator;
import org.openecomp.sdc.be.resources.data.CapabilityInstData;
import org.openecomp.sdc.be.resources.data.PropertyValueData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

public abstract class AbstractOperation {
	private static Logger log = LoggerFactory.getLogger(AbstractOperation.class.getName());
	@javax.annotation.Resource
	protected TitanGenericDao titanGenericDao;
	public static final String EMPTY_VALUE = null;

	protected Gson gson = new Gson();

	@javax.annotation.Resource
	protected ApplicationDataTypeCache applicationDataTypeCache;

	protected DataTypeValidatorConverter dataTypeValidatorConverter = DataTypeValidatorConverter.getInstance();

	protected <SomeData extends GraphNode, SomeDefenition> Either<SomeData, TitanOperationStatus> addDefinitionToNodeType(SomeDefenition someDefinition, NodeTypeEnum nodeType, String nodeUniqueId, final GraphEdgeLabels edgeType,
			Supplier<SomeData> dataBuilder, Supplier<String> defNameGenerator) {
		String defName = defNameGenerator.get();
		log.debug("Got {} {}", defName, someDefinition);

		SomeData someData = dataBuilder.get();

		log.debug("Before adding {} to graph. data = {}", defName, someData);

		@SuppressWarnings("unchecked")
		Either<SomeData, TitanOperationStatus> eitherSomeData = titanGenericDao.createNode(someData, (Class<SomeData>) someData.getClass());

		log.debug("After adding {} to graph. status is = {}", defName, eitherSomeData);

		if (eitherSomeData.isRight()) {
			TitanOperationStatus operationStatus = eitherSomeData.right().value();
			log.error("Failed to add {}  to graph. status is {}", defName, operationStatus);
			return Either.right(operationStatus);
		}
		UniqueIdData uniqueIdData = new UniqueIdData(nodeType, nodeUniqueId);
		log.debug("Before associating {} to {}.", uniqueIdData, defName);

		Either<GraphRelation, TitanOperationStatus> eitherRelations = titanGenericDao.createRelation(uniqueIdData, eitherSomeData.left().value(), edgeType, null);
		if (eitherRelations.isRight()) {
			TitanOperationStatus operationStatus = eitherRelations.right().value();
			BeEcompErrorManager.getInstance().logInternalFlowError("AddDefinitionToNodeType", "Failed to associate" + nodeType.getName() + " " + nodeUniqueId + "to " + defName + "in graph. status is " + operationStatus, ErrorSeverity.ERROR);
			return Either.right(operationStatus);
		}
		return Either.left(eitherSomeData.left().value());
	}

	protected <SomeData extends GraphNode, SomeDefenition> TitanOperationStatus addDefinitionToNodeType(TitanVertex vertex, SomeDefenition someDefinition, NodeTypeEnum nodeType, String nodeUniqueId, final GraphEdgeLabels edgeType,
			Supplier<SomeData> dataBuilder, Supplier<String> defNameGenerator) {
		String defName = defNameGenerator.get();
		log.debug("Got {} {}", defName, someDefinition);

		SomeData someData = dataBuilder.get();

		log.debug("Before adding {} to graph. data = {}", defName, someData);

		@SuppressWarnings("unchecked")
		Either<TitanVertex, TitanOperationStatus> eitherSomeData = titanGenericDao.createNode(someData);

		log.debug("After adding {} to graph. status is = {}", defName, eitherSomeData);

		if (eitherSomeData.isRight()) {
			TitanOperationStatus operationStatus = eitherSomeData.right().value();
			log.error("Failed to add {}  to graph. status is {}", defName, operationStatus);
			return operationStatus;
		}

		TitanOperationStatus relations = titanGenericDao.createEdge(vertex, eitherSomeData.left().value(), edgeType, null);
		if (!relations.equals(TitanOperationStatus.OK)) {
			TitanOperationStatus operationStatus = relations;
			BeEcompErrorManager.getInstance().logInternalFlowError("AddDefinitionToNodeType", "Failed to associate" + nodeType.getName() + " " + nodeUniqueId + "to " + defName + "in graph. status is " + operationStatus, ErrorSeverity.ERROR);
			return operationStatus;
		}
		return relations;
	}

	interface NodeElementFetcher<ElementDefinition> {
		TitanOperationStatus findAllNodeElements(String nodeId, List<ElementDefinition> listTofill);
	}

	public <ElementDefinition> TitanOperationStatus findAllResourceElementsDefinitionRecursively(String resourceId, List<ElementDefinition> elements, NodeElementFetcher<ElementDefinition> singleNodeFetcher) {

		if (log.isTraceEnabled())
			log.trace("Going to fetch elements under resource {}", resourceId);
		TitanOperationStatus resourceAttributesStatus = singleNodeFetcher.findAllNodeElements(resourceId, elements);

		if (resourceAttributesStatus != TitanOperationStatus.OK) {
			return resourceAttributesStatus;
		}

		Either<ImmutablePair<ResourceMetadataData, GraphEdge>, TitanOperationStatus> parentNodes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resourceId, GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.Resource,
				ResourceMetadataData.class);

		if (parentNodes.isRight()) {
			TitanOperationStatus parentNodesStatus = parentNodes.right().value();
			if (parentNodesStatus != TitanOperationStatus.NOT_FOUND) {
				BeEcompErrorManager.getInstance().logInternalFlowError("findAllResourceElementsDefinitionRecursively", "Failed to find parent elements of resource " + resourceId + ". status is " + parentNodesStatus, ErrorSeverity.ERROR);
				return parentNodesStatus;
			}
		}

		if (parentNodes.isLeft()) {
			ImmutablePair<ResourceMetadataData, GraphEdge> parnetNodePair = parentNodes.left().value();
			String parentUniqueId = parnetNodePair.getKey().getMetadataDataDefinition().getUniqueId();
			TitanOperationStatus addParentIntStatus = findAllResourceElementsDefinitionRecursively(parentUniqueId, elements, singleNodeFetcher);

			if (addParentIntStatus != TitanOperationStatus.OK) {
				BeEcompErrorManager.getInstance().logInternalFlowError("findAllResourceElementsDefinitionRecursively", "Failed to find all resource elements of resource " + parentUniqueId, ErrorSeverity.ERROR);

				return addParentIntStatus;
			}
		}
		return TitanOperationStatus.OK;
	}

	protected <T, TStatus> void handleTransactionCommitRollback(boolean inTransaction, Either<T, TStatus> result) {
		if (!inTransaction) {
			if (result == null || result.isRight()) {
				log.error("Going to execute rollback on graph.");
				titanGenericDao.rollback();
			} else {
				log.debug("Going to execute commit on graph.");
				titanGenericDao.commit();
			}
		}
	}

	public <ElementTypeDefinition> Either<ElementTypeDefinition, StorageOperationStatus> getElementType(Function<String, Either<ElementTypeDefinition, TitanOperationStatus>> elementGetter, String uniqueId, boolean inTransaction) {
		Either<ElementTypeDefinition, StorageOperationStatus> result = null;
		try {

			Either<ElementTypeDefinition, TitanOperationStatus> ctResult = elementGetter.apply(uniqueId);

			if (ctResult.isRight()) {
				TitanOperationStatus status = ctResult.right().value();
				if (status != TitanOperationStatus.NOT_FOUND) {
					log.error("Failed to retrieve information on element uniqueId: {}. status is {}", uniqueId, status);
				}
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(ctResult.right().value()));
				return result;
			}

			result = Either.left(ctResult.left().value());

			return result;
		} finally {
			handleTransactionCommitRollback(inTransaction, result);

		}

	}

	/**
	 * @param propertyDefinition
	 * @return
	 */

	protected StorageOperationStatus validateAndUpdateProperty(IComplexDefaultValue propertyDefinition, Map<String, DataTypeDefinition> dataTypes) {

		log.trace("Going to validate property type and value. {}", propertyDefinition);

		String propertyType = propertyDefinition.getType();
		String value = propertyDefinition.getDefaultValue();

		ToscaPropertyType type = getType(propertyType);

		if (type == null) {

			DataTypeDefinition dataTypeDefinition = dataTypes.get(propertyType);
			if (dataTypeDefinition == null) {
				log.debug("The type {}  of property cannot be found.", propertyType);
				return StorageOperationStatus.INVALID_TYPE;
			}

			StorageOperationStatus status = validateAndUpdateComplexValue(propertyDefinition, propertyType, value, dataTypeDefinition, dataTypes);

			return status;

		}
		String innerType = null;

		Either<String, TitanOperationStatus> checkInnerType = getInnerType(type, () -> propertyDefinition.getSchema());
		if (checkInnerType.isRight()) {
			return StorageOperationStatus.INVALID_TYPE;
		}
		innerType = checkInnerType.left().value();

		log.trace("After validating property type {}", propertyType);

		boolean isValidProperty = isValidValue(type, value, innerType, dataTypes);
		if (false == isValidProperty) {
			log.info("The value {} of property from type {} is invalid", value, type);
			return StorageOperationStatus.INVALID_VALUE;
		}

		PropertyValueConverter converter = type.getConverter();

		if (isEmptyValue(value)) {
			log.debug("Default value was not sent for property {}. Set default value to {}", propertyDefinition.getName(), EMPTY_VALUE);
			propertyDefinition.setDefaultValue(EMPTY_VALUE);
		} else if (false == isEmptyValue(value)) {
			String convertedValue = converter.convert(value, innerType, dataTypes);
			propertyDefinition.setDefaultValue(convertedValue);
		}
		return StorageOperationStatus.OK;
	}

	protected ToscaPropertyType getType(String propertyType) {

		ToscaPropertyType type = ToscaPropertyType.isValidType(propertyType);

		return type;

	}

	protected boolean isValidValue(ToscaPropertyType type, String value, String innerType, Map<String, DataTypeDefinition> dataTypes) {
		if (isEmptyValue(value)) {
			return true;
		}

		PropertyTypeValidator validator = type.getValidator();

		boolean isValid = validator.isValid(value, innerType, dataTypes);
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

	protected StorageOperationStatus validateAndUpdateComplexValue(IComplexDefaultValue propertyDefinition, String propertyType,

			String value, DataTypeDefinition dataTypeDefinition, Map<String, DataTypeDefinition> dataTypes) {

		ImmutablePair<JsonElement, Boolean> validateResult = dataTypeValidatorConverter.validateAndUpdate(value, dataTypeDefinition, dataTypes);

		if (validateResult.right.booleanValue() == false) {
			log.debug("The value {} of property from type {} is invalid", propertyType, propertyType);
			return StorageOperationStatus.INVALID_VALUE;
		}

		JsonElement jsonElement = validateResult.left;

		log.trace("Going to update value in property definition {} {}" , propertyDefinition.getName() , (jsonElement != null ? jsonElement.toString() : null));

		updateValue(propertyDefinition, jsonElement);

		return StorageOperationStatus.OK;
	}

	protected void updateValue(IComplexDefaultValue propertyDefinition, JsonElement jsonElement) {

		propertyDefinition.setDefaultValue(getValueFromJsonElement(jsonElement));

	}

	protected String getValueFromJsonElement(JsonElement jsonElement) {
		String value = null;

		if (jsonElement == null || jsonElement.isJsonNull()) {
			value = EMPTY_VALUE;
		} else {
			if (jsonElement.toString().isEmpty()) {
				value = "";
			} else {
				value = jsonElement.toString();
			}
		}

		return value;
	}

	protected Either<String, TitanOperationStatus> getInnerType(ToscaPropertyType type, Supplier<SchemaDefinition> schemeGen) {
		String innerType = null;
		if (type == ToscaPropertyType.LIST || type == ToscaPropertyType.MAP) {

			SchemaDefinition def = schemeGen.get();// propDataDef.getSchema();
			if (def == null) {
				log.debug("Schema doesn't exists for property of type {}", type);
				return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
			}
			PropertyDataDefinition propDef = def.getProperty();
			if (propDef == null) {
				log.debug("Property in Schema Definition inside property of type {} doesn't exist", type);
				return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
			}
			innerType = propDef.getType();
		}
		return Either.left(innerType);
	}

	/**
	 * Convert Constarint object to json in order to add it to the Graph
	 * 
	 * @param constraints
	 * @return
	 */
	public List<String> convertConstraintsToString(List<PropertyConstraint> constraints) {

		List<String> result = null;

		if (constraints != null && false == constraints.isEmpty()) {
			result = new ArrayList<String>();
			for (PropertyConstraint propertyConstraint : constraints) {
				String constraint = gson.toJson(propertyConstraint);
				result.add(constraint);
			}

		}

		return result;
	}

	public List<PropertyConstraint> convertConstraints(List<String> constraints) {

		if (constraints == null || constraints.size() == 0) {
			return null;
		}

		List<PropertyConstraint> list = new ArrayList<PropertyConstraint>();
		Type constraintType = new TypeToken<PropertyConstraint>() {
		}.getType();

		Gson gson = new GsonBuilder().registerTypeAdapter(constraintType, new PropertyConstraintDeserialiser()).create();

		for (String constraintJson : constraints) {
			PropertyConstraint propertyConstraint = gson.fromJson(constraintJson, constraintType);
			list.add(propertyConstraint);
		}

		return list;
	}

}
