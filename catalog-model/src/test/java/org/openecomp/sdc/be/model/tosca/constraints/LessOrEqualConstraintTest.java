package org.openecomp.sdc.be.model.tosca.constraints;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.model.tosca.ToscaType;


public class LessOrEqualConstraintTest {

	private LessOrEqualConstraint createTestSubject() {
		return new LessOrEqualConstraint("");
	}

	

	


	
	@Test
	public void testGetLessOrEqual() throws Exception {
		LessOrEqualConstraint testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLessOrEqual();
	}

	
	@Test
	public void testSetLessOrEqual() throws Exception {
		LessOrEqualConstraint testSubject;
		String lessOrEqual = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLessOrEqual(lessOrEqual);
	}
}