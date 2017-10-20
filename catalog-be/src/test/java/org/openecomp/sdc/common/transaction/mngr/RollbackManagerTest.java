package org.openecomp.sdc.common.transaction.mngr;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.common.transaction.api.RollbackHandler;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.DBActionCodeEnum;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.DBTypeEnum;
import org.openecomp.sdc.common.util.MethodActivationStatusEnum;

import fj.data.Either;

public class RollbackManagerTest {

	private RollbackManager createTestSubject() {
		return new RollbackManager(null, "", "", null);
	}

	
	@Test
	public void testTransactionRollback() throws Exception {
		RollbackManager testSubject;
		DBActionCodeEnum result;

		// default test
	}

	
	@Test
	public void testAddRollbackHandler() throws Exception {
		RollbackManager testSubject;
		RollbackHandler rollbackHandler = null;
		Either<RollbackHandler, MethodActivationStatusEnum> result;

		// default test
	}

	
	@Test
	public void testGetRollbackHandler() throws Exception {
		RollbackManager testSubject;
		DBTypeEnum dbType = null;
		Either<RollbackHandler, MethodActivationStatusEnum> result;

		// default test
	}
}