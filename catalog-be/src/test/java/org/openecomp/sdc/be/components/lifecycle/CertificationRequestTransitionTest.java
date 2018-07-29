package org.openecomp.sdc.be.components.lifecycle;

import org.junit.Test;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.impl.AuditingDao;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.titan.TitanGraphClient;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaElementLifecycleOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;
import mockit.Deencapsulation;

public class CertificationRequestTransitionTest extends LifecycleTestBase {

	
	
	private CertificationRequestTransition createTestSubject() {
		return new CertificationRequestTransition(
				new ComponentsUtils(new AuditingManager(new AuditingDao(), new AuditCassandraDao())),
				new ToscaElementLifecycleOperation(), new ServiceBusinessLogic(), new ToscaOperationFacade(), new TitanDao(new TitanGraphClient()));
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