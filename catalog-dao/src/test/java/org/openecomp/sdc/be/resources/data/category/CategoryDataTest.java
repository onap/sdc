package org.openecomp.sdc.be.resources.data.category;

import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.category.CategoryDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class CategoryDataTest {

	private CategoryData createTestSubject() {
		return new CategoryData(NodeTypeEnum.AdditionalInfoParameters);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		CategoryData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testGetCategoryDataDefinition() throws Exception {
		CategoryData testSubject;
		CategoryDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCategoryDataDefinition();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		CategoryData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testToString() throws Exception {
		CategoryData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}