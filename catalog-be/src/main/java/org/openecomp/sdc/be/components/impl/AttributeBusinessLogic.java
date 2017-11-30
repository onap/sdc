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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

/**
 * This class holds the business logic relevant for attributes manipulation.
 * 
 * @author mshitrit
 *
 */
@Component("attributeBusinessLogic")
public class AttributeBusinessLogic extends BaseBusinessLogic {

	private static final String CREATE_ATTRIBUTE = "CreateAttribute";
	private static final String UPDATE_ATTRIBUTE = "UpdateAttribute";
	private static final String DELETE_ATTRIBUTE = "DeleteAttribute";

	private static Logger log = LoggerFactory.getLogger(AttributeBusinessLogic.class.getName());

	/**
	 * Created attribute on the resource with resourceId
	 * 
	 * @param resourceId
	 * @param newAttributeDef
	 * @param userId
	 * @return AttributeDefinition if created successfully Or ResponseFormat
	 */
	public Either<PropertyDefinition, ResponseFormat> createAttribute(String resourceId, PropertyDefinition newAttributeDef, String userId) {
		Either<PropertyDefinition, ResponseFormat> result = null;
		Either<User, ResponseFormat> resp = validateUserExists(userId, "create Attribute", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		StorageOperationStatus lockResult = graphLockOperation.lockComponent(resourceId, NodeTypeEnum.Resource);
		if (lockResult != StorageOperationStatus.OK) {
			BeEcompErrorManager.getInstance().logBeFailedLockObjectError(CREATE_ATTRIBUTE, NodeTypeEnum.Resource.name().toLowerCase(), resourceId);
			log.info("Failed to lock component {}. Error - {}", resourceId, lockResult);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
		}

		try {
			// Get the resource from DB
			Either<Resource, StorageOperationStatus> status = toscaOperationFacade.getToscaElement(resourceId);
			if (status.isRight()) {
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, resourceId));
			}
			Resource resource = status.left().value();

			// verify that resource is checked-out and the user is the last updater
			if (!ComponentValidationUtils.canWorkOnResource(resource, userId)) {
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
			}

			// verify attribute does not exist in resource
			if (isAttributeExist(resource.getAttributes(), resourceId, newAttributeDef.getName())) {
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.ATTRIBUTE_ALREADY_EXIST, newAttributeDef.getName()));
			}
			Either<Map<String, DataTypeDefinition>, ResponseFormat> eitherAllDataTypes = getAllDataTypes(applicationDataTypeCache);
			if (eitherAllDataTypes.isRight()) {
				return Either.right(eitherAllDataTypes.right().value());
			}
			// validate property default values
			Either<Boolean, ResponseFormat> defaultValuesValidation = validatePropertyDefaultValue(newAttributeDef, eitherAllDataTypes.left().value());
			if (defaultValuesValidation.isRight()) {
				return Either.right(defaultValuesValidation.right().value());
			}

			handleDefaultValue(newAttributeDef, eitherAllDataTypes.left().value());

			// add the new attribute to resource on graph
			// need to get StorageOpaerationStatus and convert to ActionStatus from
			// componentsUtils
			Either<PropertyDefinition, StorageOperationStatus> either = toscaOperationFacade.addAttributeOfResource(resource, newAttributeDef);
			if (either.isRight()) {
				result = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(either.right().value()), resource.getName()));
				return result;
			}
			result = Either.left(either.left().value());
		
			return result;
		} finally {
			commitOrRollback(result);
			graphLockOperation.unlockComponent(resourceId, NodeTypeEnum.Resource);
		}

	}

	private boolean isAttributeExist(List<PropertyDefinition> attributes, String resourceUid, String propertyName) {
		boolean isExist = false;
		if (attributes != null) {
			isExist = attributes.stream().filter(p -> Objects.equals(p.getName(), propertyName) && Objects.equals(p.getParentUniqueId(), resourceUid)).findAny().isPresent();
		}
		return isExist;

	}

	/**
	 * @param resourceId
	 * @param attributeId
	 * @param userId
	 * @return
	 */
	public Either<PropertyDefinition, ResponseFormat> getAttribute(String resourceId, String attributeId, String userId) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "get Attribute", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		// Get the resource from DB
		Either<Resource, StorageOperationStatus> status = toscaOperationFacade.getToscaElement(resourceId);
		if (status.isRight()) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, resourceId));
		}
		Resource resource = status.left().value();

		List<PropertyDefinition> attributes = resource.getAttributes();
		if (attributes == null) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.ATTRIBUTE_NOT_FOUND, ""));
		} else {
			Either<PropertyDefinition, ResponseFormat> result;
			// verify attribute exist in resource
			Optional<PropertyDefinition> optionalAtt = attributes.stream().filter(att -> att.getUniqueId().equals(attributeId) && att.getParentUniqueId().equals(resourceId)).findAny();

			if (optionalAtt.isPresent()) {
				result = Either.left(optionalAtt.get());
			} else {
				result = Either.right(componentsUtils.getResponseFormat(ActionStatus.ATTRIBUTE_NOT_FOUND, ""));
			}
			return result;
		}

	}

	/**
	 * Updates Attribute on resource
	 * 
	 * @param resourceId
	 * @param attributeId
	 * @param newAttDef
	 * @param userId
	 * @return
	 */
	public Either<PropertyDefinition, ResponseFormat> updateAttribute(String resourceId, String attributeId, PropertyDefinition newAttDef, String userId) {
		Either<PropertyDefinition, ResponseFormat> result = null;

		StorageOperationStatus lockResult = graphLockOperation.lockComponent(resourceId, NodeTypeEnum.Resource);
		if (lockResult != StorageOperationStatus.OK) {
			BeEcompErrorManager.getInstance().logBeFailedLockObjectError(UPDATE_ATTRIBUTE, NodeTypeEnum.Resource.name().toLowerCase(), resourceId);
			log.info("Failed to lock component {}. Error - {}", resourceId, lockResult);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
		try {
			// Get the resource from DB
			Either<Resource, StorageOperationStatus> eitherResource = toscaOperationFacade.getToscaElement(resourceId);
			if (eitherResource.isRight()) {
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, resourceId));
			}
			Resource resource = eitherResource.left().value();

			// verify that resource is checked-out and the user is the last updater
			if (!ComponentValidationUtils.canWorkOnResource(resource, userId)) {
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
			}

			// verify attribute exist in resource
			Either<PropertyDefinition, ResponseFormat> eitherAttribute = getAttribute(resourceId, attributeId, userId);
			if (eitherAttribute.isRight()) {
				return Either.right(eitherAttribute.right().value());
			}
			Either<Map<String, DataTypeDefinition>, ResponseFormat> eitherAllDataTypes = getAllDataTypes(applicationDataTypeCache);
			if (eitherAllDataTypes.isRight()) {
				return Either.right(eitherAllDataTypes.right().value());
			}

			// validate attribute default values
			Either<Boolean, ResponseFormat> defaultValuesValidation = validatePropertyDefaultValue(newAttDef, eitherAllDataTypes.left().value());
			if (defaultValuesValidation.isRight()) {
				return Either.right(defaultValuesValidation.right().value());
			}
			// add the new property to resource on graph
			
			StorageOperationStatus validateAndUpdateAttribute = propertyOperation.validateAndUpdateProperty(newAttDef, eitherAllDataTypes.left().value());
			if (validateAndUpdateAttribute != StorageOperationStatus.OK) {
				log.debug("Problem while updating attribute with id {}. Reason - {}", attributeId, validateAndUpdateAttribute);
				result = Either.right(componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(validateAndUpdateAttribute), resource.getName()));
			}

			
			Either<PropertyDefinition, StorageOperationStatus> eitherAttUpdate = toscaOperationFacade.updateAttributeOfResource(resource, newAttDef);

			if (eitherAttUpdate.isRight()) {
				log.debug("Problem while updating attribute with id {}. Reason - {}", attributeId, eitherAttUpdate.right().value());
				result = Either.right(componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(eitherAttUpdate.right().value()), resource.getName()));
				return result;
			}

			result = Either.left(eitherAttUpdate.left().value());
			return result;
		} finally {
			commitOrRollback(result);
			graphLockOperation.unlockComponent(resourceId, NodeTypeEnum.Resource);
		}

	}

	/**
	 * Deletes Attribute on resource
	 * 
	 * @param resourceId
	 * @param attributeId
	 * @param userId
	 * @return
	 */
	public Either<PropertyDefinition, ResponseFormat> deleteAttribute(String resourceId, String attributeId, String userId) {

		Either<PropertyDefinition, ResponseFormat> result = null;

		Either<User, ResponseFormat> resp = validateUserExists(userId, "delete Attribute", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		StorageOperationStatus lockResult = graphLockOperation.lockComponent(resourceId, NodeTypeEnum.Resource);
		if (lockResult != StorageOperationStatus.OK) {
			BeEcompErrorManager.getInstance().logBeFailedLockObjectError(DELETE_ATTRIBUTE, NodeTypeEnum.Resource.name().toLowerCase(), resourceId);
			log.info("Failed to lock component {}. Error - {}", resourceId, lockResult);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
		}

		try {
			// Get the resource from DB
			Either<Resource, StorageOperationStatus> eitherResource = toscaOperationFacade.getToscaElement(resourceId);
			if (eitherResource.isRight()) {
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, resourceId));
			}
			Resource resource = eitherResource.left().value();

			// verify that resource is checked-out and the user is the last updater
			if (!ComponentValidationUtils.canWorkOnResource(resource, userId)) {
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
			}

			// verify attribute exist in resource
			Either<PropertyDefinition, ResponseFormat> eitherAttributeExist = getAttribute(resourceId, attributeId, userId);
			if (eitherAttributeExist.isRight()) {
				return Either.right(eitherAttributeExist.right().value());
			}
			String attributeName = eitherAttributeExist.left().value().getName();

			// delete attribute of resource from graph
			StorageOperationStatus eitherAttributeDelete = toscaOperationFacade.deleteAttributeOfResource(resource, attributeName);
			if (eitherAttributeDelete != StorageOperationStatus.OK) {
				result = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(eitherAttributeDelete), resource.getName()));
				return result;
			}

			result = Either.left(eitherAttributeExist.left().value());
			return result;
		} finally {
			commitOrRollback(result);
			graphLockOperation.unlockComponent(resourceId, NodeTypeEnum.Resource);
		}
	}
}
