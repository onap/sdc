/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
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

package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import com.google.common.collect.ImmutableList;
import fj.data.Either;
import java.util.List;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementSubstitutionFilterPropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.log.enums.EcompErrorSeverity;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;

@org.springframework.stereotype.Component("substitution-filter-operations")
public class SubstitutionFilterOperation extends BaseOperation {

    private static final Logger LOGGER = Logger.getLogger(SubstitutionFilterOperation.class);

    public Either<SubstitutionFilterDataDefinition, StorageOperationStatus> createSubstitutionFilter(
        final String componentId, final String componentInstanceId) {

        return addOrUpdateSubstitutionFilter(false, componentId, componentInstanceId,
            new SubstitutionFilterDataDefinition());
    }

    public Either<SubstitutionFilterDataDefinition, StorageOperationStatus> deleteConstraint(
        final String serviceId, final String componentInstanceId,
        final SubstitutionFilterDataDefinition substitutionFilterDataDefinition, final int propertyIndex) {

        final ListDataDefinition<RequirementSubstitutionFilterPropertyDataDefinition> properties =
            substitutionFilterDataDefinition.getProperties();
        properties.getListToscaDataDefinition().remove(propertyIndex);
        substitutionFilterDataDefinition.setProperties(properties);
        return addOrUpdateSubstitutionFilter(true, serviceId, componentInstanceId, substitutionFilterDataDefinition);
    }

    public Either<SubstitutionFilterDataDefinition, StorageOperationStatus> addNewProperty(
        final String componentId, final String componentInstanceId,
        final SubstitutionFilterDataDefinition substitutionFilterDataDefinition,
        final RequirementSubstitutionFilterPropertyDataDefinition requirementSubstitutionFilterPropertyDataDefinition) {

        ListDataDefinition<RequirementSubstitutionFilterPropertyDataDefinition> properties =
            substitutionFilterDataDefinition.getProperties();
        if (properties == null) {
            properties = new ListDataDefinition<>();
            substitutionFilterDataDefinition.setProperties(properties);
        }
        properties.getListToscaDataDefinition().add(requirementSubstitutionFilterPropertyDataDefinition);
        substitutionFilterDataDefinition.setProperties(properties);
        return addOrUpdateSubstitutionFilter(true, componentId, componentInstanceId, substitutionFilterDataDefinition);
    }

    public Either<SubstitutionFilterDataDefinition, StorageOperationStatus> updateSubstitutionFilter(
        final String serviceId, final String componentInstanceId,
        final SubstitutionFilterDataDefinition substitutionFilterDataDefinition,
        final List<RequirementSubstitutionFilterPropertyDataDefinition> requirementSubstitutionFilterPropertyDataDefinitions) {

        final ListDataDefinition<RequirementSubstitutionFilterPropertyDataDefinition> properties =
            substitutionFilterDataDefinition.getProperties();
        properties.getListToscaDataDefinition().clear();
        properties.getListToscaDataDefinition().addAll(requirementSubstitutionFilterPropertyDataDefinitions);
        substitutionFilterDataDefinition.setProperties(properties);
        return addOrUpdateSubstitutionFilter(true, serviceId, componentInstanceId,
            substitutionFilterDataDefinition);
    }

    private Either<SubstitutionFilterDataDefinition, StorageOperationStatus> addOrUpdateSubstitutionFilter(
        final boolean isUpdateAction, final String componentId, final String componentInstanceId,
        final SubstitutionFilterDataDefinition substitutionFilterDataDefinition) {

        StorageOperationStatus statusRes;
        Either<GraphVertex, JanusGraphOperationStatus> getToscaElementRes;

        getToscaElementRes = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getToscaElementRes.isRight()) {
            final JanusGraphOperationStatus status = getToscaElementRes.right().value();
            CommonUtility.addRecordToLog(LOGGER, CommonUtility.LogLevelEnum.DEBUG,
                "Failed to get tosca element {} upon adding the properties. Status is {}. ", componentId, status);
            statusRes = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
            return Either.right(statusRes);
        }
        final GraphVertex serviceVertex = getToscaElementRes.left().value();
        substitutionFilterDataDefinition.setID(componentInstanceId);
        statusRes = performUpdateToscaAction(isUpdateAction, serviceVertex,
            ImmutableList.of(substitutionFilterDataDefinition));
        if (!statusRes.equals(StorageOperationStatus.OK)) {
            janusGraphDao.rollback();
            LOGGER.error(EcompErrorSeverity.ERROR, EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR,
                " Failed to perform tosca update for substitution filter in service {} , component instance {}. status is {}",
                componentId, componentInstanceId, statusRes);
            return Either.right(statusRes);
        }
        janusGraphDao.commit();
        return Either.left(substitutionFilterDataDefinition);
    }

    private StorageOperationStatus performUpdateToscaAction(final boolean isUpdate,
                                                            final GraphVertex graphVertex,
                                                            final List<SubstitutionFilterDataDefinition> toscaDataList) {
        if (isUpdate) {
            return updateToscaDataOfToscaElement(graphVertex, EdgeLabelEnum.SUBSTITUTION_FILTER_TEMPLATE,
                VertexTypeEnum.SUBSTITUTION_FILTER_TEMPLATE, toscaDataList, JsonPresentationFields.UNIQUE_ID);
        } else {
            return addToscaDataToToscaElement(graphVertex, EdgeLabelEnum.SUBSTITUTION_FILTER_TEMPLATE,
                VertexTypeEnum.SUBSTITUTION_FILTER_TEMPLATE, toscaDataList, JsonPresentationFields.UNIQUE_ID);
        }
    }

}



