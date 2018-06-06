package org.openecomp.sdc.be.model.tosca.validators;

import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.be.model.DataTypeDefinition;

public class ToscaBooleanValidatorTest {

	private ToscaBooleanValidator createTestSubject() {
		return ToscaBooleanValidator.getInstance();
	}

	
	@Test
	public void testGetInstance() throws Exception {
		ToscaBooleanValidator result;

		// default test
		result = ToscaBooleanValidator.getInstance();
	}

	
	@Test
	public void testIsValid() throws Exception {
		ToscaBooleanValidator testSubject;
		String value = "";
		String innerType = "";
		Map<String, DataTypeDefinition> dataTypes = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		value = null;
		result = testSubject.isValid(value, innerType, dataTypes);

		// test 2
		testSubject = createTestSubject();
		value = "";
		result = testSubject.isValid(value, innerType, dataTypes);
	}

	
	@Test
	public void testIsValid_1() throws Exception {
		ToscaBooleanValidator testSubject;
		String value = "";
		String innerType = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isValid(value, innerType);
	}
}