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

package org.openecomp.sdc.be.auditing.impl.category;

import org.junit.Test;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData.Builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AuditGetCategoryHierarchyEventFactoryTest {
	private Builder newBuilder = CommonAuditData.newBuilder();
	private CommonAuditData build = newBuilder.build();

	@Test
	public void testGetLogMessage() throws Exception {
		AuditGetCategoryHierarchyEventFactory testSubject = new AuditGetCategoryHierarchyEventFactory( build,
				"user", "", "");
		assertEquals("ACTION = \"GetCategoryHierarchy\" MODIFIER = \"user\" DETAILS = \"\" STATUS = \"\" DESC = \"\"", testSubject.getLogMessage());
	}

	@Test
	public void testGetDbEvent() throws Exception {
		AuditGetCategoryHierarchyEventFactory testSubject = new AuditGetCategoryHierarchyEventFactory( build, new User(), "");
		assertNotNull(testSubject.getDbEvent());
	}
}
