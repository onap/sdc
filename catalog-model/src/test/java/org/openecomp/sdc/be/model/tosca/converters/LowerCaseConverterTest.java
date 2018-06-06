package org.openecomp.sdc.be.model.tosca.converters;

import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.be.model.DataTypeDefinition;

public class LowerCaseConverterTest {

	private LowerCaseConverter createTestSubject() {
		return LowerCaseConverter.getInstance();
	}

	
	@Test
	public void testConvert() throws Exception {
		LowerCaseConverter testSubject;
		String value = "";
		String innerType = "";
		Map<String, DataTypeDefinition> dataTypes = null;
		String result;

		// test 1
		testSubject = createTestSubject();
		value = null;
		result = testSubject.convert(value, innerType, dataTypes);

		// test 2
		testSubject = createTestSubject();
		value = "";
		result = testSubject.convert(value, innerType, dataTypes);
	}

	
	@Test
	public void testGetInstance() throws Exception {
		LowerCaseConverter result;

		// default test
		result = LowerCaseConverter.getInstance();
	}
}