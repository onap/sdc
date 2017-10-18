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

package org.openecomp.sdc.be.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.ElementOperationMock;
import org.openecomp.sdc.be.auditing.api.IAuditingManager;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CacheMangerOperation;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.resources.data.auditing.DistributionDeployEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionNotificationEvent;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import fj.data.Either;

public class ServiceBusinessLogicTest {

	private static Logger log = LoggerFactory.getLogger(ServiceBusinessLogicTest.class.getName());
	private static final String SERVICE_CATEGORY = "Mobility";
	final ServletContext servletContext = Mockito.mock(ServletContext.class);
	IAuditingManager iAuditingManager = null;
	UserBusinessLogic mockUserAdmin = Mockito.mock(UserBusinessLogic.class);
	WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
	WebApplicationContext webAppContext = Mockito.mock(WebApplicationContext.class);
	ServiceBusinessLogic bl = new ServiceBusinessLogic();
	ResponseFormatManager responseManager = null;
	IElementOperation mockElementDao;
	AuditingManager auditingManager = Mockito.mock(AuditingManager.class);
	ComponentsUtils componentsUtils = new ComponentsUtils();
	AuditCassandraDao auditingDao = Mockito.mock(AuditCassandraDao.class);
	ArtifactsBusinessLogic artifactBl = Mockito.mock(ArtifactsBusinessLogic.class);
	GraphLockOperation graphLockOperation = Mockito.mock(GraphLockOperation.class);
	TitanDao mockTitanDao = Mockito.mock(TitanDao.class);
	ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
	CacheMangerOperation cacheManager = Mockito.mock(CacheMangerOperation.class);

	User user = null;
	Service serviceResponse = null;
	Resource genericService = null;
	
	private static final String CERTIFIED_VERSION = "1.0";
	private static final String UNCERTIFIED_VERSION = "0.2";
	private static final String COMPONNET_ID = "myUniqueId";
	private static final String GENERIC_SERVICE_NAME = "org.openecomp.resource.abstract.nodes.service";
	private static Map<AuditingFieldsKeysEnum, Object> FILTER_MAP_CERTIFIED_VERSION = new HashMap<AuditingFieldsKeysEnum, Object>();
	private static Map<AuditingFieldsKeysEnum, Object> FILTER_MAP_UNCERTIFIED_VERSION_CURR = new HashMap<AuditingFieldsKeysEnum, Object>();
	private static Map<AuditingFieldsKeysEnum, Object> FILTER_MAP_UNCERTIFIED_VERSION_PREV = new HashMap<AuditingFieldsKeysEnum, Object>();

	public ServiceBusinessLogicTest() {

	}

	@Before
	public void setup() {

		ExternalConfiguration.setAppName("catalog-be");
		// Init Configuration
		String appConfigDir = "src/test/resources/config/catalog-be";
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
		ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
	
		// Elements
		mockElementDao = new ElementOperationMock();

		// User data and management
		user = new User();
		user.setUserId("jh0003");
		user.setFirstName("Jimmi");
		user.setLastName("Hendrix");
		user.setRole(Role.ADMIN.name());

		Either<User, ActionStatus> eitherGetUser = Either.left(user);
		when(mockUserAdmin.getUser("jh0003", false)).thenReturn(eitherGetUser);

		// Servlet Context attributes
		when(servletContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).thenReturn(configurationManager);
//		when(servletContext.getAttribute(Constants.SERVICE_OPERATION_MANAGER)).thenReturn(new ServiceOperation());
		when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
		when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webAppContext);
		when(webAppContext.getBean(IElementOperation.class)).thenReturn(mockElementDao);
		when(graphLockOperation.lockComponent(Mockito.anyString(), Mockito.eq(NodeTypeEnum.Service))).thenReturn(StorageOperationStatus.OK);
		when(graphLockOperation.lockComponentByName(Mockito.anyString(), Mockito.eq(NodeTypeEnum.Service))).thenReturn(StorageOperationStatus.OK);

		// artifact bussinesslogic
		ArtifactDefinition artifactDef = new ArtifactDefinition();
		when(artifactBl.createArtifactPlaceHolderInfo(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap(), Mockito.any(User.class), Mockito.any(ArtifactGroupTypeEnum.class))).thenReturn(artifactDef);

		// createService
		serviceResponse = createServiceObject(true);
		Either<Component, StorageOperationStatus> eitherCreate = Either.left(serviceResponse);
		when(toscaOperationFacade.createToscaComponent(Mockito.any(Component.class))).thenReturn(eitherCreate);
		Either<Boolean, StorageOperationStatus> eitherCount = Either.left(false);
		when(toscaOperationFacade.validateComponentNameExists("Service", null, ComponentTypeEnum.SERVICE)).thenReturn(eitherCount);
		Either<Boolean, StorageOperationStatus> eitherCountExist = Either.left(true);
		when(toscaOperationFacade.validateComponentNameExists("alreadyExist", null, ComponentTypeEnum.SERVICE)).thenReturn(eitherCountExist);

		genericService = setupGenericServiceMock();
		Either<Resource, StorageOperationStatus> findLatestGeneric = Either.left(genericService);
		when(toscaOperationFacade.getLatestCertifiedNodeTypeByToscaResourceName(GENERIC_SERVICE_NAME)).thenReturn(findLatestGeneric);
		
		
		bl = new ServiceBusinessLogic();
		bl.setElementDao(mockElementDao);
		bl.setUserAdmin(mockUserAdmin);
		bl.setArtifactBl(artifactBl);
		bl.setGraphLockOperation(graphLockOperation);
		bl.setTitanGenericDao(mockTitanDao);
		bl.setToscaOperationFacade(toscaOperationFacade);
		
