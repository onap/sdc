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
import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.auditing.impl.resourceadmin.AuditResourceEventFactoryManager;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;


public class AuditResourceEventFactoryManangerTest {

	@Test
	public void testCreateResourceEventFactory() throws Exception {
		AuditingActionEnum action = null;
		CommonAuditData commonFields = null;
		ResourceVersionInfo prevParams = null;
		ResourceVersionInfo currParams = null;
		String resourceType = "";
		User modifier = null;
		String artifactData = "";
		String comment = "";
		String did = "";
		String toscaNodeType = "";
		AuditEventFactory result;
		
		for (AuditingActionEnum iterable_element : AuditingActionEnum.values()) {
			try {
				result = AuditResourceEventFactoryManager.createResourceEventFactory(iterable_element, commonFields,new ResourceCommonInfo(), prevParams,
						currParams, resourceType,modifier, artifactData, comment,did,
						toscaNodeType);
			} catch (Exception UnsupportedOperationException) {
				continue;
			}
		}
		// default test
	}
}
