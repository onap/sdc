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

package org.openecomp.sdc.be.components.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.ElementOperationMock;
import org.openecomp.sdc.be.auditing.api.IAuditingManager;
import org.openecomp.sdc.be.auditing.impl.AuditingLogFormatUtil;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.operations.api.ICapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IPropertyOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CacheMangerOperation;
import org.openecomp.sdc.be.model.operations.impl.CsarOperation;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.model.operations.impl.ResourceOperation;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fj.data.Either;

public class ResourceBusinessLogicTest {

	private static Logger log = LoggerFactory.getLogger(ResourceBusinessLogicTest.class.getName());
	public static final String RESOURCE_CATEGORY = "Network Layer 2-3/Router";
	public static final String RESOURCE_CATEGORY1 = "Network Layer 2-3";
	public static final String RESOURCE_SUBCATEGORY = "Router";

	public static final String UPDATED_CATEGORY = "Network Layer 2-3/Gateway";
	public static final String UPDATED_SUBCATEGORY = "Gateway";

	public static final String RESOURCE_NAME = "My-Resource_Name with   space";

	final ServletContext servletContext = Mockito.mock(ServletContext.class);
	IAuditingManager iAuditingManager = null;
	IElementOperation mockElementDao;
	TitanGenericDao mockTitanDao = Mockito.mock(TitanGenericDao.class);
	UserBusinessLogic mockUserAdmin = Mockito.mock(UserBusinessLogic.class);
	final ResourceOperation resourceOperation = Mockito.mock(ResourceOperation.class);
	final LifecycleBusinessLogic lifecycleBl = Mockito.mock(LifecycleBusinessLogic.class);
	final ICapabilityTypeOperation capabilityTypeOperation = Mockito.mock(ICapabilityTypeOperation.class);
	final IPropertyOperation propertyOperation = Mockito.mock(IPropertyOperation.class);
	final ApplicationDataTypeCache applicationDataTypeCache = Mockito.mock(ApplicationDataTypeCache.class);
	WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
	WebApplicationContext webAppContext = Mockito.mock(WebApplicationContext.class);
	AuditingLogFormatUtil auditingLogFormatter = Mockito.mock(AuditingLogFormatUtil.class);
	@InjectMocks
	ResourceBusinessLogic bl = new ResourceBusinessLogic();
	ResponseFormatManager responseManager = null;
	AuditingManager auditingManager = Mockito.mock(AuditingManager.class);
	GraphLockOperation graphLockOperation = Mockito.mock(GraphLockOperation.class);
	User user = null;
	Resource resourceResponse = null;
	ComponentsUtils componentsUtils = new ComponentsUtils();
	ArtifactsBusinessLogic artifactManager = Mockito.mock(ArtifactsBusinessLogic.class);
	CsarOperation csarOperation = Mockito.mock(CsarOperation.class);
	Map<String, DataTypeDefinition> emptyDataTypes = new HashMap<String, DataTypeDefinition>();

	CacheMangerOperation cacheManager = Mockito.mock(CacheMangerOperation.class);

