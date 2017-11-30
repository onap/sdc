package org.openecomp.sdc.be.model.tosca.converters;

import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.be.model.DataTypeDefinition;


public class DefaultConverterTest {

	private DefaultConverter createTestSubject() {
		return DefaultConverter.getInstance();
	}

	
	@Test
	public void testGetInstance() throws Exception {
		DefaultConverter result;

		// default test
		result = DefaultConverter.getInstance();
	}

	
	@Test
	public void testConvert() throws Exception {
		DefaultConverter testSubject;
		String value = "";
		String innerType = "";
		Map<String, DataTypeDefinition> dataTypes = null;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convert(value, innerType, dataTypes);
	}
}