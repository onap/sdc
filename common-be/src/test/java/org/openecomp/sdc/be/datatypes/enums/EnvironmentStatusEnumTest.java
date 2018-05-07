package org.openecomp.sdc.be.datatypes.enums;

import org.junit.Test;

public class EnvironmentStatusEnumTest {

	private EnvironmentStatusEnum createTestSubject() {
		return EnvironmentStatusEnum.COMPLETED;
	}

	@Test
	public void testGetName() throws Exception {
		EnvironmentStatusEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testGetByName() throws Exception {
		String name = "";
		EnvironmentStatusEnum result;

		// default test
		EnvironmentStatusEnum[] values = EnvironmentStatusEnum.values();
		for (EnvironmentStatusEnum environmentStatusEnum : values) {
			result = EnvironmentStatusEnum.getByName(environmentStatusEnum.getName());
		}
	}
}