package org.openecomp.sdc.be.model;

import java.util.List;

import org.junit.Test;

public class PolicyTargetDTOTest {

	private PolicyTargetDTO createTestSubject() {
		return new PolicyTargetDTO();
	}

	@Test
	public void testGetType() throws Exception {
		PolicyTargetDTO testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	@Test
	public void testSetType() throws Exception {
		PolicyTargetDTO testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	@Test
	public void testGetUniqueIds() throws Exception {
		PolicyTargetDTO testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueIds();
	}

	@Test
	public void testSetUniqueIds() throws Exception {
		PolicyTargetDTO testSubject;
		List<String> ids = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueIds(ids);
	}
}