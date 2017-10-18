package org.openecomp.sdc.be.model.tosca.constraints;

import javax.annotation.Generated;

import org.junit.Test;


public class MaxLengthConstraintTest {

	private MaxLengthConstraint createTestSubject() {
		return new MaxLengthConstraint(null);
	}

	
	
	@Test
	public void testGetMaxLength() throws Exception {
		MaxLengthConstraint testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMaxLength();
	}

	
	@Test
	public void testSetMaxLength() throws Exception {
		MaxLengthConstraint testSubject;
		Integer maxLength = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setMaxLength(maxLength);
	}
}