		componentsUtils.Init();
		componentsUtils.setAuditingManager(auditingManager);
		bl.setComponentsUtils(componentsUtils);
		bl.setCassandraAuditingDao(auditingDao);
		bl.setCacheManagerOperation(cacheManager);

		mockAuditingDaoLogic();

		responseManager = ResponseFormatManager.getInstance();

	}

	@Test
	public void testGetComponentAuditRecordsCertifiedVersion() {
		Either<List<Map<String, Object>>, ResponseFormat> componentAuditRecords = bl.getComponentAuditRecords(CERTIFIED_VERSION, COMPONNET_ID, user.getUserId());
		assertTrue(componentAuditRecords.isLeft());
		int size = componentAuditRecords.left().value().size();
		assertTrue(size == 3);
	}

	@Test
	public void testGetComponentAuditRecordsUnCertifiedVersion() {
		Either<List<Map<String, Object>>, ResponseFormat> componentAuditRecords = bl.getComponentAuditRecords(UNCERTIFIED_VERSION, COMPONNET_ID, user.getUserId());
		assertTrue(componentAuditRecords.isLeft());
		int size = componentAuditRecords.left().value().size();
		assertTrue(size == 1);
	}

	@Test
	public void testHappyScenario() {
		Service service = createServiceObject(false);
		Either<Service, ResponseFormat> createResponse = bl.createService(service, user);

		if (createResponse.isRight()) {
			assertEquals(new Integer(200), createResponse.right().value().getStatus());
		}
		assertEqualsServiceObject(createServiceObject(true), createResponse.left().value());
	}

	private void assertEqualsServiceObject(Service origService, Service newService) {
		assertEquals(origService.getContactId(), newService.getContactId());
		assertEquals(origService.getCategories(), newService.getCategories());
		assertEquals(origService.getCreatorUserId(), newService.getCreatorUserId());
		assertEquals(origService.getCreatorFullName(), newService.getCreatorFullName());
		assertEquals(origService.getDescription(), newService.getDescription());
		assertEquals(origService.getIcon(), newService.getIcon());
		assertEquals(origService.getLastUpdaterUserId(), newService.getLastUpdaterUserId());
		assertEquals(origService.getLastUpdaterFullName(), newService.getLastUpdaterFullName());
		assertEquals(origService.getName(), newService.getName());
		assertEquals(origService.getName(), newService.getName());
		assertEquals(origService.getUniqueId(), newService.getUniqueId());
		assertEquals(origService.getVersion(), newService.getVersion());
		assertEquals(origService.getArtifacts(), newService.getArtifacts());
		assertEquals(origService.getCreationDate(), newService.getCreationDate());
		assertEquals(origService.getLastUpdateDate(), newService.getLastUpdateDate());
		assertEquals(origService.getLifecycleState(), newService.getLifecycleState());
		assertEquals(origService.getTags(), newService.getTags());
	}

	private void assertResponse(Either<Service, ResponseFormat> createResponse, ActionStatus expectedStatus, String... variables) {
		ResponseFormat expectedResponse = responseManager.getResponseFormat(expectedStatus, variables);
		ResponseFormat actualResponse = createResponse.right().value();
		assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		assertEquals("assert error description", expectedResponse.getFormattedMessage(), actualResponse.getFormattedMessage());
	}

	/* CREATE validations - start ***********************/
	// Service name - start

	@Test
	public void testFailedServiceValidations() {
		testServiceNameAlreadyExists();
		testServiceNameEmpty();
		// testServiceNameExceedsLimit();
		testServiceNameWrongFormat();
		testServiceDescriptionEmpty();
		testServiceDescriptionMissing();
		testServiceDescExceedsLimitCreate();
		testServiceDescNotEnglish();
		testServiceIconEmpty();
		testServiceIconMissing();
		testResourceIconInvalid();
		testResourceIconExceedsLimit();
		// testTagsExceedsLimitCreate();
		// testTagsSingleExcessLimit();
		testTagsNoServiceName();
		testInvalidTag();
		testServiceTagNotExist();
		testServiceTagEmpty();
		
		testContactIdTooLong();
		testContactIdWrongFormatCreate();
		testInvalidProjectCode();
		testProjectCodeTooLong();
		testProjectCodeTooShort();
		
		testResourceContactIdMissing();
		testServiceCategoryExist();
		testServiceBadCategoryCreate();
		testMissingProjectCode();
	}

	private void testServiceNameAlreadyExists() {
		String serviceName = "alreadyExist";
		Service serviceExccedsNameLimit = createServiceObject(false);
		// 51 chars, the limit is 50
		serviceExccedsNameLimit.setName(serviceName);
		List<String> tgs = new ArrayList<String>();
		tgs.add(serviceName);
		serviceExccedsNameLimit.setTags(tgs);

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceExccedsNameLimit, user);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_NAME_ALREADY_EXIST, ComponentTypeEnum.SERVICE.getValue(), serviceName);
	}

	private void testServiceNameEmpty() {
		Service serviceExccedsNameLimit = createServiceObject(false);
		serviceExccedsNameLimit.setName(null);

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceExccedsNameLimit, user);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.MISSING_COMPONENT_NAME, ComponentTypeEnum.SERVICE.getValue());
	}

	private void testServiceNameExceedsLimit() {
		Service serviceExccedsNameLimit = createServiceObject(false);
		// 51 chars, the limit is 50
		String tooLongServiceName = "h1KSyJh9EspI8SPwAGu4VETfqWejeanuB1PCJBxdsafefegesse";
		serviceExccedsNameLimit.setName(tooLongServiceName);

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceExccedsNameLimit, user);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_NAME_EXCEEDS_LIMIT, ComponentTypeEnum.SERVICE.getValue(), "" + ValidationUtils.COMPONENT_NAME_MAX_LENGTH);
	}

	private void testServiceNameWrongFormat() {
		Service service = createServiceObject(false);
		// contains :
		String nameWrongFormat = "ljg\fd";
		service.setName(nameWrongFormat);

		Either<Service, ResponseFormat> createResponse = bl.createService(service, user);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.INVALID_COMPONENT_NAME, ComponentTypeEnum.SERVICE.getValue());
	}

	// Service name - end
	// Service description - start
	private void testServiceDescriptionEmpty() {
		Service serviceExist = createServiceObject(false);
		serviceExist.setDescription("");

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceExist, user);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_DESCRIPTION, ComponentTypeEnum.SERVICE.getValue());
	}

	private void testServiceDescriptionMissing() {
		Service serviceExist = createServiceObject(false);
		serviceExist.setDescription(null);

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceExist, user);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_DESCRIPTION, ComponentTypeEnum.SERVICE.getValue());
	}

	private void testServiceDescExceedsLimitCreate() {
		Service serviceExccedsDescLimit = createServiceObject(false);
		// 1025 chars, the limit is 1024
		String tooLongServiceDesc = "1GUODojQ0sGzKR4NP7e5j82ADQ3KHTVOaezL95qcbuaqDtjZhAQGQ3iFwKAy580K4WiiXs3u3zq7RzXcSASl5fm0RsWtCMOIDP"
				+ "AOf9Tf2xtXxPCuCIMCR5wOGnNTaFxgnJEHAGxilBhZDgeMNHmCN1rMK5B5IRJOnZxcpcL1NeG3APTCIMP1lNAxngYulDm9heFSBc8TfXAADq7703AvkJT0QPpGq2z2P"
				+ "tlikcAnIjmWgfC5Tm7UH462BAlTyHg4ExnPPL4AO8c92VrD7kZSgSqiy73cN3gLT8uigkKrUgXQFGVUFrXVyyQXYtVM6bLBeuCGQf4C2j8lkNg6M0J3PC0PzMRoinOxk"
				+ "Ae2teeCtVcIj4A1KQo3210j8q2v7qQU69Mabsa6DT9FgE4rcrbiFWrg0Zto4SXWD3o1eJA9o29lTg6kxtklH3TuZTmpi5KVp1NFhS1RpnqF83tzv4mZLKsx7Zh1fEgYvRFwx1"
				+ "ar3RolyDfNoZiGBGTMsZzz7RPFBf2hTnLmNqVGQnHKhhGj0Y5s8t2cbqbO2nmHiJb9uaUVrCGypgbAcJL3KPOBfAVW8PcpmNj4yVjI3L4x5zHjmGZbp9vKshEQODcrmcgsYAoKqe"
				+ "uu5u7jk8XVxEfQ0m5qL8UOErXPlJovSmKUmP5B5T0w299zIWDYCzSoNasHpHjOMDLAiDDeHbozUOn9t3Qou00e9POq4RMM0VnIx1H38nJoJZz2XH8CI5YMQe7oTagaxgQTF2aa0qaq2"
				+ "V6nJsfRGRklGjNhFFYP2cS4Xv2IJO9DSX6LTXOmENrGVJJvMOZcvnBaZPfoAHN0LU4i1SoepLzulIxnZBfkUWFJgZ5wQ0Bco2GC1HMqzW21rwy4XHRxXpXbmW8LVyoA1KbnmVmROycU4"
				+ "scTZ62IxIcIWCVeMjBIcTviXULbPUyqlfEPXWr8IMJtpAaELWgyquPClAREMDs2b9ztKmUeXlMccFES1XWbFTrhBHhmmDyVReEgCwfokrUFR13LTUK1k8I6OEHOs";

		serviceExccedsDescLimit.setDescription(tooLongServiceDesc);

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceExccedsDescLimit, user);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_DESCRIPTION_EXCEEDS_LIMIT, ComponentTypeEnum.SERVICE.getValue(), "" + ValidationUtils.COMPONENT_DESCRIPTION_MAX_LENGTH);
	}

	private void testServiceDescNotEnglish() {
		Service notEnglish = createServiceObject(false);
		// Not english
		String tooLongServiceDesc = "\uC2B5";
		notEnglish.setDescription(tooLongServiceDesc);

		Either<Service, ResponseFormat> createResponse = bl.createService(notEnglish, user);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_DESCRIPTION, ComponentTypeEnum.SERVICE.getValue());
	}

	// Service description - stop
	// Service icon - start
	private void testServiceIconEmpty() {
		Service serviceExist = createServiceObject(false);
		serviceExist.setIcon("");

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceExist, user);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_ICON, ComponentTypeEnum.SERVICE.getValue());
	}

	private void testServiceIconMissing() {
		Service serviceExist = createServiceObject(false);
		serviceExist.setIcon(null);

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceExist, user);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_ICON, ComponentTypeEnum.SERVICE.getValue());
	}

	private void testResourceIconInvalid() {
		Service resourceExist = createServiceObject(false);
		resourceExist.setIcon("kjk3453^&");

		Either<Service, ResponseFormat> createResponse = bl.createService(resourceExist, user);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_ICON, ComponentTypeEnum.SERVICE.getValue());
	}

	private void testResourceIconExceedsLimit() {
		Service resourceExist = createServiceObject(false);
		resourceExist.setIcon("dsjfhskdfhskjdhfskjdhkjdhfkshdfksjsdkfhsdfsdfsdfsfsdfsf");

		Either<Service, ResponseFormat> createResponse = bl.createService(resourceExist, user);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_ICON_EXCEEDS_LIMIT, ComponentTypeEnum.SERVICE.getValue(), "" + ValidationUtils.ICON_MAX_LENGTH);
	}

	// Service icon - stop
	// Service tags - start
	private void testTagsExceedsLimitCreate() {
		Service serviceExccedsNameLimit = createServiceObject(false);
		String tag1 = "I63llMSEF12FntTwpMt64JhopkjQZzv5KS7mBoRku42PYLrBjQ";
		String tag2 = "I63llMSEF12FntTwpMt64JhopkjQZzv5KS7mBoRku42PYLrBjW";
		String tag3 = "I63llMSEF12FntTwpMt64JhopkjQZzv5KS7mBoRku42PYLrBjE";
		String tag4 = "I63llMSEF12FntTwpMt64JhopkjQZzv5KS7mBoRku42PYLrBjb";
		String tag5 = "I63llMSEF12FntTwpMt64JhopkjQZzv5KS7mBoRku42PYLrBjr";
		String tag6 = "I63llMSEF12FntTwpMt64JhopkjQZzv5KS7mBoRku42PYLrBjf";
		String tag7 = "I63llMSEF12FntTwpMt64JhopkjQZzv5KS7mBoRku42PYLrBjg";
		String tag8 = "I63llMSEF12FntTwpMt64JhopkjQZzv5KS7mBoRku42PYLrBjd";
		String tag9 = "I63llMSEF12FntTwpMt64JhopkjQZzv5KS7mBoRku42PYLrBjf";
		String tag10 = "I63llMSEF12FntTwpMt64JhopkjQZzv5KS7mBoRku42PYLrBjg";
		String tag11 = "I63llMSEF12FntTwpMt64JhopkjQZzv5KS7mBoRku42PYLrBjh";
		String tag12 = "I63llMSEF12FntTwpMt64JhopkjQZzv5KS7mBoRku42PYLrBjj";
		String tag13 = "I63llMSEF12FntTwpMt64JhopkjQZzv5KS7mBoRku42PYLrBjk";
		String tag14 = "I63llMSEF12FntTwpMt64JhopkjQZzv5KS7mBoRku42PYLrBjs";
		String tag15 = "I63llMSEF12FntTwpMt64JhopkjQZzv5KS7mBoRku42PYLrBjz";
		String tag16 = "I63llMSEF12FntTwpMt64JhopkjQZzv5KS7mBoRku42PYLrBjx";
		String tag17 = "I63llMSEF12FntTwpMt64JhopkjQZzv5KS7mBoRku42PYLrBj2";
		String tag18 = "I63llMSEF12FntTwpMt64JhopkjQZzv5KS7mBoRku42PYLrBj3";
		String tag19 = "I63llMSEF12FntTwpMt64JhopkjQZzv5KS7mBoRku42PYLrBj4";
		String tag20 = "I63llMSEF12FntTwpMt64JhopkjQZzv5KS7mBoRku42PYLrBj5";
		String tag21 = "I63llMSEF12FntTwpMt64JhopkjQZzv5KS7mBoRku42PYLrBj0";

		List<String> tagsList = new ArrayList<String>();
		tagsList.add(tag1);
		tagsList.add(tag2);
		tagsList.add(tag3);
		tagsList.add(tag4);
		tagsList.add(tag5);
		tagsList.add(tag6);
		tagsList.add(tag7);
		tagsList.add(tag8);
		tagsList.add(tag9);
		tagsList.add(tag10);
		tagsList.add(tag11);
		tagsList.add(tag12);
		tagsList.add(tag13);
		tagsList.add(tag14);
		tagsList.add(tag15);
		tagsList.add(tag16);
		tagsList.add(tag17);
		tagsList.add(tag18);
		tagsList.add(tag19);
		tagsList.add(tag20);
		tagsList.add(tag21);
		tagsList.add(serviceExccedsNameLimit.getName());

		serviceExccedsNameLimit.setTags(tagsList);

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceExccedsNameLimit, user);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_TAGS_EXCEED_LIMIT, "" + ValidationUtils.TAG_LIST_MAX_LENGTH);

	}

	private void testTagsSingleExcessLimit() {
		Service serviceExccedsNameLimit = createServiceObject(false);
		String tag1 = "afzs2qLBb5X6tZhiunkcEwiFX1qRQY8YZl3y3Du5M5xeQY5Nq9a";
		String tag2 = serviceExccedsNameLimit.getName();
		List<String> tagsList = new ArrayList<String>();
		tagsList.add(tag1);
		tagsList.add(tag2);
		serviceExccedsNameLimit.setTags(tagsList);

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceExccedsNameLimit, user);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_SINGLE_TAG_EXCEED_LIMIT, "" + ValidationUtils.TAG_MAX_LENGTH);

	}

	private void testTagsNoServiceName() {
		Service serviceExccedsNameLimit = createServiceObject(false);
		String tag1 = "afzs2qLBb";
		List<String> tagsList = new ArrayList<String>();
		tagsList.add(tag1);
		serviceExccedsNameLimit.setTags(tagsList);

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceExccedsNameLimit, user);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_TAGS_NO_COMP_NAME);

	}

	private void testInvalidTag() {
		Service serviceExccedsNameLimit = createServiceObject(false);
		String tag1 = "afzs2qLBb%#%";
		List<String> tagsList = new ArrayList<String>();
		tagsList.add(tag1);
		serviceExccedsNameLimit.setTags(tagsList);

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceExccedsNameLimit, user);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.INVALID_FIELD_FORMAT, new String[] { "Service", "tag" });

	}

	private void testServiceTagNotExist() {
		Service serviceExist = createServiceObject(false);
		serviceExist.setTags(null);

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceExist, user);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_TAGS);
	}

	private void testServiceTagEmpty() {
		Service serviceExist = createServiceObject(false);
		serviceExist.setTags(new ArrayList<String>());

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceExist, user);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_TAGS);
	}

	// Service tags - stop
	// Service contactId - start
	private void testContactIdTooLong() {
		Service serviceContactId = createServiceObject(false);
		// 59 chars instead of 50
		String contactIdTooLong = "thisNameIsVeryLongAndExeccedsTheNormalLengthForContactId";
		serviceContactId.setContactId(contactIdTooLong);

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceContactId, user);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_CONTACT, ComponentTypeEnum.SERVICE.getValue());
	}

	private void testContactIdWrongFormatCreate() {
		Service serviceContactId = createServiceObject(false);
		// 3 letters and 3 digits and special characters
		String contactIdTooLong = "yrt134!!!";
		serviceContactId.setContactId(contactIdTooLong);

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceContactId, user);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_CONTACT, ComponentTypeEnum.SERVICE.getValue());
	}

	private void testResourceContactIdMissing() {
		Service resourceExist = createServiceObject(false);
		resourceExist.setContactId(null);

		Either<Service, ResponseFormat> createResponse = bl.createService(resourceExist, user);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_CONTACT, ComponentTypeEnum.SERVICE.getValue());
	}

	// Service contactId - stop
	// Service category - start
	private void testServiceCategoryExist() {
		Service serviceExist = createServiceObject(false);
		serviceExist.setCategories(null);

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceExist, user);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.SERVICE.getValue());
	}

	public void markDistributionAsDeployedTestAlreadyDeployed() {
		String notifyAction = "DNotify";
		String requestAction = "DRequest";
		String resultAction = "DResult";
		String did = "123456";

		setupBeforeDeploy(notifyAction, requestAction, did);
		List<DistributionDeployEvent> resultList = new ArrayList<DistributionDeployEvent>();
		Map<String, Object> params = new HashMap<String, Object>();
		DistributionDeployEvent event = new DistributionDeployEvent();

		event.setAction(resultAction);
		event.setDid(did);
		event.setStatus("200");
		// ESTimeBasedEvent deployEvent = new ESTimeBasedEvent();
		// deployEvent.setFields(params);
		resultList.add(event);
		Either<List<DistributionDeployEvent>, ActionStatus> eventList = Either.left(resultList);

		Mockito.when(auditingDao.getDistributionDeployByStatus(Mockito.anyString(), Mockito.eq(resultAction), Mockito.anyString())).thenReturn(eventList);

		Either<Service, ResponseFormat> markDeployed = bl.markDistributionAsDeployed(did, did, user);
		assertTrue(markDeployed.isLeft());

		Mockito.verify(auditingDao, Mockito.times(0)).getDistributionRequest(did, requestAction);

	}

	@Test
	public void markDistributionAsDeployedTestSuccess() {
		String notifyAction = "DNotify";
		String requestAction = "DRequest";
		String did = "123456";

		setupBeforeDeploy(notifyAction, requestAction, did);

		Either<Service, ResponseFormat> markDeployed = bl.markDistributionAsDeployed(did, did, user);
		assertTrue(markDeployed.isLeft());

	}

	@Test
	public void markDistributionAsDeployedTestNotDistributed() {
		String notifyAction = "DNotify";
		String requestAction = "DRequest";
		String did = "123456";

		setupBeforeDeploy(notifyAction, requestAction, did);
		List<ResourceAdminEvent> emptyList = new ArrayList<ResourceAdminEvent>();
		Either<List<ResourceAdminEvent>, ActionStatus> emptyEventList = Either.left(emptyList);
		Mockito.when(auditingDao.getDistributionRequest(Mockito.anyString(), Mockito.eq(requestAction))).thenReturn(emptyEventList);

		Either<Component, StorageOperationStatus> notFound = Either.right(StorageOperationStatus.NOT_FOUND);
		Mockito.when(toscaOperationFacade.getToscaElement(did)).thenReturn(notFound);

		Either<Service, ResponseFormat> markDeployed = bl.markDistributionAsDeployed(did, did, user);
		assertTrue(markDeployed.isRight());
		assertEquals(404, markDeployed.right().value().getStatus().intValue());

	}

	private void testServiceBadCategoryCreate() {

		Service serviceExist = createServiceObject(false);
		CategoryDefinition category = new CategoryDefinition();
		category.setName("koko");
		List<CategoryDefinition> categories = new ArrayList<>();
		categories.add(category);
		serviceExist.setCategories(categories);

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceExist, user);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_CATEGORY, ComponentTypeEnum.SERVICE.getValue());
	}

	// Service category - stop
	// Service projectCode - start
	private void testInvalidProjectCode() {

		Service serviceExist = createServiceObject(false);
		serviceExist.setProjectCode("koko!!");

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceExist, user);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.INVALID_PROJECT_CODE);
	}

	private void testProjectCodeTooLong() {

		Service serviceExist = createServiceObject(false);
		String tooLongProjectCode = "thisNameIsVeryLongAndExeccedsTheNormalLengthForProjectCode"; 
		serviceExist.setProjectCode(tooLongProjectCode);

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceExist, user);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.INVALID_PROJECT_CODE);
	}

	private void testProjectCodeTooShort() {

		Service serviceExist = createServiceObject(false);
		serviceExist.setProjectCode("333");

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceExist, user);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.INVALID_PROJECT_CODE);
	}

	private void testMissingProjectCode() {

		Service serviceExist = createServiceObject(false);
		serviceExist.setProjectCode(null);

		Either<Service, ResponseFormat> createResponse = bl.createService(serviceExist, user);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.MISSING_PROJECT_CODE);
	}

