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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.graph.GraphElementFactory;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyRule;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.IComplexDefaultValue;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.IPropertyOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.constraints.ConstraintType;
import org.openecomp.sdc.be.model.tosca.constraints.GreaterOrEqualConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.GreaterThanConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.InRangeConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.LessOrEqualConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.LessThanConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.MinLengthConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.ValidValuesConstraint;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.openecomp.sdc.be.model.tosca.validators.PropertyTypeValidator;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;
import org.openecomp.sdc.be.resources.data.DataTypeData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.PropertyValueData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

@Component("property-operation")
public class PropertyOperation extends AbstractOperation implements IPropertyOperation {

	private TitanGenericDao titanGenericDao;
	
	public static void main(String[] args) {

		List<Pattern> buildFunctionPatterns = buildFunctionPatterns();

		for (Pattern pattern : buildFunctionPatterns) {

			String[] strs = { "str_replace", "{ str_replace:", " {str_replace:", " {   str_replace:", "{str_replace:" };
			for (String str : strs) {
				Matcher m = pattern.matcher(str);
				System.out.println(pattern.pattern() + " " + str + " " + m.find());
			}
		}

	}

	public PropertyOperation(@Qualifier("titan-generic-dao") TitanGenericDao titanGenericDao) {
		super();
		this.titanGenericDao = titanGenericDao;
	}

	private static Logger log = LoggerFactory.getLogger(PropertyOperation.class.getName());

	private static List<Pattern> functionPatterns = null;

	static {

		functionPatterns = buildFunctionPatterns();
	}

	/**
	 * The value of functions is in a json format. Build pattern for each function name
	 * 
	 * { str_replace: .... } {str_replace: .... } {str_replace: .... } { str_replace: .... }
	 * 
	 * @return
	 */
	private static List<Pattern> buildFunctionPatterns() {

		List<Pattern> functionPatterns = new ArrayList<>();

		String[] functions = { "get_input", "get_property" };

		for (String function : functions) {
			Pattern pattern = Pattern.compile("^[ ]*\\{[ ]*" + function + ":");
			functionPatterns.add(pattern);
		}

		return functionPatterns;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openecomp.sdc.be.model.operations.api.IPropertyOperation# addPropertyToResource(java.lang.String, org.openecomp.sdc.be.model.PropertyDefinition, org.openecomp.sdc.be.dao.neo4j.datatype.NodeTypeEnum, java.lang.String)
	 */
	/*
	 * @Override public Either<PropertyDefinition, StorageOperationStatus> addPropertyToResource( String propertyName, PropertyDefinition propertyDefinition, NodeTypeEnum nodeType, String resourceId) {
	 * 
	 * StorageOperationStatus isValidProperty = isTypeExistsAndValid(propertyDefinition); if (isValidProperty != StorageOperationStatus.OK) { return Either.right(isValidProperty); }
	 * 
	 * Either<PropertyData, TitanOperationStatus> status = addPropertyToGraph(propertyName, propertyDefinition, resourceId);
	 * 
	 * if (status.isRight()) { titanGenericDao.rollback(); 
	 * log.error("Failed to add property {} to resource {}, propertyName, resourceId);
	 *  return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status. right().value())); } else
	 * { titanGenericDao.commit(); PropertyData propertyData = status.left().value();
	 * 
	 * PropertyDefinition propertyDefResult = convertPropertyDataToPropertyDefinition(propertyData, propertyName, resourceId); log.debug("The returned PropertyDefintion is " + propertyDefinition); return Either.left(propertyDefResult); }
	 * 
	 * 
	 * }
	 */
	private StorageOperationStatus isTypeExistsAndValid(PropertyDefinition propertyDefinition) {

		ToscaPropertyType type = ToscaPropertyType.isValidType(propertyDefinition.getType());

		if (type == null) {
			return StorageOperationStatus.INVALID_TYPE;
		}

		String propertyType = propertyDefinition.getType();
		String innerType = null;
		String value = propertyDefinition.getDefaultValue();

		if (propertyType.equals(ToscaPropertyType.LIST) || propertyType.equals(ToscaPropertyType.MAP)) {
			SchemaDefinition schema;
			if ((schema = propertyDefinition.getSchema()) != null) {
				PropertyDataDefinition property;
				if ((property = schema.getProperty()) != null) {
					innerType = property.getType();

				}
			}
		}

		PropertyTypeValidator validator = type.getValidator();

		if (value == null || (EMPTY_VALUE != null && EMPTY_VALUE.equals(propertyDefinition.getDefaultValue()))) {
			return StorageOperationStatus.OK;
		} else {
			boolean isValid = validator.isValid(value, innerType, null);
			if (true == isValid) {
				return StorageOperationStatus.OK;
			} else {
				return StorageOperationStatus.INVALID_VALUE;
			}
		}

	}

	public PropertyDefinition convertPropertyDataToPropertyDefinition(PropertyData propertyDataResult, String propertyName, String resourceId) {
		log.debug("The object returned after create property is {}", propertyDataResult);

		PropertyDefinition propertyDefResult = new PropertyDefinition(propertyDataResult.getPropertyDataDefinition());
		propertyDefResult.setConstraints(convertConstraints(propertyDataResult.getConstraints()));
		propertyDefResult.setName(propertyName);
//		propertyDefResult.setParentUniqueId(resourceId);

		return propertyDefResult;
	}

	public static class PropertyConstraintSerialiser implements JsonSerializer<PropertyConstraint> {

		@Override
		public JsonElement serialize(PropertyConstraint src, Type typeOfSrc, JsonSerializationContext context) {
			JsonParser parser = new JsonParser();
			JsonObject result = new JsonObject();
			JsonArray jsonArray = new JsonArray();
			if (src instanceof InRangeConstraint) {
				InRangeConstraint rangeConstraint = (InRangeConstraint) src;
				jsonArray.add(parser.parse(rangeConstraint.getRangeMinValue()));
				jsonArray.add(parser.parse(rangeConstraint.getRangeMaxValue()));
				result.add("inRange", jsonArray);
			} else if (src instanceof GreaterThanConstraint) {
				GreaterThanConstraint greaterThanConstraint = (GreaterThanConstraint) src;
				jsonArray.add(parser.parse(greaterThanConstraint.getGreaterThan()));
				result.add("greaterThan", jsonArray);
			} else if (src instanceof LessOrEqualConstraint) {
				LessOrEqualConstraint lessOrEqualConstraint = (LessOrEqualConstraint) src;
				jsonArray.add(parser.parse(lessOrEqualConstraint.getLessOrEqual()));
				result.add("lessOrEqual", jsonArray);
			} else {
				log.warn("PropertyConstraint {} is not supported. Ignored.", src.getClass().getName());
			}

			return result;
		}

	}

	public static class PropertyConstraintDeserialiser implements JsonDeserializer<PropertyConstraint> {

		@Override
		public PropertyConstraint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

			PropertyConstraint propertyConstraint = null;

			Set<Entry<String, JsonElement>> set = json.getAsJsonObject().entrySet();

			if (set.size() == 1) {
				Entry<String, JsonElement> element = set.iterator().next();
				String key = element.getKey();
				JsonElement value = element.getValue();

				ConstraintType constraintType = ConstraintType.getByType(key);
				if (constraintType == null) {
					log.warn("ConstraintType was not found for constraint name:{}", key);
				} else {
					switch (constraintType) {
					case IN_RANGE:

						if (value != null) {
							if (value instanceof JsonArray) {
								JsonArray rangeArray = (JsonArray) value;
								if (rangeArray.size() != 2) {
									log.error("The range constraint content is invalid. value = {}", value);
								} else {
									InRangeConstraint rangeConstraint = new InRangeConstraint();
									String minValue = rangeArray.get(0).getAsString();
									String maxValue = rangeArray.get(1).getAsString();
									rangeConstraint.setRangeMinValue(minValue);
									rangeConstraint.setRangeMaxValue(maxValue);
									propertyConstraint = rangeConstraint;
								}
							}

						} else {
							log.warn("The value of GreaterThanConstraint is null");
						}
						break;
					case GREATER_THAN:
						if (value != null) {
							String asString = value.getAsString();
							log.debug("Before adding value to GreaterThanConstraint object. value = {}", asString);
							propertyConstraint = new GreaterThanConstraint(asString);
							break;
						} else {
							log.warn("The value of GreaterThanConstraint is null");
						}
						break;

					case LESS_THAN:
						if (value != null) {
							String asString = value.getAsString();
							log.debug("Before adding value to LessThanConstraint object. value = {}", asString);
							propertyConstraint = new LessThanConstraint(asString);
							break;
						} else {
							log.warn("The value of LessThanConstraint is null");
						}
						break;
					case GREATER_OR_EQUAL:
						if (value != null) {
							String asString = value.getAsString();
							log.debug("Before adding value to GreaterThanConstraint object. value = {}", asString);
							propertyConstraint = new GreaterOrEqualConstraint(asString);
							break;
						} else {
							log.warn("The value of GreaterOrEqualConstraint is null");
						}
						break;
					case LESS_OR_EQUAL:

						if (value != null) {
							String asString = value.getAsString();
							log.debug("Before adding value to LessOrEqualConstraint object. value = {}", asString);
							propertyConstraint = new LessOrEqualConstraint(asString);
						} else {
							log.warn("The value of GreaterThanConstraint is null");
						}
						break;

					case VALID_VALUES:

						if (value != null) {
							if (value instanceof JsonArray) {
								JsonArray rangeArray = (JsonArray) value;
								if (rangeArray.size() == 0) {
									log.error("The valid values constraint content is invalid. value = {}", value);
								} else {
									ValidValuesConstraint vvConstraint = new ValidValuesConstraint();
									List<String> validValues = new ArrayList<String>();
									for (JsonElement jsonElement : rangeArray) {
										String item = jsonElement.getAsString();
										validValues.add(item);
									}
									vvConstraint.setValidValues(validValues);
									propertyConstraint = vvConstraint;
								}
							}

						} else {
							log.warn("The value of ValidValuesConstraint is null");
						}
						break;

					case MIN_LENGTH:
						if (value != null) {
							int asInt = value.getAsInt();
							log.debug("Before adding value to Min Length object. value = {}", asInt);
							propertyConstraint = new MinLengthConstraint(asInt);
							break;
						} else {
							log.warn("The value of MinLengthConstraint is null");
						}
						break;
					default:
						log.warn("Key {} is not supported. Ignored.", key);
					}
				}
			}

			return propertyConstraint;
		}

	}

	public TitanOperationStatus addPropertiesToGraph(Map<String, PropertyDefinition> properties, String resourceId, Map<String, DataTypeDefinition> dataTypes) {

		ResourceMetadataData resourceData = new ResourceMetadataData();
		resourceData.getMetadataDataDefinition().setUniqueId(resourceId);

		if (properties != null) {
			for (Entry<String, PropertyDefinition> entry : properties.entrySet()) {

				String propertyName = entry.getKey();
				PropertyDefinition propertyDefinition = entry.getValue();

				StorageOperationStatus validateAndUpdateProperty = validateAndUpdateProperty(propertyDefinition, dataTypes);
				if (validateAndUpdateProperty != StorageOperationStatus.OK) {
					log.error("Property {} is invalid. Status is {}", propertyDefinition, validateAndUpdateProperty);
					return TitanOperationStatus.ILLEGAL_ARGUMENT;
				}

				Either<PropertyData, TitanOperationStatus> addPropertyToGraph = addPropertyToGraph(propertyName, propertyDefinition, resourceId);

				if (addPropertyToGraph.isRight()) {
					return addPropertyToGraph.right().value();
				}
			}
		}

		return TitanOperationStatus.OK;

	}

	public TitanOperationStatus addPropertiesToGraph(TitanVertex metadataVertex, Map<String, PropertyDefinition> properties, Map<String, DataTypeDefinition> dataTypes, String resourceId) {

		if (properties != null) {
			for (Entry<String, PropertyDefinition> entry : properties.entrySet()) {

				String propertyName = entry.getKey();
				PropertyDefinition propertyDefinition = entry.getValue();

				StorageOperationStatus validateAndUpdateProperty = validateAndUpdateProperty(propertyDefinition, dataTypes);
				if (validateAndUpdateProperty != StorageOperationStatus.OK) {
					log.error("Property {} is invalid. Status is {}", propertyDefinition, validateAndUpdateProperty);
					return TitanOperationStatus.ILLEGAL_ARGUMENT;
				}

				TitanOperationStatus addPropertyToGraph = addPropertyToGraphByVertex(metadataVertex, propertyName, propertyDefinition, resourceId);

				if (!addPropertyToGraph.equals(TitanOperationStatus.OK)) {
					return addPropertyToGraph;
				}
			}
		}

		return TitanOperationStatus.OK;

	}

	public Either<PropertyData, StorageOperationStatus> addProperty(String propertyName, PropertyDefinition propertyDefinition, String resourceId) {

		Either<PropertyData, TitanOperationStatus> either = addPropertyToGraph(propertyName, propertyDefinition, resourceId);
		if (either.isRight()) {
			StorageOperationStatus storageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(either.right().value());
			return Either.right(storageStatus);
		}
		return Either.left(either.left().value());
	}

