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

package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.relations;

import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListCapabiltyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.operations.api.ICapabilityOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ComponentInstanceOperation;
import org.openecomp.sdc.be.resources.data.CapabilityData;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FulfilledCapabilitiesMigrationService extends FulfilledCapabilityRequirementMigrationService<CapabilityDefinition, CapabilityData> {

    @Resource(name = "capability-operation")
    private ICapabilityOperation capabilityOperation;

    @Resource(name = "component-instance-operation")
    private ComponentInstanceOperation componentInstanceOperation;

    @Override
    Either<CapabilityDefinition, StorageOperationStatus> getToscaDefinition(CapabilityData data) {
        return capabilityOperation.getCapability(data.getUniqueId());
    }

    @Override
    void setPath(CapabilityDefinition def, List<String> path) {
        def.setPath(path);
    }

    @Override
    String getType(CapabilityDefinition def) {
        return def.getType();
    }

    @Override
    Either<List<ImmutablePair<CapabilityData, GraphEdge>>, TitanOperationStatus> getFulfilledCapReqs(ComponentInstance instance, NodeTypeEnum nodeTypeEnum) {
        return componentInstanceOperation.getFulfilledCapabilities(instance, nodeTypeEnum);
    }

    @Override
    ListDataDefinition convertToDefinitionListObject(List<CapabilityDefinition> capReqDefList) {
        List<CapabilityDataDefinition> capabilityDataDefinitions = new ArrayList<>();
        capabilityDataDefinitions.addAll(capReqDefList);
        return new ListCapabilityDataDefinition(capabilityDataDefinitions);
    }

    @Override
    MapDataDefinition convertToDefinitionMapObject(Map<String, ListDataDefinition> reqCapForInstance) {
        Map<String, ListCapabilityDataDefinition> capabilitiesList = castDataDefinitionListToCapabilityList(reqCapForInstance);
        return new MapListCapabiltyDataDefinition(capabilitiesList);
    }

    @Override
    Either<GraphVertex, TitanOperationStatus> getAssociatedDefinitions(GraphVertex component) {
        return titanDao.getChildVertex(component,  EdgeLabelEnum.FULLFILLED_REQUIREMENTS, JsonParseFlagEnum.NoParse);
    }

    @Override
    Either<GraphVertex, StorageOperationStatus> associateToGraph(GraphVertex graphVertex, Map<String, MapDataDefinition> defsByInstance) {
        return topologyTemplateOperation.assosiateElementToData(graphVertex, VertexTypeEnum.FULLFILLED_CAPABILITIES, EdgeLabelEnum.FULLFILLED_CAPABILITIES, defsByInstance);
    }

    private Map<String, ListCapabilityDataDefinition> castDataDefinitionListToCapabilityList(Map<String, ListDataDefinition> reqCapForInstance) {
        return reqCapForInstance.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> (ListCapabilityDataDefinition) entry.getValue()));
    }
}
