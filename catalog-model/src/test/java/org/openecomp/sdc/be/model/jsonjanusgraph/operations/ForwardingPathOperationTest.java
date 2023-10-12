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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

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
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

@ExtendWith(MockitoExtension.class)
public class ForwardingPathOperationTest {

    @Mock
    private JanusGraphDao janusGraphDao;

    @Mock
    private JanusGraphVertex vertex;

    private ForwardingPathOperation test = mock(ForwardingPathOperation.class, CALLS_REAL_METHODS);

    @BeforeEach
    public void setUp() throws Exception {
        test.setJanusGraphDao(janusGraphDao);
    }

    @Test
    public void deleteForwardingPath() {
        Service service = new Service();
        Set<String> paths = Set.of("path1", "path2");
        String uniqueId = "uniqueId";
        service.setUniqueId(uniqueId);
        Map<GraphPropertyEnum, Object> hasProps1 = new HashMap<>();
        hasProps1.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
        hasProps1.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        List<GraphVertex> list = new ArrayList<>();
        GraphVertex graphVertex = new GraphVertex();
        graphVertex.setVertex(vertex);
        graphVertex.setUniqueId(uniqueId);
        graphVertex.setMetadataProperties(hasProps1);
        list.add(graphVertex);
        doReturn(Either.left(graphVertex)).when(janusGraphDao).getVertexById(service.getUniqueId(), JsonParseFlagEnum.NoParse);
        doReturn(StorageOperationStatus.OK).when(test).deleteToscaDataElements(graphVertex, EdgeLabelEnum.FORWARDING_PATH,
            new ArrayList<>(paths));
        Either<Set<String>, StorageOperationStatus> ret = test.deleteForwardingPath(service, paths);
        assertTrue(ret.isLeft());
    }

    @Test
    public void deleteForwardingPath_NotFound() {
        Service service = new Service();
        Set<String> paths = Set.of("path1", "path2");
        String uniqueId = "uniqueId";
        service.setUniqueId(uniqueId);
        Map<GraphPropertyEnum, Object> hasProps1 = new HashMap<>();
        hasProps1.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
        hasProps1.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        List<GraphVertex> list = new ArrayList<>();
        GraphVertex graphVertex = new GraphVertex();
        graphVertex.setVertex(vertex);
        graphVertex.setUniqueId(uniqueId);
        graphVertex.setMetadataProperties(hasProps1);
        list.add(graphVertex);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(janusGraphDao).getVertexById(service.getUniqueId(),
            JsonParseFlagEnum.NoParse);
        Either<Set<String>, StorageOperationStatus> ret = test.deleteForwardingPath(service, paths);
        assertTrue(ret.isRight());
    }

    @Test
    public void deleteForwardingPath_ErrorDeleting() {
        Service service = new Service();
        Set<String> paths = Set.of("path1", "path2");
        String uniqueId = "uniqueId";
        service.setUniqueId(uniqueId);
        Map<GraphPropertyEnum, Object> hasProps1 = new HashMap<>();
        hasProps1.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
        hasProps1.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        List<GraphVertex> list = new ArrayList<>();
        GraphVertex graphVertex = new GraphVertex();
        graphVertex.setVertex(vertex);
        graphVertex.setUniqueId(uniqueId);
        graphVertex.setMetadataProperties(hasProps1);
        list.add(graphVertex);
        doReturn(Either.left(graphVertex)).when(janusGraphDao).getVertexById(service.getUniqueId(), JsonParseFlagEnum.NoParse);
        doReturn(StorageOperationStatus.CANNOT_UPDATE_EXISTING_ENTITY).when(test).deleteToscaDataElements(graphVertex, EdgeLabelEnum.FORWARDING_PATH,
        new ArrayList<>(paths));
        Either<Set<String>, StorageOperationStatus> ret = test.deleteForwardingPath(service, paths);
        assertTrue(ret.isRight());
    }

