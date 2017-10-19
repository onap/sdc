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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
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
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.datamodel.api.HighestFilterEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentMetadataDefinition;
import org.openecomp.sdc.be.model.CsarInfo;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.ParsedToscaYamlInfo;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.UploadCapInfo;
import org.openecomp.sdc.be.model.UploadComponentInstanceInfo;
import org.openecomp.sdc.be.model.UploadReqInfo;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.NodeTypeOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.ICacheMangerOperation;
import org.openecomp.sdc.be.model.operations.api.ICapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IPropertyOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CacheMangerOperation;
import org.openecomp.sdc.be.model.operations.impl.CsarOperation;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.tosca.CsarUtils.NonMetaArtifactInfo;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.user.IUserBusinessLogic;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import com.att.nsa.cambria.test.support.CambriaBatchingPublisherMock.Entry;

import fj.data.Either;

public class ResourceBusinessLogicTest {

	private static Logger log = LoggerFactory.getLogger(ResourceBusinessLogicTest.class.getName());
	public static final String RESOURCE_CATEGORY = "Network Layer 2-3/Router";
	public static final String RESOURCE_CATEGORY1 = "Network Layer 2-3";
	public static final String RESOURCE_SUBCATEGORY = "Router";

	public static final String UPDATED_CATEGORY = "Network Layer 2-3/Gateway";
	public static final String UPDATED_SUBCATEGORY = "Gateway";

	public static final String RESOURCE_NAME = "My-Resource_Name with   space";
	private static final String GENERIC_VF_NAME = "org.openecomp.resource.abstract.nodes.VF";
	private static final String GENERIC_VFC_NAME = "org.openecomp.resource.abstract.nodes.VFC";
	private static final String GENERIC_PNF_NAME = "org.openecomp.resource.abstract.nodes.PNF";

