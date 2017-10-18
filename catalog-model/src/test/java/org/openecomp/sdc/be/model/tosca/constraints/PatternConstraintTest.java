package org.openecomp.sdc.be.model.tosca.constraints;

import javax.annotation.Generated;

import org.junit.Test;


public class PatternConstraintTest {

	private PatternConstraint createTestSubject() {
		return new PatternConstraint();
	}

	
	@Test
	public void testSetPattern() throws Exception {
		PatternConstraint testSubject;
		String pattern = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPattern(pattern);
	}

}