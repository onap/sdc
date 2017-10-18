package org.openecomp.sdc.be.model.tosca.converters;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.model.DataTypeDefinition;


public class ToscaValueDefaultConverterTest {

	private ToscaValueDefaultConverter createTestSubject() {
		return ToscaValueDefaultConverter.getInstance();
	}

	
	@Test
	public void testGetInstance() throws Exception {
		ToscaValueDefaultConverter result;

		// default test
		result = ToscaValueDefaultConverter.getInstance();
	}

	
	@Test
	public void testConvertToToscaValue() throws Exception {
		ToscaValueDefaultConverter testSubject;
		String value = "";
		String innerType = "";
		Map<String, DataTypeDefinition> dataTypes = null;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertToToscaValue(value, innerType, dataTypes);
	}
}