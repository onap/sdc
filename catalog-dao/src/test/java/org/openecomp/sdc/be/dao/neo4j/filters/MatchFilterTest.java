package org.openecomp.sdc.be.dao.neo4j.filters;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;


public class MatchFilterTest {

	private MatchFilter createTestSubject() {
		return new MatchFilter();
	}

	
	@Test
	public void testGetProperties() throws Exception {
		MatchFilter testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		MatchFilter testSubject;
		Map<String, Object> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}

	
	@Test
	public void testAddToMatch() throws Exception {
		MatchFilter testSubject;
		String propName = "";
		Object value = null;
		MatchFilter result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.addToMatch(propName, value);
	}
}