package org.openecomp.sdc.be.components.impl;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
import org.openecomp.sdc.be.user.IUserBusinessLogic;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;


public class ConsumerBusinessLogicTest {

	private User user;
	private ConsumerDefinition consumer;
	private ConsumerDataDefinition consumerDataDefinition;

	@InjectMocks
	private ConsumerBusinessLogic consumerBusinessLogic;

	@Mock
	private ComponentsUtils componentsUtils;

	@Mock
	private IUserBusinessLogic iUserBusinessLogic;

	@Mock
	private IGraphLockOperation iGraphLockOperation;

	@Mock
	private ConsumerOperation consumerOperation;

	@Mock ConsumerData consumerData;

	@Before
	public void setUp(){
		consumerBusinessLogic = new ConsumerBusinessLogic();
		consumerDataDefinition = new ConsumerDataDefinition();
		consumer = new ConsumerDefinition();
		MockitoAnnotations.initMocks(this);
		user = new User("Stan", "Lee", "stan.lee", "stan.lee@marvel.com", "ADMIN",
				1542024000L);
	}

	@Test
	public void testCreateConsumer_givenMissingUser_thenReturnsError() throws Exception {
		Mockito.when(componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION)).thenReturn(new ResponseFormat());
		User user = new User();
		ConsumerDefinition consumerDefinition = new ConsumerDefinition();
		Assert.assertTrue(consumerBusinessLogic.createConsumer(user, consumerDefinition).isRight());
	}

	@Test
	public void testCreateConsumer_givenNonListedUser_thenReturnsError() throws Exception {
		Mockito.when(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_ACCESS)).thenReturn(new ResponseFormat());
		Mockito.when(iUserBusinessLogic.getUser(user.getUserId(), false)).thenReturn(Either.right(ActionStatus.RESTRICTED_OPERATION));
		ConsumerDefinition consumerDefinition = new ConsumerDefinition();
		Assert.assertTrue(consumerBusinessLogic.createConsumer(user, consumerDefinition).isRight());
	}

	@Test
	public void testCreateConsumer_givenNonAdminUser_thenReturnsError() throws Exception {
		user.setRole("DESIGNER");
		Mockito.when(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION)).thenReturn(new ResponseFormat());
		Mockito.when(iUserBusinessLogic.getUser(user.getUserId(), false)).thenReturn(Either.left(user));
		Assert.assertTrue(consumerBusinessLogic.createConsumer(user, consumer).isRight());
	}

	@Test
	public void testCreateConsumer_givenInvalidConsumerNames_thenReturnsError() throws Exception {
		Map<String, ActionStatus> invalidConsumerNames = new HashMap<>();
		invalidConsumerNames.put(null, ActionStatus.MISSING_DATA);
		invalidConsumerNames.put(".#()", ActionStatus.INVALID_CONTENT);
		invalidConsumerNames.put(RandomStringUtils.random(256, true, false), ActionStatus.EXCEEDS_LIMIT);
		for(Map.Entry<String, ActionStatus> e: invalidConsumerNames.entrySet()){
			consumer.setConsumerName(e.getKey());
			Mockito.when(iUserBusinessLogic.getUser(user.getUserId(), false)).thenReturn(Either.left(user));
			Mockito.when(componentsUtils.getResponseFormat(e.getValue(), "Consumer name")).thenReturn(new ResponseFormat());
			Assert.assertTrue(consumerBusinessLogic.createConsumer(user, consumer).isRight());
		}
	}

	@Test
	public void testCreateConsumer_givenInvalidConsumerPasswords_thenReturnsError() throws Exception {
		Map<String, ActionStatus> invalidPasswordResults = new HashMap<>();
		invalidPasswordResults.put(null, ActionStatus.MISSING_DATA);
		invalidPasswordResults.put(RandomStringUtils.random(64, '*' ), ActionStatus.INVALID_CONTENT_PARAM);
		invalidPasswordResults.put("password", ActionStatus.INVALID_LENGTH);
		for(Map.Entry<String, ActionStatus> e: invalidPasswordResults.entrySet()){
			consumer.setConsumerName("_marvel");
			consumer.setConsumerPassword(e.getKey());
			Mockito.when(iUserBusinessLogic.getUser(user.getUserId(), false)).thenReturn(Either.left(user));
			Mockito.when(componentsUtils.getResponseFormat(e.getValue(), "Consumer password")).thenReturn(new ResponseFormat());
			Assert.assertTrue(consumerBusinessLogic.createConsumer(user, consumer).isRight());
		}

	}

	@Test
	public void testCreateConsumer_givenInvalidConsumerSalts_thenReturnsError() throws Exception {
		consumer.setConsumerPassword(RandomStringUtils.random(64, true,true));
		Map<String, ActionStatus> invalidPasswordSalts = new HashMap<>();
		invalidPasswordSalts.put(null, ActionStatus.MISSING_DATA);
		invalidPasswordSalts.put(RandomStringUtils.random(32, "*" ), ActionStatus.INVALID_CONTENT_PARAM);
		invalidPasswordSalts.put("password", ActionStatus.INVALID_LENGTH);
		for(Map.Entry<String, ActionStatus> e: invalidPasswordSalts.entrySet()){
			consumer.setConsumerName("_marvel");
			consumer.setConsumerSalt(e.getKey());
			Mockito.when(iUserBusinessLogic.getUser(user.getUserId(), false)).thenReturn(Either.left(user));
			Mockito.when(componentsUtils.getResponseFormat(e.getValue(), "Consumer salt")).thenReturn(new ResponseFormat());
			Assert.assertTrue(consumerBusinessLogic.createConsumer(user, consumer).isRight());
		}
	}

	@Test
	public void testCreateConsumer_givenConsumerNotLocked_thenReturnsError() throws Exception {
		consumer.setConsumerName("_marvel");
		consumer.setConsumerPassword(RandomStringUtils.random(64, true,true));
		consumer.setConsumerSalt(RandomStringUtils.random(32, 'a'));
		Mockito.when(iUserBusinessLogic.getUser(user.getUserId(), false)).thenReturn(Either.left(user));
		Mockito.when(iGraphLockOperation.lockComponent(anyString(), any(NodeTypeEnum.class))).thenReturn(StorageOperationStatus.GENERAL_ERROR);
		Assert.assertTrue(consumerBusinessLogic.createConsumer(user, consumer).isRight());
	}

	@Test
	public void testCreateConsumer_givenUnableToCreateCredentials_thenReturnsError() throws Exception {
		ConsumerDataDefinition consumerDataDefinition = new ConsumerDataDefinition();
		consumerDataDefinition.setConsumerName("_marvel");
		consumerDataDefinition.setConsumerPassword(RandomStringUtils.random(64, true,true));
		consumerDataDefinition.setConsumerSalt(RandomStringUtils.random(32, 'a'));
		ConsumerDefinition consumer = new ConsumerDefinition(consumerDataDefinition);
		Mockito.when(iUserBusinessLogic.getUser(user.getUserId(), false)).thenReturn(Either.left(user));
		Mockito.when(iGraphLockOperation.lockComponent(anyString(), any(NodeTypeEnum.class))).thenReturn(StorageOperationStatus.OK);
		Mockito.when(consumerOperation.getCredentials(anyString())).thenReturn(Either.right(StorageOperationStatus.OK));
		Mockito.when(consumerOperation.createCredentials(any(ConsumerData.class))).thenReturn(Either.right(StorageOperationStatus.USER_NOT_FOUND));
		Assert.assertTrue(consumerBusinessLogic.createConsumer(user, consumer).isRight());
	}

	@Test
	public void testCreateConsumer_givenValidUserAndConsumer_thenReturnsConsumer() throws Exception {
		ConsumerDataDefinition consumerDataDefinition = new ConsumerDataDefinition();
		consumerDataDefinition.setConsumerName("_marvel");
		consumerDataDefinition.setConsumerPassword(RandomStringUtils.random(64, true,true));
		consumerDataDefinition.setConsumerSalt(RandomStringUtils.random(32, 'a'));
		ConsumerDefinition consumer = new ConsumerDefinition(consumerDataDefinition);
		Mockito.when(iUserBusinessLogic.getUser(user.getUserId(), false)).thenReturn(Either.left(user));
		Mockito.when(iGraphLockOperation.lockComponent(anyString(), any(NodeTypeEnum.class))).thenReturn(StorageOperationStatus.OK);
		Mockito.when(consumerOperation.getCredentials(anyString())).thenReturn(Either.right(StorageOperationStatus.OK));
		Mockito.when(consumerOperation.createCredentials(any(ConsumerData.class))).thenReturn(Either.left(new ConsumerData(consumerDataDefinition)));
		Assert.assertTrue(consumerBusinessLogic.createConsumer(user, consumer).isLeft());
	}

	@Test
	public void testGetConsumer_givenNullUser_thenReturnsError() throws Exception {
		Mockito.when(consumerOperation.getCredentials("marvel123")).thenReturn(Either.right(StorageOperationStatus.USER_NOT_FOUND));
		Assert.assertTrue(consumerBusinessLogic.getConsumer("marvel123", null).isRight());
	}

	@Test
	public void testGetConsumer_givenValidUserAndConsumerId_thenReturnsConsumer() throws Exception {
		Mockito.when(consumerOperation.getCredentials("marvel123")).thenReturn(Either.left(new ConsumerData()));
		Mockito.when(consumerData.getConsumerDataDefinition()).thenReturn(consumerDataDefinition);
		Mockito.when(iUserBusinessLogic.getUser(user.getUserId(), false)).thenReturn(Either.left(user));
		Assert.assertTrue(consumerBusinessLogic.getConsumer("marvel123", user).isLeft());
	}

	@Test
	public void testUpdateConsumer_givenValidConsumer_thenReturnsUpdatedConsumer() throws Exception{
		ConsumerDefinition updatedConsumer = new ConsumerDefinition(consumerDataDefinition);
		updatedConsumer.setConsumerName("marvel2");
		ConsumerDataDefinition updatedConsumerDataDef = new ConsumerDataDefinition();
		updatedConsumerDataDef.setConsumerName("marvel2");
		ConsumerData consumerData = new ConsumerData(updatedConsumerDataDef);
		Mockito.when(consumerOperation.updateCredentials(any(ConsumerData.class))).thenReturn(Either.left(consumerData));
		Assert.assertEquals(updatedConsumer.getConsumerName(), consumerBusinessLogic.updateConsumer(consumer).left().value().getConsumerName());
	}

	@Test
	public void testUpdateConsumer_givenUpdateFailure_thenReturnsError() throws Exception {
		Mockito.when(consumerOperation.updateCredentials(any(ConsumerData.class))).thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
		Assert.assertTrue(consumerBusinessLogic.updateConsumer(consumer).isRight());
	}

	@Test
	public void testDeleteConsumer_givenValidUserAndConsumerId_thenReturnsSuccessful() throws Exception {
		ConsumerData consumerData = new ConsumerData(new ConsumerDataDefinition());
		Mockito.when(iUserBusinessLogic.getUser(user.getUserId(), false)).thenReturn(Either.left(user));
		Mockito.when(consumerOperation.deleteCredentials("marvel123")).thenReturn(Either.left(consumerData));
		Assert.assertTrue(consumerBusinessLogic.deleteConsumer("marvel123", user).isLeft());
	}

	@Test
	public void testDeleteConsumer_givenInvalidUser_thenReturnsError() throws Exception {
		Mockito.when(iUserBusinessLogic.getUser(user.getUserId(), false)).thenReturn(Either.left(user));
		Mockito.when(consumerOperation.deleteCredentials("marvel123")).thenReturn(Either.right(StorageOperationStatus.USER_NOT_FOUND));
		Assert.assertTrue(consumerBusinessLogic.deleteConsumer("marvel123", user).isRight());

	}

}