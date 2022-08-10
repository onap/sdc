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

import fj.data.Either;
import java.util.List;
import java.util.Objects;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterPropertyDataDefinition;
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

    public Either<SubstitutionFilterDataDefinition, StorageOperationStatus> createSubstitutionFilter(final String componentId) {
        return addOrUpdateSubstitutionFilter(false, componentId, new SubstitutionFilterDataDefinition());
    }

    public Either<SubstitutionFilterDataDefinition, StorageOperationStatus> deleteConstraint(final String serviceId,
                                                                                             final SubstitutionFilterDataDefinition substitutionFilterDataDefinition,
                                                                                             final int propertyIndex) {
        final ListDataDefinition<SubstitutionFilterPropertyDataDefinition> properties = substitutionFilterDataDefinition.getProperties();
        properties.getListToscaDataDefinition().remove(propertyIndex);
        substitutionFilterDataDefinition.setProperties(properties);
        return addOrUpdateSubstitutionFilter(true, serviceId, substitutionFilterDataDefinition);
    }

    public Either<SubstitutionFilterDataDefinition, StorageOperationStatus> addPropertyFilter(final String componentId,
                                                                                              final SubstitutionFilterDataDefinition substitutionFilterDataDefinition,
                                                                                              final SubstitutionFilterPropertyDataDefinition substitutionFilterPropertyDataDefinition) {
        final SubstitutionFilterDataDefinition substitutionFilterDataDefinition1 = Objects
            .requireNonNullElseGet(substitutionFilterDataDefinition, SubstitutionFilterDataDefinition::new);
        final ListDataDefinition<SubstitutionFilterPropertyDataDefinition> properties = Objects
            .requireNonNullElseGet(substitutionFilterDataDefinition1.getProperties(), ListDataDefinition::new);
        properties.getListToscaDataDefinition().add(substitutionFilterPropertyDataDefinition);
        substitutionFilterDataDefinition1.setProperties(properties);
        return addOrUpdateSubstitutionFilter(true, componentId, substitutionFilterDataDefinition1);
    }

    public Either<SubstitutionFilterDataDefinition, StorageOperationStatus> updatePropertyFilters(final String componentId,
                                                                                                  final SubstitutionFilterDataDefinition substitutionFilterDataDefinition,
                                                                                                  final List<SubstitutionFilterPropertyDataDefinition> substitutionFilterPropertyDataDefinition) {
        final ListDataDefinition<SubstitutionFilterPropertyDataDefinition> properties = substitutionFilterDataDefinition.getProperties();
        properties.getListToscaDataDefinition().clear();
        properties.getListToscaDataDefinition().addAll(substitutionFilterPropertyDataDefinition);
        substitutionFilterDataDefinition.setProperties(properties);
        return addOrUpdateSubstitutionFilter(true, componentId, substitutionFilterDataDefinition);
    }

    public Either<SubstitutionFilterDataDefinition, StorageOperationStatus> updatePropertyFilter(final String componentId,
                                                                                                 final SubstitutionFilterDataDefinition substitutionFilter,
                                                                                                 final SubstitutionFilterPropertyDataDefinition substitutionFilterProperty,
                                                                                                 final int index) {
        substitutionFilter.getProperties().getListToscaDataDefinition().set(index, substitutionFilterProperty);
        return addOrUpdateSubstitutionFilter(true, componentId, substitutionFilter);
    }

    private Either<SubstitutionFilterDataDefinition, StorageOperationStatus> addOrUpdateSubstitutionFilter(final boolean isUpdateAction,
                                                                                                           final String componentId,
                                                                                                           final SubstitutionFilterDataDefinition substitutionFilterDataDefinition) {
        final Either<GraphVertex, JanusGraphOperationStatus> toscaElementEither = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (toscaElementEither.isRight()) {
            final JanusGraphOperationStatus status = toscaElementEither.right().value();
            CommonUtility
                .addRecordToLog(LOGGER, CommonUtility.LogLevelEnum.DEBUG, "Failed to get tosca element {} upon adding the properties. Status is {}. ",
                    componentId, status);
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
        }
        final GraphVertex serviceVertex = toscaElementEither.left().value();
        substitutionFilterDataDefinition.setID(componentId);
        final StorageOperationStatus operationStatus = performUpdateToscaAction(isUpdateAction, serviceVertex,
            List.of(substitutionFilterDataDefinition));
        if (!StorageOperationStatus.OK.equals(operationStatus)) {
            janusGraphDao.rollback();
            LOGGER.error(EcompErrorSeverity.ERROR, EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR,
                " Failed to perform tosca update for substitution filter in service {} , component instance {}. status is {}", componentId,
                "componentInstanceId", operationStatus);
            return Either.right(operationStatus);
        }
        janusGraphDao.commit();
        return Either.left(substitutionFilterDataDefinition);
    }

    private StorageOperationStatus performUpdateToscaAction(final boolean isUpdate, final GraphVertex graphVertex,
                                                            final List<SubstitutionFilterDataDefinition> toscaDataList) {
        if (isUpdate) {
            return updateToscaDataOfToscaElement(graphVertex, EdgeLabelEnum.SUBSTITUTION_FILTER_TEMPLATE, VertexTypeEnum.SUBSTITUTION_FILTER_TEMPLATE,
                toscaDataList, JsonPresentationFields.UNIQUE_ID);
        } else {
            return addToscaDataToToscaElement(graphVertex, EdgeLabelEnum.SUBSTITUTION_FILTER_TEMPLATE, VertexTypeEnum.SUBSTITUTION_FILTER_TEMPLATE,
                toscaDataList, JsonPresentationFields.UNIQUE_ID);
        }
    }
}
