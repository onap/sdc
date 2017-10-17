package org.openecomp.sdc.be.tosca.model;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;


public class SubstitutionMappingTest {

	private SubstitutionMapping createTestSubject() {
		return new SubstitutionMapping();
	}

	
	@Test
	public void testGetNode_type() throws Exception {
		SubstitutionMapping testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNode_type();
	}

	
	@Test
	public void testSetNode_type() throws Exception {
		SubstitutionMapping testSubject;
		String node_type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNode_type(node_type);
	}

	
	@Test
	public void testGetCapabilities() throws Exception {
		SubstitutionMapping testSubject;
		Map<String, String[]> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilities();
	}

	
	@Test
	public void testSetCapabilities() throws Exception {
		SubstitutionMapping testSubject;
		Map<String, String[]> capabilities = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilities(capabilities);
	}

	
	@Test
	public void testGetRequirements() throws Exception {
		SubstitutionMapping testSubject;
		Map<String, String[]> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirements();
	}

	
	@Test
	public void testSetRequirements() throws Exception {
		SubstitutionMapping testSubject;
		Map<String, String[]> requirements = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirements(requirements);
	}
}