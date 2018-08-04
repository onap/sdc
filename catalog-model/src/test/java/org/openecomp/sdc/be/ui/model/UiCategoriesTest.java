package org.openecomp.sdc.be.ui.model;

import java.util.List;

import org.junit.Test;
import org.openecomp.sdc.be.model.category.CategoryDefinition;


public class UiCategoriesTest {

	private UiCategories createTestSubject() {
		return new UiCategories();
	}

	
	@Test
	public void testGetResourceCategories() throws Exception {
		UiCategories testSubject;
		List<CategoryDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceCategories();
	}

	
	@Test
	public void testSetResourceCategories() throws Exception {
		UiCategories testSubject;
		List<CategoryDefinition> resourceCategories = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceCategories(resourceCategories);
	}

	
	@Test
	public void testGetServiceCategories() throws Exception {
		UiCategories testSubject;
		List<CategoryDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceCategories();
	}

	
	@Test
	public void testSetServiceCategories() throws Exception {
		UiCategories testSubject;
		List<CategoryDefinition> serviceCategories = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceCategories(serviceCategories);
	}

	
	@Test
	public void testGetProductCategories() throws Exception {
		UiCategories testSubject;
		List<CategoryDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProductCategories();
	}

	
	@Test
	public void testSetProductCategories() throws Exception {
		UiCategories testSubject;
		List<CategoryDefinition> productCategories = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProductCategories(productCategories);
	}
}