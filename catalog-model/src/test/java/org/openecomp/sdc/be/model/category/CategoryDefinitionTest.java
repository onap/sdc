package org.openecomp.sdc.be.model.category;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;


public class CategoryDefinitionTest {

	private CategoryDefinition createTestSubject() {
		return new CategoryDefinition();
	}

	
	@Test
	public void testGetSubcategories() throws Exception {
		CategoryDefinition testSubject;
		List<SubCategoryDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSubcategories();
	}

	
	@Test
	public void testSetSubcategories() throws Exception {
		CategoryDefinition testSubject;
		List<SubCategoryDefinition> subcategories = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setSubcategories(subcategories);
	}

	
	@Test
	public void testAddSubCategory() throws Exception {
		CategoryDefinition testSubject;
		SubCategoryDefinition subcategory = null;

		// default test
		testSubject = createTestSubject();
		testSubject.addSubCategory(subcategory);
	}

	
	@Test
	public void testToString() throws Exception {
		CategoryDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}