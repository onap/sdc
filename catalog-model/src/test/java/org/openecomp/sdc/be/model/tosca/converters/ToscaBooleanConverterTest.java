package org.openecomp.sdc.be.model.tosca.converters;

import org.junit.Test;

public class ToscaBooleanConverterTest {

	private ToscaBooleanConverter createTestSubject() {
		return  ToscaBooleanConverter.getInstance();
	}

	
	@Test
	public void testGetInstance() throws Exception {
		ToscaBooleanConverter result;

		// default test
		result = ToscaBooleanConverter.getInstance();
	}

	
}