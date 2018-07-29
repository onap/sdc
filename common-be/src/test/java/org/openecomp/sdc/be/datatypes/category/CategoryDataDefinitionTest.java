package org.openecomp.sdc.be.datatypes.category;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;


public class CategoryDataDefinitionTest {

	private CategoryDataDefinition createTestSubject() {
		return new CategoryDataDefinition();
	}
	
	@Test
	public void testCopyConstructor() throws Exception {
		CategoryDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		CategoryDataDefinition categoryDataDefinition = new CategoryDataDefinition(testSubject);
	}
	
	@Test
	public void testGetName() throws Exception {
		CategoryDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testSetName() throws Exception {
		CategoryDataDefinition testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	
	@Test
	public void testGetNormalizedName() throws Exception {
		CategoryDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNormalizedName();
	}

	
	@Test
	public void testSetNormalizedName() throws Exception {
		CategoryDataDefinition testSubject;
		String normalizedName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNormalizedName(normalizedName);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		CategoryDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		CategoryDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetIcons() throws Exception {
		CategoryDataDefinition testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIcons();
	}

	
	@Test
	public void testSetIcons() throws Exception {
		CategoryDataDefinition testSubject;
		List<String> icons = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setIcons(icons);
	}

	
	@Test
	public void testHashCode() throws Exception {
		CategoryDataDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	
	@Test
	public void testEquals() throws Exception {
		CategoryDataDefinition testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		obj = null;
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		result = testSubject.equals(new CategoryDataDefinition(testSubject));
		Assert.assertEquals(true, result);
	}

	
	@Test
	public void testToString() throws Exception {
		CategoryDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}