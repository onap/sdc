/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.model.operations.impl;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.elements.DataTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.exception.supplier.DataTypeOperationExceptionSupplier;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.dto.PropertyDefinitionDto;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.OperationException;
import org.openecomp.sdc.be.model.mapper.PropertyDefinitionDtoMapper;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.DataTypeData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("dataType-operation")
public class DataTypeOperation extends AbstractOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataTypeOperation.class);

    private ModelOperation modelOperation;
    private PropertyOperation propertyOperation;

    @Autowired
    public DataTypeOperation(final HealingJanusGraphGenericDao janusGraphGenericDao) {
        this.janusGraphGenericDao = janusGraphGenericDao;
    }

    //circular dependency ModelOperation->ModelElementOperation->DataTypeOperation
    @Autowired
    public void setModelOperation(final ModelOperation modelOperation) {
        this.modelOperation = modelOperation;
    }

    @Autowired
    public void setPropertyOperation(PropertyOperation propertyOperation) {
        this.propertyOperation = propertyOperation;
    }

    public List<DataTypeData> getAllDataTypeNodes() {
        final List<DataTypeData> dataTypesFound = new ArrayList<>();
        final Either<List<DataTypeData>, JanusGraphOperationStatus> getAllDataTypesWithNullModel =
            janusGraphGenericDao.getByCriteria(NodeTypeEnum.DataType, null, DataTypeData.class);

        final var dataTypesValidated = validateDataType(getAllDataTypesWithNullModel, null);
        if (CollectionUtils.isNotEmpty(dataTypesValidated)) {
            dataTypesFound.addAll(dataTypesValidated);
        }

        final List<DataTypeData> allDataTypeNodesWithModel = getAllDataTypesWithModel();
        if (CollectionUtils.isNotEmpty(allDataTypeNodesWithModel)) {
            dataTypesFound.addAll(allDataTypeNodesWithModel);
        }
        return dataTypesFound;
    }

    public Map<String, List<String>> getAllDataTypeUidsToModels() {
        final Map<String, List<String>> dataTypesFound = new HashMap<>();
        final Either<List<DataTypeData>, JanusGraphOperationStatus> getAllDataTypesWithNullModel =
            janusGraphGenericDao.getByCriteria(NodeTypeEnum.DataType, null, DataTypeData.class);

        final var dataTypesValidated = validateDataType(getAllDataTypesWithNullModel, null);

        for (DataTypeData dataType : dataTypesValidated) {
            if (!dataTypesFound.containsKey(dataType.getUniqueId())) {
                dataTypesFound.put(dataType.getUniqueId(), new ArrayList<>());
            }
            dataTypesFound.get(dataType.getUniqueId()).add(null);
        }

        modelOperation.findAllModels()
            .forEach(model -> {
                for (DataTypeData dataType : getAllDataTypesWithModel(model.getName())) {
                    if (!dataTypesFound.containsKey(dataType.getUniqueId())) {
                        dataTypesFound.put(dataType.getUniqueId(), new ArrayList<>());
                    }
                    dataTypesFound.get(dataType.getUniqueId()).add(model.getName());
                }
            });
        return dataTypesFound;
    }

    private List<DataTypeData> getAllDataTypesWithModel(final String modelName) {
        final Either<List<DataTypeData>, JanusGraphOperationStatus> getAllDataTypesByModel = janusGraphGenericDao
            .getByCriteriaForModel(NodeTypeEnum.DataType, null, modelName, DataTypeData.class);
        return validateDataType(getAllDataTypesByModel, modelName);
    }

    private List<DataTypeData> getAllDataTypesWithModel() {
        final List<DataTypeData> dataTypesWithModel = new ArrayList<>();
        modelOperation.findAllModels()
            .forEach(model -> {
                final var modelName = model.getName();
                final Either<List<DataTypeData>, JanusGraphOperationStatus> getAllDataTypesByModel = janusGraphGenericDao
                    .getByCriteriaForModel(NodeTypeEnum.DataType, null, modelName, DataTypeData.class);
                final var dataTypesValidated = validateDataType(getAllDataTypesByModel, modelName);
                dataTypesWithModel.addAll(dataTypesValidated);
            });
        return dataTypesWithModel;
    }

    private List<DataTypeData> validateDataType(final Either<List<DataTypeData>, JanusGraphOperationStatus> getDataTypes, final String modelName) {
        if (getDataTypes.isRight() && getDataTypes.right().value() == JanusGraphOperationStatus.NOT_FOUND) {
            return Collections.emptyList();
        }
        if (getDataTypes.isRight()) {
            final var status = getDataTypes.right().value();
            if (LOGGER.isErrorEnabled()) {
                final var errorMsg = String.format("Failed to fetch data types from database with model %s. Status is %s", modelName, status);
                LOGGER.error(String.valueOf(EcompLoggerErrorCode.UNKNOWN_ERROR), DataTypeOperation.class.getName(), errorMsg);
                BeEcompErrorManager.getInstance().logInternalConnectionError(DataTypeOperation.class.getName(), errorMsg, ErrorSeverity.ERROR);
            }
            return Collections.emptyList();
        }
        return getDataTypes.left().value();
    }

    public void deleteDataTypesByModelId(final String modelId) {
        final JanusGraph janusGraph = janusGraphGenericDao.getJanusGraph();
        final GraphTraversalSource traversal = janusGraph.traversal();
        final List<Vertex> dataTypeList = traversal.V()
            .has(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), modelId)
            .out(GraphEdgeLabels.MODEL_ELEMENT.getProperty())
            .has(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.DataType.getName())
            .toList();
        dataTypeList.forEach(dataTypeVertex -> {
            traversal.V(dataTypeVertex).out(GraphEdgeLabels.PROPERTY.getProperty()).drop().iterate();
            dataTypeVertex.remove();
        });
    }

    public Optional<DataTypeDataDefinition> getDataTypeByUid(final String uniqueId) {
        final Either<DataTypeData, JanusGraphOperationStatus> dataTypeEither = janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.DataType), uniqueId, DataTypeData.class);
        if (dataTypeEither.isRight()) {
            if (JanusGraphOperationStatus.NOT_FOUND.equals(dataTypeEither.right().value())) {
                return Optional.empty();
            }
            final StorageOperationStatus storageOperationStatus
                = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(dataTypeEither.right().value());
            LOGGER.warn("Failed to fetch data type '{}' from JanusGraph. Status is: {}", uniqueId, storageOperationStatus);
            throw new OperationException(ActionStatus.GENERAL_ERROR,
                String.format("Failed to fetch data type '%s' from JanusGraph. Status is: %s", uniqueId, storageOperationStatus));
        }
        return Optional.of(dataTypeEither.left().value().getDataTypeDataDefinition());
    }

    public List<PropertyDefinition> findAllProperties(final String uniqueId) {
        final Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> propertiesEither =
            propertyOperation.findPropertiesOfNode(NodeTypeEnum.DataType, uniqueId);
        if (propertiesEither.isRight()) {
            final JanusGraphOperationStatus status = propertiesEither.right().value();
            if (status == JanusGraphOperationStatus.NOT_FOUND) {
                return List.of();
            }
            LOGGER.error("Could not retrieve data type '{}' properties. JanusGraphOperationStatus: '{}'", uniqueId, status);

            throw DataTypeOperationExceptionSupplier.unexpectedErrorWhileFetchingProperties(uniqueId).get();
        }
        final Map<String, PropertyDefinition> propertyMap = propertiesEither.left().value();
        if (MapUtils.isEmpty(propertyMap)) {
            return List.of();
        }
        final List<PropertyDefinition> propertyDefinitions = new ArrayList<>(propertyMap.values());
        propertyDefinitions.sort(Comparator.comparing(PropertyDefinition::getName));
        return propertyDefinitions;
    }

    public Optional<DataTypeDefinition> handleDataTypeDownloadRequestById(final String dataTypeId) {
        if (StringUtils.isNotEmpty(dataTypeId)) {
            Optional<DataTypeDataDefinition> dataTypeDataDefinition = getDataTypeByUid(dataTypeId);
            if (dataTypeDataDefinition.isPresent()) {
                DataTypeDefinition dataTypeDefinition = new DataTypeDefinition(dataTypeDataDefinition.get());
                dataTypeDefinition.setProperties(findAllProperties(dataTypeId));
                return Optional.of(dataTypeDefinition);
            }
        }
        return Optional.empty();
    }

    public PropertyDefinitionDto createProperty(final String dataTypeId, final PropertyDefinitionDto propertyDefinitionDto) {
        final String propertyName = propertyDefinitionDto.getName();
        LOGGER.debug("Adding property '{}' to data type '{}'.", propertyName, dataTypeId);

        getDataTypeByUid(dataTypeId).orElseThrow(DataTypeOperationExceptionSupplier.dataTypeNotFound(dataTypeId));

        final Either<PropertyData, JanusGraphOperationStatus> resultEither =
            propertyOperation.addPropertyToNodeType(propertyName, PropertyDefinitionDtoMapper.mapTo(propertyDefinitionDto),
                NodeTypeEnum.DataType, dataTypeId, false);
        if (resultEither.isRight()) {
            final JanusGraphOperationStatus status = resultEither.right().value();
            LOGGER.debug("Could not create property '{}' on data type '{}'. JanusGraph status is '{}'", propertyName, dataTypeId, status);
            if (status == JanusGraphOperationStatus.JANUSGRAPH_SCHEMA_VIOLATION) {
                throw DataTypeOperationExceptionSupplier.dataTypePropertyAlreadyExists(dataTypeId, propertyName).get();
            }
            LOGGER.error("Could not create property '{}' on data type '{}'. JanusGraph status is '{}'", propertyName, dataTypeId, status);
            throw DataTypeOperationExceptionSupplier.unexpectedErrorWhileCreatingProperty(dataTypeId, propertyName).get();
        }
        LOGGER.debug("Property '{}' was added to data type '{}'.", propertyName, dataTypeId);
        final PropertyData propertyData = resultEither.left().value();
        final PropertyDataDefinition propertyDataDefinition = propertyData.getPropertyDataDefinition();
        propertyDataDefinition.setName(propertyName);
        return PropertyDefinitionDtoMapper.mapFrom(propertyDataDefinition);
    }

}
