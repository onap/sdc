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
import org.openecomp.sdc.be.auditing.impl.AuditGetUebClusterEventFactory;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData.Builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuditGetUebClusterEventFactoryTest {

	private AuditGetUebClusterEventFactory createTestSubject() {
		Builder newBuilder = CommonAuditData.newBuilder();
		CommonAuditData build = newBuilder.build();
		return new AuditGetUebClusterEventFactory(build, "", "");
	}

	@Test
	public void testGetLogMessage() throws Exception {
		AuditGetUebClusterEventFactory testSubject = createTestSubject();
		String expected1 = "ACTION = \"GetUebCluster\" CONSUMER_ID = \"\" STATUS_TIME =";
		String expected2 = "STATUS = \"\" STATUS_DESC = \"\"";
		String actual = testSubject.getLogMessage();
		assertTrue(actual.contains(expected1));
		assertTrue(actual.contains(expected2));
	}

	@Test
	public void testGetDbEvent() throws Exception {
		AuditGetUebClusterEventFactory testSubject = createTestSubject();
		AuditingGenericEvent result = testSubject.getDbEvent();
		assertEquals("GetUebCluster", result.getAction());
	}
}
