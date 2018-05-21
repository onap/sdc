package org.openecomp.sdc.be.info;

import org.junit.Test;

public class RelatedToPropertyTest {

	private RelatedToProperty createTestSubject() {
		return new RelatedToProperty();
	}

	@Test
	public void testGetPropertyKey() throws Exception {
		RelatedToProperty testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPropertyKey();
	}

	@Test
	public void testSetPropertyKey() throws Exception {
		RelatedToProperty testSubject;
		String propertyKey = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPropertyKey(propertyKey);
	}

	@Test
	public void testGetPropertyValue() throws Exception {
		RelatedToProperty testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPropertyValue();
	}

	@Test
	public void testSetPropertyValue() throws Exception {
		RelatedToProperty testSubject;
		String propertyValue = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPropertyValue(propertyValue);
	}
}