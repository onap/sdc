/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

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
