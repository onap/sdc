package org.openecomp.sdc.be.dao.jsongraph;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.DAOTitanStrategy;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgePropertyEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanGraphClient;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkaurelius.titan.core.TitanGraph;

import fj.data.Either;

public class TitanDaoTest {
	
	
	private static Logger logger = LoggerFactory.getLogger(TitanDaoTest.class);

	

	private TitanDao createTestSubject() {
		TitanGraphClient client = new TitanGraphClient(new DAOTitanStrategy());
		client.createGraph();
		return new TitanDao(client);
	}
	@Before
	public void init(){
	String appConfigDir = "src/test/resources/config/catalog-dao";
    ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
	ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);



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
	public void testGetVertexById_1() throws Exception {
		TitanDao testSubject;
		String id = "";
		JsonParseFlagEnum parseFlag = null;
		Either<GraphVertex, TitanOperationStatus> result;

		// test 1
		testSubject = createTestSubject();
		id = null;
		result = testSubject.getVertexById(id, parseFlag);

		// test 2
		testSubject = createTestSubject();
		id = "";
		result = testSubject.getVertexById(id, parseFlag);
	}

	

	


	


	

	
	
	
	@Test
	public void testGetVertexProperties() throws Exception {
		TitanDao testSubject;
		Element element = null;
		Map<GraphPropertyEnum, Object> result;

		// test 1
		testSubject = createTestSubject();
		element = null;
		result = testSubject.getVertexProperties(element);
	}

	
	@Test
	public void testGetEdgeProperties() throws Exception {
		TitanDao testSubject;
		Element element = null;
		Map<EdgePropertyEnum, Object> result;

		// test 1
		testSubject = createTestSubject();
		element = null;
		result = testSubject.getEdgeProperties(element);
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
	public void testGetCatalogVerticies() throws Exception {
		TitanDao testSubject;
		Either<Iterator<Vertex>, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCatalogVerticies();
	}


	

	

	

	


	

	

	
	@Test
	public void testGetParentVertecies_1() throws Exception {
		TitanDao testSubject;
		Vertex parentVertex = null;
		EdgeLabelEnum edgeLabel = null;
		JsonParseFlagEnum parseFlag = null;
		Either<List<Vertex>, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getParentVertecies(parentVertex, edgeLabel, parseFlag);
	}



	
	@Test
	public void testGetChildrenVertecies_1() throws Exception {
		TitanDao testSubject;
		Vertex parentVertex = null;
		EdgeLabelEnum edgeLabel = null;
		JsonParseFlagEnum parseFlag = null;
		Either<List<Vertex>, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getChildrenVertecies(parentVertex, edgeLabel, parseFlag);
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