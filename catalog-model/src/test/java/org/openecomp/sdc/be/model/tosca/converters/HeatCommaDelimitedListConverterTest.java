package org.openecomp.sdc.be.model.tosca.converters;

import org.junit.Test;


public class HeatCommaDelimitedListConverterTest {

	private HeatCommaDelimitedListConverter createTestSubject() {
		return HeatCommaDelimitedListConverter.getInstance();
	}

	
	@Test
	public void testGetInstance() throws Exception {
		HeatCommaDelimitedListConverter result;

		// default test
		result = HeatCommaDelimitedListConverter.getInstance();
	}

	

}