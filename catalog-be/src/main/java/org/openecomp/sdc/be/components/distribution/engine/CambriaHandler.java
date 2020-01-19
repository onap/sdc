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

import com.att.nsa.apiClient.credentials.ApiCredential;
import com.att.nsa.apiClient.http.HttpException;
import com.att.nsa.apiClient.http.HttpObjectNotFoundException;
import com.att.nsa.cambria.client.CambriaBatchingPublisher;
import com.att.nsa.cambria.client.CambriaClient;
import com.att.nsa.cambria.client.CambriaClient.CambriaApiException;
import com.att.nsa.cambria.client.CambriaClientBuilders;
import com.att.nsa.cambria.client.CambriaClientBuilders.AbstractAuthenticatedManagerBuilder;
import com.att.nsa.cambria.client.CambriaClientBuilders.ConsumerBuilder;
import com.att.nsa.cambria.client.CambriaClientBuilders.IdentityManagerBuilder;
import com.att.nsa.cambria.client.CambriaClientBuilders.PublisherBuilder;
import com.att.nsa.cambria.client.CambriaClientBuilders.TopicManagerBuilder;
import com.att.nsa.cambria.client.CambriaConsumer;
import com.att.nsa.cambria.client.CambriaIdentityManager;
import com.att.nsa.cambria.client.CambriaPublisher.message;
import com.att.nsa.cambria.client.CambriaTopicManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import fj.data.Either;
import org.apache.http.HttpStatus;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.SECONDS;

@Component("cambriaHandler")
public class CambriaHandler implements ICambriaHandler{

    private static final Logger log = Logger.getLogger(CambriaHandler.class.getName());
    private static final String PARTITION_KEY = "asdc" + "aa";
    private static final String SEND_NOTIFICATION = "send notification";
    private static final String CONSUMER_ID = ConfigurationManager.getConfigurationManager()
                                                                  .getDistributionEngineConfiguration()
                                                                  .getDistributionStatusTopic()
                                                                  .getConsumerId();
    private static final boolean USE_HTTPS_WITH_DMAAP = ConfigurationManager.getConfigurationManager()
                                                                    .getDistributionEngineConfiguration()
                                                                    .isUseHttpsWithDmaap();
    private final Gson gson = new Gson();


    /**
     * process the response error from Cambria client
     *
     * @param message
     * @return
     */
    private Integer processMessageException(String message) {

        String[] patterns = {"(HTTP Status )(\\d\\d\\d)", "(HTTP/\\d.\\d )(\\d\\d\\d)"};

        Integer result = checkPattern(patterns[0], message, 2);
        if (result != null) {
            return result;
        }
        result = checkPattern(patterns[1], message, 2);

        return result;

    }

    /**
     * check whether the message has a match with a given pattern inside it
     *
     * @param patternStr
     * @param message
     * @param groupIndex
     * @return
     */
    private Integer checkPattern(String patternStr, String message, int groupIndex) {
        Integer result = null;

        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(message);
        boolean find = matcher.find();
        if (find) {
            String httpCode = matcher.group(groupIndex);
            if (httpCode != null) {
                try {
                    result = Integer.valueOf(httpCode);
                }
                catch (NumberFormatException e) {
                    log.debug("Failed to parse http code {}", httpCode);
                }
            }
        }
        return result;
    }

    /**
     * retrieve all topics from U-EB server
     *
     * @param hostSet
     * @return
     */
    @Override
    public Either<Set<String>, CambriaErrorResponse> getTopics(List<String> hostSet) {

        CambriaTopicManager createTopicManager = null;
        try {

            createTopicManager = buildCambriaClient(createTopicManagerBuilder(hostSet));

            Set<String> topics = createTopicManager.getTopics();

            if (topics == null || topics.isEmpty()) {
                CambriaErrorResponse cambriaErrorResponse = new CambriaErrorResponse(CambriaOperationStatus.NOT_FOUND, null);
                return Either.right(cambriaErrorResponse);
            }

            return Either.left(topics);

        }
        catch (IOException | GeneralSecurityException e) {

            CambriaErrorResponse cambriaErrorResponse = processError(e);

            log.debug("Failed to fetch topics from U-EB server", e);
            writeErrorToLog(cambriaErrorResponse, "getTopics", "get topics");

            return Either.right(cambriaErrorResponse);
        } finally {
            if (createTopicManager != null) {
                createTopicManager.close();
            }
        }

    }

