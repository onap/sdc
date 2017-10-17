package org.openecomp.sdc.be.model;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.PropertyRule;


public class ComponentInstanceInputTest {

	private ComponentInstanceInput createTestSubject() {
		return new ComponentInstanceInput();
	}

	
	@Test
	public void testGetComponentInstanceName() throws Exception {
		ComponentInstanceInput testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstanceName();
	}

	
	@Test
	public void testSetComponentInstanceName() throws Exception {
		ComponentInstanceInput testSubject;
		String componentInstanceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstanceName(componentInstanceName);
	}

	
	@Test
	public void testGetComponentInstanceId() throws Exception {
		ComponentInstanceInput testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstanceId();
	}

	
	@Test
	public void testSetComponentInstanceId() throws Exception {
		ComponentInstanceInput testSubject;
		String componentInstanceId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstanceId(componentInstanceId);
	}

	
	@Test
	public void testGetValueUniqueUid() throws Exception {
		ComponentInstanceInput testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValueUniqueUid();
	}

	
	@Test
	public void testSetValueUniqueUid() throws Exception {
		ComponentInstanceInput testSubject;
		String valueUniqueUid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setValueUniqueUid(valueUniqueUid);
	}

	
	@Test
	public void testGetPath() throws Exception {
		ComponentInstanceInput testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPath();
	}

	
	@Test
	public void testSetPath() throws Exception {
		ComponentInstanceInput testSubject;
		List<String> path = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setPath(path);
	}

	
	@Test
	public void testGetRules() throws Exception {
		ComponentInstanceInput testSubject;
		List<PropertyRule> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRules();
	}

	
	@Test
	public void testSetRules() throws Exception {
		ComponentInstanceInput testSubject;
		List<PropertyRule> rules = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRules(rules);
	}

	
	@Test
	public void testToString() throws Exception {
		ComponentInstanceInput testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}