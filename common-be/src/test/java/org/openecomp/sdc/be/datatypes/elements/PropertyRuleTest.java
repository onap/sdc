package org.openecomp.sdc.be.datatypes.elements;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class PropertyRuleTest {

	private PropertyRule createTestSubject() {
		return new PropertyRule();
	}

	@Test
	public void testConstructor() throws Exception {
		PropertyRule testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		new PropertyRule(new LinkedList<>(), "");
	}
	
	@Test
	public void testGetRule() throws Exception {
		PropertyRule testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRule();
	}

	@Test
	public void testSetRule() throws Exception {
		PropertyRule testSubject;
		List<String> rule = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRule(rule);
	}

	@Test
	public void testGetValue() throws Exception {
		PropertyRule testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
	}

	@Test
	public void testSetValue() throws Exception {
		PropertyRule testSubject;
		String value = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setValue(value);
	}

	@Test
	public void testGetFirstToken() throws Exception {
		PropertyRule testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFirstToken();
	}

	@Test
	public void testGetToken() throws Exception {
		PropertyRule testSubject;
		int tokenNumber = 0;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToken(tokenNumber);
	}

	@Test
	public void testGetRuleSize() throws Exception {
		PropertyRule testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRuleSize();
	}

	@Test
	public void testToString() throws Exception {
		PropertyRule testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	@Test
	public void testCompareRule() throws Exception {
		PropertyRule testSubject;
		PropertyRule comparedPropertyRule = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		comparedPropertyRule = null;
		result = testSubject.compareRule(comparedPropertyRule);
		Assert.assertEquals(false, result);
	}

	@Test
	public void testReplaceFirstToken() throws Exception {
		PropertyRule testSubject;
		String token = "";

		// default test
		testSubject = createTestSubject();
		testSubject.replaceFirstToken(token);
	}
}