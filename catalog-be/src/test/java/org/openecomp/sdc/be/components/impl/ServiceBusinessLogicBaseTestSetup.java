/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletContext;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.openecomp.sdc.ElementOperationMock;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.distribution.engine.DistributionEngine;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.generic.GenericTypeBusinessLogic;
import org.openecomp.sdc.be.components.path.ForwardingPathValidator;
import org.openecomp.sdc.be.components.validation.ServiceDistributionValidation;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.components.validation.component.ComponentContactIdValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentDescriptionValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentFieldValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentIconValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentNameValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentProjectCodeValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentTagsValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentValidator;
import org.openecomp.sdc.be.components.validation.service.ServiceCategoryValidator;
import org.openecomp.sdc.be.components.validation.service.ServiceEnvironmentContextValidator;
import org.openecomp.sdc.be.components.validation.service.ServiceFieldValidator;
import org.openecomp.sdc.be.components.validation.service.ServiceFunctionValidator;
import org.openecomp.sdc.be.components.validation.service.ServiceInstantiationTypeValidator;
import org.openecomp.sdc.be.components.validation.service.ServiceNamingPolicyValidator;
import org.openecomp.sdc.be.components.validation.service.ServiceRoleValidator;
import org.openecomp.sdc.be.components.validation.service.ServiceTypeValidator;
import org.openecomp.sdc.be.components.validation.service.ServiceValidator;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.datamodel.utils.UiComponentDataConverter;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.facade.operations.CatalogOperation;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;
import org.openecomp.sdc.be.resources.data.auditing.DistributionDeployEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionNotificationEvent;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.web.context.WebApplicationContext;

class ServiceBusinessLogicBaseTestSetup extends BaseBusinessLogicMock {

    protected static final String SERVICE_CATEGORY = "Mobility";
    protected static final String INSTANTIATION_TYPE = "A-la-carte";
    protected static final String CERTIFIED_VERSION = "1.0";
    protected static final String UNCERTIFIED_VERSION = "0.2";
    protected static final String COMPONNET_ID = "myUniqueId";
    protected static final String GENERIC_SERVICE_NAME = "org.openecomp.resource.abstract.nodes.service";
    protected static final String SERVICE_ROLE = JsonPresentationFields.SERVICE_ROLE.getPresentation();
    protected static final String SERVICE_TYPE = JsonPresentationFields.SERVICE_TYPE.getPresentation();
    protected static final String SERVICE_FUNCTION = JsonPresentationFields.SERVICE_FUNCTION.getPresentation();
    protected final ServletContext servletContext = Mockito.mock(ServletContext.class);
    protected final ComponentValidator componentValidator = Mockito.mock(ComponentValidator.class);
    protected ServiceBusinessLogic bl;
    protected UserBusinessLogic mockUserAdmin = Mockito.mock(UserBusinessLogic.class);
    protected WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
    protected WebApplicationContext webAppContext = Mockito.mock(WebApplicationContext.class);
    protected ResponseFormatManager responseManager = null;
    protected ComponentsUtils componentsUtils = new ComponentsUtils(Mockito.mock(AuditingManager.class));
    protected AuditCassandraDao auditingDao = Mockito.mock(AuditCassandraDao.class);
    protected ArtifactsBusinessLogic artifactBl = Mockito.mock(ArtifactsBusinessLogic.class);
    protected GraphLockOperation graphLockOperation = Mockito.mock(GraphLockOperation.class);
    protected JanusGraphDao mockJanusGraphDao = Mockito.mock(JanusGraphDao.class);
    protected ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
    protected GenericTypeBusinessLogic genericTypeBusinessLogic = Mockito.mock(GenericTypeBusinessLogic.class);
    protected UserValidations userValidations = Mockito.mock(UserValidations.class);
    protected ResourceAdminEvent auditArchive1 = Mockito.mock(ResourceAdminEvent.class);
    protected CatalogOperation catalogOperation = Mockito.mock(CatalogOperation.class);
    protected ResourceAdminEvent auditArchive2 = Mockito.mock(ResourceAdminEvent.class);
    protected ResourceAdminEvent auditRestore = Mockito.mock(ResourceAdminEvent.class);
    protected ModelOperation modelOperation = Mockito.mock(ModelOperation.class);
    protected ServiceTypeValidator serviceTypeValidator = new ServiceTypeValidator(componentsUtils);
    protected ServiceRoleValidator serviceRoleValidator = new ServiceRoleValidator(componentsUtils);
    protected ServiceFunctionValidator serviceFunctionValidator = new ServiceFunctionValidator(componentsUtils);
    protected ServiceInstantiationTypeValidator serviceInstantiationTypeValidator = new ServiceInstantiationTypeValidator(componentsUtils);
    protected ComponentDescriptionValidator componentDescriptionValidator = new ComponentDescriptionValidator(componentsUtils);
    protected ComponentProjectCodeValidator componentProjectCodeValidator = new ComponentProjectCodeValidator(componentsUtils);
    protected ComponentIconValidator componentIconValidator = new ComponentIconValidator(componentsUtils);
    protected ComponentContactIdValidator componentContactIdValidator = new ComponentContactIdValidator(componentsUtils);
    protected ComponentTagsValidator componentTagsValidator = new ComponentTagsValidator(componentsUtils);
    protected ComponentNameValidator componentNameValidator = new ComponentNameValidator(componentsUtils, toscaOperationFacade);
    protected User user = null;
    protected Resource genericService = null;
    IElementOperation mockElementDao = new ElementOperationMock();
    protected ServiceCategoryValidator serviceCategoryValidator = new ServiceCategoryValidator(componentsUtils, mockElementDao);
    protected ServiceValidator serviceValidator = createServiceValidator();
    DistributionEngine distributionEngine = Mockito.mock(DistributionEngine.class);
    ServiceDistributionValidation serviceDistributionValidation = Mockito.mock(ServiceDistributionValidation.class);
    ComponentInstanceBusinessLogic componentInstanceBusinessLogic = Mockito.mock(ComponentInstanceBusinessLogic.class);
    ForwardingPathValidator forwardingPathValidator = Mockito.mock(ForwardingPathValidator.class);
    UiComponentDataConverter uiComponentDataConverter = Mockito.mock(UiComponentDataConverter.class);
    protected final InputsBusinessLogic inputsBusinessLogic = Mockito.mock(InputsBusinessLogic.class);

