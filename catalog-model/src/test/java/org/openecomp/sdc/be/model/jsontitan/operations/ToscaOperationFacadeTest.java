package org.openecomp.sdc.be.model.jsontitan.operations;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.jsontitan.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ToscaOperationFacadeTest {

    @InjectMocks
    private ToscaOperationFacade testInstance;

    @Mock
    private TitanDao titanDaoMock;

    @Mock
    private TopologyTemplateOperation topologyTemplateOperationMock;

    @Before
    public void setUp() throws Exception {
        testInstance = new ToscaOperationFacade();
        MockitoAnnotations.initMocks(this);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void fetchMetaDataByResourceType() throws Exception {
        ArgumentCaptor<Map> criteriaCapture = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map> criteriaNotCapture = ArgumentCaptor.forClass(Map.class);
        ComponentParametersView dataFilter = new ComponentParametersView();
        List<GraphVertex> mockVertices = getMockVertices(2);
        Either<List<GraphVertex>, TitanOperationStatus> returnedVertices = Either.left(mockVertices);

        when(titanDaoMock.getByCriteria(eq(null), criteriaCapture.capture(), criteriaNotCapture.capture(), eq(JsonParseFlagEnum.ParseMetadata))).thenReturn(returnedVertices);
        when(topologyTemplateOperationMock.getToscaElement(mockVertices.get(0), dataFilter)).thenReturn(Either.left(getResourceToscaElement("0")));
        when(topologyTemplateOperationMock.getToscaElement(mockVertices.get(1), dataFilter)).thenReturn(Either.left(getResourceToscaElement("1")));
        Either<List<Component>, StorageOperationStatus> fetchedComponents = testInstance.fetchMetaDataByResourceType(ResourceTypeEnum.VF.getValue(), dataFilter);

        verifyCriteriaForHighestVersionAndVfResourceType(criteriaCapture);
        verifyCriteriaNotIsDeleted(criteriaNotCapture);

        assertTrue(fetchedComponents.isLeft());
        List<Component> cmpts = fetchedComponents.left().value();
        assertEquals(2, cmpts.size());
        assertEquals("0", cmpts.get(0).getUniqueId());
        assertEquals("1", cmpts.get(1).getUniqueId());
    }

    private void verifyCriteriaForHighestVersionAndVfResourceType(ArgumentCaptor<Map> criteriaCapture) {
        Map<GraphPropertyEnum, Object> criteria = (Map<GraphPropertyEnum, Object>)criteriaCapture.getValue();
        assertEquals(2, criteria.size());
        assertEquals(criteria.get(GraphPropertyEnum.RESOURCE_TYPE), "VF");
        assertEquals(criteria.get(GraphPropertyEnum.IS_HIGHEST_VERSION), true);
    }

    private void verifyCriteriaNotIsDeleted(ArgumentCaptor<Map> criteriaNotCapture) {
        Map<GraphPropertyEnum, Object> notCriteria = (Map<GraphPropertyEnum, Object>)criteriaNotCapture.getValue();
        assertEquals(1, notCriteria.size());
        assertEquals(notCriteria.get(GraphPropertyEnum.IS_DELETED), true);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void fetchMetaDataByResourceType_failedToGetData() throws Exception {
        when(titanDaoMock.getByCriteria(eq(null), anyMap(), anyMap(), eq(JsonParseFlagEnum.ParseMetadata))).thenReturn(Either.right(TitanOperationStatus.GENERAL_ERROR));
        Either<List<Component>, StorageOperationStatus> fetchedComponents = testInstance.fetchMetaDataByResourceType(ResourceTypeEnum.VF.getValue(), new ComponentParametersView());
        assertTrue(fetchedComponents.isRight());
        assertEquals(StorageOperationStatus.GENERAL_ERROR, fetchedComponents.right().value());
    }
    
    @Test
    public void associatePolicyToComponentSuccessTest(){
        Either<PolicyDefinition, StorageOperationStatus> result = associatePolicyToComponentWithStatus(StorageOperationStatus.OK);
        assertTrue(result.isLeft());
    }

    @Test
    public void associatePolicyToComponentFailureTest(){
        Either<PolicyDefinition, StorageOperationStatus> result = associatePolicyToComponentWithStatus(StorageOperationStatus.BAD_REQUEST);
        assertTrue(result.isRight() && result.right().value() == StorageOperationStatus.BAD_REQUEST);
    }

    @Test
    public void updatePolicyOfComponentSuccessTest(){
        Either<PolicyDefinition, StorageOperationStatus> result = updatePolicyOfComponentWithStatus(StorageOperationStatus.OK);
        assertTrue(result.isLeft());
    }

    @Test
    public void updatePolicyOfComponentFailureTest(){
        Either<PolicyDefinition, StorageOperationStatus> result = updatePolicyOfComponentWithStatus(StorageOperationStatus.NOT_FOUND);
        assertTrue(result.isRight() && result.right().value() == StorageOperationStatus.NOT_FOUND);
    }
    
    @Test
    public void removePolicyFromComponentSuccessTest(){
        removePolicyFromComponentWithStatus(StorageOperationStatus.OK);
    }
    
    @Test
    public void removePolicyFromComponentFailureTest(){
        removePolicyFromComponentWithStatus(StorageOperationStatus.NOT_FOUND);
    }
    
    private Either<PolicyDefinition, StorageOperationStatus> associatePolicyToComponentWithStatus(StorageOperationStatus status) {
        PolicyDefinition policy = new PolicyDefinition();
        String componentId = "componentId";
        int counter = 0;
        GraphVertex vertex;
        if(status == StorageOperationStatus.OK){
            vertex = getTopologyTemplateVertex();
        } else {
            vertex = getNodeTypeVertex();
        }
        Either<GraphVertex, TitanOperationStatus> getVertexEither = Either.left(vertex);
        when(titanDaoMock.getVertexById(eq(componentId), eq(JsonParseFlagEnum.ParseMetadata))).thenReturn(getVertexEither);
        when(topologyTemplateOperationMock.addPolicyToToscaElement(eq(vertex), any(PolicyDefinition.class), anyInt())).thenReturn(status);
        return testInstance.associatePolicyToComponent(componentId, policy, counter);
    }
    
    private Either<PolicyDefinition, StorageOperationStatus> updatePolicyOfComponentWithStatus(StorageOperationStatus status) {
        PolicyDefinition policy = new PolicyDefinition();
        String componentId = "componentId";
        GraphVertex vertex = getTopologyTemplateVertex();
        when(titanDaoMock.getVertexById(eq(componentId), eq(JsonParseFlagEnum.NoParse))).thenReturn(Either.left(vertex));
        when(topologyTemplateOperationMock.updatePolicyOfToscaElement(eq(vertex), any(PolicyDefinition.class))).thenReturn(status);
        return testInstance.updatePolicyOfComponent(componentId, policy);
    }

    private void removePolicyFromComponentWithStatus(StorageOperationStatus status) {
        String componentId = "componentId";
        String policyId = "policyId";
        GraphVertex vertex = getTopologyTemplateVertex();
        Either<GraphVertex, TitanOperationStatus> getVertexEither = Either.left(vertex);
        when(titanDaoMock.getVertexById(eq(componentId), eq(JsonParseFlagEnum.NoParse))).thenReturn(getVertexEither);
        when(topologyTemplateOperationMock.removePolicyFromToscaElement(eq(vertex), eq(policyId))).thenReturn(status);
        StorageOperationStatus result = testInstance.removePolicyFromComponent(componentId, policyId);
        assertSame(result, status);
    }
    
    private List<GraphVertex> getMockVertices(int numOfVertices) {
        return IntStream.range(0, numOfVertices).mapToObj(i -> getTopologyTemplateVertex()).collect(Collectors.toList());
    }

    private ToscaElement getResourceToscaElement(String id) {
        ToscaElement toscaElement = new TopologyTemplate();
        toscaElement.setMetadata(new HashMap<>());
        toscaElement.getMetadata().put(JsonPresentationFields.COMPONENT_TYPE.getPresentation(), "RESOURCE");
        toscaElement.getMetadata().put(JsonPresentationFields.UNIQUE_ID.getPresentation(), id);
        return toscaElement;
    }

    private GraphVertex getTopologyTemplateVertex() {
        GraphVertex graphVertex = new GraphVertex();
        graphVertex.setLabel(VertexTypeEnum.TOPOLOGY_TEMPLATE);
        return graphVertex;
    }
    
    private GraphVertex getNodeTypeVertex() {
        GraphVertex graphVertex = new GraphVertex();
        graphVertex.setLabel(VertexTypeEnum.NODE_TYPE);
        return graphVertex;
    }
}