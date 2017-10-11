package org.openecomp.sdc.be.datatypes.category;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Assert;
import org.junit.Test;


public class SubCategoryDataDefinitionTest {

	private SubCategoryDataDefinition createTestSubject() {
		return new SubCategoryDataDefinition();
	}

	
	@Test
	public void testGetName() throws Exception {
		SubCategoryDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testSetName() throws Exception {
		SubCategoryDataDefinition testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	
	@Test
	public void testGetNormalizedName() throws Exception {
		SubCategoryDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNormalizedName();
	}

	
	@Test
	public void testSetNormalizedName() throws Exception {
		SubCategoryDataDefinition testSubject;
		String normalizedName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNormalizedName(normalizedName);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		SubCategoryDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		SubCategoryDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetIcons() throws Exception {
		SubCategoryDataDefinition testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIcons();
	}

	
	@Test
	public void testSetIcons() throws Exception {
		SubCategoryDataDefinition testSubject;
		List<String> icons = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setIcons(icons);
	}

	
	@Test
	public void testHashCode() throws Exception {
		SubCategoryDataDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	
	@Test
	public void testEquals() throws Exception {
		SubCategoryDataDefinition testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		obj = null;
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
	}

	
	@Test
	public void testToString() throws Exception {
		SubCategoryDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}