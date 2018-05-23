package org.openecomp.sdc.common.transaction.impl;

import org.junit.Test;
import org.openecomp.sdc.be.dao.impl.ESCatalogDAO;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.DBActionCodeEnum;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.ESActionTypeEnum;
import org.openecomp.sdc.exception.IndexingServiceException;

public class ESActionTest {

	@Test
	public void testDoAction() throws Exception {
		ESAction testSubject = new ESAction(new ESCatalogDAO(), new ESArtifactData(), ESActionTypeEnum.ADD_ARTIFACT);;
		DBActionCodeEnum result;

		// default test
		result = testSubject.doAction();
	}
	
	@Test(expected = IndexingServiceException.class)
	public void testDoAction1() throws Exception {
		ESAction testSubject = new ESAction(new ESCatalogDAO(), new ESArtifactData(), ESActionTypeEnum.REMOVE_ARTIFACT);;
		DBActionCodeEnum result;

		// default test
		result = testSubject.doAction();
	}
}