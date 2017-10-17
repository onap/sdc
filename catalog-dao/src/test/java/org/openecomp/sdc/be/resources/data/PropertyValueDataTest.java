package org.openecomp.sdc.be.resources.data;

import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.PropertyRule;


public class PropertyValueDataTest {

	private PropertyValueData createTestSubject() {
		return new PropertyValueData();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		PropertyValueData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		PropertyValueData testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetType() throws Exception {
		PropertyValueData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		PropertyValueData testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	
	@Test
	public void testGetCreationTime() throws Exception {
		PropertyValueData testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreationTime();
	}

	
	@Test
	public void testSetCreationTime() throws Exception {
		PropertyValueData testSubject;
		Long creationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCreationTime(creationTime);
	}

	
	@Test
	public void testGetModificationTime() throws Exception {
		PropertyValueData testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getModificationTime();
	}

	
	@Test
	public void testSetModificationTime() throws Exception {
		PropertyValueData testSubject;
		Long modificationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setModificationTime(modificationTime);
	}

	
	@Test
	public void testGetValue() throws Exception {
		PropertyValueData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
	}

	
	@Test
	public void testSetValue() throws Exception {
		PropertyValueData testSubject;
		String value = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setValue(value);
	}

	
	@Test
	public void testGetRules() throws Exception {
		PropertyValueData testSubject;
		List<PropertyRule> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRules();
	}

	
	@Test
	public void testSetRules() throws Exception {
		PropertyValueData testSubject;
		List<PropertyRule> rules = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRules(rules);
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		PropertyValueData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testToString() throws Exception {
		PropertyValueData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}