package org.openecomp.sdc.be.dao.neo4j;

import org.junit.Test;

import java.util.List;

public class GraphEdgePropertiesDictionaryTest {

	private GraphEdgePropertiesDictionary createTestSubject() {
		return GraphEdgePropertiesDictionary.GET_INPUT_INDEX;
	}

	@Test
	public void testGetProperty() throws Exception {
		GraphEdgePropertiesDictionary testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperty();
	}

	@Test
	public void testSetProperty() throws Exception {
		GraphEdgePropertiesDictionary testSubject;
		String property = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setProperty(property);
	}

	@Test
	public void testGetClazz() throws Exception {
		GraphEdgePropertiesDictionary testSubject;
		Class result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getClazz();
	}

	@Test
	public void testSetClazz() throws Exception {
		GraphEdgePropertiesDictionary testSubject;
		Class clazz = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setClazz(clazz);
	}

	@Test
	public void testGetAllProperties() throws Exception {
		List<String> result;

		// default test
		result = GraphEdgePropertiesDictionary.getAllProperties();
	}

	@Test
	public void testGetByName() throws Exception {
		String property = "";
		GraphEdgePropertiesDictionary result;

		// default test
		result = GraphEdgePropertiesDictionary.getByName(property);
		result = GraphEdgePropertiesDictionary.getByName("mock");
	}
}