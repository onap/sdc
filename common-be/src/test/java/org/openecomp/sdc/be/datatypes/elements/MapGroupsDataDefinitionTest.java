package org.openecomp.sdc.be.datatypes.elements;

import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;

import java.util.Map;

public class MapGroupsDataDefinitionTest {

	private MapGroupsDataDefinition createTestSubject() {
		return new MapGroupsDataDefinition();
	}
	
	@Test
	public void testConstructors() throws Exception {
		MapGroupsDataDefinition testSubject;
		Map<String, GroupInstanceDataDefinition> result;

		// default test
		new MapGroupsDataDefinition(new HashedMap());
		new MapGroupsDataDefinition(new MapDataDefinition<>(), "");
	}
	
	@Test
	public void testGetMapToscaDataDefinition() throws Exception {
		MapGroupsDataDefinition testSubject;
		Map<String, GroupInstanceDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMapToscaDataDefinition();
	}

	@Test
	public void testSetMapToscaDataDefinition() throws Exception {
		MapGroupsDataDefinition testSubject;
		Map<String, GroupInstanceDataDefinition> mapToscaDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMapToscaDataDefinition(mapToscaDataDefinition);
	}

	@Test
	public void testGetParentName() throws Exception {
		MapGroupsDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getParentName();
	}

	@Test
	public void testSetParentName() throws Exception {
		MapGroupsDataDefinition testSubject;
		String parentName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setParentName(parentName);
	}
}