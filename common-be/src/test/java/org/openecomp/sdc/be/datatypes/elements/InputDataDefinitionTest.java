package org.openecomp.sdc.be.datatypes.elements;

import javax.annotation.Generated;

import org.junit.Test;


public class InputDataDefinitionTest {

	private InputDataDefinition createTestSubject() {
		return new InputDataDefinition();
	}

	
	@Test
	public void testIsHidden() throws Exception {
		InputDataDefinition testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isHidden();
	}

	
	@Test
	public void testSetHidden() throws Exception {
		InputDataDefinition testSubject;
		Boolean hidden = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setHidden(hidden);
	}

	
	@Test
	public void testIsImmutable() throws Exception {
		InputDataDefinition testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isImmutable();
	}

	
	@Test
	public void testSetImmutable() throws Exception {
		InputDataDefinition testSubject;
		Boolean immutable = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setImmutable(immutable);
	}

	
	@Test
	public void testGetLabel() throws Exception {
		InputDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLabel();
	}

	
	@Test
	public void testSetLabel() throws Exception {
		InputDataDefinition testSubject;
		String label = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLabel(label);
	}
}