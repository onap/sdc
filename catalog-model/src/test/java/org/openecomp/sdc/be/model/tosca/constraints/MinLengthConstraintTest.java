package org.openecomp.sdc.be.model.tosca.constraints;

import javax.annotation.Generated;

import org.junit.Test;


public class MinLengthConstraintTest {

	private MinLengthConstraint createTestSubject() {
		return new MinLengthConstraint(null);
	}

	


	
	@Test
	public void testGetMinLength() throws Exception {
		MinLengthConstraint testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMinLength();
	}

	
	@Test
	public void testSetMinLength() throws Exception {
		MinLengthConstraint testSubject;
		Integer minLength = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setMinLength(minLength);
	}
}