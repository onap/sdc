package org.openecomp.sdc.be.model;

import java.util.Map;

import org.junit.Test;

public class ParsedToscaYamlInfoTest {

	private ParsedToscaYamlInfo createTestSubject() {
		return new ParsedToscaYamlInfo();
	}

	@Test
	public void testGetInstances() throws Exception {
		ParsedToscaYamlInfo testSubject;
		Map<String, UploadComponentInstanceInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInstances();
	}

	@Test
	public void testSetInstances() throws Exception {
		ParsedToscaYamlInfo testSubject;
		Map<String, UploadComponentInstanceInfo> instances = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInstances(instances);
	}

	@Test
	public void testGetGroups() throws Exception {
		ParsedToscaYamlInfo testSubject;
		Map<String, GroupDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroups();
	}

	@Test
	public void testSetGroups() throws Exception {
		ParsedToscaYamlInfo testSubject;
		Map<String, GroupDefinition> groups = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setGroups(groups);
	}

	@Test
	public void testGetInputs() throws Exception {
		ParsedToscaYamlInfo testSubject;
		Map<String, InputDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInputs();
	}

	@Test
	public void testSetInputs() throws Exception {
		ParsedToscaYamlInfo testSubject;
		Map<String, InputDefinition> inputs = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInputs(inputs);
	}

	@Test
	public void testToString() throws Exception {
		ParsedToscaYamlInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}