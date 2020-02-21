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

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.ServletContext;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.ElementOperationMock;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.ArtifactsResolver;
import org.openecomp.sdc.be.components.csar.CsarArtifactsAndGroupsBusinessLogic;
import org.openecomp.sdc.be.components.csar.CsarBusinessLogic;
import org.openecomp.sdc.be.components.csar.CsarInfo;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.generic.GenericTypeBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.components.merge.resource.ResourceDataMergeBusinessLogic;
import org.openecomp.sdc.be.components.merge.utils.MergeInstanceUtils;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.components.validation.component.ComponentContactIdValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentDescriptionValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentFieldValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentIconValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentNameValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentProjectCodeValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentTagsValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentValidator;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datamodel.api.HighestFilterEnum;
import org.openecomp.sdc.be.datamodel.utils.UiComponentDataConverter;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.facade.operations.CatalogOperation;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeTypeOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.ICapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CsarOperation;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.tosca.CsarUtils.NonMetaArtifactInfo;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

public class ResourceBusinessLogicTest {

	ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be");
	ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
	private static final Logger log = LoggerFactory.getLogger(ResourceBusinessLogicTest.class);
	private static final String RESOURCE_CATEGORY1 = "Network Layer 2-3";
	private static final String RESOURCE_SUBCATEGORY = "Router";

	private static final String UPDATED_SUBCATEGORY = "Gateway";

    private String resourceId = "resourceId1";
    private String operationId = "uniqueId1";
    Resource resourceUpdate;

	private static final String RESOURCE_NAME = "My-Resource_Name with   space";
	private static final String RESOURCE_TOSCA_NAME = "My-Resource_Tosca_Name";
	private static final String GENERIC_ROOT_NAME = "tosca.nodes.Root";
	private static final String GENERIC_VF_NAME = "org.openecomp.resource.abstract.nodes.VF";
	private static final String GENERIC_CR_NAME = "org.openecomp.resource.abstract.nodes.CR";
	private static final String GENERIC_PNF_NAME = "org.openecomp.resource.abstract.nodes.PNF";

	final ServletContext servletContext = Mockito.mock(ServletContext.class);
	IElementOperation mockElementDao;
    JanusGraphDao mockJanusGraphDao = Mockito.mock(JanusGraphDao.class);
	UserBusinessLogic mockUserAdmin = Mockito.mock(UserBusinessLogic.class);
	ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
	NodeTypeOperation nodeTypeOperation = Mockito.mock(NodeTypeOperation.class);
	NodeTemplateOperation nodeTemplateOperation = Mockito.mock(NodeTemplateOperation.class);
	TopologyTemplateOperation topologyTemplateOperation = Mockito.mock(TopologyTemplateOperation.class);
	final LifecycleBusinessLogic lifecycleBl = Mockito.mock(LifecycleBusinessLogic.class);
	final CatalogOperation catalogOperation = Mockito.mock(CatalogOperation.class);
	final ICapabilityTypeOperation capabilityTypeOperation = Mockito.mock(ICapabilityTypeOperation.class);
	final PropertyOperation propertyOperation = Mockito.mock(PropertyOperation.class);
	final ApplicationDataTypeCache applicationDataTypeCache = Mockito.mock(ApplicationDataTypeCache.class);
	WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
	UserValidations userValidations = Mockito.mock(UserValidations.class);
	WebApplicationContext webAppContext = Mockito.mock(WebApplicationContext.class);
    IInterfaceLifecycleOperation interfaceTypeOperation = Mockito.mock(IInterfaceLifecycleOperation.class);
    ArtifactCassandraDao artifactCassandraDao = Mockito.mock(ArtifactCassandraDao.class);
	IElementOperation elementDao = new ElementOperationMock();

	CsarUtils csarUtils = Mockito.mock(CsarUtils.class);
	UserBusinessLogic userBusinessLogic = Mockito.mock(UserBusinessLogic.class);
	IGroupOperation groupOperation = Mockito.mock(IGroupOperation.class);
	IGroupInstanceOperation groupInstanceOperation = Mockito.mock(IGroupInstanceOperation.class);
	IGroupTypeOperation groupTypeOperation = Mockito.mock(IGroupTypeOperation.class);
	GroupBusinessLogic groupBusinessLogic = Mockito.mock(GroupBusinessLogic.class);
	InterfaceOperation interfaceOperation = Mockito.mock(InterfaceOperation.class);
	ArtifactsOperations artifactToscaOperation = Mockito.mock(ArtifactsOperations.class);
	private PropertyBusinessLogic propertyBusinessLogic = Mockito.mock(PropertyBusinessLogic.class);
	ArtifactsResolver artifactsResolver = Mockito.mock(ArtifactsResolver.class);
	InterfaceLifecycleOperation interfaceLifecycleTypeOperation = Mockito.mock(InterfaceLifecycleOperation.class);
	ComponentInstanceBusinessLogic componentInstanceBusinessLogic = Mockito.mock(ComponentInstanceBusinessLogic.class);
	ResourceImportManager resourceImportManager = Mockito.mock(ResourceImportManager.class);
	InputsBusinessLogic inputsBusinessLogic = Mockito.mock(InputsBusinessLogic.class);
	CompositionBusinessLogic compositionBusinessLogic = Mockito.mock(CompositionBusinessLogic.class);
	ResourceDataMergeBusinessLogic resourceDataMergeBusinessLogic = Mockito.mock(ResourceDataMergeBusinessLogic.class);
	CsarArtifactsAndGroupsBusinessLogic csarArtifactsAndGroupsBusinessLogic = Mockito.mock(CsarArtifactsAndGroupsBusinessLogic.class);
	MergeInstanceUtils mergeInstanceUtils = Mockito.mock(MergeInstanceUtils.class);
	UiComponentDataConverter uiComponentDataConverter = Mockito.mock(UiComponentDataConverter.class);
	ToscaExportHandler toscaExportHandler = Mockito.mock(ToscaExportHandler.class);



