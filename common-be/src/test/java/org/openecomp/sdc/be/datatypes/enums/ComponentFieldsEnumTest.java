package org.openecomp.sdc.be.datatypes.enums;

import org.junit.Test;

public class ComponentFieldsEnumTest {

	private ComponentFieldsEnum createTestSubject() {
		return ComponentFieldsEnum.ADDITIONAL_INFORMATION;
	}

	@Test
	public void testGetValue() throws Exception {
		ComponentFieldsEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
	}

	@Test
	public void testFindByValue() throws Exception {
		String value = "artifacts";
		ComponentFieldsEnum result;

		// default test
		result = ComponentFieldsEnum.findByValue(value);
		result = ComponentFieldsEnum.findByValue("");
	}
}