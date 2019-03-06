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

import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

@Component("distributionNotificationSender")
public class DistributionNotificationSender {

    protected static final String DISTRIBUTION_NOTIFICATION_SENDING = "distributionNotificationSending";

    private static final Logger logger = Logger.getLogger(DistributionNotificationSender.class.getName());

    @javax.annotation.Resource
    protected ComponentsUtils componentUtils;
    private CambriaHandler cambriaHandler = new CambriaHandler();
    private DistributionEngineConfiguration deConfiguration =
            ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration();

    public ActionStatus sendNotification(String topicName, String distributionId,
            EnvironmentMessageBusData messageBusData, INotificationData notificationData, Service service,
            User modifier) {
        long startTime = System.currentTimeMillis();
        CambriaErrorResponse status =
                cambriaHandler.sendNotificationAndClose(topicName, messageBusData.getUebPublicKey(),
                        messageBusData.getUebPrivateKey(), messageBusData.getDmaaPuebEndpoints(), notificationData,
                        deConfiguration.getDistributionNotificationTopic().getMaxWaitingAfterSendingSeconds());
        logger.info("After publishing service {} of version {}. Status is {}", service.getName(), service.getVersion(),
                status.getHttpCode());
        auditDistributionNotification(new AuditDistributionNotificationBuilder().setTopicName(topicName).setDistributionId(distributionId).setStatus(status).setService(service).setEnvId(messageBusData.getEnvId()).setModifier(modifier).setWorkloadContext(notificationData.getWorkloadContext()).setTenant(messageBusData.getTenant()));
        long endTime = System.currentTimeMillis();
        logger.debug("After building and publishing artifacts object. Total took {} milliseconds",
                (endTime - startTime));
        return convertCambriaResponse(status);
    }

    private void auditDistributionNotification(AuditDistributionNotificationBuilder builder) {
        if (this.componentUtils != null) {
            Integer httpCode = builder.getStatus().getHttpCode();
            String httpCodeStr = String.valueOf(httpCode);

            String desc = getDescriptionFromErrorResponse(builder.getStatus());

            this.componentUtils.auditDistributionNotification(builder.getService().getUUID(),
                    builder.getService().getName(), "Service", builder.getService().getVersion(), builder.getModifier(),
                    builder.getEnvId(), builder.getService().getLifecycleState().name(), builder.getTopicName(),
                    builder.getDistributionId(), desc, httpCodeStr, builder.getWorkloadContext(), builder.getTenant());
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

    private ActionStatus convertCambriaResponse(CambriaErrorResponse status) {
        CambriaOperationStatus operationStatus = status.getOperationStatus();

        switch (operationStatus) {
            case OK:
                return ActionStatus.OK;
            case AUTHENTICATION_ERROR:
                return ActionStatus.AUTHENTICATION_ERROR;
            case INTERNAL_SERVER_ERROR:
                return ActionStatus.GENERAL_ERROR;
            case UNKNOWN_HOST_ERROR:
                return ActionStatus.UNKNOWN_HOST;
            case CONNNECTION_ERROR:
                return ActionStatus.CONNNECTION_ERROR;
            case OBJECT_NOT_FOUND:
                return ActionStatus.OBJECT_NOT_FOUND;
            default:
                return ActionStatus.GENERAL_ERROR;

        }
    }


}
