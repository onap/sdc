package org.openecomp.sdc.be.externalapi.servlet.representation;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;

public class ResourceAssetDetailedMetadataTest {

	private ResourceAssetDetailedMetadata createTestSubject() {
		return new ResourceAssetDetailedMetadata();
	}

	
	@Test
	public void testGetLastUpdaterFullName() throws Exception {
		ResourceAssetDetailedMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLastUpdaterFullName();
	}

	
	@Test
	public void testSetLastUpdaterFullName() throws Exception {
		ResourceAssetDetailedMetadata testSubject;
		String lastUpdaterFullName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLastUpdaterFullName(lastUpdaterFullName);
	}

	
	@Test
	public void testGetToscaResourceName() throws Exception {
		ResourceAssetDetailedMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToscaResourceName();
	}

	
	@Test
	public void testSetToscaResourceName() throws Exception {
		ResourceAssetDetailedMetadata testSubject;
		String toscaResourceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setToscaResourceName(toscaResourceName);
	}

	
	@Test
	public void testGetResources() throws Exception {
		ResourceAssetDetailedMetadata testSubject;
		List<ResourceInstanceMetadata> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResources();
	}

	
	@Test
	public void testSetResources() throws Exception {
		ResourceAssetDetailedMetadata testSubject;
		List<ResourceInstanceMetadata> resources = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setResources(resources);
	}

	
	@Test
	public void testGetArtifacts() throws Exception {
		ResourceAssetDetailedMetadata testSubject;
		List<ArtifactMetadata> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifacts();
	}

	
	@Test
	public void testSetArtifacts() throws Exception {
		ResourceAssetDetailedMetadata testSubject;
		List<ArtifactMetadata> artifactMetaList = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifacts(artifactMetaList);
	}

	
	@Test
	public void testGetDescription() throws Exception {
		ResourceAssetDetailedMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testSetDescription() throws Exception {
		ResourceAssetDetailedMetadata testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}
}