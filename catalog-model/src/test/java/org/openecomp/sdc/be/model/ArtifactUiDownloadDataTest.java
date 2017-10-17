package org.openecomp.sdc.be.model;

import javax.annotation.Generated;

import org.junit.Test;

public class ArtifactUiDownloadDataTest {

	private ArtifactUiDownloadData createTestSubject() {
		return new ArtifactUiDownloadData();
	}

	
	@Test
	public void testSetArtifactName() throws Exception {
		ArtifactUiDownloadData testSubject;
		String artifactName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactName(artifactName);
	}

	
	@Test
	public void testSetBase64Contents() throws Exception {
		ArtifactUiDownloadData testSubject;
		String base64Contents = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setBase64Contents(base64Contents);
	}

	
	@Test
	public void testGetArtifactName() throws Exception {
		ArtifactUiDownloadData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactName();
	}

	
	@Test
	public void testGetBase64Contents() throws Exception {
		ArtifactUiDownloadData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getBase64Contents();
	}
}