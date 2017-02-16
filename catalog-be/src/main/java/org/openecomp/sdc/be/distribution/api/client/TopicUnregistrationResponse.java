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

package org.openecomp.sdc.be.distribution.api.client;

public class TopicUnregistrationResponse {
	String distrNotificationTopicName;
	String distrStatusTopicName;
	CambriaOperationStatus notificationUnregisterResult;
	CambriaOperationStatus statusUnregisterResult;

	public TopicUnregistrationResponse(String distrNotificationTopicName, String distrStatusTopicName, CambriaOperationStatus notificationUnregisterResult, CambriaOperationStatus statusUnregisterResult) {
		super();
		this.distrNotificationTopicName = distrNotificationTopicName;
		this.distrStatusTopicName = distrStatusTopicName;
		this.notificationUnregisterResult = notificationUnregisterResult;
		this.statusUnregisterResult = statusUnregisterResult;
	}

	public String getDistrNotificationTopicName() {
		return distrNotificationTopicName;
	}

	public String getDistrStatusTopicName() {
		return distrStatusTopicName;
	}

	public CambriaOperationStatus getNotificationUnregisterResult() {
		return notificationUnregisterResult;
	}

	public CambriaOperationStatus getStatusUnregisterResult() {
		return statusUnregisterResult;
	}
}
