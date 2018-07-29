package org.openecomp.sdc.be.resources.data.category;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.category.SubCategoryDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import java.util.HashMap;
import java.util.Map;

public class SubCategoryDataTest {

	private SubCategoryData createTestSubject() {
		return new SubCategoryData(NodeTypeEnum.AdditionalInfoParameters);
	}

	@Test
	public void testCtor() throws Exception {
		new SubCategoryData(new HashMap<>());
		new SubCategoryData(NodeTypeEnum.AdditionalInfoParameters, new SubCategoryDataDefinition());
	}
	
	@Test
	public void testGetSubCategoryDataDefinition() throws Exception {
		SubCategoryData testSubject;
		SubCategoryDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSubCategoryDataDefinition();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		SubCategoryData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		SubCategoryData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}
}