package org.openecomp.sdc.be.resources.data;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;


public class PropertyDataTest {

	private PropertyData createTestSubject() {
		return new PropertyData();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		PropertyData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testGetConstraints() throws Exception {
		PropertyData testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConstraints();
	}

	
	@Test
	public void testSetConstraints() throws Exception {
		PropertyData testSubject;
		List<String> constraints = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setConstraints(constraints);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		PropertyData testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testGetPropertyDataDefinition() throws Exception {
		PropertyData testSubject;
		PropertyDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPropertyDataDefinition();
	}

	
	@Test
	public void testSetPropertyDataDefinition() throws Exception {
		PropertyData testSubject;
		PropertyDataDefinition propertyDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setPropertyDataDefinition(propertyDataDefinition);
	}

	
	@Test
	public void testToString() throws Exception {
		PropertyData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}