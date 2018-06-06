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

package org.openecomp.sdc.be.model.jsontitan.operations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

@org.springframework.stereotype.Component("forwarding-paths-operations")
public class ForwardingPathOperation extends BaseOperation {
    private static Logger logger = LoggerFactory.getLogger(ForwardingPathOperation.class.getName());


    public Either<Set<String>, StorageOperationStatus> deleteForwardingPath(Service service, Set<String> forwardingPathsToDelete) {
        Either<Set<String>, StorageOperationStatus> result = null;
        Either<GraphVertex, TitanOperationStatus> getComponentVertex;
        StorageOperationStatus status = null;

        if (result == null) {
            getComponentVertex = titanDao.getVertexById(service.getUniqueId(), JsonParseFlagEnum.NoParse);
            if (getComponentVertex.isRight()) {
                result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getComponentVertex.right().value()));
            }
        }
        if (result == null) {

            status = deleteToscaDataElements(service.getUniqueId(), EdgeLabelEnum.FORWARDING_PATH,new ArrayList<>(forwardingPathsToDelete));

            if (status != StorageOperationStatus.OK) {
                result = Either.right(status);
            }
        }

        if (result == null) {
            result = Either.left(forwardingPathsToDelete);
        }
        return result;
    }

    public Either<ForwardingPathDataDefinition, StorageOperationStatus> addForwardingPath(String serviceId, ForwardingPathDataDefinition currentPath) {
        return addOrUpdateForwardingPath(false, serviceId, currentPath);
    }

    public Either<ForwardingPathDataDefinition, StorageOperationStatus> updateForwardingPath(String serviceId, ForwardingPathDataDefinition currentPath) {
        return addOrUpdateForwardingPath(true, serviceId, currentPath);
    }

    private Either<ForwardingPathDataDefinition, StorageOperationStatus> addOrUpdateForwardingPath(boolean isUpdateAction, String serviceId, ForwardingPathDataDefinition currentPath) {

        StorageOperationStatus statusRes;
        Either<GraphVertex, TitanOperationStatus> getToscaElementRes;

        getToscaElementRes = titanDao.getVertexById(serviceId, JsonParseFlagEnum.NoParse);
        if (getToscaElementRes.isRight()) {
            TitanOperationStatus status = getToscaElementRes.right().value();
            CommonUtility.addRecordToLog(logger, CommonUtility.LogLevelEnum.DEBUG, "Failed to get tosca element {} upon adding the properties. Status is {}. ", serviceId, status);
            statusRes = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
            return Either.right(statusRes);
        }
        GraphVertex serviceVertex = getToscaElementRes.left().value();
        if (!isUpdateAction){
            currentPath.setUniqueId(UUID.randomUUID().toString());
        }
        statusRes = performUpdateToscaAction(isUpdateAction, serviceVertex, Arrays.asList(currentPath), JsonPresentationFields.FORWARDING_PATH);
        {
            if (!statusRes.equals(StorageOperationStatus.OK)) {
                logger.error("Failed to find the parent capability of capability type {}. status is {}", serviceId, statusRes);
                return Either.right(statusRes);
            }
            return Either.left(currentPath);
        }

    }


    private StorageOperationStatus performUpdateToscaAction(boolean isUpdate, GraphVertex graphVertex, List<ForwardingPathDataDefinition> toscaDataList, JsonPresentationFields mapKeyField) {
        if (isUpdate) {
            return updateToscaDataOfToscaElement(graphVertex, EdgeLabelEnum.FORWARDING_PATH, VertexTypeEnum.FORWARDING_PATH, toscaDataList, JsonPresentationFields.UNIQUE_ID);
        } else {
            return addToscaDataToToscaElement(graphVertex, EdgeLabelEnum.FORWARDING_PATH, VertexTypeEnum.FORWARDING_PATH, toscaDataList, JsonPresentationFields.UNIQUE_ID);
        }
    }

}



