package org.openecomp.sdc.be.dao.neo4j;

import org.junit.Test;

import java.util.List;

public class GraphEdgeLabelsTest {

	private GraphEdgeLabels createTestSubject() {
		return GraphEdgeLabels.ADDITIONAL_INFORMATION;
	}

	@Test
	public void testGetProperty() throws Exception {
		GraphEdgeLabels testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperty();
	}

	@Test
	public void testSetProperty() throws Exception {
		GraphEdgeLabels testSubject;
		String property = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setProperty(property);
	}

	@Test
	public void testGetAllProperties() throws Exception {
		List<String> result;

		// default test
		result = GraphEdgeLabels.getAllProperties();
	}

	@Test
	public void testGetByName() throws Exception {
		String property = "";
		GraphEdgeLabels result;

		// default test
		result = GraphEdgeLabels.getByName(property);
		result = GraphEdgeLabels.getByName("mock");
	}
}