	/**
	 * @param propertyDefinition
	 * @return
	 */
	@Override
	public StorageOperationStatus validateAndUpdateProperty(IComplexDefaultValue propertyDefinition, Map<String, DataTypeDefinition> dataTypes) {

		log.trace("Going to validate property type and value. {}", propertyDefinition);

		String propertyType = propertyDefinition.getType();
		String value = propertyDefinition.getDefaultValue();

		ToscaPropertyType type = getType(propertyType);

		if (type == null) {

			DataTypeDefinition dataTypeDefinition = dataTypes.get(propertyType);
			if (dataTypeDefinition == null) {
				log.debug("The type {} of property cannot be found.", propertyType);
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

	/*
	 * public Either<Object, Boolean> validateAndUpdatePropertyValue(String propertyType, String value, String innerType) {
	 * 
	 * log. trace("Going to validate property value and its type. type = {}, value = {}" ,propertyType, value);
	 * 
	 * ToscaPropertyType type = getType(propertyType);
	 * 
	 * if (type == null) {
	 * 
	 * Either<DataTypeDefinition, TitanOperationStatus> externalDataType = getExternalDataType(propertyType); if (externalDataType.isRight()) { TitanOperationStatus status = externalDataType.right().value(); log.debug("The type " + propertyType +
	 * " of property cannot be found. Status is " + status); if (status != TitanOperationStatus.NOT_FOUND) { BeEcompErrorManager.getInstance(). logBeInvalidTypeError("validate property type", propertyType, "property"); } return Either.right(false); }
	 * 
	 * DataTypeDefinition dataTypeDefinition = externalDataType.left().value();
	 * 
	 * Either<Map<String, DataTypeDefinition>, TitanOperationStatus> allDataTypesRes = getAllDataTypes(); if (allDataTypesRes.isRight()) { TitanOperationStatus status = allDataTypesRes.right().value(); return Either.right(false); }
	 * 
	 * Map<String, DataTypeDefinition> allDataTypes = allDataTypesRes.left().value();
	 * 
	 * ImmutablePair<JsonElement, Boolean> validateResult = dataTypeValidatorConverter.validateAndUpdate(value, dataTypeDefinition, allDataTypes);
	 * 
	 * if (validateResult.right.booleanValue() == false) { 
	 * log.debug("The value {} of property from type {} is invalid", value, propertyType); 
	 * return Either.right(false); }
	 * 
	 * JsonElement jsonElement = validateResult.left;
	 * 
	 * String valueFromJsonElement = getValueFromJsonElement(jsonElement);
	 * 
	 * return Either.left(valueFromJsonElement);
	 * 
	 * }
	 * 
	 * log.trace("After validating property type {}", propertyType);
	 * 
	 * boolean isValidProperty = isValidValue(type, value, innerType); if (false == isValidProperty) { log.debug("The value " + value + " of property from type " + type + " is invalid"); return Either.right(false); }
	 * 
	 * 
	 * Object convertedValue = value; if (false == isEmptyValue(value)) { PropertyValueConverter converter = type.getConverter(); convertedValue = converter.convert(value, null); }
	 * 
	 * return Either.left(convertedValue); }
	 */

	public Either<PropertyData, TitanOperationStatus> addPropertyToGraph(String propertyName, PropertyDefinition propertyDefinition, String resourceId) {

		ResourceMetadataData resourceData = new ResourceMetadataData();
		resourceData.getMetadataDataDefinition().setUniqueId(resourceId);

		List<PropertyConstraint> constraints = propertyDefinition.getConstraints();

		propertyDefinition.setUniqueId(UniqueIdBuilder.buildComponentPropertyUniqueId(resourceId, propertyName));
		PropertyData propertyData = new PropertyData(propertyDefinition, convertConstraintsToString(constraints));

		log.debug("Before adding property to graph {}", propertyData);
		Either<PropertyData, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(propertyData, PropertyData.class);
		log.debug("After adding property to graph {}", propertyData);
		if (createNodeResult.isRight()) {
			TitanOperationStatus operationStatus = createNodeResult.right().value();
			log.error("Failed to add property {} to graph. status is {}", propertyName, operationStatus);
			return Either.right(operationStatus);
		}

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), propertyName);
		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(resourceData, propertyData, GraphEdgeLabels.PROPERTY, props);
		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createNodeResult.right().value();
			log.error("Failed to associate resource {} to property {} in graph. status is {}", resourceId, propertyName, operationStatus);
			return Either.right(operationStatus);
		}

		return Either.left(createNodeResult.left().value());

	}

	public TitanOperationStatus addPropertyToGraphByVertex(TitanVertex metadataVertex, String propertyName, PropertyDefinition propertyDefinition, String resourceId) {

		List<PropertyConstraint> constraints = propertyDefinition.getConstraints();

		propertyDefinition.setUniqueId(UniqueIdBuilder.buildComponentPropertyUniqueId(resourceId, propertyName));
		PropertyData propertyData = new PropertyData(propertyDefinition, convertConstraintsToString(constraints));

		log.debug("Before adding property to graph {}", propertyData);
		Either<TitanVertex, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(propertyData);
		log.debug("After adding property to graph {}", propertyData);
		if (createNodeResult.isRight()) {
			TitanOperationStatus operationStatus = createNodeResult.right().value();
			log.error("Failed to add property {} to graph. status is ", propertyName, operationStatus);
			return operationStatus;
		}

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), propertyName);
		TitanVertex propertyVertex = createNodeResult.left().value();
		TitanOperationStatus createRelResult = titanGenericDao.createEdge(metadataVertex, propertyVertex, GraphEdgeLabels.PROPERTY, props);
		if (!createRelResult.equals(TitanOperationStatus.OK)) {
			log.error("Failed to associate resource {} to property {} in graph. status is {}", resourceId, propertyName, createRelResult);
			return createRelResult;
		}

		return createRelResult;

	}

	public TitanGenericDao getTitanGenericDao() {
		return titanGenericDao;
	}

	// public Either<PropertyData, StorageOperationStatus>
	// deletePropertyFromGraphFromBl(String propertyId) {
	//
	// }

	public Either<PropertyData, StorageOperationStatus> deleteProperty(String propertyId) {
		Either<PropertyData, TitanOperationStatus> either = deletePropertyFromGraph(propertyId);
		if (either.isRight()) {
			StorageOperationStatus storageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(either.right().value());
			return Either.right(storageStatus);
		}
		return Either.left(either.left().value());
	}

	public Either<PropertyData, TitanOperationStatus> deletePropertyFromGraph(String propertyId) {
		log.debug("Before deleting property from graph {}", propertyId);
		return titanGenericDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Property), propertyId, PropertyData.class);
	}

	public Either<PropertyData, StorageOperationStatus> updateProperty(String propertyId, PropertyDefinition newPropertyDefinition, Map<String, DataTypeDefinition> dataTypes) {

		StorageOperationStatus validateAndUpdateProperty = validateAndUpdateProperty(newPropertyDefinition, dataTypes);
		if (validateAndUpdateProperty != StorageOperationStatus.OK) {
			return Either.right(validateAndUpdateProperty);
		}

		Either<PropertyData, TitanOperationStatus> either = updatePropertyFromGraph(propertyId, newPropertyDefinition);
		if (either.isRight()) {
			StorageOperationStatus storageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(either.right().value());
			return Either.right(storageStatus);
		}
		return Either.left(either.left().value());
	}

	public Either<PropertyData, TitanOperationStatus> updatePropertyFromGraph(String propertyId, PropertyDefinition propertyDefinition) {
		if (log.isDebugEnabled())
			log.debug("Before updating property on graph {}", propertyId);

		// get the original property data
		Either<PropertyData, TitanOperationStatus> statusProperty = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Property), propertyId, PropertyData.class);
		if (statusProperty.isRight()) {
			log.debug("Problem while get property with id {}. Reason - {}", propertyId, statusProperty.right().value().name());
			return Either.right(statusProperty.right().value());
		}
		PropertyData orgPropertyData = statusProperty.left().value();
		PropertyDataDefinition orgPropertyDataDefinition = orgPropertyData.getPropertyDataDefinition();

		// create new property data to update
		PropertyData newPropertyData = new PropertyData();
		newPropertyData.setPropertyDataDefinition(propertyDefinition);
		PropertyDataDefinition newPropertyDataDefinition = newPropertyData.getPropertyDataDefinition();

		// update the original property data with new values
		if (orgPropertyDataDefinition.getDefaultValue() == null) {
			orgPropertyDataDefinition.setDefaultValue(newPropertyDataDefinition.getDefaultValue());
		} else {
			if (!orgPropertyDataDefinition.getDefaultValue().equals(newPropertyDataDefinition.getDefaultValue())) {
				orgPropertyDataDefinition.setDefaultValue(newPropertyDataDefinition.getDefaultValue());
			}
		}
		if (orgPropertyDataDefinition.getDescription() == null) {
			orgPropertyDataDefinition.setDescription(newPropertyDataDefinition.getDescription());
		} else {
			if (!orgPropertyDataDefinition.getDescription().equals(newPropertyDataDefinition.getDescription())) {
				orgPropertyDataDefinition.setDescription(newPropertyDataDefinition.getDescription());
			}
		}
		if (!orgPropertyDataDefinition.getType().equals(newPropertyDataDefinition.getType())) {
			orgPropertyDataDefinition.setType(newPropertyDataDefinition.getType());
		}
		if (newPropertyData.getConstraints() != null) {
			orgPropertyData.setConstraints(newPropertyData.getConstraints());
		}
		orgPropertyDataDefinition.setSchema(newPropertyDataDefinition.getSchema());

		return titanGenericDao.updateNode(orgPropertyData, PropertyData.class);
	}

	/**
	 * FOR TEST ONLY
	 * 
	 * @param titanGenericDao
	 */
	public void setTitanGenericDao(TitanGenericDao titanGenericDao) {
		this.titanGenericDao = titanGenericDao;
	}

	public Either<PropertyData, TitanOperationStatus> addPropertyToNodeType(String propertyName, PropertyDefinition propertyDefinition, NodeTypeEnum nodeType, String uniqueId) {

		List<PropertyConstraint> constraints = propertyDefinition.getConstraints();

		propertyDefinition.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(uniqueId, propertyName));
		PropertyData propertyData = new PropertyData(propertyDefinition, convertConstraintsToString(constraints));

		if (log.isDebugEnabled())
			log.debug("Before adding property to graph {}", propertyData);
		Either<PropertyData, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(propertyData, PropertyData.class);
		if (log.isDebugEnabled())
			log.debug("After adding property to graph {}", propertyData);
		if (createNodeResult.isRight()) {
			TitanOperationStatus operationStatus = createNodeResult.right().value();
			log.error("Failed to add property {} to graph. status is {}", propertyName, operationStatus);
			return Either.right(operationStatus);
		}

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), propertyName);

		UniqueIdData uniqueIdData = new UniqueIdData(nodeType, uniqueId);
		log.debug("Before associating {} to property {}", uniqueIdData, propertyName);
		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(uniqueIdData, propertyData, GraphEdgeLabels.PROPERTY, props);
		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createNodeResult.right().value();
			log.error("Failed to associate resource {} to property {} in graph. status is {}", uniqueId, propertyName, operationStatus);
			return Either.right(operationStatus);
		}

		return Either.left(createNodeResult.left().value());

	}

	public TitanOperationStatus addPropertyToNodeType(TitanVertex elementVertex, String propertyName, PropertyDefinition propertyDefinition, NodeTypeEnum nodeType, String uniqueId) {

		List<PropertyConstraint> constraints = propertyDefinition.getConstraints();

		propertyDefinition.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(uniqueId, propertyName));
		PropertyData propertyData = new PropertyData(propertyDefinition, convertConstraintsToString(constraints));

		if (log.isDebugEnabled())
			log.debug("Before adding property to graph {}", propertyData);
		Either<TitanVertex, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(propertyData);
		if (log.isDebugEnabled())
			log.debug("After adding property to graph {}", propertyData);
		if (createNodeResult.isRight()) {
			TitanOperationStatus operationStatus = createNodeResult.right().value();
			log.error("Failed to add property {} to graph. status is {} ", propertyName, operationStatus);
			return operationStatus;
		}

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), propertyName);

		TitanOperationStatus createRelResult = titanGenericDao.createEdge(elementVertex, propertyData, GraphEdgeLabels.PROPERTY, props);
		if (!createRelResult.equals(TitanOperationStatus.OK)) {
			log.error("Failed to associate resource {} to property {} in graph. status is {}", uniqueId, propertyName, createRelResult);
			return createRelResult;
		}

		return createRelResult;

	}

	public Either<Map<String, PropertyDefinition>, TitanOperationStatus> findPropertiesOfNode(NodeTypeEnum nodeType, String uniqueId) {

		Map<String, PropertyDefinition> resourceProps = new HashMap<String, PropertyDefinition>();

		Either<List<ImmutablePair<PropertyData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(nodeType), uniqueId, GraphEdgeLabels.PROPERTY, NodeTypeEnum.Property,
				PropertyData.class);

		if (childrenNodes.isRight()) {
			TitanOperationStatus operationStatus = childrenNodes.right().value();
			return Either.right(operationStatus);
		}

		List<ImmutablePair<PropertyData, GraphEdge>> values = childrenNodes.left().value();
		if (values != null) {

			for (ImmutablePair<PropertyData, GraphEdge> immutablePair : values) {
				GraphEdge edge = immutablePair.getValue();
				String propertyName = (String) edge.getProperties().get(GraphPropertiesDictionary.NAME.getProperty());
				log.debug("Property {} is associated to node {}", propertyName, uniqueId);
				PropertyData propertyData = immutablePair.getKey();
				PropertyDefinition propertyDefinition = this.convertPropertyDataToPropertyDefinition(propertyData, propertyName, uniqueId);
				resourceProps.put(propertyName, propertyDefinition);
			}

		}

		log.debug("The properties associated to node {} are {}", uniqueId, resourceProps);
		return Either.left(resourceProps);
	}

	public Either<Map<String, PropertyDefinition>, StorageOperationStatus> deleteAllPropertiesAssociatedToNode(NodeTypeEnum nodeType, String uniqueId) {

		Either<Map<String, PropertyDefinition>, TitanOperationStatus> propertiesOfNodeRes = findPropertiesOfNode(nodeType, uniqueId);

		if (propertiesOfNodeRes.isRight()) {
			TitanOperationStatus status = propertiesOfNodeRes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				return Either.right(StorageOperationStatus.OK);
			}
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}

		Map<String, PropertyDefinition> value = propertiesOfNodeRes.left().value();
		for (PropertyDefinition propertyDefinition : value.values()) {

			String propertyUid = propertyDefinition.getUniqueId();
			Either<PropertyData, TitanOperationStatus> deletePropertyRes = deletePropertyFromGraph(propertyUid);
			if (deletePropertyRes.isRight()) {
				log.error("Failed to delete property with id {}", propertyUid);
				TitanOperationStatus status = deletePropertyRes.right().value();
				if (status == TitanOperationStatus.NOT_FOUND) {
					status = TitanOperationStatus.INVALID_ID;
				}
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}

		}

		log.debug("The properties deleted from node {} are {}", uniqueId, value);
		return Either.left(value);
	}

	/**
	 * fetch all properties under a given resource(includes its parents' resources)
	 * 
	 * @param resourceId
	 * @param properties
	 * @return
	 */
	public TitanOperationStatus findAllResourcePropertiesRecursively(String resourceId, List<PropertyDefinition> properties) {
		final NodeElementFetcher<PropertyDefinition> singleNodeFetcher = (resourceIdParam, attributesParam) -> findPropertiesOfNode(NodeTypeEnum.Resource, resourceIdParam, attributesParam);
		return findAllResourceElementsDefinitionRecursively(resourceId, properties, singleNodeFetcher);
	}

	/**
	 * 
	 * 
	 * @param nodeType
	 * @param uniqueId
	 * @param properties
	 * @return
	 */
	protected TitanOperationStatus findPropertiesOfNode(NodeTypeEnum nodeType, String uniqueId, List<PropertyDefinition> properties) {

		Either<List<ImmutablePair<PropertyData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(nodeType), uniqueId, GraphEdgeLabels.PROPERTY, NodeTypeEnum.Property,
				PropertyData.class);

		if (childrenNodes.isRight()) {
			TitanOperationStatus status = childrenNodes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.OK;
			}
			return status;
		}

		List<ImmutablePair<PropertyData, GraphEdge>> values = childrenNodes.left().value();
		if (values != null) {

			for (ImmutablePair<PropertyData, GraphEdge> immutablePair : values) {
				GraphEdge edge = immutablePair.getValue();
				String propertyName = (String) edge.getProperties().get(GraphPropertiesDictionary.NAME.getProperty());
				if (log.isDebugEnabled())
					log.debug("Property {} is associated to node {}", propertyName, uniqueId);
				PropertyData propertyData = immutablePair.getKey();
				PropertyDefinition propertyDefinition = this.convertPropertyDataToPropertyDefinition(propertyData, propertyName, uniqueId);
				//Adds parent property to List if it hasn't been overrided in one of the children
				if(!properties.stream().filter(p -> p.getName().equals(propertyDefinition.getName())).findAny().isPresent()){
					properties.add(propertyDefinition);
				}

				if (log.isTraceEnabled())
					log.trace("findPropertiesOfNode - property {} associated to node {}", propertyDefinition, uniqueId);
			}

		}

		return TitanOperationStatus.OK;
	}
