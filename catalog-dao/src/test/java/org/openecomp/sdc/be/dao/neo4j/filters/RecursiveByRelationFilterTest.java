package org.openecomp.sdc.be.dao.neo4j.filters;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;


public class RecursiveByRelationFilterTest {

	private RecursiveByRelationFilter createTestSubject() {
		return new RecursiveByRelationFilter();
	}

	
	@Test
	public void testAddNode() throws Exception {
		RecursiveByRelationFilter testSubject;
		GraphNode node = null;
		RecursiveByRelationFilter result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.addNode(node);
	}

	
	@Test
	public void testAddRelation() throws Exception {
		RecursiveByRelationFilter testSubject;
		String relationType = "";
		RecursiveByRelationFilter result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.addRelation(relationType);
	}

	
	@Test
	public void testGetNode() throws Exception {
		RecursiveByRelationFilter testSubject;
		GraphNode result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNode();
	}

	
	@Test
	public void testSetNode() throws Exception {
		RecursiveByRelationFilter testSubject;
		GraphNode node = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setNode(node);
	}

	
	@Test
	public void testGetRelationType() throws Exception {
		RecursiveByRelationFilter testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelationType();
	}

	
	@Test
	public void testSetRelationType() throws Exception {
		RecursiveByRelationFilter testSubject;
		String relationType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRelationType(relationType);
	}

	
	@Test
	public void testToString() throws Exception {
		RecursiveByRelationFilter testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}