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

package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import fj.data.Either;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.*;

@org.springframework.stereotype.Component("forwarding-paths-operations")
public class ForwardingPathOperation extends BaseOperation {
    private static final Logger log = Logger.getLogger(ForwardingPathOperation.class.getName());


    public Either<Set<String>, StorageOperationStatus> deleteForwardingPath(Service service, Set<String> forwardingPathsToDelete) {
        Either<Set<String>, StorageOperationStatus> result = null;
        Either<GraphVertex, JanusGraphOperationStatus> getComponentVertex;
        StorageOperationStatus status = null;

        if (result == null) {
            getComponentVertex = janusGraphDao
                .getVertexById(service.getUniqueId(), JsonParseFlagEnum.NoParse);
            if (getComponentVertex.isRight()) {
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getComponentVertex.right().value()));
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
        Either<GraphVertex, JanusGraphOperationStatus> getToscaElementRes;

        getToscaElementRes = janusGraphDao.getVertexById(serviceId, JsonParseFlagEnum.NoParse);
        if (getToscaElementRes.isRight()) {
            JanusGraphOperationStatus status = getToscaElementRes.right().value();
            CommonUtility.addRecordToLog(log, CommonUtility.LogLevelEnum.DEBUG, "Failed to get tosca element {} upon adding the properties. Status is {}. ", serviceId, status);
            statusRes = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
            return Either.right(statusRes);
        }
        GraphVertex serviceVertex = getToscaElementRes.left().value();
        if (!isUpdateAction){
            currentPath.setUniqueId(UUID.randomUUID().toString());
        }
        statusRes = performUpdateToscaAction(isUpdateAction, serviceVertex, Arrays.asList(currentPath), JsonPresentationFields.FORWARDING_PATH);
        {
            if (!statusRes.equals(StorageOperationStatus.OK)) {
                log.error("Failed to find the parent capability of capability type {}. status is {}", serviceId, statusRes);
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



