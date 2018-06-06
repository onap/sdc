package org.openecomp.sdc.be.model.tosca.converters;

import org.junit.Test;

public class ToscaMapValueConverterTest {

	private ToscaMapValueConverter createTestSubject() {
		return ToscaMapValueConverter.getInstance();
	}

	
	@Test
	public void testGetInstance() throws Exception {
		ToscaMapValueConverter result;

		// default test
		result = ToscaMapValueConverter.getInstance();
	}


}