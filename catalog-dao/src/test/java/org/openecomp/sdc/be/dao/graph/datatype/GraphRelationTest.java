package org.openecomp.sdc.be.dao.graph.datatype;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class GraphRelationTest {

	private GraphRelation createTestSubject() {
		return new GraphRelation();
	}

	@Test
	public void testCtor() throws Exception {
		new GraphRelation("mock");
	}
	
	@Test
	public void testGetFrom() throws Exception {
		GraphRelation testSubject;
		RelationEndPoint result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFrom();
	}

	@Test
	public void testSetFrom() throws Exception {
		GraphRelation testSubject;
		RelationEndPoint from = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setFrom(from);
	}

	@Test
	public void testGetTo() throws Exception {
		GraphRelation testSubject;
		RelationEndPoint result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTo();
	}

	@Test
	public void testSetTo() throws Exception {
		GraphRelation testSubject;
		RelationEndPoint to = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTo(to);
	}

	@Test
	public void testGetType() throws Exception {
		GraphRelation testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	@Test
	public void testSetType() throws Exception {
		GraphRelation testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	@Test
	public void testAddProperty() throws Exception {
		GraphRelation testSubject;
		String property = "";
		Object value = null;

		// test 1
		testSubject = createTestSubject();
		property = null;
		value = null;
		testSubject.addProperty(property, value);

		// test 2
		testSubject = createTestSubject();
		property = "";
		value = null;
		testSubject.addProperty(property, value);

		// test 3
		testSubject = createTestSubject();
		value = null;
		property = null;
		testSubject.addProperty(property, value);
		
		testSubject = createTestSubject();
		value = new Object();
		property = "mock";
		testSubject.addProperty(property, value);
	}

	@Test
	public void testAddPropertis() throws Exception {
		GraphRelation testSubject;
		Map<String, Object> props = null;

		// default test
		testSubject = createTestSubject();
		testSubject.addPropertis(props);
		props = new HashMap<>();
		testSubject.addPropertis(props);
	}

	@Test
	public void testOverwritePropertis() throws Exception {
		GraphRelation testSubject;
		Map<String, Object> props = null;

		// default test
		testSubject = createTestSubject();
		testSubject.overwritePropertis(props);
	}

	@Test
	public void testGetProperty() throws Exception {
		GraphRelation testSubject;
		String property = "";
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperty(property);
	}

	@Test
	public void testToGraphMap() throws Exception {
		GraphRelation testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	@Test
	public void testToString() throws Exception {
		GraphRelation testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	@Test
	public void testHashCode() throws Exception {
		GraphRelation testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}
}