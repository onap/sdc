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

import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.model.ToscaTypeImportData;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ToscaTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.normatives.ToscaTypeMetadata;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.api.TypeOperations;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component("commonImportManager")
public class CommonImportManager {

    private static final Logger log = Logger.getLogger(CommonImportManager.class.getName());

    private final ComponentsUtils componentsUtils;
    private final PropertyOperation propertyOperation;

    public CommonImportManager(ComponentsUtils componentsUtils, PropertyOperation propertyOperation) {
        this.componentsUtils = componentsUtils;
        this.propertyOperation = propertyOperation;
    }

    public static void setProperties(Map<String, Object> toscaJson, Consumer<List<PropertyDefinition>> consumer) {
        consumer.accept(getProperties(toscaJson));
    }

    private static List<PropertyDefinition> getProperties(Map<String, Object> toscaJson) {
        List<PropertyDefinition> values = null;
        Either<Map<String, PropertyDefinition>, ResultStatusEnum> properties = ImportUtils.getProperties(toscaJson);

        if (properties.isLeft()) {
            values = new ArrayList<>();
            Map<String, PropertyDefinition> propertiesMap = properties.left().value();
            if (propertiesMap != null && !propertiesMap.isEmpty()) {

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
            Map<String, PropertyDefinition> collect = properties.stream()
                                                        .collect(Collectors.toMap(PropertyDefinition::getName, Function.identity()));
            consumer.accept(collect);
        }
    }

    public interface ICreateElementType<T1, T2, T3> {
        T3 createElement(T1 firstArg, T2 secondArg);
    }

    protected <T> Either<List<T>, ActionStatus> createElementTypesFromYml(String elementTypesYml, ICreateElementType<String, Map<String, Object>, T> createApi) {

        List<T> elementTypes;
        Map<String, Object> toscaJson = convertToFieldMap(elementTypesYml);
        if (toscaJson==null) {
            return Either.right(ActionStatus.INVALID_YAML_FILE);
        }
        elementTypes = createElementTypesFromToscaJsonMap(createApi, toscaJson);
        return Either.left(elementTypes);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToFieldMap(String elementTypesYml) {
        Map<String, Object> toscaJson = null;
        try {
            toscaJson = (Map<String, Object>) new Yaml().load(elementTypesYml);
        } catch (Exception e) {
            log.debug("Failed to yaml file {}", elementTypesYml, e);
        }
        return toscaJson;
    }


    protected <T extends ToscaDataDefinition> List<T> createTypesFromToscaJsonMap(
            BiFunction<String, Map<String, Object>, T> createApi, Map<String, Object> toscaJson) {
        List<T> elementTypes = new ArrayList<>();

        for (Entry<String, Object> elementTypeNameDataEntry : toscaJson.entrySet()) {
            String elementTypeName = elementTypeNameDataEntry.getKey();
            Map<String, Object> elementTypeJsonData = (Map<String, Object>) elementTypeNameDataEntry.getValue();
            T elementDefinition = createApi.apply(elementTypeName, elementTypeJsonData);
            elementTypes.add(elementDefinition);
        }
        return elementTypes;
    }

    protected <T> List<T> createElementTypesFromToscaJsonMap(
                ICreateElementType<String, Map<String, Object>, T> createApi, Map<String, Object> toscaJson) {
        List<T> elementTypes = new ArrayList<>();

        for (Entry<String, Object> elementTypeNameDataEntry : toscaJson.entrySet()) {
            String elementTypeName = elementTypeNameDataEntry.getKey();
            Map<String, Object> elementTypeJsonData = (Map<String, Object>) elementTypeNameDataEntry.getValue();
            T elementDefinition = createApi.createElement(elementTypeName, elementTypeJsonData);
            elementTypes.add(elementDefinition);
        }
        return elementTypes;
    }

    protected <T> Map<String, T> createElementTypesMapFromToscaJsonMap(
                ICreateElementType<String, Map<String, Object>, T> createApi, Map<String, Object> toscaJson) {
        Map<String, T> elementTypesMap = new HashMap<>();
        
        Iterator<Entry<String, Object>> elementTypesEntryItr = toscaJson.entrySet().iterator();
        while (elementTypesEntryItr.hasNext()) {
            Entry<String, Object> elementTypeNameDataEntry = elementTypesEntryItr.next();
            String elementTypeName = elementTypeNameDataEntry.getKey();
            Map<String, Object> elementTypeJsonData = (Map<String, Object>) elementTypeNameDataEntry.getValue();
            T elementDefinition = createApi.createElement(elementTypeName, elementTypeJsonData);
            elementTypesMap.put(elementTypeName, elementDefinition);
        }
        return elementTypesMap;
    }

    protected <F> void setField(Map<String, Object> toscaJson, String fieldName, Consumer<F> setter) {
        if (toscaJson.containsKey(fieldName)) {
            F fieldValue = (F) toscaJson.get(fieldName);
            setter.accept(fieldValue);
        }
    }

    public enum ElementTypeEnum {
        POLICY_TYPE, GROUP_TYPE, DATA_TYPE, CAPABILITY_TYPE, INTERFACE_LIFECYCLE_TYPE, RELATIONSHIP_TYPE
    }

    private ActionStatus convertFromStorageResponseForElementType(StorageOperationStatus status, ElementTypeEnum elementTypeEnum) {
        ActionStatus ret;
        switch (elementTypeEnum) {
        case GROUP_TYPE:
            ret = componentsUtils.convertFromStorageResponseForGroupType(status);
            break;
        case DATA_TYPE:
            ret = componentsUtils.convertFromStorageResponseForDataType(status);
            break;
        case CAPABILITY_TYPE:
            ret = componentsUtils.convertFromStorageResponseForCapabilityType(status);
            break;
        case INTERFACE_LIFECYCLE_TYPE:
            ret = componentsUtils.convertFromStorageResponseForLifecycleType(status);
            break;
        case RELATIONSHIP_TYPE:
            ret = componentsUtils.convertFromStorageResponseForRelationshipType(status);
            break;
        default:
            ret = componentsUtils.convertFromStorageResponse(status);
            break;
        }
        return ret;
    }

    private <T> ResponseFormat getResponseFormatForElementType(ActionStatus actionStatus, ElementTypeEnum elementTypeEnum, T elementTypeDefinition) {
        ResponseFormat ret;
        switch (elementTypeEnum) {
        case GROUP_TYPE:
            ret = componentsUtils.getResponseFormatByGroupType(actionStatus, (GroupTypeDefinition) elementTypeDefinition);
            break;
        case POLICY_TYPE:
            ret = componentsUtils.getResponseFormatByPolicyType(actionStatus, (PolicyTypeDefinition) elementTypeDefinition);
            break;
        case DATA_TYPE:
            ret = componentsUtils.getResponseFormatByDataType(actionStatus, (DataTypeDefinition) elementTypeDefinition, null);
            break;
        case CAPABILITY_TYPE:
            ret = componentsUtils.getResponseFormatByCapabilityType(actionStatus, (CapabilityTypeDefinition) elementTypeDefinition);
            break;

        default:
            ret = componentsUtils.getResponseFormat(actionStatus);
            break;
        }
        return ret;
    }

    private <T extends ToscaDataDefinition> List<ImmutablePair<T, Boolean>> createTypesByDao(List<T> elementTypesToCreate,
                                                                                             TypeOperations<T> typeOperations) {
        List<ImmutablePair<T, Boolean>> createdElementTypes = new ArrayList<>();
        for (T newTypeDefinition : elementTypesToCreate) {
            try {
                String typeName = newTypeDefinition.getType();
                T existingDefinition = typeOperations.getLatestType(typeName);
                if (existingDefinition == null /*new type*/) {
                    typeOperations.addType(newTypeDefinition);
                } else {
                    if (typeOperations.isSameType(newTypeDefinition, existingDefinition)) {
                        propertyOperation.getJanusGraphGenericDao().rollback();
                        createdElementTypes.add(new ImmutablePair<>(newTypeDefinition, null));
                        continue;
                    } else {
                        typeOperations.updateType(existingDefinition, newTypeDefinition);
                    }
                }
                propertyOperation.getJanusGraphGenericDao().commit();
                createdElementTypes.add(new ImmutablePair<>(newTypeDefinition, true));
            } catch (Exception e) {
                propertyOperation.getJanusGraphGenericDao().rollback();
                createdElementTypes.add(new ImmutablePair<>(newTypeDefinition, false));
            }

        }
        return createdElementTypes;
    }
    
    protected <T> Either<List<ImmutablePair<T, Boolean>>, ResponseFormat> createElementTypesByDao(List<T> elementTypesToCreate,
            Function<T, Either<ActionStatus, ResponseFormat>> validator, Function<T, ImmutablePair<ElementTypeEnum, String>> elementInfoGetter,
            Function<String, Either<T, StorageOperationStatus>> elementFetcher, Function<T, Either<T, StorageOperationStatus>> elementAdder,
            BiFunction<T, T, Either<T, StorageOperationStatus>> elementUpgrader) {

        List<ImmutablePair<T, Boolean>> createdElementTypes = new ArrayList<>();

        Either<List<ImmutablePair<T, Boolean>>, ResponseFormat> eitherResult = Either.left(createdElementTypes);
        Iterator<T> elementTypeItr = elementTypesToCreate.iterator();

        try {
            while (elementTypeItr.hasNext()) {
                T elementType = elementTypeItr.next();
                eitherResult = handleType(elementType, validator, elementInfoGetter, elementFetcher, elementAdder, elementUpgrader)
                                            .left()
                                            .map(elem -> append(createdElementTypes, elem));
                
                if (eitherResult.isRight()) {
                    break;
                }
                
                if(!elementTypeItr.hasNext()) {
                    log.info("all {} were created successfully!!!", elementType);
                }
            }
        }
        catch(Exception e) {
            eitherResult = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            throw e;
        } 
        finally {
            if (eitherResult.isLeft()) {
                propertyOperation.getJanusGraphGenericDao().commit();
            }
            else {
                propertyOperation.getJanusGraphGenericDao().rollback();
            }
        }

        return eitherResult;
    }
    
    private static <T> List<T> append(List<T> list, T value) {
        list.add(value);
        return list;
    }
    
    
    private <T> Either<ImmutablePair<T, Boolean>, ResponseFormat> handleType(T elementType, 
            Function<T, Either<ActionStatus, ResponseFormat>> validator, Function<T, ImmutablePair<ElementTypeEnum, String>> elementInfoGetter,
            Function<String, Either<T, StorageOperationStatus>> elementFetcher, Function<T, Either<T, StorageOperationStatus>> elementAdder,
            BiFunction<T, T, Either<T, StorageOperationStatus>> elementUpgrader) {
        
        final ImmutablePair<ElementTypeEnum, String> elementInfo = elementInfoGetter.apply(elementType);
        ElementTypeEnum elementTypeEnum = elementInfo.left;
        String elementName = elementInfo.right;
        
        Either<ActionStatus, ResponseFormat> validateElementType = validator.apply(elementType);
        if (validateElementType.isRight()) {
            ResponseFormat responseFormat = validateElementType.right().value();
            log.debug("Failed in validation of element type: {}. Response is {}", elementType, responseFormat.getFormattedMessage());
            return Either.right(responseFormat);
        }

        log.info("send {} : {} to dao for create", elementTypeEnum, elementName);

        Either<T, StorageOperationStatus> findElementType = elementFetcher.apply(elementName);
        if (findElementType.isRight()) {
            StorageOperationStatus status = findElementType.right().value();
            log.debug("searched {} finished with result:{}", elementTypeEnum, status);
            if (status != StorageOperationStatus.NOT_FOUND) {
                ResponseFormat responseFormat = getResponseFormatForElementType(convertFromStorageResponseForElementType(status, elementTypeEnum), elementTypeEnum, elementType);
                return Either.right(responseFormat);
            } else {
                return addElementType(elementType, elementAdder, elementTypeEnum, elementName);
            }
        } else {

            if (elementUpgrader != null) {
                return updateElementType(elementType, elementUpgrader, elementTypeEnum, elementName, findElementType.left().value());

            } else {
                // mshitrit Once GroupType Versions are supported add
                // code here
                log.debug("{} : {} already exists.", elementTypeEnum, elementName);
                return Either.left(new ImmutablePair<>(elementType, false));
            }

        }
    }

    private <T> Either<ImmutablePair<T, Boolean>, ResponseFormat> addElementType(T elementType, Function<T, Either<T, StorageOperationStatus>> elementAdder, ElementTypeEnum elementTypeEnum, String elementName) {
        Either<T, StorageOperationStatus> dataModelResponse = elementAdder.apply(elementType);
        
        if (dataModelResponse.isRight()) {
            BeEcompErrorManager.getInstance().logBeFailedAddingNodeTypeError("Create {}", elementTypeEnum.name());
            log.debug("failed to create {}: {}", elementTypeEnum, elementName);
            if (dataModelResponse.right().value() != StorageOperationStatus.OK) {
                ResponseFormat responseFormat = getResponseFormatForElementType(convertFromStorageResponseForElementType(dataModelResponse.right().value(), elementTypeEnum), elementTypeEnum, elementType);
                
                return Either.right(responseFormat);
            } else {
                return Either.left(new ImmutablePair<>(elementType, false));
            }
        } else {
            log.debug("{} : {}  was created successfully.", elementTypeEnum, elementName);
            return Either.left(new ImmutablePair<>(elementType, true));
        }
    }
    
    
    private <T> Either<ImmutablePair<T, Boolean>, ResponseFormat> updateElementType(T elementType, BiFunction<T, T, Either<T, StorageOperationStatus>> elementUpgrader, 
                                                        ElementTypeEnum elementTypeEnum, String elementName, T existingElementType) {
        Either<T, StorageOperationStatus> upgradeResponse = elementUpgrader.apply(elementType, existingElementType);
        if (upgradeResponse.isRight()) {
            StorageOperationStatus status = upgradeResponse.right().value();
            if (status == StorageOperationStatus.OK) {
                return Either.left(new ImmutablePair<>(elementType, false));
            } else {
                ResponseFormat responseFormat = getResponseFormatForElementType(convertFromStorageResponseForElementType(upgradeResponse.right().value(), elementTypeEnum), elementTypeEnum, elementType);
                return Either.right(responseFormat);
            }
        } else {
            log.debug("{} : {}  was upgraded successfully.", elementTypeEnum, elementName);
            return Either.left(new ImmutablePair<>(elementType, true));
        }
    }

    
    public <T extends ToscaTypeDataDefinition> Either<List<ImmutablePair<T, Boolean>>, ResponseFormat> createElementTypes(ToscaTypeImportData toscaTypeImportData, Function<String, Either<List<T>, ActionStatus>> elementTypeFromYmlCreater,
                                                                                                                          Function<List<T>, Either<List<ImmutablePair<T, Boolean>>, ResponseFormat>> elementTypeDaoCreater) {
        Either<List<T>, ActionStatus> elementTypes = elementTypeFromYmlCreater.apply(toscaTypeImportData.getToscaTypesYml());
        return elementTypes
                .right()
                .map(err -> componentsUtils.getResponseFormat(err, ""))
                .left()
                .map(toscaTypes -> enrichTypesWithNonToscaMetadata(toscaTypes, toscaTypeImportData.getToscaTypeMetadata()))
                .left()
                .bind(elementTypeDaoCreater::apply);
    }

    public <T extends ToscaDataDefinition> List<ImmutablePair<T, Boolean>> createElementTypes(String toscaTypesYml,
                                                                                              BiFunction<String, Map<String, Object>, T> createApi,
                                                                                              TypeOperations<T> typeOperations) {
        Map<String, Object> fieldMap = convertToFieldMap(toscaTypesYml);
        if (fieldMap==null) {
            throw new ComponentException(ActionStatus.INVALID_YAML_FILE);
        }
        List<T> elementTypes = createTypesFromToscaJsonMap(createApi, fieldMap);
        return createTypesByDao(elementTypes, typeOperations);
    }

    private <T extends ToscaTypeDataDefinition> List<T> enrichTypesWithNonToscaMetadata(List<T> toscaTypes, Map<String, ToscaTypeMetadata> toscaTypeMetadata) {
        return toscaTypes.stream()
                  .map(toscaType -> setNonToscaMetaDataOnType(toscaTypeMetadata, toscaType))
                  .collect(toList());
    }

    private <T extends ToscaTypeDataDefinition> T setNonToscaMetaDataOnType(Map<String, ToscaTypeMetadata> toscaTypeMetadata, T toscaTypeDefinition) {
        String toscaType = toscaTypeDefinition.getType();
        ToscaTypeMetadata typeMetaData = toscaTypeMetadata.get(toscaType);
        if (typeMetaData == null) {
            log.debug("failing while trying to associate metadata for type {}. type not exist", toscaType);
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
        toscaTypeDefinition.setIcon(typeMetaData.getIcon());
        toscaTypeDefinition.setName(typeMetaData.getDisplayName());
        return toscaTypeDefinition;
    }

    public <T> Either<List<ImmutablePair<T, Boolean>>, ResponseFormat> createElementTypes(String elementTypesYml, Function<String, Either<List<T>, ActionStatus>> elementTypeFromYmlCreater,
            Function<List<T>, Either<List<ImmutablePair<T, Boolean>>, ResponseFormat>> elementTypeDaoCreater, ElementTypeEnum elementTypeEnum) {

        Either<List<T>, ActionStatus> elementTypes = elementTypeFromYmlCreater.apply(elementTypesYml);
        if (elementTypes.isRight()) {
            ActionStatus status = elementTypes.right().value();
            ResponseFormat responseFormat = getResponseFormatForElementType(status, elementTypeEnum, null);
            return Either.right(responseFormat);
        }
        return elementTypeDaoCreater.apply(elementTypes.left().value());

    }
}
