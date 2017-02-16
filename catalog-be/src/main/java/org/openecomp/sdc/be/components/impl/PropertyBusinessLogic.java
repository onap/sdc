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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;

import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IResourceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.openecomp.sdc.be.resources.data.EntryData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import fj.data.Either;

@Component("propertyBusinessLogic")
public class PropertyBusinessLogic extends BaseBusinessLogic {

	private static final String CREATE_PROPERTY = "CreateProperty";

	private static Logger log = LoggerFactory.getLogger(PropertyBusinessLogic.class.getName());

	@javax.annotation.Resource
	private IResourceOperation resourceOperation = null;

	@javax.annotation.Resource
	private PropertyOperation propertyOperation = null;

	@javax.annotation.Resource
	private ComponentsUtils componentsUtils;

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
			Either<Resource, StorageOperationStatus> status = getResource(resourceId);
			if (status.isRight()) {
				result = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, ""));
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
				if (propertyOperation.isPropertyExist(resourceProperties, resourceId, propertyName)) {
					result = Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_ALREADY_EXIST, ""));
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
			Either<PropertyData, StorageOperationStatus> either = propertyOperation.addProperty(propertyName, newPropertyDefinition, resourceId);
			if (either.isRight()) {
				result = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(either.right().value()), resource.getName()));
				return result;
			}

			PropertyDefinition createdPropertyDefinition = propertyOperation.convertPropertyDataToPropertyDefinition(either.left().value(), propertyName, resourceId);
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
	 *            TODO
	 * @return
	 */
	public Either<Entry<String, PropertyDefinition>, ResponseFormat> getProperty(String resourceId, String propertyId, String userId) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "create Component Instance", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		// Get the resource from DB
		Either<Resource, StorageOperationStatus> status = getResource(resourceId);
		if (status.isRight()) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, ""));
		}
		Resource resource = status.left().value();

		// verify property exist in resource
		List<PropertyDefinition> properties = resource.getProperties();
		if (properties == null) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, ""));
		}
		for (PropertyDefinition property : properties) {
			// esofer - check also that the property belongs to the current
			// resource
			if (property.getUniqueId().equals(propertyId) && property.getParentUniqueId().equals(resourceId)) {
				Map<String, PropertyDefinition> propMap = new HashMap<>();
				propMap.put(property.getName(), property);
				return Either.left(propMap.entrySet().iterator().next());
			}
		}
		return Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, ""));
	}

	/**
	 * delete property of resource from graph
	 * 
	 * @param resourceId
	 * @param propertyId
	 * @param userId
	 * @return
	 */
	public Either<EntryData<String, PropertyDefinition>, ResponseFormat> deleteProperty(String resourceId, String propertyId, String userId) {

		Either<EntryData<String, PropertyDefinition>, ResponseFormat> result = null;

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
			Either<Resource, StorageOperationStatus> status = getResource(resourceId);
			if (status.isRight()) {
				result = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, ""));
				return result;
			}
			Resource resource = status.left().value();

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
			String propertyName = statusGetProperty.left().value().getKey();

			// delete property of resource from graph
			// TODO: need to get StorageOperationStatus
			Either<PropertyData, StorageOperationStatus> either = propertyOperation.deleteProperty(propertyId);
			// Either<PropertyData, StorageOperationStatus> either =
			// propertyOperation.deletePropertyFromGraph(propertyId);
			if (either.isRight()) {
				result = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(either.right().value()), resource.getName()));
				return result;
			}
			// propertyOperation.getTitanGenericDao().commit();
			PropertyDefinition createdPropertyDefinition = propertyOperation.convertPropertyDataToPropertyDefinition(either.left().value(), propertyName, resourceId);
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

		// Get the resource from DB
		Either<Resource, StorageOperationStatus> status = getResource(resourceId);
		if (status.isRight()) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, ""));
		}
		Resource resource = status.left().value();

		// verify that resource is checked-out and the user is the last updater
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

			// verify property exist in resource
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
			// validate property default values
			Either<Boolean, ResponseFormat> defaultValuesValidation = validatePropertyDefaultValue(newPropertyDefinition, dataTypes);
			if (defaultValuesValidation.isRight()) {
				result = Either.right(defaultValuesValidation.right().value());
				return result;
			}

			// add the new property to resource on graph
			// TODO: convert TitanOperationStatus to Storgae...
			Either<PropertyData, StorageOperationStatus> either = propertyOperation.updateProperty(propertyId, newPropertyDefinition, dataTypes);
			// Either<PropertyData, StorageOperationStatus> either =
			// propertyOperation.updatePropertyFromGraph(propertyId,
			// newPropertyDefinition);
			if (either.isRight()) {
				log.debug("Problem while updating property with id {}. Reason - {}", propertyId, either.right().value());
				result = Either.right(componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(either.right().value()), resource.getName()));
				return result;
				// return Either.right(ActionStatus.GENERAL_ERROR);
			}

			PropertyDefinition createdPropertyDefinition = propertyOperation.convertPropertyDataToPropertyDefinition(either.left().value(), propertyName, resourceId);
			EntryData<String, PropertyDefinition> property = new EntryData<String, PropertyDefinition>(propertyName, createdPropertyDefinition);
			result = Either.left(property);
			return result;

		} finally {
			commitOrRollback(result);
			// unlock component
			graphLockOperation.unlockComponent(resourceId, NodeTypeEnum.Resource);
		}

	}

}