    public ServiceBusinessLogicBaseTestSetup() {

    }

    protected ServiceValidator createServiceValidator() {
        List<ComponentFieldValidator> componentFieldValidators = Arrays.asList(componentContactIdValidator,
            componentDescriptionValidator,
            componentIconValidator, componentNameValidator,
            new ComponentProjectCodeValidator(componentsUtils),
            componentTagsValidator);

        List<ServiceFieldValidator> serviceFieldValidators = Arrays.asList(serviceCategoryValidator, new ServiceEnvironmentContextValidator(),
            serviceInstantiationTypeValidator, new ServiceNamingPolicyValidator(componentsUtils),
            serviceRoleValidator, serviceTypeValidator);
        return new ServiceValidator(componentsUtils, componentFieldValidators, serviceFieldValidators);
    }

    @BeforeEach
    public void setup() {

        // Elements
        IElementOperation mockElementDao = new ElementOperationMock();

        // User data and management
        user = new User();
        user.setUserId("jh0003");
        user.setFirstName("Jimmi");
        user.setLastName("Hendrix");
        user.setRole(Role.ADMIN.name());

        when(mockUserAdmin.getUser("jh0003", false)).thenReturn(user);
        when(userValidations.validateUserExists("jh0003")).thenReturn(user);
        when(userValidations.validateUserNotEmpty(eq(user), anyString())).thenReturn(user);
        when(servletContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).thenReturn(configurationManager);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webAppContext);
        when(webAppContext.getBean(IElementOperation.class)).thenReturn(mockElementDao);
        when(graphLockOperation.lockComponent(Mockito.anyString(), Mockito.eq(NodeTypeEnum.Service))).thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.lockComponentByName(Mockito.anyString(), Mockito.eq(NodeTypeEnum.Service))).thenReturn(StorageOperationStatus.OK);
        when(catalogOperation.updateCatalog(Mockito.any(), Mockito.any())).thenReturn(ActionStatus.OK);
        // artifact bussinesslogic
        ArtifactDefinition artifactDef = new ArtifactDefinition();
        when(artifactBl.createArtifactPlaceHolderInfo(Mockito.any(), Mockito.anyString(), Mockito.anyMap(), Mockito.any(User.class),
            Mockito.any(ArtifactGroupTypeEnum.class))).thenReturn(artifactDef);

        // createService
        Service serviceResponse = createServiceObject(true);
        Either<Component, StorageOperationStatus> eitherCreate = Either.left(serviceResponse);
        when(toscaOperationFacade.createToscaComponent(Mockito.any(Component.class))).thenReturn(eitherCreate);
        Either<Boolean, StorageOperationStatus> eitherCount = Either.left(false);
        when(toscaOperationFacade.validateComponentNameExists("Service", null, ComponentTypeEnum.SERVICE)).thenReturn(eitherCount);
        Either<Boolean, StorageOperationStatus> eitherCountExist = Either.left(true);
        when(toscaOperationFacade.validateComponentNameExists("alreadyExist", null, ComponentTypeEnum.SERVICE)).thenReturn(eitherCountExist);
        when(userValidations.validateUserExists(user)).thenReturn(user);

        genericService = setupGenericServiceMock();
        Either<Resource, StorageOperationStatus> findLatestGeneric = Either.left(genericService);
        when(toscaOperationFacade.getLatestCertifiedNodeTypeByToscaResourceName(GENERIC_SERVICE_NAME)).thenReturn(findLatestGeneric);

        bl = new ServiceBusinessLogic(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, groupBusinessLogic, interfaceOperation,
            interfaceLifecycleTypeOperation, artifactBl, distributionEngine, componentInstanceBusinessLogic, serviceDistributionValidation,
            forwardingPathValidator, uiComponentDataConverter, artifactToscaOperation, componentContactIdValidator, componentNameValidator,
            componentTagsValidator, componentValidator, componentIconValidator, componentProjectCodeValidator, componentDescriptionValidator,
            modelOperation, serviceRoleValidator, serviceInstantiationTypeValidator, serviceCategoryValidator, serviceValidator, inputsBusinessLogic);
        bl.setComponentContactIdValidator(componentContactIdValidator);
        bl.setComponentIconValidator(componentIconValidator);
        bl.setComponentTagsValidator(componentTagsValidator);
        bl.setComponentNameValidator(componentNameValidator);
        bl.setComponentDescriptionValidator(componentDescriptionValidator);
        bl.setComponentProjectCodeValidator(componentProjectCodeValidator);
        bl.setServiceTypeValidator(serviceTypeValidator);
        bl.setServiceFunctionValidator(serviceFunctionValidator);
        bl.setElementDao(mockElementDao);
        bl.setUserAdmin(mockUserAdmin);
        bl.setArtifactBl(artifactBl);
        bl.setGraphLockOperation(graphLockOperation);
        bl.setJanusGraphDao(mockJanusGraphDao);
        bl.setToscaOperationFacade(toscaOperationFacade);
        bl.setGenericTypeBusinessLogic(genericTypeBusinessLogic);
        bl.setComponentsUtils(componentsUtils);
        bl.setCassandraAuditingDao(auditingDao);
        bl.setUserValidations(userValidations);
        bl.setCatalogOperations(catalogOperation);

        mockAuditingDaoLogic();

        responseManager = ResponseFormatManager.getInstance();


    }

    protected Resource setupGenericServiceMock() {
        Resource genericService = new Resource();
        genericService.setVersion("1.0");
        genericService.setToscaResourceName(GENERIC_SERVICE_NAME);
        return genericService;
    }

    protected Service createServiceObject(boolean afterCreate) {
        Service service = new Service();
        service.setUniqueId("sid");
        service.setName("Service");
        CategoryDefinition category = new CategoryDefinition();
        category.setName(SERVICE_CATEGORY);
        category.setIcons(Collections.singletonList("defaulticon"));
        List<CategoryDefinition> categories = new ArrayList<>();
        categories.add(category);
        service.setCategories(categories);
        service.setInstantiationType(INSTANTIATION_TYPE);

        service.setDescription("description");
        List<String> tgs = new ArrayList<>();
        tgs.add(service.getName());
        service.setTags(tgs);
        service.setIcon("defaulticon");
        service.setContactId("aa1234");
        service.setProjectCode("12345");
        service.setEcompGeneratedNaming(true);

        if (afterCreate) {
            service.setVersion("0.1");
            service.setUniqueId(service.getName() + ":" + service.getVersion());
            service.setCreatorUserId(user.getUserId());
            service.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
        }
        return service;
    }

    private void mockAuditingDaoLogic() {
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
        createResourceAudit.setPrevState("");
        createResourceAudit.setResourceName("MyTestResource");

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
        checkOutResourceAudit.setPrevState("NOT_CERTIFIED_CHECKIN");
        checkOutResourceAudit.setResourceName("MyTestResource");
        List<ResourceAdminEvent> list = new ArrayList<>() {
            {
                add(createResourceAudit);
                add(checkInResourceAudit);
                add(checkOutResourceAudit);
            }
        };
        Either<List<ResourceAdminEvent>, ActionStatus> result = Either.left(list);
        Mockito.when(auditingDao.getByServiceInstanceId(Mockito.anyString())).thenReturn(result);

        List<ResourceAdminEvent> listPrev = new ArrayList<>();
        Either<List<ResourceAdminEvent>, ActionStatus> resultPrev = Either.left(listPrev);
        Mockito.when(auditingDao.getAuditByServiceIdAndPrevVersion(Mockito.anyString(), Mockito.anyString())).thenReturn(resultPrev);

        List<ResourceAdminEvent> listCurr = new ArrayList<>() {
            {
                add(checkOutResourceAudit);
            }
        };
        Either<List<ResourceAdminEvent>, ActionStatus> resultCurr = Either.left(listCurr);
        Mockito.when(auditingDao.getAuditByServiceIdAndCurrVersion(Mockito.anyString(), Mockito.anyString())).thenReturn(resultCurr);

        Either<List<ResourceAdminEvent>, ActionStatus> archiveAuditList = Either.left(Arrays.asList(auditArchive1, auditArchive2));
        when(auditingDao.getArchiveAuditByServiceInstanceId(anyString())).thenReturn(archiveAuditList);

        Either<List<ResourceAdminEvent>, ActionStatus> restoreAuditList = Either.left(Collections.singletonList(auditRestore));
        when(auditingDao.getRestoreAuditByServiceInstanceId(anyString())).thenReturn(restoreAuditList);

    }

    protected void setupBeforeDeploy(String notifyAction, String requestAction, String did) {

        DistributionNotificationEvent notifyEvent = new DistributionNotificationEvent();
        notifyEvent.setAction(notifyAction);
        notifyEvent.setDid(did);
        notifyEvent.setStatus("200");

        ResourceAdminEvent requestEvent = new ResourceAdminEvent();
        requestEvent.setAction(requestAction);
        requestEvent.setDid(did);
        requestEvent.setStatus("200");

        List<DistributionNotificationEvent> notifyResults = Collections.singletonList(notifyEvent);
        Either<List<DistributionNotificationEvent>, ActionStatus> eitherNotify = Either.left(notifyResults);

        Mockito.when(auditingDao.getDistributionNotify(Mockito.anyString(), Mockito.eq(notifyAction))).thenReturn(eitherNotify);

        List<ResourceAdminEvent> requestResults = Collections.singletonList(requestEvent);
        Either<List<ResourceAdminEvent>, ActionStatus> eitherRequest = Either.left(requestResults);
        Mockito.when(auditingDao.getDistributionRequest(Mockito.anyString(), Mockito.eq(requestAction))).thenReturn(eitherRequest);

        Either<Component, StorageOperationStatus> eitherService = Either.left(createServiceObject(true));
        Mockito.when(toscaOperationFacade.getToscaElement(Mockito.anyString())).thenReturn(eitherService);

        Either<List<DistributionDeployEvent>, ActionStatus> emptyEventList = Either.left(Collections.emptyList());
        Mockito.when(auditingDao.getDistributionDeployByStatus(Mockito.anyString(), Mockito.eq("DResult"), Mockito.anyString()))
            .thenReturn(emptyEventList);
    }

    private void assertResponse(Either<Service, ResponseFormat> createResponse, ActionStatus expectedStatus, String... variables) {
        assertResponse(createResponse.right().value(), expectedStatus, variables);
    }

    protected void assertComponentException(ComponentException e, ActionStatus expectedStatus, String... variables) {
        ResponseFormat actualResponse = e.getResponseFormat() != null ?
            e.getResponseFormat() : componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams());
        assertResponse(actualResponse, expectedStatus, variables);
    }

    protected void assertResponse(ResponseFormat actualResponse, ActionStatus expectedStatus, String... variables) {
        ResponseFormat expectedResponse = responseManager.getResponseFormat(expectedStatus, variables);
        assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
        assertEquals(expectedResponse.getFormattedMessage(), actualResponse.getFormattedMessage(), "assert error description");
    }

}
