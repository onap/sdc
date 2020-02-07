/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Fujitsu Limited. All rights reserved.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

@org.springframework.stereotype.Component("dataTypeBusinessLogic")
public class DataTypeBusinessLogic {

    private final PropertyOperation propertyOperation;
    private final ApplicationDataTypeCache applicationDataTypeCache;
    private final ToscaOperationFacade toscaOperationFacade;

    @Autowired
    public DataTypeBusinessLogic(final PropertyOperation propertyOperation,
                                 final ApplicationDataTypeCache applicationDataTypeCache,
                                 final ToscaOperationFacade toscaOperationFacade) {
        this.propertyOperation = propertyOperation;
        this.applicationDataTypeCache = applicationDataTypeCache;
        this.toscaOperationFacade = toscaOperationFacade;
    }

    /**
     * Get a list of data types that the Component has.
     *
     * @param componentId Unique ID of the Component
     * @return list of data types
     */
    public Either<List<DataTypeDefinition>, StorageOperationStatus> getPrivateDataTypes(String componentId) {
        ComponentParametersView filter = new ComponentParametersView();
        filter.disableAll();
        filter.setIgnoreDataType(false);

        // Get Component object
        Either<? extends Component, StorageOperationStatus> componentResult =
                toscaOperationFacade.getToscaElement(componentId, filter);
        if (componentResult.isRight()) {
            return Either.right(componentResult.right().value());
        }
        Component component = componentResult.left().value();

        List<DataTypeDefinition> dataTypesToReturn = component.getDataTypes();
        if (dataTypesToReturn == null) {
            // this means there is no DATA_TYPES graph vertex.
            // in this case, returns empty list.
            dataTypesToReturn = new ArrayList<>();
        }

        return Either.left(dataTypesToReturn);
    }

    /**
     * Get a data type in a Component
     *
     * @param componentId  Unique ID of the Component
     * @param dataTypeName Data type name
     * @return found data type
     */
    public Either<DataTypeDefinition, StorageOperationStatus> getPrivateDataType(String componentId, String dataTypeName) {
        Either<List<DataTypeDefinition>, StorageOperationStatus> dataTypesResult = this.getPrivateDataTypes(componentId);
        if (dataTypesResult.isRight()) {
            return Either.right(dataTypesResult.right().value());
        }
        List<DataTypeDefinition> dataTypes = dataTypesResult.left().value();
        Optional<DataTypeDefinition> findResult = dataTypes.stream().filter(e -> e.getName().equals(dataTypeName)).findAny();
        if (!findResult.isPresent()) {
            return Either.right(StorageOperationStatus.NOT_FOUND);
        }
        return Either.left(findResult.get());
    }

    /**
     * Delete a data type from the Component.
     *
     * @param componentId  Unique ID of the Component
     * @param dataTypeName Data type name to be deleted
     * @return deleted data type
     */
    public Either<DataTypeDefinition, StorageOperationStatus> deletePrivateDataType(String componentId, String dataTypeName) {
        ComponentParametersView filter = new ComponentParametersView();
        filter.disableAll();
        filter.setIgnoreDataType(false);

        // Get Component object
        Either<? extends Component, StorageOperationStatus> componentResult =
                toscaOperationFacade.getToscaElement(componentId, filter);
        if (componentResult.isRight()) {
            // not exists
            return Either.right(componentResult.right().value());
        }

        return deletePrivateDataType(componentResult.left().value(), dataTypeName);
    }

