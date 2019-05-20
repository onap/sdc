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

    private Either<List<InterfaceDefinition>, StorageOperationStatus> addOrUpdateInterfaces(boolean isUpdateAction,
            String componentId, List<InterfaceDefinition> interfaceDefinitions) {

        List<ToscaDataDefinition> interfaceDataDefinitions =
                interfaceDefinitions.stream().map(InterfaceDataDefinition::new).collect(Collectors.toList());
        StorageOperationStatus statusRes =
                performUpdateToscaAction(isUpdateAction, componentId, interfaceDataDefinitions, EdgeLabelEnum.INTERFACE,
                        VertexTypeEnum.INTERFACE);
        if (!statusRes.equals(StorageOperationStatus.OK)) {
            return Either.right(statusRes);
        }
        return Either.left(interfaceDefinitions);
    }

    private StorageOperationStatus performUpdateToscaAction(boolean isUpdate, String componentId,
            List<ToscaDataDefinition> toscaDataList, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel) {
        if (isUpdate) {
            return updateToscaDataOfToscaElement(componentId, edgeLabel, vertexLabel, toscaDataList,
                    JsonPresentationFields.UNIQUE_ID);
        } else {
            return addToscaDataToToscaElement(componentId, edgeLabel, vertexLabel, toscaDataList,
                    JsonPresentationFields.UNIQUE_ID);
        }
    }

    public Either<List<InterfaceDefinition>, StorageOperationStatus> updateInterfaces(String componentId,
            List<InterfaceDefinition> interfaceDefinitions) {
        return addOrUpdateInterfaces(true, componentId, interfaceDefinitions);
    }

    public Either<String, StorageOperationStatus> deleteInterface(String componentId, String interfacesToDelete) {

        StorageOperationStatus statusRes = deleteToscaDataElements(componentId, EdgeLabelEnum.INTERFACE,
                Collections.singletonList(interfacesToDelete));
        if (!statusRes.equals(StorageOperationStatus.OK)) {
            return Either.right(statusRes);
        }

        Either<Map<String, InterfaceDataDefinition>, JanusGraphOperationStatus> componentEither =
                getDataFromGraph(componentId, EdgeLabelEnum.INTERFACE);
        if (componentEither.isRight()) {
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(componentEither.right().value()));
        }

        Map<String, InterfaceDataDefinition> interfaceDataDefinitionMap = componentEither.left().value();
        if (MapUtils.isEmpty(interfaceDataDefinitionMap)) {
            statusRes = removeToscaData(componentId, EdgeLabelEnum.INTERFACE, VertexTypeEnum.INTERFACE);
            if (!statusRes.equals(StorageOperationStatus.OK)) {
                return Either.right(statusRes);
            }
        }

        return Either.left(interfacesToDelete);
    }

}