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
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.nsa.apiClient.http.HttpException;
import com.att.nsa.apiClient.http.HttpObjectNotFoundException;
import com.att.nsa.cambria.client.CambriaBatchingPublisher;
import com.att.nsa.cambria.client.CambriaClient.CambriaApiException;
import com.att.nsa.cambria.client.CambriaClientBuilders.TopicManagerBuilder;
import com.att.nsa.cambria.client.CambriaClientBuilders.PublisherBuilder;
import com.att.nsa.cambria.client.CambriaClientBuilders.ConsumerBuilder;
import com.att.nsa.cambria.client.CambriaClientBuilders.IdentityManagerBuilder;
import com.att.nsa.cambria.client.CambriaConsumer;
import com.att.nsa.cambria.client.CambriaIdentityManager;
import com.att.nsa.cambria.client.CambriaPublisher.message;
import com.att.nsa.cambria.client.CambriaTopicManager;
import com.google.gson.Gson;

import fj.data.Either;
import jline.internal.Log;

public class CambriaHandler {

	private static Logger logger = LoggerFactory.getLogger(CambriaHandler.class.getName());

	public static String PARTITION_KEY = "asdc" + "aa";

	private Gson gson = new Gson();

	public static void main(String[] args) {

		// String userBodyJson ="{\"artifactName\":\"myartifact\",
		// \"artifactType\":\"MURANO-PKG\",
		// \"artifactDescription\":\"description\",
		// \"payloadData\":\"UEsDBAoAAAAIAAeLb0bDQz\", \"Content-MD5\":
		// \"YTg2Mjg4MWJhNmI5NzBiNzdDFkMWI=\" }";
		// System.out.println(userBodyJson);
		// String encodeBase64Str = GeneralUtililty.calculateMD5 (userBodyJson);
		// System.out.println(encodeBase64Str);

		CambriaTopicManager createTopicManager = null;
		try {
			List<String> servers = new ArrayList<String>();
			// servers.add("uebsb91kcdc.it.sdc.com:3904");
			// servers.add("uebsb92kcdc.it.sdc.com:3904");
			// servers.add("uebsb93kcdc.it.sdc.com:3904");
			servers.add("uebsb91sfdc.it.att.com:3904");
			servers.add("uebsb92sfdc.it.att.com:3904");

			String key = "sSJc5qiBnKy2qrlc";
			String secret = "4ZRPzNJfEUK0sSNBvccd2m7X";

			createTopicManager = new TopicManagerBuilder().usingHttps().usingHosts(servers).authenticatedBy(key, secret).build();

			String topicName = "ASDC-DISTR-NOTIF-TOPIC-PRODesofer";

			String clientKey1 = "CGGoorrGPXPx2B1C";
			String clientSecret1 = "OTHk2mcCSbskEtHhDw8h5oUa";

			CambriaTopicManager createStatusTopicManager = new TopicManagerBuilder().usingHttps().usingHosts(servers).authenticatedBy(key, secret).build();
			String reportTopic = "ASDC-DISTR-STATUS-TOPIC-PRODESOFER";
			createStatusTopicManager.allowProducer(reportTopic, clientKey1);

			CambriaBatchingPublisher createSimplePublisher = new PublisherBuilder().onTopic(reportTopic).usingHttps().usingHosts(servers).build();
			createSimplePublisher.setApiCredentials(clientKey1, clientSecret1);

			DistributionStatusNotification distributionStatusNotification = new DistributionStatusNotification();
			distributionStatusNotification.setStatus(DistributionStatusNotificationEnum.DEPLOY_OK);
			distributionStatusNotification.setArtifactURL("Ssssssss url");
			distributionStatusNotification.setDistributionID("idddddddddddddd");
			distributionStatusNotification.setTimestamp(System.currentTimeMillis());
			distributionStatusNotification.setConsumerID("my consumer id");

			Gson gson = new Gson();
			int result = createSimplePublisher.send(PARTITION_KEY, gson.toJson(distributionStatusNotification));

			List<message> messagesInQ = createSimplePublisher.close(20, TimeUnit.SECONDS);
			System.out.println(messagesInQ == null ? 0 : messagesInQ.size());

			// createTopicManager.createTopic(topicName, "my test topic", 1, 1);

			/*
			 * 
			 * { "secret": "OTHk2mcCSbskEtHhDw8h5oUa", "aux": { "email": "esofer@intl.sdc.com", "description": "test-keys" }, "key": "CGGoorrGPXPx2B1C" }
			 * 
			 * 
			 * { "secret": "FSlNJbmGWWBvBLJetQMYxPP6", "aux": { "email": "esofer@intl.sdc.com", "description": "test-keys" }, "key": "TAIEPO0aDU4VzM0G" }
			 * 
			 */

			String clientKey2 = "TAIEPO0aDU4VzM0G";

			CambriaConsumer createConsumer1 = new ConsumerBuilder().authenticatedBy("asdc1", "consumerId1").onTopic(topicName).usingHttps().usingHosts(servers).build();
			createConsumer1.setApiCredentials(clientKey1, "OTHk2mcCSbskEtHhDw8h5oUa");

			createTopicManager.allowConsumer(topicName, clientKey1);

			CambriaConsumer createConsumer2 = null;
			if (true) {
				createConsumer2 = new ConsumerBuilder().authenticatedBy("asdc2", "consumerId3").onTopic(topicName).usingHttps().usingHosts(servers).build();
				createConsumer2.setApiCredentials(clientKey2, "FSlNJbmGWWBvBLJetQMYxPP6");

				createTopicManager.allowConsumer(topicName, clientKey2);
			}

			createSimplePublisher = new PublisherBuilder().onTopic(topicName).usingHttps().usingHosts(servers).build();
			createSimplePublisher.setApiCredentials(key, secret);
			createTopicManager.allowProducer(topicName, key);

			createSimplePublisher.send("aaaa", "{ my testttttttttttttttt }");

			while (true) {

				Iterable<String> fetch1 = createConsumer1.fetch();

				Iterator<String> iterator1 = fetch1.iterator();
				while (iterator1.hasNext()) {
					System.out.println("***********************************************");
					System.out.println("client 1" + iterator1.next());
					System.out.println("***********************************************");
				}

				if (createConsumer2 != null) {
					Iterable<String> fetch2 = createConsumer2.fetch();

					Iterator<String> iterator2 = fetch2.iterator();
					while (iterator2.hasNext()) {
						System.out.println("***********************************************");
						System.out.println("client 2" + iterator2.next());
						System.out.println("***********************************************");
					}
				}
				Thread.sleep(1000 * 20);
			}

			// createTopicManager = CambriaClientFactory.createTopicManager(
			// servers, "8F3MDAtMSBwwpSMy", "gzFmsTxSCtO5RQfAccM6PqqX");

			// createTopicManager.deleteTopic("ASDC-DISTR-NOTIF-TOPIC-PROD");
			// createTopicManager.deleteTopic("ASDC-DISTR-NOTIF-TOPIC-PROD1");

			// CambriaIdentityManager createIdentityManager =
			// CambriaClientFactory.createIdentityManager(null, null, null);
			// createIdentityManager.setApiCredentials(arg0, arg1);
			// createIdentityManager.cl

			// String topicName = " ";
			// createTopicManager.createTopic(topicName,
			// "ASDC distribution notification topic", 1, 1);
			//
			// Thread.sleep(10 * 1000);
			//
			// for (int i = 0; i < 5; i++) {
			// try {
			// boolean openForProducing = createTopicManager
			// .isOpenForProducing(topicName);
			//
			// System.out.println("openForProducing=" + openForProducing);
			// createTopicManager.allowProducer(topicName,
			// "8F3MDAtMSBwwpSMy");
			// Set<String> allowedProducers = createTopicManager
			// .getAllowedProducers(topicName);
			// System.out.println(allowedProducers);
			//
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// }

			// createTopicManager.createTopic("", "", 0, 0);
			// createTopicManager.allowProducer(arg0, arg1);
			// createTopicManager.getTopics();
			// createTopicManager.close();
			// CambriaClientFactory.
			// CambriaBatchingPublisher createSimplePublisher =
			// CambriaClientFactory.createSimplePublisher("hostlist", "topic");

			// CambriaIdentityManager createIdentityManager =
			// CambriaClientFactory.createIdentityManager(null, "apiKey",
			// "apiSecret");
			// createIdentityManager.

		} catch (Exception e) {
			Log.debug("Exception in main test of Cambria Handler: {}", e.getMessage(), e);
			e.printStackTrace();
		} finally {
			if (createTopicManager != null) {
				createTopicManager.close();
			}
		}
	}

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

