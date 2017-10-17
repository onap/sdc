package org.openecomp.sdc.be.resources.data;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.GroupTypeDataDefinition;


public class GroupTypeDataTest {

	private GroupTypeData createTestSubject() {
		return new GroupTypeData();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		GroupTypeData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testGetGroupTypeDataDefinition() throws Exception {
		GroupTypeData testSubject;
		GroupTypeDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroupTypeDataDefinition();
	}

	
	@Test
	public void testSetGroupTypeDataDefinition() throws Exception {
		GroupTypeData testSubject;
		GroupTypeDataDefinition groupTypeDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setGroupTypeDataDefinition(groupTypeDataDefinition);
	}

	
	@Test
	public void testToString() throws Exception {
		GroupTypeData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		GroupTypeData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}
}