package org.openecomp.sdc.be.datatypes.elements;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;


public class MapArtifactDataDefinitionTest {

	private MapArtifactDataDefinition createTestSubject() {
		
		Map map = new HashMap<>();
		
		return new MapArtifactDataDefinition(new MapDataDefinition(map), "");
	}

	
	@Test
	public void testGetMapToscaDataDefinition() throws Exception {
		MapArtifactDataDefinition testSubject;
		Map<String, ArtifactDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMapToscaDataDefinition();
	}

	
	@Test
	public void testSetMapToscaDataDefinition() throws Exception {
		MapArtifactDataDefinition testSubject;
		Map<String, ArtifactDataDefinition> mapToscaDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMapToscaDataDefinition(mapToscaDataDefinition);
	}

	
	@Test
	public void testGetParentName() throws Exception {
		MapArtifactDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getParentName();
	}

	
	@Test
	public void testSetParentName() throws Exception {
		MapArtifactDataDefinition testSubject;
		String parentName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setParentName(parentName);
	}
}