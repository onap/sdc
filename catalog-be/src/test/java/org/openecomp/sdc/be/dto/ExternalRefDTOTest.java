package org.openecomp.sdc.be.dto;

import org.junit.Test;

public class ExternalRefDTOTest {

	private ExternalRefDTO createTestSubject() {
		return new ExternalRefDTO();
	}

	@Test
	public void testConstructor() throws Exception {
		new ExternalRefDTO("mock");
	}
	
	@Test
	public void testGetReferenceUUID() throws Exception {
		ExternalRefDTO testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getReferenceUUID();
	}

	@Test
	public void testSetReferenceUUID() throws Exception {
		ExternalRefDTO testSubject;
		String referenceUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setReferenceUUID(referenceUUID);
	}

	@Test
	public void testToString() throws Exception {
		ExternalRefDTO testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}