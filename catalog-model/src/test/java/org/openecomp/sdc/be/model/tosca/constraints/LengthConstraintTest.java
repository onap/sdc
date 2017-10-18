package org.openecomp.sdc.be.model.tosca.constraints;

import javax.annotation.Generated;

import org.junit.Test;


public class LengthConstraintTest {

	private LengthConstraint createTestSubject() {
		return new LengthConstraint();
	}

	


	
	@Test
	public void testGetLength() throws Exception {
		LengthConstraint testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLength();
	}

	
	@Test
	public void testSetLength() throws Exception {
		LengthConstraint testSubject;
		Integer length = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setLength(length);
	}
}