/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */
package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import fj.data.Either;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;

@org.springframework.stereotype.Component("interfaces-operation")
public class InterfaceOperation extends BaseOperation {

    public Either<List<InterfaceDefinition>, StorageOperationStatus> addInterfaces(String componentId,
                                                                                   List<InterfaceDefinition> interfaceDefinitions) {
        return addOrUpdateInterfaces(false, componentId, interfaceDefinitions);
    }

    private Either<List<InterfaceDefinition>, StorageOperationStatus> addOrUpdateInterfaces(boolean isUpdateAction, String componentId,
                                                                                            List<InterfaceDefinition> interfaceDefinitions) {
        return addOrUpdateInterfaces(componentId, VertexTypeEnum.INTERFACE, EdgeLabelEnum.INTERFACE, interfaceDefinitions, isUpdateAction);
    }

    private Either<List<InterfaceDefinition>, StorageOperationStatus> addOrUpdateInterfaces(final String componentId,
                                                                                            final VertexTypeEnum vertexTypeEnum,
                                                                                            final EdgeLabelEnum edgeLabelEnum,
                                                                                            final List<InterfaceDefinition> interfaceDefinitions,
                                                                                            final boolean isUpdateAction) {
        final List<ToscaDataDefinition> interfaceDataDefinitions = interfaceDefinitions.stream()
                .map(InterfaceDataDefinition::new)
                .collect(Collectors.toList());
        final StorageOperationStatus status = performUpdateToscaAction(isUpdateAction, componentId, interfaceDataDefinitions, edgeLabelEnum,
            vertexTypeEnum);
        if (!status.equals(StorageOperationStatus.OK)) {
            return Either.right(status);
        }
        return Either.left(interfaceDefinitions);
    }

    private StorageOperationStatus performUpdateToscaAction(boolean isUpdate, String componentId, List<ToscaDataDefinition> toscaDataList,
                                                            EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel) {
        if (isUpdate) {
            return updateToscaDataOfToscaElement(componentId, edgeLabel, vertexLabel, toscaDataList, JsonPresentationFields.UNIQUE_ID);
        } else {
            return addToscaDataToToscaElement(componentId, edgeLabel, vertexLabel, toscaDataList, JsonPresentationFields.UNIQUE_ID);
        }
    }

    public Either<List<InterfaceDefinition>, StorageOperationStatus> updateInterfaces(String componentId,
                                                                                      List<InterfaceDefinition> interfaceDefinitions) {
        return addOrUpdateInterfaces(true, componentId, interfaceDefinitions);
    }

    public Either<List<InterfaceDefinition>, StorageOperationStatus> updateVfcInterfaces(String componentId,
                                                                                      List<InterfaceDefinition> interfaceDefinitions) {
        return addOrUpdateInterfaces(componentId, VertexTypeEnum.INTERFACE_ARTIFACTS, EdgeLabelEnum.INTERFACE_ARTIFACTS,
            interfaceDefinitions, true);
    }

    public Either<String, StorageOperationStatus> deleteInterface(String componentId, String interfacesToDelete) {
        return deleteInterface(componentId, interfacesToDelete, EdgeLabelEnum.INTERFACE, VertexTypeEnum.INTERFACE);
    }

    public Either<String, StorageOperationStatus> deleteVfcInterface(String componentId, String interfacesToDelete) {
        return deleteInterface(componentId, interfacesToDelete, EdgeLabelEnum.INTERFACE_ARTIFACTS, VertexTypeEnum.INTERFACE_ARTIFACTS);
    }

    private Either<String, StorageOperationStatus> deleteInterface(final String componentId, final String interfacesToDelete,
                                                                  final EdgeLabelEnum edgeLabel, final VertexTypeEnum vertexType) {
        StorageOperationStatus statusRes = deleteToscaDataElements(componentId, edgeLabel, Collections.singletonList(interfacesToDelete));
        if (!statusRes.equals(StorageOperationStatus.OK)) {
            return Either.right(statusRes);
        }
        Either<Map<String, InterfaceDataDefinition>, JanusGraphOperationStatus> interfaceEither = getDataFromGraph(componentId, edgeLabel);
        if (interfaceEither.isRight()) {
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(interfaceEither.right().value()));
        }
        final Map<String, InterfaceDataDefinition> interfaceDataDefinitionMap = interfaceEither.left().value();
        if (MapUtils.isEmpty(interfaceDataDefinitionMap)) {
            statusRes = removeToscaData(componentId, edgeLabel, vertexType);
            if (!statusRes.equals(StorageOperationStatus.OK)) {
                return Either.right(statusRes);
            }
        }
        return Either.left(interfacesToDelete);
    }
}
