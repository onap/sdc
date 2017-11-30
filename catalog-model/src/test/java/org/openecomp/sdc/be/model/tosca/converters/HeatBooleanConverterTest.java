package org.openecomp.sdc.be.model.tosca.converters;

import org.junit.Test;


public class HeatBooleanConverterTest {

	private HeatBooleanConverter createTestSubject() {
		return HeatBooleanConverter.getInstance();
	}

	
	@Test
	public void testGetInstance() throws Exception {
		HeatBooleanConverter result;

		// default test
		result = HeatBooleanConverter.getInstance();
	}

	

}