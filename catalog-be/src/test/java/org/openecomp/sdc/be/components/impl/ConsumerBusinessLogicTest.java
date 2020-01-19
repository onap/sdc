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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ConsumerDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ConsumerDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ConsumerOperation;
import org.openecomp.sdc.be.resources.data.ConsumerData;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

public class ConsumerBusinessLogicTest extends BaseBusinessLogicMock {

	private User user;
	private ConsumerDefinition consumer;
	private ConsumerDataDefinition consumerDataDefinition;

	@InjectMocks
	private ConsumerBusinessLogic consumerBusinessLogic;

	@Mock
	private ComponentsUtils componentsUtils;

	@Mock
	private UserBusinessLogic UserBusinessLogic;

	@Mock
	private IGraphLockOperation iGraphLockOperation;

	@Mock
	private ConsumerOperation consumerOperation;

	@Mock
	private ConsumerData consumerData;

	@Before
	public void setUp(){
		consumerBusinessLogic = new ConsumerBusinessLogic(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation,
			interfaceOperation, interfaceLifecycleTypeOperation, artifactToscaOperation);
		consumerDataDefinition = new ConsumerDataDefinition();
		consumer = new ConsumerDefinition();
		MockitoAnnotations.initMocks(this);
		user = new User("Stan", "Lee", "stan.lee",
				"stan.lee@marvel.com", "ADMIN", 1542024000L);
	}

