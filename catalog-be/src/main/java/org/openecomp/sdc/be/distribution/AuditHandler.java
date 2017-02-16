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

package org.openecomp.sdc.be.distribution;

import org.openecomp.sdc.be.components.distribution.engine.CambriaErrorResponse;
import org.openecomp.sdc.be.components.distribution.engine.SubscriberTypeEnum;
import org.openecomp.sdc.be.distribution.api.client.RegistrationRequest;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;

public class AuditHandler {
	ComponentsUtils componentsUtils;
	String instanceID;
	private RegistrationRequest registrationRequest;

	public AuditHandler(ComponentsUtils componentsUtils, String instanceID, RegistrationRequest registrationRequest) {
		super();
		this.componentsUtils = componentsUtils;
		this.instanceID = instanceID;
		this.registrationRequest = registrationRequest;
	}

	public void auditRegisterACL(CambriaErrorResponse registerResponse, SubscriberTypeEnum subscriberRole) {

		String topicName = (subscriberRole == SubscriberTypeEnum.CONSUMER) ? DistributionBusinessLogic.getNotificationTopicName(registrationRequest.getDistrEnvName())
				: DistributionBusinessLogic.getStatusTopicName(registrationRequest.getDistrEnvName());
		componentsUtils.auditTopicACLKeys(AuditingActionEnum.ADD_KEY_TO_TOPIC_ACL, registrationRequest.getDistrEnvName(), topicName, subscriberRole.name(), registrationRequest.getApiPublicKey(), String.valueOf(registerResponse.getHttpCode()));
	}

	public void auditUnRegisterACL(CambriaErrorResponse registerResponse, SubscriberTypeEnum subscriberRole) {
		String topicName = (subscriberRole == SubscriberTypeEnum.CONSUMER) ? DistributionBusinessLogic.getNotificationTopicName(registrationRequest.getDistrEnvName())
				: DistributionBusinessLogic.getStatusTopicName(registrationRequest.getDistrEnvName());
		componentsUtils.auditTopicACLKeys(AuditingActionEnum.REMOVE_KEY_FROM_TOPIC_ACL, registrationRequest.getDistrEnvName(), topicName, subscriberRole.name(), registrationRequest.getApiPublicKey(), String.valueOf(registerResponse.getHttpCode()));

	}

	public void auditRegisterRequest(CambriaErrorResponse registerResponse) {
		componentsUtils.auditRegisterOrUnRegisterEvent(AuditingActionEnum.DISTRIBUTION_REGISTER, instanceID, registrationRequest.getApiPublicKey(), registrationRequest.getDistrEnvName(), String.valueOf(registerResponse.getHttpCode()),
				registerResponse.getOperationStatus().name(), DistributionBusinessLogic.getNotificationTopicName(registrationRequest.getDistrEnvName()), DistributionBusinessLogic.getStatusTopicName(registrationRequest.getDistrEnvName()));

	}

	public void auditUnRegisterRequest(CambriaErrorResponse registerResponse) {
		componentsUtils.auditRegisterOrUnRegisterEvent(AuditingActionEnum.DISTRIBUTION_UN_REGISTER, instanceID, registrationRequest.getApiPublicKey(), registrationRequest.getDistrEnvName(), String.valueOf(registerResponse.getHttpCode()),
				registerResponse.getOperationStatus().name(), DistributionBusinessLogic.getNotificationTopicName(registrationRequest.getDistrEnvName()), DistributionBusinessLogic.getStatusTopicName(registrationRequest.getDistrEnvName()));
	}
}
