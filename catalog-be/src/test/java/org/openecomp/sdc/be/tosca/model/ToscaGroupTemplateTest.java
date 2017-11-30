package org.openecomp.sdc.be.tosca.model;

import java.util.List;
import java.util.Map;

import org.junit.Test;


public class ToscaGroupTemplateTest {

	private ToscaGroupTemplate createTestSubject() {
		return new ToscaGroupTemplate();
	}

	
	@Test
	public void testGetType() throws Exception {
		ToscaGroupTemplate testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		ToscaGroupTemplate testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	
	@Test
	public void testGetMembers() throws Exception {
		ToscaGroupTemplate testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMembers();
	}

	
	@Test
	public void testSetMembers() throws Exception {
		ToscaGroupTemplate testSubject;
		List<String> members = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMembers(members);
	}

	
	@Test
	public void testGetMetadata() throws Exception {
		ToscaGroupTemplate testSubject;
		IToscaMetadata result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMetadata();
	}

	
	@Test
	public void testSetMetadata() throws Exception {
		ToscaGroupTemplate testSubject;
		IToscaMetadata metadata = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMetadata(metadata);
	}

	
	@Test
	public void testGetProperties() throws Exception {
		ToscaGroupTemplate testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		ToscaGroupTemplate testSubject;
		Map<String, Object> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}
}