			createTopicManager = new TopicManagerBuilder().usingHttps().usingHosts(hostSet).build();

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
	 * @param errorMessage
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

		}

	}

	/**
	 * create a topic if it does not exists in the topicsList
	 * 
	 * @param hostSet
	 *            - list of U-EB servers
	 * @param apiKey
	 * @param secretKey
	 * @param topicsList
	 *            - list of exists topics
	 * @param topicName
	 *            - topic to create
	 * @param partitionCount
	 * @param replicationCount
	 * @return
	 */
	public CambriaErrorResponse createTopic(Collection<String> hostSet, String apiKey, String secretKey, String topicName, int partitionCount, int replicationCount) {

		CambriaTopicManager createTopicManager = null;
		try {

			createTopicManager = new TopicManagerBuilder().usingHttps().usingHosts(hostSet).authenticatedBy(apiKey, secretKey).build();

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
			createTopicManager = new TopicManagerBuilder().usingHttps().usingHosts(hostSet).authenticatedBy(managerApiKey, managerSecretKey).build();

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
			createTopicManager = new TopicManagerBuilder().usingHttps().usingHosts(hostSet).authenticatedBy(managerApiKey, managerSecretKey).build();

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

		CambriaConsumer consumer = new ConsumerBuilder().authenticatedBy(apiKey, secretKey).knownAs(consumerGroup, consumerId).onTopic(topicName).usingHttps().usingHosts(hostSet).withSocketTimeout(timeoutMS).build();
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

			createSimplePublisher = new PublisherBuilder().onTopic(topicName).usingHttps().usingHosts(uebServers).build();
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

			writeErrorToLog(cambriaErrorResponse, e.getMessage(), methodName, "send notification");

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

			createSimplePublisher = new PublisherBuilder().onTopic(topicName).usingHttps().usingHosts(uebServers).build();
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

			writeErrorToLog(response, e.getMessage(), methodName, "send notification");

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
				writeErrorToLog(response, "closing publisher returned non sent messages", methodName, "send notification");
			} else {
				logger.debug("No message left in the queue after closing cambria publisher");
				response = new CambriaErrorResponse(CambriaOperationStatus.OK, 200);
			}
		} catch (IOException | InterruptedException e) {
			logger.debug("Failed to close cambria publisher", e);
			response = new CambriaErrorResponse(CambriaOperationStatus.INTERNAL_SERVER_ERROR, 500);
			String methodName = new Object() {
			}.getClass().getEnclosingMethod().getName();
			writeErrorToLog(response, "closing publisher returned non sent messages", methodName, "send notification");
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
			createIdentityManager = new IdentityManagerBuilder().usingHttps().usingHosts(hostSet).build();
			createIdentityManager.getApiKey(apiKey);
			response = new CambriaErrorResponse(CambriaOperationStatus.OK, 200);

		} catch (HttpException | IOException | CambriaApiException | GeneralSecurityException e) {
			logger.debug("Failed to fetch api key {} from server ", apiKey, server, e);

			response = processError(e);

		}

		return response;
	}

}
