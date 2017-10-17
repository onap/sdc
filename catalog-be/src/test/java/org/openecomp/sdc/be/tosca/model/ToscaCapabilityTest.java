package org.openecomp.sdc.be.tosca.model;

import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;


public class ToscaCapabilityTest {

	private ToscaCapability createTestSubject() {
		return new ToscaCapability();
	}

	
	@Test
	public void testGetValid_source_types() throws Exception {
		ToscaCapability testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValid_source_types();
	}

	
	@Test
	public void testSetValid_source_types() throws Exception {
		ToscaCapability testSubject;
		List<String> valid_source_types = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setValid_source_types(valid_source_types);
	}

	
	@Test
	public void testGetType() throws Exception {
		ToscaCapability testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		ToscaCapability testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	
	@Test
	public void testGetDescription() throws Exception {
		ToscaCapability testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testSetDescription() throws Exception {
		ToscaCapability testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	
	@Test
	public void testGetOccurrences() throws Exception {
		ToscaCapability testSubject;
		List<Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOccurrences();
	}

	
	@Test
	public void testSetOccurrences() throws Exception {
		ToscaCapability testSubject;
		List<Object> occurrences = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setOccurrences(occurrences);
	}

	
	@Test
	public void testGetProperties() throws Exception {
		ToscaCapability testSubject;
		Map<String, ToscaProperty> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		ToscaCapability testSubject;
		Map<String, ToscaProperty> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}
}