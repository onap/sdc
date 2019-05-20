/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.components.lifecycle;

import org.junit.Test;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.impl.AuditingDao;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphClient;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaElementLifecycleOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;
import mockit.Deencapsulation;
import org.openecomp.sdc.test.utils.TestConfigurationProvider;

public class CertificationRequestTransitionTest extends LifecycleTestBase {

	
	
	private CertificationRequestTransition createTestSubject() {
		return new CertificationRequestTransition(
				new ComponentsUtils(new AuditingManager(new AuditingDao(), new AuditCassandraDao(), new TestConfigurationProvider())),
				new ToscaElementLifecycleOperation(), new ServiceBusinessLogic(), new ToscaOperationFacade(), new JanusGraphDao(new JanusGraphClient()));
	}

	@Test
	public void testGetName() throws Exception {
		CertificationRequestTransition testSubject;
		LifeCycleTransitionEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testGetAuditingAction() throws Exception {
		CertificationRequestTransition testSubject;
		AuditingActionEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAuditingAction();
	}

	@Test
	public void testValidateAllResourceInstanceCertified() throws Exception {
		CertificationRequestTransition testSubject;
		Component component = new Resource();
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "validateAllResourceInstanceCertified", component);
	}

	@Test
	public void testValidateConfiguredAtomicReqCapSatisfied() throws Exception {
		CertificationRequestTransition testSubject;
		Component component = new Resource();
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "validateConfiguredAtomicReqCapSatisfied", component);
	}
}