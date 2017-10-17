package org.openecomp.sdc.be.resources.data;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;


public class AttributeDataTest {

	private AttributeData createTestSubject() {
		return new AttributeData();
	}

	
	@Test
	public void testToString() throws Exception {
		AttributeData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		AttributeData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testGetAttributeDataDefinition() throws Exception {
		AttributeData testSubject;
		PropertyDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAttributeDataDefinition();
	}

	
	@Test
	public void testSetAttributeDataDefinition() throws Exception {
		AttributeData testSubject;
		PropertyDataDefinition attributeDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setAttributeDataDefinition(attributeDataDefinition);
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		AttributeData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}
}