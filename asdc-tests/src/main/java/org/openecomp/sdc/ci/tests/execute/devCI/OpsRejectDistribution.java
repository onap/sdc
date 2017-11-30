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

package org.openecomp.sdc.ci.tests.execute.devCI;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.testng.annotations.Test;

public class OpsRejectDistribution extends ComponentBaseTest {

	@Rule
	public static TestName name = new TestName();

	public OpsRejectDistribution() {
		super(name, OpsRejectDistribution.class.getName());
	}
	
	@Test
	public void testOpsRejectDistribution() throws Exception {
		User designer = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		
		ServiceReqDetails service = ElementFactory.getDefaultService();
		RestResponse createdService = ServiceRestUtils.createService(service, designer);
		BaseRestUtils.checkCreateResponse(createdService);
		Service serviceFirstImport = ResponseParser.parseToObjectUsingMapper(createdService.getResponse(), Service.class);
		Component serviceObject = AtomicOperationUtils.changeComponentState(serviceFirstImport, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		AtomicOperationUtils.approveAndRejectServiceForDistribution(serviceObject);
	}
}
