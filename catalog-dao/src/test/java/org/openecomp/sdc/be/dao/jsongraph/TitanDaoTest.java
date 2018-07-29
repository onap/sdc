package org.openecomp.sdc.be.dao.jsongraph;

import com.thinkaurelius.titan.core.TitanGraph;
import fj.data.Either;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.dao.DAOTitanStrategy;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgePropertyEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanGraphClient;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.utils.DAOConfDependentTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TitanDaoTest extends DAOConfDependentTest{
	
	
	private static Logger logger = LoggerFactory.getLogger(TitanDaoTest.class);
	private TitanDao dao = new TitanDao(new TitanGraphClient(new DAOTitanStrategy()));
	
	@Before
	public void init(){
	dao.titanClient.createGraph();
	}
	
	@After
	public void end(){
		dao.titanClient.cleanupGraph();
	}

	@Test
	public void testCreateVertex() throws Exception {
		Either<GraphVertex, TitanOperationStatus> result;

		// default test
		GraphVertex graphVertex = new GraphVertex(VertexTypeEnum.REQUIREMENTS);
		result = dao.createVertex(graphVertex);
		
		graphVertex = new GraphVertex();
		result = dao.createVertex(graphVertex);
	}
	
	@Test
	public void testGetVertexByLabel() throws Exception {
		Either<GraphVertex, TitanOperationStatus> result;

		// default test
		result = dao.getVertexByLabel(VertexTypeEnum.ADDITIONAL_INFORMATION);
	}
	
	@Test
	public void testCommit() throws Exception {
		TitanOperationStatus result;

		// default test
		
		result = dao.commit();
	}

	
	@Test
	public void testRollback() throws Exception {
		
		TitanOperationStatus result;

		// default test
		
		result = dao.rollback();
	}

	@Test
	public void testGetGraph() throws Exception {
		
		Either<TitanGraph, TitanOperationStatus> result;

		// default test
		
		result = dao.getGraph();
	}

	@Test
	public void testGetVertexByPropertyAndLabel() throws Exception {
		
		GraphPropertyEnum name = null;
		Object value = null;
		VertexTypeEnum label = null;
		Either<GraphVertex, TitanOperationStatus> result;

		// default test
		
		result = dao.getVertexByPropertyAndLabel(name, value, label);
		
		result = dao.getVertexByPropertyAndLabel(GraphPropertyEnum.COMPONENT_TYPE, new Object(), VertexTypeEnum.ADDITIONAL_INFORMATION);
	}

	@Test
	public void testGetVertexByPropertyAndLabel_1() throws Exception {
		
		GraphPropertyEnum name = null;
		Object value = null;
		VertexTypeEnum label = null;
		JsonParseFlagEnum parseFlag = null;
		Either<GraphVertex, TitanOperationStatus> result;

		// default test
		
		result = dao.getVertexByPropertyAndLabel(name, value, label, parseFlag);
	}

	
	@Test
	public void testGetVertexById() throws Exception {
		
		String id = "";
		Either<GraphVertex, TitanOperationStatus> result;

		// default test
		
		result = dao.getVertexById(id);
	}

	@Test
	public void testGetVertexById_1() throws Exception {
		
		String id = "";
		JsonParseFlagEnum parseFlag = null;
		Either<GraphVertex, TitanOperationStatus> result;

		// test 1
		
		id = null;
		result = dao.getVertexById(id, parseFlag);

		// test 2
		
		id = "";
		result = dao.getVertexById(id, parseFlag);
	}

	@Test
	public void testGetVertexProperties() throws Exception {
		
		Element element = null;
		Map<GraphPropertyEnum, Object> result;

		// test 1
		
		element = null;
		result = dao.getVertexProperties(element);
	}

	
	@Test
	public void testGetEdgeProperties() throws Exception {
		
		Element element = null;
		Map<EdgePropertyEnum, Object> result;

		// test 1
		
		element = null;
		result = dao.getEdgeProperties(element);
	}

	@Test
	public void testGetByCriteria() throws Exception {
		
		VertexTypeEnum type = null;
		Map<GraphPropertyEnum, Object> props = null;
		Either<List<GraphVertex>, TitanOperationStatus> result;

		// default test
		
		result = dao.getByCriteria(type, props);
	}

	@Test
	public void testGetByCriteria_1() throws Exception {
		
		VertexTypeEnum type = null;
		Map<GraphPropertyEnum, Object> props = null;
		JsonParseFlagEnum parseFlag = null;
		Either<List<GraphVertex>, TitanOperationStatus> result;

		// default test
		
		result = dao.getByCriteria(type, props, parseFlag);
	}

	
	@Test
	public void testGetByCriteria_2() throws Exception {
		
		VertexTypeEnum type = null;
		Map<GraphPropertyEnum, Object> props = null;
		Map<GraphPropertyEnum, Object> hasNotProps = null;
		JsonParseFlagEnum parseFlag = null;
		Either<List<GraphVertex>, TitanOperationStatus> result;

		// default test
		
		result = dao.getByCriteria(type, props, hasNotProps, parseFlag);
	}

	@Test
	public void testGetCatalogVerticies() throws Exception {
		
		Either<Iterator<Vertex>, TitanOperationStatus> result;

		// default test
		
		result = dao.getCatalogOrArchiveVerticies(true);
	}
	
	@Test
	public void testGetParentVertecies_1() throws Exception {
		
		Vertex parentVertex = null;
		EdgeLabelEnum edgeLabel = null;
		JsonParseFlagEnum parseFlag = null;
		Either<List<Vertex>, TitanOperationStatus> result;

		// default test
		
		result = dao.getParentVertecies(parentVertex, edgeLabel, parseFlag);
	}

	@Test
	public void testGetChildrenVertecies_1() throws Exception {
		
		Vertex parentVertex = null;
		EdgeLabelEnum edgeLabel = null;
		JsonParseFlagEnum parseFlag = null;
		Either<List<Vertex>, TitanOperationStatus> result;

		// default test
		
		result = dao.getChildrenVertecies(parentVertex, edgeLabel, parseFlag);
	}

	@Test
	public void testUpdateVertexMetadataPropertiesWithJson() throws Exception {
		
		Vertex vertex = null;
		Map<GraphPropertyEnum, Object> properties = null;
		TitanOperationStatus result;

		// default test
		
		result = dao.updateVertexMetadataPropertiesWithJson(vertex, properties);
	}

	@Test
	public void testGetProperty() throws Exception {
		Edge edge = Mockito.mock(Edge.class);;
		Object result;
		
		Property<Object> value = Mockito.mock(Property.class);
		Mockito.when(edge.property(Mockito.any())).thenReturn(value);
		
		// default test
		result = dao.getProperty(edge, EdgePropertyEnum.STATE);
	}
	
	@Test
	public void testGetProperty_1() throws Exception {
		Edge edge = Mockito.mock(Edge.class);;
		Object result;

		// default test
		result = dao.getProperty(edge, EdgePropertyEnum.STATE);
	}

	@Test
	public void testGetPropertyexception() throws Exception {
		Edge edge = Mockito.mock(Edge.class);;
		Object result;
		
		Property<Object> value = Mockito.mock(Property.class);
		Mockito.when(edge.property(Mockito.any())).thenThrow(RuntimeException.class);
		
		// default test
		result = dao.getProperty(edge, EdgePropertyEnum.STATE);
	}
	
	@Test
	public void testGetBelongingEdgeByCriteria_1() throws Exception {
		
		String parentId = "";
		EdgeLabelEnum label = null;
		Map<GraphPropertyEnum, Object> properties = null;
		Either<Edge, TitanOperationStatus> result;

		// default test
		
		result = dao.getBelongingEdgeByCriteria(parentId, label, properties);
	}
}