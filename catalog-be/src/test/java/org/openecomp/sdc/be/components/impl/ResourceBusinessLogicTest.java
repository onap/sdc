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

import fj.data.Either;
import mockit.Deencapsulation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.ElementOperationMock;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.InterfaceOperationTestUtils;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationInfo;
import org.openecomp.sdc.be.components.impl.generic.GenericTypeBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datamodel.api.HighestFilterEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.CsarInfo;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.UploadCapInfo;
import org.openecomp.sdc.be.model.UploadComponentInstanceInfo;
import org.openecomp.sdc.be.model.UploadPropInfo;
import org.openecomp.sdc.be.model.UploadReqInfo;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.NodeTypeOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.ICacheMangerOperation;
import org.openecomp.sdc.be.model.operations.api.ICapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.IPropertyOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CacheMangerOperation;
import org.openecomp.sdc.be.model.operations.impl.CsarOperation;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.tosca.CsarUtils.NonMetaArtifactInfo;
import org.openecomp.sdc.be.user.IUserBusinessLogic;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class ResourceBusinessLogicTest implements InterfaceOperationTestUtils {

	private static final Logger log = LoggerFactory.getLogger(ResourceBusinessLogicTest.class);
	public static final String RESOURCE_CATEGORY = "Network Layer 2-3/Router";
	public static final String RESOURCE_CATEGORY1 = "Network Layer 2-3";
	public static final String RESOURCE_SUBCATEGORY = "Router";

	public static final String UPDATED_CATEGORY = "Network Layer 2-3/Gateway";
	public static final String UPDATED_SUBCATEGORY = "Gateway";

	private String resourceId = "resourceId1";
	private String operationId = "uniqueId1";
	Resource resourceUpdate;

	public static final String RESOURCE_NAME = "My-Resource_Name with   space";
	private static final String GENERIC_VF_NAME = "org.openecomp.resource.abstract.nodes.VF";
	private static final String GENERIC_CR_NAME = "org.openecomp.resource.abstract.nodes.CR";
	private static final String GENERIC_VFC_NAME = "org.openecomp.resource.abstract.nodes.VFC";
	private static final String GENERIC_PNF_NAME = "org.openecomp.resource.abstract.nodes.PNF";

	final ServletContext servletContext = Mockito.mock(ServletContext.class);
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
	UserValidations userValidations = Mockito.mock(UserValidations.class);
	WebApplicationContext webAppContext = Mockito.mock(WebApplicationContext.class);
	IInterfaceLifecycleOperation interfaceTypeOperation = Mockito.mock(IInterfaceLifecycleOperation.class);
	InterfaceOperation interfaceOperation = Mockito.mock(InterfaceOperation.class);

	@InjectMocks
	ResourceBusinessLogic bl = new ResourceBusinessLogic();
	ResponseFormatManager responseManager = null;
	GraphLockOperation graphLockOperation = Mockito.mock(GraphLockOperation.class);
	User user = null;
	Resource resourceResponse = null;
	Resource genericVF = null;
	Resource genericCR = null;
	Resource genericVFC = null;
	Resource genericPNF = null;
	ComponentsUtils componentsUtils;
	ArtifactsBusinessLogic artifactManager = new ArtifactsBusinessLogic();
	CsarOperation csarOperation = Mockito.mock(CsarOperation.class);
	Map<String, DataTypeDefinition> emptyDataTypes = new HashMap<String, DataTypeDefinition>();
	private GenericTypeBusinessLogic genericTypeBusinessLogic = Mockito.mock(GenericTypeBusinessLogic.class);
	CacheMangerOperation cacheManager = Mockito.mock(CacheMangerOperation.class);

	public ResourceBusinessLogicTest() {

	}

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.reset(propertyOperation);

		ExternalConfiguration.setAppName("catalog-be");

		// init Configuration
		String appConfigDir = "src/test/resources/config/catalog-be";
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
				appConfigDir);
		ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
		componentsUtils = new ComponentsUtils(Mockito.mock(AuditingManager.class));

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
		when(userValidations.validateUserExists(eq(user.getUserId()), anyString(), eq(false)))
				.thenReturn(Either.left(user));
		when(userValidations.validateUserNotEmpty(eq(user), anyString())).thenReturn(Either.left(user));
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
		when(toscaOperationFacade.validateComponentNameExists(eq(RESOURCE_NAME), any(ResourceTypeEnum.class),
				eq(ComponentTypeEnum.RESOURCE))).thenReturn(eitherCount);
		when(interfaceOperation.updateInterface(anyString(), anyObject()))
				.thenReturn(Either.left(mockInterfaceDefinitionToReturn(RESOURCE_NAME)));
		Either<Boolean, StorageOperationStatus> validateDerivedExists = Either.left(true);
		when(toscaOperationFacade.validateToscaResourceNameExists("Root")).thenReturn(validateDerivedExists);

		Either<Boolean, StorageOperationStatus> validateDerivedNotExists = Either.left(false);
		when(toscaOperationFacade.validateToscaResourceNameExists("kuku")).thenReturn(validateDerivedNotExists);
		when(graphLockOperation.lockComponent(Mockito.anyString(), eq(NodeTypeEnum.Resource)))
				.thenReturn(StorageOperationStatus.OK);
		when(graphLockOperation.lockComponentByName(Mockito.anyString(), eq(NodeTypeEnum.Resource)))
				.thenReturn(StorageOperationStatus.OK);

		// createResource
		resourceResponse = createResourceObject(true);
		Either<Resource, StorageOperationStatus> eitherCreate = Either.left(resourceResponse);
		Either<Integer, StorageOperationStatus> eitherValidate = Either.left(null);
		when(toscaOperationFacade.createToscaComponent(any(Resource.class))).thenReturn(eitherCreate);
		when(toscaOperationFacade.validateCsarUuidUniqueness(Mockito.anyString())).thenReturn(eitherValidate);
		Map<String, DataTypeDefinition> emptyDataTypes = new HashMap<String, DataTypeDefinition>();
		when(applicationDataTypeCache.getAll()).thenReturn(Either.left(emptyDataTypes));
		when(mockTitanDao.commit()).thenReturn(TitanOperationStatus.OK);

		// BL object
		artifactManager.setNodeTemplateOperation(nodeTemplateOperation);
		bl = new ResourceBusinessLogic();
		bl.setElementDao(mockElementDao);
		bl.setUserAdmin(mockUserAdmin);
		bl.setCapabilityTypeOperation(capabilityTypeOperation);
		bl.setComponentsUtils(componentsUtils);
		bl.setLifecycleManager(lifecycleBl);
		bl.setGraphLockOperation(graphLockOperation);
		bl.setArtifactsManager(artifactManager);
		bl.setPropertyOperation(propertyOperation);
		bl.setTitanGenericDao(mockTitanDao);
		bl.setApplicationDataTypeCache(applicationDataTypeCache);
		bl.setCsarOperation(csarOperation);
		bl.setCacheManagerOperation(cacheManager);
		bl.setGenericTypeBusinessLogic(genericTypeBusinessLogic);
		toscaOperationFacade.setNodeTypeOperation(nodeTypeOperation);
		toscaOperationFacade.setTopologyTemplateOperation(topologyTemplateOperation);
		bl.setToscaOperationFacade(toscaOperationFacade);
		bl.setUserValidations(userValidations);
		bl.setInterfaceTypeOperation(interfaceTypeOperation);
		bl.setInterfaceOperation(interfaceOperation);

		Resource resourceCsar = createResourceObjectCsar(true);
		setCanWorkOnResource(resourceCsar);
		Either<Component, StorageOperationStatus> oldResourceRes = Either.left(resourceCsar);
		when(toscaOperationFacade.getToscaFullElement(resourceCsar.getUniqueId())).thenReturn(oldResourceRes);
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
		validateUserRoles(Role.ADMIN, Role.DESIGNER);
		Resource resource = createResourceObject(false);
		Either<Resource, ResponseFormat> createResponse = bl.createResource(resource,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);

		if (createResponse.isRight()) {
			assertEquals(new Integer(200), createResponse.right().value().getStatus());
		}
		assertEquals(createResourceObject(true), createResponse.left().value());
	}

	@Test
	public void testCsarUUIDnotEmpty() {
		validateUserRoles(Role.ADMIN, Role.DESIGNER);
		Resource resource = createResourceObject(false);
		resource.setCsarUUID("asasaas");
		Either<Integer, StorageOperationStatus> eitherUpdate = Either.left(2);
		when(toscaOperationFacade.validateCsarUuidUniqueness("asasaas")).thenReturn(eitherUpdate);
		Either<Resource, ResponseFormat> createResponse = bl.createResource(resource,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);

	}

	@Test
	public void testUpdateHappyScenario() {
		Resource resource = createResourceObjectCsar(true);
		setCanWorkOnResource(resource);
		validateUserRoles(Role.ADMIN, Role.DESIGNER);
		Either<Resource, StorageOperationStatus> resourceLinkedToCsarRes = Either.left(resource);
		when(toscaOperationFacade.getLatestComponentByCsarOrName(ComponentTypeEnum.RESOURCE, resource.getCsarUUID(),
				resource.getSystemName())).thenReturn(resourceLinkedToCsarRes);
		Either<Boolean, StorageOperationStatus> validateDerivedExists = Either.left(true);
		when(toscaOperationFacade.validateToscaResourceNameExists("Root")).thenReturn(validateDerivedExists);
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);
		Either<Resource, ResponseFormat> updateResponse = bl.validateAndUpdateResourceFromCsar(resource, user, null,
				null, resource.getUniqueId());
		if (updateResponse.isRight()) {
			assertEquals(new Integer(200), updateResponse.right().value().getStatus());
		}
		assertEquals(resource.getUniqueId(), updateResponse.left().value().getUniqueId());
	}


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
		validateUserRoles(Role.ADMIN, Role.DESIGNER);
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
		when(toscaOperationFacade.createToscaComponent(any(Resource.class))).thenReturn(eitherCreate);

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
		resource.setInterfaces(createMockInterfaceDefinition(RESOURCE_NAME));
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
		resource.setInterfaces(createMockInterfaceDefinition(RESOURCE_NAME));
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
		resource.setInterfaces(createMockInterfaceDefinition(RESOURCE_NAME));

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
	public void testVendorNameWrongFormat() {
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
		assertResponse(createResponse, ActionStatus.INVALID_VENDOR_NAME);

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
		when(propertyOperation.deleteAllPropertiesAssociatedToNode(any(NodeTypeEnum.class), Mockito.anyString()))
				.thenReturn(findPropertiesOfNode);

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
		validateUserRoles(Role.ADMIN, Role.DESIGNER);
		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);

		createResponse.left().value().setLastUpdaterUserId(user.getUserId());
		assertTrue(createResponse.isLeft());

		Either<Resource, StorageOperationStatus> getLatestResult = Either.left(createResponse.left().value());
		Either<Component, StorageOperationStatus> getCompLatestResult = Either.left(createResponse.left().value());
		when(toscaOperationFacade.getLatestByName(resourceExist.getName())).thenReturn(getCompLatestResult);
		when(toscaOperationFacade.overrideComponent(any(Resource.class), any(Resource.class)))
				.thenReturn(getLatestResult);

		Resource resourceToUpdtae = createResourceObject(false);

		Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> createOrUpdateResource = bl
				.createOrUpdateResourceByImport(resourceToUpdtae, user, false, false, false, null, null, false);
		assertTrue(createOrUpdateResource.isLeft());

		Mockito.verify(toscaOperationFacade, Mockito.times(1)).overrideComponent(any(Resource.class),
				any(Resource.class));
		Mockito.verify(lifecycleBl, Mockito.times(0)).changeState(Mockito.anyString(), eq(user),
				eq(LifeCycleTransitionEnum.CHECKOUT), any(LifecycleChangeInfoWithAction.class), Mockito.anyBoolean(),
				Mockito.anyBoolean());

	}

	@Test
	public void createOrUpdateResourceCertified() {
		Resource resourceExist = createResourceObject(false);
		validateUserRoles(Role.ADMIN, Role.DESIGNER);
		Either<Resource, ResponseFormat> createResponse = bl.createResource(resourceExist,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);

		assertTrue(createResponse.isLeft());
		Resource certifiedResource = createResponse.left().value();
		certifiedResource.setLifecycleState(LifecycleStateEnum.CERTIFIED);
		certifiedResource.setVersion("1.0");

		Either<Resource, StorageOperationStatus> getLatestResult = Either.left(certifiedResource);
		Either<Component, StorageOperationStatus> getCompLatestResult = Either.left(createResponse.left().value());
		when(toscaOperationFacade.getLatestByName(resourceExist.getName())).thenReturn(getCompLatestResult);
		when(toscaOperationFacade.overrideComponent(any(Resource.class), any(Resource.class)))
				.thenReturn(getLatestResult);

		when(lifecycleBl.changeState(Mockito.anyString(), eq(user), eq(LifeCycleTransitionEnum.CHECKOUT),
				any(LifecycleChangeInfoWithAction.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
						.thenReturn(createResponse);

		Resource resourceToUpdtae = createResourceObject(false);

		Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> createOrUpdateResource = bl
				.createOrUpdateResourceByImport(resourceToUpdtae, user, false, false, false, null, null, false);
		assertTrue(createOrUpdateResource.isLeft());

		Mockito.verify(toscaOperationFacade, Mockito.times(1)).overrideComponent(any(Resource.class),
				any(Resource.class));
		Mockito.verify(lifecycleBl, Mockito.times(1)).changeState(Mockito.anyString(), eq(user),
				eq(LifeCycleTransitionEnum.CHECKOUT), any(LifecycleChangeInfoWithAction.class), Mockito.anyBoolean(),
				Mockito.anyBoolean());

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
				.createOrUpdateResourceByImport(resourceToUpdtae, user, false, false, false, null, null, false);
		assertTrue(createOrUpdateResource.isLeft());

		Mockito.verify(toscaOperationFacade, Mockito.times(0)).overrideComponent(any(Resource.class),
				any(Resource.class));
		Mockito.verify(lifecycleBl, Mockito.times(0)).changeState(Mockito.anyString(), eq(user),
				eq(LifeCycleTransitionEnum.CHECKOUT), any(LifecycleChangeInfoWithAction.class), Mockito.anyBoolean(),
				Mockito.anyBoolean());

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
		String artifactInfoToNotDeleteFileName = "infoArtifactNotToDelete.yaml";
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
		artifactToDelete.setIsFromCsar(true);

		ArtifactDefinition artifactToNotDelete = new ArtifactDefinition();
		artifactToNotDelete.setMandatory(false);
		artifactToNotDelete.setArtifactName(artifactInfoToNotDeleteFileName);
		artifactToNotDelete.setArtifactType("SNMP_TRAP");
		artifactToNotDelete.setPayload(oldPayloadData);
		artifactToNotDelete.setArtifactChecksum(GeneralUtility.calculateMD5Base64EncodedByByteArray(oldPayloadData));
		artifactToNotDelete.setIsFromCsar(false);

		ArtifactDefinition artifactToIgnore = new ArtifactDefinition();

		artifacts.put(ValidationUtils.normalizeArtifactLabel(artifactToUpdate.getArtifactName()), artifactToUpdate);
		artifacts.put(ValidationUtils.normalizeArtifactLabel(artifactToDelete.getArtifactName()), artifactToDelete);
		artifacts.put(ValidationUtils.normalizeArtifactLabel(artifactToNotDelete.getArtifactName()),
				artifactToNotDelete);
		artifacts.put("ignore", artifactToIgnore);

		resource.setDeploymentArtifacts(deploymentArtifacts);
		resource.setArtifacts(artifacts);

		List<NonMetaArtifactInfo> artifactPathAndNameList = new ArrayList<>();
		NonMetaArtifactInfo deploymentArtifactInfoToUpdate = new NonMetaArtifactInfo(
				deploymentArtifactToUpdate.getArtifactName(), null,
				ArtifactTypeEnum.findType(deploymentArtifactToUpdate.getArtifactType()),
				ArtifactGroupTypeEnum.DEPLOYMENT, newPayloadData, deploymentArtifactToUpdate.getArtifactName(), false);

		NonMetaArtifactInfo informationalArtifactInfoToUpdate = new NonMetaArtifactInfo(
				artifactToUpdate.getArtifactName(), null, ArtifactTypeEnum.findType(artifactToUpdate.getArtifactType()),
				ArtifactGroupTypeEnum.DEPLOYMENT, newPayloadData, artifactToUpdate.getArtifactName(), false);

		NonMetaArtifactInfo informationalArtifactInfoToUpdateFromCsar = new NonMetaArtifactInfo(
				artifactToUpdate.getArtifactName(), null, ArtifactTypeEnum.findType(artifactToUpdate.getArtifactType()),
				ArtifactGroupTypeEnum.INFORMATIONAL, newPayloadData, artifactToUpdate.getArtifactName(), true);

		NonMetaArtifactInfo deploymentArtifactInfoToUpdateFromCsar = new NonMetaArtifactInfo(
				artifactToUpdate.getArtifactName(), null, ArtifactTypeEnum.findType(artifactToUpdate.getArtifactType()),
				ArtifactGroupTypeEnum.DEPLOYMENT, newPayloadData, artifactToUpdate.getArtifactName(), true);

		NonMetaArtifactInfo deploymentArtifactInfoToCreate = new NonMetaArtifactInfo(deploymentArtifactToCreateFileName,
				null, ArtifactTypeEnum.OTHER, ArtifactGroupTypeEnum.DEPLOYMENT, newPayloadData,
				deploymentArtifactToCreateFileName, false);

		NonMetaArtifactInfo informationalArtifactInfoToCreate = new NonMetaArtifactInfo(artifactInfoToCreateFileName,
				null, ArtifactTypeEnum.OTHER, ArtifactGroupTypeEnum.INFORMATIONAL, newPayloadData,
				artifactInfoToCreateFileName, false);

		artifactPathAndNameList.add(deploymentArtifactInfoToUpdate);
		artifactPathAndNameList.add(informationalArtifactInfoToUpdate);
		artifactPathAndNameList.add(deploymentArtifactInfoToCreate);
		artifactPathAndNameList.add(informationalArtifactInfoToCreate);
		artifactPathAndNameList.add(informationalArtifactInfoToUpdateFromCsar);
		artifactPathAndNameList.add(deploymentArtifactInfoToUpdateFromCsar);

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
			assertTrue(foundVfArtifacts.get(ArtifactOperationEnum.CREATE).size() == 4);
			assertTrue(foundVfArtifacts.get(ArtifactOperationEnum.UPDATE).size() == 4);
			assertTrue(foundVfArtifacts.get(ArtifactOperationEnum.DELETE).size() == 1);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testVFGeneratedInputs() {
		validateUserRoles(Role.ADMIN, Role.DESIGNER);
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
	public void testCRGeneratedInputs() {
		validateUserRoles(Role.ADMIN, Role.DESIGNER);
		Resource resource = createCR();
		List<InputDefinition> inputs = resource.getInputs();
		assertTrue(5 == inputs.size());
		for (InputDefinition input : inputs) {
			assertNotNull(input.getOwnerId());
		}
		assertTrue(resource.getDerivedFromGenericType().equals(genericCR.getToscaResourceName()));
		assertTrue(resource.getDerivedFromGenericVersion().equals(genericCR.getVersion()));
	}

	@Test
	public void testVFUpdateGenericInputsToLatestOnCheckout() {
		validateUserRoles(Role.ADMIN, Role.DESIGNER);
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
		validateUserRoles(Role.ADMIN, Role.DESIGNER);
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
		when(genericTypeBusinessLogic.fetchDerivedFromGenericType(resource)).thenReturn(Either.left(genericVF));
		when(genericTypeBusinessLogic.convertGenericTypePropertiesToInputsDefintion(genericVF.getProperties(),
				genericVF.getUniqueId())).thenCallRealMethod();
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
		validateUserRoles(Role.ADMIN, Role.DESIGNER);
		Resource resource = createPNF();
		List<InputDefinition> inputs = resource.getInputs();
		assertTrue(3 == inputs.size());
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
		when(genericTypeBusinessLogic.fetchDerivedFromGenericType(resource)).thenReturn(Either.left(genericVF));
		when(genericTypeBusinessLogic.generateInputsFromGenericTypeProperties(genericVF)).thenCallRealMethod();
		when(genericTypeBusinessLogic.convertGenericTypePropertiesToInputsDefintion(genericVF.getProperties(),
				resource.getUniqueId())).thenCallRealMethod();
		Either<Resource, ResponseFormat> createResponse = bl.createResource(resource,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isLeft());
		return createResponse.left().value();
	}

	private Resource createCR() {

		genericCR = setupGenericTypeMock(GENERIC_CR_NAME);
		when(toscaOperationFacade.getLatestCertifiedNodeTypeByToscaResourceName(GENERIC_CR_NAME))
				.thenReturn(Either.left(genericCR));
		Resource resource = createResourceObject(true);
		resource.setDerivedFrom(null);
		resource.setResourceType(ResourceTypeEnum.CR);
		when(toscaOperationFacade.createToscaComponent(resource)).thenReturn(Either.left(resource));
		when(genericTypeBusinessLogic.fetchDerivedFromGenericType(resource)).thenReturn(Either.left(genericCR));
		when(genericTypeBusinessLogic.generateInputsFromGenericTypeProperties(genericCR)).thenCallRealMethod();
		when(genericTypeBusinessLogic.convertGenericTypePropertiesToInputsDefintion(genericCR.getProperties(),
				resource.getUniqueId())).thenCallRealMethod();
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
		when(genericTypeBusinessLogic.fetchDerivedFromGenericType(resource)).thenReturn(Either.left(genericPNF));
		when(genericTypeBusinessLogic.generateInputsFromGenericTypeProperties(genericPNF)).thenCallRealMethod();
		when(genericTypeBusinessLogic.convertGenericTypePropertiesToInputsDefintion(genericPNF.getProperties(),
				resource.getUniqueId())).thenCallRealMethod();
		Either<Resource, ResponseFormat> createResponse = bl.createResource(resource,
				AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertTrue(createResponse.isLeft());
		return createResponse.left().value();
	}

	private Map<String, String> getGenericPropertiesByToscaName(String toscaName) {
		HashMap<String, String> PNFProps = new HashMap<String, String>() {
			{
				put("nf_function", "string");
				put("nf_role", "string");
				put("nf_type", "string");
			}
		};

		HashMap<String, String> CRProps = new HashMap<String, String>() {
			{
				putAll(PNFProps);
				put("nf_naming_code", "string");
				put("nf_naming", "org.openecomp.datatypes.Naming");
			}
		};

		HashMap<String, String> VFProps = new HashMap<String, String>() {
			{
				putAll(CRProps);
				put("availability_zone_max_count", "integer");
				put("min_instances", "integer");
				put("max_instances", "integer");
			}
		};

		if (toscaName.contains("PNF"))
			return PNFProps;
		if (toscaName.contains("CR"))
			return CRProps;
		if (toscaName.contains("VF"))
			return VFProps;

		return new HashMap<>();
	}

	private Resource setupGenericTypeMock(String toscaName) {

		Resource genericType = createResourceObject(true);
		genericType.setVersion("1.0");
		genericType.setToscaResourceName(toscaName);
		List<PropertyDefinition> genericProps = new ArrayList<>();
		Map<String, String> genericPropsMap = getGenericPropertiesByToscaName(toscaName);
		genericPropsMap.forEach((name, type) -> {
			PropertyDefinition prop = new PropertyDefinition();
			prop.setName(name);
			prop.setType(type);
			genericProps.add(prop);
		});

		genericType.setProperties(genericProps);
		return genericType;
	}

	private void validateUserRoles(Role... roles) {
		List<Role> listOfRoles = Stream.of(roles).collect(Collectors.toList());
		when(userValidations.validateUserRole(user, listOfRoles)).thenReturn(Either.left(true));
	}

	private ResourceBusinessLogic createTestSubject() {
		return bl;
	}

	@Test
	public void testGetAllCertifiedResources() throws Exception {
		ResourceBusinessLogic testSubject;
		boolean getAbstract = false;
		HighestFilterEnum highestFilter = HighestFilterEnum.HIGHEST_ONLY;
		String userId = user.getUserId();
		Either<List<Resource>, ResponseFormat> result;
		Resource resource = createResourceObject(true);

		// default test

		Either<List<Resource>, StorageOperationStatus> getLatestResult = Either.right(StorageOperationStatus.OK);

		when(toscaOperationFacade.getAllCertifiedResources(false, true)).thenReturn(getLatestResult);
		testSubject = createTestSubject();
		result = testSubject.getAllCertifiedResources(getAbstract, highestFilter, userId);
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
	public void testGetArtifactsManager() throws Exception {
		ResourceBusinessLogic testSubject;
		ArtifactsBusinessLogic result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactsManager();
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
	public void testGetCapabilityTypeOperation() throws Exception {
		ResourceBusinessLogic testSubject;
		ICapabilityTypeOperation result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilityTypeOperation();
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
	public void testGetComponentsUtils() throws Exception {
		ResourceBusinessLogic testSubject;
		ComponentsUtils result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentsUtils();
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
	public void testGetCsarOperation() throws Exception {
		ResourceBusinessLogic testSubject;
		CsarOperation result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCsarOperation();
	}

	@Test
	public void testGetLatestResourceFromCsarUuid() throws Exception {
		ResourceBusinessLogic testSubject;
		Resource resource = createResourceObject(true);
		String csarUuid = "";
		Either<Resource, ResponseFormat> result;

		Either<Resource, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getLatestComponentByCsarOrName(ComponentTypeEnum.RESOURCE, csarUuid, ""))
				.thenReturn(eitherUpdate);

		// test 1
		testSubject = createTestSubject();
		result = testSubject.getLatestResourceFromCsarUuid(csarUuid, user);
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
	public void testGetResource() throws Exception {
		ResourceBusinessLogic testSubject;
		Resource resource = createResourceObject(true);
		String resourceId = resource.getUniqueId();
		Either<Resource, ResponseFormat> result;

		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);

		// test 1
		testSubject = createTestSubject();
		result = testSubject.getResource(resourceId, user);
	}

	@Test
	public void testGetResourceByNameAndVersion() throws Exception {
		ResourceBusinessLogic testSubject;
		Resource resource = createResourceObject(true);
		String resourceName = resource.getName();
		String resourceVersion = resource.getVersion();
		String userId = user.getUserId();
		Either<Resource, ResponseFormat> result;

		when(toscaOperationFacade.getComponentByNameAndVersion(ComponentTypeEnum.RESOURCE, resourceName,
				resourceVersion)).thenReturn(Either.left(resource));

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceByNameAndVersion(resourceName, resourceVersion, userId);
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
	public void testHandleNodeTypeArtifacts() throws Exception {
		ResourceBusinessLogic testSubject;
		Resource nodeTypeResource = null;
		Map<ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle = null;
		List<ArtifactDefinition> createdArtifacts = null;
		User user = null;
		boolean inTransaction = false;
		boolean ignoreLifecycleState = false;
		Either<List<ArtifactDefinition>, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.handleNodeTypeArtifacts(nodeTypeResource, nodeTypeArtifactsToHandle, createdArtifacts,
				user, inTransaction, ignoreLifecycleState);
	}

	
	@Test
	public void testIsResourceExist() throws Exception {
		ResourceBusinessLogic testSubject;
		Resource resource = createResourceObject(true);
		String resourceName = resource.getName();
		boolean result;

		// default test
		Either<Component, StorageOperationStatus> getLatestResult = Either.right(StorageOperationStatus.NOT_FOUND);
		when(toscaOperationFacade.getLatestByName(resource.getName())).thenReturn(getLatestResult);
		testSubject = createTestSubject();
		result = testSubject.isResourceExist(resourceName);
	}

	@Test
	public void testPropagateStateToCertified() throws Exception {
		ResourceBusinessLogic testSubject;
		Resource resource = createResourceObject(true);
		;
		LifecycleChangeInfoWithAction lifecycleChangeInfo = null;
		boolean inTransaction = false;
		boolean needLock = false;
		boolean forceCertificationAllowed = false;
		Either<Resource, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.propagateStateToCertified(user, resource, lifecycleChangeInfo, inTransaction, needLock,
				forceCertificationAllowed);
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
	public void testSetArtifactsManager() throws Exception {
		ResourceBusinessLogic testSubject;
		ArtifactsBusinessLogic artifactsManager = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactsManager(artifactsManager);
	}

	@Test
	public void testSetCacheManagerOperation() throws Exception {
		ResourceBusinessLogic testSubject;
		ICacheMangerOperation cacheManagerOperation = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCacheManagerOperation(cacheManagerOperation);
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
	public void testSetComponentsUtils() throws Exception {
		ResourceBusinessLogic testSubject;
		ComponentsUtils componentsUtils = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentsUtils(componentsUtils);
	}

	@Test
	public void testSetDeploymentArtifactsPlaceHolder() throws Exception {
		ResourceBusinessLogic testSubject;
		Component component = createResourceObject(true);
		;

		// default test
		testSubject = createTestSubject();
		testSubject.setDeploymentArtifactsPlaceHolder(component, user);
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
	public void testSetElementDao() throws Exception {
		ResourceBusinessLogic testSubject;
		IElementOperation elementDao = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setElementDao(elementDao);
	}

	@Test
	public void testSetInterfaceOperation() throws Exception {
		ResourceBusinessLogic testSubject;
		InterfaceOperation interfaceOperation = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInterfaceOperation(interfaceOperation);
	}

	@Test
	public void testSetInterfaceTypeOperation() throws Exception {
		ResourceBusinessLogic testSubject;
		IInterfaceLifecycleOperation interfaceTypeOperation = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInterfaceTypeOperation(interfaceTypeOperation);
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
	public void testSetPropertyOperation() throws Exception {
		ResourceBusinessLogic testSubject;
		IPropertyOperation propertyOperation = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setPropertyOperation(propertyOperation);
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
	public void testShouldUpgradeToLatestDerived() throws Exception {
		ResourceBusinessLogic testSubject;
		Resource clonedComponent = createResourceObject(true);
		;
		Either<Component, ActionStatus> result;

		Either<Component, StorageOperationStatus> getLatestResult = Either.right(StorageOperationStatus.OK);
		when(toscaOperationFacade.shouldUpgradeToLatestDerived(clonedComponent)).thenReturn(getLatestResult);

		// default test
		testSubject = createTestSubject();
		result = testSubject.shouldUpgradeToLatestDerived(clonedComponent);
	}

	@Test
	public void testUpdateResourceMetadata() throws Exception {
		ResourceBusinessLogic testSubject;
		String resourceIdToUpdate = "";
		Resource newResource = createResourceObject(true);
		Resource currentResource = createResourceObject(true);
		boolean inTransaction = false;
		Either<Resource, ResponseFormat> result;

		// default test
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(newResource));
		when(toscaOperationFacade.getToscaElement(newResource.getUniqueId())).thenReturn(eitherUpdate);
		testSubject = createTestSubject();
		result = testSubject.updateResourceMetadata(resourceIdToUpdate, newResource, currentResource, user,
				inTransaction);
	}

	@Test
	public void testValidateAndUpdateResourceFromCsar() throws Exception {
		ResourceBusinessLogic testSubject;
		Resource resource = createResourceObject(true);
		;
		Map<String, byte[]> csarUIPayload = null;
		String payloadName = "";
		String resourceUniqueId = "";
		Either<Resource, ResponseFormat> result;

		// test 1
		testSubject = createTestSubject();
		payloadName = null;
		result = testSubject.validateAndUpdateResourceFromCsar(resource, user, csarUIPayload, payloadName,
				resourceUniqueId);

		// test 2
		testSubject = createTestSubject();
		payloadName = "";
		result = testSubject.validateAndUpdateResourceFromCsar(resource, user, csarUIPayload, payloadName,
				resourceUniqueId);
	}

	@Test
	public void testValidateDerivedFromNotEmpty() throws Exception {
		ResourceBusinessLogic testSubject;
		Resource resource = createResourceObject(true);
		AuditingActionEnum actionEnum = AuditingActionEnum.ADD_ECOMP_USER_CREDENTIALS;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validateDerivedFromNotEmpty(user, resource, actionEnum);
	}

	@Test
	public void testValidateResourceBeforeCreate() throws Exception {
		ResourceBusinessLogic testSubject;
		Resource resource = createResourceObject(true);
		;
		AuditingActionEnum actionEnum = AuditingActionEnum.ADD_CATEGORY;
		boolean inTransaction = false;
		CsarInfo csarInfo = null;
		Either<Resource, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validateResourceBeforeCreate(resource, user, actionEnum, inTransaction, csarInfo);
	}

	@Test
	public void testValidateResourceCreationFromNodeType() throws Exception {
		ResourceBusinessLogic testSubject;
		Resource resource = createResourceObject(true);
		;
		User creator = user;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validateResourceCreationFromNodeType(resource, creator);
	}

	@Test
	public void testValidatePropertiesDefaultValues() throws Exception {
		ResourceBusinessLogic testSubject;
		Resource resource = createResourceObject(true);
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validatePropertiesDefaultValues(resource);
	}

	@Test
	public void testValidateResourceNameExists() throws Exception {
		ResourceBusinessLogic testSubject;
		Resource recource = createResourceObject(true);
		String resourceName = recource.getName();
		ResourceTypeEnum resourceTypeEnum = ResourceTypeEnum.VF;
		ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
		String userId = user.getUserId();
		Either<Map<String, Boolean>, ResponseFormat> result;

		// default test

		Either<Boolean, StorageOperationStatus> dataModelResponse = Either.left(true);
		when(toscaOperationFacade.validateComponentNameUniqueness(resourceName, resourceTypeEnum, componentTypeEnum))
				.thenReturn(dataModelResponse);
		testSubject = createTestSubject();
		result = testSubject.validateResourceNameExists(resourceName, resourceTypeEnum, userId);
	}

	@Test
	public void testValidateVendorReleaseName() throws Exception {
		ResourceBusinessLogic testSubject;
		String vendorRelease = "";
		Either<Boolean, ResponseFormat> result;

		// test 1
		testSubject = createTestSubject();
		vendorRelease = null;
		result = testSubject.validateVendorReleaseName(vendorRelease);

		// test 2
		testSubject = createTestSubject();
		vendorRelease = "";
		result = testSubject.validateVendorReleaseName(vendorRelease);
	}

	@Test
	public void testValidateVendorReleaseName_1() throws Exception {
		ResourceBusinessLogic testSubject;
		Resource resource = createResourceObject(true);
		AuditingActionEnum actionEnum = null;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validateVendorReleaseName(user, resource, actionEnum);
	}

	@Test
	public void testIsResourceNameEquals() throws Exception {
		ResourceBusinessLogic testSubject;
		Resource currentResource = createResourceObject(true);
		Resource updateInfoResource = createResourceObject(true);
		updateInfoResource.setName("name");
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "isResourceNameEquals",
				new Object[] { currentResource, updateInfoResource });
	}

	@Test
	public void testAddCvfcSuffixToResourceName() throws Exception {
		ResourceBusinessLogic testSubject;
		Resource currentResource = createResourceObject(true);
		String resourceName = currentResource.getName();
		String result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "addCvfcSuffixToResourceName", new Object[] { resourceName });
	}

	
	@Test
	public void testCreateResource() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	AuditingActionEnum auditingAction = null;
	Map<String,byte[]> csarUIPayload = null;
	String payloadName = "";
	Either<Resource,ResponseFormat> result;
	
	// test 1 1
	testSubject=createTestSubject();
    
    List<Role> listOfRoles = new ArrayList<>();
    listOfRoles.add(Role.ADMIN);
    listOfRoles.add(Role.DESIGNER);
    Either<Boolean, ResponseFormat> validationResult = Either.left(true);
	when(userValidations.validateUserRole(user, listOfRoles)).thenReturn(validationResult);
	
	
	
	result=testSubject.createResource(resource, auditingAction, user, csarUIPayload, payloadName);
	
	// test 2 2
	testSubject=createTestSubject();payloadName = "";
	result=testSubject.createResource(resource, auditingAction, user, csarUIPayload, payloadName);
	
	// test 3
	testSubject=createTestSubject();payloadName = null;
	result=testSubject.createResource(resource, auditingAction, user, csarUIPayload, payloadName);
	
	// test 4
	testSubject=createTestSubject();payloadName = "";
	result=testSubject.createResource(resource, auditingAction, user, csarUIPayload, payloadName);
	}

	
	@Test
	public void testGetElementDao_1() throws Exception {
	ResourceBusinessLogic testSubject;IElementOperation result;
	
	// default test
	testSubject=createTestSubject();result=testSubject.getElementDao();
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testCreateResource_1() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);;
	AuditingActionEnum auditingAction = AuditingActionEnum.ADD_CATEGORY;
	Map<String,byte[]> csarUIPayload = new HashMap<>();
	String payloadName = "";
	Either<Resource,ResponseFormat> result;
	
	   List<Role> listOfRoles = new ArrayList<>();
	    listOfRoles.add(Role.ADMIN);
	    listOfRoles.add(Role.DESIGNER);
	    Either<Boolean, ResponseFormat> validationResult = Either.left(true);
		when(userValidations.validateUserRole(user, listOfRoles)).thenReturn(validationResult);
	
	// test 1 1
	testSubject=createTestSubject();
	result=testSubject.createResource(resource, auditingAction, user, csarUIPayload, payloadName);
	
	}

	
	@Test
	public void testValidateCsarIsNotAlreadyUsed() throws Exception {
	ResourceBusinessLogic testSubject;
	Wrapper<ResponseFormat> responseWrapper = new Wrapper<ResponseFormat>();
	Resource oldResource = createResourceObject(true);
	Resource resource = createResourceObject(true);
	String csarUUID = "";
    
    Either<Resource, StorageOperationStatus> resourceLinkedToCsarRes = Either.right(StorageOperationStatus.BAD_REQUEST);
	when(toscaOperationFacade.getLatestComponentByCsarOrName(ComponentTypeEnum.RESOURCE, csarUUID, resource.getSystemName()))
			.thenReturn(resourceLinkedToCsarRes);
    
	// default test
	testSubject=createTestSubject();Deencapsulation.invoke(testSubject, "validateCsarIsNotAlreadyUsed", new Object[]{responseWrapper, resource, oldResource, csarUUID, user});
	}

	
	@Test
	public void testValidateCsarUuidMatching() throws Exception {
	ResourceBusinessLogic testSubject;
	Wrapper<ResponseFormat> responseWrapper = new Wrapper<ResponseFormat>();
	Resource resource = createResourceObject(true);
	Resource oldResource = createResourceObject(true);
	String csarUUID = "";
	String resourceUniqueId = "";
	
	
	
	// default test
	testSubject=createTestSubject();Deencapsulation.invoke(testSubject, "validateCsarUuidMatching", new Object[]{responseWrapper, resource, oldResource, csarUUID, resourceUniqueId, user});
	}

	
	@Test
	public void testGetResourceByUniqueId() throws Exception {
	ResourceBusinessLogic testSubject;
	Wrapper<ResponseFormat> responseWrapper = new Wrapper<ResponseFormat>();
	Resource resource = createResourceObject(true);
	String resourceUniqueId = resource.getUniqueId();
	Resource result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "getResourceByUniqueId", new Object[]{responseWrapper, resourceUniqueId});
	}

	
	@Test
	public void testOverrideImmutableMetadata() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource oldRresource = createResourceObject(true);
	Resource resource = createResourceObject(true);
	
	
	// default test
	testSubject=createTestSubject();Deencapsulation.invoke(testSubject, "overrideImmutableMetadata", new Object[]{oldRresource, resource});
	}

	
	@Test
	public void testFindNodeTypesArtifactsToHandle() throws Exception {
	ResourceBusinessLogic testSubject;
	NodeTypeInfo nodeTypeInfos = new NodeTypeInfo();
	Map<String,NodeTypeInfo> nodeTypesInfo = new HashMap<>();
	nodeTypesInfo.put("key", nodeTypeInfos);
	
	CsarInfo csarInfo = new CsarInfo("", new User(), "", null, "", false);
	Resource oldResource = createResourceObject(true);
	Either<Map<String,EnumMap<ArtifactOperationEnum,List<ArtifactDefinition>>>,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "findNodeTypesArtifactsToHandle", new Object[]{nodeTypesInfo, csarInfo, oldResource});
	}

	
	@Test(expected=IllegalArgumentException.class)
	public void testFindNodeTypeArtifactsToHandle() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource curNodeType = createResourceObject(true);
	ArtifactDefinition artifactDefinition = new ArtifactDefinition();
	List<ArtifactDefinition> extractedArtifacts = new ArrayList<>();
	extractedArtifacts.add(artifactDefinition);
	Either<EnumMap<ArtifactOperationEnum,List<ArtifactDefinition>>,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "findNodeTypeArtifactsToHandle", new Object[]{curNodeType, artifactDefinition});
	}

	
	@Test
	public void testCheckoutResource() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	
	boolean inTransaction = false;
	Either<Resource,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "checkoutResource", new Object[]{resource, user, inTransaction});
	}

	


	

	
	@Test
	public void testExtractNodeTypes() throws Exception {
	ResourceBusinessLogic testSubject;
	Map<String,Object> nodes = new HashMap<>();

	Map<String,Object> mappedToscaTemplate = new HashMap<>();
	mappedToscaTemplate.put("node_types", new Object());
	
	
	// default test
	testSubject=createTestSubject();Deencapsulation.invoke(testSubject, "extractNodeTypes", new Object[]{nodes, mappedToscaTemplate});
	}

	@Test
	public void testMarkNestedVfc() throws Exception {
	ResourceBusinessLogic testSubject;
	Map<String,Object> mappedToscaTemplate = new HashMap<>();
	Map<String,NodeTypeInfo> nodeTypesInfo = new HashMap<>();
	
	
	// default test
	testSubject=createTestSubject();Deencapsulation.invoke(testSubject, "markNestedVfc", new Object[]{mappedToscaTemplate, nodeTypesInfo});
	}

	
	@Test
	public void testIsGlobalSubstitute() throws Exception {
	ResourceBusinessLogic testSubject;String fileName = "";
	boolean result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "isGlobalSubstitute", new Object[]{fileName});
	}

	
	@Test
	public void testValidateAndParseCsar() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	
	String csarUUID = "";
	Either<Map<String,byte[]>,StorageOperationStatus> csar = Either.right(StorageOperationStatus.BAD_REQUEST);
	Either<ImmutablePair<String,String>,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateAndParseCsar", new Object[]{resource, user, csarUUID, csar});
	}

	
	@Test
	public void testValidateResourceBeforeCreate_1() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	
	boolean inTransaction = false;
	Either<Resource,ResponseFormat> result;
	
	   List<Role> listOfRoles = new ArrayList<>();
	    listOfRoles.add(Role.ADMIN);
	    listOfRoles.add(Role.DESIGNER);
	    Either<Boolean, ResponseFormat> validationResult = Either.left(true);
		when(userValidations.validateUserRole(user, listOfRoles)).thenReturn(validationResult);
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateResourceBeforeCreate", new Object[]{resource, user, inTransaction});
	}

	

	
	@Test
	public void testCreateResourcesFromYamlNodeTypesList() throws Exception {
	ResourceBusinessLogic testSubject;
	String yamlName = "";
	Resource resource = createResourceObject(true);
	Map<String,Object> mappedToscaTemplate = new HashMap<>();
	boolean needLock = false;
	Map<String,EnumMap<ArtifactOperationEnum,List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
	List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
	Map<String,NodeTypeInfo> nodeTypesInfo = new HashMap<>();
	CsarInfo csarInfo = new CsarInfo("", new User(), "", null, "", false);
	Either<Map<String,Resource>,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=testSubject.createResourcesFromYamlNodeTypesList(yamlName, resource, mappedToscaTemplate, needLock, nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo);
	}


	
	@Test
	public void testFillResourceMetadata() throws Exception {
	ResourceBusinessLogic testSubject;
	String yamlName = "";
	Resource resourceVf = createResourceObject(true);;
	String nodeName = "";
	
	Either<UploadResourceInfo,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "fillResourceMetadata", new Object[]{yamlName, resourceVf, nodeName, user});
	}



	
	@Test
	public void testBuildCvfcName() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	String resourceVfName = "ResourceResource12";
	String nodeName ="org.openecomp.resource.VF.ResourceResource12";
	String result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "buildCvfcName", new Object[]{resourceVfName, nodeName});
	}

	@Test
	public void testUpdateGroupMembersUsingResource() throws Exception {
	ResourceBusinessLogic testSubject;
	GroupDefinition groupDefinition = new GroupDefinition();
	Map<String,GroupDefinition> groups = new HashMap<>();
	groups.put("key", groupDefinition);
	Resource component = createResourceObject(true);
	Either<List<GroupDefinition>,ResponseFormat> result;
	
	// test 1
	testSubject=createTestSubject();
	result=Deencapsulation.invoke(testSubject, "updateGroupMembersUsingResource", new Object[]{groups, component});
	}

	
	@Test
	public void testValidateCyclicGroupsDependencies() throws Exception {
	ResourceBusinessLogic testSubject;
	GroupDefinition groupDefinition = new GroupDefinition();
	Map<String,GroupDefinition> groups = new HashMap<>();
	groups.put("key", groupDefinition);
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateCyclicGroupsDependencies", new Object[]{groups});
	}

	
	@Test
	public void testFillAllGroupMemebersRecursivly() throws Exception {
	ResourceBusinessLogic testSubject;
	String groupName = "";
	GroupDefinition groupDefinition = new GroupDefinition();
	Map<String,GroupDefinition> groups = new HashMap<>();
	groups.put("key", groupDefinition);
	Set<String> allGroupMembers = new HashSet<String>();
	allGroupMembers.add("aaa");
	
	// default test
	testSubject=createTestSubject();Deencapsulation.invoke(testSubject, "fillAllGroupMemebersRecursivly", new Object[]{groupName, groups, allGroupMembers});
	}

	
	@Test
	public void testIsfillGroupMemebersRecursivlyStopCondition() throws Exception {
	ResourceBusinessLogic testSubject;
	String groupName = "";
	GroupDefinition groupDefinition = new GroupDefinition();
	Map<String,GroupDefinition> groups = new HashMap<>();
	groups.put("key", groupDefinition);
	Set<String> allGroupMembers = new HashSet<String>();
	allGroupMembers.add("aaa");
	boolean result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "isfillGroupMemebersRecursivlyStopCondition", new Object[]{groupName, groups, allGroupMembers});
	}

	


	
	@Test
	public void testHandleAndAddExtractedVfcsArtifacts() throws Exception {
	ResourceBusinessLogic testSubject;
	ArtifactDefinition artifactDefinition = new ArtifactDefinition();
	
	List<ArtifactDefinition> vfcArtifacts = new ArrayList<>();
	vfcArtifacts.add(artifactDefinition);
	List<ArtifactDefinition> artifactsToAdd = new ArrayList<>();
	artifactsToAdd.add(artifactDefinition);
	
	// default test
	testSubject=createTestSubject();Deencapsulation.invoke(testSubject, "handleAndAddExtractedVfcsArtifacts", new Object[]{vfcArtifacts, artifactsToAdd});
	}

	

	
	@Test
	public void testHandleVfCsarArtifacts() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	CsarInfo csarInfo = new CsarInfo("", new User(), "", null, "", false);
	ArtifactOperationEnum download = ArtifactOperationEnum.CREATE;
	ArtifactsBusinessLogic artb = new ArtifactsBusinessLogic();
	
	ArtifactOperationInfo operation = artb.new ArtifactOperationInfo(false, false, download);
	List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
	boolean shouldLock = false;
	boolean inTransaction = false;
	Either<Resource,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "handleVfCsarArtifacts", new Object[]{resource, csarInfo, createdArtifacts, operation, shouldLock, inTransaction});
	}

	

	@Test
	public void testIsArtifactDeletionRequired() throws Exception {
	ResourceBusinessLogic testSubject;
	String artifactId = "";
	byte[] artifactFileBytes = new byte[]{' '};
	boolean isFromCsar = false;
	boolean result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "isArtifactDeletionRequired", new Object[]{artifactId, artifactFileBytes, isFromCsar});
	}

	
	

	@Test
	public void testValidateArtifactNames() throws Exception {
	ResourceBusinessLogic testSubject;
	NonMetaArtifactInfo nonMetaArtifactInfo = new NonMetaArtifactInfo(operationId, operationId, null, null, null, operationId, false);
	List<NonMetaArtifactInfo> artifactPathAndNameList = new ArrayList<>();
	artifactPathAndNameList.add(nonMetaArtifactInfo);
	Either<Boolean,String> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateArtifactNames", new Object[]{artifactPathAndNameList});
	}

	
	@Test
	public void testIsNonMetaArtifact() throws Exception {
	ResourceBusinessLogic testSubject;
	ArtifactDefinition artifact = new ArtifactDefinition();
	boolean result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "isNonMetaArtifact", new Object[]{artifact});
	}

	
	@Test
	public void testIsValidArtifactType() throws Exception {
	ResourceBusinessLogic testSubject;
	ArtifactDefinition artifact = new ArtifactDefinition();
	boolean result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "isValidArtifactType", new Object[]{artifact});
	}

	
	@Test
	public void testCreateResourceInstancesRelations() throws Exception {
	ResourceBusinessLogic testSubject;
	String yamlName = "";
	Resource resource = createResourceObject(true);
	UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
	
	Map<String,UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
	uploadResInstancesMap.put("key", uploadComponentInstanceInfo);
	Either<Resource,ResponseFormat> result;
	
	// test 1
	testSubject=createTestSubject();
	result=Deencapsulation.invoke(testSubject, "createResourceInstancesRelations", new Object[]{user, yamlName, resource, uploadResInstancesMap});
	}

	
	@Test
	public void testUpdatePropertyValues() throws Exception {
	ResourceBusinessLogic testSubject;
	List<ComponentInstanceProperty> properties = new ArrayList<>();
	Map<String,UploadPropInfo> newProperties = new HashMap<>();
	Map<String,DataTypeDefinition> allDataTypes = new HashMap<>();;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "updatePropertyValues", new Object[]{properties, newProperties, allDataTypes});
	}

	
	@Test
	public void testUpdatePropertyValue() throws Exception {
	ResourceBusinessLogic testSubject;
	ComponentInstanceProperty property = new ComponentInstanceProperty();
	UploadPropInfo propertyInfo = new UploadPropInfo();
	Map<String,DataTypeDefinition> allDataTypes = new HashMap<>();
	Either<String,StorageOperationStatus> result;
	
	// test 1
	testSubject=createTestSubject();
	result=Deencapsulation.invoke(testSubject, "updatePropertyValue", new Object[]{property, propertyInfo, allDataTypes});
	}

	
	@Test
	public void testUpdateCalculatedCapReqWithSubstitutionMappings() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
	Map<String,UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
	uploadResInstancesMap.put("key", uploadComponentInstanceInfo);
	
	Either<Resource,StorageOperationStatus> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "updateCalculatedCapReqWithSubstitutionMappings", new Object[]{resource, uploadResInstancesMap});
	}

	
	@Test
	public void testFillUpdatedInstCapabilitiesRequirements() throws Exception {
	ResourceBusinessLogic testSubject;
	List<ComponentInstance> componentInstances = new ArrayList<>();
	Map<String,UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
	Map<ComponentInstance,Map<String,List<CapabilityDefinition>>> updatedInstCapabilities = new HashMap<>();
	Map<ComponentInstance,Map<String,List<RequirementDefinition>>> updatedInstRequirements = new HashMap<>();
	
	
	// default test
	testSubject=createTestSubject();Deencapsulation.invoke(testSubject, "fillUpdatedInstCapabilitiesRequirements", new Object[]{componentInstances, uploadResInstancesMap, updatedInstCapabilities, updatedInstRequirements});
	}
	
	@Test
	public void testAddInputsValuesToRi() throws Exception {
	ResourceBusinessLogic testSubject;
	UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
	Resource resource = createResourceObject(true);
	Resource originResource = createResourceObject(true);;
	ComponentInstance currentCompInstance = new ComponentInstance();
	String yamlName = "aaa";
	Map<String,List<ComponentInstanceInput>> instInputs = new HashMap<>();
	Map<String,DataTypeDefinition> allDataTypes = new HashMap<>();
	ResponseFormat result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "addInputsValuesToRi", new Object[]{uploadComponentInstanceInfo, resource, originResource, currentCompInstance, yamlName, instInputs, allDataTypes});
	}

	


	
	@Test
	public void testFindAvailableCapabilityByTypeOrName() throws Exception {
	ResourceBusinessLogic testSubject;
	RequirementDefinition validReq = new RequirementDefinition();
	validReq.setCapability(RESOURCE_NAME);
	validReq.setToscaPresentationValue(JsonPresentationFields.CAPAPILITY, null);
	ComponentInstance currentCapCompInstance = new ComponentInstance();
	Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
	currentCapCompInstance.setCapabilities(capabilities);
	UploadReqInfo uploadReqInfo = new UploadReqInfo();
	CapabilityDefinition result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "findAvailableCapabilityByTypeOrName", new Object[]{validReq, currentCapCompInstance, uploadReqInfo});
	}

	
	@Test
	public void testFindAvailableCapability() throws Exception {
	ResourceBusinessLogic testSubject;
	RequirementDefinition validReq = new RequirementDefinition();
	validReq.setCapability(RESOURCE_NAME);
	validReq.setToscaPresentationValue(JsonPresentationFields.CAPAPILITY, null);
	ComponentInstance currentCapCompInstance = new ComponentInstance();
	Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
	currentCapCompInstance.setCapabilities(capabilities);
	UploadReqInfo uploadReqInfo = new UploadReqInfo();
	CapabilityDefinition result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "findAvailableCapability", new Object[]{validReq, currentCapCompInstance, uploadReqInfo});
	}

	
	@Test
	public void testFindAviableCapability() throws Exception {
	ResourceBusinessLogic testSubject;
	RequirementDefinition validReq = new RequirementDefinition();
	validReq.setCapability(RESOURCE_NAME);
	validReq.setToscaPresentationValue(JsonPresentationFields.CAPAPILITY, null);
	ComponentInstance currentCapCompInstance = new ComponentInstance();
	Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
	currentCapCompInstance.setCapabilities(capabilities);
	CapabilityDefinition result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "findAviableCapability", new Object[]{validReq, currentCapCompInstance});
	}

	


	


	
	@Test
	public void testCreateResourceInstances() throws Exception {
	ResourceBusinessLogic testSubject;
	String yamlName = "aa";
	Resource resource = createResourceObject(true);
	Map<String,UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
	boolean inTransaction = false;
	boolean needLock = false;
	Map<String,Resource> nodeNamespaceMap = new HashMap<>();
	Either<Resource,ResponseFormat> result;
	
	// test 1
	testSubject=createTestSubject();
	result=Deencapsulation.invoke(testSubject, "createResourceInstances", new Object[]{user, yamlName, resource,uploadResInstancesMap, inTransaction, needLock, nodeNamespaceMap});
	}

	
	@Test
	public void testGetNamesToUpdate() throws Exception {
	ResourceBusinessLogic testSubject;
	UploadComponentInstanceInfo nodeTemplateInfo = new UploadComponentInstanceInfo();
	Map<String,List<String>> elements = new HashMap<>();
	Either<Map<String,String>,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "getNamesToUpdate", new Object[]{nodeTemplateInfo, elements});
	}

	
	@Test
	public void testCreateInputPropList() throws Exception {
	ResourceBusinessLogic testSubject;
	UploadPropInfo propertyDef = new UploadPropInfo();
	List<Object> propValueList = new ArrayList<>();
	
	
	// default test
	testSubject=createTestSubject();Deencapsulation.invoke(testSubject, "createInputPropList", new Object[]{propertyDef, propValueList});
	}

	
	@Test
	public void testCreateGetInputModuleFromMap() throws Exception {
	ResourceBusinessLogic testSubject;
	String propName = "aaa";
	Map<String,Object> propValue = new HashMap<>();;
	UploadPropInfo propertyDef = new UploadPropInfo();
	
	
	// default test
	testSubject=createTestSubject();Deencapsulation.invoke(testSubject, "createGetInputModuleFromMap", new Object[]{propName, propValue, propertyDef});
	}

	
	@Test
	public void testValueContainsPattern() throws Exception {
	ResourceBusinessLogic testSubject;
	Pattern pattern = null;
	Object propValue = null;
	boolean result;
	
	// test 1
	testSubject=createTestSubject();propValue = null;
	result=Deencapsulation.invoke(testSubject, "valueContainsPattern", new Object[]{Pattern.class, Object.class});
	Assert.assertEquals(false, result);
	}



	@Test
	public void testCreateCapModuleFromYaml() throws Exception {
	ResourceBusinessLogic testSubject;
	UploadComponentInstanceInfo nodeTemplateInfo = new UploadComponentInstanceInfo();
	Map<String,Object> nodeTemplateJsonMap = new HashMap<>();
	Either<Map<String,List<UploadCapInfo>>,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "createCapModuleFromYaml", new Object[]{nodeTemplateInfo, nodeTemplateJsonMap});
	}

	


	
	@Test
	public void testCreateModuleNodeTemplateCap() throws Exception {
	ResourceBusinessLogic testSubject;Object capObject = null;
	Either<UploadCapInfo,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "createModuleNodeTemplateCap", new Object[]{Object.class});
	}

	
	@Test
	public void testCreateModuleNodeTemplateReg() throws Exception {
	ResourceBusinessLogic testSubject;Object regObject = null;
	Either<UploadReqInfo,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "createModuleNodeTemplateReg", new Object[]{Object.class});
	}

	
	
	@Test
	public void testNodeForceCertification() throws Exception {
	ResourceBusinessLogic testSubject;Resource resource = createResourceObject(true);
	
	LifecycleChangeInfoWithAction lifecycleChangeInfo = null;
	boolean inTransaction = false;
	boolean needLock = false;
	Either<Resource,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "nodeForceCertification", new Object[]{Resource.class, user, LifecycleChangeInfoWithAction.class, inTransaction, needLock});
	}

	
	@Test
	public void testCreateResourceByImport() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	
	boolean isNormative = false;
	boolean isInTransaction = false;
	CsarInfo csarInfo = null;
	Either<ImmutablePair<Resource,ActionStatus>,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "createResourceByImport", new Object[]{resource, user, isNormative, isInTransaction, CsarInfo.class});
	}

	
	
	@Test
	public void testMergeOldResourceMetadataWithNew() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource oldResource = createResourceObject(true);
	Resource newResource = createResourceObject(true);;
	
	
	// default test
	testSubject=createTestSubject();Deencapsulation.invoke(testSubject, "mergeOldResourceMetadataWithNew", new Object[]{oldResource, newResource});
	}

	
	@Test
	public void testPrepareResourceForUpdate() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource latestResource = createResourceObject(true);;
	
	boolean inTransaction = false;
	boolean needLock = false;
	Either<Resource,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "prepareResourceForUpdate", new Object[]{latestResource, user, inTransaction, needLock});
	}

	
	@Test
	public void testValidateResourceBeforeCreate_2() throws Exception {
	ResourceBusinessLogic testSubject;Resource resource = createResourceObject(true);
	
	AuditingActionEnum actionEnum = null;
	boolean inTransaction = false;
	CsarInfo csarInfo = null;
	Either<Resource,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=testSubject.validateResourceBeforeCreate(resource, user, actionEnum, inTransaction, csarInfo);
	}

	
	@Test
	public void testValidateResourceType() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	AuditingActionEnum actionEnum = AuditingActionEnum.ARTIFACT_DOWNLOAD;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateResourceType", new Object[]{user, resource, actionEnum});
	}

	
	@Test
	public void testValidateLifecycleTypesCreate() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	AuditingActionEnum actionEnum = AuditingActionEnum.ARTIFACT_METADATA_UPDATE;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateLifecycleTypesCreate", new Object[]{user, resource, actionEnum});
	}

	
	@Test
	public void testValidateCapabilityTypesCreate() throws Exception {
	ResourceBusinessLogic testSubject;
	ICapabilityTypeOperation capabilityTypeOperation = new ICapabilityTypeOperation() {
		
		@Override
		public Either<CapabilityTypeDefinition, StorageOperationStatus> getCapabilityType(String uniqueId,
				boolean inTransaction) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Either<CapabilityTypeDefinition, StorageOperationStatus> getCapabilityType(String uniqueId) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Either<CapabilityTypeDefinition, StorageOperationStatus> addCapabilityType(
				CapabilityTypeDefinition capabilityTypeDefinition, boolean inTransaction) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Either<CapabilityTypeDefinition, StorageOperationStatus> addCapabilityType(
				CapabilityTypeDefinition capabilityTypeDefinition) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	Resource resource = createResourceObject(true);
	AuditingActionEnum actionEnum = AuditingActionEnum.ARTIFACT_UPLOAD;
	boolean inTransaction = false;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();
	result=Deencapsulation.invoke(testSubject, "validateCapabilityTypesCreate", new Object[]{user, capabilityTypeOperation, resource, actionEnum, inTransaction});
	}

	
	@Test(expected=UnsupportedOperationException.class)
	public void testValidateCapabilityTypeExists() throws Exception {
	ResourceBusinessLogic testSubject;
	ICapabilityTypeOperation capabilityTypeOperation = new ICapabilityTypeOperation() {
		
		@Override
		public Either<CapabilityTypeDefinition, StorageOperationStatus> getCapabilityType(String uniqueId,
				boolean inTransaction) {
			// TODO Auto-generated method stub
			return Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND);
		}
		
		@Override
		public Either<CapabilityTypeDefinition, StorageOperationStatus> getCapabilityType(String uniqueId) {
			// TODO Auto-generated method stub
			return Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND);
		}
		
		@Override
		public Either<CapabilityTypeDefinition, StorageOperationStatus> addCapabilityType(
				CapabilityTypeDefinition capabilityTypeDefinition, boolean inTransaction) {
			// TODO Auto-generated method stub
			return Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND);
		}
		
		@Override
		public Either<CapabilityTypeDefinition, StorageOperationStatus> addCapabilityType(
				CapabilityTypeDefinition capabilityTypeDefinition) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	Resource resource = createResourceObject(true);
	List<?> validationObjects = new ArrayList<>();
	AuditingActionEnum actionEnum = AuditingActionEnum.ADD_CATEGORY;
	Either<Boolean,ResponseFormat> eitherResult = Either.left(true);
	String type = "";
	boolean inTransaction = false;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateCapabilityTypeExists", new Object[]{user, capabilityTypeOperation, resource, validationObjects, actionEnum,eitherResult, type, inTransaction});
	}
	
	@Test
	public void testCreateResourceTransaction() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	
	boolean isNormative = false;
	boolean inTransaction = false;
	Either<Resource,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "createResourceTransaction", new Object[]{resource, user, isNormative, inTransaction});
	}

	
	@Test
	public void testCreateArtifactsPlaceHolderData() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	
	
	
	// default test
	testSubject=createTestSubject();Deencapsulation.invoke(testSubject, "createArtifactsPlaceHolderData", new Object[]{resource, user});
	}

	
	@Test
	public void testSetInformationalArtifactsPlaceHolder() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	
	
	
	// default test
	testSubject=createTestSubject();Deencapsulation.invoke(testSubject, "setInformationalArtifactsPlaceHolder", new Object[]{resource, user});
	}

	
	@Test
	public void testDeleteResource() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	String resourceId = resource.getUniqueId();
	ResponseFormat result;
	
	  
	Either<Component, StorageOperationStatus> resourceStatus  = Either.right(StorageOperationStatus.BAD_REQUEST);
	when(toscaOperationFacade.getToscaElement(resourceId))
				.thenReturn(resourceStatus);
	
	// default test
	testSubject=createTestSubject();result=testSubject.deleteResource(resourceId, user);
	}

	
	@Test
	public void testDeleteResourceByNameAndVersion() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	String resourceName = resource.getName();
	String version = resource.getVersion();
	
	ResponseFormat result;
	
    Either<Component, StorageOperationStatus> resourceStatus = Either.right(StorageOperationStatus.BAD_REQUEST);
	when(toscaOperationFacade.getComponentByNameAndVersion(ComponentTypeEnum.RESOURCE, resourceName, version))
			.thenReturn(resourceStatus);
	// default test
	testSubject=createTestSubject();result=testSubject.deleteResourceByNameAndVersion(resourceName, version, user);
	}

	
	@Test
	public void testUpdateResourceMetadata_1() throws Exception {
	ResourceBusinessLogic testSubject;String resourceIdToUpdate = "";
	Resource newResource = createResourceObject(true);;
	Resource currentResource = createResourceObject(true);;
	
	boolean inTransaction = false;
	Either<Resource,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=testSubject.updateResourceMetadata(resourceIdToUpdate, newResource, currentResource, user, inTransaction);
	}

	
	@Test
	public void testUpdateComponentGroupName() throws Exception {
	ResourceBusinessLogic testSubject;
	String replacePattern = "";
	String with = "";
	List<GroupDefinition> oldGroup = new ArrayList<>();
	Either<List<GroupDefinition>,Boolean> result;
	
	// test 1
	testSubject=createTestSubject();
	result=Deencapsulation.invoke(testSubject, "updateComponentGroupName", new Object[]{replacePattern, with, oldGroup});
	
	}

	
	@Test
	public void testIsComponentNameChanged() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource newResource = createResourceObject(true);
	Resource oldResource = createResourceObject(true);
	boolean result;
	
	// test 1
	testSubject=createTestSubject();
	result=Deencapsulation.invoke(testSubject, "isComponentNameChanged", new Object[]{oldResource, newResource});
	}

	
	@Test
	public void testValidateResourceFieldsBeforeCreate() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	AuditingActionEnum actionEnum = AuditingActionEnum.ACTIVATE_SERVICE_BY_API;
	boolean inTransaction = false;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateResourceFieldsBeforeCreate", new Object[]{user, resource, actionEnum, inTransaction});
	}

	
	@Test
	public void testValidateResourceFieldsBeforeUpdate() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource currentResource =  createResourceObject(true);
	Resource updateInfoResource = createResourceObject(true);
	boolean inTransaction = false;
	boolean isNested = false;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateResourceFieldsBeforeUpdate", new Object[]{updateInfoResource, currentResource, inTransaction, isNested});
	}

	
	@Test
	public void testValidateResourceName() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource currentResource = createResourceObject(true);;
	Resource updateInfoResource = createResourceObject(true);;
	boolean hasBeenCertified = false;
	boolean isNested = false;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateResourceName", new Object[]{currentResource, updateInfoResource, hasBeenCertified, isNested});
	}

	
	@Test
	public void testValidateIcon() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource currentResource = createResourceObject(true);;
	Resource updateInfoResource = createResourceObject(true);;
	boolean hasBeenCertified = false;
	Either<Boolean,ResponseFormat> result;
	
	Either<Component, StorageOperationStatus> resourceStatus = Either.right(StorageOperationStatus.BAD_REQUEST);
	when(toscaOperationFacade.getToscaElement(currentResource.getUniqueId()))
			.thenReturn(resourceStatus);
	

	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateIcon", new Object[]{currentResource, updateInfoResource, hasBeenCertified});
	}

	
	@Test
	public void testValidateVendorName() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource currentResource = createResourceObject(true);
	Resource updateInfoResource = createResourceObject(true);
	boolean hasBeenCertified = false;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateVendorName", new Object[]{currentResource, updateInfoResource, hasBeenCertified});
	}

	
	@Test
	public void testValidateResourceVendorModelNumber() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource currentResource = createResourceObject(true);;
	Resource updateInfoResource = createResourceObject(true);;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateResourceVendorModelNumber", new Object[]{currentResource, updateInfoResource});
	}

	
	@Test
	public void testValidateCategory() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource currentResource = createResourceObject(true);;
	Resource updateInfoResource = createResourceObject(true);;
	boolean hasBeenCertified = false;
	boolean inTransaction = false;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateCategory", new Object[]{currentResource, updateInfoResource, hasBeenCertified, inTransaction});
	}

	
	@Test
	public void testValidateDerivedFromDuringUpdate() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource currentResource = createResourceObject(true);
	Resource updateInfoResource = createResourceObject(true);
	boolean hasBeenCertified = false;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateDerivedFromDuringUpdate", new Object[]{currentResource, updateInfoResource, hasBeenCertified});
	}

	
	@Test
	public void testValidateNestedDerivedFromDuringUpdate() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource currentResource = createResourceObject(true);
	Resource updateInfoResource = createResourceObject(true);
	boolean hasBeenCertified = false;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateNestedDerivedFromDuringUpdate", new Object[]{currentResource, updateInfoResource, hasBeenCertified});
	}

	
	@Test
	public void testValidateDerivedFromExist() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	AuditingActionEnum actionEnum = AuditingActionEnum.ACTIVATE_SERVICE_BY_API;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateDerivedFromExist", new Object[]{user, resource, actionEnum});
	}

	
	@Test(expected=UnsupportedOperationException.class)
	public void testValidateDerivedFromExtending() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource currentResource = createResourceObject(true);
	Resource updateInfoResource = createResourceObject(true);
	AuditingActionEnum actionEnum = AuditingActionEnum.ADD_CATEGORY;
	Either<Boolean,ResponseFormat> result;
	 
	 Either<Boolean, StorageOperationStatus> dataModelResponse = Either.right(StorageOperationStatus.BAD_REQUEST);
		when(toscaOperationFacade.validateToscaResourceNameExtends(currentResource.getDerivedFrom().get(0), updateInfoResource.getDerivedFrom().get(0))).thenReturn(dataModelResponse);
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateDerivedFromExtending", new Object[]{user, currentResource, updateInfoResource, actionEnum});
	}

	
	@Test
	public void testValidateResourceNameExists_2() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateResourceNameExists", new Object[]{resource});
	}

	
	@Test
	public void testValidateCategory_1() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	AuditingActionEnum actionEnum = AuditingActionEnum.ADD_ECOMP_USER_CREDENTIALS;
	boolean inTransaction = false;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateCategory", new Object[]{user, resource, actionEnum, inTransaction});
	}

	
	@Test
	public void testValidateCategoryListed() throws Exception {
	ResourceBusinessLogic testSubject;
	CategoryDefinition category = null;
	SubCategoryDefinition subcategory = null;
	boolean inTransaction = false;
	Either<Boolean,ResponseFormat> result;
	
	// test 1
	testSubject=createTestSubject();
	result=Deencapsulation.invoke(testSubject, "validateCategoryListed", new Object[]{CategoryDefinition.class, SubCategoryDefinition.class, inTransaction});
	
	}

	
	@Test
	public void testValidateVendorReleaseName_2() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	AuditingActionEnum actionEnum = null;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=testSubject.validateVendorReleaseName(user, resource, actionEnum);
	}

	
	@Test
	public void testValidateVendorReleaseName_3() throws Exception {
	ResourceBusinessLogic testSubject;
	String vendorRelease = "";
	Either<Boolean,ResponseFormat> result;
	
	// test 1
	testSubject=createTestSubject();vendorRelease = null;
	result=testSubject.validateVendorReleaseName(vendorRelease);
	}

	
	@Test
	public void testValidateVendorName_1() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	AuditingActionEnum actionEnum = AuditingActionEnum.ARTIFACT_DELETE;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateVendorName", new Object[]{user, resource, actionEnum});
	}

	
	@Test
	public void testValidateResourceVendorModelNumber_1() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	AuditingActionEnum actionEnum = AuditingActionEnum.ARTIFACT_DELETE;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateResourceVendorModelNumber", new Object[]{user, resource, actionEnum});
	}

	
	@Test
	public void testValidateVendorName_2() throws Exception {
	ResourceBusinessLogic testSubject;
	String vendorName = "";
	Either<Boolean,ResponseFormat> result;
	
	// test 1
	testSubject=createTestSubject();
	result=Deencapsulation.invoke(testSubject, "validateVendorName", new Object[]{vendorName});
	}

	
	@Test
	public void testValidateResourceVendorModelNumber_2() throws Exception {
	ResourceBusinessLogic testSubject;
	String resourceVendorModelNumber = "";
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateResourceVendorModelNumber", new Object[]{resourceVendorModelNumber});
	}

	
	@Test
	public void testValidateCost() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	AuditingActionEnum actionEnum = AuditingActionEnum.ADD_GROUPING;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateCost", new Object[]{user, resource, AuditingActionEnum.class});
	}

	
	@Test
	public void testValidateLicenseType() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	AuditingActionEnum actionEnum = AuditingActionEnum.ADD_KEY_TO_TOPIC_ACL;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateLicenseType", new Object[]{user, resource, actionEnum});
	}


	@Test
	public void testDeleteMarkedComponents() throws Exception {
	ResourceBusinessLogic testSubject;
	Either<List<String>,ResponseFormat> result;
	
	Either<List<String>, StorageOperationStatus> deleteMarkedElements = Either.right(StorageOperationStatus.BAD_REQUEST);
	when(toscaOperationFacade.deleteMarkedElements(ComponentTypeEnum.RESOURCE)).thenReturn(deleteMarkedElements);
	
	
	// default test
	testSubject=createTestSubject();result=testSubject.deleteMarkedComponents();
	}

	
	@Test
	public void testGetComponentTypeForResponse() throws Exception {
	ResourceBusinessLogic testSubject;Component component = null;
	String result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "getComponentTypeForResponse", new Object[]{Component.class});
	}

	
	@Test
	public void testCreateGroupsFromYaml() throws Exception {
	ResourceBusinessLogic testSubject;
	String yamlFileName = "";
	Map<String,Object> toscaJson = new HashMap<>();;
	Resource resource = createResourceObject(true);
	Either<Map<String,GroupDefinition>,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "createGroupsFromYaml", new Object[]{yamlFileName, toscaJson, resource});
	}

	
	@Test
	public void testCreateInputsFromYaml() throws Exception {
	ResourceBusinessLogic testSubject;
	String yamlFileName = "";
	Map<String,Object> toscaJson = new HashMap<>();;
	Resource resource = createResourceObject(true);
	Either<Map<String,InputDefinition>,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "createInputsFromYaml", new Object[]{yamlFileName, toscaJson, resource});
	}

	
	@Test
	public void testCreateGroupInfo() throws Exception {
	ResourceBusinessLogic testSubject;String groupName = "";
	Object groupTemplateJson = null;
	Either<GroupDefinition,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "createGroupInfo", new Object[]{groupName, Object.class});
	}

	@Test
	public void testGetValidComponentInstanceCapabilities() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resource = createResourceObject(true);
	String resourceId = resource.getUniqueId();
	Map<String,List<CapabilityDefinition>> defaultCapabilities = new HashMap<>();
	Map<String,List<UploadCapInfo>> uploadedCapabilities = new HashMap<>();
	Either<Map<String,List<CapabilityDefinition>>,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "getValidComponentInstanceCapabilities", new Object[]{resourceId, defaultCapabilities, uploadedCapabilities});
	}

	
	@Test
	public void testBuildNestedToscaResourceName() throws Exception {
	ResourceBusinessLogic testSubject;
	String nodeResourceType = "VF";
	String vfResourceName = "Resource12";
	String nodeTypeFullName = "org.openecomp.resource.VF.Resource12";
	ImmutablePair<String,String> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "buildNestedToscaResourceName", new Object[]{nodeResourceType, vfResourceName, nodeTypeFullName});
	}

	
	@Test
	public void testValidateAndUpdateInterfaces() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resourceUpdate = createResourceObject(true);;
	String resourceId = resourceUpdate.getUniqueId();
	Either<Boolean,ResponseFormat> result;
	
	
	Either<Component, StorageOperationStatus> resourceStorageOperationStatusEither = Either.right(StorageOperationStatus.BAD_REQUEST);
	when(toscaOperationFacade.getToscaElement(resourceUpdate.getUniqueId())).thenReturn(resourceStorageOperationStatusEither);
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateAndUpdateInterfaces", new Object[]{resourceId, resourceUpdate});
	}

	
	@Test
	public void testUpdateInterfaceDefinition() throws Exception {
	ResourceBusinessLogic testSubject;
	Resource resourceUpdate = createResourceObject(true);
	InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
	Collection<InterfaceDefinition> interfaceDefinitionListFromToscaName = new ArrayList<>();
	Either<InterfaceDefinition,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "updateInterfaceDefinition", new Object[]{resourceUpdate, interfaceDefinition, interfaceDefinitionListFromToscaName});
	}
	
	
}
