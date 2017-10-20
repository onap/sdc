package org.openecomp.sdc.be.tosca;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;

import fj.data.Either;

public class PropertyConvertorTest {

	private PropertyConvertor createTestSubject() {
		return new PropertyConvertor();
	}

	
	@Test
	public void testGetInstance() throws Exception {
		PropertyConvertor result;

		// default test
		result = PropertyConvertor.getInstance();
	}

	
	@Test
	public void testConvertProperties() throws Exception {
		PropertyConvertor testSubject;
		Component component = null;
		ToscaNodeType toscaNodeType = null;
		Map<String, DataTypeDefinition> dataTypes = null;
		Either<ToscaNodeType, ToscaError> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertProperties(component, toscaNodeType, dataTypes);
	}

	
	@Test
	public void testConvertProperty() throws Exception {
		PropertyConvertor testSubject;
		Map<String, DataTypeDefinition> dataTypes = null;
		PropertyDefinition property = null;
		boolean isCapabiltyProperty = false;
		ToscaProperty result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testConvertToToscaObject() throws Exception {
		PropertyConvertor testSubject;
		String propertyType = "";
		String value = "";
		String innerType = "";
		Map<String, DataTypeDefinition> dataTypes = null;
		Object result;

		// default test
		testSubject = createTestSubject();
	}
}