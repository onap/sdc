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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.IResourceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InputsOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.openecomp.sdc.be.resources.data.InputsData;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("inputsBusinessLogic")
public class InputsBusinessLogic extends BaseBusinessLogic {

	private static final String CREATE_INPUT = "CreateInput";

	private static Logger log = LoggerFactory.getLogger(InputsBusinessLogic.class.getName());

	@Autowired
	private IResourceOperation resourceOperation = null;

	@javax.annotation.Resource
	private InputsOperation inputsOperation = null;

	@javax.annotation.Resource
	private PropertyOperation propertyOperation = null;

	@Autowired
	private ComponentsUtils componentsUtils;

	/**
	 * associate inputs to a given component with paging
	 * 
	 * @param componentId
	 * @param userId
	 * @param fromId
	 * @param amount
	 * @return
	 */
	public Either<List<InputDefinition>, ResponseFormat> getInputs(String userId, String componentId, String fromName,
			int amount) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "get Inputs", false);

		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		Either<List<InputDefinition>, StorageOperationStatus> inputsEitherRes = inputsOperation
				.getInputsOfComponent(componentId, fromName, amount);
		if (inputsEitherRes.isRight()) {
			if (inputsEitherRes.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
				return Either.left(new ArrayList<InputDefinition>());
			}
			ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(inputsEitherRes.right().value());
			log.debug("Failed to get inputs under component {}, error: {}", componentId, actionStatus.name());
			return Either.right(componentsUtils.getResponseFormat(actionStatus));

		}

		return Either.left(inputsEitherRes.left().value());

	}

	/**
	 * associate properties to a given component instance input
	 * 
	 * @param instanceId
	 * @param userId
	 * @param inputId
	 * @return
	 */

	public Either<List<ComponentInstanceProperty>, ResponseFormat> getComponentInstancePropertiesByInputId(
			String userId, String instanceId, String inputId) {
		Either<User, ResponseFormat> resp = validateUserExists(userId, "get Properties by input", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		Either<List<ComponentInstanceProperty>, StorageOperationStatus> propertiesEitherRes = inputsOperation
				.getComponentInstancePropertiesByInputId(instanceId, inputId);
		if (propertiesEitherRes.isRight()) {

			if (propertiesEitherRes.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
				return Either.left(new ArrayList<ComponentInstanceProperty>());
			}

			ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(propertiesEitherRes.right().value());
			log.debug("Failed to get inputs under component {}, error: {}", instanceId, actionStatus.name());
			return Either.right(componentsUtils.getResponseFormat(actionStatus));

		}

		return Either.left(propertiesEitherRes.left().value());

	}

	public Either<List<ComponentInstanceInput>, ResponseFormat> getInputsForComponentInput(String userId,
			String componentId, String inputId) {
		Either<User, ResponseFormat> resp = validateUserExists(userId, "get Inputs by input", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

		Either<List<ComponentInstanceInput>, StorageOperationStatus> inputsEitherRes = inputsOperation
				.getComponentInstanceInputsByInputId(componentId, inputId);
		if (inputsEitherRes.isRight()) {

			if (inputsEitherRes.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
				return Either.left(new ArrayList<ComponentInstanceInput>());
			}

			ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(inputsEitherRes.right().value());
			log.debug("Failed to get inputs for input under component {}, error: {}", componentId, actionStatus.name());
			return Either.right(componentsUtils.getResponseFormat(actionStatus));

		}

		return Either.left(inputsEitherRes.left().value());

	}

	public Either<List<InputDefinition>, ResponseFormat> createMultipleInputs(String userId, String componentId,
			ComponentTypeEnum componentType, ComponentInstInputsMap componentInstInputsMapUi, boolean shouldLockComp,
			boolean inTransaction) {

		Either<List<InputDefinition>, ResponseFormat> result = null;
		org.openecomp.sdc.be.model.Component component = null;
		try {
			Either<User, ResponseFormat> resp = validateUserExists(userId, "get Properties by input", false);

			if (resp.isRight()) {
				result = Either.right(resp.right().value());
				return result;
			}

			User user = resp.left().value();
			ComponentParametersView componentParametersView = new ComponentParametersView();
			componentParametersView.disableAll();
			componentParametersView.setIgnoreGroups(false);
			componentParametersView.setIgnoreArtifacts(false);
			componentParametersView.setIgnoreUsers(false);

			Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponent = validateComponentExists(
					componentId, componentType, componentParametersView, userId, null, user);

			if (validateComponent.isRight()) {
				result = Either.right(validateComponent.right().value());
				return result;
			}
			component = validateComponent.left().value();

			if (shouldLockComp) {
				Either<Boolean, ResponseFormat> lockComponent = lockComponent(component, CREATE_INPUT);
				if (lockComponent.isRight()) {
					result = Either.right(lockComponent.right().value());
					return result;
				}
			}

			Either<Boolean, ResponseFormat> canWork = validateCanWorkOnComponent(component, userId);
			if (canWork.isRight()) {
				result = Either.right(canWork.right().value());
				return result;
			}

			Either<Map<String, DataTypeDefinition>, ResponseFormat> allDataTypes = getAllDataTypes(
					applicationDataTypeCache);
			if (allDataTypes.isRight()) {
				result = Either.right(allDataTypes.right().value());
				return result;
			}

			Map<String, DataTypeDefinition> dataTypes = allDataTypes.left().value();

			Either<List<InputDefinition>, StorageOperationStatus> createInputsResult = this.inputsOperation
					.addInputsToComponent(componentId, componentType.getNodeType(), componentInstInputsMapUi,
							dataTypes);

			if (createInputsResult.isRight()) {
				ActionStatus actionStatus = componentsUtils
						.convertFromStorageResponse(createInputsResult.right().value());
				log.debug("Failed to create inputs under component {}, error: {}", componentId, actionStatus.name());
				result = Either.right(componentsUtils.getResponseFormat(actionStatus));
				return result;

			}
			result = Either.left(createInputsResult.left().value());

			return result;
		} finally {

			if (false == inTransaction) {

				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on create group.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on create group.");
					titanGenericDao.commit();
				}

			}
			// unlock resource
			if (shouldLockComp && component != null) {
				graphLockOperation.unlockComponent(componentId, componentType.getNodeType());
			}

		}

	}

	public Either<List<InputDefinition>, ResponseFormat> createInputs(String componentId, String userId,
			ComponentTypeEnum componentType, List<InputDefinition> inputsDefinitions, boolean shouldLockComp,
			boolean inTransaction) {

		Either<List<InputDefinition>, ResponseFormat> result = null;

		org.openecomp.sdc.be.model.Component component = null;
		try {

			if (inputsDefinitions != null && false == inputsDefinitions.isEmpty()) {

				if (shouldLockComp == true && inTransaction == true) {
					BeEcompErrorManager.getInstance().logInternalFlowError("createGroups",
							"Cannot lock component since we are inside a transaction", ErrorSeverity.ERROR);
					// Cannot lock component since we are in a middle of another
					// transaction.
					ActionStatus actionStatus = ActionStatus.INVALID_CONTENT;
					result = Either.right(componentsUtils.getResponseFormat(actionStatus));
					return result;
				}

				Either<User, ResponseFormat> validateUserExists = validateUserExists(userId, CREATE_INPUT, true);
				if (validateUserExists.isRight()) {
					result = Either.right(validateUserExists.right().value());
					return result;
				}

				User user = validateUserExists.left().value();

				Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponent = validateComponentExists(
						componentId, componentType, inTransaction, false);
				if (validateComponent.isRight()) {
					result = Either.right(validateComponent.right().value());
					return result;
				}
				component = validateComponent.left().value();

				if (shouldLockComp) {
					Either<Boolean, ResponseFormat> lockComponent = lockComponent(component, CREATE_INPUT);
					if (lockComponent.isRight()) {
						return Either.right(lockComponent.right().value());
					}
				}

				Either<Boolean, ResponseFormat> canWork = validateCanWorkOnComponent(component, userId);
				if (canWork.isRight()) {
					result = Either.right(canWork.right().value());
					return result;
				}

				result = createInputsInGraph(inputsDefinitions, component, user, inTransaction);
			}

			return result;

		} finally {

			if (false == inTransaction) {

				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on create group.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on create group.");
					titanGenericDao.commit();
				}

			}
			// unlock resource
			if (shouldLockComp && component != null) {
				graphLockOperation.unlockComponent(componentId, componentType.getNodeType());
			}

		}

	}

	public Either<List<InputDefinition>, ResponseFormat> createInputsInGraph(List<InputDefinition> inputsDefinitions,
			org.openecomp.sdc.be.model.Component component, User user, boolean inTransaction) {
		Either<List<InputDefinition>, ResponseFormat> result;
		List<InputDefinition> inputs = new ArrayList<InputDefinition>();
		for (InputDefinition inputDefinition : inputsDefinitions) {
			// String resourceId, String inputName, InputDefinition
			// newInputDefinition, String userId, boolean inTransaction
			Either<InputDefinition, ResponseFormat> createInput = createInput(component, user,
					component.getComponentType(), inputDefinition.getName(), inputDefinition, inTransaction);
			if (createInput.isRight()) {
				log.debug("Failed to create group {}.", createInput);
				result = Either.right(createInput.right().value());
				return result;
			}
			InputDefinition createdGroup = createInput.left().value();
			inputs.add(createdGroup);
		}
		result = Either.left(inputs);
		return result;
	}

	/**
	 * Delete input from service
	 * 
	 * @param component
	 * @param user
	 * @param componentType
	 * @param inputId
	 * @param inTransaction
	 * @return
	 */
	public Either<InputDefinition, ResponseFormat> deleteInput(String componentType, String componentId, String userId, String inputId, boolean inTransaction) {

		if (log.isDebugEnabled())
			log.debug("Going to delete input id: {}", inputId);

		// Validate user (exists)
		Either<User, ResponseFormat> userEither = validateUserExists(userId, "Delete input", true);
		if (userEither.isRight()) {
			return Either.right(userEither.right().value());
		}

		// Get component using componentType, componentId
		Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> componentEither = serviceOperation.getComponent(componentId, true);
		if (componentEither.isRight()) {
			return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(componentEither.right().value())));
		}
		org.openecomp.sdc.be.model.Component component = componentEither.left().value();

		// Validate inputId is child of the component
		// And get the inputDefinition for the response
		InputDefinition inputDefinition = null;
		Optional<InputDefinition> optionalInput = component.getInputs().stream().
		// filter by ID
				filter(input -> input.getUniqueId().equals(inputId)).
				// Get the input
				findAny();
		if (optionalInput.isPresent()) {
			inputDefinition = optionalInput.get();
		} else {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INPUT_IS_NOT_CHILD_OF_COMPONENT, inputId, componentId));
		}

		// Lock component
		Either<Boolean, ResponseFormat> lockResultEither = lockComponent(componentId, component, "deleteInput");
		if (lockResultEither.isRight()) {
			ResponseFormat responseFormat = lockResultEither.right().value();
			return Either.right(responseFormat);
		}

		// Delete input operations
		Either<String, StorageOperationStatus> deleteEither = Either.right(StorageOperationStatus.GENERAL_ERROR);
		try {
			deleteEither = inputsOperation.deleteInput(inputId);
			if (deleteEither.isRight()){
				log.debug("Component id: {} delete input id: {} failed", componentId, inputId);
				return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(deleteEither.right().value()), component.getName()));
			}
			return Either.left(inputDefinition);
		} finally {
			if (deleteEither.isRight()) {
				log.debug("Component id: {} delete input id: {} failed", componentId, inputId);
				titanGenericDao.rollback();
			} else {
				log.debug("Component id: {} delete input id: {} success", componentId, inputId);
				titanGenericDao.commit();
			}
			unlockComponent(deleteEither, component);
		}
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

	private Either<InputDefinition, ResponseFormat> createInput(org.openecomp.sdc.be.model.Component component,
			User user, ComponentTypeEnum componentType, String inputName, InputDefinition newInputDefinition,
			boolean inTransaction) {

		Either<InputDefinition, ResponseFormat> result = null;
		if (log.isDebugEnabled())
			log.debug("Going to create input {}", newInputDefinition);

		try {

			// verify property not exist in resource
			List<InputDefinition> resourceProperties = component.getInputs();

			if (resourceProperties != null) {
				if (inputsOperation.isInputExist(resourceProperties, component.getUniqueId(), inputName)) {
					return Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_ALREADY_EXIST, ""));
				}
			}

			Either<Map<String, DataTypeDefinition>, ResponseFormat> allDataTypes = getAllDataTypes(
					applicationDataTypeCache);
			if (allDataTypes.isRight()) {
				return Either.right(allDataTypes.right().value());
			}

			Map<String, DataTypeDefinition> dataTypes = allDataTypes.left().value();

			// validate input default values
			Either<Boolean, ResponseFormat> defaultValuesValidation = validatePropertyDefaultValue(newInputDefinition,
					dataTypes);
			if (defaultValuesValidation.isRight()) {
				return Either.right(defaultValuesValidation.right().value());
			}
			// convert property
			ToscaPropertyType type = getType(newInputDefinition.getType());
			PropertyValueConverter converter = type.getConverter();
			// get inner type
			String innerType = null;
			if (newInputDefinition != null) {
				SchemaDefinition schema = newInputDefinition.getSchema();
				if (schema != null) {
					PropertyDataDefinition prop = schema.getProperty();
					if (prop != null) {
						innerType = prop.getType();
					}
				}
				String convertedValue = null;
				if (newInputDefinition.getDefaultValue() != null) {
					convertedValue = converter.convert(newInputDefinition.getDefaultValue(), innerType,
							allDataTypes.left().value());
					newInputDefinition.setDefaultValue(convertedValue);
				}
			}

			// add the new property to resource on graph
			// need to get StorageOpaerationStatus and convert to ActionStatus
			// from componentsUtils
			Either<InputsData, StorageOperationStatus> either = inputsOperation.addInput(inputName, newInputDefinition,
					component.getUniqueId(), componentType.getNodeType());
			if (either.isRight()) {
				return Either.right(componentsUtils.getResponseFormat(
						componentsUtils.convertFromStorageResponse(either.right().value()), component.getName()));
			}
			// @TODO commit
			// inputsOperation.getTitanGenericDao().commit();
			InputDefinition createdInputyDefinition = inputsOperation
					.convertInputDataToInputDefinition(either.left().value());
			createdInputyDefinition.setName(inputName);
			createdInputyDefinition.setParentUniqueId(component.getUniqueId());

			return Either.left(createdInputyDefinition);

		} finally {

			if (false == inTransaction) {

				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on create group.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on create group.");
					titanGenericDao.commit();
				}

			}

		}

	}
}
