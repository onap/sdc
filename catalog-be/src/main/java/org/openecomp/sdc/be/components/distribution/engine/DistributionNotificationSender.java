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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import javax.annotation.PreDestroy;

import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionNotificationTopicConfig;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("distributionNotificationSender")
public class DistributionNotificationSender {

	protected static final String DISTRIBUTION_NOTIFICATION_SENDING = "distributionNotificationSending";

	private static Logger logger = LoggerFactory.getLogger(DistributionNotificationSender.class.getName());

	// final String BASE_ARTIFACT_URL = "/sdc/v1/catalog/services/%s/%s/";
	// final String RESOURCE_ARTIFACT_URL = BASE_ARTIFACT_URL
	// + "resources/%s/%s/artifacts/%s";
	// final String SERVICE_ARTIFACT_URL = BASE_ARTIFACT_URL + "artifacts/%s";

	@javax.annotation.Resource
	InterfaceLifecycleOperation interfaceLifecycleOperation;

	@javax.annotation.Resource
	protected ComponentsUtils componentUtils;

	ExecutorService executorService = null;

	CambriaHandler cambriaHandler = new CambriaHandler();

	NotificationExecutorService notificationExecutorService = new NotificationExecutorService();

	public DistributionNotificationSender() {
		super();

		DistributionNotificationTopicConfig distributionNotificationTopic = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration().getDistributionNotificationTopic();

		executorService = notificationExecutorService.createExcecutorService(distributionNotificationTopic);
	}

	@PreDestroy
	public void shutdown() {
		logger.debug("Going to close notificationExecutorService");
		if (executorService != null) {

			long maxWaitingTime = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration().getDistributionNotificationTopic().getMaxWaitingAfterSendingSeconds();

			notificationExecutorService.shutdownAndAwaitTermination(executorService, maxWaitingTime + 1);
		}
	}

	public StorageOperationStatus sendNotification(String topicName, String distributionId, DistributionEngineConfiguration deConfiguration, String envName, INotificationData notificationData, Service service, String userId, String modifierName) {

		Runnable task = new PublishNotificationRunnable(envName, distributionId, service, notificationData, deConfiguration, topicName, userId, modifierName, cambriaHandler, componentUtils, ThreadLocalsHolder.getUuid());
		try {
			executorService.submit(task);
		} catch (RejectedExecutionException e) {
			logger.warn("Failed to submit task. Number of threads exceeeds", e);
			return StorageOperationStatus.OVERLOAD;
		}

		return StorageOperationStatus.OK;
	}

	/**
	 * Audit the publishing notification in case of internal server error.
	 * 
	 * @param topicName
	 * @param status
	 * @param distributionId
	 * @param envName
	 */
	private void auditDistributionNotificationInternalServerError(String topicName, StorageOperationStatus status, String distributionId, String envName) {

		if (this.componentUtils != null) {
			this.componentUtils.auditDistributionNotification(AuditingActionEnum.DISTRIBUTION_NOTIFY, "", " ", "Service", " ", " ", " ", envName, " ", topicName, distributionId, "Error: Internal Server Error. " + status, " ");
		}
	}

	protected CambriaErrorResponse publishNotification(INotificationData data, DistributionEngineConfiguration deConfiguration, String topicName) {

		CambriaErrorResponse status = cambriaHandler.sendNotification(topicName, deConfiguration.getUebPublicKey(), deConfiguration.getUebSecretKey(), deConfiguration.getUebServers(), data);

		return status;
	}

}
