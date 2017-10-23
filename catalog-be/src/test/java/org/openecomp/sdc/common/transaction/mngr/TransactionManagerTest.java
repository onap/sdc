package org.openecomp.sdc.common.transaction.mngr;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.common.transaction.api.ITransactionSdnc;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.ActionTypeEnum;

public class TransactionManagerTest {

	private TransactionManager createTestSubject() {
		return new TransactionManager();
	}

	
	@Test
	public void testGetTransaction() throws Exception {
		TransactionManager testSubject;
		String userId = "";
		ActionTypeEnum actionType = null;
		ITransactionSdnc result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGenerateTransactionID() throws Exception {
		TransactionManager testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testResetTransactionId() throws Exception {
		TransactionManager testSubject;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testInit() throws Exception {
		TransactionManager testSubject;

		// default test
		testSubject = createTestSubject();
	}
}