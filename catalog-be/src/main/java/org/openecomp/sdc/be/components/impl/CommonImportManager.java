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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import fj.data.Either;
import jersey.repackaged.com.google.common.base.Function;

@Component("commonImportManager")
public class CommonImportManager {

	private static Logger log = LoggerFactory.getLogger(CommonImportManager.class.getName());

	@Resource
	private ComponentsUtils componentsUtils;
	@Resource
	private PropertyOperation propertyOperation;

	protected void setProperties(Map<String, Object> toscaJson, Consumer<List<PropertyDefinition>> consumer) {
		consumer.accept(getProperties(toscaJson));
	}

	private List<PropertyDefinition> getProperties(Map<String, Object> toscaJson) {
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

	protected void setPropertiesMap(Map<String, Object> toscaJson, Consumer<Map<String, PropertyDefinition>> consumer) {
		final List<PropertyDefinition> properties = getProperties(toscaJson);
		if (properties != null) {
			Map<String, PropertyDefinition> collect = properties.stream().collect(Collectors.toMap(e -> e.getName(), e -> e));
			consumer.accept(collect);
		}

	}

	interface ICreateElementType<T1, T2, ElementType> {
		ElementType createElement(T1 firstArg, T2 secondArg);
	}

	protected <ElementDefinition> Either<List<ElementDefinition>, ActionStatus> createElementTypesFromYml(String elementTypesYml, ICreateElementType<String, Map<String, Object>, ElementDefinition> createApi) {

		List<ElementDefinition> elementTypes = new ArrayList<>();
		try {
			Map<String, Object> toscaJson = (Map<String, Object>) new Yaml().load(elementTypesYml);

			Iterator<Entry<String, Object>> elementTypesEntryItr = toscaJson.entrySet().iterator();
			while (elementTypesEntryItr.hasNext()) {
				Entry<String, Object> elementTypeNameDataEntry = elementTypesEntryItr.next();
				String elementTypeName = elementTypeNameDataEntry.getKey();
				Map<String, Object> elementTypeJsonData = (Map<String, Object>) elementTypeNameDataEntry.getValue();
				ElementDefinition elementDefinition = createApi.createElement(elementTypeName, elementTypeJsonData);
				elementTypes.add(elementDefinition);

			}

		} catch (Exception e) {
			log.debug("Failed to yaml file {}", elementTypesYml, e);
			return Either.right(ActionStatus.INVALID_YAML_FILE);
		}
		return Either.left(elementTypes);
	}

	protected <FieldType> void setField(Map<String, Object> toscaJson, String fieldName, Consumer<FieldType> setter) {
		if (toscaJson.containsKey(fieldName)) {
			FieldType fieldValue = (FieldType) toscaJson.get(fieldName);
			setter.accept(fieldValue);
		}

	}

	public enum ElementTypeEnum {
		PolicyType, GroupType, DataType, CapabilityType, InterfaceLifecycleType
	};

	private ActionStatus convertFromStorageResponseForElementType(StorageOperationStatus status, ElementTypeEnum elementTypeEnum) {
		ActionStatus ret;
		switch (elementTypeEnum) {
		case GroupType:
			ret = componentsUtils.convertFromStorageResponseForGroupType(status);
			break;
		case DataType:
			ret = componentsUtils.convertFromStorageResponseForDataType(status);
			break;
		case CapabilityType:
			ret = componentsUtils.convertFromStorageResponseForCapabilityType(status);
			break;
		case InterfaceLifecycleType:
			ret = componentsUtils.convertFromStorageResponseForLifecycleType(status);
			break;
		default:
			ret = componentsUtils.convertFromStorageResponse(status);
			break;
		}
		return ret;
	}

	private <ElementTypeDefinition> ResponseFormat getResponseFormatForElementType(ActionStatus actionStatus, ElementTypeEnum elementTypeEnum, ElementTypeDefinition elementTypeDefinition) {
		ResponseFormat ret;
		switch (elementTypeEnum) {
		case GroupType:
			ret = componentsUtils.getResponseFormatByGroupType(actionStatus, (GroupTypeDefinition) elementTypeDefinition);
			break;
		case PolicyType:
			ret = componentsUtils.getResponseFormatByPolicyType(actionStatus, (PolicyTypeDefinition) elementTypeDefinition);
			break;
		case DataType:
			ret = componentsUtils.getResponseFormatByDataType(actionStatus, (DataTypeDefinition) elementTypeDefinition, null);
			break;
		case CapabilityType:
			ret = componentsUtils.getResponseFormatByCapabilityType(actionStatus, (CapabilityTypeDefinition) elementTypeDefinition);
			break;

		default:
			ret = componentsUtils.getResponseFormat(actionStatus);
			break;
		}
		return ret;
	}

	protected <ElementTypeDefinition> Either<List<ImmutablePair<ElementTypeDefinition, Boolean>>, ResponseFormat> createElementTypesByDao(List<ElementTypeDefinition> elementTypesToCreate,
			Function<ElementTypeDefinition, Either<ActionStatus, ResponseFormat>> validator, Function<ElementTypeDefinition, ImmutablePair<ElementTypeEnum, String>> elementInfoGetter,
			Function<String, Either<ElementTypeDefinition, StorageOperationStatus>> elementFetcher, Function<ElementTypeDefinition, Either<ElementTypeDefinition, StorageOperationStatus>> elementAdder,
			BiFunction<ElementTypeDefinition, ElementTypeDefinition, Either<ElementTypeDefinition, StorageOperationStatus>> elementUpgrader) {

		List<ImmutablePair<ElementTypeDefinition, Boolean>> createdElementTypes = new ArrayList<>();

		Either<List<ImmutablePair<ElementTypeDefinition, Boolean>>, ResponseFormat> eitherResult = Either.left(createdElementTypes);

		Iterator<ElementTypeDefinition> elementTypeItr = elementTypesToCreate.iterator();

		try {

			while (elementTypeItr.hasNext()) {
				ElementTypeDefinition elementType = elementTypeItr.next();
				final ImmutablePair<ElementTypeEnum, String> elementInfo = elementInfoGetter.apply(elementType);
				ElementTypeEnum elementTypeEnum = elementInfo.left;
				String elementName = elementInfo.right;

				Either<ActionStatus, ResponseFormat> validateElementType = validator.apply(elementType);
				if (validateElementType.isRight()) {
					ResponseFormat responseFormat = validateElementType.right().value();
					log.debug("Failed in validation of element type: {}. Response is {}", elementType, responseFormat.getFormattedMessage());
					eitherResult = Either.right(responseFormat);
					break;
				}

				log.info("send {} : {} to dao for create", elementTypeEnum.name(), elementName);

				Either<ElementTypeDefinition, StorageOperationStatus> findElementType = elementFetcher.apply(elementName);
				if (findElementType.isRight()) {
					StorageOperationStatus status = findElementType.right().value();
					log.debug("searched {} finished with result:{}", elementTypeEnum.name(), status.name());
					if (status != StorageOperationStatus.NOT_FOUND) {
						ResponseFormat responseFormat = getResponseFormatForElementType(convertFromStorageResponseForElementType(status, elementTypeEnum), elementTypeEnum, elementType);
						eitherResult = Either.right(responseFormat);
						break;
					} else {
						Either<ElementTypeDefinition, StorageOperationStatus> dataModelResponse = elementAdder.apply(elementType);

						if (dataModelResponse.isRight()) {
							try {
								BeEcompErrorManager.getInstance().logBeFailedAddingNodeTypeError("Create {}", elementTypeEnum.name());
								log.debug("failed to create {}: {}", elementTypeEnum.name(), elementName);
								if (dataModelResponse.right().value() != StorageOperationStatus.SCHEMA_VIOLATION) {
									ResponseFormat responseFormat = getResponseFormatForElementType(convertFromStorageResponseForElementType(dataModelResponse.right().value(), elementTypeEnum), elementTypeEnum, elementType);

									eitherResult = Either.right(responseFormat);
									break;
								} else {
									createdElementTypes.add(new ImmutablePair<ElementTypeDefinition, Boolean>(elementType, false));
								}
							} finally {
								propertyOperation.getTitanGenericDao().rollback();
							}
						} else {
							propertyOperation.getTitanGenericDao().commit();
							createdElementTypes.add(new ImmutablePair<ElementTypeDefinition, Boolean>(elementType, true));
							log.debug("{} : {}  was created successfully.", elementTypeEnum.name(), elementName);
						}
						if (!elementTypeItr.hasNext()) {
							log.info("all {} were created successfully!!!", elementTypeEnum.name());
						}

					}
				} else {

					if (elementUpgrader != null) {
						Either<ElementTypeDefinition, StorageOperationStatus> upgradeResponse = null;
						try {
							upgradeResponse = elementUpgrader.apply(elementType, findElementType.left().value());
							if (upgradeResponse.isRight()) {
								StorageOperationStatus status = upgradeResponse.right().value();
								if (status == StorageOperationStatus.OK) {
									createdElementTypes.add(new ImmutablePair<ElementTypeDefinition, Boolean>(elementType, false));
								} else {
									ResponseFormat responseFormat = getResponseFormatForElementType(convertFromStorageResponseForElementType(upgradeResponse.right().value(), elementTypeEnum), elementTypeEnum, elementType);
									eitherResult = Either.right(responseFormat);
									break;
								}
							} else {
								log.debug("{} : {}  was upgraded successfully.", elementTypeEnum.name(), elementName);
								createdElementTypes.add(new ImmutablePair<ElementTypeDefinition, Boolean>(elementType, true));
							}
						} finally {
							if (upgradeResponse == null || upgradeResponse.isRight()) {
								propertyOperation.getTitanGenericDao().rollback();
							} else {
								propertyOperation.getTitanGenericDao().commit();
							}
						}

					} else {
						// mshitrit Once GroupType Versions are supported add
						// code here
						createdElementTypes.add(new ImmutablePair<ElementTypeDefinition, Boolean>(elementType, false));
						log.debug("{} : {} already exists.", elementTypeEnum.name(), elementName);
					}

				}

			}
		} finally {
			if (eitherResult.isRight()) {
				propertyOperation.getTitanGenericDao().rollback();
			}
		}

		return eitherResult;

	}

	public <ElementTypeDefinition> Either<List<ImmutablePair<ElementTypeDefinition, Boolean>>, ResponseFormat> createElementTypes(String elementTypesYml, Function<String, Either<List<ElementTypeDefinition>, ActionStatus>> elementTypeFromYmlCreater,
			Function<List<ElementTypeDefinition>, Either<List<ImmutablePair<ElementTypeDefinition, Boolean>>, ResponseFormat>> elementTypeDaoCreater, ElementTypeEnum elementTypeEnum) {

		Either<List<ElementTypeDefinition>, ActionStatus> elementTypes = elementTypeFromYmlCreater.apply(elementTypesYml);
		if (elementTypes.isRight()) {
			ActionStatus status = elementTypes.right().value();
			ResponseFormat responseFormat = getResponseFormatForElementType(status, elementTypeEnum, null);
			return Either.right(responseFormat);
		}
		return elementTypeDaoCreater.apply(elementTypes.left().value());

	}
}
