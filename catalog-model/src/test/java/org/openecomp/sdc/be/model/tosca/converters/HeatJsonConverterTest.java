package org.openecomp.sdc.be.model.tosca.converters;

import org.junit.Test;


public class HeatJsonConverterTest {

	private HeatJsonConverter createTestSubject() {
		return HeatJsonConverter.getInstance();
	}

	
	@Test
	public void testGetInstance() throws Exception {
		HeatJsonConverter result;

		// default test
		result = HeatJsonConverter.getInstance();
	}

	

}