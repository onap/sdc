package org.openecomp.sdc.be.tosca.model;

import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;


public class ToscaNodeTemplateTest {

	private ToscaNodeTemplate createTestSubject() {
		return new ToscaNodeTemplate();
	}

	
	@Test
	public void testGetType() throws Exception {
		ToscaNodeTemplate testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		ToscaNodeTemplate testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	
	@Test
	public void testGetProperties() throws Exception {
		ToscaNodeTemplate testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		ToscaNodeTemplate testSubject;
		Map<String, Object> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}

	
	@Test
	public void testGetRequirements() throws Exception {
		ToscaNodeTemplate testSubject;
		List<Map<String, ToscaTemplateRequirement>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirements();
	}

	
	@Test
	public void testSetRequirements() throws Exception {
		ToscaNodeTemplate testSubject;
		List<Map<String, ToscaTemplateRequirement>> requirements = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirements(requirements);
	}

	
	@Test
	public void testGetCapabilities() throws Exception {
		ToscaNodeTemplate testSubject;
		Map<String, ToscaTemplateCapability> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilities();
	}

	
	@Test
	public void testSetCapabilities() throws Exception {
		ToscaNodeTemplate testSubject;
		Map<String, ToscaTemplateCapability> capabilities = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilities(capabilities);
	}

	
	@Test
	public void testGetMetadata() throws Exception {
		ToscaNodeTemplate testSubject;
		ToscaMetadata result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMetadata();
	}

	
	@Test
	public void testSetMetadata() throws Exception {
		ToscaNodeTemplate testSubject;
		ToscaMetadata metadata = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMetadata(metadata);
	}
}