package org.openecomp.sdc.be.dao.jsongraph;

import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphVertex;
import fj.data.Either;
import mockit.Deencapsulation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgePropertyEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphClient;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JanusGraphDaoMockTest {

	@InjectMocks
	JanusGraphDao testSubject;

	@Mock
	JanusGraphClient janusGraphClient;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testCommit() throws Exception {
		JanusGraphOperationStatus result;

		// default test
		result = testSubject.commit();
	}

	@Test
	public void testRollback() throws Exception {
		JanusGraphOperationStatus result;

		// default test
		result = testSubject.rollback();
	}

	@Test
	public void testGetGraph() throws Exception {

		Either<JanusGraph, JanusGraphOperationStatus> result;

		// default test

		result = testSubject.getGraph();
	}

	@Test
	public void testCreateVertex() throws Exception {

		GraphVertex graphVertex = new GraphVertex();
		graphVertex.setLabel(VertexTypeEnum.ADDITIONAL_INFORMATION);
		Either<GraphVertex, JanusGraphOperationStatus> result;

		JanusGraph tg = Mockito.mock(JanusGraph.class);
		Either<JanusGraph, JanusGraphOperationStatus> value = Either.left(tg);
		// default test
		JanusGraphVertex value2 = Mockito.mock(JanusGraphVertex.class);
		Mockito.when(tg.addVertex()).thenReturn(value2);
		Mockito.when(janusGraphClient.getGraph()).thenReturn(value);
		result = testSubject.createVertex(graphVertex);
	}

	@Test
	public void testCreateVertexErrorGetGraph() throws Exception {

		GraphVertex graphVertex = new GraphVertex();
		graphVertex.setLabel(VertexTypeEnum.ADDITIONAL_INFORMATION);
		Either<GraphVertex, JanusGraphOperationStatus> result;

		Either<JanusGraph, JanusGraphOperationStatus> value = Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
		// default test
		Mockito.when(janusGraphClient.getGraph()).thenReturn(value);
		result = testSubject.createVertex(graphVertex);
	}

	@Test
	public void testCreateVertexException() throws Exception {

		GraphVertex graphVertex = new GraphVertex();
		graphVertex.setLabel(VertexTypeEnum.ADDITIONAL_INFORMATION);
		Either<GraphVertex, JanusGraphOperationStatus> result;

		JanusGraph tg = Mockito.mock(JanusGraph.class);
		Either<JanusGraph, JanusGraphOperationStatus> value = Either.left(tg);
		// default test
		Mockito.when(tg.addVertex()).thenThrow(RuntimeException.class);
		Mockito.when(janusGraphClient.getGraph()).thenReturn(value);
		result = testSubject.createVertex(graphVertex);
	}

	@Test
	public void testGetVertexByPropertyAndLabel() throws Exception {
		Either<GraphVertex, JanusGraphOperationStatus> result;

		// default test
		Mockito.when(janusGraphClient.getGraph()).thenReturn(Either.right(JanusGraphOperationStatus.GENERAL_ERROR));
		result = testSubject.getVertexByPropertyAndLabel(GraphPropertyEnum.COMPONENT_TYPE, "mock",
				VertexTypeEnum.ADDITIONAL_INFORMATION);
	}

	@Test
	public void testGetFirstFoundVertex() throws Exception {
		Iterable<JanusGraphVertex> vertices = Mockito.mock(Iterable.class);
		Either<GraphVertex, JanusGraphOperationStatus> result;

		Iterator<JanusGraphVertex> value = Mockito.mock(Iterator.class);
		Mockito.when(vertices.iterator()).thenReturn(value);
		Mockito.when(value.hasNext()).thenReturn(true);
		JanusGraphVertex value2 = Mockito.mock(JanusGraphVertex.class);
		Mockito.when(value.next()).thenReturn(value2);

		// default test
		result = Deencapsulation.invoke(testSubject, "getFirstFoundVertex", JsonParseFlagEnum.NoParse, vertices);
	}

	@Test
	public void testGetFirstFoundVertexNotFound() throws Exception {
		Iterable<JanusGraphVertex> vertices = Mockito.mock(Iterable.class);
		Either<GraphVertex, JanusGraphOperationStatus> result;

		Iterator<JanusGraphVertex> value = Mockito.mock(Iterator.class);
		Mockito.when(vertices.iterator()).thenReturn(value);
		Mockito.when(value.hasNext()).thenReturn(false);
		JanusGraphVertex value2 = Mockito.mock(JanusGraphVertex.class);
		Mockito.when(value.next()).thenReturn(value2);

		// default test
		result = Deencapsulation.invoke(testSubject, "getFirstFoundVertex", JsonParseFlagEnum.NoParse, vertices);
	}

	@Test
	public void testGetVertexById_1Exception() throws Exception {

		String id = "mock";
		Either<GraphVertex, JanusGraphOperationStatus> result;

		JanusGraph tg = Mockito.mock(JanusGraph.class);
		Either<JanusGraph, JanusGraphOperationStatus> value = Either.left(tg);
		// default test
		JanusGraphVertex value2 = Mockito.mock(JanusGraphVertex.class);
		Mockito.when(tg.addVertex()).thenReturn(value2);
		Mockito.when(janusGraphClient.getGraph()).thenReturn(value);

		// test 1
		result = testSubject.getVertexById(id, JsonParseFlagEnum.NoParse);
		// Assert.assertEquals(null, result);
	}

	@Test
	public void testGetVertexById_1GraphClosed() throws Exception {

		String id = "mock";
		Either<GraphVertex, JanusGraphOperationStatus> result;

		Object b;
		Either<JanusGraph, JanusGraphOperationStatus> value = Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
		// default test
		JanusGraphVertex value2 = Mockito.mock(JanusGraphVertex.class);
		Mockito.when(janusGraphClient.getGraph()).thenReturn(value);

		// test 1
		result = testSubject.getVertexById(id, JsonParseFlagEnum.NoParse);
		// Assert.assertEquals(null, result);
	}

	@Test
	public void testSetVertexProperties_1() throws Exception {
		Vertex vertex = Mockito.mock(Vertex.class);
		Map<String, Object> properties = new HashMap<>();
		properties.put("mock", "mock");

		// default test
		testSubject.setVertexProperties(vertex, properties);
	}

	@Test
	public void testCreateAndFill() throws Exception {

		JanusGraphVertex vertex = Mockito.mock(JanusGraphVertex.class);
		JsonParseFlagEnum parseFlag = null;
		GraphVertex result;

		// default test

		result = Deencapsulation.invoke(testSubject, "createAndFill", vertex, JsonParseFlagEnum.NoParse);
	}

	@Test
	public void testParseVertexProperties() throws Exception {

		GraphVertex graphVertex = new GraphVertex();
		JanusGraphVertex vertex = Mockito.mock(JanusGraphVertex.class);
		graphVertex.setVertex(vertex);
		JsonParseFlagEnum parseFlag = null;

		// default test

		testSubject.parseVertexProperties(graphVertex, JsonParseFlagEnum.NoParse);
	}


	@Test
	public void testCreateEdge() throws Exception {

		GraphVertex from = Mockito.mock(GraphVertex.class);
		GraphVertex to = Mockito.mock(GraphVertex.class);
		
		JanusGraphVertex value = Mockito.mock(JanusGraphVertex.class);
		Mockito.when(from.getVertex()).thenReturn(value);
		Mockito.when(to.getVertex()).thenReturn(value);
		Map<EdgePropertyEnum, Object> properties = new HashMap<>();
		JanusGraphOperationStatus result;

		// default test

		result = testSubject.createEdge(from, to, EdgeLabelEnum.ADDITIONAL_INFORMATION, properties);
		from = new GraphVertex();
		to = new GraphVertex();
		result = testSubject.createEdge(from, to, EdgeLabelEnum.ADDITIONAL_INFORMATION, properties);
	}

	@Test
	public void testSetEdgeProperties() throws Exception {

		Element element = Mockito.mock(Element.class);
		Map<EdgePropertyEnum, Object> properties = new HashMap<>();

		// test 1

		properties.put(EdgePropertyEnum.STATE, "mock");
		testSubject.setEdgeProperties(element, properties);
	}

	@Test
	public void testGetByCriteria() throws Exception {
		Map<GraphPropertyEnum, Object> props = new HashMap<>();
		Either<List<GraphVertex>, JanusGraphOperationStatus> result;

		JanusGraph tg = Mockito.mock(JanusGraph.class);
		Either<JanusGraph, JanusGraphOperationStatus> value = Either.left(tg);
		JanusGraphVertex value2 = Mockito.mock(JanusGraphVertex.class);
		Mockito.when(tg.addVertex()).thenReturn(value2);
		Mockito.when(janusGraphClient.getGraph()).thenReturn(value);
		
		// default test
		result = testSubject.getByCriteria(VertexTypeEnum.ADDITIONAL_INFORMATION, props);
	}

	@Test
	public void testGetByCriteria_1() throws Exception {

		Map<GraphPropertyEnum, Object> props = new HashMap<>();
		Either<List<GraphVertex>, JanusGraphOperationStatus> result;

		Either<JanusGraph, JanusGraphOperationStatus> value = Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
		JanusGraphVertex value2 = Mockito.mock(JanusGraphVertex.class);
		Mockito.when(janusGraphClient.getGraph()).thenReturn(value);

		// default test
		result = testSubject.getByCriteria(VertexTypeEnum.ADDITIONAL_INFORMATION, props, JsonParseFlagEnum.NoParse);
	}

	@Test
	public void testGetCatalogVerticies() throws Exception {
		Either<Iterator<Vertex>, JanusGraphOperationStatus> result;

		Either<JanusGraph, JanusGraphOperationStatus> value = Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
		// default test
		JanusGraphVertex value2 = Mockito.mock(JanusGraphVertex.class);
		Mockito.when(janusGraphClient.getGraph()).thenReturn(value);
		
		// default test
		result = testSubject.getCatalogOrArchiveVerticies(true);
	}

	@Test
	public void testGetChildVertex() throws Exception {

		GraphVertex parentVertex = new GraphVertex();
		EdgeLabelEnum edgeLabel = null;
		JsonParseFlagEnum parseFlag = null;
		Either<GraphVertex, JanusGraphOperationStatus> result;

		Either<JanusGraph, JanusGraphOperationStatus> value = Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
		JanusGraphVertex value2 = Mockito.mock(JanusGraphVertex.class);
		Mockito.when(janusGraphClient.getGraph()).thenReturn(value);
		
		// default test
		result = testSubject.getChildVertex(parentVertex, EdgeLabelEnum.ADDITIONAL_INFORMATION, JsonParseFlagEnum.NoParse);
	}

	@Test
	public void testGetChildVertex_1() throws Exception {

		Vertex parentVertex = null;
		EdgeLabelEnum edgeLabel = null;
		JsonParseFlagEnum parseFlag = null;
		Either<Vertex, JanusGraphOperationStatus> result;

		Either<JanusGraph, JanusGraphOperationStatus> value = Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
		JanusGraphVertex value2 = Mockito.mock(JanusGraphVertex.class);
		Mockito.when(janusGraphClient.getGraph()).thenReturn(value);

		// default test
		result = testSubject.getChildVertex(parentVertex, edgeLabel, parseFlag);
	}


	@Test
	public void testGetParentVertex_1() throws Exception {

		Vertex parentVertex = null;
		EdgeLabelEnum edgeLabel = null;
		JsonParseFlagEnum parseFlag = null;
		Either<Vertex, JanusGraphOperationStatus> result;

		// default test

		result = testSubject.getParentVertex(parentVertex, edgeLabel, parseFlag);
	}

	@Test
	public void testGetParentVertecies_1() throws Exception {

		Vertex parentVertex = null;
		EdgeLabelEnum edgeLabel = null;
		JsonParseFlagEnum parseFlag = null;
		Either<List<Vertex>, JanusGraphOperationStatus> result;

		// default test

		result = testSubject.getParentVertecies(parentVertex, edgeLabel, parseFlag);
	}

	@Test
	public void testGetAdjacentVerticies() throws Exception {

		Vertex parentVertex = null;
		EdgeLabelEnum edgeLabel = null;
		JsonParseFlagEnum parseFlag = null;
		Direction direction = null;
		Either<List<Vertex>, JanusGraphOperationStatus> result;

		Either<JanusGraph, JanusGraphOperationStatus> value = Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
		JanusGraphVertex value2 = Mockito.mock(JanusGraphVertex.class);
		Mockito.when(janusGraphClient.getGraph()).thenReturn(value);
		// default test
		result = Deencapsulation.invoke(testSubject, "getAdjacentVerticies",
				new Object[] { Vertex.class, EdgeLabelEnum.class, JsonParseFlagEnum.class, Direction.class });
	}

	@Test
	public void testGetChildrenVertecies_1() throws Exception {

		Vertex parentVertex = null;
		EdgeLabelEnum edgeLabel = null;
		JsonParseFlagEnum parseFlag = null;
		Either<List<Vertex>, JanusGraphOperationStatus> result;

		// default test

		result = testSubject.getChildrenVertecies(parentVertex, edgeLabel, parseFlag);
	}


	@Test
	public void testDeleteBelongingEdgeByCriteria() throws Exception {

		GraphVertex vertex = null;
		EdgeLabelEnum label = null;
		Map<GraphPropertyEnum, Object> properties = null;
		Either<Edge, JanusGraphOperationStatus> result;

		// default test

		result = testSubject.deleteBelongingEdgeByCriteria(vertex, label, properties);
	}

	@Test
	public void testDeleteEdge() throws Exception {

		GraphVertex fromVertex = new GraphVertex();
		GraphVertex toVertex = new GraphVertex();
		Either<Edge, JanusGraphOperationStatus> result;

		Either<JanusGraph, JanusGraphOperationStatus> value = Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
		JanusGraphVertex value2 = Mockito.mock(JanusGraphVertex.class);
		Mockito.when(janusGraphClient.getGraph()).thenReturn(value);
		
		// default test
		result = testSubject.deleteEdge(fromVertex, toVertex, EdgeLabelEnum.ADDITIONAL_INFORMATION);
	}

	@Test
	public void testDeleteEdgeByDirection() throws Exception {
		GraphVertex fromVertex = new GraphVertex();
		JanusGraphOperationStatus result;

		// default test
		result = testSubject.deleteEdgeByDirection(fromVertex, Direction.BOTH, EdgeLabelEnum.ADDITIONAL_INFORMATION);
	}

	@Test
	public void testDeleteEdgeByDirectionMock() throws Exception {
		GraphVertex fromVertex = Mockito.mock(GraphVertex.class);
		JanusGraphOperationStatus result;

		JanusGraphVertex value = Mockito.mock(JanusGraphVertex.class);;
		Mockito.when(fromVertex.getVertex()).thenReturn(value);
		Iterator<Edge> value2 = Mockito.mock(Iterator.class);;
		Mockito.when(value.edges(Mockito.any(), Mockito.any())).thenReturn(value2);
		Mockito.when(value2.hasNext()).thenReturn(true, false);
		Edge value3 = Mockito.mock(Edge.class);;
		Mockito.when(value2.next()).thenReturn(value3);
		// default test
		result = testSubject.deleteEdgeByDirection(fromVertex, Direction.BOTH, EdgeLabelEnum.ADDITIONAL_INFORMATION);
	}
	
	@Test
	public void testUpdateVertex() throws Exception {

		GraphVertex graphVertex = new GraphVertex();
		Either<GraphVertex, JanusGraphOperationStatus> result;

		// default test

		result = testSubject.updateVertex(graphVertex);
	}

	@Test
	public void testGetVerticesByUniqueIdAndParseFlag() throws Exception {

		Map<String, ImmutablePair<GraphPropertyEnum, JsonParseFlagEnum>> verticesToGet = new HashMap<>();
		Either<Map<String, GraphVertex>, JanusGraphOperationStatus> result;
		
		// default test
		result = testSubject.getVerticesByUniqueIdAndParseFlag(verticesToGet);
		ImmutablePair<GraphPropertyEnum, JsonParseFlagEnum> value3 = ImmutablePair.of(GraphPropertyEnum.COMPONENT_TYPE, JsonParseFlagEnum.NoParse);
		verticesToGet.put("mock", value3);
		try {
			result = testSubject.getVerticesByUniqueIdAndParseFlag(verticesToGet);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testCreateEdge_2() throws Exception {

		Vertex from = null;
		Vertex to = null;
		EdgeLabelEnum label = null;
		Edge edgeToCopy = null;
		JanusGraphOperationStatus result;

		// default test

		result = testSubject.createEdge(from, to, label, edgeToCopy);
	}


	@Test
	public void testReplaceEdgeLabel() throws Exception {

		Vertex fromVertex = null;
		Vertex toVertex = null;
		Edge prevEdge = null;
		EdgeLabelEnum prevLabel = null;
		EdgeLabelEnum newLabel = null;
		JanusGraphOperationStatus result;

		// default test

		result = testSubject.replaceEdgeLabel(fromVertex, toVertex, prevEdge, prevLabel, newLabel);
	}

	@Test
	public void testUpdateVertexMetadataPropertiesWithJson() throws Exception {

		Vertex vertex = Mockito.mock(Vertex.class);;
		Map<GraphPropertyEnum, Object> properties = new HashMap<>();
		properties.put(GraphPropertyEnum.COMPONENT_TYPE, "mock");
		JanusGraphOperationStatus result;

		// default test

		result = testSubject.updateVertexMetadataPropertiesWithJson(vertex, properties);
	}

	//TODO Last
	@Test
	public void testDisassociateAndDeleteLast() throws Exception {

		GraphVertex vertex = Mockito.mock(GraphVertex.class);
		JanusGraphOperationStatus result;
		
		JanusGraphVertex value = Mockito.mock(JanusGraphVertex.class);
		Iterator<Edge> mockiter = Mockito.mock(Iterator.class);
		Edge nextmock = Mockito.mock(Edge.class);
		Mockito.when(vertex.getVertex()).thenReturn(value);
		Mockito.when(value.edges(Mockito.any(), Mockito.any())).thenReturn(mockiter);
		Mockito.when(mockiter.hasNext()).thenReturn(true, false);
		Mockito.when(mockiter.next()).thenReturn(nextmock);
		Vertex secondVertex = Mockito.mock(Vertex.class);
		Mockito.when(nextmock.outVertex()).thenReturn(secondVertex);
		Mockito.when(nextmock.inVertex()).thenReturn(secondVertex);
		Iterator<Edge> restOfEdges = Mockito.mock(Iterator.class);
		Mockito.when(secondVertex.edges(Mockito.any(), Mockito.any())).thenReturn(restOfEdges);
		Mockito.when(restOfEdges.hasNext()).thenReturn(false);
		
		// default test
		result = testSubject.disassociateAndDeleteLast(vertex, Direction.OUT, EdgeLabelEnum.ADDITIONAL_INFORMATION);
	}
	
	@Test
	public void testDisassociateAndDeleteLastOut() throws Exception {

		GraphVertex vertex = Mockito.mock(GraphVertex.class);
		JanusGraphOperationStatus result;
		
		JanusGraphVertex value = Mockito.mock(JanusGraphVertex.class);
		Iterator<Edge> mockiter = Mockito.mock(Iterator.class);
		Edge nextmock = Mockito.mock(Edge.class);
		Mockito.when(vertex.getVertex()).thenReturn(value);
		Mockito.when(value.edges(Mockito.any(), Mockito.any())).thenReturn(mockiter);
		Mockito.when(mockiter.hasNext()).thenReturn(true, false);
		Mockito.when(mockiter.next()).thenReturn(nextmock);
		Vertex secondVertex = Mockito.mock(Vertex.class);
		Mockito.when(nextmock.outVertex()).thenReturn(secondVertex);
		Mockito.when(nextmock.inVertex()).thenReturn(secondVertex);
		Iterator<Edge> restOfEdges = Mockito.mock(Iterator.class);
		Mockito.when(secondVertex.edges(Mockito.any(), Mockito.any())).thenReturn(restOfEdges);
		Mockito.when(restOfEdges.hasNext()).thenReturn(false);
		
		// default test
		result = testSubject.disassociateAndDeleteLast(vertex, Direction.IN, EdgeLabelEnum.ADDITIONAL_INFORMATION);
	}
	
	@Test
	public void testDisassociateAndDeleteLastException() throws Exception {

		GraphVertex vertex = Mockito.mock(GraphVertex.class);
		JanusGraphOperationStatus result;
		
		Mockito.when(vertex.getVertex()).thenThrow(RuntimeException.class);
		
		// default test
		result = testSubject.disassociateAndDeleteLast(vertex, Direction.OUT, EdgeLabelEnum.ADDITIONAL_INFORMATION);
	}

	@Test
	public void testMoveEdge() throws Exception {

		GraphVertex vertexA = new GraphVertex();
		GraphVertex vertexB = new GraphVertex();
		JanusGraphOperationStatus result;

		// default test

		result = testSubject.moveEdge(vertexA, vertexB, EdgeLabelEnum.ADDITIONAL_INFORMATION, Direction.BOTH);
	}
}