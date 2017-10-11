package org.openecomp.sdc.be.datatypes.elements;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;


public class ListCapabilityDataDefinitionTest {

	private ListCapabilityDataDefinition createTestSubject() {
		List list = new ArrayList<>();
		return new ListCapabilityDataDefinition(list);
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