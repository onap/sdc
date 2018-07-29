package org.openecomp.sdc.be.datatypes.elements;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class MapCapabilityPropertyTest {

	private MapCapabilityProperty createTestSubject() {
		return new MapCapabilityProperty();
	}
	
	@Test
	public void testOverloadConstructor() throws Exception {
		MapCapabilityProperty testSubject;
		Map<String, MapPropertiesDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		new MapCapabilityProperty(new HashMap<>());
	}
	
	@Test
	public void testGetMapToscaDataDefinition() throws Exception {
		MapCapabilityProperty testSubject;
		Map<String, MapPropertiesDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMapToscaDataDefinition();
	}

	@Test
	public void testSetMapToscaDataDefinition() throws Exception {
		MapCapabilityProperty testSubject;
		Map<String, MapPropertiesDataDefinition> mapToscaDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMapToscaDataDefinition(mapToscaDataDefinition);
	}
}