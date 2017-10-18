package org.openecomp.sdc.be.model.tosca.constraints;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.model.tosca.ToscaType;


public class InRangeConstraintTest {

	private InRangeConstraint createTestSubject() {
		return new InRangeConstraint(null);
	}

	


	
	@Test
	public void testGetRangeMinValue() throws Exception {
		InRangeConstraint testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRangeMinValue();
	}

	
	@Test
	public void testSetRangeMinValue() throws Exception {
		InRangeConstraint testSubject;
		String minValue = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRangeMinValue(minValue);
	}

	
	@Test
	public void testGetRangeMaxValue() throws Exception {
		InRangeConstraint testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRangeMaxValue();
	}

	
	@Test
	public void testSetRangeMaxValue() throws Exception {
		InRangeConstraint testSubject;
		String maxValue = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRangeMaxValue(maxValue);
	}
}