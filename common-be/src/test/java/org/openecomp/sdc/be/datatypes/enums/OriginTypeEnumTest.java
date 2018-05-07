package org.openecomp.sdc.be.datatypes.enums;

import org.junit.Test;

public class OriginTypeEnumTest {

	private OriginTypeEnum createTestSubject() {
		return OriginTypeEnum.CP;
	}

	@Test
	public void testGetValue() throws Exception {
		OriginTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
	}

	@Test
	public void testGetDisplayValue() throws Exception {
		OriginTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDisplayValue();
	}

	@Test
	public void testGetInstanceType() throws Exception {
		OriginTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInstanceType();
	}

	@Test
	public void testGetComponentType() throws Exception {
		OriginTypeEnum testSubject;
		ComponentTypeEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentType();
	}

	@Test
	public void testFindByValue() throws Exception {
		String value = "";
		OriginTypeEnum result;

		// default test
		result = OriginTypeEnum.findByValue(value);
		result = OriginTypeEnum.findByValue(OriginTypeEnum.CP.getValue());
	}
}