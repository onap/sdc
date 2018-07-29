package org.openecomp.sdc.be.resources.data;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;


public class ResourceCategoryDataTest {

	private ResourceCategoryData createTestSubject() {
		return new ResourceCategoryData();
	}

	@Test
	public void testCtor() throws Exception {
		new ResourceCategoryData(new HashMap<>());
		new ResourceCategoryData("mock", "mock");
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