package org.openecomp.sdc.be.components.validation;

import org.junit.Test;

public class ApiResourceEnumTest {

	private ApiResourceEnum createTestSubject() {
		return ApiResourceEnum.ENVIRONMENT_ID;
	}

	@Test
	public void testGetValue() throws Exception {
		ApiResourceEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
	}
}