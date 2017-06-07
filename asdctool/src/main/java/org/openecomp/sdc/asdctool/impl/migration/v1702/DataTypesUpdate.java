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

package org.openecomp.sdc.asdctool.impl.migration.v1702;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.ImportUtils;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.yaml.snakeyaml.Yaml;

import fj.data.Either;

/**
 * Allows to update existing or create new data types according input file (yaml)
 * @author ns019t
 *
 */
public class DataTypesUpdate {
	
	private static Logger log = LoggerFactory.getLogger(Migration1702.class.getName());
	
	@Autowired
	private PropertyOperation propertyOperation;
	@Autowired
	private ComponentsUtils componentsUtils;

	@SuppressWarnings("unchecked")
	/**
	 * Updates existing or creates new data types according input file (yaml)
	 * @param dataTypeYmlFilePath
	 * @return
	 */
	public boolean updateDataTypes(String dataTypeYmlFilePath) {
		
		
		List<String> dataTypesToUpdate = new ArrayList<>();
		dataTypesToUpdate.add("org.openecomp.datatypes.EcompHoming");
		dataTypesToUpdate.add("org.openecomp.datatypes.EcompNaming");
		dataTypesToUpdate.add("org.openecomp.datatypes.network.NetworkAssignments");
		dataTypesToUpdate.add("org.openecomp.datatypes.network.ProviderNetwork");
		dataTypesToUpdate.add("org.openecomp.datatypes.network.NetworkFlows");
		dataTypesToUpdate.add("org.openecomp.datatypes.Artifact");
		dataTypesToUpdate.add("org.openecomp.datatypes.network.VlanRequirements");
		dataTypesToUpdate.add("org.openecomp.datatypes.network.IpRequirements");
		dataTypesToUpdate.add("org.openecomp.datatypes.network.MacAssignments");
		dataTypesToUpdate.add("org.openecomp.datatypes.network.MacRequirements");
		dataTypesToUpdate.add("org.openecomp.datatypes.heat.contrailV2.virtual.machine.subInterface.AddressPairIp");
		dataTypesToUpdate.add("org.openecomp.datatypes.heat.contrailV2.virtual.machine.subInterface.MacAddress");
		dataTypesToUpdate.add("org.openecomp.datatypes.heat.contrailV2.virtual.machine.subInterface.Properties");
		dataTypesToUpdate.add("org.openecomp.datatypes.heat.contrailV2.virtual.machine.subInterface.AddressPair");
		dataTypesToUpdate.add("org.openecomp.datatypes.heat.contrailV2.virtual.machine.subInterface.AddressPairs");
		
		boolean isSuccessful = true;
		List<DataTypeDefinition> dataTypes = extractDataTypesFromYaml(dataTypeYmlFilePath);
		
		if(CollectionUtils.isEmpty(dataTypes)){
			isSuccessful = false;
		}
		
		List<ImmutablePair<DataTypeDefinition, Boolean>> createdElementTypes = new ArrayList<>();

		Iterator<DataTypeDefinition> elementTypeItr = dataTypes.iterator();
		if(isSuccessful ){
			try {
				while (elementTypeItr.hasNext()) {
					DataTypeDefinition elementType = elementTypeItr.next();
					String elementName = elementType.getName();
					Either<ActionStatus, ResponseFormat> validateElementType = validateDataType(elementType);
					if (validateElementType.isRight()) {
						log.debug("Failed to validate data type {}. Status is {}. ", elementName, validateElementType.right().value());
						isSuccessful =  false;
						break;
					}
					log.debug("Going to get data type by name {}. ", elementName);
					Either<DataTypeDefinition, StorageOperationStatus> findElementType = propertyOperation.getDataTypeByNameWithoutDerived(elementName);
					if (findElementType.isRight()) {
						StorageOperationStatus status = findElementType.right().value();
						if (status != StorageOperationStatus.NOT_FOUND) {
							log.debug("Failed to fetch data type {}. Status is {}. ", elementName , validateElementType.right().value());
							isSuccessful =  false;
							break;
						} else {
							log.debug("Going to add data type with name {}. ", elementName);
							Either<DataTypeDefinition, StorageOperationStatus> dataModelResponse = propertyOperation.addDataType(elementType);
	
							if (dataModelResponse.isRight()) {
									if (dataModelResponse.right().value() != StorageOperationStatus.SCHEMA_VIOLATION) {
										log.debug("Failed to add data type {}. Status is {}. ", elementName , dataModelResponse.right().value());
										isSuccessful =  false;
										break;
									} else {
										createdElementTypes.add(new ImmutablePair<DataTypeDefinition, Boolean>(elementType, false));
									}
							} else {
								createdElementTypes.add(new ImmutablePair<DataTypeDefinition, Boolean>(elementType, true));
							}
	
						}
					} else {
						log.debug("Going to update data type with name {}. ", elementName);
						Either<DataTypeDefinition, StorageOperationStatus> updateDataTypeRes = propertyOperation.updateDataType(elementType, findElementType.left().value());
						if (updateDataTypeRes.isRight()) {
							StorageOperationStatus status = updateDataTypeRes.right().value();
							if (status == StorageOperationStatus.OK) {
								createdElementTypes.add(new ImmutablePair<DataTypeDefinition, Boolean>(elementType, false));
							} else {
								log.debug("Failed to update data type {}. Status is {}. ", elementName , updateDataTypeRes.right().value());
								isSuccessful =  false;
								break;
							}
						} else {
							createdElementTypes.add(new ImmutablePair<DataTypeDefinition, Boolean>(elementType, true));
						}
					}
				}
			} finally {
				if(isSuccessful){
					propertyOperation.getTitanGenericDao().commit();
				}else{
					propertyOperation.getTitanGenericDao().rollback();
				}
			}
		}
		return isSuccessful;
	}

