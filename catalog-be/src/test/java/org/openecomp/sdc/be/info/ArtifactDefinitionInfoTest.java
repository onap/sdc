package org.openecomp.sdc.be.info;

import org.junit.Test;
import org.openecomp.sdc.be.model.ArtifactDefinition;


public class ArtifactDefinitionInfoTest {

	private ArtifactDefinitionInfo createTestSubject() {
		return new ArtifactDefinitionInfo(new ArtifactDefinition());
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		ArtifactDefinitionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		ArtifactDefinitionInfo testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetArtifactName() throws Exception {
		ArtifactDefinitionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactName();
	}

	
	@Test
	public void testSetArtifactName() throws Exception {
		ArtifactDefinitionInfo testSubject;
		String artifactName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactName(artifactName);
	}

	
	@Test
	public void testGetArtifactDisplayName() throws Exception {
		ArtifactDefinitionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactDisplayName();
	}

	
	@Test
	public void testSetArtifactDisplayName() throws Exception {
		ArtifactDefinitionInfo testSubject;
		String artifactDisplayName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactDisplayName(artifactDisplayName);
	}

	
	@Test
	public void testGetArtifactVersion() throws Exception {
		ArtifactDefinitionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactVersion();
	}

	
	@Test
	public void testSetArtifactVersion() throws Exception {
		ArtifactDefinitionInfo testSubject;
		String artifactVersion = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactVersion(artifactVersion);
	}

	
	@Test
	public void testGetArtifactUUID() throws Exception {
		ArtifactDefinitionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactUUID();
	}

	
	@Test
	public void testSetArtifactUUID() throws Exception {
		ArtifactDefinitionInfo testSubject;
		String artifactUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactUUID(artifactUUID);
	}
}