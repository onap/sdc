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

package org.openecomp.sdc;

import java.util.EnumMap;

import org.openecomp.sdc.be.auditing.api.IAuditingManager;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditingMockManager implements IAuditingManager {

	private static Logger log = LoggerFactory.getLogger(AuditingMockManager.class.getName());

	public AuditingMockManager(String string) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void auditEvent(EnumMap<AuditingFieldsKeysEnum, Object> auditingFields) {
		AuditingActionEnum actionEnum = AuditingActionEnum.getActionByName((String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_ACTION));
		log.debug("call was made to auditEvent with event type {}", actionEnum.getName());
	}
}
