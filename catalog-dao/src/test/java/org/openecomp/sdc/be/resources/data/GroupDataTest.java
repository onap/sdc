package org.openecomp.sdc.be.resources.data;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;


public class GroupDataTest {

	private GroupData createTestSubject() {
		return new GroupData();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		GroupData testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		GroupData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testGetGroupDataDefinition() throws Exception {
		GroupData testSubject;
		GroupDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroupDataDefinition();
	}

	
	@Test
	public void testSetGroupDataDefinition() throws Exception {
		GroupData testSubject;
		GroupDataDefinition groupDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setGroupDataDefinition(groupDataDefinition);
	}

	
	@Test
	public void testToString() throws Exception {
		GroupData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}