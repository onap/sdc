package org.openecomp.sdc.asdctool.migration.config.mocks;

import com.att.nsa.apiClient.credentials.ApiCredential;
import fj.data.Either;
import org.openecomp.sdc.be.components.distribution.engine.CambriaErrorResponse;
import org.openecomp.sdc.be.components.distribution.engine.ICambriaHandler;
import org.openecomp.sdc.be.components.distribution.engine.INotificationData;
import org.openecomp.sdc.be.components.distribution.engine.SubscriberTypeEnum;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Component("cambriaHandler")
public class CambriaHandlerMock implements ICambriaHandler {

    @Override
    public Either<Set<String>, CambriaErrorResponse> getTopics(List<String> hostSet) {
        return null;
    }

    @Override
    public CambriaErrorResponse createTopic(Collection<String> hostSet, String apiKey, String secretKey, String topicName, int partitionCount, int replicationCount) {
        return null;
    }

    @Override
    public CambriaErrorResponse unRegisterFromTopic(Collection<String> hostSet, String managerApiKey, String managerSecretKey, String subscriberApiKey, SubscriberTypeEnum subscriberTypeEnum, String topicName) {
        return null;
    }

    @Override
    public CambriaErrorResponse registerToTopic(Collection<String> hostSet, String managerApiKey, String managerSecretKey, String subscriberApiKey, SubscriberTypeEnum subscriberTypeEnum, String topicName) {
        return null;
    }

    @Override
    public com.att.nsa.cambria.client.CambriaConsumer createConsumer(Collection<String> hostSet, String topicName, String apiKey, String secretKey, String consumerId, String consumerGroup, int timeoutMS) throws Exception {
        return null;
    }

    @Override
    public CambriaErrorResponse sendNotification(String topicName, String uebPublicKey, String uebSecretKey, List<String> uebServers, INotificationData data) {
        return null;
    }

    @Override
    public CambriaErrorResponse sendNotificationAndClose(String topicName, String uebPublicKey, String uebSecretKey, List<String> uebServers, INotificationData data, long waitBeforeCloseTimeout) {
        return null;
    }

    @Override
    public CambriaErrorResponse getApiKey(String server, String apiKey) {
        return null;
    }

    @Override
    public Either<ApiCredential, CambriaErrorResponse> createUebKeys(List<String> hostSet) {
        return null;
    }

    @Override
    public Either<Iterable<String>, CambriaErrorResponse> fetchFromTopic(com.att.nsa.cambria.client.CambriaConsumer topicConsumer) {
        return null;
    }
}