/**
 * Checks existence of a property with the same name belonging to the same resource
 * or existence of property with the same name and different type (including derived from hierarchy)
 * @param properties
 * @param resourceUid
 * @param propertyName
 * @param propertyType
 * @return
 */
	public boolean isPropertyExist(List<PropertyDefinition> properties, String resourceUid, String propertyName, String propertyType) {
		boolean result = false;
		if (!CollectionUtils.isEmpty(properties)) {
			for (PropertyDefinition propertyDefinition : properties) {
	
				if ( propertyDefinition.getName().equals(propertyName) &&
						(propertyDefinition.getParentUniqueId().equals(resourceUid) || !propertyDefinition.getType().equals(propertyType)) ) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * add property to resource instance
	 * 
	 * @param innerType
	 *            TODO // * @param resourceInstanceProperty // * @param resourceInstanceId // * @param index
	 * 
	 * @return
	 */
	/*
	 * public Either<PropertyValueData, TitanOperationStatus> addPropertyToResourceInstance( ComponentInstanceProperty resourceInstanceProperty, String resourceInstanceId, Integer index) {
	 * 
	 * Either<ComponentInstanceData, TitanOperationStatus> findResInstanceRes = titanGenericDao .getNode(UniqueIdBuilder .getKeyByNodeType(NodeTypeEnum.ResourceInstance), resourceInstanceId, ComponentInstanceData.class);
	 * 
	 * if (findResInstanceRes.isRight()) { TitanOperationStatus status = findResInstanceRes.right().value(); if (status == TitanOperationStatus.NOT_FOUND) { status = TitanOperationStatus.INVALID_ID; } return Either.right(status); }
	 * 
	 * String propertyId = resourceInstanceProperty.getUniqueId(); Either<PropertyData, TitanOperationStatus> findPropertyDefRes = titanGenericDao .getNode(UniqueIdBuilder .getKeyByNodeType(NodeTypeEnum.Property), propertyId, PropertyData.class);
	 * 
	 * if (findPropertyDefRes.isRight()) { TitanOperationStatus status = findPropertyDefRes.right().value(); if (status == TitanOperationStatus.NOT_FOUND) { status = TitanOperationStatus.INVALID_ID; } return Either.right(status); }
	 * 
	 * String valueUniqueUid = resourceInstanceProperty.getValueUniqueUid(); if (valueUniqueUid == null) {
	 * 
	 * PropertyData propertyData = findPropertyDefRes.left().value(); ComponentInstanceData resourceInstanceData = findResInstanceRes.left().value();
	 * 
	 * ImmutablePair<TitanOperationStatus, String> isPropertyValueExists = findPropertyValue(resourceInstanceId, propertyId); if (isPropertyValueExists.getLeft() == TitanOperationStatus.ALREADY_EXIST) { log.debug("The property " + propertyId +
	 * " already added to the resource instance " + resourceInstanceId); resourceInstanceProperty.setValueUniqueUid(isPropertyValueExists.getRight ()); Either<PropertyValueData, TitanOperationStatus> updatePropertyOfResourceInstance =
	 * updatePropertyOfResourceInstance(resourceInstanceProperty, resourceInstanceId); if (updatePropertyOfResourceInstance.isRight()) { BeEcompErrorManager.getInstance().logInternalFlowError( "UpdatePropertyValueOnComponentInstance",
	 * "Failed to update property value on instance. Status is " + updatePropertyOfResourceInstance.right().value(), ErrorSeverity.ERROR); return Either.right(updatePropertyOfResourceInstance.right().value()); } return
	 * Either.left(updatePropertyOfResourceInstance.left().value()); }
	 * 
	 * if (isPropertyValueExists.getLeft() != TitanOperationStatus.NOT_FOUND) { 
	 * log.debug("After finding property value of {} on componenet instance {}", propertyId, resourceInstanceId); 
	 * return Either.right(isPropertyValueExists.getLeft()); }
	 * 
	 * String propertyType = propertyData.getPropertyDataDefinition().getType(); String value = resourceInstanceProperty.getValue(); Either<Object, Boolean> isValid = validateAndUpdatePropertyValue(propertyType, value);
	 * 
	 * String newValue = value; if (isValid.isRight()) { Boolean res = isValid.right().value(); if (res == false) { return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT); } } else { Object object = isValid.left().value(); if (object != null) {
	 * newValue = object.toString(); } }
	 * 
	 * String uniqueId = UniqueIdBuilder.buildResourceInstancePropertyValueUid( resourceInstanceData.getUniqueId(), index); PropertyValueData propertyValueData = new PropertyValueData(); propertyValueData.setUniqueId(uniqueId);
	 * propertyValueData.setValue(newValue);
	 * 
	 * ImmutablePair<String, Boolean> pair = validateAndUpdateRules(propertyType, resourceInstanceProperty.getRules()); if (pair.getRight() != null && pair.getRight() == false) { BeEcompErrorManager.getInstance().
	 * logBeInvalidValueError("Add property value", pair.getLeft(), resourceInstanceProperty.getName(), propertyType); return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT); } addRulesToNewPropertyValue(propertyValueData,
	 * resourceInstanceProperty, resourceInstanceId);
	 * 
	 * log.debug("Before adding property value to graph {}", propertyValueData); 
	 * Either<PropertyValueData, TitanOperationStatus> createNodeResult = titanGenericDao .createNode(propertyValueData, PropertyValueData.class);
	 * log.debug("After adding property value to graph {}", propertyValueData);
	 * 
	 * Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao .createRelation(propertyValueData, propertyData, GraphEdgeLabels.PROPERTY_IMPL, null);
	 * 
	 * if (createRelResult.isRight()) { TitanOperationStatus operationStatus = createNodeResult.right() .value(); //TODO: change logger log.error("Failed to associate property value " + uniqueId + " to property " + propertyId +
	 * " in graph. status is " + operationStatus); return Either.right(operationStatus); }
	 * 
	 * createRelResult = titanGenericDao .createRelation(resourceInstanceData, propertyValueData, GraphEdgeLabels.PROPERTY_VALUE, null);
	 * 
	 * if (createRelResult.isRight()) { TitanOperationStatus operationStatus = createNodeResult.right() .value(); //TODO: change logger log.error("Failed to associate resource instance " + resourceInstanceId + " property value " + uniqueId +
	 * " in graph. status is " + operationStatus); return Either.right(operationStatus); }
	 * 
	 * return Either.left(createNodeResult.left().value()); } else { log.error("property value already exists."); return Either.right(TitanOperationStatus.ALREADY_EXIST); }
	 * 
	 * }
	 */
	public ImmutablePair<String, Boolean> validateAndUpdateRules(String propertyType, List<PropertyRule> rules, String innerType, Map<String, DataTypeDefinition> dataTypes, boolean isValidate) {

		if (rules == null || rules.isEmpty() == true) {
			return new ImmutablePair<String, Boolean>(null, true);
		}

		for (PropertyRule rule : rules) {
			String value = rule.getValue();
			Either<Object, Boolean> updateResult = validateAndUpdatePropertyValue(propertyType, value, isValidate, innerType, dataTypes);
			if (updateResult.isRight()) {
				Boolean status = updateResult.right().value();
				if (status == false) {
					return new ImmutablePair<String, Boolean>(value, status);
				}
			} else {
				String newValue = null;
				Object object = updateResult.left().value();
				if (object != null) {
					newValue = object.toString();
				}
				rule.setValue(newValue);
			}
		}

		return new ImmutablePair<String, Boolean>(null, true);
	}

	public void addRulesToNewPropertyValue(PropertyValueData propertyValueData, ComponentInstanceProperty resourceInstanceProperty, String resourceInstanceId) {

		List<PropertyRule> rules = resourceInstanceProperty.getRules();
		if (rules == null) {
			PropertyRule propertyRule = buildRuleFromPath(propertyValueData, resourceInstanceProperty, resourceInstanceId);
			rules = new ArrayList<>();
			rules.add(propertyRule);
		} else {
			rules = sortRules(rules);
		}

		propertyValueData.setRules(rules);
	}

	private PropertyRule buildRuleFromPath(PropertyValueData propertyValueData, ComponentInstanceProperty resourceInstanceProperty, String resourceInstanceId) {
		List<String> path = resourceInstanceProperty.getPath();
		// FOR BC. Since old Property values on VFC/VF does not have rules on
		// graph.
		// Update could be done on one level only, thus we can use this
		// operation to avoid migration.
		if (path == null || path.isEmpty() == true) {
			path = new ArrayList<>();
			path.add(resourceInstanceId);
		}
		PropertyRule propertyRule = new PropertyRule();
		propertyRule.setRule(path);
		propertyRule.setValue(propertyValueData.getValue());
		return propertyRule;
	}

	private List<PropertyRule> sortRules(List<PropertyRule> rules) {

		// TODO: sort the rules by size and binary representation.
		// (x, y, .+) --> 110 6 priority 1
		// (x, .+, z) --> 101 5 priority 2

		return rules;
	}

	public ImmutablePair<TitanOperationStatus, String> findPropertyValue(String resourceInstanceId, String propertyId) {

		log.debug("Going to check whether the property {} already added to resource instance {}", propertyId, resourceInstanceId);

		Either<List<ComponentInstanceProperty>, TitanOperationStatus> getAllRes = this.getAllPropertiesOfResourceInstanceOnlyPropertyDefId(resourceInstanceId);
		if (getAllRes.isRight()) {
			TitanOperationStatus status = getAllRes.right().value();
			log.trace("After fetching all properties of resource instance {}. Status is {}", resourceInstanceId, status);
			return new ImmutablePair<TitanOperationStatus, String>(status, null);
		}

		List<ComponentInstanceProperty> list = getAllRes.left().value();
		if (list != null) {
			for (ComponentInstanceProperty instanceProperty : list) {
				String propertyUniqueId = instanceProperty.getUniqueId();
				String valueUniqueUid = instanceProperty.getValueUniqueUid();
				log.trace("Go over property {} under resource instance {}. valueUniqueId = {}", propertyUniqueId, resourceInstanceId, valueUniqueUid);
				if (propertyId.equals(propertyUniqueId) && valueUniqueUid != null) {
					log.debug("The property {} already created under resource instance {}", propertyId, resourceInstanceId);
					return new ImmutablePair<TitanOperationStatus, String>(TitanOperationStatus.ALREADY_EXIST, valueUniqueUid);
				}
			}
		}

		return new ImmutablePair<TitanOperationStatus, String>(TitanOperationStatus.NOT_FOUND, null);
	}

	/**
	 * update value of property on resource instance
	 * 
	 * @param resourceInstanceProperty
	 * @param resourceInstanceId
	 * @return
	 */
	/*
	 * public Either<PropertyValueData, TitanOperationStatus> updatePropertyOfResourceInstance( ComponentInstanceProperty resourceInstanceProperty, String resourceInstanceId) {
	 * 
	 * /// #RULES SUPPORT /// Ignore rules received from client till support resourceInstanceProperty.setRules(null); /// /// Either<ComponentInstanceData, TitanOperationStatus> findResInstanceRes = titanGenericDao .getNode(UniqueIdBuilder
	 * .getKeyByNodeType(NodeTypeEnum.ResourceInstance), resourceInstanceId, ComponentInstanceData.class);
	 * 
	 * if (findResInstanceRes.isRight()) { TitanOperationStatus status = findResInstanceRes.right().value(); if (status == TitanOperationStatus.NOT_FOUND) { status = TitanOperationStatus.INVALID_ID; } return Either.right(status); }
	 * 
	 * String propertyId = resourceInstanceProperty.getUniqueId(); Either<PropertyData, TitanOperationStatus> findPropertyDefRes = titanGenericDao .getNode(UniqueIdBuilder .getKeyByNodeType(NodeTypeEnum.Property), propertyId, PropertyData.class);
	 * 
	 * if (findPropertyDefRes.isRight()) { TitanOperationStatus status = findPropertyDefRes.right().value(); return Either.right(status); }
	 * 
	 * String valueUniqueUid = resourceInstanceProperty.getValueUniqueUid(); if (valueUniqueUid == null) { return Either.right(TitanOperationStatus.INVALID_ID); } else { Either<PropertyValueData, TitanOperationStatus> findPropertyValueRes =
	 * titanGenericDao .getNode(UniqueIdBuilder .getKeyByNodeType(NodeTypeEnum.PropertyValue), valueUniqueUid, PropertyValueData.class); if (findPropertyValueRes.isRight()) { TitanOperationStatus status = findPropertyValueRes.right().value(); if
	 * (status == TitanOperationStatus.NOT_FOUND) { status = TitanOperationStatus.INVALID_ID; } return Either.right(status); }
	 * 
	 * String value = resourceInstanceProperty.getValue();
	 * 
	 * Either<ImmutablePair<PropertyData, GraphEdge>, TitanOperationStatus> child = titanGenericDao.getChild(UniqueIdBuilder .getKeyByNodeType(NodeTypeEnum.PropertyValue), valueUniqueUid, GraphEdgeLabels.PROPERTY_IMPL, NodeTypeEnum.Property,
	 * PropertyData.class);
	 * 
	 * if (child.isRight()) { TitanOperationStatus status = child.right().value(); if (status == TitanOperationStatus.NOT_FOUND) { status = TitanOperationStatus.INVALID_ID; } return Either.right(status); }
	 * 
	 * PropertyData propertyData = child.left().value().left; String propertyType = propertyData.getPropertyDataDefinition().getType();
	 * 
	 * log.debug("The type of the property {} is {}", propertyData.getUniqueId(), propertyType);
	 * 
	 * Either<Object, Boolean> isValid = validateAndUpdatePropertyValue(propertyType, value);
	 * 
	 * String newValue = value; if (isValid.isRight()) { Boolean res = isValid.right().value(); if (res == false) { return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT); } } else { Object object = isValid.left().value(); if (object != null) {
	 * newValue = object.toString(); } } PropertyValueData propertyValueData = findPropertyValueRes.left().value(); log.debug("Going to update property value from " + propertyValueData.getValue() + " to " + newValue);
	 * propertyValueData.setValue(newValue);
	 * 
	 * ImmutablePair<String, Boolean> pair = validateAndUpdateRules(propertyType, resourceInstanceProperty.getRules()); if (pair.getRight() != null && pair.getRight() == false) { BeEcompErrorManager.getInstance().
	 * logBeInvalidValueError("Add property value", pair.getLeft(), resourceInstanceProperty.getName(), propertyType); return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT); } updateRulesInPropertyValue(propertyValueData,
	 * resourceInstanceProperty, resourceInstanceId);
	 * 
	 * Either<PropertyValueData, TitanOperationStatus> updateRes = titanGenericDao.updateNode(propertyValueData, PropertyValueData.class); if (updateRes.isRight()) { TitanOperationStatus status = updateRes.right().value(); return
	 * Either.right(status); } else { return Either.left(updateRes.left().value()); } }
	 * 
	 * }
	 */

	public void updateRulesInPropertyValue(PropertyValueData propertyValueData, ComponentInstanceProperty resourceInstanceProperty, String resourceInstanceId) {

		List<PropertyRule> currentRules = propertyValueData.getRules();

		List<PropertyRule> rules = resourceInstanceProperty.getRules();
		// if rules are not supported.
		if (rules == null) {

			PropertyRule propertyRule = buildRuleFromPath(propertyValueData, resourceInstanceProperty, resourceInstanceId);
			rules = new ArrayList<>();
			rules.add(propertyRule);

			if (currentRules != null) {
				rules = mergeRules(currentRules, rules);
			}

		} else {
			// Full mode. all rules are sent in update operation.
			rules = sortRules(rules);
		}

		propertyValueData.setRules(rules);

	}

	private List<PropertyRule> mergeRules(List<PropertyRule> currentRules, List<PropertyRule> newRules) {

		List<PropertyRule> mergedRules = new ArrayList<>();

		if (newRules == null || newRules.isEmpty() == true) {
			return currentRules;
		}

		for (PropertyRule rule : currentRules) {
			PropertyRule propertyRule = new PropertyRule(rule.getRule(), rule.getValue());
			mergedRules.add(propertyRule);
		}

		for (PropertyRule rule : newRules) {
			PropertyRule foundRule = findRuleInList(rule, mergedRules);
			if (foundRule != null) {
				foundRule.setValue(rule.getValue());
			} else {
				mergedRules.add(rule);
			}
		}

		return mergedRules;
	}

	private PropertyRule findRuleInList(PropertyRule rule, List<PropertyRule> rules) {

		if (rules == null || rules.isEmpty() == true || rule.getRule() == null || rule.getRule().isEmpty() == true) {
			return null;
		}

		PropertyRule foundRule = null;
		for (PropertyRule propertyRule : rules) {
			if (rule.getRuleSize() != propertyRule.getRuleSize()) {
				continue;
			}
			boolean equals = propertyRule.compareRule(rule);
			if (equals == true) {
				foundRule = propertyRule;
				break;
			}
		}

		return foundRule;
	}

	/**
	 * return all properties associated to resource instance. The result does contains the property unique id but not its type, default value...
	 * 
	 * @param resourceInstanceUid
	 * @return
	 */
	public Either<List<ComponentInstanceProperty>, TitanOperationStatus> getAllPropertiesOfResourceInstanceOnlyPropertyDefId(String resourceInstanceUid) {

		return getAllPropertiesOfResourceInstanceOnlyPropertyDefId(resourceInstanceUid, NodeTypeEnum.ResourceInstance);

	}

	/*
	 * public Either<ComponentInstanceProperty, StorageOperationStatus> addPropertyValueToResourceInstance( ComponentInstanceProperty resourceInstanceProperty, String resourceInstanceId, Integer index, boolean inTransaction) {
	 * 
	 * /// #RULES SUPPORT /// Ignore rules received from client till support resourceInstanceProperty.setRules(null); /// ///
	 * 
	 * Either<ComponentInstanceProperty, StorageOperationStatus> result = null;
	 * 
	 * try {
	 * 
	 * Either<PropertyValueData, TitanOperationStatus> eitherStatus = this .addPropertyToResourceInstance(resourceInstanceProperty, resourceInstanceId, index);
	 * 
	 * if (eitherStatus.isRight()) { log.error( "Failed to add property value {} to resource instance {} in Graph. status is {}" , resourceInstanceProperty, resourceInstanceId, eitherStatus.right().value().name()); result =
	 * Either.right(DaoStatusConverter .convertTitanStatusToStorageStatus(eitherStatus.right() .value())); return result; } else { PropertyValueData propertyValueData = eitherStatus.left() .value();
	 * 
	 * ComponentInstanceProperty propertyValueResult = buildResourceInstanceProperty( propertyValueData, resourceInstanceProperty);
	 * 
	 * log.debug("The returned ResourceInstanceProperty is  {}", propertyValueResult); result = Either.left(propertyValueResult); return result; } }
	 * 
	 * finally { if (false == inTransaction) { if (result == null || result.isRight()) { log.error("Going to execute rollback on graph."); titanGenericDao.rollback(); } else { log.debug("Going to execute commit on graph."); titanGenericDao.commit();
	 * } } }
	 * 
	 * }
	 * 
	 * public Either<ComponentInstanceProperty, StorageOperationStatus> updatePropertyValueInResourceInstance( ComponentInstanceProperty resourceInstanceProperty, String resourceInstanceId, boolean inTransaction) {
	 * 
	 * Either<ComponentInstanceProperty, StorageOperationStatus> result = null;
	 * 
	 * try { //TODO: verify validUniqueId exists Either<PropertyValueData, TitanOperationStatus> eitherStatus = this .updatePropertyOfResourceInstance(resourceInstanceProperty, resourceInstanceId);
	 * 
	 * if (eitherStatus.isRight()) { log.error( "Failed to add property value {} to resource instance {} in Graph. status is {}" , resourceInstanceProperty, resourceInstanceId, eitherStatus.right().value().name()); result =
	 * Either.right(DaoStatusConverter .convertTitanStatusToStorageStatus(eitherStatus.right() .value())); return result; } else { PropertyValueData propertyValueData = eitherStatus.left() .value();
	 * 
	 * ComponentInstanceProperty propertyValueResult = buildResourceInstanceProperty( propertyValueData, resourceInstanceProperty);
	 * 
	 * log.debug("The returned ResourceInstanceProperty is  {}", propertyValueResult); result = Either.left(propertyValueResult); return result; } }
	 * 
	 * finally { if (false == inTransaction) { if (result == null || result.isRight()) { log.error("Going to execute rollback on graph."); titanGenericDao.rollback(); } else { log.debug("Going to execute commit on graph."); titanGenericDao.commit();
	 * } } }
	 * 
	 * }
	 */

	public Either<PropertyValueData, TitanOperationStatus> removePropertyOfResourceInstance(String propertyValueUid, String resourceInstanceId) {

		Either<ComponentInstanceData, TitanOperationStatus> findResInstanceRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), resourceInstanceId, ComponentInstanceData.class);

		if (findResInstanceRes.isRight()) {
			TitanOperationStatus status = findResInstanceRes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return Either.right(status);
		}

		Either<PropertyValueData, TitanOperationStatus> findPropertyDefRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.PropertyValue), propertyValueUid, PropertyValueData.class);

		if (findPropertyDefRes.isRight()) {
			TitanOperationStatus status = findPropertyDefRes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return Either.right(status);
		}

		Either<GraphRelation, TitanOperationStatus> relation = titanGenericDao.getRelation(findResInstanceRes.left().value(), findPropertyDefRes.left().value(), GraphEdgeLabels.PROPERTY_VALUE);
		if (relation.isRight()) {
			// TODO: add error in case of error
			TitanOperationStatus status = relation.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return Either.right(status);
		}

		Either<PropertyValueData, TitanOperationStatus> deleteNode = titanGenericDao.deleteNode(findPropertyDefRes.left().value(), PropertyValueData.class);
		if (deleteNode.isRight()) {
			return Either.right(deleteNode.right().value());
		}
		PropertyValueData value = deleteNode.left().value();
		return Either.left(value);

	}

	public Either<ComponentInstanceProperty, StorageOperationStatus> removePropertyValueFromResourceInstance(String propertyValueUid, String resourceInstanceId, boolean inTransaction) {

		Either<ComponentInstanceProperty, StorageOperationStatus> result = null;

		try {

			Either<PropertyValueData, TitanOperationStatus> eitherStatus = this.removePropertyOfResourceInstance(propertyValueUid, resourceInstanceId);

			if (eitherStatus.isRight()) {
				log.error("Failed to remove property value {} from resource instance {} in Graph. status is {}", propertyValueUid, resourceInstanceId, eitherStatus.right().value().name());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(eitherStatus.right().value()));
				return result;
			} else {
				PropertyValueData propertyValueData = eitherStatus.left().value();

				ComponentInstanceProperty propertyValueResult = new ComponentInstanceProperty();
				propertyValueResult.setUniqueId(resourceInstanceId);
				propertyValueResult.setValue(propertyValueData.getValue());

				log.debug("The returned ResourceInstanceProperty is  {}", propertyValueResult);
				result = Either.left(propertyValueResult);
				return result;
			}
		}

		finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.error("Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}

	}

	public ComponentInstanceProperty buildResourceInstanceProperty(PropertyValueData propertyValueData, ComponentInstanceProperty resourceInstanceProperty) {

		String value = propertyValueData.getValue();
		String uid = propertyValueData.getUniqueId();
		ComponentInstanceProperty instanceProperty = new ComponentInstanceProperty(resourceInstanceProperty, value, uid);
		instanceProperty.setPath(resourceInstanceProperty.getPath());

		return instanceProperty;
	}

	public static class PropertyConstraintJacksonDeserialiser extends org.codehaus.jackson.map.JsonDeserializer<PropertyConstraint> {

		@Override
		public PropertyConstraint deserialize(org.codehaus.jackson.JsonParser json, DeserializationContext context) throws IOException, JsonProcessingException {

			ObjectCodec oc = json.getCodec();
			JsonNode node = oc.readTree(json);
			return null;
		}
	}

	@Override
	public boolean isPropertyDefaultValueValid(IComplexDefaultValue propertyDefinition, Map<String, DataTypeDefinition> dataTypes) {
		if (propertyDefinition == null) {
			return false;
		}
		boolean isValid = false;
		String innerType = null;
		String propertyType = propertyDefinition.getType();
		ToscaPropertyType type = getType(propertyType);
		if (type == ToscaPropertyType.LIST || type == ToscaPropertyType.MAP) {
			SchemaDefinition def = propertyDefinition.getSchema();
			if (def == null) {
				return false;
			}
			PropertyDataDefinition propDef = def.getProperty();
			if (propDef == null) {
				return false;
			}
			innerType = propDef.getType();
		}
		String value = propertyDefinition.getDefaultValue();
		if (type != null) {
			isValid = isValidValue(type, value, innerType, dataTypes);
		} else {
			log.trace("The given type {} is not a pre defined one.", propertyType);

			DataTypeDefinition foundDt = dataTypes.get(propertyType);
			if (foundDt != null) {
				isValid = isValidComplexValue(foundDt, value, dataTypes);
			} else {
				isValid = false;
			}
		}
		return isValid;
	}

	public boolean isPropertyTypeValid(IComplexDefaultValue property) {

		if (property == null) {
			return false;
		}

		if (ToscaPropertyType.isValidType(property.getType()) == null) {

			Either<Boolean, TitanOperationStatus> definedInDataTypes = isDefinedInDataTypes(property.getType());

			if (definedInDataTypes.isRight()) {
				return false;
			} else {
				Boolean isExist = definedInDataTypes.left().value();
				return isExist.booleanValue();
			}

		}
		return true;
	}

	@Override
	public ImmutablePair<String, Boolean> isPropertyInnerTypeValid(IComplexDefaultValue property, Map<String, DataTypeDefinition> dataTypes) {

		if (property == null) {
			return new ImmutablePair<String, Boolean>(null, false);
		}

		SchemaDefinition schema;
		PropertyDataDefinition innerProp;
		String innerType = null;
		if ((schema = property.getSchema()) != null) {
			if ((innerProp = schema.getProperty()) != null) {
				innerType = innerProp.getType();
			}
		}

		ToscaPropertyType innerToscaType = ToscaPropertyType.isValidType(innerType);

		if (innerToscaType == null) {
			DataTypeDefinition dataTypeDefinition = dataTypes.get(innerType);
			if (dataTypeDefinition == null) {
				log.debug("The inner type {} is not a data type.", innerType);
				return new ImmutablePair<String, Boolean>(innerType, false);
			} else {
				log.debug("The inner type {} is a data type. Data type definition is {}", innerType, dataTypeDefinition);
			}
		}

		return new ImmutablePair<String, Boolean>(innerType, true);
	}

	private boolean isValidComplexValue(DataTypeDefinition foundDt, String value, Map<String, DataTypeDefinition> dataTypes) {
		/*
		 * Either<Map<String, DataTypeDefinition>, TitanOperationStatus> allDataTypesRes = getAllDataTypes(); if (allDataTypesRes.isRight()) { TitanOperationStatus status = allDataTypesRes.right().value();
		 * return false; }
		 * 
		 * Map<String, DataTypeDefinition> allDataTypes = allDataTypesRes.left().value();
		 */
		ImmutablePair<JsonElement, Boolean> validateAndUpdate = dataTypeValidatorConverter.validateAndUpdate(value, foundDt, dataTypes);

		log.trace("The result after validating complex value of type {} is {}", foundDt.getName(), validateAndUpdate);

		return validateAndUpdate.right.booleanValue();

	}

	private Either<Map<String, DataTypeDefinition>, TitanOperationStatus> findAllDataTypeDefinition(DataTypeDefinition dataTypeDefinition) {

		Map<String, DataTypeDefinition> nameToDataTypeDef = new HashMap<>();

		DataTypeDefinition typeDefinition = dataTypeDefinition;

		while (typeDefinition != null) {

			List<PropertyDefinition> properties = typeDefinition.getProperties();
			if (properties != null) {
				for (PropertyDefinition propertyDefinition : properties) {
					String type = propertyDefinition.getType();
					Either<DataTypeDefinition, TitanOperationStatus> dataTypeByName = this.getDataTypeUsingName(type);
					if (dataTypeByName.isRight()) {
						return Either.right(dataTypeByName.right().value());
					} else {
						DataTypeDefinition value = dataTypeByName.left().value();
						if (false == nameToDataTypeDef.containsKey(type)) {
							nameToDataTypeDef.put(type, value);
						}
					}

				}
			}

			typeDefinition = typeDefinition.getDerivedFrom();
		}

		return Either.left(nameToDataTypeDef);
	}

	public Either<List<ComponentInstanceProperty>, TitanOperationStatus> getAllPropertiesOfResourceInstanceOnlyPropertyDefId(String resourceInstanceUid, NodeTypeEnum instanceNodeType) {

		Either<TitanVertex, TitanOperationStatus> findResInstanceRes = titanGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(instanceNodeType), resourceInstanceUid);

		if (findResInstanceRes.isRight()) {
			TitanOperationStatus status = findResInstanceRes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return Either.right(status);
		}

		Either<List<ImmutablePair<TitanVertex, Edge>>, TitanOperationStatus> propertyImplNodes = titanGenericDao.getChildrenVertecies(UniqueIdBuilder.getKeyByNodeType(instanceNodeType), resourceInstanceUid, GraphEdgeLabels.PROPERTY_VALUE);

		if (propertyImplNodes.isRight()) {
			TitanOperationStatus status = propertyImplNodes.right().value();
			return Either.right(status);
		}

		List<ImmutablePair<TitanVertex, Edge>> list = propertyImplNodes.left().value();
		if (list == null || true == list.isEmpty()) {
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}

		List<ComponentInstanceProperty> result = new ArrayList<>();
		for (ImmutablePair<TitanVertex, Edge> propertyValue : list) {
			TitanVertex propertyValueDataVertex = propertyValue.getLeft();
			String propertyValueUid = (String) titanGenericDao.getProperty(propertyValueDataVertex, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
			String value = (String) titanGenericDao.getProperty(propertyValueDataVertex, GraphPropertiesDictionary.VALUE.getProperty());

			ImmutablePair<TitanVertex, Edge> propertyDefPair = titanGenericDao.getChildVertex(propertyValueDataVertex, GraphEdgeLabels.PROPERTY_IMPL);
			if (propertyDefPair == null) {
				return Either.right(TitanOperationStatus.NOT_FOUND);
			}

			Map<String, Object> properties = titanGenericDao.getProperties(propertyValueDataVertex);
			PropertyValueData propertyValueData = GraphElementFactory.createElement(NodeTypeEnum.PropertyValue.getName(), GraphElementTypeEnum.Node, properties, PropertyValueData.class);
			String propertyUniqueId = (String) titanGenericDao.getProperty(propertyDefPair.left, GraphPropertiesDictionary.UNIQUE_ID.getProperty());

			ComponentInstanceProperty resourceInstanceProperty = new ComponentInstanceProperty();
			// set property original unique id
			resourceInstanceProperty.setUniqueId(propertyUniqueId);
			// set resource id
			// TODO: esofer add resource id
			resourceInstanceProperty.setParentUniqueId(null);
			// set value
			resourceInstanceProperty.setValue(value);
			// set property value unique id
			resourceInstanceProperty.setValueUniqueUid(propertyValueUid);
			// set rules
			resourceInstanceProperty.setRules(propertyValueData.getRules());

			result.add(resourceInstanceProperty);
		}

		return Either.left(result);
	}

	/**
	 * Find the default value from the list of component instances. Start the search from the second component instance
	 * 
	 * @param pathOfComponentInstances
	 * @param propertyUniqueId
	 * @param defaultValue
	 * @return
	 */
	public Either<String, TitanOperationStatus> findDefaultValueFromSecondPosition(List<String> pathOfComponentInstances, String propertyUniqueId, String defaultValue) {

		log.trace("In find default value: path= {} propertyUniqId={} defaultValue= {}", pathOfComponentInstances, propertyUniqueId, defaultValue);

		if (pathOfComponentInstances == null || pathOfComponentInstances.size() < 2) {
			return Either.left(defaultValue);
		}

		String result = defaultValue;

		for (int i = 1; i < pathOfComponentInstances.size(); i++) {
			String compInstanceId = pathOfComponentInstances.get(i);

			Either<List<ComponentInstanceProperty>, TitanOperationStatus> propertyValuesResult = this.getAllPropertiesOfResourceInstanceOnlyPropertyDefId(compInstanceId, NodeTypeEnum.ResourceInstance);

			log.trace("After fetching properties values of component instance {}. {}", compInstanceId, propertyValuesResult);

			if (propertyValuesResult.isRight()) {
				TitanOperationStatus status = propertyValuesResult.right().value();
				if (status != TitanOperationStatus.NOT_FOUND) {
					return Either.right(status);
				} else {
					continue;
				}
			}

			ComponentInstanceProperty foundCompInstanceProperty = fetchByPropertyUid(propertyValuesResult.left().value(), propertyUniqueId);
			log.trace("After finding the component instance property on{} . {}", compInstanceId, foundCompInstanceProperty);

			if (foundCompInstanceProperty == null) {
				continue;
			}

			List<PropertyRule> rules = getOrBuildRulesIfNotExists(pathOfComponentInstances.size() - i, pathOfComponentInstances.get(i), foundCompInstanceProperty.getRules(), foundCompInstanceProperty.getValue());

			log.trace("Rules of property {} on component instance {} are {}", propertyUniqueId, compInstanceId, rules);
			PropertyRule matchedRule = findMatchRule(pathOfComponentInstances, i, rules);
			log.trace("Match rule is {}", matchedRule);

			if (matchedRule != null) {
				result = matchedRule.getValue();
				break;
			}

		}

		return Either.left(result);

	}

	private ComponentInstanceProperty fetchByPropertyUid(List<ComponentInstanceProperty> list, String propertyUniqueId) {

		ComponentInstanceProperty result = null;

		if (list == null) {
			return null;
		}

		for (ComponentInstanceProperty instProperty : list) {
			if (instProperty.getUniqueId().equals(propertyUniqueId)) {
				result = instProperty;
				break;
			}
		}

		return result;
	}

	private List<PropertyRule> getOrBuildRulesIfNotExists(int ruleSize, String compInstanceId, List<PropertyRule> rules, String value) {

		if (rules != null) {
			return rules;
		}

		rules = buildDefaultRule(compInstanceId, ruleSize, value);

		return rules;

	}

	private List<PropertyRule> getRulesOfPropertyValue(int size, String instanceId, ComponentInstanceProperty componentInstanceProperty) {
		List<PropertyRule> rules = componentInstanceProperty.getRules();
		if (rules == null) {
			rules = buildDefaultRule(instanceId, size, componentInstanceProperty.getValue());
		}
		return rules;
	}

	private List<PropertyRule> buildDefaultRule(String componentInstanceId, int size, String value) {

		List<PropertyRule> rules = new ArrayList<>();
		List<String> rule = new ArrayList<>();
		rule.add(componentInstanceId);
		for (int i = 0; i < size - 1; i++) {
			rule.add(PropertyRule.RULE_ANY_MATCH);
		}
		PropertyRule propertyRule = new PropertyRule(rule, value);
		rules.add(propertyRule);

		return rules;

	}

	private PropertyRule findMatchRule(List<String> pathOfInstances, int level, List<PropertyRule> rules) {

		PropertyRule propertyRule = null;

		String stringForMatch = buildStringForMatch(pathOfInstances, level);

		String firstCompInstance = pathOfInstances.get(level);

		if (rules != null) {

			for (PropertyRule rule : rules) {

				int ruleSize = rule.getRule().size();
				// check the length of the rule equals to the length of the
				// instances path.
				if (ruleSize != pathOfInstances.size() - level) {
					continue;
				}
				// check that the rule starts with correct component instance id
				if (false == checkFirstItem(firstCompInstance, rule.getFirstToken())) {
					continue;
				}

				String secondToken = rule.getToken(2);
				if (secondToken != null && (secondToken.equals(PropertyRule.FORCE_ALL) || secondToken.equals(PropertyRule.ALL))) {
					propertyRule = rule;
					break;
				}

				String patternStr = buildStringForMatch(rule.getRule(), 0);

				Pattern pattern = Pattern.compile(patternStr);

				Matcher matcher = pattern.matcher(stringForMatch);

				if (matcher.matches()) {
					if (log.isTraceEnabled()) {
						log.trace("{} matches the rule {}", stringForMatch, patternStr);
					}
					propertyRule = rule;
					break;
				}
			}

		}

		return propertyRule;
	}

	private boolean checkFirstItem(String left, String right) {
		if (left != null && left.equals(right)) {
			return true;
		}
		return false;
	}

	private String buildStringForMatch(List<String> pathOfInstances, int level) {
		StringBuilder builder = new StringBuilder();

		for (int i = level; i < pathOfInstances.size(); i++) {
			builder.append(pathOfInstances.get(i));
			if (i < pathOfInstances.size() - 1) {
				builder.append("#");
			}
		}
		return builder.toString();
	}

	public void updatePropertyByBestMatch(String propertyUniqueId, ComponentInstanceProperty instanceProperty, Map<String, ComponentInstanceProperty> instanceIdToValue) {

		List<String> pathOfInstances = instanceProperty.getPath();
		int level = 0;
		int size = pathOfInstances.size();
		int numberOfMatches = 0;
		for (String instanceId : pathOfInstances) {
			ComponentInstanceProperty componentInstanceProperty = instanceIdToValue.get(instanceId);

			if (componentInstanceProperty != null) {

				List<PropertyRule> rules = getRulesOfPropertyValue(size - level, instanceId, componentInstanceProperty);
				// If it is the first level instance, then update valueUniuqeId
				// parameter in order to know on update that
				// we should update and not create new node on graph.
				if (level == 0) {
					instanceProperty.setValueUniqueUid(componentInstanceProperty.getValueUniqueUid());
					instanceProperty.setRules(rules);
				}

				PropertyRule rule = findMatchRule(pathOfInstances, level, rules);
				if (rule != null) {
					numberOfMatches++;
					String value = rule.getValue();
					if (numberOfMatches == 1) {
						instanceProperty.setValue(value);
						if (log.isDebugEnabled()) {
							log.debug("Set the value of property {} {} on path {} to be {}", propertyUniqueId, instanceProperty.getName(), pathOfInstances, value);
						}
					} else if (numberOfMatches == 2) {
						// In case of another property value match, then use the
						// value to be the default value of the property.
						instanceProperty.setDefaultValue(value);
						if (log.isDebugEnabled()) {
							log.debug("Set the default value of property {} {} on path {} to be {}", propertyUniqueId, instanceProperty.getName(), pathOfInstances, value);
						}
						break;
					}
				}
			}
			level++;
		}

	}

	public void updatePropertiesByPropertyValues(Map<String, List<ComponentInstanceProperty>> resourceInstancesProperties, Map<String, Map<String, ComponentInstanceProperty>> values) {

		if (resourceInstancesProperties == null) {
			return;
		}

		List<ComponentInstanceProperty> allProperties = new ArrayList<>();
		Collection<List<ComponentInstanceProperty>> properties = resourceInstancesProperties.values();
		if (properties != null) {
			Iterator<List<ComponentInstanceProperty>> iterator = properties.iterator();
			while (iterator.hasNext()) {
				List<ComponentInstanceProperty> compInstancePropertyList = iterator.next();
				allProperties.addAll(compInstancePropertyList);
			}
		}

		// Go over each property and check whether there is a rule which updates
		// it
		for (ComponentInstanceProperty instanceProperty : allProperties) {

			String propertyUniqueId = instanceProperty.getUniqueId();

			// get the changes per componentInstanceId.
			Map<String, ComponentInstanceProperty> instanceIdToValue = values.get(propertyUniqueId);

			if (instanceIdToValue == null) {
				continue;
			}

			this.updatePropertyByBestMatch(propertyUniqueId, instanceProperty, instanceIdToValue);

		}

	}

	/**
	 * 
	 * Add data type to graph.
	 * 
	 * 1. Add data type node
	 * 
	 * 2. Add edge between the former node to its parent(if exists)
	 * 
	 * 3. Add property node and associate it to the node created at #1. (per property & if exists)
	 * 
	 * @param dataTypeDefinition
	 * @return
	 */
	private Either<DataTypeData, TitanOperationStatus> addDataTypeToGraph(DataTypeDefinition dataTypeDefinition) {

		log.debug("Got data type {}", dataTypeDefinition);

		String dtUniqueId = UniqueIdBuilder.buildDataTypeUid(dataTypeDefinition.getName());

		DataTypeData dataTypeData = buildDataTypeData(dataTypeDefinition, dtUniqueId);

		log.debug("Before adding data type to graph. dataTypeData = {}", dataTypeData);
		Either<DataTypeData, TitanOperationStatus> createDataTypeResult = titanGenericDao.createNode(dataTypeData, DataTypeData.class);
		log.debug("After adding data type to graph. status is = {}", createDataTypeResult);

		if (createDataTypeResult.isRight()) {
			TitanOperationStatus operationStatus = createDataTypeResult.right().value();
			log.debug("Failed to data type {} to graph. status is {}", dataTypeDefinition.getName(), operationStatus);
			BeEcompErrorManager.getInstance().logBeFailedAddingNodeTypeError("AddDataType", NodeTypeEnum.DataType.getName());
			return Either.right(operationStatus);
		}

		DataTypeData resultCTD = createDataTypeResult.left().value();
		List<PropertyDefinition> properties = dataTypeDefinition.getProperties();
		Either<Map<String, PropertyData>, TitanOperationStatus> addPropertiesToDataType = addPropertiesToDataType(resultCTD.getUniqueId(), properties);
		if (addPropertiesToDataType.isRight()) {
			log.debug("Failed add properties {} to data type {}", properties, dataTypeDefinition.getName());
			return Either.right(addPropertiesToDataType.right().value());
		}

		String derivedFrom = dataTypeDefinition.getDerivedFromName();
		if (derivedFrom != null) {
			log.debug("Before creating relation between data type {} to its parent {}", dtUniqueId, derivedFrom);
			UniqueIdData from = new UniqueIdData(NodeTypeEnum.DataType, dtUniqueId);

			String deriveFromUid = UniqueIdBuilder.buildDataTypeUid(derivedFrom);
			UniqueIdData to = new UniqueIdData(NodeTypeEnum.DataType, deriveFromUid);
			Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(from, to, GraphEdgeLabels.DERIVED_FROM, null);
			log.debug("After create relation between capability type {} to its parent {}. status is {}", dtUniqueId, derivedFrom, createRelation);
			if (createRelation.isRight()) {
				return Either.right(createRelation.right().value());
			}
		}

		return Either.left(createDataTypeResult.left().value());

	}

	private DataTypeData buildDataTypeData(DataTypeDefinition dataTypeDefinition, String ctUniqueId) {

		DataTypeData dataTypeData = new DataTypeData(dataTypeDefinition);

		dataTypeData.getDataTypeDataDefinition().setUniqueId(ctUniqueId);
		Long creationDate = dataTypeData.getDataTypeDataDefinition().getCreationTime();
		if (creationDate == null) {
			creationDate = System.currentTimeMillis();
		}
		dataTypeData.getDataTypeDataDefinition().setCreationTime(creationDate);
		dataTypeData.getDataTypeDataDefinition().setModificationTime(creationDate);

		return dataTypeData;
	}

	/**
	 * add properties to capability type.
	 * 
	 * Per property, add a property node and associate it to the capability type
	 * 
	 * @param uniqueId
	 * @param properties
	 * @return
	 */
	private Either<Map<String, PropertyData>, TitanOperationStatus> addPropertiesToDataType(String uniqueId, List<PropertyDefinition> properties) {

		Map<String, PropertyData> propertiesData = new HashMap<String, PropertyData>();

		if (properties != null && false == properties.isEmpty()) {
			for (PropertyDefinition propertyDefinition : properties) {
				String propertyName = propertyDefinition.getName();

				String propertyType = propertyDefinition.getType();
				Either<Boolean, TitanOperationStatus> validPropertyType = isValidPropertyType(propertyType);
				if (validPropertyType.isRight()) {
					log.debug("Data type {} contains invalid property type {}", uniqueId, propertyType);
					return Either.right(validPropertyType.right().value());
				}
				Boolean isValid = validPropertyType.left().value();
				if (isValid == null || isValid.booleanValue() == false) {
					log.debug("Data type {} contains invalid property type {}", uniqueId, propertyType);
					return Either.right(TitanOperationStatus.INVALID_TYPE);
				}

				Either<PropertyData, TitanOperationStatus> addPropertyToNodeType = this.addPropertyToNodeType(propertyName, propertyDefinition, NodeTypeEnum.DataType, uniqueId);
				if (addPropertyToNodeType.isRight()) {
					TitanOperationStatus operationStatus = addPropertyToNodeType.right().value();
					log.debug("Failed to associate data type {} to property {} in graph. status is {}", uniqueId, propertyName, operationStatus);
					BeEcompErrorManager.getInstance().logInternalFlowError("AddPropertyToDataType", "Failed to associate property to data type. Status is " + operationStatus, ErrorSeverity.ERROR);
					return Either.right(operationStatus);
				}
				propertiesData.put(propertyName, addPropertyToNodeType.left().value());
			}

			DataTypeData dataTypeData = new DataTypeData();
			dataTypeData.getDataTypeDataDefinition().setUniqueId(uniqueId);
			long modificationTime = System.currentTimeMillis();
			dataTypeData.getDataTypeDataDefinition().setModificationTime(modificationTime);

			Either<DataTypeData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(dataTypeData, DataTypeData.class);
			if (updateNode.isRight()) {
				TitanOperationStatus operationStatus = updateNode.right().value();
				log.debug("Failed to update modification time data type {} from graph. status is {}", uniqueId, operationStatus);
				BeEcompErrorManager.getInstance().logInternalFlowError("AddPropertyToDataType", "Failed to fetch data type. Status is " + operationStatus, ErrorSeverity.ERROR);
				return Either.right(operationStatus);
			} else {
				log.debug("Update data type uid {}. Set modification time to {}", uniqueId, modificationTime);
			}

		}

		return Either.left(propertiesData);

	}

	/**
	 * Build Data type object from graph by unique id
	 * 
	 * @param uniqueId
	 * @return
	 */
	public Either<DataTypeDefinition, TitanOperationStatus> getDataTypeByUid(String uniqueId) {

		Either<DataTypeDefinition, TitanOperationStatus> result = null;

		Either<DataTypeData, TitanOperationStatus> dataTypesRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.DataType), uniqueId, DataTypeData.class);

		if (dataTypesRes.isRight()) {
			TitanOperationStatus status = dataTypesRes.right().value();
			log.debug("Data type {} cannot be found in graph. status is {}", uniqueId, status);
			return Either.right(status);
		}

		DataTypeData ctData = dataTypesRes.left().value();
		DataTypeDefinition dataTypeDefinition = new DataTypeDefinition(ctData.getDataTypeDataDefinition());

		TitanOperationStatus propertiesStatus = fillProperties(uniqueId, dataTypeDefinition);
		if (propertiesStatus != TitanOperationStatus.OK) {
			log.error("Failed to fetch properties of data type {}", uniqueId);
			return Either.right(propertiesStatus);
		}

		Either<ImmutablePair<DataTypeData, GraphEdge>, TitanOperationStatus> parentNode = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.DataType), uniqueId, GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.DataType,
				DataTypeData.class);
		log.debug("After retrieving DERIVED_FROM node of {}. status is {}", uniqueId, parentNode);
		if (parentNode.isRight()) {
			TitanOperationStatus titanOperationStatus = parentNode.right().value();
			if (titanOperationStatus != TitanOperationStatus.NOT_FOUND) {
				log.error("Failed to find the parent data type of data type {}. status is {}", uniqueId, titanOperationStatus);
				result = Either.right(titanOperationStatus);
				return result;
			}
		} else {
			// derived from node was found
			ImmutablePair<DataTypeData, GraphEdge> immutablePair = parentNode.left().value();
			DataTypeData parentCT = immutablePair.getKey();

			String parentUniqueId = parentCT.getUniqueId();
			Either<DataTypeDefinition, TitanOperationStatus> dataTypeByUid = getDataTypeByUid(parentUniqueId);

			if (dataTypeByUid.isRight()) {
				return Either.right(dataTypeByUid.right().value());
			}

			DataTypeDefinition parentDataTypeDefinition = dataTypeByUid.left().value();

			dataTypeDefinition.setDerivedFrom(parentDataTypeDefinition);

		}
		result = Either.left(dataTypeDefinition);

		return result;
	}

	private TitanOperationStatus fillProperties(String uniqueId, DataTypeDefinition dataTypeDefinition) {

		Either<Map<String, PropertyDefinition>, TitanOperationStatus> findPropertiesOfNode = this.findPropertiesOfNode(NodeTypeEnum.DataType, uniqueId);
		if (findPropertiesOfNode.isRight()) {
			TitanOperationStatus titanOperationStatus = findPropertiesOfNode.right().value();
			log.debug("After looking for properties of vertex {}. status is {}", uniqueId, titanOperationStatus);
			if (TitanOperationStatus.NOT_FOUND.equals(titanOperationStatus)) {
				return TitanOperationStatus.OK;
			} else {
				return titanOperationStatus;
			}
		} else {
			Map<String, PropertyDefinition> properties = findPropertiesOfNode.left().value();
			if (properties != null && properties.isEmpty() == false) {
				List<PropertyDefinition> listOfProps = new ArrayList<>();

				for (Entry<String, PropertyDefinition> entry : properties.entrySet()) {
					String propName = entry.getKey();
					PropertyDefinition propertyDefinition = entry.getValue();
					PropertyDefinition newPropertyDefinition = new PropertyDefinition(propertyDefinition);
					newPropertyDefinition.setName(propName);
					listOfProps.add(newPropertyDefinition);
				}
				dataTypeDefinition.setProperties(listOfProps);
			}
			return TitanOperationStatus.OK;
		}
	}

	private Either<DataTypeDefinition, StorageOperationStatus> addDataType(DataTypeDefinition dataTypeDefinition, boolean inTransaction) {

		Either<DataTypeDefinition, StorageOperationStatus> result = null;

		try {

			Either<DataTypeData, TitanOperationStatus> eitherStatus = addDataTypeToGraph(dataTypeDefinition);

			if (eitherStatus.isRight()) {
				log.debug("Failed to add data type {} to Graph. status is {}", dataTypeDefinition, eitherStatus.right().value().name());
				BeEcompErrorManager.getInstance().logBeFailedAddingNodeTypeError("AddDataType", "DataType");
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(eitherStatus.right().value()));
				return result;
			} else {
				DataTypeData capabilityTypeData = eitherStatus.left().value();

				DataTypeDefinition dataTypeDefResult = convertDTDataToDTDefinition(capabilityTypeData);
				log.debug("The returned CapabilityTypeDefinition is {}", dataTypeDefResult);
				result = Either.left(dataTypeDefResult);
				return result;
			}
		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.error("Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}

	}

	@Override
	public Either<DataTypeDefinition, StorageOperationStatus> addDataType(DataTypeDefinition dataTypeDefinition) {
		return addDataType(dataTypeDefinition, true);
	}

	@Override
	public Either<DataTypeDefinition, StorageOperationStatus> getDataTypeByName(String name, boolean inTransaction) {

		Either<DataTypeDefinition, StorageOperationStatus> result = null;
		try {

			String dtUid = UniqueIdBuilder.buildDataTypeUid(name);
			Either<DataTypeDefinition, TitanOperationStatus> ctResult = this.getDataTypeByUid(dtUid);

			if (ctResult.isRight()) {
				TitanOperationStatus status = ctResult.right().value();
				if (status != TitanOperationStatus.NOT_FOUND) {
					log.error("Failed to retrieve information on capability type {} status is {}", name, status);
				}
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(ctResult.right().value()));
				return result;
			}

			result = Either.left(ctResult.left().value());

			return result;
		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.error("Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}

	}

	@Override
	public Either<DataTypeDefinition, StorageOperationStatus> getDataTypeByName(String name) {
		return getDataTypeByName(name, true);
	}

	@Override
	public Either<DataTypeDefinition, StorageOperationStatus> getDataTypeByNameWithoutDerived(String name) {
		return getDataTypeByNameWithoutDerived(name, true);
	}

	private Either<DataTypeDefinition, StorageOperationStatus> getDataTypeByNameWithoutDerived(String name, boolean inTransaction) {

		Either<DataTypeDefinition, StorageOperationStatus> result = null;
		try {

			String uid = UniqueIdBuilder.buildDataTypeUid(name);
			Either<DataTypeDefinition, TitanOperationStatus> ctResult = this.getDataTypeByUidWithoutDerivedDataTypes(uid);

			if (ctResult.isRight()) {
				TitanOperationStatus status = ctResult.right().value();
				if (status != TitanOperationStatus.NOT_FOUND) {
					log.error("Failed to retrieve information on capability type {} status is {}", name, status);
				}
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(ctResult.right().value()));
				return result;
			}

			result = Either.left(ctResult.left().value());

			return result;
		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.error("Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}

	}

	public Either<DataTypeDefinition, TitanOperationStatus> getDataTypeByUidWithoutDerivedDataTypes(String uniqueId) {

		Either<DataTypeData, TitanOperationStatus> dataTypesRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.DataType), uniqueId, DataTypeData.class);

		if (dataTypesRes.isRight()) {
			TitanOperationStatus status = dataTypesRes.right().value();
			log.debug("Data type {} cannot be found in graph. status is {}", uniqueId, status);
			return Either.right(status);
		}

		DataTypeData ctData = dataTypesRes.left().value();
		DataTypeDefinition dataTypeDefinition = new DataTypeDefinition(ctData.getDataTypeDataDefinition());

		TitanOperationStatus propertiesStatus = fillProperties(uniqueId, dataTypeDefinition);
		if (propertiesStatus != TitanOperationStatus.OK) {
			log.error("Failed to fetch properties of data type {}", uniqueId);
			return Either.right(propertiesStatus);
		}

		return Either.left(dataTypeDefinition);
	}

	public Either<DataTypeDefinition, TitanOperationStatus> getDataTypeByNameWithoutDerivedDataTypes(String name) {

		String uid = UniqueIdBuilder.buildDataTypeUid(name);
		return getDataTypeByUidWithoutDerivedDataTypes(uid);

	}

	/**
	 * 
	 * convert between graph Node object to Java object
	 * 
	 * @param dataTypeData
	 * @return
	 */
	protected DataTypeDefinition convertDTDataToDTDefinition(DataTypeData dataTypeData) {
		log.debug("The object returned after create data type is {}", dataTypeData);

		DataTypeDefinition dataTypeDefResult = new DataTypeDefinition(dataTypeData.getDataTypeDataDefinition());

		return dataTypeDefResult;
	}

	private Either<Boolean, TitanOperationStatus> isValidPropertyType(String propertyType) {

		if (propertyType == null || propertyType.isEmpty()) {
			return Either.left(false);
		}

		ToscaPropertyType toscaPropertyType = ToscaPropertyType.isValidType(propertyType);
		if (toscaPropertyType == null) {
			Either<Boolean, TitanOperationStatus> definedInDataTypes = isDefinedInDataTypes(propertyType);
			return definedInDataTypes;
		} else {
			return Either.left(true);
		}
	}

	public Either<Boolean, TitanOperationStatus> isDefinedInDataTypes(String propertyType) {

		String dataTypeUid = UniqueIdBuilder.buildDataTypeUid(propertyType);
		Either<DataTypeDefinition, TitanOperationStatus> dataTypeByUid = getDataTypeByUid(dataTypeUid);
		if (dataTypeByUid.isRight()) {
			TitanOperationStatus status = dataTypeByUid.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				return Either.left(false);
			}
			return Either.right(status);
		}

		return Either.left(true);

	}

	private Either<DataTypeDefinition, TitanOperationStatus> getExternalDataType(String propertyType) {

		String dataTypeUid = UniqueIdBuilder.buildDataTypeUid(propertyType);
		Either<DataTypeDefinition, TitanOperationStatus> dataTypeByUid = getDataTypeByUid(dataTypeUid);
		if (dataTypeByUid.isRight()) {
			TitanOperationStatus status = dataTypeByUid.right().value();
			return Either.right(status);
		}

		return Either.left(dataTypeByUid.left().value());

	}

	public Either<Map<String, DataTypeDefinition>, TitanOperationStatus> getAllDataTypes() {

		Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
		Either<Map<String, DataTypeDefinition>, TitanOperationStatus> result = Either.left(dataTypes);

		Either<List<DataTypeData>, TitanOperationStatus> getAllDataTypes = titanGenericDao.getByCriteria(NodeTypeEnum.DataType, null, DataTypeData.class);
		if (getAllDataTypes.isRight()) {
			TitanOperationStatus status = getAllDataTypes.right().value();
			if (status != TitanOperationStatus.NOT_FOUND) {
				return Either.right(status);
			} else {
				return result;
			}
		}

		List<DataTypeData> list = getAllDataTypes.left().value();
		if (list != null) {

			log.trace("Number of data types to load is {}" , list.size());

			List<String> collect = list.stream().map(p -> p.getDataTypeDataDefinition().getName()).collect(Collectors.toList());
			log.trace("The data types to load are {}" , collect);

			for (DataTypeData dataTypeData : list) {

				log.trace("Going to fetch data type {}. uid is {}", dataTypeData.getDataTypeDataDefinition().getName(), dataTypeData.getUniqueId());
				Either<DataTypeDefinition, TitanOperationStatus> dataTypeByUid = this.getAndAddDataTypeByUid(dataTypeData.getUniqueId(), dataTypes);
				if (dataTypeByUid.isRight()) {
					TitanOperationStatus status = dataTypeByUid.right().value();
					if (status == TitanOperationStatus.NOT_FOUND) {
						status = TitanOperationStatus.INVALID_ID;
					}
					return Either.right(status);
				}
			}
		}

		if (log.isTraceEnabled()) {
			if (result.isRight()) {
				log.trace("After fetching all data types {}" , result);
			} else {
				Map<String, DataTypeDefinition> map = result.left().value();
				if (map != null) {
					String types = map.keySet().stream().collect(Collectors.joining(",", "[", "]"));
					log.trace("After fetching all data types {} " , types);
				}
			}
		}

		return result;
	}

	/**
	 * Build Data type object from graph by unique id
	 * 
	 * @param uniqueId
	 * @return
	 */
	public Either<DataTypeDefinition, TitanOperationStatus> getAndAddDataTypeByUid(String uniqueId, Map<String, DataTypeDefinition> allDataTypes) {

		Either<DataTypeDefinition, TitanOperationStatus> result = null;

		if (allDataTypes.containsKey(uniqueId)) {
			return Either.left(allDataTypes.get(uniqueId));
		}

		Either<DataTypeData, TitanOperationStatus> dataTypesRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.DataType), uniqueId, DataTypeData.class);

		if (dataTypesRes.isRight()) {
			TitanOperationStatus status = dataTypesRes.right().value();
			log.debug("Data type {} cannot be found in graph. status is {}", uniqueId, status);
			return Either.right(status);
		}

		DataTypeData ctData = dataTypesRes.left().value();
		DataTypeDefinition dataTypeDefinition = new DataTypeDefinition(ctData.getDataTypeDataDefinition());

		TitanOperationStatus propertiesStatus = fillProperties(uniqueId, dataTypeDefinition);
		if (propertiesStatus != TitanOperationStatus.OK) {
			log.error("Failed to fetch properties of data type {}", uniqueId);
			return Either.right(propertiesStatus);
		}

		allDataTypes.put(dataTypeDefinition.getName(), dataTypeDefinition);

		String derivedFrom = dataTypeDefinition.getDerivedFromName();
		if (allDataTypes.containsKey(derivedFrom)) {
			DataTypeDefinition parentDataTypeDefinition = allDataTypes.get(derivedFrom);

			dataTypeDefinition.setDerivedFrom(parentDataTypeDefinition);

			return Either.left(dataTypeDefinition);
		}

		Either<ImmutablePair<DataTypeData, GraphEdge>, TitanOperationStatus> parentNode = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.DataType), uniqueId, GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.DataType,
				DataTypeData.class);
		log.debug("After retrieving DERIVED_FROM node of {}. status is {}", uniqueId, parentNode);
		if (parentNode.isRight()) {
			TitanOperationStatus titanOperationStatus = parentNode.right().value();
			if (titanOperationStatus != TitanOperationStatus.NOT_FOUND) {
				log.error("Failed to find the parent data type of data type {}. status is {}", uniqueId, titanOperationStatus);
				result = Either.right(titanOperationStatus);
				return result;
			}
		} else {
			// derived from node was found
			ImmutablePair<DataTypeData, GraphEdge> immutablePair = parentNode.left().value();
			DataTypeData parentCT = immutablePair.getKey();

			String parentUniqueId = parentCT.getUniqueId();
			Either<DataTypeDefinition, TitanOperationStatus> dataTypeByUid = getDataTypeByUid(parentUniqueId);

			if (dataTypeByUid.isRight()) {
				return Either.right(dataTypeByUid.right().value());
			}

			DataTypeDefinition parentDataTypeDefinition = dataTypeByUid.left().value();

			dataTypeDefinition.setDerivedFrom(parentDataTypeDefinition);

		}
		result = Either.left(dataTypeDefinition);

		return result;
	}

	public Either<DataTypeDefinition, TitanOperationStatus> getDataTypeUsingName(String name) {

		String uid = UniqueIdBuilder.buildDataTypeUid(name);

		Either<DataTypeDefinition, TitanOperationStatus> dataTypeByUid = getDataTypeByUid(uid);

		return dataTypeByUid;
	}

	public Either<String, TitanOperationStatus> checkInnerType(PropertyDataDefinition propDataDef) {

		String propertyType = propDataDef.getType();

		ToscaPropertyType type = ToscaPropertyType.isValidType(propertyType);

		Either<String, TitanOperationStatus> result = getInnerType(type, () -> propDataDef.getSchema());

		return result;
	}

	public Either<List<DataTypeData>, TitanOperationStatus> getAllDataTypeNodes() {
		Either<List<DataTypeData>, TitanOperationStatus> getAllDataTypes = titanGenericDao.getByCriteria(NodeTypeEnum.DataType, null, DataTypeData.class);
		if (getAllDataTypes.isRight()) {
			TitanOperationStatus status = getAllDataTypes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.OK;
				return Either.right(status);
			}
		}
		return getAllDataTypes;
	}

	public Either<Object, Boolean> validateAndUpdatePropertyValue(String propertyType, String value, boolean isValidate, String innerType, Map<String, DataTypeDefinition> dataTypes) {
		log.trace("Going to validate property value and its type. type = {}, value = {}", propertyType, value);
		ToscaPropertyType type = getType(propertyType);

		if (isValidate) {

			if (type == null) {
				DataTypeDefinition dataTypeDefinition = dataTypes.get(propertyType);
				ImmutablePair<JsonElement, Boolean> validateResult = dataTypeValidatorConverter.validateAndUpdate(value, dataTypeDefinition, dataTypes);
				if (validateResult.right.booleanValue() == false) {
					log.debug("The value {} of property from type {} is invalid", value, propertyType);
					return Either.right(false);
				}
				JsonElement jsonElement = validateResult.left;
				String valueFromJsonElement = getValueFromJsonElement(jsonElement);
				return Either.left(valueFromJsonElement);
			}
			log.trace("before validating property type {}", propertyType);
			boolean isValidProperty = isValidValue(type, value, innerType, dataTypes);
			if (false == isValidProperty) {
				log.debug("The value {} of property from type {} is invalid", value, type);
				return Either.right(false);
			}
		}
		Object convertedValue = value;
		if (false == isEmptyValue(value) && isValidate) {
			PropertyValueConverter converter = type.getConverter();
			convertedValue = converter.convert(value, innerType, dataTypes);
		}
		return Either.left(convertedValue);
	}

	public Either<Object, Boolean> validateAndUpdatePropertyValue(String propertyType, String value, String innerType, Map<String, DataTypeDefinition> dataTypes) {
		return validateAndUpdatePropertyValue(propertyType, value, true, innerType, dataTypes);
	}

	/*
	 * @Override public PropertyOperation getPropertyOperation() { return this; }
	 */
	protected TitanOperationStatus fillProperties(String uniqueId, Consumer<List<PropertyDefinition>> propertySetter) {
		Either<Map<String, PropertyDefinition>, TitanOperationStatus> findPropertiesOfNode = this.findPropertiesOfNode(NodeTypeEnum.GroupType, uniqueId);
		if (findPropertiesOfNode.isRight()) {
			TitanOperationStatus titanOperationStatus = findPropertiesOfNode.right().value();
			log.debug("After looking for properties of vertex {}. status is {}", uniqueId, titanOperationStatus);
			if (TitanOperationStatus.NOT_FOUND.equals(titanOperationStatus)) {
				return TitanOperationStatus.OK;
			} else {
				return titanOperationStatus;
			}
		} else {
			Map<String, PropertyDefinition> properties = findPropertiesOfNode.left().value();

			if (properties != null) {
				List<PropertyDefinition> propertiesAsList = properties.entrySet().stream().map(p -> p.getValue()).collect(Collectors.toList());
				propertySetter.accept(propertiesAsList);
			}

			return TitanOperationStatus.OK;
		}
	}

	/**
	 * add properties to element type.
	 * 
	 * Per property, add a property node and associate it to the element type
	 * 
	 * @param uniqueId
	 * @param propertiesMap
	 *            
	 * @return
	 */
	protected Either<Map<String, PropertyData>, TitanOperationStatus> addPropertiesToElementType(String uniqueId, NodeTypeEnum nodeType, Map<String, PropertyDefinition> propertiesMap) {

		Map<String, PropertyData> propertiesData = new HashMap<String, PropertyData>();

		if (propertiesMap != null) {

			for (Entry<String, PropertyDefinition> propertyDefinitionEntry : propertiesMap.entrySet()) {
				String propertyName = propertyDefinitionEntry.getKey();

				Either<PropertyData, TitanOperationStatus> addPropertyToNodeType = this.addPropertyToNodeType(propertyName, propertyDefinitionEntry.getValue(), nodeType, uniqueId);

				if (addPropertyToNodeType.isRight()) {
					TitanOperationStatus operationStatus = addPropertyToNodeType.right().value();
					log.error("Failed to associate {} {} to property {} in graph. status is {}", nodeType.getName(), uniqueId, propertyName, operationStatus);
					return Either.right(operationStatus);
				}
				propertiesData.put(propertyName, addPropertyToNodeType.left().value());

			}
		}

		return Either.left(propertiesData);

	}

	protected TitanOperationStatus addPropertiesToElementType(String uniqueId, NodeTypeEnum nodeType, Map<String, PropertyDefinition> propertiesMap, TitanVertex elementVertex) {

		if (propertiesMap != null) {

			for (Entry<String, PropertyDefinition> propertyDefinitionEntry : propertiesMap.entrySet()) {
				String propertyName = propertyDefinitionEntry.getKey();

				TitanOperationStatus operationStatus = this.addPropertyToNodeType(elementVertex, propertyName, propertyDefinitionEntry.getValue(), nodeType, uniqueId);

				if (!operationStatus.equals(TitanOperationStatus.OK)) {
					log.error("Failed to associate {} {}  to property {} in graph. status is {}", nodeType.getName(), uniqueId, propertyName, operationStatus);
					return operationStatus;
				}
			}
		}

		return TitanOperationStatus.OK;

	}

	public Either<Map<String, PropertyData>, TitanOperationStatus> addPropertiesToElementType(String uniqueId, NodeTypeEnum elementType, List<PropertyDefinition> properties) {

		Map<String, PropertyDefinition> propMap;
		if (properties == null) {
			propMap = null;
		} else {
			propMap = properties.stream().collect(Collectors.toMap(propDef -> propDef.getName(), propDef -> propDef));
		}
		return addPropertiesToElementType(uniqueId, elementType, propMap);
	}

	public TitanOperationStatus addPropertiesToElementType(TitanVertex elementVertex, String uniqueId, NodeTypeEnum elementType, List<PropertyDefinition> properties) {

		Map<String, PropertyDefinition> propMap;
		if (properties == null) {
			propMap = null;
		} else {
			propMap = properties.stream().collect(Collectors.toMap(propDef -> propDef.getName(), propDef -> propDef));
		}
		return addPropertiesToElementType(uniqueId, elementType, propMap, elementVertex);
	}

	@Override
	public Either<DataTypeDefinition, StorageOperationStatus> updateDataType(DataTypeDefinition newDataTypeDefinition, DataTypeDefinition oldDataTypeDefinition) {
		return updateDataType(newDataTypeDefinition, oldDataTypeDefinition, true);
	}

	private Either<DataTypeDefinition, StorageOperationStatus> updateDataType(DataTypeDefinition newDataTypeDefinition, DataTypeDefinition oldDataTypeDefinition, boolean inTransaction) {

		Either<DataTypeDefinition, StorageOperationStatus> result = null;

		try {

			List<PropertyDefinition> newProperties = newDataTypeDefinition.getProperties();

			List<PropertyDefinition> oldProperties = oldDataTypeDefinition.getProperties();

			String newDerivedFromName = getDerivedFromName(newDataTypeDefinition);

			String oldDerivedFromName = getDerivedFromName(oldDataTypeDefinition);

			String dataTypeName = newDataTypeDefinition.getName();

			List<PropertyDefinition> propertiesToAdd = new ArrayList<>();
			if (isPropertyOmitted(newProperties, oldProperties, dataTypeName) || isPropertyTypeChanged(dataTypeName, newProperties, oldProperties, propertiesToAdd) || isDerivedFromNameChanged(dataTypeName, newDerivedFromName, oldDerivedFromName)) {

				log.debug("The new data type {} is invalid.", dataTypeName);

				result = Either.right(StorageOperationStatus.CANNOT_UPDATE_EXISTING_ENTITY);
				return result;
			}

			if (propertiesToAdd == null || propertiesToAdd.isEmpty()) {
				log.debug("No new properties has been defined in the new data type {}", newDataTypeDefinition);
				result = Either.right(StorageOperationStatus.OK);
				return result;
			}

			Either<Map<String, PropertyData>, TitanOperationStatus> addPropertiesToDataType = addPropertiesToDataType(oldDataTypeDefinition.getUniqueId(), propertiesToAdd);

			if (addPropertiesToDataType.isRight()) {
				log.debug("Failed to update data type {} to Graph. Status is {}", oldDataTypeDefinition, addPropertiesToDataType.right().value().name());
				BeEcompErrorManager.getInstance().logBeFailedAddingNodeTypeError("UpdateDataType", "Property");
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(addPropertiesToDataType.right().value()));
				return result;
			} else {

				Either<DataTypeDefinition, TitanOperationStatus> dataTypeByUid = this.getDataTypeByUid(oldDataTypeDefinition.getUniqueId());
				if (dataTypeByUid.isRight()) {
					TitanOperationStatus status = addPropertiesToDataType.right().value();
					log.debug("Failed to get data type {} after update. Status is {}", oldDataTypeDefinition.getUniqueId(), status.name());
					BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError("UpdateDataType", "Property", status.name());
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				} else {
					result = Either.left(dataTypeByUid.left().value());
				}
			}

			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.error("Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}

	}

	private String getDerivedFromName(DataTypeDefinition dataTypeDefinition) {
		String derivedFromName = dataTypeDefinition.getDerivedFromName();
		return derivedFromName;
	}

	private boolean isPropertyTypeChanged(String dataTypeName, List<PropertyDefinition> newProperties, List<PropertyDefinition> oldProperties, List<PropertyDefinition> outputPropertiesToAdd) {

		if (newProperties != null && oldProperties != null) {

			Map<String, PropertyDefinition> newPropsMapper = newProperties.stream().collect(Collectors.toMap(p -> p.getName(), p -> p));
			Map<String, PropertyDefinition> oldPropsMapper = oldProperties.stream().collect(Collectors.toMap(p -> p.getName(), p -> p));

			for (Entry<String, PropertyDefinition> newPropertyEntry : newPropsMapper.entrySet()) {

				String propName = newPropertyEntry.getKey();
				PropertyDefinition propDef = newPropertyEntry.getValue();

				PropertyDefinition oldPropertyDefinition = oldPropsMapper.get(propName);
				if (oldPropertyDefinition == null) {
					log.debug("New property {} received in the data type {}", propName, dataTypeName);
					outputPropertiesToAdd.add(propDef);
					continue;
				}

				String oldType = oldPropertyDefinition.getType();
				String oldEntryType = getEntryType(oldPropertyDefinition);

				String newType = propDef.getType();
				String newEntryType = getEntryType(propDef);

				if (false == oldType.equals(newType)) {
					log.debug("Existing property {} in data type {} has a differnet type {} than the new one {}", propName, dataTypeName, oldType, newType);
					return true;
				}

				if (false == equalsEntryTypes(oldEntryType, newEntryType)) {
					log.debug("Existing property {} in data type {} has a differnet entry type {} than the new one {}", propName, dataTypeName, oldEntryType, newEntryType);
					return true;
				}

			}

		}

		return false;
	}

	private boolean equalsEntryTypes(String oldEntryType, String newEntryType) {

		if (oldEntryType == null && newEntryType == null) {
			return true;
		} else if (oldEntryType != null && newEntryType != null) {
			return oldEntryType.equals(newEntryType);
		} else {
			return false;
		}
	}

	private String getEntryType(PropertyDefinition oldPropertyDefinition) {
		String entryType = null;
		SchemaDefinition schema = oldPropertyDefinition.getSchema();
		if (schema != null) {
			PropertyDataDefinition schemaProperty = schema.getProperty();
			if (schemaProperty != null) {
				entryType = schemaProperty.getType();
			}
		}
		return entryType;
	}

	private boolean isPropertyOmitted(List<PropertyDefinition> newProperties, List<PropertyDefinition> oldProperties, String dataTypeName) {

		boolean isValid = validateChangeInCaseOfEmptyProperties(newProperties, oldProperties, dataTypeName);
		if (false == isValid) {
			log.debug("At least one property is missing in the new data type {}", dataTypeName);
			return false;
		}

		if (newProperties != null && oldProperties != null) {

			List<String> newProps = newProperties.stream().map(p -> p.getName()).collect(Collectors.toList());
			List<String> oldProps = oldProperties.stream().map(p -> p.getName()).collect(Collectors.toList());

			if (false == newProps.containsAll(oldProps)) {
				StringJoiner joiner = new StringJoiner(",", "[", "]");
				newProps.forEach(p -> joiner.add(p));
				log.debug("Properties {} in data type {} are missing, but they already defined in the existing data type", joiner.toString(), dataTypeName);
				return true;
			}

		}
		return false;
	}

	private boolean validateChangeInCaseOfEmptyProperties(List<PropertyDefinition> newProperties, List<PropertyDefinition> oldProperties, String dataTypeName) {

		if (newProperties != null) {
			if (newProperties.isEmpty()) {
				newProperties = null;
			}
		}

		if (oldProperties != null) {
			if (oldProperties.isEmpty()) {
				oldProperties = null;
			}
		}

		if ((newProperties == null && oldProperties == null) || (newProperties != null && oldProperties != null)) {
			return true;
		}

		return false;
	}

	private boolean isDerivedFromNameChanged(String dataTypeName, String newDerivedFromName, String oldDerivedFromName) {

		if (newDerivedFromName != null) {
			boolean isEqual = newDerivedFromName.equals(oldDerivedFromName);
			if (false == isEqual) {
				log.debug("The new datatype {} derived from another data type {} than the existing one {}", dataTypeName, newDerivedFromName, oldDerivedFromName);
			}
			return !isEqual;
		} else if (oldDerivedFromName == null) {
			return false;
		} else {// new=null, old != null
			log.debug("The new datatype {} derived from another data type {} than the existing one {}", dataTypeName, newDerivedFromName, oldDerivedFromName);
			return true;
		}

	}

	/**
	 * 
	 * Future - unfinished
	 * 
	 * @param type
	 * @param value
	 * @return
	 */
	public boolean isValueToscaFunction(String type, String value) {

		boolean result = false;

		if (ToscaPropertyType.STRING.getType().equals(type) || isScalarDerivedFromString(type)) {

		}

		String[] functions = { "get_input" };

		if (value != null) {

			for (String function : functions) {

			}

		}

		return result;

	}

	/**
	 * Future - unfinished
	 * 
	 * @param type
	 * @return
	 */
	private boolean isScalarDerivedFromString(String type) {
		// TODO Auto-generated method stub
		return false;
	}
}
