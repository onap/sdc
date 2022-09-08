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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeFilterConstraintType;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.log.wrappers.Logger;

@org.springframework.stereotype.Component("node-filter-operations")
public class NodeFilterOperation extends BaseOperation {

    private static Logger logger = Logger.getLogger(NodeFilterOperation.class);

    public Either<Set<String>, StorageOperationStatus> deleteNodeFilters(final Component component, final Set<String> componentInstanceIds) {
        final Either<GraphVertex, JanusGraphOperationStatus> getComponentVertex;
        final Either<GraphVertex, JanusGraphOperationStatus> getNodeFilterVertex;
        StorageOperationStatus status;
        getComponentVertex = janusGraphDao.getVertexById(component.getUniqueId(), JsonParseFlagEnum.NoParse);
        if (getComponentVertex.isRight()) {
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getComponentVertex.right().value()));
        }
        getNodeFilterVertex = janusGraphDao
            .getChildVertex(getComponentVertex.left().value(), EdgeLabelEnum.NODE_FILTER_TEMPLATE, JsonParseFlagEnum.NoParse);
        if (getNodeFilterVertex.isLeft()) {
            status = deleteToscaDataElements(component.getUniqueId(), EdgeLabelEnum.NODE_FILTER_TEMPLATE, new ArrayList<>(componentInstanceIds));
            if (status != StorageOperationStatus.OK) {
                return Either.right(status);
            }
        }
        return Either.left(componentInstanceIds);
    }

    public Either<String, StorageOperationStatus> deleteNodeFilter(final Component component, final String componentInstanceId) {
        final Either<Set<String>, StorageOperationStatus> listStorageOperationStatusEither = deleteNodeFilters(component,
            Set.of(componentInstanceId));
        if (listStorageOperationStatusEither.isRight()) {
            return Either.right(listStorageOperationStatusEither.right().value());
        }
        return Either.left(componentInstanceId);
    }

    public Either<CINodeFilterDataDefinition, StorageOperationStatus> createNodeFilter(final String componentId, final String componentInstanceId) {
        CINodeFilterDataDefinition nodeFilterDataDefinition = new CINodeFilterDataDefinition();
        return addOrUpdateNodeFilter(false, componentId, componentInstanceId, nodeFilterDataDefinition);
    }

    public Either<CINodeFilterDataDefinition, StorageOperationStatus> deleteConstraint(final String serviceId, final String componentInstanceId,
                                                                                       final CINodeFilterDataDefinition nodeFilterDataDefinition,
                                                                                       final int propertyIndex,
                                                                                       final NodeFilterConstraintType nodeFilterConstraintType) {
        if (NodeFilterConstraintType.PROPERTIES.equals(nodeFilterConstraintType)) {
            nodeFilterDataDefinition.getProperties().getListToscaDataDefinition().remove(propertyIndex);
        } else if (NodeFilterConstraintType.CAPABILITIES.equals(nodeFilterConstraintType)) {
            removeCapabilityNodeFilterByIndex(nodeFilterDataDefinition, propertyIndex);
        }
        return addOrUpdateNodeFilter(true, serviceId, componentInstanceId, nodeFilterDataDefinition);
    }

    public Either<CINodeFilterDataDefinition, StorageOperationStatus> addPropertyFilter(final String componentId, final String componentInstanceId,
                                                                                        final CINodeFilterDataDefinition nodeFilterDataDefinition,
                                                                                        final PropertyFilterDataDefinition propertyFilterDataDefinition) {
        ListDataDefinition<PropertyFilterDataDefinition> properties = nodeFilterDataDefinition.getProperties();
        if (properties == null) {
            properties = new ListDataDefinition<>();
            nodeFilterDataDefinition.setProperties(properties);
        }
        properties.getListToscaDataDefinition().add(propertyFilterDataDefinition);
        nodeFilterDataDefinition.setProperties(properties);
        return addOrUpdateNodeFilter(true, componentId, componentInstanceId, nodeFilterDataDefinition);
    }

    public Either<CINodeFilterDataDefinition, StorageOperationStatus> addCapabilities(final String componentId, final String componentInstanceId,
                                                                                      final CINodeFilterDataDefinition nodeFilterDataDefinition,
                                                                                      final RequirementNodeFilterCapabilityDataDefinition requirementNodeFilterCapabilityDataDefinition) {
        ListDataDefinition<RequirementNodeFilterCapabilityDataDefinition> capabilities = nodeFilterDataDefinition.getCapabilities();
        if (capabilities == null) {
            capabilities = new ListDataDefinition<>();
            nodeFilterDataDefinition.setCapabilities(capabilities);
        }

        final Optional<RequirementNodeFilterCapabilityDataDefinition> existingCap = capabilities
                .getListToscaDataDefinition().stream()
                .filter(def -> def.getName().equals(requirementNodeFilterCapabilityDataDefinition.getName())).findAny();

        if (existingCap.isPresent()) {
            final ListDataDefinition<PropertyFilterDataDefinition> newProperties  = requirementNodeFilterCapabilityDataDefinition.getProperties();
            final ListDataDefinition<PropertyFilterDataDefinition> existingProperties = existingCap.get().getProperties();
            newProperties.getListToscaDataDefinition().forEach((existingProperties::add)) ;
        } else {
            capabilities.getListToscaDataDefinition().add(requirementNodeFilterCapabilityDataDefinition);
        }
        nodeFilterDataDefinition.setCapabilities(capabilities);
        return addOrUpdateNodeFilter(true, componentId, componentInstanceId, nodeFilterDataDefinition);
    }

    public Either<CINodeFilterDataDefinition, StorageOperationStatus> updateNodeFilter(final String serviceId, final String componentInstanceId,
                                                                                       final CINodeFilterDataDefinition ciNodeFilterDataDefinition) {
        return addOrUpdateNodeFilter(true, serviceId, componentInstanceId, ciNodeFilterDataDefinition);
    }

    public Either<CINodeFilterDataDefinition, StorageOperationStatus> addNodeFilterData(
            final String componentId,
            final String componentInstanceId,
            final CINodeFilterDataDefinition nodeFilterDataDefinition) {
        return addOrUpdateNodeFilter(false, componentId, componentInstanceId, nodeFilterDataDefinition);
    }

    private void removeCapabilityNodeFilterByIndex(final CINodeFilterDataDefinition nodeFilterDataDefinition, final int filterToRemoveIndex) {
        int currentFilterCountdown = filterToRemoveIndex;
        final List<RequirementNodeFilterCapabilityDataDefinition> filtersByCapability =
            nodeFilterDataDefinition.getCapabilities().getListToscaDataDefinition();
        for (final RequirementNodeFilterCapabilityDataDefinition capabilityFilterGroup : filtersByCapability) {
            final List<PropertyFilterDataDefinition> capabilityFilters = capabilityFilterGroup.getProperties().getListToscaDataDefinition();
            if (isFilterInCapabilityGroup(currentFilterCountdown, capabilityFilters)) {
                capabilityFilters.remove(currentFilterCountdown);
                break;
            } else {
                currentFilterCountdown = getRemainingFilterCount(currentFilterCountdown, capabilityFilters);
            }
        }
    }

    private boolean isFilterInCapabilityGroup(int currentFilterCount, List<PropertyFilterDataDefinition> capabilityFilters) {
        return capabilityFilters.size() > currentFilterCount;
    }

    private int getRemainingFilterCount(int currentFilterCount, final List<PropertyFilterDataDefinition> capabilityFilters) {
        return currentFilterCount - capabilityFilters.size();
    }

    private Either<CINodeFilterDataDefinition, StorageOperationStatus> addOrUpdateNodeFilter(final boolean isUpdateAction, final String componentId,
                                                                                             final String componentInstanceId,
                                                                                             final CINodeFilterDataDefinition ciNodeFilterDataDefinition) {

        final Either<GraphVertex, JanusGraphOperationStatus> serviceVertexEither =
            janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (serviceVertexEither.isRight()) {
            JanusGraphOperationStatus status = serviceVertexEither.right().value();
            CommonUtility
                .addRecordToLog(logger, CommonUtility.LogLevelEnum.DEBUG, "Failed to get tosca element {} upon adding the properties. Status is {}. ",
                    componentId, status);
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
        }
        final GraphVertex serviceVertex = serviceVertexEither.left().value();
        ciNodeFilterDataDefinition.setID(componentInstanceId);
        final StorageOperationStatus statusRes = performUpdateToscaAction(isUpdateAction, serviceVertex, List.of(ciNodeFilterDataDefinition));
        if (!statusRes.equals(StorageOperationStatus.OK)) {
            janusGraphDao.rollback();
            logger.error(" Failed to perform tosca update for node filter in service {} , component instance {}. status is {}", componentId,
                componentInstanceId, statusRes);
            return Either.right(statusRes);
        }
        janusGraphDao.commit();
        return Either.left(ciNodeFilterDataDefinition);
    }

    private StorageOperationStatus performUpdateToscaAction(final boolean isUpdate, final GraphVertex graphVertex,
                                                            final List<CINodeFilterDataDefinition> toscaDataList) {
        if (isUpdate) {
            return updateToscaDataOfToscaElement(graphVertex, EdgeLabelEnum.NODE_FILTER_TEMPLATE, VertexTypeEnum.NODE_FILTER_TEMPLATE, toscaDataList,
                JsonPresentationFields.UNIQUE_ID);
        } else {
            return addToscaDataToToscaElement(graphVertex, EdgeLabelEnum.NODE_FILTER_TEMPLATE, VertexTypeEnum.NODE_FILTER_TEMPLATE, toscaDataList,
                JsonPresentationFields.UNIQUE_ID);
        }
    }
}