	public ResourceBusinessLogicTest() {

	}

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.reset(propertyOperation);

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
		// when(servletContext.getAttribute(Constants.AUDITING_MANAGER)).thenReturn(iAuditingManager);
		when(servletContext.getAttribute(Constants.RESOURCE_OPERATION_MANAGER)).thenReturn(resourceOperation);
		when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
		when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webAppContext);
		when(webAppContext.getBean(IElementOperation.class)).thenReturn(mockElementDao);

		// Resource Operation mock methods
		// getCount
		/*
		 * Either<Integer, StorageOperationStatus> eitherCount = Either.left(0); when(resourceOperation.getNumberOfResourcesByName("MyResourceName")). thenReturn(eitherCount); Either<Integer, StorageOperationStatus> eitherCountExist = Either.left(1);
		 * when(resourceOperation.getNumberOfResourcesByName("alreadyExist")). thenReturn(eitherCountExist);
		 */
		Either<Integer, StorageOperationStatus> eitherCountRoot = Either.left(1);
		when(resourceOperation.getNumberOfResourcesByName("Root".toLowerCase())).thenReturn(eitherCountRoot);
		Either<Boolean, StorageOperationStatus> eitherFalse = Either.left(false);
		when(resourceOperation.validateResourceNameExists(ValidationUtils.normaliseComponentName("Root"), ResourceTypeEnum.VFC)).thenReturn(eitherFalse);

		Either<Boolean, StorageOperationStatus> eitherCountExist = Either.left(false);
		when(resourceOperation.validateResourceNameExists("alreadyExists", ResourceTypeEnum.VFC)).thenReturn(eitherCountExist);
		Either<Boolean, StorageOperationStatus> eitherCount = Either.left(true);
		when(resourceOperation.validateResourceNameExists(RESOURCE_NAME, ResourceTypeEnum.VFC)).thenReturn(eitherCount);

		Either<Boolean, StorageOperationStatus> validateDerivedExists = Either.left(false);
		when(resourceOperation.validateToscaResourceNameExists("Root")).thenReturn(validateDerivedExists);
		Either<Boolean, StorageOperationStatus> validateDerivedNotExists = Either.left(true);
		when(resourceOperation.validateToscaResourceNameExists(ValidationUtils.normaliseComponentName("kuku"))).thenReturn(validateDerivedNotExists);

		when(graphLockOperation.lockComponent(Mockito.anyString(), Mockito.eq(NodeTypeEnum.Resource))).thenReturn(StorageOperationStatus.OK);
		when(graphLockOperation.lockComponentByName(Mockito.anyString(), Mockito.eq(NodeTypeEnum.Resource))).thenReturn(StorageOperationStatus.OK);

		ArtifactDefinition artifactDef = new ArtifactDefinition();
		artifactDef.setUniqueId("123.123");
		Either<ArtifactDefinition, StorageOperationStatus> returnEither = Either.left(artifactDef);
		when(artifactManager.createArtifactPlaceHolderInfo(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap(), Mockito.any(User.class), Mockito.any(ArtifactGroupTypeEnum.class))).thenReturn(artifactDef);

		when(artifactManager.addHeatEnvArtifact(Mockito.any(ArtifactDefinition.class), Mockito.any(ArtifactDefinition.class), Mockito.anyString(), Mockito.any(NodeTypeEnum.class), Mockito.anyBoolean())).thenReturn(returnEither);

		// createResource
		resourceResponse = createResourceObject(true);
		Either<Resource, StorageOperationStatus> eitherCreate = Either.left(resourceResponse);
		Either<List<ResourceMetadataData>, StorageOperationStatus> eitherValidate = Either.left(null);
		when(resourceOperation.createResource(Mockito.any(Resource.class), Mockito.anyBoolean())).thenReturn(eitherCreate);
		when(resourceOperation.validateCsarUuidUniqueness(Mockito.anyString())).thenReturn(eitherValidate);
		Map<String, DataTypeDefinition> emptyDataTypes = new HashMap<String, DataTypeDefinition>();
		when(applicationDataTypeCache.getAll()).thenReturn(Either.left(emptyDataTypes));

		// BL object
		bl = new ResourceBusinessLogic();
		bl.setElementDao(mockElementDao);
		bl.setUserAdmin(mockUserAdmin);
		bl.setResourceOperation(resourceOperation);
		bl.setCapabilityTypeOperation(capabilityTypeOperation);
		componentsUtils.Init();
		componentsUtils.setAuditingManager(auditingManager);
		bl.setComponentsUtils(componentsUtils);
		bl.setLifecycleManager(lifecycleBl);
		bl.setGraphLockOperation(graphLockOperation);
		bl.setArtifactsManager(artifactManager);
		bl.setPropertyOperation(propertyOperation);
		bl.setTitanGenericDao(mockTitanDao);
		bl.setApplicationDataTypeCache(applicationDataTypeCache);
		bl.setCsarOperation(csarOperation);
		bl.setCacheManagerOperation(cacheManager);

		Resource resourceCsar = createResourceObjectCsar(true);
		setCanWorkOnResource(resourceCsar);
		Either<Resource, StorageOperationStatus> oldResourceRes = Either.left(resourceCsar);
		when(resourceOperation.getResource(resourceCsar.getUniqueId())).thenReturn(oldResourceRes);
		when(resourceOperation.getLatestResourceByCsarOrName(resourceCsar.getCsarUUID(), resourceCsar.getSystemName())).thenReturn(oldResourceRes);
		when(resourceOperation.updateResource(Mockito.any(Resource.class), Mockito.anyBoolean())).thenReturn(oldResourceRes);
		responseManager = ResponseFormatManager.getInstance();

	}

	private Resource createResourceObject(boolean afterCreate) {
		Resource resource = new Resource();
		resource.setName(RESOURCE_NAME);
		resource.addCategory(RESOURCE_CATEGORY1, RESOURCE_SUBCATEGORY);
		resource.setDescription("My short description");
		List<String> tgs = new ArrayList<String>();
		tgs.add("test");
		tgs.add(resource.getName());
		resource.setTags(tgs);
		List<String> template = new ArrayList<String>();
		template.add("Root");
		resource.setDerivedFrom(template);
		resource.setVendorName("Motorola");
		resource.setVendorRelease("1.0.0");
		resource.setContactId("ya5467");
		resource.setIcon("MyIcon");

		if (afterCreate) {
			resource.setName(resource.getName());
			resource.setVersion("0.1");
			;
			resource.setUniqueId(resource.getName().toLowerCase() + ":" + resource.getVersion());
			resource.setCreatorUserId(user.getUserId());
			resource.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
			resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		log.debug(gson.toJson(resource));
		return resource;
	}

	private Resource createResourceObjectCsar(boolean afterCreate) {
		Resource resource = new Resource();
		resource.setName(RESOURCE_NAME);
		resource.addCategory(RESOURCE_CATEGORY1, RESOURCE_SUBCATEGORY);
		resource.setDescription("My short description");
		List<String> tgs = new ArrayList<String>();
		tgs.add("test");
		tgs.add(resource.getName());
		resource.setTags(tgs);
		List<String> template = new ArrayList<String>();
		template.add("Root");
		resource.setDerivedFrom(template);
		resource.setVendorName("Motorola");
		resource.setVendorRelease("1.0.0");
		resource.setContactId("ya5467");
		resource.setIcon("MyIcon");
		resource.setCsarUUID("valid_vf.csar");
		resource.setCsarVersion("1");

		if (afterCreate) {
			resource.setName(resource.getName());
			resource.setVersion("0.1");
			;
			resource.setUniqueId(resource.getName().toLowerCase() + ":" + resource.getVersion());
			resource.setCreatorUserId(user.getUserId());
			resource.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
			resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		log.debug(gson.toJson(resource));
		return resource;
	}

	private Resource setCanWorkOnResource(Resource resource) {
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setLastUpdaterUserId(user.getUserId());
		return resource;
	}

	@Test
	public void testHappyScenario() {
		Resource resource = createResourceObject(false);
		Either<Resource, ResponseFormat> createResponse = bl.createResource(resource, user, null, null);

		if (createResponse.isRight()) {
			assertEquals(new Integer(200), createResponse.right().value().getStatus());
		}
		assertEquals(createResourceObject(true), createResponse.left().value());
	}

	@Test
	public void testUpdateHappyScenario() {
		Resource resource = createResourceObjectCsar(true);
		setCanWorkOnResource(resource);
		Either<Resource, ResponseFormat> updateResponse = bl.validateAndUpdateResourceFromCsar(resource, user, null, null, resource.getUniqueId());
		if (updateResponse.isRight()) {
			assertEquals(new Integer(200), updateResponse.right().value().getStatus());
		}
		assertEquals(resource.getUniqueId(), updateResponse.left().value().getUniqueId());
	}

	/* CREATE validations - start ***********************/
	// Resource name - start

	@Test
	public void testFailedResourceValidations() {
		testResourceNameExist();
		testResourceNameEmpty();
		// testResourceNameExceedsLimit();
		testResourceNameWrongFormat();
		testResourceDescExceedsLimitCreate();
		testResourceDescNotEnglish();
		testResourceDescriptionEmpty();
		testResourceDescriptionMissing();
		testResourceIconMissing();
		testResourceIconInvalid();
		testResourceIconExceedsLimit();
		testResourceTagNotExist();
		testResourceTagEmpty();
		testTagsExceedsLimitCreate();
		// testTagsSingleExceedsLimit();
		testTagsNoServiceName();
		testInvalidTag();
		// 1610OS Support - Because of changes in the validation in the ui these tests will fail. need to fix them
		//testContactIdTooLong();
		//testContactIdWrongFormatCreate();
		testResourceContactIdEmpty();
		testResourceContactIdMissing();
		testVendorNameExceedsLimit();
		testVendorNameWrongFormatCreate();
		testVendorReleaseWrongFormat();
		testVendorReleaseExceedsLimitCreate();
		testResourceVendorNameMissing();
		testResourceVendorReleaseMissing();
		testResourceCategoryExist();
		testResourceBadCategoryCreate();
		testHappyScenarioCostLicenseType();
		testCostWrongFormatCreate();
		testLicenseTypeWrongFormatCreate();
		testResourceTemplateNotExist();
		testResourceTemplateEmpty();
		testResourceTemplateInvalid();
	}

	private void testResourceNameExist() {
		String resourceName = "alreadyExists";
		Resource resourceExist = createResourceObject(false);
		resourceExist.setName(resourceName);
		resourceExist.getTags().add(resourceName);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist, user, null, null);
		assertResponse(createResponse, ActionStatus.COMPONENT_NAME_ALREADY_EXIST, ComponentTypeEnum.RESOURCE.getValue(), resourceName);
	}

	private void testResourceNameEmpty() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setName(null);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist, user, null, null);
		assertResponse(createResponse, ActionStatus.MISSING_COMPONENT_NAME, ComponentTypeEnum.RESOURCE.getValue());
	}

	private void testResourceNameExceedsLimit() {
		Resource resourceExccedsNameLimit = createResourceObject(false);
		// 51 chars, the limit is 50
		String tooLongResourceName = "zCRCAWjqte0DtgcAAMmcJcXeNubeX1p1vOZNTShAHOYNAHvV3iK";
		resourceExccedsNameLimit.setName(tooLongResourceName);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExccedsNameLimit, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_NAME_EXCEEDS_LIMIT, ComponentTypeEnum.RESOURCE.getValue(), "" + ValidationUtils.COMPONENT_NAME_MAX_LENGTH);
	}

	private void testResourceNameWrongFormat() {
		Resource resource = createResourceObject(false);
		// contains :
		String nameWrongFormat = "ljg?fd";
		resource.setName(nameWrongFormat);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resource, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.INVALID_COMPONENT_NAME, ComponentTypeEnum.RESOURCE.getValue());
	}

	// Resource name - end
	// Resource description - start
	private void testResourceDescExceedsLimitCreate() {
		Resource resourceExccedsDescLimit = createResourceObject(false);
		// 1025 chars, the limit is 1024
		String tooLongResourceDesc = "1GUODojQ0sGzKR4NP7e5j82ADQ3KHTVOaezL95qcbuaqDtjZhAQGQ3iFwKAy580K4WiiXs3u3zq7RzXcSASl5fm0RsWtCMOIDP"
				+ "AOf9Tf2xtXxPCuCIMCR5wOGnNTaFxgnJEHAGxilBhZDgeMNHmCN1rMK5B5IRJOnZxcpcL1NeG3APTCIMP1lNAxngYulDm9heFSBc8TfXAADq7703AvkJT0QPpGq2z2P"
				+ "tlikcAnIjmWgfC5Tm7UH462BAlTyHg4ExnPPL4AO8c92VrD7kZSgSqiy73cN3gLT8uigkKrUgXQFGVUFrXVyyQXYtVM6bLBeuCGQf4C2j8lkNg6M0J3PC0PzMRoinOxk"
				+ "Ae2teeCtVcIj4A1KQo3210j8q2v7qQU69Mabsa6DT9FgE4rcrbiFWrg0Zto4SXWD3o1eJA9o29lTg6kxtklH3TuZTmpi5KVp1NFhS1RpnqF83tzv4mZLKsx7Zh1fEgYvRFwx1"
				+ "ar3RolyDfNoZiGBGTMsZzz7RPFBf2hTnLmNqVGQnHKhhGj0Y5s8t2cbqbO2nmHiJb9uaUVrCGypgbAcJL3KPOBfAVW8PcpmNj4yVjI3L4x5zHjmGZbp9vKshEQODcrmcgsYAoKqe"
				+ "uu5u7jk8XVxEfQ0m5qL8UOErXPlJovSmKUmP5B5T0w299zIWDYCzSoNasHpHjOMDLAiDDeHbozUOn9t3Qou00e9POq4RMM0VnIx1H38nJoJZz2XH8CI5YMQe7oTagaxgQTF2aa0qaq2"
				+ "V6nJsfRGRklGjNhFFYP2cS4Xv2IJO9DSX6LTXOmENrGVJJvMOZcvnBaZPfoAHN0LU4i1SoepLzulIxnZBfkUWFJgZ5wQ0Bco2GC1HMqzW21rwy4XHRxXpXbmW8LVyoA1KbnmVmROycU4"
				+ "scTZ62IxIcIWCVeMjBIcTviXULbPUyqlfEPXWr8IMJtpAaELWgyquPClAREMDs2b9ztKmUeXlMccFES1XWbFTrhBHhmmDyVReEgCwfokrUFR13LTUK1k8I6OEHOs";

		resourceExccedsDescLimit.setDescription(tooLongResourceDesc);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExccedsDescLimit, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_DESCRIPTION_EXCEEDS_LIMIT, ComponentTypeEnum.RESOURCE.getValue(), "" + ValidationUtils.COMPONENT_DESCRIPTION_MAX_LENGTH);
	}

	private void testResourceDescNotEnglish() {
		Resource notEnglish = createResourceObject(false);
		// Not english
		String notEnglishDesc = "\uC2B5";
		notEnglish.setDescription(notEnglishDesc);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(notEnglish, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_DESCRIPTION, ComponentTypeEnum.RESOURCE.getValue());
	}

	private void testResourceDescriptionEmpty() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setDescription("");

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_DESCRIPTION, ComponentTypeEnum.RESOURCE.getValue());
	}

	private void testResourceDescriptionMissing() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setDescription(null);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_DESCRIPTION, ComponentTypeEnum.RESOURCE.getValue());
	}
	// Resource description - end
	// Resource icon start

	private void testResourceIconMissing() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setIcon(null);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_ICON, ComponentTypeEnum.RESOURCE.getValue());
	}

	private void testResourceIconInvalid() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setIcon("kjk3453^&");

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_ICON, ComponentTypeEnum.RESOURCE.getValue());
	}

	private void testResourceIconExceedsLimit() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setIcon("dsjfhskdfhskjdhfskjdhkjdhfkshdfksjsdkfhsdfsdfsdfsfsdfsf");

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_ICON_EXCEEDS_LIMIT, ComponentTypeEnum.RESOURCE.getValue(), "" + ValidationUtils.ICON_MAX_LENGTH);
	}

	// Resource icon end
	// Resource tags - start
	private void testResourceTagNotExist() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setTags(null);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_TAGS);
	}

	private void testResourceTagEmpty() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setTags(new ArrayList<String>());

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_TAGS);
	}

	private void testTagsExceedsLimitCreate() {
		Resource resourceExccedsNameLimit = createResourceObject(false);
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
		tagsList.add(resourceExccedsNameLimit.getName());

		resourceExccedsNameLimit.setTags(tagsList);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExccedsNameLimit, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_TAGS_EXCEED_LIMIT, "" + ValidationUtils.TAG_LIST_MAX_LENGTH);

	}

	private void testTagsSingleExceedsLimit() {
		Resource resourceExccedsNameLimit = createResourceObject(false);
		String tag1 = "afzs2qLBb5X6tZhiunkcEwiFX1qRQY8YZl3y3Du5M5xeQY5Nq9afcFHDZ9HaURw43gH27nAUWM36bMbMylwTFSzzNV8NO4v4ripe6Q15Vc2nPOFI";
		String tag2 = resourceExccedsNameLimit.getName();
		List<String> tagsList = new ArrayList<String>();
		tagsList.add(tag1);
		tagsList.add(tag2);

		resourceExccedsNameLimit.setTags(tagsList);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExccedsNameLimit, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_SINGLE_TAG_EXCEED_LIMIT, "" + ValidationUtils.TAG_MAX_LENGTH);

	}

	private void testTagsNoServiceName() {
		Resource serviceExccedsNameLimit = createResourceObject(false);
		String tag1 = "afzs2qLBb";
		List<String> tagsList = new ArrayList<String>();
		tagsList.add(tag1);
		serviceExccedsNameLimit.setTags(tagsList);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(serviceExccedsNameLimit, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_TAGS_NO_COMP_NAME);

	}

	private void testInvalidTag() {
		Resource serviceExccedsNameLimit = createResourceObject(false);
		String tag1 = "afzs2qLBb%#%";
		List<String> tagsList = new ArrayList<String>();
		tagsList.add(tag1);
		serviceExccedsNameLimit.setTags(tagsList);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(serviceExccedsNameLimit, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.INVALID_FIELD_FORMAT, new String[] { "Resource", "tag" });

	}

	// Resource tags - stop
	// Resource contact info start
	private void testContactIdTooLong() {
		Resource resourceContactId = createResourceObject(false);
		// 7 chars instead of 6
		String contactIdTooLong = "yrt1234";
		resourceContactId.setContactId(contactIdTooLong);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceContactId, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_CONTACT, ComponentTypeEnum.RESOURCE.getValue());
	}

	private void testContactIdWrongFormatCreate() {
		Resource resourceContactId = createResourceObject(false);
		// 3 letters and 3 digits
		String contactIdTooLong = "yrt134";
		resourceContactId.setContactId(contactIdTooLong);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceContactId, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_CONTACT, ComponentTypeEnum.RESOURCE.getValue());
	}

	private void testResourceContactIdEmpty() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setContactId("");

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_CONTACT, ComponentTypeEnum.RESOURCE.getValue());
	}

	private void testResourceContactIdMissing() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setContactId(null);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_CONTACT, ComponentTypeEnum.RESOURCE.getValue());
	}

	private void testVendorNameExceedsLimit() {
		Resource resourceExccedsVendorNameLimit = createResourceObject(false);
		String tooLongVendorName = "h1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9E";
		resourceExccedsVendorNameLimit.setVendorName(tooLongVendorName);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExccedsVendorNameLimit, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.VENDOR_NAME_EXCEEDS_LIMIT, "" + ValidationUtils.VENDOR_NAME_MAX_LENGTH);
	}

	private void testVendorNameWrongFormatCreate() {
		Resource resource = createResourceObject(false);
		// contains *
		String nameWrongFormat = "ljg*fd";
		resource.setVendorName(nameWrongFormat);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resource, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.INVALID_VENDOR_NAME);
	}

	private void testVendorReleaseWrongFormat() {
		Resource resource = createResourceObject(false);
		// contains >
		String nameWrongFormat = "1>2";
		resource.setVendorRelease(nameWrongFormat);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resource, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.INVALID_VENDOR_RELEASE);

	}

	private void testVendorReleaseExceedsLimitCreate() {
		Resource resourceExccedsNameLimit = createResourceObject(false);
		String tooLongVendorRelease = "h1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9E";
		resourceExccedsNameLimit.setVendorRelease(tooLongVendorRelease);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExccedsNameLimit, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.VENDOR_RELEASE_EXCEEDS_LIMIT, "" + ValidationUtils.VENDOR_RELEASE_MAX_LENGTH);
	}

	private void testResourceVendorNameMissing() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setVendorName(null);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.MISSING_VENDOR_NAME);
	}

	private void testResourceVendorReleaseMissing() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setVendorRelease(null);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.MISSING_VENDOR_RELEASE);
	}

	// Resource vendor name/release stop
	// Category start
	private void testResourceCategoryExist() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setCategories(null);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
	}

	private void testResourceBadCategoryCreate() {

		Resource resourceExist = createResourceObject(false);
		resourceExist.setCategories(null);
		resourceExist.addCategory("koko", "koko");

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
	}

	// Category stop
	// Cost start
	private void testHappyScenarioCostLicenseType() {
		Resource createResourceObject = createResourceObject(false);
		Resource createResourceObjectAfterCreate = createResourceObject(true);
		// Adding cost and licenseType to basic mock
		Either<Resource, StorageOperationStatus> eitherCreate = Either.left(createResourceObjectAfterCreate);
		when(resourceOperation.createResource(Mockito.any(Resource.class), Mockito.anyBoolean())).thenReturn(eitherCreate);

		String cost = "123.456";
		String licenseType = "User";
		createResourceObject.setCost(cost);
		createResourceObject.setLicenseType(licenseType);
		Either<Resource, ResponseFormat> createResponse = bl.createResource(createResourceObject, user, null, null);

		if (createResponse.isRight()) {
			assertEquals(new Integer(200), createResponse.right().value().getStatus());
		}
		createResourceObjectAfterCreate.setCost(cost);
		createResourceObjectAfterCreate.setLicenseType(licenseType);
		assertEquals(createResourceObjectAfterCreate, createResponse.left().value());
	}

	private void testCostWrongFormatCreate() {
		Resource resourceCost = createResourceObject(false);
		// Comma instead of fullstop
		String cost = "12356,464";
		resourceCost.setCost(cost);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceCost, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.INVALID_CONTENT);
	}

	// Cost stop
	// License type start
	private void testLicenseTypeWrongFormatCreate() {
		Resource resourceLicenseType = createResourceObject(false);
		// lowcase
		String licenseType = "cpu";
		resourceLicenseType.setLicenseType(licenseType);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceLicenseType, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.INVALID_CONTENT);
	}

	// License type stop
	// Derived from start
	private void testResourceTemplateNotExist() {
		Resource resourceExist = createResourceObject(false);
		List<String> list = null;
		resourceExist.setDerivedFrom(list);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.MISSING_DERIVED_FROM_TEMPLATE);
	}

	private void testResourceTemplateEmpty() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setDerivedFrom(new ArrayList<String>());

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.MISSING_DERIVED_FROM_TEMPLATE);
	}

	private void testResourceTemplateInvalid() {
		Resource resourceExist = createResourceObject(false);
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add("kuku");
		resourceExist.setDerivedFrom(derivedFrom);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.PARENT_RESOURCE_NOT_FOUND);
	}
	// Derived from stop

	private void assertResponse(Either<Resource, ResponseFormat> createResponse, ActionStatus expectedStatus, String... variables) {
		ResponseFormat expectedResponse = responseManager.getResponseFormat(expectedStatus, variables);
		ResponseFormat actualResponse = createResponse.right().value();
		assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		assertEquals("assert error description", expectedResponse.getFormattedMessage(), actualResponse.getFormattedMessage());
	}

	// UPDATE tests - start
	// Resource name
	@Test
	public void testResourceNameWrongFormat_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Resource, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		// when(resourceOperation.getResource_tx(resource.getUniqueId(),false)).thenReturn(eitherUpdate);
		when(resourceOperation.getResource(resource.getUniqueId(), false)).thenReturn(eitherUpdate);
		// contains *
		String nameWrongFormat = "ljg*fd";
		updatedResource.setName(nameWrongFormat);

		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(), updatedResource, null, user, false);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.INVALID_COMPONENT_NAME, ComponentTypeEnum.RESOURCE.getValue());

	}

	@Test
	public void testResourceNameAfterCertify_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Resource, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		// when(resourceOperation.getResource_tx(resource.getUniqueId(),false)).thenReturn(eitherUpdate);
		when(resourceOperation.getResource(resource.getUniqueId(), false)).thenReturn(eitherUpdate);

		String name = "ljg";
		updatedResource.setName(name);
		resource.setVersion("1.0");
		;

		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(), updatedResource, null, user, false);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.RESOURCE_NAME_CANNOT_BE_CHANGED);

	}

	@Test
	@Ignore
	public void testResourceNameExceedsLimit_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Resource, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		// when(resourceOperation.getResource_tx(resource.getUniqueId(),false)).thenReturn(eitherUpdate);
		when(resourceOperation.getResource(resource.getUniqueId(), false)).thenReturn(eitherUpdate);

		// 51 chars, the limit is 50
		String tooLongResourceName = "zCRCAWjqte0DtgcAAMmcJcXeNubeX1p1vOZNTShAHOYNAHvV3iK";
		updatedResource.setName(tooLongResourceName);

		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(), updatedResource, null, user, false);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_NAME_EXCEEDS_LIMIT, ComponentTypeEnum.RESOURCE.getValue(), "" + ValidationUtils.COMPONENT_NAME_MAX_LENGTH);
	}

	@Test
	public void testResourceNameAlreadyExist_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Resource, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		// when(resourceOperation.getResource_tx(resource.getUniqueId(),false)).thenReturn(eitherUpdate);
		when(resourceOperation.getResource(resource.getUniqueId(), false)).thenReturn(eitherUpdate);

		String resourceName = "alreadyExists";
		updatedResource.setName(resourceName);

		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(), updatedResource, null, user, false);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_NAME_ALREADY_EXIST, ComponentTypeEnum.RESOURCE.getValue(), resourceName);
	}

	//

	@Test
	public void testResourceDescExceedsLimit_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Resource, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		// when(resourceOperation.getResource_tx(resource.getUniqueId(),false)).thenReturn(eitherUpdate);
		when(resourceOperation.getResource(resource.getUniqueId(), false)).thenReturn(eitherUpdate);

		// 1025 chars, the limit is 1024
		String tooLongResourceDesc = "1GUODojQ0sGzKR4NP7e5j82ADQ3KHTVOaezL95qcbuaqDtjZhAQGQ3iFwKAy580K4WiiXs3u3zq7RzXcSASl5fm0RsWtCMOIDP"
				+ "AOf9Tf2xtXxPCuCIMCR5wOGnNTaFxgnJEHAGxilBhZDgeMNHmCN1rMK5B5IRJOnZxcpcL1NeG3APTCIMP1lNAxngYulDm9heFSBc8TfXAADq7703AvkJT0QPpGq2z2P"
				+ "tlikcAnIjmWgfC5Tm7UH462BAlTyHg4ExnPPL4AO8c92VrD7kZSgSqiy73cN3gLT8uigkKrUgXQFGVUFrXVyyQXYtVM6bLBeuCGQf4C2j8lkNg6M0J3PC0PzMRoinOxk"
				+ "Ae2teeCtVcIj4A1KQo3210j8q2v7qQU69Mabsa6DT9FgE4rcrbiFWrg0Zto4SXWD3o1eJA9o29lTg6kxtklH3TuZTmpi5KVp1NFhS1RpnqF83tzv4mZLKsx7Zh1fEgYvRFwx1"
				+ "ar3RolyDfNoZiGBGTMsZzz7RPFBf2hTnLmNqVGQnHKhhGj0Y5s8t2cbqbO2nmHiJb9uaUVrCGypgbAcJL3KPOBfAVW8PcpmNj4yVjI3L4x5zHjmGZbp9vKshEQODcrmcgsYAoKqe"
				+ "uu5u7jk8XVxEfQ0m5qL8UOErXPlJovSmKUmP5B5T0w299zIWDYCzSoNasHpHjOMDLAiDDeHbozUOn9t3Qou00e9POq4RMM0VnIx1H38nJoJZz2XH8CI5YMQe7oTagaxgQTF2aa0qaq2"
				+ "V6nJsfRGRklGjNhFFYP2cS4Xv2IJO9DSX6LTXOmENrGVJJvMOZcvnBaZPfoAHN0LU4i1SoepLzulIxnZBfkUWFJgZ5wQ0Bco2GC1HMqzW21rwy4XHRxXpXbmW8LVyoA1KbnmVmROycU4"
				+ "scTZ62IxIcIWCVeMjBIcTviXULbPUyqlfEPXWr8IMJtpAaELWgyquPClAREMDs2b9ztKmUeXlMccFES1XWbFTrhBHhmmDyVReEgCwfokrUFR13LTUK1k8I6OEHOs";
		updatedResource.setDescription(tooLongResourceDesc);

		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(), updatedResource, null, user, false);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_DESCRIPTION_EXCEEDS_LIMIT, ComponentTypeEnum.RESOURCE.getValue(), "" + ValidationUtils.COMPONENT_DESCRIPTION_MAX_LENGTH);

	}

	@Test
	public void testIconWrongFormat_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Resource, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		// when(resourceOperation.getResource_tx(resource.getUniqueId(),false)).thenReturn(eitherUpdate);
		when(resourceOperation.getResource(resource.getUniqueId(), false)).thenReturn(eitherUpdate);

		// contains .
		String icon = "icon.jpg";
		updatedResource.setIcon(icon);

		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(), updatedResource, null, user, false);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_ICON, ComponentTypeEnum.RESOURCE.getValue());

	}

	@Test
	public void testIconAfterCertify_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Resource, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		// when(resourceOperation.getResource_tx(resource.getUniqueId(),false)).thenReturn(eitherUpdate);
		when(resourceOperation.getResource(resource.getUniqueId(), false)).thenReturn(eitherUpdate);

		// contains
		String icon = "icon";
		updatedResource.setIcon(icon);

		resource.setVersion("1.0");
		;

		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(), updatedResource, null, user, false);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.RESOURCE_ICON_CANNOT_BE_CHANGED);

	}

	@Test
	public void testTagsExceedsLimit_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Resource, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		// when(resourceOperation.getResource_tx(resource.getUniqueId(),false)).thenReturn(eitherUpdate);
		when(resourceOperation.getResource(resource.getUniqueId(), false)).thenReturn(eitherUpdate);

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
		tagsList.add(resource.getName());

		updatedResource.setTags(tagsList);

		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(), updatedResource, null, user, false);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_TAGS_EXCEED_LIMIT, "" + ValidationUtils.TAG_LIST_MAX_LENGTH);
	}

	@Test
	public void testVendorNameWrongFormat_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Resource, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		// when(resourceOperation.getResource_tx(resource.getUniqueId(),false)).thenReturn(eitherUpdate);
		when(resourceOperation.getResource(resource.getUniqueId(), false)).thenReturn(eitherUpdate);

		// contains *
		String nameWrongFormat = "ljg*fd";
		updatedResource.setVendorName(nameWrongFormat);

		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(), updatedResource, null, user, false);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.INVALID_VENDOR_NAME);

	}

	@Test
	public void testVendorNameAfterCertify_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Resource, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		// when(resourceOperation.getResource_tx(resource.getUniqueId(),false)).thenReturn(eitherUpdate);
		when(resourceOperation.getResource(resource.getUniqueId(), false)).thenReturn(eitherUpdate);

		// contains *
		String nameWrongFormat = "ljg*fd";
		updatedResource.setVendorName(nameWrongFormat);
		resource.setVersion("1.0");
		;

		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(), updatedResource, null, user, false);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.RESOURCE_VENDOR_NAME_CANNOT_BE_CHANGED);

	}

	@Test
	public void testVendorReleaseExceedsLimit_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Resource, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		// when(resourceOperation.getResource_tx(resource.getUniqueId(),false)).thenReturn(eitherUpdate);
		when(resourceOperation.getResource(resource.getUniqueId(), false)).thenReturn(eitherUpdate);

		// 129 chars, the limit is 128
		String tooLongVendorRelease = "h1KSyJh9EspI8SPwAGu4VETfqWejeanuB1PCJBxJmJncYnrW0lnsEFFVRIukRJkwlOVnZCy8p38tjhANeZq3BGMHIawWR6ICl8Wi9mikRYALWgvJug00JrlQ0iPVKPLxy";
		updatedResource.setVendorRelease(tooLongVendorRelease);

		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(), updatedResource, null, user, false);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.VENDOR_RELEASE_EXCEEDS_LIMIT, "" + ValidationUtils.VENDOR_RELEASE_MAX_LENGTH);
	}

	// 1610OS Support - Because of changes in the validation in the ui this test needs to be fixed
