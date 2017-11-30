package org.openecomp.sdc.be.dao.jsongraph;

import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgePropertyEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanGraphClient;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;

import com.thinkaurelius.titan.core.TitanGraph;

import fj.data.Either;


public class TitanDaoTest {

	private TitanDao createTestSubject() {
		return new TitanDao(new TitanGraphClient());
	}

	
	@Test
	public void testCommit() throws Exception {
		TitanDao testSubject;
		TitanOperationStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.commit();
	}

	
	@Test
	public void testRollback() throws Exception {
		TitanDao testSubject;
		TitanOperationStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.rollback();
	}

	
	@Test
	public void testGetGraph() throws Exception {
		TitanDao testSubject;
		Either<TitanGraph, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGraph();
	}

	


	
	@Test
	public void testGetVertexByPropertyAndLabel() throws Exception {
		TitanDao testSubject;
		GraphPropertyEnum name = null;
		Object value = null;
		VertexTypeEnum label = null;
		Either<GraphVertex, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVertexByPropertyAndLabel(name, value, label);
	}

	
	@Test
	public void testGetVertexByPropertyAndLabel_1() throws Exception {
		TitanDao testSubject;
		GraphPropertyEnum name = null;
		Object value = null;
		VertexTypeEnum label = null;
		JsonParseFlagEnum parseFlag = null;
		Either<GraphVertex, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVertexByPropertyAndLabel(name, value, label, parseFlag);
	}

	
	@Test
	public void testGetVertexById() throws Exception {
		TitanDao testSubject;
		String id = "";
		Either<GraphVertex, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVertexById(id);
	}

	
	

	


	


	


	

	
	
	@Test
	public void testGetByCriteria() throws Exception {
		TitanDao testSubject;
		VertexTypeEnum type = null;
		Map<GraphPropertyEnum, Object> props = null;
		Either<List<GraphVertex>, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getByCriteria(type, props);
	}

	
	@Test
	public void testGetByCriteria_1() throws Exception {
		TitanDao testSubject;
		VertexTypeEnum type = null;
		Map<GraphPropertyEnum, Object> props = null;
		JsonParseFlagEnum parseFlag = null;
		Either<List<GraphVertex>, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getByCriteria(type, props, parseFlag);
	}

	
	@Test
	public void testGetByCriteria_2() throws Exception {
		TitanDao testSubject;
		VertexTypeEnum type = null;
		Map<GraphPropertyEnum, Object> props = null;
		Map<GraphPropertyEnum, Object> hasNotProps = null;
		JsonParseFlagEnum parseFlag = null;
		Either<List<GraphVertex>, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getByCriteria(type, props, hasNotProps, parseFlag);
	}

	


	
	@Test
	public void testGetChildVertex() throws Exception {
		TitanDao testSubject;
		GraphVertex parentVertex = null;
		EdgeLabelEnum edgeLabel = null;
		JsonParseFlagEnum parseFlag = null;
		Either<GraphVertex, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getChildVertex(parentVertex, edgeLabel, parseFlag);
	}

	
	@Test
	public void testGetParentVertex() throws Exception {
		TitanDao testSubject;
		GraphVertex parentVertex = null;
		EdgeLabelEnum edgeLabel = null;
		JsonParseFlagEnum parseFlag = null;
		Either<GraphVertex, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getParentVertex(parentVertex, edgeLabel, parseFlag);
	}

	
	@Test
	public void testGetChildrenVertecies() throws Exception {
		TitanDao testSubject;
		GraphVertex parentVertex = null;
		EdgeLabelEnum edgeLabel = null;
		JsonParseFlagEnum parseFlag = null;
		Either<List<GraphVertex>, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getChildrenVertecies(parentVertex, edgeLabel, parseFlag);
	}

	
	@Test
	public void testGetParentVertecies() throws Exception {
		TitanDao testSubject;
		GraphVertex parentVertex = null;
		EdgeLabelEnum edgeLabel = null;
		JsonParseFlagEnum parseFlag = null;
		Either<List<GraphVertex>, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getParentVertecies(parentVertex, edgeLabel, parseFlag);
	}

	



	


	


	

	


	

	

	


	

	
	@Test
	public void testUpdateVertexMetadataPropertiesWithJson() throws Exception {
		TitanDao testSubject;
		Vertex vertex = null;
		Map<GraphPropertyEnum, Object> properties = null;
		TitanOperationStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.updateVertexMetadataPropertiesWithJson(vertex, properties);
	}

	

	


	
	@Test
	public void testGetProperty_1() throws Exception {
		TitanDao testSubject;
		Edge edge = null;
		EdgePropertyEnum key = null;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperty(edge, key);
	}

	

	
	@Test
	public void testGetBelongingEdgeByCriteria_1() throws Exception {
		TitanDao testSubject;
		String parentId = "";
		EdgeLabelEnum label = null;
		Map<GraphPropertyEnum, Object> properties = null;
		Either<Edge, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getBelongingEdgeByCriteria(parentId, label, properties);
	}
}