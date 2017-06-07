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
 */

package org.openecomp.sdc.ci.tests.execute.lifecycle;

import static org.testng.AssertJUnit.assertTrue;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.testng.annotations.Test;

public class ServiceCertWithoutDeploymentArtifacts extends LCSbaseTest {
	protected ServiceReqDetails serviceDetails;
	protected User sdncDesignerDetails;
	protected User sdncAdminDetails;
	protected Service serviceServ;

	@Rule
	public static TestName testName = new TestName();
	
	public ServiceCertWithoutDeploymentArtifacts() {
		super(testName, ServiceCertWithoutDeploymentArtifacts.class.getName());
	}

	@Test
	/**
	 * checks possibility to certify service without of deployment artifacts
	 * @throws Exception
	 */
	public void testDeploymentArtifactsRestriction() throws Exception {

		sdncDesignerDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		serviceServ = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		assertTrue(serviceServ.getDeploymentArtifacts() == null || serviceServ.getDeploymentArtifacts().isEmpty());
		assertTrue(serviceServ.getComponentInstances() == null || serviceServ.getComponentInstances().isEmpty());
		serviceDetails = new ServiceReqDetails(serviceServ);
		RestResponse changeServiceState = LCSbaseTest.certifyService(serviceDetails, sdncDesignerDetails);
		assertTrue("certify service request returned status:" + changeServiceState.getErrorCode(),
				changeServiceState.getErrorCode() == 200);
	}
}
