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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.simple.JSONObject;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.User;

import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.InputsOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.openecomp.sdc.be.resources.data.InputValueData;
import org.openecomp.sdc.be.resources.data.InputsData;
import org.openecomp.sdc.be.resources.data.PropertyValueData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;
import com.thinkaurelius.titan.core.TitanVertex;

import fj.Function;
import fj.data.Either;

@Component("inputsBusinessLogic")
public class InputsBusinessLogic extends BaseBusinessLogic {

	private static final String CREATE_INPUT = "CreateInput";
	private static final String UPDATE_INPUT = "UpdateInput";

	private static Logger log = LoggerFactory.getLogger(InputsBusinessLogic.class.getName());

	

	@javax.annotation.Resource
	private PropertyOperation propertyOperation = null;


	@Autowired
	private ComponentsUtils componentsUtils;
	
	private static final String GET_INPUT = "get_input";

	private static String ASSOCIATING_INPUT_TO_PROP = "AssociatingInputToComponentInstanceProperty";
	private Gson gson = new Gson(); 
	

	/**
	 * associate inputs to a given component with paging
	 * 
	 * @param componentId
	 * @param userId
	 * @param fromId
	 * @param amount
	 * @return
	 */
	public Either<List<InputDefinition>, ResponseFormat> getInputs(String userId, String componentId, String fromName, int amount) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "get Inputs", false);

		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}
		
	
		ComponentParametersView filters = new ComponentParametersView();
		filters.disableAll();	
		filters.setIgnoreInputs(false);

		Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getComponentEither = toscaOperationFacade.getToscaElement(componentId, filters);
		if(getComponentEither.isRight()){
			ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
			log.debug("Failed to found component {}, error: {}", componentId, actionStatus.name());
			return Either.right(componentsUtils.getResponseFormat(actionStatus));
			
		}
		org.openecomp.sdc.be.model.Component component = getComponentEither.left().value();
		List<InputDefinition> inputs = component.getInputs();
	
		return Either.left(inputs);

	}
	
	public Either<List<ComponentInstanceInput>, ResponseFormat> getComponentInstanceInputs(String userId, String componentId, String componentInstanceId,String fromName, int amount) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "get Inputs", false);

		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}
		
	
		ComponentParametersView filters = new ComponentParametersView();
		filters.disableAll();	
		filters.setIgnoreInputs(false);
		filters.setIgnoreComponentInstances(false);
		filters.setIgnoreComponentInstancesInputs(false);

		Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getComponentEither = toscaOperationFacade.getToscaElement(componentId, filters);
		if(getComponentEither.isRight()){
			ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
			log.debug("Failed to found component {}, error: {}", componentId, actionStatus.name());
			return Either.right(componentsUtils.getResponseFormat(actionStatus));
			
		}
		org.openecomp.sdc.be.model.Component component = getComponentEither.left().value();
		Map<String, List<ComponentInstanceInput>> ciInputs = component.getComponentInstancesInputs();
		if(!ciInputs.containsKey(componentInstanceId)){
			ActionStatus actionStatus = ActionStatus.COMPONENT_INSTANCE_NOT_FOUND;
			log.debug("Failed to found component instance inputs {}, error: {}", componentInstanceId, actionStatus.name());
			return Either.right(componentsUtils.getResponseFormat(actionStatus));
		}
	
		return Either.left(ciInputs.get(componentInstanceId));

	}

	/**
	 * associate properties to a given component instance input
	 * 
	 * @param instanceId
	 * @param userId
	 * @param inputId
	 * @return
	 */

	public Either<List<ComponentInstanceProperty>, ResponseFormat> getComponentInstancePropertiesByInputId(String userId, String componentId, String instanceId, String inputId) {
		Either<User, ResponseFormat> resp = validateUserExists(userId, "get Properties by input", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}
		String parentId = componentId;
		org.openecomp.sdc.be.model.Component component = null;
		ComponentParametersView filters = new ComponentParametersView();
		filters.disableAll();
		filters.setIgnoreComponentInstances(false);
		
		if(!instanceId.equals(inputId)){
			
			
			Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getComponentEither = toscaOperationFacade.getToscaElement(parentId, filters);
			
			if(getComponentEither.isRight()){
				ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
				log.debug("Failed to found component {}, error: {}", parentId, actionStatus.name());
				return Either.right(componentsUtils.getResponseFormat(actionStatus));
				
			}
			component = getComponentEither.left().value();
			Optional<ComponentInstance> ciOp = component.getComponentInstances().stream().filter(ci ->ci.getUniqueId().equals(instanceId)).findAny();
			if(ciOp.isPresent()){
				parentId = ciOp.get().getComponentUid();
			}
		
		}			
	
		filters.setIgnoreInputs(false);	
		
		filters.setIgnoreComponentInstancesProperties(false);
		filters.setIgnoreComponentInstancesInputs(false);
		filters.setIgnoreProperties(false);
		
		Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getComponentEither = toscaOperationFacade.getToscaElement(parentId, filters);
		
		if(getComponentEither.isRight()){
			ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
			log.debug("Failed to found component {}, error: {}", parentId, actionStatus.name());
			return Either.right(componentsUtils.getResponseFormat(actionStatus));
			
		}
		component = getComponentEither.left().value();
		
		Optional<InputDefinition> op = component.getInputs().stream().filter(in -> in.getUniqueId().equals(inputId)).findFirst();
		if(!op.isPresent()){
			ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
			log.debug("Failed to found input {} under component {}, error: {}", inputId, parentId, actionStatus.name());
			return Either.right(componentsUtils.getResponseFormat(actionStatus));
		}
			
		return Either.left(getComponentInstancePropertiesByInputId(component, inputId));

	}
	
	public Either<InputDefinition, ResponseFormat> updateInputValue(ComponentTypeEnum componentType, String componentId, InputDefinition input, String userId, boolean shouldLockComp, boolean inTransaction) {
		
		Either<InputDefinition, ResponseFormat> result = null;
		org.openecomp.sdc.be.model.Component component = null;
		

		try {
			Either<User, ResponseFormat> resp = validateUserExists(userId, "get input", false);

			if (resp.isRight()) {
				result = Either.right(resp.right().value());
				return result;
			}

			ComponentParametersView componentParametersView = new ComponentParametersView();
			componentParametersView.disableAll();
			componentParametersView.setIgnoreInputs(false);		
			componentParametersView.setIgnoreUsers(false);

			Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponent = validateComponentExists(componentId, componentType, componentParametersView);

			if (validateComponent.isRight()) {
				result = Either.right(validateComponent.right().value());
				return result;
			}
			component = validateComponent.left().value();

			if (shouldLockComp) {
				Either<Boolean, ResponseFormat> lockComponent = lockComponent(component, UPDATE_INPUT);
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

			Either<Map<String, DataTypeDefinition>, ResponseFormat> allDataTypes = getAllDataTypes(applicationDataTypeCache);
			if (allDataTypes.isRight()) {
				result = Either.right(allDataTypes.right().value());
				return result;
			}

			Map<String, DataTypeDefinition> dataTypes = allDataTypes.left().value();
			
			Optional<InputDefinition> op = component.getInputs().stream().filter(in -> in.getUniqueId().equals(input.getUniqueId())).findFirst();
			if(!op.isPresent()){
				ActionStatus actionStatus = ActionStatus.COMPONENT_NOT_FOUND;
				log.debug("Failed to found input {} under component {}, error: {}", input.getUniqueId(), componentId, actionStatus.name());
				result = Either.right(componentsUtils.getResponseFormat(actionStatus));
				return result;
			}
			InputDefinition currentInput = op.get();
			
			String innerType = null;
			String propertyType = currentInput.getType();
			ToscaPropertyType type = ToscaPropertyType.isValidType(propertyType);
			log.debug("The type of the property {} is {}", currentInput.getUniqueId(), propertyType);

			if (type == ToscaPropertyType.LIST || type == ToscaPropertyType.MAP) {
				SchemaDefinition def = currentInput.getSchema();
				if (def == null) {
					log.debug("Schema doesn't exists for property of type {}", type);
					return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(StorageOperationStatus.INVALID_VALUE)));
				}
				PropertyDataDefinition propDef = def.getProperty();
				if (propDef == null) {
					log.debug("Property in Schema Definition inside property of type {} doesn't exist", type);
					return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(StorageOperationStatus.INVALID_VALUE)));
				}
				innerType = propDef.getType();
			}
			// Specific Update Logic
		
			Either<Object, Boolean> isValid = propertyOperation.validateAndUpdatePropertyValue(propertyType, input.getDefaultValue(), true, innerType, allDataTypes.left().value());

			String newValue = currentInput.getDefaultValue();
			if (isValid.isRight()) {
				Boolean res = isValid.right().value();
				if (res == false) {
					return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.ILLEGAL_ARGUMENT))));
				}
			} else {
				Object object = isValid.left().value();
				if (object != null) {
					newValue = object.toString();
				}
			}
			
			currentInput.setDefaultValue(newValue);
			
			Either<InputDefinition, StorageOperationStatus> status = toscaOperationFacade.updateInputOfComponent(component, currentInput);
		
			if(status.isRight()){
				ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(status.right().value());
				result = Either.right(componentsUtils.getResponseFormat(actionStatus, ""));
				return result;
			}
			
			
			result = Either.left(status.left().value());
			
			return result;
			
			
		}finally {

				if (false == inTransaction) {

					if (result == null || result.isRight()) {
						log.debug("Going to execute rollback on create group.");
						titanDao.rollback();
					} else {
						log.debug("Going to execute commit on create group.");
						titanDao.commit();
					}

				}
				// unlock resource
				if (shouldLockComp && component != null) {
					graphLockOperation.unlockComponent(componentId, componentType.getNodeType());
				}

			}


	}

	public Either<List<ComponentInstanceInput>, ResponseFormat> getInputsForComponentInput(String userId, String componentId, String inputId) {
		Either<User, ResponseFormat> resp = validateUserExists(userId, "get Properties by input", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}
		String parentId = componentId;
		org.openecomp.sdc.be.model.Component component = null;
		ComponentParametersView filters = new ComponentParametersView();
		filters.disableAll();
		filters.setIgnoreComponentInstances(false);		
		filters.setIgnoreInputs(false);			
		filters.setIgnoreComponentInstancesInputs(false);
		filters.setIgnoreProperties(false);
		
		Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getComponentEither = toscaOperationFacade.getToscaElement(parentId, filters);
		
		if(getComponentEither.isRight()){
			ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
			log.debug("Failed to found component {}, error: {}", parentId, actionStatus.name());
			return Either.right(componentsUtils.getResponseFormat(actionStatus));
			
		}
		component = getComponentEither.left().value();
		
		Optional<InputDefinition> op = component.getInputs().stream().filter(in -> in.getUniqueId().equals(inputId)).findFirst();
		if(!op.isPresent()){
			ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
			log.debug("Failed to found input {} under component {}, error: {}", inputId, parentId, actionStatus.name());
			return Either.right(componentsUtils.getResponseFormat(actionStatus));
		}
			
		return Either.left(getComponentInstanceInputsByInputId(component, inputId));

	}

	public Either<List<InputDefinition>, ResponseFormat> createMultipleInputs(String userId, String componentId, ComponentTypeEnum componentType, ComponentInstInputsMap componentInstInputsMapUi, boolean shouldLockComp, boolean inTransaction) {

		Either<List<InputDefinition>, ResponseFormat> result = null;
		org.openecomp.sdc.be.model.Component component = null;
		
		Map<String, List<ComponentInstanceInput>> inputsValueToCreateMap = new HashMap<>();
		Map<String, List<ComponentInstanceProperty>> propertiesToCreateMap = new HashMap<>();		
		Map<String, InputDefinition> inputsToCreate = new HashMap<>();
		
		try {
			Either<User, ResponseFormat> resp = validateUserExists(userId, "get Properties by input", false);

			if (resp.isRight()) {
				result = Either.right(resp.right().value());
				return result;
			}

			ComponentParametersView componentParametersView = new ComponentParametersView();
			componentParametersView.disableAll();
			componentParametersView.setIgnoreInputs(false);
			componentParametersView.setIgnoreComponentInstancesInputs(false);
			componentParametersView.setIgnoreComponentInstances(false);
			componentParametersView.setIgnoreComponentInstancesProperties(false);
			componentParametersView.setIgnoreUsers(false);

			Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponent = validateComponentExists(componentId, componentType, componentParametersView);

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

			Either<Map<String, DataTypeDefinition>, ResponseFormat> allDataTypes = getAllDataTypes(applicationDataTypeCache);
			if (allDataTypes.isRight()) {
				result = Either.right(allDataTypes.right().value());
				return result;
			}

			Map<String, DataTypeDefinition> dataTypes = allDataTypes.left().value();
			Map<String, org.openecomp.sdc.be.model.Component> origComponentMap = new HashMap<>();


			//////////////////////////////////////////////////////////////////////////////////////////////////////
			
			List<InputDefinition> resList = new ArrayList<InputDefinition>();
			Map<String, List<InputDefinition>> newInputsMap = componentInstInputsMapUi.getComponentInstanceInputsMap();
			List<ComponentInstance> ciList = component.getComponentInstances();
			if (newInputsMap != null && !newInputsMap.isEmpty()) {
				int index = 0;
				for (Entry<String, List<InputDefinition>> entry : newInputsMap.entrySet()) {
					List<ComponentInstanceInput> inputsValueToCreate = new ArrayList<>();
					String compInstId = entry.getKey();
			
					Optional<ComponentInstance> op = ciList.stream().filter(ci -> ci.getUniqueId().equals(compInstId)).findAny();
					if(!op.isPresent()){
						ActionStatus actionStatus = ActionStatus.INVALID_CONTENT;
						log.debug("Failed to find component instance {} under component {}", compInstId, componentId);
						result = Either.right(componentsUtils.getResponseFormat(actionStatus));
						return result;
					}
					ComponentInstance ci = op.get();
					String compInstname = ci.getNormalizedName();
					Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> origComponentEither = getOriginComponent(ci, origComponentMap);
					if(origComponentEither.isRight()){
						ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(origComponentEither.right().value());
						log.debug("Failed to create inputs value under component {}, error: {}", componentId, actionStatus.name());
						result = Either.right(componentsUtils.getResponseFormat(actionStatus));
						return result;
					}
					org.openecomp.sdc.be.model.Component origComponent = origComponentEither.left().value();
					
					List<InputDefinition> inputs = entry.getValue();

					if (inputs != null && !inputs.isEmpty()) {
						
						for (InputDefinition input : inputs) {					

							StorageOperationStatus status = addInputsToComponent(componentId, inputsToCreate, allDataTypes.left().value(), resList, index, inputsValueToCreate, compInstId, compInstname, origComponent, input);
							if(status != StorageOperationStatus.OK ){
								ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(status);
								log.debug("Failed to create inputs value under component {}, error: {}", componentId, actionStatus.name());
								result = Either.right(componentsUtils.getResponseFormat(actionStatus));
								return result;
							}

						}
					}
					if(!inputsValueToCreate.isEmpty()){
						inputsValueToCreateMap.put(compInstId, inputsValueToCreate);
					}
				}
			
			}
			
			Map<String, List<ComponentInstancePropInput>> newInputsPropsMap = componentInstInputsMapUi.getComponentInstanceProperties();
			if (newInputsPropsMap != null && !newInputsPropsMap.isEmpty()) {
				
				result = createInputsFromProperty(component, origComponentMap, inputsToCreate, propertiesToCreateMap,  dataTypes, resList, newInputsPropsMap);
				
				if (result.isRight()) {
					log.debug("Failed to create inputs of resource  for id {} error {}", component.getUniqueId(), result.right().value());
					return result;
				}
				resList = result.left().value();
				
			}
			
			
			Either<List<InputDefinition>, StorageOperationStatus> assotiateInputsEither = toscaOperationFacade.addInputsToComponent(inputsToCreate, component.getUniqueId());
			if(assotiateInputsEither.isRight()){
				log.debug("Failed to create inputs under component {}. Status is {}", component.getUniqueId(), assotiateInputsEither.right().value());
				result = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(assotiateInputsEither.right().value())));
				return result;
			}
			
			Either<Map<String, List<ComponentInstanceProperty>>, StorageOperationStatus> assotiatePropsEither = toscaOperationFacade.addComponentInstancePropertiesToComponent(component, propertiesToCreateMap, component.getUniqueId());
			if(assotiatePropsEither.isRight()){
				log.debug("Failed to add inputs values under component {}. Status is {}", component.getUniqueId(), assotiateInputsEither.right().value());
				result = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(assotiateInputsEither.right().value())));
				return result;
			}
			
			Either<Map<String, List<ComponentInstanceInput>>, StorageOperationStatus> addciInputsEither = toscaOperationFacade.addComponentInstanceInputsToComponent(inputsValueToCreateMap, component.getUniqueId());
			if(addciInputsEither.isRight()){
				log.debug("Failed to add inputs values under component {}. Status is {}", component.getUniqueId(), assotiateInputsEither.right().value());
				result = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(assotiateInputsEither.right().value())));
				return result;
			}
			
	
			
	
			result =  Either.left(resList);
			return result;
			///////////////////////////////////////////////////////////////////////////////////////////	
		
		} finally {

			if (false == inTransaction) {

				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on create group.");
					titanDao.rollback();
				} else {
					log.debug("Going to execute commit on create group.");
					titanDao.commit();
				}

			}
			// unlock resource
			if (shouldLockComp && component != null) {
				graphLockOperation.unlockComponent(componentId, componentType.getNodeType());
			}

		}

	}

	private StorageOperationStatus addInputsToComponent(String componentId, Map<String, InputDefinition> inputsToCreate, Map<String, DataTypeDefinition> allDataTypes, List<InputDefinition> resList, int index,
			List<ComponentInstanceInput> inputsValueToCreate, String compInstId, String compInstname, org.openecomp.sdc.be.model.Component origComponent, InputDefinition input) {
		
		Either<List<InputDefinition>, ResponseFormat> result;
		String innerType = null;
		InputDefinition oldInput = origComponent.getInputs().stream().filter(ciIn -> ciIn.getUniqueId().equals(input.getUniqueId())).findAny().get();
		String serviceInputName = compInstname + "_" + input.getName();
		input.setName(serviceInputName);
		
		JSONObject jobject = new JSONObject();
		jobject.put(GET_INPUT, input.getName());
		
		ComponentInstanceInput inputValue = new ComponentInstanceInput(oldInput, jobject.toJSONString(), null);
		
		Either<String, StorageOperationStatus> validatevalueEiter = validateInputValueBeforeCreate(inputValue, jobject.toJSONString(), false, innerType, allDataTypes);
		if (validatevalueEiter.isRight()) {								
	
			return validatevalueEiter.right().value();
		}														
		
		String uniqueId = UniqueIdBuilder.buildResourceInstanceInputValueUid(compInstId, index++);							
		inputValue.setUniqueId(uniqueId);
		inputValue.setValue(validatevalueEiter.left().value());
		
		
		input.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(componentId, input.getName()));
		input.setSchema(oldInput.getSchema());
		input.setDefaultValue(oldInput.getDefaultValue());
		input.setConstraints(oldInput.getConstraints());
		input.setDescription(oldInput.getDescription());
		input.setHidden(oldInput.isHidden());
		input.setImmutable(oldInput.isImmutable());
		input.setDefinition(oldInput.isDefinition());
		input.setRequired(oldInput.isRequired());
		inputsToCreate.put(input.getName(), input);
		
		
		
		List<GetInputValueDataDefinition> getInputValues = new ArrayList<>();
		GetInputValueDataDefinition getInputValueDataDefinition = new GetInputValueDataDefinition();
		getInputValueDataDefinition.setInputId(input.getUniqueId());
		getInputValueDataDefinition.setInputName(input.getName());
		getInputValues.add(getInputValueDataDefinition);
		inputValue.setGetInputValues(getInputValues);							
		
		inputsValueToCreate.add(inputValue);
		input.setInputs(inputsValueToCreate);
		
		resList.add(input);
		return StorageOperationStatus.OK;
	}

	public Either<List<InputDefinition>, ResponseFormat> createInputs(String componentId, String userId, ComponentTypeEnum componentType, List<InputDefinition> inputsDefinitions, boolean shouldLockComp, boolean inTransaction) {

		Either<List<InputDefinition>, ResponseFormat> result = null;

		org.openecomp.sdc.be.model.Component component = null;
		try {

			if (inputsDefinitions != null && false == inputsDefinitions.isEmpty()) {

				if (shouldLockComp == true && inTransaction == true) {
					BeEcompErrorManager.getInstance().logInternalFlowError("createGroups", "Cannot lock component since we are inside a transaction", ErrorSeverity.ERROR);
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

				Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponent = validateComponentExists(componentId, componentType, null);
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
				Map<String, InputDefinition> inputs = inputsDefinitions.stream().collect(Collectors.toMap( o -> o.getName(), o -> o));

				result = createInputsInGraph(inputs, component, user, inTransaction);
			}

			return result;

		} finally {

			if (false == inTransaction) {

				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on create group.");
					titanDao.rollback();
				} else {
					log.debug("Going to execute commit on create group.");
					titanDao.commit();
				}

			}
			// unlock resource
			if (shouldLockComp && component != null) {
				graphLockOperation.unlockComponent(componentId, componentType.getNodeType());
			}

		}

	}

	public Either<List<InputDefinition>, ResponseFormat> createInputsInGraph(Map<String, InputDefinition> inputs, org.openecomp.sdc.be.model.Component component, User user, boolean inTransaction) {
		
		List<InputDefinition> resList = inputs.values().stream().collect(Collectors.toList());	
		Either<List<InputDefinition>, ResponseFormat> result = Either.left(resList);
		List<InputDefinition> resourceProperties = component.getInputs();
	
		if(inputs != null && !inputs.isEmpty()){
			Either<Map<String, DataTypeDefinition>, ResponseFormat> allDataTypes = getAllDataTypes(applicationDataTypeCache);
			if (allDataTypes.isRight()) {
				return Either.right(allDataTypes.right().value());
			}

			Map<String, DataTypeDefinition> dataTypes = allDataTypes.left().value();
			
			for (Map.Entry<String, InputDefinition> inputDefinition : inputs.entrySet()) {
				String inputName = inputDefinition.getKey();
				inputDefinition.getValue().setName(inputName);	
	
				Either<InputDefinition, ResponseFormat> preparedInputEither = prepareAndValidateInputBeforeCreate(inputDefinition.getValue(), dataTypes);
				if(preparedInputEither.isRight()){
					return Either.right(preparedInputEither.right().value());
				}
				
			}
			if (resourceProperties != null) {
				Map<String, InputDefinition> generatedInputs = resourceProperties.stream().collect(Collectors.toMap(i -> i.getName(), i -> i));
				Either<Map<String, InputDefinition>, String> mergeEither = PropertyDataDefinition.mergeProperties(generatedInputs, inputs, false);
				if(mergeEither.isRight()){
					return Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_ALREADY_EXIST, mergeEither.right().value()));
				}
				inputs = mergeEither.left().value();
			}
			
			Either<List<InputDefinition>, StorageOperationStatus> assotiateInputsEither = toscaOperationFacade.createAndAssociateInputs(inputs, component.getUniqueId());
			if(assotiateInputsEither.isRight()){
				log.debug("Failed to create inputs under component {}. Status is {}", component.getUniqueId(), assotiateInputsEither.right().value());
				return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(assotiateInputsEither.right().value())));
			}
			result  = Either.left(assotiateInputsEither.left().value());
			
		}
		
		
		return result;
	}

	/**
	 * Delete input from service
	 * 
	 * @param componentType
	 * @param inputId
	 * @param component
	 * @param user
	 * 
	 * @return
	 */
	public Either<InputDefinition, ResponseFormat> deleteInput(String componentType, String componentId, String userId, String inputId) {

		Either<InputDefinition, ResponseFormat> deleteEither = null;
		if (log.isDebugEnabled())
			log.debug("Going to delete input id: {}", inputId);

		// Validate user (exists)
		Either<User, ResponseFormat> userEither = validateUserExists(userId, "Delete input", true);
		if (userEither.isRight()) {
			deleteEither =  Either.right(userEither.right().value());
			return deleteEither;
		}

		// Get component using componentType, componentId

		ComponentParametersView componentParametersView = new ComponentParametersView();
		componentParametersView.disableAll();
		componentParametersView.setIgnoreInputs(false);
		componentParametersView.setIgnoreComponentInstances(false);
		componentParametersView.setIgnoreComponentInstancesInputs(false);
		componentParametersView.setIgnoreComponentInstancesProperties(false);
		componentParametersView.setIgnoreUsers(false);
		
		Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> componentEither = toscaOperationFacade.getToscaElement(componentId, componentParametersView);
		if (componentEither.isRight()) {
			deleteEither =  Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(componentEither.right().value())));
			return deleteEither;
		}
		org.openecomp.sdc.be.model.Component component = componentEither.left().value();

		// Validate inputId is child of the component
		Optional<InputDefinition> optionalInput = component.getInputs().stream().
		// filter by ID
				filter(input -> input.getUniqueId().equals(inputId)).
				// Get the input
				findAny();
		if (!optionalInput.isPresent()) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INPUT_IS_NOT_CHILD_OF_COMPONENT, inputId, componentId));
		}

		// Lock component
		Either<Boolean, ResponseFormat> lockResultEither = lockComponent(componentId, component, "deleteInput");
		if (lockResultEither.isRight()) {
			ResponseFormat responseFormat = lockResultEither.right().value();
			deleteEither =  Either.right(responseFormat);
			return deleteEither;
		}

		// Delete input operations
	
		InputDefinition inputForDelete = optionalInput.get();
		try {
			StorageOperationStatus status = toscaOperationFacade.deleteInputOfResource(component, inputForDelete.getName());
			if(status != StorageOperationStatus.OK){
				log.debug("Component id: {} delete input id: {} failed", componentId, inputId);
				deleteEither =  Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status), component.getName()));
				return deleteEither;
			}
			
			List<ComponentInstanceInput> inputsValue= getComponentInstanceInputsByInputId(component, inputId);
			Map<String, List<ComponentInstanceInput>> insInputsMatToDelete = new HashMap<>();
			
			if(inputsValue != null && !inputsValue.isEmpty()){	
				for(ComponentInstanceInput inputValue: inputsValue){
					List<ComponentInstanceInput> inputList = null;
					String ciId = inputValue.getComponentInstanceId();
					if(!insInputsMatToDelete.containsKey(ciId)){
						inputList = new ArrayList<>();
					}else{
						inputList = insInputsMatToDelete.get(ciId);
					}
					inputList.add(inputValue);
					insInputsMatToDelete.put(ciId, inputList);
				}
				status = toscaOperationFacade.deleteComponentInstanceInputsToComponent(insInputsMatToDelete, component.getUniqueId());
				if(status != StorageOperationStatus.OK){
					log.debug("Component id: {} delete component instance input id: {} failed", componentId, inputId);
					deleteEither = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status), component.getName()));
					return deleteEither;
				}
			}
		
			// US848813 delete service input that relates to VL / CP property
			
			List<ComponentInstanceProperty> propertiesValue = getComponentInstancePropertiesByInputId(component, inputId);
				if(propertiesValue != null && !propertiesValue.isEmpty()){
					//propertyList = propertyValueStatus.left().value();	
					for(ComponentInstanceProperty propertyValue: propertiesValue){
				
						String value = propertyValue.getValue();
						Map<String, Object> mappedToscaTemplate = (Map<String, Object>) new Yaml().load(value);
						
						resetInputName(mappedToscaTemplate, inputForDelete.getName());
						
						value = gson.toJson(mappedToscaTemplate);
						propertyValue.setValue(value);
						String compInstId = propertyValue.getComponentInstanceId();
						propertyValue.setRules(null);
						List<GetInputValueDataDefinition> getInputsValues = propertyValue.getGetInputValues();
						if(getInputsValues != null && !getInputsValues.isEmpty()){
							Optional<GetInputValueDataDefinition> op = getInputsValues.stream().filter(gi -> gi.getInputId().equals(inputForDelete.getUniqueId())).findAny();
							if(op.isPresent()){
								getInputsValues.remove(op.get());
							}
						}
						propertyValue.setGetInputValues(getInputsValues);
						if(status != StorageOperationStatus.OK){
							log.debug("Component id: {} delete component instance property {} id: {} failed", componentId, propertyValue.getUniqueId(), inputId);
							deleteEither = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status), component.getName()));
							return deleteEither;
						}
						Either<String, TitanOperationStatus> findDefaultValue = propertyOperation.findDefaultValueFromSecondPosition(propertyValue.getPath(), propertyValue.getUniqueId(), propertyValue.getDefaultValue());
						if (findDefaultValue.isRight()) {
							deleteEither = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(DaoStatusConverter.convertTitanStatusToStorageStatus(findDefaultValue.right().value()))));
							return deleteEither;
							
						}
						String defaultValue = findDefaultValue.left().value();
						propertyValue.setDefaultValue(defaultValue);
						log.debug("The returned default value in ResourceInstanceProperty is {}", defaultValue);
						status = toscaOperationFacade.updateComponentInstanceProperty(component, compInstId, propertyValue);
						if(status != StorageOperationStatus.OK){
							log.debug("Component id: {} update component instance property {} id: {} failed", componentId, propertyValue.getUniqueId(), inputId);
							deleteEither = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status), component.getName()));
							return deleteEither;
						}
						
					}
				}
			

				deleteEither = Either.left(inputForDelete);
			return deleteEither;
		} finally {
			if (deleteEither == null || deleteEither.isRight()) {
				log.debug("Component id: {} delete input id: {} failed", componentId, inputId);
				titanDao.rollback();
			} else {
				log.debug("Component id: {} delete input id: {} success", componentId, inputId);
				titanDao.commit();
			}
			unlockComponent(deleteEither, component);
		}
	}

	private Either<InputDefinition, ResponseFormat> prepareAndValidateInputBeforeCreate(InputDefinition newInputDefinition, Map<String, DataTypeDefinition> dataTypes) {
		

		// validate input default values
		Either<Boolean, ResponseFormat> defaultValuesValidation = validatePropertyDefaultValue(newInputDefinition, dataTypes);
		if (defaultValuesValidation.isRight()) {
			return Either.right(defaultValuesValidation.right().value());
		}
		// convert property
		ToscaPropertyType type = getType(newInputDefinition.getType());
		if (type != null) {
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
					convertedValue = converter.convert(newInputDefinition.getDefaultValue(), innerType, dataTypes);
					newInputDefinition.setDefaultValue(convertedValue);
				}
			}
		}
		return Either.left(newInputDefinition);
	}
	
	public boolean isInputExist(List<InputDefinition> inputs, String resourceUid, String inputName) {

		if (inputs == null) {
			return false;
		}

		for (InputDefinition propertyDefinition : inputs) {
			String parentUniqueId = propertyDefinition.getParentUniqueId();
			String name = propertyDefinition.getName();

			if (parentUniqueId.equals(resourceUid) && name.equals(inputName)) {
				return true;
			}
		}

		return false;

	}
	
	
	public Either<InputDefinition, ResponseFormat> getInputsAndPropertiesForComponentInput(String userId, String componentId, String inputId, boolean inTransaction) {
		Either<InputDefinition, ResponseFormat> result = null;
		try {
			
			Either<User, ResponseFormat> resp = validateUserExists(userId, "get Properties by input", false);
			if (resp.isRight()) {
				return Either.right(resp.right().value());
			}
			Either<List<ComponentInstanceProperty>, StorageOperationStatus> propertiesEitherRes = null;
			
			ComponentParametersView filters = new ComponentParametersView();
			filters.disableAll();
			filters.setIgnoreComponentInstances(false);
			filters.setIgnoreInputs(false);
			filters.setIgnoreComponentInstancesInputs(false);
			filters.setIgnoreComponentInstancesProperties(false);
			filters.setIgnoreProperties(false);
			Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getComponentEither = toscaOperationFacade.getToscaElement(componentId, filters);
			if(getComponentEither.isRight()){
				ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
				log.debug("Failed to found component {}, error: {}", componentId, actionStatus.name());
				return Either.right(componentsUtils.getResponseFormat(actionStatus));
				
			}
			org.openecomp.sdc.be.model.Component component = getComponentEither.left().value();
			Optional<InputDefinition> op = component.getInputs().stream().filter(in -> in.getUniqueId().equals(inputId)).findFirst();
			if(!op.isPresent()){
				ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
				log.debug("Failed to found input {} under component {}, error: {}", inputId, componentId, actionStatus.name());
				return Either.right(componentsUtils.getResponseFormat(actionStatus));
			}
			
			InputDefinition resObj = op.get();
			
			List<ComponentInstanceInput> inputCIInput = getComponentInstanceInputsByInputId(component, inputId) ;
			
			resObj.setInputs(inputCIInput);
			
				
			List<ComponentInstanceProperty> inputProps = getComponentInstancePropertiesByInputId(component, inputId) ;
			
			resObj.setProperties(inputProps);			
	

			result = Either.left(resObj);

			return result;

		} finally {

			if (false == inTransaction) {

				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on create group.");
					titanDao.rollback();
				} else {
					log.debug("Going to execute commit on create group.");
					titanDao.commit();
				}

			}

		}

	}
	
	private List<ComponentInstanceProperty> getComponentInstancePropertiesByInputId(org.openecomp.sdc.be.model.Component component, String inputId){
		List<ComponentInstanceProperty> resList = new ArrayList<>();
		Map<String, List<ComponentInstanceProperty>> ciPropertiesMap = component.getComponentInstancesProperties();
		if(ciPropertiesMap != null && !ciPropertiesMap.isEmpty()){
			ciPropertiesMap.forEach(new BiConsumer<String, List<ComponentInstanceProperty>>() {
				@Override
				public void accept(String s, List<ComponentInstanceProperty> ciPropList) {
					String ciName = "";
					Optional<ComponentInstance> ciOp = component.getComponentInstances().stream().filter(ci ->ci.getUniqueId().equals(s)).findAny();
					if(ciOp.isPresent())
						ciName = ciOp.get().getName();
					if (ciPropList != null && !ciPropList.isEmpty()) {
						for(ComponentInstanceProperty prop: ciPropList){
							List<GetInputValueDataDefinition> inputsValues = prop.getGetInputValues();
							if(inputsValues != null && !inputsValues.isEmpty()){
								for(GetInputValueDataDefinition inputData: inputsValues){
									if(inputData.getInputId().equals(inputId) || (inputData.getGetInputIndex() != null && inputData.getGetInputIndex().getInputId().equals(inputId))){
										prop.setComponentInstanceId(s);
										prop.setComponentInstanceName(ciName);
										resList.add(prop);
										break;
									}
								}
							}
							
						}
					}
				}
			});
		}
		return resList;

	}
	
	private List<ComponentInstanceInput> getComponentInstanceInputsByInputId(org.openecomp.sdc.be.model.Component component, String inputId){
		List<ComponentInstanceInput> resList = new ArrayList<>();
		Map<String, List<ComponentInstanceInput>> ciInputsMap = component.getComponentInstancesInputs();
		if(ciInputsMap != null && !ciInputsMap.isEmpty()){
			ciInputsMap.forEach(new BiConsumer<String, List<ComponentInstanceInput>>() {
				@Override
				public void accept(String s, List<ComponentInstanceInput> ciPropList) {
					String ciName = "";
					Optional<ComponentInstance> ciOp = component.getComponentInstances().stream().filter(ci ->ci.getUniqueId().equals(s)).findAny();
					if(ciOp.isPresent())
						ciName = ciOp.get().getName();
					if (ciPropList != null && !ciPropList.isEmpty()) {
						for(ComponentInstanceInput prop: ciPropList){
							List<GetInputValueDataDefinition> inputsValues = prop.getGetInputValues();
							if(inputsValues != null && !inputsValues.isEmpty()){
								for(GetInputValueDataDefinition inputData: inputsValues){
									if(inputData.getInputId().equals(inputId) || (inputData.getGetInputIndex() != null && inputData.getGetInputIndex().getInputId().equals(inputId))){
										prop.setComponentInstanceId(s);
										prop.setComponentInstanceName(ciName);
										resList.add(prop);
										break;
									}
								}
							}
							
						}
					}
				}
			});
		}
		return resList;

	}
	
	private Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getOriginComponent(ComponentInstance ci, Map<String, org.openecomp.sdc.be.model.Component> origComponentMap){
		Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> result = null;
		String compInstname = ci.getNormalizedName();
		
		ComponentParametersView componentParametersView = new ComponentParametersView();
		componentParametersView.disableAll();
		componentParametersView.setIgnoreInputs(false);
		org.openecomp.sdc.be.model.Component origComponent = null;
		if(!origComponentMap.containsKey(ci.getComponentUid())){
			Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> componentFound  = toscaOperationFacade.getToscaElement(ci.getComponentUid(), componentParametersView);

			if (componentFound.isRight()) {
				result = Either.right(componentFound.right().value());
				return result;
			}
			origComponent =  componentFound.left().value();
			origComponentMap.put(origComponent.getUniqueId(), origComponent);
		}else{
			origComponent = origComponentMap.get(ci.getComponentUid());
		}
		result = Either.left(origComponent);
		return result;
	}
	
	
	
	private Either<List<InputDefinition>, ResponseFormat> createInputsFromProperty(org.openecomp.sdc.be.model.Component component, Map<String, org.openecomp.sdc.be.model.Component> origComponentMap,  Map<String, InputDefinition> inputsToCreate, Map<String, List<ComponentInstanceProperty>> propertiesToCreateMap, Map<String, DataTypeDefinition> dataTypes,  List<InputDefinition> resList, Map<String, List<ComponentInstancePropInput>> newInputsPropsMap) {
		List<ComponentInstance> ciList = component.getComponentInstances();
		String componentId = component.getUniqueId();
		for (Entry<String, List<ComponentInstancePropInput>> entry : newInputsPropsMap.entrySet()) {
			List<ComponentInstanceProperty> propertiesToCreate = new ArrayList<>();
			String compInstId = entry.getKey();
			List<ComponentInstancePropInput> properties = entry.getValue();
			
			Optional<ComponentInstance> op = ciList.stream().filter(ci -> ci.getUniqueId().equals(compInstId)).findAny();
			if(!op.isPresent()){
				ActionStatus actionStatus = ActionStatus.INVALID_CONTENT;
				log.debug("Failed to find component instance {} under component {}", compInstId, componentId);
				return Either.right(componentsUtils.getResponseFormat(actionStatus));
				
			}
			ComponentInstance ci = op.get();
			String compInstname = ci.getNormalizedName();
			Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> origComponentEither = getOriginComponent(ci, origComponentMap);
			if(origComponentEither.isRight()){
				ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(origComponentEither.right().value());
				log.debug("Failed to create inputs value under component {}, error: {}", componentId, actionStatus.name());
				return Either.right(componentsUtils.getResponseFormat(actionStatus));
				
			}
			org.openecomp.sdc.be.model.Component origComponent = origComponentEither.left().value();

			
			//String originType = (String) titanGenericDao.getProperty(originVertex, GraphPropertiesDictionary.LABEL.getProperty());
			
			String inputName = compInstname;
			
			if (properties != null && !properties.isEmpty()) {
				for (ComponentInstancePropInput propInput : properties) {
					Either<InputDefinition, StorageOperationStatus> createInputRes = createInputForComponentInstance(component, origComponent,ci, inputsToCreate, propertiesToCreate, dataTypes, inputName, propInput);
					
					if (createInputRes.isRight()) {
						log.debug("Failed to create input  of resource instance for id {} error {}", compInstId, createInputRes.right().value());
						return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(createInputRes.right().value())));
						
					}
					
					resList.add(createInputRes.left().value());
				
				}
				propertiesToCreateMap.put(compInstId, propertiesToCreate);
			}
			
		}
		return Either.left(resList);
	}

	private Either<InputDefinition, StorageOperationStatus> createInputForComponentInstance(org.openecomp.sdc.be.model.Component component,org.openecomp.sdc.be.model.Component orignComponent, ComponentInstance ci, Map<String, InputDefinition> inputsToCreate, List<ComponentInstanceProperty> propertiesToCreate, Map<String, DataTypeDefinition> dataTypes, String inputName, ComponentInstancePropInput propInput) {
		String propertiesName = propInput.getPropertiesName() ;
		PropertyDefinition selectedProp = propInput.getInput();
		String[] parsedPropNames = propInput.getParsedPropNames();
		
		if(parsedPropNames != null){
			for(String str: parsedPropNames){
				inputName += "_"  + str;
			}
		} else {
			inputName += "_"  + propInput.getName();
		}
		
		InputDefinition input = null;
		ComponentInstanceProperty prop = propInput;	
		
		if(propertiesName != null && !propertiesName.isEmpty() && selectedProp != null){
			input = new InputDefinition(selectedProp);
		}else{
			input = new InputDefinition(prop);
			input.setName(inputName + "_" + prop.getName());
			
		}
		input.setName(inputName);	
		input.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(component.getUniqueId(), input.getName()));
		input.setInputPath(propertiesName);
		
		JSONObject jobject = new JSONObject();
							
		
		if(prop.getValue() == null || prop.getValue().isEmpty()){
			if(propertiesName != null && !propertiesName.isEmpty() && selectedProp != null){
					
				jobject = createJSONValueForProperty(parsedPropNames.length -1, parsedPropNames, jobject, inputName);	
				prop.setValue(jobject.toJSONString());	
				
			}else{
				
				jobject.put(GET_INPUT, input.getName());
				prop.setValue(jobject.toJSONString());				
				
			}
			
		}else{
			
			String value = prop.getValue();		
			Object objValue =  new Yaml().load(value);
			if( objValue instanceof Map || objValue  instanceof List ){
				if(propertiesName == null ||propertiesName.isEmpty()){
					jobject.put(GET_INPUT, input.getName());
					prop.setValue(jobject.toJSONString());
					prop.setRules(null);
						
				}else{
					Map<String, Object> mappedToscaTemplate = (Map<String, Object>) objValue;
					createInputValue(mappedToscaTemplate, 1, parsedPropNames, inputName);
					
					String json = gson.toJson(mappedToscaTemplate);								
					prop.setValue(json);
					prop.setRules(null);
				}
					
			}else{
				jobject.put(GET_INPUT, input.getName());
				prop.setValue(jobject.toJSONString());
				prop.setRules(null);
			}
			
		}
		prop.setComponentInstanceId(ci.getUniqueId());
		prop.setComponentInstanceName(ci.getName());
		
		List<GetInputValueDataDefinition> getInputValues = new ArrayList<>();
		GetInputValueDataDefinition getInputValueDataDefinition = new GetInputValueDataDefinition();
		getInputValueDataDefinition.setInputId(input.getUniqueId());
		getInputValueDataDefinition.setInputName(input.getName());
		getInputValues.add(getInputValueDataDefinition);
		prop.setGetInputValues(getInputValues);	
		
		propertiesToCreate.add(prop);
		
		inputsToCreate.put(input.getName(), input);
		
		return Either.left(input);
		
	}
	
	private  JSONObject createJSONValueForProperty (int i, String [] parsedPropNames, JSONObject ooj, String inputName){
		
		while(i >= 1){
			if( i == parsedPropNames.length -1){				
				JSONObject jobProp = new JSONObject();
				jobProp.put(GET_INPUT, inputName);
				ooj.put(parsedPropNames[i], jobProp);
				i--;
				return createJSONValueForProperty (i, parsedPropNames, ooj, inputName);
			}else{
				JSONObject res = new JSONObject();
				res.put(parsedPropNames[i], ooj);
				i --;
				res =  createJSONValueForProperty (i, parsedPropNames, res, inputName);
				return res;
			}
		}
		
		return ooj;
	}
	
	public void resetInputName(Map<String, Object> lhm1, String inputName){
	    for (Map.Entry<String, Object> entry : lhm1.entrySet()) {
	        String key = entry.getKey();
	        Object value = entry.getValue();
	        if (value instanceof String && ((String) value).equalsIgnoreCase(inputName) && key.equals(GET_INPUT)) {
	        	value = "";
	        	lhm1.remove(key);	        	
	        } else if (value instanceof Map) {
	            Map<String, Object> subMap = (Map<String, Object>)value;
	            resetInputName(subMap, inputName);
	        } else {
	             continue;
	        }

	    }
	}
	
	private  Map<String, Object> createInputValue(Map<String, Object> lhm1, int index, String[] inputNames, String inputName){
		while(index < inputNames.length){
			if(lhm1.containsKey(inputNames[index])){
				Object value = lhm1.get(inputNames[index]);
				if (value instanceof Map){
					if(index == inputNames.length -1){
						((Map) value).put(GET_INPUT, inputName);
						return ((Map) value);
						
					}else{
						index++;
						return  createInputValue((Map)value, index, inputNames, inputName);
					}
				}else{
					Map<String, Object> jobProp = new HashMap<>();
					if(index == inputNames.length -1){
						jobProp.put(GET_INPUT, inputName);
						lhm1.put(inputNames[index], jobProp);
						return lhm1;						
					}else{						
						lhm1.put(inputNames[index], jobProp);
						index++;
						return  createInputValue(jobProp, index, inputNames, inputName);
					}
				}
			}else{				
				Map<String, Object> jobProp = new HashMap<>();
				lhm1.put(inputNames[index], jobProp);
				if(index == inputNames.length -1){
					jobProp.put(GET_INPUT, inputName);
					return jobProp;
				}else{
					index++;
					return  createInputValue(jobProp, index, inputNames, inputName);
				}
			}
		}
		return lhm1;
	}




}
