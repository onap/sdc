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
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.operations.api.IComponentInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IRequirementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.RequirementData;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FulfilledRequirementsMigrationService extends FulfilledCapabilityRequirementMigrationService<RequirementDefinition, RequirementData> {

    @Resource(name = "requirement-operation")
    IRequirementOperation requirementOperation;

    @Resource(name = "component-instance-operation")
    IComponentInstanceOperation componentInstanceOperation;

    @Override
    Either<RequirementDefinition, TitanOperationStatus> getToscaDefinition(RequirementData data) {
        return requirementOperation.getRequirement(data.getUniqueId());
    }

    @Override
    void setPath(RequirementDefinition def, List<String> path) {
        def.setPath(path);
    }

    @Override
    String getType(RequirementDefinition def) {
        return def.getCapability();
    }

    @Override
    Either<List<ImmutablePair<RequirementData, GraphEdge>>, TitanOperationStatus> getFulfilledCapReqs(ComponentInstance instance, NodeTypeEnum nodeTypeEnum) {
        return componentInstanceOperation.getFulfilledRequirements(instance, nodeTypeEnum);
    }

    @Override
    ListDataDefinition convertToDefinitionListObject(List<RequirementDefinition> capReqDefList) {
        List<RequirementDataDefinition> requirementDataDefinitions = new ArrayList<>();
        requirementDataDefinitions.addAll(capReqDefList);
        return new ListRequirementDataDefinition(requirementDataDefinitions);
    }

    @Override
    MapDataDefinition convertToDefinitionMapObject(Map<String, ListDataDefinition> reqCapForInstance) {
        Map<String, ListRequirementDataDefinition> reqDefList = castDefinitionListToRequirementList(reqCapForInstance);
        return new MapListRequirementDataDefinition(reqDefList);
    }

    @Override
    Either<GraphVertex, TitanOperationStatus> getAssociatedDefinitions(GraphVertex component) {
        return titanDao.getChildVertex(component,  EdgeLabelEnum.FULLFILLED_REQUIREMENTS, JsonParseFlagEnum.NoParse);
    }

    @Override
    Either<GraphVertex, StorageOperationStatus> associateToGraph(GraphVertex graphVertex, Map<String, MapDataDefinition> defsByInstance) {
        return topologyTemplateOperation.assosiateElementToData(graphVertex, VertexTypeEnum.FULLFILLED_REQUIREMENTS, EdgeLabelEnum.FULLFILLED_REQUIREMENTS, defsByInstance);
    }

    private Map<String, ListRequirementDataDefinition> castDefinitionListToRequirementList(Map<String, ListDataDefinition> reqCapForInstance) {
        return reqCapForInstance.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> (ListRequirementDataDefinition) entry.getValue()));
    }
}
