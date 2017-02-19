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

package org.openecomp.sdc.ci.tests.utilities;

import org.codehaus.jettison.json.JSONObject;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedResourceAuditJavaObject;
import org.openecomp.sdc.ci.tests.utils.general.Convertor;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;

public class AuditCDUtils {

	public static void validateResourceSuccessAudit(ResourceReqDetails resource, User user, String action)
			throws Exception {
		JSONObject auditBody = AuditValidationUtils.filterAuditByUuid(action, resource.getUUID());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resource, resource.getVersion(), user);
		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setStatus("201");
		expectedResourceAuditJavaObject.setDesc("OK");
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, auditBody.toString(), false);
	}

	public static void validateServiceSuccessAudit(ServiceReqDetails service, User user, String action)
			throws Exception {
		validateServiceSuccessAudit(service, user, action, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
	}

	public static void validateServiceSuccessAudit(ServiceReqDetails service, User user, String action,
			LifecycleStateEnum lifecycleStatus) throws Exception {
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = AuditValidationUtils
				.constructFieldsForAuditValidation(service, service.getVersion(), user);
		String body = AuditValidationUtils.filterAuditByUuid(action, service.getUUID()).toString();
		expectedResourceAuditJavaObject.setAction(action);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState(lifecycleStatus.toString());
		expectedResourceAuditJavaObject.setStatus("201");
		expectedResourceAuditJavaObject.setDesc("OK");
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, action, body, false);
	}

}
