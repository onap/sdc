package org.openecomp.sdc.be.model.tosca.converters;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.model.DataTypeDefinition;


public class BooleanConverterTest {

	private BooleanConverter createTestSubject() {
		return BooleanConverter.getInstance();
	}

	
	@Test
	public void testGetInstance() throws Exception {
		BooleanConverter result;

		// default test
		result = BooleanConverter.getInstance();
	}

	
	@Test
	public void testConvertToToscaValue() throws Exception {
		BooleanConverter testSubject;
		String value = "";
		String innerType = "";
		Map<String, DataTypeDefinition> dataTypes = null;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertToToscaValue(value, innerType, dataTypes);
	}
}