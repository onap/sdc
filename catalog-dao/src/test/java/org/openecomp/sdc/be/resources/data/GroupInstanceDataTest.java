package org.openecomp.sdc.be.resources.data;

import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.GroupInstanceDataDefinition;


public class GroupInstanceDataTest {

	private GroupInstanceData createTestSubject() {
		return new GroupInstanceData();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		GroupInstanceData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		GroupInstanceData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testGetName() throws Exception {
		GroupInstanceData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testGetUniqueIdKey() throws Exception {
		GroupInstanceData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueIdKey();
	}

	
	@Test
	public void testGetGroupDataDefinition() throws Exception {
		GroupInstanceData testSubject;
		GroupInstanceDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroupDataDefinition();
	}

	
	@Test
	public void testSetComponentInstDataDefinition() throws Exception {
		GroupInstanceData testSubject;
		GroupInstanceDataDefinition groupDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstDataDefinition(groupDataDefinition);
	}

	
	@Test
	public void testToString() throws Exception {
		GroupInstanceData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}