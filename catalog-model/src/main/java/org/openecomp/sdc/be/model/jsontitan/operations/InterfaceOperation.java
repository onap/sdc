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

package org.openecomp.sdc.be.model.jsontitan.operations;

import fj.data.Either;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.*;

@org.springframework.stereotype.Component("interfaces-operation")
public class InterfaceOperation extends BaseOperation {

    private static final Logger logger = Logger.getLogger(InterfaceOperation.class.getName());


    public Either<Set<String>, StorageOperationStatus> deleteInterface(Resource resource,
        Set<String> interfacesToDelete) {
        Either<Set<String>, StorageOperationStatus> result = null;
        Either<GraphVertex, TitanOperationStatus> getComponentVertex;
        StorageOperationStatus status = null;

        if (result == null) {
            getComponentVertex = titanDao.getVertexById(resource.getUniqueId(), JsonParseFlagEnum.NoParse);
            if (getComponentVertex.isRight()) {
                result = Either
                    .right(DaoStatusConverter.convertTitanStatusToStorageStatus(getComponentVertex.right().value()));
            }
        }
        if (result == null) {

            status = deleteToscaDataElements(resource.getUniqueId(), EdgeLabelEnum.INTERFACE_ARTIFACTS,
                new ArrayList<>(interfacesToDelete));

            if (status != StorageOperationStatus.OK) {
                result = Either.right(status);
            }
        }

        if (result == null) {
            result = Either.left(interfacesToDelete);
        }
        return result;
    }

    public Either<InterfaceDefinition, StorageOperationStatus> addInterface(String resourceId,
        InterfaceDefinition interfaceDefinition) {
        return addOrUpdateInterface(false, resourceId, interfaceDefinition);
    }

    public Either<InterfaceDefinition, StorageOperationStatus> updateInterface(String resourceId,
        InterfaceDefinition interfaceDefinition) {
        return addOrUpdateInterface(true, resourceId, interfaceDefinition);
    }

    private Either<InterfaceDefinition, StorageOperationStatus> addOrUpdateInterface(
        boolean isUpdateAction, String resourceId, InterfaceDefinition interfaceDefinition) {

        StorageOperationStatus statusRes;
        Either<GraphVertex, TitanOperationStatus> getToscaElementRes;

        getToscaElementRes = titanDao.getVertexById(resourceId, JsonParseFlagEnum.NoParse);
        if (getToscaElementRes.isRight()) {
            TitanOperationStatus status = getToscaElementRes.right().value();
            CommonUtility.addRecordToLog(logger, CommonUtility.LogLevelEnum.DEBUG,
                "Failed to get tosca element {} upon adding the properties. Status is {}. ", resourceId, status);
            statusRes = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
            return Either.right(statusRes);
        }
        GraphVertex resourceVertex = getToscaElementRes.left().value();
        if (!isUpdateAction) {
            interfaceDefinition.setUniqueId(UUID.randomUUID().toString());
        }
        statusRes = performUpdateToscaAction(isUpdateAction, resourceVertex, Arrays.asList(interfaceDefinition),
            JsonPresentationFields.INTERFACE);
        if (!statusRes.equals(StorageOperationStatus.OK)) {
            logger.error("Failed to find the parent capability of capability type {}. status is {}", resourceId,
                statusRes);
            return Either.right(statusRes);
        }
        return Either.left(interfaceDefinition);
    }

    private StorageOperationStatus performUpdateToscaAction(boolean isUpdate, GraphVertex graphVertex,
        List<InterfaceDefinition> toscaDataList, JsonPresentationFields mapKeyField) {
        if (isUpdate) {
            return updateToscaDataOfToscaElement(graphVertex, EdgeLabelEnum.INTERFACE_ARTIFACTS,
                VertexTypeEnum.INTERFACE_ARTIFACTS, toscaDataList, JsonPresentationFields.UNIQUE_ID);
        } else {
            return addToscaDataToToscaElement(graphVertex, EdgeLabelEnum.INTERFACE_ARTIFACTS,
                VertexTypeEnum.INTERFACE_ARTIFACTS, toscaDataList, JsonPresentationFields.UNIQUE_ID);
        }
    }


}


