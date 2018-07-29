package org.openecomp.sdc.be.datatypes.elements;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;


public class MapArtifactDataDefinitionTest {

	private MapArtifactDataDefinition createTestSubject() {
		return new MapArtifactDataDefinition();
	}

	@Test
	public void testOverloadConstructors() throws Exception {
		MapArtifactDataDefinition testSubject;
		Map<String, ArtifactDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		new MapArtifactDataDefinition(new HashMap<>());
		new MapArtifactDataDefinition(testSubject, "");
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