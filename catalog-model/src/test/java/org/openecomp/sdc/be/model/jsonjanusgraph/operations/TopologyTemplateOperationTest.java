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

import org.assertj.core.api.Assertions;
import org.janusgraph.core.JanusGraphVertex;
import fj.data.Either;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.MapCapabilityProperty;
import org.openecomp.sdc.be.datatypes.elements.MapListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
//error scenarios
public class TopologyTemplateOperationTest {

    private static final String CONTAINER_ID = "containerId";
    @InjectMocks
    private TopologyTemplateOperation topologyTemplateOperation;
    @Mock
    private JanusGraphDao janusGraphDao;

    @Test
    public void overrideToscaDataOfToscaElement_failedToFetchContainerVertex() {
        when(janusGraphDao.getVertexById(CONTAINER_ID, JsonParseFlagEnum.NoParse)).thenReturn(Either.right(
            JanusGraphOperationStatus.INVALID_ID));
        StorageOperationStatus storageOperationStatus = topologyTemplateOperation.overrideToscaDataOfToscaElement(CONTAINER_ID, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, Collections.emptyMap());
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.INVALID_ID);
    }

    @Test
    public void overrideToscaDataOfToscaElement_failedToFetchDataVertex() {
        GraphVertex containerVertex = new GraphVertex();
        when(janusGraphDao.getVertexById(CONTAINER_ID, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(containerVertex));
        when(janusGraphDao.getChildVertex(containerVertex, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, JsonParseFlagEnum.ParseJson)).thenReturn(Either.right(
            JanusGraphOperationStatus.NOT_FOUND));
        StorageOperationStatus storageOperationStatus = topologyTemplateOperation.overrideToscaDataOfToscaElement(CONTAINER_ID, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, Collections.emptyMap());
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.NOT_FOUND);
    }

    @Test
    public void updateToscaDataDeepElements_failedToFetchContainerVertex() {
        when(janusGraphDao.getVertexById(CONTAINER_ID, JsonParseFlagEnum.NoParse)).thenReturn(Either.right(
            JanusGraphOperationStatus.INVALID_ID));
        StorageOperationStatus storageOperationStatus = topologyTemplateOperation.updateToscaDataDeepElementsBlockToToscaElement(CONTAINER_ID, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, new MapCapabilityProperty(), "");
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.INVALID_ID);
    }

    @Test
    public void updateToscaDataDeepElements_failedToFetchDataVertex() {
        GraphVertex containerVertex = new GraphVertex();
        when(janusGraphDao.getVertexById(CONTAINER_ID, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(containerVertex));
        when(janusGraphDao.getChildVertex(containerVertex, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, JsonParseFlagEnum.ParseJson)).thenReturn(Either.right(
            JanusGraphOperationStatus.NOT_FOUND));
        StorageOperationStatus storageOperationStatus = topologyTemplateOperation.updateToscaDataDeepElementsBlockToToscaElement(CONTAINER_ID, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, new MapCapabilityProperty(), "");
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.NOT_FOUND);
    }

    @Test
    public void addPolicyToToscaElementSuccessTest(){
        JanusGraphOperationStatus status = JanusGraphOperationStatus.OK;
        StorageOperationStatus result = addPolicyToToscaElementWithStatus(status);
        assertThat(result).isEqualTo(StorageOperationStatus.OK);
    }

    @Test
    public void addPolicyToToscaElementFailureTest(){
        JanusGraphOperationStatus status = JanusGraphOperationStatus.ALREADY_EXIST;
        StorageOperationStatus result = addPolicyToToscaElementWithStatus(status);
        assertThat(result).isEqualTo(StorageOperationStatus.ENTITY_ALREADY_EXISTS);
    }

    @Test
    public void testAssociateOrAddCalcCapReqToComponent() {
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
    public void testSetDataTypesFromGraph() {
        GraphVertex containerVertex = new GraphVertex();
        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreComponentInstancesInterfaces(true);
        filter.setIgnoreDataType(false);
        String componentName = "componentName";
        String componentId = UniqueIdBuilder.buildResourceUniqueId();
        containerVertex.setVertex(mock(JanusGraphVertex.class));
        containerVertex.setJsonMetadataField(JsonPresentationFields.NAME, componentName);
        containerVertex.setUniqueId(componentId);
        containerVertex.setLabel(VertexTypeEnum.TOPOLOGY_TEMPLATE);
        when(janusGraphDao.getChildVertex(any(GraphVertex.class), any(EdgeLabelEnum.class), any(JsonParseFlagEnum.class))).thenReturn(Either.right(
            JanusGraphOperationStatus.GENERAL_ERROR));
        Either<ToscaElement, StorageOperationStatus> storageOperationStatus = topologyTemplateOperation.getToscaElement(containerVertex, filter);
        assertThat(storageOperationStatus).isEqualTo(Either.right(StorageOperationStatus.GENERAL_ERROR));
    }

    @Test
    public void testSetOutputsFromGraph() {
        final GraphVertex containerVertex = new GraphVertex();
        final ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreOutputs(false);
        final String componentName = "componentName";
        final String componentId = UniqueIdBuilder.buildResourceUniqueId();
        containerVertex.setVertex(mock(JanusGraphVertex.class));
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
    public void testAssociatePropertiesToComponent() {
        String componentId = UniqueIdBuilder.buildResourceUniqueId();
        GraphVertex containerVertex = new GraphVertex();
        containerVertex.setVertex(mock(JanusGraphVertex.class));
        containerVertex.setUniqueId(componentId);
        Map<String, PropertyDataDefinition> propertiesMap = new HashMap<>();
        PropertyDataDefinition propertyDataDefinition = new PropertyDataDefinition();
        propertyDataDefinition.setType("string");
        propertyDataDefinition.setDescription("Identifier of this NS descriptor");
        propertiesMap.put("descriptor_id", propertyDataDefinition);

        GraphVertex dataV = new GraphVertex();
        dataV.setVertex(mock(JanusGraphVertex.class));

        when(janusGraphDao.createVertex(any(GraphVertex.class))).thenReturn(Either.left(dataV));
        when(janusGraphDao.createEdge(containerVertex.getVertex(), dataV.getVertex(), EdgeLabelEnum.PROPERTIES, new HashMap<>()))
            .thenReturn(JanusGraphOperationStatus.OK);

        StorageOperationStatus status = topologyTemplateOperation.associatePropertiesToComponent(containerVertex, propertiesMap, componentId);
        assertEquals(StorageOperationStatus.OK, status);
        verify(janusGraphDao, times(1)).createVertex(any(GraphVertex.class));
        verify(janusGraphDao, times(1)).createEdge(any(JanusGraphVertex.class), any(JanusGraphVertex.class),
            any(EdgeLabelEnum.class), any(HashMap.class));
    }

    @Test
    public void testAssociatePropertiesToComponentFail() {
        String componentId = UniqueIdBuilder.buildResourceUniqueId();
        GraphVertex containerVertex = new GraphVertex();
        containerVertex.setVertex(mock(JanusGraphVertex.class));
        containerVertex.setUniqueId(componentId);
        Map<String, PropertyDataDefinition> propertiesMap = new HashMap<>();
        PropertyDataDefinition propertyDataDefinition = new PropertyDataDefinition();
        propertyDataDefinition.setType("string");
        propertyDataDefinition.setDescription("Identifier of this NS descriptor");
        propertiesMap.put("descriptor_id", propertyDataDefinition);

        GraphVertex dataV = new GraphVertex();
        dataV.setVertex(mock(JanusGraphVertex.class));

        when(janusGraphDao.createVertex(any(GraphVertex.class))).thenReturn(Either.right(JanusGraphOperationStatus.GENERAL_ERROR));

        StorageOperationStatus status = topologyTemplateOperation.associatePropertiesToComponent(containerVertex, propertiesMap, componentId);
        assertThat(status).isEqualTo(StorageOperationStatus.GENERAL_ERROR);
    }


    @Test
    public void testUpdateDistributionStatus() {
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
        GraphVertex graphVertex = mock(GraphVertex.class);
        JanusGraphVertex janusGraphVertex = mock(JanusGraphVertex.class);
        when(graphVertex.getVertex()).thenReturn(janusGraphVertex);
        when(janusGraphVertex.edges(Direction.IN, EdgeLabelEnum.LAST_DISTRIBUTION_STATE_MODIFIER.name())).thenReturn(edgeIterator);
        when(janusGraphDao
            .getVertexByPropertyAndLabel(GraphPropertyEnum.USERID, userId, VertexTypeEnum.USER, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(graphVertex));
        when(janusGraphDao.getVertexById(uniqueId, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.left(graphVertex));
        when(janusGraphDao.createEdge(graphVertex, graphVertex, EdgeLabelEnum.LAST_DISTRIBUTION_STATE_MODIFIER, null)).thenReturn(
            JanusGraphOperationStatus.OK);
        when(janusGraphDao.updateVertex(graphVertex)).thenReturn(Either.left(graphVertex));
        result = topologyTemplateOperation.updateDistributionStatus(uniqueId, user, DistributionStatusEnum.DISTRIBUTED);
        assertThat(result.isLeft()).isTrue();
    }

    @SuppressWarnings("unchecked")
    private StorageOperationStatus addPolicyToToscaElementWithStatus(JanusGraphOperationStatus status) {
        GraphVertex componentV = new GraphVertex();
        componentV.setVertex(mock(JanusGraphVertex.class));
        GraphVertex dataV = new GraphVertex();
        dataV.setVertex(mock(JanusGraphVertex.class));
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
        when(janusGraphDao.createEdge(any(JanusGraphVertex.class), any(JanusGraphVertex.class), any(EdgeLabelEnum.class), any(HashMap.class))).thenReturn(status);
        return topologyTemplateOperation.addPolicyToToscaElement(componentV, policy, counter);
    }

}