    /**
     * process the error message from Cambria client.
     * <p>
     * set Cambria status and http code in case we succeed to fetch it
     *
     * @return
     */
    private CambriaErrorResponse processError(Exception e) {

        CambriaErrorResponse cambriaErrorResponse = new CambriaErrorResponse();

        Integer httpCode = processMessageException(e.getMessage());

        if (httpCode != null) {
            cambriaErrorResponse.setHttpCode(httpCode);
            switch (httpCode.intValue()) {

                case 401:
                    cambriaErrorResponse.setOperationStatus(CambriaOperationStatus.AUTHENTICATION_ERROR);
                    break;
                case 409:
                    cambriaErrorResponse.setOperationStatus(CambriaOperationStatus.TOPIC_ALREADY_EXIST);
                    break;
                case 500:
                    cambriaErrorResponse.setOperationStatus(CambriaOperationStatus.INTERNAL_SERVER_ERROR);
                    break;
                default:
                    cambriaErrorResponse.setOperationStatus(CambriaOperationStatus.CONNNECTION_ERROR);
            }
        }
        else {

            boolean found = false;
            Throwable throwable = e.getCause();
            if (throwable != null) {
                String message = throwable.getMessage();

                Throwable cause = throwable.getCause();

                if (cause != null) {
                    Class<?> clazz = cause.getClass();
                    String className = clazz.getName();
                    if (className.endsWith("UnknownHostException")) {
                        cambriaErrorResponse.setOperationStatus(CambriaOperationStatus.UNKNOWN_HOST_ERROR);
                        cambriaErrorResponse.addVariable(message);
                        found = true;
                    }
                }
            }

            if (!found) {
                cambriaErrorResponse.setOperationStatus(CambriaOperationStatus.CONNNECTION_ERROR);
                cambriaErrorResponse.setHttpCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }
        }

        return cambriaErrorResponse;
    }

    /**
     * write the error to the log
     *  @param cambriaErrorResponse
     * @param methodName
     * @param operationDesc
     */
    private void writeErrorToLog(CambriaErrorResponse cambriaErrorResponse, String methodName, String operationDesc) {

        String httpCode = cambriaErrorResponse.getHttpCode() == null ? "" : String.valueOf(cambriaErrorResponse.getHttpCode());

        switch (cambriaErrorResponse.getOperationStatus()) {
            case UNKNOWN_HOST_ERROR:
                BeEcompErrorManager.getInstance().logBeUebUnkownHostError(methodName, httpCode);
                break;
            case AUTHENTICATION_ERROR:
                BeEcompErrorManager.getInstance().logBeUebAuthenticationError(methodName, httpCode);
                break;
            case CONNNECTION_ERROR:
                BeEcompErrorManager.getInstance().logBeUebConnectionError(methodName, httpCode);
                break;
            case INTERNAL_SERVER_ERROR:
                BeEcompErrorManager.getInstance().logBeUebSystemError(methodName, operationDesc);
                break;
            default:
                break;
        }

    }

