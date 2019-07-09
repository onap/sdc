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

package org.openecomp.sdc.be.auditing.impl;

import org.junit.Test;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;

public class AuditEcompOpEnvEventFactoryTest {

	private AuditEcompOpEnvEventFactory createTestSubject() {
		return new AuditEcompOpEnvEventFactory(AuditingActionEnum.ACTIVATE_SERVICE_BY_API, "", "", "", "", "");
	}

	@Test
	public void testGetLogMessage() throws Exception {
		AuditEcompOpEnvEventFactory testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLogMessage();
	}

	@Test
	public void testGetDbEvent() throws Exception {
		AuditEcompOpEnvEventFactory testSubject;
		AuditingGenericEvent result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDbEvent();
	}
}
