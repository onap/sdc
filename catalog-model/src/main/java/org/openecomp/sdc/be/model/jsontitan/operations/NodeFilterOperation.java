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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import fj.data.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterPropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.log.wrappers.Logger;

@org.springframework.stereotype.Component("service-filter-operations")
public class NodeFilterOperation extends BaseOperation {

    private static Logger logger = Logger.getLogger(NodeFilterOperation.class);

    public Either<Set<String>, StorageOperationStatus> deleteNodeFilters(Service service,
            Set<String> componentInstanceIds) {
        Either<GraphVertex, TitanOperationStatus> getComponentVertex;
        Either<GraphVertex, TitanOperationStatus> getNodeFilterVertex;
        StorageOperationStatus status;

        getComponentVertex = titanDao.getVertexById(service.getUniqueId(), JsonParseFlagEnum.NoParse);
        if (getComponentVertex.isRight()) {
            return Either.right(
                    DaoStatusConverter.convertTitanStatusToStorageStatus(getComponentVertex.right().value()));
        }

        getNodeFilterVertex =
                titanDao.getChildVertex(getComponentVertex.left().value(), EdgeLabelEnum.NODE_FILTER_TEMPLATE,
                        JsonParseFlagEnum.NoParse);
        if (getNodeFilterVertex.isLeft()) {
            status = deleteToscaDataElements(service.getUniqueId(), EdgeLabelEnum.NODE_FILTER_TEMPLATE,
                    new ArrayList<>(componentInstanceIds));
            if (status != StorageOperationStatus.OK) {
                return Either.right(status);
            }
        }

        return Either.left(componentInstanceIds);
    }


    public Either<String, StorageOperationStatus> deleteNodeFilter(Service service, String componentInstanceId) {
        final Either<Set<String>, StorageOperationStatus> listStorageOperationStatusEither =
                deleteNodeFilters(service, ImmutableSet.of(componentInstanceId));
        if (listStorageOperationStatusEither.isRight()) {
            return Either.right(listStorageOperationStatusEither.right().value());
        }
        return Either.left(componentInstanceId);
    }


    public Either<CINodeFilterDataDefinition, StorageOperationStatus> createNodeFilter(String serviceId,
            String componentInstanceId) {
        CINodeFilterDataDefinition nodeFilterDataDefinition = new CINodeFilterDataDefinition();
        return addOrUpdateNodeFilter(false, serviceId, componentInstanceId, nodeFilterDataDefinition);
    }

    public Either<CINodeFilterDataDefinition, StorageOperationStatus> deleteConstraint(String serviceId,
            String componentInstanceId, CINodeFilterDataDefinition nodeFilterDataDefinition, int propertyIndex) {
        ListDataDefinition<RequirementNodeFilterPropertyDataDefinition> properties =
                nodeFilterDataDefinition.getProperties();
        properties.getListToscaDataDefinition().remove(propertyIndex);
        nodeFilterDataDefinition.setProperties(properties);
        return addOrUpdateNodeFilter(true, serviceId, componentInstanceId, nodeFilterDataDefinition);
    }

    public Either<CINodeFilterDataDefinition, StorageOperationStatus> addNewProperty(String serviceId,
            String componentInstanceId, CINodeFilterDataDefinition nodeFilterDataDefinition,
            RequirementNodeFilterPropertyDataDefinition requirementNodeFilterPropertyDataDefinition) {
        ListDataDefinition<RequirementNodeFilterPropertyDataDefinition> properties =
                nodeFilterDataDefinition.getProperties();
        if (properties == null) {
            properties = new ListDataDefinition<>();
            nodeFilterDataDefinition.setProperties(properties);
        }
        properties.getListToscaDataDefinition().add(requirementNodeFilterPropertyDataDefinition);
        nodeFilterDataDefinition.setProperties(properties);
        return addOrUpdateNodeFilter(true, serviceId, componentInstanceId, nodeFilterDataDefinition);
    }

    public Either<CINodeFilterDataDefinition, StorageOperationStatus> updateProperties(String serviceId,
            String componentInstanceId, CINodeFilterDataDefinition nodeFilterDataDefinition,
            List<RequirementNodeFilterPropertyDataDefinition> requirementNodeFilterPropertyDataDefinition) {
        ListDataDefinition<RequirementNodeFilterPropertyDataDefinition> properties =
                nodeFilterDataDefinition.getProperties();
        properties.getListToscaDataDefinition().clear();
        properties.getListToscaDataDefinition().addAll(requirementNodeFilterPropertyDataDefinition);
        nodeFilterDataDefinition.setProperties(properties);
        return addOrUpdateNodeFilter(true, serviceId, componentInstanceId, nodeFilterDataDefinition);
    }

    public Either<CINodeFilterDataDefinition, StorageOperationStatus> updateNodeFilter(String serviceId,
            String componentInstanceId, CINodeFilterDataDefinition ciNodeFilterDataDefinition) {
        return addOrUpdateNodeFilter(true, serviceId, componentInstanceId, ciNodeFilterDataDefinition);
    }

    private Either<CINodeFilterDataDefinition, StorageOperationStatus> addOrUpdateNodeFilter(boolean isUpdateAction,
            String serviceId, String componentInstanceId, CINodeFilterDataDefinition ciNodeFilterDataDefinition) {

        StorageOperationStatus statusRes;
        Either<GraphVertex, TitanOperationStatus> getToscaElementRes;

        getToscaElementRes = titanDao.getVertexById(serviceId, JsonParseFlagEnum.NoParse);
        if (getToscaElementRes.isRight()) {
            TitanOperationStatus status = getToscaElementRes.right().value();
            CommonUtility.addRecordToLog(logger, CommonUtility.LogLevelEnum.DEBUG,
                    "Failed to get tosca element {} upon adding the properties. Status is {}. ", serviceId, status);
            statusRes = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
            return Either.right(statusRes);
        }
        GraphVertex serviceVertex = getToscaElementRes.left().value();
        ciNodeFilterDataDefinition.setID(componentInstanceId);
        statusRes = performUpdateToscaAction(isUpdateAction, serviceVertex, ImmutableList.of(ciNodeFilterDataDefinition));
        if (!statusRes.equals(StorageOperationStatus.OK)) {
            titanDao.rollback();
            logger.error(
                    " Failed to perform tosca update for node filter in service {} , component instance {}. status is {}",
                    serviceId, componentInstanceId, statusRes);
            return Either.right(statusRes);
        }
        titanDao.commit();
        return Either.left(ciNodeFilterDataDefinition);

    }


    private StorageOperationStatus performUpdateToscaAction(boolean isUpdate, GraphVertex graphVertex,
            List<CINodeFilterDataDefinition> toscaDataList) {
        if (isUpdate) {
            return updateToscaDataOfToscaElement(graphVertex, EdgeLabelEnum.NODE_FILTER_TEMPLATE,
                    VertexTypeEnum.NODE_FILTER_TEMPLATE, toscaDataList, JsonPresentationFields.UNIQUE_ID);
        } else {
            return addToscaDataToToscaElement(graphVertex, EdgeLabelEnum.NODE_FILTER_TEMPLATE,
                    VertexTypeEnum.NODE_FILTER_TEMPLATE, toscaDataList, JsonPresentationFields.UNIQUE_ID);
        }
    }

}



