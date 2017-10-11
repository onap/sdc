package org.openecomp.sdc.be.model.category;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;


public class SubCategoryDefinitionTest {

	private SubCategoryDefinition createTestSubject() {
		return new SubCategoryDefinition();
	}

	
	@Test
	public void testGetGroupings() throws Exception {
		SubCategoryDefinition testSubject;
		List<GroupingDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroupings();
	}

	
	@Test
	public void testSetGroupings() throws Exception {
		SubCategoryDefinition testSubject;
		List<GroupingDefinition> groupingDefinitions = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setGroupings(groupingDefinitions);
	}

	
	@Test
	public void testAddGrouping() throws Exception {
		SubCategoryDefinition testSubject;
		GroupingDefinition groupingDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.addGrouping(groupingDefinition);
	}

	
	@Test
	public void testToString() throws Exception {
		SubCategoryDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}