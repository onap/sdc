package org.openecomp.sdc.be.model.tosca.converters;

import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.be.model.DataTypeDefinition;


public class JsonConverterTest {

	private JsonConverter createTestSubject() {
		return JsonConverter.getInstance();
	}

	
	@Test
	public void testGetInstance() throws Exception {
		JsonConverter result;

		// default test
		result = JsonConverter.getInstance();
	}

	
	@Test
	public void testConvert() throws Exception {
		JsonConverter testSubject;
		String value = "";
		String innerType = "";
		Map<String, DataTypeDefinition> dataTypes = null;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convert(value, innerType, dataTypes);
	}
}