//	@Test
//	public void testContactIdWrongFormat_UPDATE() {
//		Resource resource = createResourceObject(true);
//		Resource updatedResource = createResourceObject(true);
//
//		// this is in order to prevent failing with 403 earlier
//		Either<Resource, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
//		// when(resourceOperation.getResource_tx(resource.getUniqueId(),false)).thenReturn(eitherUpdate);
//		when(resourceOperation.getResource(resource.getUniqueId(), false)).thenReturn(eitherUpdate);
//
//		String resourceId = resource.getUniqueId();
//		// 3 letters and 3 digits
//		String contactIdTooLong = "yrt134";
//		updatedResource.setContactId(contactIdTooLong);
//		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resourceId, updatedResource, null, user, false);
//		assertTrue(createResponse.isRight());
//
//		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_CONTACT, ComponentTypeEnum.RESOURCE.getValue());
//	}	

	@Test
	public void testResourceBadCategory_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Resource, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		// when(resourceOperation.getResource_tx(resource.getUniqueId(),false)).thenReturn(eitherUpdate);
		when(resourceOperation.getResource(resource.getUniqueId(), false)).thenReturn(eitherUpdate);

		String resourceId = resource.getUniqueId();
		String badCategory = "ddfds";
		updatedResource.setCategories(null);
		updatedResource.addCategory(badCategory, "fikt");
		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resourceId, updatedResource, null, user, false);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
	}

	@Test
	public void testResourceCategoryAfterCertify_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Resource, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		// when(resourceOperation.getResource_tx(resource.getUniqueId(),false)).thenReturn(eitherUpdate);
		when(resourceOperation.getResource(resource.getUniqueId(), false)).thenReturn(eitherUpdate);

		String resourceId = resource.getUniqueId();
		updatedResource.setCategories(null);
		updatedResource.addCategory(RESOURCE_CATEGORY1, UPDATED_SUBCATEGORY);
		resource.setVersion("1.0");
		;

		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resourceId, updatedResource, null, user, false);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.RESOURCE_CATEGORY_CANNOT_BE_CHANGED);
	}

	// Derived from start
	@Test
	public void testResourceTemplateNotExist_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Resource, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		// when(resourceOperation.getResource_tx(resource.getUniqueId(),false)).thenReturn(eitherUpdate);
		when(resourceOperation.getResource(resource.getUniqueId(), false)).thenReturn(eitherUpdate);
		String resourceId = resource.getUniqueId();

		List<String> list = null;
		updatedResource.setDerivedFrom(list);

		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resourceId, updatedResource, null, user, false);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.MISSING_DERIVED_FROM_TEMPLATE);
	}

	@Test
	public void testResourceTemplateEmpty_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);
		String resourceId = resource.getUniqueId();

		// this is in order to prevent failing with 403 earlier
		Either<Resource, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		// when(resourceOperation.getResource_tx(resource.getUniqueId(),false)).thenReturn(eitherUpdate);
		when(resourceOperation.getResource(resource.getUniqueId(), false)).thenReturn(eitherUpdate);

		updatedResource.setDerivedFrom(new ArrayList<String>());

		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resourceId, updatedResource, null, user, false);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.MISSING_DERIVED_FROM_TEMPLATE);
	}

	@Test
	public void testResourceTemplateInvalid_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);
		String resourceId = resource.getUniqueId();

		// this is in order to prevent failing with 403 earlier
		Either<Resource, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		// when(resourceOperation.getResource_tx(resource.getUniqueId(),false)).thenReturn(eitherUpdate);
		when(resourceOperation.getResource(resource.getUniqueId(), false)).thenReturn(eitherUpdate);

		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add("kuku");
		updatedResource.setDerivedFrom(derivedFrom);

		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resourceId, updatedResource, null, user, false);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.PARENT_RESOURCE_NOT_FOUND);
	}

	@Test
	public void testResourceTemplateCertify_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);
		String resourceId = resource.getUniqueId();

		// this is in order to prevent failing with 403 earlier
		Either<Resource, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		// when(resourceOperation.getResource_tx(resource.getUniqueId(),false)).thenReturn(eitherUpdate);
		when(resourceOperation.getResource(resource.getUniqueId(), false)).thenReturn(eitherUpdate);
		resource.setVersion("1.0");
		;

		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add("tosca.nodes.Root");
		updatedResource.setDerivedFrom(derivedFrom);

		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resourceId, updatedResource, null, user, false);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.RESOURCE_DERIVED_FROM_CANNOT_BE_CHANGED);
	}
	// Derived from stop

	@Test
	public void createOrUpdateResourceAlreadyCheckout() {
		Resource resourceExist = createResourceObject(false);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist, user, null, null);

		createResponse.left().value().setLastUpdaterUserId(user.getUserId());
		assertTrue(createResponse.isLeft());

		Either<Resource, StorageOperationStatus> getLatestResult = Either.left(createResponse.left().value());
		when(resourceOperation.getLatestByName(resourceExist.getName(), true)).thenReturn(getLatestResult);
		when(resourceOperation.overrideResource(Mockito.any(Resource.class), Mockito.any(Resource.class), Mockito.anyBoolean())).thenReturn(getLatestResult);

		Resource resourceToUpdtae = createResourceObject(false);

		Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> createOrUpdateResource = bl.createOrUpdateResourceByImport(resourceToUpdtae, user, false, false, false);
		assertTrue(createOrUpdateResource.isLeft());

		Mockito.verify(resourceOperation, Mockito.times(1)).overrideResource(Mockito.any(Resource.class), Mockito.any(Resource.class), Mockito.anyBoolean());
		Mockito.verify(lifecycleBl, Mockito.times(0)).changeState(Mockito.anyString(), Mockito.eq(user), Mockito.eq(LifeCycleTransitionEnum.CHECKOUT), Mockito.any(LifecycleChangeInfoWithAction.class), Mockito.anyBoolean(), Mockito.anyBoolean());

	}

	@Test
	public void createOrUpdateResourceCertified() {
		Resource resourceExist = createResourceObject(false);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist, user, null, null);

		assertTrue(createResponse.isLeft());
		Resource certifiedResource = createResponse.left().value();
		certifiedResource.setLifecycleState(LifecycleStateEnum.CERTIFIED);
		certifiedResource.setVersion("1.0");
		;

		Either<Resource, StorageOperationStatus> getLatestResult = Either.left(certifiedResource);
		when(resourceOperation.getLatestByName(resourceExist.getName(), true)).thenReturn(getLatestResult);
		when(resourceOperation.overrideResource(Mockito.any(Resource.class), Mockito.any(Resource.class), Mockito.anyBoolean())).thenReturn(getLatestResult);

		when(lifecycleBl.changeState(Mockito.anyString(), Mockito.eq(user), Mockito.eq(LifeCycleTransitionEnum.CHECKOUT), Mockito.any(LifecycleChangeInfoWithAction.class), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(createResponse);

		Resource resourceToUpdtae = createResourceObject(false);

		Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> createOrUpdateResource = bl.createOrUpdateResourceByImport(resourceToUpdtae, user, false, false, false);
		assertTrue(createOrUpdateResource.isLeft());

		Mockito.verify(resourceOperation, Mockito.times(1)).overrideResource(Mockito.any(Resource.class), Mockito.any(Resource.class), Mockito.anyBoolean());
		Mockito.verify(lifecycleBl, Mockito.times(1)).changeState(Mockito.anyString(), Mockito.eq(user), Mockito.eq(LifeCycleTransitionEnum.CHECKOUT), Mockito.any(LifecycleChangeInfoWithAction.class), Mockito.anyBoolean(), Mockito.anyBoolean());

	}

	@Test
	public void createOrUpdateResourceNotExist() {
		Resource resourceToUpdtae = createResourceObject(false);

		Either<Resource, StorageOperationStatus> getLatestResult = Either.right(StorageOperationStatus.NOT_FOUND);
		when(resourceOperation.getLatestByName(resourceToUpdtae.getName(), true)).thenReturn(getLatestResult);

		Either<Resource, StorageOperationStatus> getLatestToscaNameResult = Either.right(StorageOperationStatus.NOT_FOUND);
		when(resourceOperation.getLatestByToscaResourceName(resourceToUpdtae.getToscaResourceName(), true)).thenReturn(getLatestResult);

		Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> createOrUpdateResource = bl.createOrUpdateResourceByImport(resourceToUpdtae, user, false, false, false);
		assertTrue(createOrUpdateResource.isLeft());

		Mockito.verify(resourceOperation, Mockito.times(0)).overrideResource(Mockito.any(Resource.class), Mockito.any(Resource.class), Mockito.anyBoolean());
		Mockito.verify(lifecycleBl, Mockito.times(0)).changeState(Mockito.anyString(), Mockito.eq(user), Mockito.eq(LifeCycleTransitionEnum.CHECKOUT), Mockito.any(LifecycleChangeInfoWithAction.class), Mockito.anyBoolean(), Mockito.anyBoolean());

	}

	@Test
	public void testValidatePropertiesDefaultValues_SuccessfullWithoutProperties() {
		Resource basic = createResourceObject(true);

		Either<Boolean, ResponseFormat> validatePropertiesDefaultValues = bl.validatePropertiesDefaultValues(basic);
		assertTrue(validatePropertiesDefaultValues.isLeft());
	}

	@Test
	public void testValidatePropertiesDefaultValues_SuccessfullWithProperties() {
		Resource basic = createResourceObject(true);
		PropertyDefinition property = new PropertyDefinition();
		property.setName("myProperty");
		property.setType(ToscaPropertyType.INTEGER.getType());
		property.setDefaultValue("1");
		List<PropertyDefinition> properties = new ArrayList<>();
		properties.add(property);
		basic.setProperties(properties);
		when(propertyOperation.isPropertyTypeValid(property)).thenReturn(true);
		when(propertyOperation.isPropertyDefaultValueValid(property, emptyDataTypes)).thenReturn(true);
		Either<Boolean, ResponseFormat> validatePropertiesDefaultValues = bl.validatePropertiesDefaultValues(basic);
		assertTrue(validatePropertiesDefaultValues.isLeft());
	}

	@Test
	public void testValidatePropertiesDefaultValues_FailedWithProperties() {
		Resource basic = createResourceObject(true);
		PropertyDefinition property = new PropertyDefinition();
		property.setName("myProperty");
		property.setType(ToscaPropertyType.INTEGER.getType());
		property.setDefaultValue("1.5");
		List<PropertyDefinition> properties = new ArrayList<>();
		properties.add(property);
		basic.setProperties(properties);

		when(propertyOperation.isPropertyDefaultValueValid(property, emptyDataTypes)).thenReturn(false);
		Either<Boolean, ResponseFormat> validatePropertiesDefaultValues = bl.validatePropertiesDefaultValues(basic);
		assertTrue(validatePropertiesDefaultValues.isRight());
	}

	@Test
	public void testDeleteMarkedResourcesNoResources() {
		List<String> ids = new ArrayList<String>();
		Either<List<String>, StorageOperationStatus> eitherNoResources = Either.left(ids);
		when(resourceOperation.getAllComponentsMarkedForDeletion()).thenReturn(eitherNoResources);

		Either<List<String>, ResponseFormat> deleteMarkedResources = bl.deleteMarkedComponents();
		assertTrue(deleteMarkedResources.isLeft());
		assertTrue(deleteMarkedResources.left().value().isEmpty());

		Mockito.verify(artifactManager, Mockito.times(0)).deleteAllComponentArtifactsIfNotOnGraph(Mockito.anyList());

	}

	@Test
	public void testDeleteMarkedResources() {
		List<String> ids = new ArrayList<String>();
		String resourceInUse = "123";
		ids.add(resourceInUse);
		String resourceFree = "456";
		ids.add(resourceFree);
		Either<List<String>, StorageOperationStatus> eitherNoResources = Either.left(ids);
		when(resourceOperation.getAllComponentsMarkedForDeletion()).thenReturn(eitherNoResources);

		Either<Boolean, StorageOperationStatus> resourceInUseResponse = Either.left(true);
		Either<Boolean, StorageOperationStatus> resourceFreeResponse = Either.left(false);

		List<ArtifactDefinition> artifacts = new ArrayList<ArtifactDefinition>();
		Either<List<ArtifactDefinition>, StorageOperationStatus> getArtifactsResponse = Either.left(artifacts);
		when(resourceOperation.getComponentArtifactsForDelete(resourceFree, NodeTypeEnum.Resource, true)).thenReturn(getArtifactsResponse);

		when(resourceOperation.isComponentInUse(resourceFree)).thenReturn(resourceFreeResponse);
		when(resourceOperation.isComponentInUse(resourceInUse)).thenReturn(resourceInUseResponse);

		Either<Component, StorageOperationStatus> eitherDelete = Either.left(new Resource());
		when(resourceOperation.deleteComponent(resourceFree, true)).thenReturn(eitherDelete);

		when(artifactManager.deleteAllComponentArtifactsIfNotOnGraph(artifacts)).thenReturn(StorageOperationStatus.OK);

		Either<List<String>, ResponseFormat> deleteMarkedResources = bl.deleteMarkedComponents();
		assertTrue(deleteMarkedResources.isLeft());
		List<String> resourceIdList = deleteMarkedResources.left().value();
		assertFalse(resourceIdList.isEmpty());
		assertTrue(resourceIdList.contains(resourceFree));
		assertFalse(resourceIdList.contains(resourceInUse));

		Mockito.verify(artifactManager, Mockito.times(1)).deleteAllComponentArtifactsIfNotOnGraph(artifacts);
	}

}
