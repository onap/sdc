package org.openecomp.sdc.be.datatypes.elements;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;


public class ListRequirementDataDefinitionTest {

	private ListRequirementDataDefinition createTestSubject() {
		return new ListRequirementDataDefinition();
	}

	@Test
	public void testOverloadConstructors() throws Exception {
		ListRequirementDataDefinition testSubject;
		List<RequirementDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getListToscaDataDefinition();
		new ListRequirementDataDefinition(testSubject);
		new ListRequirementDataDefinition(new LinkedList<>());
	}
	
	@Test
	public void testGetListToscaDataDefinition() throws Exception {
		ListRequirementDataDefinition testSubject;
		List<RequirementDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getListToscaDataDefinition();
	}

	
	@Test
	public void testSetListToscaDataDefinition() throws Exception {
		ListRequirementDataDefinition testSubject;
		List<RequirementDataDefinition> listToscaDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setListToscaDataDefinition(listToscaDataDefinition);
	}
}