package org.openecomp.sdc.be.datatypes.enums;

import org.junit.Test;

public class GroupTypeEnumTest {

	private GroupTypeEnum createTestSubject() {
		return GroupTypeEnum.HEAT_STACK;
	}

	@Test
	public void testGetGroupTypeName() throws Exception {
		GroupTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroupTypeName();
	}
}