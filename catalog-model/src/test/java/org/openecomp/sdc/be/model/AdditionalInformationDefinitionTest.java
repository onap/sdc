package org.openecomp.sdc.be.model;

import org.junit.Test;


public class AdditionalInformationDefinitionTest {

	private AdditionalInformationDefinition createTestSubject() {
		return new AdditionalInformationDefinition();
	}

	
	@Test
	public void testGetParentUniqueId() throws Exception {
		AdditionalInformationDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getParentUniqueId();
	}

	
	@Test
	public void testSetParentUniqueId() throws Exception {
		AdditionalInformationDefinition testSubject;
		String parentUniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setParentUniqueId(parentUniqueId);
	}

	
	@Test
	public void testToString() throws Exception {
		AdditionalInformationDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}