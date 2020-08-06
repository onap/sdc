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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.components.impl.exceptions.DataTypeNotProvidedException;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

@org.springframework.stereotype.Component("dataTypeBusinessLogic")
public class DataTypeBusinessLogic {

    private final PropertyOperation propertyOperation;
    private final ToscaOperationFacade toscaOperationFacade;

    @Autowired
    public DataTypeBusinessLogic(final PropertyOperation propertyOperation,
                                 final ToscaOperationFacade toscaOperationFacade) {
        this.propertyOperation = propertyOperation;
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
     * Adds data types to the component, resolving data types hierarchy.
     *
     * @param component the component to add the given data types
     * @param dataTypes the data types to add
     * @throws DataTypeNotProvidedException when a data type does not exist in the database and was not provided
     */
    public void addToComponent(final Component component,
                               final Map<String, DataTypeDefinition> dataTypes) throws DataTypeNotProvidedException {
        if (MapUtils.isEmpty(dataTypes)) {
            return;
        }
        parseToDataTypeDefinitionList(dataTypes).forEach(component::addToDataTypes);
    }

    private List<DataTypeDefinition> parseToDataTypeDefinitionList(final Map<String, DataTypeDefinition> dataTypeMap)
        throws DataTypeNotProvidedException {

        final List<DataTypeDefinition> dataTypeDefinitionList = new ArrayList<>();
        dataTypeMap.forEach((key, dataTypeDefinition) -> {
            dataTypeDefinition.setName(key);
            dataTypeDefinitionList.add(dataTypeDefinition);
        });

        for (final DataTypeDefinition dataTypeDefinition : dataTypeDefinitionList) {
            loadParentHierarchy(dataTypeDefinition, dataTypeDefinitionList);
        }

        return dataTypeDefinitionList;
    }

    private void loadParentHierarchy(final DataTypeDefinition dataTypeDefinition,
                                     final List<DataTypeDefinition> componentDataTypeList)
        throws DataTypeNotProvidedException {
        if (dataTypeDefinition.getDerivedFrom() != null) {
            return;
        }
        if (dataTypeDefinition.getDerivedFromName() == null) {
            return;
        }

        DataTypeDefinition parentDataType =
            propertyOperation.findDataTypeByName(dataTypeDefinition.getDerivedFromName()).orElse(null);
        if (parentDataType != null) {
            dataTypeDefinition.setDerivedFrom(parentDataType);
            return;
        }

        parentDataType = componentDataTypeList.stream()
            .filter(dataTypeDefinition1 ->
                dataTypeDefinition1.getName().equals(dataTypeDefinition.getDerivedFromName()))
            .findFirst().orElse(null);
        if (parentDataType == null) {
            final String errorMsg =
                String.format("Data type '%s' was not provided", dataTypeDefinition.getDerivedFromName());
            throw new DataTypeNotProvidedException(errorMsg);
        }
        dataTypeDefinition.setDerivedFrom(parentDataType);
        loadParentHierarchy(dataTypeDefinition.getDerivedFrom(), componentDataTypeList);
    }

}
