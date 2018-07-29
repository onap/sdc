package org.openecomp.sdc.be.resources.data;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.PolicyTypeDataDefinition;

import java.util.HashMap;
import java.util.Map;


public class PolicyTypeDataTest {

	private PolicyTypeData createTestSubject() {
		return new PolicyTypeData();
	}

	@Test
	public void testCtor() throws Exception {
		new PolicyTypeData(new HashMap<>());
		new PolicyTypeData(new PolicyTypeDataDefinition());
	}
	
	@Test
	public void testToGraphMap() throws Exception {
		PolicyTypeData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testToString() throws Exception {
		PolicyTypeData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		PolicyTypeData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testGetPolicyTypeDataDefinition() throws Exception {
		PolicyTypeData testSubject;
		PolicyTypeDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPolicyTypeDataDefinition();
	}
}