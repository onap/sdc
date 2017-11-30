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

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.att.nsa.cambria.client.*;
import org.apache.http.HttpStatus;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.nsa.apiClient.http.HttpException;
import com.att.nsa.apiClient.http.HttpObjectNotFoundException;
import com.att.nsa.cambria.client.CambriaClient.CambriaApiException;
import com.att.nsa.cambria.client.CambriaClientBuilders.TopicManagerBuilder;
import com.att.nsa.cambria.client.CambriaClientBuilders.PublisherBuilder;
import com.att.nsa.cambria.client.CambriaClientBuilders.ConsumerBuilder;
import com.att.nsa.cambria.client.CambriaClientBuilders.IdentityManagerBuilder;
import com.att.nsa.cambria.client.CambriaPublisher.message;
import com.google.gson.Gson;

import fj.data.Either;
import jline.internal.Log;

public class CambriaHandler {

	private static Logger logger = LoggerFactory.getLogger(CambriaHandler.class.getName());

	private static final String PARTITION_KEY = "asdc" + "aa";

	private final String SEND_NOTIFICATION = "send notification";

	private Gson gson = new Gson();

	public static boolean useHttpsWithDmaap = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration().isUseHttpsWithDmaap();


	/**
	 * process the response error from Cambria client
	 * 
	 * @param message
	 * @return
	 */
	private Integer processMessageException(String message) {

		String[] patterns = { "(HTTP Status )(\\d\\d\\d)", "(HTTP/\\d.\\d )(\\d\\d\\d)" };

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
				} catch (NumberFormatException e) {
					logger.debug("Failed to parse http code {}", httpCode);
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
	public Either<Set<String>, CambriaErrorResponse> getTopics(List<String> hostSet) {

		CambriaTopicManager createTopicManager = null;
		try {

			createTopicManager = buildCambriaClient(new TopicManagerBuilder().usingHosts(hostSet));

			Set<String> topics = createTopicManager.getTopics();

			if (topics == null || true == topics.isEmpty()) {
				CambriaErrorResponse cambriaErrorResponse = new CambriaErrorResponse(CambriaOperationStatus.NOT_FOUND, null);
				return Either.right(cambriaErrorResponse);
			}

			return Either.left(topics);

		} catch (IOException | GeneralSecurityException e) {
			String methodName = new Object() {
			}.getClass().getEnclosingMethod().getName();

			CambriaErrorResponse cambriaErrorResponse = processError(e);

			logger.debug("Failed to fetch topics from U-EB server", e);
			writeErrorToLog(cambriaErrorResponse, e.getMessage(), methodName, "get topics");

			return Either.right(cambriaErrorResponse);
		} finally {
			if (createTopicManager != null) {
				createTopicManager.close();
			}
		}

	}

	/**
	 * process the error message from Cambria client.
	 * 
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
		} else {

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

			if (false == found) {
				cambriaErrorResponse.setOperationStatus(CambriaOperationStatus.CONNNECTION_ERROR);
				cambriaErrorResponse.setHttpCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}
		}

		return cambriaErrorResponse;
	}

	/**
	 * write the error to the log
	 * 
	 * @param cambriaErrorResponse
	 * @param errorMessage
	 * @param methodName
	 * @param operationDesc
	 */
	private void writeErrorToLog(CambriaErrorResponse cambriaErrorResponse, String errorMessage, String methodName, String operationDesc) {

		String httpCode = (cambriaErrorResponse.getHttpCode() == null ? "" : String.valueOf(cambriaErrorResponse.getHttpCode()));

		switch (cambriaErrorResponse.getOperationStatus()) {
		case UNKNOWN_HOST_ERROR:
			String hostname = cambriaErrorResponse.getVariables().get(0);
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeUebUnkownHostError, methodName, hostname);
			BeEcompErrorManager.getInstance().logBeUebUnkownHostError(methodName, httpCode);
			break;
		case AUTHENTICATION_ERROR:
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeUebAuthenticationError, methodName, httpCode);
			BeEcompErrorManager.getInstance().logBeUebAuthenticationError(methodName, httpCode);
			break;
		case CONNNECTION_ERROR:
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeUebConnectionError, methodName, httpCode);
			BeEcompErrorManager.getInstance().logBeUebConnectionError(methodName, httpCode);
			break;

