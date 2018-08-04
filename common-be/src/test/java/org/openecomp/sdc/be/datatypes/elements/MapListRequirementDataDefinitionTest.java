package org.openecomp.sdc.be.datatypes.elements;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;


public class MapListRequirementDataDefinitionTest {

	private MapListRequirementDataDefinition createTestSubject() {
		Map map = new HashMap<>();
		return new MapListRequirementDataDefinition(map);
	}

	
	@Test
	public void testGetMapToscaDataDefinition() throws Exception {
		MapListRequirementDataDefinition testSubject;
		Map<String, ListRequirementDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMapToscaDataDefinition();
	}

	
	@Test
	public void testAdd() throws Exception {
		MapListRequirementDataDefinition testSubject;
		String key = "";
		RequirementDataDefinition value = null;

		// default test
		testSubject = createTestSubject();
		testSubject.add(key, value);
	}
}