    /**
     * create a topic if it does not exists in the topicsList
     *
     * @param hostSet          - list of U-EB servers
     * @param apiKey
     * @param secretKey
     * @param topicName        - topic to create
     * @param partitionCount
     * @param replicationCount
     * @return
     */
    @Override
    public CambriaErrorResponse createTopic(Collection<String> hostSet, String apiKey, String secretKey, String topicName, int partitionCount, int replicationCount) {

        CambriaTopicManager createTopicManager = null;
        try {

            AbstractAuthenticatedManagerBuilder<CambriaTopicManager> clientBuilder = createTopicManagerBuilder(hostSet, apiKey, secretKey);
            createTopicManager = buildCambriaClient(clientBuilder);
            
            createTopicManager.createTopic(topicName, "ASDC distribution notification topic", partitionCount, replicationCount);

        }
        catch (HttpException | IOException | GeneralSecurityException e) {

            log.debug("Failed to create topic {}", topicName, e);

            CambriaErrorResponse cambriaErrorResponse = processError(e);

            if (cambriaErrorResponse.getOperationStatus() != CambriaOperationStatus.TOPIC_ALREADY_EXIST) {
                writeErrorToLog(cambriaErrorResponse, "createTopic", "create topic");
            }

            return cambriaErrorResponse;

        } finally {
            if (createTopicManager != null) {
                createTopicManager.close();
            }
        }
        return new CambriaErrorResponse(CambriaOperationStatus.OK);

    }
    @Override
    public CambriaErrorResponse unRegisterFromTopic(Collection<String> hostSet, String managerApiKey, String managerSecretKey, String subscriberApiKey, SubscriberTypeEnum subscriberTypeEnum, String topicName) {
        String methodName = "unRegisterFromTopic";
        CambriaTopicManager createTopicManager = null;
        try {
            AbstractAuthenticatedManagerBuilder<CambriaTopicManager> clientBuilder = createTopicManagerBuilder(hostSet, managerApiKey, managerSecretKey);
            
            createTopicManager = buildCambriaClient(clientBuilder);

            if (subscriberTypeEnum == SubscriberTypeEnum.PRODUCER) {
                createTopicManager.revokeProducer(topicName, subscriberApiKey);
            }
            else {
                createTopicManager.revokeConsumer(topicName, subscriberApiKey);
            }

        }
        catch (HttpObjectNotFoundException e) {
            log.debug("Failed to unregister {} from topic {} as {}", managerApiKey, topicName, subscriberTypeEnum.toString()
                                                                                                                 .toLowerCase(), e);
            BeEcompErrorManager.getInstance().logBeUebObjectNotFoundError(methodName, e.getMessage());

            return new CambriaErrorResponse(CambriaOperationStatus.OBJECT_NOT_FOUND, HttpStatus.SC_NOT_FOUND);

        }
        catch (HttpException | IOException | GeneralSecurityException e) {
            log.debug("Failed to unregister {} from topic {} as producer", managerApiKey, topicName, e);
            CambriaErrorResponse cambriaErrorResponse = processError(e);

            writeErrorToLog(cambriaErrorResponse, methodName, "unregister from topic as " + subscriberTypeEnum
                    .toString()
                    .toLowerCase());

            return cambriaErrorResponse;
        } finally {
            if (createTopicManager != null) {
                createTopicManager.close();
            }
        }

        return new CambriaErrorResponse(CambriaOperationStatus.OK, HttpStatus.SC_OK);
    }

    private AbstractAuthenticatedManagerBuilder<CambriaTopicManager> createTopicManagerBuilder(Collection<String> hostSet, String managerApiKey, String managerSecretKey) {
        AbstractAuthenticatedManagerBuilder<CambriaTopicManager> clientBuilder = createTopicManagerBuilder(hostSet)
                                                                         .authenticatedBy(managerApiKey, managerSecretKey);
        if (USE_HTTPS_WITH_DMAAP) {
            clientBuilder = clientBuilder.usingHttps();
        }
        
        return clientBuilder;
    }

    private AbstractAuthenticatedManagerBuilder<CambriaTopicManager> createTopicManagerBuilder(Collection<String> hostSet) {
        return new TopicManagerBuilder().usingHosts(hostSet);
    }

    /**
     * register a public key (subscriberId) to a given topic as a CONSUMER or PRODUCER
     *
     * @param hostSet
     * @param managerApiKey
     * @param managerSecretKey
     * @param subscriberApiKey
     * @param subscriberTypeEnum
     * @param topicName
     * @return
     */
    @Override
    public CambriaErrorResponse registerToTopic(Collection<String> hostSet, String managerApiKey, String managerSecretKey, String subscriberApiKey, SubscriberTypeEnum subscriberTypeEnum, String topicName) {

        String methodName = "registerToTopic";
        CambriaTopicManager createTopicManager = null;
        try {
            AbstractAuthenticatedManagerBuilder<CambriaTopicManager> clientBuilder = createTopicManagerBuilder(hostSet, managerApiKey, managerSecretKey);
            createTopicManager = buildCambriaClient(clientBuilder);

            if (subscriberTypeEnum == SubscriberTypeEnum.PRODUCER) {
                createTopicManager.allowProducer(topicName, subscriberApiKey);
            }
            else {
                createTopicManager.allowConsumer(topicName, subscriberApiKey);
            }

        }
        catch (HttpObjectNotFoundException e) {
            log.debug("Failed to register {} to topic {} as {}", managerApiKey, topicName, subscriberTypeEnum.toString()
                                                                                                             .toLowerCase(), e);

            BeEcompErrorManager.getInstance().logBeUebObjectNotFoundError(methodName, e.getMessage());

            return new CambriaErrorResponse(CambriaOperationStatus.OBJECT_NOT_FOUND, HttpStatus.SC_NOT_FOUND);

        }
        catch (HttpException | IOException | GeneralSecurityException e) {
            log.debug("Failed to register {} to topic {} as {}", managerApiKey, topicName, subscriberTypeEnum.toString()
                                                                                                             .toLowerCase(), e);
            CambriaErrorResponse cambriaErrorResponse = processError(e);

            writeErrorToLog(cambriaErrorResponse, methodName, "register to topic as " + subscriberTypeEnum
                    .toString()
                    .toLowerCase());

            return cambriaErrorResponse;
        } finally {
            if (createTopicManager != null) {
                createTopicManager.close();
            }
        }

        return new CambriaErrorResponse(CambriaOperationStatus.OK, HttpStatus.SC_OK);
    }

