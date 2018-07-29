package org.openecomp.sdc.be.dao.utils;

import fj.data.Either;
import org.junit.Test;
import org.openecomp.sdc.common.util.MethodActivationStatusEnum;

public class UserStatusEnumTest {

	@Test
	public void testFindByName() throws Exception {
		String name = "mock";
		Either<UserStatusEnum, MethodActivationStatusEnum> result;

		// default test
		result = UserStatusEnum.findByName(name);
		result = UserStatusEnum.findByName(UserStatusEnum.ACTIVE.name());
	}
}