//	@Test
//	public void testDeleteMarkedServicesNoServices() {
//		List<String> ids = new ArrayList<String>();
//		Either<List<String>, StorageOperationStatus> eitherNoResources = Either.left(ids);
//		when(toscaOperationFacade.getAllComponentsMarkedForDeletion()).thenReturn(eitherNoResources);
//
//		Either<List<String>, ResponseFormat> deleteMarkedResources = bl.deleteMarkedComponents();
//		assertTrue(deleteMarkedResources.isLeft());
//		assertTrue(deleteMarkedResources.left().value().isEmpty());
//
//		Mockito.verify(artifactBl, Mockito.times(0)).deleteAllComponentArtifactsIfNotOnGraph(Mockito.anyList());
//
//	}

	@Test
	@Ignore
	public void testDeleteMarkedServices() {
		List<String> ids = new ArrayList<String>();
		String resourceInUse = "123";
		ids.add(resourceInUse);
		String resourceFree = "456";
		ids.add(resourceFree);
		Either<List<String>, StorageOperationStatus> eitherNoResources = Either.left(ids);
		when(toscaOperationFacade.getAllComponentsMarkedForDeletion(ComponentTypeEnum.RESOURCE)).thenReturn(eitherNoResources);

		Either<Boolean, StorageOperationStatus> resourceInUseResponse = Either.left(true);
		Either<Boolean, StorageOperationStatus> resourceFreeResponse = Either.left(false);

		List<ArtifactDefinition> artifacts = new ArrayList<ArtifactDefinition>();
		Either<List<ArtifactDefinition>, StorageOperationStatus> getArtifactsResponse = Either.left(artifacts);
//		when(toscaOperationFacade.getComponentArtifactsForDelete(resourceFree, NodeTypeEnum.Service, true)).thenReturn(getArtifactsResponse);

		when(toscaOperationFacade.isComponentInUse(resourceFree)).thenReturn(resourceFreeResponse);
		when(toscaOperationFacade.isComponentInUse(resourceInUse)).thenReturn(resourceInUseResponse);

		Either<Component, StorageOperationStatus> eitherDelete = Either.left(new Resource());
		when(toscaOperationFacade.deleteToscaComponent(resourceFree)).thenReturn(eitherDelete);

		when(artifactBl.deleteAllComponentArtifactsIfNotOnGraph(artifacts)).thenReturn(StorageOperationStatus.OK);

		Either<List<String>, ResponseFormat> deleteMarkedResources = bl.deleteMarkedComponents();
		assertTrue(deleteMarkedResources.isLeft());
		List<String> resourceIdList = deleteMarkedResources.left().value();
		assertFalse(resourceIdList.isEmpty());
		assertTrue(resourceIdList.contains(resourceFree));
		assertFalse(resourceIdList.contains(resourceInUse));

		Mockito.verify(artifactBl, Mockito.times(1)).deleteAllComponentArtifactsIfNotOnGraph(artifacts);
	}

	private Service createServiceObject(boolean afterCreate) {
		Service service = new Service();
		service.setName("Service");
		CategoryDefinition category = new CategoryDefinition();
		category.setName(SERVICE_CATEGORY);
		List<CategoryDefinition> categories = new ArrayList<>();
		categories.add(category);
		service.setCategories(categories);

		service.setDescription("description");
		List<String> tgs = new ArrayList<String>();
		tgs.add(service.getName());
		service.setTags(tgs);
		// service.setVendorName("Motorola");
		// service.setVendorRelease("1.0.0");
		service.setIcon("MyIcon");
		// service.setState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		service.setContactId("aa1234");
		service.setProjectCode("12345");

		if (afterCreate) {
			service.setVersion("0.1");
			service.setUniqueId(service.getName() + ":" + service.getVersion());
			service.setCreatorUserId(user.getUserId());
			service.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
		}
		return service;
	}

	private void mockAuditingDaoLogic() {
		FILTER_MAP_CERTIFIED_VERSION.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, COMPONNET_ID);
		FILTER_MAP_UNCERTIFIED_VERSION_CURR.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, COMPONNET_ID);
		FILTER_MAP_UNCERTIFIED_VERSION_PREV.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, COMPONNET_ID);

		FILTER_MAP_UNCERTIFIED_VERSION_CURR.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION, UNCERTIFIED_VERSION);
		FILTER_MAP_UNCERTIFIED_VERSION_PREV.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_VERSION, UNCERTIFIED_VERSION);

		final ResourceAdminEvent createResourceAudit = new ResourceAdminEvent();
		createResourceAudit.setModifier("Carlos Santana(cs0008)");
		createResourceAudit.setCurrState("NOT_CERTIFIED_CHECKOUT");
		createResourceAudit.setCurrVersion("0.1");
		createResourceAudit.setServiceInstanceId("82eddd99-0bd9-4742-ab0a-1bdb5e262a05");
		createResourceAudit.setRequestId("3e65cea1-7403-4bc7-b461-e2544d83799f");
		createResourceAudit.setDesc("OK");
		createResourceAudit.setResourceType("Resource");
		createResourceAudit.setStatus("201");
		createResourceAudit.setPrevVersion("");
		createResourceAudit.setAction("Create");
		// fields.put("TIMESTAMP", "2015-11-22 09:19:12.977");
		createResourceAudit.setPrevState("");
		createResourceAudit.setResourceName("MyTestResource");
		// createResourceAudit.setFields(fields);

		final ResourceAdminEvent checkInResourceAudit = new ResourceAdminEvent();
		checkInResourceAudit.setModifier("Carlos Santana(cs0008)");
		checkInResourceAudit.setCurrState("NOT_CERTIFIED_CHECKIN");
		checkInResourceAudit.setCurrVersion("0.1");
		checkInResourceAudit.setServiceInstanceId("82eddd99-0bd9-4742-ab0a-1bdb5e262a05");
		checkInResourceAudit.setRequestId("ffacbf5d-eeb1-43c6-a310-37fe7e1cc091");
		checkInResourceAudit.setDesc("OK");
		checkInResourceAudit.setComment("Stam");
		checkInResourceAudit.setResourceType("Resource");
		checkInResourceAudit.setStatus("200");
		checkInResourceAudit.setPrevVersion("0.1");
		checkInResourceAudit.setAction("Checkin");
		// fields.put("TIMESTAMP", "2015-11-22 09:25:03.797");
		checkInResourceAudit.setPrevState("NOT_CERTIFIED_CHECKOUT");
		checkInResourceAudit.setResourceName("MyTestResource");

		final ResourceAdminEvent checkOutResourceAudit = new ResourceAdminEvent();
		checkOutResourceAudit.setModifier("Carlos Santana(cs0008)");
		checkOutResourceAudit.setCurrState("NOT_CERTIFIED_CHECKOUT");
		checkOutResourceAudit.setCurrVersion("0.2");
		checkOutResourceAudit.setServiceInstanceId("82eddd99-0bd9-4742-ab0a-1bdb5e262a05");
		checkOutResourceAudit.setRequestId("7add5078-4c16-4d74-9691-cc150e3c96b8");
		checkOutResourceAudit.setDesc("OK");
		checkOutResourceAudit.setComment("");
		checkOutResourceAudit.setResourceType("Resource");
		checkOutResourceAudit.setStatus("200");
		checkOutResourceAudit.setPrevVersion("0.1");
		checkOutResourceAudit.setAction("Checkout");
		// fields.put("TIMESTAMP", "2015-11-22 09:39:41.024");
		checkOutResourceAudit.setPrevState("NOT_CERTIFIED_CHECKIN");
		checkOutResourceAudit.setResourceName("MyTestResource");
		// checkOutResourceAudit.setFields(fields);

		// Mockito.doAnswer(new Answer<Either<List<ESTimeBasedEvent>,
		// ActionStatus> >() {
		// public Either<List<ESTimeBasedEvent>, ActionStatus>
		// answer(InvocationOnMock invocation) {
		// final Either<List<ESTimeBasedEvent>, ActionStatus> either;
		// final List<ESTimeBasedEvent> list;
		// Object[] args = invocation.getArguments();
		// Map<AuditingFieldsKeysEnum, Object> filterMap =
		// (Map<AuditingFieldsKeysEnum, Object>) args[0];
		// if( filterMap.equals(FILTER_MAP_CERTIFIED_VERSION) ){
		// list = new
		// ArrayList<ESTimeBasedEvent>(){{add(createResourceAudit);add(checkInResourceAudit);add(checkOutResourceAudit);}};
		// either = Either.left(list);
		//
		// }
		// else if( filterMap.equals(FILTER_MAP_UNCERTIFIED_VERSION_PREV) ){
		// list = new ArrayList<ESTimeBasedEvent>();
		// either = Either.left(list);
		// }
		// else if( filterMap.equals(FILTER_MAP_UNCERTIFIED_VERSION_CURR) ){
		// list = new
		// ArrayList<ESTimeBasedEvent>(){{/*add(createResourceAudit);add(checkInResourceAudit);*/add(checkOutResourceAudit);}};
		// either = Either.left(list);
		// }
		// else{
		// either = null;
		// }
		// return either;
		// }
		// }).when(auditingDao).getFilteredResourceAdminAuditingEvents(Mockito.anyMap());
		//
		//
		List<ResourceAdminEvent> list = new ArrayList<ResourceAdminEvent>() {
			{
				add(createResourceAudit);
				add(checkInResourceAudit);
				add(checkOutResourceAudit);
			}
		};
		Either<List<ResourceAdminEvent>, ActionStatus> result = Either.left(list);
		Mockito.when(auditingDao.getByServiceInstanceId(Mockito.anyString())).thenReturn(result);

		List<ResourceAdminEvent> listPrev = new ArrayList<ResourceAdminEvent>();
		Either<List<ResourceAdminEvent>, ActionStatus> resultPrev = Either.left(listPrev);
		Mockito.when(auditingDao.getAuditByServiceIdAndPrevVersion(Mockito.anyString(), Mockito.anyString())).thenReturn(resultPrev);

		List<ResourceAdminEvent> listCurr = new ArrayList<ResourceAdminEvent>() {
			{
				add(checkOutResourceAudit);
			}
		};
		Either<List<ResourceAdminEvent>, ActionStatus> resultCurr = Either.left(listCurr);
		Mockito.when(auditingDao.getAuditByServiceIdAndCurrVersion(Mockito.anyString(), Mockito.anyString())).thenReturn(resultCurr);

	}

	private void setupBeforeDeploy(String notifyAction, String requestAction, String did) {

		DistributionNotificationEvent notifyEvent = new DistributionNotificationEvent();
		notifyEvent.setAction(notifyAction);
		notifyEvent.setDid(did);
		notifyEvent.setStatus("200");

		ResourceAdminEvent requestEvent = new ResourceAdminEvent();
		requestEvent.setAction(requestAction);
		requestEvent.setDid(did);
		requestEvent.setStatus("200");

		ArrayList<DistributionNotificationEvent> arrayList = new ArrayList<DistributionNotificationEvent>();
		List<DistributionNotificationEvent> notifyResults = arrayList;
		notifyResults.add(notifyEvent);
		Either<List<DistributionNotificationEvent>, ActionStatus> eitherNotify = Either.left(notifyResults);

		Mockito.when(auditingDao.getDistributionNotify(Mockito.anyString(), Mockito.eq(notifyAction))).thenReturn(eitherNotify);

		List<ResourceAdminEvent> requestResults = new ArrayList<ResourceAdminEvent>();
		requestResults.add(requestEvent);
		Either<List<ResourceAdminEvent>, ActionStatus> eitherRequest = Either.left(requestResults);
		Mockito.when(auditingDao.getDistributionRequest(Mockito.anyString(), Mockito.eq(requestAction))).thenReturn(eitherRequest);

		Either<Component, StorageOperationStatus> eitherService = Either.left(createServiceObject(true));
		Mockito.when(toscaOperationFacade.getToscaElement(Mockito.anyString())).thenReturn(eitherService);

		List<DistributionDeployEvent> emptyList = new ArrayList<DistributionDeployEvent>();
		Either<List<DistributionDeployEvent>, ActionStatus> emptyEventList = Either.left(emptyList);
		Mockito.when(auditingDao.getDistributionDeployByStatus(Mockito.anyString(), Mockito.eq("DResult"), Mockito.anyString())).thenReturn(emptyEventList);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFindGroupInstanceOnRelatedComponentInstance() {
		
		Class<ServiceBusinessLogic> targetClass = ServiceBusinessLogic.class;
		String methodName = "findGroupInstanceOnRelatedComponentInstance";
		Object invalidId = "invalidId";
		
		Component service = createNewService();
		List<ComponentInstance> componentInstances = service.getComponentInstances();
		
		Either<ImmutablePair<ComponentInstance, GroupInstance>, ResponseFormat> findGroupInstanceRes;
		Object[] argObjects = {service, componentInstances.get(1).getUniqueId(), componentInstances.get(1).getGroupInstances().get(1).getUniqueId()};
		Class[] argClasses = {Component.class, String.class,String.class};
	    try {
	    	Method method = targetClass.getDeclaredMethod(methodName, argClasses);
	    	method.setAccessible(true);
	    	
	    	findGroupInstanceRes = (Either<ImmutablePair<ComponentInstance, GroupInstance>, ResponseFormat>) method.invoke(bl, argObjects);
	    	assertTrue(findGroupInstanceRes != null);
	    	assertTrue(findGroupInstanceRes.left().value().getKey().getUniqueId().equals(componentInstances.get(1).getUniqueId()));
	    	assertTrue(findGroupInstanceRes.left().value().getValue().getUniqueId().equals(componentInstances.get(1).getGroupInstances().get(1).getUniqueId()));
	    	
			Object[] argObjectsInvalidCiId = {service, invalidId , componentInstances.get(1).getGroupInstances().get(1).getUniqueId()};
			
			findGroupInstanceRes =	(Either<ImmutablePair<ComponentInstance, GroupInstance>, ResponseFormat>) method.invoke(bl, argObjectsInvalidCiId);
	    	assertTrue(findGroupInstanceRes != null);
	    	assertTrue(findGroupInstanceRes.isRight());
	    	assertTrue(findGroupInstanceRes.right().value().getMessageId().equals("SVC4593"));
	    	
			Object[] argObjectsInvalidGiId = {service, componentInstances.get(1).getUniqueId() , invalidId};
			
			findGroupInstanceRes =	(Either<ImmutablePair<ComponentInstance, GroupInstance>, ResponseFormat>) method.invoke(bl, argObjectsInvalidGiId);
	    	assertTrue(findGroupInstanceRes != null);
	    	assertTrue(findGroupInstanceRes.isRight());
	    	assertTrue(findGroupInstanceRes.right().value().getMessageId().equals("SVC4653"));
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	    }
	}

	private Component createNewService() {
		
		Service service = new Service();
		int listSize = 3;
		service.setName("serviceName");
		service.setUniqueId("serviceUniqueId");
		List<ComponentInstance> componentInstances = new ArrayList<>();
		ComponentInstance ci;
		for(int i= 0; i<listSize; ++i){
			ci = new ComponentInstance();
			ci.setName("ciName" + i);
			ci.setUniqueId("ciId" + i);
			List<GroupInstance>  groupInstances= new ArrayList<>();
			GroupInstance gi;
			for(int j = 0; j<listSize; ++j){
				gi = new GroupInstance();
				gi.setName(ci.getName( )+ "giName" + j);
				gi.setUniqueId(ci.getName() + "giId" + j);
				groupInstances.add(gi);
			}
			ci.setGroupInstances(groupInstances);
			componentInstances.add(ci);
		}
		service.setComponentInstances(componentInstances);
		return service;
	}
	

	@Test
	public void testDerivedFromGeneric() {
		Service service = createServiceObject(true);
		when(toscaOperationFacade.createToscaComponent(service)).thenReturn(Either.left(service));
		Either<Service, ResponseFormat> createResponse = bl.createService(service, user);
		assertTrue(createResponse.isLeft());
		service = createResponse.left().value();
		assertTrue(service.getDerivedFromGenericType().equals(genericService.getToscaResourceName()));
		assertTrue(service.getDerivedFromGenericVersion().equals(genericService.getVersion()));
	}

	
	private Resource setupGenericServiceMock(){
		Resource genericService = new Resource();
		genericService.setVersion("1.0");
		genericService.setToscaResourceName(GENERIC_SERVICE_NAME);
		return genericService;
	}
}
