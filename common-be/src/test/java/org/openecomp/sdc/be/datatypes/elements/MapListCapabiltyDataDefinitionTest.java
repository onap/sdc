package org.openecomp.sdc.be.datatypes.elements;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;


public class MapListCapabiltyDataDefinitionTest {

	private MapListCapabiltyDataDefinition createTestSubject() {
		Map map = new HashMap<>();
		return new MapListCapabiltyDataDefinition(map);
	}

	
	@Test
	public void testGetMapToscaDataDefinition() throws Exception {
		MapListCapabiltyDataDefinition testSubject;
		Map<String, ListCapabilityDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMapToscaDataDefinition();
	}

	
	@Test
	public void testAdd() throws Exception {
		MapListCapabiltyDataDefinition testSubject;
		String key = "";
		CapabilityDataDefinition value = null;

		// default test
		testSubject = createTestSubject();
		testSubject.add(key, value);
	}
}