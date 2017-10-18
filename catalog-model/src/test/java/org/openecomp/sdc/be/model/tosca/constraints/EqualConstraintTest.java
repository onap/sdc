package org.openecomp.sdc.be.model.tosca.constraints;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.model.tosca.ToscaType;


public class EqualConstraintTest {

	private EqualConstraint createTestSubject() {
		return new EqualConstraint("");
	}

	

	
	@Test
	public void testValidate() throws Exception {
		EqualConstraint testSubject;
		Object propertyValue = null;

		// test 1
		testSubject = createTestSubject();
		propertyValue = null;
		testSubject.validate(propertyValue);
	}

	

}