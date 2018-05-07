package org.openecomp.sdc.be.datatypes.elements;

import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;


public class MapListCapabiltyDataDefinitionTest {

	private MapListCapabiltyDataDefinition createTestSubject() {
		return new MapListCapabiltyDataDefinition();
	}

	@Test
	public void testConstructors() throws Exception {
		MapListCapabiltyDataDefinition testSubject;
		Map<String, ListCapabilityDataDefinition> result;

		// default test
		new MapListCapabiltyDataDefinition(new HashedMap());
		new MapListCapabiltyDataDefinition(createTestSubject());
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