package org.openecomp.sdc.be.datatypes.elements;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;


public class ListMapPropertiesDataDefinitionTest {

	private ListMapPropertiesDataDefinition createTestSubject() {
		return new ListMapPropertiesDataDefinition();
	}

	@Test
	public void testCopyConstructor() throws Exception {
		ListMapPropertiesDataDefinition testSubject;
		List<MapPropertiesDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		new ListMapPropertiesDataDefinition(testSubject);
		new ListMapPropertiesDataDefinition(new LinkedList<>());
	}
	
	@Test
	public void testGetListToscaDataDefinition() throws Exception {
		ListMapPropertiesDataDefinition testSubject;
		List<MapPropertiesDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getListToscaDataDefinition();
	}

	
	@Test
	public void testSetMapToscaDataDefinition() throws Exception {
		ListMapPropertiesDataDefinition testSubject;
		List<MapPropertiesDataDefinition> listToscaDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMapToscaDataDefinition(listToscaDataDefinition);
	}
}