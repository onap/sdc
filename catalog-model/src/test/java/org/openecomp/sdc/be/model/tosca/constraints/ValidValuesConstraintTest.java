package org.openecomp.sdc.be.model.tosca.constraints;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.model.tosca.ToscaType;


public class ValidValuesConstraintTest {

	private ValidValuesConstraint createTestSubject() {
		return new ValidValuesConstraint(null);
	}

	

	


	
	@Test
	public void testGetValidValues() throws Exception {
		ValidValuesConstraint testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValidValues();
	}

	
	@Test
	public void testSetValidValues() throws Exception {
		ValidValuesConstraint testSubject;
		List<String> validValues = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setValidValues(validValues);
	}
}