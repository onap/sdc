package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import org.janusgraph.core.JanusGraphVertex;
import fj.data.Either;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.MapCapabilityProperty;
import org.openecomp.sdc.be.datatypes.elements.MapListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListRequirementDataDefinition;
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
import static org.mockito.ArgumentMatchers.eq;
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
        GraphVertex graphVertex = Mockito.mock(GraphVertex.class);
        JanusGraphVertex janusGraphVertex = Mockito.mock(JanusGraphVertex.class);
        when(graphVertex.getVertex()).thenReturn(janusGraphVertex);
        when(janusGraphVertex.edges(Direction.IN, EdgeLabelEnum.LAST_DISTRIBUTION_STATE_MODIFIER.name())).thenReturn(edgeIterator);
        when(janusGraphDao
            .getVertexByPropertyAndLabel(GraphPropertyEnum.USERID, userId, VertexTypeEnum.USER, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(graphVertex));
        when(janusGraphDao.getVertexById(uniqueId, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.left(graphVertex));
        when(janusGraphDao.createEdge(graphVertex, graphVertex, EdgeLabelEnum.LAST_DISTRIBUTION_STATE_MODIFIER, null)).thenReturn(
            JanusGraphOperationStatus.OK);
        when(janusGraphDao.updateVertex(graphVertex)).thenReturn(Either.left(graphVertex));
        result = topologyTemplateOperation.updateDistributionStatus(uniqueId, user, DistributionStatusEnum.DISTRIBUTED);
        assertThat(result.isLeft());
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
        when(janusGraphDao.getChildVertex(eq(componentV), eq(EdgeLabelEnum.POLICIES), eq(JsonParseFlagEnum.ParseJson))).thenReturn(toscaDataVertexRes);
        Either<GraphVertex, JanusGraphOperationStatus> createVertex = Either.left(dataV);
        when(janusGraphDao.createVertex(any(GraphVertex.class))).thenReturn(createVertex);
        when(janusGraphDao.createEdge(any(JanusGraphVertex.class), any(JanusGraphVertex.class), any(EdgeLabelEnum.class), any(HashMap.class))).thenReturn(status);
        return topologyTemplateOperation.addPolicyToToscaElement(componentV, policy, counter);
    }

}
