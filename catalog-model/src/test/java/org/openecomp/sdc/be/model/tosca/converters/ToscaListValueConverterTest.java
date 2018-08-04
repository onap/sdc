package org.openecomp.sdc.be.model.tosca.converters;

import org.junit.Test;


public class ToscaListValueConverterTest {

	private ToscaListValueConverter createTestSubject() {
		return  ToscaListValueConverter.getInstance();
	}

	
	@Test
	public void testGetInstance() throws Exception {
		ToscaListValueConverter result;

		// default test
		result = ToscaListValueConverter.getInstance();
	}

	

}