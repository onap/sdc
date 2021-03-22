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

package org.openecomp.sdc.be.auditing.impl.distribution;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.DistributionDeployEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData.Builder;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuditDistribDownloadEventFactoryTest {

	@Test
	public void testGetLogMessage() throws Exception {
		Builder newBuilder = CommonAuditData.newBuilder();
		CommonAuditData build = newBuilder.build();
		AuditDistributionDeployEventFactory testSubject = new AuditDistributionDeployEventFactory(build,new ResourceCommonInfo(),"",new User(),"1.0");
		String expected = "ACTION = \"DResult\" RESOURCE_NAME = \"\" RESOURCE_TYPE = \"\" SERVICE_INSTANCE_ID = \"\" CURR_VERSION = \"1.0\" MODIFIER = \"\" DID = \"\" STATUS = \"\" DESC = \"\"";

		assertEquals(expected, testSubject.getLogMessage());
	}

	@Test
	public void testGetDbEvent() throws Exception {
		Builder newBuilder = CommonAuditData.newBuilder();
		CommonAuditData build = newBuilder.build();
		AuditDistributionDeployEventFactory testSubject = new AuditDistributionDeployEventFactory(build,new ResourceCommonInfo(),"","user","1.0", "");
		DistributionDeployEvent result = (DistributionDeployEvent)testSubject.getDbEvent();

		assertEquals("DResult", result.getAction());
		assertEquals("1.0", result.getCurrVersion());
		assertEquals("", result.getDid());
		assertEquals("user", result.getModifier());
	}
}
