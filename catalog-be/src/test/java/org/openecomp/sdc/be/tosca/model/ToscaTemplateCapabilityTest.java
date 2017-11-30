package org.openecomp.sdc.be.tosca.model;

import java.util.List;
import java.util.Map;

import org.junit.Test;


public class ToscaTemplateCapabilityTest {

	private ToscaTemplateCapability createTestSubject() {
		return new ToscaTemplateCapability();
	}

	
	@Test
	public void testGetValid_source_types() throws Exception {
		ToscaTemplateCapability testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValid_source_types();
	}

	
	@Test
	public void testSetValid_source_types() throws Exception {
		ToscaTemplateCapability testSubject;
		List<String> valid_source_types = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setValid_source_types(valid_source_types);
	}

	
	@Test
	public void testGetProperties() throws Exception {
		ToscaTemplateCapability testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		ToscaTemplateCapability testSubject;
		Map<String, Object> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}
}