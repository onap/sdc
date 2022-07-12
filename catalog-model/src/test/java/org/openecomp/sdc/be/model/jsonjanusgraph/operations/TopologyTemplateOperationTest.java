/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraphVertex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapCapabilityProperty;
import org.openecomp.sdc.be.datatypes.elements.MapListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;

@ExtendWith(MockitoExtension.class)
class TopologyTemplateOperationTest {

    private static final String CONTAINER_ID = "containerId";
    @InjectMocks
    private TopologyTemplateOperation topologyTemplateOperation;
    @Mock
    private JanusGraphDao janusGraphDao;

    @Test
    void overrideToscaDataOfToscaElement_failedToFetchContainerVertex() {
        when(janusGraphDao.getVertexById(CONTAINER_ID, JsonParseFlagEnum.NoParse)).thenReturn(Either.right(
            JanusGraphOperationStatus.INVALID_ID));
        StorageOperationStatus storageOperationStatus = topologyTemplateOperation.overrideToscaDataOfToscaElement(CONTAINER_ID,
            EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, Collections.emptyMap());
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.INVALID_ID);
    }

    @Test
    void overrideToscaDataOfToscaElement_failedToFetchDataVertex() {
        GraphVertex containerVertex = new GraphVertex();
        when(janusGraphDao.getVertexById(CONTAINER_ID, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(containerVertex));
        when(janusGraphDao.getChildVertex(containerVertex, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, JsonParseFlagEnum.ParseJson)).thenReturn(
            Either.right(
                JanusGraphOperationStatus.NOT_FOUND));
        StorageOperationStatus storageOperationStatus = topologyTemplateOperation.overrideToscaDataOfToscaElement(CONTAINER_ID,
            EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, Collections.emptyMap());
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.NOT_FOUND);
    }

    @Test
    void updateToscaDataDeepElements_failedToFetchContainerVertex() {
        when(janusGraphDao.getVertexById(CONTAINER_ID, JsonParseFlagEnum.NoParse)).thenReturn(Either.right(
            JanusGraphOperationStatus.INVALID_ID));
        StorageOperationStatus storageOperationStatus = topologyTemplateOperation.updateToscaDataDeepElementsBlockToToscaElement(CONTAINER_ID,
            EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, new MapCapabilityProperty(), "");
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.INVALID_ID);
    }

    @Test
    void updateToscaDataDeepElements_failedToFetchDataVertex() {
        GraphVertex containerVertex = new GraphVertex();
        when(janusGraphDao.getVertexById(CONTAINER_ID, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(containerVertex));
        when(janusGraphDao.getChildVertex(containerVertex, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, JsonParseFlagEnum.ParseJson)).thenReturn(
            Either.right(
                JanusGraphOperationStatus.NOT_FOUND));
        StorageOperationStatus storageOperationStatus = topologyTemplateOperation.updateToscaDataDeepElementsBlockToToscaElement(CONTAINER_ID,
            EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, new MapCapabilityProperty(), "");
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.NOT_FOUND);
    }

    @Test
    void addPolicyToToscaElementSuccessTest() {
        JanusGraphOperationStatus status = JanusGraphOperationStatus.OK;
        StorageOperationStatus result = addPolicyToToscaElementWithStatus(status);
        assertThat(result).isEqualTo(StorageOperationStatus.OK);
    }

    @Test
    void addPolicyToToscaElementFailureTest() {
        JanusGraphOperationStatus status = JanusGraphOperationStatus.ALREADY_EXIST;
        StorageOperationStatus result = addPolicyToToscaElementWithStatus(status);
        assertThat(result).isEqualTo(StorageOperationStatus.ENTITY_ALREADY_EXISTS);
    }

    @Test
    void testAssociateOrAddCalcCapReqToComponent() {
        StorageOperationStatus result;
        GraphVertex graphVertex = new GraphVertex();
        Map<String, MapListRequirementDataDefinition> calcRequirements = new HashMap<>();
        Map<String, MapListCapabilityDataDefinition> calcCapabilty = new HashMap<>();
        Map<String, MapCapabilityProperty> calCapabilitiesProps = new HashMap<>();
        addPolicyToToscaElementWithStatus(JanusGraphOperationStatus.OK);
        result = topologyTemplateOperation.associateOrAddCalcCapReqToComponent(graphVertex, calcRequirements, calcCapabilty, calCapabilitiesProps);
        assertEquals(StorageOperationStatus.OK, result);
    }

    @Test
    void testSetDataTypesFromGraph() {
        GraphVertex containerVertex = new GraphVertex();
        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreComponentInstancesInterfaces(true);
        filter.setIgnoreDataType(false);
        String componentName = "componentName";
        String componentId = UniqueIdBuilder.buildResourceUniqueId();
        containerVertex.setVertex(Mockito.mock(JanusGraphVertex.class));
        containerVertex.setJsonMetadataField(JsonPresentationFields.NAME, componentName);
        containerVertex.setUniqueId(componentId);
        containerVertex.setLabel(VertexTypeEnum.TOPOLOGY_TEMPLATE);
        when(janusGraphDao.getChildVertex(any(GraphVertex.class), any(EdgeLabelEnum.class), any(JsonParseFlagEnum.class))).thenReturn(Either.right(
            JanusGraphOperationStatus.GENERAL_ERROR));
        Either<ToscaElement, StorageOperationStatus> storageOperationStatus = topologyTemplateOperation.getToscaElement(containerVertex, filter);
        assertThat(storageOperationStatus).isEqualTo(Either.right(StorageOperationStatus.GENERAL_ERROR));
    }

    @Test
    void testSetOutputsFromGraph() {
        final GraphVertex containerVertex = new GraphVertex();
        final ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreOutputs(false);
        final String componentName = "componentName";
        final String componentId = UniqueIdBuilder.buildResourceUniqueId();
        containerVertex.setVertex(Mockito.mock(JanusGraphVertex.class));
        containerVertex.setJsonMetadataField(JsonPresentationFields.NAME, componentName);
        containerVertex.setUniqueId(componentId);
        containerVertex.setLabel(VertexTypeEnum.TOPOLOGY_TEMPLATE);
        doReturn(Either.right(JanusGraphOperationStatus.GENERAL_ERROR)).when(janusGraphDao)
            .getChildVertex(any(GraphVertex.class), any(EdgeLabelEnum.class), any(JsonParseFlagEnum.class));
        final Either<ToscaElement, StorageOperationStatus> storageOperationStatus
            = topologyTemplateOperation.getToscaElement(containerVertex, filter);
        assertThat(storageOperationStatus).isEqualTo(Either.right(StorageOperationStatus.GENERAL_ERROR));
        verify(janusGraphDao, times(1)).getChildVertex(any(GraphVertex.class), any(EdgeLabelEnum.class), any(JsonParseFlagEnum.class));
    }

    @Test
    void testUpdateDistributionStatus() {
        Either<GraphVertex, StorageOperationStatus> result;
        String uniqueId = "uniqueId";
        User user = new User();
        String userId = "userId";
        user.setUserId(userId);
        Iterator<Edge> edgeIterator = new Iterator<Edge>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public Edge next() {
                return null;
            }
        };
        GraphVertex graphVertex = Mockito.mock(GraphVertex.class);
        JanusGraphVertex janusGraphVertex = Mockito.mock(JanusGraphVertex.class);
        when(graphVertex.getVertex()).thenReturn(janusGraphVertex);
        when(janusGraphVertex.edges(Direction.IN, EdgeLabelEnum.LAST_DISTRIBUTION_STATE_MODIFIER.name())).thenReturn(edgeIterator);
        when(janusGraphDao
            .getVertexByPropertyAndLabel(GraphPropertyEnum.USERID, userId, VertexTypeEnum.USER, JsonParseFlagEnum.NoParse)).thenReturn(
            Either.left(graphVertex));
        when(janusGraphDao.getVertexById(uniqueId, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.left(graphVertex));
        when(janusGraphDao.createEdge(graphVertex, graphVertex, EdgeLabelEnum.LAST_DISTRIBUTION_STATE_MODIFIER, null)).thenReturn(
            JanusGraphOperationStatus.OK);
        when(janusGraphDao.updateVertex(graphVertex)).thenReturn(Either.left(graphVertex));
        result = topologyTemplateOperation.updateDistributionStatus(uniqueId, user, DistributionStatusEnum.DISTRIBUTED);
        assertThat(result.isLeft()).isTrue();
    }

    @Test
    void testAssociateOutputsToComponent_OK() {
        GraphVertex containerVertex = new GraphVertex();
        String componentName = "componentName";
        String componentId = UniqueIdBuilder.buildResourceUniqueId();
        containerVertex.setVertex(Mockito.mock(JanusGraphVertex.class));
        containerVertex.setJsonMetadataField(JsonPresentationFields.NAME, componentName);
        containerVertex.setUniqueId(componentId);
        containerVertex.setLabel(VertexTypeEnum.TOPOLOGY_TEMPLATE);
        final Map<String, AttributeDataDefinition> outputs = new HashMap<>();
        outputs.put("out-1", new OutputDefinition());

        when(janusGraphDao.createVertex(any(GraphVertex.class))).thenReturn(Either.left(containerVertex));
        when(janusGraphDao.createEdge(any(Vertex.class), any(Vertex.class), eq(EdgeLabelEnum.OUTPUTS), anyMap()))
            .thenReturn(JanusGraphOperationStatus.OK);

        final StorageOperationStatus result = topologyTemplateOperation.associateOutputsToComponent(containerVertex, outputs, CONTAINER_ID);
        assertEquals(StorageOperationStatus.OK, result);
    }
    @Test
    void testAssociateOutputsToComponent_Fail_createVertex() {
        GraphVertex containerVertex = new GraphVertex();
        String componentName = "componentName";
        String componentId = UniqueIdBuilder.buildResourceUniqueId();
        containerVertex.setVertex(Mockito.mock(JanusGraphVertex.class));
        containerVertex.setJsonMetadataField(JsonPresentationFields.NAME, componentName);
        containerVertex.setUniqueId(componentId);
        containerVertex.setLabel(VertexTypeEnum.TOPOLOGY_TEMPLATE);
        final Map<String, AttributeDataDefinition> outputs = new HashMap<>();
        outputs.put("out-1", new OutputDefinition());

        when(janusGraphDao.createVertex(any(GraphVertex.class))).thenReturn(Either.right(JanusGraphOperationStatus.NOT_CREATED));

        final StorageOperationStatus result = topologyTemplateOperation.associateOutputsToComponent(containerVertex, outputs, CONTAINER_ID);
        assertEquals(StorageOperationStatus.SCHEMA_ERROR, result);
    }

    @SuppressWarnings("unchecked")
    private StorageOperationStatus addPolicyToToscaElementWithStatus(JanusGraphOperationStatus status) {
        GraphVertex componentV = new GraphVertex();
        componentV.setVertex(Mockito.mock(JanusGraphVertex.class));
        GraphVertex dataV = new GraphVertex();
        dataV.setVertex(Mockito.mock(JanusGraphVertex.class));
        String componentName = "componentName";
        String componentId = UniqueIdBuilder.buildResourceUniqueId();
        String policyTypeName = "org.openecomp.policies.placement.valet.Affinity";
        componentV.setJsonMetadataField(JsonPresentationFields.NAME, componentName);
        componentV.setUniqueId(componentId);
        PolicyDefinition policy = new PolicyDefinition();
        policy.setPolicyTypeName(policyTypeName);
        int counter = 0;
        Either<GraphVertex, JanusGraphOperationStatus> toscaDataVertexRes = Either.right(
            JanusGraphOperationStatus.NOT_FOUND);
        when(janusGraphDao.getChildVertex(componentV, EdgeLabelEnum.POLICIES, JsonParseFlagEnum.ParseJson)).thenReturn(toscaDataVertexRes);
        Either<GraphVertex, JanusGraphOperationStatus> createVertex = Either.left(dataV);
        when(janusGraphDao.createVertex(any(GraphVertex.class))).thenReturn(createVertex);
        when(janusGraphDao.createEdge(any(JanusGraphVertex.class), any(JanusGraphVertex.class), any(EdgeLabelEnum.class),
            any(HashMap.class))).thenReturn(status);
        return topologyTemplateOperation.addPolicyToToscaElement(componentV, policy, counter);
    }

}
