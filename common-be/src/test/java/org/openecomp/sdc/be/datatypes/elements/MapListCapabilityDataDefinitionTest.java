package org.openecomp.sdc.be.datatypes.elements;

import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;

import java.util.Map;


public class MapListCapabilityDataDefinitionTest {

	private MapListCapabilityDataDefinition createTestSubject() {
		return new MapListCapabilityDataDefinition();
	}

	@Test
	public void testConstructors() throws Exception {
		MapListCapabilityDataDefinition testSubject;
		Map<String, ListCapabilityDataDefinition> result;

		// default test
		new MapListCapabilityDataDefinition(new HashedMap());
		new MapListCapabilityDataDefinition(createTestSubject());
	}
	
	@Test
	public void testGetMapToscaDataDefinition() throws Exception {
		MapListCapabilityDataDefinition testSubject;
		Map<String, ListCapabilityDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMapToscaDataDefinition();
	}

	
	@Test
	public void testAdd() throws Exception {
		MapListCapabilityDataDefinition testSubject;
		String key = "";
		CapabilityDataDefinition value = null;

		// default test
		testSubject = createTestSubject();
		testSubject.add(key, value);
	}
}