    /**
     * Delete a data type from the Component.
     *
     * @param component    Component object which has data types.
     *                     needs to be fetched with componentParametersView.setIgnoreDataType(false)
     * @param dataTypeName Data type name to be deleted
     * @return deleted data type
     */
    public Either<DataTypeDefinition, StorageOperationStatus> deletePrivateDataType(Component component, String dataTypeName) {
        // check the specified data type exists
        List<DataTypeDefinition> dataTypes = component.getDataTypes();
        if (CollectionUtils.isEmpty(dataTypes)) {
            return Either.right(StorageOperationStatus.NOT_FOUND);
        }
        Optional<DataTypeDefinition> dataTypeResult =
                dataTypes.stream().filter(e -> e.getName().equals(dataTypeName)).findFirst();
        if (!dataTypeResult.isPresent()) {
            return Either.right(StorageOperationStatus.NOT_FOUND);
        }

        // delete it
        StorageOperationStatus deleteResult = toscaOperationFacade.deleteDataTypeOfComponent(component, dataTypeName);
        if (deleteResult != StorageOperationStatus.OK) {
            return Either.right(deleteResult);
        }

        // return deleted data type if ok
        return Either.left(dataTypeResult.get());
    }

    /**
     * Associates a TOSCA data_type to a component.
     *
     * @param componentId the component id
     * @param dataTypeMap a map of data_type name and the data_type representation to associate to the component
     * @return the list of associated data_type
     */
    public List<DataTypeDefinition> addToComponent(final String componentId,
                                                   final Map<String, DataTypeDefinition> dataTypeMap) {
        final List<DataTypeDefinition> addedDataTypeList = new ArrayList<>();
        final List<DataTypeDefinition> parsedDataTypeDefinitionList = parseToDataTypeDefinitionList(dataTypeMap);
        for (final DataTypeDefinition dataTypeDefinition : parsedDataTypeDefinitionList) {
            addedDataTypeList.add(addToComponent(componentId, dataTypeDefinition));
        }

        return addedDataTypeList;
    }

    /**
     * Associates a data_type to a component. Creates the data_type if it does not exists.
     *
     * @param componentId the component id
     * @param dataTypeDefinitionToAdd the data_type to add to the component
     * @return the associated data_type
     */
    public DataTypeDefinition addToComponent(final String componentId, final DataTypeDefinition dataTypeDefinitionToAdd) {
        final Either<Boolean, JanusGraphOperationStatus> definedInDataTypes = propertyOperation
            .isDefinedInDataTypes(dataTypeDefinitionToAdd.getName());
        if (definedInDataTypes.isRight() || !definedInDataTypes.left().value()) {
            create(dataTypeDefinitionToAdd);
        }
        final DataTypeDefinition dataTypeDefinition = propertyOperation.findDataTypeByName(dataTypeDefinitionToAdd.getName());

        final Map<String, DataTypeDefinition> dataTypesMap = new HashMap<>();
        dataTypesMap.put(dataTypeDefinitionToAdd.getName(), dataTypeDefinition);

        final Either<List<DataTypeDefinition>, StorageOperationStatus> operationResult =
            toscaOperationFacade.addDataTypesToComponent(dataTypesMap, componentId);
        if (operationResult.isRight()) {
            final StorageOperationStatus storageOperationStatus = operationResult.right().value();
            throw new StorageException(storageOperationStatus);
        }

        return dataTypeDefinition;
    }

    /**
     * Creates a representation of the TOSCA data_type based on the DataTypeDefinition. Refreshes the application
     * data_type cache after successful creation.
     *
     * @param dataTypeDefinition the data type definition to be created
     * @return The created TOSCA data_type represented by the DataTypeDefinition
     */
    public DataTypeDefinition create(final DataTypeDefinition dataTypeDefinition) {
        dataTypeDefinition.setCreationTime(System.currentTimeMillis());
        final Either<DataTypeDefinition, StorageOperationStatus> operationResult =
            propertyOperation.addDataType(dataTypeDefinition);
        if (operationResult.isRight()) {
            throw new StorageException(operationResult.right().value());
        }

        applicationDataTypeCache.refresh();

        return operationResult.left().value();
    }

    private List<DataTypeDefinition> parseToDataTypeDefinitionList(final Map<String, DataTypeDefinition> dataTypes) {
        final List<DataTypeDefinition> dataTypeDefinitions = new ArrayList<>();
        dataTypes.forEach((key, dataTypeDefinition) -> {
            dataTypeDefinition.setName(key);
            dataTypeDefinitions.add(dataTypeDefinition);
        });

        return dataTypeDefinitions;
    }
}
