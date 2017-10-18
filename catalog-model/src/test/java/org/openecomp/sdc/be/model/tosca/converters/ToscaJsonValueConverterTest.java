package org.openecomp.sdc.be.model.tosca.converters;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.model.DataTypeDefinition;


public class ToscaJsonValueConverterTest {

	private ToscaJsonValueConverter createTestSubject() {
		return ToscaJsonValueConverter.getInstance();
	}

	
	@Test
	public void testGetInstance() throws Exception {
		ToscaJsonValueConverter result;

		// default test
		result = ToscaJsonValueConverter.getInstance();
	}

	
	@Test
	public void testConvertToToscaValue() throws Exception {
		ToscaJsonValueConverter testSubject;
		String value = "";
		String innerType = "";
		Map<String, DataTypeDefinition> dataTypes = null;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertToToscaValue(value, innerType, dataTypes);
	}
}