	final ServletContext servletContext = Mockito.mock(ServletContext.class);
	IAuditingManager iAuditingManager = null;
	IElementOperation mockElementDao;
	TitanDao mockTitanDao = Mockito.mock(TitanDao.class);
	UserBusinessLogic mockUserAdmin = Mockito.mock(UserBusinessLogic.class);
	ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
	NodeTypeOperation nodeTypeOperation = Mockito.mock(NodeTypeOperation.class);
	NodeTemplateOperation nodeTemplateOperation = Mockito.mock(NodeTemplateOperation.class);
	TopologyTemplateOperation topologyTemplateOperation = Mockito.mock(TopologyTemplateOperation.class);
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
	Resource genericVF = null;
	Resource genericVFC = null;
	Resource genericPNF = null;
	ComponentsUtils componentsUtils = new ComponentsUtils();
	ArtifactsBusinessLogic artifactManager = new ArtifactsBusinessLogic();
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
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
				appConfigDir);
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
		when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR))
				.thenReturn(webAppContextWrapper);
		when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webAppContext);
		when(webAppContext.getBean(IElementOperation.class)).thenReturn(mockElementDao);

		Either<Integer, StorageOperationStatus> eitherCountRoot = Either.left(1);
		Either<Boolean, StorageOperationStatus> eitherFalse = Either.left(true);
		when(toscaOperationFacade.validateComponentNameExists("Root", ResourceTypeEnum.VFC, ComponentTypeEnum.RESOURCE))
				.thenReturn(eitherFalse);

		Either<Boolean, StorageOperationStatus> eitherCountExist = Either.left(true);
		when(toscaOperationFacade.validateComponentNameExists("alreadyExists", ResourceTypeEnum.VFC,
				ComponentTypeEnum.RESOURCE)).thenReturn(eitherCountExist);

		Either<Boolean, StorageOperationStatus> eitherCount = Either.left(false);
		when(toscaOperationFacade.validateComponentNameExists(RESOURCE_NAME, ResourceTypeEnum.VFC,
				ComponentTypeEnum.RESOURCE)).thenReturn(eitherCount);
		when(toscaOperationFacade.validateComponentNameExists(RESOURCE_NAME, ResourceTypeEnum.VF,
				ComponentTypeEnum.RESOURCE)).thenReturn(eitherCount);
		when(toscaOperationFacade.validateComponentNameExists(RESOURCE_NAME, ResourceTypeEnum.PNF,
				ComponentTypeEnum.RESOURCE)).thenReturn(eitherCount);

		Either<Boolean, StorageOperationStatus> validateDerivedExists = Either.left(true);
		when(toscaOperationFacade.validateToscaResourceNameExists("Root")).thenReturn(validateDerivedExists);

		Either<Boolean, StorageOperationStatus> validateDerivedNotExists = Either.left(false);
		when(toscaOperationFacade.validateToscaResourceNameExists("kuku")).thenReturn(validateDerivedNotExists);
		when(graphLockOperation.lockComponent(Mockito.anyString(), Mockito.eq(NodeTypeEnum.Resource)))
				.thenReturn(StorageOperationStatus.OK);
		when(graphLockOperation.lockComponentByName(Mockito.anyString(), Mockito.eq(NodeTypeEnum.Resource)))
				.thenReturn(StorageOperationStatus.OK);

		// createResource
		resourceResponse = createResourceObject(true);
		Either<Resource, StorageOperationStatus> eitherCreate = Either.left(resourceResponse);
		Either<Integer, StorageOperationStatus> eitherValidate = Either.left(null);
		when(toscaOperationFacade.createToscaComponent(Mockito.any(Resource.class))).thenReturn(eitherCreate);
		when(toscaOperationFacade.validateCsarUuidUniqueness(Mockito.anyString())).thenReturn(eitherValidate);
		Map<String, DataTypeDefinition> emptyDataTypes = new HashMap<String, DataTypeDefinition>();
		when(applicationDataTypeCache.getAll()).thenReturn(Either.left(emptyDataTypes));

		// BL object
		artifactManager.nodeTemplateOperation = nodeTemplateOperation;
		bl = new ResourceBusinessLogic();
		bl.setElementDao(mockElementDao);
		bl.setUserAdmin(mockUserAdmin);
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
		toscaOperationFacade.setNodeTypeOperation(nodeTypeOperation);
		toscaOperationFacade.setTopologyTemplateOperation(topologyTemplateOperation);
		bl.setToscaOperationFacade(toscaOperationFacade);
		Resource resourceCsar = createResourceObjectCsar(true);
		setCanWorkOnResource(resourceCsar);
		Either<Component, StorageOperationStatus> oldResourceRes = Either.left(resourceCsar);
		when(toscaOperationFacade.getToscaElement(resourceCsar.getUniqueId())).thenReturn(oldResourceRes);
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
			resource.setUniqueId(resource.getName().toLowerCase() + ":" + resource.getVersion());
			resource.setCreatorUserId(user.getUserId());
			resource.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
			resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		}
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
		resource.setResourceVendorModelNumber("");
		resource.setContactId("ya5467");
		resource.setIcon("MyIcon");
		resource.setCsarUUID("valid_vf.csar");
		resource.setCsarVersion("1");

		if (afterCreate) {
			resource.setName(resource.getName());
			resource.setVersion("0.1");

			resource.setUniqueId(resource.getName().toLowerCase() + ":" + resource.getVersion());
			resource.setCreatorUserId(user.getUserId());
			resource.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
			resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		}
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
		Either<Resource, ResponseFormat> createResponse = bl.createResource(resource,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);

		if (createResponse.isRight()) {
			assertEquals(new Integer(200), createResponse.right().value().getStatus());
		}
		assertEquals(createResourceObject(true), createResponse.left().value());
	}

	@Test
	public void testUpdateHappyScenario() {
		Resource resource = createResourceObjectCsar(true);
		setCanWorkOnResource(resource);

		Either<Resource, StorageOperationStatus> resourceLinkedToCsarRes = Either.left(resource);
		when(toscaOperationFacade.getLatestComponentByCsarOrName(ComponentTypeEnum.RESOURCE, resource.getCsarUUID(),
				resource.getSystemName())).thenReturn(resourceLinkedToCsarRes);
		Either<Boolean, StorageOperationStatus> validateDerivedExists = Either.left(true);
		when(toscaOperationFacade.validateToscaResourceNameExists("Root")).thenReturn(validateDerivedExists);

		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);
		Either<Resource, ResponseFormat> updateResponse = bl.validateAndUpdateResourceFromCsar(resource, user, null,
				null, resource.getUniqueId());
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
		testTagsNoServiceName();
		testInvalidTag();

		testContactIdTooLong();
		testContactIdWrongFormatCreate();
		testResourceContactIdEmpty();
		testResourceContactIdMissing();
		testVendorNameExceedsLimit();
		testVendorNameWrongFormatCreate();
		testVendorReleaseWrongFormat();
		testVendorReleaseExceedsLimitCreate();
		testResourceVendorModelNumberExceedsLimit();
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

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertResponse(createResponse, ActionStatus.COMPONENT_NAME_ALREADY_EXIST, ComponentTypeEnum.RESOURCE.getValue(),
				resourceName);
	}

	private void testResourceNameEmpty() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setName(null);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertResponse(createResponse, ActionStatus.MISSING_COMPONENT_NAME, ComponentTypeEnum.RESOURCE.getValue());
	}

	private void testResourceNameExceedsLimit() {
		Resource resourceExccedsNameLimit = createResourceObject(false);
		// 51 chars, the limit is 50
		String tooLongResourceName = "zCRCAWjqte0DtgcAAMmcJcXeNubeX1p1vOZNTShAHOYNAHvV3iK";
		resourceExccedsNameLimit.setName(tooLongResourceName);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExccedsNameLimit,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_NAME_EXCEEDS_LIMIT, ComponentTypeEnum.RESOURCE.getValue(),
				"" + ValidationUtils.COMPONENT_NAME_MAX_LENGTH);
	}

	private void testResourceNameWrongFormat() {
		Resource resource = createResourceObject(false);
		// contains :
		String nameWrongFormat = "ljg?fd";
		resource.setName(nameWrongFormat);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resource,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
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

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExccedsDescLimit,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_DESCRIPTION_EXCEEDS_LIMIT,
				ComponentTypeEnum.RESOURCE.getValue(), "" + ValidationUtils.COMPONENT_DESCRIPTION_MAX_LENGTH);
	}

	private void testResourceDescNotEnglish() {
		Resource notEnglish = createResourceObject(false);
		// Not english
		String notEnglishDesc = "\uC2B5";
		notEnglish.setDescription(notEnglishDesc);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(notEnglish,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_DESCRIPTION,
				ComponentTypeEnum.RESOURCE.getValue());
	}

	private void testResourceDescriptionEmpty() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setDescription("");

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_DESCRIPTION,
				ComponentTypeEnum.RESOURCE.getValue());
	}

	private void testResourceDescriptionMissing() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setDescription(null);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_DESCRIPTION,
				ComponentTypeEnum.RESOURCE.getValue());
	}
	// Resource description - end
	// Resource icon start

	private void testResourceIconMissing() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setIcon(null);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_ICON, ComponentTypeEnum.RESOURCE.getValue());
	}

	private void testResourceIconInvalid() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setIcon("kjk3453^&");

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_ICON, ComponentTypeEnum.RESOURCE.getValue());
	}

	private void testResourceIconExceedsLimit() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setIcon("dsjfhskdfhskjdhfskjdhkjdhfkshdfksjsdkfhsdfsdfsdfsfsdfsf");

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_ICON_EXCEEDS_LIMIT, ComponentTypeEnum.RESOURCE.getValue(),
				"" + ValidationUtils.ICON_MAX_LENGTH);
	}

	// Resource icon end
	// Resource tags - start
	private void testResourceTagNotExist() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setTags(null);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_TAGS);
	}

	private void testResourceTagEmpty() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setTags(new ArrayList<String>());

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
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

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExccedsNameLimit,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_TAGS_EXCEED_LIMIT,
				"" + ValidationUtils.TAG_LIST_MAX_LENGTH);

	}

	private void testTagsSingleExceedsLimit() {
		Resource resourceExccedsNameLimit = createResourceObject(false);
		String tag1 = "afzs2qLBb5X6tZhiunkcEwiFX1qRQY8YZl3y3Du5M5xeQY5Nq9afcFHDZ9HaURw43gH27nAUWM36bMbMylwTFSzzNV8NO4v4ripe6Q15Vc2nPOFI";
		String tag2 = resourceExccedsNameLimit.getName();
		List<String> tagsList = new ArrayList<String>();
		tagsList.add(tag1);
		tagsList.add(tag2);

		resourceExccedsNameLimit.setTags(tagsList);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExccedsNameLimit,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_SINGLE_TAG_EXCEED_LIMIT,
				"" + ValidationUtils.TAG_MAX_LENGTH);

	}

	private void testTagsNoServiceName() {
		Resource serviceExccedsNameLimit = createResourceObject(false);
		String tag1 = "afzs2qLBb";
		List<String> tagsList = new ArrayList<String>();
		tagsList.add(tag1);
		serviceExccedsNameLimit.setTags(tagsList);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(serviceExccedsNameLimit,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_TAGS_NO_COMP_NAME);

	}

	private void testInvalidTag() {
		Resource serviceExccedsNameLimit = createResourceObject(false);
		String tag1 = "afzs2qLBb%#%";
		List<String> tagsList = new ArrayList<String>();
		tagsList.add(tag1);
		serviceExccedsNameLimit.setTags(tagsList);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(serviceExccedsNameLimit,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.INVALID_FIELD_FORMAT, new String[] { "Resource", "tag" });

	}

	// Resource tags - stop
	// Resource contact start

	private void testContactIdTooLong() {
		Resource resourceContactId = createResourceObject(false);
		// 59 chars instead of 50
		String contactIdTooLong = "thisNameIsVeryLongAndExeccedsTheNormalLengthForContactId";
		resourceContactId.setContactId(contactIdTooLong);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceContactId,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_CONTACT, ComponentTypeEnum.RESOURCE.getValue());
	}

	private void testContactIdWrongFormatCreate() {
		Resource resourceContactId = createResourceObject(false);
		// 3 letters and 3 digits and special characters
		String contactIdFormatWrong = "yrt134!!!";
		resourceContactId.setContactId(contactIdFormatWrong);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceContactId,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_CONTACT, ComponentTypeEnum.RESOURCE.getValue());
	}

	private void testResourceContactIdEmpty() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setContactId("");

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_CONTACT, ComponentTypeEnum.RESOURCE.getValue());
	}

	private void testResourceContactIdMissing() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setContactId(null);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_CONTACT, ComponentTypeEnum.RESOURCE.getValue());
	}

	private void testVendorNameExceedsLimit() {
		Resource resourceExccedsVendorNameLimit = createResourceObject(false);
		String tooLongVendorName = "h1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9E";
		resourceExccedsVendorNameLimit.setVendorName(tooLongVendorName);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExccedsVendorNameLimit,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.VENDOR_NAME_EXCEEDS_LIMIT,
				"" + ValidationUtils.VENDOR_NAME_MAX_LENGTH);
	}

	private void testResourceVendorModelNumberExceedsLimit() {
		Resource resourceExccedsVendorModelNumberLimit = createResourceObject(false);
		String tooLongVendorModelNumber = "h1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9E";
		resourceExccedsVendorModelNumberLimit.setResourceVendorModelNumber(tooLongVendorModelNumber);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExccedsVendorModelNumberLimit,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.RESOURCE_VENDOR_MODEL_NUMBER_EXCEEDS_LIMIT,
				"" + ValidationUtils.RESOURCE_VENDOR_MODEL_NUMBER_MAX_LENGTH);
	}

	private void testVendorNameWrongFormatCreate() {
		Resource resource = createResourceObject(false);
		// contains *
		String nameWrongFormat = "ljg*fd";
		resource.setVendorName(nameWrongFormat);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resource,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.INVALID_VENDOR_NAME);
	}

	private void testVendorReleaseWrongFormat() {
		Resource resource = createResourceObject(false);
		// contains >
		String nameWrongFormat = "1>2";
		resource.setVendorRelease(nameWrongFormat);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resource,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.INVALID_VENDOR_RELEASE);

	}

	private void testVendorReleaseExceedsLimitCreate() {
		Resource resourceExccedsNameLimit = createResourceObject(false);
		String tooLongVendorRelease = "h1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9E";
		resourceExccedsNameLimit.setVendorRelease(tooLongVendorRelease);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExccedsNameLimit,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.VENDOR_RELEASE_EXCEEDS_LIMIT,
				"" + ValidationUtils.VENDOR_RELEASE_MAX_LENGTH);
	}

	private void testResourceVendorNameMissing() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setVendorName(null);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.MISSING_VENDOR_NAME);
	}

	private void testResourceVendorReleaseMissing() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setVendorRelease(null);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.MISSING_VENDOR_RELEASE);
	}

	// Resource vendor name/release stop
	// Category start
	private void testResourceCategoryExist() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setCategories(null);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
	}

	private void testResourceBadCategoryCreate() {

		Resource resourceExist = createResourceObject(false);
		resourceExist.setCategories(null);
		resourceExist.addCategory("koko", "koko");

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
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
		when(toscaOperationFacade.createToscaComponent(Mockito.any(Resource.class))).thenReturn(eitherCreate);

		String cost = "123.456";
		String licenseType = "User";
		createResourceObject.setCost(cost);
		createResourceObject.setLicenseType(licenseType);
		Either<Resource, ResponseFormat> createResponse = bl.createResource(createResourceObject,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);

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

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceCost,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
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

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceLicenseType,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.INVALID_CONTENT);
	}

	// License type stop
	// Derived from start
	private void testResourceTemplateNotExist() {
		Resource resourceExist = createResourceObject(false);
		List<String> list = null;
		resourceExist.setDerivedFrom(list);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.MISSING_DERIVED_FROM_TEMPLATE);
	}

	private void testResourceTemplateEmpty() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setDerivedFrom(new ArrayList<String>());

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.MISSING_DERIVED_FROM_TEMPLATE);
	}

	private void testResourceTemplateInvalid() {
		Resource resourceExist = createResourceObject(false);
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add("kuku");
		resourceExist.setDerivedFrom(derivedFrom);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.PARENT_RESOURCE_NOT_FOUND);
	}
	// Derived from stop

	private void assertResponse(Either<Resource, ResponseFormat> createResponse, ActionStatus expectedStatus,
			String... variables) {
		ResponseFormat expectedResponse = responseManager.getResponseFormat(expectedStatus, variables);
		ResponseFormat actualResponse = createResponse.right().value();
		assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		assertEquals("assert error description", expectedResponse.getFormattedMessage(),
				actualResponse.getFormattedMessage());
	}

	// UPDATE tests - start
	// Resource name
	@Test
	public void testResourceNameWrongFormat_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);
		// contains *
		String nameWrongFormat = "ljg*fd";
		updatedResource.setName(nameWrongFormat);

		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);

		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(),
				updatedResource, null, user, false);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.INVALID_COMPONENT_NAME, ComponentTypeEnum.RESOURCE.getValue());

	}

	@Test
	public void testResourceNameAfterCertify_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		// when(resourceOperation.getResource_tx(resource.getUniqueId(),false)).thenReturn(eitherUpdate);
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);

		String name = "ljg";
		updatedResource.setName(name);
		resource.setVersion("1.0");

		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);

		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(),
				updatedResource, null, user, false);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.RESOURCE_NAME_CANNOT_BE_CHANGED);

	}

	@Ignore
	public void testResourceNameExceedsLimit_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);

		// 51 chars, the limit is 50
		String tooLongResourceName = "zCRCAWjqte0DtgcAAMmcJcXeNubeX1p1vOZNTShAHOYNAHvV3iK";
		updatedResource.setName(tooLongResourceName);
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);

		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(),
				updatedResource, null, user, false);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_NAME_EXCEEDS_LIMIT, ComponentTypeEnum.RESOURCE.getValue(),
				"" + ValidationUtils.COMPONENT_NAME_MAX_LENGTH);
	}

	@Test
	public void testResourceNameAlreadyExist_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);

		String resourceName = "alreadyExists";
		updatedResource.setName(resourceName);
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(updatedResource);
		when(toscaOperationFacade.updateToscaElement(updatedResource)).thenReturn(dataModelResponse);
		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(),
				updatedResource, null, user, false);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_NAME_ALREADY_EXIST, ComponentTypeEnum.RESOURCE.getValue(),
				resourceName);
	}

	//

	@Test
	public void testResourceDescExceedsLimit_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);

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
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);
		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(),
				updatedResource, null, user, false);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_DESCRIPTION_EXCEEDS_LIMIT,
				ComponentTypeEnum.RESOURCE.getValue(), "" + ValidationUtils.COMPONENT_DESCRIPTION_MAX_LENGTH);

	}

	@Test
	public void testIconWrongFormat_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);

		// contains .
		String icon = "icon.jpg";
		updatedResource.setIcon(icon);
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);

		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(),
				updatedResource, null, user, false);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_ICON, ComponentTypeEnum.RESOURCE.getValue());

	}

	@Test
	public void testIconAfterCertify_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);

		// contains
		String icon = "icon";
		updatedResource.setIcon(icon);

		resource.setVersion("1.0");
		;
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);
		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(),
				updatedResource, null, user, false);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.RESOURCE_ICON_CANNOT_BE_CHANGED);

	}

	@Test
	public void testTagsExceedsLimit_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);

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
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);
		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(),
				updatedResource, null, user, false);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_TAGS_EXCEED_LIMIT,
				"" + ValidationUtils.TAG_LIST_MAX_LENGTH);
	}

	@Test
	public void testVendorNameWrongFormat_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);

		// contains *
		String nameWrongFormat = "ljg*fd";
		updatedResource.setVendorName(nameWrongFormat);
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);
		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(),
				updatedResource, null, user, false);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.INVALID_VENDOR_NAME);

	}

	@Test
	public void testVendorNameAfterCertify_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);

		// contains *
		String nameWrongFormat = "ljg*fd";
		updatedResource.setVendorName(nameWrongFormat);
		resource.setVersion("1.0");
		;
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);
		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(),
				updatedResource, null, user, false);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.RESOURCE_VENDOR_NAME_CANNOT_BE_CHANGED);

	}

	@Test
	public void testVendorReleaseExceedsLimit_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);
		// 129 chars, the limit is 128
		String tooLongVendorRelease = "h1KSyJh9EspI8SPwAGu4VETfqWejeanuB1PCJBxJmJncYnrW0lnsEFFVRIukRJkwlOVnZCy8p38tjhANeZq3BGMHIawWR6ICl8Wi9mikRYALWgvJug00JrlQ0iPVKPLxy";
		updatedResource.setVendorRelease(tooLongVendorRelease);
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);
		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resource.getUniqueId(),
				updatedResource, null, user, false);
		assertTrue(createResponse.isRight());
		assertResponse(createResponse, ActionStatus.VENDOR_RELEASE_EXCEEDS_LIMIT,
				"" + ValidationUtils.VENDOR_RELEASE_MAX_LENGTH);
	}

	@Ignore
	public void testContactIdWrongFormat_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);

		String resourceId = resource.getUniqueId();
		// 3 letters and 3 digits
		String contactIdTooLong = "yrt134";
		updatedResource.setContactId(contactIdTooLong);
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);
		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resourceId, updatedResource, null,
				user, false);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_CONTACT, ComponentTypeEnum.RESOURCE.getValue());
	}

	@Test
	public void testResourceBadCategory_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);

		String resourceId = resource.getUniqueId();
		String badCategory = "ddfds";
		updatedResource.setCategories(null);
		updatedResource.addCategory(badCategory, "fikt");
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);
		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resourceId, updatedResource, null,
				user, false);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.COMPONENT_INVALID_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
	}

	@Test
	public void testResourceCategoryAfterCertify_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);

		String resourceId = resource.getUniqueId();
		updatedResource.setCategories(null);
		updatedResource.addCategory(RESOURCE_CATEGORY1, UPDATED_SUBCATEGORY);
		resource.setVersion("1.0");
		;
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);
		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resourceId, updatedResource, null,
				user, false);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.RESOURCE_CATEGORY_CANNOT_BE_CHANGED);
	}

	// Derived from start
	@Test
	public void testResourceTemplateNotExist_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);

		// this is in order to prevent failing with 403 earlier
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);
		String resourceId = resource.getUniqueId();

		List<String> list = null;
		updatedResource.setDerivedFrom(list);
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);
		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resourceId, updatedResource, null,
				user, false);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.MISSING_DERIVED_FROM_TEMPLATE);
	}

	@Test
	public void testResourceTemplateEmpty_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);
		String resourceId = resource.getUniqueId();

		// this is in order to prevent failing with 403 earlier
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);

		updatedResource.setDerivedFrom(new ArrayList<String>());
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);
		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resourceId, updatedResource, null,
				user, false);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.MISSING_DERIVED_FROM_TEMPLATE);
	}

	@Test
	public void testResourceTemplateInvalid_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);
		String resourceId = resource.getUniqueId();

		// this is in order to prevent failing with 403 earlier
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);

		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add("kuku");
		updatedResource.setDerivedFrom(derivedFrom);
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);
		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resourceId, updatedResource, null,
				user, false);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.PARENT_RESOURCE_NOT_FOUND);
	}

	@Test
	public void testResourceTemplateCertify_UPDATE_HAPPY() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);
		String resourceId = resource.getUniqueId();

		// this is in order to prevent failing with 403 earlier
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);

		Either<Boolean, StorageOperationStatus> isToscaNameExtending = Either.left(true);
		when(toscaOperationFacade.validateToscaResourceNameExtends(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(isToscaNameExtending);

		Either<Map<String, PropertyDefinition>, StorageOperationStatus> findPropertiesOfNode = Either
				.left(new HashMap<>());
		when(propertyOperation.deleteAllPropertiesAssociatedToNode(Mockito.any(NodeTypeEnum.class),
				Mockito.anyString())).thenReturn(findPropertiesOfNode);

		resource.setVersion("1.0");

		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add("tosca.nodes.Root");
		updatedResource.setDerivedFrom(derivedFrom);
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(updatedResource);
		when(toscaOperationFacade.updateToscaElement(updatedResource)).thenReturn(dataModelResponse);
		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resourceId, updatedResource, null,
				user, false);
		assertTrue(createResponse.isLeft());
	}

	@Test
	public void testResourceTemplateCertify_UPDATE_SAD() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);
		String resourceId = resource.getUniqueId();

		// this is in order to prevent failing with 403 earlier
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);

		Either<Boolean, StorageOperationStatus> isToscaNameExtending = Either.left(false);
		when(toscaOperationFacade.validateToscaResourceNameExtends(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(isToscaNameExtending);

		resource.setVersion("1.0");

		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add("tosca.nodes.Root");
		updatedResource.setDerivedFrom(derivedFrom);
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);
		Either<Resource, ResponseFormat> createResponse = bl.updateResourceMetadata(resourceId, updatedResource, null,
				user, false);
		assertTrue(createResponse.isRight());

		assertResponse(createResponse, ActionStatus.PARENT_RESOURCE_DOES_NOT_EXTEND);
	}
	// Derived from stop

	@Test
	public void createOrUpdateResourceAlreadyCheckout() {
		Resource resourceExist = createResourceObject(false);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);

		createResponse.left().value().setLastUpdaterUserId(user.getUserId());
		assertTrue(createResponse.isLeft());

		Either<Component, StorageOperationStatus> getLatestResult = Either.left(createResponse.left().value());
		when(toscaOperationFacade.getLatestByName(resourceExist.getName())).thenReturn(getLatestResult);
		when(toscaOperationFacade.overrideComponent(Mockito.any(Component.class), Mockito.any(Component.class)))
				.thenReturn(getLatestResult);

		Resource resourceToUpdtae = createResourceObject(false);

		Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> createOrUpdateResource = bl
				.createOrUpdateResourceByImport(resourceToUpdtae, user, false, false, false, null);
		assertTrue(createOrUpdateResource.isLeft());

		Mockito.verify(toscaOperationFacade, Mockito.times(1)).overrideComponent(Mockito.any(Resource.class),
				Mockito.any(Resource.class));
		Mockito.verify(lifecycleBl, Mockito.times(0)).changeState(Mockito.anyString(), Mockito.eq(user),
				Mockito.eq(LifeCycleTransitionEnum.CHECKOUT), Mockito.any(LifecycleChangeInfoWithAction.class),
				Mockito.anyBoolean(), Mockito.anyBoolean());

	}

	@Test
	public void createOrUpdateResourceCertified() {
		Resource resourceExist = createResourceObject(false);

		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);

		assertTrue(createResponse.isLeft());
		Resource certifiedResource = createResponse.left().value();
		certifiedResource.setLifecycleState(LifecycleStateEnum.CERTIFIED);
		certifiedResource.setVersion("1.0");

		Either<Component, StorageOperationStatus> getLatestResult = Either.left(certifiedResource);
		when(toscaOperationFacade.getLatestByName(resourceExist.getName())).thenReturn(getLatestResult);
		when(toscaOperationFacade.overrideComponent(Mockito.any(Component.class), Mockito.any(Component.class)))
				.thenReturn(getLatestResult);

		when(lifecycleBl.changeState(Mockito.anyString(), Mockito.eq(user),
				Mockito.eq(LifeCycleTransitionEnum.CHECKOUT), Mockito.any(LifecycleChangeInfoWithAction.class),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(createResponse);

		Resource resourceToUpdtae = createResourceObject(false);

		Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> createOrUpdateResource = bl
				.createOrUpdateResourceByImport(resourceToUpdtae, user, false, false, false, null);
		assertTrue(createOrUpdateResource.isLeft());

		Mockito.verify(toscaOperationFacade, Mockito.times(1)).overrideComponent(Mockito.any(Component.class),
				Mockito.any(Component.class));
		Mockito.verify(lifecycleBl, Mockito.times(1)).changeState(Mockito.anyString(), Mockito.eq(user),
				Mockito.eq(LifeCycleTransitionEnum.CHECKOUT), Mockito.any(LifecycleChangeInfoWithAction.class),
				Mockito.anyBoolean(), Mockito.anyBoolean());

	}

	@Test
	public void createOrUpdateResourceNotExist() {
		Resource resourceToUpdtae = createResourceObject(false);

		Either<Component, StorageOperationStatus> getLatestResult = Either.right(StorageOperationStatus.NOT_FOUND);
		when(toscaOperationFacade.getLatestByName(resourceToUpdtae.getName())).thenReturn(getLatestResult);

		Either<Component, StorageOperationStatus> getLatestToscaNameResult = Either
				.right(StorageOperationStatus.NOT_FOUND);
		when(toscaOperationFacade.getLatestByToscaResourceName(resourceToUpdtae.getToscaResourceName()))
				.thenReturn(getLatestToscaNameResult);

		Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> createOrUpdateResource = bl
				.createOrUpdateResourceByImport(resourceToUpdtae, user, false, false, false, null);
		assertTrue(createOrUpdateResource.isLeft());

		Mockito.verify(toscaOperationFacade, Mockito.times(0)).overrideComponent(Mockito.any(Component.class),
				Mockito.any(Component.class));
		Mockito.verify(lifecycleBl, Mockito.times(0)).changeState(Mockito.anyString(), Mockito.eq(user),
				Mockito.eq(LifeCycleTransitionEnum.CHECKOUT), Mockito.any(LifecycleChangeInfoWithAction.class),
				Mockito.anyBoolean(), Mockito.anyBoolean());

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

	// @Test
	// public void testDeleteMarkedResourcesNoResources() {
	// List<GraphVertex> ids = new ArrayList<>();
	// Either<List<GraphVertex>, StorageOperationStatus> eitherNoResources =
	// Either.left(ids);
	// when(topologyTemplateOperation.getAllComponentsMarkedForDeletion(ComponentTypeEnum.RESOURCE)).thenReturn(eitherNoResources);
	//
	// Either<List<String>, ResponseFormat> deleteMarkedResources =
	// bl.deleteMarkedComponents();
	// assertTrue(deleteMarkedResources.isLeft());
	// assertTrue(deleteMarkedResources.left().value().isEmpty());
	//
	// Mockito.verify(artifactManager,
	// Mockito.times(0)).deleteAllComponentArtifactsIfNotOnGraph(Mockito.anyList());
	//
	// }
	//
	// @Test
	// public void testDeleteMarkedResources() {
	// List<String> ids = new ArrayList<String>();
	// String resourceInUse = "123";
	// ids.add(resourceInUse);
	// String resourceFree = "456";
	// ids.add(resourceFree);
	// Either<List<String>, StorageOperationStatus> eitherNoResources =
	// Either.left(ids);
	// when(toscaOperationFacade.getAllComponentsMarkedForDeletion()).thenReturn(eitherNoResources);
	//
	// Either<Boolean, StorageOperationStatus> resourceInUseResponse =
	// Either.left(true);
	// Either<Boolean, StorageOperationStatus> resourceFreeResponse =
	// Either.left(false);
	//
	// List<ArtifactDefinition> artifacts = new ArrayList<ArtifactDefinition>();
	// Either<List<ArtifactDefinition>, StorageOperationStatus>
	// getArtifactsResponse = Either.left(artifacts);
	// when(toscaOperationFacade.getComponentArtifactsForDelete(resourceFree,
	// NodeTypeEnum.Resource, true)).thenReturn(getArtifactsResponse);
	//
	// when(toscaOperationFacade.isComponentInUse(resourceFree)).thenReturn(resourceFreeResponse);
	// when(toscaOperationFacade.isComponentInUse(resourceInUse)).thenReturn(resourceInUseResponse);
	//
	// Either<Component, StorageOperationStatus> eitherDelete = Either.left(new
	// Resource());
	// when(toscaOperationFacade.deleteToscaComponent(resourceFree)).thenReturn(eitherDelete);
	//
	// when(artifactManager.deleteAllComponentArtifactsIfNotOnGraph(artifacts)).thenReturn(StorageOperationStatus.OK);
	// List<String> deletedComponents = new ArrayList<>();
	// deletedComponents.add(resourceFree);
	// when(toscaOperationFacade.deleteMarkedElements(ComponentTypeEnum.RESOURCE)).thenReturn(Either.left(deletedComponents));
	//
	// Either<List<String>, ResponseFormat> deleteMarkedResources =
	// bl.deleteMarkedComponents();
	// assertTrue(deleteMarkedResources.isLeft());
	// List<String> resourceIdList = deleteMarkedResources.left().value();
	// assertFalse(resourceIdList.isEmpty());
	// assertTrue(resourceIdList.contains(resourceFree));
	// assertFalse(resourceIdList.contains(resourceInUse));
	//
	// Mockito.verify(artifactManager,
	// Mockito.times(1)).deleteAllComponentArtifactsIfNotOnGraph(artifacts);
	// }

	
	@SuppressWarnings("unchecked")
	@Test
	public void testFindVfCsarArtifactsToHandle() {

		Class<ResourceBusinessLogic> targetClass = ResourceBusinessLogic.class;
		String methodName = "findVfCsarArtifactsToHandle";
		Resource resource = new Resource();
		String deploymentArtifactToUpdateFileName = "deploymentArtifactToUpdate.yaml";
		String deploymentArtifactToDeleteFileName = "deploymentArtifactToDelete.yaml";
		String deploymentArtifactToCreateFileName = "deploymentArtifactToCreate.yaml";

		String artifactInfoToUpdateFileName = "infoArtifactToUpdate.yaml";
		String artifactInfoToDeleteFileName = "infoArtifactToDelete.yaml";
		String artifactInfoToCreateFileName = "infoArtifactToCreate.yaml";

		byte[] oldPayloadData = "oldPayloadData".getBytes();
		byte[] newPayloadData = "newPayloadData".getBytes();
		Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();

		ArtifactDefinition deploymentArtifactToUpdate = new ArtifactDefinition();
		deploymentArtifactToUpdate.setMandatory(false);
		deploymentArtifactToUpdate.setArtifactName(deploymentArtifactToUpdateFileName);
		deploymentArtifactToUpdate.setArtifactType("SNMP_POLL");
		deploymentArtifactToUpdate.setPayload(oldPayloadData);
		deploymentArtifactToUpdate
				.setArtifactChecksum(GeneralUtility.calculateMD5Base64EncodedByByteArray(oldPayloadData));

		ArtifactDefinition deploymentArtifactToDelete = new ArtifactDefinition();
		deploymentArtifactToDelete.setMandatory(false);
		deploymentArtifactToDelete.setArtifactName(deploymentArtifactToDeleteFileName);
		deploymentArtifactToDelete.setArtifactType("SNMP_TRAP");
		deploymentArtifactToDelete.setPayload(oldPayloadData);
		deploymentArtifactToDelete
				.setArtifactChecksum(GeneralUtility.calculateMD5Base64EncodedByByteArray(oldPayloadData));

		ArtifactDefinition deploymentArtifactToIgnore = new ArtifactDefinition();

		deploymentArtifacts.put(ValidationUtils.normalizeArtifactLabel(deploymentArtifactToUpdate.getArtifactName()),
				deploymentArtifactToUpdate);
		deploymentArtifacts.put(ValidationUtils.normalizeArtifactLabel(deploymentArtifactToDelete.getArtifactName()),
				deploymentArtifactToDelete);
		deploymentArtifacts.put("ignore", deploymentArtifactToIgnore);

		Map<String, ArtifactDefinition> artifacts = new HashMap<>();

		ArtifactDefinition artifactToUpdate = new ArtifactDefinition();
		artifactToUpdate.setMandatory(false);
		artifactToUpdate.setArtifactName(artifactInfoToUpdateFileName);
		artifactToUpdate.setArtifactType("SNMP_POLL");
		artifactToUpdate.setPayload(oldPayloadData);
		artifactToUpdate.setArtifactChecksum(GeneralUtility.calculateMD5Base64EncodedByByteArray(oldPayloadData));

		ArtifactDefinition artifactToDelete = new ArtifactDefinition();
		artifactToDelete.setMandatory(false);
		artifactToDelete.setArtifactName(artifactInfoToDeleteFileName);
		artifactToDelete.setArtifactType("SNMP_TRAP");
		artifactToDelete.setPayload(oldPayloadData);
		artifactToDelete.setArtifactChecksum(GeneralUtility.calculateMD5Base64EncodedByByteArray(oldPayloadData));

		ArtifactDefinition artifactToIgnore = new ArtifactDefinition();

		artifacts.put(ValidationUtils.normalizeArtifactLabel(artifactToUpdate.getArtifactName()), artifactToUpdate);
		artifacts.put(ValidationUtils.normalizeArtifactLabel(artifactToDelete.getArtifactName()), artifactToDelete);
		artifacts.put("ignore", artifactToIgnore);

		resource.setDeploymentArtifacts(deploymentArtifacts);
		resource.setArtifacts(artifacts);

		List<NonMetaArtifactInfo> artifactPathAndNameList = new ArrayList<>();
		NonMetaArtifactInfo deploymentArtifactInfoToUpdate = new NonMetaArtifactInfo(
				deploymentArtifactToUpdate.getArtifactName(), null,
				ArtifactTypeEnum.findType(deploymentArtifactToUpdate.getArtifactType()),
				ArtifactGroupTypeEnum.DEPLOYMENT, newPayloadData, deploymentArtifactToUpdate.getArtifactName());

		NonMetaArtifactInfo informationalArtifactInfoToUpdate = new NonMetaArtifactInfo(
				artifactToUpdate.getArtifactName(), null, ArtifactTypeEnum.findType(artifactToUpdate.getArtifactType()),
				ArtifactGroupTypeEnum.DEPLOYMENT, newPayloadData, artifactToUpdate.getArtifactName());

		NonMetaArtifactInfo deploymentArtifactInfoToCreate = new NonMetaArtifactInfo(deploymentArtifactToCreateFileName,
				null, ArtifactTypeEnum.OTHER, ArtifactGroupTypeEnum.DEPLOYMENT, newPayloadData,
				deploymentArtifactToCreateFileName);

		NonMetaArtifactInfo informationalArtifactInfoToCreate = new NonMetaArtifactInfo(artifactInfoToCreateFileName,
				null, ArtifactTypeEnum.OTHER, ArtifactGroupTypeEnum.DEPLOYMENT, newPayloadData,
				artifactInfoToCreateFileName);

		artifactPathAndNameList.add(deploymentArtifactInfoToUpdate);
		artifactPathAndNameList.add(informationalArtifactInfoToUpdate);
		artifactPathAndNameList.add(deploymentArtifactInfoToCreate);
		artifactPathAndNameList.add(informationalArtifactInfoToCreate);

		Object[] argObjects = { resource, artifactPathAndNameList, user };
		Class[] argClasses = { Resource.class, List.class, User.class };
		try {
			Method method = targetClass.getDeclaredMethod(methodName, argClasses);
			method.setAccessible(true);
			Either<EnumMap<ArtifactOperationEnum, List<NonMetaArtifactInfo>>, ResponseFormat> findVfCsarArtifactsToHandleRes = (Either<EnumMap<ArtifactOperationEnum, List<NonMetaArtifactInfo>>, ResponseFormat>) method
					.invoke(bl, argObjects);
			assertTrue(findVfCsarArtifactsToHandleRes.isLeft());
			EnumMap<ArtifactOperationEnum, List<NonMetaArtifactInfo>> foundVfArtifacts = findVfCsarArtifactsToHandleRes
					.left().value();
			assertTrue(foundVfArtifacts.get(ArtifactOperationEnum.Create).size() == 2);
			assertTrue(foundVfArtifacts.get(ArtifactOperationEnum.Update).size() == 2);
			assertTrue(foundVfArtifacts.get(ArtifactOperationEnum.Create).size() == 2);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testBuildNestedVfcToscaNamespace() {

		Class<ResourceBusinessLogic> targetClass = ResourceBusinessLogic.class;
		String methodName = "buildNestedVfcToscaNamespace";
		String nodeTypeFullName = "org.openecomp.resource.abstract.nodes.heat.FEAdd_On_Module_vLBAgentTemplate";
		String expectedNestedVfcToscaNamespace = "org.openecomp.resource.vfc.nodes.heat.FEAdd_On_Module_vLBAgentTemplate";
		Object[] argObjects = { nodeTypeFullName };
		Class[] argClasses = { String.class };
		try {
			Method method = targetClass.getDeclaredMethod(methodName, argClasses);
			method.setAccessible(true);
			String actualNestedVfcToscaNamespace = (String) method.invoke(bl, argObjects);
			assertTrue(!actualNestedVfcToscaNamespace.isEmpty());
			assertTrue(actualNestedVfcToscaNamespace.equals(expectedNestedVfcToscaNamespace));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testBuildNestedVfcToscaResourceName() {

		Class<ResourceBusinessLogic> targetClass = ResourceBusinessLogic.class;
		String methodName = "buildNestedVfcToscaResourceName";
		String vfResourceName = "vfname";
		String nodeTypeFullName = "org.openecomp.resource.abstract.nodes.heat.FEAdd_On_Module_vLBAgentTemplate";
		String expectedNestedVfcToscaResourceName = "org.openecomp.resource.vfc.vfname.abstract.nodes.heat.FEAdd_On_Module_vLBAgentTemplate";
		Object[] argObjects = { vfResourceName, nodeTypeFullName };
		Class[] argClasses = { String.class, String.class };
		try {
			Method method = targetClass.getDeclaredMethod(methodName, argClasses);
			method.setAccessible(true);
			String actualNestedVfcToscaResourceName = (String) method.invoke(bl, argObjects);
			assertTrue(!actualNestedVfcToscaResourceName.isEmpty());
			assertTrue(actualNestedVfcToscaResourceName.equals(expectedNestedVfcToscaResourceName));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testBuildNestedSubstituteYamlName() {

		Class<ResourceBusinessLogic> targetClass = ResourceBusinessLogic.class;
		String methodName = "buildNestedSubstituteYamlName";
		String nodeTypeFullName = "org.openecomp.resource.abstract.nodes.heat.FEAdd_On_Module_vLBAgentTemplate";
		String expectedNestedSubstituteYamlName = "Definitions/FEAdd_On_Module_vLBAgentTemplateServiceTemplate.yaml";
		Object[] argObjects = { nodeTypeFullName };
		Class[] argClasses = { String.class };
		try {
			Method method = targetClass.getDeclaredMethod(methodName, argClasses);
			method.setAccessible(true);
			String actualNestedSubstituteYamlName = (String) method.invoke(bl, argObjects);
			assertTrue(!actualNestedSubstituteYamlName.isEmpty());
			assertTrue(actualNestedSubstituteYamlName.equals(expectedNestedSubstituteYamlName));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testVFGeneratedInputs() {

		Resource resource = createVF();
		List<InputDefinition> inputs = resource.getInputs();
		assertTrue(8 == inputs.size());
		for (InputDefinition input : inputs) {
			assertNotNull(input.getOwnerId());
		}
		assertTrue(resource.getDerivedFromGenericType().equals(genericVF.getToscaResourceName()));
		assertTrue(resource.getDerivedFromGenericVersion().equals(genericVF.getVersion()));
	}

	@Test
	public void testVFUpdateGenericInputsToLatestOnCheckout() {

		// create a VF that is derived from generic version 1.0
		Resource resource = createVF();
		// create a new generic version without properties
		genericVF.setVersion("2.0");
		genericVF.setProperties(null);
		String currentDerivedFromVersion = resource.getDerivedFromGenericVersion();
		List<InputDefinition> currentInputs = resource.getInputs();
		// verify previous inputs ownerId fields exist - user may not delete
		// generated inputs
		assertTrue(8 == currentInputs.stream().filter(p -> null != p.getOwnerId()).collect(Collectors.toList()).size());
		Either<Boolean, ResponseFormat> upgradeToLatestGeneric = bl.shouldUpgradeToLatestGeneric(resource);
		// verify success
		assertTrue(upgradeToLatestGeneric.isLeft());
		// verify update required and valid
		assertTrue(upgradeToLatestGeneric.left().value());
		// verify version was upgraded
		assertFalse(resource.getDerivedFromGenericVersion().equals(currentDerivedFromVersion));
		// verify inputs were not deleted
		assertTrue(8 == resource.getInputs().size());
		// verify inputs ownerId fields were removed - user may delete/edit
		// inputs
		assertTrue(8 == resource.getInputs().stream().filter(p -> null == p.getOwnerId()).collect(Collectors.toList())
				.size());
	}

	@Test
	public void testVFUpdateGenericInputsToLatestOnCheckoutNotPerformed() {

		// create a VF that is derived from generic version 1.0
		Resource resource = createVF();

		// add an input to the VF
		PropertyDefinition newProp = new PropertyDefinition();
		newProp.setType("integer");
		newProp.setName("newProp");
		resource.getInputs().add(new InputDefinition(newProp));

		// create a new generic version with a new property which has the same
		// name as a user defined input on the VF with a different type
		genericVF.setVersion("2.0");
		newProp.setType("string");
		genericVF.setProperties(new ArrayList<PropertyDefinition>());
		genericVF.getProperties().add(newProp);

		String currentDerivedFromVersion = resource.getDerivedFromGenericVersion();
		assertTrue(8 == resource.getInputs().stream().filter(p -> null != p.getOwnerId()).collect(Collectors.toList())
				.size());
		Either<Boolean, ResponseFormat> upgradeToLatestGeneric = bl.shouldUpgradeToLatestGeneric(resource);
		// verify success
		assertTrue(upgradeToLatestGeneric.isLeft());
		// verify update is invalid an void
		assertFalse(upgradeToLatestGeneric.left().value());
		// verify version was not upgraded
		assertTrue(resource.getDerivedFromGenericVersion().equals(currentDerivedFromVersion));
		// verify inputs were not removed
		assertTrue(9 == resource.getInputs().size());
		// verify user defined input exists
		assertTrue(1 == resource.getInputs().stream().filter(p -> null == p.getOwnerId()).collect(Collectors.toList())
				.size());
		assertTrue(resource.getInputs().stream().filter(p -> null == p.getOwnerId()).findAny().get().getType()
				.equals("integer"));
	}

	@Test
	public void testPNFGeneratedInputsNoGeneratedInformationalArtifacts() {

		Resource resource = createPNF();
		List<InputDefinition> inputs = resource.getInputs();
		assertTrue(8 == inputs.size());
		for (InputDefinition input : inputs) {
			assertNotNull(input.getOwnerId());
		}
		assertTrue(resource.getDerivedFromGenericType().equals(genericPNF.getToscaResourceName()));
		assertTrue(resource.getDerivedFromGenericVersion().equals(genericPNF.getVersion()));
		assertTrue(0 == resource.getArtifacts().size());
	}

	private Resource createVF() {

		genericVF = setupGenericTypeMock(GENERIC_VF_NAME);
		when(toscaOperationFacade.getLatestCertifiedNodeTypeByToscaResourceName(GENERIC_VF_NAME))
				.thenReturn(Either.left(genericVF));
		Resource resource = createResourceObject(true);
		resource.setDerivedFrom(null);
		resource.setResourceType(ResourceTypeEnum.VF);
		when(toscaOperationFacade.createToscaComponent(resource)).thenReturn(Either.left(resource));
		Either<Resource, ResponseFormat> createResponse = bl.createResource(resource,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isLeft());
		return createResponse.left().value();
	}

	private Resource createPNF() {

		genericPNF = setupGenericTypeMock(GENERIC_PNF_NAME);
		when(toscaOperationFacade.getLatestCertifiedNodeTypeByToscaResourceName(GENERIC_PNF_NAME))
				.thenReturn(Either.left(genericPNF));
		Resource resource = createResourceObject(true);
		resource.setDerivedFrom(null);
		resource.setResourceType(ResourceTypeEnum.PNF);
		when(toscaOperationFacade.createToscaComponent(resource)).thenReturn(Either.left(resource));
		Either<Resource, ResponseFormat> createResponse = bl.createResource(resource,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isLeft());
		return createResponse.left().value();
	}

	private Resource setupGenericTypeMock(String toscaName) {

		Resource genericType = createResourceObject(true);
		genericType.setVersion("1.0");
		genericType.setToscaResourceName(toscaName);
		String[] propNames = { "nf_function", "nf_role", "nf_naming_code", "nf_type", "nf_naming",
				"availability_zone_max_count", "min_instances", "max_instances" };
		String[] propTypes = { "string", "string", "string", "string", "org.openecomp.datatypes.Naming", "integer",
				"integer", "integer" };
		List<PropertyDefinition> genericProps = new ArrayList<>();
		for (int i = 0; i < 8; ++i) {
			PropertyDefinition prop = new PropertyDefinition();
			prop.setName(propNames[i]);
			prop.setType(propTypes[i]);
			genericProps.add(prop);
		}
		genericType.setProperties(genericProps);
		return genericType;
	}

	private ResourceBusinessLogic createTestSubject() {
		return new ResourceBusinessLogic();
	}

	@Test
	public void testGetCsarOperation() throws Exception {
		ResourceBusinessLogic testSubject;
		CsarOperation result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCsarOperation();
	}

	@Test
	public void testSetCsarOperation() throws Exception {
		ResourceBusinessLogic testSubject;
		CsarOperation csarOperation = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCsarOperation(csarOperation);
	}

	
	@Test
	public void testGetLifecycleBusinessLogic() throws Exception {
		ResourceBusinessLogic testSubject;
		LifecycleBusinessLogic result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLifecycleBusinessLogic();
	}

	
	@Test
	public void testSetLifecycleManager() throws Exception {
		ResourceBusinessLogic testSubject;
		LifecycleBusinessLogic lifecycleBusinessLogic = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setLifecycleManager(lifecycleBusinessLogic);
	}

	
	@Test
	public void testGetElementDao() throws Exception {
		ResourceBusinessLogic testSubject;
		IElementOperation result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getElementDao();
	}

	
	@Test
	public void testSetElementDao() throws Exception {
		ResourceBusinessLogic testSubject;
		IElementOperation elementDao = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setElementDao(elementDao);
	}

	
	@Test
	public void testGetUserAdmin() throws Exception {
		ResourceBusinessLogic testSubject;
		IUserBusinessLogic result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUserAdmin();
	}

	
	@Test
	public void testSetUserAdmin() throws Exception {
		ResourceBusinessLogic testSubject;
		UserBusinessLogic userAdmin = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setUserAdmin(userAdmin);
	}

	
	@Test
	public void testGetComponentsUtils() throws Exception {
		ResourceBusinessLogic testSubject;
		ComponentsUtils result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentsUtils();
	}

	
	@Test
	public void testSetComponentsUtils() throws Exception {
		ResourceBusinessLogic testSubject;
		ComponentsUtils componentsUtils = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentsUtils(componentsUtils);
	}

	
	@Test
	public void testGetArtifactsManager() throws Exception {
		ResourceBusinessLogic testSubject;
		ArtifactsBusinessLogic result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactsManager();
	}

	
	@Test
	public void testSetArtifactsManager() throws Exception {
		ResourceBusinessLogic testSubject;
		ArtifactsBusinessLogic artifactsManager = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactsManager(artifactsManager);
	}

	
	@Test
	public void testSetPropertyOperation() throws Exception {
		ResourceBusinessLogic testSubject;
		IPropertyOperation propertyOperation = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setPropertyOperation(propertyOperation);
	}

	
	@Test
	public void testGetApplicationDataTypeCache() throws Exception {
		ResourceBusinessLogic testSubject;
		ApplicationDataTypeCache result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getApplicationDataTypeCache();
	}

	
	@Test
	public void testSetApplicationDataTypeCache() throws Exception {
		ResourceBusinessLogic testSubject;
		ApplicationDataTypeCache applicationDataTypeCache = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setApplicationDataTypeCache(applicationDataTypeCache);
	}
	
	@Test
	public void testSetDeploymentArtifactsPlaceHolder() throws Exception {
		ResourceBusinessLogic testSubject;
		Component component = new Resource() {
		};
		User user = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDeploymentArtifactsPlaceHolder(component, user);
	}


	
	@Test
	public void testValidateVendorReleaseName_1() throws Exception {
		ResourceBusinessLogic testSubject;
		String vendorRelease = "";
		Either<Boolean, ResponseFormat> result;

		// test 1
		testSubject = createTestSubject();
		vendorRelease = null;
		result = testSubject.validateVendorReleaseName(vendorRelease);
		Assert.assertEquals(false, result.left().value());

	}

	



	
	@Test
	public void testGetCapabilityTypeOperation() throws Exception {
		ResourceBusinessLogic testSubject;
		ICapabilityTypeOperation result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilityTypeOperation();
	}

	
	@Test
	public void testSetCapabilityTypeOperation() throws Exception {
		ResourceBusinessLogic testSubject;
		ICapabilityTypeOperation capabilityTypeOperation = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilityTypeOperation(capabilityTypeOperation);
	}

	
	@Test
	public void testValidatePropertiesDefaultValues() throws Exception {
		ResourceBusinessLogic testSubject;
		Resource resource = new Resource();
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validatePropertiesDefaultValues(resource);
	}

	
	@Test
	public void testGetComponentInstanceBL() throws Exception {
		ResourceBusinessLogic testSubject;
		ComponentInstanceBusinessLogic result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstanceBL();
	}


	
	@Test
	public void testGetComponentInstancesFilteredByPropertiesAndInputs() throws Exception {
		ResourceBusinessLogic testSubject;
		String componentId = "";
		ComponentTypeEnum componentTypeEnum = null;
		String userId = "";
		String searchText = "";
		Either<List<ComponentInstance>, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstancesFilteredByPropertiesAndInputs(componentId, componentTypeEnum, userId,
				searchText);
	}


	
	@Test
	public void testGetCacheManagerOperation() throws Exception {
		ResourceBusinessLogic testSubject;
		ICacheMangerOperation result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCacheManagerOperation();
	}

	
	@Test
	public void testSetCacheManagerOperation() throws Exception {
		ResourceBusinessLogic testSubject;
		ICacheMangerOperation cacheManagerOperation = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCacheManagerOperation(cacheManagerOperation);
	}


}
