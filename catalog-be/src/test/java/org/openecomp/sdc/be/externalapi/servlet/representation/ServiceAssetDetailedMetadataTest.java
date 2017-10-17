package org.openecomp.sdc.be.externalapi.servlet.representation;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;


public class ServiceAssetDetailedMetadataTest {

	private ServiceAssetDetailedMetadata createTestSubject() {
		return new ServiceAssetDetailedMetadata();
	}

	
	@Test
	public void testGetLastUpdaterFullName() throws Exception {
		ServiceAssetDetailedMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLastUpdaterFullName();
	}

	
	@Test
	public void testSetLastUpdaterFullName() throws Exception {
		ServiceAssetDetailedMetadata testSubject;
		String lastUpdaterFullName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLastUpdaterFullName(lastUpdaterFullName);
	}

	
	@Test
	public void testGetResources() throws Exception {
		ServiceAssetDetailedMetadata testSubject;
		List<ResourceInstanceMetadata> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResources();
	}

	
	@Test
	public void testSetResources() throws Exception {
		ServiceAssetDetailedMetadata testSubject;
		List<ResourceInstanceMetadata> resources = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setResources(resources);
	}

	
	@Test
	public void testGetArtifacts() throws Exception {
		ServiceAssetDetailedMetadata testSubject;
		List<ArtifactMetadata> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifacts();
	}

	
	@Test
	public void testSetArtifacts() throws Exception {
		ServiceAssetDetailedMetadata testSubject;
		List<ArtifactMetadata> artifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifacts(artifacts);
	}
}