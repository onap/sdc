package org.openecomp.sdc.be.model.tosca.converters;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.model.DataTypeDefinition;


public class IntegerConverterTest {

	private IntegerConverter createTestSubject() {
		return IntegerConverter.getInstance();
	}

	
	@Test
	public void testGetInstance() throws Exception {
		IntegerConverter result;

		// default test
		result = IntegerConverter.getInstance();
	}

	
	@Test
	public void testConvertToToscaValue() throws Exception {
		IntegerConverter testSubject;
		String value = "";
		String innerType = "";
		Map<String, DataTypeDefinition> dataTypes = null;
		Object result;

		// test 1
		testSubject = createTestSubject();
		value = null;
		result = testSubject.convertToToscaValue(value, innerType, dataTypes);
		Assert.assertEquals(null, result);

		// test 2
		testSubject = createTestSubject();
		value = "";
		result = testSubject.convertToToscaValue(value, innerType, dataTypes);
		Assert.assertEquals(null, result);
	}
}