	@Test
	public void testCreateConsumer_givenMissingUser_thenReturnsError() {
		User user = new User();
		ConsumerDefinition consumerDefinition = new ConsumerDefinition();
		Mockito.when(componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION))
				.thenReturn(new ResponseFormat());
		assertTrue(consumerBusinessLogic.createConsumer(user, consumerDefinition).isRight());
	}

	@Test
	public void testCreateConsumer_givenNonListedUser_thenReturnsError() {
		ConsumerDefinition consumerDefinition = new ConsumerDefinition();
		Mockito.when(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_ACCESS))
				.thenReturn(new ResponseFormat());
		Mockito.when(UserBusinessLogic.getUser(user.getUserId(), false)).thenThrow(new ByActionStatusComponentException(ActionStatus.RESTRICTED_OPERATION));
		assertTrue(consumerBusinessLogic.createConsumer(user, consumerDefinition).isRight());
	}

	@Test
	public void testCreateConsumer_givenNonAdminUser_thenReturnsError() {
		user.setRole("DESIGNER");
		Mockito.when(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION))
				.thenReturn(new ResponseFormat());
		Mockito.when(UserBusinessLogic.getUser(user.getUserId(), false)).thenReturn(user);
		assertTrue(consumerBusinessLogic.createConsumer(user, consumer).isRight());
	}

	@Test
	public void testCreateConsumer_givenInvalidConsumerNames_thenReturnsError() {
		Map<String, ActionStatus> invalidConsumerNames = new HashMap<>();
		invalidConsumerNames.put(null, ActionStatus.MISSING_DATA);
		invalidConsumerNames.put(".#()", ActionStatus.INVALID_CONTENT);
		invalidConsumerNames.put(RandomStringUtils.random(256, true, false), ActionStatus.EXCEEDS_LIMIT);
		for(Map.Entry<String, ActionStatus> e: invalidConsumerNames.entrySet()){
			consumer.setConsumerName(e.getKey());
			Mockito.when(UserBusinessLogic.getUser(user.getUserId(), false)).thenReturn(user);
			Mockito.when(componentsUtils.getResponseFormat(e.getValue(), "Consumer name"))
					.thenReturn(new ResponseFormat());
			assertTrue(consumerBusinessLogic.createConsumer(user, consumer).isRight());
		}
	}

	@Test
	public void testCreateConsumer_givenInvalidConsumerPasswords_thenReturnsError() {
		Map<String, ActionStatus> invalidPasswordResults = new HashMap<>();
		invalidPasswordResults.put(null, ActionStatus.MISSING_DATA);
		invalidPasswordResults.put(RandomStringUtils.random(64, '*' ), ActionStatus.INVALID_CONTENT_PARAM);
		invalidPasswordResults.put("password", ActionStatus.INVALID_LENGTH);
		for(Map.Entry<String, ActionStatus> e: invalidPasswordResults.entrySet()){
			consumer.setConsumerName("_marvel");
			consumer.setConsumerPassword(e.getKey());
			Mockito.when(UserBusinessLogic.getUser(user.getUserId(), false)).thenReturn(user);
			Mockito.when(componentsUtils.getResponseFormat(e.getValue(), "Consumer password"))
					.thenReturn(new ResponseFormat());
			assertTrue(consumerBusinessLogic.createConsumer(user, consumer).isRight());
		}
	}

	@Test
	public void testCreateConsumer_givenInvalidConsumerSalts_thenReturnsError() {
		consumer.setConsumerPassword(RandomStringUtils.random(64, true,true));
		Map<String, ActionStatus> invalidPasswordSalts = new HashMap<>();
		invalidPasswordSalts.put(null, ActionStatus.MISSING_DATA);
		invalidPasswordSalts.put(RandomStringUtils.random(32, "*" ), ActionStatus.INVALID_CONTENT_PARAM);
		invalidPasswordSalts.put("password", ActionStatus.INVALID_LENGTH);
		for(Map.Entry<String, ActionStatus> e: invalidPasswordSalts.entrySet()){
			consumer.setConsumerName("_marvel");
			consumer.setConsumerSalt(e.getKey());
			Mockito.when(UserBusinessLogic.getUser(user.getUserId(), false)).thenReturn(user);
			Mockito.when(componentsUtils.getResponseFormat(e.getValue(), "Consumer salt"))
					.thenReturn(new ResponseFormat());
			assertTrue(consumerBusinessLogic.createConsumer(user, consumer).isRight());
		}
	}

	@Test
	public void testCreateConsumer_givenConsumerNotLocked_thenReturnsError() {
		consumer.setConsumerName("_marvel");
		consumer.setConsumerPassword(RandomStringUtils.random(64, true,true));
		consumer.setConsumerSalt(RandomStringUtils.random(32, 'a'));
		Mockito.when(UserBusinessLogic.getUser(user.getUserId(), false)).thenReturn(user);
		Mockito.when(iGraphLockOperation.lockComponent(anyString(), any(NodeTypeEnum.class)))
				.thenReturn(StorageOperationStatus.GENERAL_ERROR);
		assertTrue(consumerBusinessLogic.createConsumer(user, consumer).isRight());
	}

	@Test
	public void testCreateConsumer_givenUnableToCreateCredentials_thenReturnsError() {
		ConsumerDataDefinition consumerDataDefinition = new ConsumerDataDefinition();
		consumerDataDefinition.setConsumerName("_marvel");
		consumerDataDefinition.setConsumerPassword(RandomStringUtils.random(64, true,true));
		consumerDataDefinition.setConsumerSalt(RandomStringUtils.random(32, 'a'));
		ConsumerDefinition consumer = new ConsumerDefinition(consumerDataDefinition);
		Mockito.when(UserBusinessLogic.getUser(user.getUserId(), false)).thenReturn(user);
		Mockito.when(iGraphLockOperation.lockComponent(anyString(), any(NodeTypeEnum.class)))
				.thenReturn(StorageOperationStatus.OK);
		Mockito.when(consumerOperation.getCredentials(anyString()))
				.thenReturn(Either.right(StorageOperationStatus.OK));
		Mockito.when(consumerOperation.createCredentials(any(ConsumerData.class)))
				.thenReturn(Either.right(StorageOperationStatus.USER_NOT_FOUND));
		assertTrue(consumerBusinessLogic.createConsumer(user, consumer).isRight());
	}

	@Test
	public void testCreateConsumer_givenValidUserAndConsumer_thenReturnsConsumer() {
		ConsumerDataDefinition consumerDataDefinition = new ConsumerDataDefinition();
		consumerDataDefinition.setConsumerName("_marvel");
		consumerDataDefinition.setConsumerPassword(RandomStringUtils.random(64, true,true));
		consumerDataDefinition.setConsumerSalt(RandomStringUtils.random(32, 'a'));
		ConsumerDefinition consumer = new ConsumerDefinition(consumerDataDefinition);
		Mockito.when(UserBusinessLogic.getUser(user.getUserId(), false)).thenReturn(user);
		Mockito.when(iGraphLockOperation.lockComponent(anyString(), any(NodeTypeEnum.class)))
				.thenReturn(StorageOperationStatus.OK);
		Mockito.when(consumerOperation.getCredentials(anyString()))
				.thenReturn(Either.right(StorageOperationStatus.OK));
		Mockito.when(consumerOperation.createCredentials(any(ConsumerData.class)))
				.thenReturn(Either.left(new ConsumerData(consumerDataDefinition)));
		assertTrue(consumerBusinessLogic.createConsumer(user, consumer).isLeft());
	}

	@Test
	public void testGetConsumer_givenNullUser_thenReturnsError() {
		Mockito.when(consumerOperation.getCredentials("marvel123"))
				.thenReturn(Either.right(StorageOperationStatus.USER_NOT_FOUND));
		assertTrue(consumerBusinessLogic.getConsumer("marvel123", null).isRight());
	}

	@Test
	public void testGetConsumer_givenValidUserAndConsumerId_thenReturnsConsumer() {
		Mockito.when(consumerOperation.getCredentials("marvel123"))
				.thenReturn(Either.left(new ConsumerData()));
		Mockito.when(consumerData.getConsumerDataDefinition())
				.thenReturn(consumerDataDefinition);
		Mockito.when(UserBusinessLogic.getUser(user.getUserId(), false)).thenReturn(user);
		assertTrue(consumerBusinessLogic.getConsumer("marvel123", user).isLeft());
	}

	@Test
	public void testUpdateConsumer_givenValidConsumer_thenReturnsUpdatedConsumer() {
		ConsumerDefinition updatedConsumer = new ConsumerDefinition(consumerDataDefinition);
		updatedConsumer.setConsumerName("marvel2");
		ConsumerDataDefinition updatedConsumerDataDef = new ConsumerDataDefinition();
		updatedConsumerDataDef.setConsumerName("marvel2");
		ConsumerData consumerData = new ConsumerData(updatedConsumerDataDef);
		Mockito.when(consumerOperation.updateCredentials(any(ConsumerData.class)))
				.thenReturn(Either.left(consumerData));
		assertEquals(updatedConsumer.getConsumerName(), consumerBusinessLogic.updateConsumer(consumer).left().value().getConsumerName());
	}

	@Test
	public void testUpdateConsumer_givenUpdateFailure_thenReturnsError() {
		Mockito.when(consumerOperation.updateCredentials(any(ConsumerData.class)))
				.thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
		assertTrue(consumerBusinessLogic.updateConsumer(consumer).isRight());
	}

	@Test
	public void testDeleteConsumer_givenValidUserAndConsumerId_thenReturnsSuccessful() {
		ConsumerData consumerData = new ConsumerData(new ConsumerDataDefinition());
		Mockito.when(UserBusinessLogic.getUser(user.getUserId(), false)).thenReturn(user);
		Mockito.when(consumerOperation.deleteCredentials("marvel123"))
				.thenReturn(Either.left(consumerData));
		assertTrue(consumerBusinessLogic.deleteConsumer("marvel123", user).isLeft());
	}

	@Test
	public void testDeleteConsumer_givenInvalidUser_thenReturnsError() {
		Mockito.when(UserBusinessLogic.getUser(user.getUserId(), false)).thenReturn(user);
		Mockito.when(consumerOperation.deleteCredentials("marvel123"))
				.thenReturn(Either.right(StorageOperationStatus.USER_NOT_FOUND));
		assertTrue(consumerBusinessLogic.deleteConsumer("marvel123", user).isRight());
	}
}