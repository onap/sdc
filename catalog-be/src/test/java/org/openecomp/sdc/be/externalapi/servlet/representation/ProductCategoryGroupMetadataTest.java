package org.openecomp.sdc.be.externalapi.servlet.representation;

import javax.annotation.Generated;

import org.junit.Test;


public class ProductCategoryGroupMetadataTest {

	private ProductCategoryGroupMetadata createTestSubject() {
		return new ProductCategoryGroupMetadata("", "", "");
	}

	
	@Test
	public void testGetCategory() throws Exception {
		ProductCategoryGroupMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCategory();
	}

	
	@Test
	public void testSetCategory() throws Exception {
		ProductCategoryGroupMetadata testSubject;
		String category = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCategory(category);
	}

	
	@Test
	public void testGetSubCategory() throws Exception {
		ProductCategoryGroupMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSubCategory();
	}

	
	@Test
	public void testSetSubCategory() throws Exception {
		ProductCategoryGroupMetadata testSubject;
		String subCategory = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setSubCategory(subCategory);
	}

	
	@Test
	public void testGetGroup() throws Exception {
		ProductCategoryGroupMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroup();
	}

	
	@Test
	public void testSetGroup() throws Exception {
		ProductCategoryGroupMetadata testSubject;
		String group = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setGroup(group);
	}
}