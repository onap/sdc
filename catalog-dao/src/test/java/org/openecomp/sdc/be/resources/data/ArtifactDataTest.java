package org.openecomp.sdc.be.resources.data;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;


public class ArtifactDataTest {

	private ArtifactData createTestSubject() {
		return new ArtifactData();
	}

	
	@Test
	public void testGetArtifactDataDefinition() throws Exception {
		ArtifactData testSubject;
		ArtifactDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactDataDefinition();
	}

	
	@Test
	public void testSetArtifactDataDefinition() throws Exception {
		ArtifactData testSubject;
		ArtifactDataDefinition artifactDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactDataDefinition(artifactDataDefinition);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		ArtifactData testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		ArtifactData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testToString() throws Exception {
		ArtifactData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}