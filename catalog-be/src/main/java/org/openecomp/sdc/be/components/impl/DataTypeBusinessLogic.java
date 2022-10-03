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
import java.util.Optional;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

@org.springframework.stereotype.Component("dataTypeBusinessLogic")
public class DataTypeBusinessLogic extends BaseBusinessLogic {

    final DataTypeImportManager dataTypeImportManager;

    @Autowired
    public DataTypeBusinessLogic(IElementOperation elementDao, IGroupOperation groupOperation, IGroupInstanceOperation groupInstanceOperation,
                                 IGroupTypeOperation groupTypeOperation, InterfaceOperation interfaceOperation,
                                 InterfaceLifecycleOperation interfaceLifecycleTypeOperation, ArtifactsOperations artifactToscaOperation, DataTypeImportManager dataTypeImportManager) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation,
            artifactToscaOperation);
        this.dataTypeImportManager = dataTypeImportManager;
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
        Either<? extends Component, StorageOperationStatus> componentResult = toscaOperationFacade.getToscaElement(componentId, filter);
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
        return findResult.<Either<DataTypeDefinition, StorageOperationStatus>>map(Either::left)
            .orElseGet(() -> Either.right(StorageOperationStatus.NOT_FOUND));
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
        Either<? extends Component, StorageOperationStatus> componentResult = toscaOperationFacade.getToscaElement(componentId, filter);
        if (componentResult.isRight()) {
            // not exists
            return Either.right(componentResult.right().value());
        }
        return deletePrivateDataType(componentResult.left().value(), dataTypeName);
    }

    /**
     * Delete a data type from the Component.
     *
     * @param component    Component object which has data types. needs to be fetched with componentParametersView.setIgnoreDataType(false)
     * @param dataTypeName Data type name to be deleted
     * @return deleted data type
     */
    public Either<DataTypeDefinition, StorageOperationStatus> deletePrivateDataType(Component component, String dataTypeName) {
        // check the specified data type exists
        List<DataTypeDefinition> dataTypes = component.getDataTypes();
        if (CollectionUtils.isEmpty(dataTypes)) {
            return Either.right(StorageOperationStatus.NOT_FOUND);
        }
        Optional<DataTypeDefinition> dataTypeResult = dataTypes.stream().filter(e -> e.getName().equals(dataTypeName)).findFirst();
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
     * Creates given data types. The data types must be provided in a yaml format, where each entry is one data type object, for
     * example:
     * <pre>
     * tosca.datatypes.TimeInterval:
     *   derived_from: tosca.datatypes.Root
     *   [...]
     *
     * tosca.datatypes.network.NetworkInfo:
     *   derived_from: tosca.datatypes.Root
     *   [...]
     * </pre>
     *
     * @param dataTypesYaml the data types to create in yaml format. It can contain multiple data types entries.
     * @param model Model name to associate with data type
     * @param includeToModelDefaultImports Add data type entry to default imports for model
     */
    public void createDataTypeFromYaml(final String dataTypesYaml, final String model, final boolean includeToModelDefaultImports) {
        dataTypeImportManager.createDataTypes(dataTypesYaml, model, includeToModelDefaultImports);
    }
}