	@InjectMocks
	ResponseFormatManager responseManager = null;
	GraphLockOperation graphLockOperation = Mockito.mock(GraphLockOperation.class);
	User user = null;
	Resource resourceResponse = null;
	Resource genericVF = null;
	Resource genericCR = null;
	Resource genericVFC = null;
	Resource genericPNF = null;
	Resource rootType = null;
	ComponentsUtils componentsUtils =  new ComponentsUtils(Mockito.mock(AuditingManager.class));
	ArtifactsBusinessLogic artifactManager = new ArtifactsBusinessLogic(artifactCassandraDao, toscaExportHandler, csarUtils, lifecycleBl,
			userBusinessLogic, artifactsResolver, elementDao, groupOperation, groupInstanceOperation, groupTypeOperation,
			interfaceOperation, interfaceLifecycleTypeOperation, artifactToscaOperation);
	CsarOperation csarOperation = Mockito.mock(CsarOperation.class);
	@InjectMocks
	CsarBusinessLogic csarBusinessLogic ;
	Map<String, DataTypeDefinition> emptyDataTypes = new HashMap<>();
	List<Resource> reslist;
	private GenericTypeBusinessLogic genericTypeBusinessLogic = Mockito.mock(GenericTypeBusinessLogic.class);
	protected ComponentDescriptionValidator componentDescriptionValidator =  new ComponentDescriptionValidator(componentsUtils);
	protected ComponentProjectCodeValidator componentProjectCodeValidator =  new ComponentProjectCodeValidator(componentsUtils);
	protected ComponentIconValidator componentIconValidator = new ComponentIconValidator(componentsUtils);
	protected ComponentContactIdValidator componentContactIdValidator = new ComponentContactIdValidator(componentsUtils);
	protected ComponentTagsValidator componentTagsValidator = new ComponentTagsValidator(componentsUtils);
	protected ComponentNameValidator componentNameValidator = new ComponentNameValidator(componentsUtils, toscaOperationFacade);
	private ComponentValidator componentValidator = createComponentValidator();
    private SoftwareInformationBusinessLogic softwareInformationBusinessLogic = Mockito.mock(SoftwareInformationBusinessLogic.class);

	private ComponentValidator createComponentValidator() {
		List<ComponentFieldValidator> componentFieldValidators = Arrays.asList(componentNameValidator,
				componentDescriptionValidator, componentProjectCodeValidator,
				componentIconValidator, componentContactIdValidator,
				componentTagsValidator);
		return new ComponentValidator(componentsUtils,componentFieldValidators);
	}

	ResourceBusinessLogic bl;
	public ResourceBusinessLogicTest() {
	}

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.reset(propertyOperation);

		// Elements
		mockElementDao = new ElementOperationMock();


		// User data and management
		user = new User();
		user.setUserId("jh0003");
		user.setFirstName("Jimmi");
		user.setLastName("Hendrix");
		user.setRole(Role.ADMIN.name());

