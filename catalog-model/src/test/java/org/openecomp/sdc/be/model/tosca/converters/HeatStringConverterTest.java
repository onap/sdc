package org.openecomp.sdc.be.model.tosca.converters;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.model.DataTypeDefinition;

public class HeatStringConverterTest {

	@Test
	public void convertString_strWithQuotes_returnStringAsIs() {
		String str = "'i'm string with \"quote\"'";
		String convert = HeatStringConverter.getInstance().convert(str, null, null);
		assertEquals(str, convert);
	}

	private HeatStringConverter createTestSubject() {
		return HeatStringConverter.getInstance();
	}

	
	@Test
	public void testGetInstance() throws Exception {
		HeatStringConverter result;

		// default test
		result = HeatStringConverter.getInstance();
	}

	


}