    @Test
    public void addForwardingPath() {
        Service service = new Service();
        String uniqueId = "uniqueId";
        service.setUniqueId(uniqueId);
        Map<GraphPropertyEnum, Object> hasProps1 = new HashMap<>();
        hasProps1.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
        hasProps1.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        GraphVertex graphVertex = new GraphVertex();
        graphVertex.setVertex(vertex);
        graphVertex.setUniqueId(uniqueId);
        graphVertex.setMetadataProperties(hasProps1);
        ForwardingPathDataDefinition forwardingPathDataDefinition = new ForwardingPathDataDefinition("forwardingPath");
        doReturn(Either.left(graphVertex)).when(janusGraphDao).getVertexById(service.getUniqueId(), JsonParseFlagEnum.NoParse);
        doReturn(StorageOperationStatus.OK).when(test).addToscaDataToToscaElement(graphVertex, EdgeLabelEnum.FORWARDING_PATH,
            VertexTypeEnum.FORWARDING_PATH, List.of(forwardingPathDataDefinition), JsonPresentationFields.UNIQUE_ID);
        Either<ForwardingPathDataDefinition, StorageOperationStatus> ret = test.addForwardingPath(service.getUniqueId(), forwardingPathDataDefinition);
        assertTrue(ret.isLeft());
    }

    @Test
    public void addForwardingPath_NotFound() {
        Service service = new Service();
        String uniqueId = "uniqueId";
        service.setUniqueId(uniqueId);
        Map<GraphPropertyEnum, Object> hasProps1 = new HashMap<>();
        hasProps1.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
        hasProps1.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        GraphVertex graphVertex = new GraphVertex();
        graphVertex.setVertex(vertex);
        graphVertex.setUniqueId(uniqueId);
        graphVertex.setMetadataProperties(hasProps1);
        ForwardingPathDataDefinition forwardingPathDataDefinition = new ForwardingPathDataDefinition("forwardingPath");
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(janusGraphDao).getVertexById(service.getUniqueId(), JsonParseFlagEnum.NoParse);
        Either<ForwardingPathDataDefinition, StorageOperationStatus> ret = test.addForwardingPath(service.getUniqueId(), forwardingPathDataDefinition);
        assertTrue(ret.isRight());
    }

    @Test
    public void updateForwardingPath() {
        Service service = new Service();
        String uniqueId = "uniqueId";
        service.setUniqueId(uniqueId);
        Map<GraphPropertyEnum, Object> hasProps1 = new HashMap<>();
        hasProps1.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
        hasProps1.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        GraphVertex graphVertex = new GraphVertex();
        graphVertex.setVertex(vertex);
        graphVertex.setUniqueId(uniqueId);
        graphVertex.setMetadataProperties(hasProps1);
        ForwardingPathDataDefinition forwardingPathDataDefinition = new ForwardingPathDataDefinition("forwardingPath");
        doReturn(Either.left(graphVertex)).when(janusGraphDao).getVertexById(service.getUniqueId(), JsonParseFlagEnum.NoParse);
        doReturn(StorageOperationStatus.OK).when(test).updateToscaDataOfToscaElement(graphVertex, EdgeLabelEnum.FORWARDING_PATH,
            VertexTypeEnum.FORWARDING_PATH, List.of(forwardingPathDataDefinition), JsonPresentationFields.UNIQUE_ID);
        Either<ForwardingPathDataDefinition, StorageOperationStatus> ret = test.updateForwardingPath(service.getUniqueId(), forwardingPathDataDefinition);
        assertTrue(ret.isLeft());
    }

    @Test
    public void updateForwardingPath_Error() {
        Service service = new Service();
        String uniqueId = "uniqueId";
        service.setUniqueId(uniqueId);
        Map<GraphPropertyEnum, Object> hasProps1 = new HashMap<>();
        hasProps1.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
        hasProps1.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        GraphVertex graphVertex = new GraphVertex();
        graphVertex.setVertex(vertex);
        graphVertex.setUniqueId(uniqueId);
        graphVertex.setMetadataProperties(hasProps1);
        ForwardingPathDataDefinition forwardingPathDataDefinition = new ForwardingPathDataDefinition("forwardingPath");
        doReturn(Either.left(graphVertex)).when(janusGraphDao).getVertexById(service.getUniqueId(), JsonParseFlagEnum.NoParse);
        doReturn(StorageOperationStatus.ARTIFACT_NOT_FOUND).when(test).updateToscaDataOfToscaElement(graphVertex, EdgeLabelEnum.FORWARDING_PATH,
            VertexTypeEnum.FORWARDING_PATH, List.of(forwardingPathDataDefinition), JsonPresentationFields.UNIQUE_ID);
        Either<ForwardingPathDataDefinition, StorageOperationStatus> ret = test.updateForwardingPath(service.getUniqueId(), forwardingPathDataDefinition);
        assertTrue(ret.isRight());
    }
}