		when(mockUserAdmin.getUser("jh0003", false)).thenReturn(user);
		when(userValidations.validateUserExists(eq(user.getUserId()))).thenReturn(user);
		when(userValidations.validateUserNotEmpty(eq(user), anyString())).thenReturn(user);
		// Servlet Context attributes
		when(servletContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).thenReturn(configurationManager);
		when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR))
				.thenReturn(webAppContextWrapper);
		when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webAppContext);
		when(webAppContext.getBean(IElementOperation.class)).thenReturn(mockElementDao);

		Either<Boolean, StorageOperationStatus> eitherFalse = Either.left(true);
		when(toscaOperationFacade.validateComponentNameExists("tosca.nodes.Root", ResourceTypeEnum.VFC, ComponentTypeEnum.RESOURCE))
				.thenReturn(eitherFalse);


		Either<Boolean, StorageOperationStatus> eitherCountExist = Either.left(true);
		when(toscaOperationFacade.validateComponentNameExists("alreadyExists", ResourceTypeEnum.VFC,
				ComponentTypeEnum.RESOURCE)).thenReturn(eitherCountExist);

		Either<Boolean, StorageOperationStatus> eitherCount = Either.left(false);
		when(toscaOperationFacade.validateComponentNameExists(eq(RESOURCE_NAME), any(ResourceTypeEnum.class),
				eq(ComponentTypeEnum.RESOURCE))).thenReturn(eitherCount);
		/*
		 * when(toscaOperationFacade.validateComponentNameExists(RESOURCE_NAME,
		 * ResourceTypeEnum.VF,
        when(interfaceOperation.updateInterface(anyString(), anyObject())).thenReturn(Either.left(InterfaceOperationTestUtils.mockInterfaceDefinitionToReturn(RESOURCE_NAME)));
		 * ComponentTypeEnum.RESOURCE)).thenReturn(eitherCount);
		 * when(toscaOperationFacade.validateComponentNameExists(RESOURCE_NAME,
		 * ResourceTypeEnum.PNF,
		 * ComponentTypeEnum.RESOURCE)).thenReturn(eitherCount);
		 * when(toscaOperationFacade.validateComponentNameExists(RESOURCE_NAME,
		 * ResourceTypeEnum.CR,
		 * ComponentTypeEnum.RESOURCE)).thenReturn(eitherCount);
		 */
		Either<Boolean, StorageOperationStatus> validateDerivedExists = Either.left(true);
		when(toscaOperationFacade.validateToscaResourceNameExists("tosca.nodes.Root")).thenReturn(validateDerivedExists);

		Either<Boolean, StorageOperationStatus> validateDerivedNotExists = Either.left(false);
		when(toscaOperationFacade.validateToscaResourceNameExists("kuku")).thenReturn(validateDerivedNotExists);
		when(graphLockOperation.lockComponent(anyString(), eq(NodeTypeEnum.Resource)))
				.thenReturn(StorageOperationStatus.OK);
		when(graphLockOperation.lockComponentByName(anyString(), eq(NodeTypeEnum.Resource)))
				.thenReturn(StorageOperationStatus.OK);

		// createResource
		resourceResponse = createResourceObject(true);
		Either<Resource, StorageOperationStatus> eitherCreate = Either.left(resourceResponse);
		when(toscaOperationFacade.createToscaComponent(any(Resource.class))).thenReturn(eitherCreate);
		when(catalogOperation.updateCatalog(Mockito.any(), Mockito.any())).thenReturn(ActionStatus.OK);
		Map<String, DataTypeDefinition> emptyDataTypes = new HashMap<>();
		when(applicationDataTypeCache.getAll()).thenReturn(Either.left(emptyDataTypes));
        when(mockJanusGraphDao.commit()).thenReturn(JanusGraphOperationStatus.OK);

		// BL object
		artifactManager.setNodeTemplateOperation(nodeTemplateOperation);
		bl = new ResourceBusinessLogic(mockElementDao, groupOperation, groupInstanceOperation, groupTypeOperation, groupBusinessLogic,
				interfaceOperation, interfaceLifecycleTypeOperation, artifactManager, componentInstanceBusinessLogic,
				resourceImportManager, inputsBusinessLogic, compositionBusinessLogic, resourceDataMergeBusinessLogic,
				csarArtifactsAndGroupsBusinessLogic, mergeInstanceUtils, uiComponentDataConverter, csarBusinessLogic,
				artifactToscaOperation, propertyBusinessLogic, componentContactIdValidator, componentNameValidator,
				componentTagsValidator, componentValidator,	componentIconValidator, componentProjectCodeValidator, componentDescriptionValidator);
		bl.setElementDao(mockElementDao);
		bl.setUserAdmin(mockUserAdmin);
		bl.setCapabilityTypeOperation(capabilityTypeOperation);
		bl.setComponentsUtils(componentsUtils);
		bl.setLifecycleManager(lifecycleBl);
		bl.setGraphLockOperation(graphLockOperation);
		bl.setArtifactsManager(artifactManager);
		bl.setPropertyOperation(propertyOperation);
        bl.setJanusGraphDao(mockJanusGraphDao);
		bl.setApplicationDataTypeCache(applicationDataTypeCache);
		bl.setGenericTypeBusinessLogic(genericTypeBusinessLogic);
		bl.setCatalogOperations(catalogOperation);
		toscaOperationFacade.setNodeTypeOperation(nodeTypeOperation);
		toscaOperationFacade.setTopologyTemplateOperation(topologyTemplateOperation);
		bl.setToscaOperationFacade(toscaOperationFacade);
		bl.setUserValidations(userValidations);
        bl.setInterfaceTypeOperation(interfaceTypeOperation);

		csarBusinessLogic.setCsarOperation(csarOperation);
		Resource resourceCsar = createResourceObjectCsar(true);
		setCanWorkOnResource(resourceCsar);
		Either<Component, StorageOperationStatus> oldResourceRes = Either.left(resourceCsar);
		when(toscaOperationFacade.getToscaFullElement(resourceCsar.getUniqueId())).thenReturn(oldResourceRes);
		responseManager = ResponseFormatManager.getInstance();
		bl.setComponentIconValidator(componentIconValidator);
		bl.setComponentNameValidator(componentNameValidator);
		bl.setComponentDescriptionValidator(componentDescriptionValidator);
		bl.setComponentTagsValidator(componentTagsValidator);
		bl.setComponentContactIdValidator(componentContactIdValidator);
		bl.setComponentProjectCodeValidator(componentProjectCodeValidator);
		bl.setComponentValidator(componentValidator);
        reslist = new ArrayList<Resource>();
        reslist.add(resourceResponse);
        reslist.add(genericVF);
        reslist.add(genericCR);
        reslist.add(genericVFC);
        reslist.add(genericPNF);
        Either<List<Resource>, StorageOperationStatus> returneval= Either.left(reslist);
        when(toscaOperationFacade.getAllCertifiedResources(true, true)).thenReturn(returneval);
        when(toscaOperationFacade.validateComponentNameUniqueness("Resource", ResourceTypeEnum.CR, ComponentTypeEnum.RESOURCE)).thenReturn(Either.left(true));
        Either<List<Resource>, StorageOperationStatus> returnevalexception= Either.right(StorageOperationStatus.BAD_REQUEST);
        when(toscaOperationFacade.getAllCertifiedResources(false, false)).thenReturn(returnevalexception);

	}

	private Resource createResourceObject(boolean afterCreate) {
		Resource resource = new Resource();
		resource.setName(RESOURCE_NAME);
		resource.setToscaResourceName(RESOURCE_TOSCA_NAME);
		resource.addCategory(RESOURCE_CATEGORY1, RESOURCE_SUBCATEGORY);
		resource.setDescription("My short description");
		List<String> tgs = new ArrayList<>();
		tgs.add("test");
		tgs.add(resource.getName());
		resource.setTags(tgs);
		List<String> template = new ArrayList<>();
		template.add("tosca.nodes.Root");
		resource.setDerivedFrom(template);
		resource.setVendorName("Motorola");
		resource.setVendorRelease("1.0.0");
		resource.setContactId("ya5467");
		resource.setIcon("defaulticon");

		if (afterCreate) {
			resource.setName(resource.getName());
			resource.setVersion("0.1");
			resource.setUniqueId(resource.getName()
					.toLowerCase() + ":" + resource.getVersion());
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
		List<String> tgs = new ArrayList<>();
		tgs.add("test");
		tgs.add(resource.getName());
		resource.setTags(tgs);
		List<String> template = new ArrayList<>();
		template.add("tosca.nodes.Root");
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

			resource.setUniqueId(resource.getName()
					.toLowerCase() + ":" + resource.getVersion());
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
		Resource createdResource = null;
		try {
			createdResource = bl.createResource(resource, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
			assertThat(createResourceObject(true)).isEqualTo(createdResource);
		} catch (ComponentException e) {
			assertThat(new Integer(200)).isEqualTo(e.getResponseFormat()
					.getStatus());
		}
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
		when(toscaOperationFacade.validateToscaResourceNameExists("tosca.nodes.Root")).thenReturn(validateDerivedExists);
        Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
        when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);
		Resource createdResource = null;
		try {
			createdResource = bl.validateAndUpdateResourceFromCsar(resource, user, null, null, resource.getUniqueId());
			assertThat(resource.getUniqueId()).isEqualTo(createdResource.getUniqueId());
		} catch (ComponentException e) {
			assertThat(new Integer(200)).isEqualTo(e.getResponseFormat()
					.getStatus());
		}
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
		resourceExist.getTags()
				.add(resourceName);
		validateUserRoles(Role.ADMIN, Role.DESIGNER);
		try {
			bl.createResource(resourceExist, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_NAME_ALREADY_EXIST,
					ComponentTypeEnum.RESOURCE.getValue(), resourceName);
		}
	}

	private void testResourceNameEmpty() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setName(null);

		try {
			bl.createResource(resourceExist, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.MISSING_COMPONENT_NAME, ComponentTypeEnum.RESOURCE.getValue());
		}
	}

	private void testResourceNameExceedsLimit() {
		Resource resourceExccedsNameLimit = createResourceObject(false);
		// 51 chars, the limit is 50
		String tooLongResourceName = "zCRCAWjqte0DtgcAAMmcJcXeNubeX1p1vOZNTShAHOYNAHvV3iK";
		resourceExccedsNameLimit.setName(tooLongResourceName);

		try {
			bl.createResource(resourceExccedsNameLimit, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_NAME_EXCEEDS_LIMIT,
					ComponentTypeEnum.RESOURCE.getValue(), "" + ValidationUtils.COMPONENT_NAME_MAX_LENGTH);
		}
	}

	private void testResourceNameWrongFormat() {
		Resource resource = createResourceObject(false);
		// contains :
		String nameWrongFormat = "ljg?fd";
		resource.setName(nameWrongFormat);

		try {
			bl.createResource(resource, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.INVALID_COMPONENT_NAME, ComponentTypeEnum.RESOURCE.getValue());
		}
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
		try {
			bl.createResource(resourceExccedsDescLimit, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_DESCRIPTION_EXCEEDS_LIMIT,
					ComponentTypeEnum.RESOURCE.getValue(), "" + ValidationUtils.COMPONENT_DESCRIPTION_MAX_LENGTH);
		}
	}

	private void testResourceDescNotEnglish() {
		Resource notEnglish = createResourceObject(false);
		// Not english
		String notEnglishDesc = "\uC2B5";
		notEnglish.setDescription(notEnglishDesc);

		try {
			bl.createResource(notEnglish, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_INVALID_DESCRIPTION,
					ComponentTypeEnum.RESOURCE.getValue());
		}
	}

	private void testResourceDescriptionEmpty() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setDescription("");

		try {
			bl.createResource(resourceExist, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_MISSING_DESCRIPTION,
					ComponentTypeEnum.RESOURCE.getValue());
		}
	}

	private void testResourceDescriptionMissing() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setDescription(null);

		try {
			bl.createResource(resourceExist, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_MISSING_DESCRIPTION,
					ComponentTypeEnum.RESOURCE.getValue());
		}
	}
	// Resource description - end
	// Resource icon start

	private void testResourceIconMissing() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setIcon(null);

		try {
			bl.createResource(resourceExist, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_MISSING_ICON, ComponentTypeEnum.RESOURCE.getValue());
		}
	}

	private void testResourceIconInvalid() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setIcon("kjk3453^&");

		try {
			bl.createResource(resourceExist, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_INVALID_ICON, ComponentTypeEnum.RESOURCE.getValue());
		}
	}

	private void testResourceIconExceedsLimit() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setIcon("dsjfhskdfhskjdhfskjdhkjdhfkshdfksjsdkfhsdfsdfsdfsfsdfsf");
		try {
			bl.createResource(resourceExist, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_ICON_EXCEEDS_LIMIT,
					ComponentTypeEnum.RESOURCE.getValue(), "" + ValidationUtils.ICON_MAX_LENGTH);
		}
	}

	// Resource icon end
	// Resource tags - start
	private void testResourceTagNotExist() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setTags(null);
		try {
			bl.createResource(resourceExist, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_MISSING_TAGS);
		}
	}

	private void testResourceTagEmpty() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setTags(new ArrayList<>());
		try {
			bl.createResource(resourceExist, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_MISSING_TAGS);
		}
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

		List<String> tagsList = new ArrayList<>();
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
		try {
			bl.createResource(resourceExccedsNameLimit, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_TAGS_EXCEED_LIMIT,
					"" + ValidationUtils.TAG_LIST_MAX_LENGTH);
		}
	}

	private void testTagsSingleExceedsLimit() {
		Resource resourceExccedsNameLimit = createResourceObject(false);
		String tag1 = "afzs2qLBb5X6tZhiunkcEwiFX1qRQY8YZl3y3Du5M5xeQY5Nq9afcFHDZ9HaURw43gH27nAUWM36bMbMylwTFSzzNV8NO4v4ripe6Q15Vc2nPOFI";
		String tag2 = resourceExccedsNameLimit.getName();
		List<String> tagsList = new ArrayList<>();
		tagsList.add(tag1);
		tagsList.add(tag2);

		resourceExccedsNameLimit.setTags(tagsList);
		try {
			bl.createResource(resourceExccedsNameLimit, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_SINGLE_TAG_EXCEED_LIMIT,
					"" + ValidationUtils.TAG_MAX_LENGTH);
		}
	}

	private void testTagsNoServiceName() {
		Resource serviceExccedsNameLimit = createResourceObject(false);
		String tag1 = "afzs2qLBb";
		List<String> tagsList = new ArrayList<>();
		tagsList.add(tag1);
		serviceExccedsNameLimit.setTags(tagsList);
		try {
			bl.createResource(serviceExccedsNameLimit, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_INVALID_TAGS_NO_COMP_NAME);
		}
	}

	private void testInvalidTag() {
		Resource serviceExccedsNameLimit = createResourceObject(false);
		String tag1 = "afzs2qLBb%#%";
		List<String> tagsList = new ArrayList<>();
		tagsList.add(tag1);
		serviceExccedsNameLimit.setTags(tagsList);
		try {
			bl.createResource(serviceExccedsNameLimit, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.INVALID_FIELD_FORMAT, new String[] { "Resource", "tag" });
		}
	}

	// Resource tags - stop
	// Resource contact start

	private void testContactIdTooLong() {
		Resource resourceContactId = createResourceObject(false);
		// 59 chars instead of 50
		String contactIdTooLong = "thisNameIsVeryLongAndExeccedsTheNormalLengthForContactId";
		resourceContactId.setContactId(contactIdTooLong);

		try {
			bl.createResource(resourceContactId, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_INVALID_CONTACT, ComponentTypeEnum.RESOURCE.getValue());
		}
	}

	private void testContactIdWrongFormatCreate() {
		Resource resourceContactId = createResourceObject(false);
		// 3 letters and 3 digits and special characters
		String contactIdFormatWrong = "yrt134!!!";
		resourceContactId.setContactId(contactIdFormatWrong);
		try {
			bl.createResource(resourceContactId, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_INVALID_CONTACT, ComponentTypeEnum.RESOURCE.getValue());
		}
	}

	private void testResourceContactIdEmpty() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setContactId("");
		try {
			bl.createResource(resourceExist, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_MISSING_CONTACT, ComponentTypeEnum.RESOURCE.getValue());
		}
	}

	private void testResourceContactIdMissing() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setContactId(null);
		try {
			bl.createResource(resourceExist, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_MISSING_CONTACT, ComponentTypeEnum.RESOURCE.getValue());
		}
	}

	private void testVendorNameExceedsLimit() {
		Resource resourceExccedsVendorNameLimit = createResourceObject(false);
		String tooLongVendorName = "h1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9E";
		resourceExccedsVendorNameLimit.setVendorName(tooLongVendorName);
		try {
			bl.createResource(resourceExccedsVendorNameLimit, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.VENDOR_NAME_EXCEEDS_LIMIT,
					"" + ValidationUtils.VENDOR_NAME_MAX_LENGTH);
		}
	}

	private void testResourceVendorModelNumberExceedsLimit() {
		Resource resourceExccedsVendorModelNumberLimit = createResourceObject(false);
		String tooLongVendorModelNumber = "h1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9E";
		resourceExccedsVendorModelNumberLimit.setResourceVendorModelNumber(tooLongVendorModelNumber);
		try {
			bl.createResource(resourceExccedsVendorModelNumberLimit, AuditingActionEnum.CREATE_RESOURCE, user, null,
					null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.RESOURCE_VENDOR_MODEL_NUMBER_EXCEEDS_LIMIT,
					"" + ValidationUtils.RESOURCE_VENDOR_MODEL_NUMBER_MAX_LENGTH);
		}
	}

	private void testVendorNameWrongFormatCreate() {
		Resource resource = createResourceObject(false);
		// contains *
		String nameWrongFormat = "ljg*fd";
		resource.setVendorName(nameWrongFormat);
		try {
			bl.createResource(resource, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_VENDOR_NAME, nameWrongFormat);
		}
	}

	private void testVendorReleaseWrongFormat() {
		Resource resource = createResourceObject(false);
		// contains >
        String vendorReleaseWrongFormat = "1>2";
        resource.setVendorRelease(vendorReleaseWrongFormat);
		try {
			bl.createResource(resource, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_VENDOR_RELEASE, vendorReleaseWrongFormat);
		}
	}

	private void testVendorReleaseExceedsLimitCreate() {
		Resource resourceExccedsNameLimit = createResourceObject(false);
		String tooLongVendorRelease = "h1KSyJh9Eh1KSyJh9Eh1KSyJh9Eh1KSyJh9E";
		resourceExccedsNameLimit.setVendorRelease(tooLongVendorRelease);
		try {
			bl.createResource(resourceExccedsNameLimit, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.VENDOR_RELEASE_EXCEEDS_LIMIT,
					"" + ValidationUtils.VENDOR_RELEASE_MAX_LENGTH);
		}
	}

	private void testResourceVendorNameMissing() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setVendorName(null);
		try {
			bl.createResource(resourceExist, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.MISSING_VENDOR_NAME);
		}
	}

	private void testResourceVendorReleaseMissing() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setVendorRelease(null);
		try {
			bl.createResource(resourceExist, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.MISSING_VENDOR_RELEASE);
		}
	}

	// Resource vendor name/release stop
	// Category start
	private void testResourceCategoryExist() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setCategories(null);
		try {
			bl.createResource(resourceExist, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
		}
	}

	private void testResourceBadCategoryCreate() {

		Resource resourceExist = createResourceObject(false);
		resourceExist.setCategories(null);
		resourceExist.addCategory("koko", "koko");
		try {
			bl.createResource(resourceExist, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_INVALID_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
		}
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
		Resource createdResource;
		try {
			createdResource = bl.createResource(createResourceObject, AuditingActionEnum.CREATE_RESOURCE, user, null,
					null);
			createResourceObjectAfterCreate.setCost(cost);
			createResourceObjectAfterCreate.setLicenseType(licenseType);
			assertThat(createResourceObjectAfterCreate).isEqualTo(createdResource);
		} catch (ComponentException e) {
			assertThat(new Integer(200)).isEqualTo(e.getResponseFormat()
					.getStatus());
		}
	}

	private void testCostWrongFormatCreate() {
		Resource resourceCost = createResourceObject(false);
		// Comma instead of fullstop
		String cost = "12356,464";
		resourceCost.setCost(cost);
		try {
			bl.createResource(resourceCost, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.INVALID_CONTENT);
		}
	}

	// Cost stop
	// License type start
	private void testLicenseTypeWrongFormatCreate() {
		Resource resourceLicenseType = createResourceObject(false);
		// lowcase
		String licenseType = "cpu";
		resourceLicenseType.setLicenseType(licenseType);
		try {
			bl.createResource(resourceLicenseType, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.INVALID_CONTENT);
		}
	}

	// License type stop
	// Derived from start
	private void testResourceTemplateNotExist() {
		Resource resourceExist = createResourceObject(false);
		List<String> list = null;
		resourceExist.setDerivedFrom(list);
		try {
			bl.createResource(resourceExist, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.MISSING_DERIVED_FROM_TEMPLATE);
		}
	}

	private void testResourceTemplateEmpty() {
		Resource resourceExist = createResourceObject(false);
		resourceExist.setDerivedFrom(new ArrayList<>());
		try {
			bl.createResource(resourceExist, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.MISSING_DERIVED_FROM_TEMPLATE);
		}
	}

	private void testResourceTemplateInvalid() {
		Resource resourceExist = createResourceObject(false);
		ArrayList<String> derivedFrom = new ArrayList<>();
		derivedFrom.add("kuku");
		resourceExist.setDerivedFrom(derivedFrom);
		try {
			bl.createResource(resourceExist, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.PARENT_RESOURCE_NOT_FOUND);
		}
	}

	// Derived from stop
	private void assertComponentException(ComponentException e, ActionStatus expectedStatus, String... variables) {
		ResponseFormat actualResponse = e.getResponseFormat() != null ? e.getResponseFormat()
				: componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams());
		assertResponse(actualResponse, expectedStatus, variables);
	}

	private void assertResponse(ResponseFormat actualResponse, ActionStatus expectedStatus, String... variables) {
		ResponseFormat expectedResponse = responseManager.getResponseFormat(expectedStatus, variables);
		assertThat(expectedResponse.getStatus()).isEqualTo(actualResponse.getStatus());
		assertThat(expectedResponse.getFormattedMessage()).isEqualTo(actualResponse.getFormattedMessage());
	}

	private void assertResponse(Either<Resource, ResponseFormat> createResponse, ActionStatus expectedStatus,
			String... variables) {
		assertResponse(createResponse.right()
				.value(), expectedStatus, variables);
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
		try {
			bl.updateResourceMetadata(resource.getUniqueId(), updatedResource, null, user, false);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.INVALID_COMPONENT_NAME, ComponentTypeEnum.RESOURCE.getValue());
		}
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
		try {
			bl.updateResourceMetadata(resource.getUniqueId(), updatedResource, null, user, false);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.RESOURCE_NAME_CANNOT_BE_CHANGED);
		}
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
		try {
			bl.updateResourceMetadata(resource.getUniqueId(), updatedResource, null, user, false);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_NAME_ALREADY_EXIST,
					ComponentTypeEnum.RESOURCE.getValue(), resourceName);
		}
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
		try {
			bl.updateResourceMetadata(resource.getUniqueId(), updatedResource, null, user, false);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_DESCRIPTION_EXCEEDS_LIMIT,
					ComponentTypeEnum.RESOURCE.getValue(), "" + ValidationUtils.COMPONENT_DESCRIPTION_MAX_LENGTH);
		}
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
		try {
			bl.updateResourceMetadata(resource.getUniqueId(), updatedResource, null, user, false);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_INVALID_ICON, ComponentTypeEnum.RESOURCE.getValue());
		}
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
		try {
			bl.updateResourceMetadata(resource.getUniqueId(), updatedResource, null, user, false);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.RESOURCE_ICON_CANNOT_BE_CHANGED);
		}
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

		List<String> tagsList = new ArrayList<>();
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
		try {
			bl.updateResourceMetadata(resource.getUniqueId(), updatedResource, null, user, false);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_TAGS_EXCEED_LIMIT,
					"" + ValidationUtils.TAG_LIST_MAX_LENGTH);
		}
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
		try {
			bl.updateResourceMetadata(resource.getUniqueId(), updatedResource, null, user, false);
		} catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_VENDOR_NAME, nameWrongFormat);
		}
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
		try {
			bl.updateResourceMetadata(resource.getUniqueId(), updatedResource, null, user, false);
		} catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_VENDOR_NAME, nameWrongFormat);
		}
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
		try {
			bl.updateResourceMetadata(resource.getUniqueId(), updatedResource, null, user, false);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.VENDOR_RELEASE_EXCEEDS_LIMIT,
					"" + ValidationUtils.VENDOR_RELEASE_MAX_LENGTH);
		}
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
		try {
			bl.updateResourceMetadata(resourceId, updatedResource, null, user, false);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.COMPONENT_INVALID_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
		}
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
		when(toscaOperationFacade.updateToscaElement(updatedResource)).thenReturn(dataModelResponse);
		try {
			bl.updateResourceMetadata(resourceId, updatedResource, null, user, false);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.RESOURCE_CATEGORY_CANNOT_BE_CHANGED);
		}
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
		try {
			bl.updateResourceMetadata(resourceId, updatedResource, null, user, false);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.MISSING_DERIVED_FROM_TEMPLATE);
		}
	}

	@Test
	public void testResourceTemplateEmpty_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);
		String resourceId = resource.getUniqueId();

		// this is in order to prevent failing with 403 earlier
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);

		updatedResource.setDerivedFrom(new ArrayList<>());
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);
		try {
			bl.updateResourceMetadata(resourceId, updatedResource, null, user, false);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.MISSING_DERIVED_FROM_TEMPLATE);
		}
	}

	@Test
	public void testResourceTemplateInvalid_UPDATE() {
		Resource resource = createResourceObject(true);
		Resource updatedResource = createResourceObject(true);
		String resourceId = resource.getUniqueId();

		// this is in order to prevent failing with 403 earlier
		Either<Component, StorageOperationStatus> eitherUpdate = Either.left(setCanWorkOnResource(resource));
		when(toscaOperationFacade.getToscaElement(resource.getUniqueId())).thenReturn(eitherUpdate);

		ArrayList<String> derivedFrom = new ArrayList<>();
		derivedFrom.add("kuku");
		updatedResource.setDerivedFrom(derivedFrom);
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(resource)).thenReturn(dataModelResponse);
		try {
			bl.updateResourceMetadata(resourceId, updatedResource, null, user, false);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.PARENT_RESOURCE_NOT_FOUND);
		}
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
		when(toscaOperationFacade.validateToscaResourceNameExtends(anyString(), anyString()))
				.thenReturn(isToscaNameExtending);

		Either<Map<String, PropertyDefinition>, StorageOperationStatus> findPropertiesOfNode = Either
				.left(new HashMap<>());
		when(propertyOperation.deleteAllPropertiesAssociatedToNode(any(NodeTypeEnum.class), anyString()))
				.thenReturn(findPropertiesOfNode);

		resource.setVersion("1.0");

		ArrayList<String> derivedFrom = new ArrayList<>();
		derivedFrom.add("tosca.nodes.Root");
		updatedResource.setDerivedFrom(derivedFrom);
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(updatedResource);
		when(toscaOperationFacade.updateToscaElement(updatedResource)).thenReturn(dataModelResponse);
		Resource createdResource = bl.updateResourceMetadata(resourceId, updatedResource, null, user, false);
		assertThat(createdResource).isNotNull();
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
		when(toscaOperationFacade.validateToscaResourceNameExtends(anyString(), anyString()))
				.thenReturn(isToscaNameExtending);

		resource.setVersion("1.0");

		ArrayList<String> derivedFrom = new ArrayList<>();
		derivedFrom.add("tosca.nodes.Root");
		updatedResource.setDerivedFrom(derivedFrom);
		Either<Resource, StorageOperationStatus> dataModelResponse = Either.left(resource);
		when(toscaOperationFacade.updateToscaElement(updatedResource)).thenReturn(dataModelResponse);
		Either<Map<String, PropertyDefinition>, StorageOperationStatus> findPropertiesOfNode = Either
				.left(new HashMap<>());
		when(propertyOperation.deleteAllPropertiesAssociatedToNode(any(NodeTypeEnum.class), anyString()))
				.thenReturn(findPropertiesOfNode);

		try {
			bl.updateResourceMetadata(resourceId, updatedResource, null, user, false);
		} catch (ComponentException e) {
			assertComponentException(e, ActionStatus.PARENT_RESOURCE_DOES_NOT_EXTEND);
		}
	}
	// Derived from stop

	@Test
	public void createOrUpdateResourceAlreadyCheckout() {
		createRoot();
		Resource resourceExist = createResourceObject(false);
		validateUserRoles(Role.ADMIN, Role.DESIGNER);
		Resource createdResource = bl.createResource(resourceExist, AuditingActionEnum.CREATE_RESOURCE, user, null,
				null);
		createdResource.setLastUpdaterUserId(user.getUserId());
		assertThat(createdResource).isNotNull();
		Either<Resource, StorageOperationStatus> getLatestResult = Either.left(createdResource);
		Either<Component, StorageOperationStatus> getCompLatestResult = Either.left(createdResource);
		when(toscaOperationFacade.getLatestByToscaResourceName(resourceExist.getToscaResourceName()))
				.thenReturn(getCompLatestResult);
		when(toscaOperationFacade.overrideComponent(any(Resource.class), any(Resource.class)))
				.thenReturn(getLatestResult);

		Resource resourceToUpdtae = createResourceObject(false);

		ImmutablePair<Resource, ActionStatus> createOrUpdateResource = bl
				.createOrUpdateResourceByImport(resourceToUpdtae, user, false, false, false, null, null, false);
		assertNotNull(createOrUpdateResource);

		Mockito.verify(toscaOperationFacade, Mockito.times(1))
				.overrideComponent(any(Resource.class), any(Resource.class));
		Mockito.verify(lifecycleBl, Mockito.times(0))
				.changeState(anyString(), eq(user), eq(LifeCycleTransitionEnum.CHECKOUT),
						any(LifecycleChangeInfoWithAction.class), Mockito.anyBoolean(), Mockito.anyBoolean());

	}

	@Test
	public void createOrUpdateResourceCertified() {
		createRoot();
		Resource resourceExist = createResourceObject(false);
		validateUserRoles(Role.ADMIN, Role.DESIGNER);
		Resource createdResource = bl.createResource(resourceExist, AuditingActionEnum.CREATE_RESOURCE, user, null,
				null);

		assertThat(createdResource).isNotNull();
		createdResource.setLifecycleState(LifecycleStateEnum.CERTIFIED);
		createdResource.setVersion("1.0");

		Either<Resource, StorageOperationStatus> getLatestResult = Either.left(createdResource);
		Either<Component, StorageOperationStatus> getCompLatestResult = Either.left(createdResource);
		when(toscaOperationFacade.getLatestByToscaResourceName(resourceExist.getToscaResourceName()))
				.thenReturn(getCompLatestResult);
		when(toscaOperationFacade.overrideComponent(any(Resource.class), any(Resource.class)))
				.thenReturn(getLatestResult);

		when(lifecycleBl.changeState(anyString(), eq(user), eq(LifeCycleTransitionEnum.CHECKOUT),
				any(LifecycleChangeInfoWithAction.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
						.thenReturn(Either.left(createdResource));

		Resource resourceToUpdtae = createResourceObject(false);

		ImmutablePair<Resource, ActionStatus> createOrUpdateResource = bl
				.createOrUpdateResourceByImport(resourceToUpdtae, user, false, false, false, null, null, false);
		assertNotNull(createOrUpdateResource);

		Mockito.verify(toscaOperationFacade, Mockito.times(1))
				.overrideComponent(any(Resource.class), any(Resource.class));
		Mockito.verify(lifecycleBl, Mockito.times(1))
				.changeState(anyString(), eq(user), eq(LifeCycleTransitionEnum.CHECKOUT),
						any(LifecycleChangeInfoWithAction.class), Mockito.anyBoolean(), Mockito.anyBoolean());

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

		ImmutablePair<Resource, ActionStatus> createOrUpdateResource = bl
				.createOrUpdateResourceByImport(resourceToUpdtae, user, false, false, false, null, null, false);
		assertThat(createOrUpdateResource).isNotNull();

		Mockito.verify(toscaOperationFacade, times(1))
				.createToscaComponent(eq(resourceToUpdtae));
		Mockito.verify(toscaOperationFacade, Mockito.times(0))
				.overrideComponent(any(Resource.class), any(Resource.class));
		Mockito.verify(lifecycleBl, Mockito.times(0))
				.changeState(anyString(), eq(user), eq(LifeCycleTransitionEnum.CHECKOUT),
						any(LifecycleChangeInfoWithAction.class), Mockito.anyBoolean(), Mockito.anyBoolean());

	}

	@Test
	public void testIfNodeTypeNameHasValidPrefix() {
		final List<String> definedNodeTypeNamespaceList = ConfigurationManager.getConfigurationManager()
			.getConfiguration().getDefinedResourceNamespace();

		definedNodeTypeNamespaceList.parallelStream().forEach(validNodeTypePrefix -> {
			final String nodeName = validNodeTypePrefix + "." + "abc";
			final Optional<String> result = bl.validateNodeTypeNamePrefix(nodeName, definedNodeTypeNamespaceList);
			assertTrue(result.isPresent());
		});
	}

	@Test
	public void updateNestedResource_typeIsNew() throws IOException {
		Resource resourceToUpdate = createResourceObject(false);
		String nodeName = Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX + "." + "abc";
		String jsonContent = ImportUtilsTest.loadFileNameToJsonString("normative-types-new-webServer.yml");
		CsarInfo csarInfo = new CsarInfo(user, "abcd1234", new HashMap<>(), RESOURCE_NAME, "template name", jsonContent,
				true);
		String nestedResourceName = bl.buildNestedToscaResourceName(resourceToUpdate.getResourceType()
				.name(), csarInfo.getVfResourceName(), nodeName)
				.getRight();
		when(toscaOperationFacade.getLatestByName(resourceToUpdate.getName()))
				.thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
		when(toscaOperationFacade.getLatestByToscaResourceName(resourceToUpdate.getToscaResourceName()))
				.thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
		when(toscaOperationFacade.getLatestByToscaResourceName(nestedResourceName))
				.thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));

		ImmutablePair<Resource, ActionStatus> createOrUpdateResource = bl
				.createOrUpdateResourceByImport(resourceToUpdate, user, false, false, false, csarInfo, nodeName, false);
		assertThat(createOrUpdateResource).isNotNull();

		Mockito.verify(toscaOperationFacade, times(1))
				.createToscaComponent(eq(resourceToUpdate));
		Mockito.verify(toscaOperationFacade, times(0))
				.overrideComponent(any(Resource.class), any(Resource.class));
		Mockito.verify(lifecycleBl, times(0))
				.changeState(anyString(), eq(user), eq(LifeCycleTransitionEnum.CHECKOUT),
						any(LifecycleChangeInfoWithAction.class), Mockito.anyBoolean(), Mockito.anyBoolean());
	}

	@Test
	public void updateNestedResource_typeExists() throws IOException {
		createRoot();
		Resource resourceToUpdate = createResourceObject(false);
		setCanWorkOnResource(resourceResponse);
		String nodeName = Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX + "." + "abc";
		String jsonContent = ImportUtilsTest.loadFileNameToJsonString("normative-types-new-webServer.yml");
		CsarInfo csarInfo = new CsarInfo(user, "abcd1234", new HashMap<>(), RESOURCE_NAME, "template name", jsonContent,
				true);
		String nestedResourceName = bl.buildNestedToscaResourceName(resourceToUpdate.getResourceType()
				.name(), csarInfo.getVfResourceName(), nodeName)
				.getRight();
		when(toscaOperationFacade.getLatestByName(resourceToUpdate.getName()))
				.thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
		when(toscaOperationFacade.getLatestByToscaResourceName(resourceToUpdate.getToscaResourceName()))
				.thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
		when(toscaOperationFacade.getLatestByToscaResourceName(nestedResourceName))
				.thenReturn(Either.left(resourceResponse));
		when(toscaOperationFacade.overrideComponent(any(Resource.class), any(Resource.class)))
				.thenReturn(Either.left(resourceResponse));

		ImmutablePair<Resource, ActionStatus> createOrUpdateResource = bl
				.createOrUpdateResourceByImport(resourceToUpdate, user, false, false, false, csarInfo, nodeName, false);
		assertThat(createOrUpdateResource).isNotNull();
		Mockito.verify(toscaOperationFacade, times(1))
				.overrideComponent(any(Resource.class), any(Resource.class));
		Mockito.verify(lifecycleBl, times(0))
				.changeState(anyString(), eq(user), eq(LifeCycleTransitionEnum.CHECKOUT),
						any(LifecycleChangeInfoWithAction.class), Mockito.anyBoolean(), Mockito.anyBoolean());
	}

	@Test
	public void testValidatePropertiesDefaultValues_SuccessfullWithoutProperties() {
		Resource basic = createResourceObject(true);

		Boolean validatePropertiesDefaultValues = bl.validatePropertiesDefaultValues(basic);
		assertTrue(validatePropertiesDefaultValues);
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
		Boolean validatePropertiesDefaultValues = bl.validatePropertiesDefaultValues(basic);
		assertTrue(validatePropertiesDefaultValues);
	}

	@Test(expected = ComponentException.class)
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
		bl.validatePropertiesDefaultValues(basic);
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
					.left()
					.value();
			assertEquals(4, foundVfArtifacts.get(ArtifactOperationEnum.CREATE)
					.size());
			assertEquals(4, foundVfArtifacts.get(ArtifactOperationEnum.UPDATE)
					.size());
			assertEquals(1, foundVfArtifacts.get(ArtifactOperationEnum.DELETE)
					.size());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testVFGeneratedInputs() {
		validateUserRoles(Role.ADMIN, Role.DESIGNER);
		Resource resource = createVF();
		List<InputDefinition> inputs = resource.getInputs();
		assertEquals(6, inputs.size());
		for (InputDefinition input : inputs) {
			assertThat(input.getOwnerId()).isNotNull();
		}
		assertEquals(resource.getDerivedFromGenericType(), genericVF.getToscaResourceName());
		assertEquals(resource.getDerivedFromGenericVersion(), genericVF.getVersion());
	}

	@Test
	public void testCRGeneratedInputs() {
		validateUserRoles(Role.ADMIN, Role.DESIGNER);
		Resource resource = createCR();
		List<InputDefinition> inputs = resource.getInputs();
		assertEquals(3, inputs.size());
		for (InputDefinition input : inputs) {
			assertThat(input.getOwnerId()).isNotNull();
		}
		assertEquals(resource.getDerivedFromGenericType(), genericCR.getToscaResourceName());
		assertEquals(resource.getDerivedFromGenericVersion(), genericCR.getVersion());
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
		assertEquals(6, currentInputs.stream()
				.filter(p -> null != p.getOwnerId())
				.collect(Collectors.toList())
				.size());
		Either<Boolean, ResponseFormat> upgradeToLatestGeneric = bl.shouldUpgradeToLatestGeneric(resource);
		// verify success
		assertTrue(upgradeToLatestGeneric.isLeft());
		// verify update required and valid
		assertTrue(upgradeToLatestGeneric.left()
				.value());
		// verify version was upgraded
		assertNotEquals(resource.getDerivedFromGenericVersion(), currentDerivedFromVersion);
		// verify inputs were not deleted
		assertEquals(6, resource.getInputs()
				.size());
		// verify inputs ownerId fields were removed - user may delete/edit
		// inputs
		assertEquals(6, resource.getInputs()
				.stream()
				.filter(p -> null == p.getOwnerId())
				.collect(Collectors.toList())
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
		resource.getInputs()
				.add(new InputDefinition(newProp));

		// create a new generic version with a new property which has the same
		// name as a user defined input on the VF with a different type
		genericVF.setVersion("2.0");
		newProp.setType("string");
		genericVF.setProperties(new ArrayList<>());
		genericVF.getProperties()
				.add(newProp);
		when(genericTypeBusinessLogic.fetchDerivedFromGenericType(resource)).thenReturn(Either.left(genericVF));
		when(genericTypeBusinessLogic.convertGenericTypePropertiesToInputsDefintion(genericVF.getProperties(),
				genericVF.getUniqueId())).thenCallRealMethod();
		String currentDerivedFromVersion = resource.getDerivedFromGenericVersion();
		assertEquals(6, resource.getInputs()
				.stream()
				.filter(p -> null != p.getOwnerId())
				.collect(Collectors.toList())
				.size());
		Either<Boolean, ResponseFormat> upgradeToLatestGeneric = bl.shouldUpgradeToLatestGeneric(resource);
		// verify success
		assertTrue(upgradeToLatestGeneric.isLeft());
		// verify update is invalid an void
		assertFalse(upgradeToLatestGeneric.left()
				.value());
		// verify version was not upgraded
		assertEquals(resource.getDerivedFromGenericVersion(), currentDerivedFromVersion);
		// verify inputs were not removed
		assertEquals(7, resource.getInputs()
				.size());
		// verify user defined input exists
		assertEquals(1, resource.getInputs()
				.stream()
				.filter(p -> null == p.getOwnerId())
				.collect(Collectors.toList())
				.size());
		assertEquals("integer", resource.getInputs()
				.stream()
				.filter(p -> null == p.getOwnerId())
				.findAny()
				.get()
				.getType());
	}

	@Test
	public void testPNFGeneratedInputsNoGeneratedInformationalArtifacts() {
		validateUserRoles(Role.ADMIN, Role.DESIGNER);
		Resource resource = createPNF();
		List<InputDefinition> inputs = resource.getInputs();
		assertEquals(3, inputs.size());
		for (InputDefinition input : inputs) {
			assertThat(input.getOwnerId()).isNotNull();
		}
		assertEquals(resource.getDerivedFromGenericType(), genericPNF.getToscaResourceName());
		assertEquals(resource.getDerivedFromGenericVersion(), genericPNF.getVersion());
		assertEquals(0, resource.getArtifacts()
				.size());
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
		Resource createdResource = bl.createResource(resource, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertThat(createdResource).isNotNull();
		return createdResource;
	}

	private Resource createRoot() {
		rootType = setupGenericTypeMock(GENERIC_ROOT_NAME);
		when(toscaOperationFacade.getLatestByToscaResourceName(GENERIC_ROOT_NAME))
				.thenReturn(Either.left(rootType));
		return rootType;
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
		Resource createdResource = bl.createResource(resource, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertThat(createdResource).isNotNull();
		return createdResource;
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
		Resource createdResource = bl.createResource(resource, AuditingActionEnum.CREATE_RESOURCE, user, null, null);
		assertThat(createdResource).isNotNull();
		return createdResource;
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
				put("cr_function", "string");
				put("cr_role", "string");
				put("cr_type", "string");
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
		genericType.setAbstract(true);
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
		List<Role> listOfRoles = Stream.of(roles)
				.collect(Collectors.toList());
	}

	@Test
	public void testUpdateVolumeGroup() {
		Resource resource = getResourceWithType("HEAT_VOL", "org.openecomp.groups.VfModule");
		bl.updateVolumeGroup(resource);
		assertThat(resource.getGroups().get(0).getProperties().get(0).getValue()).isEqualTo(Boolean.toString(true));
	}

	@Test
	public void testUpdateVolumeGroupNull() {
		Resource resource = getResourceWithType("HEAT_VOL", "org.openecomp.groups.VfModule");
		resource.setGroups(null);
		bl.updateVolumeGroup(resource);
	}

	@Test
	public void testUpdateVolumeGroupFail() {
		Resource resource = getResourceWithType("NON_EXIST_HEAT", "org.openecomp.groups.VfModule");
		bl.updateVolumeGroup(resource);
		assertThat(resource.getGroups().get(0).getProperties().get(0).getValue()).isEqualTo(Boolean.toString(false));
	}

	private Resource getResourceWithType(String artifactType, String groupDefinitionType) {
		ArtifactDefinition artifactToUpdate = new ArtifactDefinition();
		List<GroupDefinition> groups = new ArrayList<>();
		GroupDefinition gd = new GroupDefinition();
		List<PropertyDataDefinition> properties = new ArrayList<>();
		PropertyDataDefinition pdd = new PropertyDataDefinition();
		Map<String, ArtifactDefinition> artifacts = new HashMap<>();
		List<String> artifactsList = new ArrayList<>();

		artifactToUpdate.setArtifactType(artifactType);
		artifactToUpdate.setArtifactName(artifactType);
		artifactToUpdate.setUniqueId(artifactType);
		Resource resource = createResourceObjectCsar(true);
		artifactsList.add(artifactToUpdate.getArtifactName());


		pdd.setName("volume_group");
		pdd.setValue("true");
		pdd.setType(ToscaPropertyType.BOOLEAN.getType());

		artifacts.put(artifactToUpdate.getArtifactName(), artifactToUpdate);

		properties.add(pdd);
		gd.setType(groupDefinitionType);
		gd.setProperties(properties);
		gd.setArtifacts(artifactsList);
		groups.add(gd);

		resource.setGroups(groups);
		resource.setDeploymentArtifacts(artifacts);
		return resource;
	}


    @Test
    public void testgetAllCertifiedResources() throws Exception {
        List<Resource> list = bl.getAllCertifiedResources(true, HighestFilterEnum.HIGHEST_ONLY, "USER");
        Assert.assertEquals(reslist,list);
    }

    @Test(expected = StorageException.class)
    public void testgetAllCertifiedResources_exception() throws Exception {
        List<Resource> list = bl.getAllCertifiedResources(false, HighestFilterEnum.NON_HIGHEST_ONLY, "USER");
        Assert.assertEquals(reslist,list);
    }

    @Test
    public void testvalidateResourceNameExists() throws Exception {
        Either<Map<String, Boolean>, ResponseFormat> res = bl.validateResourceNameExists("Resource", ResourceTypeEnum.CR, "jh0003");
        Assert.assertEquals(true,res.isLeft());
    }


}
