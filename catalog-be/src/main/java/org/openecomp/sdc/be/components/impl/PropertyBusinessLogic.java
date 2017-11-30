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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.IComplexDefaultValue;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation.PropertyConstraintDeserialiser;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.openecomp.sdc.be.model.tosca.validators.DataTypeValidatorConverter;
import org.openecomp.sdc.be.model.tosca.validators.PropertyTypeValidator;
import org.openecomp.sdc.be.resources.data.EntryData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

@Component("propertyBusinessLogic")
public class PropertyBusinessLogic extends BaseBusinessLogic {

	private static final String CREATE_PROPERTY = "CreateProperty";

	private static Logger log = LoggerFactory.getLogger(PropertyBusinessLogic.class.getName());

	private static final String EMPTY_VALUE = null;
	
	private DataTypeValidatorConverter dataTypeValidatorConverter = DataTypeValidatorConverter.getInstance();

	protected static IElementOperation getElementDao(Class<IElementOperation> class1, ServletContext context) {
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);

		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);

		return webApplicationContext.getBean(class1);
	}

	public Either<Map<String, DataTypeDefinition>, ResponseFormat> getAllDataTypes() {
		Either<Map<String, DataTypeDefinition>, ResponseFormat> eitherAllDataTypes = getAllDataTypes(applicationDataTypeCache);
		return eitherAllDataTypes;
	}

	/**
	 * Create new property on resource in graph
	 * 
	 * @param resourceId
	 * @param propertyName
	 * @param newPropertyDefinition
	 * @param userId
	 * @return Either<PropertyDefinition, ActionStatus>
	 */
	public Either<EntryData<String, PropertyDefinition>, ResponseFormat> createProperty(String resourceId, String propertyName, PropertyDefinition newPropertyDefinition, String userId) {

		Either<EntryData<String, PropertyDefinition>, ResponseFormat> result = null;

		Either<User, ResponseFormat> resp = validateUserExists(userId, "create Property", false);
		if (resp.isRight()) {
			result = Either.right(resp.right().value());
			return result;
		}

		StorageOperationStatus lockResult = graphLockOperation.lockComponent(resourceId, NodeTypeEnum.Resource);
		if (!lockResult.equals(StorageOperationStatus.OK)) {
			BeEcompErrorManager.getInstance().logBeFailedLockObjectError(CREATE_PROPERTY, NodeTypeEnum.Resource.name().toLowerCase(), resourceId);
			log.info("Failed to lock component {}. Error - {}", resourceId, lockResult);
			result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
			return result;
		}

		try {
			// Get the resource from DB
			Either<Resource, StorageOperationStatus> status = toscaOperationFacade.getToscaElement(resourceId);
			if (status.isRight()) {
				result = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, resourceId));
				return result;
			}
			Resource resource = status.left().value();

			// verify that resource is checked-out and the user is the last
			// updater
			if (!ComponentValidationUtils.canWorkOnResource(resource, userId)) {
				result = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
				return result;
			}

			// verify property not exist in resource
			List<PropertyDefinition> resourceProperties = resource.getProperties();

			if (resourceProperties != null) {
				if (isPropertyExist(resourceProperties, resourceId, propertyName, newPropertyDefinition.getType())) {
					result = Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_ALREADY_EXIST, propertyName));
					return result;
				}
			}

			Either<Map<String, DataTypeDefinition>, ResponseFormat> allDataTypes = getAllDataTypes(applicationDataTypeCache);
			if (allDataTypes.isRight()) {
				result = Either.right(allDataTypes.right().value());
				return result;
			}

			Map<String, DataTypeDefinition> dataTypes = allDataTypes.left().value();

			// validate property default values
			Either<Boolean, ResponseFormat> defaultValuesValidation = validatePropertyDefaultValue(newPropertyDefinition, dataTypes);
			if (defaultValuesValidation.isRight()) {
				result = Either.right(defaultValuesValidation.right().value());
				return result;
			}
			// convert property
			ToscaPropertyType type = getType(newPropertyDefinition.getType());
			if (type != null) {
				PropertyValueConverter converter = type.getConverter();
				// get inner type
				String innerType = null;
				if (newPropertyDefinition != null) {
					SchemaDefinition schema = newPropertyDefinition.getSchema();
					if (schema != null) {
						PropertyDataDefinition prop = schema.getProperty();
						if (prop != null) {
							innerType = prop.getType();
						}
					}
					String convertedValue = null;
					if (newPropertyDefinition.getDefaultValue() != null) {
						convertedValue = converter.convert(newPropertyDefinition.getDefaultValue(), innerType, allDataTypes.left().value());
						newPropertyDefinition.setDefaultValue(convertedValue);
					}
				}
			}

			// add the new property to resource on graph
			// need to get StorageOpaerationStatus and convert to ActionStatus
			// from componentsUtils
			Either<PropertyDefinition, StorageOperationStatus> either = toscaOperationFacade.addPropertyToResource(propertyName, newPropertyDefinition, resource);
			if (either.isRight()) {
				result = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(either.right().value()), resource.getName()));
				return result;
			}

			PropertyDefinition createdPropertyDefinition = either.left().value();
			EntryData<String, PropertyDefinition> property = new EntryData<String, PropertyDefinition>(propertyName, createdPropertyDefinition);
			result = Either.left(property);
			return result;

		} finally {
			commitOrRollback(result);
			// unlock component
			graphLockOperation.unlockComponent(resourceId, NodeTypeEnum.Resource);
		}

	}

	/**
	 * Get property of resource
	 * 
	 * @param resourceId
	 * @param propertyId
	 * @param userId
	 * @return
	 */
	public Either<Entry<String, PropertyDefinition>, ResponseFormat> getProperty(String resourceId, String propertyId, String userId) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "create Component Instance", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		// Get the resource from DB
		Either<Resource, StorageOperationStatus> status =  toscaOperationFacade.getToscaElement(resourceId);
		if (status.isRight()) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, resourceId));
		}
		Resource resource = status.left().value();

		// verify property exist in resource
		List<PropertyDefinition> properties = resource.getProperties();
		if (properties == null) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, ""));
		}
		for (PropertyDefinition property : properties) {
			if (property.getUniqueId().equals(propertyId) ) {
				Map<String, PropertyDefinition> propMap = new HashMap<>();
				propMap.put(property.getName(), property);
				return Either.left(propMap.entrySet().iterator().next());
			}
		}
		return Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, ""));
	}

	private boolean isPropertyBelongsToResource(PropertyDataDefinition property, String resourceId) {
		return  property.getOwnerId() == null || property.getOwnerId().equals(resourceId);
	}

	/**
	 * delete property of resource from graph
	 * 
	 * @param resourceId
	 * @param propertyId
	 * @param userId
	 * @return
	 */
	public Either<Entry<String, PropertyDefinition>, ResponseFormat> deleteProperty(String resourceId, String propertyId, String userId) {

		Either<Entry<String, PropertyDefinition>, ResponseFormat> result = null;

		Either<User, ResponseFormat> resp = validateUserExists(userId, "delete Property", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		StorageOperationStatus lockResult = graphLockOperation.lockComponent(resourceId, NodeTypeEnum.Resource);
		if (!lockResult.equals(StorageOperationStatus.OK)) {
			BeEcompErrorManager.getInstance().logBeFailedLockObjectError(CREATE_PROPERTY, NodeTypeEnum.Resource.name().toLowerCase(), resourceId);
			result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
			return result;
		}

		try {

			// Get the resource from DB
			Either<Resource, StorageOperationStatus> getResourceRes = toscaOperationFacade.getToscaElement(resourceId);
			if (getResourceRes.isRight()) {
				result = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, resourceId));
				return result;
			}
			Resource resource = getResourceRes.left().value();

			// verify that resource is checked-out and the user is the last
			// updater
			if (!ComponentValidationUtils.canWorkOnResource(resource, userId)) {
				result = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
				return result;
			}

			// verify property exist in resource
			Either<Entry<String, PropertyDefinition>, ResponseFormat> statusGetProperty = getProperty(resourceId, propertyId, userId);
			if (statusGetProperty.isRight()) {
				result = Either.right(statusGetProperty.right().value());
				return result;
			}

			StorageOperationStatus status = toscaOperationFacade.deletePropertyOfResource(resource, statusGetProperty.left().value().getKey());
			if (status != StorageOperationStatus.OK) {
				result = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status), resource.getName()));
				return result;
			}
			// propertyOperation.getTitanGenericDao().commit();
			result = Either.left(statusGetProperty.left().value());
			return result;

		} finally {
			commitOrRollback(result);
			// unlock component
			graphLockOperation.unlockComponent(resourceId, NodeTypeEnum.Resource);
		}
	}

	/**
	 * update property
	 * 
	 * @param resourceId
	 * @param propertyId
	 * @param newPropertyDefinition
	 * @param userId
	 * @return
	 */
	public Either<EntryData<String, PropertyDefinition>, ResponseFormat> updateProperty(String resourceId, String propertyId, PropertyDefinition newPropertyDefinition, String userId) {

		Either<EntryData<String, PropertyDefinition>, ResponseFormat> result = null;

		Either<Resource, StorageOperationStatus> status = toscaOperationFacade.getToscaElement(resourceId);
		if (status.isRight()) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, resourceId));
		}
		Resource resource = status.left().value();

		if (!ComponentValidationUtils.canWorkOnResource(resource, userId)) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
		}

		StorageOperationStatus lockResult = graphLockOperation.lockComponent(resourceId, NodeTypeEnum.Resource);
		if (!lockResult.equals(StorageOperationStatus.OK)) {
			BeEcompErrorManager.getInstance().logBeFailedLockObjectError(CREATE_PROPERTY, NodeTypeEnum.Resource.name().toLowerCase(), resourceId);
			result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
			return result;
		}

		try {
			Either<Entry<String, PropertyDefinition>, ResponseFormat> statusGetProperty = getProperty(resourceId, propertyId, userId);
			if (statusGetProperty.isRight()) {
				result = Either.right(statusGetProperty.right().value());
				return result;
			}
			String propertyName = statusGetProperty.left().value().getKey();

			Either<Map<String, DataTypeDefinition>, ResponseFormat> allDataTypes = getAllDataTypes(applicationDataTypeCache);
			if (allDataTypes.isRight()) {
				result = Either.right(allDataTypes.right().value());
				return result;
			}
			Map<String, DataTypeDefinition> dataTypes = allDataTypes.left().value();

			Either<Boolean, ResponseFormat> defaultValuesValidation = validatePropertyDefaultValue(newPropertyDefinition, dataTypes);
			if (defaultValuesValidation.isRight()) {
				result = Either.right(defaultValuesValidation.right().value());
				return result;
			}

			Either<PropertyDefinition, StorageOperationStatus> either = handleProperty(propertyId, newPropertyDefinition, dataTypes);
			if (either.isRight()) {
				log.debug("Problem while updating property with id {}. Reason - {}", propertyId, either.right().value());
				result = Either.right(componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(either.right().value()), resource.getName()));
				return result;
			}


			either = toscaOperationFacade.updatePropertyOfResource(resource, newPropertyDefinition);
			if (either.isRight()) {
				result = Either.right(componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(either.right().value()), resource.getName()));
				return result;
			}
			
			EntryData<String, PropertyDefinition> property = new EntryData<String, PropertyDefinition>(propertyName, either.left().value());
			result = Either.left(property);
			return result;

		} finally {
			commitOrRollback(result);
			graphLockOperation.unlockComponent(resourceId, NodeTypeEnum.Resource);
		}

	}
	
	private boolean isPropertyExist(List<PropertyDefinition> properties, String resourceUid, String propertyName, String propertyType) {
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

	private PropertyDefinition convertPropertyDataToPropertyDefinition(PropertyData propertyDataResult, String propertyName, String resourceId) {
		log.debug("The object returned after create property is {}", propertyDataResult);
		PropertyDefinition propertyDefResult = new PropertyDefinition(propertyDataResult.getPropertyDataDefinition());
		propertyDefResult.setConstraints(convertConstraints(propertyDataResult.getConstraints()));
		propertyDefResult.setName(propertyName);
		propertyDefResult.setParentUniqueId(resourceId);
		return propertyDefResult;
	}
	
	private List<PropertyConstraint> convertConstraints(List<String> constraints) {

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
	
	private Either<PropertyDefinition, StorageOperationStatus> handleProperty(String propertyId, PropertyDefinition newPropertyDefinition, Map<String, DataTypeDefinition> dataTypes) {

		StorageOperationStatus validateAndUpdateProperty = validateAndUpdateProperty(newPropertyDefinition, dataTypes);
		if (validateAndUpdateProperty != StorageOperationStatus.OK) {
			return Either.right(validateAndUpdateProperty);
		}

		return Either.left(newPropertyDefinition);
	}
	
	private StorageOperationStatus validateAndUpdateProperty(IComplexDefaultValue propertyDefinition, Map<String, DataTypeDefinition> dataTypes) {

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
}