	@SuppressWarnings("unchecked")
	static public List<DataTypeDefinition> extractDataTypesFromYaml(String dataTypeYmlFilePath) {
		String dataTypeName;
		List<DataTypeDefinition> dataTypes = new ArrayList<>();
		try {
			File file = new File(dataTypeYmlFilePath);
			FileReader fr = new FileReader(file);
			Map<String, Object> toscaJson = (Map<String, Object>) new Yaml().load(fr);

			Iterator<Entry<String, Object>> elementTypesEntryItr = toscaJson.entrySet().iterator();
			while (elementTypesEntryItr.hasNext()) {
				Entry<String, Object> elementTypeNameDataEntry = elementTypesEntryItr.next();
				dataTypeName = elementTypeNameDataEntry.getKey();
				Map<String, Object> elementTypeJsonData = (Map<String, Object>) elementTypeNameDataEntry.getValue();
				
				DataTypeDefinition dataType = new DataTypeDefinition();
				dataType.setName(dataTypeName);

				if (elementTypeJsonData != null) {
					
					if (elementTypeJsonData.containsKey(ToscaTagNamesEnum.DESCRIPTION.getElementName())) {
						dataType.setDescription( (String)elementTypeJsonData.get(ToscaTagNamesEnum.DESCRIPTION.getElementName()));
					}
					if (elementTypeJsonData.containsKey(ToscaTagNamesEnum.DERIVED_FROM.getElementName())) {
						dataType.setDerivedFromName( (String)elementTypeJsonData.get(ToscaTagNamesEnum.DERIVED_FROM.getElementName()));
					}
					List<PropertyDefinition> properties = getProperties(elementTypeJsonData);
					if (elementTypeJsonData.containsKey(ToscaTagNamesEnum.PROPERTIES.getElementName())) {
						dataType.setProperties(properties);
					}
				}
				dataTypes.add(dataType);
			}

		} catch (Exception e) {
			log.debug("Failed to extract data types from Yaml file {}. ", dataTypeYmlFilePath);
			e.printStackTrace();
		}
		return dataTypes;
	}
	
	static public List<PropertyDefinition> getProperties(Map<String, Object> toscaJson) {
		List<PropertyDefinition> values = null;
		Either<Map<String, PropertyDefinition>, ResultStatusEnum> properties = ImportUtils.getProperties(toscaJson);

		if (properties.isLeft()) {
			values = new ArrayList<>();
			Map<String, PropertyDefinition> propertiesMap = properties.left().value();
			if (propertiesMap != null && propertiesMap.isEmpty() == false) {

				for (Entry<String, PropertyDefinition> entry : propertiesMap.entrySet()) {
					String propName = entry.getKey();
					PropertyDefinition propertyDefinition = entry.getValue();
					PropertyDefinition newPropertyDefinition = new PropertyDefinition(propertyDefinition);
					newPropertyDefinition.setName(propName);
					values.add(newPropertyDefinition);
				}
			}
		}

		return values;
	}

