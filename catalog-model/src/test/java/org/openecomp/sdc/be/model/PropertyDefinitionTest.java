package org.openecomp.sdc.be.model;

import java.util.List;

import org.junit.Test;


public class PropertyDefinitionTest {

	private PropertyDefinition createTestSubject() {
		return new PropertyDefinition();
	}

	@Test
	public void testGetConstraints() throws Exception {
		PropertyDefinition testSubject;
		List<PropertyConstraint> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConstraints();
	}

	@Test
	public void testSetConstraints() throws Exception {
		PropertyDefinition testSubject;
		List<PropertyConstraint> constraints = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setConstraints(constraints);
	}

	@Test
	public void testToString() throws Exception {
		PropertyDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	@Test
	public void testIsDefinition() throws Exception {
		PropertyDefinition testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isDefinition();
	}

	@Test
	public void testSetDefinition() throws Exception {
		PropertyDefinition testSubject;
		boolean definition = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setDefinition(definition);
	}

	@Test
	public void testHashCode() throws Exception {
		PropertyDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	@Test
	public void testEquals() throws Exception {
		PropertyDefinition testSubject;
		Object obj = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.equals(obj);
	}
}