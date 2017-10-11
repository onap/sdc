package org.openecomp.sdc.be.datatypes.elements;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;


public class ListMapPropertiesDataDefinitionTest {

	private ListMapPropertiesDataDefinition createTestSubject() {
		List list = new ArrayList<>();
		return new ListMapPropertiesDataDefinition(list);
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