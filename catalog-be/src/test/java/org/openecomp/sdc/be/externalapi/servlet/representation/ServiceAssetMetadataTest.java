package org.openecomp.sdc.be.externalapi.servlet.representation;

import javax.annotation.Generated;

import org.junit.Test;


public class ServiceAssetMetadataTest {

	private ServiceAssetMetadata createTestSubject() {
		return new ServiceAssetMetadata();
	}

	
	@Test
	public void testGetCategory() throws Exception {
		ServiceAssetMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCategory();
	}

	
	@Test
	public void testSetCategory() throws Exception {
		ServiceAssetMetadata testSubject;
		String category = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCategory(category);
	}

	
	@Test
	public void testGetLifecycleState() throws Exception {
		ServiceAssetMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLifecycleState();
	}

	
	@Test
	public void testSetLifecycleState() throws Exception {
		ServiceAssetMetadata testSubject;
		String lifecycleState = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLifecycleState(lifecycleState);
	}

	
	@Test
	public void testGetLastUpdaterUserId() throws Exception {
		ServiceAssetMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLastUpdaterUserId();
	}

	
	@Test
	public void testSetLastUpdaterUserId() throws Exception {
		ServiceAssetMetadata testSubject;
		String lastUpdaterUserId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLastUpdaterUserId(lastUpdaterUserId);
	}

	
	@Test
	public void testGetDistributionStatus() throws Exception {
		ServiceAssetMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionStatus();
	}

	
	@Test
	public void testSetDistributionStatus() throws Exception {
		ServiceAssetMetadata testSubject;
		String distributionStatus = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDistributionStatus(distributionStatus);
	}
}