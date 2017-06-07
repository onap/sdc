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

package org.openecomp.sdc.be.components.distribution.engine;

import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublishNotificationRunnable implements Runnable {

	private String envName;
	private String distributionId;
	private Service service;
	private INotificationData data;
	private DistributionEngineConfiguration deConfiguration;
	private String topicName;
	private CambriaHandler cambriaHandler;
	private ComponentsUtils componentUtils;
	private String userId;
	private String modifierName;
	private String requestId;

	private static Logger logger = LoggerFactory.getLogger(PublishNotificationRunnable.class.getName());

	public PublishNotificationRunnable(String envName, String distributionId, Service service, INotificationData data, DistributionEngineConfiguration deConfiguration, String topicName, String userId, String modifierName,
			CambriaHandler cambriaHandler, ComponentsUtils componentUtils, String requestId) {
		super();
		this.envName = envName;
		this.distributionId = distributionId;
		this.service = service;
		this.data = data;
		this.deConfiguration = deConfiguration;
		this.topicName = topicName;
		this.cambriaHandler = cambriaHandler;
		this.componentUtils = componentUtils;
		this.userId = userId;
		this.modifierName = modifierName;
		this.requestId = requestId;
	}

	public INotificationData getData() {
		return data;
	}

	public void setData(INotificationData data) {
		this.data = data;
	}

	public DistributionEngineConfiguration getDeConfiguration() {
		return deConfiguration;
	}

	public void setDeConfiguration(DistributionEngineConfiguration deConfiguration) {
		this.deConfiguration = deConfiguration;
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getModifierName() {
		return modifierName;
	}

	public void setModifierName(String modifierName) {
		this.modifierName = modifierName;
	}

	@Override
	public void run() {

		long startTime = System.currentTimeMillis();
		ThreadLocalsHolder.setUuid(this.requestId);

		CambriaErrorResponse status = cambriaHandler.sendNotificationAndClose(topicName, deConfiguration.getUebPublicKey(), deConfiguration.getUebSecretKey(), deConfiguration.getUebServers(), data,
				deConfiguration.getDistributionNotificationTopic().getMaxWaitingAfterSendingSeconds());

		logger.info("After publishing service {} of version {}. Status is {}", service.getName(), service.getVersion(), status.getHttpCode());
		auditDistributionNotification(topicName, status, service, distributionId, envName, userId, modifierName);

		long endTime = System.currentTimeMillis();
		logger.debug("After building and publishing artifacts object. Total took {} milliseconds", (endTime - startTime));

	}

	private void auditDistributionNotification(String topicName, CambriaErrorResponse status, Service service, String distributionId, String envName, String userId, String modifierName) {
		if (this.componentUtils != null) {
			Integer httpCode = status.getHttpCode();
			String httpCodeStr = String.valueOf(httpCode);

			String desc = getDescriptionFromErrorResponse(status);

			this.componentUtils.auditDistributionNotification(AuditingActionEnum.DISTRIBUTION_NOTIFY, service.getUUID(), service.getName(), "Service", service.getVersion(), userId, modifierName, envName, service.getLifecycleState().name(), topicName,
					distributionId, desc, httpCodeStr);
		}
	}

	private String getDescriptionFromErrorResponse(CambriaErrorResponse status) {

		CambriaOperationStatus operationStatus = status.getOperationStatus();

		switch (operationStatus) {
		case OK:
			return "OK";
		case AUTHENTICATION_ERROR:
			return "Error: Authentication problem towards U-EB server";
		case INTERNAL_SERVER_ERROR:
			return "Error: Internal U-EB server error";
		case UNKNOWN_HOST_ERROR:
			return "Error: Cannot reach U-EB server host";
		case CONNNECTION_ERROR:
			return "Error: Cannot connect to U-EB server";
		case OBJECT_NOT_FOUND:
			return "Error: object not found in U-EB server";
		default:
			return "Error: Internal Cambria server problem";

		}

	}
}
