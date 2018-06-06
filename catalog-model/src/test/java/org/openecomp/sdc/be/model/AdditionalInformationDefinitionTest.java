package org.openecomp.sdc.be.model;

import java.util.LinkedList;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterDataDefinition;


public class AdditionalInformationDefinitionTest {

	private AdditionalInformationDefinition createTestSubject() {
		return new AdditionalInformationDefinition();
	}

	@Test
	public void testCtor() throws Exception {
		new AdditionalInformationDefinition(new AdditionalInformationDefinition());
		new AdditionalInformationDefinition(new AdditionalInfoParameterDataDefinition());
		new AdditionalInformationDefinition(new AdditionalInformationDefinition(), "mock", new LinkedList<>());
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