		case INTERNAL_SERVER_ERROR:
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeUebSystemError, methodName, operationDesc);
			BeEcompErrorManager.getInstance().logBeUebSystemError(methodName, operationDesc);
			break;
		default:
			break;
		}

	}

	/**
	 * create a topic if it does not exists in the topicsList
	 * 
	 * @param hostSet
	 *            - list of U-EB servers
	 * @param apiKey
	 * @param secretKey
	 * @param topicName
	 *            - topic to create
	 * @param partitionCount
	 * @param replicationCount
	 * @return
	 */
	public CambriaErrorResponse createTopic(Collection<String> hostSet, String apiKey, String secretKey, String topicName, int partitionCount, int replicationCount) {

		CambriaTopicManager createTopicManager = null;
		try {

			createTopicManager = buildCambriaClient(new TopicManagerBuilder().usingHosts(hostSet).authenticatedBy(apiKey, secretKey));

			createTopicManager.createTopic(topicName, "ASDC distribution notification topic", partitionCount, replicationCount);

		} catch (HttpException | IOException | GeneralSecurityException e) {

			logger.debug("Failed to create topic {}", topicName, e);
			String methodName = new Object() {
			}.getClass().getEnclosingMethod().getName();

			CambriaErrorResponse cambriaErrorResponse = processError(e);

			if (cambriaErrorResponse.getOperationStatus() != CambriaOperationStatus.TOPIC_ALREADY_EXIST) {
				writeErrorToLog(cambriaErrorResponse, e.getMessage(), methodName, "create topic");
			}

			return cambriaErrorResponse;

		} finally {
			if (createTopicManager != null) {
				createTopicManager.close();
			}
		}
		return new CambriaErrorResponse(CambriaOperationStatus.OK);

	}

	public CambriaErrorResponse unRegisterFromTopic(Collection<String> hostSet, String topicName, String managerApiKey, String managerSecretKey, String subscriberApiKey, SubscriberTypeEnum subscriberTypeEnum) {
		CambriaTopicManager createTopicManager = null;
		try {
			createTopicManager = buildCambriaClient(new TopicManagerBuilder().usingHosts(hostSet).authenticatedBy(managerApiKey, managerSecretKey));

			if (subscriberTypeEnum == SubscriberTypeEnum.PRODUCER) {
				createTopicManager.revokeProducer(topicName, subscriberApiKey);
			} else {
				createTopicManager.revokeConsumer(topicName, subscriberApiKey);
			}

		} catch (HttpObjectNotFoundException | GeneralSecurityException e) {
			logger.debug("Failed to unregister {} from topic {} as {}", managerApiKey, topicName, subscriberTypeEnum.toString().toLowerCase(), e);
			String methodName = new Object() {
			}.getClass().getEnclosingMethod().getName();

			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeUebObjectNotFoundError, methodName, e.getMessage());
			BeEcompErrorManager.getInstance().logBeUebObjectNotFoundError(methodName, e.getMessage());

			CambriaErrorResponse cambriaErrorResponse = new CambriaErrorResponse(CambriaOperationStatus.OBJECT_NOT_FOUND, HttpStatus.SC_NOT_FOUND);
			return cambriaErrorResponse;

		} catch (HttpException | IOException e) {
			logger.debug("Failed to unregister {} from topic {} as producer", managerApiKey, topicName, e);
			String methodName = new Object() {
			}.getClass().getEnclosingMethod().getName();

			CambriaErrorResponse cambriaErrorResponse = processError(e);

			writeErrorToLog(cambriaErrorResponse, e.getMessage(), methodName, "unregister from topic as " + subscriberTypeEnum.toString().toLowerCase());

			return cambriaErrorResponse;
		} finally {
			if (createTopicManager != null) {
				createTopicManager.close();
			}
		}

		CambriaErrorResponse cambriaErrorResponse = new CambriaErrorResponse(CambriaOperationStatus.OK, HttpStatus.SC_OK);
		return cambriaErrorResponse;
	}

	/**
	 * 
	 * register a public key (subscriberId) to a given topic as a CONSUMER or PRODUCER
	 * 
	 * @param hostSet
	 * @param topicName
	 * @param managerApiKey
	 * @param managerSecretKey
	 * @param subscriberApiKey
	 * @param subscriberTypeEnum
	 * @return
	 */
	public CambriaErrorResponse registerToTopic(Collection<String> hostSet, String topicName, String managerApiKey, String managerSecretKey, String subscriberApiKey, SubscriberTypeEnum subscriberTypeEnum) {

		CambriaTopicManager createTopicManager = null;
		try {
			createTopicManager = buildCambriaClient(new TopicManagerBuilder().usingHosts(hostSet).authenticatedBy(managerApiKey, managerSecretKey));

			if (subscriberTypeEnum == SubscriberTypeEnum.PRODUCER) {
				createTopicManager.allowProducer(topicName, subscriberApiKey);
			} else {
				createTopicManager.allowConsumer(topicName, subscriberApiKey);
			}

		} catch (HttpObjectNotFoundException | GeneralSecurityException e) {
			logger.debug("Failed to register {} to topic {} as {}", managerApiKey, topicName, subscriberTypeEnum.toString().toLowerCase(), e);
			String methodName = new Object() {
			}.getClass().getEnclosingMethod().getName();

			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeUebObjectNotFoundError, methodName, e.getMessage());
			BeEcompErrorManager.getInstance().logBeUebObjectNotFoundError(methodName, e.getMessage());

			CambriaErrorResponse cambriaErrorResponse = new CambriaErrorResponse(CambriaOperationStatus.OBJECT_NOT_FOUND, HttpStatus.SC_NOT_FOUND);
			return cambriaErrorResponse;

		} catch (HttpException | IOException e) {
			logger.debug("Failed to register {} to topic {} as {}", managerApiKey, topicName, subscriberTypeEnum.toString().toLowerCase(), e);
			String methodName = new Object() {
			}.getClass().getEnclosingMethod().getName();

			CambriaErrorResponse cambriaErrorResponse = processError(e);

			writeErrorToLog(cambriaErrorResponse, e.getMessage(), methodName, "register to topic as " + subscriberTypeEnum.toString().toLowerCase());

			return cambriaErrorResponse;
		} finally {
			if (createTopicManager != null) {
				createTopicManager.close();
			}
		}

		CambriaErrorResponse cambriaErrorResponse = new CambriaErrorResponse(CambriaOperationStatus.OK, HttpStatus.SC_OK);
		return cambriaErrorResponse;
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
	public CambriaConsumer createConsumer(Collection<String> hostSet, String topicName, String apiKey, String secretKey, String consumerId, String consumerGroup, int timeoutMS) throws Exception {

		CambriaConsumer consumer = new ConsumerBuilder().authenticatedBy(apiKey, secretKey).knownAs(consumerGroup, consumerId).onTopic(topicName).usingHttps(useHttpsWithDmaap).usingHosts(hostSet).withSocketTimeout(timeoutMS).build();
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
	public Either<Iterable<String>, CambriaErrorResponse> fetchFromTopic(CambriaConsumer topicConsumer) {

		try {
			Iterable<String> messages = topicConsumer.fetch();
			if (messages == null) {
				messages = new ArrayList<String>();
			}
			return Either.left(messages);

		} catch (IOException e) {
			String methodName = new Object() {
			}.getClass().getEnclosingMethod().getName();

			CambriaErrorResponse cambriaErrorResponse = processError(e);

			logger.debug("Failed to fetch from U-EB topic. error={}", e.getMessage());
			writeErrorToLog(cambriaErrorResponse, e.getMessage(), methodName, "get messages from topic");

			return Either.right(cambriaErrorResponse);

		} catch (Exception e) {
			logger.debug("Failed to fetch from U-EB topic", e);
			String methodName = new Object() {
			}.getClass().getEnclosingMethod().getName();

			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDistributionEngineSystemError, methodName, e.getMessage());
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
	public CambriaErrorResponse sendNotification(String topicName, String uebPublicKey, String uebSecretKey, List<String> uebServers, INotificationData data) {

		CambriaBatchingPublisher createSimplePublisher = null;

		try {

			String json = gson.toJson(data);
			logger.trace("Before sending notification data {} to topic {}", json, topicName);

			createSimplePublisher = new PublisherBuilder().onTopic(topicName).usingHttps(useHttpsWithDmaap).usingHosts(uebServers).build();
			createSimplePublisher.setApiCredentials(uebPublicKey, uebSecretKey);

			int result = createSimplePublisher.send(PARTITION_KEY, json);

			try {
				Thread.sleep(1 * 1000);
			} catch (InterruptedException e) {
				logger.debug("Failed during sleep after sending the message.", e);
			}

			logger.debug("After sending notification data to topic {}. result is {}", topicName, result);

			CambriaErrorResponse response = new CambriaErrorResponse(CambriaOperationStatus.OK, 200);

			return response;

		} catch (IOException | GeneralSecurityException e) {
			logger.debug("Failed to send notification {} to topic {} ", data, topicName, e);

			String methodName = new Object() {
			}.getClass().getEnclosingMethod().getName();

			CambriaErrorResponse cambriaErrorResponse = processError(e);

			writeErrorToLog(cambriaErrorResponse, e.getMessage(), methodName, SEND_NOTIFICATION);

			return cambriaErrorResponse;
		} finally {
			if (createSimplePublisher != null) {
				logger.debug("Before closing publisher");
				createSimplePublisher.close();
				logger.debug("After closing publisher");
			}
		}
	}

	private String convertListToString(List<String> list) {
		StringBuilder builder = new StringBuilder();

		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				builder.append(list.get(i));
				if (i < list.size() - 1) {
					builder.append(",");
				}
			}
		}

		return builder.toString();
	}

	public CambriaErrorResponse sendNotificationAndClose(String topicName, String uebPublicKey, String uebSecretKey, List<String> uebServers, INotificationData data, long waitBeforeCloseTimeout) {

		CambriaBatchingPublisher createSimplePublisher = null;

		CambriaErrorResponse response = null;
		try {

			String json = gson.toJson(data);
			logger.debug("Before sending notification data {} to topic {}", json, topicName);

			createSimplePublisher = new PublisherBuilder().onTopic(topicName).usingHttps(useHttpsWithDmaap).usingHosts(uebServers).build();
			createSimplePublisher.setApiCredentials(uebPublicKey, uebSecretKey);

			int result = createSimplePublisher.send(PARTITION_KEY, json);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.debug("Failed during sleep after sending the message.", e);
			}

			logger.debug("After sending notification data to topic {}. result is {}", topicName, result);

		} catch (IOException | GeneralSecurityException e) {
			logger.debug("Failed to send notification {} to topic {} ", data, topicName, e);

			String methodName = new Object() {
			}.getClass().getEnclosingMethod().getName();

			response = processError(e);

			writeErrorToLog(response, e.getMessage(), methodName, SEND_NOTIFICATION);

			return response;

		}

		logger.debug("Before closing publisher. Maximum timeout is {} seconds", waitBeforeCloseTimeout);
		try {
			List<message> messagesInQ = createSimplePublisher.close(waitBeforeCloseTimeout, TimeUnit.SECONDS);
			if (messagesInQ != null && false == messagesInQ.isEmpty()) {
				logger.debug("Cambria client returned {} non sent messages.", messagesInQ.size());
				response = new CambriaErrorResponse(CambriaOperationStatus.INTERNAL_SERVER_ERROR, 500);
				String methodName = new Object() {
				}.getClass().getEnclosingMethod().getName();
				writeErrorToLog(response, "closing publisher returned non sent messages", methodName, SEND_NOTIFICATION);
			} else {
				logger.debug("No message left in the queue after closing cambria publisher");
				response = new CambriaErrorResponse(CambriaOperationStatus.OK, 200);
			}
		} catch (IOException | InterruptedException e) {
			logger.debug("Failed to close cambria publisher", e);
			response = new CambriaErrorResponse(CambriaOperationStatus.INTERNAL_SERVER_ERROR, 500);
			String methodName = new Object() {
			}.getClass().getEnclosingMethod().getName();
			writeErrorToLog(response, "closing publisher returned non sent messages", methodName, SEND_NOTIFICATION);
		}
		logger.debug("After closing publisher");

		return response;

	}

	public CambriaErrorResponse getApiKey(String server, String apiKey) {

		CambriaErrorResponse response = null;

		List<String> hostSet = new ArrayList<>();
		hostSet.add(server);
		CambriaIdentityManager createIdentityManager = null;
		try {
			createIdentityManager = buildCambriaClient(new IdentityManagerBuilder().usingHosts(hostSet));
			createIdentityManager.getApiKey(apiKey);
			response = new CambriaErrorResponse(CambriaOperationStatus.OK, 200);

		} catch (HttpException | IOException | CambriaApiException | GeneralSecurityException e) {
			logger.debug("Failed to fetch api key {} from server ", apiKey, server, e);

			response = processError(e);

		}

		return response;
	}

	private static <T extends CambriaClient> T buildCambriaClient(CambriaClientBuilders.AbstractAuthenticatedManagerBuilder<? extends CambriaClient> client) throws MalformedURLException, GeneralSecurityException {
		if (useHttpsWithDmaap) {
			client.usingHttps();
		}
		return (T)client.build();
	}
}
