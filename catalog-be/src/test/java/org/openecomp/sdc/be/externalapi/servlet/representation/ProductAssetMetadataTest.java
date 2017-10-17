package org.openecomp.sdc.be.externalapi.servlet.representation;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;


public class ProductAssetMetadataTest {

	private ProductAssetMetadata createTestSubject() {
		return new ProductAssetMetadata();
	}

	
	@Test
	public void testGetLifecycleState() throws Exception {
		ProductAssetMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLifecycleState();
	}

	
	@Test
	public void testSetLifecycleState() throws Exception {
		ProductAssetMetadata testSubject;
		String lifecycleState = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLifecycleState(lifecycleState);
	}

	
	@Test
	public void testGetLastUpdaterUserId() throws Exception {
		ProductAssetMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLastUpdaterUserId();
	}

	
	@Test
	public void testSetLastUpdaterUserId() throws Exception {
		ProductAssetMetadata testSubject;
		String lastUpdaterUserId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLastUpdaterUserId(lastUpdaterUserId);
	}

	
	@Test
	public void testIsActive() throws Exception {
		ProductAssetMetadata testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isActive();
	}

	
	@Test
	public void testSetActive() throws Exception {
		ProductAssetMetadata testSubject;
		boolean isActive = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setActive(isActive);
	}

	
	@Test
	public void testGetContacts() throws Exception {
		ProductAssetMetadata testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getContacts();
	}

	
	@Test
	public void testSetContacts() throws Exception {
		ProductAssetMetadata testSubject;
		List<String> contacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setContacts(contacts);
	}

	
	@Test
	public void testGetProductGroupings() throws Exception {
		ProductAssetMetadata testSubject;
		List<ProductCategoryGroupMetadata> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProductGroupings();
	}

	
	@Test
	public void testSetProductGroupings() throws Exception {
		ProductAssetMetadata testSubject;
		List<ProductCategoryGroupMetadata> productGroupings = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProductGroupings(productGroupings);
	}
}