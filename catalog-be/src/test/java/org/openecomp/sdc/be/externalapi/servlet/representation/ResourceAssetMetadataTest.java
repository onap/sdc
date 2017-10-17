package org.openecomp.sdc.be.externalapi.servlet.representation;

import javax.annotation.Generated;

import org.junit.Test;


public class ResourceAssetMetadataTest {

	private ResourceAssetMetadata createTestSubject() {
		return new ResourceAssetMetadata();
	}

	
	@Test
	public void testGetCategory() throws Exception {
		ResourceAssetMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCategory();
	}

	
	@Test
	public void testSetCategory() throws Exception {
		ResourceAssetMetadata testSubject;
		String category = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCategory(category);
	}

	
	@Test
	public void testGetSubCategory() throws Exception {
		ResourceAssetMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSubCategory();
	}

	
	@Test
	public void testSetSubCategory() throws Exception {
		ResourceAssetMetadata testSubject;
		String subCategory = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setSubCategory(subCategory);
	}

	
	@Test
	public void testGetResourceType() throws Exception {
		ResourceAssetMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceType();
	}

	
	@Test
	public void testSetResourceType() throws Exception {
		ResourceAssetMetadata testSubject;
		String resourceType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceType(resourceType);
	}

	
	@Test
	public void testGetLifecycleState() throws Exception {
		ResourceAssetMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLifecycleState();
	}

	
	@Test
	public void testSetLifecycleState() throws Exception {
		ResourceAssetMetadata testSubject;
		String lifecycleState = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLifecycleState(lifecycleState);
	}

	
	@Test
	public void testGetLastUpdaterUserId() throws Exception {
		ResourceAssetMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLastUpdaterUserId();
	}

	
	@Test
	public void testSetLastUpdaterUserId() throws Exception {
		ResourceAssetMetadata testSubject;
		String lastUpdaterUserId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLastUpdaterUserId(lastUpdaterUserId);
	}
}