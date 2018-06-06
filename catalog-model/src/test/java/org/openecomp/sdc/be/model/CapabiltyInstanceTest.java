package org.openecomp.sdc.be.model;

import java.util.Map;

import org.junit.Test;

public class CapabiltyInstanceTest {

	private CapabiltyInstance createTestSubject() {
		return new CapabiltyInstance();
	}

	@Test
	public void testGetUniqueId() throws Exception {
		CapabiltyInstance testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	@Test
	public void testSetUniqueId() throws Exception {
		CapabiltyInstance testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	@Test
	public void testGetProperties() throws Exception {
		CapabiltyInstance testSubject;
		Map<String, String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	@Test
	public void testSetProperties() throws Exception {
		CapabiltyInstance testSubject;
		Map<String, String> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}

	@Test
	public void testToString() throws Exception {
		CapabiltyInstance testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}