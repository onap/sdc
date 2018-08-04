package org.openecomp.sdc.be.tosca.model;

import java.util.Map;

import org.junit.Test;


public class ToscaTemplateRequirementTest {

	private ToscaTemplateRequirement createTestSubject() {
		return new ToscaTemplateRequirement();
	}

	
	@Test
	public void testGetCapability() throws Exception {
		ToscaTemplateRequirement testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapability();
	}

	
	@Test
	public void testSetCapability() throws Exception {
		ToscaTemplateRequirement testSubject;
		String capability = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCapability(capability);
	}

	
	@Test
	public void testGetNode() throws Exception {
		ToscaTemplateRequirement testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNode();
	}

	
	@Test
	public void testSetNode() throws Exception {
		ToscaTemplateRequirement testSubject;
		String node = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNode(node);
	}

	
	@Test
	public void testGetRelationship() throws Exception {
		ToscaTemplateRequirement testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelationship();
	}

	
	@Test
	public void testSetRelationship() throws Exception {
		ToscaTemplateRequirement testSubject;
		String relationship = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRelationship(relationship);
	}

	
	@Test
	public void testToMap() throws Exception {
		ToscaTemplateRequirement testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toMap();
	}
}