package org.openecomp.sdc.be.datatypes.enums;

import org.junit.Test;

public class ComponentTypeEnumTest {

	private ComponentTypeEnum createTestSubject() {
		return ComponentTypeEnum.PRODUCT;
	}

	@Test
	public void testGetValue() throws Exception {
		ComponentTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
	}

	@Test
	public void testGetNodeType() throws Exception {
		ComponentTypeEnum testSubject;
		NodeTypeEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNodeType();
	}

	@Test
	public void testFindByValue() throws Exception {
		String value = "";
		ComponentTypeEnum result;

		// default test
		result = ComponentTypeEnum.findByValue(value);
	}

	@Test
	public void testFindByParamName() throws Exception {
		String paramName = "";
		ComponentTypeEnum result;

		// default test
		result = ComponentTypeEnum.findByParamName(paramName);
	}

	@Test
	public void testFindParamByType() throws Exception {
		ComponentTypeEnum type = null;
		String result;

		// default test
		result = ComponentTypeEnum.findParamByType(type);
	}
}