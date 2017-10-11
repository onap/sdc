package org.openecomp.sdc.be.datatypes.elements;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;


public class ListRequirementDataDefinitionTest {

	private ListRequirementDataDefinition createTestSubject() {
		List list = new ArrayList<>();
		return new ListRequirementDataDefinition(list);
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