package org.openecomp.sdc.be.dao.neo4j.filters;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;


public class RecursiveFilterTest {

	private RecursiveFilter createTestSubject() {
		return new RecursiveFilter();
	}

	
	@Test
	public void testAddChildRelationType() throws Exception {
		RecursiveFilter testSubject;
		String type = "";
		RecursiveFilter result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.addChildRelationType(type);
	}

	
	@Test
	public void testGetChildRelationTypes() throws Exception {
		RecursiveFilter testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getChildRelationTypes();
	}

	
	@Test
	public void testSetChildRelationTypes() throws Exception {
		RecursiveFilter testSubject;
		List<String> childRelationTypes = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setChildRelationTypes(childRelationTypes);
	}

	
	@Test
	public void testGetNodeType() throws Exception {
		RecursiveFilter testSubject;
		NodeTypeEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNodeType();
	}

	
	@Test
	public void testSetNodeType() throws Exception {
		RecursiveFilter testSubject;
		NodeTypeEnum nodeType = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setNodeType(nodeType);
	}

	
	@Test
	public void testToString() throws Exception {
		RecursiveFilter testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}