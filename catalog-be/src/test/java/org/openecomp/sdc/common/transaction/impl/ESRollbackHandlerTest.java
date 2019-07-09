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

import fj.data.Either;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.api.ResourceUploadStatus;
import org.openecomp.sdc.be.dao.impl.ESCatalogDAO;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.DBActionCodeEnum;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.DBTypeEnum;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.ESActionTypeEnum;
import org.openecomp.sdc.common.util.MethodActivationStatusEnum;

public class ESRollbackHandlerTest {

	@InjectMocks
	ESRollbackHandler testSubject;
	@Mock
	ESCatalogDAO esCatalogDao;
	

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	private ESRollbackHandler createTestSubject() {
		return new ESRollbackHandler(null, "", "");
	}

	@Test
	public void testGetDBType() throws Exception {
		DBTypeEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDBType();
	}

	@Test
	public void testIsRollbackForPersistenceData() throws Exception {
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "isRollbackForPersistenceData");
	}

	@Test
	public void testIsRollbackResultValid() throws Exception {
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isRollbackResultValid(DBActionCodeEnum.FAIL_GENERAL);
		result = testSubject.isRollbackResultValid(DBActionCodeEnum.SUCCESS);
	}

	@Test
	public void testBuildEsRollbackAction() throws Exception {
		ESArtifactData artifactData = new ESArtifactData();
		artifactData.setId("mock");
		Either<ESAction, MethodActivationStatusEnum> result;

		Either<ESArtifactData, ResourceUploadStatus> value = Either.left(new ESArtifactData());
		Mockito.when(esCatalogDao.getArtifact(Mockito.anyString())).thenReturn(value);

		// default test
		for (ESActionTypeEnum iterable_element : ESActionTypeEnum.values()) {
			testSubject = createTestSubject();
			result = testSubject.buildEsRollbackAction(esCatalogDao, artifactData, iterable_element);
		}
		result = testSubject.buildEsRollbackAction(esCatalogDao, null, ESActionTypeEnum.ADD_ARTIFACT);
	}
	
	@Test
	public void testBuildEsRollbackAction2() throws Exception {
		ESArtifactData artifactData = new ESArtifactData();
		artifactData.setId("mock");
		Either<ESAction, MethodActivationStatusEnum> result;

		Either<ESArtifactData, ResourceUploadStatus> value = Either.right(ResourceUploadStatus.NOT_EXIST);
		Mockito.when(esCatalogDao.getArtifact(Mockito.anyString())).thenReturn(value);

		// default test
		for (ESActionTypeEnum iterable_element : ESActionTypeEnum.values()) {
			testSubject = createTestSubject();
			result = testSubject.buildEsRollbackAction(esCatalogDao, artifactData, iterable_element);
		}
		result = testSubject.buildEsRollbackAction(esCatalogDao, null, ESActionTypeEnum.ADD_ARTIFACT);
	}
}
