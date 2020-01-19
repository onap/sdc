/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

import com.att.nsa.apiClient.credentials.ApiCredential;
import com.att.nsa.cambria.client.CambriaConsumer;
import fj.data.Either;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ICambriaHandler {

    Either<Set<String>, CambriaErrorResponse> getTopics(List<String> hostSet);
    CambriaErrorResponse createTopic(Collection<String> hostSet, String apiKey, String secretKey, String topicName, int partitionCount, int replicationCount);
    CambriaErrorResponse unRegisterFromTopic(Collection<String> hostSet, String managerApiKey, String managerSecretKey, String subscriberApiKey, SubscriberTypeEnum subscriberTypeEnum, String topicName);
    CambriaErrorResponse registerToTopic(Collection<String> hostSet, String managerApiKey, String managerSecretKey, String subscriberApiKey, SubscriberTypeEnum subscriberTypeEnum, String topicName);
    CambriaConsumer createConsumer(Collection<String> hostSet, String topicName, String apiKey, String secretKey, String consumerId, String consumerGroup, int timeoutMS) throws Exception;
    Either<Iterable<String>, CambriaErrorResponse> fetchFromTopic(CambriaConsumer topicConsumer);
    CambriaErrorResponse sendNotification(String topicName, String uebPublicKey, String uebSecretKey, List<String> uebServers, INotificationData data);
    CambriaErrorResponse sendNotificationAndClose(String topicName, String uebPublicKey, String uebSecretKey, List<String> uebServers, INotificationData data, long waitBeforeCloseTimeout);
    CambriaErrorResponse getApiKey(String server, String apiKey);
    Either<ApiCredential, CambriaErrorResponse> createUebKeys(List<String> hostSet);


}