	private Either<ActionStatus, ResponseFormat> validateDataType(DataTypeDefinition dataType) {

		String dataTypeName = dataType.getName();
		List<PropertyDefinition> properties = dataType.getProperties();
		if (properties == null) {
			// At least one parameter should be defined either in the properties
			// section or at one of the parents
			String derivedDataType = dataType.getDerivedFromName();
			// If there are no properties, then we can create a data type if it
			// is an abstract one or it derives from non abstract data type
			if ((derivedDataType == null || derivedDataType.isEmpty())) {
				if (false == isAbstract(dataType.getName())) {
					if (false == ToscaPropertyType.isScalarType(dataTypeName)) {
						log.debug("Data type {} must have properties unless it derives from non abstract data type",dataType.getName());
						ResponseFormat responseFormat = componentsUtils.getResponseFormatByDataType(ActionStatus.DATA_TYPE_NOR_PROPERTIES_NEITHER_DERIVED_FROM, dataType, null);

						return Either.right(responseFormat);
					}
				}
			} else {
				// if it is not a scalar data type and it derives from abstract
				// data type, we should reject the request.
				if (false == ToscaPropertyType.isScalarType(dataTypeName) && true == isAbstract(derivedDataType)) {
					log.debug("Data type {} which derived from abstract data type must have at least one property",dataType.getName());
					ResponseFormat responseFormat = componentsUtils.getResponseFormatByDataType(ActionStatus.DATA_TYPE_NOR_PROPERTIES_NEITHER_DERIVED_FROM, dataType, null);

					return Either.right(responseFormat);
				}
			}
		} else {
			// properties tag cannot be empty
			if (properties.isEmpty()) {
				ResponseFormat responseFormat = componentsUtils.getResponseFormatByDataType(ActionStatus.DATA_TYPE_PROPERTIES_CANNOT_BE_EMPTY, dataType, null);

				return Either.right(responseFormat);
			}

			// check no duplicates
			Set<String> collect = properties.stream().map(p -> p.getName()).collect(Collectors.toSet());
			if (collect != null) {
				if (properties.size() != collect.size()) {
					ResponseFormat responseFormat = componentsUtils.getResponseFormatByDataType(ActionStatus.DATA_TYPE_DUPLICATE_PROPERTY, dataType, null);

					return Either.right(responseFormat);
				}
			}

			List<String> propertiesWithSameTypeAsDataType = properties.stream().filter(p -> p.getType().equals(dataType.getName())).map(p -> p.getName()).collect(Collectors.toList());
			if (propertiesWithSameTypeAsDataType != null && propertiesWithSameTypeAsDataType.isEmpty() == false) {
				log.debug("The data type {} contains properties with the type {}",dataType.getName(),dataType.getName());
				ResponseFormat responseFormat = componentsUtils.getResponseFormatByDataType(ActionStatus.DATA_TYPE_PROEPRTY_CANNOT_HAVE_SAME_TYPE_OF_DATA_TYPE, dataType, propertiesWithSameTypeAsDataType);

				return Either.right(responseFormat);
			}
		}

		String derivedDataType = dataType.getDerivedFromName();
		if (derivedDataType != null) {
			Either<DataTypeDefinition, StorageOperationStatus> derivedDataTypeByName = propertyOperation.getDataTypeByName(derivedDataType, true);
			if (derivedDataTypeByName.isRight()) {
				StorageOperationStatus status = derivedDataTypeByName.right().value();
				if (status == StorageOperationStatus.NOT_FOUND) {
					ResponseFormat responseFormat = componentsUtils.getResponseFormatByDataType(ActionStatus.DATA_TYPE_DERIVED_IS_MISSING, dataType, null);

					return Either.right(responseFormat);
				} else {
					ResponseFormat responseFormat = componentsUtils.getResponseFormatByDataType(ActionStatus.GENERAL_ERROR, dataType, null);

					return Either.right(responseFormat);

				}
			} else {

				DataTypeDefinition derivedDataTypeDef = derivedDataTypeByName.left().value();
				if (properties != null && properties.isEmpty() == false) {

					if (true == isScalarType(derivedDataTypeDef)) {
						ResponseFormat responseFormat = componentsUtils.getResponseFormatByDataType(ActionStatus.DATA_TYPE_CANNOT_HAVE_PROPERTIES, dataType, null);

						return Either.right(responseFormat);
					}

					Set<String> allParentsProps = new HashSet<>();
					do {
						List<PropertyDefinition> currentParentsProps = derivedDataTypeDef.getProperties();
						if (currentParentsProps != null) {
							for (PropertyDefinition propertyDefinition : currentParentsProps) {
								allParentsProps.add(propertyDefinition.getName());
							}
						}
						derivedDataTypeDef = derivedDataTypeDef.getDerivedFrom();
					} while (derivedDataTypeDef != null);

					// Check that no property is already defined in one of the
					// ancestors
					Set<String> alreadyExistPropsCollection = properties.stream().filter(p -> allParentsProps.contains(p.getName())).map(p -> p.getName()).collect(Collectors.toSet());
					if (alreadyExistPropsCollection != null && alreadyExistPropsCollection.isEmpty() == false) {
						List<String> duplicateProps = new ArrayList<>();
						duplicateProps.addAll(alreadyExistPropsCollection);
						ResponseFormat responseFormat = componentsUtils.getResponseFormatByDataType(ActionStatus.DATA_TYPE_PROPERTY_ALREADY_DEFINED_IN_ANCESTOR, dataType, duplicateProps);

						return Either.right(responseFormat);
					}

				}
			}
		}
		return Either.left(ActionStatus.OK);
	}
	
