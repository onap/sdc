package org.openecomp.sdc.be.model.tosca;

import org.junit.Assert;
import org.junit.Test;


public class ToscaTypeTest {

	private ToscaType createTestSubject() {
		return  ToscaType.BOOLEAN;
	}


	
	@Test
	public void testIsValidValue() throws Exception {
		ToscaType testSubject;
		String value = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isValidValue(value);
	}


	
	@Test
	public void testConvert() throws Exception {
		ToscaType testSubject;
		String value = "";
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convert(value);
	}

	
	@Test
	public void testToString() throws Exception {
		ToscaType testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}