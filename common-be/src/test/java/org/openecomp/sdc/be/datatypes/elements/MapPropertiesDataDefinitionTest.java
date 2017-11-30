package org.openecomp.sdc.be.datatypes.elements;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;


public class MapPropertiesDataDefinitionTest {

	private MapPropertiesDataDefinition createTestSubject() {
		Map map = new HashMap<>();
		return new MapPropertiesDataDefinition(new MapDataDefinition(map), "");
	}

	
	@Test
	public void testGetMapToscaDataDefinition() throws Exception {
		MapPropertiesDataDefinition testSubject;
		Map<String, PropertyDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMapToscaDataDefinition();
	}

	
	@Test
	public void testSetMapToscaDataDefinition() throws Exception {
		MapPropertiesDataDefinition testSubject;
		Map<String, PropertyDataDefinition> mapToscaDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMapToscaDataDefinition(mapToscaDataDefinition);
	}

	
	@Test
	public void testGetParentName() throws Exception {
		MapPropertiesDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getParentName();
	}

	
	@Test
	public void testSetParentName() throws Exception {
		MapPropertiesDataDefinition testSubject;
		String parentName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setParentName(parentName);
	}
}