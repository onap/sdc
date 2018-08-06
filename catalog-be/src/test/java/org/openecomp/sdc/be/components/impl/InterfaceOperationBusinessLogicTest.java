/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.ElementOperationMock;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.test.utils.InterfaceOperationTestUtils;
import org.openecomp.sdc.be.components.impl.generic.GenericTypeBusinessLogic;
import org.openecomp.sdc.be.components.validation.InterfaceOperationValidation;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsontitan.operations.*;
import org.openecomp.sdc.be.model.jsontitan.utils.InterfaceUtils;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IPropertyOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class InterfaceOperationBusinessLogicTest {

    public static final String RESOURCE_CATEGORY1 = "Network Layer 2-3";
    public static final String RESOURCE_SUBCATEGORY = "Router";

    private String resourceId = "resourceId1";
    private String operationId = "uniqueId1";
    Resource resourceUpdate;
    Operation operation;

    public static final String RESOURCE_NAME = "My-Resource_Name with   space";

    final ServletContext servletContext = Mockito.mock(ServletContext.class);
    IElementOperation mockElementDao;
    TitanDao mockTitanDao = Mockito.mock(TitanDao.class);
    UserBusinessLogic mockUserAdmin = Mockito.mock(UserBusinessLogic.class);
    ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
    NodeTypeOperation nodeTypeOperation = Mockito.mock(NodeTypeOperation.class);
    NodeTemplateOperation nodeTemplateOperation = Mockito.mock(NodeTemplateOperation.class);
    TopologyTemplateOperation topologyTemplateOperation = Mockito.mock(TopologyTemplateOperation.class);
    final IPropertyOperation propertyOperation = Mockito.mock(IPropertyOperation.class);
    final ApplicationDataTypeCache applicationDataTypeCache = Mockito.mock(ApplicationDataTypeCache.class);
    WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
    UserValidations userValidations = Mockito.mock(UserValidations.class);
    WebApplicationContext webAppContext = Mockito.mock(WebApplicationContext.class);
    ArtifactCassandraDao artifactCassandraDao = Mockito.mock(ArtifactCassandraDao.class);
    InterfaceOperation interfaceOperation = Mockito.mock(InterfaceOperation.class);
    InterfaceOperationValidation operationValidator = Mockito.mock(InterfaceOperationValidation.class);

    ResponseFormatManager responseManager = null;
    GraphLockOperation graphLockOperation = Mockito.mock(GraphLockOperation.class);
    User user = null;
    Resource resourceResponse = null;
    ComponentsUtils componentsUtils;
    ArtifactsBusinessLogic artifactManager = new ArtifactsBusinessLogic();
    private GenericTypeBusinessLogic genericTypeBusinessLogic = Mockito.mock(GenericTypeBusinessLogic.class);

    @InjectMocks
    InterfaceOperationBusinessLogic bl = new InterfaceOperationBusinessLogic();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.reset(propertyOperation);

        ExternalConfiguration.setAppName("catalog-be");

        // init Configuration
        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
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
        when(userValidations.validateUserExists(eq(user.getUserId()), anyString(), eq(false))).thenReturn(user);
        when(userValidations.validateUserNotEmpty(eq(user), anyString())).thenReturn(user);
        // Servlet Context attributes
        when(servletContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).thenReturn(configurationManager);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webAppContext);
        when(webAppContext.getBean(IElementOperation.class)).thenReturn(mockElementDao);

        Either<Boolean, StorageOperationStatus> eitherFalse = Either.left(true);
        when(toscaOperationFacade.validateComponentNameExists("Root", ResourceTypeEnum.VFC, ComponentTypeEnum.RESOURCE)).thenReturn(eitherFalse);

        Either<Boolean, StorageOperationStatus> eitherCountExist = Either.left(true);
        when(toscaOperationFacade.validateComponentNameExists("alreadyExists", ResourceTypeEnum.VFC, ComponentTypeEnum.RESOURCE)).thenReturn(eitherCountExist);

        Either<Boolean, StorageOperationStatus> eitherCount = Either.left(false);
        when(toscaOperationFacade.validateComponentNameExists(eq(RESOURCE_NAME), any(ResourceTypeEnum.class), eq(ComponentTypeEnum.RESOURCE))).thenReturn(eitherCount);

        Either<Boolean, StorageOperationStatus> validateDerivedExists = Either.left(true);
        when(toscaOperationFacade.validateToscaResourceNameExists("Root")).thenReturn(validateDerivedExists);

        Either<Boolean, StorageOperationStatus> validateDerivedNotExists = Either.left(false);
        when(toscaOperationFacade.validateToscaResourceNameExists("kuku")).thenReturn(validateDerivedNotExists);
        when(graphLockOperation.lockComponent(Mockito.anyString(), eq(NodeTypeEnum.Resource))).thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.lockComponentByName(Mockito.anyString(), eq(NodeTypeEnum.Resource))).thenReturn(StorageOperationStatus.OK);

        // createResource
        resourceResponse = createResourceObject(true);
        Either<Resource, StorageOperationStatus> eitherCreate = Either.left(resourceResponse);
        Either<Integer, StorageOperationStatus> eitherValidate = Either.left(null);
        when(toscaOperationFacade.createToscaComponent(any(Resource.class))).thenReturn(eitherCreate);
        //TODO Remove if passes
        /*when(toscaOperationFacade.validateCsarUuidUniqueness(Mockito.anyString())).thenReturn(eitherValidate);*/
        Map<String, DataTypeDefinition> emptyDataTypes = new HashMap<String, DataTypeDefinition>();
        when(applicationDataTypeCache.getAll()).thenReturn(Either.left(emptyDataTypes));

        //InterfaceOperation
        when(operationValidator.validateInterfaceOperations(anyCollection(), anyString(), anyBoolean())).thenReturn(Either.left(true));
        when(interfaceOperation.addInterface(anyString(), anyObject())).thenReturn(Either.left(InterfaceOperationTestUtils.mockInterfaceDefinitionToReturn(RESOURCE_NAME)));
        when(interfaceOperation.updateInterface(anyString(), anyObject())).thenReturn(Either.left(InterfaceOperationTestUtils.mockInterfaceDefinitionToReturn(RESOURCE_NAME)));
        when(interfaceOperation.addInterfaceOperation(anyObject(), anyObject(), anyObject())).thenReturn(Either.left(InterfaceOperationTestUtils.mockOperationToReturn(RESOURCE_NAME)));
        when(interfaceOperation.updateInterfaceOperation(anyObject(), anyObject(), anyObject())).thenReturn(Either.left(InterfaceOperationTestUtils.mockOperationToReturn(RESOURCE_NAME)));
        when(interfaceOperation.getInterfaceOperation(anyObject(), anyObject())).thenReturn(Either.left(InterfaceOperationTestUtils.mockOperationToReturn(RESOURCE_NAME)));
        when(interfaceOperation.deleteInterfaceOperation(anyObject(), anyObject(), anyObject())).thenReturn(Either.left(InterfaceOperationTestUtils.mockOperationToReturn(RESOURCE_NAME)));
        when(interfaceOperation.deleteInterfaceOperation(any(),any(), any())).thenReturn(Either.left(InterfaceOperationTestUtils.mockOperationToReturn(RESOURCE_NAME)));
        when(interfaceOperation.updateInterface(any(),any())).thenReturn(Either.left(InterfaceOperationTestUtils.mockInterfaceDefinitionToReturn(RESOURCE_NAME)));
        when(mockTitanDao.commit()).thenReturn(TitanOperationStatus.OK);

        // BL object
        artifactManager.setNodeTemplateOperation(nodeTemplateOperation);
        bl = new InterfaceOperationBusinessLogic();

        bl.setUserAdmin(mockUserAdmin);
        bl.setComponentsUtils(componentsUtils);
        bl.setGraphLockOperation(graphLockOperation);
        bl.setTitanGenericDao(mockTitanDao);
        bl.setGenericTypeBusinessLogic(genericTypeBusinessLogic);
        toscaOperationFacade.setNodeTypeOperation(nodeTypeOperation);
        toscaOperationFacade.setTopologyTemplateOperation(topologyTemplateOperation);
        bl.setToscaOperationFacade(toscaOperationFacade);
        bl.setUserValidations(userValidations);
        bl.setInterfaceOperation(interfaceOperation);
        bl.setInterfaceOperationValidation(operationValidator);
        Resource resourceCsar = createResourceObjectCsar(true);
        setCanWorkOnResource(resourceCsar);
        Either<Component, StorageOperationStatus> oldResourceRes = Either.left(resourceCsar);
        when(toscaOperationFacade.getToscaFullElement(resourceCsar.getUniqueId())).thenReturn(oldResourceRes);
        responseManager = ResponseFormatManager.getInstance();
    }

    @Test
    public void createInterfaceOperationTest() {
        Resource resource = createResourceForInterfaceOperation();
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(resource));
        operation = InterfaceOperationTestUtils.createMockOperation();
        Either<Operation, ResponseFormat> interfaceOperation = bl.createInterfaceOperation(resourceId, operation, user, true);
        Assert.assertTrue(interfaceOperation.isLeft());
        Assert.assertNotNull(interfaceOperation.left().value().getWorkflowId());
        Assert.assertNotNull(interfaceOperation.left().value().getWorkflowVersionId());
    }

    @Test
    public void updateInterfaceOperationTest() {
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        operation = InterfaceOperationTestUtils.createMockOperation();
        Resource resource = createResourceForInterfaceOperation();
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(resource));
        Either<Operation, ResponseFormat> interfaceOperation = bl.updateInterfaceOperation(resourceId, operation, user, true);
        Assert.assertTrue(interfaceOperation.isLeft());
    }

    @Test
    public void deleteInterfaceOperationTest() {
        Resource resource = createResourceForInterfaceOperation();
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(resource));
        when(artifactCassandraDao.deleteArtifact(any(String.class))).thenReturn(CassandraOperationStatus.OK);
        Either<Operation, ResponseFormat> deleteResourceResponseFormatEither = bl.deleteInterfaceOperation(resourceId, operationId, user, true);
        Assert.assertTrue(deleteResourceResponseFormatEither.isLeft());
    }

    @Test
    public void getInterfaceOperationTest() {
        Resource resource = createResourceForInterfaceOperation();
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(resource));
        Either<Operation, ResponseFormat> getResourceResponseFormatEither = bl.getInterfaceOperation(resourceId, operationId, user, true);
        Assert.assertTrue(getResourceResponseFormatEither.isLeft());
    }

    private void validateUserRoles(Role... roles) {
        List<Role> listOfRoles = Stream.of(roles).collect(Collectors.toList());
     }

    private Resource setCanWorkOnResource(Resource resource) {
        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        resource.setLastUpdaterUserId(user.getUserId());
        return resource;
    }

    private Resource setUpResourceMock(){
        Resource resource = new Resource();
        resource.setUniqueId(resourceId);
        resource.setName(RESOURCE_NAME);
        resource.addCategory(RESOURCE_CATEGORY1, RESOURCE_SUBCATEGORY);
        resource.setDescription("My short description");
        resource.setInterfaces(InterfaceOperationTestUtils.createMockInterfaceDefinition(RESOURCE_NAME));

        List<InputDefinition> inputDefinitionList = new ArrayList<>();
        inputDefinitionList.add(createInputDefinition("uniqueId1"));
        resource.setInputs(inputDefinitionList);

        return  resource;
    }

    private InputDefinition createInputDefinition(String inputId) {
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setInputId(inputId);
        inputDefinition.setDescription("Input Description");

        return  inputDefinition;

    }
    private Resource createResourceForInterfaceOperation() {
        Resource resource = new Resource();
        resource.setUniqueId(resourceId);
        resource.setName(RESOURCE_NAME);
        resource.addCategory(RESOURCE_CATEGORY1, RESOURCE_SUBCATEGORY);
        resource.setDescription("Resource name for response");
        resource.setInterfaces(InterfaceOperationTestUtils.createMockInterfaceDefinition(RESOURCE_NAME));
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



}