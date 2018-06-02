package org.openecomp.sdc.be.dao.neo4j;

import org.junit.Test;

public class GraphPropertiesDictionaryTest {

	private GraphPropertiesDictionary createTestSubject() {
		return GraphPropertiesDictionary.ADDITIONAL_INFO_ID_TO_KEY;
	}

	@Test
	public void testGetProperty() throws Exception {
		GraphPropertiesDictionary testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperty();
	}

	@Test
	public void testGetClazz() throws Exception {
		GraphPropertiesDictionary testSubject;
		Class result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getClazz();
	}

	@Test
	public void testIsUnique() throws Exception {
		GraphPropertiesDictionary testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isUnique();
	}

	@Test
	public void testIsIndexed() throws Exception {
		GraphPropertiesDictionary testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIndexed();
	}
}