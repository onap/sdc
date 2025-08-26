package org.openecomp.sdc.be.components.distribution.engine;

import org.openecomp.sdc.be.components.kafka.KafkaHandler;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

@Component("distributionDeleteNotifcationSender")
public class DistributionDeleteNotifcationSender {
    private static final Logger logger = Logger.getLogger(DistributionDeleteNotifcationSender.class.getName());

    protected ComponentsUtils componentUtils;

    private final CambriaHandler cambriaHandler;

    private final KafkaHandler kafkaHandler;

    private final DistributionEngineConfiguration deConfiguration;

    public DistributionDeleteNotifcationSender() {
        this.kafkaHandler = new KafkaHandler();
        this.deConfiguration = ConfigurationManager.getConfigurationManager()
                .getDistributionEngineConfiguration();
        this.cambriaHandler = new CambriaHandler();
    }

    public DistributionDeleteNotifcationSender(KafkaHandler kafkaHandler,
            DistributionEngineConfiguration deConfiguration, CambriaHandler cambriaHandler) {
        this.kafkaHandler = kafkaHandler;
        this.deConfiguration = deConfiguration;
        this.cambriaHandler = cambriaHandler;
    }

    /**
     * This method is used to send the notification for the delete service.
     * 
     * @param topicName        name of the Kafka topic for delete service
     * @param distributionId   random UUID generated for the notification
     * @param messageBusData   environment message bus data
     * @param notificationData the payload data to be sent in the Kafka notification
     * @return the action status of the notification
     */
    public ActionStatus sendNotificationForDeleteService(String topicName, String distributionId,
            EnvironmentMessageBusData messageBusData,
            INotificationData notificationData) {
        logger.debug("sending notification with topicName={}, distributionId={}",
                topicName,
                distributionId);

        CambriaErrorResponse status;
        if (Boolean.FALSE.equals(kafkaHandler.isKafkaActive())) {
            logger.debug("Kafka is inactive. Using CambriaHandler to send notification for topic={}", topicName);
            status = cambriaHandler
                    .sendNotificationAndClose(topicName, messageBusData.getUebPublicKey(),
                            messageBusData.getUebPrivateKey(),
                            messageBusData.getDmaaPuebEndpoints(), notificationData,
                            deConfiguration.getDistributionDeleteTopic().getMaxWaitingAfterSendingSeconds());
        } else {
            logger.debug("Kafka is active. Using KafkaHandler to send notification for topic={}", topicName);
            status = kafkaHandler.sendNotification(topicName, notificationData);
        }

        logger.info("sending notification is completed for topicName={}, with status = {}",
                topicName, status);
        return convertCambriaResponse(status);
    }

    /**
     * This method is used to convert a {@link CambriaErrorResponse} into the
     * corresponding {@link ActionStatus}.
     * 
     * @param status the operation status
     * @return the mapped ActionStatus based on the operation status
     */
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
