package org.openecomp.sdc.be.tosca.model;

import java.util.List;
import java.util.Map;

import org.junit.Test;


public class ToscaRequirementTest {

	private ToscaRequirement createTestSubject() {
		return new ToscaRequirement();
	}

	
	@Test
	public void testGetOccurrences() throws Exception {
		ToscaRequirement testSubject;
		List<Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOccurrences();
	}

	
	@Test
	public void testSetOccurrences() throws Exception {
		ToscaRequirement testSubject;
		List<Object> occurrences = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setOccurrences(occurrences);
	}

	
	@Test
	public void testToMap() throws Exception {
		ToscaRequirement testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toMap();
	}
}