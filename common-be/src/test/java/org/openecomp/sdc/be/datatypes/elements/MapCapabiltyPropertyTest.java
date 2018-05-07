package org.openecomp.sdc.be.datatypes.elements;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class MapCapabiltyPropertyTest {

	private MapCapabiltyProperty createTestSubject() {
		return new MapCapabiltyProperty();
	}
	
	@Test
	public void testOverloadConstructor() throws Exception {
		MapCapabiltyProperty testSubject;
		Map<String, MapPropertiesDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		new MapCapabiltyProperty(new HashMap<>());
	}
	
	@Test
	public void testGetMapToscaDataDefinition() throws Exception {
		MapCapabiltyProperty testSubject;
		Map<String, MapPropertiesDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMapToscaDataDefinition();
	}

	@Test
	public void testSetMapToscaDataDefinition() throws Exception {
		MapCapabiltyProperty testSubject;
		Map<String, MapPropertiesDataDefinition> mapToscaDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMapToscaDataDefinition(mapToscaDataDefinition);
	}
}