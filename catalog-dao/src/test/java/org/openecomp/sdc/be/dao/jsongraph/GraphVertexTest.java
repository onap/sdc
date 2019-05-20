package org.openecomp.sdc.be.dao.jsongraph;

import org.janusgraph.core.JanusGraphVertex;
import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

import java.util.HashMap;
import java.util.Map;


public class GraphVertexTest {

	private GraphVertex createTestSubject() {
		return new GraphVertex();
	}
	
	@Test
	public void testCtor() throws Exception {
		new GraphVertex(VertexTypeEnum.ADDITIONAL_INFORMATION);
	}
	
	@Test
	public void testGetUniqueId() throws Exception {
		GraphVertex testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		GraphVertex testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetJson() throws Exception {
		GraphVertex testSubject;
		Map<String, ? extends ToscaDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getJson();
	}

	
	@Test
	public void testSetJson() throws Exception {
		GraphVertex testSubject;
		Map<String, ? extends ToscaDataDefinition> json = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setJson(json);
	}

	
	@Test
	public void testGetVertex() throws Exception {
		GraphVertex testSubject;
		JanusGraphVertex result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVertex();
	}

	
	@Test
	public void testSetVertex() throws Exception {
		GraphVertex testSubject;
		JanusGraphVertex vertex = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setVertex(vertex);
	}

	
	@Test
	public void testGetLabel() throws Exception {
		GraphVertex testSubject;
		VertexTypeEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLabel();
	}

	
	@Test
	public void testSetLabel() throws Exception {
		GraphVertex testSubject;
		VertexTypeEnum label = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setLabel(label);
	}

	
	
	@Test
	public void testAddMetadataProperty() throws Exception {
		GraphVertex testSubject;
		GraphPropertyEnum propName = null;
		Object propValue = null;

		// test 1
		testSubject = createTestSubject();
		propValue = null;
		testSubject.addMetadataProperty(propName, propValue);
	}

	
	@Test
	public void testGetMetadataProperty() throws Exception {
		GraphVertex testSubject;
		GraphPropertyEnum metadataProperty = null;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMetadataProperty(metadataProperty);
	}

	
	@Test
	public void testGetMetadataProperties() throws Exception {
		GraphVertex testSubject;
		Map<GraphPropertyEnum, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMetadataProperties();
	}

	
	@Test
	public void testSetMetadataProperties() throws Exception {
		GraphVertex testSubject;
		Map<GraphPropertyEnum, Object> metadataProperties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMetadataProperties(metadataProperties);
	}

	
	@Test
	public void testGetMetadataJson() throws Exception {
		GraphVertex testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMetadataJson();
	}

	
	@Test
	public void testSetMetadataJson() throws Exception {
		GraphVertex testSubject;
		Map<String, Object> metadataJson = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMetadataJson(metadataJson);
	}

	

	
	@Test
	public void testGetJsonMetadataField() throws Exception {
		GraphVertex testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getJsonMetadataField(JsonPresentationFields.API_URL);
		testSubject = createTestSubject();
		testSubject.setType(ComponentTypeEnum.RESOURCE);
		result = testSubject.getJsonMetadataField(JsonPresentationFields.API_URL);
	}

	
	@Test
	public void testUpdateMetadataJsonWithCurrentMetadataProperties() throws Exception {
		GraphVertex testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.updateMetadataJsonWithCurrentMetadataProperties();
		Map<GraphPropertyEnum, Object> metadataProperties = new HashMap<>();
		metadataProperties.put(GraphPropertyEnum.COMPONENT_TYPE, "mock");
		testSubject.setMetadataProperties(metadataProperties );
		testSubject.updateMetadataJsonWithCurrentMetadataProperties();
	}
	
	@Test
	public void testGetType() throws Exception {
		GraphVertex testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.setType(ComponentTypeEnum.RESOURCE);
		testSubject.getType();
	}
	
	@Test
	public void testCloneData() throws Exception {
		GraphVertex testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.cloneData(new GraphVertex());
	}
	
	@Test
	public void testSetJsonMetadataField() throws Exception {
		GraphVertex testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.setJsonMetadataField(JsonPresentationFields.API_URL, "mock");
	}

}