	private boolean isAbstract(String dataTypeName) {

		ToscaPropertyType isPrimitiveToscaType = ToscaPropertyType.isValidType(dataTypeName);

		return isPrimitiveToscaType != null && isPrimitiveToscaType.isAbstract() == true;

	}

	private boolean isScalarType(DataTypeDefinition dataTypeDef) {

		boolean isScalar = false;
		DataTypeDefinition dataType = dataTypeDef;

		while (dataType != null) {

			String name = dataType.getName();
			if (ToscaPropertyType.isScalarType(name)) {
				isScalar = true;
				break;
			}

			dataType = dataType.getDerivedFrom();
		}

		return isScalar;
	}
	
//	public Either<DataTypeDefinition, StorageOperationStatus> updateDataType(DataTypeDefinition newDataTypeDefinition, DataTypeDefinition oldDataTypeDefinition) {
//
//		Either<DataTypeDefinition, StorageOperationStatus> result = null;
//
//		try {
//
//			List<PropertyDefinition> newProperties = newDataTypeDefinition.getProperties();
//
//			List<PropertyDefinition> oldProperties = oldDataTypeDefinition.getProperties();
//
//			String newDerivedFromName = getDerivedFromName(newDataTypeDefinition);
//
//			String oldDerivedFromName = getDerivedFromName(oldDataTypeDefinition);
//
//			String dataTypeName = newDataTypeDefinition.getName();
//			
//			List<PropertyDefinition> propertiesToAdd = new ArrayList<>();
//			if (isPropertyOmitted(newProperties, oldProperties, dataTypeName) || isPropertyTypeChanged(dataTypeName, newProperties, oldProperties, propertiesToAdd) || isDerivedFromNameChanged(dataTypeName, newDerivedFromName, oldDerivedFromName)) {
//
//				log.debug("The new data type " + dataTypeName + " is invalid.");
//
//				result = Either.right(StorageOperationStatus.CANNOT_UPDATE_EXISTING_ENTITY);
//				return result;
//			}
//
//			if (propertiesToAdd == null || propertiesToAdd.isEmpty()) {
//				log.debug("No new properties has been defined in the new data type " + newDataTypeDefinition);
//				result = Either.right(StorageOperationStatus.OK);
//				return result;
//			}
//
//			Either<Map<String, PropertyData>, TitanOperationStatus> addPropertiesToDataType = addPropertiesToDataType(oldDataTypeDefinition.getUniqueId(), propertiesToAdd);
//
//			if (addPropertiesToDataType.isRight()) {
//				log.debug("Failed to update data type {} to Graph. Status is {}", oldDataTypeDefinition, addPropertiesToDataType.right().value().name());
//				BeEcompErrorManager.getInstance().logBeFailedAddingNodeTypeError("UpdateDataType", "Property");
//				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(addPropertiesToDataType.right().value()));
//				return result;
//			} else {
//
//				Either<DataTypeDefinition, TitanOperationStatus> dataTypeByUid = this.getDataTypeByUid(oldDataTypeDefinition.getUniqueId());
//				if (dataTypeByUid.isRight()) {
//					TitanOperationStatus status = addPropertiesToDataType.right().value();
//					log.debug("Failed to get data type {} after update. Status is {}", oldDataTypeDefinition.getUniqueId(), status.name());
//					BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError("UpdateDataType", "Property", status.name());
//					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
//				} else {
//					result = Either.left(dataTypeByUid.left().value());
//				}
//			}
//
//			return result;
//
//		}
//	}

}