    /**
     * create and retrieve a Cambria Consumer for a specific topic
     *
     * @param hostSet
     * @param topicName
     * @param apiKey
     * @param secretKey
     * @param consumerId
     * @param consumerGroup
     * @param timeoutMS
     * @return
     * @throws Exception
     */
    @Override
    public CambriaConsumer createConsumer(Collection<String> hostSet, String topicName, String apiKey, String secretKey, String consumerId, String consumerGroup, int timeoutMS) throws Exception {

        CambriaConsumer consumer = new ConsumerBuilder().authenticatedBy(apiKey, secretKey)
                                                        .knownAs(consumerGroup, consumerId)
                                                        .onTopic(topicName)
                                                        .usingHosts(hostSet)
                                                        .waitAtServer(timeoutMS)
                                                        .build();
        consumer.setApiCredentials(apiKey, secretKey);
        return consumer;
    }

    public void closeConsumer(CambriaConsumer consumer) {

        if (consumer != null) {
            consumer.close();
        }

    }

    /**
     * use the topicConsumer to fetch messages from topic. in case no messages were fetched, empty ArrayList will be returned (not error)
     *
     * @param topicConsumer
     * @return
     */
    @Override
    public Either<Iterable<String>, CambriaErrorResponse> fetchFromTopic(CambriaConsumer topicConsumer) {

        String methodName = "fetchFromTopic";
        try {
            Iterable<String> messages = topicConsumer.fetch();
            if (messages == null) {
                messages = new ArrayList<>();
            }
            return Either.left(messages);

        }
        catch (IOException e) {
            CambriaErrorResponse cambriaErrorResponse = processError(e);
            log.debug("Failed to fetch from U-EB topic. error={}", e.getMessage());
            writeErrorToLog(cambriaErrorResponse, methodName, "get messages from topic");
            return Either.right(cambriaErrorResponse);

        }
        catch (Exception e) {
            log.debug("Failed to fetch from U-EB topic", e);
            BeEcompErrorManager.getInstance().logBeDistributionEngineSystemError(methodName, e.getMessage());
            CambriaErrorResponse cambriaErrorResponse = new CambriaErrorResponse(CambriaOperationStatus.INTERNAL_SERVER_ERROR, HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return Either.right(cambriaErrorResponse);
        }
    }

    /**
     * Publish notification message to a given queue
     *
     * @param topicName
     * @param uebPublicKey
     * @param uebSecretKey
     * @param uebServers
     * @param data
     * @return
     */
    @Override
    public CambriaErrorResponse sendNotification(String topicName, String uebPublicKey, String uebSecretKey, List<String> uebServers, INotificationData data) {

        CambriaBatchingPublisher createSimplePublisher = null;

        try {

            String json = gson.toJson(data);
            log.trace("Before sending notification data {} to topic {}", json, topicName);

            createSimplePublisher = new PublisherBuilder().onTopic(topicName).usingHosts(uebServers).build();
            createSimplePublisher.setApiCredentials(uebPublicKey, uebSecretKey);

            int result = createSimplePublisher.send(PARTITION_KEY, json);

            try {
                SECONDS.sleep(1L);
            }
            catch (InterruptedException e) {
                log.debug("Failed during sleep after sending the message.", e);
                Thread.currentThread().interrupt();
            }

            log.debug("After sending notification data to topic {}. result is {}", topicName, result);

            return new CambriaErrorResponse(CambriaOperationStatus.OK, 200);

        } catch (IOException | GeneralSecurityException e) {
            log.debug("Failed to send notification {} to topic {} ", data, topicName, e);

            CambriaErrorResponse cambriaErrorResponse = processError(e);

            writeErrorToLog(cambriaErrorResponse, "sendNotification", SEND_NOTIFICATION);

            return cambriaErrorResponse;
        }
        finally {
            if (createSimplePublisher != null) {
                log.debug("Before closing publisher");
                createSimplePublisher.close();
                log.debug("After closing publisher");
            }
        }
    }
    @Override
    public CambriaErrorResponse sendNotificationAndClose(String topicName, String uebPublicKey, String uebSecretKey, List<String> uebServers, INotificationData data, long waitBeforeCloseTimeout) {
        String methodName = "sendNotificationAndClose";
        CambriaBatchingPublisher createSimplePublisher;
        CambriaErrorResponse response;
        try {

            String json = gson.toJson(data);
            log.debug("Before sending notification data {} to topic {}", json, topicName);

            createSimplePublisher = new PublisherBuilder().onTopic(topicName).usingHosts(uebServers).build();
            createSimplePublisher.setApiCredentials(uebPublicKey, uebSecretKey);

            int result = createSimplePublisher.send(PARTITION_KEY, json);

            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                log.debug("Failed during sleep after sending the message.", e);
                Thread.currentThread().interrupt();
            }

            log.debug("After sending notification data to topic {}. result is {}", topicName, result);

        }
        catch (IOException | GeneralSecurityException  e) {
            log.debug("Failed to send notification {} to topic {} ", data, topicName, e);


            response = processError(e);

            writeErrorToLog(response, methodName, SEND_NOTIFICATION);

            return response;

        }

        log.debug("Before closing publisher. Maximum timeout is {} seconds", waitBeforeCloseTimeout);
        try {
            List<message> messagesInQ = createSimplePublisher.close(waitBeforeCloseTimeout, SECONDS);
            if (messagesInQ != null && !messagesInQ.isEmpty()) {
                log.debug("Cambria client returned {} non sent messages.", messagesInQ.size());
                response = new CambriaErrorResponse(CambriaOperationStatus.INTERNAL_SERVER_ERROR, 500);
                writeErrorToLog(response, methodName, SEND_NOTIFICATION);
            }
            else {
                log.debug("No message left in the queue after closing cambria publisher");
                response = new CambriaErrorResponse(CambriaOperationStatus.OK, 200);
            }
        }
        catch (InterruptedException e) {
            log.debug("InterruptedException while closing cambria publisher", e);
            Thread.currentThread().interrupt();
            response = new CambriaErrorResponse(CambriaOperationStatus.INTERNAL_SERVER_ERROR, 500);
            writeErrorToLog(response, methodName, SEND_NOTIFICATION);
        }
        catch (IOException e) {
            log.debug("Failed to close cambria publisher", e);
            response = new CambriaErrorResponse(CambriaOperationStatus.INTERNAL_SERVER_ERROR, 500);
            writeErrorToLog(response, methodName, SEND_NOTIFICATION);
        }
        log.debug("After closing publisher");

        return response;

    }
    @Override
    public CambriaErrorResponse getApiKey(String server, String apiKey) {

        CambriaErrorResponse response;
        List<String> hostSet = new ArrayList<>();
        hostSet.add(server);
        try {
            CambriaIdentityManager createIdentityManager = buildCambriaClient(new IdentityManagerBuilder().usingHosts(hostSet));
            createIdentityManager.getApiKey(apiKey);

            response = new CambriaErrorResponse(CambriaOperationStatus.OK, 200);

        }
        catch (HttpException | IOException | CambriaApiException | GeneralSecurityException e) {
            log.debug("Failed to fetch api key {} from server {}", apiKey, server, e);

            response = processError(e);

        }

        return response;
    }
    @Override
    public Either<ApiCredential, CambriaErrorResponse> createUebKeys(List<String> hostSet) {
        Either<ApiCredential, CambriaErrorResponse> result;

        try {
            CambriaIdentityManager createIdentityManager = buildCambriaClient(new IdentityManagerBuilder().usingHosts(hostSet));

            String description = String.format("ASDC Key for %s", CONSUMER_ID);
            ApiCredential credential = createIdentityManager.createApiKey("", description);
            createIdentityManager.setApiCredentials(credential.getApiKey(), credential.getApiSecret());
            result = Either.left(credential);

        }
        catch (Exception e) {
            log.debug("Failed to create ueb keys for servers {}", hostSet, e);

            result = Either.right(processError(e));

        }

        return result;
    }

    @VisibleForTesting
    <T extends CambriaClient> T buildCambriaClient(CambriaClientBuilders.AbstractAuthenticatedManagerBuilder<T> client) throws MalformedURLException, GeneralSecurityException {
        return client.build();
    }
}
