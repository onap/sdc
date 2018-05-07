package org.openecomp.sdc.be.datatypes.enums;

import org.junit.Test;

public class ResourceTypeEnumTest {

	private ResourceTypeEnum createTestSubject() {
		return ResourceTypeEnum.ABSTRACT;
	}

	@Test
	public void testGetValue() throws Exception {
		ResourceTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
	}

	@Test
	public void testIsAtomicType() throws Exception {
		ResourceTypeEnum testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isAtomicType();
	}

	@Test
	public void testGetType() throws Exception {
		String type = "";
		ResourceTypeEnum result;

		// default test
		result = ResourceTypeEnum.getType(type);
		result = ResourceTypeEnum.getType(ResourceTypeEnum.ABSTRACT.name());
	}

	@Test
	public void testGetTypeByName() throws Exception {
		String type = "";
		ResourceTypeEnum result;

		// default test
		result = ResourceTypeEnum.getType(type);
		result = ResourceTypeEnum.getTypeByName(ResourceTypeEnum.ABSTRACT.name());
	}

	@Test
	public void testGetTypeIgnoreCase() throws Exception {
		String type = "";
		ResourceTypeEnum result;

		// default test
		result = ResourceTypeEnum.getTypeIgnoreCase(type);
		result = ResourceTypeEnum.getTypeIgnoreCase(ResourceTypeEnum.ABSTRACT.name());
	}

	@Test
	public void testContainsName() throws Exception {
		String type = "";
		boolean result;

		// default test
		result = ResourceTypeEnum.containsName(type);
		result = ResourceTypeEnum.containsName(ResourceTypeEnum.ABSTRACT.name());
	}

	@Test
	public void testContainsIgnoreCase() throws Exception {
		String type = "";
		boolean result;

		// default test
		result = ResourceTypeEnum.containsIgnoreCase(type);
		result = ResourceTypeEnum.containsIgnoreCase(ResourceTypeEnum.ABSTRACT.name());
	}
}