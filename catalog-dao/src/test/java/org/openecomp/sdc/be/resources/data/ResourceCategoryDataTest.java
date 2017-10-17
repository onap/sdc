package org.openecomp.sdc.be.resources.data;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;


public class ResourceCategoryDataTest {

	private ResourceCategoryData createTestSubject() {
		return new ResourceCategoryData();
	}

	
	@Test
	public void testGetCategoryName() throws Exception {
		ResourceCategoryData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCategoryName();
	}

	
	@Test
	public void testSetCategoryName() throws Exception {
		ResourceCategoryData testSubject;
		String categoryName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCategoryName(categoryName);
	}


	
	@Test
	public void testToString() throws Exception {
		ResourceCategoryData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		ResourceCategoryData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}
}