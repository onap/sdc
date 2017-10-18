package org.openecomp.sdc.be.model.tosca.converters;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.model.DataTypeDefinition;


public class ToscaStringConvertorTest {

	private ToscaStringConvertor createTestSubject() {
		return ToscaStringConvertor.getInstance();
	}

	
	@Test
	public void testGetInstance() throws Exception {
		ToscaStringConvertor result;

		// default test
		result = ToscaStringConvertor.getInstance();
	}

	
	@Test
	public void testConvertToToscaValue() throws Exception {
		ToscaStringConvertor testSubject;
		String value = "";
		String innerType = "";
		Map<String, DataTypeDefinition> dataTypes = null;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertToToscaValue(value, innerType, dataTypes);
	}
}