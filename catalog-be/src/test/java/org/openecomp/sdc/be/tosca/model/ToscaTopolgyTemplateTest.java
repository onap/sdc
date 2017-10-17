package org.openecomp.sdc.be.tosca.model;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;


public class ToscaTopolgyTemplateTest {

	private ToscaTopolgyTemplate createTestSubject() {
		return new ToscaTopolgyTemplate();
	}

	
	@Test
	public void testGetNode_templates() throws Exception {
		ToscaTopolgyTemplate testSubject;
		Map<String, ToscaNodeTemplate> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNode_templates();
	}

	
	@Test
	public void testSetNode_templates() throws Exception {
		ToscaTopolgyTemplate testSubject;
		Map<String, ToscaNodeTemplate> node_templates = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setNode_templates(node_templates);
	}

	
	@Test
	public void testGetGroups() throws Exception {
		ToscaTopolgyTemplate testSubject;
		Map<String, ToscaGroupTemplate> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroups();
	}



	
	@Test
	public void testGetSubstitution_mappings() throws Exception {
		ToscaTopolgyTemplate testSubject;
		SubstitutionMapping result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSubstitution_mappings();
	}

	
	@Test
	public void testSetSubstitution_mappings() throws Exception {
		ToscaTopolgyTemplate testSubject;
		SubstitutionMapping substitution_mapping = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setSubstitution_mappings(substitution_mapping);
	}

	
	@Test
	public void testGetInputs() throws Exception {
		ToscaTopolgyTemplate testSubject;
		Map<String, ToscaProperty> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInputs();
	}

	
	@Test
	public void testSetInputs() throws Exception {
		ToscaTopolgyTemplate testSubject;
		Map<String, ToscaProperty> inputs = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInputs(inputs);
	}
}