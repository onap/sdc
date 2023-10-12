/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2023 Nordix Foundation. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.gson.Gson;
import fj.data.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.janusgraph.core.JanusGraphVertex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

@ExtendWith(MockitoExtension.class)
public class GroupsOperationMockTest {

    @Mock
    private JanusGraphDao janusGraphDao;

    @Mock
    private JanusGraphVertex vertex;

    protected static final String CONTAINER_ID = "containerId";
    protected static final String CONTAINER_NAME = "containerName";

    private GroupsOperation test = mock(GroupsOperation.class, CALLS_REAL_METHODS);

    @BeforeEach
    public void setUp() throws Exception {
        test.setJanusGraphDao(janusGraphDao);
    }

    @Test
    public void deleteCalculatedCapabilitiesWithProperties() {
        GroupDefinition g1 = createGroupDefinition("g1");
        GroupDefinition g2 = createGroupDefinition("g2");
        List<GroupDefinition> listGroup = new ArrayList<>();
        listGroup.add(g1);
        listGroup.add(g2);
        GraphVertex createdGraphVertex = createBasicContainerGraphVertex();
        doReturn(Either.left(createdGraphVertex)).when(janusGraphDao).getChildVertex(createdGraphVertex, EdgeLabelEnum.CALCULATED_CAPABILITIES,
            JsonParseFlagEnum.ParseJson);
        doReturn(Either.left(createdGraphVertex)).when(janusGraphDao).getVertexById(CONTAINER_ID, JsonParseFlagEnum.NoParse);
        doReturn(Either.left(createdGraphVertex)).when(test).updateOrCopyOnUpdate(createdGraphVertex, createdGraphVertex,
            EdgeLabelEnum.CALCULATED_CAPABILITIES);
        doReturn(Either.left(createdGraphVertex)).when(janusGraphDao).getChildVertex(createdGraphVertex, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES,
            JsonParseFlagEnum.ParseJson);
        doReturn(Either.left(createdGraphVertex)).when(test).updateOrCopyOnUpdate(createdGraphVertex, createdGraphVertex,
            EdgeLabelEnum.CALCULATED_CAP_PROPERTIES);
        final StorageOperationStatus ret = test.deleteCalculatedCapabilitiesWithProperties(CONTAINER_ID, listGroup);
        assertEquals(StorageOperationStatus.OK, ret);
    }

    @Test
    public void deleteCalculatedCapabilitiesWithProperties_NotFound() {
        GroupDefinition g1 = createGroupDefinition("g1");
        GroupDefinition g2 = createGroupDefinition("g2");
        List<GroupDefinition> listGroup = new ArrayList<>();
        listGroup.add(g1);
        listGroup.add(g2);
        GraphVertex createdGraphVertex = createBasicContainerGraphVertex();
        doReturn(Either.left(createdGraphVertex)).when(janusGraphDao).getVertexById(CONTAINER_ID, JsonParseFlagEnum.NoParse);
        doReturn(Either.left(createdGraphVertex)).when(janusGraphDao).getChildVertex(createdGraphVertex, EdgeLabelEnum.CALCULATED_CAPABILITIES,
            JsonParseFlagEnum.ParseJson);
        doReturn(Either.left(createdGraphVertex)).when(test).updateOrCopyOnUpdate(createdGraphVertex, createdGraphVertex,
            EdgeLabelEnum.CALCULATED_CAPABILITIES);
        doReturn(Either.left(createdGraphVertex)).when(janusGraphDao).getChildVertex(createdGraphVertex, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES,
            JsonParseFlagEnum.ParseJson);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(test).updateOrCopyOnUpdate(createdGraphVertex, createdGraphVertex,
            EdgeLabelEnum.CALCULATED_CAP_PROPERTIES);
        final StorageOperationStatus ret = test.deleteCalculatedCapabilitiesWithProperties(CONTAINER_ID, listGroup);
        assertEquals(StorageOperationStatus.NOT_FOUND, ret);
    }

    private GroupDefinition createGroupDefinition(String id) {
        GroupDefinition groupDefinition = new GroupDefinition();
        groupDefinition.setUniqueId(id);
        groupDefinition.setInvariantName("name" + id);
        return groupDefinition;
    }

    private GraphVertex createBasicContainerGraphVertex() {
        GraphVertex resource = new GraphVertex(VertexTypeEnum.TOPOLOGY_TEMPLATE);
        resource.addMetadataProperty(GraphPropertyEnum.UNIQUE_ID, CONTAINER_ID);
        resource.addMetadataProperty(GraphPropertyEnum.NAME, CONTAINER_NAME);
        resource.setJsonMetadataField(JsonPresentationFields.NAME, CONTAINER_NAME);
        resource.setJsonMetadataField(JsonPresentationFields.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
        Map<String, GroupDataDefinition> groups = new HashMap<>();
        GroupDefinition g1 = createGroupDefinition("g1");
        GroupDefinition g2 = createGroupDefinition("g2");
        groups.put(g1.getInvariantName(), new GroupDataDefinition(g1));
        groups.put(g2.getInvariantName(), new GroupDataDefinition(g2));
        resource.setJson(groups);
        return resource;
    }
}
