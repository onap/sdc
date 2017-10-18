package org.openecomp.sdc.be.model.tosca.converters;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.model.DataTypeDefinition;

import fj.data.Either;


public class MapConverterTest {

	private MapConverter createTestSubject() {
		return new MapConverter();
	}

	
	@Test
	public void testGetInstance() throws Exception {
		MapConverter result;

		// default test
		result = MapConverter.getInstance();
	}

	
	@Test
	public void testConvert() throws Exception {
		MapConverter testSubject;
		String value = "";
		String innerType = "";
		Map<String, DataTypeDefinition> dataTypes = null;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convert(value, innerType, dataTypes);
	}

	


	

}