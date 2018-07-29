package org.openecomp.sdc.be.datatypes.elements;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;


public class ListCapabilityDataDefinitionTest {

	private ListCapabilityDataDefinition createTestSubject() {
		return new ListCapabilityDataDefinition();
	}

	@Test
	public void testOverloadConstructors() throws Exception {
		ListCapabilityDataDefinition testSubject;
		List<CapabilityDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		new ListCapabilityDataDefinition(testSubject);
		new ListCapabilityDataDefinition(new LinkedList<>());
	}
	
	@Test
	public void testGetListToscaDataDefinition() throws Exception {
		ListCapabilityDataDefinition testSubject;
		List<CapabilityDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getListToscaDataDefinition();
	}

	
	@Test
	public void testSetListToscaDataDefinition() throws Exception {
		ListCapabilityDataDefinition testSubject;
		List<CapabilityDataDefinition> listToscaDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setListToscaDataDefinition(listToscaDataDefinition);
	}
}