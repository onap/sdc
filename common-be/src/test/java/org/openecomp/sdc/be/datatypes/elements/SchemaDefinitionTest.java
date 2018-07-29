package org.openecomp.sdc.be.datatypes.elements;

import org.apache.commons.collections.map.HashedMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SchemaDefinitionTest {

	private SchemaDefinition createTestSubject() {
		return new SchemaDefinition();
	}
	
	@Test
	public void testConstructors() throws Exception {
		SchemaDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		new SchemaDefinition("mock", new LinkedList<>(), new HashedMap());
	}
	
	@Test
	public void testGetDerivedFrom() throws Exception {
		SchemaDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDerivedFrom();
	}

	@Test
	public void testSetProperty() throws Exception {
		SchemaDefinition testSubject;
		PropertyDataDefinition property = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperty(property);
	}

	@Test
	public void testGetProperty() throws Exception {
		SchemaDefinition testSubject;
		PropertyDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperty();
	}

	@Test
	public void testSetDerivedFrom() throws Exception {
		SchemaDefinition testSubject;
		String derivedFrom = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDerivedFrom(derivedFrom);
	}

	@Test
	public void testGetConstraints() throws Exception {
		SchemaDefinition testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConstraints();
	}

	@Test
	public void testSetConstraints() throws Exception {
		SchemaDefinition testSubject;
		List<String> constraints = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setConstraints(constraints);
	}

	@Test
	public void testGetProperties() throws Exception {
		SchemaDefinition testSubject;
		Map<String, PropertyDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	@Test
	public void testSetProperties() throws Exception {
		SchemaDefinition testSubject;
		Map<String, PropertyDataDefinition> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}

	@Test
	public void testAddProperty() throws Exception {
		SchemaDefinition testSubject;
		String key = "";
		PropertyDataDefinition property = new PropertyDataDefinition();

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(new HashMap<>() );
		testSubject.addProperty("mock", property);
	}

	@Test
	public void testHashCode() throws Exception {
		SchemaDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	@Test
	public void testEquals() throws Exception {
		SchemaDefinition testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		obj = null;
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		result = testSubject.equals(createTestSubject());
		Assert.assertEquals(true, result);
	}

	@Test
	public void testToString() throws Exception {
		SchemaDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}