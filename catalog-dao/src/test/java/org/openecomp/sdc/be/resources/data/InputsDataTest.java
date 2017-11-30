package org.openecomp.sdc.be.resources.data;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;


public class InputsDataTest {

	private InputsData createTestSubject() {
		return new InputsData();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		InputsData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testGetConstraints() throws Exception {
		InputsData testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConstraints();
	}

	
	@Test
	public void testSetConstraints() throws Exception {
		InputsData testSubject;
		List<String> constraints = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setConstraints(constraints);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		InputsData testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testGetPropertyDataDefinition() throws Exception {
		InputsData testSubject;
		PropertyDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPropertyDataDefinition();
	}

	
	@Test
	public void testSetPropertyDataDefinition() throws Exception {
		InputsData testSubject;
		PropertyDataDefinition propertyDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setPropertyDataDefinition(propertyDataDefinition);
	}

	
	@Test
	public void testToString() throws Exception {
		InputsData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}